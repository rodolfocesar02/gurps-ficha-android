# Acompanhamento do Projeto da Ficha GURPS (Para Rodolfo)

**ÃƒÅ¡ltima AtualizaÃƒÂ§ÃƒÂ£o:** 28 de Abril de 2026
**Status Atual:** ERA DO RACIOCÃƒï¿½NIO - Conectividade Estabilizada Ã°Å¸Å’ï¿½Ã°Å¸â€ºÂ¡Ã¯Â¸ï¿½ | Lote 114 CONCLUÃƒï¿½DO Ã°Å¸Å¡â‚¬

### Sincro V24: Super Release 2.0 (Lote 86)
- **LanÃƒÂ§amento Oficial V1.5.0**: Build de produÃƒÂ§ÃƒÂ£o gerada para as variantes Visual e PraCego.
- **UnificaÃƒÂ§ÃƒÂ£o de TraÃƒÂ§os e Busca Inteligente**: FinalizaÃƒÂ§ÃƒÂ£o do Lote 86 com todas as melhorias de interface e blindagem de cÃƒÂ¡lculo integradas.
- **PreparaÃƒÂ§ÃƒÂ£o de Update**: Arquivo `update.json` atualizado para notificar os usuÃƒÂ¡rios sobre a nova versÃƒÂ£o. Ã°Å¸â€�â€�Ã°Å¸â€ºÂ¡Ã¯Â¸ï¿½

### Sincro V23: Mapeamento Arquitetural e Blindagem de Conhecimento
- **Mapa Detalhado de Engenharia**: Criado o `MAPA_DETALHADO.md` com o inventÃƒÂ¡rio completo de funÃƒÂ§ÃƒÂµes, motores de RPG, scripts e suÃƒÂ­tes de teste. Ã°Å¸â€”ÂºÃ¯Â¸ï¿½Ã°Å¸â€�ï¿½
- **EstabilizaÃƒÂ§ÃƒÂ£o de Arquitetura**: VerificaÃƒÂ§ÃƒÂ£o e confirmaÃƒÂ§ÃƒÂ£o da integridade do cÃƒÂ³digo pÃƒÂ³s-sincronia, garantindo paridade total entre o ambiente de desenvolvimento e o dispositivo funcional do usuÃƒÂ¡rio. Ã°Å¸â€ºÂ¡Ã¯Â¸ï¿½Ã¢Å“Â¨
- **Nexus Arcano & Qualidade**: Mapeamento explÃƒÂ­cito das ferramentas de auditoria e testes automatizados para prevenir regressÃƒÂµes em futuras intervenÃƒÂ§ÃƒÂµes de IA. Ã°Å¸Â§ÂªÃ°Å¸Å½Â¯

### Sincro V22: Blindagem de Dados (V1.4.5 - Final)
- **Trava de Auto-Save**: Implementada barreira de proteÃƒÂ§ÃƒÂ£o que bloqueia o salvamento automÃƒÂ¡tico durante o carregamento de fichas, prevenindo a sobrescrita acidental de arquivos com dados vazios. 
- **Feedback de Carga**: Sistema de NotificaÃƒÂ§ÃƒÂ£o atualizado para reportar erros reais de desserializaÃƒÂ§ÃƒÂ£o, eliminando falsos positivos de "Ficha Carregada". 
- **Rebuild V22**: APKs reconstruÃƒÂ­dos com logs de diagnÃƒÂ³stico para rastrear falhas remanescentes em fichas corrompidas. 

### Lote 101: O Retorno ao CÃƒÂ³dex (PrecisÃƒÂ£o Literal) - CONCLUÃƒï¿½DO
- **Motor DeterminÃƒÂ­stico**: InversÃƒÂ£o de prioridade (Manual-First) com bÃƒÂ´nus de 100 pontos para termos literais. Ã°Å¸Å½Â¯
- **Fim do Chute**: Implementada a "PrisÃƒÂ£o de Contexto" no Auditor. Se nÃƒÂ£o estÃƒÂ¡ no livro, a IA nÃƒÂ£o inventa. Ã°Å¸â€ºÂ¡Ã¯Â¸ï¿½
- **Sincro AutomÃƒÂ¡tica**: SincronizaÃƒÂ§ÃƒÂ£o de manuais agora ocorre sozinha ao abrir o Mestre IA, garantindo paridade total. Ã¢Å¡â„¢Ã¯Â¸ï¿½
- **CorreÃƒÂ§ÃƒÂ£o Mojibake**: Limpeza de sÃƒÂ­mbolos tÃƒÂ©cnicos (mÃ‚Â³, Ãƒâ€”) no banco de dados. Ã°Å¸Â§Â¹
- **BÃƒÂ´nus de Tabela**: Motor calibrado para priorizar tabelas tÃƒÂ©cnicas sobre descriÃƒÂ§ÃƒÂµes de vantagens. Ã°Å¸â€œÅ 

---

## Lembretes Fixos do Seu Projeto

### 1. Ferramentas AcessÃƒÂ­veis
NÃƒÂ³s sempre cuidamos para que toda versÃƒÂ£o lanÃƒÂ§ada tenha a versÃƒÂ£o **Visual** (Normal) e a versÃƒÂ£o **PraCego** (Com botÃƒÂ£o e navegaÃƒÂ§ÃƒÂ£o de acessibilidade para cegas por meio do programa TalkBack de celular). O emulador costuma focar na visual para seu teste rÃƒÂ¡pido.

### 2. ConsistÃƒÂªncia de Dados
Os dados das magias, vantagens e perÃƒÂ­cias moram em arquivos de texto (tipo planilhas, chamados de arquivos **JSON**). IAs que forem alterar algo lÃƒÂ¡ nÃƒÂ£o podem apagar aspas ou colchetes sem cuidado.

---

##  Registro de Lotes e Commits (Rede de SeguranÃƒÂ§a)
*Todo Agente ÃƒÂ© obrigado a quebrar tarefas maiores em "Lotes Curtos" isolados de um arquivo por vez, efetuando o Commit no final para gerar um Ponto de Retorno seguro para o usuÃƒÂ¡rio. Cada nova "Aba" ganha tambÃƒÂ©m sua prÃƒÂ³pria pasta.*

> Lista de Lotes Realizados a partir de Abril de 2026:

