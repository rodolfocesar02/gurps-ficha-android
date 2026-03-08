# PROGRESS - GURPS Ficha Android

Atualizado em: 2026-03-08

## Regra Permanente - Higiene de Texto (acentos/artefatos)
**Regra fixa para todos os lotes daqui em diante:**
1. Antes de commit de catálogo/UI, executar varredura de mojibake e acentuação quebrada nos arquivos ativos (`app/src/main/assets/*.json` e textos exibidos pela UI).
2. Bloquear publicação se houver strings com artefatos típicos (`Ã`, `Â`, `ï¿½`, `�`, `n?o`, `per?cia`, `pr?-requisito` e variantes).
3. Corrigir no próprio arquivo-fonte canônico antes de gerar APK.
4. Registrar no `PROGRESS.md` o relatório da varredura em cada lote que tocar texto.
5. Na rotina de reparo de mojibake, **não** tratar letras portuguesas válidas (ex.: `â`) como marcador de erro; usar apenas sequências típicas quebradas (`Ã`, `Â`, `â€`, `â€“`, `â€”`, `â€œ`, `â€\u009d`, `â€™`, `�`).

## Regras Operacionais
1. Sempre editar primeiro a fonte canônica de pré-requisito antes de mexer na UI.
2. Não aceitar correção só por "funcionou no caso X"; incluir teste de regressão.
3. Quando houver divergência entre JSON e regra canônica, corrigir ambos ou documentar exceção explícita.
4. Cada lote só fecha com:
- testes passando
- relatório em `app/build/reports/` ou `scripts/reports/`
- atualização deste `PROGRESS.md`
5. Commit por lote com mensagem objetiva (`lote-N: ...`).
6. No pipeline do AGENTE GURPS, páginas de abertura (capa/sumário) devem ser classificadas separadamente para não contaminar a métrica de suspeitas acionáveis.

## Saúde Atual (2026-03-08)
1. Build (visual/pracego): `100%` (compilação OK).
2. Testes críticos (pré-requisito/motor): `100%` (suite mínima OK).
3. Dados/catálogos ativos: `92%`.
4. UI/Acessibilidade (edição com rótulos): `94%`.
5. Higiene de texto: `91%`.
6. Versionamento: `93%`.

### Lote 1 - Padronização Global de Layout (Visual + PraCego)
Status: `PLANEJADO`

Objetivo:
1. Padronizar cards, botões, espaçamentos, títulos e hierarquia visual em todas as abas.
2. Garantir equivalência de usabilidade entre `visual` e `pracego`.

Passos:
1. [ ] Definir tokens únicos de UI (`spacing`, `corner`, `fontScale`, `buttonHeight`, `cardPadding`) em `UiStandards.kt`.
2. [ ] Unificar componentes reutilizáveis para ações principais/secundárias (mesmo comportamento entre abas).
3. [ ] Padronizar todos os cards de lista (Perícias, Técnicas, Magias, Equipamentos, Defesas, Rolagem).
4. [ ] Padronizar diálogos (título, bloco de conteúdo, ações finais, densidade vertical).
5. [ ] Revisar contraste e tamanho mínimo de toque para TalkBack (`pracego`).
6. [ ] Rodar checklist visual por aba + checklist de acessibilidade.

Critérios de aceite U5:
1. Nenhuma aba com estilo divergente de card/botão/fonte sem justificativa.
2. Mesma hierarquia de informação entre `visual` e `pracego`.
3. Relatório de validação por aba anexado no fechamento do lote.

## Melhorias Prioritárias de Intuitividade (Visual + PraCego)
1. Cabeçalho de cada aba com subtítulo de objetivo ("o que fazer aqui").
2. Botões de ação com nomenclatura única (evitar variações de verbo para a mesma ação).
3. Espaçamento vertical consistente entre seções e cards (ritmo visual único).
4. Estado vazio guiado (texto + CTA claro em listas sem itens).
5. Feedback imediato após ação (snackbar curto e acessível).
6. Rótulos TalkBack explícitos para editar/remover/abrir detalhes.
7. Ordem de foco previsível em todos os diálogos.
8. Em `pracego`, reforçar leitura de contexto no topo de cada diálogo (título + estado atual).

## Evidências de fechamento
1. `gh` instalado e autenticado para automação de release.
2. Release `V1.3` criada com os dois APKs (`visual` e `pracego`).
3. `docs/update/update.json` publicado com `versionCode=4` e links da `V1.3`.
4. Fluxo validado: menu `Atualizar app` e ação `Atualizar agora`.

## Comandos de Verificação (mínimo)
- `./gradlew :app:testVisualDebugUnitTest --tests com.gurps.ficha.regras_prerequisitos.PreRequisitoParserTest`
- `./gradlew :app:testVisualDebugUnitTest --tests com.gurps.ficha.domain.magias.NexusArcanoModoAlvoAdapterTest --tests com.gurps.ficha.domain.magias.NexusArcanoLoteFCanonicScenarioTest`
- `./gradlew :app:testVisualDebugUnitTest --tests nexus.arcano.NexusArcanoEngineLoteAGlobalTest --tests nexus.arcano.NexusArcanoEngineLoteBGlobalTest`
