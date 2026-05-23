"""
Lote 265: Gera topic_index.json automaticamente a partir do indice.md e glossario.md do MB.

Uso:
  python scripts/gerar_topic_index.py

Saida:
  app/src/main/assets/topic_index_gerado.json   (NÃO sobrescreve o topic_index.json manual)

Após revisar, mesclar manualmente ou via:
  python scripts/gerar_topic_index.py --merge
"""

import json
import re
import unicodedata
import sys
from pathlib import Path

# ── Caminhos ──────────────────────────────────────────────────────────────────
ROOT = Path(__file__).parent.parent
INDICE_MD   = ROOT / "indice.md"
GLOSSARIO_MD = ROOT / "glossario.md"
TOPIC_INDEX_MANUAL = ROOT / "app/src/main/assets/topic_index.json"
TOPIC_INDEX_SAIDA  = ROOT / "app/src/main/assets/topic_index_gerado.json"

SOURCE_ID = "pt_modulo_basico"

# ── Entradas para IGNORAR (referências cruzadas, ilustrações, planilhas) ──────
IGNORAR_PREFIXOS = [
    "veja também", "veja", "ilustração", "planilha de personagem",
    "tabela de reações", "cartão de registro", "ludografia",
    "planilha de planejamento", "planilha de uso", "planilha de controle",
    "índice", "glossário", "introdução", "david pulver", "sean punch",
    "steve jackson", "barão janos", "dai blackthorn", "louis d'antares",
    "professor william", "iotha", "xing la", "sora", "c-31",
]

# ── Sinônimos manuais para termos específicos ─────────────────────────────────
SINONIMOS_EXTRAS = {
    "queda": ["cair", "precipicio", "altitude", "despencar", "tombo"],
    "fogo": ["chama", "queimadura", "incendio", "calor", "fogueira"],
    "asfixia": ["sufocamento", "afogamento", "respiracao", "folego"],
    "carga": ["encumbrance", "peso", "carregando", "sobrecarga"],
    "magia": ["feitico", "encantamento", "conjuracao", "mana"],
    "esquiva": ["desviar", "esquivar", "evadir"],
    "bloqueio": ["bloquear", "escudo", "capa"],
    "aparar": ["parry", "aparada", "defesa"],
    "dano": ["ferimento", "lesao", "prejuizo"],
    "alcance": ["distancia", "range", "metros"],
    "tiro": ["disparo", "atirar", "arma de fogo", "projétil"],
    "natacao": ["nadar", "aquatico", "aquático", "agua"],
    "veneno": ["toxico", "intoxicacao", "envenenamento"],
    "cura": ["recuperacao", "primeiros socorros", "medicina"],
    "morte": ["morrer", "morto", "fatal", "óbito"],
    "atordoamento": ["atordoado", "stunned", "tonto"],
    "movimento": ["deslocamento", "mover", "velocidade"],
    "iniciativa": ["turno", "ordem de combate"],
    "cobertura": ["abrigo", "obstaculo", "parcial"],
    "posicao": ["ajoelhado", "deitado", "agachado", "sentado", "postura"],
    "manobra": ["acao", "turno", "combate"],
    "defesa": ["defender", "defesa ativa", "aparar", "esquivar"],
}

# ── Normalização ──────────────────────────────────────────────────────────────
def normalizar(texto):
    """Remove acentos, caixa baixa, strip."""
    nfkd = unicodedata.normalize("NFKD", texto)
    sem_acento = "".join(c for c in nfkd if not unicodedata.combining(c))
    return sem_acento.lower().strip()

# ── Parser de páginas ─────────────────────────────────────────────────────────
def extrair_paginas(texto):
    """
    Extrai todos os números de página de uma string.
    Expande intervalos: "418–425" → [418, 419, 420, 421, 422, 423, 424, 425]
    Limita a páginas do MB (1–575).
    """
    paginas = set()
    # Intervalos com – ou -
    for m in re.finditer(r'(\d{1,3})\s*[–\-]\s*(\d{1,3})', texto):
        inicio, fim = int(m.group(1)), int(m.group(2))
        if 1 <= inicio <= 575 and 1 <= fim <= 575 and fim >= inicio:
            paginas.update(range(inicio, fim + 1))
    # Números soltos
    for m in re.finditer(r'\b(\d{1,3})\b', texto):
        n = int(m.group(1))
        if 1 <= n <= 575:
            paginas.add(n)
    return sorted(paginas)

