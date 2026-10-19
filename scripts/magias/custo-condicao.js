'use strict';
/**
 * O custo que DEPENDE DA CONDIÇÃO — a leitura e as guardas de `custo-por-condicao.json`.
 *
 * O JSON guarda a ESTRUTURA da conta de cada magia (o que o jogador informa e como sai o PF); o texto do livro
 * continua vindo do `chunks.jsonl` por script. Este módulo confere uma coisa contra a outra: todo número da conta
 * (fora 0 a 4, que são os "dobre/triplique" e os "mais 1") tem que aparecer no texto de custo da entrada do livro.
 * Quem digita 0,25 onde o livro diz 0,5 fica vermelho aqui, com o nome da magia.
 *
 * A conta em si (o motor) é a da Mesa: `mesa-virtual/server/src/campo/magia-energia.js`, a mesma que o app lê em
 * Kotlin (`CustoPorCondicao.kt`).
 */
const fs = require('fs');
const path = require('path');

const ARQUIVO = path.join(__dirname, 'custo-por-condicao.json');
const MOTOR_PATH = '../../../../../mesa-virtual/server/src/campo/magia-energia.js';

const TIPOS_DA_ENTRADA = ['numero', 'inteiro', 'escolha', 'marca'];
const TIPOS_DO_CUSTO = ['porUnidade', 'varia', 'fixo'];
const OPERADORES = ['+', '-', '*', '/', 'cima', 'max', 'min'];

let motorCache = null;
/** O motor da conta (o da Mesa), ou null se a Mesa não está ao lado. */
function motor() {
  if (motorCache === null) {
    try { motorCache = require(MOTOR_PATH); } catch (e) { motorCache = false; }
  }
  return motorCache || null;
}

/** Os specs do arquivo (sem as chaves de comentário `_...`). */
function lerOsSpecs(arquivo) {
  const bruto = JSON.parse(fs.readFileSync(arquivo || ARQUIVO, 'utf8'));
  const out = {};
  Object.keys(bruto).forEach((k) => { if (k.charAt(0) !== '_') out[k] = bruto[k]; });
  return out;
}

/** Os números que um texto do livro escreve: "0,5 kg" → 0.5; "2.500" → 2500; "1/2" → 0.5 (e 1 e 2). */
function numerosDoTexto(texto) {
  const t = String(texto == null ? '' : texto).replace(/(\d{1,3})\.(\d{3})(?!\d)/g, '$1$2');
  const out = new Set();
  const rx = /\d+(?:,\d+)?/g;
  let m;
  while ((m = rx.exec(t)) !== null) out.add(Number(m[0].replace(',', '.')));
  const frac = /(\d+)\s*\/\s*(\d+)/g;
  while ((m = frac.exec(t)) !== null) out.add(Number(m[1]) / Number(m[2]));
  return out;
}

/** Os números escritos na expressão (os `"$id"` e os operadores não contam). */
function literaisDaConta(expr, saida) {
  const out = saida || [];
  if (typeof expr === 'number') out.push(expr);
  else if (Array.isArray(expr)) expr.slice(1).forEach((a) => literaisDaConta(a, out));
  return out;
}

function referenciasDaConta(expr, saida) {
  const out = saida || [];
  if (typeof expr === 'string' && expr.charAt(0) === '$') out.push(expr.slice(1));
  else if (Array.isArray(expr)) expr.slice(1).forEach((a) => referenciasDaConta(a, out));
  return out;
}

function operadoresDaConta(expr, saida) {
  const out = saida || [];
  if (Array.isArray(expr)) { out.push(expr[0]); expr.slice(1).forEach((a) => operadoresDaConta(a, out)); }
  return out;
}

/**
 * Confere um spec. Devolve a lista de problemas (vazia = ok).
 * @param texto o texto de custo da ENTRADA do livro (o do catálogo: `custoFormula`, ou o recorte da entrada)
 */
