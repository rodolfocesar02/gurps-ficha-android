'use strict';
/**
 * Testes dos números conferidos (MC-2). Rodar:  node --test scripts/magias/numeros.test.js
 *
 *  1. UNIDADE — a junção tabela × entrada, com textos sintéticos (nada do livro).
 *  2. LIVRO REAL — o catálogo gravado é exatamente o que o leitor tira do livro agora; as 8 que a
 *     CONFERENCIA achou erradas; e uma SONDA (com os números antigos, as 8 ficam vermelhas).
 * Precisam do leitor da Mesa ao lado; sem ele, pulam.
 */
const test = require('node:test');
const assert = require('node:assert');
const fs = require('fs');
const path = require('path');
const L = require('./localizador');
const CAB = require('./cabecalho');
const N = require('./numeros');

const TEM_LEITOR = !!CAB.leitor() && !!N.leitorDeCusto();
const opcoes = { skip: !TEM_LEITOR };

const RAIZ = path.resolve(__dirname, '..', '..');
const CHUNKS = path.join(RAIZ, 'app', 'src', 'main', 'assets', 'chunks.jsonl');
const CATALOGO = process.env.MC_CATALOGO || path.join(RAIZ, 'app', 'src', 'main', 'assets', 'magias2versao.json');
const TEM_O_LIVRO = TEM_LEITOR && fs.existsSync(CHUNKS) && fs.existsSync(CATALOGO);

const cc = () => N.leitorDeCusto();

// ------------------------------------------------------------------------------ 1. unidade

test('duração: tabela e entrada concordam -> "tabela+entrada"; discordam -> vale a entrada e fica a lista', opcoes, () => {
  const igual = N.resolverDuracao(N.lerDuracao('1 hora', false), N.lerDuracao('1 hora.', true));
  assert.deepStrictEqual(igual.campos, { duracaoTipo: 'temporaria', duracaoSeg: 3600 });
  assert.strictEqual(igual.por, 'tabela+entrada');
  assert.strictEqual(igual.discorda, null);

  const dif = N.resolverDuracao(N.lerDuracao('1 semana', false), N.lerDuracao('1 hora.', true));
  assert.strictEqual(dif.campos.duracaoSeg, 3600, 'vale a entrada');
  assert.strictEqual(dif.por, 'entrada');
  assert.deepStrictEqual(dif.discorda, { tabela: '1 semana', entrada: '1 hora' });
});

test('duração: só uma fonte se entende -> a marca diz qual; nenhuma -> fórmula, sem número', opcoes, () => {
  const soTabela = N.resolverDuracao(N.lerDuracao('10 min', false), N.lerDuracao('Permanece em vigor até que seja quebrada', true));
  assert.deepStrictEqual([soTabela.campos.duracaoSeg, soTabela.por], [600, 'tabela']);
  const soEntrada = N.resolverDuracao(N.lerDuracao('Varia', false), N.lerDuracao('2 dias.', true));
  assert.deepStrictEqual([soEntrada.campos.duracaoSeg, soEntrada.por], [172800, 'entrada']);
  const nenhuma = N.resolverDuracao(N.lerDuracao('Varia', false), null);
  assert.strictEqual(nenhuma.campos.duracaoTipo, undefined);
  assert.strictEqual(nenhuma.campos.duracaoFormula, 'Varia');
  assert.strictEqual(nenhuma.por, 'formula');
});

test('duração: "Indefinida" entra como permanente (o vocabulário do app) e guarda a palavra do livro', opcoes, () => {
  const r = N.resolverDuracao(N.lerDuracao('Indef', false), N.lerDuracao('Indefinida.', true));
  assert.deepStrictEqual(r.campos, { duracaoTipo: 'permanente', duracaoSeg: 0, duracaoFormula: 'Indefinida' });
});

