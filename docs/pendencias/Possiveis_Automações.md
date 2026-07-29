# Possíveis Automações — Vantagens (MB p.91 a p.101)

> **O que era este arquivo antes:** as suas anotações de Magro/Acima do Peso/Muito
> Gordo, Lamentável, Reputação, Status, Destreza Manual e o texto dos pontos de
> impacto. **Tudo aquilo já foi implementado** (Lotes AUTOM, TETO-HT, MIRA-1) e
> está no `PROGRESS.md`. O conteúdo antigo continua no histórico do git.
>
> **O que é agora:** a leitura das vantagens **página a página**, retomando de
> onde paramos — a ST de Golpe (p.90) já está pronta. Vai da p.91 até a p.101,
> onde as vantagens acabam e começam as Qualidades.
>
> **Regra que vale para tudo aqui:** o alvo é a **aba Rolagem** (jogo no
> Discord). O que só serviria para o combate da Saga está marcado como tal e
> **não deve ser feito**.
>
> Cada item tem: nome, custo, o trecho do livro **que interessa para automatizar**
> e, entre `<< >>`, a minha proposta de como ficaria no app.

---

## ⭐ Uma que ficou para trás na p.90

### Sorte
**15 / 30 / 60 pontos**

> Uma vez a cada hora de jogo, o personagem pode **refazer duas vezes** um teste
> ruim e **ficar com o melhor dos três resultados**. (…) A Sorte se aplica somente
> a testes de habilidade, avaliações de dano e testes de reação (…), ou quando o
> personagem está sendo atacado (nesse caso, o jogador faz as jogadas três vezes
> pelo atacante e **fica com o pior resultado**).
>
> Sorte Extraordinária: a cada 30 minutos. Sorte Impossível: a cada 10 minutos.

<<Esta é a que mais vale a pena de todas estas páginas, e eu passei direto por
ela porque você mandou começar na ST de Golpe. Fica a decisão com você.

No app: depois de qualquer rolagem, um botão **"Usar Sorte"** no resultado. Ele
rola mais duas vezes e mostra as três, marcando a melhor — e já grava o horário
do último uso, mostrando "disponível de novo em 47 min". Sem o relógio a
vantagem vira honra, e ninguém lembra da hora no meio da sessão.

O ponto delicado é que a Sorte **inverte** quando o personagem está sendo
atacado: aí é o **pior** dos três. Então o botão precisa saber se aquela rolagem
foi ofensiva ou defensiva — na aba Rolagem isso é fácil, porque a defesa já sai
de um card separado.>>

---

## Página 91

### Super Escalada
**3 pontos/nível**

> Cada nível de Super Escalada adiciona um bônus de **+1 ao Deslocamento** do
> personagem quando ele estiver **escalando** ou usando a vantagem Aderência.

<<Uma linha a mais no painel de Deslocamento: "Escalando: 5". É um número que o
app já sabe calcular e que hoje o jogador faz de cabeça. Barato de fazer.>>

### Super Salto
**10 pontos/nível**

> Cada nível de Super Salto **dobra** a distância e a altura que ele é capaz de
> saltar. O Deslocamento do personagem enquanto salta é o maior valor entre o seu
> Deslocamento terrestre normal e **1/5 da distância máxima** atingida durante um
> salto em distância.

<<Dá, mas exige trazer a tabela de Salto (p.356) para dentro do app, que hoje não
existe. Proposta: um card "Salto" no fim da Rolagem mostrando **salto em
distância** e **salto em altura** já dobrados pelos níveis.

Recomendo deixar para depois: é a única vantagem destas páginas que precisa de
uma regra nova inteira, e salto raramente decide uma cena no Discord.>>

### Super Sorte
**100 pontos**

> Uma vez a cada hora de jogo, o jogador pode **ditar o resultado** de qualquer
> jogada de dados (…). O NH efetivo do teste tem que ser no mínimo 3.

<<Não é conta, é escolha. O máximo que o app faz é um **contador de usos** com o
relógio de 1 hora, igual ao da Sorte. Se você fizer a Sorte, esta sai de graça
junto.>>

### Superaudição
**0 ou 5 pontos**

> O personagem é capaz de ouvir sons em frequências superiores à capacidade normal
> da audição humana.

<<Não gera número nenhum — não tem bônus, não tem teste. Fica de fora.>>

### ⭐ Talento (os dez do livro)(pode fazer)
**5 / 10 / 15 pontos por nível**

> - Um bônus de **+1 por nível em todas as perícias afetadas**, até mesmo nos
>   testes com valores predefinidos.
> - Um bônus de **+1 por nível em todos os testes de reação** feitos por qualquer
>   pessoa capaz de perceber o Talento do personagem, **se existir a chance de ela
>   ficar impressionada** com sua aptidão (a critério do Mestre).
> - O personagem nunca pode ter mais que **quatro níveis** em um determinado Talento.
>
> **Artífice:** Alvenaria, Armeiro, Carpintaria, Conserto de Equipamento
> Eletrônico, Eletricista, Engenharia, Maquinista, Mecânica e Ferreiro. Reação:
> qualquer pessoa para quem o personagem trabalha. *10 pontos/nível.*
>
> **Artista Talentoso:** Artista, Costura, Fotografia, Joalheiro e Trabalhos em
> Couro. Reação: quem adquirir ou apreciar o trabalho dele. *5 pontos/nível.*
>
> **Companheiro Animal:** Adestramento de Animais, Carregamento, Carroceiro,
> Cavalgar, Falcoaria e Veterinária. Reação: todos os animais. *5 pontos/nível.*
>
> **Curandeiro:** Cirurgia, Diagnose, Farmácia, Fisiologia, Medicina, Medicina
> Alternativa, Primeiros Socorros, Psicologia e Veterinária. Reação: ex-pacientes
> e atuais. *10 pontos/nível.*
>
> **Dedos Verdes:** Biologia, Conhecimento das Ervas, Cultivo, Jardinagem e
> Naturalista. Reação: jardineiros e plantas inteligentes. *5 pontos/nível.*
>
> **Agente Cativante:** Boemia, Detecção de Mentiras, Diplomacia, Dissimulação,
> Intimidação, Lábia, Liderança, Manha, Mendicância, Oratória, Política, Sex
> Appeal e Trato Social. Reação: enganadores, políticos, vendedores — **só se o
> personagem não estiver tentando manipulá-los**. *15 pontos/nível.*
>
> **Explorador:** Arremedo, Camuflagem, Naturalista, Navegação, Pesca,
> Rastreamento e Sobrevivência. Reação: exploradores e amantes da natureza.
> *10 pontos/nível.*
>
> **Habilidade Matemática:** Análise de Mercado, Astronomia, Contabilidade,
> Criptografia, Engenharia, Finanças, Física e Matemática. Reação: engenheiros e
> cientistas. *10 pontos/nível.*
>
> **Habilidade Musical:** Canto, Composição Musical, Influência Musical,
> Instrumentos Musicais e Performance em Grupo (Condução de Orquestra). Reação:
> quem ouvir ou apreciar o trabalho dele. *5 pontos/nível.*
>
> **Perspicácia Comercial:** Administração, Análise de Mercado, Comércio,
> Contabilidade, Economia, Finanças, Jogos de Azar e Propaganda. Reação: qualquer
> pessoa com quem faz negócios. *10 pontos/nível.*

<<**Esta é a maior automação que sobrou no livro inteiro, e não precisa de código
novo.** São dez vantagens, ~80 perícias no total, e o app já sabe fazer as duas
coisas que elas pedem:

1. O bônus por nível nas perícias é exatamente o `efeitos` declarado com
   `porNivel` — o mesmo mecanismo da Voz Melodiosa, que já funciona.
