#!/usr/bin/env node
'use strict';
/**
 * MC-0 — gera o índice `magias-no-livro.json` (onde está cada magia do catálogo no livro) e o
 * relatório de cobertura. Nada aqui é digitado à mão; nenhum texto do livro é copiado para o relatório.
 *
 *   node scripts/magias/localizar-magias-no-livro.js                 # índice + relatório ao lado
 *   node scripts/magias/localizar-magias-no-livro.js --relatorio <caminho.md>
 *   node scripts/magias/localizar-magias-no-livro.js --so-resumo     # só imprime as contas
 *
 * Entradas: `app/src/main/assets/chunks.jsonl`, `app/src/main/assets/magias2versao.json` e (opcional)
 * `scripts/magias/magias-localizacao-manual.json` (só LOCALIZAÇÃO).
 */
const fs = require('fs');
const path = require('path');
const L = require('./localizador');

const RAIZ = path.resolve(__dirname, '..', '..');
const CHUNKS = path.join(RAIZ, 'app', 'src', 'main', 'assets', 'chunks.jsonl');
const CATALOGO = path.join(RAIZ, 'app', 'src', 'main', 'assets', 'magias2versao.json');
const MANUAL = path.join(__dirname, 'magias-localizacao-manual.json');
const INDICE = path.join(__dirname, 'magias-no-livro.json');

function argumento(nome) {
  const i = process.argv.indexOf(nome);
  return i >= 0 ? process.argv[i + 1] : null;
}

function porcento(n, total) {
  return (100 * n / total).toFixed(1).replace('.', ',') + '%';
}

function montarIndice() {
  const livro = L.carregarLivro(CHUNKS);
  const catalogo = JSON.parse(fs.readFileSync(CATALOGO, 'utf8'));
  const manual = L.lerLocalizacaoManual(MANUAL);
  const indice = L.construirIndice(livro, catalogo, { manual });
  return { livro, catalogo, manual, indice };
}

/** Linhas de uma tabela markdown. */
function tabelaMd(cabecalho, linhas) {
  if (!linhas.length) return ['_nenhuma_', ''];
  return ['| ' + cabecalho.join(' | ') + ' |', '|' + cabecalho.map(() => '---').join('|') + '|']
    .concat(linhas.map((l) => '| ' + l.join(' | ') + ' |')).concat(['']);
}

/** Páginas com magia e bem menos texto que o normal (< 30% da mediana): a extração pode estar incompleta. */
function paginasCurtas(livro, catalogo, indice) {
  const tamanho = (n) => livro.paginas.get(n).join('').length;
  const tamanhos = [...livro.paginas.keys()].filter((n) => n >= 8 && n <= 222).map(tamanho).sort((a, b) => a - b);
  const mediana = tamanhos[Math.floor(tamanhos.length / 2)];
  const por = {};
  catalogo.forEach((m) => {
    const t = indice.magias[m.id].tabela;
    const p = t && t.pagina ? t.pagina : m.pagina;
    if (p && livro.paginas.has(p)) (por[p] = por[p] || []).push(m.nome);
  });
  return Object.keys(por).map(Number).sort((a, b) => a - b)
    .filter((p) => tamanho(p) < 0.3 * mediana)
    .map((p) => ({ pagina: p, caracteres: tamanho(p), magias: por[p].length, mediana }));
}

