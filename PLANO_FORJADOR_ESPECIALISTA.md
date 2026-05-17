# PLANO: FORJADOR ESPECIALISTA EM GURPS
**Data:** 17 de Maio de 2026
**Objetivo:** Transformar o Forjador num assistente agÃªntico que conhece a ficha, os catÃ¡logos e as regras de GURPS 4Âª Ed. â€” no estilo "Mini-VSCode".

---

## LEITURA RÃPIDA â€” Estado atual dos Lotes

> **Atualizar esta tabela a cada Lote concluÃ­do.**
> Se o contexto compactar, leia esta tabela primeiro para saber onde parou.

| Lote | Nome | Status | Commit |
|:---:|---|:---:|---|
| B | Prompt GURPS real | âœ… ConcluÃ­do | forjador(lote-b) |
| A | IDs reais no JSON | âœ… ConcluÃ­do | forjador(lote-a) |
| D | Budget de pontos | â¬œ Pendente | â€” |
| C | ValidaÃ§Ã£o prÃ©-integraÃ§Ã£o (UI) | â¬œ Pendente | â€” |
| E | Forjador AgÃªntico â€” Tools + GPS | â¬œ Pendente | â€” |

**Legenda:** â¬œ Pendente Â· ðŸ”„ Em andamento Â· âœ… ConcluÃ­do

---

## DIAGNÃ“STICO â€” 3 Problemas Raiz

### Problema 1 â€” IA nÃ£o conhece os IDs reais do catÃ¡logo
O `GOLD_TEMPLATE` pede `"nome": "Nome Exato"`. A IA chuta nomes como "Ataque Furtivo" ou "Sense Magic" que nÃ£o existem. `integrarRespostaNaFicha()` usa `limparNome()` para fuzzy-match; se nÃ£o acha, joga como Qualidade/Peculiaridade. Resultado: ficha cheia de itens sem mecÃ¢nica real.

### Problema 2 â€” IA nÃ£o sabe as regras de pontuaÃ§Ã£o GURPS
O prompt nÃ£o ensina o sistema de pontos. A IA distribui pontos de olho, sem respeitar:
- ST/HT = 10 pts/nÃ­vel acima de 10; DX/IQ = 20 pts/nÃ­vel
- PerÃ­cias: 1 pt = NH-base, 2 pts = +1, 4 pts = +2, 8 pts = +3
- Desvantagens: limite de -40 pts por personagem

### Problema 3 â€” IA confunde GURPS com D&D/Pathfinder
Erros comuns: "Ataque Furtivo", "FÃºria BÃ¡rbara", "Spell Slots", "Level 5", "Pontos de Magia". O prompt atual nÃ£o lista o que **nÃ£o existe** em GURPS.

---

## LOTE B â€” Prompt GURPS Real
**Resolve:** Problemas 2 e 3 Â· **Complexidade:** Baixa (sÃ³ prompt) Â· **Fazer primeiro** porque desbloqueia tudo

### Arquivo modificado
`app/src/main/java/com/gurps/ficha/data/network/MestreIAPromptsForjador.kt`

### O que mudar
Substituir o `PROMPT` atual inteiro por:

```kotlin
const val PROMPT = """
VOCÃŠ Ã‰ O FORJADOR â€” ESPECIALISTA EM CONSTRUÃ‡ÃƒO DE PERSONAGENS GURPS 4Âª EDIÃ‡ÃƒO BRASIL.

â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
SISTEMA DE PONTOS GURPS (OBRIGATÃ“RIO DOMINAR)
â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

ATRIBUTOS â€” custo por nÃ­vel ACIMA de 10:
  ST (ForÃ§a):        10 pts/nÃ­vel
  DX (Destreza):     20 pts/nÃ­vel
  IQ (InteligÃªncia): 20 pts/nÃ­vel
  HT (Vitalidade):   10 pts/nÃ­vel

Exemplos: ST 12 = 20 pts Â· DX 14 = 80 pts Â· IQ 13 = 60 pts

PERÃCIAS â€” pontos para atingir NH (NÃ­vel Habilidade):
  1 pt  = NH base do atributo
  2 pts = NH +1 Â· 4 pts = NH +2 Â· 8 pts = NH +3 Â· 12 pts = NH +4

Penalidade por dificuldade (reduz NH base com 1 pt):
  FÃ¡cil (F)=+0 Â· MÃ©dio (M)=-1 Â· DifÃ­cil (D)=-2 Â· M.DifÃ­cil (MD)=-3

VANTAGENS/DESVANTAGENS â€” custo vem do catÃ¡logo.
  Desvantagens tÃªm custo NEGATIVO. Limite: -40 pts em desvantagens.

PROTOCOLO DE CÃLCULO OBRIGATÃ“RIO â€” antes do JSON, some:
  Total = atributos + vantagens - |desvantagens| + perÃ­cias + magias
  Total DEVE ser â‰¤ pontos iniciais do personagem.

â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
O QUE NÃƒO EXISTE EM GURPS â€” NÃƒO INVENTE
â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

GURPS nÃ£o tem: classes, nÃ­veis de personagem (Level), slots de magia,
"Ataque Furtivo", "FÃºria BÃ¡rbara", "Pontos de Magia", "ProficiÃªncia",
"Sneak Attack", "Power Attack", "Channel Divinity".

Em GURPS, LADRÃƒO = Vantagens (Reflexos de Combate, Sorte, Ambidestria) +
  PerÃ­cias (Furtividade, Pickpocket, Abertura de Fechaduras, Armadilhas, Adaga)

Em GURPS, GUERREIRO = ST 13-15, DX 13-14, HT 12-13 +
  Vantagens (Reflexos de Combate, ResistÃªncia Ã  Dor) +
  PerÃ­cias de arma (Espada, Machado, Escudo) + TÃ¡tica

Em GURPS, MAGO = IQ 13-15 + Vantagem OBRIGATÃ“RIA "AptidÃ£o MÃ¡gica" (10 pts/nÃ­vel) +
  Magias em cadeia de prÃ©-requisitos + gasta PF (Pontos de Fadiga), nÃ£o "slots"

â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
REGRAS DE OURO DA FORJA
â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

1. USE APENAS IDs DO CATÃLOGO fornecido no contexto. Nenhum outro Ã© vÃ¡lido.
2. APTIDÃƒO MÃGICA: qualquer personagem com magias DEVE ter esta vantagem.
3. PRÃ‰-REQUISITOS: magias avanÃ§adas exigem magias bÃ¡sicas na cadeia.
4. SEM SUFIXOS no campo "id": "adaga" nÃ£o "adaga_faca_de_caca".
5. DANO em portuguÃªs: "cont", "perf", "corte", "imp", "esm". Nunca "cut"/"pi".
6. NÃVEL de perÃ­cia = NH final (DX 12 + 2 pts em MÃ©dia = NH 11).
7. CUSTO de vantagem = custo total (nÃ­vel Ã— custo/nÃ­vel se for perLevel).

SUA RESPOSTA DEVE TER:
1. IntroduÃ§Ã£o narrativa imersiva (2-3 parÃ¡grafos)
2. Justificativa dos pontos principais (1 parÃ¡grafo)
3. Resumo: "Atributos: X | Vantagens: Y | Desv: Z | PerÃ­cias: W | Total: V/MAX pts"
4. JSON obrigatÃ³rio no formato abaixo

GABARITO DE OURO:
${"$"}{GOLD_TEMPLATE}
"""
```

### Checkpoint do Lote B
ApÃ³s implementar, **commitar** com:636f4bb
```
git commit -m "forjador(lote-b): reescreve prompt com regras GURPS reais e blacklist D&D"
```
Atualizar a tabela de status no topo deste arquivo: **B â†’ âœ… ConcluÃ­do** + hash do commit.

---

## LOTE A â€” IDs Reais no JSON
**Resolve:** Problema 1 Â· **Complexidade:** MÃ©dia Â· **PrÃ©-requisito:** Lote B concluÃ­do

