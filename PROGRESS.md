# PROGRESS - GURPS Ficha Android

Atualizado em: 2026-03-10

## Atualização Rápida - 2026-03-10 (Aba VTT Embed)
Status: `EM ANDAMENTO`

Escopo executado:
1. Aba `VTT` reativada na V2 com WebView embed (mapa/grid dentro do app).
2. WebView com `hardware acceleration` forçado via `setLayerType(HARDWARE)` e `android:hardwareAccelerated="true"` no Manifest.
3. Permissão adicionada para áudio: `android.permission.MODIFY_AUDIO_SETTINGS`.
4. Tratamento de erro do WebView no UI (main-frame, HTTP e console).
5. Diagnóstico WebGL inline: checagem `canvas/webgl` e exibição de erro no app.
6. WebView debug habilitado (`WebView.setWebContentsDebuggingEnabled(true)`).
7. Corrigido bloco do `AndroidView`/`WebView` (sintaxe Kotlin) e reinstalado visual no emulador.
8. Default da aba VTT apontando para produção (API/Web), layout 16:9 full embed, mensagem neutra de carregamento (“aguardando mestre”) e campo de configuração avançada oculto por padrão.
9. Bridge parcial app→VTT: ficha JSON enviada automaticamente ao carregar o embed; captura de ROOM_STATE/ROLL_RESULT/AUDIO_STATE/FICHA_SYNC no app para depuração.

Estado atual:
1. App compila e instala (visual).
2. VTT no desktop renderiza mapa/grid normalmente.
3. No emulador, o embed mostra `Canvas/WebGL indisponível: no_canvas` (tela preta).

Pendências imediatas:
1. Investigar por que WebView do emulador não expõe Canvas/WebGL (provável limitação de WebView/Emulador).
2. Implementar fallback 2D (Canvas) no VTT quando `no_canvas`/`no_webgl` for detectado no WebView.
3. Confirmar logs do WebView via `chrome://inspect` após fallback.
4. Revalidar render do mapa/grid dentro da Aba VTT.

## Atualização Rápida - 2026-03-09 (Desvantagens)
Status: `CONCLUÍDO`

Escopo executado:
1. Preenchidas as descrições faltantes em `app/src/main/assets/desvantagens.v2.json` (cobertura final: `227/227` com descrição).
2. Corrigido nome canônico no catálogo: `Suscetibilidade à Magia`.
3. Ajustado carregamento V2 em `DataRepository` para preservar `rawCost` em `costKind=fixed` (mantém marcador `*` e corrige detecção de autocontrole mental).
4. Criado script de apoio para preenchimento por PDF canônico: `scripts/fill_missing_desvantagens_from_pdf.py`.

Validação registrada:
1. Varredura final em `desvantagens.v2.json`: `0` itens sem descrição.
2. Varredura final de mojibake (`Ã`, `Â`, `â€`, `�`): `0` ocorrências.

## Plano Macro - Versão 2.0 (sem alterar V1 em uso)
Status: `ATIVO`

Diretriz:
1. A V1 atual continua estável e utilizável.
2. Toda evolução de produto passa a entrar como trilha V2, em lotes independentes.
3. Cada lote V2 fecha com commit próprio + validação mínima.

### Lote V2.1 - Estrutura Segura de Evolução
Status: `CONCLUÍDO`

Passos:
1. [x] Definir no `PROGRESS.md` a trilha V2 separada da V1.
2. [x] Garantir que artefatos locais do AGENTE não entrem em commit (`.gitignore`).
3. [x] Criar branch de trabalho dedicada (`v2-main`) para desenvolvimento contínuo.
4. [x] Criar checklist de "não regressão V1" por release V2.

Critérios de aceite:
1. Repositório com base limpa para começar V2 sem risco de regressão acidental.
2. Commits V2 rastreáveis por lote e passo.

Evidência:
1. Checklist criado em `docs/v2/NON_REGRESSION_V1_CHECKLIST.md`.

### Lote V2.2 - Padronização Visual Global (Visual + PraCego)
Status: `EM ANDAMENTO`

Passos:
1. [x] Congelar tokens de UI globais (espaço, tipografia, raio, tamanhos de toque).
2. [x] Aplicar padrão único de card no núcleo (Perícias, Técnicas e Magias).
3. [x] Estender padrão único de card para Traços.
4. [x] Estender padrão único de card para Equipamentos.
5. [x] Estender padrão único de card para Defesas.
6. [x] Estender padrão único de card para Rolagem (base de cor/container).
7. [x] Unificar diálogos de seleção/edição (título, densidade, ações finais).
8. [x] Subpasso: padronizar densidade dos diálogos de descrição (Perícias, Técnicas e Magias).
9. [ ] Rodar checklist de contraste/foco/rótulos TalkBack.
10. [x] Validar instalação e abertura em emulador visual e pracego.
11. [x] Subpasso: validar build das variantes visual/pracego após padronização V2.
12. [x] Subpasso: criar checklist operacional de acessibilidade V2.2 (`docs/v2/A11Y_CHECKLIST_V2_2.md`).
13. [x] Subpasso: habilitar execução estável do lint (desativando detectores Compose com crash) e gerar relatório de erros reais.
14. [x] Subpasso: padronizar espaçamento de conteúdo em diálogos principais (`Dialogs*.kt`) com `UiTokens.DialogContentSpacing`.
15. [x] Subpasso: executar pré-check automatizado de A11Y e registrar relatório (`docs/v2/A11Y_CHECKLIST_V2_2_EXEC.md`).

