# Plano de implementação — GURPS Poderes

> Leitura capítulo a capítulo do livro (`docs/livros/GURPS_Poderes.md`), anotando
> **o que dá para automatizar na ficha** e o que é conversa de Mestre.
>
> ⚠️ **Página do PDF = página impressa + 2.** Toda citação aqui usa a impressa.
>
> Legenda de veredito:
> **[FAZER]** regra fechada, com número, que a ficha consegue calcular ·
> **[MOSTRAR]** o app só precisa exibir/registrar, não calcular ·
> **[MESA]** decisão do Mestre, não vira código ·
> **[FEITO]** já está no app.

---

## Onde o app está hoje (POD-1 a POD-4)

- Catálogo de **47 poderes** com fonte, foco, descrição, página e as fontes
  válidas de cada um. **[FEITO]**
- **11 fontes genéricas** com o percentual do livro. **[FEITO]**
- Escolher a fonte preenche o modificador. **[FEITO]**
- Talento com campo, custo (5 ou 10/nível, 15 no Cósmico) e entrada em
  `pontosGastos`; aviso acima de 4 níveis. **[FEITO]**
- Teto de −80% no modificador total. **[FEITO]**

🔴 **O buraco central:** o app não liga o poder às suas **habilidades**. O
diálogo cria o rótulo e não diz quais vantagens da ficha pertencem a ele.

---

## Capítulo 1 — Criação de Poderes (p.6-37)

### A anatomia (p.7-8)

Cinco peças: **fonte**, **foco**, **habilidades**, **modificador de poder**,
**Talento**. A regra que amarra tudo:

> *"Uma vantagem precisa ter o respectivo modificador de poder para ser parte
> dele; não há exceções."* (p.8)

E a definição de "ter o poder" (p.34):

> *"Os portadores do Talento para um poder, **ou qualquer de suas habilidades**
> (ou seja, qualquer vantagem com seu modificador de poder) são considerados
> possuidores daquele poder."*

**[FEITO] · POD-5 — o poder mostra suas habilidades.** O diálogo do poder precisa
listar as vantagens/desvantagens da ficha que carregam o modificador dele, e
deixar ligar/desligar dali. Hoje a ligação só existe indo na vantagem. É a peça
que falta para o poder deixar de ser um rótulo solto.

### Habilidades Alternativas (p.11)

> *"Personagens com 'habilidades alternativas' pagarão o preço integral apenas
> para sua habilidade mais cara. Todas as outras terão **1/5 do custo**. O custo
> final de cada habilidade deve ser definido **após** calcular todas as
> ampliações e limitações (inclusive o modificador de poder), aplicar o divisor e
> **arredondar para cima**."*

Exemplo do livro: Voo [36] + Super Salto 2 [18] + Caminhar no Ar [18] →
36 + ⌈18/5⌉ + ⌈18/5⌉ = 36 + 4 + 4 = **44**.

**[FAZER] · POD-6 — grupo de habilidades alternativas.** Cálculo fechado, com
exemplo verificável no livro. Marcar N habilidades como um grupo e o app aplica
a conta. Economia real de pontos, e hoje o jogador faz na mão.

Inconvenientes que o app deve **[MOSTRAR]** junto — são **três**, não dois:

1. só uma funciona por vez; trocar exige manobra **Preparar** (trocar de um ataque
   para outro ataque é ação livre);
2. o que desativar, incapacitar, neutralizar ou drenar **uma** derruba o conjunto
   inteiro até ela se recuperar;
3. habilidade que não possa ser reativada antes de a duração acabar (Neutralizar
   com Furto de Poder, qualquer coisa com Longa Distância) **tranca todas** pela
   duração.

> ⚠️ **Achado na 2ª revisão:** eu tinha registrado só dois. O terceiro estava logo
> depois de onde a minha leitura parou.

### Avaliação do modificador de poder (p.20-25)

O modificador é a **soma de componentes**:

| Componente | Valor | Página |
|---|---|---|
| Contramedidas Mundanas | −10% | 20 |
| Antipoderes | −5% (fixo, não por antipoder) | 20-21 |
| Sem Contramedidas | +0% (padrão) | 21 |
| Poderes Cósmicos | +50% | 21 |
| Desvantagem exigida — **1.** o traço | o valor em pontos vira % (Voto de −10 pts = −10%) | 23 |
| Desvantagem exigida — **2.** velocidade com que o poder se esvai | +5% gradual · +0% rápido · −5% a mais se ele se voltar contra o usuário | 23 |
| Desvantagem exigida — **3.** ato para restaurar | +5% um dia · +0% uma semana · −5% um mês | 23 |
| Energias canalizadas (isolante esotérico) | −5% | 24 |
| Energias canalizadas (isolante mundano) | −10% | 24 |

