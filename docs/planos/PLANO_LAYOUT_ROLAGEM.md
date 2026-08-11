# Plano — a aba Rolagem conforme a mockup de 11/08

Comparação entre a tela atual (foto das 4:50) e a mockup do Photoshop (4:52).
São **três** diferenças. Nenhuma mexe em regra: é tudo diagramação.

---

## Diferença 1 · O cabeçalho do canal encolhe para uma linha

**Hoje:** um botão de **50 dp** com duas linhas — `EDITAR CANAL` em cima (18 sp,
negrito) e `Ilmenitia / Raízes do Mundo` embaixo, menor.

**Na mockup:** uma linha só — **`CANAL`** em negrito e o nome do canal **ao lado**,
no mesmo eixo, com o botão visivelmente mais baixo.

### O que muda no código

`RolagemHeader`, em `ui/features/rolagem/RolagemComponents.kt`. A `Column` de dois
`Text` vira uma `Row` centralizada, e a altura cai de 50 para ~40 dp.

### O que precisa de cuidado

⚠️ **O nome do canal pode ser longo.** Hoje ele tem a largura inteira só para
si; numa linha compartilhada, ele divide o espaço com a palavra `CANAL`. Vai
precisar de `weight(1f)` + `maxLines = 1` + reticências, senão um canal de nome
comprido empurra o `CANAL` para fora.

⚠️ **A palavra "EDITAR" some.** Ela é o que diz que o botão *faz* alguma coisa.
Sem ela, `CANAL Ilmenitia` parece um rótulo, não um botão. Como o app tem
variante para quem não enxerga, a descrição acessível **precisa** continuar
dizendo "editar canal" — o texto visível pode encurtar, a fala não.

**Risco:** baixo. Um arquivo, um composable.

---

## Diferença 2 · Ataque e Dano viram o cabeçalho dos próprios cartões

**Hoje:** os dois botões azuis ficam numa fileira, e os dois cartões claros vêm
**abaixo**, separados por um respiro.

**Na mockup:** cada botão está **colado no topo do seu cartão**, formando um bloco
só — `Ataque` em cima de *Ataque Inato (Feixe) / NH 14*, e `Dano` em cima de
*Dano ST / GdP GeB / 1d-2*.

### O que muda no código

`AtaqueDanoQuickArea`, no mesmo arquivo. Hoje a estrutura é:

```
Row { Button(Ataque)   Button(Dano) }      <- fileira própria
Column(pointerInput) {
    Row { Column{ Card NH }   Column{ Card Dano } }
}
```

Passa a ser:

```
Row {
    Column { Button(Ataque)  Card NH     }
    Column { Button(Dano)    Card Dano   }
}
```

Ou seja, **os botões entram nas colunas** em vez de viverem numa fileira à parte.

### O que precisa de cuidado

🔴 **O gesto de arrastar o modificador do ataque.** A `Column` que envolve os
cartões tem um `pointerInput` com `detectVerticalDragGestures` — é ele que muda o
`mod +1` deslizando o dedo. Se os botões entrarem **dentro** dessa área, arrastar
começando em cima do botão pode virar um gesto ambíguo: o `Button` consome o
toque, e o arraste pode nem começar.

A saída é manter o `pointerInput` só na parte dos cartões, não no bloco inteiro.
Isso significa que o botão fica **por fora** do modificador de arraste, dentro da
mesma coluna — dá para fazer, mas é o ponto onde este item pode quebrar sem
avisar (o gesto some e a tela continua bonita).

⚠️ **As alturas das duas colunas.** Hoje o `Card` do ataque tem `fillMaxHeight()`
para igualar com o do dano, que é mais alto (tem GdP/GeB e a linha do dano). Com
o botão dentro da coluna, esse cálculo muda de lugar. A mockup mostra as duas
colunas **desiguais** — o cartão do Dano desce mais que o do Ataque —, então
possivelmente o `fillMaxHeight` sai.

**Risco:** médio. É o único item que mexe em gesto.

---

## Diferença 3 · "Mão hábil" volta para a coluna da esquerda

**Hoje:** ela fica **abaixo dos dois cartões**, à esquerda — foi assim que o ROL-3
a deixou, e eu já tinha marcado como aproximação.

**Na mockup:** ela está **dentro da coluna da esquerda**, logo abaixo do cartão do
Ataque, com o cartão do Dano descendo mais que ela pelo lado direito.

### O que muda no código

Sai de onde está (fim do composable) e entra na `Column` da esquerda, depois do
`Card`. Como a Diferença 2 já reorganiza essas colunas, **os dois itens são o
mesmo trabalho** — fazer separados seria mexer duas vezes no mesmo bloco.

⚠️ A caixinha do **Sem Um Dedo** vai junto: é a mesma pergunta, e separá-las
deixaria uma em cada lugar.

**Risco:** baixo, se for feito junto com a Diferença 2.

---

## O que NÃO mudou entre as duas telas

Registrado para não haver dúvida depois:

- O cartão de atributos, PV/PF, Esquiva/Apara — idênticos.
- As faixas de Luz da cena e Apara do turno — idênticas (o ROL-4 já as arrumou).
- Perícias, Reação e Resistência, Magias, Rolagem Livre — idênticos.
- Histórico da Sessão — idêntico.

---

## Ordem proposta

| lote | o quê | risco |
|---|---|---|
| **ROL-5** | Diferenças **2 e 3** juntas — a reorganização das colunas de ataque | médio |
| **ROL-6** | Diferença **1** — o cabeçalho do canal | baixo |

