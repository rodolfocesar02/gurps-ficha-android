# PROGRESS VTT+FICHA

Atualizado em: 2026-03-09
Status geral: `PLANEJADO - EXECUCAO GUIADA`

## Objetivo
Integrar o projeto VTT (`C:\Users\Rodolfo\Desktop\VTT_audio_video_ e ficha de gurps!`) com a ficha Android atual, sem alterar as abas ja existentes de edicao:

1. Geral
2. Tracos
3. Pericias
4. Tecnicas
5. Magias
6. Equipamentos
7. Defesas

A unica mudanca funcional de UX no app sera a criacao de uma nova aba `VTT` para substituir o papel atual da aba de uso em mesa (rolagens), preservando as mecanicas ja prontas.

## Escopo desta fase
1. Nenhuma mudanca em regras/motor/calculos das abas atuais.
2. Nenhuma alteracao estrutural fora da trilha de integracao.
3. Implementacao orientada por contrato versionado (`v1`).

## Diagnostico rapido de compatibilidade
1. Ficha Android ja exporta/importa JSON interoperavel.
2. VTT backend ja possui endpoint de importacao de ficha e websocket para eventos base de cena/voz.
3. VTT frontend ja contem engine GURPS em TS e componentes de token/chat/voz.
4. Gap principal: contrato de sessao/token/rolagem e aba VTT cliente no app.

## Lotes e Passos (com dono por etapa)

### Lote VTT.1 - Contrato e Base de Integracao
Status: `PENDENTE`
Objetivo: fechar contratos tecnicos sem tocar UI.
Passos:
1. Consolidar contrato de personagem (`schema`, campos obrigatorios, versionamento). Dono: `VTT + Ficha`.
2. Consolidar contrato de sessao/token (`sessionId`, `playerId`, `tokenId`). Dono: `VTT`.
3. Consolidar contrato websocket (`join_room`, `acao_rolagem_dado`, `resultado_rolagem`, `erro_acao`). Dono: `VTT + Ficha`.
4. Definir regra de conflito (ficha canonica vs sessao canonica). Dono: `VTT + Ficha`.
5. Fechar checklist de seguranca minima (schema, vinculo player-token, limites). Dono: `VTT`.
Criterio de aceite:
1. Contrato `v1` publicado e aprovado.

### Lote VTT.2 - Nova Aba `VTT` (shell)
Status: `EM ANDAMENTO`
Objetivo: criar aba VTT sem alterar abas antigas.
Passos:
1. Adicionar aba `VTT` na navegacao. Dono: `Ficha`.
2. Criar estados de tela (`desconectado`, `conectando`, `conectado`). Dono: `Ficha`.
3. Incluir configuracao de endpoint VTT (dev/homolog/prod). Dono: `Ficha`.
4. Definir fallback de abertura externa quando necessario. Dono: `Ficha`.
5. Validar que abas antigas continuam identicas. Dono: `Ficha`.
Criterio de aceite:
1. Aba `VTT` abre sem regressao nas abas atuais.

Execucao:
1. [x] 2026-03-09 - Passo VTT.2.1 concluido: aba `VTT` adicionada na navegacao sem remover abas existentes.
2. [x] 2026-03-09 - Passo VTT.2.2 concluido: estados de tela da aba `VTT` (desconectado, conectando, conectado e erro).
3. [x] 2026-03-09 - Passo VTT.2.3 concluido: configuracao de endpoint com ambiente (dev/homolog/prod/custom).
4. [x] 2026-03-09 - Passo VTT.2.4 concluido: fallback para abertura externa do VTT via navegador.

### Lote VTT.3 - Sessao e Presenca
Status: `EM ANDAMENTO`
Objetivo: entrar na sala e vincular token.
Passos:
1. Enviar `playerId + roomKey + snapshot da ficha`. Dono: `Ficha`.
2. Receber `tokenId` e persistir estado local de sessao. Dono: `Ficha`.
3. Implementar reconexao com recuperacao de token. Dono: `VTT + Ficha`.
4. Tratar sessao invalida/expirada com mensagem guiada. Dono: `Ficha`.
5. Registrar eventos criticos de entrada/saida. Dono: `VTT`.
Criterio de aceite:
1. Jogador entra e retorna com o mesmo token quando possivel.

Execucao:
1. [x] 2026-03-09 - Passo VTT.3.1 iniciado/concluido no app: envio de `roomKey + playerId + snapshot da ficha` para `/api/v1/session/join`.
2. [x] 2026-03-09 - Passo VTT.3.2 concluido no app: persistencia local de `serverUrl`, `roomKey`, `playerId`, `sessionId` e `tokenId`.
3. [x] 2026-03-09 - Passo VTT.3.3 concluido no app: reconexao envia `sessionId/tokenId` salvos para tentativa de recuperação de sessão.

### Lote VTT.4 - Fluxo de Rolagem por Token
Status: `PENDENTE`
Objetivo: mover uso da aba de rolagem para acao contextual no VTT.
Passos:
1. Clique no proprio token abre painel de acoes da ficha. Dono: `Ficha`.
2. Clique em inimigo abre painel de ataque/defesa contextual. Dono: `Ficha`.
3. Enviar acao para VTT (`acao_rolagem_dado`). Dono: `Ficha`.
4. Exibir retorno no chat e balao no token. Dono: `VTT + Ficha`.
5. Garantir que fluxo nao altera dados canonicos sem confirmacao. Dono: `Ficha`.
Criterio de aceite:
1. Fluxo de teste e ataque ocorre na aba `VTT` com resultado visivel.

### Lote VTT.5 - Voz e Acessibilidade
Status: `PENDENTE`
Objetivo: paridade Visual/PraCego no contexto VTT.
Passos:
1. Conectar/desconectar microfone/voz via aba `VTT`. Dono: `VTT + Ficha`.
2. Expor eventos com leitura acessivel (TalkBack/TTS). Dono: `Ficha`.
3. Garantir foco e ordem de navegacao consistente no `pracego`. Dono: `Ficha`.
4. Criar comandos de voz/texto basicos da sala. Dono: `VTT`.
5. Validar UX minima em Android. Dono: `Ficha`.
Criterio de aceite:
1. Sessao com voz e acessibilidade funcional.

### Lote VTT.6 - Nao Regressao e Estabilizacao
Status: `PENDENTE`
Objetivo: garantir coexistencia estavel dos dois projetos.
Passos:
1. Testes de nao regressao da ficha (todas abas antigas). Dono: `Ficha`.
2. Testes de import/export de ficha. Dono: `Ficha`.
3. Testes de sessao VTT (join, reconexao, rolagem, voz). Dono: `VTT + Ficha`.
4. Checklist de falhas de rede/timeouts e mensagens. Dono: `VTT + Ficha`.
5. Preparar release controlado da integracao. Dono: `VTT + Ficha`.
Criterio de aceite:
1. Build estavel e checklist de regressao 100% verde.

## Ordem pratica recomendada
1. VTT.1
2. VTT.3 (parte servidor primeiro)
3. VTT.2
4. VTT.4
5. VTT.5
6. VTT.6

## Regra operacional desta trilha
1. Nao alterar regras de calculo da ficha durante integracao VTT.
2. Toda mudanca de integracao ficara concentrada na aba `VTT` e camada de contrato.
3. Qualquer impacto em abas atuais exige lote separado e aprovacao explicita.
