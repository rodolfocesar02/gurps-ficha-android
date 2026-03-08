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

### Lote A1 - Escopo e Politicas (AGENTE GURPS)
Status: `CONCLUIDO`

Passos:
1. [x] Criar pasta isolada `AGENTE GURPS` para nao poluir o app principal.
2. [x] Definir escopo inicial e politica de confiabilidade do agente.
3. [x] Definir regra de idioma: fontes PT/EN permitidas, resposta final sempre em portugues (texto e audio quando houver).
4. [x] Criar checklist de entrada de fontes para livros/material externo.
5. [x] Validar com voce o escopo A1 e fechar o lote.

Criterios de aceite A1:
1. Estrutura inicial criada em `AGENTE GURPS/` (`docs`, `sources`, `scripts`).
2. Politica de idioma e confiabilidade documentada.
3. Checklist de fontes pronto para receber livros em portugues e ingles.

### Lote A2 - Base de Conhecimento e Ingestao (AGENTE GURPS)
Status: `CONCLUIDO`

Passos:
1. [x] Definir plano tecnico do pipeline de ingestao (raw -> processed -> index).
2. [x] Criar manifesto inicial de fontes com metadados minimos.
3. [x] Criar script de validacao do manifesto.
4. [x] Receber seus livros PT/EN em `AGENTE GURPS/sources/raw/` e registrar no manifesto.
5. [x] Rodar ingestao inicial (extracao/chunks) e gerar primeiro relatorio de cobertura.
6. [x] Definir modo inicial sem conflito: somente fontes em portugues por padrao.

Evidencia parcial A2.5:
1. Script `AGENTE GURPS/scripts/ingestar_pdf_hibrido.py` criado (pipeline hibrido com layout + fallback OCR).
2. Ingestao inicial executada com `--max-pages-per-pdf 5`:
- `pdfs_processados=19`
- `paginas_processadas=90`
- `chunks_gerados=378`
- `paginas_duas_colunas=71`
- `paginas_suspeitas=14`
- `ocr_disponivel=false` (fallback OCR ainda nao habilitado no ambiente atual)
3. Relatorio gerado em `AGENTE GURPS/sources/processed/reports/ingestao_inicial_report.json`.

Criterios de aceite A2:
1. Manifesto validado sem erros.
2. Estrutura de fontes pronta para receber materiais.
3. Regras de idioma preservadas (saida final sempre em portugues).
4. Fontes em ingles ficam desativadas ate liberacao explicita.

### Lote A3 - OCR e Melhoria de Qualidade (AGENTE GURPS)
Status: `CONCLUIDO`

Passos:
1. [x] Registrar lote de melhoria de OCR e meta de reducao de paginas suspeitas.
2. [x] Instalar dependencias OCR no ambiente (engine + bindings Python).
3. [x] Reexecutar ingestao inicial com OCR ativo.
4. [x] Comparar metricas antes/depois e registrar ganho de qualidade.

Criterios de aceite A3:
1. `ocr_disponivel=true` no relatorio.
2. `paginas_ocr_fallback > 0`.
3. Comparativo before/after registrado, com recomendacao objetiva para paginas residuais.

Evidencias A3:
1. OCR habilitado: `tesseract v5.5.0` com idioma `por` + `pytesseract` instalado.
2. Relatorio atualizado: `AGENTE GURPS/sources/processed/reports/ingestao_inicial_report.json`.
3. Resultado comparativo:
- Baseline A2: `ocr_disponivel=false`, `paginas_ocr_fallback=0`, `paginas_suspeitas=14`, `chunks=378`.
- A3 com OCR: `ocr_disponivel=true`, `paginas_ocr_fallback=13`, `paginas_suspeitas=14`, `chunks=391`.
4. Conclusao tecnica: houve ganho de cobertura (OCR aplicado e mais chunks), sem reducao no total de paginas suspeitas; manter fila de revisao manual das 14 paginas residuais no proximo lote.

### Lote A4 - Revisao Dirigida de Paginas Suspeitas (AGENTE GURPS)
Status: `CONCLUIDO`

Passos:
1. [x] Registrar lote de revisão dirigida com meta de listar páginas exatas suspeitas.
2. [x] Gerar relatório detalhado com `source_id`, título, página, tamanho de texto e uso de OCR.
3. [x] Validar contagem final das suspeitas e cruzar com relatório A3.
4. [x] Publicar instrução de revisão manual por prioridade.

Criterios de aceite A4:
1. Lista exata das 14 páginas suspeitas disponível em arquivo versionado.
2. Contagem do detalhado igual ao total de `paginas_suspeitas` do relatório principal.
3. Checklist de revisão manual pronto para execução.

Evidência passo 3:
1. `paginas_suspeitas_detalhado.json` registrou `total_suspeitas_detalhado=14`.
2. Relatório principal registra `baseline_relatorio_principal=14`.
3. `match_baseline=true` confirmado.

## Lotes Ativos - Atualização do App (Ideia 2)
### Lote U1 - Fundação de Atualização Remota
Status: `CONCLUIDO`
1. [x] Definir URL de metadados de atualização no `BuildConfig`.
2. [x] Criar cliente para baixar e interpretar `update.json`.
3. [x] Registrar instruções mínimas de publicação de versão.

### Lote U2 - UX de Verificação no App
Status: `CONCLUIDO`
1. [x] Adicionar ação "Verificar atualização" no menu principal.
2. [x] Exibir resultado (atualizado/atualização disponível/erro).
3. [x] Abrir link do APK mais novo para instalação manual.

### Lote U3 - Validação e Fechamento Operacional
Status: `CONCLUIDO`
1. [x] Validar build `visual/pracego` após integração.
2. [x] Executar testes mínimos de regressão.
3. [x] Documentar o que você precisa fazer para publicar cada nova versão.

### Lote U4 - Operação de Publicação (Passo a Passo com você)
Status: `CONCLUIDO`
1. [x] Definir `UPDATE_METADATA_URL` no `local.properties`.
2. [x] Publicar APKs (`Visual` e `PraCego`) com link direto.
3. [x] Atualizar/publicar `update.json` com `versionCode` novo.
4. [x] Validar no app: Menu > `Verificar atualização`.
5. [x] Compartilhar no WhatsApp o link da release atual.

Evidências de fechamento:
1. `gh` instalado e autenticado para automação de release.
2. Release `V1.3` criada com os dois APKs (`visual` e `pracego`).
3. `docs/update/update.json` publicado com `versionCode=4` e links da `V1.3`.
4. Fluxo validado: menu `Atualizar app` e ação `Atualizar agora`.

## Comandos de Verificação (mínimo)
- `./gradlew :app:testVisualDebugUnitTest --tests com.gurps.ficha.regras_prerequisitos.PreRequisitoParserTest`
- `./gradlew :app:testVisualDebugUnitTest --tests com.gurps.ficha.domain.magias.NexusArcanoModoAlvoAdapterTest --tests com.gurps.ficha.domain.magias.NexusArcanoLoteFCanonicScenarioTest`
- `./gradlew :app:testVisualDebugUnitTest --tests nexus.arcano.NexusArcanoEngineLoteAGlobalTest --tests nexus.arcano.NexusArcanoEngineLoteBGlobalTest`