### Arquivos modificados
1. `app/src/main/java/com/gurps/ficha/data/network/MestreIAPromptsForjador.kt`
2. `app/src/main/java/com/gurps/ficha/data/network/MestreIAResponse.kt`
3. `app/src/main/java/com/gurps/ficha/domain/MestreIAGeneratorUseCase.kt`

### Passo 1 â€” Novo GOLD_TEMPLATE (em MestreIAPromptsForjador.kt)
Trocar `"nome"` por `"id"` em vantagens, desvantagens, perÃ­cias e magias:

```kotlin
private const val GOLD_TEMPLATE = """
{
  "nome": "Nome do Personagem",
  "historico": "Biografia narrativa (max 800 chars)",
  "aparencia": "DescriÃ§Ã£o fÃ­sica breve",
  "atributos": { "st": 10, "dx": 10, "iq": 10, "ht": 10 },
  "vantagens":    [ { "id": "aptidao_magica",  "custo": 15,  "descricao": "NÃ­vel 3" } ],
  "desvantagens": [ { "id": "codigo_de_honra", "custo": -10, "descricao": "CÃ³digo do Samurai" } ],
  "pericias":     [ { "id": "espada_longa",    "nivel": 14 } ],
  "magias":       [ { "id": "criar_fogo",      "custo": "1 fp" } ],
  "equipamentos": [ { "nome": "Espada Longa", "peso": 1.5, "custo": 500, "quantidade": 1,
                      "rd": 0, "dano": "1d+1 corte", "st_min": 10, "aparar": "0" } ]
}
"""
```
> Equipamentos mantÃªm `"nome"` livre â€” nÃ£o tÃªm IDs padronizados no catÃ¡logo.

### Passo 2 â€” Adicionar campo `id` ao MestreIAItem (em MestreIAResponse.kt)

```kotlin
data class MestreIAItem(
    val id: String? = null,       // NOVO â€” ID do catÃ¡logo
    val nome: String = "",        // mantido para fallback/equipamentos
    val custo: Int? = null,
    val descricao: String? = null,
    val nivel: Int = 0
)
```

### Passo 3 â€” Lookup por ID antes de fuzzy (em MestreIAGeneratorUseCase.kt)
Substituir `adicionarVantagem(nomeFull, desc, custo)` por `adicionarVantagem(item: MestreIAItem, desc, custo)`:

```kotlin
private fun adicionarVantagem(item: MestreIAItem, desc: String, custo: Int) {
    // 1. Lookup direto por ID (caminho feliz â€” Lote A)
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
    // 3. NÃ£o achou â€” Qualidade ou Peculiaridade
    if (custo >= 0) viewModel.adicionarQualidade("${item.nome} ($custo pts): $desc")
    else            viewModel.adicionarPeculiaridade("${item.nome} ($custo pts): $desc")
}
```

### Passo 4 â€” Injetar catÃ¡logo real no prompt (em MestreIAPromptsForjador.kt)
Adicionar mÃ©todo no `object`:

```kotlin
fun gerarPromptComCatalogo(
    idsVantagens: List<String>,
    idsDesvantagens: List<String>,
    idsPericias: List<String>,
    idsMagias: List<String>
): String = """
    $PROMPT

    === CATÃLOGO COMPLETO DE IDs VÃLIDOS ===
    Use APENAS estes IDs. Qualquer outro serÃ¡ rejeitado pelo sistema.

    VANTAGENS (${idsVantagens.size}): ${idsVantagens.joinToString(", ")}
    DESVANTAGENS (${idsDesvantagens.size}): ${idsDesvantagens.joinToString(", ")}
    PERÃCIAS (${idsPericias.size}): ${idsPericias.joinToString(", ")}
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
git commit -m "forjador(lote-a): IDs reais no JSON, lookup direto por id no catÃ¡logo"
```
Atualizar tabela: **A â†’ âœ… ConcluÃ­do** + hash 89f26ca

---

