'use strict';
/**
 * Testes do localizador (MC-0). Rodar:  node --test scripts/magias/localizador.test.js
 *
 * Duas camadas, de propósito:
 *  1. UNIDADE — páginas SINTÉTICAS (inventadas aqui; nenhuma frase do livro), uma por formato de
 *     entrada, para provar que cada leitor funciona sozinho.
 *  2. LIVRO REAL — o `chunks.jsonl` e o catálogo de verdade: cobertura mínima, as 20 magias de jogo,
 *     as páginas que o CONFERENCIA-LIVRO-MG-0 leu à mão, e uma SONDA que prova que o teste de
 *     cobertura morde (tirar a normalização de acento derruba a cobertura).
 */
const test = require('node:test');
const assert = require('node:assert');
const fs = require('fs');
const path = require('path');
const L = require('./localizador');

const RAIZ = path.resolve(__dirname, '..', '..');
const CHUNKS = path.join(RAIZ, 'app', 'src', 'main', 'assets', 'chunks.jsonl');
const CATALOGO = path.join(RAIZ, 'app', 'src', 'main', 'assets', 'magias2versao.json');
const MANUAL = path.join(__dirname, 'magias-localizacao-manual.json');
const TEM_O_LIVRO = fs.existsSync(CHUNKS) && fs.existsSync(CATALOGO);

function livroDe(paginas) {
  return { paginas: new Map(Object.entries(paginas).map(([n, ls]) => [Number(n), ls])) };
}

// ------------------------------------------------------------------------------ 1. unidade

test('normalizar: sem acento, sem caixa, sem pontuação', () => {
  assert.strictEqual(L.normalizar('Projétil'), L.normalizar('PROJETIL'));
  assert.strictEqual(L.normalizar('Bola de Relâmpagos'), 'bola de relampagos');
  assert.strictEqual(L.normalizar('Combustível/NT'), 'combustivel nt');
  assert.strictEqual(L.normalizar(null), '');
});

test('separarVariante e marca de dificuldade', () => {
  assert.deepStrictEqual(L.separarVariante('Atrofiar (Visão)'), { base: 'Atrofiar', variante: 'Visão' });
  assert.deepStrictEqual(L.separarVariante('Baliza(…)'), { base: 'Baliza', variante: '…' });
  assert.deepStrictEqual(L.separarVariante('Sono'), { base: 'Sono', variante: null });
  assert.deepStrictEqual(L.tirarMarcaDeDificuldade('Acelerar (MD)'), { texto: 'Acelerar', muitoDificil: true });
  assert.deepStrictEqual(L.tirarMarcaDeDificuldade('Acelerar*'), { texto: 'Acelerar', muitoDificil: true });
  assert.deepStrictEqual(L.tirarMarcaDeDificuldade('Acelerar'), { texto: 'Acelerar', muitoDificil: false });
  // "(Visão)" é variante, não marca de dificuldade
  assert.strictEqual(L.tirarMarcaDeDificuldade('Atrofiar (Visão)').muitoDificil, false);
});

test('linha de classe: só classe é classe — nome de magia que começa igual NÃO é', () => {
  ['Comum; Resistível com HT', '**Projétil**', '*Encantamento*', 'Comum ou Bloqueio', 'Especial; Projétil',
    'Informação; Área', 'Comum; Resistível com Vontade ou Perícia.', 'Comum/R-HT', '> **Informação**',
  ].forEach((l) => assert.strictEqual(L.ehLinhaDeClasse(l), true, l));
  ['Resistência à Magia', 'Toque Candente', 'Projétil de Pedra', 'Área de efeito: 2 metros.',
    'Especialmente útil contra mortos-vivos.', 'Duração: 1 minuto.', '',
  ].forEach((l) => assert.strictEqual(L.ehLinhaDeClasse(l), false, l));
});

