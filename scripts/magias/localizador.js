'use strict';
/**
 * MC-0 — O localizador: onde está cada magia do catálogo no livro.
 *
 * Fonte única: `chunks.jsonl`, `source_id: "pt_magia"` (1 linha = 1 página). O livro traz cada
 * magia duas vezes: a ENTRADA (o bloco sob o nome) e a TABELA-RESUMO (pp.223–237, 1 linha por magia).
 *
 * 🔴 REGRA DO PLANO: ninguém redigita o livro. Este módulo só LOCALIZA — devolve página, linha e o
 * recorte por posição. Número e texto saem do `chunks.jsonl` por programa; o que não for achado
 * vai para uma lista, e quem ajuda a achar (`magias-localizacao-manual.json`) diz ONDE, nunca O QUÊ.
 *
 * Convenções: `linha` é o índice (0-based) em `text.split(/\r?\n/)` da página; `pagina` é o
 * `page_number` do chunk (que é a página impressa: "[MA Pág. N]"). Um `fim` é EXCLUSIVO.
 *
 * O livro tem 4 jeitos de escrever uma entrada (um por capítulo), e este módulo lê os 4:
 *   A  `**Nome**` sozinho, a classe em texto puro na linha de baixo;
 *   B  `## Nome`, a classe em negrito/itálico na linha de baixo, campos `**Duração:**`;
 *   C  `**Nome** | Classe` seguido de `--- | ---` (tabela de 2 colunas);
 *   Q  como A ou B, mas dentro de citação (`> **Nome**`).
 * E uma página que NÃO dá para recortar: as de colunas embaralhadas (a página vira uma tabela em
 * que o texto de duas ou três magias se mistura). Ali a magia só ganha a PÁGINA (`formato: 'colunas'`).
 */
const fs = require('fs');

const FONTE = 'pt_magia';
const PRIMEIRA_PAGINA_DA_TABELA = 223;
const ULTIMA_PAGINA_DA_TABELA = 237;
const PRIMEIRA_PAGINA_DAS_ENTRADAS = 1;
const ULTIMA_PAGINA_DAS_ENTRADAS = 222;
/** Quantas páginas, no máximo, uma entrada pode atravessar além da do título. */
const PAGINAS_QUE_UMA_ENTRADA_ATRAVESSA = 2;
const CELULAS_DA_TABELA = 9;
const TEM_UNIDADE_DE_TEMPO = /\b(?:seg|min|hora|dia|semana|instant)/i;
/** Página com tantas linhas de tabela (e essa fração do texto) é página de colunas embaralhadas. */
const LINHAS_DE_TABELA_PARA_SER_COLUNAS = 6;
const FRACAO_DE_TABELA_PARA_SER_COLUNAS = 0.35;
/** A entrada tem de estar a até tantas páginas da que a tabela (ou, sem ela, o catálogo) manda. */
const JANELA_DA_ENTRADA = 3;
/** Última palavra de um nome de família no livro: "Aumentar Atributo" serve a "Aumentar Força". */
const PALAVRAS_GENERICAS = new Set(['atributo', 'tipo', 'elemento']);

/** Sem acento, sem caixa, sem pontuação: "Projétil" = "Projetil", "Bola de Relâmpagos" = "bola de relampagos". */
function normalizar(s) {
  return String(s == null ? '' : s)
    .normalize('NFD').replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .replace(/&/g, ' e ')
    .replace(/[^a-z0-9]+/g, ' ')
    .trim();
}

/** Marca de dificuldade no fim do título/nome: "Acelerar (MD)", "Acelerar*". */
function tirarMarcaDeDificuldade(texto) {
  const t = String(texto == null ? '' : texto).trim();
  const semMd = t.replace(/\s*\((?:MD|M\.D\.|IQ\/MD|IQ\/Muito Dif[ií]cil)\)\s*$/i, '');
  const semAsterisco = semMd.replace(/\s*\*+\s*$/, '').trim();
  return { texto: semAsterisco, muitoDificil: semAsterisco !== t };
}

/** "Atrofiar (Visão)" -> { base: "Atrofiar", variante: "Visão" }; "Baliza(…)" também. */
function separarVariante(nome) {
  const s = String(nome == null ? '' : nome).trim();
  const i = s.indexOf('(');
  if (i < 0) return { base: s, variante: null };
  const resto = s.slice(i + 1).replace(/\)\s*$/, '').trim();
  return { base: s.slice(0, i).trim(), variante: resto || null };
}

function distancia(a, b) {
  if (a === b) return 0;
  if (!a.length) return b.length;
  if (!b.length) return a.length;
  let anterior = new Array(b.length + 1);
  for (let j = 0; j <= b.length; j++) anterior[j] = j;
  for (let i = 1; i <= a.length; i++) {
    const atual = [i];
    for (let j = 1; j <= b.length; j++) {
      const custo = a[i - 1] === b[j - 1] ? 0 : 1;
      atual[j] = Math.min(anterior[j] + 1, atual[j - 1] + 1, anterior[j - 1] + custo);
    }
    anterior = atual;
  }
  return anterior[b.length];
}

// ---------------------------------------------------------------------------------- o livro

