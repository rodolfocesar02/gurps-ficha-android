# -*- coding: utf-8 -*-
"""
IC-1 do planos/itens/PLANO.md (Mesa): o catalogo de itens do livro, com a regra de cada um.

Le os PDFs (Modulo Basico e Magia) e gera `app/src/main/assets/itens.v1.json`.
Ninguem redigita o livro: nome, NT, peso, custo, pagina e os numeros da regra saem do texto da pagina.
O que o script nao consegue ler vai para `scripts/itens/EXCECOES-IC-1.md`, para o Rodolfo conferir.
A `nota` e uma frase PROPRIA, montada dos numeros lidos (nunca o texto do livro).

Rodar (na raiz do app):   python -X utf8 scripts/itens/gerar_itens.py
Livros: a pasta em GURPS_LIVROS, ou C:/Users/Rodolfo/Desktop/rpg/gurps/livros.
No pymupdf: MB `d[N]` = pagina impressa N; Magia: pagina impressa N = indice N+1.
"""
import glob
import json
import os
import re
import sys
import unicodedata

import pymupdf

RAIZ = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
SAIDA = os.path.join(RAIZ, "app", "src", "main", "assets", "itens.v1.json")
EXCECOES = os.path.join(os.path.dirname(os.path.abspath(__file__)), "EXCECOES-IC-1.md")
LIVROS = os.environ.get("GURPS_LIVROS", "C:/Users/Rodolfo/Desktop/rpg/gurps/livros")

excecoes = []


def excecao(item, pagina, motivo):
    excecoes.append((item, pagina, motivo))


# ---------------------------------------------------------------- os livros
def achar_pdf(comeca, contem):
    for p in sorted(glob.glob(os.path.join(LIVROS, "*.pdf"))):
        b = os.path.basename(p)
        if b.startswith(comeca) and contem in b:
            return p
    raise SystemExit("PDF nao encontrado em %s: %s...%s" % (LIVROS, comeca, contem))


def abrir():
    mb = pymupdf.open(achar_pdf("4", "sico"))
    magia = pymupdf.open(achar_pdf("4", "Magia+Diagramas"))
    return mb, magia


def pagina_mb(mb, n):
    return mb[n].get_text()


def pagina_magia(magia, n):
    return magia[n + 1].get_text()


def corrido(texto):
    """O texto da pagina numa linha so: junta a palavra partida no fim da linha e tira os espacos a mais."""
    t = texto.replace("-\n", "").replace("\n", " ")
    return re.sub(r"\s+", " ", t).strip()


# ---------------------------------------------------------------- utilidades
def maiuscula(txt):
    return txt[:1].upper() + txt[1:]


def slug(nome):
    s = unicodedata.normalize("NFD", nome)
    s = "".join(c for c in s if unicodedata.category(c) != "Mn").lower()
    s = s.replace("×", "x")
    return re.sub(r"[^a-z0-9]+", "_", s).strip("_")


def numero(txt):
    """'1.500' -> 1500 ; '1,50' -> 1.5 ; '0,125' -> 0.125"""
    t = txt.strip().replace(".", "").replace(",", ".")
    v = float(t)
    return int(v) if v == int(v) else v


PALAVRAS = {"uma": 1, "um": 1, "duas": 2, "dois": 2, "tres": 3, "quatro": 4, "cinco": 5, "seis": 6, "sete": 7,
            "oito": 8, "nove": 9, "dez": 10}


def quantos(txt):
    t = slug(txt)
    return PALAVRAS[t] if t in PALAVRAS else int(t)


SEGUNDOS = {"segundo": 1, "segundos": 1, "minuto": 60, "minutos": 60, "hora": 3600, "horas": 3600}
NOME_PROPRIO = r"[A-ZÀ-Ú][\wÀ-úç]+(?:\s+(?:de\s+|da\s+|do\s+|a\s+|à\s+|ao\s+|com\s+|o\s+|sobre\s+a\s+)?[A-ZÀ-Ú][\wÀ-úç]+)*"


# ---------------------------------------------------------------- A. Equipamentos Variados (MB pp.288-289)
SECOES = [
    ("Equipamentos de Acampamento e Sobrevivência", "Acampamento e Sobrevivência"),
    ("Equipamentos de Comunicação e Informação", "Comunicação e Informação"),
    ("Equipamento de Equitação", "Equitação"),
    ("Equipamentos de Ladrões, Defensores da Lei e Espiões", "Ladrões, Defensores da Lei e Espiões"),
    ("Equipamento Médico", "Médico"),
    ("Sensores e Equipamentos Óticos", "Sensores e Óticos"),
    ("Ferramentas", "Ferramentas"),
    ("Transporte", None),
    ("Acessórios para Armas e de Combate", "Acessórios para Armas e de Combate"),
]
ENTRADA = re.compile(r"(?:(?<=\. )|(?<=^)|(?<=! ))(?P<nome>[A-ZÀ-Ú“][^().:$!]{1,70}?(?: \((?!NT|var)[^)]{1,20}\))?)\s*\((?P<nt>NT\d+|var\.?)\)")
PRECO = re.compile(r"\$(?P<custo>[\d.,]+?)(?:, (?P<peso>[\d,]+) ?(?P<un>kg|g))?(?:, desp)?(?:, (?P<h>[\d,]+) h)?\.(?!\d)")