test('tempo: fixo, faixa (o mínimo é o número; a faixa fica explícita), igual ao custo, instantâneo', opcoes, () => {
  assert.deepStrictEqual(N.resolverTempo(N.lerTempo('3 seg', false), N.lerTempo('3 segundos.', true)).campos,
    { tempoOperacaoSeg: 3, tempoTipo: 'fixo' });
  assert.deepStrictEqual(N.resolverTempo(N.lerTempo('1 a 3 seg', false), N.lerTempo('1 a 3 segundos.', true)).campos,
    { tempoOperacaoSeg: 1, tempoTipo: 'faixa', tempoMinSeg: 1, tempoMaxSeg: 3 });
  const igual = N.resolverTempo(N.lerTempo('seg.=custo', false), null);
  assert.strictEqual(igual.campos.tempoOperacaoSeg, undefined, 'fórmula não vira número');
  assert.strictEqual(igual.campos.tempoTipo, 'igualAoCusto');
  assert.strictEqual(N.resolverTempo(N.lerTempo('Instant', false), null).campos.tempoTipo, 'instantaneo');
  // "1 segundo por ponto" na entrada NÃO é um tempo fixo
  assert.strictEqual(N.lerTempo('1 segundo por ponto de energia.', true).entendido, false);
});

test('custo: número onde há número; fórmula onde há fórmula (nunca custoOperar numa fórmula)', opcoes, () => {
  const T = (s) => cc().oCustoDaTabela(s);
  const E = (s) => cc().oCustoDaEntrada(s);
  const fixo = N.resolverCusto(T('4/2'), E('4 para operar. 2 para manter.'));
  assert.deepStrictEqual(fixo.campos, { custoTipo: 'fixo', custoOperar: 4, custoManterTipo: 'numero', custoManter: 2 });
  assert.strictEqual(fixo.por, 'tabela+entrada');

  const formula = N.resolverCusto(T('1 a AM#'), E('Qualquer quantia até o seu nível de Aptidão Mágica por segundo.'));
  assert.strictEqual(formula.campos.custoOperar, undefined);
  assert.strictEqual(formula.campos.custoTipo, 'ateAptidao');

  const porRefeicao = N.resolverCusto(T('3/refeição#'), E('3 por refeição.'));
  assert.strictEqual(porRefeicao.campos.custoOperar, undefined);
  assert.strictEqual(porRefeicao.campos.custoTipo, 'porUnidade');
});

test('custo: "não pode ser mantida" é 0; "metade" NÃO vira número; "igual" é o próprio custo', opcoes, () => {
  const T = (s) => cc().oCustoDaTabela(s);
  const E = (s) => cc().oCustoDaEntrada(s);
  assert.deepStrictEqual([N.resolverCusto(T('5'), E('5. Não pode ser mantida.')).campos.custoManter, N.resolverCusto(T('5'), E('5. Não pode ser mantida.')).campos.custoManterTipo], [0, 'nao']);
  const metade = N.resolverCusto(T('6/M'), E('6 para operar. Metade disso para manter.'));
  assert.strictEqual(metade.campos.custoManter, undefined);
  assert.strictEqual(metade.campos.custoManterTipo, 'metade');
  const igual = N.resolverCusto(T('3/I'), E('3. O mesmo custo para manter.'));
  assert.deepStrictEqual([igual.campos.custoManter, igual.campos.custoManterTipo], [3, 'igual']);
  // sem manutenção escrita em lugar nenhum: "não escrito" (ausente), não zero
  const sem = N.resolverCusto(T('4'), E('4.'));
  assert.strictEqual(sem.campos.custoManter, undefined);
});

test('custo: só é DISCORDÂNCIA quando as duas dão número e eles diferem; tabela vaga não é conflito', opcoes, () => {
  const T = (s) => cc().oCustoDaTabela(s);
  const E = (s) => cc().oCustoDaEntrada(s);
  const real = N.resolverCusto(T('4/1'), E('6 para operar. 3 para manter.'));
  assert.strictEqual(real.campos.custoOperar, 6, 'vale a entrada');
  assert.deepStrictEqual(real.discorda, { tabela: '4/1', entrada: '6 para operar. 3 para manter.' });

  const vaga = N.resolverCusto(T('Varia'), E('3 para operar. 2 para manter.'));
  assert.strictEqual(vaga.campos.custoOperar, 3);
  assert.strictEqual(vaga.discorda, null, '"Varia" na tabela e o número do caso-base na entrada não brigam');

  // a entrada diz "o mesmo custo" e a tabela escreve o número: a mesma manutenção
  const mesmo = N.resolverCusto(T('4/4'), E('4. O mesmo custo para manter.'));
  assert.strictEqual(mesmo.discorda, null);
  // "4/M" na tabela e "2 para manter" na entrada: metade de 4 é 2
  const metade = N.resolverCusto(T('4/M'), E('4 para operar; 2 para manter.'));
  assert.strictEqual(metade.discorda, null);
  assert.strictEqual(metade.campos.custoManter, 2);
});

