# Guia Railway (Passo a Passo)

Objetivo: deixar sua API online para funcionar fora da sua rede Wi-Fi.

## 1) Criar o projeto

1. Entre no Railway e faca login.
2. Clique em `New Project`.
3. Escolha `Deploy from GitHub repo`.
4. Selecione este repositorio.

## 2) Configurar a pasta certa

1. Abra o servico criado no Railway.
2. Em `Settings`, ajuste o `Root Directory` para:

```txt
discord-roll-api
```

3. Salve.

## 3) Variaveis de ambiente

No Railway, em `Variables`, adicione:

- `PORT` = `8787`
- `API_KEY` = `gurps_ficha_teste` (ou outra chave sua)
- `DISCORD_BOT_TOKEN` = seu token do bot
- `DISCORD_CHANNEL_ID` = id de canal padrao (opcional)

## 4) Fazer deploy

1. Clique em `Deploy` (ou aguarde deploy automatico).
2. Quando terminar, copie a URL publica do servico.

Exemplo:

```txt
https://seu-servico.up.railway.app
```

## 5) Testar a API online

No navegador, abra:

```txt
https://seu-servico.up.railway.app/health
```

Se estiver certo, deve retornar `ok: true`.

## 6) Apontar o app Android para a URL publica

No arquivo `local.properties` do projeto Android:

```properties
DISCORD_ROLL_API_BASE_URL=https://seu-servico.up.railway.app
DISCORD_ROLL_API_KEY=gurps_ficha_teste
```

Depois:

1. gere APK novo;
2. instale no celular;
3. teste uma rolagem.

## 7) Fluxo no app

1. Aba `Rolagem` -> `Atualizar canais de voz`.
2. Escolha o canal.
3. As rolagens continuam indo para esse canal ate voce trocar.
