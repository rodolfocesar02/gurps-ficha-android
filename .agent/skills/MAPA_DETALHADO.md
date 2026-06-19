# Mapa Detalhado: Arquivos e Funções do Projeto GURPS

Mapa de engenharia completo do projeto. Use para localizar lógicas específicas sem varrer o código.
Atualizado em: 2026-06-08 (revisão de fidelidade linha-a-linha contra o código real: corrigidos nomes de
assets, tipos de pré-requisito, tools do Forjador, Voz GeminiLive, Room v24, tema, abas e testes;
adicionados arquivos novos — IdiomaRule, VecChunk*, ObjectBoxStore, GeminiLive*, AppUiEntry).
Base anterior: 2026-05-30 (Mestre IA pós-Lote 328) | 130+ arquivos documentados.

> ➕ **2026-06-08 — Feature Imagem/Retrato do Personagem:** novo `ImagemPersonagemStore.kt`
> (seção 15) + funções novas em `Personagem`, `FichaAttributeDelegate`, `FichaViewModel`,
> `FichaNetworkDelegate`, `DiscordRollApiClient`, `FichaScreen` e na API Node (`discord-roll-api`).
> Foto no cabeçalho (recorte por rosto/assunto via ML Kit) + tela cheia + envio ao Discord.
> Cada item está marcado com **[+ 2026-06-08]** nas seções abaixo.

> ➕ **2026-06-09 (Lotes 341-348):** arquivos novos e mudanças (marcados com **[+ 2026-06-09]**):
> - **Rolagem:** `domain/roll/CriticoRules.kt` (NOVO, §10) + `assets/tabelas_criticas.json` (§25) —
>   automação de Golpe Fulminante / Erro Crítico ao dar Decisivo/Crítico em testes de COMBATE.
> - **Mestre Pintor:** `data/network/GeminiImageService.kt` (NOVO, §14) — gera retrato via Gemini Image API.
> - **Templates do Forjador:** `domain/loaders/ForjadorTemplateCatalogo.kt` (NOVO, §7) +
>   `assets/forjador_templates.json` (§25) — 60 templates de personagem prontos (combatentes, furtivos,
>   sociais, 10 escolas de mago, gêneros modernos).
> - **Import/Export:** imagem do personagem viaja embutida (base64) na ficha; intent-filters do
>   `AndroidManifest` ampliados (abrir ficha do WhatsApp por extensão/octet-stream).
> - **Forjador (fluxo):** entrega JSON direto (não usa mais loop de tools p/ CRIAR ficha); correções de
>   pré-requisito de técnica, GPS de técnicas, raça "null", budget de template. Ver `ARQUITETURA_MESTRE_IA.md §9`.

> ➕ **2026-06-14 (Lotes 349-370, branch `GURPS-Saga`, HEAD `41996c4`):** modo **SAGA / NARRADOR** (3º modo de IA)
> + **motor de combate** (`domain/combat/`, Kotlin puro) + UI de combate. Arquivos novos na **§32** (nova).
> `FichaDatabase` subiu p/ **v26** (migrações 24→25→26 explícitas, tabelas da Saga). Detalhe do fluxo de IA: `ARQUITETURA_MESTRE_IA.md §10`.

> ⚠️ **AUDITOR mudou de motor (Lotes 325-328):** saiu da busca semântica (RAG/HNSW) para
> "grep + leitura dirigida" (`localizar_no_codex` + `ler_pagina`). Vários arquivos abaixo
> viraram LEGADO/MORTO — marcados com ⚠️. Detalhe e motivo de cada um em
> `ARQUITETURA_MESTRE_IA.md §5`.

---

## 1. Ponto de Entrada

- **`MainActivity.kt`** — Activity principal. Inicializa o Compose, intercepta Intents de compartilhamento de ficha, passa o Intent para o ViewModel processar. Única Activity do app. **[+ 2026-06-09]** `tratarIntentRecebido` trata `ACTION_VIEW`/`ACTION_SEND` (importar ficha do WhatsApp/explorador); limpa BOM/espaços antes do parse. Os `intent-filter` do `AndroidManifest` foram ampliados (octet-stream + filtro por EXTENSÃO `.json`/`.gurps`) para o app aparecer ao abrir a ficha no WhatsApp.

---

## 2. ViewModel e Estado Central

- **`viewmodel/FichaViewModel.kt`** — O controlador central do app. Instancia todos os delegates, mantém o `Personagem` ativo como `mutableStateOf`, coordena auto-save ao editar traços, e expõe métodos públicos que a UI chama. Delega todas as operações especializadas para os delegates. **[+ 2026-06-08]** `atualizarImagemPersonagem(uri, originalUri)`; `salvarFicha` agora também sobe o retrato ao Discord (best-effort) via `ImagemPersonagemStore.bytesBase64` + `networkDelegate.enviarRetratoDiscord`. **[+ 2026-06-09]** `exportarFichaJson*ComImagem` (suspend — embute a imagem base64 na exportação) e `restaurarImagemEmbutidaSeHouver` (no import: salva+recorta a imagem embutida e limpa o base64).

- **`viewmodel/FichaUIState.kt`** — Data classes dos estados de busca da UI: `TraitSearchState`, `SkillSearchState`, `MagicSearchState`, `TechniqueSearchState`, `EquipmentSearchState`. Sem lógica — só estruturas de dados para os filtros de catálogo.

---

## 3. Delegates do ViewModel

*Cada delegate é responsável por uma fatia da lógica do `FichaViewModel`. Sem delegates, o ViewModel seria um arquivo de 4.000+ linhas.*

- **`delegates/FichaAttributeDelegate.kt`** — Atualiza atributos primários (ST/DX/IQ/HT), atributos secundários (mod PV, PF, Vontade, Percepção, Velocidade, Deslocamento), e dados básicos do personagem (nome, jogador, campanha, histórico, aparência, notas). Aplica limites via `coerceIn`. **[+ 2026-06-08]** `atualizarImagemPersonagem(personagem, uri, originalUri)` grava os dois caminhos de imagem.

- **`delegates/FichaCombatDelegate.kt`** — Calcula e atualiza defesas ativas (Esquiva, Apara, Bloqueio). Inclui bônus manuais, seleção de perícia de Apara e de Escudo para Bloqueio. Retorna a lista de `ActiveDefense` para a UI exibir.

- **`delegates/FichaEquipmentDelegate.kt`** — Adiciona equipamentos gerais, armas do catálogo e armaduras. Gera as notas automáticas de armas (valor de Aparar, classe de arma de fogo). Filtra tags de armaduras. Contém lógica de classificação de armas de fogo por categoria (pistola, rifle, ultratech, pesada).

- **`delegates/FichaIADelegate.kt`** — Gerente completo da IA. Instancia `MestreIAUseCase` e `MestreIAGeneratorUseCase`. Controla o histórico de chat (`mestreIAChatHistory`), sessões persistidas (Room), modo da IA (`conversa`/`geracao`/`analise`), auto-sincronização do Códex, e o sistema de "bolhas batch" (agrupa eventos consecutivos do Forjador na mesma bolha de chat).

- **`delegates/FichaMagicDelegate.kt`** — Adiciona e remove magias. Valida pré-requisitos via `MagicEngine`, detecta duplicatas por escola (magias de múltiplas instâncias). Retorna escolas e classes únicas do catálogo para os filtros.

- **`delegates/FichaNetworkDelegate.kt`** — Envia rolagens para o Discord via `DiscordRollApiClient`, com retry em timeout. Busca lista de canais de voz do Discord. Sem estado próprio — só operações de rede. **[+ 2026-06-08]** `enviarRetratoDiscord(characterName, imageDataUri)` sobe o retrato uma vez (chamado no salvar da ficha).

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

- **`domain/rules/SentidoRules.kt`** — **[+ 2026-06-14, Lote 372]** Testes de Sentidos (MB p.358): `enum Sentido` (Percepção/Visão/Audição/Olfato-Paladar/Tato); `avaliar(p, sentido)` rola vs Percepção somando o "Sentido Aguçado" e descontando limitações, com COMPONENTES NOMEADOS (a "notinha"). Mapeia ids do catálogo (visao_agucada, audicao_agucada, paladar_olfato_apurado, *_discriminatorio, visao_hiperespectral, tato_apurado; redutores duro_de_ouvido/disopia; bloqueios cegueira/surdez/disosmia). Cobre traços pessoais E raciais. Puro/testável (`SentidoRulesTest`). Consumido por `DialogoSentidos` (§20).

