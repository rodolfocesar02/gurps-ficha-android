# Lote A2 - Base de Conhecimento e Pipeline de Ingestao

Status: `EM ANDAMENTO`

## Objetivo
Preparar o pipeline para receber livros em portugues e ingles, gerar base pesquisavel (RAG) e manter rastreabilidade por fonte/pagina.

## Entradas
1. Arquivos em `AGENTE GURPS/sources/raw/`.
2. Manifesto de fontes em `AGENTE GURPS/sources/manifesto_fontes.csv`.

## Pipeline previsto
1. Validar manifesto e permissao de uso.
2. Extrair texto por pagina (PDF/OCR quando necessario).
3. Normalizar texto (limpeza de ruído, metadados e idioma).
4. Quebrar em chunks com referencia de pagina.
5. Gerar embeddings e indexar no vetor DB.
6. Exportar indice de fontes para auditoria.

## Regras de saida do agente
1. Resposta final sempre em portugues.
2. Citar fonte/pagina quando houver.
3. Se fonte original estiver em ingles, mostrar traducao em portugues e indicar traducao.

## Entregaveis A2 (MVP tecnico)
1. Manifesto de fontes com campos minimos.
2. Script de validacao do manifesto.
3. Estrutura de pastas `raw/`, `processed/`, `index/`.
