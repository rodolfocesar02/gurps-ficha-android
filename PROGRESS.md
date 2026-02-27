# PROGRESS - GURPS Ficha Android

Atualizado em: 2026-02-27
Objetivo atual: evoluir o app a partir da base ja estavel em producao.



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
Levantamento do que ainda precisa ser feito no Lote 2:

- Revisar pequenos ajustes visuais residuais (espacamento/alinhamento) apos testes em aparelhos menores.
Status: `EM ANDAMENTO`
Andamento:
- Checkpoint de salvamento criado em 2026-02-27 (ajustes visuais base aplicados em Geral, Traços, Magias, Equipamentos e Combate).
- Padronizacao inicial de layout: botoes `Adicionar ...` em largura total e espacamentos harmonizados entre abas.
- Proximo passo imediato: compactacao visual para telas <= 360dp e refinamento de rodapes/dialogos.
- Commit de salvamento: `VERSÃO DEFINITIVA)` (2026-02-27).

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