test('derivar: o que a entrada completa (manutenção) entra; a variante só ganha número que não contradiz', opcoes, () => {
  const x = { tabela: { casamento: 'exato', pagina: 10, campos: { duracao: '1 min', energia: '4', tempo: '2 seg', classe: 'Comum', escola: 'X', preRequisitos: '—' } }, entrada: { pagina: 10 } };
  const m = { id: 'a', nome: 'Alfa', duracao: '1 min', energia: '4', tempoOperacao: '2 seg', pagina: 10 };
  const d = N.derivar(m, x, { duracao: '1 minuto.', custo: '4 para operar. 2 para manter.', tempo: '2 segundos.' });
  assert.strictEqual(d.campos.custoManter, 2, 'a tabela não tinha a manutenção; a entrada completa');
  assert.strictEqual(d.campos.conferido, 'livro');
  assert.strictEqual(d.campos.conferidoPagina, 10);
  // o custo de operar as duas confirmam; a manutenção só a entrada escreve — a marca é a mais fraca
  assert.deepStrictEqual(d.por, { duracao: 'tabela+entrada', tempoOperacao: 'tabela+entrada', custo: 'entrada' });
});

test('derivar: variante de família — o cabeçalho com informação PRÓPRIA da variante impede o número da família', opcoes, () => {
  const x = { tabela: { casamento: 'base', pagina: 33, campos: { duracao: '1 hora', energia: '6', tempo: '3 seg', classe: 'Comum', escola: 'X', preRequisitos: '—' } } };
  const variante = { id: 'v', nome: 'Alfa (Ar)', duracao: '30 min', energia: '6', tempoOperacao: '3 seg', pagina: 33 };
  const d = N.derivar(variante, x, {});
  assert.strictEqual(d.campos.duracaoTipo, undefined, 'a variante diz 30 min; a família, 1 hora: nada é escrito');
  assert.strictEqual(d.por.duracao, 'familia-nao-escrito');
  assert.strictEqual(d.campos.tempoOperacaoSeg, 3, 'o tempo bate com o da família');
  // se o cabeçalho da variante é igual à linha da família, o número vale
  const igual = N.derivar(Object.assign({}, variante, { duracao: '1 hora' }), x, {});
  assert.strictEqual(igual.campos.duracaoSeg, 3600);
});

test('derivar: o número conferido já existente só é preservado contra a TABELA e em magia lida pela CONFERENCIA', opcoes, () => {
  const x = { tabela: { casamento: 'exato', pagina: 10, campos: { duracao: '1 min', energia: '5', tempo: '1 seg', classe: 'Comum', escola: 'X', preRequisitos: '—' } } };
  const m = { id: 'a', nome: 'Alfa', duracao: '1 min', energia: '5', tempoOperacao: '1 seg' };
  const legado = { custoOperar: 8, tempoOperacaoSeg: 60 };
  const sem = N.derivar(m, x, {}, { legado, protegida: false });
  assert.deepStrictEqual([sem.campos.custoOperar, sem.campos.tempoOperacaoSeg], [5, 1], 'sem proteção vale a tabela');
  const com = N.derivar(m, x, {}, { legado, protegida: true });
  assert.deepStrictEqual([com.campos.custoOperar, com.campos.tempoOperacaoSeg], [8, 60]);
  assert.strictEqual(com.por.custo, 'conferencia');
  // …e nunca contra uma fórmula
  const f = { tabela: { casamento: 'exato', pagina: 10, campos: { duracao: '1 min', energia: 'Varia', tempo: 'seg.=custo', classe: 'Comum', escola: 'X', preRequisitos: '—' } } };
  const comF = N.derivar(m, f, {}, { legado, protegida: true });
  assert.strictEqual(comF.campos.custoOperar, undefined);
  assert.strictEqual(comF.campos.tempoOperacaoSeg, undefined);
});

// ------------------------------------------------------------------------------ 2. livro real

function calcularReal() {
  const A = require('./aplicar-numeros-conferidos');
  return A.calcular(A.catalogoDeAntes());
}

