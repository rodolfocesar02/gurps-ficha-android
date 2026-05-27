# Acompanhamento do Projeto da Ficha GURPS (Para Rodolfo)

**Última Atualização:** 22 de Maio de 2026
**Status Atual:** Mestre IA - Lotes 257-261 CONCLUÍDOS (semântica híbrida completa)

### Sincro V24: Super Release 2.0 (Lote 86)
- **Lançamento Oficial V1.5.0**: Build de produção gerada para as variantes Visual e PraCego.
- **Unificação de Traços e Busca Inteligente**: Finalização do Lote 86 com todas as melhorias de interface e blindagem de cálculo integradas.
- **Preparação de Update**: Arquivo `update.json` atualizado para notificar os usuários sobre a nova versão. 

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
- **Bug 2 (história de personagem errado — "  , o Ferreiro" em vez de Aragorn):** a narrativa paralela extraía o nome via regex `chamado\s+...`, que não casava com "crie o Aragorn de senhor dos aneis" (sem a palavra "chamado") → `nomePersonagem = "o personagem"` → a IA inventava um personagem genérico aleatório.
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

### Lote 157: CAUSA RAIZ REAL — ReDoS (Catastrophic Backtracking) Trava o Parse - CONCLUÍDO (commit 59fca74)
- **Avaliação concreta (5+ ocorrências do mesmo fenômeno):** o log SEMPRE para exatamente em `Iniciando Parse` quando a resposta de análise é grande (4000+ chars de markdown). Não loga `Falha no processamento` (try/catch do 152) nem escreve no chat → não é exceção nem sucesso: é **trava** (loop de CPU).
- **Causa raiz isolada:** o regex `\{\s*"nome"\s*:` rodando com `containsMatchIn` sobre 4000+ chars de markdown (tabelas, `{`, `**`) sofre **catastrophic backtracking (ReDoS)**. A coroutine congela no regex — por isso o log morria em "Iniciando Parse" (a linha seguinte é o regex) e nenhum try/catch pegava (não há exceção). **Os Lotes 152-156 não resolveram porque atacavam sintomas; este é o defeito real.**
- **Correção definitiva (zero regex sobre resposta livre da IA no caminho do parse):**
  - Gate de JSON: `rawText.contains("\"nome\"")` literal O(n) em vez de regex; início localizado por `indexOf`/`lastIndexOf`.
  - `limparNarrativaParaChat`: removido `Regex("```json.*?```", DOTALL)` (o `.*?` DOTALL também faz backtracking) → varredura linear com `indexOf`.
- **Verificação:** clean build OK (APK 19:58); `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5.

### Lote 158: Instrumentação (logs ponto-a-ponto) - CONCLUÍDO (commit e784602)
- Após 5+ rodadas atacando causas deduzidas, adicionada instrumentação `MestreIA_Trace [P0..P6]/[I0,I1]/[PX]` em cada etapa do processamento — para o log dizer **exatamente** onde trava, em vez de dedução. Sem mudança de lógica.

### Lote 159: Causa Raiz CONFIRMADA por Trace + Cadeia Completa Sem Perguntar - CONCLUÍDO (commit 41a4ff4)
- **Trace provou:** o backend agora vai até o fim (`[P0]→[I0]→[I1]→[P1] temSinalJson=false →[P2] jsonReal=-1 →[P3] null →[P4] →[P5] →[P6] FIM ok`). A mensagem **aparece no chat** (confirmado pelo usuário nas imagens). **A causa raiz era o ReDoS (Lote 157)** — os testes anteriores "iguais" eram **APK velho** sem a instrumentação. **Bug crônico da mensagem sumindo: RESOLVIDO.**
- **Problema restante (comportamental, apontado pelo usuário):**
  - Loop curto: `ITER_MAX=12` mas a cadeia do Desejo precisa de ~15-20 ciclos → parava no meio. → `ITER_MAX` 12→**25** (mantém anti-loop: só estende se adicionou itens; sem progresso encerra). Decisão do usuário.
  - Modelo **parava pra perguntar** "quer que eu aplique?" mesmo com ordem direta. Prompt intermediário reescrito: ordem direta = **executa inteira**, não pergunta no meio, só finaliza quando a magia-alvo está na ficha. `[RESPOSTA FINAL]` do modo análise não finge que terminou — reporta o que falta.
  - **Status ao vivo descritivo** (decisão do usuário — não deixar tela em branco): "✏️ Aplicando: X", "🧭 Calculando pré-requisitos: Y", "🔎 Buscando: Z" + passo N, como um editor mostrando a ação.
- **Verificação:** clean build OK (APK 20:40); `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5.

### Lote 160: Parada por ESTAGNAÇÃO (não contador) — Cadeia Longa Não Corta no Meio - CONCLUÍDO (commit 30d1fa8)
- **Trace [P0..P6] provou de novo:** backend 100% ok, `[P6] FIM ok`, mensagem aparece no chat. **Não há bug técnico.** O problema é lógico.
- **Diagnóstico:** o Lote 159 (`ITER_MAX=25`) não surtiu efeito — o log mostrou parada em **4 iterações**. Causa: `limiteIter` começava em 4 e só estendia +1 **se progrediu**. Iterações 1-2 só usaram `gps_magia` (0 itens novos) → não estenderam → iteração 4 já virou `[RESPOSTA FINAL]`. O teto de 25 era inalcançável porque o contador rígido estourava antes.
- **Correção de lógica:** removido `limiteIter`/`ITER_MIN`/`ITER_MAX`. Parada agora por **estagnação**: conta iterações consecutivas que chamaram ferramenta mas não adicionaram item; progresso **zera** o contador; 2× seguidas sem progresso → encerra. Teto duro de segurança = 30. Cadeia longa continua o quanto precisar enquanto progride.
- **Verificação:** clean build OK (APK 20:50); `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5.



### Lote 161: Separar PEDIDO do Usuário de Instrução do SISTEMA - CONCLUÍDO (commit 602ae1c)
- **Bug apontado pelo usuário:** a IA afirmou que ele aceitou uma sugestão que **ela mesma** fez, e agiu sobre algo que ele não pediu.
- **Causa raiz (log):** o código injetava prompts de orquestração ("O usuário deu uma ORDEM DIRETA...", "RESULTADO DAS FERRAMENTAS") no `localHistory` com role **"user"**. O modelo lia instrução do sistema / sugestão própria como se fosse fala do usuário → perdia a fronteira de quem pediu o quê.
- **Correção (decisão do usuário — fixar pedido + marcar sistema):** `pedidoUsuario` fixo; `comAncora()` reafirma a cada iteração "ÚNICO PEDIDO REAL DO USUÁRIO: <texto>" + "INSTRUÇÃO INTERNA DO SISTEMA (NÃO é fala do usuário)". RESULTADO DAS FERRAMENTAS e instrução da história rotulados "[SISTEMA — não é mensagem do usuário]". Prompts: "não trate suas sugestões como aceitas".
- **Verificação:** clean build OK (APK 20:55); `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5.



### Lote 162: Consultor — Regra de 2 Passos Explícita - CONCLUÍDO (commit b754325)
- **Reposicionamento (pergunta do usuário):** estamos no modo **Analisar**, não Criar. Logo, a IA sugerir+perguntar na 1ª msg **não é bug — é o correto**. Os Lotes 159-160 atacaram isso achando ser bug do modo Criar; reposicionado.
- **Decisão do usuário:** no Analisar, SEMPRE sugerir primeiro; aplicar só após 2º ok explícito (mesmo que o 1º pedido pareça ordem direta).
- **`gerarPromptConsultor` reescrito — REGRA DE 2 PASSOS:** PASSO 1 (toda 1ª msg, incl. "ordem direta") = pedido de PLANO; só ferramentas de leitura; mostra a cadeia em texto; não chama editar_ficha nem gera JSON; pede confirmação. PASSO 2 (só quando a última msg REAL do usuário é aceite claro — "sim/pode aplicar/ok") = executa o plano inteiro sem re-perguntar. Proibido presumir confirmação; sugestão não respondida ≠ aceita (resolve a alucinação "você aceitou").
- Lotes 159-160 mantidos (estagnação/teto 30 servem ao passo 2).
- **Verificação:** clean build OK (APK 20:59); `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5.



### Lote 163: Modo Alvo (Pathfinder) CORRIGIDO — contagem de escolas + atalho guloso - CONCLUÍDO (commit 9b9d2dd)
- **Testado com o MOTOR REAL** (`planejarCaminhoMinimo` + `magias2versao.json` via JUnit temporário). Dois bugs no Pathfinder:
- **Bug 1 (raiz):** `escolasConhecidas()` fazia `flatMap` de TODAS as escolas de cada magia. Magias multi-escola (ex: `convocar_elemental`=Ar/Fogo/Terra/Água) inflavam: 4 magias viravam "10 escolas" → Encantar/Desejo liberavam cedo demais e com trilha ERRADA. Fix: conta a escola **principal** (1 magia = 1 escola), alinhado à regra GURPS "1 mágica em N escolas diferentes".
- **Bug 2 (explosão):** com a contagem correta, "10/15 escolas distintas" tornava a busca combinatória → OutOfMemory / 1800 nós / trilha vazia. Fix: **atalho guloso** antes do A* (pega 1 magia aprendível que é pré-req obrigatório pendente OU abre escola nova barata; repete até o alvo liberar; O(passos×catálogo)). A* vira fallback. `ordenarCandidatas`: escola nova vira critério dominante c/ meta de escola.
- **Resultado (motor real):** encantar 10 magias/10 escolas; pequeno_desejo 11; **desejo 16 magias/15 escolas em 10ms** (antes estourava memória).
- **Verificação:** clean build (APK 21:47); `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5.
- **Próximo:** ligar o Pathfinder em produção (adapter/GPS) — ele estava DESLIGADO (ver [[diagnostico-modo-alvo-pathfinder]]); depois o anti-loop ([[plano-forjador-cadeia-desejo]]).



### Lote 164: Liga Pathfinder em Produção + Corrige Anti-Loop - CONCLUÍDO (commit 061a956)
- **(1) Pathfinder LIGADO:** `NexusArcanoModoAlvoAdapter.calcular()` agora chama `planejarCaminhoMinimo` (estava desligado/código morto desde sempre — diagnóstico). Novo campo `snapshot.trilhaOtimaIds` (runCatching → falha cai no comportamento antigo, nunca quebra o GPS). `ForjadorToolExecutor.gpsMagia` imprime "TRILHA MAIS RÁPIDA (adicione NESTA ORDEM)" — a IA segue o roteiro pronto do A*/guloso (Lote 163) em vez de tatear. Faltava `import nexus.arcano.planejarCaminhoMinimo` (extension function).
- **(2) Anti-loop corrigido (bug Lote 160):** iteração só com `forjador_gps_magia` (descobrir a trilha) era contada como estagnação → loop morria exatamente quando o GPS liberava o que adicionar. Agora: `progrediu`(itens) zera tudo; `pesquisou`(GPS/buscar) zera `semProgresso` mas conta `pesquisaSeguida`; estagnação real (nada útil) incrementa. Encerra se `semProgresso>=2` OU `pesquisaSeguida>=4` OU teto 30. Cadeia longa avança; loop de pesquisa pura ainda barrado.
- **Verificação:** clean build (APK 21:54); `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5.
- **Próximo:** teste de device do usuário — pedir "adicione a magia Desejo e as necessárias" no modo Analisar; confirmar GPS mostra TRILHA, IA segue na ordem, cadeia completa até Desejo entrar.

### Lote 165: Resposta final do Forjador some do chat em cadeia longa - CONCLUÍDO
- **Sintoma (relato do usuário, teste 22:01):** "meia vitória" — a magia Desejo + cadeia completa (17 magias) entraram OK, mas a última mensagem da IA não apareceu no chat, só no logcat.
- **CAUSA RAIZ (por trace, não suposição):** logcat mostra a resposta final indo até `[P4] escrevendo no chat` → `[P5]` → `[P6] FIM ok` — a escrita "funcionava". Existe UMA só bolha placeholder (uid=assistantUid). Em cadeia longa o modelo respondeu texto DUAS vezes (22:00:07 "Perfeito! Vamos analisar..." na 1ª iteração; 22:01:50 "Tudo certo!" no fim) e entre elas o loop injetou mensagens [SISTEMA]. `atualizarMsgAssistente(uid)` sobrescrevia SEMPRE a mesma bolha — que ficou ACIMA das [SISTEMA] — então a resposta final substituía silenciosamente a 1ª no topo e não aparecia no fim do chat. (Não era a classe de bug do ReDoS/parse dos Lotes 152-157 — aquele estava resolvido; este é da bolha única em loop multi-resposta.)
- **Correção:** novo `finalizarMsgAssistente(uid)` em `FichaIADelegate.kt`: se a bolha-alvo ainda é a ÚLTIMA da lista, atualiza no lugar (caso simples); se já há mensagens depois dela (loop injetou [SISTEMA]/texto), anexa a resposta final como NOVA bolha no fim (uid próprio do construtor, sem colidir com a bolha velha). O write final em `processarRespostaIAInterno` passa a usar `finalizarMsgAssistente` em vez de `atualizarMsgAssistente`.
- **Verificação:** clean build OK; `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5.
- **Próximo:** teste de device — repetir a cadeia Desejo no modo Analisar e confirmar que a mensagem de fechamento aparece NO FIM do chat, abaixo das [SISTEMA].

### Lote 166: Agrupa mensagens [SISTEMA] numa bolha única atualizável - CONCLUÍDO
- **Motivo (decisão do usuário):** em cadeia longa, cada item aplicado virava uma bolha [SISTEMA] separada (N bolhas: 'Magia X adicionada', 'Magia Y adicionada'...) — poluía o chat e foi o que escondia a resposta final (Lote 165). Confirmado que [SISTEMA] NÃO atrapalha o que vai ao modelo (caminho separado: bolha visual ≠ histórico de tool-results enviado).
- **Correção:** `injetarEvento` em `FichaIADelegate.kt` agora agrega — campo `sistemaBatchUid` guarda a bolha [SISTEMA] ativa; se ela ainda é a última do chat, o evento vira mais uma LINHA (bullet) nela; senão abre bolha nova "[SISTEMA] Aplicando à ficha..." com o 1º item. Resposta da IA (`finalizarMsgAssistente`), novo turno (`conversar`) e `limparChat` zeram `sistemaBatchUid` → próximo lote começa bolha nova (não anexa em bolha velha).
- **Verificação:** clean build OK; `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5.
- **Próximo:** teste de device — cadeia Desejo modo Analisar; confirmar UMA bolha [SISTEMA] com todos os itens em linhas + resposta final no fim.

### Lote 167: Bugs do Modelo Racial (rumo ao catálogo de raças) - CONCLUÍDO
- **Contexto:** preparando suporte a raça (cobaia = Anão 35 pts). Usuário criou o Anão na mão e exportou o JSON real (Anao.json) — fonte da verdade do schema do `modeloRacial`. Achados, todos com causa raiz provada (não suposição):
- **Bug 1 — ModeloRacial sem Qualidades/Peculiaridades:** data class `ModeloRacial` (Personagem.kt:924) só tinha vantagens/desvantagens/pericias; o dialog `ModeloRacialDialog` idem. As 2 peculiaridades do Anão (−2 pts) não tinham onde entrar. **Fix:** adicionados campos `qualidades: List<String>` e `peculiaridades: List<String>` (espelha `Personagem.qualidades/peculiaridades`, texto livre), com custo no `custoTotal` igual ao padrão do app (`qualidades.size` +1 cada; `peculiaridades.size * -1`). UI: duas seções texto-livre (campo + botão + lista) no `ModeloRacialDialog`. Os 2 construtores positionais de `ModeloRacial(...)` no dialog viraram named-args (a mudança do data class os quebrava de propósito — compilador pegou). `custoTotal` já é consumido em Personagem.kt:155 → −2 do Anão entra no total automaticamente.
- **Bug 2 — arredondamento do "Resistente":** `calcularCustoResistente` (CharacterRules.kt:491) fazia `15 * 0.33 = 4.95 → floor → 4`; GURPS manda `15 * (1/3) = 5.0 → 5`. Causa: grau x1/3 hardcoded como `0.33f` (e `0.3333f` noutro dialog — Bug 3, inconsistência interna) e persistido no JSON. **Fix:** `calcularCustoResistente` normaliza o grau para a fração EXATA (~1/3 → 1.0/3.0, ~1/2 → 0.5, ~1 → 1.0) antes de `floor` — robusto a fichas já salvas com 0.33. Literais da UI unificados para `1f/3f` (linhas 142, 238 de TraitSpecialRuleComponents.kt) e seleção de chip tolerante (`abs(grau-m)<0.02`) p/ ficha antiga ainda marcar certo.
- **Verificação:** clean build OK (compilou → construtores positionais corrigidos); `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5.
- **Próximo:** teste de device — recriar Anão (agora com as 2 peculiaridades) e confirmar custo 35; conferir Resistente (Comum ×1/3) = 5 pts. Depois: extrair o padrão do JSON → `racas.v1.json` (catálogo de raças, Anão como semente) + schema/prompt da IA.

### Lote 168: Dupla aplicação de autocontrole nas desvantagens - CONCLUÍDO
- **Sintoma (relato do usuário, Avareza):** dialog de seleção mostra -5 (correto: -10 ×0.5 p/ autocontrole 15), mas na lista e ao reabrir a edição mostra -2; o -2 entrava no total da ficha.
- **CAUSA RAIZ (provada via Anao.json + leitura, não suposição):** `calcularCustoDesvantagem` (CharacterRules.kt:277) usava `custoEscolhido` como base do multiplicador de autocontrole p/ tipos não-POR_NIVEL. Mas o dialog PERSISTE em `custoEscolhido` o valor JÁ multiplicado (Anao.json: `custoBase:-10, custoEscolhido:-5, autocontrole:15`). Lista/edição reaplicavam o ×0.5 sobre -5 → -2. **Universal:** qualquer desvantagem `fixo`/`escolha`/`variavel` com autocontrole 6 (×2) ou 15 (×0.5); autocontrole 9/12 não quebrava só porque ×1.5 dava inteiro / ×1.0 é idempotente (ex: Cobiça -15 ac12 ficava -15).
- **Correção:** quando HÁ autocontrole, a base do multiplicador passa a ser o `custoBase` CRU (-10), nunca `custoEscolhido` (pós-autocontrole) — aplica o multiplicador exatamente 1×. Sem autocontrole, mantém `custoEscolhido` (preserva escolha/variável, ex: Intolerância custoBase=-10 mas custoEscolhido=-5 correto). Seguro: autocontrole exige marcador `*` no custo (`usaAutocontroleMental`), que é mutuamente exclusivo com opções de `escolha` → `custoBase` é sempre o valor cru certo quando há autocontrole. Criação (DesvantagemDialogs:167 passa `getCustoBase()`) continua correta.
- **Verificação:** clean build OK; `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5.
- **Próximo:** teste de device — Avareza ac15 deve dar -5 na lista, no total e ao reeditar; conferir Cobiça (ac12) segue -15.

### Lote 169: CRASH ao abrir Perícias/Rolagem (perícia racial) - CONCLUÍDO
- **Sintoma (relato do usuário + logcat):** app fecha sozinho ao clicar na aba Perícias OU Rolagem. FATAL EXCEPTION: `IllegalArgumentException: No enum constant com.gurps.ficha.model.AtributoBase.M` em `Personagem.getPericiasTotais` (Personagem.kt:75) ← `TabPericias`.
- **CAUSA RAIZ (provada pelo stacktrace + Anao.json, não suposição):** `PericiaRacial(nome, diff, baseAtributo, nivelRelativo, custo)`. Em TraitDialogsV2 o construtor era POSICIONAL com args 2/3 trocados: `PericiaRacial(p.nome, p.atributoBase, p.dificuldadeFixa ?: "M", ...)` → `diff` recebia "IQ", `baseAtributo` recebia "M". `periciasTotais` faz `AtributoBase.valueOf(baseAtributo.uppercase())`; "M" não é atributo → exception não tratada derruba o app. Bug pré-existente que só passou a disparar quando perícia racial virou usável (Lote 167, criação do Anão pelo usuário).
- **Correção (2 pontas):** (1) Defensivo em `Personagem.periciasTotais` (Personagem.kt:75): `AtributoBase.valueOf(...)` agora em `runCatching{}.getOrDefault(AtributoBase.DX)` — ficha já salva com dado ruim NÃO crasha mais. (2) Causa: construtor em TraitDialogsV2 reescrito com ARGS NOMEADOS (`nome=`, `diff=p.dificuldadeFixa`, `baseAtributo=p.atributoBase`, ...) — impossível trocar de novo. (`PericiaDefinicao.atributoBase:String="IQ"`, `dificuldadeFixa:String?="M"` confirmados.)
- **Verificação:** clean build OK; `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5.
- **Próximo:** teste de device — abrir Perícias e Rolagem (não pode fechar); recriar perícia racial do Anão e confirmar baseAtributo correto (IQ p/ Comércio, DX p/ Maça/Machado).