---

## 6. Domain — Trait Rules

*Regras especiais por ID de vantagem. Cada `TraitRule` implementa a interface e é registrada no Registry.*

- **`domain/rules/traits/TraitRule.kt`** — Interface base: `calculateCost`, `getAttackOptions`, `getDefenseOptions`, `getDamageOptions`, `getDodgeModifier`, `getBlockModifier`, `getParryModifier`, `getSkillModifiers`, `getDamageBonusPerDie`. Todas com default `null`/`emptyList`/`0`.

- **`domain/rules/traits/TraitRuleRegistry.kt`** — Singleton que registra todas as regras e expõe métodos agregadores: `getSkillBonus`, `getParryBonus`, `getDodgeBonus`, `getBlockBonus`, `getDamageBonusPerDie`. Usado pelo `Personagem` e pelo `FichaCombatDelegate`.

- **`domain/rules/traits/AtaqueInatoRule.kt`** — Custo e opções de ataque para `ataque_inato` (vantagem composta, calcula por dados/modificadores armazenados nos metadados).

- **`domain/rules/traits/GolpeadoresRule.kt`** — Custo e dano de `golpeadores` (Striker). Lê tipo de golpeador e metadados para calcular dano.

- **`domain/rules/traits/DentesRule.kt`** — Custo e dano de `dentes` (Bite). Calcula dano por tipo de mordida.

- **`domain/rules/traits/GarrasRule.kt`** — Custo de `garras` pelo metadado `tipoGarras` (cascos=3, cegas=3, afiadas=5, pontudas=8, longas_pontudas=11; default afiadas=5). Expõe as opções de dano (`getDamageOptions`) por tipo de garra (corte/perfuração, com bônus de +1/dado em cascos, cegas e longas).

- **`domain/rules/traits/FlexibilidadeRule.kt`** — Bônus de perícia para `flexibilidade` (Contorcionismo, Acrobacia).

- **`domain/rules/traits/ApararAmpliadoRule.kt`** — Bônus de Apara para `aparar_ampliado`.

- **`domain/rules/traits/BloqueioAmpliadoRule.kt`** — Bônus de Bloqueio para `bloqueio_ampliado`.

- **`domain/rules/traits/EsquivaAmpliadaRule.kt`** — Bônus de Esquiva para `esquiva_ampliada`.

- **`domain/rules/traits/MestreDeArmasRule.kt`** — Bônus de dano por dado (`getDamageBonusPerDie`) para `mestre_de_armas`, filtrado por grupo de arma e perícia.

- **`domain/rules/traits/TelecomunicacaoRule.kt`** — Custo de `telecomunicacao` pelo metadado de alcance/tipo.

- **`domain/rules/traits/IdiomaRule.kt`** — Custo de `idioma` (GURPS p.23). Cada instância = um idioma adicional; o custo soma as duas "metades" (fala + escrita) via `metadeCusto` no companion (rudimentar=1, com sotaque=2, materna=3, nenhum=0). Metadados: `nomeIdioma`, `nivelFalado`, `nivelEscrito`.

> **Nota:** as 11 regras acima são registradas em `TraitRuleRegistry.init` (AtaqueInato, Golpeadores, Dentes, Flexibilidade, Garras, ApararAmpliado, BloqueioAmpliado, EsquivaAmpliada, MestreDeArmas, Telecomunicacao, Idioma).

---

## 7. Domain — Loaders

*Carregamento e resolução dos catálogos JSON dos assets.*

- **`domain/loaders/CatalogLoaders.kt`** — Carrega todos os catálogos de assets: `vantagens.v3.json` (+ extras de artes marciais), `desvantagens.v2.json`, `pericias.json` (+ suplementares), `magias.json`, `tecnicas_*.json` (múltiplos arquivos), `armas_*.json`, `armaduras.json`, `escudos.json`. Registra erros de carga sem lançar exceção. Faz mojibake fix nos textos carregados.

- **`domain/loaders/MetacaracteristicaCatalogo.kt`** — Carrega `metacaracteristicas.v1.json` (catálogo de metacaracterísticas prontas como Gigante, Anão, etc.) e resolve cada uma como `ModeloRacial` usando `RacaCatalogo.resolver`. Formato "enxuto": sem custos no JSON, recalculado em runtime.

- **`domain/loaders/RacaCatalogo.kt`** — Carrega `racas.v1.json` (catálogo de raças jogáveis). Resolve `RacaDefinicao` → `ModeloRacial` casando IDs contra os catálogos de vantagens/desvantagens/perícias via `DataRepository`. Custo recalculado pelo `CharacterRules` — imune a custo salvo errado. É também o schema de raças para o Forjador IA.

- **`domain/loaders/ForjadorTemplateCatalogo.kt`** — **[+ 2026-06-09]** Carrega `forjador_templates.json` (60 templates/arquétipos de personagem prontos). `escolher(prompt, templates)` faz match por palavra-chave (id/nome/descrição/tags) e retorna o template mais próximo do pedido — é **o SISTEMA (código) que escolhe, não a IA**. `formatarParaPrompt(t)` serializa o template como bloco de texto injetado no prompt do Forjador (na 1ª iteração) como "ponto de partida". `pontosBase` é só REFERÊNCIA — o budget real é o do pedido do usuário. Data classes: `ForjadorTemplate` (+ Pericia/Vantagem/Desvantagem). ⚠️ Todos os IDs dos templates são validados contra o catálogo (ver `project_forjador_pendencias`).

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

- **`domain/roll/CriticoRules.kt`** — **[+ 2026-06-09]** Regra de crítico COMPLETA do GURPS (com NH) + automação das tabelas. `classificar(soma, nhEfetivo)` → DECISIVO/FALHA_CRITICA/NORMAL (decisivo 3-4 sempre, 5@NH≥15, 6@NH≥16; falha 18 sempre, 17@NH≤15, soma≥NH+10). `ehTesteDeCombate(tipoLabel)` (Ataque/Defesa/Técnica/Magia). `rolarTabela(context, resultado)` rola **3d6** e monta o texto das DUAS tabelas (Decisivo→Golpe Fulminante + na Cabeça; Crítico→Erro Crítico + Desarmado) carregadas de `tabelas_criticas.json`. Disparado por `TabRolagem.executarRolagem` após uma rolagem de combate dar decisivo/crítico (2ª mensagem ao Discord).

---

## 11. Domain — MestreIA (Núcleo da IA)

*Para detalhes técnicos do fluxo de IA (prompts, loop de tool-use, FTS, decisões de arquitetura), ver `ARQUITETURA_MESTRE_IA.md`.*

- **`domain/MestreIAContextFilter.kt`** — Gera a string de contexto da ficha enviada para a IA: nome, atributos, HP/FP atual, vantagens, desvantagens, principais perícias. No modo `conversa` inclui aparência e histórico. Filtra metadados técnicos.

- **`domain/MestreIAGeneratorUseCase.kt`** — Orquestra o fluxo FORJADOR (criação de personagem). Usa `MestreIAClient` com modo `geracao`/`analise`, executa `ForjadorToolExecutor` a cada tool call recebida (ler ficha, buscar catálogo, GPS magia, editar ficha), faz até N iterações do loop de tool-use. Valida resposta final via `MestreIAValidacaoReport`.

- **`domain/MestreIAGraphEngine.kt`** (603 linhas) — ⚠️ **LEGADO p/ Auditor desde Lote 325.** Motor RAG semântico (BM25 + HNSW + diversificação + "Ponte de Ferro"). Hoje alcançado por `gerarCatalogoDireto` (morto) e **ATIVAMENTE pela Voz** (`GeminiLiveTools` instancia o próprio `MestreIAGraphEngine` e chama `buscarDiretoNoCodex`). O scoring BM25 daqui foi **copiado** para `MestreIARepository.rankearPorBM25` (Lote 327) — ajustar ranking do Auditor é LÁ, não aqui. Flag `MODO_HNSW_PURO` setada em `FichaIADelegate.kt:55`.

- **`domain/MestreIAPlanner.kt`** (879 linhas) — ⚠️ **QUASE MORTO desde Lote 319.** A lógica de planejamento (dicionários hardcoded) causava alucinação léxica e foi removida do fluxo. Hoje só a data class `TermoPonderado` é usada como TIPO (parâmetro com default vazio que nunca recebe valor real). Nenhum `PlanoDeBusca` roda no Auditor atual.

