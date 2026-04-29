# Acompanhamento do Projeto da Ficha GURPS (Para Rodolfo)

**Ãšltima AtualizaÃ§Ã£o:** 28 de Abril de 2026
**Status Atual:** ERA DO RACIOCÃ�NIO - Conectividade Estabilizada ðŸŒ�ðŸ›¡ï¸� | Lote 114 CONCLUÃ�DO ðŸš€

### Sincro V24: Super Release 2.0 (Lote 86)
- **LanÃ§amento Oficial V1.5.0**: Build de produÃ§Ã£o gerada para as variantes Visual e PraCego.
- **UnificaÃ§Ã£o de TraÃ§os e Busca Inteligente**: FinalizaÃ§Ã£o do Lote 86 com todas as melhorias de interface e blindagem de cÃ¡lculo integradas.
- **PreparaÃ§Ã£o de Update**: Arquivo `update.json` atualizado para notificar os usuÃ¡rios sobre a nova versÃ£o. ðŸ””ðŸ›¡ï¸�

### Sincro V23: Mapeamento Arquitetural e Blindagem de Conhecimento
- **Mapa Detalhado de Engenharia**: Criado o `MAPA_DETALHADO.md` com o inventÃ¡rio completo de funÃ§Ãµes, motores de RPG, scripts e suÃ­tes de teste. ðŸ—ºï¸�ðŸ”�
- **EstabilizaÃ§Ã£o de Arquitetura**: VerificaÃ§Ã£o e confirmaÃ§Ã£o da integridade do cÃ³digo pÃ³s-sincronia, garantindo paridade total entre o ambiente de desenvolvimento e o dispositivo funcional do usuÃ¡rio. ðŸ›¡ï¸�âœ¨
- **Nexus Arcano & Qualidade**: Mapeamento explÃ­cito das ferramentas de auditoria e testes automatizados para prevenir regressÃµes em futuras intervenÃ§Ãµes de IA. ðŸ§ªðŸŽ¯

### Sincro V22: Blindagem de Dados (V1.4.5 - Final)
- **Trava de Auto-Save**: Implementada barreira de proteÃ§Ã£o que bloqueia o salvamento automÃ¡tico durante o carregamento de fichas, prevenindo a sobrescrita acidental de arquivos com dados vazios. 
- **Feedback de Carga**: Sistema de NotificaÃ§Ã£o atualizado para reportar erros reais de desserializaÃ§Ã£o, eliminando falsos positivos de "Ficha Carregada". 
- **Rebuild V22**: APKs reconstruÃ­dos com logs de diagnÃ³stico para rastrear falhas remanescentes em fichas corrompidas. 

### Lote 101: O Retorno ao CÃ³dex (PrecisÃ£o Literal) - CONCLUÃ�DO
- **Motor DeterminÃ­stico**: InversÃ£o de prioridade (Manual-First) com bÃ´nus de 100 pontos para termos literais. ðŸŽ¯
- **Fim do Chute**: Implementada a "PrisÃ£o de Contexto" no Auditor. Se nÃ£o estÃ¡ no livro, a IA nÃ£o inventa. ðŸ›¡ï¸�
- **Sincro AutomÃ¡tica**: SincronizaÃ§Ã£o de manuais agora ocorre sozinha ao abrir o Mestre IA, garantindo paridade total. âš™ï¸�
- **CorreÃ§Ã£o Mojibake**: Limpeza de sÃ­mbolos tÃ©cnicos (mÂ³, Ã—) no banco de dados. ðŸ§¹
- **BÃ´nus de Tabela**: Motor calibrado para priorizar tabelas tÃ©cnicas sobre descriÃ§Ãµes de vantagens. ðŸ“Š

---

## Lembretes Fixos do Seu Projeto

### 1. Ferramentas AcessÃ­veis
NÃ³s sempre cuidamos para que toda versÃ£o lanÃ§ada tenha a versÃ£o **Visual** (Normal) e a versÃ£o **PraCego** (Com botÃ£o e navegaÃ§Ã£o de acessibilidade para cegas por meio do programa TalkBack de celular). O emulador costuma focar na visual para seu teste rÃ¡pido.

### 2. ConsistÃªncia de Dados
Os dados das magias, vantagens e perÃ­cias moram em arquivos de texto (tipo planilhas, chamados de arquivos **JSON**). IAs que forem alterar algo lÃ¡ nÃ£o podem apagar aspas ou colchetes sem cuidado.

---

##  Registro de Lotes e Commits (Rede de SeguranÃ§a)
*Todo Agente Ã© obrigado a quebrar tarefas maiores em "Lotes Curtos" isolados de um arquivo por vez, efetuando o Commit no final para gerar um Ponto de Retorno seguro para o usuÃ¡rio. Cada nova "Aba" ganha tambÃ©m sua prÃ³pria pasta.*

> Lista de Lotes Realizados a partir de Abril de 2026:

