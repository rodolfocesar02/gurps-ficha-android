# Catálogo de magias × livro — como não deixar ele se afastar de novo

`app/src/main/assets/magias2versao.json` (879 magias) foi refeito **do livro** nos lotes MC-0..MC-5
(14/out/2026). Regra de ouro: **ninguém redigita o livro.** Todo número e todo texto que entra no catálogo
sai do `chunks.jsonl` (`source_id: "pt_magia"`) por script. Quem mexe pode LOCALIZAR (página, linha); não
transcreve.

## O que cada script faz (rodar nesta ordem; cada um é idempotente)

| Ordem | Script | Escreve no catálogo | Relatório (Mesa) |
|---|---|---|---|
| 0 | `localizar-magias-no-livro.js` | nada (gera `magias-no-livro.json`) | `COBERTURA-MC-0.md` |
| 1 | `aplicar-cabecalho-da-tabela.js` | classe, escola, duração, energia, tempo, página | `RELATORIO-MC-1.md` |
| 2 | `aplicar-numeros-conferidos.js` | `custoOperar/Manter`, `duracaoSeg/Tipo`, `tempoOperacaoSeg` + campos de fórmula | `RELATORIO-MC-2.md` |
| 3 | `aplicar-descricoes.js` | `descricao` = bloco da entrada do livro | `RELATORIO-MC-3.md` |
| 4 | `aplicar-mecanica.js` | `mecanica.entrega`, projétil, marcas | `RELATORIO-MC-4.md` |

Relatórios ficam em `mesa-virtual/planos/magias/`. As lacunas de leitura (página embaralhada, entrada sem
texto) viram `*Motivo` no próprio catálogo — nunca um número inventado.

## O que NÃO se corrige para "ficar igual à prosa do livro"

Os **pré-requisitos** e regras como a do *Transformar Outro* são **adaptações de propósito** para o motor
de busca de magia do app (cadeia de pré-requisitos). A regra do livro não mudou; acrescentaram-se as formas
que ela já permite para a cadeia funcionar. Guardas: `MagicCatalogCabecalhoDoLivroTest` (lacuna conhecida).

## As guardas (qualquer afastamento do livro fica vermelho, com NOME e PÁGINA)

- **Kotlin** (rodam no gate do Gradle, relêem o livro por outro caminho, com os parsers do próprio app):
  `MagicCatalogCabecalhoDoLivroTest`, `MagicCatalogNumerosDoLivroTest`, `MagicCatalogDescricaoDoLivroTest`,
  `MagicCatalogMecanicaDoLivroTest`, `CicloDaMagiaTest` (o ciclo de 10 magias que a Mesa repete).
- **JS** (não fazem parte do Gradle — rode à mão antes de mexer em script ou catálogo):

  ```bash
  node --test scripts/magias/*.test.js
  ```

- **Mesa** (`mesa-virtual/server`): `test/pedra-de-toque.test.js` (app = Mesa nos 41 casos do ciclo),
  `test/magia-leitor.test.js` (o `magias-ciclo.json` não envelhece) e `test/magia-custo-do-livro.test.js`.

Depois de mexer no catálogo: `node scripts/extrair-magias-no-alvo.js` e
`node scripts/extrair-pedra-de-toque.js` (em `mesa-virtual/server`), rodar a suíte da Mesa e só então
`railway up`.

## Para o próximo catálogo que for refeito do livro

1. Localizador primeiro (onde está cada entrada); 2. cabeçalho da tabela; 3. números por fórmula (fórmula
**nunca** vira número); 4. texto recortado por script; 5. guarda que releia o livro por outro caminho;
6. sonda: estrague o catálogo de propósito e veja a guarda ficar vermelha, depois restaure com MD5.