### Lote 170: Perícia racial com NH errado (atributo-1 parasita) - CONCLUÍDO
- **Sintoma (relato + print):** Comércio/Maça-Machado raciais do Anão apareciam NH 9 (= atributo 10 − 1) em vez de NH 10 (NR 0 da planilha do livro `[2]-10`).
- **CAUSA RAIZ (provada por leitura, não suposição):** `periciasTotais` (Personagem.kt:89) converte a `PericiaRacial` em `PericiaSelecionada` com `pontosGastos = 1` fixo. Em `PericiaSelecionada.calcularNivel`, o NH = `atributo + calcularBonusPorDificuldade(dif, pontos) + bonusRacial(nivelRelativo) + bonusVantagens`. O `bonusRacial` (nivelRelativo) já estava certo, MAS o termo `calcularBonusPorDificuldade(Média, 1pt)` = **−1** era somado por cima — um −1 parasita: perícia racial (Innate Skill) tem o nível definido DIRETO por atributo+nivelRelativo, não tem "pontos gastos" no sentido normal.
- **Correção:** em `calcularNivel`, detecta perícia racial via `definicaoId.startsWith("racial_")` (id que o próprio `periciasTotais` cria) e zera o `bonus` por pontos só nesse caso. Perícia normal intacta. NH racial = atributo + nivelRelativo (+ vantagens).
- **Verificação:** clean build OK; `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5.
- **Próximo:** teste de device — perícia racial NR 0 deve dar NH = atributo (Comércio IQ10 → NH 10). ATENÇÃO: conferir no dialog "Configurar Bônus Racial" se o nivelRelativo salvo é 0 (custo 2 p/ Média); se estiver −1 (custo 1) ajustar p/ 0 — é dado do usuário, não bug de código.

### Lote 171: Catálogo de raças (racas.v1.json) + loader + resolver + UI - CONCLUÍDO
- **Contexto:** Anão validado nos 35 pts (Anao(2).json confere 100% com o livro). Estratégia acordada: 1 raça (Anão) → testar mecanismo → depois Teste B (texto cru do livro → schema, simulando IA). Formato escolhido: ENXUTO/recalculado.
- **Passo 1 — `app/src/main/assets/racas.v1.json`:** Anão como 1ª semente, formato enxuto (id/nível/autocontrole/modificador/NR; SEM custos — app recalcula via CharacterRules). É também o "schema da IA" do modo híbrido.
- **Passo 2/3 — `domain/loaders/RacaCatalogo.kt`:** modelos RacaDefinicao/RacaTracoRef/RacaPericiaRef + `carregar(context)` (assets.open→Gson, padrão CatalogLoaders) + `resolver(raca, repo)` que casa ids contra `repo.vantagens`/`repo.desvantagens` (id preferido; fallback nome normalizado/fuzzy — o que torna o modo IA viável). Modificador (ex: pele_resistente) resolvido via `modificadoresEspecificos` da própria vantagem do catálogo. Traço não encontrado é PULADO + reportado em `naoResolvidos` (diagnóstico do Teste B). custoTotal/custoFinal recalculam pela regra.
- **Passo 4 — UI:** botão "Carregar Raça do Catálogo" no `ModeloRacialDialog` + AlertDialog listando raças; clicar resolve e popula TODOS os states (atributos+secundários+vant+desv+perícias+qual/pec). Aviso em vermelho se houver `naoResolvidos`.
- **Verificação:** clean build OK; compile-check do loader/resolver OK; `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5.
- **Próximo:** TESTE A (device) — abrir Modelo Racial → "Carregar Raça do Catálogo" → Anão → confirmar custo 35 e nenhum "Não resolvidos". Se passar: TESTE B — usuário cola texto cru de outra raça do livro → eu gero schema (simula IA) → adiciona ao racas.v1.json → confere custo no app.

### Lote 172: TESTE A falhou parcial — Resistente 0 / Intolerância -10 + bug descrição não exibida - CONCLUÍDO
- **Sintoma (Teste A no device):** Carregar Anão do catálogo deu Resistente 0 pts (devia 5) e Intolerância -10 (devia -5). Além disso, a descrição ("Veneno"/"Inimigos Raciais") não aparecia na frente do nome na lista — bug pré-existente da UI, não desse modelo.
- **CAUSA RAIZ (provada vs Anao(2).json, não suposição):** erro de DADO do meu racas.v1.json + resolver, não do app. (1) Resistente: custo vem de raridade×grau (calculado pelo app igual ao dialog); o resolver usava `custoEscolhido` fallback = catálogo getCustoBase = 0 (Resistente é `variavel`, custo "0" no catálogo). (2) Intolerância (`escolha`): catálogo getCustoBase = -10, mas a opção do livro é -5; eu não informei `custoEscolhido` no JSON.
- **Correção:** (1) Resolver: quando há `metadados` de resistente (raridade/grau), calcula `custoEscolhido` via `CharacterRules.calcularCustoResistente` — a IA/catálogo só fornece raridade/grau do texto do livro, SEM custo hardcoded (sustentável p/ modo IA). (2) `racas.v1.json`: Intolerância ganhou `"custoEscolhido": -5` (o valor da opção vem do texto do livro "[-5]"). (3) Bug UI pré-existente: `ItemTraitRacial` da listagem só passava `v.nome`/`d.nome`; agora `"$nome ($descricao)"` quando há descrição (vant+desv raciais).
- **Verificação:** clean build OK; `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5.
- **Próximo:** repetir TESTE A no device — Anão do catálogo deve fechar 35 com Resistente 5, Intolerância -5, e "(Veneno)"/"(Inimigos Raciais)" visíveis. Se ok → TESTE B.

### Lote 173: TESTE B (Centauro) — limitação de Tamanho na ST + lacunas do catálogo - CONCLUÍDO
- **Contexto:** TESTE A passou (Anão = 35 no device, screenshots). Iniciado TESTE B: usuário passou texto cru do Centauro (Cataclismo, 100 pts). Eu gero o schema só com o texto (simula IA).
- **Achado 1 — ST com limitação % não existia no app:** `ModeloRacial.custoTotal` fazia `modForca*10` puro; não havia como representar "ST+8 (Tamanho, -10%) [72]". GURPS p.19/B262: ST pode ter limitação % (Tamanho −10%×ModTam até −80%; Manuseadores Precários −40%). **Fix (decisão do usuário: campo de %):** `ModeloRacial` ganhou `modForcaLimitacaoPct: Int = 0`; custoTotal calcula ST = `floor(modForca*10 * (1+pct/100))` (pct=0 → idêntico ao anterior, sem regressão). `RacaDefinicao.stLimitacaoPct` + resolver propaga. UI: linha "Limitação ST" no card de atributos (passo 10%, 0..−80) + carrega/salva o campo (2 construtores named-arg + handler do catálogo). Centauro ST+8 −10% = 80×0.9 = **72** ✅.
- **Achado 2 — "Cascos" não é vantagem solta:** grep deu 0; usuário indicou que é OPÇÃO da vantagem `garras` (costKind choice; Cascos = 3 pts, confirmado no dialog). Schema corrigido: `{id:"garras", descricao:"Cascos", custoEscolhido:3}`. (Valor da lição p/ modo IA: a IA precisará saber que sub-traços viram opção de uma vantagem-mãe.)
- **Centauro adicionado ao racas.v1.json.** Conta confere 100 pts exatos (72−20+20+10+5+3+20+2+5−5−10−2). MT+1 é descritivo (sem custo, vai na descrição).
- **Verificação:** clean build OK; `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5.
- **PENDENTE (pedido do usuário):** criar a limitação "Tamanho" em `assets/modificadores.v1.json` com uso EXCLUSIVO de ST. Fazer depois. → RESOLVIDO no Lote 174 (de forma diferente: NÃO foi p/ modificadores.v1.json — ver abaixo).
- **Próximo:** TESTE B no device — carregar Centauro do catálogo, confirmar 100 pts, ST mostrando 72 (limitação −10%), Garras=Cascos 3, e nenhum "Não resolvidos".

### Lote 174: Limitação de atributo genérica (Tamanho ST/PV; Manuseadores ST/DX) - CONCLUÍDO
- **Decisão de design (com o usuário):** o usuário perguntou onde pôr a limitação sem poluir/esconder. Conclusão: `modificadores.v1.json` é catálogo de modificador de VANTAGEM (poluiria toda vantagem e nem se aplica a atributo) — NÃO usar. Limitações são poucas e fixas → hardcoded como enum. Escopo escolhido: SÓ Modelo Racial. UI escolhida: botão "+ Limitação de Atributo" + chips (vazio = nada na tela).
- **Implementação:** enum `AtributoLimitavel{ST,DX,PV}` + enum `TipoLimitacaoAtributo` (TAMANHO→ST/PV; MANUSEADORES_PRECARIOS→ST/DX, com `aceitaEm`) + `data class LimitacaoAtributo`. `ModeloRacial`: trocado `modForcaLimitacaoPct:Int` (Lote 173, só ST) por `limitacoesAtributo: List<LimitacaoAtributo>` (genérico ST/DX/PV); `custoTotal` aplica % por atributo via `custoComLimite` (floor; soma piso −80; vazio = idêntico ao anterior, sem regressão). Resolver (`RacaCatalogo`): `RacaLimitacaoRef` + mapeia com enum tolerante, rejeita combinação inválida p/ `naoResolvidos`. `racas.v1.json`: Centauro migrado `stLimitacaoPct:-10` → `limitacoesAtributo:[{ST,TAMANHO,-10}]`. UI: botão + chips removíveis + AlertDialog (tipo→atributos válidos→%); state migrado nos 2 construtores named-arg + handler do catálogo.
- **Verificação:** clean build OK; `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5.
- **Próximo:** TESTE B no device — Centauro do catálogo = 100 pts, chip "ST: Tamanho -10%", ST custando 72, sem "Não resolvidos". Conferir Anão (sem limitação) segue 35 (regressão).

### Lote 175: Garras no catálogo via metadados.tipoGarras (forma correta) - CONCLUÍDO
- **Achado (usuário corrigiu no device + trouxe Centauro.json):** eu havia posto Garras como `{id:garras, descricao:"Cascos", custoEscolhido:3}`. O custo até batia por acaso, mas a forma CORRETA que o app usa (confirmado em VantagemDialogs.kt:253 `"garras"->mapOf("tipoGarras" to ...)` e GarrasRule.kt:20-23 `metadados["tipoGarras"]; "cascos"->3`) é `metadados:{tipoGarras:"cascos"}` — o app DERIVA o custo 3 da regra modular, sem custo hardcoded. Centauro.json salvo confirmou: `{definicaoId:garras, custoBase:3, metadados:{tipoGarras:"cascos"}, tipoCusto:escolha}`.
- **Correção:** `racas.v1.json` Garra do Centauro → `{id:"garras", metadados:{tipoGarras:"cascos"}}` (removido `descricao`/`custoEscolhido` manuais). Resolver já propaga `metadados` (RacaCatalogo:163) → `calcularCustoVantagem` → `GarrasRule` deriva 3. Nenhuma mudança de código necessária, só o dado do catálogo.
- **REGRA P/ MODO IA (importante):** vantagem tipo "escolha" com opções nomeadas (Garras: cascos/garras_cegas/afiadas/pontudas...) deve usar `metadados:{<chave>:<opcao>}` (chave por vantagem: `tipoGarras`, etc.), NÃO `custoEscolhido` cru. O app deriva o custo pela regra modular. Isso vai pro prompt da IA quando chegar a fase IA.
- **Verificação:** clean build OK; `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5.
- **Próximo:** TESTE B no device (mesmo de antes) — Centauro 100 pts, Garras=Cascos 3 via metadados.

### Lote 176: forjador_buscar_catalogo expõe schema das regras especiais - CONCLUÍDO
- **Problema (apontado pelo usuário):** a tool de busca só retornava "id | nome | custo | tipoCusto". Traços com regra especial (Aliado, Inimigo, Vício, Dependência, Reputação, Dever, Garras, Resistente, ~40 specialRule) têm o custo derivado de CAMPOS de `metadados` — o modelo ficava CEGO a eles e CHUTAVA (exatamente o que aconteceu com Garras, Lote 175). Problema sistêmico, raiz da fase IA.
- **Correção:** novo `domain/tools/RegrasEspeciaisSchema.kt` — mapa specialRule/id → schema dos metadados, EXTRAÍDO da fonte de verdade (CharacterRules.calcularCusto* linhas 320-407: inimigos/dependencia/reputacao/dever/dor_cronica/fraqueza/vulnerabilidade/manutencao/vicio + aliados/contatos via VantagemDialogs:266-267 + garras/resistente regras modulares). `buscarCatalogo` (vantagem+desvantagem) agora anexa "⚙ REGRA ESPECIAL — <campos/valores válidos/fórmula>" quando há specialRule ou id modular. O modelo deixa de adivinhar.
- **Decisão do usuário:** atacar a raiz (enriquecer a busca), não só o que o teste de raça precisa.
- **Verificação:** clean build OK; `test_forjador_complexo.py` 19/19; `test_json_repair.py` 5/5.
- **Próximo:** TESTE B device (Centauro 100, Garras=Cascos via metadados). Fase IA futura usará esse schema no prompt p/ a IA preencher regras especiais sem chutar.

### Lote 177: TESTE B — Elfo (41 pts) - CONCLUÍDO (aguarda device)
- **Contexto:** Centauro validado no device (100 pts, info correta — Teste B do Centauro PASSOU). 3ª raça: Elfo (Cataclismo 189, 41 pts), gerado só do texto cru (simula IA).
- **Achados (a IA enfrentaria os mesmos — análise estática antes de escrever, lição do Lote 175):** (1) "Atraente" não é vantagem solta → id `aparencia` (choice, opções 4/12/16/20; Atraente=4). (2) "Arco" → perícia id `arco` nome "Arcos", DX, dificuldadeFixa M; bônus racial +1 → calcularCustoPericiaRacial("M",1)=2 ✅ (1º teste com NR≠0). (3) Aptidão Mágica 0 → `aptidao_magica`, loader força TipoCusto.POR_NIVEL (CatalogLoaders:557) + caso especial em calcularCustoVantagem `5+(nivel-1)*10` → nível 1 = 5 ✅ (1º teste de costKind special). (4) Código de Honra/Senso do Dever = escolha, valor do livro via custoEscolhido (padrão Intolerância).
- **Conta confere 41:** −10(ST)+20(DX)+20(IQ)+5(AptMágica)+5(Artista)+4(Atraente)+5(Hab.Musical)+15(Idade Imutável)−10(Cód.Honra)−15(Senso Dever)+2(Arco NR+1) = 41.
- **Elfo adicionado ao racas.v1.json.** Verificação: clean build OK; 19/19; 5/5.
### Lote 178: Dois sistemas de perícia racial (Bônus p.453 vs Concedida p.454) - CONCLUÍDO
- **Achado (usuário foi à fonte primária — Módulo Básico p.453-454 + Cataclismo p.189 via PDF):** "+1 em Arco [2]" do Elfo dava custo 4 (errado). Causa NÃO era ambiguidade do livro nem bug de conta — o app só modelava UM dos DOIS sistemas GURPS de perícia racial:
  - **CONCEDIDA (p.454):** raça já sabe a perícia; custo pela Tabela p.170 (dificuldade importa). Ex.: Anão "Comércio (M) IQ [2]-10". (era o único que o app tinha)
  - **BÔNUS (p.453):** só um dom/+N no NH ao usar a perícia; NÃO concede a perícia; tabela LINEAR `+1=2, +2=4, +3=6` (máx +3), independente da dificuldade. Ex.: Elfo "+1 em Arco [2]". (NÃO existia no app → "+1 Arco" caía na tabela p.170 e dava 4)
- **Implementação:** `enum TipoPericiaRacial{CONCEDIDA,BONUS}` + campo `tipo` em `PericiaRacial`. `CharacterRules.calcularCustoBonusPericiaRacial(N)= N.coerceIn(1,3)*2` (p.453). Resolver (`RacaCatalogo`): `RacaPericiaRef.tipo`, calcula custo conforme o tipo. `periciasTotais` lista só CONCEDIDA (BONUS não vira perícia própria — p.453 "não concede a perícia"); `calcularNivel` já somava o bônus por nome → cobre os dois. `racas.v1.json`: Arco do Elfo → `tipo:"BONUS", nivelRelativo:1` → custo 2.
- **Conta Elfo = 41 exatos** (Arco BONUS = 2). Anão/Centauro intactos (default CONCEDIDA).
- **REGRA P/ MODO IA:** "+N em <perícia> [N×2]" no texto do livro = BÔNUS (p.453); "<Perícia> (Dif) Attr [pts]-NH" = CONCEDIDA (p.454). Distinção pelo formato do texto.
- **Verificação:** clean build OK; 19/19; 5/5.
### Lote 179: UI do ModeloRacialDialog — seletor Concedida/Bônus na perícia racial - CONCLUÍDO
- **Buraco apontado pelo usuário:** Lote 178 corrigiu motor+catálogo, mas a UI manual ficou pra trás. O dialog de configurar perícia racial só fazia CONCEDIDA (sempre `calcularCustoPericiaRacial`, sem campo tipo) → jogador que montasse "+1 em Arco" Bônus na mão obteria 4 pts (errado) e Arco listado como perícia própria. Catálogo funcionava só porque o JSON traz `tipo`.
- **Correção:** dialog "Perícia Racial" agora tem FilterChip Concedida|Bônus com explicação curta de cada (p.454 vs p.453); custo recalcula ao vivo conforme o tipo (`calcularCustoBonusPericiaRacial` p/ BONUS, `calcularCustoPericiaRacial` p/ CONCEDIDA); o `tipo` é gravado na `PericiaRacial`. Listagem mostra "[Bônus]" + "+N no NH (só ao usar)" vs "Attr+NR (Dif)"; título corrigido ("Perícias Raciais", não "(Bônus)").
- **Verificação:** clean build OK; 19/19; 5/5.
- **Próximo:** TESTE B device — Elfo 41 (Arco BONUS custo 2, +1 NH só ao usar, NÃO vira perícia própria); criar perícia racial MANUAL e conferir o seletor Concedida/Bônus muda o custo certo. Anão 35 / Centauro 100 = regressão.

### Lote 180: +3 raças do Cataclismo p.190 (Elfos Negros, Meio-Elfos, Elfos do Mar) - CONCLUÍDO
- **Processo definido com usuário:** ele manda texto LIMPO em .md (sala de criação/raças.md); eu monto conferindo nº a nº + valido ids no catálogo antes de escrever (lição Lote 175); paro e pergunto se engasgar. PDF cru é inviável (2-3 colunas intercaladas → custo errado silencioso).
- **3 raças adicionadas ao racas.v1.json** (pág. "Cataclismo 190", confirmada pelo usuário). Ids todos validados no catálogo (insensivel, obsessao, nt_baixo, anfibio, membrana_nictitante, resistencia_a_pressao, perícia rede=D/DX, aptidao_magica special, aparencia choice Atraente=4):
  - **Elfos Negros 31:** −10+20+20 +5(AptMág)+4(Atraente)+15(IdadeImut) −5(Insensível)−10(Intol.Total)−10(Obsessão ac12) +2(Arco BONUS) = 31 ✅
  - **Meio-Elfos 27:** +20(IQ) +5(AptMág)+2(Expect.Vida) = 27 ✅
  - **Elfos do Mar 51:** +20+20 +10(Anfíbio)+5(AptMág)+5(Artista)+4(Atraente)+15(IdadeImut)+1(Membrana)+5(Resist.Pressão) −10(Cód.Honra)−10(NT Baixo 2 = perLevel-5×2)−15(Senso Dever) −1(pec) +2(Rede BONUS) = 51 ✅
- **Confirmado por leitura:** resolver passa `nivel` p/ desvantagem + `calcularCustoDesvantagem` faz POR_NIVEL=custoBase*nivel → NT Baixo 2 = −10 sem custoEscolhido. Rede "+1 [2]" = BONUS (p.453, ignora dificuldade D).
- **Verificação:** JSON válido; clean build OK; 19/19; 5/5.
- **Próximo:** TESTE B device — carregar as 3, conferir 31/27/51, sem "Não resolvidos". Regressão: Anão 35, Centauro 100, Elfo 41.

### Lote 181: +7 raças Cataclismo p.190-194 (Esfinges x2, Gárgula, Gigante, Gnomo, Goblin, Hobgoblin, Homem-Tubarão) - CONCLUÍDO
- **Processo:** texto limpo .md do usuário; ids resolvidos via script de match exato por nome (não chute); avisei o usuário dos casos novos ANTES de escrever.
- **Espírito do Manancial PULADO** (decisão do usuário): Metacaracterística Espírito [261] não existe no app + mods compostos + Dependência [-120]. Vira lote próprio futuro (modelar Metacaracterística).
- **Casos novos resolvidos por análise (não chute):** Garras Afiadas/Cegas = opção de `garras` via metadados.tipoGarras (garras_afiadas/garras_cegas); Presas = vantagem `dentes` choice opção 2; Pobre = `riqueza` choice -15; Feio = `aparencia`(desv) choice -8; Hábito Detestável = `habitos_detestaveis`; Não Respira (Guelras) custoEscolhido 10.
- **⚠ AVISO AO USUÁRIO — Gigante (122) NÃO fecha:** `Hipoalgia` NÃO existe no catálogo de vantagens. Gigante entra com aviso "Não resolvido: hipoalgia", custo ~112 (falta 10). Decisão pendente: adicionar Hipoalgia ao catálogo ou tratar de outro jeito. As outras 6 devem fechar (Esfinge Leonina 170, Tigrina 140, Gárgula 5, Gnomo -7, Goblin 19, Hobgoblin -15, Homem-Tubarão 145).
- **Verificação:** JSON válido (14 raças total); clean build OK; 19/19; 5/5.
- **Próximo:** TESTE B device — carregar as 7; conferir totais; Gigante vai dar aviso Hipoalgia (esperado). Decidir o que fazer com Hipoalgia.

