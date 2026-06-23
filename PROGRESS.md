# Acompanhamento do Projeto da Ficha GURPS (Para Rodolfo)

**Última Atualização:** 19 de Junho de 2026
**Status Atual:** Lote_dados_3D 006 CONCLUÍDO — Acessibilidade PraCego na Rolagem 3D.
**Último Lote Registrado:** Lote_dados_3D 006 — última entrada deste arquivo
**HEAD (branch GURPS-Saga):** Lotes recentes: Lote 001=429865b, Lotes 002/003=64d4223, Lote 004=585f040, Lote 005=c3f0c47, Lote 006=448c674.

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


## Lote 304 — [2026-05-26] Fix falso alarme de compressão de contexto

- **Hash:** f4e0137
- **Causa:** `usageMetadata` vem vazio `{}` em alguns turnos (promptTokenCount=0). O detector interpretava 4915→0 como compressão real.
- **Fix:** Ignora prompt=0 na detecção e na atualização de `ultimoPromptTokenCount`.
- **Status:** ✅ Build OK

## Lote 305 — [2026-05-27] Pesquisa e planejamento: mitigação do bug <ctrl46>

- **Hash:** 075501c
- **Motivação:** Bug confirmado do Google: modelo `native-audio-preview-12-2025` emite tokens `<ctrl46>` em vez de áudio PCM após múltiplas tool calls em sequência (observado na sessão: tc=2 → silêncio no tc=3). Causa silencios persistentes sem reconexão possível sem perda de contexto.
- **Status:** ✅ Pesquisa concluída

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

## Lote 307 — [2026-05-27] Fix crash IllegalStateException no AudioRecord.stop()

- **Hash:** ba59716
- **Causa:** `encerrar()` chamado do thread OkHttp (`onClosed`) quando o `AudioRecord` já estava em estado inválido (liberado ou nunca iniciado). `native_stop()` lançava `IllegalStateException` → crash fatal.
- **Fix:** `try/catch` em `audioRecord?.stop()`, `audioRecord?.release()`, `audioTrack?.stop()`, `audioTrack?.release()` — encerramento nunca mais crashar por estado de hardware.
- **Status:** ⏳ Aguardando build

## Lote 308 — [2026-05-27] Fix: pergunta interrompida não era salva ao cair durante resposta

- **Hash:** abc1315
- **Causa:** `perguntaInterrompida` só era setada quando a sessão caía **durante tool call** (dentro do bloco `toolCall` com `webSocket==null`). Se a sessão caísse enquanto o modelo estava **gerando resposta** (sem tool call ativa), a pergunta do usuário era perdida e a reconexão não retomava.
- **Observado:** usuário perguntou "explique reflexos em combate" → `code=1011` durante resposta → reconectou → modelo disse "lembro onde paramos" mas **não chamou nenhuma tool** e encerrou.
- **Fix:** `onClosed` salva `ultimaPerguntaUsuario` em `perguntaInterrompida` antes de `encerrar()` resetar os campos, para qualquer fechamento inesperado (`code != 1000`).
- **Status:** ⏳ Aguardando build

## Lote 309 — [2026-05-27] Fix: WHEN_IDLE → SILENT no toolResponse (duplo turno de áudio)

- **Hash:** 3b8d3fb
- **Causa:** `scheduling=WHEN_IDLE` fazia o modelo gerar um **segundo turno de áudio** quando recebia o resultado da tool NON_BLOCKING — enquanto o primeiro turno ainda tocava. Resultado: 59 chunks descartados, corte abrupto, recomeço da resposta. Usuário ouvia a resposta "duas vezes".
- **Fix:** `scheduling=WHEN_IDLE` → `SILENT`: modelo incorpora o resultado da tool na resposta em andamento sem gerar novo turno de fala.
- **Status:** ⏳ Aguardando build

## Lote 310 — [2026-05-27] Auditoria linha a linha GeminiLiveService.kt — 5 correções

