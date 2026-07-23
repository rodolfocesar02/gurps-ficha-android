# PENDÊNCIAS — o mapa único do que falta

> **Por que este arquivo existe.** No teste de 18/jul o usuário disse: *"parece que to testando coisas
> que estão na fila pra ser integradas, e nos testes no aparelho estão aparecendo como bug, mas na
> verdade não foi adicionado mecanicamente"*. Ele estava certo. A informação existia, mas espalhada
> entre `MAGIA_DEFERIDOS.md`, `BURACOS_SCHEMA_MAGIAS.md`, `PROGRESS.md` e os planos — ninguém
> conseguia ver o todo. Este é o índice único.
>
> **Regra de manutenção:** ao fechar um lote, atualize aqui também. Se um item sair da lista, diga em
> qual lote saiu.

---

## 1. O número que explica quase tudo

**98 das 879 magias (11,1%) são executadas mecanicamente pelo motor** (medição de 22/jul, após os
lotes A1/P5/P9/C11/C12). O resto é narrado **por projeto** — não por bug. Além dos 98 contados pelos
campos do catálogo, o **P9** transformou 17 feixes (já contados em `dano`) de "acerta sempre" em
ataque de verdade, e o **P5** ligou a explosão do projétil.

| Efeito | Total | O motor executa | Observação |
|---|---:|---:|---|
| dano | 40 | **40** | inclui feixe (P9), explosão de projétil (P5), zonas (P1b) |
| condicao | 21 | **21** | atordoar, cegar, dormir, paralisar |
| buff | 179 | **34** | os outros 145 são rótulo-só; a triagem do P3 mostrou que a maioria precisa de substrato, não de curadoria — ver 2.1e |
| cura | 3 | **3** | Cura Superficial/Profunda/Superior |
| narrado | 379 | 0 | narrado por definição |
| ambiente | 110 | 0 | clima, luz, criar matéria |
| informacao | 82 | 0 | adivinhação, localizar, detectar |
| controle | 65 | 0 | dominar, convocar, controlar NPC |
| controle | 65 | 0 | dominar, comandar, mover objeto |
| dano | 40 | **40** | ✅ completo |
| condicao | 21 | **21** | ✅ completo |
| cura | 3 | **3** | ✅ completo |
| **Total** | **879** | **87** | |

**Consequência prática:** ao testar uma magia sorteada, a chance de ela ser mecanizada é ~10%.
É esperado topar com efeito narrado quase sempre.

### Como saber na hora se é bug ou não-implementado

Abra o Logcat filtrando por `tag:Saga_Combate`:

| O que aparece | Significa |
|---|---|
| `Efeito narrado pelo Mestre` | **Não é bug.** É uma das 792 narradas. |
| Números (dano, RD, teste, condição) | É mecânica. **Se o número estiver errado, é bug de verdade.** |
| `combate de teste RECUSADO: <motivo>` | O combate não abriu — o motivo está ali. |

⚠️ **Exceção que já mordeu:** a Morte Candente mostrava "narrado" **e era bug** — ela tem mecânica
curada, mas o caminho do Toque não a aplicava (corrigido no MEC-21). Ou seja: "narrado" numa magia
cujo efeito é `dano`/`condicao`/`cura` **é suspeito**.

---

## 2. NÃO IMPLEMENTADO — vai parecer bug quando você testar

### 2.1 Prioridade alta (bloqueiam teste de magias inteiras)

