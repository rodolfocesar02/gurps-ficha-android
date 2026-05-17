# PLANO: FORJADOR ESPECIALISTA EM GURPS
**Data:** 17 de Maio de 2026
**Objetivo:** Transformar o Forjador num assistente agêntico que conhece a ficha, os catálogos e as regras de GURPS 4ª Ed. — no estilo "Mini-VSCode".

---

## LEITURA RÁPIDA — Estado atual dos Lotes

> **Atualizar esta tabela a cada Lote concluído.**
> Se o contexto compactar, leia esta tabela primeiro para saber onde parou.

| Lote | Nome | Status | Commit |
|:---:|---|:---:|---|
| B | Prompt GURPS real | ✅ Concluído | forjador(lote-b) |
| A | IDs reais no JSON | ⬜ Pendente | — |
| D | Budget de pontos | ⬜ Pendente | — |
| C | Validação pré-integração (UI) | ⬜ Pendente | — |
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
Substituir o `PROMPT` atual inteiro por:

```kotlin
const val PROMPT = """
VOCÊ É O FORJADOR — ESPECIALISTA EM CONSTRUÇÃO DE PERSONAGENS GURPS 4ª EDIÇÃO BRASIL.

══════════════════════════════════════════════
SISTEMA DE PONTOS GURPS (OBRIGATÓRIO DOMINAR)
══════════════════════════════════════════════

ATRIBUTOS — custo por nível ACIMA de 10:
  ST (Força):        10 pts/nível
  DX (Destreza):     20 pts/nível
  IQ (Inteligência): 20 pts/nível
  HT (Vitalidade):   10 pts/nível

Exemplos: ST 12 = 20 pts · DX 14 = 80 pts · IQ 13 = 60 pts

PERÍCIAS — pontos para atingir NH (Nível Habilidade):
  1 pt  = NH base do atributo
  2 pts = NH +1 · 4 pts = NH +2 · 8 pts = NH +3 · 12 pts = NH +4

Penalidade por dificuldade (reduz NH base com 1 pt):
  Fácil (F)=+0 · Médio (M)=-1 · Difícil (D)=-2 · M.Difícil (MD)=-3

VANTAGENS/DESVANTAGENS — custo vem do catálogo.
  Desvantagens têm custo NEGATIVO. Limite: -40 pts em desvantagens.

PROTOCOLO DE CÁLCULO OBRIGATÓRIO — antes do JSON, some:
  Total = atributos + vantagens - |desvantagens| + perícias + magias
  Total DEVE ser ≤ pontos iniciais do personagem.

══════════════════════════════════════════════
O QUE NÃO EXISTE EM GURPS — NÃO INVENTE
══════════════════════════════════════════════

GURPS não tem: classes, níveis de personagem (Level), slots de magia,
"Ataque Furtivo", "Fúria Bárbara", "Pontos de Magia", "Proficiência",
"Sneak Attack", "Power Attack", "Channel Divinity".

Em GURPS, LADRÃO = Vantagens (Reflexos de Combate, Sorte, Ambidestria) +
  Perícias (Furtividade, Pickpocket, Abertura de Fechaduras, Armadilhas, Adaga)

Em GURPS, GUERREIRO = ST 13-15, DX 13-14, HT 12-13 +
  Vantagens (Reflexos de Combate, Resistência à Dor) +
  Perícias de arma (Espada, Machado, Escudo) + Tática

Em GURPS, MAGO = IQ 13-15 + Vantagem OBRIGATÓRIA "Aptidão Mágica" (10 pts/nível) +
  Magias em cadeia de pré-requisitos + gasta PF (Pontos de Fadiga), não "slots"

══════════════════════════════════════════════
REGRAS DE OURO DA FORJA
══════════════════════════════════════════════

1. USE APENAS IDs DO CATÁLOGO fornecido no contexto. Nenhum outro é válido.
2. APTIDÃO MÁGICA: qualquer personagem com magias DEVE ter esta vantagem.
3. PRÉ-REQUISITOS: magias avançadas exigem magias básicas na cadeia.
4. SEM SUFIXOS no campo "id": "adaga" não "adaga_faca_de_caca".
5. DANO em português: "cont", "perf", "corte", "imp", "esm". Nunca "cut"/"pi".
6. NÍVEL de perícia = NH final (DX 12 + 2 pts em Média = NH 11).
7. CUSTO de vantagem = custo total (nível × custo/nível se for perLevel).

SUA RESPOSTA DEVE TER:
1. Introdução narrativa imersiva (2-3 parágrafos)
2. Justificativa dos pontos principais (1 parágrafo)
3. Resumo: "Atributos: X | Vantagens: Y | Desv: Z | Perícias: W | Total: V/MAX pts"
4. JSON obrigatório no formato abaixo

GABARITO DE OURO:
${"$"}{GOLD_TEMPLATE}
"""
```

