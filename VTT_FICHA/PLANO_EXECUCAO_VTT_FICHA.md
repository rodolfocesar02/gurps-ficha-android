# Plano de Execucao VTT + Ficha (v1)

Data: 2026-03-09
Escopo: Integracao da aba `VTT` da ficha Android com backend/frontend do VTT, sem alterar mecanicas das abas atuais da ficha.

## Responsabilidades por lado

### VTT (backend/frontend web)
1. Implementar contratos de sessao e token (`join_room`, bind/rebind de token).
2. Implementar contratos de rolagem (`acao_rolagem_dado`, `resultado_rolagem`, `erro_acao`).
3. Expor estado de sala e reconexao.
4. Validar payloads e garantir seguranca minima.
5. Publicar URL estavel de ambiente (homolog/prod).

### Ficha Android (app)
1. Criar aba `VTT` como cliente fino.
2. Conectar na sala e persistir sessao local.
3. Enviar snapshot de ficha no join.
4. Enviar acao de rolagem e renderizar resultado.
5. Tratar timeout, reconexao e mensagens de erro amigaveis.

## Ordem ideal (para nao travar)

1. VTT fecha endpoints/eventos minimos de sessao.
2. VTT fecha endpoints/eventos minimos de rolagem.
3. Ficha implementa shell de conexao da aba `VTT`.
4. Ficha integra join/sessao com token.
5. Ficha integra fluxo de rolagem.
6. VTT e Ficha validam reconexao e erros.
7. Polimento visual + acessibilidade.

## Lotes e passos executaveis

## Lote A - Contrato vivo e smoke de servidor
Dependencia: nenhuma

### Passo A1 (VTT)
Implementar `POST /api/v1/session/join` com retorno de `sessionId` e `tokenId`.
Aceite:
1. Retorno 200 com `roomKey`, `playerId`, `sessionId`, `tokenId`.
2. Retorno 4xx para payload invalido.

### Passo A2 (VTT)
Implementar eventos WS `join_room`, `ficha_snapshot_sync`.
Aceite:
1. Cliente recebe `estado_inicial`.
2. Join repetido recupera token quando possivel.

### Passo A3 (VTT)
Implementar `POST /api/v1/roll/request` e WS `resultado_rolagem`.
Aceite:
1. Requisicao valida gera resultado no chat/sessao.
2. Requisicao invalida retorna `erro_acao`.

## Lote B - Aba VTT no app (conexao e sessao)
Dependencia: Lote A concluido

### Passo B1 (Ficha)
Adicionar aba `VTT` na navegacao, sem tocar abas existentes.
Aceite:
1. Abas antigas idempotentes.
2. Aba VTT com estados: desconectado/conectando/conectado.

### Passo B2 (Ficha)
Tela de conexao com `serverUrl`, `roomKey`, `playerId`.
Aceite:
1. Persistencia local dos dados da sessao.
2. Validacao de campos obrigatorios.

### Passo B3 (Ficha)
Fluxo `join_room` + envio de snapshot da ficha.
Aceite:
1. Recebe `tokenId` e salva `sessionId`.
2. Reconexao reaproveita sessao.

## Lote C - Rolagem contextual dentro da aba VTT
Dependencia: Lote B concluido

### Passo C1 (Ficha)
Painel de acoes basicas no token proprio: teste/pericia/magia.
Aceite:
1. Monta payload `acao_rolagem_dado`.
2. Envia com `requestId`.

### Passo C2 (Ficha)
Receber `resultado_rolagem` e exibir no painel/chat local.
Aceite:
1. Texto resumido e valor de rolagem visiveis.
2. Erro exibe mensagem amigavel com tentativa novamente.

### Passo C3 (VTT + Ficha)
Padronizar `erro_acao` para casos de token invalido/sessao expirada.
Aceite:
1. App exibe CTA para reconectar.
2. Sem travamentos de UI.

## Lote D - Voz, estabilidade e release
Dependencia: Lotes A/B/C concluidos

### Passo D1 (Ficha + VTT)
Integrar controles de voz existentes no VTT com status na aba VTT.
Aceite:
1. Entrar/sair de voz.
2. Atualizacao de estado de moderacao.

### Passo D2 (Ficha)
Acessibilidade (Visual + PraCego) para elementos da aba VTT.
Aceite:
1. Rotulos claros de conexao/erro/rolagem.
2. Ordem de foco consistente.

### Passo D3 (VTT + Ficha)
Teste ponta a ponta + checklist final de regressao.
Aceite:
1. Join/sessao/rolagem/reconexao ok.
2. Nenhuma regressao nas abas antigas da ficha.

## Definicao de pronto (DoD)
1. Contrato v1 atendido sem campos ambiguos.
2. Fluxo de mesa funciona sem abrir aba de rolagem antiga.
3. Build Android funcional e backend VTT estavel.
4. Logs e erros suficientes para suporte.
