#!/usr/bin/env node
'use strict';
/**
 * Passo 6 da cadeia do catálogo de magias — o custo que DEPENDE DA CONDIÇÃO.
 *
 *   node scripts/magias/aplicar-custo-por-condicao.js --simular
 *   node scripts/magias/aplicar-custo-por-condicao.js
 *
 * Rode DEPOIS do passo 2 (`aplicar-numeros-conferidos.js`), que reescreve os campos de custo. Para cada magia de
 * `custo-por-condicao.json`:
 *
 *  - grava `custoPorCondicao` ({entradas, operar, manter}: o que o jogador informa e a conta do PF);
 *  - grava o `custoTipo` do spec; onde NÃO é `fixo`, apaga o número-base que o passo 2 leu da primeira frase
 *    ("1 para operar para cada 2 kg" virava custo 1) e guarda no `custoFormula` o texto de custo da entrada;
 *  - onde é `fixo` (Criar Montaria, Mão Mágica, Animar Planta, Despertar Computador/NT), o número-base fica.
 *
 * Nenhum número do livro é digitado: o texto de custo vem do `chunks.jsonl`, e a guarda (`custo-condicao.js`)
 * confere a conta contra ele. Idempotente.
 */
const fs = require('fs');
const path = require('path');
const L = require('./localizador');
const C = require('./custo-condicao');

const RAIZ = path.resolve(__dirname, '..', '..');
const CHUNKS = path.join(RAIZ, 'app', 'src', 'main', 'assets', 'chunks.jsonl');
const CATALOGO = path.join(RAIZ, 'app', 'src', 'main', 'assets', 'magias2versao.json');
const MANUAL = path.join(__dirname, 'magias-localizacao-manual.json');

/** O texto de custo da entrada de cada magia (por id), lido do livro. */
function textosDeCusto(catalogo) {
  const livro = L.carregarLivro(CHUNKS);
  const manual = L.lerLocalizacaoManual(MANUAL);
  const indice = L.construirIndice(livro, catalogo, { manual });
  const out = new Map();
  catalogo.forEach((m) => {
    const x = indice.magias[m.id];
    const e = x && x.entrada && x.entrada.formato === 'bloco' ? L.camposDaEntrada(livro, x.entrada) : {};
    if (e.custo) out.set(m.id, String(e.custo).replace(/\*\*/g, '').replace(/\s+/g, ' ').trim());
  });
  return out;
}

function aplicar(catalogo, specs, textos) {
  const problemas = [];
  const ids = new Set(catalogo.map((m) => m.id));
  Object.keys(specs).forEach((id) => {
    if (!ids.has(id)) problemas.push(id + ': não existe no catálogo');
    else C.problemasDoSpec(id, specs[id], textos.get(id)).forEach((p) => problemas.push(p));
  });
  if (problemas.length) return { problemas };
  const saida = catalogo.map((m) => {
    const spec = specs[m.id];
    if (!spec) {
      if (m.custoPorCondicao === undefined) return m;
      const c = JSON.parse(JSON.stringify(m)); delete c.custoPorCondicao; return c;
    }
    const c = JSON.parse(JSON.stringify(m));
    c.custoTipo = spec.tipo;
    if (spec.tipo !== 'fixo') {
      delete c.custoOperar; delete c.custoManter; delete c.custoManterTipo; delete c.custoMinimo; delete c.custoMaximo;
      c.custoFormula = textos.get(m.id);
    }
    const cond = { entradas: spec.entradas, operar: spec.operar };
    if (spec.manter !== undefined) cond.manter = spec.manter;
    c.custoPorCondicao = cond;
    return c;
  });
  return { saida, problemas: [] };
}

function main() {
  const catalogo = JSON.parse(fs.readFileSync(CATALOGO, 'utf8'));
  const specs = C.lerOsSpecs();
  const r = aplicar(catalogo, specs, textosDeCusto(catalogo));
  if (r.problemas.length) {
    console.error('Problemas (nada foi gravado):\n - ' + r.problemas.join('\n - '));
    process.exit(1);
  }
  const antes = catalogo.filter((m) => m.custoPorCondicao).length;
  console.log('Magias com custo por condição: ' + Object.keys(specs).length + ' (antes: ' + antes + ')');
  if (process.argv.includes('--simular')) return;
  fs.writeFileSync(CATALOGO, (JSON.stringify(r.saida, null, 2) + '\n').replace(/\n/g, '\r\n'));
  console.log('Catálogo gravado.');
}

if (require.main === module) main();

module.exports = { aplicar, textosDeCusto };
