# Execucao Rapida (v1)

Guia pratico para coordenar os dois projetos sem bloqueio.

## Semana 1 - VTT primeiro

### Tarefa 1 (VTT)
Entregar `POST /api/v1/session/join`.
Pronto quando:
1. Retorna `sessionId` e `tokenId`.
2. Rejeita payload invalido com erro padrao.

### Tarefa 2 (VTT)
Entregar eventos WS `join_room` e `ficha_snapshot_sync`.
Pronto quando:
1. Join funciona com `roomKey` + `playerId`.
2. Reconexao reaproveita token quando possivel.

### Tarefa 3 (VTT)
Entregar rolagem integrada (`POST /api/v1/roll/request` + `resultado_rolagem` + `erro_acao`).
Pronto quando:
1. Pedido valido gera resultado de rolagem.
2. Pedido invalido retorna erro padronizado.

## Semana 2 - Ficha integra

### Tarefa 4 (Ficha)
Criar aba `VTT` com estados de conexao.
Pronto quando:
1. Abas antigas continuam iguais.
2. Aba mostra desconectado/conectando/conectado.

### Tarefa 5 (Ficha)
Integrar join de sessao e receber token.
Pronto quando:
1. Envia snapshot da ficha no join.
2. Guarda `sessionId`/`tokenId` localmente.

### Tarefa 6 (Ficha)
Integrar rolagem por acao.
Pronto quando:
1. Envia `acao_rolagem_dado`.
2. Mostra `resultado_rolagem` e `erro_acao` no app.

## Semana 3 - Estabilidade

### Tarefa 7 (VTT + Ficha)
Reconexao, timeout e retentativa.
Pronto quando:
1. Queda de rede nao quebra sessao definitivamente.
2. Mensagens de erro sao claras para usuario.

### Tarefa 8 (Ficha)
Acessibilidade da aba VTT (Visual/PraCego).
Pronto quando:
1. Rotulos e foco corretos.
2. Leitura do resultado de rolagem sem ambiguidade.

### Tarefa 9 (VTT + Ficha)
Teste ponta a ponta.
Pronto quando:
1. Join/sessao/rolagem/reconexao ok.
2. Sem regressao nas abas atuais da ficha.

## Regra de coordenação
1. Nao iniciar tarefa da Ficha que dependa de endpoint/evento ainda nao entregue pelo VTT.
2. Sempre validar com payload real do contrato v1.
3. Em divergencia, atualizar o contrato antes do codigo.
