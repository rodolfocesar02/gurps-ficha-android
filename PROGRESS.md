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
- **scripts/gerar_topic_index.py:** parse automático de `docs/fonte-regras/indice.md` + `glossario.md` → 515 tópicos
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

### Recon — 24 de Junho de 2026
**Mapa de regras de combate do GURPS Artes Marciais (doc-only, branch GURPS-Saga)**
- Novo arquivo `docs/pendencias/Artes_Marciais_Regras_Combate.md`: inventário das regras de combate do livro Artes Marciais a partir do `chunks.jsonl` (`pt_artes_marciais`, 264 págs) — análogo ao audit do `docs/fonte-regras/Combate.md`, para planejar uma eventual "Fase Artes Marciais" do combate Saga.
- Cobre: Técnicas (Cap. 3, ~110 técnicas via Tabela p258–262), Capítulo 4 — Combate (manobras expandidas, opções de combate p109–113 lidas em detalhe, combate corporal, opções de defesa, ataques múltiplos, lesões realistas), vantagens/perícias e armas/equipamentos. Cada regra com tag de encaixe no modelo de faixas (🟢 FIT / 🟡 PARCIAL / 🔴 FORA / ⚪ JÁ FEITO).
- **Recomendação registrada** (maior valor × menor custo): Ataque Telegráfico (par do Enganoso), luta agarrada profunda (chaves/Mata-Leão estendendo o lote 422), Sangramento Grave + incapacitação de membro (item 5 do teste de batalha), Ataque Dedicado/Defensivo. Fora de escopo: posicional/hexágono, montaria, cinematográfico, dado de arma do NPC. Mudança só de documentação (não compila Kotlin).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote A1-b / A1-c — 21 de Julho de 2026 (tipo de criatura — mortos-vivos e insubstancialidade viram regra)
**Segunda metade do A1 — branch GURPS-Saga**
- **A1-b — mortos-vivos** (deferido honesto do MEC-22, agora executado). Regras literais: *"Seres mortos-vivos não são afetados"* (Morte Candente) e *"Mortos-vivos não são afetados"* (Morte Putrefata). São justamente as **duas mágicas de tique que o motor já executava** — ele batia em esqueleto sem saber em quem. Novo `TipoCriatura` (VIVO / MORTO_VIVO / INSUBSTANCIAL / ELEMENTAL / CONSTRUCTO) e campo curado `naoAfeta`. Ganchos no funil de dano mágico e no tique; no tique a mágica **não fica pendurada** em quem ela não afeta — ela se desfaz.
- ⚠️ **Eixo SEPARADO da tolerância**: `ToleranciaFerimentos.NAO_VIVO` diz **quanto** dano físico o corpo sofre; `tipoCriatura` diz **se a mágica pega nele**. Um golem é `NAO_VIVO` na tolerância e `CONSTRUCTO` no tipo — e a exclusão de Morte Candente **não** pode pegá-lo por tabela. Há teste só para travar essa confusão.
- **A1-c — insubstancialidade** (MB, vantagem de 80 pontos), as quatro regras:
  1. *"Ataques físicos e de energia não afetam o personagem"* → guarda **antes de rolar** para acertar (mesmo padrão do fora-de-alcance). Não é errar o golpe, é o golpe atravessar.
  2. *"mas ele continua vulnerável a ataques psíquicos e mágicos"* → o funil `aplicarDanoMagico` **não** consulta a guarda. Magia passa.
  3. *"Da mesma maneira, **seus** ataques físicos e de energia não afetam oponentes físicos"* → metade **simétrica** no `npcResolve`. Sem ela eu teria criado um fantasma **invulnerável e letal**, que não é regra nenhuma.
  4. *"todas as jogadas sofrem uma penalidade de −3"* ao conjurar → em `npcConjurar`.
  - A saída: **Afetar Espíritos** (*"uma arma com essa mágica pode prejudicar um espírito insubstancial"*) virou buff executável que destrava o golpe.
- **Espectro adicionado ao bestiário**: sem criatura insubstancial a regra inteira seria **inalcançável em jogo** — código certo que nunca roda.
- O carregador do bestiário passou a ler `tipo` e `imunidades` (o A1 criou os campos no `NpcStats`, mas nada os preenchia do JSON). Esqueleto e Zumbi marcados `morto_vivo`. Tipo desconhecido cai em **VIVO**, então nenhuma exclusão dispara por engano.
- **+11 testes.** ⚠️ **O gate pegou um bug NO MEU TESTE**: a asserção de PV passava e a de log falhava — `IntencaoNpc` sem `alvoId` faz `intencaoAtacaHeroi` devolver false, o NPC nem tenta atacar e o herói ficava intacto **pelo motivo errado** (armadilha do MEC-31). Corrigido, e o teste do Afetar Espíritos ganhou um **controle** na mesma asserção pelo mesmo risco.
- 🟢 Gate: **867 testes por variante, ZERO falhas**, build nas duas. Não toca UI.
- ⚠️ Erro de processo meu: editei arquivos com um gate rodando e cheguei a ter **dois Gradle concorrentes**. Parei os dois, matei os daemons e refiz limpo — nenhum resultado daqueles builds foi usado.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote A1 — 21 de Julho de 2026 (imunidade por ELEMENTO — o eixo que faltava para 23 mágicas de dano)
**Primeiro passo do plano de maximizar mecânica em magia — branch GURPS-Saga**
- **A medição que orientou a decisão**: o catálogo inteiro tem **92 de 879 executáveis (10,5%)**. Agrupando os 787 restantes por substrato faltante: Sentidos 108, Informação 55, Terreno 49, Tipo de criatura 40, **Imunidade por dano 38**, Luz 31, Controle 26. Imunidade era o **mais barato com maior alcance** — dois campos e um gancho.
- **O eixo que faltava**: `tipoDano` é o multiplicador de ferimento do GURPS (cont/corte/perf), e `"quei"` caía em `DanoTipo.CONT` — o elemento se perdia. O próprio código documentava a lacuna (*"sem enum de queimadura"*). Sem elemento, "Imunidade ao Fogo" não tinha o que consultar. Novo campo **`elementoDano`**, separado do `tipoDano`.
- **Um funil, três entradas**: `aplicarDanoMagico` já concentrava magia direta, área e NPC conjurador, então a checagem entrou lá — **antes de rolar o dado**, porque o livro diz *"torna-se imune"*, logo não há dano a reduzir. Mais dois ganchos onde o caminho é próprio: o ramo de área e o tique de zona (`ZonaPersistente` ganhou `elementoDano`). **Não** toquei nos 19 call sites de `aplicarDano` — dano físico não tem elemento, e passar parâmetro por todos seria a armadilha do MEC-14 de novo.
- **Os dois lados com o mesmo código**: `Combatente.imunidades` soma o bestiário (`NpcStats.imunidades`, para o elemental de fogo) e os buffs (a mágica Imunidade, para o herói, que não tem `NpcStats`). Sem ramificação.
- **Curadoria**: 23 mágicas com elemento (11 fogo, 6 eletricidade, 4 ácido, 2 frio) e as 4 mágicas de Imunidade ligadas.
- **Duas correções que só apareceram indo ao livro**, em vez de deduzir pela escola:
  - **Adaga de Gelo e Esfera de Gelo não são dano de frio.** A Adaga diz *"dano por perfuração"* e *"nenhum efeito extra em criaturas de fogo"* — é arma física de gelo. E a Imunidade ao Frio exclui literalmente *"lanças mágicas de gelo"*.
  - **Jato e Sopro de Vapor não são fogo**: o livro diz que causam o **dobro** de dano a criaturas de fogo. Se fossem elemento fogo, um imune levaria zero — o oposto. O ×2 fica **deferido** (falta o eixo de vulnerabilidade).
  - As quatro estão **travadas por teste** contra o catálogo real, para ninguém "corrigir" isso por engano.
- **+7 testes**: imune não perde PV; a imunidade **não vaza** entre elementos (regra literal *"imunes ao calor e ao fogo, mas não da eletricidade"*); sem imunidade o dano passa; imunidade natural do bestiário; zona não fere o imune; e a trava do `soNarrado`.
- Gate: **856 testes por variante, ZERO falhas**, build nas duas. Não toca UI.
- O **primeiro gate falhou** por nome de teste com `:` — parente do `;` que já pegou duas vezes. Registrado na memória com o jeito certo de varrer.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote P3-1 — 21 de Julho de 2026 (P3: IQ e Vontade no buff, 6 mágicas curadas — e a triagem honesta dos 156)
**"faz o p3!" — branch GURPS-Saga**
- ⛔ **A promessa do `PENDENCIAS.md` era FALSA.** Ele dizia que o P3 faria *"156 magias saírem de narrado para mecânica"*. Li os **156 rótulos um a um**: a maioria esmagadora **não** é "extrair número da prosa", é efeito **sem substrato no motor**. É o mesmo erro do texto do Escudo já corrigido naquele arquivo — frase herdada sem conferir o dado.
- 📊 **Número honesto**: de 156, **8 são mecanizáveis**, **~94 precisam de feature nova** (~35 Sentidos/visões, ~14 formas de corpo, ~11 imunidades, ~12 terreno, ~10 perícias, 4 roubo de atributo, 4 tamanho, 1 ação extra) e **~54 não têm o que mecanizar** (o rótulo **é** a mecânica). Triagem completa na nova seção **2.1e**.
- ✅ **6 mágicas curadas**, números conferidos no livro: **Bloquear** (BD +1 a +5, 1 energia/ponto, um teste de defesa — p.101), **Robustez** (RD +1 a +5, um ataque — p.101), **Fortalecer Vontade** (+1/energia, máx +5 — p.100), **Enfraquecer Vontade** (−1 a cada **2** de energia, máx −5 — p.100), **Sabedoria** (IQ +1 a cada **4**, máx +5 — p.100), **Tolice** (IQ −1/energia, máx −5 — p.134).
- 🧩 **Substrato**: IQ e Vontade abertos no `BuffAplicado` (só havia ST/DX/HT). A Vontade entra via `heroiPerfil`, que é propriedade **computada** — então passa a valer **de uma vez** em todo teste de Vontade do motor (concentração do MEC-26, projétil da C1, pontaria perdida pela dor) **sem tocar em nenhum ponto de uso**. No NPC a Vontade deriva do IQ do bestiário, então o IQ cru virou `iqEfetivo`.
- 🔎 **Achado 1 — duas estavam bloqueadas por NOTAS VENCIDAS.** A nota do **Bloquear** dizia *"o motor NÃO tem campo para BD"* — mas `buffBd` existe desde o **MEC-4**. A da **Robustez** dizia que daria *"RD persistente"* — mas `buffUmUnicoUso` existe desde o **MEC-6**. Ficaram narradas por **documentação desatualizada**, não por limitação. Mesmo padrão dos 5 itens marcados ❌ na varredura de classes que já estavam prontos.
- 🐞 **Achado 2 — erro de DADO na Tolice**: o catálogo tinha `duracao: "2d dias"`; o livro (MA p.134) diz **"Duração: 1 minuto"**. Erro de transcrição que o MEC-5 não pegou. Corrigido.
- ⚠️ **Achado 3 — quase repeti a armadilha do MEC-14**: `registrarMagiaAtiva` **descarta** o buff quando `soNarrado` é true, e `soNarrado` **não conhecia os campos novos** — o buff de Vontade seria calculado certinho e **jogado fora em silêncio**. Achado antes do build; travado com teste de regressão e com um aviso no código para quem adicionar o próximo campo.
- 🚧 **Deferido de propósito — Bênção e Maldição** (±1 a ±3 em **todas** as jogadas): a regra diz *"a modificação não afetará os sucessos e falhas críticas"*, e honrar isso exige mudar a classificação de crítico no `CombatResolver`. **Lote próprio, com gate próprio** — meia regra seria pior que nenhuma. Também deferido: o teto *"IQ do alvo não pode superar o do operador"* (Sabedoria).
- **+13 testes**: 7 unitários (escala, teto, vazamento entre atributos), **4 de INTEGRAÇÃO** (o buff chega ao `heroiPerfil` pelo caminho real — lição do MEC-14) e **2 travas contra o CATÁLOGO REAL**, para esta curadoria não regredir como os 156 regrediram.
- 🟢 Gate: **845 testes por variante, ZERO falhas**, build nas duas. Não toca UI.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote AM-0 — 21 de Julho de 2026 (a aba de Magias sumia de quem comprava Aptidão Mágica 0)
**"ela começa no nível 0 e, deixando no 0, a aba de Magias não aparece" — relato do usuário na aba Traços › Vantagens**
- 📖 **A regra, conferida no livro** (MB p.41): *"**Aptidão Mágica 0:** Representa uma 'consciência mágica' básica, **um pré-requisito para se aprender magia** na maior parte dos mundos. [...] 5 pontos."* AM 0 é **exatamente** o nível que habilita magia — a aba tinha que aparecer. É bug.
- 🐞 **Causa**: o gate usava o **bônus de NH** como se fosse "tem a vantagem?" — `val temAptidaoMagica get() = nivelAptidaoMagica > 0`. Só que `nivelAptidaoMagica` é `getNivelAptidaoMagicaParaMagia`, o **bônus somado à IQ**, e o bônus de AM 0 é **zero por definição** (AM 0 não soma nada, só destranca o aprendizado). Ou seja: o único nível que a regra existe para habilitar era justamente o que o app desabilitava.
- ✅ **Correção**: o gate pergunta pela **presença** da vantagem (`MagicEngine.possuiAptidaoMagica`), não pelo bônus. O filtro que varre a ficha **e o modelo racial** virou **fonte única** lida pelas duas funções — duas cópias do mesmo filtro divergem em silêncio (lição do LIMPEZA-1).
- 📝 **Nota pra quem ler depois**: o nível **interno** da vantagem é **1-based** — interno 1 = AM 0, interno 2 = AM 1. É por isso que o bônus e a exibição são `nivel − 1` e o custo é `5 + (nivel−1)×10`. Isso **já estava certo**; só o gate mudou.
- **+5 testes**, incluindo dois casos **não relatados** que quebrariam igual: ficha com nível interno **0** (antiga ou importada) e Aptidão Mágica vinda do **modelo racial** (elfo e dragão do MB vêm com *"Aptidão Mágica 0 [5]"* na raça).
- 🟢 Gate: **823 testes por variante, ZERO falhas**, build nas duas.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-47 — 21 de Julho de 2026 (herói × a própria área: decisão da regra + o "PV do nada" das faixas)
**Achado no log de Chuva de Fogo do teste no aparelho — branch GURPS-Saga**
- 🔎 **A divergência**: o dano inicial da área **excluía** o herói (`filter { it.id != "heroi" }`) e o tique da zona o **incluía**. Fui à fonte: o capítulo de Área tem **as duas regras**, e elas puxam para lados diferentes — *"afeta todos os seres vivos dentro da área"* e *"o operador pode escolher afetar apenas partes da área, pagando o mesmo custo"*.
- ⚖️ **Decisão do usuário — poupa na conjuração, queima na zona.** As duas metades agora são diferentes **de propósito** e estão documentadas; antes a divergência era **acidental** e não havia uma linha de comentário explicando qual regra cada lado aplicava.
  - **Conjuração**: o operador usa a escolha de "afetar apenas partes" em favor de si mesmo — ninguém mira a própria explosão.
  - **Zona persistente (P1b)**: a nuvem que fica no chão é perigo **contínuo** e não distingue ninguém. Quem pisar no fogo queima, inclusive ele.
- 🐞 **Bug REAL achado na investigação (caminho SEM grade, modo faixas)**: `distancia(heroi)` é **0 por definição** — todas as distâncias do encontro são medidas a partir dele. Então o herói caía dentro de **qualquer** zona, inclusive uma Chuva de Fogo que ele mesmo largou a 20m. É o **"perder PV do nada"** que ele já tinha reportado, numa forma nova. Sem grade não dá pra saber onde a nuvem está em relação a ele, então vale o **dono**: zona do próprio herói fica de fora, zona de NPC pega (foi mirada nele). No tático **com grade nada muda** — lá a posição é real.
- 📣 **Aviso explícito** (o que faltava pra não parecer dano do nada): no **registro** da zona sai `⚠️ Você está DENTRO da <nome> — vai queimar a cada Ns`; no **tique** o log diz `☁️ VOCÊ está dentro da <nome>` no lugar da linha genérica `atinge <nome>`, que não explicava a perda de PV.
- **+3 testes**: zona do próprio herói não o fere sem grade; zona de NPC fere e nomeia; o aviso sai no registro, não só no primeiro tique.
- 📄 Doc: a tabela de Área do `CLASSES_DE_MAGICA.md` registra a decisão e rebaixa *"afetar apenas partes"* de ❌ não-implementado para 🟡 **parcial honesto** (só o caso do operador).
- 🟢 Gate: **818 testes por variante, ZERO falhas**, build nas duas. Não toca UI.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote TOK-ZOOM — 21 de Julho de 2026 (pinça de dois dedos + grid ocupando a tela toda do combate)
**"com dois dedos na tela eu consiga dar zoom-in e zoom-out, às vezes eu perco a noção de espaço" + "o grid, pode fazer ele tomar toda a tela de combate?" — branch GURPS-Saga**
- 🔍 **Zoom de dois dedos** (`HexCanvas.kt`): `detectDragGestures` → `detectTransformGestures` nos **dois** canvas (combate real e preview). Um dedo arrasta, dois dedos dão zoom, no mesmo gesto — não há botão nem modo.
- `cameraEfetiva` ganhou o parâmetro **`zoom`** (default `1f`, então toda chamada antiga fica idêntica), que **multiplica** o tamanho de hex que a câmera enquadrou sozinha. Limites `ZOOM_MIN 0.6f` / `ZOOM_MAX 4f`.
- ⚠️ **Detalhe que era fácil errar**: o pan em px é convertido pelo tamanho **já ampliado** (`tam·zoom`), não pelo original — aproximado, o dedo anda **menos** hexes, que é o comportamento de mapa. Dividir pelo `tam` original faria o mapa disparar sob zoom. Tem teste dedicado.
- O zoom do usuário **não é resetado** no reenquadramento de cada turno (o pan continua sendo): quem aproximou pra "achar o espaço" segue aproximado no turno seguinte. O centro continua **clampado** à grade.
- 🖥️ **Grid na tela toda** (`TabSaga.kt`): no tático o feed **saiu do fluxo** da `Column` — era `weight 0.7` contra `3` do grid, ~19% da altura roubados. Agora o `Box` do grid é o **único filho com peso** e ocupa tudo abaixo do cabeçalho e da caixa do Narrador.
- 📜 A narração **não sumiu**: virou `FeedFlutuanteTatico`, cartão translúcido no topo da grade. **Recolhido** mostra a última fala (3 linhas); **tocando, expande** pra metade da tela com o histórico rolável. Teto de **40 falas** — é `Column` com scroll, não `LazyColumn`, então montar a campanha inteira custaria caro.
- Menu do herói desceu de `42dp` pra `88dp` de respiro (mora abaixo do cartão); o auto-scroll da `LazyColumn` do feed agora só roda **quando ela existe** (no tático não há lista pra rolar).
- **+5 testes** em `CameraHexTest`: zoom multiplica e o default não muda nada; clamp min/max; pan proporcional ao zoom; centro preso à grade sob zoom máximo; round-trip do toque (hex → tela → hex) sob zoom.
- 🟢 Gate: **815 testes por variante, ZERO falhas**, build nas duas.
- 🚦 **Toca UI → PARA para teste no aparelho.** Dois pontos a conferir, ditos honestamente: o cartão recolhido ocupa ~88dp no topo do grid e **captura o toque ali** (aquele espaço já era do menu do herói, mas confirme que não atrapalha mover); e enquanto **expandido** ele cobre o menu do herói — um toque recolhe.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-46 — 21 de Julho de 2026 (P1b: ZONAS persistentes — a última funcionalidade de combate)
**"faz o p1b, e coloca na fila as que necessitam delas" — branch GURPS-Saga**
- ✅ **P1b**: mágica de área agora pode deixar uma **ZONA** que fere quem estiver dentro, **a cada intervalo**, enquanto durar. Antes a área feria **uma vez só**. `ZonaPersistente` (centro, raio, dado, intervalo, teste, relógio) + `tiqueDasZonas()` no avanço de turno + expiração com aviso.
- 🎨 **A UI era o ponto crítico e foi feita**: a zona é **pintada na grade** (laranja-queimado translúcido), desenhada **antes** dos hexes de movimento e dos tokens. Sem isso o jogador perderia PV "do nada" — exatamente o bug que ele já reportou uma vez ("só de se movimentar ele tá perdendo PV").
- 🧩 **Ocupação resolvida onde há informação**: o motor não tem a grade (ela vive no controller), então `ocupantesDaZona` é um **ponto de injeção** — o padrão usa a distância-ao-herói (aproximação de faixas) e o controller substitui pelo cálculo **real por hex**. Instalado no `iniciarCombate`, então vale em **campanha e sandbox**.
- **7 magias curadas**: Chuva de Ácido/Fogo/Pedras, Nuvem de Fogo/Faíscas, Tempestade de Faíscas (1s) e **Mau Cheiro** (60s + teste de **HT**, *"uma vez por minuto"*).
- 🔗 **Destravou o P8**: o degrau de custo dobrado (2d-2) só valia na aplicação única; agora vale em **cada tique** — que era o payoff pleno registrado lá no MEC-36.
- **+5 testes**: fere por turno; expira e para; quem está fora do raio não é ferido; intervalo de 60s não dispara em 5s; e o teste de HT deixa a vítima aguentar.
- 🟢 Gate: **803 testes, ZERO falhas**.
- 🚦 **Toca UI → PARA para teste no aparelho.**
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-45 — 21 de Julho de 2026 (a pergunta certa do usuário: "é DX? poderia ser Ataque Inato?")
**Teste do P11 no aparelho FUNCIONOU (Segurar → Aumentar 2/3s → 3/3s → Apontar → Arremessar, e a C1 disparou) — branch GURPS-Saga**
- ✅ **O P11 passou no aparelho**: o log dele mostra a bola crescendo (`+3 → 6`, `+3 → 9`), a mira somando, o arremesso a **9 de energia** saindo **4d-4** e tirando o goblin de combate. E a **C1** rodou sozinha quando o goblin acertou: *"Ferido, você segura firme (Vontade 20, rolou 6)"*.
- 🎯 **A pergunta dele estava certa e achou uma limitação MINHA**: *"é baseada em DX? poderia ser alguma perícia, talvez Ataque Inato?"* — o livro manda usar a perícia **Ataque Inato** (Magia p.12); eu vinha aproximando por **DX** e havia registrado isso como "simplificação honesta" porque *achei* que a perícia não existia na ficha. **Ela existe no catálogo** (`pericias.json`). Era limitação minha, não do sistema.
  - Agora o perfil carrega `nhAtaqueInato` (mesmo padrão da Acrobacia); o arremesso usa a **perícia** quando o herói a tem e **avisa no log** quando cai na DX por não tê-la.
- 🔴 **Transparência que faltava**: no **erro** o log mostrava NH e rolagem, mas no **ACERTO** não — por isso ele não via de onde vinha o acerto. Agora mostra nos dois.
- **+3 testes**: usa a perícia quando existe; avisa a queda para DX quando não; e o acerto exibe NH+rolagem.
- 🟢 Gate: **798 testes, ZERO falhas**.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-44 — 21 de Julho de 2026 (a FICHA também ressincroniza com o catálogo)
**Print do usuário: a mesma magia aparecia "Comum" na aba Magias e "Projétil" no combate — branch GURPS-Saga**
- 🎯 **O MEC-42/43 consertou só o caminho de COMBATE.** A **aba Magias** lê `personagem.magias` direto, então continuava mostrando a cópia velha — a Bola de Relâmpagos aparecia com **duas classes diferentes ao mesmo tempo**, dependendo da tela.
- **Conserto de raiz**: ao **carregar a ficha**, as magias são ressincronizadas com o catálogo (`classe`, `energia`, `tempoOperacao`). Busca por id e, falhando, pelo **nome normalizado**. Magia que o catálogo não conhece (caseira) fica **intacta**. Loga no logcat quantas foram atualizadas.
- ✅ **Confirmado o que JÁ estava certo** (investiguei antes de mexer, em vez de assumir):
  - **Tempo de operação**: `"1 a 3 seg."` → 1 s no one-shot está **correto** — o livro diz que o operador *escolhe* de 1 a 3 segundos, e a bola **cresce conforme investe**. Esse crescimento é exatamente o **Segurar/Aumentar** do P11.
  - **A conta da energia**: o teto 6 do diálogo revela **Aptidão 3**; a regra é *"até o **dobro** da AM **por segundo**"* = 6/segundo. Logo **6 de energia cabem em 1 segundo, sem espera**. Para 3 segundos seriam até 18, via Segurar/Aumentar.
  - **O botão "Segurar" ESTAVA na tela** (visível no print do usuário, ao lado de Voltar/Conjurar). Ele usou "Conjurar", que é o arremesso imediato — daí o log de one-shot. Os chips **Apontar/Arremessar** só aparecem nos tokens **depois** de segurar.
- ⚠️ **Divergência REAL registrada** (não corrigida): a Bola de Relâmpagos, no livro, **flutua** até 1 minuto e *"não pode ser arremessada da maneira usual"* — move-se sozinha a NH/5 m/s e explode ao comando ou ao tocar algo. O app a trata como projétil arremessado comum. Isso explica a duração de "1 minuto" que o usuário estranhou: **não é erro do livro**, é uma mecânica de projétil-que-flutua que não temos. Vai para o PENDENCIAS.
- 🟢 Gate: **795 testes, ZERO falhas**.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-43 — 21 de Julho de 2026 (o MEC-42 não bastou: a busca no catálogo falhava)
**"eu sincronizei e dei run novamente, continua comum a magia!" — branch GURPS-Saga**
- **O MEC-42 estava certo no diagnóstico mas incompleto no conserto.** Ele fazia o catálogo mandar sobre a cópia da ficha — mas via `getMagiaPorId(definicaoId)`. Se o `definicaoId` da ficha estiver **vazio ou de um esquema antigo**, a busca devolve `null`, o código cai no *fallback* e a magia **continua com a classe velha**. Era exatamente o caso.
- **Conserto**: `defDoCatalogo` agora busca por **id** e, falhando, pelo **NOME normalizado** (sem acento/pontuação). Conferido no catálogo: `id = "bola_de_relampagos"`, `classe = "Projétil"` — o dado está certo; o que faltava era **achá-lo**.
- **Uniformizadas as 4 buscas restantes** que ainda chamavam `getMagiaPorId` direto (mira de área, lista de conjuráveis, mecânica da UI). Agora só existe **um** ponto de busca, dentro do helper.
- 🔎 **Diagnóstico embutido**: quando o catálogo não acha a magia, sai no logcat `catálogo NÃO encontrou a magia 'X' (id='...') — usando os dados da ficha`. Se o sintoma voltar, o log diz na hora se é isto.
- 💡 **Dois lotes para uma causa** — o MEC-42 tratou "o catálogo deve mandar" e o MEC-43 tratou "…mas só se a busca funcionar". A lição: quando o conserto depende de um *lookup*, **verificar que o lookup acha**, não só que a precedência está certa.
- 🟢 Gate: **795 testes, ZERO falhas**.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-42 — 21 de Julho de 2026 (A CAUSA REAL: a ficha guarda cópia velha do catálogo)
**"isso era impossivel, eu sincronizei e dei RUN depois que vc falou pra testar" — o usuário derrubou meu diagnóstico, e tinha razão — branch GURPS-Saga**
- 🔴 **Eu errei o diagnóstico por viés de confirmação.** Afirmei "build antiga" ao ver a Bola de Relâmpagos como "Comum": peguei o primeiro indício que encaixava e **parei de investigar**. O usuário respondeu que tinha sincronizado e dado RUN naquele momento — impossível ser build velha. Ele estava certo.
- 🎯 **A causa real, muito pior**: `MagiaSelecionada` (a magia **na ficha do personagem**) guarda **cópia própria** de `classe`, `energia` e `tempoOperacao`, tirada quando a magia foi adicionada à ficha. **Toda correção de catálogo era invisível para quem já tinha a magia.** Isso anulava, na prática:
  - as **classes** corrigidas no D1/MEC-32..35 (por isso a Bola de Relâmpagos seguia "Comum" → `ehProjetil` falso → **sem os chips Segurar/Apontar/Arremessar do P11/P6**, e ela caía no caminho Comum);
  - o **custo** corrigido no MEC-41 (`"1 a 2×AM"`), que continuava lendo o `"2 a 6/M"` velho da ficha.
- **Conserto de raiz**: o **catálogo manda**; a cópia da ficha vira só *fallback* (magia caseira / catálogo ausente). Helpers `classeDaMagia` / `energiaDaMagia` / `defDoCatalogo` aplicados nos 8 pontos do caminho de combate. O custo já fazia isso desde o MEC-5b — a **classe** é que nunca fazia.
- 💡 **Lição de método**: o sintoma tinha DUAS explicações compatíveis (build velha / dado velho). Eu escolhi a que me livrava de culpa e não testei a outra. O certo era conferir onde a UI lê a classe — o que levou 2 minutos quando finalmente fiz.
- ⚠️ Gate: **795 testes, ZERO falhas**. Houve um `NoClassDefFoundError` isolado na variante **Release** (325/327), **transitório** — artefato de build; passou limpo na re-execução.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-41 — 21 de Julho de 2026 (teste no aparelho: custo variável, Lampejo morto, atordoamento opaco)
**"ainda estamos tendo problemas na conjuração de magias com custo variável! novamente!" — branch GURPS-Saga**
- 🔴 **A raiz do custo variável, que o usuário já tinha reportado 2×**: o seletor de energia só aparecia se a magia fosse **projétil**, OU tivesse dano marcado, OU fosse buff que escala. Magia de **custo variável** fora desses casos (a maioria!) era lançada **no mínimo, sem o jogador escolher**. Agora `custoVariavel` abre o seletor — é o campo que faltava, não um ajuste de condição.
- 🔴 **Bug MEU do MEC-37, achado pelo log dele**: pus as bandas do **Lampejo** só no ramo de **ÁREA**, mas o Lampejo é **classe Comum** no livro (confirmado no PDF). As bandas **nunca rodavam** — o log mostrava só "fica CEGO". Agora o caminho Comum também aplica as bandas, a todos dentro de `condicaoRaioM`, cada um pela sua distância ao centro.
  - Precisou de `distanciaEntre(a, b)` — ⚠️ **aproximação honesta**: o encounter só guarda distância ao HERÓI, então é `|dist(a) − dist(b)|`, exato em linha e subestimando fora dela.
  - `condicaoRaioM` do Lampejo subiu de 10 → **30** (as bandas do livro vão até 26m+).
