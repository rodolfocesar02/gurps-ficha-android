# PROGRESS - GURPS Ficha Android

Atualizado em: 2026-02-27
Objetivo atual: evoluir o app a partir da base ja estavel em producao.

## Proximo Passo Imediato
- `Lote 2 - passo 5`: validar fluxo completo na Aba Rolagem ("clicou -> rolou -> historico") para Atributos, Ataque, Dano, Defesas, Pericias e Magias.
- `Lote 7`: consolidar refinos finais de layout/espacamento na Aba Geral e Aba Pericias apos testes em telas pequenas.

## Estado Atual (Consolidado)
- Integracao Discord funcionando em producao (Railway + app Android).
- Envio de rolagens funcionando com selecao de canal no app.
- Cache de canais no backend (30 min).
- Historico com status de envio, erro claro e botao de reenviar.
- Build Kotlin, testes unitarios e APK debug funcionando.
- Aba Tracos com bloco novo de `Qualidades` (max 5), com +1 ponto por item e persistencia em ficha salva.
- Filtro de Pericias atualizado com `VON` visivel em telas menores e no mesmo padrao visual dos filtros de Equipamentos.
- Ajuste de layout em dialogos de Tracos: opcoes de Autocontrole com rolagem horizontal para nao encavalar.
- Aba Combate concluida no padrao visual atual, com configuracao de Esquiva/Apara/Bloqueio e edicao de bonus sem alterar regras de calculo.

## Lotes Ativos (Nova Ordem)

### Lote 1 - Revisao de JSON e catalogos (prioridade alta)
Escopo:
- Revisar arquivos JSON de catalogo, com foco principal em armaduras.
- Melhorar a consolidacao da lista de armaduras (normalizacao, agrupamento e consistencia).
- Melhorar sistema de busca e tags para facilitar encontrar itens.
Criterio de pronto:
- Catalogo de armaduras consistente e facil de manter.
- Busca por tags mais previsivel e util para uso real.
Status: `CONCLUIDO`
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
  - Aba Tracos atualizada:
    - Novo botao/fluxo `Adicionar Qualidade`.
    - Regras de Qualidades: maximo 5, sem duplicata, +1 pt cada.
    - Resumo de Pontos da aba Geral exibe `Qualidades` separadamente.
    - Padrao visual alinhado com Pericias/Magias: botoes `Adicionar ...` separados dos cards (sem `+` no cabecalho) para Vantagens, Desvantagens, Qualidades e Peculiaridades.
    - Cards de Traços agora ficam ocultos quando vazios; rodape unico com total de tracos e pontos gastos somados.
  - Aba Pericias:
    - Filtro por atributo inclui `VON` com visual no padrao de Equipamentos (texto clicavel).
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
- Mapear contexto do clique (ex.: "Ataque Espada Curta", "Defesa Esquiva", "DX").
- Tornar campos DENTRO DA ABA ROLAGEM clicaveis para disparar rolagem direta.
- Melhorar layout da aba Rolagem para uso em mesa.
- Registrar historico de rolagens na aba Rolagem com contexto correto.
Criterio de pronto:
- Fluxo "clicou no campo -> rolou -> apareceu no historico" funcionando nos cenarios principais.
Status: `EM ANDAMENTO`
Plano de implementacao (passo a passo com teste):
1. [x] Mapear contexto do clique (ex.: "Ataque Espada Curta", "Defesa Esquiva", "DX").
2. [x] Tornar campos DENTRO DA ABA ROLAGEM clicaveis para disparar rolagem direta.
3. [x] Criar layout melhor da aba Rolagem para uso.
4. [x] Registrar rolagens na aba Rolagem com contexto correto.
5. [ ] Validar fluxo completo: "clicou no campo -> rolou -> apareceu no historico" nos cenarios principais.
Matriz de mapeamento (Passo 1):
- `Atributo`:
  - Itens: `ST`, `DX`, `IQ`, `HT`, `PER`, `VON`.
  - Contexto final: `"ST"`, `"DX"`, `"IQ"`, `"HT"`, `"PER"`, `"VON"`.
  - Alvo: `personagem.getAtributo(sigla)`.
  - Fonte atual: `Personagem.getAtributo(...)`.
- `Ataque`:
  - Itens: pericias da ficha (priorizar pericias de combate no layout, manter fallback para todas).
  - Contexto final: `"Ataque <nome da pericia>"` (com especializacao quando houver).
  - Alvo: `pericia.calcularNivel(personagem)`.
  - Fonte atual: `personagem.pericias`.
- `Defesa`:
  - Itens: `Esquiva`, `Apara`, `Bloqueio` quando disponiveis.
  - Contexto final: `"Defesa Esquiva"`, `"Defesa Apara"`, `"Defesa Bloqueio"`.
  - Alvo: `activeDefense.finalValue`.
  - Fonte atual: `viewModel.defesasAtivasVisiveis`.
