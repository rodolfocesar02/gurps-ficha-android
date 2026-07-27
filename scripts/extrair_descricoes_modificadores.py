# -*- coding: utf-8 -*-
"""
Extrai as descricoes INTEGRAIS dos modificadores do Modulo Basico (chunks.jsonl)
para dentro de modificadores.v1.json.

Motivacao: 183 das 218 descricoes do JSON estao resumidas/abreviadas. O texto
completo ja existe no proprio projeto (assets/chunks.jsonl, 1 chunk por pagina),
entao a tarefa e RECORTAR verbatim, nunca reescrever.

O livro usa DOIS formatos para modificador, e o extrator trata os dois:

  1) '### Nome'    -> modificadores GERAIS (MB p.101-116). O bloco vai ate o
                      proximo heading de mesmo nivel ou superior.
  2) '**Nome:**'   -> modificadores ESPECIAIS de uma vantagem (ex.: Absorcao,
                      sob '### Ampliacoes Especiais' da Resistencia a Dano).
                      O bloco vai ate o proximo rotulo em negrito que comece
                      uma linha, ou ate o proximo heading.

Multinivel: 120 das 218 entradas sao niveis de 33 conceitos-base (Ciclico tem 5,
Inconstante 9...). O livro descreve o conceito UMA vez, explicando cada nivel
dentro do bloco. Por decisao do usuario, TODAS as entradas do grupo recebem o
bloco integral -- cada uma fica autossuficiente e 100% fiel.

Uso:
    python scripts/extrair_descricoes_modificadores.py            # gera revisao
    python scripts/extrair_descricoes_modificadores.py --aplicar  # grava o JSON

Sem --aplicar nada e alterado: so escreve o relatorio de revisao.
"""
import argparse
import json
import os
import re
import sys
import unicodedata

AQUI = os.path.dirname(os.path.abspath(__file__))
RAIZ = os.path.dirname(AQUI)
ASSETS = os.path.join(RAIZ, "app", "src", "main", "assets")
CHUNKS = os.path.join(ASSETS, "chunks.jsonl")
ALVO = os.path.join(ASSETS, "modificadores.v1.json")

FONTE_MB = "pt_modulo_basico"


def norm(s):
    """Normaliza para comparar nomes, PRESERVANDO fronteira de palavra.

    As palavras viram tokens separados por espaco porque a comparacao por
    continencia precisa casar palavra inteira. Sem isso, 'Pacto' casaria dentro
    de 'Trauma por Impacto' e o modificador herdaria a descricao errada -- erro
    real que aconteceu na primeira versao.
    """
    s = unicodedata.normalize("NFD", s).encode("ascii", "ignore").decode().lower()
    return " ".join(re.sub(r"[^a-z0-9]+", " ", s).split())


def contido(menor, maior):
    """True se `menor` aparece em `maior` como sequencia de palavras inteiras."""
    return f" {menor} " in f" {maior} "


def base_do_nome(nome):
    """Reduz a entrada ao CONCEITO do livro, que descreve o grupo inteiro.

    'Ciclico (1 segundo)'        -> 'Ciclico'
    'Efeito de Dano: Afogamento' -> 'Efeito de Dano'
    'Cosmica: Contramedida'      -> 'Cosmica'

    O corte no ':' importa: o livro trata essas familias num bloco unico que
    lista as variantes na propria frase ('Afogamento, +0%; Congelamento, +20%...').
    """
    return re.split(r"[(:]", nome)[0].strip()


