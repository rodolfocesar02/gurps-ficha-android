'use strict';
/**
 * Testes das regras do cabeçalho (MC-1). Rodar:  node --test scripts/magias/cabecalho.test.js
 * Páginas e magias SINTÉTICAS (inventadas aqui). Precisam do leitor da Mesa ao lado; sem ele, pulam.
 */
const test = require('node:test');
const assert = require('node:assert');
const L = require('./localizador');
const C = require('./cabecalho');
const P = require('./prerequisitos');

const TEM_LEITOR = !!C.leitor();
const opcoes = { skip: !TEM_LEITOR };

function tabela(campos, extra) {
  return Object.assign({
    casamento: 'exato', nome: 'Alfa', pagina: 10, reparada: null,
    campos: Object.assign({ classe: 'Comum', escola: 'Teste', duracao: '1 min', energia: '2', tempo: '1 seg', preRequisitos: 'AM1' }, campos),
  }, extra || {});
}
function magia(campos) {
  return Object.assign({ id: 'alfa', nome: 'Alfa', duracao: '1 min.', energia: '02', tempoOperacao: '1 seg.', classe: 'Comum', preRequisitos: 'AM1, Beta', pagina: 10 }, campos);
}
const campo = (d, nome) => d.decisoes.find((s) => s.campo === nome);

test('comparar: mesma coisa escrita de outro jeito é "formato"; outro número é "valor"', opcoes, () => {
  assert.strictEqual(C.comparar('energia', '04/02', '4/2'), 'formato');
  assert.strictEqual(C.comparar('energia', '4/2', '4/2'), 'igual');
  assert.strictEqual(C.comparar('energia', '4/2', '4/3'), 'valor');
  assert.strictEqual(C.comparar('duracao', '1 min.', '1 min'), 'formato');
  assert.strictEqual(C.comparar('duracao', 'Instant.', '1 seg.'), 'valor');
  assert.strictEqual(C.comparar('tempoOperacao', '3 seg.', '3 seg'), 'formato');
  assert.strictEqual(C.comparar('tempoOperacao', '3 seg.', '1 seg.'), 'valor');
});

test('decidir: o que a tabela escreve entra; o valor que mudou fica marcado como "valor"', opcoes, () => {
  const d = C.decidir(magia({ duracao: 'Instant.', energia: '03', tempoOperacao: '3 seg.' }), { tabela: tabela({ duracao: '1 min', energia: '3', tempo: '3 seg' }) }, {});
  assert.strictEqual(d.aplica, true);
  assert.strictEqual(d.fonte, 'tabela');
  assert.deepStrictEqual(d.novo, { duracao: '1 min', energia: '3', tempoOperacao: '3 seg' });
  assert.strictEqual(campo(d, 'duracao').tipo, 'valor');
  assert.strictEqual(campo(d, 'energia').tipo, 'formato');
  assert.strictEqual(campo(d, 'tempoOperacao').tipo, 'formato');
});

test('decidir: variante de família NÃO é escrita (o livro só tem a linha da família)', opcoes, () => {
  const m = magia({ nome: 'Atrofiar (Visão)', duracao: '1 min.' });
  const d = C.decidir(m, { tabela: tabela({ duracao: '10 min' }, { casamento: 'base', nome: 'Atrofiar' }) }, {});
  assert.strictEqual(d.aplica, false);
  assert.strictEqual(d.fonte, 'tabela-familia');
  assert.deepStrictEqual(d.novo, {});
  assert.strictEqual(campo(d, 'duracao').tipo, 'valor', 'a diferença aparece no relatório');
  // o contrário (o catálogo é a mãe; a tabela escreve "(Tipo)") aplica
  const mae = C.decidir(magia({ nome: 'Comandar Espírito' }), { tabela: tabela({}, { casamento: 'base', nome: 'Comandar Espírito (Tipo)' }) }, {});
  assert.strictEqual(mae.aplica, true);
});

