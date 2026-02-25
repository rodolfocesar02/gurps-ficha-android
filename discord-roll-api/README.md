# GURPS Discord Roll API

API simples para receber rolagens da ficha e publicar no Discord usando bot.

## 1) Instalar dependencias

```bash
npm install
```

## 2) Configurar ambiente

Copie `.env.example` para `.env` e preencha:

- `PORT`
- `API_KEY` (chave usada pelo app Android no header `x-api-key`)
- `DISCORD_BOT_TOKEN`
- `DISCORD_CHANNEL_ID`

## 3) Executar

```bash
npm start
```

Healthcheck:

```bash
GET http://localhost:8787/health
```

## 4) Enviar rolagem (teste)

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
    "margin": 3
  }'
```

## Payload esperado

Campos recomendados para `POST /api/rolls`:

- `character` (string)
- `testType` (string)
- `context` (string opcional)
- `target` (number)
- `modifier` (number)
- `dice` (array de number)
- `total` (number)
- `outcome` (string)
- `margin` (number)
