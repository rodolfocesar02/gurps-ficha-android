# -*- coding: utf-8 -*-
# Lote 371 (Saga): valida que os catalogos de armas tem os stats que o combate consome.
# Corpo-a-corpo: cada item precisa de modos[].alcanceCorpo (reach) e modos[].aparar.
# A distancia/fogo: precisao, alcanceDistancia (metadeDano/maximo), cdt, magnitude.
# Nao quebra build (e um check informativo): codigo != 0 se faltar campo CRITICO p/ combate.
import json, os, sys

os.chdir(os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets"))

def carregar(arquivo):
    return json.load(open(arquivo, encoding="utf-8")).get("items", [])

def check_corpo_a_corpo(erros, avisos):
    items = carregar("armas_corpo_a_corpo.v1.normalized.json")
    for it in items:
        rot = it.get("id") or it.get("nome") or "?"
        modos = it.get("modos") or []
        if not modos:
            erros.append(f"[CaC {rot}] sem 'modos'.")
            continue
        for m in modos:
            if not str(m.get("alcanceCorpo", "")).strip():
                erros.append(f"[CaC {rot}] modo sem 'alcanceCorpo' (reach 'C'/'1'/'1,2').")
            if "aparar" not in m:
                avisos.append(f"[CaC {rot}] modo sem 'aparar'.")
    return len(items)

def check_distancia(arquivo, erros, avisos):
    items = carregar(arquivo)
    for it in items:
        rot = it.get("id") or it.get("nome") or "?"
        if (it.get("precisao") or {}).get("valor") is None:
            avisos.append(f"[{arquivo} {rot}] sem precisao.valor (Acc).")
        alc = it.get("alcanceDistancia") or {}
        if not str(alc.get("maximo") or alc.get("metadeDano") or "").strip():
            erros.append(f"[{arquivo} {rot}] sem alcanceDistancia (maximo/metadeDano).")
        if (it.get("magnitude") or {}).get("valor") is None:
            avisos.append(f"[{arquivo} {rot}] sem magnitude (Bulk).")
        if (it.get("cdt") or {}).get("valor") is None:
            avisos.append(f"[{arquivo} {rot}] sem cdt (CdT).")
    return len(items)

def main():
    erros, avisos = [], []
    n_cc = check_corpo_a_corpo(erros, avisos)
    n_d = check_distancia("armas_distancia.v1.normalized.json", erros, avisos)
    n_f = check_distancia("armas_fogo.v1.normalized.json", erros, avisos)

    print("=" * 60)
    print(f"Armas: corpo-a-corpo={n_cc} | distancia={n_d} | fogo={n_f}")
    if avisos:
        print(f"AVISOS ({len(avisos)}) (nao bloqueiam):")
        for a in avisos[:40]:
            print("  ~ " + a)
        if len(avisos) > 40:
            print(f"  ... +{len(avisos) - 40} avisos")
    if erros:
        print(f"ERROS ({len(erros)}):")
        for e in erros:
            print("  - " + e)
        sys.exit(1)
    print("OK: campos criticos de combate presentes.")
    sys.exit(0)

if __name__ == "__main__":
    main()