# Casos que a busca automatica nao resolve, resolvidos na mao contra o livro.
# Motivo de cada um: ou a `pagina` do JSON esta errada, ou o livro rotula o
# bloco com outro nome. Chave = base do nome no JSON.
#   valor = (nome_do_bloco_no_livro, pagina_real)
ALIASES = {
    # O JSON explode as variantes de dano por fadiga em 6 entradas; o livro as
    # descreve juntas no bloco 'Variavel' ("Afogamento, +0%; Congelamento, +20%...").
    "Efeito de Dano": ("Variável", 109),
    # Sub-item do bloco 'Ataque Corpo a Corpo'.
    "Ataque CC": ("Ataque Corpo a Corpo", 112),
    # Paginas erradas no JSON (135 -> 125, 113 -> 63).
    "Não Pode ser Preso": ("Não Pode ser Preso", 125),
    "Incapaz de Aparar": ("Incapaz de Aparar", 63),
    # As duas 'Enfermidade' saem do box de Contagio (p105), extensao do Ciclico:
    # "+20% no caso de um ataque levemente contagioso ou +50% para um altamente
    # contagioso". O JSON aponta p43, onde nao ha nada disso.
    "Enfermidade": ("Box Lateral: Contágio", 105),
    # 'Fraco' existe em duas vantagens (Braco p46, Golpeador p63). A entrada do
    # catalogo e a do Golpeador -- confirmado pela propria descricao ("O
    # Golpeador do personagem e muito rombudo"). A p117 do JSON e engano: la so
    # existem Fragil / Pode Ser Roubado / Unico (Limitacoes de Instrumentos).
    "Fraco": ("Fraco", 63),
}

# Conceitos que NAO tem bloco proprio no texto do livro disponivel: aparecem so
# em lista/indice remissivo. Ficam com a descricao antiga e sao reportados para
# decisao do usuario -- inventar texto aqui quebraria a fidelidade pedida.
SEM_BLOCO_NO_LIVRO = {
    "Recuo Adicional",   # so citado em listas (p103) e no indice (p302)
    "Perigo",            # nenhum bloco de modificador com esse nome
}


def carregar_paginas():
    """page_number -> texto, apenas do Modulo Basico."""
    paginas = {}
    with open(CHUNKS, encoding="utf-8") as f:
        for linha in f:
            o = json.loads(linha)
            if o.get("source_id") == FONTE_MB:
                paginas[o["page_number"]] = o["text"]
    return paginas


# --- Estrategia 1: heading '### Nome' -------------------------------------

# 1 a 4 '#': a diagramacao nao e consistente -- 'Requer Preparo' vem como '#',
# a maioria como '###'. Titulo de pagina ('# Pagina 116 - ...') tambem casa aqui,
# mas nunca bate com nome de modificador, entao e filtrado no casamento.
RE_HEADING = re.compile(r"^(#{1,4})\s+(.+?)\s*$", re.M)


def blocos_por_heading(texto):
    """{nome_normalizado: (nome_original, corpo)} para cada heading da pagina."""
    achados = list(RE_HEADING.finditer(texto))
    blocos = {}
    for i, m in enumerate(achados):
        nivel = len(m.group(1))
        nome = m.group(2).strip()
        fim = len(texto)
        # Termina no proximo heading de nivel igual ou mais alto (menos '#').
        for prox in achados[i + 1:]:
            if len(prox.group(1)) <= nivel:
                fim = prox.start()
                break
        corpo = texto[m.end():fim].strip()
        if corpo:
            blocos.setdefault(norm(nome), (nome, corpo))
    return blocos


# --- Estrategia 2: rotulo '**Nome:**' -------------------------------------

# Rotulo que ABRE um modificador. O livro usa DUAS formas, e as duas comecam a
# linha:
#   '**Nome:**  texto...'   (dois-pontos dentro do negrito, texto na mesma linha)
#   '**Nome**\n texto...'   (sem dois-pontos, rotulo sozinho na linha)
# A exigencia de ':' OU fim-de-linha e o que separa um rotulo de verdade de um
# negrito de enfase no meio do paragrafo (ex.: '**+20%**', '**Nota:**' inline).
#   '| **Nome:** texto |'   (o livro tambem usa TABELA para as ampliacoes
#                            especiais de algumas vantagens -- ex.: Veiculos,
#                            p.53 -- entao o rotulo vem depois de '| ')
#   '- **Nome:** texto'     (item de lista -- p67 usa esse formato; sem aceitar
#                            o '- ' o rotulo passava batido e a busca ia parar
#                            numa pagina vizinha, trazendo o texto de OUTRO
#                            modificador de mesmo nome. Foi o que aconteceu com
#                            'Normalmente Ativa', que existe em duas vantagens.)
RE_ROTULO = re.compile(r"^[-*]?\s*\|?\s*\*\*([^*\n]{2,70}?):?\*\*(?=[ \t]*$|[ \t]|\s)", re.M)


