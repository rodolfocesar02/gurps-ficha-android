# PROGRESS - GURPS Ficha Android

Atualizado em: 2026-03-01
Objetivo atual: evoluir o app a partir da base ja estavel em producao.

## Atualizacao do Bloco (2026-03-01)
- Lote 14 (import/export JSON) avancou em base compartilhada `app/src/main`:
  - Novo envelope de interoperabilidade com metadados: `schema`, `schemaVersion`, `exportedAtUtc`, `appVersion`, `uiVariant`, `character`.
  - Exportacao no menu ajustada para dois formatos:
    - `JSON Compativel` (legado, para versoes antigas do app);
    - `JSON Versionado` (novo envelope de interoperabilidade).
  - Importacao aceita tanto envelope novo quanto JSON legado sem envelope (compatibilidade retroativa).
  - Validacao de compatibilidade de versao no import:
    - versao futura -> erro claro de versao incompativel;
    - arquivo invalido/corrompido -> erro padronizado.
  - Testes unitarios adicionados para parser/import-export (`PersonagemInteropTest`).
- Validacao executada nas duas variantes:
  - `./gradlew.bat :app:compileVisualDebugKotlin :app:compilePracegoDebugKotlin :app:testVisualDebugUnitTest :app:testPracegoDebugUnitTest --no-daemon`
  - `./gradlew.bat :app:assembleVisualDebug :app:assemblePracegoDebug --no-daemon`
  - Resultado: `BUILD SUCCESSFUL`.

## Atualizacao Final do Bloco (2026-02-28)
    - `1d92bde` chore(deploy): retry railway queue
  - Observacao operacional:
    - aplicacao final depende do Railway sair de estado `QUEUED` para `ACTIVE` no deploy mais recente.

## Handoff Rapido (VISUAL + PRACEGO)
Resumo do bloco finalizado em 2026-02-28 para o proximo agente:

- VISUAL:
  - Mantido estavel, sem mudanca de layout neste bloco.
  - Fluxo de compilacao validado junto com PRACEGO.
- PRACEGO:
  - Aba Geral reorganizada em fluxo linear com cards separados para atributos primarios e secundarios.
  - Cada atributo com botoes `-` e `+` rotulados para leitor de tela (`diminuir`/`aumentar` + nome do atributo).
  - Aba Rolagem reorganizada em cards lineares separados (sem swipe), com controles acessiveis e rotulados.
  - PV/PF mantidos como informativos (sem botoes), conforme regra definida.
  - Dialogos de Tracos/Pericias/Magias com botoes de ajuste centralizados e rotulos de acessibilidade (`-`/`+`, editar, etc).
- Validacao executada:
  - `./gradlew.bat :app:compileVisualDebugKotlin :app:compilePracegoDebugKotlin --no-daemon`
  - Resultado: `BUILD SUCCESSFUL`.
- Regra operacional reforcada:
  - Sempre explicitar no inicio do bloco de UI qual variante esta sendo alterada: `VISUAL` ou `PRACEGO`.

## Handoff (proximo agente)
Contexto imediato para continuidade sem retrabalho:
- Ultimo foco funcional: `Lote 14` (importacao/exportacao de ficha em JSON) foi iniciado e esta em `EM ANDAMENTO`.
- Fluxo ja implementado:
  - Menu com `Exportar Ficha (JSON)` e `Importar Ficha (JSON)`.
  - Export via `CreateDocument` escrevendo `personagem.toJson()`.
  - Import via `OpenDocument` lendo JSON e aplicando `Personagem.fromJson` via ViewModel.
  - Feedback de importacao por dialogo (sucesso/erro).
- Ultimos ajustes de UX entregues antes do handoff:
  - Ordem de abas alterada (`Equip.` antes de `Defesas`) e renome `Combate` -> `Defesas`.
  - Limpeza visual de `grupo`/`Tabela de Armas...` na selecao de armas e nos cards equipados.
  - Ajustes de swipe em atributos/pericias/magias e exibicao condicional de `mod` na rolagem.
- Estado de integracao:
  - Repositorio local sincronizado com GitHub (`origin/main` atualizado no ultimo push conhecido).

## Metodo de Trabalho Atual
Padrao combinado para as proximas execucoes:
1. Implementar por blocos pequenos e validaveis (evitar mudanca grande em um passo unico).
2. Sempre validar com:
   - `./gradlew.bat :app:compileDebugKotlin testDebugUnitTest --no-daemon`
   - `./gradlew.bat :app:assembleDebug --no-daemon` quando houver impacto de entrega.
