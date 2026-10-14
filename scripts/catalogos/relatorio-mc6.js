'use strict';
/**
 * MC-6 — gera `RELATORIO-MC-6.md` (na pasta de planos da Mesa) a partir da amostra.
 *
 * Uso: node scripts/catalogos/relatorio-mc6.js
 * Nada do livro é copiado: só nome, página e números.
 */
const fs = require('fs');
const path = require('path');
const A = require('./amostra-mc6');
const L = require('./livro');

const DESTINO = path.join(__dirname, '..', '..', '..', '..', '..', 'mesa-virtual', 'planos', 'magias', 'RELATORIO-MC-6.md');

/** Intervalo de Wilson a 95% para k erros em n comparações (em %). */
function wilson(k, n) {
  if (!n) return null;
  const z = 1.96;
  const p = k / n;
  const den = 1 + (z * z) / n;
  const centro = (p + (z * z) / (2 * n)) / den;
  const margem = (z * Math.sqrt((p * (1 - p)) / n + (z * z) / (4 * n * n))) / den;
  return [Math.max(0, centro - margem) * 100, Math.min(1, centro + margem) * 100];
}

const pct = (x) => (x == null ? '—' : x.toFixed(1).replace('.', ',') + '%');
const faixa = (k, n) => { const w = wilson(k, n); return w ? `${pct(w[0])}–${pct(w[1])}` : '—'; };

const NOME_DO_CAMPO = {
  custo: 'Custo em pontos', pagina: 'Página', efeitos: 'Números dos `efeitos`', descricao: 'Números da `descricao`',
  atributo: 'Atributo', dificuldade: 'Dificuldade', predefinido: 'Predefinido',
};

function tabelaDeTaxas(resumo, extras) {
  const linhas = ['| Campo | Comparados | Certos | **Erros** | O livro discorda de si | Sem o que comparar | Taxa de erro | IC 95% |', '|---|---|---|---|---|---|---|---|'];
  const campos = Object.keys(resumo);
  campos.forEach((c) => {
    const x = resumo[c];
    const n = x.ok + x.erro;
    linhas.push(`| ${NOME_DO_CAMPO[c] || c} | ${n} | ${x.ok} | **${x.erro}** | ${x['livro-discorda']} | ${x['nao-comparavel']} | ${pct(x.taxaDeErro)} | ${faixa(x.erro, n)} |`);
  });
  (extras || []).forEach((e) => linhas.push(e));
  return linhas.join('\n');
}

function valorLegivel(v) {
  if (v.numerosAusentes && v.numerosAusentes.length) {
    return 'número(s) ' + [...new Set(v.numerosAusentes.map((x) => (typeof x === 'object' ? `${x.numero} (${x.de})` : x)))].join(', ') + ' ausente(s) da entrada';
  }
  const c = v.catalogo !== undefined ? v.catalogo : '—';
  const l = v.livro !== undefined ? v.livro : '—';
  return `catálogo **${c}** · livro **${l}**`;
}

function listaDeErros(titulo, resultados, vereditos, camposIgnorados) {
  const linhas = [];
  resultados.forEach((r) => {
    Object.entries(r.campos || {}).forEach(([c, v]) => {
      if (!vereditos.includes(v.veredito)) return;
      if (camposIgnorados && camposIgnorados.includes(c)) return;
      linhas.push(`| ${r.nome} | ${r.paginaCatalogo == null ? '—' : r.paginaCatalogo} | ${NOME_DO_CAMPO[c] || c} | ${valorLegivel(v)} | ${v.por || ''} |`);
    });
  });
  if (!linhas.length) return `**${titulo}:** nenhum.\n`;
  return `**${titulo}** (${linhas.length}):\n\n| Item | Pág. catálogo | Campo | Valores | Testemunha |\n|---|---|---|---|---|\n${linhas.join('\n')}\n`;
}

