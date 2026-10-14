#!/usr/bin/env node
'use strict';
/**
 * MC-4 — a `mecanica` das magias revista contra o livro, por programa.
 *
 *   node scripts/magias/aplicar-mecanica.js --simular
 *   node scripts/magias/aplicar-mecanica.js --relatorio <caminho.md>
 *
 * 1. ENTREGA × CLASSE (a classe do livro manda): "projetil" só em classe Projétil; "toque" só em
 *    classe Toque ou onde o TEXTO da entrada exige (lista abaixo, lida na CONFERENCIA e no texto).
 * 2. RESISTÊNCIA: o atributo da `condicaoResistencia` × o da classe da tabela (só lista).
 * 3. DANO/CURA: o número da `mecanica` × o do texto da entrada; o que diverge vira lista.
 * 4. PROJÉTEIS: 1/2D, Máx e Prec do texto da entrada, onde o catálogo não tem.
 * 5. `mecanicaConferida: true` onde tudo o que se podia conferir passou e há texto do livro.
 *
 * Nenhum número é digitado. As `notas` (resumo nosso) NÃO são tocadas.
 */
const fs = require('fs');
const path = require('path');
const { execFileSync } = require('child_process');
const L = require('./localizador');
const M = require('./mecanica');
const X = require('./mecanica-extra');

const RAIZ = path.resolve(__dirname, '..', '..');
const CHUNKS = path.join(RAIZ, 'app', 'src', 'main', 'assets', 'chunks.jsonl');
const CATALOGO = path.join(RAIZ, 'app', 'src', 'main', 'assets', 'magias2versao.json');
const MANUAL = path.join(__dirname, 'mecanica-lida-na-pagina.json');

/** O TEXTO da entrada exige tocar o ALVO (lido pela CONFERENCIA §5 e confirmado no texto). */
const TOQUE_PELO_TEXTO = ['Purificar', 'Conceder Mágica', 'Suspender Aptidão Mágica', 'Roubar Vitalidade', 'Roubar Energia', 'Leitura do Corpo']
  .map((n) => L.normalizar(n));
/** À distância afeta a vítima inteira; tocando, o dano fica limitado à parte tocada. */
const TOQUE_OPCIONAL = ['Desidratar', 'Geladura'].map((n) => L.normalizar(n));
/** Ataque de chicote, nem toque nem arremesso (CONFERENCIA §5). */
const CHICOTE = [L.normalizar('Chicote de Relampago')];
/** A bola flutua e explode; não se rola ataque (CONFERENCIA §4). */
const SEM_JOGADA_DE_ATAQUE = [L.normalizar('Bola de Relampagos')];

function argumento(nome) {
  const i = process.argv.indexOf(nome);
  return i >= 0 ? process.argv[i + 1] : null;
}
function textoDoCatalogo(arr) {
  return (JSON.stringify(arr, null, 2) + '\n').replace(/\n/g, '\r\n');
}
function catalogoDeAntes() {
  const arq = argumento('--antes');
  if (arq) return JSON.parse(fs.readFileSync(arq, 'utf8'));
  try {
    return JSON.parse(execFileSync('git', ['show', 'HEAD:app/src/main/assets/magias2versao.json'], { cwd: RAIZ, maxBuffer: 64 * 1024 * 1024 }).toString('utf8'));
  } catch (e) {
    return JSON.parse(fs.readFileSync(CATALOGO, 'utf8'));
  }
}

const ATRIBUTO_DA_CONDICAO = [[/^ht/i, 'ht'], [/^vont/i, 'von'], [/^dx/i, 'dx'], [/^st/i, 'st'], [/^iq/i, 'iq']];
function atributoDaCondicao(txt) {
  const t = String(txt || '').trim();
  for (const [re, a] of ATRIBUTO_DA_CONDICAO) if (re.test(t)) return a;
  return null;
}