| # | O que falta | Afeta | Origem |
|---|---|---|---|
| P1a | ~~**Tique por turno, alvo único**~~ ✅ **FEITO (Lote MEC-22)** — Morte Candente e Morte Putrefata ferem a cada turno, a vítima testa HT, sucesso decisivo quebra a mágica | — | auditoria #11 |
| P1b | ~~**Tique por turno em ZONA**~~ ✅ **FEITO (MEC-46)** — `ZonaPersistente` + tique no avanço de turno + **área pintada na grade** (laranja). 7 magias curadas. ⚠️ PARA para teste no aparelho (UI). | Chuvas, Nuvens, Tempestade de Faíscas, Mau Cheiro |
| P2 | ~~**Manter magia com efeito continuado**~~ ✅ **FEITO (Lote MEC-22)** para as de tique — a mágica fica ativa, cobra manutenção por turno e exige concentração | — | MAGIA_DEFERIDOS |
| P3 | **Curar os buffs que só têm rótulo de texto** — 🟡 **parcial (Lote P3-1)**: 6 curadas, e a triagem dos 156 mostrou que a promessa antiga era falsa. Ver a seção 2.1e | 156 dos 179 buffs | ver correção abaixo |

> ✅ **Correção (18/jul)** — a versão anterior desta linha dizia *"Escudo não dá +DB, Armadura não dá
> +RD"*. **Está errado.** Conferido em `CombatSession.kt:44-60`: o BD mágico do Escudo soma em
> esquiva/aparar/bloquear e o `buffRd` da Armadura entra na RD — feito nos lotes MEC-2/MEC-4. O texto
> velho veio do `MAGIA_DEFERIDOS.md`, escrito **antes** daquilo existir, e eu o copiei para cá sem
> conferir. **O que realmente falta:** dos 179 buffs, **23 têm campos numéricos e funcionam**; os
> outros **156 só têm `buffRotulo`** (rótulo em texto). Mecanizá-los é extrair número da prosa —
> trabalho de curadoria de catálogo, como foi o MEC-2, não conserto de motor.

> ✅ **O MEC-22 fechou a metade sem UI.** O motor de tique existe e é reusável: `MagiaAtivaNoCombate`
> carrega a mecânica, e `tiquePorTurnoDasMagias()` resolve teste/dano/quebra no avanço do turno.
> ⚠️ **O que sobra (P1b) é a metade com UI**: uma zona de dano precisa ser **desenhada na grade**,
> senão o jogador perde PV sem causa visível — exatamente o bug já reportado uma vez. Por isso foi
> deixada para um lote próprio, que **para para teste no aparelho**.
> ⚠️ **Deferido honesto do MEC-22:** *"mortos-vivos não são afetados"* não é aplicado — o `NpcStats`
> não tem campo de tipo de criatura (mesma limitação da RD natural do Toque Candente).

### 2.1b Erros de DADO no catálogo (não são bugs de motor)

| # | O que está errado | Tamanho |
|---|---|---|
| D1 | ✅ **FECHADO (Lotes MEC-34/35).** Conferido contra o **PDF do livro** (285 páginas, `fitz`), comparando **739 magias pelo CORPO** — nunca pelo apêndice, que o usuário avisou ter erros (e tinha: era ele que dizia "Comum" para a Morte Putrefata, que no corpo é **Toque**). **O catálogo está muito bom: 6 divergências em 739.** Do meu MEC-32, **7 das 18 estavam ERRADAS e foram revertidas** (Anular Mágica, Decapitação, Dissipar Água, Bola de Relâmpagos e as 3 Metamorfose Parcial); **11 confirmadas certas**. |
| D1b | ✅ **Resolvido** — as 2 "divergências pré-existentes" eram **falso-positivo do meu casamento**: **Audição Remota** é `Informação` (campo, descrição e corpo do PDF concordam; o "Comum" vinha de uma tabela de referência cruzada) e **Conexão** casou com uma linha do **sumário**. Nada a corrigir. |

