# Motor NEXUS ARCANO

Status: planejamento ativo (isolado da ficha)

## Objetivo
Criar um motor determinístico de progressão de magias com foco em pré-requisitos reais, sem travar UI, com saída curta e previsível.

## Princípios
- Prioridade absoluta para cadeia obrigatória de pré-requisitos nomeados.
- Contadores por escola só entram após cadeia obrigatória mínima.
- Saída operacional curta: 3 próximas ações por rodada.
- Estado incremental por alvo (não recalcular tudo a cada seleção).

## Modelo de dados (novo)
- Bloco: conjunto de requisitos do mesmo tipo.
- Chave: condição booleana de desbloqueio de bloco/alvo.
- Ação: magia sugerida para progresso imediato.

## Blocos padrão
1. Bloco Cadeia Obrigatória
- Ex.: Desejo -> Pequeno Desejo -> Encantar.

2. Bloco Contadores de Escola
- Ex.: 1 magia em 15 escolas.

3. Bloco Atributos/AM
- Ex.: AM3, IQ 13+.

## Saída padrão do motor
- chavesAtivas: lista de chaves já cumpridas.
- chavesFaltantes: lista de chaves pendentes.
- proximasAcoes: até 3 ações válidas agora.
- motivoBloqueio: texto curto quando não houver ação imediata.

## Integração mínima com app
Entrada:
- magias do catálogo
- magias já aprendidas
- AM, IQ e atributos

Saída:
- até 3 recomendações
- progresso por bloco/chave

## Nome oficial
NEXUS ARCANO (Nexus Arcano de Progressão Mágica)