* [Feito] Lote 1.2: ExtraÃ§Ã£o de Loaders Json (DataRepository)       | `(Commit: 7c52e26)`
* [Feito] Lote 2.1: SeparaÃ§Ã£o de PeÃ§as do Nexus (Modelos)           | `(Commit: a6992f1)`
* [Feito] Lote 2.2: O CÃ©rebro A* (Planejador de Caminho)            | `(Commit: 113b540)`
* [Feito] Lote 2.3: O Motor de DiagnÃ³stico (Raio-X)                 | `(Commit: 105949a)`
* [Feito] Lote 2.4: Limpeza final (Helpers & Parser)                | `(Commit: aab9ff2)`
* [Feito] Lote 3: ModularizaÃ§Ã£o do FichaViewModel                   | `(Commit: 20414fd)`
* [Feito] Lote 4: PadronizaÃ§Ã£o UTF-8 e Motor Modo Alvo              | `(Commit: 0912746)`
* [Feito] Lote 6: ModularizaÃ§Ã£o do TraitDialogs                     | `(Commit: 3d186e9)`
* [Feito] Lote 6.1: CorreÃ§Ã£o de SeleÃ§Ã£o e Regras Especiais          | `(Commit: 0e9aee5)`
* [Feito] Lote 7: RefatoraÃ§Ã£o da UI de Rolagem (TabRolagem.kt)      | `(Commit: 042cd2d)`
* [Feito] Lote 7.1: CorreÃ§Ãµes de Discord, UI de Ataque e Mojibake   | `(Commit: 6c3f773)`
* [Feito] Lote 7.2: RefatoraÃ§Ã£o do RolagemDialogs.kt (FragmentaÃ§Ã£o) | `(Commit: 6c8a800c)`
* [Feito] Lote 8: AtualizaÃ§Ã£o EstÃ©tica dos Ã�cones das Abas          | `(Commit: 1cec435)`
* [Feito] Lote 9: Interface de NavegaÃ§Ã£o RPGÃ­stica (Ultra-Premium)  | `(Commit: 9f13f43)`
* [Feito] Lote 10: Melhoria do Ataque Inato e Skill Project Map     | `(Commit: e1c1a1b)`
* [Feito] Lote 11: ModernizaÃ§Ã£o da UI do Mestre IA (ChatGPT)        | `(Commit: e42cce6)`
* [Feito] Lote 12: Card de dano adaptativo e soma automÃ¡tica de ST. | `(Commit: 783c295)`
* [Feito] Lote 13: Fluxo de Mestre IA com confirmaÃ§Ã£o e anÃ¡lise consultiva. | `(Commit: a104632)`
* [Feito] Lote 14: Mestre IA Interativo (Antigravity-style) com botÃµes de aÃ§Ã£o e tom inquisitivo. | `(Commit: a104632)`
* [Feito] Lote 15: RestauraÃ§Ã£o das Defesas Ativas na Aba de Rolagem | `(Commit: 5c7c369)`
* [Feito] Lote 16: ModularizaÃ§Ã£o da FichaViewModel e ProtÃ³tipo de SugestÃµes ClicÃ¡veis (Mestre IA) | `(Commit: c279b87)`
* [Feito] Lote 17: AutomaÃ§Ã£o da vantagem Golpeadores (Strikers) | `(Commit: 65ec4ef)`
* [Feito] Lote 18: InteligÃªncia de NH e Aparar para Golpeadores/Ataque Inato | `(Commit: 687b8d9)`
* [Feito] Lote 19: Arquitetura Modular de Vantagens e DocumentaÃ§Ã£o de IA | `(Commit: 6132f9b)`
* [Feito] Lote 20: AutomaÃ§Ã£o da Vantagem Dentes (GdP-1 e Tipos) | `(Commit: 5fd613d)`
* [Feito] Lote 21: PersistÃªncia de SeleÃ§Ã£o de Ataque/Dano na SessÃ£o | `(Commit: 217eaf4)`
* [Feito] Lote 22: Ajuste Vantagem Dentes e Nomeclatura de Dano PT-BR | `(Commit: ada9efd)`
* [Feito] Lote 23: Limpeza de Unicode Mojibake (\uXXXX) no CÃ³digo Fonte | `(Commit: 33c771f)`
* [Feito] Lote 24: AutomaÃ§Ã£o de Garras, Cascos e Flexibilidade (+3 PerÃ­cias) | `(Commit: d4a5e2f)`
* [Feito] Lote 25: Bloqueio e Esquiva Ampliada e EstabilizaÃ§Ã£o de Build        | `(Commit: f202c06)`

