#!/usr/bin/env node
'use strict';
/**
 * MC-3 (opção A) — a `descricao` das magias vira o bloco da ENTRADA do livro, recortado por programa.
 *
 *   node scripts/magias/aplicar-descricoes.js --simular
 *   node scripts/magias/aplicar-descricoes.js --relatorio <caminho.md>
 *
 * - Magia com bloco recortável: `descricao` = o recorte; `descricaoConferida: true`,
 *   `descricaoPagina`, e `descricaoTitulo` quando o título do livro é outro (variante -> entrada-mãe).
 * - Sem bloco (só página, ausente do texto, remissão, colunas misturadas): fica a descrição antiga,
 *   `descricaoConferida: false` e `descricaoMotivo`. Nada é reescrito.
 * O "antes" do relatório é o catálogo do `git HEAD` (ou `--antes <arquivo>`).
 */
const fs = require('fs');
const path = require('path');
const { execFileSync } = require('child_process');
const L = require('./localizador');
const D = require('./descricao');

const RAIZ = path.resolve(__dirname, '..', '..');
const CHUNKS = path.join(RAIZ, 'app', 'src', 'main', 'assets', 'chunks.jsonl');
const CATALOGO = path.join(RAIZ, 'app', 'src', 'main', 'assets', 'magias2versao.json');
const MANUAL = path.join(__dirname, 'magias-localizacao-manual.json');

function argumento(nome) {
  const i = process.argv.indexOf(nome);
  return i >= 0 ? process.argv[i + 1] : null;
}

function textoDoCatalogo(arr) {
  return (JSON.stringify(arr, null, 2) + '\n').replace(/\n/g, '\r\n');
}

function catalogoDeAntes() {
  const arq = argumento('--antes');
  if (arq) return JSON.parse(fs.readFileSync(arq, 'utf8'));
  try {
    return JSON.parse(execFileSync('git', ['show', 'HEAD:app/src/main/assets/magias2versao.json'], { cwd: RAIZ, maxBuffer: 64 * 1024 * 1024 }).toString('utf8'));
  } catch (e) {
    return JSON.parse(fs.readFileSync(CATALOGO, 'utf8'));
  }
}

/** Decide o que cada magia ganha. */
function calcular() {
  const livro = L.carregarLivro(CHUNKS);
  const catalogo = JSON.parse(fs.readFileSync(CATALOGO, 'utf8'));
  const manual = L.lerLocalizacaoManual(MANUAL);
  const indice = L.construirIndice(livro, catalogo, { manual });
  const resultado = catalogo.map((m) => {
    const x = indice.magias[m.id];
    const e = x.entrada;
    let corte = null;
    let motivo = null;
    if (!e) motivo = 'sem-entrada-no-texto';
    else if (e.formato !== 'bloco') motivo = 'so-pagina';
    else if (e.remissao) motivo = 'remissao';
    else {
      corte = D.recortarDescricao(livro, e);
      if (!corte) motivo = 'bloco-sem-corpo';
      else if (corte.suspeita === 'colunas') { motivo = 'colunas-misturadas'; corte = null; }
      else if (corte.texto.length < 60) { motivo = 'curta-demais'; corte = null; }
    }
    const tituloDiferente = e && e.titulo && L.normalizar(e.titulo) !== L.normalizar(m.nome) ? e.titulo : null;
    return { m, x, e, corte, motivo, tituloDiferente };
  });
  return { livro, catalogo, indice, resultado };
}

/** Aplica sobre uma cópia, com os campos novos logo depois de `descricao`. */
function aplicar(calc) {
  return calc.resultado.map((r) => {
    const novo = {};
    Object.keys(r.m).forEach((k) => {
      if (['descricaoConferida', 'descricaoPagina', 'descricaoTitulo', 'descricaoMotivo'].includes(k)) return;
      novo[k] = r.m[k];
      if (k === 'descricao') {
        if (r.corte) {
          novo.descricao = r.corte.texto;
          novo.descricaoConferida = true;
          novo.descricaoPagina = r.corte.paginaInicio;
          if (r.tituloDiferente) novo.descricaoTitulo = r.tituloDiferente;
        } else {
          novo.descricaoConferida = false;
          novo.descricaoMotivo = r.motivo;
        }
      }
    });
    return novo;
  });
}

// ---------------------------------------------------------------------------------- comparação