## LOTE D â€” Budget de Pontos
**Resolve:** Problema 2 (validaÃ§Ã£o pÃ³s-IA) Â· **Complexidade:** Baixa-MÃ©dia Â· **PrÃ©-requisito:** Lote A concluÃ­do

### Arquivos modificados
`app/src/main/java/com/gurps/ficha/domain/MestreIAGeneratorUseCase.kt`

### Passo 1 â€” Passar pontos iniciais no prompt
Concatenar ao `promptFinal` antes de enviar:

```kotlin
val pontosIniciais = viewModel.personagem.pontosIniciais
val promptFinal = MestreIAPromptsForjador.gerarPromptComCatalogo(...) +
    "\n\nPONTOS DO PERSONAGEM: $pontosIniciais pts. " +
    "A soma atributos + vantagens - |desvantagens| + perÃ­cias DEVE ser â‰¤ $pontosIniciais."
```

### Passo 2 â€” Validar apÃ³s integraÃ§Ã£o

```kotlin
private fun validarBudget(ficha: MestreIAResponse): String? {
    val st = ficha.atributos.st; val dx = ficha.atributos.dx
    val iq = ficha.atributos.iq; val ht = ficha.atributos.ht
    val custoAtributos = ((st-10).coerceAtLeast(0)*10) + ((dx-10).coerceAtLeast(0)*20) +
                         ((iq-10).coerceAtLeast(0)*20) + ((ht-10).coerceAtLeast(0)*10)
    val custoVantagens    = ficha.vantagens.sumOf    { it.custo ?: 0 }
    val custoDesvantagens = ficha.desvantagens.sumOf { it.custo ?: 0 } // jÃ¡ negativo
    val custoPericias     = ficha.pericias.sumOf     { calcularCustoPericia(it) }
    val total = custoAtributos + custoVantagens + custoDesvantagens + custoPericias
    val max = viewModel.personagem.pontosIniciais
    return if (total > max) "âš ï¸ Ficha usa $total pts (mÃ¡ximo: $max pts)" else null
}
```

### Checkpoint do Lote D
```
git commit -m "forjador(lote-d): validaÃ§Ã£o de budget de pontos no prompt e pÃ³s-integraÃ§Ã£o"
```
Atualizar tabela: **D â†’ âœ… ConcluÃ­do** + hash.

---

## LOTE C â€” ValidaÃ§Ã£o PrÃ©-IntegraÃ§Ã£o (UI)
**Resolve:** UX â€” usuÃ¡rio vÃª o que vai ser aplicado antes de confirmar Â· **Complexidade:** Alta (nova tela) Â· **PrÃ©-requisito:** Lotes A e D concluÃ­dos

### Arquivos a criar/modificar
- **NOVO:** `domain/MestreIAValidacaoReport.kt`
- **MODIFICAR:** `viewmodel/FichaViewModel.kt` (expor mÃ©todo de validaÃ§Ã£o)
- **MODIFICAR:** UI do Forjador (adicionar dialog de confirmaÃ§Ã£o)

### Estrutura de dados (MestreIAValidacaoReport.kt)

```kotlin
data class ItemValidacao(
    val entrada: String,           // o que a IA gerou
    val idEncontrado: String?,     // ID real no catÃ¡logo (se achou)
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
â•”â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•—
â•‘  VALIDAÃ‡ÃƒO DA FICHA GERADA           â•‘
â• â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•£
â•‘ âœ… aptidao_magica  â†’ AptidÃ£o MÃ¡gica  â•‘
â•‘ âœ… espada_longa    â†’ Espada Longa    â•‘
â•‘ âš ï¸ "Furto Sombrio" â†’ virarÃ¡ Qualidadeâ•‘
â•‘ âŒ "Level 3"       â†’ invÃ¡lido GURPS  â•‘
â• â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•£
â•‘   Total: 98 / 100 pts                â•‘
â•‘  [APLICAR FICHA]    [CANCELAR]       â•‘
â•šâ•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
```

