# Checklist de Aceite FPAR.5
Data: 2026-03-11
Escopo: Paridade Ficha Android x VTT_Mestre (Aba VTT)

## Resultado consolidado
- Status geral: PARCIAL (tecnico OK, validacao manual final pendente)

## 1) Validacoes tecnicas executadas
1. Android build minimo: `:app:assembleVisualDebug -x lint` -> OK
2. Backend VTT testes: `npm test` -> OK (33/33)
3. Frontend VTT build: `npm run build` -> OK

## 2) Contrato de integracao (bridge)
1. `VTT_JOIN` -> OK (envio app e consumo VTT)
2. `APP_ROLL` -> OK (envio app e processamento VTT)
3. `ROOM_STATE` -> OK (dispatch VTT para app)
4. `TOKEN_SELECTED` -> OK (dispatch VTT para app)
5. `ROLL_RESULT` -> OK (dispatch VTT para app)
6. `FICHA_SYNC` -> OK com filtro de alvo no Android (player/token)

## 3) Regras de aceite da Aba VTT
1. Entrar na sala com 1 botao -> PARCIAL
- Fluxo tecnico implementado (`Conectar` + auto join/bridge), requer validacao humana em aparelho.

2. Mapa ocupa area principal da aba -> PARCIAL
- Modo imersivo ativo e embed funcional; validacao visual final em aparelho pendente.

3. Token do jogador aparece e interage -> PARCIAL
- Backend reforcado para reconexao sem duplicacao de token; precisa smoke manual com mestre+jogador.

4. Acoes de ficha funcionam no token/alvo -> PARCIAL
- APP_ROLL/selecao de token e gating de tecnicas/magias estao ativos; validar cenario real completo.

5. Estado de combate sincroniza com ficha -> PARCIAL
- FICHA_SYNC com validacao de alvo implementado; falta prova manual em sessao real.

6. Usuario leigo sem configuracao tecnica -> BLOQUEADO (externo)
- Ainda depende de rotina operacional de execucao do servidor VTT para ambiente local.

## 4) Bloqueios externos para aceite 100%
1. Executar smoke manual com dois clientes (mestre web + app Android) na mesma sala.
2. Registrar evidencia de reconexao real sem duplicar token.
3. Confirmar fluxo de uso leigo sem ajuste manual de rede na instalacao alvo.

## 5) Conclusao do passo
- FPAR.5 Passo 1 concluido (checklist objetivo + evidencias tecnicas).
- FPAR.5 Passo 2 necessario para aceite final 100%: rodada manual assistida e fechamento dos bloqueios externos.
