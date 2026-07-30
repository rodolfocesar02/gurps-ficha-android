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

# Parte 3 — p.141 a p.163

## ⭐ Fora de Forma / Muito Fora de Forma
**−5 ou −15 pontos** · p.143

> **−1** (ou **−2**) em todos os testes de HT para permanecer consciente, evitar a
> morte, resistir aos efeitos de doenças e venenos, etc. **Isso não reduz sua HT
> nem as perícias baseadas nesse atributo.**

<<**O espelho da Boa Forma**, que o `ResistenciaRules` e o `MarcosDeVidaRules` já
somam. Mesma leitura, sinal trocado, `porOpcao` pelo custo.

⚠️ E ela contrasta de propósito com o **Fácil de Matar** que acabou de entrar: a
Fácil de Matar toca **só** os testes de morte; esta toca **todos** os testes de
resistência. Ter as duas na mesma tela, com essa diferença explícita, é o que
impede alguém "unificar" as duas por engano mais tarde.>>

## ⭐ Gagueira · Voz Irritante
**−10 pontos** · p.144 e p.162

> **−2 em todos os testes de reação** quando uma conversa for necessária, assim
> como nos testes de **Atuação, Canto, Diplomacia, Lábia, Oratória e Sex Appeal**.
>
> **Voz Irritante:** os efeitos são **idênticos aos da Gagueira**. É o oposto da
> Voz Melodiosa e **não é permitido comprar as duas**.

<<**O espelho exato da Voz Melodiosa**, que já está declarada. Duas desvantagens
pelo preço de uma: a Voz Irritante reusa a mesma lista.

E entra também no `IncompatibilidadeDeTracos` — é o segundo par proibido que o
livro dá explicitamente.>>

## ⭐ Timidez
**−5 / −10 / −20 pontos** · p.160

> Todas as perícias que o obrigam a lidar com o público (**Atuação, Boemia,
> Comércio, Diplomacia, Dissimulação, Intimidação, Lábia, Liderança, Manha,
> Mendicância, Oratória, Pedagogia, Política, Sex Appeal e Trato Social**) sofrem
> **−1** (Suave), **−2** (Grave). **Incapacitante:** não pode aprender nenhuma
> delas, e o predefinido sofre **−4** adicional.

<<Quinze perícias, `porOpcao` pelos três custos. É a maior lista única das 48
páginas.

⚠️ O nível de −20 **proíbe aprender** — isso é `PreRequisitoChecker`, não
`efeitos`. Declarar só a penalidade e deixar a proibição de fora seria entregar
metade.>>

## ⭐ Pouca Empatia
**−20 pontos** · p.154

> **−3** em todas as perícias que dependem da compreensão da motivação emocional:
> **Boemia, Comércio, Criminologia, Deslumbrar, Detecção de Mentiras, Diplomacia,
> Dissimulação, Interrogatório, Lábia, Liderança, Manha, Política, Psicologia,
> Sex Appeal, Sociologia e Trato Social.**
>
> Mutuamente excludente com **Insensível** e **Oblívio**.

<<Dezesseis perícias nomeadas, valor fixo. Mais um par (na verdade, um trio)
proibido para o `IncompatibilidadeDeTracos`.>>

## Oblívio · Insensível · Ingênuo · Incapaz de Sentir Prazer
**−5 a −15 pontos** · p.147, p.148, p.152

> **Oblívio:** **−1** para usar **ou resistir** às perícias de Influenciar —
> Diplomacia, Intimidação, Lábia, Manha, Sex Appeal e Trato Social.
>
> **Insensível:** **−3** em Pedagogia e em Psicologia (para ajudar); reação **−1**
> de ex-vítimas e de quem tem Empatia; **+1** em Interrogatório e Intimidação
> quando usa ameaça ou tortura.
>
> **Ingênuo:** **+4 para resistir a Sex Appeal**; **−4 em Trato Social**; reação
> **−2**.
>
> **Incapaz de Sentir Prazer:** **−3** em Boemia, Connoisseur, Arte Erótica e
> Jogos de Azar; reação de **−1 a −3** quando fica evidente.

