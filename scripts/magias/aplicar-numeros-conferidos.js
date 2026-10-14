#!/usr/bin/env node
'use strict';
/**
 * MC-2 — refaz os números conferidos do `magias2versao.json` pelo livro, por programa.
 *
 *   node scripts/magias/aplicar-numeros-conferidos.js --simular
 *   node scripts/magias/aplicar-numeros-conferidos.js --relatorio <caminho.md>
 *
 * Apaga os valores de 17/jul (`duracaoTipo`, `duracaoSeg`, `tempoOperacaoSeg`, `custoOperar`,
 * `custoManter`, que saíram da `descricao` por IA) e gera de novo a partir da tabela e da entrada do
 * livro (ver `numeros.js`). O "antes" do relatório é o catálogo que está no `git HEAD`.
 */
const fs = require('fs');
const path = require('path');
const { execFileSync } = require('child_process');
const L = require('./localizador');
const N = require('./numeros');
const CAB = require('./cabecalho');

const RAIZ = path.resolve(__dirname, '..', '..');
const CHUNKS = path.join(RAIZ, 'app', 'src', 'main', 'assets', 'chunks.jsonl');
const CATALOGO = path.join(RAIZ, 'app', 'src', 'main', 'assets', 'magias2versao.json');
const MANUAL = path.join(__dirname, 'magias-localizacao-manual.json');
const LEGADO = ['duracaoTipo', 'duracaoSeg', 'tempoOperacaoSeg', 'custoOperar', 'custoManter'];

function argumento(nome) {
  const i = process.argv.indexOf(nome);
  return i >= 0 ? process.argv[i + 1] : null;
}

function textoDoCatalogo(arr) {
  return (JSON.stringify(arr, null, 2) + '\n').replace(/\n/g, '\r\n');
}

/** Os ids das magias que a CONFERENCIA-LIVRO-MG-0 (seção 2) leu na entrada do livro e achou certas. */
function protegidasPelaConferencia(catalogo) {
  const caminho = path.resolve(__dirname, '../../../../../mesa-virtual/planos/magias/CONFERENCIA-LIVRO-MG-0.md');
  if (!fs.existsSync(caminho)) return new Set();
  const md = fs.readFileSync(caminho, 'utf8');
  const ini = md.indexOf('## 2.');
  const fim = md.indexOf('## 3.');
  if (ini < 0 || fim < 0) return new Set();
  const nomes = [];
  md.slice(ini, fim).split('\n').forEach((l) => {
    const m = /^\|\s*([^|]+?)\s*\|\s*p\.\d+/.exec(l);
    if (m && m[1] !== 'Magia') m[1].split(/\s*\/\s*/).forEach((n) => nomes.push(L.normalizar(n)));
  });
  const ids = new Set();
  catalogo.forEach((m) => {
    const n = L.normalizar(m.nome);
    if (nomes.some((x) => x === n || (x.length > 6 && n.startsWith(x + ' ')))) ids.add(m.id);
  });
  return ids;
}

function calcular(antes) {
  const livro = L.carregarLivro(CHUNKS);
  const catalogo = JSON.parse(fs.readFileSync(CATALOGO, 'utf8'));
  const manual = L.lerLocalizacaoManual(MANUAL);
  const indice = L.construirIndice(livro, catalogo, { manual });
  const protegidas = protegidasPelaConferencia(catalogo);
  const legado = new Map((antes || []).map((m) => [m.id, m]));
  const resultado = catalogo.map((m) => {
    const x = indice.magias[m.id];
    const entrada = x.entrada && x.entrada.formato === 'bloco' ? L.camposDaEntrada(livro, x.entrada) : {};
    return { m, x, entrada, d: N.derivar(m, x, entrada, { legado: legado.get(m.id), protegida: protegidas.has(m.id) }) };
  });
  return { livro, catalogo, indice, resultado };
}

/** O catálogo de antes: o que está no git HEAD (o MC-1 não tocou nos números conferidos). */
function catalogoDeAntes() {
  try {
    const bruto = execFileSync('git', ['show', 'HEAD:app/src/main/assets/magias2versao.json'], { cwd: RAIZ, maxBuffer: 64 * 1024 * 1024 }).toString('utf8');
    return JSON.parse(bruto);
  } catch (e) {
    return JSON.parse(fs.readFileSync(CATALOGO, 'utf8'));
  }
}

function aplicar(calc) {
  return calc.resultado.map((r) => {
    const m = JSON.parse(JSON.stringify(r.m));
    N.CAMPOS_DOS_NUMEROS.forEach((k) => { delete m[k]; });
    LEGADO.forEach((k) => { delete m[k]; });
    // os campos novos entram no fim do objeto, na ordem de CAMPOS_DOS_NUMEROS
    N.CAMPOS_DOS_NUMEROS.forEach((k) => { if (r.d.campos[k] !== undefined) m[k] = r.d.campos[k]; });
    return m;
  });
}

