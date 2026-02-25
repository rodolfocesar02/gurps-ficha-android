# PROGRESS - GURPS Ficha Android

Atualizado em: 2026-02-25  
Objetivo: consolidar implementação atual, reduzir riscos e seguir em lotes curtos com salvamento ao fim de cada lote.

## Status Atual Consolidado
- Build Kotlin, testes unitários e geração de APK debug estão funcionando.
- Aba Equipamentos já possui integração de:
  - armas corpo a corpo,
  - armas à distância,
  - armas de fogo,
  - escudos,
  - armaduras.
- Regras já ativas:
  - filtro por ST mínimo para seleção,
  - DB de escudo somado no Bloqueio,
  - dano de arma calculado com ST quando aplicável,
  - custo/peso total consolidados.

## Regra de Trabalho (Obrigatória)
- Todo lote concluído deve terminar com:
  1. validação padrão completa;
  2. ponto de salvamento no Git (commit/checkpoint) com mensagem clara do lote;
  3. atualização deste `PROGRESS.md`.

## Cuidado de Codificação (UTF-8)
- Problema já observado em produção: textos com mojibake (`MÃ¡scara`, `CalÃ§as`, `OxigÃªnio`).
- Regra preventiva:
  1. manter scripts e JSONs sempre em UTF-8;
  2. evitar salvar arquivos de dados em ANSI/Windows-1252;
  3. antes de fechar lote de dados, revisar ocorrências suspeitas (`Ã`, `Â`, `â`, `�`);
  4. manter normalização defensiva no carregamento de catálogo (`DataRepository.sanitized`).

## Plano de Lotes (Próximos Passos)

### Lote 1 - Correção crítica de armaduras compostas
Escopo:
- Corrigir adição de `traje completo + cabeça` para não dividir custo/peso base indevidamente.
- Garantir que os conjuntos compostos usem valores próprios por parte (base e componente).
Critério de pronto:
- Teste funcional no app com os 5 conjuntos especiais aprovado.
- Custo/peso final batendo com tabela.
Status: `CONCLUIDO (2026-02-25)`

### Lote 2 - Higiene de dados de armaduras (parser + JSON)
Escopo:
- Corrigir parser de dinheiro para não converter `$1.300` em `1.3`.
- Corrigir campos OCR inválidos em armaduras (ex.: `rdRaw` com data).
- Regenerar `armaduras.v1.json` com consistência de custo/peso/RD.
Critério de pronto:
- Sem linhas críticas inválidas no JSON.
- Valores monetários de milhar corretos.
Status: `CONCLUIDO (2026-02-25)`

### Lote 3 - Revisão de reviewFlags (armas)
Escopo:
- Revisar e limpar itens com `reviewFlags` em:
  - `armas_corpo_a_corpo.v1.review_flags.json`
  - `armas_fogo.v1.review_flags.json`
- Manter `armas_distancia` como referência já limpa.
Critério de pronto:
- Redução máxima dos `reviewFlags` sem quebrar normalização.
Status: `CONCLUIDO (2026-02-25)`

### Lote 4 - Ajuste de layout da aba Equipamentos (UX)
Escopo:
- Reposicionar resumo de Equipamentos para o final da aba (rodapé).
- Reduzir tamanho visual do card resumo e tipografia.
- Cards de Armas: após adicionar, mostrar somente nome + dano.
Critério de pronto:
- Layout validado em aparelho antigo e atual, sem poluição de informação.
Status: `CONCLUIDO (2026-02-25)`

### Lote 5 - Robustez e manutenção
Escopo:
- Reduzir fragilidade de parsing por texto em `notas` (ex.: RD por regex).
- Melhorar tratamento de erro em carga de catálogos (sem falha silenciosa).
- Adicionar testes focados em equipamentos (armas/escudos/armaduras).
Critério de pronto:
- Regressões principais cobertas por testes.
Status: `CONCLUIDO (2026-02-25)`

## Plano Posterior (Depois dos Lotes Pendentes Acima)

### Lote 6 - Nova aba Rolagem (estrutura inicial)
Escopo:
- Criar aba `Rolagem` na navegação principal.
- Manter aba `Notas` limpa temporariamente (sem remover código legado de forma destrutiva).
- Estruturar estado da tela para suportar: testes, modificadores e histórico local.
Critério de pronto:
- Aba `Rolagem` acessível e estável.
- `Notas` sem conteúdo jogável ativo.
Status: `CONCLUIDO (2026-02-25)`

### Lote 7 - Motor de rolagem local (MVP jogável)
Escopo:
- Implementar rolagem 3d6 para:
  - testes de atributo,
  - ataque,
  - defesa,
  - rolagem livre.
- Implementar modificadores manuais (+/-) por teste.
- Exibir resultado com cálculo completo (valor base, modificador, total alvo, rolagem, sucesso/falha).
Critério de pronto:
- Todos os tipos de teste executando localmente com resultado consistente.
Status: `PENDENTE`
Andamento:
- Passo 1 concluído (2026-02-25): aba `Rolagem` já possui teste local 3d6 para atributo/ataque/defesa/livre, modificador manual e histórico local de sessão.

### Lote 8 - Ficha clicável na aba Rolagem
Escopo:
- Tornar campos principais da ficha clicáveis para disparar teste direto.
- Mapear contexto do clique (ex.: "Ataque Espada Curta", "Defesa Esquiva", "DX").
- Registrar histórico de rolagens na própria aba `Rolagem`.
Critério de pronto:
- Fluxo "clicou no campo -> rolou -> apareceu no histórico" funcionando para os cenários principais.
Status: `PENDENTE`

### Lote 9 - Integração Discord (backend + app)
Escopo:
- Criar backend leve para envio de mensagens ao Discord via bot (token fora do app).
- Definir payload padrão de rolagem (personagem, tipo, modificador, resultado).
- Conectar app ao backend para publicar resultado no canal configurado.
Critério de pronto:
- Rolagem feita no app aparece no canal Discord via bot com confirmação de envio no app.
Status: `PENDENTE`
Observação:
- Pré-requisito local para validar backend: Node.js + npm instalados no ambiente.
Andamento:
- Passo 1 concluído (2026-02-25): backend `discord-roll-api` criado e healthcheck validado localmente.

### Lote 10 - Polimento e segurança da integração
Escopo:
- Tratar erros de rede/autenticação e estados offline.
- Adicionar opções de configuração de sessão/canal de envio.
- Cobrir fluxo crítico com testes (unidade + integração onde viável).
Critério de pronto:
- Integração estável em cenário real de mesa (uso contínuo sem travas/falhas silenciosas).
Status: `PENDENTE`

## Itens Fechados (Não Mexer Agora)
- Lote 3 (Perícias) fechado e validado.
- Lote 4 (Magia) fechado e validado.
- Lote 5 (Combate de ficha) fechado no escopo de ficha (sem jogabilidade tática).
- Auto-save de recuperação implementado.

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
- `snapshots/checkpoint-20260224-224754`
- `snapshots/checkpoint-20260224-232517`

## Decisão de Escopo Mantida
- Situações de jogabilidade de combate (manobra/postura/múltiplas defesas por turno etc.) permanecem fora da criação/manutenção da ficha nesta fase.
## lembra-me desse item posteriormente
o campo de resumo de Equipamento esta acima das Armaduras, esta errado, tem que ficar abaixo de tudo com uma nota de rodapé, pode diminuir o card dele e o tamanho das letras! 
 os cards de Armas tbm pode diminuir as iformações depois de adicionado, apenas o nome do item, dano que ele causa!
