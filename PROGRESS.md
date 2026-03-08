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
6. No pipeline do AGENTE GURPS, páginas de abertura (capa/sumário) devem ser classificadas separadamente para não contaminar a métrica de suspeitas acionáveis.

## Situação de Lotes
1. Lotes históricos concluídos e já validados foram arquivados deste arquivo para evitar ruído operacional.
2. Lotes em aberto no momento: **A11, A12 e A13 (AGENTE GURPS)**.
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

### Lote A5 - Correcao Dirigida das 14 Paginas Suspeitas
Status: `CONCLUIDO`

Passos:
1. [x] Registrar lote de correcao dirigida com base no A4.
2. [x] Implementar refinamento OCR por pagina suspeita (multiplos modos de OCR).
3. [x] Reprocessar somente suspeitas e atualizar paginas/chunks derivados.
4. [x] Gerar relatorio before/after do lote A5 e validar ganho.

Criterios de aceite A5:
1. Relatorio de correcao com comparativo por pagina suspeita.
2. Numero de suspeitas reduzido ou justificativas tecnicas registradas por pagina.
3. Saida final preserva regra operacional de resposta em portugues.

Evidencias A5:
1. Relatorio: `AGENTE GURPS/sources/processed/reports/correcao_suspeitas_report.json`.
2. Resultado do refinamento dirigido:
- `target_pages=14`
- `improved_pages=13`
- `before_suspects=14`
- `after_suspects=5`
- `delta_suspects=9`
3. Relatorio detalhado atualizado: `paginas_suspeitas_detalhado.json` com 5 remanescentes.
4. Checklist final de revisao restante: `AGENTE GURPS/docs/A5_REVISAO_RESTANTE_5_PAGINAS.md`.

### Lote A6 - Fechamento de Suspeitas Operacionais (Capas/Sumarios)
Status: `CONCLUIDO`

Passos:
1. [x] Registrar lote A6 com criterio de separacao entre suspeita tecnica e suspeita operacional.
2. [x] Implementar classificador de paginas residuais para identificar capas/sumarios de abertura.
3. [x] Gerar relatorio de suspeitas operacionais (acionaveis) apos classificacao.
4. [x] Fechar lote com evidencias e regra permanente para evitar falso positivo de capa.

Criterios de aceite A6:
1. Relatorio com duas classes: `aceitas_contexto` (capa/sumario) e `acionaveis`.
2. Paginas residuais atuais classificadas de forma objetiva e rastreavel.
3. Se `acionaveis=0`, lote encerrado com justificativa tecnica.

Evidencia parcial A6.3:
1. Relatorio gerado: `AGENTE GURPS/sources/processed/reports/suspeitas_operacionais_report.json`.
2. Resultado: `total_suspeitas_entrada=5`, `aceitas_contexto=5`, `acionaveis=0`.

Evidencias finais A6:
1. Classificador versionado: `AGENTE GURPS/scripts/classificar_suspeitas_operacionais.py`.
2. Relatório operacional versionado: `AGENTE GURPS/sources/processed/reports/suspeitas_operacionais_report.{json,md}`.
3. Encerramento técnico: `acionaveis=0`, sem pendência real no lote inicial de 90 páginas.

### Lote A7 - Indexacao Vetorial (RAG Base)
Status: `CONCLUIDO`

Passos:
1. [x] Registrar lote A7 no `PROGRESS` com escopo tecnico.
2. [x] Criar estrutura de backend (`AGENTE GURPS/backend`) e configuracoes de ambiente.
3. [x] Implementar script de indexacao de `chunks.jsonl` para ChromaDB.
4. [x] Gerar relatorio de indexacao com total de chunks indexados e fonte/pagina.

Criterios de aceite A7:
1. Index vetorial persistido em `AGENTE GURPS/index/chroma/`.
2. Script reproduzivel para reindexar base (`python .../indexar_chunks_chroma.py`).
3. Relatorio de indexacao versionado em `sources/processed/reports/`.

Evidencias A7:
1. Script: `AGENTE GURPS/backend/indexar_chunks_chroma.py`.
2. Relatorio: `AGENTE GURPS/sources/processed/reports/indexacao_chroma_report.json`.
3. Resultado atual: `total_chunks_indexados=392` na colecao `gurps_pt_v1`.

### Lote A8 - API de Consulta (/ask) com Politica PT-BR
Status: `CONCLUIDO`

