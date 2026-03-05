# PROGRESS - GURPS Ficha Android

Atualizado em: 2026-03-05

## Estado Atual
- Branch: `main`
- Variantes: `visual` e `pracego`
- Build: estável nas variantes principais
- Foco: estabilidade da ficha e evolução do motor externo `NEXUS ARCANO`
- Pasta do motor externo: `motor modo alvo/`
- Checkpoint consolidado: suíte Lote 2 separada e verde em teste unitário.
- Status operacional do Lote 4: pronto para teste manual guiado com flag do NEXUS ARCANO.
- Stress test do NEXUS ARCANO reexecutado em `2026-03-05` com relatórios atualizados em `app/build/reports/`.
- Modo alvo antigo: removido do projeto (`app/` e `motor modo alvo/arquivos-legados`), fluxo unificado no NEXUS ARCANO.
- Diagnóstico funcional atual: em teste manual real (`AM3`, `IQ15`), usuário chegou a `35` magias e ainda não liberou `Desejo` seguindo apenas recomendações.
- Diagnóstico de performance em tempo real (2026-03-05): sem crash/ANR no fluxo principal, porém com jank recorrente (`Choreographer: Skipped 31-48 frames`) em interações da tela de magias.
- Decisão técnica: não reiniciar do zero; evoluir o NEXUS ARCANO para um planejador global (não guloso por rodada).

## Plano de Virada do Motor (Lotes + Passos)
Objetivo macro: garantir que recomendações levem ao alvo com menor caminho viável e progresso explícito de requisitos compostos (cadeia + escolas + atributos).

### Lote A - Modelo de Objetivo Global (Obrigatório)
Status: `EM ANDAMENTO`

Passos:
1. Formalizar alvo como conjunto de metas obrigatórias: `PARCIAL`
   - cadeia (`Encantar -> Pequeno Desejo -> Desejo`);
   - contadores (`10 escolas`, `15 escolas`);
   - gates numéricos (`AM/IQ/DX`).
2. Converter pré-requisito textual para estrutura canônica por tipo: `PARCIAL`
   - `MAGIA_EXATA`, `MAGIA_OU`, `ESCOLAS_DISTINTAS`, `NUMERICO`, `CADEIA`.
3. Definir estado de progresso por meta com `faltante`, `atendido`, `bloqueado por upstream`: `PARCIAL`.
4. Adicionar checksum de estado para auditoria determinística: `PENDENTE`.

### Lote B - Planejador de Caminho Mínimo (BFS/A* com custo)
Status: `PENDENTE`

Passos:
1. Implementar busca global de caminho (BFS/A*) sobre estado incremental.
2. Função de custo:
   - `+1` por magia adicionada;
   - penalidade para escola repetida quando meta de escolas ainda não fechou;
   - penalidade forte para ação que não reduz nenhuma meta pendente.
3. Restringir expansão a magias aprendíveis no estado atual (sem sugestão impossível).
4. Garantir que saída da busca devolva:
   - próxima ação;
   - trilha curta prevista;
   - metas impactadas pela ação.

### Lote C - Recomendador de Escolas (anti-deriva)
Status: `PENDENTE`

Passos:
1. Priorizar automaticamente escola nova enquanto houver meta de escolas pendente.
2. Bloquear recomendação redundante de mesma escola em sequência, salvo exceção obrigatória de cadeia.
3. Excluir definitivamente escola `Tecnológica` do pool recomendado.
4. Incluir fallback controlado quando não houver escola nova aprendível (explicar motivo).

### Lote D - Contrato de UI e Transparência de Progresso
Status: `PENDENTE`

Passos:
1. Exibir progresso explícito no diálogo:
   - `Escolas p/ Encantar: X/10`;
   - `Escolas p/ Desejo: Y/15`;
   - `Cadeia: Encantar -> Pequeno Desejo -> Desejo`.
2. Mostrar "próxima obrigatória" vs "próxima lateral útil".
3. Mostrar motivo de bloqueio em formato curto e estável (sem texto ambíguo).
4. Manter botão/fluxo de teste manual para validação rodada a rodada.

### Lote E - Performance e Estabilidade do Fluxo
Status: `EM ANDAMENTO`

Passos:
1. Manter pré-aquecimento de catálogo/índice de magias em background (`FichaViewModel init`) - `feito`.
2. Manter cache de filtros/listas (ViewModel + DataRepository) - `feito`.
3. Reduzir recomputação pesada na lista fora do modo alvo - `feito`.
4. Medir p95 de abertura do seletor e p95 de scroll com cenário fixo no emulador.
5. Definir meta operacional: eliminar `Skipped frames` recorrente acima de 30 em uso normal.

