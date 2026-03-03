# PROGRESS - GURPS Ficha Android

Atualizado em: 2026-03-03

## Estado Atual
- Branch: `main`
- Variantes ativas: `visual` e `pracego`
- Versão atual: `versionCode 2`, `versionName 1.1`
- Build/tests base: OK (`compileVisualDebugKotlin`, `compilePracegoDebugKotlin`, `testVisualDebugUnitTest`, `testPracegoDebugUnitTest`)

## O que está consolidado
- Regras de pré-requisito de magias com suporte a:
  - `Aptidão Mágica` considerando base nível 0.
  - Operadores `ou`/`e` em parsing principal.
  - Marcadores de sem pré-requisito (`-`, travessões e variações mojibake, `???`, vazio) tratados como liberados.
  - Busca de pré-requisito por prioridade: magia -> vantagem -> perícia (com exceções de domínio já aplicadas).
- Modo Alvo de magias ativo com:
  - foco na magia objetivo;
  - motivo curto de bloqueio no card;
  - lista relacionada por cadeia de desbloqueio (nome/escola/requisitos agregados) em evolução.
- UX de adição forçada:
  - removido gesto de segurar no card;
  - fluxo movido para diálogo com confirmação (`SEU MESTRE AUTORIZOU?`).
- Acessibilidade PRACEGO:
  - rótulos de navegação e elementos críticos revisados;
  - rolagens com rótulo incluindo atributo e valor.

## Pontos em aberto (prioridade)
1. Refinar Modo Alvo para sempre sugerir trilha mínima de desbloqueio para casos agregados (ex.: `N magias de escola`, `N escolas diferentes`, família por nome como "Ácido"/"Relâmpago").
2. Finalizar V1 de assistente guiado offline na tela de magias (voz + feedback curto) com validação em fluxo real PRACEGO.
3. Revisão final de regressão de links de pré-requisito após V1.

## Nota de release
- Release assinada continua disponível quando solicitada, com saída padronizada em:
  - `app/build/outputs/apk/release_named/GURPS_VISUAL.apk`
  - `app/build/outputs/apk/release_named/GURPS_PRACEGO.apk`

## Regra operacional
- Toda entrega deve fechar com:
  1. build/test;
  2. atualização deste `PROGRESS.md`;
  3. commit/push;
  4. geração de APKs quando solicitado.
