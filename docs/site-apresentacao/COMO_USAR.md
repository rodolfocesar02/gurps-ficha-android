# Site de apresentação do app

Abra **`index.html`** em qualquer navegador (duplo clique). Não precisa de servidor,
não precisa de internet.

## A ordem dos capítulos

A página segue a **ordem das abas do app**: Geral, Traços, Raças, Perícias,
Técnicas, Magia, Equipamento, Rolagem — e depois Ferimento e o Índice de regras.
Quem está com o app na mão encontra o capítulo onde espera.

⚠️ **Raças** não é uma aba: é um diálogo aberto de dentro de Traços. Fica logo
depois dela por isso.

## O que tem dentro

| arquivo | o que é |
|---|---|
| `index.html` | a página inteira — HTML, CSS e JavaScript num arquivo só |
| `img/` | as capturas do app, tiradas do emulador na versão **9.8** |

Nada aqui é usado pelo aplicativo. É uma pasta de apresentação; pode ser copiada
para qualquer lugar desde que `index.html` e `img/` fiquem juntos.

## Como a página funciona

Cada tela do app aparece com **pinos numerados** por cima. Clicar num pino
destaca a explicação ao lado; clicar na explicação destaca o pino. A cor diz o
tipo de coisa:

| cor | significa |
|---|---|
| aço | botão evidente |
| ouro | **é clicável, mas não parece** |
| sangue | **gesto invisível** (arrastar o dedo) |
| verdete | calculado sozinho pelo app |

O ouro e o sangue são o motivo de a página existir: são as coisas que um
jogador novo não descobre sozinho.

## Para atualizar as capturas

Com o emulador aberto e o app instalado:

```
adb exec-out screencap -p > docs/site-apresentacao/img/NOME.png
```

⚠️ As telas foram tiradas com a **resolução 1080x2424**. Os pinos são posicionados
em **porcentagem**, então trocar a resolução não os desalinha — mas mudar o
*layout* da tela sim. Depois de mexer numa tela, confira os pinos daquela imagem.

## Sobre os livros

As regras e as páginas citadas na página foram conferidas contra o texto do
Módulo Básico que já vive dentro do app (`chunks.jsonl`). **Nenhuma imagem,
tabela ou trecho longo dos PDFs foi copiado para cá** — a página cita a página do
livro e mostra a tela do app, que é o que ela existe para apresentar.

## O que está guardado para depois

`img/_futuro/` tem capturas que **não entram na página ainda**, por decisão do
usuário: são recursos que valem mais quando estiverem em uso de verdade, com
histórias reais para mostrar.

| arquivo | do quê é |
|---|---|
| `09-mestre-ia.png` | o **Mestre de Regras** — a IA que responde citando a página do livro |

Também ficam fora da página, mas soltas em `img/`, três capturas que já serviram
e podem voltar: `00-menu.png`, `00b-carregar.png` e `05-magia.png` (a aba de
magias da ficha, substituída pelas telas do catálogo).

⚠️ A **Saga** — a campanha conduzida por IA, com combate tático em hexágonos —
não tem captura aqui. Quando ela entrar, é o capítulo que mais precisa de telas
de uma partida real, não de uma tela de configuração vazia.

## O que está guardado para depois

`img/_futuro/` tem capturas que **não entram na página ainda**, por decisão do
usuário: são recursos que valem mais quando estiverem em uso de verdade, com
histórias reais para mostrar.

| arquivo | do quê é |
|---|---|
| `09-mestre-ia.png` | o **Mestre de Regras** — a IA que responde citando a página do livro |

Também ficam fora da página, mas soltas em `img/`, três capturas que já serviram
e podem voltar: `00-menu.png`, `00b-carregar.png` e `05-magia.png` (a aba de
magias da ficha, substituída pelas telas do catálogo).

⚠️ A **Saga** — a campanha conduzida por IA, com combate tático em hexágonos —
não tem captura aqui. Quando ela entrar, é o capítulo que mais precisa de telas
de uma partida real, não de uma tela de configuração vazia.