# ── Parser do indice.md ───────────────────────────────────────────────────────
def parse_indice(caminho):
    """
    Retorna lista de (termo, [páginas]).
    Cada entrada termina com '.'.
    """
    texto = caminho.read_text(encoding="utf-8")

    # Juntar linhas quebradas (sem ponto no final) — exceto linhas em branco
    linhas = texto.splitlines()
    bloco = []
    entrada_atual = ""
    for linha in linhas:
        linha = linha.strip()
        if not linha:
            if entrada_atual:
                bloco.append(entrada_atual)
                entrada_atual = ""
            continue
        if entrada_atual:
            entrada_atual += " " + linha
        else:
            entrada_atual = linha
        if linha.endswith("."):
            bloco.append(entrada_atual)
            entrada_atual = ""
    if entrada_atual:
        bloco.append(entrada_atual)

    entradas = []
    for linha in bloco:
        linha = linha.strip().rstrip(".")
        if not linha:
            continue

        # Ignorar cabeçalho e linhas sem vírgula+número
        if not re.search(r'\d', linha):
            continue

        # Separar termo das referências: "Termo principal, páginas; sub, páginas."
        partes = linha.split(",", 1)
        if len(partes) < 2:
            continue

        termo = partes[0].strip()

        # Ignorar entradas de referência cruzada
        termo_norm = normalizar(termo)
        ignorar = False
        for prefixo in IGNORAR_PREFIXOS:
            if termo_norm.startswith(normalizar(prefixo)):
                ignorar = True
                break
        if ignorar:
            continue

        # Ignorar termos muito curtos ou siglas soltas
        if len(termo) < 3:
            continue

        resto = partes[1]
        # Ignorar se começa com "veja"
        if normalizar(resto.strip()).startswith("veja"):
            continue

        paginas = extrair_paginas(resto)
        # Precisa ter pelo menos 1 página válida
        if not paginas:
            continue

        # Filtrar páginas muito baixas (prefácio, créditos) exceto se realmente úteis
        paginas_uteis = [p for p in paginas if p >= 7]
        if not paginas_uteis:
            continue

        entradas.append((termo, paginas_uteis))

    return entradas

# ── Parser do glossario.md ────────────────────────────────────────────────────
def parse_glossario(caminho):
    """
    Retorna dict: { termo_normalizado: (termo_original, página) }
    """
    texto = caminho.read_text(encoding="utf-8")
    resultado = {}

    # Padrão: "termo: definição... Pág. N." ou "Termo (SIGLA): ... Pág. N."
    for m in re.finditer(r'^([A-ZÀ-Úa-zà-ú][^\n:]{1,60}):\s+(.+?)(?:P[aá]g\.\s*(\d+))?\.?\s*$',
                          texto, re.MULTILINE):
        termo = m.group(1).strip()
        pagina_str = m.group(3)
        if pagina_str:
            try:
                pagina = int(pagina_str)
                if 7 <= pagina <= 575:
                    resultado[normalizar(termo)] = (termo, pagina)
            except ValueError:
                pass

    return resultado

# ── Geração de keywords ───────────────────────────────────────────────────────
def gerar_keywords(termo, glossario):
    """
    Gera lista de keywords para um termo do índice.
    1. Palavras do próprio termo (sem stopwords)
    2. Sinônimos do glossário se o termo aparecer lá
    3. Sinônimos manuais adicionais
    """
    STOPWORDS = {"de", "do", "da", "dos", "das", "e", "em", "no", "na",
                 "nos", "nas", "a", "o", "os", "as", "com", "por", "para",
                 "um", "uma", "uns", "umas", "ou", "que", "se", "ao", "à"}

    palavras = [p for p in normalizar(termo).split() if p not in STOPWORDS and len(p) > 2]
    keywords = list(dict.fromkeys(palavras))  # dedup preservando ordem

    # Adicionar sinônimos manuais
    for palavra in palavras:
        if palavra in SINONIMOS_EXTRAS:
            for s in SINONIMOS_EXTRAS[palavra]:
                s_norm = normalizar(s)
                if s_norm not in keywords:
                    keywords.append(s_norm)

    # Adicionar do glossário
    termo_norm = normalizar(termo)
    if termo_norm in glossario:
        _, pagina_gloss = glossario[termo_norm]
        # O glossário confirma o termo — não adiciona keywords novas aqui,
        # apenas valida que o termo é real

    # Limitar a 10 keywords para não poluir o matching
    return keywords[:10]

# ── Geração de require_all e fallback_any ─────────────────────────────────────
def gerar_matching(keywords, termo_original):
    """
    require_all: todas as palavras significativas do termo original (não sinônimos)
    fallback_any: pares das primeiras 2 keywords com sinônimos
    """
    if not keywords:
        return [], []

    STOPWORDS = {"de", "do", "da", "dos", "das", "e", "em", "no", "na",
                 "nos", "nas", "a", "o", "os", "as", "com", "por", "para",
                 "um", "uma", "uns", "umas", "ou", "que", "se", "ao", "à"}

    # require_all: palavras do TERMO ORIGINAL (não sinônimos) — mais específico
    palavras_termo = [normalizar(p) for p in termo_original.split()
                      if normalizar(p) not in STOPWORDS and len(p) > 2]

    if len(palavras_termo) >= 2:
        # Termo composto: exige todas as palavras do termo
        require_all = palavras_termo
    elif palavras_termo:
        # Termo simples: exige só essa palavra
        require_all = [palavras_termo[0]]
    else:
        require_all = [keywords[0]] if keywords else []

    # fallback_any: combina keyword principal com sinônimos
    principal = palavras_termo[0] if palavras_termo else keywords[0]
    fallback = []
    for k in keywords[1:4]:  # primeiros sinônimos
        if k != principal:
            fallback.append([principal, k])

    return require_all, fallback

