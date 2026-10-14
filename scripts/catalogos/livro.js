'use strict';
/**
 * MC-6 — leitura do livro (`chunks.jsonl`) para conferir os OUTROS catálogos por amostragem.
 *
 * Só leitura: nada aqui escreve em catálogo. Acha a entrada de uma vantagem, desvantagem, perícia ou
 * técnica pelo TÍTULO (linha `## Nome`, `### Nome` ou `**Nome**` sozinha) e só aceita o candidato que
 * traz, logo abaixo, o cabeçalho do tipo certo (custo em pontos; "DX/Difícil"; "Média"). Assim a
 * linha de uma tabela de índice, ou uma citação no meio do texto, nunca é tomada pela entrada.
 */
const fs = require('fs');
const path = require('path');

const ASSETS = path.join(__dirname, '..', '..', 'app', 'src', 'main', 'assets');

/** "Visão Noturna" e "VISÃO  noturna*" viram a mesma chave. */
function normalizar(s) {
  return String(s == null ? '' : s)
    .normalize('NFD').replace(/[̀-ͯ]/g, '')
    .toLowerCase().replace(/[^a-z0-9]+/g, ' ').trim();
}

/** Custo escrito NA linha do título: "## Epilepsia (-30 pontos)" ou "**Hermafrodita** | 5 pontos". */
function custoNaLinha(linha) {
  const l = String(linha).trim();
  let m = /\(\s*(-?\d[^)]*\bpontos?\b[^)]*)\)\s*\**\s*$/i.exec(l);
  if (m) return m[1].replace(/\*+/g, '').trim();
  m = /\|\s*(-?\d[^|]*\bpontos?\b[^|]*)\s*$/i.exec(l);
  return m ? m[1].replace(/\*+/g, '').trim() : null;
}

/** O título que a linha é, ou null. Aceita `## X`, `### X`, `**X**`, `**X***` e `**X** | 5 pontos`. */
function tituloDaLinha(linha) {
  const l = String(linha).trim();
  let titulo = null;
  let m = /^#{1,6}\s+(.+?)\s*#*$/.exec(l);
  if (m) {
    titulo = m[1].replace(/\s*\(\s*-?\d[^)]*\bpontos?\b[^)]*\)\s*\**\s*$/i, '').replace(/\*+/g, '').trim();
  } else {
    m = /^\*\*([^*|]+?)\*\*\**\s*(?:\|.*)?$/.exec(l);
    if (m && !/:$/.test(m[1].trim())) titulo = m[1].trim();
  }
  // "### 0 a 10 pontos" é a linha de CUSTO escrita como título, e não o título de uma entrada.
  if (titulo && /^[-\d]/.test(titulo) && /\bpontos?\b/i.test(titulo)) return null;
  return titulo;
}

/** "V. Desastrado, pág. 132": a entrada só remete a outra, não traz custo próprio. */
const ehRemissao = (linha) => /^[>\s*_]*V\.\s/.test(String(linha).trim());

const DIFICULDADES = /(F[aá]cil|M[eé]dia|Dif[ií]cil|Muito Dif[ií]cil)/i;

/** Perícia: "**DX/Difícil**", "**IQ/Média**", "| DX/Média |". */
function ehCabecalhoDePericia(linha) {
  const l = String(linha).replace(/[*|]+/g, '').trim();
  return l.length > 0 && l.length < 60 && /\//.test(l) && DIFICULDADES.test(l)
    && /^(DX|IQ|HT|ST|Vont|VON|Per|PER|Vontade|Percep)/i.test(l);
}

/** Técnica: "**Média**", "*Difícil*", "| Média |" sozinha. */
function ehCabecalhoDeTecnica(linha) {
  return /^[*|\s]*(M[eé]dia|Dif[ií]cil|F[aá]cil)[*|\s]*$/i.test(String(linha).trim());
}