### Checkpoint do Lote C
```
git commit -m "forjador(lote-c): dialog de validaÃ§Ã£o prÃ©-integraÃ§Ã£o com status por item"
```
Atualizar tabela: **C â†’ âœ… ConcluÃ­do** + hash.

---

## LOTE E â€” Forjador AgÃªntico: O "Mini-VSCode"
**Conceito:** O Forjador para de receber o JSON inteiro da ficha. Passa a **explorar a ficha com ferramentas**, como Claude Code explora um projeto com Read/Grep/Edit. Â· **Complexidade:** Alta Â· **PrÃ©-requisito:** Todos os lotes anteriores concluÃ­dos

### Analogia VSCode â†’ Forjador

| Claude Code | Forjador AgÃªntico |
|---|---|
| `Read file` | `ler_atributos()`, `ler_vantagens()` |
| `Grep` por sÃ­mbolo | `buscar_vantagem(query)`, `buscar_magia(query)` |
| `Glob` por padrÃ£o | `listar_pericias()`, `listar_equipamentos()` |
| Arquivo aberto no editor | Personagem ativo |
| SessÃ£o aberta no VSCode | `mestreIAChatHistory` persistente |
| NotificaÃ§Ã£o "arquivo mudou" | `[SISTEMA] Adaga adicionada na ficha` |

### Assets disponÃ­veis como "sistema de arquivos" do Forjador

O app tem **11 catÃ¡logos** nos assets â€” cada um Ã© uma "pasta" que o modelo pode explorar:

| Asset | ConteÃºdo |
|---|---|
| `vantagens.v3.json` | Vantagens gerais (`id, nome, costKind, perLevel, tags`) |
| `vantagens_artes_marciais.v1.json` | Vantagens de Artes Marciais |
| `desvantagens.v2.json` | Desvantagens (`id, nome, custo`) |
| `pericias.json` | PerÃ­cias (`id, nome, atributoBase, dificuldadeFixa`) |
| `pericias_artes_marciais.v1.json` | PerÃ­cias de Artes Marciais |
| `magias2versao.json` | Magias (`id, nome, escola, preRequisitos, energia`) |
| `tecnicas.v1.json` | TÃ©cnicas (`id, nome, preRequisitoRaw, sourceBook`) |
| `armas_corpo_a_corpo.v1.normalized.json` | Armas CC (`id, nome, dano, grupo, st_min`) |
| `armas_fogo.v1.normalized.json` | Armas de fogo (`id, nome, dano, alcance, municao`) |
| `armas_distancia.v1.normalized.json` | Armas Ã  distÃ¢ncia |
| `armaduras.v2.json` + `escudos.v1.json` | ProteÃ§Ã£o (`id, nome, rd, peso, custo`) |
| `modificadores.v1.json` | AmpliaÃ§Ãµes e LimitaÃ§Ãµes |

### Mapa completo de Tools

**GRUPO 1 â€” Leitura da Ficha (como Read)**
```
ler_atributos()     â†’ { st, dx, iq, ht, pv, pf, vb, pm, velocidade, am_nivel }
ler_vantagens()     â†’ [{ id, nome, custo, nivel }]
ler_desvantagens()  â†’ [{ id, nome, custo }]
ler_pericias()      â†’ [{ id, nome, nh, pts_gastos, atributo_base }]
ler_magias()        â†’ [{ id, nome, escola, pts_gastos }]
ler_tecnicas()      â†’ [{ id, nome }]
ler_equipamentos()  â†’ [{ nome, dano, peso, rd, custo }]
ler_historia()      â†’ { historico, aparencia }
calcular_pontos()   â†’ { atributos, vantagens, desvantagens, pericias, total, maximo }
```

**GRUPO 2 â€” Busca no CatÃ¡logo (como Grep)**
```
buscar_vantagem(query)    â†’ vantagens.v3 + vantagens_artes_marciais
buscar_desvantagem(query) â†’ desvantagens.v2
buscar_pericia(query)     â†’ pericias + pericias_artes_marciais
buscar_magia(query)       â†’ magias2versao (com escola + prÃ©-req + energia)
buscar_tecnica(query)     â†’ tecnicas.v1 (com sourceBook + prÃ©-req)
buscar_arma(query)        â†’ armas_cc + armas_fogo + armas_distancia
buscar_armadura(query)    â†’ armaduras.v2 + escudos.v1
buscar_modificador(query) â†’ modificadores.v1
```