3. Fechar bloco com commit claro; quando solicitado, fazer tambem `push` para GitHub.
4. Atualizar este `PROGRESS.md` a cada bloco concluido, com status real do lote.
5. Preservar compatibilidade de dados salvos (fichas antigas) em qualquer alteracao de modelo.
6. Manter limpeza visual sem alterar regra matematica sem aprovacao explicita.



## Lotes Ativos (Nova Ordem)

### Lote 1 - Revisao de JSON e catalogos (prioridade alta)
Escopo:
Andamento:
- Catalogo de armaduras consolidado em `armaduras.v2.json` (72 itens), com conversao/validacao automatica via scripts `convert_armaduras_v2.py` e `validate_armaduras_v2.py`.
- Runtime simplificado: removido asset legado `armaduras.v1.json` e removidos assets legados de armas `raw/review_flags` (app usa apenas arquivos `normalized`).
- Observacoes integradas por tipo:
  - Armaduras: observacoes vinculadas ao item e exibidas tambem no card equipado.
  - Armas corpo a corpo: suporte a `Aparar` + observacoes [1..12].
  - Armas distancia: observacoes [1..8].
  - Armas de fogo: observacoes por categoria de tabela (Pistolas/MM, Rifles/Espingardas, Ultra-Tech, Pesadas), com subfiltro quando `Armas de Fogo` estiver ativo.
- Robustez de observacoes de armas: suporte aos formatos `[1]` e `[1, 2, 3]` e fallback por nome/tipo/dano para itens antigos equipados.
- Padrao de layout adotado:
  - Botoes `Adicionar ...` separados dos cards.
  - Sem icone `+` em cabecalho de card.
  - Cards de itens ocultos quando vazios e exibidos somente apos existir ao menos 1 item.
  - Aba Pericias sem card de cabecalho; resumo compacto de pontos no rodape (total de pericias + pontos gastos).
  - Aba Magias no mesmo padrao: botao `Adicionar Magia` separado, lista em card proprio e resumo no rodape (quantidade + pontos gastos).
  - Itens de Magias exibidos em cards individuais (mesmo comportamento visual adotado em Pericias).
 
    - Padrao visual alinhado com Pericias/Magias: botoes `Adicionar ...` separados dos cards (sem `+` no cabecalho) para Vantagens, Desvantagens, Qualidades e Peculiaridades.
    - Linha de filtros com rolagem horizontal para garantir acesso em telas menores.
  - Dialogos de Tracos:
    - Seletor de Autocontrole (adicionar/editar desvantagem) com rolagem horizontal para evitar sobreposicao de opcoes.

Checklist de manutencao do catalogo de armaduras (obrigatorio em futuras edicoes):
1. Regenerar JSON:
   - `python scripts/convert_armaduras_v2.py --input "<xlsx>" --output "app/src/main/assets/armaduras.v2.json"`
2. Validar consistencia:
   - `python scripts/validate_armaduras_v2.py --input "app/src/main/assets/armaduras.v2.json"`
3. Validacao Android:
   - `./gradlew.bat :app:compileDebugKotlin testDebugUnitTest --no-daemon`
   - `./gradlew.bat :app:assembleDebug --no-daemon`
4. Commit com mensagem clara citando o passo do lote.

### Lote 2 - Ficha clicavel na aba Rolagem (ex-Lote 8)
Escopo:
Levantamento do que ainda precisa ser feitof no Lote 2:

- Revisar pequenos ajustes visuais residuais (espacamento/alinhamento) apos testes em aparelhos menores.
Status: `EM ANDAMENTO`
Andamento:
- Checkpoint de salvamento criado em 2026-02-27 (ajustes visuais base aplicados em Geral, Traços, Magias, Equipamentos e Combate).
- Padronizacao inicial de layout: botoes `Adicionar ...` em largura total e espacamentos harmonizados entre abas.
- Proximo passo imediato: compactacao visual para telas <= 360dp e refinamento de rodapes/dialogos.
- Commit de salvamento: `VERSÃO DEFINITIVA)` (2026-02-27).
- Padronizacao ampla de UI concluida:
  - novo arquivo base `UiStandards.kt` com componentes reutilizaveis (`StandardTabColumn`, `PrimaryActionButton`, `SummaryFooterCard`);
  - abas migradas para o padrao unico (Traços, Pericias, Magias, Equipamentos, Combate e Notas);
  - nome de arquivo padronizado: `TagMagias.kt` -> `TabMagias.kt`;
  - validacao executada com sucesso: `:app:compileDebugKotlin testDebugUnitTest` e `:app:assembleDebug`.
