#!/usr/bin/env python3
import argparse
import csv
import json
import re
import shutil
import unicodedata
from dataclasses import dataclass
from pathlib import Path
from typing import List, Tuple

import fitz  # PyMuPDF
import pdfplumber

try:
    import pytesseract  # type: ignore
    from PIL import Image  # type: ignore
    tesseract_cmd = shutil.which("tesseract")
    if not tesseract_cmd:
        default_tesseract = Path("C:/Program Files/Tesseract-OCR/tesseract.exe")
        if default_tesseract.exists():
            tesseract_cmd = str(default_tesseract)
    if tesseract_cmd:
        pytesseract.pytesseract.tesseract_cmd = tesseract_cmd
        OCR_AVAILABLE = True
    else:
        OCR_AVAILABLE = False
except Exception:
    OCR_AVAILABLE = False


def normalize_spaces(text: str) -> str:
    text = text.replace("\u00a0", " ")
    text = re.sub(r"\s+", " ", text).strip()
    return text


def slugify(value: str) -> str:
    norm = unicodedata.normalize("NFKD", value).encode("ascii", "ignore").decode("ascii")
    return re.sub(r"[^a-zA-Z0-9]+", "_", norm).strip("_").lower()


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


def detect_layout(words: List[dict], page_width: float) -> str:
    if not words:
        return "vazio"
    left = sum(1 for w in words if float(w["x0"]) < page_width * 0.45)
    right = sum(1 for w in words if float(w["x0"]) >= page_width * 0.55)
    ratio = min(left, right) / max(left, right) if max(left, right) else 0.0
    return "duas_colunas" if ratio >= 0.35 and (left + right) >= 60 else "uma_coluna"


def reorder_words(words: List[dict], layout: str, page_width: float) -> List[dict]:
    if layout != "duas_colunas":
        return sorted(words, key=lambda w: (round(float(w["top"]), 1), float(w["x0"])))

    mid = page_width / 2.0
    left_words = [w for w in words if float(w["x0"]) < mid]
    right_words = [w for w in words if float(w["x0"]) >= mid]
    left_sorted = sorted(left_words, key=lambda w: (round(float(w["top"]), 1), float(w["x0"])))
    right_sorted = sorted(right_words, key=lambda w: (round(float(w["top"]), 1), float(w["x0"])))
    return left_sorted + right_sorted


def words_to_text(words: List[dict]) -> str:
    if not words:
        return ""
    lines = {}
    for w in words:
        key = round(float(w["top"]), 1)
        lines.setdefault(key, []).append(w)
    ordered = []
    for top in sorted(lines.keys()):
        line_words = sorted(lines[top], key=lambda x: float(x["x0"]))
        ordered.append(" ".join(str(x["text"]) for x in line_words if str(x["text"]).strip()))
    return normalize_spaces("\n".join(ordered))


def ocr_page(doc: fitz.Document, page_index: int) -> str:
    if not OCR_AVAILABLE:
        return ""
    page = doc.load_page(page_index)
    pix = page.get_pixmap(matrix=fitz.Matrix(2, 2), alpha=False)
    img = Image.frombytes("RGB", [pix.width, pix.height], pix.samples)
    text = pytesseract.image_to_string(img, lang="por")
    return normalize_spaces(text)


@dataclass
class SourceEntry:
    source_id: str
    title: str
    relative_path: str
    allowed: str


def load_manifest(manifest_path: Path) -> List[SourceEntry]:
    entries: List[SourceEntry] = []
    with manifest_path.open("r", encoding="utf-8", newline="") as f:
        reader = csv.DictReader(f)
        for row in reader:
            entries.append(
                SourceEntry(
                    source_id=row["id"],
                    title=row["titulo"],
                    relative_path=row["arquivo_relativo"],
                    allowed=row["permitido_uso"].strip().lower(),
                )
            )
    return entries