### Lote 182: Gigante — Hipoalgia id corrigido (fecha 122) - CONCLUÍDO
- **Resolução do aviso do Lote 181:** o usuário corrigiu/confirmou o id real no catálogo: `hipoalgia_alto_limiar_de_dor` (não `hipoalgia`). É `costKind:fixed, fixed:10` = bate com livro "Hipoalgia [10]". racas.v1.json Gigante atualizado.
- **Bônus:** confirmado que `pele_resistente` (valor -40) existe em `modificadores_especificos` de `resistencia_a_dano` (chave snake_case; data class lê via @SerializedName alternate — por isso Anão sempre fechou). Falso alarme de leitura do meu script Python, app está correto.
- **Conta Gigante = 122:** ST+15 Tam-20% (150×0.8=120) −20(DX) +20(HT) −5(Vel-0,25) +20(Desl.Ampl) +10(Hipoalgia) +6(Paladar3=2×3) +6(RD2 Pele Resist=10×0.6) +1(Voz Penetrante qual) −10−10−10−5(desv) −1(pec) = 122 ✅.
- **Verificação:** JSON válido; clean build OK; 19/19; 5/5.
- **Próximo:** TESTE B device — 7 raças do Lote 181 (agora Gigante deve fechar 122, SEM aviso). Regressão das 7 anteriores. Espírito do Manancial segue pendente (lote futuro: Metacaracterística).

### Lote 183: Metacaracterísticas reusando ModeloRacial (sem over-engineering) - CONCLUÍDO
- **Correção de rumo (usuário cortou over-engineering):** eu estava criando catálogo separado + Room novo + migration. Usuário apontou: ModeloRacial JÁ salva na ficha; meta é o MESMO pacote, só muda nome e onde grava. GURPS p.262 confirma: "funciona quase da mesma maneira que vantagem/desvantagem"; "anote a metacaracterística, NÃO seus componentes"; "Mestre pode modificar os elementos, alterando o custo".
- **Decisões do usuário:** dialog renomeado "Raça e Metacaracterísticas"; ao Salvar → escolhe Raça (ficha, como hoje) ou Metacaracterística (reutilizável); meta guarda componentes (reabrir/editar); embutida numa raça = 1 item de custo único (não expande).
- **Implementação enxuta (sem Room/migration):** `ModeloRacial` ganhou `tipo: TipoModeloRacial{RACA,METACARACTERISTICA}` + `metacaracteristicas: List<MetacaracteristicaRef>` (id/nome/custo/desc, soma 1× no custoTotal). Storage LEVE: `MetacaracteristicaStore` grava lista de ModeloRacial(tipo=META) em `filesDir/metacaracteristicas_usuario.json` (padrão filesDir já usado p/ maps; zero migration, zero FichaDatabase). UI: título novo; botão "Salvar" abre AlertDialog Raça|Metacaracterística; botão "Adicionar Metacaracterística" + chips removíveis; seletor lista metas salvas (clica → vira MetacaracteristicaRef com custoTotal). Construtores ModeloRacial todos named-arg (RacaCatalogo:209 incluso) — campos novos default, sem regressão.
- **Verificação:** clean build OK; 19/19; 5/5.
- **Próximo:** TESTE B device — montar um conjunto, Salvar→Metacaracterística (confirmar grava), reabrir Raça e "Adicionar Metacaracterística" (chip +custo). Depois isso destrava Espírito do Manancial (Metacaracterística Espírito 261 como item). Regressão: 14 raças do catálogo seguem fechando.

### Lote 184: Catálogo PRONTO de metacaracterísticas (Espírito, Entidade Astral, Máquina) - CONCLUÍDO
- **Decisão do usuário:** as 3 do livro (Módulo Básico p.263) são PRONTAS → asset read-only, igual racas.v1.json (não filesDir, que é do usuário). Conferido que a soma dos componentes bate o total: Espírito 15+30+128+38+10+20+20=261; Entidade Astral 15+30+40+36+10+20+20=171; Máquina 30+25−30=25.
- **Implementação:** `assets/metacaracteristicas.v1.json` (id/nome/custo/pagina/descricao/componentes — componentes só texto informativo, GURPS p.262 "anote a meta, não os componentes") + `MetacaracteristicaCatalogo.kt` loader (mesmo padrão de RacaCatalogo). UI: `catalogoMetas` carregado; botão "Adicionar Metacaracterística" aparece se há catálogo OU salvas; seletor mostra 2 seções: "Do livro" (catálogo, custo fixo) + "Minhas" (filesDir do Lote 183). Clicar → MetacaracteristicaRef com o custo (1 item, não expande).
- **Verificação:** JSON válido (3 metas); clean build OK; 19/19; 5/5.
- **Próximo:** TESTE device — abrir Raça e Metacaracterísticas → "Adicionar Metacaracterística" → secção "Do livro" lista Espírito 261/Astral 171/Máquina 25 → clicar vira chip somando no custo. Isso destrava o Espírito do Manancial (raça pendente: agora "Metacaracterística Espírito [261]" = adicionar a meta Espírito do catálogo).

### Lote 185: Metacaracterística editável (componentes estruturados + dialog recursivo) - CONCLUÍDO
- **Pedido do usuário:** clicar na metacaracterística e EDITAR os traços (GURPS p.262 "modificar os elementos, alterando o custo"), não só apagar; e ver todos os traços que ela tem. Usuário adicionou no modificadores.v1.json os mods que faltavam (corrigiu id duplicado mod_normalmente_ativa → _ampliação/_limitação; add mod_somente_materia -10%, mod_sempre_ativa_50 -50%); confirmou Imunidade a Danos ao Metabolismo = vantagem `resistente` Muito Comum ×1 = 30.
- **3 partes de código:** (1) `RacaCatalogo.resolver` ganhou FALLBACK: modificador não achado em `def.modificadoresEspecificos` é buscado no catálogo GLOBAL `repo.modificadoresGerais` (modificadores.v1.json) por id/nome — necessário p/ Insubstancialidade+"Afeta a Matéria". (2) `metacaracteristicas.v1.json` reestruturado: componentes = vantagens/desvantagens com ids reais (formato de raça), não string; `MetacaracteristicaCatalogo.carregar(ctx,repo)` resolve cada uma num ModeloRacial(tipo=META) reusando RacaCatalogo.resolver. (3) `MetacaracteristicaRef` agora carrega `conteudo: ModeloRacial` (custo = conteudo.custoTotal, recalcula); UI: meta vira ItemTraitRacial (toque=editar via ModeloRacialDialog RECURSIVO, lixeira=remover).
- **Contas conferidas (componentes):** Espírito 15+30+128+38+10+20+20=261; Entidade Astral 15+30+40+36+10+20+20=171; Máquina 30+5+20−30=25. Insubst 80×(1+1−0,4)=128; Invis 40×(1−0,1+0,05)=38; etc.
- **Verificação:** JSON válido; clean build OK; 19/19; 5/5.
- **Próximo:** TESTE device — "Adicionar Metacaracterística" → Espírito; conferir chip 261; TOCAR nela → abre dialog recursivo com os 7 componentes (Idade Imutável, Resistente=Imunidade Metabolismo 30, Insubstancialidade 128, Invisibilidade 38, Não Come/Dorme/Respira); editar um componente e ver custo recalcular. Astral=171, Máquina=25. Sem "Não resolvidos".

### Lote 186: Nome da raça mostra metacaracterística entre parênteses - CONCLUÍDO
- **Pedido do usuário:** ao selecionar metacaracterística, o "Nome da Raça" deve virar "Humano (Espírito)" / "Anão (Entidade Astral)".
- **Implementação (derivado, não destrutivo):** `nomeEfetivo()` = nome-base + "(Meta1, Meta2)" quando há metacaracterísticas; regex remove "(...)" antigo do fim p/ não acumular ao reabrir. `montarModelo` salva `nomeEfetivo()` (campo `nome` editável continua só o base — remover a meta limpa o parêntese automaticamente, sem prender texto). UI: abaixo do campo "Nome da Raça" aparece "Será salvo como: <nomeEfetivo>" quando há meta. `tempModelo` (só custo na tela) intacto — nome irrelevante ali.
- **Verificação:** clean build OK; 19/19; 5/5.
- **Pendente:** usuário disse "DUAS COISAS IMPORTANTES" mas só descreveu a 1ª (esta). 2ª coisa ainda não informada — perguntar.
- **Próximo:** TESTE device — adicionar meta → conferir "Nome da Raça" / "Será salvo como" mostra "Base (Meta)"; remover meta → parêntese some; salvar e ver na ficha.

### Lote 187: Botão da aba Traços renomeado p/ "Raça e Metacaracterísticas" - CONCLUÍDO
- **Pedido:** o botão "Modelo Racial (Humano)" na aba Traços deve se chamar "Raça e Metacaracterísticas".
- **Correção:** TabTracos.kt:66 `texto = "Raça e Metacaracterísticas (${p.modeloRacial.nome})"` (mantém o nome entre parênteses — que já vem com a metacaracterística via Lote 186, ex: "Humano (Espírito)"). TabGeral.kt:331 (linha do resumo de pontos "Modelo Racial (nome)") deixado como está — é rótulo do somatório de pontos, não o botão.
- **Verificação:** clean build OK; 19/19; 5/5.
- **Próximo:** TESTE device — botão da aba Traços agora "Raça e Metacaracterísticas (<nome>)". Junto: testes pendentes dos Lotes 185/186 (meta editável recursiva; nome com parêntese).

### Lote 188: +9 metacaracterísticas do Módulo Básico (catálogo 3→12) - CONCLUÍDO
- **Processo:** usuário pôs as 18 do MB no metacaracterisca.md; eu mapeei TODOS os componentes vs catálogo via script (não chute), avisei ausências, montei só as que fecham. Usuário adicionou Matemático Intuitivo (matematico_intuitivo, fixed 5) e Características Proibidas (caracteristicas_proibidas, 0) ao catálogo.
- **9 adicionadas e conferidas (conta bate exata):** Corpo de Água 175, Corpo de Gelo 99, Corpo de Metal 175, Corpo de Pedra 140, Corpo de Terra 175, Quadrúpede -35, Animal Doméstico -30, Animal Selvagem -30, Autômato -85. Padrões reusados: ImunMetab = `resistente` MuitoComum×1 (Lote 185); Tol.Ferim "Homog,SemSangue"=_homogeneo(40)+_sem_sangue(5); RD=resistencia_a_dano perLevel5; invertebrado=desv -20; Indiferente(6)=autocontrole; custoEscolhido do livro p/ specialRule/choice (Fraqueza/Vulnerab/Fragilidade/Estigma/Bestial — padrão Lotes 172/177).
- **🛑 PENDENTE — faltam no catálogo (usuário adiciona, eu fecho depois):** (1) "Sem Pernas" com variantes Aquático[0]/Desliza[0]/Esteiras-Rodas[-20] → trava Ictioide -50, Vermiforme -35, Veículo Terrestre -100. (2) "Reprogramável" [-10] → trava só IA 32 (Matemático Intuitivo já ok). Essas 4 metas NÃO foram adicionadas.
- **Verificação:** JSON válido (12 metas); clean build OK; 19/19; 5/5.
- **Próximo:** TESTE device — carregar as 9 metas novas (totais acima); usuário adiciona Sem Pernas + Reprogramável p/ destravar as 4 restantes.

### Lote 189: Últimas 4 metacaracterísticas — catálogo COMPLETO (16, todo o Módulo Básico) - CONCLUÍDO
- **Correção minha:** eu errei o levantamento do Lote 188 — `reprogramavel` (DESV fixed -10) e `sem_pernas` (DESV choice [-30,-20,-10,-5]) JÁ existiam; minha busca por nome exato falhou (encoding). Usuário apontou reprogramavel; reconferi por id e achei os dois.
- **Detalhe Sem Pernas:** catálogo não tem opção [0]. Ictioide/Vermiforme usam "Sem Pernas (Aquático/Desliza) [0]" → `custoEscolhido: 0` (resolver respeita 0 pois é Int? não-null, `?:` não cai no fallback — confirmado por leitura RacaCatalogo:197). Veículo Terrestre "Esteiras/Rodas [-20]" → custoEscolhido -20.
- **4 adicionadas (conta exata):** Ictioide -50, Vermiforme -35, Veículo Terrestre -100, IA 32. Catálogo metacaracteristicas.v1.json = **16 metas (todas as do Módulo Básico)**.
- **Verificação:** JSON válido (16); clean build OK; 19/19; 5/5.
- **Próximo:** TESTE device — as 16 metas; foco nas 4 novas (Ictioide -50, Vermiforme -35, Veículo Terrestre -100, IA 32) sem "Não resolvidos". Catálogo de metacaracterísticas COMPLETO.

### Lote 190: Telecomunicação — regra modular (3 opções nomeadas) - CONCLUÍDO
- **Diagnóstico (print do usuário):** dialog de Telecomunicação mostrava "Custo Variável -1/30/+1" genérico (não as 3 opções). Causa: `specialRule:"telecomunicacao"` declarada no JSON sem implementação no CharacterRules. Padrão de Garras/Resistente (regra modular) é o caminho consistente — usuário escolheu.
- **Implementação:** novo `TelecomunicacaoRule.kt` em `domain/rules/traits/` (espelha GarrasRule): lê `metadados.tipoTelecomunicacao` → laser=15, diapsiquia=30, radio=10; aplica modificadores % por cima (ceil, piso -80). Registrado em `TraitRuleRegistry`. UI: `TelecomunicacaoConfig` em TraitSpecialRuleComponents (3 cards radio) + state `tipoTelecomunicacao` em `VantagemDialogs` (criação + edição) + ligado nos 3 sites (when por id criação 438, when por id criação 507, when por specialRule edição 828) + LaunchedEffects (cálculo de custo nos 2 dialogs) + load metadados na edição (669).
- **Atenção (usuário corrigiu):** removi "Infravermelho" — o livro/livro do usuário tem só 3 opções (Laser/Diapsiquia/Rádio), não 4.
- **Desbloqueia:** Homens-Inseto/Guerreiros Insetos do raças.md (têm "Diapsiquia (Transmissão Aberta +50%; Racial -20%) [39]"); 30×1,3=39 ✅. Ainda falta "Sem Atrativos" -4 no catálogo p/ fechar essas raças. Medusas (139) segue pendente decisão.
- **Verificação:** clean build OK; 19/19; 5/5.
- **Próximo:** TESTE device — abrir Telecomunicação: 3 cards "Comunicação a Laser/Diapsiquia/Rádio" com custo certo; criar uma Diapsiquia com modificadores Transmissão Aberta+50% e Racial-20% deve dar 39. Depois usuário add Sem Atrativos e eu monto Kobolds/Homens-Inseto/Guerreiros Insetos.

### Lote 191: Modificador ganha campo livre de Descrição/Especificação - CONCLUÍDO
- **Pedido (usuário):** modificadores como Cíclico exigem que o jogador especifique uma condição (ex: "interrompido por cuidados médicos"). O app não permitia.
- **Diagnóstico:** modelo `ModificadorSelecao` JÁ tinha `descricao: String?`; UI (`ModificadorSelecionadoItem`) só mostrava nome/%/níveis/lixeira, sem editar. Faltou só a UI.
- **Correção:** `ModificadorSelecionadoItem` (TraitCommonComponents.kt:122) virou Card com Column: linha 1 (nome/%/níveis/lixeira como antes) + linha 2 OutlinedTextField "Descrição/Especificação (opcional)" ligado a `mod.descricao` via `onUpdate(mod.copy(descricao=it))`. Decisão (usuário): campo livre em TODOS os modificadores (não só uns específicos) — mais simples e cobre qualquer caso.
- **Sem ajuste no resolver:** descrição é só texto livre, não afeta custo. Persistência via `ModificadorSelecao.descricao` já existente.
- **Verificação:** clean build OK; 19/19; 5/5.
- **Próximo:** TESTE device — adicionar um modificador (ex: Cíclico no Ataque Tóxico) → campo "Descrição/Especificação" aparece editável; texto persiste ao salvar e reabrir.



### Lote 192: +3 raças Cataclismo p.196 (Kobolds, Homens-Inseto, Guerreiros Insetos) - PARCIAL
- **Adicionadas 3 raças ao racas.v1.json** (catálogo 14→17). Ids/contas conferidas via script.
- **Kobolds (-60) ✅ FECHA EXATO:** −20+20−40−5 +2(Consumo Reduzido 2 c/ Estômago de Ferro=2*2*(1-0.5)) +5(Resistente Doenças=10×0.5) −15(Desatento ac9=−10×1.5) −5(Estigma Inculto) −2(2 pec) = −60.
- **Homens-Inseto (29) e Guerreiros Insetos (25):** ainda NÃO fecham. Diapsiquia = telecomunicacao tipoDiapsiquia (30) + mods Transmissão Aberta +50% + Racial −20% = 30×1,3=39. `mod_transmissao_aberta` ✅ existe. **`racial` AUSENTE** no modificadores.v1.json — usuário adiciona, fecha automático no próximo build (racas.v1.json já referencia "racial").
- **Confirmações de catálogo:** Memória Racial Passiva = choice 15; Senso Dever Colônia = choice -10; Timidez Suave = choice -5; Sem Atrativos = aparencia(desv) -4; Amigável = choice -5; Desatento ac9 (autocontrole); Expectativa Vida Reduzida 1 nível = choice -10; Resistente a Doenças = resistente (raridade 10 ocasional, grau 0.5 = +8 no teste) = 5.
- **Verificação:** JSON válido (17 raças); clean build OK; 19/19; 5/5.
- **PENDENTE:** adicionar `racial` (limitação -20%) ao modificadores.v1.json — destrava 2 Insetos. Medusas em análise pelo usuário (Ataque Inato + Atribulação).
- **Próximo:** TESTE device — Kobolds fechar -60; Insetos darão custo errado até `racial` ser adicionado.

### Lote 193: Insetos destravados — racial id corrigido (mod_racial) - CONCLUÍDO
- **Resolução do pendente Lote 192:** usuário adicionou ao modificadores.v1.json com id `mod_racial` (com prefixo); eu havia posto `"racial"` no racas.v1.json. Corrigido para `mod_racial`.
- **Insetos fecham EXATO:** Homens-Inseto 29 (10+10 +15+10+10 +**39**(Diapsiquia trans+50% racial-20%) −5−10−25−5−4−10−5 −1); Guerreiros Insetos 25 (30−20+20 +25+20+**39**+15 −10−10−40−25−5−4−10).
- **Catálogo de raças: 17 fechando** — só Espírito do Manancial (lote 181, destravado pela meta Espírito) e Medusas (em análise pelo usuário) pendentes.
- **Verificação:** JSON válido (17 raças); clean build OK; 19/19; 5/5.
- **Próximo:** TESTE device — Kobolds -60, Homens-Inseto 29, Guerreiros Insetos 25 sem "Não resolvidos". Diapsiquia mostra 39 quando os mods são aplicados.

### Lote 194: Cíclico (5 intervalos) torna-se porNivel=true (regra real do livro) - CONCLUÍDO
- **Achado (usuário trouxe o trecho do livro, MB p.103-104):** o "Cíclico, 1 minuto, +80%" da Medusa NÃO é erro de edição como eu havia respondido — o livro manda **"Multiplique o custo acima pelo número de ciclos após o primeiro"**. 1 minuto = +40% × 2 ciclos = +80% ✅. Eu errei na resposta anterior (tinha dito que "2 ciclos" era descritivo).
- **Bug do app:** todos os 5 Cíclicos (1s/10s/1m/1h/1d) estavam `porNivel:false` → não permitiam multiplicar por ciclos.
- **Correção:** os 5 viraram `porNivel:true` no modificadores.v1.json + descrição esclarece "(use o ajuste de níveis)". UI já suporta níveis (`ModificadorSelecionadoItem` tem +/− níveis quando porNivel=true, vimos no Lote 191). Cálculo no resolver: `valor*niveis` (CharacterRules linha 227/266/311 — código já trata).
- **Validação matemática:** Medusa Ataque Tóxico 4 × (1 + 0 + 0,4×2) = 4 × 1,8 = 7,2 → ceil 8 ✅ — bate com livro [8]! O livro do Cataclismo está consistente com as regras do MB; quem estava errado era eu (e o app).
- **"interrompido por cuidados médicos"** — usuário tinha razão também: puramente descritivo, sem %. Usar campo descrição do modificador (Lote 191).
- **Verificação:** JSON válido; clean build OK; 19/19; 5/5.
- **Próximo:** TESTE device — ao escolher Cíclico (qualquer intervalo) em Ataque Inato, aparecem botões +/− pra ajustar ciclos; nível 2 do Cíclico 1m = +80%. Destrava parte da Medusa (resta Atribulação composta e Golpeadores).

