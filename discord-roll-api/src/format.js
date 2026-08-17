/**
 * **Como a mensagem chega no Discord.**
 *
 * Ficava dentro do `server.js`, que sobe o servidor ao ser importado — dava
 * para ler o código, mas não dava para **testar** sem abrir uma porta. Saiu
 * para cá no lote NOTA-2, junto com a correção do formato da anotação.
 *
 * ⚠️ Aqui só entra função **pura**: recebe o pacote, devolve texto. Nada de
 * rede, nada de Discord. É o que torna o `npm test` possível.
 */

/**
 * Classifica crítico pela regra COMPLETA do GURPS (considera o NH efetivo).
 *  - Decisivo: 3-4 sempre; 5 se NH>=15; 6 se NH>=16.
 *  - Falha crítica: 18 sempre; 17 se NH<=15; soma >= NH+10 sempre.
 * nh pode ser null (sem alvo) -> usa só a regra simples.
 */
function classificarCritico(soma, nh) {
  if (soma === 3 || soma === 4) return 'DECISIVO';
  if (nh != null) {
    if (soma === 5 && nh >= 15) return 'DECISIVO';
    if (soma === 6 && nh >= 16) return 'DECISIVO';
  }
  if (soma === 18) return 'FALHA';
  if (nh != null) {
    if (soma === 17 && nh <= 15) return 'FALHA';
    if (soma >= nh + 10) return 'FALHA';
  } else if (soma === 17) {
    return 'FALHA';
  }
  return 'NORMAL';
}

function formatRollMessage(payload) {
  const character = payload.character || 'Personagem';
  const testType = payload.testType || 'Rolagem';
  const context = payload.context ? ` (${payload.context})` : '';
  const diceValues = Array.isArray(payload.dice) ? payload.dice : [];
  const dice = diceValues.length > 0
    ? diceValues.map((value) => `🎲${value}`).join(' ')
    : '-';
  const total = payload.total != null ? String(payload.total) : '-';

  // Anotação do Bloco de Notas: NÃO é rolagem, e não pode parecer uma.
  //
  // 🔴 O app manda `dice` vazio, `total` 0 e `target` nulo de propósito, para a
  // nota não fingir um resultado que ninguém rolou. Só que o molde de baixo
  // imprime esses campos de qualquer jeito, e o Discord mostrava
  // "Dados: - = 0" e "Resultado: **...**" numa mensagem que é só texto.
  //
  // ⚠️ O `testType: "Nota"` já vinha no pacote desde o lote NOTA-1 — faltava o
  // bot olhar para ele. Aqui ele para de aplicar o molde de rolagem.
  if (testType === 'Nota') {
    return [
      `**${character}**`,
      `**${testType}**${context}`,
      String(payload.outcome || '')
    ].join('\n');
  }

  // 2ª mensagem: tabela crítica automática (Golpe Fulminante / Erro Crítico).
  // O app marca o testType com 💥 ou 💀 e manda o texto pronto no outcome.
  if (testType.startsWith('💥') || testType.startsWith('💀')) {
    return [
      `**${character}**`,
      `**${testType}**`,
      `Dados: ${dice} = **${total}**`,
      '',
      String(payload.outcome || '')
    ].join('\n');
  }

  const margin = Number(payload.margin);
  const hasMargin = Number.isFinite(margin);
  const isSuccess = String(payload.outcome || '').startsWith('sucesso') ||
    String(payload.outcome || '').startsWith('SUCESSO');
  const isFailure = String(payload.outcome || '').startsWith('falha') ||
    String(payload.outcome || '').startsWith('FALHA');
  const isThreeD6 =
    payload.testType !== 'Dano' &&
    diceValues.length === 3 &&
    diceValues.every((value) => Number.isInteger(value) && value >= 1 && value <= 6);
  const rawDiceTotal = isThreeD6
    ? diceValues.reduce((acc, value) => acc + value, 0)
    : null;
  // NH efetivo = payload.target (já com modificadores), quando houver.
  const nh = Number.isFinite(Number(payload.target)) ? Number(payload.target) : null;
  const critico = isThreeD6 ? classificarCritico(rawDiceTotal, nh) : 'NORMAL';

  let outcomeLabel = payload.outcome || '-';
  if (critico === 'DECISIVO') {
    outcomeLabel = 'SUCESSO DECISIVO 🍀';
  } else if (critico === 'FALHA') {
    outcomeLabel = 'FALHA CRÍTICA! 😈';
  } else if (hasMargin && isSuccess) {
    outcomeLabel = `SUCESSO +${Math.abs(margin)}`;
  } else if (hasMargin && isFailure) {
    outcomeLabel = `FALHA -${Math.abs(margin)}`;
  }

  return [
    `**${character}**`,
    `**${testType}**${context}`,
    `Dados: ${dice} = **${total}**`,
    `Resultado: **${outcomeLabel}**`
  ].join('\n');
}

module.exports = { classificarCritico, formatRollMessage };