<<Quatro desvantagens, todas JSON direto. ⚠️ Duas delas dão **bônus** em algum
ponto (o +1 do Insensível na Intimidação, o +4 do Ingênuo para resistir) — vale
comentário no JSON para ninguém "corrigir" o sinal.>>

## Mau Cheiro · Teimosia · Sadismo · Vozes Fantasmagóricas · Magnetismo Sobrenatural
**−5 a −15 pontos** · p.151, p.159, p.155, p.162, p.148

<<Todas dão **reação −2 ou −1**, umas fixas e outras condicionais. É o mesmo
molde já usado dezenas de vezes; entram em bloco.>>

## Megalomania · No Limite · Viciado em Trabalho
p.151, p.152, p.160

<<As três têm **duas caixinhas de públicos opostos**, como o Excesso de Confiança:
+2 de quem admira, −2 de quem acha loucura. O padrão já está pronto.>>

## Mão Fraca · Maneta · Sem Um Dedo · Zarolho
**−2 a −20 pontos** · p.151, p.149, p.157, p.163

> **Mão Fraca:** **−2 por nível** (máx. 3) nas tarefas que exigem mão firme —
> armas de combate corpo a corpo, escalar, pegar objetos no ar.
>
> **Maneta (Um Braço):** **−4** nas tarefas que podem ser feitas com um braço mas
> normalmente exigem dois (Escalada, Luta Greco-Romana). Não pode usar arma de
> duas mãos, nem arma + escudo.
>
> **Sem Um Dedo:** **−1 na DX daquela mão**. **Sem o Polegar:** **−5**.
>
> **Zarolho:** **−1 na DX** em combate e coordenação mão-olho; **−3 nos ataques à
> distância**, *a menos que ele realize uma manobra Apontar antes*.

<<Grupo de "corpo incompleto", e os quatro encaixam em coisas que **acabaram de
ser construídas**:

- O **Zarolho** entra no diálogo de Mira: o −3 some quando a caixinha **Apontei**
  está marcada. É a primeira desvantagem a *interagir* com o Apontar do MIRA-3.
- O **Sem Um Dedo** entra no seletor de **mão hábil/inábil** — a penalidade é de
  **uma mão só**, e o app já sabe qual mão está sendo usada.
- **Mão Fraca** e **Maneta** são listas de perícia com `porNivel`/`porOpcao`.>>

## ⭐ Temor
**−2 pontos/nível** · p.159

> Subtraia o nível de Temor da **Vontade** sempre que fizer uma **Verificação de
> Pânico** ou resistir a **Intimidação** ou a poder sobrenatural de medo. O
> personagem **adiciona** seu nível aos testes de Intimidação feitos **contra**
> ele. Não é permitido reduzir o alvo a menos de **3**. É o oposto de **Destemor**
> e não pode ter os dois.

<<Espelho do **Destemor**, que o `ResistenciaRules` já soma. Mesmo piso de 3 do
Fácil de Matar — a terceira regra do livro com esse piso, o que sugere extrair um
único lugar para ele.>>

## ⭐ Suscetibilidade à Magia · Suscetível
**Variável** · p.159

> **Suscetibilidade à Magia:** acrescente o nível ao NH de quem lançar mágica
> contra ele e **subtraia o mesmo valor dos testes para resistir**. Não vale
> contra Mágicas de Projétil, armas mágicas nem mágicas de informação. Combina com
> Aptidão Mágica, **mas não com Abascanto**.
>
> **Suscetível:** **−1 por nível** nos testes de HT para resistir a doença, veneno
> etc. Não pode reduzir a HT efetiva a menos de **3**. Não pode ser **Resistente**
> e Suscetível ao mesmo objeto.

<<**Suscetibilidade à Magia é o espelho exato da Resistência à Magia**, que já
está no card de Reação e Resistência com o texto das três exceções. Copiar o
texto e trocar o sinal — e entra no `IncompatibilidadeDeTracos` com o Abascanto,
que já está lá.>>

## Sem Imaginação
**−5 pontos** · p.156

