# Acompanhamento do Projeto da Ficha GURPS (Para Rodolfo)

**Última Atualização:** 20 de Abril de 2026
**Status Atual:** ERA DO CONHECIMENTO - Motor GraphRAG Lite (Zero-Native) Ativado 🧠🕸️

### Sincro V23: Mapeamento Arquitetural e Blindagem de Conhecimento
- **Mapa Detalhado de Engenharia**: Criado o `MAPA_DETALHADO.md` com o inventário completo de funções, motores de RPG, scripts e suítes de teste. 🗺️🔍
- **Estabilização de Arquitetura**: Verificação e confirmação da integridade do código pós-sincronia, garantindo paridade total entre o ambiente de desenvolvimento e o dispositivo funcional do usuário. 🛡️✨
- **Nexus Arcano & Qualidade**: Mapeamento explícito das ferramentas de auditoria e testes automatizados para prevenir regressões em futuras intervenções de IA. 🧪🎯

### Sincro V22: Blindagem de Dados (V1.4.5 - Final)
- **Trava de Auto-Save**: Implementada barreira de proteção que bloqueia o salvamento automático durante o carregamento de fichas, prevenindo a sobrescrita acidental de arquivos com dados vazios. 
- **Feedback de Carga**: Sistema de Notificação atualizado para reportar erros reais de desserialização, eliminando falsos positivos de "Ficha Carregada". 
- **Rebuild V22**: APKs reconstruídos com logs de diagnóstico para rastrear falhas remanescentes em fichas corrompidas. 

Acabamos de implementar **As Novas Regras para Agentes Virtuais (IAs)**.
Eu, como Inteligência Artificial, deixei instruções de ouro em uma pasta especial chama `.agent/skills/` para que **qualquer outra IA que trabalhar com você no futuro saiba o que fazer e como te tratar:**

1. **Falar Simples:** Qualquer IA tem a obrigação de falar com você em um português normal. Nada de termos técnicos complicados. Se for preciso explicar o que foi feito, que seja em linguagem do dia a dia.
2. **Entender de Abas:** O agente novo vai ler o mapa `README_AGENTE.md` logo de cara, sabendo que sua ficha é dividida em Geral, Traços, Perícias, Magias, Equipamentos, Defesas e Rolagem, sem você precisar repetir tudo.
3. **Sempre Testar (Construção do App):** Proibimos qualquer IA de dizer que "terminou" o trabalho sem antes rodar um teste do sistema (um comando chamado `./gradlew build`), que garante que o aplicativo vai abrir no seu emulador sem travar.

---

## Lembretes Fixos do Seu Projeto

### 1. Ferramentas Acessíveis
Nós sempre cuidamos para que toda versão lançada tenha a versão **Visual** (Normal) e a versão **PraCego** (Com botão e navegação de acessibilidade para cegas por meio do programa TalkBack de celular). O emulador costuma focar na visual para seu teste rápido.

### 2. Consistência de Dados
Os dados das magias, vantagens e perícias moram em arquivos de texto (tipo planilhas, chamados de arquivos **JSON**). IAs que forem alterar algo lá não podem apagar aspas ou colchetes sem cuidado.

---

##  Registro de Lotes e Commits (Rede de Segurança)
*Todo Agente é obrigado a quebrar tarefas maiores em "Lotes Curtos" isolados de um arquivo por vez, efetuando o Commit no final para gerar um Ponto de Retorno seguro para o usuário. Cada nova "Aba" ganha também sua própria pasta.*

> Lista de Lotes Realizados a partir de Abril de 2026:

