'use strict';
/**
 * MC-6 — comparadores campo a campo (catálogo × entrada do livro). Funções PURAS: recebem texto
 * (o do catálogo e o da linha lida do livro) e devolvem o veredito. Quem lê o livro é `livro.js`.
 *
 * Vereditos: `ok` | `erro` | `nao-comparavel` (um dos lados não é número/forma conhecida; NÃO conta
 * como erro nem como acerto — a taxa de erro é erros / (ok + erros)).
 */
const { normalizar } = require('./livro');

/** Todos os números do texto, com sinal ("-15 ou -20 pontos" → [-15, -20]). */
function numeros(txt) {
  return [...String(txt == null ? '' : txt).replace(/\*+/g, '').replace(/[−–—]/g, '-')
    .matchAll(/-?\d+(?:[.,]\d+)?/g)].map((m) => parseFloat(m[0].replace(',', '.')));
}

const unicosOrdenados = (xs) => [...new Set(xs)].sort((a, b) => a - b);
const iguais = (a, b) => a.length === b.length && a.every((x, i) => x === b[i]);
const porNivel = (txt) => /n[ií]vel|n[ií]veis/i.test(String(txt));

/** Tira o que não é custo: asteriscos, barra invertida e o "-1" de "por -1 de Verificação de Pânico". */
function limparCusto(txt) {
  return String(txt == null ? '' : txt).replace(/\\/g, '').replace(/\*+/g, '').replace(/[−–—]/g, '-')
    .replace(/\bpor\s+-?\d+/gi, ' por ').trim();
}

/** "-5 a -15" → [-15, -5]; sem faixa → null. */
function faixaDoTexto(txt) {
  const m = /(-?\d+)\s+a\s+(-?\d+)/i.exec(txt);
  return m ? [parseFloat(m[1]), parseFloat(m[2])].sort((x, y) => x - y) : null;
}

/** O livro escreve a unidade ("5 pontos/boca", "25 pontos/vida"): para o catálogo, "/nível" cobre isso. */
const temUnidade = (txt) => /\d\s*(?:pontos?\s*)?\/\s*[a-zçãõáéíóú]+/i.test(String(txt))
  || /\bpontos?\s+por\b/i.test(String(txt));

/**
 * Custo em pontos de vantagem/desvantagem: `rawCost` do catálogo × linha de custo do livro.
 * Uma FAIXA do livro ("-5 a -15") casa com a lista do catálogo ("-5, -10 ou -15") quando a lista cobre
 * as duas pontas e fica toda dentro dela; "5 pontos/boca" casa com "5/nível" (a unidade é nominal).
 */
function compararCusto(rawCatalogo, linhaDoLivro) {
  const raw = limparCusto(rawCatalogo);
  const livro = limparCusto(linhaDoLivro);
  if (/vari[aá]vel/i.test(raw) || /vari[aá]vel/i.test(livro)) {
    return { veredito: 'nao-comparavel', motivo: 'custo "Variável" num dos lados', catalogo: raw, livro };
  }
  const a = unicosOrdenados(numeros(raw));
  const b = unicosOrdenados(numeros(livro));
  if (!a.length || !b.length) return { veredito: 'nao-comparavel', motivo: 'sem número', catalogo: raw, livro };
  let mesmosNumeros = iguais(a, b);
  if (!mesmosNumeros) {
    const fb = faixaDoTexto(livro);
    const fa = faixaDoTexto(raw);
    const dentro = (xs, f) => f && xs.every((x) => x >= f[0] && x <= f[1]) && xs.includes(f[0]) && xs.includes(f[1]);
    mesmosNumeros = !!((fb && !fa && dentro(a, fb)) || (fa && !fb && dentro(b, fa)));
  }
  // "por nível" e "por unidade" (boca, vida, cultura) contam como o MESMO tipo de custo repetido
  const mesmoNivel = (porNivel(raw) || temUnidade(raw)) === (porNivel(livro) || temUnidade(livro));
  return {
    veredito: mesmosNumeros && mesmoNivel ? 'ok' : 'erro',
    catalogo: raw, livro,
    motivo: !mesmosNumeros ? 'números diferentes' : (!mesmoNivel ? 'por nível diferente' : ''),
  };
}

const ATRIBUTOS = { dx: 'DX', iq: 'IQ', ht: 'HT', st: 'ST', vont: 'VON', vontade: 'VON', von: 'VON', per: 'PER', percepcao: 'PER' };
const DIFICULDADES = { facil: 'F', media: 'M', dificil: 'D', 'muito dificil': 'MD' };

