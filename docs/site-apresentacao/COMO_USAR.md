# Site de apresentação do app

Abra **`index.html`** em qualquer navegador (duplo clique). Não precisa de servidor,
não precisa de internet.

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