* [Feito] Lote 1.2: ExtraÃƒÂ§ÃƒÂ£o de Loaders Json (DataRepository)       | `(Commit: 7c52e26)`
* [Feito] Lote 2.1: SeparaÃƒÂ§ÃƒÂ£o de PeÃƒÂ§as do Nexus (Modelos)           | `(Commit: a6992f1)`
* [Feito] Lote 2.2: O CÃƒÂ©rebro A* (Planejador de Caminho)            | `(Commit: 113b540)`
* [Feito] Lote 2.3: O Motor de DiagnÃƒÂ³stico (Raio-X)                 | `(Commit: 105949a)`
* [Feito] Lote 2.4: Limpeza final (Helpers & Parser)                | `(Commit: aab9ff2)`
* [Feito] Lote 3: ModularizaÃƒÂ§ÃƒÂ£o do FichaViewModel                   | `(Commit: 20414fd)`
* [Feito] Lote 4: PadronizaÃƒÂ§ÃƒÂ£o UTF-8 e Motor Modo Alvo              | `(Commit: 0912746)`
* [Feito] Lote 6: ModularizaÃƒÂ§ÃƒÂ£o do TraitDialogs                     | `(Commit: 3d186e9)`
* [Feito] Lote 6.1: CorreÃƒÂ§ÃƒÂ£o de SeleÃƒÂ§ÃƒÂ£o e Regras Especiais          | `(Commit: 0e9aee5)`
* [Feito] Lote 7: RefatoraÃƒÂ§ÃƒÂ£o da UI de Rolagem (TabRolagem.kt)      | `(Commit: 042cd2d)`
* [Feito] Lote 7.1: CorreÃƒÂ§ÃƒÂµes de Discord, UI de Ataque e Mojibake   | `(Commit: 6c3f773)`
* [Feito] Lote 7.2: RefatoraÃƒÂ§ÃƒÂ£o do RolagemDialogs.kt (FragmentaÃƒÂ§ÃƒÂ£o) | `(Commit: 6c8a800c)`
* [Feito] Lote 8: AtualizaÃƒÂ§ÃƒÂ£o EstÃƒÂ©tica dos Ãƒï¿½cones das Abas          | `(Commit: 1cec435)`
* [Feito] Lote 9: Interface de NavegaÃƒÂ§ÃƒÂ£o RPGÃƒÂ­stica (Ultra-Premium)  | `(Commit: 9f13f43)`
* [Feito] Lote 10: Melhoria do Ataque Inato e Skill Project Map     | `(Commit: e1c1a1b)`
* [Feito] Lote 11: ModernizaÃƒÂ§ÃƒÂ£o da UI do Mestre IA (ChatGPT)        | `(Commit: e42cce6)`
* [Feito] Lote 12: Card de dano adaptativo e soma automÃƒÂ¡tica de ST. | `(Commit: 783c295)`
* [Feito] Lote 13: Fluxo de Mestre IA com confirmaÃƒÂ§ÃƒÂ£o e anÃƒÂ¡lise consultiva. | `(Commit: a104632)`
* [Feito] Lote 14: Mestre IA Interativo (Antigravity-style) com botÃƒÂµes de aÃƒÂ§ÃƒÂ£o e tom inquisitivo. | `(Commit: a104632)`
* [Feito] Lote 15: RestauraÃƒÂ§ÃƒÂ£o das Defesas Ativas na Aba de Rolagem | `(Commit: 5c7c369)`
* [Feito] Lote 16: ModularizaÃƒÂ§ÃƒÂ£o da FichaViewModel e ProtÃƒÂ³tipo de SugestÃƒÂµes ClicÃƒÂ¡veis (Mestre IA) | `(Commit: c279b87)`
* [Feito] Lote 17: AutomaÃƒÂ§ÃƒÂ£o da vantagem Golpeadores (Strikers) | `(Commit: 65ec4ef)`
* [Feito] Lote 18: InteligÃƒÂªncia de NH e Aparar para Golpeadores/Ataque Inato | `(Commit: 687b8d9)`
* [Feito] Lote 19: Arquitetura Modular de Vantagens e DocumentaÃƒÂ§ÃƒÂ£o de IA | `(Commit: 6132f9b)`
* [Feito] Lote 20: AutomaÃƒÂ§ÃƒÂ£o da Vantagem Dentes (GdP-1 e Tipos) | `(Commit: 5fd613d)`
* [Feito] Lote 21: PersistÃƒÂªncia de SeleÃƒÂ§ÃƒÂ£o de Ataque/Dano na SessÃƒÂ£o | `(Commit: 217eaf4)`
* [Feito] Lote 22: Ajuste Vantagem Dentes e Nomeclatura de Dano PT-BR | `(Commit: ada9efd)`
* [Feito] Lote 23: Limpeza de Unicode Mojibake (\uXXXX) no CÃƒÂ³digo Fonte | `(Commit: 33c771f)`
* [Feito] Lote 24: AutomaÃƒÂ§ÃƒÂ£o de Garras, Cascos e Flexibilidade (+3 PerÃƒÂ­cias) | `(Commit: d4a5e2f)`
* [Feito] Lote 25: Bloqueio e Esquiva Ampliada e EstabilizaÃƒÂ§ÃƒÂ£o de Build        | `(Commit: f202c06)`

