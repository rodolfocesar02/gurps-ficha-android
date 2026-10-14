'use strict';
/**
 * Testes das descrições recortadas do livro (MC-3). Rodar:  node --test scripts/magias/descricao.test.js
 *
 *  1. UNIDADE — um bloco sintético em cada formato do livro (A, B, C, E, F, citação): o recorte tira
 *     o título, o markdown e as bordas de tabela, e deixa os parágrafos.
 *  2. LIVRO REAL — para 30 magias sorteadas com SEMENTE FIXA, a descrição do catálogo é igual,
 *     caractere a caractere, ao recorte feito agora do `chunks.jsonl`. SONDA: trocar uma palavra
 *     deixa o teste vermelho.
 */
const test = require('node:test');
const assert = require('node:assert');
const fs = require('fs');
const path = require('path');
const L = require('./localizador');
const D = require('./descricao');

const RAIZ = path.resolve(__dirname, '..', '..');
const CHUNKS = path.join(RAIZ, 'app', 'src', 'main', 'assets', 'chunks.jsonl');
const CATALOGO = process.env.MC_CATALOGO || path.join(RAIZ, 'app', 'src', 'main', 'assets', 'magias2versao.json');
const MANUAL = path.join(__dirname, 'magias-localizacao-manual.json');
const TEM_O_LIVRO = fs.existsSync(CHUNKS) && fs.existsSync(CATALOGO);

function livroDe(paginas) {
  return { paginas: new Map(Object.entries(paginas).map(([n, ls]) => [Number(n), ls])) };
}

function descricaoDe(paginas, nome, pagina) {
  const livro = livroDe(paginas);
  const r = L.construirIndice(livro, [{ id: 'x', nome, pagina: pagina || 10 }], {});
  const e = r.magias.x.entrada;
  return e ? D.recortarDescricao(livro, e) : null;
}

// ------------------------------------------------------------------------------ 1. unidade

test('formato A: tira o título, mantém classe, corpo, campos e Item; sem markdown', () => {
  const d = descricaoDe({
    10: ['# Página 10 - Teste', '', '**Alfa**', 'Comum; Resistível com HT', 'Corpo da alfa, uma frase.', 'Duração: 1 minuto.',
      'Custo: 2 para operar.', '', '**Item**', '(a) Um item.', '', '**Beta**', 'Comum', 'Outra.'],
  }, 'Alfa');
  assert.strictEqual(d.texto, 'Comum; Resistível com HT\nCorpo da alfa, uma frase.\nDuração: 1 minuto.\nCusto: 2 para operar.\n\nItem\n(a) Um item.');
  assert.strictEqual(d.suspeita, null);
});

test('formato B: "## Nome" + classe em negrito, campos em negrito, para no ---', () => {
  const d = descricaoDe({
    10: ['# Página 10 - Teste', '', '## Gama', '**Projétil**', 'Corpo da gama.', '**Duração:** 1 s.', '**Custo:** 1 a 3.', '', '---', '## Delta', '*Área*'],
  }, 'Gama');
  assert.strictEqual(d.texto, 'Projétil\nCorpo da gama.\nDuração: 1 s.\nCusto: 1 a 3.');
});

test('formato C: classe na linha do título; campos colados por | viram linhas', () => {
  const d = descricaoDe({
    10: ['# Página 10 - Teste', '', '**Delta** | Comum', '--- | ---', 'Corpo da delta.', '**Duração:** 1 min. | **Pré-requisitos:** Alfa |', '', '---'],
  }, 'Delta');
  assert.strictEqual(d.texto, 'Comum\nCorpo da delta.\nDuração: 1 min.\nPré-requisitos: Alfa');
});

test('formato E: "**Nome** (Classe)"', () => {
  const d = descricaoDe({
    10: ['# Página 10 - Teste', '', '**Épsilon** (Informação)', '', 'Corpo da épsilon.', '', '**Custo:** 4.', '---'],
  }, 'Épsilon');
  assert.strictEqual(d.texto, 'Informação\n\nCorpo da épsilon.\n\nCusto: 4.');
});

test('formato F: mini-tabela — as células viram linhas, o cabeçalho dá a classe', () => {
  const d = descricaoDe({
    10: ['# Página 10 - Teste', '', '| Eta | Informação | Comum |', '|---|---|---|', '| Corpo da eta. | | |', '| **Duração:** 1 min. | | |', '', '| Teta | Comum |'],
  }, 'Eta');
  assert.strictEqual(d.texto, 'Informação\nComum\nCorpo da eta.\nDuração: 1 min.');
});

test('citação (>): marcas e linhas vazias de citação somem', () => {
  const d = descricaoDe({
    10: ['# Página 10 - Teste', '', '> **Zeta**', '>', '> **Informação**', '>', '> Corpo da zeta.', '>', '> **Custo:** 10.'],
  }, 'Zeta');
  assert.strictEqual(d.texto, 'Informação\n\nCorpo da zeta.\n\nCusto: 10.');
});

test('título quebrado em duas linhas: a continuação do título não entra na descrição', () => {
  const d = descricaoDe({
    10: ['# Página 10 - Teste', '', '**Kappa**', 'Superior (MD)', 'Comum; Resistível com Vontade', 'Corpo.', 'Duração: 1 min.'],
  }, 'Kappa Superior');
  assert.strictEqual(d.texto.split('\n')[0], 'Comum; Resistível com Vontade');
  assert.ok(!/Superior/.test(d.texto));
});

test('a entrada atravessa a página: o cabeçalho "# Página N" não entra', () => {
  const d = descricaoDe({
    20: ['# Página 20 - Teste', '', '**Mi**', 'Comum', 'Corpo da mi.'],
    21: ['# Página 21 - Teste', '', 'Duração: 1 min.', 'Custo: 3.', '', '**Ni**', 'Comum', 'Outra.'],
  }, 'Mi', 20);
  assert.strictEqual(d.texto, 'Comum\nCorpo da mi.\nDuração: 1 min.\nCusto: 3.');
});