* [Feito] Lote 27: Redesign Visual das Defesas (Cards Individuais e BotÃµes)    | `(Commit: a9b2c3d)`
* [Feito] Lote 28: RefatoraÃ§Ã£o da TabRolagem.kt (ExtraÃ§Ã£o de DiÃ¡logos)         | `(Commit: de16e6f)`
* [Feito] Lote 29: OtimizaÃ§Ã£o de EspaÃ§o e Padding (BotÃµes 2dp)             | `(Commit: b3f2d1e)`
* [Feito] Lote 30: Integrar BD do Escudo na Esquiva/Apara + Notas de UI      | `(Commit: 249874d)`
* [Feito] Lote 31: AutomaÃ§Ã£o da Vantagem Mestre de Armas (Dano NH vs DX)      | `(Commit: c5a16f0)`
* [Feito] Lote 32: CategorizaÃ§Ã£o e Filtragem Estrita do Mestre de Armas     | `(Commit: e8f9a2c)`
* [Feito] Lote 33: Mapeamento Estrito e CorreÃ§Ã£o de Vazamento (Mestre de Armas) | `(Manual: Antigravity)`
* [Feito] Lote 34: RestauraÃ§Ã£o da Apara/Bloqueio e IDs de PerÃ­cia Sublinhados | `(Commit: f3c3e9a)`
* [Feito] Lote 35: Visibilidade Permanente de Defesas (Fallback AutomÃ¡tico) | `(Commit: 2d1948b)`
* [Feito] Lote 36: VÃ­nculo Estrito do Dano com o Ataque Selecionado (Final) | `(Commit: 194f5d1)`
* [Feito] Lote 37: Acessibilidade Ultra na Aba de Rolagem (TalkBack) | `(Commit: 51dbd9f)`
* [Feito] Lote 38: AtualizaÃ§Ã£o do Mapa do Projeto (100% PrecisÃ£o) | `(Commit: 53b1f89)`
* [Feito] Lote 39: AtualizaÃ§Ã£o da Skill de Vantagens (TraitRule API) | `(Commit: d5768dd)`
* [Feito] Lote 40: AtualizaÃ§Ã£o das Regras Operacionais (Commits e PraCego) | `(Commit: 4f63b8a)`
* [Feito] Lote 41: AtualizaÃ§Ã£o das Regras de RPG GURPS (Combat Context) | `(Commit: d26daad)`
* [Feito] Lote 45: Restauracao do Aro Vermelho e Filtro de Invisibilidade (Sincro V16) | `(Commit: f604490)`
* [Feito] Lote 46: Pente Fino de Acessibilidade (TalkBack) em Pre-requisitos (Sincro V17) | `(Commit: bab709c)`
* [Feito] Lote 47: SincronizaÃ§Ã£o em Nuvem InvisÃ­vel (Railway + DeviceID) (Sincro V18) | `(Manual: Antigravity)`
* [Feito] Lote 48: Biblioteca Unificada e ProteÃ§Ã£o contra Conflitos de Nomes (Sincro V19) | `(Manual: Antigravity)`
* [Feito] Lote 49: RestauraÃ§Ã£o SistÃªmica e UnificaÃ§Ã£o de Branches (Integridade Total) | `(Commit: 7b346b1)`
* [Feito] Lote 50: Motor Nexus Arcano Estabilizado (ResoluÃ§Ã£o Desejo + Metas Incrementais) | `(Commit: a2e2820)`
* [Feito] Lote 51: Mestre IA PRIME  - Soberania Multi-Flavor (Gemini/DeepSeek) + SeguranÃ§a Sete Chaves (Vault) + Rastreabilidade TÃ©cnica | `(Commit: babc20a)`
* [Feito] Sincronia de Roadmap: Roadmap do Mestre IA atualizado com novos lotes | `(Commit: d63158d)`
* [Feito] Lote 52: Robustez no Parsing de JSON (Auto-Healing) | `(Commit: f59f90b)`
(Problema: O uso de Regex para capturar JSON no MestreIAClient Ã© eficiente, mas frÃ¡gil se a IA enviar um JSON malformado ou truncado.
Melhoria: Implementar um "JSON Repair" (como uma limpeza agressiva de caracteres de controle) e validar a estrutura contra o MestreIAResponse usando KotlinX.Serialization antes de chegar ao UseCase. Se o JSON falhar, o sistema deve pedir automaticamente uma re-formataÃ§Ã£o para a IA (Auto-Healing).)
* [Feito] Lote 53: Contexto Diferencial (Token Economy) | `(Commit: fbdb0da)`
.(Problema: Enviar o personagem inteiro em cada mensagem gasta muitos tokens e pode confundir a IA com dados irrelevantes.
Melhoria: No processarPrompt, implementar uma lÃ³gica que identifique o que mudou na ficha ou o que Ã© relevante para a pergunta atual. Se o usuÃ¡rio pergunta sobre "Dano", nÃ£o precisamos enviar a lista de "Equipamento de Camping".)
* [Feito] Lote 54: Streaming de Resposta (SSE/UX) | `(Commit: 89ab389)`
(Problema: Esperar a resposta completa da API pode gerar um "atraso" perceptÃ­vel na UI (LatÃªncia).
Melhoria: Se as APIs (OpenRouter/Gemini) suportarem, implementar Server-Sent Events (SSE). Ver o Mestre IA "escrevendo" em tempo real melhora drasticamente a percepÃ§Ã£o de performance.)
* [Feito] Lote 55: Auditoria de Regras (Fiscal Ativo) | `(Commit: fc1f47c)`
* [Feito] Lote 56: Local-First RAG (Busca Vetorial) | `(Commit: c110272)`.(Problema: O buscador semÃ¢ntico no MestreIARagEngine pode ser pesado para buscas em muitos arquivos JSON.
Melhoria: Avaliar o uso de uma pequena biblioteca de busca vetorial local (ou um index prÃ©-calculado) para que o findRelevantChunks seja instantÃ¢neo, mesmo com manuais extensos.)
* [Feito] Lote 57: ReconstruÃ§Ã£o de Elite (RAG 1194 Chunks) e Triplo Fallback (600 Usos) | `(Manual: Antigravity)`
* [Feito] Lote 58: EstabilizaÃ§Ã£o do Mestre IA (Motor de Reparo por Pilha) | `(Commit: bf9b73c)`
    - [x] **EstabilizaÃ§Ã£o IA 2026**: MigraÃ§Ã£o total para Gemini 3.0 e 2.5 (resolvendo 404 de modelos antigos).
    - [x] **RestauraÃ§Ã£o Nexus**: Motor de investigaÃ§Ã£o multi-estÃ¡gio e trilhas de prÃ©-requisitos universalizado.
    - [x] **Blindagem de ConexÃ£o**: CorreÃ§Ã£o de headers OpenRouter e normalizaÃ§Ã£o de histÃ³rico de chat.
    * ImplementaÃ§Ã£o de algoritmo de fechamento de JSON baseado em Pilha (Stack) e BotÃµes de DiagnÃ³stico UI.
* [Feito] Lote 59: IA Master Laboratory (Suite de Auditoria Python) | `(Commit: bf9b73c)`
    * CriaÃ§Ã£o do validador de fidelidade ao catÃ¡logo e simulador de stress offline em Python.
* [Feito] Lote 60: Stress Test do Motor de Pilha e Sync do Gabarito de Ouro | `(Manual: Antigravity)`



    * RefatoraÃ§Ã£o do `MestreIAResponse` para aceitar Objetos em vez de Strings para Vantagens e Magias (alinhado com o Gabarito de Ouro).
    * EvoluÃ§Ã£o do `repararJsonTruncado` para fechar Strings `"` abertas e ignorar sufixos nocivos.
    * Teste UnitÃ¡rio criado: `testStressReparoJsonTruncado` com aninhamento de 500pts aprovado.

### Lote 60: DefiniÃ§Ã£o de Schemas Nativos (Tool Builder)
*   **Commit:** `d025cc1 feat(ia): Lote 60 - define schemas nativos de Function Calling para Gemini e OpenAI`
*   **Melhorias Implementadas:**
    *   CriaÃ§Ã£o da classe `MestreIATools.kt` para orquestrar as ferramentas.
    *   DefiniÃ§Ã£o rigorosa de Schemas JSON (`fill_character_sheet` e `search_rules`) que forÃ§am a IA a obedecer o layout da ficha.
    *   Fim da Engenharia de Prompt (Regex) para requisiÃ§Ã£o de criaÃ§Ã£o de fichas.