test('rótulo do campo: com negrito, com citação, dentro de célula', () => {
  assert.strictEqual(L.rotuloDoCampo('Duração: 1 hora.'), 'duracao');
  assert.strictEqual(L.rotuloDoCampo('**Duração:** 1 hora.'), 'duracao');
  assert.strictEqual(L.rotuloDoCampo('> **Custo básico:** 3.'), 'custo');
  assert.strictEqual(L.rotuloDoCampo('| **Tempo de operação:** 2 s. | |'), 'tempo');
  assert.strictEqual(L.rotuloDoCampo('**Pré-requisito:** Alfa.'), 'preRequisitos');
  assert.strictEqual(L.rotuloDoCampo('Pré-requisitos: Alfa e Beta.'), 'preRequisitos');
  assert.strictEqual(L.rotuloDoCampo('Uma frase: qualquer.'), null);
});

test('tabela: 9 células; 8 com energia e tempo colados são reparadas; sem página vale', () => {
  const livro = livroDe({
    223: [
      '# Página 223 - Apêndice',
      '| Pág. | Nome da Mágica | Classe | Escola | Duração | Energia | Tempo | Pré-requisitos | Contag. |',
      '|---|---|---|---|---|---|---|---|---|',
      '| 10 | Alfa | Comum/R-HT | Cura | 1 min | 2/1 | 3 seg | Beta | 4 |',
      '| 12 | Gama* | Área | Fogo | Perm | 3 seg=custo | Alfa e 2 outras | 5 |', // 8 células
      '| | Delta | Especial | 4 Diferentes | 1 hora | 4# | 30 seg | AM1 | 8 |', // sem página
      '| xx | Épsilon | Comum | Ar | 1 s | 1 | 1 s | — | 0 |', // página inválida
    ],
  });
  const { linhas, malFormadas } = L.lerTabela(livro);
  assert.strictEqual(linhas.length, 3);
  assert.strictEqual(malFormadas.length, 1);
  const [alfa, gama, delta] = linhas;
  assert.deepStrictEqual([alfa.pagina, alfa.nome, alfa.energia, alfa.tempo, alfa.reparada], [10, 'Alfa', '2/1', '3 seg', null]);
  assert.deepStrictEqual([gama.nome, gama.muitoDificil, gama.energia, gama.tempo, gama.reparada],
    ['Gama', true, '3', 'seg=custo', 'celulas-coladas']);
  assert.deepStrictEqual([delta.pagina, delta.nome, delta.reparada], [null, 'Delta', 'sem-pagina']);
});

const PAG_A_Q = {
  10: [
    '# Página 10 - Escola de Teste', // 0
    '', // 1
    '**Alfa**', // 2   formato A
    'Comum; Resistível com HT', // 3
    'Texto da alfa.', // 4
    'Duração: 1 minuto.', // 5
    'Custo: 2 para operar. 1 para manter.', // 6
    'Tempo de operação: 3 segundos.', // 7
    '', // 8
    '**Item**', // 9
    '(a) Um item qualquer.', // 10
    '', // 11
    '**Beta**', // 12  formato A
    'Comum', // 13
    'Texto da beta.', // 14
    'Duração: 10 minutos.', // 15
    '---', // 16
    '## Gama', // 17  formato B
    '**Projétil**', // 18
    '**Duração:** 1 s.', // 19
    '**Custo:** 1 a 3.', // 20
    '---', // 21
    '**Delta** | Comum', // 22 formato C
    '--- | ---', // 23
    'Texto da delta.', // 24
    '**Duração:** 1 min. | **Pré-requisitos:** Alfa', // 25
    '---', // 26
    '**Épsilon** (Informação)', // 27 formato E
    'Texto.', // 28
    '**Custo:** 4.', // 29
    '---', // 30
    '> **Zeta**', // 31 formato Q (citação)
    '>', // 32
    '> **Informação**', // 33
    '>', // 34
    '> **Custo:** 10.', // 35
  ],
};

