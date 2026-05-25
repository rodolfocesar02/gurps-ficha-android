# Arquitetura do Mestre IA - GURPS Ficha Android

Este documento descreve todos os arquivos que compõem o ecossistema da Inteligência Artificial do aplicativo, divididos por sua função técnica e responsabilidade.

*Para o mapa completo de todos os arquivos do projeto, ver `MAPA_DETALHADO.md`.*

---

## 1. O "Cérebro" (Lógica e Orquestração)

Estes arquivos gerenciam o fluxo de pensamento da IA, desde o recebimento da pergunta até a montagem da resposta final.

*   **`MestreIAUseCase.kt`** — `domain/`
    *   **Modo:** AUDITOR (Dúvidas de Regras)
    *   **Descrição:** O "Maestro do Auditor". Recebe a pergunta do usuário, sincroniza o Códex (chunks) se necessário, aciona o `MestreIAGraphEngine` para busca RAG, monta o histórico de conversa e envia tudo ao `MestreIAClient`. Contém o loop de Function Calling para a ferramenta `consultar_manual_direto`. Também instancia o `NexusArcanoEngine` para cálculos de pré-requisitos de magias, adaptando o catálogo do `DataRepository`.

*   **`MestreIAGeneratorUseCase.kt`** — `domain/` *(arquivo não listado no documento original)*
    *   **Modo:** FORJADOR (Criação e Edição de Fichas)
    *   **Descrição:** O "Maestro do Forjador". Especializado em criação e análise de fichas — **não usa RAG** do manual (chunks são ruído para esse modo). Injeta o catálogo de IDs oficiais no prompt, executa o loop de Function Calling das ferramentas do Forjador (`ForjadorTools`) via `ForjadorToolExecutor`, e valida a resposta final com `MestreIAValidacaoReport`.

*   **`MestreIAGraphEngine.kt`** — `domain/`
    *   **Modo:** AUDITOR (Dúvidas de Regras)
    *   **Descrição:** O Motor de Busca RAG. Realiza busca direta nos `chunks.jsonl` via FTS SQLite — **o grafo foi descontinuado como rota primária**. Extrai palavras-chave da query, faz busca com pool de 200 candidatos pré-filtrados pelo FTS4 (limit fixo para escalar com 5000+ chunks sem degradar), aplica BM25-Kotlin com IDF por termo + bonus AND (todos os termos presentes) + bonus de proximidade (termos < 100 chars), delega reranking semântico ao `MestreIASemanticEngine` (top-50 BM25 → cosseno híbrido 60/40), injeta páginas garantidas via `MestreIATopicIndex` e retorna os top-30 chunks com formatação por estrelas de relevância (★★★/★★/★) via `formatarParaIA`. Inclui "Pocket RAG": chunks de baixa relevância são comprimidos às sentenças que contêm os termos buscados.

*   **`MestreIAPlanner.kt`** — `domain/`
    *   **Modo:** AUDITOR e LIVE (compartilhado)
    *   **Descrição:** O "Batedor" (Pré-processador de Queries). Transforma linguagem leiga em termos técnicos de GURPS usando mapeamentos locais. Gera um `PlanoDeBusca` com: termos técnicos extraídos, categorias de regra identificadas, sub-queries temáticas paralelas (multi-query decomposition) e contexto de stats de equipamentos do inventário do personagem (para perguntas sobre armas específicas como alcance de pistola subaquática).

*   **`MestreIATopicIndex.kt`** — `domain/`
    *   **Modo:** AUDITOR e LIVE (compartilhado)
    *   **Descrição:** O "Garantidor de Páginas Críticas". Singleton que lê `topic_index.json` dos assets e resolve, para qualquer query, quais páginas específicas dos manuais **devem** entrar no contexto RAG — mesmo que o FTS4 falhe por keyword mismatch. Lógica de matching em dois níveis: (1) `require_all` — todos os termos devem estar presentes na query; (2) `fallback_any` — basta um par [keyword1, keyword2] presentes. Garante que, por exemplo, a pergunta "atirar numa piscina" sempre traga Pyramid p.7 (tiro subaquático), mesmo que a palavra "subaquático" não apareça na pergunta.

