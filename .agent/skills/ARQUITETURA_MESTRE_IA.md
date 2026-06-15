# ⚙️ ARQUITETURA DO MOTOR "MESTRE IA"

> **Atualizado em 2026-06-09 (Forjador JSON-direto + Templates + Pintor — §9). Auditor (§1-8) revisado pós-Lote 328.** Documento reescrito após
> auditoria linha-a-linha de todos os arquivos do Auditor (revisão 2026-06-08 reconferiu
> contagens/números de linha contra o código real e confirmou a Voz como caller ativo do
> GraphEngine). Substitui a versão antiga (que descrevia o RAG semântico pré-Lote 325). Seções de
> **código LEGADO/MORTO** estão marcadas com ⚠️ e o lote em que deixaram de ser usadas — leia-as
> antes de mexer em qualquer coisa.

> ➕ **2026-06-09:** Forjador mudou de fluxo (JSON direto, não mais loop de tools p/ CRIAR) e ganhou
> Templates + Mestre Pintor. Ver **§9** (nova). O AUDITOR (§1-8) não mudou.

> ➕ **2026-06-14 (Lotes 349-370):** novo **3º modo de IA — NARRADOR (modo Saga)** + **motor de combate**
> (`domain/combat`, Kotlin puro) + UI de combate. Ver **§10** (nova). Auditor (§1-8) e Forjador (§9) não mudaram.
> Branch `GURPS-Saga` (submódulo), HEAD `41996c4`.

Sistema de IA (RAG) integrado ao app de Ficha GURPS. **Dois modos** sobre a mesma base:

1. **AUDITOR** (modo `conversa`/dúvida): responde regras de GURPS. **Desde o Lote 325 usa
   busca por palavra-chave ("grep + leitura dirigida"), NÃO mais busca semântica/embedding.**
2. **FORJADOR** (modo `geracao`/`analise`): cria fichas. **[+ 2026-06-09]** Hoje o modelo **entrega
   um JSON completo da ficha de uma vez** e o app preenche pilar a pilar (`MestreIAGeneratorUseCase`)
   — não usa mais loop de `fill_character_sheet` para CRIAR. As `ForjadorTools` ainda existem (edição
   incremental/consulta). Ver **§9**.

---

## 1. FLUXO VIVO DO AUDITOR (estado atual — Lote 328)

O Auditor faz o modelo trabalhar como um pesquisador que usa um índice:

```
Pergunta
  → MestreIAUseCase.conversarComMestreIA()   [loop até MAX_TOOL_CALLS=8]
      → modelo chama localizar_no_codex(termos, livros?)   "página de resultados"
          → MestreIARepository.localizarNoCodex()
              → FTS4 AND (manualChunkDao.buscarRegras) → fallback OR se AND vazio
              → rankearPorBM25()  ← ORDENA por relevância (Lote 327)
              → devolve lista compacta: livro | página | trecho (NÃO o texto cheio)
      → modelo julga a lista e chama ler_pagina(livro, pagina, pagina_final?)
          → MestreIARepository.lerPaginas() → buscarPorPaginaESource() → texto COMPLETO
      → repete até ter lido o suficiente
  → resposta final forçada (desativarTools=true)
      → trava anti-confabulação (Lote 328): se NUNCA leu página, exige declarar
        "não localizei" em vez de citar de memória
  → MestreIACitationValidator valida páginas citadas vs chunks lidos (Lote 315)
```

### Arquivos VIVOS do Auditor e o que cada um faz