/** As 8 que a CONFERENCIA leu no livro: [duração s, tempo s, operar, manter]; null = não se confere. */
const OITO = {
  'Lealdade': [3600, 2, 2, 2],
  'Audição Remota': [60, 3, 4, 2],
  'Eliminar Odor': [3600, null, 2, 2],
  'Possessão de Máquina/NT': [60, 30, 6, 2],
  'Torpor': [60, 2, 3, 2],
  'Cativar': [3600, 1, 3, 3],
  'Alimento Essencial': [null, 30, null, null],
  'Arma Congelante': [60, 3, 3, 1],
};

/** Confere as 8 num catálogo; devolve as magias que não batem. */
function conferirAsOito(catalogo) {
  const erros = [];
  Object.entries(OITO).forEach(([nome, [dur, tmp, op, mt]]) => {
    const m = catalogo.find((c) => c.nome === nome);
    if (!m) { erros.push(nome + ': fora do catálogo'); return; }
    if (dur !== null && !(m.duracaoTipo === 'temporaria' && m.duracaoSeg === dur)) erros.push(nome + ': duração');
    if (tmp !== null && m.tempoOperacaoSeg !== tmp) erros.push(nome + ': tempo');
    if (op !== null && m.custoOperar !== op) erros.push(nome + ': operar');
    if (mt !== null && m.custoManter !== mt) erros.push(nome + ': manter');
  });
  return erros;
}

test('livro real: as 8 magias que estavam erradas saem com os valores do livro', { skip: !TEM_O_LIVRO }, () => {
  const catalogo = JSON.parse(fs.readFileSync(CATALOGO, 'utf8'));
  assert.deepStrictEqual(conferirAsOito(catalogo), []);
});

test('🟥 sonda: com os números ANTIGOS (os da descrição), as 8 ficam vermelhas', { skip: !TEM_LEITOR }, () => {
  const catalogo = JSON.parse(fs.readFileSync(path.join(RAIZ, 'app', 'src', 'main', 'assets', 'magias2versao.json'), 'utf8'));
  // o que o catálogo de 17/jul dizia (CONFERENCIA §1, coluna "Catálogo diz")
  const antigo = JSON.parse(JSON.stringify(catalogo));
  const por = (n) => antigo.find((m) => m.nome === n);
  Object.assign(por('Lealdade'), { duracaoTipo: 'temporaria', duracaoSeg: 604800, tempoOperacaoSeg: 1, custoOperar: 4, custoManter: 2 });
  Object.assign(por('Audição Remota'), { duracaoTipo: 'temporaria', duracaoSeg: 60, tempoOperacaoSeg: 2, custoOperar: 3, custoManter: 1 });
  Object.assign(por('Eliminar Odor'), { custoManter: 1 });
  Object.assign(por('Possessão de Máquina/NT'), { tempoOperacaoSeg: 10, custoManter: 3 });
  Object.assign(por('Torpor'), { tempoOperacaoSeg: 1 });
  Object.assign(por('Cativar'), { tempoOperacaoSeg: 5 });
  Object.assign(por('Alimento Essencial'), { tempoOperacaoSeg: 1 });
  Object.assign(por('Arma Congelante'), { tempoOperacaoSeg: 2, custoOperar: 4 });
  const erros = conferirAsOito(antigo);
  const magias = new Set(erros.map((e) => e.split(':')[0]));
  assert.strictEqual(magias.size, 8, 'as 8 ficam vermelhas: ' + erros.join('; '));
});

test('livro real: o catálogo gravado É o que o leitor tira do livro agora (879, campo a campo)', { skip: !TEM_O_LIVRO }, () => {
  const calc = calcularReal();
  const erros = [];
  calc.resultado.forEach((r) => {
    N.CAMPOS_DOS_NUMEROS.forEach((k) => {
      const esperado = r.d.campos[k];
      const gravado = r.m[k];
      if (JSON.stringify(esperado) !== JSON.stringify(gravado)) erros.push(r.m.nome + ' (p.' + (r.x.tabela && r.x.tabela.pagina) + '): ' + k + ' gravado ' + JSON.stringify(gravado) + ' ≠ livro ' + JSON.stringify(esperado));
    });
  });
  assert.deepStrictEqual(erros.slice(0, 10), [], 'números fora do livro em ' + erros.length + ' ponto(s)');
});