*   **`MestreIASemanticEngine.kt`** — `domain/`
    *   **Modo:** AUDITOR e LIVE (compartilhado)
    *   **Descrição:** O Motor de Reranking Semântico. Singleton que executa reranking híbrido (BM25 + cosseno) sobre os top-50 chunks candidatos do BM25. Fluxo: gera embedding da query via API Gemini (`gemini-embedding-001`, 3072 dims, `RETRIEVAL_QUERY`), busca embeddings pré-computados dos candidatos na tabela `vec_chunks`, calcula similaridade cosseno em Kotlin puro, combina `score_final = 0.6×BM25_norm + 0.4×cosseno`. Possui **cache de embedding por sessão** (`ConcurrentHashMap`, thread-safe para coroutines paralelas, até 50 entradas) — evita chamadas repetidas à API Gemini quando a mesma query aparece em múltiplas tool calls. Fallback gracioso para BM25 puro se `vec_chunks` estiver vazio ou se houver ≤10 candidatos.

*   **`MestreIARuleAuditor.kt`** — `domain/` *(arquivo não listado no documento original)*
    *   **Descrição:** O "Motor Fiscal". Valida as sugestões da IA contra as regras oficiais implementadas no `CharacterRules.kt`. Audita custo de atributos, dificuldade de perícias e níveis sugeridos, gerando `AuditNote` com campo auditado, valor sugerido pela IA e valor correto calculado.

*   **`MestreIAContextFilter.kt`** — `domain/` *(arquivo não listado no documento original)*
    *   **Descrição:** O "Resumidor de Ficha". Gera um snapshot textual compacto da ficha do personagem atual para enviar como contexto à IA. Filtra metadados técnicos e expõe apenas o que é relevante: atributos, HP/FP atual, lista de vantagens/desvantagens/perícias principais. No modo "conversa" inclui aparência e histórico.

*   **`MestreIAValidacaoReport.kt`** — `domain/` *(arquivo não listado no documento original)*
    *   **Descrição:** Modelo de dados do relatório de validação do Forjador. Define `ItemValidacao` (cada item com status OK/FUZZY/FALLBACK/ERRO) e `RelatorioValidacao` (resultado completo com listas de vantagens, desvantagens, perícias, magias e técnicas validadas, mais `alertaBudget` para alertar sobre limite de pontos).

---

## 2. As "Ferramentas" (Function Calling)

Definem as funções que a IA pode chamar durante seu raciocínio.

*   **`MestreIATools.kt`** — `data/network/`
    *   **Modo:** AUDITOR
    *   **Descrição:** A "Caixa de Ferramentas do Auditor". Define os schemas JSON de Function Calling compatíveis com Gemini (Native) e OpenAI/DeepSeek. Ferramentas disponíveis: `consultar_manual_direto` (busca RAG nos chunks), `inspecionar_personagem` (lê seções da ficha: armas com dano, armaduras com RD, atributos, status de HP/FP) e `consultar_nexus_arcano` (cálculo de pré-requisitos de magias).

*   **`ForjadorTools.kt`** — `domain/tools/` *(arquivo não listado no documento original)*
    *   **Modo:** FORJADOR e LIVE
    *   **Descrição:** A "Caixa de Ferramentas do Forjador". Define os schemas JSON de Function Calling para criação e edição de fichas, compatíveis com Gemini e OpenAI. Ferramentas: `forjador_ler_ficha` (lê seções da ficha atual), `forjador_buscar_catalogo` (busca vantagens/desvantagens/perícias/magias no catálogo oficial), `forjador_gps_magia` (GPS de pré-requisitos — calcula o caminho mínimo até uma magia alvo) e `forjador_editar_ficha` (edita a ficha diretamente: adicionar/remover/alterar).

