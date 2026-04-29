# 🗺️ Arquitetura do Mestre IA - GURPS Ficha Android

Este documento descreve todos os arquivos que compõem o ecossistema da Inteligência Artificial do aplicativo, divididos por sua função técnica e responsabilidade.

---

## 🧠 1. O "Cérebro" (Lógica e Orquestração)

Estes arquivos gerenciam o fluxo de pensamento da IA, desde o recebimento da pergunta até a montagem da resposta final.

*   **`MestreIAUseCase.kt`**
    *   **Descrição:** O "Maestro" do sistema. Ele coordena a execução de todas as tarefas: recebe a pergunta do usuário, aciona o motor de busca (GraphEngine), busca o histórico de conversas e formata os dados para enviar ao cliente de rede. É a ponte entre a Interface (UI) e a lógica de IA.
*   **`MestreIAGraphEngine.kt`**
    *   **Descrição:** O Motor Nexus (Busca Híbrida). Ele é responsável pelo "GraphRAG". Primeiro, busca no grafo de conhecimento (`graph_knowledge.json`) e, se encontrar referências a páginas, faz uma "Ponte de Página" para buscar o texto bruto nos `chunks.jsonl`. Ele garante que a IA tenha o contexto correto do livro antes de responder.
*   **`MestreIAPrompts.kt`**
    *   **Descrição:** O "Código de Conduta". Contém as diretrizes de personalidade e regras de RPG para os dois modos da IA: o **FORJADOR** (criação e análise de fichas) e o **AUDITOR** (tira-dúvidas de regras oficiais). Define como a IA deve se comportar e falar.

---

## 🌐 2. A "Boca" (Comunicação e Rede)

Responsáveis por levar a pergunta até os servidores (Gemini/OpenRouter) e trazer a resposta.

*   **`MestreIAClient.kt`**
    *   **Descrição:** O Mensageiro. Utiliza `HttpURLConnection` para fazer as chamadas de rede. Gerencia os timeouts (90s), a autenticação via API Key e a montagem do JSON final que vai para o servidor. É aqui que o "Alinhamento Python" é mantido para garantir paridade com o motor de regras original.
*   **`MestreIAResponse.kt`**
    *   **Descrição:** O Tradutor de Resposta. Define as classes de dados que mapeiam exatamente o que a IA devolve (texto, tokens usados, latência e chamadas de ferramentas).
*   **`MestreIATools.kt`**
    *   **Descrição:** A "Caixa de Ferramentas". Define as funções (Function Calling) que a IA pode executar, como "consultar ficha do personagem" ou "verificar inventário".

---

## 💾 3. O "Arquivo Morto" (Dados e Persistência)

Arquivos que lidam com a gravação e leitura de dados no dispositivo.

*   **`MestreIARepository.kt`**
    *   **Descrição:** O Almoxarife. Centraliza o acesso aos dados da IA. Ele sabe onde buscar os recortes de manuais e como ler os nós do grafo, servindo essas informações para o motor de busca.
*   **`DataRepository.kt`**
    *   **Descrição:** A Interface de Dados. Define o contrato de tudo que pode ser buscado no app (fichas, itens, magias, regras). É o ponto central de consulta de qualquer dado.
*   **`ChatHistoryDao.kt`**
    *   **Descrição:** O Historiador. Gerencia o banco de dados Room para salvar e recuperar o histórico das conversas. É o que permite que a IA "se lembre" do que vocês falaram 5 minutos atrás.
*   **`FichaDao.kt`**
    *   **Descrição:** O Guardião da Ficha. Permite que a IA acesse os dados atuais do seu personagem (PV, ST, Perícias) para dar respostas personalizadas à sua situação no jogo.
*   **`AppDatabase.kt`**
    *   **Descrição:** O Coração do Banco. Configura e inicializa todas as tabelas do banco de dados Room que a IA utiliza.

---

## 📚 4. A "Enciclopédia" (Assets de Conhecimento)

As fontes de verdade que alimentam o sistema com o conhecimento oficial de GURPS.

*   **`graph_knowledge.json`**
    *   **Descrição:** O Códex (Grafo). Contém as regras resumidas e "mastigadas" (como a regra de Combate Subaquático que acabamos de ajustar). É a primeira fonte consultada para evitar alucinações.
*   **`chunks.jsonl`**
    *   **Descrição:** A Biblioteca (Recortes). Contém milhares de parágrafos extraídos diretamente dos manuais de GURPS. Fornece a prova documental e os detalhes completos das regras.

---

## 📦 5. A "Estrutura" (Modelos e Entidades)

Define o formato físico de como os dados são tratados no código.

*   **`ChatHistoryEntity.kt`**: Modelo da tabela de histórico no banco.
*   **`MestreIAChunk.kt`**: Modelo que representa um recorte de texto do manual.
*   **`GraphNodeEntity.kt`**: Modelo que representa um nó de regra no grafo.
*   **`CatalogFilters.kt`**: Ferramenta de normalização de texto (remove acentos e lida com erros de digitação na busca).

---
> [!NOTE]
> Esta arquitetura foi desenhada para ser um sistema de **RAG Híbrido**, priorizando o Grafo (conhecimento estruturado) sobre os Chunks (texto bruto) para garantir a máxima fidelidade às regras do GURPS 4ª Edição.
