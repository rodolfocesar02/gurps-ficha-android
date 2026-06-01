# Mapa Detalhado: Arquivos e Funções do Projeto GURPS

Mapa de engenharia completo do projeto. Use para localizar lógicas específicas sem varrer o código.
Atualizado em: 2026-05-30 (seção 11/13/14 do Mestre IA revistas pós-Lote 328) | 130+ arquivos documentados.

> ⚠️ **AUDITOR mudou de motor (Lotes 325-328):** saiu da busca semântica (RAG/HNSW) para
> "grep + leitura dirigida" (`localizar_no_codex` + `ler_pagina`). Vários arquivos abaixo
> viraram LEGADO/MORTO — marcados com ⚠️. Detalhe e motivo de cada um em
> `ARQUITETURA_MESTRE_IA.md §5`.

---

## 1. Ponto de Entrada

- **`MainActivity.kt`** — Activity principal. Inicializa o Compose, intercepta Intents de compartilhamento de ficha (importação via `.gurps` compartilhado), passa o Intent para o ViewModel processar. Única Activity do app.

---

## 2. ViewModel e Estado Central

- **`viewmodel/FichaViewModel.kt`** — O controlador central do app. Instancia todos os delegates, mantém o `Personagem` ativo como `mutableStateOf`, coordena auto-save ao editar traços, e expõe métodos públicos que a UI chama. Delega todas as operações especializadas para os delegates.

- **`viewmodel/FichaUIState.kt`** — Data classes dos estados de busca da UI: `TraitSearchState`, `SkillSearchState`, `MagicSearchState`, `TechniqueSearchState`, `EquipmentSearchState`. Sem lógica — só estruturas de dados para os filtros de catálogo.

---

## 3. Delegates do ViewModel

*Cada delegate é responsável por uma fatia da lógica do `FichaViewModel`. Sem delegates, o ViewModel seria um arquivo de 4.000+ linhas.*

- **`delegates/FichaAttributeDelegate.kt`** — Atualiza atributos primários (ST/DX/IQ/HT), atributos secundários (mod PV, PF, Vontade, Percepção, Velocidade, Deslocamento), e dados básicos do personagem (nome, jogador, campanha, histórico, aparência, notas). Aplica limites via `coerceIn`.

- **`delegates/FichaCombatDelegate.kt`** — Calcula e atualiza defesas ativas (Esquiva, Apara, Bloqueio). Inclui bônus manuais, seleção de perícia de Apara e de Escudo para Bloqueio. Retorna a lista de `ActiveDefense` para a UI exibir.

- **`delegates/FichaEquipmentDelegate.kt`** — Adiciona equipamentos gerais, armas do catálogo e armaduras. Gera as notas automáticas de armas (valor de Aparar, classe de arma de fogo). Filtra tags de armaduras. Contém lógica de classificação de armas de fogo por categoria (pistola, rifle, ultratech, pesada).

- **`delegates/FichaIADelegate.kt`** — Gerente completo da IA. Instancia `MestreIAUseCase` e `MestreIAGeneratorUseCase`. Controla o histórico de chat (`mestreIAChatHistory`), sessões persistidas (Room), modo da IA (`conversa`/`geracao`/`analise`), auto-sincronização do Códex, e o sistema de "bolhas batch" (agrupa eventos consecutivos do Forjador na mesma bolha de chat).

- **`delegates/FichaMagicDelegate.kt`** — Adiciona e remove magias. Valida pré-requisitos via `MagicEngine`, detecta duplicatas por escola (magias de múltiplas instâncias). Retorna escolas e classes únicas do catálogo para os filtros.

- **`delegates/FichaNetworkDelegate.kt`** — Envia rolagens para o Discord via `DiscordRollApiClient`, com retry em timeout. Busca lista de canais de voz do Discord. Sem estado próprio — só operações de rede.

- **`delegates/FichaPersistenceDelegate.kt`** — Salva, carrega e exclui fichas via `FichaStorageRepository`. Filtra o auto-save (`_autosave_recuperacao`) da listagem pública. Tenta import via `PersonagemInterop` e faz fallback para `Personagem.fromJson` em JSONs antigos.

- **`delegates/FichaSearchDelegate.kt`** — Mantém os estados de busca (`advantageSearch`, `disadvantageSearch`, `skillSearch`, `magicSearch`, `techniqueSearch`, `equipmentSearch`). Delega as filtragens para `DataRepository`. Cache simples para `filtrarMagias` (evita reprocessar a cada recomposição).

- **`delegates/FichaSkillDelegate.kt`** — Adiciona, remove e atualiza perícias. Valida pré-requisitos via `DataRepository.validarPreRequisitosPericia`. Detecta técnicas pelo nome normalizado (para não misturar com perícias). Recalcula NH ao alterar pontos gastos.

- **`delegates/FichaSocialDelegate.kt`** — Gerencia configuração de envio ao Discord: seleção de canal de voz, status de carregamento de canais, persistência do canal selecionado em `SharedPreferences`. Coordena com `FichaNetworkDelegate` para buscar canais.

- **`delegates/FichaTraitDelegate.kt`** — Adiciona, remove e edita vantagens e desvantagens. Valida duplicatas (permite múltiplas instâncias de `ataque_inato`, `golpeadores`, `resistencia_a_dano`). Normaliza o nível de vantagens acumulativas. Delega custo para `DataRepository.criarVantagemSelecionada`.

---

## 4. Domain — Engines

*Lógica de regras pura do GURPS 4ª Ed. Sem Android, sem UI.*

- **`domain/engine/MagicEngine.kt`** — Cálculo de Aptidão Mágica por escola (vantagem `aptidao_magica` com mod de escola). Valida se magia pode ter múltiplas instâncias. Valida pré-requisitos de magias (NH mínimo e magias dependentes). Calcula custo de energia ajustado pelo NH.

- **`domain/engine/SkillEngine.kt`** — Regras de técnicas: calcula o tipo de limite (`TecnicaLimiteKind`) de cada técnica (explícito relativo, baseado em perícia base, Aparar, Bloquear, metade da penalidade). Inclui normalização de texto para matching de nomes.