### Lote 61: O Motor ReAct (Orquestrador AssÃ­ncrono)
*   **Commit:** `a353efe feat(ia): Lote 61 - Orquestrador ReAct intercepta Tool Calls para preenchimento de ficha sem regex`
*   **Commit:** `161fb58 feat(ia): implementa ReAct loop real no MestreIAUseCase para pesquisa de regras e ativa criacao em todos os modos`
*   **Melhorias Implementadas:**
    *   RefatoraÃ§Ã£o profunda em `MestreIAClient` e `MestreIAUseCase` para capturar chamadas assÃ­ncronas no protocolo SSE.
    *   InterceptaÃ§Ã£o da ferramenta nativa `fill_character_sheet`.
    *   Passagem direta do JSON estrito e perfeito para a `FichaIADelegate`, eliminando completamente os riscos de JSON malformado e ativando instantaneamente o botÃ£o de IntegraÃ§Ã£o.
    *   **Loop ReAct Verdadeiro:** Implementada a recursÃ£o nativa. Se a IA solicitar `search_rules`, o sistema intercepta, busca as regras via RAG, e faz uma chamada recursiva devolvendo o texto para a IA automaticamente para que ela conclua sua tarefa.
    *   Desbloqueio de Ferramentas: `fill_character_sheet` agora estÃ¡ sempre disponÃ­vel, mesmo no modo PadrÃ£o/Conversa, permitindo que a IA forje fichas em qualquer cenÃ¡rio.

### Fix Estrutural do RAG (CÃ©rebro Local de Busca)
*   **Commit:** `6d1d531 fix(rag): implementa busca multi-camada (exata + flexivel) no SQLite FTS4 para tolerÃ¢ncia a erros e maior precisao`
*   **Melhorias Implementadas:**
    *   SubstituiÃ§Ã£o da busca engessada FTS4 (`AND` total) por uma "Busca em Cascata" inteligente.
    *   TolerÃ¢ncia a Erros: Se o usuÃ¡rio digitar "descricoa", a busca flexÃ­vel garante o Ranqueamento atravÃ©s de outras palavras corretas ("PerÃ­cia", "Furtividade"), evitando a devoluÃ§Ã£o de resultados vazios e mitigando as temidas "alucinaÃ§Ãµes da IA".
* [Feito] Lote 62: ConfiguraÃ§Ã£o do Ambiente GraphRAG (D:\VSBuildTools) | `(Manual: Antigravity)`
    - InstalaÃ§Ã£o das ferramentas C++ Build Tools no drive D: para suporte a compilaÃ§Ã£o nativa.
* [Feito] Lote 63: Pivot TÃ©cnico para GraphRAG Lite (Zero-Native) | `(Commit: 8f45a91)`
    - ImplementaÃ§Ã£o de motor baseado em **ChromaDB + NetworkX** para evitar erros de DLL e compilador no Windows.
* [Feito] Lote 64: IntegraÃ§Ã£o Kotlin e DB VersÃ£o 12 (GraphRAG) | `(Commit: 8f45a91)`
    - MigraÃ§Ã£o do Banco de Dados SQLite para suporte Ã  tabela `graph_nodes` e busca hÃ­brida.
* [Feito] Lote 65: EstabilizaÃ§Ã£o de APIs e Chaves (OpenRouter/DeepSeek) | `(Commit: 8f45a91)`
    - RemoÃ§Ã£o de sufixos :free obsoletos e rotaÃ§Ã£o de credenciais ativas.
* [Feito] Lote 66: Auditoria de Dados e InjeÃ§Ã£o de Regras Mestre | `(Commit: 92b4cdd)`
    - Limpeza de 284 duplicatas no manual (Magia).
    - PadronizaÃ§Ã£o de referÃªncias: `[MB]`, `[MÃ¡g]`, `[AM]`.
    - InjeÃ§Ã£o de Tabelas Fundamentais (DistÃ¢ncia, MT, EscuridÃ£o, Cobertura) no Grafo.
* [Feito] Lote 67: UX de Elite - BotÃ£o de Copiar e HistÃ³rico Persistente | `(Commit: pendente)`
    - ImplementaÃ§Ã£o do LocalClipboardManager para cÃ³pia rÃ¡pida de respostas.
    - CriaÃ§Ã£o de tabelas `chat_sessions` e `chat_messages` no SQLite (V13).
    - Interface de seletor de histÃ³rico para recuperaÃ§Ã£o de conversas passadas.
* [Feito] Lote 68: Motor Investigador (ExpansÃ£o SemÃ¢ntica) | `(Commit: c2e2491)`
* [Feito] Lote 69: O Toque do Mestre (Contexto Adjacente) | `(Commit: ca18212)`
* [Feito] Lote 70: Motor ReAct Multi-Stage (O Mestre Investigador) | `(Commit: pendente)`


    - ImplementaÃ§Ã£o de Loop de InvestigaÃ§Ã£o (While) em `MestreIAUseCase.kt`.
    - Suporte a atÃ© 3 iteraÃ§Ãµes consecutivas de busca por resposta.
    - AcumulaÃ§Ã£o inteligente de contexto entre buscas para evitar perda de raciocÃ­nio.
* [Feito] Lote 81: RestauraÃ§Ã£o Nano-GraphRAG e MemÃ³ria de Agente | `(Commit: 51c0d3b)`
    - ImplementaÃ§Ã£o de Busca Relacional em Dois Saltos (Multi-Hop) no `MestreIAGraphEngine.kt`.
    - IntegraÃ§Ã£o com o sistema MemPalace (ChromaDB local) para memÃ³ria de longo prazo do agente.
    - CriaÃ§Ã£o do "PalÃ¡cio da MemÃ³ria" (Knowledge Item) com mapa detalhado de engenharia e regras SSOT.
    - ExpansÃ£o do contexto RAG com "Essential Nodes" (Atributos e Regras Base) e Radar de RAM.
    - RotaÃ§Ã£o de Chaves: Mapeamento de `MESTRE_IA_OPENROUTER_2_KEY` e `.3_KEY` no `MestreIAUseCase.kt` para garantir continuidade apÃ³s expiraÃ§Ã£o da chave anterior.

