# PLANO SAGA — GUIA DE IMPLEMENTAÇÃO PARA CLAUDE CODE
## Branch: `GURPS-Saga` · Base: Lote 348 (`feature-mestre-ia-graphrag`)
**Este arquivo é o contrato de execução.** Cada sessão de Claude Code executa EXATAMENTE UM LOTE deste plano e para.

> 📍 **Cópia canônica:** esta, em `.agent/skills/` do repo do app (versionada com o código).
>
> **Registro de execução** (lotes fora do plano também contam na numeração):
> - ✅ Lote 349 = A1 (2026-06-12, commit `f47ec38`)
> - ✅ Lote 350 = EXTRA: fix do toolset unificado do modo análise (2026-06-12, `8af1192`)
> - ✅ Lote 351 = EXTRA: suíte de testes 100% verde + lint baseline (2026-06-12, `302deae`) — a regra 3 (build é lei) vale integralmente a partir daqui
> - ✅ Lote 352 = A2 (2026-06-12) — pendente só a validação do usuário no aparelho (passo 5)
> - ✅ A3 = resolvido FORA do fluxo de lotes (declarado pelo usuário em 2026-06-13; não consumiu número)
> - ✅ Lote 353 = A4 (2026-06-13) — fundação de dados (Room v25) + 16 tools do Narrador + executor; migração validada no emulador, testes instrumentados verdes
> - ✅ Lote 354 = A5 (2026-06-13) — Narrador (modo saga) + Aba Saga + ponte de rolagem real; build verde, smoke test no emulador OK. Pendente: roteiro de jogo no aparelho (chaves de IA)
> - ✅ Lote 355 = EXTRA: polimento do Narrador pós-teste real (definir_cena real, cena de abertura automática, narração no Flash, consultar_mundo por palavras-chave) (2026-06-13)
> - ✅ Lote 356 = EXTRA: configuração de campanha (session zero: gênero/tom/dificuldade/magia/NT/livros) + excluir campanha; migração Room v25→v26 (2026-06-13)
> - ✅ Lote 357 = EXTRA (UI): tela de criação da Saga limpa — configurações atrás do botão "Configuração do Jogo" (diálogo) (2026-06-13)
> - ✅ Lote 358 = EXTRA (UI): diálogo "Configuração do Jogo" em tela cheia + barra de rolagem visível (2026-06-13)
> - ✅ Lote 359 = B1 (2026-06-13) — FASE B início: modelos de combate (domain/combat) + CombatEncounter (ordem/manobrasLegais/estadoResumo) + testes
> - ✅ Lotes 360-362 = B2+B3+B4 (2026-06-13, commit único) — ataque/manobras (CombatActions+ModificadoresCombate), dano localizado (HitLocationRules, paridade Mesa Virtual), estados vitais (InjuryRules); todos com testes
> - ✅ Lote 363 = B6 (2026-06-13) — bestiário (17 criaturas) + check_bestiario.py + BestiarioModels/loader + NpcCombatBrain + testes
> - ✅ Lote 364 = B5 PARCIAL (2026-06-13) — camada de regra: CombatResolver (defesas no fluxo + troca completa, teste de round c/ crítico forçado). Executor `aplicar_dano` + card "Defenda-se!" ADIADOS p/ B8/B7 (precisam do estado vivo do encontro/UI). **Escopo "B2 a B6" do usuário concluído.**
> - ✅ Lote 365 = B7 (2026-06-14) — UI de combate (visual aprovado): CombatTracker + ManeuverCards + DefendaSeCard (TalkBack) + CombatSession (motor de encontro puro, encadeia B1-B6) + SagaCombatController (estado Compose + ponte de defesa) + BestiarioCatalogo. Testes verdes; build 2 variantes verde. MVP usa inicial colorida (retrato real fica p/ B7/E2). `controller.iniciarCombate` pronto p/ o Narrador chamar no B8.
> - ✅ Lote 366 = B8 (2026-06-14) — Narrador⇄combate: CombatBridge + 6 tools reais (iniciar_combate/acao_npc/aplicar_dano/aplicar_condicao/gastar_recurso/conceder_xp); fim de combate → prosa factual + saque (armas dos derrotados) entregue na ficha + conceder_xp; lei de ferro 8 no prompt. Testes verdes; build 2 variantes verde. **FASE B COMPLETA.** Divergência (regra 12): NPC dirigido pelo motor (B6), acao_npc devolve estado factual (UI interativa do B7 venceu o "round em lote" do plano).
> - ✅ Lote 367 = EXTRA fix de UI (2026-06-14, validação no aparelho) — painel de combate engolia o chat e cortava as manobras sem rolagem; agora divide a tela com o feed (weight) e rola por dentro (cabeçalho "Rodada" fixo). Só layout; build 2 variantes verde.
> - ✅ Lote 368 = EXTRA combate fidelidade (2026-06-14, pós-validação) — "arma em uso": corrige o herói atacando de SOCO (tipo de dano parseado da expressão, pa*→pi*; armas de fogo/distância incluídas), lista de ataques escolhíveis (SeletorDeArma), penalidade de distância + só-Esquiva contra tiro. Estudei o cap. de Combate no `chunks.jsonl` e **corrigi a regra Mover e Atacar** (estava invertida: o correto é CaC −4+teto 9, à distância −2). Build 2 variantes verde. Pendente do pedido do usuário: narração no combate (próximo) e manobras com opções (Preparar/Sacar, Apontar, Avaliar, Mudar Postura, Mover dirigido).
> - ✅ Lote 369 = EXTRA narração no combate (2026-06-14) — log do combate virou prosa de mestre DETERMINÍSTICA (sem IA), mantendo os números num colchete técnico. `CombatSession.narrarTroca`. Build 2 variantes verde.
> - ✅ Lote 370 = EXTRA manobras com opções (2026-06-14) — Mover dirigido (alvo/direção/metros), Mudar de Postura (seletor + regra "não levanta direto de deitado"), Avaliar (+1..+3, MB p.365). Sub-diálogos na UI. Build 2 variantes verde. FALTA do pedido: Apontar (+Prec) e Preparar/Sacar (arma pronta) — exigem puxar o Acc da arma do catálogo p/ a ficha.
> - ✅ Lote 371 = EXTRA stats de arma → ficha (2026-06-14) — os JSONs já tinham reach/Acc/1-2D/Máx/CdT/Bulk/Recuo; o app passou a LER e GUARDAR (ArmaCatalogoItem + loader + Equipamento anulável + AtaqueHeroi). Análise confirmou: aditivo, sem migração, fichas antigas intactas. `check_armas.py` (0 erros). Sem mudar regra de combate ainda. Build 2 variantes verde.
> - ✅ Lote 372 = EXTRA Testes de Sentidos na Rolagem (2026-06-14) — clicar PER abre diálogo Visão/Audição/Olfato-Paladar/Tato (regra MB p.358); `SentidoRules` automatiza vantagens/desvantagens (Visão/Audição Aguçada, Hiperespectral, Discriminatório, Duro de Ouvido, Cegueira/Surdez/Disosmia) com "notinha" do motivo; variante PraCego (botão rotulado). Sem mudar estrutura da ficha. Testes + build 2 variantes verde.
> - ✅ Lote 373 = EXTRA combate à distância (2026-06-14) — manobra Apontar (+Precisão/Acc no próximo tiro), dano pela metade além de 1/2D, erro automático além do Máx (usa os stats do Lote 371). Testes + build 2 variantes verde.
> - ✅ Lote 374 = EXTRA combate corpo-a-corpo (2026-06-14) — engajamento por reach (arma "C"/"1"/"2" define quem você atinge; longe demais = aproxime-se) + Sacar/Preparar arma (na mão vs guardada; Preparar gasta o turno, livre com Saque Rápido). Teste + build 2 variantes verde.
> - ✅ Lote 375 = EXTRA fim das regras de arma (2026-06-14) — Bulk no Avançar-e-Atacar à distância (−2 ou Magnitude, o pior) + Aparar E/D (esgrima −2 extra; desbalanceada não apara após atacar; "Não"/à distância não apara). Persistiu `armaAparar`. Testes + build 2 variantes verde. **Regras de arma no combate completas** (reach, Apontar/Acc, 1/2D, Máx, Bulk, Aparar E/D, Sacar/Preparar).
> - ✅ Lote 376 = EXTRA rajada (2026-06-14) — CdT → bônus de acerto (tabela MB p.374) + Recuo → múltiplos acertos (1+⌊margem/Recuo⌋) + Ataque Total à distância +1. Testes + build 2 variantes verde.
> - ✅ Lote 377 = EXTRA dual-wield (2026-06-16) — Ataque Total (Duplo, MB p.366): 2 golpes no mesmo alvo, 2ª arma na mão inábil −4 salvo Ambidestria (`heroiAtaqueDuplo` + helpers `resolverGolpeHeroi`/`golpeForaDeAlcance`). De quebra fechou a lacuna **"sem defesa ativa após Ataque Total"** (todos os modos): `heroiSemDefesaAtiva` anula a defesa do herói até o próximo turno. UI: modo Duplo + seletor da 2ª arma + notinha. Testes + build 2 variantes verde. **Polir combate FECHADO** (RoF/Recuo + dual-wield).
> - ✅ Lote 378 = BUGFIX da validação no aparelho (2026-06-17) — após **auditoria do capítulo de combate (MB 363–384)** vs o app (diretriz: concluir TODAS as regras antes da Fase C): (1) perícia da arma casa **armas de fogo** (especialização + fallback por família); (2) **Mover e Atacar** abre o card e funciona (`heroiMoverEAtacar` aproxima + golpeia; só Esquiva/Bloqueio depois, MB p.367); (3) Sacar com UX clara ("Trocar arma"); (4) dano sem token duplicado (`semTokenTipo`). Testes + build 2 variantes verde.
> - ✅ Lote 379 = BUGFIX ficha/escudo (2026-06-17) — BD do escudo/capa só conta quando **explicitamente selecionado** na defesa de Bloqueio (`getBonusEscudo` perdeu o fallback que pegava o melhor escudo só por estar na lista). Sem seleção = BD 0; com seleção = soma em Esquiva/Apara/Bloqueio (MB p.375). Teste atualizado + build 2 variantes verde.
> - ✅ Lote 380 = BD do escudo no combate (2026-06-17, MB p.375) — o BD (em `HeroiPerfilCombate.bonusEscudo`) é REMOVIDO da defesa quando a arma pronta é de **duas mãos** (sem mão livre) ou o ataque é de **arma de fogo**. Detecção de 2 mãos orientada a DADO (`ArmaCatalogoItem.duasMaosPorGrupo`, pelo grupo), não por nome (correção de viés apontado pelo usuário). Arma de fogo do NPC: flag + `pareceArmaDeFogo`. Testes + build 2 variantes verde.
> - ✅ Lote 381 = Modificador de Tamanho no acerto (2026-06-17, MB p.549) — à distância soma-se o **MT do alvo** ao NH (nas duas direções: herói↔NPC). `NpcStats.modificadorTamanho`←`BestiarioCriatura.mt`; `HeroiPerfilCombate.modificadorTamanho`←ficha. Corpo-a-corpo NÃO soma MT (p.548). Motor + testes verdes. ⚠️ Pendência de DADO: preencher MT no bestiário (criaturas grandes/pequenas) conferindo valores — sem chutar.
> - ✅ Lote 382 = Choque + Cambaleante (2026-06-17, MB p.419/380) — loop de regras 1/5. Choque: PV perdidos → −DX/IQ no próximo turno (`choquePendente`, teto −4, não nas defesas). Cambaleante (<1/3 PV): Esquiva e Deslocamento à metade. Testes + build 2 variantes verde.
> - ➡️ Próximo (auditoria de combate, em ordem): "números que faltam" (**Modificador de Tamanho** no acerto + **Bônus de Defesa de escudo**) → Fintar → Ataque Enganoso/Golpe Rápido → Choque/Cambaleante → defesas que faltam → movimento por postura/Agachar → críticos com tabelas → Tolerância a Ferimentos/Divisor de Armadura → Agarrar/munição. Só DEPOIS de tudo isso: Fases C/D/E. (Validação no aparelho em paralelo; interação Narrador-no-combate adiada por ordem do usuário.)

