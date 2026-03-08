#!/usr/bin/env python3
from __future__ import annotations

import hashlib
import json
import os
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Dict, List, Optional

from dotenv import load_dotenv

try:
    import chromadb
except Exception as exc:  # pragma: no cover
    raise RuntimeError("Dependencia ausente: chromadb. Rode `pip install -r requirements.txt`.") from exc


def _safe_int(value: Any, default: int) -> int:
    try:
        return int(value)
    except Exception:
        return default


@dataclass
class RagSettings:
    repo_root: Path
    chroma_dir: Path
    chunks_file: Path
    reports_dir: Path
    collection_name: str
    top_k: int
    openai_api_key: str
    openai_embed_model: str
    openai_chat_model: str


def load_settings() -> RagSettings:
    backend_dir = Path(__file__).resolve().parent
    env_file = backend_dir / ".env"
    if env_file.exists():
        load_dotenv(env_file)
    else:
        load_dotenv()

    repo_root = backend_dir.parents[1]
    chroma_rel = os.getenv("CHROMA_DIR", "AGENTE GURPS/index/chroma")
    chunks_rel = os.getenv("CHUNKS_FILE", "AGENTE GURPS/sources/processed/chunks.jsonl")
    reports_rel = os.getenv("REPORTS_DIR", "AGENTE GURPS/sources/processed/reports")

    return RagSettings(
        repo_root=repo_root,
        chroma_dir=(repo_root / chroma_rel),
        chunks_file=(repo_root / chunks_rel),
        reports_dir=(repo_root / reports_rel),
        collection_name=os.getenv("RAG_COLLECTION", "gurps_pt_v1"),
        top_k=_safe_int(os.getenv("RAG_TOP_K"), 6),
        openai_api_key=os.getenv("OPENAI_API_KEY", "").strip(),
        openai_embed_model=os.getenv("OPENAI_EMBED_MODEL", "text-embedding-3-small"),
        openai_chat_model=os.getenv("OPENAI_CHAT_MODEL", "gpt-4.1-mini"),
    )


def _hash_embedding(text: str, dim: int = 256) -> List[float]:
    vec = [0.0] * dim
    words = text.lower().split()
    if not words:
        return vec
    for token in words:
        digest = hashlib.sha256(token.encode("utf-8")).digest()
        idx = int.from_bytes(digest[:4], byteorder="big") % dim
        vec[idx] += 1.0
    norm = sum(x * x for x in vec) ** 0.5
    if norm > 0:
        vec = [x / norm for x in vec]
    return vec


def build_embeddings(settings: RagSettings, texts: List[str]) -> List[List[float]]:
    if settings.openai_api_key:
        try:
            from openai import OpenAI

            client = OpenAI(api_key=settings.openai_api_key)
            response = client.embeddings.create(model=settings.openai_embed_model, input=texts)
            return [item.embedding for item in response.data]
        except Exception:
            pass
    return [_hash_embedding(text) for text in texts]


def get_chroma_collection(settings: RagSettings):
    settings.chroma_dir.mkdir(parents=True, exist_ok=True)
    client = chromadb.PersistentClient(path=str(settings.chroma_dir))
    return client.get_or_create_collection(
        name=settings.collection_name,
        metadata={"hnsw:space": "cosine"},
    )


def load_chunks(chunks_file: Path) -> List[Dict[str, Any]]:
    if not chunks_file.exists():
        raise FileNotFoundError(f"Arquivo de chunks nao encontrado: {chunks_file}")
    items: List[Dict[str, Any]] = []
    with chunks_file.open("r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            row = json.loads(line)
            if not row.get("text"):
                continue
            items.append(row)
    return items


def retrieve_context(settings: RagSettings, question: str, top_k: Optional[int] = None) -> Dict[str, Any]:
    collection = get_chroma_collection(settings)
    k = top_k or settings.top_k
    emb = build_embeddings(settings, [question])[0]
    result = collection.query(
        query_embeddings=[emb],
        n_results=k,
        include=["documents", "metadatas", "distances"],
    )

    docs = result.get("documents", [[]])[0]
    metas = result.get("metadatas", [[]])[0]
    distances = result.get("distances", [[]])[0]

    items = []
    for doc, meta, distance in zip(docs, metas, distances):
        score = 1.0 - float(distance) if distance is not None else 0.0
        row = {
            "text": doc or "",
            "score": round(score, 4),
            "source_id": (meta or {}).get("source_id", ""),
            "source_title": (meta or {}).get("source_title", ""),
            "page_number": int((meta or {}).get("page_number", 0)),
            "chunk_id": (meta or {}).get("chunk_id", ""),
        }
        items.append(row)

    return {"items": items}


def _build_prompt(question: str, mode: str, context_items: List[Dict[str, Any]]) -> str:
    context_lines = []
    for idx, item in enumerate(context_items, start=1):
        context_lines.append(
            f"[{idx}] source_id={item['source_id']} | titulo={item['source_title']} | "
            f"pagina={item['page_number']} | score={item['score']}\n{item['text']}"
        )
    context_block = "\n\n".join(context_lines)

    return (
        "Voce e o AGENTE GURPS. Responda SEMPRE em portugues.\n"
        "Use somente o contexto recuperado. Se faltar evidencia, diga explicitamente.\n"
        "Nao invente regra canonica.\n"
        "Sempre inclua no final uma secao 'Fontes' com os itens usados no formato [n].\n"
        "Quando houver inferencia, escreva: 'Inferencia: ...'.\n\n"
        f"Modo: {mode}\n"
        f"Pergunta: {question}\n\n"
        "Contexto:\n"
        f"{context_block}\n"
    )


def answer_with_citations(settings: RagSettings, question: str, mode: str, context_items: List[Dict[str, Any]]) -> str:
    prompt = _build_prompt(question, mode, context_items)
    if settings.openai_api_key:
        try:
            from openai import OpenAI

            client = OpenAI(api_key=settings.openai_api_key)
            response = client.responses.create(
                model=settings.openai_chat_model,
                input=prompt,
                temperature=0.2,
            )
            text = response.output_text.strip()
            if text:
                return text
        except Exception:
            pass

    if not context_items:
        return (
            "Nao encontrei base suficiente para responder com confianca nesta etapa.\n"
            "Inferencia: necessario ampliar indexacao ou revisar fontes.\n"
            "Fontes: nenhuma."
        )

    top = context_items[0]
    return (
        "Resposta provisoria (modo offline, sem LLM):\n"
        f"Trecho mais proximo da pergunta:\n{top['text'][:700]}\n\n"
        "Inferencia: resposta resumida diretamente do trecho recuperado.\n"
        "Fontes:\n"
        f"[1] {top['source_title']} (source_id={top['source_id']}), pag. {top['page_number']}."
    )
