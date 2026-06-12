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
> - ➡️ Próximo: A4 (≈353)

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

### LOTE A4 (≈352) — Fundação de dados da Saga + contrato de tools
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

### LOTE A5 (≈353) — Narrador mínimo viável + Aba Saga
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

### LOTE B1 (≈354) — Modelos + sequência de turnos
1. `CombatModels.kt`: `Combatente`, `Postura`, `Condicao`, `Manobra`, `DefesasUsadas`, `NpcStats` (campos no §4.1 do PLANO_GURPS_SAGA_v2; PT-BR como o domínio existente).
2. `CombatEncounter.kt`: construtor com lista de combatentes + distâncias iniciais; ordenação por Velocidade Básica (desempate DX, depois aleatório com seed); `proximoTurno()`, `rodadaAtual`, `manobrasLegais(c): List<Manobra>` (filtra por condições: atordoado → só recuperar/Defesa Total; caído → Mudar Postura...; sem alvo engajado → sem ataque corpo-a-corpo), `estadoResumo(): String` (relatório factual p/ IA).
3. Testes (`CombatEncounterTest`): ordem com 4 combatentes (2 empates), legalidade de manobras em 6 estados, resumo determinístico.
**Aceite:** suíte verde; nenhuma dependência de Android no módulo.

### LOTE B2 (≈355) — Ataque e manobras núcleo
1. `CombatActions.kt`: `resolverAtaque(atacante, alvo, arma, manobra, localAlvo?, encounter): RelatorioAtaque`.
2. NH efetivo = NH da arma ± manobra (Ataque Total Determinado +4; Mover-e-Atacar teto 9 e sem aparar depois) ± postura do atacante ± penalidade do local visado ± visibilidade (tabela própria `ModificadoresCombate.kt`: escuridão, névoa — valores do MB).
3. Implementar: Ataque, Ataque Total (Determinado/Duplo/Forte), Mover (gasta Deslocamento real nos `distancias`), Mover-e-Atacar, Mudar Postura, Preparar, Defesa Total (flag p/ B4).
4. Rolagem interna de NPC: 3d6 com a MESMA classificação do `CriticoRules`.
5. Testes: matriz de ≥12 casos com gabarito calculado à mão no comentário.
**Aceite:** suíte verde; relatório de ataque legível (`"NH 14 −3 vitais −2 escuro = 9; rolou 8: acerto, margem 1"`).

### LOTE B3 (≈356) — Dano localizado (porte da Mesa Virtual JS→Kotlin)
1. Extraia os números de `Mesa Virtual/index.html` (calculadora): tabela de locais (penalidade de mira; RD extra quando houver), multiplicadores tipo×local (perf ×3 vitais; ×4 crânio; corte ×1,5 pescoço; etc.), limites de membro (>PV/2 incapacita braço/perna; >PV/3 mão/pé).
2. `HitLocationRules.kt`: `data class Local(...)`, `fun multiplicador(tipoDano, local)`, `fun aplicarDano(alvo, danoBase, tipo, local): RelatorioDano` (ordem: RD do local → dano penetrante → multiplicador → limite de membro → retorna PV a subtrair + efeitos).
3. Testes de PARIDADE: ≥12 casos idênticos aos da calculadora web (rode-a mentalmente/manual e cole o gabarito no teste).
**Aceite:** paridade 100% com a Mesa Virtual; comentários `// MB p.XXX` em cada tabela.

### LOTE B4 (≈357) — Estados vitais
`InjuryRules.kt`: choque (−min(dano,4) em DX/IQ no próximo turno); ferimento grave (>PV/2) → HT ou atordoado+caído; PV≤0 → HT por turno para agir; morte: HT a −1×PV, −2×PV...; inconsciência; recuperação de atordoamento (HT no fim do turno). Integrar ao `Combatente`. Teste-simulação 0→morte com log de cada teste e seed fixa.

### LOTE B5 (≈358) — Defesas no fluxo + dano fim-a-fim
1. Estender `CombatRules.kt` SEM quebrar funções atuais: apara múltipla (−4 cumulativo na mesma arma), retração (+1 Esquiva/+3 Apara-Bloqueio, 1×/turno), Defesa Total (+2 na escolhida), bloqueio 1×/turno, sem defesa em surpresa/costas.
2. Executor real `aplicar_dano` no `NarradorToolExecutor` encadeando B3+B4.
3. UI: card "Defenda-se!" (opções com valores finais; toque rola via TabRolagem) emitido quando o alvo é o herói.
4. Crítico no ataque do NPC contra o herói: defesa anulada + tabela do `CriticoRules` (já pronta) aplicada.
**Aceite:** teste de integração: round completo herói×1 NPC via executores, com um crítico forçado (seed) disparando a tabela.

### LOTE B6 (≈359) — Bestiário + cérebro tático de NPC
1. `assets/bestiario.v1.json` (~40 criaturas; schema §4.5) + `scripts/check_bestiario.py` no padrão dos checks existentes (IDs únicos, dano PT-BR `cont/corte/perf/imp`, locais válidos).
2. `model/BestiarioModels.kt` + loader no padrão `CatalogLoaders`.
3. `NpcCombatBrain.kt`: dado o estado, decide manobra/alvo/local por `agressividade`, `moral` (foge abaixo de X% PV), alcance da arma e distância — determinístico com seed. É o fallback quando o Narrador não especificar detalhes em `acao_npc`.
4. Testes: arqueiro mantém distância; bruto avança; covarde foge a 30% PV.
**Aceite:** `check_bestiario.py` zero erros; 3 goblins lutam sozinhos de forma coerente em teste de simulação.

### LOTE B7 (≈360) — UI de combate + TalkBack
1. `ui/features/saga/CombatTracker.kt`: faixas horizontais (Engajado/Perto/Médio/Longe/Extremo) com retratos, barra de PV, postura e condições; herói fixo à esquerda.
2. `ManeuverCards.kt`: somente `manobrasLegais()`; sub-diálogo de alvo + local do golpe (lista com penalidades visíveis).
3. TalkBack: cada combatente = frase única ("Goblin, faixa Médio, catorze metros, em pé, ferido"); cards e sub-diálogos com `stateDescription`.
**Aceite:** combate jogável de olhos fechados na variante PraCego (roteiro no relatório).

### LOTE B8 (≈361) — Integração Narrador⇄Combate
Executores reais: `iniciar_combate` (instancia do bestiário e/ou `forjar_npc` via Forjador), `acao_npc` (valida → executa → relatório), `aplicar_condicao`, `gastar_recurso`. Round de NPCs em LOTE (1 chamada de IA decide intenções de todos; motor executa um a um). Fim de combate → relatório agregado → Narrador converte em prosa → saque da tabela da criatura → gancho p/ `conceder_xp`.
**Aceite:** dizer ao Narrador "três bandidos saem da mata" → combate completo → prosa final SEM números inventados (`NarradorOutputValidator` zero alarmes) → saque entregue na ficha.

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
