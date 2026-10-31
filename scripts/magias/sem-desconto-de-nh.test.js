'use strict';
/**
 * "NH elevado não reduz o custo" — as guardas do passo 7 (P-5b, 31/out/2026). As da conta (Mesa e app) estão em
 * `test/sem-desconto-de-nh.test.js` (Mesa) e `MagiaSemDescontoDeNhTest` (Kotlin), com os mesmos casos.
 *
 *  1. cada magia da lista tem a frase DELA no PDF, na página dita (`custos-do-pdf.json`, lido do PDF por `extrair-custos-do-pdf.py`);
 *  2. toda frase do PDF que fala do NH e do custo está classificada: isenta uma magia ou está em `naoSeAplica` com o porquê;
 *  3. o catálogo gravado É o que o passo 7 tira da lista (idempotente).
 */
const test = require('node:test');
const assert = require('node:assert');
const fs = require('node:fs');
const path = require('node:path');
const C = require('./custo-condicao');
const P7 = require('./aplicar-sem-desconto-de-nh');

const CATALOGO = path.join(__dirname, '..', '..', 'app', 'src', 'main', 'assets', 'magias2versao.json');
const catalogo = JSON.parse(fs.readFileSync(CATALOGO, 'utf8'));
const lista = P7.lerALista();
const frases = C.frasesDeNhDoPdf();

test('a lista tem 16 magias e cada uma está no catálogo com a página do livro', () => {
  assert.strictEqual(Object.keys(lista.magias).length, 16);
  const porId = new Map(catalogo.map((m) => [m.id, m]));
  Object.keys(lista.magias).forEach((id) => {
    assert.ok(porId.has(id), id + ' não está no catálogo');
    assert.deepStrictEqual(porId.get(id).semDescontoDeNh, { pagina: lista.magias[id].pagina }, id);
  });
});

test('🔴 cada magia da lista tem a frase do livro, na página dita, no PDF; e nenhuma frase do PDF ficou sem classificação', () => {
  assert.ok(frases.length >= 20, 'custos-do-pdf.json sem as frases: rode extrair-custos-do-pdf.py');
  assert.deepStrictEqual(P7.problemasDaLista(lista, catalogo, frases), []);
});

test('🟥 sonda: uma frase que o livro não tem, uma página errada e uma frase nova sem dono deixam a guarda vermelha', () => {
  const clone = (x) => JSON.parse(JSON.stringify(x));
  const inventada = clone(lista); inventada.magias.forca.frase = 'O NH nunca importa para a Força';
  assert.ok(P7.problemasDaLista(inventada, catalogo, frases).some((p) => /forca: a frase .* não está na p\.37/.test(p)));
  const pagina = clone(lista); pagina.magias.telemagica.pagina = 129;
  assert.ok(P7.problemasDaLista(pagina, catalogo, frases).some((p) => /telemagica: a frase .* não está na p\.129/.test(p)));
  const nova = frases.concat([{ pagina: 99, frase: 'Um NH elevado não reduz o custo de uma mágica nova' }]);
  assert.ok(P7.problemasDaLista(lista, catalogo, nova).some((p) => /p\.99: a frase .* ninguém a classificou/.test(p)));
  const semMotivo = clone(lista); semMotivo.naoSeAplica[0].motivo = '';
  assert.ok(P7.problemasDaLista(semMotivo, catalogo, frases).some((p) => /ninguém a classificou/.test(p)), 'sem o porquê não vale');
  const inexistente = clone(lista); inexistente.magias.nao_existe = { pagina: 1, frase: 'x' };
  assert.ok(P7.problemasDaLista(inexistente, catalogo, frases).some((p) => /nao_existe: não existe no catálogo/.test(p)));
});

test('o catálogo gravado É o que o passo 7 tira da lista (idempotente): quem saiu da lista perde o campo', () => {
  assert.strictEqual(JSON.stringify(P7.aplicar(catalogo, lista)), JSON.stringify(catalogo), 'rode node scripts/magias/aplicar-sem-desconto-de-nh.js');
  const menos = JSON.parse(JSON.stringify(lista)); delete menos.magias.forca;
  const sem = P7.aplicar(catalogo, menos).find((m) => m.id === 'forca');
  assert.strictEqual(sem.semDescontoDeNh, undefined);
});

test('o livro NÃO isenta Graça nem Sabedoria (irmãs de Força e Vigor, mas a entrada não diz nada do NH)', () => {
  ['graca', 'sabedoria', 'fortalecer_vontade', 'debilitar'].forEach((id) => assert.strictEqual(lista.magias[id], undefined, id));
});