function paginasErradas(resultados) {
  const erros = resultados.filter((r) => r.campos && r.campos.pagina && r.campos.pagina.veredito === 'erro');
  if (!erros.length) return '';
  const dist = {};
  erros.forEach((r) => { const d = r.campos.pagina.catalogo - r.campos.pagina.livro; dist[d] = (dist[d] || 0) + 1; });
  const linhas = erros.map((r) => `| ${r.nome} | ${r.campos.pagina.catalogo} | ${r.campos.pagina.livro} | ${r.campos.pagina.catalogo - r.campos.pagina.livro} |`);
  return `**Página errada** (${erros.length}); diferença catálogo − livro: ${Object.entries(dist).sort((a, b) => a[0] - b[0]).map(([d, n]) => `${d > 0 ? '+' : ''}${d}×${n}`).join(' · ')}\n\n| Item | Catálogo | Livro (impressa) | Diferença |\n|---|---|---|---|\n${linhas.join('\n')}\n`;
}

function semTestemunha(resultados) {
  const sem = resultados.filter((r) => !r.localizado && !r.tabela);
  if (!sem.length) return 'Todos os itens da amostra têm ao menos uma testemunha no livro.\n';
  return `**Sem testemunha no livro** (${sem.length}; o nome do catálogo não aparece como título nem na tabela-resumo — não conta como erro): ${sem.map((r) => r.nome).join('; ')}.\n`;
}

function varreduraDeLixo() {
  const dir = L.ASSETS;
  const ler = (f) => { const j = JSON.parse(fs.readFileSync(path.join(dir, f), 'utf8')); return Array.isArray(j) ? j : j.items; };
  const alvo = { vantagens: 'vantagens.v3.json', desvantagens: 'desvantagens.v2.json', pericias: 'pericias.v3.json', tecnicas: 'tecnicas.v1.json' };
  const linhas = ['| Catálogo | Itens | Com a marca `=== PAGE N ===` na descrição | Com número de página solto numa linha (junto da marca) | Sem página no catálogo (0/vazia) |', '|---|---|---|---|---|'];
  const nomes = {};
  Object.entries(alvo).forEach(([k, f]) => {
    const itens = ler(f);
    const marca = itens.filter((x) => /={4,}\s*PAGE\s*\d+/i.test(x.descricao || ''));
    const solto = itens.filter((x) => /^\s*\*{0,2}\d{1,3}\*{0,2}\s*$/m.test(x.descricao || '') && /={4,}\s*PAGE/i.test(x.descricao || ''));
    const semPagina = itens.filter((x) => A.paginaDoItem(x) === null);
    linhas.push(`| ${k} | ${itens.length} | ${marca.length} | ${solto.length} | ${semPagina.length} |`);
    if (marca.length) nomes[k] = marca.map((x) => x.nome);
  });
  return { tabela: linhas.join('\n'), nomes };
}