E a orientação prática: *"Tente manter o valor entre −10% e −30%"* (p.25).

> ⚠️ **Conferido na revisão:** a linha "Desvantagem exigida" que eu tinha escrito
> como **um** valor são na verdade **três escolhas encadeadas** (p.23). Um montador
> que peça só o valor da desvantagem entregaria um número incompleto.

**[FAZER] · POD-7 — montador de modificador por componentes.** Diálogo que soma
os componentes e devolve o total, para quem cria poder personalizado. Hoje só dá
para digitar o número final.

### Os 11 modificadores prontos (p.26-30) — **[FEITO]**

### Inclusão e melhoria no jogo (p.33-34)

Regras de progressão que o livro **recomenda**, não impõe:
- para melhorar o Talento, ter usado alguma habilidade do poder na sessão anterior;
- para melhorar uma habilidade, ter usado **aquela** habilidade;
- para incluir habilidade nova, ter passado por um "gatilho" (p.36), e comprá-la
  com Destreinado / Inconstante / Incontrolável, removíveis depois.

**[MESA]** — depende do Mestre lembrar o que foi usado na sessão. Não vira trava.
**[MOSTRAR]** as três limitações sugeridas como atalho ao adicionar habilidade nova.

### Preços para Talentos (p.29) — **[FEITO]** (5 / 10 / 15 no Cósmico)

---

## Capítulo 2 — Criação de Habilidades (p.38-120)

O maior capítulo, e o mais **catalogável**: quase tudo aqui é conteúdo que falta
nos assets, não regra nova de cálculo.

### 🔴 O que falta de catálogo (medido contra os assets)

| Asset | No livro | No app | Falta |
|---|---|---|---|
| Ampliações e limitações (p.107-113) | 48 | 31 | **17** |
| Novas vantagens (p.90-97) | 6 | 1 | **5** |

> 🔴 **CORRIGIDO no POD-8: esta tabela está errada.** Dos 48 títulos, **16 são
> remissão** (*"Solavanco veja Efeito Incômodo"*) e não são modificador próprio.
> O buraco real era **1 modificador e 4 vantagens** — Características Variantes é
> seção, e Controle Divino é caixa lateral. Contar título não é contar conteúdo.

**Modificadores ausentes:** Subaquático, Sempre Ativa, Variável, Uso Limitado,
Ataque Surpresa, Normalmente Ativa, Efeito Seletivo, Fogo Instantâneo, Defesa
Ativa, Desvantagem Exigida, Difícil de Usar, Efeito do Dano Ausente, Magnético,
Exige Teste de Reação, Gatilho Incontrolável, Solavanco, Características
Variantes.

**Vantagens ausentes:** Controle (p.90), Criar (p.92), Controle Divino (p.92),
Estática (p.94), Ilusão (p.95). *(Neutralizar já existe.)*

**[FEITO, MAS INCOMPLETO] · POD-8 — completar os dois catálogos.**
⚠️ Entregou 4 vantagens e 1 modificador; **faltaram 8 modificadores** — ver POD-8b. É o mesmo formato do POD-1:
extrair do PDF, varredura como teste. Sem isso, metade dos poderes do livro não
tem como ser montada na ficha — as habilidades sugeridas citam justamente essas
vantagens e esses modificadores.

### Reservas de Energia (p.119)

> *"Compre Pontos de Fadiga pelo preço costumeiro de **3 pontos cada**, mas trate-os
> como uma nova vantagem, 'Reserva de Energia' (RE). Ela sempre está ligada a uma
> fonte de poder particular; por exemplo, 10 PF para poderes psíquicos será
> 'RE 10 (Psíquico) [30]'."*

Regras que a ficha consegue guardar sozinha:
- **só abastece habilidades da mesma fonte**;
- recarrega **1 ponto a cada 10 minutos**, independente de repouso;
- esgotar a RE **não** causa os efeitos de estar abaixo de 1/3 dos PF, e ter a RE
  cheia **não** protege contra eles;
- Ataque por Fadiga, falta de sono e esforço adicional comum **não** a gastam;
- se o poder não puder recorrer aos PF normais, **−5%** no modificador de poder.

#### 🔴 As cinco limitações da RE — achadas na 2ª revisão