---

## 0. REGRAS PERMANENTES (valem em todos os lotes — releia a cada sessão)

1. **Um lote por sessão.** Execute apenas o lote pedido. Ao terminar: relatório + PARE. Nunca "aproveite" para adiantar o próximo.
2. **Numeração:** confira no `PROGRESS.md` o último lote concluído. O lote pedido recebe o próximo número livre. A ordem deste plano é obrigatória; os números podem deslocar se o projeto avançou em paralelo.
3. **Build é lei:** `./gradlew build` verde nas **duas variantes** (Visual e PraCego) antes de qualquer commit. Se vermelho, conserte ou reverta — nunca commite quebrado.
4. **Não toque no que funciona:** Auditor, Forjador, Voz, Pintor, Ficha, Rolagem, Nexus Arcano e VTT só podem ser alterados quando o passo do lote mandar explicitamente. Qualquer outra mudança neles = bug.
5. **Regras de GURPS nunca são inventadas:** consulte `.agent/skills/Skill_GURPS.MD` e, em dúvida, o Códex via os assets/chunks. Toda regra implementada ganha comentário com a referência (ex.: `// MB p.398`). **Jamais copie texto dos livros** — implemente a matemática.
6. **Prompts de IA são categoriais:** descrevem categorias de comportamento, **zero exemplos hardcoded** (lição do Lote 318 — exemplos viram cola/viés).
7. **Toda regra nova tem teste** em `app/src/test/...` no padrão das suítes `NexusArcano*Test`/`RulesLayerTest`. Regra sem teste = lote incompleto.
8. **Acessibilidade nasce junto:** todo componente novo de UI recebe `contentDescription`/semântica TalkBack no MESMO lote (padrão `UiA11y.kt`), não depois.
9. **PROGRESS.md:** ao fechar o lote, adicione a entrada no formato existente (número, data, resumo, arquivos, decisões). Commit: `Lote NNN: <resumo curto>`.
10. **Antes de deletar qualquer símbolo:** `grep -rn "NomeDoSimbolo" app/ | grep -v "Arquivo de origem"` deve voltar vazio. Cole a saída no relatório.
11. **Idioma:** código em inglês onde o projeto já usa inglês; domínio GURPS em PT-BR onde o projeto já usa PT-BR (siga o padrão do arquivo vizinho). UI sempre PT-BR.
12. **Em conflito entre este plano e o código real:** o código real vence; relate a divergência no relatório e proponha ajuste — não improvise silenciosamente.
13. **Check de conclusão:** ao fechar cada lote, marque ✅ no título do lote NESTE arquivo (a cópia em `.agent/skills/` do repo) com o número real executado e a data, e atualize o "Registro de execução" no topo. Lotes extras (fora do plano) entram no registro para a numeração não se perder.

