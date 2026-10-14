#!/usr/bin/env node
'use strict';
/**
 * MC-1 — aplica o cabeçalho do livro ao `magias2versao.json`, por programa.
 *
 *   node scripts/magias/aplicar-cabecalho-da-tabela.js --simular        # só mostra as contas
 *   node scripts/magias/aplicar-cabecalho-da-tabela.js                   # grava catálogo + exceções
 *   node scripts/magias/aplicar-cabecalho-da-tabela.js --relatorio <caminho.md>
 *
 * As regras (o que vem da tabela, o que a entrada arbitra, o que não se mexe) estão em
 * `cabecalho.js`. Nada é digitado: todo texto novo sai do `chunks.jsonl`.
 *
 * Grava: o catálogo; `cabecalho-excecoes.json` (o que ficou diferente da tabela, e por quê — o teste
 * do app deixa passar SÓ isso); e o relatório de divergências.
 */
const fs = require('fs');
const path = require('path');
const L = require('./localizador');
const C = require('./cabecalho');
const P = require('./prerequisitos');

const RAIZ = path.resolve(__dirname, '..', '..');
const CHUNKS = path.join(RAIZ, 'app', 'src', 'main', 'assets', 'chunks.jsonl');
const CATALOGO = path.join(RAIZ, 'app', 'src', 'main', 'assets', 'magias2versao.json');
const MANUAL = path.join(__dirname, 'magias-localizacao-manual.json');
const EXCECOES = path.join(__dirname, 'cabecalho-excecoes.json');
const CODIGO = path.join(RAIZ, 'app', 'src', 'main', 'java');

const CAMPOS_DO_CABECALHO = ['duracao', 'energia', 'tempoOperacao', 'classe', 'pagina'];

function argumento(nome) {
  const i = process.argv.indexOf(nome);
  return i >= 0 ? process.argv[i + 1] : null;
}

