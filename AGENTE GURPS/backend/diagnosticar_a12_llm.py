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
        "llm_mode": None,
        "error_type": None,
        "error_message": None,
    }

    if key:
        try:
            from openai import OpenAI

            base_url = os.getenv("OPENAI_BASE_URL", "").strip()
            client_kwargs = {"api_key": key}
            if base_url:
                client_kwargs["base_url"] = base_url
            client = OpenAI(**client_kwargs)

            try:
                resp = client.responses.create(model=model, input="Responda apenas OK.")
                text = (resp.output_text or "").strip()
                report["llm_call_ok"] = bool(text)
                report["llm_mode"] = "responses"
            except Exception:
                completion = client.chat.completions.create(
                    model=model,
                    messages=[{"role": "user", "content": "Responda apenas OK."}],
                    temperature=0.2,
                )
                msg = completion.choices[0].message if completion.choices else None
                text = (msg.content or "").strip() if msg else ""
                report["llm_call_ok"] = bool(text)
                report["llm_mode"] = "chat.completions"
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