### Relatório padrão de fim de lote (obrigatório)
```
LOTE NNN — <título>
✔ Passos executados: <lista>
✔ Arquivos criados/alterados: <lista com linhas +/-> 
✔ Testes: <N novos, todos verdes>  ✔ Build: Visual ✔ / PraCego ✔
⚠ Divergências do plano: <ou "nenhuma">
→ Próximo lote sugerido: NNN+1 (<título>)
```

---

## FASE A — CASA LIMPA E FUNDAÇÃO (5 lotes)

### ✅ LOTE A1 (= Lote 349, concluído 2026-06-12) — Limpeza cirúrgica de código morto e assets-lixo
**Objetivo:** remover ~1.880 linhas zumbis e 4 assets do APK antes que a Saga os referencie.
**Passos:**
1. `grep -rn "MestreIATopicIndex" app/` → confirme que só o próprio arquivo aparece → delete `MestreIATopicIndex.kt`.
2. Em `MestreIAUseCase.kt`: localize e remova `executarBuscaCodex`, `gerarCatalogoDireto`, `reescreverQueryParaGurps` e os cases de tools que nunca disparam no modo Auditor atual (confirme com grep que nenhum roteador os chama).
3. `MestreIAPlanner.kt`: mova a data class `TermoPonderado` para `MestreIAQueryEngine.kt`; delete o resto do arquivo; ajuste imports.
4. Renomeie `getOpenAITools` → `getForjadorLegacyToolsOpenAI` (e a variante Gemini), atualize os callers; adicione comentário-guarda: `// LEGADO: usado SOMENTE pelo fluxo antigo do Forjador. Não adicionar tools aqui.`
5. Cabeçalho `⚠️ USADO APENAS PELA VOZ (GeminiLive) E FORJADOR — NÃO REMOVER` em `MestreIAGraphEngine.kt`, `VectorEngine`/`SemanticEngine` (se existirem como arquivos próprios).
6. `assets/`: delete `pericias_v2_rules_map copy.json`, `topic_index.json`, `topic_index_backup_manual.json`, `topic_index_gerado.json` (mova para `lixeira/` no repo, fora do APK).
7. `VttHostAutoDetect`: envolva a leitura de `/proc/net/arp` em `if (Build.VERSION.SDK_INT < 29)`.
8. Rode toda a suíte de testes existente.
**Aceite:** build verde 2 variantes; greps de deleção vazios anexados; APK menor (anote o delta); `ARQUITETURA_MESTRE_IA.md §5` atualizado marcando os itens como "removidos no Lote NNN".