/** Os nomes de magia que o código do app procura por texto (`hasMagia("Animação")`…). */
function nomesQueOMotorProcura() {
  const nomes = new Set();
  const anda = (dir) => {
    fs.readdirSync(dir, { withFileTypes: true }).forEach((d) => {
      const p = path.join(dir, d.name);
      if (d.isDirectory()) { anda(p); return; }
      if (!d.name.endsWith('.kt')) return;
      const s = fs.readFileSync(p, 'utf8');
      const re = /(?:hasMagia|nivelMagia)\("([^"]+)"\)|magia\.nome\.equals\("([^"]+)"/g;
      let m;
      while ((m = re.exec(s)) !== null) nomes.add(L.normalizar(m[1] || m[2]));
    });
  };
  if (fs.existsSync(CODIGO)) anda(CODIGO);
  return nomes;
}

/** O catálogo em texto, do jeito que o arquivo é gravado hoje: 2 espaços, `\n` no fim, CRLF. */
function textoDoCatalogo(arr) {
  return (JSON.stringify(arr, null, 2) + '\n').replace(/\n/g, '\r\n');
}

function calcular() {
  const livro = L.carregarLivro(CHUNKS);
  const bruto = fs.readFileSync(CATALOGO, 'utf8');
  const catalogo = JSON.parse(bruto);
  const manual = L.lerLocalizacaoManual(MANUAL);
  const indice = L.construirIndice(livro, catalogo, { manual });
  const vocabulario = C.vocabulario(livro);
  const literais = nomesQueOMotorProcura();
  const nomesJaUsados = new Set(catalogo.map((m) => L.normalizar(m.nome)));

  const resultado = [];
  catalogo.forEach((m) => {
    const x = indice.magias[m.id];
    const entrada = x.entrada && x.entrada.formato === 'bloco' ? L.camposDaEntrada(livro, x.entrada) : {};
    const d = C.decidir(m, x, entrada);

    // ---- o nome: só erro de digitação, nunca acento nem caixa; nunca um nome que o motor procura
    let novoNome = null;
    let motivoNome = null;
    const tab = x.tabela;
    // se o TÍTULO da própria entrada já é o nome do catálogo, o livro tem dois nomes para a magia
    // (título × tabela): não se troca ("Resfriar" na entrada, "Resfriar Área" na tabela)
    const tituloDaEntradaJaEOnome = !!(x.entrada && x.entrada.titulo && L.normalizar(x.entrada.titulo) === L.normalizar(m.nome));
    if (tab && !tituloDaEntradaJaEOnome && ['aproximado', 'prefixo-inverso'].includes(tab.casamento) && tab.nome
        && tab.nome !== m.nome && L.normalizar(tab.nome) !== L.normalizar(m.nome)) {
      novoNome = tab.nome;
      motivoNome = 'tabela (' + tab.casamento + ')';
    } else {
      const pelaLivro = C.nomeCorrigidoPeloLivro(m.nome, livro, vocabulario);
      if (pelaLivro && L.normalizar(pelaLivro) !== L.normalizar(m.nome)) {
        novoNome = pelaLivro;
        motivoNome = 'vocabulario do livro';
      }
    }
    if (novoNome) {
      if (literais.has(L.normalizar(m.nome))) { novoNome = null; motivoNome = null; }
      else if (nomesJaUsados.has(L.normalizar(novoNome))) { novoNome = null; motivoNome = null; }
    }
    resultado.push({ m, x, entrada, d, novoNome, motivoNome });
  });
  return { livro, bruto, catalogo, indice, resultado, literais };
}

/** Aplica as decisões sobre uma cópia do catálogo e devolve (novoCatalogo, excecoes, estatisticas). */
function aplicar(calc) {
  const novoCat = JSON.parse(JSON.stringify(calc.catalogo));
  const excecoes = {};
  const est = { total: novoCat.length, aplicadas: 0, familia: 0, semTabela: 0, mudou: {}, nomes: 0 };
  CAMPOS_DO_CABECALHO.forEach((c) => { est.mudou[c] = { igual: 0, formato: 0, valor: 0, mantido: 0 }; });

  calc.resultado.forEach((r, i) => {
    const m = novoCat[i];
    const { d } = r;
    Object.keys(d.novo).forEach((k) => { m[k] = d.novo[k]; });
    if (r.novoNome) { m.nome = r.novoNome; est.nomes++; }
    m.fonteCabecalho = d.fonte || 'sem-tabela';
    if (d.aplica) est.aplicadas++; else if (d.fonte === 'tabela-familia') est.familia++; else est.semTabela++;

    d.decisoes.forEach((s) => {
      if (est.mudou[s.campo]) est.mudou[s.campo][s.tipo] = (est.mudou[s.campo][s.tipo] || 0) + 1;
      // o que ficou DIFERENTE da tabela, de propósito (a guarda deixa passar só isto)
      const difereDaTabela = (s.campo !== 'preRequisitos') && d.aplica && s.tipo !== 'igual' && s.tipo !== 'formato' && s.depois !== null
        && (s.motivo || '').length > 0 && /^(mantido|o-livro|tres-fontes|tabela-)/.test(s.motivo || (s.tipo === 'mantido' ? 'mantido' : ''));
      const motivoFixo = s.tipo === 'mantido' || /o-livro-discorda|tres-fontes/.test(s.motivo || '');
      if (d.aplica && s.campo !== 'preRequisitos' && motivoFixo && s.motivo !== 'tabela-vazia') {
        (excecoes[r.m.id] = excecoes[r.m.id] || {})[s.campo] = {
          motivo: s.motivo,
          catalogo: s.depois,
          tabela: s.tabela !== undefined ? s.tabela : (r.x.tabela ? tabelaDoCampo(r.x.tabela, s.campo) : null),
          entrada: s.entrada !== undefined ? s.entrada : null,
        };
      }
      void difereDaTabela;
    });
  });
  return { novoCat, excecoes, est };
}

function tabelaDoCampo(t, campo) {
  if (campo === 'pagina') return t.pagina;
  if (campo === 'tempoOperacao') return t.campos.tempo;
  return t.campos[campo];
}

// ---------------------------------------------------------------------------------- relatório

function md(cabecalho, linhas) {
  if (!linhas.length) return ['_nenhuma_', ''];
  const esc = (v) => String(v == null ? '—' : v).replace(/\|/g, '\\|').replace(/\n/g, ' ');
  return ['| ' + cabecalho.join(' | ') + ' |', '|' + cabecalho.map(() => '---').join('|') + '|']
    .concat(linhas.map((l) => '| ' + l.map(esc).join(' | ') + ' |')).concat(['']);
}

function relatorio(calc, ap) {
  const out = [];
  const nome = (r) => r.novoNome ? r.m.nome + ' → ' + r.novoNome : r.m.nome;
  const pag = (r) => (r.x.tabela && r.x.tabela.pagina) || r.m.pagina || '—';
  const total = calc.catalogo.length;
  const estat = ap.est;

  out.push('# MC-1 — O cabeçalho do catálogo, igual ao livro');
  out.push('');
  out.push('Gerado por `scripts/magias/aplicar-cabecalho-da-tabela.js` (no app). Nada foi digitado à mão: todo texto novo saiu do `chunks.jsonl` por programa. Só nome, página e números aparecem aqui.');
  out.push('');
  out.push('## 1. O que mudou, em números');
  out.push('');
  out.push('| | Magias |');
  out.push('|---|---|');
  out.push('| No catálogo | ' + total + ' |');
  out.push('| Com a linha da tabela aplicada (`fonteCabecalho: "tabela"`) | ' + estat.aplicadas + ' |');
  out.push('| Variantes de família: o livro só tem a linha da família, **não escrito** (`tabela-familia`) | ' + estat.familia + ' |');
  out.push('| Sem linha na tabela | ' + estat.semTabela + ' |');
  out.push('| Nomes corrigidos (erro de digitação; o `id` não mudou) | ' + estat.nomes + ' |');
  out.push('');
  out.push('| Campo | Já igual | Só a forma ("04/02" → "4/2") | **Valor mudou** | Mantido (motivo na seção 4) |');
  out.push('|---|---|---|---|---|');
  CAMPOS_DO_CABECALHO.forEach((c) => {
    const o = estat.mudou[c];
    out.push('| ' + c + ' | ' + o.igual + ' | ' + o.formato + ' | **' + o.valor + '** | ' + o.mantido + ' |');
  });
  out.push('');
  out.push('"Só a forma" quer dizer: o leitor da Mesa extrai o mesmo número do texto antigo e do novo. Ex.: custo "04/02" → "4/2", tempo "3 seg." → "3 seg".');
  out.push('');

  out.push('## 2. 🔴 A tabela do livro tem defeitos de extração (já corrigidos na leitura)');
  out.push('');
  out.push('Foram achados lendo a tabela do `chunks.jsonl`; sem tratá-los, copiar a tabela estragaria o catálogo.');
  out.push('');
  const tab = L.lerTabela(calc.livro);
  out.push('- **Colunas Energia e Tempo trocadas na página ' + (tab.colunasTrocadas.join(', ') || '—') + '** do livro (' +
    tab.linhas.filter((t) => /colunas-trocadas/.test(t.reparada || '')).length + ' linhas): lá a ordem é Duração · Tempo · Energia (`10 min | 1 seg | 4/1#`), nas outras é Duração · Energia · Tempo. Detectado por conteúdo (a coluna de energia só tem unidade de tempo), não por número de página fixo.');
  out.push('- ' + tab.linhas.filter((t) => /celulas-coladas/.test(t.reparada || '')).length + ' linhas com energia e tempo colados na mesma célula ("3 seg=custo").');
  out.push('- ' + tab.linhas.filter((t) => /sem-pagina/.test(t.reparada || '')).length + ' linhas com a coluna "Pág." vazia.');
  out.push('- Texto sujo na própria tabela (não consertável por programa): classe "Comum/R/-HT", "Essencial/Comum", custo "6,2" por "6/2", unidade colada na energia (' +
    ap.est.mudou.energia.mantido + ' linhas de energia mantidas pelo texto de hoje).');
  out.push('');

  const rotulos = { duracao: 'Duração', energia: 'Energia (custo)', tempoOperacao: 'Tempo de operação', classe: 'Classe', pagina: 'Página' };
  out.push('## 3. 🔴 O VALOR mudou: catálogo de hoje × livro (lista nominal)');
  out.push('');
  out.push('O livro manda. Cada linha é uma magia em que o catálogo dizia **outra coisa** (não só outra escrita). A coluna "Fonte" diz de onde veio o texto novo: `tabela` ou `entrada` (a linha "Duração:"/"Tempo de operação:" da própria entrada, quando ela discorda da tabela).');
  out.push('');
  CAMPOS_DO_CABECALHO.forEach((campo) => {
    const linhas = [];
    calc.resultado.forEach((r) => {
      r.d.decisoes.forEach((s) => {
        if (s.campo !== campo || s.tipo !== 'valor') return;
        const daEntrada = /vale a entrada/.test(s.motivo || '');
        linhas.push([nome(r), pag(r), s.antes, s.depois, daEntrada ? 'entrada' : 'tabela',
          daEntrada ? (s.tabela !== undefined ? 'a tabela diz: ' + s.tabela : '') : (r.x.tabela.casamento !== 'exato' ? r.x.tabela.casamento : '')]);
      });
    });
    out.push('### ' + rotulos[campo] + ' (' + linhas.length + ')');
    out.push('');
    md(['Magia', 'Página', 'Antes (catálogo)', 'Depois (livro)', 'Fonte', 'Nota'], linhas).forEach((l) => out.push(l));
  });

  out.push('## 4. Onde o livro discorda de si mesmo, e o que ficou');
  out.push('');
  out.push('Regra do plano: a **entrada** (onde o livro explica a magia) vale mais que a tabela. Estes são os casos em que a entrada e a tabela dão números diferentes, ou em que a tabela ficou de fora por estar suja.');
  out.push('');
  const discordam = [];
  calc.resultado.forEach((r) => r.d.decisoes.forEach((s) => {
    if (s.campo === 'preRequisitos') return;
    if (s.tipo === 'mantido' || /vale a entrada/.test(s.motivo || '')) {
      discordam.push([nome(r), pag(r), rotulos[s.campo] || s.campo, s.antes, s.tabela !== undefined ? s.tabela : tabelaDoCampo(r.x.tabela, s.campo), s.entrada !== undefined ? s.entrada : null, s.depois, s.motivo]);
    }
  }));
  md(['Magia', 'Página', 'Campo', 'Catálogo (antes)', 'Tabela', 'Entrada', 'Ficou', 'Por quê'], discordam).forEach((l) => out.push(l));

  out.push('## 5. Variantes de família: o livro só tem a linha da família (não escrito)');
  out.push('');
  const fam = [];
  calc.resultado.forEach((r) => {
    if (r.d.fonte !== 'tabela-familia') return;
    const difs = r.d.decisoes.filter((s) => s.tipo === 'valor').map((s) => rotulos[s.campo] + ': «' + s.antes + '» × tabela «' + s.depois + '»');
    fam.push([r.m.nome, pag(r), r.x.tabela.casamento, r.x.tabela.nome, difs.join('; ') || 'igual']);
  });
  out.push('São ' + fam.length + ' magias. Onde o catálogo já concorda com a linha da família, nada a fazer; onde diferem, o catálogo tem o valor da variante e a tabela só diz o da família — o MC-2 confere pela entrada.');
  out.push('');
  md(['Magia', 'Página', 'Casamento', 'Linha da tabela', 'Diferenças'], fam).forEach((l) => out.push(l));

  out.push('## 6. Nomes corrigidos (o `id` não muda)');
  out.push('');
  md(['Antes', 'Depois', 'Página', 'Como'], calc.resultado.filter((r) => r.novoNome).map((r) => [r.m.nome, r.novoNome, pag(r), r.motivoNome])).forEach((l) => out.push(l));
  const soAcento = calc.resultado.filter((r) => r.x.tabela && r.x.tabela.casamento === 'exato' && r.x.tabela.nome !== r.m.nome);
  out.push('Fora estes, ' + soAcento.length + ' nomes diferem do livro só por acento, caixa ou pontuação. **Não foram trocados**: o app compara nome por texto em 17 regras (`hasMagia("Animação")`…) e a ficha guarda uma cópia do nome; trocar a grafia sem necessidade só arrisca quebrar uma delas.');
  out.push('');

  out.push('## 7. Pré-requisitos: NÃO foram mexidos (formas adaptadas para o motor de busca)');
  out.push('');
  out.push('O pré-requisito do catálogo **não é a transcrição do livro**: foi adaptado para o motor de busca de magias do app manter a cadeia de pré-requisitos (a regra do livro não mudou; acrescentaram-se as formas apropriadas — por exemplo, as variantes "(Criaturas do Ar/Mar/Terra)"). A tabela do livro abrevia ("AM2", "#" = "veja a entrada") e o `PreRequisitoParser` trata "#" como *não validar*; a entrada escreve em prosa. Por isso este lote **não toca** em `preRequisitos`. A lista abaixo é só **consulta** — o conteúdo que difere do texto do livro (nível de Aptidão Mágica, quantidade de mágicas, quais magias são citadas), para o Rodolfo reconhecer o que é adaptação e o que, se houver, escapou:');
  out.push('');
  const indiceNomes = P.indiceDeNomes(calc.catalogo);
  const nomePorId = new Map(calc.catalogo.map((m) => [m.id, m.nome]));
  const divergem = [];
  let comparadas = 0;
  calc.resultado.forEach((r) => {
    const E = r.entrada && r.entrada.preRequisitos;
    if (!E) return;
    comparadas++;
    const c = P.compararConteudo(r.m.preRequisitos || '', E.replace(/\.\s*$/, ''), indiceNomes);
    if (!c) return;
    const partes = [];
    if (c.am) partes.push('AM: catálogo ' + (c.am[0] == null ? '—' : c.am[0]) + ' × livro ' + (c.am[1] == null ? '—' : c.am[1]));
    if (c.quantidades) partes.push('quantidade: catálogo ' + JSON.stringify(c.quantidades[0]) + ' × livro ' + JSON.stringify(c.quantidades[1]));
    if (c.soNoCatalogo.length) partes.push('só o catálogo exige: ' + c.soNoCatalogo.map((id) => nomePorId.get(id)).join(', '));
    if (c.soNaEntrada.length) partes.push('só o livro exige: ' + c.soNaEntrada.map((id) => nomePorId.get(id)).join(', '));
    divergem.push([r.m.nome, pag(r), partes.join('; ')]);
  });
  out.push('- Com pré-requisito na entrada: ' + comparadas + ' magias. **Conteúdo diferente: ' + divergem.length + '.**');
  out.push('- Parte disso é redação ("pelo menos seis" × "6"; "Controle do Corpo" × "Corpo") que a comparação não entende, e parte é a adaptação para o motor de busca. **Nada aqui é para corrigir sem ele dizer** — é um mapa do que difere.');
  out.push('');
  md(['Magia', 'Página', 'O que diverge'], divergem).forEach((l) => out.push(l));
  return { texto: out.join('\n'), divergemPre: divergem.length, comparadasPre: comparadas };
}

// ---------------------------------------------------------------------------------- main

function main() {
  const calc = calcular();
  const ap = aplicar(calc);
  const rel = relatorio(calc, ap);
  const e = ap.est;
  console.log('Catálogo: ' + e.total + ' magias | tabela aplicada: ' + e.aplicadas + ' | família (não escrito): ' + e.familia + ' | sem tabela: ' + e.semTabela + ' | nomes: ' + e.nomes);
  CAMPOS_DO_CABECALHO.forEach((c) => {
    const o = e.mudou[c];
    console.log('  ' + c.padEnd(14) + ' igual ' + String(o.igual).padStart(3) + ' | forma ' + String(o.formato).padStart(3) + ' | VALOR ' + String(o.valor).padStart(3) + ' | mantido ' + String(o.mantido).padStart(3));
  });
  console.log('Pré-requisitos com conteúdo diferente da entrada: ' + rel.divergemPre + ' de ' + rel.comparadasPre);
  calc.resultado.filter((r) => r.novoNome).forEach((r) => console.log('  nome: ' + r.m.nome + ' -> ' + r.novoNome + ' [' + r.motivoNome + ']'));
  console.log('Exceções registradas: ' + Object.keys(ap.excecoes).length + ' magias');
  if (process.argv.includes('--simular')) return;

  fs.writeFileSync(CATALOGO, textoDoCatalogo(ap.novoCat));
  const saida = {
    gerado: 'por scripts/magias/aplicar-cabecalho-da-tabela.js (nunca à mão). O que ficou DIFERENTE da tabela do livro, de propósito: a guarda do app deixa passar só isto.',
    magias: ap.excecoes,
  };
  fs.writeFileSync(EXCECOES, JSON.stringify(saida, null, 1) + '\n');
  const relPath = argumento('--relatorio');
  if (relPath) fs.writeFileSync(relPath, rel.texto + '\n');
  console.log('Catálogo gravado. Exceções: ' + path.relative(RAIZ, EXCECOES) + (relPath ? ' | Relatório: ' + relPath : ''));
}

if (require.main === module) main();

module.exports = { calcular, aplicar, relatorio, textoDoCatalogo, nomesQueOMotorProcura, CAMPOS_DO_CABECALHO };