test('decidir: pré-requisito NUNCA muda neste lote (a tabela usa "#" e o parser do app desliga a validação)', opcoes, () => {
  const d = C.decidir(magia({ preRequisitos: 'AM1, Beta' }), { tabela: tabela({ preRequisitos: 'AM1, Beta#' }) }, { preRequisitos: 'Aptidão Mágica 1 e Beta.' });
  assert.strictEqual(d.novo.preRequisitos, undefined);
  assert.strictEqual(campo(d, 'preRequisitos').tipo, 'mantido');
  assert.strictEqual(campo(d, 'preRequisitos').tabela, 'AM1, Beta#');
});

test('decidir: classe — mesmo significado fica a do catálogo (que às vezes diz mais)', opcoes, () => {
  const d = C.decidir(magia({ classe: 'Comum/R-Vont ou perícia' }), { tabela: tabela({ classe: 'Comum/R-Vont' }) }, {});
  assert.strictEqual(d.novo.classe, undefined, 'não perde o "ou perícia"');
  const e = C.decidir(magia({ classe: 'Área' }), { tabela: tabela({ classe: 'Comum' }) }, {});
  assert.strictEqual(e.novo.classe, 'Comum', 'classe de verdade diferente: a tabela manda');
});

test('decidir: classe — a entrada arbitra: concorda com o catálogo e discorda da tabela, vale a entrada', opcoes, () => {
  const d = C.decidir(magia({ classe: 'Projétil' }), { tabela: tabela({ classe: 'Comum' }) }, { classe: 'Projétil' });
  assert.strictEqual(d.novo.classe, undefined);
  assert.match(campo(d, 'classe').motivo, /o-livro-discorda-de-si/);
});

test('decidir: classe suja da tabela ("Comum/R/-HT") não entra', opcoes, () => {
  const d = C.decidir(magia({ classe: 'Comum/R-HT' }), { tabela: tabela({ classe: 'Comum/R/-HT' }) }, {});
  assert.strictEqual(d.novo.classe, undefined);
  assert.strictEqual(campo(d, 'classe').motivo, 'tabela-ilegivel');
});

test('decidir: texto da tabela que apagaria o custo de manter não entra ("6,2" por "6/2")', opcoes, () => {
  const d = C.decidir(magia({ energia: '06/02' }), { tabela: tabela({ energia: '6,2' }) }, {});
  assert.strictEqual(d.novo.energia, undefined);
  assert.strictEqual(campo(d, 'energia').motivo, 'tabela-perde-a-manutencao');
});

test('decidir: energia que ainda traz unidade de tempo é suspeita e não entra', opcoes, () => {
  const d = C.decidir(magia({ energia: '3' }), { tabela: tabela({ energia: 'Varia hora=custo' }) }, {});
  assert.strictEqual(d.novo.energia, undefined);
  assert.strictEqual(campo(d, 'energia').motivo, 'tabela-suspeita');
});

test('decidir: duração — a entrada, quando é clara, arbitra', opcoes, () => {
  // catálogo e tabela concordam (1 min), a entrada diz 5 segundos
  const a = C.decidir(magia({ duracao: '1 min.' }), { tabela: tabela({ duracao: '1 min' }) }, { duracao: '5 segundos.' });
  assert.strictEqual(a.novo.duracao, '5 segundos');
  assert.match(campo(a, 'duracao').motivo, /vale a entrada/);
  // a entrada concorda com o catálogo: a tabela é que está errada, nada muda
  const b = C.decidir(magia({ duracao: '1 min.' }), { tabela: tabela({ duracao: 'Instant' }) }, { duracao: '1 minuto.' });
  assert.strictEqual(b.novo.duracao, undefined);
  assert.match(campo(b, 'duracao').motivo, /o-livro-discorda-de-si/);
});