### Lote F - Validação, Auditoria e Go/No-Go
Status: `PENDENTE`

Passos:
1. Cenário canônico de aceite:
   - personagem `AM3`, `IQ15`;
   - alvo `Desejo`;
   - seguir apenas recomendadas até liberar alvo.
2. Critério de sucesso funcional:
   - alvo liberado sem deriva longa;
   - cada ação reduz ao menos 1 meta pendente.
3. Critério de sucesso de UX:
   - usuário entende claramente o que falta (cadeia + contadores).
4. Auditoria final:
   - unit tests do motor;
   - teste manual em emulador;
   - relatório consolidado no `PROGRESS.md`.

## Feito
- Lote A global iniciado no NEXUS ARCANO:
  - novo contrato de metas canônicas `ArcanoMetaProgress` + `ArcanoMetaTipo`;
  - novo diagnóstico público `diagnosticarMetasAlvo(alvoId, estado)`;
  - cobre metas de cadeia, escolas distintas, numéricos (`AM/IQ/soma`) e alvo final;
  - testes globais adicionados em `NexusArcanoEngineLoteAGlobalTest` para múltiplos alvos (`Desejo`, `Desejo Superior`, `Translocação`).
- Catálogo de magias com ajustes e normalizações recentes (incluindo escola `Animais` por caminhos).
- Regras especiais de magias ajustadas para caminhos `Ar/Terra/Mar` quando aplicável.
- Fluxo da ficha estabilizado após reinício/validação do emulador.
- Organização de trabalho separada criada em `motor modo alvo/` para evolução do novo motor.
- Validação funcional da variante `pracego` executada no emulador:
  - build/assemble `pracego` verde;
  - instalação em emulador (`installPracegoDebug`) concluída;
  - app aberto com sucesso (`MainActivity` em estado resumed);
  - stress rápido com `adb shell monkey` (300 eventos) sem crash/ANR do app.
- Auditoria rápida de acessibilidade na UI:
  - cobertura de `semantics/contentDescription` presente nos fluxos principais já mapeados;
  - sem crash de execução na variante `pracego` durante validação.
- Limitação de ferramenta registrada:
  - `lintPracegoDebug` falhou por bug interno do lint (`AutoboxingStateCreationDetector`), sem evidência de erro funcional do app.
- Correção de matching singular/plural nas dependências nomeadas do NEXUS ARCANO:
  - pré-requisito em plural agora casa com magia em singular (e vice-versa), reduzindo bloqueio falso.
- Correção no filtro de escola da UI de magias:
  - comparação passou de `contains` para igualdade normalizada, evitando falso positivo (ex.: filtro `Ar` não puxa mais `Quebrar e Consertar`).
- Stress test completo do NEXUS ARCANO (Lote 2/3) reexecutado e verde:
  - `stress_ramificacoes_longas_com_magias_v2`: 40 alvos, 600 amostras, p50 `0,002 ms`, p95 `0,037 ms`, p99 `5,535 ms`, max `573,522 ms`, `0` exceções/inconsistências;
  - `sweep_escola_encantamento_consistencia_e_tempo_magias_v2`: 59 alvos, p50 `0,834 ms`, p95 `1,216 ms`, max `437,819 ms`, `0` exceções/inconsistências;
  - `comparativo_delta_incremental_vs_full_por_rodada_magias_v2`: 24 rodadas, equivalência funcional preservada (`0` inconsistências), delta ainda acima do full no p95 (`1,541 ms` vs `1,414 ms`);
  - `telemetria_ranking_lote2_magias_v2`: `ALVOS_COM_DIAGNOSTICO=67`, `TOP1_ESCOLA_NOVA=100%`, `TOP1_SEM_PREREQ=100%`, `TOP3_TEM_SEM_PREREQ=100%`.
- Limpeza estrutural do modo alvo antigo concluída:
  - removidos `MagiaTargetEngine`, `MagiaDependencyPlanner`, `MagiaPlannerDataSource` e testes legados associados em `app/src/main`, `app/src/test` e `motor modo alvo/arquivos-legados`.
  - `DataRepository` desacoplado da interface legada antiga.
