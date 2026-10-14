'use strict';
/**
 * MC-6 — os OUTROS catálogos têm a mesma doença do de magias? Só relatório, sem consertar nada.
 *
 * Sorteia 40 itens de cada catálogo (semente fixa → a mesma amostra sempre) e confronta, com o
 * `chunks.jsonl`, o que dá para conferir por programa. Cada campo tem DUAS testemunhas no livro:
 *   a ENTRADA (o texto da vantagem/perícia/técnica) e a TABELA-RESUMO do fim do livro.
 *     vantagens / desvantagens : custo em pontos, página, números dos `efeitos`
 *     perícias                 : atributo, dificuldade, predefinido, página
 *     técnicas                 : dificuldade, predefinido, página
 *
 * Veredito por campo:
 *   ok               o catálogo concorda com o livro
 *   erro             discorda das testemunhas que o livro dá (as duas, ou a única que existe)
 *   livro-discorda   a entrada e a tabela divergem ENTRE SI e o catálogo concorda com uma delas
 *   nao-comparavel   não há número/forma a conferir (nunca entra na taxa)
 *
 * Uso: node scripts/catalogos/amostra-mc6.js [--escrever]
 * ⚠️ Nada do livro é copiado para o relatório: só nome, página e números.
 */
const fs = require('fs');
const path = require('path');
const L = require('./livro');
const C = require('./comparar');
const T = require('./tabelas');

const SEMENTE = 20261014;
const TAMANHO = 40;