* [Feito] Lote 1.2: Extração de Loaders Json (DataRepository)       | `(Commit: 7c52e26)`
* [Feito] Lote 2.1: Separação de Peças do Nexus (Modelos)           | `(Commit: a6992f1)`
* [Feito] Lote 2.2: O Cérebro A* (Planejador de Caminho)            | `(Commit: 113b540)`
* [Feito] Lote 2.3: O Motor de Diagnóstico (Raio-X)                 | `(Commit: 105949a)`
* [Feito] Lote 2.4: Limpeza final (Helpers & Parser)                | `(Commit: aab9ff2)`
* [Feito] Lote 3: Modularização do FichaViewModel                   | `(Commit: 20414fd)`
* [Feito] Lote 4: Padronização UTF-8 e Motor Modo Alvo              | `(Commit: 0912746)`
* [Feito] Lote 6: Modularização do TraitDialogs                     | `(Commit: 3d186e9)`
* [Feito] Lote 6.1: Correção de Seleção e Regras Especiais          | `(Commit: 0e9aee5)`
* [Feito] Lote 7: Refatoração da UI de Rolagem (TabRolagem.kt)      | `(Commit: 042cd2d)`
* [Feito] Lote 7.1: Correções de Discord, UI de Ataque e Mojibake   | `(Commit: 6c3f773)`
* [Feito] Lote 7.2: Refatoração do RolagemDialogs.kt (Fragmentação) | `(Commit: 6c8a800c)`
* [Feito] Lote 8: Atualização Estética dos Ícones das Abas          | `(Commit: 1cec435)`
* [Feito] Lote 9: Interface de Navegação RPGística (Ultra-Premium)  | `(Commit: 9f13f43)`
* [Feito] Lote 10: Melhoria do Ataque Inato e Skill Project Map     | `(Commit: e1c1a1b)`
* [Feito] Lote 11: Modernização da UI do Mestre IA (ChatGPT)        | `(Commit: e42cce6)`
* [Feito] Lote 12: Card de dano adaptativo e soma automática de ST. | `(Commit: 783c295)`
* [Feito] Lote 13: Fluxo de Mestre IA com confirmação e análise consultiva. | `(Commit: a104632)`
* [Feito] Lote 14: Mestre IA Interativo (Antigravity-style) com botões de ação e tom inquisitivo. | `(Commit: a104632)`
* [Feito] Lote 15: Restauração das Defesas Ativas na Aba de Rolagem | `(Commit: 5c7c369)`
* [Feito] Lote 16: Modularização da FichaViewModel e Protótipo de Sugestões Clicáveis (Mestre IA) | `(Commit: c279b87)`
* [Feito] Lote 17: Automação da vantagem Golpeadores (Strikers) | `(Commit: 65ec4ef)`
* [Feito] Lote 18: Inteligência de NH e Aparar para Golpeadores/Ataque Inato | `(Commit: 687b8d9)`
* [Feito] Lote 19: Arquitetura Modular de Vantagens e Documentação de IA | `(Commit: 6132f9b)`
* [Feito] Lote 20: Automação da Vantagem Dentes (GdP-1 e Tipos) | `(Commit: 5fd613d)`
* [Feito] Lote 21: Persistência de Seleção de Ataque/Dano na Sessão | `(Commit: 217eaf4)`
* [Feito] Lote 22: Ajuste Vantagem Dentes e Nomeclatura de Dano PT-BR | `(Commit: ada9efd)`
* [Feito] Lote 23: Limpeza de Unicode Mojibake (\uXXXX) no Código Fonte | `(Commit: 33c771f)`
* [Feito] Lote 24: Automação de Garras, Cascos e Flexibilidade (+3 Perícias) | `(Commit: d4a5e2f)`
* [Feito] Lote 25: Bloqueio e Esquiva Ampliada e Estabilização de Build        | `(Commit: f202c06)`

