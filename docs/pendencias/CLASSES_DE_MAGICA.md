# CLASSES DE MÁGICAS (Magia p.11–14) — regra × implementação

> **Origem.** O usuário perguntou se eu já tinha lido a seção *"Diferentes Tipos de Magia / Classes de
> Mágicas"*. Resposta honesta: **só em partes**. Eu vinha citando p.11 e p.12 para Projétil, Toque e
> Área conforme precisava, mas **nunca varri a seção inteira** — e esta varredura achou regras com
> efeito mecânico que nunca foram implementadas nem registradas em lugar nenhum.
>
> **Fonte:** `assets/chunks.jsonl`, páginas 11–14 de *Princípios da Magia*. Cada item foi conferido
> **no código**, não de memória.

---

## Resumo

| Classe | Estado geral |
|---|---|
| **Comuns** | ✅ o essencial está feito |
| **Área** | 🟡 falta custo mínimo e afetar parte da área |
| **Toque** | 🟡 falta o bloqueio de "não conjura enquanto sustenta" e o Aparar que não dispara |
| **Projétil** | 🔴 falta **carregar em vários turnos** e o **teste de Vontade ao ser ferido** |
| **Bloqueio** | 🔴 três regras estruturais faltando |
| **Informação** | 🔴 tudo narrativo (por projeto), mas há regras mecânicas não aplicadas |
| **Resistíveis** | 🟡 falta "optar por não resistir" e a regra de Área Resistível |
| **Encantamento / Especiais** | ⏸️ fora de escopo (itens mágicos / regra própria por magia) |

---

## 1. Mágicas Comuns

| Regra (literal) | Estado |
|---|---|
| Afeta **um objetivo por vez** | ✅ |
| Custo × (1 + MT) para Modificador de Tamanho positivo; **MT negativo não reduz** | ✅ `MagicCost.custoAjustadoPorTamanho`, ligado em `MagicCasting` |
| Penalidade = **distância em metros** se não puder tocar | ✅ `MagicDistance.penalidadeDistanciaMetros` |
| **−5 adicional** se não puder ver nem tocar | ✅ ligado (`"sem ver nem tocar"` no breakdown de NH) |
| *"A distância deve ser calculada no momento em que o teste é feito"* | ✅ (o grid dá a distância no instante) |
| **Barreiras físicas não afetam** mágicas Comuns | ✅ por omissão (nada bloqueia) |
| *"Nunca atinge o alvo errado, a menos que haja choque de retorno"* | ✅ por omissão |
| Direcionar por **local-alvo** ou por **objetivo não visto** (com risco de fracasso) | ❌ **não implementado** — é conjuração às cegas, hoje impossível na UI |

---

## 2. Mágicas de Área

| Regra (literal) | Estado |
|---|---|
| Custo real = **custo básico × raio em metros** (mín. 1 m) | ✅ `MagicCost.custoAreaPorRaio` |
| Custo básico **fracionário** (1/2, 1/10), gastando no mínimo 1 ponto | ✅ |
| Efeito alcança **até 4 metros acima** da superfície | ⏸️ sem eixo vertical no jogo |
| Penalidade de NH = distância até a **borda mais próxima** da área | ✅ (`distBorda` na mira de área) |
| Afeta **todos** os seres vivos dentro da área | ✅ |
| **Raio de 1 m = 1 hex; 2 m = hex central + adjacentes; 3 m = anel seguinte** | ✅ (`HexGrid.range`) |
| *"Algumas mágicas de Área especificam um **custo mínimo**: o operador sempre paga esse custo"* | ❌ **não implementado** — 0 ocorrências de `custoMinimo` no código |
| *"O operador pode escolher afetar **apenas partes da área**, mas o custo é o mesmo"* | ❌ não implementado |

---

## 3. Mágicas de Toque

| Regra (literal) | Estado |
|---|---|
| **Dois testes**: NH da mágica + ataque corpo a corpo | ✅ |
| **Sem modificador de distância** (é lançada sobre o próprio operador) | ✅ |
| Sustentar a mão carregada **não custa energia nem exige teste** | ✅ |
| Alvo pode usar **qualquer defesa ativa**; se defende, a mágica **não dispara** e continua carregada | ✅ |
| Se acerta, o ataque causa **dano normal** E a mágica afeta | 🟡 a mágica afeta (MEC-21); o **dano do soco** não é somado |
| Certas mágicas de Toque são **Resistíveis** → 2º teste do operador | ✅ |
| *"O operador **não pode fazer outras mágicas** enquanto sustenta uma mágica de Toque"* | ❌ **não implementado** |
| *"**Aparar** com essa mão ou cajado **não dispara** a mágica"* | ❌ não implementado |
| *"Se a mágica ignorar armaduras, nem Aparar desarmado nem bloqueio protegem"* | 🟡 `armadura: "ignora"` existe, mas não interage com Aparar/bloquear |
| **Cajados mágicos** (carregar em cajado, soltar dispersa, disputa pelo cajado) | ⏸️ deferido |

---

## 4. Mágicas de Projétil

