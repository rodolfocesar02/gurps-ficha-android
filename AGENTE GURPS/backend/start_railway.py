#!/usr/bin/env python3
from __future__ import annotations

import os

import uvicorn


def main() -> int:
    port = int(os.getenv("PORT", "8787"))
    uvicorn.run("api_server:app", host="0.0.0.0", port=port, app_dir="AGENTE GURPS/backend")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
