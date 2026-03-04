# PROGRESS - GURPS Ficha Android

Atualizado em: 2026-03-04

## Estado Atual
- Branch: `main`
- Variantes: `visual` e `pracego`
- Build: estável nas variantes principais
- Foco: estabilidade da ficha e evolução do motor externo `NEXUS ARCANO`
- Pasta do motor externo: `motor modo alvo/`
- Checkpoint consolidado: suíte Lote 2 separada e verde em teste unitário.

## Feito
- Catálogo de magias com ajustes e normalizações recentes (incluindo escola `Animais` por caminhos).
- Regras especiais de magias ajustadas para caminhos `Ar/Terra/Mar` quando aplicável.
- Fluxo da ficha estabilizado após reinício/validação do emulador.
- Organização de trabalho separada criada em `motor modo alvo/` para evolução do novo motor.

## Lotes (divisão por partes)
### Lote 1 - Núcleo de Chaves e Blocos
Partes implementadas:
- Contratos base definidos (`ArcanoEstadoPersonagem`, `ArcanoChave`, `ArcanoAcao`, `ArcanoResultado`).
- Núcleo inicial implementado em `motor modo alvo/src/NexusArcanoEngine.kt`.
- Regra hard-first aplicada: cadeia obrigatória antes de contadores de escola.
- Extração inicial de dependências nomeadas e regra `N mágicas em X escolas`.
- Extração e validação inicial de pré-requisitos numéricos (`AM`/`IQ`).
- Bloqueio numérico evita sugestão falsa de escola quando falta atributo.
- Suporte inicial a pré-requisito numérico composto (ex.: `(DX+IQ):30+`).
- Filtro inicial de ambiguidade nome/escola em dependências nomeadas.
- Filtro de sobreposição em dependências nomeadas (evita curto+longo simultâneo).
- Testes automatizados do Lote 1 implementados (`Desejo`, `Desejo Superior`, `Teleporte`, `Convocar Demônio`, `Translocação`):
  - `app/src/test/java/nexus/arcano/NexusArcanoEngineLote1Test.kt`
  - suíte verde em `:app:testVisualDebugUnitTest --tests nexus.arcano.NexusArcanoEngineLote1Test`
- API de motivo estável inicial implementada no resultado:
  - `motivoBloqueio` (mensagem)
  - `motivoCodigo` (`NUMERIC_GATE`, `SCHOOL_COUNT_PENDING`, `CHAIN_PENDING`, `TARGET_PENDING`, `UNKNOWN_BLOCK`)
- Saída inicial com chaves ativas/faltantes + até 3 ações.
- Smoke-check manual criado em `motor modo alvo/lotes/lote-01-smoke-check.md`.

Partes faltantes:
- Expandir suporte de atributos compostos além de `DX`/`IQ` no estado atual.
- Refinar ambiguidade nome/escola para cenários mais complexos de texto.
- Refinar matriz de códigos de bloqueio para cenários combinados.

### Lote 2 - Planejador de 3 Próximas Ações
Partes implementadas:
- Limite de saída em até 3 ações já considerado no núcleo inicial.
- Desempate determinístico por prioridade e nome normalizado.
- Fallback de escolas evita sugerir alvo/cadeia pendente como ação lateral.
- Priorização com custo inicial de desbloqueio (dependências/número/escolas) aplicada ao ranking.
- Fallback robusto para completar até 3 ações quando faltarem escolas novas.
- Testes do planejador extraídos para suíte dedicada do Lote 2:
  - `app/src/test/java/nexus/arcano/NexusArcanoEngineLote2Test.kt`
  - cobertura atual: `até 3 ações`, `ordem determinística`, `não sugerir alvo/cadeia como lateral`, `fallback final`, `empate de custo`.
- Fallback em 3 passos com deduplicação de escola por rodada:
  - escolas novas sem repetição;
  - fallback sem repetir escola;
  - último recurso com repetição permitida.
- Planejador agora sugere apenas magias aprendíveis no estado atual (evita recomendação bloqueada).
- Regra `outras escolas` agora exclui automaticamente a escola da magia de origem nas sugestões laterais.
- Pontuação de custo calibrada com peso maior para bloqueios relevantes:
  - dependência nomeada pendente;
  - gate numérico pendente;
  - gate de escolas do próprio candidato.
- Telemetria de ranking adicionada no motor:
  - novo diagnóstico por alvo com lista de candidatas, custo e elegibilidade;
  - motivo de exclusão padronizado (`SEM_ESCOLA`, `NAO_APRENDIVEL_AGORA`, `ESCOLA_DA_ORIGEM_BLOQUEADA`).
- Cobertura de teste ampliada no Lote 2 para:
  - exclusão de candidata bloqueada por pré-requisito numérico;
  - exclusão de escola inválida em contexto de `outras escolas`.
  - priorização por custo (candidata barata antes da cara).
  - diagnóstico de ranking validado com motivo de exclusão.

Partes faltantes:
- Ajuste fino final de pesos usando execução no catálogo real completo.

### Lote 3 - Estado Incremental
Partes implementadas:
- Estrutura separada preparada para cache incremental por alvo.
- Snapshot em memória por `alvo + estado` para:
  - `calcularEstadoAlvo`;
  - `diagnosticarRankingAlvo`.
- LRU leve (até 256 entradas por cache) para conter uso de memória.
- Invalidação operacional inicial:
  - limpeza total (`limparCache`);
  - invalidação por magia (`invalidarCachePorMagia`).
- Métrica básica de cache adicionada (`cacheStats`: entradas/hits/misses).
- Testes iniciais do Lote 3 adicionados:
  - `app/src/test/java/nexus/arcano/NexusArcanoEngineLote3Test.kt`
  - valida hit de cache e invalidação por magia.

Partes faltantes:
- Recalcular apenas chaves impactadas por cada nova magia (delta real, sem recomputo completo).
- Métrica de performance por rodada com janela temporal (tempo médio e p95).

### Lote 4 - Adaptador mínimo com a ficha
Partes implementadas:
- Recurso antigo desativado na ficha para evitar travamentos.

Partes faltantes:
- Criar adaptador de entrada/saída do `NEXUS ARCANO`.
- Exibir na UI apenas: chaves + 3 próximas ações.
- Reativação controlada por flag.

## Próximos Passos imediatos
1. Rodar telemetria de ranking no catálogo real e calibrar pesos finais do Lote 2.
2. Implementar delta incremental real no Lote 3 (recalcular apenas blocos impactados).
3. Ligar métricas de tempo por rodada (médio/p95) para validar ganho prático.

## Regra operacional
1. Implementar apenas a parte atual do lote.
2. Rodar build/test das variantes necessárias.
3. Atualizar este `PROGRESS.md` marcando:
- partes implementadas do lote atual;
- partes faltantes do lote atual;
- próximo passo imediato.
4. Commit + push.
