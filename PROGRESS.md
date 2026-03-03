# PROGRESS - GURPS Ficha Android

Atualizado em: 2026-03-03
Objetivo atual: consolidar regras de pré-requisito (Aptidão Mágica nível 0-base e Modo Alvo por família de nome) mantendo estabilidade de build.

## Atualização do Bloco (2026-03-03 - Modo Alvo com trilha de desbloqueio)
- Ajuste funcional do `Modo Alvo`:
  - saiu de lista ampla por relação textual para **lista ordenada por progressão**;
  - agora prioriza:
    - magia alvo;
    - pré-requisitos explícitos por nome;
    - trilha de magias possíveis por escola (desbloqueando próximas em sequência);
    - requisitos de “escolas diferentes” (caso de `Encantar`) com foco em escolas ainda faltantes.
- Casos foco cobertos:
  - `Relampago` (`6 magias de Ar`): lista sugerida por cadeia de desbloqueio da escola Ar;
  - `Encantar` (`1 magia em 10 escolas diferentes`): sugestões distribuídas por escolas para completar o requisito.
- Validação:
  - `:app:compileVisualDebugKotlin` OK;
  - `:app:installVisualDebug` OK;
  - execução da variante visual no emulador realizada.

## Atualização do Bloco (2026-03-03 - magias sem pré-requisito com dash/mojibake)
- Problema identificado:
  - parte do dataset está com `preRequisitos` em formato mojibake de travessão (`â€”`) em vez de `—`;
  - a validação estava tratando só `—` literal e bloqueava magia básica com mensagem `???`.
- Correção aplicada:
  - `DataRepository.validarPreRequisitosMagia` e `missingPreRequisitoReport` agora usam `isSemPreRequisitoRaw(...)`;
  - a regra considera como sem pré-requisito: vazio, `-`, `—`, `–`, `−`, variações mojibake (`â€”`, `â€“`, `âˆ’`) e `???`;
  - `preRequisitoRawNormalizado` passou a aplicar `fixMojibakeIfNeeded()` antes de validar.
- Varredura executada no JSON:
  - total identificado com marcador de sem pré-requisito: **29 magias**;
  - inclui casos citados: `purificar_o_ar` e `localizar_ar`.
- Validação técnica:
  - `:app:compileVisualDebugKotlin` OK;
  - `:app:testVisualDebugUnitTest` OK;
  - `:app:installVisualDebug` OK e abertura no emulador (Visual) realizada.

## Atualização do Bloco (2026-03-03 - migração de `magias2versao.json` e validação de link de pré-requisitos)
- Arquivo de dados de magias:
  - mantido o arquivo oficial `app/src/main/assets/magias2versao.json` (não foi criado arquivo paralelo);
  - correções diretas de termos quebrados no próprio JSON (ex.: `Relmpago` -> `Relampago`, `Projteis` -> `Projeteis`, `Furaco` -> `Furacao`, `mgicas` -> `magicas`);
  - confirmado que o dataset segue no formato estruturado por campo (`classe`, `escola`, `duracao`, `energia`, `tempoOperacao`, `preRequisitos`) e sem retorno ao formato monolítico em `texto`.
- Consistência de pré-requisitos (checagem de ligação):
  - teste de consistência executado para famílias/nomes após correção;
  - caso alvo validado: `relampago` com pré-requisito por escola (`6 mágicas do Ar`) e cadeia relacionada por nome (`Relampago`/`Relampagos`) preservada;
  - contagens observadas na validação: escola `Ar` com 49 magias e família `relampag` com 11 magias.
- Rotina operacional seguida:
  - salvar alterações de dados;
  - compilar/testar;
  - instalar e abrir variante visual no emulador para verificação funcional.

## Atualização do Bloco (2026-03-03 - normalização de schema de magia e acessibilidade da rolagem)
- Magias - schema/fallback:
  - reforçada normalização para manter metadados separados de `texto`;
  - quando `classe/escola/duração/energia/tempo de operação/pré-requisitos` vierem só em `texto`, o loader extrai para os campos corretos;
  - `texto` passa a ficar reservado para descrição residual (linhas que não são metadados).
