# PROGRESS - GURPS Ficha Android

Atualizado em: 2026-03-07

## Objetivo Atual
Garantir que o **Modo Alvo** aplique os pré-requisitos de magia com comportamento canônico GURPS, sem divergência entre telas, motor e catálogo.

## Estado Atual
Feito:
1. Fonte canônica de pré-requisito aplicada no carregamento de catálogo e no fluxo principal de validação.
2. Catálogo runtime (`magias2versao.json`) sincronizado com overrides canônicos (`same=36`, `diff=0`).
3. Pendências com marcador `#` sem override canônico resolvidas (`sem_override_com_hash=0`).
4. Motor do modo alvo ajustado para tratar alternativas com `ou` usando parser por termo, sem perder dependências compartilhadas.
5. Prioridade hard-first aplicada na recomendação (`cadeia -> escolas -> alvo`), sem lateral furar bloqueio de cadeia.
6. Auditoria global de todas as magias estabilizada sem OOM, com relatório gerado em `app/build/reports/`.
7. Manual curto do Modo Alvo implementado com pop-up automático no primeiro clique e opção persistente `Não mostrar mais`.
8. Manual do Modo Alvo refinado para UX: pop-up automático no primeiro uso, opção persistente `Não mostrar mais` e rótulos de acessibilidade nos elementos críticos para `pracego`.
9. Variante `visual`: diálogos de Vantagens/Desvantagens/Técnicas ajustados para controle por gesto vertical (swipe), sem botões `+/-`.
10. Variante `pracego`: diálogos mantêm botões `+/-` funcionais e rotulados para TalkBack.
11. Aba Rolagem com rótulos dinâmicos incluindo valor atual de clique (`Rolar ST X`, `Rolar DX X`, `Rolar Esquiva X` etc.).
12. Diálogo de Técnicas refinado: título removido, nome da técnica em destaque, texto redundante removido e resumo final no formato `Nome da Técnica NH X`.
13. Motor de pré-requisito de Técnicas reforçado com guarda por família de perícia (tiro/esgrima/corpo a corpo/defesa ativa), reduzindo combinações incoerentes.

Falta:
1. Revisão sistemática das técnicas de Artes Marciais com pré-requisitos sensíveis contra o PDF (ajuste fino por técnica).
2. Executar bateria de validação pós-ajuste (build visual/pracego + verificação em emulador).

## Diagnóstico Confirmado
1. Divergência de validação rápida vs hierárquica na UI foi eliminada no `FichaViewModel`.
2. Uso de texto bruto sem canônico no runtime foi corrigido para a carga de `MagiaDefinicao`.
3. Divergências de catálogo runtime vs override canônico foram saneadas para o conjunto mapeado.
4. Casos combinados avançados de parser/motor foram cobertos com seleção de branch relevante por estado.
5. Auditoria global `NexusArcanoModoAlvoAuditoriaTodasMagiasTest` executa sem OOM com limites controlados de BFS.

## Plano de Ação (Lotes)

### Lote 1 - Fonte Única de Pré-Requisito Canônico
Status: `CONCLUIDO`

Passos:
1. [x] Criar no `DataRepository` uma função pública de acesso canônico por magia (`id -> preRequisitoCanonico`).
Arquivo: `app/src/main/java/com/gurps/ficha/data/DataRepository.kt`
2. [x] Aplicar override canônico no carregamento de magias (não só na validação textual), para que `MagiaDefinicao.preRequisitos` já saia normalizado.
Arquivo: `app/src/main/java/com/gurps/ficha/data/DataRepository.kt`
3. [x] Garantir que `FichaViewModel` e `NexusArcanoModoAlvoAdapter` usem essa mesma fonte, sem caminhos paralelos.
Arquivos:
- `app/src/main/java/com/gurps/ficha/viewmodel/FichaViewModel.kt`
- `app/src/main/java/com/gurps/ficha/domain/magias/NexusArcanoModoAlvoAdapter.kt`