def regra_do_variado(nome, categoria, corpo):
    """A regra e a nota (frase propria) de um item de Equipamentos Variados, pelos numeros do texto."""
    notas = []
    regra = {"tipo": "narrativo"}

    m = re.search(r"Ilumina (?:durante|por) (\d+) ?h(?:oras)?", corpo) or re.search(r"Dura (\d+) h", corpo)
    facho = re.search(r"Facho de ([\d,]+) m", corpo)
    if m or facho:
        regra = {"tipo": "luz"}
        if m:
            regra["duracaoHoras"] = int(m.group(1))
            notas.append("Ilumina por %s h." % m.group(1))
        if facho:
            regra["alcanceMetros"] = numero(facho.group(1))
            notas.append("Facho de %s m." % facho.group(1))
        return regra, " ".join(notas)

    if categoria == "Acessórios para Armas e de Combate":
        nh = re.search(r"\+(\d) sobre o NH", corpo)
        prec = re.search(r"\+(\d) em Prec", corpo)
        dado = re.search(r"dano em -(\d) por dado", corpo)
        cabe = re.search(r"Espaço para (\d+) flechas", corpo)
        if nh:
            return {"tipo": "acessorio", "bonusNh": int(nh.group(1))}, "+%s no NH do tiro." % nh.group(1)
        if prec:
            r = {"tipo": "acessorio", "bonusPrec": int(prec.group(1)), "soApontando": "apontados" in corpo}
            return r, "+%s em Prec%s." % (prec.group(1), ", só em tiro apontado" if r["soApontando"] else "")
        if dado:
            return {"tipo": "acessorio", "danoPorDado": -int(dado.group(1))}, "−%s de dano por dado; abafa o tiro." % dado.group(1)
        if cabe:
            return {"tipo": "narrativo", "capacidade": int(cabe.group(1))}, "Cabem %s flechas ou virotes." % cabe.group(1)

    concede = re.search(r"[Cc]oncede (?:\(NT - (\d)\) níveis de )?(" + NOME_PROPRIO + r")(?: (\d+))?", corpo)
    if concede:
        regra = {"tipo": "concede", "vantagem": concede.group(2)}
        if concede.group(1):
            regra["niveis"] = "NT-" + concede.group(1)
            notas.append("Concede %s (NT − %s níveis)." % (concede.group(2), concede.group(1)))
        elif concede.group(3):
            regra["niveis"] = int(concede.group(3))
            notas.append("Concede %s %s." % (concede.group(2), concede.group(3)))
        else:
            notas.append("Concede %s." % concede.group(2))
        return regra, " ".join(notas)

    basico = re.search(r"[Ee]quipamento básico para (?:a |as |uma )?per[ií]cias? ([^.;:$]+)", corpo)
    bonus = re.search(r"\+(\d)\s+(?:em|na perícia|nos testes de|sobre a perícia)\s+(" + NOME_PROPRIO + ")", corpo)
    controla = re.search(r"\+(\d) para controlar", corpo)
    falta = re.search(r"penalidade de -(\d+) (?:em|nos testes de)\s+(" + NOME_PROPRIO + ")", corpo)
    tipo = "cura" if categoria == "Médico" else "kit"
    a_seguir = re.search(r"básico para uma das perícias a seguir: (.+)", corpo)
    if a_seguir:
        variantes = []
        for pedaco in a_seguir.group(1).split(";"):
            preco = re.search(r"\$([\d.]+), ([\d,]+) kg", pedaco)
            nomes = re.findall(r"(" + NOME_PROPRIO + r")(?: \([^)N][^)]*\))? \(NT\d\)", pedaco)
            if preco and nomes:
                variantes.append({"pericias": nomes, "custo": numero(preco.group(1)), "peso": numero(preco.group(2))})
        regra = {"tipo": "kit", "basico": True, "periciaAEscolher": True, "variantes": variantes}
        return regra, "Equipamento básico de uma perícia à escolha (%d opções de preço)." % len(variantes)
    if basico and basico.group(1).strip()[:1].islower():
        return {"tipo": tipo, "basico": True, "periciaAEscolher": True}, "Equipamento básico de uma perícia à escolha."
    if bonus:
        regra = {"tipo": tipo, "pericia": bonus.group(2), "bonus": int(bonus.group(1))}
        notas.append("+%s em %s." % (bonus.group(1), bonus.group(2)))
    elif controla:
        regra = {"tipo": "kit", "pericia": "Cavalgar", "bonus": int(controla.group(1))}
        notas.append("+%s para controlar a montaria." % controla.group(1))
    elif basico:
        pericias = [p.strip() for p in re.split(r",| e ", basico.group(1)) if p.strip()]
        pericias = [re.sub(r" de um grupo$| específica.*$", "", p) for p in pericias]
        regra = {"tipo": tipo, "pericias": pericias, "basico": True}
        notas.append("Equipamento básico de %s." % " e ".join(pericias))
    elif falta:
        sem = "sem ele" in corpo
        regra = {"tipo": "kit", "pericia": falta.group(2), ("penalidadeSemEle" if sem else "penalidade"): -int(falta.group(1))}
        notas.append(("Sem ele, −%s em %s." if sem else "−%s em %s.") % (falta.group(1), falta.group(2)))
    improvisado = re.search(r"equipamento improvisado \(-(\d+)\) para (" + NOME_PROPRIO + ")", corpo)
    if improvisado and regra["tipo"] != "narrativo":
        regra["improvisado"] = {"pericia": improvisado.group(2), "mod": -int(improvisado.group(1))}
        notas.append("Serve de improviso para %s (−%s)." % (improvisado.group(2), improvisado.group(1)))
    doses = re.search(r"(\d+) doses", corpo)
    if doses and categoria == "Médico" and regra["tipo"] == "narrativo":
        regra = {"tipo": "cura", "antidoto": True, "doses": int(doses.group(1))}
        notas.append("Antídoto de um veneno específico, %s doses." % doses.group(1))
    if regra["tipo"] in ("kit", "cura") and categoria == "Equitação":
        regra["soCompensaRedutor"] = True
        notas.append("Só compensa redutores.")
    if regra["tipo"] != "narrativo":
        return regra, " ".join(notas)

    cap = re.search(r"Suporta ([\d.,]+) ?(kg|t)\b", corpo) or re.search(r"Carrega ([\d.,]+) (kg)", corpo)
    litros = re.search(r"Capac\. ([\d,]+) litros?", corpo) or re.search(r"Conserva ([\d,]+) litros?", corpo)
    if cap:
        return regra, "Suporta %s %s." % (cap.group(1), cap.group(2))
    if litros:
        return regra, "Leva %s litro(s) de líquido." % litros.group(1)
    return regra, ""