A recarga de "1 ponto a cada 10 minutos" que eu tinha anotado como **a** regra é
só o **padrão**. O livro dá cinco limitações que mudam a recarga **e** o preço:

| Limitação | O que faz | Valor |
|---|---|---|
| **Carga Especial** | não recarrega com o tempo; só por RD com Absorção, Sanguessuga, Roubar Energia | **−70%** (−80% se perder 1 ponto/segundo) |
| **Carga Lenta** | recarrega devagar | **−20%** (1/hora) · **−60%** (1/dia) |
| **Poder Único** | só se o personagem tiver 2+ poderes da fonte; a RE serve a **um** deles | **−50%** |
| **Somente Habilidades** | paga só o PF básico; não serve para esforço adicional nem proezas | **−10%** |
| **Somente Proezas** | só esforço adicional e proezas; não paga o uso normal | **−10%** |

⚠️ Carga Especial e Carga Lenta são **incompatíveis** entre si; Somente
Habilidades e Somente Proezas também.

**[FAZER] · POD-9 — Reserva de Energia.** É uma segunda barra de PF com regra de
recarga própria, trava de fonte **e as cinco limitações acima** — que são o que
faz a RE valer a pena modelar, porque mudam quando ela volta. O −5% vira
componente do montador (POD-7).

### Modificadores por Multiplicação (p.102) — regra opcional

> *"Some e aplique as ampliações. Em seguida, totalize as limitações (reduzindo
> qualquer total maior que −80% para −80%) e aplique-as ao resultado."*

Exemplo do livro: +20% e −50% dão **70%** no modelo aditivo (o do app hoje) e
**60%** no multiplicativo.

**[MESA]** com um pedaço **[FAZER]**: é escolha do Mestre, mas se ele escolher, a
conta é fechada. Vira uma chave na ficha, não um cálculo novo espalhado.
⚠️ *"não se recomenda usar ambos"* — tem de ser uma chave só, valendo para a ficha
inteira.

### Habilidades Parcialmente Limitadas (p.70)

> *"RD 5 (10 contra Fogo)" seria RD 5 [25] + RD 5 (Limitada, Fogo, −40%) [15].*

**[MOSTRAR]** — o app já permite comprar as duas entradas separadas; o que falta é
alguém saber que é assim que se faz. Nota no diálogo, não cálculo novo.

---

## Capítulo 3 — Poderes e Exemplos de Habilidades (p.121-151)

### A lista de habilidades de cada poder — o achado do capítulo

Cada verbete traz **"Habilidades de \<Poder\>"**: a lista das vantagens que o
livro sugere para aquele poder, já com os modificadores recomendados. Exemplo
(Água): *"Anfíbio; Caminhar no Ar, com Específico, Vapor (−40%); Caminhar sobre
Líquidos; Controle (Água); Criar (Água); …"* — **19 itens**.

Teste de extração: **44 dos 47** poderes têm a lista, com média de **12,6
habilidades** cada. ⚠️ E a contaminação de verbete vizinho reapareceu (Alteração
de Probabilidades puxou a lista da Antimagia) — a extração precisa da mesma trava
de duplicata que salvou o POD-1.

**[FEITO] · POD-10 — habilidades sugeridas no catálogo.** Novo campo por poder.
Com ele, escolher "Telepatia" passa a **oferecer** as vantagens que fazem parte
dela, em vez de o jogador ter de saber de cor. É o que fecha o buraco do POD-5.

### Exemplos de Habilidades (p.136-151)

Receitas prontas de habilidade (ataques elementais, controle de armas, defesas,
movimento, mentais, transformações), com tabelas de custo.
**[MOSTRAR]** — é conteúdo de consulta, e o app já tem o Índice de Regras para
esse papel. Não vira cálculo.

---

## Capítulo 4 — Poderes em Ação (p.152-178)

O capítulo das regras de **mesa** — é aqui que a aba Rolagem entra.

### 🔴 O teste de ativação (p.156) — a regra mais valiosa do livro para o app

> *"Faça um teste de **HT** se o modificador de poder for Biológico, Elemental,
> Natureza ou Super, ou de **Vontade** se for Chi, Divino, Espiritual, Mágico,
> Moral, ou Psíquico."*

> 🔴 **CORRIGIDO no POD-11: esta leitura está errada.** A tabela de HT/Vontade
> não é o teste de ativação — é o de **incapacitação** depois de falha crítica
> em esforço adicional ou proeza (p.156), e o Cósmico é **imune**. Não existe
> "atributo de ativação do poder": o que se rola depende da habilidade.