/** Decide a entrega nova de UMA magia. Devolve `{ de, para, motivo, extra }` ou null. */
function decidirEntrega(m, classes) {
  const mec = m.mecanica || {};
  const nome = L.normalizar(m.nome);
  const e = mec.entrega === undefined ? null : mec.entrega;
  if (CHICOTE.includes(nome)) {
    return e === null ? null : { de: e, para: null, motivo: 'ataque de chicote (nem toque nem arremesso)', extra: { ataqueComoChicote: true } };
  }
  if (SEM_JOGADA_DE_ATAQUE.includes(nome)) return { de: e, para: e, motivo: 'a bola flutua e explode: sem jogada de ataque', extra: { semJogadaDeAtaque: true }, igual: true };
  if (e === 'projetil' && !classes.includes('Projétil')) {
    return { de: 'projetil', para: null, motivo: 'a classe do livro é ' + classes.join('+') + ', não Projétil' };
  }
  if (e === 'toque' && !classes.includes('Toque')) {
    if (TOQUE_OPCIONAL.includes(nome)) return { de: 'toque', para: 'toque', motivo: 'toque opcional (à distância afeta a vítima inteira)', extra: { toqueOpcional: true }, igual: true };
    if (TOQUE_PELO_TEXTO.includes(nome)) return null;
    return { de: 'toque', para: null, motivo: 'a classe do livro é ' + classes.join('+') + ' e o texto não exige tocar o alvo' };
  }
  if (e === null && TOQUE_PELO_TEXTO.includes(nome)) {
    return { de: null, para: 'toque', motivo: 'o texto do livro exige tocar o alvo' };
  }
  return null;
}

function calcular() {
  const lei = M.leitor();
  const livro = L.carregarLivro(CHUNKS);
  const catalogo = JSON.parse(fs.readFileSync(CATALOGO, 'utf8'));
  const manual = fs.existsSync(MANUAL) ? JSON.parse(fs.readFileSync(MANUAL, 'utf8')).magias : [];
  const apontadas = new Map();
  manual.forEach((x) => { if (!apontadas.has(x.id)) apontadas.set(x.id, []); apontadas.get(x.id).push(x); });
  const resultado = catalogo.map((m) => {
    const mec = m.mecanica || {};
    const classes = lei.aClasseDaMagia(m.classe).classes;
    const r = { m, classes, mudancas: [], extra: {}, motivos: [], lido: null, danoStatus: null, projetilStatus: null, resDiverge: null };

    // 1. entrega
    const ent = decidirEntrega(m, classes);
    if (ent) {
      if (!ent.igual) r.mudancas.push({ campo: 'entrega', de: ent.de, para: ent.para, motivo: ent.motivo });
      else r.mudancas.push({ campo: 'entrega', de: ent.de, para: ent.para, motivo: ent.motivo, semMudanca: true });
      r.entrega = ent;
      Object.assign(r.extra, ent.extra || {});
    }

    // 2. resistência
    const res = lei.aClasseDaMagia(m.classe).resistencia;
    const attrCond = atributoDaCondicao(mec.condicaoResistencia);
    // o atributo da classe: o escrito ("R-HT") ou o PRIMEIRO de uma resistência especial ("R-ST ou Vontade")
    let atributosDaClasse = [];
    if (res && res.atributo) atributosDaClasse = [res.atributo];
    else if (res && Array.isArray(res.atributos)) atributosDaClasse = res.atributos; // "ST ou Vontade"
    else if (res && res.especial && atributoDaCondicao(res.especial)) atributosDaClasse = [atributoDaCondicao(res.especial)];
    if (atributosDaClasse.length && attrCond && !atributosDaClasse.includes(attrCond)) {
      r.resDiverge = { classe: atributosDaClasse.join(' ou ') + (res.mod ? '(' + res.mod + ')' : ''), mecanica: mec.condicaoResistencia };
    }

    // 3. dano e cura
    const lidas = apontadas.get(m.id) || [];
    const porTipo = {};
    lidas.forEach((it) => { porTipo[it.tipo] = Object.assign({ pagina: it.pagina, linhas: it.linhas }, X.lerLinhasApontadas(livro, it)); });
    r.lidoPorTipo = porTipo;
    r.lido = porTipo.cura || porTipo.curaTotal || porTipo.projetil || porTipo.dano || null;
    if (mec.efeito === 'dano') {
      let d = M.conferirDano(m);
      if (d.status === 'sem-dado') { const f = X.danoPorFormula(m); if (f) d = Object.assign({}, d, f); }
      if (d.status === 'sem-texto' && porTipo.dano) {
        const l = porTipo.dano;
        const meu = String(mec.danoPorEnergia || '').trim();
        const okDado = l.dados.includes(meu);
        const okEnergia = l.energiaPorDado === null || mec.danoFixo || l.energiaPorDado === mec.energiaPorDado;
        d = { status: okDado && okEnergia ? 'confere' : 'diverge', livro: l.dados, energiaPorDado: l.energiaPorDado, daPagina: true };
      }
      r.danoStatus = d;
    }
    if (mec.efeito === 'cura' && r.lido) {
      const c = r.lido;
      let ok = true;
      if (c.tipo === 'cura') ok = c.maxPv === mec.curaMaxPv && c.porEnergia === mec.curaPvPorEnergia;
      if (c.tipo === 'curaTotal') ok = mec.curaTotal === true && c.custo === m.custoOperar;
      r.curaStatus = ok ? 'confere' : 'diverge';
    }

    // 4. projéteis
    if (classes.includes('Projétil')) {
      let p = M.conferirProjetil(m);
      if (p.status === 'sem-texto' && r.lidoPorTipo.projetil) {
        const n = r.lidoPorTipo.projetil.numeros;
        const meu = p.meu;
        const faltam = ['meioDano', 'maximo', 'precisao'].filter((c) => meu[c] === null && n[c] !== null);
        const divergem = ['meioDano', 'maximo', 'precisao'].filter((c) => meu[c] !== null && n[c] !== null && meu[c] !== n[c]);
        p = { status: divergem.length ? 'diverge' : (faltam.length ? 'completar' : 'confere'), meu, livro: n, faltam, divergem, naoAchou: [], daPagina: true };
      }
      r.projetilStatus = p;
    }
    return r;
  });
  return { livro, catalogo, resultado };
}

