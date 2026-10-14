'use strict';
/**
 * MC-2 — Os números conferidos, refeitos pelo livro.
 *
 * Para cada magia, lê a duração, o tempo de operação e o custo em DUAS fontes independentes do
 * mesmo livro — a linha da tabela-resumo (T) e as linhas "Duração:/Custo:/Tempo de operação:" da
 * entrada (E) — com o MESMO leitor que a Mesa usa, e junta:
 *
 *  - T e E concordam            -> grava ("tabela+entrada");
 *  - T e E discordam            -> VALE A ENTRADA ("entrada"), e a magia entra na lista "o livro
 *                                  discorda de si" (decisão do Rodolfo caso a caso);
 *  - só uma fonte se entende    -> grava com a marca de qual veio ("tabela" ou "entrada");
 *  - fórmula ("1 por dado", "igual ao custo", "Varia") -> NÃO vira número: grava o tipo e a fórmula
 *    lida; quem resolve é o cartão, pedindo a energia ao jogador.
 *
 * Os nomes dos campos que o app e a Mesa já leem continuam os mesmos: `duracaoTipo`, `duracaoSeg`,
 * `tempoOperacaoSeg`, `custoOperar`, `custoManter`. Os novos (`custoTipo`, `custoFormula`,
 * `conferido`, `conferidoPor`…) são acréscimos que os leitores antigos ignoram.
 */
const L = require('./localizador');
const CAB = require('./cabecalho');

const CUSTO_PATH = '../../../../../mesa-virtual/server/src/campo/magia-custo-do-livro.js';
let custoCache = null;
/** O leitor de custo da Mesa, ou null se a Mesa não está ao lado. */
function leitorDeCusto() {
  if (custoCache === null) {
    try { custoCache = require(CUSTO_PATH); } catch (e) { custoCache = false; }
  }
  return custoCache || null;
}

/** A entrada escreve a duração sem deixar dúvida ("1 hora", "Permanente")? Vem de `cabecalho.js`. */
const DURACAO_DA_ENTRADA = /^(?:\d+(?:[.,]\d+)?\s*(?:seg\w*|min\w*|horas?|dias?|semanas?)|permanente|indefinid[ao]|instant[aâ]ne[ao])\s*$/i;
/** O tempo da entrada: "3 segundos", "1 a 3 segundos". */
const TEMPO_DA_ENTRADA = /^\d+(?:\s*a\s*\d+)?\s*(?:seg\w*|min\w*|horas?)\s*$/i;

function primeiraFrase(texto) {
  return String(texto == null ? '' : texto).split(/\.\s|\.$/)[0].replace(/\*\*/g, '').trim();
}

// ---------------------------------------------------------------------------------- duração

/** `{ tipo, segundos, entendida, bruto }` a partir de um texto de duração; `null` sem texto. */
function lerDuracao(texto, daEntrada) {
  const lei = CAB.leitor();
  const t = daEntrada ? primeiraFrase(texto) : String(texto == null ? '' : texto).trim();
  if (!t) return null;
  if (daEntrada && !DURACAO_DA_ENTRADA.test(t)) return { tipo: 'especial', segundos: null, entendida: false, bruto: t };
  const r = lei.aDuracaoDaMagia(t);
  return { tipo: r.tipo, segundos: r.segundos, entendida: r.entendida === true, bruto: t };
}

function mesmaDuracao(a, b) {
  return a.tipo === b.tipo && (a.segundos || 0) === (b.segundos || 0);
}