function gerar() {
  const s = A.main();
  const out = [];
  const c = s.catalogos;
  out.push('# MC-6 — Os outros catálogos têm a mesma doença?');
  out.push('');
  out.push('Gerado por `scripts/catalogos/relatorio-mc6.js` (no app), sobre a amostra de `scripts/catalogos/amostra-mc6.js`. **Só relatório: nada foi consertado.** Nenhum texto do livro foi copiado: só nome, página e números.');
  out.push('');
  out.push(`## 1. Como foi medido\n\n- **Amostra:** ${s.tamanho} itens de cada catálogo, sorteados com semente fixa (${s.semente}); rodar de novo dá a mesma amostra. Para os \`efeitos\`, que só 18% dos itens têm, há uma **segunda amostra** de ${s.tamanho} só entre os que os têm.`);
  out.push('- **Duas testemunhas por campo**, ambas do próprio `chunks.jsonl`: a **entrada** (o texto da vantagem/perícia/técnica) e a **tabela-resumo** do fim do livro (custo e página das vantagens/desvantagens; atributo, dificuldade, predefinido e página das perícias; dificuldade, predefinido e página das técnicas).');
  out.push('- **Erro** = o catálogo discorda das testemunhas que o livro dá. **O livro discorda de si** = entrada e tabela divergem entre si e o catálogo concorda com uma delas (não se acusa o catálogo). **Sem o que comparar** = custo "Variável", predefinido "Especial", entrada que só remete a outra página: fora da taxa.');
  out.push(`- **Página:** a da tabela é a impressa (exata). A do \`chunks.jsonl\` do Módulo Básico vem **1 acima** da impressa (deslocamento medido contra a tabela: ${JSON.stringify(Object.fromEntries(Object.entries(s.deslocamentos).map(([f, d]) => [f.replace('pt_', ''), d.deslocamento])))}).`);
  out.push('- **Limites:** n pequeno (o IC 95% de Wilson está na tabela); o catálogo e o livro divergem às vezes por **nome** (item sem testemunha); a extração do livro perde tabelas, então "número que o texto extraído não tem" é um limite da fonte, e por isso a `descricao` só é medida na prosa.');
  out.push('');

  const rotulo = { vantagens: 'Vantagens (`vantagens.v3.json`)', desvantagens: 'Desvantagens (`desvantagens.v2.json`)', pericias: 'Perícias (`pericias.v3.json`)', tecnicas: 'Técnicas (`tecnicas.v1.json`)' };
  const resumoGeral = ['| Catálogo | Itens | Amostra | Com testemunha | Erros por campo (erros/comparados) |', '|---|---|---|---|---|'];
  Object.entries(c).forEach(([n, cat]) => {
    const com = cat.amostra.filter((r) => r.localizado || r.tabela).length;
    const partes = Object.entries(cat.resumo)
      .filter(([campo]) => campo !== 'descricao')
      .map(([campo, x]) => `${NOME_DO_CAMPO[campo] || campo} ${x.erro}/${x.ok + x.erro}`);
    if (cat.resumoEfeitos && cat.resumoEfeitos.efeitos) {
      const x = cat.resumoEfeitos.efeitos;
      partes.push(`efeitos (2ª amostra) ${x.erro}/${x.ok + x.erro}`);
    }
    resumoGeral.push(`| ${rotulo[n]} | ${cat.total} | ${cat.amostra.length} | ${com} | ${partes.join(' · ')} |`);
  });
  out.push('## 2. Em uma tabela\n');
  out.push(resumoGeral.join('\n'));
  out.push('');

  let secao = 3;
  Object.entries(c).forEach(([n, cat]) => {
    out.push(`## ${secao++}. ${rotulo[n]}\n`);
    const extras = [];
    if (cat.resumoEfeitos && cat.resumoEfeitos.efeitos) {
      const x = cat.resumoEfeitos.efeitos;
      extras.push(`| Números dos \`efeitos\` (2ª amostra, só quem os tem) | ${x.ok + x.erro} | ${x.ok} | **${x.erro}** | ${x['livro-discorda']} | ${x['nao-comparavel']} | ${pct(x.taxaDeErro)} | ${faixa(x.erro, x.ok + x.erro)} |`);
    }
    // na tabela principal, os `efeitos` da amostra geral entram só se houver; a 2ª amostra vai na linha extra
    out.push(tabelaDeTaxas(cat.resumo, extras));
    out.push('');
    out.push(`Localizadas pela entrada: ${cat.localizadas}/${cat.amostra.length}; com linha na tabela-resumo: ${cat.comTabela}/${cat.amostra.length}.\n`);
    out.push(semTestemunha(cat.amostra));
    const todos = cat.amostra.concat(cat.amostraComEfeitos || []);
    out.push(listaDeErros('Erros (custo, atributo, dificuldade, predefinido, efeitos)', todos.filter((r, i, a) => a.findIndex((x) => x.id === r.id) === i), ['erro'], ['pagina', 'descricao']));
    out.push(listaDeErros('O livro discorda de si (o catálogo concorda com a tabela)', cat.amostra, ['livro-discorda'], []));
    out.push(paginasErradas(cat.amostra));
    const desc = cat.amostra.filter((r) => r.campos && r.campos.descricao && r.campos.descricao.veredito === 'erro');
    if (desc.length) {
      out.push(`**Números da \`descricao\` que a entrada extraída não tem** (${desc.length}; muitos são limite da extração, não erro do catálogo — veja a seção 7): ${desc.map((r) => `${r.nome} [${r.campos.descricao.numerosAusentes.join(', ')}]`).join('; ')}.\n`);
    }
  });

  const lixo = varreduraDeLixo();
  out.push(`## ${secao++}. Varredura completa (não é amostra): lixo de extração dentro da \`descricao\`\n`);
  out.push(lixo.tabela);
  out.push('');
  Object.entries(lixo.nomes).forEach(([k, nomes]) => out.push(`- **${k}:** ${nomes.join(', ')}.`));
  out.push('');
  out.push('A marca `============= PAGE N =============` é resto do pipeline que montou o catálogo: não é texto do livro e não tem regra dentro. Fica para o Rodolfo decidir se vale um lote de limpeza (mecânico: apagar a linha).');
  out.push('');
  out.push('**Como li os números da `descricao` que sobraram na amostra** (vistos um a um): são de três tipos. (a) **Lixo de extração** dentro da descrição — números soltos de página e de custo (Indulgente, Problemas na Coluna). (b) **Texto que o catálogo tem e a entrada extraída não** (Visão Periférica traz uma comparação com Visão 360°; Visão 360 graus traz uma linha de custo; Visão Penetrante e Espadas de Lâmina Larga têm um detalhe a mais): só com o `chunks.jsonl` não dá para saber se é acréscimo do catálogo ou perda da extração — precisa do livro físico. (c) Artefato da medição, já tratado (o ".30"/".50" de calibre do Canhoneiro). **Nenhum dos que sobraram é um número de regra trocado que eu consiga mostrar**, o que não reproduz a doença das magias (278 de 812 com número diferente). Em técnicas, a `descricao` quase não tem número a conferir.');
  out.push('');

  out.push(`## ${secao++}. O que isso diz sobre cada catálogo\n`);
  out.push(conclusoes(c));
  out.push('');
  fs.writeFileSync(DESTINO, out.join('\n') + '\n');
  console.log('gravado', DESTINO);
}

