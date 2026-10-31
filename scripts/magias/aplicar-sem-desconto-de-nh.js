#!/usr/bin/env node
'use strict';
/**
 * Passo 7 da cadeia do catálogo de magias — as magias em que um NH elevado NÃO reduz o custo (P-5b, Magia pp.37, 89, 127, 128, 180).
 *
 *   node scripts/magias/aplicar-sem-desconto-de-nh.js --simular
 *   node scripts/magias/aplicar-sem-desconto-de-nh.js
 *
 * `sem-desconto-de-nh.json` diz QUEM (o id, a página e a frase do livro). Aqui a lista é conferida contra o PDF
 * (`custos-do-pdf.json`, que `extrair-custos-do-pdf.py` lê direto do PDF de 2025) e gravada no catálogo como
 * `semDescontoDeNh: {pagina}`; onde a magia saiu da lista, o campo sai do catálogo. Idempotente.
 *
 * A guarda tem duas pontas: (1) cada magia da lista tem a frase DELA no PDF, na página dita; (2) toda frase do PDF que fala do NH
 * e do custo está classificada (ou isenta uma magia, ou está em `naoSeAplica` com o porquê). Frase nova no livro, sem dono, é erro.
 */
const fs = require('fs');
const path = require('path');
const C = require('./custo-condicao');

const RAIZ = path.resolve(__dirname, '..', '..');
const CATALOGO = path.join(RAIZ, 'app', 'src', 'main', 'assets', 'magias2versao.json');
const LISTA = path.join(__dirname, 'sem-desconto-de-nh.json');

/** A lista (`{magias, naoSeAplica}`), sem as chaves de comentário. */
function lerALista(arquivo) {
  const bruto = JSON.parse(fs.readFileSync(arquivo || LISTA, 'utf8'));
  return { magias: bruto.magias || {}, naoSeAplica: bruto.naoSeAplica || [] };
}

const normalizar = (s) => String(s || '').replace(/\s+/g, ' ').trim();

/** Os problemas da lista contra o catálogo e contra as frases do PDF (vazio = ok). */
function problemasDaLista(lista, catalogo, frasesDoPdf) {
  const p = [];
  const ids = new Set(catalogo.map((m) => m.id));
  const frases = (frasesDoPdf || []).map((f) => ({ pagina: f.pagina, frase: normalizar(f.frase) }));
  if (!frases.length) p.push('sem as frases do PDF (rode extrair-custos-do-pdf.py)');
  Object.keys(lista.magias).forEach((id) => {
    const x = lista.magias[id];
    if (!ids.has(id)) { p.push(id + ': não existe no catálogo'); return; }
    if (!Number.isInteger(x.pagina) || !x.frase) { p.push(id + ': sem página ou frase'); return; }
    if (frases.length && !frases.some((f) => f.pagina === x.pagina && f.frase.indexOf(normalizar(x.frase)) >= 0)) {
      p.push(id + ': a frase «' + x.frase + '» não está na p.' + x.pagina + ' do PDF');
    }
  });
  frases.forEach((f) => {
    const dono = Object.keys(lista.magias).some((id) => lista.magias[id].pagina === f.pagina && f.frase.indexOf(normalizar(lista.magias[id].frase)) >= 0);
    const naoSeAplica = lista.naoSeAplica.some((n) => n.pagina === f.pagina && f.frase.indexOf(normalizar(n.inicio)) >= 0 && n.motivo);
    if (!dono && !naoSeAplica) p.push('p.' + f.pagina + ': a frase «' + f.frase.slice(0, 100) + '» fala do NH e do custo e ninguém a classificou');
  });
  return p;
}

function aplicar(catalogo, lista) {
  return catalogo.map((m) => {
    const x = lista.magias[m.id];
    if (!x) {
      if (m.semDescontoDeNh === undefined) return m;
      const c = JSON.parse(JSON.stringify(m)); delete c.semDescontoDeNh; return c;
    }
    const c = JSON.parse(JSON.stringify(m));
    c.semDescontoDeNh = { pagina: x.pagina };
    return c;
  });
}

function main() {
  const catalogo = JSON.parse(fs.readFileSync(CATALOGO, 'utf8'));
  const lista = lerALista();
  const problemas = problemasDaLista(lista, catalogo, C.frasesDeNhDoPdf());
  if (problemas.length) {
    console.error('Problemas (nada foi gravado):\n - ' + problemas.join('\n - '));
    process.exit(1);
  }
  console.log('Magias em que o NH não reduz o custo: ' + Object.keys(lista.magias).length + ' (antes: ' + catalogo.filter((m) => m.semDescontoDeNh).length + ')');
  if (process.argv.includes('--simular')) return;
  fs.writeFileSync(CATALOGO, (JSON.stringify(aplicar(catalogo, lista), null, 2) + '\n').replace(/\n/g, '\r\n'));
  console.log('Catálogo gravado.');
}

if (require.main === module) main();

module.exports = { LISTA, lerALista, problemasDaLista, aplicar };
