#!/usr/bin/env python3
import argparse
import json
import re
import unicodedata
from datetime import datetime, timezone
from pathlib import Path

import fitz


def slugify(value: str) -> str:
    normalized = unicodedata.normalize("NFKD", value)
    ascii_only = normalized.encode("ascii", "ignore").decode("ascii")
    lowered = ascii_only.lower()
    lowered = re.sub(r"[^a-z0-9]+", "_", lowered)
    lowered = re.sub(r"_+", "_", lowered).strip("_")
    return lowered or "arma_distancia"


def clean_lines(text: str):
    return [ln.strip() for ln in text.splitlines() if ln.strip()]


def is_group_line(line: str):
    if line in {"NT", "Arma", "Dano", "Prec", "Distância", "Peso", "CdT", "Tiros", "Custo", "ST"}:
        return False
    if line.startswith("Observações"):
        return False
    # Evita capturar células como "A(1)" ou "1(4)" como se fossem grupos.
    if re.fullmatch(r"[A-Za-z]?\([^)]+\)", line):
        return False
    if re.fullmatch(r"\d+|—|\^", line):
        return False
    return "(" in line and ")" in line


def parse_page_2_rows(lines):
    # Tabela da página 2 usa: NT, Arma, Dano, Prec, Distância, Peso, CdT, Tiros, Custo, ST, Magnit., Observações
    rows = []
    current_group = None
    i = 0
    nt_pattern = re.compile(r"^\d+$|^—$|^\^$")

    while i < len(lines):
        line = lines[i]
        if line.startswith("Observações"):
            break

        if is_group_line(line):
            current_group = line
            i += 1
            continue

        if nt_pattern.fullmatch(line):
            # tentativa direta de 12 colunas
            chunk = lines[i : i + 12]
            if len(chunk) < 12:
                break
            nt, arma, dano, prec, dist, peso, cdt, tiros, custo, st, magnit, obs = chunk
            # validação mínima para evitar falso positivo em fluxo de texto
            if "$" not in custo and custo not in {"—", "-", "var.", "var"}:
                i += 1
                continue

            rows.append(
                {
                    "tipo": "arma_distancia",
                    "categoria": "Tabela de Armas de Combate à Distância",
                    "grupo": current_group or "NÃO_CLASSIFICADO",
                    "nt": nt,
                    "nome": arma,
                    "tipo_dano": dano,
                    "precisao": prec,
                    "alcance_distancia": dist,
                    "peso": peso,
                    "Cdt": cdt,
                    "tiros": tiros,
                    "custo": custo,
                    "st_minimo": st,
                    "Magnitude": magnit,
                    "Recuo": "",
                    "Cl": "",
                    "observacoes": obs,
                    "source": "pdf_page_2",
                    "reviewFlags": [],
                }
            )
            i += 12
            continue

        i += 1

    return rows


def convert(input_pdf: Path, output_json: Path):
    doc = fitz.open(input_pdf)
    if doc.page_count < 2:
        raise SystemExit("PDF sem páginas suficientes para extrair a tabela de armas à distância.")

    page_text = doc[1].get_text("text")
    lines = clean_lines(page_text)
    items = parse_page_2_rows(lines)

    used = set()
    for it in items:
        base = slugify(it["nome"])
        fid = base
        n = 2
        while fid in used:
            fid = f"{base}_{n}"
            n += 1
        used.add(fid)
        it["id"] = fid

        # flags iniciais para revisão manual
        if it["nt"] in {"—", "^"}:
            it["reviewFlags"].append("nt_nao_numerico")
        if it["grupo"] == "NÃO_CLASSIFICADO":
            it["reviewFlags"].append("grupo_nao_classificado")
        if "/" in it["nome"] or it["nome"].startswith("com "):
            it["reviewFlags"].append("nome_possivel_submodo")

    payload = {
        "version": 1,
        "kind": "armas_distancia_raw_from_pdf",
        "sourceFile": str(input_pdf),
        "generatedAtUtc": datetime.now(timezone.utc).isoformat(),
        "totalItems": len(items),
        "items": items,
    }
    output_json.parent.mkdir(parents=True, exist_ok=True)
    output_json.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    print(f"OK: {len(items)} itens => {output_json}")


def main():
    parser = argparse.ArgumentParser(description="Extrai armas de combate à distância (draft) do PDF OCR")
    parser.add_argument("--input", required=True, help="Caminho do PDF")
    parser.add_argument("--output", required=True, help="Caminho do JSON de saída")
    args = parser.parse_args()
    convert(Path(args.input), Path(args.output))


if __name__ == "__main__":
    main()
