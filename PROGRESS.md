# PROGRESS - GURPS Ficha Android

Atualizado em: 2026-03-06

## Objetivo Atual
Garantir que o **Modo Alvo** aplique os pré-requisitos de magia com comportamento canônico GURPS, sem divergência entre telas, motor e catálogo.

## Diagnóstico Confirmado
1. Existem duas lógicas de validação em paralelo (rápida e hierárquica), com resultados diferentes em alguns casos.
2. O Modo Alvo consome `preRequisitos` bruto da `MagiaDefinicao` no adapter, sem garantir uso do texto canônico corrigido por override.
3. O runtime usa `app/src/main/assets/magias2versao.json`, que contém divergências de texto em relação às correções canônicas mapeadas no repositório.
4. O parser do motor (`NexusArcanoEngine`) é simplificado para alguns casos de `ou` e pode selecionar cadeia/regras numéricas fora da alternativa correta.
5. Auditoria global `NexusArcanoModoAlvoAuditoriaTodasMagiasTest` falha por OOM e não fecha diagnóstico completo de regressão.

## Plano de Ação (Lotes)

### Lote 1 - Fonte Única de Pré-Requisito Canônico
Status: `EM ANDAMENTO`

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
Status: `EM ANDAMENTO`

Passos:
1. [x] Definir um único contrato de validação para UI e Modo Alvo (mesma semântica de bloqueio por magia).
2. [x] Eliminar fallback conflitante no `FichaViewModel` entre validação rápida e validação principal.
3. [ ] Fechar auditoria de divergência residual entre mensagem de chave do modo alvo e bloqueio de adição na UI.
Arquivos:
- `app/src/main/java/com/gurps/ficha/viewmodel/FichaViewModel.kt`
- `app/src/main/java/com/gurps/ficha/data/DataRepository.kt`
- `app/src/main/java/com/gurps/ficha/domain/magias/NexusArcanoModoAlvoAdapter.kt`

Critério de aceite:
- Mesmo personagem + mesma magia => mesma resposta de bloqueio em lista, diálogo e modo alvo.

### Lote 4 - Correção Semântica do Parser do Motor (`ou`/alternativas)
Status: `EM ANDAMENTO`

Passos:
1. [x] Revisar `dependenciasNomeadasGrupos` para preservar corretamente alternativas compostas (`A ou B, C`).
2. [x] Associar seleção de dependências nomeadas ao parser por termo (vírgulas + `ou`), com fallback heurístico.
3. [ ] Cobrir casos de `outras escolas`, soma de atributos e requisitos nomeados simultâneos.
Arquivo: `motor modo alvo/src/NexusArcanoEngine.kt`

Critério de aceite:
- O motor não recomenda cadeia impossível nem ignora restrição de alternativa.

### Lote 5 - Ordem Canônica de Progressão
Status: `PENDENTE`

Passos:
1. Reforçar prioridade hard-first:
- cadeia nomeada obrigatória
- depois contagem de escolas
- depois alvo final
2. Garantir que recomendação lateral não fure bloqueio de cadeia.
Arquivo: `motor modo alvo/src/NexusArcanoEngine.kt`

Critério de aceite:
- Cenário `Encantar -> Pequeno Desejo -> Desejo` sempre respeitado antes de fechamento final.

### Lote 6 - Testes, Auditoria e Evidência
Status: `PENDENTE`

Passos:
1. Adicionar testes de regressão para:
- `nao pode ser/ter` (cego/cegueira, surdo/surdez)
- `(DX + IQ):30+`
- `outras escolas` vs `escolas diferentes`
- alternativa com `ou`
Arquivos:
- `app/src/test/java/com/gurps/ficha/regras_prerequisitos/PreRequisitoParserTest.kt`
- `app/src/test/java/nexus/arcano/*`
2. Ajustar auditoria global para não estourar memória (reduzir BFS/estado, limitar amostra ou particionar teste).
Arquivo: `app/src/test/java/nexus/arcano/NexusArcanoModoAlvoAuditoriaTodasMagiasTest.kt`
3. Gerar relatórios em `app/build/reports/` com resumo de sucesso/falha.

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
