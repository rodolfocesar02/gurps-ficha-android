"""P-8 — valida cada página convertida do PDF ANTES de aplicá-la ao `chunks.jsonl` (sem confiar no conversor).

    python -I scripts/magias/validar-paginas-do-pdf.py --pdf "<PDF de Magia>" --paginas 36,49,104   (ou --paginas todas)
                                                       [--fundir-bordas] [--quadro-largo] [--json saida.json]

Uma página só é "SEGURA" (pode ir para o `pdf-para-chunks.py --aplicar`) se passar nas DUAS provas:

  1. **Conteúdo** — o texto convertido, comparado com o texto CRU do PDF (sem a aba do capítulo e o número impresso, que o conversor tira de propósito):
     nenhum número se perde, nenhum entra, cada "Custo" aparece, e as palavras cobrem o cru (a hifenização é juntada; tolera 1,5%).
     Os números e as palavras podem estar todos lá e a página ainda estar EMBARALHADA (p.191: frases cortadas, entradas sem classe); por isso:
  2. **Estrutura** — cada título "### Nome" é seguido da classe em negrito; nenhum parágrafo começa no meio de uma frase (minúscula); nenhum campo
     (Duração, Custo, Tempo, Pré-requisitos) se repete na mesma entrada; nenhum parágrafo solto fica entre dois campos da mesma entrada.
     (O PRIMEIRO parágrafo da página, antes de qualquer título, pode começar no meio da frase: é a entrada que vem da página anterior.)
     Calibrada nas páginas já refeitas e aprovadas antes (26, 37, 46, 75, 91, 112, 117, 118, 134, 142, 144, 161, 166, 168, 185): todas zeradas.

Uma página que NÃO passa fica com o texto antigo até alguém olhar o ensaio (`pdf-para-chunks.py --paginas N --mostrar`) e decidir (as aberturas de
capítulo com "historinha" e os quadros coloridos de regra são os casos difíceis).
"""
import argparse
import importlib.util
import json
import os
import re
import sys
from collections import Counter

sys.stdout.reconfigure(encoding="utf-8")
AQUI = os.path.dirname(os.path.abspath(__file__))
spec = importlib.util.spec_from_file_location("conv", os.path.join(AQUI, "pdf-para-chunks.py"))
conv = importlib.util.module_from_spec(spec)
spec.loader.exec_module(conv)

NUM = re.compile(r"\d+(?:[.,]\d+)?")
PAL = re.compile(r"[A-Za-zÀ-ÿ]+")
QUEBRA = chr(10)
CAMPO = re.compile(r"^\*\*(Dura[çc][ãa]o|Custo[^:*]*|Tempo de opera[çc][ãa]o|Pr[ée]-requisitos?)[^*]*\*\*")


def sem_marcas(t):
    return re.sub(r"[#*|]", " ", t)


def texto_cru(pagina):
    partes = []
    for bloco in pagina.get_text("dict")["blocks"]:
        if bloco["type"] != 0:
            continue
        for l in bloco["lines"]:
            partes.append(" ".join(sp["text"] for sp in l["spans"] if sp["color"] != conv.BRANCO))
    return re.sub(r"[-­]\s*" + QUEBRA + r"\s*", "", QUEBRA.join(partes))


def conteudo(cru, corpo):
    nc, nn = Counter(NUM.findall(cru)), Counter(NUM.findall(sem_marcas(corpo)))
    perdeu = {k: v - nn.get(k, 0) for k, v in nc.items() if v > nn.get(k, 0)}
    inventou = {k: v - nc.get(k, 0) for k, v in nn.items() if v > nc.get(k, 0)}
    pc, pn = Counter(w.lower() for w in PAL.findall(cru)), Counter(w.lower() for w in PAL.findall(sem_marcas(corpo)))
    faltam = sum(v - pn.get(k, 0) for k, v in pc.items() if v > pn.get(k, 0))
    total = sum(pc.values())
    custo = (len(re.findall("Custo", cru)), len(re.findall("Custo", corpo)))
    ok = not perdeu and not inventou and custo[0] == custo[1] and faltam <= max(3, int(total * 0.015))
    return ok, dict(perdeu=perdeu, inventou=inventou, palavras_faltando=faltam, palavras=total, custo=custo)


