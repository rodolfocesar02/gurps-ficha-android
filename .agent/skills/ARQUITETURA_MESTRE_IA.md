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
    *   **Descrição:** O Motor de Busca RAG. Realiza busca direta nos `chunks.jsonl` via FTS SQLite — **o grafo foi descontinuado como rota primária**. Extrai palavras-chave da query, faz busca agressiva com pool de até 1.500 chunks candidatos, aplica um motor de pontuação com pesos por raridade de termo e bonus por expansão de sinônimos (via dicionário do `MestreIAPlanner`), e retorna os top-30 chunks mais relevantes para a IA.

*   **`MestreIAPlanner.kt`** — `domain/`
    *   **Descrição:** O "Batedor" (Pré-processador de Queries). Transforma linguagem leiga em termos técnicos de GURPS usando mapeamentos locais. Gera um `PlanoDeBusca` com: termos técnicos extraídos, categorias de regra identificadas, sub-queries temáticas paralelas (multi-query decomposition) e contexto de stats de equipamentos do inventário do personagem (para perguntas sobre armas específicas como alcance de pistola subaquática).

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
    *   **Modo:** FORJADOR
    *   **Descrição:** A "Caixa de Ferramentas do Forjador". Define os schemas JSON de Function Calling para criação e edição de fichas, compatíveis com Gemini e OpenAI. Ferramentas: `forjador_ler_ficha` (lê seções da ficha atual), `forjador_buscar_catalogo` (busca vantagens/desvantagens/perícias/magias no catálogo oficial), `forjador_gps_magia` (GPS de pré-requisitos — calcula o caminho mínimo até uma magia alvo) e `forjador_editar_ficha` (edita a ficha diretamente: adicionar/remover/alterar).

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

*   **`ChatHistoryEntity.kt`** — `data/storage/`
    *   **Descrição:** Modelo da tabela de histórico de conversas no banco Room. Armazena role (user/assistant), texto, nome do modelo usado e timestamp da sessão.

*   **`CatalogFilters.kt`** — `domain/filters/`
    *   **Descrição:** Ferramenta de higienização de texto. Remove acentos, corrige Mojibake (ex: `ǜ→a`, `Ǹ→e`), padroniza espaços e converte para lowercase. Usada pelo `MestreIAQueryEngine` e pelo `MestreIAPlanner` para garantir que buscas sejam agnósticas a formatação e encoding.

---

## 8. A "Interface" (UI do Chat)

*   **`DialogsMestreIA.kt`** — `ui/` *(arquivo não listado no documento original)*
    *   **Descrição:** A tela de chat com o Mestre IA. Composable `DialogMestreIA` que exibe o histórico de mensagens com scroll automático, campo de input com seletor de modo (Auditor/Forjador), indicador de carregamento e suporte a cópia de mensagens. Conecta-se ao `FichaViewModel` para disparar `MestreIAUseCase` ou `MestreIAGeneratorUseCase` conforme o modo selecionado.

---

## 9. A "Voz" (Reconhecimento de Fala)

*   **`VozMestreIA.kt`** — `ui/components/`
    *   **Descrição:** O Ouvido do Mestre IA. Encapsula o `SpeechRecognizer` nativo do Android para captura de comandos de voz em PT-BR. Ciclo de vida: `iniciar()` abre o microfone → `onEstado(ESCUTANDO)` → usuário fala → `onEstado(PROCESSANDO)` → `onResultado(texto)` entrega o texto reconhecido → `onEstado(OCIOSO)`. `cancelar()` interrompe; `liberar()` destrói o recognizer (chamado no `DisposableEffect` do `FichaScreen`). **Não usa servidor próprio** — depende do Google Speech Recognition instalado no dispositivo.
    *   **Integração:** Instanciado em `FichaScreen`. `onResultado` chama `viewModel.conversarComMestreIA(texto, "geracao")` — o texto vai direto para o Forjador sem passar pelo chat. O dialog do Mestre IA abre automaticamente com a resposta.
    *   **Ativação:** Long press no ícone do Mestre IA na `FichaCustomNavigationBar`. Toque simples continua abrindo o chat normalmente.
    *   **Feedback visual:** `FichaCustomNavigationBar` recebe `estadoVoz: EstadoVoz` e exibe anel pulsante ao redor do ícone — **verde** durante `ESCUTANDO`, **amarelo** durante `PROCESSANDO`.
    *   **Permissão:** `RECORD_AUDIO` já declarada no `AndroidManifest.xml`. Android solicita ao usuário na primeira vez que o long press é acionado.

---

> [!NOTE]
> Esta arquitetura usa **RAG Direto nos Chunks** como rota primária de busca. O MestreIAUseCase (Auditor) consome RAG do manual. O MestreIAGeneratorUseCase (Forjador) **não usa RAG** — trabalha exclusivamente com o catálogo de IDs e as ferramentas do Forjador. Os dois modos compartilham o MestreIAClient (rede), MestreIATools/ForjadorTools (function calling) e FichaDatabase (persistência).
