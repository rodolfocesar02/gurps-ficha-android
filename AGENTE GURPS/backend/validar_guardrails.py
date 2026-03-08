#!/usr/bin/env python3
from __future__ import annotations

import json
from datetime import datetime, timezone
from pathlib import Path
import sys

from fastapi.testclient import TestClient

backend_dir = Path(__file__).resolve().parent
sys.path.insert(0, str(backend_dir))

from api_server import app  # noqa: E402


def run_case(client: TestClient, question: str, mode: str, top_k: int = 4):
    resp = client.post("/ask", json={"question": question, "mode": mode, "top_k": top_k})
    data = resp.json()
    answer = data.get("answer", "")
    return {
        "status_code": resp.status_code,
        "confidence": data.get("confidence"),
        "sources_count": len(data.get("sources", [])),
        "has_fontes": "fontes:" in answer.lower(),
        "has_inferencia": "inferencia" in answer.lower(),
        "answer_preview": answer[:180],
    }


def main() -> int:
    client = TestClient(app)

    report = {
        "generated_at_utc": datetime.now(timezone.utc).isoformat(),
        "cases": {
            "contexto_forte_esperado": run_case(
                client, "Qual o custo base de Aptidao Magica no modulo basico?", "regras"
            ),
            "contexto_fraco_esperado": run_case(
                client, "Regra inexistente de teleportar galaxias com 1 PF", "regras"
            ),
        },
    }

    repo_root = backend_dir.parents[1]
    out = repo_root / "AGENTE GURPS" / "sources" / "processed" / "reports" / "guardrails_report.json"
    out.parent.mkdir(parents=True, exist_ok=True)
    with out.open("w", encoding="utf-8", newline="\n") as f:
        json.dump(report, f, ensure_ascii=False, indent=2)

    print(f"OK: relatório gerado em {out}")
    print(json.dumps(report["cases"], ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
