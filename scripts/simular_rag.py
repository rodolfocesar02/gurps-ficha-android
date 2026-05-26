#!/usr/bin/env python3
"""
Simulador do pipeline RAG do Mestre IA.
Reproduz: FTS4 → BM25 → TopicIndex → Contexto Final
"""
import json, unicodedata, re, math, sys
from collections import defaultdict

PERGUNTA = (
    "estou voando atraz do meu inimigo, a uma distancia de 6 metros, "
    "nos estamos a 10hex por segundo! quero acertar um jato de chamas nele, "
    "qual o redutor de ataque?"
)

DICIONARIO_SINONIMOS = {
    "tiro": ["disparo", "arma", "fogo", "projetil", "acerto", "atirar"],
    "penalidade": ["modificador", "bonus", "malus", "reducao", "ajuste", "redutor"],
    "alcance": ["distancia", "range", "metro", "faixa"],
    "voar": ["voo", "voando", "aereo", "altitude", "elevado"],
    "chamas": ["fogo", "incendio", "calor", "queimar", "flamulas"],
    "jato": ["rajada", "sopro", "chama", "cone", "area"],
    "velocidade": ["rapido", "lento", "hex", "deslocamento", "sprint"],
    "ataque": ["acertar", "golpear", "atingir", "combate"],
    "redutor": ["penalidade", "modificador", "malus"],
    "movimento": ["deslocamento", "velocidade", "passo", "corrida"],
    "magia": ["feitico", "encantamento", "conjuracao", "energia", "mana"],
}

STOPWORDS = {
    "de", "da", "do", "em", "no", "na", "um", "uma", "os", "as",
    "que", "e", "o", "a", "eu", "ele", "ela", "nos", "eles",
    "por", "com", "se", "para", "ao", "aos", "das", "dos",
    "meu", "minha", "seu", "sua", "nos", "atraz", "qual",
}

ASSETS = "app/src/main/assets"


def normalizar(s: str) -> str:
    s = unicodedata.normalize("NFD", s)
    s = "".join(c for c in s if unicodedata.category(c) != "Mn")
    return re.sub(r"[^a-z0-9 ]", "", s.lower())


def token_match_fn(req: str, tokens: set) -> bool:
    return any(t.startswith(req) or (len(t) >= 5 and req.startswith(t)) for t in tokens)


# ──────────────────────────────────────────────────────────────────
print("╔══ SIMULADOR MESTRE IA ═══════════════════════════════════════")
print(f'║  Pergunta: "{PERGUNTA[:100]}"')
print("╠═══════════════════════════════════════════════════════════════")

# ── PASSO 1: PLANNER ────────────────────────────────────────────
print("║  [PASSO 1] PLANNER — extração de termos")

palavras_norm = normalizar(PERGUNTA).split()
termos_base = list(dict.fromkeys(
    w for w in palavras_norm if w not in STOPWORDS and len(w) >= 4
))

termos_expandidos = set(termos_base)
for t in termos_base:
    for chave, sinonimos in DICIONARIO_SINONIMOS.items():
        chave_n = normalizar(chave)
        if t in chave_n or chave_n in t or any(t == normalizar(s) for s in sinonimos):
            termos_expandidos.update(normalizar(s) for s in sinonimos)

# Sub-queries temáticas
sub_queries = [
    "jato chamas ataque redutor penalidade",
    "voando velocidade hex deslocamento ataque",
    "distancia alcance redutor modificador combate",
]

print(f"║    Termos base   ({len(termos_base)}): {termos_base}")
print(f"║    Expandidos  ({len(termos_expandidos)}): {sorted(termos_expandidos)}")
print(f"║    Sub-queries: {sub_queries}")

# ── PASSO 2: FTS4 ───────────────────────────────────────────────
print("╠═══════════════════════════════════════════════════════════════")
print("║  [PASSO 2] FTS4 — keyword match no corpus")

chunks = []
with open(f"{ASSETS}/chunks.jsonl", encoding="utf-8") as f:
    for line in f:
        chunks.append(json.loads(line))

def fts_match(chunk, termos):
    texto = normalizar(chunk.get("text", ""))
    return any(t in texto for t in termos)

pool_fts = [c for c in chunks if fts_match(c, termos_expandidos)][:200]
paginas_fts = sorted(set(c["page_number"] for c in pool_fts if c.get("page_number")))
print(f"║    FTS4: {len(pool_fts)} chunks | páginas: {paginas_fts[:40]}")

# ── PASSO 3: BM25 ───────────────────────────────────────────────
print("╠═══════════════════════════════════════════════════════════════")
print("║  [PASSO 3] BM25 — scoring de relevância")

k1, b = 1.5, 0.75
N = len(pool_fts)
avgdl = sum(len(c["text"]) for c in pool_fts) / max(N, 1)

idf_map = {}
for t in termos_base:
    df = sum(1 for c in pool_fts if t in normalizar(c["text"]))
    idf_map[t] = max(math.log((N - df + 0.5) / (df + 0.5) + 1.0), 0.01)