### Lote 195: Modificador com `bonusBase` + `porNivel` (Cone +50% base + 10%/m) - CONCLUÍDO
- **Pedido (usuário, trouxe regra MB):** Cone é "+50% base + 10% por metro de largura". Catálogo tinha valor:+50% porNivel:true → cálculo daria 50×N (Cone 15m=750%) em vez de 50+10×15=200%. Estrutura "valor×níveis" não cobre "base + por nível".
- **Decisão (usuário):** novo campo `bonusBase` no `ModificadorDefinicao`/`Selecao` — modelagem estruturalmente correta.
- **Implementação:** (1) Modelo: `ModificadorDefinicao.bonusBase: Int = 0` (com @SerializedName alternate `bonus_base`) e `ModificadorSelecao.bonusBase: Int = 0` (no FIM do data class p/ não quebrar 6 call sites posicionais existentes). (2) Cálculo em 5 lugares (CharacterRules ×3 + AtaqueInato/Dentes/Garras/Golpeadores/Telecomunicacao Rules): `bonusBase + (if porNivel valor*niveis else valor)`. bonusBase=0 default = comportamento idêntico ao anterior. (3) Catálogo `mod_cone`: bonusBase:50, valor:10, porNivel:true. (4) Propagação no resolver de raça: ModificadorSelecao a partir de modDef recebe `bonusBase = modDef.bonusBase` (4 call sites na UI + 1 no RacaCatalogo global; mods_especificos ficam com 0). (5) **BUG colateral corrigido:** o resolver de raça (catálogo global) hardcodava `porNivel:false` — significava que Cíclico (Lote 194 porNivel:true) NÃO funcionava por nível quando vinha via raça. Agora propaga `ger.porNivel` também.
- **Conta Cone Medusa:** Cone 15m = bonusBase 50 + valor 10 × niveis 15 = **+200%** ✅ (livro [+200%]).
- **Verificação:** clean build OK; 19/19; 5/5.
- **Próximo:** TESTE device — Cone com níveis=15 deve dar +200%; Cíclico via raça (Insetos) também deve respeitar níveis (era bug latente). Destrava Atribulação da Medusa (que tem Cone +200% como 1 dos 6 mods).



### Lote 196: Correção exibição do total do modificador Cone (bonusBase não aparecia no label) - CONCLUÍDO
- **Bug (usuário reportou via screenshot):** ao adicionar o modificador Cone, o item selecionado exibia só `+10% p/ nível` — o `bonusBase` de 50 não aparecia no label. O cálculo de custo (CharacterRules) já estava correto desde o Lote 195; só a exibição estava errada.
- **Causa:** `ModificadorSelecionadoItem` (TraitCommonComponents.kt linha 128) mostrava `${mod.valor}%` sem considerar `bonusBase` nem `niveis`.
- **Correção:** linha 128 substituída por `val totalMod = mod.bonusBase + if (mod.porNivel) mod.valor * mod.niveis else mod.valor` → exibe o total real (ex: Cone nível 6 = 50+10×6 = +110%). Cálculo de custo não foi tocado (já correto).
- **Verificação:** clean build OK em 17s.
- **Próximo:** TESTE device — Cone nível 1 deve exibir +60% (50+10); Cone nível 6 deve exibir +110% (50+60). Custo calculado já batia antes, agora a exibição confirma.

### Lote 197: Medusa (139 pts) adicionada ao catálogo de raças - CONCLUÍDO
- **Pedido (usuário):** montar a Medusa no `racas.v1.json` usando o JSON de ficha (`ficha_gurps(1).json`) que ele já validou no app como fonte da verdade — assim os custos batem com o livro Cataclismo p.196.
- **Componentes mapeados (todos no catálogo):** Aptidão Mágica nível 2 (custoEscolhido 5); Ataque Inato Veneno de cobra (tox 1d, mods Acompanhamento+Cíclico 1m); Atribulação Olhar Petrificante (mods Cone+Prazo Permanente+Área Seletiva+Imprecação 2+paralisia+Base Sensorial Lim); Golpeadores Mordida (mods Incapaz de Aparar+Fraco, custoEscolhido 5); Resistente ao Olhar de medusa (raridade 5, grau 1.0=Imunidade, =5 pts).
- **Decisões:** `paralisia` entra como modificador específico da Atribulação (specialRule registrada, sem prefixo `mod_`); `mod_base_sensorial_lim` (já existente, -20%) cobre o "Base Visual -20%" do livro — mesma mecânica, é base sensorial limitada. `mod_paralisia` standalone e `mod_base_visual` standalone NÃO foram necessários porque a Atribulação já tem `paralisia` como ampliação especial dela e `mod_base_sensorial_lim` cobre o caso.
- **Catálogo de raças: 18 fechando** — restou só Espírito do Manancial (raça pulada Lote 181, agora destravada pela meta Espírito 261) pendente de montar quando o usuário pedir.
- **Verificação:** JSON válido (18 raças); clean build OK (7s).
- **Próximo:** TESTE device — abrir Medusa, conferir se aparece sem "Não resolvidos"; conferir custo total bate com 139 pts do livro.

### Lote 215: fix Dentes Afiados Reptante — 0 pts em vez de 1 - CONCLUÍDO
- **Causa:** `DentesRule` usa `metadados["tipoDentes"]`. Sem o campo, usa default `"rombo"` = 0 pts. JSON da raça não tinha metadados.
- **Fix:** adicionado `"metadados": {"tipoDentes": "dentes_afiados"}` na entrada de dentes do Reptante. `DentesRule` agora retorna 1 pt corretamente.
- **Regra para futuras raças com Dentes:** sempre incluir `metadados: {tipoDentes: "rombo"|"bico_afiado"|"dentes_afiados"|"presas"}`.
- **Commit:** `ba95481`

### Lote 216: Reptantes (58 pts, Cataclismo 200) adicionado ao catálogo - CONCLUÍDO
- **Reptante:** ST+4 (Tamanho -10%), IQ-1, HT+2. MT+1. Vantagens: Dentes Afiados (1), Garras Afiadas (5), Longevidade, Membrana Nictitante 3, RD 1 (Pele Resistente -40% = 3), Tolerância à Temperatura 5, Visão Periférica. Desvantagens: Estigma Social Bárbaro (-10), Timidez Suave (-5). Perícias: Camuflagem (F) IQ+0 [1], Sobrevivência Deserto (M) Per+0 [2]. Qualidade: traço inerente idiomas Com Sotaque.
- **Verificação:** 36(atrib)+34(vant)-15(desv)+3(perícias) = 58 pts ✓
- **Commit:** `e51ea76`

### Lote 217: fix Não Respira (Guelras) Povo do Mar - CONCLUÍDO
- **Bug:** `custoEscolhido: 10` era o valor pós-modificador. O app aplicava `-50%` em cima → `floor(10×0.5) = 5` em vez de 10. Correto: `custoEscolhido` deve ser o custo **base** (20); app calcula `floor(20×0.5) = 10` ✓.
- **Regra geral:** para vantagens FIXO com mods no catálogo racial, `custoEscolhido` = custo base do catálogo (não o valor já calculado).
- **Commit:** `5f95cab`

### Lote 218: Povo do Mar (52 pts, Cataclismo 200) + mod_guelras - CONCLUÍDO
- **Povo do Mar:** sem mod. atributos. Vantagens: Deslocamento Ampliado 1 (Água), Escorregadio 1, Fala Subaquática, Membrana Nictitante 5, Não Respira (Guelras -50% = 10 pts), Resistência à Pressão 1 (5 pts), Sentido de Monitoramento Sonar (20 pts). Desvantagem: Dependência Água Muito Comum Diária (-15). Qualidade: Aquático (traço inerente, sem custo).
- **modificadores.v1.json:** adicionado `mod_guelras` (-50%, p.352) — limitação de Não Respira: só funciona na água ou em ar úmido.
- **Verificação:** 0(atrib)+67(vant)-15(desv) = 52 pts ✓
- **Commit:** `6c5092a`

### Lote 219: campo MT no dialog de modelo racial - CONCLUÍDO
- **Dialog de Raça/Metacaracterísticas:** adicionado `AjustadorVerticalRacial("MT")` na Row de Vel.Básica/Desloc., ao lado do Deslocamento. Estado inicializado de `modeloOriginal.modificadorTamanho`.
- **Carregar Raça do Catálogo:** propagação de `modificadorTamanho = m.modificadorTamanho` ao popular os campos do dialog (antes ficava 0 mesmo para raças com MT do catálogo).
- **`montarModelo()`:** inclui `modificadorTamanho` na construção do `ModeloRacial`. Gson serializa/desserializa automaticamente; fichas antigas recebem 0 (padrão correto).
- **Build:** 8s OK.
- **Commit:** `14bcf7b`

### Lote 220: MT na mesma linha que BC nas Características Derivadas - CONCLUÍDO
- **Fix UI:** MT estava numa linha separada abaixo do BC. Corrigido para aparecer na mesma Row (Vel. Basica / Desloc. / BC / MT), oculto se 0.
- **Commit:** `54cf3ab`

### Lote 221: Pequeninos/Halfling (0 pts, Cataclismo 199) adicionado ao catálogo - CONCLUÍDO
- **Pequenino:** ST-3, DX+1, HT+1. MT-1. PV+1, Deslocamento-1. Vantagens: Reconhecimento Social 1 (Bom Vizinho), Silêncio 1, Talento dos Pequeninos para Armas de Longa Distância 2. Desvantagens: Amigável (-5), Código de Honra Pequeninos (-5), Gula (auto12, -5). Peculiaridades (2, = -2): Aversão a grandes corpos d'água; Acomodado.
- **Verificação:** 0(atrib)-3(sec)+20(vant)-15(desv)-2(peculiaridades) = 0 pts ✓
- **Commit:** `37ae2aa`

### Lote 222: Orc (-22 pts, Cataclismo 199) adicionado ao catálogo - CONCLUÍDO
- **Orc:** IQ-1, HT+2. PV+3. Audição Aguçada 2. Desvantagens: Briquento (auto12, -10), Estigma Social Bárbaro (-10), Intolerância Total (-10). Perícia: Briga (F) DX+0 [1]. Peculiaridades (3, = -3 pts): curvam-se a seres mais fortes; medem posição social pelas coisas que controlam; não consideram traição errada.
- **Verificação:** 0(atrib)+6(PV+3)+4(vant)-30(desv)+1(perícia)-3(peculiaridades) = -22 pts ✓
- **Commit:** `1975166`

### Lote 223: Ogro (28 pts, Cataclismo 198) adicionado ao catálogo - CONCLUÍDO
- **Ogro:** ST+10 (Tamanho -10%), DX-1, IQ-3, HT+3. MT+1. Vantagens: Abascanto 2, Hipoalgia, Paladar/Olfato Apurado 1, RD 3 (Pele Resistente -40%), Visão Noturna 9. Desvantagens: Estigma Social Bárbaro (-10), Estigma Social Inculto (-5), Hábito Detestável Come sapientes (-15), Hediondo/Aparência (-16). Sem perícias/qualidades/peculiaridades (não mencionadas no texto).
- **Verificação:** 40(atrib) + 34(vant) - 46(desv) = 28 pts ✓
- **Commit:** `41c1829`

### Lote 224: campo MT (Modificador de Tamanho) racial - CONCLUÍDO
- **Novo campo:** `ModeloRacial.modificadorTamanho: Int = 0` (GURPS B19). Bônus de ataque para acertar a criatura; afeta testes de Visão.
- **`Personagem`**: propriedade computada `modificadorTamanho` lida do modelo racial.
- **`RacaCatalogo.kt`**: `RacaDefinicao` ganha campo `mt: Int = 0`; resolver propaga para `ModeloRacial.modificadorTamanho`.
- **`TabGeral.kt`**: exibe "MT: ±N" nas Características Derivadas, **após BC, oculto se 0** (personagens humanos não veem o campo).
- **`racas.v1.json`**: preenchido `"mt"` em 4 raças já no catálogo: Esfinge Leonina/Panterina +1, Esfinge Tigrina +1, Gigante +2, Kobold -1.
- **Build:** 13s OK.
- **Commit:** `53eb210`

### Lote 225: modificadores em desvantagens (Configurar + Editar) - CONCLUÍDO
- **Bug:** `ConfigurarDesvantagemDialog` nunca exibia a seção de modificadores (estava com comentário `// ... logic for showAddMod etc`). `EditarDesvantagemDialog` tinha o botão "Add" mas nunca instanciava o `EscopoModificadoresDialog` — clicar não abria nada.
- **Correção:** `ConfigurarDesvantagemDialog` ganhou a seção "Modificadores (%)" com botão Add + lista `ModificadorSelecionadoItem` + `EscopoModificadoresDialog`. `EditarDesvantagemDialog` ganhou o `EscopoModificadoresDialog` que faltava. Lógica idêntica à das vantagens, sem `mod_aptidao_escola` (que não existe em desvantagens).
- **Build:** 8s OK.
- **Commit:** `bd5dabe`

### Lote 226: mods em desvantagens raciais + mod_furia_em_combate + fix Minotauro - CONCLUÍDO
- **Bug:** `RacaCatalogo.kt` resolver de desvantagens NÃO processava o array `mods` — só vantagens tinham esse tratamento. Resultado: `furia` do Minotauro ficava com `modificadores:[]`, sem o `+50%` Fúria em Combate, então o app calculava `-10×1.5(auto9) = -15` em vez de `-22`. Total aparecia 19 pts em vez de 13.
- **Correção:** Extendido o resolver de desvantagens para processar `mods` com a mesma lógica das vantagens (incluindo formato `id:N` para níveis e fallback no catálogo global de modificadores).
- **modificadores.v1.json:** adicionado `mod_furia_em_combate` (+50%, p.143) — indica que a Fúria só é ativada em combate.
- **Verificação de cálculo:** Fúria: `-10 × 1.5(auto9) = -15`, depois `ceil(-15 × 1.5) = ceil(-22.5) = -22` ✓. Total: 40(atributos)+57(vantagens)-88(desvantagens)+4(perícias) = 13 pts ✓
- **Commit:** `ac41dc3`

### Lote 227: arredondamento correto para vantagens com limitações (floor, não ceil) - CONCLUÍDO
- **Bug:** `calcularCustoVantagem` usava `ceil` para todos os modificadores. GURPS p.102 diz: ampliações arredondam para cima (ceil), limitações **eliminam frações** (floor). Com só limitações o custo podia ficar 1 pt acima do correto (ex: RD 1 crânio = `ceil(5×0.3) = ceil(1.5) = 2` em vez do correto `floor(1.5) = 1`).
- **Correção:** `calcularCustoVantagem` agora usa `floor` quando `percentualFinal < 0` e `ceil` quando positivo/misto.
- **Casos Minotauro verificados:** RD crânio: `floor(10×0.30)=3` ✓ | RD pele: `floor(15×0.60)=9` ✓.
- **Commit:** `c1be581`

### Lote 228: corrige tipoDano Chifres Minotauro pa → pa++ - CONCLUÍDO
- **Bug:** `tipoDano` dos Chifres estava `pa` (5 pts base) em vez de `pa++` (8 pts base). Com `pa` o custo calculado seria 8 pts, não 13. `pa++` × (1-40%+100%) = 8×1.6 = 12.8 ≈ 13 ✓
- **Commit:** `645edea`

### Lote 229: Minotauro no catálogo + mod_comprido + RD múltipla permitida - CONCLUÍDO
- **Minotauro (13 pts, Cataclismo 198)** adicionado ao `racas.v1.json`: ST+3/DX+1/IQ-2/HT+3. Vantagens: Audição Aguçada 3, Golpeadores (Chifres pa, Arco Limitado + Comprido 1MT, custoEscolhido:13), Abascanto 3, RD 2 crânio (`mod_apenas_o_cranio:7` = -70%), RD 3 pele (`pele_resistente`), Senso de Direção, Visão Periférica. Briga DX+2 concedida. Desvantagens: Fúria em Combate (auto9, -22), Sanguinolência (auto9), Hediondo (aparencia -16), Intolerância Total (-10), Hábito Detestável Come sapientes (-15), Estigma Social Inculto (-5), Solitário (auto12, -5). Peculiaridade: "Odeia ogros, e ser confundido com ogros".
- **modificadores.v1.json:** adicionado `mod_comprido` (+100%/nível, ampliação, p.62) — antes não existia no catálogo global.
- **FichaTraitDelegate.kt:** `resistencia_a_dano` agora entra na lista `permiteMultiplas` — usuário pode adicionar RD crânio + RD pele + RD tronco etc. com descrições diferentes, igual ao ataque_inato.
- **Commit:** `f294e42`
- **Build:** OK (16s).

### Lote 230: mods de raça aceitam "id:N" (níveis) + Medusa Cone:15 / Cíclico:2 - CONCLUÍDO
- **Bug (usuário viu na ficha):** raças não tinham como expressar "Cone 15 níveis" ou "Cíclico 1m 2 ciclos" — o resolver hardcodava `niveis=1` em todos os mods das raças. Cone da Medusa ficaria +60% (50+10×1) em vez de +200% (50+10×15).
- **Solução (mínima, retrocompatível):** id estendido no array `mods` aceita formato `"id:N"`. Resolver (RacaCatalogo.kt linha 129) faz split em `:` — pré:colon = modId, pós:colon = níveis. Sem colon = 1 (compat antiga, todas as 17 raças continuam funcionando).
- **Medusa corrigida:** `mod_cone:15` (Cone 15m de largura, +200%) e `mod_ciclico_1m:2` (Cíclico 1m com 2 ciclos = +80%). Aptidão Mágica 1 ficou com `nivel:2 custoEscolhido:5` no JSON da ficha — questionei, mas é o valor que o usuário validou no app.
- **Verificação:** clean build OK (9s).
- **Próximo:** TESTE device — Cone da Medusa deve mostrar +200% e Cíclico +80%; soma da Atribulação deve fechar 75 pts. Catálogo continua com 18 raças.


### Lote 231: Pente fino acessibilidade Pracego — diálogos de traços e Mestre IA - CONCLUÍDO - **Commit:** `85cdbbd`
- **Escopo:** 11 fixes em TraitDialogsV2.kt (ModeloRacialDialog completo), + fixes em TraitCommonComponents.kt, TraitDialogs.kt, DialogsMestreIA.kt e DialogsCommon.kt.
- **Fixes principais:** ItemTraitRacial com labels por nome, FilterChip tipo perícia (Concedida/Bônus) com descrição do que significa cada opção, botão limpar busca, info modificador, remover modificador, menu modo Mestre IA, botão enviar mensagem, excluir ficha com nome.
- **Verificação:** assemblePracegoDebug + assembleVisualDebug OK.

### Lote 232: Mestre IA no rodapé + ícone real + renomear — CONCLUÍDO - **Commits:** `e61cd2b`, `59d061f`, `eacbad9`, `6fb073a`, `baa5f5b`
- **Ícone no rodapé:** `FichaCustomNavigationBar` ganhou `RPGNavigationItem` fixo à esquerda para o Mestre IA com ícone `tab_mestre_ia.png` (hexágono com olho/pirâmide); aba ficou com mesma animação zoom/glow dos outros ícones quando selecionado; abas restantes empurradas para a direita.
- **`FichaScreen`:** `onMestreIAClick` e `mestreIAAberto` conectados — ícone anima quando o dialog está aberto.
- **Dialog Mestre IA:** emoji de estrela → imagem `tab_mestre_ia`, "Mestre Digital 2.0" → "Mestre IA", emoji de rosto no estado vazio → imagem do ícone.
- **Padronização de diálogos de seleção:** ItemTraitRacial segue padrão `AppListItemCard` igual Tab Traços; diálogo de magias migrado para `FullscreenDialogContainer` (igual vantagens/perícias); diálogos de arma, escudo e armadura também migrados para fullscreen.

### Lote 233: Campo Motivo na Rolagem Livre - CONCLUÍDO - **Commit:** `09092f8`
- **Pedido:** usuário queria digitar o motivo da rolagem livre (ex: "Pânico") para aparecer no Discord em vez do genérico "Livre".
- **`RolagemPersonalizadaDialog`:** novos parâmetros `motivo: String` e `onUpdateMotivo`; `OutlinedTextField` "Motivo (opcional)" com placeholder adicionado entre o card de expressão e o botão Rolar.
- **`TabRolagem`:** estado `dadosPersonalizadosMotivo`; `executarRolagemPersonalizada` usa `label.ifBlank{"Livre"}` — texto do histórico e contexto Discord refletem o motivo digitado.
- **Verificação:** assembleVisualDebug OK (30s).

### Lote 234: Remove botão "Mestre IA (Beta)" do MenuDialog - CONCLUÍDO - **Commit:** `09092f8`
- **Motivo:** com o ícone no rodapé (Lote 232) o botão do menu ficou redundante.
- **`DialogsCommon`:** parâmetro `onMestreIA` e botão "Mestre IA (Beta)" removidos; `pracegoTraversal` do Fechar atualizado de 8→7.
- **`FichaScreen`:** bloco `onMestreIA = { ... }` removido do call site.
- **Verificação:** assembleVisualDebug OK (20s).


