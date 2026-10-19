#!/usr/bin/env python3
"""
MC-7 — reextrai páginas do PDF de Magia (a CAMADA DE TEXTO do PDF) para o formato markdown do
`chunks.jsonl` (`source_id: pt_magia`). Ninguém redigita o livro: o texto sai do PDF por programa; este
script só decide a ordem das colunas, junta linhas em parágrafos e marca o que o localizador lê
(`### Nome`, `**Classe**`, `**Duração:** ...`, `**Item**`).

    python -I scripts/magias/pdf-para-chunks.py --pdf "GURPS 4ª Edição - Magia.pdf" --paginas 26,37,185
    python -I scripts/magias/pdf-para-chunks.py --pdf ... --paginas 26,37,185 --aplicar

Sem `--aplicar` é um ensaio: mostra, página a página, o que mudaria (tamanho, números que saem e entram).
Com `--aplicar` troca SÓ o campo `text` dessas páginas no `chunks.jsonl`; o resto do arquivo fica byte a byte
como está (CRLF e a mesma serialização). Depois: rodar a cadeia dos scripts do MC (ver LEIA-ME.md) e subir
`CODEX_VERSION_CURRENT`.

Por que existe: a extração antiga (por IA, a partir de imagens) embaralhou as páginas de colunas e cortou
outras (a p.185 tinha 739 caracteres). O PDF tem camada de texto (InDesign): é só lê-la na ordem certa.

Páginas do PDF: `indice = impressa + 1` (a capa é o índice 0); o script confere pelo número da página impressa
(o texto branco na aba do capítulo) e para se não bater.
"""
import argparse
import json
import re
import sys

import pymupdf

BRANCO = 0xFFFFFF
PRETO = 0x000000
# Os rótulos "Duração:", "Custo:"... e o valor deles são coloridos, e a cor muda de capítulo para capítulo
# (azul na Água, outro azul no Ar...): o que vale é "itálico, colorido e com a palavra do rótulo".
ROTULOS = re.compile(r"^(Duração|Custo|Custo básico|Tempo de operação|Pré-requisitos?)$", re.I)
FONTE_TITULO = "BoldIt"
LIMIAR_TITULO = 13.0
ROTULOS_DE_CLASSE = ("SemiBold",)

# Duas regras que ajudam algumas páginas e ATRAPALHAM outras (o ensaio de 19/out mudou 46 e 10 páginas, algumas para
# pior). Por isso só valem quando pedidas, página a página: `--fundir-bordas` e `--quadro-largo`.
FUNDIR_BORDAS = False
QUADRO_LARGO = False

SEPARADOR_DE_COLUNA = 40.0  # pontos: dois grupos de x0 mais longe que isto são colunas diferentes
LACUNA_DE_PARAGRAFO = 2.5   # pontos entre o fim de uma linha e o começo da próxima


def eh_branco(span):
    return span["color"] == BRANCO


def texto_do_span(span):
    return span["text"]


def classificar(linha):
    """'ignorar' | 'titulo' | 'classe' | 'item' | 'rotulo' | 'continua-rotulo' | 'corpo'."""
    spans = [s for s in linha["spans"] if s["text"].strip() != ""]
    if not spans:
        return "ignorar"
    if all(eh_branco(s) for s in spans):
        return "ignorar"  # aba do capítulo e número da página
    if all("-Black" in s["font"] for s in spans):
        return "ignorar"  # título decorativo da abertura do capítulo (letras enormes)
    # citação em destaque (itálico grande) e a assinatura dela
    if any(s["font"].endswith("-It") and s["size"] >= LIMIAR_TITULO for s in spans):
        return "ignorar"
    if all(s["font"].endswith(FONTE_TITULO) and s["size"] >= LIMIAR_TITULO for s in spans):
        return "titulo"
    if all(any(s["font"].endswith(c) for c in ROTULOS_DE_CLASSE) and 10.0 <= s["size"] <= 12.0 for s in spans):
        return "classe"
    texto = "".join(s["text"] for s in spans).strip()
    if all(s["font"].endswith("-Bold") and 9.5 <= s["size"] <= 11.0 for s in spans) and texto in ("Item", "Itens"):
        return "item"
    primeiro = spans[0]
    colorido = primeiro["color"] not in (PRETO, BRANCO)
    # um rótulo é um trecho em itálico e colorido no começo da linha ("Duração", "Custo", "Modificadores", "Exemplos")
    seguinte = next((x["text"] for x in spans[1:]), "")
    if colorido and primeiro["font"].endswith("-It") and (
            ROTULOS.match(primeiro["text"].strip().rstrip(":").strip())
            or seguinte.lstrip().startswith(":") or primeiro["text"].rstrip().endswith(":")):
        return "rotulo"
    if colorido:
        return "continua-rotulo"
    return "corpo"