test('entradas nos formatos A, B, C, E e Q: título, fim do bloco e onde estão os campos', () => {
  const catalogo = ['Alfa', 'Beta', 'Gama', 'Delta', 'Épsilon', 'Zeta'].map((n) => ({ id: L.normalizar(n), nome: n, pagina: 10 }));
  const r = L.construirIndice(livroDe(PAG_A_Q), catalogo, {});
  const e = (id) => r.magias[id].entrada;
  assert.deepStrictEqual([e('alfa').linhaInicio, e('alfa').linhaFim], [2, 12], 'A: vai até o próximo título e leva o Item');
  assert.deepStrictEqual(Object.keys(e('alfa').campos).sort(), ['custo', 'duracao', 'tempo']);
  assert.deepStrictEqual([e('beta').linhaInicio, e('beta').linhaFim], [12, 16], 'A: para no ---');
  assert.deepStrictEqual([e('gama').linhaInicio, e('gama').linhaFim], [17, 21], 'B: ## + classe em negrito');
  assert.deepStrictEqual([e('delta').linhaInicio, e('delta').linhaFim], [22, 26], 'C: **Nome** | Classe');
  assert.deepStrictEqual([e('epsilon').linhaInicio, e('epsilon').linhaFim], [27, 30], 'E: **Nome** (Classe)');
  assert.deepStrictEqual([e('zeta').linhaInicio, e('zeta').linhaFim], [31, 36], 'Q: dentro de citação');
  assert.ok(e('zeta').campos.custo, 'Q: acha o Custo dentro da citação');
  ['alfa', 'beta', 'gama', 'delta', 'epsilon', 'zeta'].forEach((id) => assert.strictEqual(e(id).formato, 'bloco', id));
});

test('o bloco de uma magia não contém o título da seguinte', () => {
  const catalogo = ['Alfa', 'Beta'].map((n) => ({ id: L.normalizar(n), nome: n, pagina: 10 }));
  const livro = livroDe(PAG_A_Q);
  const r = L.construirIndice(livro, catalogo, {});
  const linhas = L.linhasDoBloco(livro, r.magias.alfa.entrada);
  assert.ok(linhas.some((l) => /Dura[cç][aã]o:/.test(l)));
  assert.ok(!linhas.some((l) => l.includes('**Beta**')), 'Beta é a seguinte e ficou de fora');
  assert.ok(linhas.some((l) => l.includes('**Item**')), 'o Item faz parte da entrada');
});

test('formato F: cada entrada é uma mini-tabela; cabeçalho com VÁRIOS nomes não é título', () => {
  const livro = livroDe({
    10: [
      '# Página 10 - Teste', '',
      '| Eta | Informação | Comum |', // 2
      '|---|---|---|',
      '| Texto da eta. | | |',
      '| **Duração:** 1 min. | | |', // 5
      '', // 6
      '| Teta | Comum |', // 7
      '|---|---|',
      '| Texto da teta. | |',
      '', // 10
      '| Iota | Capa | Kappa |', // 11  — várias magias lado a lado: NÃO é título F
      '|---|---|---|',
      '| texto | texto | texto |',
    ],
  });
  const catalogo = ['Eta', 'Teta', 'Iota'].map((n) => ({ id: L.normalizar(n), nome: n, pagina: 10 }));
  const r = L.construirIndice(livro, catalogo, {});
  assert.deepStrictEqual([r.magias.eta.entrada.formato, r.magias.eta.entrada.linhaInicio, r.magias.eta.entrada.linhaFim], ['bloco', 2, 6]);
  assert.ok(r.magias.eta.entrada.campos.duracao);
  assert.deepStrictEqual([r.magias.teta.entrada.formato, r.magias.teta.entrada.linhaFim], ['bloco', 10]);
  assert.notStrictEqual(r.magias.iota.entrada && r.magias.iota.entrada.formato, 'bloco', 'coluna embaralhada não vira bloco');
});

test('título quebrado em duas linhas só junta se o resultado é um nome conhecido', () => {
  const livro = livroDe({
    10: [
      '# Página 10 - Teste', '',
      '**Kappa**', 'Superior (MD).', 'Comum; Resistível com Vontade', 'Texto.', 'Duração: 1 min.', '',
      '**Lambda**', 'Comum', 'Texto.', 'Duração: 2 min.',
    ],
  });
  const r = L.construirIndice(livro, [
    { id: 'kappa_superior', nome: 'Kappa Superior', pagina: 10 },
    { id: 'lambda', nome: 'Lambda', pagina: 10 },
  ], {});
  assert.strictEqual(r.magias.kappa_superior.entrada.casamento, 'exato');
  assert.deepStrictEqual([r.magias.kappa_superior.entrada.linhaInicio, r.magias.kappa_superior.entrada.linhaFim], [2, 8]);
  // sem "Kappa Superior" no catálogo, "Kappa" + "Superior" NÃO se junta (e Lambda continua título)
  const r2 = L.construirIndice(livro, [{ id: 'lambda', nome: 'Lambda', pagina: 10 }], {});
  assert.strictEqual(r2.magias.lambda.entrada.linhaInicio, 8);
});