### Lote 235: Comando de Voz no Mestre IA (Long Press) - CONCLUÍDO - **Commit:** `948bb5a`
- **Novo arquivo:** `VozMestreIA.kt` (`ui/components/`) encapsula o `SpeechRecognizer` nativo Android em PT-BR. Estados: `OCIOSO → ESCUTANDO → PROCESSANDO → OCIOSO` (ou `ERRO`). Callbacks `onEstado` e `onResultado`.
- **`FichaCustomNavigationBar`:** ícone do Mestre IA ganhou `combinedClickable` — toque simples abre o chat, **segurar ativa o microfone**. Anel pulsante **verde** durante escuta e **amarelo** durante processamento como feedback visual.
- **`FichaScreen`:** instancia `VozMestreIA` com `remember`, `onResultado` chama `conversarComMestreIA(texto, "geracao")` e abre o dialog automaticamente. `DisposableEffect` garante `liberar()` ao sair da tela.
- **Permissão:** `RECORD_AUDIO` já estava no `AndroidManifest.xml` — Android solicita ao usuário no primeiro long press.
- **Docs:** `ARQUITETURA_MESTRE_IA.md` ganhou seção "9. A Voz"; `MAPA_DETALHADO.md` atualizado com os dois arquivos.
- **Verificação:** assembleVisualDebug OK (30s).

### Lote 236: Correção do Sistema de Voz (Long Press não disparava) - CONCLUÍDO - **Commit:** `cdf4b34`
- **Causa raiz 1 (stale lambda):** `vozMestreIA.onEstado` e `onResultado` eram atribuídos uma vez em `remember {}`, capturando `estadoVoz` e `showMestreIADialog` por valor — nunca viam as atualizações de estado posteriores. Corrigido com `SideEffect` que re-atribui os lambdas a cada recomposição.
- **Causa raiz 2 (permissão runtime):** `RECORD_AUDIO` estava no manifesto mas Android 6+ exige `requestPermissions()` em tempo de execução. Adicionado `rememberLauncherForActivityResult(RequestPermission)` que pede a permissão ao usuário na primeira vez e só então chama `iniciar()`.
- **Causa raiz 3 (threading):** `SpeechRecognizer` deve ser criado e operado na Main thread. Adicionado `mainHandler = Handler(Looper.getMainLooper())` e `mainHandler.post { ... }` envolvendo toda a criação/início da escuta.
- **Verificação:** assembleVisualDebug OK (17s).

### Lote 237: Classificador de Intenção por IA no Comando de Voz - CONCLUÍDO - **Commit:** `f32ad4b`
- **Problema corrigido:** voz estava hardcoded em `"geracao"` — qualquer coisa falada virava criação de história e tentava integrar na ficha.
- **Novo arquivo:** `VozIntencaoClassifier.kt` — chama o Gemini Flash Lite com prompt minúsculo após o reconhecimento de voz. Retorna `DUVIDA` → `"conversa"`, `ANALISE` → `"analise"`, `CRIAR` → `"geracao"`.
- **`VozMestreIA.kt`:** `onResultado` agora passa `(texto, modo)`. Mantém estado `PROCESSANDO` enquanto a IA classifica, depois emite `OCIOSO`.
- **`FichaScreen.kt`:** `onResultado` recebe o modo classificado, atualiza `viewModel.mestreIAMode` e chama `conversarComMestreIA(texto, modo)`.
- **Fallback seguro:** qualquer erro na chamada ao classificador cai em `"conversa"` (Dúvida, gratuito) — nunca modifica a ficha por acidente.
- **Fase 2 pendente:** botão de microfone dentro do dialog do Mestre IA (mesmo classificador).
- **Verificação:** assembleVisualDebug OK (10s).

### Lote 238: TTS Nativo — Mestre IA Responde em Voz - CONCLUÍDO - **Commit:** `bf13e48`
- **Novo arquivo:** `VozTTS.kt` — encapsula `android.speech.tts.TextToSpeech` em PT-BR. Speech rate 1.05x, pitch neutro.
- **Limpeza para fala:** remove blocos de código, `[Pág. X]`, markdown (negrito, itálico, títulos, listas), limita a 800 chars para não falar respostas enormes.
- **`FichaScreen.kt`:** `vozTTS.falar(resposta)` chamado no callback `onResult` — só ativa quando o comando veio por voz, não interfere no chat de texto normal.
- **Reversão simples:** deletar `VozTTS.kt` + remover 3 linhas do `FichaScreen.kt`.
- **Verificação:** assembleVisualDebug OK (23s).

### Lotes 240-243: Gemini Live — RAG Real + UI Auto-open + camelCase Fix - CONCLUÍDO - **Commit:** `45a5932`
- **Lote 240 — RAG real no consultarManual:** `GeminiLiveTools.kt` reescrito. `consultarManual` chama `MestreIAPlanner.planejarBusca` + `MestreIAGraphEngine.buscarDiretoNoCodex` (com AND-bonus, proximity scoring) usando `runBlocking`. Sub-queries temáticas fazem merge de chunks. Resultado formatado via `formatarParaIA` — idêntico ao caminho do Auditor de texto. Corrigidos nomes de campo: `pontosGastos`, `custoFinal`, `deslocamentoBasico`, `velocidadeBasica.toDouble()`.
- **Lote 241 — camelCase fix (code=1007):** `GeminiLiveService.buildSetupMessage()` tinha snake_case nas chaves JSON (`generation_config`, `system_instruction`). Corrigido para camelCase conforme spec da Gemini Live API (`generationConfig`, `responseModalities`, `speechConfig`, `voiceConfig`, `prebuiltVoiceConfig`, `voiceName`, `systemInstruction`). `responseModalities` agora só `["AUDIO"]` — `TEXT` foi removido (causava rejeição). Model name tem prefixo `models/`.
- **Lote 242 — chat auto-abre + thread safety:** `FichaScreen.kt`: callback `onEstado` abre `showMestreIADialog=true` via `Handler(Looper.getMainLooper()).post{}` quando estado muda para OUVINDO. `onRespostaMestre` também usa Handler(Main). `DialogMestreIA` recebe `estadoLive` e `onEncerrarLive`. Banner removido do topo da tela.
- **Lote 243 — botão encerrar dentro do dialog:** `DialogsMestreIA.kt`: aceita `estadoLive: EstadoLive` e `onEncerrarLive: () -> Unit`. Quando voz ativa: mostra dot colorido + label do estado no título; oculta input de texto; exibe status + botão "Encerrar voz" no rodapé. Quando inativo: comportamento normal com botão "Fechar".
- **Build:** assembleVisualDebug OK (9s).

### Lote 239: Gemini Live API — Voz Bidirecional com Tool Calling (Fase 1) - CONCLUÍDO - **Commit:** `dea9751`
- **Novo arquivo `GeminiLiveService.kt`:** WebSocket OkHttp persistente para `bidiGenerateContent`. Captura microfone (AudioRecord 16kHz PCM) em chunks de ~100ms, envia em base64. Reproduz resposta de áudio (AudioTrack 24kHz PCM). Gerencia estados OCIOSO/CONECTANDO/OUVINDO/FALANDO/ERRO. Trata `toolCall`, `serverContent`, `goAway` e `turnComplete`.
- **Novo arquivo `GeminiLiveTools.kt`:** Despacha tool calls do Gemini para os métodos reais do FichaViewModel (obterFicha, obterPontosRestantes, adicionarVantagem, removerVantagem, adicionarDesvantagem, adicionarPericia, consultarManual). Busca definições no `dataRepository` antes de chamar o ViewModel.
- **`FichaScreen.kt`:** Instancia `GeminiLiveService` e `GeminiLiveTools`. `iniciarVozComPermissao()` agora ramifica: se `VOZ_BIDIRECIONAL_HABILITADA=true` → abre/encerra sessão Live; senão → caminho antigo. Transcrições salvas no chat via `adicionarMensagemVoz`. `DisposableEffect` encerra a sessão ao sair da tela.
- **`FichaIADelegate.kt`:** Novo método `adicionarMensagemVoz(texto, role)` — adiciona mensagem ao histórico em memória (`mestreIAChatHistory`) e persiste no `ChatHistoryDao` criando sessão se necessário.
- **`FichaViewModel.kt`:** Expõe `adicionarMensagemVoz()` delegando ao `FichaIADelegate`.
- **`build.gradle.kts`:** Novos campos `GEMINI_LIVE_MODEL` (`gemini-2.5-flash-native-audio-latest`), `GEMINI_LIVE_VOICE` (`Charon`), `VOZ_BIDIRECIONAL_HABILITADA` (`false` por padrão — habilitar no APK de teste).
- **Voz escolhida:** Charon (masculina, grave, PT-BR testado).
- **Feature flag:** `BuildConfig.VOZ_BIDIRECIONAL_HABILITADA=false` — invisível no APK padrão. Para testar: mudar para `true` em `build.gradle.kts` e gerar novo APK.
- **Verificação:** assembleVisualDebug OK (26s).

### Lote 244: Gemini Live — Fix protocolo (setupComplete + camelCase) - CONCLUÍDO - **Commit:** `f396366`
- **GeminiLiveService.kt:** Corrigido EOFException do WebSocket — mensagens agora enviadas só após `setupComplete` do servidor, não no `onOpen`. URL corrigida para `v1alpha` (era `v1beta` — server ignorava tudo silenciosamente). OkHttp usa `ws.send(ByteString)` via `.encodeUtf8()` — frames binários conforme spec Gemini Live (texto é rejeitado). `onMessage(ws, ByteString)` sobrescrito para capturar respostas binárias. AudioTrack migrado para `USAGE_MEDIA` com buffer de 2s para evitar underrun. `Channel<ByteArray>(capacity=200)` + coroutine única de reprodução para evitar SIGSEGV de escritas concorrentes. `outputTranscription` usado no chat (PT-BR) em vez de `modelTurn.parts[].text` (inglês interno). `toolResponse` em camelCase. Campos inválidos `enableAudioTranscription` e `outputAudioTranscription` removidos.
- **Build:** assembleVisualDebug OK.

### Lote 245: Voz — Ferramentas Completas (buscarCatalogo + editarFicha + trilhaDeMagias) - CONCLUÍDO - **Commit:** `bac9b5b`
- **GeminiLiveTools.kt reescrito:** Delega ao `ForjadorToolExecutor` (mesmo executor do Forjador/Auditor de texto). Ferramentas implementadas: `buscarCatalogo(tipo, query)` — previne alucinação de IDs buscando no catálogo oficial antes de qualquer edição; `editarFicha(operacao, secao, alvo, valor)` — CRUD unificado com a mesma lógica do Forjador para vantagens, desvantagens, perícias, técnicas, magias, equipamentos e atributos (com validação de pré-requisitos de magia); `trilhaDeMagias(magia_alvo)` — GPS de pré-requisitos, trilha ótima de magias; `lerFicha(secao)` — leitura de qualquer seção. Ferramentas legadas (adicionarVantagem, removerVantagem, etc.) mantidas como aliases.
- **GeminiLiveService.kt:** `buildSetupMessage()` declara 5 ferramentas (lerFicha, buscarCatalogo, editarFicha, trilhaDeMagias, consultarManual). `systemPrompt` atualizado com protocolo obrigatório: buscarCatalogo antes de qualquer edição, GPS antes de adicionar magia.
- **Build:** assembleVisualDebug OK.

### Lote 247: Voz — Fix conexão + transcrição bidirecional funcionando - CONCLUÍDO - **Commit:** `905dfd5`
- **Modelo trocado:** `gemini-2.5-flash-native-audio-latest` → `gemini-3.1-flash-live-preview` — mais rápido, suporta transcrição nativa e tools simultâneas.
- **code=1007 resolvido:** `outputAudioTranscription` removido do `generationConfig` (campo não existe nesse modelo). Diagnóstico via `logcat --pid` que revelou a mensagem exata do servidor.
- **Formato do microfone corrigido:** `realtimeInput.mediaChunks[]` → `realtimeInput.audio{data, mimeType}` conforme spec oficial da API.
- **Saudação corrigida:** `clientContent turnComplete=false` para injetar contexto sem disparar resposta; saudação via `realtimeInput.text` que dispara áudio imediato.
- **Transcrição bidirecional funcionando:** `outputTranscription` chega como objeto `{"text":"fragmento"}` palavra por palavra — acumulado em `pendingTextoFallback` e exibido completo no `turnComplete`. Mesmo fix para `inputTranscription` do usuário.
- **Log limpo:** Suprime spam de base64 PCM; mostra `♪ Áudio iniciado` por turno.
- **Resultado:** Modelo conecta, fala saudação em PT-BR, ouve usuário, usa tools (buscarCatalogo, editarFicha, etc.) e transcreve conversa no chat. ✅

### Lote 246: Voz — Fix auto-interrupção permanente + texto em inglês no chat - CONCLUÍDO - **Commit:** `feed965`
- **modeloFalando = false no turnComplete:** A flag nunca era resetada — após a 1ª resposta o modelo bloqueava o microfone permanentemente e ficava mudo para sempre.
- **thought=true ignorado:** Substituiu o filtro de regex (`**Título**`) por verificação do campo `part.optBoolean("thought", false)`. Pensamentos internos do modelo em inglês (sem marcadores **) vazavam para o chat. Agora são descartados na fonte.
- **Build:** assembleVisualDebug OK (23s clean build). Instalado via WiFi ADB (192.168.1.84:33933).


### Lote 248: Raças e Metacaracterísticas no Mestre IA (voz e texto) - CONCLUÍDO - **Commit:** `06b2647`
- **Duas novas ferramentas:** `forjador_buscar_racas(query, tipo)` — lista raças/metacaracterísticas dos catálogos; `forjador_aplicar_modelo_racial(id, tipo)` — aplica ModeloRacial completo (atributos, vantagens, desvantagens, perícias) via `viewModel.atualizarModeloRacial()`.
- **Propagação de Context:** `ForjadorToolExecutor` recebe `Context?` (necessário para carregar os JSONs de assets). Propagado por FichaViewModel → FichaIADelegate → MestreIAGeneratorUseCase → ForjadorToolExecutor; e FichaScreen → GeminiLiveTools → ForjadorToolExecutor.
- **Voz (GeminiLiveService):** 2 ferramentas declaradas no `buildSetupMessage()` e instrução no systemPrompt.
- **Texto (ForjadorTools):** Schemas adicionados em getGeminiTools() e getOpenAITools(). Constantes TOOL_BUSCAR_RACAS e TOOL_APLICAR_RACIAL adicionadas.
- **Build:** assembleVisualDebug OK (5s). Instalado via WiFi ADB (192.168.1.84:33933).

### Lote 249 (fix pós-248): Correções Ferramentas Raciais — CONCLUÍDO | commit: 5209db9
- Remove take(15)/take(10) em buscarRacas — lista cortava raças no meio (ex: Kobold era o limite)
- Adiciona TOOL_BUSCAR_RACAS e TOOL_APLICAR_RACIAL ao filtro em MestreIAGeneratorUseCase — loop quebrava silenciosamente
- Proíbe perícias/vantagens em qualidades no prompt do Forjador
- Bloqueia invenção de nomes genéricos como "Kaelen" — usa nome do usuário ou "Sem Nome"


### Lote 250: Melhorias DeepSeek API — CONCLUÍDO | commit: 366be61
- Migra `deepseek-chat` → `deepseek-v4-flash` (deprecação em 24/07/2026)
- Ativa Thinking Mode no Auditor: modelo raciocina passo a passo antes de responder
- Captura `reasoning_content` e loga em `MestreIA_Thinking` para debug
- Loga `prompt_cache_hit_tokens` / `prompt_cache_miss_tokens` em `MestreIA_Cache`


### Lote 251: Plano RAG Mestre IA + Descontinuação Voz Clássica + Mesa Virtual Completa — CONCLUÍDO | commit: 3c3d43b
- `PLANO_MesteIA_RAG.md` criado: diagnóstico dos 3 tipos de falha RAG + 9 melhorias em 3 ondas
  - Onda 1 (dias 1-3): FTS4→FTS5+BM25, filtro source_id, chunking menor
  - Onda 2 (semanas 1-2): SQLite-vec semântico, LRU cache, compressão de contexto (Pocket RAG), índice por tópico
  - Onda 3 (meses): pipeline offline de livros, tabelas FTS5 particionadas
- Descontinuação da voz clássica: `VozMestreIA.kt`, `VozTTS.kt`, `VozIntencaoClassifier.kt` removidos
- `GeminiLiveService.kt`: enum `EstadoVoz` migrado para cá (compatibilidade com FichaCustomNavigationBar)
- `FichaScreen.kt`: simplificado — apenas GeminiLive, sem `BuildConfig.VOZ_BIDIRECIONAL_HABILITADA`
- `Mesa Virtual/index.html`: suporte completo a `modeloRacial` (modForca, modDestreza, etc.), especialização de perícias, histórico/notas, PV/PF corretos com modificadores raciais
- `ARQUITETURA_MESTRE_IA.md`: atualizado com GeminiLiveService, GeminiLiveTools e marcação de deprecações


### Lote 252: RAG — BM25-Kotlin + Filtro por Fonte + Pool Otimizado — CONCLUÍDO | commits: db839d5, e03624e
- `ManualChunkDao`: nova query `buscarRegrasPorFonte()` com filtro `source_id` (busca por livro específico)
- `MestreIARepository`: novo método `buscarNoCodexPorFonte()` para buscas direcionadas (ex: só Pyramid)
- `MestreIAGraphEngine`: scoring substituído por **BM25-Kotlin real**
  - IDF por termo: `log((N-df+0.5)/(df+0.5))` — termos raros pesam mais que termos comuns
  - TF com saturação k1=1.5: repetição do mesmo termo não infla o score infinitamente
  - Normalização por comprimento b=0.75: chunks longos não têm vantagem injusta sobre curtos
  - AND bonus (+15) e proximidade bonus (+5) calibrados em escala BM25
- Pool reduzido 1500 → 500: BM25 ranqueia bem; pool menor = busca mais rápida
- Nota: FTS5 nativo requer Room 2.7 (alpha). BM25-Kotlin equivale ao resultado sem alpha.


### Lote 253: RAG — LRU Cache de Buscas FTS — CONCLUÍDO | commit: 3372e58
- `MestreIARepository`: LinkedHashMap LRU (20 entradas) para resultados FTS da sessão
- Cache hit evita re-processar queries repetidas no multi-query temático paralelo
- Mutex protege acesso concorrente das coroutines
- `limparCacheFTS()` para reset entre sessões de perguntas


### Lote 254: RAG — Pocket RAG: Compressão Seletiva de Contexto — CONCLUÍDO | commit: 2a551bd
- `MestreIAGraphEngine.comprimirChunk()`: extrai sentenças relevantes por termos de busca
  - Chunks ★★★ (BM25 >= 8.0): texto completo preservado
  - Chunks ★★/★: comprimidos para 4 sentenças relevantes + contexto vizinho
  - Chunks sem match: primeiras 2 sentenças como contexto mínimo
- Contexto RAG estimado: ~35KB → ~12-18KB (-40 a -60% de tokens)
- `formatarParaIA()` recebe `query` para guiar qual sentença é relevante
- Thresholds ajustados para escala BM25-Kotlin (8.0/2.0)


### Lote 255: RAG — Dicionário 90+ Entradas + Filtro por Livro — CONCLUÍDO | commit: 8e92543
- `MestreIAPlanner.dicionarioTecnico`: expandido de 45 → 90+ entradas
  - Novos temas: voo, cavalaria, veículos, artes marciais, fogo/veneno/doença/radiação
  - Social/reputação, economia, encumbrance, magia completa com pré-requisitos
- `PlanoDeBusca.livrosRelevantes`: novo campo com source_ids detectados pelo cenário
  - tiro+subaquático → `pt_pyramid_26_underwater` entra automaticamente
  - Loga no Logcat `MestreIA_RAG` a lista de livros relevantes detectados
- `livrosPorCategoria`: mapa público preparado para uso pelo GraphEngine na Onda 2



### Lote 256: RAG — Melhoria 2D: Índice de Tópicos (topic_index.json) — CONCLUÍDO | commit: 8bef6dc
- `topic_index.json`: 11 tópicos declarativos com `require_all` + `fallback_any` + páginas garantidas
  - tiro_subaquatico → Pyramid p.7+8 + MB p.106-108 (garante alcance÷1000 sempre presente)
  - combate_subaquatico, queda_dano, dano_fogo, asfixia, critico, carga, alcance_tiro, magia, sanidade, movimentacao_agua
- `MestreIATopicIndex.kt`: loader JSON + matching engine (require_all / fallback_any)
  - Carregado no init do GraphEngine via `MestreIATopicIndex.carregar(context)`
  - Matching tolerante: `require_all` como primário, pares `fallback_any` como backup
- `MestreIAGraphEngine.kt`: chunks do topic_index injetados com prioridade máxima
  - Entram antes do `take(30)`, nunca cortados pelo diversificador de fontes
  - Log `MestreIA_RAG`: "TopicIndex garantido: pt_pyramid_26 p.7 → 2 chunks"
