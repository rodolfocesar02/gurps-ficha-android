#!/usr/bin/env python3
import argparse
import json
import re
import unicodedata
from collections import defaultdict
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, Iterable, List, Set, Tuple


TOKEN_RE = re.compile(r"[\wÀ-ÿ][\wÀ-ÿ'/-]*", re.UNICODE)
MOJIBAKE_RE = re.compile(r"[ÃÂ�]")


def normalize_key(value: str) -> str:
    decomposed = unicodedata.normalize("NFKD", value)
    no_marks = "".join(ch for ch in decomposed if unicodedata.category(ch) != "Mn")
    lowered = no_marks.lower().replace("-", " ")
    lowered = re.sub(r"[^a-z0-9]+", "", lowered)
    return lowered


@dataclass
class FileFinding:
    path: str
    snippet: str


def iter_strings(node):
    if isinstance(node, dict):
        for value in node.values():
            yield from iter_strings(value)
    elif isinstance(node, list):
        for value in node:
            yield from iter_strings(value)
    elif isinstance(node, str):
        yield node


def load_aliases(path: Path) -> List[dict]:
    payload = json.loads(path.read_text(encoding="utf-8"))
    terms = payload.get("canonical_terms")
    if not isinstance(terms, list):
        raise SystemExit(f"Arquivo de aliases invalido: {path}")
    return terms


def collect_json_files(root_assets: Path, include_snapshots: bool, snapshots_root: Path) -> List[Path]:
    files = sorted(root_assets.glob("*.json"))
    if include_snapshots and snapshots_root.exists():
        files += sorted(snapshots_root.rglob("*.json"))
    return files


def audit(
    files: Iterable[Path],
    aliases: List[dict],
) -> dict:
    token_forms_by_norm: Dict[str, Set[str]] = defaultdict(set)
    token_files_by_norm: Dict[str, Set[str]] = defaultdict(set)
    mojibake_findings: List[FileFinding] = []

    alias_norm_to_term: Dict[str, str] = {}
    alias_variants_by_term: Dict[str, Set[str]] = {}
    canonical_by_term: Dict[str, str] = {}
    for term in aliases:
        term_id = term["id"]
        canonical = term["canonical"]
        canonical_by_term[term_id] = canonical
        variants = set(term.get("variants", []))
        variants.add(canonical)
        normalized_set = {normalize_key(v) for v in variants}
        alias_variants_by_term[term_id] = normalized_set
        for norm in normalized_set:
            alias_norm_to_term[norm] = term_id

    alias_hits: Dict[str, Dict[str, Set[str]]] = defaultdict(lambda: defaultdict(set))

    for file_path in files:
        try:
            payload = json.loads(file_path.read_text(encoding="utf-8"))
        except Exception:
            continue
        rel_path = str(file_path).replace("\\", "/")
        for s in iter_strings(payload):
            if MOJIBAKE_RE.search(s):
                mojibake_findings.append(FileFinding(path=rel_path, snippet=s[:180]))
            for token in TOKEN_RE.findall(s):
                norm = normalize_key(token)
                if len(norm) < 3:
                    continue
                token_forms_by_norm[norm].add(token)
                token_files_by_norm[norm].add(rel_path)
                term_id = alias_norm_to_term.get(norm)
                if term_id:
                    alias_hits[term_id][token].add(rel_path)

    variant_groups = []
    for norm, forms in token_forms_by_norm.items():
        if len(forms) <= 1:
            continue
        variant_groups.append(
            {
                "norm": norm,
                "forms": sorted(forms),
                "files": sorted(token_files_by_norm[norm]),
                "occurrenceFilesCount": len(token_files_by_norm[norm]),
            }
        )
    variant_groups.sort(key=lambda g: (-len(g["forms"]), g["norm"]))

    canonical_report = []
    for term in aliases:
        term_id = term["id"]
        hits = alias_hits.get(term_id, {})
        observed = sorted(hits.keys(), key=lambda x: x.lower())
        canonical_report.append(
            {
                "id": term_id,
                "canonical": canonical_by_term[term_id],
                "observedForms": observed,
                "observedFiles": sorted({fp for forms in hits.values() for fp in forms}),
                "needsNormalization": len(observed) > 1,
            }
        )

    critical = [
        item for item in canonical_report
        if item["id"] in {"carate", "judo", "luta_greco_romana", "sacar_rapido", "pre_requisito", "pre_definido"}
        and item["needsNormalization"]
    ]

    return {
        "summary": {
            "filesScanned": len(list(files)),
            "mojibakeFindings": len(mojibake_findings),
            "variantGroups": len(variant_groups),
            "criticalCanonicalConflicts": len(critical),
        },
        "criticalCanonicalConflicts": critical,
        "canonicalTerms": canonical_report,
        "mojibakeFindings": [finding.__dict__ for finding in mojibake_findings],
        "topVariantGroups": variant_groups[:200],
    }


def main():
    parser = argparse.ArgumentParser(
        description="Audita associacao textual/aliases em JSONs (acentos, hifenizacao, mojibake)."
    )
    parser.add_argument(
        "--assets-dir",
        default="app/src/main/assets",
        help="Diretorio principal dos JSONs usados pelo app.",
    )
    parser.add_argument(
        "--include-snapshots",
        action="store_true",
        help="Inclui JSONs em snapshots/ na auditoria.",
    )
    parser.add_argument(
        "--snapshots-dir",
        default="snapshots",
        help="Diretorio raiz de snapshots.",
    )
    parser.add_argument(
        "--aliases",
        default="scripts/text_aliases_ptbr.json",
        help="Arquivo de aliases canonicos.",
    )
    parser.add_argument(
        "--report-out",
        default="scripts/reports/text_association_report.json",
        help="Arquivo de saida do relatorio JSON.",
    )
    parser.add_argument(
        "--strict",
        action="store_true",
        help="Retorna erro se houver conflitos canonicos criticos ou mojibake.",
    )
    args = parser.parse_args()

    assets_dir = Path(args.assets_dir)
    snapshots_dir = Path(args.snapshots_dir)
    aliases_path = Path(args.aliases)
    report_out = Path(args.report_out)

    if not assets_dir.exists():
        raise SystemExit(f"Diretorio de assets nao encontrado: {assets_dir}")
    if not aliases_path.exists():
        raise SystemExit(f"Arquivo de aliases nao encontrado: {aliases_path}")

    files = collect_json_files(assets_dir, args.include_snapshots, snapshots_dir)
    aliases = load_aliases(aliases_path)
    result = audit(files, aliases)

    report_out.parent.mkdir(parents=True, exist_ok=True)
    report_out.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    summary = result["summary"]
    print("=== Auditoria de Associacao Textual ===")
    print(f"Arquivos analisados: {summary['filesScanned']}")
    print(f"Mojibake encontrado: {summary['mojibakeFindings']}")
    print(f"Grupos de variantes: {summary['variantGroups']}")
    print(f"Conflitos canonicos criticos: {summary['criticalCanonicalConflicts']}")
    print(f"Relatorio: {report_out}")

    if result["criticalCanonicalConflicts"]:
        print("\nConflitos criticos:")
        for item in result["criticalCanonicalConflicts"]:
            formas = " | ".join(item["observedForms"])
            print(f"- {item['id']} -> {formas}")

    if args.strict and (
        summary["mojibakeFindings"] > 0 or summary["criticalCanonicalConflicts"] > 0
    ):
        raise SystemExit(1)


if __name__ == "__main__":
    main()