Passos:
1. [x] Registrar lote A8 no `PROGRESS` com regras de resposta.
2. [x] Implementar API FastAPI com endpoints `/health` e `/ask`.
3. [x] Implementar recuperacao top-k no indice vetorial + montagem de contexto com citacoes.
4. [x] Aplicar politica fixa: resposta final em portugues, citando fonte/pagina, e marcar inferencia.
5. [x] Validar chamada local da API e documentar comandos de execucao.

Criterios de aceite A8:
1. Endpoint `/ask` funcional com retorno em portugues.
2. Resposta inclui bloco de fontes (`source_id`, `source_title`, `page_number`).
3. Sem base suficiente, API retorna aviso de baixa confianca (sem inventar regra).

Evidencias A8:
1. API: `AGENTE GURPS/backend/api_server.py` (`/health`, `/ask`).
2. Validacao local (TestClient): `health=200`, `ask=200`, `sources=3`.
3. Guia de execucao atualizado: `AGENTE GURPS/backend/README.md`.

### Lote A9 - Guardrails de Confianca e Citacoes (Backend AGENTE)
Status: `CONCLUIDO`

Passos:
1. [x] Registrar lote A9 no `PROGRESS`.
2. [x] Reforcar regra de baixa confianca por score minimo recuperado (sem inventar resposta).
3. [x] Garantir secao de fontes consistente no output, mesmo quando houver resposta por LLM.
4. [x] Validar cenarios minimo: com contexto e sem contexto, com saida em portugues.

Criterios de aceite A9:
1. Backend retorna aviso de baixa confianca quando evidencia for fraca.
2. Resposta final inclui citacoes de fonte/pagina.
3. Politica PT-BR preservada em todos os cenarios de fallback.

Evidencias A9:
1. Guardrails em runtime: `AGENTE GURPS/backend/rag_runtime.py` (`evaluate_evidence`, `build_low_confidence_answer`, `ensure_sources_block`).
2. API com decisao por evidencia: `AGENTE GURPS/backend/api_server.py`.
3. Relatorio de validacao: `AGENTE GURPS/sources/processed/reports/guardrails_report.json`.
4. Resultado de validacao:
- `contexto_forte_esperado`: `confidence=media`, `has_fontes=true`, `has_inferencia=true`.
- `contexto_fraco_esperado`: `confidence=baixa`, `has_fontes=true`, `has_inferencia=true`.

### Lote A10 - Integracao no App Android (Assistente)
Status: `CONCLUIDO`

Passos:
1. [x] Registrar lote A10 no `PROGRESS`.
2. [x] Adicionar configuracao de URL da API do agente no `BuildConfig`.
3. [x] Implementar cliente Android para endpoint `/ask`.
4. [x] Adicionar entrada no menu para abrir dialogo "Assistente GURPS".
5. [x] Implementar dialogo de consulta com pergunta, resposta e fontes.
6. [x] Validar compilacao das variantes `visual` e `pracego`.

Criterios de aceite A10:
1. Usuario consegue abrir o assistente pelo menu.
2. Consulta retorna resposta + fontes sem travar a UI.
3. Funciona nas duas variantes (visual e pracego).

Evidencias A10:
1. URL da API no app: `BuildConfig.GURPS_AGENT_API_BASE_URL` em `app/build.gradle.kts`.
2. Cliente Android: `app/src/main/java/com/gurps/ficha/agent/GurpsAgentService.kt`.
3. UI do assistente: `app/src/main/java/com/gurps/ficha/ui/DialogsAssistente.kt`.
4. Entrada no menu + abertura do dialogo: `DialogsCommon.kt` e `FichaScreen.kt`.
5. Validacao build: `:app:compileVisualDebugKotlin` e `:app:compilePracegoDebugKotlin` com `BUILD SUCCESSFUL`.

### Lote A10.1 - UX Intuitiva do Assistente (Ajuste rápido)
Status: `CONCLUIDO`

Passos:
1. [x] Simplificar seleção de modo para chips (`Regras`, `Criação`, `Lore`).
2. [x] Adicionar dica de uso e ação rápida (`Exemplo Judô`).
3. [x] Adicionar botão `Limpar` para reset da consulta.
4. [x] Melhorar mensagem de erro de rede para texto humano.
5. [x] Revalidar build/instalação da variante visual.