Critério de aceite:
- Para uma magia qualquer, texto de pré-requisito exibido/validado/recomendado é idêntico em todos os fluxos.

### Lote 2 - Saneamento do Catálogo Runtime (`magias2versao.json`)
Status: `CONCLUIDO`

Passos:
1. [x] Auditar divergências entre `magias2versao.json` e overrides canônicos (`preRequisitosOverridePorMagiaId`).
2. [x] Corrigir entradas críticas de progressão (prioridade alta):
- `encantar`
- `pequeno_desejo`
- `desejo`
- `desejo_superior`
- cadeias de portal/expulsão/convocação com contagem de escolas
3. [x] Sincronizar no runtime as 36 entradas com override canônico (resultado: `same=36`, `diff=0` no auditor local).
4. [x] Corrigir entradas com marcador `#` que devem virar regra explícita canônica quando houver definição conhecida e ainda não possuem override (resultado: `sem_override_com_hash=0`).
Arquivo: `app/src/main/assets/magias2versao.json`

Critério de aceite:
- Diferenças entre catálogo runtime e regra canônica reduzidas aos casos explicitamente não automatizáveis.

### Lote 3 - Unificação da Validação de Pré-Requisito
Status: `CONCLUIDO`

Passos:
1. [x] Definir um único contrato de validação para UI e Modo Alvo (mesma semântica de bloqueio por magia).
2. [x] Eliminar fallback conflitante no `FichaViewModel` entre validação rápida e validação principal.
3. [x] Fechar auditoria de divergência residual entre mensagem de chave do modo alvo e bloqueio de adição na UI (adapter usa fonte única de mensagem de bloqueio).
Arquivos:
- `app/src/main/java/com/gurps/ficha/viewmodel/FichaViewModel.kt`
- `app/src/main/java/com/gurps/ficha/data/DataRepository.kt`
- `app/src/main/java/com/gurps/ficha/domain/magias/NexusArcanoModoAlvoAdapter.kt`

Critério de aceite:
- Mesmo personagem + mesma magia => mesma resposta de bloqueio em lista, diálogo e modo alvo.

### Lote 4 - Correção Semântica do Parser do Motor (`ou`/alternativas)
Status: `CONCLUIDO`

Passos:
1. [x] Revisar `dependenciasNomeadasGrupos` para preservar corretamente alternativas compostas (`A ou B, C`).
2. [x] Associar seleção de dependências nomeadas ao parser por termo (vírgulas + `ou`), com fallback heurístico.
3. [x] Cobrir casos de `outras escolas`, soma de atributos e requisitos nomeados simultâneos (seleção de branch relevante por estado em `magiaAprendivelAgora` e avaliação de candidatas).
Arquivo: `motor modo alvo/src/NexusArcanoEngine.kt`

Critério de aceite:
- O motor não recomenda cadeia impossível nem ignora restrição de alternativa.

### Lote 5 - Ordem Canônica de Progressão
Status: `CONCLUIDO`

Passos:
1. [x] Reforçar prioridade hard-first:
- cadeia nomeada obrigatória
- depois contagem de escolas
- depois alvo final
2. [x] Garantir que recomendação lateral não fure bloqueio de cadeia.
Arquivo: `motor modo alvo/src/NexusArcanoEngine.kt`

Critério de aceite:
- Cenário `Encantar -> Pequeno Desejo -> Desejo` sempre respeitado antes de fechamento final.

### Lote 6 - Testes, Auditoria e Evidência
Status: `CONCLUIDO`

Passos:
1. [x] Adicionar testes de regressão para:
- `nao pode ser/ter` (cego/cegueira, surdo/surdez)
- `(DX + IQ):30+`
- `outras escolas` vs `escolas diferentes`
- alternativa com `ou`
Arquivos:
- `app/src/test/java/com/gurps/ficha/regras_prerequisitos/PreRequisitoParserTest.kt`
- `app/src/test/java/nexus/arcano/*`
2. [x] Ajustar auditoria global para não estourar memória (limites de BFS/estado aplicados no teste global).
Arquivo: `app/src/test/java/nexus/arcano/NexusArcanoModoAlvoAuditoriaTodasMagiasTest.kt`
3. [x] Gerar relatórios em `app/build/reports/` com resumo de sucesso/falha.

