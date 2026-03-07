#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import re
import unicodedata
from dataclasses import dataclass
from difflib import SequenceMatcher
from pathlib import Path
from typing import Dict, List, Optional, Tuple

import pandas as pd


def normalize_ascii(text: str) -> str:
    decomposed = unicodedata.normalize("NFKD", str(text))
    no_accents = "".join(ch for ch in decomposed if not unicodedata.combining(ch))
    return re.sub(r"[^a-z0-9]+", "", no_accents.lower())


def normalize_name(text: str) -> str:
    decomposed = unicodedata.normalize("NFKD", str(text))
    no_accents = "".join(ch for ch in decomposed if not unicodedata.combining(ch))
    lowered = no_accents.lower().replace("*", "")
    cleaned = (
        lowered.replace("lmina", "lamina")
        .replace("insignificncia", "insignificancia")
        .replace("neutralizao instantnea", "neutralizacao instantanea")
        .replace("pnico", "panico")
        .replace("regenerao instantnea", "regeneracao instantanea")
        .replace("restaurao instantnea", "restauracao instantanea")
    )
    return re.sub(r"[^a-z0-9]+", " ", cleaned).strip()


def strip_parenthetical(text: str) -> str:
    return re.sub(r"\s*\([^)]*\)", "", text).strip()


@dataclass
class XlsxSpell:
    name: str
    page: Optional[int]
    description: str
    key: Tuple[str, Optional[int]]
    base_key: Tuple[str, Optional[int]]


def find_column(columns: List[str], target_ascii: str) -> str:
    mapping = {normalize_ascii(c): c for c in columns}
    if target_ascii in mapping:
        return mapping[target_ascii]
    raise KeyError(f"Coluna nao encontrada: {target_ascii}. Disponiveis: {columns}")


def load_xlsx_descriptions(path: Path, sheet: str) -> List[XlsxSpell]:
    df = pd.read_excel(path, sheet_name=sheet)
    col_name = find_column(list(df.columns), "nome")
    col_desc = find_column(list(df.columns), "descricao")
    try:
        col_page = find_column(list(df.columns), "pag")
    except KeyError:
        col_page = find_column(list(df.columns), "pagina")

    rows: List[XlsxSpell] = []
    for _, row in df.iterrows():
        raw_name = str(row.get(col_name, "")).strip()
        if not raw_name or raw_name.lower() == "nan":
            continue
        raw_desc = str(row.get(col_desc, "")).strip()
        if not raw_desc or raw_desc.lower() == "nan":
            continue

        page: Optional[int] = None
        raw_page = row.get(col_page)
        if raw_page is not None and str(raw_page).strip().lower() not in ("", "nan"):
            try:
                page = int(float(raw_page))
            except Exception:
                page = None

        normalized = normalize_name(raw_name)
        base = normalize_name(strip_parenthetical(raw_name))
        rows.append(
            XlsxSpell(
                name=raw_name,
                page=page,
                description=raw_desc,
                key=(normalized, page),
                base_key=(base, page),
            )
        )
    return rows


def load_pdf_overrides(path: Optional[Path]) -> Dict[str, str]:
    if path is None or not path.exists():
        return {}
    payload = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(payload, dict):
        raise ValueError("Arquivo de override PDF precisa ser um objeto JSON {id: descricao}.")
    return {str(k): str(v).strip() for k, v in payload.items() if str(v).strip()}


def choose_candidate_by_similarity(target_name: str, candidates: List[XlsxSpell]) -> Optional[XlsxSpell]:
    if not candidates:
        return None
    scored = []
    target = normalize_name(target_name)
    for candidate in candidates:
        ratio = SequenceMatcher(None, target, candidate.key[0]).ratio()
        scored.append((ratio, candidate))
    scored.sort(key=lambda item: item[0], reverse=True)
    best_ratio, best = scored[0]
    if best_ratio < 0.78:
        return None
    if len(scored) == 1:
        return best
    second_ratio = scored[1][0]
    if best_ratio - second_ratio < 0.03:
        return None
    return best