*   **`ForjadorToolExecutor.kt`** — `domain/tools/` *(arquivo não listado no documento original)*
    *   **Modo:** FORJADOR e LIVE
    *   **Descrição:** O "Braço Executor do Forjador". Recebe o nome e args JSON de cada tool call do Forjador e executa a ação real no app. Delega: `forjador_ler_ficha` → lê seções da `FichaViewModel`; `forjador_buscar_catalogo` → busca no `DataRepository` + injeta schema de regras especiais via `RegrasEspeciaisSchema` (para traços com custo variável como Aliado, Garras, Inimigo); `forjador_gps_magia` → calcula trilha de pré-requisitos via `NexusArcanoModoAlvoAdapter`; `forjador_editar_ficha` → aplica edição real na ficha via `FichaViewModel`; `forjador_buscar_racas` / `forjador_aplicar_modelo_racial` → carrega catálogo de raças e metacaracterísticas via `RacaCatalogo` / `MetacaracteristicaCatalogo`. Reutilizado integralmente pelo `GeminiLiveTools`.

*   **`RegrasEspeciaisSchema.kt`** — `domain/tools/` *(arquivo não listado no documento original)*
    *   **Modo:** FORJADOR e LIVE
    *   **Descrição:** O "Dicionário de Regras Especiais de Custo". Singleton com schemas textuais extraídos diretamente do `CharacterRules.kt` para traços cujo custo **não é fixo** — dependem de metadados (ex: Aliado, Inimigo, Garras, Vício, Contato, Reputação, Dor Crônica, Vulnerabilidade). Quando o `ForjadorToolExecutor` detecta que um traço tem `specialRule`, injeta o schema correspondente na resposta da tool call — assim a IA sabe exatamente quais campos de `metadados` preencher, evitando alucinação de custo.

---

## 3. A "Boca" (Comunicação e Rede)

Responsáveis por levar a pergunta até os servidores e trazer a resposta.

*   **`MestreIAClient.kt`** — `data/network/`
    *   **Descrição:** O Mensageiro Híbrido. Utiliza `HttpURLConnection` para chamadas de rede com timeout de 120s. Suporta dois backends: **Gemini** (Google Native) e **OpenRouter/OpenAI** (DeepSeek, MiMo, etc.), escolhidos dinamicamente pelas chaves configuradas. Gerencia montagem do JSON final, modo streaming, histórico de mensagens e parsing de Function Calls nos dois formatos de API.

*   **`MestreIAResponse.kt`** — `data/network/`
    *   **Descrição:** O "Molde Resiliente da Ficha". Define as classes de dados e o desserializador tolerante a variações que mapeia o JSON da IA para os campos da ficha. Aceita atributos em múltiplos formatos (objeto `atributos`, campos soltos PT `forca/destreza`, campos soltos EN `st/dx`). Suporta a flag `substituir` (lista de seções que devem ser substituídas, não somadas) para edições cirúrgicas.

*   **`MestreIAPromptsAuditor.kt`** — `data/network/` *(era "MestreIAPrompts.kt" no documento original — arquivo renomeado/dividido)*
    *   **Modo:** AUDITOR
    *   **Descrição:** O "Código de Conduta do Auditor". Contém o prompt de sistema do modo AUDITOR com: diretiva de fidelidade exclusiva ao Códex, método analógico (usar regras próximas quando a exata não existe), protocolo obrigatório de cálculo passo-a-passo (Citar → Identificar → Calcular → Concluir) e protocolo de variáveis completas (nunca confundir stat da arma com distância cênica da pergunta).

*   **`MestreIAPromptsForjador.kt`** — `data/network/` *(arquivo não listado no documento original)*
    *   **Modo:** FORJADOR
    *   **Descrição:** O "Código de Conduta do Forjador". Contém o prompt de sistema do modo FORJADOR com: tabela completa de custo de atributos/vantagens/desvantagens/perícias de GURPS 4ª Ed., o template JSON canônico (`GOLD_TEMPLATE`) que a IA deve preencher, lista de IDs oficiais do catálogo (injetada dinamicamente) e instruções de uso das ferramentas do Forjador.

