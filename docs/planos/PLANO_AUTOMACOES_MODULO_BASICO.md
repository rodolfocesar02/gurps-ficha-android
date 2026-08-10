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

## MB-6 · 🔴 Fadiga — o botão "PF" (desenho do usuário, 03/08)

> ✅ **FEITO** — versão 6.9-MB5 (10/08). ⏭️ Falta validar no aparelho (roteiro **T-PF**).

**A ideia, na sua palavra:** *"transformar a palavra PF na aba de Rolagem num
botão, e dentro dele colocar tudo isso, com as caixinhas de seleção de cada um,
o tempo e os cálculos ali. Ele escolhe, salva; quando fechar, os devidos redutores
vêm à tona na ficha. Depois pode entrar de novo no PF e tirar as seleções."*

⚠️ **Isso resolve a pergunta que eu ia te fazer.** Eu tinha listado o MB-6 como
bloqueado por "o app não sabe que horas são na campanha". O seu desenho **contorna
o problema**: quem informa o tempo é o jogador, dentro do painel. O app não precisa
de relógio — precisa de um formulário.

**O que entra no painel** (MB p.426-428):

| Fonte | O que o app calcula |
|---|---|
| Esforço em combate / corrida / natação | custo em PF por período |
| **Estafa** | o teste, e o efeito de falhar |
| **Fome** | perda por refeição perdida |
| **Desidratação** | perda por período sem água (bem mais rápida que a fome) |
| **Sono perdido** | perda por noite, e o acúmulo |
| **Recuperação** | quanto volta com descanso, sono e comida |

**Como funciona, seguindo o molde que já existe.** É o mesmo padrão do
`PainelEstadosTemporarios` (as nove desvantagens temporárias) e do
`PainelIluminacao`: caixinhas que o jogador liga, o app soma, e a origem de cada
ponto fica escrita. A diferença é que aqui o estado **persiste na ficha** em vez de
valer só para a rolagem — é uma condição do personagem, não da jogada.

**Custo.** Médio-alto. É o maior painel da aba, e mexe no que a ficha **guarda**.
Mas o molde existe e o risco é baixo.

**Ganho.** Alto, e não só em exploração: a **estafa** aparece em qualquer combate
longo, e hoje ninguém acompanha.

---

## MB-7 · 🔴 Ferimento por local — o botão "PV" (desenho do usuário, 03/08)

> ✅ **FEITO E VALIDADO NO APARELHO** (10/08). Entregue em 6.9-MB5 e depois
> refeito como silhueta tocável nos lotes **PV-1a..1d** (7.1 a 7.4), a pedido do
> usuário. Rendeu de brinde a correção do off-by-one do teto de membro no motor
> de combate da Saga (**MB-7b**, 7.0).

**A ideia, na sua palavra:** *"dentro do PV tem todas as partes do corpo que podem
ser atingidas (semelhante ao usado no ataque). O jogador coloca o dano que levou na
parte que o Mestre indicou, e o app já faz o trabalho — vê se incapacita, se
decepa. E como temos RD das armaduras e as partes que cada uma cobre, podemos usar
isso a nosso favor, com uma caixinha para selecionar o RD ou não: às vezes o
jogador não está vestindo a armadura, mas ela está comprada na aba de
Equipamentos."*

🔴 **A parte da armadura é a melhor ideia deste documento**, e eu fui conferir se o
dado aguenta. Aguenta:

O catálogo `armaduras.v2.json` (72 peças) tem **`locaisNorm`** — os locais já
normalizados — e eles **casam com os locais de acerto** do `LocaisDeAtaque`:

| Local na armadura | Quantas peças cobrem |
|---|---|
| tronco | 29 |
| virilha | 23 |
| pernas | 18 |
| braços | 17 |
| crânio | 13 |
| pés | 13 |
| mãos | 12 |
| pescoço | 11 |
| rosto | 7 |
| olhos | 1 |

E o campo `rd` não é só um número — traz `principal`, `secundario`, `flexivel`,
`frontalSomente` e `dividida`. Mais: **9 armaduras têm componentes por local**
(o traje que cobre várias partes com RD diferente em cada).