> **−2** em qualquer tarefa que exigir criatividade ou imaginação, incluindo a
> maioria dos testes de Artista, todos os de Engenharia para invenções e todos
> para usar Desenvolvedor.

<<Palavra por palavra o **espelho do Versátil**, que usa o curinga `*`. Uma linha
de JSON.>>

## Assassino Relutante (Pacifismo)
**−5 pontos** · p.153

> Sempre que fizer um ataque letal contra uma pessoa que possa ver, ele sofre
> **−4** para acertar e **não pode Apontar**. Se não puder ver o rosto do inimigo,
> a penalidade é de **−2**.

<<Caixinha no diálogo de Mira, e a segunda desvantagem a conversar com o
**Apontar**: aqui ela **desabilita** a caixinha, em vez de ser cancelada por ela.
Vale a pena fazer as duas juntas.>>

## Paralisia Frente ao Combate
**−15 pontos** · p.153

> **−2 em todas as Verificações de Pânico.** É o oposto de **Reflexos em Combate**
> — não é possível adquirir ambos.

<<Mais um par proibido, e este é o que mais importa: **Reflexos em Combate é uma
das vantagens mais compradas do jogo**. Sem a trava, dá para comprar as duas e o
app soma +1 na defesa e −2 no pânico ao mesmo tempo.>>

## Invertebrado
**−20 pontos** · p.148

> Ele utiliza sua Base de Carga total para **empurrar**, mas apenas **1/4 da BC**
> para calcular o peso que é capaz de **erguer, carregar ou puxar**.

<<Entra no botão **Desloc.** do DESL-2, que já mostra a tabela de carga: com
Invertebrado, a coluna de peso-limite vira **1/4**. Sem isso o personagem carrega
quatro vezes mais do que deveria — e é o tipo de erro que ninguém confere.>>

---

# ✅ LEITURA COMPLETA — p.120 a p.163

As desvantagens acabam na **p.163** (Zarolho); da p.164 em diante são
**Peculiaridades**, que valem −1 ponto e por definição não têm efeito mecânico
fixo.

**44 páginas lidas uma a uma.** Achados que a varredura por palavra-chave do
`Automações_Desvantagens.md` não tinha pegado: **mais de 40**.

## As cinco famílias que a leitura revelou

A leitura completa mudou o desenho, e é exatamente por isso que valeu esperar:

### 1. Interruptor de estado — **10 clientes**, não 3

Um traço que só vale **enquanto o jogador diz que está valendo**, e que mexe em
atributo, perícia **e** no número de autocontrole:

**Dor Crônica** (−2/−4/−6 em DX, IQ e autocontrole) · **Dorminhoco** ·
**Enjoo** (−2/−5) · **Flashbacks** (−2/−5/bloqueia) · **Lunático** (−2 Vontade e
autocontrole na lua cheia) · **Repugnância** (−5 em tudo por 10 min) ·
**Problemas na Coluna** (−3/−4 em DX e IQ) · **Supersensitivo** (−1 a −4 por
gente perto) · **Sangue Frio** (−1 DX e Deslocamento por 5 °C) · **Fobias** (a
penalidade que sobra **mesmo quando ele passa** no teste).

<<Se eu tivesse desenhado essa família com os 3 clientes que conhecia na p.140,
teria errado: **Lunático e Sangue Frio mexem em Vontade e Deslocamento**, que não
estavam no meu desenho. Refazer depois de construído seria o caro.>>

### 2. Penalidade pelo número de autocontrole — **6 clientes**

Uma tabela `NA 6 → −4, NA 9 → −3, NA 12 → −2, NA 15 → −1` que se repete
literalmente igual em: **Covardia** (Verificação de Pânico) · **Gastar
Compulsivamente** (Comércio) · **Egoísmo** (reação) · **Fobias** (todos os
testes) · **Solitário** (reação) · **Xenofilia** (bônus na Verificação de Pânico).

<<É uma tabela só, com alvos diferentes. Merece um campo próprio no catálogo —
algo como `porAutocontrole` — em vez de seis regras Kotlin.>>