* [Feito] Lote 82: RefatoraÃ§Ã£o Mestre IA (Especialistas) | `(Commit: 86d7e4f)`
    - DivisÃ£o do UseCase em Auditor (Regras/RAG) e Forjador (GeraÃ§Ã£o/AnÃ¡lise).
    - RefatoraÃ§Ã£o do `MestreIAResponse` para aceitar objetos complexos em Vantagens/Desvantagens/Magias (CorreÃ§Ã£o Crash GSON).
    - ImplementaÃ§Ã£o de fallbacks especializados (Elite para Forja, Lite para Auditoria).
    - Limpeza de sintaxe e remoÃ§Ã£o de redundÃ¢ncias no `FichaIADelegate`.

* [Feito] Lote 83: Blindagem Ultra-Resiliente (Zero-Crash JSON) | `(Commit: f93da21)`
    - ImplementaÃ§Ã£o do `MestreIAItemDeserializer` com suporte a aliases (`nh`, `desc`, `id`).
    - Tratamento de exceÃ§Ãµes interno no parse para evitar interrupÃ§Ã£o do fluxo do app.
    - SincronizaÃ§Ã£o de motores GSON entre `Client`, `Delegate` e `Generator`.

* [Feito] Lote 84.5: CÃ©rebro Mestre (Regras de Ouro e InjeÃ§Ã£o de CatÃ¡logo TÃ©cnico) | `(Commit: Lote84.5)`
    - IA agora recebe lista oficial de nomes de Armas e Equipamentos.
    - Regra estrita: Magias exigem AptidÃ£o MÃ¡gica e inclusÃ£o de prÃ©-requisitos.
* [Feito] Lote 84.6: SeparaÃ§Ã£o Arquitetural (Prompts Isolados) | `(Commit: Lote84.6)`
    - Prompts movidos para `MestreIAPrompts.kt`, limpando o cliente de rede.
* [Feito] Lote 84.7: Alinhamento TÃ©cnico (NT, Dano PT-BR e RAG Expansivo) | `(Commit: Lote84.7)`
    - ProibiÃ§Ã£o de termos em inglÃªs (cut/pi) e exigÃªncia de sufixos /NT.
    - ExpansÃ£o automÃ¡tica do RAG para garantir catÃ¡logo tÃ©cnico em personagens novos.
* [Feito] Lote 84.8: ConsciÃªncia de App (IA entende a automaÃ§Ã£o do sistema) | `(Commit: Lote84.8)`
    - IA agora entende que o app automatiza cÃ¡lculos se os nomes estiverem corretos.

* [Feito] Lote 85: Mesa Virtual (Lote 1) - Console do Narrador e AutomaÃ§Ãµes | `(Commit: Lote85)`
    - CriaÃ§Ã£o do Console Web (`index.html`) com suporte a visualizaÃ§Ã£o de mÃºltiplas fichas JSON simultÃ¢neas.
    - ImplementaÃ§Ã£o de Calculadora de Dano Localizado com multiplicadores oficiais (CrÃ¢nio x4, Vitais x3) e limites de membros (PV/2 e PV/3).
    - AdiÃ§Ã£o de controles interativos de PV/PF e ajuste manual de bÃ´nus de RD por localizaÃ§Ã£o.
    - AutomaÃ§Ã£o de caracterÃ­sticas derivadas na interface: PER, VON, Velocidade, Deslocamento e Dano de ST (GdP/GeB).
    - PreparaÃ§Ã£o do App Android com `intent-filter` para o protocolo `gurpsapp://conectar`.

* [Feito] Lote 86: UnificaÃ§Ã£o de TraÃ§os e Busca Inteligente (Modificadores) | `(Commit: 5ce593b)`
    - ImplementaÃ§Ã£o de barra de busca no diÃ¡logo de modificadores (catÃ¡logo geral + especÃ­ficos).
    - UnificaÃ§Ã£o das interfaces de AdiÃ§Ã£o e EdiÃ§Ã£o de Vantagens e Desvantagens (Ficha e Modelo Racial).
    - CorreÃ§Ã£o da persistÃªncia de regras especiais (Aliados, Patronos, DependÃªncia, Inimigos, etc.).
    - Blindagem de cÃ¡lculo de custo para traÃ§os legados (fallback de `specialRule` via catÃ¡logo).
    - AtivaÃ§Ã£o de salvamento automÃ¡tico (`salvarFicha()`) apÃ³s qualquer ediÃ§Ã£o de traÃ§os.

[Feito] Lote 87: ExibiÃ§Ã£o de descriÃ§Ãµes de perÃ­cias/magias na Aba de Rolagem | `(Commit: c3e3859)`
* [Feito] Lote 88: Blindagem do Mestre IA (Rastro de Provas) | `(Manual: Antigravity)`
    - ImplementaÃ§Ã£o do protocolo de citaÃ§Ã£o obrigatÃ³ria `[Livro, PÃ¡g. X]`.
    - Bloqueio de memÃ³ria externa para evitar alucinaÃ§Ãµes de regras nÃ£o documentadas no CÃ³dex.
    - ExigÃªncia de uso de ferramentas de busca para dÃºvidas tÃ©cnicas.
    - AnÃ¡lise profunda do sistema e identificaÃ§Ã£o de 5 vulnerabilidades crÃ­ticas de alucinaÃ§Ã£o.
* [Feito] Lote 89: HigienizaÃ§Ã£o de Ativos (Assets Cleanup) | `(Manual: Antigravity)`
    - RemoÃ§Ã£o de banco de dados residual `chroma.sqlite3` (Legado Lote 63).
    - ExclusÃ£o de `catalogo_nomes_ia.json` obsoleto (substituÃ­do pelo GraphRAG dinÃ¢mico).
    - Faxina de arquivos JSON legados (`vantagens.v1`, `v2`, `magias.json`, etc.) para reduzir tamanho do APK.
    - RealocaÃ§Ã£o de scripts de prÃ©-processamento (`populate_graph.py`) para fora da pasta de assets do App.

