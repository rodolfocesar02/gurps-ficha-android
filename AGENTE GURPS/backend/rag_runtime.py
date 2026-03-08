#!/usr/bin/env python3
from __future__ import annotations

import hashlib
import json
import os
import re
from collections import defaultdict
from datetime import datetime, timezone
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


def _resolve_env_path(raw_value: str, backend_dir: Path, must_exist: bool) -> Path:
    raw = (raw_value or "").strip()
    candidate = Path(raw)
    if candidate.is_absolute():
        return candidate

    trial_paths = [
        Path.cwd() / raw,
        backend_dir / raw,
        backend_dir.parent / raw,
        backend_dir.parent.parent / raw,
    ]
    # Compatibilidade para deploy com subdiretorio na Railway.
    if raw.startswith("AGENTE GURPS/"):
        stripped = raw[len("AGENTE GURPS/") :]
        trial_paths.extend(
            [
                Path.cwd() / stripped,
                backend_dir / stripped,
                backend_dir.parent / stripped,
                backend_dir.parent.parent / stripped,
            ]
        )

    if must_exist:
        for path in trial_paths:
            if path.exists():
                return path.resolve()
    return trial_paths[0].resolve()


@dataclass
class RagSettings:
    repo_root: Path
    chroma_dir: Path
    chunks_file: Path
    reports_dir: Path
    collection_name: str
    top_k: int
    openai_api_key: str
    openai_base_url: str
    openai_embed_model: str
    openai_chat_model: str