### 3. Espelhos de vantagem já implementada — **8 clientes**

Cada um é "copiar a leitura existente e trocar o sinal":

| Desvantagem | Espelho de | Onde já existe |
|---|---|---|
| Atrapalhado | Destreza Manual Elevada | ✅ feito (D-JSON) |
| Fácil de Matar | Duro de Matar | ✅ feito (D-FIX) |
| **Fora de Forma** | Boa Forma | `ResistenciaRules` |
| **Temor** | Destemor | `ResistenciaRules` |
| **Suscetibilidade à Magia** | Resistência à Magia | `ResistenciaRules` |
| **Suscetível** | Resistente | — |
| **Sem Imaginação** | Versátil | curinga `*` |
| **Voz Irritante / Gagueira** | Voz Melodiosa | catálogo |

### 4. 🔴 Pares mutuamente excludentes — **9**, e o app trava **1**

O `IncompatibilidadeDeTracos` existe desde o Lote RESIST-1 e conhece **um** par
(Abascanto × Aptidão Mágica). O livro dá pelo menos mais oito, todos com a
palavra "não é possível adquirir ambos":

- **Paralisia Frente ao Combate × Reflexos em Combate** ← o mais grave, porque
  Reflexos em Combate é das vantagens mais compradas
- Voz Irritante × Voz Melodiosa
- Temor × Destemor
- Atrapalhado × Destreza Manual Elevada
- Cegueira Noturna × Visão Noturna **e** × Visão no Escuro
- Mão Fraca × Manuseadores Precários
- Sem Noção de Profundidade × Zarolho
- Pouca Empatia × Insensível **e** × Oblívio
- Suscetível × Resistente (no mesmo objeto)

<<Isto é **baratíssimo** — é uma lista de pares num arquivo que já existe — e é o
que mais protege a ficha de ficar incoerente.>>

### 5. Conversam com o que acabou de ser construído — **4**

- **Zarolho** e **Assassino Relutante** ↔ a caixinha **Apontar** do MIRA-3 (um é
  cancelado por ela, o outro a desabilita)
- **Sem Um Dedo** ↔ o seletor de **mão hábil/inábil**
- **Invertebrado** ↔ a tabela de carga do **DESL-2** (peso-limite vira 1/4)

## Ordem que eu proponho

| # | Lote | O que | Por quê |
|---|---|---|---|
| 1 | **D-PAR** | Os 8 pares proibidos no `IncompatibilidadeDeTracos` | mais barato de todos, e é o que protege a ficha de ficar incoerente |
| 2 | **D-ESPELHO** | Fora de Forma, Temor, Suscetibilidade à Magia, Suscetível, Sem Imaginação, Gagueira, Voz Irritante | cada um é copiar leitura existente e trocar o sinal |
| 3 | **D-LISTA** | Timidez, Pouca Empatia, Oblívio, Insensível, Ingênuo, Incapaz de Sentir Prazer, Mau Cheiro, Teimosia, Mão Fraca, Maneta + o bloco de reação | JSON puro, ~90 efeitos |
| 4 | **D-NA** | O campo `porAutocontrole` + os 6 clientes | mecanismo novo, mas pequeno e com clientes conhecidos |
| 5 | **D-ESTADO** | O interruptor de estado + os 10 clientes | o maior; agora dá para desenhar sabendo que precisa cobrir Vontade e Deslocamento |
| 6 | **D-MIRA** | Zarolho, Assassino Relutante, Sem Um Dedo, Invertebrado | encaixam em telas prontas |
| 7 | **D-CRIT** | Completamente Desastrado no `CriticoRules` | o mais delicado: muda o desfecho da rolagem, não o número |

## Duas dúvidas

1. O **`porAutocontrole`** (item 4) é a mesma tabela em seis lugares. Faço como
   campo do catálogo, ou prefere seis regras Kotlin?
2. O **piso de 3** aparece em três regras diferentes (Fácil de Matar, Temor,
   Suscetível). Extraio para um lugar único, ou deixo repetido em cada uma?