* [Feito] Lote 98: PaginaÃ§Ã£o de Resultados (PÃ¡gina 2) e Calibragem de PrecisÃ£o (Peso de Ouro) | `(Commit: Lote98)`
    - ImplementaÃ§Ã£o de parÃ¢metro `pagina` na ferramenta de busca para evitar loops infinitos.
    - Sistema de Pesos: Termos originais da pergunta valem +10, sinÃ´nimos automÃ¡ticos valem +2.
    - BÃ´nus Massivo de TÃ­tulo (+35) para match exato com a dÃºvida do usuÃ¡rio.
    - Fim do problema de "Sangramento" ser enterrado por magias de cura ou outros termos genÃ©ricos.
* [Feito] Lote 100: ConsciÃªncia BibliogrÃ¡fica (Source-Aware RAG) e DicionÃ¡rio TÃ©cnico | `(Commit: Lote100)`
    - ImplementaÃ§Ã£o de Busca Filtrada: O sistema agora distingue entre livros diferentes que possuem o mesmo nÃºmero de pÃ¡gina, eliminando colisÃµes (ex: PÃ¡g 117 de Magia vs Artes Marciais).
    - DicionÃ¡rio TÃ©cnico Mestre: InjeÃ§Ã£o de sinÃ´nimos de alta fidelidade (ex: "Cavar" remete automaticamente a "EscavaÃ§Ã£o") para garantir que o motor de busca encontre a regra correta mesmo com linguagem comum.
    - Regex de PrecisÃ£o: Captura automÃ¡tica da fonte bibliogrÃ¡fica `[Livro]` a partir dos resumos do grafo para direcionar a carga de recortes manuais.
* [Feito] Lote 101: Motor RAG SemÃ¢ntico HÃ­brido & Anti-DiluiÃ§Ã£o (FTS4 Layering) | `(Commit: Lote101)`
    - DesativaÃ§Ã£o de "blindagem" (stopWords genÃ©ricas de RPG como 'ataque' e 'dano'), devolvendo a capacidade do motor de interpretar frases naturais cruas sem falhas.
    - ImplementaÃ§Ã£o de Busca por Camadas (Layering): Garantia matemÃ¡tica de que palavras raras (ex: 'piscina') nÃ£o sejam engolidas do limite do FTS4 por palavras comuns (ex: 'dano') atravÃ©s de iteraÃ§Ãµes individuais na base de recortes.
* [Feito] Lote 102: Algoritmo de Raridade (TF-IDF Proxy local em Kotlin) | `(Commit: Lote102)`
    - ImplementaÃ§Ã£o de heurÃ­stica inspirada no TF-IDF (Inverse Document Frequency) diretamente no re-ranking local.
    - CÃ¡lculo de peso por raridade: Palavras com muitos resultados no SQLite (peso 1) nÃ£o pontuam alto; palavras com poucos recortes exatos (peso atÃ© 50) geram multiplicadores explosivos (ex: 'Combate AquÃ¡tico' supera 100% 'Dano de Arma').
    - O motor agora filtra o ruÃ­do de perguntas longas atravÃ©s de matemÃ¡tica pura, sem depender de injeÃ§Ãµes rÃ­gidas.
* [Feito] Lote 103: RAG State-of-the-Art (RRF & Parent Document) | `(Commits: 6a059ea, a43a88e)`
    - **Parent Document Retrieval:** O motor agora busca a pÃ¡gina inteira em que o recorte se encontra, resolvendo perdas de contexto onde regras importantes ou tabelas continuavam no prÃ³ximo parÃ¡grafo. O limite do prompt saltou para 15000 chars.
    - **RRF (Reciprocal Rank Fusion):** Implementada a fÃ³rmula matemÃ¡tica padrÃ£o da indÃºstria `(1 / Rank + 60)` para fundir de forma justa o ranking de palavras-chave da busca textual (FTS) com as sugestÃµes de pÃ¡gina vindas do Knowledge Graph, gerando um Top 3 infalÃ­vel.

* [Feito] Lote 104: Filtro de RuÃ­do & Mega-Contexto (60k) | `(Commit: de125e6)`
    - **Filtro de RuÃ­do:** ExpansÃ£o de termos por sinÃ´nimos (gladiador, luta) agora afeta apenas o Grafo. A busca de texto bruto (Chunks) foca 100% nos termos reais do usuÃ¡rio para evitar poluiÃ§Ã£o.
    - **Janela de 60k Chars:** Limite da `PonteDeFerro` expandido de 15k para 60k caracteres. Isso permite enviar atÃ© 10 pÃ¡ginas completas (Documento Pai) sem cortes.
    - **Top 8 Retrieval:** Motor agora coleta as 8 melhores pÃ¡ginas encontradas, garantindo que regras especÃ­ficas entrem no prompt mesmo que nÃ£o sejam o Top 1 de score.

* [Feito] Lote 105: Diversidade de Elite & BÃ´nus de Autoridade | `(Commit: c8bcfe6)`
    - **Filtro Anti-MonopÃ³lio:** Implementada trava algorÃ­tmica que limita a 2 recortes por pÃ¡gina no Top 8. Isso obriga o motor a trazer diversidade de regras (ex: PÃ¡g 16 + PÃ¡g 430 + PÃ¡g 397) em vez de inundar o contexto com uma Ãºnica pÃ¡gina genÃ©rica.
    - **BÃ´nus de Grafo (5x):** PÃ¡ginas sugeridas pelo Knowledge Graph agora recebem um multiplicador de relevÃ¢ncia de 500%, garantindo que a inteligÃªncia estrutural prevaleÃ§a sobre a mera repetiÃ§Ã£o de palavras-chave.

* [Feito] Lote 106: Contexto Adjacente (PÃ¡gina Suporte) | `(Manual: Antigravity)`
    - **PÃ¡gina n+1:** O motor `MestreIAGraphEngine` agora recupera automaticamente a pÃ¡gina seguinte para cada pÃ¡gina de impacto encontrada, garantindo integridade de tabelas e fÃ³rmulas longas.
    - **ExpansÃ£o de Contexto:** Aumentado o limite de recortes finais de 10 para **20** para acomodar o suporte adjacente sem cortes.
    - **ValidaÃ§Ã£o de ColisÃ£o:** Confirmada a recuperaÃ§Ã£o da fÃ³rmula de dano (PÃ¡g 432) ao buscar por termos na PÃ¡g 431.