**[FEITO] · POD-11 — a regra da fonte e o bônus do Talento.** O app **já sabe** a
fonte do poder (POD-2) e já sabe o nível do Talento (POD-3). Com esta tabela ele
decide sozinho qual atributo rolar e quanto somar. É um botão que se resolve
inteiro com o que já está na ficha.

⚠️ Cobre as 10 fontes; o **Cósmico** não aparece na lista do livro — tratar como
caso à parte, não chutar.

### O Talento no teste (p.158)

Soma nos testes para **usar** a habilidade. **Não** soma no dano, no teste de
resistência do alvo, nem em reação. Casos por categoria: perícias furtivas,
suporte à vida, movimento, transformações, alterar a realidade.
**[FAZER]** junto com o POD-11 — é o bônus do botão.

### Custo em PF do uso (p.159-160)

- uso intensivo (testes a cada 1-2 s): **1 PF por minuto**;
- uso prolongado de baixa intensidade: **1 PF por hora**;
- habilidade contínua: teste **uma vez por minuto** e **1 PF por teste**.

**[FAZER]** — encaixa no controle de PF que a ficha já tem, e é o consumidor
natural da Reserva de Energia (POD-9).

### Esforço Adicional (p.160-161)

> 🔴 **CORRIGIDO na revisão: eu tinha lido a regra errada.** Escrevi *"1 PF =
> +15% de efeito"*. Isso não é a regra base — é o exemplo de uma **variante
> cinematográfica opcional** ("Esforço Adicional Divino", p.161), que multiplica
> pelos PF gastos e **ignora o teto**.

A regra de verdade **não troca PF por efeito** — troca **penalidade em Vontade**:

> *"Para usar um esforço adicional, faça um **teste de Vontade com redutor de −1
> por aumento de 5% no efeito**, ou fração. (…) O bônus máximo para o efeito é de
> **100%**, com redutor **−20** para Vontade."* (p.160)

Modificador: **+5** nas situações de *Apenas em Emergências* (p.100).

⚠️ E ela só vale para habilidade **ativa que exija teste para ativar** ou Disputa
Rápida. Habilidade passiva não usa; habilidade que pede **teste de ataque** também
não — para ataque o equivalente é o Ataque Total (Determinado).

**[FAZER]** — a conta é fechada e verificável (Atribulação 9 sobre 8 = +12,5% →
−3). Mas é **outra** conta da que eu tinha planejado.

### Ampliações Temporárias (p.172) e Uso Predefinido (p.173)

Concentrar + teste de Vontade (mental) ou Preparar + teste de HT (física);
**−1 por +10%** de ampliação adicionada; custa **2 PF** (temporária) ou **3 PF**
(predefinido), além do gasto voluntário.
**[FAZER]** — mas é regra avançada; fica para depois do POD-11.

### Combinação de Poderes (p.171)

Cada participante com a **mesma** habilidade e poder do líder dá **+50%**; com
habilidade semelhante de outro poder da mesma fonte, **+25%**.
**[MESA]** para a ficha (é cena de grupo), **[FAZER]** só se algum dia entrar no
combate da Saga. Fora do escopo Rolagem-primeiro.

### Perícias do Poder (p.162) — regra opcional

Uma perícia por habilidade, IQ/Difícil, default **IQ−6**.
**[MESA]** com chave: só existe se o Mestre ligar.

### Regras por fonte (p.176-178)

Poderes divinos pedem teste de reação da divindade; espirituais têm Ira dos
Espíritos (Volúvel); elementais sofrem **−1 a −9** onde o elemento é suprimido.
**[MOSTRAR]** — o app registra a fonte e pode lembrar a regra na hora de rolar.

---

## Capítulo 5 — Jogos de Poder (p.179-202)

Guia de Mestre: origens por gênero, nível de poder, restrições, campanhas.
Varredura por regra numérica achou **7 trechos em 24 páginas** — confirma que é
capítulo de conversa, não de conta. **[MESA]** quase inteiro.

Duas exceções que a ficha consegue avisar:

- **Teto de pontos em poderes (p.184):** *"a diretriz de não mais que **50%** dos
  níveis de pontos base se mantém razoavelmente bem para PdJs na faixa de 100 a
  300 pontos"*. **[MOSTRAR]** — aviso do mesmo tipo do teto de 4 níveis de
  Talento: mostra e deixa passar.