* [Feito] Lote 27: Redesign Visual das Defesas (Cards Individuais e BotÃƒÂµes)    | `(Commit: a9b2c3d)`
* [Feito] Lote 28: RefatoraÃƒÂ§ÃƒÂ£o da TabRolagem.kt (ExtraÃƒÂ§ÃƒÂ£o de DiÃƒÂ¡logos)         | `(Commit: de16e6f)`
* [Feito] Lote 29: OtimizaÃƒÂ§ÃƒÂ£o de EspaÃƒÂ§o e Padding (BotÃƒÂµes 2dp)             | `(Commit: b3f2d1e)`
* [Feito] Lote 30: Integrar BD do Escudo na Esquiva/Apara + Notas de UI      | `(Commit: 249874d)`
* [Feito] Lote 31: AutomaÃƒÂ§ÃƒÂ£o da Vantagem Mestre de Armas (Dano NH vs DX)      | `(Commit: c5a16f0)`
* [Feito] Lote 32: CategorizaÃƒÂ§ÃƒÂ£o e Filtragem Estrita do Mestre de Armas     | `(Commit: e8f9a2c)`
* [Feito] Lote 33: Mapeamento Estrito e CorreÃƒÂ§ÃƒÂ£o de Vazamento (Mestre de Armas) | `(Manual: Antigravity)`
* [Feito] Lote 34: RestauraÃƒÂ§ÃƒÂ£o da Apara/Bloqueio e IDs de PerÃƒÂ­cia Sublinhados | `(Commit: f3c3e9a)`
* [Feito] Lote 35: Visibilidade Permanente de Defesas (Fallback AutomÃƒÂ¡tico) | `(Commit: 2d1948b)`
* [Feito] Lote 36: VÃƒÂ­nculo Estrito do Dano com o Ataque Selecionado (Final) | `(Commit: 194f5d1)`
* [Feito] Lote 37: Acessibilidade Ultra na Aba de Rolagem (TalkBack) | `(Commit: 51dbd9f)`
* [Feito] Lote 38: AtualizaÃƒÂ§ÃƒÂ£o do Mapa do Projeto (100% PrecisÃƒÂ£o) | `(Commit: 53b1f89)`
* [Feito] Lote 39: AtualizaÃƒÂ§ÃƒÂ£o da Skill de Vantagens (TraitRule API) | `(Commit: d5768dd)`
* [Feito] Lote 40: AtualizaÃƒÂ§ÃƒÂ£o das Regras Operacionais (Commits e PraCego) | `(Commit: 4f63b8a)`
* [Feito] Lote 41: AtualizaÃƒÂ§ÃƒÂ£o das Regras de RPG GURPS (Combat Context) | `(Commit: d26daad)`
* [Feito] Lote 45: Restauracao do Aro Vermelho e Filtro de Invisibilidade (Sincro V16) | `(Commit: f604490)`
* [Feito] Lote 46: Pente Fino de Acessibilidade (TalkBack) em Pre-requisitos (Sincro V17) | `(Commit: bab709c)`
* [Feito] Lote 47: SincronizaÃƒÂ§ÃƒÂ£o em Nuvem InvisÃƒÂ­vel (Railway + DeviceID) (Sincro V18) | `(Manual: Antigravity)`
* [Feito] Lote 48: Biblioteca Unificada e ProteÃƒÂ§ÃƒÂ£o contra Conflitos de Nomes (Sincro V19) | `(Manual: Antigravity)`
* [Feito] Lote 49: RestauraÃƒÂ§ÃƒÂ£o SistÃƒÂªmica e UnificaÃƒÂ§ÃƒÂ£o de Branches (Integridade Total) | `(Commit: 7b346b1)`
* [Feito] Lote 50: Motor Nexus Arcano Estabilizado (ResoluÃƒÂ§ÃƒÂ£o Desejo + Metas Incrementais) | `(Commit: a2e2820)`
* [Feito] Lote 51: Mestre IA PRIME  - Soberania Multi-Flavor (Gemini/DeepSeek) + SeguranÃƒÂ§a Sete Chaves (Vault) + Rastreabilidade TÃƒÂ©cnica | `(Commit: babc20a)`
* [Feito] Sincronia de Roadmap: Roadmap do Mestre IA atualizado com novos lotes | `(Commit: d63158d)`
* [Feito] Lote 52: Robustez no Parsing de JSON (Auto-Healing) | `(Commit: f59f90b)`
(Problema: O uso de Regex para capturar JSON no MestreIAClient ÃƒÂ© eficiente, mas frÃƒÂ¡gil se a IA enviar um JSON malformado ou truncado.
Melhoria: Implementar um "JSON Repair" (como uma limpeza agressiva de caracteres de controle) e validar a estrutura contra o MestreIAResponse usando KotlinX.Serialization antes de chegar ao UseCase. Se o JSON falhar, o sistema deve pedir automaticamente uma re-formataÃƒÂ§ÃƒÂ£o para a IA (Auto-Healing).)
* [Feito] Lote 53: Contexto Diferencial (Token Economy) | `(Commit: fbdb0da)`
.(Problema: Enviar o personagem inteiro em cada mensagem gasta muitos tokens e pode confundir a IA com dados irrelevantes.
Melhoria: No processarPrompt, implementar uma lÃƒÂ³gica que identifique o que mudou na ficha ou o que ÃƒÂ© relevante para a pergunta atual. Se o usuÃƒÂ¡rio pergunta sobre "Dano", nÃƒÂ£o precisamos enviar a lista de "Equipamento de Camping".)
* [Feito] Lote 54: Streaming de Resposta (SSE/UX) | `(Commit: 89ab389)`
(Problema: Esperar a resposta completa da API pode gerar um "atraso" perceptÃƒÂ­vel na UI (LatÃƒÂªncia).
Melhoria: Se as APIs (OpenRouter/Gemini) suportarem, implementar Server-Sent Events (SSE). Ver o Mestre IA "escrevendo" em tempo real melhora drasticamente a percepÃƒÂ§ÃƒÂ£o de performance.)
* [Feito] Lote 55: Auditoria de Regras (Fiscal Ativo) | `(Commit: fc1f47c)`
* [Feito] Lote 56: Local-First RAG (Busca Vetorial) | `(Commit: c110272)`.(Problema: O buscador semÃƒÂ¢ntico no MestreIARagEngine pode ser pesado para buscas em muitos arquivos JSON.
Melhoria: Avaliar o uso de uma pequena biblioteca de busca vetorial local (ou um index prÃƒÂ©-calculado) para que o findRelevantChunks seja instantÃƒÂ¢neo, mesmo com manuais extensos.)
* [Feito] Lote 57: ReconstruÃƒÂ§ÃƒÂ£o de Elite (RAG 1194 Chunks) e Triplo Fallback (600 Usos) | `(Manual: Antigravity)`
* [Feito] Lote 58: EstabilizaÃƒÂ§ÃƒÂ£o do Mestre IA (Motor de Reparo por Pilha) | `(Commit: bf9b73c)`
    - [x] **EstabilizaÃƒÂ§ÃƒÂ£o IA 2026**: MigraÃƒÂ§ÃƒÂ£o total para Gemini 3.0 e 2.5 (resolvendo 404 de modelos antigos).
    - [x] **RestauraÃƒÂ§ÃƒÂ£o Nexus**: Motor de investigaÃƒÂ§ÃƒÂ£o multi-estÃƒÂ¡gio e trilhas de prÃƒÂ©-requisitos universalizado.
    - [x] **Blindagem de ConexÃƒÂ£o**: CorreÃƒÂ§ÃƒÂ£o de headers OpenRouter e normalizaÃƒÂ§ÃƒÂ£o de histÃƒÂ³rico de chat.
    * ImplementaÃƒÂ§ÃƒÂ£o de algoritmo de fechamento de JSON baseado em Pilha (Stack) e BotÃƒÂµes de DiagnÃƒÂ³stico UI.
* [Feito] Lote 59: IA Master Laboratory (Suite de Auditoria Python) | `(Commit: bf9b73c)`
    * CriaÃƒÂ§ÃƒÂ£o do validador de fidelidade ao catÃƒÂ¡logo e simulador de stress offline em Python.
* [Feito] Lote 60: Stress Test do Motor de Pilha e Sync do Gabarito de Ouro | `(Manual: Antigravity)`



    * RefatoraÃƒÂ§ÃƒÂ£o do `MestreIAResponse` para aceitar Objetos em vez de Strings para Vantagens e Magias (alinhado com o Gabarito de Ouro).
    * EvoluÃƒÂ§ÃƒÂ£o do `repararJsonTruncado` para fechar Strings `"` abertas e ignorar sufixos nocivos.
    * Teste UnitÃƒÂ¡rio criado: `testStressReparoJsonTruncado` com aninhamento de 500pts aprovado.

### Lote 60: DefiniÃƒÂ§ÃƒÂ£o de Schemas Nativos (Tool Builder)
*   **Commit:** `d025cc1 feat(ia): Lote 60 - define schemas nativos de Function Calling para Gemini e OpenAI`
*   **Melhorias Implementadas:**
    *   CriaÃƒÂ§ÃƒÂ£o da classe `MestreIATools.kt` para orquestrar as ferramentas.
    *   DefiniÃƒÂ§ÃƒÂ£o rigorosa de Schemas JSON (`fill_character_sheet` e `search_rules`) que forÃƒÂ§am a IA a obedecer o layout da ficha.
    *   Fim da Engenharia de Prompt (Regex) para requisiÃƒÂ§ÃƒÂ£o de criaÃƒÂ§ÃƒÂ£o de fichas.