> ⚠️ Cada caso exige ler a descrição: várias magias começam com referência cruzada ("Como Ilusão
> Simples, mas…") em vez da linha de classe, então não dá para corrigir com regex.

### 2.1e P3 — a triagem dos 156 buffs (Lote P3-1)

> ⛔ **A promessa antiga estava errada.** Esta seção dizia que o P3 faria *"156 magias saírem de
> narrado para mecânica"*. Li os **156 rótulos um a um** e isso é falso: a grande maioria **não** é
> "extrair número da prosa" — é efeito **sem substrato no motor**. É o mesmo erro do texto do Escudo
> que já foi corrigido aqui: eu havia herdado a frase sem conferir o dado.

**Balde 1 — mecanizável (o que dá pra fazer):** ~8 mágicas.

| Mágica | Regra do livro | Estado |
|---|---|---|
| **Bloquear** | BD +1 a +5, 1 de energia por ponto, **um único teste de defesa** (p.101) | ✅ **P3-1** |
| **Robustez** | RD +1 a +5, 1 por ponto, **um único ataque** (p.101) | ✅ **P3-1** |
| **Fortalecer Vontade** | Vontade +1 por energia, máx +5 (p.100) | ✅ **P3-1** |
| **Enfraquecer Vontade** | Vontade −1 a cada **2** de energia, máx −5 (p.100) | ✅ **P3-1** |
| **Sabedoria** | IQ +1 a cada **4** de energia, máx +5 (p.100) | ✅ **P3-1** |
| **Tolice** | IQ −1 por energia, máx −5 (p.134) | ✅ **P3-1** |
| **Bênção / Maldição** | ±1 a ±3 em **todas as jogadas** | 🟡 **próximo lote** — a regra diz *"a modificação não afetará os sucessos e falhas críticas"*, e honrar isso exige mudar a classificação de crítico no `CombatResolver`. Meia regra seria pior que nenhuma. |

> 🔎 **Dois desses estavam bloqueados por notas VENCIDAS.** O `notas` do **Bloquear** dizia *"o motor
> NÃO tem campo para BD"* — mas `buffBd` existe desde o **MEC-4** (Escudo). O da **Robustez** dizia
> que o motor *"daria RD persistente"* — mas `buffUmUnicoUso` existe desde o **MEC-6**. As duas
> ficaram narradas por documentação desatualizada, não por limitação. Mesmo padrão dos 5 itens
> marcados ❌ na varredura de classes que já estavam feitos.

**Balde 2 — falta SUBSTRATO no motor (não é curadoria, é feature):** ~94 mágicas.

| Grupo | Qtd | O que falta |
|---|---|---|
| Sentidos e visões (Infravisão, Metalovisão, Ver o Invisível, Visão Noturna…) | ~35 | não existe sistema de Sentidos/Percepção no combate |
| Formas de corpo (Corpo de Ar/Pedra/Fogo/Gelo/Sombra…) | ~14 | são **metacaracterísticas inteiras** do MB |
| Imunidades (fogo, frio, ácido, veneno, doença, radiação…) | ~11 | não há flag de tipo de dano no `Combatente` — mesma classe do "mortos-vivos imunes" e da RD natural |
| Terreno e movimento (Atravessar Terra, Caminhar nas Paredes, Nadar, Retardar Queda) | ~12 | a grade não tem terreno, obstáculo nem eixo vertical |
| Perícias (Conceder/Requisitar Perícia, Serralheiro, Moldar Planta) | ~10 | o combate não roda perícias fora das de ataque |
| Roubo de atributo (Roubar Força/Graça/Sabedoria/Vigor) | 4 | o buff é de **um lado só**; roubar exige debuff no alvo **e** buff no operador, amarrados |
| Tamanho (Aumentar/Encolher/Outro) | 4 | MT muda ST/PV/Desloc por **multiplicação**; o substrato é aditivo |
| Acelerar | 1 | Padrão de Tempo Alterado = **ação extra por turno** |

**Balde 3 — narrativo, sem o que mecanizar:** ~54 mágicas (Banquete do Monge, Guarda-Chuva, Tepidez,
Vigília, Dom das Línguas, Memorizar, Persuasão…). O rótulo **é** a mecânica; o Narrador já o recebe.

> 📌 **Número honesto do P3:** de 156, **8 são mecanizáveis** (6 feitas, 2 no próximo lote), ~94
> precisam de feature nova e ~54 não têm o que mecanizar. Quem quiser mais cobertura de magia em
> combate ganha mais abrindo **Sentidos** ou **imunidade por tipo de dano** — cada um destrava uma
> dezena de mágicas de uma vez — do que continuando a curar rótulo por rótulo.

### 2.1c Classes de Mágicas (Magia p.11–14) — varredura completa

Documento próprio: **[CLASSES_DE_MAGICA.md](CLASSES_DE_MAGICA.md)** — **o capítulo 1 inteiro
(p.5–15)**, regra a regra, conferido **no código**. O PDF não foi necessário: o `chunks.jsonl` tem as
11 páginas e preservou até as tabelas.

✅ **Achado que invertia a direção do erro, já corrigido (MEC-25):** o MEC-9 deixava o jogo **mais
restritivo que a regra**. O livro diz que o teto de energia é *"o maior número entre os níveis da
mágica ou o nível de Aptidão Mágica"* (exemplo literal: Cura Profunda 1 a 4, com AM 10, vai a
**10 níveis**). Agora vale `max(faixa, Aptidão)`, com teste usando o exemplo do livro.