- **Talento que também melhora perícia (p.188):** *"o custo do Talento deve ser
  aumentado para **10 ou 15** pontos/nível"*. **[MOSTRAR]** no campo de custo.

---

# O plano revisado — ordem de implementação

Revisei os cinco capítulos e ordenei por **quanto cada leva destrava**, não pela
ordem do livro. O critério: primeiro o que faz o poder deixar de ser um rótulo
solto; depois o que a aba Rolagem usa; por último o avançado.

| Leva | O quê | Por que nesta posição |
|---|---|---|
| **POD-5** | O poder lista e gerencia suas **habilidades** | 🔴 É o buraco central. Sem isto o poder não é um poder, é um rótulo. Tudo abaixo depende dele. |
| **POD-10** | Habilidades **sugeridas** de cada poder, do livro | Enche o POD-5 de conteúdo. Sem ele o jogador tem de saber de cor. |
| **POD-8** | Completar catálogos: **17** modificadores + **5** vantagens | As habilidades sugeridas citam justamente o que falta. |
| **POD-11** | **Botão de ativar** o poder na Rolagem (HT ou Von pela fonte, + Talento) | Primeira coisa que aparece na aba Rolagem. Usa só o que já está na ficha. |
| **POD-6** | **Habilidades alternativas** (1/5 do custo, arredondando para cima) | Conta fechada, exemplo verificável, economia real de pontos. |
| **POD-9** | **Reserva de Energia** (3 pts/PF, recarga 1 por 10 min) | Segunda barra de PF. Depende do POD-5 para saber a fonte. |
| **POD-7** | **Montador** de modificador por componentes | Só serve para poder personalizado; a maioria usa os 11 prontos. |
| **POD-12** | Custo em PF do uso, esforço adicional (1 PF = +15%) | Regra de mesa contínua; depende do POD-11 existir. |
| **POD-13** | Ampliações temporárias, uso predefinido, multiplicação | Avançado. Último de propósito. |

## O que eu decidi NÃO fazer, e por quê

- **Combinação de Poderes (p.171)** — é cena de grupo, aparece no combate da
  Saga, não na ficha. Fora do escopo Rolagem-primeiro.
- **Exemplos de Habilidades (p.136-151)** — receitas de consulta; o app já tem o
  Índice de Regras para esse papel.
- **Progressão entre sessões (p.33-34)** — depende do Mestre lembrar o que foi
  usado. Não vira trava.
- **Perícias do Poder (p.162)** e **Modificadores por Multiplicação (p.102)** —
  regras opcionais que mudam a ficha inteira. Só com chave, e só se pedidas.

## ⚠️ Duas armadilhas que a extração já provou existir

1. **Verbete vizinho colando em silêncio.** Aconteceu com foco (POD-1) e com
   descrição (POD-4), e o teste de extração das habilidades já mostrou o mesmo
   (Alteração de Probabilidades puxou a lista da Antimagia). **Toda extração nova
   precisa da trava de duplicata** antes de virar asset.
2. **Escape de shell comendo regex.** Um `` virou BACKSPACE literal e deixou um
   teste cego. **Texto Kotlin e regex vão pela ferramenta de edição**, nunca por
   heredoc de shell.


---

# Revisão do plano inteiro contra o livro (após POD-11)

Duas correções em dois lotes seguidos (POD-8 e POD-11) me fizeram reler cada
item ainda não implementado **com a página aberta**, antes de codar.

## O que estava errado

| Item | Eu tinha escrito | O livro diz | Achado em |
|---|---|---|---|
| **POD-8** | faltam 17 modificadores e 5 vantagens | 1 e 4 — dos 48 títulos, **16 são remissão** | ao codar |
| **POD-11** | botão de ativar: HT ou Vontade pela fonte | é o teste de **incapacitação** após falha crítica; Cósmico **imune** | ao codar |
| **POD-12** | esforço adicional: 1 PF = +15% | **−1 na Vontade por 5%** de efeito, teto +100% a −20; o 15% é variante opcional | nesta revisão |
| **POD-7** | desvantagem exigida = um valor | **três** escolhas encadeadas (traço, velocidade que se esvai, ato para restaurar) | nesta revisão |

⚠️ O padrão dos quatro é o mesmo: **eu li o trecho e não a seção**. Uma tabela
dentro de *Poderes Incapacitados*, um exemplo dentro de uma variante opcional, um
título de remissão, uma linha de uma lista de três. Cada um lido sozinho parece
uma regra fechada.

## O que resistiu à conferência

