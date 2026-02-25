# PROGRESS - GURPS Ficha Android

Atualizado em: 2026-02-24
Objetivo: manter somente o status operacional atual, sem histórico extenso.

## Status Geral
- Build e testes unitários compilam com sucesso no estado atual.
- Aba `Equipamentos` já integra catálogos de:
  - armas corpo a corpo,
  - armas à distância,
  - armas de fogo,
  - escudos,
  - armaduras.
- Regras já ativas na ficha:
  - filtro por ST mínimo para seleção de armas/escudos,
  - soma automática de DB de escudo no Bloqueio,
  - cálculo de dano exibido conforme ST do personagem para armas aplicáveis,
  - custo/peso consolidados na aba de equipamentos.

## Itens Fechados (Não Mexer Agora)
- Lote 3 (Perícias) fechado e validado.
- Lote 4 (Magia) fechado e validado.
- Lote 5 (Combate de ficha) fechado no escopo de ficha (sem jogabilidade tática).
- Fontes estáveis em uso:
  - `app/src/main/assets/vantagens.v3.json`
  - `app/src/main/assets/desvantagens.v2.json`
- Pipelines ativos de equipamentos:
  - `armas_corpo_a_corpo.v1.*`
  - `armas_distancia.v1.*`
  - `armas_fogo.v1.*`
  - `escudos.v1.json`
  - `armaduras.v1.json`
- Auto-save de recuperação implementado para reduzir perda de trabalho.

## Itens Que Precisam Revisão
- Revisão manual dos itens com flags:
  - `app/src/main/assets/armas_corpo_a_corpo.v1.review_flags.json`
  - `app/src/main/assets/armas_distancia.v1.review_flags.json`
  - `app/src/main/assets/armas_fogo.v1.review_flags.json`
- Normalização de texto OCR (acentuação/mojibake residual em alguns nomes/descrições).
- Conferência de custo/peso em linhas com formatos especiais (`var.`, `desp.`, múltiplos modos).
- Teste funcional contínuo em dispositivo real (estabilidade e recuperação de estado).

## Próximos Passos
1. Revisar e limpar `reviewFlags` das tabelas já importadas.
2. Congelar versão dos JSONs de equipamentos após revisão manual.
3. Só depois avançar para novo lote de regras, mantendo passos curtos.

## Validação Padrão
- Compilar + testes unitários:
  - `./gradlew.bat :app:compileDebugKotlin testDebugUnitTest --no-daemon`
- Gerar APK debug:
  - `./gradlew.bat :app:assembleDebug --no-daemon`
  - saída: `app/build/outputs/apk/debug/app-debug.apk`

## Pontos de Salvamento
- `snapshots/checkpoint-20260224-151219`
- `snapshots/checkpoint-20260224-193129`
- `snapshots/checkpoint-20260224-202718`

## Decisão de Escopo Mantida
- Situações de jogabilidade de combate (manobra/postura/múltiplas defesas por turno etc.) permanecem fora da criação/manutenção da ficha nesta fase.