* [Feito] Lote 107: Blindagem de FTS4 (NormalizaÃ§Ã£o de Acentos) | `(Manual: Antigravity)`
    - **IndexaÃ§Ã£o Normalizada:** A coluna `search_text` agora Ã© povoada sem acentos e em lowercase, blindando o motor contra encoding corrompido (Mojibake).
    - **Busca AgnÃ³stica:** Os termos de busca sÃ£o normalizados antes da consulta, permitindo que "colisao" encontre "ColisÃ£o" e vice-versa.
    - **UnificaÃ§Ã£o de Scoring:** O re-ranking TF-IDF agora utiliza os mesmos termos normalizados, garantindo precisÃ£o matemÃ¡tica no Top 3.

* [Finalizado] Lote 108: Sincronia AutomÃ¡tica e Limpeza de Legado | `(Commit: 2e59eba)`
    - **RemoÃ§Ã£o de Gatilhos Manuais:** Extintos os comandos "forÃ§ar sincronizaÃ§Ã£o" via chat.
    - **Sincronia Inteligente:** Implementado **Mutex** no `MestreIARepository` para garantir carga Ãºnica e atÃ´mica.
    - **Performance & Background:** UseCase migrado para `Dispatchers.IO`, eliminando lag na UI.
    - **Encoding UTF-8 (Fim do Mojibake):** ForÃ§ada leitura de assets em UTF-8, corrigindo acentos corrompidos.
    - **v19 do Banco:** Incrementada versÃ£o do DB para forÃ§ar reset limpo dos Ã­ndices.

* [Finalizado] Lote 109: PurificaÃ§Ã£o Arquitetural | `(Commit: 2e59eba)`
    - **Isolamento de RepositÃ³rio:** Criado o `MestreIARepository` para separar a lÃ³gica de regras da lÃ³gica de ficha (`DataRepository`).
    - **DelegaÃ§Ã£o Limpa:** O `DataRepository` agora apenas delega as chamadas de busca, reduzindo seu tamanho e complexidade.
    - **Estabilidade de Testes:** Ajustados Stubs e inicializaÃ§Ã£o `lazy` para permitir testes unitÃ¡rios sem dependÃªncia de Contexto.

* [Finalizado] Lote 111: OtimizaÃ§Ã£o de RAG e Cura de Contexto (Dano por ColisÃ£o) | `(Commit: ce531eb)`
    - **DiagnÃ³stico de Carga:** RemoÃ§Ã£o de limitaÃ§Ãµes de cÃ³digo que truncavam regras vitais.
    - **Prioridade VIP:** BÃ´nus de score +1000 para pÃ¡ginas recomendadas pelo Grafo (Garante PÃ¡g 433).
    - **Abertura de Gargalo:** Entrega de atÃ© 25 recortes de contexto ao Gemini no CaseUse.
    - **ValidaÃ§Ã£o de Dados:** VerificaÃ§Ã£o da integridade da regra de ColisÃ£o no banco SQLite.

* [Finalizado] Lote 112: Motor de RaciocÃ­nio e Hierarquia (Plan Systemic Evolution) | `(Commit: Lote112)`
    - **Janela Deslizante (N-1, N, N+1):** RecuperaÃ§Ã£o automÃ¡tica da pÃ¡gina anterior para integridade de regras.
    - **Hierarquia de Autoridade:** BÃ´nus (+50) para o MÃ³dulo BÃ¡sico, garantindo soberania da "Lei MÃ£e".
    - **Prompt de RaciocÃ­nio (Pilares):** IA agora decompÃµe problemas em AÃ§Ã£o, Atributo, Ambiente e Estado.
    - **PurificaÃ§Ã£o do Grafo:** InjeÃ§Ã£o de source_id em todos os 2476 nÃ³s e unificaÃ§Ã£o de NÃ³s Mestres (Ataque Total).

* [Finalizado] Lote 112.1: CorreÃ§Ã£o do Gargalo de Regex no RAG | `(Commit: 6dc456c)`
    - **DiagnÃ³stico:** O Grafo corretamente identificava mÃºltiplas pÃ¡ginas (ex: [PÃ¡g. 353, 354, 388] para Terreno), mas a Regex capturava apenas a primeira.
    - **CorreÃ§Ã£o MatemÃ¡tica:** SubstituiÃ§Ã£o de `Regex.find()` por `Regex.findAll()` no `MestreIAGraphEngine.kt`, iterando sobre todas as ocorrÃªncias de pÃ¡ginas no resumo.
    - **Resultado PrÃ¡tico:** A "Ponte de PÃ¡gina" agora enfileira todas as pÃ¡ginas listadas (353, 354 e 388), permitindo que a IA aplique a regra matemÃ¡tica de Lama no combate.

* [Finalizado] Lote 112.2: RRF Rank Normalization (Reciprocal Rank Fusion) | `(Commit: Pending)`
    - **DiagnÃ³stico:** O Algoritmo de Ranking Lexical RRF penalizava as pÃ¡ginas extras do Grafo. Se o Grafo apontava 3 pÃ¡ginas, a segunda e terceira ganhavam pontuaÃ§Ã£o decrescente, impedindo regras secundÃ¡rias (Lama) de chegarem ao Top 8.
    - **CorreÃ§Ã£o:** Alterado o `graphRank` para tratar **todas** as pÃ¡ginas apontadas pelo Grafo com pontuaÃ§Ã£o absoluta (Rank = 1). A responsabilidade do desempate Ã© agora puramente lexical.

