# -*- coding: utf-8 -*-
"""
Simula o que a UI vai mostrar nos botoes Ampliacoes/Limitacoes, para CADA
vantagem e desvantagem do catalogo.

Existe porque testar so a funcao de filtro com dados inventados nao prova nada
sobre o catalogo real: o valor esta em rodar a mesma regra sobre os 218
modificadores e as 493 vantagens/desvantagens de verdade e conferir invariantes.

Reproduz `classificarModificador` e `modificadorCabeEm`
(TraitCommonComponents.kt). Se a regra do Kotlin mudar, mudar aqui tambem.

Uso: python scripts/simular_modificadores_ui.py
Sai com codigo 1 se alguma invariante quebrar.
"""
import json
import os
import re

AQUI = os.path.dirname(os.path.abspath(__file__))
ASSETS = os.path.join(os.path.dirname(AQUI), "app", "src", "main", "assets")


def carregar(nome):
    with open(os.path.join(ASSETS, nome), encoding="utf-8") as f:
        return json.load(f)


def classificar(mod):
    """Espelha classificarModificador: tipo primeiro, sinal do valor como rede."""
    t = (mod.get("tipo") or "").strip().lower()
    if t.startswith("amp"):
        return "ampliacao"
    if t.startswith("lim"):
        return "limitacao"
    n = re.search(r"-?\d+", mod.get("valor") or "")
    return "limitacao" if (n and int(n.group()) < 0) else "ampliacao"


def cabe_em(mod, traco_id):
    """Espelha modificadorCabeEm."""
    dono = (mod.get("donoId") or "").strip()
    return not dono or dono == traco_id


def main():
    mods = carregar("modificadores.v1.json")
    poderes = carregar("modificadores_poderes.v1.json")
    tracos = carregar("vantagens.v3.json") + carregar("desvantagens.v2.json")

    falhas = []

    # --- Invariante 1: todo modificador cai em EXATAMENTE um dos dois botoes ---
    for m in mods + poderes:
        if classificar(m) not in ("ampliacao", "limitacao"):
            falhas.append(f"[1] sem botao: {m['id']}")

    # --- Invariante 2: todo donoId aponta para um traco existente ---
    ids_tracos = {t["id"] for t in tracos}
    for m in mods:
        dono = (m.get("donoId") or "").strip()
        if dono and dono not in ids_tracos:
            falhas.append(f"[2] donoId orfao: {m['id']} -> {dono}")

    # --- Invariante 3: modificador com dono aparece no dono e em mais ninguem ---
    com_dono = [m for m in mods if (m.get("donoId") or "").strip()]
    for m in com_dono:
        dono = m["donoId"]
        if not cabe_em(m, dono):
            falhas.append(f"[3] nao aparece no proprio dono: {m['id']}")
        outro = next((t["id"] for t in tracos if t["id"] != dono), None)
        if outro and cabe_em(m, outro):
            falhas.append(f"[3] vaza para outro traco: {m['id']} em {outro}")

    # --- Invariante 4: nenhum traco fica sem opcao nos dois botoes ---
    # (uma lista vazia indicaria filtro agressivo demais)
    vazios = []
    total_amp = total_lim = 0
    for t in tracos:
        visiveis = [m for m in mods if cabe_em(m, t["id"])]
        amp = sum(1 for m in visiveis if classificar(m) == "ampliacao")
        lim = sum(1 for m in visiveis if classificar(m) == "limitacao")
        total_amp += amp
        total_lim += lim
        if amp == 0 or lim == 0:
            vazios.append((t["id"], amp, lim))
    if vazios:
        falhas.append(f"[4] tracos com botao vazio: {vazios[:5]}")

    # --- Relatorio ---
    n = len(tracos)
    antes = sum(1 for m in mods if classificar(m) == "ampliacao")
    antes_l = len(mods) - antes
    print(f"tracos simulados: {n} (vantagens + desvantagens)")
    print(f"modificadores no catalogo geral: {len(mods)}")
    print()
    print("media de opcoes por traco:")
    print(f"   Ampliacoes: {antes} -> {total_amp / n:.1f}   (antes do filtro -> depois)")
    print(f"   Limitacoes: {antes_l} -> {total_lim / n:.1f}")
    print(f"   escondidos por nao pertencerem ao traco: {len(com_dono)} marcados")

    # Amostra concreta: um traco sem modificador proprio e um com.
    for alvo in ("abafador_de_mana", "nao_respira", "resistencia_a_dano"):
        t = next((x for x in tracos if x["id"] == alvo), None)
        if not t:
            continue
        vis = [m for m in mods if cabe_em(m, alvo)]
        proprios = [m for m in mods if (m.get("donoId") or "") == alvo]
        print(f"   {t['nome']}: ve {len(vis)} de {len(mods)}"
              f" ({len(proprios)} exclusivos dele)")

    if falhas:
        print(f"\nFALHAS ({len(falhas)}):")
        for f in falhas:
            print(f"   ! {f}")
        return 1
    print("\nOK: todas as invariantes passaram.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
