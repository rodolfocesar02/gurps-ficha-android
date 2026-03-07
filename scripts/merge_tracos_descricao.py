#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import re
import unicodedata
from pathlib import Path
from typing import Dict, List, Optional, Tuple

import pandas as pd


def normalize_text(value: str) -> str:
    txt = unicodedata.normalize("NFKD", str(value))
    txt = "".join(ch for ch in txt if not unicodedata.combining(ch))
    txt = txt.lower()
    txt = re.sub(r"\s+", " ", txt).strip()
    return txt


def normalize_key(value: str) -> str:
    txt = normalize_text(value)
    txt = re.sub(r"[^a-z0-9]+", " ", txt).strip()
    return txt


def find_column(columns: List[str], ascii_target: str) -> str:
    def asciify(s: str) -> str:
        raw = unicodedata.normalize("NFKD", s)
        raw = "".join(ch for ch in raw if not unicodedata.combining(ch))
        return re.sub(r"[^a-z0-9]+", "", raw.lower())

    mapped = {asciify(col): col for col in columns}
    if ascii_target in mapped:
        return mapped[ascii_target]
    raise KeyError(f"Coluna '{ascii_target}' nao encontrada. Colunas: {columns}")


def load_vantagens_xlsx(path: Path) -> Tuple[Dict[Tuple[str, Optional[int]], str], Dict[str, str]]:
    df = pd.read_excel(path, sheet_name=0)
    col_page = find_column(list(df.columns), "pagina")
    col_name = find_column(list(df.columns), "vantagem")
    col_desc = find_column(list(df.columns), "descricaooriginal")

    by_name_page: Dict[Tuple[str, Optional[int]], str] = {}
    by_name: Dict[str, str] = {}

    for _, row in df.iterrows():
        name = str(row.get(col_name, "")).strip()
        if not name or name.lower() == "nan":
            continue
        desc = str(row.get(col_desc, "")).strip()
        if not desc or desc.lower() == "nan":
            continue

        page_raw = row.get(col_page)
        page: Optional[int] = None
        if page_raw is not None and str(page_raw).strip().lower() not in ("", "nan"):
            try:
                page = int(float(page_raw))
            except Exception:
                page = None

        key = normalize_key(name)
        by_name_page[(key, page)] = desc
        by_name.setdefault(key, desc)

    return by_name_page, by_name


def should_skip_line(line: str) -> bool:
    low = normalize_text(line)
    if not low:
        return True
    if low.startswith("--- page"):
        return True
    if low in {"lista de vantagens", "lista de desvantagens", "vantagens", "desvantagens"}:
        return True
    return False


def is_cost_line(line: str) -> bool:
    low = normalize_text(line)
    return bool(re.fullmatch(r"-?\d+(\s*(ou|a)\s*-?\d+)?\s*pontos?", low))


def clean_joined_text(lines: List[str]) -> str:
    if not lines:
        return ""
    out: List[str] = []
    for line in lines:
        txt = " ".join(line.split())
        if not txt:
            continue
        if out and out[-1].endswith("-") and txt and txt[0].islower():
            out[-1] = out[-1][:-1] + txt
        else:
            out.append(txt)
    joined = " ".join(out)
    joined = re.sub(r"\s+", " ", joined).strip()
    return joined


def extract_descriptions_from_text_dump(
    text_dump_path: Path,
    names: List[str],
    max_lines_window: int = 44,
) -> Dict[str, str]:
    lines = text_dump_path.read_text(encoding="utf-8", errors="ignore").splitlines()
    normalized_names = {normalize_key(name): name for name in names if name.strip()}
    name_keys = sorted(normalized_names.keys(), key=len, reverse=True)
    name_set = set(name_keys)
    extracted: Dict[str, str] = {}
    candidates: Dict[str, List[Tuple[int, int]]] = {}

    for idx, raw in enumerate(lines):
        line_key = normalize_key(raw)
        if not line_key or len(line_key) > 90:
            continue

        matched_key: Optional[str] = None
        for name_key in name_keys:
            if line_key == name_key:
                matched_key = name_key
                break
            if line_key.startswith(name_key + " "):
                suffix = line_key[len(name_key):].strip()
                if suffix and re.fullmatch(r"[0-9ivxlcdm ]{1,10}", suffix):
                    matched_key = name_key
                    break
            if line_key.endswith(" " + name_key):
                prefix = line_key[: -len(name_key)].strip()
                if prefix and re.fullmatch(r"[0-9ivxlcdm ]{1,10}", prefix):
                    matched_key = name_key
                    break
        if matched_key is None:
            continue

        nearby = [normalize_text(lines[j]) for j in range(idx + 1, min(len(lines), idx + 6))]
        has_cost_hint = any(("ponto" in n) or re.search(r"-?\d+\s*(ou|a)?\s*-?\d*\s*pontos?", n) for n in nearby)
        score = 2 if has_cost_hint else 0
        candidates.setdefault(matched_key, []).append((idx, score))

    for key_line, rows in candidates.items():
        rows_sorted = sorted(rows, key=lambda x: x[1], reverse=True)
        idx = rows_sorted[0][0]

        collected: List[str] = []
        for j in range(idx + 1, min(len(lines), idx + 1 + max_lines_window)):
            line = lines[j].strip()
            line_key = normalize_key(line)
            if line.lower().startswith("--- page"):
                break
            if line_key in name_set and line_key != key_line:
                break
            if should_skip_line(line):
                if collected:
                    break
                continue
            if is_cost_line(line):
                continue
            if normalize_text(line).startswith("v. ") and "pag." in normalize_text(line):
                continue
            collected.append(line)

        text = clean_joined_text(collected)
        if len(text) >= 20:
            extracted[key_line] = text

    return extracted