| Item | Número | Página | Verificação |
|---|---|---|---|
| **POD-6** Habilidades Alternativas | 1/5 do custo, arredondando para cima | 11 | exemplo do livro confere: 36 + 4 + 4 = **44** |
| **POD-9** Reserva de Energia | 3 pts/PF; recarrega 1 a cada 10 min; −5% se não puder usar PF normal | 119 | citação literal |
| **POD-12** custo em PF do uso | **1 PF/minuto** intensivo · **1 PF/hora** prolongado | 159 | citação literal |
| **POD-13** ampliações temporárias | −1 por +10% de ampliação; custa 2 PF | 172 | citação literal |
| **POD-13** uso predefinido | custa 3 PF | 173 | citação literal |
| Perícias do Poder (opcional) | IQ/Difícil, predefinido **IQ−6** | 162 | citação literal |
| Modificadores por Multiplicação | +20% e −50% dão 70% aditivo, **60%** multiplicativo | 102 | exemplo do livro |

## A ordem, revisada

Não mudou de ordem — mudou de **conteúdo**. POD-12 deixou de ser "PF por efeito"
e virou "penalidade de Vontade por efeito", e POD-7 ficou maior do que parecia.

1. **POD-6** — Habilidades Alternativas *(o mais fechado que sobrou)*
2. **POD-9** — Reserva de Energia
3. **POD-7** — montador de modificador *(agora com as três escolhas da desvantagem)*
4. **POD-12** — esforço adicional e custo em PF do uso
5. **POD-13** — ampliações temporárias e uso predefinido

## A regra que passei a seguir

**Ler a seção inteira antes de escrever a linha do plano**, e guardar no plano a
*citação*, não o meu resumo dela. As quatro correções vieram todas de resumo meu
que soava fechado — e os testes não pegam isso, porque eles guardam o que eu
escrevi.


---

# 2ª revisão — a pergunta do usuário: *"tem certeza que leu direito desta vez?"*

**Não.** A revisão anterior conferiu os **números**; não conferiu a **extensão**
das seções — e é a extensão que me pegou nas quatro vezes anteriores.

Medindo os dois itens que eu tinha marcado como mais confiáveis:

| Seção | Tamanho | Eu tinha lido | O que faltava |
|---|---|---|---|
| Habilidades Alternativas (p.11) | ~5.700 chars | 2.600 | o **3º inconveniente** |
| Reservas de Energia (p.119) | ~5.900 chars | 2.200 | as **5 limitações** da RE |

Nos dois casos a **conta principal continuou certa** (1/5 arredondando para cima;
3 pontos por PF). O que faltava era o entorno — e no POD-9 o entorno é metade do
lote, porque as limitações mudam a regra de recarga que eu tinha anotado como a
única.

⚠️ Uma medição grosseira sugere que **Avaliação de Modificadores** (p.20),
**Esforço Adicional** (p.160), **Ampliações Temporárias** (p.172) e
**Multiplicação** (p.102) também foram lidos parcialmente. A medida é grosseira
(conta até o fim da página seguinte, não até o fim da seção), então ela indica
onde olhar, não um veredito.

## O que passa a valer antes de cada lote

Ler a seção **até o título seguinte**, não até onde a resposta aparece. Achar o
número não é o fim da leitura — nas quatro vezes, o número estava certo e o
problema era o que vinha depois dele.


---

# 3ª revisão — leitura COMPLETA das quatro seções restantes

Feita com um extrator que corta **no título seguinte**, não num número de
caracteres. O corte vem do documento, não da minha paciência.

## 🔴 O achado grave: o POD-8 ficou com 8 modificadores de fora

No POD-8 eu concluí que, dos 48 títulos, **16 eram só remissão** e não existiam
como modificador. Errado: eles são remissões **para onde o modificador está
definido dentro do próprio livro**. Segui as referências:

| Modificador | Valor | Onde está definido |
|---|---|---|
| Ataque Surpresa | **+150%** | dentro de *De Cima* (p.102) |
| Solavanco | **+30%** | dentro de *Efeito Incômodo* (p.103) |
| Fogo Instantâneo | +10% | dentro de *Fogo Contínuo* (p.104) |
| Defesa Ativa | −40% | p.110 |
| Efeito do Dano Ausente | −20% | dentro de *Modificadores de Dano* (p.105) |
| Difícil de Usar | −5% por −3, até −12 | dentro de *Destreinado* (p.102) |
| Exige Teste de Reação | −5% | dentro de *Volúvel* (p.112) |
| Gatilho Incontrolável | −5% | dentro de *Incontrolável* (p.104) |