def blocos_por_rotulo(texto):
    """{nome_normalizado: (nome_original, corpo)} para rotulos '**Nome:**'."""
    achados = list(RE_ROTULO.finditer(texto))
    blocos = {}
    for i, m in enumerate(achados):
        nome = m.group(1).strip()
        fim = achados[i + 1].start() if i + 1 < len(achados) else len(texto)
        # Um heading tambem encerra o bloco.
        h = RE_HEADING.search(texto, m.end())
        if h and h.start() < fim:
            fim = h.start()
        corpo = texto[m.start():fim].strip()
        if corpo:
            blocos.setdefault(norm(nome), (nome, corpo))
    return blocos


def casar(blocos, alvo_norm):
    """Casamento exato; depois por continencia de PALAVRAS INTEIRAS.

    A continencia serve para dois casos legitimos:
      - o livro rotula 'Guiado ou Teleguiado' e o JSON tem 'Guiado';
      - o JSON tem 'Cosmica: Contramedida' e o livro so 'Cosmica'.
    """
    if alvo_norm in blocos:
        return blocos[alvo_norm], "exato"
    candidatos = [
        (k, v) for k, v in blocos.items()
        if len(k) >= 4 and (contido(alvo_norm, k) or contido(k, alvo_norm))
    ]
    if len(candidatos) == 1:
        return candidatos[0][1], "parcial"
    if len(candidatos) > 1:
        # Prefere o rotulo mais parecido em tamanho -- evita pegar bloco vizinho.
        melhor = min(candidatos, key=lambda kv: abs(len(kv[0]) - len(alvo_norm)))
        return melhor[1], "ambiguo"
    return None, "nao_achou"


def buscar_bloco(paginas, pagina, nome):
    """Procura o bloco do modificador na pagina indicada e nas vizinhas.

    Vizinhas importam porque o texto do livro atravessa a virada de pagina e a
    pagina anotada no JSON as vezes aponta o inicio da vantagem, nao do
    modificador.
    """
    if nome in SEM_BLOCO_NO_LIVRO:
        return None
    if nome in ALIASES:
        nome, pagina = ALIASES[nome]

    alvo = norm(nome)
    # A pagina anotada no JSON erra com frequencia (as vezes aponta o inicio da
    # vantagem, nao do modificador; as vezes ha desalinhamento de 1-3 paginas).
    # A ordem importa: comeca na pagina anotada e vai abrindo o cerco, para o
    # bloco mais proximo ganhar de um homonimo distante.
    for delta in (0, 1, -1, 2, 3, -2):
        pg = pagina + delta
        texto = paginas.get(pg)
        if not texto:
            continue
        for extrator, tag in ((blocos_por_heading, "heading"), (blocos_por_rotulo, "rotulo")):
            achado, confianca = casar(extrator(texto), alvo)
            if achado:
                nome_livro, corpo = achado
                return {
                    "texto": corpo,
                    "pagina_usada": pg,
                    "formato": tag,
                    "confianca": confianca,
                    "nome_no_livro": nome_livro,
                    "deslocamento": delta,
                }
    return None