---

## 5. Domain — Rules

*Cálculos de regras encapsulados por tema.*

- **`domain/rules/CharacterRules.kt`** — A base de tudo. Tabelas de Golpe/Empurrão por ST. Cálculo de PV, PF, Velocidade Básica, Deslocamento, Percepção, Vontade. Custo de atributos primários e secundários. Cálculo de custo de vantagens com `specialRule` (Aliado, Inimigo, Dependente, Reputação, Dever, Manutenção, Vício, Contato, Dor Crônica, Fraqueza, Vulnerabilidade). Limite de desvantagens. Referência global `DATA_REPOSITORY_INSTANCE`.

- **`domain/rules/CombatRules.kt`** — Fórmulas de defesas ativas: `calcularEsquiva`, `calcularApara`, `calcularBloqueio` e suas variantes de base. Puro e sem dependências externas.

- **`domain/rules/MagiaEnergiaRules.kt`** — Redução de custo de energia por NH alto (NH≥15 → -1, NH≥20 → -2+). Parse de string de custo de energia ("2 pontos" → 2). Usado por `MagicEngine` e pelos diálogos de magia.

---

## 6. Domain — Trait Rules

*Regras especiais por ID de vantagem. Cada `TraitRule` implementa a interface e é registrada no Registry.*

- **`domain/rules/traits/TraitRule.kt`** — Interface base: `calculateCost`, `getAttackOptions`, `getDefenseOptions`, `getDamageOptions`, `getDodgeModifier`, `getBlockModifier`, `getParryModifier`, `getSkillModifiers`, `getDamageBonusPerDie`. Todas com default `null`/`emptyList`/`0`.

- **`domain/rules/traits/TraitRuleRegistry.kt`** — Singleton que registra todas as regras e expõe métodos agregadores: `getSkillBonus`, `getParryBonus`, `getDodgeBonus`, `getBlockBonus`, `getDamageBonusPerDie`. Usado pelo `Personagem` e pelo `FichaCombatDelegate`.

- **`domain/rules/traits/AtaqueInatoRule.kt`** — Custo e opções de ataque para `ataque_inato` (vantagem composta, calcula por dados/modificadores armazenados nos metadados).

- **`domain/rules/traits/GolpeadoresRule.kt`** — Custo e dano de `golpeadores` (Striker). Lê tipo de golpeador e metadados para calcular dano.

- **`domain/rules/traits/DentesRule.kt`** — Custo e dano de `dentes` (Bite). Calcula dano por tipo de mordida.

- **`domain/rules/traits/GarrasRule.kt`** — Custo de `garras` pelo metadado `tipoGarras` (cascos=3, afiadas=5, pontudas=8, longas_pontudas=11). Também expõe opções de ataque.

- **`domain/rules/traits/FlexibilidadeRule.kt`** — Bônus de perícia para `flexibilidade` (Contorcionismo, Acrobacia).

- **`domain/rules/traits/ApararAmpliadoRule.kt`** — Bônus de Apara para `aparar_ampliado`.

- **`domain/rules/traits/BloqueioAmpliadoRule.kt`** — Bônus de Bloqueio para `bloqueio_ampliado`.

- **`domain/rules/traits/EsquivaAmpliadaRule.kt`** — Bônus de Esquiva para `esquiva_ampliada`.

- **`domain/rules/traits/MestreDeArmasRule.kt`** — Bônus de dano por dado (`getDamageBonusPerDie`) para `mestre_de_armas`, filtrado por grupo de arma e perícia.

- **`domain/rules/traits/TelecomunicacaoRule.kt`** — Custo de `telecomunicacao` pelo metadado de alcance/tipo.

---

## 7. Domain — Loaders

*Carregamento e resolução dos catálogos JSON dos assets.*

- **`domain/loaders/CatalogLoaders.kt`** — Carrega todos os catálogos de assets: `vantagens.v3.json` (+ extras de artes marciais), `desvantagens.v2.json`, `pericias.json` (+ suplementares), `magias.json`, `tecnicas_*.json` (múltiplos arquivos), `armas_*.json`, `armaduras.json`, `escudos.json`. Registra erros de carga sem lançar exceção. Faz mojibake fix nos textos carregados.

- **`domain/loaders/MetacaracteristicaCatalogo.kt`** — Carrega `metacaracteristicas.v1.json` (catálogo de metacaracterísticas prontas como Gigante, Anão, etc.) e resolve cada uma como `ModeloRacial` usando `RacaCatalogo.resolver`. Formato "enxuto": sem custos no JSON, recalculado em runtime.

- **`domain/loaders/RacaCatalogo.kt`** — Carrega `racas.v1.json` (catálogo de raças jogáveis). Resolve `RacaDefinicao` → `ModeloRacial` casando IDs contra os catálogos de vantagens/desvantagens/perícias via `DataRepository`. Custo recalculado pelo `CharacterRules` — imune a custo salvo errado. É também o schema de raças para o Forjador IA.

---

## 8. Domain — Filters

- **`domain/filters/TextNormalizer.kt`** — (Lote 314) Normalizador único do projeto. 4 presets: `SIMPLE` (sem acento + lowercase), `BUSCA_PADRAO` (+ mojibake fix + colapsa não-alfanuméricos), `PERICIA_RAW` (preserva `/+_-`), `ARMA_GRUPO` (strip parênteses + despluralização para Mestre de Armas). Consumido por `CatalogFilters`, `DialogsTecnicas`, `SkillEngine`, `DataRepository`. **Pendência:** `MestreDeArmasRule.normalize` ainda tem cópia local — migrar para preset `ARMA_GRUPO` quando houver ficha de teste pronta.

- **`domain/filters/CatalogFilters.kt`** — Fachada pública de busca/comparação. `normalizarBusca` e `contemBusca` delegam ao `TextNormalizer.BUSCA_PADRAO`. Usados em todo o app para filtros de catálogo (vantagens, perícias, magias, armas, armaduras, escudos). Inclui também `normalizarLocal` (caso específico de armaduras — substitui espaço por `_`, não migrado para o TextNormalizer).

