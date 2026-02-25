# PROGRESS - GURPS Ficha Android

Atualizado em: 2026-02-25
Objetivo atual: evoluir o app a partir da base ja estavel em producao.

## Proximo Passo Imediato
- Executar `Lote 7 - micro-passo 1`: ajustar estrutura da aba Geral sem alterar regras de calculo.

## Estado Atual (Consolidado)
- Integracao Discord funcionando em producao (Railway + app Android).
- Envio de rolagens funcionando com selecao de canal no app.
- Cache de canais no backend (30 min).
- Historico com status de envio, erro claro e botao de reenviar.
- Build Kotlin, testes unitarios e APK debug funcionando.

## Lotes Ativos (Nova Ordem)

### Lote 1 - Revisao de JSON e catalogos (prioridade alta)
Escopo:
- Revisar arquivos JSON de catalogo, com foco principal em armaduras.
- Melhorar a consolidacao da lista de armaduras (normalizacao, agrupamento e consistencia).
- Melhorar sistema de busca e tags para facilitar encontrar itens.
Criterio de pronto:
- Catalogo de armaduras consistente e facil de manter.
- Busca por tags mais previsivel e util para uso real.
Status: `PENDENTE`

### Lote 2 - Ficha clicavel na aba Rolagem (ex-Lote 8)
Escopo:
- Tornar campos principais da ficha clicaveis para disparar rolagem direta.
- Mapear contexto do clique (ex.: "Ataque Espada Curta", "Defesa Esquiva", "DX").
- Registrar historico de rolagens na aba Rolagem com contexto correto.
Criterio de pronto:
- Fluxo "clicou no campo -> rolou -> apareceu no historico" funcionando nos cenarios principais.
Status: `PENDENTE`

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