test('a entrada atravessa a quebra de página e para no título da seguinte', () => {
  const livro = livroDe({
    20: ['# Página 20 - Teste', '', '**Mi**', 'Comum', 'Texto.', 'Duração: 1 min.'],
    21: ['# Página 21 - Teste', '', 'Custo: 3.', 'Pré-requisito: Alfa.', '', '**Ni**', 'Comum', 'Texto.'],
  });
  const r = L.construirIndice(livro, [{ id: 'mi', nome: 'Mi', pagina: 20 }, { id: 'ni', nome: 'Ni', pagina: 21 }], {});
  const e = r.magias.mi.entrada;
  assert.deepStrictEqual([e.pagina, e.paginaFim, e.linhaFim], [20, 21, 5]);
  assert.strictEqual(L.linhasDoBloco(livro, e).filter((l) => /Custo:/.test(l)).length, 1);
});

test('casamento: exato, base (variante → mãe), genérico, prefixo e aproximado — nessa ordem', () => {
  const tabela = [
    '# Página 223 - Apêndice',
    '| Pág. | Nome | Classe | Escola | Duração | Energia | Tempo | Pré | Contag |',
    '|---|---|---|---|---|---|---|---|---|',
    '| 10 | Alfa | Comum | X | 1 min | 2 | 1 s | — | 0 |',
    '| 10 | Atrofiar | Comum | X | 1 min | 2 | 1 s | — | 0 |',
    '| 11 | Aumentar Atributo | Comum | X | 1 min | 2 | 1 s | — | 0 |',
    '| 12 | Convocar Elemental | Especial | X | 1 h | 4 | 30 s | — | 0 |',
    '| 13 | Aquecer | Área | X | 1 min | 2 | 1 s | — | 0 |',
  ];
  const livro = livroDe({ 223: tabela, 10: ['# Página 10 - X'] });
  const cat = [
    { id: 'a', nome: 'Alfa', pagina: 10 },
    { id: 'b', nome: 'Atrofiar (Visão)', pagina: 10 },
    { id: 'c', nome: 'Aumentar Força', pagina: 11 },
    { id: 'd', nome: 'Convocar Elemental Fogo', pagina: 12 },
    { id: 'e', nome: 'Aquece', pagina: 13 },
    { id: 'f', nome: 'Nada Disso', pagina: 99 },
  ];
  const r = L.construirIndice(livro, cat, {});
  const c = (id) => r.magias[id].tabela && r.magias[id].tabela.casamento;
  assert.deepStrictEqual(['a', 'b', 'c', 'd', 'e'].map(c), ['exato', 'base', 'generico', 'prefixo', 'aproximado']);
  assert.strictEqual(r.magias.b.tabela.variante, 'Visão');
  assert.strictEqual(r.magias.f.tabela, null, 'o que não se acha não se inventa');
});

test('localização manual: só dá a página e a linha — o resto do bloco sai do livro', () => {
  const livro = livroDe({ 10: ['# Página 10 - X', '', '## Sombra (MD)', '**Comum; Resistível com HT**', 'Texto.', 'Duração: 1 min.'] });
  const cat = [{ id: 'corpo_de_sombra', nome: 'Corpo de Sombra', pagina: 10 }];
  const sem = L.construirIndice(livro, cat, {});
  assert.strictEqual(sem.magias.corpo_de_sombra.entrada, null);
  const com = L.construirIndice(livro, cat, { manual: [{ id: 'corpo_de_sombra', pagina: 10, linha: 2, titulo: 'Corpo de Sombra' }] });
  assert.strictEqual(com.magias.corpo_de_sombra.entrada.casamento, 'manual');
  assert.ok(com.magias.corpo_de_sombra.entrada.campos.duracao);
});

// ------------------------------------------------------------------------------ 2. livro real

