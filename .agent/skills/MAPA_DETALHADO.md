# Mapa Detalhado: Arquivos e Funções do Projeto GURPS

Mapa de engenharia completo do projeto. Use para localizar lógicas específicas sem varrer o código.

> ➕ **2026-08-03 — Varredura de completude.** O mapa estava com **80 arquivos de código, 113 testes
> e 5 assets** fora dele — quase todos das frentes de **automação de regras** (jul/ago) e do **padrão
> de tela** (ago). Todos entraram nas seções abaixo, com a descrição saindo do KDoc de cada arquivo,
> não de memória. As frentes novas cobertas:
> - **§5 Domain — Rules** cresceu de 5 para 41 arquivos e ganhou subdivisões (atributos e tetos;
>   testes de resistir; combate/mira; rodapé das perícias; deslocamento e estados; texto).
> - **§6 Trait Rules** ganhou a **arquitetura híbrida** (`TraitEffectModels` + `EfeitoInterpretador`):
>   bônus simples viram DADO no JSON, casos complexos continuam classe Kotlin, e **a classe vence**.
> - **§20 Features** ganhou os ~18 painéis/diálogos da aba Rolagem, quase todos extraídos da
>   `TabRolagem` quando ela bateu no teto de 1.000 linhas.
> - **§21 UI** ganhou o **padrão de tela** (`AppSelectionUi`, `AppButtons`) — ver a skill
>   `.claude/skills/padrao-de-tela/`.
> - **§28 Testes** foi reescrita por frente: a suíte passou de ~1.200 para **1.830**.
>
> ⚠️ Ainda **fora do mapa de propósito**: os arquivos de `ui/saga/` (combate tático) e a suíte
> `nexus/arcano/`, que já têm documento próprio.

Atualizado em: 2026-07-22 (**§32.9 nova**: PILAR MAGIA no combate; motor executa 98 das 879 magias).
Rede de invariantes SIM-1 e build paralelo BUILD-1 (gate 7-8min → 1m36s). **§32.4 revista** após a
refatoração REFACTOR-0..3: a DECISÃO e a TRADUÇÃO saíram do `SagaCombatController` (2243→2099) para
arquivos puros e testáveis (`RegrasMovimentoTatico`, `TraducaoFichaParaCombate`), e a UI de
conjuração saiu do `CombatUi` (2000→1610) para `CombatUiConjuracao`.
Base anterior: 2026-06-08 (fidelidade linha-a-linha) | 2026-05-30 (Mestre IA pós-Lote 328) | 130+ arquivos.

> ➕ **2026-06-08 — Feature Imagem/Retrato do Personagem:** novo `ImagemPersonagemStore.kt`
> (seção 15) + funções novas em `Personagem`, `FichaAttributeDelegate`, `FichaViewModel`,
> `FichaNetworkDelegate`, `DiscordRollApiClient`, `FichaScreen` e na API Node (`discord-roll-api`).
> Foto no cabeçalho (recorte por rosto/assunto via ML Kit) + tela cheia + envio ao Discord.
> Cada item está marcado com **[+ 2026-06-08]** nas seções abaixo.

> ➕ **2026-06-09 (Lotes 341-348):** arquivos novos e mudanças (marcados com **[+ 2026-06-09]**):
> - **Rolagem:** `domain/roll/CriticoRules.kt` (NOVO, §10) + `assets/tabelas_criticas.json` (§25) —
>   automação de Golpe Fulminante / Erro Crítico ao dar Decisivo/Crítico em testes de COMBATE.
> - **Mestre Pintor:** `data/network/GeminiImageService.kt` (NOVO, §14) — gera retrato via Gemini Image API.
> - **Templates do Forjador:** `domain/loaders/ForjadorTemplateCatalogo.kt` (NOVO, §7) +
>   `assets/forjador_templates.json` (§25) — 60 templates de personagem prontos (combatentes, furtivos,
>   sociais, 10 escolas de mago, gêneros modernos).
> - **Import/Export:** imagem do personagem viaja embutida (base64) na ficha; intent-filters do
>   `AndroidManifest` ampliados (abrir ficha do WhatsApp por extensão/octet-stream).
> - **Forjador (fluxo):** entrega JSON direto (não usa mais loop de tools p/ CRIAR ficha); correções de
>   pré-requisito de técnica, GPS de técnicas, raça "null", budget de template. Ver `ARQUITETURA_MESTRE_IA.md §9`.

> ➕ **2026-06-14 (Lotes 349-370, branch `GURPS-Saga`, HEAD `41996c4`):** modo **SAGA / NARRADOR** (3º modo de IA)
> + **motor de combate** (`domain/combat/`, Kotlin puro) + UI de combate. Arquivos novos na **§32** (nova).
> `FichaDatabase` subiu p/ **v26** (migrações 24→25→26 explícitas, tabelas da Saga). Detalhe do fluxo de IA: `ARQUITETURA_MESTRE_IA.md §10`.
>
> ➕ **2026-07-17:** Atualização englobando novos módulos: Combate Tático Hexagonal (Saga/VTT), Motor de Magia Detalhado (`domain/magic`), sensores para rolagem 3D, e stores de imagens para VTT e Cenários.

> ⚠️ **AUDITOR mudou de motor (Lotes 325-328):** saiu da busca semântica (RAG/HNSW) para
> "grep + leitura dirigida" (`localizar_no_codex` + `ler_pagina`). Vários arquivos abaixo
> viraram LEGADO/MORTO — marcados com ⚠️. Detalhe e motivo de cada um em
> `ARQUITETURA_MESTRE_IA.md §5`.

---

## 1. Ponto de Entrada

- **`MainActivity.kt`** — Activity principal. Inicializa o Compose, intercepta Intents de compartilhamento de ficha, passa o Intent para o ViewModel processar. Única Activity do app. **[+ 2026-06-09]** `tratarIntentRecebido` trata `ACTION_VIEW`/`ACTION_SEND` (importar ficha do WhatsApp/explorador); limpa BOM/espaços antes do parse. Os `intent-filter` do `AndroidManifest` foram ampliados (octet-stream + filtro por EXTENSÃO `.json`/`.gurps`) para o app aparecer ao abrir a ficha no WhatsApp.

---

## 2. ViewModel e Estado Central

- **`viewmodel/FichaViewModel.kt`** — O controlador central do app. Instancia todos os delegates, mantém o `Personagem` ativo como `mutableStateOf`, coordena auto-save ao editar traços, e expõe métodos públicos que a UI chama. Delega todas as operações especializadas para os delegates. **[+ 2026-06-08]** `atualizarImagemPersonagem(uri, originalUri)`; `salvarFicha` agora também sobe o retrato ao Discord (best-effort) via `ImagemPersonagemStore.bytesBase64` + `networkDelegate.enviarRetratoDiscord`. **[+ 2026-06-09]** `exportarFichaJson*ComImagem` (suspend — embute a imagem base64 na exportação) e `restaurarImagemEmbutidaSeHouver` (no import: salva+recorta a imagem embutida e limpa o base64).

- **`viewmodel/FichaUIState.kt`** — Data classes dos estados de busca da UI: `TraitSearchState`, `SkillSearchState`, `MagicSearchState`, `TechniqueSearchState`, `EquipmentSearchState`. Sem lógica — só estruturas de dados para os filtros de catálogo.

---

## 3. Delegates do ViewModel

*Cada delegate é responsável por uma fatia da lógica do `FichaViewModel`. Sem delegates, o ViewModel seria um arquivo de 4.000+ linhas.*

- **`delegates/FichaAttributeDelegate.kt`** — Atualiza atributos primários (ST/DX/IQ/HT), atributos secundários (mod PV, PF, Vontade, Percepção, Velocidade, Deslocamento), e dados básicos do personagem (nome, jogador, campanha, histórico, aparência, notas). Aplica limites via `coerceIn`. **[+ 2026-06-08]** `atualizarImagemPersonagem(personagem, uri, originalUri)` grava os dois caminhos de imagem.

- **`delegates/FichaCombatDelegate.kt`** — Calcula e atualiza defesas ativas (Esquiva, Apara, Bloqueio). Inclui bônus manuais, seleção de perícia de Apara e de Escudo para Bloqueio. Retorna a lista de `ActiveDefense` para a UI exibir.

- **`delegates/FichaEquipmentDelegate.kt`** — Adiciona equipamentos gerais, armas do catálogo e armaduras. Gera as notas automáticas de armas (valor de Aparar, classe de arma de fogo). Filtra tags de armaduras. Contém lógica de classificação de armas de fogo por categoria (pistola, rifle, ultratech, pesada).

- **`delegates/FichaIADelegate.kt`** — Gerente completo da IA. Instancia `MestreIAUseCase` e `MestreIAGeneratorUseCase`. Controla o histórico de chat (`mestreIAChatHistory`), sessões persistidas (Room), modo da IA (`conversa`/`geracao`/`analise`), auto-sincronização do Códex, e o sistema de "bolhas batch" (agrupa eventos consecutivos do Forjador na mesma bolha de chat).

- **`delegates/FichaMagicDelegate.kt`** — Adiciona e remove magias. Valida pré-requisitos via `MagicEngine`, detecta duplicatas por escola (magias de múltiplas instâncias). Retorna escolas e classes únicas do catálogo para os filtros.

- **`delegates/FichaNetworkDelegate.kt`** — Envia rolagens para o Discord via `DiscordRollApiClient`, com retry em timeout. Busca lista de canais de voz do Discord. Sem estado próprio — só operações de rede. **[+ 2026-06-08]** `enviarRetratoDiscord(characterName, imageDataUri)` sobe o retrato uma vez (chamado no salvar da ficha).

- **`delegates/FichaPersistenceDelegate.kt`** — Salva, carrega e exclui fichas via `FichaStorageRepository`. Filtra o auto-save (`_autosave_recuperacao`) da listagem pública. Tenta import via `PersonagemInterop` e faz fallback para `Personagem.fromJson` em JSONs antigos.

- **`delegates/FichaSearchDelegate.kt`** — Mantém os estados de busca (`advantageSearch`, `disadvantageSearch`, `skillSearch`, `magicSearch`, `techniqueSearch`, `equipmentSearch`). Delega as filtragens para `DataRepository`. Cache simples para `filtrarMagias` (evita reprocessar a cada recomposição).

- **`delegates/FichaSkillDelegate.kt`** — Adiciona, remove e atualiza perícias. Valida pré-requisitos via `DataRepository.validarPreRequisitosPericia`. Detecta técnicas pelo nome normalizado (para não misturar com perícias). Recalcula NH ao alterar pontos gastos.

- **`delegates/FichaSocialDelegate.kt`** — Gerencia configuração de envio ao Discord: seleção de canal de voz, status de carregamento de canais, persistência do canal selecionado em `SharedPreferences`. Coordena com `FichaNetworkDelegate` para buscar canais.

- **`delegates/FichaTraitDelegate.kt`** — Adiciona, remove e edita vantagens e desvantagens. Valida duplicatas (permite múltiplas instâncias de `ataque_inato`, `golpeadores`, `resistencia_a_dano`). Normaliza o nível de vantagens acumulativas. Delega custo para `DataRepository.criarVantagemSelecionada`.


- **`delegates/FichaHistoryDelegate.kt`** — `diffAndLog(antigo, novo)` compara duas versões do `Personagem` campo a campo (nome, jogador, pontos, XP, os quatro atributos primários, os modificadores secundários, listas de traços/perícias/magias/equipamento) e devolve a ficha com um `RegistroLog` por mudança. É o que alimenta o histórico de alterações da ficha.

---

## 4. Domain — Engines

*Lógica de regras pura do GURPS 4ª Ed. Sem Android, sem UI.*

- **`domain/engine/MagicEngine.kt`** — Cálculo de Aptidão Mágica por escola (vantagem `aptidao_magica` com mod de escola). Valida se magia pode ter múltiplas instâncias. Valida pré-requisitos de magias (NH mínimo e magias dependentes). Calcula custo de energia ajustado pelo NH.

- **`domain/engine/SkillEngine.kt`** — Regras de técnicas: calcula o tipo de limite (`TecnicaLimiteKind`) de cada técnica (explícito relativo, baseado em perícia base, Aparar, Bloquear, metade da penalidade). Inclui normalização de texto para matching de nomes.

---

## 5. Domain — Rules

*Cálculos de regras encapsulados por tema.*

- **`domain/rules/CharacterRules.kt`** — A base de tudo. Tabelas de Golpe/Empurrão por ST. Cálculo de PV, PF, Velocidade Básica, Deslocamento, Percepção, Vontade. Custo de atributos primários e secundários. Cálculo de custo de vantagens com `specialRule` (Aliado, Inimigo, Dependente, Reputação, Dever, Manutenção, Vício, Contato, Dor Crônica, Fraqueza, Vulnerabilidade). Limite de desvantagens. Referência global `DATA_REPOSITORY_INSTANCE`.

- **`domain/rules/CombatRules.kt`** — Fórmulas de defesas ativas: `calcularEsquiva`, `calcularApara`, `calcularBloqueio` e suas variantes de base. Puro e sem dependências externas.

- **`domain/rules/MagiaEnergiaRules.kt`** — Redução de custo de energia por NH alto (NH≥15 → -1, NH≥20 → -2+). Parse de string de custo de energia ("2 pontos" → 2). Usado por `MagicEngine` e pelos diálogos de magia.

- **`domain/rules/SentidoRules.kt`** — **[+ 2026-06-14, Lote 372]** Testes de Sentidos (MB p.358): `enum Sentido` (Percepção/Visão/Audição/Olfato-Paladar/Tato); `avaliar(p, sentido)` rola vs Percepção somando o "Sentido Aguçado" e descontando limitações, com COMPONENTES NOMEADOS (a "notinha"). Mapeia ids do catálogo (visao_agucada, audicao_agucada, paladar_olfato_apurado, *_discriminatorio, visao_hiperespectral, tato_apurado; redutores duro_de_ouvido/disopia; bloqueios cegueira/surdez/disosmia). Cobre traços pessoais E raciais. Puro/testável (`SentidoRulesTest`). Consumido por `DialogoSentidos` (§20).


### Atributos, custo e tetos

- **`domain/rules/AtributoBonusRules.kt`** — Soma os bônus de atributo vindos de vantagens/desvantagens (GANCHO-A). Mora fora do `Personagem` por causa do teto de 1.000 linhas: lá só cabe a chamada de uma linha por propriedade.
- **`domain/rules/TetoDeAtributoRules.kt`** — Desvantagens que impõem **teto** a um atributo (Lote TETO-HT): `Magro` limita HT a 14, `Muito Gordo` a 13 (MB p.19). Não é bônus nem penalidade — é limite de criação, e por isso não cabe no campo `efeitos`.
- **`domain/rules/TetoDeNivelDoTraco.kt`** — Quantos níveis um traço pode ter (Lote TETO-1). Catálogo → Aptidão Mágica 11 → geral 20. Achado no T-LI6: a Mão Fraca travava em 3 na perícia mas o seletor deixava subir sem limite.
- **`domain/rules/StBracalRules.kt`** / **`DxBracalRules.kt`** — O **efeito** da ST e da DX Braçal (MB p.89 e p.56); o custo mora em `traits/BracalCustoRules.kt`. ⚠️ A DX Braçal **não** melhora perícia de combate — o livro proíbe, e o espelho ingênuo da ST erraria isso.
- **`domain/rules/StEspecializadaRules.kt`** — **ST de Golpe** (MB p.88) e **ST de Levantamento** (MB p.65): duas metades da força, cada uma servindo só à sua metade.

### Testes que não são perícia nem atributo puro

- **`domain/rules/ReacaoRules.kt`** — Teste de Reação (MB p.494): 3d6 contra a tabela, com os modificadores sociais (Aparência, Carisma, Voz Melodiosa, Reputação) já somados e a origem de cada ponto.
- **`domain/rules/AutocontroleRules.kt`** — Testes de autocontrole (MB p.120). O app já **guardava** o NA (6/9/12/15) para o multiplicador de custo e nunca rolava nada.
- **`domain/rules/ResistenciaRules.kt`** — Os testes de **resistir** que a ficha monta sozinha (Lote RESIST-1): consciência, morte, doença, veneno, medo, pânico. Aplica o [PisoDeTeste] no alvo final.
- **`domain/rules/MarcosDeVidaRules.kt`** — Os marcos de PV/PF e os testes que eles exigem (MB p.419-423). O GURPS não pede teste "ao tomar dano" — pede quando o dano **cruza um marco**.
- **`domain/rules/PisoDeTeste.kt`** — O alvo de um teste **nunca desce abaixo de 3** (Lote D-ESPELHO). Em 3d6 um alvo 2 seria fracasso automático, e o livro não quer que desvantagem torne o teste impossível.
- **`domain/rules/SorteRules.kt`** — **Sorte** (MB p.90): refazer duas vezes e ficar com o melhor dos três, uma vez por hora de jogo.
- **`domain/rules/TalentoInstintivoRules.kt`** — **Talento Instintivo** (MB p.92): rolar uma perícia que o personagem **não tem**, com o atributo cheio.
- **`domain/rules/VisualizacaoRules.kt`** — **Visualização** (MB p.99): um minuto de concentração, teste de IQ, e o bônus sai da margem de sucesso — com três arredondamentos diferentes, dois deles exceção.