def variados(mb):
    paginas = {288: corrido(pagina_mb(mb, 288)), 289: corrido(pagina_mb(mb, 289))}
    itens = []
    for pg, texto in paginas.items():
        marcas = []
        for titulo, categoria in SECOES:
            pos = texto.find(titulo)
            if pos >= 0:
                marcas.append((pos, len(titulo), categoria))
        marcas.sort()
        for i, (pos, tam, categoria) in enumerate(marcas):
            if categoria is None:
                continue
            fim = marcas[i + 1][0] if i + 1 < len(marcas) else len(texto)
            trecho = texto[pos + tam:fim].strip()
            achados = list(ENTRADA.finditer(trecho))
            for k, m in enumerate(achados):
                corpo = trecho[m.end():achados[k + 1].start() if k + 1 < len(achados) else len(trecho)].strip(" .")
                corpo = corpo + "."
                nome = m.group("nome").strip().replace("“", "").replace("”", "").strip()
                nt = m.group("nt")
                item = {"id": slug(nome), "nome": nome, "categoria": categoria,
                        "nt": None if nt.startswith("var") else int(nt[2:]), "ntVariavel": nt.startswith("var"),
                        "peso": 0, "custo": None, "legalidade": 4, "pagina": pg, "livro": "MB"}
                precos = list(PRECO.finditer(corpo))
                if precos:
                    p = precos[-1]
                    item["custo"] = numero(p.group("custo"))
                    if p.group("peso"):
                        peso = numero(p.group("peso"))
                        item["peso"] = peso / 1000 if p.group("un") == "g" else peso
                    if p.group("h"):
                        item["bateriaHoras"] = numero(p.group("h"))
                    if len(precos) > 1 or len(re.findall(r"\$", corpo)) > 1:
                        excecao(nome, pg, "mais de um preço no texto; ficou $%s e %s kg" % (item["custo"], item["peso"]))
                else:
                    excecao(nome, pg, "o script não achou o preço")
                if re.search(r"Cada 10 metros|Por litro|Cada 0,5 litro|Conjunto com 4|Cada par", corpo):
                    item["unidade"] = re.search(r"Cada 10 metros|Por litro|Cada 0,5 litro|Conjunto com 4|Cada par", corpo).group(0).lower()
                regra, nota = regra_do_variado(nome, categoria, corpo)
                igual = re.match(r"(?:Como o item anterior|Igual (?:à|ao) [^,.]+)", corpo)
                if igual and itens and regra["tipo"] == "narrativo":
                    anterior = itens[-1]
                    regra = dict(anterior["regra"])
                    regra["comoO"] = anterior["id"]
                    nota = "Como %s." % anterior["nome"]
                    mais = re.search(r"dar (" + NOME_PROPRIO + ")", corpo)
                    if mais:
                        regra["concede"] = mais.group(1)
                        nota = "Como %s, e dá %s." % (anterior["nome"], mais.group(1))
                    rd = re.search(r"RD (\d+) e PV (\d+)", corpo)
                    if rd:
                        regra["rd"], regra["pv"] = int(rd.group(1)), int(rd.group(2))
                        nota = "Como %s, com RD %s e PV %s." % (anterior["nome"], rd.group(1), rd.group(2))
                item["nota"] = nota
                item["regra"] = regra
                if regra["tipo"] == "narrativo" and re.search(r"[+-]\d|\dd", corpo):
                    excecao(nome, pg, "tem número no texto que o script não transformou em regra")
                itens.append(item)
    return itens


# ---------------------------------------------------------------- B. Granadas e incendiárias (MB p.277)
LINHA_GRANADA = re.compile(
    r"(?P<nt>\d|\^) (?P<nome>(?:de )?[A-ZÀ-Ú][\wÀ-ú ]+?) (?P<dano>(?:\d+d(?:×\d)? (?:cont|qmd) ex(?: \[\d+d\])?|espec\. \(\d+ m\)|HT-\d+ at \(\d+ m\)))\s+"
    r"(?P<peso>[\d,]+) (?P<estopim>\d+(?:[–-]\d+)?|espec\.) \$(?P<custo>\d+) (?P<cl>\d) \[(?P<obs>[\d, ]+)\]")