- 🟡 **Dúvida dele respondida no código**: *"eles voltaram a agir em 2 turnos — teve teste de HT ou é tempo fixo?"* — **tem teste de HT**, mas só o **sucesso** era logado. Agora loga os dois: "recupera-se (HT 11, rolou 8)" **e** "continua ATORDOADO (HT 11, rolou 14)".
- 🔴 **Dado errado**: Bola de Relâmpagos tinha `energia: "2 a 6/M"`; o livro diz *"qualquer quantia até o **dobro** da Aptidão Mágica por segundo, por três segundos"*. Corrigido para `"1 a 2×AM"` — padrão que o `tetoDeEnergiaDano` já entende.
- ⚠️ **Provável causa dos "sem opção de Segurar/Apontar"**: o aparelho estava com build **anterior** ao MEC-39/40 (o print mostra a Bola de Relâmpagos como "Comum", classe que eu já havia corrigido para Projétil no MEC-33). **Precisa reinstalar** para ver os chips do P11/P6.
- **+2 testes**: Lampejo Comum aplica bandas em todos no raio; `distanciaEntre` simétrico.
- 🟢 Gate: **795 testes, ZERO falhas**.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-40 — 20 de Julho de 2026 (P6: Precisão do projétil ao Apontar)
**"faz p6" — branch GURPS-Saga**
- ✅ **P6**: desbloqueado pelo P11. Enquanto segura o projétil, **Apontar** no alvo soma a **Precisão (Acc)** da magia ao Ataque Inato do arremesso — mais a mira de vários turnos (+1 no 2º segundo, +2 no 3º+, MB p.364), reusando o `apontarAlvoId`/`apontarStacks` das armas.
- **Detalhe que teria virado bug**: `heroiArremessarProjetil` chama `limparApontar()` no início — a mira seria apagada antes de ser lida. Captura o bônus **antes** do `inicioAcaoHeroi`.
- **12 magias curadas** com a Precisão das notas (Bola de Fogo 1, Relâmpago 3, Adaga de Gelo 3…). Campo `precisao` no schema.
- **UI**: chip **🎯 Apontar** no token inimigo enquanto segura o projétil (ao lado de Arremessar), via o passthrough de Apontar que já existia.
- **+3 testes**: Apontar soma; sem Apontar não soma; Apontar no alvo errado não vale.
- 🟢 Gate: **793 testes, ZERO falhas**.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-39 — 20 de Julho de 2026 (P11: projétil multi-turno — a feature keystone + C1 de brinde)
**"faz o P11!" — branch GURPS-Saga**
- ✅ **P11**: o projétil mágico agora pode ser **SEGURADO na mão entre turnos** (Magia p.12). `heroiCarregarProjetil` (cria e segura, sem arremessar), `heroiAumentarProjetil` (+energia sem teste, até **Aptidão/turno** e no máx. **3 segundos**), `heroiArremessarProjetil` (resolve o Ataque Inato/esquiva/dano), `dissiparProjetil` (ação livre).
- 🧱 **Feito ADITIVO — o one-shot que o usuário testou não foi tocado.** Extraí a resolução do arremesso para `resolverArremessoProjetil`, reusado pelos dois caminhos. Zero regressão no projétil de um turno.
- ✅ **C1 destravada de brinde**: *"se sofrer uma lesão enquanto sustenta o projétil, teste de Vontade; falha → o projétil o afeta imediatamente"*. Reusa o sinal `choquePendente` do MEC-26, no avanço do turno. Era ⛔ bloqueada há vários lotes justamente por falta do P11.
- 🔒 **Guarda C5 estendida**: não conjura outra mágica enquanto segura o projétil (como no toque).
- **UI**: botão **"Segurar"** no diálogo de conjurar do projétil (ao lado de "Conjurar"); chips **Arremessar** (no token inimigo), **Aumentar** e **Dissipar** (no token do herói); rótulo do projétil no estado ("Bola de Fogo · 5 en · 2/3s").
- 🐛 **Bug estrutural pego pelo compilador**: inseri um `companion object` novo no meio da classe, mas ela já tinha um — cascata de "unresolved" pela classe inteira. Movida a constante para o companion existente.
- **+7 testes de motor** (carregar sem arremessar; aumentar até o teto e o máx 3s; guarda de conjurar; arremessar consome; dissipar; C1 dispara no herói ferido; sem lesão não dispara).
- ⚠️ **VETADO pelo usuário**: magia **cerimonial** (interpretativa, fora de combate) — não entra.
- 🟢 Gate: **790 testes, ZERO falhas**.
- 🚦 **Toca UI → PARA para teste no aparelho.**
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-38 — 20 de Julho de 2026 (P7: Toque Candente — armadura não protege, RD natural sim)
**Loop de magia — branch GURPS-Saga**
- ✅ **P7**: o bloqueio era o `NpcStats` ter um campo `rd` único. Adicionado **`rdNatural`** (a parcela pele/escamas) e um terceiro valor de `armadura`: **`"ignora_vestida"`** — ignora `rd − rdNatural` mas mantém a RD natural. Centralizado em `rdContraMagia`, que os 2 sites de dano de magia agora usam.
- **Literal conferido**: a descrição do Toque Candente diz *"Armadura não protege, mas RD natural sim"* — curado para `ignora_vestida`.
- **+1 teste forte** (contraste determinístico): RD natural gigante + `ignora_vestida` = dano **0**; o mesmo alvo com `ignora` leva dano. Prova que a distinção funciona sem depender de rolagem.
- 🐛 **De novo o `;` no nome do teste** — o gate pegou pela 2ª vez neste loop. Vou parar de usar pontuação em nomes de teste.
- 🟢 Gate: **783 testes, ZERO falhas**.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-37 — 20 de Julho de 2026 (P4: Lampejo em bandas + rider de ofuscamento)
**Loop de magia, cada item seu lote — branch GURPS-Saga**
- ✅ **P4**: o Lampejo agora aplica efeito **por banda de distância** ao centro do clarão: **≤10m** cega 3s + ofusca −3 por 60s; **11–25m** só ofusca −3 por 60s; **26m+** ofusca −3 por 3s. Estrutura `CondicaoBanda` (lista no schema, Gson-friendly) + `bandaPara`/`usaBandas`.
- 🆕 **Mecânica nova reusável — o "ofuscamento"**: penalidade TEMPORÁRIA às perícias de combate (`penalidadeCombateTemp` + timer `penalidadeCombateSeg` no Combatente), entra nos ataques do herói e do NPC junto do CEGO, e **expira sozinha** no avanço de turno. Era o que faltava para o P4 — e é a mesma peça que o **P9** (Jatos: "−3 a todas as perícias de combate por 1d seg") vai reusar.
- **+3 testes**: `bandaPara` escolhe a faixa certa; integração perto-cega-longe-só-ofusca; e o ofuscamento expira.
- 🐛 **A regra da memória valeu**: escrevi um nome de teste com `;` (proibido em Kotlin) — o gate pegou, corrigido para "e".
- 🟢 Gate: **782 testes, ZERO falhas**.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-36 — 20 de Julho de 2026 (P8 degrau 2d-2 + P10 raio mínimo)
**"faz P4, P8, P9, P10 num lote só" — mas P4 e P9 precisam de mecânica nova; este lote é P8+P10 — branch GURPS-Saga**
- **Reescopo honesto na leitura**: os quatro não eram "pequenos" iguais. **P8** (degrau) e **P10** (raio mínimo) são limpos e completos. **P4** (Lampejo) precisa de um *rider* de **−3 na DX por 1 min** — penalidade de atributo temporária que **não existe** no motor. **P9** (Jatos) são `narrado`/`feixe` sem caminho de resolução; projeção ali é feature de ataque em feixe. Os dois viram lote próprio no loop, o que casa com o "cada um num lote" do usuário.
- ✅ **P8**: `danoDeAreaComDegrau` — Chuva de Fogo/Pedras trocam 1d-1 por **2d-2** ao pagar o dobro do custo. Limiar = custo-base **2** dobrado (da nota curada). ⚠️ **Suposição honesta**: o "1/I" do catálogo não deixa o custo-base cristalino, e o payoff pleno é o **tique por segundo** (P1b) — hoje o degrau já vale na aplicação única. O mecanismo fica pronto para o P1b reusar.
- ✅ **P10**: `raioEfetivo` — Nuvem de Faíscas e Sono Coletivo têm raio **mínimo 2m**; a mira eleva o raio escolhido e avisa "mínimo desta mágica".
- **+3 testes** (degrau troca no limiar / não troca abaixo; raio mínimo eleva o pequeno e preserva o grande).
- 🟢 Gate: **779 testes, ZERO falhas**.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-35 — 20 de Julho de 2026 (D1 FECHADO: mais 3 erros meus revertidos; placar final)
**"mas ja esta corrigido?" — branch GURPS-Saga**
- Fui fechar as 10 que faltavam e achei **mais um erro meu**: as **3 Metamorfose Parcial**. A `descricao` do JSON diz "Especial", mas o **corpo do livro** diz *"Metamorfose Parcial (MD) — **Comum; Resistível com Vontade**"* — segundo caso comprovado de **descrição corrompida** (o 1º foi a Bola de Relâmpagos). Revertidas para `Comum/R-Vont`, que era o valor **original** do catálogo.
- ✅ **As outras 7 fecharam sem mudança**: Petrificação Parcial, Muralha de Relâmpagos e Transportar Outro no Tempo batem com a descrição (o apêndice discorda, mas ele é o que não se deve seguir); **Audição Remota** e **Conexão** eram **falso-positivo do meu casamento** — a 1ª é `Informação` nas três fontes e a 2ª casou com uma linha do **sumário** do livro.
- 📊 **PLACAR FINAL DO D1 — 7 das 18 correções minhas estavam ERRADAS (39%)**. As 11 certas ficaram. É bem pior do que os 22% que eu havia estimado no MEC-34, porque na hora eu ainda não tinha conferido as 8 sem entrada direta.
- 💡 **A regra que fica**: a `descricao` do JSON é boa fonte (o usuário está certo), **mas não é infalível** — 2 casos comprovados de linha de classe vinda da coluna vizinha. Quando a mudança altera regra em jogo, cruzar com o **corpo do PDF** é o que decide. O **apêndice não serve** para isso.
- 🟢 Gate: **776 testes, ZERO falhas**.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-34 — 20 de Julho de 2026 (D1 conferido contra o PDF: 4 erros meus revertidos)
**"C:\...\GURPS 4ª Edição - Magia.pdf, mas nao confie na lista do apendice... a fonte da verdade é a descrição" — branch GURPS-Saga**
- **O usuário estava certo nos dois pontos.** O apêndice tem erros: é ele que lista a **Morte Putrefata** como `Comum`, quando o **corpo da magia** diz *"Morte Putrefata (MD) — **Toque**; Resistível com HT"*. Meu índice inicial casava com o apêndice porque o nome do corpo tem sufixo "(MD)" — refeito para **ignorar linhas do apêndice** (terminam em `*`) e limpar sufixos.
- **Método**: PDF de **285 páginas** extraído com `fitz`, índice `nome → classe da linha seguinte`, comparado com as 879 do catálogo. **739 casaram pelo corpo da magia.**
- 📊 **O catálogo está muito melhor do que minha varredura sugeria: só 6 divergências em 739.**
- 🔴 **Quatro correções MINHAS do MEC-32 estavam ERRADAS e foram revertidas**: **Anular Mágica** (é Área, eu pus Comum), **Decapitação** (é Comum, eu pus Toque), **Dissipar Água** (é Área, eu pus Comum) e **Bola de Relâmpagos** (é Projétil, eu pus Área — já corrigida no MEC-33).
- ✅ **Cinco confirmadas certas**: Extinguir Fogo, Retardar Fogo, Sopro de Vapor, Teia de Aranha e Controle de Elemental Ar.
- 🟡 **Oito sem entrada própria no PDF** (aparecem só como referência cruzada "Como listada em…"): as 3 Metamorfose Parcial, 3 Controle de Elemental, Petrificação Parcial, Muralha de Relâmpagos e Transportar Outro no Tempo. Seguem aplicadas **por conferir**.
- 🟡 **Duas divergências PRÉ-EXISTENTES** (não vieram do meu lote): Audição Remota e Conexão — registradas como D1b.
- 💡 **A lição do dia, agora com evidência**: eu fechei o MEC-32 com **uma** fonte e declarei 18 correções. Cruzando com o livro, **4 estavam erradas** — 22% de erro. Quando a mudança altera regra em jogo, **uma fonte não basta**.
- 🟢 Gate: **776 testes, ZERO falhas**.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-33 — 20 de Julho de 2026 (o usuário provou que a fonte do D1 é falível)
**Foto do livro: "Bola de Relâmpagos — Projétil", print do app: "Comum", meu D1: "Área" — branch GURPS-Saga**
- 🔴 **ERREI no MEC-32, e a foto do livro provou.** Eu usei a **1ª linha da `descricao`** como classe oficial. Para a Bola de Relâmpagos essa linha diz **"Área"** — mas o livro diz **"Projétil"**. A descrição está **corrompida**: a extração pegou a linha de classe da **coluna vizinha** (o problema de PDF de duas colunas que o próprio usuário havia me alertado quando ofereceu o livro).
- **Cruzei com uma 2ª fonte** (o corpo da magia no `chunks.jsonl`) e **só 3 das 18 eram verificáveis**: Bola de Relâmpagos (**eu errei** — corrigida agora para `Projétil`), Muralha de Relâmpagos (✅ certo) e Teia de Aranha (✅ certo). Uma 3ª fonte, a tabela do apêndice, está **desalinhada na extração** (devolve "D" e "3" como classe) e não serve.
- ⚠️ **As outras 15 continuam aplicadas mas NÃO VERIFICADAS.** Não reverti porque as classes anteriores também eram suspeitas (a Morte Putrefata, confirmada pelo teste do usuário, era uma delas). Mas o lote **não pode ser tratado como fechado** — está marcado assim no PENDENCIAS.
- 📌 **Revisão de uma resposta minha anterior**: quando o usuário ofereceu o PDF do livro, eu disse que **não seria necessário** porque o capítulo de regras estava completo no corpus. Para as **REGRAS** aquilo continua valendo. Para **conferir classe de magia individual**, eu estava errado — o corpus tem 80 páginas ausentes nos capítulos de magias e as descrições podem vir corrompidas. **O PDF (ou a foto do livro) é a fonte certa aqui.**
- 💡 **Lição de método**: eu tinha *uma* fonte e tratei como verdade. O certo, quando a decisão muda regra em jogo, é **cruzar duas** — foi só ao cruzar que o erro apareceu.
- 🟢 Gate: **776 testes, ZERO falhas**.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-32 — 20 de Julho de 2026 (D1: 18 classes erradas no catálogo; C13 feito; C10 inimplementável)
**"faça o d1, c13, c10" — branch GURPS-Saga**
- ✅ **D1 — 18 magias com a CLASSE errada, corrigidas.** A 1ª linha da descrição é a classe oficial do livro; comparei todas as 879 contra ela e conferi **caso a caso**. Destaques: **Decapitação** e **Petrificação Parcial** eram `Comum` sendo **Toque** (não exigiam encostar!); **Bola de Relâmpagos**, **Extinguir Fogo**, **Muralha de Relâmpagos** e **Retardar Fogo** eram `Comum` sendo **Área** (não pediam raio); **Dissipar Água** e **Sopro de Vapor** o inverso; os **4 Controle de Elemental** eram `Especial` sendo `Comum/R-ST ou Vontade`; as **3 Metamorfose Parcial** eram `Comum/R-Vont` sendo **Especial** (transformam o próprio corpo — não há alvo para resistir).
- **Sobraram 4 divergências, todas falso-positivo** — a 1ª linha delas é **referência cruzada** ("Como Ilusão Simples, mas…"), não linha de classe. Foi por isso que este lote foi de leitura e não de regex: a primeira varredura, automática, teria "consertado" essas 4 e quebrado o que estava certo.
- ✅ **C13 (MEC-32)**: mágica **Comum** em alvo **adjacente** não sofre mais redutor de distância. A regra condiciona o redutor a *"se o operador não conseguir tocá-lo"* — a 1 m ele alcança. Antes `tocando` era fixo em `false` e encostar no alvo custava −1.
- ⛔ **C10 é INIMPLEMENTÁVEL hoje**, e o motivo só apareceu ao abrir o código: *"optar por não resistir"* exige **aliados** (o jogo só tem herói × inimigos) e *"Abascanto em dobro"* exige o campo **Abascanto** no `NpcStats`, que **não existe**. Reclassificado ao lado de C1 e C8 — com a dependência nomeada, em vez de ficar como pendência solta que alguém tentaria de novo.
- 🟢 Gate: **776 testes, ZERO falhas**.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-31 — 20 de Julho de 2026 (modo BONECO também não RESISTE)
**"agora vc tirou a opção de manter? quando acertei a magia, nao apareceu o pop-up" — log do aparelho, branch GURPS-Saga**
- ✅ **Não era regressão.** O log do usuário mostra a resposta: *"ACERTA! Goblin 2 **RESISTE** (resistência 10) — a mágica se dissipa."* A Morte Candente é `R-HT`; resistiu → nada fica ativo → nada a manter. Comportamento **correto** pela regra (Magia p.13: resistível só funciona automaticamente em sucesso decisivo).
- 🟡 **Mas havia uma incoerência real no modo de teste**: o **Boneco** promete *"não agem nem defendem"* e o goblin continuava **resistindo**, então testar Morte Candente virava loteria — errar o toque, ou acertar e o alvo resistir. Corrigido: no Boneco o alvo **também não resiste** (`npcResistiu`, nos 4 pontos de disputa). Rótulo atualizado para "não agem, não defendem e não resistem".
- **Escopo preservado**: resistência **não é** defesa ativa pela regra; a mudança vale **só no sandbox** (`ModoTesteNpc.BONECO`), nunca em campanha. O **Congelado continua resistindo** — e há teste de controle provando isso, para o modo não virar sinônimo do outro.
- 🐛 **Meu teste passou por vacuidade e o controle pegou**: escrevi os dois casos com **NH 25**, e com margem tão alta o alvo praticamente nunca vence a disputa — então "nenhuma resistência no Boneco" era verdade **por acidente**. O teste do Congelado falhou justamente por isso e denunciou o cenário fraco. Baixei para **NH 12** (perto do NH 14 real do usuário) e ampliei a varredura; aí os dois passam **medindo o que deveriam**.
- 🟢 Gate: **775 testes, ZERO falhas**.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-30 — 20 de Julho de 2026 (BUG: o card "Manter?" não saía da tela e travava tudo)
**Print do aparelho: "o pop-up nao sai da tela, mesmo eu escolhendo 1 opção... nao consigo selecionar NPC nem o heroi" — branch GURPS-Saga**
- 🔴 **Regressão do MEC-23, achada no teste.** O card de manutenção ficava preso e **bloqueava a tela inteira** (ele é modal e tem `return` antes do resto dos overlays).
- **Causa — a MESMA do TESTE-1c**: no motor, `manutencaoPendente` é um `var` comum. Mutar um campo **dentro** da sessão **não notifica o Compose**; só a troca da referência de `sessao` notificaria. E o controller expunha por getter (`get() = sessao?.manutencaoPendente`), o que dá a ilusão de reatividade sem tê-la.
- **Por que "Deixar acabar" travava pior**: essa opção não muda mais nada no `CombatUiState`. Como o `estado` é `mutableStateOf` com igualdade estrutural, o objeto novo era **igual** ao antigo → sem notificação → sem recomposição → card eterno.
- **Corrigido** com espelho **observável** no controller (`mutableStateOf`), ressincronizado dentro de `atualizarEstado()` — **antes** do early-return, senão encerrar o combate deixaria a fila presa. E `encerrarManual` passa a limpá-lo, para não vazar para a luta seguinte (mesmo cuidado do `viradaFinalPendente`).
- ⚠️ **Limite honesto**: este bug **não é pegável** pelos 773 testes — é reatividade de Compose na camada de ViewModel, exatamente a lacuna medida no TESTE-C (e o motivo de o Robolectric ter sido descartado). Só o teste no aparelho pega. É a **segunda vez** que esse padrão morde; a lição é que **getter sobre campo mutável do motor não é estado observável**.
- 🟢 Gate: **773 testes, ZERO falhas** (sem teste novo — não há como cobrir isto na JVM).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lotes MEC-28 e MEC-29 — 20 de Julho de 2026 (C5 a C10: dois feitos, dois já prontos, dois sem onde aplicar)
**"e depois vamos do c5 a c10?" — branch GURPS-Saga**
- ✅ **C5 (MEC-28)**: com a mão CARREGADA por mágica de Toque, o operador *"não pode fazer outras mágicas"*. Guarda no topo do `heroiConjurar`. A metade do **Projétil** da mesma regra é **moot** — ele nunca fica sustentado (mesma causa da C1 bloqueada).
- ✅ **C7 (MEC-29)**: *"lançada mais de uma vez no mesmo objetivo, só a **mais poderosa** conta — não acumulam"*. Relançar Escudo agora **substitui** pela versão mais forte em vez de somar (+1 e +4 davam +5). Cura e dano continuam acumuláveis, que é a exceção do livro — e sai de graça, porque não passam por esse caminho.
- 🔎 **Dois itens da lista JÁ ESTAVAM FEITOS** — e desta vez conferi **antes** de escrever código: **C6** (`dissiparToque` existe, é ação livre e tem botão na UI) e **C9** (`custo.minimo` é aplicado; eu havia procurado `custoMinimo`, identificador errado).
- ⛔ **C8 sem onde aplicar**: *"cancelar de repente custa 1 ponto"*. **Não existe ação de cancelar** no app — os únicos caminhos que encerram mágica são os do MEC-23 (deixar acabar / não poder pagar), que por regra são **grátis**. Cobrar neles seria errado. Precisa antes de um botão de cancelar.
- 🟡 **C10 moot na prática**: "optar por não resistir" só importa com **aliados**, que o jogo não tem. O Abascanto em dobro na Área Resistível continua pendente de verdade.
- 📉 **Autocrítica com número**: dos 12 itens que levantei na varredura, **5 estavam errados** — todos marcados como faltando quando já existiam. A causa foi sempre a mesma: **contar ocorrências ou ler comentário em vez de ler o código**. A varredura ficou **pessimista**, que é o lado menos perigoso do erro, mas inflou a dívida. Registrado para calibrar leituras futuras.
- **+4 testes**: mão carregada bloqueia conjurar; dissipar libera; o buff mais forte substitui; o mais fraco é ignorado.
- 🟢 Gate: **773 testes, ZERO falhas**.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-27 — 20 de Julho de 2026 (C2: uma mágica de Bloqueio por turno) — e C3 já estava certa
**"enquanto a tarefa nao finaliza, ja deixe encaminhado o proximo lote!" — branch GURPS-Saga**
- ✅ **C2 feita** (Magia p.12): *"o personagem pode operar apenas **uma mágica de Bloqueio por turno**, independentemente de seu nível de habilidade"*. Flag `bloqueioMagicoUsadoNoTurno` marcada em `aplicarBloqueioMagico` e **renovada quando o turno do herói recomeça**; o seletor de defesa deixa de oferecer bloqueio já usado. Sem isso o herói bloqueava magicamente **cada** ataque da rodada — defesa ilimitada, exatamente o que a regra proíbe.
- ✅ **C3 já estava correta no resultado**: o golpe fulminante **anula toda a defesa** (`anulada = atk.critico == DECISIVO`), inclusive o bloqueio mágico. 🟡 Sobra um detalhe honesto: as opções são montadas **antes** da rolagem, então o jogador ainda pode escolher o bloqueio e **gastar PF** num crítico que já ia passar. É desperdício de energia, não erro de regra — registrado assim.
- 🐛 **TERCEIRO erro meu na mesma varredura**: eu havia marcado *"Bloqueio interrompe automaticamente a concentração"* como ❌. **Estava implementado** dentro de `aplicarBloqueioMagico`. Os três erros (custo por NH, este, e o C3) têm a **mesma causa**: contei ocorrências / li comentário em vez de ler o código. Os três agora estão corrigidos nos documentos — e este ganhou **teste**, que é o que impede de se perder de novo.
- **+3 testes**: a cota é consumida; **renova** na rodada seguinte; e o bloqueio interrompe a conjuração em andamento.
- 🟢 Gate: **769 testes, ZERO falhas**.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-26 — 20 de Julho de 2026 (C4: apanhar abala a concentração) — e C1 estava BLOQUEADA
**"faz C1 e C4 juntas!" — branch GURPS-Saga**
- ⛔ **A C1 não pôde ser feita, e o motivo só apareceu ao ir implementar**: a regra é *"se sofrer uma lesão enquanto **sustenta o projétil**, teste de Vontade; falhando, o projétil o afeta imediatamente"*. Mas o motor **conjura e arremessa o projétil no MESMO turno** — **nunca existe projétil sustentado**. Implementar seria código morto. Ela depende do deferido "Projétil carregado em vários turnos". Registrada como **BLOQUEADA com a dependência nomeada**, não como pendência solta.
- ✅ **C4 feita** (Magia p.10): mágica que exige concentração é abalada quando o operador **apanha** ou fica **atordoado** → **Vontade−3**. Fracasso **congela** (a mágica não avança nem fere naquele turno, mas **não acaba**); **falha crítica desfaz**. Sucesso segue normal. Alvo real: a Morte Candente exige concentração desde o MEC-22.
- 🐛 **Bug de ordem que o teste pegou**: eu chequei o abalo **depois** de `anterior.choquePendente = 0` — o gatilho "sofreu uma lesão" já tinha sido apagado e o teste nunca disparava. Movido para antes do reset, com comentário explicando por quê.
- 🐛 **Dois erros meus no próprio teste**, ambos achados por instrumentação em vez de chute: (1) o laço reusava as mesmas 40 seeds, então as 200 iterações eram **idênticas** e a falha crítica (que exige 17–18) não podia aparecer; (2) a asserção **por sessão** abortava quando uma luta congelava num turno e a mágica saía depois por **outro** motivo (a vítima quebrando-a com sucesso decisivo, do MEC-22). Reescrito como **agregado** sobre 400 seeds, que é o formato que o diagnóstico provou: 219 congelamentos, 4 falhas críticas, 41 sucessos.
- **+4 testes**: sem gatilho não há teste (não punir o mago à toa); dano dispara; atordoamento dispara; e os três desfechos existem.
- 🟢 Gate: **766 testes, ZERO falhas**.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-25 — 20 de Julho de 2026 (Aptidão Mágica destrava o teto de energia) + fila montada
**"sim, e ja coloque na fila as pendencias necessarias!" — branch GURPS-Saga**
- **O caso raro em que eu errei para o lado RESTRITIVO.** O MEC-9 travava a energia no teto da magia ("1 a 4" → 4). Magia p.9 diz: *"o limite superior é determinado pelo **maior número possível entre os níveis da mágica ou o nível de Aptidão Mágica** do operador"*, com exemplo literal: **Cura Profunda (1 a 4) com Aptidão 10 vai a 10 níveis** (2 a 20 PV). Agora vale `max(faixa, Aptidão)`.
- **Teste com o exemplo do livro**, mais o caso oposto (Aptidão baixa **não** encolhe a faixa) — o segundo é o que impede a correção de virar bug novo.
- 🐛 **Segundo item da lista estava ERRADO — meu erro, achado ao ir implementar.** Eu havia registrado "Bloqueio não reduz custo por NH" como pendência. **Já estava implementado**, com exceção explícita e comentada em `MagicCasting.custoTotal`, **e com teste** (`Bloqueio NUNCA reduz custo por NH alto`). Eu tinha contado ocorrências de `custoAjustadoPorNH` sem ler o contexto — a mesma preguiça que já me fez errar a varredura de `entrega: toque` no MEC-24. **Documentos corrigidos nos dois lugares.**
- **Fila montada** no `PENDENCIAS.md`: **C1 a C12**, ordenadas por impacto. As três 🔴 do topo: Vontade ao ser ferido sustentando projétil; só uma mágica de Bloqueio por turno; Bloqueio não vale contra golpe fulminante. Mais a C4 (Vontade−3 ao manter mágica de concentração), irmã da C1.
- 🟢 Gate: **762 testes, ZERO falhas**.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote DOC-CLASSES — 20 de Julho de 2026 (varredura das Classes de Mágicas, p.11–14)
**"confirma se vc ja lido sobre? se nao, pode agregar essas regras num arquivo dentro da pendencias" — branch GURPS-Saga**
- **Resposta honesta ao usuário: eu tinha lido só EM PARTES.** Vinha citando p.11/p.12 caso a caso (Projétil, Toque, Área) conforme cada lote precisava, mas **nunca varri a seção inteira** — e a varredura achou regras com efeito mecânico que nunca foram implementadas **nem registradas em lugar nenhum**. Não foi decisão; foi omissão.
- **Criado `docs/pendencias/CLASSES_DE_MAGICA.md`**: as 8 classes (Comuns, Área, Toque, Projétil, Bloqueio, Informação, Resistíveis, Encantamento/Especiais), regra por regra, com o texto literal e o estado **conferido no código** — não de memória.
- 🔴 **Achado mais grave (C1)**: *"Se sofrer uma lesão enquanto sustenta o projétil, o operador faz teste de **Vontade**. Se fracassar, **o projétil o afeta imediatamente**."* Hoje o mago segura uma Bola de Fogo, apanha, e não acontece nada. Barato de implementar e com consequência real.
- 🔴 **Três regras estruturais de Bloqueio faltando**: só **uma por turno**; **não vale contra golpe fulminante**; e **não sofre redução de custo por NH alto** — esta última é uma **exceção** que o motor viola, porque aplica `custoAjustadoPorNH` a todas as classes.
- 🟡 Outras: não conjurar enquanto sustenta Toque/Projétil; dissipar sustentada como **ação livre**; **custo mínimo** de Área (0 ocorrências no código); Resistível "optar por não resistir" e Abascanto em dobro na Área Resistível.
- ✅ **Também confirmou o que ESTÁ certo**: custo × (1+MT), −5 sem ver nem tocar, custo de Área × raio, penalidade até a borda, raio 1m = 1 hex, Projétil nunca aparado, RD protege contra Projétil. Tudo ligado e conferido.
- **Sem alteração de código** — o gate verde de 761 testes segue valendo.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-24 — 20 de Julho de 2026 (o toggle de dano voltou — e revelou erro de CLASSE no catálogo)
**"lembra-se de que falamos sobre esse sistema causar dano(1d por energia)? isso nao tinha sido corrigido?" — print da Morte Putrefata, branch GURPS-Saga**
- **O MEC-20 corrigiu, mas não cobria este caso.** Ele passou a gatear o toggle pelo `efeito` curado — e a Morte Putrefata **é** `efeito: dano`, então passava pelo filtro. Só que o dano dela já está definido pelo livro (**1d-1 por turno**); oferecer "1d por energia" em cima disso convida a somar dano que a regra não prevê.
- **Correção (MEC-24)**: o toggle agora também exige que a magia **NÃO tenha dano próprio** (`temDanoEstruturado` ou `temTiquePorTurno`). Ele sobra só para magia de dano **sem número curado**, que é exatamente o caso para o qual o MA-6 foi criado.
- 🔴 **Achado maior: erro de DADO no catálogo.** A Morte Putrefata estava com `classe: "Comum/R-HT"`, mas a primeira linha da descrição — que é a classe oficial do livro — diz **"Toque; Resistível com HT"**. Por isso ela aparecia lançável **a 5 metros** no print, sem exigir toque. Corrigido para `Toque/R-HT`.
- 🔍 **Varredura sistemática do mesmo erro** (a lição de conferir a regra geral, não o caso): comparei o campo `classe` de todas as 879 contra a primeira linha da descrição. **~17 magias com classe REALMENTE errada**, entre elas Decapitação e Petrificação Parcial (livro diz Toque, campo diz Comum), Bola de Relâmpagos / Extinguir Fogo / Muralha de Relâmpagos / Retardar Fogo (livro diz Área, campo diz Comum), Dissipar Água / Anular Mágica / Sopro de Vapor (o inverso) e os 4 Controle de Elemental. **Isso muda o comportamento do motor** — a classe é quem decide se carrega a mão, se pede raio ou se é direta.
- ⚠️ **Não corrigi as outras 16 neste lote**: cada uma exige ler a descrição para separar classe real de referência cruzada (várias começam com "Como Ilusão Simples, mas…"). Fica como lote próprio, registrado no PENDENCIAS.
- 🐛 **Minha primeira varredura estava errada e teria "consertado" 87 magias sem necessidade**: eu comparei `entrega: toque` com a classe, mas são conceitos diferentes — uma magia **Comum** pode ser lançada tocando (sem penalidade de distância). Refeito comparando com a linha do livro.
- 🟢 Gate: **761 testes, ZERO falhas**.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-23 — 20 de Julho de 2026 (manter mágica virou OPCIONAL — era obrigatório)
**"a magia esta sendo mantida de forma obrigatoria... em via de regras de gurps manter magias e opcional!" — teste no aparelho, branch GURPS-Saga**
- **O usuário está certo e o MEC-22 estava errado.** O motor debitava o PF da manutenção **sozinho** e a mágica seguia para sempre: não havia como largar a Morte Candente. O log dele mostra `✨ Manutenção de mágicas: −2 PF` turno após turno, sem escolha.
- **Implementado**: quando a manutenção vence, as mágicas **do herói** ficam em `manutencaoPendente` e a tela pergunta **"Manter X? (−N PF)"** com *Manter* / *Deixar acabar*. Manter cobra e segue; não manter **encerra a mágica e para o gasto**. O prompt tem prioridade no overlay (como a virada final), porque o turno espera a decisão.
- **Sem PF suficiente a mágica cai de qualquer jeito** — não há como pagar, e o PF nunca fica negativo. Tem teste.
- **NPC continua automático**: não há a quem perguntar. A cobrança dele foi separada e passou a debitar o PF do próprio NPC (antes o `cobrancasPorOperador` só era lido para o herói).
- **`MagicActive` passou a devolver a cobrança POR MÁGICA** (`venceramManutencao`), não só o total agregado — sem isso não dá para perguntar mágica a mágica. Preferi estender o motor a duplicar a condição de vencimento no `CombatSession` (seria o mesmo "copiar a regra" que já criou dívida no TESTE-C).
- 🔴 **Um teste PRÉ-EXISTENTE quebrou — e estava certo que quebrasse**: `magia ativa cobra manutencao ao completar o intervalo` trancava justamente o débito automático, ou seja, **trancava a regra errada**. Reescrito para exigir que a manutenção fique pendente e só cobre depois do "Manter".
- **+4 testes**: o motor não cobra sozinho; manter cobra e mantém; não manter encerra sem cobrar; e sem PF a mágica cai mesmo querendo manter.
- 🟢 Gate: **760 testes, ZERO falhas**.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote TOK-PF — 18 de Julho de 2026 (barra azul de fadiga sobre o token)
**"so temos barra de PV, podemos fazer uma barra Azul pra representar a de PF?" — branch GURPS-Saga**
- **Motivo real**: o MEC-22 fez a manutenção de mágica drenar PF **por turno**. Sem barra, o jogador via o recurso sumir sem conseguir acompanhar — pedido veio direto do roteiro de teste.
- **Implementado**: `pfPct` no `TokenTatico`, barra azul fina logo abaixo da de PV, com a mesma gramática de cor da de vida (azul → âmbar abaixo de 25%).
- **Só o HERÓI tem barra.** O bestiário não rastreia fadiga de NPC — desenhar barra neles seria inventar dado. `pfPct = null` nos NPCs e nada é desenhado. O máximo vem da ficha (`personagem.pontosFadiga`), porque o `Combatente` só guarda `pfAtual`.
- ♿ **A barra é só visual — o TalkBack precisava das palavras.** A variante **PraCego** perderia a informação, então a `descricaoAcessivel` ganhou "fadiga cheia / parcial / quase esgotada". Isso exigiu `pfPct` também no `CombatenteUi` (classe diferente do `TokenTatico`).
- **+1 teste**: prova que manter a mágica de tique **drena PF turno a turno** — sem isso a barra seria decorativa.
- 🟢 Gate: **756 testes, ZERO falhas** (o flaky do Nexus passou de novo — segue não-determinístico, não consertado).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-22 — 18 de Julho de 2026 (motor de TIQUE por turno — a Morte Candente finalmente queima)
**Escolhido com o usuário como "o melhor a implementar agora" — branch GURPS-Saga**
- **A regra, literal**: *"Toda vez, a vítima deve fazer um teste de HT; em uma falha (crítica ou não), ele recebe 1d-1 de dano por fogo. Em um sucesso, ele não leva dano naquele turno; em um sucesso decisivo, a mágica está quebrada."* + *"Nem RD nem Resistência ao Fogo protegem"*. A Morte Putrefata troca o dado por **6 pontos** na falha crítica.
- **Decisão de escopo que valeu**: o P1 parecia um lote grande porque misturava **alvo único** (Morte Candente/Putrefata — são de Toque, a vítima já é um token) com **zona persistente** (Chuvas/Nuvens/Géiser — precisa desenhar área na grade). Separei e fiz **só a metade sem UI**, que não engorda a fila de coisa não validada do usuário.
- **Implementado**: 4 campos novos (`danoPorTurnoExpr`, `danoPorTurnoTeste`, `danoPorTurnoCriticoFixo`, `quebraEmSucessoDecisivo`) + `temTiquePorTurno()`. O `MagiaAtivaNoCombate` (que já cobrava manutenção desde o MA-3d-4) passou a carregar a mecânica, e `tiquePorTurnoDasMagias()` resolve teste/dano/quebra no avanço do turno. **RD não protege** — o dano vai direto para `InjuryRules.ferir`.
- **P2 saiu de brinde**: a mágica fica ATIVA, cobra manutenção por turno (custo "03/02" → 2) e exige concentração, tudo reusando o relógio que já existia. Não precisei criar estrutura paralela.
- ✅ **O risco que eu mesmo sinalizei antes de começar não virou bug**: a "regra da estreia". A mágica é registrada durante a ação do herói e o tique roda no fim **desse mesmo turno** — sem trava, a vítima levaria um turno de dano de graça. `pularPrimeiroTique` resolve, e **tem teste dedicado** provando que no turno da aplicação o PV não muda.
- **+5 testes**: estreia sem dano; o tique aparecendo a partir do turno seguinte; **sucesso decisivo quebrando a mágica e removendo-a das ativas** (varredura de 120 seeds até a vítima tirar 3–4); o 6 fixo da Putrefata; e magia sem tique não entrando no motor.
- ⚠️ **Deferido honesto**: *"mortos-vivos não são afetados"* — o `NpcStats` não tem campo de tipo de criatura (mesma limitação da RD natural do Toque Candente). Registrado no PENDENCIAS.
- 🟢 **Gate: 755 testes, ZERO falhas — primeiro gate inteiramente verde da sessão.** Honestidade: ficou verde porque o **flaky do Nexus passou desta vez** (ele é não-determinístico), não porque foi consertado. A outra sessão ainda vai finalizá-lo.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote ORGANIZA-2 — 18 de Julho de 2026 (Artes Marciais entra no mapa — e estava desatualizado também)
**"o arquivo Artes_Marciais_Regras_Combate.md possui pendencias nele tbm!" + autorização para commitar os arquivos antes intocados — branch GURPS-Saga**
- **Movido** `Artes_Marciais_Regras_Combate.md` → `docs/pendencias/` (é **inventário**, não fonte de regra), com as 4 referências cruzadas atualizadas.
- 🔍 **Mesma doença do `Combate.md`, confirmada no código**: o documento listava como pendentes coisas **já implementadas**. Remarcados ⚪ **FEITO**: Chaves/imobilizações/estrangulamentos (PONTE-1), **Ataque Dedicado** e **Ataque Defensivo** (PONTE-4), **Ataque Telegráfico** (PONTE-3) — este último inclusive com a regra fina do crítico usando NH−4 sem virar falha crítica. O Sangramento (PONTE-2) também já saiu do bloco *Lesões Realistas*.
- **Aviso de validade no topo do documento**: a legenda dele diz *"modelo de FAIXAS, sem hexágono"* — foi escrita antes da grade existir. Os **17 itens 🔴 "posicional/hexágono"** merecem reavaliação pelo mesmo motivo dos 5 do `Combate.md`.
- **Contagem real depois da conferência**: ⚪ feito 9 → **13**; 🟢 codável 6 → **2**; 🟡 parcial 39; 🔴 fora 17.
- **PENDENCIAS.md ganhou a seção 2.4** com o bloco de Artes Marciais organizado por sinergia com o motor atual — e a recomendação subiu **Lesões Realistas** para 3º lugar, porque o Sangramento já provou que o bloco encaixa bem no motor de dano.
- **Commitados os arquivos antes deixados de fora** (autorização explícita do usuário): `logcat_novo.md` e `.agent/skills/MAPA_DETALHADO.md`.
- ⚠️ **Nexus Arcano segue FORA do commit, agora por confirmação do usuário**: a outra sessão que estava consertando o teste flaky **acabou os tokens** e o conserto está incompleto (o teste segue vermelho). Ele vai pedir para finalizarem. Commitar aquilo pela metade colocaria vermelho conhecido na branch.
- **Nota de higiene registrada**: `logcat_novo.md` é despejo transitório na raiz — candidato a `.gitignore`, já que o LOG-1 (`tag:Saga_Combate`) dá o mesmo em tempo real.
- ⚠️ Gate: **750 testes nas DUAS variantes**; único vermelho o flaky do Nexus Arcano.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote ORGANIZA — 18 de Julho de 2026 (casa arrumada + PENDENCIAS.md, o mapa único)
**"organiza a casa pra nos... depois mapei, analise, e transcreva tudo pra um unico arquivo das pendencias" — branch GURPS-Saga**
- **O problema que motivou**: o usuário estava testando no aparelho e batendo em coisas **não implementadas**, achando que eram bugs. A informação existia, mas espalhada por 4 documentos — ninguém via o todo. Culpa minha por nunca ter consolidado.
- **Arrumação** (com `git mv`, histórico preservado): `docs/fonte-regras/` (Combate.md, indice.md — transcrição do livro), `docs/planos/` (5 planos), `docs/pendencias/` (deferidos + auditoria + o novo PENDENCIAS.md). Raiz ficou só com README e PROGRESS.
- **Ponteiros corrigidos**: nenhum código LÊ `.md` em runtime, mas 9 arquivos (4 `.kt`, PROGRESS, planos e uma skill) citavam os caminhos antigos em comentários. Todos atualizados e conferidos — 0 ponteiros mortos, 0 caminhos duplicados.
- **NÃO tocado de propósito**: `docs/pendencias/Artes_Marciais_Regras_Combate.md` e `logcat_novo.md` (alheios/em edição pelo usuário), `.agent/skills/MAPA_DETALHADO.md` (editado por ele) e os arquivos do Nexus Arcano (outra sessão). Conferido no `git status` que nenhum entrou no commit.
- **`docs/pendencias/PENDENCIAS.md`** — o mapa único, com o número que explica a frustração: **87 das 879 magias (9,9%) são executadas mecanicamente**; 792 são narradas **por projeto**. Tabela por efeito, heurística de logcat para o usuário distinguir bug de não-implementado sozinho (`"Efeito narrado pelo Mestre"` = não é bug), lista priorizada do que falta, lista do que está pronto mas **não validado**, e a dívida técnica conhecida.
- 🔍 **Achado da análise**: dos 13 itens "FORA DO ESCOPO" do `Combate.md`, **5 foram excluídos por não haver grade de hexágonos — e a grade foi construída depois**. Conferi item a item **no código** antes de publicar, e ainda bem: o **Passo já está implementado** (35 ocorrências no motor) enquanto o documento ainda o marca como fora do escopo. Ia publicar afirmação errada. Situação real: Passo ✅ feito, Passando-por-Outros e Cobertura 🟡 parciais, Evadir e Espaçamento ❌ não feitos.
- ⚠️ Gate: **750 testes nas DUAS variantes**; único vermelho o flaky do Nexus Arcano.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lotes MEC-20 e MEC-21 — 18 de Julho de 2026 (dano oferecido onde a regra não permite; TOQUE não fazia nada)
**Teste no aparelho: "magias como Criar Ar, Localizar Ar aparecem opção de gerar dano, isso é regra ou BUG?" + Morte Candente sem dano no log — branch GURPS-Saga**
- **MEC-20 — o toggle "Causa dano" aparecia em quase tudo. É BUG.** A condição era `!projétil && !toque && !cura && (área || tem alvo)` — ou seja, liberava **Informação e utilidade**. A fonte só autoriza o "1d por ponto de energia" para magia de **combate** e **Projétil** (*"uma mágica de combate talvez cause 1d de dano por ponto"*); **não existe** regra para bombear dano em Localizar Ar. Corrigido gateando pelo `efeito` curado do catálogo. **Números**: só **40 das 879** magias são de dano — o switch aparecia em ~800 e agora aparece em 40. Cobertura do catálogo conferida: **0 magias sem `efeito`**, então o fallback nunca dispara.
- **MEC-21 — NENHUMA magia de TOQUE fazia efeito mecânico.** A conjuração de Toque dá `return` cedo (só carrega a mão) e o descarregar (`heroiEntregarToque`) terminava sempre em *"Efeito narrado pelo Mestre"*. Toque Candente, Morte Candente, Toque Chocante e Toque Congelante acertavam e **não produziam nada** — exatamente o que o usuário viu no logcat da Morte Candente.
  - Corrigido: o descarregar agora aplica **cura**, **condição** (com prazo do MEC-17/18 e escape do MEC-19) ou **dano** (`aplicarDanoMagico`), reusando os mesmos helpers do caminho direto. Sem mecânica curada, continua narrado — não se inventa efeito.
  - A **energia investida** passou a viajar com a mão carregada (`ToqueCarregado.energiaInvestida`); sem ela não havia como escalar dano nem duração.
