# Relatorio de Paridade - Aba_VTT (Android) x VTT_Mestre
Data: 2026-03-11

## Escopo da verificacao
- Contrato de eventos da bridge App <-> WebView <-> VTT_Mestre.
- Contrato WS/REST do backend para join/roll/sync.
- Validacao tecnica minima dos dois projetos.

## Evidencias de validacao tecnica
- VTT_Mestre backend: `npm test` => **34/34 OK**
- VTT_Mestre frontend: `npm run build` => **OK**
- Android Aba_VTT: `:app:assembleVisualDebug -x lint` => **OK**

## Matriz de contrato (paridade)
1. `VTT_JOIN` (App -> Web)  
Status: **OK**  
Evidencia: Android envia `window.postMessage(JSON.stringify({type:'VTT_JOIN', payload}))`; App.tsx consome em `window.addEventListener('message', ...)` e chama `handleJoinSession(...)`.

2. `APP_ROLL` (App -> Web -> WS)  
Status: **OK**  
Evidencia: Android envia `APP_ROLL`; App.tsx converte para `socket.emit('acao_rolagem_dado', envelope v1)`.

3. `ROOM_STATE` (Web -> App)  
Status: **OK**  
Evidencia: App.tsx usa `dispatchToApp('ROOM_STATE', payload)` no `estado_inicial` e atualizacoes de cena; Android trata em `tratarMensagemBridge`.

4. `TOKEN_SELECTED` (Web -> App)  
Status: **OK**  
Evidencia: App.tsx em `handleTokenClick` faz `dispatchToApp('TOKEN_SELECTED', { tokenId, name, isOwn })`; Android abre dialogo contextual.

5. `ROLL_RESULT` (Web -> App)  
Status: **OK**  
Evidencia: App.tsx envia via `dispatchToApp('ROLL_RESULT', res)` ao receber `resultado_rolagem`; Android consome e exibe resumo.

6. `FICHA_SYNC` (Web -> App)  
Status: **OK (com filtro no app)**  
Evidencia: App.tsx repassa `atualizacao_ficha_sync` para bridge; Android aplica validacao por `playerId/tokenId` antes de importar.

7. Snapshot de ficha no join (`gurps-android-ficha`) (App -> Web)  
Status: **GAP**
Evidencia: Android dispara `window.dispatchEvent('gurps-android-ficha', detail=ficha)`; App.tsx atual **nao** possui listener para esse evento.
Impacto: ficha do app pode nao entrar automaticamente no VTT web embed quando nao existe ficha local no navegador.

8. `AUDIO_STATE` (Web -> App)  
Status: **GAP**
Evidencia: Android trata `AUDIO_STATE`, mas App.tsx atual nao faz `dispatchToApp('AUDIO_STATE', ...)`.
Impacto: painel de audio da Aba_VTT fica sem estado consolidado vindo do VTT.

## Diagnostico de runtime observado
- Erro de tela preta no Chrome identificado:
  - `Extension type renderer already has a handler`
- Causa raiz encontrada: mistura de versoes Pixi (7.3.x + 7.4.x) por dependencias nao usadas.
- Acao aplicada: remocao de `@pixi/math` e `@pixi/math-extras`; arvore Pixi unificada em 7.4.2.

## Conclusao de paridade
- Ponte principal de jogo (join, roll, room state, token selected, ficha sync) esta estruturada e funcional em contrato.
- Existem **2 gaps funcionais** antes do aceite completo:
1. Consumir `gurps-android-ficha` no App.tsx.
2. Emitir `AUDIO_STATE` do App.tsx para o Android.

## Proximo lote recomendado (antes do teste manual final)
Lote FPAR.6 - Fechamento de Paridade de Bridge
1. Passo 1: adicionar listener `gurps-android-ficha` no App.tsx e aplicar snapshot no estado local/token.
2. Passo 2: padronizar emissao de `AUDIO_STATE` via `dispatchToApp`.
3. Passo 3: smoke integrado Android+VTT (join auto, mapa, token selected, 1 roll, 1 sync ficha, 1 evento audio).