def eh_inicio_de_rotulo(span):
    # no MEIO de uma linha só vale a palavra de rótulo conhecida (itálico colorido solto pode ser ênfase)
    return (span["color"] not in (PRETO, BRANCO) and span["font"].endswith("-It")
            and ROTULOS.match(span["text"].strip().rstrip(":").strip()) is not None)


def dividir_nos_rotulos(linha):
    """Uma linha com um rótulo no MEIO vira duas: o rótulo começa um campo novo."""
    spans = linha["spans"]
    cortes = [i for i, s in enumerate(spans) if i > 0 and eh_inicio_de_rotulo(s)
              and "".join(x["text"] for x in spans[:i]).strip() != ""]
    if not cortes:
        return [linha]
    pedacos, ini = [], 0
    for c in cortes + [len(spans)]:
        pedacos.append(dict(linha, spans=spans[ini:c]))
        ini = c
    return pedacos


def linhas_da_pagina(pagina):
    saida = []
    for n, bloco in enumerate(pagina.get_text("dict")["blocks"]):
        if bloco["type"] != 0:
            continue
        for l in bloco["lines"]:
            x0, y0, x1, y1 = l["bbox"]
            saida.append({"x0": x0, "y0": y0, "x1": x1, "y1": y1, "spans": l["spans"], "bloco": n})
    dividido = []
    for l in saida:
        dividido.extend(dividir_nos_rotulos(l))
    for l in dividido:
        l["tipo"] = classificar(l)
    return [l for l in dividido if l["tipo"] != "ignorar"]


def bordas_das_colunas(linhas):
    """As margens esquerdas das colunas: os x0 que várias linhas LONGAS compartilham.
    Linhas curtas (texto que contorna uma figura, células de tabela) não abrem coluna."""
    xs = sorted(l["x0"] for l in linhas if (l["x1"] - l["x0"]) >= 100)
    grupos = []  # [min_x0, quantidade]
    for x in xs:
        if grupos and x - grupos[-1][0] < 12.0:
            grupos[-1][1] += 1
        else:
            grupos.append([x, 1])
    if not grupos:
        return [0.0]
    maior = max(g[1] for g in grupos)
    bordas = [g[0] for g in grupos if g[1] >= max(4, 0.12 * maior)]
    # (`--fundir-bordas`) Duas margens a menos de SEPARADOR_DE_COLUNA uma da outra são a MESMA coluna: o recuo do 1º parágrafo ou o texto
    # que contorna uma figura começa mais à direita. Vale a mais à esquerda (a margem de verdade). Sem isto, a p.142
    # (abertura de capítulo) partia o Apressar em duas colunas e perdia o fim da linha do Custo.
    if not FUNDIR_BORDAS:
        return bordas or [grupos[0][0]]
    fundidas = []
    for b in bordas:
        if fundidas and b - fundidas[-1] < SEPARADOR_DE_COLUNA:
            continue
        fundidas.append(b)
    return fundidas or [grupos[0][0]]


def coluna_da_linha(linha, bordas):
    if linha["tipo"] == "classe":
        # a classe vem alinhada à direita: pelo centro
        centro = (linha["x0"] + linha["x1"]) / 2.0
        ref = centro - 40.0
    else:
        ref = linha["x0"] + 12.0
    melhor = 0
    for i, b in enumerate(bordas):
        if b <= ref:
            melhor = i
    return melhor