*   **`DiscordRollApiClient.kt`** — `data/network/` *(arquivo não listado no documento original)*
    *   **Modo:** Independente (Rolagem de Dados)
    *   **Descrição:** O "Arauto do Discord". Cliente HTTP que envia os resultados de rolagens de dados (`3d6`, etc.) para um webhook ou endpoint externo de integração com Discord. Payload inclui: personagem, tipo de teste, contexto, alvo, modificador, dados rolados, total, resultado (sucesso/falha/crítico) e margem. Usado para que jogadores em sessão remota vejam as rolagens em tempo real no canal Discord da mesa.

---

## 4. O "Mecanismo de Busca" (Query Engine)

*   **`MestreIAQueryEngine.kt`** — `data/` *(arquivo não listado no documento original)*
    *   **Descrição:** O Gerador de Queries FTS4. Responsável por montar strings de busca válidas para o SQLite FTS4. Contém: lista de stopwords em PT-BR (palavras ignoradas na busca), dicionário de sinônimos técnicos de GURPS (ex: `agu` → `submers, subaquat, underwat, piscin, mergulh`) e a lógica de geração de queries agressivas com OR expansivo e prefixo wildcard (`termo*`). **Regra crítica interna:** FTS4 não suporta wildcard dentro de parênteses — o engine garante o formato correto.

---

## 5. O "Arquivo Morto" (Dados e Persistência)

Arquivos que lidam com a gravação e leitura de dados no dispositivo.

*   **`MestreIARepository.kt`** — `data/`
    *   **Descrição:** O Guardião do Códex. Especializado em persistência e sincronização do banco de chunks RAG. Garante que `chunks.jsonl` esteja importado no SQLite antes de qualquer busca (operação atômica protegida por Mutex). Gerencia versionamento do Códex (atualmente v2: texto + source_title) para forçar re-importação quando o formato evolui. Delega a busca FTS ao `ManualChunkDao` e a geração de queries ao `MestreIAQueryEngine`.

*   **`DataRepository.kt`** — `data/`
    *   **Descrição:** A Interface Central de Dados. Define o contrato de tudo que pode ser buscado no app (fichas, vantagens, desvantagens, perícias, magias, raças, metacaracterísticas, etc.) e serve como ponte entre os UseCases e os DAOs/loaders. Expõe `buscarNoCodexDireto()` para o `MestreIAGraphEngine` e `sincronizarCodexSeNecessario()` para o `MestreIAUseCase`.

*   **`ChatHistoryDao.kt`** — `data/storage/`
    *   **Descrição:** O Historiador. Gerencia o banco de dados Room para salvar e recuperar o histórico das conversas por sessão.

*   **`FichaDatabase.kt`** — `data/storage/`
    *   **Descrição:** O Coração de Dados (SSOT). Gerencia o banco SQLite via Room, unificando quatro pilares: Fichas (`FichaDao`), Histórico de Chat (`ChatHistoryDao`), Manual de Regras (`ManualChunkDao`) e Grafo de Conhecimento (`GraphNodeDao`). Contém `prePopulateManual()` que importa `chunks.jsonl` para o SQLite na primeira execução, e a Lógica de Purificação (Lote 110/111) que reconstrói tabelas técnicas para corrigir Mojibake sem afetar dados do usuário.

---

## 6. A "Enciclopédia" (Assets de Conhecimento)

As fontes de verdade que alimentam o sistema com o conhecimento oficial de GURPS.

*   **`chunks.jsonl`** — `assets/`
    *   **Descrição:** A Biblioteca (Recortes). Contém milhares de parágrafos extraídos diretamente dos manuais de GURPS (Módulo Básico, Artes Marciais, Pirâmide, etc.). É a **fonte primária do RAG** — fornece prova documental com número de página para todas as respostas do Auditor. Importado para o SQLite como tabela FTS4 (`manual_chunks`) na inicialização do app.

