# PROGRESS - GURPS Ficha Android

Atualizado em: 2026-03-03

## Estado Atual
- Branch: `main`
- Variantes ativas: `visual` e `pracego`
- Versão atual: `versionCode 2`, `versionName 1.1`
- Build/tests base: OK (`compileVisualDebugKotlin`, `compilePracegoDebugKotlin`, `testVisualDebugUnitTest`, `testPracegoDebugUnitTest`)

## Entregas consolidadas
- Regras de pré-requisito de magias com suporte a:
  - `Aptidão Mágica` base nível 0.
  - Operadores `ou`/`e` no parsing principal.
  - Marcadores de sem pré-requisito (`-`, travessões e mojibake, `???`, vazio) tratados como liberados.
  - Prioridade de resolução: magia -> vantagem -> perícia.
- Modo Alvo de magias ativo com:
  - foco em magia objetivo;
  - motivo curto de bloqueio no card;
  - lista relacionada por cadeia de desbloqueio (nome/escola/requisitos agregados).
- UX de adição forçada:
  - removido gesto de segurar no card;
  - ação no diálogo com confirmação (`SEU MESTRE AUTORIZOU?`).
- Acessibilidade PRACEGO:
  - revisão dos rótulos críticos;
  - rolagens com rótulo incluindo atributo e valor.

## Assistente Guiado por Voz (V1) - Implementado
- Escopo V1 (offline) na seleção de magias:
  - botão `Ajuda por Voz` (PRACEGO);
  - reconhecimento via `SpeechRecognizer` (`RecognizerIntent`, preferência offline);
  - retorno por `TextToSpeech` em português;
  - comandos suportados:
    - `quero <nome da magia>` / `objetivo <nome da magia>`
    - `próxima`
    - `adicionar sugerida`
    - `por que bloqueada`
  - feedback curto em tela e por voz para orientar a trilha de desbloqueio.
- Permissão adicionada:
  - `android.permission.RECORD_AUDIO`
- Limite atual do V1:
  - depende da disponibilidade local do reconhecimento offline no dispositivo.

## Próximas prioridades
1. Refinar a trilha mínima do Modo Alvo para todos os casos agregados (`N escolas`, `N magias de escola`, família por nome).
2. Melhorar a robustez dos comandos de voz com aliases e confirmação contextual.
3. Rodada final de regressão de pré-requisitos + acessibilidade PRACEGO.

## Releases
- Artefatos release nomeados:
  - `app/build/outputs/apk/release_named/GURPS_VISUAL.apk`
  - `app/build/outputs/apk/release_named/GURPS_PRACEGO.apk`

## Regra operacional
- Toda entrega deve fechar com:
  1. build/test;
  2. atualização deste `PROGRESS.md`;
  3. commit/push;
  4. geração de APKs quando solicitado.