function problemasDoSpec(id, spec, texto) {
  const p = [];
  const ruim = (m) => p.push(id + ': ' + m);
  if (!spec || typeof spec !== 'object') return [id + ': não é um objeto'];
  if (TIPOS_DO_CUSTO.indexOf(spec.tipo) < 0) ruim('tipo "' + spec.tipo + '" não é um de ' + TIPOS_DO_CUSTO.join('/'));
  const entradas = Array.isArray(spec.entradas) ? spec.entradas : null;
  if (!entradas) { ruim('sem lista de entradas'); return p; }
  const ids = new Set();
  entradas.forEach((e) => {
    if (!e || typeof e.id !== 'string') { ruim('entrada sem id'); return; }
    if (ids.has(e.id)) ruim('entrada repetida: ' + e.id);
    ids.add(e.id);
    if (TIPOS_DA_ENTRADA.indexOf(e.tipo) < 0) ruim(e.id + ': tipo "' + e.tipo + '" desconhecido');
    if (!e.rotulo) ruim(e.id + ': sem rótulo');
    if (e.tipo === 'escolha') {
      const ops = Array.isArray(e.opcoes) ? e.opcoes : [];
      if (ops.length < 2) ruim(e.id + ': escolha com menos de 2 opções');
      if (!ops.some((o) => o.id === e.padrao)) ruim(e.id + ': o padrão "' + e.padrao + '" não é uma das opções');
      ops.forEach((o) => { if (typeof o.valor !== 'number' || !o.rotulo || !o.id) ruim(e.id + ': opção incompleta'); });
    } else if (e.tipo === 'numero' || e.tipo === 'inteiro') {
      if (typeof e.padrao !== 'number') ruim(e.id + ': sem padrão numérico');
      if (typeof e.min === 'number' && typeof e.padrao === 'number' && e.padrao < e.min) ruim(e.id + ': padrão abaixo do mínimo');
      if (typeof e.max === 'number' && typeof e.padrao === 'number' && e.padrao > e.max) ruim(e.id + ': padrão acima do máximo');
      if (typeof e.max !== 'number') ruim(e.id + ': sem máximo (o servidor recusa o que não tem teto)');
    }
  });
  if (spec.operar === undefined) ruim('sem conta (operar)');
  const contas = [['operar', spec.operar]];
  if (spec.manter !== undefined && ['igual', 'metade', 'nenhum'].indexOf(spec.manter) < 0) contas.push(['manter', spec.manter]);
  contas.forEach(([nome, expr]) => {
    operadoresDaConta(expr).forEach((o) => { if (OPERADORES.indexOf(o) < 0) ruim(nome + ': operador "' + o + '" fora do vocabulário'); });
    referenciasDaConta(expr).forEach((r) => {
      if (!ids.has(r) && !(nome === 'manter' && r === 'operar')) ruim(nome + ': "$' + r + '" não é uma entrada');
    });
  });
  const m = motor();
  if (m && !p.length) {
    const r = m.oCustoPorCondicao(spec, m.condicoesPadrao(spec));
    if (!r.ok) ruim('a conta com os valores padrão não fecha: ' + (r.motivo || r.erro));
  }
  // o guarda do livro: número da conta (fora 0..4) tem que estar no texto de custo da entrada
  if (texto) {
    const nos = numerosDoTexto(texto);
    contas.forEach(([nome, expr]) => {
      literaisDaConta(expr).forEach((n) => {
        if (n >= 0 && n <= 4 && Math.floor(n) === n) return;
        if (!nos.has(n)) ruim(nome + ': o número ' + n + ' não aparece no texto de custo do livro («' + String(texto).slice(0, 90) + '…»)');
      });
    });
  } else {
    ruim('sem o texto de custo do livro para conferir');
  }
  return p;
}

module.exports = { ARQUIVO, motor, lerOsSpecs, numerosDoTexto, literaisDaConta, problemasDoSpec };
