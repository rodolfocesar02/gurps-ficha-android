const express = require('express');
const dotenv = require('dotenv');

dotenv.config();

const app = express();
app.use(express.json({ limit: '1mb' }));

const port = Number(process.env.PORT || 8787);
const apiKey = process.env.API_KEY || '';
const botToken = process.env.DISCORD_BOT_TOKEN || '';
const defaultChannelId = process.env.DISCORD_CHANNEL_ID || '';
const DISCORD_TYPE_GUILD_VOICE = 2;

function requireConfigured() {
  return Boolean(apiKey && botToken);
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

function sanitizeChannelId(value) {
  return String(value || '').trim();
}

function discordAuthHeaders() {
  return {
    'Content-Type': 'application/json',
    Authorization: `Bot ${botToken}`
  };
}

async function sendToDiscord(content, channelId) {
  const response = await fetch(`https://discord.com/api/v10/channels/${channelId}/messages`, {
    method: 'POST',
    headers: discordAuthHeaders(),
    body: JSON.stringify({ content })
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(`discord_error_${response.status}: ${text}`);
  }

  return response.json();
}

async function listVoiceChannels() {
  const guildsResponse = await fetch('https://discord.com/api/v10/users/@me/guilds', {
    method: 'GET',
    headers: discordAuthHeaders()
  });
  if (!guildsResponse.ok) {
    const text = await guildsResponse.text();
    throw new Error(`discord_guilds_error_${guildsResponse.status}: ${text}`);
  }

  const guilds = await guildsResponse.json();
  const channels = [];

  for (const guild of guilds) {
    const channelsResponse = await fetch(`https://discord.com/api/v10/guilds/${guild.id}/channels`, {
      method: 'GET',
      headers: discordAuthHeaders()
    });

    if (!channelsResponse.ok) {
      continue;
    }

    const guildChannels = await channelsResponse.json();
    guildChannels
      .filter((channel) => channel && channel.type === DISCORD_TYPE_GUILD_VOICE)
      .forEach((channel) => {
        channels.push({
          id: channel.id,
          name: channel.name,
          guildId: guild.id,
          guildName: guild.name
        });
      });
  }

  channels.sort((a, b) => {
    const guildCmp = a.guildName.localeCompare(b.guildName, 'pt-BR', { sensitivity: 'base' });
    if (guildCmp !== 0) return guildCmp;
    return a.name.localeCompare(b.name, 'pt-BR', { sensitivity: 'base' });
  });

  return channels;
}

app.get('/health', (_req, res) => {
  res.json({
    ok: true,
    service: 'gurps-discord-roll-api',
    configured: requireConfigured(),
    defaultChannelConfigured: Boolean(defaultChannelId)
  });
});

app.get('/api/channels', async (req, res) => {
  if (!requireConfigured()) {
    return res.status(500).json({ ok: false, error: 'service_not_configured' });
  }

  const incomingKey = req.header('x-api-key') || '';
  if (!incomingKey || incomingKey !== apiKey) {
    return unauthorized(res);
  }

  try {
    const channels = await listVoiceChannels();
    return res.json({ ok: true, channels });
  } catch (error) {
    return res.status(502).json({
      ok: false,
      error: 'discord_channels_failed',
      detail: error.message
    });
  }
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
  const targetChannelId = sanitizeChannelId(payload.channelId) || sanitizeChannelId(defaultChannelId);
  if (!targetChannelId) {
    return res.status(400).json({ ok: false, error: 'channel_id_missing' });
  }

  const message = formatRollMessage(payload);

  try {
    const discordMessage = await sendToDiscord(message, targetChannelId);
    return res.json({
      ok: true,
      discordMessageId: discordMessage.id,
      channelId: targetChannelId
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