---

## 9. Domain — Magias (Nexus Arcano)

- **`domain/magias/NexusArcanoModoAlvoAdapter.kt`** — Adapter entre o `NexusArcanoEngine` (módulo separado) e o ViewModel. Traduz `List<MagiaDefinicao>` em `ArcanoCatalogo`, chama o engine para calcular trilha ótima (A*/guloso), e retorna `NexusArcanoModoAlvoSnapshot` com relacionados, chaves, trilha mínima e avisos.

---

## 10. Domain — Roll

- **`domain/roll/RollDispatchPolicy.kt`** — Política de retry e mensagens de erro para envio de rolagens ao Discord. `deveRetentar` retorna `true` só para timeout (statusCode null). `mensagemErro` mapeia HTTP 401/400/500/502 para mensagens amigáveis.

---

## 11. Domain — MestreIA (Núcleo da IA)

*Para detalhes técnicos do fluxo de IA (prompts, loop de tool-use, FTS, decisões de arquitetura), ver `ARQUITETURA_MESTRE_IA.md`.*

- **`domain/MestreIAContextFilter.kt`** — Gera a string de contexto da ficha enviada para a IA: nome, atributos, HP/FP atual, vantagens, desvantagens, principais perícias. No modo `conversa` inclui aparência e histórico. Filtra metadados técnicos.

- **`domain/MestreIAGeneratorUseCase.kt`** — Orquestra o fluxo FORJADOR (criação de personagem). Usa `MestreIAClient` com modo `geracao`/`analise`, executa `ForjadorToolExecutor` a cada tool call recebida (ler ficha, buscar catálogo, GPS magia, editar ficha), faz até N iterações do loop de tool-use. Valida resposta final via `MestreIAValidacaoReport`.

- **`domain/MestreIAGraphEngine.kt`** — ⚠️ **LEGADO p/ Auditor desde Lote 325.** Motor RAG semântico (BM25 + HNSW + diversificação + "Ponte de Ferro"). Hoje só alcançado por `gerarCatalogoDireto` (morto) e potencialmente Forjador/Voz. O scoring BM25 daqui foi **copiado** para `MestreIARepository.rankearPorBM25` (Lote 327) — ajustar ranking do Auditor é LÁ, não aqui.

- **`domain/MestreIAPlanner.kt`** — ⚠️ **QUASE MORTO desde Lote 319.** A lógica de planejamento (dicionários hardcoded) causava alucinação léxica e foi removida do fluxo. Hoje só a data class `TermoPonderado` é usada como TIPO (parâmetro com default vazio que nunca recebe valor real). Nenhum `PlanoDeBusca` roda no Auditor atual.

- **`domain/MestreIARuleAuditor.kt`** — Auditor fiscal (Lote 55). Compara a `MestreIAResponse` sugerida pela IA contra os cálculos reais do `CharacterRules`. Gera lista de `AuditNote` com campo, valor sugerido vs. correto. Usado pelo Forjador para detectar custo errado de atributos.

- **`domain/MestreIAUseCase.kt`** — Orquestra o fluxo AUDITOR. **Desde Lote 325 NÃO usa RAG semântico:** loop de tool-use com `localizar_no_codex` (FTS4 AND/OR + ranking BM25) e `ler_pagina` (texto completo), via `MestreIARepository` — até **8 iterações** (`MAX_TOOL_CALLS`). **Lote 328:** trava anti-confabulação (`leuAlgumaPagina`) — se nunca leu página, força declarar "não localizei" em vez de citar de memória. `ehErroDeApi()` preciso. ⚠️ Contém funções legadas no mesmo arquivo: `executarBuscaCodex` (cases das 5 tools de embedding nunca disparam — Lote 317→325) e `gerarCatalogoDireto`/`reescreverQueryParaGurps` (MORTAS, zero callers).

- **`domain/MestreIACitationValidator.kt`** — (Lote 315, VIVO) Verificador de Citações. Extrai citações `[Livro, Pág]` da resposta e compara com as páginas dos chunks lidos; o que não bate vira aviso "⚠️ não verificadas" anexado à resposta. Não bloqueia — apenas avisa.

- **`domain/MestreIATopicIndex.kt`** — ⚠️ **MORTO desde Lote 272.** Lê `topic_index.json` para "páginas garantidas", mas NENHUM arquivo o referencia (nem `carregar()`). Determinismo por tópico foi rejeitado. Não reviver sem rediscutir.

- **`domain/MestreIASemanticEngine.kt` / `MestreIAVectorEngine.kt`** — ⚠️ **DORMENTES p/ Auditor desde Lote 325.** Reranking cosseno e busca HNSW (ObjectBox). Só via GraphEngine (Forjador/Voz). Os utilitários `floatArrayToByteArray`/`byteArrayToFloatArray` do SemanticEngine ainda são usados na importação de embeddings (FichaDatabase).

- **`domain/MestreIAValidacaoReport.kt`** — Data classes do relatório de validação do Forjador: `ItemValidacao` (entrada, idEncontrado, status, mensagem) e `RelatorioValidacao` (vantagens/desvantagens/perícias/magias/técnicas, totalOk, totalFallback, alertaBudget). `StatusValidacao` enum: OK, FUZZY, FALLBACK, ERRO.

---

## 12. Domain — Tools (Forjador)

- **`domain/tools/ForjadorTools.kt`** — Define os schemas das 4 ferramentas do Forjador: `forjador_ler_ficha`, `forjador_buscar_catalogo`, `forjador_gps_magia`, `forjador_editar_ficha`. Exporta formato nativo Gemini (`getGeminiTools`) e formato OpenAI (`getOpenAITools`).