* [Finalizado] Lote 112.3: CorreÃ§Ã£o do Anti-MonopÃ³lio e Autenticidade de Fonte BibliogrÃ¡fica | `(Commit: Pending)`
    - **DiagnÃ³stico de Magias:** Consultas como "Magia Desejo" recuperavam o nÃ³ correto, mas a pÃ¡gina (PÃ¡g 61) era carregada de 3 livros diferentes simultaneamente (MÃ³dulo BÃ¡sico, Artes Marciais e Magia). O filtro "Anti-MonopÃ³lio" as considerava idÃªnticas (PÃ¡g 61) e cortava o livro Magia (por ter prioridade menor no desempate), ocultando a regra real. A lista de *stop words* tambÃ©m estava bloqueando palavras tÃ©cnicas vitais (ex: "prÃ©", "requisitos").
    - **RefatoraÃ§Ã£o da Ponte de PÃ¡gina (`PaginaAlvo`):** Implementada amarraÃ§Ã£o com `sourceId` nos metadados da base. Agora, o sistema exige que a pÃ¡gina 61 do Grafo corresponda exclusivamente Ã  PÃ¡g 61 do suplemento correto (Magia).
    - **BÃ´nus Lexical Especializado:** Adicionado BÃ´nus +60 Lexical se os "Termos Base" ou o "NÃ³ do Grafo" pertencerem ao grupo semÃ¢ntico de "Magia" (pt_gurps_magia) ou "Artes Marciais" (pt_artes_marciais), superando artificialmente o bÃ´nus de Autoridade do MÃ³dulo BÃ¡sico para buscas ultra-especializadas.
    - **Stop Words:** Removidas palavras cruciais como "requisitos" e "pre" do limpador lexical no `extrairPalavrasChave`.

* [Feito] Lote 113: IntegraÃ§Ã£o Nexus-IA (O Consultor Arcano) | `(Commit: c4a1b2d)`
* [Feito] Lote 114: Blindagem de Conectividade & Contexto Arcano | `(Commit: 9cb6448)`
    - **Fix Conectividade:** ResoluÃ§Ã£o do Erro 400 atravÃ©s da segregaÃ§Ã£o de URLs e Chaves por provedor (Gemini vs DeepSeek).
    - **Enriquecimento Arcano:** InjeÃ§Ã£o automÃ¡tica de Escolas e PrÃ©-requisitos no contexto de magias para o Mestre IA.
    - **EstabilizaÃ§Ã£o de Build:** CorreÃ§Ã£o de inferÃªncia de tipos no ranking RRF do GraphEngine.
    - **ConexÃ£o de Motores:** ImplementaÃ§Ã£o da ferramenta nativa `consultar_nexus_arcano` no Mestre IA, permitindo que a IA invoque o Motor Nexus em milissegundos.
    - **Gabarito TÃ©cnico:** O motor agora gera um "Gabarito de Ouro" determinÃ­stico (Estado Zero) com Ã¡rvore de dependÃªncias completa e sugestÃµes para metas de escolas.
    - **Fidelidade BibliogrÃ¡fica:** CorreÃ§Ã£o de dados no `magias2versao.json` (Sopro de Fogo/Ãcido/Frio) alinhando "ResistÃªncia" com o MÃ³dulo BÃ¡sico.
    - **IndependÃªncia de Ficha:** A ferramenta funciona de forma isolada, permitindo planejar magias mesmo sem uma ficha ativa ou iniciada.

* [Feito] Lote 115: Consolidação de Arquitetura e Auditoria Subaquática | `(Commit: 8faf742)`
* [Feito] Lote 116: Auditoria e Restauração do Mestre IA | `(Commit: d403148)`

### Lote 116: Auditoria e Restauração do Mestre IA
*   **Commit:** `d403148 Diagnostico e Restauracao: Reversao de mudancas antecipadas e consolidacao da logica de busca do Codex.`
*   **Melhorias Implementadas:**
    *   **Reversão Sistêmica:** Restauração do projeto ao estado original estável após diagnóstico de falhas em mudanças antecipadas.
    *   **Diagnóstico de Precisão:** Identificado que o limite de 5 itens no catálogo estava deletando regras vitais (como Combate Subaquático) em favor de detalhes menos relevantes.
    *   **Diagnóstico de Carga:** Confirmada a necessidade de um carregador multi-fonte para incluir o suplemento Pyramid #3/26 na base de Chunks.
    *   **Otimização de Performance:** Consolidação de melhorias internas no motor de busca (Map para scores e dicionário técnico refinado) que foram mantidas por serem seguras e necessárias.
    - **MestreIAClient.kt**: Identificada arquitetura leve baseada em `HttpURLConnection` (sem Retrofit).
    - **ARQUITETURA_MESTRE_IA.md**: Criada documentação completa do ecossistema da IA para manutenção futura.
    - **Auditoria de Regras**: Diagnosticada falha de busca no Grafo por falta de sinônimos/tags nos nós (Match de Prefixo).
    - **graph_knowledge.json**: Regra de combate subaquático (Pyramid #3/26) estabilizada e validada.


**[Bateria de Testes a Realizar]**
- Bateria de Testes (Stress Test)
Impacto em Alta Velocidade: "Um cavaleiro em carga a cavalo (Move 8) atinge um soldado com uma lança. Como calculo o dano de colisão baseado na ST do cavalo?"
Regras de Afogamento: "Meu personagem caiu em um rio e está sem fôlego. Quanto tempo ele aguenta antes de começar a perder PV e quais são os testes de HT?"
Visibilidade Crítica: "Estou tentando atirar em um alvo na escuridão total, mas tenho 'Visão Noturna 5'. Qual a minha penalidade final?"
Equipamentos e Carga: "Estou carregando 40kg de ouro. Minha ST é 10. Como isso afeta minha Esquiva e meu Deslocamento atual?"
Aparar com Escudo: "Um ogro me atacou com uma clava gigante. Posso usar a regra de 'Aparar com o Escudo' ou sou obrigado a Bloquear?"
Criação de Especialista: "Gere uma ficha de um Ninja especializado em infiltração tecnológica (NT 9), com 'Mãos Pegajosas' e 'Passo Leve', usando 150 pontos."
Regra de Recuo (Armas de Fogo): "Se eu der uma rajada de 3 tiros com uma submetralhadora de Recuo 2, como calculo quantos tiros acertaram?"