// ---------------------------------------------------------------------------------- relatório

function md(cabecalho, linhas) {
  if (!linhas.length) return ['_nenhuma_', ''];
  const esc = (v) => String(v == null ? '—' : v).replace(/\|/g, '\\|').replace(/\n/g, ' ');
  return ['| ' + cabecalho.join(' | ') + ' |', '|' + cabecalho.map(() => '---').join('|') + '|']
    .concat(linhas.map((l) => '| ' + l.map(esc).join(' | ') + ' |')).concat(['']);
}

const TXT_DUR = (c) => {
  if (c.duracaoTipo === 'temporaria') return c.duracaoSeg + ' s';
  if (c.duracaoTipo) return c.duracaoTipo;
  return c.duracaoFormula ? '«' + c.duracaoFormula + '»' : '—';
};
const TXT_TEMPO = (c) => {
  if (c.tempoTipo === 'faixa') return c.tempoMinSeg + '–' + c.tempoMaxSeg + ' s';
  if (c.tempoOperacaoSeg !== undefined && c.tempoOperacaoSeg !== null) return c.tempoOperacaoSeg + ' s';
  return c.tempoFormula ? '«' + c.tempoFormula + '»' : '—';
};
const TXT_CUSTO = (c) => {
  if (c.custoTipo === 'fixo' || c.custoOperar !== undefined) {
    const mt = c.custoManterTipo === 'metade' ? ' / manter ½' : (c.custoManter !== undefined && c.custoManter !== null ? ' / manter ' + c.custoManter : '');
    return c.custoOperar + mt;
  }
  if (c.custoTipo === 'faixa') return c.custoMinimo + ' a ' + c.custoMaximo;
  return c.custoFormula ? '«' + c.custoFormula + '»' : (c.custoTipo || '—');
};