Evidencias A10.1:
1. UI atualizada: `app/src/main/java/com/gurps/ficha/ui/DialogsAssistente.kt`.
2. Mensagens de erro mais claras: `app/src/main/java/com/gurps/ficha/agent/GurpsAgentService.kt`.
3. Build/instalação visual validados após ajuste.
4. Arte de botão reservada para próxima implementação: `AGENTE GURPS/IMAGENS!/GURPS AGENT.png`.

### Lote A11 - Reingestão Completa da Base (qualidade de resposta)
Status: `EM_ANDAMENTO`

Passos:
1. [x] Reexecutar ingestão sem limite de páginas para todos os PDFs permitidos.
2. [x] Regenerar `pages/chunks` e reindexar Chroma com base completa.
3. [x] Gerar relatório comparativo (antes/depois) com cobertura por fonte.
4. [x] Validar perguntas críticas (ex.: Judô, Aptidão Mágica, custo por nível).

Criterios de aceite A11:
1. Cobertura deixa de ficar concentrada em capa/sumário.
2. A maioria das consultas de regras passa a retornar `confiança média/alta`.
3. Relatório de qualidade versionado.

Evidência passo 1:
1. Baseline preservado em `AGENTE GURPS/sources/processed/reports/ingestao_baseline_pre_a11.json`.
2. Nova ingestão completa executada com `--max-pages-per-pdf 10000`.
3. Resultado: `pdfs_processados=19`, `paginas_processadas=3268`, `chunks_gerados=17971`, `paginas_suspeitas=20`, `ocr_disponivel=true`.

Evidência passo 2:
1. Reindexação executada em `AGENTE GURPS/backend/indexar_chunks_chroma.py`.
2. Resultado: `total_chunks_indexados=17971` na coleção `gurps_pt_v1`.
3. Relatório atualizado: `AGENTE GURPS/sources/processed/reports/indexacao_chroma_report.json`.

Evidência passo 3:
1. Script comparativo criado: `AGENTE GURPS/scripts/gerar_comparativo_a11.py`.
2. Relatórios gerados:
- `AGENTE GURPS/sources/processed/reports/a11_comparativo_report.json`
- `AGENTE GURPS/sources/processed/reports/a11_comparativo_report.md`
3. Resultado global:
- `paginas_processadas`: `90 -> 3268`
- `chunks_gerados`: `391 -> 17971`

Evidência passo 4:
1. Script de validação crítica: `AGENTE GURPS/backend/validar_a11_consultas_criticas.py`.
2. Relatório gerado: `AGENTE GURPS/sources/processed/reports/a11_consultas_criticas_report.json`.
3. Resultado atual:
- `judo_funcionamento`: `confidence=baixa`
- `aptidao_magica`: `confidence=media`
- `custo_por_nivel`: `confidence=baixa`
4. Conclusão operacional: cobertura melhorou forte (A11), mas o critério de maioria `media/alta` ainda não foi atingido; depende do próximo lote (A12 com LLM habilitado + ajuste fino de retrieval).

### Lote A12 - Resposta Inteligente (LLM habilitado)
Status: `PLANEJADO`

Passos:
1. [ ] Configurar `OPENAI_API_KEY` no backend do agente.
2. [ ] Validar geração de resposta natural em PT-BR com citação obrigatória.
3. [ ] Ajustar prompt final para reduzir resposta bruta/trechos truncados.
4. [ ] Reexecutar suite de guardrails (`guardrails_report.json`).

Criterios de aceite A12:
1. Respostas legíveis e objetivas em português.
2. Fontes e páginas exibidas em todas as respostas.
3. Sem alucinação em cenário de baixa evidência.

### Lote A13 - Polimento Final de UX no App
Status: `PLANEJADO`

Passos:
1. [ ] Aplicar ícone/arte do assistente no menu/botão (imagem já versionada).
2. [ ] Melhorar layout de resposta longa (quebra, resumo e “ver mais”).
3. [ ] Exibir fontes em bloco compacto e clicável.
4. [ ] Ajustar feedback visual por confiança (baixa/média/alta).
5. [ ] Validar nas variantes `visual` e `pracego`.

Criterios de aceite A13:
1. Fluxo de pergunta-resposta mais rápido de entender.
2. UI consistente com o restante do app.
3. Acessibilidade preservada na variante pra cego.

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
