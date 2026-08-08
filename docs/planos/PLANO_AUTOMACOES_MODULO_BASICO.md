# O que ainda dá para automatizar no Módulo Básico

*Varredura do `chunks.jsonl`, capítulo por capítulo, contra o que o app já faz.
03 de agosto de 2026.*

---

## Antes: uma coisa que descobri sobre o arquivo

⚠️ **O `chunks.jsonl` tem TRÊS livros com numeração de página sobreposta**:

| Livro | Faixa no arquivo |
|---|---|
| GURPS Gun Fu | p.1 – 49 |
| GURPS Magia | p.50 – 287 |
| **Módulo Básico** | **p.288 – 579** |

Ou seja: existem três "página 272" diferentes. Isso não é defeito — é o índice
de cada livro — mas explica por que buscar por número de página às vezes traz a
resposta errada. **Buscar por conteúdo é confiável; buscar por página, não.**

Os capítulos do Módulo Básico no arquivo:

| Capítulo | Páginas no arquivo |
|---|---|
| Evolução do Personagem | 295–298 |
| Lista de Características | 299–325 |
| Combate (manobras, defesas) | 326–350 e 367–389 |
| Testes de Habilidade | 351–361 |
| Choque e Estresse | 362–366 |
| Combate Tático | 390–395 |
| **Situações Especiais de Combate** | **396–421** |
| **Lesões, Enfermidades e Fadiga** | **422–448** |
| Criando Modelos | 449–463 |
| Tecnologia e Artefatos | 464–488 |
| O Mestre / Mundos de Jogo | 489–527 |

Os dois em negrito são onde está quase tudo que sobrou.

---

## O placar: o que já está feito

Não é pouco. Antes de listar o que falta, o que **já** existe em `domain/rules/`:

| Regra | Onde |
|---|---|
| Apontar, Precisão, mira acoplada, Telescópica | `ApontarRules` |
| Avançar e Atacar (−2 ou Magnitude; −4 e teto 9) | `AvancarEAtacarRules` |
| Pontos de impacto e arma do oponente | `LocaisDeAtaque` |
| Golpe Rápido e apara repetida | `GolpeRapidoEAparaRules` |
| Tabela de Velocidade/Distância | `TabelaVelocidadeDistancia` |
| Marcos de PV/PF e os testes que eles pedem | `MarcosDeVidaRules` |
| Testes de resistir (consciência, morte, veneno, medo) | `ResistenciaRules` |
| Testes de Sentidos | `SentidoRules` |
| Reação, Autocontrole, Sorte, Talento Instintivo, Visualização | 5 arquivos |
| Luz da cena e Visão Noturna | `IluminacaoRules` |
| Deslocamentos (voo, escalada, natação…) | `DeslocamentosRules` |
| Mão inábil, Zarolho, Pacifismo, Disopia, Desastrado | 4 arquivos |
| Atirador e Arqueiro Heroico | `AtiradorRules` |

✅ E uma que eu **achei que faltava e não falta**: a **Carga** (`nivelCarga`) já é
calculada e já desconta do Deslocamento e da Esquiva. O que falta ali é só
*explicar* na tela — cai no item MB-13 abaixo.

---

# Os candidatos, em três ondas

Ordenei por **quanto o jogador ganha ÷ quanto custa fazer**, não por ordem do
livro.

---

# ONDA 1 — alto valor, dado já existe

*Estas cinco não precisam de catálogo novo nem de decisão sua. O número já está
na ficha ou na arma; falta a conta e a caixinha.*

## MB-1 · 🔴 Modificadores de Combate — a tabela que resume tudo (p.548-549)

**O que o livro tem.** Duas listagens que são o resumo de todo o combate: os
modificadores de ataque corpo a corpo e os de ataque à distância. Postura do
alvo, postura sua, alvo agarrado, alvo imóvel, tiro às cegas, cobertura, alvo
deitado…

**O que o app já faz.** Distância, luz, mão inábil, Apontar, Avançar e Atacar.

**O que falta.** O resto da lista — e principalmente a **regra do asterisco**:

> Se qualquer modificador marcado com um asterisco (*) for aplicado, o **NH
> efetivo depois de todos os modificadores não pode exceder 9**.

⚠️ Isso é um **teto**, não uma penalidade — a mesma armadilha do Avançar e Atacar,
que já implementamos. Um espadachim NH 20 vai a 9. É a regra que mais escapa,
porque parece que "já está penalizado o bastante".

E mais uma trava que ninguém lembra:

> As penalidades combinadas de visibilidade **não podem exceder −10** (−6 se
> estiver acostumado à cegueira).

**Custo.** Médio. É uma lista de caixinhas no diálogo *Onde acertar*, que já
existe e já tem cinco fontes convivendo.

**Ganho.** Alto. É o coração do combate, e hoje o jogador soma na mão.