- **Hash:** 9c651ad
- **Escopo:** Pente fino completo no arquivo após acúmulo de patches incrementais.
- **Bug crítico (#2):** `modeloFalando` não era resetado quando o watchdog disparava. Nova sessão iniciava com mic permanentemente mudo. Fix: reseta `modeloFalando=false` e cancela `micReleaseJob` antes de `reconectarAutomaticamente()` no watchdog.
- **Bug #3:** `turnoTemAudio=false` e `modeloFalando=false` redundantes dentro do `else` do bloco NON_BLOCKING toolCall — já eram `false` ao entrar no `else`. Removidos.
- **Bug #4:** `ultimaPerguntaUsuario` atribuída duas vezes nos dois ramos do `if/else` de `inputTranscription`. Extraído para antes do `if`.
- **Bug #5:** `keepAliveJob` e `capturaJob` cancelados mas não nulificados em `encerrar()`. Assimetria com `reproducaoJob`. Corrigido com `= null`.
- **Bug #1:** Indentação incorreta de `limparFilaAudio()` na linha 852 (colagem manual sem ajuste). Corrigido.
- **Status:** ⏳ Aguardando build

## Lote 311 — [2026-05-27] Diagnóstico: dois monitores de aceleração de áudio

- **Hash:** ac6e0ea
- **Diagnóstico 1 — delta entre chunks:** dentro do `reproducaoJob`, loga para cada chunk o tamanho, duração teórica (bytes/48000) e delta real de chegada. Se `deltaCheg < duracaoTeórica/2` → loga `⚠ ACUMULANDO`. Indica que chunks chegam mais rápido do que o hardware os consome.
- **Diagnóstico 2 — monitor periódico `playbackHeadPosition`:** coroutine `audioMonitorJob` que a cada 500ms (enquanto `modeloFalando=true`) mede a taxa real de avanço do hardware em fps. 24000fps = normal. Emojis: 🟢 normal / 🟡 leve / 🔴 ACELERADO / 🔵 lento.
- **Status:** ⏳ Aguardando build e teste

## Lote 312 — [2026-05-27] feat: AcousticEchoCanceler + mic sempre aberto

- **Hash:** 703cf38
- **Motivação:** Pesquisa em repositórios reais (GeminiLive-Assistant-Android, android/ai-samples) mostrou que o padrão correto é usar cancelamento de eco em hardware e deixar o mic aberto o tempo todo — não bloqueio de software.
- **AcousticEchoCanceler:** ativado em hardware logo após criar o `AudioRecord`. Cancela o eco do speaker no microfone — modelo não ouve a si mesmo.
- **Mic sempre aberto:** removido `if (modeloFalando) continue` no loop de captura e no keepAlive. O AEC garante que o eco não chega ao servidor.
- **Resultado esperado:** usuário pode falar e interromper o modelo a qualquer momento, sem janela de silêncio forçado.
- **Status:** ⏳ Aguardando build e teste

## Lote 313 — [2026-05-28] docs: relatório DRY de duplicações + plano para §1 (normalização de texto)

- **Hash:** 4340056
- **Escopo:** Análise — nenhum código de produção alterado.
- **Entrega 1:** novo arquivo `.agent/skills/RELATORIO_DRY_DUPLICACOES.md` documentando **11 padrões** de código duplicado no projeto Android (~380–450 linhas elimináveis). Inclui evidências por arquivo:linha, exemplos de código, diagnóstico, sugestão de refatoração e plano em 3 lotes (A: RAG/buscas → B: UI → C: infra).
- **Entrega 2:** plano detalhado para o §1 do relatório (normalização de texto — 7 implementações paralelas), em 7 etapas reversíveis, com rede de segurança via testes antes de qualquer refatoração. Plano apresentado em linguagem de funcionalidade (sem tecniquês) para o usuário aprovar.
- **Próximos passos sugeridos:** após aprovação do usuário, executar a Etapa 1 (baseline de testes) do plano §1 — ainda não iniciado.
- **Status:** ✅ Relatório entregue; refatoração aguardando aprovação.

## Lote 314 — [2026-05-28] refactor: §1 do relatório DRY — TextNormalizer centralizado (6 de 7 migradas)

- **Hashes (branch `refactor/text-normalizer`, mesclada em `feature/mestre-ia-graphrag`):**
  - `eecb0e8` — Etapa 2: cria TextNormalizer (sem ligar ainda) + 18 testes
  - `fdb573d` — Etapa 3: CatalogFilters.normalizarBusca delega ao TextNormalizer
  - `6657a4c` — Etapa 4: DialogsTecnicas delega ao CatalogFilters (apaga função privada)
  - `cb6797d` — Etapa 5: SkillEngine + DataRepository (normalizarComparacao + normalizarChaveClasse) delegam
- **Escopo:** unifica 6 das 7 implementações paralelas de normalização de texto. Cria preset central `TextNormalizer` em `domain/filters/` com 4 modos (`SIMPLE`, `BUSCA_PADRAO`, `PERICIA_RAW`, `ARMA_GRUPO`).
- **Branch isolada:** trabalho feito em `refactor/text-normalizer` ("modo paranoico" — protegeu a principal). Mesclada após validação.
- **Cobertura nova:** 18 testes do `TextNormalizer` cobrindo cada modo, incluindo o `ARMA_GRUPO` (Mestre de Armas) que historicamente não tinha teste.
- **Validação visual:** 5 cenários de busca testados no app pelo usuário (catálogos + Técnicas) — 0 regressões.
- **Validação automatizada:** baseline preservada (130 verdes, 17 vermelhos pré-existentes não-relacionados) em cada uma das 4 etapas commitadas.
- **Adiamento consciente — Etapa 6 (Mestre de Armas):** decisão deliberada de não migrar `MestreDeArmasRule.normalize` por risco de regressão silenciosa em cálculo de bônus de dano sem ficha de teste pronta. Preset `ARMA_GRUPO` já implementado e testado, esperando migração futura quando o usuário tiver ficha de validação.
- **Linhas eliminadas:** ~58 (líquido após criar 102 de TextNormalizer + testes).
- **Status:** ✅ Mesclado em `feature/mestre-ia-graphrag` (ainda não pushed para origin).

## Lote 315 — [2026-05-28] fix: Auditor RAG — verificador de citações + topK 15 + prompt anti-alucinação

- **Hash:** a46d36a
- **Escopo:** 3 mudanças coordenadas para resolver alucinação confiante do Auditor (caso real:
  modelo citou "[Módulo Básico, pág. 174]" com -5 inventado para "escalar com uma mão",
  regra que não existe no GURPS). Diagnóstico completo em `.agent/skills/DIAGNOSTICO_AUDITOR_RAG.md`.

### Motivação (evidência do logcat)
- Modelo fez 5 buscas honestas, declarou "não vi resultados nos fragmentos", mas mesmo assim
  inventou página específica (174) e número (-5) usando "conhecimento padrão de GURPS".
- Prompt sozinho já proibia isso (linha 11-14), mas modelo desobedeceu — comportamento típico
  de LLMs que preferem "ser útil" a admitir desconhecimento.
- Logcat também revelou que `MODO_HNSW_PURO=true` está LIGADO (BM25 desativado), e o RAG
  retornava 40+ chunks por busca, afogando o modelo em contexto.

### Mudança A — Verificador de Citações (novo)
- **Arquivo novo:** `domain/MestreIACitationValidator.kt` (~165 linhas).
- Extrai citações no formato `[Livro, Pág. X]` e `pág. NNN` da resposta do modelo.
- Compara cada citação contra páginas dos chunks que o RAG efetivamente retornou.
- Citações sem chunk correspondente são marcadas como "⚠️ não verificadas" e anexadas
  ao final da resposta com aviso visual para o usuário.
- **NÃO impede alucinação** (impossível com LLM atual) — apenas AVISA quando acontece.
- Integração em `MestreIAUseCase.kt` (~15 linhas modificadas) no ponto onde `respostaFinal` é montada.

### Mudança B — HNSW topK 50→15
- **Arquivo:** `domain/MestreIAGraphEngine.kt`.
- Dois pontos: linha 77 (modo HNSW puro, ativo hoje) e linha 214 (modo BM25+HNSW, desativado).
- Constantes do scoreMap e take ajustadas proporcionalmente (50.0 → 15.0) para evitar
  scores negativos.
- Resultado esperado: 40+ chunks por busca → ~15 chunks, modelo foca melhor.

### Mudança C — Prompt do Auditor reforçado
- **Arquivo:** `data/network/MestreIAPromptsAuditor.kt`.
- Adicionada seção "REGRA CRÍTICA DE CITAÇÃO (LOTE 315)" com:
  - Lista explícita de comportamentos PROIBIDOS (citar página por inferência, números inventados, etc.).
  - Lista de comportamentos OBRIGATÓRIOS (declarar quando não achou, marcar conhecimento geral).
  - Exemplo concreto da alucinação detectada (pág. 174 / -5) como caso negativo.
  - Avisa o modelo que existe sistema externo verificando suas citações (aumenta cumprimento).

### Validação
- ✅ Compila (`./gradlew :app:compilePracegoDebugKotlin` BUILD SUCCESSFUL em 12s).
- ⏳ Validação funcional: usuário vai re-testar a bateria de 7 perguntas reais
  (registradas em `pergutas.txt`) e comparar respostas antes/depois.

### Rollback
- 1 commit único na `feature/mestre-ia-graphrag` — `git revert <hash>` desfaz tudo.
- Nenhuma mudança em testes, banco de dados, ou catálogos.

## Lote 316 — [2026-05-28] fix: Auditor — maxTokens 16k + regra de leitura cuidadosa + regra de tamanho

- **Hash:** 22ff256
- **Escopo:** 2 mudanças no código + 2 verificações (sem mexer):
  - **A. maxTokens 4096 → 16384** (`MestreIAUseCase.kt` linha 164):
    resolve corte de respostas longas observado em pergunta sobre "Ataque Súbito".
  - **B. temperature:** verificado, **já estava em 0.1** em `MestreIAClient.kt`
    linhas 349 e 373 — sem alteração necessária (modelo já está em modo determinístico).
  - **C. log cache_hit:** verificado, **já existe** em `MestreIAClient.kt` linhas 259-263
    (`MestreIA_Cache: Cache hit=X miss=Y (Z% do prompt em cache)`). Sem duplicação.
  - **D. Regra de tamanho:** nova seção no prompt do Auditor obrigando ~10k caracteres
    máximo, com instrução de "encerrar limpo" se exceder. Mira o problema do corte
    no meio de tabela/frase observado.
  - **E. Princípio de leitura cuidadosa:** nova seção no prompt obrigando o modelo a
    **comparar TODOS os chunks recebidos** antes de escolher uma manobra/regra, e
    preferir regras ESPECIALIZADAS quando aplicáveis. Mira o caso "Avançar e Atacar
    vs Ataque Súbito" onde o modelo escolheu a primeira opção e ignorou a melhor
    que estava no mesmo pacote de chunks. Princípio genérico, sem hardcode de páginas
    ou termos específicos (contraste com erro de IAs anteriores que blindavam casos
    específicos).

### Motivação
- Lote 315 detectou alucinação de páginas; Lote 316 ataca outro problema:
  modelo **escolher mal** entre chunks já recebidos.
- Pesquisa profunda da documentação DeepSeek (ver `.agent/skills/DEEPSEEK_DOCUMENTACAO.md`)
  confirmou: max_tokens default 4000 é fácil de estourar em respostas técnicas;
  temperature recomendada para tarefas determinísticas é baixa (já estava em 0.1).

### Validação
- ✅ Compila (`./gradlew :app:compilePracegoDebugKotlin` BUILD SUCCESSFUL em 15s).
- ⏳ Validação funcional: usuário re-testará perguntas (especialmente a do "Ataque Súbito"
  e perguntas longas que cortavam) para confirmar melhoria.

### Rollback
- 1 commit único — `git revert <hash>` desfaz tudo.
- Nenhuma mudança em testes, banco, catálogos.

## Lote 317 — [2026-05-29] feat: tools especializadas por livro + remoção do índice MB hardcoded

- **Hash:** ef51715
- **Motivação:** evidência do logcat mostrou que o modelo SEMPRE usava `livro="Módulo Básico"` mesmo quando a pergunta envolvia magia/armas de fogo/artes marciais. Causa: o prompt do Auditor tinha 50 linhas do índice oficial do MB hardcoded, viciando o modelo a pensar "tudo importante está no MB". Resultado: 619 chunks (52% do códex) dos outros 4 livros ficavam subutilizados, e modelo alucinava páginas do índice (ex: pág. 551 — citada sem chunk porque estava no índice do prompt).
- **Mudança A — Prompt:**
  - **Removidas** 50 linhas de índice hardcoded do Módulo Básico (linhas 98-149 antigas).
  - **Removida** seção "QUAL LIVRO USAR" — substituída por descriptions das tools especializadas.
  - Nova seção "FERRAMENTAS DISPONÍVEIS" descrevendo as 5 tools por domínio + regra genérica de escolha (ler pergunta inteira antes de escolher).
- **Mudança B — Tools especializadas (`MestreIATools.kt`):**
  - 4 novas constantes: `TOOL_REGRAS_MAGIA`, `TOOL_REGRAS_ARMAS_FOGO`, `TOOL_REGRAS_ARTES_MARCIAIS`, `TOOL_REGRAS_AQUATICO`.
  - Cada uma adicionada em ambos `getGeminiTools()` e `getOpenAITools()` com description detalhada do domínio.
  - `TOOL_MANUAL_DIRETO` mantida como fallback genérico, com description atualizada para "Use APENAS quando não cabe nas especializadas".
- **Mudança C — Execução (`MestreIAUseCase.kt`):**
  - Extraída lógica de execução de busca para método helper `executarBuscaCodex()` (privado, suspend).
  - 4 novos `case` no `when (toolCall.name)`: cada tool especializada chama o helper com `filtroLivro` fixo.
  - Log de tool agora inclui `[livro=X]` quando especificado.
- **Por quê tools em vez de regra no prompt:** modelo escolhe ferramenta pelo entendimento da pergunta inteira (não palavra-chave). Descriptions das tools auto-documentam quando usar. Não polui o prompt com regras IF-ELSE. Documentação oficial DeepSeek recomenda esse padrão (ver `.agent/skills/DEEPSEEK_DOCUMENTACAO.md`).
- **Validação:**
  - ✅ Compila (BUILD SUCCESSFUL em 7s).
  - ⏳ Funcional: usuário re-testará a pergunta "duas pistolas em corredor escuro" — esperado: modelo escolhe `consultar_regras_armas_fogo` em vez de `consultar_manual_direto` com `livro="MB"`.
- **Risco:** modelo pode escolher tool errada em casos ambíguos (ex: "magia que dispara projétil" — magia ou armas?). Mitigação: regra no prompt diz "prefira o domínio principal da ação".
- **Rollback:** `git revert <hash>` desfaz tudo. Sem mudanças em testes/banco/catálogos.

## Lote 318 — [2026-05-29] fix: remove hardcode dos exemplos nas descriptions das tools especializadas

- **Hash:** be43445
- **Motivação:** O usuário detectou que no Lote 317 eu blindei as descriptions das 4 tools especializadas com **listas hardcoded de exemplos** (ex: "Ataque Furacão, Joelhada, Mata-leão, Chave de Braço" na tool de Artes Marciais; "pistola, revólver, rifle, espingarda, metralhadora..." na de Armas de Fogo). Isso é exatamente o anti-padrão que o usuário identificou em sessões anteriores como causa raiz dos 7 dicionários hardcoded que corrompem o RAG: **listas finitas viram cola para casos específicos e quebram em casos novos**.
- **Mudança (8 descriptions reescritas):**
  - Removidos TODOS os exemplos nominais nas descriptions.
  - Substituídos por **descrição categorial** ("tema central da pergunta", "foco da pergunta").
  - Cada tool aplicada em ambos formatos: Gemini (`getGeminiTools`) e OpenAI (`getOpenAITools`).
- **Antes vs Depois (exemplo Artes Marciais):**
  - ❌ Antes: "técnicas marciais específicas (Ataque Furacão, Joelhada, Mata-leão, Golpe Fulminante, Chave de Braço), estilos marciais, combate desarmado, agarrar, derrubar, imobilizar, judô, karatê, boxe, esgrima..."
  - ✅ Depois: "técnicas corpo a corpo nomeadas, estilos marciais específicos, combate desarmado ou manobras avançadas além das básicas do Módulo Básico"
- **Princípio:** descrição por categoria conceitual cobre qualquer caso futuro (técnica nova, estilo novo). Lista hardcoded só cobre o que alguém pensou antes.
- **Validação:** Compila (BUILD SUCCESSFUL em 4s). Validação funcional via script Python `scripts/testar_tools_318.py` chamando API DeepSeek real com 8 perguntas (2 por tema).
- **Rollback:** `git revert <hash>` desfaz Lote 318. Lote 317 ainda funcional (só com descriptions blindadas).

## Lote 319 — [2026-05-29] feat: migra Gemini Live para tools especializadas (espelho do Auditor Texto)

- **Hash:** 4a1b689
- **Motivação:** Auditoria identificou que o **Gemini Live (voz)** ainda usava o `MestreIAPlanner` com 7 dicionários hardcoded e prompt com índice MB completo — exatamente os anti-padrões que removemos do Auditor Texto nos Lotes 317/318. Live ficava com tratamento pior que Texto, e modelo da voz preferia sempre Módulo Básico, ignorando 619 chunks (52% do códex) dos outros 4 livros.
- **Pesquisa prévia:** documentação oficial Gemini Live API (https://ai.google.dev/gemini-api/docs/live-api/tools) confirmou suporte a múltiplas tools especializadas, mas com particularidades vs DeepSeek:
  - Formato simplificado (sem wrapper `{"type":"function"}`)
  - `behavior: NON_BLOCKING` essencial para evitar bug `<ctrl46>` (Lote 306)
  - Sem cache automático (sessão WebSocket única)
  - Manual tool response handling (já implementado no projeto)
- **Mudanças (3 arquivos):**

### `GeminiLiveService.kt`
- **Substituída** a declaração da tool `consultarManual` por **5 tools especializadas**:
  - `consultarManual` (genérica, fallback para casos transversais)
  - `consultarRegrasMagia`
  - `consultarRegrasArmasFogo`
  - `consultarRegrasArtesMarciais`
  - `consultarRegrasAquatico`
- **Todas com `nonBlocking = true`** (previne bug `<ctrl46>`).
- **Descriptions categoriais** (sem hardcode de exemplos, lição do Lote 318).
- **Removidas** ~50 linhas de índice MB hardcoded do prompt + seção "QUAL LIVRO USAR".
- **Adicionados 4 labels visuais** novos no `when` da linha 768 (ex: "🔫 Consultando Gun Fu...").

### `GeminiLiveTools.kt`
- **Removido import** de `MestreIAPlanner` (não usado mais).
- **Adicionados 4 cases** no `when` da função `executar()`: cada uma chama `consultarManual(args, livroForcado="X")`.
- **Refatorada** função `consultarManual` para aceitar `livroForcado: String?`:
  - **Removido** uso do `MestreIAPlanner.planejarBusca` (7 dicionários hardcoded).
  - **Removido** loop de sub-queries temáticas do Planner.
  - Agora chama `graphEngine.buscarDiretoNoCodex(termos, [], filtroLivro=X)` direto, sem expansão de termos.
- Tool genérica `consultarManual` continua aceitando `args.livro` opcional para retrocompatibilidade.

### Por quê tools em vez de regra no prompt
- Modelo escolhe pela natureza da pergunta inteira, não palavra-chave isolada.
- Descriptions auto-documentam quando usar cada uma.
- Não polui o prompt do Live (importante: cada token na sessão WebSocket conta).
- Espelha exatamente o que funciona no Texto (87% acerto comprovado no Lote 318).

### Validação
- ✅ Compila (BUILD SUCCESSFUL em 6s — 2 warnings pré-existentes sobre `isSpeakerphoneOn deprecated`, não relacionados).
- ⏳ Funcional: **usuário precisa testar voz no celular real** (Python não testa Gemini Live). Cenários sugeridos:
  - Pergunta de magia → esperado: modelo usa `consultarRegrasMagia` (label "✨ Consultando o Livro de Magia...")
  - Pergunta de arma de fogo → esperado: `consultarRegrasArmasFogo` (label "🔫 Consultando Gun Fu...")
  - Pergunta combinada → esperado: modelo escolhe a especializada do domínio principal.

### Riscos
- 🟡 **Bug `<ctrl46>` poderia voltar** se alguma tool nova esquecesse `nonBlocking=true`. Mitigação: todas as 5 estão `nonBlocking=true`, validado linha por linha.
- 🟡 Modelo Gemini pode ser mais literal que DeepSeek nas descriptions. Se errar muito, refinar.
- 🟢 Planner ainda existe no código (não foi apagado), pode ser apagado em lote futuro de limpeza se confirmar que não há mais uso.

### Rollback
- 1 commit único — `git revert <hash>` desfaz tudo.
- `MestreIAPlanner.kt` continua intacto (deixar como "código morto" por enquanto, decisão consciente).
- Nenhuma mudança em catálogos, banco, testes ou outras superfícies.

## Lote 320 — [2026-05-29] fix: echo de áudio Live — AudioSource.MIC → VOICE_COMMUNICATION + NS + AGC

- **Hash:** 2834ba1
- **Bug observado:** Após testar voz no Xiaomi 23078PND5G, modelo "ouvia a si mesmo" — `inputTranscription` capturava fragmentos da saudação que o próprio modelo acabara de falar (ex: "Ah", ",", "Ja", "ck", "E", "go", ". Bom tê-lo aqui na mesa." — frase exata da saudação). Resultado: bug em loop, modelo respondendo à própria voz.
- **Auditoria do código (bloco a bloco) revelou 3 anti-padrões combinados em `GeminiLiveService.iniciarCaptura()`:**
  1. `MediaRecorder.AudioSource.MIC` — fonte CRUA, sem pré-processamento do Android.
  2. `AcousticEchoCanceler` software — fraco, depende do fabricante, sem referência do speaker.
  3. `AudioManager.MODE_NORMAL` — sistema não sabe que app está em comunicação bidirecional.
- **Documentação oficial (developer.android.com):**
  > *"VOICE_COMMUNICATION is similar to MIC but adds acoustic echo cancellation so audio from the loudspeaker is not heard by the microphone. This is intended for use during Voice over IP (VoIP) and video calls."*
  > *"Android implementations should provide an acoustic echo canceler (AEC) on the capture path when capturing with VOICE_COMMUNICATION."*
- **Fix aplicado em 1 ponto cirúrgico (`iniciarCaptura()` linhas 1080-1130):**
  - **`MediaRecorder.AudioSource.MIC` → `MediaRecorder.AudioSource.VOICE_COMMUNICATION`**: ativa AEC nativo do sistema (mais potente que o software manual).
  - **Adicionado `NoiseSuppressor`**: reduz ruído ambiente capturado pelo mic.
  - **Adicionado `AutomaticGainControl`**: normaliza volume do mic (voz baixa fica audível).
  - **Mantido `AcousticEchoCanceler` como 2ª camada** (caso VOICE_COMMUNICATION não ative AEC em algum dispositivo — sem custo).
  - **Mantido `MODE_NORMAL`** (MODE_IN_COMMUNICATION historicamente suprimia voz do usuário neste app).
  - Logs atualizados para refletir cada efeito ativado.
- **Por que NÃO foi causado pelos Lotes anteriores:** auditoria do `git diff 703cf38..HEAD` confirmou que nenhuma linha de áudio foi tocada entre Lote 312 (`703cf38`, 27/05) e Lote 320. O bug existia desde o Lote 312 — só não tinha sido testado novamente até hoje.
- **Validação:**
  - ✅ Compila (BUILD SUCCESSFUL em 6s).
  - ⏳ Funcional: usuário precisa testar voz no celular real (Xiaomi via WiFi adb).
- **Risco:** Doc oficial alerta que alguns dispositivos têm implementação ruim de VOICE_COMMUNICATION também. Se Xiaomi 23078PND5G estiver entre eles, avaliar reverter para MIC + bloqueio de software (`if (modeloFalando) continue`).
- **Rollback:** `git revert <hash>` desfaz tudo.

## Lote 321 — [2026-05-29] feat: prompt Live com regras categoriais (busca exaustiva + paralelismo)

- **Hash:** e05e777
- **Motivação:** Análise do logcat revelou 2 limitações comportamentais do Gemini Live:
  1. Modelo fez 6 tool calls 100% sequenciais (nunca paralelas) em sessão de voz.
  2. Modelo esgotou 1 livro de cada vez antes de pular pro próximo, parando antes de explorar todos os domínios relevantes da pergunta.
- **Diagnóstico:** NÃO é trava sistêmica — código (`GeminiLiveService.kt:785-787`) tem loop `for i in 0 until calls.length()` pronto pra processar N tool calls paralelas. Live também NÃO tem `MAX_TOOL_CALLS` (diferente do Auditor Texto). O comportamento sequencial é do modelo (doc oficial: *"function calling executes sequentially by default"*) + ausência de instruções explícitas no prompt.
- **Princípio guia (lição do próprio modelo):**
  > *"Exemplos detalhados podem criar vieses ou direcionar fluxo de forma restritiva. O protocolo sistêmico valoriza a clareza nas regras de priorização e na hierarquia das ferramentas, permitindo análise dinâmica e adaptável."*
- **Mudança em `GeminiLiveService.kt` prompt (sem hardcode de exemplos):**
  - **Adicionado "PROTOCOLO DE BUSCA EXAUSTIVA"**: para perguntas que combinam múltiplos domínios (ação + ambiente, regra base + situação especial), consultar TODOS os domínios relevantes antes de responder. Hierarquia categorial: (1) ação principal, (2) contexto/ambiente, (3) situações especiais aplicáveis.
  - **Adicionado "PROTOCOLO DE PARALELISMO"**: incentiva chamar 2+ tools no mesmo turno em vez de aguardar resultado entre elas.
  - **Removido exemplo "magia de fogo subaquática"** da REGRA DE ESCOLHA (coerência com o princípio do modelo).
- **Por que sem exemplos:** lição confirmada do Lote 318 — listas finitas viram cola; categorias generalizam.
- **Validação:** ✅ Compila (BUILD SUCCESSFUL em 5s). ⏳ Funcional: usuário re-testará voz com pergunta que combine múltiplos domínios.
- **Risco:** Gemini Live pode ignorar regra de paralelismo (default é sequential). Mitigação: se ignorar, perdemos pouco — comportamento sequential já era o atual.
- **Rollback:** `git revert <hash>` desfaz.

## Lote 322 — [2026-05-29] fix: filtro de livro vira híbrido (rígido + fallback complementar)

- **Hash:** 013ef45
- **Bug descoberto na análise do log da sessão Live (16:07):**
  - Modelo chamou `consultarRegrasArmasFogo` para "pólvora molhada".
  - HNSW retornou top-5 globais: pág.408 MB (Mau Funcionamento), pág.7 Pyramid, pág.280 MB, pág.262 Magia, pág.411 MB.
  - Filtro `livro='Gun Fu'` REJEITOU TODOS OS 5 (nenhum era Gun Fu).
  - Resultado: **só 1 chunk obscuro de Gun Fu sobrou (1.108 chars)** — material praticamente inútil.
  - Mesma coisa para `consultarRegrasAquatico`: 2 chunks de 1.755 chars.
  - Bug arquitetural do Lote 317 (tools especializadas) descoberto só agora — filtro rígido vazia o resultado quando a regra real está em outro livro.
- **Análise das opções (pensamento exaustivo):**
  - **A. Boost (não filtro):** complica scoring, perde especialização → descartada.
  - **B. topK maior + filtrar:** simples mas pode esgotar livros pequenos → descartada.
  - **C. Busca dupla:** 2x latência → descartada.
  - **D. Filtro rígido + fallback complementar (ESCOLHIDA):** rápida, robusta, preserva intenção.
- **Mudança em `MestreIAGraphEngine.buscarDiretoNoCodex()` linhas 75-119:**
  - **topK aumentado** de 15→30 quando há `sourceIdFiltro` (margem pra filtro).
  - **Filtro rígido** mantido (chunks do livro escolhido vêm primeiro).
  - **Fallback complementar:** se filtro rígido deixou <5 chunks, complementa com até 5 chunks globais (de outros livros) que estavam no top-30 do HNSW e não foram incluídos.
  - Log adicional: `"Filtro X deixou N chunks — complementando com M de outros livros"`.
- **Comportamento esperado:**
  - Tool `consultarRegrasArmasFogo` para "pólvora molhada" → HNSW top-30 → filtra Gun Fu (sobram 1-2) → complementa com top 5 globais → modelo recebe **6-7 chunks: 1-2 do Gun Fu + 5 dos mais relevantes globais (provável: pág.408 MB, pág.7 Pyramid)**.
  - Tool `consultarRegrasMagia` para "bola de fogo" → filtro deixa 10+ chunks de Magia → não complementa (já rico).
- **Validação:**
  - ✅ Compila (BUILD SUCCESSFUL em 7s).
  - ⏳ Funcional: usuário re-testará voz e verificará se chunks complementares aparecem nos logs.
- **Risco:** mínimo — mudança contida em 1 função, fallback só ativa quando filtro falha. Reversível.
- **Rollback:** `git revert <hash>` desfaz.

## Lote 323 — [2026-05-29] fix: prompt Live anti-enrolamento (mantém saudação cerimonial, corta meta-fala nas respostas)

- **Hash:** `415a661`
- **Bug observado:** Modelo de voz "enche linguiça". Análise do log mostrou que de 113 fragmentos de resposta, ~38% (43 fragmentos) eram puro enrolamento (saudação cerimonial pós-pergunta, meta-fala anunciando o que vai fazer, conclusão genérica sobre "campanha"). Só 62% era resposta técnica útil.
- **Causa raiz identificada (auditoria do prompt linhas 215-218 + 509-525):**
  - Linha 218: `"Personalidade: sábio, justo, levemente dramático"` — aplicava o "dramático" a TODAS as respostas, não só à saudação.
  - Linhas 513/518 das vozes Sadaltager/Gacrux: `"Demonstre autoridade desde a primeira frase"` / `"Tom sóbrio e respeitoso"` — induziam respostas elaboradas e cerimoniais.
  - Linha 217: `"Respostas curtas e diretas"` existia, mas era contradita pelas regras dramáticas acima.
- **Decisão de design (usuário aprovou):**
  - **Manter** saudação cerimonial inicial (imersão de mesa de RPG, legítimo).
  - **Eliminar** enrolamento APÓS a pergunta concreta.
  - **Princípios categoriais, ZERO exemplos** (lição do Lote 318 — exemplos viram cola, categorias generalizam).
- **Mudança A — Personalidade no prompt principal:**
  - Linha 218 reescrita: o "dramático" agora é EXPLICITAMENTE restrito à saudação inicial. Após pergunta concreta, tom técnico/direto/factual.
- **Mudança B — As 3 vozes (Sadaltager, Gacrux, Charon):**
  - Cada `instrucaoSaudacao` reescrita para deixar EXPLÍCITO que se aplica APENAS à saudação inicial.
  - As 3 agora delegam o estilo de resposta ao prompt principal.
  - Diferenciação entre vozes preservada via timbre/cadência (não estrutura).
- **Mudança C — Nova seção "PROIBIDO ENROLAR NAS RESPOSTAS":**
  - Categoria 1: META-FALA (anunciar o que vai fazer antes de fazer).
  - Categoria 2: REPETIÇÃO CERIMONIAL (nome do personagem repetido, saudação no meio).
  - Categoria 3: CONCLUSÕES GENÉRICAS (encerrar com frases sem informação).
  - Regra final: "A primeira frase da resposta deve conter informação técnica direta da regra."
  - **Zero exemplos de frases específicas** — só descrição categorial do anti-padrão.
- **Validação:** ✅ Compila (BUILD SUCCESSFUL em 11s). ⏳ Funcional: usuário re-testará voz e medirá razão "resposta útil / total".
- **Risco:** baixo — mudanças no prompt apenas. Reversíveis via revert.
- **Rollback:** `git revert <hash>` desfaz.

----------------------------------------------------------------------------------------------------------------------------------------------------

## Lote 324 — [2026-05-29] feat: Live RAG (fusão 5 tools → 1 com array de livros) + VAD 1500ms

- **Hash:** `a711ec8`
- **Sintomas observados no log do Lote 323:**
  1. Modelo usou **3 chamadas sequenciais** de `consultarManual` (genérica), nenhuma das 4 especializadas. Pergunta cruzava domínios (escalada + escudo + aparar/bloquear), mas o modelo não tinha como expressar essa combinação numa só chamada.
  2. UI fragmentou a fala do usuário em **4 balões separados** ("Fala mestre.", "Queria tirar uma dúvida.", "Eu estou escalando...", "Eu posso aparar...") porque o VAD do Gemini Live fecha o turno a cada ~800ms de silêncio (cada pausa pra respirar).
  3. **Erro de regra** identificado pelo usuário: modelo confundiu aparar com bloquear (não se apara com escudo, se bloqueia). Causa-raiz na granularidade das tools: queries das 3 chamadas não cobriram "bloquear com escudo".
- **Causa raiz (busca):** 5 tools especializadas (1 genérica + 4 por livro) cobriam só 5 dos 32 cenários combinatórios possíveis. Quando o tópico tocava 2+ livros, modelo ou usava só a genérica (perdia foco) ou fazia chamadas sequenciais (latência + queries muito estreitas cada uma).
- **Causa raiz (UI):** `realtimeInputConfig` estava desativado no setup da sessão (comentário "diagnóstico — modelo 2.5 preview pode não suportar"). Sem essa config, VAD usa default de ~800ms.

### Mudança A — Fusão das 5 tools em 1 (multi-livro via array)
- `MestreIAGraphEngine.buscarDiretoNoCodex`: novo parâmetro `filtroLivros: List<String>?` com precedência sobre o legado `filtroLivro: String?` (Auditor Texto não muda — backward compat). Mapeamento para `source_id` agora retorna `Set<String>?`. Filtro de chunks via `it.source_id in sourceIdsFiltro`.
- `GeminiLiveTools.consultarManual`: assinatura `(args)` apenas. Lê `args.livros: array<string>` (aceita também `args.livro: string` legado). Passa lista para `filtroLivros`.
- `GeminiLiveTools.executar`: removidos os 4 cases `consultarRegrasMagia/ArmasFogo/ArtesMarciais/Aquatico`. Restou apenas `consultarManual`.
- `GeminiLiveService.criarSetupSessao`: removidas as 4 declarações de tools especializadas. `consultarManual` agora declara `livros: array<string>` com `items.enum` dos 5 livros. Description categorial (sem exemplos) instruindo a passar TODOS os livros relevantes num único array.
- `GeminiLiveService` (helper novo) `labelConsultarManual(args)`: gera label de loading dinâmico tipo "📖 Consultando 📕 Módulo Básico + 🥋 Artes Marciais...". Substitui o `when` antigo com 5 strings fixas.
- Fallback complementar do Lote 322 (top-5 globais quando filtro deixa <5 chunks) **preservado** — agora aplica também para multi-livro.

### Mudança B — Prompt principal
- Linha de tools: substituídas as 5 linhas por **uma só** descrevendo `consultarManual(termos, livros?)` com regra de "informar TODOS os livros relevantes no MESMO array".
- "PROTOCOLO DE PARALELISMO" reescrito: paralelismo deixa de ser "múltiplas chamadas no mesmo turno" e passa a ser "uma única chamada com array multi-livro".
- "PROTOCOLO DE BUSCA — DÚVIDAS DE REGRAS": ajustado pra mencionar só `consultarManual` (com array 'livros').

### Mudança C — VAD silenceDurationMs = 1500
- `realtimeInputConfig.automaticActivityDetection.silenceDurationMs = 1500` agora reativado.
- Sobe de ~800ms (default) pra 1500ms → permite pausa pra respirar entre frases sem fechar turno do usuário.
- Efeito esperado: 1 balão por turno do usuário em vez de 4-5.
- Trade-off: modelo demora ~700ms a mais pra começar a responder após usuário parar de falar (aceitável; pode até ajudar contra a sensação de "fala atropelando").

### Princípios mantidos
- **Zero exemplos hardcoded nas descriptions** (lição do Lote 318 — exemplos viram cola, categorias generalizam).
- **Auditor Texto não foi tocado** (decisão do usuário neste lote).
- **Compatibilidade**: `filtroLivro: String?` legado preservado, `args.livro: string` legado preservado.

### Validação
- ✅ Compila (BUILD SUCCESSFUL em 25s).
- ⏳ Funcional: usuário re-testará voz. Métricas a observar:
  - Quantidade de balões por turno do usuário (alvo: 1).
  - Quantidade de tool calls por resposta (alvo: ≤2; ideal 1 com array).
  - Acerto da regra de bloquear/aparar com escudo durante escalada (caso do log que falhou no Lote 323).

### Risco
- Médio. `realtimeInputConfig` foi desativado historicamente por suspeita de incompatibilidade com modelo 2.5 preview. Se a sessão não abrir, basta remover o bloco `realtimeInputConfig` e reverter pro default. As mudanças de tool são puramente aditivas no backend e não tocam o Auditor.

### Rollback
- `git revert <hash>` desfaz tudo. Como há mudança em 3 arquivos (GraphEngine, GeminiLiveTools, GeminiLiveService), revert único é seguro.

----------------------------------------------------------------------------------------------------------------------------------------------------

## Lote 325 — [2026-05-30] feat: NOVO motor de busca do Auditor (grep + leitura dirigida, substitui embedding)

- **Hash:** `7b3f9c2`
- **Escopo:** APENAS Auditor Texto (modo dúvida/conversa). Voz (Live) e Forjador NÃO tocados.
- **Diagnóstico (origem do problema, confirmado com o usuário e com dados):**
  - 3 sessões com a MESMA pergunta → 3 respostas diferentes (escalada+escudo+aparar). Causa: variância na recuperação.
  - Motor era **HNSW puro** (flag de teste `MODO_HNSW_PURO=true` em `FichaIADelegate.kt:55`, ligada desde testes antigos). 1 embedding por PÁGINA inteira (~5700 chars, mediana medida no chunks.jsonl) = vetor "borrado": a busca casava pelo SUBSTANTIVO dominante, ignorando a INTENÇÃO (verbo).
  - O sistema ainda CARIMBAVA o chunk borrado como `[★★★] priorize` — mandava o modelo confiar no resultado errado.
  - Resultado: modelo "adaptava" (confabulava) regras a partir de páginas vagamente relacionadas, com eloquência > verdade. Ex. real: confundiu Escalada (perícia mundana, MB) com "Escalada de Lagarto" (cinematográfica, Artes Marciais) e cruzou regra inexistente de aparar.
  - BM25+HNSW também era ruim → trocar algoritmo de ranking não resolvia. Nenhum dos dois JULGA relevância; só ordenam por número-proxy.
  - Determinismo por tópico (TopicIndex) também não serve: usuário mostrou que "traga a tabela X" e "posso fazer Y contra X" caíam nos mesmos fragmentos — carimbaria o erro.
- **Solução (pedida pelo usuário): buscar como o Claude busca neste projeto** — palavra-chave (grep) + estreitamento por AND + leitura dirigida + julgamento por LEITURA, em loop. Não usar embedding.

### Novas ferramentas (substituem as 5 tools de embedding NO AUDITOR)
- `localizar_no_codex(termos, livros?)` — "página de resultados". FTS4 **AND** (cada palavra a mais estreita, igual ao Google). Retorna lista COMPACTA: livro|página|trecho curto. NÃO texto completo. Fallback OR rotulado "aproximado" quando AND dá zero.
- `ler_pagina(livro, pagina, pagina_final?)` — abre o TEXTO COMPLETO da página/intervalo escolhido (máx 4 págs). Adiciona os chunks reais ao contexto (Verificador de Citações valida contra eles).
- Loop ensinado no prompt: localizar → julgar pela leitura (não pela ordem) → ler → (seguir referências) → responder citando [Livro, Pág].

### Anti-confabulação (cerne da queixa) — no prompt do Auditor
- Usar SOMENTE o que leu com ler_pagina.
- DESCARTAR página de tema diferente mesmo que compartilhe palavra no título (homônimo).
- NÃO acrescentar penalidades/condições que a pergunta não pediu nem a página declarou.
- SEPARAR "regra oficial" de "interpretação"; melhor dizer "não encontrei" do que maquiar.
- Zero exemplos hardcoded no prompt (lição do Lote 318).

### Arquivos
- `data/MestreIARepository.kt`: + `localizarNoCodex()` (FTS4 AND + snippet + fallback OR), + `lerPaginas()` (intervalo, livro→source_id), + helpers `tokenizarTermos`/`construirTrecho`/`mapearLivroParaSourceId`. Data classes `LocalizarHit`/`LocalizarResultado`.
- `data/DataRepository.kt`: expõe `localizarNoCodex`/`lerPaginas`.
- `data/network/MestreIATools.kt`: + constantes `TOOL_LOCALIZAR`/`TOOL_LER`, + `getAuditorToolsOpenAI()` (localizar+ler+inspect+nexus). `getOpenAITools`/`getGeminiTools` INTACTAS (Forjador).
- `data/network/MestreIAClient.kt`: `gerarJsonOpenRouter` (DeepSeek) e `gerarJsonGoogleNative` (Gemini) — branch do Auditor chama `getAuditorToolsOpenAI()`/`getAuditorToolsGemini()`; Forjador (geracao/analise) inalterado. [corrigido no fix ac26f1d]
- `domain/MestreIAUseCase.kt`: + cases dispatch `TOOL_LOCALIZAR`/`TOOL_LER`, + `executarLocalizar()`/`executarLer()`. `MAX_TOOL_CALLS` 5→8 (loop localizar+ler precisa de mais idas). Cases antigos MANTIDOS (não quebra nada).
- `data/network/MestreIAPromptsAuditor.kt`: SYSTEM_PROMPT_BASE reescrito (loop + anti-confabulação).

### Embeddings: MANTIDOS DORMENTES (decisão do usuário)
- `chunks.jsonl` continua com os 1197 embeddings (48MB). HNSW segue disponível pra Voz/Forjador.
- O Auditor simplesmente não chama mais busca semântica.
- Existe `chunks.jsonl.bak` (mesmo texto, MESMAS 1197 páginas, SEM embeddings, 6.5MB). Import tolera ausência de embedding (`if (obj.has("embedding"))` em FichaDatabase). **Plano futuro:** se os testes provarem que keyword cobre tudo, trocar pro .bak e cortar 48MB.

### Validação
- ⚠️ ERRATA: o 1º commit (`f20172c`) foi feito com o build QUEBRADO e a fiação incompleta
  (tipo nullable em localizarNoCodex; `getAuditorTools*` criadas mas nunca chamadas; prompt
  real `MestreIAPromptsAuditor.PROMPT` não havia sido trocado — eu editei a constante errada).
  Corrigido no commit seguinte: tipos nullable, fiação de `getAuditorToolsOpenAI`/`getAuditorToolsGemini`
  em `gerarJsonOpenRouter`/`gerarJsonGoogleNative`, e reescrita do PROMPT real.
- ✅ Compila após a correção (BUILD SUCCESSFUL em 1m02s).
- ⏳ Funcional: usuário testará no chat (Auditor). Métricas:
  - Consistência: mesma pergunta repetida → mesma página lida → resposta estável.
  - Acerto: Escalada (perícia mundana) ≠ Escalada de Lagarto; bloquear (não aparar) com escudo.
  - Honestidade: declara "não encontrei" em vez de confabular.

### Risco
- Médio. Mudança de arquitetura de busca, mas isolada no Auditor (Forjador/Voz intactos; cases antigos preservados). Palavra-chave pode errar sinônimo — mitigado por: (a) modelo conhece termos de GURPS e tenta sinônimos no loop; (b) fallback OR no localizar; (c) embedding ainda existe se precisarmos religar.

### Rollback
- `git revert <hash>` desfaz. Auditor volta às 5 tools de embedding.

----------------------------------------------------------------------------------------------------------------------------------------------------

## Lote 326 — [2026-05-30] chore: log de avaliação do novo motor de busca (Auditor)

- **Hash:** `bdc37a2`
- **Motivo:** antes de testar o Lote 325, fechar 3 cegos do log que impediam AVALIAR a qualidade da busca (não só ver que rodou).
- **Cego #1 — dispatch logava arg errado:** a linha de dispatch lia `args.query` (vazio nas tools novas, que usam `termos`/`livros`/`pagina`). Agora loga os args REAIS por tipo de tool: `TOOL[0]: [localizar_no_codex] termos="..." livros=[...]` / `TOOL[1]: [ler_pagina] livro="..." pag=N`.
- **Cego #2 — LOCALIZAR não mostrava QUAIS páginas:** logava só "N páginas". Agora: `LOCALIZAR [AND]: N páginas (retornando M) → [modulo_b:354, artes_ma:62, ...]`. Permite distinguir "página certa nem apareceu" (falha da busca) de "apareceu e o modelo ignorou" (falha do modelo).
- **Cego #3 — LER não mostrava o texto lido:** logava só "N chunks, X chars". Agora: `LER[i] PREVIEW: "..."` (300 chars). Permite comparar o que o modelo LEU com o que AFIRMOU → audita confabulação.
- **Arquivos:** `domain/MestreIAUseCase.kt` (dispatch consciente de tipo + preview de LER), `data/MestreIARepository.kt` (lista de páginas no LOCALIZAR).
- **Como avaliar (filtro logcat tag `MestreIA_RAG`):**
  - **Consistência:** repita a MESMA pergunta 2-3x. Compare LOCALIZAR (mesmos termos? mesmas páginas?) e LER (mesmas páginas?). Idealmente idênticas.
  - **Causa-raiz de erro de regra:** a página correta apareceu no `→ [...]` do LOCALIZAR? Se NÃO = busca falhou (ajustar termos/sinônimos). Se SIM mas não foi lida = modelo escolheu mal (ajustar prompt).
  - **Confabulação:** compare `LER PREVIEW` com a resposta. Afirmou algo fora do texto lido? `alucinou=` na linha RESPOSTA OK também sinaliza.
  - **Esforço:** `toolsFeitas=N` na RESPOSTA OK (loop localizar+ler gasta 2+ por conceito; teto 8).
- **Validação:** ✅ Compila (BUILD SUCCESSFUL). Sem impacto funcional (só `Log.i`).
- **Risco:** mínimo.
- **Rollback:** `git revert bdc37a2`.

----------------------------------------------------------------------------------------------------------------------------------------------------

## Lote 327 — [2026-05-31] feat: autonomia do Forjador (catálogo rico) + ranking/anti-confabulação do Auditor

- **Hash:** `9bb4172`
- **Resumo:** commit único juntando o trabalho da sessão (decisão do usuário). Duas frentes: FORJADOR (plano completo A+F, C, B, D, E) e AUDITOR (ranking BM25 + trava anti-confabulação). 11 arquivos `.kt`.

### FORJADOR (plano `.agent/skills/PLANO_MELHORIAS_FORJADOR.md`)
Princípio (herdado do Auditor): o Forjador LÊ o número que a ficha já calcula — nunca recalcula.
- **A+F:** `forjador_ler_ficha(pontos)` e `validarBudget` usam `Personagem.pontosGastos`/`pontosRestantes` (fonte de verdade completa). Removida `calcularPontosGastos` (Executor) e a estimativa `nivel*2` de perícia — 2 bugs que faziam o Forjador relatar pontos errados.
- **C (read-back numérico):** `forjador_ler_ficha(secao=derivados)` → Esquiva/Apara/Bloqueio, dano GdP/GeB, PV/PF/Vontade/Percepção, Velocidade/Desloc, carga, Aptidão Mágica. Read-back automático pós-edição inclui `derivados`.
- **B (equipamento de catálogo):** `buscar_catalogo` aceita `arma`/`armadura`/`escudo` com stats REAIS; `editar_ficha(adicionar, equipamentos, <id>)` resolve do catálogo (dano por ST automático + grupo p/ Mestre de Armas).
- **D (veredito técnica):** `buscar_catalogo(tecnica)` dá ✓PODE/⚠FALTA via `tecnicaAtendePreRequisito`.
- **E (secundários):** `editar_ficha(alterar, atributos, pv|vontade|percepcao|deslocamento|velocidade)`.

### AUDITOR
- **`rankearPorBM25`** em `MestreIARepository`: `localizar` ordena por relevância antes do `take` — corrige o fallback OR que devolvia "500 páginas" em ordem de nº de página.
- **Trava anti-confabulação** em `MestreIAUseCase` (`leuAlgumaPagina`) + "REGRA DE OURO do loop" no prompt (não responder sem ler).
- Comentários de **código LEGADO/MORTO** em `GraphEngine`/`Planner`/`SemanticEngine`/`VectorEngine` (ver `ARQUITETURA_MESTRE_IA.md` §5).

### Validação
- ✅ Compila (BUILD SUCCESSFUL). `GeminiLiveService.kt` (logs do Lote 324) deixado FORA a pedido do usuário.
- ⏳ Funcional: testar criar/editar ficha (pontos/stats devem bater com a tela) e Auditor.

### Pendência (descoberta no teste do Forjador) → próximo: Lote G
- `forjador_buscar_catalogo` ainda **não devolve `descricao`/`pagina`** — por isso o modelo cita fonte de memória (citou "B43"; a página real é 82). Os JSONs TÊM os campos (vant/desv/magia/técnica; perícia via `pericias_v2_rules_map.json` 332/332). Lote G = expor descrição+página (limpas de mojibake) na busca.

### Rollback
- `git revert 9bb4172`.

----------------------------------------------------------------------------------------------------------------------------------------------------


----------------------------------------------------------------------------------------------------------------------------------------------------

## Lote 329 — [2026-06-01] feat: Forjador cria ficha INCREMENTAL (sem botão) + cadeia de magia automática + correções pós-teste

- **Hash:** `d7db852`
- **Resumo:** o modo CRIAR deixou de depender do JSON final + botão INTEGRAR. O Forjador
  agora MONTA a ficha ao vivo via `forjador_editar_ficha` (igual ao modo análise), bloco a
  bloco na ordem dos 9 pilares. Se a conexão cai no meio, o que já foi aplicado permanece.
  Inclui Lote G (descrição+página no catálogo) e 9 Pilares (prompt criador + consultor),
  que vinham desta mesma sessão sem commit.

### B-COMPLETO (criação incremental)
- **Início da criação:** se há ficha não-vazia → `viewModel.autoSaveIA()` + `novaFicha()`
  (salva a atual, cria do zero). Só na 1ª config (não a cada fallback).
- **Síntese final:** virou `[FECHAMENTO]` (mensagem ao jogador), NÃO gera mais JSON.
- **Prompt criador (`MestreIAPromptsForjador.PROMPT`):** nova seção "MODO INCREMENTAL";
  removida a exigência de JSON final + GABARITO (GOLD_TEMPLATE virou código morto).
- **`FichaIADelegate`:** modo `geracao` NÃO cria `fichaGeradaPendente` → some o botão
  INTEGRAR na criação. Modo `analise` (Consultor) intacto (mantém sugerir→INTEGRAR).
- **Teto de pesquisa 4→12** + aviso de orçamento por rodada (anti-loop de pesquisa).
- **9 PILARES** nos DOIS prompts (criador = método de construção; consultor = estrutura
  de análise). Sem exemplos hardcoded (princípio anti-viés do Lote 318).

### Correções pós-teste (a partir de logs + comparação com fichas humanas)
- **BUG aparência:** `editarFicha` não aceitava `aparencia`/`notas` → texto se perdia. Corrigido.
- **BUG Riqueza invertida:** id existe em vantagens E desvantagens (escala única Falido↔Rico);
  o executor sempre preferia a vantagem (+10). Corrigido p/ respeitar a SEÇÃO pedida +
  retornar o custo real aplicado.
- **custoEscolhido:** `buscar_catalogo` mostra opções de escala (ESCOLHA/POR_NIVEL/VARIAVEL)
  e o prompt ensina a passar `custo=N`/`nivel=N`. Antes ignorava (tudo nível 1) → fichas rasas.
- **BUG encerramento prematuro:** `totalItens()` (detector de progresso) só contava listas;
  iterações de ATRIBUTOS/nome eram lidas como "estagnação" e o loop morria em 2 iterações.
  Agora usa fingerprint do estado (`pontosGastos` + itens + textos) e compara `!=` (desvantagem
  baixa pontos = progresso).
- **BUG não-fecha:** se o modelo parava de chamar tools sem fechar (resposta curta/interna
  "Dados coletados com sucesso"), o loop encerrava com esse lixo. Agora força UMA rodada de
  FECHAMENTO (tools off) p/ escrever a mensagem real ao jogador.
- **BUG salvamento-lixo (sistema):** `autoSaveIA` usava `IA_<nome>_<timestamp>` com timestamp
  novo a cada edição → ~20 arquivos-lixo por ficha. Agora fixa `nomeSessaoIA` na 1ª chamada e
  SOBRESCREVE o mesmo arquivo (zera em `novaFicha()`).
- **Log do budget** no início da criação.

### CADEIA DE MAGIA AUTOMÁTICA (sistemática, sem viés)
- `editarFicha` (secao=magias): ao pedir uma magia-alvo com pré-requisitos faltando (sem
  `forcar`), o SISTEMA adiciona a TRILHA INTEIRA na ordem (via `nexusAdapter.calcular(...)
  .trilhaOtimaIds`, a mesma do GPS) + o alvo, numa só chamada. Antes BLOQUEAVA e o modelo
  tateava 1 magia/iteração (Raspha gastou 5 iterações p/ 1 magia).
- **Medição real (teste JUnit rodado, depois removido):** fogo 1-3 magias, Encantar 10,
  Desejo 16 — todos <110ms. Decisão: SEM teto (cadeia longa é regra do GURPS), MAS o retorno
  avisa nº de magias + pontos consumidos + total da ficha (fonte de verdade `pontosGastos`).
- **GPS + descrição da tool** avisam que basta pedir o alvo (não tatear pré-req). Categórico,
  sem exemplo de magia → zero viés. Modelo pode pedir N alvos numa rodada (cada um resolve a cadeia).

### Validação
- ✅ Compila (BUILD SUCCESSFUL) em todas as etapas.
- ✅ Testes reais: criação do Kael, Raspha, Rapha (logs analisados) + teste JUnit de trilha.
- ⏳ Funcional pendente: reconfirmar criação completa (aparência grava, Riqueza no sinal certo,
  cadeia de magia entra inteira, 1 só arquivo salvo por ficha, fechamento honesto sem confabular).

### PENDÊNCIAS conhecidas (lotes futuros — ver PLANO_MELHORIAS_FORJADOR.md §8)
- **Confabulação no fechamento:** o fechamento forçado (tools off) pode o modelo DESCREVER
  magias/vantagens que NÃO aplicou (observado no Rapha: disse "150/150, 6 magias", ficha tinha
  "111/150, 1 magia"). NÃO corrigido ainda — é o mais perigoso (engana o usuário). Próximo alvo.
- **Mojibake:** falso positivo (artefato de transferência; no app/editor o texto está correto).
- **Arquétipos hardcoded** (LADRÃO/GUERREIRO/MAGO no PROMPT) citam custo e contradizem regra
  "app calcula custo" — candidatos a remover (usuário fará teste A/B antes).

### Rollback
- `git revert d7db852`.

----------------------------------------------------------------------------------------------------------------------------------------------------

## Lote 330 — [2026-06-01] fix: magias de escola única e elementais (sub-escola por entradas no catálogo)

- **Hash:** `a0235aa`
- **Resumo:** corrige um IMPASSE de UI que impedia adicionar certas magias. O `MagicEngine`
  exigia "especialização" para magias que a UI não dava campo para preencher → clicava
  Adicionar e a magia nunca entrava (validação barrava em silêncio). Regra GURPS: magia NÃO
  tem especialização livre (isso é de PERÍCIA); o que parecia especialização é SUB-ESCOLA
  (animais: Terra/Ar/Mar; elementais: Ar/Fogo/Terra/Água), resolvida por ENTRADAS SEPARADAS
  no catálogo — não por campo de texto.

### MagicEngine.kt
- `validarEspecializacaoObrigatoria`: lista de "exige especialização" ESVAZIADA. Saíram
  cavalgar, passageiro_interno, golem, adivinhacao, controle_de_hibrido (escola única) e os
  3 elementais. A sub-escola de Animais continua tratada à parte (`exigeSubEscolaAnimais`).
- `validarRegrasEspeciaisMagia`: removidos os 3 blocos hardcoded dos elementais (validavam por
  NOME genérico "Convocar/Controle de Elemental", conflitando com as novas entradas por elemento).
  Pré-requisito agora vem do TEXTO do JSON, validado pelo motor Nexus (igual às demais magias).
- `permiteMultiplasInstanciasMagia`/`PorEscola`: removidos os elementais antigos (agora são ids
  distintos por elemento). Mantido `anular_possessao`.

### DataRepository.kt
- `preRequisitosOverridePorMagiaId`: removidas as 3 linhas genéricas dos elementais (texto
  "escola apropriada" que o parser não resolvia). As novas entradas leem o preReq do JSON.

### Catálogo (magias2versao.json) — usuário criou as entradas; correção pontual da IA
- Usuário adicionou entradas por sub-escola: Cavalgar (Terra/Ar/Mar) e 12 elementais
  (convocar/controle/criar × Ar/Fogo/Terra/Água), cada uma com escola e pré-requisito próprios.
- IA corrigiu as 4 entradas de Convocar Elemental: preReq dizia "8 magicas da escola apropriada"
  (texto vago que o parser nunca resolve → magia ficaria travada) → trocado para o formato
  canônico "8 magicas de <Elemento>" (mesmo padrão de 60+ magias que já funcionam).
- Total: 839 → 853 magias (sem perda; validado).

### Validação
- ✅ Compila (BUILD SUCCESSFUL).
- ✅ Funcional confirmado no device: Cavalgar (Criaturas do Ar) entra na lista (antes travava).
- ⏳ Pendente testar elementais no device (dependem das entradas do catálogo): Convocar Fogo
  deve bloquear até 8 magias de Fogo; Controle/Criar exigem o anterior do mesmo elemento.

### Rollback
- `git revert a0235aa`.

----------------------------------------------------------------------------------------------------------------------------------------------------

## Lote 331 — [2026-06-01] fix: pré-requisito "N magias de escola/tema" + correções de texto

- **Hash:** `6ef376d`
- **Resumo:** o motor Nexus IGNORAVA o tipo de pré-requisito "N magias de uma escola"
  (ex: Convocar Elemental = "8 magias de Ar") — caía no `else` de `branchFromAlternative`
  e a magia liberava sem checar. Corrigido: agora vira `RegraNumerica` com escola e é
  validado de verdade, contando por ESCOLA (Fogo/Ar/...) ou, se o termo não for escola
  real, por NOME/tema (ex: "6 magias de Ácido" = magias com "Ácido" no nome). Mensagem
  clara de falta ("Ter 8 magias de Ar (atual N)").

### NexusArcanoEngine.kt
- `RegraNumerica` ganhou `escolaRequerida` + `minMagiasEscola`.
- `branchFromAlternative`: trata `PreRequisitoType.MagiasEscola` (antes ignorado).
- `atendeRegraNumerica` + `contarMagiasPorEscolaOuNome`: conta por escola real OU por
  nome (fallback p/ temas). `escolasConhecidasNorm` derivado do catálogo.
- Chave de falta com descrição clara.

### magias2versao.json (usuário + IA)
- USUÁRIO: criou sub-escolas (Cavalgar Terra/Ar/Mar, elementais por elemento, balizas,
  sentidos separados) e corrigiu nomes de escola incompletos no texto: "Luz"→"Luz e Trevas"
  (8 magias), "Controle da Mente"→"Mente" (4), "Planta"→"Plantas".
- IA: ajustou os 7 especiais (visao_brilhante/microscopica "não pode ser"; geiser; corpo_de_vento
  "NH 16"; remover_infeccao "Deteriorar, Purificar ou Cura"; remover_maldicao). Removeu o ", ou"
  (vírgula antes de "ou" quebrava o parser).
- Regra de precedência documentada em `VALIDAR_PRECEDENCIA.md`: E > OU ("X, Y ou Z" = X E (Y OU Z)).

### Validação
- ✅ Compila. Testado (device + JUnit temporários, depois removidos): Convocar Elemental,
  Cavalgar, Raio Solar (com 6 Luz e Trevas), ~18 magias de escola/tema agora validam certo.

### PENDÊNCIA GRANDE (próximo lote) — 43 magias ainda liberam com ficha vazia
- **Raiz única:** quando uma alternativa do "OU" (ou dependência) é um requisito que o motor
  NÃO reconhece (vantagem/perícia como "Persuasão", "Resistência a Danos"; magia nominal que
  não resolve; "X de cada elemento"), a branch fica VAZIA e `all{}` de lista vazia = true →
  vira "passe livre". Ex: infravisao ("Visão Aguçada ou 5 magias..."), acalmar_animal, etc.
- **3 abordagens a avaliar:** (a) corrigir o motor (branch de requisito não-reconhecido vira
  IMPOSSÍVEL, não vazia); (b) corrigir texto no JSON; (c) função no motor que reconheça
  vantagem/perícia/tema. Decidir no próximo lote — exige simular as 853 antes/depois.
- Também pendente: "magia X com NH 16+" (SkillMinLevel) e "N de cada elemento".

### Rollback
- `git revert 6ef376d`.

----------------------------------------------------------------------------------------------------------------------------------------------------

## Lote 332 — [2026-06-01] fix: pré-requisito de magia com sufixo "/NT" não casava

- **Hash:** `8b95626`
- **Resumo:** continuação do Lote 331. Várias magias citam no pré-requisito o nome de
  outra magia SEM o sufixo "/NT" (ex: "Controle de Máquina"), mas a magia real tem o
  sufixo ("Controle de Máquina/NT"). O matching exigia nome igual → não achava → a magia
  liberava sem o pré-requisito. Corrigido em `variantesSingularPlural` (NexusArcanoStrings):
  além de singular/plural, gera a variante SEM o sufixo " nt"/" tl" (a "/" já vira espaço
  na normalização, então o sufixo fica solto no fim). Resolveu ~6-9 magias de máquina/
  combustível (convocar_maquina, falar_com_maquinas, identificar_funcao, panent, etc.).

### Validação
- ✅ Compila. Varredura (ficha zerada AM0/IQ10, JUnit temporário removido): magias que
  liberavam errado caíram de 43 → 34.

### PENDÊNCIA (continua) — 34 magias ainda liberam com ficha vazia
- **~15 VANTAGEM:** preReq cita vantagem (Empatia, Resistência a Danos, Visão Aguçada,
  Noção do Perigo, Audição Aguçada). O motor NÃO recebe as vantagens da ficha
  (`ArcanoEstadoPersonagem` só tem magias/AM/IQ/DX). Próximo passo: passar
  vantagens da ficha pro motor e checar (ideia do usuário: se tem na ficha libera, senão bloqueia).
- **~10 MAGIA sub-escola/nome:** "Convocar Animal"/"Controle de Animal" (existem só como
  sub-escola: Criaturas da Terra/Ar/Mar) — motor precisa aceitar "qualquer variação".
  Outras: "Ampliar Objeto", "Voz Ampliada", "Transmissão de Pensamento", "Possessão".
- **~9 TEMA/elemento/elementais-com-OU:** "6 magias com Energia", "1 de cada elemento",
  convocar_elemental_* (OU + 2ª parte não resolvida).
- Raiz comum (Lote 331): alternativa de OU/dependência não-reconhecida vira branch vazia = passe livre.

### Rollback
- `git revert 8b95626`.

----------------------------------------------------------------------------------------------------------------------------------------------------

## Lote 333 — [2026-06-01] data: correções de texto nos pré-requisitos (3 frentes) + sub-escolas

- **Hash:** `b8429b5`
- **Resumo:** o usuário corrigiu manualmente os textos de pré-requisito de dezenas de magias
  (nomes errados, plural, vantagem citada errada) e criou novas entradas por sub-escola
  (conexao_com_animal, criar_portal, repelir_animal — por elemento/tipo, como Cavalgar).
  Resultado: magias que liberavam com ficha vazia caíram de 34 → 21. Tudo que dependia
  SÓ de texto está resolvido; os 21 restantes dependem de correção no MOTOR.

### Correções de texto (exemplos)
- fortalecer: nome errado → "Resistência a Choques". telepatia → "Transmissão Mental".
  aumentar_objeto → "Alongar Objeto". condensar_vapor → "Ferver Água" (era "Fever").
- Vantagens citadas certas: "Empatia com Animais", "Noção do Perigo", "Audição Aguçada".
- "Sentido Aguçado (Visão/Audição)" como magia onde o livro pede.
- Novas entradas por sub-escola: conexao_com_animal_*, criar_portal (teleporte/tempo/planar),
  repelir_animal_*. baliza_teleporte/baliza_planar.

### PENDÊNCIA (21 restantes — todos dependem do MOTOR) → próximo lote
- **Frente 1 — VANTAGEM (~8):** "ou vantagem X" (Empatia, Noção do Perigo, Audição/Visão
  Aguçada). O motor (`ArcanoEstadoPersonagem`) NÃO recebe vantagens da ficha. Implementar:
  passar `vantagensConhecidas` e checar. Ideia do usuário: tem na ficha → libera; senão bloqueia.
  Magias: acalmar_animal, agitar_animal, conceder_energia, medo, descanso_final,
  percepcao_do_perigo, visao_sonora, olhos_do_falcao.
- **Frente 2 — NOME-BASE = sub-escola (~3):** localizar_animais/possessao_de_animais pedem
  "Convocar Animal"/"Possessão" (que só existem como sub-escola). Motor deve aceitar nome-base
  = qualquer "Nome (...)". conexao_com_animal_agua: conferir nome exato.
- **Frente 3 — TEMA/elemento (~6):** combustivel ("Energia"), conceder_idioma ("Comunicação"),
  detectar_pontos_fracos ("1 de cada uma das 4 escolas"), convocar_elemental_* (2ª parte do OU),
  atrofiar_sentidos, sabedoria, jato_de_som ("Voz Ampliada").

### Rollback
- `git revert b8429b5`.

----------------------------------------------------------------------------------------------------------------------------------------------------

## Lote 334 — [2026-06-02] feat: motor resolve as 3 frentes pendentes do pré-requisito de magia

- **Hash:** `df19c8a`
- **Resumo:** implementadas no MOTOR as 3 frentes que o Lote 333 deixou pendentes
  (dependiam de código, não de texto).

### Frente 1 — VANTAGEM
- `ArcanoEstadoPersonagem` agora carrega `vantagensConhecidasNorm`/`periciasConhecidasNorm`.
- `branchFromAlternative`: token nomeado não-resolvido (ex: "Empatia com Animais") deixa de
  ser descartado (passe livre) e vira `vantagensRequeridas` — só satisfeito se a ficha tiver
  a vantagem/perícia. Stop-list `tokensGenericosIgnorados` ("qualquer"...) evita bloquear curingas.
- Adapter (`falhaPreRequisitoHierarquica`/`calcular`) e `FichaMagicDelegate` passam as
  vantagens/perícias da ficha NORMALIZADAS (mesmo `normalize` do motor).
- Magias: acalmar_animal, agitar_animal, conceder_energia, medo, descanso_final,
  percepcao_do_perigo, visao_sonora, olhos_do_falcao.

### Frente 3 — "1 de cada elemento"
- `PreRequisitoParser.expandirCadaUmDosElementos`: "1 mágica de cada um dos quatro escola
  (ar,terra,fogo,agua)" → 4 segmentos `MagiasEscola` ANDados. Alvo: detectar_pontos_fracos.

### Frente 2 — NOME-BASE = sub-escola
- `resolverVariantesSubEscola` + `RequisitoBranch.gruposDependenciaOu`: nome-base
  ("Convocar Animal") casa com QUALQUER variante "(...)" via grupo OU. Beneficia
  localizar_animais, possessao_de_animais, conceder_idioma.

### Fix de cache
- `CacheKey` passou a incluir vantagens/perícias. Sem isso, a 1ª consulta (sem a vantagem,
  bloqueado) ficava cacheada e a 2ª (com a vantagem) retornava o resultado velho.

### Validação
- Build Visual debug compila verde. Testes alvo rodados: **6 falhas, TODAS pré-existentes**
  (anteriores ao lote — confirmado rodando o baseline sem o código deste lote), **0 regressão**.
  São elas: NexusArcanoEngineLote2Test::fallback_final / ::fallback_controlado;
  NexusArcanoEngineLoteAGlobalTest::metas_globais_de_desejo;
  NexusArcanoEngineStressMagiasV2Test::stress_ramificacoes / ::sweep_escola_encantamento;
  PreRequisitoParserTest::repository validates fallback magia vantagem pericia and escudo exception.

### PENDÊNCIA
- Frente 3 (tema/elemento) restante: combustivel ("Energia"), conceder_idioma ("Comunicação"),
  convocar_elemental_* (2ª parte do OU), atrofiar_sentidos, sabedoria, jato_de_som — o usuário
  vai analisar caso a caso (texto vs motor).
- "2 mágicas Localizar" (em localizar_animais) ainda cai como vantagem fake (tema-count não
  implementado) — bloqueia corretamente, mas não há rota de satisfação por contagem de tema.

### Rollback
- `git revert df19c8a`.

----------------------------------------------------------------------------------------------------------------------------------------------------

## Lote 335 — [2026-06-03] feat: motor entende "N magias quaisquer" + auditoria geral

- **Hash:** `56b6c71` (código) · docs: `RELATORIO_PREREQUISITOS_QUEBRADOS.md` em commit separado
- **Resumo:** pré-requisito "N magias quaisquer" (total da ficha) antes era ignorado
  pelo motor (caía no else) → magia liberava sem checar. Corrigido. Cobre Retardo
  ("AM3, 15 mágicas quaisquer"), Metamorfose Superior ("10 outras mágicas"),
  Anular Mágica (12), Suspender Magia (8).

### Mudanças
- `RegraNumerica.minMagiasQuaisquer` conta `estado.magiasConhecidasIds.size`.
- `branchFromAlternative` trata `QuantidadeOutrasMagias`: sem contexto = quaisquer;
  com contexto ("de mente") = contagem por escola/tema.
- `atendeRegraNumerica` valida total; `branchKey` + chave de UI ("Ter N magias quaisquer").
- Parser reconhece "N magias" e "N magias quaisquer" puro (regra nova, depois do escolaMatch).

### Auditoria de satisfatibilidade (todas as 873)
- Teste real: monta ficha que CUMPRE e verifica se libera. 32 magias NÃO liberam mesmo
  cumprindo → documentadas em `RELATORIO_PREREQUISITOS_QUEBRADOS.md` (grupos: digitação,
  metamorfose, "de cada elemento", sub-escola, não-validável). Rodolfo corrigindo os textos.

### Lag da tela de magias (diagnóstico via logcat — NÃO corrigido ainda)
- Causa: `prereqFailure`/`calcular` rodam na MAIN THREAD. Modo Alvo filtra as 873 de uma vez
  = ~10s congelando. Magia `acelerar_tempo` ("2 magias em 10 escolas diferentes") = 3,7s
  sozinha no celular (pathfinder explode). Fix pendente: mover cálculo p/ coroutine + loading.

### Rollback
- `git revert 56b6c71`.

----------------------------------------------------------------------------------------------------------------------------------------------------

## Lote 336 — [2026-06-03] feat: vantagem Idioma (ponta a ponta) + pré-requisito de magia por idioma

- **Hash:** `c8f0151`
- **Resumo:** criada a vantagem "Idioma" (GURPS p.23) usando o padrão `specialRule`, e
  ligada ao motor de pré-requisito de magia. Resolve magias que pediam "N idioma(s) Com
  Sotaque" (copiar, escriba, dom_da_escrita, dom_das_linguas, pergaminho_magico) — antes
  viravam vantagem-fake e nunca liberavam.

### Implementação (5 passos do plano)
1. `vantagens.v3.json`: vantagem `idioma` (costKind=special, rawCost "2 a 6").
2. `IdiomaRule.kt`: custo por nível — metade (fala OU escrita): Rud 1, Sotaque 2, Materna 3;
   custo final = fala + escrita. Registrada no `TraitRuleRegistry`.
3. `VantagemDialogs` + `IdiomaConfig` (em TraitSpecialRuleComponents): UI nome + nível
   Falado/Escrito, custo ao vivo (add e edit). Deps do LaunchedEffect atualizadas.
4. `FichaTraitDelegate`: `idioma` entra na lista de múltiplas instâncias. O nome do idioma
   é gravado na `descricao` → lista mostra "Idioma (Élfico)" e diferencia instâncias.
5. `FichaMagicDelegate.tokensIdioma`: gera tokens sintéticos por nível/contagem
   ("N idioma(s) <nível>", singular/plural/numeral por extenso) p/ casar no motor; nível
   superior conta para inferiores. SEM mexer no parser nem no motor (shim no delegate).

### Validação
- Custos: Rud/Rud=2, Sot/Sot=4, Mat/Mat=6, Mat/Nenhum=3, Mat/Rud=4, Nenhum=0.
- pergaminho_magico libera com 1 idioma Sotaque, bloqueia com Rudimentar/sem.
- dom_da_escrita libera com 3 idiomas Sotaque, bloqueia com 2. Build verde.

### PENDÊNCIA
- Língua materna (0 pt) hoje é só anotação — decidir se vira instância especial.
- JSON `magias2versao.json` (as 32 correções de texto do Rodolfo + passageiro_interno)
  ainda não commitado — commit de texto separado, a cargo do Rodolfo.

### Rollback
- `git revert c8f0151`.

----------------------------------------------------------------------------------------------------------------------------------------------------

## Lotes 337-339 — [2026-06-03] feat: "não ter Desvantagem", fixes UI Modo Alvo, caminho leve (perf)

- **Hash:** `f22f6eb`

### Lote 337 — pré-requisito "não ter Desvantagem X"
- Parser aceita "não ter / não possuir / sem X" (além de "não pode ter"); remove a palavra
  "Desvantagem" da condição.
- Motor: `RequisitoBranch.condicoesProibidas` + `atendeCondicaoProibida`; estado ganha
  `desvantagensConhecidasNorm`; incluído em cacheKey/branchKey/combinarBranches.
- Adapter/delegate passam as desvantagens da ficha. Resolve `paladar_remoto` (não ter
  Disosmia) e `visao_brilhante` (não ter Cegueira).

### Lote 338 — fixes do Modo Alvo (UI)
- `MagicDialogs`: campo "Qual encantamento?" só para `imunidade_a_encantamento` (heurística
  antiga `descricao.contains("encantamento")` pegava Pequeno Desejo, Cajado, Desejo, Encantar).
- `FichaViewModel.requisitarModoAlvo`: recalcula o snapshot também quando o ESTADO de magias
  muda — antes só ao trocar o alvo, então a lista de recomendadas CONGELAVA ao aprender magia.
- `SelectingMagicDialog`: lista do Modo Alvo lidera com as `proximasAcoes` do pathfinder e
  NÃO varre mais as 879 magias com `prereqsSatisfied` (só filtra quando há busca por texto).

### Lote 339 — caminho LEVE para a lista (performance)
- `NexusArcanoEngine.faltaPreRequisitoLeve`: só "liberada? e o que falta", SEM rodar
  `sugerirProximasAcoes` (pathfinder). `falhaPreRequisitoHierarquica` usa esse caminho.
- 6x mais rápido no PC (526→84ms nas 878 magias), ZERO divergência de veredito (testado).
  Elimina o caso patológico `acelerar_tempo` (~5s por magia na lista).

### Diagnóstico do lag (via logcat PERF, instrumentação temporária já removida)
- Antes: ~86s ao adicionar 1 magia (varredura dupla de 879 + acelerar_tempo 5s na main thread).
- Depois dos fixes: ~11,7s — filtro 0ms, lista 17ms, pior magia 45ms.
- **PENDENTE:** `atualizarModoAlvoSnapshot` ainda ~9s (pathfinder do alvo desejo) → mover para
  background (coroutine + estado "calculando"); não perde precisão. Próxima sessão.

### Rollback
- `git revert f22f6eb`.

----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 340 (hash `763f42c`) — 08 de Junho de 2026
**Forjador: ficha completa — nome/historia auto, pericias por catalogo, threshold, modoEfetivo**
- Sistema aplica nome/historia/aparencia automaticamente apos geracao (regex no MestreIAGeneratorUseCase)
- buscarCatalogo("pericia","") retorna catalogo completo de 281 pericias de uma so vez
- Prompt instrui: usar query vazia para pericias — ler tudo, escolher, aplicar (sem buscas cegas uma a uma)
- Threshold resposta curta: 120->300 chars (evita encerramento prematuro com 8 tokens)
- modoEfetivo: geracao->analise quando historico existe (evita recriar personagem ao continuar conversa)
- Thinking desligado no Forjador (economiza tokens, acelera execucao)
- Reescrita dos prompts: 9 pilares em ordem, GPS opcional, regra anti-loop Aptidao Magica
- lerFicha("completo") entrega todas as secoes de uma vez
- buscarCatalogo retorna descricao de vantagens
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 341 (hash `b927a51`) — 09 de Junho de 2026
**Imagem: enquadramento de rosto no cabecalho + Forjador: correcoes de tecnica/JSON**
- Imagem: faixa do cabecalho 2:1, rosto centralizado, Compose Alignment.Center (corrige rosto cortado/so testa)
- Forjador: corrige bug do pre-requisito plural ("pericias pre-requisitos") em FichaSkillDelegate.extrairAncoraPericiaNoLimite — "Chute" (Briga ou Carate) falhava com as pericias na ficha
- Forjador: GPS de tecnicas — adiciona automaticamente a pericia-base nomeada no pre-req (1 pt) quando a ficha nao tem compativel (ForjadorToolExecutor.gpsAdicionarPericiaBaseDeTecnica)
- Forjador: remove mensagem tecnica "itens nao aplicados" do chat (fica so no Logcat)
- Forjador: corrige raca "null" (org.json.optString devolve string "null")
- Diagnostico: logs MestreIA_JSON (JSON cru da IA + IDs por secao)
- Docs: remove planos/relatorios obsoletos, adiciona Skill_GURPS.MD
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 342 (hash `9eab63c`) — 09 de Junho de 2026
**Forjador: 50 templates validados + fix budget | Imagem proporcional | Acessibilidade**
- Templates: 20 -> 50 no forjador_templates.json (todos com IDs validados contra catalogo, 0 quebrados)
- Corrige 5 IDs quebrados nos originais (carate, mineracao, sentido_de_direcao, obrigacao, sensivel)
- Expande tags/sinonimos dos 20 (samurai, ronin, espadachim, cavaleiro, feiticeiro...)
- +10 templates (fantasia: assassino/espadachim/cavaleiro/artifice/caca-mortos-vivos; generos: detetive/soldado/medico/cientista/horror)
- +20 templates do Rodolfo (8 IDs corrigidos na mesclagem; templates2.json -> lixeira)
- Fix budget: pontosBase do template vira referencia; budget real (pedido) sempre manda (corrige empurrao p/ 150 pts)
- scripts/add_templates.py + merge_templates.py: geradores com validacao de ID
- Imagem: recorte proporcional ao tamanho do rosto (rosto ~42% da faixa) -> enquadramento consistente entre artes
- Acessibilidade Pracego: rotulos em 8 botoes mudos (DiceRoller +-dados/mod, DialogsMestreIA historico/limpar, TraitDialogsV2 +-limitacao)
- UI: remove bloco custo/modelo do dialogo de gerar retrato
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 343 (hash `6ad7afc`) — 09 de Junho de 2026
**Forjador: +10 magos tematicos (60 templates no total)**
- 10 templates de mago do Rodolfo: Ilusionista, Invocador, Mago de Batalha, Cronomante, Astrologo, Encantador, Alquimista de Guerra, Necromante Branco, Bruxa do Pantano, Magista Runico
- Magias validadas contra catalogo (das 200, so premonicao e doenca nao existiam -> removidas)
- Cada mago: Aptidao Magica 2 + pericias base (ocultismo/alquimia/pesquisa) + ~20 magias tematicas
- scripts/check_magos.py + add_magos.py (validacao de magias)
- forjador_templates.json: 50 -> 60 templates, 0 IDs quebrados, 0 duplicados
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 344 (hash `c171a2c`) — 09 de Junho de 2026
**Forjador: pericias tematicas nos 10 magos (IDs validados)**
- Adiciona pericias por mago (principais 2pts / secundarias 1pt), ~150 pts total
- Todas validadas; 7 nomes resolvidos (Armaria->armeiro_nt, Conhecimento de Espiritos->conhecimento_oculto_conhecimento_espiritual, Detectar Mentiras->deteccao_de_mentiras, Simbologia Oculta->desenho_de_simbolos, Historia Antiga->historia)
- Astrologia e Historia Militar nao existem no catalogo -> removidas
- scripts/check_pericias_magos.py + add_pericias_magos.py
- 60 templates, 0 IDs quebrados
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 345 (hash `3aaa5f9`) — 09 de Junho de 2026
**Forjador: enriquece os 10 magos (vantagens/desvantagens/equip/atributos)**
- Cada mago ganha identidade propria (antes todos identicos: so aptidao_magica+curiosidade)
- Vantagens tematicas (Carisma/Voz, Empatia com Espiritos/Plantas, Intuicao, Nocao Exata do Tempo, Reflexos em Combate, Resistente, Versatil)
- Desvantagens caracteristicas (Luxuria, Voto, Reputacao, Senso do Dever, Obsessao, Distraido)
- Equipamentos (bordao/cajado, adaga, tunica/vestes) + atributos variados por arquetipo
- Tudo validado; corrige espada_longa->espada_larga em guerreiro_espada_escudo e paladino_sagrado
- scripts/enrich_magos.py | 60 templates, 0 IDs quebrados em TODOS os campos
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 346 (hash `214e0c7`) — 09 de Junho de 2026
**Rolagem: automacao de Golpe Fulminante / Erro Critico (tabelas 3d6)**
- Rolagem de combate (Ataque/Defesa/Tecnica/Magia) com Decisivo/Critico dispara 2a rolagem 3d6 nas tabelas
- Mostra AS DUAS tabelas (Decisivo: Golpe Fulminante + na Cabeca; Critico: Erro Critico + Desarmado) - jogador escolhe na interpretacao
- assets/tabelas_criticas.json (4 tabelas, entradas 3-18, texto completo)
- domain/roll/CriticoRules.kt: regra de critico COMPLETA com NH (5@NH15+, 6@NH16+, 17@NH<=15, soma>=NH+10) + loader + rolarTabela
- TabRolagem: classificacao correta + disparo automatico (Atributo/Pericia NAO disparam)
- server.js: classificarCritico com NH + render da 2a mensagem (testType 💥/💀). EXIGE deploy Railway
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 347 (hash `2615e92`) — 09 de Junho de 2026
**Import: ficha do WhatsApp abre direto no app (acessibilidade)**
- Problema: clicar na ficha .json/.gurps compartilhada (WhatsApp) nao abria no app; tinha que salvar e procurar nas pastas (ruim p/ cegos)
- Causa: intent-filters so cobriam application/json e text/plain; WhatsApp reatribui p/ application/octet-stream (generico)
- Manifest: ACTION_VIEW cobre octet-stream/text-json/x-gurps; novo filtro por EXTENSAO (pathPattern .gurps/.json, host=*, mime=*/*) casa pelo nome do arquivo; ACTION_SEND ampliado
- MainActivity: limpa BOM/espacos antes do parse + aviso de arquivo vazio
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 348 (hash `83ff3d3`) — 09 de Junho de 2026
**Import/Export: imagem do personagem viaja junto com a ficha**
- Exportar/compartilhar embute a imagem ORIGINAL no .json (base64); importar em qualquer celular salva+recorta o rosto e a foto reaparece
- Personagem: campo imagemPersonagemBase64 (so na exportacao; limpo no import - nao incha persistencia local)
- ImagemPersonagemStore.salvarDeBase64 (decodifica + reusa pipeline de salvarImagem)
- FichaViewModel: exportar*ComImagem (suspend) + restaurarImagemEmbutidaSeHouver
- FichaScreen: 3 call sites (compativel/versionado/compartilhar) usam versao com imagem em coroutine
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 349 — 12 de Junho de 2026
**GURPS Saga Fase A1: limpeza cirurgica de codigo morto e assets-lixo (branch GURPS-Saga)**
- DELETADOS: MestreIATopicIndex.kt (morto desde L272) e MestreIAPlanner.kt (879 linhas; data class TermoPonderado MOVIDA para MestreIAQueryEngine)
- MestreIAUseCase: removidos executarBuscaCodex, gerarCatalogoDireto, reescreverQueryParaGurps, os 5 when-cases mortos das tools de embedding, campo graphEngine e ToolResult.Duplicada (+todasDuplicadas) — orfaos apos a remocao
- MestreIATools: getOpenAITools/getGeminiTools renomeadas getLegacyEmbeddingToolsOpenAI/Gemini + comentario-guarda (nao adicionar tools ali)
- BUG MAPEADO (NAO corrigido — fora do escopo do A1): getAuditorUnificadoTools* (modo analise, commit d9d999c) monta a base com o toolset LEGADO de embedding achando que eram as ForjadorTools; executor nao roda 8 dos schemas oferecidos. Corrigir em lote proprio — ver ARQUITETURA_MESTRE_IA.md secao 5.7
- Cabecalhos-guarda "USADO APENAS PELA VOZ (GeminiLive) E FORJADOR" em MestreIAGraphEngine/VectorEngine/SemanticEngine
- Assets movidos para lixeira/assets_lote349 (fora do APK): pericias_v2_rules_map copy.json, topic_index.json, topic_index_backup_manual.json, topic_index_gerado.json (~933 KB brutos; APK debug -0,15 MB: 98,31 -> 98,16 Visual / 98,17 PraCego)
- VttHostAutoDetect: leitura de /proc/net/arp pulada em API >= 29 (Android 10+ bloqueia; cai direto no scan ativo)
- Build: assemble Visual+PraCego (debug e release) VERDE. Testes: 17 falhas PRE-EXISTENTES no Lote 348 (paridade 17=17 provada rodando a suite num worktree limpo do commit base aefb3ce) — ZERO regressao deste lote; consertar em lote dedicado
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 350 — 12 de Junho de 2026
**Fix: toolset unificado do modo analise (Auditor) + teste de contrato**
- CORRIGE o bug mapeado no Lote 349: getAuditorUnificadoToolsOpenAI/Gemini montavam a base com o toolset LEGADO de embedding (engano do commit d9d999c) — o modelo recebia 8 schemas que o executor nao roda e NAO recebia as tools de ficha
- Base agora e ForjadorTools.getOpenAITools()/getGeminiTools() + localizar_no_codex + ler_pagina + consultar_nexus_arcano (duplicata de nexus eliminada)
- Toolset legado DELETADO (getLegacyEmbeddingTools*, getSheetSchema*, getArrayOf*, constantes TOOL_MANUAL_DIRETO/TOOL_REGRAS_*) — ~460 linhas; TOOL_FILL_SHEET e TOOL_INSPECT_CHARACTER permanecem (ainda usados)
- NOVO MestreIAToolsTest (4 testes de CONTRATO): cada toolset enviado a IA bate EXATAMENTE com o conjunto que o executor aceita (unificado = 9 tools; Auditor = 4) — quebra se alguem dessincronizar toolset x executor
- build.gradle.kts: testImplementation org.json:json (android.jar dos unit tests so tem stubs "not mocked")
- Docs: ARQUITETURA_MESTRE_IA.md secao 5.7 (bug corrigido) + MAPA_DETALHADO.md secao 14 atualizados
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 351 — 12 de Junho de 2026
**Conserto das 17 falhas de teste pre-existentes (suite 100% verde nas 2 variantes)**
Diagnostico caso a caso: 14 eram TESTES DESATUALIZADOS (codigo evoluiu de proposito), 2 eram BUGS REAIS no codigo, 1 era teste quebrado por construcao.
- TESTES ATUALIZADOS para o contrato vigente:
  - PersonagemRulesTest (2): BD do escudo soma em TODAS as defesas ativas desde o Lote 30 (MB p.374) — esquiva 7->9 e 8->10, apara 8->10
  - MestreIAContextFilterTest (5): reescrito — gerarContexto devolve TEXTO compacto (Token Economy L53+), nao mais JSON com truncamento/pontos
  - PericiaJsonParsingTest (2): mojibake e consertado PELO LOADER (fixMojibakeIfNeeded, L314) antes do Gson; teste agora valida o pipeline real (fix + parse)
  - NexusArcanoEngineLote2Test (2): fallback de escolas repetidas foi REMOVIDO por design ("Passo 2 Fallback Removido" em NexusArcanoHeuristics) — sem escola nova: lista vazia + motivoCodigo SCHOOL_COUNT_PENDING
  - NexusArcanoEngineLoteAGlobalTest (1): METAS INCREMENTAIS (L50) — so a PROXIMA meta de escolas pendente aparece (a de 15 escolas do desejo so apos a do encantar)
  - NexusArcanoEngineStressMagiasV2Test (2): teto de proximas acoes e 5 (take(5): 3 imediatas + lookahead), nao 3
- BUGS REAIS CORRIGIDOS no codigo:
  - MestreIAClient.extrairJsonFicha: recorta no '}' que FECHA o primeiro '{' (balanceamento, ignora chaves em strings) — prosa apos o JSON derrubava o parse (Gson estrito); intencao do Lote 52 restaurada
  - PreRequisitoChecker/DataRepository: EXCECAO DO ESCUDO restaurada e ESCOPADA (novo flag contextoMagia, default false) — em pre-requisito de MAGIA, "Escudo" e a MAGIA Escudo (Livro de Magia, Protecao); a pericia Escudo nao satisfaz. Validacao de PERICIAS nao muda (flag desligado). A regra existia no caminho MagiaConhecida mas o fallback do parser passou a emitir VantagemConhecida, contornando-a
- DELETADO: MestreIARagEngineTest — quebrado por construcao ("null as Any as Context" lanca NPE sempre) e media performance do motor RAG legado (dormente p/ Auditor desde L325)
- LINT BASELINE (lint-baseline.xml): com os testes verdes o build avancou ate o lint, que tem 16 erros ANTIGOS (ex.: MissingPermission no GeminiLiveService/Voz — protegido, fora do escopo). Baseline congela a divida documentada; build falha so em erro NOVO de lint
- Resultado: ./gradlew build COMPLETO VERDE (testes 2 variantes + assemble + lint). A partir deste lote a regra 3 do plano Saga (build e lei) vale de verdade
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 352 — 12 de Junho de 2026
**GURPS Saga Fase A2: um motor de busca so + Codex em dieta (branch GURPS-Saga)**
- VOZ migrou para o motor do Auditor: GeminiLiveTools.consultarManual agora usa localizarNoCodex (FTS4 AND/OR + ranking BM25) + lerPaginas — em UMA chamada localiza, le o texto COMPLETO das 3 melhores paginas e devolve compacto com citacao [Livro, Pag]
- LIVE_MAX_TOOL_PAYLOAD (18k chars) em local unico + truncamento CENTRALIZADO no roteador executar() — QUALQUER tool da Voz fica protegida do code=1007 do Gemini Live; loga quando trunca
- Caminho semantico (GraphEngine/HNSW/ObjectBox/SemanticEngine) ficou com ZERO callers: cabecalhos atualizados p/ "LEGADO DORMENTE desde Lote 352" (nao deletados, conforme plano); inicializacao do HNSW no startup (FichaIADelegate) comentada — nao carrega mais embeddings mortos na RAM
- CODEX EM DIETA: chunks.jsonl 54,9 MB -> 6,5 MB (mesmo texto, SEM embeddings; conferido: 1197 chunk_ids identicos, zero perda). chunks.jsonl.bak movido p/ lixeira/assets_lote352. CODEX_VERSION_CURRENT 3 -> 4 (forca re-seed do Room no proximo boot)
- APK: debug 98,2 -> 74,8 MB (-23,4) | release 86,9 -> 66,0 MB (-20,9). DIVERGENCIA do aceite ">=40 MB menor": o plano assumiu bytes BRUTOS, mas o zip ja comprimia os embeddings (54,9 brutos ~ 20,6 comprimidos) — no APARELHO a economia e bem maior (asset -48 MB + Room sem embeddings + ObjectBox vazio + RAM do indice HNSW)
- ARMADILHA DESCOBERTA: o empacotador incremental (zipflinger) substitui entries e deixa "buraco" no zip — o APK nao encolhe ate forcar reempacotamento. Medicoes de tamanho sempre apos apagar o APK e reempacotar
- Build: ./gradlew build COMPLETO VERDE (testes 2 variantes + lint + assemble)
- PENDENTE (validacao do usuario no aparelho): roteiro do passo 5 — 3 perguntas de regra na VOZ e 3 no AUDITOR, todas citando pagina
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 353 — 13 de Junho de 2026
**GURPS Saga Fase A4: fundacao de dados da campanha + contrato de tools do Narrador (branch GURPS-Saga)**
- data/storage/SagaEntities.kt: 4 entidades novas — CampanhaEntity, CenaEntity, CampaignFactEntity (FTS4: campanhaId/sujeito/predicado/objeto/peso/cenaId/texto), WorldStateEntity (PK campanhaId)
- data/storage/SagaDao.kt: CRUD de campanha/cena/world_state + buscarFatos(campanhaId, query, limite) — normaliza a consulta (CatalogFilters.normalizarBusca, mesma do Codex), MATCH AND com fallback OR, ranking peso DESC depois frequencia dos termos (BM25 simplificado, padrao do ManualChunkDao)
- FichaDatabase.kt: v24 -> v25, registra as 4 entidades + sagaDao(); MIGRATION_24_25 EXPLICITA (SQL conferido byte a byte contra o createAllTables do FichaDatabase_Impl gerado pelo Room) mantendo fallbackToDestructiveMigration como rede
- domain/saga/NarradorTools.kt: contrato das 14 tools do Narrador (pedir_rolagem, iniciar_combate, acao_npc, aplicar_dano, aplicar_condicao, gastar_recurso, consultar_mundo, registrar_fato, avancar_relogio, passar_tempo, conceder_xp, definir_cena, forjar_npc, inspecionar_personagem) + reuso localizar_no_codex/ler_pagina = 16 no total. Schemas Gemini E OpenAI gerados de uma spec neutra unica. Descricoes CATEGORIAIS (regra 6, zero exemplos)
- domain/saga/NarradorToolExecutor.kt: roteador suspend executar(nome, argsJson): String no padrao do ForjadorToolExecutor. REAIS: registrar_fato, consultar_mundo, inspecionar_personagem (delega lerSecao), localizar_no_codex/ler_pagina (delega ao DataRepository). Resto -> {"erro":"nao_implementado","tool":...}; desconhecida -> ferramenta_desconhecida; dependencia ausente degrada com erro JSON (nao excecao)
- TESTES: NarradorToolsTest (JVM, contrato dos 2 toolsets == TODAS, 16 tools, obrigatorios) VERDE; SagaFoundationTest (instrumentado, Room em memoria + FTS4 REAL do device: 5 fatos/busca/ordenacao por peso/isolamento por campanha + roundtrip registrar->consultar + roteamento nao_implementado/desconhecida/sem_campanha) 3/3 VERDE no Pixel_8a API 34
- MIGRACAO VALIDADA no emulador: instalado v25 por cima do v24 SEM desinstalar; logcat sem Migration/Room/IllegalStateException/FATAL e "CODEX OK v4: 1197 chunks" (dados PRESERVADOS = migracao aditiva real rodou, NAO o fallback destrutivo; schema das 4 tabelas validado pelo Room)
- DIVERGENCIA do plano (passo 3): o plano pede "siga o padrao das migracoes anteriores" mas NAO EXISTE migracao anterior — o projeto sempre usou fallbackToDestructiveMigration (bump apagava o banco; fichas sobreviviam pela nuvem). Esta e a PRIMEIRA migracao explicita; documentado no comentario do MIGRATION_24_25
- DIVERGENCIA menor: §3.2 do PLANO_GURPS_SAGA_v2 (citado no passo 4) nao existe no repo; os parametros das tools foram projetados a partir dos nomes/objetivos do proprio plano
- Efeito colateral necessario: res/drawable/Sir Aldric.png (nome invalido com espaco/maiuscula, travava o merge de recursos) movido para lixeira/ — sem relacao com a Saga, mas o build nao compilava sem isso
- Build: ./gradlew build COMPLETO VERDE (testes 2 variantes + lint + assemble)
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 354 — 13 de Junho de 2026
**GURPS Saga Fase A5: Narrador minimo viavel + Aba Saga (branch GURPS-Saga)**
- data/network/MestreIAPromptsNarrador.kt: persona CATEGORIAL do Narrador (modo "saga") — leis de ferro (nunca declarar numero/resultado sem tool do turno; pedir_rolagem com mods nomeados; fatos de consultar_mundo sao canonicos; max 3 paragrafos; terminar abrindo escolha), uso de cada tool em 1 frase, proibicoes. Zero exemplos (regra 6)
- domain/MestreIANarradorUseCase.kt: clone estrutural do Generator — top-5 consultar_mundo automatico sobre a msg do jogador, contexto via MestreIAContextFilter + cena + 8 turnos, fila de modelos (Gemini 2.5 Pro -> DeepSeek V3), loop de tool-use com NarradorToolExecutor, validacao final + 1 re-pedido (Auto-Healing)
- data/network/MestreIAClient.kt: modo "saga" -> NarradorTools nos DOIS switches (Gemini e OpenAI) + require(modo in MODOS_VALIDOS) no inicio de perguntarAoMestre (conversa/geracao/analise/planejamento/saga)
- domain/saga/NarradorOutputValidator.kt: regex de resultado mecanico (\d+ (de )? dano|PV|PF|margem) sem tool correspondente no turno -> 1 instrucao de correcao. +NarradorOutputValidatorTest (4 casos) VERDE
- domain/saga/NarradorToolExecutor.kt: + interface RollBridge + execucao REAL de pedir_rolagem (suspende ate a UI tocar o dado)
- viewmodel/delegates/FichaSagaDelegate.kt: estado observavel da aba (campanhas/cena/feed/rolagemPendente/fase/processando), ponte de rolagem (resolve NH da ficha por nome de pericia/atributo -> alvo, 3d6 + CriticoRules.classificar — MESMO caminho da TabRolagem), persistencia dos turnos nas tabelas de chat (sessao "saga#<id>" por campanha, sem migracao), CRUD de campanha/cena via SagaDao
- FichaViewModel: instancia o delegate + getters/metodos saga* (additivo)
- ui/TabSaga.kt: sem campanha -> criar/continuar; com campanha -> feed (bolhas jogador/narrador/sistema) + indicador de fase (liveRegion) + card de rolagem (toque = 3d6) + barra de envio. Maquina de escrever local (2-3 palavras/30ms) no ultimo turno do Narrador. TalkBack: contentDescription em bolhas, card e controles
- FichaScreen + FichaCustomNavigationBar: aba "Saga" registrada (icone reusa tab_mestre_ia)
- Build: ./gradlew build COMPLETO VERDE (testes 2 variantes + lint + assemble). Smoke test no emulador: app abre sem crash com o delegate Saga no construtor do VM (PID vivo, zero FATAL)
- DIVERGENCIA do plano: §3.2 do PLANO_GURPS_SAGA_v2 (citado) nao existe no repo — parametros das tools projetados a partir dos nomes/objetivos do plano. Persistencia "CenaEntity/chat": optei pelas tabelas de chat (sessao por campanha) por nao exigir migracao; CenaEntity guarda titulo/resumo
- PENDENTE (validacao do usuario no aparelho): roteiro do aceite — criar campanha -> "tento ouvir a conversa dos guardas" -> card Audicao com mods -> tocar dado -> narracao cita a margem real -> fato registrado -> fechar/reabrir -> contexto continua. (Requer chaves de IA reais; a IA roda no device.)
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 355 — 13 de Junho de 2026
**Polimento do Narrador (correcao pos-teste real do A5 — branch GURPS-Saga)**
Baseado na analise da 1a sessao de jogo real do usuario (logcat): nucleo OK (rolagem interativa, fatos, lei de ferro), mas 4 pontos crus corrigidos:
- (A) definir_cena REAL: estava nao_implementado; agora grava titulo/bioma/humor/resumo na CenaEntity (SagaDao.atualizarDescricaoCena), preserva campo omitido, e o cabecalho da TabSaga reflete a cena (delegate re-le cenaAberta apos cada turno)
- (B) Cena de ABERTURA automatica: ao criar campanha, o Narrador enquadra onde o heroi esta + 1o gancho (prompt-semente de sistema, nao vira bolha de jogador) — resolve o "comecei perdido" observado no teste
- (C) Narracao no FLASH: fila do Narrador agora Gemini 2.5 Flash -> 2.5 Pro -> DeepSeek V3 (antes Pro primario, 15-19s/turno). Narracao nao precisa de raciocinio pesado
- (D) consultar_mundo automatico por PALAVRAS-CHAVE: extrairPalavrasChave (tira pontuacao/aspas/stopwords, <=8 termos) em vez da frase crua do jogador — query FTS melhor
- TabSaga: texto de estado vazio ajustado ("Narrador preparando a cena de abertura")
- Sem mudanca de schema (Room segue v25; so 1 @Query novo). Build completo verde 2 variantes
- NAO incluido (fora do escopo escolhido A+B+C+D): margem negativa e warning "resource failed to call release" — anotados para lote futuro
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 356 — 13 de Junho de 2026
**Configuracao de campanha (session zero) + excluir campanha (branch GURPS-Saga)**
Pedido do usuario apos teste real: faltava lixeira pra apagar campanha e um menu de definicoes do jogo.
- domain/saga/CampanhaConfig.kt: data class do "session zero" (genero, conceito livre, tom, dificuldade, magiaPermitida, nivelTecnologico NT, livros liberados) + paraPromptBloco() que vira bloco CATEGORIAL no system prompt do Narrador (tom/dificuldade traduzidos em instrucoes; magia off = "nao existe neste mundo"; livros = trava textual). Gson para (de)serializar
- SagaEntities: CampanhaEntity ganha configJson (TEXT) | FichaDatabase v25->v26 + MIGRATION_25_26 (ALTER TABLE campanhas ADD COLUMN configJson TEXT NOT NULL DEFAULT '{}'); SQL conferido vs createAllTables do _Impl gerado
- SagaDao: excluirCampanhaCompleta (@Transaction) apaga fatos+cenas+world_state+campanha sem orfaos
- FichaSagaDelegate: criarCampanha(nome, config) salva configJson; rodarTurno injeta configBloco no narrar(); excluirCampanha(id) (limpa tambem a sessao de chat "saga#id")
- MestreIANarradorUseCase.narrar: novo param configBloco -> entra no systemBase
- FichaViewModel: sagaCriarCampanha(nome, config) + sagaExcluirCampanha(id)
- ui/TabSaga: form de criacao expandido (FilterChips de genero/tom/dificuldade/livros, Switch magia, stepper NT, conceito livre) + lixeira por campanha com AlertDialog de confirmacao. TalkBack em todos os controles
- MIGRACAO v25->v26 VALIDADA no emulador (instalado por cima do banco real com a campanha "Quartedec"; logcat sem Migration/IllegalState/FATAL + "CODEX OK v4 1197 chunks" = dados preservados)
- Build completo verde 2 variantes
- NOTA: a config hoje e TRAVA TEXTUAL no prompt (o Narrador e instruido a respeitar). Trava REAL de tools/catalogo (ex.: bloquear magia de fato) fica para quando a Fase B/C precisar
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 357 — 13 de Junho de 2026
**UI: tela de criacao da Saga limpa (configuracoes em dialogo) (branch GURPS-Saga)**
Feedback do usuario: a tela de criacao estava POLUIDA (genero/tom/dificuldade/magia/NT/livros todos inline).
- ui/TabSaga: tela de criacao agora so tem Nome + botao "Configuração do Jogo" (icone Tune) + resumo de 1 linha (ex.: "Faroeste · Sombrio · Dificil · NT5 · sem magia") + botao Criar campanha
- Novo ConfiguracaoJogoDialog: AlertDialog rolavel (heightIn max 460dp) com TODAS as definicoes dentro (FilterChips genero/tom/dificuldade/livros, conceito, Switch magia, stepper NT); botao "Concluir"
- Estado consolidado em um unico `config: CampanhaConfig` (antes eram 7 vars soltas); edicao live via config.copy(...)
- resumoConfig() helper para o texto sob o botao. TalkBack mantido em todos os controles
- SO UI: zero mudanca de logica/schema/regras. Build completo verde 2 variantes
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 358 — 13 de Junho de 2026
**UI: dialogo "Configuração do Jogo" em tela cheia + barra de rolagem visivel (branch GURPS-Saga)**
Feedback do usuario: o dialogo de config deveria ser TELA CHEIA e mostrar uma barra de rolagem (nao dava pra perceber que havia mais conteudo abaixo).
- ui/TabSaga: ConfiguracaoJogoDialog deixou de ser AlertDialog -> agora Dialog(usePlatformDefaultWidth=false) + Surface fillMaxSize = TELA CHEIA
- Barra superior fixa (titulo + Concluir + X) + HorizontalDivider; conteudo num BoxWithConstraints/verticalScroll
- BarraDeRolagem (composable nova): thumb desenhado a partir do ScrollState (o Compose Android nao tem scrollbar nativa) — posicao/altura do thumb calculadas com LocalDensity (viewport vs maxValue); aparece so quando ha rolagem
- SO UI: zero mudanca de logica/schema/regras. Build completo verde 2 variantes. Nao testado no emulador (usuario roda no Android Studio)
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 359 — 13 de Junho de 2026
**Saga FASE B (Combate) B1: modelos + sequencia de turnos (branch GURPS-Saga)**
Inicio da Fase B. Tudo em domain/combat/, KOTLIN PURO (zero dependencia de Android), testavel sem UI. Referencias // MB nos comentarios.
- domain/combat/CombatModels.kt: Postura (6, MB p.551), Condicao (atordoado/caido/inconsciente/agarrado/surpreso), Manobra (11: ataque, ataque total, defesa total, mover, mover e atacar, mudar postura, preparar, aguardar, avaliar, concentrar, nao fazer nada), DefesasUsadas (base p/ B5), NpcStats (vel/desloc default de DX+HT), Combatente (estado mutavel pv/pf/postura/condicoes + vivo/caido)
- domain/combat/CombatEncounter.kt: ordem por Velocidade Basica desc -> DX desc -> aleatorio com SEED (deterministico); proximoTurno()/rodadaAtual; engajado() (corpo-a-corpo se dist<=1 do heroi); manobrasLegais() (inconsciente->nenhuma; atordoado->Defesa Total/Nao Fazer Nada; caido->sem Ataque Total/Mover e Atacar; sem alvo engajado->sem ataque CaC); estadoResumo() factual e deterministico p/ a IA
- CombatEncounterTest: ordem com 4 combatentes e 2 empates (vel+DX) + determinismo por seed; proximoTurno/rodada; manobras legais em 6 estados; estadoResumo deterministico/factual. VERDE
- DIVERGENCIA: §4.1 do PLANO_GURPS_SAGA_v2 nao existe no repo; campos derivados do MB + Skill_GURPS
- Build completo verde 2 variantes
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lotes 360-362 — 13 de Junho de 2026
**Saga FASE B: B2 (ataque/manobras) + B3 (dano localizado) + B4 (estados vitais) (branch GURPS-Saga)**
Tres lotes de regra PURA (domain/combat, sem Android), agrupados num commit para economizar builds (autorizado pelo usuario). Cada um com sua suite de testes.
- B2 (CombatActions.kt + ModificadoresCombate.kt): calcularNH (PURO) = NH arma ± manobra (Ataque Total Determinado +4; Mover-e-Atacar CaC -4, a distancia teto 9) ± postura ± local visado ± visibilidade; avaliarRolagem (acerto/margem/critico via CriticoRules); resolverAtaque (3d6 + relatorio legivel "NH 14 -3 vitais -2 escuro = 9; rolou 8: acerto, margem 1"); flags atacanteSemDefesaAtiva (Ataque Total) e semApararDepois (Mover-e-Atacar). LocalAtaque (11 locais, penalidade de mira MB p.398), Visibilidade, AtaqueTotalModo. CombatActionsTest: matriz de 14 NH + criticos + flags. VERDE
- B3 (HitLocationRules.kt): PORTE FIEL da calculadora da Mesa Virtual (index.html DAMAGE_RULES/applySmartDmg). DanoTipo (cont1.0/corte1.5/pi-0.5/pi1.0/pi+1.5/pi++2.0/perf2.0); overrides cranio x4 (qualquer tipo) e vitais x3 (so perfurante/perf); RD extra cranio +2; limite de membro braco/perna ceil(PV*0.5), mao/pe ceil(PV*0.33) com flag incapacitou. HitLocationRulesTest: 15 casos de PARIDADE com gabarito do JS. VERDE
- B4 (InjuryRules.kt): penalidadeChoque (-min(dano,4)); ehFerimentoGrave (>PV/2); aplicarGolpe (morte automatica <=-5xPV; cheques de morte -1x..-4xPV recem-cruzados; ferimento grave HT->atordoado+caido ou inconsciente por falha 5+; inconsciencia por PV<=0); recuperaAtordoamento; ferir(Combatente) muta PV/condicoes. InjuryRulesTest: choque/grave/recuperacao/morte-automatica + simulacao 0->morte com log e seed fixa. VERDE
- DIVERGENCIA (B2): plano diz "Mover-e-Atacar teto 9" (regra A DISTANCIA); no CaC a regra e -4. Implementei as duas (param aDistancia), matematica correta do GURPS. DIVERGENCIA (B3): paridade e com a Mesa Virtual (locais olho/pescoco/virilha sem override especial usam mult base — documentado)
- Build completo verde 2 variantes
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 363 — 13 de Junho de 2026
**Saga FASE B B6: bestiario + cerebro tatico de NPC (branch GURPS-Saga)**
- assets/bestiario.v1.json: 17 criaturas (goblin, goblin_arqueiro, kobold, orc, lobo, lobo_atroz, urso_pardo, rato_gigante, aranha_gigante, serpente_venenosa, esqueleto, zumbi, bandido, bandido_arqueiro, mercenario, ogro, cultista) com st/dx/iq/ht/pv/rd/vel/desloc/agressividade/moral/ataques. (Plano pede ~40; F1 expande — comecei com 17 cobrindo os arquetipos)
- scripts/check_bestiario.py (padrao dos checks): IDs unicos, campos obrigatorios, dano NdX±Y, tipo PT-BR valido (cont/corte/pi-/pi/pi+/pi++/perf), stats positivos, agressividade/moral 0-10, >=1 ataque. RODADO: 0 erros
- model/BestiarioModels.kt: BestiarioCriatura/AtaqueCriatura/Bestiario + BestiarioLoader.parse (Gson) + novoCombatente() (cria Combatente do B1). NpcStats ganhou agressividade/moral
- domain/combat/NpcCombatBrain.kt: decidir(npc, encounter, alvo, seed) deterministico — fuga por moral/PV (limiarFugaPV), arqueiro mantem distancia, bruto avanca/Ataque Total, default avanca/ataca. Fallback do acao_npc (B8)
- NpcCombatBrainTest: arqueiro mantem distancia, bruto avanca, covarde foge a 30% PV, bestiario carrega/integridade, 3 goblins avancam coerentes. VERDE
- BUG corrigido: Gson nao roda init de data class -> Bestiario.get() agora busca direto (sem mapa cacheado vazio)
- Build completo verde 2 variantes
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 364 — 13 de Junho de 2026
**Saga FASE B B5 (camada de regra): defesas no fluxo + troca completa (branch GURPS-Saga)**
- domain/combat/CombatResolver.kt: modificadores de defesa (recuo +3 esquiva / +1 apara-bloqueio; Defesa Total Determinada +2; apara extra -4 cumulativa por arma; bloqueio 1x/turno; defesa anulada por critico/surpresa); opcoesDefesa() (alimenta o card "Defenda-se!" do B7/B8); defesaBemSucedida (3-4 passa, 17-18 falha); resolverTroca() encadeando B2(ataque)->defesa->B3(dano localizado)->B4(ferimento)
- CombatResolverTest: modificadores (recuo/defesa total/apara extra), anulacao por critico/surpresa, bloqueio 1x/turno, e ROUND COMPLETO heroi×goblin com CRITICO FORCADO (anula defesa, fere de verdade) + round normal (apara com sucesso, sem dano). VERDE (aceite do B5 ao nivel de regra)
- DIVERGENCIA do plano (regra 12): (1) NAO estendi CombatRules.kt (domain/rules, usado pelo Personagem) p/ nao arriscar a ficha — a logica de defesa do combate fica em domain/combat/CombatResolver. (2) Executor real `aplicar_dano` no NarradorToolExecutor e o card "Defenda-se!" na UI dependem do ESTADO VIVO do encontro (nasce no B8 iniciar_combate) e da UI de combate (B7); fazer agora seria fragil/falso -> ADIADOS para B7/B8. B5 entrega a camada de resolucao testada
- Build completo verde 2 variantes
- ESCOPO "B2 a B6" do usuario: CONCLUIDO (B2,B3,B4,B6,B5). Restam na Fase B: B7 (UI CombatTracker) e B8 (Narrador<->combate: iniciar_combate/acao_npc/aplicar_dano reais + card Defenda-se), fora do pedido "b2 a b6"
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 365 — 14 de Junho de 2026  ·  commit `98a691e`
**Saga FASE B B7: UI de combate + motor de encontro (branch GURPS-Saga)**
- domain/combat/CombatSession.kt: SESSAO de combate (Kotlin puro) que orquestra um encontro inteiro encadeando B1-B6 — heroi ataca (resolverAtaque->resolverTroca), turno de NPC (NpcCombatBrain decide; override do Narrador entra no B8), defesa interativa do heroi, dano/ferimento, fim de combate (vitoria/derrota), parser de dano "<n>d[±m]" e mapeador de tipo. HeroiPerfilCombate/DefesaHeroi/ResultadoCombate
- Enablers minimos: NpcStats ganhou armaNh (o motor precisa do "para acertar" do NPC; novoCombatente popula do AtaqueCriatura.nh); CombatEncounter ganhou distancia MUTAVEL + moverEmRelacaoAoHeroi/definirDistancia (manobra Mover muda a faixa). Construtor inalterado -> testes antigos intactos
- ui/saga/CombatUi.kt (VISUAL APROVADO no mockup): CombatTracker (faixas Engajado->Extremo, avatar de INICIAL colorida [azul heroi/vermelho inimigo - placeholder do retrato real, ver registro B7/E2], barra de PV verde->amarelo->vermelho, postura/condicoes, heroi destacado); ManeuverCards (so manobrasLegais; sub-dialogo de alvo + local com penalidades visiveis; modo do Ataque Total); DefendaSeCard (opcoesDefesa com valor final + Rolar). Tudo com contentDescription p/ TalkBack (aceite: jogavel de olhos fechados)
- viewmodel/delegates/SagaCombatController.kt: embrulha a CombatSession com estado Compose (CombatUiState/CombatenteUi/FaixaDistancia) + corrotinas + ponte de defesa suspensa ("Defenda-se!"); le o heroi da ficha (NH da melhor pericia de combate, dano da arma equipada, esquiva/apara/bloqueio, RD da armadura) e devolve o PV ao fim do combate. domain/loaders/BestiarioCatalogo.kt (le assets/bestiario.v1.json, cache)
- Fiacao: TabSaga mostra o CombatePainel no lugar da barra de texto quando o combate esta ativo; getters/acoes sagaCombate* no FichaViewModel; controller criado no FichaSagaDelegate (linhas factuais -> turnos "sistema" efemeros no feed)
- CombatSessionTest: parser de dano, mapa de tipo, heroi ataca goblin adjacente, vitoria quando inimigos caem, NPC ataca heroi (esquiva soma 3 sempre defende), fuga por moral baixa. VERDE
- NAO inclui (vai no B8): iniciar_combate disparado pelo Narrador, acao_npc override, aplicar_dano/aplicar_condicao/gastar_recurso/conceder_xp reais, saque + prosa final agregada. O controller.iniciarCombate ja existe e sera chamado pelo executor no B8
- Build completo verde 2 variantes
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 366 — 14 de Junho de 2026  ·  commit `07a059b`
**Saga FASE B B8: integracao Narrador<->combate (branch GURPS-Saga)**
- NarradorToolExecutor: interface CombatBridge + roteamento das 6 tools que antes davam "nao_implementado" -> iniciar_combate, acao_npc, aplicar_dano, aplicar_condicao, gastar_recurso, conceder_xp. Parsers finos (validam args, delegam a bridge)
- FichaSagaDelegate implementa CombatBridge: iniciarCombate (delega ao controller); aplicar_dano (em combate -> combatente vivo via HitLocationRules+ferir; FORA de combate -> PV do heroi na ficha; tipo "fad" -> debita PF); aplicar_condicao (mapeia p/ Condicao; aplica no combatente, fora de combate vira nota); gastar_recurso (pf/pv reais e salvos; dinheiro/municao/item = nota narrativa); conceder_xp (xpGanhos += pts, salva, + turno "sistema" no feed). Mapeadores localDeString/condicaoDeString
- Fim de combate: SagaCombatController.onFim -> narrarFimDeCombate. finalizar() (guarda 1x) salva PV do heroi, computa SAQUE (armas dos inimigos derrotados, agrupadas) e entrega na ficha (sagaAdicionarItem), e dispara um turno do Narrador com o relatorio factual agregado p/ converter em PROSA (sem inventar numeros) + conceder_xp pelo marco
- SagaCombatController: aplicarDanoCombatente/aplicarCondicaoCombatente (efeitos do Narrador fora do loop), perfilHeroi() (perfil da ficha atual), emCurso (combate em andamento), reavaliarFim() na CombatSession
- FichaViewModel: sagaConcederXp/sagaDefinirPvAtual/sagaDefinirPfAtual/sagaAdicionarItem (mutam e SALVAM a ficha carregada pelo caminho normal salvarFicha)
- MestreIAPromptsNarrador: lei de ferro 8 (combate abre com iniciar_combate, jogador resolve na UI, Narrador narra so abertura+desfecho; nunca golpe a golpe nem numeros) + descricao das tools de combate refinada (categorial, zero exemplos)
- build.gradle.kts: testOptions unitTests.isReturnDefaultValues = true (android.util.Log retorna default no teste JVM -> permite testar o roteamento do executor)
- NarradorToolExecutorCombatTest: roteamento das 6 tools com CombatBridge falsa (parse de inimigos, campos obrigatorios, acao_npc exige combate ativo, degradacao sem bridge). VERDE
- DIVERGENCIA (regra 12): o "round de NPCs em LOTE" do plano (Narrador decide intencoes de todos) conflita com a UI interativa em tempo real APROVADA no B7. A tatica do NPC fica no motor (NpcCombatBrain, B6) e acao_npc devolve o ESTADO FACTUAL p/ o Narrador narrar, em vez de dirigir o turno. forjar_npc no iniciar_combate (NPC sob medida) e tabelas de saque por criatura ficam p/ enriquecimento futuro (F1); saque do B8 = armas dos derrotados
- FASE B COMPLETA (motor B1-B6 + UI B7 + integracao B8). Pendente so a validacao no aparelho (combate jogavel ponta a ponta com chaves de IA reais)
- Build completo verde 2 variantes
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 408 — 23 de Junho de 2026
**Saga combate: Golpe Rápido (tópicos `[]` 1/N, MB p.370, branch GURPS-Saga)**
- Início da varredura dos tópicos NÃO-FEITOS do Combate.md (autonomia do usuário). **Golpe Rápido**: nova manobra `GOLPE_RAPIDO` — `heroiGolpeRapido(ataque, alvoId, local)` faz **2 ataques corpo-a-corpo** no mesmo turno, cada um com **−6** (`resolverGolpeHeroi(modAdicional=-6)`), **mantendo a defesa ativa** (não é Ataque Total). UI: manobra com seletor de alvo/local; `FichaViewModel.sagaCombateGolpeRapido`.
- Teste: 2 ataques, componente −6, defesa mantida. Build 2 variantes + testes verdes.
- Combate.md: "Golpe Rápido" e "Ataque Enganoso" (marcador corrigido) → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 407 — 23 de Junho de 2026
**Saga combate: Aparar Desarmado vs GdP — FECHA O LOOP DOS 16 PARCIAIS (16/16, MB p.376, branch GURPS-Saga)**
- O −3 ao aparar uma arma com as mãos nuas (391) agora é **dispensado quando o ataque é por ponta (GdP)**, além da exceção Caratê/Judô. GdP é inferido do **dano PERF (perfuração = sempre por ponta)** — dado estruturado (`DanoTipo`), não nome. `opcoesDefesaHeroi(ataqueGdP)`; controller passa `tipoDano(armaTipo) == PERF && !aDistancia`.
- DEFERIDO: lesão no braço que apara ao falhar (sem PV por membro nem escolha de local pelo atacante no modelo).
- Teste: apara desarmada vs corte = −3; vs GdP/PERF = sem −3. Build 2 variantes + testes verdes.
- Combate.md: "Aparar Desarmado" → FEITO.
- **✅ LOOP DOS 16 PARCIAIS COMPLETO (393–407):** Fazer Nada, Deslocamento, Apontar, Ataque Total, Concentrar, Preparar+Armas Preparadas, Aguardar, Movimento, Opções de Ataque CaC, Precisão/Disparo com Mira, Velocidade e Distância, Retirada+Esquiva-e-Queda, Aparar, Quando uma Arma Está Preparada, Aparar Desarmado.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 406 — 23 de Junho de 2026
**Saga combate: Quando uma Arma Está Preparada — cair/atordoar desprepara desbalanceada (loop dos 16 parciais 15/16, MB p.383, branch GURPS-Saga)**
- A seção sobrepõe ao 398 (Armas Desbalanceadas, já feito); o bit NOVO: *"cair, perder o equilíbrio ou ficar atordoado empunhando uma arma que precisa de preparação a deixa despreparada"* (MB p.383). Motor: `marcarArmaDespreparada(rotulo)`; controller: `verificarDesprepararPorEstado` após o turno do NPC — se o herói está ATORDOADO/CAÍDO/DEITADO e empunha uma arma desbalanceada, ela fica despreparada (reusa o bloqueio do 398).
- DEFERIDO: mudar de alcance de arma longa (sem rastreio de alcance atual no modelo) e tempos de guardar/embainhar = narrativo.
- Teste: `marcarArmaDespreparada` bloqueia o ataque até Preparar. Build 2 variantes + testes verdes.
- Combate.md: "Quando uma Arma Está Preparada?" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 405 — 23 de Junho de 2026
**Saga combate: Aparar com a Mão Inábil (loop dos 16 parciais 13/16, MB p.376, branch GURPS-Saga)**
- Núcleo do Aparar já feito (375/389/390). Faltava **Aparar com a Mão Inábil**: variante de defesa Aparar **−2 efetivo**, **anulada por Ambidestria**. `OpcaoDefesa.maoInabil`; `opcoesDefesa(ambidestro)` emite a variante (só sem Ambidestria); `opcoesDefesaHeroi`→controller passa `temAmbidestria`. UI: card "🤚 mão inábil".
- DEFERIDO por falta de dado estruturado: aparar **arremesso** −1/−2 (sem flag thrown-vs-projétil no NpcStats) e **aparar-desarmado→ferir o atacante** (sem flag de arma natural do NPC). Anotado no Combate.md.
- Teste: variante mão inábil = −2; ausente com Ambidestria. Build 2 variantes + testes verdes.
- Combate.md: "Aparar" → FEITO (com deferidos honestos).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 404 — 23 de Junho de 2026
**Saga combate: Retirada e Jogar-se ao Chão — Esquiva e Queda (loop dos 16 parciais 12/16, fecha 2 tópicos, MB p.377, branch GURPS-Saga)**
- A Retirada foi feita no 389; faltava **Jogar-se ao Chão / Esquiva e Queda**: variante de defesa **Esquiva +3 só contra ATAQUE À DISTÂNCIA**, mas o herói **termina deitado**. `OpcaoDefesa.jogarSeAoChao` + `DefesaHeroi.jogarSeAoChao`; `opcoesDefesa(permitirJogarSeAoChao)` emite a variante; `opcoesDefesaHeroi` gateia (vs tiro, não-deitado, não-atordoado); `npcResolve` põe `postura = DEITADO` após defender. UI: card mostra "⤓ jogar-se ao chão".
- Teste: variante +3 vs tiro, ausente vs corpo-a-corpo; defender com ela deixa o herói deitado. Build 2 variantes + testes verdes.
- Combate.md: "Retirada e Jogar-se ao Chão" e "Esquiva e Queda" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 403 — 23 de Junho de 2026
**Saga combate: Velocidade e Distância do Alvo (loop dos 16 parciais 11/16, MB p.550, branch GURPS-Saga)**
- A penalidade de distância já existia; faltava a de **alvo em movimento**: `Combatente.velocidadeAtual` (m percorridos no último Move) é **somado à distância** numa ÚNICA penalidade (`penalidadeDistancia(dist + velocidade)`, MB p.550 — não somar separado). `heroiMove`/MOVER do NPC setam; `inicioAcaoHeroi`/início do `npcResolve` zeram (parado = 0). Vale nas 2 direções (herói atira no NPC; NPC atira no herói).
- Teste: NPC parado = só "distância 5m"; com velocidade = "Vel/Dist". Build 2 variantes + testes verdes.
- Combate.md: "Velocidade e Distância do Alvo" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 402 — 23 de Junho de 2026
**Saga combate: Precisão e Disparo com Mira — teto de pontaria (loop dos 16 parciais 10/16, MB p.364, branch GURPS-Saga)**
- Acc + mira contínua + firmar já existiam; faltava o **teto**: a soma dos bônus de pontaria **não excede 2× a Prec** (MB p.364). Em `resolverGolpeHeroi`, mantém o breakdown (mira (Acc)/mira contínua/firmar) e, se o total passa de `2×Acc`, soma um componente negativo "teto de pontaria (2×Acc)".
- DEFERIDO: miras telescópicas/laser e sistemas de pontaria não estão no catálogo (sem dado de scope).
- Teste: rifle Acc 2 com mira 3 turnos (+2) + firmar (+1) = 5 → teto em 4. Build 2 variantes + testes verdes.
- Combate.md: "Precisão e Disparo com Mira" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 401 — 23 de Junho de 2026
**Saga combate: Opções de Ataque CaC — Ataque Enganoso (loop dos 16 parciais 9/16, MB p.369, branch GURPS-Saga)**
- O cabeçalho "Opções de Ataques CaC" introduz as opções de golpe; faltava a mais usada: **Ataque Enganoso**. `resolverGolpeHeroi(enganoso)` e `heroiAtaca(enganoso)`: cada passo dá **−2 no NH** (componente "ataque enganoso") por **−1 na defesa do alvo** (`defValorFinal − enganoso`). UI: stepper no diálogo de ATAQUE corpo-a-corpo, limitado para o **NH efetivo não cair abaixo de 10** (`maxEnganoso = (nh−10)/2`, teto 4).
- DEFERIDO (tópicos próprios `[]`): Golpe Rápido (2 ataques a −6) e Visar a Arma do Oponente.
- Teste: o golpe registra o componente "ataque enganoso". Build 2 variantes + testes verdes.
- Combate.md: "Opções de Ataques com Armas de Combate Corpo a Corpo" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 400 — 23 de Junho de 2026
**Saga combate: Movimento — postura reduz o Deslocamento (loop dos 16 parciais 8/16, MB p.368, branch GURPS-Saga)**
- O Deslocamento por manobra já existia; faltava a **redução por postura**: `Combatente.deslocamentoEfetivo` agora aplica em pé/agachado = cheio, **ajoelhado/rastejando = 1/3**, **deitado = 1**, **sentado = 0**; depois o cambaleante corta pela metade (MB p.380). Vale p/ herói e NPC (inimigo derrubado quase não se move).
- Terreno difícil/obstáculos = Narrador. Teste: deslocamento por postura (6→2→2→1→0). Build 2 variantes + testes verdes.
- Combate.md: "Movimento" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 399 — 23 de Junho de 2026
**Saga combate: Aguardar — Interromper Investida (loop dos 16 parciais 7/16, MB p.392, branch GURPS-Saga)**
- O "aguardar por gatilho arbitrário" é narrativo; o núcleo de combate é **Interromper Investida**: `heroiAguardar(ataque)` firma uma arma **perfurante (PERF) corpo-a-corpo** → `aguardarInvestidaArma`. No `npcResolve`, se o NPC **avança** (MOVER sem recuar / MOVER_E_ATACAR), o herói **golpeia primeiro** com a arma firmada, **+1 de dano por 2m percorridos** (`bonusInvestidaPendente` somado em `resolverGolpeHeroi`). Sem arma perfurante = Aguardar genérico (narrativo, sem bônus).
- Manobra AGUARDAR roteada para `heroiAguardar`; já estava em `manobrasLegais`. O herói ainda pode defender enquanto aguarda.
- DEFERIDO: gatilhos arbitrários (segurar refém, coordenar com aliados, disparo de oportunidade) = Narrador.
- Testes: investida é interrompida com bônus; arma não-perfurante = aguardar genérico. Build 2 variantes + testes verdes.
- Combate.md: "Aguardar" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 398 — 23 de Junho de 2026
**Saga combate: Armas Preparadas / Preparar — desbalanceada despreparada (loop dos 16 parciais 6/16, fecha 2 parciais, MB p.270/366, branch GURPS-Saga)**
- REGRA (MB p.270): arma desbalanceada ('D') fica **DESPREPARADA após cada ataque** a menos que **ST ≥ 1,5× a ST mínima** da arma; re-preparar = manobra Preparar. `AtaqueHeroi.stMinimo` (← `armaStMinimo` do catálogo); `marcarDespreparoSeNecessario` nos 3 caminhos de ataque corpo-a-corpo; `armaDespreparadaRotulo` (persiste entre turnos, identifica a arma pelo rótulo).
- BLOQUEIO: `heroiAtaca` recusa se a arma está despreparada; o controller (`armaDespreparadaBloqueia`) avisa **sem gastar o turno** (heroiAtaca/MoverEAtacar/AtaqueDuplo). `heroiManobra(PREPARAR)` e `sacarArma` re-empunham (`prepararArmaEmpunhada`).
- DEFERIDO: Martial Arts distingue 'D' (desbalanceada) de '‡' (despreparo) — o Básico (fonte do projeto) trata juntos; recarregar = sem sistema de munição (por decisão do Lote 366); abrir porta/ativar vantagem = Narrador.
- Testes: desbalanceada com ST baixa fica despreparada e bloqueia o 2º ataque; Preparar re-empunha; ST ≥ 1,5× não desprepara. Build 2 variantes + testes verdes.
- Combate.md: "Preparar" e "Armas Preparadas" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 397 — 23 de Junho de 2026
**Saga combate: Concentrar — Vontade-3 ao ser perturbado (loop dos 16 parciais 5/16, MB p.344, branch GURPS-Saga)**
- O EFEITO da concentração (magia/psi/perícia IQ) é do Narrador; o motor de combate modela a **mecânica de interrupção**: `heroiManobra(CONCENTRAR)` marca `concentrando`; em `npcResolve`, se o herói é **forçado a defender** (`defesaTentada`) ou **ferido**, testa **Vontade-3** (`heroiPerfil.vontade - 3`); falha → perde a concentração (recomeça). Vale só no turno (re-declara p/ continuar).
- Teste: ser perturbado durante a concentração dispara o teste de Vontade-3 (loop de seeds até um acerto). Build 2 variantes + testes verdes.
- Combate.md: "Concentrar" → FEITO (mecânica de combate; efeito = Narrador).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 396 — 23 de Junho de 2026
**Saga combate: Ataque Total (Fogo de Retenção) — fecha o Ataque Total (loop dos 16 parciais 4/16, MB p.409, branch GURPS-Saga)**
- REGRA (MB p.409): arma à distância CdT 5+ cobre uma área e **acerta quem ENTRAR** antes do próximo turno (negação de área/interrupção). Mapeado ao tracker de faixas: nova manobra `FOGO_RETENCAO` (`heroiFogoRetencao(ataque)`, exige `aDistancia` + `cadenciaTiro≥5`); marca `fogoRetencaoArma` + `heroiSemDefesaAtiva` (é Ataque Total); dura até a próxima ação (limpo em `inicioAcaoHeroi`).
- INTERRUPÇÃO: no `npcResolve`, se a zona está coberta e o NPC **avança** (MOVER sem recuar / MOVER_E_ATACAR), o herói dispara uma rajada nele (reusa `resolverGolpeHeroi` → RoF/Recuo/distância), ANTES de o NPC agir; se morre, sai.
- UI: manobra "Fogo de Retenção" aparece quando a arma empunhada é à distância CdT 5+ (sem precisar de alvo — é área).
- DEFERIDO: múltiplas zonas (CdT 10+), escolha de nº de tiros por zona, "margem de 1m da linha" — abstraídos no modelo de faixas.
- Testes: NPC que avança é alvejado; CdT < 5 é recusado. Build 2 variantes + testes verdes.
- Combate.md: "Ataque Total" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 395 — 23 de Junho de 2026
**Saga combate: Apontar completo — firmar +1 + Vontade ao ser ferido (loop dos 16 parciais 3/16, MB p.364, branch GURPS-Saga)**
- **FIRMAR (+1 Acc):** `heroiApontar(alvoId, firmado)` + `apontarFirmado`; o tiro soma +1 só se a arma é de fogo (`AtaqueHeroi.armaDeFogo`, setado por `armaTipoCombate` do catálogo — estruturado). UI: `SubDialogoApontar` com Switch "Firmar a arma (+1 Prec.)" mostrado só para arma de fogo.
- **VONTADE AO SER FERIDO:** `HeroiPerfilCombate.vontade` (← `p.vontade`); se o herói é ferido **ainda mirando** (sem usar defesa — caso de defesa anulada por crítico), testa Vontade; falha → perde a mira. (Defender já perdia a mira no Lote 392.)
- DEFERIDO: besta também "firma" (sem flag de besta no catálogo); apoio físico (mureta/tripé) e bruços não são modelados — o "firmar" é a declaração do jogador.
- Testes: firmar soma +1 no tiro; ferimento mirando dispara o teste de Vontade (loop de seeds até um crítico). Build 2 variantes + testes verdes.
- Combate.md: "Apontar" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 394 — 23 de Junho de 2026
**Saga combate: Deslocamento — Disparada (loop dos 16 parciais 2/16, MB p.353, branch GURPS-Saga)**
- A manobra Mover já funcionava; faltava a **Disparada** (sprint): Moves consecutivos **na mesma direção (linha reta)** dão **+20% de Deslocamento a partir do 2º** (MB p.353). `heroiMoveSeguidos`+`heroiMoveDirecao` (capturados antes de `inicioAcaoHeroi`, que zera o contador → ação não-Move quebra; mudar de direção recomeça); `heroiMove` aplica o sprint e narra "(disparada +Nm)".
- Veículo/montaria (Combate Montado p.396 / Veículos p.462) = capítulo à parte, fora do escopo do combate Saga a pé.
- Teste: 1º Move sem disparada; 2º consecutivo +1m (desloc 6); ação não-Move reinicia. Build 2 variantes + testes verdes.
- Combate.md: "Deslocamento" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 393 — 23 de Junho de 2026
**Saga combate: Fazer Nada / Atordoado — defesas −4 (loop dos 16 parciais 1/16, MB p.364, branch GURPS-Saga)**
- A manobra forçada ao atordoado e a recuperação (HT/IQ em `avancarTurno`) JÁ existiam. Faltava o **−4 em TODAS as defesas ativas enquanto atordoado** (MB p.364). `opcoesDefesaHeroi` aplica `penAtordoado` (herói); `esquivaNpc`/`melhorDefesaNpc` usam `penDefesaAtordoado` (NPC).
- Teste: esquiva e apara caem −4 com `Condicao.ATORDOADO`. Build 2 variantes + testes verdes.
- Combate.md: "Fazer Nada" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 392 — 23 de Junho de 2026
**Saga combate: Apontar — mira de vários turnos + perde a mira ao defender (loop de defesa 5/5 — FECHA O LOOP, MB p.364, branch GURPS-Saga)**
- MIRA DE VÁRIOS TURNOS (MB p.364): mirar o MESMO alvo por segundos seguidos acumula **+1 (2º seg) / +2 (3º+)** ALÉM da Precisão (Acc). `apontarStacks` (espelha o Avaliar); bônus = `(stacks−1).coerceIn(0,2)` aplicado no acerto à distância.
- PERDER A MIRA: usar uma **defesa ativa** zera a pontaria (`limparApontar` quando `troca.defesaTentada`); o log avisa. (Defender entre os turnos = perde o Acc no tiro seguinte, como manda a regra.)
- DEFERIDO (registrado): **firmar** a arma (+1 Acc) e o **teste de Vontade** para não perder a mira ao ser ferido (não modelados).
- Testes: stacking +1→+2→teto; defender perde a mira (loop de seeds com acerto). Build 2 variantes + testes verdes.
- Combate.md: "Apontar" segue PARCIAL (núcleo da mira feito; firmar/Vontade deferidos).
- **✅ LOOP DE REFINO DE DEFESA 388–392 COMPLETO** (Defesa Total Aumentada+Dupla, Retirada, Aparar à queima-roupa, Aparar Desarmado −3, Apontar multi-turno).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 391 — 23 de Junho de 2026
**Saga combate: Aparar Desarmado — −3 vs armas (loop de defesa 4/5, MB p.376, branch GURPS-Saga)**
- REGRA (MB p.376): aparar uma ARMA com as mãos nuas sofre **−3**, salvo se o herói usa **Caratê ou Judô** (valor cheio). `opcoesDefesaHeroi(ataqueComArma)` aplica `penAparaDesarmada = 3` quando a "arma" empunhada é desarmada (`armaPronta.desarmado`), o NPC ataca com arma e a perícia não é marcial.
- `AtaqueHeroi.aparaMarcial` (novo) = true quando a melhor perícia de luta do herói é Caratê/Judô — detectado por `definicaoId` estruturado (set `MARCIAIS_APARA`, não por nome livre). Controller passa `ataqueComArma = npc.armaNome.isNotBlank()`.
- DEFERIDO (registrado): a exceção **GdP** (o motor não distingue GdP/GeB no ataque do NPC) e a **lesão no braço que apara** ao falhar.
- Testes: mãos nuas vs arma = −3; vs ataque desarmado = sem penalidade; Caratê/Judô = valor cheio. Build 2 variantes + testes verdes.
- Combate.md: "Aparar Desarmado" segue PARCIAL (com o avanço anotado).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 390 — 23 de Junho de 2026
**Saga combate: Aparar — só à queima-roupa contra tiro (loop de defesa 3/5, MB p.376, branch GURPS-Saga)**
- BUG: o card oferecia **Aparar contra um atirador distante**. A regra (MB p.376): só se apara um ataque à distância se o atacante estiver **adjacente (≤1m)** — apara-se a ARMA, não o projétil. `opcoesDefesaHeroi(atacanteAdjacente)` gateia o `podeAparar`; o controller passa `s.distancia(npc) <= 1`. Bloqueio (escudo) continua valendo contra tiro (a regra do escudo não muda).
- Narração: ao aparar um tiro à queima-roupa, o log explica "você desvia a arma do atirador (não o projétil)".
- Testes: corpo-a-corpo e tiro a 1m oferecem aparar; tiro de longe não. Build 2 variantes + testes verdes.
- Combate.md: "Aparar" segue PARCIAL — falta mão inábil −4, arremesso −1/−2, aparar-desarmado→ferir o atacante.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 389 — 22 de Junho de 2026
**Saga combate: Retirada / Opções de Defesa Ativa (loop de defesa 2/5, MB p.377, branch GURPS-Saga)**
- A Retirada (recuar) existia no motor mas **não era oferecida** no card (o param `recuo` nunca era ligado). Agora `opcoesDefesaHeroi(contraAtaqueCorpoACorpo)` calcula `permitirRecuo` e `CombatResolver.opcoesDefesa` emite **variantes "com recuo"** de cada defesa (Esquiva +3, Aparar/Bloquear +1).
- EXCEÇÃO MARCIAL (MB p.377): aparar com **esgrima** ao recuar dá **+3** (não +1) — `valorDefesaFinal` usa o flag `esgrima`. Boxe/Caratê/Judô (+3 desarmado) ficam de fora por ora (sem flag de perícia de luta).
- RESTRIÇÕES: só **contra ataque corpo-a-corpo**; **1×/turno** (reusa `DefesasUsadas.retracaoUsada`, marcado em `npcResolve` quando `DefesaHeroi.recuo`); bloqueado se **atordoado**. (Postura sentado/ajoelhado e o passo físico p/ trás = simplificação registrada — o herói está sempre engajado no tracker.)
- UI: as variantes aparecem no card "Defenda-se!" com sufixo "↩ recuar" + componente "+N recuo"; a Dupla (388) ignora variantes com recuo na 2ª defesa.
- Testes: esgrima+recuo=+3, variantes emitidas só com `permitirRecuo`, recuo só corpo-a-corpo e 1×/turno. Build 2 variantes + testes verdes.
- Combate.md: "Opções de Defesa Ativa" → FEITO (Retirada). "Retirada e Jogar-se ao Chão" segue parcial (falta Esquiva-e-Queda).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 388 — 22 de Junho de 2026
**Saga combate: Defesa Total completa — Aumentada + Dupla (loop de defesa 1/5, MB p.366, branch GURPS-Saga)**
- A manobra Defesa Total do herói **não fazia nada** (só logava). Agora `CombatSession.heroiDefesaTotal(modo, aumentadaEm)` com `enum DefesaTotalModo { AUMENTADA, DUPLA }`; o benefício vale até a PRÓXIMA ação do herói (limpo em `inicioAcaoHeroi`).
- **AUMENTADA:** +2 numa defesa escolhida — `opcoesDefesaHeroi` passa `defesaTotalEm = defesaTotalAumentadaEm` ao `CombatResolver` (reusa `BONUS_DEFESA_TOTAL` já existente).
- **DUPLA:** se a 1ª defesa falha (e o ataque NÃO foi anulado por golpe decisivo), tenta automaticamente uma 2ª defesa de TIPO diferente. Como `resolverTroca` MUTA o defensor (aplica dano), decido o resultado ANTES via `CombatResolver.defesaBemSucedida` (puro): o controller prepara a melhor 2ª defesa (`opcoes.filter{tipo≠1ª}.maxBy{valorFinal}`) e passa em `npcResolve(..., defesaSecundaria)`; o motor troca `def` pela 2ª só se a 1ª falhou.
- UI: manobra "Defesa Total" abre `SubDialogoDefesaTotal` (Aumentada [+ qual defesa] / Dupla); `FichaViewModel.sagaCombateDefesaTotal`; wrapper no `SagaCombatController`.
- Testes (`CombatSessionTest`): Aumentada soma +2 só na defesa escolhida; Dupla salva o herói quando a 1ª falha (loop de seeds, acerto não-crítico). Build 2 variantes + testes verdes.
- Combate.md: "Defesa Total" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 387 — 22 de Junho de 2026
**Saga combate: refino do Ataque Total — Forte correto (MB p.365, branch GURPS-Saga)**
- BUG DE REGRA corrigido: o Ataque Total **Forte** dava **+2 fixo** de dano. A regra (MB p.365) é **+2 OU +1 por dado, o que for maior** — então armas de vários dados eram subestimadas (uma 3d devia dar +3). `CombatSession.bonusDanoForte(manobra, modo, danoExpr, aDistancia)` virou função pura (companion, testável) = `max(2, nº de dados)`.
- RESTRIÇÃO: Forte só vale **corpo-a-corpo** (à distância não tem Forte; MB p.365). Gateado por `aDistancia` no herói e no NPC. (Espada de energia/queimadura ficaria de fora pela regra, mas o motor só modela dano por ST de GdP/GeB — todo corpo-a-corpo aqui é elegível; nota para quando houver dano de queimadura.)
- UI: a opção "Forte" some quando a arma empunhada é à distância; o rótulo do **Determinado** mostra **+1** à distância (era sempre "+4", errado para tiro).
- Testes (`CombatSessionTest`): 1d→+2, 2d→+2, 3d→+3, 4d→+4, à distância→0, manobra/modo diferentes→0. Build 2 variantes + testes verdes.
- Combate.md: "Ataque Total" segue PARCIAL (Determinado/Forte/Duplo/Fintar OK; falta **Fogo de Retenção**, CdT 5+, MB p.409).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 386 — 22 de Junho de 2026
**Saga combate: Luta agarrada — base (loop de regras 5/5, MB p.370–371, branch GURPS-Saga)**
- AGARRAR (MB p.370): `CombatSession.heroiAgarrar(ataque, alvoId)` — ataque normal; se acerta e o alvo não defende, adiciona `Condicao.AGARRADO`. O alvo agarrado defende a −4 (`penAgarrado` em `resolverGolpeHeroi`) e, no turno dele, gasta a ação tentando se desvencilhar (Disputa Rápida em `npcResolve`, não ataca).
- DERRUBAR (MB p.370–371): `heroiDerrubar(alvoId)` — Disputa Rápida do maior entre ST/DX; vencendo, o alvo vai a CAÍDO/DEITADO. Helper puro `vencaDisputaRapida(valorA, rolA, valorB, rolB)` (empate de margem favorece o defensor, MB p.348).
- UI: manobras Agarrar/Derrubar habilitadas quando há alvo corpo-a-corpo (sem alvo à distância) → seletor de alvo; `FichaViewModel.sagaCombateAgarrar/Derrubar`; wrappers no `SagaCombatController`.
- Testes (`CombatSessionTest`): `vencaDisputaRapida` segue a regra; agarrar deixa o NPC AGARRADO; NPC agarrado gasta o turno se soltando (não ataca); derrubar joga o alvo no chão. Build 2 variantes + testes verdes.
- Combate.md: "Agarrar" e "Derrubar" → FEITO (base). Sub-sistema completo (Imobilizar/Estrangular/Mata-Leão/Chave de Braço/Encontrão/Empurrão) fica para lotes futuros.
- NOTA (git): o código de domínio (CombatSession.kt/CombatModels.kt) foi varrido por engano para o commit `02e4567` (sessão paralela dados-3D, `git add -A`); este commit fecha o Lote 386 com a fiação de UI + testes + docs sob a mensagem correta. ⚠️ Colisão de numeração: a série dados-3D também tem um "Lote 386" (correção de colisão física dos dados) — são trabalhos distintos.
- LOOP DE REGRAS DE COMBATE 5/5 COMPLETO (382–386). Próximo: validação no aparelho (tudo de uma vez) + resto do sub-sistema de luta agarrada.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 385 — 17 de Junho de 2026
**Saga combate: Tolerância a Ferimentos (loop de regras 4/5, MB p.381, branch GURPS-Saga)**
- REGRA (MB p.381): mortos-vivos/máquinas/objetos/enxames são menos vulneráveis a pi/perf. Novo `enum ToleranciaFerimentos { NORMAL, NAO_VIVO, HOMOGENEO, DIFUSO }`. `HitLocationRules.multiplicador(tipo, local, tolerancia)` sobrescreve o multiplicador de pi/perf (NÃO-VIVO: perf/pi++ ×1, pi+ ×½, pi ×⅓, pi- ×⅕; HOMOGÊNEO mais ainda) e remove o bônus de crânio/vitais (sem órgãos); DIFUSO = teto no dano final (pi/perf ≤1 PV, resto ≤2). `aplicarDano` aplica.
- CAMINHO DE DADO: `NpcStats.tolerancia` ← `BestiarioCriatura.tolerancia` (string do JSON → enum). `CombatResolver.resolverTroca` repassa ao `aplicarDano`; chamadores (`resolverGolpeHeroi`, rajada, `aplicarDanoCombatente`) passam a tolerância do alvo. Herói = NORMAL.
- DADO DO BESTIÁRIO: **esqueleto e zumbi** marcados `"tolerancia": "nao_vivo"` (canônico: mortos-vivos corpóreos = Unliving) — agora resistem a tiros (pi ×⅓), como o exemplo do próprio MB p.381.
- Testes (`HitLocationRulesTest`): Não-Vivo reduz pi (9→3) e não dá bônus de vitais; perf segue ×1; Homogêneo (pi ×0.2); Difuso (teto 1/2). Build 2 variantes + lint verde.
- Combate.md: "Lesões em Alvos Difusos, Homogêneos e Não-Vivos" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 384 — 17 de Junho de 2026
**Saga combate: Tabelas de Golpe Fulminante / Erro Crítico (loop de regras 3/5, MB p.557–558, branch GURPS-Saga)**
- GOLPE FULMINANTE (MB p.558): a defesa já é anulada pelo crítico; a tabela (3d6) modifica o DANO. `CriticoRules.golpeFulminante(soma)` → DOBRO/TRIPLO/MÁXIMO/RD_METADE/FERIMENTO_GRAVE/NORMAL. Aplicado em `resolverGolpeHeroi` (herói→NPC) e `npcResolve` (NPC→herói) via `aplicarGolpeFulminante` (+ `CombatSession.danoMaximo`; `forcarFerimentoGrave` threadeado por `resolverTroca`→`ferir`→`aplicarGolpe`).
- ERRO CRÍTICO (MB p.557): `CriticoRules.erroCritico(soma, desarmado)` → efeito no ATACANTE. O motor aplica os mecânicos (ACERTA_A_SI[_METADE] = dano em si; CAI = derrubado/deitado) e NARRA o resto (QUEBRA_ARMA/LARGA_ARMA/DESEQUILIBRIO — não rastreamos durabilidade/empunhadura de arma). `AtaqueHeroi.desarmado` escolhe a tabela armada/desarmada.
- WORKAROUND DE BUILD: 3 detectores do compose-runtime lint (`NullSafeMutableLiveData`, `FrequentlyChangingValue`, `RememberInComposition`) crashavam com `IncompatibleClassChangeError` ("Found class KaSimpleVariableAccessCall, but interface was expected" — Kotlin Analysis API × versão do lint) ao re-executar o lint, derrubando o build (os lotes anteriores passavam só porque o lint estava em cache). Desligados em `app/build.gradle.kts` (mesmo padrão dos 2 já existentes; nosso código não usa Compose neles).
- Testes: `golpeFulminante`/`erroCritico` (mapeamento das tabelas), `danoMaximo`, e integração (Golpe Fulminante com NH alto, Erro crítico com NH baixo). Build 2 variantes + lint verde.
- Combate.md: "Golpes Fulminantes e Erros Críticos", "Golpes Fulminantes", "Erros Críticos" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 383 — 17 de Junho de 2026
**Saga combate: Fintar (loop de regras 2/5, MB p.366, branch GURPS-Saga)**
- MANOBRA FINTAR: `CombatSession.heroiFintar(ataque, alvoId)` — Disputa Rápida entre o NH do herói com a arma e a defesa do alvo (maior entre `armaNh` e DX do NPC). Helper puro `fintaResultado(nhAtk, rolAtk, nhDef, rolDef)`: 0 se o fintador falha; margem do atacante se o defensor falha; margem de vitória se ambos passam.
- EFEITO: se vence, `fintaAlvoId`/`fintaPenalidade` reduzem a defesa do alvo no PRÓXIMO golpe corpo-a-corpo (em `resolverGolpeHeroi`, `defValorFinal`); aplica também aos dois golpes do Ataque Total (Duplo) (MB p.366). Exige arma corpo-a-corpo no alcance. `limparFinta` espelha avaliar/apontar (consumido no ataque; descartado em mover/manobra/outra prep).
- UI: manobra Fintar (quando há arma corpo-a-corpo + alvo ao alcance) → seletor de alvo; `FichaViewModel.sagaCombateFintar`.
- Testes: `fintaResultado` (4 casos), finta bem-sucedida abate a defesa no golpe seguinte, finta bloqueada com arma à distância. Build 2 variantes + lint verde.
- Combate.md: "Fintar" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 382 — 17 de Junho de 2026
**Saga combate: Choque + Cambaleante (loop de regras 1/5, MB p.419/380, branch GURPS-Saga)**
- CHOQUE (MB p.419/381): toda perda de PV gera choque — penalidade em DX/IQ (acerto) no PRÓXIMO turno. `InjuryRules.penalidadeChoque(pvPerdidos, pvMax)`: −1/PV; se PV Inicial ≥20, −1 a cada PVInicial/10; teto −4. NÃO afeta defesas (MB p.375). `Combatente.choquePendente` acumula em `ferir`; aplicado ao acerto do herói (`resolverGolpeHeroi`) e do NPC (`npcResolve`); expira em `avancarTurno` (fim do turno de quem agiu).
- CAMBALEANTE (MB p.380): com < 1/3 do PV Inicial, Vel.Básica/Deslocamento e Esquiva caem à metade. `Combatente.cambaleante`/`deslocamentoEfetivo`; Esquiva do herói (`opcoesDefesaHeroi`) e do NPC (`esquivaNpc`) reduzidas; Deslocamento à metade em `heroiMove`/`heroiMoverEAtacar`/NPC/controller.
- Testes: penalidadeChoque (PV<20 e ≥20 com teto); choque aplicado ao golpe + expira; ferir acumula choque; cambaleante reduz Esquiva e Deslocamento. Build 2 variantes + lint verde.
- Combate.md: "Efeitos de Lesões" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 381 — 17 de Junho de 2026
**Saga combate: Modificador de Tamanho (MT) do alvo no acerto à distância (MB p.549, branch GURPS-Saga)**
- REGRA (MB p.549, lida no `chunks.jsonl`): no ataque À DISTÂNCIA soma-se o **MT do alvo** ao NH (alvo grande = mais fácil; pequeno = mais difícil). A lista de modificadores corpo-a-corpo (p.548) **não** inclui MT → MT é só à distância.
- DAS DUAS DIREÇÕES: herói atira no NPC → soma o MT do NPC (`alvo.stats.modificadorTamanho`) em `resolverGolpeHeroi`; NPC atira no herói → soma o MT do herói (`heroiPerfil.modificadorTamanho`, da ficha `p.modificadorTamanho`) em `npcResolve`. Aparece no colchete técnico como "tamanho do alvo (MT)".
- CAMINHO DE DADO: `NpcStats.modificadorTamanho` (novo) ← `BestiarioCriatura.mt` (lido do JSON, default 0) em `novoCombatente`; `HeroiPerfilCombate.modificadorTamanho` ← `construirPerfilHeroi`.
- Testes (`CombatSessionTest`): herói→Ogro(MT+2) soma; corpo-a-corpo não soma; NPC→herói(MT+1) soma. Build 2 variantes + lint verde.
- ⚠️ PENDÊNCIA DE DADO (não chutar): as 17 criaturas do bestiário estão com MT 0 (o JSON não tinha o campo). A regra só muda números quando o MT for preenchido nas criaturas grandes/pequenas (Ogro/Urso-pardo/Lobo Atroz etc.), conferindo valores no Bestiário/Módulo Básico.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 380 — 17 de Junho de 2026
**Saga combate: quando o BD do escudo conta na defesa (MB p.375, branch GURPS-Saga)**
- REGRA (MB p.375, lida no `chunks.jsonl`): o BD do escudo só vale com o escudo **PREPARADO** (mão livre) e **NÃO contra armas de fogo** (vale contra corpo-a-corpo, arremesso e arcos/bestas). Direção (frente/lado) não é modelada — simplificação.
- O BD agora é guardado à parte em `HeroiPerfilCombate.bonusEscudo` (vem de `getBonusEscudo`, já embutido em esquiva/apara/bloqueio) e **removido** em `opcoesDefesaHeroi` quando: (a) a arma pronta do herói é de **duas mãos** (sem mão livre p/ o escudo) ou (b) o ataque é de **arma de fogo**.
- DETECÇÃO ORIENTADA A DADO (correção do viés apontado pelo usuário — não usar nome): "duas mãos" vem do **grupo do catálogo** via novo `ArmaCatalogoItem.duasMaosPorGrupo` (fogo: só "pistola" é 1 mão; arco/besta = 2). Aplicado no loader de fogo/distância (`CatalogLoaders`) e no combate (`ehDuasMaos`). Arma de fogo do NPC: flag `NpcStats.armaDeFogo` + `CombatSession.pareceArmaDeFogo(nome)` — heurística por nome só aqui porque NPC é texto livre (sem catálogo).
- Testes: `duasMaosPorGrupo` (Pistola/Feixe(Pistola)=1 mão; Rifle/Mosquete/Espingarda/Arco/Besta=2), `pareceArmaDeFogo`, e remoção do BD da Esquiva (1 mão = +BD; vs fogo ou 2 mãos = sai o BD). Build 2 variantes + lint verde.
- LIMITAÇÃO honesta: fichas ANTIGAS cujas armas não têm `grupo`/`armaDuasMaos` não detectam "2 mãos" — re-adicionar a arma do catálogo resolve (dado correto). Não foi feito hack por nome p/ contornar isso.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 379 — 17 de Junho de 2026
**Ficha: BD de escudo/capa só com escolha explícita (validação no aparelho, branch GURPS-Saga)**
- BUG (reportado no aparelho): o Bônus de Defesa (BD) do escudo/capa aparecia em Esquiva/Apara **só por ter o equipamento na lista** — confuso. Causa: `Personagem.DefesasAtivas.getBonusEscudo` tinha um fallback `?: escudos.maxByOrNull { bonusDefesa }` que pegava o melhor escudo quando nenhum estava selecionado.
- CORREÇÃO: `getBonusEscudo` agora retorna BD só quando há **escudo explicitamente selecionado** (`escudoSelecionadoNome`, setado no diálogo de Bloqueio). Sem seleção → BD 0 em todas as defesas. Ao selecionar, o BD soma em Esquiva/Apara/Bloqueio (correto: GURPS MB p.375, escudo pronto dá DB em todas as defesas ativas). Afeta também o perfil de combate da Saga (usa as mesmas defesas).
- NÃO mexido (convenção mantida): `FichaCombatDelegate.ajustarEscudoAutomatico` ainda auto-seleciona o melhor escudo **quando o personagem tem perícia de Escudo** (não dispara só por ter o equipamento). Pode virar manual-sempre num lote futuro se o usuário pedir.
- Teste atualizado (`PersonagemRulesTest`): o que codificava o fallback agora valida "sem seleção = BD 0; com seleção = BD soma". Build 2 variantes + lint verde.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 378 — 17 de Junho de 2026
**Saga combate: bugfix de jogabilidade da validação no aparelho (branch GURPS-Saga)**
- CONTEXTO: 1º teste de combate no aparelho (personagem pistoleiro). Antes, fiz a **auditoria do capítulo de combate (MB 363–384, lido linha a linha no `chunks.jsonl`)** vs o app — registrada em memória; diretriz do usuário: **concluir TODAS as regras de combate antes da Fase C**. Este lote ataca os bugs que travavam o jogo.
- BUG 1 — PERÍCIA DA ARMA (armas de fogo): `SagaCombatController.acharPericiaDaArma` reescrito. Agora casa grupo/nome da arma contra **nome + ESPECIALIZAÇÃO + id** da perícia (a perícia de fogo é "Armas de Fogo/NT" com "Pistola"/"Rifle" guardado à parte → só comparar o nome falhava). Fallback por **família** quando a ficha vem sem o grupo da arma (criada pela IA): tipo de combate → "Armas de Fogo"/arco/besta/arremesso, preferindo a especialização que casa, senão a perícia de maior NH.
- BUG 2 — MOVER E ATACAR não abria o card / virava só narração: o roteamento (`CombatUi.ManeuverCards`) só tratava ATAQUE/ATAQUE_TOTAL como ataque. Agora MOVER_E_ATACAR abre o `SubDialogoAlvoLocal` e **funciona de fato**: novo `CombatSession.heroiMoverEAtacar` aproxima-se do alvo (gastando até o Deslocamento) e golpeia com a penalidade do motor (CaC −4 e teto NH 9; à distância −2/Bulk). Regra fina (MB p.367): na defesa seguinte **só Esquiva/Bloqueio — sem aparar** (flag `heroiSemAparar`). Lista de alvos = quem está a até *reach + Deslocamento* (`CombatUiState.alvosMoverEAtacar`). Delegate `FichaViewModel.sagaCombateMoverEAtacar`.
- BUG 5 — SACAR confuso: botão renomeado para **"Trocar arma"** (toggle Trocar/Fechar) + cabeçalho "Toque numa arma para empunhá-la" no painel.
- BUG 6 — DANO com tipo duplicado ("2d-1 pa pi"): novo `CombatSession.semTokenTipo` remove o token de tipo da expressão (o tipo já é mostrado à parte); `construirAtaques` calcula o tipo a partir do bruto e guarda a expressão limpa.
- BUG 3 — MOVER "sem direção/metros": o diálogo `SubDialogoMover` (direção + alvo + metros) JÁ existe e está correto; era build antigo no aparelho — reconferir no rebuild.
- NÃO são bugs (esclarecido ao usuário): dano e dano localizado **funcionam** (print "3 pen ×1.0 = 3"; rosto+pi = ×1.0 é o multiplicador correto); Avaliar é só corpo-a-corpo (pistoleiro à distância não recebe = regra).
- Testes (`CombatSessionTest`): `semTokenTipo`; Mover-e-Atacar corpo-a-corpo (aproxima até o alcance, golpeia, não apara depois, restaura no turno seguinte); Mover-e-Atacar à distância (aplica a penalidade). Build 2 variantes + lint verde.
- PRÓXIMO da auditoria: "números que faltam" (Modificador de Tamanho no acerto + Bônus de Defesa de escudo nas defesas).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 377 — 16 de Junho de 2026
**Saga combate: dual-wield (Ataque Total Duplo) + "sem defesa ativa" após Ataque Total (branch GURPS-Saga)**
- DUAL-WIELD / ATAQUE TOTAL (DUPLO) (MB p.366, lido no `chunks.jsonl`): novo `CombatSession.heroiAtaqueDuplo(principal, secundaria, alvoId, local, ambidestria)` — DOIS golpes no MESMO alvo; o 1º com a mão hábil (NH normal), o 2º com a arma na mão inábil sofrendo **−4 salvo Ambidestria** (pág. 38). Avaliar/Mira valem só no 1º golpe; se o alvo cai no 1º, o 2º não é desferido.
- REFATORAÇÃO SEGURA: o corpo do golpe único saiu de `heroiAtaca` para os helpers privados `resolverGolpeHeroi(...)` (resolução pura, com `modAdicional` nomeado p/ a mão inábil) e `golpeForaDeAlcance(...)`; `heroiAtaca` e `heroiAtaqueDuplo` reaproveitam ambos (comportamento do ataque simples inalterado — testes antigos verdes).
- SEM DEFESA ATIVA APÓS ATAQUE TOTAL (MB p.366) — lacuna pré-existente fechada p/ TODOS os modos (Determinado/Forte/Duplo): flag `heroiSemDefesaAtiva` (ligada ao fim de um Ataque Total, zerada no início da próxima ação do herói); `opcoesDefesaHeroi` devolve vazio; `npcResolve` passa `surpresa=true` (anula a defesa) + log "🛡️ sem defesa ativa…"; o controller pula o card "Defenda-se!" e resolve direto.
- AMBIDESTRIA: detectada por id `ambidestria` (vantagens pessoais + raciais, mesmo padrão do `SentidoRules`); exposta em `CombatUiState.heroiAmbidestro` e usada para zerar o −4.
- UI (`CombatUi.kt`): `SubDialogoAlvoLocal` ganha o modo **Duplo** (quando há ≥1 arma além da empunhada) + seletor da 2ª arma + "notinha" do −4/Ambidestria; botão vira "Atacar (Duplo)". `FichaViewModel.sagaCombateAtacarDuplo` → `controller.heroiAtaqueDuplo`. Tudo com `contentDescription` (acessível nas 2 variantes, sem arquivo PraCego extra).
- Testes (`CombatSessionTest`): 2 golpes resolvidos + −4 da mão inábil no colchete técnico; Ambidestria zera a penalidade; sem defesa ativa após Ataque Total (e restauração no turno seguinte). Build 2 variantes verde.
- FECHA o "polir combate" (RoF/Recuo no 376 + dual-wield aqui). PRÓXIMO: **validação no aparelho** (combate ponta a ponta + sentidos), depois Fases C/D/E do plano.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 376 — 14 de Junho de 2026
**Saga combate à distância: rajada (CdT/Recuo) + Ataque Total à distância +1 (branch GURPS-Saga)**
- RAJADA (MB p.374): `AtaqueHeroi` ganhou `cadenciaTiro`/`recuo` (do `armaCadenciaTiro`/`armaRecuo`). `CombatSession`: `bonusCadenciaTiro(tiros)` (2-4=+0, 5-8=+1, 9-12=+2, 13-16=+3, 17-24=+4, 25-49=+5, 50-99=+6, +) somado ao acerto à distância (dispara a rajada cheia = CdT); `acertosDaRajada(margem, recuo, tiros)` = 1 + ⌊margem/Recuo⌋, limitado aos tiros. Em `heroiAtaca`: o 1º tiro é resolvido por `resolverTroca`; tiros EXTRAS (Recuo) aplicam dano adicional em loop (HitLocationRules+ferir), com log "rajada: +N projéteis".
- ATAQUE TOTAL À DISTÂNCIA = +1 (não +4): `CombatActions.calcularNH` trata Determinado à distância como +1 (MB p.366); corpo-a-corpo segue +4.
- Controller popula `cadenciaTiro`/`recuo` no `construirAtaques`.
- Testes: tabela de bônus de CdT, `acertosDaRajada` (1+margem/recuo, teto), rajada aplica múltiplos acertos (SMG Recuo 1), Ataque Total à distância +1. Build 2 variantes verde.
- FALTA do "polir combate": **dual-wield** (Lote 377) — 2 armas prontas, mão inábil −4 / Ambidestria, Ataque Total Duplo. Depois: validação no aparelho.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 375 — 14 de Junho de 2026  ·  commit `de7f266`
**Saga combate: Bulk no Avançar-e-Atacar + Aparar E/D — fecha as regras de arma (branch GURPS-Saga)**
- PLUMBING: `Equipamento.armaAparar` (novo, anulável) + `adicionarEquipamentoArma` copia `arma.aparar`. `ArmaCatalogoItem.aparar` já existia. Sem migração (mesmo padrão dos campos do Lote 371).
- BULK (Magnitude) no Avançar-e-Atacar à distância (MB p.366/271): `CombatActions.calcularNH(magnitudeArma=...)` aplica "−2 OU a Magnitude, o pior". `AtaqueHeroi.magnitude` (do `armaMagnitude`); `heroiAtaca` passa a magnitude quando à distância.
- APARAR E/D (MB p.270/404): `ApararTipo` (NORMAL/ESGRIMA/DESBALANCEADA/NAO) + `CombatSession.parseAparar("0D"/"0E"/"F"/"Não"/"-1")`. `AtaqueHeroi.apararTipo`. `opcoesDefesaHeroi(armaPronta)` agora: SEM Aparar se arma à distância, "Não", ou desbalanceada já usada para atacar neste turno (flag `atacouDesbalanceada`, zerada no início de cada ação via `inicioAcaoHeroi`); ESGRIMA → apara extra −2 (`CombatResolver.PENALIDADE_APARA_ESGRIMA`, novo param `esgrima` em opcoesDefesa/valorDefesaFinal). Controller passa a arma empunhada (`ataqueSelecionado`) ao montar o card "Defenda-se!".
- Testes: Bulk pior-de (CombatActionsTest); esgrima −2 (CombatResolverTest); parseAparar, Aparar bloqueado à distância, desbalanceada não apara após atacar (CombatSessionTest). Build 2 variantes verde.
- **REGRAS DE ARMA NO COMBATE COMPLETAS** (reach, Apontar/Acc, 1/2D, Máx, Bulk, Aparar E/D, Sacar/Preparar). Resta só validação no aparelho + (futuro) RoF/Recuo/rajada e dual-wield.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 374 — 14 de Junho de 2026  ·  commit `41a21e1`
**Saga combate corpo-a-corpo: engajamento por reach + Sacar/Preparar arma (branch GURPS-Saga)**
- ENGAJAMENTO POR REACH: `heroiAtaca` agora bloqueia ataque (à distância OU corpo-a-corpo) se `dist > ataque.alcance` ("longe demais — aproxime-se"). O `alcance` do corpo-a-corpo vem do reach da arma ("C"/"1"/"2" convertido em metros no Lote 371). Controller: `alvos` corpo-a-corpo = inimigos com `dist <= reachMelee` (antes era fixo <=1); ATAQUE liberado quando há alvo no alcance (cobre lança reach 2).
- SACAR/PREPARAR (arma pronta vs guardada, MB p.366 "para atacar, a arma precisa estar preparada"): `SagaCombatController.sacarArma(indice)` — com Saque Rápido (perícia) é AÇÃO LIVRE (troca na hora); senão é a manobra Preparar e CONSOME o turno. `temSaqueRapido` detecta a perícia. A arma EMPUNHADA = `ataqueSelecionado` (default índice 0 ao iniciar). ViewModel `sagaCombateSacarArma`.
- UI: SeletorDeArma virou "Sacar" (não "Trocar"): mostra a arma na mão + alcance/Máx; ao abrir, avisa "Sacar outra arma é Preparar (gasta o turno) — livre com Saque Rápido"; a arma já empunhada aparece "(na mão)".
- Teste: corpo-a-corpo respeita reach (espada reach 1 não alcança a 2m; lança reach 2 alcança). Build 2 variantes verde.
- Ainda falta (último de combate): Bulk no Avançar-e-Atacar à distância + Aparar E/D (esgrima/desbalanceada) — precisam persistir armaMagnitude/armaAparar estruturado na ficha.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 373 — 14 de Junho de 2026  ·  commit `91e76d3`
**Saga combate à distância: Apontar + alcance (1/2D, Máx) — usa os stats do Lote 371 (branch GURPS-Saga)**
- Manobra `APONTAR` no enum (MB p.364): `CombatSession.heroiApontar(alvoId)` mira numa arma à distância; o PRÓXIMO tiro ao mesmo alvo soma a `precisao` (Acc) da arma via modsExtra. Consome ao atacar; perde em qualquer outra manobra (limparApontar em mover/manobra/avaliar/ataque). Mutuamente exclusivo com Avaliar.
- `AtaqueHeroi` ganhou `meioDano` (1/2D); `construirAtaques` preenche do `armaMeioDanoMetros`. `alcance` já era o Máx (Lote 371).
- `heroiAtaca` à distância: (1) **Máx** — se `dist > alcance`, o tiro NÃO chega (erro automático, não rola, log "fora de alcance"); (2) **1/2D** — se `dist >= meioDano`, o dado básico cai pela metade antes de RD (MB p.270), com nota no log "└ além de 1/2D: dano pela metade".
- Controller: `heroiApontar`; `atualizarEstado` oferece APONTAR (além de ATAQUE) quando a arma selecionada é à distância e há alvo vivo. ViewModel `sagaCombateApontar`. UI: manobra Apontar → `SubDialogoEscolherAlvo` (reuso) → mira.
- Testes: Apontar soma Acc no tiro; além do Máx não acerta; além de 1/2D corta o dano. Build 2 variantes verde.
- Ainda falta (próximo lote): engajamento por reach corpo-a-corpo (arma "C"/"1"/"2"), Preparar/Sacar arma (pronta vs guardada), Bulk no Avançar-e-Atacar à distância, Aparar E/D.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 372 — 14 de Junho de 2026  ·  commit `988ab54`
**Rolagem: Testes de Sentidos (Per) + automação de vantagens/desvantagens (branch GURPS-Saga)**
- PEDIDO do usuário: clicar "PER" deveria abrir um diálogo com os SENTIDOS (Visão/Audição/Olfato-Paladar), pois são testes de regra (MB p.358); + mapear e AUTOMATIZAR as vantagens/desvantagens que dão bônus/redutor; + "notinha" do motivo; + versão PraCego.
- Estudo no Códex (chunks p359): todo teste de sentido rola vs PERCEPÇÃO somando o "Sentido Aguçado" correspondente.
- `domain/rules/SentidoRules.kt` (PURO): enum Sentido (Percepção/Visão/Audição/Olfato-Paladar/Tato); `avaliar(p, sentido)` devolve percepção base + componentes NOMEADOS + valorFinal + bloqueado/motivo. IDs mapeados do catálogo: visao_agucada(+nível), visao_hiperespectral(+3), audicao_agucada(+nível), paladar_olfato_apurado(+nível), oflato_discriminatorio/paladar_discriminatorio(+4), tato_apurado(+nível); redutores duro_de_ouvido(-4), disopia(-6 condicional); bloqueios cegueira(Cego)/surdez(Surdo)/disosmia(Sem olfato). Cobre vantagens PESSOAIS e RACIAIS.
- `SentidoRulesTest`: base, Visão Aguçada+nota, Duro de Ouvido -4, hiperespectral+discriminatório, cegueira/surdez bloqueiam, vantagem racial conta. VERDE.
- UI: `ui/features/rolagem/DialogoSentidos.kt` — clicar PER (intercept no TabRolagem, sem mexer no AtributosQuickRollPanel) abre o diálogo; cada sentido mostra valor + "notinha" ("Percepção 12 (+2 Visão Aguçada)") e rola pelo MESMO caminho (executarRolagem→Discord) com o rótulo carregando o motivo. Sentido bloqueado fica desabilitado ("Cego"). **Variante PraCego: botão rotulado grande "Rolar (14)"** em vez de tocar o número; semântica TalkBack em todos os itens. O mod situacional do PER (swipe) soma a todos.
- SEM mudança de estrutura de ficha (só LÊ vantagens/desvantagens existentes). Build 2 variantes verde.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 371 — 14 de Junho de 2026  ·  commit `82c1856`
**Saga: stats de arma do catálogo → ficha → combate (plumbing, branch GURPS-Saga)**
- CONTEXTO: combate GURPS depende de stats que os JSONs JÁ TINHAM mas o app DESCARTAVA. Análise de risco confirmou: aditivo, sem migração Room (Equipamento mora no Personagem.toJson()→FichaEntity.json TEXT), construção só por args nomeados, Gson preenche ausentes com null/0/false → ficha antiga carrega intacta.
- `ArmaCatalogoItem`: +campos alcanceCorpoACorpo, duasMaos, precisao(Acc), meioDanoMetros(1/2D), maximoMetros(Máx), alcanceMultStRaw(×ST arcos), cadenciaTiro(CdT), tirosRaw, magnitude(Bulk), recuo(Rcl). Todos com default → seguro.
- `CatalogLoaders`: loader corpo-a-corpo lê modo1.alcanceCorpo + flag †/‡ (duas mãos); loader distância/fogo lê precisao/alcanceDistancia(metade/máx, detecta ×ST)/cdt/tiros/magnitude/recuo.
- `Equipamento` (ficha): +campos armaAlcanceCorpoACorpo/armaDuasMaos/armaPrecisao/armaMeioDanoMetros/armaMaximoMetros/armaAlcanceMultStRaw/armaCadenciaTiro/armaTirosRaw/armaMagnitude/armaRecuo (ANULÁVEIS, no fim da data class). `adicionarEquipamentoArma` copia do catálogo.
- `SagaCombatController.construirAtaques`: passa alcance REAL (Máx p/ tiro; reach "C"/"1"/"1,2"→metros p/ CaC via `reachParaMetros`) e `precisao` real ao `AtaqueHeroi` (antes era 50/1 fixo e 0). Sem mudar regra ainda — só carrega o dado.
- `scripts/check_armas.py`: valida que os 3 catálogos têm os campos críticos. Rodou: corpo-a-corpo=60, distância=28, fogo=62, ZERO erros (13 avisos não-bloqueantes em armas especiais: lança-chamas sem Acc, autos com CdT "!" especial).
- SEM mudança de regra de combate (vem no Lote 372: Apontar/Acc, 1/2D, Máx, engajamento por reach, Bulk no Avançar-e-Atacar, Aparar E/D). Build 2 variantes verde.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 370 — 14 de Junho de 2026  ·  commit `41996c4`
**Saga: manobras com opções no combate (branch GURPS-Saga)**
- PEDIDO do usuario: as manobras estavam "muito fixas" (ex.: Mover nao perguntava p/ onde/quantos metros). Implementadas com sub-dialogos, seguindo o cap. de Combate lido no Codex (MB p.364-366)
- MOVER dirigido: SubDialogoMover (avancar/recuar, em relacao a qual inimigo ou todos, quantos metros ate o Deslocamento). CombatSession.heroiMove(alvoId, afastar, metros) com clamp no deslocamento
- MUDAR DE POSTURA: SubDialogoPostura lista so as posturas alcancaveis; CombatSession.posturasAlcancaveis() aplica a regra do MB "nao se levanta direto de deitado" (de DEITADO so RASTEJANDO/SENTADO/AJOELHADO)
- AVALIAR (MB p.365): CombatSession.heroiAvaliar(alvoId) acumula +1 ate +3 contra o alvo; o bonus entra no PROXIMO ataque corpo-a-corpo aquele alvo (modsExtra) e e consumido; reseta em alvo novo ou em qualquer outra manobra. SubDialogoEscolherAlvo
- UI: roteamento no ManeuverCards (MOVER/AVALIAR/MUDAR_POSTURA abrem sub-dialogo); CombatUiState ganhou deslocamentoHeroi/posturaHeroi/posturasAlcancaveis. Getters sagaCombateMover(alvoId,afastar,metros)/sagaCombateAvaliar no ViewModel
- Testes: avaliar acumula/reseta/entra no ataque; nao levanta direto de deitado; mover dirigido respeita metros e teto de deslocamento
- Build 2 variantes verde
- AINDA FALTA do pedido "manobras": Apontar (+Precisao) e Preparar/Sacar arma (arma pronta vs guardada) — precisam puxar o Acc da arma do catalogo p/ a ficha (proximo lote)
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 369 — 14 de Junho de 2026  ·  commit `2233b45`
**Saga: narração no combate (log evocativo, branch GURPS-Saga)**
- PEDIDO do usuario (validacao): o log do combate era "so texto matematico, nada narrativo". Solucao: narracao DETERMINISTICA (sem IA -> instantanea e de graca) que vira prosa de mestre MANTENDO os numeros num colchete tecnico [..]
- CombatSession.narrarTroca(): compoe a linha a partir dos dados estruturados (RelatorioAtaque/RelatorioTroca/RelatorioDano/ferimento) -> falha ("erra"/"FALHA CRITICA"), defesa ("se esquiva"/"apara"/"bloqueia"), acerto ("acerta X no rosto - N de dano (corte)! cambaleia e cai, atordoado"), critico ("GOLPE CERTEIRO"), 0 de dano ("a protecao absorve tudo"). "voce" (3a pessoa PT-BR) serve p/ heroi e NPC. Colchete tecnico usa calculo.descricao() (mostra postura/local/distancia) + dado + breakdown do dano
- preposicaoLocal() p/ "no rosto"/"na perna"/"nos vitais". Substitui o antigo log cru (troca.texto) nas trocas heroi e NPC. Demais linhas (mover/manobra/atordoamento/fim) mantidas
- O log evocativo tambem MELHORA a entrada da prosa final do Narrador (narrarFimDeCombate usa o log agregado)
- Teste: CombatSessionTest "log de combate e narrativo e mantem os numeros" (verbo narrativo + colchete com rolagem)
- Build 2 variantes verde
- NOTA: continua sem IA por turno (decisao de custo/velocidade). Narrador entra na abertura e no desfecho. Narracao por rodada via IA = opcao futura (toggle)
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 368 — 14 de Junho de 2026  ·  commit `01b01a5`
**Saga: arma em uso real + estudo de regras no Codex (branch GURPS-Saga)**
- PEDIDO do usuario (validacao no aparelho): o pistoleiro atacava de SOCO ("10 cont"), nao dava p/ ver/escolher a arma, e ele pediu p/ eu ESTUDAR as regras no chunks.jsonl (Codex). Li o cap. de Combate do Modulo Basico (manobras p.364-366) direto da fonte
- BUG do "soco" (2 causas): (1) eu usava armaTipoCombate (modo "corpo_a_corpo"/"distancia"/"armas_de_fogo") como se fosse TIPO de dano -> sempre CONT; (2) o perfil so olhava arma corpo-a-corpo. Corrigido: CombatSession.tipoDano agora parseia o token de tipo da expressao de dano ("GeB+2 corte", "2d-1 pa+") e mapeia pa-/pa/pa+/pa++ (Devir) -> pi-/pi/pi+/pi++
- HeroiPerfilCombate agora e SO defesa; ataque virou AtaqueHeroi (rotulo/nh/danoExpr/tipo/aDistancia/alcance/precisao). SagaCombatController.construirAtaques monta a lista de ataques da ficha: cada arma equipada (corpo-a-corpo E fogo/distancia) com pericia casada (acharPericiaDaArma fuzzy por grupo/nome), NH, dano resolvido por ST e tipo; + desarmado. Armas a distancia entram primeiro (pistoleiro saca o revolver). Modo "armas_de_fogo" tratado como a distancia
- Regras de tiro: penalidade de distancia (CombatSession.penalidadeDistancia, tabela Tamanho/Velocidade MB p.550) aplicada via CombatActions.calcularNH(modsExtra=...) tanto p/ heroi quanto p/ NPC arqueiro; contra ataque a distancia o alvo SO pode Esquivar (nao aparar)
- UI: SeletorDeArma no card de manobras ("Empunhando: Revolver — NH X, 2d-1 pi+ · a distancia", botao Trocar); alvos do ataque = todos os vivos (a distancia) ou adjacentes (corpo-a-corpo); ATAQUE liberado a distancia mesmo sem inimigo adjacente
- CORRECAO DE REGRA (lida no Codex, MB p.366): Mover e Atacar estava INVERTIDO. Correto: corpo-a-corpo -4 E teto NH 9; a distancia -2 (sem teto). Consertado em CombatActions + comentario + CombatActionsTest
- Testes: CombatSessionTest (tipoDano corte/perf/pa+, penalidade de distancia, tiro a distancia loga e penaliza) + CombatActionsTest atualizado p/ a regra correta. build.gradle ja tinha returnDefaultValues
- Build 2 variantes verde
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 367 — 14 de Junho de 2026  ·  commit `79f410c`
**Saga FASE B: fix da UI de combate (achado na validacao no aparelho, branch GURPS-Saga)**
- BUG (1o teste real no device): o CombatePainel entrava na Column do feed SEM weight nem scroll -> engolia o espaco do chat (feed sumia) e TRANSBORDAVA, cortando as manobras de baixo sem como rolar
- CombatUi.kt: CombatePainel agora recebe Modifier e usa Surface(fillMaxWidth) + Column(fillMaxSize); cabecalho "Rodada" FIXO no topo; tracker + manobras/defesa num Column com weight(1f) + verticalScroll -> rola por dentro
- TabSaga.kt: o painel recebe Modifier.weight(1.5f) e o feed (LazyColumn) mantem weight(1f) -> chat ~40% / combate ~60%, ambos visiveis e roláveis
- So UI/layout; sem mudanca de logica/regra. Build 2 variantes verde
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote_dados_3D 001 — 18 de Junho de 2026  ·  commit `429865b`
**Simulação 3D de Dados (branch GURPS-Saga)**
- Migração completa de React Three Fiber para SceneView Filament nativo.
- Integração da engine JBullet para colisão e repulsão vetorial 3D.
- Resolvido o "fantasma visual" onde os dados se sobrepunham: matriz física agora preserva a escala original do modelo visual.
- Mapeamento dinâmico de leitura de faces: vetores X, Y, Z sincronizados com o modelo `.glb` exportado do Blender.
- Câmera Top-Down fixa e paredes elásticas reduzidas garantindo que 100% dos eventos físicos ocorram dentro da tela.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote_dados_3D 002 & 003 — 18 de Junho de 2026
**Som Imersivo e Interligação de Combate (branch GURPS-Saga)**
- **Áudio Baseado em Física:** Implementado `CollisionListener` em `PhysicsWorld` que monitora `contactManifolds`. Impactos reais (`appliedImpulse > 0.5f`) disparam sons de batida dinâmicos no `DiceSoundManager` com variações de pitch e volume.
- **Remoção do Mock:** O `Dice3DScene` abandonou a janela de testes isolada com "HUD neon" e agora aceita parâmetros escaláveis (`diceCount`, `onRollFinished`).
- **Overlay de Batalha:** `TabRolagem.kt` completamente refatorada. `executarRolagem`, `executarRolagemDano` e `executarRolagemPersonalizada` agora disparam o estado `PendingRollState`.
- Ao disparar rolagem, a tela sofre um `Modifier.blur(16.dp)`, revelando o `Dice3DScene` transparente que faz a jogada física real, colhe os resultados da simulação e devolve para as regras do GURPS injetar no Chat do Discord com todos os modificadores aplicados.
----------------------------------------------------------------------------------------------------------------------------------------------------


### Lote_dados_3D 004 — 19 de Junho de 2026  (commits 64d4223, ad895f9, 585f040)
**Customização de Cores dos Dados 3D (branch GURPS-Saga)**
- Criação da loja/persitência de cores via SharedPreferences (DiceColorsStore).
- Menu de Configuração ultra-premium ConfigurarDadosDialog com grid flow, radiantes e UI temática.
- Correção crítica da engine Filament: conversão do compose color space (sRGB) para LinearSrgb permitindo fidelidade absoluta na renderização das texturas `baseColorFactor` no JBullet/SceneView.
- Substituição de mocks 2D no preview por um micro motor 3D ativo exibindo rotação vetorial do .glb em tempo real na tela de configuração.
- Integração no Menu superior ao lado da foto do personagem (DialogsCommon.kt).
---------------------------------------------------------------------------------------------------------------------------------------------------

### Lote_dados_3D 005 — 19 de Junho de 2026  (commits b15b57a, c3f0c47)
**Imersão Sensorial 3D: Materiais, Haptics e Limites Físicos (branch GURPS-Saga)**
- **Haptic Feedback Dinâmico**: Roteamento de eventos do motor de física (JBullet) para a API `LocalHapticFeedback` do Compose. Colisões com força acima de 1.5 disparam trancos táteis, simulando impacto direto do dado na mão do usuário.
- **Renderização Baseada em Física (PBR)**: Shader Filament agora mapeia dinamicamente os parâmetros `metallicFactor` e `roughnessFactor` conforme o material selecionado (Fosco, Plástico ou Metal).
- **Caixa de Contenção Física**: Restauração das barreiras grossas com `BoxShape` nos eixos X e Z da cena, contendo implacavelmente a física gravitacional, para assegurar que todos os dados saltem livremente mas permaneçam estritamente dentro da "mesa" na tela do celular.

### Lote_dados_3D 006 — 19 de Junho de 2026 (commit 448c674)
**Acessibilidade PraCego na Rolagem 3D (branch GURPS-Saga)**
- **TalkBack Dinâmico**: Implementação de `LaunchedEffect` aliado ao `LocalView.current.announceForAccessibility()` para forçar a leitura do resultado exato da rolagem na variante PraCego.
- **Interpretação Narrativa**: Textos convertidos de dados crus para frases descritivas claras (ex: "Esquiva (NH 14). Falhou por 2", "Dano causou 8").
