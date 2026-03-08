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


def run_case(client: TestClient, question: str, mode: str = "regras", top_k: int = 6):
    resp = client.post("/ask", json={"question": question, "mode": mode, "top_k": top_k})
    body = resp.json()
    answer = body.get("answer", "")
    return {
        "status_code": resp.status_code,
        "confidence": body.get("confidence"),
        "sources_count": len(body.get("sources", [])),
        "has_fontes": "fontes:" in answer.lower(),
        "answer_preview": answer[:220],
    }


def main() -> int:
    client = TestClient(app)
    cases = {
        "judo_funcionamento": run_case(client, "Como funciona a perícia Judô?"),
        "aptidao_magica": run_case(client, "O que é Aptidão Mágica no GURPS?"),
        "custo_por_nivel": run_case(client, "Como calcular custo por nível em vantagens no GURPS?"),
    }

    payload = {
        "generated_at_utc": datetime.now(timezone.utc).isoformat(),
        "cases": cases,
    }

    repo_root = backend_dir.parents[1]
    out = repo_root / "AGENTE GURPS" / "sources" / "processed" / "reports" / "a11_consultas_criticas_report.json"
    out.parent.mkdir(parents=True, exist_ok=True)
    with out.open("w", encoding="utf-8", newline="\n") as f:
        json.dump(payload, f, ensure_ascii=False, indent=2)

    print(f"OK: relatório gerado em {out}")
    print(json.dumps(cases, ensure_ascii=True))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