### Lote 61: O Motor ReAct (Orquestrador AssÃƒÂ­ncrono)
*   **Commit:** `a353efe feat(ia): Lote 61 - Orquestrador ReAct intercepta Tool Calls para preenchimento de ficha sem regex`
*   **Commit:** `161fb58 feat(ia): implementa ReAct loop real no MestreIAUseCase para pesquisa de regras e ativa criacao em todos os modos`
*   **Melhorias Implementadas:**
    *   RefatoraÃƒÂ§ÃƒÂ£o profunda em `MestreIAClient` e `MestreIAUseCase` para capturar chamadas assÃƒÂ­ncronas no protocolo SSE.
    *   InterceptaÃƒÂ§ÃƒÂ£o da ferramenta nativa `fill_character_sheet`.
    *   Passagem direta do JSON estrito e perfeito para a `FichaIADelegate`, eliminando completamente os riscos de JSON malformado e ativando instantaneamente o botÃƒÂ£o de IntegraÃƒÂ§ÃƒÂ£o.
    *   **Loop ReAct Verdadeiro:** Implementada a recursÃƒÂ£o nativa. Se a IA solicitar `search_rules`, o sistema intercepta, busca as regras via RAG, e faz uma chamada recursiva devolvendo o texto para a IA automaticamente para que ela conclua sua tarefa.
    *   Desbloqueio de Ferramentas: `fill_character_sheet` agora estÃƒÂ¡ sempre disponÃƒÂ­vel, mesmo no modo PadrÃƒÂ£o/Conversa, permitindo que a IA forje fichas em qualquer cenÃƒÂ¡rio.

### Fix Estrutural do RAG (CÃƒÂ©rebro Local de Busca)
*   **Commit:** `6d1d531 fix(rag): implementa busca multi-camada (exata + flexivel) no SQLite FTS4 para tolerÃƒÂ¢ncia a erros e maior precisao`
*   **Melhorias Implementadas:**
    *   SubstituiÃƒÂ§ÃƒÂ£o da busca engessada FTS4 (`AND` total) por uma "Busca em Cascata" inteligente.
    *   TolerÃƒÂ¢ncia a Erros: Se o usuÃƒÂ¡rio digitar "descricoa", a busca flexÃƒÂ­vel garante o Ranqueamento atravÃƒÂ©s de outras palavras corretas ("PerÃƒÂ­cia", "Furtividade"), evitando a devoluÃƒÂ§ÃƒÂ£o de resultados vazios e mitigando as temidas "alucinaÃƒÂ§ÃƒÂµes da IA".
* [Feito] Lote 62: ConfiguraÃƒÂ§ÃƒÂ£o do Ambiente GraphRAG (D:\VSBuildTools) | `(Manual: Antigravity)`
    - InstalaÃƒÂ§ÃƒÂ£o das ferramentas C++ Build Tools no drive D: para suporte a compilaÃƒÂ§ÃƒÂ£o nativa.
* [Feito] Lote 63: Pivot TÃƒÂ©cnico para GraphRAG Lite (Zero-Native) | `(Commit: 8f45a91)`
    - ImplementaÃƒÂ§ÃƒÂ£o de motor baseado em **ChromaDB + NetworkX** para evitar erros de DLL e compilador no Windows.
* [Feito] Lote 64: IntegraÃƒÂ§ÃƒÂ£o Kotlin e DB VersÃƒÂ£o 12 (GraphRAG) | `(Commit: 8f45a91)`
    - MigraÃƒÂ§ÃƒÂ£o do Banco de Dados SQLite para suporte ÃƒÂ  tabela `graph_nodes` e busca hÃƒÂ­brida.
* [Feito] Lote 65: EstabilizaÃƒÂ§ÃƒÂ£o de APIs e Chaves (OpenRouter/DeepSeek) | `(Commit: 8f45a91)`
    - RemoÃƒÂ§ÃƒÂ£o de sufixos :free obsoletos e rotaÃƒÂ§ÃƒÂ£o de credenciais ativas.
* [Feito] Lote 66: Auditoria de Dados e InjeÃƒÂ§ÃƒÂ£o de Regras Mestre | `(Commit: 92b4cdd)`
    - Limpeza de 284 duplicatas no manual (Magia).
    - PadronizaÃƒÂ§ÃƒÂ£o de referÃƒÂªncias: `[MB]`, `[MÃƒÂ¡g]`, `[AM]`.
    - InjeÃƒÂ§ÃƒÂ£o de Tabelas Fundamentais (DistÃƒÂ¢ncia, MT, EscuridÃƒÂ£o, Cobertura) no Grafo.
* [Feito] Lote 67: UX de Elite - BotÃƒÂ£o de Copiar e HistÃƒÂ³rico Persistente | `(Commit: pendente)`
    - ImplementaÃƒÂ§ÃƒÂ£o do LocalClipboardManager para cÃƒÂ³pia rÃƒÂ¡pida de respostas.
    - CriaÃƒÂ§ÃƒÂ£o de tabelas `chat_sessions` e `chat_messages` no SQLite (V13).
    - Interface de seletor de histÃƒÂ³rico para recuperaÃƒÂ§ÃƒÂ£o de conversas passadas.
* [Feito] Lote 68: Motor Investigador (ExpansÃƒÂ£o SemÃƒÂ¢ntica) | `(Commit: c2e2491)`
* [Feito] Lote 69: O Toque do Mestre (Contexto Adjacente) | `(Commit: ca18212)`
* [Feito] Lote 70: Motor ReAct Multi-Stage (O Mestre Investigador) | `(Commit: pendente)`


    - ImplementaÃƒÂ§ÃƒÂ£o de Loop de InvestigaÃƒÂ§ÃƒÂ£o (While) em `MestreIAUseCase.kt`.
    - Suporte a atÃƒÂ© 3 iteraÃƒÂ§ÃƒÂµes consecutivas de busca por resposta.
    - AcumulaÃƒÂ§ÃƒÂ£o inteligente de contexto entre buscas para evitar perda de raciocÃƒÂ­nio.
* [Feito] Lote 81: RestauraÃƒÂ§ÃƒÂ£o Nano-GraphRAG e MemÃƒÂ³ria de Agente | `(Commit: 51c0d3b)`
    - ImplementaÃƒÂ§ÃƒÂ£o de Busca Relacional em Dois Saltos (Multi-Hop) no `MestreIAGraphEngine.kt`.
    - IntegraÃƒÂ§ÃƒÂ£o com o sistema MemPalace (ChromaDB local) para memÃƒÂ³ria de longo prazo do agente.
    - CriaÃƒÂ§ÃƒÂ£o do "PalÃƒÂ¡cio da MemÃƒÂ³ria" (Knowledge Item) com mapa detalhado de engenharia e regras SSOT.
    - ExpansÃƒÂ£o do contexto RAG com "Essential Nodes" (Atributos e Regras Base) e Radar de RAM.
    - RotaÃƒÂ§ÃƒÂ£o de Chaves: Mapeamento de `MESTRE_IA_OPENROUTER_2_KEY` e `.3_KEY` no `MestreIAUseCase.kt` para garantir continuidade apÃƒÂ³s expiraÃƒÂ§ÃƒÂ£o da chave anterior.