- **Impacto**: FTS4 pode falhar por keyword mismatch — topic_index garante as páginas certas mesmo assim



### Lotes 257+258: RAG — Raciocínio com Lacunas + Query Rewriting — CONCLUÍDO | commit: a3c5415
- `MestreIAPromptsAuditor`: Protocolo de Lacuna obrigatório no prompt do sistema
  - Quando regra não existe: declarar lacuna → identificar regras aplicáveis → compor interpretação → marcar ⚠️ Interpretação RAG
  - NUNCA inventar regra. NUNCA responder com negativa vazia.
- `MestreIAUseCase`: quando busca direta retorna vazio, instrução para aplicar Protocolo de Lacuna
- `gerarCatalogoDireto`: Query Rewriting ativado quando FTS retorna < 5 chunks
  - Chama Gemini Flash Lite para reformular em termos técnicos do GURPS
  - Merge dos resultados original + reescrito, deduplicado
  - Só ativa quando necessário — sem custo extra em buscas normais


### Lotes 259+260+261: RAG — Busca Semântica Híbrida + Scripts Offline — CONCLUÍDO | commits: a291d6b, 6e6a7e0
- `VecChunkEntity` + `VecChunkDao`: tabela `vec_chunks` no Room (versão 24)
  - Armazena embeddings de 3072 dims como ByteArray little-endian (12288 bytes/chunk)
- `MestreIASemanticEngine`: reranking BM25+semântico por similaridade cosseno em Kotlin puro
  - score_final = 0.6×BM25_norm + 0.4×cosseno
  - Embedding da query via Gemini `gemini-embedding-001` (~200ms) — modelo correto: 3072 dims
  - Fallback gracioso: vec_chunks vazio → BM25 puro, sem erro
- `FichaDatabase` v24: importa campo `"embedding"` do chunks.jsonl automaticamente
- `MestreIAGraphEngine`: reranking semântico após BM25 (top-50 reranqueados)
- `scripts/gerar_embeddings.py`: gera embeddings offline para chunks.jsonl via API Gemini
  - SSL bypass necessário em ambiente corporativo (ctx.check_hostname = False)
  - 1197 embeddings gerados (54.9 MB chunks.jsonl); validado: 0 erros, 0 dims erradas
- `scripts/processar_livro.py`: pipeline PDF → extração → chunking → embedding → append
  - Adicionar livro novo = 1 comando Python, sem tocar no app
- **Validação semântica:** "atirar com revólver numa piscina" → similaridade 0.6902 com Pyramid p.7 (regra subaquática) sem nenhum keyword match direto — busca semântica funcionando
- **Commit 6e6a7e0:** corrige modelo de text-embedding-004 (404) para gemini-embedding-001 (correto)
- **Lote 262 (tabelas por categoria)**: preparatório — `livrosPorCategoria` e `buscarRegrasPorFonte` já existem. Tabelas FTS separadas implementar quando atingir 5000+ chunks.

### Lote 263: Velocidade + Feedback Visual + Thinking Adaptativo — CONCLUÍDO | commit: 01c8ceb
- **Thinking Mode adaptativo:** só ativa quando `isComplexo=true` (pergunta com número, cálculo, metros, queda, etc.)
  - Perguntas simples: DeepSeek responde sem raciocínio interno — economiza ~15-20s na iteração final
  - Perguntas complexas: Thinking continua ativo para precisão máxima
- **Loops reduzidos em perguntas simples:** `loopsRestantes=2` (antes: 3) → máximo 1 tool call em vez de 2
  - Perguntas do tipo "qual a regra de X" → 1 busca + resposta final (economiza ~4-8s de rede + embedding)
- **Feedback visual granular (elimina silêncio de 54s):**
  - "Analisando pergunta..." → durante Planner
  - "Consultando o manual..." → durante FTS4+BM25+Semantic
  - "Buscando regras relacionadas..." → durante multi-query temático
  - "Analisando regras (N chunks encontrados)..." → iteração 1 complexa
  - "Preparando resposta..." → iteração 1 simples
  - "Buscando no manual: <query>..." → quando IA faz tool call
  - "Verificando regras adicionais..." → iteração intermediária
  - "Elaborando resposta final..." → última iteração
- **Diagnóstico logcat (pergunta "caí de um cavalo 12 hex"):**
  - Total: 54s | Thinking na iter.3: 21s | 3 tool calls | 44.510 tokens
  - Com Lote 263 estimativa: ~30-35s (complexa detectada pelo "12 hex" e "queda")



### Lote 265b: Correção do Match do TopicIndex — CONCLUÍDO | commit: 28219ae
- **Diagnóstico:** logcat mostrou 93+ tópicos disparando para "atirar com arco a cavalo" — ainda poluição de contexto
- **Causa raiz 1:** `resolverPaginasGarantidas` recebia a query expandida pelo planner (40+ termos) em vez da pergunta original
  - Fix: `MestreIAGraphEngine.buscarDiretoNoCodex` recebe `perguntaOriginal` e passa para o TopicIndex
- **Causa raiz 2:** lógica de match `t.contains(req) || req.contains(t)` — token "em" casava com "competencia", "empregos" etc.
  - Fix: `tokenMatch` usa `t.startsWith(req) || (t.length >= 5 && req.startsWith(t))`
  - Tokens mínimo 4 chars (antes: 2) — exclui "em", "na", "de", "com"
- **Resultado:** "arco a cavalo" → 3 tópicos relevantes (antes: 93+). Zero ruído.
- **Fallbacks ampliados** nos tópicos manuais: `modificadores_ataque_distancia`, `cavalos`, `combate_montado`, `tiro_subaquatico`, `queda_dano`, `cobertura_obstaculo_tiro`


### Lote 264: Status no Balão + Thinking sem Iteração Final + topic_index expandido — CONCLUÍDO | commit: 4d91d4b
- **FichaIADelegate:** status de progresso aparece no corpo do balão (⏳ Consultando...) em vez da badge 7sp
  - `onChunk` limpa corretamente o prefixo ⏳ antes de escrever a resposta final
- **MestreIAUseCase:** Thinking desativado na última iteração (`isComplexo=false` quando `isUltimaIteracao`)
  - Economiza ~15s na resposta final — raciocínio interno já foi feito nas iterações anteriores
- **topic_index.json expandido:**
  - `queda_dano`: adicionadas p.431+432 (regra de Quedas detalhada no Livro 2) + keywords cavalo/montaria/caiu
  - `modificadores_ataque_distancia`: garante p.548+549+552 para queries com "tiro"/"arco"
  - `postura_alvo_combate`: garante p.548+549+552+365 para queries com "ajoelhado"
  - `cobertura_obstaculo_tiro`: garante p.549 para queries com "cobertura"
  - `modificadores_combate_geral`: garante p.548+549+550+552 para queries com "modificador"+"combate"
  - `movimentacao_agua`: garante Pyramid p.7 + MB p.107 para queries com "nadar"
  - Total: 15 tópicos (antes: 11)
- **Revert parcial (commit 402b977):** keywords específicas de cavalo/montaria removidas do topic_index
  - Decisão: RAG deve ser genérico — topic_index não deve hardcodar cenários específicos
  - p.431+432 mantidas no tópico queda_dano (as páginas são corretas e genéricas)
- **Diagnóstico de escala confirmado:** BM25 com limit=200 processa pool fixo — não degrada com 5000+ chunks
  - p.549 (tabela modificadores tiro) estava em rank #31 no BM25 puro → deslocada por chunks de Artes Marciais
  - topic_index resolve o displacement garantindo as páginas críticas independente do ranking BM25

### Lote 265: topic_index gerado do Índice do Livro — CONCLUÍDO | commit: 4179816
- **scripts/gerar_topic_index.py:** parse automático de `indice.md` + `glossario.md` → 515 tópicos
  - `PALAVRAS_GENERICAS`: descarta termos genéricos ("combate", "dano", "armas", etc.) como `require_all` sozinhos
  - Termos compostos (2+ palavras) sempre usam todas as palavras como `require_all` — mais precisos
  - `--merge`: mescla gerado com manuais preservando entradas existentes
- **topic_index.json:** 15 manuais + 515 gerados = **530 tópicos** (antes: 15)
  - Cobertura ~80-90% das regras do Módulo Básico contra displacement BM25
  - Nenhum `require_all` genérico sozinho nos tópicos gerados
- **Correção de regressão:** primeira versão tinha 546 gerados com `require_all` genéricos → disparavam em quase toda pergunta de combate, poluindo contexto da IA com páginas irrelevantes (camelos, centrum, bactérias)
  - Solução: `PALAVRAS_GENERICAS` descarta esses termos no `gerar_matching()` → tópico não é criado
- **Arquivos auxiliares:** `topic_index_gerado.json` (output do script, 515 tópicos), `topic_index_backup_manual.json` (11 tópicos pré-lote-264)

### Lote 266: busca semântica híbrida BM25 + embeddings ativada — CONCLUÍDO | commit: e5b7592
- **Problema:** `vec_chunks` estava sempre vazia — embeddings existiam no `chunks.jsonl` (3072 dims, gemini-embedding-001) mas nunca eram importados para o banco porque `prePopulateManual` só rodava quando `totalChunks == 0`
- **FichaDatabase.kt:** nova verificação independente — se `totalChunks > 0` mas `vecCount == 0`, reimporta só os embeddings sem apagar chunks
- **MestreIARepository.kt:** bump `CODEX_VERSION_CURRENT` 2→3 força reimportação completa em devices existentes
- **DialogsMestreIA.kt:** modo "📖 Dúvida" (RAG Auditor) reativado no menu (havia sido desabilitado no lote anterior)
- **Resultado confirmado no Logcat:** `TOTAL: 1197 chunks | 1197 embeddings semânticos` em ~57s (única vez)
- **Próximas aberturas:** zero reimportação — banco populado, versão `3` salva no SharedPreferences
- **Arquitetura ativa:** FTS4 → BM25 → reranking semântico cosseno (60% BM25 + 40% semântico) → TopicIndex → Top-30 → Gemini

## Lote 267 — [2026-05-25] DIAGNÓSTICO RAG + PLANO DE REFATORAÇÃO — PLANEJADO | análise: 4c2a8f9

### DIAGNÓSTICO: 3 Erros Estruturais Descobertos

**Erro 1 — BM25 não entende intenção semântica**
- Pergunta "me da a tabela de golpe fulminante" → busca por termos "golpe" + "fulminante"
- Pergunta "bloqueio funciona contra golpe fulminante" → busca pelos **mesmos termos**
- Resultado: **chunks idênticos retornados para intenções completamente diferentes**
- **Localização:** `domain/MestreIAGraphEngine.kt` linhas 26-27, 60-95 (BM25 scoring)
- **Impacto:** RAG ignora intenção da pergunta, pondera apenas frequência de keywords

**Erro 2 — comprimirChunk() destrói tabelas e blocos estruturados**
- Chunk página 558 (Tabela de Golpe Fulminante: 5.251 chars) é comprimido para score < 8.0
- Função busca sentenças contendo "golpe" ou "fulminante" e descarta o resto
- A **tabela numérica** (`**3** → resultado`, `**4** → resultado`) não é "sentença" no sentido esperado → é excluída
- **Localização:** `domain/MestreIAGraphEngine.kt` linhas 301-305 (formatarParaIA) e 332-353 (comprimirChunk)
- **Impacto:** Tabelas, listas, blocos estruturados chegam à IA incompletos ou desaparecem

**Erro 3 — TopicIndex scores = 0, anulando a garantia**
- TopicIndex injeta páginas críticas (p.557) no `chunksFinais`
- Mas essas páginas **não entram em `chunksPontuadosFinais`**, recebem score 0
- formatarParaIA() recebe score 0 → chamam comprimirChunk() → conteúdo destruído (Erro 2 agravado)
- **Localização:** `domain/MestreIAGraphEngine.kt` linhas 168 (adição) e 188 (scoresMap sem TopicIndex)
- **Impacto:** TopicIndex existe mas é inútil — garante injeção, mas conteúdo é destruído

### PLANO: 4 Correções Propostas

**Correção 1 — Score TopicIndex = 999 [FÁCIL]**
- Arquivo: `domain/MestreIAGraphEngine.kt` linha 188
- TopicIndex chunks sempre recebem ★★★ → texto completo
- 1 linha de código

**Correção 2 — Remover comprimirChunk() [CRÍTICO]**
- Arquivo: `domain/MestreIAGraphEngine.kt` linhas 301-305
- Remover lógica de compressão: sempre enviar chunks completos
- 1 linha de código (simplificação)
- **Racional:** Melhor 15 chunks completos que 30 comprimidos/truncados

**Correção 3 — Limite chunks: 30→15 [SIMPLES]**
- Arquivo: `domain/MestreIAGraphEngine.kt` linha 184
- Com texto completo, contexto cresce — menos chunks evita overflow
- 1 número mudado

**Correção 4 — Análise semântica de intenção [ALTO IMPACTO]**
- Arquivos: `domain/MestreIAPlanner.kt` + `domain/MestreIAUseCase.kt`
- Adicionar enum `IntencaoBusca` com 5 tipos (TABELA, EXPLICAÇÃO, REGRA, CÁLCULO, GERAL)
- Função `analisarIntencao(pergunta)` que classifica a intenção antes da busca
- Usar intenção em `MestreIAUseCase.kt` para ajustar query de busca antes de chamar RAG
- **Impacto:** IA controla a busca, não BM25 — respostas semanticamente corretas

### Evidência do Diagnóstico

**Logcat "preciso da tabela de golpe fuminante!"**
```
BM25 top-5: p.382(1pts) | p.400(1pts) | p.390(1pts) | p.389(1pts) | p.401(1pts)
Contexto final: 28 chunks | páginas: [...556, 557]
RAG OK: 28 chunks | 12632 chars de contexto

[RESPOSTA IA:]
"o conteúdo exato da tabela (os resultados de 3d) não está presente nos trechos do Códex fornecidos"
```

**Análise:**
1. Página 558 (tabela real) não está nos top-5 BM25 — **Erro 1 (BM25 ignora intenção)**
2. TopicIndex força p.557 com score 0 — **Erro 3 (score zero)**
3. p.557 é comprimida para ~600 chars, perde conteúdo — **Erro 2 (compressor destrói)**
4. IA recebe referências, não tabela → responde "não encontrada"

### Verificação Esperada (Após implementação)

**Teste 1:** "me da a tabela de golpe fulminante"
- Esperado: Tabela 3-18 **completa** em resposta

**Teste 2:** "é possível usar mágicas de Bloqueio contra um golpe fulminante?"
- Esperado: Regras de magia, **NÃO** lista de tabela

**Teste 3:** "como funciona um golpe fulminante?"
- Esperado: Explicação, **NÃO** tabela numérica

**Logcat esperado:**
- TopicIndex chunks: `[★★★]` (score 999)
- Contexto final: ≤ 15 chunks, **texto completo**
- Sem compressão lexical

### Arquivos Afetados
- `domain/MestreIAGraphEngine.kt` — Score, compressão, limite
- `domain/MestreIAPlanner.kt` — Enum + função análise intenção
- `domain/MestreIAUseCase.kt` — Usar intenção na busca

### Status
- ✅ Plano documentado em memory (`project_rag_refactoring.md`)
- ✅ Análise rastreada para próximos lotes
- ⏳ Implementação: Lote futuro priorizado

## Lote 268 — [2026-05-25] Implementação RAG: 4 Correções
- **Hash:** 6d3a2cc
- **Mudanças:**
  - **Correção 1:** TopicIndex score = 999 (garante páginas críticas chegam completas)
  - **Correção 2:** Removido `comprimirChunk()` (elimina destruição de tabelas numéricas)
  - **Correção 3:** Limite 30→15 chunks (compensa maior tamanho com qualidade)
  - **Correção 4:** Análise de intenção semântica (query ajustada antes da busca RAG)
- **Arquivos:** MestreIAGraphEngine.kt, MestreIAPlanner.kt, MestreIAUseCase.kt
- **Testes:** MestreIARagEngineTest.kt corrigido
- **Status:** ✅ BUILD OK, implementação concluída
- **Verificação pendente:** teste manual com 3 casos (tabela, regra, explicação)

## Lote 269 — [2026-05-25] Ajuste Prompt Auditor: Ferramentas + RAG
- **Hash:** e80aad0
- **Mudanças:**
  - Adicionado seção FERRAMENTAS DISPONÍVEIS (inspecionar_personagem, consultar_manual_direto, consultar_nexus_arcano)
  - inspecionar_personagem: nova diretriz de USO (contextualiza ao personagem real)
  - consultar_manual_direto: ajustado para "confiar no RAG" (Lote 268 entrega chunks completos)
  - Consolidado PROTOCOLO DE LACUNA (removida duplicata, linguagem simplificada)
  - Diretriz 3: PERSONALIZAÇÃO PELO PERSONAGEM (novo)
  - Alinhado com Lote 268 (RAG semântico + IntencaoBusca)
- **Status:** ✅ BUILD OK

## Lote 270 — [2026-05-25] Reset Arquitetural do AUDITOR — Sistema Semântico de Intenção — PLANEJADO
- **Problema raiz:** O sistema RAG do AUDITOR é léxico-cêntrico em 4 camadas independentes.
  Pesa palavras individualmente — `golpe_fulminante` (termo raro, IDF alto) domina o scoring
  mesmo quando é apenas o contexto da pergunta, não o sujeito da dúvida.
  Exemplo: "é possível usar magia de bloqueio contra golpe fulminante?" → sistema busca golpe_fulminante
  em vez de magia_de_bloqueio, porque golpe_fulminante tem IDF maior.
- **Erros catalogados (13):**
  1. BM25 pesa todos os tokens igualmente — sem distinção núcleo vs. contexto
  2. `analisarIntencao()` retorna enum plano — sem estrutura relacional (sujeito/alvo)
  3. `plano.termos.take(3)` sem ordenação — pode omitir o núcleo real da pergunta
  4. `gerarSubQueriesTemáticas` hardcoded — cenários fixos, miss em perguntas relacionais
  5. IDF calculado sobre pool de 200 candidatos (não corpus) — matemática corrompida
  6. Bonus AND +15.0 amplifica viés léxico independente do que importa
  7. Query OR plana — 20 tokens sem ordenação de relevância
  8. avgdl calculado do pool filtrado, não do corpus completo
  9. Sem estratégia de fallback quando gerarCatalogoDireto() falha
  10. Expansão bidirecional no dicionário — matches não controlados
  11. Comentário diz FTS5, código usa FTS4 (MestreIAQueryEngine)
  12. Comentário diz 384 dims, código usa 3072 (MestreIASemanticEngine)
  13. Referência a `consultar_grafo_regras` descontinuado em MestreIATools
- **Arquivos resetados (8):**
  - MestreIAPlanner.kt — TermoPonderado, IntencaoEstruturada, sub-queries dinâmicas
  - MestreIAQueryEngine.kt — tokens ordenados, camadas NEAR/AND/OR
  - MestreIAGraphEngine.kt — bonus proporcional, avgdl/IDF reais, take(30), Pocket RAG real
  - MestreIAUseCase.kt — queryAjustada por núcleo, busca em camadas com fallback
  - MestreIATopicIndex.kt — correção de comentário apenas
  - MestreIASemanticEngine.kt — correção de comentário apenas
  - MestreIAPromptsAuditor.kt — sem mudança funcional
  - MestreIATools.kt — remove referência a consultar_grafo_regras
- **Arquivos não tocados (infraestrutura compartilhada):**
  - MestreIAClient.kt, MestreIARepository.kt, FichaIADelegate.kt, GeminiLiveTools.kt, MestreIADatabase.kt
- **Contratos externos preservados:**
  - `MestreIAUseCase.conversarComMestreIA(prompt, modo, onStatusUpdate, onChunk, onResultado)`
  - `MestreIAGraphEngine.buscarDiretoNoCodex(query, termosExtras, perguntaOriginal)`
  - `MestreIAPlanner.planejarBusca()` e `PlanoDeBusca.subQueriesTemáticas`
- **Sublotes:**
  - 270-A: MestreIAPlanner — TermoPonderado(termo, peso), IntencaoEstruturada(entidadePrimaria, entidadeSecundaria, relacao), sub-queries dinâmicas por estrutura relacional
  - 270-B: MestreIAQueryEngine — tokens ordenados por relevância, suporte a camadas NEAR/AND/OR
  - 270-C: MestreIAGraphEngine — bonus proporcional ao núcleo, avgdl/IDF reais (corpus global), take(30) + Pocket RAG real
  - 270-D: MestreIAUseCase — queryAjustada por termos de núcleo, busca em camadas com fallback
  - 270-E: MestreIATools — remove referência descontinuada; MestreIAPromptsAuditor — sem mudança
  - 270-F: MestreIATopicIndex + MestreIASemanticEngine — correção de comentários apenas (3072 dims, FTS4)