- Padronizacao complementar concluida:
  - novo container reutilizavel de dialogo fullscreen em `DialogStandards.kt`;
  - dialogos de selecao migrados para o mesmo padrao visual (Pericias, Magias, Vantagens e Desvantagens);
  - backend `discord-roll-api` com respostas de erro e validacao de API key padronizadas (helpers reutilizaveis em `server.js`);
  - validacao executada com sucesso: `node --check discord-roll-api/src/server.js`, `:app:compileDebugKotlin testDebugUnitTest` e `:app:assembleDebug`.

### Lote 3 - Login / Autenticacao
Escopo:
- Definir modelo de autenticacao (simples e adequado para grupo pequeno).
- Proteger envio de rolagens com identidade de usuario.
- Preparar base para evolucao de seguranca futura sem quebrar UX.
Criterio de pronto:
- Usuario autenticado no app.
- Backend aceitando apenas requisicoes autenticadas.
Status: `PENDENTE`

### Lote 4 - Observabilidade e suporte
Escopo:
- Melhorar logs de erro no backend para diagnostico rapido.
- Padronizar mensagens de erro no app e backend.
- Registrar evento minimo para suporte (sem expor dados sensiveis).
Criterio de pronto:
- Falhas mais faceis de identificar e corrigir.
Status: `PENDENTE`

### Lote 5 - UX e desempenho
Escopo:
- Polir experiencia da aba Rolagem e lista de canais.
- Ajustar feedback visual em carregamento, sucesso e falha.
- Revisar pontos de lentidao da UI em aparelhos antigos.
Criterio de pronto:
- Fluxo rapido e claro em uso continuo de mesa.
Status: `PENDENTE`

### Lote 6 - Preparacao de release
Escopo:
- Revisar configuracoes de seguranca para release.
- Revisar permissao e trafego de rede por ambiente.
- Fechar checklist final para distribuicao estavel.
Criterio de pronto:
- Build release com configuracao segura e previsivel.
Status: `PENDENTE`

Plano de implementacao (micro-passos):

Riscos e mitigacao:
- Risco medio de regressao visual em telas pequenas:
  - mitigar com teste manual em pelo menos 2 tamanhos de tela.
- Risco medio em resumo de pontos:
  - mitigar conferindo totais antes/depois em ficha de teste.
- Risco medio de truncamento de texto antigo >200:
  - mitigar definindo comportamento explicito (bloquear novas edicoes acima do limite, sem apagar automaticamente conteudo salvo).

Criterio de pronto:
- Aba Geral reorganizada conforme escopo, sem quebra de calculos.
- Historico e persistencia funcionando.
- Layout validado em uso real (scroll, campos, resumo).
Status: `EM ANDAMENTO`



### Lote 7 - Seguranca de rede e configuracao por ambiente
Escopo:
- Remover `cleartext` global e permitir HTTP somente em debug/local.
- Exigir HTTPS em release para chamadas de backend.
- Revisar fluxo de chave de API no cliente para reduzir exposicao de segredo estatico.

Plano de implementacao (micro-passos):
1. Mapear configuracao atual de rede no app (Manifest, BuildConfig e cliente HTTP).
2. Criar politica por ambiente:
   - debug: permitir endpoint local de desenvolvimento;
   - release: bloquear cleartext e aceitar apenas HTTPS.
3. Implementar `network_security_config` separado por build type (debug/release) e ajustar Manifest para usar a configuracao correta.
4. Ajustar validacao de `DISCORD_ROLL_API_BASE_URL` no app:
   - release: falhar cedo se URL nao for HTTPS;
   - debug: permitir URL local HTTP explicitamente.
5. Revisar uso de `DISCORD_ROLL_API_KEY` no cliente:
   - remover dependencia de segredo estatico para cenario de release quando possivel;
   - manter fallback de dev controlado para debug/local.
6. Atualizar mensagens de erro de configuracao de rede para facilitar diagnostico (sem vazar dado sensivel).
7. Validar em execucao real:
   - debug com backend local HTTP;
   - release apontando para HTTPS;
   - tentativa de HTTP em release deve falhar de forma previsivel.
8. Rodar validacao padrao + teste manual de rolagem/listagem de canais.

