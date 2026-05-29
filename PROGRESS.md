# Acompanhamento do Projeto da Ficha GURPS (Para Rodolfo)

**Última Atualização:** 22 de Maio de 2026
**Status Atual:** Mestre IA - Lotes 257-261 CONCLUÍDOS (semântica híbrida completa)

## Lote 300 — [2026-05-26] Fix RAG: Planner detecta nomes de vantagens GURPS

- **Hash:** 2879e39
- **Causa raiz (logcat):** Query "reflexos em combate" era classificada como `GENERICO/combate` pelo grupo semântico do Planner. Sub-queries geradas ("combate ataque dano modificador manobra") puxavam páginas 379, 548, 327 (combate geral) — nunca p83 onde está o texto completo da vantagem. Chunk `pt_modulo_basico_p83_c1` (definição + bônus de defesa ativa) nunca entrava no resultado.
- **Mudanças:**
  - `MestreIAPlanner.kt`: mapa de ~40 vantagens/desvantagens GURPS conhecidas (`vantagensConhecidas`)
  - Quando nome exato detectado na query → entidadePrimaria = nome exato, relação = FUNCIONAMENTO, sub-query específica (ex: "reflexos em combate vantagem bonus defesa"), boost 1.2 nos termos
  - Remove sub-queries genéricas de combate que diluíam o ranking BM25+HNSW
- **Status:** ✅ Build OK

## Lote 301 — [2026-05-26] Fix: reconexão ao encerrar + watchdog de inatividade

- **Hash:** af856fb
- **Bug 1 (reconexão ao encerrar):** `encerrar()` parava `AudioRecord` antes de fechar o WebSocket. Servidor detectava parada abrupta do stream e fechava com `code=1008`. O `onClosed` interpretava como fechamento inesperado e reconectava automaticamente. Fix: flag `encerramentoIntencional` setada no início do `encerrar()`; WebSocket fechado ANTES do AudioRecord.
- **Bug 2 (silêncio de 57s):** Modelo respondeu por áudio sem chamar tool, servidor ficou 57s inativo até `code=1008`. Fix: watchdog de 50s — reinicia a cada mensagem recebida; expira sem mensagem → reconecta preventivamente.
- **Mudanças:**
  - `GeminiLiveService.kt`: flag `encerramentoIntencional`, reordenação de `encerrar()`, método `reiniciarWatchdog()`, `watchdogJob`
- **Status:** ✅ Build OK

## Lote 302 — [2026-05-26] Detecta compressão de contexto do servidor no log

- **Hash:** 833506e
- **Motivação:** Não havia forma de saber se o servidor compactou o contexto (system prompt, histórico) durante a sessão.
- **Mudanças:**
  - `GeminiLiveService.kt`: log W "COMPACTADO" quando `sessionResumptionUpdate.resumable=false`
  - `GeminiLiveService.kt`: log W "COMPRESSÃO DE CONTEXTO" quando prompt token count cai >500 em relação ao turno anterior
  - Campo `ultimoPromptTokenCount` para rastrear evolução dos tokens
- **Status:** ✅ Build OK

## Lote 303 — [2026-05-26] take(20) → take(30) no multi-query consultarManual

- **Hash:** aa40e72
- **Mudanças:** `GeminiLiveTools.kt`: take(20) → take(30) no path multi-query
- **Status:** ✅ Build OK


## Lote 304 — [2026-05-26] Fix falso alarme de compressão de contexto

- **Hash:** f4e0137
- **Causa:** `usageMetadata` vem vazio `{}` em alguns turnos (promptTokenCount=0). O detector interpretava 4915→0 como compressão real.
- **Fix:** Ignora prompt=0 na detecção e na atualização de `ultimoPromptTokenCount`.
- **Status:** ✅ Build OK

## Lote 305 — [2026-05-27] Pesquisa e planejamento: mitigação do bug <ctrl46>