function relatorio(calc, ap, antes) {
  const out = [];
  const antesPorId = new Map(antes.map((m) => [m.id, m]));
  const pag = (r) => (r.x.tabela && r.x.tabela.pagina) || r.m.pagina || '—';
  const combate = (r) => {
    const mec = r.m.mecanica || {};
    const cl = String(r.m.classe || '');
    return ['dano', 'cura', 'condicao'].includes(mec.efeito) || /Bloqueio/.test(cl) || /Proj[eé]til/.test(cl);
  };
  const total = calc.catalogo.length;

  // contas
  const cont = { duracao: {}, tempoOperacao: {}, custo: {} };
  let temNumero = { duracao: 0, tempo: 0, custoOperar: 0, custoManter: 0 };
  const mudou = [];
  let ganhou = 0;
  let perdeu = 0;
  const discordam = [];
  const familiaNaoEscrita = [];
  calc.resultado.forEach((r) => {
    Object.keys(r.d.por).forEach((k) => { cont[k] = cont[k] || {}; cont[k][r.d.por[k]] = (cont[k][r.d.por[k]] || 0) + 1; });
    const c = r.d.campos;
    if (c.duracaoTipo) temNumero.duracao++;
    if (c.tempoOperacaoSeg !== undefined) temNumero.tempo++;
    if (c.custoOperar !== undefined) temNumero.custoOperar++;
    if (c.custoManter !== undefined) temNumero.custoManter++;
    const a = antesPorId.get(r.m.id) || {};
    const campos = [
      ['Duração', a.duracaoTipo === undefined ? null : (a.duracaoTipo === 'temporaria' ? a.duracaoSeg + ' s' : a.duracaoTipo), c.duracaoTipo === undefined ? null : (c.duracaoTipo === 'temporaria' ? c.duracaoSeg + ' s' : c.duracaoTipo)],
      ['Tempo de operação', a.tempoOperacaoSeg === undefined ? null : a.tempoOperacaoSeg + ' s', c.tempoOperacaoSeg === undefined ? null : c.tempoOperacaoSeg + ' s'],
      ['Custo de operar', a.custoOperar === undefined ? null : a.custoOperar, c.custoOperar === undefined ? null : c.custoOperar],
      ['Custo de manter', a.custoManter === undefined ? null : a.custoManter, c.custoManter === undefined ? null : c.custoManter],
    ];
    campos.forEach(([nome, antesV, depoisV]) => {
      if (antesV !== null && depoisV !== null && String(antesV) !== String(depoisV)) {
        mudou.push({ r, nome, antes: antesV, depois: depoisV });
      } else if (antesV === null && depoisV !== null) ganhou++;
      else if (antesV !== null && depoisV === null) perdeu++;
    });
    Object.keys(r.d.discorda).forEach((k) => discordam.push({ r, campo: k, d: r.d.discorda[k] }));
    if (r.d.naoEscrito && r.d.naoEscrito.length) familiaNaoEscrita.push(r);
  });

  out.push('# MC-2 — Os números conferidos, refeitos pelo livro');
  out.push('');
  out.push('Gerado por `scripts/magias/aplicar-numeros-conferidos.js` (no app). Os valores de 17/jul (`duracaoTipo`, `duracaoSeg`, `tempoOperacaoSeg`, `custoOperar`, `custoManter`) saíram da `descricao`, transcrita por IA. Foram **apagados** e refeitos por programa, de duas leituras independentes do livro: a linha da tabela-resumo e as linhas "Duração:/Custo:/Tempo de operação:" da entrada. Nada foi digitado à mão; só nome, página e números aparecem aqui.');
  out.push('');
  out.push('## 1. Em números');
  out.push('');
  out.push('| | Com número | Só fórmula/texto |');
  out.push('|---|---|---|');
  out.push('| Duração (`duracaoTipo`) | ' + temNumero.duracao + ' | ' + (total - temNumero.duracao) + ' |');
  out.push('| Tempo de operação (`tempoOperacaoSeg`) | ' + temNumero.tempo + ' | ' + (total - temNumero.tempo) + ' |');
  out.push('| Custo de operar (`custoOperar`) | ' + temNumero.custoOperar + ' | ' + (total - temNumero.custoOperar) + ' |');
  out.push('| Custo de manter (`custoManter`) | ' + temNumero.custoManter + ' | ' + (total - temNumero.custoManter) + ' |');
  out.push('');
  const legadoN = (k) => antes.filter((m) => m[k] !== undefined && m[k] !== null).length;
  out.push('Antes (de 17/jul): duração ' + legadoN('duracaoTipo') + ', tempo ' + legadoN('tempoOperacaoSeg') + ', custo de operar ' + legadoN('custoOperar') + ', custo de manter ' + legadoN('custoManter') + '.');
  out.push('');
  out.push('**De onde veio cada número** (quanto mais para a direita, menos certeza):');
  out.push('');
  const fontes = ['tabela+entrada', 'entrada', 'tabela', 'conferencia', 'formula', 'familia-nao-escrito'];
  md(['Campo'].concat(fontes), ['duracao', 'tempoOperacao', 'custo'].map((k) => [k].concat(fontes.map((f) => cont[k][f] || 0)))).forEach((l) => out.push(l));
  out.push('- `tabela+entrada`: as duas leituras dão o mesmo número. `entrada`: só ela se entende, **ou** o livro discorda de si e vale a entrada. `tabela`: só a tabela se entende. `formula`: não é número (cartão pergunta ao jogador). `conferencia`: o número antigo, que a CONFERENCIA-LIVRO-MG-0 já tinha lido na entrada do livro, foi mantido porque a entrada não é legível por programa e a tabela discorda dela. `familia-nao-escrito`: variante cujo cabeçalho tem informação própria e não bate com a linha da família — nada foi gravado.');
  out.push('');
  out.push('Mudaram de valor **' + mudou.length + '** números (os que já existiam e agora são outros); **' + ganhou + '** passaram a ter número onde não havia; **' + perdeu + '** perderam o número (viraram fórmula ou variante não escrita).');
  out.push('');

  out.push('## 2. 🔴 O que MUDOU de valor, com efeito em jogo (' + mudou.length + ')');
  out.push('');
  out.push('"Combate" = efeito dano/cura/condição, ou classe Bloqueio ou Projétil.');
  out.push('');
  out.push('**Convenção do tempo em faixa** ("1 a 3 seg.", os projéteis): `tempoOperacaoSeg` é o **mínimo** (1), o que o parser do app sempre leu; a faixa fica explícita em `tempoMinSeg`/`tempoMaxSeg`. Em 17/jul o número variava de magia para magia (Bola de Fogo 1, Adaga de Gelo 3), e as linhas "3 s → 1 s" abaixo são só isso. **"1 s → 0 s"**: o livro diz "nenhum" (instantâneo), p.ex. Aumentar Atributo.');
  out.push('');
  const ordenar = (a, b) => (combate(b.r) ? 1 : 0) - (combate(a.r) ? 1 : 0) || a.r.m.nome.localeCompare(b.r.m.nome, 'pt');
  md(['Magia', 'Página', 'Campo', 'Antes (17/jul)', 'Depois (livro)', 'Combate', 'Fonte'],
    mudou.sort(ordenar).map((x) => [x.r.m.nome, pag(x.r), x.nome, x.antes, x.depois, combate(x.r) ? '⚔️' : '',
      x.nome === 'Duração' ? x.r.d.por.duracao : (x.nome === 'Tempo de operação' ? x.r.d.por.tempoOperacao : x.r.d.por.custo)])).forEach((l) => out.push(l));

  out.push('## 3. As 8 que a CONFERENCIA achou erradas');
  out.push('');
  const oito = ['Audição Remota', 'Eliminar Odor', 'Lealdade', 'Possessão de Máquina/NT', 'Torpor', 'Cativar', 'Alimento Essencial', 'Arma Congelante'];
  const linhas8 = [];
  oito.forEach((nome) => {
    const r = calc.resultado.find((x) => x.m.nome === nome);
    if (!r) return;
    const a = antesPorId.get(r.m.id) || {};
    linhas8.push([nome, pag(r),
      TXT_DUR(a) + ' · ' + TXT_TEMPO(a) + ' · ' + TXT_CUSTO(a),
      TXT_DUR(r.d.campos) + ' · ' + TXT_TEMPO(r.d.campos) + ' · ' + TXT_CUSTO(r.d.campos)]);
  });
  md(['Magia', 'Página', 'Antes (duração · tempo · custo)', 'Depois (livro)'], linhas8).forEach((l) => out.push(l));

  out.push('## 4. Onde o livro discorda de si mesmo — vale a entrada (' + discordam.length + ')');
  out.push('');
  out.push('A tabela-resumo e a entrada dão números diferentes. Valeu a **entrada** (é onde o livro explica a magia). Para o Rodolfo decidir caso a caso.');
  out.push('');
  md(['Magia', 'Página', 'Campo', 'Tabela diz', 'Entrada diz', 'Combate'],
    discordam.sort((a, b) => a.r.m.nome.localeCompare(b.r.m.nome, 'pt')).map((x) => [x.r.m.nome, pag(x.r), x.campo, x.d.tabela, x.d.entrada, combate(x.r) ? '⚔️' : ''])).forEach((l) => out.push(l));

  out.push('## 5. Variantes de família: nada foi gravado (' + familiaNaoEscrita.length + ')');
  out.push('');
  out.push('O livro descreve a família numa entrada só. Quando o cabeçalho da variante não diz o mesmo que a família, **não se grava número** (o app usa o texto do cabeçalho da variante).');
  out.push('');
  md(['Magia', 'Página', 'Campos não escritos'], familiaNaoEscrita.map((r) => [r.m.nome, pag(r), r.d.naoEscrito.join(', ')])).forEach((l) => out.push(l));

  out.push('## 6. Fórmulas (não viram número; o cartão pergunta ao jogador)');
  out.push('');
  const tiposCusto = {};
  calc.resultado.forEach((r) => { const t = r.d.campos.custoTipo || 'sem'; tiposCusto[t] = (tiposCusto[t] || 0) + 1; });
  md(['Tipo de custo', 'Magias'], Object.entries(tiposCusto).sort((a, b) => b[1] - a[1]).map(([k, v]) => [k, v])).forEach((l) => out.push(l));
  return { texto: out.join('\n'), mudou, discordam, ganhou, perdeu, temNumero, cont, familia: familiaNaoEscrita.length };
}

