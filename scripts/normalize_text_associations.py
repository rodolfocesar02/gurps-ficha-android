#!/usr/bin/env python3
import argparse
import json
import re
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple


MOJIBAKE_MARKERS = ("Ã", "Â", "â", "�")
SKIP_TEXT_NORMALIZATION_KEYS = {
    "id",
    "kind",
    "schema",
    "schemaVersion",
    "sourceFile",
    "sourceFiles",
    "generatedAtUtc",
}


@dataclass
class ReplacementRule:
    term_id: str
    canonical: str
    variant: str
    pattern: re.Pattern[str]


def load_aliases(path: Path) -> List[dict]:
    payload = json.loads(path.read_text(encoding="utf-8"))
    terms = payload.get("canonical_terms")
    if not isinstance(terms, list):
        raise SystemExit(f"Arquivo de aliases invalido: {path}")
    return terms


def build_rules(aliases: List[dict]) -> List[ReplacementRule]:
    rules: List[ReplacementRule] = []
    for term in aliases:
        term_id = term["id"]
        canonical = term["canonical"]
        variants = term.get("variants", [])
        for variant in variants:
            if variant.strip().lower() == canonical.strip().lower():
                continue
            escaped = re.escape(variant)
            pattern = re.compile(rf"(?<!\w){escaped}(?!\w)", re.IGNORECASE)
            rules.append(
                ReplacementRule(
                    term_id=term_id,
                    canonical=canonical,
                    variant=variant,
                    pattern=pattern,
                )
            )
    return rules


def fix_mojibake_once(text: str) -> str:
    if not any(marker in text for marker in MOJIBAKE_MARKERS):
        return text
    try:
        repaired = text.encode("latin-1", errors="ignore").decode("utf-8", errors="ignore")
    except Exception:
        return text
    return repaired if repaired else text


def apply_rules_to_string(
    value: str,
    rules: List[ReplacementRule],
    fix_mojibake: bool,
) -> Tuple[str, int, bool]:
    original = value
    current = value
    mojibake_changed = False
    if fix_mojibake:
        repaired = fix_mojibake_once(current)
        if repaired != current:
            mojibake_changed = True
            current = repaired

    replacements = 0
    for rule in rules:
        current, count = rule.pattern.subn(rule.canonical, current)
        replacements += count

    changed = current != original
    return current, replacements, mojibake_changed and changed


def walk_and_normalize(
    node: Any,
    rules: List[ReplacementRule],
    fix_mojibake: bool,
    parent_key: Optional[str] = None,
) -> Tuple[Any, int, int]:
    # returns (normalized_node, alias_replacements, mojibake_fixes)
    if isinstance(node, dict):
        total_alias = 0
        total_mojibake = 0
        out = {}
        for key, value in node.items():
            norm_value, alias_count, mojibake_count = walk_and_normalize(
                value, rules, fix_mojibake, key
            )
            out[key] = norm_value
            total_alias += alias_count
            total_mojibake += mojibake_count
        return out, total_alias, total_mojibake
    if isinstance(node, list):
        total_alias = 0
        total_mojibake = 0
        out = []
        for item in node:
            norm_item, alias_count, mojibake_count = walk_and_normalize(
                item, rules, fix_mojibake, parent_key
            )
            out.append(norm_item)
            total_alias += alias_count
            total_mojibake += mojibake_count
        return out, total_alias, total_mojibake
    if isinstance(node, str):
        if parent_key in SKIP_TEXT_NORMALIZATION_KEYS:
            return node, 0, 0
        normalized, alias_count, mojibake_changed = apply_rules_to_string(node, rules, fix_mojibake)
        return normalized, alias_count, 1 if mojibake_changed else 0
    return node, 0, 0