2. O bônus de reação é o **condicional com caixinha** que você pediu no Rosto
   Sincero: ele só vale "se existir a chance de a pessoa ficar impressionada", e
   quem sabe disso é o Mestre, não o app.

As dez já existem no catálogo (`vantagens.v3.json`), **sem nenhum efeito
declarado** — ou seja, hoje quem compra Artífice não ganha nada. É preencher
JSON, não escrever Kotlin.

Dois cuidados que eu já vejo:
- **Teto de 4 níveis.** O app deve avisar ao passar disso (aviso, não trava — do
  mesmo jeito que fizemos com o teto de HT do Magro).
- **Talentos que se sobrepõem.** Engenharia está em Artífice **e** em Habilidade
  Matemática; Veterinária em Companheiro Animal **e** em Curandeiro; Naturalista
  em Dedos Verdes **e** em Explorador. O livro diz que aí eles **somam** e podem
  passar de +4. A notinha de origem (Lote NOTA-1) vai mostrar as duas linhas, que
  é justamente o caso em que o jogador precisa conferir.
- O Agente Cativante tem a condição invertida: o bônus **some** se ele estiver
  manipulando. Vai no texto da caixinha.>>

---

## Página 92

### Talento Instintivo
**20 pontos/nível**

> Uma vez a cada sessão de jogo **por nível**, ele pode tentar fazer um teste
> contra **qualquer perícia**, utilizando o valor do **atributo apropriado**: IQ
> para as perícias baseadas em IQ, DX para as baseadas em DX e assim por diante.
> Ele **não sofre nenhuma penalidade** pela utilização do valor predefinido, mas
> os modificadores de situação e de equipamento se aplicam normalmente. (…) Esta
> vantagem **não surte efeito nas perícias que o personagem já conhece**.

<<Dá, e é útil no Discord: hoje, para rolar uma perícia que não está na ficha, o
jogador tem que abrir o livro para descobrir o valor predefinido e a penalidade.

Proposta: no diálogo de perícias, as que o personagem **não tem** aparecem numa
seção "não conhecidas" com o NH predefinido calculado. Quem tiver Talento
Instintivo ganha um botão **"usar Talento Instintivo"** que troca o predefinido
pelo **atributo cheio**, sem penalidade, e desconta um uso da sessão.

O contador de usos por sessão é a parte chata: o app não sabe quando a sessão
começou. Sugiro um botão "zerar usos" manual em vez de tentar adivinhar.>>

---

## Páginas 93 e 94

### Telecinese
**5 pontos/nível**

> O personagem consegue manipular objetos à distância (…) com uma **ST igual ao
> nível do personagem em Telecinese (TC)**. (…) Em situações onde o personagem
> faria um teste de ST, faça o mesmo **contra sua TC**. (…) Qualquer coisa que se
> beneficiaria de uma Destreza Manual Elevada recebe um **bônus de +4** se o
> personagem obtiver um sucesso em um teste de IQ.

<<Duas metades, e só uma serve para a Rolagem:

**Serve:** a caixinha "rolar com TC em vez de ST", irmã da que já existe para a
ST Braçal e a ST de Levantamento. Mesmo padrão, mesmo lugar da tela, custo quase
zero de fazer.

**Também serve:** o +4 vira caixinha condicional nas mesmas perícias da Destreza
Manual — que você já mandou marcar em todas as perícias. Encaixa direto no que
está pronto.

**Não serve (é da Saga):** manobra Concentrar, arremesso, agarrar, levitação.
Isso é combate no grid. Pela sua regra, **não fazer**.>>

### Telecomunicação
**10 a 30 pontos**

> Para estabelecer contato é necessário um segundo de concentração e um **sucesso
> num teste de IQ**. (…) Ele é capaz de manter vários contatos, mas o teste de IQ
> sofre uma **penalidade cumulativa de -1 para cada contato após o primeiro**.

<<Já existe uma regra Kotlin para a Telecomunicação (`TelecomunicacaoRule`), mas
ela não cobre isso. O -1 por contato é um número que o jogador tem que contar na
mão.

Proposta pequena: um teste "Telecomunicação" no botão de Reação e Resistência,
com um seletor de **quantos contatos já estão abertos**. Baixa prioridade — é
raro.>>

---

## Página 95

### Terror
**30 pontos + 10 por -1 de Verificação de Pânico**

> Qualquer indivíduo que o vir ou ouvir deve fazer uma **Verificação de Pânico**
> imediatamente. (…) É possível comprar penalidades adicionais a um custo de 10
> pontos por **-1** aplicado ao teste. As vítimas recebem um **bônus de +1 para
> cada Verificação de Pânico depois da primeira** feita em um período de 24 horas.

<<Aqui quem rola é o **alvo**, não o personagem — e o app é uma ficha, não tem
alvo. O útil seria uma linha de consulta: "Terror: alvo faz Verificação de Pânico
com -3". Uma frase, para o jogador colar no Discord.

Automatizar a Verificação de Pânico em si (p.360) é outro assunto, e caberia mais
no Mestre de Regras do que na Rolagem.>>

### Títere
**5 a 10 pontos**

<<Sem conta nenhuma — é um acordo de mesa sobre um Aliado. Fora.>>

### ⭐ Tolerância a Ferimentos
**5 a 100 pontos**

> **Sem Cérebro:** Oponentes não podem usá-lo como alvo para causar dano
> adicional. (…) um golpe no crânio ou no olho é considerado como um **golpe
> normal no rosto**. *5 pontos.*
>
> **Sem Cabeça:** O personagem **não tem os pontos de impacto "crânio" e "rosto"**
> e não tem necessidade de um elmo. *7 pontos.*
>
> **Sem Olhos:** Como o personagem não tem olhos, **eles não podem ser atacados**.
> Ele também é **imune a ataques que produzem cegueira**. *5 pontos.*
>
> **Sem Órgãos Vitais:** Considere todos os golpes em "**órgãos vitais**" ou na
> "**virilha**" como golpes no **tronco**. *5 pontos.*
>
> **Sem Pescoço:** Ele **não tem o ponto de impacto "pescoço"** e não pode ser
> decapitado, asfixiado nem estrangulado. *5 pontos.*
>
> **Sem Sangue:** Ele **não sangra**, não é afetado por toxinas de origem
> sanguínea. *5 pontos.*

<<**Esta encaixa direto no diálogo de Mira que acabamos de fazer** (Lote MIRA-1),
e é a única das oito que tem lugar certo na tela hoje.

O diálogo lista Crânio, Rosto, Pescoço, Olho, Órgãos Vitais, Virilha… Se o
personagem for Sem Cabeça, esses pontos **não existem no corpo dele**. Hoje o
app oferece todos para todo mundo.

Mas atenção ao lado de quem: a Mira é para **atacar o inimigo**, e essas
vantagens são do **próprio personagem**. Então o efeito certo na aba Rolagem não
é sumir com as opções — é:

1. **No card de PV/marcos**, com Sem Sangue: o teste de **sangramento** nunca
   aparece. Esse é imediato e correto.
2. **Uma nota na ficha** dizendo quais pontos de impacto o corpo dele não tem,
   para o Mestre saber na hora que descrever o golpe.

Sumir com as opções do diálogo de Mira só faria sentido no combate da Saga, onde
o app conhece o alvo — e aí, pela sua regra, **não fazer agora**.>>

---

## Página 96

### Tolerância à Radiação · Tolerância à Temperatura · Tolerância Ampliada
**Variável / 1 ponto por nível / 5 a 25 pontos**

> **Temperatura:** Cada nível acrescenta um número de graus igual à **HT/2** do
> personagem à sua zona de conforto (padrão humano: 2 °C a 32 °C), distribuídos
> como o jogador quiser entre os extremos.

