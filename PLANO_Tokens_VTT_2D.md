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

### TOK-4 — Polimento + combate real
1. Tokens dirigidos pelo `CombatSession` real (posições via `HexCombatSync`/`HexPortabilidade`).
2. Anel de vida (HP%) ao redor do token do inimigo; nome embaixo.
3. Revalidar defesa por timing no fluxo 2D.

---

## 4. Decisões de design (recomendação embutida)
- **Formato do token: CÍRCULO com borda colorida** (padrão VTT). Hex-crop é possível mas
  menos clássico e complica o recorte — círculo primeiro.
- **"Agente secundário" = corrotina + fila simples no app** chamando o `GeminiImageService`
  existente. Não precisa de novo backend/modelo; o gatilho é disparado pelas tools do
  Narrador (`iniciar_combate`, troca de cena).
- **Custo**: ~$0.067/imagem; cache agressivo por tipo/cena mantém o gasto em centavos por
  campanha.

## 5. Registro de execução
- [ ] TOK-1 — token de imagem + canvas novo + roteamento
- [ ] TOK-2 — gatilho de inimigos
- [ ] TOK-3 — gatilho de fundo
- [ ] TOK-4 — polimento + combate real