* [Feito] Lote 27: Redesign Visual das Defesas (Cards Individuais e Botões)    | `(Commit: a9b2c3d)`
* [Feito] Lote 28: Refatoração da TabRolagem.kt (Extração de Diálogos)         | `(Commit: de16e6f)`
* [Feito] Lote 29: Otimização de Espaço e Padding (Botões 2dp)             | `(Commit: b3f2d1e)`
* [Feito] Lote 30: Integrar BD do Escudo na Esquiva/Apara + Notas de UI      | `(Commit: 249874d)`
* [Feito] Lote 31: Automação da Vantagem Mestre de Armas (Dano NH vs DX)      | `(Commit: c5a16f0)`
* [Feito] Lote 32: Categorização e Filtragem Estrita do Mestre de Armas     | `(Commit: e8f9a2c)`
* [Feito] Lote 33: Mapeamento Estrito e Correção de Vazamento (Mestre de Armas) | `(Manual: Antigravity)`
* [Feito] Lote 34: Restauração da Apara/Bloqueio e IDs de Perícia Sublinhados | `(Commit: f3c3e9a)`
* [Feito] Lote 35: Visibilidade Permanente de Defesas (Fallback Automático) | `(Commit: 2d1948b)`
* [Feito] Lote 36: Vínculo Estrito do Dano com o Ataque Selecionado (Final) | `(Commit: 194f5d1)`
* [Feito] Lote 37: Acessibilidade Ultra na Aba de Rolagem (TalkBack) | `(Commit: 51dbd9f)`
* [Feito] Lote 38: Atualização do Mapa do Projeto (100% Precisão) | `(Commit: 53b1f89)`
* [Feito] Lote 39: Atualização da Skill de Vantagens (TraitRule API) | `(Commit: d5768dd)`
* [Feito] Lote 40: Atualização das Regras Operacionais (Commits e PraCego) | `(Commit: 4f63b8a)`
* [Feito] Lote 41: Atualização das Regras de RPG GURPS (Combat Context) | `(Commit: d26daad)`
* [Feito] Lote 45: Restauracao do Aro Vermelho e Filtro de Invisibilidade (Sincro V16) | `(Commit: f604490)`
* [Feito] Lote 46: Pente Fino de Acessibilidade (TalkBack) em Pre-requisitos (Sincro V17) | `(Commit: bab709c)`
* [Feito] Lote 47: Sincronização em Nuvem Invisível (Railway + DeviceID) (Sincro V18) | `(Manual: Antigravity)`
* [Feito] Lote 48: Biblioteca Unificada e Proteção contra Conflitos de Nomes (Sincro V19) | `(Manual: Antigravity)`
* [Feito] Lote 49: Restauração Sistêmica e Unificação de Branches (Integridade Total) | `(Commit: 7b346b1)`
* [Feito] Lote 50: Motor Nexus Arcano Estabilizado (Resolução Desejo + Metas Incrementais) | `(Commit: a2e2820)`
* [Feito] Lote 51: Mestre IA PRIME  - Soberania Multi-Flavor (Gemini/DeepSeek) + Segurança Sete Chaves (Vault) + Rastreabilidade Técnica | `(Commit: babc20a)`
* [Feito] Sincronia de Roadmap: Roadmap do Mestre IA atualizado com novos lotes | `(Commit: d63158d)`
* [Feito] Lote 52: Robustez no Parsing de JSON (Auto-Healing) | `(Commit: f59f90b)`
(Problema: O uso de Regex para capturar JSON no MestreIAClient é eficiente, mas frágil se a IA enviar um JSON malformado ou truncado.
Melhoria: Implementar um "JSON Repair" (como uma limpeza agressiva de caracteres de controle) e validar a estrutura contra o MestreIAResponse usando KotlinX.Serialization antes de chegar ao UseCase. Se o JSON falhar, o sistema deve pedir automaticamente uma re-formatação para a IA (Auto-Healing).)
* [Feito] Lote 53: Contexto Diferencial (Token Economy) | `(Commit: fbdb0da)`
.(Problema: Enviar o personagem inteiro em cada mensagem gasta muitos tokens e pode confundir a IA com dados irrelevantes.
Melhoria: No processarPrompt, implementar uma lógica que identifique o que mudou na ficha ou o que é relevante para a pergunta atual. Se o usuário pergunta sobre "Dano", não precisamos enviar a lista de "Equipamento de Camping".)
* [Feito] Lote 54: Streaming de Resposta (SSE/UX) | `(Commit: 89ab389)`
(Problema: Esperar a resposta completa da API pode gerar um "atraso" perceptível na UI (Latência).
Melhoria: Se as APIs (OpenRouter/Gemini) suportarem, implementar Server-Sent Events (SSE). Ver o Mestre IA "escrevendo" em tempo real melhora drasticamente a percepção de performance.)
* [Feito] Lote 55: Auditoria de Regras (Fiscal Ativo) | `(Commit: fc1f47c)`
* [Feito] Lote 56: Local-First RAG (Busca Vetorial) | `(Commit: c110272)`.(Problema: O buscador semântico no MestreIARagEngine pode ser pesado para buscas em muitos arquivos JSON.
Melhoria: Avaliar o uso de uma pequena biblioteca de busca vetorial local (ou um index pré-calculado) para que o findRelevantChunks seja instantâneo, mesmo com manuais extensos.)
* [Feito] Lote 57: Reconstrução de Elite (RAG 1194 Chunks) e Triplo Fallback (600 Usos) | `(Manual: Antigravity)`
* [Feito] Lote 58: Estabilização do Mestre IA (Motor de Reparo por Pilha) | `(Commit: bf9b73c)`
    - [x] **Estabilização IA 2026**: Migração total para Gemini 3.0 e 2.5 (resolvendo 404 de modelos antigos).
    - [x] **Restauração Nexus**: Motor de investigação multi-estágio e trilhas de pré-requisitos universalizado.
    - [x] **Blindagem de Conexão**: Correção de headers OpenRouter e normalização de histórico de chat.
    * Implementação de algoritmo de fechamento de JSON baseado em Pilha (Stack) e Botões de Diagnóstico UI.