def agrupar_tabelas(ordenadas):
    """Células lado a lado (mesma coluna, mesmo topo) viram uma linha de tabela:
    `{"tipo": "tabela", "celulas": [...], "geo": [(x0, x1)...], "orig": [as linhas]}`. Depois `validar_tabelas` confere."""
    saida = []
    for i, l in enumerate(ordenadas):
        ant = saida[-1] if saida else None
        # quem tem uma parceira ao lado (mesmo topo) abre uma linha de tabela: não é continuação de célula
        prox = ordenadas[i + 1] if i + 1 < len(ordenadas) else None
        abre_linha = bool(prox and prox["tipo"] == "corpo" and prox["col"] == l["col"]
                          and abs(prox["y0"] - l["y0"]) <= 2.5 and prox["x0"] > l["x1"] + 6)
        # a célula que continua na linha de baixo ("(machados, lanças, etc.)") volta para a 1ª célula da linha
        if (ant and ant["tipo"] == "tabela" and l["tipo"] == "corpo" and ant["col"] == l["col"] and not abre_linha
                and abs(l["x0"] - ant["x0"]) <= 8 and -2 <= l["y0"] - ant["y1"] <= 3):
            ant["celulas"][0] = ant["celulas"][0] + " " + texto_da_linha(l)
            ant["y1"] = l["y1"]
            ant["orig"].append(l)
            continue
        if (ant and l["tipo"] == "corpo" and ant["tipo"] in ("corpo", "tabela") and ant["col"] == l["col"]
                and abs(l["y0"] - ant["y0"]) <= 2.5 and l["x0"] > ant["x1"] + 6):
            if ant["tipo"] == "corpo":
                ant = dict(ant, tipo="tabela", celulas=[texto_da_linha(ant)], geo=[(ant["x0"], ant["x1"])], orig=[ant])
                saida[-1] = ant
            ant["celulas"].append(texto_da_linha(l))
            ant["geo"].append((l["x0"], l["x1"]))
            ant["orig"].append(l)
            ant["x1"] = l["x1"]
            continue
        saida.append(l)
    return validar_tabelas(saida)


def validar_tabelas(itens):
    """Uma tabela tem 2 linhas ou mais, e suas colunas ALINHAM de uma linha para a outra (à esquerda ou à direita).
    Texto justificado com espaços largos, ou contornando uma figura, tem pedaços lado a lado que não alinham: volta a
    ser texto, linha por linha."""
    saida, i = [], 0
    while i < len(itens):
        if itens[i]["tipo"] != "tabela":
            saida.append(itens[i])
            i += 1
            continue
        j = i
        while j + 1 < len(itens) and itens[j + 1]["tipo"] == "tabela":
            j += 1
        grupo = itens[i:j + 1]
        valida = len(grupo) >= 2
        if valida:
            n = min(len(r["geo"]) for r in grupo)
            for k in range(n):
                x0 = [r["geo"][k][0] for r in grupo]
                x1 = [r["geo"][k][1] for r in grupo]
                if not (max(x0) - min(x0) <= 6 or max(x1) - min(x1) <= 6):
                    valida = False
        if valida:
            saida.extend(grupo)
        else:
            for r in grupo:
                saida.extend(r["orig"])
        i = j + 1
    return saida


def sem_contos(ordenadas):
    """Contos (a história de abertura de capítulo) são texto COLORIDO sem rótulo na frente. O valor de um campo que
    continua em outra linha vem logo depois do rótulo (uma cadeia curta); o valor que continua no topo da página, vindo
    da página anterior, é uma corrida de uma dúzia de linhas. Uma corrida de 14 ou mais linhas coloridas seguidas é um
    conto: a página tem conto, e todas as linhas coloridas sem rótulo na frente saem."""
    presas, cadeia, ultima_col = set(), 0, None
    for i, l in enumerate(ordenadas):
        if l["tipo"] == "rotulo":
            cadeia, ultima_col = 1, l["col"]
        elif l["tipo"] == "continua-rotulo" and cadeia and cadeia <= 5 and l["col"] == ultima_col:
            cadeia += 1
            presas.add(i)
        else:
            cadeia = 0
    # a corrida mais longa de linhas coloridas seguidas na mesma coluna
    maior, corrida, anterior = 0, 0, None
    for l in ordenadas:
        if l["tipo"] == "continua-rotulo" and (anterior is None or anterior["col"] == l["col"]):
            corrida += 1
        elif l["tipo"] == "continua-rotulo":
            corrida = 1
        else:
            corrida = 0
        anterior = l if l["tipo"] == "continua-rotulo" else None
        maior = max(maior, corrida)
    if maior < 14:
        return ordenadas
    fora = {i for i, l in enumerate(ordenadas) if l["tipo"] == "continua-rotulo" and i not in presas}
    return [l for i, l in enumerate(ordenadas) if i not in fora]