- **`domain/tools/ForjadorToolExecutor.kt`** — Executor das ferramentas do Forjador. Mapeia nome da tool → implementação Kotlin: `lerFicha` (lê seção do personagem), `buscarCatalogo` (busca em vantagens/desvantagens/perícias/magias + injeta `RegrasEspeciaisSchema`), `gpsMagia` (trilha mínima via NexusArcano), `editarFicha` (aplica mudanças no personagem via ViewModel). Faz read-back pós-edição.

- **`domain/tools/RegrasEspeciaisSchema.kt`** — Schemas textuais das regras especiais de vantagens/desvantagens que têm custo calculado por metadados (Aliado, Inimigo, Dependente, Garras, Resistente, Ataque Inato, etc.). Injetado pelo `buscarCatalogo` quando o traço tem `specialRule`, para que o modelo saiba exatamente quais metadados preencher.

---

## 13. Data — Repositórios

- **`data/DataRepository.kt`** — Repositório central de catálogos. Carrega (lazy, com Mutex) vantagens, desvantagens, perícias, magias, técnicas, armas, armaduras, escudos, raças, metacaracterísticas. Expõe métodos de filtragem (`filtrarVantagens`, `filtrarDesvantagens`, `filtrarPericias`, `filtrarMagias`, `filtrarArmasCatalogo`, etc.), criação de objetos selecionados (`criarVantagemSelecionada`, `criarPericiaSelecionada`) e validação de pré-requisitos.

- **`data/MestreIARepository.kt`** — Repositório do Códex + **motor de busca VIVO do Auditor (Lotes 325-327).** Sincroniza `chunks.jsonl` → `manual_chunks` (FTS4) com Mutex (`CODEX_VERSION_CURRENT = 3`). Funções do Auditor: **`localizarNoCodex`** (FTS4 AND, fallback OR, + **`rankearPorBM25`** que ordena por relevância — Lote 327), **`lerPaginas`** (texto completo de página/intervalo), `buscarPorPaginaESource`. Mantém também `buscarNoCodexDireto`/`buscarPorPagina` (usados pelo GraphEngine legado).

- **`data/MestreIAQueryEngine.kt`** — Preparação de queries FTS4 (`prepararQueryFTSAgressiva`, OR + sinônimos). ⚠️ Usado pelo GraphEngine (Forjador/legado), **NÃO pelo `localizarNoCodex` do Auditor**, que tokeniza por conta própria. Um dos 3 dicionários de sinônimos do projeto.

---

## 14. Data — Network

- **`data/network/MestreIAClient.kt`** — Cliente HTTP para APIs de IA. Suporta Gemini nativo (`generativelanguage.googleapis.com`) e OpenRouter/OpenAI-compatible. Monta JSON do request (`gerarJsonGoogleNative`, `gerarJsonOpenRouter`), lida com tool calls na resposta, captura tokens de uso. Modo stream desabilitado (JSON puro). Log de auditoria do prompt (tamanho, modelo, tokens).

- **`data/network/MestreIAPromptsAuditor.kt`** — Prompt de sistema do AUDITOR (reescrito Lotes 325/328): loop `localizar`→`ler`, "REGRA DE OURO" (não responder sem ter lido), anti-confabulação (citar só o que leu). **CATEGORIAL, sem exemplos hardcoded** (lição do Lote 318 — exemplo vira cola/viés).

- **`data/network/MestreIAPromptsForjador.kt`** — Prompt de sistema do FORJADOR (modo `geracao`/`analise`). Define o comportamento da IA como criador de fichas, protocolo de uso das tools (`forjador_*`), ordem de operações e formato de JSON final.

- **`data/network/MestreIAResponse.kt`** — Data classes da resposta estruturada da IA: `MestreIAResponse` (envelope completo da ficha gerada), `AtributosIA`, `VantagemIA`, `DesvantagemIA`, `PericiaIA`, `MagiaIA`, `TecnicaIA`. Usado no fluxo Forjador e no `TOOL_FILL_SHEET` (Auditor).

- **`data/network/MestreIATools.kt`** — Schemas das ferramentas. **AUDITOR atual (Lote 325): `getAuditorToolsOpenAI`/`getAuditorToolsGemini`** = `localizar_no_codex` + `ler_pagina` + `inspecionar_personagem` + `consultar_nexus_arcano`. ⚠️ `getOpenAITools`/`getGeminiTools` (5 tools de embedding: `consultar_manual_direto` + 4 especializadas por livro + `TOOL_FILL_SHEET`) agora só servem ao FORJADOR — legadas p/ o Auditor. A seleção por modo acontece em `MestreIAClient`.

- **`data/network/DiscordRollApiClient.kt`** — Cliente HTTP para o servidor Discord do projeto. Envia `DiscordRollPayload` (personagem, tipo de teste, dados, resultado) via POST. Também busca lista de `DiscordVoiceChannel` disponíveis. Data classes: `DiscordRollPayload`, `DiscordRollSendResult`, `DiscordVoiceChannel`.

---

## 15. Data — Storage (Room / Persistência)

- **`data/storage/FichaDatabase.kt`** — Configuração Room v22. Entidades: `FichaEntity`, `ManualChunkEntity`, `GraphNodeEntity` (legado), `ChatSessionEntity`, `ChatMessageEntity`. `fallbackToDestructiveMigration`. Método `prePopulateManual` (importa `chunks.jsonl` para FTS4). `graphNodeDao` declarado mas GraphNode está descontinuado.

- **`data/storage/FichaDao.kt`** — DAO Room para fichas: `upsert`, `getJson`, `deleteByName`, `listNames` (ordenado por `updatedAt` DESC).

- **`data/storage/FichaEntity.kt`** — Entidade Room `fichas`: `nomeArquivo` (PK), `json` (texto completo), `updatedAt` (timestamp).

- **`data/storage/FichaStorageRepository.kt`** — Repositório de persistência de fichas. Migra fichas antigas de `SharedPreferences` → Room (operação única). `salvarFicha`, `carregarFicha`, `excluirFicha`, `listarFichas`. Normaliza nomes de arquivo para compatibilidade cross-versão.

