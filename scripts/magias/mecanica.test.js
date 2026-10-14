'use strict';
/**
 * Testes da `mecanica` revista contra o livro (MC-4). Rodar:  node --test scripts/magias/mecanica.test.js
 * Unidade com magias sintéticas; livro real com o catálogo gravado. Precisam do leitor da Mesa.
 */
const test = require('node:test');
const assert = require('node:assert');
const fs = require('fs');
const path = require('path');
const M = require('./mecanica');
const X = require('./mecanica-extra');
const A = require('./aplicar-mecanica');

const TEM_LEITOR = !!M.leitor();
const opcoes = { skip: !TEM_LEITOR };
const RAIZ = path.resolve(__dirname, '..', '..');
const CATALOGO = process.env.MC_CATALOGO || path.join(RAIZ, 'app', 'src', 'main', 'assets', 'magias2versao.json');
const TEM_O_LIVRO = TEM_LEITOR && fs.existsSync(CATALOGO) && fs.existsSync(path.join(RAIZ, 'app', 'src', 'main', 'assets', 'chunks.jsonl'));

const mag = (nome, classe, mecanica, extra) => Object.assign({ id: nome.toLowerCase(), nome, classe, mecanica }, extra || {});

// ------------------------------------------------------------------------------ unidade

test('entrega: projétil só em classe Projétil', opcoes, () => {
  const lei = M.leitor();
  const com = A.decidirEntrega(mag('Alfa', 'Comum', { entrega: 'projetil' }), lei.aClasseDaMagia('Comum').classes);
  assert.deepStrictEqual([com.de, com.para], ['projetil', null]);
  const proj = A.decidirEntrega(mag('Beta', 'Projétil', { entrega: 'projetil' }), lei.aClasseDaMagia('Projétil').classes);
  assert.strictEqual(proj, null, 'classe Projétil fica');
  const bloq = A.decidirEntrega(mag('Gama', 'Bloqueio/R-DX', { entrega: 'projetil' }), lei.aClasseDaMagia('Bloqueio/R-DX').classes);
  assert.strictEqual(bloq.para, null);
});

test('entrega: toque só em classe Toque, ou onde o texto exige; toque opcional marcado', opcoes, () => {
  const lei = M.leitor();
  const cl = (c) => lei.aClasseDaMagia(c).classes;
  assert.strictEqual(A.decidirEntrega(mag('Delta', 'Toque/R-HT', { entrega: 'toque' }), cl('Toque/R-HT')), null);
  const semTexto = A.decidirEntrega(mag('Épsilon', 'Comum', { entrega: 'toque' }), cl('Comum'));
  assert.deepStrictEqual([semTexto.de, semTexto.para], ['toque', null]);
  assert.strictEqual(A.decidirEntrega(mag('Purificar', 'Comum/R-Espec.', { entrega: 'toque' }), cl('Comum')), null, 'o texto exige');
  const opc = A.decidirEntrega(mag('Desidratar', 'Comum/R-HT', { entrega: 'toque' }), cl('Comum'));
  assert.deepStrictEqual([opc.para, opc.extra.toqueOpcional], ['toque', true]);
  // o texto exige e o catálogo não tinha: ganha toque
  const roubar = A.decidirEntrega(mag('Roubar Vitalidade', 'Comum', { efeito: 'narrado' }), cl('Comum'));
  assert.deepStrictEqual([roubar.de, roubar.para], [null, 'toque']);
  // chicote e bola
  const chicote = A.decidirEntrega(mag('Chicote de Relâmpago', 'Comum', { entrega: 'toque' }), cl('Comum'));
  assert.deepStrictEqual([chicote.para, chicote.extra.ataqueComoChicote], [null, true]);
  const bola = A.decidirEntrega(mag('Bola de Relâmpagos', 'Projétil', { entrega: 'area' }), cl('Projétil'));
  assert.strictEqual(bola.extra.semJogadaDeAtaque, true);
});