### Combate: mira, distância e manobra

- **`domain/rules/LocaisDeAtaque.kt`** — `enum LocalAtaque` (torso, crânio, olho, vitais…) com a penalidade de cada um, e `MiraRules`, que monta a lista de alvos **com o NH já reduzido**. ⚠️ Nasceu em `domain/combat/` e mudou de pacote no MIRA-1: é tabela do livro, não depende de sessão de combate.
- **`domain/rules/ApontarRules.kt`** — **Apontar, Precisão e Visão Telescópica** (MB p.364/373 e p.99). O Apontar acumula segundos (+1 com dois, +2 com três), soma a arma firmada e a **mira acoplada**, e respeita o teto do **dobro da Prec**. ⚠️ A Telescópica fica fora desse teto: ela cancela distância, não soma no NH.
- **`domain/rules/AlcanceDoAtaque.kt`** — Responde "este ataque é à distância?" e "até onde ele chega". Decide por **perícia** e por **arma**; quando as duas discordam, `conflito()` devolve o aviso em vez de escolher calado.
- **`domain/rules/TabelaVelocidadeDistancia.kt`** — Tabela de Tamanho e Velocidade/Distância (MB p.550-551). O seletor anda de **degrau** da tabela, não de metro: cada toque vale exatamente −1.
- **`domain/rules/AvancarEAtacarRules.kt`** — **Avançar e Atacar** (MB p.366): −2 **ou a Magnitude, o que for pior** à distância; −4 **e teto de NH 9** no corpo a corpo. ⚠️ Magnitude positiva é dado torto e nunca vira bônus.
- **`domain/rules/AtiradorRules.kt`** — As vantagens **Atirador** (25 pts) e **Arqueiro Heroico** (20 pts). Prec sem Apontar (metade com arma de duas mãos ou automática), a troca *"em vez de"* com o Avançar e Atacar, e o arqueiro acumulando os segundos **um turno mais cedo**.
- **`domain/rules/GolpeRapidoEAparaRules.kt`** — **Golpe Rápido** (MB p.371) e **apara repetida** no mesmo turno (MB p.377). Os dois mudam com Treinado por um Mestre / Mestre de Armas.
- **`domain/rules/MaoInabilRules.kt`** — A penalidade de **mão inábil** (MB p.14) e quem a anula (Ambidestria). Inclui `SemUmDedoRules`.
- **`domain/rules/MiraImpedimentosRules.kt`** — **Zarolho** e **Assassino Relutante** (Lote D-MIRA): as duas conversam com a mesma caixinha do Apontar, em direções opostas — uma é cancelada por ela, a outra a proíbe.
- **`domain/rules/DisopiaRules.kt`** — A parte de **combate** da Disopia (MB p.135); o −6 do teste de Visão fica no `SentidoRules`. Duas variantes pelo mesmo custo, e a ficha não guarda qual — o app pergunta.
- **`domain/rules/DesastradoRules.kt`** — **Completamente Desastrado** (MB p.133): qualquer fracasso em teste de DX ou perícia baseada em DX vira **falha crítica**.
- **`domain/rules/IluminacaoRules.kt`** — A luz da cena (MB p.395/549) e as vantagens que enxergam no escuro. Sem isto a Visão Noturna era decorativa: não havia escuridão para cancelar.
- **`domain/rules/TiposDeDano.kt`** — `enum DanoTipo` (cont, corte, pi-, pi, pi+, pi++, perf) com o multiplicador de ferimento, e `ToleranciaFerimentos`. ⚠️ Nasceu em `domain/combat/` e mudou de pacote no MB-7, pelo mesmo motivo do `LocaisDeAtaque`: é tabela do livro, e a ficha não pode depender do motor da Saga para ler o botão PV.
- **`domain/rules/ModificadoresDeCombate.kt`** — As duas listagens que fecham o capítulo de combate (MB p.547-549). ⚠️ Traz **só o que ainda não é automático** em outro arquivo — há teste proibindo ids que colidam. O **teto de 9 do asterisco** é máximo, não penalidade: NH 20 vai a 9, NH 10 vai a 5.
- **`domain/rules/TamanhoDoAlvoRules.kt`** — Os 13 degraus do Modificador de Tamanho (MT), com exemplo em cada um. O MT do alvo entra direto no ataque à distância.
- **`domain/rules/TiroContinuoRules.kt`** — Quantos projéteis de uma rajada acertam (`1 + margem/Recuo`, limitado pelos tiros disparados) e o **mau funcionamento** por NT, com a tabela de falha saindo sozinha no gatilho.
- **`domain/rules/QuedaRules.kt`** — Dano por **queda e colisão** (MB p.430-432). 🔴 A tabela do livro e a fórmula `√(21,4 × g × h)` discordam em 3 dos 15 pontos conferidos; vale a **tabela** para 1G, e a fórmula só para outra gravidade ou acima de 112 m. As frações abaixo de 1d são três faixas fixas, não arredondamento.
- **`domain/rules/FadigaRules.kt`** — O catálogo do botão **PF** (MB p.426-428). 🔴 Guarda a **origem** de cada PF perdido, porque fome não volta com descanso e sono não volta com comida. `reconciliar()` impede o painel de devolver de graça o PF gasto por fora (magia, combate, deslize).
- **`domain/rules/FerimentoPorLocalRules.kt`** — A conta inteira do botão **PV** (MB p.399-400 e 419-422): RD → multiplicador do tipo **e** do local → teto do membro → decepamento, mais choque e teste de nocaute. 🔴 O mínimo que incapacita é `floor(PV × fração) + 1`, não "arredondado para cima" — arredondar erra 1 ponto com PV **par**, que é metade dos personagens.
- **`domain/rules/MapaDaSilhueta.kt`** — Onde cada parte do corpo fica na arte da silhueta do botão PV (Lote PV-1a). 🔴 As 12 linhas de corte foram **medidas** na imagem por `docs/arte/silhueta/mapa_silhueta.py`, não desenhadas no olho — e a ferramenta pinta o mapa por cima da arte para conferência humana, que foi o que pegou a boca caindo dentro do pescoço. ⚠️ **Tocar e pintar são diferentes**: o toque cobre o retângulo inteiro (a mão tem só 44 dp de traço) e só o destaque é recortado no corpo. ⚠️ "Esquerdo" é o lado **do personagem** — aparece à direita da imagem.
- **`domain/rules/CoberturaDaArmadura.kt`** — Traduz o texto livre do `armaduraLocal` da ficha (*"corpo, membros"*, *"traje completo"*, e literalmente **`"crnio"`** e **`"pescoo"`**) para os locais do corpo, e lê o `rdRaw` (`"4/2*"`, `"5D"`, `"+20"`). ⚠️ Quem protege o **tronco** protege os **vitais**. A varredura dos 72 itens reais do catálogo é o teste que vale.

### Perícias: o rodapé "Modificadores:"

- **`domain/rules/ModificadoresSituacionais.kt`** — Os modificadores de **situação** de 45 perícias (Lote P-SIT). O app guardava esse texto e nunca o lia.
- **`domain/rules/QualidadeDoEquipamento.kt`** — A tabela de **Modificadores de Equipamentos** (MB p.346), citada por 32 perícias. Cinco níveis, coluna dupla (técnico −10 / comum −5).
- **`domain/rules/FamiliaridadeCulturalRules.kt`** — O **−3** que todo mundo paga fora da própria cultura (MB p.24); a vantagem **apaga** a penalidade em vez de somar bônus.

### Deslocamento e estados

- **`domain/rules/DeslocamentosRules.kt`** — Todos os deslocamentos do personagem (Lote DESL-2, MB p.17-19 e p.395), consumido pelo `DialogoDeslocamentos`.
- **`domain/rules/DeslocamentosEspeciais.kt`** — Voo (Velocidade Básica × 2, MB p.99) e escalada — dois números que o app tinha tudo para calcular e deixava para o jogador.
- **`domain/rules/EstadosTemporarios.kt`** — As nove desvantagens que só valem **enquanto o jogador diz que valem** (Lote D-ESTADO), com graus (Dor Crônica Suave/Grave/Excruciante). ⚠️ A chave de Vontade é **`VON`**, não `VONT` — o `getAtributo` só conhece `VON`, e a chave errada sumia sem erro.
- **`domain/rules/IncompatibilidadeDeTracos.kt`** — Pares que o livro **proíbe** na mesma ficha. Hoje só **Abascanto × Aptidão Mágica** (MB p.85). ⚠️ Só o que o livro proíbe — "a critério do Mestre" não entra.

### Texto e explicação

- **`domain/rules/OrigemDosNumeros.kt`** — De onde vem cada pedaço dos números de **defesa** e de **item** (Lote NOTA-2) — o irmão do NOTA-1, que explicava as perícias.
- **`domain/rules/MensagensDefesa.kt`** — Explica **por que** uma defesa ativa não está disponível. Separado do `CombatRules` de propósito: lá é fórmula pura, aqui é texto para o usuário.
- **`domain/rules/FichaTecnicaDaArma.kt`** — A ficha técnica da arma **em português** (Lote ARMA-2): `Tiros 80(3)` vira *"80 tiros, 3 turnos para recarregar"*, `CL 2` vira *"restrito"*, e o `×15/×20` do arco vira metros com a ST da ficha. Campo ausente sai **—**, nunca **0**.

---

## 6. Domain — Trait Rules

*Regras especiais por ID de vantagem. Cada `TraitRule` implementa a interface e é registrada no Registry.*

- **`domain/rules/traits/TraitRule.kt`** — Interface base: `calculateCost`, `getAttackOptions`, `getDefenseOptions`, `getDamageOptions`, `getDodgeModifier`, `getBlockModifier`, `getParryModifier`, `getSkillModifiers`, `getDamageBonusPerDie`. Todas com default `null`/`emptyList`/`0`.

- **`domain/rules/traits/TraitRuleRegistry.kt`** — Singleton que registra todas as regras e expõe métodos agregadores: `getSkillBonus`, `getParryBonus`, `getDodgeBonus`, `getBlockBonus`, `getDamageBonusPerDie`. Usado pelo `Personagem` e pelo `FichaCombatDelegate`.

- **`domain/rules/traits/AtaqueInatoRule.kt`** — Custo e opções de ataque para `ataque_inato` (vantagem composta, calcula por dados/modificadores armazenados nos metadados).

- **`domain/rules/traits/GolpeadoresRule.kt`** — Custo e dano de `golpeadores` (Striker). Lê tipo de golpeador e metadados para calcular dano.

- **`domain/rules/traits/DentesRule.kt`** — Custo e dano de `dentes` (Bite). Calcula dano por tipo de mordida.

- **`domain/rules/traits/GarrasRule.kt`** — Custo de `garras` pelo metadado `tipoGarras` (cascos=3, cegas=3, afiadas=5, pontudas=8, longas_pontudas=11; default afiadas=5). Expõe as opções de dano (`getDamageOptions`) por tipo de garra (corte/perfuração, com bônus de +1/dado em cascos, cegas e longas).

- **`domain/rules/traits/FlexibilidadeRule.kt`** — Bônus de perícia para `flexibilidade` (Contorcionismo, Acrobacia).

- **`domain/rules/traits/ApararAmpliadoRule.kt`** — Bônus de Apara para `aparar_ampliado`.

- **`domain/rules/traits/BloqueioAmpliadoRule.kt`** — Bônus de Bloqueio para `bloqueio_ampliado`.

- **`domain/rules/traits/EsquivaAmpliadaRule.kt`** — Bônus de Esquiva para `esquiva_ampliada`.

- **`domain/rules/traits/MestreDeArmasRule.kt`** — Bônus de dano por dado (`getDamageBonusPerDie`) para `mestre_de_armas`, filtrado por grupo de arma e perícia.

- **`domain/rules/traits/TelecomunicacaoRule.kt`** — Custo de `telecomunicacao` pelo metadado de alcance/tipo.

- **`domain/rules/traits/IdiomaRule.kt`** — Custo de `idioma` (GURPS p.23). Cada instância = um idioma adicional; o custo soma as duas "metades" (fala + escrita) via `metadeCusto` no companion (rudimentar=1, com sotaque=2, materna=3, nenhum=0). Metadados: `nomeIdioma`, `nivelFalado`, `nivelEscrito`.

> **Nota:** as 11 regras acima são registradas em `TraitRuleRegistry.init` (AtaqueInato, Golpeadores, Dentes, Flexibilidade, Garras, ApararAmpliado, BloqueioAmpliado, EsquivaAmpliada, MestreDeArmas, Telecomunicacao, Idioma).


- **`traits/TraitEffectModels.kt`** — Os modelos do campo `efeitos` (`EfeitoDeclarado`, `TipoEfeito`, `EscopoEfeito`, `BonusCondicional`) declarado em `vantagens.v3.json` / `desvantagens.v2.json`. Base da **arquitetura híbrida**: bônus simples viram DADO no JSON; casos complexos continuam classe Kotlin. Resolução por `porAutocontrole` → `porOpcao` → `porNivel`.
- **`traits/EfeitoInterpretador.kt`** — Transforma o campo `efeitos` numa `TraitRule` sem escrever uma classe por vantagem. ⚠️ Quando existem os dois, **a classe Kotlin vence** o JSON — validado por `scripts/validar_efeitos.py`.
- **`traits/TracoSelecionado.kt`** — A interface que uma `TraitRule` enxerga, seja o traço vantagem ou desvantagem. Existe por um buraco silencioso (Lote D-0): os agregadores varriam só `personagem.vantagens`, então regra registrada com id de DESVANTAGEM nunca era chamada — sem erro nenhum.
- **`traits/BracalCustoRules.kt`** — O **custo** de ST/DX Braçal (MB p.89 e p.56). O app tratava como escolha de três valores fixos; no livro são o preço de **cada +1**, e o que muda é **quantos braços**.
- **`traits/CegueiraRule.kt`** — Os **−6** da Cegueira em todas as perícias de combate (MB p.127). É classe Kotlin, e não `efeitos`, porque declarar ~70 alvos por nome seria um paredão de JSON que sai de sincronia no primeiro catálogo novo.
- **`traits/MaoFracaRule.kt`** — **Mão Fraca** (MB p.151): −2 por nível, até 3, nas perícias de **arma de combate corpo a corpo**. ⚠️ Migrada do JSON para Kotlin porque a lista escrita de memória ("perícias delicadas") não era a do livro.

---

## 7. Domain — Loaders

*Carregamento e resolução dos catálogos JSON dos assets.*

- **`domain/loaders/CatalogLoaders.kt`** — Carrega todos os catálogos de assets: `vantagens.v3.json` (+ extras de artes marciais), `desvantagens.v2.json`, `pericias.json` (+ suplementares), `magias.json`, `tecnicas_*.json` (múltiplos arquivos), `armas_*.json`, `armaduras.json`, `escudos.json`. Registra erros de carga sem lançar exceção. Faz mojibake fix nos textos carregados.

- **`domain/loaders/MetacaracteristicaCatalogo.kt`** — Carrega `metacaracteristicas.v1.json` (catálogo de metacaracterísticas prontas como Gigante, Anão, etc.) e resolve cada uma como `ModeloRacial` usando `RacaCatalogo.resolver`. Formato "enxuto": sem custos no JSON, recalculado em runtime.

- **`domain/loaders/RacaCatalogo.kt`** — Carrega `racas.v1.json` (catálogo de raças jogáveis). Resolve `RacaDefinicao` → `ModeloRacial` casando IDs contra os catálogos de vantagens/desvantagens/perícias via `DataRepository`. Custo recalculado pelo `CharacterRules` — imune a custo salvo errado. É também o schema de raças para o Forjador IA.