O ROL-5 primeiro porque é o que muda mais, e porque as duas diferenças dele são o
mesmo bloco de código. O ROL-6 é independente e pode vir depois sem retrabalho.

⚠️ **Os dois pedem teste no aparelho**, e o ROL-5 pede um teste específico: **o
arraste do modificador do ataque continua funcionando?** É o que pode sumir em
silêncio.

---

## As duas perguntas que valem confirmar antes

1. **A palavra "EDITAR" pode sumir do botão do canal?** Na mockup ela sumiu. Se
   for de propósito, ok — só preciso manter a fala do leitor de tela dizendo o
   que o botão faz.
2. **As duas colunas de ataque podem ter alturas diferentes?** Na mockup o cartão
   do Dano desce mais que o do Ataque. Hoje elas são forçadas à mesma altura.

---

# Revisão de 11/08 — o que eu tinha lido errado, e as respostas do usuário

## 🔴 Diferença 4 · O cartão de atributos encolhe (eu tinha dito que não mudava)

Eu listei o cartão de atributos como "idêntico". **Estava errado** — comparei o
*conteúdo* (ST, DX, IQ, HT, VON, PER, PV, PF: os mesmos) e não o **respiro**.

Comparando os dois recortes lado a lado:

- O **espaço abaixo do PV/PF** é bem maior hoje do que na mockup. Na mockup a
  linha do PV/PF quase encosta na borda de baixo do cartão.
- O **espaço entre os números dos atributos e a linha do PV/PF** também encolheu.
- No total, o cartão fica visivelmente **mais baixo**.

⚠️ Vale registrar o formato do meu erro: **"o conteúdo é o mesmo" não é "a tela é
a mesma"**. Diagramação é justamente o que sobra depois que o conteúdo é igual, e
foi exatamente isso que o usuário estava pedindo para olhar.

### Onde mexer

Três números empilhados, todos em `TabRolagem` e `PainelAtributosEStatus`:

| onde | hoje | efeito |
|---|---|---|
| `outerCardVerticalPadding` | 8 dp (tela pequena) / **12 dp** | o respiro de cima e de baixo do cartão |
| `Arrangement.spacedBy(6.dp)` no `Column` do painel | **6 dp** | o vão entre a linha dos atributos e o PV/PF |
| `innerCardVerticalPadding` | 4 / **6 dp** | o respiro dentro de cada coluna de atributo |

⚠️ **Os dois primeiros são compartilhados com outros painéis** da mesma tela
(defesas, iluminação, ataque). Mudar o valor global encolhe tudo junto — pode até
ser desejável, mas é uma mudança bem maior que a pedida. O caminho conservador é
o painel de atributos passar a receber os seus próprios valores.

---

## Resposta 1 · O texto do botão do canal se adapta ao tamanho

Decisão do usuário: **"EDITAR" sai**, e o texto **se adapta ao botão** — cresceu,
a fonte diminui; encurtou, a fonte volta a crescer.

### ⚠️ O detalhe técnico que isso esbarra

O projeto está no **Compose BOM 2024.09.02**. O `autoSize` nativo do `BasicText`
(`TextAutoSize.StepBased`) só chegou depois disso — **não está disponível aqui**.

Então "a fonte se adapta" precisa ser feito à mão, e há duas formas:

1. **Medir e reduzir** (`onTextLayout` + `didOverflowWidth` → baixa 1 sp e
   recompõe até caber). Funciona em qualquer versão, mas recompõe algumas vezes
   até assentar e precisa de um piso, senão o texto pode virar ilegível num canal
   de nome muito longo.
2. **Subir o Compose BOM** para uma versão com `autoSize`. Resolve de vez e serve
   para as outras telas, mas é uma atualização de dependência no meio de um lote
   de layout — merece lote próprio, com o gate completo.

**Proposta:** fazer o (1) agora, com piso de fonte definido, e anotar o (2) como
melhoria futura. Não vou subir o BOM dentro de um lote de diagramação.

⚠️ E a fala continua: o texto visível pode virar só `CANAL`, mas a descrição
acessível **precisa** continuar dizendo que o botão **edita** o canal — senão,
para quem usa leitor de tela, ele deixa de anunciar o que faz.

---

## Resposta 2 · Simetria só na parte de cima

Decisão do usuário: as duas colunas de ataque são **simétricas em cima**
(os botões `Ataque` e `Dano` com a mesma altura e alinhados) e **assimétricas
embaixo**, porque a coluna da esquerda ganha o quadrado da **mão inábil**.

Isso resolve a dúvida do `fillMaxHeight`:

- **Os botões** ficam iguais e alinhados no topo — é o que dá a leitura de
  "duas colunas".
- **Os cartões** deixam de ser forçados à mesma altura. Cada um fica com a altura
  do próprio conteúdo, e a caixinha da mão entra embaixo do da esquerda.

---

## Ordem revisada

| lote | o quê | risco |
|---|---|---|
| **ROL-5** | Diferenças **2 e 3** — as colunas de ataque (botão como cabeçalho, mão inábil na coluna esquerda, simetria só em cima) | médio — é o do gesto de arraste |
| **ROL-6** | Diferença **4** — o respiro do cartão de atributos | baixo |
| **ROL-7** | Diferença **1** — o cabeçalho do canal, com fonte que se adapta | baixo/médio — a fonte adaptativa é feita à mão nesta versão do Compose |