test('dano: o dado do catálogo tem de aparecer no texto; o da linha de custo vale mais', opcoes, () => {
  const base = (descricao, k) => ({ id: 'x', nome: 'X', descricaoConferida: true, descricao, mecanica: Object.assign({ efeito: 'dano' }, k) });
  assert.strictEqual(M.conferirDano(base('Comum\nCausa 1d de dano por ponto de energia.\nCusto: 1 a 3.', { danoPorEnergia: '1d', energiaPorDado: 1 })).status, 'confere');
  assert.strictEqual(M.conferirDano(base('Comum\nCausa 1d-1 de dano por ponto de energia.', { danoPorEnergia: '1d', energiaPorDado: 1 })).status, 'diverge');
  // "1d em vez de 1d-1" no texto, mas a linha de custo diz 1d+1: o livro discorda de si
  assert.strictEqual(M.conferirDano(base('Comum\nCada ponto compra 1d em vez de 1d-1.\nCusto: 1 a 4. Dá 1d+1 de dano a cada ponto.', { danoPorEnergia: '1d', energiaPorDado: 1 })).status, 'diverge-custo');
  // a variante de custo DOBRADO não conta
  assert.strictEqual(M.conferirDano(base('Comum\nCausa 1d-1 por segundo.\nCusto: 1. Dobrando o custo, causa 2d-2 por segundo.', { danoPorEnergia: '1d-1', energiaPorDado: 1 })).status, 'confere');
  // a energia por dado só é cobrada quando o texto a diz
  assert.strictEqual(M.conferirDano(base('Comum\nCausa 1d de dano para cada 2 pontos de energia.', { danoPorEnergia: '1d', energiaPorDado: 1 })).status, 'diverge-energia');
  assert.strictEqual(M.conferirDano({ id: 'y', nome: 'Y', descricaoConferida: false, descricao: 'x', mecanica: { efeito: 'dano', danoPorEnergia: '1d' } }).status, 'sem-texto');
});

test('dano em fórmula: "N por dado" e "N ponto por segundo por energia"', opcoes, () => {
  const m1 = { id: 'g', nome: 'G', descricaoConferida: true, descricao: 'Comum\nCusto: 1 por dado de dano causado, até 3.', mecanica: { efeito: 'dano', danoPorEnergia: '1d', energiaPorDado: 1 } };
  assert.strictEqual(X.danoPorFormula(m1).status, 'confere');
  const m2 = { id: 'n', nome: 'N', descricaoConferida: true, descricao: 'Área\nA nuvem causa 1 ponto de dano por segundo para cada ponto de energia investido.', mecanica: { efeito: 'dano', danoPorEnergia: '1', energiaPorDado: 1 } };
  assert.strictEqual(X.danoPorFormula(m2).status, 'confere');
});

test('projétil: 1/2D, Máx e Prec do texto; o que o catálogo não tem, completa', opcoes, () => {
  const n = M.numerosDoProjetilNoTexto('Alcance 1/2D 25, Máx 50, Prec 1. Ataque Inato.');
  assert.deepStrictEqual(n, { meioDano: 25, maximo: 50, precisao: 1 });
  const m = { id: 'p', nome: 'P', classe: 'Projétil', descricaoConferida: true, descricao: 'Projétil\nTem 1/2D 20, Max 40, Prec 1.', mecanica: { efeito: 'dano' } };
  const r = M.conferirProjetil(m);
  assert.strictEqual(r.status, 'completar');
  assert.deepStrictEqual(r.faltam.sort(), ['maximo', 'meioDano', 'precisao']);
  const dif = M.conferirProjetil(Object.assign({}, m, { mecanica: { alcanceMeioDano: 25, alcanceMaximo: 40, precisao: 1 } }));
  assert.deepStrictEqual([dif.status, dif.divergem], ['diverge', ['meioDano']]);
});

test('resistência: só compara atributo contra atributo', () => {
  assert.strictEqual(A.atributoDaCondicao('HT'), 'ht');
  assert.strictEqual(A.atributoDaCondicao('HT_por_pv'), 'ht');
  assert.strictEqual(A.atributoDaCondicao('Vontade+1'), 'von');
  assert.strictEqual(A.atributoDaCondicao('resistido por Esquecimento'), null);
});

// ------------------------------------------------------------------------------ livro real

const catalogo = () => JSON.parse(fs.readFileSync(CATALOGO, 'utf8'));
const classesDe = (m) => M.leitor().aClasseDaMagia(m.classe).classes;

test('livro real: nenhuma magia fora de classe Projétil tem entrega "projetil"', { skip: !TEM_O_LIVRO }, () => {
  const ruins = catalogo().filter((m) => (m.mecanica || {}).entrega === 'projetil' && !classesDe(m).includes('Projétil')).map((m) => m.nome);
  assert.deepStrictEqual(ruins, []);
});

