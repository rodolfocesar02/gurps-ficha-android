# PROGRESS - GURPS Ficha Android

Atualizado em: 2026-03-03

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