<<Só a Temperatura gera número, e é um número **de ambiente** — o app não sabe
que temperatura faz na cena. O que dá é mostrar a faixa calculada na ficha:
"Zona de conforto: -3 °C a 37 °C". Uma linha de consulta, sem teste.

Radiação e Gravidade não produzem nada que caiba na aba Rolagem.>>

---

## Página 97

### Toque Sensível
**10 pontos**

> Ele recebe um bônus de **+4** (além de quaisquer bônus devidos a Tato Apurado)
> em **qualquer tarefa que utiliza o tato**; ex.: um teste de Perícia Forense para
> notar as semelhanças entre dois pedaços de tecido ou um teste de **Revistar**
> para sentir pequenos objetos escondidos.

<<Caixinha condicional, igual ao Rosto Sincero. O livro diz "qualquer tarefa que
utiliza o tato" e dá dois exemplos — ou seja, **não é uma lista fechada**, quem
decide é o Mestre. Isso é exatamente o caso de uso da caixinha: o app oferece,
o jogador marca quando vale.

Sugiro deixá-la disponível em **todas** as perícias, como você decidiu para a
Destreza Manual, com o texto "+4 se a tarefa for pelo tato".>>

### ⭐ Treinado por um Mestre
**30 pontos**

> Ele sofre **metade da penalidade usual** quando utiliza um **Golpe Rápido** ou
> **Aparar mais de uma vez por turno**. Esses benefícios se aplicam a todas as
> suas perícias de **combate desarmado** e perícias com **Armas Brancas**.

<<Encaixa no diálogo de Mira, que já é o lugar onde escolhemos penalidades de
ataque. É só mais uma opção na lista:

- **Golpe Rápido:** -6 normalmente, **-3** com Treinado por um Mestre.
- **Aparar repetido:** -4 por apara extra, **-2** com a vantagem.

O segundo item cabe no card de defesa da Rolagem: um seletor "esta é a Nª apara
do turno", que já desconta certo. Hoje isso é conta de cabeça no meio do combate.

Cuidado: **só vale para desarmado e Armas Brancas**. Arma de fogo não entra.>>

### Ultravisão
**0 ou 10 pontos** — *já parcialmente automatizada*

> O personagem recebe um bônus de **+2 em todos os testes de Visão** realizados
> sob luz UV, assim como em todos os testes de **Observação, Perícia Forense e
> Revistar**. (…) Durante a noite (…) o permite **ignorar até -2 em penalidades
> por escuridão** (cumulativo com Visão Noturna).

<<O catálogo já dá o +2 nas três perícias. **Faltam duas coisas:** o +2 no teste
de **Visão** (que hoje vive no `DialogoSentidos`, não no catálogo) e os -2 de
escuridão. Completar é barato.>>

### Venturoso
**15 pontos**

> Toda vez que assume um **risco desnecessário** (na opinião do Mestre), o
> personagem recebe um bônus de **+1 nos testes de habilidade**. Além disso, se
> obtiver uma **falha crítica** num rompante de alto risco, o jogador pode **fazer
> um novo teste**.

<<Caixinha condicional em todas as perícias — "+1 se estiver correndo risco
desnecessário". A condição é opinião do Mestre, que é exatamente o que a caixinha
resolve.

A segunda metade é melhor ainda: quando a rolagem der **falha crítica** e o
personagem tiver Venturoso, o app oferece o botão **"refazer"** na hora. O app já
sabe reconhecer falha crítica.>>

### Versátil
**5 pontos**

> Ele recebe um bônus de **+1 em qualquer tarefa que exija criatividade ou
> inventividade**, incluindo a maioria dos testes com a perícia **Artista**, os
> testes de **Engenharia** para novas invenções (…).

<<Mesma receita: caixinha em todas as perícias, "+1 se a tarefa exigir
criatividade". Junto com Toque Sensível e Venturoso, são três caixinhas do mesmo
molde — dá para fazer as três de uma vez.>>

### Vida Extra
**25 pontos/vida**

> Toda vez que volta dos mortos, o personagem **gasta uma Vida Extra** — remova-a
> da planilha e **reduza em 25 o total de pontos** do personagem.

<<Não é teste, é contador — mas é um contador que **mexe no custo da ficha**, e
isso o app sabe fazer. Um botão "usar uma vida" que baixa o nível da vantagem e
recalcula os pontos sozinho.

Vale porque, feito na mão, é o tipo de coisa que fica errada meses depois.>>

### Ver o Invisível · Vínculo Especial (p.98)
**15 pontos / 5 pontos**

<<Nenhuma das duas gera número ou teste. Fora.>>

---

## Página 98

### Visão 360 Graus
**25 pontos**

> Ele **não sofre redutores** quando estiver se defendendo de ataques laterais ou
> pelas costas. Ele é capaz de atacar inimigos nas laterais ou na retaguarda (…)
> mas sofre uma **penalidade de -2**. (…) Ele recebe um **bônus de +5** em suas
> tentativas de detectar tentativas de **Perseguição** e **nunca é surpreendido**
> por um ataque pelas costas.

<<Só o **+5 em Perseguição** serve para a aba Rolagem — e serve bem, porque é um
número fixo numa perícia nomeada: vira `efeitos` declarado, sem código.

O resto (defesa lateral, ataque nas costas) depende de saber onde o inimigo está
— é grid, é Saga. **Não fazer.**>>

### Visão Hiperespectral
**25 pontos** — *já parcialmente automatizada*

> Concede um bônus de **+3 em todos os testes de Visão**, incluindo os testes com
> as perícias **Observação, Perícia Forense ou Revistar** e em **todos os testes
> de Rastreamento**.

<<🔴 **Achei um buraco aqui.** O app já dá o +3 no **teste de Visão** (está no
`SentidoRules`), mas **não dá nas quatro perícias** — o catálogo está sem efeito
nenhum para ela. Quem compra Visão Hiperespectral hoje ganha metade do que pagou.

Conserto: quatro linhas de `efeitos` no catálogo. É o mesmo formato da Ultravisão,
que está logo ao lado e já tem três das quatro.>>

### Visão Microscópica · Visão no Escuro · Visão Penetrante
**5 pontos/nível / 25 pontos / 10 pontos/nível**

> **Visão no Escuro:** Ele **não sofre nenhuma penalidade** em suas perícias por
> causa da escuridão, independente de sua origem.

<<Microscópica e Penetrante não geram número. A Visão no Escuro gera, mas depende
de um seletor de escuridão que **o app ainda não tem** — ver a Visão Noturna,
logo abaixo, que é a que justifica criar esse seletor.>>

### ⭐ Visão Noturna
**1 ponto/nível (máximo 9)**

> Cada nível permite que ele **ignore uma penalidade de -1 provocada pela
> escuridão** em testes que envolvam a visão ou no combate, contanto que haja pelo
> menos um pouco de luz. (…) Ela **não surte nenhum efeito sobre a penalidade de
> -10 de uma escuridão total**.
>
> *Exemplo:* Visão Noturna 4 eliminaria completamente as penalidades de uma
> escuridão de até -4 — e reduziria uma penalidade de -7 para apenas **-3**.

<<Vale a pena, e resolve um problema maior que a própria vantagem.

Proposta: um **seletor de iluminação** no painel de modificadores que já existe
(`PainelModificadorGlobal`), com as opções do livro — penumbra -1, escuro -3,
muito escuro -6, escuridão total -10. O jogador escolhe a luz da cena **uma vez**
e o modificador entra em todas as rolagens de visão e combate.

A Visão Noturna então **come parte dessa penalidade sozinha**, e a linha mostra a
conta: "Escuro -7, Visão Noturna 4 → **-3**". Sem isso, o jogador tem que lembrar
do nível dele e subtrair de cabeça a cada rolagem.