/** "DX/Difícil", "IQ ou DX/Média" → { atributos: ['IQ','DX'], dificuldade: 'M' } (ou null). */
function lerCabecalhoDePericia(txt) {
  const s = String(txt).replace(/\*+/g, '').trim();
  const i = s.lastIndexOf('/');
  if (i < 0) return null;
  const dif = DIFICULDADES[normalizar(s.slice(i + 1))];
  if (!dif) return null;
  const atributos = s.slice(0, i).split(/\s+ou\s+|\s*,\s*|\s*\/\s*/i)
    .map((t) => ATRIBUTOS[normalizar(t).replace(/\s+/g, '')]).filter(Boolean);
  return atributos.length ? { atributos, dificuldade: dif } : null;
}

/** Perícia: atributo base e dificuldade do catálogo × cabeçalho do livro. */
function compararPericia(item, linhaDoLivro) {
  const livro = lerCabecalhoDePericia(linhaDoLivro);
  if (!livro) return { atributo: { veredito: 'nao-comparavel' }, dificuldade: { veredito: 'nao-comparavel' } };
  const poss = [item.atributoBase].concat(item.atributosPossiveis || []).filter(Boolean)
    .map((x) => ATRIBUTOS[normalizar(x).replace(/\s+/g, '')] || String(x).toUpperCase());
  const atributo = livro.atributos.includes(poss[0])
    ? { veredito: 'ok' }
    : { veredito: 'erro', catalogo: item.atributoBase, livro: livro.atributos.join('/') };
  const dificuldade = item.dificuldadeFixa === livro.dificuldade
    ? { veredito: 'ok' }
    : { veredito: 'erro', catalogo: item.dificuldadeFixa, livro: livro.dificuldade };
  return { atributo, dificuldade };
}

/** Dificuldade de técnica: "Média"/"Difícil" do catálogo × linha do livro. */
function compararDificuldadeDeTecnica(rawCatalogo, linhaDoLivro) {
  const a = DIFICULDADES[normalizar(String(rawCatalogo).replace(/\*/g, ''))];
  const b = DIFICULDADES[normalizar(String(linhaDoLivro).replace(/\*/g, ''))];
  if (!a || !b) return { veredito: 'nao-comparavel', catalogo: String(rawCatalogo), livro: String(linhaDoLivro) };
  return a === b ? { veredito: 'ok' } : { veredito: 'erro', catalogo: a, livro: b };
}

/**
 * "Predefinido: Acrobacia-6" → só os números com sinal; compara como conjuntos.
 *  - o livro diz "Especial" ou só "perícia pré-requisito": não há número a conferir (nao-comparavel);
 *  - o livro diz "e outros/outras" ou "(mesma)": ele lista menos que o catálogo — vale se o que o livro
 *    escreve está CONTIDO no catálogo (o catálogo acrescenta, não diverge).
 */
function compararPredefinido(rawCatalogo, textoDoLivro) {
  const a = String(rawCatalogo == null ? '' : rawCatalogo).trim();
  const b = String(textoDoLivro == null ? '' : textoDoLivro).trim();
  if (!b && (!a || a === '-' || /^n[aã]o\b/i.test(a))) return { veredito: 'ok' };
  if (!b || !a || a === '-') return { veredito: 'nao-comparavel', catalogo: a, livro: b };
  if (/^especial\b/i.test(b)) return { veredito: 'nao-comparavel', motivo: 'o livro diz "Especial"', catalogo: a, livro: b };
  const na = unicosOrdenados(numeros(a));
  const nb = unicosOrdenados(numeros(b));
  if (!na.length && !nb.length) return { veredito: 'nao-comparavel', catalogo: a, livro: b };
  if (iguais(na, nb)) return { veredito: 'ok' };
  const incompleto = /\boutr[oa]s?\b|\(mesma\)/i.test(b);
  if (incompleto && nb.length && nb.every((x) => na.includes(x))) {
    return { veredito: 'ok', motivo: 'o catálogo acrescenta ao que o livro lista' };
  }
  return { veredito: 'erro', catalogo: a, livro: b };
}

/** Número como o livro o escreve ("+2", "-1", "0,5") — a forma em que procuramos no bloco. */
function simbolo(n) {
  return String(Math.abs(n)).replace('.', ',');
}