def quadro_largo(linhas, colunas):
    """Um quadro que ocupa 2 ou mais colunas mas NÃO a página toda (na p.112 o Lampejo, embaixo das duas primeiras
    colunas, com a terceira correndo ao lado). Lido por coluna, ele se enfiava no meio do Brilho e do Escurecer.
    Devolve `(x0, x1, y0)` do quadro, ou `None`. As linhas dele vão para uma coluna a mais, no fim."""
    if len(colunas) < 3:
        return None
    gap = min(b - a for a, b in zip(colunas, colunas[1:]))
    largas = [l for l in linhas if l["tipo"] == "corpo" and (l["x1"] - l["x0"]) > 1.35 * gap]
    if len(largas) < 3:
        return None
    x0, x1 = min(l["x0"] for l in largas), max(l["x1"] for l in largas)
    if x1 >= colunas[-1] + 10:
        return None  # ocupa a página toda: é uma faixa, e a ordem por coluna já a trata
    topo = min(l["y0"] for l in largas)
    # o título do quadro fica logo acima da primeira linha larga, na margem esquerda dele
    for l in linhas:
        if l["tipo"] == "titulo" and abs(l["x0"] - x0) <= 15 and 0 <= topo - l["y0"] <= 60:
            topo = min(topo, l["y0"])
    return x0, x1, topo


def atribuir_colunas(linhas, colunas):
    for l in linhas:
        l["col"] = coluna_da_linha(l, colunas)
    quadro = quadro_largo(linhas, colunas) if QUADRO_LARGO else None
    if quadro:
        qx0, qx1, qy0 = quadro
        for l in linhas:
            if l["x0"] >= qx0 - 5 and l["x1"] <= qx1 + 5 and l["y0"] >= qy0 - 2:
                l["col"] = len(colunas)


def ordenar(linhas):
    colunas = bordas_das_colunas(linhas)
    atribuir_colunas(linhas, colunas)
    ordenadas = sorted(linhas, key=lambda l: (l["col"], round(l["y0"], 1), l["x0"]))
    sem = sem_contos(ordenadas)
    if len(sem) != len(ordenadas):
        # o conto tinha colunas próprias (margens esquerdas que não são as da página): sem ele, as colunas se refazem
        colunas = bordas_das_colunas(sem)
        atribuir_colunas(sem, colunas)
        sem = sorted(sem, key=lambda l: (l["col"], round(l["y0"], 1), l["x0"]))
    return agrupar_tabelas(sem), colunas


# ------------------------------------------------------------------------------------- vocabulário

def vocabulario(caminho_chunks):
    """Palavras do português que o chunks.jsonl já tem: serve para decidir o hífen no fim da linha."""
    palavras = set()
    rx = re.compile(r"[a-záàâãéêíóôõúüç]+(?:-[a-záàâãéêíóôõúüç]+)*", re.I)
    with open(caminho_chunks, "r", encoding="utf-8") as f:
        for linha in f:
            if '"language": "pt"' not in linha:
                continue
            try:
                texto = json.loads(linha)["text"]
            except ValueError:
                continue
            for p in rx.findall(texto):
                palavras.add(p.lower())
    return palavras


def limpar(t):
    t = t.replace("⁄", "/").replace(" ", " ").replace("­", "")
    return re.sub(r"[ \t]+", " ", t).strip()


def juntar(anterior, nova, vocab):
    """Junta duas linhas do mesmo parágrafo, resolvendo o hífen do fim da linha."""
    if not anterior:
        return nova
    if anterior.endswith("-") and nova.startswith("-"):
        return anterior + nova[1:]
    if anterior.endswith("-") and nova:
        m = re.search(r"([A-Za-zÀ-ÿ]+)-$", anterior)
        n = re.match(r"([A-Za-zÀ-ÿ]+)", nova)
        if m and n and n.group(1)[:1].islower():
            colado = (m.group(1) + n.group(1)).lower()
            com_hifen = (m.group(1) + "-" + n.group(1)).lower()
            if colado in vocab or com_hifen not in vocab:
                return anterior[:-1] + nova
        return anterior + nova
    return anterior + " " + nova


def texto_da_linha(linha):
    return limpar("".join(s["text"] for s in linha["spans"]))