- **Hash do plano:** 3445c8e
- **Hash da implementação:** 2791ede
- **Hash do fix (formatarParaIA):** 01ac7bd — ordena chunks por score antes de groupBy(source_id); garante TopicIndex (score=999) apareça primeiro e não seja cortado pelo limite de 35000 chars
- **Build:** ✅ compilePracegoDebugKotlin OK — sem erros de compilação
- **Testes:** 17 falhas pré-existentes (NexusArcano/PersonagemRules), não introduzidas pelo Lote 270
- **Status:** ✅ IMPLEMENTADO + FIX APLICADO

## Lote 271 — [2026-05-25] Reset do AUDITOR: Busca Livre pela IA — CONCLUÍDO
- **Problema raiz:** O sistema RAG do AUDITOR entregava RECORTES comprimidos (`comprimirChunkPorSentencas`), não páginas completas. Mesmo quando o chunk correto era encontrado pelo BM25, o conteúdo relevante era cortado. Além disso, o Planner gerava queries fixas que podiam não cobrir todas as dimensões semânticas de perguntas complexas.
- **Decisão arquitetural (usuário):** Isolar completamente o AUDITOR do sistema RAG/Planner. Dar ao modelo liberdade total de investigação com ferramentas (até 5 tool calls). Prompt blindado para fonte de verdade = tools only.
- **Nova arquitetura (sem RAG pré-contexto):**
  - Modelo recebe a pergunta do usuário + ferramentas disponíveis
  - IA decide sozinha quais queries fazer, em que ordem e quantas vezes (máx. 5)
  - Cada tool call retorna o texto **completo** da página (sem compressão)
  - Se incerta após 5 buscas, IA pode fazer 1 pergunta ao usuário e 1 busca extra
  - Contexto acumulado dinamicamente (60.000 chars max)
- **Arquivos alterados (3):**
  - `MestreIAPromptsAuditor.kt` — reescrito: prompt blind (fonte de verdade = tools), protocolo de decomposição dimensional, exemplo de raciocínio correto, protocolo de busca até 5, quando não encontrar
  - `MestreIATools.kt` — descrições de `consultar_manual_direto` reescritas para Gemini e OpenAI: queries curtas (máx 6 palavras), decomposição por conceito isolado, exemplos bons/ruins
  - `MestreIAUseCase.kt` — loop `while(true)` substituindo Planner + multi-query + pre-context; `catalogoDinamico` começa vazio; `toolCallsFeitas` (max=5); resultado de cada tool labelado `=== BUSCA N ["query"]: ===`; limite de contexto 60k chars; `isUltimaIteracao` força resposta sem tools
- **Arquivos NÃO alterados:** Forjador (MestreIAGeneratorUseCase, MestreIAPromptsForjador, ForjadorTools) — completamente isolado
- **Arquivos agora dead code (não deletados ainda):** `MestreIAPlanner.kt`, `MestreIAQueryEngine.kt`, `MestreIASemanticEngine.kt` — não mais chamados pelo AUDITOR; `gerarCatalogoDireto()` e `reescreverQueryParaGurps()` em MestreIAUseCase
- **Build:** ✅ compilePracegoDebugKotlin OK + assemblePracegoDebug OK

---

## Commit `729c80a` — 2026-05-26
**feat: Gemini Live — session resumption, reconexão automática, RAG 40 chunks, log tokens**

### GeminiLiveService.kt
- **Session Resumption**: salva `newHandle` do `sessionResumptionUpdate` a cada turno; ao reconectar envia `sessionResumption: { handle }` no setup — servidor restaura sessão lógica completa com todo o contexto, sem reinjetar nada
- **Reconexão automática**: `goAway` e `onClosed` com code ≠ 1000 reconectam sozinhos após 1.5s; encerramento manual (code=1000) cancela o `Runnable` pendente via `removeCallbacks()` e limpa o token — resolve o bug de precisar clicar "Encerrar" duas vezes
- **Histórico de turnos**: `ArrayDeque` com últimos 5 pares `(pergunta→resposta)` — reinjetado como contexto quando session resumption não está disponível
- **Pergunta interrompida**: se conexão cai no meio do RAG (`webSocket=null` ao tentar enviar toolResponse), salva `ultimaPerguntaUsuario` e reinjetar na nova sessão para o modelo retomar
- **Log tokens**: `📊 Tokens — prompt: X | resposta: Y | total: Z` exibido após cada `turnComplete`
- **sessionResumptionUpdate**: suprimido do log de ruído (era spam a cada ~1s); token capturado silenciosamente em `Log.d`
- **Prompt**: instrução explícita para NÃO chamar `consultarManual` antes de saber qual é a dúvida — resolve chamada RAG prematura da sessão anterior

### GeminiLiveTools.kt
- **RAG voz**: `take(50)` → `take(40)` — redução ~26% nos tokens por consulta sem perda de qualidade comprovada em testes

### MestreIATools.kt + MestreIAGraphEngine.kt
- **Pyramid Aquático**: adicionado ao enum de livros Gemini e OpenAI; mapping `"pyramid aquático"/"pyramid aquatico"` → `pt_pyramid_26_underwater` no `buscarDiretoNoCodex`

### FichaIADelegate.kt
- **Loading phrases**: personalizadas com nome do personagem, até 2 desvantagens e 1 vantagem shuffled; sem exemplos de estilo no prompt (modo aleatório puro)

### Validado em logcat
- Pergunta "atirar em inimigo entre copas de árvore a 3m": 2 buscas paralelas, citou Pág. 374/550/551, calculou -2 distância + -2 cobertura parcial, ofereceu calcular chance final
- Tokens: `prompt: 122.694` vs `~165k` anterior com 50 chunks — redução confirmada
- Session resumption token atualizado a cada ~1s ✅

---

## Commit `458d955` — 2026-05-26
**Lote 1: Context Window Compression + Session Resumption transparent + goAway preventivo**

### GeminiLiveService.kt — buildSetupMessage()
- **`sessionResumptionConfig: { transparent: true }`**: servidor passa a gerenciar os tokens automaticamente e inclui `lastConsumedClientMessageIndex` no update — garante que mensagens enviadas durante queda não se percam; handle ainda enviado na reconexão quando disponível
- **`contextWindowCompression: { triggerTokens: 100000, slidingWindow: { targetTokens: 4000 } }`**: quando contexto passa de 100k tokens o servidor comprime automaticamente para ~4k — resolve o crescimento de 120k+ após 2-3 perguntas RAG; sessões agora rodam indefinidamente (sem limite de 15min)
- **`goAway.timeLeft` preventivo**: campo parsed — se `timeLeft > 5s` reconecta com delay de 500ms em vez de 1500ms, evitando interrupção perceptível ao usuário
- **`reconectarAutomaticamenteComDelay(motivo, delayMs)`**: refatoração para suportar delay variável; `reconectarAutomaticamente()` mantida como atalho com 1500ms padrão

### Build
- ✅ `assemblePracegoDebug` OK — 83 tasks up-to-date
- ✅ APK instalado em 192.168.1.84:44221

---

## Commit `17c9eca` — 2026-05-26
**Lote 2: Automatic Activity Detection (AAD) configurado no setup**

### GeminiLiveService.kt — buildSetupMessage()
- **`realtimeInputConfig.automaticActivityDetection`**: configura explicitamente sensibilidade de voz em vez de depender dos padrões do servidor
  - `startOfSpeechSensitivity: 0.5` — detecção balanceada de início de fala
  - `endOfSpeechSensitivity: 0.5` — menos falsas ativações de fim de turno
  - `prefixPaddingMs: 200` — captura 200ms antes do início detectado, evita cortar começo de palavras
  - `silenceDurationMs: 1000` — só considera turno encerrado após 1s de silêncio (evita cortes no meio da fala)

### Build
- ✅ `assemblePracegoDebug` OK
- ✅ APK instalado em 192.168.1.84:44221

---

## Commit `2b42b18` — 2026-05-26
**Lotes 3+4: migração para Gemini 2.5 + Proactive Audio + endpoint v1beta**

### app/build.gradle.kts
- **Modelo**: `gemini-3.1-flash-live-preview` → `gemini-2.5-flash-preview-native-audio-dialog`
  - Async Function Calling ✅ — RAG não bloqueia mais o WebSocket durante busca
  - Proactive Audio ✅ — modelo ignora barulho de fundo
  - Affective Dialogue ✅ — adapta tom ao estado emocional
  - 30 vozes HD em 24 idiomas

### GeminiLiveService.kt
- **`proactiveAudioConfig: { enabled: true }`**: modelo só responde quando a fala é direcionada a ele — ignora TV ligada, conversas ao redor, barulho de fundo
- **Endpoint**: `v1alpha` → `v1beta` — versão estável da API

### Build
- ✅ `assembleDebug` OK
- ✅ APK visual instalado em 192.168.1.84:44221


---

## Lote 272 — [2026-05-26] Fix Race Condition Bug #2117 — micReleaseJob

- **Hash:** e3f1916
- **Mudanças:**
  - `GeminiLiveService.kt`: adicionado `micReleaseJob: Job?` para rastrear o timer de liberação do mic
  - No início de novo turno de áudio: `micReleaseJob?.cancel()` antes de `limparFilaAudio()` — cancela o timer do turno anterior
  - No `turnComplete`: `micReleaseJob = scope.launch { ... }` em vez de `scope.launch { ... }` — permite rastrear e cancelar
  - Adicionado `if (!isActive) return@launch` no início do bloco do timer — garante que job cancelado não libera mic
  - No `encerrar()`: `micReleaseJob?.cancel()` adicionado ao cleanup
- **Causa do bug:** quando o modelo respondia rápido (turno 2 antes do timer do turno 1 expirar), o timer antigo chamava `modeloFalando = false` no meio do áudio novo → reprodução acelerada e sobreposição de conversas
- **Status:** ✅ Build OK

## Lote 273 — [2026-05-26] Fix Dupla Saudação — Live ativo não gera saudação de texto

- **Hash:** 18b22e1
- **Mudanças:**
  - `FichaScreen.kt` linha 539: `LaunchedEffect(Unit)` agora verifica `estadoLive` antes de chamar `gerarSaudacaoMestreIA()`
  - Se Live estiver CONECTANDO, OUVINDO ou FALANDO → saudação de texto não é gerada
  - Se Live estiver OCIOSO ou ERRO → comportamento anterior (gera saudação de texto normalmente)
- **Causa do bug:** segurar o ícone do Mestre IA iniciava o GeminiLive, que ao chegar em OUVINDO abria o dialog, que disparava o `LaunchedEffect(Unit)`, gerando saudação de texto E saudação por voz ao mesmo tempo
- **Status:** ✅ Build OK

## Lote 274 — [2026-05-26] Fix lerFicha(atributos) sem nome do personagem

- **Hash:** 22b9e56
- **Mudanças:**
  - `ForjadorToolExecutor.kt` linha 52: adicionado `Nome: ${p.nome}` no início do bloco `atributos`
  - Nome só aparece se não estiver em branco
- **Causa do bug:** `lerFicha("atributos")` retornava apenas ST/DX/IQ/HT/PV/PF/Aptidão — sem o nome. O modelo chamava essa ferramenta na saudação, não encontrava o nome, e ficava sem saber quem era o personagem. O bloco `secao == "tudo"` incluía o nome, mas o modelo nunca chamava "tudo", sempre "atributos".
- **Confirmado no logcat:** linha 91 — resultado da tool = `ST: 11 | DX: 16 | IQ: 12 | HT: 11 | PV: 11 | PF: 11 | Aptidão Mágica: 0` (sem nome). Pensamento do modelo (linha 101) confirmou: "I've hit a snag: the lerFicha output lacks a character name"
- **Status:** ✅ Build OK

## Lote 275 — [2026-05-26] Contexto ficha completo no Live + saudação com nome

- **Hash:** 6c02ada
- **Mudanças:**
  - `FichaScreen.kt`: substituído `"Nome: ${p.nome}, Pontos restantes: X"` (49 chars) por `MestreIAContextFilter.gerarContexto(personagem, "conversa")` — envia ficha completa (nome, atributos, HP/FP, vantagens, desvantagens, perícias, aparência, histórico)
  - `GeminiLiveService.kt`: saudação agora extrai o nome diretamente do contexto injetado e passa explicitamente no prompt — `"Cumprimente [Nome] pelo nome de forma breve e natural"`; se não tiver nome, cai no fallback genérico
- **Motivo:** modelo recebia apenas 49 chars de contexto, sem nome completo. Agora recebe a ficha toda antes de qualquer ferramenta, eliminando a necessidade de chamar `lerFicha` só para saber o nome na saudação
- **Status:** ✅ Build OK

## Lote 276 — [2026-05-26] Fix interrupted + AudioTrack VOICE_COMMUNICATION

- **Hash:** 47e859e
- **Mudanças:**
  - `GeminiLiveService.kt`: quando `interrupted=true`, agora zera `bytesAudioTurno=0`, cancela `micReleaseJob` e libera o mic imediatamente — antes o turno interrompido deixava os bytes acumulados do turno anterior, e quando chegava o `turnComplete` criava um timer fantasma de 2280ms causando áudio acelerado
  - `GeminiLiveService.kt`: `AudioTrack` trocado de `USAGE_MEDIA` para `USAGE_VOICE_COMMUNICATION` — alinha com o `AudioRecord` que já usava `VOICE_COMMUNICATION`. Isso faz o Android tratar como chamada de voz com cancelamento de eco, evitando conflito entre saída e entrada de áudio (causava mic mudo/silencioso por vários segundos)
- **Evidência no logcat:** linha 211-225 — `interrupted` seguido de `aguardando 2280ms` com bytes do turno anterior. Linhas 109-162 — `audioRecordData [mute]` por 16+ segundos com `f` parado (mic capturando silêncio)
- **Status:** ✅ Build OK

## Lote 277 — [2026-05-26] Habilitar transcrição de áudio entrada e saída

- **Hash:** 66f70a1
- **Mudanças:**
  - `GeminiLiveService.kt` setup: adicionado `outputAudioTranscription: {}` e `inputAudioTranscription: {}` no `generationConfig`
- **Causa:** o setup só pedia `responseModalities: ["AUDIO"]` sem habilitar transcrição. O servidor não mandava `outputTranscription` nem `inputTranscription` nas mensagens, então `pendingTextoFallback` ficava sempre vazio e `onRespostaMestre` nunca era chamado — nada aparecia no chat
- **Status:** ✅ Build OK

## Lote 278 — [2026-05-26] v1beta → v1alpha + proactiveAudio

- **Hash:** 34c982b
- **Mudanças:**
  - `GeminiLiveService.kt`: endpoint `v1beta` → `v1alpha` — necessário para usar proactiveAudio e affectiveDialogue
  - `GeminiLiveService.kt`: adicionado `proactivity: { proactiveAudio: true }` no setup — modelo ignora barulho de fundo, TV ligada, conversas ao redor; só responde quando a fala é claramente direcionada a ele
- **Motivo:** `proactiveAudio` e `affectiveDialogue` só funcionam em `v1alpha`. A migração para `v1beta` foi feita como "versão estável" mas não havia dependência técnica específica
- **Risco:** se o setup travar (não responder `setupComplete`), reverter para `v1beta` e remover `proactivity`
- **Status:** ✅ Build OK — aguardando teste

## Lote 279 — [2026-05-26] Remove proactiveAudio — trava setup

- **Hash:** e61a97b
- **Mudanças:**
  - `GeminiLiveService.kt`: removido `proactivity: { proactiveAudio: true }` — servidor não respondia `setupComplete`, app ficava travado em "Conectando..."
  - Mantido `v1alpha` — não causou problema, só o proactiveAudio travava
- **Conclusão:** `proactiveAudio` incompatível com `gemini-2.5-flash-native-audio-preview-12-2025`, igual ao `proactiveAudioConfig` removido no commit 96c038a
- **Status:** ✅ Build OK

## Lote 280 — [2026-05-26] Fix posição dos campos de transcrição no setup

- **Hash:** 83c86d9
- **Mudanças:**
  - `GeminiLiveService.kt`: movido `outputAudioTranscription` e `inputAudioTranscription` para fora do `generationConfig` — devem ficar no nível do `setup`, não dentro de `generationConfig`
- **Causa do travamento:** campos dentro de `generationConfig` eram rejeitados silenciosamente pelo servidor — `setupComplete` nunca chegava, app ficava em "Conectando..." indefinidamente
- **Confirmado na doc:** `BidiGenerateContentSetup` tem esses campos no nível raiz, não dentro de `generationConfig`
- **Status:** ✅ Build OK — aguardando teste

## Lote 281 — [2026-05-26] Forçar saída de áudio nas caixas de som

- **Hash:** b47092e
- **Mudanças:**
  - `GeminiLiveService.kt` `iniciarCaptura()`: ativa `AudioManager.MODE_IN_COMMUNICATION` + `isSpeakerphoneOn = true` — mantém cancelamento de eco do `VOICE_COMMUNICATION` mas força saída pelo alto-falante (caixas de som), não pelo ouvido
  - `GeminiLiveService.kt` `encerrar()`: restaura `isSpeakerphoneOn = false` + `MODE_NORMAL` ao fechar a sessão — evita deixar o celular preso em modo chamada
- **Causa:** `USAGE_VOICE_COMMUNICATION` no AudioTrack roteava o áudio para o alto-falante do ouvido (como ligação telefônica). Solução: manter o modo mas forçar speakerphone
- **Status:** ✅ Build OK

## Lote 282 — [2026-05-26] Fix transcrição usuário — cada palavra num balão

- **Hash:** b80f5cd
- **Mudanças:**
  - `GeminiLiveService.kt`: adicionado `pendingTextoUsuario` — acumula fragmentos do `inputTranscription` igual ao que já fazíamos com `outputTranscription`
  - `onTranscricaoUsuario` agora só é chamado **uma vez** no `turnComplete` com o texto completo — não mais a cada fragmento
  - `pendingTextoUsuario` zerado no `interrupted` e no `turnComplete`
- **Causa:** `inputTranscription` chega fragmentado palavra por palavra (igual ao output). Cada fragmento chamava `onTranscricaoUsuario` → cada palavra virava um balão separado no chat
- **Status:** ✅ Build OK

## Lote 283 — [2026-05-26] Fix keepalive não pausava durante fala do modelo

- **Hash:** 1d233ae
- **Mudanças:**
  - `GeminiLiveService.kt`: keepalive agora checa `modeloFalando` antes de enviar silêncio — `if (modeloFalando) continue`
- **Causa:** O keepalive enviava áudio silencioso a cada 20s independente do estado. Se o ciclo de 20s caísse logo após o modelo terminar de falar, o servidor recebia silêncio no exato momento em que o usuário começa a falar — podendo confundir o detector de fala (VAD) e atrasar o reconhecimento da voz do usuário
- **Status:** ✅ Build OK

## Lote 284 — [2026-05-26] Fix timer de mic muito longo — usar generationComplete

- **Hash:** cf8dfda
- **Mudanças:**
  - `GeminiLiveService.kt`: adicionado `bytesAudioGerado` — congelado no `generationComplete`
  - Timer do `micReleaseJob` agora usa `bytesAudioGerado` em vez de `bytesAudioTurno`
  - `bytesAudioGerado` zerado no início de cada turno e no `interrupted`
- **Causa:** O `turnComplete` chegava ~4s depois do `generationComplete`. Nesse intervalo o servidor enviava silêncio de cauda que era somado ao `bytesAudioTurno`, inflando o timer (7960ms para uma fala de ~4s). Resultado: o mic ficava bloqueado por quase o dobro do necessário — o usuário falava antes do timer acabar e a voz era descartada
- **Status:** ✅ Build OK

## Lote 285 — [2026-05-26] Fix 4 bugs confirmados + logs de diagnóstico nos 3 não confirmados

- **Hash:** 54304d4
- **Mudanças:**
  - `sessaoAtiva` agora é `@Volatile` — jobs em threads IO viam valor desatualizado após `encerrar()`
  - `turnComplete` sem áudio: `micReleaseJob` com `duracaoMs == 0` libera mic imediatamente com log `(turno sem áudio)` — antes `modeloFalando` ficava `true` para sempre
  - `bytesAudioTurno` e `bytesAudioGerado` zerados ao final do `turnComplete` — antes o valor vazava para o próximo turno se `generationComplete` não chegasse
  - `reproducaoJob = null` após cancel em `encerrar()` — canal antigo fechado antes do job ser nulificado
  - **Logs de diagnóstico adicionados:**
    - `audioRecord.read()` negativo → log `⚠ audioRecord.read() retornou erro`
    - `audioTrack.write()` negativo → log `⚠ audioTrack.write() retornou erro`
    - `pendingTextoUsuario > 2000 chars` → log `⚠ pendingTextoUsuario muito grande`
    - `limparFilaAudio > 50 chunks` → log `⚠ limparFilaAudio: canal estava muito cheio`
- **Status:** ✅ Build OK

## Lote 286 — [2026-05-26] Fix voz do usuário completamente suprimida pelo echo canceller

- **Hash:** 5fe618c
- **Mudanças:**
  - `iniciarCaptura()`: `AudioManager.MODE_IN_COMMUNICATION` → `MODE_NORMAL` + `isSpeakerphoneOn = true`
  - `AudioRecord`: `MediaRecorder.AudioSource.VOICE_COMMUNICATION` → `AudioSource.MIC`