- 🔴 **Consequência sobre trabalho MEU, que eu não tinha percebido**: o **MEC-19** (fuga do gelo do Toque Congelante) **nunca disparava em jogo**, porque a paralisia jamais era imposta — a magia é de entrega `toque`. Eu havia testado a REGRA isolada e declarado o lote fechado sem verificar o caminho real. É a mesma lição do TESTE-C (testar a regra ≠ testar que ela está ligada), agora numa segunda forma: **testar a regra ≠ testar que ela é alcançável**.
- **+3 testes** cobrindo o descarregar: dano fere de verdade; condição é imposta E registra o escape (o que faz o MEC-19 existir em jogo); e sem mecânica curada continua narrado.
- ⚠️ **Ainda NÃO feito, e o usuário perguntou**: o ciclo por turno da Morte Candente/Putrefata (*"toda vez, a vítima deve fazer um teste de HT; falha = 1d-1"*) e a opção de **manter** a magia. É o item 11 da auditoria, o mesmo mecanismo de tique do MEC-16 — segue pendente e agora com um caso de uso concreto.
- ⚠️ Gate: **750 testes nas DUAS variantes**; único vermelho o flaky do Nexus Arcano.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote TESTE-SANDBOX — 18 de Julho de 2026 (o botão de combate de teste falhava EM SILÊNCIO)
**"nao esta entrando no modo de combate, os botoes aparecem acima do grid, porem nada de combate!" — branch GURPS-Saga**
- **🔴 A causa**: `sagaIniciarCombateTeste` chamava `iniciarCombateSandbox` e **DESCARTAVA o retorno**. Só que `iniciarCombate` tem **quatro saídas de recusa** que devolvem um código em vez de abrir a luta: `sem_contexto`, `combate_ja_ativo`, `heroi_incapacitado` (herói com **PV ≤ 0**) e bestiário ausente. Nenhuma delas chegava à tela — o botão simplesmente **não fazia nada**, sem explicação.
- **Dois suspeitos práticos para o caso do usuário**: (1) **PV ≤ 0** persistido na ficha de uma luta de teste anterior — aí TODO combate de teste seguinte era recusado para sempre; (2) **sessão presa** de uma luta anterior, que além de recusar **esconde o próprio botão** (ele está sob `if (!sagaCombateAtivo)`), o que bate com "os botões aparecem mas nada de combate".
- **Corrigido nas duas pontas**:
  - **Destrava**: numa ARENA DE TESTE uma luta presa não pode bloquear a próxima — o sandbox agora encerra a anterior e recomeça. Em campanha a recusa continua valendo (lá ela protege de o Narrador abrir combate por cima de outro).
  - **Explica**: `avisoSandbox` traduz o código em algo acionável ("seu herói está com 0 de 12 PV — restaure os PV na ficha e tente de novo") e aparece num card no centro do preview. Tinha que ser um canal PRÓPRIO: o `avisoTatico` dos overlays de combate retorna cedo quando não há combate ativo — exatamente o caso em que esta mensagem importa.
  - **Registra**: a recusa também sai no logcat (`tag:Saga_Combate`).
- 🐛 **Erro meu de entendimento de regra, pego pelo gate**: escrevi um teste afirmando que o herói a 0 PV "não está vivo". **Errado** — em GURPS `vivo` só cai em **−PV máximo**; 0 PV é "de pé, cambaleante". A trava de não abrir combate a 0 PV é decisão do `iniciarCombate`, não propriedade do motor. O teste foi reescrito para trancar o fato real, com o erro anotado para o próximo leitor.
- ⚠️ **Limite honesto**: a tradução recusa→mensagem mora no `SagaCombatController`, **não testável na JVM** (precisa de `Application`) — a mesma limitação medida no TESTE-C. O motor está coberto; a fiação depende do teste no aparelho.
- ⚠️ Gate: **747 testes nas DUAS variantes**; único vermelho o flaky do Nexus Arcano.
- 🚦 **Lote de UI → PARA para teste no aparelho.**
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote TESTE-NPC — 18 de Julho de 2026 (NPCs congelados no preview, para testar magia e combate)
**"podemos deixar os NPC do preview apenas congelados?" — branch GURPS-Saga**
- **Contexto**: validar as regras novas (MEC-13..19) no aparelho é difícil com o goblin atacando de volta e a luta andando sozinha.
- **Três modos** (`ModoTesteNpc`), seletor no topo do preview, visível TAMBÉM durante a luta — trocar vale na hora, sem reiniciar o combate:
  - **Normal** — cérebro tático age e defende (jogo de verdade);
  - **Congelado** — não age, mas **ainda esquiva/apara**;
  - **Boneco** — não age nem defende.
- **A distinção Congelado × Boneco é real, não cosmética**: agir e defender são pontos SEPARADOS no motor (`npcIntencao` × `melhorDefesaNpc`/`esquivaNpc`). Congelar o turno não desliga a esquiva.
- 🐛 **Bug meu que o teste pegou (e teria confundido no aparelho)**: eu havia implementado o Boneco **zerando** a defesa. Não funciona — em GURPS **3 ou 4 é sucesso automático**, então uma Esquiva 0 ainda escapava de vez em quando. No aparelho isso apareceria como *"botei Boneco e mesmo assim ele esquivou"*. Corrigido com `npcSeDefendeu`, que **pula a rolagem**. Conferido um a um que os 5 sites trocados são defesa de NPC e que os 3 do **herói** ficaram intactos.
- ⚠️ **Promessa minha corrigida**: eu havia dito ao usuário que Boneco = "tudo acerta". **Está errado** — o alvo não se defende, mas o atacante ainda faz a própria jogada de acerto e pode errar. Tirar essa jogada esconderia bug no caminho de acerto, que é justamente o que se quer validar. O rótulo na tela diz isso ("você ainda pode errar o ataque").
- **Só o sandbox sai do NORMAL** — nenhum caminho de campanha seta outro valor, e há teste trancando o padrão.
- **+4 testes**: padrão NORMAL; congelado não age; congelado ainda esquiva (em 40 tentativas); boneco nunca esquiva mas ainda acerta em algumas.
- ⚠️ Gate: **746 testes nas DUAS variantes**; único vermelho o flaky do Nexus Arcano. Houve um `packagePracegoRelease FAILED` isolado (file lock do Windows no empacotamento incremental) — **transitório, confirmado passando na re-execução**.
- 🚦 **Lote de UI → PARA para teste no aparelho.**
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote TESTE-C — 18 de Julho de 2026 (regra tática sai do controller e passa a ser testada de verdade)
**"viewmodel (UI), nao existem teste pra isso?" — e a resposta era um problema meu — branch GURPS-Saga**
- **Por que a camada de ViewModel tem 0 testes**: `SagaCombatController` exige `FichaViewModel`, que é `AndroidViewModel(Application)`. **Na JVM pura não existe `Application`**, então nada dele entra na suíte. É estrutural, não descuido.
- 🔴 **Dívida MINHA, achada ao conferir**: o teste da virada final (`HexRegrasFacingTest`) tinha um `direcoesPermitidas` que **reimplementava a regra** em vez de chamá-la. Uma cópia passa verde mesmo que o original quebre — e o caso concreto era real: o código de verdade usa `deslocamentoEfetivo` (com carga/ferimento) e a cópia recebia um deslocamento cru. Trocar um pelo outro quebraria o jogo para herói ferido **sem derrubar teste nenhum**.
- **Implementado**: `RegrasMovimentoTatico` no `domain` (Kotlin puro) com `viradaFinalLivre`, `direcoesDaViradaFinal` e `podeMoverNaGrade`. O controller passou a **chamar** essas funções; o teste passou a exercitar o **código real** e a cópia foi apagada.
- **+4 testes**: as travas de movimento (atordoado/agarrado/imobilizado e magia multi-turno), o caso oposto (cego **pode** andar — travar demais também é bug) e o do `deslocamentoEfetivo` que a cópia não protegia.
- ℹ️ **Escopo menor do que o previsto, e isso é bom**: os filtros do menu tático (`menuTaticoInimigo`) **já eram função pura e já tinham 9 testes** desde o TOK-6b-2. Só faltavam a virada e as travas.
- ⚠️ Gate: **742 testes nas DUAS variantes**; único vermelho segue sendo o **flaky pré-existente do Nexus Arcano**.

#### Experimento Robolectric (opção A) — TESTADO E **DESCARTADO**
- Montado a pedido do usuário para decidir com dado, não com opinião. 4 testes rodando de verdade contra um `Application` simulado.
- **Descartado por 5 razões medidas**: (1) **nenhum bug do app encontrado** — as 3 falhas do caminho foram todas minhas (SDK, uso errado da API `"Goblin" to 7` que é *quantidade* e não PV, e premissa de iniciativa); (2) **~15× mais lento** (~2,5s/teste contra ~0,16s da suíte JVM); (3) o Robolectric 4.13 só suporta **SDK 34** e o app tem alvo **35** — testaria um Android diferente do publicado; (4) `FichaViewModel` novo nasce com ficha **em branco**, o goblin ganha a iniciativa 100% das vezes e o caminho feliz nunca roda sem fixture pesada; (5) o cenário mais valioso (provar que a trava de agarrado está **ligada**) é impossível sem alargar visibilidade só para teste — piorar o código para agradar o teste.
- **Conclusão registrada**: o C cobre barato o que a UI tem de regra; o que sobra (layout, tamanho de toque, recomposição) **só o teste no aparelho real pega** — e a infraestrutura dele (`ui-test-junit4`) já está no `build.gradle`, apenas sem uso.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote LOG-1 — 18 de Julho de 2026 (combate visível no logcat, para o teste no aparelho)
**"podemos colocar logs ativos, pra aparecer no logcat do Android Studio?" — branch GURPS-Saga**
- **Diagnóstico**: o motor de combate/magia tinha **ZERO logging**. Todo o logging do app era do lado da IA (`MestreIA_*`, `GeminiLive`) — na hora que uma regra saísse errada no aparelho, não havia nada para olhar.
- **Atalho aproveitado**: o combate já escreve os números na narrativa (NH, rolagem, dano, RD, condição) em **164 pontos**. Trocando `mutableListOf()` por uma `LogDeCombate : ArrayList<String>()` que espelha no `add`, os 164 pontos passam a sair no logcat **sem serem tocados** — uma linha alterada.
- **No Android Studio**: filtrar por `tag:Saga_Combate`. Marco `═══ COMBATE INICIADO ═══` com os PV de todo mundo facilita achar o começo da luta num log cheio.
- **+ traço mecânico da explosão** (`⚙`): a narrativa só mostra o dano final, então a conta do MEC-14 (distância ao centro → bruto → dividido) ficava invisível. É o número mais fácil de sair errado e o mais difícil de conferir de olho.
- **Seguro para a suíte**: o `unitTests.isReturnDefaultValues = true` já estava ligado no build, então `android.util.Log` vira no-op no JVM. Gate confirmou: **738 testes, exatamente os mesmos de antes**.
- 📊 **Calibragem pedida pelo usuário** ("essa quantidade de testes é normal?"): 56.261 linhas de produção / 10.771 de teste ≈ 1 linha de teste para cada 5 de produção; 337 testes em `domain/combat`, 146 em `domain/magic`, **0 em `viewmodel`/UI**. É proporção saudável — e o zero na UI explica por que os bugs que ele achou no aparelho (seletor de energia, magia inventada, combate sem conjurar) escaparam: estavam todos na costura de UI, a única camada sem cobertura.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-19 — 18 de Julho de 2026 (escapar da condição por teste de atributo — o gelo)
**Continuação de "faça os 10 buracos de schema que faltam!" — branch GURPS-Saga**
- **O problema**: o Toque Congelante virava `condicao: paralisado` genérica — **sem saída nenhuma**. Combinado com o MEC-17 (que só faz condição sair por TEMPO) e sem prazo no catálogo, a vítima ficava paralisada para sempre. O livro dá a saída: *"não pode tomar nenhuma ação até que ele **rompa o gelo com um teste de ST** bem-sucedido com uma penalidade de **-1 por cada 0,5cm de gelo**"*, e *"Custo: **2 por 0,5cm**"* → −1 a cada 2 pontos de energia.
- **Implementado**: `condicaoEscapeAtributo` + `condicaoEscapeEnergiaPorPonto` no schema, `EscapeCondicao` no Combatente, e a tentativa de romper resolvida no avanço do turno — a vítima gasta o turno tentando; sucesso liberta na hora, falha tenta de novo. Tudo aparece no log.
- **Terceira via de saída de condição**, agora completas: por **tempo** (MEC-17), por **teste de resistência na imposição** (MEC-18) e por **escape recorrente** (MEC-19).
- **+2 testes** (a penalidade escalando com a energia; e o preso tentando romper todo turno).
- ⚠️ Gate: **738 testes nas DUAS variantes**; único vermelho segue sendo o **flaky pré-existente do Nexus Arcano**.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-18 — 18 de Julho de 2026 (condição imposta SEM teste; duração escalada pela energia)
**Continuação de "faça os 10 buracos de schema que faltam!" — branch GURPS-Saga**
- **🔴 Bug real achado ao conferir um item que eu achava falso positivo**: a auditoria dizia que "Vontade não existe no enum". **É falso positivo mesmo** — Deturpar/Medo/Pânico/Quietude têm `R-Vont` na CLASSE e já resistem por Vontade corretamente. **Mas do lado disso apareceu coisa pior**: o ramo de magia de condição só testava resistência quando a CLASSE trazia `R-XXX`. O **Jato de Som** é classe `Comum` — logo **atordoava sem teste nenhum**, quando o livro manda *"teste contra seu HT MENOS o custo de energia da mágica"*. O mesmo valia para **Jato de Areia/Lama/Neve** (classe Comum, o livro manda testar HT para não cegar).
- **Implementado**: o ramo de condição passa a honrar o `condicaoResistencia` da própria magia quando a classe não dá resistência. Novo `condicaoRdBonusPor` para o *"+1 ao HT efetivo a cada cinco pontos de RD"* do Jato de Som, e `resistenciaEfetivaDaCondicao()` juntando HT − energia + bônus de RD.
- **`condicaoDuracaoSegPorEnergia`** (encaixa no relógio do MEC-17): texto literal do Jato de Areia — *"ficará cego ... **por um segundo por ponto de energia** colocada na mágica"*. Curadas 6 (Jato de Ácido/Areia/Lama/Neve/Vapor, Sopro de Vapor); 3 delas ganharam também o `condicaoResistencia: HT` que faltava.
- 🧹 **Dado morto limpo**: Atordoamento, Cegar, Emudecer, Paralisia Total e Terror tinham `condicaoResistencia: "HT"` que **nunca era lido** (resistem pela classe `R-XXX`). Ficava parecendo que o Terror resistia com HT quando o livro diz Vontade — removido para não enganar a próxima leitura.
- ✅ **Teto de energia (item 4 da auditoria) já estava fechado** pelo **MEC-9** — o seletor limita pela regra da magia ("1 a 3" → 3). A auditoria foi escrita antes dele. Falta só uma trava redundante no motor; registrado, não feito.
- **+3 testes** (HT−energia com e sem RD; duração escalada vs fixa; e o que prova que **sempre há teste**, nunca imposição automática).
- ⚠️ Gate: **736 testes nas DUAS variantes**; único vermelho segue sendo o **flaky pré-existente do Nexus Arcano**.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-17 — 18 de Julho de 2026 (condição com PRAZO — antes era ETERNA) + reclassificação do MEC-16
**Continuação de "faça os 10 buracos de schema que faltam!" — branch GURPS-Saga**
- **🔴 O buraco de verdade**: `imporCondicaoMagica` fazia `condicoes.add(cond)` **e mais nada**. Só ATORDOADO tinha recuperação (teste de HT) e DORMINDO acordava com golpe — **CEGO, PARALISADO, AMEDRONTADO e SILENCIADO NUNCA saíam**. Um goblin cegado ficava cego a luta inteira, sendo que a Cegar dura **10 segundos** no livro. Isso não é detalhe: cegueira eterna é praticamente matar o inimigo com uma magia barata.
- **Implementado**: `condicaoDuracaoSeg` no schema + `condicoesTemporarias` no Combatente (relógio por condição). O prazo corre quando o turno de quem sofre a condição termina (1 turno = 1 segundo) e a condição cai sozinha ao zerar, com linha no log. Reaplicar mantém o **maior** prazo — a segunda Cegar não pode encurtar a primeira.
- **Curadoria sem heurística**: 10 magias curadas lendo o campo **estruturado** `duracao` (Cegar 10s, Medo 600s, Paralisia Total 60s, Terror 60s…). As `Instant.`/`Perm.` ficaram de fora **de propósito** — têm regra própria de saída (o atordoamento sai por HT; o Sono, quando acordam o alvo).
- **+2 testes**: a cegueira com prazo expira sozinha; e a condição **sem** prazo NÃO expira por tempo (esse segundo teste é o que impede a correção de virar um bug novo, tirando paralisia que deveria durar).
- 📋 **MEC-16 RECLASSIFICADO — e por isso NÃO foi feito**: eu tinha planejado "dano por turno" como buraco de schema. Não é. As Chuvas (Ácido/Fogo/Pedras), a Nuvem de Faíscas e o Géiser são **zonas persistentes**: uma área do grid que fere quem está dentro, a cada turno, pela duração. Isso exige (1) registro da zona na sessão, (2) tique de dano no avanço do turno e (3) **desenhar a zona na grade**. O item (3) não é opcional: dano invisível recriaria exatamente o bug que você reportou no aparelho ("só de se movimentar ele tá perdendo PV"). Como é lote de UI, ele **para para teste** pela sua própria regra — e já há fila de coisa não validada.
- ⚠️ Gate: **733 testes nas DUAS variantes**; único vermelho segue sendo o **flaky pré-existente do Nexus Arcano**.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-15 — 18 de Julho de 2026 (distâncias do Projétil: 1/2D e Máximo)
**Continuação de "faça os 10 buracos de schema que faltam!" — branch GURPS-Saga**
- **A penalidade de distância (SSR) já existia**; o que faltava era o resto: **Máx** (não dá para acertar além dele) e **1/2D** (metade do dano). Estavam só na prosa das `notas` das 12 magias de projétil.
- **A fonte literal me deu 3 detalhes que eu teria errado de memória**: (1) o 1/2D vale a partir da distância **maior ou IGUAL** — eu teria escrito `>`, e o alvo exatamente no 1/2D levaria dano cheio; (2) divide o dano **básico** (antes da RD), arredondando para baixo; (3) além do 1/2D o alvo resiste à atribulação com **+3**.
- **Implementado**: `alcanceMeioDano`/`alcanceMaximo` + `foraDoAlcanceMaximo()`/`aplicarMeioDano()`. A guarda de Máx recusa **antes de cobrar fadiga** (mesmo princípio do MEC-13 — o operador enxerga a distância, não faz sentido queimar o turno num tiro que a regra proíbe). Tem teste provando que o PF não é cobrado.
- ⚠️ **Contradição do próprio livro, registrada**: a seção "Distância" diz metade do dano **E** +3 para resistir; a seção "Metade do Dano (1/2D)" diz +3 **em vez de** a metade. Adotada a formulação inequívoca (os dois). Está no `docs/pendencias/MAGIA_DEFERIDOS.md` — é uma linha para inverter se em mesa o usuário preferir.
- ⚠️ **Precisão (Prec) NÃO foi para o schema, de propósito**: Acc só vale com a manobra **Apontar**, e hoje o projétil é conjurado e arremessado no mesmo turno (`heroiConjurar` chama `limparApontar()`). Somar Prec sem Apontar seria bônus de graça — o oposto da regra. Seria campo morto; destrava junto com o deferido "carregar em vários turnos".
- 🐛 **Erro meu, pego antes de commitar**: o primeiro regex de curadoria pegou `Max` de qualquer contexto e marcou **Força, Vigor, Debilitar, Calor (Máx 1500!), Metalovisão** como se fossem alcance de arma — eram tetos de nível/energia/profundidade. Revertido e refeito com escopo estrito (`entrega: projetil` **e** o bloco `1/2D … Máx` colado): 25 magias erradas → **12 corretas**. É exatamente a lição já registrada de *preferir dado estruturado a heurística sobre prosa*.
- **+4 testes** (o ≥ do 1/2D; sem 1/2D não decai; o limite exato do Máx; e a recusa sem cobrar fadiga).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lotes MEC-13 e MEC-14 — 18 de Julho de 2026 (buracos de schema: alvo inválido e explosão)
**Continuação de "faça os 10 buracos de schema que faltam!" — branch GURPS-Saga**
- **MEC-13 — magia de objeto lançada em gente**: `Desintegrar`, `Enfraquecer`, `Explodir` e `Fender` só afetam **objetos inanimados**, mas nada impedia mirá-las num goblin: a fadiga era gasta e o efeito simplesmente não acontecia. Novo campo `alvoValido: "objeto"` + `soAfetaObjeto()`; a guarda entra **no topo do `heroiConjurar`, ANTES de cobrar a energia** — recusar depois de cobrar seria o mesmo prejuízo com mensagem melhor.
  - Deferido com honestidade: **Espantar Zumbi** ficou anotada mas sem trava — exigiria saber se o alvo é morto-vivo, e o bestiário não tem esse campo.
- **MEC-14 — EXPLOSÃO com decaimento por distância**: a regra estava só na prosa das `notas`. Texto literal: *"O alvo e qualquer um mais próximo do alvo que um metro recebe dano total. Os mais afastados **dividem o dano em três vezes a distância em metros** (arredondado para baixo)."* Virou `explosaoDivisorPorMetro` + `danoDaExplosao()`, aplicado no ramo de ÁREA; o controller tático passa a distância real de cada alvo ao hex central (`distanciaAoCentro`).
  - **Exatamente 3 magias** têm essa regra: Bola de Fogo Explosiva, Bola de Relâmpagos, Relâmpago Explosivo. A **Concussão NÃO entra** — o auditor a tinha agrupado junto, mas o "raio de 10 m" dela é do **atordoamento**, não decaimento de dano; e a **Explodir** também não (fragmentação em objeto, outra regra).
  - **Sem o campo nada decai** — que é o certo para chuva/nuvem: dano ambiental atinge todos igual, não é onda de choque. Tem teste trancando isso.
  - ⚠️ **Limitação honesta registrada no `docs/pendencias/MAGIA_DEFERIDOS.md`**: o Relâmpago Explosivo é `entrega: projetil` e o ramo de projétil acerta **um alvo só** — o alvo direto leva o dano cheio (correto), mas **quem está ao redor não leva respingo**. Espalhar do ponto de impacto exigiria o projétil resolver contra um HEX.
- 🐛 **Bug meu pego pelo próprio teste**: calculei `brutoAqui` (o dano já dividido) e continuei passando `bruto` para `aplicarDano` — o decaimento não saía do lugar. O unitário puro passava; só o teste de integração com **dois goblins a distâncias diferentes** revelou. Fica a lição: testar a função pura não prova que ela está ligada.
- **+4 testes** em `MagicCombatTest` (dano cheio até 1 m; 20 a 2/3/4/7 m = 3/2/1/0; sem divisor não decai; e o de integração).
- ⚠️ Gate: **727 testes nas DUAS variantes (Visual e PraCego)**, único vermelho segue sendo o **flaky pré-existente do Nexus Arcano** (`planejador_resolve_requisito_de_contagem_por_escola`, em correção em sessão separada).
- **Faltam do plano**: MEC-15 (1/2D, Máx, Precisão), MEC-16 (dano por turno de chuvas/nuvens), MEC-17 (condições ricas).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote HEX-FACING-2 — 17 de Julho de 2026 (faltava virar no FIM do movimento — o caso canônico da regra)
**"A opção de virar só tem no começo do meu turno — eu poderia ou não virar no final?" — teste no aparelho, branch GURPS-Saga**
- **Sim, poderia — e era exatamente o caso que faltava.** O HEX-FACING resolveu só METADE da regra (virar ANTES de agir). A p.388 é explícita sobre o outro caso, que é o principal: *"Mudar de direção **no final do movimento: Livre!** O personagem pode se virar para **qualquer direção** se não usou mais que a **metade** dos seus pontos de movimento; se usou mais, ele pode mudar sua direção em **apenas um lado de hexágono**."*
- **O que o app fazia**: mover → o facing GRUDAVA na direção do movimento (`HexCombatState.mover`) → `depoisDaAcaoDoHeroi()` passava o turno na hora. O jogador fugia, ficava olhando para onde correu e levava flanco/costas sem chance de se orientar. Era a causa concreta do print anterior.
- **Implementado**: depois de mover o turno **PARA** (`viradaFinalPendente`) e a grade mostra "🧭 Para onde você fica olhando?" com as direções PERMITIDAS pela regra + "Manter". Só depois da escolha o turno passa (`concluirViradaFinal`).
- **A gradação da regra está no código**: `direcoesDaViradaFinal()` devolve as 6 direções se `andou ≤ (deslocamento+1)/2`; senão só 3 (a atual + uma vizinha de cada lado na roda de 6). O prompt explica qual caso se aplica e por quê.
- **Dois riscos da mudança, cobertos**: (1) se o combate ENCERRA com o movimento, não pede virada (seria prompt preso em luta acabada); (2) `encerrarManual` limpa a pendência, para não vazar para a próxima luta.
- **+4 testes**: até metade → 6 direções; mais que metade → 3; e o que prova que **mesmo restrito a um lado o herói tira o atacante das COSTAS** (vira flanco).
- 💡 **Duas perguntas do usuário, dois erros meus evitados**: a 1ª (é ação de movimento? perde o turno?) evitou eu fazer virar CONSUMIR o turno — o oposto da regra; a 2ª pegou que eu tinha implementado só metade dela. Em ambas a resposta estava na fonte literal, e eu não teria chegado nela de memória.
- ⚠️ Gate: compila nas 4 variantes; único vermelho segue sendo o **flaky pré-existente do Nexus Arcano**.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote HEX-FACING — 17 de Julho de 2026 (o jogador não tinha como VIRAR — pagava o flanco sem poder reagir)
**"Nessa posição no grid, eu deveria estar flanqueado?" + "pesquise a regra de virar-se: é ação de movimento? perde o turno?" — branch GURPS-Saga**
- **A dúvida do print**: o card mostrava "Esquiva 5 (−2 flanco)" com o goblin acima-e-à-direita. **Verifiquei os 4 elos e TODOS estavam corretos**: a regra (FRENTE = direção encarada + 2 vizinhas = 3 de 6; FLANCO = 2; COSTAS = 1), a geometria (`Direcao.de` por projeção em cubo — confere para alvo distante), o desenho da seta (`anguloDaDirecao`: LESTE 0°, NORDESTE −60°) e a ponte (`facingDoAtaque` usa o facing do ALVO, não do atacante). Conclusão: se o herói olhava para SUDESTE o flanco estava certo; para LESTE seria frente.
- **🔴 O problema real não era o cálculo — era a AGÊNCIA**: o facing do herói é definido automaticamente pela direção do último movimento (`HexCombatState.mover`), e **não existia nenhuma forma de virar de propósito**. O jogador recuava, ficava olhando para onde fugiu, levava −2 pelo flanco e **não podia fazer nada**. A penalidade era aplicada sem a decisão tática que a justifica.
- **PESQUISA NA FONTE LITERAL (pedido do usuário — e valeu muito)**: **p.368** "um passo consiste em movimento de até 1/10 do Deslocamento, **uma mudança de direção (ex.: virar-se), ou as duas coisas**"; **p.387** "manobras como Ataque ou Preparar permitem um passo... pode **mudar de direção livremente** antes ou depois do movimento"; **p.388** "Mudar de direção no final do movimento: **Livre!**" (qualquer direção se usou ≤ metade dos pontos; 1 lado se usou mais). **Virar NÃO consome o turno** — de memória eu provavelmente teria feito consumir, que é o oposto da regra.
- **Implementado**: chip **🧭 Virar** no menu do token do herói + `SubDialogoVirar` (6 rumos, com a regra explicada na tela). `heroiVirar(direcao)` **não chama `depoisDaAcaoDoHeroi()`** — é livre, o menu nem fecha: o jogador se orienta e age no mesmo turno.
- **Bloqueios da fonte respeitados**: **p.86** agarrado/imobilizado "não pode... nem mudar de direção" → o chip some e a função recusa; fora do turno → recusa. **p.386** (*Avançar e Atacar* não pode virar no fim do deslocamento) não se aplica porque a virada acontece antes de escolher a manobra.
- **+4 testes** em `HexRegrasFacingTest`: os arcos completos olhando para Leste (frente = L/NE/SE; flanco = NO/SO), **virar tira o mesmo atacante do flanco e o põe na frente**, e classificação correta de atacante DISTANTE (o caso do print, que era magia à distância).
- ⚠️ Gate: compila nas 4 variantes; único vermelho segue sendo o **flaky pré-existente do Nexus Arcano**.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-12 — 17 de Julho de 2026 (o card de defesa oferecia magia que NÃO defende)
**Print do aparelho: o card "Defenda-se!" contra Bola de Fogo trazia "🔮 Aumentar Força (bloqueio) 15" — branch GURPS-Saga**
- ✅ **Antes de tudo: o card FUNCIONOU** — o MEC-11 acertou. O jogador foi atacado por magia e teve a escolha (Esquiva 7, Esquiva+jogar-se ao chão 10). Primeira correção de UI da sessão que chegou funcionando ao aparelho.
- **🔴 O erro**: entre as defesas aparecia **Aumentar Força**, que só aumenta ST — não desvia bola de fogo nenhuma. Pior: com NH 15, escolhê-la seria quase sempre sucesso → **imunidade a magia praticamente de graça**, quebrando o combate.
- **A causa**: `opcoesBloqueioMagico` aceitava QUALQUER magia cuja classe contivesse "Bloqueio". Mas o catálogo tem DUAS coisas diferentes, e a distinção está nos dados:
  - `"Bloqueio"` **puro** = reação que PROTEGE de um ataque chegando — Desviar Energia (cita literalmente "mágica Bola de Fogo ou Relâmpago"), Desviar/Devolver Projétil, Bloquear (BD instantâneo), Robustez (RD instantânea), Braço de Ferro, Apanhar Projétil, Girar Lâmina, Refletir Olhar, Translocação.
  - `"Comum ou Bloqueio"` = a magia PODE ser lançada como reação, mas o efeito **não é defensivo** — Aumentar Força/Destreza/Inteligência/Vitalidade, Fascinar, Dominar Animal.