- **`domain/MestreIARuleAuditor.kt`** — Auditor fiscal (Lote 55). Compara a `MestreIAResponse` sugerida pela IA contra os cálculos reais do `CharacterRules`. Gera lista de `AuditNote` com campo, valor sugerido vs. correto. Usado pelo Forjador para detectar custo errado de atributos.

- **`domain/MestreIAUseCase.kt`** — Orquestra o fluxo AUDITOR. **Desde Lote 325 NÃO usa RAG semântico:** loop de tool-use com `localizar_no_codex` (FTS4 AND/OR + ranking BM25) e `ler_pagina` (texto completo), via `MestreIARepository` — até **8 iterações** (`MAX_TOOL_CALLS`). **Lote 328:** trava anti-confabulação (`leuAlgumaPagina`) — se nunca leu página, força declarar "não localizei" em vez de citar de memória. `ehErroDeApi()` preciso. ⚠️ Contém funções legadas no mesmo arquivo: `executarBuscaCodex` (cases das 5 tools de embedding nunca disparam — Lote 317→325) e `gerarCatalogoDireto`/`reescreverQueryParaGurps` (MORTAS, zero callers).

- **`domain/MestreIACitationValidator.kt`** — (Lote 315, VIVO) Verificador de Citações. Extrai citações `[Livro, Pág]` da resposta e compara com as páginas dos chunks lidos; o que não bate vira aviso "⚠️ não verificadas" anexado à resposta. Não bloqueia — apenas avisa.

- **`domain/MestreIATopicIndex.kt`** — ⚠️ **MORTO desde Lote 272.** Lê `topic_index.json` para "páginas garantidas", mas NENHUM arquivo o referencia (nem `carregar()`). Determinismo por tópico foi rejeitado. Não reviver sem rediscutir.

- **`domain/MestreIASemanticEngine.kt` / `MestreIAVectorEngine.kt`** — ⚠️ **DORMENTES p/ Auditor desde Lote 325.** Reranking cosseno e busca HNSW (ObjectBox). Só via GraphEngine (Forjador/Voz). Os utilitários `floatArrayToByteArray`/`byteArrayToFloatArray` do SemanticEngine ainda são usados na importação de embeddings (FichaDatabase).

- **`domain/MestreIAValidacaoReport.kt`** — Data classes do relatório de validação do Forjador: `ItemValidacao` (entrada, idEncontrado, status, mensagem) e `RelatorioValidacao` (vantagens/desvantagens/perícias/magias/técnicas, totalOk, totalFallback, alertaBudget). `StatusValidacao` enum: OK, FUZZY, FALLBACK, ERRO.

---

## 12. Domain — Tools (Forjador)

- **`domain/tools/ForjadorTools.kt`** — Define os schemas das **6 ferramentas** do Forjador: `forjador_ler_ficha`, `forjador_buscar_catalogo`, `forjador_gps_magia`, `forjador_editar_ficha`, `forjador_buscar_racas`, `forjador_aplicar_modelo_racial`. Exporta formato nativo Gemini (`getGeminiTools`) e formato OpenAI (`getOpenAITools`).

- **`domain/tools/ForjadorToolExecutor.kt`** — Executor das ferramentas do Forjador. Mapeia nome da tool → implementação Kotlin: `lerFicha` (lê seção do personagem), `buscarCatalogo` (busca em vantagens/desvantagens/perícias/magias + injeta `RegrasEspeciaisSchema`), `gpsMagia` (trilha mínima via NexusArcano), `editarFicha` (aplica mudanças no personagem via ViewModel), `buscarRacas` e `aplicarModeloRacial` (catálogos de raças/metacaracterísticas via `RacaCatalogo`/`MetacaracteristicaCatalogo`, carregados lazily). Faz read-back pós-edição (`lerSecao`).

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

- **`data/network/MestreIATools.kt`** — Schemas das ferramentas. **AUDITOR atual (Lote 325): `getAuditorToolsOpenAI`/`getAuditorToolsGemini`** = `localizar_no_codex` + `ler_pagina` + `inspecionar_personagem` + `consultar_nexus_arcano`. Modo `analise` usa `getAuditorUnificadoToolsOpenAI/Gemini` = ForjadorTools + localizar/ler + nexus (base corrigida no Lote 350 — antes apontava para o toolset legado de embedding, engano do commit d9d999c). O toolset legado (`getOpenAITools`/`getGeminiTools`, renomeado `getLegacyEmbeddingTools*` no Lote 349) foi DELETADO no Lote 350 junto com `getSheetSchema*`/`getArrayOf*` e as constantes `TOOL_MANUAL_DIRETO`/`TOOL_REGRAS_*`. A seleção por modo acontece em `MestreIAClient`.

- **`data/network/DiscordRollApiClient.kt`** — Cliente HTTP para o servidor Discord do projeto. Envia `DiscordRollPayload` (personagem, tipo de teste, dados, resultado) via POST. Também busca lista de `DiscordVoiceChannel` disponíveis. Data classes: `DiscordRollPayload`, `DiscordRollSendResult`, `DiscordVoiceChannel`. **[+ 2026-06-08]** `postPortrait(baseUrl, apiKey, characterName, imageDataUri)` → `POST /api/portrait` (sobe o retrato data:base64 que o bot reanexa nos embeds de rolagem).

- **`data/network/GeminiImageService.kt`** — **[+ 2026-06-09] MESTRE PINTOR.** Gera retrato artístico do personagem via Gemini Image API (`gemini-3.1-flash-image`, chave PAGA `MESTRE_IA_GEMINI_IMAGE_KEY`, ~$0,067/imagem, proporção 9:16). `gerarRetrato(prompt)` → POST `:generateContent` com `responseModalities=["IMAGE","TEXT"]`, devolve a imagem (base64). Fluxo: `FichaIADelegate.gerarRetratoIA()` → `GeminiImageService.gerarRetrato()` → `ImagemPersonagemStore.salvarImagem()` → `FichaViewModel.atualizarImagemPersonagem()`. Entradas na UI: dialog pós-Forjador (`DialogRetratoIA` em FichaScreen) e modo "pintor" no ChatInputBar (DialogsMestreIA).

---

## 15. Data — Storage (Room / Persistência)

- **`data/storage/FichaDatabase.kt`** — Configuração Room **v24** (Lote 259 adicionou `vec_chunks`). Entidades: `FichaEntity`, `ManualChunkEntity`, `GraphNodeEntity` (legado), `ChatSessionEntity`, `ChatMessageEntity`, `VecChunkEntity`. DAOs expostos: `fichaDao`, `manualChunkDao`, `graphNodeDao` (legado), `chatHistoryDao`, `vecChunkDao`. `fallbackToDestructiveMigration`. Método `prePopulateManual` (importa `chunks.jsonl` → `manual_chunks` FTS4 + embeddings → `vec_chunks`; reimporta só embeddings se chunks existem mas vec está vazio). `graphNodeDao` declarado mas GraphNode está descontinuado.

- **`data/storage/FichaDao.kt`** — DAO Room para fichas: `upsert`, `getJson`, `deleteByName`, `listNames` (ordenado por `updatedAt` DESC).

- **`data/storage/FichaEntity.kt`** — Entidade Room `fichas`: `nomeArquivo` (PK), `json` (texto completo), `updatedAt` (timestamp).

- **`data/storage/FichaStorageRepository.kt`** — Repositório de persistência de fichas. Migra fichas antigas de `SharedPreferences` → Room (operação única). `salvarFicha`, `carregarFicha`, `excluirFicha`, `listarFichas`. Normaliza nomes de arquivo para compatibilidade cross-versão.

- **`data/storage/ManualChunkDao.kt`** — DAO FTS4 para o Códex. `buscarRegras` (query FTS4 full-text), `buscarPorPagina`, `buscarPorPaginaESource`, `getChunkById`, `getCount`, `clearAll`. Tabela virtual FTS4 com `search_text` (texto + source_title).

- **`data/storage/ManualChunkEntity.kt`** — Entidade FTS4 `manual_chunks`: `chunk_id`, `text`, `source_title`, `source_id`, `page_number`, `search_text` (campo de busca composto).

- **`data/storage/MetacaracteristicaStore.kt`** — Persistência leve de metacaracterísticas criadas pelo usuário (arquivo `metacaracteristicas_usuario.json` em `filesDir`). Lista, salva (por nome, case-insensitive) e exclui. Usa JSON direto em vez de Room (sem migration necessária).

