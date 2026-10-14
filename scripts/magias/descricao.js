'use strict';
/**
 * MC-3 — A `descricao` da magia, recortada do livro por programa (opção A).
 *
 * A descrição passa a ser o bloco da ENTRADA do livro: da linha de classe (o título é o `nome`) até
 * o fim do bloco, o "Item" incluído. O markdown do `chunks.jsonl` vira texto simples e os parágrafos
 * ficam como estão. Nenhuma palavra é digitada: o texto vai do `chunks.jsonl` para o catálogo.
 *
 * O formato é o que o app já lê: a primeira linha é a classe, depois o corpo, depois "Duração:",
 * "Custo:", "Tempo de operação:", "Pré-requisitos:" e o "Item" (o `resumoDaDescricao` do combate
 * corta por esses rótulos).
 */
const L = require('./localizador');

const SEPARADOR = /^\|?[\s|:-]+\|?$/;

/** Tira as marcas de markdown de UMA linha de texto. */
function limparLinha(linha) {
  let t = String(linha).replace(/\r/g, '');
  t = t.replace(/^(?:>\s*)+/, '');
  t = t.replace(/^#{1,6}\s*/, '');
  t = t.replace(/^[-+]\s+/, '');
  t = t.replace(/\*\*/g, '').replace(/__/g, '');
  // itálico de uma palavra ou frase: *texto*
  t = t.replace(/(^|[\s(])\*([^*\s][^*]*?)\*(?=$|[\s).,;:])/g, '$1$2');
  return t.replace(/[ \t]+$/g, '').replace(/^[ \t]+/g, '');
}

const COMECA_COM_CLASSE = /^(?:comum|area|projetil|toque|bloqueio|informacao|encantamento|especial)\b/;

/**
 * A linha é a classe da magia? A forma estrita do localizador, ou uma linha curta que começa por uma
 * classe e não é frase ("Comum; Resiste à Cura", "Área Arbóreo"): a classe nunca termina em ponto.
 */
function pareceClasse(linha) {
  if (L.ehLinhaDeClasse(linha)) return true;
  const t = limparLinha(linha);
  return t.length > 0 && t.length <= 120 && !/[.!?]$/.test(t) && COMECA_COM_CLASSE.test(L.normalizar(t));
}

/** Uma linha de tabela markdown -> as células com texto (cada uma vira uma linha). */
function celulasDaLinha(linha) {
  return String(linha).trim().replace(/^\|/, '').replace(/\|\s*$/, '').split('|').map((c) => limparLinha(c)).filter((c) => c !== '');
}

/**
 * Recorta a descrição de uma entrada em bloco. Devolve `{ texto, classe, paginaInicio, paginaFim }`
 * ou `null` se o bloco não dá uma descrição (sem corpo).
 */
function recortarDescricao(livro, entrada) {
  if (!entrada || entrada.formato !== 'bloco') return null;
  const brutas = L.linhasDoBloco(livro, entrada);
  if (!brutas.length) return null;
  const saida = [];
  let i = 0;

  const primeira = brutas[0].trim();
  const ehTabelaF = primeira.startsWith('|') && !SEPARADOR.test(primeira);

  if (ehTabelaF) {
    // formato F: `| Nome | Classe | … |`, `|---|`, linhas de texto em células
    const cab = celulasDaLinha(primeira);
    cab.slice(1).forEach((c) => saida.push(c)); // a(s) classe(s); a 1ª célula é o título
    i = 1;
  } else {
    // título (+ continuação do título, + classe)
    const titulo = limparLinha(primeira);
    let j = 1;
    const emTitulo = /\|/.test(primeira) || /\*\*\s*\(/.test(primeira);
    if (emTitulo) {
      // formato C (`**Nome** | Classe`) ou E (`**Nome** (Classe)`): a classe está na própria linha
      const c = /\|\s*(.+?)\s*\|?$/.exec(primeira);
      const e = /\(([^)]+)\)\s*$/.exec(primeira);
      const classe = c ? limparLinha(c[1]) : (e ? limparLinha(e[1]) : null);
      if (classe) saida.push(classe);
    } else {
      // pula linhas vazias e a continuação do título até a linha de classe
      let achou = false;
      let k = 1;
      const candidatas = [];
      while (k < brutas.length && candidatas.length < 4) {
        const t = brutas[k].trim();
        k++;
        if (!limparLinha(t)) continue;
        candidatas.push({ k: k - 1, t });
        if (pareceClasse(t)) { achou = true; break; }
      }
      if (achou) {
        const ultima = candidatas[candidatas.length - 1];
        saida.push(limparLinha(ultima.t));
        j = ultima.k + 1;
      } else {
        j = 1; // sem linha de classe: o corpo começa logo depois do título
      }
    }
    void titulo;
    i = j;
  }

  let emTabela = false;
  let suspeita = null;
  for (; i < brutas.length; i++) {
    const bruta = brutas[i];
    const t = bruta.trim();
    if (t === '---' || /^(?:>\s*)*---\s*$/.test(t)) continue;
    if (t.startsWith('|')) {
      emTabela = true;
      if (SEPARADOR.test(t)) continue;
      celulasDaLinha(t).forEach((c) => saida.push(c));
      continue;
    }
    if (/^--- \| ---/.test(t)) continue;
    if (emTabela && t === '') { saida.push(''); emTabela = false; continue; }
    // a linha termina em células vazias ("texto | |"): tira
    const semCauda = t.replace(/(?:\s*\|)+\s*$/, '');
    const separadores = (semCauda.match(/\s\|\s/g) || []).length;
    if (separadores >= 2) suspeita = 'colunas'; // 3+ colunas na mesma linha: o texto de várias magias se mistura
    if (separadores >= 1) {
      // campos colados na mesma linha (formato C): "**Duração:** 1 semana | **Pré-requisitos:** …"
      semCauda.split(/\s\|\s/).forEach((p) => { const l = limparLinha(p); if (l) saida.push(l); });
      continue;
    }
    saida.push(limparLinha(semCauda));
  }

  // parágrafos: tira vazios do começo e do fim, e junta 3+ vazios em 1
  // restos de borda de tabela ("| texto |"): tira os pipes das pontas de cada linha
  const aparadas = saida.map((l) => l.replace(/^\s*\|\s*/, '').replace(/\s*\|\s*$/, ''));
  const texto = aparadas.join('\n').replace(/\n{3,}/g, '\n\n').replace(/^\n+/, '').replace(/\n+$/, '').trim();
  if (!texto) return null;
  const classe = saida.find((l) => l !== '') || '';
  return { texto, classe, paginaInicio: entrada.pagina, paginaFim: entrada.paginaFim, suspeita };
}

module.exports = { recortarDescricao, limparLinha, celulasDaLinha, pareceClasse };