function gerador(semente) {
  let a = semente | 0;
  return function () {
    a = (a + 0x6D2B79F5) | 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

/** `n` itens, sorteados sem repetição, devolvidos na ordem do catálogo. */
function sortear(lista, n, semente) {
  const idx = lista.map((_, i) => i);
  const r = gerador(semente);
  for (let i = idx.length - 1; i > 0; i--) {
    const j = Math.floor(r() * (i + 1));
    [idx[i], idx[j]] = [idx[j], idx[i]];
  }
  return idx.slice(0, Math.min(n, lista.length)).sort((a, b) => a - b).map((i) => lista[i]);
}

const lerJson = (f) => JSON.parse(fs.readFileSync(path.join(L.ASSETS, f), 'utf8'));

function catalogos() {
  const itens = (j) => (Array.isArray(j) ? j : j.items);
  return {
    vantagens: { arquivo: 'vantagens.v3.json', itens: itens(lerJson('vantagens.v3.json')), tipo: 'custo' },
    desvantagens: { arquivo: 'desvantagens.v2.json', itens: itens(lerJson('desvantagens.v2.json')), tipo: 'custo' },
    pericias: { arquivo: 'pericias.v3.json', itens: itens(lerJson('pericias.v3.json')), tipo: 'pericia' },
    tecnicas: { arquivo: 'tecnicas.v1.json', itens: itens(lerJson('tecnicas.v1.json')), tipo: 'tecnica' },
  };
}

/** A página que o catálogo declara (número), ou null. */
function paginaDoItem(item) {
  const cand = item.pagina != null ? item.pagina : item.paginaRaw;
  const m = /\d+/.exec(String(cand == null ? '' : cand));
  const n = m ? parseInt(m[0], 10) : null;
  return n && n > 0 ? n : null;
}

/** O texto de "Predefinido:" dentro do bloco, ou "". */
function predefinidoDoBloco(bloco) {
  const inicio = /^[*|\s_]*Predefinid[oa]s?[*_]*:?[*_]*:?\s*/i;
  const linha = bloco.find((l) => inicio.test(l.trim()));
  if (!linha) return '';
  return linha.trim().replace(inicio, '').replace(/[*|_]+/g, '').trim();
}

const preDoItem = (item) => (item.preDefinido && typeof item.preDefinido === 'object' ? item.preDefinido.raw : item.preDefinido);

/** Testemunhas: veredito de cada campo pela ENTRADA e pela TABELA. */
function pelaEntrada(tipo, item, entrada, bloco) {
  const v = {};
  if (tipo === 'custo') {
    v.custo = entrada.cabecalho.remissao
      ? { veredito: 'nao-comparavel', motivo: 'o livro só remete a outra página', catalogo: String(item.rawCost) }
      : C.compararCusto(item.rawCost, entrada.cabecalho.texto);
  } else if (tipo === 'pericia') {
    const c = C.compararPericia(item, entrada.cabecalho.texto);
    v.atributo = c.atributo;
    v.dificuldade = c.dificuldade;
    v.predefinido = C.compararPredefinido(preDoItem(item), predefinidoDoBloco(bloco));
  } else {
    v.dificuldade = C.compararDificuldadeDeTecnica(item.dificuldadeRaw, entrada.cabecalho.texto);
    v.predefinido = C.compararPredefinido(item.preDefinidoRaw, predefinidoDoBloco(bloco));
  }
  return v;
}

const rotuloDificuldade = { F: 'Fácil', M: 'Média', D: 'Difícil', MD: 'Muito Difícil' };

function pelaTabela(tipo, item, ocorrencias, paginaCatalogo) {
  // mais de uma linha com o mesmo nome (vantagem E desvantagem, ou variantes): vale a que concorda
  const tentativas = ocorrencias.map((o) => {
    const v = {};
    if (tipo === 'custo') {
      v.custo = C.compararCusto(item.rawCost, o.custo);
    } else if (tipo === 'pericia') {
      const c = C.compararPericia(item, `${o.atributo}/${rotuloDificuldade[o.dificuldade] || o.dificuldade}`);
      v.atributo = c.atributo;
      v.dificuldade = c.dificuldade;
      v.predefinido = C.compararPredefinido(preDoItem(item), o.predefinido);
    } else {
      v.dificuldade = C.compararDificuldadeDeTecnica(item.dificuldadeRaw, rotuloDificuldade[o.dificuldade] || o.dificuldade);
      v.predefinido = C.compararPredefinido(item.preDefinidoRaw, o.predefinido);
    }
    return { v, o };
  });
  // Prefere a linha da tabela que cai na página que o catálogo declara (a vantagem e a desvantagem
  // de mesmo nome — Forma de Sombras — têm páginas diferentes); depois, a que não discorda.
  const daPagina = paginaCatalogo == null ? null : tentativas.find((t) => t.o.pagina === paginaCatalogo);
  const melhor = daPagina
    || tentativas.find((t) => Object.values(t.v).every((x) => x.veredito !== 'erro'))
    || tentativas[0];
  return { v: melhor.v, pagina: melhor.o.pagina };
}

function conferirItem(nomeDoCatalogo, tipo, item, paginas, indice, tabelas) {
  const nome = item.nome;
  const dica = paginaDoItem(item);
  const fonteDoItem = tipo === 'tecnica' ? (/gun/i.test(String(item.sourceBook || '')) ? 'pt_gun_fu' : 'pt_artes_marciais') : null;
  const entrada = L.acharEntrada(paginas, indice, nome, tipo, dica, fonteDoItem);
  const r = { catalogo: nomeDoCatalogo, id: item.id, nome, paginaCatalogo: dica, localizado: !!entrada, entrada: {}, tabela: null };

  // a tabela de técnicas é a do Artes Marciais: as de Gun Fu não têm tabela (casariam por nome com outra)
  const semTabela = tipo === 'tecnica' && /gun/i.test(String(item.sourceBook || ''));
  const mapa = semTabela ? new Map() : (tipo === 'custo' ? tabelas.custos : (tipo === 'pericia' ? tabelas.pericias : tabelas.tecnicas));
  const chaves = [L.normalizar(nome), L.normalizar(String(nome).replace(/\([^)]*\)|\/NT|†/g, ' '))];
  let ocorrencias = null;
  for (const k of chaves) { if (mapa.has(k)) { ocorrencias = mapa.get(k); break; } }
  if (ocorrencias) {
    const t = pelaTabela(tipo, item, ocorrencias, dica);
    r.tabela = { campos: t.v, pagina: t.pagina };
  }

  if (entrada) {
    // o texto da entrada inclui a linha do título e a do custo (o custo pode estar no título)
    const bloco = [paginas[entrada.p].linhas[entrada.idx], entrada.cabecalho.texto]
      .concat(L.blocoDaEntrada(paginas, entrada, tipo));
    r.fonte = entrada.fonte;
    r.paginaLivro = entrada.pagina;
    r.candidatos = entrada.total;
    r.entrada = pelaEntrada(tipo, item, entrada, bloco);
    // os números da `descricao` do catálogo existem na entrada do livro? (a doença das magias)
    r.entrada.descricao = entrada.cabecalho.remissao
      ? { veredito: 'nao-comparavel', motivo: 'o livro só remete a outra página' }
      : C.compararDescricao(item.descricao, bloco.join('\n'));
    if (item.efeitos && item.efeitos.length) {
      // a entrada só remete a outra ("V. Talento, pág. 90"): não há texto próprio para conferir
      r.efeitos = entrada.cabecalho.remissao
        ? { veredito: 'nao-comparavel', motivo: 'o livro só remete a outra página', total: 0, numerosAusentes: [], alvosAusentes: [] }
        : C.compararEfeitos(item.efeitos, bloco.join('\n'));
    }
  }
  return r;
}

/** A diferença modal (página do chunk − página impressa da tabela) por fonte: o deslocamento do livro. */
function deslocamentosDoLivro(resultados) {
  const porFonte = {};
  resultados.filter((r) => r.localizado && r.tabela && r.tabela.pagina != null).forEach((r) => {
    const d = r.paginaLivro - r.tabela.pagina;
    porFonte[r.fonte] = porFonte[r.fonte] || {};
    porFonte[r.fonte][d] = (porFonte[r.fonte][d] || 0) + 1;
  });
  const modal = {};
  Object.keys(porFonte).forEach((f) => {
    const [d] = Object.entries(porFonte[f]).sort((a, b) => b[1] - a[1])[0];
    modal[f] = { deslocamento: parseInt(d, 10), distribuicao: porFonte[f] };
  });
  return modal;
}

/** Junta as duas testemunhas num veredito por campo (regras no cabeçalho do arquivo). */
function combinar(e, t) {
  if (e && e.veredito === 'ok') return { ...e, por: t && t.veredito === 'ok' ? 'entrada+tabela' : 'entrada' };
  if (e && e.veredito === 'erro') {
    if (t && t.veredito === 'ok') return { ...e, veredito: 'livro-discorda', por: 'a tabela concorda com o catálogo' };
    return { ...e, por: t && t.veredito === 'erro' ? 'entrada+tabela' : 'entrada' };
  }
  if (t && t.veredito !== 'nao-comparavel') return { ...t, por: 'tabela' };
  return e || t || { veredito: 'nao-comparavel' };
}

function fechar(resultados, deslocamentos) {
  resultados.forEach((r) => {
    r.campos = {};
    const nomes = new Set(Object.keys(r.entrada).concat(r.tabela ? Object.keys(r.tabela.campos) : []));
    nomes.forEach((c) => { r.campos[c] = combinar(r.entrada[c], r.tabela && r.tabela.campos[c]); });
    if (r.efeitos) r.campos.efeitos = { ...r.efeitos, por: 'entrada' };

    // página: a da tabela é a impressa (exata); a da entrada precisa do deslocamento do livro
    if (r.paginaCatalogo != null) {
      if (r.tabela && r.tabela.pagina != null) {
        r.campos.pagina = r.tabela.pagina === r.paginaCatalogo
          ? { veredito: 'ok', por: 'tabela' }
          : { veredito: 'erro', catalogo: r.paginaCatalogo, livro: r.tabela.pagina, por: 'tabela' };
      } else if (r.localizado && deslocamentos[r.fonte]) {
        const impressa = r.paginaLivro - deslocamentos[r.fonte].deslocamento;
        r.campos.pagina = impressa === r.paginaCatalogo
          ? { veredito: 'ok', por: 'entrada' }
          : { veredito: 'erro', catalogo: r.paginaCatalogo, livro: impressa, por: 'entrada' };
      }
    }
  });
}

/** Taxas por campo: ok, erro, livro-discorda, nao-comparavel. */
function resumir(resultados) {
  const campos = {};
  resultados.forEach((r) => Object.entries(r.campos || {}).forEach(([c, v]) => {
    campos[c] = campos[c] || { ok: 0, erro: 0, 'livro-discorda': 0, 'nao-comparavel': 0 };
    campos[c][v.veredito] = (campos[c][v.veredito] || 0) + 1;
  }));
  Object.values(campos).forEach((x) => {
    const base = x.ok + x.erro;
    x.taxaDeErro = base ? Math.round((1000 * x.erro) / base) / 10 : null;
  });
  return campos;
}

function main() {
  const escrever = process.argv.includes('--escrever');
  const paginas = L.carregarLivro();
  const indice = L.construirIndice(paginas);
  const tabelas = T.lerTabelas(paginas);
  const cats = catalogos();
  const saida = {
    semente: SEMENTE, tamanho: TAMANHO,
    tabelas: { custos: tabelas.custos.size, pericias: tabelas.pericias.size, tecnicas: tabelas.tecnicas.size },
    catalogos: {},
  };
  let k = 0;
  const todos = [];
  Object.entries(cats).forEach(([nome, cat]) => {
    k++;
    const amostra = sortear(cat.itens, TAMANHO, SEMENTE + k)
      .map((it) => conferirItem(nome, cat.tipo, it, paginas, indice, tabelas));
    // 2ª amostra: só os que têm `efeitos` (a geral traz poucos), para a taxa de erro valer
    let comEfeitos = [];
    if (nome === 'vantagens' || nome === 'desvantagens') {
      const pool = cat.itens.filter((it) => it.efeitos && it.efeitos.length);
      comEfeitos = sortear(pool, TAMANHO, SEMENTE + 100 + k)
        .map((it) => conferirItem(nome, cat.tipo, it, paginas, indice, tabelas));
    }
    saida.catalogos[nome] = { arquivo: cat.arquivo, total: cat.itens.length, tipo: cat.tipo, amostra, amostraComEfeitos: comEfeitos };
    todos.push(...amostra, ...comEfeitos);
  });

  saida.deslocamentos = deslocamentosDoLivro(todos);
  // Fonte sem tabela (Gun Fu): o deslocamento sai da diferença MODAL entre a entrada e o catálogo.
  // ⚠️ Isto herda o viés do catálogo (se ele errasse sempre igual, passaria): vai marcado.
  const distCat = {};
  todos.filter((r) => r.localizado && r.paginaCatalogo != null && !saida.deslocamentos[r.fonte]).forEach((r) => {
    distCat[r.fonte] = distCat[r.fonte] || {};
    const d = r.paginaLivro - r.paginaCatalogo;
    distCat[r.fonte][d] = (distCat[r.fonte][d] || 0) + 1;
  });
  Object.entries(distCat).forEach(([f, dist]) => {
    const [d] = Object.entries(dist).sort((a, b) => b[1] - a[1])[0];
    saida.deslocamentos[f] = { deslocamento: parseInt(d, 10), distribuicao: dist, calibragem: 'catalogo (fonte sem tabela)' };
  });
  fechar(todos, saida.deslocamentos);
  Object.values(saida.catalogos).forEach((c) => {
    c.resumo = resumir(c.amostra);
    c.resumoEfeitos = c.amostraComEfeitos.length ? { efeitos: resumir(c.amostraComEfeitos).efeitos } : null;
    c.localizadas = c.amostra.filter((r) => r.localizado).length;
    c.comTabela = c.amostra.filter((r) => r.tabela).length;
  });

  console.log('tabelas lidas:', JSON.stringify(saida.tabelas), '| deslocamentos:', JSON.stringify(
    Object.fromEntries(Object.entries(saida.deslocamentos).map(([f, d]) => [f, d.deslocamento]))));
  Object.entries(saida.catalogos).forEach(([nome, c]) => {
    console.log(nome.padEnd(13), 'total', String(c.total).padStart(4), '| localizadas', c.localizadas, '| com tabela', c.comTabela);
    Object.entries(c.resumo).forEach(([campo, x]) => console.log('   ', campo.padEnd(12), JSON.stringify(x)));
    if (c.resumoEfeitos && c.resumoEfeitos.efeitos) console.log('    efeitos(2a)  ', JSON.stringify(c.resumoEfeitos.efeitos));
  });
  if (escrever) {
    fs.writeFileSync(path.join(__dirname, 'amostra-mc6.json'), JSON.stringify(saida, null, 2) + '\n');
    console.log('gravado amostra-mc6.json');
  }
  return saida;
}

if (require.main === module) main();
module.exports = {
  sortear, gerador, paginaDoItem, predefinidoDoBloco, conferirItem, deslocamentosDoLivro, combinar, fechar, resumir, main,
};