def rotulo_e_valor(linha):
    spans = [s for s in linha["spans"] if s["text"].strip() != ""]
    rotulo = spans[0]["text"].strip().rstrip(":").strip()
    resto = limpar("".join(s["text"] for s in linha["spans"][linha["spans"].index(spans[0]) + 1:]))
    resto = re.sub(r"^\s*:\s*", "", resto)
    return rotulo, resto


# ------------------------------------------------------------------------------------- a página

def titulo_do_capitulo(pagina):
    for l in pagina.get_text().split("\n"):
        t = l.strip()
        if t and not t.isdigit():
            return t
    return ""


def paragrafos(linhas, vocab):
    """[(tipo, texto)] na ordem de leitura, com as linhas de um parágrafo já juntas."""
    saida = []
    atual = None  # [tipo, texto, ultima_linha]

    def fechar():
        nonlocal atual
        if atual:
            saida.append((atual[0], atual[1]))
        atual = None

    for l in linhas:
        tipo = l["tipo"]
        if tipo == "rotulo":
            fechar()
            r, v = rotulo_e_valor(l)
            atual = ["rotulo", (r, v), l]
            continue
        if tipo == "tabela":
            fechar()
            saida.append(("tabela", l["celulas"]))
            continue
        texto = texto_da_linha(l)
        if not texto:
            continue
        if tipo in ("titulo", "classe"):
            if atual and atual[0] == tipo and atual[2]["col"] == l["col"] and (l["y0"] - atual[2]["y1"]) < 6:
                atual[1] = juntar(atual[1], texto, vocab) if tipo == "classe" else atual[1] + " " + texto
                atual[2] = l
            else:
                fechar()
                atual = [tipo, texto, l]
            continue
        if tipo == "tabela":
            fechar()
            saida.append(("tabela", l["celulas"]))
            continue
        if tipo == "item":
            fechar()
            saida.append(("item", "Item"))
            continue
        if tipo == "continua-rotulo":
            if atual and atual[0] == "rotulo" and atual[2]["col"] == l["col"]:
                r, v = atual[1]
                atual[1] = (r, juntar(v, texto, vocab))
                atual[2] = l
                continue
            tipo = "corpo"
        # corpo
        if atual and atual[0] == "corpo":
            ult = atual[2]
            terminou = atual[1].rstrip()[-1:] in ".!?:;”\")…"
            # a frase que não terminou e continua em minúscula é o MESMO parágrafo: o parágrafo passa de uma
            # coluna para a outra, e uma figura abre uma lacuna que não é espaço entre parágrafos
            continua = (not terminou) and texto[:1].islower()
            mesma_coluna = ult["col"] == l["col"]
            novo_paragrafo = (not continua) and (
                not mesma_coluna
                or (l["y0"] - ult["y1"]) > LACUNA_DE_PARAGRAFO
                or (terminou and l["x0"] > ult["x0"] + 3.0))
            if not novo_paragrafo:
                atual[1] = juntar(atual[1], texto, vocab)
                atual[2] = l
                continue
        fechar()
        atual = ["corpo", texto, l]
    fechar()
    return saida


def markdown(impressa, titulo, itens):
    linhas = ["# Página %d - %s" % (impressa, titulo), ""]
    primeiro_titulo = True
    i = 0
    while i < len(itens):
        tipo, texto = itens[i]
        if tipo == "titulo":
            if not primeiro_titulo or len(linhas) > 2:
                linhas += ["---", ""]
            primeiro_titulo = False
            linhas.append("### " + texto)
        elif tipo == "classe":
            linhas += ["**" + texto + "**", ""]
        elif tipo == "rotulo":
            r, v = texto
            linhas += ["**%s:** %s" % (r, v), ""]
        elif tipo == "tabela":
            linhas.append("| " + " | ".join(texto) + " |")
            if i == 0 or itens[i - 1][0] != "tabela":
                linhas.append("|" + "---|" * len(texto))
            if i + 1 >= len(itens) or itens[i + 1][0] != "tabela":
                linhas.append("")
        elif tipo == "item":
            linhas.append("**Item**")
        else:
            linhas += [texto, ""]
        i += 1
    while linhas and linhas[-1] == "":
        linhas.pop()
    return "\n".join(linhas)