Achados que ninguém tinha registrado antes:

| # | Regra que falta | Por que importa |
|---|---|---|
| C1 | ~~**Vontade ao ser ferido sustentando projétil**~~ ✅ **FEITO (MEC-39)** — destravou com o P11: ferido segurando o projétil, testa Vontade; falha → ele dispara no próprio herói. Usa o sinal `choquePendente` do MEC-26. | — |
| C2 | ~~**Só UMA mágica de Bloqueio por turno**~~ ✅ **FEITO (Lote MEC-27)** | — |
| C3 | ✅ **já correto no resultado** (o crítico anula toda a defesa). 🟡 Resta só o jogador poder **gastar PF** escolhendo bloqueio num crítico que já ia passar — desperdício, não erro de regra. | — |
| C4 | ~~**Vontade−3 ao ser ferido/atordoado mantendo mágica de concentração**~~ ✅ **FEITO (Lote MEC-26)** — fracasso congela o tique do turno, falha crítica desfaz a mágica | — |
| C5 | ~~**Não pode conjurar enquanto sustenta Toque**~~ ✅ **FEITO (Lote MEC-28)**. A metade do Projétil é **moot** — ele nunca fica sustentado (mesma causa da C1). | — |
| C6 | ✅ **JÁ ESTAVA FEITO** — `dissiparToque()` existe, não gasta o turno e tem botão na UI. Listei por engano. | — |
| C7 | ~~**Mágicas não acumulam**~~ ✅ **FEITO (Lote MEC-29)** — relançar troca pela mais forte em vez de somar | — |
| C8 | ⛔ **SEM ONDE APLICAR** — não existe ação de cancelar mágica no app. Os únicos caminhos que encerram são os do MEC-23 (deixar acabar / não poder pagar), que por regra são **grátis**. Precisa antes de um botão de cancelar. | — |
| C9 | ✅ **JÁ ESTAVA FEITO** — `custoTotal` faz `.coerceAtLeast(custo.minimo)`. Procurei `custoMinimo`, identificador errado. | — |
| C10 | ⛔ **INIMPLEMENTÁVEL hoje** — as duas metades faltam base: "optar por não resistir" exige **aliados** (o jogo só tem herói × inimigos) e o "Abascanto em dobro" exige o campo **Abascanto** no `NpcStats`, que **não existe**. Mesma classe do C1/C8. | — |
| C11 | 🟡 **parcial (Lote C11 + UI-MAGIA-1, 22/jul)** — `encolherZona` reduz a área e loga; **expandir é recusado** (regra). Chip "Encolher para Nm" na UI. ⛔ **Deferido, e é a MAIOR parte**: o custo de manutenção proporcional. Zona **não tem manutenção** no motor (e está certo: paga-se a operação e ganha-se a duração inteira). Sem extensão de zona, não há proporcional a cobrar. | Menor |
| C13 | ~~**Comum em alvo ADJACENTE sem redutor**~~ ✅ **FEITO (Lote MEC-32)** — `tocando = distancia <= 1` | — |
| C12 | ~~**Rituais alternativos**~~ ✅ **FEITO (Lote C12 + UI-MAGIA-1, 22/jul)** — `RitualDeConjuracao` (gestos/voz/passos + caprichar): omitir penaliza (−2/−4, somam), caprichar dá +1 **dobrando o tempo**. Entra no NH como parcela nomeada. Painel 🕯️ Ritual recolhido por padrão. | Menor |

