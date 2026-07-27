# -*- coding: utf-8 -*-
"""
Marca em modificadores.v1.json quais modificadores pertencem a UMA vantagem
especifica, gravando o campo `donoId`.

Problema: 56 dos 218 modificadores do catalogo geral sao, no livro, "Ampliacoes
Especiais"/"Limitacoes Especiais" de uma vantagem (p.ex. Guelras so existe para
Nao Respira; Pele Resistente so para Resistencia a Dano). Como o app mostra o
catalogo geral inteiro para QUALQUER vantagem, o jogador ve dezenas de opcoes
que nao fazem sentido para o traco que esta editando.

Por que MARCAR e nao MOVER para dentro da vantagem: `RacaCatalogo` resolve
racas e metacaracteristicas procurando modificador por id no catalogo geral
(RacaCatalogo.kt:152 e :217). 12 dos 56 sao citados por racas -- remove-los
quebraria a resolucao silenciosamente. Marcando, o id continua onde esta e
quem filtra e a UI.

Fichas ja salvas nao correm risco: ModificadorSelecao grava id/nome/valor/
niveis dentro da propria ficha e nao consulta o catalogo ao carregar.

Uso:
    python scripts/marcar_donos_modificadores.py             # dry-run
    python scripts/marcar_donos_modificadores.py --aplicar   # grava
"""
import argparse
import json
import os
import re
import unicodedata

AQUI = os.path.dirname(os.path.abspath(__file__))
RAIZ = os.path.dirname(AQUI)
ASSETS = os.path.join(RAIZ, "app", "src", "main", "assets")
ALVO = os.path.join(ASSETS, "modificadores.v1.json")

# Dono de cada modificador presos a uma vantagem. A chave e o `id` do
# modificador; o valor e o NOME da vantagem/desvantagem dona, resolvido para id
# real contra vantagens.v3.json / desvantagens.v2.json.
#
# Cada atribuicao foi conferida no Modulo Basico pelo heading '##' que manda na
# secao 'Ampliacoes/Limitacoes Especiais' -- e, quando a pagina do JSON estava
# errada, pelo proprio texto do modificador (ex.: 'So Sofre Dano de X' diz
# "afetar a Retencao"; 'Ativada ou Desativada a Vontade' diz "pode se tornar
# invisivel").
DONOS = {
    # Resistencia a Dano (MB p.84-85)
    "mod_absorcao": "Resistência a Dano",
    "mod_campo_de_forca": "Resistência a Dano",
    "mod_enrijecido": "Resistência a Dano",
    "mod_reflexao": "Resistência a Dano",
    "mod_parcial": "Resistência a Dano",
    "mod_pele_resistente": "Resistência a Dano",
    "mod_sem_armadura": "Resistência a Dano",
    "mod_semiablativa": "Resistência a Dano",
    # Retencao (MB p.85-86)
    "mod_engolfar": "Retenção",
    "mod_grudento": "Retenção",
    "mod_inquebravel": "Retenção",
    "mod_so_sofre_dano_de_x_1": "Retenção",
    "mod_so_sofre_dano_de_x_2": "Retenção",
    "mod_so_sofre_dano_de_x_3": "Retenção",
    # Nao Respira (MB p.74)
    "mod_guelras": "Não Respira",
    "mod_absorcao_oxigenio": "Não Respira",
    "mod_combustao_oxigenio": "Não Respira",
    "mod_reserva_oxigenio": "Não Respira",
    # Leitura da Mente (MB p.69)
    "mod_somente_cibernetica": "Leitura da Mente",
    "mod_somente_sensorial": "Leitura da Mente",
    "mod_telecomunicacao": "Leitura da Mente",
    "mod_telepatico": "Leitura da Mente",
    "mod_racial": "Leitura da Mente",
    # Telecomunicacao (MB p.93-94)
    "mod_ondas_curtas": "Telecomunicação",
    "mod_transmissao_aberta": "Telecomunicação",
    "mod_universal": "Telecomunicação",
    # Golpeadores (MB p.62-63)
    "mod_incapaz_de_aparar": "Golpeadores",
    "mod_fraco": "Golpeadores",
    "mod_arco_limitado": "Golpeadores",
    "mod_desajeitado": "Golpeadores",
    # Deslocamento Ampliado / veiculos (MB p.53)
    "mod_bonus_manuseio": "Deslocamento Ampliado",
    "mod_redutor_manuseio": "Deslocamento Ampliado",
    "mod_limitado_a_estradas": "Deslocamento Ampliado",
    "mod_newtoniana": "Deslocamento Ampliado",
    # Bracos Adicionais (MB p.46)
    "mod_comprido": "Braços Adicionais",
    # Invisibilidade (MB p.68-69). ATENCAO: a linearizacao das colunas do livro
    # jogou estes modificadores sob o heading '## Leitura da Mente' na p69 --
    # o dono correto se confirma pelo texto ("o personagem e invisivel para
    # mais de um tipo de visao", "a invisibilidade do personagem o oculta...").
    "mod_afeta_maquinas_vantagem": "Invisibilidade",
    "mod_ativada_ou_desativada": "Invisibilidade",
    "mod_Estendida": "Invisibilidade",
    "mod_somente_materia": "Invisibilidade",
    "mod_normalmente_ativa_ampliação": "Invisibilidade",
    # Insubstancialidade (MB p.66-67). O bloco de modificadores da p67 pertence
    # a ela (ultima vantagem da p66). "carregar nada enquanto se move ATRAVES DA
    # MATERIA" e "capaz de se MATERIALIZAR" nao sao de Invisibilidade.
    "mod_capaz_de_carregar_objetos": "Insubstancialidade",
    "mod_normalmente_ativa_limitação": "Insubstancialidade",
    # Neutralizar (MB p.74)
    "mod_furto_de_poder": "Neutralizar",
    "mod_poder_unico": "Neutralizar",
    # Desvantagens
    "mod_somente_fadiga_desvantagem_fraqueza": "Fraqueza",
    "mod_variavel_desvantagem_fraqueza": "Fraqueza",
    "mod_somente_fadiga_vantagem_vulnerabilidade": "Vulnerabilidade",
    "mod_furia_em_combate": "Fúria",
    "mod_nao_pode_ser_preso": "Aversão",
    "mod_substituicao": "Dieta Restrita",
}