def main() -> int:
    parser = argparse.ArgumentParser(description="Ingestao hibrida de PDFs para AGENTE GURPS.")
    parser.add_argument("--max-pages-per-pdf", type=int, default=9999, help="Limite de paginas por PDF (0 = todas as paginas).")
    parser.add_argument("--max-pdfs", type=int, default=0, help="Limite de PDFs (0 = todos).")
    args = parser.parse_args()

    root = Path(__file__).resolve().parents[2]
    base = root / "AGENTE GURPS"
    manifest = base / "sources" / "manifesto_fontes.csv"
    processed_dir = base / "sources" / "processed"
    reports_dir = processed_dir / "reports"
    pages_out = processed_dir / "pages.jsonl"
    chunks_out = processed_dir / "chunks.jsonl"
    report_out = reports_dir / "ingestao_inicial_report.json"

    reports_dir.mkdir(parents=True, exist_ok=True)
    processed_dir.mkdir(parents=True, exist_ok=True)

    entries = [e for e in load_manifest(manifest) if e.allowed == "sim"]
    if args.max_pdfs > 0:
        entries = entries[: args.max_pdfs]

    pages = []
    chunks = []
    report = {
        "pdfs_processados": 0,
        "paginas_processadas": 0,
        "paginas_duas_colunas": 0,
        "paginas_ocr_fallback": 0,
        "paginas_suspeitas": 0,
        "ocr_disponivel": OCR_AVAILABLE,
        "warnings": [],
        "sources": [],
    }

    for entry in entries:
        pdf_path = base / "sources" / entry.relative_path
        if not pdf_path.exists():
            report["warnings"].append(f"arquivo ausente: {entry.relative_path}")
            continue

        with pdfplumber.open(str(pdf_path)) as plumb, fitz.open(str(pdf_path)) as fitz_doc:
            total_pages = len(plumb.pages)
            max_pages = min(total_pages, args.max_pages_per_pdf) if args.max_pages_per_pdf > 0 else total_pages
            src_stats = {
                "id": entry.source_id,
                "titulo": entry.title,
                "arquivo": entry.relative_path,
                "paginas_lidas": max_pages,
                "paginas_total_pdf": total_pages,
                "suspeitas": 0,
            }

            for page_idx in range(max_pages):
                page = plumb.pages[page_idx]
                words = page.extract_words(use_text_flow=False, keep_blank_chars=False)
                layout = detect_layout(words, page.width)
                reordered = reorder_words(words, layout, page.width)
                native_text = words_to_text(reordered)
                used_ocr = False
                final_text = native_text

                suspect_native = len(native_text) < 180
                if suspect_native and OCR_AVAILABLE:
                    ocr_text = ocr_page(fitz_doc, page_idx)
                    if len(ocr_text) > len(native_text):
                        final_text = ocr_text
                        used_ocr = True

                # Pagina suspeita e avaliada pelo texto final apos fallback.
                suspect_final = len(final_text) < 180
                if suspect_final:
                    report["paginas_suspeitas"] += 1
                    src_stats["suspeitas"] += 1

                if layout == "duas_colunas":
                    report["paginas_duas_colunas"] += 1
                if used_ocr:
                    report["paginas_ocr_fallback"] += 1

                page_id = f"{entry.source_id}_p{page_idx+1}"
                pages.append(
                    {
                        "page_id": page_id,
                        "source_id": entry.source_id,
                        "source_title": entry.title,
                        "page_number": page_idx + 1,
                        "layout": layout,
                        "used_ocr": used_ocr,
                        "text": final_text,
                    }
                )

                for ci, chunk in enumerate(split_chunks(final_text), start=1):
                    chunks.append(
                        {
                            "chunk_id": f"{page_id}_c{ci}",
                            "source_id": entry.source_id,
                            "source_title": entry.title,
                            "page_number": page_idx + 1,
                            "text": chunk,
                            "language": "pt",
                        }
                    )

                report["paginas_processadas"] += 1

            report["pdfs_processados"] += 1
            report["sources"].append(src_stats)

    with pages_out.open("w", encoding="utf-8", newline="\n") as f:
        for p in pages:
            f.write(json.dumps(p, ensure_ascii=False) + "\n")

    with chunks_out.open("w", encoding="utf-8", newline="\n") as f:
        for c in chunks:
            f.write(json.dumps(c, ensure_ascii=False) + "\n")

    report["chunks_gerados"] = len(chunks)
    report["pages_output"] = str(pages_out.relative_to(root))
    report["chunks_output"] = str(chunks_out.relative_to(root))
    with report_out.open("w", encoding="utf-8", newline="\n") as f:
        json.dump(report, f, ensure_ascii=False, indent=2)

    print("OK: ingestao concluida.")
    print(json.dumps({
        "pdfs_processados": report["pdfs_processados"],
        "paginas_processadas": report["paginas_processadas"],
        "chunks_gerados": report["chunks_gerados"],
        "paginas_suspeitas": report["paginas_suspeitas"],
        "ocr_disponivel": report["ocr_disponivel"],
    }, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