def granadas(mb):
    texto = corrido(pagina_mb(mb, 277))
    tabela = texto[texto.find("Tabela de Granadas"):texto.find("Observações [1]")]
    itens = []
    for m in LINHA_GRANADA.finditer(tabela):
        nt = 10 if m.group("nt") == "^" else int(m.group("nt"))
        cru = m.group("nome").strip()
        nome = cru if cru.startswith("Coquetel") else "Granada " + (cru if cru.startswith("de ") else "de " + cru if cru == "Pólvora Negra" else cru)
        dano = m.group("dano")
        obs = [int(x) for x in m.group("obs").replace(" ", "").split(",")]
        regra = {"tipo": "explosivo"}
        d = re.match(r"(\d+d(?:×\d)?) (cont|qmd) ex(?: \[(\d+)d\])?", dano)
        raio = re.search(r"\((\d+) m\)", dano)
        if d:
            regra.update({"expr": d.group(1), "tipoDano": d.group(2), "frag": int(d.group(3)) if d.group(3) else 0})
        if raio:
            regra["raio"] = int(raio.group(1))
        ht = re.match(r"HT-(\d+) at", dano)
        if ht:
            regra["especial"] = "atordoamento"
            regra["resistirHt"] = -int(ht.group(1))
        elif dano.startswith("espec.") and not cru.startswith("Coquetel"):
            regra["especial"] = "quimica"
        if cru.startswith("Coquetel"):
            regra["molotov"] = True
            regras = corrido(pagina_mb(mb, 410)) + " " + corrido(pagina_mb(mb, 411))
            mauf = re.search(r"Mauf (\d+)", regras)
            fogo = re.search(r"causa (\d+d) pontos de dano por queimadura e depois (\d+d) ponto de dano por queimadura por segundo", regras)
            if mauf:
                regra["mauf"] = int(mauf.group(1))
            if fogo:
                regra.update({"expr": fogo.group(1), "tipoDano": "qmd", "porSegundo": fogo.group(2), "paginaDaRegra": 411})
            else:
                excecao("Coquetel Molotov", 411, "o script não leu o dano (3d e depois 1d por segundo)")
        est = m.group("estopim")
        if est != "espec.":
            p = re.split(r"[–-]", est)
            regra["estopim"] = [int(p[0]), int(p[1])] if len(p) == 2 else int(p[0])
        regra["acender"] = 1 in obs      # obs [1]: Preparar para acender o estopim
        regra["pino"] = 2 in obs         # obs [2]: sacar e puxar o pino
        ident = slug(nome) + ("_nt%d" % nt if any(i["nome"] == nome for i in itens) else "")
        partes = []
        if d:
            partes.append("%s %s explosivo%s" % (d.group(1), "contusão" if d.group(2) == "cont" else "queimadura",
                                               ", fragmentos %sd" % d.group(3) if d.group(3) else ""))
        if ht:
            partes.append("HT−%s ou atordoado, em %s m" % (ht.group(1), regra.get("raio")))
        if regra.get("especial") == "quimica":
            partes.append("nuvem de %s m" % regra.get("raio"))
        if regra.get("molotov"):
            if regra.get("expr"):
                partes.append("%s de queimadura, depois %s por segundo" % (regra["expr"], regra.get("porSegundo")))
            partes.append("quebra no impacto e incendeia %s m" % regra.get("raio"))
        if "estopim" in regra:
            e = regra["estopim"]
            partes.append("explode em %s s" % ("%d a %d" % tuple(e) if isinstance(e, list) else e))
        itens.append({"id": ident, "nome": nome + (" (NT%d)" % nt if ident.endswith("_nt%d" % nt) else ""),
                      "categoria": "Granadas e Incendiárias", "nt": nt, "ntVariavel": False,
                      "peso": numero(m.group("peso")), "custo": int(m.group("custo")), "legalidade": int(m.group("cl")),
                      "pagina": 277, "livro": "MB", "nota": maiuscula("; ".join(partes)) + ".", "regra": regra})
    if len(itens) != 9:
        excecao("Tabela de Granadas", 277, "o script leu %d linhas; o livro tem 9" % len(itens))
    return itens