* [Feito] Lote 82: RefatoraÃƒÂ§ÃƒÂ£o Mestre IA (Especialistas) | `(Commit: 86d7e4f)`
    - DivisÃƒÂ£o do UseCase em Auditor (Regras/RAG) e Forjador (GeraÃƒÂ§ÃƒÂ£o/AnÃƒÂ¡lise).
    - RefatoraÃƒÂ§ÃƒÂ£o do `MestreIAResponse` para aceitar objetos complexos em Vantagens/Desvantagens/Magias (CorreÃƒÂ§ÃƒÂ£o Crash GSON).
    - ImplementaÃƒÂ§ÃƒÂ£o de fallbacks especializados (Elite para Forja, Lite para Auditoria).
    - Limpeza de sintaxe e remoÃƒÂ§ÃƒÂ£o de redundÃƒÂ¢ncias no `FichaIADelegate`.

* [Feito] Lote 83: Blindagem Ultra-Resiliente (Zero-Crash JSON) | `(Commit: f93da21)`
    - ImplementaÃƒÂ§ÃƒÂ£o do `MestreIAItemDeserializer` com suporte a aliases (`nh`, `desc`, `id`).
    - Tratamento de exceÃƒÂ§ÃƒÂµes interno no parse para evitar interrupÃƒÂ§ÃƒÂ£o do fluxo do app.
    - SincronizaÃƒÂ§ÃƒÂ£o de motores GSON entre `Client`, `Delegate` e `Generator`.

* [Feito] Lote 84.5: CÃƒÂ©rebro Mestre (Regras de Ouro e InjeÃƒÂ§ÃƒÂ£o de CatÃƒÂ¡logo TÃƒÂ©cnico) | `(Commit: Lote84.5)`
    - IA agora recebe lista oficial de nomes de Armas e Equipamentos.
    - Regra estrita: Magias exigem AptidÃƒÂ£o MÃƒÂ¡gica e inclusÃƒÂ£o de prÃƒÂ©-requisitos.
* [Feito] Lote 84.6: SeparaÃƒÂ§ÃƒÂ£o Arquitetural (Prompts Isolados) | `(Commit: Lote84.6)`
    - Prompts movidos para `MestreIAPrompts.kt`, limpando o cliente de rede.
* [Feito] Lote 84.7: Alinhamento TÃƒÂ©cnico (NT, Dano PT-BR e RAG Expansivo) | `(Commit: Lote84.7)`
    - ProibiÃƒÂ§ÃƒÂ£o de termos em inglÃƒÂªs (cut/pi) e exigÃƒÂªncia de sufixos /NT.
    - ExpansÃƒÂ£o automÃƒÂ¡tica do RAG para garantir catÃƒÂ¡logo tÃƒÂ©cnico em personagens novos.
* [Feito] Lote 84.8: ConsciÃƒÂªncia de App (IA entende a automaÃƒÂ§ÃƒÂ£o do sistema) | `(Commit: Lote84.8)`
    - IA agora entende que o app automatiza cÃƒÂ¡lculos se os nomes estiverem corretos.

* [Feito] Lote 85: Mesa Virtual (Lote 1) - Console do Narrador e AutomaÃƒÂ§ÃƒÂµes | `(Commit: Lote85)`
    - CriaÃƒÂ§ÃƒÂ£o do Console Web (`index.html`) com suporte a visualizaÃƒÂ§ÃƒÂ£o de mÃƒÂºltiplas fichas JSON simultÃƒÂ¢neas.
    - ImplementaÃƒÂ§ÃƒÂ£o de Calculadora de Dano Localizado com multiplicadores oficiais (CrÃƒÂ¢nio x4, Vitais x3) e limites de membros (PV/2 e PV/3).
    - AdiÃƒÂ§ÃƒÂ£o de controles interativos de PV/PF e ajuste manual de bÃƒÂ´nus de RD por localizaÃƒÂ§ÃƒÂ£o.
    - AutomaÃƒÂ§ÃƒÂ£o de caracterÃƒÂ­sticas derivadas na interface: PER, VON, Velocidade, Deslocamento e Dano de ST (GdP/GeB).
    - PreparaÃƒÂ§ÃƒÂ£o do App Android com `intent-filter` para o protocolo `gurpsapp://conectar`.

* [Feito] Lote 86: UnificaÃƒÂ§ÃƒÂ£o de TraÃƒÂ§os e Busca Inteligente (Modificadores) | `(Commit: 5ce593b)`
    - ImplementaÃƒÂ§ÃƒÂ£o de barra de busca no diÃƒÂ¡logo de modificadores (catÃƒÂ¡logo geral + especÃƒÂ­ficos).
    - UnificaÃƒÂ§ÃƒÂ£o das interfaces de AdiÃƒÂ§ÃƒÂ£o e EdiÃƒÂ§ÃƒÂ£o de Vantagens e Desvantagens (Ficha e Modelo Racial).
    - CorreÃƒÂ§ÃƒÂ£o da persistÃƒÂªncia de regras especiais (Aliados, Patronos, DependÃƒÂªncia, Inimigos, etc.).
    - Blindagem de cÃƒÂ¡lculo de custo para traÃƒÂ§os legados (fallback de `specialRule` via catÃƒÂ¡logo).
    - AtivaÃƒÂ§ÃƒÂ£o de salvamento automÃƒÂ¡tico (`salvarFicha()`) apÃƒÂ³s qualquer ediÃƒÂ§ÃƒÂ£o de traÃƒÂ§os.

[Feito] Lote 87: ExibiÃƒÂ§ÃƒÂ£o de descriÃƒÂ§ÃƒÂµes de perÃƒÂ­cias/magias na Aba de Rolagem | `(Commit: c3e3859)`
* [Feito] Lote 88: Blindagem do Mestre IA (Rastro de Provas) | `(Manual: Antigravity)`
    - ImplementaÃƒÂ§ÃƒÂ£o do protocolo de citaÃƒÂ§ÃƒÂ£o obrigatÃƒÂ³ria `[Livro, PÃƒÂ¡g. X]`.
    - Bloqueio de memÃƒÂ³ria externa para evitar alucinaÃƒÂ§ÃƒÂµes de regras nÃƒÂ£o documentadas no CÃƒÂ³dex.
    - ExigÃƒÂªncia de uso de ferramentas de busca para dÃƒÂºvidas tÃƒÂ©cnicas.
    - AnÃƒÂ¡lise profunda do sistema e identificaÃƒÂ§ÃƒÂ£o de 5 vulnerabilidades crÃƒÂ­ticas de alucinaÃƒÂ§ÃƒÂ£o.
* [Feito] Lote 89: HigienizaÃƒÂ§ÃƒÂ£o de Ativos (Assets Cleanup) | `(Manual: Antigravity)`
    - RemoÃƒÂ§ÃƒÂ£o de banco de dados residual `chroma.sqlite3` (Legado Lote 63).
    - ExclusÃƒÂ£o de `catalogo_nomes_ia.json` obsoleto (substituÃƒÂ­do pelo GraphRAG dinÃƒÂ¢mico).
    - Faxina de arquivos JSON legados (`vantagens.v1`, `v2`, `magias.json`, etc.) para reduzir tamanho do APK.
    - RealocaÃƒÂ§ÃƒÂ£o de scripts de prÃƒÂ©-processamento (`populate_graph.py`) para fora da pasta de assets do App.