def merge_descriptions(
    vantagens_json_path: Path,
    desvantagens_json_path: Path,
    vantagens_xlsx_path: Path,
    modulo_text_path: Path,
    report_path: Path,
) -> None:
    vantagens = json.loads(vantagens_json_path.read_text(encoding="utf-8"))
    desvantagens = json.loads(desvantagens_json_path.read_text(encoding="utf-8"))

    vant_xlsx_by_name_page, vant_xlsx_by_name = load_vantagens_xlsx(vantagens_xlsx_path)

    all_names = [item.get("nome", "") for item in vantagens] + [item.get("nome", "") for item in desvantagens]
    pdf_by_name = extract_descriptions_from_text_dump(modulo_text_path, all_names)

    stats = {
        "vantagens_total": len(vantagens),
        "desvantagens_total": len(desvantagens),
        "vantagens_with_descricao": 0,
        "desvantagens_with_descricao": 0,
        "vantagens_source_pdf": 0,
        "vantagens_source_xlsx": 0,
        "desvantagens_source_pdf": 0,
        "unmatched_vantagens": [],
        "unmatched_desvantagens": [],
    }

    for item in vantagens:
        name = str(item.get("nome", "")).strip()
        page = item.get("pagina")
        key = normalize_key(name)
        desc_pdf = pdf_by_name.get(key, "").strip()
        desc_xlsx = vant_xlsx_by_name_page.get((key, page), "").strip()
        if not desc_xlsx:
            desc_xlsx = vant_xlsx_by_name.get(key, "").strip()

        chosen = desc_pdf or desc_xlsx
        if chosen:
            item["descricao"] = chosen
            stats["vantagens_with_descricao"] += 1
            if desc_pdf:
                stats["vantagens_source_pdf"] += 1
            else:
                stats["vantagens_source_xlsx"] += 1
        else:
            stats["unmatched_vantagens"].append({"id": item.get("id"), "nome": name, "pagina": page})

    for item in desvantagens:
        name = str(item.get("nome", "")).strip()
        key = normalize_key(name)
        desc_pdf = pdf_by_name.get(key, "").strip()
        if desc_pdf:
            item["descricao"] = desc_pdf
            stats["desvantagens_with_descricao"] += 1
            stats["desvantagens_source_pdf"] += 1
        else:
            stats["unmatched_desvantagens"].append(
                {"id": item.get("id"), "nome": name, "pagina": item.get("pagina")}
            )

    vantagens_json_path.write_text(json.dumps(vantagens, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    desvantagens_json_path.write_text(json.dumps(desvantagens, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    report_path.parent.mkdir(parents=True, exist_ok=True)
    report_path.write_text(json.dumps(stats, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(stats, ensure_ascii=False, indent=2))


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Mescla descricoes de vantagens/desvantagens nos JSONs ativos.")
    parser.add_argument("--vantagens-json", required=True, type=Path)
    parser.add_argument("--desvantagens-json", required=True, type=Path)
    parser.add_argument("--vantagens-xlsx", required=True, type=Path)
    parser.add_argument("--modulo-text", required=True, type=Path)
    parser.add_argument("--report", required=True, type=Path)
    return parser.parse_args()


if __name__ == "__main__":
    args = parse_args()
    merge_descriptions(
        vantagens_json_path=args.vantagens_json,
        desvantagens_json_path=args.desvantagens_json,
        vantagens_xlsx_path=args.vantagens_xlsx,
        modulo_text_path=args.modulo_text,
        report_path=args.report,
    )
