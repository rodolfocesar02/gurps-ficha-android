# Plano — Refatorar os arquivos grandes do combate (separar MOTOR de TELA)

**Decisão do usuário (22/jul):** só os arquivos que estamos mexendo agora. Os outros grandes
(`TabVtt`, `MestreIAGeneratorUseCase`, `GeminiLiveService`, `Personagem`, `TabRolagem`,
`CatalogLoaders`) estão **estáveis, sem bug**, e mexer neles só criaria testes e simulações novas
sem necessidade. **Não entram neste plano.**

**Princípio (pedido do usuário):** separar **regra/mecânica** de **tela/visual**. Código do motor não
deve depender de código de tela.

## O mapa dos grandes (medido 22/jul)

| Arquivo | Linhas | Tem teste? | No plano? | Motivo |
|---|---:|:---:|:---:|---|
| `domain/combat/CombatSession.kt` | 3.675 | sim | 🟡 talvez | motor PURO (0 imports de tela) e testado — grande, mas seguro. Só dividir se atrapalhar. |
| `viewmodel/delegates/SagaCombatController.kt` | 2.243 | **não** | ✅ **alvo principal** | o "garçom" entre tela e motor; **zero testes**; onde os bugs TOK-8/9/10 nasceram. |
| `ui/saga/CombatUi.kt` | 2.000 | não | ✅ secundário | tela do combate; **desenha, não decide** (checado: as "contas" são só exibição). Dividir por telas. |
| os outros 6 (>1000) | — | — | ❌ **fora** | estáveis, sem bug, de outras frentes. |

## Estado atual da separação (a boa notícia)

- **`CombatSession` já está limpo**: 0 imports de Android/Compose. O motor NÃO conhece a tela. Nada a fazer aqui do lado da separação.
- **`CombatUi` quase limpo**: os 7 trechos que pareciam "regra" são exibição de valor já calculado + comentários. Ele desenha.
- **O problema é o `SagaCombatController`**: 4 imports de tela + `Context` usado em 14 lugares, misturando três responsabilidades num arquivo só:
  1. **decidir** o que é permitido agora (pode mover? tem virada pendente? é meu turno?),
  2. **traduzir** magia da ficha para o motor (catálogo, classe, energia),
  3. **guardar estado de tela** (`mutableStateOf`, o que a UI observa).

O objetivo é tirar (1) e (2) para arquivos **puros e testáveis**, deixando no controller só (3) — a cola com a tela.

## Regra de ouro deste plano

**Não se refatora com segurança quem não tem teste.** Refatorar é mover código; sem rede, a única
forma de saber se quebrou é o usuário no aparelho — o oposto do que queremos. Por isso **cada
extração começa escrevendo o teste do comportamento atual**, e só então move o código. Se o teste
continua verde depois de mover, a refatoração preservou o comportamento.

Cada fase abaixo é **um lote**: gate verde nas 2 variantes, commit + push + bump, PROGRESS.

---

## Fase 0 — Rede antes de tocar (nenhum código de produção muda)
Estender a simulação de invariantes (`CombateInvariantesTest`, SIM-1) para exercitar os caminhos do
controller que hoje não têm rede: mover → virada → avançar turno; conjurar área por faixa; encolher
zona. Se algum invariante pega algo agora, é bug pré-existente — reportar, não esconder.
**Entrega:** mais invariantes, mesmo comportamento. **Risco:** nenhum (só teste).

## Fase 1 — Extrair as REGRAS DE DECISÃO do controller (`RegrasDeCombateTatico`, puro)
Mover para um arquivo puro (sem tela) as funções que **decidem**, hoje presas no controller:
`hexesAlcancaveisHeroi` (o que já checa `viradaFinalPendente`, condições, concentração),
a guarda de "é meu turno / mira pendente" de `sagaAoTocarHexTatico`. Elas viram funções que recebem
o estado e devolvem a resposta — testáveis na JVM, como já foi feito com `RegrasMovimentoTatico`.
**Isto fecha a lacuna que gerou o TOK-8.** O controller passa a só chamar essas funções.
**Entrega:** −200 a −300 linhas no controller, +1 arquivo puro testado.

## Fase 2 — Extrair a TRADUÇÃO catálogo→motor (`MagiaParaCombate`, puro)
`defDoCatalogo`, `classeDaMagia`, `energiaDaMagia`, `construirPerfilHeroi`, `construirAtaques` —
tudo que converte a ficha/catálogo em objetos que o motor entende. É lógica pura de tradução,
não precisa de tela nem de estado observável.
**Entrega:** −300 a −400 linhas no controller, +1 arquivo puro testado (pega a classe de bug MEC-42/43,
a cópia velha do catálogo).

## Fase 3 — Quebrar o `CombatUi` por TELA (só se ainda incomodar)
Dividir o arquivo de 2.000 linhas por assunto de tela: diálogo de conjuração, menu do token, painel
de faixas, overlays. Cada um num arquivo. **Sem lógica nova** — só recortar e mover Composables.
UI não tem teste unitário; a validação é o build compilar + teste no aparelho.
**Entrega:** 4-5 arquivos de ~400 linhas no lugar de um de 2.000. **Este é o único que PARA para teste no aparelho.**

## Fase 4 — `CombatSession`, só se necessário
Ele é grande mas puro e testado. Dividir por assunto (dano mágico / zonas / projétil / turno) só
se as fases anteriores mostrarem que vale. Baixa prioridade: risco baixo, incômodo baixo.

## Fase FINAL — Atualizar o `MAPA_DETALHADO.md`
Depois que os arquivos mudarem de forma, a seção 32 do mapa fica errada. Atualizar:
onde cada regra passou a morar, os arquivos puros novos, e o fato de o controller ter encolhido.
(Pedido explícito do usuário: este passo faz parte do plano.)

---

## Ordem e por quê
0 → 1 → 2 são **motor/lógica**, sem tela, com teste garantindo que nada quebra — **não param para
teste no aparelho**. A Fase 3 é a única de tela, fica por último e é a única que precisa da sua
validação. Assim a maior parte do ganho vem sem ida-e-volta.

## O que este plano NÃO faz
- Não toca nos 6 arquivos estáveis fora do escopo.
- Não muda comportamento nenhum — refatorar é reorganizar, não reescrever regra.
- Não adiciona feature de magia (isso é o A2, plano separado).