**O fluxo, então:**

1. O Mestre diz *"você levou 8 no braço direito"*.
2. O jogador abre o **PV**, escolhe **braço**, digita **8**.
3. O app procura, no inventário, que armadura cobre `bracos` — e mostra a RD dela
   **com a caixinha de "estou vestindo"**, marcada por padrão.
4. Desconta a RD, aplica o multiplicador do tipo de dano (corte ×1,5, perfuração
   ×1,5, contusão ×1…), e diz o que aconteceu: ferimento normal, **membro
   incapacitado** ou **decepado**.
5. Os PV caem, e o efeito fica registrado no local — o braço incapacitado tira o
   ataque daquela mão.

⚠️ **A caixinha "estou vestindo" é a parte que faz isso funcionar de verdade.**
Sem ela, o app assumiria que tudo que está comprado está no corpo — e o jogador que
carrega uma armadura de reserva na mochila ganharia RD de graça. É a mesma distinção
que o `confiscado` já faz na Saga: estar **na ficha** não é estar **em uso**.

**⚠️ O que isso muda na estrutura.** A ficha passa a guardar **ferimento por
local**, não só o total de PV. É campo novo no `Personagem` — a mesma classe de
mudança do `armaPrecisaoAcessorio`, mas maior.

**Custo.** Alto — é o maior lote da lista. **Ganho.** O maior também: muda o que o
personagem **consegue fazer**, não só um número.

---

## MB-8 · 🔴 Mau funcionamento — o bloqueio NÃO existe (corrigido em 03/08)

⚠️ **Eu estava errado neste item.** Escrevi que faltava o campo `Mauf` nas 62
armas e que seria preciso extrair do livro uma por uma. Você mandou o quadro da
regra, e ele desfaz o problema:

> O número de mau funcionamento é uma **função do nível tecnológico**: ele é
> **12 em NT3, 14 em NT4, 16 em NT5 e 17 em NT6+**.

Não é dado por arma — é **fórmula sobre o NT**. E o NT de toda arma já chegou ao
modelo no **Lote ARMA-1**. Conferido no catálogo:

| NT | Armas | Mauf |
|---|---|---|
| 2–3 | 2 | **12** |
| 4 | 5 | **14** |
| 5 | 6 | **16** |
| 6 a 11 | 49 | **17** |

Ou seja: **as 62 armas de fogo já têm Mauf hoje**, sem tocar em nenhum JSON.

**Como dispara** (seu desenho): igual às tabelas de crítico — sozinho, quando o
gatilho acontece.

> A arma emperra, erra o alvo ou falha de alguma outra maneira, se o resultado de
> **qualquer jogada de ataque** for **maior ou igual ao seu Mauf**.

⚠️ **"Qualquer jogada de ataque", e o resultado é NÃO-MODIFICADO.** É o dado cru,
não o NH efetivo — a mesma lógica do crítico. E não depende de ter errado: um 17
enguiça mesmo que o atirador tivesse NH 20.

Depois vem a tabela, com 3d:

| 3d | Resultado |
|---|---|
| 3–4 | Problema mecânico ou elétrico |
| 5–8 | Disparo falho |
| 9–11 | Emperramento |
| 12–14 | Disparo falho |
| 15–18 | Problema mecânico e **possível explosão** |

**O molde já existe e é exato.** O `domain/roll/CriticoRules.kt` + o asset
`tabelas_criticas.json` fazem isto hoje para Golpe Fulminante e Erro Crítico:
`ehTesteDeCombate(tipo)` decide se a tabela vale, `classificar(soma, nh)` decide se
disparou, e a tabela sai na tela. O mau funcionamento é uma **quarta tabela** no
mesmo arquivo, com um gatilho diferente.

⚠️ **Uma coisa que o livro deixa claro e o app precisa respeitar:** é **regra
opcional**. Precisa de um interruptor de campanha — ligado por quem quer pólvora
que falha, desligado por quem não quer.