**GRUPO 3 â€” Motores de Regra (ExecuÃ§Ã£o de lÃ³gica jÃ¡ existente)**
```
gps_magia(alvo_id)          â†’ NexusArcanoAdapter.calcular() com estado atual do personagem
                               Retorna: cadeia completa, prÃ³ximas aÃ§Ãµes, bloqueios, escolas
verificar_prereq_magia(id)  â†’ checa se o personagem jÃ¡ pode aprender aquela magia agora
```

**GRUPO 4 â€” Escrita na Ficha (como Edit)**
```
adicionar_vantagem(id, custo?)
adicionar_desvantagem(id, custo?)
adicionar_pericia(id, pts)
adicionar_magia(id, pts)
adicionar_tecnica(id)
adicionar_equipamento(nome, peso, custo, dano?)
atualizar_atributo(nome, valor)
```

### GPS de Magias como Tool â€” exemplo de conversa real

```
UsuÃ¡rio: "Quero aprender Tempestade de RelÃ¢mpagos, o que preciso?"

Forjador chama: gps_magia("tempestade_de_relampagos")

NexusArcanoAdapter retorna:
  cadeia: "RelÃ¢mpago â†’ Nuvem de RelÃ¢mpagos â†’ Tempestade de RelÃ¢mpagos"
  magiasDoPersonagem: ["relampago"]  â† jÃ¡ tem!
  proximaAcao: "nuvem_de_relampagos"
  bloqueio: nenhum (AM e IQ suficientes)
  progressoCadeia: "1/3 magias da cadeia"

Forjador responde:
  "VocÃª jÃ¡ tem RelÃ¢mpago! PrÃ³ximo passo: Nuvem de RelÃ¢mpagos (prÃ©-req: âœ…).
   ApÃ³s isso, Tempestade de RelÃ¢mpagos estarÃ¡ desbloqueada.
   Quer que eu adicione Nuvem de RelÃ¢mpagos agora?"
```

### SessÃ£o Persistente â€” Eventos de MudanÃ§a

O `mestreIAChatHistory` jÃ¡ existe. Mas quando o usuÃ¡rio edita a ficha **fora do chat**, o modelo nÃ£o sabe. SoluÃ§Ã£o: injetar eventos de sistema no histÃ³rico automaticamente.

```kotlin
// FichaViewModel.kt â€” toda mutaÃ§Ã£o na ficha dispara um evento
fun adicionarVantagem(def, custo, desc) {
    // ... cÃ³digo atual ...
    injetarEventoMestreIA(
        "[SISTEMA] Ficha atualizada: '${def.nome}' adicionada ($custo pts). " +
        "Total: ${calcularPontosGastos()}/${pontosIniciais} pts."
    )
}

fun adicionarPericia(def, pts) {
    // ... cÃ³digo atual ...
    injetarEventoMestreIA(
        "[SISTEMA] Ficha atualizada: '${def.nome}' NH ${calcularNH(def, pts)} " +
        "adicionada ($pts pts)."
    )
}

private fun injetarEventoMestreIA(texto: String) {
    mestreIAChatHistory.add(MestreIAChatMessage(role = "system", text = texto))
}
```

### Loop AgÃªntico para o Forjador