test('três ou mais colunas na mesma linha = texto de várias magias misturado: marcado como suspeito', () => {
  const d = descricaoDe({
    10: ['# Página 10 - Teste', '', '**Iota**', 'Comum', 'Corpo da iota.', 'Um | Dois | Três mais texto'],
  }, 'Iota');
  assert.strictEqual(d.suspeita, 'colunas');
});

test('classe com redação incomum ("Comum; Resiste à Cura") continua sendo a 1ª linha', () => {
  const d = descricaoDe({
    10: ['# Página 10 - Teste', '', '**Lambda**', 'Comum; Resiste à Cura', 'Corpo da lambda.'],
  }, 'Lambda');
  assert.strictEqual(d.texto.split('\n')[0], 'Comum; Resiste à Cura');
});

// ------------------------------------------------------------------------------ 2. livro real

/** Gerador pseudoaleatório de semente fixa (mulberry32): o mesmo sorteio em toda máquina. */
function sorteio(semente) {
  let a = semente >>> 0;
  return () => {
    a = (a + 0x6D2B79F5) >>> 0;
    let t = a;
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

function recorteAgora() {
  const livro = L.carregarLivro(CHUNKS);
  const catalogo = JSON.parse(fs.readFileSync(CATALOGO, 'utf8'));
  const indice = L.construirIndice(livro, catalogo, { manual: L.lerLocalizacaoManual(MANUAL) });
  return { livro, catalogo, indice };
}

/** As magias de `amostra` cuja descrição NÃO é igual, caractere a caractere, ao recorte de agora. */
function diferentes(amostra, livro, indice) {
  return amostra.filter((m) => {
    const e = indice.magias[m.id].entrada;
    const r = D.recortarDescricao(livro, e);
    return !r || r.texto !== m.descricao;
  }).map((m) => m.nome);
}

test('livro real: 30 magias sorteadas (semente fixa) têm a descrição IGUAL ao recorte feito agora', { skip: !TEM_O_LIVRO }, () => {
  const { livro, catalogo, indice } = recorteAgora();
  const conferidas = catalogo.filter((m) => m.descricaoConferida === true);
  assert.ok(conferidas.length > 780, 'conferidas: ' + conferidas.length);
  const rnd = sorteio(13102026);
  const ordem = conferidas.slice();
  for (let i = ordem.length - 1; i > 0; i--) {
    const j = Math.floor(rnd() * (i + 1));
    [ordem[i], ordem[j]] = [ordem[j], ordem[i]];
  }
  const amostra = ordem.slice(0, 30);
  assert.strictEqual(amostra.length, 30);
  assert.deepStrictEqual(diferentes(amostra, livro, indice), []);
});

test('livro real: TODAS as descrições conferidas são o recorte de agora (as 812)', { skip: !TEM_O_LIVRO }, () => {
  const { livro, catalogo, indice } = recorteAgora();
  const conferidas = catalogo.filter((m) => m.descricaoConferida === true);
  assert.deepStrictEqual(diferentes(conferidas, livro, indice).slice(0, 10), []);
});

test('🟥 sonda: trocar UMA palavra da descrição deixa o teste vermelho, com o nome da magia', { skip: !TEM_O_LIVRO }, () => {
  const { livro, catalogo, indice } = recorteAgora();
  const alvo = catalogo.find((m) => m.id === 'lealdade');
  const mutada = Object.assign({}, alvo, { descricao: alvo.descricao.replace(/operador/, 'operadora') });
  assert.notStrictEqual(mutada.descricao, alvo.descricao, 'a troca tem de ter acontecido');
  assert.deepStrictEqual(diferentes([mutada], livro, indice), ['Lealdade']);
  // e a original passa
  assert.deepStrictEqual(diferentes([alvo], livro, indice), []);
});

test('livro real: o que não se recortou fica marcado, com motivo, e a descrição antiga não é tocada', { skip: !TEM_O_LIVRO }, () => {
  const catalogo = JSON.parse(fs.readFileSync(CATALOGO, 'utf8'));
  const semMarca = catalogo.filter((m) => typeof m.descricaoConferida !== 'boolean').map((m) => m.nome);
  assert.deepStrictEqual(semMarca, [], 'toda magia tem descricaoConferida');
  const semMotivo = catalogo.filter((m) => m.descricaoConferida === false && !m.descricaoMotivo).map((m) => m.nome);
  assert.deepStrictEqual(semMotivo, []);
  const semPagina = catalogo.filter((m) => m.descricaoConferida === true && !m.descricaoPagina).map((m) => m.nome);
  assert.deepStrictEqual(semPagina, []);
  // 67 marcadas no dia 14/out: não pode piorar
  assert.ok(catalogo.filter((m) => m.descricaoConferida === false).length <= 67);
});

test('livro real: a descrição conferida começa pela classe e não traz título, markdown nem borda de tabela', { skip: !TEM_O_LIVRO }, () => {
  const catalogo = JSON.parse(fs.readFileSync(CATALOGO, 'utf8'));
  const ruins = [];
  catalogo.filter((m) => m.descricaoConferida === true).forEach((m) => {
    if (/\*\*|(^|\n)#|(^|\n)>|(^|\n)---/.test(m.descricao)) ruins.push(m.nome + ': markdown');
    if (/(^|\n)\|/.test(m.descricao) || /\|\s*(\n|$)/.test(m.descricao)) ruins.push(m.nome + ': borda de tabela');
  });
  assert.deepStrictEqual(ruins.slice(0, 10), []);
});
