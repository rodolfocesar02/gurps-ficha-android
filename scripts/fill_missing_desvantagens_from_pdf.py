#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import re
import unicodedata
from pathlib import Path
from typing import Dict, List, Optional, Tuple

from pypdf import PdfReader


def normalize(text: str) -> str:
    text = unicodedata.normalize("NFD", text)
    text = "".join(ch for ch in text if unicodedata.category(ch) != "Mn")
    return text.lower()


def compact(text: str) -> str:
    return re.sub(r"\s+", " ", text).strip()


def clean_excerpt(text: str) -> str:
    text = text.replace("\r", "\n")
    text = text.replace("-\n", "")
    text = re.sub(r"\n\s*\n+", "\n", text)
    text = re.sub(r"[ \t]+", " ", text)
    text = re.sub(r"\n[ \t]+", "\n", text)
    return text.strip()


def file_by_token(folder: Path, token: str) -> Optional[Path]:
    token_norm = normalize(token)
    for p in folder.glob("*.pdf"):
        if token_norm in normalize(p.name):
            return p
    return None


def build_aliases(nome: str, item_id: str) -> List[str]:
    aliases = [nome]
    explicit: Dict[str, List[str]] = {
        "desdobramento_de_pers": ["Desdobramento de Pers.", "Desdobramento de Personalidade"],
        "deficiencia_fisica": ["Deficiência Física"],
        "dificuldade_com_numeros": ["Dificuldade com Números"],
        "fragilidade_em_aceleracao": ["Fragilidade em Aceleração"],
        "nao_iconografico": ["Não Iconográfico"],
        "padrao_de_tempo_reduzido": ["Padrão de Tempo Reduzido"],
        "paralisia_frente_ao_combate": ["Paralisia Frente ao Combate"],
        "susceptibilidade_a_magia": ["Susceptibilidade à Magia"],
        "susceptibilidade_a_magia": ["Susceptibilidade à Magia", "Suscetibilidade à Magia"],
        "visao_restrita": ["Visão Restrita"],
    }
    aliases.extend(explicit.get(item_id, []))
    seen = set()
    out: List[str] = []
    for a in aliases:
        k = normalize(a)
        if k in seen:
            continue
        seen.add(k)
        out.append(a)
    return out


def find_segment(
    text: str,
    all_names: List[str],
    aliases: List[str],
) -> Optional[str]:
    text_norm = normalize(text)
    starts: List[Tuple[int, str, int]] = []

    for alias in aliases:
        alias_norm = normalize(alias)
        pos = 0
        while True:
            idx = text_norm.find(alias_norm, pos)
            if idx < 0:
                break
            prev = text_norm[idx - 1] if idx > 0 else "\n"
            after_idx = idx + len(alias_norm)
            after = text_norm[after_idx: after_idx + 16]
            score = 0
            if prev in {"\n", "\r", ".", ":", ";"}:
                score += 2
            if re.match(r"\s*(?:\d+|[-–—]|\bv\.)", after):
                score += 2
            starts.append((idx, alias, score))
            pos = idx + 1

    if not starts:
        return None

    best_text: Optional[str] = None
    best_score = -1

    for start, alias, start_score in starts:
        end = len(text)
        search_from = start + len(alias)
        for other in all_names:
            o_norm = normalize(other)
            idx = text_norm.find(o_norm, search_from)
            if idx >= 0 and idx < end:
                end = idx

        excerpt = text[start:end]
        for a in aliases:
            excerpt = re.sub(
                rf"^\s*{re.escape(a)}\s*(?:\d+\s*)?",
                "",
                excerpt,
                flags=re.IGNORECASE,
            )
        excerpt = re.sub(r"^\s*[-–—]?\s*\d+(?:\s*a\s*\d+)?\s*pontos?\*?\s*", "", excerpt, flags=re.IGNORECASE)
        excerpt = clean_excerpt(excerpt)
        length_score = min(len(compact(excerpt)) // 80, 5)
        total = start_score + length_score
        if len(compact(excerpt)) >= 40 and total > best_score:
            best_text = excerpt
            best_score = total

    return best_text


def extract_missing(
    json_path: Path,
    pdf_path: Path,
    report_path: Path,
) -> None:
    data = json.loads(json_path.read_text(encoding="utf-8"))
    reader = PdfReader(str(pdf_path))

    all_names = [str(item.get("nome", "")).strip() for item in data if str(item.get("nome", "")).strip()]
    filled: List[Dict[str, object]] = []
    unresolved: List[Dict[str, object]] = []

    for item in data:
        desc = str(item.get("descricao") or "").strip()
        if desc:
            continue

        nome = str(item.get("nome", "")).strip()
        item_id = str(item.get("id", "")).strip()
        pagina = int(item.get("pagina") or 0)
        aliases = build_aliases(nome, item_id)

        candidate_ranges = [
            (max(1, pagina), min(len(reader.pages), pagina + 1)),
            (max(1, pagina - 1), min(len(reader.pages), pagina + 2)),
            (max(1, pagina - 2), min(len(reader.pages), pagina + 3)),
        ]

        found = None
        used_range: Optional[Tuple[int, int]] = None
        for start_pg, end_pg in candidate_ranges:
            text_parts: List[str] = []
            for pg in range(start_pg, end_pg + 1):
                page_text = reader.pages[pg - 1].extract_text() or ""
                if page_text:
                    text_parts.append(page_text)
            combined = "\n".join(text_parts)
            found = find_segment(combined, all_names, aliases)
            if found:
                used_range = (start_pg, end_pg)
                break

        if found:
            item["descricao"] = found
            filled.append(
                {
                    "id": item_id,
                    "nome": nome,
                    "pagina_json": pagina,
                    "paginas_busca": list(used_range) if used_range else None,
                }
            )
        else:
            unresolved.append({"id": item_id, "nome": nome, "pagina_json": pagina})

    json_path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    payload = {
        "total": len(data),
        "missing_after": sum(1 for item in data if not str(item.get("descricao") or "").strip()),
        "filled_now": len(filled),
        "filled_ids": filled,
        "unresolved": unresolved,
    }
    report_path.parent.mkdir(parents=True, exist_ok=True)
    report_path.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(payload, ensure_ascii=False, indent=2))


def main() -> None:
    parser = argparse.ArgumentParser(description="Preenche descricoes faltantes de desvantagens via PDF canonico.")
    parser.add_argument("--json", required=True, type=Path, help="Arquivo desvantagens.v2.json")
    parser.add_argument("--pdf", required=False, type=Path, help="PDF do Modulo Basico")
    parser.add_argument("--report", required=True, type=Path, help="Relatorio JSON de execucao")
    args = parser.parse_args()

    pdf_path = args.pdf
    if pdf_path is None:
        raw_folder = Path("AGENTE GURPS/sources/raw")
        pdf_path = file_by_token(raw_folder, "modulo basico.pdf")
        if pdf_path is None:
            raise SystemExit("Nao achei automaticamente o PDF 'Modulo Basico' em AGENTE GURPS/sources/raw.")

    extract_missing(args.json, pdf_path, args.report)


if __name__ == "__main__":
    main()