- Auditoria automática magia-por-magia do modo alvo (NEXUS ARCANO) adicionada e executada:
  - teste: `NexusArcanoModoAlvoAuditoriaTodasMagiasTest`;
  - total de magias auditadas: `839` (catálogo `magias2versao.json`);
  - status atual após correções de parser/cadeia: `SUCESSO=838`, `FALHA=0`, `BLOQUEIO_NUMERICO=1` (`desejo_superior`);
  - checagem de menor caminho (BFS limitada) em `404` alvos: `DESVIO_MENOR_CAMINHO_ENCONTRADO=0`;
  - relatório: `app/build/reports/nexus_arcano_modo_alvo_auditoria_todas_magias.txt`.
- Teste em emulador executado para lag/gargalo (variante `pracego`):
  - instalação: `:app:installPracegoDebug` OK;
  - carga de eventos: `adb shell monkey -p com.gurps.ficha.pracego --throttle 80 -v 1200`;
  - sem crash/ANR detectado no logcat durante execução;
  - `gfxinfo`: `879` frames, janky `17,41%` (legacy `29,81%`), p50 `17ms`, p90 `48ms`, p95 `113ms`, p99 `400ms`, `Slow UI thread=119`, `Slow draw commands=66`.

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
- Matching singular/plural em nomes de magia para dependências nomeadas (ex.: `Curar Planta` <-> `Curar Plantas`).
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
  - delta p50 ~ 0.035 ms, p95 ~ 1.463 ms;
  - full p50 ~ 0.015 ms, p95 ~ 1.373 ms;
  - equivalência funcional preservada (sem inconsistências);
  - refinamento incremental aplicado e validado sem divergência;
  - conclusão atual: delta ainda acima do p95 do full nesse cenário.
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
- Refinar delta incremental para bater p95 do full em cenário de rodada real (ainda acima).

### Lote 4 - Adaptador mínimo com a ficha
Partes implementadas:
- Recurso antigo desativado na ficha para evitar travamentos.
- Adaptador mínimo `NEXUS ARCANO` criado no app:
  - `app/src/main/java/com/gurps/ficha/domain/magias/NexusArcanoModoAlvoAdapter.kt`
  - entrada: catálogo `magias2versao` + estado do personagem (`magias`, `AM`, `IQ`, `DX`);
  - saída: `relacionadosIds` + `chavesAtivas` + `chavesFaltantes` + `proximasAcoesIds` + `aviso`.
- `FichaViewModel` integrado ao adaptador:
  - stubs removidos de `listaRelacionadosMagiaAlvo`, `idsRelacionadosMagiaAlvo`,
    `assinaturaEstadoMagiasParaModoAlvo`, `requisitarModoAlvo`;
  - cálculo assíncrono em `Dispatchers.Default` com cancelamento seguro;
  - estado de UI adicionado: `modoAlvoChavesAtivas`, `modoAlvoChavesFaltantes`, `modoAlvoProximasAcoes`.
- UI de magias preparada para exibir resumo mínimo no modo alvo:
  - linha de `3 próximas ações`;
  - linha de `chaves faltantes` (até 3);
  - sem reativar a flag global do modo alvo antigo.
- Reativação controlada por flag de build implementada:
  - `BuildConfig.MODO_ALVO_NEXUS_HABILITADO`;
  - leitura de `local.properties` / propriedade Gradle `MODO_ALVO_NEXUS_HABILITADO`.
- Robustez de cálculo no `FichaViewModel`:
  - cálculo do NEXUS ARCANO usa snapshot imutável de estado (`magias conhecidas`, `IQ`, `DX`, `AM`);
  - evita inconsistência quando o usuário altera magias durante recálculo assíncrono.
- UX do fluxo de alvo refinada:
  - ao ativar modo alvo ou definir novo alvo, limpa busca/filtros de classe/escola para reduzir estado confuso;
  - resumo adicional acessível no `pracego` com leitura consolidada de alvo, próximas ações e chaves pendentes.
- Testes unitários do adaptador adicionados:
  - `app/src/test/java/com/gurps/ficha/domain/magias/NexusArcanoModoAlvoAdapterTest.kt`
- Checklist de smoke test emulador do Lote 4 criado:
  - `motor modo alvo/lotes/lote-04-smoke-check-emulador.md`
- Validação de execução no emulador (`pracego`) após os ajustes:
  - `:app:installPracegoDebug` concluído;
  - `MainActivity` em estado resumed após launch;
  - sem crash/ANR em abertura imediata.