/** Lê o `chunks.jsonl` e devolve `{ paginas: Map<numero, string[]> }` só do livro de Magia. */
function carregarLivro(caminhoDoChunks) {
  const paginas = new Map();
  const texto = fs.readFileSync(caminhoDoChunks, 'utf8');
  texto.split('\n').forEach((linha) => {
    if (!linha.trim()) return;
    let c;
    try { c = JSON.parse(linha); } catch (e) { return; }
    if (c.source_id !== FONTE) return;
    paginas.set(c.page_number, String(c.text).split(/\r?\n/));
  });
  return { paginas };
}

// ---------------------------------------------------------------------------------- a tabela

/**
 * As linhas da tabela-resumo (pp.223–237). Cada uma: `{ pagina, nome, classe, escola, duracao,
 * energia, tempo, preRequisitos, contagem, muitoDificil, reparada, paginaDoChunk, linha }`.
 * `pagina` é a coluna "Pág." (onde a entrada está; null se a célula veio vazia). `reparada` diz o
 * que foi preciso para ler a linha ("celulas-coladas": energia e tempo vieram numa célula só).
 * `malFormadas`: linhas de tabela que nem assim deu para ler.
 */
function lerTabela(livro) {
  const linhas = [];
  const malFormadas = [];
  const colunasTrocadas = [];
  for (let n = PRIMEIRA_PAGINA_DA_TABELA; n <= ULTIMA_PAGINA_DA_TABELA; n++) {
    const ls = livro.paginas.get(n);
    if (!ls) continue;
    const daPagina = [];
    ls.forEach((bruta, i) => {
      const t = bruta.trim();
      if (!t.startsWith('|')) return;
      if (/^\|[\s|:-]+\|?$/.test(t)) return; // separador |---|---|
      let celulas = t.replace(/^\|/, '').replace(/\|\s*$/, '').split('|').map((c) => c.trim());
      if (/^p[aá]g\.?$/i.test(celulas[0])) return; // cabeçalho
      let reparada = null;
      if (celulas.length === CELULAS_DA_TABELA - 1) {
        // energia e tempo colados na mesma célula: "3 seg=custo" -> "3" | "seg=custo"
        const m = /^(\S+)\s+(\S.*)$/.exec(celulas[5] || '');
        if (m && (/^\d/.test(m[1]) || /^(?:varia|especial)$/i.test(m[1]))) {
          celulas = celulas.slice(0, 5).concat([m[1], m[2]], celulas.slice(6));
          reparada = 'celulas-coladas';
        }
      }
      const paginaOk = celulas[0] === '' || /^\d+$/.test(celulas[0]);
      if (celulas.length !== CELULAS_DA_TABELA || !paginaOk || !celulas[1]) {
        malFormadas.push({ paginaDoChunk: n, linha: i, celulas: celulas.length });
        return;
      }
      if (celulas[0] === '') reparada = reparada || 'sem-pagina';
      const marca = tirarMarcaDeDificuldade(celulas[1]);
      daPagina.push({
        pagina: celulas[0] === '' ? null : parseInt(celulas[0], 10),
        nome: marca.texto,
        muitoDificil: marca.muitoDificil,
        classe: celulas[2],
        escola: celulas[3],
        duracao: celulas[4],
        energia: celulas[5],
        tempo: celulas[6],
        preRequisitos: celulas[7],
        contagem: celulas[8],
        reparada,
        paginaDoChunk: n,
        linha: i,
      });
    });
    // No texto extraído, uma página da tabela (a 233) saiu com Energia e Tempo TROCADOS. Detecta-se
    // pelo conteúdo: a maioria das linhas tem unidade de tempo na coluna de energia e nenhuma na de tempo.
    const trocadas = daPagina.filter((t) => TEM_UNIDADE_DE_TEMPO.test(t.energia) && !TEM_UNIDADE_DE_TEMPO.test(t.tempo)).length;
    const normais = daPagina.filter((t) => !TEM_UNIDADE_DE_TEMPO.test(t.energia) && TEM_UNIDADE_DE_TEMPO.test(t.tempo)).length;
    if (trocadas >= 5 && trocadas > 3 * normais) {
      colunasTrocadas.push(n);
      daPagina.forEach((t) => {
        const e = t.energia;
        t.energia = t.tempo;
        t.tempo = e;
        t.reparada = t.reparada ? t.reparada + '+colunas-trocadas' : 'colunas-trocadas';
      });
    }
    daPagina.forEach((t) => linhas.push(t));
  }
  return { linhas, malFormadas, colunasTrocadas };
}

// ---------------------------------------------------------------------------------- as entradas