- **`domain/loaders/ForjadorTemplateCatalogo.kt`** — **[+ 2026-06-09]** Carrega `forjador_templates.json` (60 templates/arquétipos de personagem prontos). `escolher(prompt, templates)` faz match por palavra-chave (id/nome/descrição/tags) e retorna o template mais próximo do pedido — é **o SISTEMA (código) que escolhe, não a IA**. `formatarParaPrompt(t)` serializa o template como bloco de texto injetado no prompt do Forjador (na 1ª iteração) como "ponto de partida". `pontosBase` é só REFERÊNCIA — o budget real é o do pedido do usuário. Data classes: `ForjadorTemplate` (+ Pericia/Vantagem/Desvantagem). ⚠️ Todos os IDs dos templates são validados contra o catálogo (ver `project_forjador_pendencias`).


- **`domain/loaders/ArmasCatalogLoader.kt`** **[+ Lote ARMA-1]** — O leitor dos **três** catálogos de arma, separado do `CatalogLoaders` por dois motivos: tamanho (aquele arquivo passava de 1.100 linhas) e **teste** — aqui a entrada é uma `String`, não um `Context`, então a suíte roda sobre o asset real. Lê `nt`, `cl`, `municaoKg`, `precisaoAcessorio` (o `+N` da mira embutida), os `raw` do livro, as flags † / ‡ e a lista de **modos de ataque**.

---

## 8. Domain — Filters

- **`domain/filters/TextNormalizer.kt`** — (Lote 314) Normalizador único do projeto. 4 presets: `SIMPLE` (sem acento + lowercase), `BUSCA_PADRAO` (+ mojibake fix + colapsa não-alfanuméricos), `PERICIA_RAW` (preserva `/+_-`), `ARMA_GRUPO` (strip parênteses + despluralização para Mestre de Armas). Consumido por `CatalogFilters`, `DialogsTecnicas`, `SkillEngine`, `DataRepository`. **Pendência:** `MestreDeArmasRule.normalize` ainda tem cópia local — migrar para preset `ARMA_GRUPO` quando houver ficha de teste pronta.

- **`domain/filters/CatalogFilters.kt`** — Fachada pública de busca/comparação. `normalizarBusca` e `contemBusca` delegam ao `TextNormalizer.BUSCA_PADRAO`. Usados em todo o app para filtros de catálogo (vantagens, perícias, magias, armas, armaduras, escudos). Inclui também `normalizarLocal` (caso específico de armaduras — substitui espaço por `_`, não migrado para o TextNormalizer).

---

## 9. Domain — Magias (Nexus Arcano)

- **`domain/magias/NexusArcanoModoAlvoAdapter.kt`** — Adapter entre o `NexusArcanoEngine` (módulo separado) e o ViewModel. Traduz `List<MagiaDefinicao>` em `ArcanoCatalogo`, chama o engine para calcular trilha ótima (A*/guloso), e retorna `NexusArcanoModoAlvoSnapshot` com relacionados, chaves, trilha mínima e avisos.

- **`domain/magic/MagicCasting.kt`** — Lógica e regras de restrição de conjuração de magias no novo motor detalhado.
- **`domain/magic/MagicClass.kt`** — Definições de classes e propriedades mecânicas das magias.
- **`domain/magic/MagicCore.kt`** — Motor principal com a lógica da mecânica estendida de magias.
- **`domain/magic/MagicMechanics.kt`** — Regras de mecânica de magias (resistências, redutores, modificadores de área).
- **`domain/magic/MagicTime.kt`** — Sistema de controle de tempo de conjuração, duração e manutenção de magias.

---

## 10. Domain — Roll

- **`domain/roll/RollDispatchPolicy.kt`** — Política de retry e mensagens de erro para envio de rolagens ao Discord. `deveRetentar` retorna `true` só para timeout (statusCode null). `mensagemErro` mapeia HTTP 401/400/500/502 para mensagens amigáveis.

- **`domain/roll/CriticoRules.kt`** — **[+ 2026-06-09]** Regra de crítico COMPLETA do GURPS (com NH) + automação das tabelas. `classificar(soma, nhEfetivo)` → DECISIVO/FALHA_CRITICA/NORMAL (decisivo 3-4 sempre, 5@NH≥15, 6@NH≥16; falha 18 sempre, 17@NH≤15, soma≥NH+10). `ehTesteDeCombate(tipoLabel)` (Ataque/Defesa/Técnica/Magia). `rolarTabela(context, resultado)` rola **3d6** e monta o texto das DUAS tabelas (Decisivo→Golpe Fulminante + na Cabeça; Crítico→Erro Crítico + Desarmado) carregadas de `tabelas_criticas.json`. Disparado por `TabRolagem.executarRolagem` após uma rolagem de combate dar decisivo/crítico (2ª mensagem ao Discord).

---

## 11. Domain — MestreIA (Núcleo da IA)

*Para detalhes técnicos do fluxo de IA (prompts, loop de tool-use, FTS, decisões de arquitetura), ver `ARQUITETURA_MESTRE_IA.md`.*

- **`domain/MestreIAContextFilter.kt`** — Gera a string de contexto da ficha enviada para a IA: nome, atributos, HP/FP atual, vantagens, desvantagens, principais perícias. No modo `conversa` inclui aparência e histórico. Filtra metadados técnicos.

- **`domain/MestreIAGeneratorUseCase.kt`** — Orquestra o fluxo FORJADOR (criação de personagem). Usa `MestreIAClient` com modo `geracao`/`analise`, executa `ForjadorToolExecutor` a cada tool call recebida (ler ficha, buscar catálogo, GPS magia, editar ficha), faz até N iterações do loop de tool-use. Valida resposta final via `MestreIAValidacaoReport`.

- **`domain/MestreIAGraphEngine.kt`** (603 linhas) — ⚠️ **LEGADO p/ Auditor desde Lote 325.** Motor RAG semântico (BM25 + HNSW + diversificação + "Ponte de Ferro"). Hoje alcançado por `gerarCatalogoDireto` (morto) e **ATIVAMENTE pela Voz** (`GeminiLiveTools` instancia o próprio `MestreIAGraphEngine` e chama `buscarDiretoNoCodex`). O scoring BM25 daqui foi **copiado** para `MestreIARepository.rankearPorBM25` (Lote 327) — ajustar ranking do Auditor é LÁ, não aqui. Flag `MODO_HNSW_PURO` setada em `FichaIADelegate.kt:55`.

- **`domain/MestreIAPlanner.kt`** (879 linhas) — ⚠️ **QUASE MORTO desde Lote 319.** A lógica de planejamento (dicionários hardcoded) causava alucinação léxica e foi removida do fluxo. Hoje só a data class `TermoPonderado` é usada como TIPO (parâmetro com default vazio que nunca recebe valor real). Nenhum `PlanoDeBusca` roda no Auditor atual.

- **`domain/MestreIARuleAuditor.kt`** — Auditor fiscal (Lote 55). Compara a `MestreIAResponse` sugerida pela IA contra os cálculos reais do `CharacterRules`. Gera lista de `AuditNote` com campo, valor sugerido vs. correto. Usado pelo Forjador para detectar custo errado de atributos.

- **`domain/MestreIAUseCase.kt`** — Orquestra o fluxo AUDITOR. **Desde Lote 325 NÃO usa RAG semântico:** loop de tool-use com `localizar_no_codex` (FTS4 AND/OR + ranking BM25) e `ler_pagina` (texto completo), via `MestreIARepository` — até **8 iterações** (`MAX_TOOL_CALLS`). **Lote 328:** trava anti-confabulação (`leuAlgumaPagina`) — se nunca leu página, força declarar "não localizei" em vez de citar de memória. `ehErroDeApi()` preciso. ⚠️ Contém funções legadas no mesmo arquivo: `executarBuscaCodex` (cases das 5 tools de embedding nunca disparam — Lote 317→325) e `gerarCatalogoDireto`/`reescreverQueryParaGurps` (MORTAS, zero callers).

- **`domain/MestreIACitationValidator.kt`** — (Lote 315, VIVO) Verificador de Citações. Extrai citações `[Livro, Pág]` da resposta e compara com as páginas dos chunks lidos; o que não bate vira aviso "⚠️ não verificadas" anexado à resposta. Não bloqueia — apenas avisa.

- **`domain/MestreIATopicIndex.kt`** — ⚠️ **MORTO desde Lote 272.** Lê `topic_index.json` para "páginas garantidas", mas NENHUM arquivo o referencia (nem `carregar()`). Determinismo por tópico foi rejeitado. Não reviver sem rediscutir.

- **`domain/MestreIASemanticEngine.kt` / `MestreIAVectorEngine.kt`** — ⚠️ **DORMENTES p/ Auditor desde Lote 325.** Reranking cosseno e busca HNSW (ObjectBox). Só via GraphEngine (Forjador/Voz). Os utilitários `floatArrayToByteArray`/`byteArrayToFloatArray` do SemanticEngine ainda são usados na importação de embeddings (FichaDatabase).

- **`domain/MestreIAValidacaoReport.kt`** — Data classes do relatório de validação do Forjador: `ItemValidacao` (entrada, idEncontrado, status, mensagem) e `RelatorioValidacao` (vantagens/desvantagens/perícias/magias/técnicas, totalOk, totalFallback, alertaBudget). `StatusValidacao` enum: OK, FUZZY, FALLBACK, ERRO.

---

## 12. Domain — Tools (Forjador)

- **`domain/tools/ForjadorTools.kt`** — Define os schemas das **6 ferramentas** do Forjador: `forjador_ler_ficha`, `forjador_buscar_catalogo`, `forjador_gps_magia`, `forjador_editar_ficha`, `forjador_buscar_racas`, `forjador_aplicar_modelo_racial`. Exporta formato nativo Gemini (`getGeminiTools`) e formato OpenAI (`getOpenAITools`).

- **`domain/tools/ForjadorToolExecutor.kt`** — Executor das ferramentas do Forjador. Mapeia nome da tool → implementação Kotlin: `lerFicha` (lê seção do personagem), `buscarCatalogo` (busca em vantagens/desvantagens/perícias/magias + injeta `RegrasEspeciaisSchema`), `gpsMagia` (trilha mínima via NexusArcano), `editarFicha` (aplica mudanças no personagem via ViewModel), `buscarRacas` e `aplicarModeloRacial` (catálogos de raças/metacaracterísticas via `RacaCatalogo`/`MetacaracteristicaCatalogo`, carregados lazily). Faz read-back pós-edição (`lerSecao`).

- **`domain/tools/RegrasEspeciaisSchema.kt`** — Schemas textuais das regras especiais de vantagens/desvantagens que têm custo calculado por metadados (Aliado, Inimigo, Dependente, Garras, Resistente, Ataque Inato, etc.). Injetado pelo `buscarCatalogo` quando o traço tem `specialRule`, para que o modelo saiba exatamente quais metadados preencher.

---

## 13. Data — Repositórios

- **`data/DataRepository.kt`** — Repositório central de catálogos. Carrega (lazy, com Mutex) vantagens, desvantagens, perícias, magias, técnicas, armas, armaduras, escudos, raças, metacaracterísticas. Expõe métodos de filtragem (`filtrarVantagens`, `filtrarDesvantagens`, `filtrarPericias`, `filtrarMagias`, `filtrarArmasCatalogo`, etc.), criação de objetos selecionados (`criarVantagemSelecionada`, `criarPericiaSelecionada`) e validação de pré-requisitos.

- **`data/MestreIARepository.kt`** — Repositório do Códex + **motor de busca VIVO do Auditor (Lotes 325-327).** Sincroniza `chunks.jsonl` → `manual_chunks` (FTS4) com Mutex (`CODEX_VERSION_CURRENT = 3`). Funções do Auditor: **`localizarNoCodex`** (FTS4 AND, fallback OR, + **`rankearPorBM25`** que ordena por relevância — Lote 327), **`lerPaginas`** (texto completo de página/intervalo), `buscarPorPaginaESource`. Mantém também `buscarNoCodexDireto`/`buscarPorPagina` (usados pelo GraphEngine legado).

- **`data/MestreIAQueryEngine.kt`** — Preparação de queries FTS4 (`prepararQueryFTSAgressiva`, OR + sinônimos). ⚠️ Usado pelo GraphEngine (Forjador/legado), **NÃO pelo `localizarNoCodex` do Auditor**, que tokeniza por conta própria. Um dos 3 dicionários de sinônimos do projeto.

---

## 14. Data — Network

- **`data/network/MestreIAClient.kt`** — Cliente HTTP para APIs de IA. Suporta Gemini nativo (`generativelanguage.googleapis.com`) e OpenRouter/OpenAI-compatible. Monta JSON do request (`gerarJsonGoogleNative`, `gerarJsonOpenRouter`), lida com tool calls na resposta, captura tokens de uso. Modo stream desabilitado (JSON puro). Log de auditoria do prompt (tamanho, modelo, tokens).

- **`data/network/MestreIAPromptsAuditor.kt`** — Prompt de sistema do AUDITOR (reescrito Lotes 325/328): loop `localizar`→`ler`, "REGRA DE OURO" (não responder sem ter lido), anti-confabulação (citar só o que leu). **CATEGORIAL, sem exemplos hardcoded** (lição do Lote 318 — exemplo vira cola/viés).

- **`data/network/MestreIAPromptsForjador.kt`** — Prompt de sistema do FORJADOR (modo `geracao`/`analise`). Define o comportamento da IA como criador de fichas, protocolo de uso das tools (`forjador_*`), ordem de operações e formato de JSON final.

- **`data/network/MestreIAResponse.kt`** — Data classes da resposta estruturada da IA: `MestreIAResponse` (envelope completo da ficha gerada), `AtributosIA`, `VantagemIA`, `DesvantagemIA`, `PericiaIA`, `MagiaIA`, `TecnicaIA`. Usado no fluxo Forjador e no `TOOL_FILL_SHEET` (Auditor).

- **`data/network/MestreIATools.kt`** — Schemas das ferramentas. **AUDITOR atual (Lote 325): `getAuditorToolsOpenAI`/`getAuditorToolsGemini`** = `localizar_no_codex` + `ler_pagina` + `inspecionar_personagem` + `consultar_nexus_arcano`. Modo `analise` usa `getAuditorUnificadoToolsOpenAI/Gemini` = ForjadorTools + localizar/ler + nexus (base corrigida no Lote 350 — antes apontava para o toolset legado de embedding, engano do commit d9d999c). O toolset legado (`getOpenAITools`/`getGeminiTools`, renomeado `getLegacyEmbeddingTools*` no Lote 349) foi DELETADO no Lote 350 junto com `getSheetSchema*`/`getArrayOf*` e as constantes `TOOL_MANUAL_DIRETO`/`TOOL_REGRAS_*`. A seleção por modo acontece em `MestreIAClient`.

- **`data/network/DiscordRollApiClient.kt`** — Cliente HTTP para o servidor Discord do projeto. Envia `DiscordRollPayload` (personagem, tipo de teste, dados, resultado) via POST. Também busca lista de `DiscordVoiceChannel` disponíveis. Data classes: `DiscordRollPayload`, `DiscordRollSendResult`, `DiscordVoiceChannel`. **[+ 2026-06-08]** `postPortrait(baseUrl, apiKey, characterName, imageDataUri)` → `POST /api/portrait` (sobe o retrato data:base64 que o bot reanexa nos embeds de rolagem).

- **`data/network/GeminiImageService.kt`** — **[+ 2026-06-09] MESTRE PINTOR.** Gera retrato artístico do personagem via Gemini Image API (`gemini-3.1-flash-image`, chave PAGA `MESTRE_IA_GEMINI_IMAGE_KEY`, ~$0,067/imagem, proporção 9:16). `gerarRetrato(prompt)` → POST `:generateContent` com `responseModalities=["IMAGE","TEXT"]`, devolve a imagem (base64). Fluxo: `FichaIADelegate.gerarRetratoIA()` → `GeminiImageService.gerarRetrato()` → `ImagemPersonagemStore.salvarImagem()` → `FichaViewModel.atualizarImagemPersonagem()`. Entradas na UI: dialog pós-Forjador (`DialogRetratoIA` em FichaScreen) e modo "pintor" no ChatInputBar (DialogsMestreIA).

---

## 15. Data — Storage (Room / Persistência)

