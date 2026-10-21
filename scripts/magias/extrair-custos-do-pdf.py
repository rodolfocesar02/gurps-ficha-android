#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
O texto de CUSTO de cada magia, lido DIRETO do PDF de Magia (a edição que vale: 2025, Guilda dos Escribas) e gravado em
`custos-do-pdf.json`. Ninguém redigita o livro: o programa acha a entrada no texto corrido do PDF (as páginas em ordem, uma
entrada pode passar de uma página para a outra) e recorta do rótulo "Custo..." até o próximo campo ("Tempo de operação",
"Pré-requisito", "Item", "Duração").

Para que serve: o `chunks.jsonl` (texto antigo transcrito por IA) tem páginas velhas ou embaralhadas — a auditoria achou 78
de 221 páginas com número diferente do PDF, e o conversor página a página (`pdf-para-chunks.py`) ainda erra em layouts como a
p.150. Onde o `chunks.jsonl` não traz a linha de custo da entrada (Animação, Comandar Espírito, Expulsar, Drenar Mana,
Moldar Fogo...), a GUARDA do custo por condição (`custo-condicao.js`) confere a conta contra este arquivo.

    python -I scripts/magias/extrair-custos-do-pdf.py --pdf "<PDF de Magia>"            # grava custos-do-pdf.json
    python -I scripts/magias/extrair-custos-do-pdf.py --pdf "<PDF de Magia>" --ver id1 id2   # só mostra

Um nome de entrada no PDF que não é o do catálogo ("Controle de (Animal)" para as três categorias) entra em `APELIDOS`.
O arquivo é lido pelos testes em JS: eles não precisam do PDF.
"""
import argparse
import io
import json
import os
import re
import sys

import pymupdf

AQUI = os.path.dirname(os.path.abspath(__file__))
CATALOGO = os.path.normpath(os.path.join(AQUI, '..', '..', 'app', 'src', 'main', 'assets', 'magias2versao.json'))
SAIDA = os.path.join(AQUI, 'custos-do-pdf.json')

CLASSE = r'(?:Comum|Área|Bloqueio|Informação|Especial|Encantamento|Projétil|Toque|Bloq)'
ROTULO = (r'(?P<rotulo>Custo(?:\s+[Bb][áa]sico|\s+de\s+energia[^:]{0,25}|\s+e\s+[Tt]empo[^:]{0,30})?'
          r'|Duração,\s+Custo[^:]{0,40})\s*:')
FIM = r'(?= (?:Tempo de opera|Pré-requisito|Item |Item$|Duração:|Duração, |Resistência|Efeito:|Modificador))'

# O livro escreve UMA entrada para o grupo (Controle de (Animal)); o catálogo tem uma por categoria.
APELIDOS = {
    'controle_de_animal_terra': 'Controle de (Animal)', 'controle_de_animal_ar': 'Controle de (Animal)',
    'controle_de_animal_mar': 'Controle de (Animal)',
    'repelir_animal_terra': 'Repelir (Animal)', 'repelir_animal_ar': 'Repelir (Animal)', 'repelir_animal_mar': 'Repelir (Animal)',
    'geiser': 'Gêiser',
}


def junta(t):
    t = re.sub(r'-\n(?=[a-zà-ú])', '', t)
    t = re.sub(r'\s*\n\s*', ' ', t)
    return re.sub(r'\s+', ' ', t)


def texto_corrido(pdf):
    partes = []
    for i in range(min(222, len(pdf))):
        partes.append('⟦p%d⟧ ' % (i - 1) + junta(pdf[i].get_text()))
    return ' '.join(partes)


def achar_a_entrada(texto, nome, pagina):
    rx = re.compile(re.escape(nome).replace(r'\ ', r'\s+') + r'\*?(?:\s*\([^)]*\))?\s+' + CLASSE)
    melhor = None
    for mm in rx.finditer(texto):
        marcas = re.findall(r'⟦p(-?\d+)⟧', texto[:mm.start()])
        p = int(marcas[-1]) if marcas else 0
        if p > 215:          # a tabela-resumo no fim do livro repete os nomes
            continue
        if melhor is None or abs(p - pagina) < abs(melhor[1] - pagina):
            melhor = (mm.start(), p)
    return melhor


def extrair(texto, magia):
    nome = APELIDOS.get(magia['id']) or re.sub(r'\s*\(.*?\)\s*$', '', magia['nome']).replace('/NT', '').strip()
    achou = achar_a_entrada(texto, nome, magia.get('pagina') or 0)
    if not achou:
        return None
    ini, pagina = achou
    corpo = texto[ini:ini + 5200]
    # a entrada acaba onde a próxima começa: o primeiro "<Nome> <Classe>" depois do título (em linhas gerais)
    c = re.search(ROTULO + r'(.*?)' + FIM, corpo)
    if not c:
        return {'pagina': pagina, 'rotulo': None, 'texto': None}
    rotulo = 'basico' if re.search(r'[Bb][áa]sico', c.group('rotulo')) else 'custo'
    linha = c.group(0)
    linha = re.sub(r'⟦p-?\d+⟧', '', linha)
    linha = re.sub(r'^\s*[^:]*:\s*', '', linha, count=1)
    return {'pagina': pagina, 'rotulo': rotulo, 'texto': re.sub(r'\s+', ' ', linha).strip()}


def main():
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
    ap = argparse.ArgumentParser()
    ap.add_argument('--pdf', required=True)
    ap.add_argument('--ver', nargs='*', help='só mostra estas magias, sem gravar')
    a = ap.parse_args()
    catalogo = json.load(open(CATALOGO, encoding='utf-8'))
    pdf = pymupdf.open(a.pdf)
    texto = texto_corrido(pdf)
    saida = {}
    for m in catalogo:
        if a.ver and m['id'] not in a.ver:
            continue
        r = extrair(texto, m)
        if a.ver:
            print('%s: %s' % (m['id'], json.dumps(r, ensure_ascii=False)))
        elif r and r['texto']:
            saida[m['id']] = r
    if a.ver:
        return
    leia = ['Gerado por scripts/magias/extrair-custos-do-pdf.py a partir do PDF de Magia (edição de 2025). NÃO edite à mão.',
            'Serve de apoio à guarda do custo por condição onde o chunks.jsonl não traz a linha de custo da entrada.']
    out = {'_leia': leia}
    for k in sorted(saida):
        out[k] = saida[k]
    with open(SAIDA, 'w', encoding='utf-8', newline='\n') as f:
        json.dump(out, f, ensure_ascii=False, indent=1)
        f.write('\n')
    print('custos lidos do PDF: %d de %d magias' % (len(saida), len(catalogo)))


if __name__ == '__main__':
    main()
