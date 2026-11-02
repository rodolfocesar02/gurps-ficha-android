'use strict';
/**
 * Passo 8 — as correções manuais (P-9): a função aplica o que diz, é idempotente, e o catálogo gravado traz as 8
 * (a cadeia 1 a 7 as desfazia: este teste fica vermelho se alguém rodar a cadeia e esquecer o passo 8).
 */
const test = require('node:test');
const assert = require('node:assert');
const fs = require('node:fs');
const P8 = require('./aplicar-correcoes-manuais');

const { correcoes } = JSON.parse(fs.readFileSync(P8.CORRECOES, 'utf8'));
const catalogo = JSON.parse(fs.readFileSync(P8.CATALOGO, 'utf8'));
const porId = Object.fromEntries(catalogo.map((m) => [m.id, m]));

test('a função troca o texto, grava o valor, não mexe na lista de fora e é idempotente', () => {
  const lista = [
    { id: 'percepcao_de_vida', descricao: 'Texto. Duração: 1 minuto.\nCusto básico: 1.' },
    { id: 'sopro_congelante', mecanica: { danoPorEnergia: '1d-1', notas: 'A 1d-1 por ponto.' } },
    { id: 'criar_elemental_ar' }, { id: 'criar_elemental_agua' }, { id: 'criar_elemental_fogo' }, { id: 'criar_elemental_terra' }
  ];
  const r = P8.aplicar(lista, correcoes);
  assert.strictEqual(r.lista[0].descricao, 'Texto.\nCusto básico: 1.');
  assert.deepStrictEqual([r.lista[1].mecanica.danoPorEnergia, r.lista[1].mecanica.notas], ['1d+1', 'A 1d+1 por ponto.']);
  assert.deepStrictEqual([r.lista[1].custoManter, r.lista[1].custoManterTipo], [0, 'nao']);
  assert.strictEqual(r.lista[2].tempoOperacaoTela, 'segundos = pontos do elemental (p.28)');
  assert.strictEqual(lista[0].descricao, 'Texto. Duração: 1 minuto.\nCusto básico: 1.', 'a lista de fora não é mexida');
  assert.strictEqual(r.aplicadas.length, correcoes.length);
  // de novo: nada a aplicar, tudo "já certo"
  const de_novo = P8.aplicar(r.lista, correcoes);
  assert.deepStrictEqual(de_novo.aplicadas, []);
  assert.strictEqual(de_novo.jaCertas.length, correcoes.length);
  assert.deepStrictEqual(de_novo.lista, r.lista);
});

test('uma magia que não existe no catálogo é avisada (e não calada)', () => {
  const r = P8.aplicar([], [{ adendo: 'AX', id: 'nao_existe', campo: 'custoManter', de: null, para: 0 }]);
  assert.deepStrictEqual(r.semMagia, ['AX nao_existe · custoManter']);
});

test('🔴 o catálogo gravado traz as 8 correções (a cadeia 1 a 7 as desfaz; o passo 8 as devolve)', () => {
  const r = P8.aplicar(catalogo, correcoes);
  assert.deepStrictEqual(r.aplicadas, [], 'rode: node scripts/magias/aplicar-correcoes-manuais.js — faltou: ' + r.aplicadas.join(' | '));
  assert.deepStrictEqual(r.semMagia, []);
  assert.doesNotMatch(porId.percepcao_de_vida.descricao, /Duração: 1 minuto/);
  assert.strictEqual(porId.sopro_congelante.mecanica.danoPorEnergia, '1d+1');
  assert.deepStrictEqual([porId.sopro_congelante.custoManter, porId.sopro_congelante.custoManterTipo], [0, 'nao']);
  ['ar', 'agua', 'fogo', 'terra'].forEach((e) => assert.strictEqual(porId['criar_elemental_' + e].tempoOperacaoTela, 'segundos = pontos do elemental (p.28)', e));
});