### ✅ LOTE A2 (= Lote 352, concluído 2026-06-12; falta validar Voz/Auditor no aparelho) — Um motor de busca só + Códex em dieta
**Objetivo:** Voz passa a usar `localizar/ler` do Auditor; `chunks.jsonl` perde os embeddings (−48 MB).
**Passos:**
1. Em `GeminiLiveTools.kt`: substitua a implementação da tool de consulta ao manual por chamadas a `MestreIARepository.localizarNoCodex(query)` + `lerPaginas(ids)`; formate o retorno compacto.
2. Crie `const val LIVE_MAX_TOOL_PAYLOAD = <valor atual usado nos truncamentos>` em local único; aplique o truncamento centralizado no roteador `executar()` da Voz; logue quando truncar.
3. Confirme com grep que NENHUM caller restante usa o caminho semântico (HNSW/ObjectBox) — se a Voz era o último, o caminho fica dormente (não delete ainda; marque com o cabeçalho ⚠️ de legado dormente).
4. Substitua o conteúdo de `assets/.../chunks.jsonl` pelo de `chunks.jsonl.bak` (mesmo texto, sem embeddings). Bump `CODEX_VERSION_CURRENT` (+1) para forçar re-seed do Room.
5. Teste manual roteirizado: 3 perguntas de regra na Voz e 3 no Auditor (use as perguntas-padrão do histórico de logs); todas devem citar página.
**Aceite:** build verde; APK ≥40 MB menor (anote); Voz e Auditor respondem com citação; nenhum import de embeddings no caminho da Voz.

### ✅ LOTE A3 (resolvido fora do fluxo — declarado pelo usuário em 2026-06-13) — Persistência no Railway (Node — fora do Gradle)
**Objetivo:** fichas e retratos sobrevivem a restart do servidor Discord.
**Passos:** em `discord-roll-api/`: adicionar `better-sqlite3`; criar `db.js` (tabelas `fichas(deviceId TEXT PK, json TEXT, updatedAt INTEGER)` e `portraits(name TEXT PK, mime TEXT, blob BLOB)`); apontar o caminho do arquivo para o Volume Railway (`process.env.RAILWAY_VOLUME_MOUNT_PATH || './data'`); converter as rotas `/api/fichas*` e o mapa `portraits` para write-through (escreve no SQLite, mantém cache em memória para leitura); `GET /health` reporta `dbOk`.
**Aceite:** deploy; criar ficha → restart manual do serviço → ficha e retrato persistem. (Sem mudanças no app Android neste lote.)

### ✅ LOTE A4 (= Lote 353, concluído 2026-06-13) — Fundação de dados da Saga + contrato de tools
**Objetivo:** persistência da campanha e o contrato completo do Narrador (sem IA ainda).
**Passos:**
1. `data/storage/SagaEntities.kt`:
   - `CampanhaEntity(id, nome, cenarioId, personagemId, criadaEm, capituloAtual, resumoCapitulo, tempoJogoMin, seedMundo)`
   - `CenaEntity(id, campanhaId, indice, titulo, resumo, bioma, humor, fechadaEm?)`
   - `CampaignFactEntity` **FTS4** (`campanhaId, sujeito, predicado, objeto, peso, cenaId, texto` — `texto` = concatenação indexada)
   - `WorldStateEntity(campanhaId PK, climaPorRegiaoJson, relogiosJson, ecologiaJson, economiaJson, ultimoTickMin)`
2. `SagaDao.kt` (CRUD + `buscarFatos(campanhaId, query, limite)` via MATCH, ordenado por peso e BM25 como em `ManualChunkDao`).
3. Migração Room v24→v25 em `FichaDatabase` (siga o padrão das migrações anteriores; rode app antigo→novo num emulador para validar).
4. `domain/saga/NarradorTools.kt`: schemas Gemini **e** OpenAI das 14 tools (§3.2 do PLANO_GURPS_SAGA_v2): `pedir_rolagem, iniciar_combate, acao_npc, aplicar_dano, aplicar_condicao, gastar_recurso, consultar_mundo, registrar_fato, avancar_relogio, passar_tempo, conceder_xp, definir_cena, forjar_npc, inspecionar_personagem` (+ reuso declarado de `localizar_no_codex`/`ler_pagina`). Descrições categoriais.
5. `domain/saga/NarradorToolExecutor.kt`: roteador `suspend fun executar(nome, argsJson): String` no padrão do `ForjadorToolExecutor`. Implementação REAL de: `registrar_fato`, `consultar_mundo`, `inspecionar_personagem` (delega ao existente), `localizar/ler` (delega). Os demais devolvem JSON `{"erro":"nao_implementado","tool":"..."}` logado.
6. Testes: DAO (gravar 5 fatos, buscar por termo, ordenação por peso), executor (roteamento + fato roundtrip).
**Aceite:** build verde; migração validada; testes verdes.

