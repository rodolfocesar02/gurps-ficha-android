# PLANO — PILAR MAGIA (Saga)

Integra as regras de magia do GURPS (Módulo Básico p.6–14 / livro Magia) ao modo Saga, nos
**dois palcos**: narrativa (fora de combate) e combate tático (no grid VTT 2D).

## Decisões travadas com o usuário (14/jul/2026)
1. **Só o herói conjura mecanicamente** por enquanto. Magia de NPC (xamã goblin etc.) fica
   NARRATIVA (o Narrador descreve). O "cérebro de magia" do NPC é um lote FUTURO deferido.
2. **Entrada da conjuração no combate = chip `🔮 Conjurar` dedicado** no menu do token do herói
   (separado do Concentrar-se genérico), no mesmo padrão do chip "Trocar arma" (TOK-6b-3).
3. **Regra de ouro (definida no MA-1):** automatizar a ESPINHA (NH efetivo → rolagem → resistência
   → custo de FP → dano/condição) e DELEGAR ao Narrador o efeito exótico que não cabe em regra
   geral (Criar Objeto, Teleporte, Reanimar…). Classe ESPECIAL do parser já marca esses.

## O que JÁ existe (alicerce — não reimplementar)
- **MA-1 (motor puro, `domain/magic/`)** ✅: `MagicCore.kt` (mana ambiente, redução de custo por NH,
  custo de área × raio, custo por MT, penalidade de distância, penalidade por múltiplas magias
  ativas, classificação da operação decisivo/sucesso/fracasso/crítico, tabela de choque de retorno,
  `MagiaAtivaNoCombate` + `MagicActive.avancarTurnoSegundos` tick). `MagicClass.kt`
  (`MagicClassParser` tolerante: classe + resistência dos 879 feitiços, 99,89%). Testado.
- **Ficha**: `Personagem.magias: List<MagiaSelecionada>` com `calcularNivel(p, aptidao)` = o NH da
  magia (IQ/Muito Difícil + Aptidão Mágica + pontos).
- **`MagicEngine` (domain/engine)**: checagem de pré-requisitos + redução de energia (já usado no
  fluxo de rolagem manual da ficha, fora da Saga).
- **Catálogo** `assets/magias2versao.json` (879 feitiços, descrição exata do livro, custo de FP,
  duração, página) via `CatalogLoaders`.
- **Tools do Narrador** (`NarradorTools` + executor): framework maduro; `gastar_recurso` já debita
  FP; `definir_cena` já existe (estender p/ mana ambiente).

## MA-2 — Resolvedor de conjuração (motor puro, sem UI) ✅ FEITO
`MagicCasting.kt` (+ `MagicEnergy`) em `domain/magic/`. Lido direto do livro (pt_magia p.5–15) pra
ficar fiel. Puro, determinístico (caller joga os dados), 29 testes.
- **`MagicEnergy.parse`**: campo `energia` do catálogo é string livre ("2", "1 a 3", "Varia",
  "1/2") → `CustoEnergia`. Tolerante, nunca lança.
- **`nhEfetivo(ctx)`**: NH básico + mana (−5 baixa) + distância (−1/m, só Comum/Área/Informação e
  se não tocar) + sem-ver-nem-tocar (−5) + múltiplas magias (−3 concentração / −1 andamento) +
  queimar PV (−1/PV). Devolve as PARCELAS (transparência p/ UI/feed).
- **`custoTotal(ctx, custo)`**: área × raio / Comum × MT ANTES de reduzir por NH (p.8); a redução
  usa o NH básico só com o −5 de mana baixa (não a distância); **Bloqueio NUNCA reduz** (p.12).
- **`resolver(...)`**: classifica 3d (MA-1), custo a pagar (decisivo perdoa; fracasso 1 exceto
  Informação; crítico tudo + choque), marca `exigeResistencia` (resistível só automática no
  decisivo, p.13).
- **`resolverResistencia(...)`**: Disputa Rápida margem-operador × resistência-alvo, empate favorece
  o defensor, **Regra do 16** p/ alvo vivo, Abascanto penaliza o operador (p.14).
- **Escala de efeito** (p.9/14): `tetoNiveisEfeito` = max(níveis da magia, Aptidão Mágica);
  1 pto = 1d dano / 1s cegueira.
- **`tempoOperacaoAjustado`** por NH alto (p.9): NH20–24 metade, 25–29 ¼, +metade a cada 5, piso 1s.
- **Fronteira**: o resolvedor NÃO toca Android — recebe NH básico (`MagiaSelecionada.calcularNivel`),
  Aptidão (`MagicEngine.getNivelAptidaoMagicaParaMagia`) e atributos já prontos. MA-3/MA-4 fazem a
  fiação e chamam este cérebro.

## MA-3 — Magia no COMBATE (grid + manobra)

### ✅ MA-3a FEITO — a espinha conjurável no grid
- Chip **🔮 Conjurar** no menu do token do herói (só se ele conhece magias) → `SubDialogoConjurar`
  (escolhe magia + alvo [inimigo ou "em mim"] + energia do Projétil). Conjurar = manobra Concentrar
  (gasta o turno).
