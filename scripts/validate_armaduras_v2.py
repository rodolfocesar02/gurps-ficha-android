#!/usr/bin/env python3
import argparse
import json
import re
import unicodedata
from collections import Counter
from pathlib import Path


VALID_LOCAIS = {
    "cabeca",
    "cranio",
    "rosto",
    "olhos",
    "pescoco",
    "corpo",
    "tronco",
    "virilha",
    "membros",
    "bracos",
    "maos",
    "pernas",
    "pes",
    "traje_completo",
}

MOJIBAKE_MARKERS = ("Ã", "Â", "�")


def normalize_text(value: str) -> str:
    n = unicodedata.normalize("NFKD", value or "")
    n = n.encode("ascii", "ignore").decode("ascii").lower()
    n = re.sub(r"[^a-z0-9]+", " ", n)
    return re.sub(r"\s+", " ", n).strip()


def normalize_local(value: str) -> str:
    n = unicodedata.normalize("NFKD", value or "")
    n = n.encode("ascii", "ignore").decode("ascii").lower()
    n = re.sub(r"\s+", "_", n.strip())
    return n


def split_locais(raw: str) -> list[str]:
    return [x.strip() for x in re.split(r"[,;/|]", raw or "") if x.strip()]


def has_mojibake(text: str) -> bool:
    return any(marker in (text or "") for marker in MOJIBAKE_MARKERS)


def main() -> int:
    parser = argparse.ArgumentParser(description="Valida armaduras.v2.json")
    parser.add_argument("--input", required=True, help="Caminho do armaduras.v2.json")
    args = parser.parse_args()

    payload = json.loads(Path(args.input).read_text(encoding="utf-8"))
    items = payload.get("items", [])

    id_counter = Counter()
    nome_norm_counter = Counter()
    unknown_locals = Counter()
    mojibake_fields = []

    for item in items:
        item_id = (item.get("id") or "").strip()
        nome = (item.get("nome") or "").strip()
        local_raw = (item.get("localRaw") or "").strip()

        id_counter[item_id] += 1
        if nome:
            nome_norm_counter[normalize_text(nome)] += 1

        for token in split_locais(local_raw):
            loc = normalize_local(token)
            if loc and loc not in VALID_LOCAIS:
                unknown_locals[loc] += 1

        if has_mojibake(nome):
            mojibake_fields.append(f"item.nome:{item_id}")
        if has_mojibake(local_raw):
            mojibake_fields.append(f"item.localRaw:{item_id}")

        for comp in item.get("componentes", []):
            comp_local = (comp.get("localRaw") or "").strip()
            if has_mojibake(comp_local):
                mojibake_fields.append(f"comp.localRaw:{item_id}")
            for token in split_locais(comp_local):
                loc = normalize_local(token)
                if loc and loc not in VALID_LOCAIS:
                    unknown_locals[loc] += 1

    id_dups = [k for k, c in id_counter.items() if k and c > 1]
    nome_dups = [k for k, c in nome_norm_counter.items() if k and c > 1]

    print(f"items={len(items)}")
    print(f"id_duplicates={len(id_dups)}")
    print(f"nome_normalizado_duplicates={len(nome_dups)}")
    print(f"locais_desconhecidos={len(unknown_locals)}")
    print(f"mojibake_flags={len(mojibake_fields)}")

    if id_dups:
        print("ERRO: IDs duplicados")
        for d in id_dups[:20]:
            print(f"- {d}")
    if nome_dups:
        print("ALERTA: nomes normalizados repetidos")
        for d in nome_dups[:20]:
            print(f"- {d}")
    if unknown_locals:
        print("ALERTA: locais fora da lista canônica")
        for loc, count in unknown_locals.most_common(20):
            print(f"- {loc} x{count}")
    if mojibake_fields:
        print("ALERTA: texto potencialmente quebrado")
        for f in mojibake_fields[:20]:
            print(f"- {f}")

    return 1 if id_dups else 0


if __name__ == "__main__":
    raise SystemExit(main())
