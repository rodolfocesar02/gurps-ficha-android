# Possíveis Automações — Desvantagens (MB p.120 a p.167)

> **O que é este arquivo.** O irmão do `Possiveis_Automações.md`, no mesmo
> formato e com o mesmo critério: leitura do livro **página a página**, e para
> cada desvantagem o nome, o custo, o trecho que **interessa para automatizar** e,
> entre `<< >>`, a proposta de como ficaria no app.
>
> **Por que ele existe, se já há um `Automações_Desvantagens.md`.** Aquele foi uma
> varredura **por palavra-chave**, e o próprio arquivo admite: *"foram chutes de
> varredura, não leitura"*. Ele classificou 221 desvantagens em oito categorias e
> declarou **16**. Este aqui é a leitura de verdade — e já na terceira página
> apareceu coisa que a varredura não pegou.
>
> **Regra que vale para tudo aqui:** o alvo é a **aba Rolagem** (jogo no
> Discord). O que só serviria para o combate da Saga está marcado e **não deve
> ser feito**.
>
> **Escala:** as desvantagens ocupam **48 páginas** (p.120-167), contra 11 das
> vantagens. Este arquivo é escrito em partes, na ordem do livro.

---

## O que já está automatizado (não repetir)

**16 desvantagens** já têm `efeitos` declarados: `acima_do_peso`, `amigavel`,
`aparencia`, `assustar_animais`, `caracteristicas_distintas`, `daltonismo`,
`destruidor_da_vida`, `estigma_social`, `fantasias`, `gordo`,
`habitos_detestaveis`, `magro`, `muito_gordo`, `paranoia`, `senso_do_dever`,
`veracidade`. Mais as **30 de autocontrole** (Lote D-1) e as de perda de sentido,
que o `SentidoRules` já trata.

---

# Parte 1 — p.124 a p.131

## ⭐ Atrapalhado(pode fazer)
**−5 ou −10 pontos** · p.124

> O personagem sofre uma penalidade em qualquer teste baseado em DX para realizar
> **trabalhos delicados**, como as perícias listadas em **Destreza Manual
> Elevada** (pág. 53) e também com a perícia **Sacar Rápido**.
>
> Por −5 pontos, a penalidade é de **−3**; por −10 pontos, de **−6**. Esta
> desvantagem **não** se aplica a tarefas baseadas em IQ, tarefas em grande-escala
> baseadas em DX ou jogadas de dados relacionadas com o combate, a não ser Sacar
> Rápido.

<<**Esta é a de melhor relação custo-benefício de todas as que li**, porque o
trabalho já está feito: ela é o **espelho negativo da Destreza Manual Elevada**,
que já está declarada no catálogo com a lista de perícias certinha. É copiar a
lista e trocar o sinal.

Usa `porOpcao` (o custo escolhido define −3 ou −6), que já existe desde o Lote
OPCAO-1.

⚠️ E o livro delimita com uma precisão rara: **não vale em IQ, não vale em
combate** — exceto Sacar Rápido. Sem essa ressalva no código, ela roubaria NH de
todas as perícias de combate do personagem.

O −1 por nível em Influência/reação "em locais onde é importante ter boas
maneiras" é **condicional** — vira caixinha.>>

## ⭐ Baixa Autoestima(pode fazer)
**−10 pontos** · p.125

> Ele sofre uma penalidade de **−3 em todos os testes de habilidade** sempre que
> acreditar que não tem qualquer chance de obter sucesso ou que outras pessoas
> esperam que ele fracasse (a critério do Mestre).

<<**Caixinha do curinga `*`**, exatamente como Venturoso e Versátil — só que
negativa. O mecanismo foi criado no Lote TAL-1 justamente para isto: o livro dá
uma **situação**, não uma lista, e quem decide se ela vale é o Mestre.

Sai de graça agora que o curinga existe. Uma linha de JSON.>>

## Barulhento(pode fazer)
**−2 pontos/nível** · p.125

> Cada nível concede um bônus de **+2 nos testes de Sentidos para ouvir o
> personagem** e impõe uma penalidade de **−2 nos testes de Furtividade** dele.
> Em algumas circunstâncias (ex.: numa ópera), cada nível também pode impor uma
> penalidade de **−1 nos testes de reação**.

<<Duas linhas: `Furtividade −2 porNivel` (direto) e `reacao −1 porNivel`
**condicional** ("em lugares onde o barulho incomoda").

