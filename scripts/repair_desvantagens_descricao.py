#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import re
from pathlib import Path
from typing import Any, Dict, List, Tuple


def mojibake_score(text: str) -> int:
    markers = ["Ã", "Â", "â", "�", "\ufffd"]
    return sum(text.count(m) for m in markers)


def try_decode(text: str, src: str, dst: str) -> str | None:
    try:
        return text.encode(src, errors="strict").decode(dst, errors="strict")
    except Exception:
        return None


def fix_mojibake(text: str) -> str:
    current = text
    for _ in range(3):
        base = mojibake_score(current)
        best = current
        for src in ("latin-1", "cp1252"):
            candidate = try_decode(current, src, "utf-8")
            if candidate is None:
                continue
            if mojibake_score(candidate) < base:
                best = candidate
                base = mojibake_score(candidate)
        if best == current:
            break
        current = best
    return current.replace("\u00a0", " ").strip()


def build_cross_name_patterns(names: List[str]) -> List[re.Pattern[str]]:
    pats: List[re.Pattern[str]] = []
    for name in names:
        n = re.escape(name)
        pats.append(re.compile(rf"[.!?]\s+{n}\s+[A-ZÁÉÍÓÚÂÊÔÃÕÇ]"))
        pats.append(re.compile(rf"(?:^|[\s.;]){n}\s+(?:M|F|X)?\s*[—\-]\s*(?:Vari[aá]vel|[+\-]?\d)", re.IGNORECASE))
    return pats


def trim_pollution(text: str, own_name: str, all_names: List[str]) -> Tuple[str, bool, str | None]:
    low = text.lower()
    cut_positions: List[Tuple[int, str]] = []

    hard_markers = [
        "modificadores os seguintes modificadores",
        "lista de desvantagens",
        "lista de vantagens",
        "nome tipo valor pág",
        "nome tipo valor pag",
    ]
    for marker in hard_markers:
        i = low.find(marker)
        if i >= 120:
            cut_positions.append((i, f"marker:{marker}"))

    other_names = [n for n in all_names if n != own_name]
    combined = build_cross_name_patterns(other_names)
    for pat in combined:
        m = pat.search(text)
        if not m:
            continue
        idx = m.start()
        if idx >= 100:
            cut_positions.append((idx, "cross_name"))

    if not cut_positions:
        return text, False, None

    cut_at, reason = min(cut_positions, key=lambda x: x[0])
    return text[:cut_at].rstrip(), True, reason


def clean_text(text: str) -> str:
    text = re.sub(r"\s+", " ", text).strip()
    text = re.sub(r"\s+([,.;:!?])", r"\1", text)
    text = re.sub(r"([,.;:!?])(\S)", r"\1 \2", text)
    text = re.sub(r"\s+", " ", text).strip()
    return text


def repair(path: Path, report: Path) -> None:
    data: List[Dict[str, Any]] = json.loads(path.read_text(encoding="utf-8"))
    names = [str(x.get("nome", "")).strip() for x in data if str(x.get("nome", "")).strip()]

    changed = 0
    encoding_fixed = 0
    trimmed = 0
    trimmed_items: List[Dict[str, Any]] = []

    for item in data:
        desc = str(item.get("descricao", "") or "")
        if not desc:
            continue
        original = desc

        fixed = fix_mojibake(desc)
        if fixed != desc:
            encoding_fixed += 1

        trimmed_text, did_trim, reason = trim_pollution(fixed, str(item.get("nome", "")).strip(), names)
        if did_trim:
            trimmed += 1
            trimmed_items.append({
                "id": item.get("id"),
                "nome": item.get("nome"),
                "reason": reason,
            })

        final = clean_text(trimmed_text)
        if final != original:
            changed += 1
            item["descricao"] = final

    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    payload = {
        "total": len(data),
        "changed": changed,
        "encoding_fixed": encoding_fixed,
        "trimmed": trimmed,
        "trimmed_items": trimmed_items,
    }
    report.parent.mkdir(parents=True, exist_ok=True)
    report.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(payload, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Reparo de descrições em desvantagens.v2.json")
    parser.add_argument("--input", required=True, type=Path)
    parser.add_argument("--report", required=True, type=Path)
    args = parser.parse_args()
    repair(args.input, args.report)
