# AGENTE GURPS

Esta pasta concentra toda a implementacao do agente de IA de suporte ao sistema GURPS.

Objetivo:
1. Responder duvidas de regras (custos, niveis, pre-requisitos, testes).
2. Apoiar criacao de ficha, lore e background.
3. Trabalhar com fontes em portugues e ingles, sempre entregando resposta final em portugues.

Estrutura:
1. `docs/` - especificacoes, politicas e planos por lote.
2. `sources/` - materiais de referencia (nao versionar conteudo sem permissao).
3. `scripts/` - ingestao, validacao e utilitarios de RAG.
4. `backend/` - API FastAPI (`/health`, `/ask`) e deploy cloud (Railway).

Regras basicas:
1. Resposta final sempre em portugues.
2. Quando houver fonte canonica, citar fonte/pagina.
3. Quando for inferencia, marcar explicitamente como inferencia.

Deploy cloud:
1. Guia oficial do backend: `AGENTE GURPS/backend/README.md`.
2. Subir em servico separado na Railway (nao misturar com bot de dados).