def limpar(texto):
    """Remove a MARCACAO markdown, preservando todas as PALAVRAS do livro.

    O dialog do app (CatalogoDescricaoDialog) usa Text() puro, sem renderizar
    markdown -- entao '**Permeavel:**' apareceria com os asteriscos na tela.
    Aqui some so o simbolo: '**Permeavel:** texto' -> 'Permeavel: texto'.
    Nenhuma palavra e alterada, so a pontuacao de formatacao.
    """
    # Rodape/cabecalho de pagina que possa ter entrado no recorte.
    texto = re.sub(r"^#\s*(P[aá]g\.?|P[aá]gina)\s*\d+.*$", "", texto, flags=re.M)
    # Headings viram linha simples.
    texto = re.sub(r"^#{1,6}\s*", "", texto, flags=re.M)
    # Separador horizontal do markdown.
    texto = re.sub(r"^\s*-{3,}\s*$", "", texto, flags=re.M)
    # Negrito e italico: fica so o conteudo.
    texto = re.sub(r"\*\*(.+?)\*\*", r"\1", texto, flags=re.S)
    texto = re.sub(r"(?<!\w)\*(?!\s)(.+?)(?<!\s)\*(?!\w)", r"\1", texto, flags=re.S)
    # Tabela: '| a | b |' -> 'a | b' (mantem as colunas, tira a moldura).
    linhas = []
    for ln in texto.split("\n"):
        if re.match(r"^\s*\|[\s|:-]*\|\s*$", ln):
            continue  # linha separadora '|---|---|'
        if ln.strip().startswith("|"):
            ln = " | ".join(c.strip() for c in ln.strip().strip("|").split("|") if c.strip())
        linhas.append(ln)
    texto = "\n".join(linhas)
    # Espacos de sobra deixados pela remocao.
    texto = re.sub(r"[ \t]{2,}", " ", texto)
    texto = re.sub(r"[ \t]+$", "", texto, flags=re.M)
    texto = re.sub(r"\n{3,}", "\n\n", texto)
    return texto.strip()


def descricoes_editadas_sem_commit():
    """{(id, valor)} cujas descricoes diferem do commitado no git.

    Sao alteracoes feitas a mao que ainda nao entraram num commit -- tipicamente
    de outra sessao trabalhando na mesma arvore. Devolve conjunto vazio se o git
    nao responder (repo ausente, arquivo novo): sem base de comparacao, nada e
    marcado como protegido.
    """
    import subprocess
    caminho_git = "app/src/main/assets/modificadores.v1.json"
    try:
        r = subprocess.run(["git", "show", f"HEAD:{caminho_git}"],
                           capture_output=True, cwd=RAIZ)
        if r.returncode != 0:
            return set()
        antigos = json.loads(r.stdout.decode("utf-8"))
    except Exception:
        return set()

    with open(ALVO, encoding="utf-8") as f:
        atuais = json.load(f)

    # (id, valor) NAO e unico -- 'Comprido +100%' aparece duas vezes. Sem o
    # contador de repeticao as duas colidiriam e seriam marcadas como editadas
    # sem terem sido, ficando presas com a descricao velha.
    def indexar(lista):
        vistos = {}
        saida = {}
        for x in lista:
            base = (x["id"], x.get("valor"))
            n = vistos.get(base, 0)
            vistos[base] = n + 1
            saida[base + (n,)] = x.get("descricao") or ""
        return saida

    antes = indexar(antigos)
    agora = indexar(atuais)
    # Entrada nova (ausente no HEAD) tambem e trabalho nao commitado.
    return {k for k, desc in agora.items() if antes.get(k) != desc}