*   **`topic_index.json`** — `assets/`
    *   **Descrição:** O Índice de Tópicos Críticos. JSON com lista de tópicos de regras que **precisam de páginas garantidas** no RAG, mesmo quando a FTS4 falha por keyword mismatch. Cada tópico tem: `id`, `keywords` (pool de termos), `require_all` (termos obrigatórios para match primário), `fallback_any` (pares de termos para match secundário) e `pages` (lista de `{source_id, pages[]}` a injetar). Atualmente cobre: tiro subaquático, combate subaquático, queda/dano, dano por fogo, asfixia/afogamento, acerto/falha crítica, carga/encumbrance, alcance de tiro, magia/custo de energia, sanidade/medo e movimentação na água. **Editável sem recompilação** — o `MestreIATopicIndex` lê em runtime.

*   **`mestre_ia_temas.json`** — `assets/`
    *   **Descrição:** Catálogo de temas semânticos do Mestre IA. Define clusters de termos relacionados usados pelo `MestreIAPlanner` para expansão de queries — vai além do dicionário hardcoded, permitindo adicionar novos temas sem recompilar.

*   **`magias2versao.json`** — `assets/`
    *   **Descrição:** Catálogo completo de magias de GURPS 4ª Ed. com pré-requisitos, custo de energia, escolas e níveis. Base de dados do `NexusArcanoEngine` (GPS de pré-requisitos) e do `ForjadorTools` (`forjador_gps_magia`).

*   **`pericias.json`** / **`pericias_v2_rules_map.json`** / **`pericias_artes_marciais.v1.json`** — `assets/`
    *   **Descrição:** Catálogos de perícias. `pericias.json` contém todas as perícias do Módulo Básico. `pericias_v2_rules_map.json` mapeia regras especiais de dificuldade por perícia (usado pelo `MestreIARuleAuditor`). `pericias_artes_marciais.v1.json` adiciona perícias exclusivas do suplemento Artes Marciais.

*   **`vantagens.v3.json`** / **`vantagens.v3.schema.json`** / **`vantagens_artes_marciais.v1.json`** — `assets/`
    *   **Descrição:** Catálogos de vantagens. `vantagens.v3.json` é a lista principal com IDs oficiais, custo e metadados. `vantagens.v3.schema.json` define o schema de validação para importação. `vantagens_artes_marciais.v1.json` adiciona vantagens do suplemento Artes Marciais.

*   **`desvantagens.v2.json`** / **`desvantagens.v2.schema.json`** — `assets/`
    *   **Descrição:** Catálogo de desvantagens com IDs oficiais, custo (positivo = pontos que o jogador recebe), tipo de custo e `specialRule` para traços de custo variável.

*   **`armas_corpo_a_corpo.v1.normalized.json`** / **`armas_fogo.v1.normalized.json`** / **`armas_distancia.v1.normalized.json`** — `assets/`
    *   **Descrição:** Catálogos normalizados de armas por categoria (corpo a corpo, armas de fogo, distância/arremesso). Cada entrada tem: nome, dano, ST mínima, alcance, peso, custo e tipo de combate. Usados pelo `MestreIAGraphEngine` nas tabelas técnicas injetadas no contexto da IA.

*   **`armaduras.v2.json`** / **`escudos.v1.json`** — `assets/`
    *   **Descrição:** Catálogos de equipamentos de proteção. `armaduras.v2.json` lista armaduras com RD, local protegido e peso. `escudos.v1.json` lista escudos com bônus de Aparar e RD.

*   **`tecnicas.v1.json`** — `assets/`
    *   **Descrição:** Catálogo de técnicas de Artes Marciais e GunFu. Cada técnica tem: nome, perícia base, nível padrão e custo de pontos para aprimorar.

*   **`racas.v1.json`** — `assets/`
    *   **Descrição:** Catálogo de raças/espécies jogáveis. Cada raça tem um `ModeloRacial` com atributos modificados e lista de vantagens/desvantagens raciais embutidas. Usado pelo `ForjadorToolExecutor` (`forjador_buscar_racas` / `forjador_aplicar_modelo_racial`) e pelo `GeminiLiveTools`.

*   **`metacaracteristicas.v1.json`** — `assets/`
    *   **Descrição:** Catálogo de metacaracterísticas padrão (pré-definidas pelos desenvolvedores). Complementa o `MetacaracteristicaStore` (que armazena as criadas pelo usuário).