**Custo.** Baixo-médio, agora que o dado existe. **Ganho.** Alto e muito temático em
NT baixo: em NT3 um **12** já enguiça, e isso acontece o tempo todo.

---

## MB-9 · Dano por queda e colisão (p.431-432)

> ✅ **FEITO** — versão 6.8-MB4 (10/08). Regra pronta e testada; **sem tela** —
> a queda entra no botão PV num lote futuro (roteiro **T-QD** esperando).

A tabela de velocidade de queda está inteira no arquivo (1 m = velocidade 5, até
100+ m = 47), e o dano sai dela mais o peso.

**Custo.** Baixo — é tabela pura, do mesmo formato da `TabelaVelocidadeDistancia`
que já existe.

**Ganho.** Médio. Aparece pouco, mas quando aparece ninguém lembra a conta.

# ⏸️ ADIADOS — o lembrete (decisão sua, 03/08)

*Três frentes que ficam registradas para quando fizer sentido. Nada aqui está
descartado; está **fora da fila atual**.*

## MB-10 · Combate montado (MB p.397-399)

- **O que é:** movimento montado, ataques da montaria, armas de cavalaria, defesa
  montada, perda de controle e diferença de altura.
- **O que já ajuda quando voltar:** o `LocaisDeAtaque` e a
  `TabelaVelocidadeDistancia` funcionam igual montado; e a regra *"usar o NH da
  arma em vez do menor entre ela e Cavalgar"* já está escrita no `AtiradorRules`
  como benefício **não** automatizado — quando o MB-10 sair, aquele item sai junto
  de graça.
- **O que falta:** a ficha **não guarda** que o personagem está montado. É um
  interruptor novo, no molde do `PainelEstadosTemporarios`.
- **⚠️ O gancho já existe:** o catálogo de armas corpo a corpo traz a observação
  *"[9] Dano maior quando usada montado"*, e ela hoje é só texto. Quando o MB-10
  sair, essa nota vira conta.

## MB-11 · Regras especiais por arma (MB p.405-407)

Truques sujos, capas, chicotes, garrote, manguais, picaretas, escudos, líquidos no
rosto — cerca de **oito blocos pequenos e independentes**.

- **Por que fica de fora agora:** cada um serve a **uma** arma específica, e o
  retorno por regra implementada é o menor da lista inteira.
- **A vantagem quando voltar:** é a frente mais **fácil de fatiar** — um bloco por
  lote, sem risco de um quebrar o outro.
