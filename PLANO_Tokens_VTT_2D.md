# PLANO — Combate Tático estilo VTT 2D (tokens de imagem)

**Decisão do usuário (10/jul/2026), após teste no aparelho dos lotes HEX-7..9 (render 3D):**
manter a visão TOP-DOWN, mas **abandonar os modelos .glb** e usar **imagens (PNG/JPEG) como tokens**
— estilo RPG de mesa (VTT clássico: Roll20/Foundry). Fundo cinza por enquanto; futuramente o
Narrador dispara a geração da imagem de fundo do cenário e dos tokens dos inimigos por
**gatilhos assíncronos** ("agentes secundários").

---

## 1. Avaliação honesta do experimento 3D (HEX-7..9)

### O que funcionou e FICA
- **Motor de regras puro (HEX-1..6)** — coordenadas, facing, LoS, cobertura, IA posicional,
  84+ testes. É **agnóstico de render**: dirige o 2D igualzinho dirigia o 3D. Intocado.
- **Canvas 2D (HEX-2)** — funcionou de primeira, alinhamento perfeito entre desenho e toque
  (mesma matemática nas duas direções), iteração rápida.
- **UX recém-adicionada** — hexes válidos em verde, aviso "muito longe", halos, painel de
  combate embaixo da cena. Tudo migra pro novo canvas.

### O que o 3D custou (e por que sai)
- **6 bugs só descobertos no aparelho**: cena vazia (câmera degenerada), nodes órfãos
  (construtor vs Composable do SceneScope), leak de MaterialInstance, modelos minúsculos,
  walk-cycle rodando parado, halos desalinhados do overlay 2D.
- **Ciclo de iteração caro**: ~8 min de build + instalar APK + teste manual por rodada.
- **O golpe fatal — visto de CIMA, 3D não entrega nada**: um modelo 3D em câmera ortográfica
  top-down vira uma "bolha" sem leitura (o Duck parecia uma bolinha amarela). Um retrato 2D
  bem recortado carrega MUITO mais identidade visual — e é o padrão consagrado de mesa.
- **Veredito**: o código 3D (`HexScene3D.kt`) fica **parado como legado/experimento** atrás da
  flag `modoTaticoHex3D` (deprecated, não deletado — o SceneView continua em uso pelos dados 3D).

---

## 2. Infra existente que o plano REUSA (nada disso precisa ser criado)

| Peça | Onde | Papel no plano |
|---|---|---|
| Retrato do jogador | `Personagem.imagemPersonagemUri` / `OriginalUri` (filesDir/portraits/) | Vira o token do herói |
| Recorte inteligente | `ImagemPersonagemStore` (ML Kit Face Detection enquadra o rosto) | Reusar para recortar o token CIRCULAR centrado no rosto |
| Geração de imagem | `GeminiImageService.gerarRetrato` (gemini-3.1-flash-image, ~$0.067/img) | Gatilhos de inimigo e fundo |
| Grade 2D | `HexCanvas.kt` (HEX-2) + helpers `internal` | Base do novo canvas de tokens |
| Motor de regras | `domain/combat/hex/` (HEX-1..6) | Dirige tudo, sem mudança |
| Estado tático | `HexTaticoState` (hexes válidos, aviso, seleção) | Ganha campo de imagem por token |

---

## 3. Lotes

### TOK-1 — Token de imagem no Canvas 2D (fundo cinza) 🎯 PRIMEIRO
1. **`TokenImageStore`** (novo, `data/storage/`): dado o retrato do personagem, recorta um
   bitmap **CIRCULAR** centrado no rosto (reusa a detecção do `ImagemPersonagemStore`),
   salva cache em `filesDir/tokens/heroi.png`. Regenera quando o retrato muda.
2. **`HexCanvasTatico`** (evolução do `HexCanvasDemo` em `ui/saga/`):
   - Token do herói = imagem circular com **borda azul** + setinha de facing na borda.
   - Inimigo sem imagem = círculo **vermelho + inicial** (fallback atual — nunca quebra).
   - Fundo cinza (`COR_GRADE_FUNDO` atual). Halos verde/amarelo e aviso migram do 3D.
   - Animação de movimento: `animateFloatAsState` nas coordenadas de tela (trivial no 2D).