function relatorio({ livro, catalogo, manual, indice }) {
  const r = L.resumir(indice);
  const por = (f) => catalogo.filter((m) => f(indice.magias[m.id]));
  const pg = (m) => {
    const t = indice.magias[m.id].tabela;
    return t && t.pagina ? t.pagina : (m.pagina || '—');
  };
  const out = [];
  out.push('# MC-0 — Onde está cada magia no livro');
  out.push('');
  out.push('Gerado por `scripts/magias/localizar-magias-no-livro.js` (no app) a partir do `chunks.jsonl` (`pt_magia`) e do `magias2versao.json`. **Nada aqui foi digitado à mão e nenhum texto do livro foi copiado**: só nome, página e números. Para gerar de novo: `node scripts/magias/localizar-magias-no-livro.js --relatorio <este arquivo>`.');
  out.push('');
  out.push('## 1. A cobertura');
  out.push('');
  out.push('| | Magias | % das ' + r.total + ' |');
  out.push('|---|---|---|');
  out.push('| Com a **linha da tabela-resumo** (pp.223–237) | ' + r.comTabela + ' | ' + porcento(r.comTabela, r.total) + ' |');
  out.push('| Com a **entrada** em bloco recortável (do título ao fim) | ' + r.comBloco + ' | ' + porcento(r.comBloco, r.total) + ' |');
  out.push('| Com a entrada **só por página** (página de colunas embaralhadas) | ' + r.soPagina + ' | ' + porcento(r.soPagina, r.total) + ' |');
  out.push('| **Sem entrada** no `chunks.jsonl` | ' + r.semEntrada + ' | ' + porcento(r.semEntrada, r.total) + ' |');
  out.push('| **Localizadas nas duas fontes** (tabela + entrada, bloco ou página) | ' + r.localizada + ' | ' + porcento(r.localizada, r.total) + ' |');
  out.push('| Localizadas nas duas com **bloco recortável** | ' + r.comBlocoETabela + ' | ' + porcento(r.comBlocoETabela, r.total) + ' |');
  out.push('| Sem tabela e sem entrada | ' + r.semNenhuma + ' | ' + porcento(r.semNenhuma, r.total) + ' |');
  out.push('');
  out.push('A tabela-resumo tem ' + indice.tabela.length + ' linhas lidas (' + indice.tabela.filter((t) => t.reparada).length
    + ' precisaram de reparo de célula; ' + indice.malFormadas.length + ' não deu para ler).');
  out.push('');
  out.push('**Como cada uma foi achada** (quanto mais para baixo, mais merece um olhar):');
  out.push('');
  const ordem = ['exato', 'base', 'generico', 'prefixo', 'prefixo-inverso', 'aproximado', 'manual', 'pagina'];
  const linhasCas = ordem.filter((k) => r.casamentoTabela[k] || r.casamentoEntrada[k])
    .map((k) => [k, r.casamentoTabela[k] || 0, r.casamentoEntrada[k] || 0]);
  tabelaMd(['Casamento', 'Tabela', 'Entrada'], linhasCas).forEach((l) => out.push(l));
  out.push('- `exato`: o nome é o mesmo (sem acento, caixa ou pontuação). `base`: a variante entre parênteses aponta para a entrada-mãe (ou o contrário). `generico`: o livro escreve uma família ("Aumentar Atributo") e o catálogo abre por atributo. `prefixo`/`prefixo-inverso`: o nome é o começo do outro ("Convocar Elemental" × "Convocar Elemental Fogo"). `aproximado`: erro de digitação, a até 2 letras. `manual`: localização dada à mão em `magias-localizacao-manual.json`. `pagina`: só a página.');
  out.push('');

  out.push('## 2. 🔴 Sem entrada no `chunks.jsonl` (' + r.semEntrada + ')');
  out.push('');
  out.push('O nome (nem ele inteiro, nem o título quebrado em linhas ou células) **não aparece** como título em nenhuma página. O livro do Rodolfo tem essas entradas; o texto extraído para o app não. Para essas, MC-2 só terá a tabela e MC-3 manterá a descrição antiga **marcada**. **Para conferir no livro dele.**');
  out.push('');
  const semEntrada = por((m) => !m.entrada);
  tabelaMd(['Magia', 'Página (tabela)', 'Página (catálogo)', 'Tem linha na tabela'],
    semEntrada.map((m) => [m.nome, pg(m), m.pagina || '—', indice.magias[m.id].tabela ? 'sim' : 'não'])).forEach((l) => out.push(l));

  const curtas = paginasCurtas(livro, catalogo, indice);
  out.push('### Páginas em que o texto extraído parece incompleto');
  out.push('');
  out.push('Têm menos de 30% do texto de uma página comum (mediana: ' + (curtas[0] ? curtas[0].mediana : '—') + ' caracteres). É onde mais magias somem da seção acima.');
  out.push('');
  tabelaMd(['Página', 'Caracteres', 'Magias do catálogo nela'], curtas.map((c) => [c.pagina, c.caracteres, c.magias])).forEach((l) => out.push(l));

  out.push('## 3. Só a página (' + r.soPagina + ')');
  out.push('');
  out.push('São páginas em que o livro foi impresso em colunas e o texto extraído embaralhou as colunas numa tabela (o texto de duas ou três magias se mistura nas mesmas linhas). Dá para dizer **em que página e linha** a magia está, mas não dá para recortar o bloco dela sem inventar. MC-3 mantém a descrição antiga **marcada** para essas.');
  out.push('');
  const soPagina = catalogo.filter((m) => indice.magias[m.id].entrada && indice.magias[m.id].entrada.formato !== 'bloco');
  tabelaMd(['Magia', 'Página', 'Linha'], soPagina.map((m) => [m.nome, indice.magias[m.id].entrada.pagina, indice.magias[m.id].entrada.linha])).forEach((l) => out.push(l));

  out.push('## 4. Achadas por casamento aproximado (conferir o nome)');
  out.push('');
  out.push('São as que o catálogo escreve de um jeito e o livro de outro. O MC-1 corrige o **nome** pelo que o livro escreve (o `id` não muda).');
  out.push('');
  const aprox = [];
  catalogo.forEach((m) => {
    const x = indice.magias[m.id];
    ['tabela', 'entrada'].forEach((fonte) => {
      const c = x[fonte];
      if (c && ['aproximado', 'prefixo', 'prefixo-inverso', 'generico', 'manual'].includes(c.casamento)) {
        aprox.push([m.nome, fonte, c.casamento, c.nome || c.titulo || '—', c.pagina || '—']);
      }
    });
  });
  tabelaMd(['Catálogo escreve', 'Fonte', 'Casamento', 'Livro escreve', 'Página'], aprox).forEach((l) => out.push(l));

  out.push('## 5. Entradas em bloco sem as linhas Duração/Custo/Tempo (' + r.blocoSemCampos + ')');
  out.push('');
  out.push('O bloco foi achado, mas não tem nenhuma linha de cabeçalho da magia: é uma remissão ("como listada em…"), uma variante que o livro descreve dentro de outra, ou a entrada vem só com o texto. Para essas, MC-2 usa só a tabela.');
  out.push('');
  const semCampos = catalogo.filter((m) => {
    const e = indice.magias[m.id].entrada;
    return e && e.formato === 'bloco' && !e.temCampos;
  });
  tabelaMd(['Magia', 'Página', 'Remete à página'],
    semCampos.map((m) => [m.nome, indice.magias[m.id].entrada.pagina, indice.magias[m.id].entrada.remissao || '—'])).forEach((l) => out.push(l));

  out.push('## 6. Localização dada à mão (' + manual.length + ')');
  out.push('');
  out.push('Só **onde**: página, linha e o nome exato. Nenhum número, nenhum texto. Arquivo: `scripts/magias/magias-localizacao-manual.json`.');
  out.push('');
  tabelaMd(['Magia', 'Página', 'Linha', 'Por quê'],
    manual.map((m) => [(catalogo.find((c) => c.id === m.id) || {}).nome || m.id, m.pagina, m.linha != null ? m.linha : '—', m.nota || ''])).forEach((l) => out.push(l));

  if (indice.manuaisNaoResolvidos.length) {
    out.push('## 7. ⚠️ Localização manual que não se resolveu');
    out.push('');
    tabelaMd(['Magia', 'Página', 'Título'], indice.manuaisNaoResolvidos.map((m) => [m.nome, m.pagina, m.titulo || '—'])).forEach((l) => out.push(l));
  }
  if (indice.malFormadas.length) {
    out.push('## 8. ⚠️ Linhas da tabela que não se leram');
    out.push('');
    tabelaMd(['Página do chunk', 'Linha', 'Células'], indice.malFormadas.map((m) => [m.paginaDoChunk, m.linha, m.celulas])).forEach((l) => out.push(l));
  }
  return out.join('\n');
}