- **`data/storage/RostoDetector.kt`** — Acha o rosto (ou, na falta dele, o assunto) para enquadrar retratos. Compartilhado pelo `ImagemPersonagemStore` (cabeçalho da ficha) e pelo `TokenImageStore` (tokens do VTT) — antes cada um tinha a sua cópia. É uma **cascata**, não uma chamada só: o ML Kit Face Detection é treinado em rosto humano e falha em criatura, e aí entra a Subject Segmentation e depois o recorte por saliência.

- **`data/storage/FichaDatabase.kt`** — Configuração Room **v26** (Lote 259 adicionou `vec_chunks`; Lote 356 subiu p/ v26 com as tabelas da Saga — ver §32.2, migrações 24→25→26 explícitas). Entidades: `FichaEntity`, `ManualChunkEntity`, `GraphNodeEntity` (legado), `ChatSessionEntity`, `ChatMessageEntity`, `VecChunkEntity`. DAOs expostos: `fichaDao`, `manualChunkDao`, `graphNodeDao` (legado), `chatHistoryDao`, `vecChunkDao`. `fallbackToDestructiveMigration`. Método `prePopulateManual` (importa `chunks.jsonl` → `manual_chunks` FTS4 + embeddings → `vec_chunks`; reimporta só embeddings se chunks existem mas vec está vazio). `graphNodeDao` declarado mas GraphNode está descontinuado.

- **`data/storage/FichaDao.kt`** — DAO Room para fichas: `upsert`, `getJson`, `deleteByName`, `listNames` (ordenado por `updatedAt` DESC).

- **`data/storage/FichaEntity.kt`** — Entidade Room `fichas`: `nomeArquivo` (PK), `json` (texto completo), `updatedAt` (timestamp).

- **`data/storage/FichaStorageRepository.kt`** — Repositório de persistência de fichas. Migra fichas antigas de `SharedPreferences` → Room (operação única). `salvarFicha`, `carregarFicha`, `excluirFicha`, `listarFichas`. Normaliza nomes de arquivo para compatibilidade cross-versão.

- **`data/storage/ManualChunkDao.kt`** — DAO FTS4 para o Códex. `buscarRegras` (query FTS4 full-text), `buscarPorPagina`, `buscarPorPaginaESource`, `getChunkById`, `getCount`, `clearAll`. Tabela virtual FTS4 com `search_text` (texto + source_title).

- **`data/storage/ManualChunkEntity.kt`** — Entidade FTS4 `manual_chunks`: `chunk_id`, `text`, `source_title`, `source_id`, `page_number`, `search_text` (campo de busca composto).

- **`data/storage/MetacaracteristicaStore.kt`** — Persistência leve de metacaracterísticas criadas pelo usuário (arquivo `metacaracteristicas_usuario.json` em `filesDir`). Lista, salva (por nome, case-insensitive) e exclui. Usa JSON direto em vez de Room (sem migration necessária).

- **`data/storage/ImagemPersonagemStore.kt`** — **[+ 2026-06-08]** Processa e armazena o retrato do personagem em `filesDir/portraits/`. `salvarImagem(context, uri)` decodifica (com `inSampleSize`), corrige rotação via EXIF (`androidx.exifinterface`), enquadra o assunto principal (ML Kit **Subject Segmentation** — `play-services-mlkit-subject-segmentation`) refinando pelo rosto (ML Kit **Face Detection**), recorta na proporção do cabeçalho e salva **DUAS versões**: recortada (cabeçalho) e inteira (tela cheia, maior lado 1600px) — retorna `ImagensSalvas(recortadaUri, originalUri)`. `bytesBase64(caminho)` gera `data:image/jpeg;base64,...` para o Discord (mesma estratégia do VTT `resolveTokenImagePayload`). `excluirImagem(caminho)`. **[+ 2026-06-09]** `salvarDeBase64(context, dataUri)` — restaura a imagem EMBUTIDA numa ficha importada: decodifica o base64, grava arquivo temp e reusa `salvarImagem` (re-recorta o rosto + gera as 2 versões). Funções internas: `detectarAssunto`/`boundingBoxDaMascara` (FloatBuffer da máscara), `detectarRosto`, `recortarFaixa` (centro horizontal no assunto/rosto; vertical com margem acima do rosto, ou topo do assunto, ou topo da imagem), `redimensionar`/`redimensionarMaiorLado`. Sem Room, sem migration.

- **`data/storage/ChatHistoryDao.kt`** — DAO Room para histórico de chat: sessões (`getAllSessions`, `createSession`, `updateSessionTitle`, `updateSessionTimestamp`) e mensagens (`insertMessage`, `getMessagesForSession`).

- **`data/storage/ChatHistoryEntity.kt`** — Entidades Room: `ChatSessionEntity` (`chat_sessions`: id, title, createdAt, updatedAt) e `ChatMessageEntity` (`chat_messages`: id, sessionId, role, text, modelName, createdAt).

- **`data/storage/CenarioImageStore.kt`** — Persistência e gerenciamento de imagens de cenário do VTT/Combate Tático.

- **`data/storage/TokenImageStore.kt`** — Persistência e gerenciamento de imagens de tokens da Mesa Virtual.

- **`data/storage/GraphNodeDao.kt`** — ⚠️ LEGADO — NÃO UTILIZADO. DAO Room para o grafo de conhecimento (descontinuado). Declarado no `FichaDatabase` mas nunca chamado pelo código ativo.

- **`data/storage/GraphNodeEntity.kt`** — ⚠️ LEGADO — NÃO UTILIZADO. Entidade Room `graph_knowledge` do grafo descontinuado. Mantida só para não quebrar a migration do Room.

- **`data/storage/VecChunkEntity.kt`** — (Lote 259) Entidade Room `vec_chunks`: `chunk_id` (PK) + `embedding` (ByteArray little-endian). Guarda os embeddings semânticos importados do `chunks.jsonl`. ⚠️ DORMENTE p/ Auditor desde Lote 325 (embeddings só usados via GraphEngine/Voz). Embeddings reais têm 3072 dims (Gemini) — o comentário "384 floats" no arquivo está desatualizado.

- **`data/storage/VecChunkDao.kt`** — (Lote 259) DAO Room dos embeddings: `insertAll`, `getByIds`, `getAll`, `getCount`, `clearAll`.

- **`data/storage/VecChunkOBEntity.kt`** — Entidade **ObjectBox** (não Room) para busca vetorial HNSW: `id`, `chunkId` (`@Index`), `embedding` (`@HnswIndex(dimensions = 3072)`). Usada pelo `MestreIAVectorEngine`. ⚠️ DORMENTE p/ Auditor.

- **`data/storage/ObjectBoxStore.kt`** — Singleton do `BoxStore` ObjectBox (`gurps_vec_store`), usado exclusivamente para o vector search HNSW. `init`/`get`/`close`. Room continua o banco principal. ⚠️ DORMENTE p/ Auditor.

---

## 16. Model

*Data classes puras. Sem lógica de negócio (exceto `Personagem.kt` que tem cálculos derivados).*

- **`model/Personagem.kt`** — Modelo raiz. Todos os campos do personagem GURPS 4ª Ed. (atributos primários/secundários, vantagens, desvantagens, qualidades, peculiaridades, perícias, técnicas, magias, equipamentos, modelo racial, HP/FP de rolagem, notas). Tem propriedades calculadas (`pontosVida`, `pontosFadiga`, `velocidadeBasica`, etc.) que usam `CharacterRules` e `TraitRuleRegistry`. `toJson`/`fromJson` para serialização. **[+ 2026-06-08]** Campos novos `imagemPersonagemUri` (foto RECORTADA do cabeçalho) e `imagemPersonagemOriginalUri` (foto INTEIRA p/ tela cheia) — ambos `file://` em `filesDir/portraits/`, default vazio (retrocompatível). **[+ 2026-06-09]** Campo `imagemPersonagemBase64` — preenchido APENAS na exportação (foto viaja embutida na ficha); limpo no import (não incha persistência local).

- **`model/PersonagemInterop.kt`** — Importação/exportação versionada. `importarJson` suporta envelope `{"schema":"gurps-ficha","character":{...}}` e fallback para JSON legado sem envelope. `exportarJson` gera o envelope com metadados (schemaVersion, exportedAtUtc, appVersion, uiVariant).

- **`model/CatalogosSuplementares.kt`** — Data classes dos catálogos suplementares: `PericiaSuplementarItem`, `TecnicaCatalogoItem`, `PericiaV2RuleMapItem` (e subclasses de regra: `PericiaV2TipoRegra`, `PericiaV2PreRequisitoRegra`, `PericiaV2PreDefinidoRegra`).

- **`model/ArmaCatalogoItem.kt`** — Data class de arma do catálogo: nome, dano, ST mínimo, peso, custo, grupo (perícia), aparar. **[+ 2026-06-14, Lote 371]** stats de combate lidos dos JSONs normalizados: `alcanceCorpoACorpo` ("C"/"1"/"1,2"), `duasMaos` (†/‡), `precisao` (Acc), `meioDanoMetros` (1/2D), `maximoMetros` (Máx), `alcanceMultStRaw` (×ST p/ arcos), `cadenciaTiro` (CdT), `tirosRaw`, `magnitude` (Bulk), `recuo` (Rcl). `Equipamento` (em `Personagem.kt`) ganhou os campos `arma*` correspondentes (anuláveis, backward-compatible) populados em `FichaEquipmentDelegate.adicionarEquipamentoArma`.

- **`model/ArmaduraCatalogoItem.kt`** — Data class de armadura: nome, RD, peso, custo, locais cobertos, componentes (lista de peças individuais), tags.

- **`model/EscudoCatalogoItem.kt`** — Data class de escudo: nome, BD, peso, custo, habilidade de bloqueio.

- **`model/MestreIAChunk.kt`** — Data class de chunk do Códex: `chunk_id`, `text`, `source_title`, `source_id`, `page_number`. Usado pelo RAG.


- **`model/PericiasDeCombate.kt`** — `PERICIAS_COMBATE_CORPO_A_CORPO` e `PERICIAS_COMBATE_DISTANCIA` (e a união, `PERICIAS_COMBATE`). A separação existe porque o MIRA-2 precisa perguntar "este ataque é à distância?" — antes era um `setOf` único com um comentário no meio, que separava para humano ler. ⚠️ Guarda **aliases legados** de ids que fichas antigas podem ter gravado; ver `PericiasDeCombateCatalogoTest`.
- **`model/ModeloRacialTotais.kt`** — O conteúdo **real** de um modelo racial: o dele mais o das metacaracterísticas embutidas (MB p.262), resolvido recursivamente com profundidade limitada.
- **`model/Poder.kt`** — `Poder` (o poder na ficha) e `PoderDefinicao` (a entrada do catálogo `poderes.v1.json`).
- **`model/RegistroLog.kt`** — Uma linha do histórico de alterações da ficha. Preenchido pelo `FichaHistoryDelegate`.

---

## 17. PreRequisitos

- **`regras_prerequisitos/PreRequisitoChecker.kt`** — Motor de verificação de pré-requisitos. `checkParseResult(personagem, parsed)` avalia um `ParseResult` contra o personagem (respeita `bypassValidation`) e retorna uma **String de relatório** ("todos requisitos atendidos" / "faltando: ..."). Também tem `checkSimples` (lista direta de `PreRequisitoType`) e `check` (legado). Data class `ConditionStatus` (label, isMet, current, required).

- **`regras_prerequisitos/PreRequisitoParser.kt`** — Parser de texto bruto de pré-requisito ("IQ 12+", "Magia X em NH 14+") → `ParseResult` (`tipos`, `terms`, `bypassValidation`, `warnings`). Cada `PreRequisitoTerm` tem `alternatives: List<List<PreRequisitoType>>` (OR de grupos AND). Detecta marcador de bypass (`#`/especial) → `bypassValidation = true`.

- **`regras_prerequisitos/PreRequisitoType.kt`** — Sealed class com `readableName()` e **17 tipos** de pré-requisito (não 5): `AttributeMin`, `AptidaoMagica`, `MagiaConhecida`, `VantagemConhecida`, `PericiaConhecida`, `MagiasEscola`, `MagiaInclusaNaContagem`, `QualquerMagiaComNome`, `QuantidadeOutrasMagias`, `QuantidadeMagiasPorEscolas`, `QuantidadeMagiasPorTemas`, `MagiasEmEscolasDiferentes`, `AtributosSomaMin`, `NaoPodeSer`, `SkillMinLevel`, `NivelMin`. (Bypass NÃO é um tipo aqui — é o flag `bypassValidation` do `ParseResult`.)

---

## 18. UI — Telas Principais

- **`ui/FichaScreen.kt`** — Container principal. Scaffold com `FichaCustomNavigationBar` e roteamento de dialogs globais (importação, erro de carga, atualização). Abas reais: **Geral, Traços, Perícias, Técnicas, Magia, Equip., Rolagem** (na variante Pracego a aba "Magia" é omitida da barra). A aba **VTT** entra em modo imersivo (esconde o chrome via `vttImmersiveUi`). Combate e Notas são exibidos dentro de outras abas/seções, não como abas separadas na barra. **[+ 2026-06-08]** Quando há foto, o `topBar` vira `CabecalhoComImagem` (private composable: foto de fundo altura fixa 140dp + gradiente + título e linha de pontos overlaid; ícone câmera troca a foto; toque abre tela cheia). `ImagemPersonagemFullscreenDialog` (private): foto INTEIRA em tela cheia, fundo preto, sem texto, X/toque fecha. Picker via `ActivityResultContracts.OpenDocument()` (explorador de arquivos completo, qualquer pasta — não só galeria) chama `ImagemPersonagemStore.salvarImagem`. Sem foto: `TopAppBar` padrão + ícone "adicionar foto".

- **`ui/TabGeral.kt`** — Aba de informações básicas: nome, jogador, campanha, pontos iniciais/gastos/restantes, atributos primários (ST/DX/IQ/HT) com custo, atributos secundários (PV, PF, Vontade, Percepção, Velocidade, Deslocamento), modelo racial ativo.

- **`ui/TabCombate.kt`** — Aba de combate: defesas ativas (Esquiva, Apara, Bloqueio) com bônus manual editável, lista de armas equipadas com dano calculado, armaduras por local corporal com RD total.

- **`ui/TabPericias.kt`** — Aba de perícias: busca por texto/atributo/dificuldade, lista com NH calculado e pontos gastos, adição do catálogo, edição inline de pontos.

- **`ui/TabMagias.kt`** — Aba de magias: busca por escola e classe, lista com custo de energia reduzido por NH, modo alvo (Nexus Arcano) com trilha mínima e chaves de progressão.

- **`ui/TabTracos.kt`** — Aba de vantagens e desvantagens: busca, listagem por custo, adição do catálogo com seleção de modificadores. Ponto de entrada para `VantagemDialogs` e `DesvantagemDialogs`.

- **`ui/TabTecnicas.kt`** — Aba de técnicas: listagem com NH calculado relativo à perícia base, busca por nome/fonte.

- **`ui/TabEquipamentos.kt`** — Aba de equipamentos: lista de itens com peso individual e total, adição de arma/armadura/item genérico do catálogo.

- **`ui/TabRolagem.kt`** — Hub de rolagem. Lista atributos, perícias, magias e traços com ataque inato para rolagem de 3d6. Exibe resultado, margem de sucesso/falha, críticos. Dispatch para Discord se configurado. **[+ 2026-06-09]** `executarRolagem` usa `CriticoRules.classificar` (regra COMPLETA com NH) e, em testes de COMBATE (Ataque/Defesa/Técnica/Magia) que dão Decisivo/Crítico, chama `dispararTabelaCritica` → 2ª rolagem 3d6 nas tabelas → 2ª mensagem ao Discord.

- **`ui/TabNotas.kt`** — Aba de notas e texto livre: histórico, aparência, notas gerais do personagem.

- **`ui/TabVtt.kt`** — Aba de integração VTT: configuração de sessão (URL do servidor, Room Key, Player ID, Token ID), status de conexão, botão de auto-detect na LAN.

---

## 19. UI — Dialogs

- **`ui/DialogsMestreIA.kt`** — Interface de chat completa do Mestre IA: balões de mensagem (usuário/assistente/sistema), botão de copiar por bolha, seletor de sessão histórica (`HistorySelectorDialog`), seletor de modo (conversa/geração/análise), painel de configuração de API (URL, chave, modelo), botão de sincronização forçada do Códex.

- **`ui/DialogsCommon.kt`** — Dialogs comuns reutilizados em múltiplas abas: confirmação de exclusão, diálogo de texto simples, seletor de opções.