✅ **Saíram da fila (20/jul):** **Aptidão Mágica destrava o teto de energia** — feito no **MEC-25**;
e **"Bloqueio não reduz custo por NH"**, que eu havia listado por engano — **já estava implementado
e com teste** desde antes.

> ⚠️ **Honestidade sobre o processo:** eu vinha citando p.11/p.12 caso a caso conforme precisava,
> mas nunca tinha lido a seção inteira. Estas 8 regras estavam invisíveis — não por decisão, por
> omissão. É o mesmo padrão dos documentos desatualizados: **o que não é varrido inteiro, engana**.

### 2.1d Projéteis ESPECIAIS que não são arremessados

| # | O que falta | Afeta |
|---|---|---|
| P13 | ⛔ **VETADO pelo usuário (21/jul)** — a Bola de Relâmpagos segue tratada como projétil comum. *"não vamos criar várias linhas de código por causa de 1 única magia"*. Decisão de escopo, não limitação técnica. | Bola de Relâmpagos |

### 2.2 Regras de magia específicas

| # | O que falta | Afeta |
|---|---|---|
| P4 | ~~Bandas de distância do **Lampejo**~~ ✅ **FEITO (MEC-37)** — bandas + rider de ofuscamento (−N nas perícias de combate, com timer) — mecânica reusável nos Jatos (P9) | Lampejo | ✅ VALIDADO no aparelho 21/jul |
| P5 | ~~**projétil-contra-HEX**~~ ✅ **FEITO (Lote P5, 22/jul)** — `resolverExplosaoDoProjetil`: o alvo leva dano cheio, os vizinhos dividem por `3×distância`. O dado rola **uma vez** (campo `brutoForcado`). Ponto de injeção `vizinhosDoImpacto` (faixas no motor, hex real no controller), como o P1b. | Relâmpago Explosivo |
| P6 | ~~**Precisão do projétil**~~ ✅ **FEITO (MEC-40)** — Apontar antes de arremessar soma a Precisão (+ mira de vários turnos). Chip 🎯 Apontar no token inimigo. 12 magias curadas. | 12 projéteis |
| P7 | ~~**RD natural × armadura** do Toque Candente~~ ✅ **FEITO (MEC-38)** — campo `rdNatural` no bestiário + `armadura: "ignora_vestida"` → ignora a vestida, natural protege | Toque Candente | ✅ VALIDADO no aparelho 21/jul |
| P8 | ~~Degrau de custo dobrado (2d-2)~~ ✅ **FEITO (MEC-36)** — `danoDeAreaComDegrau`; limiar = custo-base 2 dobrado. ✅ O payoff pleno chegou com o P1b (MEC-46): o degrau agora vale em **cada tique** da zona. | Chuva de Fogo/Pedras | ✅ VALIDADO no aparelho 21/jul |
| P9 | ~~**resolução de FEIXE**~~ ✅ **FEITO (Lote P9, 22/jul)** — `resolverFeixe`: DX−4 (ou DX−2 nos Sopros da boca) ou o NH da perícia Ataque Inato **sem** redutor; o alvo esquiva ou bloqueia, **nunca apara**. 17 magias curadas. Exceção do **Jato de Ácido** (não bloqueável) travada por teste. **Deferidos**: projeção/knockback (exige direção na grade) e o "dobro em criaturas de fogo" (falta o eixo de vulnerabilidade). | Jatos, Sopros |
| P10 | ~~Raio mínimo de 2m~~ ✅ **FEITO (MEC-36)** — `raioEfetivo`; a mira eleva o raio ao mínimo | Nuvem de Faíscas, Sono Coletivo | ✅ VALIDADO no aparelho 21/jul |
| P11 | ~~**Projétil carregado em vários turnos**~~ ✅ **FEITO (MEC-39)** — carregar/aumentar (até 3s, +Aptidão/turno)/arremessar/dissipar, aditivo ao one-shot. UI: botão "Segurar" no diálogo + chips Arremessar/Aumentar/Dissipar no token. **Cerimonial VETADO pelo usuário.** ⚠️ PARA para teste no aparelho (UI). |
| P12 | ~~**conjurar no modo de faixas**~~ ✅ **FEITO (Lote UI-MAGIA-1, 22/jul)** — botão 🔮 Conjurar no painel de combate reusa o `SubDialogoConjurar`; área sem grade resolve por FAIXA (`resolverAreaPorFaixa`, centro = herói). ⚠️ PARA para teste no aparelho. | combate sem grade |