function indiceReal(fonteDoLocalizador) {
  const lib = fonteDoLocalizador || L;
  const livro = lib.carregarLivro(CHUNKS);
  const catalogo = JSON.parse(fs.readFileSync(CATALOGO, 'utf8'));
  const manual = lib.lerLocalizacaoManual(MANUAL);
  return { livro, catalogo, indice: lib.construirIndice(livro, catalogo, { manual }) };
}

/** O localizador com um pedaço do código cortado — para as SONDAS. */
function localizadorMutilado(trechoParaTirar, nome) {
  const src = fs.readFileSync(path.join(__dirname, 'localizador.js'), 'utf8');
  assert.ok(src.includes(trechoParaTirar), 'a sonda "' + nome + '" não acha o trecho que ela devia cortar');
  const mod = { exports: {} };
  // eslint-disable-next-line no-new-func
  new Function('module', 'exports', 'require', src.replace(trechoParaTirar, ''))(mod, mod.exports, require);
  return mod.exports;
}

/**
 * A CATRACA da cobertura: o que foi medido em 13/out/2026 (879 magias) e subiu em 19/out/2026 (13 páginas reextraídas do PDF, MC-7:
 * localizada 853 -> 871, bloco 814 -> 851). Só sobe. Se o catálogo ou
 * o livro mudarem e a cobertura cair, este teste fica vermelho com os números; se subir, suba aqui.
 */
const CATRACA = { comTabela: 879, localizada: 871, comBlocoETabela: 851 };

test('livro real: a cobertura não cai (catraca: tabela 879, localizada 871, bloco 851)', { skip: !TEM_O_LIVRO }, () => {
  const { indice, catalogo } = indiceReal();
  const r = L.resumir(indice);
  assert.strictEqual(r.total, catalogo.length);
  assert.ok(r.comTabela >= CATRACA.comTabela, 'tabela: ' + r.comTabela + '/' + r.total);
  assert.ok(r.localizada >= CATRACA.localizada, 'localizada (tabela + entrada): ' + r.localizada + '/' + r.total);
  assert.ok(r.comBlocoETabela >= CATRACA.comBlocoETabela, 'bloco recortável: ' + r.comBlocoETabela + '/' + r.total);
  assert.strictEqual(r.semNenhuma, 0, 'toda magia tem ao menos a linha da tabela ou a entrada');
  assert.strictEqual(indice.malFormadas.length, 0, 'toda linha da tabela se lê');
  assert.deepStrictEqual(indice.manuaisNaoResolvidos, [], 'toda localização manual se resolve');
});

test('🟥 sonda: sem normalizar o acento a cobertura CAI abaixo do mínimo (o teste morde)', { skip: !TEM_O_LIVRO }, () => {
  const mutilado = localizadorMutilado(".normalize('NFD').replace(/[\\u0300-\\u036f]/g, '')", 'acento');
  const { indice } = indiceReal(mutilado);
  const r = mutilado.resumir(indice);
  // morde: sem a normalização a cobertura fica ABAIXO da catraca (e o teste de cima ficaria vermelho)
  assert.ok(r.localizada < CATRACA.localizada, 'sem acento a cobertura devia cair: ' + r.localizada);
  assert.ok(r.comBlocoETabela < CATRACA.comBlocoETabela, 'sem acento o bloco devia cair: ' + r.comBlocoETabela);
});

test('🟥 sonda: sem exigir que a PRIMEIRA parte seja classe, "Resistência à Magia" vira linha de classe', () => {
  const mutilado = localizadorMutilado('ehClasse(partes[0]) && ', 'primeira parte');
  assert.strictEqual(mutilado.ehLinhaDeClasse('Resistência à Magia'), true, 'a sonda tem que morder');
  assert.strictEqual(L.ehLinhaDeClasse('Resistência à Magia'), false);
});

test('livro real: as magias com nome parecido com classe/resistência têm bloco (Resistência à Magia, Toque)', { skip: !TEM_O_LIVRO }, () => {
  const { indice, catalogo } = indiceReal();
  ['Resistência à Magia', 'Resistência ao Fogo', 'Toque', 'Toque Candente'].forEach((nome) => {
    const m = catalogo.find((c) => c.nome === nome);
    assert.ok(m, nome + ' está no catálogo');
    const e = indice.magias[m.id].entrada;
    assert.ok(e && e.formato === 'bloco', nome + ' ficou sem bloco');
  });
});

