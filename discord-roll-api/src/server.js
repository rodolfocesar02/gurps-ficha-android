const express = require('express');
const dotenv = require('dotenv');

dotenv.config();

const app = express();
app.use(express.json({ limit: '1mb' }));

const port = Number(process.env.PORT || 8787);
const apiKey = process.env.API_KEY || '';
const botToken = process.env.DISCORD_BOT_TOKEN || '';
const channelId = process.env.DISCORD_CHANNEL_ID || '';

function requireConfigured() {
  return Boolean(apiKey && botToken && channelId);
}

function unauthorized(res) {
  return res.status(401).json({ ok: false, error: 'unauthorized' });
}

function formatRollMessage(payload) {
  const character = payload.character || 'Personagem';
  const testType = payload.testType || 'Rolagem';
  const context = payload.context ? ` (${payload.context})` : '';

  const target = payload.target != null ? String(payload.target) : '-';
  const modifier = Number(payload.modifier || 0);
  const modifierLabel = modifier >= 0 ? `+${modifier}` : String(modifier);

  const dice = Array.isArray(payload.dice) && payload.dice.length > 0
    ? payload.dice.join(', ')
    : '-';

  const total = payload.total != null ? String(payload.total) : '-';
  const outcome = payload.outcome || '-';
  const margin = payload.margin != null ? String(payload.margin) : '-';

  return [
    `🎲 **${character}**`,
    `Teste: **${testType}**${context}`,
    `Alvo: **${target}** | Mod: **${modifierLabel}**`,
    `Dados: [${dice}] | Total: **${total}**`,
    `Resultado: **${outcome}** | Margem: **${margin}**`
  ].join('\n');
}

async function sendToDiscord(content) {
  const response = await fetch(`https://discord.com/api/v10/channels/${channelId}/messages`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bot ${botToken}`
    },
    body: JSON.stringify({ content })
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(`discord_error_${response.status}: ${text}`);
  }

  return response.json();
}

app.get('/health', (_req, res) => {
  res.json({
    ok: true,
    service: 'gurps-discord-roll-api',
    configured: requireConfigured()
  });
});

app.post('/api/rolls', async (req, res) => {
  if (!requireConfigured()) {
    return res.status(500).json({ ok: false, error: 'service_not_configured' });
  }

  const incomingKey = req.header('x-api-key') || '';
  if (!incomingKey || incomingKey !== apiKey) {
    return unauthorized(res);
  }

  const payload = req.body || {};
  const message = formatRollMessage(payload);

  try {
    const discordMessage = await sendToDiscord(message);
    return res.json({
      ok: true,
      discordMessageId: discordMessage.id
    });
  } catch (error) {
    return res.status(502).json({
      ok: false,
      error: 'discord_send_failed',
      detail: error.message
    });
  }
});

app.listen(port, () => {
  console.log(`[gurps-discord-roll-api] running on port ${port}`);
});