| Arquivo | Papel atual | Funções vivas |
|---|---|---|
| `domain/MestreIAUseCase.kt` | Orquestrador. Loop de tool calls, failover entre modelos, trava anti-confabulação. | `conversarComMestreIA()`, `executarLocalizar()`, `executarLer()` |
| `data/MestreIARepository.kt` | Motor de busca por palavra-chave + persistência do Códex. | `localizarNoCodex()`, `lerPaginas()`, `rankearPorBM25()`, `buscarPorPaginaESource()`, `sincronizarCodexSeNecessario()` |
| `data/MestreIAQueryEngine.kt` | Normaliza/expande query FTS4 (sinônimos, stopwords). **Usado pelo GraphEngine (Forjador), NÃO pelo localizar do Auditor** — o localizar tokeniza por conta própria. | `prepararQueryFTSAgressiva()` |
| `data/network/MestreIATools.kt` | Schemas JSON das tools. | `getAuditorToolsOpenAI()`, `getAuditorToolsGemini()` |
| `data/network/MestreIAClient.kt` | HTTP com os LLMs (DeepSeek/Gemini/etc). Seleciona toolset por modo. | `perguntarAoMestre()`, `gerarJsonOpenRouter()`, `gerarJsonGoogleNative()` |
| `data/network/MestreIAPromptsAuditor.kt` | System prompt do Auditor (loop localizar→ler, anti-confabulação). | `PROMPT` |
| `domain/MestreIACitationValidator.kt` | Detecta páginas citadas que não vieram dos chunks lidos → aviso ao usuário. | `extrair()`, `validar()`, `formatarAviso()` |
| `data/storage/ManualChunkDao.kt` | DAO Room FTS4. | `buscarRegras()` (FTS4 MATCH), `buscarPorPaginaESource()` |
| `data/storage/FichaDatabase.kt` | Importa `chunks.jsonl` → tabela `manual_chunks` (+ `vec_chunks` se houver embedding). | `prePopulateManual()` |

---

## 2. AS DUAS FERRAMENTAS DO AUDITOR (Lote 325, fiação corrigida no 326-fix)

Definidas em `MestreIATools.getAuditorToolsOpenAI()` (DeepSeek) e `getAuditorToolsGemini()` (Gemini).
Selecionadas em `MestreIAClient` (`gerarJsonOpenRouter`/`gerarJsonGoogleNative`) quando `modo != geracao/analise`.

- **`localizar_no_codex(termos, livros?)`** — "página de resultados". FTS4 AND (cada palavra
  estreita); fallback OR se AND vazio. Retorna lista compacta livro|página|trecho, **rankeada
  por BM25** (Lote 327). NÃO devolve texto completo. `livros` é array opcional de filtro.
- **`ler_pagina(livro, pagina, pagina_final?)`** — texto COMPLETO da página/intervalo (máx 4 págs).
  É a única fonte de verdade para citar.
- Auxiliares: `inspecionar_personagem(secao)`, `consultar_nexus_arcano(magia_alvo)`.

---

## 3. RANKING (Lote 327) — `MestreIARepository.rankearPorBM25()`

Extraído fielmente do scoring que JÁ existia em `MestreIAGraphEngine` (e estava sendo bypassado).
Score por chunk = BM25 (tf/idf/tamanho) + bônus de cobertura de termos (10×presentes/total)
+ bônus de proximidade (+5 se 2 termos a <100 chars) − penalidade de índice (págs <30 do MB).
**Só lexical** — sem reranking semântico (esse era o papel do embedding, removido do Auditor).

`corpusSizeCache` (1197) alimenta o IDF. `avgdl=900` (`calcularAvgdlCorpus`).

---

## 4. TRAVA ANTI-CONFABULAÇÃO (Lote 328) — em `MestreIAUseCase`

Problema observado nos testes: o ranking melhorou a busca mas o modelo ainda citava páginas
**de memória** quando localizava muito e lia pouco.

- Rastreador `leuAlgumaPagina` (true só quando um `ler_pagina` retorna chunks reais).
- Na resposta final: **bifurca a instrução**:
  - **Não leu nada** → instrução exige declarar "não localizei", proíbe citar de memória.
  - **Leu** → instrução exige citar SOMENTE páginas lidas, sem acrescentar de memória.
- Prompt ganhou "REGRA DE OURO DO LOOP": localizar só aponta, ler_pagina é que dá a regra;
  se localizou e não leu, a próxima ação é ler, nunca responder.