- **`data/storage/ImagemPersonagemStore.kt`** — **[+ 2026-06-08]** Processa e armazena o retrato do personagem em `filesDir/portraits/`. `salvarImagem(context, uri)` decodifica (com `inSampleSize`), corrige rotação via EXIF (`androidx.exifinterface`), enquadra o assunto principal (ML Kit **Subject Segmentation** — `play-services-mlkit-subject-segmentation`) refinando pelo rosto (ML Kit **Face Detection**), recorta na proporção do cabeçalho e salva **DUAS versões**: recortada (cabeçalho) e inteira (tela cheia, maior lado 1600px) — retorna `ImagensSalvas(recortadaUri, originalUri)`. `bytesBase64(caminho)` gera `data:image/jpeg;base64,...` para o Discord (mesma estratégia do VTT `resolveTokenImagePayload`). `excluirImagem(caminho)`. **[+ 2026-06-09]** `salvarDeBase64(context, dataUri)` — restaura a imagem EMBUTIDA numa ficha importada: decodifica o base64, grava arquivo temp e reusa `salvarImagem` (re-recorta o rosto + gera as 2 versões). Funções internas: `detectarAssunto`/`boundingBoxDaMascara` (FloatBuffer da máscara), `detectarRosto`, `recortarFaixa` (centro horizontal no assunto/rosto; vertical com margem acima do rosto, ou topo do assunto, ou topo da imagem), `redimensionar`/`redimensionarMaiorLado`. Sem Room, sem migration.

- **`data/storage/ChatHistoryDao.kt`** — DAO Room para histórico de chat: sessões (`getAllSessions`, `createSession`, `updateSessionTitle`, `updateSessionTimestamp`) e mensagens (`insertMessage`, `getMessagesForSession`).

- **`data/storage/ChatHistoryEntity.kt`** — Entidades Room: `ChatSessionEntity` (`chat_sessions`: id, title, createdAt, updatedAt) e `ChatMessageEntity` (`chat_messages`: id, sessionId, role, text, modelName, createdAt).

- **`data/storage/GraphNodeDao.kt`** — ⚠️ LEGADO — NÃO UTILIZADO. DAO Room para o grafo de conhecimento (descontinuado). Declarado no `FichaDatabase` mas nunca chamado pelo código ativo.

- **`data/storage/GraphNodeEntity.kt`** — ⚠️ LEGADO — NÃO UTILIZADO. Entidade Room `graph_knowledge` do grafo descontinuado. Mantida só para não quebrar a migration do Room.

- **`data/storage/VecChunkEntity.kt`** — (Lote 259) Entidade Room `vec_chunks`: `chunk_id` (PK) + `embedding` (ByteArray little-endian). Guarda os embeddings semânticos importados do `chunks.jsonl`. ⚠️ DORMENTE p/ Auditor desde Lote 325 (embeddings só usados via GraphEngine/Voz). Embeddings reais têm 3072 dims (Gemini) — o comentário "384 floats" no arquivo está desatualizado.

- **`data/storage/VecChunkDao.kt`** — (Lote 259) DAO Room dos embeddings: `insertAll`, `getByIds`, `getAll`, `getCount`, `clearAll`.

- **`data/storage/VecChunkOBEntity.kt`** — Entidade **ObjectBox** (não Room) para busca vetorial HNSW: `id`, `chunkId` (`@Index`), `embedding` (`@HnswIndex(dimensions = 3072)`). Usada pelo `MestreIAVectorEngine`. ⚠️ DORMENTE p/ Auditor.

- **`data/storage/ObjectBoxStore.kt`** — Singleton do `BoxStore` ObjectBox (`gurps_vec_store`), usado exclusivamente para o vector search HNSW. `init`/`get`/`close`. Room continua o banco principal. ⚠️ DORMENTE p/ Auditor.

---

## 16. Model

*Data classes puras. Sem lógica de negócio (exceto `Personagem.kt` que tem cálculos derivados).*

- **`model/Personagem.kt`** — Modelo raiz. Todos os campos do personagem GURPS 4ª Ed. (atributos primários/secundários, vantagens, desvantagens, qualidades, peculiaridades, perícias, técnicas, magias, equipamentos, modelo racial, HP/FP de rolagem, notas). Tem propriedades calculadas (`pontosVida`, `pontosFadiga`, `velocidadeBasica`, etc.) que usam `CharacterRules` e `TraitRuleRegistry`. `toJson`/`fromJson` para serialização. **[+ 2026-06-08]** Campos novos `imagemPersonagemUri` (foto RECORTADA do cabeçalho) e `imagemPersonagemOriginalUri` (foto INTEIRA p/ tela cheia) — ambos `file://` em `filesDir/portraits/`, default vazio (retrocompatível). **[+ 2026-06-09]** Campo `imagemPersonagemBase64` — preenchido APENAS na exportação (foto viaja embutida na ficha); limpo no import (não incha persistência local).

- **`model/PersonagemInterop.kt`** — Importação/exportação versionada. `importarJson` suporta envelope `{"schema":"gurps-ficha","character":{...}}` e fallback para JSON legado sem envelope. `exportarJson` gera o envelope com metadados (schemaVersion, exportedAtUtc, appVersion, uiVariant).

- **`model/CatalogosSuplementares.kt`** — Data classes dos catálogos suplementares: `PericiaSuplementarItem`, `TecnicaCatalogoItem`, `PericiaV2RuleMapItem` (e subclasses de regra: `PericiaV2TipoRegra`, `PericiaV2PreRequisitoRegra`, `PericiaV2PreDefinidoRegra`).

- **`model/ArmaCatalogoItem.kt`** — Data class de arma do catálogo: nome, dano, ST mínimo, peso, custo, grupo (perícia), aparar. **[+ 2026-06-14, Lote 371]** stats de combate lidos dos JSONs normalizados: `alcanceCorpoACorpo` ("C"/"1"/"1,2"), `duasMaos` (†/‡), `precisao` (Acc), `meioDanoMetros` (1/2D), `maximoMetros` (Máx), `alcanceMultStRaw` (×ST p/ arcos), `cadenciaTiro` (CdT), `tirosRaw`, `magnitude` (Bulk), `recuo` (Rcl). `Equipamento` (em `Personagem.kt`) ganhou os campos `arma*` correspondentes (anuláveis, backward-compatible) populados em `FichaEquipmentDelegate.adicionarEquipamentoArma`.

- **`model/ArmaduraCatalogoItem.kt`** — Data class de armadura: nome, RD, peso, custo, locais cobertos, componentes (lista de peças individuais), tags.

- **`model/EscudoCatalogoItem.kt`** — Data class de escudo: nome, BD, peso, custo, habilidade de bloqueio.

- **`model/MestreIAChunk.kt`** — Data class de chunk do Códex: `chunk_id`, `text`, `source_title`, `source_id`, `page_number`. Usado pelo RAG.

---

## 17. PreRequisitos

- **`regras_prerequisitos/PreRequisitoChecker.kt`** — Motor de verificação de pré-requisitos. `checkParseResult(personagem, parsed)` avalia um `ParseResult` contra o personagem (respeita `bypassValidation`) e retorna uma **String de relatório** ("todos requisitos atendidos" / "faltando: ..."). Também tem `checkSimples` (lista direta de `PreRequisitoType`) e `check` (legado). Data class `ConditionStatus` (label, isMet, current, required).

- **`regras_prerequisitos/PreRequisitoParser.kt`** — Parser de texto bruto de pré-requisito ("IQ 12+", "Magia X em NH 14+") → `ParseResult` (`tipos`, `terms`, `bypassValidation`, `warnings`). Cada `PreRequisitoTerm` tem `alternatives: List<List<PreRequisitoType>>` (OR de grupos AND). Detecta marcador de bypass (`#`/especial) → `bypassValidation = true`.

- **`regras_prerequisitos/PreRequisitoType.kt`** — Sealed class com `readableName()` e **17 tipos** de pré-requisito (não 5): `AttributeMin`, `AptidaoMagica`, `MagiaConhecida`, `VantagemConhecida`, `PericiaConhecida`, `MagiasEscola`, `MagiaInclusaNaContagem`, `QualquerMagiaComNome`, `QuantidadeOutrasMagias`, `QuantidadeMagiasPorEscolas`, `QuantidadeMagiasPorTemas`, `MagiasEmEscolasDiferentes`, `AtributosSomaMin`, `NaoPodeSer`, `SkillMinLevel`, `NivelMin`. (Bypass NÃO é um tipo aqui — é o flag `bypassValidation` do `ParseResult`.)