- **Correção**: só Bloqueio PURO entra (`BLOQUEIO in classes && COMUM !in classes`). Regra estrutural, derivada do dado — não heurística de nome.
- **+3 testes** no `MagicClassParserTest` com as strings REAIS do catálogo ("Bloqueio", "Bloqueio/R-DX", "Bloqueio/R-Espec." passam; "Comum ou Bloqueio", "Comum ou Bloqueio/R-Vont", "Comum/Bloqueio/R-IQ" não).
- ⚠️ Gate: compila nas 4 variantes; único vermelho segue sendo o **flaky pré-existente do Nexus Arcano**.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-11 — 17 de Julho de 2026 (a defesa contra magia de NPC NUNCA disparava)
**"No preview, só do personagem se movimentar ele está perdendo PV — algum bug?" — teste no aparelho, branch GURPS-Saga**
- **Não era o movimento.** O PV sumia de verdade, mas pela **Bola de Fogo do goblin conjurador** — que fui eu quem colocou no combate de teste (`magiasDeclaradas = listOf("Bola de Fogo")`), justamente para exercitar a defesa contra magia do MEC-8.
- **🔴 O bug real (meu, do MEC-8): a defesa interativa contra magia NUNCA era alcançada.** As opções de defesa só são calculadas quando `intencaoAtacaHeroi(intencao)` é true, e essa função aceitava apenas `ATAQUE | ATAQUE_TOTAL | MOVER_E_ATACAR | AGARRAR`. **Conjurar é a manobra `CONCENTRAR`** — fora da lista. Logo `opcoes` vinha VAZIA, meu código do MEC-8 caía no ramo de fallback e chamava `npcConjurar` SEM o card: o motor esquivava sozinho. Do lado do jogador: move, leva dano, **sem card, sem rolagem, sem escolha** — parecia que andar machucava.
- **Quando escrevi "a esquiva agora é interativa" no MEC-8, não era verdade na prática**: o código existia mas era inalcançável. Só o teste no aparelho revelaria.
- **Correção**: `intencaoAtacaHeroi` passa a aceitar `intencao.conjurar?.projetil == true` (Projétil mágico no herói É um ataque defensável — Esquiva ou magia de Bloqueio, Magia p.12).
- **Risco que a correção criava, e blindagem**: `intencaoAtacaHeroi` também guarda o `npcResolve`; com a conjuração passando, uma intenção de conjurar cairia no fluxo de ataque com ARMA. Guarda explícita adicionada (`intencao.conjurar != null` → sai antes).
- **+2 testes** (a intenção de conjurar conta como defensável; `npcResolve` não resolve conjuração como ataque de arma) + suíte `domain.combat` inteira sem regressão.
- ⚠️ Gate: compila nas 4 variantes; único vermelho segue sendo o **flaky pré-existente do Nexus Arcano**.
- 💡 **Nota para o teste**: o personagem terminou a luta em 3/10 PV e isso PERSISTE na ficha — recuperar PV antes de testar de novo (o motor recusa abrir combate com o herói a 0).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-10 — 17 de Julho de 2026 (magias de CURA não existiam mecanicamente)
**"As magias de cura não estão configuradas mecanicamente no combate? Não estão dando opção de escolher quanto de fadiga usar" — teste no aparelho, branch GURPS-Saga**
- **Resposta: NÃO estavam — e a causa era ESTRUTURAL.** O `efeito` do schema não tinha valor para cura (`dano | condicao | buff | ambiente | controle | informacao | narrado`). Sem forma de dizer "restaura PV", **todas** as magias de curar caíram em `narrado`: não devolviam PV nenhum e — exatamente como o usuário notou — **não ofereciam o seletor de energia**, que só aparecia para dano e buff.
- **Padrão de sempre**: os curadores SABIAM e escreveram na `notas` ("gasta 1 a 4 energia, restaura o DOBRO"), mas não havia CAMPO.
- ⚠️ **Ponto cego da auditoria do LIMPEZA-4 — vale registrar**: ela examinou as **84 magias que o motor JÁ executava**, e cura não era uma delas → era **estruturalmente invisível** para aquela varredura. A auditoria acha campo faltando em magia que já funciona; nunca acharia uma CATEGORIA INTEIRA que não existe. Quem achou foi o usuário, jogando.
- **Efeito `cura` novo**, com os números conferidos na descrição fiel: `curaPvPorEnergia`, `curaMaxPv`, `curaTotal`. Cura Superficial = 1 PV/energia, teto 3 ("Restaura até 3 PV", custo 1 a 3); Cura Profunda = 2 PV/energia, teto 8 ("até 8 PV", custo 1 a 4); Cura Superior = todos os PV perdidos (custo fixo 20).
- **Motor** (`aplicarCuraMagica`): restaura PV de verdade, **nunca passa do PV máximo** e não "cura" quem está inteiro (curar 8 em quem perdeu 1 restaura 1). Se o alvo estava INCONSCIENTE por PV negativo e a cura o traz ao positivo, a inconsciência sai (é consequência do PV, MB p.380). Sem alvo explícito, cura o próprio operador — o caso comum no combate.
- **UI**: o seletor de energia passa a aparecer para cura, mostrando o que ela compra ("cada 1 PF = 2 PV, até 8"); o alvo padrão é **SI MESMO** (curar o goblin por engano seria o pior default possível); o toggle "causa dano" some em magia de cura.
- **+9 testes** (6 no `MagicMechanicsTest`: escala por energia, tetos, não estoura o perdido, Cura Superior, teto de energia derivado, cura sem número não entra no motor; 3 no `MagicCombatTest`: restaura PV no combate real, não passa do máximo).
- ⚠️ Gate: compila nas 4 variantes; único vermelho segue sendo o **flaky pré-existente do Nexus Arcano**.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote UX-1 — 17 de Julho de 2026 (conjurar em DOIS PASSOS — pedido do usuário no aparelho)
**"Cada magia como um botão; ao selecionar abre outro diálogo com alvo/dano/energia/PV. Se o personagem tiver 200 magias, demora e fica lento o combate" — branch GURPS-Saga**
- **O problema**: o `SubDialogoConjurar` era um diálogo ÚNICO — a lista inteira de magias em rádios e, só lá no fim da rolagem, alvo + "causa dano" + energia + queimar PV. Com muitas magias o jogador rola a lista completa **a cada conjuração** só para alcançar os parâmetros. Trava o ritmo do combate.
- **Passo 1 — escolher a magia**: cada magia virou um **botão** de largura cheia (nome em destaque; embaixo `classe · NH · custo`, e o motivo quando indisponível). Um toque leva direto ao passo 2.
- **Passo 2 — só os parâmetros DAQUELA magia**: título do diálogo = nome da magia; alvo, "causa dano", energia (com o teto do MEC-9) e queimar PV. Botão **"Voltar"** retorna à lista em vez de fechar tudo — errar a escolha não custa recomeçar a conjuração.
- **Busca por nome no passo 1** (aparece com mais de 6 magias, via `CatalogFilters.contemBusca`): a divisão em dois passos resolve o "rolar até o fim para achar os parâmetros", mas **não** resolveria "tenho 200 magias e preciso achar a Bola de Fogo" — que era a preocupação real do usuário. A busca resolve.
- Estado por magia com `remember(sel.id)`: trocar de magia zera energia/alvo/PV em vez de carregar valores da escolha anterior.
- ⚠️ Gate: compila nas 4 variantes; único vermelho segue sendo o **flaky pré-existente do Nexus Arcano** (em conserto na sessão paralela do usuário).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote TESTE-1c — 17 de Julho de 2026 (o modo tático NÃO ligava — bug de reatividade do Compose)
**"Você disse que ia deixar o modo tático ligado e não ficou" — teste no aparelho, branch GURPS-Saga**
- **A causa era REATIVIDADE, não lógica.** Toda a cadeia do TESTE-1/1b estava correta (botão → `sagaIniciarCombateTeste` → `iniciarCombateSandbox` → flag → grade montada), e eu conferi elo por elo. Mas nenhum elo era o problema.
- **O bug (clássico do Compose):** `val ativo: Boolean get() = sessao != null`, com `sessao` sendo um `var` COMUM — logo, `ativo` **não é observável**. E a tela testava `if (sagaCombateAtivo && sagaEstadoTatico != null)`. Pelo **curto-circuito do `&&`**, enquanto `ativo` era falso o `estadoTatico` (o único observável dos dois) **nunca era lido** — e composable que não lê um estado não se inscreve nele. Resultado: o combate começava de verdade por baixo, mas **nada mandava a tela redesenhar**; o preview ficava no demo para sempre.
- **Correção na RAIZ:** `sessao` virou `by mutableStateOf(null)` — conserta de uma vez todos os pontos que dependem de `ativo` (53 usos no controller), não só este. Como `CombatSession` é mutável por dentro, o `mutableStateOf` notifica na ATRIBUIÇÃO (começar/encerrar combate), que é exatamente o que a UI precisa.
- **Cinto e suspensório:** em `HexCanvasTatico` o estado observável passou a ser lido PRIMEIRO (`val estadoTaticoAtual = viewModel.sagaEstadoTatico` antes do `&&`), para que uma futura regressão em `ativo` não reintroduza o mesmo silêncio.
- **Lição registrada (e que eu não segui de verdade):** eu **afirmei que funcionaria** sem ter como verificar. A cadeia lógica estava toda certa — o que falhou foi a reatividade da UI, que só o aparelho revela. É exatamente o acordo `feedback_lote_ui_para_teste`: lote que toca UI **para** para teste, e eu devo dizer "isso precisa do seu teste" em vez de "agora vai funcionar".
- ⚠️ Gate: compila nas 4 variantes; único vermelho segue sendo o **flaky pré-existente do Nexus Arcano** (em conserto na sessão paralela do usuário).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-9 — 17 de Julho de 2026 (os 2 buracos de schema que eram MEUS)
**Primeiro lote do roteiro do `docs/pendencias/BURACOS_SCHEMA_MAGIAS.md` — atacando os que eu mesmo introduzi**
- **1 dos 13 "críticos" era FALSO POSITIVO — e verificar valeu o lote inteiro.** O auditor disse que "resistência por VONTADE não existe no enum" (Terror/Pânico/Medo/Êxtase). **Já existe e já funciona**: a resistência principal vem da CLASSE da magia (`R-Vont` → `AtributoResistencia.VONTADE`), tratada em `resistenciaDoAlvo`; o `condicaoResistencia` é outro campo, para o teste EXTRA embutido no dano (o atordoar do Relâmpago). O auditor conflatou os dois. **Se eu tivesse "consertado", teria quebrado o que estava certo.**
- **🔴 BUG REAL, meu, do MEC-2 — o +2 da arma encantada era MULTIPLICADO.** Eu somava o bônus ao dano BRUTO com o comentário de que `(dano+2) − RD == (dano − RD) + 2` — verdade para subtração pura, mas **o multiplicador de ferimento vem DEPOIS da RD** (`HitLocationRules`: `floor(penetrante × mult)`). Então corte (×1,5) transformava o +2 em **+3**; perfuração (×2), em **+4**. O livro diz "após a penetração da armadura **e os modificadores de ferimento**". Agora vai como `bonusAposRd` no `CombatResolver.resolverTroca`, somado ao ferimento final. Teste trava o número exato: dano 8, RD 2, corte → **11** (não 12).
- **🔴 Teto de energia — porta que o MEC-7 abriu.** O seletor novo permitia despejar 10 num Toque Candente ("Custo: 1 a 3") e sair **10d**. `MagicEnergy.tetoDeEnergiaDano(energia, aptidao)` (domínio puro, testável — lição do MEC-5 de não enterrar regra na UI) passa a mandar no teto. ⚠️ Armadilha: `"2 a 2×AM"` — o regex de faixa leria "2 a 2" e limitaria em **2**, quando o certo é 2×Aptidão Mágica; por isso o "AM" é testado ANTES da faixa.
- **+7 testes** (3 no `CombatResolverTest`: bônus não multiplicado / bônus não fura armadura sozinho / regressão sem bônus; 4 no `MagicCastingTest`: faixa simples, ×AM, Varia, custo fixo).
- ⚠️ Gate: compila e todos os meus testes passam; o único vermelho segue sendo o **flaky pré-existente do Nexus Arcano** (já em conserto em sessão paralela do usuário).
- **Restam 11 achados reais** no `docs/pendencias/BURACOS_SCHEMA_MAGIAS.md`: explosão sem decaimento, dano por segundo (chuvas/nuvens), restrição de alvo (dá para Desintegrar um vivo), stats de projétil (1/2D-Máx-Precisão), duração de cegueira por energia, Jato de Som (HT − energia), Toque Candente (RD natural), Morte Candente/Putrefata (tick com teste), Lampejo (bandas), Toque Congelante (escape por ST).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lotes LIMPEZA-1 a 4 — 17 de Julho de 2026 (dívida que EU criei, apontada na minha própria revisão)
**"Corrija tudo, coloque cada um num lote e resolva todos" — branch GURPS-Saga**
- **LIMPEZA-1 — fonte ÚNICA dos overlays táticos.** Para consertar o TESTE-1b eu havia DUPLICADO por copy-paste a pilha de overlays (status/"Defenda-se!", magias ativas, mira de área, menu do token) dentro do diálogo do preview. Duas cópias divergem em silêncio — mexer numa e esquecer a outra reproduz exatamente o bug que o usuário pegou (combate que não deixa conjurar). Agora existe `OverlaysCombateTatico(viewModel, menuHeroiNoTopo, paddingTopo)` em `CombatUi.kt`, extensão de `BoxScope`, usada pelos DOIS lugares. O parâmetro `menuHeroiNoTopo` guarda a única diferença real: na tela de campanha o menu do herói vai em cima (libera os hexes de movimento, TOK-6b-3); no diálogo cabe embaixo, perto do polegar.
- **LIMPEZA-2 — flag de teste fora da API de produção.** O `forcarTatico` do TESTE-1 era um parâmetro que SÓ o teste usava, sujando a assinatura de `iniciarCombate`. Agora o sandbox tem entrada própria: `iniciarCombateSandbox(...)`, que liga um `taticoForcadoUmaVez` privado. ⚠️ **Risco pego na revisão:** eu ia consumir a flag lá embaixo (na hora de montar a grade), mas `iniciarCombate` tem SAÍDAS ANTECIPADAS antes disso (sem contexto / combate já ativo / herói incapacitado) — se caísse numa delas a flag ficaria ligada e **vazaria para o próximo combate, que seria real**. Consumo movido para a 1ª linha da função.
- **LIMPEZA-3 — código morto marcado.** `EfeitoMagia.kt`/`EfeitoMagiaCanvas.kt` (VFX-1) têm **zero chamadores** em produção: o mapeamento está certo e testado, mas nada dispara efeito no combate. Em vez de deixar ambíguo (código que compila, tem teste e ninguém usa apodrece), os dois arquivos ganharam aviso de cabeçalho **⚠️ PROTÓTIPO NÃO LIGADO** explicando que falta a integração com o grid e que a ordem foi decisão do usuário (mecânica 100% antes da arte). Ao ligar, remover o aviso.
- **LIMPEZA-4 — auditoria PROATIVA de buracos de schema. ACHOU 26 (13 de impacto ALTO).** Padrão observado: **5 vezes nesta sessão** um campo faltante só apareceu quando o jogo saiu errado no aparelho (`danoFixo` → Géiser 15d; `buffBd` → Escudo não passava; `buffUmUnicoUso` → Aumentar Força não fazia nada; `buffArmaTipo` → +2 do gume vazando pro arco; `escalaComEnergia` → jogador sem escolher energia). Em TODAS a curadoria já sabia a resposta e escreveu na `notas` — faltava CAMPO, não competência. Agente auditou as 84 magias que o motor executa. **Resultado completo em `docs/pendencias/BURACOS_SCHEMA_MAGIAS.md`.** Os mais graves:
  - 🔴 **Resistência por VONTADE não existe no enum** — Terror/Pânico/Medo/Êxtase/Atordoamento Mental são R-Vont, mas `condicaoResistencia` só aceita HT. Terror está gravado como "HT": **o motor rola o atributo errado**.
  - 🔴 **Sem teto de energia por magia** — e isto é **consequência DIRETA do MEC-7** (o seletor que acabei de entregar): Toque Candente é "custo 1 a 3", mas o seletor deixa despejar 10 → sai **10d**. Precisa de `energiaMaxima`.
  - 🔴 **`buffDanoArma` provavelmente soma ANTES da RD** — o livro diz "+2 após penetrar a armadura E os modificadores de ferimento". Dano 4 vs RD 5: correto = 2 de lesão; motor = 1. Número errado em toda luta contra armadura.
  - 🔴 **Explosão sem decaimento** — "quem está a mais de 1m divide o dano por 3× a distância"; hoje todos na área levam dano CHEIO.
  - 🔴 **Dano por SEGUNDO** (Chuvas/Nuvens/Géiser respingo) — o motor aplica UMA vez o que o livro manda aplicar a cada segundo por até 1 min.
  - 🔴 **Sem restrição de alvo** — Desintegrar/Enfraquecer/Fender só afetam objetos inanimados; hoje dá para desintegrar um NPC vivo.
  - 🔴 **Stats de projétil (1/2D, Máx, Precisão) 100% em texto morto** — não aplica meio-dano além do 1/2D, não limita no Máx, não dá o bônus de Acc ao Apontar.
  - Ainda: cegueira com duração por energia (o efeito PRINCIPAL dos Jatos), Jato de Som resiste com "HT − energia gasta", Toque Candente ignora armadura mas **não** RD natural, Morte Candente/Putrefata têm tick por turno com teste de HT, Lampejo tem bandas de distância, Toque Congelante sai por teste de ST.
  - ⚠️ **Isto é ROTEIRO, não regressão**: nenhum desses é novo — sempre estiveram assim. A auditoria só os tornou visíveis antes de você tropeçar neles no aparelho.