# ---------------------------------------------------------------- C. Munição especial (MB pp.277, 279-280)
def municao(mb):
    itens = []

    def bloco(pg, comeca, termina):
        t = corrido(pagina_mb(mb, pg))
        a = t.find(comeca)
        if a < 0:
            excecao(comeca, pg, "o script não achou o parágrafo")
            return ""
        b = t.find(termina, a + len(comeca)) if termina else -1
        return t[a:b if b > 0 else a + 900]

    def divisor(t):
        m = re.search(r"divisor de armadura de [\[(]([\d,]+)[\])]", t)
        return numero(m.group(1)) if m else None

    finas = bloco(277, "Pontas Finas", "Qualidade das Armas")
    if finas:
        itens.append({"id": "flechas_de_ponta_fina", "nome": "Flechas e virotes de ponta fina", "categoria": "Munição",
                      "nt": int(re.search(r"NT(\d)", finas).group(1)), "ntVariavel": False, "peso": 0, "custo": None,
                      "legalidade": 4, "pagina": 277, "livro": "MB",
                      "nota": "O dano de perfuração vira perfurante, com divisor de armadura %s." % divisor(finas),
                      "regra": {"tipo": "municao", "divisor": divisor(finas), "tipoDano": "pa", "serve": ["arco", "besta"]}})
    po = bloco(279, "Munição de Ponta Oca (PO)", "Munição de Ponta Rígida")
    prpa = bloco(279, "Munição de Ponta Rígida", None)
    p280 = corrido(pagina_mb(mb, 280))
    prpa = prpa + " " + p280[:p280.find("Munição de Ponta Destacável")] if prpa else prpa
    pdpa = bloco(280, "Munição de Ponta Destacável", "Qualidade das Armas de Fogo")
    vezes = {"dobro": 2, "5 vezes": 5}
    for ident, nome, t, passo in (("municao_ponta_oca", "Munição de ponta oca (PO)", po, 1),
                                  ("municao_prpa", "Munição perfurante de ponta rígida (PRPA)", prpa, -1),
                                  ("municao_pdpa", "Munição perfurante de ponta destacável (PDPA)", pdpa, -1)):
        if not t:
            continue
        nt = re.search(r"NT(\d)\+", t)
        cl = re.search(r"CL(\d)", t)
        custo = next((v for k, v in vezes.items() if re.search(k + r" (?:do |o )?custo", t)), 1)
        regra = {"tipo": "municao", "divisor": divisor(t) if ident != "municao_pdpa" else None, "passoDoTipo": passo,
                 "custoVezes": custo, "serve": ["pistola", "metralhadora de mão", "rifle", "metralhadora"]}
        nota = "Divisor de armadura %s; o tipo de dano %s um passo." % (regra["divisor"], "sobe" if passo > 0 else "desce")
        if ident == "municao_pdpa":
            mais = re.search(r"dano em \+(\d) por dado", t)
            dist = re.search(r"distância aumenta em (\d+)%", t)
            regra.update({"comoA": "municao_prpa", "danoPorDado": int(mais.group(1)) if mais else None,
                          "distanciaPorCento": int(dist.group(1)) if dist else None})
            nota = "Como a PRPA, com +%s de dano por dado e +%s%% de distância." % (regra["danoPorDado"], regra["distanciaPorCento"])
        itens.append({"id": ident, "nome": nome, "categoria": "Munição", "nt": int(nt.group(1)) if nt else None, "ntVariavel": False,
                      "peso": 0, "custo": None, "legalidade": int(cl.group(1)) if cl else 4,
                      "pagina": 280 if ident == "municao_pdpa" else 279, "livro": "MB", "nota": nota, "regra": regra})
        if nt is None:
            excecao(nome, 279, "o script não achou o NT")
    return itens


# ---------------------------------------------------------------- D. Venenos (MB p.439)
VENENOS = ["Arsênico", "Cianureto", "Fumaça", "Gás Lacrimogêneo", "Gás Mostarda", "Gás Nervoso", "Veneno de Cobra"]
TRANSMISSOES = [("acompanhamento", "acompanhamento"), ("contato", "contato"), ("digestivo", "digestivo"),
                ("respirat", "respiratorio"), ("visual", "visual"), ("sangu", "sanguineo")]


def venenos(mb):
    texto = corrido(pagina_mb(mb, 439))
    texto = texto[texto.find("Exemplos de Venenos"):texto.find("Indivíduos despreparados")]
    marcas = []
    for n in VENENOS:
        m = re.search(re.escape(n) + r"(?: \(NT(\d)\))?:", texto)
        if m:
            marcas.append((m.start(), m.end(), n, m.group(1)))
        else:
            excecao(n, 439, "o script não achou o veneno na página")
    marcas.sort()
    itens = []
    for i, (ini, fim, nome, nt) in enumerate(marcas):
        corpo = texto[fim:marcas[i + 1][0] if i + 1 < len(marcas) else len(texto)]
        baixo = corpo.lower()
        trans = [chave for pedaco, chave in TRANSMISSOES if pedaco in baixo]
        regra = {"tipo": "veneno", "transmissao": trans, "area": "efeito de área" in baixo}
        ret = re.search(r"retardo de (\w+) (horas?|minutos?|segundos?)", corpo)
        regra["retardo"] = quantos(ret.group(1)) * SEGUNDOS[ret.group(2)] if ret else 0
        if "não há teste de HT" in corpo:
            regra["mod"] = None
            regra["semTeste"] = True   # o `null` some quando a ficha viaja (o Gson não escreve nulos): a marca explícita fica
        else:
            ht = re.search(r"teste de HT-(\d)", corpo)
            regra["mod"] = -int(ht.group(1)) if ht else 0
        dano = re.search(r"(\d+d|\d+) pontos? de dano por toxina", corpo)
        if dano:
            regra["dano"] = dano.group(1)
            regra["tipoDano"] = "tox"
        inter = re.search(r"intervalos de (\w+) (horas?|minutos?)", corpo)
        ciclos = re.search(r"por (\w+) ciclos", corpo)
        regra["intervalo"] = quantos(inter.group(1)) * SEGUNDOS[inter.group(2)] if inter else 0
        regra["ciclos"] = quantos(ciclos.group(1)) if ciclos else 1
        pedacos = [x for x in re.split(r"(?=Como (?:veneno|agente))", corpo) if x.startswith("Como")]
        if len(pedacos) > 1:
            por = {}
            for pd in pedacos:
                cabeca = pd.split(",")[0].lower()
                quais = [chave for pedaco, chave in TRANSMISSOES if pedaco in cabeca]
                v = {}
                r2 = re.search(r"retardo de (\w+) (horas?|minutos?|segundos?)", pd)
                v["retardo"] = quantos(r2.group(1)) * SEGUNDOS[r2.group(2)] if r2 else 0
                h2 = re.search(r"teste de HT-(\d)", pd)
                if h2:
                    v["mod"] = -int(h2.group(1))
                d2 = re.search(r"(\d+d|\d+) pontos? de dano por toxina", pd)
                if d2:
                    v["dano"] = d2.group(1)
                i2 = re.search(r"intervalos de (\w+) (horas?|minutos?)", pd)
                c2 = re.search(r"por (\w+) ciclos", pd)
                if i2:
                    v["intervalo"] = quantos(i2.group(1)) * SEGUNDOS[i2.group(2)]
                if c2:
                    v["ciclos"] = quantos(c2.group(1))
                for q in quais:
                    por[q] = v
            regra["porTransmissao"] = por
            primeira = por.get(trans[0]) if trans else None
            if primeira:
                regra.update({k: primeira[k] for k in primeira})
        atrib = [a for a in ("tosse", "cegueira", "agonia", "paralisia") if a in baixo]
        if atrib:
            regra["atribulacoes"] = atrib
        sint = re.search(r"perder ([\d/, ou]+) do seu PV sofre respectivamente uma penalidade de ([-\d, ou]+) na DX", corpo)
        if sint:
            fr = re.findall(r"(\d)/(\d)", sint.group(1))
            dx = re.findall(r"-(\d+)", sint.group(2))
            regra["sintomas"] = [{"fracao": [int(a), int(b)], "dx": -int(x)} for (a, b), x in zip(fr, dx)]
        custo = re.search(r"\$ ?(\d+)/dose", corpo)
        cl = re.search(r"CL(\d)", corpo)
        if len(pedacos) > 1:
            excecao(nome, 439, "números diferentes por transmissão: confira o campo porTransmissao (%s)" % ", ".join(sorted(regra["porTransmissao"])))
        partes = []
        if trans:
            partes.append("Por " + ", ".join(trans))
        if regra["mod"] is None:
            partes.append("sem teste de HT")
        else:
            partes.append("HT%s" % ("−%d" % -regra["mod"] if regra["mod"] else ""))
        if dano:
            vezes = ", %d vezes" % regra["ciclos"] if regra["ciclos"] > 1 else ""
            partes.append("%s de toxina%s" % (regra["dano"], vezes))
        itens.append({"id": slug(nome), "nome": nome, "categoria": "Venenos", "nt": int(nt) if nt else None, "ntVariavel": False,
                      "peso": 0, "custo": int(custo.group(1)) if custo else None, "porDose": True,
                      "legalidade": int(cl.group(1)) if cl else 4, "pagina": 439, "livro": "MB",
                      "nota": "; ".join(partes) + ".", "regra": regra})
    return itens