### Checkpoint do Lote B
Após implementar, **commitar** com:
```
git commit -m "forjador(lote-b): reescreve prompt com regras GURPS reais e blacklist D&D"
```
Atualizar a tabela de status no topo deste arquivo: **B → ✅ Concluído** + hash do commit.

---

## LOTE A — IDs Reais no JSON
**Resolve:** Problema 1 · **Complexidade:** Média · **Pré-requisito:** Lote B concluído

### Arquivos modificados
1. `app/src/main/java/com/gurps/ficha/data/network/MestreIAPromptsForjador.kt`
2. `app/src/main/java/com/gurps/ficha/data/network/MestreIAResponse.kt`
3. `app/src/main/java/com/gurps/ficha/domain/MestreIAGeneratorUseCase.kt`

### Passo 1 — Novo GOLD_TEMPLATE (em MestreIAPromptsForjador.kt)
Trocar `"nome"` por `"id"` em vantagens, desvantagens, perícias e magias:

```kotlin
private const val GOLD_TEMPLATE = """
{
  "nome": "Nome do Personagem",
  "historico": "Biografia narrativa (max 800 chars)",
  "aparencia": "Descrição física breve",
  "atributos": { "st": 10, "dx": 10, "iq": 10, "ht": 10 },
  "vantagens":    [ { "id": "aptidao_magica",  "custo": 15,  "descricao": "Nível 3" } ],
  "desvantagens": [ { "id": "codigo_de_honra", "custo": -10, "descricao": "Código do Samurai" } ],
  "pericias":     [ { "id": "espada_longa",    "nivel": 14 } ],
  "magias":       [ { "id": "criar_fogo",      "custo": "1 fp" } ],
  "equipamentos": [ { "nome": "Espada Longa", "peso": 1.5, "custo": 500, "quantidade": 1,
                      "rd": 0, "dano": "1d+1 corte", "st_min": 10, "aparar": "0" } ]
}
"""
```
> Equipamentos mantêm `"nome"` livre — não têm IDs padronizados no catálogo.

### Passo 2 — Adicionar campo `id` ao MestreIAItem (em MestreIAResponse.kt)

```kotlin
data class MestreIAItem(
    val id: String? = null,       // NOVO — ID do catálogo
    val nome: String = "",        // mantido para fallback/equipamentos
    val custo: Int? = null,
    val descricao: String? = null,
    val nivel: Int = 0
)
```

### Passo 3 — Lookup por ID antes de fuzzy (em MestreIAGeneratorUseCase.kt)
Substituir `adicionarVantagem(nomeFull, desc, custo)` por `adicionarVantagem(item: MestreIAItem, desc, custo)`:

```kotlin
private fun adicionarVantagem(item: MestreIAItem, desc: String, custo: Int) {
    // 1. Lookup direto por ID (caminho feliz — Lote A)
    if (!item.id.isNullOrBlank()) {
        repository.vantagens.find { it.id == item.id }?.let {
            viewModel.adicionarVantagem(it, custo = if (custo != 0) custo else it.getCustoBase(), desc = desc)
            return
        }
        repository.desvantagens.find { it.id == item.id }?.let {
            viewModel.adicionarDesvantagem(it, custo = if (custo != 0) custo else it.getCustoBase(), desc = desc)
            return
        }
    }
    // 2. Fuzzy fallback por nome (comportamento atual)
    val nomeLimpo = limparNome(item.nome)
    repository.vantagens.find { limparNome(it.nome) == nomeLimpo }?.let {
        viewModel.adicionarVantagem(it, custo = if (custo != 0) custo else it.getCustoBase(), desc = desc); return
    }
    repository.desvantagens.find { limparNome(it.nome) == nomeLimpo }?.let {
        viewModel.adicionarDesvantagem(it, custo = if (custo != 0) custo else it.getCustoBase(), desc = desc); return
    }
    // 3. Não achou — Qualidade ou Peculiaridade
    if (custo >= 0) viewModel.adicionarQualidade("${item.nome} ($custo pts): $desc")
    else            viewModel.adicionarPeculiaridade("${item.nome} ($custo pts): $desc")
}
```

### Passo 4 — Injetar catálogo real no prompt (em MestreIAPromptsForjador.kt)
Adicionar método no `object`:

```kotlin
fun gerarPromptComCatalogo(
    idsVantagens: List<String>,
    idsDesvantagens: List<String>,
    idsPericias: List<String>,
    idsMagias: List<String>
): String = """
    $PROMPT

    === CATÁLOGO COMPLETO DE IDs VÁLIDOS ===
    Use APENAS estes IDs. Qualquer outro será rejeitado pelo sistema.

    VANTAGENS (${idsVantagens.size}): ${idsVantagens.joinToString(", ")}
    DESVANTAGENS (${idsDesvantagens.size}): ${idsDesvantagens.joinToString(", ")}
    PERÍCIAS (${idsPericias.size}): ${idsPericias.joinToString(", ")}
    MAGIAS (${idsMagias.size}): ${idsMagias.joinToString(", ")}
""".trimIndent()
```

Em `MestreIAGeneratorUseCase.kt`, substituir o prompt fixo por:

```kotlin
val promptFinal = MestreIAPromptsForjador.gerarPromptComCatalogo(
    idsVantagens   = repository.vantagens.map { it.id },
    idsDesvantagens= repository.desvantagens.map { it.id },
    idsPericias    = repository.pericias.map { it.id },
    idsMagias      = repository.magias.map { it.id }
)
```

### Checkpoint do Lote A
```
git commit -m "forjador(lote-a): IDs reais no JSON, lookup direto por id no catálogo"
```
Atualizar tabela: **A → ✅ Concluído** + hash.

---

## LOTE D — Budget de Pontos
**Resolve:** Problema 2 (validação pós-IA) · **Complexidade:** Baixa-Média · **Pré-requisito:** Lote A concluído

### Arquivos modificados
`app/src/main/java/com/gurps/ficha/domain/MestreIAGeneratorUseCase.kt`

### Passo 1 — Passar pontos iniciais no prompt
Concatenar ao `promptFinal` antes de enviar:

```kotlin
val pontosIniciais = viewModel.personagem.pontosIniciais
val promptFinal = MestreIAPromptsForjador.gerarPromptComCatalogo(...) +
    "\n\nPONTOS DO PERSONAGEM: $pontosIniciais pts. " +
    "A soma atributos + vantagens - |desvantagens| + perícias DEVE ser ≤ $pontosIniciais."
```

### Passo 2 — Validar após integração

```kotlin
private fun validarBudget(ficha: MestreIAResponse): String? {
    val st = ficha.atributos.st; val dx = ficha.atributos.dx
    val iq = ficha.atributos.iq; val ht = ficha.atributos.ht
    val custoAtributos = ((st-10).coerceAtLeast(0)*10) + ((dx-10).coerceAtLeast(0)*20) +
                         ((iq-10).coerceAtLeast(0)*20) + ((ht-10).coerceAtLeast(0)*10)
    val custoVantagens    = ficha.vantagens.sumOf    { it.custo ?: 0 }
    val custoDesvantagens = ficha.desvantagens.sumOf { it.custo ?: 0 } // já negativo
    val custoPericias     = ficha.pericias.sumOf     { calcularCustoPericia(it) }
    val total = custoAtributos + custoVantagens + custoDesvantagens + custoPericias
    val max = viewModel.personagem.pontosIniciais
    return if (total > max) "⚠️ Ficha usa $total pts (máximo: $max pts)" else null
}
```

### Checkpoint do Lote D
```
git commit -m "forjador(lote-d): validação de budget de pontos no prompt e pós-integração"
```
Atualizar tabela: **D → ✅ Concluído** + hash.

---

## LOTE C — Validação Pré-Integração (UI)
**Resolve:** UX — usuário vê o que vai ser aplicado antes de confirmar · **Complexidade:** Alta (nova tela) · **Pré-requisito:** Lotes A e D concluídos

### Arquivos a criar/modificar
- **NOVO:** `domain/MestreIAValidacaoReport.kt`
- **MODIFICAR:** `viewmodel/FichaViewModel.kt` (expor método de validação)
- **MODIFICAR:** UI do Forjador (adicionar dialog de confirmação)

### Estrutura de dados (MestreIAValidacaoReport.kt)

```kotlin
data class ItemValidacao(
    val entrada: String,           // o que a IA gerou
    val idEncontrado: String?,     // ID real no catálogo (se achou)
    val status: Status,
    val mensagem: String
) {
    enum class Status { OK, FUZZY, FALLBACK, ERRO }
}

data class RelatorioValidacao(
    val itens: List<ItemValidacao>,
    val totalOk: Int,
    val totalFallback: Int,
    val pontosCalculados: Int,
    val pontosMaximos: Int
)
```

### UI do dialog

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

### Checkpoint do Lote C
```
git commit -m "forjador(lote-c): dialog de validação pré-integração com status por item"
```
Atualizar tabela: **C → ✅ Concluído** + hash.

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
