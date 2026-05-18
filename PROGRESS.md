# Acompanhamento do Projeto da Ficha GURPS (Para Rodolfo)

**Última Atualização:** 17 de Maio de 2026
**Status Atual:** ERA DO RACIOCÍNIO - Lote 133 CONCLUÍDO


### Sincro V24: Super Release 2.0 (Lote 86)
- **Lançamento Oficial V1.5.0**: Build de produção gerada para as variantes Visual e PraCego.
- **Unificação de Traços e Busca Inteligente**: Finalização do Lote 86 com todas as melhorias de interface e blindagem de cálculo integradas.
- **Preparação de Update**: Arquivo `update.json` atualizado para notificar os usuários sobre a nova versão. Ã°Å¸â€â€Ã°Å¸â€ºÂ¡Ã¯Â¸ï¿½

### Sincro V23: Mapeamento Arquitetural e Blindagem de Conhecimento
- **Mapa Detalhado de Engenharia**: Criado o `MAPA_DETALHADO.md` com o inventário completo de funções, motores de RPG, scripts e suítes de teste. Ã°Å¸â€”ÂºÃ¯Â¸ï¿½Ã°Å¸â€ï¿½
- **Estabilização de Arquitetura**: Verificação e confirmação da integridade do código pós-sincronia, garantindo paridade total entre o ambiente de desenvolvimento e o dispositivo funcional do usuário. Ã°Å¸â€ºÂ¡Ã¯Â¸ï¿½Ã¢Å“Â¨
- **Nexus Arcano & Qualidade**: Mapeamento explícito das ferramentas de auditoria e testes automatizados para prevenir regressões em futuras intervenções de IA. Ã°Å¸Â§ÂªÃ°Å¸Å½Â¯

### Sincro V22: Blindagem de Dados (V1.4.5 - Final)
- **Trava de Auto-Save**: Implementada barreira de proteção que bloqueia o salvamento automático durante o carregamento de fichas, prevenindo a sobrescrita acidental de arquivos com dados vazios. 
- **Feedback de Carga**: Sistema de Notificação atualizado para reportar erros reais de desserialização, eliminando falsos positivos de "Ficha Carregada". 
- **Rebuild V22**: APKs reconstruídos com logs de diagnóstico para rastrear falhas remanescentes em fichas corrompidas. 

### Lote 101: O Retorno ao Códex (Precisão Literal) - CONCLUÍDO
- **Motor Determinístico**: Inversão de prioridade (Manual-First) com bônus de 100 pontos para termos literais. 🎯
- **Fim do Chute**: Implementada a "Prisão de Contexto" no Auditor. Se não está no livro, a IA não inventa. ðŸ›¡ï¸�
- **Sincro Automática**: Sincronização de manuais agora ocorre sozinha ao abrir o Mestre IA, garantindo paridade total. âš™ï¸�
- **Correção Mojibake**: Limpeza de símbolos técnicos (m³, ×) no banco de dados. 🧹
- **Bônus de Tabela**: Motor calibrado para priorizar tabelas técnicas sobre descrições de vantagens. 📊

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

* [Feito] Lote 82: Refatoração Mestre IA (Especialistas) | `(Commit: 86d7e4f)`
    - Divisão do UseCase em Auditor (Regras/RAG) e Forjador (Geração/Análise).
    - Refatoração do `MestreIAResponse` para aceitar objetos complexos em Vantagens/Desvantagens/Magias (Correção Crash GSON).
    - Implementação de fallbacks especializados (Elite para Forja, Lite para Auditoria).
    - Limpeza de sintaxe e remoção de redundâncias no `FichaIADelegate`.

* [Feito] Lote 83: Blindagem Ultra-Resiliente (Zero-Crash JSON) | `(Commit: f93da21)`
    - Implementação do `MestreIAItemDeserializer` com suporte a aliases (`nh`, `desc`, `id`).
    - Tratamento de exceções interno no parse para evitar interrupção do fluxo do app.
    - Sincronização de motores GSON entre `Client`, `Delegate` e `Generator`.

* [Feito] Lote 84.5: Cérebro Mestre (Regras de Ouro e Injeção de Catálogo Técnico) | `(Commit: Lote84.5)`
    - IA agora recebe lista oficial de nomes de Armas e Equipamentos.
    - Regra estrita: Magias exigem Aptidão Mágica e inclusão de pré-requisitos.
* [Feito] Lote 84.6: Separação Arquitetural (Prompts Isolados) | `(Commit: Lote84.6)`
    - Prompts movidos para `MestreIAPrompts.kt`, limpando o cliente de rede.
* [Feito] Lote 84.7: Alinhamento Técnico (NT, Dano PT-BR e RAG Expansivo) | `(Commit: Lote84.7)`
    - Proibição de termos em inglês (cut/pi) e exigência de sufixos /NT.
    - Expansão automática do RAG para garantir catálogo técnico em personagens novos.
* [Feito] Lote 84.8: Consciência de App (IA entende a automação do sistema) | `(Commit: Lote84.8)`
    - IA agora entende que o app automatiza cálculos se os nomes estiverem corretos.

* [Feito] Lote 85: Mesa Virtual (Lote 1) - Console do Narrador e Automações | `(Commit: Lote85)`
    - Criação do Console Web (`index.html`) com suporte a visualização de múltiplas fichas JSON simultâneas.
    - Implementação de Calculadora de Dano Localizado com multiplicadores oficiais (Crânio x4, Vitais x3) e limites de membros (PV/2 e PV/3).
    - Adição de controles interativos de PV/PF e ajuste manual de bônus de RD por localização.
    - Automação de características derivadas na interface: PER, VON, Velocidade, Deslocamento e Dano de ST (GdP/GeB).
    - Preparação do App Android com `intent-filter` para o protocolo `gurpsapp://conectar`.

* [Feito] Lote 86: Unificação de Traços e Busca Inteligente (Modificadores) | `(Commit: 5ce593b)`
    - Implementação de barra de busca no diálogo de modificadores (catálogo geral + específicos).
    - Unificação das interfaces de Adição e Edição de Vantagens e Desvantagens (Ficha e Modelo Racial).
    - Correção da persistência de regras especiais (Aliados, Patronos, Dependência, Inimigos, etc.).
    - Blindagem de cálculo de custo para traços legados (fallback de `specialRule` via catálogo).
    - Ativação de salvamento automático (`salvarFicha()`) após qualquer edição de traços.

[Feito] Lote 87: Exibição de descrições de perícias/magias na Aba de Rolagem | `(Commit: c3e3859)`
* [Feito] Lote 88: Blindagem do Mestre IA (Rastro de Provas) | `(Manual: Antigravity)`
    - Implementação do protocolo de citação obrigatória `[Livro, Pág. X]`.
    - Bloqueio de memória externa para evitar alucinações de regras não documentadas no Códex.
    - Exigência de uso de ferramentas de busca para dúvidas técnicas.
    - Análise profunda do sistema e identificação de 5 vulnerabilidades críticas de alucinação.
* [Feito] Lote 89: Higienização de Ativos (Assets Cleanup) | `(Manual: Antigravity)`
    - Remoção de banco de dados residual `chroma.sqlite3` (Legado Lote 63).
    - Exclusão de `catalogo_nomes_ia.json` obsoleto (substituído pelo GraphRAG dinâmico).
    - Faxina de arquivos JSON legados (`vantagens.v1`, `v2`, `magias.json`, etc.) para reduzir tamanho do APK.
    - Realocação de scripts de pré-processamento (`populate_graph.py`) para fora da pasta de assets do App.

* [Feito] Lote 98: Paginação de Resultados (Página 2) e Calibragem de Precisão (Peso de Ouro) | `(Commit: Lote98)`
    - Implementação de parâmetro `pagina` na ferramenta de busca para evitar loops infinitos.
    - Sistema de Pesos: Termos originais da pergunta valem +10, sinônimos automáticos valem +2.
    - Bônus Massivo de Título (+35) para match exato com a dúvida do usuário.
    - Fim do problema de "Sangramento" ser enterrado por magias de cura ou outros termos genéricos.
* [Feito] Lote 100: Consciência Bibliográfica (Source-Aware RAG) e Dicionário Técnico | `(Commit: Lote100)`
    - Implementação de Busca Filtrada: O sistema agora distingue entre livros diferentes que possuem o mesmo número de página, eliminando colisões (ex: Pág 117 de Magia vs Artes Marciais).
    - Dicionário Técnico Mestre: Injeção de sinônimos de alta fidelidade (ex: "Cavar" remete automaticamente a "Escavação") para garantir que o motor de busca encontre a regra correta mesmo com linguagem comum.
    - Regex de Precisão: Captura automática da fonte bibliográfica `[Livro]` a partir dos resumos do grafo para direcionar a carga de recortes manuais.
