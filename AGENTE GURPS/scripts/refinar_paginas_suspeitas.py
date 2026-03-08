#!/usr/bin/env python3
import csv
import json
import re
from pathlib import Path
from typing import Dict, List, Tuple

import fitz  # PyMuPDF
import pytesseract
from PIL import Image


def normalize_spaces(text: str) -> str:
    text = text.replace("\u00a0", " ")
    return re.sub(r"\s+", " ", text).strip()


def split_chunks(text: str, max_chars: int = 1200, overlap: int = 120) -> List[str]:
    text = normalize_spaces(text)
    if not text:
        return []
    chunks: List[str] = []
    start = 0
    while start < len(text):
        end = min(len(text), start + max_chars)
        if end < len(text):
            pivot = text.rfind(". ", start, end)
            if pivot > start + 200:
                end = pivot + 1
        chunk = text[start:end].strip()
        if chunk:
            chunks.append(chunk)
        if end >= len(text):
            break
        start = max(0, end - overlap)
    return chunks


def load_manifest_path_map(manifest_path: Path) -> Dict[str, Path]:
    mapping: Dict[str, Path] = {}
    with manifest_path.open("r", encoding="utf-8", newline="") as f:
        reader = csv.DictReader(f)
        for row in reader:
            if row.get("permitido_uso", "").strip().lower() != "sim":
                continue
            mapping[row["id"]] = Path(row["arquivo_relativo"])
    return mapping


def ocr_try_modes(doc: fitz.Document, page_index: int) -> Tuple[str, int]:
    page = doc.load_page(page_index)
    pix = page.get_pixmap(matrix=fitz.Matrix(2.5, 2.5), alpha=False)
    img = Image.frombytes("RGB", [pix.width, pix.height], pix.samples)
    best_text = ""
    best_len = 0
    # PSM variados para melhorar páginas de capa/índice/coluna.
    for psm in [6, 4, 11, 3, 12]:
        cfg = f"--oem 3 --psm {psm}"
        txt = normalize_spaces(pytesseract.image_to_string(img, lang="por", config=cfg))
        if len(txt) > best_len:
            best_len = len(txt)
            best_text = txt
    return best_text, best_len


def main() -> int:
    root = Path(__file__).resolve().parents[2]
    base = root / "AGENTE GURPS"
    manifest = base / "sources" / "manifesto_fontes.csv"
    pages_path = base / "sources" / "processed" / "pages.jsonl"
    chunks_path = base / "sources" / "processed" / "chunks.jsonl"
    detailed_path = base / "sources" / "processed" / "reports" / "paginas_suspeitas_detalhado.json"
    out_report = base / "sources" / "processed" / "reports" / "correcao_suspeitas_report.json"

    # Tesseract path fallback
    tesseract_default = Path("C:/Program Files/Tesseract-OCR/tesseract.exe")
    if tesseract_default.exists():
        pytesseract.pytesseract.tesseract_cmd = str(tesseract_default)

    path_map = load_manifest_path_map(manifest)
    detailed = json.loads(detailed_path.read_text(encoding="utf-8"))
    target_pages = {(x["source_id"], int(x["page_number"])) for x in detailed["itens"]}

    pages = []
    with pages_path.open("r", encoding="utf-8") as f:
        for line in f:
            pages.append(json.loads(line))

    improvements = []
    docs_cache: Dict[str, fitz.Document] = {}
    for p in pages:
        sid = p["source_id"]
        pg = int(p["page_number"])
        if (sid, pg) not in target_pages:
            continue
        rel = path_map.get(sid)
        if rel is None:
            continue
        full_pdf = base / "sources" / rel
        if sid not in docs_cache:
            docs_cache[sid] = fitz.open(str(full_pdf))
        old_text = p.get("text", "") or ""
        old_len = len(normalize_spaces(old_text))
        new_text, new_len = ocr_try_modes(docs_cache[sid], pg - 1)
        improved = new_len > old_len + 20
        if improved:
            p["text"] = new_text
            p["used_ocr"] = True
        improvements.append(
            {
                "source_id": sid,
                "page_number": pg,
                "old_len": old_len,
                "new_len": new_len,
                "improved": improved,
            }
        )

    for doc in docs_cache.values():
        doc.close()

    # Rebuild chunks from updated pages
    chunks = []
    for p in pages:
        text = p.get("text", "") or ""
        page_id = p["page_id"]
        for i, c in enumerate(split_chunks(text), start=1):
            chunks.append(
                {
                    "chunk_id": f"{page_id}_c{i}",
                    "source_id": p["source_id"],
                    "source_title": p["source_title"],
                    "page_number": p["page_number"],
                    "text": c,
                    "language": "pt",
                }
            )

    with pages_path.open("w", encoding="utf-8", newline="\n") as f:
        for p in pages:
            f.write(json.dumps(p, ensure_ascii=False) + "\n")

    with chunks_path.open("w", encoding="utf-8", newline="\n") as f:
        for c in chunks:
            f.write(json.dumps(c, ensure_ascii=False) + "\n")

    before_suspects = sum(1 for x in improvements if x["old_len"] < 180)
    after_suspects = sum(1 for x in improvements if x["new_len"] < 180)
    report = {
        "target_pages": len(improvements),
        "improved_pages": sum(1 for x in improvements if x["improved"]),
        "before_suspects": before_suspects,
        "after_suspects": after_suspects,
        "delta_suspects": before_suspects - after_suspects,
        "delta_chunks_total": len(chunks),
        "pages": sorted(improvements, key=lambda x: (x["source_id"], x["page_number"])),
    }
    out_report.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({
        "target_pages": report["target_pages"],
        "improved_pages": report["improved_pages"],
        "before_suspects": report["before_suspects"],
        "after_suspects": report["after_suspects"],
        "delta_suspects": report["delta_suspects"],
    }, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