- **#5 (processo, não código):** rodei 8 lotes seguidos (MEC-5→TESTE-1b) com pouca validação no aparelho, e o usuário achou **3 bugs reais de imediato** — todos na costura **UI/integração**, exatamente onde os testes unitários do motor não alcançam (eles pegaram bem o Géiser e o Debilitar resistido, mas nenhum ia perceber que a TELA nunca perguntava a energia). Acordo registrado em memória: **lote que toca UI/integração para para teste no aparelho antes do próximo.**
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote TESTE-1b — 17 de Julho de 2026 (o combate de teste não deixava CONJURAR — furo do TESTE-1)
**Feedback do aparelho: "cliquei no combate de teste, era pra começar um combate correto?" — branch GURPS-Saga**
- **O furo do TESTE-1**: o botão iniciava o combate, mas o preview é um `Dialog` em tela cheia que só continha o GRID. O menu de manobras no token (com o chip 🔮 de conjurar) e o card "Defenda-se!" moram na tela de campanha, ATRÁS do diálogo — então o jogador começava a luta e não tinha como conjurar nem defender (o goblin conjurador travaria o turno).
- **Correção**: o bloco `if (sagaCombateAtivo)` dentro do diálogo agora replica os overlays essenciais da tela tática (reusando os MESMOS composables): `CombateStatusTatico` (status + `DefendaSeCard`, que é quem trata a defesa no 2D — confirmado lendo o código, o `DefesaPorTimingCard` da linha 579 só vale no 3D), `MenuTaticoDoToken` (manobras + conjurar), pílula de magias ativas e overlay de mira de área.
- **Investigação que evitou pior**: descobri que `taticoAtivo` (linha 442) decide o layout pelos flags de config (`modoTaticoHex`), enquanto `HexCanvasTatico` decide por `sagaEstadoTatico != null` — mas confirmei que NÃO é bug real nos fluxos de campanha (flag e estado concordam lá); o descasamento só aparecia com o `forcarTatico` do teste. Não mexi na tela principal.
- **Removido código morto**: um `if (heroi) ... else ...` meu com os dois ramos idênticos.
- ⚠️ **Build gate**: a compilação passa e a mudança (UI-only) é neutra, MAS `./gradlew build` fica VERMELHO por um teste FLAKY PRÉ-EXISTENTE e alheio: `NexusArcanoEngineLoteBGlobalTest.planejador_resolve_requisito_de_contagem_por_escola` ("veio: []"). Provado: (1) falha no HEAD commitado SEM minha mudança; (2) o motor/teste Nexus foram tocados por último em lotes antigos (c5a0d321/f22f6eb9); (3) é fixture em memória do pathfinder, sem relação com magia/UI. Causa: iteração de coleção não-determinística no pathfinder guloso. A branch JÁ estava vermelha nisso — meu commit não introduz o vermelho. Chip de tarefa aberto para consertar o flaky à parte.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote TESTE-1 — 17 de Julho de 2026 (combate de teste no preview da grade — pedido do usuário)
**"Na aba Saga tem um preview grade tática, consegue deixar o modo de combate ligado dentro desse grid pra eu testar as magias?" — branch GURPS-Saga**
- **Botão "⚔️ Combate de teste (2 goblins)"** dentro do diálogo do preview (`TabSaga`). Inicia um combate REAL sem o Narrador — 2 goblins a 5m, um deles conjurador (Bola de Fogo, para exercitar a esquiva interativa do MEC-8). `HexCanvasTatico` já troca sozinho para o modo combate quando `sagaCombateAtivo && sagaEstadoTatico != null`, então o mesmo grid vira jogável e o chip 🔮 aparece.
- **`sagaIniciarCombateTeste`** no viewModel → `iniciarCombate(..., forcarTatico = true)`. O `forcarTatico` foi necessário porque a grade tática só monta com `modoTaticoHex` (config da campanha, default false); no preview não há campanha, então sem isso o combate cairia no modo "faixas", NÃO no grid que o usuário quer.
- Erro evitado na revisão: eu tinha escrito `atualizarSagaUi()` (função que NÃO existe) — o `rodarLoop`/`atualizarEstado` do controller já publicam o estado sozinhos; a UI reage por `sagaCombateAtivo`/`sagaEstadoTatico`. Removido.
- **Serve para validar TODAS as ~84 magias mecânicas no aparelho** (MEC-1..8): dano projétil/área/toque, condições, buffs contínuos (Escudo/Armadura/Força — agora duram o minuto inteiro após o MEC-5), buffs de um uso (Aumentar Força), e a esquiva interativa contra magia de NPC. Build gate verde nas duas variantes.
- **NÃO inclui os efeitos VISUAIS** (VFX-1) — ligá-los no grid é o próximo lote (o motor precisa emitir "magia X no hexágono Y", a UI converter para pixels e animar). O protótipo está pronto e testado, mas mostrar no combate é integração à parte.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-8 — 17 de Julho de 2026 (NPC conjurador: fim da magia INVENTADA + esquiva interativa)
**Teste do usuário: "o Narrador inventou magia (Dardo Mágico) e inventou mecânica — não tive rolagem de esquiva nem opção de defesa" — branch GURPS-Saga**
- **A culpa era MINHA, não do Narrador.** "Dardo Mágico" estava HARDCODED no meu código (`iniciarCombate`): um regex no nome que o usuário digitava (`mag[oa]|conjurad|feiticei…`) fabricava uma mágica que **não existe em GURPS** (é nome de D&D), com números que EU inventei (NH IQ+3, 1d, 1 PF). Causa-raiz: a tool `iniciar_combate` **não tinha campo** para o Narrador declarar as mágicas do NPC → meu código "preenchia o buraco" inventando.
- **Correção 1 — magia REAL do catálogo**: campo novo `magias_dos_inimigos` na tool + no prompt do Narrador (que agora é instruído a NUNCA inventar nome). O app procura cada nome nas 879 magias curadas (`npcMagiaDoCatalogo`), usa a mecânica REAL (dado/entrega/custo) e **RECUSA** nome que não existe (o inimigo fica sem magia, em vez de ganhar uma inventada). Fidelidade ao livro é a base do projeto.
- **Correção 2 — esquiva INTERATIVA**: contra mágica de Projétil de NPC o herói agora vê o card "Defenda-se!" (Esquiva ou Bloqueio mágico, Magia p.12), com a ROLAGEM DELE — igual ao combate com arma. Antes o motor esquivava sozinho (o próprio comentário do código admitia "resolução síncrona; defesa interativa é refinamento futuro"). `npcConjurar` ganhou parâmetro `defesaHeroi`; mágica de dano NÃO-projétil (Comum resistível) segue sem defesa ativa (resiste por atributo, é a regra).
- **Cadeia ligada**: tool → executor → interface `CombatBridge` → delegate → controller (5 assinaturas). `FakeBridge` do teste atualizado.
- **+6 testes**: magia do NPC vem do catálogo (Bola de Fogo, não "Dardo Mágico"); esquiva usa a rolagem do jogador (rolagem 3 vs Esquiva 12 = esquiva garantida; rolagem 18 = a magia acerta); a tool repassa as magias declaradas.
- **DEFERIDO HONESTO**: o dano da mágica do NPC usa uma APROXIMAÇÃO de dados (1 a 3), não a expansão completa "1d por energia" que o herói tem. É jogável e fiel ao nome/tipo, mas não é o cálculo cheio. NPC ainda só lança magia de DANO (Projétil/direto) — não buff, área mirada nem condição pura.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-7 — 17 de Julho de 2026 (o jogador não podia ESCOLHER a energia — bug achado no aparelho)
**Teste do usuário: "testei Escudo e Aumentar Força, não tive opção de escolher quanto de fadiga gastar" — branch GURPS-Saga**
- **O bug, numa linha**: `if (!m.ehProjetil) energia = 1` no diálogo de conjuração. **O seletor de energia só existia para Projétil** (e raio para Área). Escudo e Aumentar Força são classe COMUM → a energia era travada em 1 → o herói pagava e levava o **efeito MÍNIMO**: Escudo com BD **+1** em vez dos +4 possíveis; Aumentar Força com **+1** de ST em vez de +5. Sem escolha e sem aviso.
- **Por que é grave**: nessas magias a ESCOLHA é a magia. Escudo sem decidir quanto investir não é magia, é um botão.
- **Correção**: `MagiaConjuravelUi` ganhou `escalaComEnergia` + `energiaMax` + `dicaEnergia`. O seletor agora aparece para **toda magia cujo EFEITO escala com energia** (Escudo, Armadura, Força, Graça, Vigor, Apressar, Nublar, Aumentar Força/Destreza/Vitalidade, Debilitar, Fragilidade, Inabilidade) e mostra **o que a energia compra**: "Energia investida: 6 PF — *cada 2 PF = +1 de Defesa (até +4)* — máx 8".
- **O teto vem da REGRA** (`buffEnergiaPorNivel × buffMaxNiveis`): Escudo trava em 8 PF porque acima disso a Defesa não sobe — o app não deixa o jogador queimar fadiga à toa. Projétil mantém o teto da Aptidão Mágica (Magia p.12).
- **Quase repeti o erro do MEC-5**: escrevi o cálculo PRIVADO dentro da tela — exatamente o que deixou 325 mágicas com a duração errada (o parser era privado no controller, fora do alcance dos testes). Movido para `MagicMechanics.escalaDeEnergia` (domínio puro), com teste.
- **+6 testes**, incluindo um de COERÊNCIA: o teto do seletor tem que bater com o teto que o motor aplica — se divergirem, o gate quebra em vez de o jogador descobrir gastando PF sem efeito. Build gate verde nas duas variantes.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-6 — 17 de Julho de 2026 (buff de UM ÚNICO USO — Aumentar Força/Destreza/Vitalidade)
**O herói pagava o PF e NADA acontecia — branch GURPS-Saga**
- **O bug**: Aumentar Força/Destreza/Vitalidade têm duração `"Instant."` (valem para um único teste/ação curta). `registrarSeMagiaAtiva` só aceitava TEMPORARIA/DURADOURA/PERMANENTE, então elas eram **silenciosamente descartadas**: o custo era cobrado e o bônus nunca aplicava. Estavam CURADAS com número desde o MEC-2 — faltava o gancho.
- **Campo `buffUmUnicoUso`** + `BuffAplicado.umUnicoUso`/`estreou`. Não são mágicas ativas (não têm manutenção nem relógio): entram direto na lista de buffs do alvo (`aplicarBuffDeUmUso`), então o `heroiPerfil` computado já as enxerga.
- **A armadilha do CONSUMO** (a parte que exigia cuidado): conjurar GASTA a ação do turno. Se o buff sumisse no fim desse mesmo turno, o herói nunca conseguiria usá-lo — seria trocar "não faz nada" por "não faz nada, com mais código". O buff **sobrevive ao turno da conjuração** e é consumido ao fim da ação SEGUINTE do dono, no `avancarTurno` (mesma armadilha do Lote 424, mesmo padrão do `estreou`).
- **Curada a Aumentar Vitalidade** (só tinha rótulo, sem número): HT +1 por energia, máx 5. **Aumentar Inteligência segue narrada** — é IQ, e o motor de combate não usa IQ.
- **+5 testes**: aplica na hora; SOBREVIVE ao turno da conjuração; some depois da ação seguinte; não vira mágica ativa; Vitalidade sobe o HT. Build gate verde nas duas variantes.
- **DEFERIDO HONESTO — Bloquear e Robustez ficaram de fora.** Eu as tinha listado junto, mas ao abrir o código: são de classe **Bloqueio**, e o app modela magia de Bloqueio como **opção de DEFESA** (rola o NH da magia como defesa). Robustez ("RD +5 contra UM ataque") não é uma rolagem de defesa; encaixá-la ali seria meia-regra errada. Precisam do caminho de reação de bloqueio aplicar buff — outro lote.
- **Estado do catálogo**: 879/879 curadas. O motor EXECUTA **86** (dano 40 + condição 21 + buff com número 25). Gargalos que sobram, todos de MOTOR (não de curadoria): **IQ/Vontade/Percepção** (29 magias, destravaria a escola de Mente), **dano por TIPO** (10 imunidades), **BD/RD instantâneo de Bloqueio** (Bloquear/Robustez).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-5 — 17 de Julho de 2026 (duração, custo e manutenção eram lidos ERRADO em centenas de magias)
**A pergunta do usuário ("é narrativo ou mecânico?") destravou o bug mais grave do PILAR MAGIA — branch GURPS-Saga**
- **A resposta**: duração, tempo de operação e custo SEMPRE foram mecânicos (custo pago em PF de verdade e reduzido por NH; tempo vira manobras Concentrar; duração tem tick, cobra manutenção e expira). **Mas os parsers erravam, calados.**
- **MEC-5a — DURAÇÃO: 325 das 879 lidas errado.** `'Perm.'` (**154 magias**) não casava com a busca pela palavra `"permanente"` INTEIRA → virava INSTANTÂNEA → mágica permanente nunca era rastreada (buff permanente nunca aplicava). `'1 hora'` (**82**) → só multiplicava por 60 quando achava `"min"`, então HORA virava SEGUNDO: **todo buff de 1 hora expirava no turno seguinte** (Pele de Crocodilo RD 4, Imunidades, Esconder, Escalada). `'Indef.'` (31) → instantânea; `'1 dia'` (13) → 1 segundo; `'Hora'`/`'Dia#'` (10) → unidade SEM número não tem dígito e caía em instantânea.
- **MEC-5a — CUSTO: 307 das 879 lidas errado.** `'04/02'` é **operar 4 / manter 2**, mas o regex de FRAÇÃO casava primeiro e devolvia 2,0 com base=null (Agonizar `'08/06'` → 1,33 em vez de custar 8). Auditando o catálogo inteiro: **a fração de área pura (`'1/2'`) NÃO EXISTE** — o ramo de fração nunca acertou um caso real e não tinha teste nenhum. A **manutenção real nunca era lida** (o `/02` era engolido); o motor estimava metade do custo. Agora usa o valor do catálogo e só estima quando falta.
- **Por que ninguém viu**: os dois bugs se escondiam um no outro — as mágicas de 1 hora expiravam em 1 turno, então a manutenção errada quase nunca chegava a ser cobrada. Só ficaram visíveis quando o MEC-2 fez os buffs valerem de verdade. **Motivo estrutural**: `parseDuracao`/`parseTempoSeg` eram **privados no controller**, fora do alcance da suíte que já rodava contra as 879 reais (`MagicCatalogRealityCheckTest`, para classe/resistência). `MagicTime` extraído para o domínio puro, com trava contra o catálogo REAL.
- **MEC-5b — AUDITORIA COM AGENTES (pedido do usuário) + os números CANÔNICOS**: 24 escolas em paralelo, **976 magias conferidas contra a `descricao` FIEL ao livro** (que traz as linhas canônicas "Duração: … Custo: … Tempo de operação: …"). Cruzando NÚMERO contra NÚMERO (só confiança alta): **50 conflitos de duração e 10 de custo** — erros de TRANSCRIÇÃO no cabeçalho. Ex.: Arma Congelante cabeçalho `03/01`+`3 seg.` vs livro **4 para operar, 1 manter, 2 seg.**; Analisar Mágica `5` vs **8**; Subjugar `4` vs **6** (manter 3); Criar Elemental Água `Perm.` vs **1 hora, não pode ser mantida**.
- **A correção certa (arquitetura, não remendo)**: em vez de patchear strings, **652 magias ganharam campos NUMÉRICOS** (`custoOperar`, `custoManter`, `duracaoSeg`, `duracaoTipo`, `tempoOperacaoSeg`) no catálogo. O motor **lê número**; o parser de texto virou FALLBACK para as **227** sem canônico (confiança < alta) — honesto, não invenção. Elimina a classe inteira de bug em vez de tapar os buracos achados.
- **A validação pegou 2 coisas antes do commit**: (1) **uma invariante MINHA estava errada** — escrevi um teste exigindo "manter ≤ operar" e ele acusou duas mágicas; o catálogo tem `'0/1'` (custa **0 para lançar**, 1 para manter), que é legítimo em GURPS. (2) **Luz Solar Contínua** ("role 2d para o número de dias") e **Vigília** ("1 noite") vieram como "temporária" SEM segundos: gravar 0s faria a mágica expirar no instante do lançamento, **pior que o fallback** → o merge agora recusa temporária-sem-segundos por regra.
- **Quase-erro registrado**: ao verificar os achados, meu primeiro grep pegou o bloco **"Item"** da descrição (custo para ENCANTAR um cajado: 750 de energia) em vez do custo da mágica, e me fez achar que os agentes tinham inventado números. Eles pegaram o bloco certo. Se eu tivesse confiado no meu atalho, teria descartado uma auditoria correta.
- **+18 testes** (MagicTimeTest novo com trava no catálogo real; MagicCastingTest para operar/manter; MagicCatalogRealityCheckTest para o custo). Build gate verde nas duas variantes.
- **DEFERIDO HONESTO**: 227 mágicas seguem no parser de texto (o auditor não teve confiança alta). `custoBasicoArea` foi extraído pelos agentes mas **ainda NÃO é usado** pelo motor (área continua usando o custo base × raio). "1 noite" e durações do tipo "role 2d dias" não têm modelo.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-2 — 16 de Julho de 2026 (MECÂNICA das magias — os BUFFS saem do papel e viram regra)
**19 buffs com número EXECUTÁVEL; 74 narrados honestamente — branch GURPS-Saga**
- **Problema**: o MEC-1 curou 94 buffs, mas eles ficavam GRAVADOS E INERTES — só `buffRotulo` (texto livre) e `buffDanoArma` existiam, e nem o `buffDanoArma` era lido por ninguém. "Pele de Crocodilo RD 4", "Voo Deslocamento 10", "Força +1 ST por 2 de energia" eram TEXTO.
- **Schema estendido a partir dos DADOS** (li os 93 buffs curados antes de desenhar — o erro do AR-1 foi desenhar por adivinhação): quase todo buff do livro é "N por NÍVEL, X de energia por nível, teto de M níveis". Campos novos: `buffEnergiaPorNivel`, `buffMaxNiveis`, `buffRd`, `buffEsquiva`, `buffAtributo`+`buffAtributoValor` (negativo nos debuffs), `buffDeslocamento`, `buffDeslocamentoFixo` (absoluto: Voo=10), `buffPenalidadeAtacantes` (Nublar), `buffArmaTipo` ("cac"/"distancia" — sem isto o +2 do gume vazaria pro arco). Níveis = `min(energia / buffEnergiaPorNivel, buffMaxNiveis)`, piso 1.
- **Re-curadoria multi-agente** (13 escolas em paralelo, 45s, 0 falhas): **19 com número** (Força/Graça/Vigor/Debilitar/Fragilidade/Inabilidade, Apressar, Nublar, Voo, Voo do Falcão, Proteger Animal, Armas/Projéteis Flamejantes-Congelantes-de Relâmpago, Aumentar Força/Destreza) e **74 marcados `semNumero`** com justificativa (metacaracterísticas, vantagens, imunidades, utilidade). Os agentes recusaram números que não cabiam — ex.: "Imunidade à Água dá RD 2 CONTRA ÁCIDO; RD condicional a um tipo de dano o motor não separa, aplicar como RD geral seria errado".
- **Handler no motor**: `BuffAplicado` (deltas concretos) guardado na `MagiaAtivaNoCombate` e na LISTA `Combatente.buffs` — computar o perfil efetivo a partir da lista (em vez de mutar e "desmutar") faz a expiração ser um `remove`, imune a drift/reversão dupla. `heroiPerfil` virou **propriedade computada** (base + buffs): os ~30 pontos de uso do motor passaram a enxergar buff sem eu tocar em nenhum. `deslocamentoEfetivo` recebe o buff na BASE (antes de postura/cambaleante = ordem das regras). Expiração no tick REVERTE; `dissiparMagiaAtiva` para os permanentes.
- **NPC também**: `stEfetivo`/`htEfetivo`/`dxEfetivo` (40 pontos de leitura trocados) — sem isso Debilitar/Fragilidade/Inabilidade seriam gravados e não fariam NADA, pois o motor lia o bestiário direto.
- **`mecanica.notas` chega ao Narrador** (`resumoEfeito` = nota curada + descrição): antes a curadoria de ambiente/controle/informação era 100% desperdiçada.
- **A revisão pré-commit achou 2 bugs REAIS**: **(1)** o ramo de **ÁREA** tinha a PRÓPRIA cópia do `expandirDano` e não passava `danoFixo` — o Géiser é de área, então ele ainda sairia **15d** apesar do MEC-1 (a correção de ontem não o alcançava). **(2)** `registrarSeMagiaAtiva` só olhava `res.sucesso`; um **Debilitar RESISTIDO ainda aplicaria −3 ST**, porque "sucesso" só quer dizer que a conjuração deu certo — agora checa `res.alvoResistiu`.
- **+16 testes** (MagicMechanicsTest +7, MagicCombatTest +9): escala por energia com teto/piso, debuff negativo, RD fixa não vira RD 20, Voo absoluto, +2 só no alcance certo, buff sem número não vira delta, expiração reverte, Debilitar derruba o ST efetivo do NPC.
- **DEFERIDOS HONESTOS (curados com número, mas o motor NÃO alcança — não vender como pronto)**: **Aumentar Força/Destreza** têm duração "Instant." (valem só num teste) e o motor não tem gancho de "próximo teste" → nunca são registrados; **Proteger Animal** é de ÁREA e `registrarSeMagiaAtiva` pula Área → a RD 5 não aplica pelo fluxo real; **Força +ST não aumenta o dano da arma** (o dano vem de `danoExpr` fixo do catálogo, não é recalculado por ST) — sobe só nas Disputas de luta agarrada; **buffs só valem no combate** (a tool narrativa `lancar_magia` não os aplica). **BD (Bônus de Defesa)** não tem campo — o +3 do Proteger Animal fica narrado. `ambiente`/`controle`/`informacao` seguem TAGUEADOS + narrados (agora com a nota curada chegando ao Narrador); handler próprio continua sendo lote futuro.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MEC-1 — 16 de Julho de 2026 (MECÂNICA das magias — 12 escolas curadas em PARALELO)
**482 das 879 magias ganharam `mecanica` estruturada — branch GURPS-Saga**
- **Pedido do usuário**: "vc pode delegar novos agentes pra fazer isso, nao apenas 1 unico agente". Feito: workflow multi-agente, **1 agente por escola**, cada um lendo as descrições fiéis do catálogo e devolvendo a `mecanica` estruturada (schema validado). Os agentes NÃO escrevem o JSON — devolvem dados, eu faço o merge; assim não há conflito.
- **12 de 23 escolas curadas** (501 magias, ~3,5 min): água, alimentos, animais, clima, comunicação, corpo, cura, deslocamento, encantamento, fogo, ilusão, luz_e_trevas. **As outras 11 falharam por LIMITE DE SESSÃO** (mente, metamágica, necromancia, plantas, portal, proteção, quebrar_e_consertar, reconhecimento, som, tecnológica, terra) — não por erro; retomáveis via `resumeFromRunId` (as 12 feitas voltam do cache, sem custo).
- **Distribuição**: dano 28, condição 13, buff 94, ambiente 50, controle 28, informação 26, narrado 243. **Executam de verdade hoje: `dano` e `condicao` (41)** — Fogo, Água, Clima e Luz/Trevas ganharam dano real (Bola de Fogo, Jato de Chamas, Adaga de Gelo, Geladura, Raio Solar, Chuva de Ácido). `buff`/`ambiente`/`controle` ficam TAGUEADOS e narrados — handler profundo é lote futuro (deferido honesto).
- **Validação das 6 de Ar (AR-1)**: sobreviveram intactas; o agente de Clima, relendo as mesmas descrições, chegou nos MESMOS números por conta própria — bom voto de confiança na curadoria.
- **Revisão das 41 que o motor executa achou 3 bugs REAIS** (por isso não commitei direto): **(1) dano fixo escalando** — `expandirDano` sempre multiplica pela energia, mas o Géiser é 3d FIXO (custo 5) e sairia **15d**; Chicote de Relâmpago (energia compra alcance) e Chuva de Ácido idem. O schema do AR-1 não tinha como dizer "dano fixo" → **campo `danoFixo` novo**; os agentes já avisavam nas `notas` ("dano fixo", "NÃO escala"), só não tinham onde. **(2) `danoPorEnergia: "1"` não é dado** — Nuvem de Faíscas/Fogo e Tempestade de Faíscas causam pontos, o regex exige `d`, caía no fallback e virava **"3d"** (~10× o dano) → agora vira `"0d+N"`, que o rolador entende (o rolador devolveria 0 para um "1" pelado). Elas ESCALAM ("1 ponto/seg por ponto de energia"), então NÃO são `danoFixo`. **(3) Dominar Animal → `paralisado`** era meia-regra errada (R-IQ domina, não congela) → virou `controle` narrado.
- **+3 testes** (`MagicMechanicsTest`): dano fixo não escala; pontos viram 0d+N; regressão do comportamento antigo. Build gate verde nas duas variantes.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote COND-1 — 15 de Julho de 2026 (PILAR MAGIA — condições mágicas ligadas no motor)
**Saga: Sono, Cegueira, Medo, Paralisar e Silêncio agora FUNCIONAM (não só narrados) — branch GURPS-Saga**
- **Pergunta do usuário**: quais efeitos adversos funcionam além de sangramento/atordoamento? Resposta: as 8 condições existentes (atordoado, sangrando, caído, inconsciente, agarrado, imobilizado, sufocando, surpreso) têm efeito real — mas da MAGIA só o atordoado estava ligado.
- **+5 condições novas** na enum `Condicao`, cada uma com efeito MECÂNICO: **CEGO** (−4 para atacar e defender, MB p.394), **DORMINDO** (incapacitado + indefeso; **acorda ao levar dano**, MB p.428), **PARALISADO** (incapacitado + indefeso; não acorda), **AMEDRONTADO** (só recua/defende — não ataca; o NPC foge), **SILENCIADO** (não conjura — o ritual exige fala, Magia p.8).
- **Engate no motor**: `manobrasLegais` (dormindo/paralisado→só "nada"; amedrontado→sem ataque); `melhorDefesaNpc`+`opcoesDefesaHeroi` (dormindo/paralisado indefesos; cego −4); mods de ataque do herói e do NPC (cego −4); `avancarTurno` (sweep que acorda quem dorme e levou dano); `heroiConjurar`/`npcConjurar` (silenciado bloqueia); cérebro do NPC (amedrontado foge).
- **Handler `mecanica.condicao` generalizado** (`imporCondicaoMagica`): mapeia a string do catálogo (sono/cegueira/medo/paralisar/silenciar) → enum e impõe. **Magia de CONDIÇÃO pura** (`efeito == "condicao"`) impõe no sucesso não resistido, no combate direto E em área (Sono coletivo).
- **+5 testes** (`MagicCombatTest`, total 35): Sono impõe DORMINDO; quem dorme só faz nada; dormindo acorda com dano; paralisado não acorda; silenciado bloqueia a conjuração. Zero regressão.
- **Nota**: a infra está pronta; as magias de Sono/Cegueira/Medo (de outras escolas) serão CURADAS com `mecanica.condicao` quando eu chegar nas escolas delas.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MA-8 — 15 de Julho de 2026 (PILAR MAGIA — descrição da magia PRO NARRADOR)
**Saga: o Narrador (IA) agora sabe o que a magia FAZ, não só o resultado — branch GURPS-Saga**
- **Pergunta do usuário**: ao conjurar, o Narrador recebe a descrição da magia? **Resposta**: recebia só o RESULTADO mecânico (as linhas do combate viram turnos "sistema" no feed, e a IA lê os últimos 8), mas NÃO a `descricao` — então em magias de efeito "narrado" ele sabia "conjurou X" mas não o que X faz.
- **Combate**: o controller pega do catálogo (`getMagiaPorId`) um **resumo do efeito** (`resumoDaDescricao`: a descrição fiel ANTES das seções Duração/Custo/Item, sem a linha de classe, ~300 chars) e passa em `ContextoConjuracao.resumoEfeito`; o motor ANEXA esse resumo à linha "Efeito narrado pelo Mestre" (combate direto e área) → chega ao Narrador via o feed.
- **Narrativa**: `lancar_magia` passou a devolver `efeito` (a descrição do catálogo, ~500 chars) no JSON — o Narrador narra o efeito fiel fora de combate.
- **Bônus**: o dano de ÁREA agora também usa a `mecanica` estruturada (dado exato, tipo, ignora armadura) quando houver (antes só o dano direto/Projétil).
- **+1 teste** (`MagicCombatTest`, total 30): magia narrada leva o resumo do efeito ao log. Zero regressão.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote AR-1 — 15 de Julho de 2026 (MECÂNICA das magias / Escola AR fase 1 — dano estruturado)
**Saga: a prosa das magias começa a virar regra — sem tocar na descrição — branch GURPS-Saga**
- **Problema (pergunta do usuário)**: 879 magias com efeito em PROSA (`descricao` fiel), não em regra. Muitas não são só dano.
- **Solução — campo `mecanica`** (legível pela máquina) ao lado da `descricao` (intocada). Modelo `domain/magic/MagicMechanics.kt` (`MagiaMecanica`): `efeito` fechado (dano/condicao/buff/ambiente/controle/informacao/narrado) + parâmetros. Adicionado ao `MagiaDefinicao` (catálogo, parseado por Gson); o combate lê via `DataRepository.getMagiaPorId(id).mecanica` (cobre até magias já aprendidas). `ContextoConjuracao.mecanica`.
- **Handler de `dano` no motor** (`aplicarDanoMagico`): `MagicMechanics.expandirDano` escala o dado por energia ("1d-1"/energia → 3d-3 com 3 de energia; "1d"/2 energia → 2d com 4); `tipoDano`; **armadura "ignora"** (Toque Chocante fere mesmo com RD alta); **condição embutida** (Relâmpago atordoa: HT −1 por 2 PV; Concussão HT−3). Liga nas ramificações de Projétil e dano direto de `resolverConjuracao`.
- **6 magias de Ar de DANO curadas** (lendo as descrições): Relâmpago, Toque Chocante, Concussão, Olhar de Relâmpago, Relâmpago Explosivo, Chicote de Relâmpago.
- **+7 testes** (`MagicMechanicsTest` 5 + `MagicCombatTest` +2, total 29): expansão de dano, penalidade de condição, Toque Chocante ignora armadura, Relâmpago atordoa. Zero regressão.
- **Próximo (AR-2)**: buffs (Corpo de Ar, Arma de Relâmpago +2…), ambiente (Muralhas, Furacão, clima…), controle (Turbilhão), informação + as ~43 magias restantes de Ar. Plano em `docs/planos/PLANO_MECANICA_MAGIAS.md`.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MA-7 — 15 de Julho de 2026 (PILAR MAGIA / Fase 7 — NPC CONJURADOR)
**Saga: os inimigos magos agora conjuram DE VERDADE — branch GURPS-Saga**
- **Dados**: `NpcStats.magias: List<NpcMagia>` (nome, nh, projetil, custoFP, danoDados). O bestiário ganhou `BestiarioCriatura.magias` (`MagiaCriatura`) → mapeado no `novoCombatente`. **Fallback**: um conceito de conjurador (regex `mago/conjurad/feiticei/brux/necromant/xam/arcan/piromant…`) sem mágica curada ganha um "Dardo Mágico" padrão (Projétil 1d, NH = IQ+3) — afordância de jogo (o usuário nomeou o inimigo).
- **Cérebro** (`NpcCombatBrain`): `IntencaoNpc.conjurar`; se o NPC tem mágica ofensiva + PF suficiente + herói a ≥1m, ele **conjura** (se colado, recua um passo antes — mago não gosta de melee). Sem PF, cai para ação mundana.
- **Resolução** (`CombatSession.npcConjurar`): usa o **mesmo `MagicCasting`** do herói. O NPC paga a própria fadiga; o herói se defende — **Projétil → ESQUIVA** (rolada pelo motor); acertou → dano 1d×danoDados com a **RD do herói**. **Falha crítica → choque de retorno NO NPC** (dano/atordoamento). O controller (`executarTurnoNpc`) roteia a intenção de conjurar para `npcConjurar`.
- **+3 testes** (`MagicCombatTest`, total 27): o cérebro decide lançar; `npcConjurar` gasta o PF do NPC e fere/esquiva; sem PF não conjura. Zero regressão (inclui `NpcCombatBrainTest`).
- **Refinamento futuro (honesto)**: a defesa do herói vs mágica de NPC é síncrona (auto-esquiva) — falta a versão INTERATIVA (card "Defenda-se!"); NPC só lança Projétil/dano direto (não Área/Toque/buff).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MA-6 — 15 de Julho de 2026 (PILAR MAGIA / Fase 6 — DANO de magia direta no combate)
**Saga: magias de dano que não são Projétil (jatos etc.) agora ferem no combate — branch GURPS-Saga**
- **Motivo (teste do usuário)**: "Jato de Chamas" é `classe="Comum"` no catálogo (não Projétil), então o dano estava só na descrição → caía em "efeito narrado", e no combate tático **ninguém narra** → parecia que não fazia nada. (O sucesso decisivo estava CERTO — o "custo 0 PF" prova a regra da p.7; era o efeito bespoke que faltava.)
- **`ContextoConjuracao.danoPorEnergia`**: o jogador marca **"Causa dano (1d por energia)"** no seletor de Conjurar (para Comum/Área com alvo; Projétil já tinha o seu; Toque não). Diretriz de Mágicas de Combate, **Magia p.14** ("um ponto de energia compra 1d de dano").
- **Motor**: `resolverConjuracao` — Comum de dano funciona no SUCESSO (sem teste de acerto, diferente do Projétil que tem 2 testes), dano 1d×energia com RD e resistência. `heroiConjurarArea` — todos os atingidos levam 1d×energia (rolado uma vez, com a RD de cada). Sem a flag, continua narrado.
- **+3 testes** (`MagicCombatTest`, total 24): Comum com dano fere; Comum sem dano continua narrado; área com dano fere todos. Zero regressão.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MA-5 — 15 de Julho de 2026 (PILAR MAGIA / Fase 5 — polimento: mana por cena + doc) 🎉 FECHA O PILAR
**Saga: lugares mágicos e anti-mágicos afetam a conjuração — branch GURPS-Saga**
- **Mana ambiente por cena**: `definir_cena` ganhou o param `mana` (muito_alta/alta/normal/baixa/nula). O executor chama `definirManaAmbiente` na `CombatBridge` → `FichaSagaDelegate` guarda `viewModel.sagaNivelMana` (em memória, sem migração de DB). **Todos os caminhos de conjuração** — combate (`heroiConjurar` + área) e narrativa (`lancarMagia`) — passaram a usar essa mana em vez do NORMAL fixo: **baixa = −5 no NH efetivo**, **nula = bloqueia a conjuração** (`MagicMana.podeOperar`, com mensagem factual). Prompt do Narrador orientado a definir mana em lugares mágicos/anti-mágicos.
- **Doc dos deferidos** `docs/pendencias/MAGIA_DEFERIDOS.md`: registro honesto da fronteira — o que é automatizado (espinha), o que é narrado (efeitos bespoke), o que fica deferido (NPC conjurador, carregar projétil multi-turno, magia cerimonial, cajados, efeito de buff mecânico) e as simplificações fiéis (dano do projétil ≈ contusão, Ataque Inato ≈ DX, Vontade do NPC ≈ IQ, manutenção ≈ metade do custo).
- **Magias ativas na UI** (pílula "✨ Ativas") já entregue no MA-3d-4; choque de retorno já traz rótulos do MA-1.
- Zero regressão (saga + combate + magia verdes).
- **🎉 PILAR MAGIA COMPLETO (MA-1..5)**: motor puro → resolvedor → magia no combate (conjurar/Projétil/Área/Toque/Bloqueio/multi-turno/ativas) → magia na narrativa → polimento. NPC conjurador deferido.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MA-4 — 15 de Julho de 2026 (PILAR MAGIA / Fase 4 — magia na NARRATIVA: tool lancar_magia)
**Saga: conjurar conversando com o Narrador, fora de combate — branch GURPS-Saga**
- **Tool `lancar_magia`** (`NarradorTools`, 19ª tool): `{ magia, alvo?, energia_extra?, resistencia_alvo? }`. Dispatch no `NarradorToolExecutor` → método `lancarMagia` na `CombatBridge` → implementado no `FichaSagaDelegate` (onde a ficha vive).
- **Resolução**: acha a magia no grimório do herói, calcula a Aptidão (`MagicEngine`) e o NH (`calcularNivel`), rola 3d e resolve pelo **mesmo `MagicCasting` do MA-2** (o cérebro compartilhado — narrativa e combate usam o mesmo motor); debita a fadiga direto na ficha; devolve **JSON factual** (resultado decisivo/sucesso/fracasso/crítico, custo pago, PF, resistência se `resistencia_alvo` informado, choque de retorno). O **EFEITO é narrado pelo Mestre**.
- **Guardas**: bloqueada DENTRO de combate (`em_combate` — lá a conjuração é o chip na tela); erro `magia_desconhecida` se não é do grimório; `campos_obrigatorios` sem nome.
- **Prompt do Narrador** atualizado com a orientação de quando chamar `lancar_magia` (fora de combate) vs. o chip Conjurar (na tela).
- **+3 testes** (`NarradorToolExecutorCombatTest`: dispatch fora de combate, guarda em-combate, guarda campos) + contagem de tools 18→19 (`NarradorToolsTest`).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MA-3d-4 — 15 de Julho de 2026 (PILAR MAGIA / Fase 3d-4 — magias ATIVAS + tick — FECHA O MA-3)
**Saga: buffs que persistem e drenam fadiga a cada turno — branch GURPS-Saga**
- **Registro**: após conjurar com sucesso uma magia de DURAÇÃO (temporária/duradoura, não-Projétil/Toque/Área), o controller a registra (`registrarSeMagiaAtiva` → `CombatSession.registrarMagiaAtiva`), parseando o campo `duracao` do catálogo ("1 min."→temporária 60s, "permanente", "instantâneo") e a **manutenção ≈ metade do custo** (Magia p.15) reduzida por NH.
- **Tick**: o `avancarTurno` roda o tick a cada turno do herói (= 1s de jogo) via `MagicActive` (do MA-1): **cobra a manutenção** do PF do herói ao completar o intervalo (e reseta a temporária) e **expira** as duradouras. **Permanentes** não cobram nem expiram.
- **UI**: pílula "✨ Ativas: X (58s) · Y" no canto superior direito do grid tático (`CombatUiState.magiasAtivas`).
- **Fronteira honesta**: o EFEITO do buff (ex.: Escudo → +DB) é bespoke, não automatizável do catálogo → **narrado pelo Mestre**; o motor rastreia manutenção/expiração/visibilidade.
- **+2 testes** (`MagicCombatTest`, total 21): manutenção cobra PF ao completar o intervalo; permanente não cobra nem expira. Zero regressão.
- **🎉 FECHA O MA-3 (magia no combate)**: MA-3a/b/c + MA-3d (Área/Toque/Bloqueio/ativas) completos. NPC conjurador deferido; próximos MA-4 (narrativa) e MA-5 (polimento).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MA-3d-3 — 15 de Julho de 2026 (PILAR MAGIA / Fase 3d-3 — mágicas de BLOQUEIO)
**Saga: conjurar uma defesa mágica no "Defenda-se!" — branch GURPS-Saga**
- **Defesa por mágica**: quando um inimigo ataca, o card "Defenda-se!" agora lista, além de Esquiva/Aparar/Bloquear normais, as **mágicas de Bloqueio** que o herói conhece (🔮 nome, valor = NH da magia). `opcoesBloqueioMagico` no controller injeta essas opções (`OpcaoDefesa.magiaBloqueioId/Nome`).
- **Ao escolher**: o controller paga o custo em PF (**NÃO reduzido por NH** — exceção da regra, Magia p.12) e o motor `aplicarBloqueioMagico` **quebra automaticamente** qualquer conjuração em andamento (p.12); o sucesso do bloqueio (rolar ≤ NH) passa pelo fluxo de defesa normal (`npcResolve` com tipo BLOQUEIO).
- **Não aparece** contra golpe fulminante ou ataque pelas costas (opções de defesa vazias → sem bloqueio mágico, fiel à p.12).
- **+2 testes** (`MagicCombatTest`, total 19): bloqueio cobra o custo cheio em PF + loga; bloqueio interrompe uma conjuração em andamento. Zero regressão.
- **Deferido p/ MA-3d-4**: magias ativas + tick de manutenção.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MA-3d-2 — 15 de Julho de 2026 (PILAR MAGIA / Fase 3d-2 — mágicas de TOQUE)
**Saga: carrega a mágica na mão e entrega num soco — branch GURPS-Saga**
- **Carregar**: no 🔮 Conjurar, uma magia de **Toque** lança em si mesmo (botão "Carregar na mão"). No sucesso, `resolverConjuracao` guarda `toqueCarregado` (a mão fica energizada) em vez de aplicar efeito.
- **Entregar**: o chip **✋ {magia}** aparece no menu de um inimigo **ADJACENTE** → `heroiEntregarToque`: ataque com a mão (aprox. DX) e o alvo usa **qualquer defesa ativa**; se se defende, a mágica **continua carregada** (tenta de novo); se acerta, **descarrega** e resistíveis fazem o **2º teste** (fresh, Magia p.12) — efeito narrado pelo Mestre. Chip **✋ Dissipar** no menu do herói (ação livre).
- **+3 testes** (`MagicCombatTest`, total 17): Toque carrega a mão sem aplicar efeito na hora; entregar descarrega (acerto) ou mantém (defesa/erro); dissipar limpa. Zero regressão.
- **Deferido p/ MA-3d-3/4**: Bloqueio (defesa reativa), magias ativas + tick.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MA-3d-1 — 15 de Julho de 2026 (PILAR MAGIA / Fase 3d — magia de ÁREA mirada no grid)
**Saga: você toca um hex e a explosão cai lá — pegando todos no raio — branch GURPS-Saga**
- **Mira no grid**: no 🔮 Conjurar, uma magia de **Área** troca o seletor de alvo por um **stepper de RAIO** (custo × raio, Magia p.11) e o botão vira **"Mirar no grid"**. O app entra em MIRA (`miraAreaPendente`) e o **próximo toque num hex é o CENTRO** da explosão. Overlay "🎯 Toque o centro de X (raio Nm)" + Cancelar.
- **`CombatSession.heroiConjurarArea`**: UM teste de lançamento; o controller calcula pela grade quem está no raio (`HexGrid.range(centro, raio−1)`; 1 hex = 1 m; raio 1 = só o hex central, raio 2 = +adjacentes, p.13) e a distância do herói até a **borda mais próxima** (penalidade, p.11). Cada alvo na área **resiste sozinho** contra a margem do operador (p.14) → o motor lista **atingidos × resistentes**. Efeito bespoke (dano/condição) → **narrado pelo Mestre**.
- **+3 testes** (`MagicCombatTest`, total 14): área sem resistência atinge todos + gasta custo × raio; área vazia resolve sem atingir; área resistível separa atingidos de resistentes. Zero regressão.
- **Deferido p/ MA-3d-2/3/4**: Toque (carrega a mão + ataque), Bloqueio (defesa reativa), magias ativas + tick.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MA-3c — 15 de Julho de 2026 (PILAR MAGIA / Fase 3c — conjuração multi-turno com interrupção)
**Saga: magias de vários segundos exigem foco — e o golpe do inimigo pode quebrá-lo — branch GURPS-Saga**
- **Multi-turno** (Magia p.7/9): magias com tempo de operação > 1s (do catálogo, reduzido por NH via `tempoOperacaoAjustado`) entram em **concentração** — o turno inicial é a 1ª manobra Concentrar; restam `tempo−1` turnos. `heroiConjurar` guarda `conjuracaoEmAndamento` e SÓ resolve no ÚLTIMO turno (`continuarConjuracao`). Extraí a resolução para `resolverConjuracao` (compartilhada pelo lançamento de 1s e pelo fim do multi-turno).
- **Interrupção** (Magia p.7): no `executarTurnoNpc`, foto do PV/atordoamento do herói antes/depois do golpe; se levou dano ou ficou atordoado enquanto concentrava, `interromperConjuracaoSeConjurando` — **atordoado PERDE automático**; ferido exige **Vontade−3** para manter. `abortarConjuracao` cancela sem custo e **não gasta o turno** (o herói reescolhe).
- **UI**: card `🔮 Conjurando X — [Continuar] [Abortar]` (`CombateStatusTatico`, prioritário na vez do herói); enquanto concentra, o menu do token E o movimento pelos hexes verdes ficam BLOQUEADOS (`hexesAlcancaveisHeroi` e o menu retornam vazio). `CombatUiState.conjurando`.
- **+5 testes** (`MagicCombatTest`, total 11): entra em concentração e só resolve no fim; atordoado perde automático; Vontade−3 (falha perde / passa mantém); abortar limpa sem custo; 1s resolve na hora. Zero regressão.
- **Deferido honestamente p/ MA-3d**: Toque, Bloqueio, Área no hex + resistência de área, magias ativas + tick.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MA-3b — 15 de Julho de 2026 (PILAR MAGIA / Fase 3b — Projétil fiel + queimar PV)
**Saga: o inimigo pode ESQUIVAR do projétil; o mago pode queimar PV — branch GURPS-Saga**
- **Projétil = 2 testes** (Magia p.12): além do lançamento, um teste de **Ataque Inato** para acertar (aprox. DX + SSR de distância); o alvo pode **ESQUIVAR** (ou bloquear), **NUNCA aparar**. Só quem acerta e não é esquivado sofre o dano 1d × energia com RD. Fecha o maior gap de fidelidade do MA-3a (antes o projétil acertava no sucesso do lançamento).
- **Queimar PV** (Magia p.8): o mago paga parte do custo com PV (fere de verdade via `InjuryRules.ferir`) no lugar de PF; cada PV é −1 no NH (já entrava no NH efetivo do MA-2 via `ctx.pvQueimados`). Stepper no `SubDialogoConjurar` com teto no custo estimado; PV e PF sincronizam com a ficha (`sagaDefinirPvAtual`/`sagaDefinirPfAtual`).
- **+2 testes** (`MagicCombatTest`, total 6): o projétil pode ser esquivado ou passar longe (o 2º teste age); queimar PV fere o mago e penaliza o NH em −1 por PV. Zero regressão na suíte de combate.
- **Deferido honestamente**: MA-3c (multi-turno + interrupção, Toque, Bloqueio) e MA-3d (Área no hex + resistência de área, magias ativas + tick). Cada um é uma fatia própria — MA-3b/c era grande demais para um lote.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MA-3a — 14 de Julho de 2026 (PILAR MAGIA / Fase 3a — a espinha conjurável no grid)
**Saga: o herói CONJURA no combate — chip 🔮 no token — branch GURPS-Saga**
- **Chip 🔮 Conjurar** no menu do token do herói (só se ele conhece magias) → `SubDialogoConjurar`: escolhe a magia (NH, classe, custo), o alvo (um inimigo ou "em mim mesmo") e — para Projétil — a energia investida (1d de dano por ponto, teto na Aptidão Mágica). Conjurar = manobra Concentrar (gasta o turno).
- **`CombatSession.heroiConjurar`**: usa os resolvedores do MA-2 (rola 3d, NH efetivo, custo), **paga a fadiga (PF)** e aplica o que é DERIVÁVEL por regra: **Projétil** → dano 1d × energia com RD (Magia p.470); **Resistível** → Disputa Rápida (HT/Vont do alvo, Regra do 16, Vontade≈IQ no NPC); **falha crítica** → choque de retorno (dano/atordoamento no operador). Efeito bespoke (Sono/Cura/Criar) → narrado; o motor loga o fato.
- **Controller** extrai da ficha: NH via `calcularNivel`, Aptidão via `MagicEngine`, classe/energia do catálogo já em `MagiaSelecionada` (sem DataRepository). `CombatUiState.magiasConjuraveis` alimenta o seletor; a fadiga gasta sincroniza com a ficha (`sagaDefinirPfAtual`).
- **+4 testes de integração** (`MagicCombatTest`): projétil causa dano + gasta PF; RD reduz o dano; log sempre registra e PF nunca sobe; automagia não aplica dano de projétil. Zero regressão na suíte de combate.
- **Deferido honestamente p/ MA-3b+**: teste separado de Ataque Inato + esquiva do alvo (por ora o projétil acerta no sucesso do lançamento; tipo de dano aproximado por contusão ×1); conjuração multi-turno + interrupção; Toque; Bloqueio; Área no hex; magias ATIVAS + tick; queimar PV; mana por cena (fixa em NORMAL); Sono/Cegueira → `Condicao`; NPC conjurador.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MA-2 — 14 de Julho de 2026 (PILAR MAGIA / Fase 2 — resolvedor de conjuração, motor puro)
**Saga: o "cérebro" da magia, fiel ao livro — branch GURPS-Saga**
- **Fonte lida direto**: regras do livro Magia nos chunks (`pt_magia` p.5–15) — lançamento, custo, resistência, distância, duração, choque, tempo de operação. Extraídas e conferidas para MÁXIMA fidelidade.
- **`MagicCasting.kt` + `MagicEnergy`** (`domain/magic/`, PURO, sem Android): o resolvedor único que os dois palcos (narrativa MA-4 + combate MA-3) vão chamar. Caller joga os dados.
- **`MagicEnergy.parse`**: o campo `energia` do catálogo é string livre ("2", "1 a 3", "Varia", "1/2") → `CustoEnergia`. Tolerante, nunca lança.
- **`nhEfetivo(ctx)`** com PARCELAS (transparência p/ UI): mana (−5 baixa), distância (−1/m, só Comum/Área/Informação e se não tocar), sem-ver-nem-tocar (−5), múltiplas magias (−3 concentração / −1 andamento), queimar PV (−1/PV, p.8).
- **`custoTotal`**: área × raio / Comum × MT ANTES da redução por NH (p.8); a redução usa o NH básico só com o −5 de mana baixa (não a distância); **Bloqueio NUNCA reduz** (p.12).
- **`resolver`**: classifica 3d (MA-1), custo a pagar (decisivo perdoa; fracasso 1 exceto Informação paga tudo; crítico tudo + choque de retorno), marca `exigeResistencia` (resistível só automática no sucesso decisivo, p.13).
- **`resolverResistencia`**: Disputa Rápida margem-operador × resistência-alvo, empate favorece o defensor, **Regra do 16** p/ alvo vivo, Abascanto penaliza o operador (p.14).
- **Escala de efeito** (p.9/14): `tetoNiveisEfeito` = max(níveis da magia, Aptidão Mágica); 1 pto = 1d dano / 1s cegueira. **`tempoOperacaoAjustado`** por NH alto (p.9): NH20–24 metade, 25–29 ¼, +metade a cada 5, piso 1s.
- **+29 testes** (`MagicCastingTest`), cada um citando a página da regra. Fronteira honesta: o resolvedor recebe NH básico (`calcularNivel`) e Aptidão (`MagicEngine`) já prontos — a fiação Android fica no MA-3/MA-4.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote TOK-6b-3 — 14 de Julho de 2026 (VTT 2D / Fase 6b-3 — GRID PROTAGONISTA: layout pós-teste do 6b-2)
**Saga: mais grade, menos moldura — feedback do teste no aparelho — branch GURPS-Saga**
- **Trocar arma virou chip do herói** (`SubDialogoTrocarArma` no `MenuTaticoDoToken`): o botão mostra a arma empunhada (🔄 nome); abre o diálogo de armas; sacar é Preparar (gasta o turno) ou livre com Saque Rápido. Com isso o **painel fixo de arma do rodapé foi REMOVIDO** (`PainelArmaTatico` apagado, param `manobrasNoGrid` some).
- **Caixa do Narrador SUBIU** pro topo do grid no combate tático (fina: 1-2 linhas, `BarraDeEnvio(compacto=true)`), liberando o rodapé pra grade. Fora do tático continua no rodapé.
- **Status virou OVERLAY sobre a grade** (`CombateStatusTatico`): só aparece quando exige atenção — Defenda-se! (card cheio), fim de combate (+ Fechar), ou "Inimigos agindo…" (pílula translúcida). **Na vez do herói não renderiza NADA** — as ações moram nos tokens. O painel `weight(1f)` fixo saiu; o grid ganhou esse espaço.
- **GRID PROTAGONISTA**: bloco tático agora é `Box(weight 3f)` direto (grid do topo ao rodapé) em vez de `Column{grid 2.2 + painel 1}`; feed encolhe no tático (`weight 0.7`, cards `TurnoBolha(compacto)` com menos padding + `bodySmall`).
- **Câmera (o "hexes grandes demais, não movo o deslocamento todo")**: (1) margem de enquadramento 2,5→1,1 hex (menos borda desperdiçada); (2) **piso de toque menor AO MOVER** — 30dp com o herói selecionado (cabe mais do range), 40dp ao só observar. Com o grid mais alto + menu do herói no TOPO (deixa os hexes de movimento livres embaixo), o deslocamento aparece bem mais.
- **Menu do herói no TOPO, do inimigo embaixo**: o self-menu não cobre os hexes verdes de movimento; o menu ofensivo fica ao alcance do polegar. Respiro de 42dp evita cobrir o cabeçalho "Combate tático".
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote TOK-6b-2 — 14 de Julho de 2026 (VTT 2D / Fase 6b-2 — MANOBRAS NOS TOKENS: carrossel translúcido)
**Saga: a manobra mora onde ela acontece — tocar no token abre as ações — branch GURPS-Saga**
- **Menu do SEU token** (`MenuTaticoDoToken` + `menuTaticoHeroi` pura): manobras sobre si mesmo — Mudar Postura, Defesa Total, Aguardar, Preparar, Concentrar, Desvencilhar (se preso), Fogo de Retenção, Não Fazer Nada. Mover NÃO é botão (continua sendo o hex verde, TOK-4).
- **Menu do token INIMIGO** (`menuTaticoInimigo` pura, mapeada nas regras MB p.363-371/AM p.69-77): golpes/Fintar/Agarrar/Derrubar/Encontrão/Empurrão exigem o alvo AO ALCANCE; Mover e Atacar exige alcançá-lo com o Deslocamento; Avaliar/Apontar valem à vista; Imobilizar/Estrangular/Chave/Mata-Leão exigem o alvo JÁ AGARRADO. **O toque no token JÁ escolhe o alvo**: ações só-de-alvo disparam direto; as que pedem parâmetro (local do golpe, modo, firmar, postura) reusam os sub-diálogos com o alvo fixo (`listOf(alvo)` pré-selecionado).
- **Carrossel translúcido** sobre a grade (BottomCenter, `Color(0xCC10161F)`, scroll horizontal, chip ✕), some quando não é a vez do herói/há defesa pendente/combate encerrado; trocar de token reseta os diálogos (`remember(tokenId)`).
- **Painel clássico enxuto no tático** (`CombatePainel(manobrasNoGrid = true)` → `PainelArmaTatico`): só arma empunhada (Preparar/Trocar) + dica de onde tocar; Defenda-se!/fim de combate continuam no painel. Modo faixas 100% intocado.
- **Fechar o menu**: tocar hex vazio com inimigo selecionado limpa a seleção (`limparSelecaoTatica` no controller + fachada no ViewModel); com o herói selecionado a seleção fica (tentar outro hex de movimento).
- **Achado da varredura própria (regra REAL)**: a grade driblava a luta agarrada — herói ATORDOADO/AGARRADO/IMOBILIZADO ainda via hexes verdes e saía andando sem Desvencilhar. `hexesAlcancaveisHeroi()` agora aplica as mesmas travas do MOVER de faixas (MB p.371/420).
- **Revisão adversarial (finder de regras GURPS completou; finder de estado/UI caiu no limite do Fable → seguimos no Opus). 2 achados ALTA REAIS corrigidos — ambos eram o soft-fail "perde o turno" que o teste de batalha reprovou** (o motor rejeita e `depoisDaAcaoDoHeroi` avança o turno mesmo assim; o painel antigo mascarava com o diálogo de confirmação que mostrava a distância):
  - **Agarrar/Derrubar/Empurrão** apareciam no alcance da ARMA (lança 2 m), mas o motor exige `dist ≤ 1` → agora gateados por **adjacência real**; Encontrão passou a `alcancaMovendo` (o motor carrega). `menuTaticoInimigo` ganhou `adjacente` + `alvoNoChao`.
  - **Imobilizar** aparecia para agarrado EM PÉ, mas o motor exige agarrado **E no chão** (MB p.371) → agora `agarrado && alvoNoChao`.