- **`ui/DialogsMagias.kt`** — Dialogs específicos de magias: adição com seleção de escola e pontos, edição de pontos de magia existente.

- **`ui/DialogsPericias.kt`** — Dialogs de perícias: adição com especialização, atributo e dificuldade escolhidos, edição de pontos.

- **`ui/DialogsTecnicas.kt`** — Dialogs de técnicas: adição do catálogo com visualização de pré-requisito e limite de NH.

- **`ui/DialogsTracos.kt`** — Dialogs de traços (legado/entrada): seleção de vantagem/desvantagem, visualização de custo e modificadores disponíveis.

- **`ui/DiceRoller.kt`** — Componente de rolagem 3d6: resultado visual, cálculo de margem, identificação de crítico (acerto em ≤4, falha em ≥17, acerto/falha em 3/18).

---

## 20. UI — Features (Subcomponentes Especializados)

- **`ui/features/traits/TraitRule.kt`** — (ver seção 6 — é domain, não UI)
- **`ui/features/traits/TraitCommonComponents.kt`** — Componentes genéricos de traços: `EscopoModificadoresDialog` (seletor de modificadores com busca), chip de custo, card de traço com ações.

- **`ui/features/traits/TraitDialogs.kt`** — Diálogos de adição/edição de vantagens e desvantagens simples (sem regra especial).

- **`ui/features/traits/TraitDialogsV2.kt`** — Versão expandida dos diálogos de traços com suporte a metadados estruturados (para traços com `specialRule`).

- **`ui/features/traits/TraitSpecialRuleComponents.kt`** — Hub de componentes de regras especiais: UI de Aliado, Patrono, Dependência, Inimigo, Mestre de Armas (com seleção de grupo de arma). Cada regra especial tem seu próprio composable.

- **`ui/features/traits/VantagemDialogs.kt`** — Dialog unificado de adição de vantagem: detecta `specialRule` e renderiza o componente correto de `TraitSpecialRuleComponents`.

- **`ui/features/traits/DesvantagemDialogs.kt`** — Dialog unificado de adição de desvantagem: mesma arquitetura de `VantagemDialogs`.

- **`ui/features/magic/MagicDialogs.kt`** — Dialogs de configuração de magia: seleção de escola para `imunidade_a_encantamento`, configuração de encantamento alvo, seleção de AM (Aptidão Mágica) ativa.

- **`ui/features/magic/SelectingMagicDialog.kt`** — Dialog de busca e seleção de magia do catálogo com pré-visualização de pré-requisitos e custo de energia.

- **`ui/features/rolagem/RolagemModels.kt`** — Data classes da aba de rolagem: `RollMappedOption` (opção de rolagem mapeada de perícia/traço), `DamageSourceOption` (fonte de dano), `StDamageMode` (modo de dano por ST).

- **`ui/features/rolagem/RolagemComponents.kt`** — Componentes visuais da rolagem: card de opção de rolagem, resultado visual com cor (verde=sucesso, vermelho=falha, dourado=crítico).

- **`ui/features/rolagem/RolagemPrimaryDialogs.kt`** — Dialogs primários de rolagem: seleção de modificador antes de rolar, confirmação de envio para Discord.

- **`ui/features/rolagem/RolagemSecondaryDialogs.kt`** — Dialogs secundários: configuração de canal Discord, histórico de rolagens da sessão.

- **ui/features/dice3d/Dice3DScene.kt** — Cena 3D física que simula a rolagem dos dados usando SceneView (Filament) e JBullet, substituindo mock 2D. Aplica as cores via shader LinearSrgb.

- **ui/features/dice3d/DiceColorSetup.kt** — Menu premium de customização visual dos dados (ConfigurarDadosDialog), contendo o Dice3DPreview (mini cena 3D giratória em tempo real) e o DiceColorsStore (SharedPreferences).

- **ui/features/dice3d/PhysicsWorld.kt** — Setup da engine JBullet. Mapeia a colisão, restituição, paredes elásticas e detecta os lados do dado.

- **ui/features/dice3d/DiceSoundManager.kt** — Gerencia os sons físicos (batidas) mapeados pela simulação do JBullet em tempo real.

- **ui/features/dice3d/DiceSensorManager.kt** — Gerencia eventos de acelerômetro e sensores para acionar fisicamente a rolagem de dados 3D balançando o aparelho.


- **`ui/features/rolagem/DialogoSentidos.kt`** — **[+ 2026-06-14, Lote 372]** Diálogo de Testes de Sentidos: tocar **PER** (intercept em `TabRolagem`, sem alterar `AtributosQuickRollPanel`) abre os 5 sentidos com valor efetivo + "notinha" do motivo (via `SentidoRules`); cada um rola pelo mesmo caminho (`executarRolagem`→Discord) com o rótulo carregando o bônus/redutor. Sentido bloqueado fica desabilitado ("Cego"/"Surdo"). **Variante PraCego:** botão rotulado grande ("Rolar (14)") + semântica TalkBack.

- **`ui/features/virtualtabletop/MesaVirtualScreen.kt`** — Tela da Mesa Virtual (placeholder). Exibe estado de conexão e botões de ação VTT. Ainda em desenvolvimento.

- **`ui/features/virtualtabletop/MesaVirtualViewModel.kt`** — ViewModel da Mesa Virtual. `MesaVirtualState` com discordId, token, campaignId, isConnected, activePlayers. `conectar()` apenas atualiza o estado local (integração Railway planejada).


### `ui/features/rolagem/` — os painéis e diálogos da aba Rolagem

*A `TabRolagem` bateu no teto de 1.000 linhas várias vezes; quase todo arquivo aqui saiu dela.*

- **`RolagemModels.kt`** — Os tipos da aba: `TipoTeste`, `HistoricoRolagemItem`, `RollMappedOption`, `DamageSourceOption`, `StDamageMode` (GdP/GeB), `PericiaRollOption`, `MagiaRollOption`.
- **`RolagemComponents.kt`** — Os cartões da aba: cabeçalho, atributos, PV/PF, área de ataque e dano, defesas ativas, histórico e a navegação.
- **`RolagemPrimaryDialogs.kt`** — Configurar ataque, configurar dano, e as listas de perícia/técnica/magia. ⚠️ Contém `condicionaisDaPericia`, **fonte única** das caixinhas: a lista era montada duas vezes e as duas casavam por índice — bastava uma ganhar uma fonte nova para o marcado somar o valor errado.
- **`RolagemSecondaryDialogs.kt`** — Rolagem personalizada, magia da alma, energia manual, editar PV/PF, editar canal do Discord, bônus de defesa.
- **`DialogosDeDefesaRolagem.kt`** — Os três diálogos de configuração de defesa + os de PV/PF, extraídos quando o MARCOS-1 levou a aba a 1.046 linhas.
- **`OverlayDados3D.kt`** — A camada que cobre a tela enquanto os dados 3D rolam e mostra o resultado (`textoDoResultado`, `anuncioDoResultado`).
- **`DialogoMira.kt`** — **Onde acertar** (MIRA-1): a lista de locais com o NH **já reduzido**, mais a linha de distância, o Apontar (contador de segundos), arma firmada, mira acoplada, Avançar e Atacar, Zarolho, Pacifismo e Disopia. É onde quase toda regra de tiro se encontra.
- **`DialogoReacaoEResistencia.kt`** — Junta num lugar só o Teste de Reação, o Autocontrole e os testes de resistir (Lote RESIST-1).
- **`DialogoSentidos.kt`** — Testes de Sentidos (abre ao tocar em "PER"), com a notinha do motivo de cada bônus.
- **`DialogoTalentoInstintivo.kt`** — Lista **só** as perícias que o personagem **não** tem, cada uma com o NH do atributo base.
- **`DialogoVisualizacao.kt`** — A calculadora do bônus de Visualização — a conta que mais erra para cima quando feita de cabeça.
- **`PainelAtributosEStatus.kt`** — O cartão de atributos + PV/PF do topo, com os painéis de ST/DX Braçal e ST de Levantamento.
- **`PainelMarcosDeVida.kt`** — Os testes exigidos pela queda de PV e o estado atual (Cambaleante, Cansado…).
- **`PainelBonusCondicional.kt`** — As caixinhas dos bônus que valem **nesta** rolagem (Rosto Sincero, Camaleão…).
- **`PainelEstadosTemporarios.kt`** — O interruptor das nove desvantagens temporárias, no mesmo lugar das Braçais.
- **`PainelIluminacao.kt`** — O seletor de luz da cena, com a Visão Noturna já descontada e a conta escrita.
- **`PainelModificadoresDeCombate.kt`** — A página de modificadores condicionais (MB p.547-549) virada em caixinhas, dentro do *Onde acertar*. Arquivo próprio porque o `DialogoMira` já passa de 900 linhas.
- **`DialogoFadiga.kt`** — O **botão PF** (MB-6). O jogador marca de onde veio o cansaço e o rodapé diz **como cada perda volta** — a informação que muda o jogo. Ao abrir, o PF gasto por fora cai na linha *Perda anotada à mão*, para o painel não devolver de graça o que a magia consumiu.
- **`DialogoFerimento.kt`** — O **botão PV** (MB-7). Local do corpo, tipo de dano, e a **RD das peças que ele está vestindo** naquele local — com a caixinha de vestir, porque comprar não é vestir. Mostra a conta escrita, o choque, o teste de nocaute e se o membro foi incapacitado ou decepado.
- **`PainelAutocontrole.kt`** — A seção de autocontrole; **não renderiza nada** se a ficha não tem desvantagem com NA.
- **`PainelReacao.kt`** — O Teste de Reação com os modificadores somados. Renderiza **sempre**, mesmo com +0 (decisão do usuário: o Mestre pode pedir a qualquer momento).
- **`PainelSorte.kt`** — Usar Sorte na **última** rolagem; aparece só depois de rolar.
- **`PainelAparaRepetida.kt`** — Qual apara do turno é esta (−4, −8, −12), zerando no turno seguinte.
- **`PainelModificadorGlobal.kt`** — Degraus de modificador em botões rotulados. **Só na variante `pracego`** — quem enxerga ajusta deslizando o dedo, gesto que não funciona com TalkBack.
- **`CampoNotaBonus.kt`** — Onde o jogador anota **de onde vem** um bônus digitado à mão.
- **`ComposicaoDaDefesa.kt`** — De onde vem cada ponto de uma defesa, dentro do diálogo (não no card: quebraria a largura em três linhas).

### `ui/features/traits/` — complementos dos diálogos de traço

- **`VantagemEditarDialog.kt`** — O diálogo de **edição** de vantagem, separado do de adicionar em 28/07 por causa do teto de 1.000 linhas. ⚠️ Foi essa separação que deixou os dois divergirem em quatro pontos (Lote LAYOUT-3).
- **`TracoFormComuns.kt`** **[+ Lote LAYOUT-3]** — O miolo compartilhado entre configurar e editar: `NivelDoTraco` (com `−`/`+` visíveis nas **duas** variantes e o teto à vista), `CampoDeDescricaoDoTraco`, `RodapeDoTraco` e `rotuloDoTipoDeCusto` — o que tirou `por_nivel` da tela do jogador.
- **`BracalConfig.kt`** — A configuração de ST/DX Braçal: **braços × níveis**, no lugar dos três botões de valor fixo que cobravam o preço uma vez só.
- **`DialogsPoderes.kt`** — Lista dos poderes na ficha (com lápis e lixeira na linha), o editor de poder e o seletor do catálogo.
- **`DialogoDeslocamentos.kt`** — Todos os deslocamentos, **só leitura**, atrás do botão "Desloc." (decisão do usuário: não é seletor).
- **`OrigemDoBonus.kt`** — A linha discreta que explica de onde vem o bônus de uma perícia. Sem ela a automação vira caixa preta.

### `ui/features/equipamento/`

- **`CardDetalheArma.kt`** **[+ Lote ARMA-3]** — O card de detalhe da arma, aberto pelo toque na lista (o botão de adicionar mora dentro dele) e também pelo inventário. Inclui `CardArmaForaDoCatalogo`, para a arma criada à mão que não casa com nada — ele **diz** por que os campos faltam, em vez de abrir vazio.

### `ui/features/magic/`

- **`DialogoDescricaoMagia.kt`** — O pop-up **único** de descrição de mágica, com barra de rolagem. Existia um igual embutido no diálogo de edição, que não dava para reusar e **não rolava** — descrição longa ficava cortada.

---

## 21. UI — Componentes Utilitários

- **`ui/components/FichaCustomNavigationBar.kt`** — Barra de navegação inferior customizada com ícones e labels das abas. Suporta `onLongPress` no ícone do Mestre IA para ativar a voz. Recebe `estadoLive: EstadoLive` (Gemini Live) e o mapeia internamente para `EstadoVoz` (OUVINDO→ESCUTANDO/anel verde; FALANDO/CONECTANDO/PROCESSANDO→anel amarelo), reusando o anel visual pulsante existente.

- **`ui/components/GeminiLiveService.kt`** — (~81KB) Serviço de **voz em tempo real** via Gemini Live API (WebSocket OkHttp). Substituiu o antigo `VozMestreIA`/`SpeechRecognizer`. Captura áudio (`AudioRecord`) e reproduz (`AudioTrack`), gerencia a sessão WebSocket bidirecional, despacha tool calls para `GeminiLiveTools`. Estados: `EstadoLive` (OCIOSO, CONECTANDO, OUVINDO, FALANDO, PROCESSANDO, ERRO). Mantém `EstadoVoz` por compatibilidade com a navbar. Ver `project_gemini_live_estado.md`.

- **`ui/components/GeminiLiveTools.kt`** — Roteador de ferramentas da Voz. `executar(nome, args)` mapeia as tools do Gemini Live → implementações: lê ficha (`lerFicha`), busca catálogo/edita/GPS de magias/raças (delega ao `ForjadorToolExecutor`), e `consultarManual` (RAG via `MestreIAGraphEngine.buscarDiretoNoCodex` — **caller ativo do GraphEngine**, com truncamento de payload p/ evitar code=1007 do servidor Live). Mantém aliases legados (`obterFicha`, `adicionarVantagem`, etc.).

- **`ui/DialogStandards.kt`** — Padrões visuais de dialogs: dimensões, espaçamentos, cores de botões primário/secundário/destrutivo.

- **`ui/HorizontalDivider.kt`** — Divisor horizontal estilizado usado em listas e seções.

- **`ui/SectionCard.kt`** — Card de seção com título e conteúdo, usado em TabGeral e TabCombate.

- **`ui/UiStandards.kt`** — Constantes de design do app: padding padrão, tamanhos de fonte, breakpoints.

- **`ui/UiA11y.kt`** — Helpers de acessibilidade: `semantics` para TalkBack, labels descritivos. Usado pela variante Pracego.

- **`ui/UiActionLabels.kt`** — Strings de labels de ação para acessibilidade (variante Pracego): "adicionar vantagem", "remover perícia", etc.


- **`ui/AppSelectionUi.kt`** **[+ Lote LAYOUT-1]** — A moldura dos diálogos de seleção: `AppSelectionDialog` (título, subtítulo, busca, chips, contador, lista e Fechar), `AppSelectionRow` (com `detalhe`, `detalheADireita`, `extra` e `acoes`), `AppFiltroChip` e `contadorDe`. ⚠️ Existe porque **seis** diálogos escreviam à mão o mesmo `Card + Row` com padding `8/6`, enquanto o `UiTokens` dizia `12/10`.
- **`ui/AppButtons.kt`** **[+ Lote LAYOUT-1b]** — Quatro papéis no lugar de sete tipos: `AppBotaoPrincipal`, `AppBotaoSecundario`, `AppBotaoDiscreto`, `AppBotaoIcone`, mais `AppBotaoDestrutivo` (a única exceção de cor) e `AppBotaoPasso` (o `−`/`+`). `AppFileiraDeBotoes` resolve espaço e margem. ⚠️ `UiTokens.TouchMinHeight = 48.dp` existia com **zero** usos enquanto havia botões de 32 e 36 dp.
- **`ui/UiScrollbar.kt`** — Barra de rolagem visível para `Modifier.verticalScroll`. O Compose não desenha barra nesse caso (só listas preguiçosas têm), e o resultado era um diálogo que parecia truncado.

---

## 22. UI — Tema

- **`ui/theme/Color.kt`** — Paleta de cores do app (Material You / esquemas claro e escuro). Paleta **única** — não há cores condicionadas à variante Visual/Pracego (a diferenciação de variante é só comportamental, nas telas).

