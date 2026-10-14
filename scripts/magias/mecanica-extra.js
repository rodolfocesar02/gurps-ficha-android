'use strict';
/**
 * MC-4 — complementos da conferência da `mecanica`: o dano escrito como FÓRMULA ("1 por dado",
 * "1 ponto de dano por segundo para cada ponto de energia") e a leitura de números em LINHAS que
 * o localizador apontou à mão (`mecanica-lida-na-pagina.json`), para páginas de colunas embaralhadas.
 */
const L = require('./localizador');
const M = require('./mecanica');

/**
 * Dano sem dado no texto: "Custo: 1 por dado de dano causado, até 3" (N de energia por dado) ou
 * "N ponto(s) de dano por segundo para cada ponto de energia". Devolve `{status, ...}` ou null.
 */
function danoPorFormula(m) {
  const k = m.mecanica || {};
  const corpo = M.corpoDoLivro(m);
  if (corpo === null) return null;
  const t = L.normalizar(corpo);
  let r = /\b(\d+)\s+por\s+dado\s+de\s+dano\b/.exec(t);
  if (r) {
    const energiaPorDado = parseInt(r[1], 10);
    const ok = /^\d*d/.test(String(k.danoPorEnergia || '')) && k.energiaPorDado === energiaPorDado;
    return { status: ok ? 'confere' : 'diverge-energia', formula: 'N por dado', energiaPorDado };
  }
  r = /\b(\d+)\s+pontos?\s+de\s+dano\s+por\s+segundo\s+para\s+cada\s+ponto\s+de\s+energia\b/.exec(t);
  if (r) {
    const ok = String(k.danoPorEnergia || '') === String(parseInt(r[1], 10)) && k.energiaPorDado === 1;
    return { status: ok ? 'confere' : 'diverge', formula: 'N ponto por segundo por energia', pontos: parseInt(r[1], 10) };
  }
  return null;
}

/** As linhas (página:linha) que o JSON manual aponta, juntas. */
function linhasApontadas(livro, item) {
  const ls = livro.paginas.get(item.pagina) || [];
  return (item.linhas || []).map((i) => ls[i]).filter((x) => x !== undefined);
}

/**
 * Lê os números das linhas apontadas. `tipo`:
 *  - `projetil`: 1/2D, Máx, Prec;
 *  - `cura`: até N PV, e quantas vezes o valor gasto é restaurado ("o mesmo valor" = 1, "duas vezes" = 2);
 *  - `curaTotal`: "Custo: N."
 */
function lerLinhasApontadas(livro, item) {
  const texto = linhasApontadas(livro, item).join('\n');
  if (item.tipo === 'projetil') return { tipo: 'projetil', numeros: M.numerosDoProjetilNoTexto(texto) };
  if (item.tipo === 'cura') {
    const max = /at[eé]\s+(\d+)\s+PV/i.exec(texto);
    let porEnergia = null;
    if (/o\s+mesmo\s+valor/i.test(texto)) porEnergia = 1;
    else if (/duas\s+vezes\s+o\s+valor/i.test(texto)) porEnergia = 2;
    else if (/tr[eê]s\s+vezes\s+o\s+valor/i.test(texto)) porEnergia = 3;
    return { tipo: 'cura', maxPv: max ? parseInt(max[1], 10) : null, porEnergia };
  }
  if (item.tipo === 'dano') {
    return { tipo: 'dano', dados: M.dadosNoTexto(texto), energiaPorDado: M.energiaPorDadoNoTexto(texto) };
  }
  if (item.tipo === 'curaTotal') {
    const custos = [...texto.matchAll(/Custo:\s*(\d+)\./g)].map((x) => parseInt(x[1], 10));
    return { tipo: 'curaTotal', custo: custos.length ? custos[custos.length - 1] : null };
  }
  return { tipo: item.tipo, numeros: null };
}

module.exports = { danoPorFormula, lerLinhasApontadas, linhasApontadas };
