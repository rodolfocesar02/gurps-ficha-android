'use strict';
/**
 * MC-6 — as TABELAS-RESUMO do próprio livro como segunda testemunha.
 *
 * O Módulo Básico traz, no fim, listas de vantagens/desvantagens (nome, tipo, custo, página) e de
 * perícias (nome, atributo, dificuldade, predefinido, página); Artes Marciais traz a de técnicas
 * (nome, dificuldade, pré-requisito, predefinido, página). Quando a entrada e a tabela dizem a mesma
 * coisa e o catálogo outra, o erro é do catálogo. Quando a entrada e a tabela divergem entre si, "o
 * livro discorda de si" — e não se acusa o catálogo que concorda com uma delas.
 */
const { normalizar } = require('./livro');

const CLASSES = /^(F|M|Soc|S|Sob|Exo|Man|Sup|Fis|Men)$/i;
const DIFS = /^(F|M|D|MD)$/i;
const ATRIBUTOS = /^(DX|IQ|HT|ST|Vont|VON|Per|PER|Vontade)$/i;

const soNumero = (s) => /^\d{1,3}(?:\s*[,;]\s*\d{1,3})*$/.test(String(s).trim());

function adicionar(mapa, nome, valor) {
  const k = normalizar(String(nome).replace(/[†‡*]+/g, ' '));
  if (!k) return;
  if (!mapa.has(k)) mapa.set(k, []);
  mapa.get(k).push(valor);
}

/** Linhas em tabela Markdown: células sem as barras das pontas. */
function celulas(linha) {
  const t = String(linha).trim();
  if (!t.startsWith('|')) return null;
  return t.replace(/^\|/, '').replace(/\|$/, '').split('|').map((c) => c.trim());
}

/**
 * Lê as três tabelas. `paginas` é a saída de `livro.carregarLivro()`.
 * Devolve { custos, pericias, tecnicas }, cada uma Map(nome normalizado → [ocorrências]).
 */
function lerTabelas(paginas) {
  const custos = new Map();
  const pericias = new Map();
  const tecnicas = new Map();

  paginas.forEach((pg) => {
    if (pg.fonte === 'pt_modulo_basico' && pg.pagina >= 296 && pg.pagina <= 306) {
      pg.linhas.forEach((linha) => {
        const c = celulas(linha);
        if (c) {
          // vantagens/desvantagens: [nome, classe, X/Sob/—, custo, pág] repetido
          for (let i = 0; i + 4 < c.length; i += 5) {
            const [nome, cls, , custo, pag] = c.slice(i, i + 5);
            if (nome && CLASSES.test(cls) && custo && soNumero(pag)) {
              adicionar(custos, nome, { custo, pagina: parseInt(pag, 10), pagLivro: pg.pagina });
            }
          }
          // perícias: [nome, atributo, dificuldade, predefinido, pág] repetido
          for (let i = 0; i + 4 < c.length; i += 5) {
            const [nome, atr, dif, pre, pag] = c.slice(i, i + 5);
            if (nome && ATRIBUTOS.test(atr) && DIFS.test(dif) && soNumero(pag)) {
              adicionar(pericias, nome, { atributo: atr, dificuldade: dif.toUpperCase(), predefinido: pre, pagina: parseInt(pag, 10) });
            }
          }
          return;
        }
        // formato sem barras de coluna: "Obsessão M — -1, -5, ou -10* 151, 164 | Único M Sob -5 159"
        linha.split(/\s\|\s/).forEach((trecho) => {
          const m = /^(.+?)\s+(F|M|Soc|S)\s+(—|X|Sob|Exo|Man|Sup|-)\s+(.+?)\s+(\d{1,3}(?:\s*,\s*\d{1,3})*)$/.exec(trecho.trim());
          if (m) adicionar(custos, m[1], { custo: m[4], pagina: parseInt(m[5], 10), pagLivro: pg.pagina });
        });
      });
    }
    if (pg.fonte === 'pt_artes_marciais' && pg.pagina >= 255 && pg.pagina <= 264) {
      pg.linhas.forEach((linha) => {
        const c = celulas(linha);
        if (!c || c.length < 6) return;
        const [nome, dif] = c;
        const pag = c[c.length - 1];
        if (nome && /^(M|D)$/i.test(dif) && soNumero(pag)) {
          adicionar(tecnicas, nome, { dificuldade: dif.toUpperCase(), predefinido: c[3], pagina: parseInt(pag, 10) });
        }
      });
    }
  });
  return { custos, pericias, tecnicas };
}

module.exports = { lerTabelas, celulas };