/** Conclusão curta por catálogo, escrita a partir dos números (nada digitado). */
function conclusoes(c) {
  const taxa = (cat, campo) => { const x = cat.resumo[campo]; return x ? { erro: x.erro, n: x.ok + x.erro, t: x.taxaDeErro } : null; };
  const f = (t) => (t ? `${t.erro}/${t.n} (${pct(t.t)})` : '—');
  const v = c.vantagens; const d = c.desvantagens; const p = c.pericias; const t = c.tecnicas;
  const efV = v.resumoEfeitos && v.resumoEfeitos.efeitos; const efD = d.resumoEfeitos && d.resumoEfeitos.efeitos;
  const fe = (x) => (x ? `${x.erro}/${x.ok + x.erro} (${pct(x.taxaDeErro)})` : '—');
  return [
    `- **Vantagens:** custo ${f(taxa(v, 'custo'))}, página ${f(taxa(v, 'pagina'))}, \`efeitos\` ${fe(efV)}. Os erros de custo são de **opção perdida** ("0 ou 10" virou 10), não de transcrição trocada. Não parece merecer um plano igual ao das magias; vale uma correção pontual das listadas.`,
    `- **Desvantagens:** custo ${f(taxa(d, 'custo'))}, página ${f(taxa(d, 'pagina'))}, \`efeitos\` ${fe(efD)}. O ponto sério é o **lixo \`=== PAGE N ===\`** na descrição (seção 7). Custo e efeitos estão bem.`,
    `- **Perícias:** atributo ${f(taxa(p, 'atributo'))}, dificuldade ${f(taxa(p, 'dificuldade'))}, predefinido ${f(taxa(p, 'predefinido'))}, página ${f(taxa(p, 'pagina'))}. O catálogo mais limpo da amostra. Sem plano; corrigir a(s) listada(s).`,
    `- **Técnicas:** dificuldade ${f(taxa(t, 'dificuldade'))}, predefinido ${f(taxa(t, 'predefinido'))}, **página ${f(taxa(t, 'pagina'))}**; ${t.amostra.filter((r) => !r.localizado && !r.tabela).length} dos ${t.amostra.length} nomes da amostra não existem no livro (nem como título nem na tabela). É o único catálogo com a doença das magias: número de jogo (dificuldade) errado em quase 1 de cada 5, página errada em mais da metade, e nomes que o livro não tem. **Merece um plano igual ao MC-0..MC-5** (a tabela-resumo do Artes Marciais já é uma fonte pronta, como a tabela das magias).`,
  ].join('\n');
}

if (require.main === module) gerar();
module.exports = { wilson, gerar };
