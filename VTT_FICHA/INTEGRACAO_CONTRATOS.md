# Contratos de Integração (App <-> VTT)

## 1) Contrato de personagem (snapshot)
Envelope base:
1. `schema`: `gurps.personagem`
2. `schemaVersion`: inteiro
3. `appVersion`
4. `uiVariant`
5. `character`: objeto completo da ficha

Campos mínimos obrigatórios para sessão:
1. `character.nome`
2. `character.forca`, `destreza`, `inteligencia`, `vitalidade`
3. `character.pericias[]`
4. `character.tecnicas[]`
5. `character.magias[]`
6. `character.equipamentos[]`
7. `character.defesasAtivas`

## 2) Contrato de sessão VTT
Identificadores:
1. `sessionId`
2. `roomKey`
3. `playerId`
4. `tokenId`

Regras:
1. `tokenId` não pode mudar durante uma sessão ativa.
2. Reentrada com mesmo `playerId` tenta recuperar o mesmo `tokenId`.

## 3) Contrato de eventos WebSocket
Eventos mínimos:
1. `estado_inicial`
2. `cenario_atualizado`
3. `resultado_chat`
4. `audio_atualizado`
5. `audio_moderacao_atualizado`

Evento de ação (cliente -> servidor):
1. `acao_rolagem_dado`
2. payload mínimo:
   - `tokenId`
   - `tipoAcao` (teste, ataque, defesa, magia)
   - `alvoId` (quando aplicável)
   - `contexto` (arma/perícia/magia selecionada)

Evento de resultado (servidor -> clientes):
1. `resultado_chat`
2. payload mínimo:
   - `tokenId`
   - `tipoAcao`
   - `resultadoRolagem`
   - `sucessoOuFalha`
   - `textoResumo`
   - `timestampUtc`

## 4) Resolução de conflito de dados
Prioridade:
1. Estado da sessão (posição, voz, visibilidade) -> VTT.
2. Estado do personagem (atributos, perícias, equipamentos) -> Ficha.

Sincronização:
1. Snapshot da ficha no join.
2. Atualização incremental sob demanda (ex.: HP/PF/estado de combate).
3. Eventos com `source` e `version` para evitar sobrescrita incorreta.

## 5) Segurança mínima
1. Não aceitar ação de token sem vínculo válido `playerId -> tokenId`.
2. Validar payloads com schema.
3. Logar ações críticas (join, move, roll, voice moderation).