* [Feito] Lote 98: PaginaÃƒÂ§ÃƒÂ£o de Resultados (PÃƒÂ¡gina 2) e Calibragem de PrecisÃƒÂ£o (Peso de Ouro) | `(Commit: Lote98)`
    - ImplementaÃƒÂ§ÃƒÂ£o de parÃƒÂ¢metro `pagina` na ferramenta de busca para evitar loops infinitos.
    - Sistema de Pesos: Termos originais da pergunta valem +10, sinÃƒÂ´nimos automÃƒÂ¡ticos valem +2.
    - BÃƒÂ´nus Massivo de TÃƒÂ­tulo (+35) para match exato com a dÃƒÂºvida do usuÃƒÂ¡rio.
    - Fim do problema de "Sangramento" ser enterrado por magias de cura ou outros termos genÃƒÂ©ricos.
* [Feito] Lote 100: ConsciÃƒÂªncia BibliogrÃƒÂ¡fica (Source-Aware RAG) e DicionÃƒÂ¡rio TÃƒÂ©cnico | `(Commit: Lote100)`
    - ImplementaÃƒÂ§ÃƒÂ£o de Busca Filtrada: O sistema agora distingue entre livros diferentes que possuem o mesmo nÃƒÂºmero de pÃƒÂ¡gina, eliminando colisÃƒÂµes (ex: PÃƒÂ¡g 117 de Magia vs Artes Marciais).
    - DicionÃƒÂ¡rio TÃƒÂ©cnico Mestre: InjeÃƒÂ§ÃƒÂ£o de sinÃƒÂ´nimos de alta fidelidade (ex: "Cavar" remete automaticamente a "EscavaÃƒÂ§ÃƒÂ£o") para garantir que o motor de busca encontre a regra correta mesmo com linguagem comum.
    - Regex de PrecisÃƒÂ£o: Captura automÃƒÂ¡tica da fonte bibliogrÃƒÂ¡fica `[Livro]` a partir dos resumos do grafo para direcionar a carga de recortes manuais.
* [Feito] Lote 101: Motor RAG SemÃƒÂ¢ntico HÃƒÂ­brido & Anti-DiluiÃƒÂ§ÃƒÂ£o (FTS4 Layering) | `(Commit: Lote101)`
    - DesativaÃƒÂ§ÃƒÂ£o de "blindagem" (stopWords genÃƒÂ©ricas de RPG como 'ataque' e 'dano'), devolvendo a capacidade do motor de interpretar frases naturais cruas sem falhas.
    - ImplementaÃƒÂ§ÃƒÂ£o de Busca por Camadas (Layering): Garantia matemÃƒÂ¡tica de que palavras raras (ex: 'piscina') nÃƒÂ£o sejam engolidas do limite do FTS4 por palavras comuns (ex: 'dano') atravÃƒÂ©s de iteraÃƒÂ§ÃƒÂµes individuais na base de recortes.
* [Feito] Lote 102: Algoritmo de Raridade (TF-IDF Proxy local em Kotlin) | `(Commit: Lote102)`
    - ImplementaÃƒÂ§ÃƒÂ£o de heurÃƒÂ­stica inspirada no TF-IDF (Inverse Document Frequency) diretamente no re-ranking local.
    - CÃƒÂ¡lculo de peso por raridade: Palavras com muitos resultados no SQLite (peso 1) nÃƒÂ£o pontuam alto; palavras com poucos recortes exatos (peso atÃƒÂ© 50) geram multiplicadores explosivos (ex: 'Combate AquÃƒÂ¡tico' supera 100% 'Dano de Arma').
    - O motor agora filtra o ruÃƒÂ­do de perguntas longas atravÃƒÂ©s de matemÃƒÂ¡tica pura, sem depender de injeÃƒÂ§ÃƒÂµes rÃƒÂ­gidas.
* [Feito] Lote 103: RAG State-of-the-Art (RRF & Parent Document) | `(Commits: 6a059ea, a43a88e)`
    - **Parent Document Retrieval:** O motor agora busca a pÃƒÂ¡gina inteira em que o recorte se encontra, resolvendo perdas de contexto onde regras importantes ou tabelas continuavam no prÃƒÂ³ximo parÃƒÂ¡grafo. O limite do prompt saltou para 15000 chars.
    - **RRF (Reciprocal Rank Fusion):** Implementada a fÃƒÂ³rmula matemÃƒÂ¡tica padrÃƒÂ£o da indÃƒÂºstria `(1 / Rank + 60)` para fundir de forma justa o ranking de palavras-chave da busca textual (FTS) com as sugestÃƒÂµes de pÃƒÂ¡gina vindas do Knowledge Graph, gerando um Top 3 infalÃƒÂ­vel.

* [Feito] Lote 104: Filtro de RuÃƒÂ­do & Mega-Contexto (60k) | `(Commit: de125e6)`
    - **Filtro de RuÃƒÂ­do:** ExpansÃƒÂ£o de termos por sinÃƒÂ´nimos (gladiador, luta) agora afeta apenas o Grafo. A busca de texto bruto (Chunks) foca 100% nos termos reais do usuÃƒÂ¡rio para evitar poluiÃƒÂ§ÃƒÂ£o.
    - **Janela de 60k Chars:** Limite da `PonteDeFerro` expandido de 15k para 60k caracteres. Isso permite enviar atÃƒÂ© 10 pÃƒÂ¡ginas completas (Documento Pai) sem cortes.
    - **Top 8 Retrieval:** Motor agora coleta as 8 melhores pÃƒÂ¡ginas encontradas, garantindo que regras especÃƒÂ­ficas entrem no prompt mesmo que nÃƒÂ£o sejam o Top 1 de score.

* [Feito] Lote 105: Diversidade de Elite & BÃƒÂ´nus de Autoridade | `(Commit: c8bcfe6)`
    - **Filtro Anti-MonopÃƒÂ³lio:** Implementada trava algorÃƒÂ­tmica que limita a 2 recortes por pÃƒÂ¡gina no Top 8. Isso obriga o motor a trazer diversidade de regras (ex: PÃƒÂ¡g 16 + PÃƒÂ¡g 430 + PÃƒÂ¡g 397) em vez de inundar o contexto com uma ÃƒÂºnica pÃƒÂ¡gina genÃƒÂ©rica.
    - **BÃƒÂ´nus de Grafo (5x):** PÃƒÂ¡ginas sugeridas pelo Knowledge Graph agora recebem um multiplicador de relevÃƒÂ¢ncia de 500%, garantindo que a inteligÃƒÂªncia estrutural prevaleÃƒÂ§a sobre a mera repetiÃƒÂ§ÃƒÂ£o de palavras-chave.