## MB-2 · 🔴 Recuo e fogo contínuo (p.549 e p.409)

**O que o livro diz**, na mesma página:

> Num ataque em fogo contínuo, o ataque atinge um **disparo adicional para cada
> múltiplo inteiro do Recuo na margem de sucesso**.

**Por que é barato.** O `Recuo` e a `CdT` **já estão na ficha** desde o Lote 371,
e o card de detalhe já os mostra. Falta só a conta depois da rolagem: *"acertou
por 7, Recuo 2 → 3 tiros acertaram"*.

**Custo.** Baixo. Uma regra pura e uma linha no resultado da rolagem.

**Ganho.** Alto para quem usa arma automática — hoje é a conta mais chata da mesa.

## MB-3 · Modificador de Tamanho do alvo (p.549)

> 2. Aplique o **Modificador de Tamanho (MT)** do alvo.

Um passo obrigatório do ataque à distância que o app pula. O MT do **personagem**
já existe na ficha (vem da raça); o que falta é perguntar o **do alvo**.

**Custo.** Baixo — um seletor no diálogo de mira, com os exemplos do livro
("humano 0, cachorro −2, cavalo +1").

## MB-4 · Cobertura (p.408 e p.549)

> Se o alvo estiver atrás de alguma cobertura, é possível escolher atacar sem
> penalidades e determinar aleatoriamente o ponto de impacto (…) ou apontar
> contra um local exposto.

Uma **escolha** com duas consequências diferentes, e hoje o app não oferece
nenhuma das duas.

**Custo.** Baixo-médio. Encaixa no diálogo de mira, ao lado dos pontos de impacto.

## MB-5 · Prender o fôlego (p.356)

> | Sem esforço | HT×10 segundos |
> | Esforço moderado | HT×4 segundos |
> | Esforço pesado | HT segundos |
>
> Multiplique por **1,5** se hiperventilar — ou por **2,5** com oxigênio puro.

**Por que entra na Onda 1.** É pura aritmética sobre a HT, que a ficha tem. Três
linhas de regra, um painel de leitura.

**Ganho.** Alto em campanha com mergulho, gás, vácuo ou estrangulamento — e é
número que ninguém decora.

---

# ONDA 2 — alto valor, precisa de dado novo ou decisão sua

## MB-6 · 🔴 Fadiga: estafa, fome, sede e sono (p.426-428)

O capítulo inteiro de custo e recuperação de PF. Hoje o app **guarda** os PF e
avisa nos marcos, mas não sabe **por que** eles caem nem quando voltam.

Entra aqui: custo de correr/nadar/lutar, **estafa**, fome, desidratação, sono
perdido, e a recuperação.

⚠️ **Precisa de uma decisão sua**: isso exige o app saber que **tempo passou** —
"faz 12 horas que ele não come". Hoje a ficha não tem relógio de campanha. Ou o
jogador informa, ou não dá para automatizar sozinho.

**Ganho.** Alto em campanha de exploração/sobrevivência. Baixo em campanha de
masmorra curta.

## MB-7 · Membros incapacitados (p.422)

> **Incapacitando membros** · **Desmembramento** · **Efeitos de lesões
> incapacitantes**

Braço inutilizado tira o ataque daquela mão; perna derruba e muda o Deslocamento.
O app já sabe de PV e dos marcos — o que falta é **que parte** do corpo levou.

⚠️ **Depende do ponto de impacto.** O `LocaisDeAtaque` já diz onde o ataque mirou,
mas a ficha **não guarda** o ferimento por local. Precisaria de campo novo no
personagem.

**Ganho.** Alto — muda o que o personagem consegue fazer, não só o número de PV.

## MB-8 · Mau funcionamento de arma de fogo (p.408)

> Uma arma irá enguiçar em vez de disparar em qualquer jogada de ataque com
> resultado **não-modificado maior ou igual ao seu Mauf**.
>
> | 3–4 | Problema mecânico ou elétrico |
> | 5–8 | Disparo falho |
> | 9–11 | Emperramento |
> | 12–14 | Disparo falho |
> | 15–18 | Problema mecânico e possível explosão |

🔴 **Bloqueio de dado.** Conferi as chaves do catálogo de armas de fogo:

```
alcanceDistancia, categoria, cdt, cl, custo, dano, grupo, id, magnitude,
nome, nt, ntRaw, observacoes, peso, precisao, recuo, reviewFlags, source,
stMinimo, tipo, tiros
```

**Não existe campo `mauf`.** Ele teria de ser extraído do livro e acrescentado nas
62 armas — é o mesmo tipo de trabalho do ARMA-1, e vale conferir se a tabela do
livro traz essa coluna ou se ela vem só em suplemento.

**Ganho.** Médio, e muito temático em campanha de pólvora negra ou NT baixo.

## MB-9 · Dano por queda e colisão (p.431-432)