def bm25(chunk):
    texto = normalizar(chunk["text"])
    dl = len(texto)
    score = 0.0
    for t in termos_base:
        tf = texto.count(t)
        if tf == 0:
            continue
        idf = idf_map.get(t, 0.01)
        score += idf * (tf * (k1 + 1)) / (tf + k1 * (1 - b + b * dl / avgdl))
    # Bonus AND
    if len(termos_base) >= 2 and all(t in texto for t in termos_base):
        score += 15.0
    # Bonus proximidade
    for i, t1 in enumerate(termos_base):
        p1 = texto.find(t1)
        if p1 < 0:
            continue
        for t2 in termos_base[i + 1:]:
            p2 = texto.find(t2)
            if p2 >= 0 and abs(p1 - p2) < 100:
                score += 5.0
    # Penalidade páginas iniciais MB
    if (chunk.get("page_number") or 0) < 30 and chunk.get("source_id") == "pt_modulo_basico":
        score -= 0.5
    return score

scored = sorted([(c, bm25(c)) for c in pool_fts], key=lambda x: -x[1])

print("║    BM25 top-15:")
for c, s in scored[:15]:
    src = c["source_id"].replace("pt_", "")
    snippet = normalizar(c["text"])[:80].replace("\n", " ")
    print(f"║      p.{c['page_number']:>4} [{src[:10]}] ({s:5.1f}pts) — {snippet}")

# Diversificação por página (top-15 únicos)
vistos = set()
top15 = []
for c, s in scored[:50]:
    key = f"{c['source_id']}_{c['page_number']}"
    if key not in vistos:
        vistos.add(key)
        top15.append(c)
    if len(top15) >= 15:
        break

# ── PASSO 4: TOPIC INDEX ────────────────────────────────────────
print("╠═══════════════════════════════════════════════════════════════")
print("║  [PASSO 4] TOPIC INDEX — páginas garantidas")

tokens_orig = set(w for w in normalizar(PERGUNTA).split() if len(w) >= 4)
print(f"║    Tokens da pergunta (>=4 chars): {sorted(tokens_orig)}")

with open(f"{ASSETS}/topic_index.json", encoding="utf-8") as f:
    topic_data = json.load(f)

chunks_por_pagina = defaultdict(list)
for c in chunks:
    if c.get("page_number") and c.get("source_id"):
        chunks_por_pagina[(c["source_id"], c["page_number"])].append(c)

topicos_match = []
for topico in topic_data["topics"]:
    req = topico.get("require_all", [])
    fb = topico.get("fallback_any", [])
    match = False
    if req and all(token_match_fn(r, tokens_orig) for r in req):
        match = True
        reason = f"require_all={req}"
    elif fb:
        for par in fb:
            if all(token_match_fn(r, tokens_orig) for r in par):
                match = True
                reason = f"fallback_any={par}"
                break
    if match:
        topicos_match.append((topico, reason))

print(f"║    Tópicos disparados: {len(topicos_match)}")
for t, reason in topicos_match:
    pages_info = " | ".join(
        f"{p['source_id'].replace('pt_','')}:{p['pages']}" for p in t["pages"]
    )
    print(f"║      + {t['id']} ({reason})")
    print(f"║          → {pages_info}")

chunks_topic = []
for topico, _ in topicos_match:
    for pg_entry in topico["pages"]:
        src = pg_entry["source_id"]
        for pg in pg_entry["pages"]:
            clist = chunks_por_pagina.get((src, pg), [])
            chunks_topic.extend(clist)

# ── PASSO 5: MERGE FINAL ────────────────────────────────────────
print("╠═══════════════════════════════════════════════════════════════")
print("║  [PASSO 5] CONTEXTO FINAL (TopicIndex → BM25)")

ids_topic = {c["chunk_id"] for c in chunks_topic}
chunks_finais = list({c["chunk_id"]: c for c in chunks_topic}.values())
for c in top15:
    if c["chunk_id"] not in ids_topic:
        chunks_finais.append(c)

chunks_finais = chunks_finais[:30]

print(f"║    {len(chunks_finais)} chunks no contexto:")
for c in chunks_finais:
    src = c["source_id"].replace("pt_", "")
    origem = "★★★ TOPIC" if c["chunk_id"] in ids_topic else "   BM25"
    print(f"║    [{origem}] {src:<25} p.{c['page_number']}")

# ── PASSO 6: TEXTO DOS CHUNKS RELEVANTES ────────────────────────
print("╠═══════════════════════════════════════════════════════════════")
print("║  [PASSO 6] CHUNKS MAIS RELEVANTES — texto completo")

# Palavras-chave de interesse para esse cenário
KEYWORDS_INTERESSE = {
    "voando", "voo", "aereo", "altitude", "chamas", "fogo", "jato",
    "velocidade", "hex", "deslocamento", "redutor", "penalidade",
    "modificador", "distancia", "alcance", "ataque", "movimento",
    "magia", "feitico"
}

def relevancia_chunk(c):
    texto = normalizar(c["text"])
    return sum(1 for k in KEYWORDS_INTERESSE if k in texto)

chunks_ordenados = sorted(chunks_finais, key=relevancia_chunk, reverse=True)

for c in chunks_ordenados[:8]:
    src = c["source_id"].replace("pt_", "")
    hits = [k for k in KEYWORDS_INTERESSE if k in normalizar(c["text"])]
    print(f"\n║  ┌─ [{src}] p.{c['page_number']} | keywords: {hits}")
    # Mostrar o texto com destaque
    for linha in c["text"].strip().split("\n")[:25]:
        print(f"║  │ {linha}")
    print("║  └─────────────────────────────────────────")

print("\n╚══ FIM DA SIMULAÇÃO ══════════════════════════════════════════")