test('livro real: nenhum número antigo de 17/jul sobrou sem origem (toda magia tem `conferido: livro`)', { skip: !TEM_O_LIVRO }, () => {
  const catalogo = JSON.parse(fs.readFileSync(CATALOGO, 'utf8'));
  const semOrigem = catalogo.filter((m) => m.conferido !== 'livro' || !m.conferidoPor).map((m) => m.nome);
  assert.deepStrictEqual(semOrigem, []);
  // e nada de custoOperar numa fórmula
  const ruins = catalogo.filter((m) => m.custoOperar !== undefined && m.custoTipo !== 'fixo').map((m) => m.nome);
  assert.deepStrictEqual(ruins, []);
});

test('livro real: a cobertura dos números não cai (catraca)', { skip: !TEM_O_LIVRO }, () => {
  const catalogo = JSON.parse(fs.readFileSync(CATALOGO, 'utf8'));
  const c = (k) => catalogo.filter((m) => m[k] !== undefined && m[k] !== null).length;
  assert.ok(c('duracaoTipo') >= 846, 'duração ' + c('duracaoTipo'));
  assert.ok(c('tempoOperacaoSeg') >= 826, 'tempo ' + c('tempoOperacaoSeg'));
  assert.ok(c('custoOperar') >= 542, 'custo de operar ' + c('custoOperar'));
  assert.ok(c('custoManter') >= 317, 'custo de manter ' + c('custoManter'));
});

// ------------------------------------------------------------------------------ 3. o texto da tela

const camposFixo = (operar, manterTipo, manter) => {
  const c = { custoTipo: 'fixo', custoOperar: operar };
  if (manterTipo) { c.custoManterTipo = manterTipo; if (manter !== undefined) c.custoManter = manter; }
  return c;
};

test('tela: o cabeçalho (tabela) diz um número, o conferido (entrada) outro -> grava o texto do conferido', opcoes, () => {
  // Desintegrar: "1 a 4" na tabela, "1 a 10" na entrada
  assert.strictEqual(N.energiaDaTela({ energia: '1 a 4' }, { custoTipo: 'faixa', custoMinimo: 1, custoMaximo: 10 }), '1 a 10');
  // Arma Flamejante: "4/1" na tabela, "6 para operar, 3 para manter" na entrada
  assert.strictEqual(N.energiaDaTela({ energia: '4/1' }, camposFixo(6, 'numero', 3)), '6/3');
  // "metade" fica "/M" (o arredondamento é regra do MG-3, não do texto); o "#" do cabeçalho é mantido
  assert.strictEqual(N.energiaDaTela({ energia: '6/M#' }, camposFixo(1, 'metade')), '1/M#');
  // "não pode ser mantida" é "/0"
  assert.strictEqual(N.energiaDaTela({ energia: '8/4' }, camposFixo(5, 'nao', 0)), '5/0');
  // a fórmula do cabeçalho ("1 por IQ-") sai quando o conferido é uma faixa
  assert.strictEqual(N.energiaDaTela({ energia: '1 por IQ-/M' }, { custoTipo: 'faixa', custoMinimo: 1, custoMaximo: 5, custoManterTipo: 'metade' }), '1 a 5/M');
});

test('tela: onde o cabeçalho concorda com o conferido, ou não diz número, NADA é gravado', opcoes, () => {
  assert.strictEqual(N.energiaDaTela({ energia: '4/2' }, camposFixo(4, 'numero', 2)), null, 'concordam');
  assert.strictEqual(N.energiaDaTela({ energia: '1 a 3/I' }, { custoTipo: 'faixa', custoMinimo: 1, custoMaximo: 3 }), null, 'faixa igual, com sufixo');
  assert.strictEqual(N.energiaDaTela({ energia: 'Varia' }, camposFixo(3, 'numero', 2)), null, '"Varia": o jogador escolhe');
  assert.strictEqual(N.energiaDaTela({ energia: 'Varia' }, { custoTipo: 'faixa', custoMinimo: 1, custoMaximo: 3 }), null);
  assert.strictEqual(N.energiaDaTela({ energia: '4' }, camposFixo(4, 'metade')), null, '"metade" não discorda de um cabeçalho sem manutenção');
});

