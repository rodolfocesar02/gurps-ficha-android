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
- Teste pesado com catálogo real `magias2versao.json` adicionado:
  - `NexusArcanoEngineStressMagiasV2Test`
  - estresse de 40 alvos com pré-requisitos longos (600 amostras):
    - antes da otimização de índices: p95 ~ 7.8 ms | p99 ~ 106.8 ms | max ~ 3449.9 ms
    - após otimização de índices/cache de parsing: p95 ~ 0.027 ms | p99 ~ 3.946 ms | max ~ 367.0 ms
  - sweep completo da escola `Encantamento` (59 alvos):
    - antes da otimização de índices: p50 ~ 1460.8 ms | p95 ~ 1581.0 ms | max ~ 1613.1 ms
    - após otimização de índices/cache de parsing: p50 ~ 0.784 ms | p95 ~ 0.929 ms | max ~ 214.8 ms
  - consistência funcional: sem exceções e sem inconsistências estruturais em ambos.
- Teste comparativo `delta incremental vs full` por rodada adicionado (`magias2versao.json`):
  - alvo: `desejo`, 24 rodadas;
  - delta p50 ~ 0.044 ms, p95 ~ 1.047 ms;
  - full p50 ~ 0.025 ms, p95 ~ 1.020 ms;
  - equivalência funcional preservada (sem inconsistências);
  - refinamento incremental aplicado (`INCREMENTAL_NO_IMPACT`) para reaproveitar sugestões/bloqueio quando mudança não impacta alvo;
  - conclusão atual: ainda sem ganho de p95 sobre full nesse cenário, mas diferença caiu.
- Telemetria de ranking Lote 2 calibrada com `magias2versao.json`:
  - `ALVOS_COM_DIAGNOSTICO=67`;
  - `TOP1_ESCOLA_NOVA=100%`;
  - `TOP1_SEM_PREREQ=100%`;
  - `TOP3_TEM_SEM_PREREQ=100%`.
- Tratamento de pré-requisito vazio/mojibake consolidado no custo do ranking
  (`—`, `â€”`, etc. passam a ser considerados sem pré-requisito real).
- Desempate determinístico por alvo adicionado no ranking (`tieBreakPorAlvo`) para reduzir repetição em empates.
- Otimização estrutural no motor para caminho de escola:
  - índices em memória por catálogo (`id -> nome/pre/escolas normalizadas`);
  - cache de parsing por magia (`dependenciasNomeadas`, `regrasEscolas`, `regrasNumericas`, `cadeiaObrigatoria`);
  - remoção de varreduras repetidas do catálogo completo em cálculos internos.

Partes faltantes:
- Ajuste fino final de diversidade entre escolas nas sugestões (ainda há concentração de top1 em parte dos alvos).

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
- Invalidação incremental por impacto de alvo adicionada:
  - `alvosImpactadosPorMagia` (dependência direta/indireta + alvos com regra de escolas);
  - `invalidarCacheIncrementalPorMagia` remove apenas entradas de alvos impactados.
- Delta incremental inicial implementado no motor:
  - `calcularEstadoAlvoIncremental(alvo, estadoAnterior, resultadoAnterior, estadoNovo)`;
  - caminho incremental para mudança apenas de magias conhecidas;
  - fallback automático para full apenas quando há desalinhamento inválido do snapshot anterior.
- Delta incremental expandido para mudança de atributos sem fallback full:
  - recálculo incremental de chaves numéricas (`AM/IQ/soma`) no snapshot do alvo;
  - manutenção de equivalência funcional com cálculo completo validada em teste.
- Métrica básica de cache adicionada (`cacheStats`: entradas/hits/misses).
- Métrica de tempo por rodada adicionada:
  - `timingStats` com `amostras`, `mediaMs`, `p95Ms`, `maxMs`;
  - janela limitada em memória (até 512 amostras recentes).
- Testes iniciais do Lote 3 adicionados:
  - `app/src/test/java/nexus/arcano/NexusArcanoEngineLote3Test.kt`
  - valida hit de cache, invalidação por magia, invalidação incremental por impacto, delta incremental e métricas de tempo.

Partes faltantes:
- Refinar delta incremental para bater p95 do full em cenário de rodada real (ainda levemente acima).

### Lote 4 - Adaptador mínimo com a ficha
Partes implementadas:
- Recurso antigo desativado na ficha para evitar travamentos.

Partes faltantes:
- Criar adaptador de entrada/saída do `NEXUS ARCANO`.
- Exibir na UI apenas: chaves + 3 próximas ações.
- Reativação controlada por flag.

## Próximos Passos imediatos
1. Ajustar diversidade do ranking do Lote 2 entre alvos distintos (telemetria já coletada).
2. Otimizar o caminho incremental de sugestões e repetir comparativo delta vs full.
3. Preparar adaptador mínimo (Lote 4) em flag separada para primeiro teste no emulador.

## Regra operacional
1. Implementar apenas a parte atual do lote.
2. Rodar build/test das variantes necessárias.
3. Atualizar este `PROGRESS.md` marcando:
- partes implementadas do lote atual;
- partes faltantes do lote atual;
- próximo passo imediato.
4. Commit + push.
