# PLANO: FORJADOR ESPECIALISTA EM GURPS
**Data:** 17 de Maio de 2026
**Objetivo:** Transformar o Forjador num assistente agêntico que conhece a ficha, os catálogos e as regras de GURPS 4ª Ed. — no estilo "Mini-VSCode".

---

## LEITURA RÁPIDA — Estado atual dos Lotes

> **Atualizar esta tabela a cada Lote concluído.**
> Se o contexto compactar, leia esta tabela primeiro para saber onde parou.

| Lote | Nome | Status | Commit |
|:---:|---|:---:|---|
| B | Prompt GURPS real | ✅ Concluído | forjador(lote-b) · 636f4bb |
| A | IDs reais no JSON | ✅ Concluído | forjador(lote-a) · 89f26ca |
| D | Budget de pontos | ✅ Concluído | forjador(lote-d) · 395c055 |
| C | Validação pré-integração (UI) | ✅ Concluído | forjador(lote-c) · 0b5ad4b |
| E | Forjador Agêntico — Tools + GPS | ⬜ Pendente | — |

**Legenda:** ⬜ Pendente · 🔄 Em andamento · ✅ Concluído

---

## DIAGNÓSTICO — 3 Problemas Raiz

### Problema 1 — IA não conhece os IDs reais do catálogo
O `GOLD_TEMPLATE` pede `"nome": "Nome Exato"`. A IA chuta nomes como "Ataque Furtivo" ou "Sense Magic" que não existem. `integrarRespostaNaFicha()` usa `limparNome()` para fuzzy-match; se não acha, joga como Qualidade/Peculiaridade. Resultado: ficha cheia de itens sem mecânica real.

### Problema 2 — IA não sabe as regras de pontuação GURPS
O prompt não ensina o sistema de pontos. A IA distribui pontos de olho, sem respeitar:
- ST/HT = 10 pts/nível acima de 10; DX/IQ = 20 pts/nível
- Perícias: 1 pt = NH-base, 2 pts = +1, 4 pts = +2, 8 pts = +3
- Desvantagens: limite de -40 pts por personagem

### Problema 3 — IA confunde GURPS com D&D/Pathfinder
Erros comuns: "Ataque Furtivo", "Fúria Bárbara", "Spell Slots", "Level 5", "Pontos de Magia". O prompt atual não lista o que **não existe** em GURPS.

---

## LOTE B — Prompt GURPS Real
**Resolve:** Problemas 2 e 3 · **Complexidade:** Baixa (só prompt) · **Fazer primeiro** porque desbloqueia tudo

### Arquivo modificado
`app/src/main/java/com/gurps/ficha/data/network/MestreIAPromptsForjador.kt`

### O que mudar
Substituir o `PROMPT` atual inteiro com regras GURPS reais, blacklist D&D, arquétipos corretos, protocolo de cálculo obrigatório e template com campo `id`.

### Checkpoint do Lote B ✅
```
git commit -m "forjador(lote-b): reescreve prompt com regras GURPS reais e blacklist D&D"
```
Commit: **636f4bb**

---

## LOTE A — IDs Reais no JSON
**Resolve:** Problema 1 · **Complexidade:** Média · **Pré-requisito:** Lote B concluído

### Arquivos modificados
1. `app/src/main/java/com/gurps/ficha/data/network/MestreIAPromptsForjador.kt`
2. `app/src/main/java/com/gurps/ficha/data/network/MestreIAResponse.kt`
3. `app/src/main/java/com/gurps/ficha/domain/MestreIAGeneratorUseCase.kt`

### O que foi feito
- `GOLD_TEMPLATE` alterado: `"nome"` → `"id"` em vantagens/desvantagens/perícias/magias
- `MestreIAItem` ganhou campo `id: String? = null`
- `MestreIAItemDeserializer` lê ambos os campos `id` e `nome`
- `adicionarVantagem/Pericia/Magia` faz lookup por ID primeiro, fuzzy-match como fallback
- `gerarPromptComCatalogo()` injeta pares `"id (Nome)"` de todos os catálogos

### Checkpoint do Lote A ✅
```
git commit -m "forjador(lote-a): IDs reais no JSON, lookup direto por id no catálogo"
```
Commit: **89f26ca**

---

## LOTE D — Budget de Pontos
**Resolve:** Problema 2 (validação pós-IA) · **Complexidade:** Baixa-Média · **Pré-requisito:** Lote A concluído