⚠️ E a regra que o app precisa saber para não errar: na **escuridão total (-10)
a Visão Noturna não vale nada**. Quem tem Visão no Escuro, aí sim, ignora tudo.>>

---

## Página 99

### Visão Periférica
**15 pontos**

> Se for atacado pela direita ou pela esquerda, o personagem é capaz de se
> defender **sem sofrer penalidades**. Sua defesa ativa sofre uma penalidade de
> **-2 nos ataques pelas costas**. Fora do combate, o personagem recebe um bônus
> de **+3 nos testes para detectar tentativas de Perseguição** ou emboscadas.

<<Mesma divisão da Visão 360 Graus: o **+3 em Perseguição** vira `efeitos`
declarado agora; a parte de defesa por ângulo é grid/Saga e **não deve ser
feita**.>>

### Visualização
**10 pontos**

> Para usar este talento, o personagem precisa se concentrar durante um minuto
> (…). Em seguida, ele faz um **teste de IQ**. O personagem recebe um bônus de
> **+1 na ação visualizada para cada ponto na margem de sucesso**. Se as
> circunstâncias não forem exatamente as mesmas, o bônus é **reduzido pela metade**
> (no mínimo +1). Se alguma coisa for gritantemente diferente, **divida por 3**
> (sem valor mínimo). (…) Isso a torna **inútil durante um combate**.

<<Gosto desta: é uma calculadora fechada, e a conta é chata de fazer na mão.

Um diálogo "Visualização": rola IQ, mostra a margem de sucesso, e pergunta em três
botões — *idêntico / parecido / muito diferente*. O app aplica ÷1, ÷2 (mínimo +1)
ou ÷3 (sem mínimo) e devolve **"+4 no próximo teste"**.

O bônus resultante devia poder ser **guardado** e oferecido como caixinha na
próxima rolagem — senão o jogador anota num papel e esquece.>>

### Voo
**40 pontos**

> O Deslocamento do personagem em voo é igual à sua **Velocidade Básica × 2**;
> descarte todas as frações.

<<Uma linha no painel de Deslocamento: "Voando: 12". O app já tem a Velocidade
Básica. Barato, e some sozinho para quem não tem a vantagem.

As dezenas de ampliações e limitações (Alado, Planar, Voo Espacial…) mudam
**como** ele voa, não o número — isso é narrativa.>>

### Visão Telescópica
**5 pontos/nível**

> Cada nível permite a ele **ignorar uma penalidade de distância do alcance de
> -1** relacionada aos testes de Visão, ou de **-2 se ele realizar uma manobra
> Apontar**. Essa habilidade também pode funcionar como uma mira telescópica,
> concedendo um bônus de **Precisão de +1 por nível** nos ataques à distância,
> contanto que o personagem **não realize uma manobra Apontar durante um número de
> segundos igual ao bônus**.

<<Depende de uma coisa que o app ainda não tem: **penalidade por distância**. Se
um dia entrar um campo "distância do alvo" na Rolagem, esta vantagem é a primeira
cliente — e aí o desconto é automático.

Enquanto isso não existe, fazer só a Visão Telescópica seria automatizar o
desconto de uma penalidade que ninguém está aplicando.>>

---

## Página 101

### Voz Melodiosa — ✅ **já feita**

> +2 em Arremesso, Atuação, Canto, Diplomacia, Lábia, Oratória, Política e Sex
> Appeal, e +2 em qualquer teste de reação de quem puder ouvir sua voz.

<<Já está no catálogo com a caixinha de reação e sete das oito perícias. **Falta o
Arremesso.**

Mas eu desconfio da tradução antes de mexer: "Arremesso" é perícia de **atirar
objetos**, e nada tem a ver com voz. No original a lista tem *Broadcast*
(transmissão de rádio/TV), que faz todo sentido e provavelmente virou "Arremesso"
por engano de tradução. Se for isso, o certo é **não** adicionar Arremesso.
Me diga como quer que fique.>>

### Xeno-adaptabilidade
*V. Adaptabilidade Cultural, p.35*

<<Só um apontador para outra vantagem. Nada a fazer.>>

---

# Resumo — o que eu faria, em ordem

| # | O que | Por que primeiro | Esforço |
|---|---|---|---|
| 1 | **Os dez Talentos** | ~80 perícias sem bônus nenhum hoje; é preencher JSON, sem Kotlin novo | baixo |
| 2 | **Visão Hiperespectral** (as 4 perícias) | é buraco, não melhoria: a vantagem entrega metade | baixo |
| 3 | **+3/+5 em Perseguição** (Visão Periférica e 360°) | duas linhas de JSON | baixo |
| 4 | **Três caixinhas novas**: Toque Sensível +4, Venturoso +1, Versátil +1 | mesmo molde do Rosto Sincero, feitas de uma vez | baixo |
| 5 | **Sorte** (rolar 3, ficar com a melhor) | a que mais muda o jogo no Discord | médio |
| 6 | **Seletor de iluminação + Visão Noturna** | resolve a vantagem e cria a base para escuridão em geral | médio |
| 7 | **Treinado por um Mestre** no diálogo de Mira | Golpe Rápido -3 e apara repetida -2 | médio |
| 8 | **Linhas de Deslocamento**: voando, escalando | dois números que o app já sabe calcular | baixo |
| 9 | **Visualização** | calculadora fechada, conta chata na mão | médio |
| 10 | **Talento Instintivo** | precisa da lista de perícias não conhecidas | médio |

**Descartadas de propósito** (grid/Saga, pela sua regra): as manobras da
Telecinese, o ângulo de defesa da Visão 360° e da Periférica, o Terror do lado do
alvo, e os pontos de impacto da Tolerância a Ferimentos no diálogo de Mira.

**Sem automação possível** (não geram número nem teste): Superaudição, Títere,
Ver o Invisível, Vínculo Especial, Visão Microscópica, Visão Penetrante,
Tolerância à Radiação, Tolerância Ampliada, Xeno-adaptabilidade.

---

## Dúvidas para você

1. **Sorte** está uma página antes do ponto que você mandou começar. Faço?
2. O **contador de usos por sessão** (Talento Instintivo, Sorte, Super Sorte): o
   app não sabe quando a sessão começa. Botão "zerar" manual resolve, ou você
   prefere que ele não conte nada e só mostre o texto da regra?
3. O **seletor de iluminação** é maior que a Visão Noturna sozinha — ele passa a
   valer para todas as rolagens de visão e combate. Topa esse alcance, ou prefere
   que a Visão Noturna só apareça como nota informativa?

---
---

# MIRA-2 — Distância do alvo no diálogo "Onde acertar"

*Pedido seu, 29/07/2026: "existe uma regra de acertar alvos com arma à
distância, uma regra de a cada 2 metros de distância tem uma penalidade de -1".*

## ⚠️ Primeiro, a regra não é essa

Fui atrás nos chunks e a penalidade **não é linear**. Se fosse −1 a cada 2
metros, atirar a 100 metros daria −50 e ninguém acertaria nada. O que o livro diz
(**MB p.373**):

> Como regra geral, um alvo até **2 metros** de distância está suficientemente
> próximo para que **não haja penalidade** para atingi-lo. A **3 metros**, há uma
> penalidade de **-1**; até **5 metros**, **-2**; até **7 metros**, **-3**; até
> **10 metros**, **-4**; e assim por diante, com **cada aumento aproximado de 50%
> na distância resultando numa penalidade de -1**. (…) No caso de distâncias que
> se encontram entre dois valores da tabela, **use a maior distância**.
>
> **Exemplo:** O alvo está a **17 metros**. Esse valor é arredondado para **20
> metros** na tabela, resultando numa penalidade de **-6**.