- **`data/storage/ManualChunkDao.kt`** — DAO FTS4 para o Códex. `buscarRegras` (query FTS4 full-text), `buscarPorPagina`, `buscarPorPaginaESource`, `getChunkById`, `getCount`, `clearAll`. Tabela virtual FTS4 com `search_text` (texto + source_title).

- **`data/storage/ManualChunkEntity.kt`** — Entidade FTS4 `manual_chunks`: `chunk_id`, `text`, `source_title`, `source_id`, `page_number`, `search_text` (campo de busca composto).

- **`data/storage/MetacaracteristicaStore.kt`** — Persistência leve de metacaracterísticas criadas pelo usuário (arquivo `metacaracteristicas_usuario.json` em `filesDir`). Lista, salva (por nome, case-insensitive) e exclui. Usa JSON direto em vez de Room (sem migration necessária).

- **`data/storage/ChatHistoryDao.kt`** — DAO Room para histórico de chat: sessões (`getAllSessions`, `createSession`, `updateSessionTitle`, `updateSessionTimestamp`) e mensagens (`insertMessage`, `getMessagesForSession`).

- **`data/storage/ChatHistoryEntity.kt`** — Entidades Room: `ChatSessionEntity` (`chat_sessions`: id, title, createdAt, updatedAt) e `ChatMessageEntity` (`chat_messages`: id, sessionId, role, text, modelName, createdAt).

- **`data/storage/GraphNodeDao.kt`** — ⚠️ LEGADO — NÃO UTILIZADO. DAO Room para o grafo de conhecimento (descontinuado). Declarado no `FichaDatabase` mas nunca chamado pelo código ativo.

- **`data/storage/GraphNodeEntity.kt`** — ⚠️ LEGADO — NÃO UTILIZADO. Entidade Room `graph_knowledge` do grafo descontinuado. Mantida só para não quebrar a migration do Room.

---

## 16. Model

*Data classes puras. Sem lógica de negócio (exceto `Personagem.kt` que tem cálculos derivados).*

- **`model/Personagem.kt`** — Modelo raiz. Todos os campos do personagem GURPS 4ª Ed. (atributos primários/secundários, vantagens, desvantagens, qualidades, peculiaridades, perícias, técnicas, magias, equipamentos, modelo racial, HP/FP de rolagem, notas). Tem propriedades calculadas (`pontosVida`, `pontosFadiga`, `velocidadeBasica`, etc.) que usam `CharacterRules` e `TraitRuleRegistry`. `toJson`/`fromJson` para serialização.

- **`model/PersonagemInterop.kt`** — Importação/exportação versionada. `importarJson` suporta envelope `{"schema":"gurps-ficha","character":{...}}` e fallback para JSON legado sem envelope. `exportarJson` gera o envelope com metadados (schemaVersion, exportedAtUtc, appVersion, uiVariant).

- **`model/CatalogosSuplementares.kt`** — Data classes dos catálogos suplementares: `PericiaSuplementarItem`, `TecnicaCatalogoItem`, `PericiaV2RuleMapItem` (e subclasses de regra: `PericiaV2TipoRegra`, `PericiaV2PreRequisitoRegra`, `PericiaV2PreDefinidoRegra`).

- **`model/ArmaCatalogoItem.kt`** — Data class de arma do catálogo: nome, dano, alcance, ST mínimo, peso, custo, habilidade base, aparar, grupo, etc.

- **`model/ArmaduraCatalogoItem.kt`** — Data class de armadura: nome, RD, peso, custo, locais cobertos, componentes (lista de peças individuais), tags.

- **`model/EscudoCatalogoItem.kt`** — Data class de escudo: nome, BD, peso, custo, habilidade de bloqueio.

- **`model/MestreIAChunk.kt`** — Data class de chunk do Códex: `chunk_id`, `text`, `source_title`, `source_id`, `page_number`. Usado pelo RAG.

---

## 17. PreRequisitos

- **`regras_prerequisitos/PreRequisitoChecker.kt`** — Motor de verificação de pré-requisitos. `checkParseResult` avalia cada `PreRequisitoType` contra o personagem (atributo mínimo, vantagem necessária, perícia necessária, NH mínimo). Retorna lista de condições com status (atendido/faltando).

- **`regras_prerequisitos/PreRequisitoParser.kt`** — Parser de texto bruto de pré-requisito ("IQ 12+", "Magia X em NH 14+") → lista de `PreRequisitoType`. Suporta pré-requisitos compostos (AND/OR implícito).

- **`regras_prerequisitos/PreRequisitoType.kt`** — Sealed class / data classes dos tipos de pré-requisito: `AtributoMinimo`, `VantagemNecessaria`, `PericiaMinima`, `MagiaMinima`, `Bypass` (ignorar validação).

---

## 18. UI — Telas Principais

- **`ui/FichaScreen.kt`** — Container principal. Scaffold com `FichaCustomNavigationBar`, troca de abas (Geral, Combate, Perícias, Magias, Traços, Equipamentos, Rolagem, Técnicas, Notas, VTT), e roteamento de dialogs globais (importação, erro de carga, atualização).

- **`ui/TabGeral.kt`** — Aba de informações básicas: nome, jogador, campanha, pontos iniciais/gastos/restantes, atributos primários (ST/DX/IQ/HT) com custo, atributos secundários (PV, PF, Vontade, Percepção, Velocidade, Deslocamento), modelo racial ativo.

- **`ui/TabCombate.kt`** — Aba de combate: defesas ativas (Esquiva, Apara, Bloqueio) com bônus manual editável, lista de armas equipadas com dano calculado, armaduras por local corporal com RD total.

- **`ui/TabPericias.kt`** — Aba de perícias: busca por texto/atributo/dificuldade, lista com NH calculado e pontos gastos, adição do catálogo, edição inline de pontos.

- **`ui/TabMagias.kt`** — Aba de magias: busca por escola e classe, lista com custo de energia reduzido por NH, modo alvo (Nexus Arcano) com trilha mínima e chaves de progressão.