*   **`modificadores.v1.json`** — `assets/`
    *   **Descrição:** Catálogo de modificadores de vantagens e desvantagens (ex: "Sempre Ativo", "Custo de Manutenção"). Permite calcular custo final de traços com modificadores aplicados.

*   **`graph_knowledge.json`** — `assets/`
    *   **Descrição:** O Códex Legado (Grafo). Contém regras resumidas em formato de nós de grafo.
    *   **⚠️ LEGADO — NÃO UTILIZADO:** Substituído pelo RAG direto nos `chunks.jsonl`. O arquivo existe no assets mas não é mais carregado nem consultado em nenhum ponto do código.

---

## 7. A "Estrutura" (Modelos e Entidades)

Define o formato físico de como os dados são tratados no código.

*   **`MestreIAChunk.kt`** — `model/`
    *   **Descrição:** O modelo de domínio de um fragmento de regra. Representa um parágrafo extraído do manual com: `chunk_id`, `text` (texto original para a IA), `source_title` (nome do livro), `source_id` (ex: `pt_modulo_basico`) e `page_number`.

*   **`ManualChunkEntity.kt`** — `data/storage/` *(arquivo não listado no documento original)*
    *   **Descrição:** A entidade Room da tabela `manual_chunks` (FTS4). Espelha `MestreIAChunk` mas adiciona `search_text` — versão normalizada do texto usada exclusivamente para busca FTS, sem afetar o texto original exibido à IA.

*   **`ManualChunkDao.kt`** — `data/storage/` *(arquivo não listado no documento original)*
    *   **Descrição:** DAO da tabela FTS de chunks. Expõe: `buscarRegras()` (busca MATCH FTS4), `buscarPorPagina()`, `buscarPorPaginaESource()` (busca por página + livro específico), `getChunkById()`, `getCount()` e `clearAll()`.

*   **`GraphNodeEntity.kt`** — `data/storage/`
    *   **Descrição:** A entidade Room da tabela `graph_nodes` (FTS4). Representa um nó do grafo de conhecimento com: `entityId`, `title`, `level` (nível da comunidade), `summary` (conhecimento destilado), `category` e `source_id` para rastreabilidade bibliográfica.
    *   **⚠️ LEGADO:** Mantida apenas para não quebrar o schema do Room. Nenhum dado é inserido ou consultado ativamente.

*   **`GraphNodeDao.kt`** — `data/storage/` *(arquivo não listado no documento original)*
    *   **Descrição:** DAO do grafo de conhecimento. Expõe busca FTS (`buscarNodes`), busca por ID (`getNodeById`), por categoria (`findByCategory`), por título (LIKE) e `getEssentialNodes()`.
    *   **⚠️ LEGADO — NÃO UTILIZADO:** O DAO está declarado no `FichaDatabase` e instanciado (lazy) no `DataRepository`, mas **nenhum método seu é chamado em nenhum lugar do app**. A tabela `graph_nodes` existe no banco porém está vazia e inativa. Candidato a remoção futura.

*   **`VecChunkEntity.kt`** — `data/storage/`
    *   **Descrição:** Entidade Room da tabela `vec_chunks` — armazena embeddings semânticos pré-computados para busca híbrida BM25 + cosseno. Cada linha representa um chunk do manual com `chunk_id` (chave primária, liga a `manual_chunks`) e `embedding` (384 floats serializados como `ByteArray` little-endian = 1536 bytes por chunk). Embeddings gerados offline pelo script Python `gerar_embeddings.py` com modelo `all-MiniLM-L6-v2`.

*   **`VecChunkDao.kt`** — `data/storage/`
    *   **Descrição:** DAO da tabela `vec_chunks`. Expõe: `insertAll()` (upsert em lote), `getByIds(ids)` (busca por lista de chunk_ids — usado pelo `MestreIASemanticEngine` para buscar vetores dos candidatos BM25), `getCount()` (verificação de disponibilidade — se 0, semântico é pulado) e `clearAll()`.

