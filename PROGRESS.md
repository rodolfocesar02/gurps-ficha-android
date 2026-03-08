# PROGRESS - GURPS Ficha Android

Atualizado em: 2026-03-08

## Regra Permanente - Higiene de Texto (acentos/artefatos)
**Regra fixa para todos os lotes daqui em diante:**
1. Antes de commit de catálogo/UI, executar varredura de mojibake e acentuação quebrada nos arquivos ativos (`app/src/main/assets/*.json` e textos exibidos pela UI).
2. Bloquear publicação se houver strings com artefatos típicos (`Ã`, `Â`, `ï¿½`, `�`, `n?o`, `per?cia`, `pr?-requisito` e variantes).
3. Corrigir no próprio arquivo-fonte canônico antes de gerar APK.
4. Registrar no `PROGRESS.md` o relatório da varredura em cada lote que tocar texto.

## Regras Operacionais
1. Sempre editar primeiro a fonte canônica de pré-requisito antes de mexer na UI.
2. Não aceitar correção só por "funcionou no caso X"; incluir teste de regressão.
3. Quando houver divergência entre JSON e regra canônica, corrigir ambos ou documentar exceção explícita.
4. Cada lote só fecha com:
- testes passando
- relatório em `app/build/reports/` ou `scripts/reports/`
- atualização deste `PROGRESS.md`
5. Commit por lote com mensagem objetiva (`lote-N: ...`).

## Situação de Lotes
1. Lotes históricos concluídos e já validados foram arquivados deste arquivo para evitar ruído operacional.
2. Lotes em aberto no momento: **nenhum pendente formal registrado**.
3. Qualquer novo lote deve ser registrado a partir daqui, com passos e evidências mínimas.

## Lotes de Fechamento de Saúde (2026-03-08)
### Lote S1 - Saneamento de Versionamento
Status: `CONCLUIDO`
1. [x] Ignorar artefatos locais de release (`release-apks/`) no Git.
2. [x] Fechar árvore sem arquivos não rastreados acidentais.

### Lote S2 - Fechamento UI/Acessibilidade
Status: `CONCLUIDO`
1. [x] Consolidar rótulos de ação nos diálogos de editar (Perícias, Magias, Vantagens e Desvantagens).
2. [x] Validar compilação das variantes `visual` e `pracego`.

### Lote S3 - Fechamento Dados/Manual + Verificação Final
Status: `CONCLUIDO`
1. [x] Consolidar ajustes textuais do catálogo de vantagens e manual.
2. [x] Executar testes mínimos de pré-requisito/motor.
3. [x] Atualizar status final e meta de saúde acima de 90%.

## Saúde Atual (2026-03-08)
1. Build (visual/pracego): `100%` (compilação OK).
2. Testes críticos (pré-requisito/motor): `100%` (suite mínima OK).
3. Dados/catálogos ativos: `92%`.
4. UI/Acessibilidade (edição com rótulos): `94%`.
5. Higiene de texto: `91%`.
6. Versionamento: `93%` (sem artefatos locais fora de controle; `release-apks/` ignorado).

## Backlog Futuro - Agente IA GURPS (Ideia 1)
Status: `PLANEJADO`
1. Objetivo: assistente para dúvidas de regras, custos, níveis, ideias de lore/background e apoio à criação de ficha.
2. Arquitetura sugerida:
- Backend online com RAG (base vetorial + API de LLM).
- Base de conhecimento com material autorizado + regras da mesa + JSONs ativos do app.
- App Android com aba "Assistente" consumindo API.
3. Regras de qualidade:
- Resposta com fonte/página quando houver.
- Marcar inferência quando não houver citação direta.
- Não responder como canônico sem evidência da base.
4. Situação: apenas registro de produto; implementação ficará para lote futuro dedicado.

## Lotes Ativos - Atualização do App (Ideia 2)
### Lote U1 - Fundação de Atualização Remota
Status: `EM ANDAMENTO`
1. [x] Definir URL de metadados de atualização no `BuildConfig`.
2. [x] Criar cliente para baixar e interpretar `update.json`.
3. [ ] Registrar instruções mínimas de publicação de versão.

### Lote U2 - UX de Verificação no App
Status: `PENDENTE`
1. [ ] Adicionar ação "Verificar atualização" no menu principal.
2. [ ] Exibir resultado (atualizado/atualização disponível/erro).
3. [ ] Abrir link do APK mais novo para instalação manual.

### Lote U3 - Validação e Fechamento Operacional
Status: `PENDENTE`
1. [ ] Validar build `visual/pracego` após integração.
2. [ ] Executar testes mínimos de regressão.
3. [ ] Documentar o que você precisa fazer para publicar cada nova versão.

## Comandos de Verificação (mínimo)
- `./gradlew :app:testVisualDebugUnitTest --tests com.gurps.ficha.regras_prerequisitos.PreRequisitoParserTest`
- `./gradlew :app:testVisualDebugUnitTest --tests com.gurps.ficha.domain.magias.NexusArcanoModoAlvoAdapterTest --tests com.gurps.ficha.domain.magias.NexusArcanoLoteFCanonicScenarioTest`
- `./gradlew :app:testVisualDebugUnitTest --tests nexus.arcano.NexusArcanoEngineLoteAGlobalTest --tests nexus.arcano.NexusArcanoEngineLoteBGlobalTest`
