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

