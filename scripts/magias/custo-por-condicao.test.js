'use strict';
/**
 * O custo que DEPENDE DA CONDIÇÃO — as guardas do catálogo (as da Mesa estão em `test/custo-por-condicao.test.js`).
 *
 * 1. cada conta de `custo-por-condicao.json` confere com o TEXTO DE CUSTO DO LIVRO (`chunks.jsonl`): os números da
 *    conta (fora 0 a 4) têm que aparecer nele;
 * 2. o catálogo gravado É o que o passo 6 tira do JSON (nada à mão), e onde o custo é conta não sobra o número-base
 *    errado que o passo 2 lia da primeira frase;
 * 3. os 73 casos do livro (`app/src/test/resources/custo_por_condicao_casos.json`, os mesmos do Kotlin e da Mesa)
 *    fecham com a conta do catálogo.
 */
const test = require('node:test');
const assert = require('node:assert');
const fs = require('node:fs');
const path = require('node:path');
const C = require('./custo-condicao');
const P6 = require('./aplicar-custo-por-condicao');

const RAIZ = path.resolve(__dirname, '..', '..');
const CATALOGO = path.join(RAIZ, 'app', 'src', 'main', 'assets', 'magias2versao.json');
const CASOS = path.join(RAIZ, 'app', 'src', 'test', 'resources', 'custo_por_condicao_casos.json');
const MOTOR = C.motor();
const opcoes = { skip: !MOTOR };

const catalogo = JSON.parse(fs.readFileSync(CATALOGO, 'utf8'));
const porId = new Map(catalogo.map((m) => [m.id, m]));
const specs = C.lerOsSpecs();
const textos = P6.textosDeCusto(catalogo);

test('as 18 magias do pedido dele estão no arquivo', () => {
  assert.deepStrictEqual(Object.keys(specs).sort(), ['alongar_objeto', 'animar_objeto', 'animar_planta', 'aumentar_objeto', 'brilho',
    'chicote_de_relampago', 'contrair_objeto', 'copiar', 'criar_animal', 'criar_montaria', 'despertar_computadornt', 'encolher_objeto',
    'fortalecer', 'mao_magica', 'reconstruirnt', 'teia_de_aranha', 'transformar_objeto', 'apressar'].sort());
});

test('toda conta confere com o texto de custo do livro (número fora de 0 a 4 tem que estar lá)', opcoes, () => {
  const problemas = [];
  Object.keys(specs).forEach((id) => C.problemasDoSpec(id, specs[id], textos.get(id)).forEach((p) => problemas.push(p)));
  assert.deepStrictEqual(problemas, []);
});

test('🟥 sonda: um número que o livro não tem na conta (0,25 no lugar de 0,5) deixa a guarda vermelha', opcoes, () => {
  const ruim = JSON.parse(JSON.stringify(specs.alongar_objeto));
  ruim.operar = ['*', ['cima', ['/', ['max', '$peso', 0.25], 0.25]], '$duplicacoes'];
  const p = C.problemasDoSpec('alongar_objeto', ruim, textos.get('alongar_objeto'));
  assert.ok(p.some((x) => /o número 0\.25 não aparece/.test(x)), p.join(' | '));
  const vazio = C.problemasDoSpec('alongar_objeto', specs.alongar_objeto, '');
  assert.ok(vazio.some((x) => /sem o texto de custo do livro/.test(x)));
  const solta = JSON.parse(JSON.stringify(specs.alongar_objeto));
  solta.operar = ['*', '$peso', '$nao_existe'];
  assert.ok(C.problemasDoSpec('alongar_objeto', solta, textos.get('alongar_objeto')).some((x) => /"\$nao_existe" não é uma entrada/.test(x)));
  const eval_ = JSON.parse(JSON.stringify(specs.alongar_objeto));
  eval_.operar = ['eval', 1];
  assert.ok(C.problemasDoSpec('alongar_objeto', eval_, textos.get('alongar_objeto')).some((x) => /fora do vocabulário/.test(x)));
});

test('o catálogo gravado É o que o passo 6 tira do arquivo (idempotente) e não sobra número-base errado', () => {
  const r = P6.aplicar(catalogo, specs, textos);
  assert.deepStrictEqual(r.problemas, []);
  assert.strictEqual(JSON.stringify(r.saida), JSON.stringify(catalogo), 'rode node scripts/magias/aplicar-custo-por-condicao.js');
  Object.keys(specs).forEach((id) => {
    const m = porId.get(id);
    assert.deepStrictEqual(m.custoPorCondicao.entradas, specs[id].entradas, id);
    assert.strictEqual(m.custoTipo, specs[id].tipo, id);
    if (specs[id].tipo !== 'fixo') {
      assert.strictEqual(m.custoOperar, undefined, id + ': a conta não pode ter custoOperar');
      assert.strictEqual(m.custoManter, undefined, id + ': a conta não pode ter custoManter');
      assert.ok(m.custoFormula && m.custoFormula.length > 10, id + ': sem o texto do livro');
    }
  });
});

test('os 73 casos do livro fecham com a conta do catálogo (o que o app e a Mesa também rodam)', opcoes, () => {
  const casos = JSON.parse(fs.readFileSync(CASOS, 'utf8')).casos;
  assert.ok(casos.length >= 70);
  casos.forEach((k) => {
    const r = MOTOR.oCustoPorCondicao(porId.get(k.id).custoPorCondicao, k.condicoes);
    assert.strictEqual(r.ok, true, k.id + ' ' + JSON.stringify(k.condicoes));
    assert.deepStrictEqual([r.operar, r.manter, r.manterTipo], [k.operar, k.manter, k.manterTipo], k.id + ' ' + JSON.stringify(k.condicoes));
  });
});