- **⚠️ Metade já está meio pronta:** as observações `[n]` do catálogo de armas já
  trazem o texto dessas regras (*"[6] Aparar manguais sofre −4…"*, *"[8] Corda
  para estrangular; veja Garrote"*). Hoje o app **mostra** o texto; o que falta é
  transformá-lo em número.

## MB-12 · Perigos de ambiente (MB p.428-437)

Ácido, calor, frio, chamas, eletricidade, radiação, pressão, gravidade, atmosferas
perigosas, enjoo.

- **Por que fica de fora:** são muitas tabelas, e quase todas dependem do Mestre
  narrar a situação antes.
- **⚠️ E há um argumento melhor que "custa caro":** o app é a **ficha do jogador**.
  Estas são tabelas do **Mestre**, e ele já tem o Auditor para consultá-las no
  livro. Automatizar aqui seria mover trabalho para o lado errado da mesa.
- **A exceção que eu abriria:** se alguma delas virar rotina na sua campanha —
  frio numa campanha ártica, radiação numa pós-apocalíptica —, aí ela sozinha vale
  um lote.

---

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

# A ordem que eu recomendo — revista com os seus desenhos (03/08)

| Ordem | Lote | Situação |
|---|---|---|
| 1 | **MB-13** (explicar a Carga) | Meia hora, conserta uma desconfiança |
| 2 | **MB-2** (Recuo / fogo contínuo) | Dado na ficha desde o Lote 371 |
| 3 | **MB-8** (mau funcionamento) | ⬆️ **Subiu**: o Mauf sai do NT, que já existe |
| 4 | **MB-5** (prender o fôlego) | Regra pura sobre a HT |
| 5 | **MB-3** (Modificador de Tamanho) | Passo obrigatório do tiro que o app pula |
| 6 | 🔴 **MB-1** (Modificadores de Combate) | Maior ganho; traz o teto de 9 |
| 7 | **MB-4** (cobertura) | Mesmo diálogo do MB-1 |
| 8 | **MB-9** (queda) | Tabela pura, molde existe |
| 9 | 🔴 **MB-6** (botão "PF" — fadiga) | ⬆️ **Destravado**: o seu desenho dispensa o relógio de campanha |
| 10 | 🔴 **MB-7** (botão "PV" — ferimento por local) | O maior lote, e o de maior ganho |
| — | ⏸️ **MB-10, MB-11, MB-12** | Adiados por você — ver o lembrete |

⚠️ **MB-6 e MB-7 são irmãos** — os dois transformam um número do topo da aba
(**PF** e **PV**) num botão que abre um painel de estado. Fazer os dois seguidos
compartilha o desenho e o molde; fazer separados por meses faria o segundo
divergir do primeiro, que foi exatamente o que aconteceu com os diálogos de
configurar × editar vantagem.

# As perguntas — respondidas por você em 03/08

| Pergunta | Sua resposta | O que mudou |
|---|---|---|
| A campanha usa montaria? | *"Pensar num futuro"* | MB-10 vira lembrete, sai da fila |
| A ficha deve saber que horas são? | **Não precisa** — o jogador informa no painel do PF | 🔴 Destravou o MB-6 |
| Ferimento guardado por local? | **Sim**, e com a RD da armadura por local | 🔴 Destravou o MB-7, e ele ficou maior e melhor |

**Sobra uma decisão nova**, que o seu desenho do MB-7 criou:

> ❓ **Quando o jogador marca "estou vestindo" numa armadura, isso deve ficar
> salvo na ficha, ou é escolha de cada ferimento?**
>
> Salvo é mais cômodo (marca uma vez e esquece) e mais arriscado (a armadura que
> ele tirou para dormir continua dando RD). Por ferimento é chato e sempre certo.
> Meu palpite é **salvo na ficha, com a caixinha aparecendo em cada ferimento já
> marcada** — mas é sua chamada.

# ⚠️ Uma nota sobre método

Duas coisas que eu quero deixar escritas, porque este projeto já foi mordido pelas
duas:

**Confirmar o dado antes de prometer a regra.** O MB-8 parecia fácil até eu olhar
as chaves do catálogo e ver que **não existe campo `Mauf`**. Se eu tivesse
planejado sem conferir, o lote quebraria no meio.

**Regra que precisa de estado novo não é "só uma caixinha".** MB-6, MB-7 e MB-10
mexem no que a ficha **guarda**, não no que ela **calcula**. Isso é outro tipo de
trabalho — e é por isso que estão na Onda 2 e 3, mesmo tendo ganho alto.


---

# Onde o plano parou (10/08/2026)

**Onda 1 e 2 fechadas.** MB-1, MB-2, MB-4, MB-5, MB-8, MB-9, MB-13 e os dois
grandes — MB-6 e MB-7 — estão no repositório com gate verde.

O **MB-7 cresceu além do planejado**: virou uma silhueta tocável do corpo, com
zoom em três telas e o mapa medido na própria arte (lotes PV-1a a PV-1d). O que o
plano previa como uma lista de locais virou o desenho, e o teste no aparelho
aprovou.

**Continuam adiados a pedido do usuário:** MB-10 (montaria), MB-11 (regras
especiais por arma) e MB-12 (perigos de ambiente).

⚠️ **Pendências reais que sobraram:**
- **T-PF** — o painel de fadiga (MB-6) ainda não foi tocado no aparelho.
- **T-QD** e **T-FO** — queda e fôlego têm a regra pronta e **nenhuma tela**.
- **T-7B** — a correção do teto de membro mudou um número do **combate da Saga**
  e precisa ser vista lá dentro.