* [Feito] Lote 106: Contexto Adjacente (PÃƒÂ¡gina Suporte) | `(Manual: Antigravity)`
    - **PÃƒÂ¡gina n+1:** O motor `MestreIAGraphEngine` agora recupera automaticamente a pÃƒÂ¡gina seguinte para cada pÃƒÂ¡gina de impacto encontrada, garantindo integridade de tabelas e fÃƒÂ³rmulas longas.
    - **ExpansÃƒÂ£o de Contexto:** Aumentado o limite de recortes finais de 10 para **20** para acomodar o suporte adjacente sem cortes.
    - **ValidaÃƒÂ§ÃƒÂ£o de ColisÃƒÂ£o:** Confirmada a recuperaÃƒÂ§ÃƒÂ£o da fÃƒÂ³rmula de dano (PÃƒÂ¡g 432) ao buscar por termos na PÃƒÂ¡g 431.

* [Feito] Lote 107: Blindagem de FTS4 (NormalizaÃƒÂ§ÃƒÂ£o de Acentos) | `(Manual: Antigravity)`
    - **IndexaÃƒÂ§ÃƒÂ£o Normalizada:** A coluna `search_text` agora ÃƒÂ© povoada sem acentos e em lowercase, blindando o motor contra encoding corrompido (Mojibake).
    - **Busca AgnÃƒÂ³stica:** Os termos de busca sÃƒÂ£o normalizados antes da consulta, permitindo que "colisao" encontre "ColisÃƒÂ£o" e vice-versa.
    - **UnificaÃƒÂ§ÃƒÂ£o de Scoring:** O re-ranking TF-IDF agora utiliza os mesmos termos normalizados, garantindo precisÃƒÂ£o matemÃƒÂ¡tica no Top 3.

* [Finalizado] Lote 108: Sincronia AutomÃƒÂ¡tica e Limpeza de Legado | `(Commit: 2e59eba)`
    - **RemoÃƒÂ§ÃƒÂ£o de Gatilhos Manuais:** Extintos os comandos "forÃƒÂ§ar sincronizaÃƒÂ§ÃƒÂ£o" via chat.
    - **Sincronia Inteligente:** Implementado **Mutex** no `MestreIARepository` para garantir carga ÃƒÂºnica e atÃƒÂ´mica.
    - **Performance & Background:** UseCase migrado para `Dispatchers.IO`, eliminando lag na UI.
    - **Encoding UTF-8 (Fim do Mojibake):** ForÃƒÂ§ada leitura de assets em UTF-8, corrigindo acentos corrompidos.
    - **v19 do Banco:** Incrementada versÃƒÂ£o do DB para forÃƒÂ§ar reset limpo dos ÃƒÂ­ndices.

* [Finalizado] Lote 109: PurificaÃƒÂ§ÃƒÂ£o Arquitetural | `(Commit: 2e59eba)`
    - **Isolamento de RepositÃƒÂ³rio:** Criado o `MestreIARepository` para separar a lÃƒÂ³gica de regras da lÃƒÂ³gica de ficha (`DataRepository`).
    - **DelegaÃƒÂ§ÃƒÂ£o Limpa:** O `DataRepository` agora apenas delega as chamadas de busca, reduzindo seu tamanho e complexidade.
    - **Estabilidade de Testes:** Ajustados Stubs e inicializaÃƒÂ§ÃƒÂ£o `lazy` para permitir testes unitÃƒÂ¡rios sem dependÃƒÂªncia de Contexto.

* [Finalizado] Lote 111: OtimizaÃƒÂ§ÃƒÂ£o de RAG e Cura de Contexto (Dano por ColisÃƒÂ£o) | `(Commit: ce531eb)`
    - **DiagnÃƒÂ³stico de Carga:** RemoÃƒÂ§ÃƒÂ£o de limitaÃƒÂ§ÃƒÂµes de cÃƒÂ³digo que truncavam regras vitais.
    - **Prioridade VIP:** BÃƒÂ´nus de score +1000 para pÃƒÂ¡ginas recomendadas pelo Grafo (Garante PÃƒÂ¡g 433).
    - **Abertura de Gargalo:** Entrega de atÃƒÂ© 25 recortes de contexto ao Gemini no CaseUse.
    - **ValidaÃƒÂ§ÃƒÂ£o de Dados:** VerificaÃƒÂ§ÃƒÂ£o da integridade da regra de ColisÃƒÂ£o no banco SQLite.

* [Finalizado] Lote 112: Motor de RaciocÃƒÂ­nio e Hierarquia (Plan Systemic Evolution) | `(Commit: Lote112)`
    - **Janela Deslizante (N-1, N, N+1):** RecuperaÃƒÂ§ÃƒÂ£o automÃƒÂ¡tica da pÃƒÂ¡gina anterior para integridade de regras.
    - **Hierarquia de Autoridade:** BÃƒÂ´nus (+50) para o MÃƒÂ³dulo BÃƒÂ¡sico, garantindo soberania da "Lei MÃƒÂ£e".
    - **Prompt de RaciocÃƒÂ­nio (Pilares):** IA agora decompÃƒÂµe problemas em AÃƒÂ§ÃƒÂ£o, Atributo, Ambiente e Estado.
    - **PurificaÃƒÂ§ÃƒÂ£o do Grafo:** InjeÃƒÂ§ÃƒÂ£o de source_id em todos os 2476 nÃƒÂ³s e unificaÃƒÂ§ÃƒÂ£o de NÃƒÂ³s Mestres (Ataque Total).

* [Finalizado] Lote 112.1: CorreÃƒÂ§ÃƒÂ£o do Gargalo de Regex no RAG | `(Commit: 6dc456c)`
    - **DiagnÃƒÂ³stico:** O Grafo corretamente identificava mÃƒÂºltiplas pÃƒÂ¡ginas (ex: [PÃƒÂ¡g. 353, 354, 388] para Terreno), mas a Regex capturava apenas a primeira.
    - **CorreÃƒÂ§ÃƒÂ£o MatemÃƒÂ¡tica:** SubstituiÃƒÂ§ÃƒÂ£o de `Regex.find()` por `Regex.findAll()` no `MestreIAGraphEngine.kt`, iterando sobre todas as ocorrÃƒÂªncias de pÃƒÂ¡ginas no resumo.
    - **Resultado PrÃƒÂ¡tico:** A "Ponte de PÃƒÂ¡gina" agora enfileira todas as pÃƒÂ¡ginas listadas (353, 354 e 388), permitindo que a IA aplique a regra matemÃƒÂ¡tica de Lama no combate.

* [Finalizado] Lote 112.2: RRF Rank Normalization (Reciprocal Rank Fusion) | `(Commit: Pending)`
    - **DiagnÃƒÂ³stico:** O Algoritmo de Ranking Lexical RRF penalizava as pÃƒÂ¡ginas extras do Grafo. Se o Grafo apontava 3 pÃƒÂ¡ginas, a segunda e terceira ganhavam pontuaÃƒÂ§ÃƒÂ£o decrescente, impedindo regras secundÃƒÂ¡rias (Lama) de chegarem ao Top 8.
    - **CorreÃƒÂ§ÃƒÂ£o:** Alterado o `graphRank` para tratar **todas** as pÃƒÂ¡ginas apontadas pelo Grafo com pontuaÃƒÂ§ÃƒÂ£o absoluta (Rank = 1). A responsabilidade do desempate ÃƒÂ© agora puramente lexical.