```
UsuÃ¡rio: "Que vantagem ficaria legal no meu guerreiro com machado?"

IteraÃ§Ã£o 1: IA chama ler_atributos() + ler_vantagens() + ler_pericias()
            â†’ vÃª: ST 14, DX 13, Machado NH 15, Reflexos de Combate (jÃ¡ tem)

IteraÃ§Ã£o 2: IA chama buscar_vantagem("combate corpo a corpo machado guerreiro")
            â†’ catÃ¡logo retorna: Maestria em Armas, Sentido de Combate, ResistÃªncia Ã  Dor

IteraÃ§Ã£o 3: IA chama calcular_pontos()
            â†’ vÃª: 82/100 pts gastos, sobram 18 pts

IteraÃ§Ã£o 4: IA responde fundamentado:
            "Com 18 pts livres e Machado NH 15, recomendo Maestria em Armas (id: maestria_em_armas, 
             custo: 15 pts). Deixa seu Machado ainda mais letal. Adiciono na ficha?"
```

### Arquivos a Criar/Modificar no Lote E

```
NOVOS:
  domain/tools/ForjadorTools.kt           â† definiÃ§Ã£o JSON dos 4 grupos de tools
  domain/tools/ForjadorToolExecutor.kt    â† execuÃ§Ã£o: chama repository/viewModel/NexusArcano

MODIFICAR:
  domain/MestreIAGeneratorUseCase.kt      â† habilitar loop agÃªntico, remover JSON upfront
  domain/magias/NexusArcanoModoAlvoAdapter.kt â† expor calcular() ao executor
  viewmodel/FichaViewModel.kt             â† injetarEventoMestreIA() em cada mutaÃ§Ã£o
  data/network/MestreIAPromptsForjador.kt â† prompt sem JSON upfront, instruÃ§Ãµes de tools
```

### Checkpoint do Lote E
```
git commit -m "forjador(lote-e): loop agÃªntico com tools, GPS de magias, sessÃ£o persistente"
```
Atualizar tabela: **E â†’ âœ… ConcluÃ­do** + hash.

---

## VERIFICAÃ‡ÃƒO â€” Como testar cada Lote

### Lote B
1. Abrir Mestre IA â†’ Forjador â†’ pedir "Crie um ladrÃ£o de 100 pontos"
2. JSON gerado nÃ£o deve conter: "Ataque Furtivo", "Level", "Spell Slot", "Pontos de Magia"
3. Deve conter: ids como `furtividade`, `adaga`, `reflexos_de_combate`

### Lote A
1. Logcat (`MestreIA_Forjador`) nÃ£o deve mostrar: `"Adicionando como Qualidade:"`
2. Ficha resultante deve ter vantagens com mecÃ¢nica real (custo fixo, tags, etc.)

### Lote D
1. Pedir personagem de 50 pts â†’ soma final deve ser â‰¤ 50
2. Se exceder â†’ alerta no log e/ou na UI

### Lote C
1. Ao aplicar ficha gerada â†’ dialog aparece com âœ…/âš ï¸/âŒ por item
2. BotÃ£o "Cancelar" nÃ£o altera nada na ficha

### Lote E
1. Perguntar "Que magia combina com meu mago?" â†’ Logcat mostra ferramentas sendo chamadas
2. Perguntar "Caminho para Tempestade de RelÃ¢mpagos?" â†’ deve usar `gps_magia()`
3. Editar ficha manualmente â†’ prÃ³xima mensagem ao Forjador deve refletir a mudanÃ§a

---

## LIMITAÃ‡Ã•ES REAIS (o que este plano nÃ£o resolve)

- **Busca semÃ¢ntica verdadeira:** "guerreiro de fogo" nÃ£o vai achar "Mestre do Elemento Fogo" sem embeddings vetoriais (~100MB no APK). NÃ£o planejado.
- **PrÃ©-requisitos de tÃ©cnicas em cascata:** O GPS de magias existe (`NexusArcanoEngine`). Um GPS equivalente para tÃ©cnicas nÃ£o existe ainda â€” seria um Lote F futuro.
- **Modelos fracos:** DeepSeek Flash pode seguir o prompt parcialmente. Se falhar muito, escalonar para DeepSeek Pro ou Gemini Pro no fallback do `MestreIAGeneratorUseCase`.

---

*Gerado em 17/05/2026 â€” Forjador v2 Sprint Planning*
*Atualizar tabela de status no topo a cada Lote concluÃ­do.*