---

## 18. UI — Telas Principais

- **`ui/FichaScreen.kt`** — Container principal. Scaffold com `FichaCustomNavigationBar` e roteamento de dialogs globais (importação, erro de carga, atualização). Abas reais: **Geral, Traços, Perícias, Técnicas, Magia, Equip., Rolagem** (na variante Pracego a aba "Magia" é omitida da barra). A aba **VTT** entra em modo imersivo (esconde o chrome via `vttImmersiveUi`). Combate e Notas são exibidos dentro de outras abas/seções, não como abas separadas na barra. **[+ 2026-06-08]** Quando há foto, o `topBar` vira `CabecalhoComImagem` (private composable: foto de fundo altura fixa 140dp + gradiente + título e linha de pontos overlaid; ícone câmera troca a foto; toque abre tela cheia). `ImagemPersonagemFullscreenDialog` (private): foto INTEIRA em tela cheia, fundo preto, sem texto, X/toque fecha. Picker via `ActivityResultContracts.OpenDocument()` (explorador de arquivos completo, qualquer pasta — não só galeria) chama `ImagemPersonagemStore.salvarImagem`. Sem foto: `TopAppBar` padrão + ícone "adicionar foto".

- **`ui/TabGeral.kt`** — Aba de informações básicas: nome, jogador, campanha, pontos iniciais/gastos/restantes, atributos primários (ST/DX/IQ/HT) com custo, atributos secundários (PV, PF, Vontade, Percepção, Velocidade, Deslocamento), modelo racial ativo.

- **`ui/TabCombate.kt`** — Aba de combate: defesas ativas (Esquiva, Apara, Bloqueio) com bônus manual editável, lista de armas equipadas com dano calculado, armaduras por local corporal com RD total.

- **`ui/TabPericias.kt`** — Aba de perícias: busca por texto/atributo/dificuldade, lista com NH calculado e pontos gastos, adição do catálogo, edição inline de pontos.

- **`ui/TabMagias.kt`** — Aba de magias: busca por escola e classe, lista com custo de energia reduzido por NH, modo alvo (Nexus Arcano) com trilha mínima e chaves de progressão.

- **`ui/TabTracos.kt`** — Aba de vantagens e desvantagens: busca, listagem por custo, adição do catálogo com seleção de modificadores. Ponto de entrada para `VantagemDialogs` e `DesvantagemDialogs`.

- **`ui/TabTecnicas.kt`** — Aba de técnicas: listagem com NH calculado relativo à perícia base, busca por nome/fonte.

- **`ui/TabEquipamentos.kt`** — Aba de equipamentos: lista de itens com peso individual e total, adição de arma/armadura/item genérico do catálogo.

- **`ui/TabRolagem.kt`** — Hub de rolagem. Lista atributos, perícias, magias e traços com ataque inato para rolagem de 3d6. Exibe resultado, margem de sucesso/falha, críticos. Dispatch para Discord se configurado. **[+ 2026-06-09]** `executarRolagem` usa `CriticoRules.classificar` (regra COMPLETA com NH) e, em testes de COMBATE (Ataque/Defesa/Técnica/Magia) que dão Decisivo/Crítico, chama `dispararTabelaCritica` → 2ª rolagem 3d6 nas tabelas → 2ª mensagem ao Discord.

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

- **ui/features/dice3d/Dice3DScene.kt** — Cena 3D física que simula a rolagem dos dados usando SceneView (Filament) e JBullet, substituindo mock 2D. Aplica as cores via shader LinearSrgb.

- **ui/features/dice3d/DiceColorSetup.kt** — Menu premium de customização visual dos dados (ConfigurarDadosDialog), contendo o Dice3DPreview (mini cena 3D giratória em tempo real) e o DiceColorsStore (SharedPreferences).

- **ui/features/dice3d/PhysicsWorld.kt** — Setup da engine JBullet. Mapeia a colisão, restituição, paredes elásticas e detecta os lados do dado.

- **ui/features/dice3d/DiceSoundManager.kt** — Gerencia os sons físicos (batidas) mapeados pela simulação do JBullet em tempo real.


- **`ui/features/rolagem/DialogoSentidos.kt`** — **[+ 2026-06-14, Lote 372]** Diálogo de Testes de Sentidos: tocar **PER** (intercept em `TabRolagem`, sem alterar `AtributosQuickRollPanel`) abre os 5 sentidos com valor efetivo + "notinha" do motivo (via `SentidoRules`); cada um rola pelo mesmo caminho (`executarRolagem`→Discord) com o rótulo carregando o bônus/redutor. Sentido bloqueado fica desabilitado ("Cego"/"Surdo"). **Variante PraCego:** botão rotulado grande ("Rolar (14)") + semântica TalkBack.

- **`ui/features/virtualtabletop/MesaVirtualScreen.kt`** — Tela da Mesa Virtual (placeholder). Exibe estado de conexão e botões de ação VTT. Ainda em desenvolvimento.

- **`ui/features/virtualtabletop/MesaVirtualViewModel.kt`** — ViewModel da Mesa Virtual. `MesaVirtualState` com discordId, token, campaignId, isConnected, activePlayers. `conectar()` apenas atualiza o estado local (integração Railway planejada).

---

## 21. UI — Componentes Utilitários

- **`ui/components/FichaCustomNavigationBar.kt`** — Barra de navegação inferior customizada com ícones e labels das abas. Suporta `onLongPress` no ícone do Mestre IA para ativar a voz. Recebe `estadoLive: EstadoLive` (Gemini Live) e o mapeia internamente para `EstadoVoz` (OUVINDO→ESCUTANDO/anel verde; FALANDO/CONECTANDO/PROCESSANDO→anel amarelo), reusando o anel visual pulsante existente.

- **`ui/components/GeminiLiveService.kt`** — (~81KB) Serviço de **voz em tempo real** via Gemini Live API (WebSocket OkHttp). Substituiu o antigo `VozMestreIA`/`SpeechRecognizer`. Captura áudio (`AudioRecord`) e reproduz (`AudioTrack`), gerencia a sessão WebSocket bidirecional, despacha tool calls para `GeminiLiveTools`. Estados: `EstadoLive` (OCIOSO, CONECTANDO, OUVINDO, FALANDO, PROCESSANDO, ERRO). Mantém `EstadoVoz` por compatibilidade com a navbar. Ver `project_gemini_live_estado.md`.

- **`ui/components/GeminiLiveTools.kt`** — Roteador de ferramentas da Voz. `executar(nome, args)` mapeia as tools do Gemini Live → implementações: lê ficha (`lerFicha`), busca catálogo/edita/GPS de magias/raças (delega ao `ForjadorToolExecutor`), e `consultarManual` (RAG via `MestreIAGraphEngine.buscarDiretoNoCodex` — **caller ativo do GraphEngine**, com truncamento de payload p/ evitar code=1007 do servidor Live). Mantém aliases legados (`obterFicha`, `adicionarVantagem`, etc.).

- **`ui/DialogStandards.kt`** — Padrões visuais de dialogs: dimensões, espaçamentos, cores de botões primário/secundário/destrutivo.

- **`ui/HorizontalDivider.kt`** — Divisor horizontal estilizado usado em listas e seções.

- **`ui/SectionCard.kt`** — Card de seção com título e conteúdo, usado em TabGeral e TabCombate.

- **`ui/UiStandards.kt`** — Constantes de design do app: padding padrão, tamanhos de fonte, breakpoints.

- **`ui/UiA11y.kt`** — Helpers de acessibilidade: `semantics` para TalkBack, labels descritivos. Usado pela variante Pracego.

- **`ui/UiActionLabels.kt`** — Strings de labels de ação para acessibilidade (variante Pracego): "adicionar vantagem", "remover perícia", etc.

---

## 22. UI — Tema

- **`ui/theme/Color.kt`** — Paleta de cores do app (Material You / esquemas claro e escuro). Paleta **única** — não há cores condicionadas à variante Visual/Pracego (a diferenciação de variante é só comportamental, nas telas).

- **`ui/theme/Theme.kt`** — `GURPSFichaTheme`: configura `MaterialTheme` com `ColorScheme` e `Typography`. Usa **Material You dynamic color** (`dynamicDark/LightColorScheme` em Android 12+) com fallback para `DarkColorScheme`/`LightColorScheme`; ajusta a cor da status bar. **Não** lê `BuildConfig.UI_VARIANT` — a diferenciação Visual/Pracego acontece nas telas/dialogs (via `isPraCegoVariant = BuildConfig.UI_VARIANT == "pracego"`), não no tema.

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