O +2 para os **outros** ouvirem não tem onde entrar — a ficha é do personagem,
não de quem procura por ele.>>

## Briguento(pode fazer)
**−10 pontos\*** · p.126

> Como ninguém gosta de um fanfarrão, seus testes de reação sofrem uma penalidade
> de **−2**.

<<Reação **−2 fixa**, não condicional: o livro não põe "quando". O autocontrole
já está feito; falta só o −2.>>

## Características Sobrenaturais(pode fazer)
**−5 a −10 pontos** · p.126

> **Sem Reflexo / Sem Sombra / Palidez:** a reação de quem perceber sofre **−2**;
> os testes para descobrir seu segredo recebem **+2**. −10 pontos.
> **Sem Calor Corporal:** reação **−1** de quem tocar nele; **+1** nos testes para
> descobrir. −5 pontos.

<<Reação **condicional** — o livro é explícito que só vale "quando notadas". Cada
variante com o seu texto ("de quem perceber que você não tem reflexo").

O bônus nos testes de terceiros para descobrir o segredo fica de fora pelo mesmo
motivo do Barulhento: quem rola é o outro.>>

## ⭐ Cegueira(pode fazer)
**−50 pontos** · p.127 · *parcialmente automatizada*

> Todas as **perícias de combate** de um personagem cego sofrem uma penalidade de
> **−6**. Ele (…) **não é capaz de dirigir um golpe contra um ponto de impacto
> específico**. (…) Seja qual for o caso, o personagem **não sofre nenhuma outra
> penalidade por atuar no escuro**.

<<O `SentidoRules` já bloqueia os testes de Visão. Faltam **três coisas**, e uma
delas é um erro que o app comete hoje:

1. **−6 em todas as perícias de combate.** Declarável com a lista
   `PERICIAS_COMBATE_CORPO_A_CORPO` + as de distância, que o Lote MIRA-2 acabou de
   separar.
2. 🔴 **A luz da cena não deveria penalizá-lo.** O Lote LUZ-1 aplica a escuridão
   a todo mundo; o livro diz que o cego **já pagou** essa conta e não sofre mais
   nada por escuro. Hoje o app soma as duas penalidades — número errado.
3. **O diálogo de Mira deveria avisar** que ele não pode mirar em ponto de
   impacto. Não sumir com a lista (a informação é útil), mas dizer.>>

## ⭐ Cegueira Noturna(pode fazer)
**−10 pontos** · p.127

> Se a penalidade sobre a visão ou o combate devido à má iluminação estiver entre
> **−1 e −4** para a maioria das pessoas, então a dele será o **dobro ou −3, o que
> for pior**. Se a penalidade for de **−5 ou pior**, então ele deve agir como se
> fosse **completamente cego**.

<<**Encaixa direto no seletor de Luz da Cena** (Lote LUZ-1) — ela é o espelho
exato da Visão Noturna, e o painel já sabe explicar a conta.

⚠️ Duas armadilhas na mesma frase:
- **"o dobro ou −3, o que for pior"**: com luz −1, o dobro é −2, mas o −3 é pior
  → vale **−3**. Só a partir de −2 o dobro passa a mandar.
- **De −5 em diante ele é cego**, ou seja **−10**, e não −10 pelo dobro. O salto
  é abrupto de propósito.

O livro ainda diz que ela é **mutuamente excludente** com Visão Noturna e Visão
no Escuro — vai para o `IncompatibilidadeDeTracos`, que já existe.>>

## Circunspecção(pode fazer)
**−10 pontos** · p.127

> A reação das outras pessoas sofre uma penalidade de **−2** em qualquer situação
> onde a desvantagem fique evidente.

<<Reação −2 **condicional** ("quando a falta de humor aparecer"). O "em qualquer
situação onde fique evidente" é literalmente a definição de caixinha.>>

## Corcunda(pode fazer)
**−10 pontos** · p.130

> As roupas e armaduras comuns não servem bem nele, impondo uma penalidade de
> **−1 sobre a DX**. (…) A maioria das pessoas reage a ele com uma penalidade de
> **−1**. (…) Seu aspecto físico também é distinto, o que resulta em uma
> penalidade de **−3 nas perícias Disfarce e Perseguição**.

<<Três efeitos, todos declaráveis: **−1 DX** (tipo `atributo`), **−1 reação** e
**−3** em Disfarce/NT e Perseguição.

