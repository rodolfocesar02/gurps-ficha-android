# ⚙️ ARQUITETURA DO MOTOR "MESTRE IA"

> **Atualizado em 2026-06-08 (revisão de fidelidade pós-Lote 328).** Documento reescrito após
> auditoria linha-a-linha de todos os arquivos do Auditor (revisão 2026-06-08 reconferiu
> contagens/números de linha contra o código real e confirmou a Voz como caller ativo do
> GraphEngine). Substitui a versão antiga (que descrevia o RAG semântico pré-Lote 325). Seções de
> **código LEGADO/MORTO** estão marcadas com ⚠️ e o lote em que deixaram de ser usadas — leia-as
> antes de mexer em qualquer coisa.

Sistema de IA (RAG) integrado ao app de Ficha GURPS. **Dois modos** sobre a mesma base:

1. **AUDITOR** (modo `conversa`/dúvida): responde regras de GURPS. **Desde o Lote 325 usa
   busca por palavra-chave ("grep + leitura dirigida"), NÃO mais busca semântica/embedding.**
2. **FORJADOR** (modo `geracao`/`analise`): cria fichas via `fill_character_sheet`. Não foi
   tocado pelos Lotes 325-328; mantém seu próprio toolset (`ForjadorTools`).

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
- **Hoje o Auditor NÃO o chama.** É alcançado por `gerarCatalogoDireto` (ver 5.3, morto) e
  **ATIVAMENTE pela Voz** (`ui/components/GeminiLiveTools.kt` instancia o próprio
  `MestreIAGraphEngine(repo)` e chama `buscarDiretoNoCodex` — caller real, não hipotético).
- O scoring BM25 dele foi **copiado** (não movido) para `rankearPorBM25` no Repository (Lote 327).
  Se for ajustar ranking do Auditor, mexa em `rankearPorBM25`, **não aqui**.
- A flag `MODO_HNSW_PURO` (companion object) ainda existe e é setada `true` em
  `FichaIADelegate.kt` (linha 55) — mas só afeta este GraphEngine, que o Auditor não usa.
  Relevante apenas para Forjador/Voz.

### 5.2 `executarBuscaCodex()` em `MestreIAUseCase.kt` (linha ~521) — ⚠️ LEGADO (Lote 317, morto no 325)
- Helper das 5 tools de embedding antigas (`consultar_manual_direto`, `consultar_regras_magia`,
  `_armas_fogo`, `_artes_marciais`, `_aquatico`). Chama `graphEngine.buscarDiretoNoCodex`.
- Os `when` cases dessas tools AINDA existem no dispatch do UseCase, mas **as tools não são mais
  oferecidas ao modelo** (o toolset do Auditor agora é só localizar+ler). Então os cases nunca
  disparam. Mantidos como rede; podem ser removidos num lote de limpeza.

### 5.3 `gerarCatalogoDireto()` + `reescreverQueryParaGurps()` em `MestreIAUseCase.kt` — ⚠️ MORTO
- `gerarCatalogoDireto` (linha ~623): zero callers no app. Era usado pelo fluxo antigo de
  pré-contexto RAG. Chama GraphEngine + Planner + query-rewrite via Gemini Lite.
- `reescreverQueryParaGurps` (linha ~681): só chamado por `gerarCatalogoDireto` → morto junto.

### 5.4 `MestreIATopicIndex.kt` (123 linhas) — ⚠️ MORTO desde Lote 272
- Mapa "tópico → páginas garantidas". **Nenhum arquivo o referencia** (nem `carregar()` é
  chamado). Comentário no GraphEngine diz "TopicIndex removido (Lote 272)".
- Decisão de design (validada com usuário): determinismo por tópico NÃO serve para este corpus
  (mesma página viria para perguntas diferentes sobre o mesmo substantivo). Não reviver sem
  rediscutir.

### 5.5 `MestreIAPlanner.kt` (879 linhas) — ⚠️ quase morto (só o TIPO é usado)
- Lógica de planejamento de busca com 7 dicionários hardcoded — **causava alucinação léxica**,
  removida do fluxo no Lote 319.
- Hoje só a data class `MestreIAPlanner.TermoPonderado` é referenciada, como parâmetro com
  default vazio em assinaturas (`MestreIAQueryEngine` linha 90, `MestreIAGraphEngine` linha 51,
  `MestreIAUseCase` linha 627). A lógica nunca roda no Auditor.

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

### 5.7 `consultar_manual_direto` + 4 especializadas em `MestreIATools.getOpenAITools/getGeminiTools` — ⚠️ LEGADO p/ Auditor
- Schemas das 5 tools de embedding. `getOpenAITools`/`getGeminiTools` ainda existem e são usadas
  pelo **Forjador** (que precisa de `fill_character_sheet` etc.). Para o Auditor foram
  substituídas por `getAuditorTools*`. Não confundir os dois conjuntos.

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
- **Limpeza pendente:** 5.2, 5.3, 5.4 podem ser removidos num lote dedicado (reduz 1000+ linhas
  mortas). Não remover sem confirmar que Forjador/Voz não dependem do GraphEngine.
- **Princípio inviolável:** prompts do Auditor são CATEGORIAIS, **sem exemplos hardcoded**
  (lição do Lote 318 — exemplo vira cola e cria viés direcionado).
