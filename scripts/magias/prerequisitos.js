'use strict';
/**
 * MC-1 — compara o CONTEÚDO do pré-requisito do catálogo com o da entrada do livro, sem depender
 * da redação: nível de Aptidão Mágica, quantidades ("pelo menos oito mágicas") e quais magias do
 * catálogo são citadas. Só mede; não escreve nada no catálogo.
 */
const L = require('./localizador');

const NUMEROS = {
  um: 1, uma: 1, dois: 2, duas: 2, tres: 3, quatro: 4, cinco: 5, seis: 6, sete: 7, oito: 8, nove: 9,
  dez: 10, onze: 11, doze: 12, treze: 13, catorze: 14, quinze: 15,
};

/** Aptidão Mágica: "Aptidão Mágica 2", "AM2", "AM 3", "Aptidao Magica 1+" -> número, ou null. */
function nivelAM(texto) {
  const t = L.normalizar(texto);
  const m = /\baptidao magica (\d)\b/.exec(t) || /\bam ?(\d)\b/.exec(t);
  return m ? parseInt(m[1], 10) : null;
}

/** As quantidades de magias exigidas: "5 mágicas de Cura" -> [5]; "pelo menos oito mágicas" -> [8]. */
function quantidades(texto) {
  const t = L.normalizar(texto);
  const saida = [];
  const re = /\b(\d+|um|uma|dois|duas|tres|quatro|cinco|seis|sete|oito|nove|dez|onze|doze|treze|catorze|quinze)\s+(?:outras\s+|diferentes\s+|das\s+)?magicas?\b/g;
  let m;
  while ((m = re.exec(t)) !== null) {
    const n = /^\d+$/.test(m[1]) ? parseInt(m[1], 10) : NUMEROS[m[1]];
    if (n) saida.push(n);
  }
  return saida.sort((a, b) => a - b);
}

/** Índice dos nomes do catálogo (normalizados, maiores primeiro) para achar magias citadas. */
function indiceDeNomes(catalogo) {
  const nomes = new Map();
  catalogo.forEach((m) => {
    [m.nome, L.separarVariante(m.nome).base].forEach((n) => {
      const k = L.normalizar(n);
      if (k.length >= 4 && !nomes.has(k)) nomes.set(k, m.id);
    });
  });
  return [...nomes.entries()].sort((a, b) => b[0].length - a[0].length);
}

/** Os ids das magias do catálogo citadas no texto (o maior nome vence: "Criar Fogo Contínuo" ≠ "Criar Fogo"). */
function magiasCitadas(texto, indice) {
  let t = ' ' + L.normalizar(texto) + ' ';
  const achados = new Set();
  indice.forEach(([nome, id]) => {
    const alvo = ' ' + nome + ' ';
    let i = t.indexOf(alvo);
    while (i >= 0) {
      achados.add(id);
      t = t.slice(0, i) + ' '.repeat(alvo.length) + t.slice(i + alvo.length);
      i = t.indexOf(alvo);
    }
  });
  return achados;
}

/**
 * Compara catálogo (C) e entrada (E). Devolve `null` se o conteúdo é o mesmo, ou
 * `{ am: [c, e]|null, quantidades: [c, e]|null, soNoCatalogo: [ids], soNaEntrada: [ids] }`.
 */
function compararConteudo(C, E, indice) {
  const amC = nivelAM(C);
  const amE = nivelAM(E);
  const qC = quantidades(C);
  const qE = quantidades(E);
  const mC = magiasCitadas(C, indice);
  const mE = magiasCitadas(E, indice);
  const soC = [...mC].filter((x) => !mE.has(x)).sort();
  const soE = [...mE].filter((x) => !mC.has(x)).sort();
  const amDifere = (amC || 0) !== (amE || 0);
  const qDifere = JSON.stringify(qC) !== JSON.stringify(qE);
  if (!amDifere && !qDifere && !soC.length && !soE.length) return null;
  return {
    am: amDifere ? [amC, amE] : null,
    quantidades: qDifere ? [qC, qE] : null,
    soNoCatalogo: soC,
    soNaEntrada: soE,
  };
}

module.exports = { nivelAM, quantidades, indiceDeNomes, magiasCitadas, compararConteudo };