Os outros 8 apontam para o **Módulo Básico** (Subaquático, Sempre Ativa,
Variável, Uso Limitado, Efeito Seletivo, Desvantagem Exigida, Magnético) ou não
são modificador (Características Variantes).

⚠️ **Pior que o buraco:** o teste `as remissoes do livro NAO viraram modificador`
**proíbe** esses 8 de existirem. Ele transformou a minha conclusão errada em
trava. Quem tentasse acrescentá-los depois seria reprovado pelo gate.

**[FAZER] · POD-8b — os 8 modificadores que ficaram de fora**, e desfazer a trava
que os proíbe.

## O que a leitura completa acrescentou em cada seção

### Avaliação de Modificadores (p.20-26) — a tabela estava incompleta

Componentes que faltavam na minha tabela:

| Componente | Valor |
|---|---|
| Antipoderes — contramedidas **específicas** (Estática/Neutralizar, perícias) | −5% |
| Antipoderes — **as duas** situações juntas | **−10%** |
| Contramedidas **tecnológicas** (distinta das mundanas) | −5% |
| Energias canalizadas — energia que permeia tudo e não pode ser filtrada | **+0%** |
| Poder fica inútil quando a **Reserva de Energia** esgota | −5% |
| Efeitos Incômodos | −5% |
| Penitência para reparar (até um mês de jejum/aventura) | −5% |

⚠️ Eu tinha registrado *"Antipoderes −5% fixo"*. São **dois níveis**.

### Esforço Adicional (p.160) — completo, e menor do que eu temia

A seção tem 2.134 chars; a minha leitura anterior cobria quase tudo. A regra
segue como corrigido na 2ª revisão: **−1 na Vontade por 5%** de efeito, teto de
**+100%** a **−20**, **+5** nas situações de *Apenas em Emergências*.

### Ampliações Temporárias (p.172) — números certos, entorno faltando

Confirmados: **−1 por +10%** de ampliação, custa **2 PF**. O que faltava:

- Concentrar + **Vontade** (mental) ou Preparar + **HT** (física);
- pode usar **perícia** conforme a fonte (Meditação/chi, Magia Ritualística/
  espiritual, Perícia Abrangente/psíquico, Ritual Religioso/divino,
  Taumatologia/mágico);
- **cada PF gasto voluntariamente cancela −1** da penalidade — nunca vira bônus;
- **o Talento soma** neste teste;
- sucesso decisivo: **sem custo de PF**;
- falha crítica: indisponível por **1d segundos** *e* checa incapacitação —
  que atinge o **poder inteiro** (liga com o POD-11).

### Modificadores por Multiplicação (p.102) — confirmado, sem novidade

Aditivo (padrão do app): +20% e −50% → **70%**. Multiplicativo: → **60%**.
*"Não se recomenda usar ambos."*

## Placar honesto desta leitura

| | |
|---|---|
| Seções lidas até o título seguinte | 4 de 4 |
| Erros achados | **3** — os 8 modificadores do POD-8, a tabela de componentes incompleta, o entorno das Ampliações Temporárias |
| Números que resistiram | esforço adicional, ampliações temporárias, multiplicação |

O padrão continua o mesmo dos anteriores: **os números que eu anotei estavam
certos; o que faltava era o que vinha ao redor deles.**


---

# Status, conferido no arquivo (não de memória)

| Lote | O quê | Situação |
|---|---|---|
| POD-1/2/3 | catálogo de 47 poderes, fonte→modificador, Talento | ✅ feito |
| POD-4 | focos inventados, descrição vazando, teste cego | ✅ feito |
| POD-5 | o poder lista e gerencia suas habilidades | ✅ feito |
| POD-10 | as 567 habilidades sugeridas do livro | ✅ feito |
| POD-8 | 4 vantagens + 1 modificador | ⚠️ **incompleto** |
| POD-11 | incapacitação pela fonte + bônus do Talento | ✅ feito |
| **POD-8b** | os 8 modificadores + desfeita a trava | ✅ feito |
| **POD-6** | Habilidades Alternativas — regra pura pronta; **falta a tela** | ⚠️ parcial |
| **POD-9** | Reserva de Energia + as 5 limitações | ⚠️ regra pronta; **falta a tela** |
| **POD-7** | montador de modificador | ⚠️ regra pronta; **falta a tela** |
| **POD-12** | esforço adicional + custo em PF | ⚠️ regra pronta; **falta a tela** |
| **POD-13** | ampliações temporárias, uso predefinido, multiplicação | ⚠️ regra pronta; **falta a tela** |