/** O número aparece no bloco como palavra inteira (com ou sem sinal na frente)? */
function numeroNoBloco(blocoTexto, n) {
  const s = String(blocoTexto).replace(/[−–—]/g, '-');
  // não pode ser a parte final de outro número ("1.30", "2,5"); um ponto SOZINHO antes (".30" de calibre) vale
  const re = new RegExp('(?<!\\d[.,]?)' + simbolo(n).replace(',', '[.,]') + '(?![\\d]|[.,]\\d)');
  return re.test(s);
}

/** Todos os números (valor, porOpcao, porAutocontrole) de uma lista de efeitos. */
function numerosDosEfeitos(efeitos) {
  const r = [];
  (efeitos || []).forEach((e) => {
    if (typeof e.valor === 'number') r.push({ n: e.valor, de: e.alvo || e.tipo, campo: 'valor' });
    ['porOpcao', 'porAutocontrole'].forEach((c) => {
      if (e[c] && typeof e[c] === 'object') {
        Object.values(e[c]).forEach((v) => { if (typeof v === 'number') r.push({ n: v, de: e.alvo || e.tipo, campo: c }); });
      }
    });
  });
  return r;
}

/**
 * Efeitos: cada número tem de aparecer no bloco da entrada, e o alvo (perícia/atributo) também.
 * Devolve { veredito, numerosAusentes[], alvosAusentes[], total }.
 */
function compararEfeitos(efeitos, blocoTexto) {
  const lista = numerosDosEfeitos(efeitos);
  if (!lista.length) return { veredito: 'nao-comparavel', total: 0, numerosAusentes: [], alvosAusentes: [] };
  const numerosAusentes = lista.filter((x) => x.n !== 0 && !numeroNoBloco(blocoTexto, x.n))
    .map((x) => ({ numero: x.n, de: x.de }));
  const bloco = normalizar(blocoTexto);
  const alvosAusentes = [];
  (efeitos || []).forEach((e) => {
    if (e.tipo !== 'pericia' || typeof e.alvo !== 'string') return;
    const nome = normalizar(e.alvo.replace(/\([^)]*\)|\/NT|†/g, ' '));
    if (nome.length >= 4 && !bloco.includes(nome) && !alvosAusentes.includes(e.alvo)) alvosAusentes.push(e.alvo);
  });
  return {
    // ⚠️ Só o NÚMERO ausente é erro. O alvo ausente fica como informação: o catálogo expande
    // categorias do livro ("perícias de Influência") em perícias nomeadas que a entrada não escreve.
    veredito: numerosAusentes.length ? 'erro' : 'ok',
    total: lista.length, numerosAusentes, alvosAusentes,
  };
}

/**
 * A `descricao` do catálogo: cada NÚMERO que ela escreve tem de existir no bloco da entrada do livro.
 * É a doença que o catálogo de magias teve (descrição transcrita por IA com número trocado), medida
 * aqui por amostragem. Números de 1 dígito sozinhos ("1", "2") não valem prova: aparecem em todo texto.
 * Devolve { veredito, numerosAusentes[], total }.
 */
function compararDescricao(descricao, blocoTexto) {
  // Linhas de TABELA ("| 3 | 1.000× | 15 pontos |") ficam de fora: a extração do livro costuma perdê-las, e
  // "número que o texto extraído não tem" ali não prova erro do catálogo (é limite da fonte).
  const prosa = String(descricao || '').split('\n').filter((l) => !/^\s*\|/.test(l)).join('\n');
  // remissões a página ("pág. 98", "págs. 99-100", "pág. MB138") não são número de regra
  const semRemissoes = prosa
    .replace(/={4,}\s*PAGE\s*\d+\s*={4,}/gi, ' ') // marca de página que sobrou da extração: é lixo, não regra
    .replace(/p[aá]gs?(?:ina)?s?\.?\s*(?:MB|AM|MA|B)?\s*\d+(?:\s*[-–e,]\s*\d+)*/gi, ' ');
  const brutos = numeros(semRemissoes);
  const lista = unicosOrdenados(brutos.map((n) => Math.abs(n))).filter((n) => n >= 10 || !Number.isInteger(n));
  if (!lista.length) return { veredito: 'nao-comparavel', total: 0, numerosAusentes: [] };
  const ausentes = lista.filter((n) => !numeroNoBloco(blocoTexto, n));
  return { veredito: ausentes.length ? 'erro' : 'ok', total: lista.length, numerosAusentes: ausentes };
}

module.exports = {
  numeros, compararCusto, lerCabecalhoDePericia, compararPericia, compararDificuldadeDeTecnica,
  compararPredefinido, numeroNoBloco, numerosDosEfeitos, compararEfeitos, compararDescricao,
};