* [Feito] Lote 101: Motor RAG Semântico Híbrido & Anti-Diluição (FTS4 Layering) | `(Commit: Lote101)`
    - Desativação de "blindagem" (stopWords genéricas de RPG como 'ataque' e 'dano'), devolvendo a capacidade do motor de interpretar frases naturais cruas sem falhas.
    - Implementação de Busca por Camadas (Layering): Garantia matemática de que palavras raras (ex: 'piscina') não sejam engolidas do limite do FTS4 por palavras comuns (ex: 'dano') através de iterações individuais na base de recortes.
* [Feito] Lote 102: Algoritmo de Raridade (TF-IDF Proxy local em Kotlin) | `(Commit: Lote102)`
    - Implementação de heurística inspirada no TF-IDF (Inverse Document Frequency) diretamente no re-ranking local.
    - Cálculo de peso por raridade: Palavras com muitos resultados no SQLite (peso 1) não pontuam alto; palavras com poucos recortes exatos (peso até 50) geram multiplicadores explosivos (ex: 'Combate Aquático' supera 100% 'Dano de Arma').
    - O motor agora filtra o ruído de perguntas longas através de matemática pura, sem depender de injeções rígidas.
* [Feito] Lote 103: RAG State-of-the-Art (RRF & Parent Document) | `(Commits: 6a059ea, a43a88e)`
    - **Parent Document Retrieval:** O motor agora busca a página inteira em que o recorte se encontra, resolvendo perdas de contexto onde regras importantes ou tabelas continuavam no próximo parágrafo. O limite do prompt saltou para 15000 chars.
    - **RRF (Reciprocal Rank Fusion):** Implementada a fórmula matemática padrão da indústria `(1 / Rank + 60)` para fundir de forma justa o ranking de palavras-chave da busca textual (FTS) com as sugestões de página vindas do Knowledge Graph, gerando um Top 3 infalível.

* [Feito] Lote 104: Filtro de Ruído & Mega-Contexto (60k) | `(Commit: de125e6)`
    - **Filtro de Ruído:** Expansão de termos por sinônimos (gladiador, luta) agora afeta apenas o Grafo. A busca de texto bruto (Chunks) foca 100% nos termos reais do usuário para evitar poluição.
    - **Janela de 60k Chars:** Limite da `PonteDeFerro` expandido de 15k para 60k caracteres. Isso permite enviar até 10 páginas completas (Documento Pai) sem cortes.
    - **Top 8 Retrieval:** Motor agora coleta as 8 melhores páginas encontradas, garantindo que regras específicas entrem no prompt mesmo que não sejam o Top 1 de score.

* [Feito] Lote 105: Diversidade de Elite & Bônus de Autoridade | `(Commit: c8bcfe6)`
    - **Filtro Anti-Monopólio:** Implementada trava algorítmica que limita a 2 recortes por página no Top 8. Isso obriga o motor a trazer diversidade de regras (ex: Pág 16 + Pág 430 + Pág 397) em vez de inundar o contexto com uma única página genérica.
    - **Bônus de Grafo (5x):** Páginas sugeridas pelo Knowledge Graph agora recebem um multiplicador de relevância de 500%, garantindo que a inteligência estrutural prevaleça sobre a mera repetição de palavras-chave.

* [Feito] Lote 106: Contexto Adjacente (Página Suporte) | `(Manual: Antigravity)`
    - **Página n+1:** O motor `MestreIAGraphEngine` agora recupera automaticamente a página seguinte para cada página de impacto encontrada, garantindo integridade de tabelas e fórmulas longas.
    - **Expansão de Contexto:** Aumentado o limite de recortes finais de 10 para **20** para acomodar o suporte adjacente sem cortes.
    - **Validação de Colisão:** Confirmada a recuperação da fórmula de dano (Pág 432) ao buscar por termos na Pág 431.

* [Feito] Lote 107: Blindagem de FTS4 (Normalização de Acentos) | `(Manual: Antigravity)`
    - **Indexação Normalizada:** A coluna `search_text` agora é povoada sem acentos e em lowercase, blindando o motor contra encoding corrompido (Mojibake).
    - **Busca Agnóstica:** Os termos de busca são normalizados antes da consulta, permitindo que "colisao" encontre "Colisão" e vice-versa.
    - **Unificação de Scoring:** O re-ranking TF-IDF agora utiliza os mesmos termos normalizados, garantindo precisão matemática no Top 3.

* [Finalizado] Lote 108: Sincronia Automática e Limpeza de Legado | `(Commit: 2e59eba)`
    - **Remoção de Gatilhos Manuais:** Extintos os comandos "forçar sincronização" via chat.
    - **Sincronia Inteligente:** Implementado **Mutex** no `MestreIARepository` para garantir carga única e atômica.
    - **Performance & Background:** UseCase migrado para `Dispatchers.IO`, eliminando lag na UI.
    - **Encoding UTF-8 (Fim do Mojibake):** Forçada leitura de assets em UTF-8, corrigindo acentos corrompidos.
    - **v19 do Banco:** Incrementada versão do DB para forçar reset limpo dos índices.

* [Finalizado] Lote 109: Purificação Arquitetural | `(Commit: 2e59eba)`
    - **Isolamento de Repositório:** Criado o `MestreIARepository` para separar a lógica de regras da lógica de ficha (`DataRepository`).
    - **Delegação Limpa:** O `DataRepository` agora apenas delega as chamadas de busca, reduzindo seu tamanho e complexidade.
    - **Estabilidade de Testes:** Ajustados Stubs e inicialização `lazy` para permitir testes unitários sem dependência de Contexto.

* [Finalizado] Lote 111: Otimização de RAG e Cura de Contexto (Dano por Colisão) | `(Commit: ce531eb)`
    - **Diagnóstico de Carga:** Remoção de limitações de código que truncavam regras vitais.
    - **Prioridade VIP:** Bônus de score +1000 para páginas recomendadas pelo Grafo (Garante Pág 433).
    - **Abertura de Gargalo:** Entrega de até 25 recortes de contexto ao Gemini no CaseUse.
    - **Validação de Dados:** Verificação da integridade da regra de Colisão no banco SQLite.

* [Finalizado] Lote 112: Motor de Raciocínio e Hierarquia (Plan Systemic Evolution) | `(Commit: Lote112)`
    - **Janela Deslizante (N-1, N, N+1):** Recuperação automática da página anterior para integridade de regras.
    - **Hierarquia de Autoridade:** Bônus (+50) para o Módulo Básico, garantindo soberania da "Lei Mãe".
    - **Prompt de Raciocínio (Pilares):** IA agora decompõe problemas em Ação, Atributo, Ambiente e Estado.
    - **Purificação do Grafo:** Injeção de source_id em todos os 2476 nós e unificação de Nós Mestres (Ataque Total).

* [Finalizado] Lote 112.1: Correção do Gargalo de Regex no RAG | `(Commit: 6dc456c)`
    - **Diagnóstico:** O Grafo corretamente identificava múltiplas páginas (ex: [Pág. 353, 354, 388] para Terreno), mas a Regex capturava apenas a primeira.
    - **Correção Matemática:** Substituição de `Regex.find()` por `Regex.findAll()` no `MestreIAGraphEngine.kt`, iterando sobre todas as ocorrências de páginas no resumo.
    - **Resultado Prático:** A "Ponte de Página" agora enfileira todas as páginas listadas (353, 354 e 388), permitindo que a IA aplique a regra matemática de Lama no combate.

* [Finalizado] Lote 112.2: RRF Rank Normalization (Reciprocal Rank Fusion) | `(Commit: Pending)`
    - **Diagnóstico:** O Algoritmo de Ranking Lexical RRF penalizava as páginas extras do Grafo. Se o Grafo apontava 3 páginas, a segunda e terceira ganhavam pontuação decrescente, impedindo regras secundárias (Lama) de chegarem ao Top 8.
    - **Correção:** Alterado o `graphRank` para tratar **todas** as páginas apontadas pelo Grafo com pontuação absoluta (Rank = 1). A responsabilidade do desempate é agora puramente lexical.