const CLASSES_DO_LIVRO = '(?:comum|area|projetil|toque|bloqueio|informacao|encantamento|especial)';
/** Tira citação (`>`), `#`, lista (`-`), `**` e itálico: "> **Informação**" -> "Informação". */
function semMarcas(linha) {
  return String(linha).trim()
    .replace(/^(?:>\s*)+/, '')
    .replace(/^#{1,6}\s*/, '')
    .replace(/^[-+]\s+/, '')
    .replace(/[*_]/g, '')
    .trim();
}

/**
 * A linha é a classe da magia? "Comum; Resistível com HT", "**Projétil**", "*Encantamento*",
 * "Comum ou Bloqueio", "Especial; Projétil". A primeira parte TEM de ser classe (por isso
 * "Resistência à Magia" e "Toque Candente" — nomes de magia — não passam).
 */
function ehLinhaDeClasse(linha) {
  const bruto = semMarcas(linha).replace(/\.\s*$/, '');
  const t = normalizar(bruto);
  if (!t || t.length > 100) return false;
  const partes = bruto.split(/\s*(?:;|\/|\+|,)\s*/).map(normalizar).filter(Boolean);
  if (!partes.length) return false;
  const soClasse = new RegExp('^' + CLASSES_DO_LIVRO + '$');
  const ehClasse = (p) => p.split(/\s+(?:ou|e)\s+/).every((q) => soClasse.test(q));
  const ehResistencia = (p) => /^(?:resistivel|resistencia) /.test(p) || /^r [a-z]/.test(p);
  return ehClasse(partes[0]) && partes.slice(1).every((p) => ehClasse(p) || ehResistencia(p));
}

const ROTULOS_DO_CABECALHO = [
  ['duracao', /^dura[cç][aã]o$/i],
  ['custo', /^custo( b[aá]sico)?$/i],
  ['tempo', /^tempo de opera[cç][aã]o$/i],
  ['preRequisitos', /^pr[eé]-?\s?requisitos?$/i],
];

/** "**Duração:** ..." ou "Duração: ..." (ou numa célula `| **Duração:** x |`) -> 'duracao'; senão null. */
function rotuloDoCampo(linha) {
  const t = String(linha).trim().replace(/^(?:>\s*)+/, '').replace(/^\|\s*/, '').replace(/^[-+]\s+/, '').replace(/^\*\*/, '');
  const m = /^([^:*|]{3,40}?)\s*(?:\*\*)?\s*:/.exec(t);
  if (!m) return null;
  const alvo = m[1].trim();
  for (const [chave, re] of ROTULOS_DO_CABECALHO) if (re.test(alvo)) return chave;
  return null;
}

/** A linha é "Item"/"Itens" (sub-bloco da entrada, não uma magia)? */
function ehItem(linha) {
  return /^itens?$/.test(normalizar(semMarcas(linha)));
}

/** A página é de colunas embaralhadas (uma tabela em que várias magias se misturam)? */
function paginaDeColunas(linhas) {
  if (!linhas) return false;
  let total = 0;
  let tabela = 0;
  linhas.forEach((x) => {
    const t = x.trim();
    if (!t) return;
    total++;
    if (t.startsWith('|') && !/^\|[\s|:-]+\|?$/.test(t)) tabela++;
  });
  return tabela >= LINHAS_DE_TABELA_PARA_SER_COLUNAS && tabela / Math.max(total, 1) > FRACAO_DE_TABELA_PARA_SER_COLUNAS;
}

/**
 * O texto de um título candidato, ou null: `## Nome`, `**Nome**` sozinho, `**Nome** | Classe`
 * (formato C), tudo isso também dentro de citação (`> `).
 */
function candidatoATitulo(linha) {
  const t = String(linha).trim().replace(/^(?:>\s*)+/, '');
  let m = /^#{1,4}\s+(.{2,100})$/.exec(t);
  if (m) return { texto: m[1].replace(/[*_]/g, '').trim(), forma: 'H', classeNaLinha: false };
  m = /^\*\*([^*:|]{2,100})\*\*$/.exec(t);
  if (m) return { texto: m[1].trim(), forma: 'B', classeNaLinha: false };
  m = /^\*\*([^*:|]{2,100})\*\*\s*\|\s*(.+?)\s*\|?$/.exec(t);
  if (m && ehLinhaDeClasse(m[2])) return { texto: m[1].trim(), forma: 'C', classeNaLinha: true };
  m = /^\*\*([^*:|]{2,100})\*\*\s*\(([^)]{3,60})\)\s*$/.exec(t);
  if (m && ehLinhaDeClasse(m[2])) return { texto: m[1].trim(), forma: 'E', classeNaLinha: true };
  return null;
}

const SEPARADOR_DE_TABELA = /^\|[\s|:-]+\|?$/;

/**
 * Formato F: cada entrada é uma mini-tabela — `| Nome | Classe |` e, na linha de baixo, `|---|---|`.
 * Só é título se a 1ª célula é um nome e as outras (se houver) são classes; um cabeçalho com VÁRIOS
 * nomes (página de colunas embaralhadas) não é.
 */
function cabecalhoDeTabela(ls, i) {
  const t = String(ls[i]).trim();
  if (!t.startsWith('|') || SEPARADOR_DE_TABELA.test(t)) return null;
  const proxima = ls[i + 1] === undefined ? '' : String(ls[i + 1]).trim();
  if (!SEPARADOR_DE_TABELA.test(proxima)) return null;
  const cel = t.replace(/^\|/, '').replace(/\|\s*$/, '').split('|').map((c) => c.replace(/\*\*/g, '').trim());
  const nome = cel[0];
  if (!nome || nome.length > 100 || /[:.]/.test(nome) || ehLinhaDeClasse(nome) || ehItem(nome)) return null;
  const outras = cel.slice(1).filter(Boolean);
  if (!outras.length || !outras.every(ehLinhaDeClasse)) return null;
  return { texto: nome, forma: 'F', classeNaLinha: true };
}

