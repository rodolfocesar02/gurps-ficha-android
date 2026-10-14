'use strict';
/**
 * MC-6 — testes do que MEDE: os comparadores e o localizador. Se um deles errar, o relatório acusa
 * (ou absolve) o catálogo por engano — por isso cada caso abaixo é um erro que eu já cometi medindo.
 *   node --test scripts/catalogos/*.test.js
 */
const test = require('node:test');
const assert = require('node:assert');
const C = require('./comparar');
const L = require('./livro');
const A = require('./amostra-mc6');

test('custo: lista do catálogo cobre a faixa do livro ("-5 a -15" × "-5, -10 ou -15")', () => {
  assert.strictEqual(C.compararCusto('-5, -10 ou -15', '-5 a -15 pontos').veredito, 'ok');
  // ... mas uma lista que NÃO cobre as duas pontas é erro
  assert.strictEqual(C.compararCusto('-5, -10', '-5 a -15 pontos').veredito, 'erro');
});

test('custo: a unidade do livro ("5 pontos/boca") casa com "5/nível"; opção perdida é erro', () => {
  assert.strictEqual(C.compararCusto('5/nível', '5 pontos/boca').veredito, 'ok');
  assert.strictEqual(C.compararCusto('10', '0 ou 10 pontos').veredito, 'erro');
  assert.strictEqual(C.compararCusto('10', '10 pontos/nível').veredito, 'erro');
});

test('custo: o "-1" de "por -1 de Verificação" não é número de custo; "Variável" não se compara', () => {
  assert.strictEqual(C.compararCusto('30 + 10/nível', '30 pontos + 10 pontos por -1 de Verificação de Pânico').veredito, 'ok');
  assert.strictEqual(C.compararCusto('Variável', '-5 pontos').veredito, 'nao-comparavel');
});

test('predefinido: "Especial" não se compara; "e outros" só vale se o catálogo CONTÉM o que o livro lista', () => {
  assert.strictEqual(C.compararPredefinido('Armeiro-6', 'Especial.').veredito, 'nao-comparavel');
  assert.strictEqual(C.compararPredefinido('IQ-5 (e outros -3)', 'IQ-5 e outros.').veredito, 'ok');
  assert.strictEqual(C.compararPredefinido('IQ-5', 'IQ-5 ou Comércio-3.').veredito, 'erro');
});

test('perícia: atributo e dificuldade; "DX ou IQ/Difícil" lê os dois atributos', () => {
  assert.deepStrictEqual(C.lerCabecalhoDePericia('DX ou IQ/Difícil'), { atributos: ['DX', 'IQ'], dificuldade: 'D' });
  const r = C.compararPericia({ atributoBase: 'DX', dificuldadeFixa: 'M' }, 'DX/Fácil');
  assert.strictEqual(r.atributo.veredito, 'ok');
  assert.strictEqual(r.dificuldade.veredito, 'erro');
});

test('número no bloco: ".30" de calibre vale; "1.30" não vale por "30"', () => {
  assert.strictEqual(C.numeroNoBloco('calibre .30 e .50', 30), true);
  assert.strictEqual(C.numeroNoBloco('o valor 1.30 no total', 30), false);
  assert.strictEqual(C.numeroNoBloco('bônus de +2 na reação', 2), true);
});

test('descrição: remissão de página, marca de extração e linha de tabela ficam de fora', () => {
  const desc = 'Veja pág. 99 e págs. 100-101.\n============= PAGE 141 =============\n| 3 | 1.000× | 15 pontos |\nDá +12 de bônus.';
  assert.strictEqual(C.compararDescricao(desc, 'Dá +12 de bônus.').veredito, 'ok');
  assert.strictEqual(C.compararDescricao('Dá +12 de bônus.', 'Dá +11 de bônus.').veredito, 'erro');
});

test('localizador: custo no título, custo em cabeçalho, remissão e linha de tabela', () => {
  assert.strictEqual(L.tituloDaLinha('## Epilepsia (-30 pontos)'), 'Epilepsia');
  assert.strictEqual(L.custoNaLinha('## Epilepsia (-30 pontos)'), '-30 pontos');
  assert.strictEqual(L.tituloDaLinha('**Hermafrodita** | 5 pontos'), 'Hermafrodita');
  assert.strictEqual(L.tituloDaLinha('### 0 a 10 pontos'), null, 'linha de custo escrita como título não é título');
  assert.strictEqual(L.ehLinhaDeCusto('| 3 | 1.000× | 15 pontos |'), false, 'linha de tabela não é custo');
  assert.strictEqual(L.ehLinhaDeCusto('**-15 ou -20 pontos**'), true);
  assert.strictEqual(L.ehCabecalhoDePericia('| DX/Média |'), true);
  assert.strictEqual(L.ehCabecalhoDeTecnica('*Difícil*'), true);
});

test('amostra: semente fixa dá sempre os mesmos 40, sem repetição', () => {
  const lista = Array.from({ length: 276 }, (_, i) => ({ id: 'x' + i }));
  const a = A.sortear(lista, 40, 20261015).map((x) => x.id);
  const b = A.sortear(lista, 40, 20261015).map((x) => x.id);
  assert.deepStrictEqual(a, b);
  assert.strictEqual(new Set(a).size, 40);
});

test('sonda: estragar o custo de uma vantagem da amostra é visto pelo comparador', () => {
  // o mesmo comparador que acusa o catálogo, sobre um custo trocado de propósito (10 → 15)
  assert.strictEqual(C.compararCusto('15', '10 pontos').veredito, 'erro');
  assert.strictEqual(C.compararCusto('10', '10 pontos').veredito, 'ok');
});