def estrutura(md):
    sem_classe = fragmentos = repetidos = soltos = 0
    achados = []
    for n_sec, sec in enumerate(re.split(r"^---$", md, flags=re.M)):
        linhas = [l for l in sec.splitlines() if l.strip() and not l.startswith("# Página")]
        if not linhas:
            continue
        # o começo da página, antes do primeiro título, continua a entrada da página ANTERIOR: lá, começar no meio da frase é o livro
        continuacao = n_sec == 0 and not linhas[0].startswith("### ")
        if linhas[0].startswith("### "):
            proxima = linhas[1] if len(linhas) > 1 else ""
            if not re.match(r"^\*\*[^*]+\*\*$", proxima.strip()) and not proxima.startswith(("|", "Modificadores")):
                sem_classe += 1
                achados.append("sem classe: " + linhas[0][4:40])
        idx = [i for i, l in enumerate(linhas) if CAMPO.match(l)]
        nomes = [CAMPO.match(linhas[i]).group(1)[:5] for i in idx]
        if len(nomes) != len(set(nomes)):
            repetidos += 1
            achados.append("campo repetido em: " + linhas[0][:40])
        item = next((i for i, l in enumerate(linhas) if l.strip() == "**Item**"), len(linhas))
        for i, l in enumerate(linhas):
            if l.startswith(("**", "|", "#", "-")):
                continue
            if re.match(r"^[a-zà-ÿ]", l):
                if continuacao and i == 0:
                    continue
                fragmentos += 1
                achados.append("começa no meio: " + l[:40])
            elif len(idx) >= 2 and idx[0] < i < idx[-1] and i < item:
                soltos += 1
                achados.append("solto entre campos: " + l[:40])
    ok = sem_classe == 0 and repetidos == 0 and fragmentos == 0 and soltos == 0
    return ok, dict(sem_classe=sem_classe, fragmentos=fragmentos, campos_repetidos=repetidos, soltos_entre_campos=soltos, achados=achados[:4])


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--pdf", required=True)
    ap.add_argument("--paginas", required=True, help="impressas, separadas por vírgula, ou 'todas'")
    ap.add_argument("--chunks", default="app/src/main/assets/chunks.jsonl")
    ap.add_argument("--fundir-bordas", action="store_true")
    ap.add_argument("--quadro-largo", action="store_true")
    ap.add_argument("--json")
    a = ap.parse_args()
    conv.FUNDIR_BORDAS, conv.QUADRO_LARGO = a.fundir_bordas, a.quadro_largo
    doc = conv.pymupdf.open(a.pdf)
    vocab = conv.vocabulario(a.chunks)
    impressas = list(range(2, 223)) if a.paginas == "todas" else [int(x) for x in a.paginas.split(",") if x.strip()]
    saida, seguras = {}, []
    for p in impressas:
        novo = conv.converter(doc, p, vocab)
        corpo = novo.split(QUEBRA, 1)[1] if QUEBRA in novo else novo
        ok_c, c = conteudo(texto_cru(doc[conv.pagina_do_pdf(doc, p)]), corpo)
        ok_e, e = estrutura(novo)
        saida[p] = dict(segura=ok_c and ok_e, conteudo=c, estrutura=e)
        if ok_c and ok_e:
            seguras.append(p)
        else:
            motivo = []
            if not ok_c:
                motivo.append("conteúdo: perdeu=%s inventou=%s palavras_faltando=%d/%d custo=%s" % (c["perdeu"], c["inventou"], c["palavras_faltando"], c["palavras"], c["custo"]))
            if not ok_e:
                motivo.append("estrutura: %s" % {k: v for k, v in e.items() if v})
            print("  p.%d NÃO é segura | %s" % (p, " | ".join(motivo)))
    print("SEGURAS (%d de %d): %s" % (len(seguras), len(impressas), ",".join(str(p) for p in seguras)))
    if a.json:
        with open(a.json, "w", encoding="utf-8") as f:
            json.dump({str(k): v for k, v in saida.items()}, f, ensure_ascii=False, indent=1)


if __name__ == "__main__":
    main()