- **Hash:** 075501c
- **Motivação:** Bug confirmado do Google: modelo `native-audio-preview-12-2025` emite tokens `<ctrl46>` em vez de áudio PCM após múltiplas tool calls em sequência (observado na sessão: tc=2 → silêncio no tc=3). Causa silencios persistentes sem reconexão possível sem perda de contexto.
- **Status:** ✅ Pesquisa concluída

## Lote 306 — [2026-05-27] Mitigação bug <ctrl46>: NON_BLOCKING + detector

- **Hash:** b6df4dd
- **Motivação:** Bug confirmado do Google: `<ctrl46>` emitido em vez de áudio após múltiplas tool calls. Hipótese: ciclo silêncio-de-espera → retomada de fala repetido N vezes dispara o bug.
- **Mudanças — `GeminiLiveService.kt`:**
  - `buildFuncao()`: novo parâmetro `nonBlocking: Boolean` — adiciona `"behavior": "NON_BLOCKING"` na declaração da tool
  - `buscarCatalogo` e `consultarManual`: marcados como `nonBlocking = true` → modelo fala enquanto a tool processa
  - `toolResponse`: `scheduling=WHEN_IDLE` injetado dentro do `response` — entrega resultado quando modelo terminar de falar
  - `toolCallCount`: contador de tool calls por sessão — logado em cada `toolCall` e no detector de `<ctrl46>`
  - Detector `<ctrl46>`: ao detectar token em `outputTranscription`, loga com nível ERROR incluindo `tc=N` (número de tool calls na sessão) para mapear o limiar exato
  - `encerrar()`: reseta `toolCallCount = 0`
- **Status:** ⏳ Aguardando build no Android Studio

## Lote 307 — [2026-05-27] Fix crash IllegalStateException no AudioRecord.stop()

- **Hash:** ba59716
- **Causa:** `encerrar()` chamado do thread OkHttp (`onClosed`) quando o `AudioRecord` já estava em estado inválido (liberado ou nunca iniciado). `native_stop()` lançava `IllegalStateException` → crash fatal.
- **Fix:** `try/catch` em `audioRecord?.stop()`, `audioRecord?.release()`, `audioTrack?.stop()`, `audioTrack?.release()` — encerramento nunca mais crashar por estado de hardware.
- **Status:** ⏳ Aguardando build

## Lote 308 — [2026-05-27] Fix: pergunta interrompida não era salva ao cair durante resposta

- **Hash:** abc1315
- **Causa:** `perguntaInterrompida` só era setada quando a sessão caía **durante tool call** (dentro do bloco `toolCall` com `webSocket==null`). Se a sessão caísse enquanto o modelo estava **gerando resposta** (sem tool call ativa), a pergunta do usuário era perdida e a reconexão não retomava.
- **Observado:** usuário perguntou "explique reflexos em combate" → `code=1011` durante resposta → reconectou → modelo disse "lembro onde paramos" mas **não chamou nenhuma tool** e encerrou.
- **Fix:** `onClosed` salva `ultimaPerguntaUsuario` em `perguntaInterrompida` antes de `encerrar()` resetar os campos, para qualquer fechamento inesperado (`code != 1000`).
- **Status:** ⏳ Aguardando build

## Lote 309 — [2026-05-27] Fix: WHEN_IDLE → SILENT no toolResponse (duplo turno de áudio)

- **Hash:** 3b8d3fb
- **Causa:** `scheduling=WHEN_IDLE` fazia o modelo gerar um **segundo turno de áudio** quando recebia o resultado da tool NON_BLOCKING — enquanto o primeiro turno ainda tocava. Resultado: 59 chunks descartados, corte abrupto, recomeço da resposta. Usuário ouvia a resposta "duas vezes".
- **Fix:** `scheduling=WHEN_IDLE` → `SILENT`: modelo incorpora o resultado da tool na resposta em andamento sem gerar novo turno de fala.
- **Status:** ⏳ Aguardando build

## Lote 310 — [2026-05-27] Auditoria linha a linha GeminiLiveService.kt — 5 correções

