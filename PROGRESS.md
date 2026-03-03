# PROGRESS - GURPS Ficha Android

Atualizado em: 2026-03-03

## Estado Atual
- Branch: `main`
- Variantes ativas: `visual` e `pracego`
- Build geral: estável (debug/release das duas variantes compilando)
- Problema aberto: `Modo Alvo` ainda falha em cenários com pré-requisitos encadeados/complexos (ex.: Desejo, Convocar Demônio, Translocação)

## Regras importantes do projeto (manter)
- Aptidão Mágica é pré-requisito global para magias (nível base 0).
- Parser deve respeitar `ou` como alternativa e `e` como acumulação.
- Fallback de pré-requisito nominal: magia -> vantagem -> perícia.
- Marcadores `Especial` e `#` mantêm bypass de validação automática.
- Marcadores de sem pré-requisito (`-`, variações de traço, vazio, `???`) devem liberar magia.
- PRACEGO exige rótulos semânticos claros e mensagens curtas de bloqueio/recomendação.

## O que já foi estruturado
- Motor de Modo Alvo separado do ViewModel: `MagiaTargetEngine`.
- Cálculo assíncrono no ViewModel com estados:
  - `modoAlvoCarregando`
  - `modoAlvoRelacionadosIds`
  - `modoAlvoErro`
  - `modoAlvoAviso`
- Guardrails ativos (tempo/nós/profundidade) para evitar travamentos.
- Cache de parse e cache de resultado por contexto.

## Decisão técnica nova (prioridade máxima)
- Não reescrever o sistema inteiro de magias.
- Reescrever **somente o planner do Modo Alvo** (núcleo de geração de trilha).
- Manter parser, validação base e UI atuais.

## Plano para o próximo agente

### Etapa A - Novo Planner de Dependências (substituir núcleo atual)
Objetivo:
- Montar trilha completa com dependências transitivas (A -> B -> C -> básicas), sem cortar requisitos intermediários.

Entregáveis:
- Novo componente de planejamento (ex.: `MagiaDependencyPlanner`) usado por `MagiaTargetEngine`.
- Resolução de requisitos por tipo:
  - nomes explícitos de magia,
  - `N mágicas de escola X`,
  - `N mágicas em K escolas diferentes`,
  - combinações com `ou`/`e`.

Critério de aceite:
- Para um alvo bloqueado, sempre haver “próximas ações possíveis” quando existir caminho válido.

### Etapa B - Estratégia de priorização
Objetivo:
- Ordenar trilha por menor custo de desbloqueio (menos passos primeiro).

Entregáveis:
- Heurística de prioridade com pontuação por distância de desbloqueio.
- Separar na resposta:
  - `proximas_imediatas` (já adicionáveis)
  - `trilha_planejada` (ordem de desbloqueio)

### Etapa C - Guardrails mais inteligentes
Objetivo:
- Evitar falso-positivo de “trilha parcial” em casos comuns.

Entregáveis:
- Ajustar limites com orçamento progressivo.
- Só retornar parcial quando realmente exceder busca útil.

### Etapa D - Testes críticos obrigatórios
Casos mínimos:
- `Relâmpago`
- `Teleporte`
- `Translocação`
- `Convocar Demônio`
- `Desejo`
- `Encantar`

Critério de aceite:
- Testes unitários e integração do planner cobrindo encadeamento e quantificadores.

### Etapa E - PRACEGO no fluxo final
Objetivo:
- Garantir que o usuário cego receba orientação acionável.

Entregáveis:
- Texto curto sempre com:
  - o que falta agora,
  - qual magia pode adicionar agora,
  - qual próxima etapa após isso.

## Checklist de entrega (obrigatório)
1. Rodar build/test nas duas variantes.
2. Atualizar este `PROGRESS.md`.
3. Commit + push.
4. Gerar APKs quando solicitado.

## Andamento novo (2026-03-03 - Reescrita do planner de Modo Alvo)
- Iniciado novo núcleo de planejamento: `MagiaDependencyPlanner`.
- `MagiaTargetEngine` passou a usar o planner novo para montar trilha transitiva.
- Objetivo desta troca:
  - resolver pré-requisitos em cadeia (dependência da dependência);
  - melhorar casos com `N mágicas em K escolas diferentes`;
  - reduzir lista vazia/incompleta em alvos complexos.
- Status: implementação inicial concluída e compilando; validar comportamento real dos casos críticos.