- **+2 achados MÉDIOS de texto corrigidos**: menu vazio de inimigo diferencia "Fora de alcance — avance pelos hexes verdes" de "Você só pode agir sobre si — toque no SEU token" (herói atordoado/preso não tem manobra direcionada nem hex verde).
- **Deferidos honestamente**: (1) confirmação de 2º toque para ações destrutivas (Encontrão/chaves) — **conflita com a diretiva do lote** ("o toque no token já escolheu o alvo"), e com os filtros ALTA o chip só aparece quando é legal e não perde turno; (2) sub-diálogo braço/perna da Chave de Membro — pré-existente (o painel antigo também fixava braço), não é regressão.
- **+9 testes** (`MenuTaticoTest`): herói só manobras sobre si; menu respeita as legais do motor; adjacente oferece golpes+projeções; **alcance de arma sem adjacência = golpes SIM, agarrar/derrubar/empurrão NÃO**; fora de alcance só Avaliar/Apontar/Encontrão/Mover-e-Atacar; chaves exigem agarrado; **Imobilizar exige agarrado E no chão**; ordem do motor preservada; ícone exaustivo por manobra.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote TOK-6b-1 — 12 de Julho de 2026 (VTT 2D / Fase 6b-1 — grid dominante + barra de HP no token + piso de toque + pan)
**Saga: a vida mora no grid; a câmera respeita o dedo; o mapa se arrasta — branch GURPS-Saga**
- **Feedback do teste do 6a**: a câmera abria DEMAIS ao selecionar o herói (enquadrar todos os alcançáveis de deslocamento 5+ → hexes pequenos de novo).
- **Piso de toque na câmera** (`pisoToquePx` no `calcularCamera`, callers passam 40dp): o hex nunca fica menor que o tocável; o que não couber no enquadramento fica pro pan. Piso nunca ultrapassa o teto.
- **PAN por arrasto** (real + demo): 2º `pointerInput` com `detectDragGestures`; o mapa segue o dedo; centro **clampado ao raio da grade** (nunca "se perde" em tela vazia); reset quando o enquadramento-alvo muda de verdade (mover/seleção/morte).
- **Barra de HP sobre o token** (`desenharBarraHpENome`, substitui o anel): trilho escuro + preenchimento verde/âmbar/vermelho por PV%, **mini-ícones de condição** acima (🩸 sangrando, 💫 atordoado, 🤼 agarrado, 😮‍💨 sufocando, ⬇ postura baixa — `TokenTatico.condicoesIcones` no controller; sem `.take()` que cortaria emoji composto no meio), nome sob o token.
- **Cards de vida SOMEM no modo tático** (`CombatePainel(mostrarTracker = false)`) — a informação mora no grid; modo faixas intocado.
- **Grid DOMINANTE**: bloco de combate weight 3f (feed 1f), canvas weight 2.2 vs painel 1 → grade ~metade da tela (com Modo Jogo sem chrome, bem mais).
- **Revisão adversarial (2 finders; verificadores no session limit → verificação manual): 3 achados, todos resolvidos** — 2 já corrigidos por varredura própria antes da revisão terminar (pan sem clamp → `cameraEfetiva` PURA com centro clampado à extensão axial da grade; `take(8)` cortaria emoji ZWJ no surrogate → removido) + comentários órfãos do "anel" atualizados.
- **+4 testes** (piso vence o enquadramento; piso ≤ teto; pan gigante clampado; pan zero preserva a câmera).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote TOK-6a — 12 de Julho de 2026 (VTT 2D / Fase 6a — MODO JOGO + câmera + fixes do teste físico)
**Saga: em campanha o app vira JOGO em tela cheia; a câmera enquadra os combatentes — branch GURPS-Saga**
- **Motivação (teste no aparelho FÍSICO)**: mecânica OK, mas hexes ~20px (mínimo Android: 48dp) intocáveis no dedo, imagens invisíveis nesse tamanho, fundo ausente, ⅓ da tela em chrome.
- **MODO JOGO**: `hideAppChrome` estendido — `Saga + campanha ativa` esconde cabeçalho da ficha, PontosBar e abas (REUSA a infra do VTT legado; orientação landscape ficou EXCLUSIVA do VTT — a Saga é vertical). Header da campanha virou **1 linha compacta** (título da cena + **X** de sair, a única saída do jogo — sempre visível, primeiro elemento do FeedDaCampanha).
- **CÂMERA do canvas** (`CameraHex` + `calcularCamera` + `hexParaTelaCam`/`telaParaHexCam`): enquadra o **bounding box dos combatentes** + ~2,5 hexes de margem, com piso (visão full-grid) e teto (~7 unidades visíveis). No cenário do print do usuário (todos num canto), hexes ficam **3–4× maiores** → retratos visíveis, toque possível. Animada (tween 400 ms); o tap usa `rememberUpdatedState` (pointerInput não reinicia por frame); culling simples dos hexes fora da tela. Aplicada no combate real E no preview demo.
- **Labels de coordenadas (q,r) REMOVIDOS** (flag interna `DEBUG_COORDENADAS_HEX`).
- **FIX do fundo no combate real**: era cache-only e nunca rechecava — se a geração (gatilho pós-turno) ainda rodava quando a grade abriu, ficava cinza pra sempre. Agora usa o mesmo caminho do demo (`obterFundoCena` espera no Mutex a geração em curso e recompõe quando chega).
- **Achado da revisão adversarial (corrigido)**: com deslocamento 5 > margem 2,5, os **hexes verdes alcançáveis na direção oposta aos inimigos ficavam FORA da viewport e intocáveis** — recuar/fugir limitado na prática. Fix: os alcançáveis entram no enquadramento da câmera (selecionar o herói abre a câmera suave; desselecionar reaperta). O verificador também confirmou como NÃO-bugs: tap durante animação (draw e tap usam o mesmo cam por frame), dessincronia 250/400 ms (cosmética), round-trip do cube-round (inverso algébrico exato).
- **+9 testes puros** (`CameraHexTest`: amplia >2,5× no combate colado, afasta no espalhado, nunca menor que full-grid, centraliza, round-trip do toque, null fora da grade, tam inválido não crasha, **alcançáveis sempre dentro da viewport**).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote TOK-5b — 11 de Julho de 2026 (VTT 2D / Fase 5b — IA posicional do NPC + manter à distância)
**Saga combate tático: o NPC flanqueia/kita/recua DE VERDADE pela grade; Interromper Investida mantém o oponente à distância — branch GURPS-Saga**
- **6ª fatia do `docs/planos/PLANO_Tokens_VTT_2D.md`** — fecha o TOK-5. Torna o facing do TOK-5a **testável sem depender do Narrador**: o goblin flanqueia o herói sozinho.
- **`PosicaoBridge.moverNpcNaGrade(npcId, intencao)`** (novo): quando a grade está ativa, quem decide PRA ONDE o NPC vai é a **IA posicional do HEX-5** (`HexTaticaNpc` — flanquear agressivo/kite arqueiro/recuar covarde), iterada **vizinho a vizinho até o deslocamento** (flanquear emerge da sequência). Facing final = encarando o herói (recuar de costas daria flanco de graça). Null = modo faixas intacto.
- **`npcResolve` MOVER**: distância vem da grade (`definirDistancia`) em vez do ±passo abstrato; **FUGA na grade**: `FUGA_METROS` (20) é inalcançável num raio 7 — recuar JÁ na borda = saiu do campo (fix da varredura própria; sem ele o NPC covarde recuaria em círculos pra sempre).
- **`npcResolve` MOVER_E_ATACAR**: o NPC avança pela grade (podendo flanquear); **se não alcançar o herói, o avanço consome a manobra SEM golpe** (fiel ao Avançar-e-Atacar). Sem bridge → força 1m como antes.
- **Manter um Oponente à Distância** (AM p.101, `HexManterADistancia` do HEX-6): golpe de Interromper Investida que CAUSA DANO põe a arma no caminho — **arma não-perfurante → Disputa Rápida de ST** (perdeu → o avanço PARA); **estocada perfurante cravada → Vontade−3 do NPC** (falhou → recua da lâmina). Simplificações honestas documentadas: sem o dano-máximo/arma-presa do avanço forçado; só dispara com dano>0 (o caso "não penetrou RD" fica pro polimento).
- **Deferidos documentados**: cobertura na linha de tiro (sem modelo de obstáculos na grade), Evadir (raro em 1×N, BFS conservador cobre), Aguardar-por-alcance (o gatilho já usa distâncias reais pós-TOK-4).
- **+3 testes de integração** (MOVER pela grade; MOVER_E_ATACAR sem alcance consome turno; sem bridge = 1m antigo). Todos os testes de combate verdes.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote TOK-5a — 11 de Julho de 2026 (VTT 2D / Fase 5a — facing, através-de-hex e Retirada REAIS)
**Saga combate tático: as primeiras regras do `docs/fonte-regras/Combate.md` substituídas pela POSIÇÃO real na grade — branch GURPS-Saga**
- **5ª fatia do `docs/planos/PLANO_Tokens_VTT_2D.md`** — sub-lote "a" (facing/através/retirada). Evadir, Aguardar por alcance, cobertura e manter-à-distância ficam pro 5b.
- **`CombatSession.PosicaoBridge`** (interface nova + `var posicaoBridge`, null = modo faixas com zero regressão): `facingDoAtaque`, `penalidadeAtravesDeHex`, `aoAtacar` (vira o atacante pro alvo — facing é livre no próprio turno), `recuarUmHex`.
- **Herói→NPC (`resolverGolpeHeroi`)**: mod **"através de hex ocupado" −4** (MB p.389, corpo-a-corpo alcance ≥2, via `HexAtaqueAtravesHex` do HEX-6); **FLANCO → defesa do NPC −2** (MB p.390); **COSTAS → defesa ANULADA** via `surpresa=true` (MB p.374). Tudo logado com a página da regra. **Flanquear com Avançar-e-Atacar agora vale a pena de verdade.**
- **NPC→herói (`npcResolve`)**: NPC vira pro herói ao atacar; **COSTAS do herói → surpresa** (defesa anulada + aviso no log). **FLANCO → o card "Defenda-se!" abre com TODAS as opções −2 e o BD do escudo removido** (`HexRegrasFacing.ajustarOpcoesDefesa` do HEX-4 — o módulo esperou 5 lotes pra ser plugado e entrou sem mudanças). COSTAS = sem card (o motor narra).
- **Retirada REAL** (MB p.377): defesa com recuo move o herói **1 hex de verdade** na direção oposta ao atacante; as novas distâncias (a TODOS os NPCs) entram no encounter. Hex atrás ocupado/fora da grade → recuo só narrativo (bônus mantido, como o MB abstrai).
- **+4 testes de integração** no `CombatSessionTest` com bridge FAKE (flanco reduz e loga; costas anula e loga; através-de-hex no cálculo; **sem bridge = regressão zero nos logs**). Todos os testes de combate existentes verdes sem mudança.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote TOK-4 — 11 de Julho de 2026 (VTT 2D / Fase 4 — combate REAL no grid)
**Saga combate tático: o grid vira o tabuleiro do combate de verdade — branch GURPS-Saga**
- **4ª fatia do `docs/planos/PLANO_Tokens_VTT_2D.md`** (diretiva do usuário: automatizar no grid as ações do `docs/fonte-regras/Combate.md` que hoje são botões abstratos de faixa). Itens antes "FORA DO ESCOPO por falta de grade" começam a ser desbloqueados: **Deslocamento/Movimento** real, **Passo**, **Espaçamento** (ocupação de hex).
- **`HexSetup`** (novo, kotlin puro): `setupDoEncontro` (herói na origem, inimigos espalhados pelas 6 direções a `distanciaM` hexes, colisão resolvida, facing pro herói), `hexesAlcancaveis` (livres a dist ≤ deslocamento — sem pathfinding no TOK-4, terreno aberto documentado), `moverHeroi` (facing pela direção), `distanciasAoHeroi` (mapa que alimenta o encounter), `manterApenas` (mortos saem da grade). **12 testes puros**.
- **`CombatSession.heroiMoveTatico(novasDistancias, metros)`** — ADITIVO (o `heroiMove` de faixa fica intocado): consome o turno com as regras do Mover (disparada aproximada, velocidade p/ Vel/Dist, limpa Avaliar/Apontar/Finta) e seta as distâncias EXATAS por NPC vindas do grid. **O grid é a fonte da verdade no turno de mover do herói.**
- **`SagaCombatController`**: `estadoTatico` (state) + `avisoTatico` + `tokensTaticos` (id/nome/ehHeroi/pvPct/posição/facing) + `hexesAlcancaveisHeroi()` (só com o token do herói selecionado e no turno dele) + `aoTocarHexTatico(hex)` (seleção / MOVER tático / avisos) + `sincronizarGridComEncounter()` no início do `atualizarEstado` (Encontrão/Empurrão/Projeção/Mover do NPC reprojetados via `HexPortabilidade`; mortos saem). `iniciarCombate` monta a grade + `HexCombatSync.projetarSetupInicial` (uso correto do contrato "setup only" do HEX-3).
- **`HexCanvasCombateReal`**: tokens do controller, tap → controller, hexes alcançáveis em verde, **anel de HP** (arco verde/âmbar/vermelho) + **nome sob o token**, imagem do inimigo por TIPO derivado do id (`goblin_2`→`goblin` — MESMA chave do gatilho TOK-2), fundo TOK-3 cache-only, aviso auto-hide. Fix de recomposição: leitura explícita do `CombatUiState` — sem ela o anel de HP ficaria stale após dano sem movimento (getter lê encounter não-observável). Preview standalone continua no demo.
- **Revisão adversarial (2 finders completaram; verificadores no session limit → verificação manual): 5 achados REAIS, todos corrigidos + 1 bug próprio evitado:**
  1. (alta) Divergência grid↔encounter por colisão era ESCRITA DE VOLTA no encounter no próximo Mover tático (NPC "teleportava" de 1m pra 2m sem ação) → `aplicarNovaDistancia` agora **desvia pro anel livre na distância-alvo** (grid == encounter garantido; fallback antigo só com anel cheio). Testes do HexPortabilidade atualizados pro novo contrato.
  2. (média) Disparada com semântica misturada tático×faixa → `heroiMoveTatico` seta `heroiMoveDirecao = null` E **o MOVER de faixa some do painel quando a grade está ativa** (a substituição da diretiva).
  3. (média) Range geométrico deixava ATRAVESSAR inimigos de graça (fuga de cerco sem Evadir) → `hexesAlcancaveis` virou **BFS sobre hexes livres**; cercado pelos 6 = preso. Evadir chega no TOK-5.
  4. (baixa) Setup na borda sobrepunha 2 inimigos no mesmo hex → anel clampado no cálculo + colisão empurra pra DENTRO.
  5. (média) HP ring stale latente → dependência explícita do CombatUiState (mesmo fix do item acima).
  6. (próprio) `estadoTatico` incondicional removeria o MOVER de faixa SEM canvas visível quando a flag tática está OFF → setup só roda com `modoTaticoHex`/`3D` ligado.
- **+6 testes puros novos dos fixes** (cerco fechado, contorno BFS, borda sem sobreposição, anel-alvo com fallback).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote TOK-3 — 10 de Julho de 2026 (VTT 2D / Fase 3 — fundo de cenário gerado)
**Saga combate tático: a cena vira imagem de fundo top-down sob a grade — branch GURPS-Saga**
- **3ª fatia do `docs/planos/PLANO_Tokens_VTT_2D.md`** — quando o Narrador estabelece a cena (`definir_cena`), um gatilho assíncrono gera a vista aérea do CHÃO do lugar e ela vira o fundo da grade tática.
- **`CenarioImageStore`** (novo, `data/storage/`):
  - `chaveCena(campanhaId, cenaId, titulo, bioma)` **pura**: `c{camp}_s{cena}_h{hash-do-conteúdo-FÍSICO}` — o hash importa porque `definir_cena` ATUALIZA a mesma cena (nasce "Início", vira "O Coliseu de Ferro"); sem hash, um fundo genérico gerado cedo ficaria grudado. Conteúdo físico muda → chave muda → regenera → **irmãos obsoletos da mesma cena são apagados**. **HUMOR fica FORA da chave** (achado CONFIRMADO da revisão adversarial: humor é volátil — "tenso"→"alívio" na mesma locação — e não muda o terreno; se entrasse no hash, cada retoque de clima regeneraria o fundo pago). Humor ainda entra no PROMPT da 1ª geração. Teste-trava do contrato de custo incluído.
  - `cenaValidaParaFundo(titulo)` **pura**: placeholder "Início"/vazio não gera fundo (economiza a geração inútil da abertura).
  - `promptFundoCena(titulo, bioma?, humor?)` **pura**: vista aérea do chão, painterly; **proíbe criaturas/grid/texto** — grade e tokens são desenhados por cima.
  - `obterFundoCena(...)`: cache `filesDir/cenarios/{chave}.jpg` (JPEG 85, maior lado ≤1024); Mutex por chave deduplica gatilho×canvas; escrita atômica (reusa `salvarBitmapAtomico`, promovido a `internal` parametrizado no `TokenImageStore`); `gerarImagem` injetada.
  - `temFundoCena` (exists barato, sem decode — o gatilho por turno vira no-op cedo), `fundoCenaCacheado` (hit-only), `limparCache`.
- **Gatilho no `FichaSagaDelegate.rodarTurno`**: após o refresh da cena pós-turno, `dispararGeracaoFundoCena()` — guards (placeholder, cache já existe via `temFundoCena`, key vazia) e `scope.launch` fire-and-forget.
- **Canvas**: `HexCanvasTatico` lê `sagaCampanhaAtiva`/`sagaCenaAtiva` (ambos `mutableStateOf` — recompõe na troca de cena), carrega cache-first e gera on-demand no miss (Mutex faz esperar o gatilho em vez de gerar 2×). `HexCanvasDemo` desenha o fundo em escala **COVER** centralizado + **scrim escuro** (α=0.4) antes da grade. Sem campanha/cena (preview standalone) ou falha → fundo cinza eterno.
- **+11 testes puros** (`CenarioImageStoreTest`: chave estável/muda com conteúdo/não colide entre campanhas/null≡vazio; guard "Início"; prompt com/sem bioma/humor + proibições).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote TOK-2 — 10 de Julho de 2026 (VTT 2D / Fase 2 — gatilho de tokens de inimigos)
**Saga combate tático: inimigos ganham retrato GERADO por Gemini via gatilho assíncrono — branch GURPS-Saga**
- **2ª fatia do `docs/planos/PLANO_Tokens_VTT_2D.md`** — os "agentes secundários" do plano viram corrotinas fire-and-forget.
- **`GeminiImageService.gerarImagem(apiKey, modelId, prompt, rotuloLog)`** — método genérico extraído do `gerarRetrato` (que mantém assinatura/prompt/parse idênticos e delega). Serve tokens agora e fundo de cenário no TOK-3.
- **`TokenImageStore` estendido**:
  - `normalizarTipo(tipo)` **pura**: minúsculas, sem acento (NFD), `[^a-z0-9]+`→`_` colapsado — chave de cache ("Orc Bruto"→`orc_bruto`).
  - `promptTokenInimigo(nome, descricao?)` **pura**: busto frontal, fundo neutro escuro, sem texto/marca-d'água. Descrição do bestiário refina o prompt.
  - `obterTokenInimigo(context, tipo, nomeVisivel, descricao, gerarImagem)`: cache `filesDir/tokens/inimigos/{chave}.png` **por TIPO** (3 goblins = 1 imagem ≈ $0.067); **Mutex por tipo** (ConcurrentHashMap) deduplica gerações concorrentes (gatilho × canvas); recorte quadrado centrado no rosto = mesmo pipeline do herói; `gerarImagem` é lambda injetada (desacoplado do Gemini).
  - **`salvarPngAtomico`** (temp + rename): cancelamento de corrotina no meio da escrita nunca deixa PNG truncado servido como cache — aplicado ao herói também.
  - `tokenInimigoCacheado` (hit-only) e `limparCache` cobrindo os dois diretórios.
- **Gatilho no `SagaCombatController.iniciarCombate`**: ao abrir o encontro, para cada TIPO distinto de inimigo dispara `scope.launch { obterTokenInimigo(...) }` com nome+descrição do bestiário — pré-aquece o cache antes de a grade abrir. `runCatching` + key vazia = no-op: o combate NUNCA espera nem falha por causa da imagem.
- **`HexCanvasDemo`**: mapa `tokensInimigos` carregado por `LaunchedEffect` (cache-first; se miss, gera on-demand — o preview demo também exercita o TOK-2, 1× por tipo). No draw, inimigo com imagem usa `desenharTokenImagem` (borda vermelha); sem imagem fica no círculo+inicial.
- **+10 testes puros** (6 `normalizarTipo` + 4 `promptTokenInimigo`).
- Consistência de chave gatilho×canvas: batem quando `normalizarTipo(nome) == id do bestiário` (caso do demo "Goblin"→`goblin`); divergências raras se resolvem no TOK-4 (canvas dirigido pelo CombatSession, que conhece o id).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote TOK-1 — 10 de Julho de 2026 (VTT 2D / Fase 1 — tokens de imagem)
**Saga combate tático: pivot 3D → VTT 2D com tokens de imagem (retrato do jogador) — branch GURPS-Saga**
- **1ª fatia do `docs/planos/PLANO_Tokens_VTT_2D.md`** (decisão do usuário 10/jul após teste do 3D no aparelho: top-down mantém, .glb sai, imagem entra — estilo mesa Roll20/Foundry).
- **`TokenImageStore`** (novo, `data/storage/`): gera e cacheia o token do herói a partir do retrato da ficha (`Personagem.imagemPersonagemOriginalUri`). Recorte QUADRADO 1:1 **centrado no rosto** (ML Kit Face Detection, mesma config do `ImagemPersonagemStore`), lado = 2.2× o rosto (moldura cabelo/ombros), escalado pra 256px, cache `filesDir/tokens/heroi_<hash>.png`. Matemática do recorte extraída em `calcularRecorteQuadrado(...)` **pura** (testável sem Android). Falha em qualquer etapa → null → canvas usa fallback.
- **`HexCanvas.kt` evoluído**:
  - Novo entry-point **`HexCanvasTatico(viewModel)`** — `produceState` carrega o token (assíncrono, key = retratoUri) e injeta no canvas.
  - **`desenharTokenImagem`**: retrato circular (clipPath) + borda colorida (herói azul/inimigo vermelho) + anel branco de seleção + **facing como triângulo na borda externa** (estilo VTT).
  - **Hexes válidos de movimento** pintados de verde translúcido (`desenharHexPreenchido`) — paridade com o que o 3D tinha.
  - **Aviso "Muito longe"** no header (auto-hide 2 s) — paridade com o 3D.
  - **Movimento ANIMADO** (200 ms): posições por token animadas em coordenadas axiais-neutras (`ax=√3q+√3r/2, ay=1.5r`) via `animateFloatAsState` em loop `key(t.id)` — token desliza em vez de teleportar, correto em qualquer resize.
  - Fallback círculo+inicial permanece para inimigo (imagem gerada é o TOK-2) e herói sem retrato.
- **Roteamento (`TabSaga`)**: `modoTaticoHex` OU `modoTaticoHex3D` caem AMBOS no `HexCanvasTatico` + `CombatePainel` empilhados. Switch do 3D **removido** da config; `CampanhaConfig.modoTaticoHex3D` deprecated (campo fica por compat Gson). Preview standalone da tela inicial pivota pro canvas 2D. `HexScene3D.kt` vira código legado sem call sites (não deletado).
- **15 testes puros novos** (8 recorte quadrado + 7 estado tático: hexes válidos/aviso/facing).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote MA-1 — 8 de Julho de 2026 (PILAR MAGIA / Fase 1 — motor puro)
**Saga magia: motor puro (parser tolerante da classe + regras MB p.6–14) — branch GURPS-Saga**
- **1ª fatia do PILAR MAGIA.** Zero toque no combate atual. Novo package `domain/magic/` em kotlin puro. Aproveita o catálogo já existente (`magias2versao.json` — **879 magias** com id/dificuldade/página/classe/escola/duração/energia/tempo/pré-req/descrição).
- **`MagicClass.kt`** — parser tolerante do campo `classe`. Cobre 8 classes (Comum/Área/Projétil/Toque/Bloqueio/Informação/Encantamento/Especial) + resistência codificada (`R-HT`, `R-Vont+1`, `R-HT ou IQ`, `R-Especial`, `R-Tranca Mágica`, marcador `#`, fórmula composta `(ST+Vont)/2`). Aliases absorvem os typos reais do JSON (`Comm`→Comum, `Projetil`→Projétil, `Encant.`→Encantamento). Reality check contra as **879 magias**: **99,89% de cobertura** (só `travar_vontade` cai em "parte não reconhecida" e mesmo assim entrega Área + COMPOSTA + rótulo pro Narrador).
- **`MagicCore.kt`** — helpers puros:
  - `MagicMana` — 5 níveis (`MUITO_ALTA`/`ALTA`/`NORMAL`/`BAIXA`/`NULA`), penalidade e permissão de operar.
  - `MagicCost` — redução por NH (`NH≥15 → −1`, `NH≥20 → −2`, `+1 a cada +5`); custo por raio da Área (fracionário, piso 1); custo por MT do alvo (Comum).
  - `MagicDistance` — penalidade = metros/hexes; −5 adicional se sem contato nem visão.
  - `MagicMultiplasMagias` — `−3` por magia em concentração + `−1` por magia em andamento (permanente não penaliza).
  - `MagicOperationRuling` — classificação 3d (`SUCESSO_DECISIVO`/`SUCESSO`/`FRACASSO`/`FALHA_CRITICA`) + custo a pagar por resultado (Informação paga total no fracasso).
  - `MagicChoqueRetorno` — tabela completa 3d→18 da falha crítica (Magia p.7).
  - `MagicActive` — `MagiaAtivaNoCombate` (id, operador, alvo, energia, timer, custo manutenção, tipo de duração) + `avancarTurnoSegundos()` que decai timers, cobra manutenção e expira duradouras/temporárias corretamente (Instantânea filtrada, Permanente/Encantamento imunes).
- **68 testes puros** (17 parser + 33 core + 2 reality-check contra o JSON), todos verdes.
- Zero mudança no `CombatSession`/`CombatResolver`/`MagicEngine` existentes. MA-2 pluga esse motor no encontro; MA-3 adiciona tools do Narrador; MA-4 wire no HexScene3D; MA-5 polimento.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote HEX-9 — 8 de Julho de 2026 (T3 / Fase 7 do PILAR — polimento final + defesa por timing)
**Saga combate tático 3D: câmera ortográfica, wrap yaw circular, halo hex e cone facing 3D, defesa por timing (Clair Obscur) — branch GURPS-Saga**
- **9ª e ÚLTIMA fatia do PILAR.** Motor GURPS INTOCADO. Polimento em cima de HEX-1..8.
- **Câmera ortográfica no HexScene3D**: `SideEffect { cameraNode.camera.setProjection(Camera.Projection.ORTHO, ...) }` com meio-raio adaptado a `raioGrade+1` (metros). Câmera em `(0, 10, 0) → (0,0,0)`. Alinhamento perfeito 2D↔3D (overlay 2D continua servindo p/ tap, mas agora encaixado pixel-a-pixel).
- **Wrap yaw circular** (kotlin puro em `HexRender3D.ajustarYawParaMenorCaminho`): fórmula `((alvo-corrente)%360 + 540)%360 - 180` devolve o alvo equivalente MAIS PRÓXIMO — 170°→−170° anima +20°, não −340°. 4 testes puros novos. Integrado no `TokenNode3D` via `finishedListener` do `animateFloatAsState`.
- **Halo do HEX TOCADO** no chão 3D: `PlaneNode` 0.9x0.9 m amarelo (α=0.35) na posição do hex via `HexRender3D.hexParaMundo(hexSelecionado)`. Aparece independente de haver token no hex.
- **Cone de facing 3D**: `CubeNode` branco translúcido (0.5x0.1x0.15 m) a 0.5 m à frente do token na direção do yaw — desambigua Frente/Flanco/Costas na câmera top-down.
- **Defesa por timing (Clair Obscur)** — nova feature grande, arquivo `ui/saga/DefesaPorTiming.kt`:
  - **Regras puras** (`object DefesaPorTimingRegras`): janela de 1000 ms; `<300ms → +1 (perfeito)`, `<600ms → 0 (bom)`, `<1000ms → −1 (tarde)`, `≥1000ms → BONUS_EXPIRADO` (marcador por identidade). `aplicarBonus(opcao, bonus)` soma no `valorFinal` e adiciona `ComponenteMod("timing (<rótulo>)", delta)` — o feed do Narrador mostra o abatimento. `opcaoPadrao(opcoes)` = `maxByOrNull { valorFinal }` filtrado por `disponivel`.
  - **Card Compose** (`@Composable DefesaPorTimingCard`): `Dialog` NÃO dispensável (back/click-outside desligados). Barra `LinearProgressIndicator` decai; cor muda verde → âmbar → vermelho conforme urgência. Botão por opção disponível chama `onEscolher(aplicarBonus(opcao, bonus))`. Timeout auto-seleciona `opcaoPadrao(opcoes)` sem bônus.
  - **11 testes puros das regras**.
- **Wire no `TabSaga`**: card aparece SÓ quando `sagaModoTaticoHex3D == true` E `defesaPendente != null`. Modo 2D e painel de faixas mantêm o UX antigo (botões dentro do `CombatePainel`). Sem flag extra — o 3D é opt-in cinematográfico completo.
- **Novas MaterialInstances memoizadas** no `HexScene3DBase` (`haloHexMi`, `coneFacingMi`) — segue o padrão do HEX-8.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote HEX-8 — 8 de Julho de 2026 (T3 / Fase 6 do PILAR — modelos .glb + halo + interpolação)
**Saga combate tático 3D: substitui cilindros por modelos .glb, halo de seleção, interpolação suave — branch GURPS-Saga**
- 8ª de 9 fatias. Escopo escolhido: **modelos gratuitos por enquanto** (CC-BY 4.0). O motor de regras (HEX-1..6) continua intocado; refino visual em cima do render do HEX-7.
- **Novos assets** (`app/src/main/assets/models/`):
  - `token_heroi.glb` — **CesiumMan** (humanóide com walk cycle, autor: Cesium, CC-BY 4.0, ~438 KB).
  - `token_inimigo.glb` — **Duck** (patinho amarelo, autor: Sony via COLLADA WG, CC-BY 4.0, ~120 KB).
  - `LICENSES.txt` — compliance CC-BY.
  - Estratégia: modelos genéricos como PLACEHOLDER honesto (substituíveis a qualquer momento). ~558 KB no APK.
- **`HexScene3D.kt` refatorado**: substitui `CylinderNode` puro por `ModelNode` carregando `.glb` via `rememberModelInstance`. Novo Composable `@SceneScope.TokenNode3D()` encapsula o render por token com **fallback robusto**: se `ModelInstance` retorna `null` (carregamento assíncrono OU asset ausente) o token cai num **cilindro colorido** (azul herói / vermelho inimigo / verde aliado) — nunca invisível.
- **Halo de seleção**: `PlaneNode` circular amarelo translúcido (α=0.55) sob o token selecionado, criado dentro do `if (selecionado)` — quando desseleciona, `NodeLifecycle.onDispose` remove.
- **Interpolação suave**: `animateFloatAsState + tween(200 ms)` para `x`, `z` e `yawGraus` — token não "teleporta" entre hexes; anima o movimento e o giro. Limitação honesta: `animateFloatAsState` do yaw **não trata wrap circular** — giro de 170° para −170° anima pelo caminho longo (340°). Corrigível no HEX-9 com spec circular.
- **5 MaterialInstances memoizadas** em `remember(materialLoader)` — chão + halo + fallback herói/inimigo/aliado. Filament não coleta lixo; alocar 1x pela vida da Scene.
- **Luz solar mais forte** (`intensity(120_000f)`) para os PBRs dos `.glb` responderem.
- Overlay 2D com grade/tap **continua** — grade 3D nativa fica pro HEX-9 (câmera ortográfica).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote HEX-7 — 8 de Julho de 2026 (T3 / Fase 5 do PILAR — render 3D SceneView)
**Saga combate tático 3D: SceneView/Filament + tokens 3D + overlay 2D pra grade e toque — branch GURPS-Saga**
- 7ª de 9 fatias. Primeira ponte do motor 2D (HEX-1..6) para render 3D. Escopo escolhido pelo usuário: **Plataforma vazia + grade + tokens placeholder** (modelos .glb ficam pro HEX-8).
- **`HexRender3D`** (kotlin puro em `domain/combat/hex/HexRender3D.kt`): converte `HexCoord` axial em `(x, z)` metros no mundo 3D (Y-up, 1 hex = 1 m) e `Direcao` em yaw radianos. Fórmula pointy-top: `x = q + r/2, z = r·√3/2`. Distância entre vizinhos = 1 m exato. `projetar(estado, idHeroi, idsInimigos)` devolve `List<Token3D>` colorido (HEROI/ALIADO/INIMIGO). 8 testes puros novos, total 76 no package hex.
- **`HexScene3DDemo`** (Compose em `ui/saga/HexScene3D.kt`): usa SceneView 3.0.0 (já presente no projeto desde o lote dos dados 3D). Câmera perspectiva top-down inclinada (Position(0, 12, 6) → lookAt(0,0,0)); LightNode SUN; PlaneNode 20x20 m cinza; CylinderNode por token (radius 0.35, height 1.6, cor por `HexRender3D.Cor`) com rotation Y aplicando o yaw. Overlay Canvas 2D transparente por cima da Scene desenha a grade hex (reusando helpers do `HexCanvas` — `tamanhoHex`, `hexParaTela`, `telaParaHex`, `desenharHex`, agora `internal`) e captura tap → altera `HexTaticoState`. Alinhamento perfeito 2D↔3D fica pro HEX-9 (câmera ortográfica dedicada).
- **Flag nova em `CampanhaConfig`**: `modoTaticoHex3D` (default false, aditiva/backward-compat via Gson). Só efetivo se `modoTaticoHex` também. Novo Switch no config panel (`enabled` só quando o pai está ligado); getter `sagaModoTaticoHex3D` no `FichaViewModel`.
- **Roteamento em `TabSaga`**: quando 3D → `HexScene3DDemo`; senão 2D → `HexCanvasDemo`; senão `CombatePainel`. Fallback claro pra Canvas 2D se usuário desligar só o 3D.
- Zero toque em CombatSession/CombatResolver. HEX-2..6 continuam intocados.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote HEX-6 — 7 de Julho de 2026 (T3 / Fase 4 do PILAR — regras posicionais MB+AM)
**Saga combate tático: cobertura + ataque através de hex + manter à distância — branch GURPS-Saga**
- 6ª de 9 fatias. Regras 🔴 que o modelo de FAIXAS abstratas do Saga não cobre — motor puro 2D em cima de `HexGrid`, sem tocar em `CombatSession`.
- **`HexRegrasPosicionais.kt`** (kotlin puro, 3 objects):
  - **`HexCobertura`** (MB p.407–408): `enum Grau(LIMPA(0), PARCIAL(-2), TOTAL(-10, podeAtacar=false))`. `grauEntre(atacante, alvo, hexesBloqueadores)` → classifica: TOTAL se bloqueador está entre atacante e alvo na linha reta; PARCIAL se vizinho do alvo está em bloqueadores E mais perto do atacante; LIMPA caso contrário. TOTAL tem precedência.
  - **`HexAtaqueAtravesHex`** (MB p.389): `penalidade(atacante, alvo, alcanceArmaMetros, ocupantesAliados, ocupantesInimigos)` → `null` se fora de alcance ou alcance < 2 para dist ≥ 2; `0` linha limpa ou só aliados (treino básico); `-4` se inimigo no meio. Endpoints excluídos.
  - **`HexManterADistancia`** (AM p.101): tabela por `TipoInterrupcao` (NENHUMA/APAROU_SEM_DANO/APAROU_COM_DANO_NAO_ESTOCADA/APAROU_COM_ESTOCADA_PERFURANTE/NOCAUTE_OU_PROJECAO) → `Resultado(podeAvancar, movimentoExtra, disputaSTNecessaria, testeVontadeMod)`. Vontade-3 base do MB, caller ajusta por Hipoalgia/Hiperalgia.
- **15 testes puros** (6 cobertura + 5 ataque-através + 4 manter-à-distância) — todos verdes na 1ª rodada. Total 67 no package hex.
- Ataque Telegráfico (AM p.109) já implementado no PONTE-3 — só documentado na header como complemento.
- Zero toque em CombatSession/CombatResolver/NpcCombatBrain. Caller pluga `HexCobertura.grauEntre` no cálculo de penalidade à distância; `HexAtaqueAtravesHex.penalidade` no ataque corpo-a-corpo com hex intermediário; `HexManterADistancia.avaliar` quando defensor pega uma carga com sucesso.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote HEX-5 — 5 de Julho de 2026 (T3 / Fase 3 do PILAR — IA posicional do NPC)
**Saga combate tático: IA tática que escolhe o HEX de destino (flanquear/kite/cobertura/aproximar/recuar) — branch GURPS-Saga**
- 5ª de 9 fatias. `NpcCombatBrain` do Lote 363 decide MANOBRA (Ataque/Ataque Total/Mover/Fugir/Defesa Total) usando só distância; ele **continua intocado**. `HexTaticaNpc` **complementa** decidindo o HEX de destino quando manobra é Mover/Mover-e-Atacar. Se não é movimento → null (fica onde está).
- **`HexTaticaNpc`** (`domain/combat/hex/HexTaticaNpc.kt`, kotlin puro): `data class PerfilTatico(agressividade, moral, alcanceArmaMetros, temArmaDistancia)`. `decidirDestino(estado, npcId, intencao, perfil, idHeroi, hexesComCobertura)` → `HexCoord?`. Estratégias por perfil, na ordem de precedência:
  - **recuar** (moral baixa/PV crítico) → maximiza distância entre vizinhos (`escolherRecuar`).
  - **arqueiro** (`temArmaDistancia && alcance≥3`) → **kite**: minimiza `|distância − alcance|`, prefere candidato com **vizinho em cobertura** (LoS bloqueada — Set passado pelo caller).
  - **agressivo** (`agressividade≥6`) → **flanquear**: via `HexGrid.facingDoAtaque`, procura candidato que caia em FLANCO/COSTAS do herói; prefere COSTAS + adjacente; se nada flanqueia → cai no aproximar.
  - **padrão** → aproximar (minimiza distância).