const palavras = (s) => new Set(L.normalizar(s).split(' ').filter((w) => w.length > 2));
function jaccard(a, b) {
  if (!a.size || !b.size) return 0;
  let inter = 0;
  a.forEach((w) => { if (b.has(w)) inter++; });
  return inter / (a.size + b.size - inter);
}
const numeros = (s) => (String(s).match(/\d+(?:[.,]\d+)?/g) || []).sort().join(',');

/**
 * Classifica a descrição ANTIGA contra o recorte do livro:
 * `igual` | `contida` (a antiga é um pedaço do livro) | `numeros` (algum número diferente) |
 * `texto` (mesmos números, outras palavras) | `outra-magia` (parece o bloco de OUTRA magia).
 */
function compararComAntiga(calc, antes) {
  const antesPorId = new Map(antes.map((m) => [m.id, m]));
  const comCorte = calc.resultado.filter((r) => r.corte);
  const conj = new Map(comCorte.map((r) => [r.m.id, palavras(r.corte.texto)]));
  const lista = [];
  const cont = { igual: 0, contida: 0, numeros: 0, texto: 0, 'outra-magia': 0 };
  comCorte.forEach((r) => {
    const velha = (antesPorId.get(r.m.id) || {}).descricao || '';
    const a = L.normalizar(velha);
    const b = L.normalizar(r.corte.texto);
    let classe;
    let outra = null;
    if (a === b) classe = 'igual';
    else {
      const propria = jaccard(palavras(velha), conj.get(r.m.id));
      let melhor = 0;
      let melhorR = null;
      if (propria < 0.35) {
        comCorte.forEach((o) => {
          if (o.m.id === r.m.id) return;
          const s = jaccard(palavras(velha), conj.get(o.m.id));
          if (s > melhor) { melhor = s; melhorR = o; }
        });
      }
      if (propria < 0.35 && melhor >= 0.5 && melhor > propria + 0.15) { classe = 'outra-magia'; outra = melhorR.m.nome; }
      else if (a && b.includes(a)) classe = 'contida';
      else if (numeros(velha) !== numeros(r.corte.texto)) classe = 'numeros';
      else classe = 'texto';
    }
    cont[classe]++;
    lista.push({ r, classe, outra, velha });
  });
  return { cont, lista };
}

// ---------------------------------------------------------------------------------- relatório

function md(cabecalho, linhas) {
  if (!linhas.length) return ['_nenhuma_', ''];
  const esc = (v) => String(v == null ? '—' : v).replace(/\|/g, '\\|').replace(/\n/g, ' ');
  return ['| ' + cabecalho.join(' | ') + ' |', '|' + cabecalho.map(() => '---').join('|') + '|']
    .concat(linhas.map((l) => '| ' + l.map(esc).join(' | ') + ' |')).concat(['']);
}