### Arquivos modificados
`app/src/main/java/com/gurps/ficha/domain/MestreIAGeneratorUseCase.kt`

### O que foi feito
- `pontosIniciais` do personagem injetado no prompt via `gerarPromptComCatalogo(pontosIniciais=...)`
- Seção `=== BUDGET DO PERSONAGEM ===` adicionada ao prompt do Forjador
- `validarBudget(ficha)` calcula custo real de atributos + vantagens - |desvantagens| + perícias
- Alerta retornado se total > máximo: `"⚠️ Ficha usa ~$total pts (máximo: $max pts)"`

### Checkpoint do Lote D ✅
```
git commit -m "forjador(lote-d): validação de budget de pontos no prompt e pós-integração"
```
Commit: **395c055**

---

## LOTE C — Validação Pré-Integração (UI)
**Resolve:** UX — usuário vê o que vai ser aplicado antes de confirmar · **Complexidade:** Alta (nova tela) · **Pré-requisito:** Lotes A e D concluídos

### Arquivos criados/modificados
- **CRIADO:** `domain/MestreIAValidacaoReport.kt` — `ItemValidacao`, `StatusValidacao`, `RelatorioValidacao`
- **MODIFICADO:** `domain/MestreIAGeneratorUseCase.kt` — `gerarRelatorio()` e `validarBudget()`
- **MODIFICADO:** `viewmodel/delegates/FichaIADelegate.kt` — `relatorioValidacao` state exposto

### Estrutura de dados criada

```kotlin
data class ItemValidacao(
    val entrada: String,
    val idEncontrado: String?,
    val nomeEncontrado: String?,
    val status: StatusValidacao,
    val mensagem: String
)

enum class StatusValidacao { OK, FUZZY, FALLBACK, ERRO }

data class RelatorioValidacao(
    val vantagens: List<ItemValidacao>,
    val desvantagens: List<ItemValidacao>,
    val pericias: List<ItemValidacao>,
    val magias: List<ItemValidacao>,
    val totalOk: Int,
    val totalFallback: Int,
    val alertaBudget: String?
)
```

### UI do dialog (mockup)
```
╔══════════════════════════════════════╗
║  VALIDAÇÃO DA FICHA GERADA           ║
╠══════════════════════════════════════╣
║ ✅ aptidao_magica  → Aptidão Mágica  ║
║ ✅ espada_longa    → Espada Longa    ║
║ ⚠️ "Furto Sombrio" → virará Qualidade║
║ ❌ "Level 3"       → inválido GURPS  ║
╠══════════════════════════════════════╣
║   Total: 98 / 100 pts                ║
║  [APLICAR FICHA]    [CANCELAR]       ║
╚══════════════════════════════════════╝
```

### Checkpoint do Lote C ✅
```
git commit -m "forjador(lote-c): dialog de validação pré-integração com status por item"
```
Commit: **0b5ad4b**

---

## LOTE E — Forjador Agêntico: O "Mini-VSCode"
**Conceito:** O Forjador para de receber o JSON inteiro da ficha. Passa a **explorar a ficha com ferramentas**, como Claude Code explora um projeto com Read/Grep/Edit. · **Complexidade:** Alta · **Pré-requisito:** Todos os lotes anteriores concluídos

### Analogia VSCode → Forjador

| Claude Code | Forjador Agêntico |
|---|---|
| `Read file` | `ler_atributos()`, `ler_vantagens()` |
| `Grep` por símbolo | `buscar_vantagem(query)`, `buscar_magia(query)` |
| `Glob` por padrão | `listar_pericias()`, `listar_equipamentos()` |
| Arquivo aberto no editor | Personagem ativo |
| Sessão aberta no VSCode | `mestreIAChatHistory` persistente |
| Notificação "arquivo mudou" | `[SISTEMA] Adaga adicionada na ficha` |

### Assets disponíveis como "sistema de arquivos" do Forjador

O app tem **11 catálogos** nos assets — cada um é uma "pasta" que o modelo pode explorar:

