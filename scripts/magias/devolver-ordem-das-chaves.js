'use strict';
// Rode DEPOIS da cadeia (e do passo 8), na raiz do app: `node scripts/magias/devolver-ordem-das-chaves.js`.
// Devolve a ORDEM DAS CHAVES do catálogo à do HEAD (só a ordem: se o conteúdo mudar, aborta), para o diff do git mostrar só o que mudou de verdade.
// (A cadeia apaga e regrava campos, e eles vão para o fim do objeto: sem isto o diff do catálogo vem com milhares de linhas só de ordem.)
const fs = require('fs');
const { execSync } = require('child_process');
const CAT = 'app/src/main/assets/magias2versao.json';
const ref = JSON.parse(execSync('git show HEAD:' + CAT, { maxBuffer: 1e9 }).toString('utf8'));
const cur = JSON.parse(fs.readFileSync(CAT, 'utf8'));
const objeto = (x) => x && typeof x === 'object' && !Array.isArray(x);
function como(r, c) {
  if (Array.isArray(c)) return c.map((v, i) => (Array.isArray(r) && r.length === c.length ? como(r[i], v) : como(undefined, v)));
  if (!objeto(c)) return c;
  const saida = {};
  if (objeto(r)) Object.keys(r).forEach((k) => { if (k in c) saida[k] = como(r[k], c[k]); });
  Object.keys(c).forEach((k) => { if (!(k in saida)) saida[k] = como(objeto(r) ? r[k] : undefined, c[k]); });
  return saida;
}
const porId = Object.fromEntries(ref.map((m) => [m.id, m]));
const ordenado = cur.map((m) => como(porId[m.id], m));
if (JSON.stringify(ordenado.map((m) => JSON.stringify(Object.entries(m).sort()))) !== JSON.stringify(cur.map((m) => JSON.stringify(Object.entries(m).sort())))) throw new Error('o conteúdo mudou ao reordenar: abortei');
fs.writeFileSync(CAT, (JSON.stringify(ordenado, null, 2) + '\n').replace(/\n/g, '\r\n'));
console.log('ordem das chaves devolvida ao HEAD:', ordenado.length, 'magias');
