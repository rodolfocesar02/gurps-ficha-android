#!/usr/bin/env python3
import csv
import sys
from pathlib import Path

REQUIRED_COLUMNS = [
    "id",
    "titulo",
    "idioma",
    "tipo",
    "edicao",
    "arquivo_relativo",
    "permitido_uso",
    "prioridade",
    "observacoes",
]


def main() -> int:
    repo_root = Path(__file__).resolve().parents[2]
    manifest = repo_root / "AGENTE GURPS" / "sources" / "manifesto_fontes.csv"
    if not manifest.exists():
        print(f"ERRO: manifesto nao encontrado: {manifest}")
        return 1

    with manifest.open("r", encoding="utf-8", newline="") as f:
        reader = csv.DictReader(f)
        if reader.fieldnames != REQUIRED_COLUMNS:
            print("ERRO: colunas invalidas no manifesto.")
            print("Esperado:", ",".join(REQUIRED_COLUMNS))
            print("Atual   :", ",".join(reader.fieldnames or []))
            return 1

        errors = []
        seen_ids = set()
        for idx, row in enumerate(reader, start=2):
            row_id = (row.get("id") or "").strip()
            if not row_id:
                errors.append(f"linha {idx}: id vazio")
            elif row_id in seen_ids:
                errors.append(f"linha {idx}: id duplicado '{row_id}'")
            else:
                seen_ids.add(row_id)

            idioma = (row.get("idioma") or "").strip().lower()
            if idioma not in {"pt", "en"}:
                errors.append(f"linha {idx}: idioma invalido '{idioma}' (use pt/en)")

            permitido = (row.get("permitido_uso") or "").strip().lower()
            if permitido not in {"sim", "nao"}:
                errors.append(f"linha {idx}: permitido_uso invalido '{permitido}' (use sim/nao)")

            rel = (row.get("arquivo_relativo") or "").strip()
            if not rel:
                errors.append(f"linha {idx}: arquivo_relativo vazio")

        if errors:
            print("ERRO: manifesto invalido:")
            for e in errors:
                print("-", e)
            return 1

    print("OK: manifesto validado com sucesso.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
