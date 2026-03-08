#!/usr/bin/env python3
from __future__ import annotations

import json
from collections import defaultdict
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Dict, List

from rag_runtime import build_embeddings, get_chroma_collection, load_chunks, load_settings


def batched(items: List[Dict[str, Any]], batch_size: int = 128):
    for start in range(0, len(items), batch_size):
        end = min(len(items), start + batch_size)
        yield items[start:end]


def main() -> int:
    settings = load_settings()
    collection = get_chroma_collection(settings)
    chunks = load_chunks(settings.chunks_file)
    collection.delete(where={})

    source_counter = defaultdict(int)
    total = 0

    for batch in batched(chunks, batch_size=128):
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

    print("OK: indexacao concluida.")
    print(json.dumps({"total_chunks_indexados": total, "collection_name": settings.collection_name}, ensure_ascii=False))
    print(f"relatorio={report_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