⚠️ O −1 de DX é **evitável**: o livro diz que basta pagar **10% a mais** por
equipamento sob medida. Isso é decisão de ficha, não de rolagem — sugiro declarar
o −1 e explicar a saída no texto, em vez de tentar rastrear o equipamento.<< <ressalva, quando o personagem tiver colocar aarmadura, um nota em vermelho no top do dialogo dos equipamentos, dizendo que precisa pagar 10% a mais pelo equipament ouredutor de-1 na DX .>>

## Credulidade(pode fazer)
**−10 pontos\*** · p.130

> Além disso, um personagem crédulo também sofre uma penalidade de **−3 em
> qualquer teste de Comércio** ou em qualquer situação em que sua credulidade
> possa ser explorada. Um crédulo **nunca pode aprender** a perícia Detecção de
> Mentiras.

<<O −3 em Comércio é direto. O "ou em qualquer situação em que sua credulidade
possa ser explorada" é o curinga `*` **condicional** — dois efeitos na mesma
desvantagem, um fixo e um oferecido.

A proibição de aprender Detecção de Mentiras é caso para o
`PreRequisitoChecker`, não para `efeitos`.>>

## Deficiência Física(pode fazer)
**−10 a −30 pontos** · p.131

> **Perna Incapacitada:** **−3** em todos os testes de habilidade que exigem uso
> das pernas, o que inclui **todas as perícias com armas de combate corpo a corpo
> e todas as perícias de combate desarmado** (as perícias com armas de projétil
> **não** são prejudicadas). O Deslocamento Básico deve ser reduzido à **metade da
> Velocidade Básica**. −10 pontos.
>
> **Pernas Faltando:** **−6** (…). Deslocamento Básico reduzido a **2**. −20 pontos.
>
> **Nenhuma Perna / Paraplégico:** **−6** (…). Deslocamento Básico **0**. −30 pontos.

<<Duas metades, e as duas têm onde encaixar:

**A penalidade de perícia** é `porOpcao` com a lista de combate corpo a corpo —
e a exclusão das armas de projétil é exatamente a divisão que o Lote MIRA-2 criou
em `PERICIAS_COMBATE_CORPO_A_CORPO` × `PERICIAS_COMBATE_DISTANCIA`. Sem essa
separação, esta desvantagem penalizaria o arqueiro de perna quebrada, que o livro
poupa de propósito.

**O Deslocamento** entra no **botão "Desloc."** do Lote DESL-2, que já mostra
todas as linhas com a conta. Vira mais uma origem: *"Terrestre 2 — Pernas
Faltando (MB p.131)"*.

⚠️ E há um detalhe que o app precisa respeitar: o livro diz que ele **recebe
todos os pontos** pela redução de Deslocamento. Ou seja, a conta de pontos **não
pode** cobrar de novo por um Deslocamento baixo que veio daqui.>>

---



# Parte 2 — p.132 a p.140

## ⭐ Desastrado / Completamente Desastrado(pode fazer)
**−5 ou −15 pontos** · p.133

> **Completamente Desastrado:** qualquer **fracasso** em um teste de DX ou em uma
> perícia com base em DX **é considerado uma falha crítica**.

<<Esta não é bônus nem penalidade — é uma mudança na **classificação do
resultado**, e o app já tem o lugar exato: `CriticoRules.classificar`.

⚠️ E é a mais perigosa de errar, porque muda o desfecho da rolagem inteira: o
dado que daria "falha por 2" passa a mandar rolar na Tabela de Erro Crítico.
Precisa valer **só para DX e perícias de DX** — um fracasso em Teologia (IQ)
continua sendo fracasso comum.

O nível de −5 pontos não tem número: é o Mestre inventando trapalhadas. Fica de
fora.

<<ressalva- é possivel colocar pra aparecer o erro na jogada no discord, pra lembrar o mestre da desvantagem? uma mensagem no log do discord de erro!>>

## Desdobramento de Personalidade(pode fazer)
**−15 pontos\*** · p.133

> Todas as personalidades são de alguma forma superficiais e fingidas, o que impõe
> uma penalidade de **−1 em todos os testes de reação**. Além disso, as pessoas
> que **testemunharem uma mudança** de personalidade (…) recebem uma penalidade
> adicional de **−3**.

<<Dois efeitos de reação na mesma desvantagem: **−1 fixo** e **−3 condicional**
("de quem viu você trocar de personalidade"). O padrão já existe.>>

## ⭐ Dor Crônica(pode fazer)
**−5 a −15 pontos** · p.137
<<coloque a caixa no mesmo lugar onde fica o ST e DX braçal>>