test('tela: duração e tempo, na gramática do cabeçalho', opcoes, () => {
  // criar_elemental: a tabela diz "Perm.", a entrada diz 1 hora
  assert.strictEqual(N.duracaoDaTela({ duracao: 'Perm.' }, { duracaoTipo: 'temporaria', duracaoSeg: 3600 }), '1 hora');
  assert.strictEqual(N.duracaoDaTela({ duracao: '1 min' }, { duracaoTipo: 'temporaria', duracaoSeg: 60 }), null);
  assert.strictEqual(N.duracaoDaTela({ duracao: '1 min' }, { duracaoTipo: 'temporaria', duracaoSeg: 7200 }), '2 horas');
  assert.strictEqual(N.duracaoDaTela({ duracao: 'Varia' }, { duracaoTipo: 'temporaria', duracaoSeg: 60 }), null, 'ilegível não é reescrito');
  // chuva_de_fogo: a tabela diz "1 seg", o conferido é 60 s
  assert.strictEqual(N.tempoDaTela({ tempoOperacao: '1 seg' }, { tempoTipo: 'fixo', tempoOperacaoSeg: 60 }), '1 min.');
  assert.strictEqual(N.tempoDaTela({ tempoOperacao: '3 seg.' }, { tempoTipo: 'fixo', tempoOperacaoSeg: 3 }), null);
  assert.strictEqual(N.tempoDaTela({ tempoOperacao: '1 a 3 seg.' }, { tempoTipo: 'fixo', tempoOperacaoSeg: 2 }), null, 'faixa continua faixa');
  assert.strictEqual(N.segundosEmTexto(90), '90 seg.');
  assert.strictEqual(N.segundosEmTexto(86400), '1 dia');
  assert.strictEqual(N.segundosEmTexto(172800), '2 dias');
  assert.strictEqual(N.segundosEmTexto(604800), '1 semana');
});

test('livro real: Desintegrar tem "1 a 10" na tela e "1 a 4" no cabeçalho; nenhuma tela repete o cabeçalho', { skip: !TEM_O_LIVRO }, () => {
  const catalogo = JSON.parse(fs.readFileSync(CATALOGO, 'utf8'));
  const des = catalogo.find((m) => m.id === 'desintegrar');
  assert.strictEqual(des.energia, '1 a 4');
  assert.strictEqual(des.energiaTela, '1 a 10');
  // nenhuma tela repete o cabeçalho (seria ruído) e toda tela vem de um número conferido
  catalogo.forEach((m) => {
    if (m.energiaTela) { assert.notStrictEqual(m.energiaTela, m.energia, m.id); assert.ok(m.custoTipo === 'fixo' || m.custoTipo === 'faixa', m.id); }
    if (m.duracaoTela) { assert.notStrictEqual(m.duracaoTela, m.duracao, m.id); assert.ok(m.duracaoTipo, m.id); }
    if (m.tempoOperacaoTela) { assert.notStrictEqual(m.tempoOperacaoTela, m.tempoOperacao, m.id); assert.ok(m.tempoOperacaoSeg !== undefined, m.id); }
  });
});

test('🟥 sonda: o catálogo gravado É o que `derivar` tira do livro, com a tela e tudo (nada à mão)', { skip: !TEM_O_LIVRO }, () => {
  const catalogo = JSON.parse(fs.readFileSync(CATALOGO, 'utf8'));
  const livro = L.carregarLivro(CHUNKS);
  const indice = L.construirIndice(livro, catalogo, { manual: L.lerLocalizacaoManual(path.join(__dirname, 'magias-localizacao-manual.json')) });
  const erros = [];
  catalogo.forEach((m) => {
    const x = indice.magias[m.id];
    const entrada = x.entrada && x.entrada.formato === 'bloco' ? L.camposDaEntrada(livro, x.entrada) : {};
    const d = N.derivar(m, x, entrada, { legado: m, protegida: m.conferidoPor && Object.values(m.conferidoPor).includes('conferencia') });
    ['energiaTela', 'duracaoTela', 'tempoOperacaoTela'].forEach((k) => {
      if ((d.campos[k] || null) !== (m[k] || null)) erros.push(m.id + ' ' + k + ': gravado ' + m[k] + ' × livro ' + d.campos[k]);
    });
  });
  assert.deepStrictEqual(erros.slice(0, 10), [], 'tela fora do livro em ' + erros.length + ' ponto(s)');
});