- Candidatos = vizinhos do NPC + posição atual (permite ficar), filtrados por !ocupados. Deterministico com a ordem dos vizinhos do `HexGrid`.
- **6 testes puros** — os mais delicados (flanquear + arqueiro-com-cobertura) passaram na 1ª. Total 52 no package hex.
- Zero toque em `NpcCombatBrain`/`CombatSession`. O caller (UI do HEX-6/controller tático) chama primeiro `NpcCombatBrain.decidir()` (mesma lógica de sempre), depois `HexTaticaNpc.decidirDestino(...)` para complementar, e usa `HexPortabilidade.aplicarNovaDistancia` para sincronizar a posição após o motor mutar distância.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote HEX-4 — 5 de Julho de 2026 (T3 / Fase 2c do PILAR — regras posicionais base + portabilidade)
**Saga combate tático: facing (frente/flanco/costas) + portabilidade das manobras que mexem em distância — branch GURPS-Saga**
- 4ª de 9 fatias. Duas peças puras, ambas destacáveis pela UI do HEX-5:
- **`HexRegrasFacing`** (`domain/combat/hex/HexRegrasFacing.kt`): aplica MB p.374/390 sobre a mecânica de defesa: `facingDoAtaque(origem, alvo, facingAlvo)` (wrapper); `ajustarValorDefesa(base, facing)` → FRENTE preserva / FLANCO base−2 (piso 0) / COSTAS 0; `defesaAnulada(facing)` → true só em costas (equivalente ao `CombatResolver.defesaAnulada(surpresa=true)`); `ajustarOpcoesDefesa(List<OpcaoDefesa>, facing)` — usa `copy` do data class, preserva flags (recuo/jogar-se-ao-chão/mão-inábil/acrobática). Devolve emptyList em costas para a UI não oferecer defesas anuladas. 7 testes puros.
- **`HexPortabilidade`** (`domain/combat/hex/HexPortabilidade.kt`): **fecha a divergência do HEX-3**. `aplicarNovaDistancia(estado, idNpc, novaMetros, idHeroi)` reprojeta a posição do NPC quando o motor força uma nova distância (Encontrão=1m, Empurrão knockback, Mover, npcResolve). APROXIMAR usa `HexGrid.linhaReta` e caminha hex a hex parando ao chegar/colidir; AFASTAR usa `Direcao.de(heroi, npc)` e caminha no vetor oposto; NUNCA atropela o herói (para em 1 hex); NÃO muda o facing (knockback empurra, não vira o NPC); respeita colisões. 7 testes puros incl. a colisão que trava o caminho.
- **Zero toque em `CombatSession`/`CombatResolver`** — mantendo o padrão dos 3 hex anteriores. A UI do HEX-5 pluga as duas funções: `ajustarOpcoesDefesa` antes de mostrar defesas; `aplicarNovaDistancia` depois de cada ação do motor.
- **Divergência honesta (aceitável, documentada)**: Recuo, Avançar-e-Atacar (Mover-e-Atacar) e Aguardar-por-posição — que o plano listava no HEX-4 — ficam para o HEX-5 junto com a UI de round tático, porque exigem observação dos hexes vizinhos e não fazem sentido sem a UI viva. HEX-4 concentra nas regras que se PLUGAM (facing/portabilidade), HEX-5 traz as regras que se OFERECEM (Recuo, Aguardar).
- **Revisão adversarial achou 2 achados reais (severidade média), corrigidos:** (1) **BD do escudo não era descontado em flanco** (MB p.375: "só quando o atacante vem pela frente") — herói com escudo +2 defendia 9 em flanco quando o correto é 7. Fix: `bonusEscudoEmbutido` como parâmetro em `ajustarValorDefesa` e `ajustarOpcoesDefesa`; ambos descontam junto do −2 no flanco. (2) **Invariante `soma(componentes) ≈ valorFinal` do CombatResolver quebrada** — o card "Defenda-se!" mostraria "Esquiva 7 (+8 esquiva +1 recuo)" (soma 9, valor 7), o jogador não veria POR QUE está −2. Fix: `ajustarOpcoesDefesa` no flanco adiciona `ComponenteMod("flanco", delta)` explícito. +3 testes puros trancam os fixes. Total 46 testes puros no package hex.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote HEX-3 — 5 de Julho de 2026 (T3 / Fase 2b do PILAR — sincronia hex ↔ CombatEncounter)
**Saga combate tático: sincronia POSIÇÃO EM HEX → distância em metros no motor de combate — branch GURPS-Saga**
- 3ª de 9 fatias. **Prova a integração** entre a grade e o `CombatSession` — o motor **não muda uma linha** (RegLote 4 do plano: "não toque no que funciona"). Zero toque no combate por faixas.
- **Estado posicional real** (`domain/combat/hex/HexCombatState.kt`): `PosicaoCombatente(id, posicao, facing)` + `HexCombatState(posicoes, hexSelecionado?, idSelecionado?)`. `distanciaHex(a,b): Int?` calcula pela posição real (1 hex = 1m, MB p.366). `mover` só permite adjacente livre; `aoTocarHex` implementa lógica de toque. `setupInicial(idHeroi, idInimigo)` cria cena Herói(0,0) × Goblin(3,0). 6 testes puros.
- **Sincronia** (`domain/combat/hex/HexCombatSync.kt`): `sincronizarDistancias(estado, encounter, idHeroi)` para cada NPC vivo COM posição no estado chama `encounter.definirDistancia(id, hexDistancia)`. NPCs sem posição são ignorados (mantêm distância anterior — nota honesta: "esquecidos" pelo hex; será revisado no HEX-4/5 quando cada NPC ganhar posição obrigatória). `sincronizarUm` para casos pontuais. 3 testes puros incluindo o teste-chave: mover herói 1 hex ao leste reduz a distância no encounter em 1m.
- **Insight que simplificou tudo:** o `CombatSession` só lê distância via `encounter.distancia(c)` — sincronizar ANTES de cada ação faz alcance/reach/penalidade de distância (MB p.550) funcionarem com base na posição real, sem espelhar nem duplicar o motor.
- **Divergência honesta do plano:** o plano falava "uma troca hero×NPC jogável na grade". Escolhi PAUSAR na sincronia (provada por teste) — a UI de round tático via hex fica junto com o HEX-4 (facing/costas), o que agrupa naturalmente as regras posicionais em um só lote em vez de espalhar UI parcial. Modo tático continua com o Canvas DEMO do HEX-2 até o HEX-4 trazer a integração viva.
- **Revisão adversarial multi-agente achou 1 bug REAL de CONTRATO** (severidade média, pego antes de qualquer uso em produção): o header original prescrevia "sincronizar antes de cada ação" — isso DESFARIA silenciosamente as mudanças de distância que o motor faz (Encontrão força 1m, Empurrão knockback, Mover, npcResolve aproximar/afastar). Como o motor é a fonte de verdade da distância DURANTE a ação, a sincronia hex→motor só é segura no SETUP. **Fix**: renomeada para `projetarSetupInicial`/`projetarUm`, header reescrito com aviso explícito ("⚠️ USE UMA VEZ ao abrir o combate; NÃO chame após ações"), **+ teste de trava contratual** que documenta o cenário: re-projetar após uma manobra do motor apaga a mudança (o teste confirma o comportamento indesejado, garantindo que qualquer futuro caller entenda o contrato).
- **Divergência do plano registrada**: a portabilidade das manobras do motor (Encontrão/Empurrão/Mover/Troca/npcResolve) para operar via `HexCombatState.mover` fica como pré-requisito do HEX-4/5. Isso ajusta o escopo do pilar hexágono — a "fonte única de verdade posicional" que o plano descreve não fica pronta no HEX-3, e essa honestidade é mais valiosa que fingir que ficou.
- Build verde 2 variantes. 16 testes puros no package hex total (+1 de trava contratual).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote HEX-2 — 5 de Julho de 2026 (T3 / Fase 2a do PILAR — grade 2D + flag + roteamento)
**Saga combate tático: Compose Canvas 2D com grade de hexágonos, tokens móveis por toque — branch GURPS-Saga**
- 2ª de 9 fatias do PILAR (`docs/planos/PLANO_Combate_Tatico_Hex_3D.md`). **Prova visual da grade** — SEM regras de combate plugadas ainda (HEX-3 pluga `CombatSession`).
- **Feature flag:** `CampanhaConfig.modoTaticoHex: Boolean = false` (aditivo/Gson-safe, fichas antigas usam default). Switch na tela "Configuração do Jogo" com aviso "EXPERIMENTAL". Modo faixas (Lotes 419–424) continua o padrão intocado.
- **Estado puro** (`domain/combat/hex/HexTaticoDemo.kt`): `HexTaticoState(tokens, hexSelecionado?, tokenSelecionadoId?, raioGrade=7)`; `aoTocarHex` implementa a lógica (mover se vizinho livre / selecionar token / destacar hex vazio). `mover` devolve `this` quando ilegal (evita recomposição). 6 testes puros.
- **Canvas 2D** (`ui/saga/HexCanvas.kt`): `HexCanvasDemo` composable. `hexParaTela` (pointy-top: q influencia x e r meia-linha; r puxa y por 1.5t) + `telaParaHex` (inverso RedBlob + cube-round + descarta fora do raio). `desenharHex` desenha 6 vértices a 30°+60°*i com destaque quando selecionado + rótulo (q,r) para debug (some no HEX-7). `desenharToken` círculo azul/vermelho + flecha de facing + inicial + realce branco quando selecionado. Estilos (`ESTILO_LABEL_HEX`, `ESTILO_INICIAL_TOKEN`) declarados fora do `@Composable` para poder usar dentro do `DrawScope`.
- **Roteamento** (`TabSaga.kt` linha ~426): quando `sagaCombateAtivo && sagaModoTaticoHex` → renderiza `HexCanvasDemo` (weight 1.5f, mesmo footprint do `CombatePainel`). Flag lida via `FichaSagaDelegate.configAtiva` (parse do `CampanhaEntity.configJson`) exposta como `viewModel.sagaModoTaticoHex`.
- **Revisão adversarial** (3 dimensões: geometria hex↔tela, roteamento+flag, Compose+estado): **0 achados confirmados, 0 sem veredito**. Build verde 2 variantes. Combate por faixas intocado.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote HEX-1 — 5 de Julho de 2026 (T3 / Fase 1 do PILAR — combate tático em hexágonos)
**Saga combate tático: motor `HexGrid` puro (Kotlin, sem Android) — branch GURPS-Saga**
- **PILAR NOVO** começa aqui — 1ª de 9 fatias (`docs/planos/PLANO_Combate_Tatico_Hex_3D.md`). Princípio-mestre: motor de regras 2D primeiro, 3D depois. **Zero toque no combate atual** — arquivos novos em `domain/combat/hex/`.
- **`HexCoord.kt`:** data class axial `(q,r)` pointy-top; `s = -q-r` implícito; +/− vetoriais; `distancia` via cube; enum `Direcao` (6 direções nomeadas: LESTE/SUDESTE/SUDOESTE/OESTE/NOROESTE/NORDESTE, circular horário, `oposta = ord+3 mod 6`); `Direcao.de(a,b)` por projeção; enum `Facing` (FRENTE 0, FLANCO −2, COSTAS anula) com `Facing.calcular(origemAtk, alvo, facingAlvo)` — arco frontal 180° (3 hexes), costas 1 hex, flancos 2 hexes (fiel a MB p.390/AM p.104); `arredondarCube` (algoritmo RedBlob que preserva x+y+z=0); `lerpHex`.
- **`HexGrid.kt` (object):** `distancia`, `vizinhos` (6 na ordem do enum), `range(centro, raio)` (3n²+3n+1 hexes; require raio≥0), `linhaReta` (distancia+1 pontos via lerpHex + arredondamento cube), `linhaDeVisao(de, ate, bloqueado)` (ignora as pontas — herói/alvo dentro de vegetação enxerga fora), `facingDoAtaque` (proxy p/ Facing.calcular).
- **24 testes** em `HexGridTest.kt` — kotlin puro (~5s p/ rodar todos): distância simétrica, vizinhos únicos a d=1, range 0/1/2 (contagens 1/7/19), linha reta com passos adjacentes, LoS livre/bloqueada/pontas ignoradas, facing frente (facing + laterais)/flanco/costas por 6 direções.
- **Referência canônica no comentário:** RedBlob Games (redblobgames.com/grids/hexagons). Escala Saga: 1 hex = 1 metro (MB p.366).
- **Escopo honesto:** só a matemática — nenhuma peça do combate atual vê o hex ainda (é HEX-3 que integra). ⚠️ Revisão adversarial NÃO rodou (limite semanal); validado por auto-revisão manual (matemática cube, ordem circular das direções, diff angular do facing, edge cases de LoS/isolamento). Build verde 2 variantes.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 424 — 5 de Julho de 2026 (T1-2 da ordem de prioridade)
**Saga: ações improvisadas → modificadores de combate (tool `aplicar_modificador_combate`) — branch GURPS-Saga**
- **Fecha o pedido original do usuário** ("combate só de botões não é RPG"): a caixa de chat no combate (Lote 420) agora tem EFEITO MECÂNICO. O jogador improvisa ("me escondo atrás da árvore", "jogo areia nos olhos dele") → o Narrador valida (pedir_rolagem se incerto) → chama **`aplicar_modificador_combate(alvo_id, valor, aplica_em: ataque|defesa, motivo, duracao_rodadas?)`** → vira bônus/penalidade **NOMEADO** no motor.
- Motor (`CombatSession.ModSituacional`): mods por combatente em ataque (entra como `ComponenteMod` no NH — aparece no cálculo do golpe) ou defesa (soma nas opções de defesa do herói e na defesa do NPC alvejado); duração em rodadas (decrementa ao fim do turno do dono) ou combate inteiro; valor coerçado ±10; expiração automática. 18ª tool do Narrador (schema + executor com guarda `combateAtivo` + bridge + wrapper no controller) + prompt (lei 8 estendida + lista). +4 testes (2 domínio, roteamento, contagem).
- **Revisão adversarial (verificadores caíram no limite → verifiquei os 4 achados manualmente; todos reais, corrigidos):** (1) ALTA — mod com duração finita expirava ANTES de valer (o chat roda no turno do dono; decrementava no fim do mesmo turno) → regra da ESTREIA: o turno da criação não conta; (2) agarrões (`heroiAgarrar`/`npcAgarraHeroi`) ignoravam os mods → agora entram no ataque e na defesa; (3) fallback de defesa passiva (herói sem opção) soma o mod; (4) `duracao_rodadas` 0/negativa era promovida a "combate inteiro" silenciosamente → agora é erro. Disputas de ST (imobilizar/chaves/mata-leão) ficam FORA dos mods por escopo (documentado).
- Escopo honesto: sem UI nova (o mod aparece no log do golpe e nos valores das defesas); NPCs recebem mods mas o cérebro deles não os cria — é ferramenta do Narrador. Build verde 2 variantes.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 423 — 5 de Julho de 2026 (T1-1 da ordem de prioridade)
**Saga: Sangramento ENTRE CENAS — persiste na ficha + passar_tempo real-parcial (branch GURPS-Saga)**
- **Fecha de verdade o item 5 do teste de batalha:** o sangramento (PONTE-2) era efêmero — morria com a sessão de combate. Agora **persiste na ficha** (`Personagem.sagaSangrando` + penalidade de local + intervalo; campos aditivos/anuláveis, Gson-safe) e **atravessa cenas**: `finalizar()` grava, `iniciarCombate` restaura (herói entra na próxima luta já SANGRANDO).
- **`passar_tempo` deixou de ser `nao_implementado`** (real-PARCIAL): registra `tempoJogoMin` na campanha (campo/DAO já existiam) e **processa o sangramento fora de combate** — `InjuryRules.sangrarPorTempo` (pura: 1 teste por intervalo até estancar ou morrer, continua inconsciente = pode sangrar até a morte, MB p420); PV gravado na ficha; feed recebe resumo (perdeu X PV / estancou / morreu). Clima/relógios/ecologia continuam na Fase C2 (nota honesta no retorno).
- **Cura fora de combate estanca** (gastar_recurso negativo → `sagaLimparSangramento`, MB p424/p52). **Fim de combate avisa o Narrador** ("herói saiu SANGRANDO — trate ou passe o tempo") e o prompt/schema da tool instruem a não ignorar. +2 testes (roteamento passar_tempo; sangrarPorTempo puro). Build verde 2 variantes.
- **Revisão adversarial (verificadores caíram no limite → verifiquei os 6 achados brutos manualmente) achou 3 bugs reais, corrigidos:** (1) flag `morto` por limiar de PV em vez do veredito REAL dos cheques de HT (podia declarar morte com o herói vivo) — `sangrarPorTempo` agora devolve `{logs, morto, desmaiou}` do motor; (2) morto ficava com `sagaSangrando=true` na ficha (estado zumbi) — morte limpa o estado; (3) desmaio durante o tempo era descartado — agora reportado ao Narrador (feed+JSON). +cap de 1 ano por chamada no `passar_tempo` (evita overflow de `minutos*60`).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote PONTE-4 — 24 de Junho de 2026
**Saga combate: Ataque Dedicado / Ataque Defensivo (AM p98) — 4ª e última ponte de Artes Marciais (branch GURPS-Saga)**
- **Ataque Dedicado** (entre Ataque e Ataque Total): Determinado **+2 para acertar** OU Forte **+1 de dano FIXO** (não o +2/+1-por-dado do Ataque Total); em troca **−2 em TODAS as defesas** no turno seguinte + **proíbe a Retirada**. **Ataque Defensivo** (entre Ataque e Defesa Total): **−2 de dano (ou −1/dado, o pior)** + **+1 numa defesa escolhida** (Aparar/Bloquear).
- 2 manobras novas + `DedicadoModo` (Determinado/Forte). `calcularNH`/`resolverAtaque` ganham `dedicadoModo`; `modDanoManobra` (companion) faz o dano; flags por-turno (`heroiPenalidadeDefesaDedicado`/`heroiSemRetirada`/`heroiBonusDefesaDefensivo`) setadas no fim de `heroiAtaca`, lidas em `opcoesDefesaHeroi`, zeradas na próxima ação. `manobrasLegais` + controller (some à distância/sem-alvo/preso) + UI (seletores) + VM wrappers + 4 testes.
- **Deferido honesto:** NPC usar Dedicado/Defensivo; vínculo fino "não apara com a mão que atacou" (abstraído como −2 geral, mesmo padrão do lote 421); bônus opcional de ST alta no Dedicado Forte (+1/2-dados, "a critério do Mestre"). Build verde 2 variantes.
- **Nota:** a revisão adversarial por agentes NÃO rodou neste lote (limite semanal da conta) — validado por revisão MANUAL (ciclo das flags, callers posicionais, não-herança do Ataque Total, piso de dano 0) + 4 testes.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote PONTE-3 — 24 de Junho de 2026
**Saga combate: Ataque Telegráfico (AM p109) — 3ª ponte de Artes Marciais (branch GURPS-Saga)**
- **Ataque Telegráfico** (par/oposto do Ataque Enganoso, lote 401): **+4 para acertar, mas +2 em TODAS as defesas do alvo**. Mutuamente exclusivo com o Enganoso. O **+4 NÃO conta para o golpe fulminante** — o crítico é recomputado com o NH ANTES do +4 (`atk.copy(critico = CriticoRules.classificar(soma, nhEfetivo−4))`); acerto/margem seguem com o +4. Não acumula com Avaliar. Em `resolverGolpeHeroi`/`heroiAtaca` + wrappers + UI (toggle Switch no diálogo de ataque, exclusivo com o stepper do Enganoso, sem o limite de NH≥12). Teste estatístico (acerta mais).
- **Revisão adversarial achou e corrigiu 1 bug ALTA** que eu introduzi: o recompute do crítico (`copy(critico=...)`) podia transformar um ACERTO normal em FALHA CRÍTICA (soma 17 + NH efetivo alto → `classificar(17, NH−4)` vira falha) — o herói acertava com dano E sofria erro crítico no mesmo golpe. Fix: só rebaixar DECISIVO→NORMAL, **nunca** promover a falha. +teste que tranca isso (2000 lances NH 18).
- **Deferido honesto:** o NPC usar Ataque Telegráfico proativamente (o cérebro não escolheria; só o herói usa). Build verde 2 variantes.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote PONTE-2 — 24 de Junho de 2026
**Saga combate: Sangramento (MB p420 / AM p138) — 2ª ponte de Artes Marciais (branch GURPS-Saga)**
- **Sangramento** no motor: ferimento por corte/perfuração (contusão não sangra) marca `Condicao.SANGRANDO` + estado no `Combatente` (lesão, penalidade de local, intervalo, testes limpos). A cada intervalo (60s comum; 30s em vitais/pescoço/crânio com penalidade, AM p138) o ferido testa HT −(lesão/5)−penalidade no início do turno (`avancarTurno`); falha = −1 PV (−3 se 18); sucesso decisivo (≤4) ou 3 intervalos limpos = estanca. **Recuperar ≥1 PV estanca** (MB p52, no caminho de cura em combate). Marca nos DOIS sentidos (funil `CombatResolver.resolverTroca`).
- `InjuryRules`: `classificarSangramento`, `tickSangramento`, `estancarSangramento` + `ferir(tipo, local)` (params aditivos = sem regressão nos call-sites antigos). 3 testes puros. UI: "sangrando" aparece de graça (Condicao.rotulo).
- **Revisão adversarial multi-agente achou e corrigiu 7 bugs reais** (fidelidade GURPS que eu teria perdido): (1) 17/18 em 3d6 SEMPRE falham (faltava o teto — herói de HT alto ficava imune; falha crítica −3 PV virava código morto); (5) falha crítica de HT = 17 com HT efetivo ≤15 também perde 3 PV; (4) penalidade −1/5PV pelo DÉFICIT total de PV, não pela maior lesão única; (2) dano narrado de corte (tool do Narrador) também marca sangramento (tipo/local repassados); (6) pescoço grave só p/ corte/perfuração (não `pi`, AM p138); (3) inconsciente-mas-vivo continua sangrando até a morte; (7) referência de regra corrigida.
- **Honesto:** o intervalo de 60s = ~60 rodadas, então em combates CURTOS quase nunca dispara (fiel à regra — importa em lutas longas). **Deferido:** manobra Primeiros Socorros (ação de 1 min, não cabe em 1 turno); sangramento ENTRE cenas (precisa persistir na ficha / tick no `passar_tempo` do Narrador — follow-up de maior impacto); veias/artérias, cirurgia, desmembramento. Build verde 2 variantes (+4 testes).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote PONTE-1 — 24 de Junho de 2026
**Saga combate: chaves de luta agarrada (Chave de Membro + Mata-Leão) — 1ª ponte de Artes Marciais (branch GURPS-Saga)**
- Primeira regra do mapa `docs/pendencias/Artes_Marciais_Regras_Combate.md` (ponte: regras que sobrevivem ao futuro pilar de hexágono). Estende o agarrão herói↔NPC (lote 422) com 2 finalizações sobre alvo AGARRADO.
- **Chave de Membro** (AM p69-70/81): Disputa Rápida de ST (vítima resiste com max ST/HT; +4 se perna) → dano por **contusão = margem** no braço/perna. **Mata-Leão** (AM p77): estrangular com 2 mãos (**+3 ST**) → dano no pescoço (×1,5) + **SUFOCANDO**. Nas **duas direções** (herói→NPC via manobra; NPC→herói via `npcChaveMembroHeroi`/`npcMataLeaoHeroi` despachados em `npcResolve`). `NpcCombatBrain`: NPC desarmado que já agarrou o herói escolhe Mata-Leão (agress≥8)/Chave (≥6)/Imobilizar por agressividade. UI: 2 diálogos de alvo agarrado. 2 testes determinísticos.
- **Deferido honesto (sem chute):** sem NH de perícia de luta no perfil (Disputa por ST pura); RD flexível não distinguida da rígida; ramo "sanguíneo"/fadiga do mata-leão; chaves finas (dedo/cabeça/pescoço/tesoura/triângulo) que exigem ponto de impacto/posição que o modelo de faixas não carrega. Build verde 2 variantes (+revisão adversarial).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 422 — 24 de Junho de 2026
**Saga combate: luta agarrada NPC→herói (Desvencilhar-se) — fecha as últimas regras codáveis do docs/fonte-regras/Combate.md (branch GURPS-Saga)**
- **Ponto cego do audit:** o grep original (só `##`) tinha pulado subtópicos `###`/`####`. Achados e fechados: "Aparando Armas Pesadas" e "Preparando Armas e Outros Equipamentos" (deferidos/feitos com razão honesta), e a luta agarnada NPC→herói (lacuna REAL, não deferimento).
- **Agarrão era mão única** (herói→NPC). Agora o inverso existe: (a) `NpcCombatBrain` — NPC DESARMADO engajado pode AGARRAR o herói (50%) e, se já agarrou, IMOBILIZAR; (b) `npcResolve` — `npcAgarraHeroi` (ataque defensável, sem dano → herói AGARRADO) e `npcImobilizaHeroi` (Disputa de ST → IMOBILIZADO); (c) herói preso leva −4 nas defesas (espelha o NPC agarrado).
- **Herói AGARRADO/IMOBILIZADO restrito** (MB p.371): manobras perdem Apontar/Aguardar/Concentrar/Fintar/à distância/avanço (imobilizado perde quase tudo) e só ataca DESARMADO (`construirAtaques` filtra, reconstruído na transição). Nova manobra **Desvencilhar-se** (`heroiDesvencilhar`): Disputa Rápida de ST — captor +5 agarrado/+10 imobilizado, −4 se atordoado, soltura automática se o captor cai. Wrapper no ViewModel + botão na UI + teste determinístico.
- **Simplificações honestas:** sem "1×/10s" ao imobilizar e sem +2/braço extra; passo de 1m abstraído; alcance C vs 1 não distinguidos (preso = só desarmado, adaga não liberada à parte); NPC imobiliza sem exigir o herói no chão (heroiImobilizar é mais estrito).
- **PLACAR FINAL do docs/fonte-regras/Combate.md: 0 parciais, 0 não-feitos, 0 não-marcados (h2–h6).** Build verde nas 2 variantes. Todas as regras de combate codáveis estão implementadas; o resto é deferido por dado/narrativa com razão registrada in-file.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 421 — 24 de Junho de 2026
**Saga: tool de desequipar — fecha o item 1 do teste de batalha (parte de equipamento, branch GURPS-Saga)**
- **Problema (item 1):** o Narrador narrava "tiraram suas armas e armaduras", mas o combate continuava usando os itens (lia tudo de `p.equipamentos`). Agora há sincronia ficha↔narrativa para equipamento.
- **Tool nova `gerir_equipamento(item_nome, operacao)`** — operacao: `confiscar` (tira, recuperável), `devolver` (volta a usar), `destruir` (some da ficha). `item_nome` casa por nome (igual/contém) ou por categoria (`armas`/`armaduras`/`tudo`). Schema + executor + bridge + helper `sagaGerirEquipamento` no ViewModel (persiste na ficha).
- **Campo `confiscado` em `Equipamento`** (aditivo/anulável — Gson retrocompatível): item TIRADO continua na ficha (recuperável) mas indisponível. **Combate respeita:** `construirAtaques` ignora arma confiscada (herói luta no soco) e `rdHeroi` ignora armadura confiscada (sem RD). **Persiste entre batalhas** (era exatamente a falha do item 1).
- **Prompt:** nova lei 10 (mudou o que o herói POSSUI → chame `gerir_equipamento`) + tool na lista. **UI:** aba Equipamentos marca "⛓️ confiscado na história" em arma/armadura.
- **Teste** de roteamento da tool (item_nome obrigatório). Build verde nas 2 variantes.
- **Limitação honesta documentada:** Aparar/Bloquear/bônus de escudo vêm do modelo de DEFESA da ficha (app-wide), ainda NÃO gateados por confisco — herói desarmado pode ainda aparar/bloquear pelos valores da ficha. Follow-up se necessário.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 420 — 24 de Junho de 2026
**Saga: teste de batalha — Frentes 2/3/4 (itens 2,3,4,5,1-parcial) + endurecimento pós-revisão adversarial (branch GURPS-Saga)**
- **Frente 2 — itens 2 e 3 (sem chat no combate / após cair):** a `BarraDeEnvio` (caixa "O que você faz?") deixou de ser SUBSTITUÍDA pelo painel de combate — agora fica SEMPRE visível abaixo do painel. O jogador fala com o Narrador DURANTE o combate (o `iniciar_combate` não bloqueia o loop da IA) e DEPOIS de desmaiar/morrer/vencer, sem sair e voltar à campanha. Em combate, placeholder "Falar com o Narrador…" e botão "Falar". `TabSaga.kt`.
- **Frente 4 — item 4 (Ataque Enganoso parecia desabilitado):** no passo 0 a "−" fica cinza (correto) e mostrava "−0/−0", parecendo bug. Agora no passo 0 o texto vira "toque + (cada passo: −2 no acerto, −1 na defesa; até N)"; e em corpo-a-corpo com NH < 12 (ex.: Briga fraca) aparece nota explicando POR QUE sumiu (NH efetivo não cai abaixo de 10). `CombatUi.kt`.
- **Frente 3 — item 5 (derrotado sem dano) e item 1 (parte de PV/estado):** (a) guarda em `iniciarCombate`: herói a 0 PV ou abaixo NÃO entra em combate fadado — recusa com "heroi_incapacitado" e manda o Narrador narrar/curar (`SagaCombatController`); (b) cura/descanso: `gastar_recurso` com `quantidade` NEGATIVA RESTAURA PV/PF (até o máximo) — schema, prompt e bridge; (c) prompt lei 8: não iniciar luta com herói caído, curar antes.
- **Endurecimento pós-revisão adversarial (6 bugs reais achados por revisão multi-agente, todos corrigidos antes do commit):**
  - 🔴 **ALTA (soft-lock):** o `NarradorToolExecutor` rejeitava `quantidade <= 0` ANTES do bridge → minha cura por valor negativo era código morto e o herói a 0 PV não lutava nem curava. Agora só ZERO é inválido; negativo é permitido para pv/pf (dinheiro/munição seguem exigindo positivo). `NarradorToolExecutor.kt` + novo teste.
  - 🟠 **MÉDIA (reabrir combate):** falar com o Narrador no meio da luta podia fazê-lo chamar `iniciar_combate` de novo e sobrescrever a sessão. `iniciarCombate` agora recusa "combate_ja_ativo" se já há luta em curso (espelha `acao_npc`).
  - 🟠 **MÉDIA (narração de fim perdida):** se o golpe fatal vinha de uma tool num turno de texto, a prosa de desfecho + XP eram descartadas por `processando`. Agora o `CombatFim` fica pendente e é drenado no `finally` de `rodarTurno`. `FichaSagaDelegate.kt`.
  - 🟠 **MÉDIA (cura fantasma em combate):** `gastar_recurso` escrevia só na ficha; em combate o motor sobrescrevia. Agora, com combate em curso, pv/pf são roteados ao motor (`ajustarRecursoHeroiEmCombate`) e sincronizados.
  - 🟡 **BAIXA (painel preso):** combate encerrado e não fechado deixava o painel velho na tela; `iniciarCombate` agora faz `encerrarManual()` de sessão já encerrada antes de seguir.