def collect_json_files(root: Path) -> List[Path]:
    return sorted(root.rglob("*.json"))


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Normaliza termos de texto em JSON por aliases canonicos."
    )
    parser.add_argument(
        "--input-dir",
        default="app/src/main/assets",
        help="Diretorio de entrada com JSONs.",
    )
    parser.add_argument(
        "--output-dir",
        default="scripts/normalized_assets",
        help="Diretorio de saida com JSONs normalizados.",
    )
    parser.add_argument(
        "--aliases",
        default="scripts/text_aliases_ptbr.json",
        help="Arquivo de aliases canonicos.",
    )
    parser.add_argument(
        "--report-out",
        default="scripts/reports/text_normalization_report.json",
        help="Relatorio de normalizacao.",
    )
    parser.add_argument(
        "--fix-mojibake",
        action="store_true",
        help="Tenta reparar strings com marcadores comuns de mojibake.",
    )
    parser.add_argument(
        "--in-place",
        action="store_true",
        help="Aplica normalizacao no proprio diretorio de entrada (ignora --output-dir).",
    )
    args = parser.parse_args()

    input_dir = Path(args.input_dir)
    output_dir = Path(args.output_dir)
    aliases_file = Path(args.aliases)
    report_file = Path(args.report_out)

    if not input_dir.exists():
        raise SystemExit(f"Diretorio de entrada nao encontrado: {input_dir}")
    if not aliases_file.exists():
        raise SystemExit(f"Arquivo de aliases nao encontrado: {aliases_file}")

    aliases = load_aliases(aliases_file)
    rules = build_rules(aliases)
    files = collect_json_files(input_dir)

    report_entries: List[Dict[str, Any]] = []
    total_changed_files = 0
    total_alias_replacements = 0
    total_mojibake_fixes = 0

    for src in files:
        rel = src.relative_to(input_dir)
        dst = src if args.in_place else output_dir / rel
        payload = json.loads(src.read_text(encoding="utf-8"))
        normalized, alias_count, mojibake_count = walk_and_normalize(
            payload, rules, args.fix_mojibake
        )
        original_text = json.dumps(payload, ensure_ascii=False, indent=2) + "\n"
        normalized_text = json.dumps(normalized, ensure_ascii=False, indent=2) + "\n"
        changed = original_text != normalized_text
        if changed:
            total_changed_files += 1
            total_alias_replacements += alias_count
            total_mojibake_fixes += mojibake_count
            dst.parent.mkdir(parents=True, exist_ok=True)
            dst.write_text(normalized_text, encoding="utf-8")
        elif not args.in_place:
            dst.parent.mkdir(parents=True, exist_ok=True)
            dst.write_text(original_text, encoding="utf-8")

        report_entries.append(
            {
                "file": str(rel).replace("\\", "/"),
                "changed": changed,
                "aliasReplacements": alias_count,
                "mojibakeFixes": mojibake_count,
            }
        )

    report = {
        "summary": {
            "inputDir": str(input_dir).replace("\\", "/"),
            "outputDir": str(input_dir if args.in_place else output_dir).replace("\\", "/"),
            "filesScanned": len(files),
            "filesChanged": total_changed_files,
            "aliasReplacements": total_alias_replacements,
            "mojibakeFixes": total_mojibake_fixes,
            "inPlace": args.in_place,
            "fixMojibake": args.fix_mojibake,
        },
        "files": report_entries,
    }

    report_file.parent.mkdir(parents=True, exist_ok=True)
    report_file.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    print("=== Normalizacao de Associacao Textual ===")
    print(f"Arquivos analisados: {len(files)}")
    print(f"Arquivos alterados: {total_changed_files}")
    print(f"Substituicoes por alias: {total_alias_replacements}")
    print(f"Reparos de mojibake: {total_mojibake_fixes}")
    print(f"Relatorio: {report_file}")
    if args.in_place:
        print(f"Saida aplicada em: {input_dir}")
    else:
        print(f"Saida gerada em: {output_dir}")


if __name__ == "__main__":
    main()