- **`ui/TabTracos.kt`** — Aba de vantagens e desvantagens: busca, listagem por custo, adição do catálogo com seleção de modificadores. Ponto de entrada para `VantagemDialogs` e `DesvantagemDialogs`.

- **`ui/TabTecnicas.kt`** — Aba de técnicas: listagem com NH calculado relativo à perícia base, busca por nome/fonte.

- **`ui/TabEquipamentos.kt`** — Aba de equipamentos: lista de itens com peso individual e total, adição de arma/armadura/item genérico do catálogo.

- **`ui/TabRolagem.kt`** — Hub de rolagem. Lista atributos, perícias, magias e traços com ataque inato para rolagem de 3d6. Exibe resultado, margem de sucesso/falha, críticos. Dispatch para Discord se configurado.

- **`ui/TabNotas.kt`** — Aba de notas e texto livre: histórico, aparência, notas gerais do personagem.

- **`ui/TabVtt.kt`** — Aba de integração VTT: configuração de sessão (URL do servidor, Room Key, Player ID, Token ID), status de conexão, botão de auto-detect na LAN.

---

## 19. UI — Dialogs

- **`ui/DialogsMestreIA.kt`** — Interface de chat completa do Mestre IA: balões de mensagem (usuário/assistente/sistema), botão de copiar por bolha, seletor de sessão histórica (`HistorySelectorDialog`), seletor de modo (conversa/geração/análise), painel de configuração de API (URL, chave, modelo), botão de sincronização forçada do Códex.

- **`ui/DialogsCommon.kt`** — Dialogs comuns reutilizados em múltiplas abas: confirmação de exclusão, diálogo de texto simples, seletor de opções.

- **`ui/DialogsMagias.kt`** — Dialogs específicos de magias: adição com seleção de escola e pontos, edição de pontos de magia existente.

- **`ui/DialogsPericias.kt`** — Dialogs de perícias: adição com especialização, atributo e dificuldade escolhidos, edição de pontos.

- **`ui/DialogsTecnicas.kt`** — Dialogs de técnicas: adição do catálogo com visualização de pré-requisito e limite de NH.

- **`ui/DialogsTracos.kt`** — Dialogs de traços (legado/entrada): seleção de vantagem/desvantagem, visualização de custo e modificadores disponíveis.

- **`ui/DiceRoller.kt`** — Componente de rolagem 3d6: resultado visual, cálculo de margem, identificação de crítico (acerto em ≤4, falha em ≥17, acerto/falha em 3/18).

---

## 20. UI — Features (Subcomponentes Especializados)

- **`ui/features/traits/TraitRule.kt`** — (ver seção 6 — é domain, não UI)
- **`ui/features/traits/TraitCommonComponents.kt`** — Componentes genéricos de traços: `EscopoModificadoresDialog` (seletor de modificadores com busca), chip de custo, card de traço com ações.

- **`ui/features/traits/TraitDialogs.kt`** — Diálogos de adição/edição de vantagens e desvantagens simples (sem regra especial).

- **`ui/features/traits/TraitDialogsV2.kt`** — Versão expandida dos diálogos de traços com suporte a metadados estruturados (para traços com `specialRule`).

- **`ui/features/traits/TraitSpecialRuleComponents.kt`** — Hub de componentes de regras especiais: UI de Aliado, Patrono, Dependência, Inimigo, Mestre de Armas (com seleção de grupo de arma). Cada regra especial tem seu próprio composable.

- **`ui/features/traits/VantagemDialogs.kt`** — Dialog unificado de adição de vantagem: detecta `specialRule` e renderiza o componente correto de `TraitSpecialRuleComponents`.

- **`ui/features/traits/DesvantagemDialogs.kt`** — Dialog unificado de adição de desvantagem: mesma arquitetura de `VantagemDialogs`.

- **`ui/features/magic/MagicDialogs.kt`** — Dialogs de configuração de magia: seleção de escola para `imunidade_a_encantamento`, configuração de encantamento alvo, seleção de AM (Aptidão Mágica) ativa.

- **`ui/features/magic/SelectingMagicDialog.kt`** — Dialog de busca e seleção de magia do catálogo com pré-visualização de pré-requisitos e custo de energia.

- **`ui/features/rolagem/RolagemModels.kt`** — Data classes da aba de rolagem: `RollMappedOption` (opção de rolagem mapeada de perícia/traço), `DamageSourceOption` (fonte de dano), `StDamageMode` (modo de dano por ST).

- **`ui/features/rolagem/RolagemComponents.kt`** — Componentes visuais da rolagem: card de opção de rolagem, resultado visual com cor (verde=sucesso, vermelho=falha, dourado=crítico).

- **`ui/features/rolagem/RolagemPrimaryDialogs.kt`** — Dialogs primários de rolagem: seleção de modificador antes de rolar, confirmação de envio para Discord.

- **`ui/features/rolagem/RolagemSecondaryDialogs.kt`** — Dialogs secundários: configuração de canal Discord, histórico de rolagens da sessão.

- **`ui/features/virtualtabletop/MesaVirtualScreen.kt`** — Tela da Mesa Virtual (placeholder). Exibe estado de conexão e botões de ação VTT. Ainda em desenvolvimento.

- **`ui/features/virtualtabletop/MesaVirtualViewModel.kt`** — ViewModel da Mesa Virtual. `MesaVirtualState` com discordId, token, campaignId, isConnected, activePlayers. `conectar()` apenas atualiza o estado local (integração Railway planejada).

---

## 21. UI — Componentes Utilitários

- **`ui/components/FichaCustomNavigationBar.kt`** — Barra de navegação inferior customizada com ícones e labels das abas. Suporta `onLongPress` no ícone do Mestre IA para ativar reconhecimento de voz (`EstadoVoz`), com anel verde/amarelo pulsante como feedback visual durante escuta/processamento.