# Ficam SEM dono de proposito -- nao pertencem a uma vantagem so:
#  - 'Limitacoes de Instrumentos' (MB p.117-118): Fraco/Fragil/Pode Ser Roubado/
#    Unico valem para qualquer vantagem encarnada num gadget;
#  - 'Enfermidade': sem bloco proprio no livro.
SEM_DONO_PROPOSITAL = {
    "mod_fragil", "mod_pode_ser_roubado", "mod_único",
    # Enfermidade sai do box "Contagio" (p105), que estende o modificador
    # Ciclico -- e Ciclico nao e uma vantagem, entao nao ha dono a apontar.
    "mod_enfermidade_leve", "mod_enfermidade_alta",
}


def norm(s):
    s = unicodedata.normalize("NFD", s or "").encode("ascii", "ignore").decode().lower()
    return " ".join(re.sub(r"[^a-z0-9]+", " ", s).split())


def indice_de_tracos():
    """nome normalizado -> id, varrendo vantagens e desvantagens."""
    idx = {}
    for arq in ("vantagens.v3.json", "desvantagens.v2.json"):
        with open(os.path.join(ASSETS, arq), encoding="utf-8") as f:
            for x in json.load(f):
                n = norm(x.get("nome"))
                if n and n not in idx:
                    idx[n] = x.get("id")
    return idx


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--aplicar", action="store_true")
    args = ap.parse_args()

    idx = indice_de_tracos()
    with open(ALVO, encoding="utf-8") as f:
        mods = json.load(f)

    ids_existentes = {m["id"] for m in mods}
    erros = []

    # Validacao 1: todo id do mapa precisa existir no catalogo.
    for mid in DONOS:
        if mid not in ids_existentes:
            erros.append(f"id de modificador inexistente no catalogo: {mid}")

    # Validacao 2: todo dono precisa resolver para uma vantagem/desvantagem real.
    donos_id = {}
    for mid, dono in DONOS.items():
        rid = idx.get(norm(dono))
        if not rid:
            erros.append(f"dono nao encontrado nos catalogos: {dono!r} (de {mid})")
        else:
            donos_id[mid] = rid

    marcados = 0
    for m in mods:
        if m["id"] in donos_id:
            if args.aplicar:
                m["donoId"] = donos_id[m["id"]]
            marcados += 1

    # Validacao 3: os presos que ficaram sem dono sao apenas os intencionais.
    presos_sem_dono = [
        m["id"] for m in mods
        if not (101 <= m["pagina"] <= 116)
        and m["id"] not in donos_id
        and m["id"] not in SEM_DONO_PROPOSITAL
    ]

    print(f"modificadores no catalogo: {len(mods)}")
    print(f"marcados com donoId:       {marcados}")
    print(f"sem dono de proposito:     {len(SEM_DONO_PROPOSITAL)}")
    if presos_sem_dono:
        print(f"AVISO: presos a uma vantagem mas sem dono atribuido ({len(presos_sem_dono)}):")
        for i in sorted(set(presos_sem_dono)):
            print(f"   - {i}")
    if erros:
        print(f"\nERROS ({len(erros)}):")
        for e in erros:
            print(f"   ! {e}")
        return 1

    if args.aplicar:
        with open(ALVO, "w", encoding="utf-8") as f:
            json.dump(mods, f, ensure_ascii=False, indent=2)
        print(f"\nAPLICADO: {marcados} modificadores marcados em {ALVO}")
    else:
        print("\n(dry-run -- nada alterado; use --aplicar)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
