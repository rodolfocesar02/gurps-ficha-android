# PENDÊNCIAS — o mapa único do que falta

> **Por que este arquivo existe.** No teste de 18/jul o usuário disse: *"parece que to testando coisas
> que estão na fila pra ser integradas, e nos testes no aparelho estão aparecendo como bug, mas na
> verdade não foi adicionado mecanicamente"*. Ele estava certo. A informação existia, mas espalhada
> entre `MAGIA_DEFERIDOS.md`, `BURACOS_SCHEMA_MAGIAS.md`, `PROGRESS.md` e os planos — ninguém
> conseguia ver o todo. Este é o índice único.
>
> **Regra de manutenção:** ao fechar um lote, atualize aqui também. Se um item sair da lista, diga em
> qual lote saiu.

---

## 1. O número que explica quase tudo

**87 das 879 magias (9,9%) são executadas mecanicamente pelo motor.** O resto é narrado **por
projeto** — não por bug.

| Efeito | Total | O motor executa | Observação |
|---|---:|---:|---|
| narrado | 379 | 0 | narrado por definição |
| buff | 179 | **23** | os outros 156 não têm número no livro (`semNumero`) |
| ambiente | 110 | 0 | clima, luz, criar matéria |
| informacao | 82 | 0 | adivinhação, localizar, detectar |
| controle | 65 | 0 | dominar, comandar, mover objeto |
| dano | 40 | **40** | ✅ completo |
| condicao | 21 | **21** | ✅ completo |
| cura | 3 | **3** | ✅ completo |
| **Total** | **879** | **87** | |

**Consequência prática:** ao testar uma magia sorteada, a chance de ela ser mecanizada é ~10%.
É esperado topar com efeito narrado quase sempre.

### Como saber na hora se é bug ou não-implementado

Abra o Logcat filtrando por `tag:Saga_Combate`:

| O que aparece | Significa |
|---|---|
| `Efeito narrado pelo Mestre` | **Não é bug.** É uma das 792 narradas. |
| Números (dano, RD, teste, condição) | É mecânica. **Se o número estiver errado, é bug de verdade.** |
| `combate de teste RECUSADO: <motivo>` | O combate não abriu — o motivo está ali. |

⚠️ **Exceção que já mordeu:** a Morte Candente mostrava "narrado" **e era bug** — ela tem mecânica
curada, mas o caminho do Toque não a aplicava (corrigido no MEC-21). Ou seja: "narrado" numa magia
cujo efeito é `dano`/`condicao`/`cura` **é suspeito**.

---

## 2. NÃO IMPLEMENTADO — vai parecer bug quando você testar

### 2.1 Prioridade alta (bloqueiam teste de magias inteiras)

| # | O que falta | Afeta | Origem |
|---|---|---|---|
| P1 | **Tique por turno** — dano recorrente com teste a cada turno | Morte Candente, Morte Putrefata, Chuva de Ácido/Fogo/Pedras, Nuvem de Faíscas, Géiser, Mau Cheiro | auditoria #2 e #11 |
| P2 | **Manter magia com efeito continuado** — a manutenção cobra PF, mas o efeito não persiste | todas as de duração | MAGIA_DEFERIDOS |
| P3 | **Efeito de buff aplicado de verdade** — Escudo não dá +DB, Armadura não dá +RD | **156 dos 179 buffs** | MAGIA_DEFERIDOS |

> P1 e P2 são o **mesmo mecanismo** (zona/efeito que persiste e tica). Um lote resolve os dois.
> ⚠️ P1 tem parte de UI: uma zona de dano precisa ser **desenhada na grade**, senão o jogador perde
> PV sem causa visível — exatamente o bug que o usuário já reportou uma vez.

### 2.2 Regras de magia específicas

| # | O que falta | Afeta |
|---|---|---|
| P4 | Bandas de distância do **Lampejo** (≤10m / 11–25m / 26m+ com efeitos diferentes) | Lampejo |
| P5 | **Respingo do Relâmpago Explosivo** — é `projetil` e o ramo acerta 1 alvo só | Relâmpago Explosivo |
| P6 | **Precisão (Acc)** do projétil — só valeria com a manobra Apontar, que não existe no fluxo de conjuração | 12 magias de projétil |
| P7 | **RD natural × armadura** do Toque Candente — o bestiário tem um campo `rd` único, sem separar | Toque Candente |
| P8 | Degrau de custo dobrado (2d-2) | Chuva de Fogo, Chuva de Pedras |
| P9 | Projeção/derrubada dos Jatos (`1d de projeção por energia`, Géiser empurra + DX-5) | Jatos, Géiser |
| P10 | Raio mínimo de 2m | Nuvens, Sono Coletivo |
| P11 | Projétil **carregado em vários turnos**; magia **cerimonial**; **cajados**; modificadores de longa distância para Informação | diversas |
| P12 | **Conjurar no modo FAIXAS** — o chip 🔮 só existe no grid tático | todo combate sem grade |

### 2.3 Combate — "fora do escopo" cuja justificativa CADUCOU

