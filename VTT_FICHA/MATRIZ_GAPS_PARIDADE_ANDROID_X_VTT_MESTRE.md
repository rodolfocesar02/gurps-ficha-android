# Matriz de Gaps de Paridade - Android x VTT_Mestre
Data: 2026-03-11
Status: FPAR.1 Passo 1 CONCLUIDO

## Metodo de classificacao
- OK: comportamento equivalente no fluxo principal.
- PARCIAL: existe implementacao, mas difere do Android ou falta parte critica.
- FALTANDO: nao existe implementacao funcional equivalente.

## 1) Paridade por aba da ficha
1. Geral: OK
- VTT_Mestre possui edicao de nome, jogador, campanha, pontos iniciais e atributos primarios/secundarios.

2. Tracos (vantagens/desvantagens): PARCIAL
- VTT_Mestre exibe e permite catalogo/edicao dentro da aba de pericias.
- Gap: Android trata Traços como aba dedicada; VTT_Mestre mistura com Pericias.

3. Pericias: OK
- Cadastro via catalogo, ajuste de pontos, NH calculado e rolagem.

4. Tecnicas: FALTANDO (visivel para usuario)
- Existe suporte de catalogo no estado interno, mas nao ha secao/lista/acoes de tecnicas na UI da ficha web.
- Impacto: paridade de combate e rolagens contextuais fica incompleta.

5. Magias: OK/PARCIAL
- Cadastro e rolagem existem.
- Gap parcial: validar 100% dos gates/pre-requisitos do Android em cenario real.

6. Equipamentos: OK
- Inventario, equipar/desequipar, quantidade e uso no combate.

7. Defesas: PARCIAL
- VTT_Mestre mostra defesas dentro de Combate.
- Gap: Android possui aba Defesas explicita com fluxo proprio.

8. Rolagem: PARCIAL
- VTT_Mestre possui rolagens contextuais e APP_ROLL.
- Gap: falta equivalencia visual/funcional com painel dedicado da aba Rolagem do Android.

9. Notas/Historico: OK
- Campos presentes na ficha web.

## 2) Paridade de integracao App <-> VTT
1. VTT_JOIN: OK
2. ROOM_STATE: OK
3. TOKEN_SELECTED: OK
4. ROLL_RESULT: OK
5. APP_ROLL: OK
6. FICHA_SYNC: PARCIAL
- Evento existe e despacha para app.
- Gap: falta validar ciclo completo em sessao real com aplicacao no estado da ficha Android.

## 3) Ciclo do token do jogador
1. Criar/reaproveitar token no join: OK
2. Vinculo playerId -> tokenId: OK
3. Selecionar token e retorno de evento: OK
4. Mover token no mapa: OK
5. Reentrada sem duplicacao: PARCIAL
- Existe estrategia de reaproveitamento, precisa teste de nao duplicacao em reconexao multipla.

## 4) Gaps priorizados para execucao
Prioridade P0 (bloqueia aceite de negocio):
1. Tecnicas visiveis e operacionais na ficha web (listar, editar, remover, rolar).
2. Fluxo dedicado de rolagem equivalente ao Android (inclusive contexto token/alvo).
3. Validacao ponta-a-ponta de FICHA_SYNC no app Android.

Prioridade P1 (qualidade/paridade UX):
1. Separar/organizar Traços e Defesas com navegacao equivalente ao Android.
2. Validar prerequisitos de magias/pericias com mesma regra do Android.
3. Testar reconexao sem duplicacao de token em cenarios longos.

## 5) Lotes derivados desta matriz
1. FPAR.2 - UI/Fluxo: Tecnicas + ajuste de Traços/Defesas/Rolagem.
2. FPAR.3 - Mecanicas: prerequisitos e rolagens contextuais completas.
3. FPAR.4 - Integracao Token/Ficha: FICHA_SYNC e reconexao sem duplicacao.
4. FPAR.5 - Nao regressao e aceite final.

## Evidencias usadas nesta matriz
1. Android: FichaScreen e abas (Geral, Traços, Pericias, Tecnicas, Magia, Equip., Defesas, Rolagem, VTT).
2. VTT_Mestre: FichaGurps.tsx, App.tsx, WebGLMap.tsx, backend/app.js.
3. Contratos observados ativos: VTT_JOIN, APP_ROLL, ROOM_STATE, TOKEN_SELECTED, ROLL_RESULT, FICHA_SYNC.
