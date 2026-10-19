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
| 2 | `aplicar-numeros-conferidos.js` | `custoOperar/Manter`, `duracaoSeg/Tipo`, `tempoOperacaoSeg` + campos de fórmula + `energiaTela/duracaoTela/tempoOperacaoTela` | `RELATORIO-MC-2.md` |
| 3 | `aplicar-descricoes.js` | `descricao` = bloco da entrada do livro | `RELATORIO-MC-3.md` |
| 4 | `aplicar-mecanica.js` | `mecanica.entrega`, projétil, marcas | `RELATORIO-MC-4.md` |
| 5 (só se uma página do `chunks.jsonl` estiver ruim) | `pdf-para-chunks.py` | só o `text` das páginas pedidas, em `chunks.jsonl` | `RELATORIO-MC-7.md` |
| 6 (depois da 2, e de novo a cada vez que a 2 rodar) | `aplicar-custo-por-condicao.js` | `custoPorCondicao` + `custoTipo`/`custoFormula` das magias de `custo-por-condicao.json`; apaga o número-base errado que a 2 lia da 1ª frase | `CUSTO-NA-HORA-DA-CONJURACAO.md` |

**Páginas ruins no `chunks.jsonl`** (cortadas ou com colunas embaralhadas): o PDF de Magia tem camada de texto. 
`python -I scripts/magias/pdf-para-chunks.py --pdf "<PDF>" --paginas 23,33 [--mostrar] [--aplicar]` lê a página do PDF 
(ordem das colunas, parágrafos, tabelas), mostra o que mudaria (números que saem e entram) e, com `--aplicar`, troca só 
essas linhas do `chunks.jsonl`. Depois rode a cadeia **0, 1, 2, 3, 4** (a 1 também: ela refresca o cabeçalho e as 
exceções) e suba `CODEX_VERSION_CURRENT`. ⚠️ `mecanica-lida-na-pagina.json` guarda posições de LINHA de páginas que 
foram embaralhadas: se reextrair uma delas, reaponte as linhas (e confira o `mecanica.test.js`). Número que SAI 
da página depois de reextrair é sinal de erro do conversor: o ensaio mostra.
O conversor já sabe: colunas pelas linhas longas, tabelas só com 2+ linhas alinhadas, historinha de abertura de capítulo 
(14+ linhas coloridas sem rótulo) fora. **Confira SEMPRE o ensaio de uma página nova** (`--mostrar`): quadros de texto que 
cortam as colunas (p.66) entram no meio da entrada de cima, e páginas de abertura de capítulo são as mais frágeis.

**As duas opções do conversor** (`--fundir-bordas` e `--quadro-largo`) existem porque nenhuma regra de layout serve a todas as
páginas: ligadas por padrão mudavam 46 e 10 páginas já boas, algumas para pior (p.95, p.152, p.165, p.218). Só vale ligar quando o
ensaio (`--mostrar`) mostra o defeito: `--fundir-bordas` conserta a p.142 (abertura de capítulo com margens a menos de 40 pt) e
`--quadro-largo` a p.112 (o Lampejo, um quadro de 2 colunas embaixo das duas primeiras). **Confira SEMPRE as linhas de custo**: todo
"Custo:" do PDF tem que aparecer no texto convertido, e nenhum número do texto antigo pode sumir. A **p.152** (três modelos de
mortos-vivos no meio da página) o conversor embaralha: ficou o texto antigo.

Relatórios ficam em `mesa-virtual/planos/magias/`. As lacunas de leitura (página embaralhada, entrada sem
texto) viram `*Motivo` no próprio catálogo — nunca um número inventado.

## A tela mostra o número que é cobrado (`*Tela`)

O cabeçalho (`energia`, `duracao`, `tempoOperacao`) é a linha da **tabela-resumo** e fica como está. Onde o número
conferido (vale a entrada) discorda dele e o cabeçalho diz um número, o script grava o texto do número certo em
`energiaTela`, `duracaoTela`, `tempoOperacaoTela` ("1 a 10", "6/3", "1 hora"). O app troca o cabeçalho por ele ao
carregar o catálogo (`CicloDaMagia.comTextoDaTela`) e a Mesa ao ler a ficha (`magia-na-tela.js`): tela e conta leem o
mesmo número. Cabeçalho "Varia" não é reescrito (decisão do Rodolfo, `PONTAS-ANTES-DO-MG-1.md`). Guardas:
`numeros.test.js` (seção 3), `TextoDaTelaDoLivroTest`, `test/magia-na-tela.test.js` na Mesa.

## O custo que DEPENDE DA CONDIÇÃO (passo 6)

Onde o livro escreve uma CONTA ("1 por 0,5 kg de peso original, por duplicação da dimensão"), o passo 2 lia o primeiro número da frase
e gravava um custo FIXO errado (Alongar Objeto 3/2, Animar Objeto 1/1, Teia de Aranha 1...). `custo-por-condicao.json` guarda a
ESTRUTURA da conta de cada magia (o que o jogador informa e como sai o PF); o passo 6 a grava no catálogo (`custoPorCondicao`) e
apaga o número-base errado. Guardas: `custo-por-condicao.test.js` (todo número da conta, fora 0 a 4, tem que estar no texto de custo
do livro; sonda), `CustoPorCondicaoTest` (Kotlin) e `test/custo-por-condicao.test.js` (Mesa), os três com os 73 casos de
`app/src/test/resources/custo_por_condicao_casos.json`. As 18 do lote 1; as outras ~260 magias de custo `porUnidade`/`porPonto`/`varia`
e as ~40 "fixas com condição" (Imunidade ao Fogo ×2/×3, Lealdade, Muralha Mágica...) entram em lotes, a mesma estrutura.

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