*Nomes de arquivo conferidos contra os `assets.open(...)` reais em `CatalogLoaders.kt`,
`DataRepository.kt`, `RacaCatalogo.kt`, `MetacaracteristicaCatalogo.kt` e `MestreIATopicIndex.kt`.*

| Arquivo | Conteúdo | Carregado por |
|---|---|---|
| `vantagens.v3.json` | Vantagens oficiais GURPS 4ª Ed. (formato v3 com modificadores estruturados) | `CatalogLoaders` |
| `vantagens_artes_marciais.v1.json` | Vantagens exclusivas do suplemento Artes Marciais | `CatalogLoaders` |
| `desvantagens.v2.json` | Desvantagens oficiais (formato v2 com specialRule) | `CatalogLoaders` |
| `pericias.json` | Perícias do Módulo Básico | `CatalogLoaders` |
| `pericias_artes_marciais.v1.json` | Perícias suplementares (Artes Marciais) | `CatalogLoaders` |
| `pericias_v2_rules_map.json` | Mapa de regras de perícias v2 (tipo, pré-requisito, predefinido) | `CatalogLoaders` |
| `magias2versao.json` | Magias com pré-requisitos raw | `CatalogLoaders` |
| `tecnicas.v1.json` | Técnicas (arquivo único — Módulo Básico + suplementos) | `CatalogLoaders` |
| `armas_corpo_a_corpo.v1.normalized.json` | Armas de combate corpo a corpo | `CatalogLoaders` |
| `armas_distancia.v1.normalized.json` | Armas de ataque à distância | `CatalogLoaders` |
| `armas_fogo.v1.normalized.json` | Armas de fogo | `CatalogLoaders` |
| `modificadores.v1.json` | Catálogo global de modificadores | `CatalogLoaders` |
| `armaduras.v2.json` | Armaduras com componentes por local corporal | `CatalogLoaders` |
| `escudos.v1.json` | Escudos com BD | `CatalogLoaders` |
| `racas.v1.json` | Raças jogáveis (formato enxuto — sem custos, recalculado) | `RacaCatalogo` |
| `metacaracteristicas.v1.json` | Pacotes prontos de metacaracterísticas (Gigante, Anão, etc.) | `MetacaracteristicaCatalogo` |
| `forjador_templates.json` **[+ 2026-06-09]** | 60 templates/arquétipos de personagem prontos (combatentes, furtivos, sociais, 10 magos, gêneros modernos). IDs validados contra o catálogo. | `ForjadorTemplateCatalogo` |
| `tabelas_criticas.json` **[+ 2026-06-09]** | 4 tabelas de combate (Golpe Fulminante, na Cabeça, Erro Crítico, Erro Crítico Desarmado), entradas 3-18 com texto completo. | `CriticoRules` |
| `bestiario.v1.json` **[+ 2026-06-14]** | Bestiário da Saga: 17 criaturas (goblin, orc, lobo, ogro, esqueleto, bandido…) com stats de combate e ataques. Validado por `scripts/check_bestiario.py`. | `BestiarioCatalogo` / `BestiarioLoader` |
| `mestre_ia_temas.json` | Temas canônicos de busca para o Mestre IA | `DataRepository` |
| `chunks.jsonl` | Chunks do manual GURPS (1 por página, FTS4). 54.9MB com embeddings; Auditor usa só o texto, embeddings (3072 dims) dormentes p/ ele. | `FichaDatabase.prePopulateManual` |
| `chunks.jsonl.bak` | Idêntico SEM embeddings (6.5MB, 1196 linhas). Candidato a substituir o .jsonl quando confirmado que Auditor não precisa de embedding. | (não carregado — backup) |
| `topic_index.json` | ⚠️ Páginas garantidas — lido só por `MestreIATopicIndex.carregar()`, que existe mas **NINGUÉM chama** (MORTO desde Lote 272). Asset órfão na prática. | `MestreIATopicIndex` (nunca invocado) |

> **Nota:** há vários assets de apoio/backup não consumidos em runtime (`*.schema.json`,
> `topic_index_backup_manual.json`, `topic_index_gerado.json`, `pericias_v2_rules_map copy.json`).
> Não são catálogos ativos.

---

## 26. Scripts de Manutenção (pasta `scripts/`)

*~40 scripts Python no total. Destaques (todos conferidos como existentes):*

- **`generate_pericias_v2_rules_map.py`** — Gera o mapa de regras de perícias v2 a partir do texto bruto.
- **`fix_mojibake_project.py`** — Corrige encoding corrompido (mojibake) em todo o projeto.
- **`cleanup_assets_text.py`** — Normaliza textos e limpa artefatos de OCR de PDFs.
- **`gerar_embeddings.py`** — Gera os embeddings dos chunks (importados no `chunks.jsonl`).
- **`gerar_topic_index.py`** — Gera o `topic_index.json` (asset hoje órfão).
- **`processar_livro.py` / `sanitize_manuals.py`** — Pipeline de ingestão dos manuais (chunks do Códex).
- **Série `convert_*.py` / `normalize_*.py`** — Convertem/normalizam dados brutos (planilhas, PDFs) para o formato JSON dos assets (vantagens, desvantagens, perícias, armas, armaduras, escudos, técnicas).
- **Série `validate_*.py`** — Validação de integridade dos catálogos (armaduras, técnicas, associações de texto).
- **`check_bestiario.py`** (Lote 363) — valida `bestiario.v1.json`. **`check_armas.py`** **[+ 2026-06-14, Lote 371]** — valida que os 3 catálogos de armas têm os stats de combate (reach CaC; precisão/alcance/CdT/Bulk à distância).
- (⚠️ a antiga doc citava `audit_active_jsons_v2.py`, que **não existe** na pasta.)

---

## 27. Motor Nexus Arcano (módulo separado `motor modo alvo/src/`)

- **`NexusArcanoEngine.kt`** — Orquestrador: avalia chaves, computa trilha ótima (A*/guloso via `planejarCaminhoMinimo`), retorna `ArcanoResultado`.
- **`ArcanoModels.kt`** — Modelos: `ArcanoChave`, `ArcanoMetaTipo`, `ArcanoMetaProgress`, `ArcanoEstadoPersonagem`, `ArcanoResultado`.
- **`ArcanoCatalogo.kt`** — Interface que o adapter implementa para fornecer pré-requisitos e escolas ao engine.
- **`NexusArcanoHeuristics.kt`** — Avalia quantas magias de cada escola o personagem possui para detectar chaves desbloqueadas.
- **`NexusArcanoParser.kt`** — Interpreta texto bruto de pré-requisito de magia → lista de dependências tipadas.
- **`NexusArcanoPathfinder.kt`** — DFS/guloso para encontrar caminho mínimo até a magia alvo.
- **`NexusArcanoStrings.kt`** — Formatação de mensagens para a UI (avisos, trilha de aprendizado, bloqueios).
- **`ArcanoCatalogoDesejoExemplo.kt`** — Catálogo de exemplo (fixture) para testar o motor sem o app.
- **`diagnostico_desejo.kt` / `diagnostico_parser.kt` / `diagnostico_real.kt`** — Mains de diagnóstico standalone do motor (parser, pathfinder, cenário real). Ferramentas de depuração, não fazem parte do app.

---

## 28. Testes Automatizados (`app/src/test/`)

*Pacote base: `com/gurps/ficha/` (exceto a suíte `nexus/arcano/`, que fica em `app/src/test/java/nexus/arcano/`).*