function main() {
  const antes = catalogoDeAntes();
  const calc = calcular(antes);
  const novo = aplicar(calc);
  const rel = relatorio(calc, novo, antes);
  const t = rel.temNumero;
  console.log('Catálogo: ' + calc.catalogo.length + ' magias');
  console.log('  com duração ' + t.duracao + ' | tempo ' + t.tempo + ' | custo de operar ' + t.custoOperar + ' | custo de manter ' + t.custoManter);
  console.log('  mudaram de valor: ' + rel.mudou.length + ' | ganharam número: ' + rel.ganhou + ' | perderam: ' + rel.perdeu);
  console.log('  o livro discorda de si: ' + rel.discordam.length + ' | variantes não escritas: ' + rel.familia);
  Object.keys(rel.cont).forEach((k) => console.log('  ' + k + ': ' + JSON.stringify(rel.cont[k])));
  if (process.argv.includes('--simular')) return;
  fs.writeFileSync(CATALOGO, textoDoCatalogo(novo));
  const relPath = argumento('--relatorio');
  if (relPath) fs.writeFileSync(relPath, rel.texto + '\n');
  console.log('Catálogo gravado.' + (relPath ? ' Relatório: ' + relPath : ''));
}

if (require.main === module) main();

module.exports = { calcular, aplicar, relatorio, textoDoCatalogo, catalogoDeAntes, protegidasPelaConferencia, LEGADO };