⚠️ Os marcadores `[FAZER]` deste arquivo ficaram desatualizados em POD-5, POD-8 e
POD-10: eles foram implementados e o plano continuou dizendo que faltavam. Quem
lesse só o plano concluiria errado. Corrigido acima — e é mais um caso do mesmo
problema da sessão: **o documento e o fato divergindo em silêncio.**


---

# Onde o PILAR PODERES parou

**Todas as regras do livro que dão para automatizar estão implementadas e
testadas.** O que falta agora não é leitura de livro — é **tela**:

| Regra | Estado |
|---|---|
| POD-6 Habilidades Alternativas | falta marcar o grupo na ficha |
| POD-7 Montador de modificador | falta o diálogo que soma os componentes |
| POD-9 Reserva de Energia | falta a segunda barra de PF |
| POD-12 Esforço adicional | falta o botão na Rolagem |
| POD-13 Ampliações temporárias | falta o diálogo da proeza |

⚠️ Um lote de **tela** deste tamanho para para teste no aparelho, e mexe na aba
Rolagem — que é escopo separado. Vale decidir quais destas cinco valem tela antes
de fazer as cinco.


---

# As telas (11.0-TELAS)

| Regra | Tela |
|---|---|
| POD-6 Habilidades Alternativas | ✅ caixa de marcar + economia + os 3 inconvenientes |
| POD-7 Montador de modificador | ✅ diálogo por componentes, escrevendo no campo existente |
| POD-9 Reserva de Energia | ✅ campo, limitações, custo e aviso de conflito |
| POD-12 Esforço adicional | ✅ botão Poderes na aba Rolagem |
| POD-13 Ampliações temporárias | ✅ no mesmo diálogo |

⚠️ POD-12 e POD-13 não pertencem ao diálogo de poderes: acontecem **no meio de
uma rolagem**. Pô-las ali seria configurá-las onde ninguém as usa. O passo
seguinte é decidir onde elas entram na aba Rolagem — conversa antes de código.


---

# PILAR PODERES — fechado

Todas as regras de GURPS Poderes que dão para automatizar estão **implementadas,
testadas e com tela**:

| Lote | Regra | Onde aparece |
|---|---|---|
| POD-1/2/3 | catálogo, fonte→modificador, Talento | diálogo de poderes |
| POD-5 | as habilidades do poder | diálogo de poderes |
| POD-6 | habilidades alternativas (1/5) | diálogo de poderes |
| POD-7 | montador de modificador | diálogo de poderes |
| POD-8/8b | 4 vantagens + 9 modificadores | catálogos |
| POD-9 | Reserva de Energia | diálogo de poderes |
| POD-10 | 567 habilidades sugeridas | catálogo |
| POD-11 | incapacitação pela fonte, bônus do Talento | diálogo + Rolagem |
| POD-12/13 | esforço adicional, ampliações temporárias | **botão Poderes na Rolagem** |

## O que ficou de fora, e por quê

- **Combinação de Poderes (p.171)** — cena de grupo; vive no combate da Saga.
- **Exemplos de Habilidades (p.136-151)** — receitas de consulta; o Índice de
  Regras já faz esse papel.
- **Progressão entre sessões (p.33-34)** — depende do Mestre lembrar o que foi
  usado.
- **Perícias do Poder (p.162)** e **Multiplicação (p.102)** — opcionais que mudam
  a ficha inteira. A regra da multiplicação **está pronta**; falta só a chave.

## O placar honesto deste pilar

**Cinco erros de leitura meus**, todos do mesmo formato — *li o trecho, não a
seção*:

| Onde | O que eu escrevi | O que o livro diz |
|---|---|---|
| POD-8 | faltam 17 modificadores | 16 títulos eram remissão… |
| POD-8b | …e não eram modificador | 8 **eram**, definidos sob outro título |
| POD-11 | botão de ativar: HT/Vontade pela fonte | é o teste de **incapacitação** |
| POD-12 | 1 PF = +15% de efeito | **−1 na Vontade por 5%** |
| POD-7 | desvantagem exigida = um valor | **três** escolhas encadeadas |

E dois defeitos que os testes acharam e eu não teria achado lendo: o `\b` que
virou **backspace** (deixando dois testes cegos) e as contas com `Double` que
erravam **por um ponto**.

⚠️ A regra que sobrou de tudo isso: **guardar a citação, não o meu resumo dela**,
e ler a seção **até o título seguinte**.