- **`ui/theme/Theme.kt`** — `GURPSFichaTheme`: configura `MaterialTheme` com `ColorScheme` e `Typography`. Usa **Material You dynamic color** (`dynamicDark/LightColorScheme` em Android 12+) com fallback para `DarkColorScheme`/`LightColorScheme`; ajusta a cor da status bar. **Não** lê `BuildConfig.UI_VARIANT` — a diferenciação Visual/Pracego acontece nas telas/dialogs (via `isPraCegoVariant = BuildConfig.UI_VARIANT == "pracego"`), não no tema.

- **`ui/theme/Type.kt`** — Tipografia do app: `TextStyle` para títulos, corpo e legendas.

---

## 23. VTT — Mesa Virtual

- **`vtt/VttBridgeCodec.kt`** — Codec de serialização para a ponte VTT. Converte JSON em JavaScript string literal com escape correto de caracteres especiais. Usado para injetar dados da ficha em WebView do Foundry.

- **`vtt/VttHostAutoDetect.kt`** — Auto-detecção de servidor VTT na LAN. Primeiro tenta ARP table (`/proc/net/arp`), depois scan ativo por subnet. Faz probe HTTP paralelo (com coroutines) nos candidatos para detectar qual tem a API do servidor GURPS.

- **`vtt/VttSessionService.kt`** — Serviço de sessão VTT. `joinSession` (entra numa sala com roomKey e playerId), retorna `VttJoinSessionResult` (sessionId, tokenId, needsBind). Chamadas HTTP para o servidor Railway/local.

- **`vtt/VttSessionStorage.kt`** — Persistência local da sessão VTT em `SharedPreferences`: serverUrl, webUrl, roomKey, playerId, sessionId, tokenId, tokenImageUri, autoReconnect. `VttSessionSnapshot` data class.

- **`vtt/VttTokenBindService.kt`** — Vincula o token do personagem (imagem e ID) ao player na sessão VTT. Retorna `VttTokenBindResult`. Separado do `VttSessionService` para responsabilidade única.

- **`vtt/VttRollService.kt`** — Envia rolagens para o servidor VTT via HTTP: `VttRollRequest` (roomKey, playerId, tokenId, tipoAcao, nomeAcao, modificador, alvoTokenId) → `VttRollResult`.

---

## 24. Update

- **`update/AppUpdateService.kt`** — Verifica nova versão no GitHub (endpoint configurado em `BuildConfig`). Compara `versionCode` atual vs. mais recente. Retorna `AppUpdateState` com URLs de APK para variante Visual e Pracego. Data classes: `AppUpdateMetadata`, `AppUpdateState`.

- **`update/AppUpdateHelper.kt`** — Executa o download e instalação da nova APK via `DownloadManager`. Registra `BroadcastReceiver` para detectar conclusão do download e dispara o intent de instalação via `FileProvider`.

---

## 25. Assets — Catálogos JSON Ativos

*Arquivos em `app/src/main/assets/`.*

*Nomes de arquivo conferidos contra os `assets.open(...)` reais em `CatalogLoaders.kt`,
`DataRepository.kt`, `RacaCatalogo.kt`, `MetacaracteristicaCatalogo.kt` e `MestreIATopicIndex.kt`.*

| Arquivo | Conteúdo | Carregado por |
|---|---|---|
| `vantagens.v3.json` | Vantagens oficiais GURPS 4ª Ed. (formato v3 com modificadores estruturados) | `CatalogLoaders` |
| `vantagens_artes_marciais.v1.json` | Vantagens exclusivas do suplemento Artes Marciais | `CatalogLoaders` |
| `desvantagens.v2.json` | Desvantagens oficiais (formato v2 com specialRule) | `CatalogLoaders` |
| `pericias.v3.json` **[+ Lote PERUNI]** | **O catálogo de perícia em uso.** 281 perícias, unificação de `pericias.json` (base) + `pericias_v2_rules_map.json` (camada). Traz id, nome, atributo, dificuldade, descrição, pré-requisito, predefinido e o rodapé `Modificadores:`. | `CatalogLoaders` |
| `pericias.json` | ⚠️ **LEGADO** — a base antiga (só id/nome/atributo/dificuldade). Continua no disco até a validação no aparelho fechar. | (não carregado) |
| `poderes.v1.json` | 44 poderes (GURPS Poderes): fontes possíveis, foco e descrição. | `CatalogLoaders` |
| `modificadores_poderes.v1.json` | 31 modificadores específicos de poderes, separados do catálogo global. | `CatalogLoaders` |
| `pericias_artes_marciais.v1.json` | Perícias suplementares (Artes Marciais) | `CatalogLoaders` |
| `pericias_v2_rules_map.json` | Mapa de regras de perícias v2 (tipo, pré-requisito, predefinido) | `CatalogLoaders` |
| `magias2versao.json` | Magias com pré-requisitos raw | `CatalogLoaders` |
| `tecnicas.v1.json` | Técnicas (arquivo único — Módulo Básico + suplementos) | `CatalogLoaders` |
| `armas_corpo_a_corpo.v1.normalized.json` | Armas de combate corpo a corpo | `CatalogLoaders` |
| `armas_distancia.v1.normalized.json` | Armas de ataque à distância | `CatalogLoaders` |
| `armas_fogo.v1.normalized.json` | Armas de fogo | `CatalogLoaders` |
| `modificadores.v1.json` | Catálogo global de modificadores | `CatalogLoaders` |
| `armaduras.v2.json` | Armaduras com componentes por local corporal | `CatalogLoaders` |
| `escudos.v1.json` | Escudos com BD | `CatalogLoaders` |
| `racas.v1.json` | Raças jogáveis (formato enxuto — sem custos, recalculado) | `RacaCatalogo` |
| `metacaracteristicas.v1.json` | Pacotes prontos de metacaracterísticas (Gigante, Anão, etc.) | `MetacaracteristicaCatalogo` |
| `forjador_templates.json` **[+ 2026-06-09]** | 60 templates/arquétipos de personagem prontos (combatentes, furtivos, sociais, 10 magos, gêneros modernos). IDs validados contra o catálogo. | `ForjadorTemplateCatalogo` |
| `tabelas_criticas.json` **[+ 2026-06-09]** | 4 tabelas de combate (Golpe Fulminante, na Cabeça, Erro Crítico, Erro Crítico Desarmado), entradas 3-18 com texto completo. | `CriticoRules` |
| `bestiario.v1.json` **[+ 2026-06-14]** | Bestiário da Saga: 17 criaturas (goblin, orc, lobo, ogro, esqueleto, bandido…) com stats de combate e ataques. Validado por `scripts/check_bestiario.py`. | `BestiarioCatalogo` / `BestiarioLoader` |
| `mestre_ia_temas.json` | Temas canônicos de busca para o Mestre IA | `DataRepository` |
| `chunks.jsonl` | Chunks do manual GURPS (1 por página, FTS4). 54.9MB com embeddings; Auditor usa só o texto, embeddings (3072 dims) dormentes p/ ele. | `FichaDatabase.prePopulateManual` |
| `chunks.jsonl.bak` | Idêntico SEM embeddings (6.5MB, 1196 linhas). Candidato a substituir o .jsonl quando confirmado que Auditor não precisa de embedding. | (não carregado — backup) |
| `topic_index.json` | ⚠️ Páginas garantidas — lido só por `MestreIATopicIndex.carregar()`, que existe mas **NINGUÉM chama** (MORTO desde Lote 272). Asset órfão na prática. | `MestreIATopicIndex` (nunca invocado) |

> **Nota:** há vários assets de apoio/backup não consumidos em runtime.
> Não são catálogos ativos, mas dois deles são **contrato** e valem ser lidos antes de mexer
> nos catálogos de traço:
> - `vantagens.v3.schema.json` / `desvantagens.v2.schema.json` — o JSON Schema de cada catálogo,
>   incluindo o formato do campo `efeitos` (ver §6). É o que diz quais chaves existem e o que
>   cada uma aceita.
> - `topic_index_backup_manual.json`, `topic_index_gerado.json`, `pericias_v2_rules_map copy.json`
>   — backups sem consumo.

---

## 26. Scripts de Manutenção (pasta `scripts/`)

*~40 scripts Python no total. Destaques (todos conferidos como existentes):*

- **`generate_pericias_v2_rules_map.py`** — Gera o mapa de regras de perícias v2 a partir do texto bruto.
- **`fix_mojibake_project.py`** — Corrige encoding corrompido (mojibake) em todo o projeto.
- **`cleanup_assets_text.py`** — Normaliza textos e limpa artefatos de OCR de PDFs.
- **`gerar_embeddings.py`** — Gera os embeddings dos chunks (importados no `chunks.jsonl`).
- **`gerar_topic_index.py`** — Gera o `topic_index.json` (asset hoje órfão).
- **`processar_livro.py` / `sanitize_manuals.py`** — Pipeline de ingestão dos manuais (chunks do Códex).
- **Série `convert_*.py` / `normalize_*.py`** — Convertem/normalizam dados brutos (planilhas, PDFs) para o formato JSON dos assets (vantagens, desvantagens, perícias, armas, armaduras, escudos, técnicas).
- **Série `validate_*.py`** — Validação de integridade dos catálogos (armaduras, técnicas, associações de texto).
- **`check_bestiario.py`** (Lote 363) — valida `bestiario.v1.json`. **`check_armas.py`** **[+ 2026-06-14, Lote 371]** — valida que os 3 catálogos de armas têm os stats de combate (reach CaC; precisão/alcance/CdT/Bulk à distância).
- (⚠️ a antiga doc citava `audit_active_jsons_v2.py`, que **não existe** na pasta.)

---

## 27. Motor Nexus Arcano (módulo separado `motor modo alvo/src/`)

- **`NexusArcanoEngine.kt`** — Orquestrador: avalia chaves, computa trilha ótima (A*/guloso via `planejarCaminhoMinimo`), retorna `ArcanoResultado`.
- **`ArcanoModels.kt`** — Modelos: `ArcanoChave`, `ArcanoMetaTipo`, `ArcanoMetaProgress`, `ArcanoEstadoPersonagem`, `ArcanoResultado`.
- **`ArcanoCatalogo.kt`** — Interface que o adapter implementa para fornecer pré-requisitos e escolas ao engine.
- **`NexusArcanoHeuristics.kt`** — Avalia quantas magias de cada escola o personagem possui para detectar chaves desbloqueadas.
- **`NexusArcanoParser.kt`** — Interpreta texto bruto de pré-requisito de magia → lista de dependências tipadas.
- **`NexusArcanoPathfinder.kt`** — DFS/guloso para encontrar caminho mínimo até a magia alvo.
- **`NexusArcanoStrings.kt`** — Formatação de mensagens para a UI (avisos, trilha de aprendizado, bloqueios).
- **`ArcanoCatalogoDesejoExemplo.kt`** — Catálogo de exemplo (fixture) para testar o motor sem o app.
- **`diagnostico_desejo.kt` / `diagnostico_parser.kt` / `diagnostico_real.kt`** — Mains de diagnóstico standalone do motor (parser, pathfinder, cenário real). Ferramentas de depuração, não fazem parte do app.

---

## 28. Testes Automatizados (`app/src/test/`)

*Pacote base: `com/gurps/ficha/` (exceto a suíte `nexus/arcano/`, que fica em `app/src/test/java/nexus/arcano/`).*

- **`domain/rules/RulesLayerTest.kt`** — Testes de `CharacterRules` e `CombatRules` (atributos, PV, defesas).
- **`domain/rules/MagiaEnergiaRulesTest.kt`** — Redução de custo de energia por NH.
- **`model/PersonagemRulesTest.kt`** — Validação de criação de personagem e limites de pontos.
- **`model/PersonagemInteropTest.kt`** — Import/export versionado (envelope + fallback legado).
- **`model/PericiaJsonParsingTest.kt`** — Parsing dos JSONs de perícias.
- **`domain/filters/TextNormalizerTest.kt`** — Os 4 presets do `TextNormalizer`.
- **`domain/MestreIAContextFilterTest.kt`** — String de contexto da ficha enviada à IA.
- **`domain/MestreIARagEngineTest.kt`** — Motor RAG (GraphEngine).
- **`data/network/MestreIAClientTest.kt`** — Montagem de request / parsing de tool calls.
- **`data/storage/FichaStorageRepositoryTest.kt`** — Persistência de fichas.
- **`domain/roll/RollDispatchPolicyTest.kt`** — Política de retry/erro de rolagem.
- **`regras_prerequisitos/PreRequisitoParserTest.kt`** — Parser de pré-requisitos.
- **`ui/TabCombateStateTest.kt`** — Estado da aba de combate.
- **`domain/magias/NexusArcanoLoteFCanonicScenarioTest.kt`** + **`NexusArcanoModoAlvoAdapterTest.kt`** — Cenários do adapter Nexus Arcano.
- **`nexus/arcano/NexusArcanoEngine*Test.kt`** — Suíte massiva do motor de magias (Lote1/2/3, GlobalA/B, StressMagiasV2, AuditoriaTodasMagias) + `NexusArcanoTestCatalog.kt` (catálogo de fixtures).
- **`vtt/VttBridgeCodecStressTest.kt`** — Teste de robustez do codec VTT.
- **[+ 2026-06-14] Combate da Saga** (`domain/combat/`): `CombatEncounterTest`, `CombatActionsTest` (inclui Mover e Atacar correto), `HitLocationRulesTest`, `InjuryRulesTest`, `NpcCombatBrainTest`, `CombatResolverTest`, `CombatSessionTest` (sessão ponta a ponta: arma/tipo de dano/distância, narração, avaliar, postura, mover dirigido, rajada, **dual-wield: 2 golpes + mão inábil −4/Ambidestria, sem defesa após Ataque Total**).
- **[+ 2026-06-14] Narrador/Saga** (`domain/saga/`): `NarradorToolsTest` (contrato das 19 tools — `assertEquals(19, TODAS.size)` após MA-4 `lancar_magia`), `NarradorOutputValidatorTest`, `NarradorToolExecutorCombatTest` (roteamento das 6 tools de combate via `CombatBridge` falsa). Instrumentado: `SagaFoundationTest` (FTS4 real).

### Suítes por frente *(a suíte passou de ~1.200 para 1.830 testes entre jul e ago/2026)*

*O padrão destes testes não é "um caso que eu imaginei": é **varredura** sobre o catálogo real
e **invariante** ("isto nunca pode acontecer"). Foi assim que apareceram os furos que a asserção
pontual não pegava.*