- **Causa:** `MODE_IN_COMMUNICATION` + `VOICE_COMMUNICATION` ativa echo canceller agressivo de chamada telefônica. Logcat mostrava `s:100` (100 frames suprimidos) — servidor nunca recebia a voz do usuário, zero `inputTranscription`. `MODE_NORMAL` desativa o canceller de chamada; `isSpeakerphoneOn=true` mantém o som saindo pelas caixas.
- **Status:** ✅ Build OK

## Lote 287 — [2026-05-26] Fix transcrição do usuário não aparecia no chat

- **Hash:** 945a104
- **Mudanças:**
  - `GeminiLiveService.kt`: bloco `inputTranscription` movido para **antes** do bloco `turnComplete`
- **Causa:** servidor manda `inputTranscription` e `turnComplete` no **mesmo JSON**. O código processava `turnComplete` primeiro — lia `pendingTextoUsuario`, exibia, e o **zerava**. Depois processava `inputTranscription` e acumulava no buffer já vazio. No próximo JSON não vinha mais `turnComplete`, então o texto ficava preso e nunca era exibido. Solução: processar `inputTranscription` antes do `turnComplete` dentro do mesmo JSON.
- **Status:** ✅ Build OK

## Lote 288 — [2026-05-26] Fix 3 bugs de lógica no processamento de transcrição e histórico

- **Hash:** 4e2a652
- **Mudanças:**
  - `pendingTranscricaoModelo` — novo buffer separado para `outputTranscription`; antes acumulava no `pendingTextoFallback` junto com `text` parts, causando texto duplicado ou garbled no chat
  - `turnComplete`: resposta exibida no chat usa `outputTranscription` se disponível, `text` parts como fallback — não mistura os dois
  - `interrupted`: limpa `pendingTranscricaoModelo` junto com os outros buffers
  - Histórico de turnos: usa `textoUsuario` do turno atual em vez de `ultimaPerguntaUsuario` (que poderia ter valor de turno anterior caso o usuário ficasse em silêncio)
- **Bugs corrigidos:**
  1. `outputTranscription` misturado com `text` → texto dobrado/garbled no chat do modelo
  2. `outputTranscription` chegando no mesmo JSON que `turnComplete` → mesma race condition que o `inputTranscription` tinha (agora processado antes do `turnComplete`)
  3. Histórico salvava pergunta de turno anterior quando turno atual não tinha fala do usuário
- **Status:** ✅ Build OK

## Lote 289 — [2026-05-26] Fix mic liberado tarde demais — timer não descontava tempo já reproduzido

- **Hash:** 7417dce
- **Mudanças:**
  - `inicioAudioTurno`: novo timestamp gravado no primeiro chunk de áudio do turno
  - `turnComplete`: timer = `duracaoTotal - jaDecorrido + 300ms` em vez de `duracaoTotal + 300ms`
  - `inicioAudioTurno` zerado no `interrupted` e ao final do `turnComplete`
- **Causa:** o modelo começa a enviar áudio ~10s antes do `turnComplete` chegar. O timer usava a duração total (ex: 10s), mas o áudio já estava tocando há 10s — então o mic só abria 10s depois do `turnComplete`, quando na verdade já devia abrir quase imediatamente. No log: `generationComplete` às 18:55:38, `turnComplete` às 18:55:45 (7s depois), timer calculou 10s → mic abriu às 18:55:55 (17s após o fim real do áudio).
- **Status:** ✅ Build OK

## Lote 290 — [2026-05-26] Fix transcrição do usuário não aparecia no chat (UI)

- **Hash:** 20ba9d4
- **Mudanças:**
  - `FichaScreen.kt`: `onTranscricaoUsuario` agora abre o dialog igual ao `onRespostaMestre`
- **Causa:** `onTranscricaoUsuario` chamava `adicionarMensagemVoz` mas não abria o dialog. Se o chat estivesse fechado quando o usuário falava, a pergunta era adicionada à lista mas o dialog não abria — o usuário nunca via sua própria pergunta aparecer. A resposta do modelo abria o dialog, mas a pergunta já tinha passado.
- **Status:** ✅ Build OK

## Lote 291 — [2026-05-26] Fix áudio acelerado após correção do timer de mic

- **Hash:** 0f8197a
- **Mudanças:**
  - Timer do mic: trocado `inicioAudioTurno` (tempo de rede) por `audioTrack.playbackHeadPosition` (frames reais reproduzidos pelo hardware)
  - Removida variável `inicioAudioTurno` (não mais necessária)
- **Causa:** Lote 289 usava `System.currentTimeMillis() - inicioAudioTurno` para estimar quanto já foi reproduzido. Mas o AudioTrack tem buffer interno — os chunks chegam da rede mais rápido do que o hardware os toca. Quando o servidor enviava chunks em ritmo próximo ao de reprodução, `jaDecorridoMs` ficava próximo de `duracaoTotal`, o timer virava ~300ms, e o AudioTrack ainda tinha vários segundos de buffer pendente acelerando para esvaziar. `playbackHeadPosition` mede frames hardware reais — independente do buffer de rede.
- **Status:** ✅ Build OK

## Lote 292 — [2026-05-26] Fix transcrição do usuário: streaming imediato em vez de esperar turnComplete

- **Hash:** edeac27
- **Mudanças:**
  - `GeminiLiveService.kt`: novo callback `onAtualizarTranscricaoUsuario` — chamado com fragmentos seguintes e versão final consolidada
  - `inputTranscription`: primeiro fragmento chama `onTranscricaoUsuario` (cria entrada no chat), fragmentos seguintes chamam `onAtualizarTranscricaoUsuario` (atualiza a entrada existente)
  - `FichaIADelegate.kt`: novo campo `ultimaMensagemVozUsuarioUid` + método `atualizarUltimaMensagemVozUsuario` — edita a mensagem existente pelo uid em vez de criar nova
  - `FichaViewModel.kt`: delegação de `atualizarUltimaMensagemVozUsuario`
  - `FichaScreen.kt`: registra o callback `onAtualizarTranscricaoUsuario`
- **Causa:** `onTranscricaoUsuario` só era chamado no `turnComplete`, que chega DEPOIS que o modelo termina de falar. Em turnos com toolCall (RAG), o `turnComplete` vinha apenas após o modelo responder completamente — o usuário não via sua própria frase no chat durante todo o processamento. Agora o primeiro fragmento abre o balão imediatamente e os seguintes atualizam o texto.
- **Status:** ✅ Build OK

## Lote 293 — [2026-05-26] Fix timer de mic: polling de playbackHeadPosition em vez de cálculo por bytes

- **Hash:** edeac27
- **Mudanças:**
  - `GeminiLiveService.kt`: removida lógica de timer baseada em `bytesAudioGerado / 48000`
  - Novo campo `framesInicioTurno`: captura `playbackHeadPosition` no primeiro chunk de áudio do turno
  - Polling no `micReleaseJob`: verifica a cada 100ms se o hardware parou de avançar além de `framesInicioTurno` por 200ms consecutivos — só então libera o mic
  - Bytes mantidos apenas para diagnóstico (log) — não usados para timer
- **Causa:** `bytesAudioTurno` acumulava bytes em excesso em turnos com toolCall (2.628.480 bytes = 54s para uma resposta de ~23s), causando timer inflado → mic bloqueado muito além do fim do áudio → quando finalmente liberava, o AudioTrack reproduzia o buffer acumulado em velocidade acelerada. O polling detecta o fim real da reprodução pelo hardware, independente do volume de dados.
- **Status:** ✅ Build OK

## Lote 294 — [2026-05-26] Fix mic travado: framesInicioTurno capturado antes do flush() que reseta o contador

- **Hash:** edeac27
- **Mudanças:**
  - `GeminiLiveService.kt`: movida captura de `framesInicioTurno` para DEPOIS de `limparFilaAudio()`, não antes
- **Causa:** `limparFilaAudio()` chama `audioTrack.flush()`, que reseta o `playbackHeadPosition` internamente. O código capturava `framesInicioTurno` antes do flush, guardando o valor do turno anterior (ex: 181.440). Depois do flush, o hardware reiniciava do zero (~0 frames). No polling, `frameAtual` (0..N) nunca ficava `> inicioTurno` (181.440) — o loop ficava preso para sempre, `modeloFalando` nunca virava `false`, e o app exibia "Falando..." indefinidamente. No log: `frames: inicio=181440 atual=175104` — atual menor que início, loop preso por 37s até o usuário fechar manualmente.
- **Status:** ✅ Build OK

## Lote 295 — [2026-05-26] Fix áudio acelerado: polling de estabilidade substituído por alvo determinístico

- **Hash:** edeac27
- **Mudanças:**
  - `GeminiLiveService.kt`: `micReleaseJob` agora aguarda `playbackHeadPosition >= inicioTurno + framesEsperados - 200` em vez de detectar "estabilidade por 200ms"
  - `framesEsperados = bytesAudioGerado / 2` (16-bit mono = 2 bytes por frame)
  - Timeout de segurança: duração esperada + 3s
- **Causa:** O polling anterior detectava quando o counter parava de avançar por 200ms. O `reproducaoJob` bloqueia no canal `audioChannel` esperando chunks — nesse instante o hardware esvazia o buffer interno brevemente antes do próximo chunk chegar. O polling interpretava esse pause como "fim da reprodução" e liberava o mic cedo. O áudio ainda bufferizado no AudioTrack continuava tocando acelerado (sem back-pressure). No log: turno 4 liberado com 14,96s mas esperado 23,12s — 8,16s de áudio ainda buffered.
- **Status:** ✅ Build OK

## Lote 296 — [2026-05-26] Fix áudio acelerado: causa raiz — overflow silencioso do audioChannel

- **Hash:** 11bcdba
- **Mudanças:**
  - `GeminiLiveService.kt`: `audioChannel` trocado de `capacity=200` para `Channel.UNLIMITED`
- **Causa raiz:** Canal tinha capacidade 200 chunks. `trySend()` retorna `false` silenciosamente quando cheio — sem log, sem erro. Para respostas longas (~40s ≈ 400 chunks), metade dos chunks era descartada. O AudioTrack reproduzia os 200 que entraram em velocidade normal e parava. O restante do áudio nunca chegava — ao usuário parecia áudio acelerado/truncado. Com `UNLIMITED`, o back-pressure é natural: `write()` bloqueia no hardware até ele consumir, sem descartar nada. Memória: 40s de áudio = ~1,9MB, trivial.
- **Status:** ✅ Build OK

## Lote 297 — [2026-05-26] Fix code=1007: interrompe áudio ao receber toolCall durante turno ativo

- **Hash:** edb4367 → 1a7d9b9
- **Causa raiz (logcat):** Gemini 2.5 async function calling emite `toolCall` **antes** do `generationComplete` — turno de áudio ainda aberto. O app enviava `toolResponse` com `turnoTemAudio=true`. O servidor ficava em estado inconsistente (70s silêncio) e fechava com `code=1007`.
- **Mudanças:**
  - `GeminiLiveService.kt`:
    - Quando `toolCall` chega com turno de áudio ativo: `limparFilaAudio()` imediatamente antes de processar (modelo já decidiu — áudio pendente é descartável)
    - Flag `aguardandoRespostaServidor`: controla estado visual PROCESSANDO e timeout (mic NÃO bloqueado — usuário pode interromper normalmente)
    - Timeout de 90s: reconecta se servidor não responder após toolResponse
    - `EstadoLive.PROCESSANDO` adicionado ao enum
  - `DialogsMestreIA.kt`: dot amarelo "processando..." + status "⚙️ Processando..."
  - `FichaCustomNavigationBar.kt`: `PROCESSANDO` mapeado para anel amarelo
- **Status:** ✅ Build OK

## Lote 298 — [2026-05-26] Fix code=1007: limita payload do consultarManual

- **Hash:** a65282b
- **Causa raiz (logcat):** 1º `consultarManual` com 18 chunks → funcionou. 2º `consultarManual` com 40 chunks → 61s silêncio + `code=1007`. Payload excedeu limite do servidor Gemini Live.
- **Mudanças:**
  - `GeminiLiveTools.kt`: `take(40)` → `take(20)` no path multi-query
  - `GeminiLiveTools.kt`: cap de 25.000 chars no resultado final — previne payload gigante em qualquer path
- **Status:** ✅ Build OK


## Lote 299 — [2026-05-26] Fix: reduz cap consultarManual de 25k para 18k chars

- **Hash:** ae2198f
- **Motivação:** Fórum Google confirma limite ~20KB para payloads na API Gemini. Cap anterior de 25k chars (~25KB) estava acima desse limite documentado. 18k chars (~18KB) fica conservadoramente abaixo e dentro da faixa empiricamente segura.
- **Mudanças:**
  - `GeminiLiveTools.kt`: cap de 25.000 → 18.000 chars no resultado do `consultarManual`
- **Status:** ✅ Build OK

## Lote 300 — [2026-05-26] Fix RAG: Planner detecta nomes de vantagens GURPS

- **Hash:** 2879e39
- **Causa raiz (logcat):** Query "reflexos em combate" era classificada como `GENERICO/combate` pelo grupo semântico do Planner. Sub-queries geradas ("combate ataque dano modificador manobra") puxavam páginas 379, 548, 327 (combate geral) — nunca p83 onde está o texto completo da vantagem. Chunk `pt_modulo_basico_p83_c1` (definição + bônus de defesa ativa) nunca entrava no resultado.
- **Mudanças:**
  - `MestreIAPlanner.kt`: mapa de ~40 vantagens/desvantagens GURPS conhecidas (`vantagensConhecidas`)
  - Quando nome exato detectado na query → entidadePrimaria = nome exato, relação = FUNCIONAMENTO, sub-query específica (ex: "reflexos em combate vantagem bonus defesa"), boost 1.2 nos termos
  - Remove sub-queries genéricas de combate que diluíam o ranking BM25+HNSW
- **Status:** ✅ Build OK

## Lote 301 — [2026-05-26] Fix: reconexão ao encerrar + watchdog de inatividade

- **Hash:** af856fb
- **Bug 1 (reconexão ao encerrar):** `encerrar()` parava `AudioRecord` antes de fechar o WebSocket. Servidor detectava parada abrupta do stream e fechava com `code=1008`. O `onClosed` interpretava como fechamento inesperado e reconectava automaticamente. Fix: flag `encerramentoIntencional` setada no início do `encerrar()`; WebSocket fechado ANTES do AudioRecord.
- **Bug 2 (silêncio de 57s):** Modelo respondeu por áudio sem chamar tool, servidor ficou 57s inativo até `code=1008`. Fix: watchdog de 50s — reinicia a cada mensagem recebida; expira sem mensagem → reconecta preventivamente.
- **Mudanças:**
  - `GeminiLiveService.kt`: flag `encerramentoIntencional`, reordenação de `encerrar()`, método `reiniciarWatchdog()`, `watchdogJob`
- **Status:** ✅ Build OK

## Lote 302 — [2026-05-26] Detecta compressão de contexto do servidor no log

- **Hash:** 833506e
- **Motivação:** Não havia forma de saber se o servidor compactou o contexto (system prompt, histórico) durante a sessão.
- **Mudanças:**
  - `GeminiLiveService.kt`: log W "COMPACTADO" quando `sessionResumptionUpdate.resumable=false`
  - `GeminiLiveService.kt`: log W "COMPRESSÃO DE CONTEXTO" quando prompt token count cai >500 em relação ao turno anterior
  - Campo `ultimoPromptTokenCount` para rastrear evolução dos tokens
- **Status:** ✅ Build OK

## Lote 303 — [2026-05-26] take(20) → take(30) no multi-query consultarManual

- **Hash:** aa40e72
- **Mudanças:** `GeminiLiveTools.kt`: take(20) → take(30) no path multi-query
- **Status:** ✅ Build OK

## Lote 310 — [2026-05-27] Auditoria linha a linha GeminiLiveService.kt — 5 correções

- **Hash:** 9c651ad
- **Escopo:** Pente fino completo no arquivo após acúmulo de patches incrementais.
- **Bug crítico (#2):** `modeloFalando` não era resetado quando o watchdog disparava. Nova sessão iniciava com mic permanentemente mudo. Fix: reseta `modeloFalando=false` e cancela `micReleaseJob` antes de `reconectarAutomaticamente()` no watchdog.
- **Bug #3:** `turnoTemAudio=false` e `modeloFalando=false` redundantes dentro do `else` do bloco NON_BLOCKING toolCall — já eram `false` ao entrar no `else`. Removidos.
- **Bug #4:** `ultimaPerguntaUsuario` atribuída duas vezes nos dois ramos do `if/else` de `inputTranscription`. Extraído para antes do `if`.
- **Bug #5:** `keepAliveJob` e `capturaJob` cancelados mas não nulificados em `encerrar()`. Assimetria com `reproducaoJob`. Corrigido com `= null`.
- **Bug #1:** Indentação incorreta de `limparFilaAudio()` na linha 852 (colagem manual sem ajuste). Corrigido.
- **Status:** ⏳ Aguardando build

## Lote 309 — [2026-05-27] Fix: WHEN_IDLE → SILENT no toolResponse (duplo turno de áudio)

- **Hash:** 3b8d3fb
- **Causa:** `scheduling=WHEN_IDLE` fazia o modelo gerar um **segundo turno de áudio** quando recebia o resultado da tool NON_BLOCKING — enquanto o primeiro turno ainda tocava. Resultado: 59 chunks descartados, corte abrupto, recomeço da resposta. Usuário ouvia a resposta "duas vezes".
- **Fix:** `scheduling=WHEN_IDLE` → `SILENT`: modelo incorpora o resultado da tool na resposta em andamento sem gerar novo turno de fala.
- **Status:** ⏳ Aguardando build

## Lote 308 — [2026-05-27] Fix: pergunta interrompida não era salva ao cair durante resposta

- **Hash:** abc1315
- **Causa:** `perguntaInterrompida` só era setada quando a sessão caía **durante tool call** (dentro do bloco `toolCall` com `webSocket==null`). Se a sessão caísse enquanto o modelo estava **gerando resposta** (sem tool call ativa), a pergunta do usuário era perdida e a reconexão não retomava.
- **Observado:** usuário perguntou "explique reflexos em combate" → `code=1011` durante resposta → reconectou → modelo disse "lembro onde paramos" mas **não chamou nenhuma tool** e encerrou.
- **Fix:** `onClosed` salva `ultimaPerguntaUsuario` em `perguntaInterrompida` antes de `encerrar()` resetar os campos, para qualquer fechamento inesperado (`code != 1000`).
- **Status:** ⏳ Aguardando build

## Lote 307 — [2026-05-27] Fix crash IllegalStateException no AudioRecord.stop()

- **Hash:** ba59716
- **Causa:** `encerrar()` chamado do thread OkHttp (`onClosed`) quando o `AudioRecord` já estava em estado inválido (liberado ou nunca iniciado). `native_stop()` lançava `IllegalStateException` → crash fatal.
- **Fix:** `try/catch` em `audioRecord?.stop()`, `audioRecord?.release()`, `audioTrack?.stop()`, `audioTrack?.release()` — encerramento nunca mais crashar por estado de hardware.
- **Status:** ⏳ Aguardando build

## Lote 306 — [2026-05-27] Mitigação bug <ctrl46>: NON_BLOCKING + detector

- **Hash:** b6df4dd
- **Motivação:** Bug confirmado do Google: `<ctrl46>` emitido em vez de áudio após múltiplas tool calls. Hipótese: ciclo silêncio-de-espera → retomada de fala repetido N vezes dispara o bug.
- **Mudanças — `GeminiLiveService.kt`:**
  - `buildFuncao()`: novo parâmetro `nonBlocking: Boolean` — adiciona `"behavior": "NON_BLOCKING"` na declaração da tool
  - `buscarCatalogo` e `consultarManual`: marcados como `nonBlocking = true` → modelo fala enquanto a tool processa
  - `toolResponse`: `scheduling=WHEN_IDLE` injetado dentro do `response` — entrega resultado quando modelo terminar de falar
  - `toolCallCount`: contador de tool calls por sessão — logado em cada `toolCall` e no detector de `<ctrl46>`
  - Detector `<ctrl46>`: ao detectar token em `outputTranscription`, loga com nível ERROR incluindo `tc=N` (número de tool calls na sessão) para mapear o limiar exato
  - `encerrar()`: reseta `toolCallCount = 0`
- **Status:** ⏳ Aguardando build no Android Studio

## Lote 305 — [2026-05-27] Pesquisa e planejamento: mitigação do bug <ctrl46>

- **Hash:** 075501c
- **Motivação:** Bug confirmado do Google: modelo `native-audio-preview-12-2025` emite tokens `<ctrl46>` em vez de áudio PCM após múltiplas tool calls em sequência (observado na sessão: tc=2 → silêncio no tc=3). Causa silencios persistentes sem reconexão possível sem perda de contexto.
- **Status:** ✅ Pesquisa concluída

## Lote 304 — [2026-05-26] Fix falso alarme de compressão de contexto

- **Hash:** f4e0137
- **Causa:** `usageMetadata` vem vazio `{}` em alguns turnos (promptTokenCount=0). O detector interpretava 4915→0 como compressão real.
- **Fix:** Ignora prompt=0 na detecção e na atualização de `ultimoPromptTokenCount`.
- **Status:** ✅ Build OK
