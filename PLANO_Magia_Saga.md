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
- Chip `🔮 Conjurar` no `MenuTaticoDoToken` do herói (só se conhece magias).
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

## MA-4 — Magia na NARRATIVA (tool do Narrador)
- Tool `lancar_magia { magia, alvo?, energia_extra? }` + executor: resolve conjuração fora de
  combate pelo mesmo `MagicCasting`; debita FP via `gastar_recurso`; devolve resultado FACTUAL.
- Orientação no prompt do Narrador: quando chamar `lancar_magia` vs narrar direto.

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
- [ ] MA-3 — magia no combate (chip Conjurar + mira + multi-turno + magias ativas)
- [ ] MA-4 — magia na narrativa (tool `lancar_magia`)
- [ ] MA-5 — polimento + honestidade