> Enquanto o personagem estiver sentindo dor, **reduza a DX e a IQ** em: **−2**
> (Suave), **−4** (Grave), **−6** (Excruciante). **Reduza os testes de autocontrole
> (…) pelo mesmo valor.**

<<Não é permanente: vale **durante o surto**. Então é um **interruptor** na aba
Rolagem — "estou em surto de dor" — no mesmo molde da ST Braçal e da mão inábil.
Ligado, desconta de DX, IQ **e do número de autocontrole**.

⚠️ O terceiro alvo é o que a varredura por palavra-chave nunca acharia: mexer no
**NA do autocontrole** é uma família de efeito que o catálogo ainda não tem.>>

## Dorminhoco(pode fazer)
**−5 pontos** · p.137
<<coloque a caixa no mesmo lugar onde fica o ST e DX braçal>>

> Até uma hora depois de despertar (…), ele sofre **−2 em todos os testes de
> autocontrole** e **−1 em IQ** ou nas perícias baseadas em IQ.

<<Mesmo interruptor da Dor Crônica, com valores menores. Se um for feito, o outro
sai junto.>>

## Egoísmo(pode fazer)
**−5 pontos\*** · p.137


> (…) provavelmente provocando uma reação negativa (**penalidade de −3** na reação
> do alvo).

<<Reação −3 **condicional** — só depois de ele ofender alguém.>>

## Enjoo(pode fazer)
**−10 pontos** · p.138

> Um fracasso indica que o personagem vomita e sofre **−5 em todos os testes de
> habilidade, DX e IQ** durante o resto da viagem. Um sucesso indica que ele está
> apenas muito enjoado: **−2**.

<<Interruptor de três posições (bem / enjoado −2 / vomitando −5), da mesma família
da Dor Crônica. "Todos os testes de habilidade" é o curinga `*`.>>

## Entorpecido(pode fazer)
**−20 pontos** · p.138

> Quando estiver realizando uma tarefa que exige que seus olhos e mãos estejam
> coordenados, o personagem sofre **todos os efeitos de um nível da desvantagem
> Atrapalhado**.

<<Reaproveita a lista do Atrapalhado — que por sua vez reaproveita a da Destreza
Manual Elevada. Três traços, uma lista só: vale a pena fazer o Atrapalhado
primeiro e este vem quase de graça.>>

## ⭐ Disopia(pode fazer)
**−25 pontos** · p.135 · *parcialmente automatizada*

> **Hipermetrope:** **−6** nos testes de Visão para enxergar a menos de um metro
> e **−3 nos testes de DX** para qualquer tarefa manual que exija proximidade,
> incluindo o combate corporal.
>
> **Míope:** **−6** nos testes de Visão para enxergar a mais de um metro. Ele
> também sofre **−2 nos ataques corpo a corpo**. Para ataque à distância, **dobre
> a distância até o alvo** quando estiver calculando o modificador de distância.

<<O `SentidoRules` já trata o −6 do teste de Visão. Faltam as outras três, e uma
delas encaixa exatamente no que acabou de ser feito:

🔴 **"dobre a distância até o alvo"** entra na `TabelaVelocidadeDistancia` do Lote
MIRA-2. Um míope a 20 metros conta como **40**, que arredonda para o degrau de 50 → **−8** em vez de −6. É uma linha
de código, e sem ela o míope atira como quem enxerga.

⚠️ E as duas variantes são **opostas**: o hipermetrope é penalizado **de perto**,
o míope **de longe**. Um `porOpcao` não resolve, porque as duas custam −25 — vai
precisar do campo de descrição ou de duas entradas no catálogo.>>

## ⭐ Fácil de Matar(pode fazer)
**−2 pontos/nível** · p.140

> Cada nível impõe uma penalidade de **−1 nos testes de HT feitos para verificar a
> sobrevivência** do personagem quando ele está com **−1×PV Inicial ou abaixo**,
> assim como em qualquer teste onde fracassar causaria a morte súbita. **Isso não
> afeta a maioria dos testes normais de HT** — apenas aqueles que servem para
> evitar a morte. Os testes não podem ser reduzidos abaixo de **3**.

<<É o **espelho exato do Duro de Matar**, que o `MarcosDeVidaRules` e o
`ResistenciaRules` já somam. Basta a mesma leitura com o sinal trocado.

⚠️ Duas ressalvas que o livro faz questão de deixar claras, e que sem código
viram erro silencioso:
- **Só nos testes de morte.** Resistir a veneno, doença ou esforço **não** é
  afetado. A separação já existe no app (`ResistenciaRules` lista os testes um a
  um), então dá para acertar.