- Correções de trilha aplicadas após teste manual de `Desejo`, `Cavalgar` e `Adivinhação`:
  - parser de regra de escolas no NEXUS ARCANO atualizado para aceitar `magia(s)` e `mágica(s)` e números por palavra (`dez`, `quinze`, etc.);
  - fallback da lista no diálogo de magias reforçado quando o pool do alvo vem vazio (passa a buscar sugestões no catálogo completo);
  - filtro de opções imediatas mantém prioridade para magias aprendíveis agora.
- Hotfix de continuidade de recomendações após progresso parcial:
  - adaptador do NEXUS ARCANO agora sempre complementa as 3 próximas ações com candidatas elegíveis do ranking quando a cadeia principal estiver bloqueada;
  - evita estado "parou de recomendar" após adicionar várias magias sem fechar totalmente o alvo.
- Hotfix de performance/UI no modo alvo:
  - remoção de varredura pesada em recomposição da tela de magias;
  - lista e próxima recomendada passam a usar IDs prontos do ViewModel (`modoAlvoRelacionadosIds` / `modoAlvoProximasAcoesIds`), reduzindo risco de ANR.
- Diagnóstico consolidado do lote atual (teste manual):
  - motor atual ainda é heurístico guloso por rodada (`greedy`) e não garante caminho global mínimo;
  - em cenário real de `Desejo` houve deriva para trilhas longas (ex.: ~27 magias) apesar de existir trilha mais curta;
  - conclusão: falta implementar planejamento de caminho mínimo global para cumprir objetivo de "menos magias possível".
- Ajuste operacional de validação:
  - durante o teste atual, `MODO_ALVO_HABILITADO` foi forçado para `true` em `DialogsMagias.kt` para eliminar ambiguidade de flag e garantir visibilidade do chip no emulador.
- Auditoria de uso em runtime consolidada:
  - fluxo de UI/ViewModel usa `NexusArcanoModoAlvoAdapter` como fonte de cálculo do modo alvo;
  - sem referências remanescentes a `MagiaTargetEngine`/`MagiaDependencyPlanner`/`MagiaPlannerDataSource` no código ativo.
- Correção estrutural no motor para cenários com `ou` em dependências nomeadas:
  - leitura por grupos alternativos (`A ou B`) em vez de tratar tudo como obrigatório;
  - cadeia obrigatória dinâmica por estado para reduzir trilhas longas desnecessárias;
  - desempate de alternativa por custo aproximado de profundidade.

Partes faltantes:
- Rodar validação funcional guiada do fluxo completo de magias com a flag do NEXUS ARCANO ligada
  (entrada/saída do diálogo, adicionar magias recomendadas em sequência e confirmar estabilidade da trilha).
- Remover o forçamento temporário (`MODO_ALVO_HABILITADO = true`) e retornar ao controle por flag de build após validação final.
- Novo sublote pendente: algoritmo de caminho mínimo (BFS/A*) no NEXUS ARCANO para otimização global de trilha.

## Checkpoint 10 Itens (2026-03-05)
1. Build unitário do stress do NEXUS ARCANO executado e verde.
2. Relatório de ramificações longas atualizado (`p95=0,037 ms`, sem inconsistências).
3. Relatório de sweep da escola Encantamento atualizado (`p95=1,216 ms`, sem inconsistências).
4. Comparativo delta vs full atualizado (equivalência funcional preservada).
5. Telemetria do ranking Lote 2 reconfirmada com `67` alvos com diagnóstico.
6. Correção de singular/plural implementada no parser de dependências nomeadas.
7. Teste unitário de regressão para singular/plural adicionado e verde.
8. Correção de filtro de escola por igualdade normalizada aplicada no app.
9. Auditoria de runtime confirma uso do NEXUS ARCANO no fluxo principal.
10. Auditoria de código confirma remoção do legado antigo do modo alvo no projeto.

## Próximos Passos imediatos
1. Iniciar Lote A (modelo de objetivo global) e congelar contrato de metas para `Desejo`.
2. Implementar Lote B (busca global BFS/A*) com custo anti-deriva por escola.
3. Integrar Lote C/D no diálogo de magias (progresso explícito + recomendação obrigatória/lateral).
4. Rodar Lote F cenário canônico (`AM3`, `IQ15`, `Desejo`) e registrar resultado.

## Regra operacional
1. Implementar apenas a parte atual do lote.
2. Rodar build/test das variantes necessárias.
3. Atualizar este `PROGRESS.md` marcando:
- partes implementadas do lote atual;
- partes faltantes do lote atual;
- próximo passo imediato.
4. Commit + push.