Evidência parcial adicional:
1. `UiActionLabels` aplicado nos diálogos principais para ações finais consistentes.
2. `StandardDialogColumn` e `UiTokens.DialogContentSpacing` ativos no corpo dos diálogos de seleção/edição e descrição.
3. Pré-check automatizado: `contentDescription=129`, `pracegoTraversal=14`.

### Lote V2.3 - Fluxo de Dados e Regras Canônicas
Status: `CONCLUÍDO`

Passos:
1. [x] Auditoria de JSONs ativos (pré-requisitos, nomes e acentuação).
2. [x] Correção orientada por canônico com testes de regressão (sem divergências detectadas nesta rodada).
3. [x] Varredura automática de artefatos de encoding antes de build.
4. [x] Relatório de cobertura por catálogo (vantagens, desvantagens, perícias, técnicas, magias).

Evidência:
1. `scripts/reports/active_json_audit_v2.json`
2. `scripts/reports/active_json_audit_v2.md`
3. `scripts/reports/catalog_coverage_v2.json`
4. `scripts/reports/catalog_coverage_v2.md`
5. `app:preBuild` agora depende de `validateActiveJsonAssets` (bloqueio automático em caso de issues).
6. Regressão executada:
   - `:app:testVisualDebugUnitTest --tests com.gurps.ficha.regras_prerequisitos.PreRequisitoParserTest`
   - `:app:testVisualDebugUnitTest --tests com.gurps.ficha.domain.magias.NexusArcanoModoAlvoAdapterTest --tests com.gurps.ficha.domain.magias.NexusArcanoLoteFCanonicScenarioTest`
   - `:app:testVisualDebugUnitTest --tests nexus.arcano.NexusArcanoEngineLoteAGlobalTest --tests nexus.arcano.NexusArcanoEngineLoteBGlobalTest`

### Lote V2.4 - UX de Edição e Intuitividade
Status: `CONCLUÍDO`

Passos:
1. [x] Revisar textos de ação para consistência (Adicionar/Editar/Remover/Fechar).
2. [x] Padronizar feedback pós-ação (snackbar curto + acessível).
3. [x] Melhorar estado vazio e mensagens de erro orientadas a solução.
4. [x] Definir mapa de foco final para `pracego`.

Evidência parcial:
1. Novo arquivo `app/src/main/java/com/gurps/ficha/ui/UiActionLabels.kt` centralizando rótulos de ação.
2. Diálogos de Perícias/Técnicas/Magias/Traços/Comuns migrados para uso de `UiActionLabels`.
3. `FichaScreen` passou a usar `SnackbarHostState` para feedback curto nas ações: nova ficha, salvar, carregar, excluir, exportar e importar.
4. Novo componente `GuidedEmptyState` em `UiStandards.kt` aplicado nas abas Perícias, Técnicas e Magias.
5. Mensagens de fallback orientadas para ação aplicadas em importação e em diálogo de carregamento sem fichas salvas.
6. Mapa de foco documentado em `docs/v2/PRACEGO_FOCUS_MAP_V2_4.md`.
7. Novo utilitário `Modifier.pracegoTraversal(...)` aplicado no menu principal e nos pontos de entrada de Perícias/Técnicas/Magias.
8. Aba Rolagem: descrição disponível nas listas de Perícias/Técnicas/Magias (toque no nome abre popup).
9. Aba Rolagem: opção de dano ST alternável entre `GdP` e `GeB` quando não houver arma selecionada.
10. Validação executada:
   - `./gradlew :app:compileVisualDebugKotlin :app:compilePracegoDebugKotlin` (OK)

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

## Evidências de fechamento
1. `gh` instalado e autenticado para automação de release.
2. Release `V1.3` criada com os dois APKs (`visual` e `pracego`).
3. `docs/update/update.json` publicado com `versionCode=4` e links da `V1.3`.
4. Fluxo validado: menu `Atualizar app` e ação `Atualizar agora`.

## Comandos de Verificação (mínimo)
- `./gradlew :app:testVisualDebugUnitTest --tests com.gurps.ficha.regras_prerequisitos.PreRequisitoParserTest`
- `./gradlew :app:testVisualDebugUnitTest --tests com.gurps.ficha.domain.magias.NexusArcanoModoAlvoAdapterTest --tests com.gurps.ficha.domain.magias.NexusArcanoLoteFCanonicScenarioTest`
- `./gradlew :app:testVisualDebugUnitTest --tests nexus.arcano.NexusArcanoEngineLoteAGlobalTest --tests nexus.arcano.NexusArcanoEngineLoteBGlobalTest`