### ✅ LOTE A5 (= Lote 354, concluído 2026-06-13; falta roteiro de jogo no aparelho) — Narrador mínimo viável + Aba Saga
**Objetivo:** jogar uma cena: narração → pedido de rolagem → dado real → consequência.
**Passos:**
1. `data/network/MestreIAPromptsNarrador.kt` — persona em blocos categoriais: identidade (mestre de GURPS 4ª ed.); leis de ferro (nunca declarar números/resultados sem tool deste turno; em incerteza mecânica, `pedir_rolagem` com modificadores nomeados; fatos de `consultar_mundo` são canônicos; máx. 3 parágrafos por turno; terminar abrindo escolha ao jogador); uso de cada tool em 1 frase categorial; proibições (não decidir dano, não pular a vez do jogador, não inventar conteúdo de regra — usar `localizar/ler`).
2. `domain/MestreIANarradorUseCase.kt` — clone estrutural do `MestreIAGeneratorUseCase`: monta contexto (ficha via `MestreIAContextFilter` + cena atual + últimos 8 turnos + top-5 `consultar_mundo` automático sobre a mensagem do jogador), loop de tool-use com `NarradorToolExecutor`, fila de fallback de modelos existente, persiste turnos em `CenaEntity`/chat.
3. `MestreIAClient`: modo `saga` → seleciona `NarradorTools` (siga o switch de modos existente; adicione `require` de modo válido).
4. `domain/saga/NarradorOutputValidator.kt` v1: se a prosa final contém padrão de resultado mecânico (regex de `\d+ (de )?dano|PV|margem`) sem tool call correspondente no turno → 1 re-pedido com instrução de correção (padrão Auto-Healing do Lote 52). Teste unitário com 4 casos.
5. `ui/TabSaga.kt` + registro na navegação: lista de campanhas (criar/continuar) → feed da cena reusando os componentes visuais do chat do Mestre IA; sugestões clicáveis (promover o padrão do Lote 16); **máquina de escrever local** (renderizar a resposta em blocos de 2-3 palavras/30 ms); indicador de fase ("consultando o Códex… pedindo rolagem…") alimentado pelos nomes das tools executadas.
6. Executor real de `pedir_rolagem`: emite evento p/ UI → card com perícia, mods nomeados e alvo → ao toque, usa o MESMO caminho de rolagem da `TabRolagem` (3d6 + `CriticoRules.classificar`) → devolve `{soma, alvo, margem, resultado, critico}` ao loop.
7. TalkBack: card de rolagem e feed com semântica completa.
**Aceite:** roteiro manual gravado no relatório: criar campanha → "tento ouvir a conversa dos guardas" → card Audição com mods → tocar dado → narração cita a margem real → fato registrado → fechar app → reabrir → contexto continua. Build verde 2 variantes.

---

## FASE B — COMBATE GURPS 4ª ED. (8 lotes)
> Tudo em `domain/combat/`, Kotlin puro, testável sem UI. Referência de regra em comentário (`// MB p.XXX`). Fonte de verdade conceitual: `.agent/skills/Skill_GURPS.MD` + Códex.

### ✅ LOTE B1 (= Lote 359, concluído 2026-06-13) — Modelos + sequência de turnos
1. `CombatModels.kt`: `Combatente`, `Postura`, `Condicao`, `Manobra`, `DefesasUsadas`, `NpcStats` (campos no §4.1 do PLANO_GURPS_SAGA_v2; PT-BR como o domínio existente).
2. `CombatEncounter.kt`: construtor com lista de combatentes + distâncias iniciais; ordenação por Velocidade Básica (desempate DX, depois aleatório com seed); `proximoTurno()`, `rodadaAtual`, `manobrasLegais(c): List<Manobra>` (filtra por condições: atordoado → só recuperar/Defesa Total; caído → Mudar Postura...; sem alvo engajado → sem ataque corpo-a-corpo), `estadoResumo(): String` (relatório factual p/ IA).
3. Testes (`CombatEncounterTest`): ordem com 4 combatentes (2 empates), legalidade de manobras em 6 estados, resumo determinístico.
**Aceite:** suíte verde; nenhuma dependência de Android no módulo.

### ✅ LOTE B2 (= Lote 360, concluído 2026-06-13) — Ataque e manobras núcleo
1. `CombatActions.kt`: `resolverAtaque(atacante, alvo, arma, manobra, localAlvo?, encounter): RelatorioAtaque`.
2. NH efetivo = NH da arma ± manobra (Ataque Total Determinado +4; Mover-e-Atacar teto 9 e sem aparar depois) ± postura do atacante ± penalidade do local visado ± visibilidade (tabela própria `ModificadoresCombate.kt`: escuridão, névoa — valores do MB).
3. Implementar: Ataque, Ataque Total (Determinado/Duplo/Forte), Mover (gasta Deslocamento real nos `distancias`), Mover-e-Atacar, Mudar Postura, Preparar, Defesa Total (flag p/ B4).
4. Rolagem interna de NPC: 3d6 com a MESMA classificação do `CriticoRules`.
5. Testes: matriz de ≥12 casos com gabarito calculado à mão no comentário.
**Aceite:** suíte verde; relatório de ataque legível (`"NH 14 −3 vitais −2 escuro = 9; rolou 8: acerto, margem 1"`).

### ✅ LOTE B3 (= Lote 361, concluído 2026-06-13) — Dano localizado (porte da Mesa Virtual JS→Kotlin)
1. Extraia os números de `Mesa Virtual/index.html` (calculadora): tabela de locais (penalidade de mira; RD extra quando houver), multiplicadores tipo×local (perf ×3 vitais; ×4 crânio; corte ×1,5 pescoço; etc.), limites de membro (>PV/2 incapacita braço/perna; >PV/3 mão/pé).
2. `HitLocationRules.kt`: `data class Local(...)`, `fun multiplicador(tipoDano, local)`, `fun aplicarDano(alvo, danoBase, tipo, local): RelatorioDano` (ordem: RD do local → dano penetrante → multiplicador → limite de membro → retorna PV a subtrair + efeitos).
3. Testes de PARIDADE: ≥12 casos idênticos aos da calculadora web (rode-a mentalmente/manual e cole o gabarito no teste).
**Aceite:** paridade 100% com a Mesa Virtual; comentários `// MB p.XXX` em cada tabela.

