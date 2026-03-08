#!/usr/bin/env python3
from __future__ import annotations

import json
from datetime import datetime, timezone
from pathlib import Path

from dotenv import load_dotenv
import os


def main() -> int:
    root = Path(__file__).resolve().parents[2]
    env_path = root / "AGENTE GURPS" / "backend" / ".env"
    report_path = root / "AGENTE GURPS" / "sources" / "processed" / "reports" / "a12_llm_diagnostico_report.json"

    load_dotenv(env_path)
    key = os.getenv("OPENAI_API_KEY", "").strip()
    model = os.getenv("OPENAI_CHAT_MODEL", "gpt-4.1-mini")

    report = {
        "generated_at_utc": datetime.now(timezone.utc).isoformat(),
        "env_file_found": env_path.exists(),
        "openai_api_key_set": bool(key),
        "chat_model": model,
        "llm_call_ok": False,
        "error_type": None,
        "error_message": None,
    }

    if key:
        try:
            from openai import OpenAI

            client = OpenAI(api_key=key)
            resp = client.responses.create(model=model, input="Responda apenas OK.")
            text = (resp.output_text or "").strip()
            report["llm_call_ok"] = bool(text)
        except Exception as exc:
            report["error_type"] = type(exc).__name__
            report["error_message"] = str(exc)[:500]

    report_path.parent.mkdir(parents=True, exist_ok=True)
    with report_path.open("w", encoding="utf-8", newline="\n") as f:
        json.dump(report, f, ensure_ascii=False, indent=2)

    print(f"OK: diagnóstico salvo em {report_path}")
    print(json.dumps({k: report[k] for k in ['openai_api_key_set', 'llm_call_ok', 'error_type']}, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