# ---------------------------------------------------------------- E. Elixires (Magia pp.213-219)
SECOES_ELIXIR = ["Controle de Animais", "Habilidades de Combate", "Elixires Hostis", "Habilidades Mágicas", "Elixires Medicinais",
                 "Elixires Populares", "Habilidades Mentais", "Controle da Mente", "Perícias e Habilidades Físicas"]
FORMAS = [("poção", "pocao"), ("pó", "po"), ("pastilha", "pastilha"), ("unguento", "unguento"), ("pomada", "unguento")]
ATRIBUTO = r"(ST|DX|IQ|HT)"


def formas_de(txt):
    t = (txt or "").lower()
    todas = ["pocao", "po", "pastilha", "unguento"]
    if t.startswith("qualquer"):
        return [f for f in todas if not (f == "pastilha" and "menos na forma de pastilha" in t)]
    achadas = []
    for palavra, chave in FORMAS:
        if re.search(r"\b" + palavra + r"\b", t) and chave not in achadas:
            achadas.append(chave)
    return achadas


def duracao_de(txt):
    if not txt:
        return None
    m = re.match(r"(\d+d(?:\s?[+×x]\s?\d+)?|\d+|[Uu]m|[Uu]ma)\s*(minutos?|horas?|dias?)", txt)
    if m:
        qt = m.group(1).replace(" ", "")
        return {"quanto": "1" if qt.lower() in ("um", "uma") else qt, "unidade": m.group(2).rstrip("s")}
    return {"texto": txt.split(" (")[0].strip().lower()}