Critério de aceite:
- Suite alvo passa sem OOM e com relatório reproduzível.

### Lote 7 - UX Modo Alvo e Acessibilidade
Status: `CONCLUIDO`

Passos:
1. [x] Implementar manual curto do Modo Alvo com abertura automática no primeiro uso.
Arquivo: `app/src/main/java/com/gurps/ficha/ui/DialogsMagias.kt`
2. [x] Persistir preferência `Não mostrar mais`.
Arquivo: `app/src/main/java/com/gurps/ficha/viewmodel/FichaViewModel.kt`
3. [x] Remover botão manual dedicado e manter somente o pop-up inicial.
Arquivo: `app/src/main/java/com/gurps/ficha/ui/DialogsMagias.kt`
4. [x] Rotular elementos críticos para TalkBack no fluxo do manual e no toggle do Modo Alvo.
Arquivo: `app/src/main/java/com/gurps/ficha/ui/DialogsMagias.kt`

Critério de aceite:
- Primeiro clique em Modo Alvo abre manual; após marcar `Não mostrar mais`, não reaparece.

### Lote 8 - Ajustes de Interação (Visual vs PraCego)
Status: `CONCLUIDO`

Passos:
1. [x] Trocar `+/-` por swipe vertical em Vantagens/Desvantagens na variante `visual`.
Arquivo: `app/src/main/java/com/gurps/ficha/ui/DialogsTracos.kt`
2. [x] Manter `+/-` na variante `pracego` com rótulos de acessibilidade.
Arquivos:
- `app/src/main/java/com/gurps/ficha/ui/DialogsTracos.kt`
- `app/src/main/java/com/gurps/ficha/ui/DialogsTecnicas.kt`
3. [x] Aplicar mesmo comportamento em Técnicas (visual swipe, pracego botões).
Arquivo: `app/src/main/java/com/gurps/ficha/ui/DialogsTecnicas.kt`
4. [x] Ajustar semântica de rolagens para anunciar o valor atual no botão.
Arquivo: `app/src/main/java/com/gurps/ficha/ui/TabRolagem.kt`

Critério de aceite:
- Visual opera sem botões `+/-` nesses diálogos; PraCego anuncia e opera com `+/-`; rolagens anunciam atributo/defesa + valor.

### Lote 9 - Coerência de Pré-Requisito de Técnicas
Status: `CONCLUIDO`

Passos:
1. [x] Melhorar diálogo de configuração/edição para mostrar apenas perícias compatíveis.
Arquivo: `app/src/main/java/com/gurps/ficha/ui/DialogsTecnicas.kt`
2. [x] Reforçar validação por família de requisito (tiro, esgrima, corpo a corpo, defesa ativa) no motor de técnicas.
Arquivo: `app/src/main/java/com/gurps/ficha/viewmodel/FichaViewModel.kt`
3. [~] Revisar técnica por técnica com base no PDF de Artes Marciais para eliminar incoerências restantes.
   - [x] Passo 3.1: reforçada a classificação para termos ambíguos (`arma apropriada`, `ataque corpo a corpo`) no motor de pré-requisito de técnicas, evitando aceitar perícias de tiro em técnicas de combate corpo a corpo.
   - [x] Passo 3.2: removido `Escudo` da classificação de `arma corpo a corpo` para impedir seleção indevida como perícia base em técnicas de arma.
4. [x] Fechar validação final em emulador e testes direcionados.
   - [x] `scripts/validate_tecnicas_prerequisitos.py` executado (`incoerentes=0`, `revisaoManual=0`).
   - [x] Testes unitários direcionados executados: `PreRequisitoParserTest` e `PericiaJsonParsingTest`.
   - [x] Build `visual` instalada no emulador (`:app:installVisualDebug`).