* [Finalizado] Lote 112.3: CorreÃƒÂ§ÃƒÂ£o do Anti-MonopÃƒÂ³lio e Autenticidade de Fonte BibliogrÃƒÂ¡fica | `(Commit: Pending)`
    - **DiagnÃƒÂ³stico de Magias:** Consultas como "Magia Desejo" recuperavam o nÃƒÂ³ correto, mas a pÃƒÂ¡gina (PÃƒÂ¡g 61) era carregada de 3 livros diferentes simultaneamente (MÃƒÂ³dulo BÃƒÂ¡sico, Artes Marciais e Magia). O filtro "Anti-MonopÃƒÂ³lio" as considerava idÃƒÂªnticas (PÃƒÂ¡g 61) e cortava o livro Magia (por ter prioridade menor no desempate), ocultando a regra real. A lista de *stop words* tambÃƒÂ©m estava bloqueando palavras tÃƒÂ©cnicas vitais (ex: "prÃƒÂ©", "requisitos").
    - **RefatoraÃƒÂ§ÃƒÂ£o da Ponte de PÃƒÂ¡gina (`PaginaAlvo`):** Implementada amarraÃƒÂ§ÃƒÂ£o com `sourceId` nos metadados da base. Agora, o sistema exige que a pÃƒÂ¡gina 61 do Grafo corresponda exclusivamente ÃƒÂ  PÃƒÂ¡g 61 do suplemento correto (Magia).
    - **BÃƒÂ´nus Lexical Especializado:** Adicionado BÃƒÂ´nus +60 Lexical se os "Termos Base" ou o "NÃƒÂ³ do Grafo" pertencerem ao grupo semÃƒÂ¢ntico de "Magia" (pt_gurps_magia) ou "Artes Marciais" (pt_artes_marciais), superando artificialmente o bÃƒÂ´nus de Autoridade do MÃƒÂ³dulo BÃƒÂ¡sico para buscas ultra-especializadas.
    - **Stop Words:** Removidas palavras cruciais como "requisitos" e "pre" do limpador lexical no `extrairPalavrasChave`.

* [Feito] Lote 113: IntegraÃƒÂ§ÃƒÂ£o Nexus-IA (O Consultor Arcano) | `(Commit: c4a1b2d)`
* [Feito] Lote 114: Blindagem de Conectividade & Contexto Arcano | `(Commit: 9cb6448)`
    - **Fix Conectividade:** ResoluÃƒÂ§ÃƒÂ£o do Erro 400 atravÃƒÂ©s da segregaÃƒÂ§ÃƒÂ£o de URLs e Chaves por provedor (Gemini vs DeepSeek).
    - **Enriquecimento Arcano:** InjeÃƒÂ§ÃƒÂ£o automÃƒÂ¡tica de Escolas e PrÃƒÂ©-requisitos no contexto de magias para o Mestre IA.
    - **EstabilizaÃƒÂ§ÃƒÂ£o de Build:** CorreÃƒÂ§ÃƒÂ£o de inferÃƒÂªncia de tipos no ranking RRF do GraphEngine.
    - **ConexÃƒÂ£o de Motores:** ImplementaÃƒÂ§ÃƒÂ£o da ferramenta nativa `consultar_nexus_arcano` no Mestre IA, permitindo que a IA invoque o Motor Nexus em milissegundos.
    - **Gabarito TÃƒÂ©cnico:** O motor agora gera um "Gabarito de Ouro" determinÃƒÂ­stico (Estado Zero) com ÃƒÂ¡rvore de dependÃƒÂªncias completa e sugestÃƒÂµes para metas de escolas.
    - **Fidelidade BibliogrÃƒÂ¡fica:** CorreÃƒÂ§ÃƒÂ£o de dados no `magias2versao.json` (Sopro de Fogo/Ãƒcido/Frio) alinhando "ResistÃƒÂªncia" com o MÃƒÂ³dulo BÃƒÂ¡sico.
    - **IndependÃƒÂªncia de Ficha:** A ferramenta funciona de forma isolada, permitindo planejar magias mesmo sem uma ficha ativa ou iniciada.

* [Feito] Lote 115: ConsolidaÃ§Ã£o de Arquitetura e Auditoria SubaquÃ¡tica | `(Commit: 8faf742)`
* [Feito] Lote 116: Auditoria e RestauraÃ§Ã£o do Mestre IA | `(Commit: d403148)`
* [Feito] Lote 117: CorreÃ§Ã£o de PrecisÃ£o RAG (O NÃ³ de Ouro) | `(Commit: db16362)`
* [Feito] Lote 118: DiagnÃ³stico de Falha na ExtraÃ§Ã£o de PÃ¡ginas e PreparaÃ§Ã£o para CorreÃ§Ã£o | `(Commit: f3576eb)`

### Lote 117: CorreÃ§Ã£o de PrecisÃ£o RAG (O NÃ³ de Ouro)
*   **Commit:** `db16362 Lote 117: CorreÃ§Ã£o de PrecisÃ£o RAG - PriorizaÃ§Ã£o de Regras e ExpansÃ£o de Funil no Motor de Busca`

[Feito] Lote 118: DiagnÃ³stico de Falha na ExtraÃ§Ã£o de PÃ¡ginas e PreparaÃ§Ã£o para CorreÃ§Ã£o (`f3576eb`)


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

* [Feito] Lote 130: Ponte Inventário — Planner lê a ficha do personagem para busca contextual | (Commit: Pending)
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

**[Bateria de Testes a Realizar]**
- Bateria de Testes (Stress Test)
Impacto em Alta Velocidade: "Um cavaleiro em carga a cavalo (Move 8) atinge um soldado com uma lança. Como calculo o dano de colisão baseado na ST 16 do cavalo?"
Regras de Afogamento: "Meu personagem caiu em um rio e está sem fôlego. Quanto tempo ele aguenta antes de começar a perder PV e quais são os testes de HT?"
Visibilidade Crítica: "Estou tentando atirar em um alvo na escuridão total, mas tenho 'Visão Noturna 5'. Qual a minha penalidade final?"
Equipamentos e Carga: "Estou carregando 40kg de ouro. Minha ST é 10. Como isso afeta minha Esquiva e meu Deslocamento atual?"
Aparar com Escudo: "Um ogro me atacou com uma clava gigante. Posso usar a regra de 'Aparar com o Escudo' ou sou obrigado a Bloquear?"
Criação de Especialista: "Gere uma ficha de um Ninja especializado em infiltração tecnológica (NT 9), com 'Mãos Pegajosas' e 'Passo Leve', usando 150 pontos."
Regra de Recuo (Armas de Fogo): "Se eu der uma rajada de 3 tiros com uma submetralhadora de Recuo 2, como calculo quantos tiros acertaram?"