### 2.3 Combate — "fora do escopo" cuja justificativa CADUCOU

O `Combate.md` tem 13 itens marcados *fora do escopo*. **Cinco foram excluídos porque "não há grade
de hexágonos"** — e a grade foi construída depois (HEX-1..9 + VTT 2D). Conferido item a item **no
código** (não só no documento), porque o `Combate.md` está desatualizado em relação à realidade:

| Item | Justificativa original (hoje falsa) | Situação REAL no código |
|---|---|---|
| **Passo** | *"o Saga usa faixas abstratas"* | ✅ **Já implementado** (35 ocorrências no motor). Só o `Combate.md` está desatualizado — corrigir a marcação. |
| **Passando por Outros** | *"sem grade de hexágonos"* | 🟡 **Parcial**: o BFS bloqueia hex de inimigo (conservador). Falta liberar atravessar **aliado** (MB p.389). |
| **Evadir** | *"no modelo de faixas o herói se move livremente"* | ❌ **Não implementado** — há TODO explícito em `HexSetup.kt:50`. |
| **Espaçamento** | *"abstraído no tracker de faixas"* | ❌ **Não implementado** (0 ocorrências). |
| **Superpenetração e Cobertura** | *"exige posicionamento"* | 🟡 **Parcial**: cobertura existe como **modificador situacional manual**; cobertura por terreno/posição e superpenetração não. |

Os outros 8 continuam legitimamente fora (perícias de Artes Marciais, cinematográfico, montaria).

> ⚠️ **Lição registrada:** a marcação do `Combate.md` não é confiável sozinha — o Passo aparece como
> "fora do escopo" e está pronto há lotes. Ao revisitar, confira o código.

### 2.4 GURPS Artes Marciais — o mapa próprio (`docs/pendencias/Artes_Marciais_Regras_Combate.md`)

O Artes Marciais tem **inventário próprio**, com 62 itens classificados. Situação depois de conferir
**no código** (o documento estava desatualizado, como o `Combate.md`):

| Marcador | Qtd | Significado |
|---|---:|---|
| ⚪ JÁ FEITO | 9 → **13** | +4 remarcados agora: eram listados como pendentes e estão prontos |
| 🟢 FIT | 6 → **2** | codável com o que já existe |
| 🟡 PARCIAL | 39 | precisa de dado novo (perícia de luta do NPC, qualidade de arma, durabilidade de objeto) |
| 🔴 FORA | 17 | posicional, montaria, cinematográfico ou construção de personagem |

**Corrigidos agora (estavam como pendentes, mas já existem):** Chaves/imobilizações/estrangulamentos
(PONTE-1), **Ataque Dedicado** e **Ataque Defensivo** (PONTE-4), **Ataque Telegráfico** (PONTE-3).
O Sangramento das *Lesões Realistas* também saiu no PONTE-2.