* [Finalizado] Lote 112.3: Correção do Anti-Monopólio e Autenticidade de Fonte Bibliográfica | `(Commit: Pending)`
    - **Diagnóstico de Magias:** Consultas como "Magia Desejo" recuperavam o nó correto, mas a página (Pág 61) era carregada de 3 livros diferentes simultaneamente (Módulo Básico, Artes Marciais e Magia). O filtro "Anti-Monopólio" as considerava idênticas (Pág 61) e cortava o livro Magia (por ter prioridade menor no desempate), ocultando a regra real. A lista de *stop words* também estava bloqueando palavras técnicas vitais (ex: "pré", "requisitos").
    - **Refatoração da Ponte de Página (`PaginaAlvo`):** Implementada amarração com `sourceId` nos metadados da base. Agora, o sistema exige que a página 61 do Grafo corresponda exclusivamente à Pág 61 do suplemento correto (Magia).
    - **Bônus Lexical Especializado:** Adicionado Bônus +60 Lexical se os "Termos Base" ou o "Nó do Grafo" pertencerem ao grupo semântico de "Magia" (pt_gurps_magia) ou "Artes Marciais" (pt_artes_marciais), superando artificialmente o bônus de Autoridade do Módulo Básico para buscas ultra-especializadas.
    - **Stop Words:** Removidas palavras cruciais como "requisitos" e "pre" do limpador lexical no `extrairPalavrasChave`.

* [Feito] Lote 113: Integração Nexus-IA (O Consultor Arcano) | `(Commit: c4a1b2d)`
* [Feito] Lote 114: Blindagem de Conectividade & Contexto Arcano | `(Commit: 9cb6448)`
    - **Fix Conectividade:** Resolução do Erro 400 através da segregação de URLs e Chaves por provedor (Gemini vs DeepSeek).
    - **Enriquecimento Arcano:** Injeção automática de Escolas e Pré-requisitos no contexto de magias para o Mestre IA.
    - **Estabilização de Build:** Correção de inferência de tipos no ranking RRF do GraphEngine.
    - **Conexão de Motores:** Implementação da ferramenta nativa `consultar_nexus_arcano` no Mestre IA, permitindo que a IA invoque o Motor Nexus em milissegundos.
    - **Gabarito Técnico:** O motor agora gera um "Gabarito de Ouro" determinístico (Estado Zero) com árvore de dependências completa e sugestões para metas de escolas.
    - **Fidelidade Bibliográfica:** Correção de dados no `magias2versao.json` (Sopro de Fogo/Ãcido/Frio) alinhando "Resistência" com o Módulo Básico.
    - **Independência de Ficha:** A ferramenta funciona de forma isolada, permitindo planejar magias mesmo sem uma ficha ativa ou iniciada.

* [Feito] Lote 115: Consolidação de Arquitetura e Auditoria Subaquática | `(Commit: 8faf742)`
* [Feito] Lote 116: Auditoria e Restauração do Mestre IA | `(Commit: d403148)`
* [Feito] Lote 117: Correção de Precisão RAG (O Nó de Ouro) | `(Commit: db16362)`
* [Feito] Lote 118: Diagnóstico de Falha na Extração de Páginas e Preparação para Correção | `(Commit: f3576eb)`

### Lote 117: Correção de Precisão RAG (O Nó de Ouro)
*   **Commit:** `db16362 Lote 117: Correção de Precisão RAG - Priorização de Regras e Expansão de Funil no Motor de Busca`

[Feito] Lote 118: Diagnóstico de Falha na Extração de Páginas e Preparação para Correção (`f3576eb`)


* [Feito] Lote 119: Estabilização de Infraestrutura e Busca Direta (RAG) | (Commit: Pending)
    - **Busca Direta (Códex):** Refatoração do MestreIARepository e MestreIAGraphEngine para eliminar falhas na busca direta. Implementada limpeza agressiva de pontuação e filtragem de Stop Words para garantir que termos como 'piscina' ou '.45' retornem resultados precisos ignorando ruídos.
    - **Fila de Failover (Alta Disponibilidade):** Reconfiguração da orquestração (MestreIAUseCase). O Gemini 3.1 Pro foi desativado por erros de quota. A nova prioridade é: DeepSeek (Grátis) -> NVIDIA -> Gemini Flash-Lite -> OpenRouter (Backup).
    - **Gemini 3.1 Upgrade:** Atualização para os modelos gemini-3.1-pro-preview e gemini-3.1-flash-lite-preview, abandonando versões descontinuadas conforme solicitado pelo usuário.
    - **Agência Investigativa:** Redução do engessamento do Auditor via Prompt de Sistema (MestreIAPromptsAuditor.kt), forçando transparência sobre buscas falhas e encorajando o uso de regras similares.

* [Feito] Lote 122: Blindagem Anti-Alucinação e Diagnóstico de Falha | (Commit: Pending)
    - **Regras Oficiais Estritas:** O prompt do Auditor foi blindado para proibir terminantemente o uso de conhecimentos externos ou outros sistemas de RPG. Agora a IA deve usar apenas analogias técnicas baseadas nos chunks oficiais (ex: usar regras de Densidade/Materiais para resolver situações de Água) ou pedir novos termos ao usuário.
    - **Expansão Lexical de Ambiente:** Adicionados sinônimos técnicos para líquidos (refração, densidade, atrito) no motor de busca para capturar regras análogas quando não houver uma resposta direta.
    - **Diagnóstico de Failover:** O UseCase agora reporta um resumo detalhado de falhas caso todos os modelos falhem, permitindo identificar se o problema foi Quota (429) ou Créditos (402) em cada tentativa.

* [Feito] Lote 126: MiMo Prime + Correções Críticas RAG | (Commit: Pending)
    - **MiMo Xiaomi (Posição 1):** Adicionados modelos mimo-v2.5-pro e mimo-v2-flash como primeiros da fila de failover. Se o Pro cair, entra o Flash automaticamente.
    - **Fix Histórico Infinito (CRÍTICO):** Corrigido bug onde cada tool call adicionava até 35.000 chars ao historicoInvestigacao. No failover, o mimo-flash recebia 522.922 chars / 164.359 tokens. Agora o histórico guarda apenas resumo (2.000 chars) enquanto o contexto completo fica na ponteDeFerro.
    - **NVIDIA Fix:** Modelo meta/llama-3.1-405b-instruct (removido do tier gratuito) substituído por meta/llama-3.3-70b-instruct.
    - **Logs Estruturados:** Logs do RAG reorganizados com bordas ╔/║/╚ filtráveis pela tag MestreIA_RAG.
    - **Última Iteração Forçada:** Adicionado prompt [RESPOSTA FINAL OBRIGATÓRIA] na última iteração para forçar resposta mesmo se o modelo insistir em chamar tools.

* [Feito] Lote 127: RAG 2.0 — Breakthrough de Busca e Raciocínio | (Commit: f41088e)
    - **Problema Raiz Resolvido (search_text + source_title):** O FTS4 indexava apenas o texto do chunk, não o source_title. Chunks do Pyramid #3/26 "Underwater Adventures" não eram encontrados ao buscar "subaquático" porque seu texto diz "Armas de Fogo: Divida os alcances por 1.000" sem mencionar "piscina". Agora search_text inclui source_title normalizado — buscar "subaquat*" via sinonimos encontra "underwater*" no search_text do Pyramid.
    - **Auto-Versionamento do Codex (sem perder ficha):** Sistema detecta automaticamente se o banco está com o search_text antigo (v1) e re-importa apenas a tabela manual_chunks. Ficha do personagem e histórico de chat são preservados. Usa SharedPreferences "codex_search_text_version".
    - **Sinonimos "underwater" no QueryEngine:** Adicionado "underwat" como sinonimo bilateral de "subaquat", "piscin", "submers", "agu", "mergulh". Também expandidos: "pistol"/"revolv", "alcanc"/"distanc", "modif"/"penal", "armadur", "visibil", "marciai" e outros.
    - **AND Bonus + Proximity Scoring:** Chunks com TODOS os termos base presentes ganham +500 pontos. Pares de termos base dentro de 100 caracteres ganham +200 pontos por par. Isso prioriza chunks que falam sobre o cenário exato (ex: "tiro" + "agua" juntos) vs chunks que mencionam apenas um dos termos.
    - **Relevance Hints (★★★/★★/★):** O contexto enviado à IA agora inclui marcadores de relevância por chunk. Chunks com score ≥ 800 pts recebem [★★★], 300+ recebem [★★], demais [★]. A IA é instruída a priorizar os marcados com ★★★.
    - **Chain-of-Thought Obrigatório:** Protocolo no prompt do sistema exige que a IA mostre 5 passos ao resolver qualquer cálculo: (1) citar regra com [Livro, Pág], (2) identificar valores da situação, (3) mostrar o cálculo explícito, (4) dar o resultado numérico, (5) interpretar o resultado. Proibido arredondar para cima ou omitir impossibilidades.
    - **Dicionário Planner Expandido:** De 23 para 55+ entradas cobrindo combate à distância, armas de fogo, penalidades, cura, armadura, visibilidade, artes marciais, e todos os atributos GURPS com seus sinônimos.
    - **chunkScores no GraphSearchResult:** Motor RAG agora retorna scores junto dos chunks, permitindo formatarParaIA usar relevância real de cada chunk ao marcar ★★★/★★/★.