- **Traços — regras e catálogo** (`domain/rules/`): `DesvantagensEspelhoTest`, `DesvantagensDListaTest`, `DesvantagensDNaTest`, `DesvantagensDMiraECritTest`, `DesvantagensDJsonTest`, `DesvantagensSimulacaoTest` (simulação, não asserção pontual), `EstadosTemporariosTest`, `IncompatibilidadeDeTracosTest`, `TravaDeParesNoDelegateTest` (⚠️ teste de **fiação**: 17 testes de regra ficaram verdes enquanto o delegate não consultava a trava), `TetoDeAtributoRulesTest`, `TetoDeNivelDoTracoTest`, `AutocontroleRulesTest`, `CustoAutocontroleTest`, `IdsDeVantagemNoCatalogoTest`, `VantagemRacialContaTest`, `TalentoECuringaTest`.
- **Efeitos declarados** (`domain/rules/traits/`): `EfeitoInterpretadorTest`, `EfeitoPontaAPontaTest`, `EfeitoPorOpcaoTest`, `EfeitosDeclaradosCatalogoTest`, `EfeitosNoLoaderTest`, `RegistryDesvantagensTest`, `BonusCondicionalTest`, `MestreDeArmasDanoTest`.
- **Perícias** (`domain/rules/`): `PericiasCatalogoUnificadoTest` (⚠️ a rede do PERUNI — compara o catálogo unificado com um fixture gerado **antes** da mudança; pegou 3 regressões), `PericiasEquipCultSitTest`, `PericiasSimulacaoTest`, `ConferenciaCruzadaPericiasTest`, `PericiasDeCombateCatalogoTest` (⚠️ compara as listas de perícia de combate com o catálogo — a comparação que **não existia**, e por isso `canhoneiro_nt` ficou anos de fora).
- **Rolagem e combate na ficha** (`domain/rules/`): `MiraRulesTest`, `ApontarRulesTest`, `ApontarAcumuladoTest`, `MiraAcopladaEConflitoTest`, `AvancarEAtacarTest`, `AtiradorRulesTest`, `AlcanceDoAtaqueTest`, `TabelaVelocidadeDistanciaTest`, `GolpeRapidoEAparaRulesTest`, `MaoInabilRulesTest`, `MarcosDeVidaRulesTest`, `ResistenciaRulesTest`, `ResistenciaLesaoTest`, `ReacaoRulesTest`, `SorteRulesTest`, `TalentoInstintivoRulesTest`, `VisualizacaoRulesTest`, `IluminacaoEDeslocamentoTest`, `DeslocamentosRulesTest`, `SentidoRulesTest`, `MensagensDefesaTest`, `OrigemDosNumerosTest`, `RotulosAcessiveisTest`, `StBracalRulesTest`, `DxBracalRulesTest`, `StEspecializadaRulesTest`, `AtributoBonusRulesTest`, `ModificadoresDeCombateTest`, `TamanhoDoAlvoRulesTest`, `TiroContinuoRulesTest`, `QuedaRulesTest`, `FadigaRulesTest`, `FerimentoPorLocalRulesTest`, `CoberturaDaArmaduraTest`, `MapaDaSilhuetaTest`.
- **A fiação da tela** (`ui/`): `PadraoDeTelaTest` lê o código-fonte de `ui/` e reprova as violações do padrão — arquivo não migrado fica numa lista de dívida **com data**. `BotoesPvPfLigadosTest` confere que as palavras PV e PF realmente abrem os painéis, que os callbacks são repassados nos quatro arquivos do caminho e que o ViewModel chama `salvarFicha()`. ⚠️ Nenhum teste de regra pega um `= {}` esquecido no meio da fiação.
- **Catálogo de armas** (`domain/loaders/`): `ArmasCatalogoTest` **[+ Lote ARMA-1]** — varre as **150 armas do asset real**. Achou o `+N` da mira acoplada (12 armas), o 2º modo de ataque (29 corpo a corpo), o `"2.900".toIntOrNull() == null` (57 de 124 alcances) e a linha deslocada uma coluna em 3 armas de fogo. `FichaTecnicaDaArmaTest` cobre a tradução do jargão.
- **Magia** (`domain/magic/`): `MagicCoreTest`, `MagicCastingTest`, `MagicMechanicsTest`, `MagicTimeTest`, `MagicClassParserTest`, `RitualEAreaTest`, `MagicCatalogRealityCheckTest` (⚠️ confere o motor contra o catálogo **real** das 879 magias), `MagicEngineAptidaoTest`, `MagMecanizacaoTest`, `MagicCombatTest`.
- **Combate tático hexagonal** (`domain/combat/hex/`): `HexGridTest`, `HexCombatStateTest`, `HexCombatSyncTest`, `HexSetupTest`, `HexRegrasFacingTest`, `HexRegrasPosicionaisTest`, `HexTaticaNpcTest`, `HexTaticoStateTest`, `HexTaticoDemoTest`, `HexPortabilidadeTest`, `HexRender3DTest`.
- **Subsistemas do combate** (`domain/combat/subsistemas/`): `AtaqueMagicoResolverTest`, `DanoMagicoResolverTest`, `EfeitosMagicosDelegateTest`, `ZonaDelegateTest` — os delegates que existem para o `CombatSession` **não** crescer.
- **Imagem e VTT** (`data/storage/`, `ui/saga/`): `RostoDetectorSalienciaTest`, `TokenImageStoreRecorteTest`, `CenarioImageStoreTest`, `FaxinaRetratosOrfaosTest`, `TokenRecursoTest`, `CameraHexTest`, `MenuTaticoTest`, `VfxMapperTest`, `DefesaPorTimingRegrasTest`.
- **UI** (`ui/`): `PadraoDeTelaTest` **[+ Lote LAYOUT-2]** — ⚠️ **lê o código-fonte de `ui/`** e reprova `Card` cru em `LazyColumn`, o padding `8/6` à mão, botão abaixo de 48 dp, cor de botão fora do tema e `enum.name` num `Text`. Existe porque `UiTokens` e `AppListItemCard` estavam no projeto e **nenhum** dos seis diálogos os usava: componente que ninguém é obrigado a usar não é padrão, é sugestão. Carrega uma **dívida datada** de arquivos ainda não migrados, com limite que não cresce calado. Mais: `TextoDoResultadoTest`, `ClassificarModificadorTest`, `ModificadorDonoTest`, `OrigemDoBonusTest`.
- **ViewModel** (`viewmodel/delegates/`): `NotaBonusManualTest`, `PvPfNegativoTest`.
- **IA** (`data/network/`, `domain/`): `MestreIAToolsTest`, `ContextoEfeitosParaIATest`.
- **Pré-requisitos**: `PreRequisitoPericiaComEspecializacaoTest`.

---

## 29. Endereços Rápidos (Funções Críticas)

| O que buscar | Onde está |
|---|---|
| Esquiva / Apara / Bloqueio (cálculo) | `CombatRules.kt` → `calcularEsquiva/Apara/Bloqueio` |
| Bônus de Mestre de Armas | `MestreDeArmasRule.kt` → `getDamageBonusPerDie` |
| Golpe/Empurrão por ST | `CharacterRules.kt` → `calcularDanoGdP / calcularDanoGeB` (tabelas privadas `tabelaGdP / tabelaGeB`, com extrapolação) |
| Custo de vantagem com specialRule | `CharacterRules.kt` → `calcularCustoAliado/Inimigo/...` |
| Cálculo de NH de perícia | `SkillEngine.kt` → `getRegraPerfilTecnica` |
| Loop de tool-use do Auditor | `MestreIAUseCase.kt` → `conversarComMestreIA` (localizar→ler, máx 8) |
| Busca do Auditor (localizar/ler) | `MestreIARepository.kt` → `localizarNoCodex` / `lerPaginas` |
| Ranking do Auditor (ajustar AQUI) | `MestreIARepository.kt` → `rankearPorBM25` (Lote 327) |
| Trava anti-confabulação | `MestreIAUseCase.kt` → var `leuAlgumaPagina` (Lote 328) |
| Loop de tool-use do Forjador | `MestreIAGeneratorUseCase.kt` → `gerarPersonagem` |
| ⚠️ Scoring RAG semântico (LEGADO p/ Auditor) | `MestreIAGraphEngine.kt` → `buscarDiretoNoCodex` |
| Query FTS (GraphEngine/Forjador) | `MestreIAQueryEngine.kt` → `prepararQueryFTSAgressiva` |
| Carregamento de raças | `RacaCatalogo.kt` → `resolver` |
| Envio para Discord | `DiscordRollApiClient.kt` → `postRoll` |
| Salvar / carregar ficha | `FichaStorageRepository.kt` → `salvarFicha / carregarFicha` |
| Importar JSON versionado | `PersonagemInterop.kt` → `importarJson` |
| Normalização de busca | `CatalogFilters.kt` → `normalizarBusca` |
| Auto-detect VTT na LAN | `VttHostAutoDetect.kt` → `detectLanHost` |
| **Combate Saga: orquestra o encontro** | `domain/combat/CombatSession.kt` (heroiAtaca/**heroiAtaqueDuplo**/npcResolve/heroiMove/heroiAvaliar/narrarTroca; golpe único em `resolverGolpeHeroi`) |
| **Combate: dual-wield / sem defesa pós-Ataque Total** | `CombatSession.heroiAtaqueDuplo` (mão inábil −4/Ambidestria, MB p.366) + flag `heroiSemDefesaAtiva` (anula defesa do herói) · UI `SubDialogoAlvoLocal` modo Duplo |
| **Combate: NH efetivo / Mover e Atacar** | `domain/combat/CombatActions.kt` → `calcularNH` (CaC −4+teto 9; à distância −2) |
| **Combate: dano localizado / tipo (pa*→pi*)** | `HitLocationRules.aplicarDano` + `CombatSession.tipoDano` |
| **Combate: ponte motor↔UI / arma escolhível** | `viewmodel/delegates/SagaCombatController.kt` (`construirAtaques`, `CombatUiState`) |
| **Narrador: tools / executor (combate via bridge)** | `domain/saga/NarradorTools.kt` + `NarradorToolExecutor.kt` (`CombatBridge`) |
| **Narrador: estado da aba + bridges** | `viewmodel/delegates/FichaSagaDelegate.kt` (RollBridge + CombatBridge + narrarFimDeCombate) |

---

## 30. Variantes de Build

| Variante | Foco |
|---|---|
| `Visual` | Estética visual, cores vibrantes, layouts densos |
| `Pracego` | Acessibilidade total (TalkBack), labels extras, diálogos simplificados |

Chave de controle: `BuildConfig.UI_VARIANT` (usado para condicionar lógica de UI entre as variantes — ex.: `isPraCegoVariant` espalhado pelas telas/dialogs).

Cada variante tem seu próprio **source set** com um ponto de entrada de UI:
- **`app/src/visual/.../ui/AppUiEntry.kt`** e **`app/src/pracego/.../ui/AppUiEntry.kt`** — `@Composable AppUiEntry(viewModel)` específico de cada flavor. Ambos hoje delegam a `FichaScreen(viewModel)`; o source set garante que cada build compile a sua versão. `MainActivity` chama `AppUiEntry` dentro de `GURPSFichaTheme`.

---

## 31. Servidor Discord (Node/Express — fora do `app/`)

*Pasta `discord-roll-api/` (raiz do projeto Android). Node 18+, Express. Roda no Railway. NÃO é compilado pelo Gradle.*

- **`discord-roll-api/src/server.js`** — API que publica rolagens no Discord via bot. Rotas: `GET /health`, `GET /api/channels` (lista canais de voz, com cache 30min), `POST /api/rolls` (monta mensagem da rolagem e envia ao canal), `GET|POST /api/fichas*` (persistência in-memory de fichas na nuvem por `deviceId`). `formatRollMessage` formata texto (crítico, margem). `sendToDiscord` envia ao endpoint do Discord. **[+ 2026-06-08]** Map `portraits` (in-memory: sanitizedName → {mime,buffer,ext}); `parseDataUri`/`sanitizeName`; rota nova **`POST /api/portrait`** {character, image(data:base64)} guarda o retrato; `sendToDiscord` passou a aceitar portrait opcional → com retrato manda **embed + multipart** (FormData/Blob, globais Node 18+) com `thumbnail` `attachment://portrait.<ext>`, sem retrato manda `{content}` como antes; `/api/rolls` busca `portraits.get(sanitizeName(payload.character))`. Limite do `express.json` subiu p/ 8mb. **[+ 2026-06-09]** `classificarCritico(soma, nh)` aplica a regra COMPLETA com NH (corrige a simplificada 3-4/17-18); `formatRollMessage` detecta a 2ª mensagem de tabela crítica (testType começa com 💥/💀) e renderiza o texto cru. ⚠️ portraits e fichas são in-memory (perdem no restart do Railway). ⚠️ Mudanças exigem **deploy** no Railway p/ valer online.

> **Nota de build [+ 2026-06-08]:** `app/build.gradle.kts` ganhou deps p/ a feature de imagem:
> `com.google.mlkit:face-detection:16.1.7`, `com.google.android.gms:play-services-mlkit-subject-segmentation:16.0.0-beta1`
> e `androidx.exifinterface:exifinterface:1.3.7` (Coil 2.5.0 já existia).

> **Nota de build [+ Lotes 3D Dice]:** Injeção da engine 3D nativa (io.github.sceneview:sceneview) e do motor de colisão JBullet (cz.advel.jbullet) para física de dados real.

---

## 32. SAGA / NARRADOR / MOTOR DE COMBATE  [+ 2026-06-14, Lotes 349-370]

- **`domain/combat/CombatSessionTipos.kt`** **[+ Lote MOTOR-5]** — Os **tipos de fronteira** do `CombatSession`: `HeroiPerfilCombate`, `AtaqueHeroi`, `DefesaHeroi`, `ResultadoCombate`, `ApararTipo`. Puro dado, sem lógica — separados do motor para ele parar de crescer.
- **`domain/combat/SagaLog.kt`** **[+ Lote LOG-1]** — Espelha o log narrativo da luta no Logcat. **Como usar:** filtre por `tag:Saga_Combate` no Android Studio; toda linha que o motor escreve (NH, rolagem, dano, RD, condição, recusa por alcance) sai lá na ordem em que aconteceu.

*Modo solo-RPG narrado por IA (3º modo de IA = `saga`). Fluxo de IA detalhado em `ARQUITETURA_MESTRE_IA.md §10`.
Tudo em `domain/combat/` é Kotlin PURO (sem Android, determinístico por seed) e coberto por testes (§28).*

### 32.1 Narrador (IA modo `saga`)
- **`domain/MestreIANarradorUseCase.kt`** — Orquestrador do modo `saga` (clone do Generator: fila de fallback de modelos + loop de tool-use + Auto-Healing + `consultar_mundo` automático por palavra-chave). Narração no Gemini 2.5 Flash.
- **`domain/saga/NarradorTools.kt`** — Schemas das **19 tools** (17 próprias + `localizar_no_codex`/`ler_pagina` reusadas do Auditor). Spec neutra única → Gemini + OpenAI. `NarradorToolsTest` garante toolset == executor. Cresceu do MA-4 em diante (`lancar_magia`, `aplicar_modificador_combate`, `gerir_equipamento`); `magias_dos_inimigos` foi param novo do MEC-8, não tool nova.
- **`domain/saga/NarradorToolExecutor.kt`** — Roteador nome→impl. Interfaces `RollBridge` (rolagem interativa) e **`CombatBridge`** (combate). Reais: fato/mundo/inspecionar/cena/rolagem/Códex + 6 de combate. `forjar_npc`/`avancar_relogio`/`passar_tempo` = `nao_implementado` (Fase C/D).
- **`domain/saga/NarradorOutputValidator.kt`** — Anti-confabulação: alarme se a prosa cita número/regra que não veio de tool no turno.
- **`domain/saga/CampanhaConfig.kt`** — Session zero (gênero/tom/dificuldade/magia/NT/livros) → bloco no prompt.
- **`data/network/MestreIAPromptsNarrador.kt`** — Persona categorial do Narrador (8 leis de ferro; a nº 8 = protocolo de combate).
- **`viewmodel/delegates/FichaSagaDelegate.kt`** — Estado da aba Saga; implementa `RollBridge` + `CombatBridge`; resolve NH→3d6→`CriticoRules`; persiste turnos (chat sessão `saga#<id>`); `narrarFimDeCombate` (prosa + saque + XP).
- **`ui/TabSaga.kt`** — UI da aba (campanhas, feed/máquina de escrever, card de rolagem, `ConfiguracaoJogoDialog` tela cheia, `BarraDeRolagem`). Renderiza `CombatePainel` (com `weight`) quando há combate.

### 32.2 Persistência da Saga (Room v26)
- **`data/storage/SagaEntities.kt`** — `CampanhaEntity` (+`configJson`), `CenaEntity`, `CampaignFactEntity` (FTS4), `WorldStateEntity`.
- **`data/storage/SagaDao.kt`** — CRUD + `buscarFatos` (MATCH AND/fallback OR, ranking peso→frequência) + `excluirCampanhaCompleta` (@Transaction). `FichaDatabase` v24→25→26 com `MIGRATION_24_25`/`MIGRATION_25_26` explícitas.