function relatorio(calc, antes) {
  const out = [];
  const comp = compararComAntiga(calc, antes);
  const total = calc.catalogo.length;
  const conferidas = calc.resultado.filter((r) => r.corte);
  const naoConferidas = calc.resultado.filter((r) => !r.corte);
  const pag = (r) => (r.x.tabela && r.x.tabela.pagina) || r.m.pagina || '—';
  const porMotivo = {};
  naoConferidas.forEach((r) => { porMotivo[r.motivo] = (porMotivo[r.motivo] || 0) + 1; });

  out.push('# MC-3 — As descrições, recortadas do livro (opção A)');
  out.push('');
  out.push('Gerado por `scripts/magias/aplicar-descricoes.js` (no app). A `descricao` passou a ser o **bloco da entrada do livro**, recortado do `chunks.jsonl` por programa: da linha de classe até o fim do bloco (o "Item" incluído), o markdown virou texto simples e os parágrafos ficaram como estão. Sem IA no meio. Só nome, página e números aparecem aqui.');
  out.push('');
  out.push('## 1. Em números');
  out.push('');
  out.push('| | Magias |');
  out.push('|---|---|');
  out.push('| No catálogo | ' + total + ' |');
  out.push('| **Descrição do livro** (`descricaoConferida: true`) | ' + conferidas.length + ' |');
  out.push('| Descrição antiga **mantida e marcada** (`descricaoConferida: false`) | ' + naoConferidas.length + ' |');
  Object.entries(porMotivo).sort((a, b) => b[1] - a[1]).forEach(([k, v]) => out.push('| &nbsp;&nbsp;↳ ' + k + ' | ' + v + ' |'));
  out.push('| Variantes que receberam a entrada-mãe (`descricaoTitulo`) | ' + conferidas.filter((r) => r.tituloDiferente).length + ' |');
  out.push('');
  out.push('## 2. 🔴 O quanto a transcrição por IA errou');
  out.push('');
  out.push('Das ' + conferidas.length + ' magias com bloco, a descrição antiga comparada com o que o livro escreve:');
  out.push('');
  const c = comp.cont;
  md(['Situação', 'Magias', '%'], [
    ['**Igual** ao livro', c.igual, (100 * c.igual / conferidas.length).toFixed(1) + '%'],
    ['**Contida** no livro (a antiga é só um pedaço do bloco)', c.contida, (100 * c.contida / conferidas.length).toFixed(1) + '%'],
    ['**Algum número diferente** do livro', c.numeros, (100 * c.numeros / conferidas.length).toFixed(1) + '%'],
    ['Mesmos números, **outras palavras**', c.texto, (100 * c.texto / conferidas.length).toFixed(1) + '%'],
    ['**Era de outra magia** (texto parecido com o bloco de uma vizinha)', c['outra-magia'], (100 * c['outra-magia'] / conferidas.length).toFixed(1) + '%'],
  ]).forEach((l) => out.push(l));
  out.push('"Igual" é raro de propósito: a descrição antiga não trazia o título nem a formatação do livro. O que importa é a diferença de **número** e o texto de **outra magia**.');
  out.push('');

  out.push('## 3. Descrição com número diferente do livro (' + c.numeros + ')');
  out.push('');
  md(['Magia', 'Página'], comp.lista.filter((x) => x.classe === 'numeros').map((x) => [x.r.m.nome, pag(x.r)])).forEach((l) => out.push(l));

  out.push('## 4. 🔴 Descrição que era de OUTRA magia (' + c['outra-magia'] + ')');
  out.push('');
  md(['Magia', 'Página', 'A antiga parecia a de'], comp.lista.filter((x) => x.classe === 'outra-magia').map((x) => [x.r.m.nome, pag(x.r), x.outra])).forEach((l) => out.push(l));

  out.push('## 5. Para o Rodolfo conferir no livro: a descrição antiga ficou, marcada (' + naoConferidas.length + ')');
  out.push('');
  out.push('Não deu para recortar o bloco por programa. `so-pagina`: página de colunas embaralhadas. `sem-entrada-no-texto`: o `chunks.jsonl` não traz a entrada. `remissao`: o bloco só remete a outra página. `colunas-misturadas`: o recorte pegaria texto de magias vizinhas. `curta-demais`/`bloco-sem-corpo`: o bloco não tem corpo.');
  out.push('');
  md(['Magia', 'Página', 'Motivo'], naoConferidas.sort((a, b) => a.m.nome.localeCompare(b.m.nome, 'pt')).map((r) => [r.m.nome, pag(r), r.motivo])).forEach((l) => out.push(l));

  out.push('## 6. Variantes: a descrição é a da entrada-mãe');
  out.push('');
  md(['Magia', 'Página', 'Título no livro'], conferidas.filter((r) => r.tituloDiferente).map((r) => [r.m.nome, pag(r), r.tituloDiferente])).forEach((l) => out.push(l));
  return { texto: out.join('\n'), comp, conferidas: conferidas.length, naoConferidas: naoConferidas.length, porMotivo };
}

function main() {
  const antes = catalogoDeAntes();
  const calc = calcular();
  const rel = relatorio(calc, antes);
  console.log('Catálogo: ' + calc.catalogo.length + ' magias');
  console.log('  descrição do livro: ' + rel.conferidas + ' | antiga mantida e marcada: ' + rel.naoConferidas + ' ' + JSON.stringify(rel.porMotivo));
  console.log('  antiga × livro: ' + JSON.stringify(rel.comp.cont));
  if (process.argv.includes('--simular')) return;
  fs.writeFileSync(CATALOGO, textoDoCatalogo(aplicar(calc)));
  const relPath = argumento('--relatorio');
  if (relPath) fs.writeFileSync(relPath, rel.texto + '\n');
  console.log('Catálogo gravado.' + (relPath ? ' Relatório: ' + relPath : ''));
}

if (require.main === module) main();

module.exports = { calcular, aplicar, relatorio, compararComAntiga, textoDoCatalogo, catalogoDeAntes };