function main() {
  const dados = montarIndice();
  const r = L.resumir(dados.indice);
  console.log('Catálogo: ' + r.total + ' magias');
  console.log('  com linha da tabela ........ ' + r.comTabela + ' (' + porcento(r.comTabela, r.total) + ')');
  console.log('  com entrada em bloco ....... ' + r.comBloco + ' (' + porcento(r.comBloco, r.total) + ')');
  console.log('  com entrada só por página .. ' + r.soPagina + ' (' + porcento(r.soPagina, r.total) + ')');
  console.log('  sem entrada no livro ....... ' + r.semEntrada + ' (' + porcento(r.semEntrada, r.total) + ')');
  console.log('  localizadas nas duas fontes  ' + r.localizada + ' (' + porcento(r.localizada, r.total) + ')');
  console.log('  nas duas, com bloco ........ ' + r.comBlocoETabela + ' (' + porcento(r.comBlocoETabela, r.total) + ')');
  if (process.argv.includes('--so-resumo')) return;

  const saida = {
    gerado: 'por scripts/magias/localizar-magias-no-livro.js, do chunks.jsonl (pt_magia) e do magias2versao.json (nunca à mão)',
    convencoes: 'linha = índice 0-based em text.split(/\\r?\\n/) da página; fim é exclusivo; pagina = page_number do chunk (a página impressa)',
    resumo: r,
    magias: dados.indice.magias,
  };
  fs.writeFileSync(INDICE, JSON.stringify(saida, null, 1) + '\n');
  console.log('Índice: ' + path.relative(RAIZ, INDICE));

  const rel = argumento('--relatorio') || path.join(__dirname, 'COBERTURA-MC-0.md');
  fs.writeFileSync(rel, relatorio(dados) + '\n');
  console.log('Relatório: ' + rel);
}

if (require.main === module) main();

module.exports = { montarIndice, relatorio, CHUNKS, CATALOGO, MANUAL, INDICE };