- **`ui/components/VozMestreIA.kt`** — Encapsula o `SpeechRecognizer` do Android para reconhecimento de voz em PT-BR. Estados: `OCIOSO`, `ESCUTANDO`, `PROCESSANDO`, `ERRO`. Callbacks `onEstado` e `onResultado`. Instanciado em `FichaScreen` e conectado ao Forjador via `conversarComMestreIA`.

- **`ui/DialogStandards.kt`** — Padrões visuais de dialogs: dimensões, espaçamentos, cores de botões primário/secundário/destrutivo.

- **`ui/HorizontalDivider.kt`** — Divisor horizontal estilizado usado em listas e seções.

- **`ui/SectionCard.kt`** — Card de seção com título e conteúdo, usado em TabGeral e TabCombate.

- **`ui/UiStandards.kt`** — Constantes de design do app: padding padrão, tamanhos de fonte, breakpoints.

- **`ui/UiA11y.kt`** — Helpers de acessibilidade: `semantics` para TalkBack, labels descritivos. Usado pela variante Pracego.

- **`ui/UiActionLabels.kt`** — Strings de labels de ação para acessibilidade (variante Pracego): "adicionar vantagem", "remover perícia", etc.

---

## 22. UI — Tema

- **`ui/theme/Color.kt`** — Paleta de cores do app (Material You). Cores diferenciadas por variante Visual/Pracego.

- **`ui/theme/Theme.kt`** — `GURPSFichaTheme`: configura `MaterialTheme` com `ColorScheme` e `Typography`. Detecta `BuildConfig.UI_VARIANT` para aplicar paleta correta.

- **`ui/theme/Type.kt`** — Tipografia do app: `TextStyle` para títulos, corpo e legendas.

---

## 23. VTT — Mesa Virtual

- **`vtt/VttBridgeCodec.kt`** — Codec de serialização para a ponte VTT. Converte JSON em JavaScript string literal com escape correto de caracteres especiais. Usado para injetar dados da ficha em WebView do Foundry.

- **`vtt/VttHostAutoDetect.kt`** — Auto-detecção de servidor VTT na LAN. Primeiro tenta ARP table (`/proc/net/arp`), depois scan ativo por subnet. Faz probe HTTP paralelo (com coroutines) nos candidatos para detectar qual tem a API do servidor GURPS.

- **`vtt/VttSessionService.kt`** — Serviço de sessão VTT. `joinSession` (entra numa sala com roomKey e playerId), retorna `VttJoinSessionResult` (sessionId, tokenId, needsBind). Chamadas HTTP para o servidor Railway/local.

- **`vtt/VttSessionStorage.kt`** — Persistência local da sessão VTT em `SharedPreferences`: serverUrl, webUrl, roomKey, playerId, sessionId, tokenId, tokenImageUri, autoReconnect. `VttSessionSnapshot` data class.

- **`vtt/VttTokenBindService.kt`** — Vincula o token do personagem (imagem e ID) ao player na sessão VTT. Retorna `VttTokenBindResult`. Separado do `VttSessionService` para responsabilidade única.

- **`vtt/VttRollService.kt`** — Envia rolagens para o servidor VTT via HTTP: `VttRollRequest` (roomKey, playerId, tokenId, tipoAcao, nomeAcao, modificador, alvoTokenId) → `VttRollResult`.

---

## 24. Update

- **`update/AppUpdateService.kt`** — Verifica nova versão no GitHub (endpoint configurado em `BuildConfig`). Compara `versionCode` atual vs. mais recente. Retorna `AppUpdateState` com URLs de APK para variante Visual e Pracego. Data classes: `AppUpdateMetadata`, `AppUpdateState`.

- **`update/AppUpdateHelper.kt`** — Executa o download e instalação da nova APK via `DownloadManager`. Registra `BroadcastReceiver` para detectar conclusão do download e dispara o intent de instalação via `FileProvider`.

---

## 25. Assets — Catálogos JSON Ativos

*Arquivos em `app/src/main/assets/`.*

| Arquivo | Conteúdo |
|---|---|
| `vantagens.v3.json` | Vantagens oficiais GURPS 4ª Ed. (formato v3 com modificadores estruturados) |
| `vantagens_artes_marciais.v1.json` | Vantagens exclusivas do suplemento Artes Marciais |
| `desvantagens.v2.json` | Desvantagens oficiais (formato v2 com specialRule) |
| `pericias.json` | Perícias do Módulo Básico |
| `pericias_suplementares_*.json` | Perícias de suplementos (Artes Marciais, GunFu, etc.) |
| `magias.json` | Magias do Módulo Básico com pré-requisitos raw |
| `tecnicas_modulo_basico.json` | Técnicas do Módulo Básico |
| `tecnicas_artes_marciais.json` | Técnicas do suplemento Artes Marciais |
| `tecnicas_gunfu.json` | Técnicas do suplemento GunFu |
| `armas_cac.json` | Armas de combate corpo a corpo |
| `armas_distancia.json` | Armas de ataque à distância |
| `armas_fogo.json` | Armas de fogo (módulo básico) |
| `armas_fogo_gunfu.json` | Armas de fogo do suplemento GunFu |
| `armaduras.json` | Armaduras com componentes por local corporal |
| `escudos.json` | Escudos com BD |
| `racas.v1.json` | Raças jogáveis (formato enxuto — sem custos, recalculado) |
| `metacaracteristicas.v1.json` | Pacotes prontos de metacaracterísticas (Gigante, Anão, etc.) |
| `chunks.jsonl` | Chunks do manual GURPS (1 por página, FTS4). Auditor usa só o texto; embedding (48MB) dormente p/ ele. |
| `chunks.jsonl.bak` | Idêntico SEM embeddings (6.5MB). Candidato a substituir o .jsonl quando confirmado que Auditor não precisa de embedding. |
| `temas_ia.json` | Temas canônicos de busca para o Mestre IA |
| `topic_index.json` | ⚠️ Páginas garantidas — lido só pelo `MestreIATopicIndex`, que está MORTO (Lote 272). Asset órfão na prática. |

---

## 26. Scripts de Manutenção (pasta `scripts/`)