def tirar_nome_repetido(texto, nome_no_livro):
    """Remove o nome do modificador repetido no inicio da descricao.

    O recorte comeca no proprio rotulo ('Pele Resistente: Via de regra...'), mas
    o dialog ja exibe o nome como titulo -- repetir polui a leitura. So remove
    quando bate com o nome do bloco; nunca corta texto de conteudo.
    """
    alvo = norm(nome_no_livro)
    if not alvo:
        return texto
    # 'Nome: resto' na mesma linha.
    m = re.match(r"\s*([^\n:]{2,70}?)\s*:\s*(?=\S)", texto)
    if m and norm(m.group(1)) == alvo:
        return texto[m.end():].strip()
    # 'Nome' sozinho na primeira linha.
    primeira, _, resto = texto.partition("\n")
    if norm(primeira) == alvo and resto.strip():
        return resto.strip()
    return texto


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--aplicar", action="store_true",
                    help="grava as descricoes no JSON (sem isso, so relata)")
    ap.add_argument("--saida", default=None, help="caminho do relatorio de revisao")
    ap.add_argument("--sobrescrever-pendentes", action="store_true",
                    help="tambem grava por cima do que ja foi editado a mao e nao "
                         "esta commitado (por padrao esse trabalho e preservado)")
    ap.add_argument("--forcar-ids", default="",
                    help="lista separada por virgula de ids que devem ser gravados "
                         "mesmo estando protegidos. Use para corrigir uma entrada "
                         "especifica sem liberar o arquivo inteiro.")
    args = ap.parse_args()
    forcados = {i.strip() for i in args.forcar_ids.split(",") if i.strip()}

    # O projeto e editado por varias sessoes na MESMA arvore. Se uma delas ja
    # reescreveu uma descricao a mao e ainda nao commitou, sobrescrever aqui
    # apagaria esse trabalho sem aviso. Entao a diferenca contra o HEAD do git
    # vira uma lista de intocaveis.
    protegidos = descricoes_editadas_sem_commit()
    if protegidos and not args.sobrescrever_pendentes:
        print(f"preservando {len(protegidos)} descricao(oes) editada(s) a mao e nao commitada(s)")

    paginas = carregar_paginas()
    with open(ALVO, encoding="utf-8") as f:
        mods = json.load(f)

    # Uma busca por CONCEITO-BASE; todos os niveis do grupo herdam o bloco.
    cache = {}
    for m in mods:
        b = base_do_nome(m["nome"])
        chave = (b, m["pagina"])
        if chave not in cache:
            cache[chave] = buscar_bloco(paginas, m["pagina"], b)

    relatorio = []
    aplicados = 0
    # Mesmo esquema de chave do descricoes_editadas_sem_commit: (id, valor, n).
    vistos = {}
    for m in mods:
        _base = (m["id"], m.get("valor"))
        _n = vistos.get(_base, 0)
        vistos[_base] = _n + 1
        chave_protecao = _base + (_n,)
        chave = (base_do_nome(m["nome"]), m["pagina"])
        achado = cache[chave]
        antes = m.get("descricao") or ""
        item = {
            "id": m["id"],
            "nome": m["nome"],
            "valor": m.get("valor"),
            "pagina": m["pagina"],
            "desc_antes": antes,
            "tam_antes": len(antes),
        }
        protegido = (chave_protecao in protegidos
                     and not args.sobrescrever_pendentes
                     and m["id"] not in forcados)
        if achado:
            novo = limpar(achado["texto"])
            novo = tirar_nome_repetido(novo, achado["nome_no_livro"])
            item.update({
                "desc_depois": novo,
                "tam_depois": len(novo),
                "pagina_usada": achado["pagina_usada"],
                "formato": achado["formato"],
                "confianca": "PROTEGIDO" if protegido else achado["confianca"],
                "nome_no_livro": achado["nome_no_livro"],
                "protegido": protegido,
            })
            if args.aplicar and not protegido:
                m["descricao"] = novo
                aplicados += 1
        else:
            item.update({"desc_depois": None, "confianca": "NAO_ACHADO"})
        relatorio.append(item)

    # --- resumo ---
    from collections import Counter
    conf = Counter(r["confianca"] for r in relatorio)
    print("total de modificadores:", len(mods))
    for k, v in conf.most_common():
        print(f"  {k}: {v}")
    achados = [r for r in relatorio if r.get("desc_depois")]
    if achados:
        cresceu = sum(1 for r in achados if r["tam_depois"] > r["tam_antes"])
        encolheu = sum(1 for r in achados if r["tam_depois"] < r["tam_antes"])
        print(f"  -> texto cresceu em {cresceu}, encolheu em {encolheu}")

    saida = args.saida or os.path.join(AQUI, "_revisao_modificadores.json")
    with open(saida, "w", encoding="utf-8") as f:
        json.dump(relatorio, f, ensure_ascii=False, indent=2)
    print("relatorio de revisao:", saida)

    if args.aplicar:
        with open(ALVO, "w", encoding="utf-8") as f:
            json.dump(mods, f, ensure_ascii=False, indent=2)
        print(f"APLICADO: {aplicados} descricoes gravadas em {ALVO}")
    else:
        print("(dry-run -- nada foi alterado; use --aplicar para gravar)")


if __name__ == "__main__":
    main()