* [Feito] Lote 128: Fila de Modelos Reordenada + Anti-Loop de Query Duplicada | (Commit: cbe7a84)
    - **Reordenação da Fila de Failover (baseado em análise de logcat):** MiMo v2.5-pro e MiMo v2-flash foram movidos das posições 1-2 para as posições 3-4. Logcat mostrou que ambos entravam em loop de tool calls sem nunca dar resposta final. DeepSeek Gratuito agora é posição 1 (confiável, responde diretamente). Gemini 3.1 Flash-Lite agora é posição 2 (rápido e econômico). NVIDIA e OpenRouter mantidos nas posições 5-7.
    - **Detecção de Query Duplicada (Anti-Loop MiMo):** Adicionada checagem antes de cada tool call: se a mesma query (primeiros 40 chars normalizados) já foi pesquisada no historicoInvestigacao atual, o sistema detecta o loop, injeta mensagem "AVISO: busca já realizada — responda com o contexto atual" e decrementa o loop sem refazer a busca. O logcat mostrou o MiMo Flash chamando exatamente a mesma query duas vezes consecutivas ("armas de fogo subaquáticas alcance dano revólver").

* [Feito] Lote 130: Ponte Inventário — Planner lê a ficha do personagem para busca contextual | (Commit: 37c054e)
    - **Problema resolvido:** Planner era cego para o inventário. Se o personagem tinha um "Revólver Colt .45" e perguntava "meu revólver", o sistema buscava genérico. Agora cruza os termos da pergunta com os equipamentos reais da ficha.
    - **MestreIAPlanner.kt — Cruzamento com inventário:** planejarBusca agora recebe List<Equipamento>. Para cada equipamento, normaliza nome e armaGrupo e verifica match com termos da pergunta. Se match → usa o nome real ("Revólver Colt .45") como query de stats, não o genérico. Possessivo ("meu/minha") + 1 arma no inventário → match automático mesmo sem nome explícito. PlanoDeBusca retorna contextoEquipamentos: String com stats reais.
    - **MestreIAUseCase.kt — Injeção do inventário no contexto:** Passa viewModel.personagem.equipamentos ao Planner. Se contextoEquipamentos não vazio, injeta como "=== EQUIPAMENTO DO PERSONAGEM (inventário) ===" NO INÍCIO do ponteDeFerro (prioridade máxima). Dados reais do personagem chegam à IA antes de qualquer chunk do RAG.
    - **MestreIAUseCase.kt — TOOL_INSPECT_CHARACTER expandido:** Adicionadas seções "armas" (lista armas com tipo, dano, grupo, ST mín) e "armaduras" (lista armaduras com RD e local). IA agora pode pedir para ver o inventário completo de armas ou armaduras do personagem.
    - **MestreIATools.kt — Schemas atualizados:** Descrição do inspecionar_personagem atualizada para incluir 'armas' e 'armaduras'. Enum OpenAI/DeepSeek expandido: atributos, vantagens, pericias, status, armas, armaduras.
    - **Fallback preservado:** Se nenhum equipamento do inventário der match, cai no itemDetector genérico (comportamento do Lote 129). Match de inventário desativa o itemDetector para evitar buscas duplas.

* [Feito] Lote 129: Intuição de Equipamentos — Pré-busca de Stats + Verificação de Variáveis | (Commit: d7b7595)
    - **Problema resolvido:** IA aplicava fórmulas (ex: "alcance ÷ 1.000") sem ter os stats do equipamento. Dividia a distância cênica (4m) em vez do stat da arma (½D=50m). Conclusão errada: "sem penalidade" em vez de "IMPOSSÍVEL".
    - **Solução B — itemDetector no Planner (MestreIAPlanner.kt):** 50+ itens catalogados em 6 categorias com queries de stats específicas: armas de fogo (revólver, pistola, rifle, espingarda, metralhadora, carabina, fuzil, submetralhadora, garrucha), armas de arco/besta/funda (arco, besta, funda, zarabatana, bodoque), armas de arremesso (shuriken, kunai, dardo), armas C/C — espadas/facas (espada, sabre, florete, katana, cimitarra, faca, adaga, punhal), armas C/C — contundentes (machado, clava, maça, porrete, martelo, mangual), armas C/C — haste (lança, alabarda, naginata, cajado, bordão, tridente, arpão, chicote), armaduras (armadura, colete, elmo, capacete, cota, lorica, brigantina, placa), escudos (broquel, rodela). PlanoDeBusca retorna subQueriesStats: List<String>.
    - **Solução B — Execução no UseCase (MestreIAUseCase.kt):** Antes de chamar a IA, executa buscarDiretoNoCodex para cada subQueryStats. Chunks de stats injetados no contexto inicial com cabeçalho "=== STATS DO EQUIPAMENTO (pré-carregado) ===". IA recebe os números prontos antes da iteração 1.
    - **Solução A — Protocolo de Variáveis no Prompt (MestreIAPromptsAuditor.kt):** Novo bloco instrui a IA a distinguir stat de equipamento de valor cênico: "alcance da arma na tabela (½D=50m) ≠ distância até o alvo (4m)". 3 exemplos de distinção crítica. Se não tiver os stats, deve chamar tool call antes de calcular.


### Lote 131: Velocidade e UX Seguras - CONCLUÍDO (commit 94b6163)
- **Desativar Tools na Última Iteração**: `desativarTools = true` é passado para `perguntarAoMestre` na iteração final — tools não são injetadas no JSON, impedindo fisicamente que DeepSeek/MiMo chamem ferramentas quando deveriam responder.
- **Pré-stats em Paralelo**: Loop sequencial de pré-busca de equipamentos substituído por `coroutineScope { async/awaitAll }` — múltiplas buscas de stats rodam simultaneamente.
- **Feedback Visual Granular**: Mensagens de status descritivas por fase ("Consultando X...", "Buscando no manual: Y...", "Verificando stats...", "Compilando resposta final...").

### Lote 132: Fast Answerer + Múltiplas Armas - CONCLUÍDO (commit d94517e)
- **Fast Answerer**: na última iteração, o modelo "pesquisador" (ex: DeepSeek) passa o contexto acumulado para o Gemini Flash-Lite responder — elimina ~9s de geração lenta no final do fluxo.
- **Múltiplas armas**: quando o jogador diz "minha arma" com várias armas no inventário, todas são injetadas no contexto — a IA apresenta stats de todas e o jogador decide qual usar.
- Log mostra `FAST ANSWERER: deepseek-chat → gemini-flash` quando o switch ocorre.

### Lote 133: Handoff de Contexto + Remove Fast Answerer - CONCLUÍDO (commit f59c299)
- **Handoff de Contexto**: ao cair para o próximo modelo da fila, `catalogoDinamico` e `historicoInvestigacao` são preservados — novo modelo recebe todos os chunks e pesquisas do anterior sem refazer buscas do zero. Log: `HANDOFF → gemini: 4 entradas | ctx=35000chars`
- **Remove Fast Answerer**: revertido o switch para Gemini Flash-Lite (era 3× mais lento que DeepSeek).

### Lote 134: Forjador Complexo — Schema Rico para Fichas Completas - CONCLUÍDO (commit 9dcd167)
- **Problema resolvido:** o Forjador gerava JSON simples e o tradutor descartava silenciosamente técnicas, qualidades, peculiaridades, modificadores, especializações e autocontrole — fichas complexas (estilo Jatobá/Ent) nunca eram integradas por completo.
- **Schema rico (`MestreIAResponse`/`MestreIAItem`):** novos campos `especializacao`, `autocontrole`, `modificadores`, `periciaBaseId`, `notas`, `pontosIniciais`; equipamento ganhou `tipo`/`tipoCombate`/`catalogoId`/`bonusDefesa`. Deserializer tolerante a string ou objeto e a custo textual ("-15 pts", "1 fp").
- **Tradutor (`integrarRespostaNaFicha`):** agora integra técnicas (com lookup da perícia-base já na ficha), qualidades, peculiaridades, modificadores casados com `modificadoresEspecificos` do catálogo, especialização de perícia e autocontrole de desvantagens mentais. Equipamento mapeia ARMA/ARMADURA/ESCUDO/CAPA.
- **Prompt (`MestreIAPromptsForjador`):** gabarito de ouro reescrito com exemplo complexo + seção explicando cada campo novo; catálogo de técnicas injetado.
- **Ferramentas (`ForjadorTools`/`ForjadorToolExecutor`):** `forjador_buscar_catalogo` aceita `tecnica` e devolve `modificadores`/`tipoCusto` das vantagens para a IA escolher estruturas válidas.
- **Validação (`gerarRelatorio`):** valida técnicas contra o catálogo (OK/FUZZY/FALLBACK).
- **Verificação:** `test_forjador_complexo.py` (local) — 14/14 testes, incluindo ficha estilo Jatobá resolvendo 100% sem FALLBACK. `BUILD SUCCESSFUL`.
- **Pendente (decisão do usuário):** `modeloRacial` (criação de raças tipo Ent) fica para um lote seguinte.