3. **Roteamento**: modo tático usa o novo canvas. Flag `modoTaticoHex3D` deprecated
   (Switch some da config; quem tinha ligado cai no 2D novo).
4. Testes puros do recorte/estado + build + revisão adversarial + commit.

### TOK-2 — Gatilho: tokens de inimigos gerados (agente secundário)
1. No `iniciar_combate`, para cada **TIPO** de inimigo (ex.: `goblin`, `orc_bruto`):
   - Cache hit em `filesDir/tokens/inimigos/{tipo}.png` → usa direto.
   - Miss → **corrotina em background** chama `GeminiImageService` com prompt derivado do
     nome/descrição ("retrato de busto de um goblin feroz, fantasia medieval, vista frontal,
     fundo neutro escuro, estilo pintura digital").
2. Enquanto gera → fallback círculo+inicial; quando chega → recompose troca a imagem.
3. **Cache por TIPO, não por instância** — 3 goblins = 1 imagem (custo sob controle).
4. Falha de rede/geração → fica no fallback, **nunca bloqueia o combate**.

### TOK-3 — Gatilho: fundo do cenário gerado
1. Quando o Narrador estabelece/troca a CENA (conceito já existe — "Cena: O Coliseu de Ferro"),
   gatilho assíncrono gera fundo top-down ("vista aérea do chão de uma arena de gladiadores,
   areia, marcas de luta, iluminação dramática").
2. Cache em `filesDir/cenarios/{campanhaId}_{hashDaCena}.jpg`.
3. No canvas: fundo desenhado sob a grade com **scrim escuro** por cima (grade e tokens
   continuam legíveis). Fundo cinza permanece como fallback eterno.

### TOK-4 — Ponte CombatSession ↔ grid (combate REAL na grade)
**Diretiva do usuário (11/jul):** revisitar o `Combate.md` e AUTOMATIZAR no grid as ações que hoje
são botões abstratos de faixa — substituindo como estão. Itens do audit marcados "FORA DO ESCOPO
por falta de grade de hexágonos" ficam DESBLOQUEADOS (a grade existe e os módulos HEX-1..6 já
implementam a mecânica).

1. **Estado tático REAL**: `HexCombatSync.projetarSetupInicial` no `iniciar_combate` projeta os
   combatentes do encontro na grade; tokens com id/nome do bestiário (imagem TOK-2 pela chave
   certa), **anel de HP** e nome sob o token. Estado sai do demo e passa a ser dirigido pelo
   `SagaCombatController`.
2. **Mover do herói pelo grid** (substitui o botão de faixa): tocar hex → valida custo contra o
   **Deslocamento real** (postura reduz — Lote 400); a distância nova alimenta
   `encounter.distanciaAoHeroi`. Hexes alcançáveis destacados em verde (raio = deslocamento).
3. **NPC move pela IA posicional** (`HexTaticaNpc`, HEX-5): flanquear/kite/recuar refletidos no
   grid; `HexPortabilidade.aplicarNovaDistancia` sincroniza quando o motor muda distância.
4. Revalidar defesa por timing no fluxo 2D.

### TOK-5 — Ações espaciais do Combate.md (substituições regra-a-regra)
Mapa das ações automatizáveis pelo grid (item do audit → como fica):
- **Alcance da arma C/1/2/3** (Combate.md "Alcance") → tocar no inimigo só ataca se
  `HexAtaqueAtravesHex` valida; fora de alcance → oferece Avançar-e-Atacar. −4 através de hex
  de inimigo; aliado livre (MB p.389).
- **Passo de 1m** (era ⏸️ "posicionamento em hexágono") → manobras com passo movem 1 hex real.
- **Espaçamento/ocupação** (era ⏸️) → grid já impede 2 tokens no hex; vira regra oficial.
- **Evadir** (era ⏸️ "sem bloqueio de hexágono a vencer") → atravessar hex de oponente via
  Disputa de DX; Obstrução (AM p.101, HEX-6) como contra.
- **Distância / Velocidade do alvo** (à distância) → penalidade pela distância REAL em hexes
  (1 hex = 1 m) em vez da faixa abstrata.
- **Cobertura/Superpenetração** (era ⏸️ "exige posicionamento e linha de tiro") →
  `HexCobertura` (HEX-6): LoS bloqueada, −2 parcial.
- **Facing/flanco/costas** (MB p.389-390) → `HexRegrasFacing` (HEX-4) ajusta as defesas do
  herói e do NPC pela posição real do atacante.
- **Empurrão/Projeção/Encontrão** → knockback em HEXES visível no grid
  (`HexPortabilidade.aplicarNovaDistancia`); velocidade relativa do Encontrão = hexes percorridos.
- **Retirada** (defesa) → recuo de 1 hex animado no grid.
- **Agarrar/luta agarrada** → exige adjacência (alcance C = mesmo hex/adjacente).
- **Aguardar/Interromper Investida** → gatilho dispara quando o inimigo ENTRA no alcance em hexes.
- **Manter à distância** (AM p.101) → `HexManterADistancia` (HEX-6) cobra os 2 MV extras no grid.
Fatiar TOK-5 em sub-lotes se crescer (alcance+distância primeiro; facing/cobertura depois;
Evadir/Aguardar por último).

---

## 4. Decisões de design (recomendação embutida)
- **Formato do token: CÍRCULO com borda colorida** (padrão VTT). Hex-crop é possível mas
  menos clássico e complica o recorte — círculo primeiro.
- **"Agente secundário" = corrotina + fila simples no app** chamando o `GeminiImageService`
  existente. Não precisa de novo backend/modelo; o gatilho é disparado pelas tools do
  Narrador (`iniciar_combate`, troca de cena).
- **Custo**: ~$0.067/imagem; cache agressivo por tipo/cena mantém o gasto em centavos por
  campanha.

### TOK-6 — MODO JOGO: tela cheia imersiva + redesign do combate
**Diretiva do usuário (12/jul, após teste no aparelho FÍSICO):** o combate FUNCIONA, mas os
hexágonos ficam pequenos demais pro dedo (~20px; o mínimo Android é 48dp), as imagens dos tokens
ficam invisíveis nesse tamanho, o fundo gerado não apareceu, e a tela desperdiça espaço com
cabeçalho da ficha + abas. Visão do usuário: **em campanha, o app vira um JOGO em tela cheia** —
sem menus superiores nem abas; só um "X" no canto superior direito pra sair.

**Diagnóstico técnico do teste:**
- Hexes pequenos: a grade raio 7 (15 hexes de diâmetro) é desenhada INTEIRA num canvas de ~1/4
  da tela. No mouse do emulador funciona; no dedo, não.
- Imagens "ausentes" nos tokens: elas provavelmente carregam — são É invisíveis a ~15px.
- Fundo ausente: BUG — o canvas do combate REAL usa `fundoCenaCacheado` (cache-only); se a
  geração ainda não terminou quando o canvas compôs, fica null pra sempre (as keys do
  LaunchedEffect não mudam quando o arquivo aparece). O demo usa `obterFundoCena` (espera no
  Mutex e recompõe) — o real tem que usar o mesmo caminho.
- Poluição: labels de coordenadas (q,r) em todos os hexes.

**TOK-6a — Modo Jogo + câmera + fixes:**
1. `hideAppChrome` estendido: `selectedTitle == "Saga" && sagaCampanhaAtiva != null` → some
   cabeçalho da ficha, PontosBar e bottom bar (REUSA a infra do VTT legado). **X flutuante** no
   canto superior direito → `sagaSair()` (volta ao chrome normal).
2. **Câmera do canvas**: enquadrar o BOUNDING BOX dos combatentes + 2 hexes de margem em vez da
   grade inteira — hexes 3–4× maiores, auto-zoom conforme a luta se espalha/concentra.
3. Labels de coordenadas REMOVIDOS (viram flag interna de debug).
4. Fix do fundo no combate real (obterFundoCena com geração/espera, igual ao demo).

**TOK-6b — AÇÕES NOS TOKENS (redesenhado com o usuário, 12/jul, após teste do 6a):**
Feedback do teste: a câmera abre DEMAIS ao selecionar o herói (enquadrar TODOS os alcançáveis
de deslocamento 5+ deixa os hexes pequenos de novo). Nova visão do usuário: o mapa É a
interface — grade em ~2/3 da tela; as manobras moram NOS TOKENS (menu em leque/carrossel
translúcido sobre o mapa); os cards de vida somem (barra de HP sobre a cabeça do token).

**Mapeamento GURPS — qual ação mora em qual token:**
- Token do JOGADOR (manobras SOBRE SI, sem alvo — MB p.363-366):
  Aguardar (+Interromper Investida) · Preparar/Trocar arma · Mudar Postura · Defesa Total
  (Aumentada/Dupla) · Concentrar · Não Fazer Nada · Desvencilhar-se (só quando agarrado).
  MOVER continua sendo o toque no hex verde (não vira botão).
- Token do INIMIGO (ações DIRECIONADAS, têm alvo — MB p.364-371):
  Ataque · Ataque Total (Determinado/Duplo/Forte/Fintar) · Ataque Dedicado · Ataque Defensivo ·
  Avançar e Atacar · Golpe Rápido · Fintar · Avaliar · Apontar (à distância) · Agarrar ·
  Empurrão · Encontrão · Derrubar/Imobilizar/Estrangular/Chave/Mata-Leão (só com alvo agarrado) ·
  Fogo de Retenção (CdT 5+).
  Os botões exibidos = interseção com `manobrasHeroi`/`alvos` do CombatUiState (o motor JÁ
  filtra por distância/alcance/estado — a UI radial só reorganiza o que já é legal).
  Obs. GURPS: Concentrar é manobra sobre SI (mantém magia/tarefa mental) — fica no jogador,
  embora o usuário o tenha listado no inimigo.

**Sub-fatias:**
- TOK-6b-1 — Grid 2/3 da tela + barra de HP sobre o token (substitui o anel e os CARDS de vida
  do tracker, que somem no modo tático; condições viram mini-ícones na barra) + FIX da câmera
  (piso de toque ~40dp por hex; enquadrar alcançáveis SÓ até esse piso) + PAN por arrasto
  (offset manual somado ao centro da câmera; reset quando a câmera-alvo muda de verdade).
- TOK-6b-2 — Menus radiais nos tokens: tocar no HERÓI abre o leque de manobras-de-si
  (translúcido, em arco/carrossel sobre o mapa); tocar num INIMIGO abre o leque ofensivo;
  sub-diálogos (local do golpe, modo do Ataque Total, enganoso/telegráfico) re-estilizados
  translúcidos por cima do mapa; painel clássico de manobras SOME no modo tático (fica só a
  linha da arma empunhada + Trocar arma).

## 5. Registro de execução
- [x] ✅ TOK-1 — token de imagem + canvas novo + roteamento (10/jul/2026, commit 4fb8977 — TokenImageStore com recorte por rosto + HexCanvasTatico + hexes verdes/aviso/animação migrados do 3D + Switch 3D removido + 15 testes puros)
- [x] ✅ TOK-2 — gatilho de inimigos (10/jul/2026 — GeminiImageService.gerarImagem genérico + obterTokenInimigo com cache por TIPO + Mutex dedup + salvarPngAtomico + gatilho fire-and-forget no iniciarCombate + canvas cache-first com geração on-demand no demo + 10 testes puros)
- [x] ✅ TOK-3 — gatilho de fundo (10/jul/2026 — CenarioImageStore com chave por conteúdo FÍSICO da cena (humor fora, achado da revisão adversarial), gatilho pós-turno no delegate, canvas cover+scrim, 12 testes puros)
- [x] ✅ TOK-4 — combate REAL no grid (11/jul/2026 — HexSetup + heroiMoveTatico + ponte no controller + HexCanvasCombateReal com anel de HP; MOVER de faixa substituído pelo toque no hex; revisão adversarial: 5 achados corrigidos incl. anel-alvo no HexPortabilidade e BFS anti-atravessar; 18 testes puros novos)
- [x] ✅ TOK-5a — facing/através-de-hex/Retirada REAIS (11/jul/2026 — `CombatSession.PosicaoBridge` opcional: FLANCO −2/COSTAS anula nos DOIS sentidos (card de defesa ajustado via HexRegrasFacing com BD do escudo; esquiva passiva incluída), −4 atacando através de hex de inimigo, Retirada recua 1 hex real e atualiza as distâncias; +4 testes de integração com bridge fake; regressão zero sem bridge)
- [x] ✅ TOK-5b — IA posicional do NPC + manter à distância (11/jul/2026 — `moverNpcNaGrade` itera HexTaticaNpc vizinho-a-vizinho: o goblin flanqueia/kita/recua de verdade; MOVER_E_ATACAR sem alcance consome o turno; fuga pela borda da grade; Interromper Investida mantém o oponente à distância via Disputa ST/Vontade−3. DEFERIDOS documentados: cobertura (sem obstáculos na grade), Evadir (BFS conservador cobre), Aguardar-por-alcance (já coberto pós-TOK-4))
- [x] ✅ TOK-6a — Modo Jogo + câmera + fixes (12/jul/2026 — tela cheia em campanha via hideAppChrome estendido (vertical, sem o landscape do VTT), header 1-linha com X de sair, câmera enquadrando combatentes+alcançáveis (achado da revisão: verdes fora da viewport eram intocáveis), labels de coordenadas fora, fix do fundo cache-only no combate real, 9 testes puros da câmera)
- [x] ✅ TOK-6b-1 — grid dominante + barra de HP no token (cards de vida somem no modo tático) + piso de toque 40dp na câmera + pan por arrasto com clamp (12/jul/2026 — `cameraEfetiva` pura, `condicoesIcones` 🩸💫🤼😮‍💨⬇, `CombatePainel(mostrarTracker=false)`, 4 testes novos)
- [x] ✅ TOK-6b-3 — GRID PROTAGONISTA / layout pós-teste (14/jul/2026 — Trocar arma virou chip do herói (`SubDialogoTrocarArma`) e o painel de arma fixo do rodapé foi REMOVIDO; caixa do Narrador subiu pro topo do grid (fina); status (Defenda-se!/fim/inimigos agindo) virou overlay `CombateStatusTatico` que só aparece quando exige atenção; feed encolhe no tático; câmera com margem 1,1 hex + piso de toque 30dp ao mover → o deslocamento cabe; menu do herói no topo (deixa os hexes de movimento livres), do inimigo embaixo)
- [x] ✅ TOK-6b-2 — menus de manobra NOS TOKENS (14/jul/2026 — carrossel translúcido: tocar no HERÓI = `menuTaticoHeroi` (manobras sobre si); tocar no INIMIGO = `menuTaticoInimigo` com o alvo já fixado pelo toque (ações só-de-alvo disparam direto, as com parâmetro reusam os sub-diálogos com `listOf(alvo)`); Mover continua o hex verde; painel clássico → `PainelArmaTatico` (só a arma + dica); `limparSelecaoTatica` fecha o menu. Bug de regra corrigido na varredura: grade driblava a luta agarrada (herói atordoado/preso movia sem Desvencilhar). **Revisão adversarial: 2 achados ALTA REAIS (soft-fail "perde o turno") corrigidos** — filtros do menu agora batem EXATO com as precondições do motor: Agarrar/Derrubar/Empurrão exigem adjacência (não alcance de arma), Encontrão usa alcance-movendo, Imobilizar exige agarrado E no chão; +2 textos de menu vazio. 9 testes de `MenuTaticoTest` + 13 de `CameraHexTest`)