def run_merge(
    magias_json_path: Path,
    xlsx_path: Path,
    xlsx_sheet: str,
    report_path: Path,
    pdf_overrides_path: Optional[Path],
) -> None:
    magias = json.loads(magias_json_path.read_text(encoding="utf-8"))
    xlsx_rows = load_xlsx_descriptions(xlsx_path, xlsx_sheet)
    pdf_overrides = load_pdf_overrides(pdf_overrides_path)

    by_key: Dict[Tuple[str, Optional[int]], List[XlsxSpell]] = {}
    by_base_key: Dict[Tuple[str, Optional[int]], List[XlsxSpell]] = {}
    by_page: Dict[Optional[int], List[XlsxSpell]] = {}
    for row in xlsx_rows:
        by_key.setdefault(row.key, []).append(row)
        by_base_key.setdefault(row.base_key, []).append(row)
        by_page.setdefault(row.page, []).append(row)

    updated = 0
    resolved_by = {
        "pdf_override": 0,
        "exact_name_page": 0,
        "base_name_page": 0,
        "similarity_same_page": 0,
    }
    unresolved: List[dict] = []
    ambiguous: List[dict] = []

    for magia in magias:
        magia_id = str(magia.get("id", "")).strip()
        nome = str(magia.get("nome", "")).strip()
        page = magia.get("pagina")
        page_int = int(page) if isinstance(page, int) else None

        if magia_id in pdf_overrides:
            magia["descricao"] = pdf_overrides[magia_id]
            updated += 1
            resolved_by["pdf_override"] += 1
            continue

        key = (normalize_name(nome), page_int)
        exact = by_key.get(key, [])
        if len(exact) == 1:
            magia["descricao"] = exact[0].description
            updated += 1
            resolved_by["exact_name_page"] += 1
            continue
        if len(exact) > 1:
            desc_set = {item.description for item in exact}
            if len(desc_set) == 1:
                magia["descricao"] = exact[0].description
                updated += 1
                resolved_by["exact_name_page"] += 1
                continue
            ambiguous.append(
                {
                    "id": magia_id,
                    "nome": nome,
                    "pagina": page_int,
                    "reason": "multiple_exact_candidates_with_different_description",
                    "candidates": [item.name for item in exact],
                }
            )
            continue

        base_key = (normalize_name(strip_parenthetical(nome)), page_int)
        base = by_base_key.get(base_key, [])
        if len(base) == 1:
            magia["descricao"] = base[0].description
            updated += 1
            resolved_by["base_name_page"] += 1
            continue
        if len(base) > 1:
            desc_set = {item.description for item in base}
            if len(desc_set) == 1:
                magia["descricao"] = base[0].description
                updated += 1
                resolved_by["base_name_page"] += 1
                continue
            chosen = choose_candidate_by_similarity(nome, base)
            if chosen is not None:
                magia["descricao"] = chosen.description
                updated += 1
                resolved_by["base_name_page"] += 1
                continue
            ambiguous.append(
                {
                    "id": magia_id,
                    "nome": nome,
                    "pagina": page_int,
                    "reason": "multiple_base_candidates_with_different_description",
                    "candidates": [item.name for item in base],
                }
            )
            continue

        same_page = by_page.get(page_int, [])
        chosen = choose_candidate_by_similarity(nome, same_page)
        if chosen is not None:
            magia["descricao"] = chosen.description
            updated += 1
            resolved_by["similarity_same_page"] += 1
            continue

        unresolved.append({"id": magia_id, "nome": nome, "pagina": page_int})

    magias_json_path.write_text(
        json.dumps(magias, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )

    with_description = sum(
        1 for item in magias if str(item.get("descricao", "")).strip()
    )
    report = {
        "magias_total": len(magias),
        "xlsx_rows_with_description": len(xlsx_rows),
        "pdf_overrides_loaded": len(pdf_overrides),
        "updated_this_run": updated,
        "magias_with_descricao": with_description,
        "magias_without_descricao": len(magias) - with_description,
        "resolved_by": resolved_by,
        "ambiguous_count": len(ambiguous),
        "ambiguous": ambiguous,
        "unresolved_count": len(unresolved),
        "unresolved": unresolved,
    }
    report_path.parent.mkdir(parents=True, exist_ok=True)
    report_path.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    print(json.dumps(report, ensure_ascii=False, indent=2))


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Mescla descricoes de MAGIAS.xlsx no magias2versao.json ativo."
    )
    parser.add_argument("--magias-json", required=True, type=Path)
    parser.add_argument("--xlsx", required=True, type=Path)
    parser.add_argument("--sheet", default="Folha2")
    parser.add_argument("--report", required=True, type=Path)
    parser.add_argument("--pdf-overrides", type=Path, default=None)
    return parser.parse_args()


if __name__ == "__main__":
    args = parse_args()
    run_merge(
        magias_json_path=args.magias_json,
        xlsx_path=args.xlsx,
        xlsx_sheet=args.sheet,
        report_path=args.report,
        pdf_overrides_path=args.pdf_overrides,
    )
