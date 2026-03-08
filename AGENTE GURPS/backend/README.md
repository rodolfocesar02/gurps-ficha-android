# Backend RAG - AGENTE GURPS

Backend local para consulta RAG com base vetorial (ChromaDB) e resposta final em portugues.

## 1) Preparacao
```bash
cd "AGENTE GURPS/backend"
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
copy .env.example .env
```

Preencha `OPENAI_API_KEY` no `.env`.

## 2) Indexar base
```bash
python indexar_chunks_chroma.py
```

Saida:
1. indice em `AGENTE GURPS/index/chroma/`
2. relatorio em `AGENTE GURPS/sources/processed/reports/indexacao_chroma_report.json`

## 3) Subir API
```bash
uvicorn app:app --reload --port 8787
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