/** A linha pode ser a 2ª/3ª metade de um título quebrado ("Superior (MD)", "**Elétrica/NT**")? */
function ehContinuacaoDeTitulo(linha) {
  const t = semMarcas(linha).replace(/\(MD\)\.\s*$/i, '(MD)');
  if (!t || t.length > 40 || /[:.|]/.test(t) || t === '---') return false;
  if (t.split(/\s+/).length > 4 || !/^[A-ZÁÂÃÀÉÊÍÓÔÕÚÇ0-9]/.test(t)) return false;
  return !ehLinhaDeClasse(linha) && !ehItem(linha);
}

/** O índice da próxima linha com texto (pula vazias e `>` vazios), ou `ls.length`. */
function proximaComTexto(ls, de) {
  let j = de;
  while (j < ls.length && !semMarcas(ls[j])) j++;
  return j;
}

/**
 * Os títulos do livro. Uma linha candidata é título de magia quando (a) a classe vem na própria
 * linha ou na próxima linha não vazia, ou (b) o texto é um nome conhecido (catálogo ou tabela).
 * `nomesConhecidos`: Set de nomes normalizados. Cada título: `{ pagina, linha, texto, norm,
 * muitoDificil, forma, comClasse, linhaDaClasse }`.
 */
function lerTitulos(livro, nomesConhecidos) {
  const titulos = [];
  for (let n = PRIMEIRA_PAGINA_DAS_ENTRADAS; n <= ULTIMA_PAGINA_DAS_ENTRADAS; n++) {
    const ls = livro.paginas.get(n);
    if (!ls) continue;
    const consumidas = new Set(); // linhas que são a 2ª/3ª metade de um título quebrado
    ls.forEach((bruta, i) => {
      if (consumidas.has(i)) return;
      const cand = cabecalhoDeTabela(ls, i) || candidatoATitulo(bruta);
      if (!cand) return;
      if (ehItem(cand.texto) || (cand.forma !== 'H' && ehLinhaDeClasse(cand.texto))) return;
      let j = proximaComTexto(ls, i + 1);
      let comClasse = cand.classeNaLinha || (j < ls.length && ehLinhaDeClasse(ls[j]));
      let texto = cand.texto;
      if (!comClasse && j < ls.length) {
        // título quebrado: "**Alucinação**" / "Superior (MD)" / classe
        const partes = [];
        const linhasDasPartes = [];
        let k = j;
        while (partes.length < 2 && k < ls.length && ehContinuacaoDeTitulo(ls[k])) {
          partes.push(semMarcas(ls[k]).replace(/\(MD\)\.\s*$/i, '(MD)'));
          linhasDasPartes.push(k);
          k = proximaComTexto(ls, k + 1);
        }
        const composto = [cand.texto].concat(partes).join(' ');
        const nomeComposto = normalizar(tirarMarcaDeDificuldade(composto).texto);
        // só junta se o resultado for um nome que o catálogo ou a tabela conhecem (senão engole o título vizinho)
        if (partes.length && k < ls.length && ehLinhaDeClasse(ls[k]) && nomesConhecidos.has(nomeComposto)) {
          texto = composto;
          comClasse = true;
          j = k;
          linhasDasPartes.forEach((x) => consumidas.add(x));
        }
      }
      const marca = tirarMarcaDeDificuldade(texto);
      const norm = normalizar(marca.texto);
      const conhecido = nomesConhecidos.has(norm) || nomesConhecidos.has(normalizar(separarVariante(marca.texto).base));
      if (!comClasse && !conhecido) return;
      titulos.push({
        pagina: n, linha: i, texto: marca.texto, norm, muitoDificil: marca.muitoDificil,
        forma: cand.forma, comClasse, linhaDaClasse: cand.classeNaLinha ? i : (comClasse ? j : null),
      });
    });
  }
  return titulos;
}

/**
 * O bloco de uma entrada, a partir do título: vai até o `---`, o próximo título, ou qualquer outro
 * cabeçalho `#` que não seja "Item". Atravessa a quebra de página (até 2 páginas além da do título).
 * Devolve `{ paginaInicio, linhaInicio, paginaFim, linhaFim, campos }`; `campos` = onde estão as
 * linhas Duração/Custo/Tempo/Pré-requisitos (`{ pagina, linha }`), nunca o texto delas.
 */
function recortarBloco(livro, titulo, ehOutroTitulo) {
  let pagina = titulo.pagina;
  let paginaFim = pagina;
  let linhaFim = 0;
  const campos = {};
  if (titulo.forma === 'F') {
    const ls = livro.paginas.get(pagina);
    let i = titulo.linha + 2; // pula o cabeçalho e o `|---|`
    for (; i < ls.length; i++) {
      const t = ls[i].trim();
      if (!t.startsWith('|') || ehOutroTitulo(pagina, i)) break;
      const r = rotuloDoCampo(t);
      if (r && campos[r] === undefined) campos[r] = { pagina, linha: i };
    }
    return { paginaInicio: pagina, linhaInicio: titulo.linha, paginaFim: pagina, linhaFim: i, campos };
  }
  for (let passo = 0; passo <= PAGINAS_QUE_UMA_ENTRADA_ATRAVESSA; passo++) {
    const ls = livro.paginas.get(pagina);
    // a 1ª linha de uma página que continua é "# Página N - ..."
    let i = passo === 0 ? titulo.linha + 1 : 1;
    let fechou = false;
    for (; i < ls.length; i++) {
      const t = ls[i].trim();
      const semCitacao = t.replace(/^(?:>\s*)+/, '');
      const ehCabecalho = /^#{1,4}\s/.test(semCitacao) && !ehItem(semCitacao);
      // título dado à mão: as linhas de continuação dele ("**Elétrica/NT**") não são outro título
      const continuacaoDoManual = titulo.forma === 'manual' && pagina === titulo.pagina && i <= titulo.linha + 3;
      if (semCitacao === '---' || (!continuacaoDoManual && ehOutroTitulo(pagina, i)) || ehCabecalho) { fechou = true; break; }
      const r = rotuloDoCampo(t);
      if (r && campos[r] === undefined) campos[r] = { pagina, linha: i };
    }
    paginaFim = pagina;
    linhaFim = i;
    if (fechou) break;
    if (passo === PAGINAS_QUE_UMA_ENTRADA_ATRAVESSA || !livro.paginas.has(pagina + 1)) break;
    pagina += 1;
  }
  return { paginaInicio: titulo.pagina, linhaInicio: titulo.linha, paginaFim, linhaFim, campos };
}