O `Combate.md` tem 13 itens marcados *fora do escopo*. **Cinco foram excluídos porque "não há grade
de hexágonos"** — e a grade foi construída depois (HEX-1..9 + VTT 2D). Conferido item a item **no
código** (não só no documento), porque o `Combate.md` está desatualizado em relação à realidade:

| Item | Justificativa original (hoje falsa) | Situação REAL no código |
|---|---|---|
| **Passo** | *"o Saga usa faixas abstratas"* | ✅ **Já implementado** (35 ocorrências no motor). Só o `Combate.md` está desatualizado — corrigir a marcação. |
| **Passando por Outros** | *"sem grade de hexágonos"* | 🟡 **Parcial**: o BFS bloqueia hex de inimigo (conservador). Falta liberar atravessar **aliado** (MB p.389). |
| **Evadir** | *"no modelo de faixas o herói se move livremente"* | ❌ **Não implementado** — há TODO explícito em `HexSetup.kt:50`. |
| **Espaçamento** | *"abstraído no tracker de faixas"* | ❌ **Não implementado** (0 ocorrências). |
| **Superpenetração e Cobertura** | *"exige posicionamento"* | 🟡 **Parcial**: cobertura existe como **modificador situacional manual**; cobertura por terreno/posição e superpenetração não. |

Os outros 8 continuam legitimamente fora (perícias de Artes Marciais, cinematográfico, montaria).

> ⚠️ **Lição registrada:** a marcação do `Combate.md` não é confiável sozinha — o Passo aparece como
> "fora do escopo" e está pronto há lotes. Ao revisitar, confira o código.

### 2.4 Simplificações honestas (não são bugs, são aproximações documentadas)

- Tipo de dano do Projétil ≈ contusão (×1 de ferimento)
- Ataque Inato ≈ DX do herói (a perícia não existe na ficha)
- Vontade do NPC ≈ IQ (bestiário não tem Vontade)
- Manutenção ≈ metade do custo, quando o catálogo não traz o número exato
- **Aparar/Bloquear/escudo não são gateados por confisco de equipamento** (modelo de defesa app-wide)
- "+3 para resistir" além do 1/2D: o livro se contradiz entre duas seções; adotada a versão
  inequívoca (metade do dano **e** +3)

---

## 3. IMPLEMENTADO mas NÃO validado no aparelho

Aqui, se aparecer estranho, **é candidato a bug real** — vale reportar.

| Lote | O que faz |
|---|---|
| MEC-13 | Magia de objeto (Desintegrar, Fender…) não pode mirar criatura |
| MEC-14 | Explosão decai com a distância (÷ 3 × distância) |
| MEC-15 | 1/2D (metade do dano) e Alcance Máximo do projétil |
| MEC-17 | Condição com **prazo** — antes cegueira era eterna |
| MEC-18 | Condição exige **teste** (Jato de Som atordoava sem teste); cegueira dura por energia |
| MEC-19 | Fuga do gelo por teste de ST |
| MEC-20 | "Causa dano" só aparece em magia que pode causar dano (40, não ~800) |
| MEC-21 | **Toque aplica efeito** — antes nenhuma magia de toque fazia nada |
| TESTE-NPC | Modos Normal / Congelado / Boneco |
| TESTE-SANDBOX | O combate de teste explica por que não abriu |
| LOG-1 | Combate visível no logcat |
| TESTE-C | Regra de movimento tático testada no código real |
| TOK-6b-3 | Layout do grid protagonista |

---

## 4. Dívida técnica conhecida

- **Teste flaky pré-existente**: `NexusArcanoEngineLoteBGlobalTest.planejador_resolve_requisito_de_contagem_por_escola`.
  É o **único vermelho** de todos os gates. Está sendo corrigido em sessão separada.
- **Camada de ViewModel/UI sem teste automatizado.** Motivo estrutural: `SagaCombatController` exige
  `FichaViewModel`, que é `AndroidViewModel(Application)`, e `Application` não existe na JVM.
  **Robolectric foi testado e descartado** (18/jul): nenhum bug do app encontrado, ~15× mais lento,
  suporta só SDK 34 contra o alvo 35. O caminho aberto é `androidTest` no **celular físico** — a
  infraestrutura (`ui-test-junit4`) já está no `build.gradle`, sem uso.
- **Duas lições que já custaram bug**, ambas registradas:
  1. Teste que **copia** a regra em vez de chamá-la passa verde com o original quebrado (TESTE-C).
  2. Testar a regra ≠ testar que ela está **ligada** e é **alcançável** — o MEC-19 passou nos testes
     e nunca disparava em jogo, porque a magia é de Toque e o Toque não aplicava efeito (MEC-21).

---

## 5. Recomendação de ordem

1. **P1 + P2 juntos** (motor de tique/efeito persistente) — maior retorno: destrava Morte Candente,
   Morte Putrefata, Chuvas, Nuvens, Géiser e o "manter" de todas as magias de duração. Inclui UI.
2. **P3** (efeito de buff aplicado) — 156 magias saem de "narrado" para mecânica.
3. **2.3** (revisar os 5 itens de combate cuja justificativa caducou com a grade).
4. O resto de 2.2, por ordem de aparição nos testes.
