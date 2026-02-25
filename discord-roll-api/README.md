# GURPS Discord Roll API

API para receber rolagens do app e publicar no Discord via bot.

## Endpoints

- `GET /health`
- `GET /api/channels` (lista canais de voz)
- `POST /api/rolls` (envia rolagem)

## Configuracao local

1. Instalar dependencias:

```bash
npm install
```

2. Copiar `.env.example` para `.env` e preencher:

- `PORT` (ex.: `8787`)
- `API_KEY` (chave usada pelo app no header `x-api-key`)
- `DISCORD_BOT_TOKEN`
- `DISCORD_CHANNEL_ID` (opcional, canal padrao)

3. Rodar:

```bash
npm start
```

4. Healthcheck:

```bash
http://localhost:8787/health
```

## Teste rapido de rolagem

```bash
curl -X POST http://localhost:8787/api/rolls \
  -H "Content-Type: application/json" \
  -H "x-api-key: troque_esta_chave" \
  -d '{
    "character": "Arthos",
    "testType": "Ataque",
    "context": "Espada Larga",
    "target": 14,
    "modifier": 2,
    "dice": [3, 4, 2],
    "total": 11,
    "outcome": "SUCESSO",
    "margin": 3,
    "channelId": "1412954766983565315"
  }'
```

## Deploy no Railway (resumo)

1. Crie um projeto no Railway e conecte este repositorio.
2. No servico, use a pasta raiz `discord-roll-api`.
3. Configure variaveis:
   - `PORT=8787`
   - `API_KEY=...`
   - `DISCORD_BOT_TOKEN=...`
   - `DISCORD_CHANNEL_ID=...` (opcional)
4. Deploy.
5. Copie a URL publica gerada (ex.: `https://seu-app.up.railway.app`).
6. No app Android, defina:
   - `DISCORD_ROLL_API_BASE_URL=<URL_PUBLICA>`
   - `DISCORD_ROLL_API_KEY=<API_KEY>`
