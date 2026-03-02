# PROGRESS - GURPS Ficha Android

Atualizado em: 2026-03-02
Objetivo atual: estabilizar release das variantes VISUAL e PRACEGO e organizar entrega de build.

## Atualização do Bloco (2026-03-02 - ícones de abas e release adiado)
- Decisão de entrega:
  - geração de novo `release` adiada temporariamente para conclusão de testes funcionais.
- Ícones das abas substituídos pelos arquivos fornecidos em `C:\Users\Rodolfo\Desktop\bachup ficha gurps\Icones`:
  - `Geral.png` -> `tab_geral.png`
  - `Traços.png` -> `tab_tracos.png`
  - `Pericias.png` -> `tab_pericias.png`
  - `Técnicas.png` -> `tab_tecnicas.png`
  - `Magia.png` -> `tab_magia.png`
  - `Equipamentos.png` -> `tab_equipamentos.png`
  - `Defesas.png` -> `tab_defesas.png`
  - `Rolagem.png` -> `tab_rolagem.png`
- Navegação inferior atualizada para usar `painterResource` com os novos ícones personalizados em todas as abas.
- Auditoria final de rótulos PRACEGO após as últimas mudanças:
  - verificação manual das áreas alteradas (barra de abas, diálogo de magias, diálogo de técnicas);
  - sem falta crítica nova de `contentDescription` nesses fluxos;
  - ícones das abas mantidos com rótulo semântico `Aba <nome>` na variante PRACEGO.
- Tentativa de lint de acessibilidade:
  - `:app:lintPracegoDebug` executado;
  - falha por crash interno do lint/Compose (`AutoboxingStateCreationDetector`), não por erro funcional de regra de rótulo.

## Atualização do Bloco (2026-03-02 - ícone e estabilidade de gesto)
- Ícone do app atualizado com a arte `app/src/main/assets/icon-app.jpg`:
  - recurso aplicado em `app/src/main/res/drawable/icon_app.jpg`;
  - manifesto atualizado para usar `@drawable/icon_app` em `android:icon` e `android:roundIcon`.
- Correção de bug no fluxo de adicionar magia:
  - ação de segurar por 3 segundos movida do card inteiro para o texto do nome da magia;
  - swipe vertical na lista não dispara mais abertura indevida do diálogo de magia;
  - acessibilidade PRACEGO ajustada para instruir explicitamente “no nome”.
- Verificação de corrupção de encoding executada:
  - varredura em `app/src/main/java`, `app/src/main/res` e `PROGRESS.md`;
  - corrigidas strings corrompidas de UI em `DialogsTecnicas.kt` (textos de técnica/perícia/pré-requisito);
  - mantidos tokens de compatibilidade legada em normalizadores/parsers para não quebrar dados antigos.

## Atualização do Bloco (2026-03-02)
- Build e testes validados para as duas variantes:
  - `:app:compileVisualDebugKotlin`
  - `:app:compilePracegoDebugKotlin`
  - `:app:testVisualDebugUnitTest`
  - `:app:testPracegoDebugUnitTest`
  - `:app:assembleVisualDebug`
  - `:app:assemblePracegoDebug`
- APKs debug nomeados por variante/data/commit foram gerados em:
  - `app/build/outputs/apk/named/gurps-ficha-visual-debug-20260302-433dae7.apk`
  - `app/build/outputs/apk/named/gurps-ficha-pracego-debug-20260302-433dae7.apk`
- Build release ajustado para assinatura automática com `debug keystore` enquanto não houver keystore de produção configurada.

## Status Atual
- Variantes ativas:
  - `visual`
  - `pracego`
- Versão de app atual:
  - `versionCode = 2`
  - `versionName = 1.1`
- Último commit funcional registrado:
  - `433dae7` feat(magias): add specialization-based special cases and forced add on 3s hold

## Próximas Entregas Imediatas
1. Aplicar novo ícone do aplicativo nas duas variantes.
2. Gerar APKs `release` assinados e nomeados por variante/data/commit.
3. Validar instalação manual dos APKs de release em dispositivo real.

## Regra de Trabalho
- Toda alteração deve fechar com:
  1. validação de build/test;
  2. atualização deste `PROGRESS.md`;
  3. geração dos artefatos quando aplicável.