test('livro real: as 20 magias que mais aparecem em jogo estão no índice, com a página da tabela', { skip: !TEM_O_LIVRO }, () => {
  const { indice, catalogo } = indiceReal();
  const nomes = ['Bola de Fogo', 'Relampago', 'Projétil de Pedra', 'Adaga de Gelo', 'Raio Solar', 'Toque Candente',
    'Toque Chocante', 'Jato de Chamas', 'Fender', 'Sono', 'Atordoamento', 'Cegar', 'Medo', 'Paralisia Total',
    'Cura Superficial', 'Cura Profunda', 'Escudo', 'Armadura', 'Acelerar', 'Invisibilidade'];
  assert.strictEqual(nomes.length, 20);
  // Medo (p.134) não tinha a entrada no texto extraído; deixou de faltar em 19/out/2026, quando as 13 páginas
  // foram reextraídas do PDF (MC-7). Se uma destas voltar a faltar, ponha o nome aqui, com a razão.
  const SEM_ENTRADA_NO_TEXTO = [];
  nomes.forEach((n) => {
    const m = catalogo.find((c) => L.normalizar(c.nome) === L.normalizar(n));
    assert.ok(m, n + ' está no catálogo');
    const x = indice.magias[m.id];
    assert.ok(x.tabela, n + ': linha da tabela');
    if (SEM_ENTRADA_NO_TEXTO.includes(n)) {
      assert.strictEqual(x.entrada, null, n + ': o texto extraído não traz a entrada (se passar a trazer, tire daqui)');
      return;
    }
    assert.ok(x.entrada, n + ': entrada');
    assert.strictEqual(x.entrada.pagina, x.tabela.pagina, n + ': a entrada está na página que a tabela manda');
  });
});

test('livro real: páginas lidas à mão no CONFERENCIA batem (inclusive onde o catálogo errava a página)', { skip: !TEM_O_LIVRO }, () => {
  const { indice, catalogo } = indiceReal();
  // [nome no catálogo, página do livro] — da seção 1 e 2 do CONFERENCIA-LIVRO-MG-0 (lido no livro)
  const lidas = [['Audição Remota', 173], ['Eliminar Odor', 24], ['Lealdade', 136], ['Possessão de Máquina/NT', 178],
    ['Torpor', 134], ['Cativar', 139], ['Alimento Essencial', 79], ['Sono Tranquilo', 138], ['Tapete Voador', 146],
    ['Soterramento', 53], ['Passageiro Interno', 31]];
  lidas.forEach(([nome, pagina]) => {
    const m = catalogo.find((c) => c.nome === nome);
    assert.ok(m, nome + ' está no catálogo');
    const x = indice.magias[m.id];
    assert.strictEqual(x.tabela.pagina, pagina, nome + ': página na tabela');
    assert.strictEqual(x.entrada.pagina, pagina, nome + ': página da entrada');
    assert.strictEqual(x.entrada.formato, 'bloco', nome + ': bloco recortável');
  });
});

test('livro real: onde o catálogo errava a página (MC-1 consertou), catálogo, tabela e entrada concordam', { skip: !TEM_O_LIVRO }, () => {
  const { indice, catalogo } = indiceReal();
  // [nome, página do livro]. O catálogo dizia 144 (Passo Leve) e 176 (Possessão de Máquina).
  [['Passo Leve', 145], ['Possessão de Máquina/NT', 178]].forEach(([nome, doLivro]) => {
    const m = catalogo.find((c) => c.nome === nome);
    const x = indice.magias[m.id];
    assert.strictEqual(m.pagina, doLivro, nome + ': catálogo');
    assert.strictEqual(x.tabela.pagina, doLivro, nome + ': tabela');
    assert.strictEqual(x.entrada.pagina, doLivro, nome + ': entrada');
  });
});