- **`CombatSession.heroiConjurar(ctx, custo, energia, nome, alvoId)`**: rola pelos resolvedores do
  MA-2, paga a fadiga (PF), aplica o que é DERIVÁVEL por regra:
  - **Projétil**: dano 1d × energia investida ao alvo, com RD (Magia p.470).
  - **Resistível**: Disputa Rápida (HT/Vont/… do alvo, Regra do 16, Vontade≈IQ no NPC).
  - **Falha crítica**: choque de retorno (dano/atordoamento no operador).
  - Efeito bespoke → narrado pelo Mestre; o motor loga o fato.
- Controller extrai da ficha (NH via `calcularNivel`, Aptidão via `MagicEngine`, classe/energia do
  catálogo já em `MagiaSelecionada`); `CombatUiState.magiasConjuraveis` alimenta o seletor; a fadiga
  gasta sincroniza com a ficha.
- **+4 testes** de integração (`MagicCombatTest`): projétil causa dano + gasta PF; RD reduz; log
  sempre registra e PF nunca sobe; automagia não aplica dano de projétil.
- **Deferido honestamente p/ MA-3b+**: teste separado de Ataque Inato + esquiva do alvo (por ora o
  projétil acerta no sucesso do lançamento); conjuração multi-turno + interrupção; Toque; Bloqueio;
  Área centrada num hex; magias ATIVAS no combate + tick de manutenção; queimar PV; mana por cena
  (fixa em NORMAL); mapeamento de condição (Sono/Cegueira → `Condicao`); NPC conjurador.

### ✅ MA-3b FEITO — Projétil fiel + queimar PV
- **Projétil = 2 testes** (Magia p.12): lançamento + **Ataque Inato** para acertar (aprox. DX + SSR de
  distância); o alvo pode **ESQUIVAR** (ou bloquear), **nunca aparar**. Acertou → dano 1d × energia
  com RD. (O projétil ainda é resolvido no mesmo turno do lançamento — o multi-turno de "carregar"
  fica p/ MA-3c.)
- **Queimar PV** (Magia p.8): o mago paga parte do custo com PV (dói — `InjuryRules.ferir`) no lugar
  de PF; cada PV é −1 no NH (já no NH efetivo do MA-2). Stepper no `SubDialogoConjurar` (teto no
  custo estimado). PV e PF sincronizam com a ficha.
- **+2 testes** (`MagicCombatTest`): o projétil pode ser esquivado/errar (o 2º teste age); queimar PV
  fere o mago e penaliza o NH.

### ✅ MA-3c FEITO — conjuração MULTI-TURNO com interrupção
- Magias de vários segundos (tempo do catálogo reduzido por NH, Magia p.9) entram em
  **concentração**: o turno inicial é a 1ª manobra Concentrar; restam `tempo−1` turnos. `heroiConjurar`
  guarda `conjuracaoEmAndamento` e SÓ resolve no último turno (via `continuarConjuracao`).
- **Interrupção** (Magia p.7): o controller, após o turno do NPC, se o herói levou dano ou ficou
  atordoado, chama `interromperConjuracaoSeConjurando` — **atordoado PERDE automático**; ferido exige
  **Vontade−3** para manter. `abortarConjuracao` cancela sem custo (não gasta o turno).
- **UI**: card `🔮 Conjurando X — [Continuar] [Abortar]` (`CombateStatusTatico`); enquanto concentra,
  o menu do token e o movimento pelos hexes verdes ficam BLOQUEADOS (só continuar/abortar).
- **+5 testes** (`MagicCombatTest`, total 11): entra em concentração e só resolve no fim; atordoado
  perde automático; Vontade−3 falha perde / passa mantém; abortar limpa sem custo; 1s resolve na hora.

### ✅ MA-3d — pacote final COMPLETO (Área + Toque + Bloqueio + magias ativas)
- **✅ Área centrada num HEX**: no 🔮 Conjurar, magia de Área mostra um stepper de RAIO (custo × raio,
  p.11) e o botão vira "Mirar no grid"; o app entra em MIRA (`miraAreaPendente`) e o próximo toque num
  hex é o CENTRO. `heroiConjurarArea`: 1 teste de lançamento; o controller calcula quem está no raio
  (`HexGrid.range(centro, raio−1)`, 1 hex = 1 m) e a distância até a borda; cada alvo resiste sozinho
  contra a margem do operador (p.14); efeito bespoke → narrado (o motor lista atingidos × resistentes).
  Overlay "🎯 Toque o centro…" + Cancelar. +3 testes.
- **✅ Toque**: no 🔮 Conjurar, magia de Toque lança em si ("Carregar na mão"). `resolverConjuracao`
  no sucesso guarda `toqueCarregado`; o chip **✋ {magia}** aparece no menu de um inimigo ADJACENTE →
  `heroiEntregarToque`: ataque com a mão (aprox. DX) + defesa ativa do alvo; se defende, continua
  carregada; se acerta, descarrega e resistíveis fazem o 2º teste (Magia p.12). Chip **✋ Dissipar** no
  herói. +3 testes.