### Lote 135: Forjador — Extração de JSON Raiz + Reparo de Truncamento - CONCLUÍDO (commit a6281bc)
- **Bug relatado:** a IA gerava a ficha completa no JSON (visível no logcat), mas ao integrar no app só aparecia o nome — e errado ("Cicatriz no Lábio" em vez de "Aragorn").
- **Causa 1 (nome errado):** a extração usava `lastOrNull()` no regex `{"nome":` — pegava o ÚLTIMO objeto interno (uma peculiaridade `{ "nome": "Cicatriz no Lábio" }`) em vez do objeto raiz. Corrigido para localizar a RAIZ: primeiro `{` após a cerca ` ```json `, ou primeiro `{"nome":` do texto.
- **Causa 2 (não preenchia):** a resposta truncava em 4096 tokens (DeepSeek duplicava ~35 magias e estourava o limite) → JSON inválido sem fechar → parse falhava → `fichaGeradaPendente = null` → sem integração.
- **Reparo de truncamento (`repararJsonTruncado`):** novo algoritmo de pilha que fecha strings/arrays/objetos abertos, ignora chaves/aspas dentro de strings e respeita escapes; volta ao último ponto seguro quando corta no meio de um valor.
- **maxTokens 4096 → 8192** na síntese final (ficha complexa não cabia em 4096).
- **Prompt:** regras anti-duplicação (cada id uma vez) e contra desperdício de tokens ("fechar o JSON é mais importante que adicionar mais um item").
- **Verificação:** `test_json_repair.py` (local) — 5/5, incluindo o caso real do logcat (Aragorn cortado no meio de `aparencia`); `test_forjador_complexo.py` 14/14; `BUILD SUCCESSFUL`.

### Lote 136: Forjador — Remove RAG do Manual (Ponte de Ferro 35k) - CONCLUÍDO (commit 17fd045)
- **Diagnóstico (do logcat do Aragorn):** o Forjador chamava `gerarCatalogoDireto` e injetava ~35.000 chars de texto bruto do manual (chunks RAG / "Ponte de Ferro") em TODA iteração. Como a query era genérica ("crie o Aragorn"), o FTS retornava 1148 chunks / quase o manual inteiro — seleção arbitrária de páginas empatadas, 100% ruído para criação de personagem.
- **Por que era nocivo:** esses 35k tokens competiam com o JSON pelo limite de saída (causa raiz do truncamento do Lote 135), encareciam (~35k tokens de input por iteração × 4) e deixavam o Forjador lento sem nenhum ganho — o Forjador precisa de **IDs do catálogo** (já injetados via `MestreIAPromptsForjador`) e da tool `forjador_buscar_catalogo`, não do texto do livro.
- **Correção:** `MestreIAGeneratorUseCase` passa `MestreIAClient.CatalogoNomes()` vazio em vez de chamar o RAG. Confirmado por código que os caminhos são separados — modo Dúvidas usa `MestreIAUseCase.conversarComMestreIA` (arquivo diferente, zero alterações).
- **Verificação:** `git diff` confirma `MestreIAUseCase.kt` (Dúvidas) intacto; `BUILD SUCCESSFUL`.

### Lote 137: Forjador — Atributos Tolerantes + Narrativa Fiel ao Pedido - CONCLUÍDO (commit b80e4bf)
- **Bug 1 (atributos zerados — Aragorn.json com tudo 10):** a IA emitiu os atributos no formato `Personagem` (`"forca":14,"destreza":14,...`) em vez do formato `MestreIAResponse` (`"atributos":{"st":14,...}`). Ela copiou o formato que viu em `contextoPersonagem` (`personagem.toJson()`). Como não havia campo `atributos`, o Gson usava o default `MestreIAAtributos(10,10,10,10)` → ficha integrava com ST/DX/IQ/HT todos 10.
  - **Fix:** campos soltos opcionais (`forca/destreza/inteligencia/vitalidade` + `st/dx/iq/ht`) no `MestreIAResponse` + função `atributosEfetivos()` que resolve 3 formatos por prioridade (objeto canônico > soltos PT > soltos EN). `integrarRespostaNaFicha` e `validarBudget` passam a usar `atributosEfetivos()`.
- **Bug 2 (história de personagem errado — "Kaelen, o Ferreiro" em vez de Aragorn):** a narrativa paralela extraía o nome via regex `chamado\s+...`, que não casava com "crie o Aragorn de senhor dos aneis" (sem a palavra "chamado") → `nomePersonagem = "o personagem"` → a IA inventava um personagem genérico aleatório.
  - **Fix:** a narrativa agora recebe o **pedido inteiro do usuário** (`prompt`) + instrução explícita de ser fiel a personagens conhecidos de livro/filme/jogo e não inventar outro.
- **Verificação:** `test_forjador_complexo.py` 19/19 (5 casos novos de `atributosEfetivos`, incl. o caso real do Aragorn formato Personagem); `test_json_repair.py` 5/5; `BUILD SUCCESSFUL`.

### Lote 138: Forjador — Chat Mostra a História DA FICHA (Fonte Única) - CONCLUÍDO (commit 3f9d48c)
- **Problema:** o Forjador rodava DUAS IAs em paralelo gerando DUAS histórias. A do chat (`narrativaDeferred`, call separado) podia divergir totalmente da que ia para a ficha (campo `historico` do JSON). O usuário lia uma história ("Kaelen, o Ferreiro") no chat enquanto a ficha integrada tinha o `historico` correto do Aragorn — duas fontes, sem garantia de coerência.
- **Decisão do usuário:** usar a história da ficha como fonte única.
- **`FichaIADelegate`:** quando o JSON é parseado com sucesso, o texto exibido no chat passa a ser o `historico` + `aparencia` do próprio objeto integrado (exatamente o que foi pra ficha). Fallback para a narrativa limpa do texto se não houver `historico`/`aparencia`.
- **`MestreIAGeneratorUseCase`:** removida a narrativa paralela inteira (`coroutineScope`/`async`). Só o loop agêntico da ficha roda → **−1 chamada de IA por geração** (mais rápido e barato). O prompt da síntese final agora pede explicitamente os campos `historico` e `aparencia` no JSON. Imports `async`/`coroutineScope` removidos.
- **Verificação:** `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5; `BUILD SUCCESSFUL`.

### Lote 139: Forjador — História Primeiro, Ficha Construída a Partir Dela - CONCLUÍDO (commit 219b888)
- **Rearquitetura a pedido do usuário:** em vez de história paralela (divergia) ou só no fim (espera), o agente agora **concebe a história ANTES da ficha** e usa essa história como base para montar a ficha — como um mestre de RPG humano: pensa quem é o personagem, depois traduz em números.
- **Iteração 0 (nova, leve, sem tools):** a própria IA classifica o pedido e decide o caminho — **(A)** se o usuário trouxe história pronta: PRESERVA o texto dele (voz/fatos/estilo), podendo enriquecer com até 1 parágrafo sem contradizer; **(B)** se é só conceito/personagem conhecido: ESCREVE fiel. A história aparece no chat na hora (`onChunk`) — usuário não fica esperando.
- **Decisão preservar-vs-criar feita pela IA**, não por regex frágil (decisão do usuário). História do usuário é canônica.
- **Coerência:** a história da it.0 entra no `localHistory` como contexto DEFINITIVO; iterações 1-4 montam perícias/vantagens/desvantagens refletindo a narrativa; o prompt da síntese final manda **copiar** (não reescrever) a história nos campos `historico`/`aparencia`. Chat e ficha = mesma fonte.
- **Custo:** substitui a narrativa paralela do Lote ≤137 (mesmo custo: 1 call leve), mas a ficha sai mais fiel ao personagem.
- **Novos:** `PROMPT_HISTORIA_SISTEMA` + `gerarPromptHistoria()` em `MestreIAPromptsForjador`.
- **Verificação:** `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5; `BUILD SUCCESSFUL`.

### Lote 140: Forjador — Integração Idempotente + Dedup (Fim das Duplicatas) - CONCLUÍDO (commit fbf9fce)
- **Diagnóstico (logcat do Aragorn + ficha salva):** o JSON gerado pela IA estava **perfeito** (16 vantagens, 5 equipamentos, zero duplicata, fechado corretamente). Mas a ficha salva tinha **20 vantagens e 10 equipamentos**. As duplicadas eram exatamente as vantagens `por_nivel` (destemor, carisma, reconhecimento_social, renda_propria) + os 5 equipamentos repetidos em bloco.
- **Causa raiz:** `confirmarIntegracao()` zerava `fichaGeradaPendente` **depois** de `integrarRespostaNaFicha` → duplo-clique no botão "INTEGRAR" ou recomposição do Compose disparava a integração **2×**. Vantagens `fixo`/`escolha` são bloqueadas por `jaExisteIdentica` em `FichaTraitDelegate`, mas `por_nivel` e equipamentos **não têm dedup** → só essas duplicaram (bate 100% com a ficha salva).
- **Correção 1 — idempotência:** `confirmarIntegracao()` agora captura a ficha numa local, zera `fichaGeradaPendente` **antes** de integrar e trava reentrada com `integracaoEmAndamento` (`@Volatile`, `try/finally`).
- **Correção 2 — dedup defensivo:** `integrarRespostaNaFicha` aplica `distinctBy(id+especializacao)` em todas as listas e `nome+tipo` em equipamentos. Protege também contra LLM que gera o mesmo array 2× dentro do JSON.
- **Observação:** logcat mostrou `Budget excedido: 1207 pts (máximo: 300 pts)` — esperado neste teste (usuário pediu "não se preocupe com pontos"); não tratado aqui.
- **Verificação:** `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5; `BUILD SUCCESSFUL`.

### Lote 141: Forjador — Modo Consultor (Analisa e Sugere, Não Integra) - CONCLUÍDO (commit 5ff6772)
- **Problema de design (levantado pelo usuário):** o Forjador não distinguia "criar do zero" de "analisar e sugerir". Os modos `geracao` e `analise` faziam exatamente a mesma coisa — sempre geravam ficha JSON + botão INTEGRAR. Pedir "o que ficaria melhor nessa ficha?" gerava uma ficha nova em vez de só opinar. O modo `analise` era código morto (sem entrada na UI; menu só tinha Dúvida/Criar).
- **Decisão do usuário:** modo Consultor separado; edição = só o delta; aplicar via linguagem natural.
- **3 modos agora distintos:**
  - **📖 Dúvida** (`conversa`) — regras via RAG (inalterado).
  - **🔍 Analisar ficha** (`analise`) — NOVO Consultor: lê a ficha existente (`forjador_ler_ficha`), responde **só em texto** (diagnóstico + sugestões priorizadas com IDs reais e o porquê), **sem JSON, sem botão INTEGRAR**. Pula a iteração 0 (não cria história/personagem). Termina perguntando se quer aplicar.
  - **🏗️ Criar** (`geracao`) — ficha do zero, integra (inalterado).
- **Aplicar uma sugestão:** o usuário pede em linguagem natural no modo Criar; o dedup + idempotência do Lote 140 preservam o resto da ficha.
- **Arquivos:** `DialogsMestreIA` (3ª opção no menu), `FichaIADelegate` (só `geracao` seta `fichaGeradaPendente`), `MestreIAGeneratorUseCase` (`analise` usa `gerarPromptConsultor`, pula it.0, resposta final textual), `MestreIAPromptsForjador` (`gerarPromptConsultor()` + `blocoCatalogo` refatorado para reuso).
- **Verificação:** `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5; `BUILD SUCCESSFUL`.

### Lote 142: Consultor Conversacional — Aplicar Sem Trocar de Modo - CONCLUÍDO (commit 5a5f4ee)
- **Correção de UX (apontada pelo usuário):** no Lote 141 a instrução ficou "para aplicar, troque para o modo Criar" — fricção que o usuário não queria. A decisão dele era fluxo **conversacional natural** (igual à interação do chat aqui): no modo Analisar, perguntar e mandar aplicar na mesma conversa.
- **Modo Analisar agora tem 2 comportamentos, a IA escolhe pelo pedido:**
  - **SUGERIR** (pergunta tipo "o que melhora?"): responde texto, sem JSON, sem botão.
  - **APLICAR** ("faça a 1 e 2, e adicione X, Y"): gera **só o DELTA** em JSON → botão INTEGRAR aparece. Tudo sem trocar de modo.
- **`gerarPromptConsultor`** reescrito com os dois modos explícitos; removida a instrução de trocar para Criar.
- **`FichaIADelegate`:** modo `analise` seta `fichaGeradaPendente` **se** vier JSON (delta); só texto → sem botão.
- **`integrarRespostaNaFicha` agora é DELTA-SAFE:** não sobrescreve `nome`/`historico`/`aparencia`/`notas` vazios e só aplica atributos se o JSON realmente os trouxe — mesclar um delta não apaga/zera a ficha existente.
- **Verificação:** `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5; `BUILD SUCCESSFUL`.

### Lote 143: Consultor — Substituição de Seção (Remover/Dedup/Editar) - CONCLUÍDO (commit ebe8001)
- **Problema (logcat):** usuário pediu ao Consultor para corrigir as duplicatas da ficha do Aragorn. A IA alucinou campos inexistentes (`removerVantagens`, `removerEquipamentos`, `remover:{}`) porque **o sistema só sabia ADICIONAR** — não havia caminho de remoção. O parser ignorava os campos, `integrarRespostaNaFicha` rodava com listas vazias → botão INTEGRAR clicado 2× e **nada acontecia**, com feedback enganoso "integrada com sucesso".
- **Decisão do usuário:** substituição de seção.
- **Schema:** campo `substituir: List<String>` — seções que vêm completas e devem substituir (não somar) a ficha.
- **Tradutor:** para cada seção em `substituir`, zera a lista atual (`removerX` em loop decrescente) antes de reaplicar a do JSON; seções fora de `substituir` = merge aditivo (inalterado).
- **Prompt Consultor:** MODO APLICAR instruído a usar `substituir` + lista final completa quando remover/dedup/editar; proíbe inventar campo `remover`; exemplo no prompt.
- **`FichaIADelegate`:** detecta delta inócuo (sem listas e sem `substituir`) e **avisa no chat** em vez de mentir "integrada com sucesso".
- **Pendente/próximo:** usuário sugeriu uma **ferramenta de edição** — implementada no Lote 144.
- **Verificação:** `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5; `BUILD SUCCESSFUL`.

### Lote 144: Ferramenta de Edição `forjador_editar_ficha` - CONCLUÍDO (commit 73ac659)
- **Sugestão do usuário:** em vez de só gerar JSON, dar ao agente uma ferramenta que **edita a ficha direto** (escolhe item e remove/substitui/altera) — como uma ferramenta de editar código. Arquiteturalmente superior ao `substituir` para mudanças cirúrgicas.
- **Decisões do usuário:** aplica direto (sem botão, conversacional); remover tira **só 1 cópia** (a última ocorrência) por chamada — seguro para dedup.
- **`ForjadorTools`:** nova `TOOL_EDITAR` (`forjador_editar_ficha`) nos schemas Gemini + OpenAI. Params: `operacao` (remover/adicionar/alterar), `secao`, `alvo` (id|nome), `valor` opcional (`nivel=14;esp=Florestas`).
- **`ForjadorToolExecutor.editarFicha`:** aplica direto via `viewModel.removerX`/`adicionarX` + `autoSaveIA`. `remover` = última ocorrência (normalização de acentos no match). `adicionar`/`alterar` por lookup id/nome no catálogo.
- **`MestreIAGeneratorUseCase`:** `TOOL_EDITAR` incluída no filtro de tool calls do loop agêntico.
- **Prompt Consultor:** `forjador_editar_ficha` é o **caminho preferencial** para itens pontuais (cirúrgico, aplica na hora); JSON `substituir` (Lote 143) vira fallback.
- **Verificação:** `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5; `BUILD SUCCESSFUL`.

### Lote 145: `forjador_editar_ficha` Completo — Atributos, Técnica, Magia + Log - CONCLUÍDO (commit bfd651a)
- **Double-check no logcat (usuário testou):** a ferramenta só fazia bem remover/adicionar vantagem-perícia. Buracos confirmados no log real:
  - ST não alterava (sem case "atributos" → caía no `else`; a IA *presumia* sucesso e mentia "ST 14").
  - adicionar técnica falhava (`else` "não suportado"); a IA tentou contornar via JSON `substituir`.
  - **sem log de resultado** → qualquer "funcionou" era fé, não verificação.
- **ATRIBUTOS:** novo case — alterar forca/destreza/inteligencia/vitalidade (aceita ST/DX/IQ/HT, valor solto ou `valor=`). Loga "de X para Y".
- **TÉCNICAS:** adiciona via catálogo + perícia-base. **Auto-escolhe** a melhor perícia da ficha que atende o pré-requisito (`tecnicaAtendePreRequisito`) e tem maior NH; `periciaBase=<id>` opcional. Se nenhuma serve, erro claro citando o pré-requisito (decisão do usuário: auto-escolher melhor base válida — resolve o caso da técnica "Ataque Furacão" que exige Arma C/C).
- **MAGIAS:** adicionar via catálogo.
- **PERÍCIA:** usa `nivel` (NH) para calcular pontos, não custo bruto.
- **LOG DE RESULTADO:** `execute()` loga o retorno de toda tool (`Forjador_Tools "Resultado ..."`) → auditoria real no próximo logcat.
- `ForjadorTools`: schema/enum incluem "atributos" + doc de `periciaBase`.
- **Verificação:** `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5; `BUILD SUCCESSFUL`. Pendente: teste de device do usuário (agora com log de resultado para auditoria real).

### Lote 146: Read-back — Verificação Automática Pós-Edição - CONCLUÍDO (commit 5244df5)
- **Ideia do usuário:** após editar, o modelo deveria reler a ficha e conferir se a alteração realmente entrou — em vez de presumir. (No logcat a IA dizia "ST aumentado para 14" sem ter lido de volta — era fé.)
- **Mecânica:** quando uma iteração do loop tem ≥1 `forjador_editar_ficha`, o app detecta as seções tocadas (dos `args`), **relê automaticamente** essas seções (estado real pós-edição, via novo `ForjadorToolExecutor.lerSecao()`; `atributos` puxa `pontos` junto) e injeta no contexto a releitura + instrução obrigatória de verificar item por item.
- **Decisões do usuário:** se algo não aplicou, o agente tenta corrigir sozinho 1× e depois reporta; o read-back roda sempre que houver edição.
- **Garantia:** loop de 4 iterações com `desativarTools` só na última → há folga para a IA reagir ao read-back e re-aplicar antes de finalizar.
- **Verificação:** `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5; `BUILD SUCCESSFUL`.

### Lote 147: editar_ficha Idempotente + Ler Técnicas + Read-back Auto-fix - CONCLUÍDO (commit 819ebec)
- **3 incoerências confirmadas no logcat** (usuário testou trocar perícia machado→arremesso):
  - **#1 — `alterar` técnica/perícia/magia DUPLICAVA:** só existia o caminho "adicionar". Pedir `alterar` (ex: re-vincular Contra-Ataque) criava uma 2ª cópia. Resultado real visto na tela: ficha com **2 Contra-Ataque**, uma órfã. **Fix:** técnica/perícia/magia agora **idempotentes** — removem todas as ocorrências do alvo (inclui órfãs) antes de re-adicionar. Alterar/re-aplicar atualiza no lugar, nunca duplica.
  - **#2 — read-back não auto-corrigia:** o Lote 146 relia, mas a IA só **sugeria** limpar a duplicata e **perguntava**. **Fix:** instrução do read-back reescrita, imperativa — detectou duplicata/órfã que ela causou → CHAME `forjador_editar_ficha remover` AGORA, sem perguntar.
  - **#3 — `forjador_ler_ficha` não tinha case "tecnicas"** (nem qualidades/peculiaridades) → a IA recebia "seção inválida" e ficava cega para técnicas. **Fix:** `lerFicha` cobre técnicas (mostra base + NH ou "ÓRFÃ"), qualidades, peculiaridades; enum/descrição da tool (Gemini+OpenAI) e msg de erro atualizados.
- **Verificação:** `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5; `BUILD SUCCESSFUL`.
- **Próximo (memória):** testar MAGIAS — 900 magias + sistema de pré-requisitos complexo.

### Lote 148: Correção da Regressão do 147 — Unicidade por Chave Composta - CONCLUÍDO (commit fb64bcf)
- **Regressão apontada pelo usuário:** o Lote 147 fez a idempotência de técnica/perícia casar **só por nome/id**, o que apagava variantes legítimas (Contra-Ataque de Espada some ao adicionar Contra-Ataque de Machado).
- **Regras de unicidade corretas (confirmadas com o usuário):**
  - **TÉCNICA:** chave = técnica **+ perícia-base**. Variantes de bases diferentes coexistem. Remoção seletiva: só remove (mesma técnica + **mesma** base alvo) OU (mesma técnica **órfã** — base não existe na ficha). Base resolvida ANTES de remover. Outras bases válidas preservadas.
  - **PERÍCIA:** chave = perícia **+ especialização**. Sobrevivência/Florestas e Sobrevivência/Montanhas coexistem.
  - **MAGIA:** chave = só a magia (app não deixa duplicar) — Lote 147 já estava certo, mantido.
- **Resultado:** mata a técnica órfã (perícia-base removida → NH em branco) sem destruir duplicatas legítimas.
- **Verificação:** `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5; `BUILD SUCCESSFUL`.

### Lote 149: Modelo "Vê" a Validação de Pré-Requisito de Magia (Igual ao Usuário) - CONCLUÍDO (commit fca8a80)
- **Diagnóstico (logcat do "Mestre Arkanus", 328 magias):** a IA despejou as magias de cabeça, chamou `forjador_gps_magia` **0 vezes**, e o `editarFicha` adicionava sem validar → cadeias de pré-requisito potencialmente quebradas.
- **Ideia do usuário:** não reinventar validação — fazer o modelo **enxergar o sistema que o app já tem** (`prereqFailureForMagia` / `adicionarMagia(ignora=)` — o mesmo que mostra "✓ Requisitos Atendidos" e o botão "Adição Forçada" na tela de magias).
- **`editarFicha` magia:** chama `prereqFailureForMagia` antes de adicionar. Falta pré-requisito → **BLOQUEADO** com o motivo exato (igual o app barra o usuário) + instrução de usar `gps_magia` e a cadeia. `valor="forcar=true"` → `adicionarMagia(ignora=true)` (= botão "Adição Forçada", gatilho narrativo).
- **`buscar_catalogo magia`:** cada resultado mostra "✓ requisitos atendidos" ou "⚠ FALTA: X" — o modelo vê o **mesmo status que o usuário** antes de tentar.
- **Prompt Forjador + Consultor:** protocolo obrigatório — `gps_magia(id)` + adicionar a cadeia toda na ordem antes da magia-alvo (decisão do usuário: IA resolve a cadeia sozinha).
- **Verificação:** `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5; `BUILD SUCCESSFUL`. Pendente: teste de device com magia avançada real (ex: "Desejo") — validar que a IA usa o GPS e monta a cadeia.

### Lote 150: GPS de Magia com VEREDITO + Modelo Não Calcula Pré-Requisito - CONCLUÍDO (commit 464b929)
- **Diagnóstico (logcat do teste "Desejo"):** o GPS calculava certo (`Escolas para Encantar: 10/10`), mas o modelo **ignorava o número** e refazia a conta de cabeça, alucinando o mapeamento escola↔magia ("Criação Inspirada = escola Quebrar e Consertar") e concluindo errado que faltavam escolas. Causa: o GPS devolvia número cru e ambíguo (com rótulo duplicado `Escolas: Escolas:`) → o modelo interpretava em vez de obedecer.
- **Não era bug do GPS nem da escola** (catálogo 839/839 com escola; GPS contou 10/10 certo) — era o LLM não confiando na ferramenta.
- **Correção real:**
  - `gpsMagia`: **VEREDITO claro no topo** via `prereqFailureForMagia` (mesmo juiz do app): "✅ PODE ADICIONAR AGORA" / "⛔ AINDA NÃO — Falta: X" / "✅ JÁ ESTÁ na ficha". Corrigido o rótulo duplicado `Cadeia:`/`Escolas:`.
  - Prompt: protocolo reescrito — o modelo **NÃO calcula/conta escola** (ele erra). Lê o VEREDITO, desce a cadeia recursivamente pelos pré-requisitos que faltam, adiciona as base, rechama o GPS até o VEREDITO de X virar "PODE ADICIONAR", então adiciona X e confere (read-back). O app é o juiz.
- **Resultado:** usuário pede magia X → sistema adiciona toda a cadeia + a própria X, sem o modelo inventar a matemática de escolas.
- **Verificação:** `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5; `BUILD SUCCESSFUL`.

### Lote 151: Loop com Teto Dinâmico — Cadeias Longas de Magia (Desejo) - CONCLUÍDO (commit 6c45361)
- **Diagnóstico (logcat teste "Desejo"):** o Lote 150 funcionou **perfeito** — GPS com VEREDITO claro, o modelo **obedeceu** (não alucinou escola desta vez), desceu a cadeia recursivamente, o app validou os pré-requisitos e **10 magias-base entraram** (Luz, Escudo, Acalmar Animal, Localizar Água, Atear Fogo, Tolice, Localizar Planta, Localizar Terra, Conceder Energia, Aporte). MAS o loop tinha teto **fixo de 4 iterações**; a cadeia do Desejo é profunda demais (10 escolas → Encantar → Pequeno Desejo → Desejo) e foi cortada no meio — no fim a IA só **sugeria** os próximos passos. Não foi bug nem estouro de token: foi o limite de iterações.
- **Correção (decisão do usuário: teto dinâmico):** `for(1..4)` → `while` com `limiteIter` dinâmico (base `ITER_MIN=4`, máx `ITER_MAX=12`). Mede `totalItens()` antes/depois de cada iteração; se progrediu (itens novos) e < máx, estende +1. Sem progresso → não estende → caminha para a síntese (anti-loop natural). Síntese agora por `iteracao >= limiteIter-1` (dinâmico). Prompt intermediário reforçado: não pare no meio da cadeia.
- **Verificação:** `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5; `BUILD SUCCESSFUL`.

### Lote 152: Blindar processarRespostaIA — Resposta Nunca Some do Chat - CONCLUÍDO (commit d80d79a)
- **Bug (logcat teste Desejo):** o Lote 151 funcionou (loop estendeu 4→7, magias entrando), mas a cadeia longa não terminou no limite e a IA respondeu **texto** (análise / "falta X, quer continuar?") em vez de JSON. `processarRespostaIA` roda em `scope.launch(Main)`; uma exceção entre "Tool Calls" e "Iniciando Parse" (regex/reparo de JSON sobre 5742 chars de markdown) **matava a coroutine em silêncio** → chat vazio (o usuário via a mensagem no log mas não no app).
- **Correção:** corpo extraído para `processarRespostaIAInterno`, envolto em `try/catch(Throwable)`. Qualquer falha cai num fallback que **exibe o texto da IA** (limpo do bloco ```json```) no chat + salva sessão + `onResult`. Mensagem da IA nunca mais desaparece silenciosamente.
- **Verificação:** `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5; `BUILD SUCCESSFUL`.

### Lote 153: Gate de JSON — Não Parsear Markdown como Ficha (Causa Raiz) - CONCLUÍDO (commit 7e5c7bc)
- **Diagnóstico (logcat com Lote 152 já no APK via clean build):** desta vez `Iniciando Parse` apareceu, mas o log **parou ali** — travou **dentro da extração de JSON**. Causa raiz: a resposta de análise/consultor é **texto markdown** com `{` em exemplos/tabelas; o código fazia `rawText.indexOf("{")` e jogava o trecho no `repararJsonTruncado`, que mastigava markdown grande e **travava a coroutine** antes do try/catch do Lote 152 conseguir reportar.
- **Correção definitiva (gate na entrada):** só tenta extrair JSON se houver **sinal real de ficha** — bloco ```json``` OU regex `{"nome":`. Texto puro → `jsonNoTexto=null` → pula todo o parse/reparo → cai direto no `narrativaLimpa` (texto aparece inteiro no chat). Defesa extra: `repararJsonTruncado` devolve a entrada intacta se > 50k chars.
- **Nota de processo:** o teste anterior falhou porque o APK era antigo (build incremental não regenerou); resolvido com `gradlew clean assembleVisualDebug`.
- **Verificação:** clean build OK; `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5.

### Lote 154: CAUSA RAIZ — Race Condition no Histórico do Chat - CONCLUÍDO (commit e6b8b63)
- **Sintoma persistente (3+ testes):** a resposta final do agente não aparecia no chat, mesmo após Lotes 152/153 e clean build. O usuário insistiu (corretamente) que não era APK velho nem o parse.
- **CAUSA RAIZ:** `assistantIndex` era fixado no início (`size-1`). Durante o loop agêntico longo (9 iterações, ~19 magias), `injetarEvento` faz `mestreIAChatHistory = mestreIAChatHistory + [SISTEMA] msg` dezenas de vezes. Os callbacks (`onStatusUpdate`/`onChunk`/`onResultado`) faziam `val h = mestreIAChatHistory.toMutableList(); …; mestreIAChatHistory = h` — entre o `toMutableList()` e a atribuição, outra coroutine injetava `[SISTEMA]` e essa mensagem **e a resposta final eram descartadas**. Race condition clássica de *lost update* num estado compartilhado (`mutableStateOf`).
- **Correção:** helper `atualizarMsgAssistente(ref, transform)` acha a mensagem por **identidade de objeto (`===`)** na lista **viva** no instante da escrita e substitui só ela — nunca sobrescreve a lista inteira com cópia velha. Todos os 4 callbacks + `processarRespostaIA`/`Interno` + o fallback do try/catch (Lote 152) usam o helper. `assistantIndex` eliminado.
- **Erros do modelo (do mesmo log, ainda abertos):** (1) cadeia do Desejo não terminou — `ITER_MAX=12` insuficiente, parou em "Encantar liberado"; (2) modelo desperdiça iterações (relê ficha vazia, 1 magia por ciclo); (3) pega cadeias inteiras por escola em vez de 1 magia/escola (19 magias vs ~13 necessárias). A tratar em lote seguinte.
- **Verificação:** clean build OK; `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5.

### Lote 155: ID Estável na ChatMessage — Fim da Race Condition Residual - CONCLUÍDO (commit 1b7d735)
- **Bug residual do Lote 154:** o `Iniciando Parse` passou a aparecer nas duas respostas (backend OK), mas a final ainda não chegava no chat. O `atualizarMsgAssistente` achava a mensagem por `===` (identidade de objeto), mas cada `onStatusUpdate`/`onChunk` faz `.copy()` criando **nova referência** e reatribuía `assistantRef`. Em concorrência (status updates vs `onResultado`), o `ref` passado não batia mais por `===` na lista viva → `idx<0` → mensagem final **não escrita**.
- **Correção definitiva:** `ChatMessage` ganhou campo `uid` (UUID, default) que **sobrevive aos `.copy()`**. `atualizarMsgAssistente` acha por `uid` (String imutável), nunca por `===` nem índice. `conversar` fixa `assistantUid` uma vez; todos os callbacks e `processarRespostaIA`/`Interno` usam o `uid`. `assistantRef` eliminado.
- **Verificação:** clean build OK (APK 18:41); `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5.

### Lote 156: Fallback Indevido + Fila DeepSeek 2 (Chave Gratuita) - CONCLUÍDO (commit cda51eb)
- **Lote 155 confirmado no log:** a mensagem de erro **apareceu no chat** ("Erro: Falha na conexão") em vez de sumir — a blindagem do histórico (uid estável) funciona.
- **Diagnóstico (logcat):** DeepSeek respondeu HTTP 200 (6117 chars) mas o sistema **trocou para o Gemini**, que deu **429** (chave free tier com `limit: 0`, quota zerada) → "Erro: Falha na conexão".
- **Problema 1 — detecção de erro frágil:** `startsWith("Erro") || contains("Erro de API")` confundia texto legítimo da IA (análise mencionando "Erro") com falha de infra → `break` + fallback indevido. **Corrigido:** `fun ehErroDeApi()` casa só os padrões reais do `MestreIAClient` (`Erro <3 dígitos>:`, `Erro de Conexão:`, `Erro: Resposta vazia/Modo Stream/Falha na conexão`). Aplicado nos 3 pontos (loop, resposta final, narrativa it.0).
- **Problema 2 — fallback morto:** a fila usava Gemini 3.1 Pro, cuja chave está com quota 0 (free tier zerado). **Corrigido:** fallback agora é `MESTRE_IA_DEEPSEEK_2_KEY` (chave DeepSeek gratuita, já no BuildConfig). Fila: DeepSeek → DeepSeek 2.
- **Verificação:** clean build OK (APK 19:35); `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5.



**[Bateria de Testes a Realizar]**
- Bateria de Testes (Stress Test)
Impacto em Alta Velocidade: "Um cavaleiro em carga a cavalo (Move 8) atinge um soldado com uma lança. Como calculo o dano de colisão baseado na ST 16 do cavalo?"
Regras de Afogamento: "Meu personagem caiu em um rio e está sem fôlego. Quanto tempo ele aguenta antes de começar a perder PV e quais são os testes de HT?"
Visibilidade Crítica: "Estou tentando atirar em um alvo na escuridão total, mas tenho 'Visão Noturna 5'. Qual a minha penalidade final?"
Equipamentos e Carga: "Estou carregando 40kg de ouro. Minha ST é 10. Como isso afeta minha Esquiva e meu Deslocamento atual?"
Aparar com Escudo: "Um ogro me atacou com uma clava gigante. Posso usar a regra de 'Aparar com o Escudo' ou sou obrigado a Bloquear?"
Criação de Especialista: "Gere uma ficha de um Ninja especializado em infiltração tecnológica (NT 9), com 'Mãos Pegajosas' e 'Passo Leve', usando 150 pontos."
Regra de Recuo (Armas de Fogo): "Se eu der uma rajada de 3 tiros com uma submetralhadora de Recuo 2, como calculo quantos tiros acertaram?"