Critério de aceite:
- Técnicas com pré-requisito específico só aceitam perícias compatíveis com a regra textual/canônica.

### Lote 10 - Auditoria de Rotulagem por Aba (PraCego)
Status: `EM_ANDAMENTO`

Passos:
1. [x] Registrar lotes e checklist de execução no `PROGRESS.md`.
2. [x] Corrigir anúncio de estado "pré-requisito atendido" antes do cálculo finalizar na lista de Magias (TalkBack).
3. [x] Rotular explicitamente ação de editar em item de Perícias.
4. [x] Rotular explicitamente ação de editar em item de Técnicas.
5. [ ] Validar com build `visual/pracego` + instalação no emulador.

Critério de aceite:
- Fluxo de Magias/Perícias/Técnicas na variante `pracego` sem ambiguidade de ação para TalkBack.

### Lote 11 - Fechamento e Publicação
Status: `PENDENTE`

Passos:
1. [ ] Atualizar `PROGRESS.md` com fechamento dos lotes 10/11 e evidências de validação.
2. [ ] Confirmar árvore de commit sem `pdf/xlsx`.
3. [ ] Executar push para `origin/main`.

Critério de aceite:
- Branch remota atualizada sem artefatos indevidos (`.pdf`/`.xlsx`).

## Regras Operacionais
1. Sempre editar primeiro a fonte canônica de pré-requisito antes de mexer na UI.
2. Não aceitar correção só por "funcionou no caso X"; incluir teste de regressão.
3. Quando houver divergência entre JSON e regra canônica, corrigir ambos ou documentar exceção explícita.
4. Cada lote só fecha com:
- testes passando
- relatório em `app/build/reports/`
- atualização deste `PROGRESS.md`
5. Commit por lote com mensagem objetiva (`lote-N: ...`).

## Operação Atual (2026-03-07)
1. [x] Registrar e versionar o início da validação no emulador.
2. [x] Validar comportamento da build `visual` em execução real.
3. [x] Tentar remover qualquer pacote instalado (`visual`/`pracego`) no emulador.
4. [x] Instalar novamente apenas a versão `visual`.
5. [x] Confirmar abertura de `com.gurps.ficha.visual/com.gurps.ficha.MainActivity` via `am start -W` (Status: ok).
6. [x] Confirmar ausência de `pracego` e pacote base via `am start -W` (Error type 3).
7. [x] Adicionar manual curto do Modo Alvo (<=200 caracteres) com abertura automática na primeira ativação e preferência persistida.
8. [x] Refinar regra de compatibilidade de perícia base para técnicas com texto ambíguo de pré-requisito (arma apropriada/ataque corpo a corpo).
9. [x] Instalar build `visual` no emulador após os ajustes de técnicas (task `:app:installVisualDebug`).
10. [x] Validar consistência final do lote de técnicas com auditoria e testes direcionados.
11. [x] Corrigir coerência do diálogo **Editar Técnica** para espelhar o diálogo de seleção/configuração:
    - mesmo bloco informativo (`Pré-requisito` e `Pré-definido`);
    - remoção do título redundante `Editar Técnica`;
    - filtro de `Perícia base` apenas para perícias compatíveis com o pré-requisito;
    - instalação da build `visual` no emulador após ajuste.

## Comandos de Verificação (mínimo)
- `./gradlew :app:testVisualDebugUnitTest --tests com.gurps.ficha.regras_prerequisitos.PreRequisitoParserTest`
- `./gradlew :app:testVisualDebugUnitTest --tests com.gurps.ficha.domain.magias.NexusArcanoModoAlvoAdapterTest --tests com.gurps.ficha.domain.magias.NexusArcanoLoteFCanonicScenarioTest`
- `./gradlew :app:testVisualDebugUnitTest --tests nexus.arcano.NexusArcanoEngineLoteAGlobalTest --tests nexus.arcano.NexusArcanoEngineLoteBGlobalTest`