- **`domain/rules/RulesLayerTest.kt`** — Testes de `CharacterRules` e `CombatRules` (atributos, PV, defesas).
- **`domain/rules/MagiaEnergiaRulesTest.kt`** — Redução de custo de energia por NH.
- **`model/PersonagemRulesTest.kt`** — Validação de criação de personagem e limites de pontos.
- **`model/PersonagemInteropTest.kt`** — Import/export versionado (envelope + fallback legado).
- **`model/PericiaJsonParsingTest.kt`** — Parsing dos JSONs de perícias.
- **`domain/filters/TextNormalizerTest.kt`** — Os 4 presets do `TextNormalizer`.
- **`domain/MestreIAContextFilterTest.kt`** — String de contexto da ficha enviada à IA.
- **`domain/MestreIARagEngineTest.kt`** — Motor RAG (GraphEngine).
- **`data/network/MestreIAClientTest.kt`** — Montagem de request / parsing de tool calls.
- **`data/storage/FichaStorageRepositoryTest.kt`** — Persistência de fichas.
- **`domain/roll/RollDispatchPolicyTest.kt`** — Política de retry/erro de rolagem.
- **`regras_prerequisitos/PreRequisitoParserTest.kt`** — Parser de pré-requisitos.
- **`ui/TabCombateStateTest.kt`** — Estado da aba de combate.
- **`domain/magias/NexusArcanoLoteFCanonicScenarioTest.kt`** + **`NexusArcanoModoAlvoAdapterTest.kt`** — Cenários do adapter Nexus Arcano.
- **`nexus/arcano/NexusArcanoEngine*Test.kt`** — Suíte massiva do motor de magias (Lote1/2/3, GlobalA/B, StressMagiasV2, AuditoriaTodasMagias) + `NexusArcanoTestCatalog.kt` (catálogo de fixtures).
- **`vtt/VttBridgeCodecStressTest.kt`** — Teste de robustez do codec VTT.
- **[+ 2026-06-14] Combate da Saga** (`domain/combat/`): `CombatEncounterTest`, `CombatActionsTest` (inclui Mover e Atacar correto), `HitLocationRulesTest`, `InjuryRulesTest`, `NpcCombatBrainTest`, `CombatResolverTest`, `CombatSessionTest` (sessão ponta a ponta: arma/tipo de dano/distância, narração, avaliar, postura, mover dirigido, rajada, **dual-wield: 2 golpes + mão inábil −4/Ambidestria, sem defesa após Ataque Total**).
- **[+ 2026-06-14] Narrador/Saga** (`domain/saga/`): `NarradorToolsTest` (contrato das 16 tools), `NarradorOutputValidatorTest`, `NarradorToolExecutorCombatTest` (roteamento das 6 tools de combate via `CombatBridge` falsa). Instrumentado: `SagaFoundationTest` (FTS4 real).

---

## 29. Endereços Rápidos (Funções Críticas)

| O que buscar | Onde está |
|---|---|
| Esquiva / Apara / Bloqueio (cálculo) | `CombatRules.kt` → `calcularEsquiva/Apara/Bloqueio` |
| Bônus de Mestre de Armas | `MestreDeArmasRule.kt` → `getDamageBonusPerDie` |
| Golpe/Empurrão por ST | `CharacterRules.kt` → `calcularDanoGdP / calcularDanoGeB` (tabelas privadas `tabelaGdP / tabelaGeB`, com extrapolação) |
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
| **Combate Saga: orquestra o encontro** | `domain/combat/CombatSession.kt` (heroiAtaca/**heroiAtaqueDuplo**/npcResolve/heroiMove/heroiAvaliar/narrarTroca; golpe único em `resolverGolpeHeroi`) |
| **Combate: dual-wield / sem defesa pós-Ataque Total** | `CombatSession.heroiAtaqueDuplo` (mão inábil −4/Ambidestria, MB p.366) + flag `heroiSemDefesaAtiva` (anula defesa do herói) · UI `SubDialogoAlvoLocal` modo Duplo |
| **Combate: NH efetivo / Mover e Atacar** | `domain/combat/CombatActions.kt` → `calcularNH` (CaC −4+teto 9; à distância −2) |
| **Combate: dano localizado / tipo (pa*→pi*)** | `HitLocationRules.aplicarDano` + `CombatSession.tipoDano` |
| **Combate: ponte motor↔UI / arma escolhível** | `viewmodel/delegates/SagaCombatController.kt` (`construirAtaques`, `CombatUiState`) |
| **Narrador: tools / executor (combate via bridge)** | `domain/saga/NarradorTools.kt` + `NarradorToolExecutor.kt` (`CombatBridge`) |
| **Narrador: estado da aba + bridges** | `viewmodel/delegates/FichaSagaDelegate.kt` (RollBridge + CombatBridge + narrarFimDeCombate) |

---

## 30. Variantes de Build

| Variante | Foco |
|---|---|
| `Visual` | Estética visual, cores vibrantes, layouts densos |
| `Pracego` | Acessibilidade total (TalkBack), labels extras, diálogos simplificados |

Chave de controle: `BuildConfig.UI_VARIANT` (usado para condicionar lógica de UI entre as variantes — ex.: `isPraCegoVariant` espalhado pelas telas/dialogs).

Cada variante tem seu próprio **source set** com um ponto de entrada de UI:
- **`app/src/visual/.../ui/AppUiEntry.kt`** e **`app/src/pracego/.../ui/AppUiEntry.kt`** — `@Composable AppUiEntry(viewModel)` específico de cada flavor. Ambos hoje delegam a `FichaScreen(viewModel)`; o source set garante que cada build compile a sua versão. `MainActivity` chama `AppUiEntry` dentro de `GURPSFichaTheme`.

---

## 31. Servidor Discord (Node/Express — fora do `app/`)

*Pasta `discord-roll-api/` (raiz do projeto Android). Node 18+, Express. Roda no Railway. NÃO é compilado pelo Gradle.*

- **`discord-roll-api/src/server.js`** — API que publica rolagens no Discord via bot. Rotas: `GET /health`, `GET /api/channels` (lista canais de voz, com cache 30min), `POST /api/rolls` (monta mensagem da rolagem e envia ao canal), `GET|POST /api/fichas*` (persistência in-memory de fichas na nuvem por `deviceId`). `formatRollMessage` formata texto (crítico, margem). `sendToDiscord` envia ao endpoint do Discord. **[+ 2026-06-08]** Map `portraits` (in-memory: sanitizedName → {mime,buffer,ext}); `parseDataUri`/`sanitizeName`; rota nova **`POST /api/portrait`** {character, image(data:base64)} guarda o retrato; `sendToDiscord` passou a aceitar portrait opcional → com retrato manda **embed + multipart** (FormData/Blob, globais Node 18+) com `thumbnail` `attachment://portrait.<ext>`, sem retrato manda `{content}` como antes; `/api/rolls` busca `portraits.get(sanitizeName(payload.character))`. Limite do `express.json` subiu p/ 8mb. **[+ 2026-06-09]** `classificarCritico(soma, nh)` aplica a regra COMPLETA com NH (corrige a simplificada 3-4/17-18); `formatRollMessage` detecta a 2ª mensagem de tabela crítica (testType começa com 💥/💀) e renderiza o texto cru. ⚠️ portraits e fichas são in-memory (perdem no restart do Railway). ⚠️ Mudanças exigem **deploy** no Railway p/ valer online.

> **Nota de build [+ 2026-06-08]:** `app/build.gradle.kts` ganhou deps p/ a feature de imagem:
> `com.google.mlkit:face-detection:16.1.7`, `com.google.android.gms:play-services-mlkit-subject-segmentation:16.0.0-beta1`
> e `androidx.exifinterface:exifinterface:1.3.7` (Coil 2.5.0 já existia).

---

## 32. SAGA / NARRADOR / MOTOR DE COMBATE  [+ 2026-06-14, Lotes 349-370]

*Modo solo-RPG narrado por IA (3º modo de IA = `saga`). Fluxo de IA detalhado em `ARQUITETURA_MESTRE_IA.md §10`.
Tudo em `domain/combat/` é Kotlin PURO (sem Android, determinístico por seed) e coberto por testes (§28).*

### 32.1 Narrador (IA modo `saga`)
- **`domain/MestreIANarradorUseCase.kt`** — Orquestrador do modo `saga` (clone do Generator: fila de fallback de modelos + loop de tool-use + Auto-Healing + `consultar_mundo` automático por palavra-chave). Narração no Gemini 2.5 Flash.
- **`domain/saga/NarradorTools.kt`** — Schemas das **16 tools** (14 próprias + `localizar_no_codex`/`ler_pagina` reusadas do Auditor). Spec neutra única → Gemini + OpenAI. `NarradorToolsTest` garante toolset == executor.
- **`domain/saga/NarradorToolExecutor.kt`** — Roteador nome→impl. Interfaces `RollBridge` (rolagem interativa) e **`CombatBridge`** (combate). Reais: fato/mundo/inspecionar/cena/rolagem/Códex + 6 de combate. `forjar_npc`/`avancar_relogio`/`passar_tempo` = `nao_implementado` (Fase C/D).
- **`domain/saga/NarradorOutputValidator.kt`** — Anti-confabulação: alarme se a prosa cita número/regra que não veio de tool no turno.
- **`domain/saga/CampanhaConfig.kt`** — Session zero (gênero/tom/dificuldade/magia/NT/livros) → bloco no prompt.
- **`data/network/MestreIAPromptsNarrador.kt`** — Persona categorial do Narrador (8 leis de ferro; a nº 8 = protocolo de combate).
- **`viewmodel/delegates/FichaSagaDelegate.kt`** — Estado da aba Saga; implementa `RollBridge` + `CombatBridge`; resolve NH→3d6→`CriticoRules`; persiste turnos (chat sessão `saga#<id>`); `narrarFimDeCombate` (prosa + saque + XP).
- **`ui/TabSaga.kt`** — UI da aba (campanhas, feed/máquina de escrever, card de rolagem, `ConfiguracaoJogoDialog` tela cheia, `BarraDeRolagem`). Renderiza `CombatePainel` (com `weight`) quando há combate.