Ou seja: cresce rápido no começo (2 → 3 metros já custa −1) e depois desacelera
muito (de 100 para 150 metros custa outro −1 só). É a **Tabela de Tamanho e
Velocidade/Distância**, MB p.550-551, coluna "Velocidade/Distância":

| Distância | Penalidade | | Distância | Penalidade |
|---:|:---:|---|---:|:---:|
| até 2 m | **0** | | 50 m | **−8** |
| 3 m | **−1** | | 70 m | **−9** |
| 5 m | **−2** | | 100 m | **−10** |
| 7 m | **−3** | | 150 m | **−11** |
| 10 m | **−4** | | 200 m | **−12** |
| 15 m | **−5** | | 300 m | **−13** |
| 20 m | **−6** | | 500 m | **−14** |
| 30 m | **−7** | | 700 m | **−15** |

E segue: **1 km = −16**, 1,5 km = −17, 2 km = −18, 3 km = −19, 5 km = −20. A
regra de continuação é fechada: *cada aumento de 10 vezes na medida linear vale
mais −6*.

<<A tabela é a mesma coisa que a régua de tamanho do alvo (MT), só com o sinal
trocado — por isso ela se chama "Tamanho E Velocidade/Distância". Isso importa
para o app: **uma tabela só resolve os dois**, e ainda serve para o alvo veloz
(soma a velocidade em m/s à distância antes de consultar) e para os testes de
Sentidos.>>

## Como eu faria no app

### 1. A tabela em Kotlin puro

Arquivo novo `domain/rules/TabelaVelocidadeDistancia.kt`, ao lado do
`LocaisDeAtaque.kt` que a mira já usa. Uma função e nada mais:

```
penalidadePara(metros) -> Int
```

Dois cuidados que os testes precisam travar:

- ⚠️ **Arredonda para a distância MAIOR**, nunca para a mais próxima. 17 metros
  vira 20 metros (−6), **não** 15 metros (−5). Errar isso deixa o jogador com um
  bônus que ele não tem — e ninguém percebe.
- ⚠️ **Até 2 metros é zero**, e o livro é explícito sobre o porquê: *"disparar
  contra um alvo próximo não é mais fácil (nem mais difícil) que atacar em
  combate corporal"*.

### 2. Uma linha de distância no topo do diálogo de Mira

**Desenho do usuário (rascunho de 29/07):** duas linhas no cabeçalho,
*"Distância"* e *"Velocidade do Alvo"*, cada uma com um `− 0 +`; o jogador ajusta
e a calculadora interna já desconta em todas as partes do corpo.

O lugar está certo. Três ajustes:

**a) O `−/+` anda de LINHA DA TABELA, não de metro em metro.**

Se cada toque valer 1 metro, chegar a 100 metros são 98 toques. Andando pela
tabela — 2 → 3 → 5 → 7 → 10 → 15 → 20 → 30 → 50 → 70 → 100 — são **13 toques, e
cada toque vale exatamente −1**.

<<Isso é o melhor que sai do rascunho: o botão deixa de ser um contador de metros
e passa a ser a **própria regra**. Um passo mais longe = um ponto mais difícil. O
jogador aprende a tabela usando o app, sem nunca abrir o livro.>>

**b) 🔴 Distância e velocidade NÃO são duas penalidades — são uma só.**

Duas linhas com dois `− 0 +` separados dão a entender que cada uma desconta o
seu. O livro manda o contrário (MB p.551):

> Acrescente a velocidade em metros/segundo **à distância** antes de consultar a
> coluna "Medida Linear".
>
> **Exemplo:** Um motoboy a **40 metros** viajando a 90 km/h (**30 m/s**) tem uma
> velocidade/distância de 40 + 30 = **70**, que resulta numa base de 70 metros, e
> implica numa penalidade de **−9**.

Somando primeiro: **−9**. Como duas penalidades separadas: −7 (40 m) e −7
(30 m/s) = **−14**. Quase o dobro do certo.

<<Dois campos de entrada tudo bem — mas **um número só de saída**, e mostrando a
conta na tela: `40 m + 30 m/s = 70 → −9`. Sem a conta visível, o jogador não tem
como desconfiar quando estiver errado, que é a mesma razão das notinhas de origem
do Lote NOTA-1.>>

**c) A velocidade merece menos espaço que a distância.**

O próprio livro: *"Na maioria dos combates que envolve combatentes a pé e objetos
inanimados, é preferível **ignorar a velocidade**"*. Ela fica atrás de um botão
`+ velocidade`, e só aparece quando o Mestre disser que o alvo está correndo.

**O resultado, em uma linha só:**

```
Alvo:  [ − ]  20 m  [ + ]   + velocidade                    −6
```

Ao mudar, **os 13 números da lista recalculam na hora**. Com o Arco NH 18 a 20
metros, o Torso passa a mostrar **12** e o Olho, **3**.

<<Uma linha em vez de duas também resolve um problema que apareceu no print do
rascunho: as duas linhas **empurraram o começo da lista para fora da tela** — a
lista abria direto no "Vitais", sem Torso, Braço e Perna. O cabeçalho não pode
comer o alvo mais comum do jogo.>>

<<Por que dentro do diálogo de Mira e não num painel separado: porque as duas
penalidades se somam no **mesmo** ataque, e o valor do diálogo é justamente
mostrar o número final sem conta mental. Se a distância ficasse fora, o jogador
veria "Crânio 11" na tela e teria que subtrair −6 de cabeça — que é exatamente o
problema que a mira veio resolver.

E há um detalhe de fluxo: hoje o **toque simples** no NH rola direto no torso.
Com a distância no diálogo, o toque simples continuaria rolando **sem** ela. Duas
saídas: ou a distância vira um estado que fica guardado na aba (e o toque simples
já a usa), ou o toque simples passa a valer só para corpo a corpo. **Prefiro a
primeira**, e acho que ela devia ficar visível no card de Ataque: "alvo a 20 m"
embaixo do NH, igual ao "mod +2" que já aparece lá.>>

### 3. O que a arma já sabe, e que dá de graça

Os campos já existem na ficha desde o Lote 371 — `armaMaximoMetros` (Máx),
`armaMeioDanoMetros` (1/2D), `armaPrecisao` (Prec) e `armaAlcanceMultStRaw`
(arcos: ×10/×15 da ST). Com a distância na mão, o diálogo pode avisar sozinho:

- **Passou do Máx** → a linha inteira em vermelho: *"fora de alcance (Máx 200 m)"*.
  Hoje o jogador só descobre lendo a ficha da arma.
- **Passou do 1/2D** → nota: *"além do 1/2D — dano pela metade"*. Essa é a que
  mais escapa na mesa, porque não muda o ataque, muda o **dano**.
- **Precisão** → uma caixinha *"Apontei 1 turno (+3)"*, que soma o Prec da arma.
  O livro deixa isso somar até o dobro do Prec com turnos extras (MB p.373), mas
  eu começaria com o caso simples de um turno.

<<Cuidado com o arco: o Máx dele **não é um número fixo**, é múltiplo da ST
(`×10/×15`). Um arco na mão de ST 12 alcança bem mais longe que o mesmo arco numa
ST 9. Então o aviso de "fora de alcance" tem que resolver o multiplicador contra
a ST de quem está empunhando — e, se a ST Braçal estiver ligada, contra ela.>>

### 4. O Modificador de Tamanho do alvo (opcional, e eu deixaria para depois)

A mesma tabela dá o MT: alvo grande é **bônus**, alvo pequeno é **penalidade**.
Um cavalo é +2, um carro +2, um prédio +4; uma pomba é −5.