- Log: `ÚLTIMA ITERAÇÃO: ... | leuAlgumaPagina=true/false`.

---

## 5. ⚠️ CÓDIGO LEGADO / MORTO (NÃO confiar, NÃO reusar sem revisar)

Estas peças continuam compiladas mas **não fazem parte do fluxo vivo do Auditor**. Mantidas
por (a) o Forjador ainda usar parte, ou (b) ninguém ter removido. Documentadas para evitar que
um futuro "vou reusar o que já existe" caia numa armadilha.

### 5.1 `MestreIAGraphEngine.kt` (603 linhas) — ⚠️ NÃO usado pelo Auditor desde Lote 325
- Era o motor RAG semântico: BM25 + HNSW + reranking + diversificação (`buscarDiretoNoCodex`).
- **Hoje o Auditor NÃO o chama.** Caller vivo: **a Voz** (`ui/components/GeminiLiveTools.kt`
  instancia o próprio `MestreIAGraphEngine(repo)` e chama `buscarDiretoNoCodex`).
  O caller morto `gerarCatalogoDireto` foi REMOVIDO no Lote 349 (ver 5.3).
- **Lote 349:** ganhou cabeçalho-guarda `⚠️ USADO APENAS PELA VOZ (GeminiLive) E FORJADOR`.
- O scoring BM25 dele foi **copiado** (não movido) para `rankearPorBM25` no Repository (Lote 327).
  Se for ajustar ranking do Auditor, mexa em `rankearPorBM25`, **não aqui**.
- A flag `MODO_HNSW_PURO` (companion object) ainda existe e é setada `true` em
  `FichaIADelegate.kt` (linha 55) — mas só afeta este GraphEngine, que o Auditor não usa.
  Relevante apenas para Forjador/Voz.

### 5.2 `executarBuscaCodex()` em `MestreIAUseCase.kt` — ✅ REMOVIDO no Lote 349
- Era o helper das 5 tools de embedding antigas (`consultar_manual_direto`, `consultar_regras_magia`,
  `_armas_fogo`, `_artes_marciais`, `_aquatico`); chamava `graphEngine.buscarDiretoNoCodex`.
- Lote 349 removeu a função, os 5 `when` cases mortos do dispatch, o campo `graphEngine` do
  UseCase e o subtipo `ToolResult.Duplicada` (+ mecanismo `todasDuplicadas`), que só ela alimentava.

### 5.3 `gerarCatalogoDireto()` + `reescreverQueryParaGurps()` — ✅ REMOVIDOS no Lote 349
- Estavam em `MestreIAUseCase.kt` com zero callers (pré-contexto RAG do fluxo antigo).
- A data class `CatalogoLocalResult` (tipo de retorno) foi removida junto.

### 5.4 `MestreIATopicIndex.kt` — ✅ DELETADO no Lote 349
- Morto desde o Lote 272; nenhum arquivo o referenciava. Os assets `topic_index*.json`
  foram movidos para `lixeira/assets_lote349/` (fora do APK).
- Decisão de design (validada com usuário): determinismo por tópico NÃO serve para este corpus
  (mesma página viria para perguntas diferentes sobre o mesmo substantivo). Não reviver sem
  rediscutir.

### 5.5 `MestreIAPlanner.kt` (879 linhas) — ✅ DELETADO no Lote 349
- Lógica de planejamento com 7 dicionários hardcoded — causava alucinação léxica, fora do
  fluxo desde o Lote 319. Só o TIPO `TermoPonderado` era usado.
- A data class `TermoPonderado` foi MOVIDA para `MestreIAQueryEngine` (agora
  `MestreIAQueryEngine.TermoPonderado`); assinaturas em `MestreIAQueryEngine` e
  `MestreIAGraphEngine` atualizadas.

### 5.6 `MestreIAVectorEngine.kt` (160 linhas) / `MestreIASemanticEngine.kt` (203 linhas) — ⚠️ dormentes no Auditor
- Busca semântica HNSW (`MestreIAVectorEngine`, via ObjectBox) e reranking cosseno
  (`MestreIASemanticEngine`). **O Auditor não chama mais** (Lote 325).