### 32.2 Persistência da Saga (Room v26)
- **`data/storage/SagaEntities.kt`** — `CampanhaEntity` (+`configJson`), `CenaEntity`, `CampaignFactEntity` (FTS4), `WorldStateEntity`.
- **`data/storage/SagaDao.kt`** — CRUD + `buscarFatos` (MATCH AND/fallback OR, ranking peso→frequência) + `excluirCampanhaCompleta` (@Transaction). `FichaDatabase` v24→25→26 com `MIGRATION_24_25`/`MIGRATION_25_26` explícitas.

### 32.3 Motor de combate puro (`domain/combat/`)
- **`CombatModels.kt`** — `Postura`/`Condicao`/`Manobra`, `NpcStats` (com `armaNh`), `Combatente` (PV/PF/postura/condições mutáveis; `vivo`/`caido`).
- **`CombatEncounter.kt`** — Iniciativa (Vel.Básica→DX→seed), `proximoTurno`, `manobrasLegais`, `estadoResumo`, distância MUTÁVEL (`moverEmRelacaoAoHeroi`/`definirDistancia`).
- **`CombatActions.kt`** — `calcularNH` (manobra/postura/local/visibilidade/`modsExtra`/`magnitudeArma`) + `resolverAtaque` (3d6) + `avaliarRolagem`. **Mover e Atacar: CaC −4+teto 9; à distância −2 OU a Magnitude/Bulk, o pior** (MB p.366/271, Lote 375).
- **`ModificadoresCombate.kt`** — `LocalAtaque` (penalidades p/ acertar, MB p.398), `Visibilidade`, `AtaqueTotalModo`.
- **`HitLocationRules.kt`** — Dano localizado (paridade Mesa Virtual: crânio×4, vitais×3 perf, limites de membro).
- **`InjuryRules.kt`** — Choque, ferimento grave, cheques de morte, KO, recuperação de atordoamento, `ferir(Combatente)`.
- **`NpcCombatBrain.kt`** — Intenção tática do NPC (fuga por moral, arqueiro mantém distância, bruto avança).
- **`CombatResolver.kt`** — Modificadores de defesa (recuo/Defesa Total/apara extra/bloqueio 1×; **esgrima → apara extra −2**, param `esgrima`, Lote 375) + `resolverTroca` (ataque→defesa→dano→ferimento; crítico anula defesa).
- **`CombatSession.kt`** — **Orquestra o encontro:** `heroiAtaca(AtaqueHeroi,…)`, `npcIntencao`/`npcResolve` (defesa interativa), `heroiMove`/`heroiManobra`/`heroiAvaliar`, **`heroiApontar`** (mira → +Acc, Lote 373), `narrarTroca` (log evocativo + colchete técnico), `tipoDano`/`rolarDano` (`pa*`→`pi*`), `penalidadeDistancia` (MB p.550), **`parseAparar`** (E/D/Não, Lote 375). Regras por ataque: **reach/Máx** (`dist > alcance` → não alcança), **1/2D** (dano pela metade), **Bulk** (Avançar-e-Atacar), **Apontar/Acc**, **Aparar E/D** (`opcoesDefesaHeroi(armaPronta)` tira aparar de arma à distância/Não/desbalanceada-já-usada via flag `atacouDesbalanceada`). Tipos: `HeroiPerfilCombate` (defesa), `AtaqueHeroi` (arma escolhível: nh/dano/tipo/alcance/precisao/meioDano/magnitude/apararTipo), `ApararTipo`, `ResultadoCombate`.
- **`model/BestiarioModels.kt`** + **`domain/loaders/BestiarioCatalogo.kt`** — Catálogo de criaturas (`assets/bestiario.v1.json`) → `Combatente` (`novoCombatente`). Loader com cache. ⚠️ Gson não roda init de data class → `Bestiario.get()` busca direto (sem mapa cacheado).

### 32.4 UI e ponte de combate
- **`viewmodel/delegates/SagaCombatController.kt`** — Embrulha `CombatSession` com estado Compose (`CombatUiState`/`CombatenteUi`/`FaixaDistancia`/`DefesaPendenteUi`) + corrotinas + ponte de defesa suspensa. `construirAtaques` lê as armas da ficha (corpo-a-corpo + fogo/distância, perícia casada por grupo/nome, dano por ST, tipo correto, reach/Acc/1-2D/Máx/Bulk/aparar). `heroiApontar`; **`sacarArma(indice)`** (Saque Rápido = livre, senão Preparar gasta o turno, Lote 374); alvos corpo-a-corpo por reach. Devolve PV/saque/XP à ficha (`sagaConcederXp`/`sagaDefinirPvAtual`/`sagaAdicionarItem` no ViewModel).
- **`ui/saga/CombatUi.kt`** — Visual aprovado: `CombatTracker` (faixas Engajado→Extremo, barra de PV, postura/condições, avatar de inicial colorida = **placeholder do retrato real**), `SeletorDeArma` (mostra arma na mão + alcance; "Sacar" = Preparar), `ManeuverCards` + sub-diálogos (alvo/local, Mover dirigido, Avaliar, **Apontar**, Postura), `DefendaSeCard`. TalkBack em tudo.

### 32.5 Regras de arma no combate — COMPLETAS (Lotes 371-375)
Stats de arma vêm do catálogo → ficha (`Equipamento.arma*`) → `AtaqueHeroi`: **reach** ("C"/"1"/"2", engajamento), **Acc + Apontar**, **1/2D** (meio dano), **Máx** (não alcança além), **Mover-e-Atacar** (CaC −4+teto / à distância −2 ou **Bulk**), **Aparar E/D** (esgrima/desbalanceada/Não/à distância), **Sacar/Preparar** (arma pronta vs guardada; livre c/ Saque Rápido). Sentidos na Rolagem: §5 `SentidoRules` + §20 `DialogoSentidos`.

### 32.6 Pendências
- **Validação no aparelho** do combate ponta a ponta (chaves de IA reais) — pendente.
- **Futuro:** CdT/Recuo/rajada (RoF/Rcl), dual-wield, Ataque Total à distância (+1). **Retratos reais de NPC/cena** (Mestre Pintor em tempo real) — registro Lotes B7/E2 do plano. Fases C/D/E do plano Saga.

---

> [!TIP]
> **DICA PARA O AGENTE**: Ao modificar regras de combate ou magias, rode `NexusArcanoLoteFCanonicScenarioTest.kt`, `RulesLayerTest.kt` e a suíte `domain/combat/*Test.kt` (Saga). **Regra de combate só se implementa lendo a fonte literal: `assets/chunks.jsonl` (Códex), não só o resumo do `Skill_GURPS.MD`** — ver `ARQUITETURA_MESTRE_IA.md §10`.
>
> **Sobre o Mestre IA (pós-Lote 328):** o AUDITOR não usa mais RAG semântico — usa
> `localizar_no_codex` + `ler_pagina` (`MestreIARepository`). Para ajustar a busca/ranking do
> Auditor, mexa em `MestreIARepository.rankearPorBM25` — **NÃO** no `MestreIAGraphEngine` (legado
> p/ Auditor). Os 3 dicionários de sinônimos (`MestreIAPlanner`, `MestreIAGraphEngine`,
> `MestreIAQueryEngine`) só importam para o GraphEngine/Forjador/Voz, não para o Auditor.
> Antes de mexer em qualquer arquivo do Mestre IA, leia `ARQUITETURA_MESTRE_IA.md §5` (código
> legado/morto e desde quando) para não reanimar algo descontinuado.