const CAMPOS_DO_PROJETIL = { meioDano: 'alcanceMeioDano', maximo: 'alcanceMaximo', precisao: 'precisao' };

/** Aplica sobre uma cópia: a entrega, os campos que o livro completa e a marca de conferência. */
function aplicar(calc) {
  return calc.resultado.map((r) => {
    const m = JSON.parse(JSON.stringify(r.m));
    const mec = m.mecanica;
    if (!mec) return m;
    // entrega
    if (r.entrega && !r.entrega.igual) {
      if (r.entrega.para === null) delete mec.entrega; else mec.entrega = r.entrega.para;
    }
    Object.assign(mec, r.extra);
    // projéteis: completa o que o livro dá e o catálogo não tem
    if (r.projetilStatus && r.projetilStatus.faltam) {
      r.projetilStatus.faltam.forEach((c) => { mec[CAMPOS_DO_PROJETIL[c]] = r.projetilStatus.livro[c]; });
    }
    // marca de conferência
    const motivos = [];
    const temTexto = m.descricaoConferida === true || Object.keys(r.lidoPorTipo || {}).length > 0;
    if (!temTexto) motivos.push('sem-texto-do-livro');
    if (r.danoStatus && !['confere'].includes(r.danoStatus.status)) motivos.push('dano-' + r.danoStatus.status);
    if (r.curaStatus === 'diverge') motivos.push('cura-diverge');
    if (r.projetilStatus && ['diverge', 'incompleto', 'sem-texto'].includes(r.projetilStatus.status)) {
      // "incompleto" = o livro não dá (Arremessar Mágica sem 1/2D, Teia de Aranha, Bola de Relâmpagos)
      if (r.projetilStatus.status !== 'incompleto') motivos.push('projetil-' + r.projetilStatus.status);
    }
    if (r.resDiverge) motivos.push('resistencia-diverge');
    r.motivos = motivos;
    const novo = {};
    Object.keys(m).forEach((k) => {
      if (['mecanicaConferida', 'mecanicaMotivo'].includes(k)) return;
      novo[k] = m[k];
      if (k === 'mecanica') {
        novo.mecanicaConferida = motivos.length === 0;
        if (motivos.length) novo.mecanicaMotivo = motivos.join(', ');
      }
    });
    return novo;
  });
}

// ---------------------------------------------------------------------------------- relatório

function md(cabecalho, linhas) {
  if (!linhas.length) return ['_nenhuma_', ''];
  const esc = (v) => String(v == null ? '—' : v).replace(/\|/g, '\\|').replace(/\n/g, ' ');
  return ['| ' + cabecalho.join(' | ') + ' |', '|' + cabecalho.map(() => '---').join('|') + '|']
    .concat(linhas.map((l) => '| ' + l.map(esc).join(' | ') + ' |')).concat(['']);
}

