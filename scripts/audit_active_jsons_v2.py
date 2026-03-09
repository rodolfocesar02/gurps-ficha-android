#!/usr/bin/env python3
import json
import re
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app" / "src" / "main" / "assets"
REPORTS = ROOT / "scripts" / "reports"
REPORTS.mkdir(parents=True, exist_ok=True)

ACTIVE_FILES = [
    "vantagens.v3.json",
    "vantagens_artes_marciais.v1.json",
    "desvantagens.v2.json",
    "pericias.json",
    "pericias_v2_rules_map.json",
    "pericias_artes_marciais.v1.json",
    "magias2versao.json",
    "tecnicas.v1.json",
    "armas_corpo_a_corpo.v1.normalized.json",
    "armas_distancia.v1.normalized.json",
    "armas_fogo.v1.normalized.json",
    "escudos.v1.json",
    "armaduras.v2.json",
]

REQUIRES_PREREQ = {
    "pericias_v2_rules_map.json",
    "pericias_artes_marciais.v1.json",
    "magias2versao.json",
    "tecnicas.v1.json",
}

SUSPECT_PATTERNS = {
    "char_replacement": re.compile(r"\uFFFD|�"),
    "mojibake_sequences": re.compile(r"Ã¡|Ã©|Ã­|Ã³|Ãº|Ã§|Ã£|Ãµ|Ã¢|Ãª|Ã´|Â|â€|â€“|â€”|â€œ|â€\u009d|â€™"),
    "question_mark_words": re.compile(r"\b(?:n\?o|per\?cia|pr\?-requisito|descri\?ao)\b", re.IGNORECASE),
}

PREREQ_KEY_RE = re.compile(r"pre.?requisito|prereq", re.IGNORECASE)


def iter_strings(value):
    if isinstance(value, str):
        yield value
    elif isinstance(value, list):
        for item in value:
            yield from iter_strings(item)
    elif isinstance(value, dict):
        for v in value.values():
            yield from iter_strings(v)


def flatten_items(doc):
    if isinstance(doc, list):
        return doc
    if isinstance(doc, dict):
        items = doc.get("items")
        if isinstance(items, list):
            return items
    return []


def find_prereq_keys(value, acc):
    if isinstance(value, dict):
        for k, v in value.items():
            if PREREQ_KEY_RE.search(k):
                acc[k] += 1
            find_prereq_keys(v, acc)
    elif isinstance(value, list):
        for item in value:
            find_prereq_keys(item, acc)


def audit_file(path: Path):
    result = {
        "file": path.name,
        "exists": path.exists(),
        "json_valid": False,
        "top_level": None,
        "items_count": 0,
        "missing_id_count": 0,
        "missing_nome_count": 0,
        "duplicate_ids": [],
        "prereq_keys_found": {},
        "suspicious_counts": {},
        "issues": [],
    }

    if not path.exists():
        result["issues"].append("arquivo_nao_encontrado")
        return result

    try:
        raw = path.read_text(encoding="utf-8")
        doc = json.loads(raw)
        result["json_valid"] = True
        result["top_level"] = type(doc).__name__
    except Exception as exc:
        result["issues"].append(f"json_invalido: {exc.__class__.__name__}")
        return result

    items = flatten_items(doc)
    result["items_count"] = len(items)

    ids = []
    for item in items:
        if not isinstance(item, dict):
            continue
        item_id = str(item.get("id", "")).strip()
        item_nome = str(item.get("nome", "")).strip()
        if not item_id:
            result["missing_id_count"] += 1
        else:
            ids.append(item_id.lower())
        if not item_nome:
            result["missing_nome_count"] += 1

    dup = [k for k, v in Counter(ids).items() if v > 1]
    result["duplicate_ids"] = sorted(dup)
    if dup:
        result["issues"].append("ids_duplicados")
    if result["missing_id_count"] > 0:
        result["issues"].append("itens_sem_id")
    if result["missing_nome_count"] > 0:
        result["issues"].append("itens_sem_nome")

    prereq_counter = Counter()
    find_prereq_keys(doc, prereq_counter)
    result["prereq_keys_found"] = dict(prereq_counter)
    if path.name in REQUIRES_PREREQ and not prereq_counter:
        result["issues"].append("sem_chave_prerequisito_detectada")

    text_blob = "\n".join(iter_strings(doc))
    suspicious = {}
    for label, regex in SUSPECT_PATTERNS.items():
        suspicious[label] = len(regex.findall(text_blob))
    result["suspicious_counts"] = suspicious
    if any(v > 0 for v in suspicious.values()):
        result["issues"].append("texto_suspeito_mojibake")

    return result


def main():
    rows = [audit_file(ASSETS / name) for name in ACTIVE_FILES]
    summary = {
        "checked_files": len(rows),
        "json_valid_files": sum(1 for r in rows if r["json_valid"]),
        "files_with_issues": sum(1 for r in rows if r["issues"]),
        "total_items": sum(r["items_count"] for r in rows),
        "total_missing_id": sum(r["missing_id_count"] for r in rows),
        "total_missing_nome": sum(r["missing_nome_count"] for r in rows),
    }
    payload = {
        "generated_at_utc": datetime.now(timezone.utc).isoformat(),
        "summary": summary,
        "files": rows,
    }

    out_json = REPORTS / "active_json_audit_v2.json"
    out_md = REPORTS / "active_json_audit_v2.md"
    out_json.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")

    lines = [
        "# Auditoria V2 - JSONs ativos",
        "",
        f"- Arquivos verificados: {summary['checked_files']}",
        f"- JSON valido: {summary['json_valid_files']}",
        f"- Arquivos com issues: {summary['files_with_issues']}",
        f"- Total de itens: {summary['total_items']}",
        f"- Itens sem id: {summary['total_missing_id']}",
        f"- Itens sem nome: {summary['total_missing_nome']}",
        "",
        "## Detalhe por arquivo",
        "",
    ]
    for row in rows:
        lines.append(f"### {row['file']}")
        lines.append(f"- json_valid: {row['json_valid']}")
        lines.append(f"- items_count: {row['items_count']}")
        lines.append(f"- missing_id_count: {row['missing_id_count']}")
        lines.append(f"- missing_nome_count: {row['missing_nome_count']}")
        lines.append(f"- duplicate_ids: {len(row['duplicate_ids'])}")
        lines.append(f"- prereq_keys_found: {', '.join(row['prereq_keys_found'].keys()) or '(nenhuma)'}")
        lines.append(
            "- suspicious_counts: "
            + ", ".join(f"{k}={v}" for k, v in row["suspicious_counts"].items())
        )
        lines.append(f"- issues: {', '.join(row['issues']) or '(nenhuma)'}")
        lines.append("")

    out_md.write_text("\n".join(lines), encoding="utf-8")
    print(f"OK: relatório salvo em {out_json}")
    print(f"OK: resumo salvo em {out_md}")


if __name__ == "__main__":
    main()