### ✅ LOTE B4 (= Lote 362, concluído 2026-06-13) — Estados vitais
`InjuryRules.kt`: choque (−min(dano,4) em DX/IQ no próximo turno); ferimento grave (>PV/2) → HT ou atordoado+caído; PV≤0 → HT por turno para agir; morte: HT a −1×PV, −2×PV...; inconsciência; recuperação de atordoamento (HT no fim do turno). Integrar ao `Combatente`. Teste-simulação 0→morte com log de cada teste e seed fixa.

### 🟡 LOTE B5 (= Lote 364, PARCIAL 2026-06-13: camada de regra feita; executor `aplicar_dano` + card "Defenda-se!" movidos p/ B7/B8) — Defesas no fluxo + dano fim-a-fim
1. Estender `CombatRules.kt` SEM quebrar funções atuais: apara múltipla (−4 cumulativo na mesma arma), retração (+1 Esquiva/+3 Apara-Bloqueio, 1×/turno), Defesa Total (+2 na escolhida), bloqueio 1×/turno, sem defesa em surpresa/costas.
2. Executor real `aplicar_dano` no `NarradorToolExecutor` encadeando B3+B4.
3. UI: card "Defenda-se!" (opções com valores finais; toque rola via TabRolagem) emitido quando o alvo é o herói.
4. Crítico no ataque do NPC contra o herói: defesa anulada + tabela do `CriticoRules` (já pronta) aplicada.
**Aceite:** teste de integração: round completo herói×1 NPC via executores, com um crítico forçado (seed) disparando a tabela.

### ✅ LOTE B6 (= Lote 363, concluído 2026-06-13; 17 criaturas, F1 expande p/ 40) — Bestiário + cérebro tático de NPC
1. `assets/bestiario.v1.json` (~40 criaturas; schema §4.5) + `scripts/check_bestiario.py` no padrão dos checks existentes (IDs únicos, dano PT-BR `cont/corte/perf/imp`, locais válidos).
2. `model/BestiarioModels.kt` + loader no padrão `CatalogLoaders`.
3. `NpcCombatBrain.kt`: dado o estado, decide manobra/alvo/local por `agressividade`, `moral` (foge abaixo de X% PV), alcance da arma e distância — determinístico com seed. É o fallback quando o Narrador não especificar detalhes em `acao_npc`.
4. Testes: arqueiro mantém distância; bruto avança; covarde foge a 30% PV.
**Aceite:** `check_bestiario.py` zero erros; 3 goblins lutam sozinhos de forma coerente em teste de simulação.

### ✅ LOTE B7 (= Lote 365, 2026-06-14) — UI de combate + TalkBack
1. `ui/features/saga/CombatTracker.kt`: faixas horizontais (Engajado/Perto/Médio/Longe/Extremo) com retratos, barra de PV, postura e condições; herói fixo à esquerda.
2. `ManeuverCards.kt`: somente `manobrasLegais()`; sub-diálogo de alvo + local do golpe (lista com penalidades visíveis).
3. TalkBack: cada combatente = frase única ("Goblin, faixa Médio, catorze metros, em pé, ferido"); cards e sub-diálogos com `stateDescription`.
**Aceite:** combate jogável de olhos fechados na variante PraCego (roteiro no relatório).
> 🎨 **Visual aprovado (2026-06-13):** mockup dos 3 cards (CombatTracker faixas + PV + herói destacado; card de manobra só com legais + sub-diálogo alvo/local; card "Defenda-se!" com valores finais e Rolar) validado pelo usuário. No B7, INICIAL: avatar com a inicial colorida (azul herói / vermelho inimigo) + barra de PV verde→amarelo→vermelho.
> 📌 **REGISTRO p/ depois (retratos):** trocar as iniciais por RETRATOS reais — herói via `ImagemPersonagemStore` (já existe); NPCs via imagem gerada. O projeto já tem o Mestre Pintor (`data/network/GeminiImageService.kt`, API de imagem do Gemini). Ideia do usuário: o NARRADOR gera imagens em TEMPO REAL (retrato de NPC ao `forjar_npc`/`iniciar_combate`, arte de cena ao `definir_cena`) com cache por NPC/cena. Casa com o LOTE E2 (Imagem de cena) — implementar lá ou num lote dedicado de "retratos de combate". Fallback sempre = inicial colorida.

### ✅ LOTE B8 (= Lote 366, 2026-06-14) — Integração Narrador⇄Combate
Executores reais: `iniciar_combate` (instancia do bestiário e/ou `forjar_npc` via Forjador), `acao_npc` (valida → executa → relatório), `aplicar_condicao`, `gastar_recurso`. Round de NPCs em LOTE (1 chamada de IA decide intenções de todos; motor executa um a um). Fim de combate → relatório agregado → Narrador converte em prosa → saque da tabela da criatura → gancho p/ `conceder_xp`.
**Aceite:** dizer ao Narrador "três bandidos saem da mata" → combate completo → prosa final SEM números inventados (`NarradorOutputValidator` zero alarmes) → saque entregue na ficha.
> **Entregue (Lote 366):** CombatBridge + as 6 tools roteadas (iniciar_combate/acao_npc/aplicar_dano/aplicar_condicao/gastar_recurso/conceder_xp); fim de combate → prosa factual + saque (armas dos derrotados) na ficha + conceder_xp; lei de ferro 8 no prompt; teste de roteamento verde.
> **Divergências (regra 12):** (1) NPC dirigido pelo MOTOR (NpcCombatBrain/B6) e jogado na UI interativa do B7 — `acao_npc` devolve o ESTADO FACTUAL p/ narração em vez de dirigir o "round em lote" (a UI aprovada venceu). (2) `forjar_npc` dentro do iniciar_combate (NPC sob medida) e TABELAS DE SAQUE por criatura ficam p/ enriquecimento (F1); saque atual = armas dos inimigos derrotados. (3) `gastar_recurso` dinheiro/munição/item = nota narrativa (a ficha não modela esses como número vivo); pf/pv são reais e salvos.

