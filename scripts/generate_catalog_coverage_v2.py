#!/usr/bin/env python3
import json
from datetime import datetime, timezone
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
REPORTS = ROOT / "scripts" / "reports"
AUDIT_FILE = REPORTS / "active_json_audit_v2.json"
OUT_MD = REPORTS / "catalog_coverage_v2.md"
OUT_JSON = REPORTS / "catalog_coverage_v2.json"

CATEGORIES = {
    "vantagens": ["vantagens.v3.json", "vantagens_artes_marciais.v1.json"],
    "desvantagens": ["desvantagens.v2.json"],
    "pericias": ["pericias.json", "pericias_v2_rules_map.json", "pericias_artes_marciais.v1.json"],
    "tecnicas": ["tecnicas.v1.json"],
    "magias": ["magias2versao.json"],
}


def main():
    if not AUDIT_FILE.exists():
        raise SystemExit(f"Arquivo nao encontrado: {AUDIT_FILE}")

    audit = json.loads(AUDIT_FILE.read_text(encoding="utf-8"))
    by_name = {f["file"]: f for f in audit.get("files", [])}

    out = {
        "generated_at_utc": datetime.now(timezone.utc).isoformat(),
        "categories": {},
    }

    lines = [
        "# Cobertura por catalogo (V2.3)",
        "",
        "Base: `scripts/reports/active_json_audit_v2.json`",
        "",
    ]

    for category, files in CATEGORIES.items():
        existing = [by_name[f] for f in files if f in by_name]
        total_items = sum(i.get("items_count", 0) for i in existing)
        has_issues = any(i.get("issues") for i in existing)
        missing = [f for f in files if f not in by_name]

        out["categories"][category] = {
            "files_expected": files,
            "files_found": [i["file"] for i in existing],
            "files_missing": missing,
            "total_items": total_items,
            "has_issues": has_issues,
        }

        lines.append(f"## {category}")
        lines.append(f"- arquivos esperados: {len(files)}")
        lines.append(f"- arquivos encontrados: {len(existing)}")
        lines.append(f"- itens totais: {total_items}")
        lines.append(f"- possui issues: {has_issues}")
        if missing:
            lines.append(f"- arquivos ausentes: {', '.join(missing)}")
        lines.append("")

    OUT_MD.write_text("\n".join(lines), encoding="utf-8")
    OUT_JSON.write_text(json.dumps(out, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"OK: cobertura salva em {OUT_MD}")
    print(f"OK: cobertura json salva em {OUT_JSON}")


if __name__ == "__main__":
    main()