- São acionados pela **Voz** quando o GeminiLiveTools dispara o GraphEngine (caller real);
  Forjador idem se acionar o GraphEngine.
- Os embeddings (`chunks.jsonl`, 54.9MB / tabela `vec_chunks`, entidade `VecChunkEntity`)
  ficam **dormentes** para o Auditor. Existe `chunks.jsonl.bak` (mesmo texto, sem embeddings,
  6.5MB) para troca futura — ver memória.
- Utilitários puros `floatArrayToByteArray`/`byteArrayToFloatArray` (de `MestreIASemanticEngine`)
  AINDA são usados na importação de embeddings (`FichaDatabase.prePopulateManual`).

### 5.7 Toolset legado de embedding em `MestreIATools` — ✅ DELETADO no Lote 350
- Eram os schemas das 5 tools de embedding (`consultar_manual_direto` + 4 especializadas) +
  `fill_character_sheet`: `getLegacyEmbeddingToolsOpenAI/Gemini` (ex-`getOpenAITools`/`getGeminiTools`,
  renomeadas no Lote 349 com comentário-guarda).
- 🐛 **BUG CORRIGIDO no Lote 350:** `getAuditorUnificadoToolsOpenAI/Gemini` (toolset do modo
  `analise`, commit d9d999c) montavam a base chamando o toolset LEGADO de embedding acreditando
  que ele continha as ForjadorTools. Resultado: o modo `analise` oferecia ao modelo 8 schemas
  que o executor (`MestreIAGeneratorUseCase`) não roda e NÃO oferecia as tools de ficha do
  Forjador. A base agora é `ForjadorTools.getOpenAITools()/getGeminiTools()` + localizar/ler/nexus
  (a duplicata de `consultar_nexus_arcano` sumiu junto — as ForjadorTools não incluem nexus).
- Com a correção o toolset legado ficou sem callers e foi DELETADO, junto com `getSheetSchema*`,
  os helpers privados `getArrayOf*` e as constantes `TOOL_MANUAL_DIRETO`/`TOOL_REGRAS_*`
  (~460 linhas). `TOOL_FILL_SHEET` e `TOOL_INSPECT_CHARACTER` permanecem — ainda referenciadas
  em `FichaIADelegate`/`MestreIAUseCase` e nos toolsets do Auditor.

---

## 6. DADOS (Códex)

- `assets/chunks.jsonl` (54.9 MB): 1197 chunks = **1 por página** (mediana ~5700 chars). Campos:
  chunk_id, source_id, source_title, page_number, text, **embedding** (3072 dims).
- 5 livros (source_id): `pt_modulo_basico` (578), `pt_magia` (284), `pt_artes_marciais` (264),
  `pt_gun_fu` (49), `pt_pyramid_26_underwater` (22).
- `assets/chunks.jsonl.bak` (6.5 MB, 1196 linhas — 1 a menos que o .jsonl): idêntico SEM
  embeddings. Import tolera ausência (`if (obj.has("embedding"))` em FichaDatabase). Candidato a
  substituir o .jsonl quando se confirmar que o Auditor não precisa mais de embedding.
- Importação: `FichaDatabase.prePopulateManual` → `manual_chunks` (FTS4, DAO `ManualChunkDao`) +
  `vec_chunks` (vetores, entidade `VecChunkEntity`, DAO `VecChunkDao`). O Room está em
  **version = 24** (`FichaDatabase`). Versão do search_text controlada por `CODEX_VERSION_CURRENT`
  (= 3) no Repository.

---

## 7. HISTÓRICO DE LOTES (RAG do Auditor)