---

## FASE C — MUNDO VIVO (5 lotes)

### LOTE C1 (≈362) — Schema de cenário + Fendaverso mínimo
`model/CenarioModels.kt` + loader; `assets/cenarios/fendaverso/cenario.json` (3 regiões com níveis de mana distintos, 4 facções/6 relógios, 1 cidade com 8 NPCs âncora, ecologia de 12 criaturas do bestiário, 6 tabelas de encontro, calendário com 4 estações, 1 arco em 3 atos com marcos de XP); `scripts/check_cenario.py` (IDs cruzados com bestiário). Narrador recebe bloco "cenário ativo" no contexto.
**Aceite:** check zero erros; Narrador descreve a cidade usando só dados do pacote (verificável: nomes batem).

### LOTE C2 (≈363) — Tempo + clima
`domain/saga/WorldTickEngine.kt`: calendário (minutos de jogo), Markov de clima por região×estação (matriz no cenario.json), seed em `WorldStateEntity`. Executor real `passar_tempo` → delta textual de ≤3 linhas no contexto. Clima atual aplica modificador real em `pedir_rolagem` quando pertinente (chuva → visibilidade).
**Aceite:** teste determinístico de 30 dias com seed; dormir 3 dias no jogo → clima evolui e o Narrador menciona sem comando.

### LOTE C3 (≈364) — Relógios de facção + eventos
Avanço por tempo (taxa no cenário) e por `avancar_relogio`; relógio cheio → `CampaignFact` peso 10 + evento da facção injetado como "ACONTECIMENTO OBRIGATÓRIO" no próximo contexto; háptico curto na UI quando completar. Testes de avanço/transbordo.
**Aceite:** ignorar o culto por 2 capítulos (simulado) → o ritual acontece e `consultar_mundo` o devolve em 1º lugar.

### LOTE C4 (≈365) — Ecologia + economia
Predador-presa discreto (`densidade += natalidade − pressãoDePredadores − caçaDoJogador`, saturação 0-10) alimentado pelas mortes registradas em combate; economia: `preçoFinal = base × (1 + escassezRegional)`, escassez movida por eventos/estação. Rumores gerados de eventos entram como fatos peso 4.
**Aceite:** teste: exterminar lobos → 2 ticks → cervos ↑, grão ↑ de preço, rumor gerado.

### LOTE C5 (≈366) — Memória hierárquica + orçamento de contexto
Resumo de cena ao fechar (chamada ao modelo Lite da fila, ≤150 palavras, salvo em `CenaEntity.resumo`); ao fechar capítulo, colapsar resumos de cena no `resumoCapitulo`. Montador de contexto com orçamento fixo (alvo ≤6k tokens: ficha filtrada + cena + resumoCapitulo + top-5 fatos + 8 turnos + delta de tick). Logar `tokens/turno` no padrão de log do Client.
**Aceite:** sessão sintética de 40 turnos (script) mantém média ≤6k e responde corretamente pergunta sobre a cena 1 no turno 40.

---

## FASE D — EVOLUÇÃO (2 lotes)

### LOTE D1 (≈367) — XpEngine
`domain/saga/XpEngine.kt`: teto por sessão (constante, default 4); `conceder_xp` valida motivo contra marcos do arco + interpretação de desvantagens (checagem categorial no fechamento de cena: o Narrador responde se desvantagens listadas foram honradas); cada `pedir_rolagem` registra a perícia em `periciasUsadasJson` da campanha. Extrato de XP visível no feed.
**Aceite:** arco do C1 concluído rende 3–5 pts com justificativa item a item.

### LOTE D2 (≈369*) — Acampamento (*depois do upgrade E0 se a UI pedir APIs novas)
`ui/features/saga/CampScreen.kt`: gastar XP reusando os fluxos de edição da ficha (trava: perícia não usada exige tempo de treino — diálogo explica a regra); loja (catálogos × preço do tick); cura natural (1 PV/dia com HT) e médica (perícia Primeiros Socorros/Medicina); oferta de cicatriz consentida após ferimento crítico sobrevivido (aceitar desvantagem do catálogo = pontos, recusar = custo de cura). 
**Aceite:** ciclo aventura→acampamento→ficha evoluída→nova aventura sem tocar em tela antiga.

---

## FASE E — IMERSÃO (5 lotes)

### LOTE E0 (≈368) — Upgrade de plataforma (PRÉ-REQUISITO da fase)
Compose BOM 2024.xx + AGP/Kotlin compatíveis. ZERO mudança funcional. Smoke test manual nas 2 variantes (abrir ficha, rolar, Auditor, Forjador, Voz, Pintor). Se algo quebrar: corrigir API depreciada no mesmo lote.
**Aceite:** build verde, app comportamento idêntico (checklist no relatório).

### LOTE E1 (≈370) — AudioEngine
Deps: Media3 (ExoPlayer) + SoundPool. `domain/media/AudioEngine.kt`: música por humor com crossfade 2s; camadas de ambiente por bioma×clima×hora; SFX por evento (dado, acerto, crítico, moedas, level-up); ducking sob TTS. `assets/audio/` v1 (10 músicas, 12 ambientes, 15 SFX — fontes CC0/CC-BY documentadas em `CREDITOS_AUDIO.md`) + `audio_map.json` (tags→arquivos). Executor real `definir_cena` (parte áudio).
**Aceite:** trocar humor → crossfade audível; tick muda clima → chuva entra; dado tem som; créditos completos.