def load_settings() -> RagSettings:
    backend_dir = Path(__file__).resolve().parent
    env_file = backend_dir / ".env"
    if env_file.exists():
        load_dotenv(env_file)
    else:
        load_dotenv()

    chroma_rel = os.getenv("CHROMA_DIR", "AGENTE GURPS/index/chroma")
    chunks_rel = os.getenv("CHUNKS_FILE", "AGENTE GURPS/sources/processed/chunks.jsonl")
    reports_rel = os.getenv("REPORTS_DIR", "AGENTE GURPS/sources/processed/reports")

    return RagSettings(
        repo_root=Path.cwd().resolve(),
        chroma_dir=_resolve_env_path(chroma_rel, backend_dir, must_exist=False),
        chunks_file=_resolve_env_path(chunks_rel, backend_dir, must_exist=True),
        reports_dir=_resolve_env_path(reports_rel, backend_dir, must_exist=False),
        collection_name=os.getenv("RAG_COLLECTION", "gurps_pt_v1"),
        top_k=_safe_int(os.getenv("RAG_TOP_K"), 6),
        openai_api_key=os.getenv("OPENAI_API_KEY", "").strip(),
        openai_base_url=os.getenv("OPENAI_BASE_URL", "").strip(),
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

            client_kwargs = {"api_key": settings.openai_api_key}
            if settings.openai_base_url:
                client_kwargs["base_url"] = settings.openai_base_url
            client = OpenAI(**client_kwargs)
            response = client.embeddings.create(model=settings.openai_embed_model, input=texts)
            return [item.embedding for item in response.data]
        except Exception:
            pass
    return [_hash_embedding(text) for text in texts]


def get_chroma_collection(settings: RagSettings):
    client = get_chroma_client(settings)
    return client.get_or_create_collection(
        name=settings.collection_name,
        metadata={"hnsw:space": "cosine"},
    )


def get_chroma_client(settings: RagSettings):
    settings.chroma_dir.mkdir(parents=True, exist_ok=True)
    return chromadb.PersistentClient(path=str(settings.chroma_dir))


def reset_collection(settings: RagSettings):
    client = get_chroma_client(settings)
    try:
        client.delete_collection(settings.collection_name)
    except Exception:
        pass
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


def _batched(items: List[Dict[str, Any]], batch_size: int = 128):
    for start in range(0, len(items), batch_size):
        end = min(len(items), start + batch_size)
        yield items[start:end]


def reindex_collection(settings: RagSettings) -> Dict[str, Any]:
    collection = reset_collection(settings)
    chunks = load_chunks(settings.chunks_file)

    source_counter = defaultdict(int)
    total = 0

    for batch in _batched(chunks, batch_size=128):
        ids = [row["chunk_id"] for row in batch]
        docs = [row["text"] for row in batch]
        metas = []
        for row in batch:
            source_id = row.get("source_id", "")
            source_counter[source_id] += 1
            metas.append(
                {
                    "chunk_id": row.get("chunk_id", ""),
                    "source_id": source_id,
                    "source_title": row.get("source_title", ""),
                    "page_number": int(row.get("page_number", 0)),
                    "language": row.get("language", "pt"),
                }
            )

        embs = build_embeddings(settings, docs)
        collection.upsert(ids=ids, documents=docs, metadatas=metas, embeddings=embs)
        total += len(batch)

    report = {
        "generated_at_utc": datetime.now(timezone.utc).isoformat(),
        "collection_name": settings.collection_name,
        "chunks_file": str(settings.chunks_file),
        "chroma_dir": str(settings.chroma_dir),
        "total_chunks_indexados": total,
        "fontes_indexadas": dict(sorted(source_counter.items())),
    }

    settings.reports_dir.mkdir(parents=True, exist_ok=True)
    report_path = settings.reports_dir / "indexacao_chroma_report.json"
    with report_path.open("w", encoding="utf-8", newline="\n") as f:
        json.dump(report, f, ensure_ascii=False, indent=2)

    return report


def ensure_collection_ready(settings: RagSettings) -> Dict[str, Any]:
    collection = get_chroma_collection(settings)
    try:
        count = int(collection.count())
    except Exception:
        count = 0

    auto_index = os.getenv("RAG_AUTO_INDEX_ON_STARTUP", "1").strip().lower() not in {"0", "false", "no"}
    if count > 0 or not auto_index:
        return {"ready": count > 0, "auto_index": auto_index, "count": count}
    if not settings.chunks_file.exists():
        return {
            "ready": False,
            "auto_index": auto_index,
            "count": 0,
            "error": f"chunks_file nao encontrado: {settings.chunks_file}",
        }

    report = reindex_collection(settings)
    return {"ready": True, "auto_index": auto_index, "count": report.get("total_chunks_indexados", 0), "reindexed": True}


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


def format_sources_section(context_items: List[Dict[str, Any]], max_sources: int = 6) -> str:
    if not context_items:
        return "Fontes: nenhuma."
    lines = ["Fontes:"]
    for idx, item in enumerate(context_items[:max_sources], start=1):
        lines.append(
            f"[{idx}] {item['source_title']} (source_id={item['source_id']}), pag. {item['page_number']}."
        )
    return "\n".join(lines)


def build_low_confidence_answer(context_items: List[Dict[str, Any]]) -> str:
    return (
        "Nao encontrei evidencia forte o suficiente para responder com confianca.\n"
        "Inferencia: consulte os trechos e confirme no livro canonico antes de aplicar na mesa.\n"
        f"{format_sources_section(context_items)}"
    )


def _significant_tokens(question: str) -> List[str]:
    stop = {
        "qual", "quais", "como", "quando", "onde", "porque", "por", "para", "com",
        "sem", "uma", "um", "uns", "umas", "dos", "das", "de", "do", "da", "no",
        "na", "nos", "nas", "e", "ou", "que", "o", "a", "os", "as",
    }
    tokens = [t for t in re.findall(r"[a-zA-Z0-9áéíóúãõâêîôûç]+", question.lower()) if len(t) >= 3]
    return [t for t in tokens if t not in stop]


def evaluate_evidence(question: str, context_items: List[Dict[str, Any]]) -> Dict[str, Any]:
    if not context_items:
        return {"enough": False, "best_score": 0.0, "max_overlap": 0, "reason": "sem_contexto"}

    best_score = float(context_items[0].get("score", 0.0))
    tokens = _significant_tokens(question)
    max_overlap = 0

    for item in context_items:
        txt = (item.get("text") or "").lower()
        overlap = sum(1 for tok in tokens if tok in txt)
        if overlap > max_overlap:
            max_overlap = overlap

    enough = (best_score >= 0.35 and max_overlap >= 2) or (max_overlap >= 3)
    reason = "ok" if enough else "fraca"
    return {
        "enough": enough,
        "best_score": round(best_score, 4),
        "max_overlap": max_overlap,
        "reason": reason,
    }


def ensure_sources_block(answer: str, context_items: List[Dict[str, Any]]) -> str:
    if "fontes:" in answer.lower():
        return answer
    return answer.rstrip() + "\n\n" + format_sources_section(context_items)


def answer_with_citations(settings: RagSettings, question: str, mode: str, context_items: List[Dict[str, Any]]) -> str:
    prompt = _build_prompt(question, mode, context_items)
    if settings.openai_api_key:
        try:
            from openai import OpenAI

            client_kwargs = {"api_key": settings.openai_api_key}
            if settings.openai_base_url:
                client_kwargs["base_url"] = settings.openai_base_url
            client = OpenAI(**client_kwargs)

            text = ""
            try:
                # OpenAI native Responses API
                response = client.responses.create(
                    model=settings.openai_chat_model,
                    input=prompt,
                    temperature=0.2,
                )
                text = (response.output_text or "").strip()
            except Exception:
                # Compat mode (Google/OpenAI-compatible and similares)
                completion = client.chat.completions.create(
                    model=settings.openai_chat_model,
                    messages=[{"role": "user", "content": prompt}],
                    temperature=0.2,
                )
                msg = completion.choices[0].message if completion.choices else None
                text = (msg.content or "").strip() if msg else ""
            if text:
                return ensure_sources_block(text, context_items)
        except Exception:
            pass

    if not context_items:
        return build_low_confidence_answer(context_items)

    top = context_items[0]
    answer = (
        "Resposta provisoria (modo offline, sem LLM):\n"
        f"Trecho mais proximo da pergunta:\n{top['text'][:700]}\n\n"
        "Inferencia: resposta resumida diretamente do trecho recuperado.\n"
    )
    return ensure_sources_block(answer, context_items)