def efeitos_de(efeito):
    """Os efeitos em número que o script reconhece no texto do elixir."""
    r = []
    for m in re.finditer(r"Adiciona (\d+d|\d+) (?:a|ao|à) " + ATRIBUTO, efeito):
        r.append({"atributo": m.group(2), "mod": "+" + m.group(1)})
    for m in re.finditer(r"bônus de \+(\d+) (?:a|ao|à) " + ATRIBUTO + r"(?! para resistir)", efeito):
        r.append({"atributo": m.group(2), "mod": "+" + m.group(1)})
    m = re.search(r"bônus de \+(\d+) ao HT para resistir a qualquer veneno", efeito)
    if m:
        r.append({"resistirVeneno": int(m.group(1))})
    for m in re.finditer(r"reduz a " + ATRIBUTO + r" em (\d+)", efeito):
        r.append({"atributo": m.group(1), "mod": "-" + m.group(2), "porDose": True})
    m = re.search(r"penalidade de -(\d+) para " + ATRIBUTO + r"(?: e " + ATRIBUTO + r")?", efeito)
    if m:
        for a in (m.group(2), m.group(3)):
            if a:
                r.append({"atributo": a, "mod": "-" + m.group(1)})
    m = re.search(r"[Cc]ura (\d+d|\d+) (?:pontos de vida|PV)", efeito)
    if m:
        r.append({"cura": m.group(1)})
    m = re.search(r"restaura (\d+d|\d+) PF", efeito)
    if m:
        r.append({"curaPf": m.group(1)})
    m = re.search(r"RD (\d+)", efeito)
    if m:
        r.append({"rd": int(m.group(1))})
    m = re.search(r"Velocidade Básica e o deslocamento em (\d+)", efeito)
    if m:
        r.append({"velocidade": int(m.group(1)), "deslocamento": int(m.group(1))})
    m = re.search(r"dano de (\d+d)", efeito) or re.search(r"causa (\d+d) de dano", efeito)
    if m:
        e = {"dano": m.group(1)}
        menos = re.search(r"reduz esse dano para (\d+d)", efeito)
        if menos:
            e["comSucessoEmHt"] = menos.group(1)
        r.append(e)
    m = re.search(r"teste de HT-(\d+)", efeito)
    if m:
        r.append({"resistirHt": -int(m.group(1))})
    m = re.search(r"penalidade de -(\d+) para todas as jogadas", efeito)
    if m:
        r.append({"todasAsJogadas": -int(m.group(1))})
    m = re.search(r"bônus de \+(\d+) de reação", efeito)
    if m:
        r.append({"reacao": int(m.group(1))})
    m = re.search(r"reagem a ele com uma penalidade de -(\d+)", efeito)
    if m:
        r.append({"reacao": -int(m.group(1))})
    for m in re.finditer(r"bônus de \+(\d+) (?:a qualquer teste de|em todos os testes de|aos testes de) (" + NOME_PROPRIO + r"|audição|visão)", efeito):
        r.append({"pericia": m.group(2).capitalize(), "bonus": int(m.group(1))})
    m = re.search(r"Adiciona (\d+) ao nível de habilidade das perícias de (" + NOME_PROPRIO + ")", efeito)
    if m:
        r.append({"pericia": m.group(2), "bonus": int(m.group(1))})
    for m in re.finditer(r"(?:mágica|mágicas) (" + NOME_PROPRIO + r") \(pág\. (\d+)\)", efeito):
        r.append({"magica": m.group(1), "paginaMagia": int(m.group(2))})
    da = r"(?:vantagem|desvantagem|[Cc]oncede um|[Cc]oncede (\w+) níveis de|ganha(?: (\d+) níveis em)?)\s+"
    for m in re.finditer(da + r"(" + NOME_PROPRIO + r") \(pág\. MB(\d+)\)", efeito):
        e = {"traco": m.group(3), "paginaMb": int(m.group(4))}
        niveis = m.group(1) or m.group(2)
        if niveis:
            e["niveis"] = quantos(niveis)
        r.append(e)
    return r


def nota_do_elixir(efeitos, dur):
    p = []
    for e in efeitos:
        if "atributo" in e:
            p.append("%s em %s" % (e["mod"].replace("-", "−"), e["atributo"]))
        elif "cura" in e:
            p.append("cura %s PV" % e["cura"])
        elif "curaPf" in e:
            p.append("ou devolve %s PF" % e["curaPf"])
        elif "rd" in e:
            p.append("RD %d" % e["rd"])
        elif "velocidade" in e:
            p.append("+%d em Velocidade e Deslocamento" % e["velocidade"])
        elif "dano" in e:
            p.append("%s de dano%s" % (e["dano"], " (%s com sucesso em HT)" % e["comSucessoEmHt"] if e.get("comSucessoEmHt") else ""))
        elif "resistirHt" in e:
            p.append("resiste com HT−%d" % -e["resistirHt"])
        elif "todasAsJogadas" in e:
            p.append("−%d em todas as jogadas" % -e["todasAsJogadas"])
        elif "reacao" in e:
            p.append("%s%d de reação" % ("+" if e["reacao"] > 0 else "−", abs(e["reacao"])))
        elif "pericia" in e:
            p.append("+%d em %s" % (e["bonus"], e["pericia"]))
        elif "magica" in e:
            p.append("como a mágica %s" % e["magica"])
        elif "traco" in e:
            p.append("dá %s%s" % (e["traco"], " %d" % e["niveis"] if e.get("niveis") else ""))
        elif "resistirVeneno" in e:
            p.append("+%d em HT para resistir a veneno" % e["resistirVeneno"])
    if not p:
        return ""
    quando = ""
    if dur and "quanto" in dur:
        quando = " por %s %s%s" % (dur["quanto"], dur["unidade"], "" if dur["quanto"] == "1" else "s")
    return maiuscula(", ".join(p) + quando + ".")