- Magias - correção de nome:
  - adicionado reparo automático de nomes truncados no carregamento (ex.: `Relmpago` -> `Relampago`);
  - fallback seguro por `id` para casos suspeitos de truncamento no campo `nome`;
  - mesma correção aplicada em `preRequisitos` para reduzir falhas por tokens corrompidos.
- Rolagem PRACEGO:
  - rótulo semântico dos botões rápidos de atributo agora inclui valor:
    - formato: `Rolagem de ST X`, `Rolagem de IQ X`, etc.
- Validação:
  - `:app:compileVisualDebugKotlin` e `:app:compilePracegoDebugKotlin` OK;
  - `:app:testVisualDebugUnitTest` e `:app:testPracegoDebugUnitTest` OK.
- Nota para futuros agentes:
  - manter prioridade de leitura dos campos estruturados de magia; usar extração por `texto` apenas como compatibilidade legada.

## Atualização do Bloco (2026-03-03 - Aptidão Mágica 0-base e Modo Alvo por família)
- Aptidão Mágica (regra base):
  - cálculo de nível efetivo ajustado para base 0 (`nível interno - 1`) em validações de magia;
  - presença da vantagem continua habilitando acesso à aba/regras mágicas;
  - exibição de nível/custo na UI de Vantagens ajustada para refletir `nível 0 +5, +10 por nível`.
- Modo Alvo (magias):
  - reforçada a identificação de relacionadas por parser de pré-requisito;
  - adicionada detecção de famílias no **nome da magia** (ex.: `Ácido`, `Relâmpago`) com tolerância a singular/plural e acentuação;
  - mantida coexistência com relação por escola e por nomes explícitos.
- Verificação técnica:
  - `:app:compileVisualDebugKotlin` e `:app:compilePracegoDebugKotlin` executados com sucesso;
  - `:app:testVisualDebugUnitTest` e `:app:testPracegoDebugUnitTest` executados com sucesso.

## Atualização do Bloco (2026-03-02 - UX de Magias, técnicas e padronização de cards)
- Magias - usabilidade:
  - implementado `Modo Alvo` na seleção de magias para foco em uma magia objetivo;
  - adicionada listagem relacionada ao alvo (alvo + pré-requisitos textuais detectados);
  - cards de magia bloqueada agora mostram motivo curto: `Falta: <resumo>`.
- Magias - adição forçada:
  - removida a mecânica de segurar por 3 segundos;
  - adição forçada movida para o diálogo da magia;
  - clique em `Adição Forçada sem pré-requisito` abre popup de confirmação:
    - texto em caixa alta: `SEU MESTRE AUTORIZOU?`
    - botões: `SIM` e `NAO`.
- Técnicas - pré-requisito:
  - corrigida extração da âncora de limite (`pré-requisito+N`) para não bloquear falsamente;
  - perícias de combate corpo a corpo (ex.: `Espadas Curtas`) voltam a satisfazer técnicas como `Finta`.
- UI/consistência:
  - padronização de cards de seleção para o mesmo estilo da seleção de perícias em:
    - vantagens/desvantagens;
    - técnicas/perícias suplementares;
    - seletores de arma/escudo/armadura.
- Build e artefatos:
  - debug e release compilados com sucesso nas variantes `visual` e `pracego`;
  - APKs release atualizados em:
    - `app/build/outputs/apk/release_named/GURPS_VISUAL.apk`
    - `app/build/outputs/apk/release_named/GURPS_PRACEGO.apk`
- Controle de versão:
  - commit aplicado e enviado para `origin/main`:
    - `8f8572d` - melhorias de UX de magias, confirmação de adição forçada, padronização de cards e ajuste de pré-requisito de técnicas.

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
  - `8f8572d` Melhora UX de magias (modo alvo e bloqueio curto), move adição forçada para diálogo com confirmação, padroniza cards e ajusta pré-requisito de técnicas

## Próximas Entregas Imediatas
1. Validar fluxo completo do `Modo Alvo` em sessão real de criação de personagem.
2. Refinar `Modo Alvo` com trilha explícita (`alvo -> pré-requisito -> subpré-requisito`), se necessário.
3. Executar checklist final de regressão visual/PRACEGO antes do próximo release público.

## Regra de Trabalho
- Toda alteração deve fechar com:
  1. validação de build/test;
  2. atualização deste `PROGRESS.md`;
  3. geração dos artefatos quando aplicável.