**O que de fato falta, por ordem de sinergia com o motor atual:**

| Bloco | Itens | Por que trava |
|---|---|---|
| **Lesões Realistas** (p136–139) | resto do bloco além do Sangramento | 🟡 alta sinergia com o motor de dano — o melhor custo/benefício restante |
| **Opções de Defesa** (p121–123) | Aparar com armas desbalanceadas, defesas específicas | 🟡 várias encaixam direto |
| **Alvos específicos** | olhos, pontos de pressão | 🔴/🟡 exige tabela de locais estendida |
| **Modos de ataque alternativos** | Corte com a Ponta, Golpe com o Pomo, Caneladas | 🟡 exige a arma ter modos alternativos na ficha |
| **Combinações** (p109/80) | sequência fixa; falha cancela o resto | 🟡 novo tipo de manobra encadeada |
| **Detectando Fintas**, **Ataque Total "Longo"** (+1 alcance) | — | 🟡 pequenos, isolados |
| **Preparar** (p105–108) | Empunhadura Defensiva/Invertida, Sacar Rápido em Série | 🟡 o Saga não modela arco lateral nem série de saques |
| **Golpeando Escudos** | destruir escudo/capa | 🟡 exige durabilidade de objeto, que não existe |
| **Armas e Equipamentos** (p211–234) | qualidade, peso, balanceamento | 🟡/🔴 exige campos que o bestiário e o catálogo não têm |

> ⚠️ **Os 17 🔴 merecem reavaliação.** A legenda do documento diz *"modelo de FAIXAS, **sem
> hexágono**"* — escrita antes da grade existir. É a mesma justificativa caducada de 2.3.

### 2.5 Simplificações honestas (não são bugs, são aproximações documentadas)

- Tipo de dano do Projétil ≈ contusão (×1 de ferimento)
- Ataque Inato ≈ DX do herói (a perícia não existe na ficha)
- Vontade do NPC ≈ IQ (bestiário não tem Vontade)
- Manutenção ≈ metade do custo, quando o catálogo não traz o número exato
- **Aparar/Bloquear/escudo não são gateados por confisco de equipamento** (modelo de defesa app-wide)
- "+3 para resistir" além do 1/2D: o livro se contradiz entre duas seções; adotada a versão
  inequívoca (metade do dano **e** +3)

---

## 3. IMPLEMENTADO mas NÃO validado no aparelho

Aqui, se aparecer estranho, **é candidato a bug real** — vale reportar.

| Lote | O que faz |
|---|---|
| MEC-13 | Magia de objeto (Desintegrar, Fender…) não pode mirar criatura |
| MEC-14 | Explosão decai com a distância (÷ 3 × distância) |
| MEC-15 | 1/2D (metade do dano) e Alcance Máximo do projétil |
| MEC-17 | Condição com **prazo** — antes cegueira era eterna |
| MEC-18 | Condição exige **teste** (Jato de Som atordoava sem teste); cegueira dura por energia |
| MEC-19 | Fuga do gelo por teste de ST |
| MEC-20 | "Causa dano" só aparece em magia que pode causar dano (40, não ~800) |
| MEC-21 | **Toque aplica efeito** — antes nenhuma magia de toque fazia nada |
| TESTE-NPC | Modos Normal / Congelado / Boneco |
| TESTE-SANDBOX | O combate de teste explica por que não abriu |
| LOG-1 | Combate visível no logcat |
| TESTE-C | Regra de movimento tático testada no código real |
| TOK-6b-3 | Layout do grid protagonista |

---

## 4. Dívida técnica conhecida

- **Teste flaky pré-existente**: `NexusArcanoEngineLoteBGlobalTest.planejador_resolve_requisito_de_contagem_por_escola`.
  É o **único vermelho** de todos os gates. Está sendo corrigido em sessão separada.
