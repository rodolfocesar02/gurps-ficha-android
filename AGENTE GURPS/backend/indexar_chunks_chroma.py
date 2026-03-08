#!/usr/bin/env python3
from __future__ import annotations

import json

from rag_runtime import load_settings, reindex_collection


def main() -> int:
    settings = load_settings()
    report = reindex_collection(settings)
    total = int(report.get("total_chunks_indexados", 0))
    report_path = settings.reports_dir / "indexacao_chroma_report.json"

    print("OK: indexacao concluida.")
    print(json.dumps({"total_chunks_indexados": total, "collection_name": settings.collection_name}, ensure_ascii=False))
    print(f"relatorio={report_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
