const express = require('express');
const dotenv = require('dotenv');

dotenv.config();

const app = express();
// Limite maior: o retrato chega como data:base64 (algumas centenas de KB).
app.use(express.json({ limit: '8mb' }));

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

// Armazenamento em nuvem para fichas (In-memory para persistência rápida nesta sessão)
// Estrutura: deviceId -> { characterName -> fichaJson }
const cloudFichas = new Map();

// Retratos dos personagens (in-memory). Estrutura:
//   sanitizedCharacterName -> { mime, buffer, ext }
// Subidos UMA vez (ao salvar a ficha) e reanexados em cada embed de rolagem.
const portraits = new Map();

function sanitizeName(value) {
  return String(value || '').trim().replace(/[^a-zA-Z0-9_-]/g, '_');
}

/** Converte um data:URI (data:image/png;base64,XXXX) em { mime, buffer, ext }. */
function parseDataUri(dataUri) {
  const match = /^data:([^;]+);base64,(.+)$/i.exec(String(dataUri || ''));
  if (!match) return null;
  const mime = match[1];
  const buffer = Buffer.from(match[2], 'base64');
  const ext = mime.split('/')[1] || 'png';
  return { mime, buffer, ext };
}

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

function sanitizeChannelId(value) {
  return String(value || '').trim();
}

function discordAuthHeaders() {
  return {
    'Content-Type': 'application/json',
    Authorization: `Bot ${botToken}`
  };
}

/**
 * Envia mensagem ao Discord.
 * - Sem retrato: manda { content } como antes (texto simples).
 * - Com retrato: manda um embed com a descrição e o retrato anexado como
 *   arquivo, referenciado por embed.thumbnail.url = attachment://<file>.
 *   Exige multipart/form-data (payload_json + files[0]).
 */
async function sendToDiscord(content, channelId, portrait) {
  const url = `https://discord.com/api/v10/channels/${channelId}/messages`;

  if (!portrait) {
    const response = await fetch(url, {
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

  const fileName = `portrait.${portrait.ext}`;
  const payloadJson = {
    embeds: [
      {
        description: content,
        color: 0x5865f2,
        thumbnail: { url: `attachment://${fileName}` }
      }
    ],
    attachments: [{ id: 0, filename: fileName }]
  };

  const form = new FormData();
  form.append('payload_json', JSON.stringify(payloadJson));
  form.append(
    'files[0]',
    new Blob([portrait.buffer], { type: portrait.mime }),
    fileName
  );

  // NÃO definir Content-Type manualmente: o fetch/FormData define o boundary.
  const response = await fetch(url, {
    method: 'POST',
    headers: { Authorization: `Bot ${botToken}` },
    body: form
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
  const portrait = portraits.get(sanitizeName(payload.character)) || null;

  try {
    const discordMessage = await sendToDiscord(message, targetChannelId, portrait);
    return res.json({
      ok: true,
      discordMessageId: discordMessage.id,
      channelId: targetChannelId
    });
  } catch (error) {
    return jsonError(res, 502, 'discord_send_failed', error.message);
  }
});

// Recebe e guarda o retrato do personagem (data:base64). Reanexado nos embeds.
app.post('/api/portrait', (req, res) => {
  if (!hasValidApiKey(req)) return unauthorized(res);

  const { character, image } = req.body || {};
  if (!character || !image) {
    return jsonError(res, 400, 'dados_insuficientes');
  }

  const parsed = parseDataUri(image);
  if (!parsed) {
    return jsonError(res, 400, 'imagem_invalida');
  }
  // Limite de segurança (~4MB já decodificado).
  if (parsed.buffer.length > 4 * 1024 * 1024) {
    return jsonError(res, 413, 'imagem_muito_grande');
  }

  const key = sanitizeName(character);
  portraits.set(key, parsed);
  console.log(`[portrait] retrato salvo para ${key} (${parsed.buffer.length} bytes, ${parsed.mime})`);
  res.json({ ok: true });
});

// --- NOVAS ROTAS PARA PERSISTÊNCIA DE FICHAS EM NUVEM ---

// Listar fichas de um dispositivo
app.get('/api/fichas/:deviceId', (req, res) => {
  if (!hasValidApiKey(req)) return unauthorized(res);
  
  const deviceId = req.params.deviceId;
  const userFichas = cloudFichas.get(deviceId) || new Map();
  const names = Array.from(userFichas.keys());
  
  res.json({ ok: true, fichas: names });
});

// Baixar uma ficha específica
app.get('/api/fichas/:deviceId/:characterName', (req, res) => {
  if (!hasValidApiKey(req)) return unauthorized(res);
  
  const { deviceId, characterName } = req.params;
  const userFichas = cloudFichas.get(deviceId);
  
  if (!userFichas || !userFichas.has(characterName)) {
    return jsonError(res, 404, 'ficha_nao_encontrada');
  }
  
  res.json({ ok: true, ficha: userFichas.get(characterName) });
});

// Salvar/Atualizar uma ficha
app.post('/api/fichas', (req, res) => {
  if (!hasValidApiKey(req)) return unauthorized(res);
  
  const { deviceId, characterName, fichaJson } = req.body;
  
  if (!deviceId || !characterName || !fichaJson) {
    return jsonError(res, 400, 'dados_insuficientes');
  }

  // Sanitização simples do nome para evitar problemas de URL (mesma lógica do App)
  const safeName = characterName.replace(/[^a-zA-Z0-9_-]/g, '_');
  
  if (!cloudFichas.has(deviceId)) {
    cloudFichas.set(deviceId, new Map());
  }
  
  // Tenta converter a string JSON em objeto para evitar double-encoding no retorno
  let dataToSave = fichaJson;
  if (typeof fichaJson === 'string') {
    try {
      dataToSave = JSON.parse(fichaJson);
    } catch (e) {
      console.error(`[cloud] Erro ao parsear fichaJson para ${safeName}:`, e.message);
    }
  }
  
  cloudFichas.get(deviceId).set(safeName, dataToSave);
  
  console.log(`[cloud] Ficha salva e processada: ${safeName} para o dispositivo ${deviceId}`);
  res.json({ ok: true });
});

app.listen(port, () => {
  console.log(`[gurps-discord-roll-api] running on port ${port}`);
});
