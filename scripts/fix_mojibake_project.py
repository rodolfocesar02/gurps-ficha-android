#!/usr/bin/env python3
from __future__ import annotations

import json
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
REPORT = ROOT / "scripts" / "reports" / "mojibake_project_fix_report.json"

TEXT_EXTS = {
    ".json", ".md", ".txt", ".kt", ".kts", ".gradle", ".pro", ".xml", ".csv", ".yml", ".yaml", ".properties", ".ps1", ".py"
}

MARKERS = ["Ã", "Â", "â", "\ufffd"]


def score(text: str) -> int:
    return sum(text.count(m) for m in MARKERS)


def try_decode(text: str, src: str, dst: str) -> str | None:
    try:
        return text.encode(src, errors="strict").decode(dst, errors="strict")
    except Exception:
        return None


def repair_text(text: str) -> tuple[str, int, int]:
    original_score = score(text)
    current = text
    for _ in range(4):
        base = score(current)
        best = current
        for src in ("latin-1", "cp1252"):
            cand = try_decode(current, src, "utf-8")
            if cand is None:
                continue
            cscore = score(cand)
            if cscore < base:
                best = cand
                base = cscore
        if best == current:
            break
        current = best
    current = current.replace("\u00a0", " ")
    return current, original_score, score(current)


def is_text_path(path: Path) -> bool:
    if path.suffix.lower() in TEXT_EXTS:
        return True
    # extensionless known text files
    return path.name in {"README", "LICENSE", "gradlew", "gradlew.bat"}


def main() -> None:
    cmd = ["git", "ls-files"]
    proc = subprocess.run(cmd, cwd=ROOT, capture_output=True, text=True, check=True)
    files = [ROOT / line.strip() for line in proc.stdout.splitlines() if line.strip()]

    changed = []
    scanned = 0

    for path in files:
        rel = path.relative_to(ROOT).as_posix()
        if rel.startswith("build/") or rel.startswith(".gradle/") or rel.startswith("release-apks/"):
            continue
        if not path.exists() or not path.is_file() or not is_text_path(path):
            continue
        scanned += 1
        try:
            original = path.read_text(encoding="utf-8")
        except Exception:
            continue
        fixed, before, after = repair_text(original)
        if fixed != original and after <= before:
            path.write_text(fixed, encoding="utf-8", newline="\n")
            changed.append({
                "file": rel,
                "markers_before": before,
                "markers_after": after,
            })

    report = {
        "scanned_files": scanned,
        "changed_files": len(changed),
        "changes": changed,
    }
    REPORT.parent.mkdir(parents=True, exist_ok=True)
    REPORT.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
