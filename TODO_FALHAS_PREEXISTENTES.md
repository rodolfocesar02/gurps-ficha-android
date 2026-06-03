# TODO — 6 falhas de teste PRÉ-EXISTENTES (anteriores ao Lote 334)

> Criado em 2026-06-02. Estas falhas **já existiam antes** do Lote 334 (confirmado rodando o
> baseline sem o código do lote). NÃO são regressão. Tarefa dedicada para uma sessão futura.

## Objetivo (fluxo a seguir, em ordem)
1. **Mapear** cada falha (entrada, asserção que quebra, valor esperado vs obtido).
2. **Causa-raiz** — o que faz a falha acontecer (mudança de comportamento do motor? fixture
   desatualizada? regra alterada em lote anterior sem atualizar o teste?).
3. **Solução** — decidir por falha: corrigir o MOTOR (se o comportamento está errado) OU
   atualizar o TESTE/fixture (se o teste é que ficou obsoleto). Justificar a escolha.
4. **Corrigir.**
5. **Testar** — rodar a falha isolada + a classe inteira.
6. **Validar** — rodar a suíte alvo completa e confirmar 0 falhas (ou só as que foram
   conscientemente adiadas).

## Como reproduzir
```
./gradlew :app:testVisualDebugUnitTest \
  --tests "nexus.arcano.*" \
  --tests "com.gurps.ficha.regras_prerequisitos.*"
```
Relatórios em `app/build/reports/tests/testVisualDebugUnitTest/` e
`app/build/reports/nexus_arcano_*` (os de stress geram .txt com as inconsistências).

## As 6 falhas

| # | Classe::método | Pista inicial |
|---|----------------|---------------|
| 1 | `NexusArcanoEngineLote2Test::fallback_final_completa_tres_acoes_quando_so_ha_escolas_repetidas` | esperava 3 ações, veio 1. Lógica de `proximasAcoes`/fallback quando só há escolas repetidas. |
| 2 | `NexusArcanoEngineLote2Test::fallback_controlado_explica_quando_nao_ha_escola_nova_aprendivel` | esperava 3, veio 0. Mesmo subsistema de fallback de recomendação. |
| 3 | `NexusArcanoEngineLoteAGlobalTest::metas_globais_de_desejo_expoem_cadeia_escolas_e_alvo` | metas globais de "desejo" (cadeia + escolas + alvo). Conferir parsing de "1 magica em N escolas". |
| 4 | `NexusArcanoEngineStressMagiasV2Test::stress_ramificacoes_longas_com_magias_v2` | inconsistências no stress de ramificações (ver .txt do relatório). |
| 5 | `NexusArcanoEngineStressMagiasV2Test::sweep_escola_encantamento_consistencia_e_tempo_magias_v2` | sweep da escola Encantamento (ver .txt). |
| 6 | `PreRequisitoParserTest::repository validates fallback magia vantagem pericia and escudo exception` | parser: fallback magia/vantagem/perícia + exceção do escudo. |

## Notas / hipóteses
- #1 e #2 são do MESMO subsistema (fallback de `proximasAcoes` quando não há escola nova
  aprendível) — provavelmente uma causa-raiz única resolve as duas.
- #4 e #5 são testes de STRESS que escrevem .txt — começar lendo o relatório gerado para ver
  EXATAMENTE quais magias divergem antes de tocar no motor.
- Possível que vários destes sejam **fixtures desatualizadas** por mudanças de comportamento
  legítimas dos Lotes 330-334 (remoção de hardcodes, novo parsing). Nesse caso a correção é
  atualizar o teste, não o motor — mas SÓ depois de confirmar que o comportamento novo é o correto.
- Antes de corrigir, conferir se algum lote anterior já anotou essas falhas como "aceitas".

## Ao terminar
- Atualizar este arquivo (ou removê-lo) e registrar no PROGRESS.md como um lote próprio
  (código + commit docs com hash), seguindo o protocolo de lotes.