function resolverDuracao(T, E) {
  const tOk = T && T.entendida;
  const eOk = E && E.entendida;
  let escolhida = null;
  let por = null;
  let discorda = null;
  if (tOk && eOk) {
    if (mesmaDuracao(T, E)) { escolhida = E; por = 'tabela+entrada'; } else { escolhida = E; por = 'entrada'; discorda = { tabela: T.bruto, entrada: E.bruto }; }
  } else if (eOk) { escolhida = E; por = 'entrada'; } else if (tOk) { escolhida = T; por = 'tabela'; }
  if (!escolhida) {
    const bruto = (T && T.bruto) || (E && E.bruto) || '';
    return { campos: bruto ? { duracaoFormula: bruto } : {}, por: 'formula', discorda: null };
  }
  // O vocabulário que o app e a Mesa leem tem 3 tipos (instantanea, temporaria, permanente). "Indefinida"
  // (dura até uma condição da descrição) entra como permanente — o que o app sempre fez — e a palavra
  // do livro fica em duracaoFormula, para não perder a nuance.
  const indefinida = escolhida.tipo === 'indefinida';
  const campos = {
    duracaoTipo: indefinida ? 'permanente' : escolhida.tipo,
    duracaoSeg: escolhida.tipo === 'temporaria' ? escolhida.segundos : 0,
  };
  if (indefinida) campos.duracaoFormula = 'Indefinida';
  return { campos, por, discorda };
}

// ---------------------------------------------------------------------------------- tempo

function lerTempo(texto, daEntrada) {
  const lei = CAB.leitor();
  const t = daEntrada ? primeiraFrase(texto) : String(texto == null ? '' : texto).trim();
  if (!t) return null;
  if (daEntrada && !TEMPO_DA_ENTRADA.test(t)) return { tipo: 'variavel', entendido: false, bruto: t };
  const r = lei.oTempoDeOperacao(t);
  return { tipo: r.tipo, segundos: r.segundos, minimo: r.minimo, maximo: r.maximo, unidade: r.unidade, entendido: r.entendido === true, bruto: t };
}

function mesmoTempo(a, b) {
  return a.tipo === b.tipo && (a.segundos || 0) === (b.segundos || 0) && (a.minimo || 0) === (b.minimo || 0) && (a.maximo || 0) === (b.maximo || 0)
    && (a.unidade || 0) === (b.unidade || 0);
}

function camposDoTempo(r) {
  if (r.tipo === 'fixo') return { tempoOperacaoSeg: r.segundos, tempoTipo: 'fixo' };
  if (r.tipo === 'instantaneo') return { tempoOperacaoSeg: 0, tempoTipo: 'instantaneo' };
  if (r.tipo === 'faixa') return { tempoOperacaoSeg: r.minimo, tempoTipo: 'faixa', tempoMinSeg: r.minimo, tempoMaxSeg: r.maximo };
  if (r.tipo === 'igualAoCusto') return { tempoTipo: 'igualAoCusto', tempoUnidadeSeg: r.unidade, tempoFormula: r.bruto };
  return { tempoTipo: 'varia', tempoFormula: r.bruto };
}

function resolverTempo(T, E) {
  const tOk = T && T.entendido;
  const eOk = E && E.entendido;
  let escolhida = null;
  let por = null;
  let discorda = null;
  if (tOk && eOk) {
    if (mesmoTempo(T, E)) { escolhida = E; por = 'tabela+entrada'; } else { escolhida = E; por = 'entrada'; discorda = { tabela: T.bruto, entrada: E.bruto }; }
  } else if (eOk) { escolhida = E; por = 'entrada'; } else if (tOk) { escolhida = T; por = 'tabela'; }
  if (!escolhida) {
    const bruto = (T && T.bruto) || (E && E.bruto) || '';
    return { campos: bruto ? { tempoTipo: 'varia', tempoFormula: bruto } : {}, por: 'formula', discorda: null };
  }
  return { campos: camposDoTempo(escolhida), por, discorda };
}

// ---------------------------------------------------------------------------------- custo

/** O custo é um NÚMERO (fixo ou faixa) e não uma fórmula? */
function concreto(r) {
  return r.tipo === 'fixo' || r.tipo === 'faixa';
}

function mesmoCusto(a, b) {
  return a.tipo === b.tipo && (a.operar || 0) === (b.operar || 0) && (a.minimo || 0) === (b.minimo || 0) && (a.maximo || 0) === (b.maximo || 0);
}