*   **`FichaStorageRepository.kt`** — `data/storage/`
    *   **Descrição:** O Repositório de Fichas (CRUD). Camada de acesso a dados especializada em fichas de personagem. Gerencia: migração única de `SharedPreferences` → Room (para usuários que vieram da versão antiga), `salvarFicha()` / `carregarFicha()` / `listarFichas()` / `excluirFicha()` via `FichaDao`. Também expõe o `ChatHistoryDao` para que o `FichaViewModel` acesse histórico sem depender do `FichaDatabase` diretamente.

*   **`MetacaracteristicaStore.kt`** — `data/storage/`
    *   **Descrição:** Persistência leve de metacaracterísticas criadas pelo usuário. Armazena uma lista de `ModeloRacial` (com tipo `METACARACTERISTICA`) em arquivo JSON no `filesDir` (`metacaracteristicas_usuario.json`) — **evita migration do Room** para uma feature opcional. CRUD simples: `listar()`, `salvar()` (upsert por nome, case-insensitive), `remover()`. Decisão de design documentada: metacaracterística é o mesmo pacote de um modelo racial, reutilizando o tipo existente.

*   **`ChatHistoryEntity.kt`** — `data/storage/`
    *   **Descrição:** Modelo da tabela de histórico de conversas no banco Room. Armazena role (user/assistant), texto, nome do modelo usado e timestamp da sessão.

*   **`CatalogFilters.kt`** — `domain/filters/`
    *   **Descrição:** Ferramenta de higienização de texto. Remove acentos, corrige Mojibake (ex: `ǜ→a`, `Ǹ→e`), padroniza espaços e converte para lowercase. Usada pelo `MestreIAQueryEngine` e pelo `MestreIAPlanner` para garantir que buscas sejam agnósticas a formatação e encoding.

---

## 8. A "Interface" (UI do Chat)

*   **`DialogsMestreIA.kt`** — `ui/` *(arquivo não listado no documento original)*
    *   **Descrição:** A tela de chat com o Mestre IA. Composable `DialogMestreIA` que exibe o histórico de mensagens com scroll automático, campo de input com seletor de modo (Auditor/Forjador), indicador de carregamento e suporte a cópia de mensagens. Conecta-se ao `FichaViewModel` para disparar `MestreIAUseCase` ou `MestreIAGeneratorUseCase` conforme o modo selecionado.

---

## 9. A "Voz" (Voz Bidirecional em Tempo Real — Gemini Live)

> ⚠️ **Voz Clássica Descontinuada:** Os arquivos `VozMestreIA.kt`, `VozTTS.kt` e `VozIntencaoClassifier.kt` foram removidos. O único modo de voz ativo é o Gemini Live (bidirecional). A enum `EstadoVoz` foi migrada para `GeminiLiveService.kt` e mantida pois ainda é usada pelo anel visual da `FichaCustomNavigationBar`.