### 32.3b Subsistemas extraídos do motor (`domain/combat/subsistemas/`) — Lote MOTOR (jul/2026)
> O `CombatSession` (era 3675, hoje **3230**) **não é estável — vai crescer** com as ~700 magias
> restantes + vantagens/perícias/itens que ainda tocam o combate. Por isso foi decomposto em
> subsistemas ANTES da enxurrada, cada um num delegate testável sozinho. O motor injeta o que cada um precisa por
> **lambda** (log, RNG, HT/RD, callbacks) e reexpõe a API por delegação — quem chamava `s.registrarZona`
> etc. continua igual. Comportamento preservado (rede de invariantes SIM-1 prova).
- **`subsistemas/ZonaDelegate.kt`** (MOTOR-1) — zonas persistentes: `zonasAtivas`, `ocupantesDaZona` (ponto de injeção da grade), `registrarZona`/`limparZonas`/`encolherZona`/`tiqueDasZonas`, não-acúmulo da mesma mágica. Teste próprio: `ZonaDelegateTest`.
- **`subsistemas/DanoMagicoResolver.kt`** (MOTOR-2) — o **funil de dano mágico** compartilhado pelas 4 entradas (magia direta, área, feixe P9, explosão de projétil P5): imunidade por elemento (A1) → tipo de criatura (A1-b) → rola/1-2D/RD → condição embutida. Extraí-lo **destravou** projétil/feixe/área (antes presos a ele). Teste próprio: `DanoMagicoResolverTest`.
- **`subsistemas/AtaqueMagicoResolver.kt`** (MOTOR-3) — **acerto+defesa da magia à distância** (a camada ENTRE conjuração e funil de dano): feixe (P9, DX−4/−2 ou Ataque Inato, esquiva/bloqueio nunca aparar), arremesso de projétil (P6/P11, mira + distância), explosão (P5, alvo cheio + respingo ÷ `3×dist`, dado rolado 1×). `vizinhosDoImpacto` é o ponto de injeção do respingo. **O projétil CARREGADO (segurar/aumentar/arremessar, P11) NÃO saiu** — está tecido no coração da conjuração (NH/custo/PF/choque). Teste próprio: `AtaqueMagicoResolverTest`.
- **`subsistemas/EfeitosMagicosDelegate.kt`** (MOTOR-4) — **efeitos mágicos ativos**: `magiasAtivas`/`manutencaoPendente`, `registrar`/`aplicarBuffDeUmUso`/`dissipar`/`resolverManutencao`, `tiquePorTurno` (Morte Candente/Putrefata), `abaloDeConcentracao` (Vontade−3 ao ser ferido/atordoado), `avancarUmSegundo` (manutenção + expiração revertendo o buff). Guarda as 3 regras sutis: **não-acúmulo** (Magia p.9), **regra da estreia** (MEC-22), **abalo** (Magia p.7). `CombatSession.ManutencaoPendente` fica aninhado (controller/UI o referenciam). Teste próprio: `EfeitosMagicosDelegateTest`.
- **Fim do loop MOTOR: `CombatSession` 3675 → 3230 (−445, −12%).** Extraídos os 4 subsistemas com **estado próprio e ciclo fechado**. O que resta (`npcResolve`, `resolverGolpeHeroi`, `avancarTurno`, `resolverConjuracao`, conjuração de área/toque, agarrão) é o **núcleo interativo acoplado**: o laço de turno em si + a conjuração tecida em NH/custo/PF/choque, tudo compartilhando `resolverTroca`. A matemática de ataque (`CombatResolver`) e as tabelas de crítico (`CriticoRules`) **já são objetos à parte**. Puxar mais espalharia acoplamento (delegates de N injeções) em vez de reduzi-lo — a decomposição limpa parou aqui. A pressão das ~700 magias já foi desviada para os delegates de dano/ataque/efeitos.

### 32.3 Motor de combate puro (`domain/combat/`)
- **`CombatModels.kt`** — `Postura`/`Condicao`/`Manobra`, `NpcStats` (com `armaNh`), `Combatente` (PV/PF/postura/condições mutáveis; `vivo`/`caido`).
- **`CombatEncounter.kt`** — Iniciativa (Vel.Básica→DX→seed), `proximoTurno`, `manobrasLegais`, `estadoResumo`, distância MUTÁVEL (`moverEmRelacaoAoHeroi`/`definirDistancia`).
- **`CombatActions.kt`** — `calcularNH` (manobra/postura/local/visibilidade/`modsExtra`/`magnitudeArma`) + `resolverAtaque` (3d6) + `avaliarRolagem`. **Mover e Atacar: CaC −4+teto 9; à distância −2 OU a Magnitude/Bulk, o pior** (MB p.366/271, Lote 375).
- **`ModificadoresCombate.kt`** — `LocalAtaque` (penalidades p/ acertar, MB p.398), `Visibilidade`, `AtaqueTotalModo`.
- **`HitLocationRules.kt`** — Dano localizado (crânio ×4, vitais ×3 perf, teto de membro). 🔴 **Deixou de ser porte fiel da Mesa Virtual no MB-7b:** o JS calculava o teto como `ceil(PV × 0,5)` e errava 1 ponto com PV **par** (o livro manda 6 onde ele dava 5), e usava `0,33` em vez de `1/3` nas extremidades. O teto agora **delega** para `FerimentoPorLocalRules.minimoQueIncapacita` — a segunda cópia da regra foi deletada, que é o que tinha permitido uma delas ficar errada em silêncio. ⚠️ O **olho** ficou fora deste motor de propósito.
- **`InjuryRules.kt`** — Choque, ferimento grave, cheques de morte, KO, recuperação de atordoamento, `ferir(Combatente)`.
- **`NpcCombatBrain.kt`** — Intenção tática do NPC (fuga por moral, arqueiro mantém distância, bruto avança).
- **`CombatResolver.kt`** — Modificadores de defesa (recuo/Defesa Total/apara extra/bloqueio 1×; **esgrima → apara extra −2**, param `esgrima`, Lote 375) + `resolverTroca` (ataque→defesa→dano→ferimento; crítico anula defesa).
- **`CombatSession.kt`** — **Orquestra o encontro:** `heroiAtaca(AtaqueHeroi,…)`, `npcIntencao`/`npcResolve` (defesa interativa), `heroiMove`/`heroiManobra`/`heroiAvaliar`, **`heroiApontar`** (mira → +Acc, Lote 373), `narrarTroca` (log evocativo + colchete técnico), `tipoDano`/`rolarDano` (`pa*`→`pi*`), `penalidadeDistancia` (MB p.550), **`parseAparar`** (E/D/Não, Lote 375). Regras por ataque: **reach/Máx** (`dist > alcance` → não alcança), **1/2D** (dano pela metade), **Bulk** (Avançar-e-Atacar), **Apontar/Acc**, **Aparar E/D** (`opcoesDefesaHeroi(armaPronta)` tira aparar de arma à distância/Não/desbalanceada-já-usada via flag `atacouDesbalanceada`). Tipos: `HeroiPerfilCombate` (defesa), `AtaqueHeroi` (arma escolhível: nh/dano/tipo/alcance/precisao/meioDano/magnitude/apararTipo), `ApararTipo`, `ResultadoCombate`.
- **`model/BestiarioModels.kt`** + **`domain/loaders/BestiarioCatalogo.kt`** — Catálogo de criaturas (`assets/bestiario.v1.json`) → `Combatente` (`novoCombatente`). Loader com cache. ⚠️ Gson não roda init de data class → `Bestiario.get()` busca direto (sem mapa cacheado).

### 32.4 UI e ponte de combate
- **`viewmodel/delegates/SagaCombatController.kt`** (2099 linhas, era 2243) — Embrulha `CombatSession` com estado Compose (`CombatUiState`/`CombatenteUi`/`FaixaDistancia`/`DefesaPendenteUi`) + corrotinas + ponte de defesa suspensa. `heroiApontar`; **`sacarArma(indice)`** (Saque Rápido = livre, senão Preparar gasta o turno, Lote 374); alvos corpo-a-corpo por reach. Devolve PV/saque/XP à ficha.
  - ⚙️ **Lote REFACTOR (jul/2026): a DECISÃO e a TRADUÇÃO saíram daqui para arquivos puros e testáveis** (o controller precisa de Android, não roda na JVM, e era onde os bugs TOK-8/9/10 nasceram):
    - **`domain/combat/hex/RegrasMovimentoTatico.kt`** (REFACTOR-1) — `podeMoverAgora`/`motivoNaoPodeMover` (turno? virada pendente? preso?) e `interpretarToque` (o que um toque no hex significa). A trava do TOK-8 virou teste unitário em `HexRegrasFacingTest`.
    - **`domain/combat/TraducaoFichaParaCombate.kt`** (REFACTOR-2) — `classeDaMagia`/`energiaDaMagia` (**o catálogo manda sobre a cópia da ficha**, bug MEC-42), `chaveNome` (normalização, MEC-43), `construirPerfilHeroi`/`construirAtaques` + ajudantes (`rdHeroi`/`acharPericiaDaArma`/`reachParaMetros`/`ehDuasMaos`/`melhorPericiaDesarmada`). Testes em `TraducaoFichaParaCombateTest`.
    - O controller agora **coleta o estado e obedece** — não decide nem traduz.
- **`ui/saga/CombatUi.kt`** (1610 linhas, era 2000) — `CombatTracker` (faixas, barra de PV, postura/condições), `SeletorDeArma`, `ManeuverCards` + sub-diálogos (alvo/local, Mover, Avaliar, Apontar, Postura, Virar, Trocar arma), `MenuTaticoDoToken`, `DefendaSeCard`, overlays. `OpcaoRadio` é `internal` (compartilhado). TalkBack em tudo.
- **`ui/saga/CombatUiConjuracao.kt`** (428 linhas, REFACTOR-3) — UI de CONJURAÇÃO recortada do CombatUi: `SubDialogoConjurar` (2 passos, busca), `PainelRitual` (C12), `BotaoConjurarFaixas`/`ZonasAtivasFaixas` (P12/C11 no modo faixas). Composables `internal`.

### 32.5 Regras de arma no combate — COMPLETAS (Lotes 371-375)
Stats de arma vêm do catálogo → ficha (`Equipamento.arma*`) → `AtaqueHeroi`: **reach** ("C"/"1"/"2", engajamento), **Acc + Apontar**, **1/2D** (meio dano), **Máx** (não alcança além), **Mover-e-Atacar** (CaC −4+teto / à distância −2 ou **Bulk**), **Aparar E/D** (esgrima/desbalanceada/Não/à distância), **Sacar/Preparar** (arma pronta vs guardada; livre c/ Saque Rápido). Sentidos na Rolagem: §5 `SentidoRules` + §20 `DialogoSentidos`.

### 32.6 Componentes Visuais e Efeitos Mágicos (Saga UI)
- **`ui/saga/DefesaPorTiming.kt`** — Mecânica visual de interatividade para defesa baseada em timing.
- **`ui/saga/EfeitoMagia.kt`** / **`ui/saga/EfeitoMagiaCanvas.kt`** — Estruturas e renderizador de efeitos visuais de magias no campo de batalha.
- **`ui/saga/HexCanvas.kt`** — Renderizador 2D do grid hexagonal (Canvas) no Compose.
- **`ui/saga/HexScene3D.kt`** — Cena 3D para o combate tático utilizando SceneView.

### 32.7 Combate Tático Hexagonal (Saga / VTT)
*Implementações do grid hexagonal para encontros táticos em `domain/combat/hex/`.*
- **`HexGrid.kt`** / **`HexCoord.kt`** — Implementação e coordenadas geométricas da malha hexagonal.
- **`HexCombatState.kt`** / **`HexCombatSync.kt`** — Estado tático do encontro e lógica de sincronização (VTT/Saga).
- **`HexPortabilidade.kt`** / **`HexRegrasPosicionais.kt`** / **`HexRegrasFacing.kt`** — Regras de movimento, limites de terreno, vantagens de flanco/retaguarda e encaramento (facing).
- **`HexRender3D.kt`** / **`HexSetup.kt`** — Motores de renderização 3D e preparação do cenário.
- **`HexTaticaNpc.kt`** — IA posicional dos NPCs avaliando grid.
- **`HexTaticoDemo.kt`** — Demonstração e fluxo de testes táticos independentes do modo principal.

### 32.9 PILAR MAGIA no combate (Lotes MA/MEC/AR/A1/P/C — jul/2026)
*O motor de conjuração vive em `domain/magic/` (puro) e é orquestrado por `CombatSession`/`SagaCombatController`. Onde está cada coisa:*
- **`domain/magic/MagicMechanics.kt`** — a `MagiaMecanica` (schema curado ao lado da descrição fiel) e o `BuffAplicado`. Campos por eixo: dano (`danoPorEnergia`/`danoFixo`/`elementoDano`), condição, buff (`buffAtributo`/`buffImunidade`/`buffAfetaInsubstancial`), zona, **feixe** (`feixePenalidadeDx`/`feixeBloqueavel`), `naoAfeta` (tipo de criatura). ⚠️ **`soNarrado`**: descarta o buff antes de aplicar — **todo campo novo tem que entrar nele** (armadilha do MEC-14).
- **`domain/magic/MagicCore.kt`** — custo (`custoAjustadoPorNH`, redução por NH alto), resultado da operação (sucesso decisivo perdoa energia), **`RitualDeConjuracao`** (C12: gestos/voz/passos + caprichar, `tempoAjustado`).
- **`domain/magic/MagicCasting.kt`** — `ContextoConjuracao`, `nhEfetivo` (o ritual entra como parcela nomeada), `custoTotal`.
- **`CombatSession.kt` — funil de dano mágico**: `aplicarDanoMagico` (checa imunidade por elemento A1 e `naoAfetaTipo` A1-b **antes** de rolar; `brutoForcado` para explosão), `resolverFeixe` (P9: DX−4/Ataque Inato, esquiva/bloqueio, nunca aparar; `bloqueioNpc`), `resolverExplosaoDoProjetil` (P5: alvo cheio + respingo dividido por `3×dist`; injeção `vizinhosDoImpacto`), `resolverArremessoProjetil` (P6/P11), `golpeContraInsubstancial` (A1-c: arma atravessa espírito).
- **`CombatSession.kt` — zonas persistentes (P1b)**: `ZonaPersistente` (raio `var` p/ C11 encolher, `estreou` p/ regra da estreia TOK-9, `ordinal` p/ log TOK-10), `tiqueDasZonas` (não acumula a mesma magia — `zonaSuplantadaPara`), `encolherZona`, `registrarZona`.
- **`SagaCombatController.kt`** — pontes: `heroiConjurar`/`iniciarMiraArea` (carregam o ritual), `resolverAreaPorFaixa` (P12: área sem grade), `resolverMiraAreaNoHex` (grid), `instalarOcupacaoDeZonaPelaGrade`/`instalarVizinhosDeImpactoPelaGrade` (posição real por hex), `zonasAtivasUi`/`encolherZona`.
- **`ui/saga/CombatUi.kt`** — `SubDialogoConjurar` (2 passos, busca), `PainelRitual` (C12), `BotaoConjurarFaixas`/`ZonasAtivasFaixas` (P12/C11 no modo faixas).
- **Bestiário**: `NpcStats` ganhou `imunidades`/`tipoCriatura`/`rdNatural`; `BestiarioModels` lê `tipo`/`imunidades` do JSON. Espectro (insubstancial) e os mortos-vivos marcados.
- **Rede de invariantes**: `test/…/combat/CombateInvariantesTest.kt` (SIM-1) — 200 combates aleatórios afirmando o que nunca pode acontecer. Cobre o **motor**, não o controller.
- **Estado das pendências de magia**: `docs/pendencias/PENDENCIAS.md` (sempre a fonte da verdade).

### 32.8 Pendências
- **Validação no aparelho**: fila em `docs/pendencias/PENDENCIAS.md §3` e na memória `project_testes_aparelho_pendentes`.
- **Magia**: A2 (visibilidade, ~139 magias, alto risco de integração — estender o SIM-1 antes), vulnerabilidade por tipo de dano, projeção/knockback dos Jatos. C8/C10/P13 bloqueados ou vetados.
- **Combate base:** CdT/Recuo/rajada, Ataque Total à distância (+1). Fases C/D/E do plano Saga.

---

> [!TIP]
> **DICA PARA O AGENTE**: Ao modificar regras de combate ou magias, rode `NexusArcanoLoteFCanonicScenarioTest.kt`, `RulesLayerTest.kt` e a suíte `domain/combat/*Test.kt` (Saga). **Regra de combate só se implementa lendo a fonte literal: `assets/chunks.jsonl` (Códex), não só o resumo do `Skill_GURPS.MD`** — ver `ARQUITETURA_MESTRE_IA.md §10`.
>
> **Sobre o Mestre IA (pós-Lote 328):** o AUDITOR não usa mais RAG semântico — usa
> `localizar_no_codex` + `ler_pagina` (`MestreIARepository`). Para ajustar a busca/ranking do
> Auditor, mexa em `MestreIARepository.rankearPorBM25` — **NÃO** no `MestreIAGraphEngine` (legado
> p/ Auditor). Os 3 dicionários de sinônimos (`MestreIAPlanner`, `MestreIAGraphEngine`,
> `MestreIAQueryEngine`) só importam para o GraphEngine/Forjador/Voz, não para o Auditor.
> Antes de mexer em qualquer arquivo do Mestre IA, leia `ARQUITETURA_MESTRE_IA.md §5` (código
> legado/morto e desde quando) para não reanimar algo descontinuado.