/** A manutenção em números: "o mesmo custo" vale o custo de operar; "metade" fica "metade". */
function manutencaoEfetiva(r) {
  if (!r.manterTipo) return null;
  if (r.manterTipo === 'igual') return r.operar !== null && r.operar !== undefined ? r.operar : 'igual';
  if (r.manterTipo === 'nao') return 0;
  if (r.manterTipo === 'metade') return 'metade';
  return r.manter;
}

function mesmaManutencao(a, b) {
  const x = manutencaoEfetiva(a);
  const y = manutencaoEfetiva(b);
  if (x === y) return true;
  // "metade" e um número que é a metade (arredondada para cima ou para baixo) dizem a mesma coisa
  const metadeVale = (m, n, r) => m === 'metade' && typeof n === 'number' && r.operar !== null && r.operar !== undefined
    && (n === Math.floor(r.operar / 2) || n === Math.ceil(r.operar / 2));
  return metadeVale(x, y, a) || metadeVale(y, x, b);
}

function camposDoCusto(r, manterTipo, manter) {
  const c = { custoTipo: r.tipo };
  if (r.tipo === 'fixo') {
    c.custoOperar = r.operar;
    if (manterTipo) c.custoManterTipo = manterTipo;
    if (manterTipo === 'nao') c.custoManter = 0;
    else if (manterTipo === 'numero' || manterTipo === 'igual') c.custoManter = manter;
  } else if (r.tipo === 'faixa') {
    c.custoMinimo = r.minimo; c.custoMaximo = r.maximo;
    if (manterTipo) c.custoManterTipo = manterTipo;
    if (manterTipo === 'nao') c.custoManter = 0;
  } else if (r.tipo !== 'naoAplica') {
    if (r.formula) c.custoFormula = r.formula;
    if (manterTipo) c.custoManterTipo = manterTipo;
  }
  return c;
}

/**
 * @param T  `oCustoDaTabela(...)`  @param E  `oCustoDaEntrada(...)` (ou null sem entrada)
 */
function resolverCusto(T, E) {
  const tOk = T && T.entendido;
  const eOk = E && E.entendido;
  if (!tOk && !eOk) {
    const bruto = (T && T.bruto) || (E && E.bruto) || '';
    return { campos: bruto ? { custoTipo: 'varia', custoFormula: bruto } : {}, por: 'formula', discorda: null };
  }
  if (tOk && !eOk) return { campos: camposDoCusto(T, T.manterTipo, T.manter), por: 'tabela', discorda: null };
  if (eOk && !tOk) return { campos: camposDoCusto(E, E.manterTipo, E.manter), por: 'entrada', discorda: null };

  // as duas se entendem
  const tabelaNaoAplica = T.tipo === 'naoAplica';
  if (tabelaNaoAplica) return { campos: camposDoCusto(E, E.manterTipo, E.manter), por: 'entrada', discorda: null };
  // Só é DISCORDÂNCIA quando as duas dão NÚMERO e eles diferem. Tabela vaga ("Varia") e entrada com o
  // número do caso-base, ou o contrário (fórmula na entrada), não é conflito: vale a mais específica.
  if (!concreto(T) || !concreto(E)) {
    if (concreto(E) || !concreto(T)) {
      // a entrada é número, ou as duas são fórmulas: vale a entrada
      return { campos: camposDoCusto(E, E.manterTipo, E.manter), por: T.tipo === E.tipo && !concreto(T) ? 'tabela+entrada' : 'entrada', discorda: null };
    }
    // a tabela é número e a entrada é fórmula ("Qualquer quantia até a Aptidão Mágica"): vale a fórmula
    return { campos: camposDoCusto(E, E.manterTipo, E.manter), por: 'entrada', discorda: null };
  }
  const mesmo = mesmoCusto(T, E);
  // a manutenção: a da entrada se ela escreve; senão a da tabela (que complementa)
  const mE = E.manterTipo || null;
  const mT = T.manterTipo || null;
  let manterTipo = mE || mT;
  let manter = mE ? E.manter : T.manter;
  let discordaManter = false;
  if (mE && mT && !mesmaManutencao(E, T)) discordaManter = true;
  const base = E;
  const campos = camposDoCusto(base, manterTipo, manter);
  if (mesmo && !discordaManter) {
    return { campos, por: (mE && mT) || (!mE && !mT) ? 'tabela+entrada' : (mE ? 'entrada' : 'tabela'), discorda: null };
  }
  return { campos, por: 'entrada', discorda: { tabela: T.bruto, entrada: E.bruto } };
}