- **Hash:** 9c651ad
- **Escopo:** Pente fino completo no arquivo após acúmulo de patches incrementais.
- **Bug crítico (#2):** `modeloFalando` não era resetado quando o watchdog disparava. Nova sessão iniciava com mic permanentemente mudo. Fix: reseta `modeloFalando=false` e cancela `micReleaseJob` antes de `reconectarAutomaticamente()` no watchdog.
- **Bug #3:** `turnoTemAudio=false` e `modeloFalando=false` redundantes dentro do `else` do bloco NON_BLOCKING toolCall — já eram `false` ao entrar no `else`. Removidos.
- **Bug #4:** `ultimaPerguntaUsuario` atribuída duas vezes nos dois ramos do `if/else` de `inputTranscription`. Extraído para antes do `if`.
- **Bug #5:** `keepAliveJob` e `capturaJob` cancelados mas não nulificados em `encerrar()`. Assimetria com `reproducaoJob`. Corrigido com `= null`.
- **Bug #1:** Indentação incorreta de `limparFilaAudio()` na linha 852 (colagem manual sem ajuste). Corrigido.
- **Status:** ⏳ Aguardando build

## Lote 311 — [2026-05-27] Diagnóstico: dois monitores de aceleração de áudio

- **Hash:** ac6e0ea
- **Diagnóstico 1 — delta entre chunks:** dentro do `reproducaoJob`, loga para cada chunk o tamanho, duração teórica (bytes/48000) e delta real de chegada. Se `deltaCheg < duracaoTeórica/2` → loga `⚠ ACUMULANDO`. Indica que chunks chegam mais rápido do que o hardware os consome.
- **Diagnóstico 2 — monitor periódico `playbackHeadPosition`:** coroutine `audioMonitorJob` que a cada 500ms (enquanto `modeloFalando=true`) mede a taxa real de avanço do hardware em fps. 24000fps = normal. Emojis: 🟢 normal / 🟡 leve / 🔴 ACELERADO / 🔵 lento.
- **Status:** ⏳ Aguardando build e teste

## Lote 312 — [2026-05-27] feat: AcousticEchoCanceler + mic sempre aberto

- **Hash:** 703cf38
- **Motivação:** Pesquisa em repositórios reais (GeminiLive-Assistant-Android, android/ai-samples) mostrou que o padrão correto é usar cancelamento de eco em hardware e deixar o mic aberto o tempo todo — não bloqueio de software.
- **AcousticEchoCanceler:** ativado em hardware logo após criar o `AudioRecord`. Cancela o eco do speaker no microfone — modelo não ouve a si mesmo.
- **Mic sempre aberto:** removido `if (modeloFalando) continue` no loop de captura e no keepAlive. O AEC garante que o eco não chega ao servidor.
- **Resultado esperado:** usuário pode falar e interromper o modelo a qualquer momento, sem janela de silêncio forçado.
- **Status:** ⏳ Aguardando build e teste

## Lote 313 — [2026-05-28] docs: relatório DRY de duplicações + plano para §1 (normalização de texto)

- **Hash:** 4340056
- **Escopo:** Análise — nenhum código de produção alterado.
- **Entrega 1:** novo arquivo `.agent/skills/RELATORIO_DRY_DUPLICACOES.md` documentando **11 padrões** de código duplicado no projeto Android (~380–450 linhas elimináveis). Inclui evidências por arquivo:linha, exemplos de código, diagnóstico, sugestão de refatoração e plano em 3 lotes (A: RAG/buscas → B: UI → C: infra).
- **Entrega 2:** plano detalhado para o §1 do relatório (normalização de texto — 7 implementações paralelas), em 7 etapas reversíveis, com rede de segurança via testes antes de qualquer refatoração. Plano apresentado em linguagem de funcionalidade (sem tecniquês) para o usuário aprovar.
- **Próximos passos sugeridos:** após aprovação do usuário, executar a Etapa 1 (baseline de testes) do plano §1 — ainda não iniciado.
- **Status:** ✅ Relatório entregue; refatoração aguardando aprovação.

## Lote 314 — [2026-05-28] refactor: §1 do relatório DRY — TextNormalizer centralizado (6 de 7 migradas)

- **Hashes (branch `refactor/text-normalizer`, mesclada em `feature/mestre-ia-graphrag`):**
  - `eecb0e8` — Etapa 2: cria TextNormalizer (sem ligar ainda) + 18 testes
  - `fdb573d` — Etapa 3: CatalogFilters.normalizarBusca delega ao TextNormalizer
  - `6657a4c` — Etapa 4: DialogsTecnicas delega ao CatalogFilters (apaga função privada)
  - `cb6797d` — Etapa 5: SkillEngine + DataRepository (normalizarComparacao + normalizarChaveClasse) delegam
- **Escopo:** unifica 6 das 7 implementações paralelas de normalização de texto. Cria preset central `TextNormalizer` em `domain/filters/` com 4 modos (`SIMPLE`, `BUSCA_PADRAO`, `PERICIA_RAW`, `ARMA_GRUPO`).
- **Branch isolada:** trabalho feito em `refactor/text-normalizer` ("modo paranoico" — protegeu a principal). Mesclada após validação.
- **Cobertura nova:** 18 testes do `TextNormalizer` cobrindo cada modo, incluindo o `ARMA_GRUPO` (Mestre de Armas) que historicamente não tinha teste.
- **Validação visual:** 5 cenários de busca testados no app pelo usuário (catálogos + Técnicas) — 0 regressões.
- **Validação automatizada:** baseline preservada (130 verdes, 17 vermelhos pré-existentes não-relacionados) em cada uma das 4 etapas commitadas.
- **Adiamento consciente — Etapa 6 (Mestre de Armas):** decisão deliberada de não migrar `MestreDeArmasRule.normalize` por risco de regressão silenciosa em cálculo de bônus de dano sem ficha de teste pronta. Preset `ARMA_GRUPO` já implementado e testado, esperando migração futura quando o usuário tiver ficha de validação.
- **Linhas eliminadas:** ~58 (líquido após criar 102 de TextNormalizer + testes).
- **Status:** ✅ Mesclado em `feature/mestre-ia-graphrag` (ainda não pushed para origin).

## Lote 315 — [2026-05-28] fix: Auditor RAG — verificador de citações + topK 15 + prompt anti-alucinação

- **Hash:** a46d36a
- **Escopo:** 3 mudanças coordenadas para resolver alucinação confiante do Auditor (caso real:
  modelo citou "[Módulo Básico, pág. 174]" com -5 inventado para "escalar com uma mão",
  regra que não existe no GURPS). Diagnóstico completo em `.agent/skills/DIAGNOSTICO_AUDITOR_RAG.md`.

### Motivação (evidência do logcat)
- Modelo fez 5 buscas honestas, declarou "não vi resultados nos fragmentos", mas mesmo assim
  inventou página específica (174) e número (-5) usando "conhecimento padrão de GURPS".
- Prompt sozinho já proibia isso (linha 11-14), mas modelo desobedeceu — comportamento típico
  de LLMs que preferem "ser útil" a admitir desconhecimento.
- Logcat também revelou que `MODO_HNSW_PURO=true` está LIGADO (BM25 desativado), e o RAG
  retornava 40+ chunks por busca, afogando o modelo em contexto.

### Mudança A — Verificador de Citações (novo)
- **Arquivo novo:** `domain/MestreIACitationValidator.kt` (~165 linhas).
- Extrai citações no formato `[Livro, Pág. X]` e `pág. NNN` da resposta do modelo.
- Compara cada citação contra páginas dos chunks que o RAG efetivamente retornou.
- Citações sem chunk correspondente são marcadas como "⚠️ não verificadas" e anexadas
  ao final da resposta com aviso visual para o usuário.
- **NÃO impede alucinação** (impossível com LLM atual) — apenas AVISA quando acontece.
- Integração em `MestreIAUseCase.kt` (~15 linhas modificadas) no ponto onde `respostaFinal` é montada.

### Mudança B — HNSW topK 50→15
- **Arquivo:** `domain/MestreIAGraphEngine.kt`.
- Dois pontos: linha 77 (modo HNSW puro, ativo hoje) e linha 214 (modo BM25+HNSW, desativado).
- Constantes do scoreMap e take ajustadas proporcionalmente (50.0 → 15.0) para evitar
  scores negativos.
- Resultado esperado: 40+ chunks por busca → ~15 chunks, modelo foca melhor.

### Mudança C — Prompt do Auditor reforçado
- **Arquivo:** `data/network/MestreIAPromptsAuditor.kt`.
- Adicionada seção "REGRA CRÍTICA DE CITAÇÃO (LOTE 315)" com:
  - Lista explícita de comportamentos PROIBIDOS (citar página por inferência, números inventados, etc.).
  - Lista de comportamentos OBRIGATÓRIOS (declarar quando não achou, marcar conhecimento geral).
  - Exemplo concreto da alucinação detectada (pág. 174 / -5) como caso negativo.
  - Avisa o modelo que existe sistema externo verificando suas citações (aumenta cumprimento).

### Validação
- ✅ Compila (`./gradlew :app:compilePracegoDebugKotlin` BUILD SUCCESSFUL em 12s).
- ⏳ Validação funcional: usuário vai re-testar a bateria de 7 perguntas reais
  (registradas em `pergutas.txt`) e comparar respostas antes/depois.

### Rollback
- 1 commit único na `feature/mestre-ia-graphrag` — `git revert <hash>` desfaz tudo.
- Nenhuma mudança em testes, banco de dados, ou catálogos.

## Lote 316 — [2026-05-28] fix: Auditor — maxTokens 16k + regra de leitura cuidadosa + regra de tamanho

- **Hash:** 22ff256
- **Escopo:** 2 mudanças no código + 2 verificações (sem mexer):
  - **A. maxTokens 4096 → 16384** (`MestreIAUseCase.kt` linha 164):
    resolve corte de respostas longas observado em pergunta sobre "Ataque Súbito".
  - **B. temperature:** verificado, **já estava em 0.1** em `MestreIAClient.kt`
    linhas 349 e 373 — sem alteração necessária (modelo já está em modo determinístico).
  - **C. log cache_hit:** verificado, **já existe** em `MestreIAClient.kt` linhas 259-263
    (`MestreIA_Cache: Cache hit=X miss=Y (Z% do prompt em cache)`). Sem duplicação.
  - **D. Regra de tamanho:** nova seção no prompt do Auditor obrigando ~10k caracteres
    máximo, com instrução de "encerrar limpo" se exceder. Mira o problema do corte
    no meio de tabela/frase observado.
  - **E. Princípio de leitura cuidadosa:** nova seção no prompt obrigando o modelo a
    **comparar TODOS os chunks recebidos** antes de escolher uma manobra/regra, e
    preferir regras ESPECIALIZADAS quando aplicáveis. Mira o caso "Avançar e Atacar
    vs Ataque Súbito" onde o modelo escolheu a primeira opção e ignorou a melhor
    que estava no mesmo pacote de chunks. Princípio genérico, sem hardcode de páginas
    ou termos específicos (contraste com erro de IAs anteriores que blindavam casos
    específicos).

### Motivação
- Lote 315 detectou alucinação de páginas; Lote 316 ataca outro problema:
  modelo **escolher mal** entre chunks já recebidos.
- Pesquisa profunda da documentação DeepSeek (ver `.agent/skills/DEEPSEEK_DOCUMENTACAO.md`)
  confirmou: max_tokens default 4000 é fácil de estourar em respostas técnicas;
  temperature recomendada para tarefas determinísticas é baixa (já estava em 0.1).

### Validação
- ✅ Compila (`./gradlew :app:compilePracegoDebugKotlin` BUILD SUCCESSFUL em 15s).
- ⏳ Validação funcional: usuário re-testará perguntas (especialmente a do "Ataque Súbito"
  e perguntas longas que cortavam) para confirmar melhoria.

### Rollback
- 1 commit único — `git revert <hash>` desfaz tudo.
- Nenhuma mudança em testes, banco, catálogos.

----------------------------------------------------------------------------------------------------------------------------------------------------
