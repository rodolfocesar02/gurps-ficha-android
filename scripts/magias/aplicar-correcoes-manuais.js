#!/usr/bin/env node
'use strict';
/**
 * Passo 8 da cadeia do catálogo de magias — as CORREÇÕES MANUAIS (P-9, 02/nov/2026).
 *
 *   node scripts/magias/aplicar-correcoes-manuais.js --simular
 *   node scripts/magias/aplicar-correcoes-manuais.js
 *
 * Rode DEPOIS de todos os outros (passos 1 a 7): eles regeneram `descricao`, `custoManter`, `tempoOperacaoTela`... e **desfariam** uma correção
 * gravada à mão. As correções moram em `correcoes-manuais.json` (cada uma com o adendo, a página e o porquê). É idempotente: o que já está certo é só avisado.
 * Uma correção que o catálogo já traz sem ela (a página foi reextraída do PDF, a cadeia passou a ler certo) é apagada do JSON.
 */
const fs = require('fs');
const path = require('path');

const RAIZ = path.resolve(__dirname, '..', '..');
const CATALOGO = path.join(RAIZ, 'app', 'src', 'main', 'assets', 'magias2versao.json');
const CORRECOES = path.join(__dirname, 'correcoes-manuais.json');

/** Aplica `correcoes` sobre uma CÓPIA da lista. @returns `{lista, aplicadas, jaCertas, semMagia}` */
function aplicar(lista, correcoes) {
  const copia = JSON.parse(JSON.stringify(lista));
  const porId = {};
  copia.forEach((m) => { porId[m.id] = m; });
  const aplicadas = [];
  const jaCertas = [];
  const semMagia = [];
  correcoes.forEach((c) => {
    const rotulo = c.adendo + ' ' + c.id + ' · ' + c.campo;
    const m = porId[c.id];
    if (!m) { semMagia.push(rotulo); return; }
    const caminho = c.campo.split('.');
    const dono = caminho.length === 2 ? (m[caminho[0]] = m[caminho[0]] || {}) : m;
    const chave = caminho[caminho.length - 1];
    const valor = dono[chave];
    if (c.de === null || c.de === undefined) {
      if (valor === c.para) { jaCertas.push(rotulo); return; }
      dono[chave] = c.para;
    } else if (typeof valor === 'string' && valor.indexOf(c.de) >= 0) {
      dono[chave] = valor.replace(c.de, c.para);
    } else {
      jaCertas.push(rotulo);
      return;
    }
    aplicadas.push(rotulo);
  });
  return { lista: copia, aplicadas, jaCertas, semMagia };
}

function main() {
  const lista = JSON.parse(fs.readFileSync(CATALOGO, 'utf8'));
  const { correcoes } = JSON.parse(fs.readFileSync(CORRECOES, 'utf8'));
  const r = aplicar(lista, correcoes);
  console.log('Correções manuais: ' + correcoes.length + ' · aplicadas ' + r.aplicadas.length + ' · já certas ' + r.jaCertas.length + ' · magia ausente ' + r.semMagia.length);
  r.aplicadas.forEach((x) => console.log('  aplicada: ' + x));
  if (r.semMagia.length) { console.error('Magia que não está no catálogo: ' + r.semMagia.join(' | ')); process.exit(1); }
  if (process.argv.includes('--simular') || !r.aplicadas.length) return;
  fs.writeFileSync(CATALOGO, (JSON.stringify(r.lista, null, 2) + '\n').replace(/\n/g, '\r\n'));
  console.log('Catálogo gravado.');
}

if (require.main === module) main();

module.exports = { aplicar, CATALOGO, CORRECOES };