test('decidir: texto da entrada que só PARECE duração ("Permanece em vigor…") não arbitra', opcoes, () => {
  const d = C.decidir(magia({ duracao: 'Indef.#' }), { tabela: tabela({ duracao: 'Indef#' }) }, { duracao: 'Permanece em vigor até que seja quebrada' });
  assert.strictEqual(d.novo.duracao, 'Indef#', 'fica o da tabela, que é só a forma');
  assert.ok(!/vale a entrada/.test(campo(d, 'duracao').motivo || ''));
});

test('decidir: tempo "por ponto" na entrada não arbitra (é fórmula, não número)', opcoes, () => {
  const d = C.decidir(magia({ tempoOperacao: '1 seg.' }), { tabela: tabela({ tempo: '1 seg' }) }, { tempo: '1 segundo por ponto de energia.' });
  assert.strictEqual(d.novo.tempoOperacao, '1 seg');
});

test('decidir: página — a da tabela entra; sem página na tabela, fica a do catálogo', opcoes, () => {
  const a = C.decidir(magia({ pagina: 144 }), { tabela: tabela({}, { pagina: 145 }) }, {});
  assert.strictEqual(a.novo.pagina, 145);
  const b = C.decidir(magia({ pagina: 27 }), { tabela: tabela({}, { pagina: null, reparada: 'sem-pagina' }) }, {});
  assert.strictEqual(b.novo.pagina, undefined);
  assert.strictEqual(campo(b, 'pagina').motivo, 'tabela-sem-pagina');
});

test('decidir: sem linha na tabela não escreve nada', opcoes, () => {
  const d = C.decidir(magia({}), { tabela: null, entrada: null }, {});
  assert.strictEqual(d.aplica, false);
  assert.deepStrictEqual(d.novo, {});
});

test('nome: erro de digitação sai do vocabulário do livro; nome que existe não é mexido', () => {
  const livro = { paginas: new Map([[10, ['# Página 10', 'Aumentar Vitalidade é uma mágica.', '| | Aumentar Vitalidade | Vigor |', 'Furacão e Projétil.']]]) };
  const voc = C.vocabulario(livro);
  assert.strictEqual(C.nomeCorrigidoPeloLivro('Aumentar Vitlidade', livro, voc), 'Aumentar Vitalidade');
  assert.strictEqual(C.nomeCorrigidoPeloLivro('Furacão', livro, voc), null);
  assert.strictEqual(C.nomeCorrigidoPeloLivro('Palavra Inexistente', livro, voc), null, 'sem vizinha no livro, não inventa');
});

test('pré-requisito: compara CONTEÚDO (AM, quantidade, magias citadas), não a redação', () => {
  const cat = [{ id: 'cura', nome: 'Cura Profunda' }, { id: 'vigor', nome: 'Vigor' }, { id: 'remendar', nome: 'Remendar' }];
  const idx = P.indiceDeNomes(cat);
  assert.strictEqual(P.compararConteudo('AM2, Vigor, 5 mágicas de Cura', 'Aptidão Mágica 2 e Vigor e pelo menos cinco mágicas de Cura.', idx), null);
  const dif = P.compararConteudo('AM2, 5 mágicas de Cura', 'Aptidão Mágica 2 e pelo menos oito mágicas de Cura', idx);
  assert.deepStrictEqual(dif.quantidades, [[5], [8]]);
  const dif2 = P.compararConteudo('AM3, Vigor', 'Aptidão Mágica 2 e Remendar', idx);
  assert.deepStrictEqual(dif2.am, [3, 2]);
  assert.deepStrictEqual(dif2.soNoCatalogo, ['vigor']);
  assert.deepStrictEqual(dif2.soNaEntrada, ['remendar']);
  // o maior nome vence: "Cura Profunda" não vira "Cura" + "Profunda"
  assert.deepStrictEqual([...P.magiasCitadas('Cura Profunda', idx)], ['cura']);
});