# ── Gerar ID do tópico ────────────────────────────────────────────────────────
def gerar_id(termo):
    norm = normalizar(termo)
    # Substituir espaços e caracteres especiais por _
    id_ = re.sub(r'[^a-z0-9]+', '_', norm).strip('_')
    return id_[:50]

# ── Deduplicação por páginas similares ───────────────────────────────────────
def deduplicar(topicos):
    """Remove tópicos que são subconjunto exato de outro com mesmo require_all."""
    vistos = {}
    resultado = []
    for t in topicos:
        chave = (tuple(t["require_all"]), tuple(t["pages"][0]["pages"]))
        if chave not in vistos:
            vistos[chave] = True
            resultado.append(t)
    return resultado

# ── Main ──────────────────────────────────────────────────────────────────────
def main():
    print(f"Lendo {INDICE_MD}...")
    entradas_indice = parse_indice(INDICE_MD)
    print(f"  {len(entradas_indice)} entradas encontradas no índice")

    print(f"Lendo {GLOSSARIO_MD}...")
    glossario = parse_glossario(GLOSSARIO_MD)
    print(f"  {len(glossario)} termos no glossário")

    # Carregar topic_index manual para não duplicar IDs
    ids_existentes = set()
    if TOPIC_INDEX_MANUAL.exists():
        with open(TOPIC_INDEX_MANUAL, encoding="utf-8") as f:
            manual = json.load(f)
        ids_existentes = {t["id"] for t in manual.get("topics", [])}
        print(f"  {len(ids_existentes)} tópicos já existem no topic_index manual")

    topicos = []
    ids_gerados = set()

    for termo, paginas in entradas_indice:
        keywords = gerar_keywords(termo, glossario)
        if not keywords:
            continue

        require_all, fallback_any = gerar_matching(keywords, termo)

        id_ = gerar_id(termo)
        # Evitar colisão de IDs
        if id_ in ids_gerados:
            id_ = id_ + "_2"
        ids_gerados.add(id_)

        topico = {
            "id": id_,
            "keywords": keywords,
            "require_all": require_all,
            "fallback_any": fallback_any,
            "pages": [
                {"source_id": SOURCE_ID, "pages": paginas}
            ],
            "nota": f"Gerado do índice MB: '{termo}' → p.{paginas[0] if len(paginas)==1 else str(paginas[0])+'-'+str(paginas[-1])}"
        }
        topicos.append(topico)

    topicos = deduplicar(topicos)
    print(f"\n{len(topicos)} tópicos gerados")

    # Verificar se --merge foi passado
    merge = "--merge" in sys.argv

    if merge:
        # Mesclar com o manual, mantendo os manuais e adicionando os novos
        with open(TOPIC_INDEX_MANUAL, encoding="utf-8") as f:
            manual_json = json.load(f)

        ids_manuais = {t["id"] for t in manual_json["topics"]}
        novos = [t for t in topicos if t["id"] not in ids_manuais]
        manual_json["topics"].extend(novos)
        manual_json["version"] = manual_json.get("version", 1) + 1

        with open(TOPIC_INDEX_MANUAL, "w", encoding="utf-8") as f:
            json.dump(manual_json, f, ensure_ascii=False, indent=2)

        print(f"Mesclado: +{len(novos)} topicos novos -> {TOPIC_INDEX_MANUAL}")
        print(f"Total: {len(manual_json['topics'])} tópicos")
    else:
        # Salvar separado para revisão
        saida = {"version": 2, "topics": topicos}
        with open(TOPIC_INDEX_SAIDA, "w", encoding="utf-8") as f:
            json.dump(saida, f, ensure_ascii=False, indent=2)

        print(f"\nSalvo em: {TOPIC_INDEX_SAIDA}")
        print("Para mesclar com o topic_index.json manual:")
        print("  python scripts/gerar_topic_index.py --merge")

    # Estatísticas
    todas_paginas = set()
    for t in topicos:
        for p in t["pages"]:
            todas_paginas.update(p["pages"])
    print(f"\nEstatísticas:")
    print(f"  Tópicos gerados: {len(topicos)}")
    print(f"  Páginas cobertas: {len(todas_paginas)} ({min(todas_paginas)}–{max(todas_paginas)})")
    print(f"  Média keywords/tópico: {sum(len(t['keywords']) for t in topicos)/len(topicos):.1f}")

if __name__ == "__main__":
    main()
