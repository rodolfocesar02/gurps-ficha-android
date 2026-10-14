'use strict';
/**
 * MC-1 — O cabeçalho do catálogo igual ao livro.
 *
 * Para cada magia com linha na tabela-resumo, decide o que vira o `duracao`, `energia`,
 * `tempoOperacao`, `classe`, `preRequisitos`, `pagina` e (se for erro de digitação) o `nome` —
 * SEMPRE copiando do `chunks.jsonl` por programa, nunca digitando.
 *
 * Três fontes do mesmo livro: C = o catálogo de hoje; T = a linha da tabela-resumo; E = as linhas
 * da própria entrada ("Duração:", "Pré-requisitos:", a linha de classe). A tabela é um ÍNDICE do
 * livro (abreviado e, no texto extraído, às vezes sujo); a entrada é a regra por extenso. Por isso:
 *
 *  - duracao, energia, tempoOperacao, pagina  -> o que a TABELA escreve (é o que o plano manda).
 *    Se o valor for diferente do de hoje, vai para a lista de divergências de VALOR.
 *  - classe -> a da tabela, SALVO quando a entrada discorda dela e concorda com o catálogo (o livro
 *    discorda de si mesmo: vale a entrada) ou quando a da tabela está suja ("Comum/R/-HT").
 *  - preRequisitos -> NÃO muda aqui. O do catálogo é o formato canônico do projeto; a tabela escreve
 *    "AM2" e "#" ("ver a entrada") e o `PreRequisitoParser` do app trata "#" como "não validar":
 *    copiar a tabela tiraria o pré-requisito de dezenas de magias. A diferença de CONTEÚDO (que
 *    magias a entrada exige) é medida à parte, com o parser do app.
 *  - Variantes ("Aumentar Força", "Atrofiar (Visão)") só ganham o que a tabela diz da FAMÍLIA se o
 *    catálogo concordar; senão ficam como estão e entram na lista.
 */
const L = require('./localizador');

const LEITOR = '../../../../../mesa-virtual/server/src/campo/magia-leitor.js';
let leitorCache = null;
/** O leitor da Mesa (o mesmo que a Mesa usa), ou null se a Mesa não está ao lado. */
function leitor() {
  if (leitorCache === null) {
    try { leitorCache = require(LEITOR); } catch (e) { leitorCache = false; }
  }
  return leitorCache || null;
}

const CASAMENTOS_QUE_APLICAM = ['exato', 'aproximado', 'manual', 'prefixo-inverso'];

function semPontoFinal(s) {
  return String(s == null ? '' : s).trim().replace(/\.\s*$/, '').trim();
}

/** Compara dois textos de cabeçalho: 'igual' (idênticos), 'formato' (mesmo valor, outra escrita) ou 'valor'. */
function comparar(campo, antes, depois) {
  const A = String(antes == null ? '' : antes).trim();
  const B = String(depois == null ? '' : depois).trim();
  if (A === B) return 'igual';
  if (L.normalizar(A) === L.normalizar(B)) return 'formato';
  const lei = leitor();
  if (!lei) return 'valor';
  const sig = (o) => { const c = Object.assign({}, o); ['bruto', 'nota', 'listada', 'original', 'texto', 'raw'].forEach((k) => delete c[k]); return JSON.stringify(c); };
  try {
    if (campo === 'duracao') return sig(lei.aDuracaoDaMagia(A)) === sig(lei.aDuracaoDaMagia(B)) ? 'formato' : 'valor';
    if (campo === 'tempoOperacao') return sig(lei.oTempoDeOperacao(A)) === sig(lei.oTempoDeOperacao(B)) ? 'formato' : 'valor';
    if (campo === 'energia') return sig(lei.oCustoDaMagiaComManutencao(A)) === sig(lei.oCustoDaMagiaComManutencao(B)) ? 'formato' : 'valor';
  } catch (e) { return 'valor'; }
  return 'valor';
}

/** "Comum; Resistível com Vontade" (entrada) -> "Comum/R-Vont" (jeito da tabela), para comparar classes. */
function classeDaEntradaNoJeitoDaTabela(texto) {
  let t = semPontoFinal(texto);
  t = t.replace(/\s*;\s*Resist[ií]vel com\s+/i, '/R-').replace(/\s*;\s*Resist[êe]ncia\s+/i, '/R-');
  t = t.replace(/R-Vontade/gi, 'R-Vont').replace(/\s*;\s*/g, '/');
  return t;
}

const ATRIBUTOS_NA_RESISTENCIA = { ht: 'ht', vont: 'von', vontade: 'von', dx: 'dx', st: 'st', iq: 'iq' };

