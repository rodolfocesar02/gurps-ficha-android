const test = require('node:test');
const assert = require('node:assert');
const { formatRollMessage } = require('../src/format');

/**
 * **A mensagem que a mesa vê** — Lote NOTA-2.
 *
 * Primeiro teste deste projeto. Ele nasceu junto com a correção do formato da
 * anotação, e a razão é a de sempre: o defeito só apareceu **no Discord**, com
 * o jogo rolando, porque não havia nada abaixo disso que pudesse pegá-lo.
 */

const NOTA = {
  character: 'jack Eagle eye carter!!!',
  testType: 'Nota',
  context: 'Teste de Nota...',
  // 🔴 Os três que o app manda de propósito para a nota não fingir rolagem.
  dice: [],
  total: 0,
  target: null,
  margin: null,
  outcome: 'Teste de Nota\nessa msg foi feitaa PRA testar a Nota do app!'
};

test('a anotação não mostra Dados nem Resultado', () => {
  const msg = formatRollMessage(NOTA);
  assert.ok(!msg.includes('Dados:'), 'a linha de Dados voltou na anotação');
  assert.ok(!msg.includes('Resultado:'), 'a linha de Resultado voltou na anotação');
});

test('a anotação mantém o personagem, o rótulo e o texto inteiro', () => {
  const msg = formatRollMessage(NOTA);
  assert.strictEqual(
    msg,
    [
      '**jack Eagle eye carter!!!**',
      '**Nota** (Teste de Nota...)',
      'Teste de Nota',
      'essa msg foi feitaa PRA testar a Nota do app!'
    ].join('\n')
  );
});

test('anotação sem texto não quebra a mensagem', () => {
  const msg = formatRollMessage({ ...NOTA, outcome: undefined });
  assert.ok(msg.includes('**Nota**'));
  assert.ok(!msg.includes('undefined'), 'vazou um undefined para a mesa');
});

test('a rolagem comum NÃO mudou', () => {
  // ⚠️ O que mais importa aqui. A correção da nota é um caso novo; se ela tiver
  // encostado no molde de rolagem, toda mensagem da mesa muda junto.
  const msg = formatRollMessage({
    character: 'jack',
    testType: 'Perícia',
    context: 'Furtividade',
    dice: [3, 4, 5],
    total: 12,
    target: 14,
    margin: 2,
    outcome: 'sucesso'
  });
  assert.strictEqual(
    msg,
    [
      '**jack**',
      '**Perícia** (Furtividade)',
      'Dados: 🎲3 🎲4 🎲5 = **12**',
      'Resultado: **SUCESSO +2**'
    ].join('\n')
  );
});

test('a tabela crítica continua com o formato dela', () => {
  // Este caso especial já existia e usa outro molde: tem Dados, não tem
  // Resultado. Ele não pode ter sido atropelado pelo caso da Nota.
  const msg = formatRollMessage({
    character: 'jack',
    testType: '💥 Golpe Fulminante',
    dice: [3],
    total: 3,
    outcome: 'Acerta um ponto vital.'
  });
  assert.ok(msg.includes('Dados:'), 'a tabela crítica perdeu a linha de Dados');
  assert.ok(!msg.includes('Resultado:'));
  assert.ok(msg.includes('Acerta um ponto vital.'));
});

test('o sucesso decisivo e a falha crítica continuam sendo detectados', () => {
  const decisivo = formatRollMessage({
    character: 'x', testType: 'Perícia', dice: [1, 1, 1], total: 3, target: 12, outcome: 'sucesso'
  });
  assert.ok(decisivo.includes('SUCESSO DECISIVO'), decisivo);

  const critica = formatRollMessage({
    character: 'x', testType: 'Perícia', dice: [6, 6, 6], total: 18, target: 12, outcome: 'falha'
  });
  assert.ok(critica.includes('FALHA CRÍTICA'), critica);
});
