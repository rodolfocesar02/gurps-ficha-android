#!/usr/bin/env python3
from __future__ import annotations

import json
import re
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app" / "src" / "main" / "assets"
REPORT = ROOT / "scripts" / "reports" / "asset_text_cleanup_report.json"

MOJI_MARKERS = ["Ã", "Â", "â", "\ufffd"]

REPLACEMENTS_LITERAL = {
    "per?cia": "perícia",
    "per?cias": "perícias",
    "Per?cia": "Perícia",
    "compat?veis": "compatíveis",
    "pr?tico": "prático",
    "n?o": "não",
    "N?o": "Não",
    "Carat?": "Caratê",
    "pr?-requisito": "pré-requisito",
    "Pr?-requisito": "Pré-requisito",
    "pr?-definido": "pré-definido",
    "Pr?-definido": "Pré-definido",
    "opera??o": "operação",
    "Opera??o": "Operação",
    "Edi??o": "Edição",
    "M?dia": "Média",
    "Dif?cil": "Difícil",
    "Jud?": "Judô",
    "Press?o": "Pressão",
    "?til": "útil",
    "pontos?": "pontos",
    "pts?": "pts",
    "Custo?": "Custo",
}

REPLACEMENTS_REGEX = [
    (re.compile(r"\bpr\?\b", re.IGNORECASE), "pré"),
    (re.compile(r"\bvel\?\b", re.IGNORECASE), "vela"),
]


def marker_score(text: str) -> int:
    return sum(text.count(m) for m in MOJI_MARKERS)


def try_decode(text: str, src: str, dst: str) -> str | None:
    try:
        return text.encode(src, errors="strict").decode(dst, errors="strict")
    except Exception:
        return None


def repair_mojibake(text: str) -> str:
    current = text
    for _ in range(4):
        base = marker_score(current)
        best = current
        for src in ("latin-1", "cp1252"):
            cand = try_decode(current, src, "utf-8")
            if cand is None:
                continue
            c = marker_score(cand)
            if c < base:
                best = cand
                base = c
        if best == current:
            break
        current = best
    return current.replace("\u00a0", " ")


def repair_common(text: str) -> str:
    out = text
    for a, b in REPLACEMENTS_LITERAL.items():
        out = out.replace(a, b)
    for rgx, rep in REPLACEMENTS_REGEX:
        out = rgx.sub(rep, out)
    return out


def walk(node: Any) -> Any:
    if isinstance(node, dict):
        return {k: walk(v) for k, v in node.items()}
    if isinstance(node, list):
        return [walk(v) for v in node]
    if isinstance(node, str):
        s = repair_mojibake(node)
        s = repair_common(s)
        return s
    return node


def count_q_tokens(text: str) -> int:
    return len(re.findall(r"[A-Za-zÀ-ÿ]*\?[A-Za-zÀ-ÿ?]*", text))


def main() -> None:
    changed = []
    scanned = 0
    json_files = sorted(ASSETS.rglob("*.json"))

    for path in json_files:
        rel = path.relative_to(ROOT).as_posix()
        try:
            original_text = path.read_text(encoding="utf-8")
            payload = json.loads(original_text)
        except Exception:
            continue
        scanned += 1

        repaired = walk(payload)
        repaired_text = json.dumps(repaired, ensure_ascii=False, indent=2) + "\n"

        before_markers = marker_score(original_text)
        after_markers = marker_score(repaired_text)
        before_q = count_q_tokens(original_text)
        after_q = count_q_tokens(repaired_text)

        if repaired_text != original_text:
            path.write_text(repaired_text, encoding="utf-8")
            changed.append({
                "file": rel,
                "markers_before": before_markers,
                "markers_after": after_markers,
                "q_tokens_before": before_q,
                "q_tokens_after": after_q,
            })

    report = {
        "scanned_json_files": scanned,
        "changed_files": len(changed),
        "changes": changed,
    }
    REPORT.parent.mkdir(parents=True, exist_ok=True)
    REPORT.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