def elixires(magia):
    texto, marcas = "", []
    for p in range(213, 220):
        marcas.append((len(texto), p))
        texto += " " + corrido(pagina_magia(magia, p))
    texto = re.sub(r"Alquimia \d{3} Alquimia \d{3}(?: 28)?", " ", texto)
    texto = re.sub(r"\s+", " ", texto)
    # as marcas de página mudaram com a limpeza: refaz pela ordem
    def pagina_de(nome_pos_original):
        return nome_pos_original

    posicoes, bruto = [], ""
    for p in range(213, 220):
        limpo = re.sub(r"Alquimia \d{3} Alquimia \d{3}(?: 28)?", " ", corrido(pagina_magia(magia, p)))
        limpo = re.sub(r"\s+", " ", limpo)
        posicoes.append((len(bruto), p))
        bruto += " " + limpo
    texto = bruto

    def pagina(pos):
        pg = 213
        for ini, p in posicoes:
            if ini <= pos:
                pg = p
        return pg

    achados = list(re.finditer(r"Nomes? Alternativos?:", texto))
    itens, secao = [], "Elixires"
    for k, m in enumerate(achados):
        antes = texto[max(0, m.start() - 110):m.start()].strip()
        nome = re.split(r"\.\s|\)\s", antes)[-1].strip()
        for s in SECOES_ELIXIR:
            if nome.startswith(s + " ") or (s + " ") in nome:
                secao = s
                nome = nome.split(s + " ", 1)[1].strip()
        fim = achados[k + 1].start() if k + 1 < len(achados) else len(texto)
        bloco = texto[m.end():fim]
        corte = re.search(r"Ingredientes:[^.]*\.", bloco)
        bloco = bloco[:corte.end()] if corte else bloco[:900]

        def campo(rotulo, b=bloco):
            x = re.search(rotulo + r":\s*(.+?)\.(?= [A-ZÀ-Ú]|$)", b)
            return x.group(1).strip() if x else None

        efeito = bloco.split("Duração:")[0]
        efeito = efeito.split(".", 1)[1].strip() if "." in efeito else efeito
        dur = duracao_de(campo("Duração"))
        forma = campo("Forma")
        custo = re.search(r"Custo:\s*\$([\d.]+)\+?(?:\s*/\s*\$([\d.]+))?", bloco)
        efeitos = efeitos_de(efeito)
        pg = pagina(m.start())
        regra = {"tipo": "elixir", "formas": formas_de(forma), "duracao": dur, "efeitos": efeitos, "secao": secao}
        if forma and ("embora" in forma or "deve ser" in forma or "resiste" in forma):
            excecao(nome + " (elixir)", pg, "a forma tem uma condição a mais no livro: \"%s\"" % forma[:70])
        if not efeitos:
            regra["narrado"] = True
            excecao(nome + " (elixir)", pg, "efeito de interpretação ou que o script não leu em número: fica com o Mestre")
        if not regra["formas"]:
            excecao(nome + " (elixir)", pg, "o script não leu a forma")
        rotulo = ("Elixir " if nome.split()[0] in ("Antídoto", "Calmante", "Mata-Dragão", "Domador") else "Elixir de ") + nome
        if dur and "texto" in dur and re.match(r"como|veja", dur["texto"]):
            excecao(rotulo, pg, "a duração está descrita no texto do livro, não em número")
        if efeitos and re.search(r"\bapenas\b|\bsomente\b|\bexceto\b|a menos que", efeito):
            excecao(rotulo, pg, "o efeito tem uma condição no livro (apenas, exceto, a menos que): confira a nota")
        itens.append({"id": "elixir_" + slug(nome), "nome": rotulo, "categoria": "Elixires", "nt": None, "ntVariavel": False,
                      "peso": 0, "custo": numero(custo.group(1)) if custo else None,
                      "custoMagiaRara": numero(custo.group(2)) if custo and custo.group(2) else None, "porDose": True,
                      "legalidade": 4, "pagina": pg, "livro": "Magia", "nota": nota_do_elixir(efeitos, dur), "regra": regra})
        if not custo:
            excecao(nome + " (elixir)", pg, "o script não leu o custo")
    return itens


# ---------------------------------------------------------------- juntar e gravar
def gerar():
    mb, magia = abrir()
    itens = variados(mb) + granadas(mb) + municao(mb) + venenos(mb) + elixires(magia)
    vistos = {}
    for i in itens:
        if i["id"] in vistos:
            excecao(i["nome"], i["pagina"], "id repetido com \"%s\": ganhou o sufixo da página" % vistos[i["id"]])
            i["id"] = "%s_p%d" % (i["id"], i["pagina"])
        vistos[i["id"]] = i["nome"]
    return itens


def gravar(itens):
    por_tipo = {}
    for i in itens:
        por_tipo[i["regra"]["tipo"]] = por_tipo.get(i["regra"]["tipo"], 0) + 1
    catalogo = {
        "versao": 1,
        "fonte": "Módulo Básico pp.277, 279-280, 288-289, 439; Magia pp.213-219. Gerado por scripts/itens/gerar_itens.py; a nota é frase própria.",
        "tipos": por_tipo,
        "itens": itens,
    }
    with open(SAIDA, "w", encoding="utf-8", newline="\n") as f:
        json.dump(catalogo, f, ensure_ascii=False, indent=1)
        f.write("\n")
    with open(EXCECOES, "w", encoding="utf-8", newline="\n") as f:
        f.write("# IC-1: o que o script não leu sozinho (para o Rodolfo conferir)\n\n")
        f.write("Gerado por `scripts/itens/gerar_itens.py`. %d itens no catálogo; %d linhas aqui.\n\n" % (len(itens), len(excecoes)))
        f.write("| Item | Página | O que conferir |\n|---|---|---|\n")
        for nome, pg, motivo in excecoes:
            f.write("| %s | %s | %s |\n" % (nome, pg, motivo))
    return por_tipo


if __name__ == "__main__":
    itens = gerar()
    if "--conferir" in sys.argv:
        atual = json.load(open(SAIDA, encoding="utf-8"))["itens"]
        if atual != json.loads(json.dumps(itens, ensure_ascii=False)):
            raise SystemExit("O catálogo no disco NÃO bate com o que o livro dá hoje. Rode sem --conferir e olhe o diff.")
        print("ok: %d itens, o catálogo bate com os PDFs" % len(itens))
    else:
        tipos = gravar(itens)
        print("%d itens -> %s" % (len(itens), SAIDA))
        print("por tipo:", json.dumps(tipos, ensure_ascii=False))
        print("%d exceções -> %s" % (len(excecoes), EXCECOES))