* [Feito] Lote 59: IA Master Laboratory (Suite de Auditoria Python) | `(Commit: bf9b73c)`
    * Criação do validador de fidelidade ao catálogo e simulador de stress offline em Python.
* [Feito] Lote 60: Stress Test do Motor de Pilha e Sync do Gabarito de Ouro | `(Manual: Antigravity)`



    * Refatoração do `MestreIAResponse` para aceitar Objetos em vez de Strings para Vantagens e Magias (alinhado com o Gabarito de Ouro).
    * Evolução do `repararJsonTruncado` para fechar Strings `"` abertas e ignorar sufixos nocivos.
    * Teste Unitário criado: `testStressReparoJsonTruncado` com aninhamento de 500pts aprovado.

### Lote 60: Definição de Schemas Nativos (Tool Builder)
*   **Commit:** `d025cc1 feat(ia): Lote 60 - define schemas nativos de Function Calling para Gemini e OpenAI`
*   **Melhorias Implementadas:**
    *   Criação da classe `MestreIATools.kt` para orquestrar as ferramentas.
    *   Definição rigorosa de Schemas JSON (`fill_character_sheet` e `search_rules`) que forçam a IA a obedecer o layout da ficha.
    *   Fim da Engenharia de Prompt (Regex) para requisição de criação de fichas.

### Lote 61: O Motor ReAct (Orquestrador Assíncrono)
*   **Commit:** `a353efe feat(ia): Lote 61 - Orquestrador ReAct intercepta Tool Calls para preenchimento de ficha sem regex`
*   **Commit:** `161fb58 feat(ia): implementa ReAct loop real no MestreIAUseCase para pesquisa de regras e ativa criacao em todos os modos`
*   **Melhorias Implementadas:**
    *   Refatoração profunda em `MestreIAClient` e `MestreIAUseCase` para capturar chamadas assíncronas no protocolo SSE.
    *   Interceptação da ferramenta nativa `fill_character_sheet`.
    *   Passagem direta do JSON estrito e perfeito para a `FichaIADelegate`, eliminando completamente os riscos de JSON malformado e ativando instantaneamente o botão de Integração.
    *   **Loop ReAct Verdadeiro:** Implementada a recursão nativa. Se a IA solicitar `search_rules`, o sistema intercepta, busca as regras via RAG, e faz uma chamada recursiva devolvendo o texto para a IA automaticamente para que ela conclua sua tarefa.
    *   Desbloqueio de Ferramentas: `fill_character_sheet` agora está sempre disponível, mesmo no modo Padrão/Conversa, permitindo que a IA forje fichas em qualquer cenário.

### Fix Estrutural do RAG (Cérebro Local de Busca)
*   **Commit:** `6d1d531 fix(rag): implementa busca multi-camada (exata + flexivel) no SQLite FTS4 para tolerância a erros e maior precisao`
*   **Melhorias Implementadas:**
    *   Substituição da busca engessada FTS4 (`AND` total) por uma "Busca em Cascata" inteligente.
    *   Tolerância a Erros: Se o usuário digitar "descricoa", a busca flexível garante o Ranqueamento através de outras palavras corretas ("Perícia", "Furtividade"), evitando a devolução de resultados vazios e mitigando as temidas "alucinações da IA".
* [Feito] Lote 62: Configuração do Ambiente GraphRAG (D:\VSBuildTools) | `(Manual: Antigravity)`
    - Instalação das ferramentas C++ Build Tools no drive D: para suporte a compilação nativa.
* [Feito] Lote 63: Pivot Técnico para GraphRAG Lite (Zero-Native) | `(Commit: 8f45a91)`
    - Implementação de motor baseado em **ChromaDB + NetworkX** para evitar erros de DLL e compilador no Windows.
* [Feito] Lote 64: Integração Kotlin e DB Versão 12 (GraphRAG) | `(Commit: 8f45a91)`
    - Migração do Banco de Dados SQLite para suporte à tabela `graph_nodes` e busca híbrida.
* [Feito] Lote 65: Estabilização de APIs e Chaves (OpenRouter/DeepSeek) | `(Commit: 8f45a91)`
    - Remoção de sufixos :free obsoletos e rotação de credenciais ativas.