/**
 * Os campos de cabeçalho de uma entrada, COMO O LIVRO ESCREVE (texto depois dos dois-pontos, sem
 * marcas): `{ duracao, custo, tempo, preRequisitos, classe }`. Lê por programa do bloco já
 * localizado; nenhum valor é digitado. `classe` é a linha de classe logo depois do título.
 * Um rótulo que aparece mais de uma vez fica com a PRIMEIRA ocorrência.
 */
function camposDaEntrada(livro, entrada) {
  const saida = {};
  if (!entrada || entrada.formato !== 'bloco') return saida;
  const linhas = linhasDoBloco(livro, entrada);
  // a classe: a 1ª linha (depois do título) que é só classe
  for (let i = 1; i < Math.min(linhas.length, 8); i++) {
    if (!semMarcas(linhas[i])) continue;
    if (ehLinhaDeClasse(linhas[i])) { saida.classe = semMarcas(linhas[i]).replace(/\.\s*$/, ''); }
    break;
  }
  linhas.forEach((l) => {
    // uma linha pode ter vários campos em células: "| **Duração:** 1 min. | **Custo:** 2 |"
    const pedacos = l.includes('|') ? l.split('|') : [l];
    pedacos.forEach((p) => {
      const rotulo = rotuloDoCampo(p);
      if (!rotulo) return;
      const valor = p.replace(/^(?:>\s*)+/, '').replace(/\*\*/g, '').replace(/^[\s|+-]+/, '')
        .replace(/^[^:]*:\s*/, '').trim();
      if (rotulo === 'custo') {
        if (saida.custo === undefined) saida.custo = valor;
      } else if (saida[rotulo] === undefined) saida[rotulo] = valor;
    });
  });
  return saida;
}

/** O bloco só remete a outra página ("Como listada em Mágicas de X, pág. 105.")? -> o número, ou null. */
function remissaoDoBloco(linhas) {
  for (const l of linhas) {
    const m = /listad[ao]s?\s+em.{0,80}?p[aá]g\.?\s*(\d{1,3})/i.exec(l);
    if (m) return parseInt(m[1], 10);
  }
  return null;
}

/** As linhas do bloco, em ordem (para os testes e para os lotes seguintes). */
function linhasDoBloco(livro, bloco) {
  const saida = [];
  const primeira = bloco.paginaInicio != null ? bloco.paginaInicio : bloco.pagina;
  for (let p = primeira; p <= bloco.paginaFim; p++) {
    const ls = livro.paginas.get(p) || [];
    const de = p === primeira ? bloco.linhaInicio : 1;
    const ate = p === bloco.paginaFim ? bloco.linhaFim : ls.length;
    for (let i = de; i < ate; i++) saida.push(ls[i]);
  }
  return saida;
}

// ---------------------------------------------------------------------------------- o casamento

function agruparPor(itens, chave) {
  const m = new Map();
  itens.forEach((it) => {
    const k = chave(it);
    if (!m.has(k)) m.set(k, []);
    m.get(k).push(it);
  });
  return m;
}

/** Entre vários candidatos, o mais perto de uma página de referência (a ordem do livro desempata). */
function maisPerto(candidatos, referencias, paginaDe) {
  const refs = referencias.filter((r) => r != null && r > 0);
  if (candidatos.length === 1 || !refs.length) return candidatos[0];
  let melhor = candidatos[0];
  let menor = Infinity;
  candidatos.forEach((c) => {
    const p = paginaDe(c);
    const d = p == null ? Infinity : Math.min(...refs.map((r) => Math.abs(p - r)));
    if (d < menor) { menor = d; melhor = c; }
  });
  return melhor;
}

function indexar(itens, textoDe) {
  return {
    porNome: agruparPor(itens, (it) => normalizar(textoDe(it))),
    porBase: agruparPor(itens, (it) => normalizar(separarVariante(textoDe(it)).base)),
  };
}

/**
 * Acha, em `itens`, o que corresponde ao `nome` do catálogo. Em ordem: nome igual; mesma base (a
 * variante entre parênteses aponta para a mãe, e o contrário); genérico ("Aumentar (Atributo)" serve
 * a "Aumentar Força"); aproximado (erro de digitação: até 2 letras, um único vizinho).
 * Devolve `{ item, casamento, variante, ... }` ou null.
 */
