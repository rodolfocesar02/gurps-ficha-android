# 🗺️ Arquitetura do Mestre IA - GURPS Ficha Android

Este documento descreve todos os arquivos que compõem o ecossistema da Inteligência Artificial do aplicativo, divididos por sua função técnica e responsabilidade.

---

## 🧠 1. O "Cérebro" (Lógica e Orquestração)

Estes arquivos gerenciam o fluxo de pensamento da IA, desde o recebimento da pergunta até a montagem da resposta final.

*   **`MestreIAUseCase.kt`**
    *   **Descrição:** O "Maestro" do sistema. Ele coordena a execução de todas as tarefas: recebe a pergunta do usuário, aciona o motor de busca (GraphEngine), busca o histórico de conversas e formata os dados para enviar ao cliente de rede. É a ponte entre a Interface (UI) e a lógica de IA.
*   **`MestreIAGraphEngine.kt`**
    *   **Descrição:** O Motor Nexus (Busca Híbrida). Ele é responsável pelo "GraphRAG". Primeiro, busca no grafo de conhecimento (`graph_knowledge.json`) e, se encontrar referências a páginas, faz uma "Ponte de Página" para buscar o texto bruto nos `chunks.jsonl`.
*   **`MestreIAPlanner.kt`**
    *   **Descrição:** O "Batedor". É responsável pelo pré-processamento da dúvida do usuário, transformando linguagem leiga em termos técnicos e categorias oficiais para otimizar a precisão da busca.
*   **`MestreIAPrompts.kt`**
    *   **Descrição:** O "Código de Conduta". Contém as diretrizes de personalidade e regras de RPG para os dois modos da IA: o **FORJADOR** (criação e análise de fichas) e o **AUDITOR** (tira-dúvidas de regras oficiais).

---

## 🌐 2. A "Boca" (Comunicação e Rede)

Responsáveis por levar a pergunta até os servidores (Gemini/OpenRouter) e trazer a resposta.

*   **`MestreIAClient.kt`**
    *   **Descrição:** O Mensageiro. Utiliza `HttpURLConnection` para fazer as chamadas de rede. Gerencia os timeouts (90s), a autenticação via API Key e a montagem do JSON final que vai para o servidor.
*   **`MestreIAResponse.kt`**
    *   **Descrição:** O Molde da Ficha. Define as classes de dados e o desserializador resiliente (Lote 83) que mapeia a resposta da IA para os campos da ficha (atributos, vantagens, perícias, etc.), lidando com variações de nomenclatura enviadas pelo modelo.
*   **`MestreIATools.kt`**
    *   **Descrição:** A "Caixa de Ferramentas". Define as funções (Function Calling) que a IA pode executar: `consultar_grafo_regras` para pesquisa, `fill_character_sheet` para criação e o vital `consultar_nexus_arcano` para cálculo de pré-requisitos de magias.

---

## 💾 3. O "Arquivo Morto" (Dados e Persistência)

Arquivos que lidam com a gravação e leitura de dados no dispositivo.

*   **`MestreIARepository.kt`**
    *   **Descrição:** O Almoxarife. Centraliza o acesso aos dados da IA. Ele sabe onde buscar os recortes de manuais e como ler os nós do grafo, servindo essas informações para o motor de busca.
*   **`DataRepository.kt`**
    *   **Descrição:** A Interface de Dados. Define o contrato de tudo que pode ser buscado no app (fichas, itens, magias, regras).
*   **`ChatHistoryDao.kt`**
    *   **Descrição:** O Historiador. Gerencia o banco de dados Room para salvar e recuperar o histórico das conversas.
*   **`FichaDatabase.kt`**
    *   **Descrição:** O Coração de Dados (SSOT). É a fonte única de verdade do aplicativo, gerenciando o banco de dados SQLite (Room). Suas funções vão muito além da IA:
        *   **Gestão de Persistência:** Unifica Fichas, Histórico de Mensagens e o Manual de Regras.
        *   **Lógica de Purificação (Lote 110/111):** Possui um motor inteligente que limpa e reconstrói as tabelas técnicas do Códex para corrigir erros de codificação (Mojibake) sem afetar os dados salvos pelos usuários.
        *   **Orquestração de DAOs:** Centraliza o acesso aos quatro pilares de dados: Personagem, Recortes (Manual), Grafo de Regras e Sessões de Chat.

---

## 📚 4. A "Enciclopédia" (Assets de Conhecimento)

As fontes de verdade que alimentam o sistema com o conhecimento oficial de GURPS.

*   **`graph_knowledge.json`**
    *   **Descrição:** O Códex (Grafo). Contém as regras resumidas e "mastigadas". É a primeira fonte consultada para evitar alucinações.
*   **`chunks.jsonl`**
    *   **Descrição:** A Biblioteca (Recortes). Contém milhares de parágrafos extraídos diretamente dos manuais de GURPS. Fornece a prova documental e os detalhes completos.

---

## 📦 5. A "Estrutura" (Modelos e Entidades)

Define o formato físico de como os dados são tratados no código.

*   **`ChatHistoryEntity.kt`**: Modelo da tabela de histórico no banco.
*   **`MestreIAChunk.kt`**: Modelo que representa um recorte de texto do manual.
*   **`GraphNodeEntity.kt`**: Modelo que representa um nó de regra no grafo.
*   **`CatalogFilters.kt`**: Ferramenta de higienização de texto. Remove acentos, lida com caracteres corrompidos (Mojibake) e padroniza espaços para garantir que a busca no banco de dados seja agnóstica a formatação.

---
> [!NOTE]
> Esta arquitetura foi desenhada para ser um sistema de **RAG Híbrido**, priorizando o Grafo (conhecimento estruturado) sobre os Chunks (texto bruto) para garantir a máxima fidelidade às regras do GURPS 4ª Edição.