* [Feito] Lote 66: Auditoria de Dados e Injeção de Regras Mestre | `(Commit: 92b4cdd)`
    - Limpeza de 284 duplicatas no manual (Magia).
    - Padronização de referências: `[MB]`, `[Mág]`, `[AM]`.
    - Injeção de Tabelas Fundamentais (Distância, MT, Escuridão, Cobertura) no Grafo.
* [Feito] Lote 67: UX de Elite - Botão de Copiar e Histórico Persistente | `(Commit: pendente)`
    - Implementação do LocalClipboardManager para cópia rápida de respostas.
    - Criação de tabelas `chat_sessions` e `chat_messages` no SQLite (V13).
    - Interface de seletor de histórico para recuperação de conversas passadas.
* [Feito] Lote 68: Motor Investigador (Expansão Semântica) | `(Commit: c2e2491)`
* [Feito] Lote 69: O Toque do Mestre (Contexto Adjacente) | `(Commit: ca18212)`
* [Feito] Lote 70: Motor ReAct Multi-Stage (O Mestre Investigador) | `(Commit: pendente)`


    - Implementação de Loop de Investigação (While) em `MestreIAUseCase.kt`.
    - Suporte a até 3 iterações consecutivas de busca por resposta.
    - Acumulação inteligente de contexto entre buscas para evitar perda de raciocínio.
* [Feito] Lote 81: Restauração Nano-GraphRAG e Memória de Agente | `(Commit: 51c0d3b)`
    - Implementação de Busca Relacional em Dois Saltos (Multi-Hop) no `MestreIAGraphEngine.kt`.
    - Integração com o sistema MemPalace (ChromaDB local) para memória de longo prazo do agente.
    - Criação do "Palácio da Memória" (Knowledge Item) com mapa detalhado de engenharia e regras SSOT.
    - Expansão do contexto RAG com "Essential Nodes" (Atributos e Regras Base) e Radar de RAM.
    - Rotação de Chaves: Mapeamento de `MESTRE_IA_OPENROUTER_2_KEY` e `.3_KEY` no `MestreIAUseCase.kt` para garantir continuidade após expiração da chave anterior.

* [Feito] Lote 82: Refatoração Mestre IA (Especialistas) | `(Commit: pendente)`
    - Divisão do UseCase em Auditor (Regras/RAG) e Forjador (Geração/Análise).
    - Refatoração do `MestreIAResponse` para aceitar objetos complexos em Vantagens/Desvantagens/Magias (Correção Crash GSON).
    - Implementação de fallbacks especializados (Elite para Forja, Lite para Auditoria).
    - Limpeza de sintaxe e remoção de redundâncias no `FichaIADelegate`.



### Próximos Passos (Desejos do Usuário):
- Bateria de Testes (Stress Test)
Impacto em Alta Velocidade: "Um cavaleiro em carga a cavalo (Move 8) atinge um soldado com uma lança. Como calculo o dano de colisão baseado na ST do cavalo?"
Regras de Afogamento: "Meu personagem caiu em um rio e está sem fôlego. Quanto tempo ele aguenta antes de começar a perder PV e quais são os testes de HT?"
Visibilidade Crítica: "Estou tentando atirar em um alvo na escuridão total, mas tenho 'Visão Noturna 5'. Qual a minha penalidade final?"
Equipamentos e Carga: "Estou carregando 40kg de ouro. Minha ST é 10. Como isso afeta minha Esquiva e meu Deslocamento atual?"
Aparar com Escudo: "Um ogro me atacou com uma clava gigante. Posso usar a regra de 'Aparar com o Escudo' ou sou obrigado a Bloquear?"
Criação de Especialista: "Gere uma ficha de um Ninja especializado em infiltração tecnológica (NT 9), com 'Mãos Pegajosas' e 'Passo Leve', usando 150 pontos."
Regra de Recuo (Armas de Fogo): "Se eu der uma rajada de 3 tiros com uma submetralhadora de Recuo 2, como calculo quantos tiros acertaram?"
Sufocamento por Fumaça: "O prédio está pegando fogo! Quais são os testes para não desmaiar por inalação de fumaça a cada turno?"
Salto em Distância: "Tenho ST 12 e DX 11 e HT 10. Qual a distância máxima que consigo saltar com uma corrida prévia?"
Magia e Fadiga: "Se eu conjurar 'Bola de Fogo' gastando 3 pontos de fadiga, mas minha Aptidão Mágica for 2, quanto tempo levo para carregar a magia?"

"Segundo Cérebro" (O Validador de Regras).(vamos pensar niisso!)