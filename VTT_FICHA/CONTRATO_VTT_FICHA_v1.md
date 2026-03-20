# CONTRATO VTT <-> FICHA (v1)

Data: 2026-03-09  
Status: Oficial para integracao incremental

## 1. Objetivo
Definir um contrato unico para App Ficha (Android) e VTT (Web/Backend) trabalharem juntos sem quebrar as abas atuais da ficha.

## 2. Principios
1. A ficha continua canonica para dados de personagem (atributos, pericias, tecnicas, magias, equipamentos, defesas).
2. O VTT continua canonico para estado de sessao (sala, posicao de token, audio/voz, presenca).
3. Tudo deve ser versionado por `contractVersion`.
4. Toda mensagem critica precisa de `requestId` e `timestampUtc`.

## 3. Envelope padrao
Todas as mensagens de integracao (REST/WS) devem aceitar:

```json
{
  "contractVersion": "v1",
  "requestId": "uuid-ou-id-curto",
  "timestampUtc": "2026-03-09T12:00:00Z",
  "source": "ficha-app|vtt-web|vtt-backend",
  "payload": {}
}
```

## 4. Contrato de personagem
Formato aceito (ja existente no ecossistema):

```json
{
  "schema": "gurps.personagem",
  "schemaVersion": 1,
  "appVersion": "string",
  "uiVariant": "visual|pracego|vtt",
  "character": { }
}
```

Campos minimos para integracao:
1. `character.nome`
2. `character.forca`, `character.destreza`, `character.inteligencia`, `character.vitalidade`
3. `character.pericias[]`
4. `character.tecnicas[]`
5. `character.magias[]`
6. `character.equipamentos[]`
7. `character.defesasAtivas`

## 5. Sessao e identidade
Identificadores obrigatorios:
1. `roomKey`
2. `playerId`
3. `tokenId` (após vinculo)
4. `sessionId`

Regras:
1. `playerId` + `roomKey` devem recuperar o mesmo `tokenId` em reconexao, quando possivel.
2. Acao de token sem vinculo valido `playerId -> tokenId` deve ser rejeitada.
3. `tokenId` nao pode ser trocado sem evento explicito de reassociacao.

## 6. REST (v1)

### 6.1 Ja implementado no backend VTT
1. `GET /api/v1/health`
2. `POST /api/v1/importar-ficha`

Resposta padrao de erro:

```json
{
  "errorCode": "STRING",
  "message": "Descricao curta",
  "details": {}
}
```

### 6.2 Para implementar na integracao (v1.x)
1. `POST /api/v1/session/join`
2. `POST /api/v1/session/leave`
3. `POST /api/v1/token/bind`
4. `POST /api/v1/roll/request`
5. `GET /api/v1/session/state?roomKey=...&playerId=...`

## 7. WebSocket (v1)

### 7.1 Ja implementado no backend VTT
Servidor -> cliente:
1. `estado_inicial`
2. `cenario_atualizado`
3. `audio_atualizado`
4. `audio_moderacao_atualizado`
5. `signal`
6. `ice-candidate`
7. `user-joined-voice`

Cliente -> servidor:
1. `solicitar_movimento`
2. `solicitar_audio`
3. `join-voice`
4. `signal`
5. `ice-candidate`
6. `moderacao_voz_mute`
7. `moderacao_voz_speaker_only`

### 7.2 Eventos de integracao ficha-vtt (a implementar v1.x)
1. `join_room`
2. `ficha_snapshot_sync`
3. `acao_rolagem_dado`
4. `resultado_rolagem`
5. `erro_acao`

Payload minimo de `acao_rolagem_dado`:

```json
{
  "roomKey": "mesa-01",
  "playerId": "p1",
  "tokenId": "t1",
  "tipoAcao": "teste|ataque|defesa|magia",
  "contexto": {
    "itemId": "id-pericia-ou-arma-ou-magia",
    "alvoId": "opcional",
    "modificador": 0
  }
}
```

Payload minimo de `resultado_rolagem`:

```json
{
  "roomKey": "mesa-01",
  "tokenId": "t1",
  "tipoAcao": "teste|ataque|defesa|magia",
  "dado": "3d6",
  "total": 11,
  "alvo": 13,
  "margem": 2,
  "resultado": "sucesso|falha|sucesso_critico|falha_critica",
  "textoResumo": "Ataque com Espada Curta: sucesso por 2"
}
```

## 8. Regras de sincronizacao
1. Snapshot completo da ficha no `join_room`.
2. Durante sessao, enviar apenas delta para estado rapido (HP/PF/status).
3. Conflito:
   - se campo for de personagem: vence ficha;
   - se campo for de sessao: vence VTT.
4. Toda atualizacao deve incluir `version` incremental por entidade.

## 9. Timeouts e retentativa
1. Timeout de request WS: 10s.
2. Retentativa automatica: 2 vezes (backoff exponencial).
3. Se falhar: exibir erro amigavel e botao "Tentar novamente".

## 10. Seguranca minima
1. Validar schema dos payloads no backend.
2. Limitar tamanho de payload (ex.: 256KB por mensagem WS).
3. Sanitizar texto livre antes de log/chat.
4. Logar: join, bind de token, rolagem, moderacao de audio, erros.

## 11. Compatibilidade e migracao
1. Mudancas breaking exigem `contractVersion` novo (`v2`).
2. Backend deve manter `v1` por pelo menos 1 ciclo de release apos entrar `v2`.

## 12. Ordem recomendada de implementacao
1. Fechar `join_room` + `token/bind`.
2. Fechar `acao_rolagem_dado` + `resultado_rolagem`.
3. Fechar sync delta de HP/PF.
4. Fechar voz e reconexao.