*   **`GeminiLiveService.kt`** — `ui/components/`
    *   **Descrição:** O Coração da Voz Bidirecional. Gerencia a conexão **WebSocket persistente** com a API Gemini Live (`wss://generativelanguage.googleapis.com/.../BidiGenerateContent`). Fluxo completo: `iniciarSessao(contextoFicha)` → conecta WebSocket → envia `setup` (modelo + voz + prompt de sistema + tools) → aguarda `setupComplete` → injeta contexto da ficha + saudação → inicia captura de microfone e reprodução de áudio em paralelo.
    *   **Captura de áudio:** `AudioRecord` a 16kHz/PCM16 em coroutine de I/O. Envia chunks de ~100ms como `realtimeInput.audio` em Base64. **Bloqueia o envio do microfone enquanto o modelo está falando** (`modeloFalando=true`) para evitar auto-interrupção.
    *   **Reprodução de áudio:** `AudioTrack` a 24kHz/PCM16 em `MODE_STREAM`. Canal de coroutine com capacidade 200 chunks — garante ordem e ritmo natural sem pular frames. Ao iniciar novo turno de áudio, `limparFilaAudio()` descarta chunks anteriores e reseta o `AudioTrack`.
    *   **Keepalive:** Envia áudio silencioso (100ms de zeros PCM) a cada 20s para manter o WebSocket vivo.
    *   **Function Calling:** Ao receber `toolCall`, executa as ferramentas via `onToolCall` (implementado por `GeminiLiveTools`), exibe label visual de feedback imediato (ex: "📖 Consultando o Códex..."), e devolve `toolResponse` ao servidor.
    *   **Transcrições:** Captura `inputTranscription` (o que o usuário falou) via `onTranscricaoUsuario` e `outputTranscription`/`text` (o que o modelo respondeu) via `onRespostaMestre` — ambos exibidos no chat para manter histórico visual.
    *   **Estados:** `EstadoLive` — `OCIOSO`, `CONECTANDO`, `OUVINDO`, `FALANDO`, `ERRO`.
    *   **Prompt de sistema:** Embutido no próprio arquivo — contém todas as ferramentas disponíveis, fluxo para raças/metacaracterísticas, protocolo obrigatório de cálculo (citar → identificar → calcular → concluir), regras de comportamento (nunca inventar regras ou IDs) e estilo de voz (sábio, justo, levemente dramático).
    *   **Modelo e voz:** `BuildConfig.GEMINI_LIVE_MODEL` (`gemini-3.1-flash-live-preview`) e `BuildConfig.GEMINI_LIVE_VOICE` (`Charon`).

*   **`GeminiLiveTools.kt`** — `ui/components/`
    *   **Descrição:** O Executor de Ferramentas do Gemini Live. Implementa o callback `onToolCall` do `GeminiLiveService` — recebe o nome e args da ferramenta chamada pelo modelo de voz e delega para os executores corretos. Reutiliza integralmente o `ForjadorToolExecutor` para edição de fichas e o `MestreIAGraphEngine` para RAG, garantindo que a voz bidirecional tenha exatamente as mesmas capacidades do chat de texto.
    *   **Ferramentas disponíveis:** `lerFicha` (lê seções da ficha), `buscarCatalogo` (busca IDs oficiais — previne alucinação), `editarFicha` (edita ficha via `ForjadorToolExecutor`), `trilhaDeMagias` (GPS de pré-requisitos), `consultarManual` (RAG com multi-query via `MestreIAPlanner` + `MestreIAGraphEngine`), `forjador_buscar_racas`, `forjador_aplicar_modelo_racial`.
    *   **Compatibilidade:** Mapeia ferramentas legadas (`obterFicha`, `adicionarVantagem`, etc.) para as APIs atuais — garante que sessões antigas ainda funcionem.
    *   **RAG no Live:** `consultarManual` usa `runBlocking` para executar busca síncrona no `MestreIAGraphEngine`, com multi-query via `MestreIAPlanner.subQueriesTemáticas`. Retorna até 20 chunks com instrução de citar a página e calcular passo a passo.

---

> [!NOTE]
> Esta arquitetura tem **dois caminhos de voz independentes**: (1) Clássico — `VozMestreIA` captura STT → `VozIntencaoClassifier` decide o modo → `MestreIAUseCase`/`MestreIAGeneratorUseCase` processam → `VozTTS` fala a resposta. (2) Bidirecional — `GeminiLiveService` gerencia WebSocket direto com Gemini Live, que faz STT+IA+TTS em um único serviço de streaming. `GeminiLiveTools` garante que ambos os caminhos tenham acesso às mesmas ferramentas (RAG, catálogo, edição de ficha). Controlado por `BuildConfig.VOZ_BIDIRECIONAL_HABILITADA`.

> [!NOTE]
> Esta arquitetura usa **RAG Direto nos Chunks** como rota primária de busca. O MestreIAUseCase (Auditor) consome RAG do manual. O MestreIAGeneratorUseCase (Forjador) **não usa RAG** — trabalha exclusivamente com o catálogo de IDs e as ferramentas do Forjador. Os dois modos compartilham o MestreIAClient (rede), MestreIATools/ForjadorTools (function calling) e FichaDatabase (persistência).
