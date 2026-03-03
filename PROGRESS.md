# PROGRESS - GURPS Ficha Android

Atualizado em: 2026-03-03

## Estado Atual
- Branch: `main`
- Variantes: `visual` e `pracego`
- Versão: `versionCode 2`, `versionName 1.1`
- Situação crítica aberta: travamento no `Modo Alvo` com magias de pré-requisito complexo (relato de crash repetido em teste manual).

## Diagnóstico resumido
- O cálculo de trilha do `Modo Alvo` está acoplado ao ViewModel/UI e pode escalar demais em cenários complexos.
- Sintoma provável: sobrecarga de processamento durante recomputações (queda de responsividade e encerramento do app).
- Decisão técnica aprovada: separar o motor de magias do motor geral da ficha.

## LOTE DE REFATORAÇÃO (para próximos agentes)

### Etapa 1 - Separação arquitetural (início imediato)
Objetivo:
- Extrair regras de sugestão/trilha de magias para um motor dedicado (`MagicEngine`), desacoplado da tela.

Entregáveis:
- Novo componente de domínio/dados para cálculo de `Modo Alvo`.
- ViewModel apenas orquestra entrada/saída (sem lógica pesada interna).
- Cobertura mínima de compilação e regressão básica.

Critério de aceite:
- Nenhum comportamento funcional removido.
- Código de `Modo Alvo` não fica mais concentrado no `FichaViewModel`.

### Etapa 2 - Execução assíncrona e estabilidade
Objetivo:
- Tirar cálculo de trilha da thread de UI.

Entregáveis:
- Cálculo em `Dispatchers.Default`.
- Estados explícitos para UI: `idle`, `loading`, `ready`, `error`.
- Debounce/cancelamento de recomputação quando usuário altera filtros/alvo rapidamente.

Critério de aceite:
- Tela continua responsiva enquanto calcula recomendações.

### Etapa 3 - Guardrails de segurança
Objetivo:
- Evitar explosão combinatória.

Entregáveis:
- Limites configuráveis: profundidade, nós explorados e tempo máximo por execução.
- Fallback seguro quando exceder limite (mensagem curta e trilha parcial útil).

Critério de aceite:
- Sem crash em cenários de pré-requisito profundo.

### Etapa 4 - Cache e desempenho
Objetivo:
- Reaproveitar resultados e reduzir custo de parsing.

Entregáveis:
- Cache do parse de pré-requisitos por magia.
- Cache de resultados por chave de contexto (alvo + conjunto de magias conhecidas + AM/atributos relevantes).

Critério de aceite:
- Tempo de resposta perceptivelmente menor em reabertura de alvo já consultado.

### Etapa 5 - Qualidade e acessibilidade PRACEGO
Objetivo:
- Tornar o fluxo de descoberta de pré-requisitos mais intuitivo e robusto para usuário cego.

Entregáveis:
- Rótulos e mensagens curtas consistentes para estado de bloqueio/desbloqueio.
- Ajustes no assistente guiado por voz V1 para narrar progresso do alvo sem redundância.
- Casos de teste automatizados do `Modo Alvo` (ex.: `Relâmpago`, `Encantar`, casos com escolas diferentes).

Critério de aceite:
- Fluxo validado manualmente no PRACEGO sem perda de contexto do alvo.

## Releases
- Pasta padrão de saída:
  - `app/build/outputs/apk/release_named/`
- Convenção atual:
  - `GURPS_VISUAL.apk`
  - `GURPS_PRACEGO.apk`
  - versões de backup estável nomeadas com sufixo `_ESTAVEL_ANTERIOR`.

## Regra operacional
Toda entrega deve fechar com:
1. build/test;
2. atualização deste `PROGRESS.md`;
3. commit/push;
4. geração de APKs quando solicitado.

## Andamento do Lote
- Etapa 1 iniciada e aplicada parcialmente:
  - lógica de trilha do `Modo Alvo` extraída para `MagiaTargetEngine`;
  - `FichaViewModel` agora delega cálculo de relacionados para o motor dedicado.
- Validação da Etapa 1 (parcial):
  - `compileVisualDebugKotlin` OK;
  - `compilePracegoDebugKotlin` OK;
  - `testVisualDebugUnitTest` OK.

## Andamento do lote (2026-03-03 - Etapa 2)
- `Modo Alvo` migrado para cálculo assíncrono no `FichaViewModel`.
- Estados adicionados para UI:
  - `modoAlvoCarregando`
  - `modoAlvoRelacionadosIds`
  - `modoAlvoErro`
- Requisições agora têm cancelamento/substituição quando o contexto muda.
- A tela de magias passa a consumir resultado pronto e exibe status de cálculo.
- Validação:
  - `compileVisualDebugKotlin` OK
  - `compilePracegoDebugKotlin` OK
  - `testVisualDebugUnitTest` OK

## Andamento do lote (2026-03-03 - Etapa 3)
- Guardrails adicionados ao `MagiaTargetEngine`:
  - limite de tempo por execução;
  - limite de nós analisados;
  - limite de profundidade de busca.
- Ao atingir limite, o motor retorna trilha parcial segura (sem travar).
- `FichaViewModel` agora expõe `modoAlvoAviso` para informar fallback ao usuário.
- UI de magias mostra aviso de trilha parcial quando guardrail é acionado.
- Validação:
  - `compileVisualDebugKotlin` OK
  - `compilePracegoDebugKotlin` OK
  - `testVisualDebugUnitTest` OK

## Andamento do lote (2026-03-03 - Etapa 4)
- Cache implementado no motor de magias:
  - cache de parse de pré-requisito (`PreRequisitoParser`) com LRU interno;
  - cache de resultado de `Modo Alvo` por chave de contexto (alvo + assinatura de estado).
- Integração no `FichaViewModel`:
  - envio de chave contextual para reaproveitar resultado sem recomputação.
- Resultado esperado:
  - melhora de resposta ao repetir busca do mesmo alvo no mesmo estado de personagem.
- Validação:
  - `compileVisualDebugKotlin` OK
  - `compilePracegoDebugKotlin` OK
  - `testVisualDebugUnitTest` OK

## Andamento do lote (2026-03-03 - Etapa 5)
- PRACEGO (Modo Alvo) refinado:
  - card da magia recomendada marca `Recomendada`;
  - rótulo semântico da recomendação explicita prioridade para avanço do alvo.
- Testes adicionados para casos críticos de parser:
  - estilo `Relâmpago` (`AM1, 6 mágicas do Ar`);
  - estilo `Encantar` (`1 mágica em dez escolas diferentes`).
- Bateria de validação executada (completa):
  - `compileVisualDebugKotlin` OK
  - `compilePracegoDebugKotlin` OK
  - `testVisualDebugUnitTest` OK
  - `testPracegoDebugUnitTest` OK
  - `assembleVisualDebug` OK
  - `assemblePracegoDebug` OK
  - `assembleVisualRelease` OK
  - `assemblePracegoRelease` OK