Regras de seguranca para implementacao:
1. Nao navegar para outra aba.
2. Clique e rolagem acontecem somente dentro de `TabRolagem`.
3. Contexto enviado para payload/historico deve ser unico e padronizado.
4. Se item nao tiver alvo valido (ex.: defesa indisponivel), bloquear rolagem e mostrar estado visual desabilitado.
Proposta de organizacao da aba Rolagem (base para Passo 3):
1. Bloco `Teste Rapido por Atributo` (grade com 6 botoes clicaveis).
2. Bloco `Ataques` (lista clicavel com nome + nivel).
3. Bloco `Defesas` (chips/botoes de Esquiva/Apara/Bloqueio).
4. Bloco `Ajustes da Rolagem` (modificador atual e botoes +/-).
5. Bloco `Historico da Sessao` (resultado local + status de envio).
Andamento:
- 2026-02-26: Passo 1 concluido na `TabRolagem` com mapeamento explicito de contexto/alvo por tipo:
  - Atributos -> contexto sigla (`ST`, `DX`, `IQ`, `HT`, `PER`, `VON`) e alvo por `getAtributo`.
  - Ataques -> contexto padronizado `Ataque <nome/especializacao>` e alvo por nivel calculado.
  - Defesas -> contexto padronizado `Defesa <nome>` e alvo por valor final de defesa ativa.
  - Chaves de selecao de ataque normalizadas para evitar ambiguidade entre pericias repetidas.
  - Validacao executada: `:app:compileDebugKotlin` e `testDebugUnitTest`.
- 2026-02-26: Passo 2 iniciado na `TabRolagem` (parcial):
  - Bloco rapido de atributos implementado com clique direto no valor para rolar (`ST`, `DX`, `IQ`, `HT`, `VON`, `PER`).
  - Swipe vertical por atributo para ajustar modificador local por campo (intervalo `-20..+20`).
  - Painel lateral com `PV/PF` e refinamento visual da aba para uso de mesa.
  - Pendente no passo 2: concluir elementos clicaveis diretos para `Ataques` e `Defesas` no layout jogavel.
- 2026-02-27: Ajuste de regra de rolagem concluido:
  - `modificador` passou a ser aplicado no valor do teste (nao mais no total dos dados), com margem coerente para GURPS.
- 2026-02-27: Bloco `Ataque e Dano` implementado e iterado para uso rapido:
  - Removido card legado `Rolagem (MVP)` da Aba Rolagem.
  - Novo fluxo de combate com botao `Configurar seu Ataque e Dano`.
  - Card `Ataque`: exibe pericia de combate e `NH` clicavel para rolar ataque 3d6.
  - Card `Dano`: exibe arma/fonte e linhas de dano clicaveis individualmente (suporte a multiplas entradas, ex.: `1d+4 corte/1d-2+3 perf`).
  - Fallback sem arma habilitado com dano baseado em `ST`.
  - Swipe vertical no bloco para ajustar `mod de ataque` com foco em uso de mesa.
  - Layout compactado com dois cards lado a lado (Ataque e Dano), textos centralizados e menor espacamento vertical.
  - Pendente no lote: bloco clicavel de `Defesas` no mesmo padrao rapido.
- 2026-02-27: Avancos de layout e fluxo na Aba Rolagem:
  - Bloco `Defesas` implementado com 3 cards lado a lado (`Esquiva`, `Apara`, `Bloqueio`), cada um com clique de rolagem e swipe de modificador proprio.
  - `Ataque` e `Dano` mantidos lado a lado com altura simetrica e mesma cor de card do bloco de atributos.
  - `mod` exibido dentro dos cards de `Ataque`, `Dano` e `Defesas`; removido texto geral de mod de ataque fora dos cards.
  - Botao unico de configuracao foi dividido em dois botoes: `Ataque` e `Dano` com dialogs separados.
  - Bloco `Pericias` adicionado com rolagem por `NH`, swipe de modificador por item e dialog full-screen.
  - Bloco `Magias` adicionado (visivel apenas com `Aptidao Magica`), com mesmo padrao de `Pericias` e rolagem por `NH`.
  - Dialogs de `Pericias`/`Magias` na Rolagem passaram a fechar automaticamente ao clicar em `NH`.

Levantamento do que foi feito no Lote 2:
- Rolagem rapida por Atributos, Ataque, Dano, Defesas, Pericias e Magias implementada na mesma aba.
- Historico local com status de envio e reenvio mantido apos os novos blocos.
- Fluxo de mesa ficou mais compacto e adaptativo para telas pequenas.

Levantamento do que ainda precisa ser feito no Lote 2:
- Executar validacao manual fim-a-fim dos cenarios principais (passo 5).
- Revisar pequenos ajustes visuais residuais (espacamento/alinhamento) apos testes em aparelhos menores.

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