A tabela de velocidade de queda está inteira no arquivo (1 m = velocidade 5, até
100+ m = 47), e o dano sai dela mais o peso.

**Custo.** Baixo — é tabela pura, do mesmo formato da `TabelaVelocidadeDistancia`
que já existe.

**Ganho.** Médio. Aparece pouco, mas quando aparece ninguém lembra a conta.

## MB-10 · Combate montado (p.397-399)

Movimento, ataques da montaria, armas de cavalaria, defesa montada, perda de
controle, diferença de altura.

⚠️ Depende do app saber que o personagem **está montado**, e a ficha não tem esse
estado. É um interruptor novo, no estilo dos Estados Temporários.

**Ganho.** Alto **se** a sua campanha usa montaria; zero se não usa. **Pergunta
para você**, não para mim.

---

# ONDA 3 — vale menos, ou é do Mestre e não do jogador

## MB-11 · Truques sujos, capas, chicotes, garrote (p.405-407)

Regras especiais por tipo de arma. São ~8 blocos pequenos e independentes.

**Por que fica para depois.** Cada um serve a uma arma específica, e o retorno por
regra implementada é o menor da lista. Mas é a onda mais **fácil de fatiar**: dá
para fazer um por lote, sem risco.

## MB-12 · Calor, frio, ácido, radiação, pressão (p.428-437)

Perigos de ambiente. São muitas tabelas e quase todas dependem do Mestre narrar a
situação.

**Minha recomendação:** ficar de fora por enquanto. O app é a **ficha do jogador**
— estas são tabelas do Mestre, e ele já tem o Auditor para consultá-las.

## MB-13 · ⚠️ Explicar a Carga que já existe

Não é regra nova. A `nivelCarga` já desconta do Deslocamento e da Esquiva, mas o
jogador **não vê** isso: a Esquiva aparece 8 e ele não sabe que seria 10 sem a
mochila.

**Custo.** Muito baixo — é uma notinha, no formato do `OrigemDosNumeros` que já
existe.

**Ganho.** Alto em confiança: número que muda sem explicação parece defeito. Já
aconteceu duas vezes neste projeto (o Mestre de Armas que "não aparecia" e a
arqueira que "não via o aviso de alcance").

---

# A ordem que eu recomendo

| Ordem | Lote | Por quê primeiro |
|---|---|---|
| 1 | **MB-13** (explicar a Carga) | Meia hora, e conserta uma desconfiança |
| 2 | **MB-2** (Recuo / fogo contínuo) | O dado já está na ficha desde o Lote 371 |
| 3 | **MB-5** (prender o fôlego) | Regra pura sobre a HT, sem dependência |
| 4 | **MB-3** (Modificador de Tamanho) | Um passo **obrigatório** do tiro que o app pula |
| 5 | 🔴 **MB-1** (Modificadores de Combate) | O maior ganho, e o teto de 9 é a regra que mais escapa |
| 6 | **MB-4** (cobertura) | Encaixa no mesmo diálogo do MB-1 |
| 7 | **MB-9** (queda) | Tabela pura, molde já existe |
| 8 | **MB-7** (membros incapacitados) | Precisa de campo novo na ficha — decisão sua |
| 9 | **MB-6** (fadiga) | Precisa de tempo de campanha — decisão sua |
| 10 | **MB-8** (mau funcionamento) | Precisa extrair o `Mauf` para 62 armas |
| 11 | **MB-10** (montaria) | Só se a sua campanha usa |
| 12 | **MB-11** (truques sujos etc.) | Fatiável, um por lote, sem pressa |

---

# Três perguntas que eu preciso que você responda

Não dá para decidir por você, e cada resposta muda o plano:

1. **A sua campanha usa montaria?** Se não, o MB-10 sai da lista e ninguém perde
   nada.
2. **Você quer que a ficha saiba que horas são na campanha?** É o que destrava o
   MB-6 (fome, sede, sono). Se a resposta for não, a fadiga fica só no custo de
   ação, sem o resto.
3. **O ferimento deve ficar guardado por local do corpo?** É o que destrava o
   MB-7. Muda a estrutura da ficha, então é decisão de arquitetura, não de tela.

---

# ⚠️ Uma nota sobre método

Duas coisas que eu quero deixar escritas, porque este projeto já foi mordido pelas
duas:

**Confirmar o dado antes de prometer a regra.** O MB-8 parecia fácil até eu olhar
as chaves do catálogo e ver que **não existe campo `Mauf`**. Se eu tivesse
planejado sem conferir, o lote quebraria no meio.

**Regra que precisa de estado novo não é "só uma caixinha".** MB-6, MB-7 e MB-10
mexem no que a ficha **guarda**, não no que ela **calcula**. Isso é outro tipo de
trabalho — e é por isso que estão na Onda 2 e 3, mesmo tendo ganho alto.
