#!/usr/bin/env python3
import argparse
import json
import re
from datetime import datetime, timezone
from pathlib import Path


def parse_int(value: str):
    if value is None:
        return None
    s = str(value).strip()
    m = re.match(r"^-?\d+", s)
    if m:
        return int(m.group(0))
    return None


def parse_float_pt(value: str):
    if value is None:
        return None
    s = str(value).strip()
    if not s:
        return None
    if "," in s:
        cleaned = s.replace(".", "").replace(",", ".")
    elif "." in s:
        # Heuristica: "1.500" -> 1500; "6.8" -> 6.8
        m = re.fullmatch(r"(-?\d+)\.(\d+)", s)
        if not m:
            return None
        frac = m.group(2)
        cleaned = m.group(1) + frac if len(frac) == 3 else s
    else:
        cleaned = s
    if re.fullmatch(r"-?\d+(\.\d+)?", cleaned):
        return float(cleaned)
    return None


def split_pair(value: str):
    if value is None:
        return []
    return [x.strip() for x in str(value).split("/") if x.strip()]


def parse_money(value: str):
    if value is None:
        return {"raw": "", "valor": None}
    s = str(value).strip()
    cleaned = s.replace("$", "").replace("+", "").strip()
    val = parse_float_pt(cleaned)
    return {"raw": s, "valor": val}


def parse_st(value: str):
    if value is None:
        return {"raw": "", "valor": None, "flags": []}
    s = str(value).strip()
    flags = []
    if "†" in s or "â€ " in s:
        flags.append("dagger")
    if "‡" in s or "â€¡" in s:
        flags.append("double_dagger")
    cleaned = (
        s.replace("†", "")
        .replace("‡", "")
        .replace("â€ ", "")
        .replace("â€¡", "")
        .replace("Des", "")
        .replace("T", "")
        .strip()
    )
    return {"raw": s, "valor": parse_int(cleaned), "flags": flags}


def is_special(value: str):
    if value is None:
        return True
    s = str(value).strip().lower()
    return s in {"", "-", "—", "espec.", "var.", "var", "â€”"}


def review_flags(item: dict):
    flags = []
    if item.get("nt") is None:
        flags.append("nt_nao_numerico")
    if item.get("stMinimo", {}).get("valor") is None and not is_special(item.get("stMinimo", {}).get("raw", "")):
        flags.append("st_minimo_nao_numerico")
    if item.get("custo", {}).get("valor") is None and not is_special(item.get("custo", {}).get("raw", "")):
        flags.append("custo_nao_numerico")
    if item.get("cdt", {}).get("valor") is None and not is_special(item.get("cdt", {}).get("raw", "")):
        flags.append("cdt_nao_numerico")
    if item.get("cl", {}).get("valor") is None and not is_special(item.get("cl", {}).get("raw", "")):
        flags.append("cl_nao_numerico")
    if item.get("recuo", {}).get("valor") is None and not is_special(item.get("recuo", {}).get("raw", "")):
        flags.append("recuo_nao_numerico")
    if len(item.get("alcanceDistancia", {}).get("partes", [])) > 2:
        flags.append("alcance_formato_incomum")
    if len(item.get("peso", {}).get("partes", [])) > 2:
        flags.append("peso_formato_incomum")
    return sorted(set(flags))


def normalize(raw: dict):
    items = []
    for src in raw.get("items", []):
        dist_parts = split_pair(src.get("alcance_distancia"))
        peso_parts = split_pair(src.get("peso"))
        recuo_raw = src.get("Recuo")
        cl_raw = src.get("Cl")

        item = {
            "id": src.get("id"),
            "tipo": src.get("tipo"),
            "categoria": src.get("categoria"),
            "grupo": src.get("grupo"),
            "nome": src.get("nome"),
            "ntRaw": src.get("nt"),
            "nt": parse_int(src.get("nt")),
            "dano": {"raw": src.get("tipo_dano", "")},
            "precisao": {"raw": src.get("precisao"), "valor": parse_int(src.get("precisao"))},
            "alcanceDistancia": {
                "raw": src.get("alcance_distancia", ""),
                "partes": dist_parts,
                "metadeDano": dist_parts[0] if len(dist_parts) >= 1 else None,
                "maximo": dist_parts[1] if len(dist_parts) >= 2 else None,
            },
            "peso": {
                "raw": src.get("peso", ""),
                "partes": peso_parts,
                "armaKg": parse_float_pt(peso_parts[0]) if len(peso_parts) >= 1 else None,
                "municaoKg": parse_float_pt(peso_parts[1]) if len(peso_parts) >= 2 else None,
            },
            "cdt": {"raw": src.get("Cdt"), "valor": parse_int(src.get("Cdt"))},
            "tiros": {"raw": src.get("tiros", "")},
            "custo": parse_money(src.get("custo")),
            "stMinimo": parse_st(src.get("st_minimo")),
            "magnitude": {"raw": src.get("Magnitude"), "valor": parse_int(src.get("Magnitude"))},
            "recuo": {"raw": recuo_raw, "valor": parse_int(recuo_raw)},
            "cl": {"raw": cl_raw, "valor": parse_int(cl_raw)},
            "observacoes": src.get("observacoes", ""),
            "source": src.get("source", ""),
        }
        item["reviewFlags"] = sorted(set((src.get("reviewFlags") or []) + review_flags(item)))
        items.append(item)

    review_count = sum(1 for x in items if x.get("reviewFlags"))
    return {
        "version": 1,
        "kind": "armas_distancia_normalized",
        "sourceKind": raw.get("kind"),
        "sourceFile": raw.get("sourceFile"),
        "generatedAtUtc": datetime.now(timezone.utc).isoformat(),
        "totalItems": len(items),
        "reviewNeededItems": review_count,
        "items": items,
    }


def convert(input_json: Path, output_json: Path):
    raw = json.loads(input_json.read_text(encoding="utf-8"))
    out = normalize(raw)
    output_json.parent.mkdir(parents=True, exist_ok=True)
    output_json.write_text(json.dumps(out, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"OK: {out['totalItems']} itens => {output_json}")
    print(f"Itens com reviewFlags: {out['reviewNeededItems']}")


def main():
    parser = argparse.ArgumentParser(description="Normaliza armas à distância v1 a partir do JSON raw")
    parser.add_argument("--input", required=True, help="Caminho do armas_distancia.v1.raw.json")
    parser.add_argument("--output", required=True, help="Caminho do armas_distancia.v1.normalized.json")
    args = parser.parse_args()
    convert(Path(args.input), Path(args.output))


if __name__ == "__main__":
    main()