function relatorio(calc, aplicada) {
  const out = [];
  const pag = (r) => r.m.pagina || '—';
  const trocas = calc.resultado.filter((r) => r.mudancas.some((x) => !x.semMudanca));
  const total = calc.catalogo.length;
  const conferidas = aplicada.filter((m) => m.mecanicaConferida === true).length;
  const porMotivo = {};
  aplicada.forEach((m) => { if (m.mecanicaMotivo) m.mecanicaMotivo.split(', ').forEach((x) => { porMotivo[x] = (porMotivo[x] || 0) + 1; }); });

  out.push('# MC-4 — A `mecanica` das magias, confrontada com o livro');
  out.push('');
  out.push('Gerado por `scripts/magias/aplicar-mecanica.js` (no app). A `mecanica` foi curadoria por IA em cima da descrição errada. Aqui ela foi **confrontada com o bloco da entrada do livro** (a `descricao` do MC-3) e com a classe da tabela. O que o livro diz por número foi lido por programa; o que o texto extraído não permite ficou numa lista. As `notas` (resumo nosso) **não foram tocadas**. Só nome, página e números.');
  out.push('');
  out.push('## 1. Em números');
  out.push('');
  out.push('| | Magias |');
  out.push('|---|---|');
  out.push('| No catálogo | ' + total + ' |');
  out.push('| `entrega` mudou | ' + trocas.length + ' |');
  out.push('| `mecanicaConferida: true` | ' + conferidas + ' |');
  out.push('| `mecanicaConferida: false` | ' + (total - conferidas) + ' |');
  Object.entries(porMotivo).sort((a, b) => b[1] - a[1]).forEach(([k, v]) => out.push('| &nbsp;&nbsp;↳ ' + k + ' | ' + v + ' |'));
  out.push('');

  out.push('## 2. 🔴 A `entrega` agora segue a classe do livro (' + trocas.length + ')');
  out.push('');
  out.push('Regra: **projétil** só em classe Projétil; **toque** só em classe Toque ou onde o texto da entrada exige tocar o alvo. Classe Comum não pede jogada de ataque nem toque (Magia pp.11–12). Ver a CONFERENCIA §5 e o MG-0 §3.');
  out.push('');
  const porRegra = {};
  trocas.forEach((r) => r.mudancas.filter((x) => !x.semMudanca).forEach((x) => { const k = (x.de || '(nenhuma)') + ' → ' + (x.para || '(nenhuma)'); (porRegra[k] = porRegra[k] || []).push(r); }));
  md(['Mudança', 'Magias'], Object.entries(porRegra).map(([k, v]) => [k, v.length])).forEach((l) => out.push(l));
  md(['Magia', 'Página', 'Classe', 'Era', 'Ficou', 'Por quê'],
    trocas.sort((a, b) => a.m.nome.localeCompare(b.m.nome, 'pt')).map((r) => {
      const x = r.mudancas.find((y) => !y.semMudanca);
      return [r.m.nome, pag(r), r.m.classe, x.de || '—', x.para || '—', x.motivo];
    })).forEach((l) => out.push(l));

  out.push('## 3. Toque que fica, e por quê');
  out.push('');
  md(['Magia', 'Página', 'Classe', 'Motivo'],
    calc.resultado.filter((r) => {
      const e = (r.m.mecanica || {}).entrega;
      const novo = r.entrega ? r.entrega.para : e;
      return (novo === 'toque') && !r.classes.includes('Toque');
    }).map((r) => [r.m.nome, pag(r), r.m.classe, r.entrega && r.entrega.motivo ? r.entrega.motivo : 'o texto do livro exige tocar o alvo'])).forEach((l) => out.push(l));
  out.push('Marcadas à parte: **Chicote de Relâmpago** (`ataqueComoChicote`: alcance 2 a 8 m, nem toque nem arremesso) e **Bola de Relâmpagos** (`semJogadaDeAtaque`: flutua e explode; não se rola Ataque Inato).');
  out.push('');

  out.push('## 4. Dano e cura por energia: o número do catálogo × o do livro');
  out.push('');
  const dano = calc.resultado.filter((r) => r.danoStatus);
  const contD = {};
  dano.forEach((r) => { contD[r.danoStatus.status] = (contD[r.danoStatus.status] || 0) + 1; });
  md(['Situação', 'Magias'], Object.entries(contD).map(([k, v]) => [k, v])).forEach((l) => out.push(l));
  out.push('`confere`: o dado do catálogo aparece no texto da entrada (e a energia por dado bate quando o texto a diz). `sem-dado`: o texto não traz dado (dano em ponto fixo, fórmula). `sem-texto`: a descrição não é do livro (página embaralhada).');
  out.push('');
  md(['Magia', 'Página', 'Catálogo', 'Situação', 'O que o livro traz'],
    dano.filter((r) => r.danoStatus.status !== 'confere').map((r) => [r.m.nome, pag(r),
      (r.m.mecanica.danoPorEnergia || '—') + ' / ' + r.m.mecanica.energiaPorDado + (r.m.mecanica.danoFixo ? ' fixo' : ''),
      r.danoStatus.status, (r.danoStatus.livro && r.danoStatus.livro.length) ? r.danoStatus.livro.join(', ') : (r.danoStatus.formula || '—')])).forEach((l) => out.push(l));
  const curas = calc.resultado.filter((r) => r.lido && (r.lido.tipo === 'cura' || r.lido.tipo === 'curaTotal'));
  out.push('**Cura** (a página 91 é de colunas embaralhadas; os números vieram das linhas apontadas em `mecanica-lida-na-pagina.json`):');
  out.push('');
  md(['Magia', 'Página', 'Linhas', 'Catálogo', 'Livro', 'Situação'], curas.map((r) => {
    const k = r.m.mecanica;
    const cat = r.lido.tipo === 'cura' ? 'até ' + k.curaMaxPv + ' PV, ' + k.curaPvPorEnergia + ' por energia' : 'total, custo ' + r.m.custoOperar;
    const liv = r.lido.tipo === 'cura' ? 'até ' + r.lido.maxPv + ' PV, ' + r.lido.porEnergia + ' por energia' : 'custo ' + r.lido.custo;
    return [r.m.nome, r.lido.pagina, r.lido.linhas.join(', '), cat, liv, r.curaStatus];
  })).forEach((l) => out.push(l));

  out.push('## 5. Projéteis: 1/2D, Máx e Prec');
  out.push('');
  md(['Magia', 'Página', 'Catálogo (1/2D · Máx · Prec)', 'Livro', 'Situação'],
    calc.resultado.filter((r) => r.projetilStatus).map((r) => {
      const p = r.projetilStatus;
      const f = (o) => o ? [o.meioDano, o.maximo, o.precisao].map((v) => (v === null || v === undefined ? '—' : v)).join(' · ') : '—';
      return [r.m.nome, pag(r), f(p.meu), f(p.livro), p.status + (p.faltam && p.faltam.length ? ' (preenchido: ' + p.faltam.join(', ') + ')' : '') + (p.daPagina ? ' [linha apontada]' : '')];
    })).forEach((l) => out.push(l));
  out.push('"incompleto" = o livro não dá o número (Arremessar Mágica sem 1/2D; Teia de Aranha: o Máx é o comprimento do fio; Bola de Relâmpagos flutua).');
  out.push('');

  out.push('## 6. Resistência: o atributo da mecânica × o da classe da tabela');
  out.push('');
  const resD = calc.resultado.filter((r) => r.resDiverge);
  md(['Magia', 'Página', 'Classe da tabela', 'Mecânica diz'], resD.map((r) => [r.m.nome, pag(r), r.m.classe + ' (' + r.resDiverge.classe + ')', r.resDiverge.mecanica])).forEach((l) => out.push(l));
  out.push('Só entram aqui as que têm atributo nos dois lados e eles diferem. **Não foram alteradas** — o Rodolfo confere na entrada.');
  out.push('');

  out.push('## 7. `mecanicaConferida: false` (' + (total - conferidas) + ')');
  out.push('');
  md(['Magia', 'Página', 'Motivo'], aplicada.filter((m) => m.mecanicaConferida === false)
    .sort((a, b) => a.nome.localeCompare(b.nome, 'pt')).map((m) => [m.nome, m.pagina, m.mecanicaMotivo])).forEach((l) => out.push(l));
  return { texto: out.join('\n'), trocas, conferidas, porMotivo, contD, resD };
}

function main() {
  const calc = calcular();
  const aplicada = aplicar(calc);
  const rel = relatorio(calc, aplicada);
  console.log('Catálogo: ' + calc.catalogo.length + ' magias');
  console.log('  entrega mudou: ' + rel.trocas.length + ' | mecanicaConferida: ' + rel.conferidas + ' | não: ' + (calc.catalogo.length - rel.conferidas));
  console.log('  dano: ' + JSON.stringify(rel.contD) + ' | resistência diverge: ' + rel.resD.length);
  console.log('  motivos: ' + JSON.stringify(rel.porMotivo));
  if (process.argv.includes('--simular')) return;
  fs.writeFileSync(CATALOGO, textoDoCatalogo(aplicada));
  const relPath = argumento('--relatorio');
  if (relPath) fs.writeFileSync(relPath, rel.texto + '\n');
  console.log('Catálogo gravado.' + (relPath ? ' Relatório: ' + relPath : ''));
}

if (require.main === module) main();

module.exports = { calcular, aplicar, relatorio, decidirEntrega, atributoDaCondicao, textoDoCatalogo, catalogoDeAntes, TOQUE_PELO_TEXTO, TOQUE_OPCIONAL };