| Regra (literal) | Estado |
|---|---|
| **Dois testes**: NH da mágica + **Ataque Inato** | ✅ (Ataque Inato aproximado por DX) |
| Concentra 1 segundo, testa, **sem modificador de distância** ao criar | ✅ |
| Energia investida até o **nível de Aptidão Mágica** | ✅ (MEC-9) |
| **Pode ser bloqueada ou esquivada, NUNCA aparada** | ✅ |
| **1d de dano por ponto de energia** | ✅ |
| **RD natural ou de armadura protege normalmente** | ✅ |
| Ataque à distância normal (tamanho, velocidade, distância) | 🟡 usa SSR de distância; tamanho/velocidade não |
| *"**Aumentar** o projétil por até 3 segundos, +1 a AM de energia por turno"* | ❌ **não implementado** (já era deferido conhecido) |
| *"Enquanto sustenta, pode **mover com Deslocamento total**, fazer **Aguardar** ou **Apontar**, ou atacar **com a outra mão**"* | ❌ não implementado |
| *"**Não pode fazer outra operação mágica** enquanto segura o projétil"* | ❌ não implementado |
| 🔴 *"Se **sofrer uma lesão** enquanto sustenta o projétil, faz **teste de Vontade**. Se fracassar, **o projétil o afeta imediatamente!**"* | ❌ **não implementado — regra com dente, achada só agora** |
| *"Viaja em linha reta: **barreiras físicas** o afetam como qualquer projétil"* | ❌ não implementado |

---

## 5. Mágicas de Bloqueio

| Regra (literal) | Estado |
|---|---|
| Defesa instantânea contra ataque físico ou mágica; conta como aparar/bloquear/esquivar | ✅ (`opcoesBloqueioMagico`) |
| 🔴 *"Só é possível operar **uma** mágica de Bloqueio **por turno**"* | ❌ **não implementado** |
| 🔴 *"**Não** é possível usar mágicas de Bloqueio contra um **golpe fulminante**"* | ❌ não implementado (o `golpeFulminante` existe no motor, mas não gateia isto) |
| 🔴 *"Interrompem **automaticamente a concentração** do operador — ele perde a mágica que preparava"* | ❌ não implementado |
| Sustentando Toque → não é afetada; sustentando Projétil → não pode aumentar, mas guarda | ❌ |
| 🔴 *"**Não sofrem redução de custo** em função de NH elevado"* | ❌ — `custoAjustadoPorNH` é aplicado a todas; Bloqueio é **exceção** |

---

## 6. Mágicas de Informação

| Regra (literal) | Estado |
|---|---|
| Paga o **custo total** mesmo fracassando | ✅ (`custoAPagar(..., ehInformacao)`) |
| *"O Mestre faz o teste **em segredo**"* | ❌ (o app mostra a rolagem) |
| *"Numa **falha crítica**, o Mestre **mente** para o jogador"* | ❌ |
| *"Só pode ser usada **uma vez por dia** por operador — exceto as de *Localizar*"* | ❌ |
| *"Não têm duração; **é impossível mantê-las**"* | ❌ |
| Penalidade de −1 por item conhecido semelhante ignorado na busca | ❌ |
| **Modificadores de Longa Distância** (tabela: 200 m = 0; 750 m = −1; … 1.500 km = −8; −2 por fator de 10 adicional) | ❌ já registrado como deferido |

---

## 7. Mágicas Resistíveis

| Regra (literal) | Estado |
|---|---|
| Funcionam **automaticamente** num sucesso decisivo | ✅ |
| Num sucesso normal, disputa contra a resistência do objetivo | ✅ |
| *"O objetivo **sempre** tem chance de resistir, **mesmo inconsciente**"* | 🟡 conferir: `melhorDefesaNpc` zera defesa de inconsciente, mas resistência é outro teste |
| *"O objetivo consciente **pode optar por NÃO resistir**"* | ❌ não implementado (importa para magia benéfica lançada em aliado) |
| **Área Resistível**: todos na área testam; a mágica só afeta quem tiver **margem inferior à do operador**; Abascanto vale **em dobro** | 🟡 a disputa por margem existe; o **dobro do Abascanto** não |

---

## 8. Dissipar mágica sustentada (p.14)

| Regra (literal) | Estado |
|---|---|
| *"Dissipar uma mágica de Toque ou Projétil sustentada é **ação livre**, a qualquer momento do turno"* | ❌ não implementado — hoje a mão carregada não tem como ser descartada |
| *"Pode **soltar o projétil no chão**, também ação livre; não fere o operador **a menos que seja explosivo**, mas atinge o que estiver embaixo — e projéteis de queimadura **começam incêndios**"* | ❌ |

---

## O que eu recomendaria atacar primeiro

1. 🔴 **Teste de Vontade ao ser ferido sustentando projétil** — é a regra mais perigosa que falta: hoje o mago segura uma Bola de Fogo, apanha, e nada acontece. Barato de implementar e tem consequência real.
2. 🔴 **As três de Bloqueio** (uma por turno; não vale contra golpe fulminante; não reduz custo por NH) — regras curtas, todas com efeito direto no equilíbrio.
3. 🟡 **"Não pode conjurar enquanto sustenta Toque/Projétil"** — uma trava, fecha as duas classes.
4. 🟡 **Dissipar como ação livre** — pequeno, e destrava o jogador que ficou com a mão carregada sem querer.
5. ⏸️ Informação e Longa Distância seguem narrativos por projeto (ver `PENDENCIAS.md`).