test('livro real: "toque" só em classe Toque, nas que o texto exige e nas de toque opcional', { skip: !TEM_O_LIVRO }, () => {
  const L = require('./localizador');
  const permitidas = new Set(A.TOQUE_PELO_TEXTO.concat(A.TOQUE_OPCIONAL));
  const ruins = catalogo().filter((m) => (m.mecanica || {}).entrega === 'toque' && !classesDe(m).includes('Toque')
    && !permitidas.has(L.normalizar(m.nome))).map((m) => m.nome);
  assert.deepStrictEqual(ruins, []);
  // e as que o texto exige TÊM toque
  const faltam = catalogo().filter((m) => A.TOQUE_PELO_TEXTO.includes(L.normalizar(m.nome)) && (m.mecanica || {}).entrega !== 'toque').map((m) => m.nome);
  assert.deepStrictEqual(faltam, []);
});

test('livro real: Chicote de Relâmpago e Bola de Relâmpagos marcados como a CONFERENCIA descreve', { skip: !TEM_O_LIVRO }, () => {
  const c = catalogo();
  const chicote = c.find((m) => m.id === 'chicote_de_relampago').mecanica;
  assert.strictEqual(chicote.ataqueComoChicote, true);
  assert.strictEqual(chicote.entrega, undefined);
  assert.strictEqual(c.find((m) => m.id === 'bola_de_relampagos').mecanica.semJogadaDeAtaque, true);
});

test('livro real: as 46 de dano e cura conferem com o livro, salvo as 4 listadas', { skip: !TEM_O_LIVRO }, () => {
  const calc = A.calcular();
  const naoConfere = calc.resultado.filter((r) => (r.danoStatus && r.danoStatus.status !== 'confere') || r.curaStatus === 'diverge')
    .map((r) => r.m.nome).sort();
  // Fender (a entrada não está no texto), Sopro Congelante (a entrada só traz o Item), Sopro de Vapor
  // (o livro diz 1d na descrição e 1d+1 na linha de custo) e Tempestade de Faíscas (o livro dá 1d-1/2d-2/3d-3
  // por custo 2/4/6; o catálogo tem 1 por energia).
  assert.deepStrictEqual(naoConfere, ['Fender', 'Sopro Congelante', 'Sopro de Vapor', 'Tempestade de Faíscas']);
  const alvo = calc.resultado.filter((r) => ['dano', 'cura'].includes((r.m.mecanica || {}).efeito));
  assert.strictEqual(alvo.length, 46);
});

test('livro real: projéteis — 1/2D, Máx e Prec batem com o livro onde o livro os dá', { skip: !TEM_O_LIVRO }, () => {
  const calc = A.calcular();
  const ruins = calc.resultado.filter((r) => r.projetilStatus && r.projetilStatus.status === 'diverge').map((r) => r.m.nome);
  assert.deepStrictEqual(ruins, []);
  const c = catalogo();
  const bfe = c.find((m) => m.id === 'bola_de_fogo_explosiva').mecanica;
  assert.deepStrictEqual([bfe.alcanceMeioDano, bfe.alcanceMaximo, bfe.precisao], [25, 50, 1]);
  const con = c.find((m) => m.id === 'concussao').mecanica;
  assert.deepStrictEqual([con.alcanceMeioDano, con.alcanceMaximo, con.precisao], [20, 40, 1]);
});

test('livro real: o catálogo gravado É o que o aplicador gera hoje (idempotente)', { skip: !TEM_O_LIVRO }, () => {
  const calc = A.calcular();
  const gerado = A.aplicar(calc);
  const atual = catalogo();
  const dif = [];
  gerado.forEach((g, i) => { if (JSON.stringify(g.mecanica) !== JSON.stringify(atual[i].mecanica)
    || g.mecanicaConferida !== atual[i].mecanicaConferida) dif.push(g.nome); });
  assert.deepStrictEqual(dif.slice(0, 10), []);
});

test('🟥 sonda: pôr "projetil" numa magia Comum, ou tirar o toque de Purificar, fica vermelho', { skip: !TEM_O_LIVRO }, () => {
  const c = JSON.parse(JSON.stringify(catalogo()));
  const alu = c.find((m) => m.nome === 'Alucinação');
  alu.mecanica.entrega = 'projetil';
  const ruins = c.filter((m) => (m.mecanica || {}).entrega === 'projetil' && !classesDe(m).includes('Projétil')).map((m) => m.nome);
  assert.deepStrictEqual(ruins, ['Alucinação']);
  const L = require('./localizador');
  const pur = c.find((m) => m.nome === 'Purificar');
  delete pur.mecanica.entrega;
  const faltam = c.filter((m) => A.TOQUE_PELO_TEXTO.includes(L.normalizar(m.nome)) && (m.mecanica || {}).entrega !== 'toque').map((m) => m.nome);
  assert.deepStrictEqual(faltam, ['Purificar']);
});
