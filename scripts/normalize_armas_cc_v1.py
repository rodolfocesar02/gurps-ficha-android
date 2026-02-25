#!/usr/bin/env python3
import argparse
import json
import re
from datetime import datetime, timezone
from pathlib import Path


def pick(lst, idx):
    if not lst:
        return None
    if idx < len(lst):
        return lst[idx]
    return lst[-1]


def normalize_damage(raw: str):
    text = (raw or "").strip()
    if not text:
        return {"raw": text, "base": None, "tipo": None, "modos": []}

    def parse_tipo(fragmento: str):
        low = fragmento.lower()
        if "corte" in low:
            return "corte"
        if "cont" in low:
            return "cont"
        if "perf" in low:
            return "perf"
        if "qmd" in low:
            return "qmd"
        if "at" in low:
            return "at"
        return None

    partes = [p.strip() for p in text.split("/") if p.strip()]
    modos = []
    for parte in partes:
        m = re.search(r"(GdP|GeB|HT-\d+\([^)]+\)|\d+d(?:[+-]\d+)?(?:\(\d+\))?)", parte, re.IGNORECASE)
        modos.append(
            {
                "raw": parte,
                "base": m.group(1) if m else None,
                "tipo": parse_tipo(parte),
            }
        )

    base_legacy = modos[0]["base"] if modos else None
    tipo_legacy = modos[0]["tipo"] if modos else None
    return {"raw": text, "base": base_legacy, "tipo": tipo_legacy, "modos": modos}


def parse_nt(raw: str):
    if raw is None:
        return None
    s = str(raw).strip()
    if re.fullmatch(r"-?\d+", s):
        return int(s)
    return None


def parse_st(raw: str):
    if raw is None:
        return {"raw": "", "valor": None, "flags": []}
    s = str(raw).strip()
    flags = []
    if "†" in s:
        flags.append("dagger")
    if "‡" in s:
        flags.append("double_dagger")
    cleaned = s.replace("†", "").replace("‡", "").strip()
    return {"raw": s, "valor": int(cleaned) if re.fullmatch(r"-?\d+", cleaned) else None, "flags": flags}


def is_special_token(s: str):
    if s is None:
        return True
    low = s.strip().lower()
    return low in {"", "—", "-", "var.", "var", "desp.", "nao", "não"}


def review_flags(item):
    flags = []
    if item.get("nt") is None:
        flags.append("nt_nao_numerico")
    if item.get("stMinimo", {}).get("valor") is None and not is_special_token(item.get("stMinimo", {}).get("raw", "")):
        flags.append("st_minimo_nao_numerico")
    if re.search(r"(GdP|GeB|HT-\d|\d+d)", item.get("grupo", ""), re.IGNORECASE):
        flags.append("grupo_suspeito_possivel_desalinhamento")
    for mode in item.get("modos", []):
        if is_special_token(mode.get("alcanceCorpo", "")):
            flags.append("alcance_especial")
            break
    return sorted(set(flags))


def normalize(raw_data):
    out_items = []
    for src in raw_data["items"]:
        custos = src.get("custoModos") or []
        pesos = src.get("pesoModos") or []
        alcances = src.get("alcanceCorpoModos") or []
        aparares = src.get("apararModos") or []

        mode_count = max(len(custos), len(pesos), len(alcances), len(aparares), 1)
        modos = []
        for i in range(mode_count):
            custo_obj = pick(custos, i) or {}
            peso_obj = pick(pesos, i) or {}
            modos.append(
                {
                    "modo": i + 1,
                    "alcanceCorpo": pick(alcances, i),
                    "aparar": pick(aparares, i),
                    "custo": {
                        "raw": custo_obj.get("raw"),
                        "kind": custo_obj.get("kind"),
                        "valor": custo_obj.get("value"),
                        "temPrefixoMais": custo_obj.get("hasPlusPrefix", False),
                    },
                    "peso": {
                        "raw": peso_obj.get("raw"),
                        "kg": peso_obj.get("kg"),
                    },
                }
            )

        item = {
            "id": src["id"],
            "rowNumber": src.get("rowNumber"),
            "tipo": src.get("tipo"),
            "categoria": src.get("categoria"),
            "grupo": src.get("grupo"),
            "nome": src.get("nome"),
            "nt": parse_nt(src.get("ntRaw")),
            "ntRaw": src.get("ntRaw"),
            "dano": normalize_damage(src.get("danoRaw", "")),
            "stMinimo": parse_st(src.get("stMinimoRaw")),
            "observacoes": src.get("observacoes"),
            "modos": modos,
            "raw": {
                "alcanceCorpo": src.get("alcanceCorpoRaw"),
                "aparar": src.get("apararRaw"),
                "custo": src.get("custoRaw"),
                "peso": src.get("pesoRaw"),
            },
        }
        item["reviewFlags"] = review_flags(item)
        out_items.append(item)

    review_total = sum(1 for x in out_items if x.get("reviewFlags"))
    return {
        "version": 1,
        "kind": "armas_corpo_a_corpo_normalized",
        "sourceKind": raw_data.get("kind"),
        "sourceFile": raw_data.get("sourceFile"),
        "generatedAtUtc": datetime.now(timezone.utc).isoformat(),
        "totalItems": len(out_items),
        "reviewNeededItems": review_total,
        "items": out_items,
    }


def convert(input_json: Path, output_json: Path):
    raw = json.loads(input_json.read_text(encoding="utf-8"))
    data = normalize(raw)
    output_json.parent.mkdir(parents=True, exist_ok=True)
    output_json.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"OK: {data['totalItems']} itens => {output_json}")
    print(f"Itens com reviewFlags: {data['reviewNeededItems']}")


def main():
    parser = argparse.ArgumentParser(description="Normaliza armas corpo a corpo v1 a partir do JSON raw")
    parser.add_argument("--input", required=True, help="Caminho do armas_corpo_a_corpo.v1.raw.json")
    parser.add_argument("--output", required=True, help="Caminho do armas_corpo_a_corpo.v1.normalized.json")
    args = parser.parse_args()
    convert(Path(args.input), Path(args.output))


if __name__ == "__main__":
    main()