- **✅ Bloqueio**: as mágicas de Bloqueio conhecidas viram opções no card "Defenda-se!" (🔮 nome,
  valor = NH da magia). Ao escolher, o controller paga o custo (NÃO reduzido por NH, p.12) e o motor
  (`aplicarBloqueioMagico`) quebra qualquer conjuração em andamento; o sucesso (rolar ≤ NH) usa o fluxo
  de defesa normal. Não aparece contra golpe fulminante / pelas costas (opções vazias). +2 testes.
- **✅ Magias ATIVAS + tick**: após conjurar com sucesso uma magia de DURAÇÃO (temporária/duradoura,
  não-Projétil/Toque/Área), o controller a registra via `registrarMagiaAtiva`; o `avancarTurno` roda o
  tick (1s por turno do herói) com `MagicActive`: **cobra a manutenção** do PF (≈ metade do custo,
  reduzido por NH, p.15) ao completar o intervalo e **expira** as duradouras. Permanentes não cobram
  nem expiram. Pílula "✨ ativas" no canto do grid. +2 testes. Efeito de buff é bespoke → narrado.
- Seletor de magia: NH, custo, tempo de operação, classe; esmaece as impossíveis (mana nula, FP
  insuficiente, pré-requisito faltando).
- Mira pela CLASSE: Comum/Projétil/Toque → token do inimigo (penalidade de distância pela grade);
  Área → hex central (custo × raio); automagia → si.
- Tempo de operação: 1s resolve no turno; vários segundos → estado "conjurando" (Concentrar-se por
  N turnos; atingido + falha de Vontade = perde a concentração — MB p.363).
- Projétil = 2 testes (carregar + Ataque Inato, nunca aparável); Toque = mão + ataque c-a-c;
  Bloqueio = defesa reativa.
- Duração TEMPORÁRIA/DURADOURA → entra como `MagiaAtivaNoCombate` na `CombatEncounter`; tick a cada
  turno (cobra manutenção/expira) via `MagicActive`.
- Dano/condição aplicados pelos hooks existentes do motor de combate.

## ✅ MA-4 — Magia na NARRATIVA (tool do Narrador) FEITO
- Tool **`lancar_magia { magia, alvo?, energia_extra?, resistencia_alvo? }`** (`NarradorTools`, 19ª tool)
  + dispatch no `NarradorToolExecutor` + método `lancarMagia` na `CombatBridge`, implementado no
  `FichaSagaDelegate`: acha a magia no grimório, calcula NH (via `calcularNivel` + `MagicEngine`), rola
  3d e resolve pelo **mesmo `MagicCasting` do MA-2**; debita a fadiga direto na ficha; devolve JSON
  factual (resultado, custo pago, PF, resistência se informada, choque de retorno). O EFEITO é narrado.
- **Guardas**: bloqueada DENTRO de combate (lá é o jogador na tela); erro se a magia não é do herói.
- **Prompt do Narrador** atualizado: quando chamar `lancar_magia` (fora de combate) vs. o chip na tela.
- **+3 testes** (dispatch, guarda em-combate, guarda campos) + contagem de tools 18→19.

## MA-5 — Polimento + honestidade
- Mana ambiente por cena (estende `definir_cena`).
- Narração do choque de retorno (rótulos já no motor).
- Indicador de FP e de magias ativas na UI da Saga.
- Acessibilidade PraCego (contentDescription das magias/estados).
- Doc dos feitiços deferidos (efeitos bespoke que ficam narrativos).

## Deferido honestamente
- **NPC conjurador mecânico** (lote próprio futuro).
- Efeitos bespoke (Criar Objeto, Teleporte, Reanimar, etc.) → narrativos via ESPECIAL.

## Registro de execução
- [x] ✅ MA-1 — motor puro (mana/custo/área/distância/choque/tick + parser de classe)
- [x] ✅ MA-2 — resolvedor `MagicCasting` + `MagicEnergy` (cérebro compartilhado, fiel ao livro pt_magia p.5–15, 29 testes)
- [x] ✅ MA-3a — espinha conjurável no grid (chip 🔮 Conjurar + seletor + `heroiConjurar` no motor: Projétil com dano/RD, resistência, choque; 4 testes de integração)
- [x] ✅ MA-3b — Projétil 2 testes + esquiva do alvo (nunca aparar) + queimar PV (2 testes novos)
- [x] ✅ MA-3c — conjuração multi-turno + interrupção (Vontade−3 / atordoado; continuar/abortar; 5 testes novos)
- [x] ✅ MA-3d — Área no hex + Toque + Bloqueio + magias ativas + tick (4 sub-lotes, 10 testes novos)
- [x] ✅ MA-4 — tool `lancar_magia` na narrativa (executor + bridge + delegate + prompt, 3 testes)
- [ ] MA-5 — polimento (mana por cena, choque narrado, FP/magias ativas na UI, PraCego, doc dos deferidos)
- [ ] MA-4 — magia na narrativa (tool `lancar_magia`)
- [ ] MA-5 — polimento + honestidade
