# Backend RAG - AGENTE GURPS

Backend local para consulta RAG com base vetorial (ChromaDB) e resposta final em portugues.

## 1) Preparacao
```bash
cd "AGENTE GURPS/backend"
py -3.11 -m venv .venv311
.venv311\Scripts\activate
pip install -r requirements.txt
copy .env.example .env
```

Preencha `OPENAI_API_KEY` no `.env`.

Exemplo Google AI Studio (OpenAI-compatible):
```env
OPENAI_API_KEY=(COLE_AQUI_A_SUA_KEY)
OPENAI_BASE_URL=https://generativelanguage.googleapis.com/v1beta/openai/
OPENAI_CHAT_MODEL=gemini-2.0-flash
OPENAI_EMBED_MODEL=text-embedding-004
```

## 2) Indexar base
```bash
python indexar_chunks_chroma.py
```

Saida:
1. indice em `AGENTE GURPS/index/chroma/`
2. relatorio em `AGENTE GURPS/sources/processed/reports/indexacao_chroma_report.json`

## 3) Subir API
```bash
uvicorn api_server:app --reload --port 8787
```

Teste rapido local:
```bash
curl -X POST "http://127.0.0.1:8787/ask" ^
  -H "Content-Type: application/json" ^
  -d "{\"question\":\"O que e Aptidao Magica no GURPS?\",\"mode\":\"regras\",\"top_k\":6}"
```

Endpoints:
1. `GET /health`
2. `POST /ask`

Exemplo de request:
```json
{
  "question": "Qual o custo base de Aptidao Magica no GURPS 4a edicao?",
  "mode": "regras",
  "top_k": 6
}
```
