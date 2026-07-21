# PILAR MAGIA — Deferidos e Fronteiras Honestas

O que o motor de magia (MA-1..5) **automatiza** e o que fica **narrado pelo Mestre** ou **deferido**
para uma fatia futura. Registro honesto para não vender o que não entrega.

## A regra de ouro (decidida com o usuário)
O motor automatiza a **ESPINHA** de toda conjuração — NH efetivo (mana, distância, múltiplas magias,
queimar PV), rolagem 3d, classificação (decisivo/sucesso/fracasso/crítico), **resistência** (Disputa
Rápida, Regra do 16, Abascanto), **custo** de fadiga (×tamanho/área antes de reduzir por NH; Bloqueio
não reduz), **choque de retorno** e **duração/manutenção** (magias ativas). O **EFEITO** específico
que não cabe em regra geral é **narrado pelo Mestre**.

## Efeitos NARRADOS (não automatizados — o catálogo não tem campo estruturado de efeito)
- **Projétil**: dano 1d × energia com RD é regra geral (p.470) → automatizado (2 testes + esquiva).
- **Dano direto (Lote MA-6)**: magias de dano que NÃO são Projétil (jatos, dano de área…) ganharam a
  opção **"Causa dano (1d/energia)"** no seletor — o jogador marca e o motor aplica 1d×energia com RD
  (diretriz de Mágicas de Combate, p.14). Sem marcar, continua narrado.
- Todo o resto (Sono, Cegueira, Cura, Criar Objeto, Teleporte, Reanimar, buffs como Escudo→+DB…):
  o motor resolve o **lançamento + resistência + custo** e diz **quem foi afetado**; o Mestre narra o
  que acontece. As **magias ativas** (temporárias/duradouras) rastreiam **manutenção/expiração/
  visibilidade**, mas o **efeito do buff** é narrado.

## DEFERIDO (fatia futura própria)
- ~~**NPC conjurador**~~ ✅ **FEITO (Lote MA-7)**: o NpcStats ganhou `magias`; o cérebro do NPC lança
  quando tem mágica + fôlego + herói ao alcance; resolve pelo mesmo `MagicCasting` (o herói esquiva
  Projétil / leva dano com sua RD). Conceito de "conjurador/mago" sem mágica curada no bestiário ganha
  um "Dardo Mágico" padrão. **Refinamento futuro**: a defesa do herói vs mágica de NPC é rolada pelo
  motor (síncrona) — falta a versão INTERATIVA (card "Defenda-se!") e o NPC conjurar Área/Toque/buff.
- **Projétil — carregar em vários turnos** (Magia p.12): hoje o projétil é lançado no mesmo turno do
  lançamento (com 2 testes: lançamento + Ataque Inato + esquiva do alvo). O "aumentar por até 3s"
  antes de arremessar fica para depois.
- **Magia cerimonial** (assistentes, ×10 tempo, energia coletiva, p.12).
- **Cajados mágicos** (reduzir distância / carregar Toque, p.13).
- **Modificadores de longa distância** para Informação (tabela p.14) — a Informação hoje é resolvida
  narrativamente.
- ~~**Efeito do buff aplicado mecanicamente**~~ ✅ **FEITO (Lotes MEC-2 e MEC-4)** — o Escudo soma BD
  em esquiva/aparar/bloquear e a Armadura soma RD, via `heroiPerfil` (`CombatSession.kt:44-60`).
  **O que sobra** é catálogo, não motor: dos 179 buffs, 23 têm campos numéricos e funcionam; os
  outros **156 só têm `buffRotulo`** (rótulo em texto) e precisariam de curadoria da prosa.
  ⚠️ Esta linha ficou desatualizada por vários lotes e induziu a erro no `PENDENCIAS.md`.
- **Magia no modo FAIXAS**: a conjuração no combate só existe no modo TÁTICO (grid) — o chip 🔮 mora
  no token. No modo de faixas não há UI de conjurar.
- **PRECISÃO (Prec/Acc) do Projétil** (Lote MEC-15): o catálogo traz a Prec das 12 magias de projétil
  (ex.: Bola de Fogo Prec 1, Relâmpago Prec 3), mas a Precisão **só se aplica com a manobra Apontar** —
  e hoje o projétil é conjurado e arremessado **no mesmo turno**, com `heroiConjurar` chamando
  `limparApontar()`. Não há turno em que se possa mirar. Somar a Prec sem Apontar seria dar bônus de
  graça, o oposto da regra. Isto se destrava junto com o deferido **"Projétil — carregar em vários
  turnos"** logo acima; o maquinário de Apontar (Acc + mira de vários turnos + firmar) já existe e é
  usado pelas armas. Por isso o campo `precisao` NÃO foi adicionado ao schema: seria campo morto.
- **"+3 para resistir OU metade do dano"** (Lote MEC-15): o livro se contradiz entre duas seções — a
  seção *"Distância"* diz que além do 1/2D o ataque *"causa apenas metade do dano **E** ... é resistido
  com um bônus de +3"*, enquanto a seção *"Metade do Dano (1/2D)"* diz que o +3 vale *"**em vez de** o
  dano ser reduzido pela metade"*. Adotada a formulação inequívoca (**os dois**, da seção "Distância").
  Se em mesa o usuário preferir o "em vez de", é uma linha em `aplicarDanoMagico`.
- **Respingo do RELÂMPAGO EXPLOSIVO** (Lote MEC-14): o decaimento de explosão ("divide o dano por 3× a
  distância em metros") está implementado no ramo de ÁREA, e vale de verdade para a **Bola de Fogo
  Explosiva** e a **Bola de Relâmpagos** (ambas `entrega: area`). O Relâmpago Explosivo, porém, é
  `entrega: projetil` — o ramo de projétil acerta **um alvo só**, então o alvo direto leva o dano
  cheio (correto: distância 0) mas **quem está ao redor dele não leva respingo nenhum**. Espalhar a
  partir do ponto de impacto exigiria o projétil resolver contra um HEX em vez de um combatente.
  O campo `explosaoDivisorPorMetro: 3` já está no catálogo, esperando esse ramo.

## Simplificações honestas (fiéis o bastante, documentadas)
- **Tipo de dano do Projétil** aproximado por contusão (×1 de ferimento, como queimadura básica).
- ~~**Ataque Inato** aproximado pela DX~~ ✅ **RESOLVIDO (MEC-45)** — a perícia **existe** no catálogo; eu havia assumido que não. O arremesso usa o NH dela quando o herói a tem e **avisa no log** quando cai na DX.
- **Vontade do NPC** ≈ IQ (o bestiário não tem campo de Vontade separado).
- **Manutenção** ≈ metade do custo (p.15), reduzida por NH — o catálogo raramente traz o custo exato.
- **Distância na narrativa** abstraída (o `lancar_magia` assume contato/curta distância; o Narrador
  descreve a posição).