### LOTE E2 (≈371) — Imagem de cena
`assets/cenas/` (~30 ilustrações bioma×hora geradas previamente em lote pelo `GeminiImageService`, curadas, otimizadas WebP) + `ImagemCenaStore` (clone do `ImagemPersonagemStore`); cabeçalho da cena mostra a arte do bioma instantânea; botão "ilustrar este momento" → Narrador escreve prompt visual → `GeminiImageService` 16:9 → cache por cena.
**Aceite:** cena nova exibe arte <100 ms; ilustração sob demanda persiste ao reabrir.
> 📌 **REGISTRO (geração em tempo real pelo Narrador):** além das cenas, o mesmo `GeminiImageService` (Mestre Pintor) deve gerar **retratos de NPC** em tempo real ao `forjar_npc`/`iniciar_combate` (ver REGISTRO no LOTE B7). Padrão: Narrador escreve o prompt visual → gera 1:1 p/ retrato de NPC, 16:9 p/ cena → cache por id (NPC) / por cena. Fallback = inicial colorida (NPC) / arte de bioma pré-gerada (cena). Decidir se vira sub-lote próprio "retratos de combate" entre B7 e E2.

### LOTE E3 (≈372) — Vida visual + tato
Lottie-compose: chuva/neve/brasas/névoa como overlay da arte conforme clima; shake+flash em ferimento grave do herói; háptica (acerto curto, crítico duplo, 0 PV longo, relógio de facção tique). Tudo com toggle em configurações (acessibilidade vestibular).
**Aceite:** checklist sensorial de 10 eventos.

### LOTE E4 (≈373) — Saga por voz
TTS nativo (`TextToSpeech`) lendo a prosa (fila por parágrafo, velocidade configurável; ON por padrão na variante PraCego); `GeminiLiveTools` ganha o roteador do `NarradorToolExecutor` → jogar a Saga conversando (modo Voz abre campanha ativa).
**Aceite:** sessão de 15 min 100% por voz+TalkBack incluindo um combate (roteiro no relatório).

---

## FASE F — CONTEÚDO E RELEASE (4 lotes)
**F1 (≈374):** Fendaverso completo (8 regiões, 12 facções, 3 cidades, 40 NPCs âncora, ecologia 40 criaturas, 3 arcos). Só dados+checks.
**F2 (≈375):** Stress: script de sessão sintética 100 turnos (padrão IA Master Laboratory) medindo tokens/latência/RAM; ajustar orçamento e paginação.
**F3 (≈376):** Auditoria PraCego de TODA a Saga (pente-fino padrão Lote 46) + correções.
**F4 (≈377):** Release Saga v1: versão, changelog, `update.json`, APKs das 2 variantes, post Discord.

---

## ANEXO — PROMPTS PRONTOS

### P0 · Prompt inicial (primeira sessão — cole no Claude Code na raiz do repo)
```
Você vai implementar o modo "GURPS Saga" neste projeto Android, seguindo um plano por lotes.

LEIA PRIMEIRO, NESTA ORDEM (obrigatório, antes de qualquer código):
1. PLANO_SAGA_CLAUDE_CODE.md  (inteiro — é o contrato; a seção 0 são suas regras permanentes)
2. .agent/skills/MAPA_DETALHADO.md
3. .agent/skills/ARQUITETURA_MESTRE_IA.md
4. .agent/skills/Skill_GURPS.MD  (índice; consulte sob demanda)
5. PROGRESS.md  (cabeçalho + últimos 5 lotes, para pegar convenções e o último número de lote)

DEPOIS:
1. Confirme que o working tree está limpo (git status). Se não estiver, PARE e me avise.
2. Crie a branch:  git checkout -b GURPS-Saga
3. Identifique o número do último lote no PROGRESS.md. O lote de hoje é o próximo número.
4. Execute SOMENTE o LOTE A1 do plano (limpeza cirúrgica), passo a passo, na ordem.
   - Antes de cada deleção: grep de callers; cole a saída no relatório.
5. Rode ./gradlew build nas duas variantes. Só prossiga com tudo verde.
6. Atualize PROGRESS.md no formato existente e o §5 do ARQUITETURA_MESTRE_IA.md.
7. Commit: "Lote NNN: Limpeza cirúrgica (código morto + assets) — Saga Fase A1".
8. Apresente o relatório padrão do plano e PARE. Não inicie o próximo lote.

REGRAS PERMANENTES: as 12 da seção 0 do PLANO_SAGA_CLAUDE_CODE.md valem em toda sessão.
Se o código real divergir do plano, o código vence: relate e proponha, não improvise.
```

### P1 · Prompt das sessões seguintes (template)
```
Continuamos o GURPS Saga na branch GURPS-Saga.
Releia a seção 0 (regras) do PLANO_SAGA_CLAUDE_CODE.md e os últimos 2 lotes do PROGRESS.md.
Execute SOMENTE o LOTE <ID do plano, ex.: A4> — todos os passos, na ordem.
Build verde nas 2 variantes + testes novos verdes + PROGRESS.md atualizado + commit no padrão.
Relatório padrão ao final e PARE.
```

### P2 · Prompt de correção (quando um lote falhar no aceite)
```
O LOTE <ID> não passou no aceite: <descreva o sintoma>.
Não avance. Diagnostique a causa raiz, corrija dentro do escopo do lote,
rode os testes e o build das 2 variantes, e me traga o relatório revisado.
Se a causa estiver fora do escopo do lote, descreva-a e proponha onde tratá-la — sem implementar.
```