- **Piso de 3.** O alvo nunca desce abaixo disso — um HT 10 está limitado a Fácil
  de Matar 7.>>

## Estigma Social · Fantasias(pode fazer)
*Já declaradas no catálogo. A p.139 traz a tabela completa das variantes
(Cidadão de Segunda Categoria −1, Excomungado −3, Minoria −2, Monstro −3…) —
vale conferir se o `porOpcao` atual cobre todas.*

## Excesso de Confiança(pode fazer)
**−5 pontos\*** · p.140

> Impõe um **bônus de +2** aos testes de reação de pessoas **jovens ou ingênuas**
> e uma **penalidade de −2** a PdMs **experientes**.

<<Duas caixinhas com públicos opostos, na mesma desvantagem. É o caso que mostra
por que a reação virou caixinha em vez de soma: aplicar as duas sempre daria zero,
e o certo é o Mestre dizer com quem ele está falando.>>

## Fanatismo(pode fazer)
**−15 pontos** · p.140

> **Fanatismo Extremista:** recebe um **bônus de +3 nos testes de Vontade** para
> resistir à Lavagem Cerebral, Interrogatório e qualquer tentativa sobrenatural de
> controle da mente.

<<Caixinha no teste de Vontade — e é curioso: uma **desvantagem que dá bônus**. O
catálogo suporta (o valor é só um número), mas vale um comentário no JSON para
ninguém "corrigir" o sinal depois.>>

## Fácil de Decifrar(pode fazer)
**−10 pontos** · p.140

> (…) se ele também tiver **Veracidade**, sua **Lábia sofre uma penalidade de
> −5**.

<<O bônus de +4 é para **os outros** rolarem — fica de fora, como o Barulhento.

Mas o −5 em Lábia **só quando também tem Veracidade** é automatizável e é de um
tipo que ainda não existe no catálogo: **efeito que depende de outro traço estar
na ficha**. Hoje o `EfeitoInterpretador` não sabe fazer isso. Ou vira regra
Kotlin, ou fica como caixinha com o texto explicando.>>

---

# Até onde a leitura chegou

⚠️ **Este arquivo cobre p.124 a p.140** — 17 das 48 páginas, lidas uma a uma.
**Falta de p.141 a p.167.**

Não escrevi as que não li. A varredura por palavra-chave do
`Automações_Desvantagens.md` cobriu o resto nominalmente, mas ela é justamente o
que este arquivo veio substituir: nas 17 páginas lidas apareceram **doze**
automações que a varredura não tinha pegado, incluindo três que **corrigem coisas
já implementadas** (Cegueira × luz da cena, Disopia × distância do alvo, Fácil de
Matar × testes de morte).

## O que já dá para fazer, em ordem

| # | Item | Por quê |
|---|---|---|
| 1 | **Atrapalhado** | a lista de perícias já existe no catálogo (Destreza Manual), é copiar e trocar o sinal |
| 2 | **Baixa Autoestima** | uma linha de JSON — o curinga `*` foi criado no TAL-1 e serve exatamente para isto |
| 3 | **Fácil de Matar** | espelho do Duro de Matar, que o app já soma |
| 4 | **Cegueira Noturna** | encaixa no seletor de Luz da Cena, já pronto |
| 5 | 🔴 **Cegueira não deve levar penalidade de escuridão** | é erro que o app comete hoje |
| 6 | 🔴 **Míope dobra a distância do alvo** | uma linha na tabela do MIRA-2 |
| 7 | **Corcunda, Credulidade, Barulhento, Briguento, Circunspecção, Desdobramento** | reação e perícia, tudo JSON |
| 8 | **Deficiência Física** | perícia por `porOpcao` + linha no botão Desloc. do DESL-2 |
| 9 | **Completamente Desastrado** | mexe no `CriticoRules` — é código, e o mais delicado da lista |
| 10 | **Dor Crônica / Dorminhoco / Enjoo** | interruptor de estado; precisa da família "mexe no NA do autocontrole", que não existe |

## Dúvida

O **interruptor de estado** (Dor Crônica, Enjoo, Dorminhoco) é uma família nova:
um traço que só vale **enquanto o jogador diz que está valendo**, e que mexe em
atributo *e* no número de autocontrole. Quer que eu faça essa família junto, ou
prefere fechar primeiro tudo que é só JSON?