function achar(nome, referencias, itens, indices, textoDe, paginaDe) {
  const norm = normalizar(nome);
  const { base, variante } = separarVariante(nome);
  const normBase = normalizar(base);

  let c = indices.porNome.get(norm);
  if (c && c.length) return { item: maisPerto(c, referencias, paginaDe), casamento: 'exato', variante: null };

  c = normBase ? indices.porBase.get(normBase) : null;
  if (c && c.length) {
    const maes = c.filter((it) => !separarVariante(textoDe(it)).variante);
    const pool = maes.length ? maes : c;
    const item = maisPerto(pool, referencias, paginaDe);
    return { item, casamento: 'base', variante: variante || separarVariante(textoDe(item)).variante };
  }

  const palavras = (it) => normalizar(separarVariante(textoDe(it)).base).split(' ').filter(Boolean);
  const tamanho = (it) => palavras(it).join(' ').length;
  const doMaior = (lista) => {
    const maior = Math.max(...lista.map(tamanho));
    return lista.filter((it) => tamanho(it) === maior);
  };

  // o livro escreve "Aumentar Atributo" (ou "Aumentar (Atributo)") e o catálogo "Aumentar Força"
  const comPlaceholder = itens.filter((it) => {
    const w = palavras(it);
    const ph = PALAVRAS_GENERICAS.has(w[w.length - 1]) && w.length >= 2 ? w.slice(0, -1).join(' ') : null;
    return ph && norm.startsWith(ph + ' ');
  });
  if (comPlaceholder.length) {
    const alvo = doMaior(comPlaceholder);
    return { item: maisPerto(alvo, referencias, paginaDe), casamento: 'generico', variante: null };
  }
  const comParenteses = itens.filter((it) => {
    const sv = separarVariante(textoDe(it));
    const b = normalizar(sv.base);
    return sv.variante && b.length > 0 && norm.startsWith(b + ' ');
  });
  if (comParenteses.length) {
    const alvo = doMaior(comParenteses);
    return { item: maisPerto(alvo, referencias, paginaDe), casamento: 'generico', variante: null };
  }

  // o livro escreve a família ("Convocar Elemental") e o catálogo abre por elemento ("... Fogo")
  const prefixos = itens.filter((it) => {
    const w = palavras(it);
    if (w.length < 2) return false; // "Toque", "Aumentar": curto demais para valer como família
    const p = w.join(' ');
    return norm.startsWith(p + ' ') && norm.slice(p.length + 1).split(' ').length <= 2;
  });
  if (prefixos.length) {
    const alvo = doMaior(prefixos);
    return { item: maisPerto(alvo, referencias, paginaDe), casamento: 'prefixo', variante: null };
  }

  // o catálogo encurtou o nome ("Resfriar" por "Resfriar Área"): só com a página concordando
  const inversos = itens.filter((it) => {
    const p = palavras(it).join(' ');
    if (!p.startsWith(norm + ' ') || p.slice(norm.length + 1).split(' ').length > 2) return false;
    const pg = paginaDe(it);
    return pg != null && referencias.some((r) => r != null && r > 0 && Math.abs(pg - r) <= 1);
  });
  if (inversos.length) {
    const unicos = new Set(inversos.map((it) => normalizar(textoDe(it))));
    if (unicos.size === 1) return { item: maisPerto(inversos, referencias, paginaDe), casamento: 'prefixo-inverso', variante: null };
  }

  // erro de digitação ("Aumentar Vitlidade", "Aquece"): só com UM vizinho mais perto
  let melhorD = Infinity;
  let vizinhos = [];
  itens.forEach((it) => {
    const alvo = normalizar(textoDe(it));
    if (Math.abs(alvo.length - norm.length) > 2) return;
    const d = distancia(norm, alvo);
    if (d < melhorD) { melhorD = d; vizinhos = [it]; } else if (d === melhorD) vizinhos.push(it);
  });
  const pertoDemais = melhorD <= 1 ? norm.length >= 5 : (melhorD <= 2 && melhorD / Math.max(norm.length, 1) <= 0.15);
  if (pertoDemais) {
    const unicos = new Set(vizinhos.map((v) => normalizar(textoDe(v))));
    if (unicos.size === 1) {
      return { item: maisPerto(vizinhos, referencias, paginaDe), casamento: 'aproximado', variante: null, distancia: melhorD };
    }
  }
  return null;
}

/** `{ id, pagina, titulo, tabela? }` — só LOCALIZAÇÃO, nunca número nem texto. */
function lerLocalizacaoManual(caminho) {
  if (!caminho || !fs.existsSync(caminho)) return [];
  const dados = JSON.parse(fs.readFileSync(caminho, 'utf8'));
  return Array.isArray(dados) ? dados : (dados.magias || []);
}

const SO_CLASSE = new RegExp('^' + CLASSES_DO_LIVRO + '$');

/**
 * Última chance: o nome aparece na página sem ser um título que dê para recortar. Três jeitos:
 *  (a) uma CÉLULA inteira de uma linha de tabela (páginas de colunas embaralhadas);
 *  (b) o nome partido em 2 ou 3 células vizinhas da MESMA linha ("| Metamorfosear | Outro (MD) |");
 *  (c) uma linha curta, sem ":" nem ".", que é só o nome (com a classe antes: "Especial Transmogrificação").
 * Devolve o índice da linha, ou -1. Menção no meio de uma frase ("Pré-requisito: X.") não vale.
 */
