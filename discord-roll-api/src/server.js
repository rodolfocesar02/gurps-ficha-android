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
const CHANNEL_CACHE_TTL_MS = 30 * 60 * 1000;
const CHANNEL_CACHE_TTL_SECONDS = Math.floor(CHANNEL_CACHE_TTL_MS / 1000);
const channelsCache = {
  items: [],
  fetchedAt: 0
};

function requireConfigured() {
  return Boolean(apiKey && botToken);
}

function jsonError(res, statusCode, error, detail) {
  const payload = { ok: false, error };
  if (detail) payload.detail = detail;
  return res.status(statusCode).json(payload);
}

function unauthorized(res) {
  return jsonError(res, 401, 'unauthorized');
}

function hasValidApiKey(req) {
  const incomingKey = req.header('x-api-key') || '';
  return Boolean(incomingKey && incomingKey === apiKey);
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
  const margin = Number(payload.margin);
  const hasMargin = Number.isFinite(margin);
  const isSuccess = String(payload.outcome || '').startsWith('SUCESSO');
  const isFailure = String(payload.outcome || '').startsWith('FALHA');
  const isThreeD6 =
    payload.testType !== 'Dano' &&
    diceValues.length === 3 &&
    diceValues.every((value) => Number.isInteger(value) && value >= 1 && value <= 6);
  const rawDiceTotal = isThreeD6
    ? diceValues.reduce((acc, value) => acc + value, 0)
    : null;

  let outcomeLabel = payload.outcome || '-';
  if (isThreeD6 && (rawDiceTotal === 3 || rawDiceTotal === 4)) {
    outcomeLabel = 'SUCESSO DECISIVO 🍀';
  } else if (isThreeD6 && (rawDiceTotal === 17 || rawDiceTotal === 18)) {
    outcomeLabel = 'FALHA CRÍTICA! 😈';
  } else if (hasMargin && isSuccess) {
    const plusMargin = Math.abs(margin);
    outcomeLabel = `SUCESSO +${plusMargin}`;
  } else if (hasMargin && isFailure) {
    const minusMargin = Math.abs(margin);
    outcomeLabel = `FALHA -${minusMargin}`;
  }

  return [
    `**${character}**`,
    `**${testType}**${context}`,
    `Dados: ${dice} = **${total}**`,
    `Resultado: **${outcomeLabel}**`
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

function cacheAgeMs() {
  if (!channelsCache.fetchedAt) return Number.MAX_SAFE_INTEGER;
  return Date.now() - channelsCache.fetchedAt;
}

function cacheIsFresh() {
  return channelsCache.items.length > 0 && cacheAgeMs() < CHANNEL_CACHE_TTL_MS;
}

function cacheAgeSeconds() {
  return Math.max(0, Math.floor(cacheAgeMs() / 1000));
}

async function getVoiceChannelsCached() {
  if (cacheIsFresh()) {
    return { channels: channelsCache.items, fromCache: true };
  }

  const channels = await listVoiceChannels();
  channelsCache.items = channels;
  channelsCache.fetchedAt = Date.now();
  return { channels, fromCache: false };
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
    return jsonError(res, 500, 'service_not_configured');
  }

  if (!hasValidApiKey(req)) {
    return unauthorized(res);
  }

  try {
    const { channels, fromCache } = await getVoiceChannelsCached();
    return res.json({
      ok: true,
      channels,
      fromCache,
      cacheAgeSeconds: cacheAgeSeconds(),
      cacheTtlSeconds: CHANNEL_CACHE_TTL_SECONDS
    });
  } catch (error) {
    return jsonError(res, 502, 'discord_channels_failed', error.message);
  }
});

app.post('/api/rolls', async (req, res) => {
  if (!requireConfigured()) {
    return jsonError(res, 500, 'service_not_configured');
  }

  if (!hasValidApiKey(req)) {
    return unauthorized(res);
  }

  const payload = req.body || {};
  const targetChannelId = sanitizeChannelId(payload.channelId) || sanitizeChannelId(defaultChannelId);
  if (!targetChannelId) {
    return jsonError(res, 400, 'channel_id_missing');
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
    return jsonError(res, 502, 'discord_send_failed', error.message);
  }
});

app.listen(port, () => {
  console.log(`[gurps-discord-roll-api] running on port ${port}`);
});