/** Vantagem/desvantagem: a linha de custo ("**10 pontos/nível**", "-15 ou -20 pontos", "Variável"). */
function ehLinhaDeCusto(linha) {
  const bruta = String(linha).trim();
  if (/^\|/.test(bruta)) return false; // linha de tabela ("| 3 | 1.000× | 15 pontos |") não é o custo da entrada
  const l = bruta.replace(/[*_]+/g, '').replace(/^[#>\s]+/, '').trim();
  if (!l || l.length > 90) return false;
  if (/^V\.\s/.test(l)) return false;
  if (/^vari[aá]vel\b/i.test(l)) return true;
  // o custo começa por número (ou sinal): "10 pontos/nível", "-15 ou -20 pontos", "5, 10 ou 15 pontos"
  return /^[-−–]?\d/.test(l) && /\bpontos?\b/i.test(l);
}

function carregarLivro() {
  const paginas = [];
  fs.readFileSync(path.join(ASSETS, 'chunks.jsonl'), 'utf8').split('\n').forEach((linha) => {
    if (!linha.trim()) return;
    const o = JSON.parse(linha);
    paginas.push({ fonte: o.source_id, pagina: o.page_number, linhas: String(o.text).split(/\r?\n/) });
  });
  return paginas;
}

/** Índice título normalizado -> ocorrências { fonte, pagina, idx, p } (p = posição no vetor de páginas). */
function construirIndice(paginas) {
  const indice = new Map();
  paginas.forEach((pg, p) => {
    pg.linhas.forEach((linha, idx) => {
      const t = tituloDaLinha(linha);
      if (!t) return;
      const k = normalizar(t);
      if (!k) return;
      if (!indice.has(k)) indice.set(k, []);
      indice.get(k).push({ fonte: pg.fonte, pagina: pg.pagina, idx, p });
    });
  });
  return indice;
}

/** Próxima(s) linha(s) não vazias depois de `idx` (até `ate`), com o índice de cada uma. */
function proximas(linhas, idx, ate) {
  const r = [];
  for (let i = idx + 1; i < linhas.length && r.length < ate; i++) {
    const t = linhas[i].trim();
    if (t === '' || /^-{3,}$/.test(t) || /^\|[-|\s]+\|$/.test(t)) continue;
    r.push({ i, texto: linhas[i] });
  }
  return r;
}

const limpar = (s) => String(s).replace(/[*|]+/g, '').trim();

/** Valida a ocorrência como entrada do `tipo` e devolve o cabeçalho lido (ou null). */
function validar(paginas, ocorrencia, tipo) {
  const pg = paginas[ocorrencia.p];
  const seguintes = proximas(pg.linhas, ocorrencia.idx, 4);
  if (tipo === 'custo') {
    const inline = custoNaLinha(pg.linhas[ocorrencia.idx]);
    if (inline) return { linha: ocorrencia.idx, texto: inline };
    let remissao = false;
    for (const s of seguintes) {
      if (tituloDaLinha(s.texto)) break; // já é a entrada seguinte: o custo achado depois seria dela
      if (ehLinhaDeCusto(s.texto)) return { linha: s.i, texto: limpar(s.texto) };
      if (ehRemissao(s.texto)) remissao = true;
    }
    return remissao ? { linha: ocorrencia.idx, texto: '', remissao: true } : null;
  }
  if (tipo === 'pericia') {
    const achou = seguintes.slice(0, 3).find((s) => ehCabecalhoDePericia(s.texto));
    return achou ? { linha: achou.i, texto: limpar(achou.texto) } : null;
  }
  if (tipo === 'tecnica') {
    const achou = seguintes.slice(0, 2).find((s) => ehCabecalhoDeTecnica(s.texto));
    return achou ? { linha: achou.i, texto: limpar(achou.texto) } : null;
  }
  return null;
}

/**
 * O bloco da entrada: da linha do título até o próximo título VÁLIDO do mesmo tipo (ou o fim da
 * página seguinte). Devolve as linhas, sem o cabeçalho de outra entrada.
 */
function blocoDaEntrada(paginas, ocorrencia, tipo) {
  const linhas = [];
  for (let p = ocorrencia.p; p <= ocorrencia.p + 1 && p < paginas.length; p++) {
    const pg = paginas[p];
    if (pg.fonte !== paginas[ocorrencia.p].fonte) break;
    const ini = p === ocorrencia.p ? ocorrencia.idx + 1 : 0;
    for (let i = ini; i < pg.linhas.length; i++) {
      const t = tituloDaLinha(pg.linhas[i]);
      if (t && validar(paginas, { p, idx: i }, tipo)) return linhas;
      linhas.push(pg.linhas[i]);
    }
  }
  return linhas;
}

/**
 * Acha a entrada de `nome` do `tipo`. Entre várias, a que traz o cabeçalho certo; empate: a mais
 * próxima da `paginaDica` (em número de página do livro, sem exigir igualdade).
 */
function acharEntrada(paginas, indice, nome, tipo, paginaDica, fonte) {
  const chaves = [normalizar(nome)];
  const semParenteses = normalizar(String(nome).replace(/\([^)]*\)/g, ' '));
  if (semParenteses && !chaves.includes(semParenteses)) chaves.push(semParenteses);
  const semMarcas = normalizar(String(nome).replace(/\/NT\b|\(†\)|†/g, ' '));
  if (semMarcas && !chaves.includes(semMarcas)) chaves.push(semMarcas);
  const candidatos = [];
  chaves.forEach((k) => (indice.get(k) || []).forEach((o) => {
    if (fonte && o.fonte !== fonte) return; // a técnica de Gun Fu não pode casar com a de mesmo nome do Artes Marciais
    const cab = validar(paginas, o, tipo);
    if (cab) candidatos.push({ ...o, cabecalho: cab, chave: k });
  }));
  if (!candidatos.length) return null;
  const dica = Number.isFinite(paginaDica) ? paginaDica : null;
  candidatos.sort((a, b) => {
    const ra = a.cabecalho.remissao ? 1 : 0;
    const rb = b.cabecalho.remissao ? 1 : 0;
    if (ra !== rb) return ra - rb; // a entrada com custo próprio vence a que só remete
    if (a.chave !== b.chave) return chaves.indexOf(a.chave) - chaves.indexOf(b.chave);
    if (dica === null) return a.pagina - b.pagina;
    return Math.abs(a.pagina - dica) - Math.abs(b.pagina - dica);
  });
  return { ...candidatos[0], total: candidatos.length };
}

module.exports = {
  ASSETS, normalizar, tituloDaLinha, custoNaLinha, ehCabecalhoDePericia, ehCabecalhoDeTecnica, ehLinhaDeCusto,
  carregarLivro, construirIndice, validar, blocoDaEntrada, acharEntrada,
};