function linhaSoDaPagina(linhas, nome) {
  const alvo = normalizar(separarVariante(nome).base);
  if (!alvo) return -1;
  const semMd = (c) => normalizar(tirarMarcaDeDificuldade(c.replace(/\*\*/g, '')).texto);
  for (let i = 0; i < linhas.length; i++) {
    const t = linhas[i].trim();
    if (!t) continue;
    if (t.includes('|')) {
      const cel = t.replace(/^\|/, '').replace(/\|\s*$/, '').split('|').map(semMd);
      if (cel.some((c) => c === alvo)) return i; // (a)
      for (let a = 0; a < cel.length; a++) {
        for (let n = 2; n <= 3 && a + n <= cel.length; n++) {
          if (cel.slice(a, a + n).join(' ') === alvo) return i; // (b)
        }
      }
      continue;
    }
    const s = semMarcas(t);
    if (!s || s.length > 70 || /[:.]/.test(s)) continue;
    const n = semMd(s);
    if (n === alvo) return i; // (c)
    const w = n.split(' ');
    if (w.length > 1 && SO_CLASSE.test(w[0]) && w.slice(1).join(' ') === alvo) return i;
  }
  return -1;
}

/**
 * O índice: para cada magia do catálogo, a linha da tabela e a entrada.
 * `opcoes.manual`: lista de `{ id, pagina, titulo, tabela? }`.
 */
function construirIndice(livro, catalogo, opcoes) {
  const manual = new Map(((opcoes && opcoes.manual) || []).map((m) => [m.id, m]));
  const { linhas: tabela, malFormadas } = lerTabela(livro);

  const nomes = new Set();
  const conhecer = (n) => { nomes.add(normalizar(n)); nomes.add(normalizar(separarVariante(n).base)); };
  catalogo.forEach((m) => conhecer(m.nome));
  tabela.forEach((t) => conhecer(t.nome));
  manual.forEach((m) => { if (m.titulo) conhecer(m.titulo); });
  nomes.delete('');
  nomes.delete('item');
  nomes.delete('itens');

  const titulos = lerTitulos(livro, nomes);
  const noTitulo = new Set(titulos.map((t) => t.pagina + ':' + t.linha));
  const ehOutroTitulo = (pagina, linha) => noTitulo.has(pagina + ':' + linha);

  const indiceTabela = indexar(tabela, (t) => t.nome);
  const indiceTitulos = indexar(titulos, (t) => t.texto);
  const blocos = new Map();
  const bloco = (t) => {
    const k = t.pagina + ':' + t.linha;
    if (!blocos.has(k)) blocos.set(k, recortarBloco(livro, t, ehOutroTitulo));
    return blocos.get(k);
  };

  const magias = {};
  const manuaisNaoResolvidos = [];

  catalogo.forEach((m) => {
    const saida = { nome: m.nome, paginaCatalogo: m.pagina || null, tabela: null, entrada: null };
    const aMao = manual.get(m.id);

    // ---- a linha da tabela
    let linhaTabela = null;
    if (aMao && aMao.tabela) {
      const alvo = normalizar(aMao.tabela);
      const c = tabela.filter((t) => normalizar(t.nome) === alvo);
      if (c.length) linhaTabela = { item: maisPerto(c, [aMao.pagina, m.pagina], (t) => t.pagina), casamento: 'manual', variante: null };
    }
    if (!linhaTabela) linhaTabela = achar(m.nome, [m.pagina], tabela, indiceTabela, (t) => t.nome, (t) => t.pagina);
    if (linhaTabela) {
      const t = linhaTabela.item;
      saida.tabela = {
        casamento: linhaTabela.casamento,
        variante: linhaTabela.variante || null,
        reparada: t.reparada,
        paginaDoChunk: t.paginaDoChunk,
        linha: t.linha,
        nome: t.nome,
        muitoDificil: t.muitoDificil,
        pagina: t.pagina,
        campos: {
          classe: t.classe, escola: t.escola, duracao: t.duracao, energia: t.energia,
          tempo: t.tempo, preRequisitos: t.preRequisitos,
        },
      };
    }

    // ---- a entrada
    const paginaDoLivro = saida.tabela && saida.tabela.pagina ? saida.tabela.pagina : null;
    let achado = null;
    if (aMao && aMao.pagina && (aMao.titulo || aMao.linha != null)) {
      const alvo = normalizar(aMao.titulo || '');
      const ls = livro.paginas.get(aMao.pagina);
      let c = titulos.filter((t) => t.pagina === aMao.pagina && (aMao.linha != null ? t.linha === aMao.linha : t.norm === alvo));
      if (!c.length && aMao.linha != null && ls && ls[aMao.linha] !== undefined) {
        // o título existe no livro, mas o texto perdeu o começo do nome: vale a linha que o manual aponta
        const cand = candidatoATitulo(ls[aMao.linha]);
        c = [{
          pagina: aMao.pagina, linha: aMao.linha, texto: cand ? cand.texto : semMarcas(ls[aMao.linha]),
          norm: alvo, muitoDificil: false, forma: 'manual', comClasse: false, linhaDaClasse: null,
        }];
      }
      if (c.length) achado = { item: c[0], casamento: 'manual', variante: null };
      else manuaisNaoResolvidos.push({ id: m.id, nome: m.nome, pagina: aMao.pagina, titulo: aMao.titulo || null });
    }
    if (!achado && paginaDoLivro) {
      // o título cujo texto casa e que está na página que a TABELA manda
      const norm = normalizar(m.nome);
      const normBase = normalizar(separarVariante(m.nome).base);
      const na = titulos.filter((t) => t.pagina === paginaDoLivro && (t.norm === norm || t.norm === normBase
        || normalizar(separarVariante(t.texto).base) === normBase));
      if (na.length) {
        const exato = na.filter((t) => t.norm === norm);
        achado = exato.length
          ? { item: exato[0], casamento: 'exato', variante: null }
          : { item: na[0], casamento: 'base', variante: separarVariante(m.nome).variante || separarVariante(na[0].texto).variante };
      }
    }
    if (!achado) {
      // só vale título perto da página que a tabela (ou o catálogo) manda: o mesmo nome longe é outra coisa
      const ref = paginaDoLivro || m.pagina;
      const perto = ref ? titulos.filter((t) => Math.abs(t.pagina - ref) <= JANELA_DA_ENTRADA) : titulos;
      achado = achar(m.nome, [paginaDoLivro, m.pagina], perto, indexar(perto, (t) => t.texto), (t) => t.texto, (t) => t.pagina);
    }

    if (achado) {
      const t = achado.item;
      const b = bloco(t);
      // título numa página de colunas embaralhadas, com linhas de tabela dentro do bloco: o recorte
      // misturaria magias vizinhas, então vale só a posição ("colunas"), não o bloco
      const misto = t.forma !== 'F' && paginaDeColunas(livro.paginas.get(t.pagina))
        && linhasDoBloco(livro, b).some((l) => l.trim().startsWith('|'));
      saida.entrada = {
        formato: misto ? 'colunas' : 'bloco',
        casamento: achado.casamento,
        variante: achado.variante || null,
        titulo: t.texto,
        pagina: t.pagina,
        paginaFim: b.paginaFim,
        linhaInicio: b.linhaInicio,
        linhaFim: b.linhaFim,
        temCampos: Object.keys(b.campos).length > 0,
        campos: b.campos,
        remissao: Object.keys(b.campos).length ? null : remissaoDoBloco(linhasDoBloco(livro, b)),
      };
    } else {
      // sem bloco que se recorte (página de colunas embaralhadas etc.): só a PÁGINA e a linha
      const candidatas = [paginaDoLivro, m.pagina].filter((p, i, a) => p && a.indexOf(p) === i);
      for (const p of candidatas) {
        for (const q of [p, p - 1, p + 1]) {
          const ls = livro.paginas.get(q);
          if (!ls || q < PRIMEIRA_PAGINA_DAS_ENTRADAS || q > ULTIMA_PAGINA_DAS_ENTRADAS) continue;
          const i = linhaSoDaPagina(ls, m.nome);
          if (i >= 0) {
            saida.entrada = {
              formato: paginaDeColunas(ls) ? 'colunas' : 'pagina', casamento: 'pagina', variante: null,
              titulo: null, pagina: q, linha: i, temCampos: false, campos: {},
            };
            break;
          }
        }
        if (saida.entrada) break;
      }
    }
    magias[m.id] = saida;
  });

  return { magias, tabela, malFormadas, titulos, manuaisNaoResolvidos };
}