| Lote | Mudança |
|---|---|
| 271 | Busca livre — IA controla queries (até 5 tool calls). |
| 272 | TopicIndex removido do fluxo (hoje morto). |
| 315 | Verificador de Citações (`MestreIACitationValidator`). |
| 316 | maxTokens 16384 na resposta final + leitura cuidadosa. |
| 317/318 | 5 tools especializadas por livro, descriptions categoriais (sem exemplos). |
| 319 | Removido Planner do fluxo Live (alucinação léxica dos 7 dicionários). |
| **325** | **Virada: Auditor troca busca semântica por "grep + leitura dirigida"** (localizar+ler). |
| 325-fix | Corrige build quebrado + fiação real das tools (`getAuditorTools*`) + prompt real. |
| 326 | Logs de avaliação (dispatch correto, lista de páginas, preview de leitura). |
| 327 | `rankearPorBM25` — religa o ranking que o 325 havia bypassado (lista deixa de vir por nº de página). |
| 328 | Trava anti-confabulação (leuAlgumaPagina) + regra de ouro do loop no prompt. |

---

## 8. PONTOS DE ATENÇÃO FUTUROS

- **Custo:** cada iteração reenvia o contexto acumulado; localizar que devolve muita página
  infla o request. O ranking (327) mitigou; paginação do localizar (ideia do usuário) é o
  próximo passo natural se o custo ainda incomodar.
- **Limpeza pendente:** ✅ FEITA no Lote 349 — 5.2, 5.3, 5.4 e 5.5 removidos (~1.900 linhas).
  GraphEngine/Vector/Semantic preservados (a Voz depende deles) com cabeçalhos-guarda.
- **Bug do toolset unificado (modo `analise`):** ver 5.7 — corrigir em lote dedicado.
- **Princípio inviolável:** prompts do Auditor são CATEGORIAIS, **sem exemplos hardcoded**
  (lição do Lote 318 — exemplo vira cola e cria viés direcionado).

---

## 9. FORJADOR — fluxo atual (JSON direto) + Templates + Mestre Pintor [+ 2026-06-09]

### 9.1 Fluxo JSON-direto (criação de ficha)
O Forjador (`MestreIAGeneratorUseCase`) hoje **não usa mais o loop de tools para CRIAR**. O modelo
recebe o prompt (`MestreIAPromptsForjador`) + catálogo + budget + (opcional) um TEMPLATE base, e
**retorna um JSON completo** da ficha. O app então percorre o JSON **pilar a pilar** (atributos →
raça → vantagens → desvantagens → perícias → técnicas → magias → equipamentos), aplicando cada item
via `ForjadorToolExecutor.aplicarEdit`.
- **Resolução de IDs:** `resolverId` (alias → exato → fuzzy por nome) em TODAS as seções de catálogo,
  antes de descartar um item.
- **Diagnóstico:** logs `MestreIA_JSON` (JSON cru + IDs por seção) logo após o parse.
- **Correções (Lotes recentes):** raça "null" (org.json devolve string "null"); bug de pré-requisito
  plural de técnica ("perícias pré-requisitos") no `FichaSkillDelegate.extrairAncoraPericiaNoLimite`;
  **GPS de técnicas** (`ForjadorToolExecutor.gpsAdicionarPericiaBaseDeTecnica` — adiciona a perícia-base
  nomeada no pré-req quando a ficha não tem nenhuma compatível). Detalhes: `project_forjador_pendencias`.

### 9.2 Templates de personagem (`ForjadorTemplateCatalogo` + `forjador_templates.json`)
60 arquétipos prontos. **É o CÓDIGO que escolhe** o template mais próximo do pedido (match por
palavra-chave em id/nome/descrição/tags via `escolher()`), **não a IA**. O bloco do template é
injetado no prompt na 1ª iteração como "ponto de partida". `pontosBase` é só REFERÊNCIA — o budget
real é sempre o do pedido do usuário. Todos os IDs dos templates são validados contra o catálogo.

### 9.3 Mestre Pintor (`GeminiImageService`)
Gera retrato artístico do personagem via Gemini Image API (`gemini-3.1-flash-image`, chave PAGA,
~$0,067/imagem). Fluxo: `FichaIADelegate.gerarRetratoIA()` → `GeminiImageService.gerarRetrato()` →
`ImagemPersonagemStore.salvarImagem()` → `FichaViewModel.atualizarImagemPersonagem()`. Acionado por
dialog pós-Forjador ou pelo modo "pintor" no chat. (A imagem entra no fluxo da feature de retrato:
cabeçalho + tela cheia + Discord + viaja embutida na ficha exportada.)