def pagina_do_pdf(doc, impressa):
    """O índice do PDF da página impressa N, conferido pelo número branco da aba."""
    for idx in (impressa + 1, impressa, impressa + 2):
        if not (0 <= idx < doc.page_count):
            continue
        for b in doc[idx].get_text("dict")["blocks"]:
            if b["type"] != 0:
                continue
            for l in b["lines"]:
                for s in l["spans"]:
                    if s["color"] == BRANCO and s["text"].strip() == str(impressa):
                        return idx
    raise SystemExit("não achei a página impressa %d no PDF (o número branco da aba não bate)" % impressa)


def converter(doc, impressa, vocab):
    idx = pagina_do_pdf(doc, impressa)
    pagina = doc[idx]
    linhas, _ = ordenar(linhas_da_pagina(pagina))
    return markdown(impressa, titulo_do_capitulo(pagina), paragrafos(linhas, vocab))


# ------------------------------------------------------------------------------------- o chunks.jsonl

NUM = re.compile(r"\d+(?:[.,]\d+)?")


def aplicar(caminho_chunks, novos):
    """Troca o `text` das páginas em `novos` ({impressa: markdown}); o resto fica byte a byte."""
    with open(caminho_chunks, "rb") as f:
        bruto = f.read()
    fim = b"\r\n" if b"\r\n" in bruto else b"\n"
    partes = bruto.split(fim)
    mudou = []
    for i, p in enumerate(partes):
        if not p.strip():
            continue
        o = json.loads(p.decode("utf-8"))
        if o.get("source_id") != "pt_magia" or o.get("page_number") not in novos:
            continue
        antigo = o["text"]
        o["text"] = novos[o["page_number"]]
        partes[i] = json.dumps(o, ensure_ascii=False, separators=(", ", ": ")).encode("utf-8")
        mudou.append((o["page_number"], len(antigo), len(o["text"])))
    with open(caminho_chunks, "wb") as f:
        f.write(fim.join(partes))
    return mudou


def texto_atual(caminho_chunks, impressas):
    saida = {}
    with open(caminho_chunks, "r", encoding="utf-8") as f:
        for linha in f:
            if '"source_id": "pt_magia"' not in linha:
                continue
            o = json.loads(linha)
            if o["page_number"] in impressas:
                saida[o["page_number"]] = o["text"]
    return saida


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--pdf", required=True)
    ap.add_argument("--paginas", required=True, help="páginas IMPRESSAS, separadas por vírgula")
    ap.add_argument("--chunks", default="app/src/main/assets/chunks.jsonl")
    ap.add_argument("--aplicar", action="store_true")
    ap.add_argument("--fundir-bordas", action="store_true",
                    help="margens a menos de 40pt são a mesma coluna (p.142, p.2, p.6...); confira o ensaio")
    ap.add_argument("--quadro-largo", action="store_true",
                    help="quadro que ocupa 2+ colunas, mas não a página toda, é lido no fim (p.112); confira o ensaio")
    ap.add_argument("--mostrar", action="store_true", help="imprime o markdown de cada página")
    ap.add_argument("--saida", help="grava o markdown de cada página neste JSON (ensaio)")
    a = ap.parse_args()
    global FUNDIR_BORDAS, QUADRO_LARGO
    FUNDIR_BORDAS, QUADRO_LARGO = a.fundir_bordas, a.quadro_largo
    sys.stdout.reconfigure(encoding="utf-8")

    impressas = [int(x) for x in a.paginas.split(",") if x.strip()]
    doc = pymupdf.open(a.pdf)
    vocab = vocabulario(a.chunks)
    novos = {p: converter(doc, p, vocab) for p in impressas}
    antigos = texto_atual(a.chunks, set(impressas))

    for p in impressas:
        velho, novo = antigos.get(p, ""), novos[p]
        nv, nn = NUM.findall(velho), NUM.findall(novo)
        saem = sorted(set(nv) - set(nn))
        entram = sorted(set(nn) - set(nv))
        print("p.%d: %d -> %d caracteres | números que saem: %s | entram: %s" % (p, len(velho), len(novo), saem[:12], entram[:12]))
        if a.mostrar:
            print(novo)
            print("=" * 60)
    if a.saida:
        with open(a.saida, "w", encoding="utf-8") as f:
            json.dump({str(k): v for k, v in novos.items()}, f, ensure_ascii=False, indent=1)
    if a.aplicar:
        for p, antes, depois in aplicar(a.chunks, novos):
            print("gravada p.%d (%d -> %d caracteres)" % (p, antes, depois))


if __name__ == "__main__":
    main()