/** A resistência pelo atributo que ela cita ("R-Vont ou perícia" e "R-Vont#" são "von"; o resto, "esp"). */
function resistenciaBase(res) {
  if (!res) return '';
  if (res.atributo) return res.atributo + ':' + (res.mod || 0);
  const primeira = L.normalizar(String(res.especial || '')).split(' ')[0] || '';
  return ATRIBUTOS_NA_RESISTENCIA[primeira] ? ATRIBUTOS_NA_RESISTENCIA[primeira] + ':0' : 'esp';
}

/** As duas classes dizem a mesma coisa (mesmas classes, mesma resistência base)? */
function classesIguais(a, b) {
  const lei = leitor();
  if (!lei) return L.normalizar(a) === L.normalizar(b);
  const pa = lei.aClasseDaMagia(a);
  const pb = lei.aClasseDaMagia(b);
  return JSON.stringify([...pa.classes].sort()) === JSON.stringify([...pb.classes].sort())
    && resistenciaBase(pa.resistencia) === resistenciaBase(pb.resistencia);
}

/** A classe da tabela está legível (só classes do livro + resistência)? "Comum/R/-HT" e "Essencial/Comum" não. */
function classeLegivel(texto) {
  const lei = leitor();
  if (!lei) return /^[\p{L}]+(\/R-[\p{L}0-9+# .-]+)?$/u.test(texto);
  const c = lei.aClasseDaMagia(texto);
  return c.reconhecida === true && !/\/-|\/\//.test(texto);
}

/** Uma duração escrita de forma que NÃO deixa dúvida: "1 hora", "30 segundos", "Permanente", "Indefinida". */
const DURACAO_SEM_DUVIDA = /^(?:\d+(?:[.,]\d+)?\s*(?:seg\w*|min\w*|horas?|dias?|semanas?|m[eê]s(?:es)?|anos?)|permanente|indefinid[ao]|instant[aâ]ne[ao])\s*$/i;
/** Um tempo de operação sem dúvida: só "N segundos/minutos/horas" (nada de "por ponto", "por kg"). */
const TEMPO_SEM_DUVIDA = /^\d+\s*(?:seg\w*|min\w*|horas?)\s*$/i;

/** O texto da entrada é claro o bastante para arbitrar (e o leitor da Mesa o entende)? */
function leituraEntendida(campo, texto) {
  const lei = leitor();
  if (!lei || !texto) return false;
  try {
    if (campo === 'duracao') {
      return DURACAO_SEM_DUVIDA.test(texto) && lei.aDuracaoDaMagia(texto).entendida === true;
    }
    if (campo === 'tempoOperacao') {
      return TEMPO_SEM_DUVIDA.test(texto) && lei.oTempoDeOperacao(texto).entendido === true;
    }
  } catch (e) { return false; }
  return false;
}

/** O texto novo apagaria o custo de manter que o antigo tinha, com o mesmo custo de operar? */
function perdeAManutencao(antes, depois) {
  const lei = leitor();
  if (!lei) return false;
  try {
    const a = lei.oCustoDaMagiaComManutencao(String(antes == null ? '' : antes));
    const d = lei.oCustoDaMagiaComManutencao(String(depois == null ? '' : depois));
    const temA = a.manutencao !== undefined && a.manutencao !== null;
    const temD = d.manutencao !== undefined && d.manutencao !== null;
    const ilegivel = d.desconhecido === true && a.desconhecido !== true; // "6,2": nem o leitor entende
    return (temA && !temD && a.fixo === d.fixo) || ilegivel;
  } catch (e) { return false; }
}

/**
 * A energia, depois do reparo de colunas, é só um TEMPO ("3 seg", "Varia hora=custo")? Então a
 * linha ainda está deslocada. "5/dia", "1/15 seg" e "2 por dia#" são custos de verdade.
 */
function energiaSuspeita(energia) {
  const t = String(energia || '');
  if (/\/|\bpor\b/i.test(t)) return false;
  return /=\s*custo/i.test(t) || /^\s*\d+\s*(?:seg|min|hora|dia|semana)\w*\.?\s*$/i.test(t);
}

/**
 * Decide o cabeçalho de UMA magia.
 * @param m        a magia do catálogo
 * @param x        o item do índice (`tabela`, `entrada`)
 * @param entrada  `L.camposDaEntrada(...)` ({} se não há bloco)
 * @returns `{ aplica, fonte, novo, decisoes, motivo }`
 *   `novo` só tem os campos que mudam de texto; `decisoes` = uma por campo, para o relatório.
 */
function decidir(m, x, entrada) {
  const t = x.tabela;
  const e = entrada || {};
  if (!t) return { aplica: false, fonte: entrada && Object.keys(e).length ? 'entrada' : null, novo: {}, decisoes: [], motivo: 'sem-tabela' };

  const catTemVariante = !!L.separarVariante(m.nome).variante;
  const aplica = CASAMENTOS_QUE_APLICAM.includes(t.casamento) || (t.casamento === 'base' && !catTemVariante);
  const decisoes = [];
  const novo = {};

  const registrar = (campo, antes, depois, tipo, motivo) => {
    decisoes.push({ campo, antes: antes == null ? null : antes, depois: depois == null ? null : depois, tipo, motivo: motivo || null });
  };

  if (!aplica) {
    // família: o livro tem uma linha só para as variantes — só comparamos, não escrevemos
    [['duracao', 'duracao'], ['energia', 'energia'], ['tempoOperacao', 'tempo']].forEach(([campo, k]) => {
      registrar(campo, m[campo], t.campos[k], comparar(campo, m[campo], t.campos[k]), 'variante-de-familia');
    });
    return { aplica: false, fonte: 'tabela-familia', novo, decisoes, motivo: 'variante-de-familia' };
  }

  // ---- duracao, energia, tempoOperacao: o que a tabela escreve
  [['duracao', 'duracao'], ['energia', 'energia'], ['tempoOperacao', 'tempo']].forEach(([campo, k]) => {
    const valor = String(t.campos[k] == null ? '' : t.campos[k]).trim();
    if (!valor) { registrar(campo, m[campo], m[campo], 'igual', 'tabela-vazia'); return; }
    if (campo === 'energia' && energiaSuspeita(valor)) {
      registrar(campo, m[campo], m[campo], 'mantido', 'tabela-suspeita');
      return;
    }
    // duração e tempo: a ENTRADA do livro, quando se entende, arbitra (regra do plano: o livro
    // discorda de si -> vale a entrada). O custo em prosa só o MC-2 sabe ler.
    const daEntrada = campo === 'duracao' ? e.duracao : (campo === 'tempoOperacao' ? e.tempo : null);
    if (daEntrada) {
      const E = semPontoFinal(String(daEntrada).split(/\.\s/)[0]);
      const entendida = leituraEntendida(campo, E);
      if (entendida && comparar(campo, E, valor) === 'valor') {
        if (comparar(campo, m[campo], E) !== 'valor') {
          registrar(campo, m[campo], m[campo], 'mantido', 'o-livro-discorda-de-si: vale a entrada');
          decisoes[decisoes.length - 1].tabela = valor;
          decisoes[decisoes.length - 1].entrada = E;
          return;
        }
        // catálogo, tabela e entrada diferentes: vale a entrada
        const tipoE = comparar(campo, m[campo], E);
        const catalogoIgualATabela = comparar(campo, m[campo], valor) !== 'valor';
        registrar(campo, m[campo], E, tipoE, catalogoIgualATabela
          ? 'o-livro-discorda-de-si: tabela e catalogo contra a entrada, vale a entrada'
          : 'tres-fontes-diferentes: vale a entrada');
        decisoes[decisoes.length - 1].tabela = valor;
        decisoes[decisoes.length - 1].entrada = E;
        if (E !== m[campo]) novo[campo] = E;
        return;
      }
    }
    const tipo = comparar(campo, m[campo], valor);
    if (campo === 'energia' && tipo === 'valor' && perdeAManutencao(m.energia, valor)) {
      // o texto da tabela veio sujo ("6,2" por "6/2") e apagaria o custo de manter
      registrar(campo, m[campo], m[campo], 'mantido', 'tabela-perde-a-manutencao');
      return;
    }
    registrar(campo, m[campo], valor, tipo, t.reparada || null);
    if (tipo !== 'igual') novo[campo] = valor;
  });

  // ---- classe
  {
    const C = m.classe == null ? '' : String(m.classe).trim();
    const T = String(t.campos.classe || '').trim();
    const E = e.classe ? classeDaEntradaNoJeitoDaTabela(e.classe) : null;
    let escolhida = T;
    let motivo = null;
    if (C && C === T) {
      // igual, letra por letra: nada a decidir
      escolhida = C;
    } else if (!T || !classeLegivel(T)) {
      escolhida = C;
      motivo = 'tabela-ilegivel';
    } else if (C && classesIguais(C, T)) {
      // mesmo significado: fica o do catálogo, que às vezes diz MAIS ("R-Vont ou perícia", "R-Vont#")
      escolhida = C;
      motivo = 'mesmo-significado';
    } else if (E && !classesIguais(E, T) && classesIguais(E, C)) {
      escolhida = C;
      motivo = 'o-livro-discorda-de-si: vale a entrada';
    } else if (E && !classesIguais(E, T) && !classesIguais(E, C) && !classesIguais(C, T)) {
      escolhida = C;
      motivo = 'tres-fontes-diferentes';
    }
    const tipo = escolhida === C ? (motivo === 'mesmo-significado' ? 'formato' : (motivo ? 'mantido' : 'igual')) : (L.normalizar(escolhida) === L.normalizar(C) || classesIguais(escolhida, C) ? 'formato' : 'valor');
    registrar('classe', C, escolhida, tipo, motivo);
    if (escolhida !== C) novo.classe = escolhida;
    decisoes[decisoes.length - 1].tabela = T;
    decisoes[decisoes.length - 1].entrada = e.classe || null;
  }

  // ---- preRequisitos: NÃO se mexe neste lote. O do catálogo é o formato canônico do projeto
  // ("AM2, IQ 13+, 2 mágicas…") que o PreRequisitoParser entende; a tabela abrevia e usa "#"
  // (o parser desliga a validação com "#"), e a entrada é prosa. Registra as duas para o relatório.
  {
    const C = m.preRequisitos == null ? '' : String(m.preRequisitos).trim();
    registrar('preRequisitos', C, C, 'mantido', 'formato-canonico-do-projeto');
    decisoes[decisoes.length - 1].tabela = t.campos.preRequisitos;
    decisoes[decisoes.length - 1].entrada = e.preRequisitos ? semPontoFinal(e.preRequisitos) : null;
  }

  // ---- pagina
  {
    const P = t.pagina;
    if (P == null) registrar('pagina', m.pagina, m.pagina, 'mantido', 'tabela-sem-pagina');
    else {
      registrar('pagina', m.pagina, P, m.pagina === P ? 'igual' : 'valor', null);
      if (m.pagina !== P) novo.pagina = P;
    }
  }
  return { aplica: true, fonte: 'tabela', novo, decisoes, motivo: null };
}

// ---------------------------------------------------------------------------------- nomes

/** As palavras do livro (normalizadas) -> quantas vezes aparecem; para achar erro de digitação. */
function vocabulario(livro) {
  const v = new Map();
  livro.paginas.forEach((ls) => ls.forEach((l) => {
    L.normalizar(l).split(' ').forEach((w) => { if (w.length >= 3) v.set(w, (v.get(w) || 0) + 1); });
  }));
  return v;
}

/** Acha, no texto cru do livro, como a frase (normalizada) é escrita; devolve a grafia do livro ou null. */
function grafiaNoLivro(livro, frase) {
  const palavras = frase.split(' ');
  for (const ls of livro.paginas.values()) {
    for (const l of ls) {
      if (!L.normalizar(l).includes(frase)) continue;
      const toks = l.replace(/[*_|>#()\[\]]/g, ' ').split(/\s+/).filter(Boolean);
      for (let i = 0; i + palavras.length <= toks.length; i++) {
        const janela = toks.slice(i, i + palavras.length);
        if (L.normalizar(janela.join(' ')) === frase) return janela.join(' ').replace(/[.,;:]+$/, '');
      }
    }
  }
  return null;
}

/**
 * O nome pelo que o livro escreve, quando o do catálogo tem ERRO DE DIGITAÇÃO. Só troca uma palavra
 * que não existe em lugar nenhum do livro por UMA palavra do livro a 1 letra de distância, e só se a
 * frase corrigida aparece no livro. Devolve a grafia do livro, ou null (não mexe).
 */
function nomeCorrigidoPeloLivro(nome, livro, voc) {
  const palavras = L.normalizar(nome).split(' ');
  let mudou = false;
  const corrigidas = palavras.map((w) => {
    if (w.length < 4 || voc.has(w)) return w;
    const viz = [];
    voc.forEach((n, v) => { if (Math.abs(v.length - w.length) <= 1 && L.distancia(v, w) === 1) viz.push([v, n]); });
    if (viz.length === 0) return w;
    viz.sort((a, b) => b[1] - a[1]);
    mudou = true;
    return viz[0][0];
  });
  if (!mudou) return null;
  return grafiaNoLivro(livro, corrigidas.join(' '));
}

module.exports = {
  leitor,
  comparar,
  classeDaEntradaNoJeitoDaTabela,
  classesIguais,
  classeLegivel,
  energiaSuspeita,
  decidir,
  vocabulario,
  grafiaNoLivro,
  nomeCorrigidoPeloLivro,
  CASAMENTOS_QUE_APLICAM,
};