/**
 * As contas de cobertura sobre um índice. `localizada` = tem a linha da tabela E a entrada (bloco
 * recortável OU só a página); `comBloco` = tem a linha da tabela E um bloco que se recorta.
 */
function resumir(indice) {
  const ms = Object.values(indice.magias);
  const r = {
    total: ms.length, comTabela: 0, comBloco: 0, soPagina: 0, semEntrada: 0,
    localizada: 0, comBlocoETabela: 0, semNenhuma: 0, blocoSemCampos: 0, remissoes: 0,
    casamentoTabela: {}, casamentoEntrada: {},
  };
  ms.forEach((m) => {
    const t = !!m.tabela;
    const e = m.entrada;
    if (t) { r.comTabela++; r.casamentoTabela[m.tabela.casamento] = (r.casamentoTabela[m.tabela.casamento] || 0) + 1; }
    if (e) r.casamentoEntrada[e.casamento] = (r.casamentoEntrada[e.casamento] || 0) + 1;
    if (e && e.formato === 'bloco') {
      r.comBloco++;
      if (!e.temCampos) r.blocoSemCampos++;
      if (e.remissao) r.remissoes++;
    } else if (e) r.soPagina++;
    else r.semEntrada++;
    if (t && e) r.localizada++;
    if (t && e && e.formato === 'bloco') r.comBlocoETabela++;
    if (!t && !e) r.semNenhuma++;
  });
  return r;
}

module.exports = {
  FONTE,
  normalizar,
  separarVariante,
  tirarMarcaDeDificuldade,
  distancia,
  carregarLivro,
  lerTabela,
  lerTitulos,
  ehLinhaDeClasse,
  rotuloDoCampo,
  paginaDeColunas,
  recortarBloco,
  linhasDoBloco,
  construirIndice,
  lerLocalizacaoManual,
  remissaoDoBloco,
  camposDaEntrada,
  resumir,
};
