#!/usr/bin/env python3
"""
Teste de Context Caching com Gemini 1.5 Flash.
Fluxo: carrega chunks.jsonl → cria cache → faz pergunta → mostra resposta.
"""
import json, time, urllib.request, urllib.error

API_KEY   = "AIzaSyCxA2B049n3b5qGiLBXloZVo_mCwwx_cXk"  # gemini1.key — conta paga
MODEL     = "models/gemini-2.5-flash"
BASE_URL  = "https://generativelanguage.googleapis.com/v1beta"
ASSETS    = "app/src/main/assets/chunks.jsonl"

PERGUNTA  = (
    "Estou atirando num alvo dentro de uma piscina com meu revolver .45, "
    "o alvo esta submerso a 4 metros, qual redutor no ataque e dano?"
)

SYSTEM = (
    "Você é o Mestre IA de GURPS 4ª edição. "
    "Responda SOMENTE com base no conteúdo dos livros fornecidos no contexto. "
    "Cite sempre o livro e a página de onde veio a regra. "
    "Se a regra envolver cálculo, mostre o passo a passo explícito. "
    "Se a informação não estiver no contexto, diga claramente que não encontrou."
)

# ── helpers ─────────────────────────────────────────────────────────────────

def post(url, body: dict) -> dict:
    data = json.dumps(body).encode()
    req  = urllib.request.Request(url, data=data, headers={"Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(req, timeout=120) as r:
            return json.loads(r.read())
    except urllib.error.HTTPError as e:
        err = e.read().decode()
        print(f"\n[ERRO HTTP {e.code}] {err[:600]}")
        raise

def get(url) -> dict:
    with urllib.request.urlopen(url, timeout=30) as r:
        return json.loads(r.read())

# ── PASSO 1: carregar texto dos chunks ──────────────────────────────────────

print("╔══ TESTE CONTEXT CACHE — GEMINI 1.5 FLASH ════════════════════════")
print("║  [1] Carregando chunks.jsonl...")

# Carrega apenas fontes que cabem no limite de 1M tokens
FONTES_INCLUIDAS = {"pt_magia", "pt_pyramid_26_underwater", "pt_gun_fu"}

textos = []
contagem = {}
with open(ASSETS, encoding="utf-8") as f:
    for line in f:
        obj = json.loads(line)
        src  = obj.get("source_id", "?")
        if src not in FONTES_INCLUIDAS:
            continue
        pg   = obj.get("page_number", "?")
        text = obj.get("text", "").strip()
        if text:
            label = src.replace("pt_", "")
            textos.append(f"[{label} | pág.{pg}]\n{text}")
            contagem[src] = contagem.get(src, 0) + 1

corpus = "\n\n---\n\n".join(textos)
print(f"║    {len(textos)} chunks carregados | {len(corpus)/1024/1024:.1f} MB | ~{len(corpus)//4:,} tokens")
print(f"║    Fontes: {contagem}")

# ── PASSO 2: enviar corpus + pergunta direto ──────────────────────────────────

print("╠═══════════════════════════════════════════════════════════════════")
print(f'║  [2] Enviando {len(textos)} chunks + pergunta direto no request...')
print(f'║  Pergunta: "{PERGUNTA}"')

query_url  = f"{BASE_URL}/{MODEL}:generateContent?key={API_KEY}"
query_body = {
    "systemInstruction": {"parts": [{"text": SYSTEM}]},
    "contents": [
        {"role": "user",  "parts": [{"text": f"BASE DE CONHECIMENTO GURPS:\n\n{corpus}"}]},
        {"role": "model", "parts": [{"text": "Entendido. Pode perguntar."}]},
        {"role": "user",  "parts": [{"text": PERGUNTA}]}
    ],
    "generationConfig": {"temperature": 0.1, "maxOutputTokens": 4096}
}

t1 = time.time()
resp_query = post(query_url, query_body)
elapsed2   = time.time() - t1

# ── PASSO 3: exibir resposta ─────────────────────────────────────────────────

print("╠═══════════════════════════════════════════════════════════════════")
print(f"║  [3] Resposta recebida em {elapsed2:.1f}s")

usage = resp_query.get("usageMetadata", {})
print(f"║    promptTokens    : {usage.get('promptTokenCount', '?')}")
print(f"║    cachedTokens    : {usage.get('cachedContentTokenCount', '?')}")
print(f"║    responseTokens  : {usage.get('candidatesTokenCount', '?')}")
print("╠═══════════════════════════════════════════════════════════════════")

candidates = resp_query.get("candidates", [])
if candidates:
    parts = candidates[0].get("content", {}).get("parts", [])
    resposta = "".join(p.get("text", "") for p in parts)
    print("\n  RESPOSTA DO MESTRE IA:\n")
    for linha in resposta.strip().split("\n"):
        print(f"  {linha}")
else:
    print("║  [ERRO] Nenhum candidato na resposta.")
    print(json.dumps(resp_query, indent=2, ensure_ascii=False)[:800])

print("\n╚══ FIM DO TESTE ═══════════════════════════════════════════════════")