### Lote 7 - Reforma visual da aba Geral (planejado)
Escopo:
- Reduzir em ~20% a area de "Informacoes Basicas" para abrir espaco para os blocos abaixo.
- Unificar em um unico card:
  - "Atributos Primarios"
  - "Atributos Secundarios"
  - "Caracteristicas Derivadas"
  com separadores visuais suaves.
- Em "Atributos Secundarios", remover textos explicativos entre parenteses (somente rotulo limpo).
- Reestruturar "Resumo de Pontos" para:
  - "Atributos" (primarios + secundarios juntos),
  - "Vantagens",
  - "Desvantagens",
  - "Qualidades e Peculiaridades",
  - "Pericias",
  - "Magias",
  - "Total Gastos".
- Trazer conteudo da antiga aba Notas para a aba Geral:
  - "Nota Geral"
  - "Historia" (max 200 caracteres)
  - "Aparencia" (max 200 caracteres)

Plano de implementacao (micro-passos):
1. Ajustar estrutura de layout da aba Geral (sem mexer em logica de calculo).
2. Unificar os 3 blocos de atributos em um card com divisorias.
3. Limpar rotulos de atributos secundarios (remover textos em parenteses).
4. Refatorar visual do "Resumo de Pontos" com nova composicao.
5. Mover "Notas" para "Nota Geral" na aba Geral.
6. Aplicar limite de 200 caracteres em Historia/Aparencia.
7. Validar compatibilidade com fichas salvas antigas (sem perda silenciosa de dados).
8. Rodar validacao padrao + teste manual em tela pequena/grande.

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

Andamento:
- 2026-02-27: Primeira rodada de reforma aplicada:
  - Aba `Notas` removida da navegacao; campos textuais migrados para a Aba Geral.
  - Bloco de anotacoes na Geral com limites aplicados:
    - `Campanha` (200),
    - `Historia` (1000),
    - `Aparencia` (1000),
    - `Notas` (1000).
  - Controles de atributos primarios e secundarios migrados de botoes +/- para swipe vertical.
  - Ajustes de densidade visual na Geral (menos padding/espacamento entre cards e elementos).
  - Blocos `Anotacoes` e `Resumo de Pontos` convertidos para botoes com dialogs (`Anotacoes` full-screen, `Resumo` adaptativo).

Levantamento do que ainda precisa ser feito no Lote 7:
- Avaliar se unificacao dos 3 blocos de atributos em um unico card sera mantida como requisito final.
- Refinar layout final da Geral em telas pequenas e grandes com teste manual.
- Fechar consolidacao de rotulos e composicao final do resumo de pontos conforme criterio de pronto.

### Lote 8 - Seguranca de rede e configuracao por ambiente
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

### Lote 9 - Refatoracao de arquitetura (ViewModel e Repository)
Escopo:
- Quebrar `FichaViewModel` por responsabilidades (tracos, pericias, magias, equipamentos, combate, rolagem).
- Quebrar `DataRepository` em componentes menores por dominio de catalogo.
- Reduzir acoplamento UI-regra-negocio com camadas mais claras.
Criterio de pronto:
- Arquivos grandes divididos em modulos/coordenadores menores.
- Fluxos principais continuam funcionando sem regressao funcional.
- Leitura/manutencao de codigo simplificada.
Status: `PENDENTE`

### Lote 10 - Performance e confiabilidade de dados locais
Escopo:
- Evitar carga pesada em getters sincronos acessados pela UI.
- Precarregar catalogos em `Dispatchers.IO` com cache estavel para filtros.
- Revisar autosave para manter responsividade em fichas grandes.
Criterio de pronto:
- Menos risco de travamento/intermitencia ao abrir telas e filtrar listas.
- Fluxos de busca e filtros mais fluidos em aparelhos modestos.
Status: `PENDENTE`

### Lote 11 - Rede, logs e observabilidade
Escopo:
- Padronizar tratamento de erro e remover `printStackTrace` em runtime.
- Melhorar cliente HTTP (timeouts, parse de erro, codigo mais testavel; considerar migracao para OkHttp/Retrofit).
- Melhorar mensagens de falha para suporte tecnico sem expor dados sensiveis.
Criterio de pronto:
- Erros de rede e catalogo com diagnostico previsivel.
- Menos falhas silenciosas e retrabalho de suporte.
Status: `PENDENTE`

### Lote 12 - Evolucao de banco, testes e higiene de repo
Escopo:
- Preparar evolucao do Room (schema exportado, estrategia de migration).
- Ampliar cobertura de testes para ViewModel, parser de catalogos e fluxo de rolagem.
- Limpar versionamento de artefatos pesados em `snapshots/` (ex.: APKs) e reforcar higiene de repositorio.
Criterio de pronto:
- Base pronta para evolucao de schema sem sustos.
- Maior confianca em mudancas futuras por testes.
- Historico de git mais limpo e focado em codigo.
Status: `PENDENTE`

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