<<Dá para pôr um segundo seletor "tamanho do alvo" ao lado da distância, mas isso
é informação que **só o Mestre tem**, e cada botão a mais no diálogo é um atrito
a mais no meio da mesa. Minha sugestão: **fazer a distância primeiro**, usar por
umas sessões, e só então decidir se o MT vale o espaço na tela.>>

### 5. Quando o campo aparece — e o app já sabe distinguir

Só em **ataque à distância**. Numa Espada não faz sentido: a mira continua exatamente
como está hoje.

**Pergunta do usuário: "o app já faz essa distinção?"** Faz, em dois lugares —
mas **nenhum dos dois chega ao diálogo de mira hoje**:

1. **Pela arma.** O catálogo é dividido em três arquivos
   (`armas_corpo_a_corpo`, `armas_distancia`, `armas_fogo`) e o campo
   `armaTipoCombate` só assume três valores: `corpo_a_corpo`, `distancia` e
   `armas_de_fogo`. Sem ambiguidade nenhuma. Hoje quem lê isso é
   `TraducaoFichaParaCombate.kt` — arquivo **da Saga**.
2. **Pela perícia.** Dentro de `PERICIAS_COMBATE` (em `Personagem.kt`) já existe
   o bloco marcado `// Pericias de Ataque a Distancia`: Arco, Arcos, Besta,
   Zarabatana, Funda, Bolas, Laço, Rede, Arremesso, Facas de Arremesso, Shuriken
   e a família de Armas de Fogo.

<<O problema do item 2 é que está tudo num `setOf` só — o comentário separa para
**humano ler**, não para o código perguntar. Conserto: dividir em
`PERICIAS_COMBATE_DISTANCIA` e `PERICIAS_COMBATE_CORPO_A_CORPO`, e fazer
`PERICIAS_COMBATE` virar a **união** das duas. Assim nada que usa a lista antiga
quebra.

E a decisão no diálogo deve usar as **duas** fontes, nesta ordem:
- **Se há arma escolhida na fonte de dano** → vale o tipo dela. É o dado mais
  confiável, veio do catálogo.
- **Sem arma** → vale a perícia do ataque.

Isso cobre o caso que aparece no seu print: ataque "Arcos" com a fonte de dano
ainda em "Dano ST". Só pela arma, o campo não apareceria; só pela perícia, uma
faca de arremesso empunhada com a perícia Faca ficaria de fora. As duas juntas
acertam os dois casos.

⚠️ E tem uma amarra a evitar: a regra do que é "à distância" **não pode ficar
morando na Saga**, senão a ficha volta a depender dela — é o mesmo motivo que fez
o `LocalAtaque` mudar de pacote no MIRA-1. Vai para `domain/rules/`, e o combate
passa a importar de lá.>>

## O que isto destrava depois

A **Visão Telescópica** (p.99, na lista lá em cima) ficou marcada como
"impossível por enquanto" justamente porque o app não tinha penalidade de
distância. Com esta tabela pronta, ela vira uma linha: cada nível **cancela −1**
dessa penalidade (ou −2 se o personagem Apontou).

Mesma coisa para as **penalidades de longa distância** que a Diapsiquia da
Telecomunicação usa (MB p.241) — é a mesma régua.

## Ordem sugerida

| # | Passo | Por que nesta ordem |
|---|---|---|
| 1 | `TabelaVelocidadeDistancia.kt` + testes | é a regra; sem ela o resto chuta |
| 2 | Separar `PERICIAS_COMBATE` em corpo a corpo × distância, e a regra do tipo de arma sair da Saga para `domain/rules/` | é o que decide se a linha aparece |
| 3 | Linha de distância no diálogo de Mira, com o `−/+` andando pela tabela | é o pedido, e sozinho já resolve |
| 4 | Velocidade do alvo atrás do `+ velocidade`, **somando** à distância | decidido no rascunho de 29/07 |
| 5 | Distância visível no card de Ataque | senão o toque simples ignora ela em silêncio |
| 6 | Avisos de Máx e 1/2D | os dados já estão na ficha, é só ler |
| 7 | Caixinha de Precisão/Apontar | fecha o cálculo do ataque à distância |
| 8 | Visão Telescópica | cliente da tabela, agora possível |
| 9 | MT do alvo | só se você sentir falta depois de usar |

## O teste que não pode faltar

⚠️ O caso do motoboy do livro, como teste automático: **40 m + 30 m/s tem de dar
−9**, e não −14. É a única forma de garantir que a soma acontece **antes** da
consulta à tabela, e não depois. Um erro aí dobra a penalidade e ninguém percebe
olhando a tela.

Junto com o do arredondamento: **17 m tem de dar −6** (arredonda para 20 m), e não
−5.

---
---

# PLANO — 29/07/2026 · ✅ **FECHADO no mesmo dia**

## O que está liberado

Lendo o arquivo inteiro, encontrei **uma** marca sua: `(pode fazer)` no **Talento**
(linha 93). Não há nenhum `<<<...>>>` em lugar nenhum do arquivo, e só existe uma
cópia dele na árvore — as outras ressalvas não salvaram.

Então o plano abaixo tem duas partes: o **Lote TAL-1**, que está liberado e
detalhado; e a fila do resto, esperando um "sim" por número.

---

## 🔴 O achado que muda o plano do Talento

Antes de escrever qualquer JSON eu fui conferir **como o app casa o nome da
perícia** — e é aqui que este lote pode falhar em silêncio.

`PericiaSelecionada.calcularNivel` chama
`TraitRuleRegistry.getSkillBonus(personagem, nome)`, e lá dentro o valor sai de
`bonuses[skillName]`: **busca exata por chave de mapa**. O `alvo` do `efeitos`
tem de ser **idêntico** ao `nome` da perícia no catálogo, acento por acento.

E o catálogo carrega `/NT` em boa parte delas:

| O livro escreve | O catálogo tem |
|---|---|
| Engenharia | **Engenharia/NT** |
| Mecânica | **Mecânica/NT** |
| Matemática | **Matemática/NT** |
| Cirurgia | **Cirurgia/NT** |
| Armeiro, Eletricista, Ferreiro, Maquinista | **…/NT** |
| Costura, Fotografia, Joalheiro | **…/NT** |
| Medicina | **Medicina/NT** |
| Alvenaria, Carpintaria, Contabilidade | *sem `/NT`* |
| Medicina Alternativa | *sem `/NT`* |

Ou seja: **não é uma regra uniforme** — "Medicina/NT" leva a barra e "Medicina
Alternativa" não. Escrever a lista do livro direto no JSON faria ~40 dos ~80
bônus **não fazerem nada, sem erro nenhum no log**. É a mesma família do bug do
`matematica nt` × `matematica aplicada` que consertamos em 28/07.

<<Analogia: é discar um telefone certo com o DDD errado. A ligação não dá erro de
"número inválido" — só não chama.>>

---

## Lote TAL-1 — os dez Talentos

### TAL-1a · A rede primeiro (e ela vale para tudo que já existe)

**Um teste que varre os dois catálogos** (`vantagens.v3.json` e
`desvantagens.v2.json`) e falha se algum `efeitos` de `tipo: "pericia"` tiver um
`alvo` que **não existe** em `pericias.json`.

⚠️ Este passo vem **antes** de escrever o Talento, e por dois motivos:

1. Sem ele, eu escrevo 80 linhas e não tenho como saber quais pegaram.
2. Ele confere **de graça os 91 efeitos que já estão no catálogo**. Pode muito
   bem já haver bônus mudo lá dentro hoje — e se houver, este teste acha.

<<Este é o tipo de teste que a memória chama de "camada certa": não é uma asserção
pontual sobre um personagem, é um **invariante do catálogo**.>>

### TAL-1b · Preencher os dez

Para cada Talento, uma entrada de `efeitos` por perícia:

```json
{ "tipo": "pericia", "alvo": "Engenharia/NT", "valor": 1, "porNivel": true }
```

Mais **uma** entrada de reação, condicional:

```json
{ "tipo": "reacao", "valor": 1, "porNivel": true,
  "condicao": "de quem pode ficar impressionado com a aptidão dele" }
```

O `porNivel: true` já existe e funciona (`valorPara(nivel)`), e as dez vantagens
já estão no catálogo como `costKind: "perLevel"` — então nível 3 dá +3 sem código
novo.

O texto da condição muda por Talento, porque a plateia muda: *"qualquer pessoa
para quem ele trabalha"* no Artífice, *"todos os animais"* no Companheiro Animal,
*"ex-pacientes e atuais"* no Curandeiro.

⚠️ **Agente Cativante é o único com a condição invertida:** o bônus **some** se
ele estiver tentando manipular a pessoa. O texto da caixinha tem de dizer isso,
senão o jogador marca sempre.

### TAL-1c · Talentos que se sobrepõem — têm de SOMAR

Três perícias estão em dois Talentos ao mesmo tempo:

| Perícia | Está em |
|---|---|
| Engenharia/NT | Artífice **e** Habilidade Matemática |
| Veterinária | Companheiro Animal **e** Curandeiro |
| Naturalista | Dedos Verdes **e** Explorador |

O livro permite: *"talentos que se sobrepõem (e apenas eles) podem conceder bônus
maiores que +4"*.

O interpretador já soma quando o mesmo alvo aparece duas vezes, e a notinha do
NOTA-1 já sabe listar origem por origem. **Mas isso precisa de teste**, porque é
o caso em que um número grande aparece na tela e o jogador precisa poder conferir
de onde vieram as duas parcelas.

### TAL-1d · O teto de 4 níveis — aviso, não trava

O livro: *"nunca pode ter mais que quatro níveis em um determinado Talento"*.

Primeiro conferir se o campo `max` do catálogo (hoje `null` nas dez) já limita o
seletor de níveis. Se limitar, é só preencher `4`. Se não, entra como **aviso** no
mesmo molde do teto de HT do Magro — porque o Mestre pode ter liberado, e travar
a ficha do jogador por uma regra que entrou depois é o erro que já cometemos com
o `conhecimento_oculto`.

### TAL-1e · O gate

Build nas 2 variantes, suíte inteira, `PROGRESS.md`, commit. **Sem APK** — só
quando você pedir.

### Por que este lote vale o esforço

São ~80 perícias que hoje **não recebem nada**. Quem compra Artífice nível 2 e
gasta 20 pontos não ganha um único ponto de NH. E não precisa de uma linha de
Kotlin novo: é JSON mais testes.

---

## A fila do resto — esperando seu "sim" por número

| # | Item | O que é | Depende de |
|---|---|---|---|
| 2 | **Visão Hiperespectral** | 4 linhas de JSON; hoje a vantagem entrega metade do que custa | nada |
| 3 | **+5 e +3 em Perseguição** (Visão 360° e Periférica) | 2 linhas de JSON | nada |
| 4 | **Três caixinhas**: Toque Sensível +4, Venturoso +1, Versátil +1 | mesmo molde do Rosto Sincero, as três de uma vez | nada |
| 5 | **Sorte** | rolar 3 e ficar com a melhor (ou a pior, se estiver sendo atacado) + relógio de 1 h | decisão sobre o contador |
| 6 | **Seletor de iluminação + Visão Noturna** | penumbra −1 … escuridão total −10, e a Visão Noturna comendo parte | decisão de alcance |
| 7 | **Treinado por um Mestre** | Golpe Rápido −3 e apara repetida −2, no diálogo de Mira | nada |
| 8 | **Linhas de Deslocamento** | "Voando: 12", "Escalando: 5" | nada |
| 9 | **Visualização** | calculadora fechada: rola IQ, aplica ÷1/÷2/÷3, guarda o bônus | nada |
| 10 | **Visão Telescópica** | agora é possível: cancela −1 da distância por nível | ✅ MIRA-2 pronto |
| 11 | **Talento Instintivo** | perícias não conhecidas com o predefinido calculado | contador de sessão |
| 12 | **Sem Sangue** (Tolerância a Ferimentos) | o teste de sangramento nunca aparece | nada |

**Os itens 2, 3 e 4 dão para fazer no mesmo lote do Talento** — são todos JSON e
caixinha, e o gate roda uma vez só.

---

## As três decisões que travam itens

1. **Sorte (item 5)** — está na p.90, uma página antes de onde você mandou
   começar. Faço ou deixo fora?
2. **Contador por sessão** (Sorte, Super Sorte, Talento Instintivo) — o app não
   sabe quando a sessão começou. Botão **"zerar usos"** manual, ou não contar nada
   e só mostrar o texto da regra?
3. **Seletor de iluminação (item 6)** — ele é maior que a Visão Noturna: passa a
   valer para **todas** as rolagens de visão e combate. Topa esse alcance, ou
   prefere que a Visão Noturna fique só como nota informativa?

E sobre a **Voz Melodiosa**: o livro em português lista "Arremesso", que é perícia
de atirar objetos e nada tem a ver com voz — no original é *Broadcast*
(transmissão). Confirma que **não** devo adicionar?


---
---

# ✅ FECHAMENTO — 29/07/2026

Você disse "pode fazer todos os lotes em loop". Os **12 itens** do plano estão
resolvidos: **11 feitos** em 7 lotes, **1 descartado** de propósito.

| # | Item | Lote | Versão |
|---|---|---|---|
| 1 | Os dez Talentos | TAL-1 | 3.9 |
| 2 | Visão Hiperespectral (4 perícias) | TAL-1 | 3.9 |
| 3 | +5/+3 em Perseguição | TAL-1 | 3.9 |
| 4 | Toque Sensível, Venturoso, Versátil | TAL-1 | 3.9 |
| 6 | Seletor de iluminação + Visão Noturna | LUZ-1 | 4.0 |
| 8 | Deslocamento voando e escalando | DESL-1 | 4.0 |
| 7 | Treinado por um Mestre | MESTRE-1 | 4.1 |
| 5 | Sorte | SORTE-1 | 4.2 |
| 10 | Visão Telescópica + Apontar/Precisão | MIRA-3 | 4.3 |
| 9 | Visualização | VIS-1 | 4.4 |
| 11 | Talento Instintivo | TI-1 | 4.5 |
| 12 | Sem Sangue | ❌ **não feito** — o sangramento vive em `domain/combat/`, que é da Saga | — |

## As decisões que tomei sozinho, e por quê

As três dúvidas do plano estavam bloqueando itens, e o "loop" não podia parar
nelas. Escolhi assim — qualquer uma é uma linha de mudar:

1. **Sorte (estava uma página antes do ponto de partida):** **fiz.** É a que mais
   muda o jogo no Discord, e deixá-la fora por uma página seria formalismo.
2. **Contador por sessão:** **botão de zerar manual.** O app não sabe quando a
   sessão começou; chutar devolveria usos que você já gastou.
3. **Seletor de iluminação com alcance amplo:** **fiz amplo** — entra no ataque,
   nas defesas e no teste de Visão. A Visão Noturna sozinha não justificaria a
   tela; o seletor sim.
4. **"Arremesso" na Voz Melodiosa:** **não adicionei.** É perícia de atirar
   objetos e no original a lista tem *Broadcast* — erro de tradução.

## O que apareceu no caminho e não estava no plano

- 🔴 **A vantagem Agente Cativante não existia no catálogo.** Foi criada.
- 🔴 **Dois nomes de perícia com acento comido:** `Mendicncia` e `Analise de
  Mercado`. Nome errado ali é bônus mudo para sempre, porque o casamento é por
  nome exato. Entrou um teste que varre o catálogo procurando o sinal (3+
  consoantes seguidas).
