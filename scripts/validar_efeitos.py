# -*- coding: utf-8 -*-
"""
Valida o campo `efeitos` de vantagens.v3.json e desvantagens.v2.json.

Existe porque erro de digitacao em JSON NAO e pego pelo compilador Kotlin:
escrever "Escalda" em vez de "Escalada" gera um bonus que simplesmente nunca
aplica -- e ninguem percebe, porque nao ha erro em lugar nenhum.

Checagens:
  1. `tipo` dentro do enum (pericia / defesa / atributo);
  2. `alvo` de pericia com correspondente EXATO em pericias.json
     (inclusive especializacao: "Navegacao" nao casa com "Navegacao (Ar)");
  3. `alvo` de defesa dentro de esquiva / aparar / bloqueio;
  4. `valor` inteiro;
  5. conflito: traco com `efeitos` E classe Kotlin registrada (a Kotlin vence,
     entao o JSON seria ignorado silenciosamente).

Uso:
    python scripts/validar_efeitos.py
Sai com codigo 1 se achar qualquer problema.
"""
import json
import os
import re
import sys

AQUI = os.path.dirname(os.path.abspath(__file__))
RAIZ = os.path.dirname(AQUI)
ASSETS = os.path.join(RAIZ, "app", "src", "main", "assets")
REGISTRY = os.path.join(
    RAIZ, "app", "src", "main", "java", "com", "gurps", "ficha",
    "domain", "rules", "traits", "TraitRuleRegistry.kt"
)

TIPOS_VALIDOS = {"pericia", "perícia", "defesa", "atributo"}
ALVOS_DEFESA = {"esquiva", "aparar", "apara", "bloqueio", "bloquear"}
ATRIBUTOS_VALIDOS = {"ST", "DX", "IQ", "HT", "VONT", "PER", "PV", "PF", "VEL", "DESL"}


def carregar(nome):
    with open(os.path.join(ASSETS, nome), encoding="utf-8") as f:
        return json.load(f)


def nomes_de_pericia():
    """Nomes exatos, incluindo os dos arquivos suplementares.

    Os catalogos nao sao homogeneos: ha entradas que sao string solta no meio
    da lista de objetos, e o suplementar pode vir embrulhado num dict. Ignora
    o que nao souber ler em vez de quebrar -- o objetivo aqui e validar
    `efeitos`, nao a saude dos catalogos de pericia.
    """
    nomes = set()
    for arq in ("pericias.json", "pericias_artes_marciais.v1.json"):
        caminho = os.path.join(ASSETS, arq)
        if not os.path.exists(caminho):
            continue
        dados = json.load(open(caminho, encoding="utf-8"))
        if isinstance(dados, dict):
            dados = next((v for v in dados.values() if isinstance(v, list)), [])
        for p in dados:
            if isinstance(p, dict) and p.get("nome"):
                nomes.add(p["nome"])
            elif isinstance(p, str) and p.strip():
                nomes.add(p.strip())
    return nomes


def ids_com_regra_kotlin():
    """Ids registrados no TraitRuleRegistry.init -- a classe Kotlin vence o JSON.

    Varre a PASTA inteira em vez de supor um arquivo por classe: `StBracalRule`
    e `DxBracalRule` moram juntas em `BracalCustoRules.kt`, e o mapeamento
    classe->arquivo as perdia em silencio -- justamente o tipo de falha
    invisivel que este validador existe para evitar.

    Aceita as duas formas de declarar o id:
        override val traitId: String = "pendulear"
        override val traitId: String = ID   (com `const val ID = "st_bracal"`)
    """
    if not os.path.exists(REGISTRY):
        return set()
    classes = set(re.findall(r"register\((\w+)\(\)\)", open(REGISTRY, encoding="utf-8").read()))
    if not classes:
        return set()

    pasta = os.path.dirname(REGISTRY)
    ids = set()
    for nome in os.listdir(pasta):
        if not nome.endswith(".kt"):
            continue
        fonte = open(os.path.join(pasta, nome), encoding="utf-8").read()
        # Corta o arquivo em blocos por declaracao de classe, para nao atribuir
        # o traitId de uma classe a outra que more no mesmo arquivo.
        partes = re.split(r"\n(?=(?:open |abstract )?class\s+\w+)", fonte)
        for parte in partes:
            m_classe = re.search(r"class\s+(\w+)", parte)
            if not m_classe or m_classe.group(1) not in classes:
                continue
            m = re.search(r'traitId[^=]*=\s*"([^"]+)"', parte)
            if m:
                ids.add(m.group(1))
                continue
            # traitId = ID -> procurar a constante no mesmo bloco
            if re.search(r"traitId[^=]*=\s*ID\b", parte):
                m_const = re.search(r'const val ID\s*=\s*"([^"]+)"', parte)
                if m_const:
                    ids.add(m_const.group(1))
    return ids


def main():
    pericias = nomes_de_pericia()
    kotlin = ids_com_regra_kotlin()
    erros = []
    total_efeitos = 0
    tracos_com_efeitos = 0

    for arquivo in ("vantagens.v3.json", "desvantagens.v2.json"):
        for traco in carregar(arquivo):
            efeitos = traco.get("efeitos") or []
            if not efeitos:
                continue
            tracos_com_efeitos += 1
            tid = traco.get("id", "?")
            nome = traco.get("nome") or tid

            if tid in kotlin:
                erros.append(
                    f"{nome} [{tid}]: tem `efeitos` no JSON E classe Kotlin registrada "
                    f"-- a Kotlin vence, o JSON seria ignorado em silencio"
                )

            for i, ef in enumerate(efeitos):
                total_efeitos += 1
                onde = f"{nome} [{tid}] efeito #{i + 1}"

                tipo = str(ef.get("tipo", "")).strip().lower()
                if tipo not in TIPOS_VALIDOS:
                    erros.append(f"{onde}: tipo {ef.get('tipo')!r} invalido")
                    continue

                alvo = str(ef.get("alvo", "")).strip()
                if not alvo:
                    erros.append(f"{onde}: sem `alvo`")
                    continue

                if not isinstance(ef.get("valor"), int):
                    erros.append(f"{onde}: `valor` precisa ser inteiro, veio {ef.get('valor')!r}")

                if tipo.startswith("per"):
                    # "reacao" nao e pericia do catalogo: e o alvo reservado
                    # para modificador de Teste de Reacao (ReacaoRules).
                    if alvo == "reacao":
                        pass
                    elif alvo not in pericias:
                        sugestao = [p for p in pericias if p.lower().startswith(alvo.lower()[:5])][:3]
                        extra = f" (parecidas: {sugestao})" if sugestao else ""
                        erros.append(f"{onde}: pericia {alvo!r} nao existe no catalogo{extra}")
                elif tipo == "defesa":
                    if alvo.lower() not in ALVOS_DEFESA:
                        erros.append(f"{onde}: alvo de defesa {alvo!r} invalido")
                elif tipo == "atributo":
                    if alvo.upper() not in ATRIBUTOS_VALIDOS:
                        erros.append(f"{onde}: atributo {alvo!r} invalido")

    print(f"tracos com `efeitos`: {tracos_com_efeitos}")
    print(f"efeitos declarados:   {total_efeitos}")
    print(f"pericias no catalogo: {len(pericias)}")
    print(f"regras Kotlin ativas: {len(kotlin)}")

    if erros:
        print(f"\nERROS ({len(erros)}):")
        for e in erros:
            print(f"   ! {e}")
        return 1
    print("\nOK: nenhum problema encontrado.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
