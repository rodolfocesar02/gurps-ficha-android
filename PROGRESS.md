# PROGRESS - GURPS Ficha Android

Atualizado em: 2026-03-04

## Estado Atual
- Branch: `main`
- Variantes: `visual` e `pracego`
- Build: estável (debug/release das duas variantes compilando)
- Foco atual: qualidade do `Modo Alvo` em pré-requisitos complexos (ex.: `Desejo`, `Convocar Demônio`, `Translocação`).

## Regras de Projeto (essenciais)
- Aptidão Mágica é pré-requisito global para magias (base nível 0).
- Parser: `ou` = alternativa, `e` = acumulação.
- Fallback nominal de pré-requisito: magia -> vantagem -> perícia.
- `Especial` e `#` = bypass da validação automática.
- `-`/traços/vazio/`???` = sem pré-requisito (deve liberar).
- PRACEGO: rótulos semânticos claros + mensagens curtas de bloqueio e recomendação.

## Feito
- `Modo Alvo` desacoplado da UI:
  - motor dedicado `MagiaTargetEngine`;
  - cálculo assíncrono no `FichaViewModel`;
  - estados de UI (`carregando`, `erro`, `aviso`, `ids`).
- Guardrails implementados (tempo/nós/profundidade).
- Cache implementado:
  - parse de pré-requisito;
  - resultado por contexto de alvo.
- Início da reescrita do planner:
  - novo `MagiaDependencyPlanner` com resolução transitiva;
  - prioridade de match nominal exato (evita falso match `Encantar` vs `Encantamento`);
  - tentativa com rollback para múltiplas escolas/candidatas.
- Testes e builds executados repetidamente nas duas variantes sem quebra.

## Não Feito (pendente)
- Fechar robustez total do planner para todos os casos com `N mágicas em K escolas diferentes`.
- Garantir que sempre apareça uma trilha útil quando existir caminho válido (evitar listas “secas”).
- Cobertura de testes dedicada ao planner para casos reais:
  - `Desejo`, `Convocar Demônio`, `Translocação`, `Teleporte`, `Encantar`, `Relâmpago`.
- Ajustar UX final do PRACEGO para narrar:
  - falta imediata,
  - próxima ação possível,
  - próxima etapa após adicionar.

## Próximos Passos (ordem)
1. Refinar heurística de escolha de escola/candidata no planner (priorizar escolas com magia básica destravável).
2. Adicionar testes automatizados de regressão para os 6 casos críticos.
3. Ajustar mensagem final de guia no card PRACEGO com ações imediatas.
4. Validar novamente com bateria completa (debug/release + unit tests em ambas variantes).

## Regra operacional
1. Rodar build/test das duas variantes.
2. Atualizar este `PROGRESS.md`.
3. Commit + push.
4. Gerar APKs quando solicitado.

## Atualização incremental (Etapa A)
- Heurística de escolas refinada no planner:
  - em `N magias em K escolas`, agora prioriza escolas com magia básica (sem pré-requisito) e com magia já aprendível no estado atual.
- Resultado esperado:
  - aumentar chance de recomendação imediata e reduzir listas vazias em alvos complexos.
- Pendência:
  - validar manualmente no fluxo real os casos `Desejo`, `Convocar Demônio` e `Translocação`.

## Atualização incremental (Etapa B - 2026-03-03)
- `Modo Alvo` desativado temporariamente na UI para estabilizar o fluxo geral de magias.
- `Ajuda por Voz` desativada temporariamente na variante `pracego`.
- `magias2versao.json` passou por limpeza de codificação e remoção de caracteres inválidos.
- Regras específicas ajustadas:
  - `transformar_objeto`: `Aptidão Mágica 2, Remodelar e 4 mágicas de Criar`.
  - `restauracao`: `Cura Profunda ou 2 entre Aliviar Paralisia e mágicas de Restaurar`.
  - `talisma`: pré-requisito simplificado para `Encantar`.