- 🔴 **Carisma não dava as quatro perícias** que o livro nomeia.
- **Seis nomes meus errados** foram pegos pela conferência contra o catálogo antes
  de virarem bônus mudo — `Pesca` é `Pescaria`, `Trato-Social` tem hífen,
  `Conserto de Equipamento Eletrônico/NT (†)` tem adaga.
- **A apara repetida tem quatro degraus, não dois** — eu havia planejado errado.
- **O Talento Instintivo não dependia do valor predefinido**, como eu havia
  escrito. Bom, porque `preDefinicoes` está vazio nas 281 perícias.


---
---

# PLANO — Botão "Desloc." com todos os deslocamentos (Lote DESL-2)

*Pedido seu no T-L7: "ao invés de colocar apenas quando aparece a vantagem, as
características derivadas de deslocamento — Desloc., Voando, Escalando, Nadando
etc. — podemos deixar um botão com nome `Desloc.` sem número algum aparecendo;
clica nele, aparecem todos os tipos de deslocamento que temos nas regras do
livro."*

## Por que a sua ideia é melhor que o que eu fiz

O que eu fiz no DESL-1 foi somar **células condicionais** na linha de
Características Derivadas. No seu print isso já está apertado: *Vel. Básica ·
Desloc. · BC · Voando · Escalando* — cinco números numa linha, e faltam pelo
menos três tipos de deslocamento. Cada vantagem nova empurra a linha mais um
pouco, até quebrar.

Um botão único resolve os dois problemas de uma vez: a linha volta a ter tamanho
fixo, e **cabe tudo** — inclusive os deslocamentos que hoje ficam de fora porque
não teriam onde aparecer.

## O que o livro dá (conferido, p.17-19 e p.353)

### Terrestre

- **Deslocamento Básico** = Velocidade Básica **sem a fração** (5,75 → 5).
- **Por nível de carga** (p.17): Nenhuma = cheio; Leve **×0,8**; Média **×0,6**;
  Pesada **×0,4**; Muito Pesada **×0,2**. ⚠️ *"Ignore todas as frações. A Carga
  nunca reduz o Deslocamento ou a Esquiva a um valor inferior a 1."*
- **Disparada** (p.353): correr em linha reta é um pouco mais rápido.

<<Esta é a parte que mais vale: os cinco níveis de carga de uma vez, com o peso
que o personagem está carregando **agora** marcando qual linha está valendo. Hoje
o jogador tem de multiplicar de cabeça no meio da cena.>>

### Aquático

> O Deslocamento aquático normalmente é igual ao **Deslocamento Básico/5**,
> arredondado para baixo. (…) Personagens **anfíbios** têm um Deslocamento
> aquático e terrestre **iguais ao Deslocamento Básico**. Personagens
> **Aquáticos** têm um Deslocamento aquático igual ao Deslocamento Básico e um
> **Deslocamento terrestre de zero**.

<<🔴 **Achei um buraco aqui.** O app já tem `bonusDeslocamentoAquatico` e mostra a
linha, mas ele calcula **sempre** como `Básico/5 + bônus`. As vantagens
**Anfíbio** (p.39) e **Aquático** (p.156) mudam a conta inteira — e a segunda
zera o deslocamento **terrestre**, que o app continuaria mostrando cheio. Vale
conferir e consertar junto.>>

### Aéreo

> Sem nenhuma vantagem especial, o Deslocamento aéreo de um personagem é sempre
> **zero**. Personagens com Voo têm um Deslocamento aéreo igual à **Velocidade
> Básica × 2 (não Deslocamento Básico × 2)**. (…) Personagens com **Caminhar no
> Ar** têm um Deslocamento aéreo **igual ao terrestre**.

<<⚠️ O livro **avisa** entre parênteses que é Velocidade Básica e não Deslocamento
Básico — sinal de que erram muito nisso. O DESL-1 acertou (Velocidade 5,75 → 11,
como no seu print), mas fica registrado aqui para nunca "simplificar" errado.

E falta o **Caminhar no Ar** (p.46), que iguala o aéreo ao terrestre.>>

### Escalando

- **Deslocamento + Super Escalada** (p.91), valendo também com Aderência.

### Saltando

- Salto em distância e em altura (p.356), dobrados por nível de **Super Salto**.

<<Esta é a única que precisa de **regra nova**: a tabela de Salto não existe no
app. É o item mais caro do lote — sugiro deixá-la para o fim e entregar o botão
sem ela primeiro, com as outras cinco.>>

## Como eu faria

### 1. Uma regra só, que devolve a lista

Arquivo `domain/rules/DeslocamentosRules.kt`, substituindo o
`DeslocamentosEspeciais.kt` do DESL-1 (que fica pequeno demais para o que o botão
precisa). Uma função:

```
todosOsDeslocamentos(personagem) -> List<Linha>
```

Cada `Linha` traz **rótulo, valor, unidade e a conta** — a conta é o que faz o
número ser conferível, igual às notinhas de origem do NOTA-1:

| Rótulo | Valor | Conta |
|---|---|---|
| Terrestre | 5 m/s | Velocidade Básica 5,75 sem fração |
| — com carga Leve | 4 m/s | 5 × 0,8 |
| — com carga Média | 3 m/s | 5 × 0,6 |
| Nadando | 1 m/s | Deslocamento 5 ÷ 5 |
| Voando | 11 m/s | Velocidade Básica 5,75 × 2 |
| Escalando | 6 m/s | Deslocamento 5 + Super Escalada 1 |

⚠️ **Todas as linhas aparecem sempre**, inclusive as de valor **zero** — e é aí
que o botão fica melhor que as células condicionais. Ver "Aéreo: 0 (sem a
vantagem Voo)" **ensina a regra**; a célula que simplesmente não existe não ensina
nada, e o jogador fica sem saber se é zero ou se o app esqueceu. É o mesmo motivo
do aviso de alcance no MIRA-2b: silêncio é resposta ambígua.

### 2. O botão e o diálogo

Na aba Geral, a linha de Características Derivadas fica **fixa**:
*Vel. Básica · Desloc. · BC · Dano GdP · Dano GeB* — e um botão **"Desloc."** que
abre a lista. Sem número no botão, como você pediu: o número que interessa muda
com a cena, então o botão não deveria eleger um.

<<Sugiro que o **nível de carga atual** venha destacado na lista, calculado do
peso do equipamento que já está na ficha. Aí o botão responde à pergunta que o
jogador realmente faz — "quanto eu ando agora?" — e não só "quanto eu andaria".>>

### 3. Ordem sugerida

| # | Passo | Por que nesta ordem |
|---|---|---|
| 1 | `DeslocamentosRules.kt` com terrestre + os 5 níveis de carga | é o mais usado, e hoje é conta de cabeça |
| 2 | Aquático, aéreo e escalando, todos com a conta à vista | já existem, só mudam de lugar |
| 3 | Botão "Desloc." + diálogo; a linha derivada volta a ser fixa | resolve o aperto da linha |
| 4 | Consertar **Anfíbio** e **Aquático** | é buraco, não melhoria |
| 5 | **Caminhar no Ar** | uma linha, depois do item 4 |
| 6 | **Disparada** (p.353) | precisa ler a página |
| 7 | **Saltando** + Super Salto (p.356) | regra nova, a mais cara |

## Dúvida

O diálogo deve mostrar **todos os cinco níveis de carga** sempre, ou só o nível
atual com um "ver todos"? Eu prefiro todos — a tabela inteira ensina a regra e é
curta —, mas se a tela ficar cheia, o "ver todos" resolve.