- **Camada de ViewModel/UI sem teste automatizado.** Motivo estrutural: `SagaCombatController` exige
  `FichaViewModel`, que é `AndroidViewModel(Application)`, e `Application` não existe na JVM.
  **Robolectric foi testado e descartado** (18/jul): nenhum bug do app encontrado, ~15× mais lento,
  suporta só SDK 34 contra o alvo 35. O caminho aberto é `androidTest` no **celular físico** — a
  infraestrutura (`ui-test-junit4`) já está no `build.gradle`, sem uso.
- **Duas lições que já custaram bug**, ambas registradas:
  1. Teste que **copia** a regra em vez de chamá-la passa verde com o original quebrado (TESTE-C).
  2. Testar a regra ≠ testar que ela está **ligada** e é **alcançável** — o MEC-19 passou nos testes
     e nunca disparava em jogo, porque a magia é de Toque e o Toque não aplicava efeito (MEC-21).

---

## 5. Recomendação de ordem (atualizada 22/jul)

**O que já saiu da fila:** P1, P2, P4–P12, C1–C7, C9, C11, C12 e os lotes A1/A1-b/A1-c (imunidade
por elemento, tipo de criatura, insubstancialidade). O executável em combate está em **~120 de 879**.

**Bloqueados por falta de base (não é preguiça — falta o pré-requisito):**
- **C8** — cancelar mágica: não existe ação de cancelar no app; os caminhos que encerram hoje são grátis por regra.
- **C10** — "optar por não resistir" exige **aliados** (o jogo só tem herói × inimigos); o Abascanto exige campo no `NpcStats`.
- **P13** — vetado pelo usuário (Bola de Relâmpagos segue projétil comum).

**O que sobra e vale fazer, por retorno:**
1. **A2 — visibilidade** (sentidos + luz/escuridão): **~139 magias**, o maior prêmio. Mas mexe nos
   modificadores de ataque e defesa, o coração do combate. **Alto risco de regressão de integração**
   (perfil dos bugs TOK-8/9/10). Recomendação: **estender a rede de invariantes (SIM-1) para cobrir
   os modificadores ANTES** de mexer neles, e atacar em lotes pequenos.
2. **P3 — buffs restantes**: dos 145 buffs sem número, a maioria precisa de substrato (Sentidos,
   imunidades) que o A2 e o A1 destravam — não é curadoria. Ver seção 2.1e.
3. **Vulnerabilidade por tipo de dano** (o "dobro em criaturas de fogo" do Jato de Vapor, deferido no
   P9): fecharia o par do A1 (imunidade), reusando o mesmo eixo de `elementoDano`.
4. **Projeção/knockback dos Jatos** (deferido no P9): reusa o Empurrão, mas precisa de direção na grade.

> ⚠️ **Teto realista**: os 379 `narrado` são narrativa de verdade e **não** são fila de trabalho.
> Fazendo A2, o executável chega a ~250 (28%), não 879.

---

## 6. Nota de higiene do repositório

- `logcat_novo.md` (22 KB) é um despejo de logcat versionado na raiz. Não faz mal, mas é dado
  transitório — candidato a `.gitignore` se virar hábito, agora que o `tag:Saga_Combate` (LOG-1)
  dá o mesmo em tempo real.
- O trabalho do **Nexus Arcano** de outra sessão foi **commitado (22/jul, `793d59ae`)** com
  autorização do usuário: planejador de requisito por contagem de escola + pathfinder. O gate ficou
  verde na execução em que foi commitado; como o teste era **intermitente**, uma passada verde não
  prova que a intermitência acabou — quem fez a correção confirma.
- 🏗️ **BUILD-1 (22/jul)**: `org.gradle.parallel` + `org.gradle.caching` + `maxParallelForks` no
  Gradle. O gate caiu de **7-8 min para 1m36s**. Durante o trabalho, `./gradlew testVisualDebugUnitTest`
  (12s) basta; o `build` completo só antes de commitar.