| Asset | Conteúdo |
|---|---|
| `vantagens.v3.json` | Vantagens gerais (`id, nome, costKind, perLevel, tags`) |
| `vantagens_artes_marciais.v1.json` | Vantagens de Artes Marciais |
| `desvantagens.v2.json` | Desvantagens (`id, nome, custo`) |
| `pericias.json` | Perícias (`id, nome, atributoBase, dificuldadeFixa`) |
| `pericias_artes_marciais.v1.json` | Perícias de Artes Marciais |
| `magias2versao.json` | Magias (`id, nome, escola, preRequisitos, energia`) |
| `tecnicas.v1.json` | Técnicas (`id, nome, preRequisitoRaw, sourceBook`) |
| `armas_corpo_a_corpo.v1.normalized.json` | Armas CC (`id, nome, dano, grupo, st_min`) |
| `armas_fogo.v1.normalized.json` | Armas de fogo (`id, nome, dano, alcance, municao`) |
| `armas_distancia.v1.normalized.json` | Armas à distância |
| `armaduras.v2.json` + `escudos.v1.json` | Proteção (`id, nome, rd, peso, custo`) |
| `modificadores.v1.json` | Ampliações e Limitações |

### Mapa completo de Tools

**GRUPO 1 — Leitura da Ficha (como Read)**
```
ler_atributos()     → { st, dx, iq, ht, pv, pf, vb, pm, velocidade, am_nivel }
ler_vantagens()     → [{ id, nome, custo, nivel }]
ler_desvantagens()  → [{ id, nome, custo }]
ler_pericias()      → [{ id, nome, nh, pts_gastos, atributo_base }]
ler_magias()        → [{ id, nome, escola, pts_gastos }]
ler_tecnicas()      → [{ id, nome }]
ler_equipamentos()  → [{ nome, dano, peso, rd, custo }]
ler_historia()      → { historico, aparencia }
calcular_pontos()   → { atributos, vantagens, desvantagens, pericias, total, maximo }
```

**GRUPO 2 — Busca no Catálogo (como Grep)**
```
buscar_vantagem(query)    → vantagens.v3 + vantagens_artes_marciais
buscar_desvantagem(query) → desvantagens.v2
buscar_pericia(query)     → pericias + pericias_artes_marciais
buscar_magia(query)       → magias2versao (com escola + pré-req + energia)
buscar_tecnica(query)     → tecnicas.v1 (com sourceBook + pré-req)
buscar_arma(query)        → armas_cc + armas_fogo + armas_distancia
buscar_armadura(query)    → armaduras.v2 + escudos.v1
buscar_modificador(query) → modificadores.v1
```

**GRUPO 3 — Motores de Regra (Execução de lógica já existente)**
```
gps_magia(alvo_id)          → NexusArcanoAdapter.calcular() com estado atual do personagem
                               Retorna: cadeia completa, próximas ações, bloqueios, escolas
verificar_prereq_magia(id)  → checa se o personagem já pode aprender aquela magia agora
```

**GRUPO 4 — Escrita na Ficha (como Edit)**
```
adicionar_vantagem(id, custo?)
adicionar_desvantagem(id, custo?)
adicionar_pericia(id, pts)
adicionar_magia(id, pts)
adicionar_tecnica(id)
adicionar_equipamento(nome, peso, custo, dano?)
atualizar_atributo(nome, valor)
```

### GPS de Magias como Tool — exemplo de conversa real

```
Usuário: "Quero aprender Tempestade de Relâmpagos, o que preciso?"

Forjador chama: gps_magia("tempestade_de_relampagos")

NexusArcanoAdapter retorna:
  cadeia: "Relâmpago → Nuvem de Relâmpagos → Tempestade de Relâmpagos"
  magiasDoPersonagem: ["relampago"]  ← já tem!
  proximaAcao: "nuvem_de_relampagos"
  bloqueio: nenhum (AM e IQ suficientes)
  progressoCadeia: "1/3 magias da cadeia"

Forjador responde:
  "Você já tem Relâmpago! Próximo passo: Nuvem de Relâmpagos (pré-req: ✅).
   Após isso, Tempestade de Relâmpagos estará desbloqueada.
   Quer que eu adicione Nuvem de Relâmpagos agora?"
```

### Sessão Persistente — Eventos de Mudança

O `mestreIAChatHistory` já existe. Mas quando o usuário edita a ficha **fora do chat**, o modelo não sabe. Solução: injetar eventos de sistema no histórico automaticamente.

