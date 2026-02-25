# PROGRESS - GURPS Ficha Android

Atualizado em: 2026-02-25
Objetivo atual: evoluir o app a partir da base ja estavel em producao.

## Proximo Passo Imediato
- Iniciar `Lote 2 - passo 1`: mapear campos principais da ficha para gatilho de rolagem direta (somente mapeamento/sem alterar regras de rolagem).

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
Status: `CONCLUIDO`
Andamento:
- Passo 1 concluido (2026-02-25): novo arquivo `armaduras.v2.json` gerado a partir de `tabelas_de_Armaduras2.xlsx`, com 72 itens base, componentes/add-ons estruturados, `notes` completas (NT baixo/alto), tags e metadados de RD (dividida/flexivel/frontal).
- Passo 1 concluido (2026-02-25): app passou a carregar `armaduras.v2.json`, incluindo busca por `tags` e `observacoesDetalhadas`.
- Passo 2 concluido (2026-02-25): UI de armaduras atualizada com filtro dedicado por `tag` (alem de NT/local), contador de resultados e exibicao de tags na lista para selecao mais previsivel.
- Passo 3 concluido (2026-02-25): consolidacao final do catalogo de armaduras com validacao automatica (sem duplicidade de `id`, sem duplicidade de nome normalizado, sem locais fora do canonico e sem texto quebrado/mojibake).
- Passo 3 concluido (2026-02-25): `scripts/convert_armaduras_v2.py` reforcado para reparar texto quebrado na conversao e `scripts/validate_armaduras_v2.py` criado para checagem de consistencia do JSON antes de commit.
- Passo 4 concluido (2026-02-25): UX final do seletor de armaduras ajustada (ordem de filtros Local -> NT -> Tag, texto de orientacao de uso e acao "Limpar filtros"), fechando o lote de catalogo.
- Ajuste rapido (2026-02-25): removidas tags redundantes de `local/local_exp/nt/tipo` na linha de tags e no filtro de Tag da armadura, evitando repeticao visual.
- Ajuste tecnico (2026-02-25): limpeza de assets legados de armas (`raw` e `review_flags`), mantendo apenas os arquivos `normalized` usados pelo app em runtime.
- Ajuste funcional (2026-02-25): armas corpo a corpo passam a carregar/exibir `Aparar` e observacoes da tabela, inclusive na arma ja selecionada/equipada.
- Ajuste tecnico (2026-02-25): removido fallback de `armaduras.v1.json` e removido asset legado `armaduras.v1.json`; runtime usa somente `armaduras.v2.json`.
- Ajuste funcional (2026-02-25): observacoes de armadura passam a aparecer tambem na lista de armaduras equipadas.
- Ajuste funcional (2026-02-25): observacoes de armas de fogo revisadas por categoria de tabela (Pistolas/MM, Rifles/Espingardas, Ultra-Tech e Pesadas), com subfiltro dedicado quando `Armas de Fogo` esta selecionado.
- Ajuste funcional (2026-02-25): em selecao de arma, observacoes de armas de fogo ficam ocultas por padrao e aparecem no contexto correto de filtro.
- Ajuste corretivo (2026-02-25): leitura de referencias de observacao de arma corrigida para aceitar formatos `[1]` e `[1, 2, 3]`.
- Ajuste corretivo (2026-02-25): fallback por nome/tipo/dano da arma para preencher observacoes em itens antigos ja equipados (quando o ID de catalogo nao estiver confiavel).

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