---

## 10. NARRADOR (modo Saga) + MOTOR DE COMBATE  [+ 2026-06-13/14, Lotes 349-370]

**3º modo de IA** sobre o mesmo `MestreIAClient`: modo `saga`. O NARRADOR conduz uma aventura solo
(RPG de mesa para 1 jogador) usando a ficha como herói. Plano canônico: `.agent/skills/PLANO_SAGA_CLAUDE_CODE.md`.

### 10.1 Loop do Narrador (clone do Forjador, modo `saga`)
```
Mensagem do jogador
  → MestreIANarradorUseCase.narrar()  [fila de fallback de modelos; loop de tool-use; Auto-Healing]
      → consultar_mundo automático (top-5 por palavra-chave) injeta fatos canônicos
      → modelo chama as NarradorTools (rolagem, combate, fatos, cena, XP, Códex…)
          → NarradorToolExecutor roteia cada tool → resultado JSON factual
      → prosa final (≤3 parágrafos, 2ª pessoa)
  → NarradorOutputValidator: zero números/regras inventados (alarme se a prosa cita dado/PV/dano
    que não veio de tool naquele turno)
```
- **Lei de ferro:** o Narrador NUNCA declara resultado mecânico de cabeça — chama a ferramenta e narra a consequência.
- Narração no Gemini 2.5 **Flash** (não Pro) — turno ~rápido (Lote 355).

### 10.2 Arquivos do Narrador
| Arquivo | Papel |
|---|---|
| `domain/MestreIANarradorUseCase.kt` | Orquestrador do modo `saga` (clone do Generator: fallback de modelos + loop de tools + Auto-Healing + consultar_mundo automático). |
| `domain/saga/NarradorTools.kt` | Schemas das **16 tools** (14 próprias + `localizar_no_codex`/`ler_pagina` reusadas do Auditor). Spec neutra única → Gemini e OpenAI. CATEGORIAL, zero exemplos. |
| `domain/saga/NarradorToolExecutor.kt` | Roteador nome→implementação. Reais: registrar_fato, consultar_mundo, inspecionar_personagem, definir_cena, pedir_rolagem (via `RollBridge`), localizar/ler, **e as 6 de combate via `CombatBridge`** (iniciar_combate, acao_npc, aplicar_dano, aplicar_condicao, gastar_recurso, conceder_xp). `avancar_relogio`/`passar_tempo`/`forjar_npc` = `nao_implementado` (Fase C/D). |
| `domain/saga/NarradorOutputValidator.kt` | Detector anti-confabulação da prosa (números/regras sem tool). |
| `domain/saga/CampanhaConfig.kt` | Session zero (gênero/tom/dificuldade/magia/NT/livros) → bloco textual no prompt. |
| `data/network/MestreIAPromptsNarrador.kt` | Persona do Narrador (categorial). Leis de ferro incl. a nº 8 (combate abre com `iniciar_combate`; jogador resolve na UI; narra só abertura+desfecho). |
| `viewmodel/delegates/FichaSagaDelegate.kt` | Estado observável da aba Saga; implementa `RollBridge` (rolagem interativa) **e `CombatBridge`** (liga as tools ao combate + ficha); fim de combate → `narrarFimDeCombate` (prosa + saque + XP). Persiste turnos em tabelas de chat (sessão `saga#<id>`). |
| `ui/TabSaga.kt` | UI da aba (lista/criação de campanha, feed/máquina de escrever, card de rolagem, `ConfiguracaoJogoDialog`, TalkBack). Mostra o `CombatePainel` quando há combate. |
| `data/storage/SagaEntities.kt` / `SagaDao.kt` | Room: Campanha/Cena/CampaignFact (FTS4)/WorldState. `FichaDatabase` hoje em **v26** (MIGRATION_24_25 + 25_26 explícitas). |