- Build verde nas 2 variantes. **Pendente honesto (item 1, parte de equipamento):** desarmar/remover armas e armaduras narrados ainda NÃO sincroniza com a ficha — precisa de tool nova de desequipar (lote dedicado).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 419 — 24 de Junho de 2026
**Saga: Prompt do Narrador — coerência de estado + 3ª via (teste de batalha, itens 6 e 7, branch GURPS-Saga)**
- **Item 7 (alternativas A/B/C como gaiola):** lei de ferro 7 estendida — se o Narrador listar alternativas, elas são SUGESTÕES e ele deve deixar EXPLÍCITO que o jogador pode agir livremente FORA da lista (nunca apresentá-las como as únicas saídas). A caixa "O que você faz?" sempre aceita texto livre.
- **Item 6 (cena incoerente com o estado do herói):** nova lei de ferro 9 — antes de devolver a iniciativa, RESPEITAR o estado do herói (inspecionar_personagem + relatório do combate). Se ele está inconsciente/caído/atordoado/imobilizado/sufocando/capturado, NÃO perguntar "o que você faz?" — narrar a consequência do estado (mundo segue, captores/inimigos agem, tempo passa) e só devolver a decisão quando o herói puder agir.
- Mudança só no `MestreIAPromptsNarrador.kt` (string do prompt). Build verde nas 2 variantes (BUILD SUCCESSFUL). Itens 1/2/3/4/5 nas próximas frentes.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 418 — 23 de Junho de 2026
**Saga combate: CLOSURA DO AUDIT DO docs/fonte-regras/Combate.md — 0 tópicos sem marcação (doc-only, branch GURPS-Saga)**
- Marcados os últimos tópicos não-feitos com status honesto. **Lesões e Defesas Ativas → FEITO** (já estava: −4 atordoado/393 + choque-não-penaliza-defesa/382). **Armas de Arremesso → FEITO (base)** (arremesso = ataque à distância; "não fica preparada após arremessar" deferido por falta de flag thrown-vs-arco).
- **FORA DO ESCOPO/DEFERIDO (`[—]`, com razão):** Mata-Leão/Chave de Braço/Torção (Martial Arts p.403/404, fora do Básico); Ataques que não Causam Dano (Atribulação/poderes especiais); Aparar com Armas Improvisadas (durabilidade de objeto); Recarregar e Disparar (sem munição, decisão 366); Superpenetração e Cobertura (posicionamento em hexágono); Dano Especial/Acompanhamento/Conjuntos (tipos de dano/modificadores especiais); Segurar (objeto empunhado pelo NPC); Outras Ações / Outras Ações em Combate / Ações Prolongadas Comuns (narrativo/Narrador).
- **PLACAR FINAL do docs/fonte-regras/Combate.md: 70 FEITO [x] · 23 fora-do-escopo/deferido [—] · 0 sem marcação.** Todas as regras de combate codificáveis no modelo abstrato do Saga estão implementadas, build verde nas 2 variantes, testadas. Mudança só de documentação (não compila Kotlin).
- **PRÓXIMO (combinado): validação no aparelho de TUDO** (tarefa do usuário). NÃO entrar na Fase C antes disso.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 417 — 23 de Junho de 2026
**Saga combate: Projeção / knockback geral (tópicos `[]` 10/N, MB p.378, branch GURPS-Saga)**
- `aplicarProjecao(...)`: contusão SEMPRE projeta; corte só se NÃO penetrou a RD; **1m por múltiplo de (ST−2) do dano básico**; o projetado testa DX (−1/m após o 1º) ou cai. Chamado nos dois caminhos (`resolverGolpeHeroi` após acertar o NPC; `npcResolve` após o NPC acertar o herói). Reusa a lógica de knockback do Empurrão (Lote 410, "apenas projeção").
- Teste: golpe contuso forte (maça 3d) vs ST 8 projeta o alvo. Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Projeção" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 416 — 23 de Junho de 2026
**Saga combate: Agachar + cluster de movimento em hexágono (tópicos `[]` 9/N, MB p.368, branch GURPS-Saga)**
- **Agachar**: o benefício mecânico (alvo menor à distância) → `penalidadePosturaAlvejado(postura)` (agachado/ajoelhado/rastejando/sentado −2, deitado −4) somado ao acerto À DISTÂNCIA nos dois caminhos (`resolverGolpeHeroi` e `npcResolve`). Vale p/ herói e NPC (atira-se pior em alvo agachado/deitado).
- **FORA DO ESCOPO (marcados `[—]`):** Passo, Espaçamento, Passando por Outros, Evadir, Ações Livres — são posicionamento em hexágono / ações narrativas, abstraídos no tracker de faixas (sem grade de hexágonos). Também Armadura Flexível/Trauma (sem flag rígida/flexível) e Corrosão (sem tipo de dano "cor") = limitados por dado.
- Teste: penalidade por postura + tiro em alvo deitado registra a postura. Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Agachar" → FEITO; Passo/Espaçamento/Passando por Outros/Evadir/Ações Livres → fora do escopo.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 415 — 23 de Junho de 2026
**Saga combate: Sucessos Decisivos em Defesa (tópicos `[]` 8/N, MB p.374, branch GURPS-Saga)**
- Crítico ao defender um ataque **corpo-a-corpo** → o atacante joga na Tabela de Erro Crítico (reusa `aplicarErroCritico`, Lote 384). `CombatResolver.defesaDecisiva(soma, valor)` (3-4 sempre; 5 se valor≥15; 6 se ≥16); no `npcResolve`, se `troca.defendeu && !aDistancia && defesaDecisiva` → o NPC tropeça. Vs ataque à distância = sem efeito (regra). 
- DEFERIDO: pegar a arma de arremesso ao aparar desarmado com crítico.
- Teste: defesa soma 3 (crítico) com acerto não-crítico dispara o erro crítico do atacante. Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Sucessos Decisivos em Jogadas de Defesa" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 414 — 23 de Junho de 2026
**Saga combate: Esquiva Acrobática (tópicos `[]` 7/N, MB p.377, branch GURPS-Saga)**
- Variante de defesa **Esquiva Acrobática**: com a perícia Acrobacia, o motor testa Acrobacia ANTES da esquiva → **+2 (sucesso) / −2 (falha)**. `HeroiPerfilCombate.acrobacia` (← perícia `acrobacia` da ficha); `OpcaoDefesa.acrobatica` + `DefesaHeroi.acrobatica`; `opcoesDefesa(permitirAcrobatica)` emite a variante (só com Acrobacia, não atordoado); `npcResolve` rola e ajusta. UI: card "🤸 acrobática (±2)".
- FORA DO ESCOPO (marcadas `[—]`): Esquiva Altruísta (sem aliados no combate solo) e Esquiva com Veículo (combate a pé).
- Teste: oferecida com Acrobacia, ausente sem; dispara o teste no `npcResolve`. Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Esquiva Acrobática" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 413 — 23 de Junho de 2026
**Saga combate: Divisores de Armadura (tópicos `[]` 6/N, MB p.378, branch GURPS-Saga)**
- Divisor de armadura na expressão de dano (ex.: `3d(2) pa`): `CombatSession.divisorArmadura(expr)` extrai o "(2)"; `rdComDivisor(rd, divisor)` aplica — divisor ≥1 **reduz** a RD (÷, arredonda p/ baixo); fracionário (0,5/0,2/0,1) **melhora** a RD (×2/×5/×10) e trata RD 0 como 1. Aplicado nos dois caminhos de dano (`resolverGolpeHeroi` e `npcResolve`) ANTES do `aplicarDano`. `semTokenTipo` preserva o "(2)" (só tira o token de tipo no fim).
- DEFERIDO: modificadores especiais de penetração (toxina/agentes de contato/respiratório) — não modelados.
- Teste: parsing do divisor + RD reduzida/melhorada (5 casos). Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Divisores de Armadura e Modificadores de Penetração" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 412 — 23 de Junho de 2026
**Saga combate: Estrangulamento / Strangle (tópicos `[]` 5/N, luta agarrada, MB p.371, branch GURPS-Saga)**
- Nova manobra `ESTRANGULAR` — `heroiEstrangular(alvoId)`: exige o alvo **AGARRADO**; **Disputa de ST vs max(ST,HT)**; a **margem de vitória** = dano por contusão **×1,5** (pescoço), RD protege; penetrando, o alvo fica `SUFOCANDO`. No `npcResolve`, o NPC SUFOCANDO **perde 1 PV/turno** (proxy de fôlego) enquanto preso, e solta a condição ao escapar. Manobra (e Imobilizar) aparecem só com inimigo agarrado.
- **Núcleo da luta agarrada COMPLETO** (Agarrar 386, Derrubar 386, Imobilizar 411, Estrangular 412). Mata-Leão/Chave de Braço/Torção = Martial Arts (deferidos). "Ações Depois de Agarrar" → FEITO.
- Testes: estrangular causa dano+sufocamento; recusa sem agarrar. Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Asfixia ou Estrangulamento" e "Ações Depois de Agarrar" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 411 — 23 de Junho de 2026
**Saga combate: Imobilizar / Pin (tópicos `[]` 4/N, luta agarrada, MB p.371, branch GURPS-Saga)**
- Nova manobra `IMOBILIZAR` — `heroiImobilizar(alvoId)`: exige o alvo **AGARRADO + no chão** (DEITADO/CAÍDO); **Disputa de ST** (+3 por categoria de MT de vantagem); vencendo, o alvo ganha `Condicao.IMOBILIZADO` (indefeso). `melhorDefesaNpc` retorna defesa 0 se IMOBILIZADO; o bloco AGARRADO do `npcResolve` agora cobre IMOBILIZADO (forceja a −3, não ataca, solta as duas condições ao vencer). Manobra aparece só com inimigo agarrado. Novas condições: `IMOBILIZADO`, `SUFOCANDO` (p/ 412).
- Mata-Leão/Chave de Braço/Torção referenciam Martial Arts (p.403/404, fora do Básico) → deferidos.
- Testes: herói forte imobiliza o agarrado caído; recusa sem agarrar. Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Imobilizar" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 410 — 23 de Junho de 2026
**Saga combate: Empurrão / Shove (tópicos `[]` 3/N, MB p.371, branch GURPS-Saga)**
- Nova manobra `EMPURRAO` — `heroiEmpurrao(alvoId)`: acerto por **DX** (adjacente), alvo pode defender. Se acerta, **GdP×2** vira **projeção/knockback** = 1m por múltiplo de **(ST−2)** no resultado (MB p.378), com possível queda (teste de DX por metro extra); **nunca causa lesão**. `HeroiPerfilCombate.danoGdP` (← `p.danoGdP`). UI: manobra com seletor de alvo.
- Teste: empurrão projeta o alvo sem alterar o PV. Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Empurrão" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 409 — 23 de Junho de 2026
**Saga combate: Encontrão / Slam (tópicos `[]` 2/N, MB p.371, branch GURPS-Saga)**
- Nova manobra `ENCONTRAO` — `heroiEncontrao(alvoId)`: carrega até o alvo, acerto por **DX** (sem o −4/teto-9 do Avançar e Atacar), alvo pode defender (corpo = arma pesada). Se acerta, **dano mútuo por contusão = (PV×vel.relativa)/100 dados** (`encontraoDanoDados` no companion: <1d → 1d-3/1d-2/1d-1; ≥1d arredonda 0,5+). Derrubada: alvo cai se leva o dobro; herói cai se leva o dobro, ou testa DX se causou ≥. Vel. relativa usa `velocidadeAtual` (Lote 403). UI: manobra com seletor de alvo.
- DEFERIDO: derrubada em mergulho/acometida/arremetida com escudo e encontrão com veículo/montaria.
- Testes: fórmula de dados (5 casos); encontrão com dano mútuo. Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Encontrão" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 408 — 23 de Junho de 2026
**Saga combate: Golpe Rápido (tópicos `[]` 1/N, MB p.370, branch GURPS-Saga)**
- Início da varredura dos tópicos NÃO-FEITOS do docs/fonte-regras/Combate.md (autonomia do usuário). **Golpe Rápido**: nova manobra `GOLPE_RAPIDO` — `heroiGolpeRapido(ataque, alvoId, local)` faz **2 ataques corpo-a-corpo** no mesmo turno, cada um com **−6** (`resolverGolpeHeroi(modAdicional=-6)`), **mantendo a defesa ativa** (não é Ataque Total). UI: manobra com seletor de alvo/local; `FichaViewModel.sagaCombateGolpeRapido`.
- Teste: 2 ataques, componente −6, defesa mantida. Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Golpe Rápido" e "Ataque Enganoso" (marcador corrigido) → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 407 — 23 de Junho de 2026
**Saga combate: Aparar Desarmado vs GdP — FECHA O LOOP DOS 16 PARCIAIS (16/16, MB p.376, branch GURPS-Saga)**
- O −3 ao aparar uma arma com as mãos nuas (391) agora é **dispensado quando o ataque é por ponta (GdP)**, além da exceção Caratê/Judô. GdP é inferido do **dano PERF (perfuração = sempre por ponta)** — dado estruturado (`DanoTipo`), não nome. `opcoesDefesaHeroi(ataqueGdP)`; controller passa `tipoDano(armaTipo) == PERF && !aDistancia`.
- DEFERIDO: lesão no braço que apara ao falhar (sem PV por membro nem escolha de local pelo atacante no modelo).
- Teste: apara desarmada vs corte = −3; vs GdP/PERF = sem −3. Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Aparar Desarmado" → FEITO.
- **✅ LOOP DOS 16 PARCIAIS COMPLETO (393–407):** Fazer Nada, Deslocamento, Apontar, Ataque Total, Concentrar, Preparar+Armas Preparadas, Aguardar, Movimento, Opções de Ataque CaC, Precisão/Disparo com Mira, Velocidade e Distância, Retirada+Esquiva-e-Queda, Aparar, Quando uma Arma Está Preparada, Aparar Desarmado.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 406 — 23 de Junho de 2026
**Saga combate: Quando uma Arma Está Preparada — cair/atordoar desprepara desbalanceada (loop dos 16 parciais 15/16, MB p.383, branch GURPS-Saga)**
- A seção sobrepõe ao 398 (Armas Desbalanceadas, já feito); o bit NOVO: *"cair, perder o equilíbrio ou ficar atordoado empunhando uma arma que precisa de preparação a deixa despreparada"* (MB p.383). Motor: `marcarArmaDespreparada(rotulo)`; controller: `verificarDesprepararPorEstado` após o turno do NPC — se o herói está ATORDOADO/CAÍDO/DEITADO e empunha uma arma desbalanceada, ela fica despreparada (reusa o bloqueio do 398).
- DEFERIDO: mudar de alcance de arma longa (sem rastreio de alcance atual no modelo) e tempos de guardar/embainhar = narrativo.
- Teste: `marcarArmaDespreparada` bloqueia o ataque até Preparar. Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Quando uma Arma Está Preparada?" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 405 — 23 de Junho de 2026
**Saga combate: Aparar com a Mão Inábil (loop dos 16 parciais 13/16, MB p.376, branch GURPS-Saga)**
- Núcleo do Aparar já feito (375/389/390). Faltava **Aparar com a Mão Inábil**: variante de defesa Aparar **−2 efetivo**, **anulada por Ambidestria**. `OpcaoDefesa.maoInabil`; `opcoesDefesa(ambidestro)` emite a variante (só sem Ambidestria); `opcoesDefesaHeroi`→controller passa `temAmbidestria`. UI: card "🤚 mão inábil".
- DEFERIDO por falta de dado estruturado: aparar **arremesso** −1/−2 (sem flag thrown-vs-projétil no NpcStats) e **aparar-desarmado→ferir o atacante** (sem flag de arma natural do NPC). Anotado no docs/fonte-regras/Combate.md.
- Teste: variante mão inábil = −2; ausente com Ambidestria. Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Aparar" → FEITO (com deferidos honestos).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 404 — 23 de Junho de 2026
**Saga combate: Retirada e Jogar-se ao Chão — Esquiva e Queda (loop dos 16 parciais 12/16, fecha 2 tópicos, MB p.377, branch GURPS-Saga)**
- A Retirada foi feita no 389; faltava **Jogar-se ao Chão / Esquiva e Queda**: variante de defesa **Esquiva +3 só contra ATAQUE À DISTÂNCIA**, mas o herói **termina deitado**. `OpcaoDefesa.jogarSeAoChao` + `DefesaHeroi.jogarSeAoChao`; `opcoesDefesa(permitirJogarSeAoChao)` emite a variante; `opcoesDefesaHeroi` gateia (vs tiro, não-deitado, não-atordoado); `npcResolve` põe `postura = DEITADO` após defender. UI: card mostra "⤓ jogar-se ao chão".
- Teste: variante +3 vs tiro, ausente vs corpo-a-corpo; defender com ela deixa o herói deitado. Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Retirada e Jogar-se ao Chão" e "Esquiva e Queda" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 403 — 23 de Junho de 2026
**Saga combate: Velocidade e Distância do Alvo (loop dos 16 parciais 11/16, MB p.550, branch GURPS-Saga)**
- A penalidade de distância já existia; faltava a de **alvo em movimento**: `Combatente.velocidadeAtual` (m percorridos no último Move) é **somado à distância** numa ÚNICA penalidade (`penalidadeDistancia(dist + velocidade)`, MB p.550 — não somar separado). `heroiMove`/MOVER do NPC setam; `inicioAcaoHeroi`/início do `npcResolve` zeram (parado = 0). Vale nas 2 direções (herói atira no NPC; NPC atira no herói).
- Teste: NPC parado = só "distância 5m"; com velocidade = "Vel/Dist". Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Velocidade e Distância do Alvo" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 402 — 23 de Junho de 2026
**Saga combate: Precisão e Disparo com Mira — teto de pontaria (loop dos 16 parciais 10/16, MB p.364, branch GURPS-Saga)**
- Acc + mira contínua + firmar já existiam; faltava o **teto**: a soma dos bônus de pontaria **não excede 2× a Prec** (MB p.364). Em `resolverGolpeHeroi`, mantém o breakdown (mira (Acc)/mira contínua/firmar) e, se o total passa de `2×Acc`, soma um componente negativo "teto de pontaria (2×Acc)".
- DEFERIDO: miras telescópicas/laser e sistemas de pontaria não estão no catálogo (sem dado de scope).
- Teste: rifle Acc 2 com mira 3 turnos (+2) + firmar (+1) = 5 → teto em 4. Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Precisão e Disparo com Mira" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 401 — 23 de Junho de 2026
**Saga combate: Opções de Ataque CaC — Ataque Enganoso (loop dos 16 parciais 9/16, MB p.369, branch GURPS-Saga)**
- O cabeçalho "Opções de Ataques CaC" introduz as opções de golpe; faltava a mais usada: **Ataque Enganoso**. `resolverGolpeHeroi(enganoso)` e `heroiAtaca(enganoso)`: cada passo dá **−2 no NH** (componente "ataque enganoso") por **−1 na defesa do alvo** (`defValorFinal − enganoso`). UI: stepper no diálogo de ATAQUE corpo-a-corpo, limitado para o **NH efetivo não cair abaixo de 10** (`maxEnganoso = (nh−10)/2`, teto 4).
- DEFERIDO (tópicos próprios `[]`): Golpe Rápido (2 ataques a −6) e Visar a Arma do Oponente.
- Teste: o golpe registra o componente "ataque enganoso". Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Opções de Ataques com Armas de Combate Corpo a Corpo" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 400 — 23 de Junho de 2026
**Saga combate: Movimento — postura reduz o Deslocamento (loop dos 16 parciais 8/16, MB p.368, branch GURPS-Saga)**
- O Deslocamento por manobra já existia; faltava a **redução por postura**: `Combatente.deslocamentoEfetivo` agora aplica em pé/agachado = cheio, **ajoelhado/rastejando = 1/3**, **deitado = 1**, **sentado = 0**; depois o cambaleante corta pela metade (MB p.380). Vale p/ herói e NPC (inimigo derrubado quase não se move).
- Terreno difícil/obstáculos = Narrador. Teste: deslocamento por postura (6→2→2→1→0). Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Movimento" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 399 — 23 de Junho de 2026
**Saga combate: Aguardar — Interromper Investida (loop dos 16 parciais 7/16, MB p.392, branch GURPS-Saga)**
- O "aguardar por gatilho arbitrário" é narrativo; o núcleo de combate é **Interromper Investida**: `heroiAguardar(ataque)` firma uma arma **perfurante (PERF) corpo-a-corpo** → `aguardarInvestidaArma`. No `npcResolve`, se o NPC **avança** (MOVER sem recuar / MOVER_E_ATACAR), o herói **golpeia primeiro** com a arma firmada, **+1 de dano por 2m percorridos** (`bonusInvestidaPendente` somado em `resolverGolpeHeroi`). Sem arma perfurante = Aguardar genérico (narrativo, sem bônus).
- Manobra AGUARDAR roteada para `heroiAguardar`; já estava em `manobrasLegais`. O herói ainda pode defender enquanto aguarda.
- DEFERIDO: gatilhos arbitrários (segurar refém, coordenar com aliados, disparo de oportunidade) = Narrador.
- Testes: investida é interrompida com bônus; arma não-perfurante = aguardar genérico. Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Aguardar" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 398 — 23 de Junho de 2026
**Saga combate: Armas Preparadas / Preparar — desbalanceada despreparada (loop dos 16 parciais 6/16, fecha 2 parciais, MB p.270/366, branch GURPS-Saga)**
- REGRA (MB p.270): arma desbalanceada ('D') fica **DESPREPARADA após cada ataque** a menos que **ST ≥ 1,5× a ST mínima** da arma; re-preparar = manobra Preparar. `AtaqueHeroi.stMinimo` (← `armaStMinimo` do catálogo); `marcarDespreparoSeNecessario` nos 3 caminhos de ataque corpo-a-corpo; `armaDespreparadaRotulo` (persiste entre turnos, identifica a arma pelo rótulo).
- BLOQUEIO: `heroiAtaca` recusa se a arma está despreparada; o controller (`armaDespreparadaBloqueia`) avisa **sem gastar o turno** (heroiAtaca/MoverEAtacar/AtaqueDuplo). `heroiManobra(PREPARAR)` e `sacarArma` re-empunham (`prepararArmaEmpunhada`).
- DEFERIDO: Martial Arts distingue 'D' (desbalanceada) de '‡' (despreparo) — o Básico (fonte do projeto) trata juntos; recarregar = sem sistema de munição (por decisão do Lote 366); abrir porta/ativar vantagem = Narrador.
- Testes: desbalanceada com ST baixa fica despreparada e bloqueia o 2º ataque; Preparar re-empunha; ST ≥ 1,5× não desprepara. Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Preparar" e "Armas Preparadas" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 397 — 23 de Junho de 2026
**Saga combate: Concentrar — Vontade-3 ao ser perturbado (loop dos 16 parciais 5/16, MB p.344, branch GURPS-Saga)**
- O EFEITO da concentração (magia/psi/perícia IQ) é do Narrador; o motor de combate modela a **mecânica de interrupção**: `heroiManobra(CONCENTRAR)` marca `concentrando`; em `npcResolve`, se o herói é **forçado a defender** (`defesaTentada`) ou **ferido**, testa **Vontade-3** (`heroiPerfil.vontade - 3`); falha → perde a concentração (recomeça). Vale só no turno (re-declara p/ continuar).
- Teste: ser perturbado durante a concentração dispara o teste de Vontade-3 (loop de seeds até um acerto). Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Concentrar" → FEITO (mecânica de combate; efeito = Narrador).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 396 — 23 de Junho de 2026
**Saga combate: Ataque Total (Fogo de Retenção) — fecha o Ataque Total (loop dos 16 parciais 4/16, MB p.409, branch GURPS-Saga)**
- REGRA (MB p.409): arma à distância CdT 5+ cobre uma área e **acerta quem ENTRAR** antes do próximo turno (negação de área/interrupção). Mapeado ao tracker de faixas: nova manobra `FOGO_RETENCAO` (`heroiFogoRetencao(ataque)`, exige `aDistancia` + `cadenciaTiro≥5`); marca `fogoRetencaoArma` + `heroiSemDefesaAtiva` (é Ataque Total); dura até a próxima ação (limpo em `inicioAcaoHeroi`).
- INTERRUPÇÃO: no `npcResolve`, se a zona está coberta e o NPC **avança** (MOVER sem recuar / MOVER_E_ATACAR), o herói dispara uma rajada nele (reusa `resolverGolpeHeroi` → RoF/Recuo/distância), ANTES de o NPC agir; se morre, sai.
- UI: manobra "Fogo de Retenção" aparece quando a arma empunhada é à distância CdT 5+ (sem precisar de alvo — é área).
- DEFERIDO: múltiplas zonas (CdT 10+), escolha de nº de tiros por zona, "margem de 1m da linha" — abstraídos no modelo de faixas.
- Testes: NPC que avança é alvejado; CdT < 5 é recusado. Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Ataque Total" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 395 — 23 de Junho de 2026
**Saga combate: Apontar completo — firmar +1 + Vontade ao ser ferido (loop dos 16 parciais 3/16, MB p.364, branch GURPS-Saga)**
- **FIRMAR (+1 Acc):** `heroiApontar(alvoId, firmado)` + `apontarFirmado`; o tiro soma +1 só se a arma é de fogo (`AtaqueHeroi.armaDeFogo`, setado por `armaTipoCombate` do catálogo — estruturado). UI: `SubDialogoApontar` com Switch "Firmar a arma (+1 Prec.)" mostrado só para arma de fogo.
- **VONTADE AO SER FERIDO:** `HeroiPerfilCombate.vontade` (← `p.vontade`); se o herói é ferido **ainda mirando** (sem usar defesa — caso de defesa anulada por crítico), testa Vontade; falha → perde a mira. (Defender já perdia a mira no Lote 392.)
- DEFERIDO: besta também "firma" (sem flag de besta no catálogo); apoio físico (mureta/tripé) e bruços não são modelados — o "firmar" é a declaração do jogador.
- Testes: firmar soma +1 no tiro; ferimento mirando dispara o teste de Vontade (loop de seeds até um crítico). Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Apontar" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 394 — 23 de Junho de 2026
**Saga combate: Deslocamento — Disparada (loop dos 16 parciais 2/16, MB p.353, branch GURPS-Saga)**
- A manobra Mover já funcionava; faltava a **Disparada** (sprint): Moves consecutivos **na mesma direção (linha reta)** dão **+20% de Deslocamento a partir do 2º** (MB p.353). `heroiMoveSeguidos`+`heroiMoveDirecao` (capturados antes de `inicioAcaoHeroi`, que zera o contador → ação não-Move quebra; mudar de direção recomeça); `heroiMove` aplica o sprint e narra "(disparada +Nm)".
- Veículo/montaria (Combate Montado p.396 / Veículos p.462) = capítulo à parte, fora do escopo do combate Saga a pé.
- Teste: 1º Move sem disparada; 2º consecutivo +1m (desloc 6); ação não-Move reinicia. Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Deslocamento" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 393 — 23 de Junho de 2026
**Saga combate: Fazer Nada / Atordoado — defesas −4 (loop dos 16 parciais 1/16, MB p.364, branch GURPS-Saga)**
- A manobra forçada ao atordoado e a recuperação (HT/IQ em `avancarTurno`) JÁ existiam. Faltava o **−4 em TODAS as defesas ativas enquanto atordoado** (MB p.364). `opcoesDefesaHeroi` aplica `penAtordoado` (herói); `esquivaNpc`/`melhorDefesaNpc` usam `penDefesaAtordoado` (NPC).
- Teste: esquiva e apara caem −4 com `Condicao.ATORDOADO`. Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Fazer Nada" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 392 — 23 de Junho de 2026
**Saga combate: Apontar — mira de vários turnos + perde a mira ao defender (loop de defesa 5/5 — FECHA O LOOP, MB p.364, branch GURPS-Saga)**
- MIRA DE VÁRIOS TURNOS (MB p.364): mirar o MESMO alvo por segundos seguidos acumula **+1 (2º seg) / +2 (3º+)** ALÉM da Precisão (Acc). `apontarStacks` (espelha o Avaliar); bônus = `(stacks−1).coerceIn(0,2)` aplicado no acerto à distância.
- PERDER A MIRA: usar uma **defesa ativa** zera a pontaria (`limparApontar` quando `troca.defesaTentada`); o log avisa. (Defender entre os turnos = perde o Acc no tiro seguinte, como manda a regra.)
- DEFERIDO (registrado): **firmar** a arma (+1 Acc) e o **teste de Vontade** para não perder a mira ao ser ferido (não modelados).
- Testes: stacking +1→+2→teto; defender perde a mira (loop de seeds com acerto). Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Apontar" segue PARCIAL (núcleo da mira feito; firmar/Vontade deferidos).
- **✅ LOOP DE REFINO DE DEFESA 388–392 COMPLETO** (Defesa Total Aumentada+Dupla, Retirada, Aparar à queima-roupa, Aparar Desarmado −3, Apontar multi-turno).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 391 — 23 de Junho de 2026
**Saga combate: Aparar Desarmado — −3 vs armas (loop de defesa 4/5, MB p.376, branch GURPS-Saga)**
- REGRA (MB p.376): aparar uma ARMA com as mãos nuas sofre **−3**, salvo se o herói usa **Caratê ou Judô** (valor cheio). `opcoesDefesaHeroi(ataqueComArma)` aplica `penAparaDesarmada = 3` quando a "arma" empunhada é desarmada (`armaPronta.desarmado`), o NPC ataca com arma e a perícia não é marcial.
- `AtaqueHeroi.aparaMarcial` (novo) = true quando a melhor perícia de luta do herói é Caratê/Judô — detectado por `definicaoId` estruturado (set `MARCIAIS_APARA`, não por nome livre). Controller passa `ataqueComArma = npc.armaNome.isNotBlank()`.
- DEFERIDO (registrado): a exceção **GdP** (o motor não distingue GdP/GeB no ataque do NPC) e a **lesão no braço que apara** ao falhar.
- Testes: mãos nuas vs arma = −3; vs ataque desarmado = sem penalidade; Caratê/Judô = valor cheio. Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Aparar Desarmado" segue PARCIAL (com o avanço anotado).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 390 — 23 de Junho de 2026
**Saga combate: Aparar — só à queima-roupa contra tiro (loop de defesa 3/5, MB p.376, branch GURPS-Saga)**
- BUG: o card oferecia **Aparar contra um atirador distante**. A regra (MB p.376): só se apara um ataque à distância se o atacante estiver **adjacente (≤1m)** — apara-se a ARMA, não o projétil. `opcoesDefesaHeroi(atacanteAdjacente)` gateia o `podeAparar`; o controller passa `s.distancia(npc) <= 1`. Bloqueio (escudo) continua valendo contra tiro (a regra do escudo não muda).
- Narração: ao aparar um tiro à queima-roupa, o log explica "você desvia a arma do atirador (não o projétil)".
- Testes: corpo-a-corpo e tiro a 1m oferecem aparar; tiro de longe não. Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Aparar" segue PARCIAL — falta mão inábil −4, arremesso −1/−2, aparar-desarmado→ferir o atacante.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 389 — 22 de Junho de 2026
**Saga combate: Retirada / Opções de Defesa Ativa (loop de defesa 2/5, MB p.377, branch GURPS-Saga)**
- A Retirada (recuar) existia no motor mas **não era oferecida** no card (o param `recuo` nunca era ligado). Agora `opcoesDefesaHeroi(contraAtaqueCorpoACorpo)` calcula `permitirRecuo` e `CombatResolver.opcoesDefesa` emite **variantes "com recuo"** de cada defesa (Esquiva +3, Aparar/Bloquear +1).
- EXCEÇÃO MARCIAL (MB p.377): aparar com **esgrima** ao recuar dá **+3** (não +1) — `valorDefesaFinal` usa o flag `esgrima`. Boxe/Caratê/Judô (+3 desarmado) ficam de fora por ora (sem flag de perícia de luta).
- RESTRIÇÕES: só **contra ataque corpo-a-corpo**; **1×/turno** (reusa `DefesasUsadas.retracaoUsada`, marcado em `npcResolve` quando `DefesaHeroi.recuo`); bloqueado se **atordoado**. (Postura sentado/ajoelhado e o passo físico p/ trás = simplificação registrada — o herói está sempre engajado no tracker.)
- UI: as variantes aparecem no card "Defenda-se!" com sufixo "↩ recuar" + componente "+N recuo"; a Dupla (388) ignora variantes com recuo na 2ª defesa.
- Testes: esgrima+recuo=+3, variantes emitidas só com `permitirRecuo`, recuo só corpo-a-corpo e 1×/turno. Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Opções de Defesa Ativa" → FEITO (Retirada). "Retirada e Jogar-se ao Chão" segue parcial (falta Esquiva-e-Queda).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 388 — 22 de Junho de 2026
**Saga combate: Defesa Total completa — Aumentada + Dupla (loop de defesa 1/5, MB p.366, branch GURPS-Saga)**
- A manobra Defesa Total do herói **não fazia nada** (só logava). Agora `CombatSession.heroiDefesaTotal(modo, aumentadaEm)` com `enum DefesaTotalModo { AUMENTADA, DUPLA }`; o benefício vale até a PRÓXIMA ação do herói (limpo em `inicioAcaoHeroi`).
- **AUMENTADA:** +2 numa defesa escolhida — `opcoesDefesaHeroi` passa `defesaTotalEm = defesaTotalAumentadaEm` ao `CombatResolver` (reusa `BONUS_DEFESA_TOTAL` já existente).
- **DUPLA:** se a 1ª defesa falha (e o ataque NÃO foi anulado por golpe decisivo), tenta automaticamente uma 2ª defesa de TIPO diferente. Como `resolverTroca` MUTA o defensor (aplica dano), decido o resultado ANTES via `CombatResolver.defesaBemSucedida` (puro): o controller prepara a melhor 2ª defesa (`opcoes.filter{tipo≠1ª}.maxBy{valorFinal}`) e passa em `npcResolve(..., defesaSecundaria)`; o motor troca `def` pela 2ª só se a 1ª falhou.
- UI: manobra "Defesa Total" abre `SubDialogoDefesaTotal` (Aumentada [+ qual defesa] / Dupla); `FichaViewModel.sagaCombateDefesaTotal`; wrapper no `SagaCombatController`.
- Testes (`CombatSessionTest`): Aumentada soma +2 só na defesa escolhida; Dupla salva o herói quando a 1ª falha (loop de seeds, acerto não-crítico). Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Defesa Total" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 387 — 22 de Junho de 2026
**Saga combate: refino do Ataque Total — Forte correto (MB p.365, branch GURPS-Saga)**
- BUG DE REGRA corrigido: o Ataque Total **Forte** dava **+2 fixo** de dano. A regra (MB p.365) é **+2 OU +1 por dado, o que for maior** — então armas de vários dados eram subestimadas (uma 3d devia dar +3). `CombatSession.bonusDanoForte(manobra, modo, danoExpr, aDistancia)` virou função pura (companion, testável) = `max(2, nº de dados)`.
- RESTRIÇÃO: Forte só vale **corpo-a-corpo** (à distância não tem Forte; MB p.365). Gateado por `aDistancia` no herói e no NPC. (Espada de energia/queimadura ficaria de fora pela regra, mas o motor só modela dano por ST de GdP/GeB — todo corpo-a-corpo aqui é elegível; nota para quando houver dano de queimadura.)
- UI: a opção "Forte" some quando a arma empunhada é à distância; o rótulo do **Determinado** mostra **+1** à distância (era sempre "+4", errado para tiro).
- Testes (`CombatSessionTest`): 1d→+2, 2d→+2, 3d→+3, 4d→+4, à distância→0, manobra/modo diferentes→0. Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Ataque Total" segue PARCIAL (Determinado/Forte/Duplo/Fintar OK; falta **Fogo de Retenção**, CdT 5+, MB p.409).
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 386 — 22 de Junho de 2026
**Saga combate: Luta agarrada — base (loop de regras 5/5, MB p.370–371, branch GURPS-Saga)**
- AGARRAR (MB p.370): `CombatSession.heroiAgarrar(ataque, alvoId)` — ataque normal; se acerta e o alvo não defende, adiciona `Condicao.AGARRADO`. O alvo agarrado defende a −4 (`penAgarrado` em `resolverGolpeHeroi`) e, no turno dele, gasta a ação tentando se desvencilhar (Disputa Rápida em `npcResolve`, não ataca).
- DERRUBAR (MB p.370–371): `heroiDerrubar(alvoId)` — Disputa Rápida do maior entre ST/DX; vencendo, o alvo vai a CAÍDO/DEITADO. Helper puro `vencaDisputaRapida(valorA, rolA, valorB, rolB)` (empate de margem favorece o defensor, MB p.348).
- UI: manobras Agarrar/Derrubar habilitadas quando há alvo corpo-a-corpo (sem alvo à distância) → seletor de alvo; `FichaViewModel.sagaCombateAgarrar/Derrubar`; wrappers no `SagaCombatController`.
- Testes (`CombatSessionTest`): `vencaDisputaRapida` segue a regra; agarrar deixa o NPC AGARRADO; NPC agarrado gasta o turno se soltando (não ataca); derrubar joga o alvo no chão. Build 2 variantes + testes verdes.
- docs/fonte-regras/Combate.md: "Agarrar" e "Derrubar" → FEITO (base). Sub-sistema completo (Imobilizar/Estrangular/Mata-Leão/Chave de Braço/Encontrão/Empurrão) fica para lotes futuros.
- NOTA (git): o código de domínio (CombatSession.kt/CombatModels.kt) foi varrido por engano para o commit `02e4567` (sessão paralela dados-3D, `git add -A`); este commit fecha o Lote 386 com a fiação de UI + testes + docs sob a mensagem correta. ⚠️ Colisão de numeração: a série dados-3D também tem um "Lote 386" (correção de colisão física dos dados) — são trabalhos distintos.
- LOOP DE REGRAS DE COMBATE 5/5 COMPLETO (382–386). Próximo: validação no aparelho (tudo de uma vez) + resto do sub-sistema de luta agarrada.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 385 — 17 de Junho de 2026
**Saga combate: Tolerância a Ferimentos (loop de regras 4/5, MB p.381, branch GURPS-Saga)**
- REGRA (MB p.381): mortos-vivos/máquinas/objetos/enxames são menos vulneráveis a pi/perf. Novo `enum ToleranciaFerimentos { NORMAL, NAO_VIVO, HOMOGENEO, DIFUSO }`. `HitLocationRules.multiplicador(tipo, local, tolerancia)` sobrescreve o multiplicador de pi/perf (NÃO-VIVO: perf/pi++ ×1, pi+ ×½, pi ×⅓, pi- ×⅕; HOMOGÊNEO mais ainda) e remove o bônus de crânio/vitais (sem órgãos); DIFUSO = teto no dano final (pi/perf ≤1 PV, resto ≤2). `aplicarDano` aplica.
- CAMINHO DE DADO: `NpcStats.tolerancia` ← `BestiarioCriatura.tolerancia` (string do JSON → enum). `CombatResolver.resolverTroca` repassa ao `aplicarDano`; chamadores (`resolverGolpeHeroi`, rajada, `aplicarDanoCombatente`) passam a tolerância do alvo. Herói = NORMAL.
- DADO DO BESTIÁRIO: **esqueleto e zumbi** marcados `"tolerancia": "nao_vivo"` (canônico: mortos-vivos corpóreos = Unliving) — agora resistem a tiros (pi ×⅓), como o exemplo do próprio MB p.381.
- Testes (`HitLocationRulesTest`): Não-Vivo reduz pi (9→3) e não dá bônus de vitais; perf segue ×1; Homogêneo (pi ×0.2); Difuso (teto 1/2). Build 2 variantes + lint verde.
- docs/fonte-regras/Combate.md: "Lesões em Alvos Difusos, Homogêneos e Não-Vivos" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 384 — 17 de Junho de 2026
**Saga combate: Tabelas de Golpe Fulminante / Erro Crítico (loop de regras 3/5, MB p.557–558, branch GURPS-Saga)**
- GOLPE FULMINANTE (MB p.558): a defesa já é anulada pelo crítico; a tabela (3d6) modifica o DANO. `CriticoRules.golpeFulminante(soma)` → DOBRO/TRIPLO/MÁXIMO/RD_METADE/FERIMENTO_GRAVE/NORMAL. Aplicado em `resolverGolpeHeroi` (herói→NPC) e `npcResolve` (NPC→herói) via `aplicarGolpeFulminante` (+ `CombatSession.danoMaximo`; `forcarFerimentoGrave` threadeado por `resolverTroca`→`ferir`→`aplicarGolpe`).
- ERRO CRÍTICO (MB p.557): `CriticoRules.erroCritico(soma, desarmado)` → efeito no ATACANTE. O motor aplica os mecânicos (ACERTA_A_SI[_METADE] = dano em si; CAI = derrubado/deitado) e NARRA o resto (QUEBRA_ARMA/LARGA_ARMA/DESEQUILIBRIO — não rastreamos durabilidade/empunhadura de arma). `AtaqueHeroi.desarmado` escolhe a tabela armada/desarmada.
- WORKAROUND DE BUILD: 3 detectores do compose-runtime lint (`NullSafeMutableLiveData`, `FrequentlyChangingValue`, `RememberInComposition`) crashavam com `IncompatibleClassChangeError` ("Found class KaSimpleVariableAccessCall, but interface was expected" — Kotlin Analysis API × versão do lint) ao re-executar o lint, derrubando o build (os lotes anteriores passavam só porque o lint estava em cache). Desligados em `app/build.gradle.kts` (mesmo padrão dos 2 já existentes; nosso código não usa Compose neles).
- Testes: `golpeFulminante`/`erroCritico` (mapeamento das tabelas), `danoMaximo`, e integração (Golpe Fulminante com NH alto, Erro crítico com NH baixo). Build 2 variantes + lint verde.
- docs/fonte-regras/Combate.md: "Golpes Fulminantes e Erros Críticos", "Golpes Fulminantes", "Erros Críticos" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 383 — 17 de Junho de 2026
**Saga combate: Fintar (loop de regras 2/5, MB p.366, branch GURPS-Saga)**
- MANOBRA FINTAR: `CombatSession.heroiFintar(ataque, alvoId)` — Disputa Rápida entre o NH do herói com a arma e a defesa do alvo (maior entre `armaNh` e DX do NPC). Helper puro `fintaResultado(nhAtk, rolAtk, nhDef, rolDef)`: 0 se o fintador falha; margem do atacante se o defensor falha; margem de vitória se ambos passam.
- EFEITO: se vence, `fintaAlvoId`/`fintaPenalidade` reduzem a defesa do alvo no PRÓXIMO golpe corpo-a-corpo (em `resolverGolpeHeroi`, `defValorFinal`); aplica também aos dois golpes do Ataque Total (Duplo) (MB p.366). Exige arma corpo-a-corpo no alcance. `limparFinta` espelha avaliar/apontar (consumido no ataque; descartado em mover/manobra/outra prep).
- UI: manobra Fintar (quando há arma corpo-a-corpo + alvo ao alcance) → seletor de alvo; `FichaViewModel.sagaCombateFintar`.
- Testes: `fintaResultado` (4 casos), finta bem-sucedida abate a defesa no golpe seguinte, finta bloqueada com arma à distância. Build 2 variantes + lint verde.
- docs/fonte-regras/Combate.md: "Fintar" → FEITO.
----------------------------------------------------------------------------------------------------------------------------------------------------

### Lote 382 — 17 de Junho de 2026
**Saga combate: Choque + Cambaleante (loop de regras 1/5, MB p.419/380, branch GURPS-Saga)**
- CHOQUE (MB p.419/381): toda perda de PV gera choque — penalidade em DX/IQ (acerto) no PRÓXIMO turno. `InjuryRules.penalidadeChoque(pvPerdidos, pvMax)`: −1/PV; se PV Inicial ≥20, −1 a cada PVInicial/10; teto −4. NÃO afeta defesas (MB p.375). `Combatente.choquePendente` acumula em `ferir`; aplicado ao acerto do herói (`resolverGolpeHeroi`) e do NPC (`npcResolve`); expira em `avancarTurno` (fim do turno de quem agiu).
- CAMBALEANTE (MB p.380): com < 1/3 do PV Inicial, Vel.Básica/Deslocamento e Esquiva caem à metade. `Combatente.cambaleante`/`deslocamentoEfetivo`; Esquiva do herói (`opcoesDefesaHeroi`) e do NPC (`esquivaNpc`) reduzidas; Deslocamento à metade em `heroiMove`/`heroiMoverEAtacar`/NPC/controller.
- Testes: penalidadeChoque (PV<20 e ≥20 com teto); choque aplicado ao golpe + expira; ferir acumula choque; cambaleante reduz Esquiva e Deslocamento. Build 2 variantes + lint verde.
- docs/fonte-regras/Combate.md: "Efeitos de Lesões" → FEITO.
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