Riscos e mitigacao:
- Risco medio de quebrar fluxo de desenvolvimento local:
  - mitigar permitindo HTTP apenas no build debug com regra explicita.
- Risco alto de regressao em release por configuracao incorreta de URL:
  - mitigar com validacao de schema (`https://`) antes da chamada de rede e teste manual em build release.
- Risco medio de erro de autenticacao apos ajuste de chave:
  - mitigar com rollout gradual e mensagens claras para diferenciar erro de chave, rede e ambiente.

Criterio de pronto:
- Build release sem trafego HTTP aberto globalmente.
- Rede separada por ambiente (debug/release) com politica explicita.
- Risco de vazamento de segredo reduzido.
- Fluxo de desenvolvimento local preservado em debug.
Status: `PENDENTE`

### Lote 8 - Refatoracao de arquitetura (ViewModel e Repository)
Escopo:
- Quebrar `FichaViewModel` por responsabilidades (tracos, pericias, magias, equipamentos, combate, rolagem).
- Quebrar `DataRepository` em componentes menores por dominio de catalogo.
- Reduzir acoplamento UI-regra-negocio com camadas mais claras.
Criterio de pronto:
- Arquivos grandes divididos em modulos/coordenadores menores.
- Fluxos principais continuam funcionando sem regressao funcional.
- Leitura/manutencao de codigo simplificada.
Status: `PENDENTE`

### Lote 9 - Performance e confiabilidade de dados locais
Escopo:
- Evitar carga pesada em getters sincronos acessados pela UI.
- Precarregar catalogos em `Dispatchers.IO` com cache estavel para filtros.
- Revisar autosave para manter responsividade em fichas grandes.
Criterio de pronto:
- Menos risco de travamento/intermitencia ao abrir telas e filtrar listas.
- Fluxos de busca e filtros mais fluidos em aparelhos modestos.
Status: `PENDENTE`

### Lote 10 - Rede, logs e observabilidade
Escopo:
- Padronizar tratamento de erro e remover `printStackTrace` em runtime.
- Melhorar cliente HTTP (timeouts, parse de erro, codigo mais testavel; considerar migracao para OkHttp/Retrofit).
- Melhorar mensagens de falha para suporte tecnico sem expor dados sensiveis.
Criterio de pronto:
- Erros de rede e catalogo com diagnostico previsivel.
- Menos falhas silenciosas e retrabalho de suporte.
Status: `PENDENTE`

### Lote 11 - Evolucao de banco, testes e higiene de repo
Escopo:
- Preparar evolucao do Room (schema exportado, estrategia de migration).
- Ampliar cobertura de testes para ViewModel, parser de catalogos e fluxo de rolagem.
- Limpar versionamento de artefatos pesados em `snapshots/` (ex.: APKs) e reforcar higiene de repositorio.
- Introduzir `RuleEngine Module` para centralizar calculos de GURPS fora da UI, facilitando testes unitarios e reduzindo acoplamento.
Criterio de pronto:
- Base pronta para evolucao de schema sem sustos.
- Maior confianca em mudancas futuras por testes.
- Historico de git mais limpo e focado em codigo.
Status: `PENDENTE`

### Lote 12 - Telemetria de instalacao e notificacao de novos usuarios
Escopo:
- Registrar instalacao/primeiro uso do app com identificador tecnico (sem expor dados sensiveis).
- Enviar evento para backend para acompanhamento de base instalada.
- Disparar notificacao administrativa quando um novo dispositivo/app ativo for detectado.
- Definir politica minima de privacidade e retenção para esses eventos.
Criterio de pronto:
- Backend com endpoint de registro de instalacao/primeiro uso.
- App enviando evento de forma resiliente (retry simples, sem travar UI).
- Canal de notificacao administrativa funcionando para novos registros.
Status: `PENDENTE`

### Lote 13 - Controle de acesso remoto (bloqueio por app/dispositivo)
Escopo:
- Definir modelo de identificacao do cliente (ex.: instalacao + token assinado pelo backend).
- Criar status de acesso no backend (ativo/bloqueado).
- Validar no app, em startup e antes de chamadas sensiveis, se o acesso esta permitido.
- Exibir mensagem clara no app quando o acesso estiver bloqueado.
Criterio de pronto:
- Bloqueio remoto aplicavel sem rebuild do app.
- Requisicoes protegidas recusando clientes bloqueados com erro padronizado.
- Fluxo de desbloqueio previsivel e auditavel.
Status: `PENDENTE`