```kotlin
// FichaViewModel.kt — toda mutação na ficha dispara um evento
fun adicionarVantagem(def, custo, desc) {
    // ... código atual ...
    injetarEventoMestreIA(
        "[SISTEMA] Ficha atualizada: '${def.nome}' adicionada ($custo pts). " +
        "Total: ${calcularPontosGastos()}/${pontosIniciais} pts."
    )
}

fun adicionarPericia(def, pts) {
    // ... código atual ...
    injetarEventoMestreIA(
        "[SISTEMA] Ficha atualizada: '${def.nome}' NH ${calcularNH(def, pts)} " +
        "adicionada ($pts pts)."
    )
}

private fun injetarEventoMestreIA(texto: String) {
    mestreIAChatHistory.add(MestreIAChatMessage(role = "system", text = texto))
}
```

### Loop Agêntico para o Forjador

```
Usuário: "Que vantagem ficaria legal no meu guerreiro com machado?"

Iteração 1: IA chama ler_atributos() + ler_vantagens() + ler_pericias()
            → vê: ST 14, DX 13, Machado NH 15, Reflexos de Combate (já tem)

Iteração 2: IA chama buscar_vantagem("combate corpo a corpo machado guerreiro")
            → catálogo retorna: Maestria em Armas, Sentido de Combate, Resistência à Dor

Iteração 3: IA chama calcular_pontos()
            → vê: 82/100 pts gastos, sobram 18 pts

Iteração 4: IA responde fundamentado:
            "Com 18 pts livres e Machado NH 15, recomendo Maestria em Armas (id: maestria_em_armas,
             custo: 15 pts). Deixa seu Machado ainda mais letal. Adiciono na ficha?"
```

### Arquivos a Criar/Modificar no Lote E

```
NOVOS:
  domain/tools/ForjadorTools.kt           ← definição JSON dos 4 grupos de tools
  domain/tools/ForjadorToolExecutor.kt    ← execução: chama repository/viewModel/NexusArcano

MODIFICAR:
  domain/MestreIAGeneratorUseCase.kt      ← habilitar loop agêntico, remover JSON upfront
  domain/magias/NexusArcanoModoAlvoAdapter.kt ← expor calcular() ao executor
  viewmodel/FichaViewModel.kt             ← injetarEventoMestreIA() em cada mutação
  data/network/MestreIAPromptsForjador.kt ← prompt sem JSON upfront, instruções de tools
```

### Checkpoint do Lote E
```
git commit -m "forjador(lote-e): loop agêntico com tools, GPS de magias, sessão persistente"
```
Atualizar tabela: **E → ✅ Concluído** + hash.

---

## VERIFICAÇÃO — Como testar cada Lote

### Lote B
1. Abrir Mestre IA → Forjador → pedir "Crie um ladrão de 100 pontos"
2. JSON gerado não deve conter: "Ataque Furtivo", "Level", "Spell Slot", "Pontos de Magia"
3. Deve conter: ids como `furtividade`, `adaga`, `reflexos_de_combate`

### Lote A
1. Logcat (`MestreIA_Forjador`) não deve mostrar: `"Adicionando como Qualidade:"`
2. Ficha resultante deve ter vantagens com mecânica real (custo fixo, tags, etc.)

### Lote D
1. Pedir personagem de 50 pts → soma final deve ser ≤ 50
2. Se exceder → alerta no log e/ou na UI

### Lote C
1. Ao aplicar ficha gerada → dialog aparece com ✅/⚠️/❌ por item
2. Botão "Cancelar" não altera nada na ficha

### Lote E
1. Perguntar "Que magia combina com meu mago?" → Logcat mostra ferramentas sendo chamadas
2. Perguntar "Caminho para Tempestade de Relâmpagos?" → deve usar `gps_magia()`
3. Editar ficha manualmente → próxima mensagem ao Forjador deve refletir a mudança

---

## LIMITAÇÕES REAIS (o que este plano não resolve)

- **Busca semântica verdadeira:** "guerreiro de fogo" não vai achar "Mestre do Elemento Fogo" sem embeddings vetoriais (~100MB no APK). Não planejado.
- **Pré-requisitos de técnicas em cascata:** O GPS de magias existe (`NexusArcanoEngine`). Um GPS equivalente para técnicas não existe ainda — seria um Lote F futuro.
- **Modelos fracos:** DeepSeek Flash pode seguir o prompt parcialmente. Se falhar muito, escalonar para DeepSeek Pro ou Gemini Pro no fallback do `MestreIAGeneratorUseCase`.

---

*Gerado em 17/05/2026 — Forjador v2 Sprint Planning*
*Atualizar tabela de status no topo a cada Lote concluído.*
