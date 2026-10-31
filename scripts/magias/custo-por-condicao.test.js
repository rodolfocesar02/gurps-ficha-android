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

test('as 18 magias do pedido dele (lote 1) estão no arquivo à mão', () => {
  const doLote1 = Object.keys(C.lerOsSpecs(C.ARQUIVO));
  assert.deepStrictEqual(doLote1.sort(), ['alongar_objeto', 'animar_objeto', 'animar_planta', 'aumentar_objeto', 'brilho',
    'chicote_de_relampago', 'contrair_objeto', 'copiar', 'criar_animal', 'criar_montaria', 'despertar_computadornt', 'encolher_objeto',
    'fortalecer', 'mao_magica', 'reconstruirnt', 'teia_de_aranha', 'transformar_objeto', 'apressar'].sort());
});

test('o lote 2 tem o tamanho esperado e nenhuma magia aparece nos dois arquivos', () => {
  const gerados = Object.keys(C.lerOsSpecs(C.ARQUIVO_GERADO));
  assert.ok(gerados.length >= 250, 'poucas magias no lote 2: ' + gerados.length);
  assert.strictEqual(Object.keys(specs).length, 18 + gerados.length, 'id repetido entre os dois arquivos');
});

test('toda conta confere com o texto de custo do livro (número fora de 0 a 4 tem que estar lá)', opcoes, () => {
  const problemas = [];
  Object.keys(specs).forEach((id) => C.problemasDoSpec(id, specs[id], C.textoDoGuarda(id, specs[id], textos, porId)).forEach((p) => problemas.push(p)));
  assert.deepStrictEqual(problemas, []);
});

test('🔴 a conta confere também com a linha de custo lida DIRETO do PDF (o chunks.jsonl tem páginas velhas)', opcoes, () => {
  const doPdf = C.custosDoPdf();
  assert.ok(Object.keys(doPdf).length >= 700, 'custos-do-pdf.json vazio: rode extrair-custos-do-pdf.py');
  const problemas = [];
  const textosDoPdf = new Map(Object.keys(doPdf).map((id) => [id, doPdf[id].texto]));
  Object.keys(specs).forEach((id) => {
    if (!textosDoPdf.has(id)) return;          // a entrada não saiu do PDF (nome diferente): vale a guarda do chunks
    C.problemasDoSpec(id, specs[id], C.textoDoGuarda(id, specs[id], textosDoPdf, porId)).filter((p) => /não aparece/.test(p)).forEach((p) => problemas.push(p));
  });
  assert.deepStrictEqual(problemas, []);
});

