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

Falta:
1. Nenhuma pendência aberta nos lotes 1-6 deste ciclo.

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

## Regras Operacionais
1. Sempre editar primeiro a fonte canônica de pré-requisito antes de mexer na UI.
2. Não aceitar correção só por "funcionou no caso X"; incluir teste de regressão.
3. Quando houver divergência entre JSON e regra canônica, corrigir ambos ou documentar exceção explícita.
4. Cada lote só fecha com:
- testes passando
- relatório em `app/build/reports/`
- atualização deste `PROGRESS.md`
5. Commit por lote com mensagem objetiva (`lote-N: ...`).

## Comandos de Verificação (mínimo)
- `./gradlew :app:testVisualDebugUnitTest --tests com.gurps.ficha.regras_prerequisitos.PreRequisitoParserTest`
- `./gradlew :app:testVisualDebugUnitTest --tests com.gurps.ficha.domain.magias.NexusArcanoModoAlvoAdapterTest --tests com.gurps.ficha.domain.magias.NexusArcanoLoteFCanonicScenarioTest`
- `./gradlew :app:testVisualDebugUnitTest --tests nexus.arcano.NexusArcanoEngineLoteAGlobalTest --tests nexus.arcano.NexusArcanoEngineLoteBGlobalTest`