### Lote 14 - Importacao e exportacao de ficha de personagem
Escopo:
- Exportar ficha para arquivo (JSON) com metadados de versao.
- Importar ficha de arquivo com validacao e tratamento de compatibilidade.
- Permitir compartilhamento entre jogadores (arquivo local / intent de compartilhamento).
- Prever estrategia de migracao de schema para import de fichas antigas.
Criterio de pronto:
- Usuario consegue exportar e importar ficha sem perda de dados principais.
- Erros de importacao com mensagens claras (arquivo invalido/versao incompativel).
- Testes unitarios para parser e compatibilidade basica entre versoes.
Status: `EM ANDAMENTO`
Andamento:
- Menu principal com acoes de `Exportar Ficha (JSON)` e `Importar Ficha (JSON)`.
- Exportacao implementada via seletor de arquivo Android (`CreateDocument`) com dois formatos:
  - `JSON Compativel` (legado);
  - `JSON Versionado` (envelope com metadados via `PersonagemInterop.exportarJson`).
- Importacao implementada via seletor de arquivo Android (`OpenDocument`) com parser versionado e feedback de sucesso/erro em dialogo.
- Compatibilidade preservada para JSON legado sem envelope e validacao de versao futura no import.
- Testes unitarios adicionados para parser e compatibilidade basica entre versoes (`PersonagemInteropTest`).

## Fora de Escopo (agora)
- Nao alterar regras matematicas de custo/pontos durante reforma visual da aba Geral.
- Nao remover persistencia legada de fichas sem migracao planejada.
- Nao alterar fluxo de integracao Discord ja validado em producao, exceto correcoes pontuais de bug.

## Referencia Rapida de Erros (Discord)
- `unauthorized` (401): chave de acesso invalida ou ausente.
- `service_not_configured` (500): backend sem variavel obrigatoria.
- `channel_id_missing` (400): canal nao definido para envio.
- `discord_send_failed` (502): Discord recusou/indisponivel no envio.
- `discord_channels_failed` (502): falha ao listar canais no Discord.
- `falha de internet/timeout ao conectar no servidor`: erro de rede sem resposta HTTP.

## Validacao Padrao
- Compilar + testes unitarios:
  - `./gradlew.bat :app:compileDebugKotlin testDebugUnitTest --no-daemon`
- Gerar APK debug:
  - `./gradlew.bat :app:assembleDebug --no-daemon`
  - saida: `app/build/outputs/apk/debug/app-debug.apk`

## Regra de Trabalho
- Fechar cada lote com:
  1. validacao padrao;
  2. commit com mensagem clara;
  3. atualizacao deste `PROGRESS.md`.

## Regra Global - Encoding e Acentuacao (Projeto Todo)
- Todos os arquivos de codigo/texto devem permanecer em `UTF-8`.
- Antes de commit, validar rapidamente se surgiram textos corrompidos:
  - `rg -n "�|Ã|Â" app/src/main/java app/src/main/res PROGRESS.md README.md`
- Se aparecer caractere corrompido em string de UI:
  1. corrigir o texto na origem;
  2. garantir que o arquivo foi salvo em UTF-8;
  3. recompilar (`:app:compileDebugKotlin`) e validar em tela.
- Excecoes permitidas (nao sao bug de UI):
  - strings de compatibilidade/normalizacao de legado (ex.: parser e limpeza de mojibake em dados antigos).

## REGRA GLOBAL - UI EM DUAS VARIANTES (OBRIGATORIO)
- BASE UNICA DE REGRA DE NEGOCIO: TODA REGRA DE DADOS/VIEWMODEL/DOMINIO FICA EM `app/src/main`.
- UI VISUAL: ALTERACOES DE LAYOUT DA VERSAO PADRAO DEVEM SER FEITAS NA VARIANTE `VISUAL`.
- UI PRA CEGO: ALTERACOES DE LAYOUT DE ACESSIBILIDADE DEVEM SER FEITAS NA VARIANTE `PRACEGO`.
- SEMPRE CONFIRMAR EXPLICITAMENTE, NO INICIO DE CADA BLOCO DE AJUSTE VISUAL, QUAL VARIANTE ESTA SENDO ALTERADA: `VISUAL` OU `PRACEGO`.
- SE O PEDIDO DO USUARIO NAO ESPECIFICAR A VARIANTE, CONFIRMAR ANTES DE EDITAR LAYOUT.