### 10.3 Motor de combate (`domain/combat/`, Kotlin PURO, testável) — Lotes 359-370
Tudo determinístico (RNG por seed injetável), sem Android. Encadeado pela `CombatSession`.
| Arquivo | Papel |
|---|---|
| `CombatModels.kt` | Enums `Postura`/`Condicao`/`Manobra`, `NpcStats` (tem `armaNh`), `Combatente` (estado mutável). |
| `CombatEncounter.kt` | Iniciativa (Vel.Básica→DX→seed), `proximoTurno`, `manobrasLegais`, distância MUTÁVEL (`moverEmRelacaoAoHeroi`). |
| `CombatActions.kt` | `calcularNH` (postura/local/visibilidade/`modsExtra`) + `resolverAtaque` (3d6). **Mover e Atacar: CaC −4+teto 9; à distância −2** (corrigido no Lote 368 lendo o Códex). |
| `ModificadoresCombate.kt` | Tabelas: `LocalAtaque` (penalidades MB p.398), `Visibilidade`, `AtaqueTotalModo`. |
| `HitLocationRules.kt` | Dano localizado (paridade com a calculadora da Mesa Virtual: crânio ×4, vitais ×3 perf, limites de membro). |
| `InjuryRules.kt` | Choque, ferimento grave, cheques de morte, inconsciência, recuperação de atordoamento. |
| `NpcCombatBrain.kt` | Cérebro tático do NPC (fuga por moral, arqueiro mantém distância, bruto avança) — determinístico. |
| `CombatResolver.kt` | Camada de defesa (recuo/Defesa Total/apara extra/bloqueio 1×) + `resolverTroca` (ataque→defesa→dano→ferimento; crítico anula defesa). |
| `CombatSession.kt` | **Orquestra o encontro inteiro:** `heroiAtaca(AtaqueHeroi,…)`, `npcIntencao`/`npcResolve` (defesa interativa "Defenda-se!"), `heroiMove`/`heroiManobra`/`heroiAvaliar` (manobras), `narrarTroca` (log EVOCATIVO + colchete técnico), parser de dano `tipoDano`/`rolarDano` (mapeia `pa*`→`pi*`), `penalidadeDistancia` (MB p.550). `HeroiPerfilCombate` (defesas) + `AtaqueHeroi` (arma escolhível). |
| `model/BestiarioModels.kt` + `domain/loaders/BestiarioCatalogo.kt` | Bestiário (17 criaturas, `assets/bestiario.v1.json`) → `Combatente`. Loader com cache. |
| `viewmodel/delegates/SagaCombatController.kt` | **Ponte motor↔UI:** estado Compose (`CombatUiState`/`CombatenteUi`/`FaixaDistancia`), corrotinas, ponte de defesa suspensa, lista de ataques da ficha (corpo-a-corpo + fogo/distância, perícia casada por grupo/nome), devolve PV/saque/XP à ficha. |
| `ui/saga/CombatUi.kt` | UI aprovada no mockup: CombatTracker (faixas/PV/postura, inicial colorida = placeholder do retrato real — ver registro B7/E2), SeletorDeArma, ManeuverCards + sub-diálogos (alvo/local, Mover, Avaliar, Postura), DefendaSeCard. TalkBack em tudo. |

### 10.4 Estado e pendências
- **FASE B (combate) COMPLETA + polimento 367-370.** Falta do pedido do usuário: **Apontar (+Precisão)** e **Preparar/Sacar arma** (arma pronta vs guardada) — exigem puxar o `Acc`/alcance da arma do catálogo (`ArmaCatalogoItem`/`Equipamento` ainda não carregam isso).
- **Validação no aparelho** do combate ponta a ponta segue pendente (chaves de IA reais).
- **Retratos reais de NPC/cena** gerados pelo Mestre Pintor em tempo real = registro futuro (Lotes B7/E2 do plano).
