'use strict';
/**
 * MC-4 — a `mecanica` das magias confrontada com o LIVRO.
 *
 * A `mecanica` (efeito, entrega, danoPorEnergia, 1/2D…) foi curadoria por IA em cima da descrição
 * errada. Não dá para regerar por script, mas dá para CONFRONTAR com o bloco da entrada, que agora é
 * a `descricao` (MC-3): o que o livro escreve é lido por programa, e o que diverge vira lista.
 *
 * Nenhum número é digitado: os que entram vêm do texto do livro.
 */
const L = require('./localizador');

const LEITOR = '../../../../../mesa-virtual/server/src/campo/magia-leitor.js';
let leitorCache = null;
function leitor() {
  if (leitorCache === null) {
    try { leitorCache = require(LEITOR); } catch (e) { leitorCache = false; }
  }
  return leitorCache || null;
}

/** O corpo da magia: a descrição do livro antes do sub-bloco "Item"; null se a descrição não é do livro. */
function corpoDoLivro(m) {
  if (m.descricaoConferida !== true) return null;
  const d = String(m.descricao || '');
  const i = d.search(/\n(?:Item|Itens)\n/);
  return i > 0 ? d.slice(0, i) : d;
}

const NUMERO_POR_EXTENSO = { um: 1, uma: 1, dois: 2, duas: 2, tres: 3, quatro: 4, cinco: 5, seis: 6 };

/** "1d", "1d-1", "2d+1" no texto -> a lista, na ordem em que aparecem. */
function dadosNoTexto(texto) {
  return [...String(texto).matchAll(/\b(\d+d(?:[+-]\d+)?)(?![\w])/g)].map((x) => x[1]);
}

/** "a cada 2 pontos", "por 2 pontos", "para cada dois pontos de energia" -> 2; sem a frase -> null. */
function energiaPorDadoNoTexto(texto) {
  const t = L.normalizar(texto);
  const m = /\b(?:a cada|por|para cada)\s+(\d+|um|uma|dois|duas|tres|quatro|cinco|seis)\s+pontos?\b/.exec(t);
  if (!m) return null;
  return /^\d+$/.test(m[1]) ? parseInt(m[1], 10) : NUMERO_POR_EXTENSO[m[1]];
}

/**
 * Confronta o dano por energia. Devolve `{ status, livro }`:
 *  - `confere`: o dado do catálogo aparece no corpo (e a energia por dado bate quando o texto a diz);
 *  - `diverge`: o texto traz dados e o do catálogo NÃO está entre eles;
 *  - `sem-texto`: a descrição não é do livro (página embaralhada / entrada ausente);
 *  - `sem-dado`: o texto não traz dado nenhum (dano em ponto fixo, "1 ponto de dano").
 */
function conferirDano(m) {
  const k = m.mecanica || {};
  const corpo = corpoDoLivro(m);
  if (corpo === null) return { status: 'sem-texto', livro: null };
  const dados = dadosNoTexto(corpo);
  if (!dados.length) return { status: 'sem-dado', livro: [] };
  const meu = String(k.danoPorEnergia || '').trim();
  if (!/^\d+d/.test(meu)) return { status: 'sem-dado', livro: dados };
  if (!dados.includes(meu)) return { status: 'diverge', livro: dados };
  // o dado escrito na PRÓPRIA linha de custo ("Custo: 1 a 4. Dá 1d+1 de dano a cada ponto…") vale mais que
  // o resto do texto: se é outro que o do catálogo, o livro discorda de si ou o catálogo errou
  const linhasDeCusto = corpo.split('\n').filter((l) => /^Custo/i.test(l.trim()));
  const doCusto = [];
  linhasDeCusto.forEach((l) => l.split(/(?<=[.!?])\s+/).forEach((frase) => {
    // "Dobrando o custo básico, causa 2d-2 por segundo": variante de custo dobrado, não o dado base
    if (/\bdobr|\bduplic|\btriplic/i.test(frase)) return;
    dadosNoTexto(frase).forEach((d) => { if (!doCusto.includes(d)) doCusto.push(d); });
  }));
  if (doCusto.length && !doCusto.includes(meu)) {
    return { status: 'diverge-custo', livro: dados, doCusto };
  }
  const epd = energiaPorDadoNoTexto(corpo);
  if (!k.danoFixo && epd !== null && k.energiaPorDado !== undefined && k.energiaPorDado !== epd) {
    return { status: 'diverge-energia', livro: dados, energiaPorDado: epd };
  }
  return { status: 'confere', livro: dados };
}

/** 1/2D, Máx e Precisão no texto da entrada (os rótulos como o livro escreve). */
function numerosDoProjetilNoTexto(texto) {
  const t = String(texto);
  const pega = (re) => { const m = re.exec(t); return m ? parseFloat(m[1].replace(',', '.')) : null; };
  const meioDano = pega(/1\/2\s*D\s*[:=]?\s*(\d+(?:[.,]\d+)?)/i);
  const maximo = pega(/\bM[aá]x(?:imo)?\.?\s*[:=]?\s*(\d+(?:[.,]\d+)?)/i);
  const precisao = pega(/\bPrec(?:is[aã]o)?\.?\s*[:=]?\s*([+-]?\d+)/i);
  return { meioDano, maximo, precisao };
}

/** Os projéteis (classe Projétil): confronta 1/2D, Máx e Prec do catálogo com o texto da entrada. */
function conferirProjetil(m) {
  const k = m.mecanica || {};
  const corpo = corpoDoLivro(m);
  const meu = { meioDano: k.alcanceMeioDano === undefined ? null : k.alcanceMeioDano,
    maximo: k.alcanceMaximo === undefined ? null : k.alcanceMaximo,
    precisao: k.precisao === undefined ? null : k.precisao };
  if (corpo === null) return { status: 'sem-texto', meu, livro: null };
  const livro = numerosDoProjetilNoTexto(corpo);
  const campos = ['meioDano', 'maximo', 'precisao'];
  const faltam = campos.filter((c) => meu[c] === null && livro[c] !== null);
  const divergem = campos.filter((c) => meu[c] !== null && livro[c] !== null && meu[c] !== livro[c]);
  const naoAchou = campos.filter((c) => meu[c] === null && livro[c] === null);
  let status = 'confere';
  if (divergem.length) status = 'diverge';
  else if (faltam.length) status = 'completar';
  else if (naoAchou.length) status = 'incompleto';
  return { status, meu, livro, faltam, divergem, naoAchou };
}

module.exports = {
  leitor,
  corpoDoLivro,
  dadosNoTexto,
  energiaPorDadoNoTexto,
  conferirDano,
  numerosDoProjetilNoTexto,
  conferirProjetil,
};