test('🟥 sonda: o PDF pega o que o chunks.jsonl esconde (Mágica Penetrante, 10 → 8): um número que só o texto velho tem fica vermelho', opcoes, () => {
  const doPdf = C.custosDoPdf();
  assert.match(doPdf.magica_penetrante.texto, /Divisor de Armadura Custo 2 2 3 4 5 6 10 8 Ignora RD 10/, 'o PDF de 2025 traz 5 → 6 e 10 → 8');
  const conta = { tipo: 'varia', entradas: [], operar: ['*', 8, 4] };
  const velho = C.problemasDoSpec('x', conta, 'Veja a tabela abaixo. Divisor de Armadura Custo 2 2 3 4 4 6 5 8 10 10 Ignora RD 10');
  const novo = C.problemasDoSpec('x', conta, doPdf.magica_penetrante.texto);
  assert.deepStrictEqual([velho.length, novo.length], [0, 0], 'o 8 está nos dois textos: a conta passa');
  const seis = C.problemasDoSpec('x', { tipo: 'varia', entradas: [], operar: 9 }, doPdf.magica_penetrante.texto);
  assert.ok(seis.some((p) => /o número 9 não aparece/.test(p)), seis.join(' | '));
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

test('o spec só aceita `tamanho: true` e `fonte` "descricao" ou "como:<id>" (P-5); o `baixo` está no vocabulário', opcoes, () => {
  const base = { tipo: 'varia', entradas: [], operar: ['baixo', 1.5] };
  const ok = C.problemasDoSpec('x', Object.assign({}, base, { tamanho: true, fonte: 'como:calor' }), 'Custo 1,5.');
  assert.deepStrictEqual(ok, []);
  assert.ok(C.problemasDoSpec('x', Object.assign({}, base, { tamanho: false }), '1').some((p) => /tamanho/.test(p)));
  assert.ok(C.problemasDoSpec('x', Object.assign({}, base, { fonte: 'qualquer coisa' }), '1').some((p) => /fonte/.test(p)));
  // o texto do guarda: a linha própria, mais a descrição ("veja a tabela abaixo") ou a linha de outra magia ("exatamente como Calor")
  const fake = new Map([['a', 'veja a tabela'], ['b', 'tem 7']]);
  const porIdFake = new Map([['a', { descricao: 'tabela: 2 4 6' }]]);
  assert.match(C.textoDoGuarda('a', { fonte: 'descricao' }, fake, porIdFake), /2 4 6/);
  assert.match(C.textoDoGuarda('a', { fonte: 'como:b' }, fake, porIdFake), /tem 7/);
  assert.strictEqual(C.textoDoGuarda('a', {}, fake, porIdFake), 'veja a tabela');
});

test('P-5c: o modificador de NH — a guarda aceita `$<escolha>_mod`, recusa referência solta, `mod` que não é número e modificador sem nome', opcoes, () => {
  const dist = { id: 'distancia', tipo: 'escolha', rotulo: 'Distância', padrao: 'a', opcoes: [{ id: 'a', rotulo: 'A', valor: 3, mod: 0 }, { id: 'b', rotulo: 'B', valor: 5, mod: -2 }] };
  const ok = { tipo: 'varia', entradas: [dist], operar: '$distancia', modificador: '$distancia_mod', modificadorRotulo: 'Salto', modificadorPagina: 147 };
  assert.deepStrictEqual(C.problemasDoSpec('x', ok, 'Custo 3.'), []);
  const solta = Object.assign({}, ok, { modificador: '$nao_existe_mod' });
  assert.ok(C.problemasDoSpec('x', solta, 'Custo 3.').some((p) => /"\$nao_existe_mod" não é uma entrada/.test(p)));
  const semNome = Object.assign({}, ok); delete semNome.modificadorRotulo;
  assert.ok(C.problemasDoSpec('x', semNome, 'Custo 3.').some((p) => /nome da parcela/.test(p)));
  const ruim = JSON.parse(JSON.stringify(ok)); ruim.entradas[0].opcoes[1].mod = '-2';
  assert.ok(C.problemasDoSpec('x', ruim, 'Custo 3.').some((p) => /o `mod` da opção b não é número/.test(p)));
  // um número que o livro não tem na expressão do modificador também fica vermelho (a guarda vale para as duas contas)
  const inventado = Object.assign({}, ok, { modificador: ['-', 0, ['*', 7, '$distancia']] });
  assert.ok(C.problemasDoSpec('x', inventado, 'Custo 3.').some((p) => /modificador: o número 7 não aparece/.test(p)));
  // e o catálogo: o Teleporte e as magias do salto no tempo trazem o modificador e o nome da parcela
  ['teleporte', 'teleportar_outro', 'telemagica', 'viagem_no_tempo', 'transportar_outro_no_tempo', 'jornada_rapida', 'desviar_teleporte', 'translocacao_no_tempo', 'translocar_outro_no_tempo'].forEach((id) => {
    const c = porId.get(id).custoPorCondicao;
    assert.ok(c.modificador !== undefined && c.modificadorRotulo && Number.isInteger(c.modificadorPagina), id);
  });
});

test('o catálogo gravado É o que o passo 6 tira do arquivo (idempotente) e não sobra número-base errado', () => {
  const r = P6.aplicar(catalogo, specs, textos);
  assert.deepStrictEqual(r.problemas, []);
  assert.strictEqual(JSON.stringify(r.saida), JSON.stringify(catalogo), 'rode node scripts/magias/aplicar-custo-por-condicao.js');
  Object.keys(specs).forEach((id) => {
    const m = porId.get(id);
    assert.deepStrictEqual(m.custoPorCondicao.entradas, specs[id].entradas, id);
    if (specs[id].tipo !== 'inalterado') assert.strictEqual(m.custoTipo, specs[id].tipo, id);
    if (specs[id].tipo === 'inalterado') {
      // a magia de ÁREA: o custo básico do livro continua no catálogo; a conta o multiplica pelo raio
      assert.ok(m.custoTipo, id + ': o tipo do passo 2 continua');
    } else if (specs[id].tipo !== 'fixo') {
      assert.strictEqual(m.custoOperar, undefined, id + ': a conta não pode ter custoOperar');
      assert.strictEqual(m.custoManter, undefined, id + ': a conta não pode ter custoManter');
      assert.ok(m.custoFormula && m.custoFormula.length >= 6, id + ': sem o texto do livro');
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

// ------------------------------------------------------------------------------------------------ lote 2

test('os casos do lote 2 (escritos à mão no gerador, do texto do livro) fecham com a conta do catálogo', opcoes, () => {
  const arquivo = path.join(RAIZ, 'app', 'src', 'test', 'resources', 'custo_por_condicao_casos_gerados.json');
  const casos = JSON.parse(fs.readFileSync(arquivo, 'utf8')).casos;
  assert.ok(casos.length >= 500, 'poucos casos: ' + casos.length);
  const sem = Object.keys(C.lerOsSpecs(C.ARQUIVO_GERADO)).filter((id) => !casos.some((k) => k.id === id));
  assert.deepStrictEqual(sem, [], 'magias do lote 2 sem caso');
  casos.forEach((k) => {
    const r = MOTOR.oCustoPorCondicao(porId.get(k.id).custoPorCondicao, k.condicoes);
    assert.strictEqual(r.ok, true, k.id + ' ' + JSON.stringify(k.condicoes) + ' ' + JSON.stringify(r));
    assert.deepStrictEqual([r.operar, r.manter, r.manterTipo], [k.operar, k.manter, k.manterTipo], k.id + ' ' + JSON.stringify(k.condicoes));
    if (k.modificador !== undefined) assert.strictEqual(r.modificador, k.modificador, k.id + ' ' + JSON.stringify(k.condicoes) + ' modificador (P-5c)');
  });
});

test('o arquivo gerado É o que o gerador produz (nada à mão)', () => {
  const G = require('./gerar-custo-por-condicao');
  G.montar();
  const out = G.paraJson();
  const lido = (a) => fs.readFileSync(a, 'utf8').replace(/\r\n/g, '\n');
  assert.strictEqual(lido(G.SAIDA), JSON.stringify(out.specs, null, 1) + '\n', 'rode node scripts/magias/gerar-custo-por-condicao.js');
  assert.strictEqual(lido(G.CASOS), JSON.stringify(out.casos, null, 1) + '\n', 'rode node scripts/magias/gerar-custo-por-condicao.js');
});

test('a regra da ÁREA (Magia p.11): custo básico × raio, no mínimo 1 PF; só as que o livro escreve "Custo básico"', opcoes, () => {
  // Escudo Antiteleporte: 1/3 por metro, raio mínimo 3 → 1; raio 10 → ⌈3,33⌉ = 4
  const e = porId.get('escudo_antiteleporte').custoPorCondicao;
  assert.deepStrictEqual([MOTOR.oCustoPorCondicao(e, { raio: 3 }).operar, MOTOR.oCustoPorCondicao(e, { raio: 10 }).operar], [1, 4]);
  assert.strictEqual(MOTOR.oCustoPorCondicao(e, { raio: 2 }).erro, 'condicao_invalida', 'abaixo do raio mínimo do livro');
  // P-5: as que o livro escreve "Custo básico" com uma conta no lugar do número (Acelerar/Retardar Tempo, Retardar Fogo) também perguntam o raio
  ['acelerar_tempo', 'retardar_tempo', 'retardar_fogo'].forEach((id) => {
    const e = porId.get(id).custoPorCondicao.entradas.map((x) => x.id);
    assert.deepStrictEqual(e, ['raio', 'ritmo'], id);
  });
  // toda magia com raio pergunta o raio PRIMEIRO, com o mínimo do livro e o padrão no mínimo
  const comRaio = [...porId.values()].filter((m) => m.custoPorCondicao && m.custoPorCondicao.entradas.some((x) => x.id === 'raio'));
  assert.ok(comRaio.length >= 120, 'magias com raio: ' + comRaio.length);
  comRaio.forEach((m) => {
    const r = m.custoPorCondicao.entradas.find((x) => x.id === 'raio');
    assert.strictEqual(r.padrao, r.min, m.id + ': o padrão do raio é o mínimo (o custo de antes, para quem não mexe)');
  });
});
