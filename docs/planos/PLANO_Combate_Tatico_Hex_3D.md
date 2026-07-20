# PLANO — Combate Tático em Hexágonos (3D) para o modo Saga

**Decisão (2026-06-24):** o usuário escolheu **combate tático em grade de hexágonos** (tipo XCOM / Baldur's
Gate 3), com render 3D, mantendo TODA a mecânica em GURPS. Objetivo: desbloquear as regras **posicionais**
que o modelo de FAIXAS abstratas do Saga não cobre (orientação/facing, alcance contado, movimento, flanco,
cobertura, "manter à distância", comprimento de arma, vários alvos por posição).

> Status: PLANO (nada implementado ainda). É um **pilar novo**, escopo de semanas/meses — não um lote.

---

## Princípio-mestre de sequenciamento
**Motor de regras primeiro (2D barato), 3D depois.** O 3D *parece* a parte cara, mas é a casca. O valor e a
dificuldade estão no **motor posicional** (posição, distância em hex, linha de visão, orientação, movimento).
Construir e validar isso numa grade 2D (Compose Canvas) ANTES de gastar esforço/arte em 3D.

## O que JÁ existe e será reaproveitado
- **Motor 3D:** `SceneView 3.0.0` (sobre Google Filament), provado nos dados 3D (`Dice3DScene.kt`: câmera,
  luz, carga de `.glb`, vários objetos). Render de herói/inimigos/terreno = mesmo motor, outros modelos.
  **Esta é a fundação de render reaproveitável.**
- **VTT (LEGADO — decisão do usuário 2026-06-24: não será mais usado como VTT):** `TabVtt.kt` NÃO é alvo de
  integração. Vale só como **doador de código** — o encanamento de imagem de token/mapa (seleção, hash sha1,
  resolução de payload, cache local de imagem) pode ser SALVADO para os tokens do tático. A renderização em
  **WebView NÃO é reaproveitada** (o tático usa Compose Canvas → Filament, não WebView).
- **Motor de combate Saga:** `CombatSession`/`CombatEncounter`/`CombatResolver`/`HitLocationRules`/
  `InjuryRules`/`CombatActions`/`NpcCombatBrain` — toda a resolução (ataque→defesa→dano→ferimento) é REUTILIZADA.
  Só a **camada de posição** muda (hoje `distanciaAoHeroi` é um número/faixa; vira posição em hex).

## Arquitetura proposta
- **Sistema de coordenadas:** hex **axial/cube** (padrão da indústria). Domínio puro/testável `HexGrid`:
  posição, distância em hex, vizinhos, linha de visão (LoS), **6 orientações** (facing).
- **Escala GURPS:** 1 hex = 1 metro (≈1 jarda), como o tabuleiro do Módulo Básico.
- **Integração:** trocar a distância-única do `CombatEncounter` por **posição em hex por combatente**.
  Distância vira hex-distância; alcance/reach da arma vira nº de hexes; facing dá os modificadores de
  ataque pela retaguarda/flanco. O resto da resolução continua igual.
- **Coexistência:** manter o modo FAIXAS atual como fallback OU atrás de uma **feature flag**, para não
  regredir o combate que já funciona no aparelho.

---

## Fases (roadmap)

### Fase 0 — Escopo e fundação de dados
- Confirmar: hex axial, escala 1 hex = 1 m, NT realista (sem cinematográfico).
- Decidir a flag de modo (faixas ↔ tático) e onde o Narrador escolhe o terreno.

### Fase 1 — Motor de hex (2D, Kotlin puro, SEM render)
- `HexGrid` + `HexCoord` (axial/cube): distância, vizinhos, range(n), linha reta, **LoS** (bloqueio por
  obstáculo), **facing** (frente/flanco/costas a partir de duas posições + orientação).
- Testes unitários completos (puro, sem Android). **Entregável: motor posicional provado.**

### Fase 2 — Combate na grade 2D (Compose Canvas)
- Desenhar hexágonos + tokens 2D (salvar o encanamento de imagem de token do VTT legado, se ajudar), seleção e **movimento por toque**.
- Plugar o motor de regras existente com posição em hex: alcance em hexes, **passo + ataque**, **Avançar e
  Atacar**, **Recuo**, **Aguardar/Interromper** por posição, orientação → bônus de ataque pelas costas/flanco.
- **Entregável: combate tático jogável em 2D**, regras posicionais validadas.

### Fase 3 — IA tática do NPC
- Estender `NpcCombatBrain` para **mover-se na grade**: aproximar, **flanquear**, manter distância (kite de
  arqueiro), buscar **cobertura**, focar alvo. Hoje a IA decide manobra; ganha decisão de POSIÇÃO.

### Fase 4 — Regras posicionais 🔴 (MB + Artes Marciais) que o hex desbloqueia
- Orientação/facing, alcance/reach reais, **cobertura**, **vários alvos por posição/linha**, "manter à
  distância", corredores de carga, e (quando o `NpcStats` ganhar peso/comprimento de arma) "quem golpeia
  primeiro" por comprimento. Cruzar com o `docs/fonte-regras/Combate.md` (✅ fechado) e o `docs/pendencias/Artes_Marciais_Regras_Combate.md`.

### Fase 5 — Render 3D (Filament/SceneView)
- Trocar o Canvas 2D por **cena 3D**: terreno/grade em 3D, modelos `.glb` de herói/inimigos, **câmera tática**
  (orbitar/zoom/pan), realce do hex selecionado, animações básicas (idle/ataque/dano). Reaproveita a stack dos
  dados 3D + ambientes (a cena já lê bioma/humor do Narrador → ambientação automática).

### Fase 6 — Polimento e sabor
- Opcional "Clair Obscur": defesa por **timing** (mini-janela de parry/esquiva) sobre a rolagem GURPS.
- Efeitos, áudio, partículas; integração fina com o feed narrado do Narrador.

---

## Integração com o Narrador (Saga)
- `iniciar_combate` passa a **posicionar tokens** numa grade (terreno descrito pelo Narrador → grade gerada,
  ou grade padrão por bioma). O bridge `CombatBridge` continua válido; só o estado de posição fica mais rico.
- `acao_npc` segue reportando estado factual; ganha posição/orientação.

## Riscos e custos honestos
- **Pilar novo, meses.** Não cabe em um lote. Marcos incrementais e verdes a cada passo.
- **Regressão:** manter o modo faixas como fallback/flag até o tático estar sólido.
- **Arte 3D:** modelos animados de herói/inimigos = pipeline de asset (custo de arte). Começar com
  tokens/figuras simples (billboards) e evoluir.
- **Performance no aparelho:** Filament é eficiente, mas grade + vários modelos + sombras exige cuidado.
- **Dados faltantes:** algumas regras (comprimento/peso de arma do NPC) ainda dependem de campos novos no
  `NpcStats` — já mapeado no `docs/pendencias/Artes_Marciais_Regras_Combate.md`.

## PRIMEIRO PASSO concreto (provar barato, sem 3D nem arte)
**Fase 1 + início da Fase 2:** o motor `HexGrid` puro (com testes) + uma tela Compose Canvas que desenha a
grade, posiciona o herói e 1 inimigo, e resolve UM ataque corpo-a-corpo com **alcance e movimento por hex**.
Pequeno, build verde, commitável — prova o conceito e o sequenciamento antes de qualquer investimento em 3D.

> ⚠️ Gate honesto: este pilar só deve abrir DEPOIS de o usuário revalidar no aparelho o combate atual
> (Lotes 419–422). Não misturar a estabilização do que existe com a construção do que é novo.