- Rolagem de magia:
  - para `Talismã`, o diálogo de custo de energia agora exige escolher uma magia do repertório antes de confirmar.
- Validação:
  - `:app:compileVisualDebugKotlin` OK
  - `:app:compilePracegoDebugKotlin` OK

## Atualização incremental (Etapa C - 2026-03-03)
- Padronização textual em `magias2versao.json`:
  - `Controle de Animais` -> `Controle de Animal`.
- Nova regra de sub-escola para magia da escola `Animais`:
  - opções fixas: `Criaturas da Terra`, `Criaturas do Ar`, `Criaturas do Mar`.
  - seleção obrigatória ao adicionar magia da escola `Animais`.
- `Controle de Animal`:
  - permite múltiplas instâncias;
  - bloqueia duplicata da mesma sub-escola;
  - aceita coexistência por sub-escola diferente (`Terra`, `Ar`, `Mar`).
- UI:
  - diálogo de configuração de magia agora usa seleção guiada (dropdown) para sub-escola de `Animais`.
  - aba de magias exibe especialização no título da magia: `Nome (Sub-escola)`.

## Atualização incremental (Etapa D - 2026-03-04)
- Escola `Animais` migrada para caminhos explícitos em catálogo:
  - remoção das versões genéricas: `controle_de_animal`, `convocar_animal`, `dominar_animal`, `falar_com_animais`.
  - criação de variantes por caminho: `_terra`, `_ar`, `_mar` para as quatro famílias acima.
- Pré-requisitos entre variantes de `Animais` alinhados por caminho:
  - `Convocar Animal (X)` -> `Controle de Animal (X)`;
  - `Dominar Animal (X)` -> `Controle de Animal (X)`;
  - `Falar com Animais (X)` -> `Convocar Animal (X)`.
- Runtime simplificado para `Animais`:
  - removida exigência de sub-escola em tempo de adição para qualquer magia da escola `Animais`;
  - diálogo mantém especialização apenas para casos ainda exigidos (`Elemental`, etc.).

## Atualização incremental (Etapa E - 2026-03-04)
- Regras especiais de magias ligadas a `Animais` atualizadas para caminhos distintos:
  - `cavalgar`: exige >= 1 `Controle de Animal` (qualquer caminho);
  - `controle_de_hibrido`: exige 2 caminhos distintos de `Controle de Animal` (`Ar/Terra/Mar`);
  - `passageiro_interno`: exige 2 caminhos distintos de `Controle de Animal` (`Ar/Terra/Mar`);
  - `repelir_animal`: exige >= 1 `Controle de Animal`.
- Compatibilidade mantida para fichas antigas:
  - reconhecimento de caminho por sufixo de id (`_ar`, `_terra`, `_mar`);
  - fallback por texto/especialização legado quando necessário.

## Atualização incremental (Etapa F - 2026-03-04)
- Modo Alvo reativado na UI:
  - `MODO_ALVO_HABILITADO = true`.
- Testabilidade do motor melhorada:
  - novo contrato `MagiaPlannerDataSource`;
  - `DataRepository` implementa o contrato;
  - `MagiaTargetEngine`/`MagiaDependencyPlanner` desacoplados para testes unitários.
- Testes unitários novos:
  - `MagiaTargetEngineAnimaisTest`: valida trilhas por caminho (`Ar/Terra/Mar`) sem vazamento.
  - `MagiaTargetEngineCatalogSweepTest`: varredura completa do catálogo no Modo Alvo.
- Sweep do catálogo:
  - total de magias testadas: `839`;
  - exceções/travas: `0`;
  - alvos sem aparecer na própria trilha: `0`;
  - relatório salvo em `app/build/reports/modo_alvo_sweep_report.txt`.
- Validação executada nas duas variantes:
  - `:app:compileVisualDebugKotlin` OK
  - `:app:compilePracegoDebugKotlin` OK
  - `:app:testVisualDebugUnitTest` OK
  - `:app:testPracegoDebugUnitTest` OK