test('tabela: página com Energia e Tempo trocados é detectada pelo conteúdo e corrigida', () => {
  const linha = (pag, nome, dur, a, b) => '| ' + pag + ' | ' + nome + ' | Comum | Escola | ' + dur + ' | ' + a + ' | ' + b + ' | Alfa | 1 |';
  const trocada = [];
  for (let i = 0; i < 6; i++) trocada.push(linha(10 + i, 'Troca' + i, '1 min', (i + 1) + ' seg', (i + 2) + '/1'));
  const normal = [];
  for (let i = 0; i < 6; i++) normal.push(linha(20 + i, 'Normal' + i, '1 min', (i + 2) + '/1', (i + 1) + ' seg'));
  const livro = livroDe({ 224: ['# Página 224 - Apêndice'].concat(normal), 225: ['# Página 225 - Apêndice'].concat(trocada) });
  const r = L.lerTabela(livro);
  assert.deepStrictEqual(r.colunasTrocadas, [225]);
  const t = r.linhas.find((x) => x.nome === 'Troca0');
  assert.deepStrictEqual([t.energia, t.tempo, t.reparada], ['2/1', '1 seg', 'colunas-trocadas']);
  const n = r.linhas.find((x) => x.nome === 'Normal0');
  assert.deepStrictEqual([n.energia, n.tempo, n.reparada], ['2/1', '1 seg', null], 'página normal não se mexe');
});

test('livro real: só a página 233 do texto tem Energia e Tempo trocados, e ficam 58 linhas reparadas', { skip: !TEM_O_LIVRO }, () => {
  const livro = L.carregarLivro(CHUNKS);
  const r = L.lerTabela(livro);
  assert.deepStrictEqual(r.colunasTrocadas, [233]);
  assert.strictEqual(r.linhas.filter((t) => /colunas-trocadas/.test(t.reparada || '')).length, 58);
  // a Passo Leve: duração 10 min, energia 4/1#, tempo 1 seg (era "1 seg" na energia)
  const passo = r.linhas.find((t) => t.nome === 'Passo Leve');
  assert.deepStrictEqual([passo.duracao, passo.energia, passo.tempo], ['10 min', '4/1#', '1 seg']);
});

test('livro real: nenhum bloco engole o título de outra magia, e quase todos têm Duração/Custo', { skip: !TEM_O_LIVRO }, () => {
  const { indice, livro } = indiceReal();
  const titulosPorLugar = new Set(indice.titulos.map((t) => t.pagina + ':' + t.linha));
  let comBloco = 0;
  let comCampos = 0;
  const invadidos = [];
  Object.entries(indice.magias).forEach(([id, m]) => {
    const e = m.entrada;
    if (!e || e.formato !== 'bloco') return;
    comBloco++;
    if (e.temCampos) comCampos++;
    for (let p = e.pagina; p <= e.paginaFim; p++) {
      const ls = livro.paginas.get(p);
      const de = p === e.pagina ? e.linhaInicio + 1 : 1;
      const ate = p === e.paginaFim ? e.linhaFim : ls.length;
      for (let i = de; i < ate; i++) {
        // as 3 linhas depois de um título dado à mão são a continuação dele, não outro título
        const continuacao = p === e.pagina && i <= e.linhaInicio + 3 && m.entrada.casamento === 'manual';
        if (titulosPorLugar.has(p + ':' + i) && !continuacao) invadidos.push(id + ' p.' + p + ':' + i);
      }
    }
  });
  assert.deepStrictEqual(invadidos, [], 'bloco que contém o título de outra magia');
  assert.ok(comCampos / comBloco >= 0.9, 'com Duração/Custo: ' + comCampos + '/' + comBloco);
});

test('livro real: o índice commitado é o que o localizador gera hoje (não envelheceu)', { skip: !TEM_O_LIVRO }, () => {
  const arquivo = path.join(__dirname, 'magias-no-livro.json');
  if (!fs.existsSync(arquivo)) return;
  const salvo = JSON.parse(fs.readFileSync(arquivo, 'utf8'));
  const { indice } = indiceReal();
  assert.deepStrictEqual(salvo.resumo, L.resumir(indice), 'rode: node scripts/magias/localizar-magias-no-livro.js');
  assert.deepStrictEqual(Object.keys(salvo.magias), Object.keys(indice.magias));
});
