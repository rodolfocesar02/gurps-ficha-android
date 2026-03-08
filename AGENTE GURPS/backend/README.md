# Backend RAG - AGENTE GURPS

Backend para consulta RAG com base vetorial (ChromaDB) e resposta final em portugues.

## 1) Preparacao local
```bash
cd "AGENTE GURPS/backend"
py -3.11 -m venv .venv311
.venv311\Scripts\activate
pip install -r requirements.txt
copy .env.example .env
```

Exemplo para Google AI Studio (OpenAI-compatible):
```env
OPENAI_API_KEY=(COLE_AQUI_A_SUA_KEY)
OPENAI_BASE_URL=https://generativelanguage.googleapis.com/v1beta/openai/
OPENAI_CHAT_MODEL=gemini-2.5-flash
OPENAI_EMBED_MODEL=text-embedding-004
```

## 2) Indexar base manualmente (opcional)
```bash
python indexar_chunks_chroma.py
```

Saida:
1. indice em `AGENTE GURPS/index/chroma/`
2. relatorio em `AGENTE GURPS/sources/processed/reports/indexacao_chroma_report.json`

## 3) Subir API local
```bash
uvicorn api_server:app --reload --port 8787
```

Teste rapido:
```bash
curl -X POST "http://127.0.0.1:8787/ask" ^
  -H "Content-Type: application/json" ^
  -d "{\"question\":\"O que e Aptidao Magica no GURPS?\",\"mode\":\"regras\",\"top_k\":6}"
```

Endpoints:
1. `GET /health`
2. `POST /ask`

## 4) Deploy na Railway (servico separado)
Use este repositorio em um servico dedicado do AGENTE (nao misturar com o bot de dados).

Build Command:
```bash
pip install -r "AGENTE GURPS/backend/requirements.txt"
```

Start Command:
```bash
python "AGENTE GURPS/backend/start_railway.py"
```

Variaveis obrigatorias na Railway:
```env
OPENAI_API_KEY=(COLE_AQUI_A_SUA_KEY)
OPENAI_BASE_URL=https://generativelanguage.googleapis.com/v1beta/openai/
OPENAI_CHAT_MODEL=gemini-2.5-flash
OPENAI_EMBED_MODEL=text-embedding-004
RAG_TOP_K=6
RAG_COLLECTION=gurps_pt_v1
RAG_AUTO_INDEX_ON_STARTUP=1
CHROMA_DIR=AGENTE GURPS/index/chroma
CHUNKS_FILE=AGENTE GURPS/sources/processed/chunks.jsonl
```

Observacoes:
1. Em cloud nova, se o indice estiver vazio, o backend reindexa automaticamente no startup.
2. O primeiro boot pode demorar mais por causa da indexacao inicial.
3. Healthcheck recomendado: `GET /health`.