- **`audit_active_jsons_v2.py`** — Verifica integridade dos catálogos JSON (IDs únicos, campos obrigatórios, referências cruzadas).
- **`generate_pericias_v2_rules_map.py`** — Gera o mapa de regras de perícias v2 a partir do texto bruto.
- **`fix_mojibake_project.py`** — Corrige encoding corrompido (mojibake) em todo o projeto.
- **`cleanup_assets_text.py`** — Normaliza textos e limpa artefatos de OCR de PDFs.
- **Série `convert_*.py`** — Converte dados brutos (planilhas, PDFs) para o formato JSON dos assets.

---

## 27. Motor Nexus Arcano (módulo separado `motor modo alvo/src/`)

- **`NexusArcanoEngine.kt`** — Orquestrador: avalia chaves, computa trilha ótima (A*/guloso via `planejarCaminhoMinimo`), retorna `ArcanoResultado`.
- **`ArcanoModels.kt`** — Modelos: `ArcanoChave`, `ArcanoMetaTipo`, `ArcanoMetaProgress`, `ArcanoEstadoPersonagem`, `ArcanoResultado`.
- **`ArcanoCatalogo.kt`** — Interface que o adapter implementa para fornecer pré-requisitos e escolas ao engine.
- **`NexusArcanoHeuristics.kt`** — Avalia quantas magias de cada escola o personagem possui para detectar chaves desbloqueadas.
- **`NexusArcanoParser.kt`** — Interpreta texto bruto de pré-requisito de magia → lista de dependências tipadas.
- **`NexusArcanoPathfinder.kt`** — DFS/guloso para encontrar caminho mínimo até a magia alvo.
- **`NexusArcanoStrings.kt`** — Formatação de mensagens para a UI (avisos, trilha de aprendizado, bloqueios).

---

## 28. Testes Automatizados (`app/src/test/`)

- **`rules/RulesLayerTest.kt`** — Testes de `CharacterRules` e `CombatRules` (atributos, PV, defesas).
- **`PersonagemRulesTest.kt`** — Validação de criação de personagem e limites de pontos.
- **`domain/magias/NexusArcanoLoteFCanonicScenarioTest.kt`** — Cenários ouro do Nexus Arcano (progressão incremental de metas).
- **`domain/magias/NexusArcano*Test.kt`** — Suíte massiva de testes do motor de magias.
- **`vtt/VttBridgeCodecStressTest.kt`** — Teste de robustez do codec VTT.

---

## 29. Endereços Rápidos (Funções Críticas)

| O que buscar | Onde está |
|---|---|
| Esquiva / Apara / Bloqueio (cálculo) | `CombatRules.kt` → `calcularEsquiva/Apara/Bloqueio` |
| Bônus de Mestre de Armas | `MestreDeArmasRule.kt` → `getDamageBonusPerDie` |
| Golpe/Empurrão por ST | `CharacterRules.kt` → `tabelaGdP / tabelaGeB` |
| Custo de vantagem com specialRule | `CharacterRules.kt` → `calcularCustoAliado/Inimigo/...` |
| Cálculo de NH de perícia | `SkillEngine.kt` → `getRegraPerfilTecnica` |
| Loop de tool-use do Auditor | `MestreIAUseCase.kt` → `conversarComMestreIA` (localizar→ler, máx 8) |
| Busca do Auditor (localizar/ler) | `MestreIARepository.kt` → `localizarNoCodex` / `lerPaginas` |
| Ranking do Auditor (ajustar AQUI) | `MestreIARepository.kt` → `rankearPorBM25` (Lote 327) |
| Trava anti-confabulação | `MestreIAUseCase.kt` → var `leuAlgumaPagina` (Lote 328) |
| Loop de tool-use do Forjador | `MestreIAGeneratorUseCase.kt` → `gerarPersonagem` |
| ⚠️ Scoring RAG semântico (LEGADO p/ Auditor) | `MestreIAGraphEngine.kt` → `buscarDiretoNoCodex` |
| Query FTS (GraphEngine/Forjador) | `MestreIAQueryEngine.kt` → `prepararQueryFTSAgressiva` |
| Carregamento de raças | `RacaCatalogo.kt` → `resolver` |
| Envio para Discord | `DiscordRollApiClient.kt` → `postRoll` |
| Salvar / carregar ficha | `FichaStorageRepository.kt` → `salvarFicha / carregarFicha` |
| Importar JSON versionado | `PersonagemInterop.kt` → `importarJson` |
| Normalização de busca | `CatalogFilters.kt` → `normalizarBusca` |
| Auto-detect VTT na LAN | `VttHostAutoDetect.kt` → `detectLanHost` |

---

## 30. Variantes de Build

| Variante | Foco |
|---|---|
| `Visual` | Estética visual, cores vibrantes, layouts densos |
| `Pracego` | Acessibilidade total (TalkBack), labels extras, diálogos simplificados |

Chave de controle: `BuildConfig.UI_VARIANT` (usado para condicionar lógica de UI entre as variantes).

---

> [!TIP]
> **DICA PARA O AGENTE**: Ao modificar regras de combate ou magias, rode `NexusArcanoLoteFCanonicScenarioTest.kt` e `RulesLayerTest.kt`.
>
> **Sobre o Mestre IA (pós-Lote 328):** o AUDITOR não usa mais RAG semântico — usa
> `localizar_no_codex` + `ler_pagina` (`MestreIARepository`). Para ajustar a busca/ranking do
> Auditor, mexa em `MestreIARepository.rankearPorBM25` — **NÃO** no `MestreIAGraphEngine` (legado
> p/ Auditor). Os 3 dicionários de sinônimos (`MestreIAPlanner`, `MestreIAGraphEngine`,
> `MestreIAQueryEngine`) só importam para o GraphEngine/Forjador/Voz, não para o Auditor.
> Antes de mexer em qualquer arquivo do Mestre IA, leia `ARQUITETURA_MESTRE_IA.md §5` (código
> legado/morto e desde quando) para não reanimar algo descontinuado.