// ---------------------------------------------------------------------------------- a magia

const CAMPOS_DOS_NUMEROS = [
  'duracaoTipo', 'duracaoSeg', 'duracaoFormula',
  'tempoOperacaoSeg', 'tempoTipo', 'tempoMinSeg', 'tempoMaxSeg', 'tempoUnidadeSeg', 'tempoFormula',
  'custoOperar', 'custoManter', 'custoManterTipo', 'custoTipo', 'custoMinimo', 'custoMaximo', 'custoFormula',
  'conferido', 'conferidoPagina', 'conferidoPor',
];

/**
 * Deriva os números conferidos de UMA magia.
 * @param m        a magia do catálogo (com o cabeçalho já do livro, MC-1)
 * @param x        o item do índice (`tabela`, `entrada`)
 * @param entrada  `L.camposDaEntrada(...)` ({} se não há bloco)
 * @returns `{ campos, por, discorda, familia }`; `campos` é o que se grava (sem nulos).
 */
function derivar(m, x, entrada, opcoes) {
  const op = opcoes || {};
  const t = x.tabela;
  const e = entrada || {};
  const cc = leitorDeCusto();
  const porSource = {};
  const discorda = {};

  const T = t ? {
    duracao: lerDuracao(t.campos.duracao, false),
    tempo: lerTempo(t.campos.tempo, false),
    custo: cc.oCustoDaTabela(t.campos.energia),
  } : { duracao: null, tempo: null, custo: null };
  const E = {
    duracao: e.duracao ? lerDuracao(e.duracao, true) : null,
    tempo: e.tempo ? lerTempo(e.tempo, true) : null,
    custo: e.custo ? cc.oCustoDaEntrada(e.custo) : null,
  };

  const d = resolverDuracao(T.duracao, E.duracao);
  const te = resolverTempo(T.tempo, E.tempo);
  const c = resolverCusto(T.custo, E.custo);
  porSource.duracao = d.por; porSource.tempoOperacao = te.por; porSource.custo = c.por;
  if (d.discorda) discorda.duracao = d.discorda;
  if (te.discorda) discorda.tempoOperacao = te.discorda;
  if (c.discorda) discorda.custo = c.discorda;

  let campos = Object.assign({}, d.campos, te.campos, c.campos);

  // 🛡️ A CONFERENCIA-LIVRO-MG-0 já LEU, na entrada do livro, o número antigo destas magias e ele estava
  // certo; aqui a entrada não é legível por programa e a tabela (que o livro mesmo contradiz) diz outro.
  // Só se preserva o que JÁ existia, e só contra um número da tabela — nenhum número é digitado.
  const lg = op.legado || null;
  if (lg && op.protegida) {
    if (porSource.duracao === 'tabela' && campos.duracaoTipo && !campos.duracaoFormula && lg.duracaoTipo
        && (lg.duracaoTipo !== campos.duracaoTipo || (lg.duracaoSeg || 0) !== (campos.duracaoSeg || 0))) {
      campos.duracaoTipo = lg.duracaoTipo; campos.duracaoSeg = lg.duracaoSeg || 0; porSource.duracao = 'conferencia';
    }
    if (porSource.tempoOperacao === 'tabela' && campos.tempoTipo === 'fixo' && lg.tempoOperacaoSeg > 0
        && lg.tempoOperacaoSeg !== campos.tempoOperacaoSeg) {
      campos.tempoOperacaoSeg = lg.tempoOperacaoSeg; porSource.tempoOperacao = 'conferencia';
    }
    if (porSource.custo === 'tabela' && campos.custoTipo === 'fixo' && lg.custoOperar !== undefined && lg.custoOperar !== null
        && (lg.custoOperar !== campos.custoOperar || (lg.custoManter === undefined ? null : lg.custoManter) !== (campos.custoManter === undefined ? null : campos.custoManter))) {
      campos.custoOperar = lg.custoOperar;
      delete campos.custoManter; delete campos.custoManterTipo;
      if (lg.custoManter !== undefined && lg.custoManter !== null) {
        campos.custoManter = lg.custoManter;
        campos.custoManterTipo = lg.custoManter === 0 ? 'nao' : 'numero';
      }
      porSource.custo = 'conferencia';
    }
  }

  // Variante de família: o livro só descreve a família. O número só vale se o cabeçalho da própria
  // variante (que o MC-1 deixou como estava) disser o mesmo; senão, nada é escrito para esse campo.
  const familia = !!(x.tabela && CAB.CASAMENTOS_QUE_APLICAM.indexOf(x.tabela.casamento) < 0
    && !(x.tabela.casamento === 'base' && !L.separarVariante(m.nome).variante));
  const naoEscrito = [];
  if (familia) {
    const lei = CAB.leitor();
    const mesmoQueCabecalho = {
      duracao: () => { const h = lei.aDuracaoDaMagia(m.duracao); const r = d.campos; return !r.duracaoTipo || (h.entendida && h.tipo === r.duracaoTipo && (h.segundos || 0) === (r.duracaoSeg || 0)); },
      tempoOperacao: () => { const h = lei.oTempoDeOperacao(m.tempoOperacao); const r = te.campos; return r.tempoOperacaoSeg === undefined || (h.entendido && camposDoTempo(h).tempoOperacaoSeg === r.tempoOperacaoSeg); },
      custo: () => { const h = cc.oCustoDaTabela(m.energia); const r = c.campos; return r.custoOperar === undefined || (h.entendido && h.tipo === 'fixo' && h.operar === r.custoOperar); },
    };
    const grupos = {
      duracao: ['duracaoTipo', 'duracaoSeg', 'duracaoFormula'],
      tempoOperacao: ['tempoOperacaoSeg', 'tempoTipo', 'tempoMinSeg', 'tempoMaxSeg', 'tempoUnidadeSeg', 'tempoFormula'],
      custo: ['custoOperar', 'custoManter', 'custoManterTipo', 'custoTipo', 'custoMinimo', 'custoMaximo', 'custoFormula'],
    };
    // o cabeçalho da variante é IGUAL à linha da família: não tem informação própria da variante, então
    // o número do livro (a entrada, se discorda da tabela) vale para ela também
    const igualATabela = {
      duracao: () => { const h = lei.aDuracaoDaMagia(m.duracao); return !!(T.duracao && T.duracao.entendida && h.entendida && h.tipo === T.duracao.tipo && (h.segundos || 0) === (T.duracao.segundos || 0)); },
      tempoOperacao: () => { const h = lei.oTempoDeOperacao(m.tempoOperacao); return !!(T.tempo && T.tempo.entendido && h.entendido && JSON.stringify(camposDoTempo(h)) === JSON.stringify(camposDoTempo(T.tempo))); },
      custo: () => { const h = cc.oCustoDaTabela(m.energia); return !!(T.custo && T.custo.entendido && h.entendido && mesmoCusto(h, T.custo)); },
    };
    Object.keys(grupos).forEach((g) => {
      if (!mesmoQueCabecalho[g]() && !igualATabela[g]()) {
        grupos[g].forEach((k) => { delete campos[k]; });
        naoEscrito.push(g);
        porSource[g] = 'familia-nao-escrito';
      }
    });
  }

  campos.conferido = 'livro';
  const pagina = (t && t.pagina) || (x.entrada && x.entrada.pagina) || null;
  if (pagina) campos.conferidoPagina = pagina;
  campos.conferidoPor = porSource;
  return { campos, por: porSource, discorda, familia, naoEscrito };
}

module.exports = {
  CAMPOS_DOS_NUMEROS,
  leitorDeCusto,
  lerDuracao,
  lerTempo,
  resolverDuracao,
  resolverTempo,
  resolverCusto,
  derivar,
  primeiraFrase,
};
