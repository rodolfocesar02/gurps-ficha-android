# Relatório DRY — Análise de Duplicações de Código

> **Projeto**: GURPS Ficha Android (Kotlin/Jetpack Compose)
> **Data da análise**: 2026-05-28
> **Escopo**: `app/src/main/java/com/gurps/ficha/`
> **Status**: 🔍 **Apenas análise e sugestões — nenhum arquivo foi modificado.**

## Sumário Executivo

Foram identificados **11 padrões principais** de código duplicado, totalizando uma estimativa de **~380–450 linhas** que podem ser eliminadas por refatoração. As duplicações mais críticas são:

| # | Padrão | Impacto | Risco se não corrigido |
|---|--------|---------|------------------------|
| 1 | Normalização de texto (7 cópias) | 🔴 Alto | Bugs de busca inconsistente |
| 2 | Dicionários de sinônimos do RAG (3 cópias) | 🔴 Alto | Divergência semântica do AUDITOR |
| 3 | Dialogs Compose (~13 estruturas idênticas) | 🔴 Alto | Inconsistência de UX, manutenção pesada |
| 4 | TraitRules `calculateCost()` (12 arquivos) | 🟡 Médio | Boilerplate, regra de modificador divergir |
| 5 | Clientes HTTP (`MestreIAClient`, `DiscordRollApiClient`) | 🟡 Médio | Tratamento de erro inconsistente |
| 6 | Filtros de catálogo (7 funções) | 🟡 Médio | Comportamento sutilmente diferente |
| 7 | SharedPreferences (3 stores) | 🟡 Médio | Bugs de chave/migração |
| 8 | Trait Dialogs V1 vs V2 | 🟡 Médio | Confusão arquitetural |
| 9 | Parsing JSON manual | 🟡 Médio | Erros silenciosos de schema |
| 10 | `coerceIn()` em atributos | 🟢 Baixo | Cosmético |
| 11 | `try/catch` genérico com log | 🟢 Baixo | Cosmético |

---

## 1. 🔴 Normalização de Texto — 7 Implementações Paralelas

### Evidência

| Arquivo | Linha | Função | Linhas |
|---------|-------|--------|--------|
| [CatalogFilters.kt](app/src/main/java/com/gurps/ficha/domain/filters/CatalogFilters.kt#L9-L28) | 9-28 | `normalizarBusca()` (pública) | 20 |
| [CatalogFilters.kt](app/src/main/java/com/gurps/ficha/domain/filters/CatalogFilters.kt#L31-L36) | 31-36 | `contemBusca()` | 6 |
| [SkillEngine.kt](app/src/main/java/com/gurps/ficha/domain/engine/SkillEngine.kt#L220-L228) | 220-228 | `normalizarTexto()` (inline) | 9 |
| [DataRepository.kt](app/src/main/java/com/gurps/ficha/data/DataRepository.kt#L764-L768) | 764-768 | `normalizarBusca()` (inline) | 5 |
| [DataRepository.kt](app/src/main/java/com/gurps/ficha/data/DataRepository.kt#L880-L883) | 880-883 | `Normalizer.normalize()` inline | 4 |
| [MestreDeArmasRule.kt](app/src/main/java/com/gurps/ficha/domain/rules/traits/MestreDeArmasRule.kt#L30-L58) | 30-58 | `private fun normalize()` | 29 |
| [DialogsTecnicas.kt](app/src/main/java/com/gurps/ficha/ui/DialogsTecnicas.kt#L55-L59) | 55-59 | `private fun normalizarBusca()` | 5 |
| [FichaSkillDelegate.kt](app/src/main/java/com/gurps/ficha/viewmodel/delegates/FichaSkillDelegate.kt#L223-L226) | 223-226 | `private fun normalizarTexto()` | 4 |

### Exemplo Concreto (versão canônica vs. clone)

**Canônica — `CatalogFilters.kt:9-28`:**
```kotlin
fun normalizarBusca(valor: String): String {
    val limpo = valor
        .replace("ǜ", "a").replace("ǭ", "a").replace("Ǹ", "e") /* ... */
    val normalized = Normalizer.normalize(limpo, Normalizer.Form.NFD)
    val accentRemoved = normalized.replace(Regex("\\p{M}+"), "")
    return accentRemoved.lowercase(Locale.ROOT)
        .replace(Regex("[^a-z0-9]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}
```

**Variante — `MestreDeArmasRule.kt:30-58`** (faz quase o mesmo, mas **adiciona** stemming de plural e remoção de parênteses, divergindo):
```kotlin
private fun normalize(text: String?): String {
    if (text == null) return ""
    var res = text.lowercase()
    if (res.contains("(")) res = res.substringBefore("(").trim()
    val nfd = Normalizer.normalize(res, Normalizer.Form.NFD)
    res = Regex("\\p{InCombiningDiacriticalMarks}+").replace(nfd, "")
    res = res.replace(Regex("[^a-z0-9\\s]"), " ")
    res = res.split(" ").filter { it.isNotBlank() }.map { word ->
        if (word.length > 3 && word.endsWith("s")) word.substring(0, word.length - 1) else word
    }.joinToString(" ")
    return res.replace(Regex("\\s+"), " ").trim()
}
```

### Diagnóstico
- **`CatalogFilters.normalizarBusca`** já é a versão canônica do projeto (confirmado no MAPA_DETALHADO §8).
- Os clones existem porque **alguns chamadores precisavam de variações** (stemming, despluralização, manter parênteses, etc.) — mas isso foi resolvido com cópia em vez de parametrização.

### Refatoração Sugerida
Criar um **único objeto** `domain/filters/TextNormalizer.kt` com modos:

```kotlin
object TextNormalizer {
    enum class Mode { SIMPLE, STEM_PLURAL, STRIP_PARENS, STEM_AND_STRIP }

    fun normalize(text: String?, mode: Mode = Mode.SIMPLE): String { /* ... */ }

    fun contains(haystack: String, needle: String, mode: Mode = Mode.SIMPLE): Boolean { /* ... */ }
}
```

E **deletar** os 7 clones, substituindo cada chamada pela versão centralizada.

**Economia estimada:** ~40 linhas, e elimina risco de divergência em buscas de catálogo.

---

## 2. 🔴 Dicionários de Sinônimos do RAG — 3 Fontes da Verdade

### Evidência (já documentada em [feedback_rag_maintenance.md](C:/Users/Rodolfo/.claude/projects/c--Users-Rodolfo-Desktop-ficha-gurps/memory/feedback_rag_maintenance.md))

| Arquivo | Linhas | Tipo de dicionário |
|---------|--------|---------------------|
| [MestreIAQueryEngine.kt](app/src/main/java/com/gurps/ficha/data/MestreIAQueryEngine.kt#L29-L73) | 29-73 (~45 linhas) | Sinônimos técnicos para FTS4 |
| [MestreIAPlanner.kt](app/src/main/java/com/gurps/ficha/domain/MestreIAPlanner.kt#L99-L138) | 99-138 (~40 linhas) | Grupos semânticos (combate, magia, ambiente) |
| [MestreIAGraphEngine.kt](app/src/main/java/com/gurps/ficha/domain/MestreIAGraphEngine.kt) | dispersos | Boost de termos canônicos |

### Exemplo da divergência

**`MestreIAQueryEngine.kt:29+`:**
```kotlin
private val sinonimos = mapOf(
    "colis"  to listOf("colis", "encontr", "impact"),
    "dano"   to listOf("dano", "ferim", "lesao"),
    /* ~40 entradas */
)
```

**`MestreIAPlanner.kt:99+`:**
```kotlin
private val grupoMagia = setOf(
    "magia", "feitico", "encantamento", "conjuracao", "escola", "mana", "energia",
    "bloqueio", "fireball", "cura", "ilusao", "telecinese", /* ... */
)
private val grupoCombateCaC = setOf(/* ... */)
private val grupoAmbiente   = setOf(/* ... */)
```

### Diagnóstico
- São **estruturas diferentes** (Map vs. Sets agrupados) servindo ao mesmo propósito (expansão de query).
- Já há regra documentada de **sincronizar manualmente** — porém isso é frágil.
- Quando um termo novo for adicionado, é fácil esquecer de propagar.

### Refatoração Sugerida
Criar `domain/MestreIASynonyms.kt` como **fonte única**:

```kotlin
object MestreIASynonyms {
    val GROUPS: Map<String, Set<String>> = mapOf(
        "magia"   to setOf("magia", "feitico", "encantamento", /* ... */),
        "combate" to setOf("combate", "luta", /* ... */),
        /* ... */
    )

    fun expandQuery(termo: String): List<String> { /* deriva sinônimos */ }
    fun groupOf(termo: String): String? { /* retorna grupo semântico */ }
}
```

E reescrever `QueryEngine`, `Planner` e `GraphEngine` para consumir esse objeto.

**Economia estimada:** ~50–80 linhas, e **elimina o risco crítico de RAG inconsistente**.

---

## 3. 🔴 Diálogos Compose com Estrutura Idêntica — 13 Arquivos

### Evidência

| Arquivo | Composable | Linhas aprox. |
|---------|------------|----------------|
| [DialogsPericias.kt:96-150](app/src/main/java/com/gurps/ficha/ui/DialogsPericias.kt#L96-L150) | `SelecionarPericiaDialog` | 54 |
| [DialogsTecnicas.kt:70-150](app/src/main/java/com/gurps/ficha/ui/DialogsTecnicas.kt#L70-L150) | `SelecionarTecnicaDialog` | 80 |
| [TraitDialogs.kt:34-100](app/src/main/java/com/gurps/ficha/ui/features/traits/TraitDialogs.kt#L34-L100) | `SeletorListaTraitsDialog` | 66 |
| [TraitDialogsV2.kt:28-100](app/src/main/java/com/gurps/ficha/ui/features/traits/TraitDialogsV2.kt#L28-L100) | `ModeloRacialDialog` | 72 |
| [VantagemDialogs.kt](app/src/main/java/com/gurps/ficha/ui/features/traits/VantagemDialogs.kt) | múltiplos seletores | — |
| [DesvantagemDialogs.kt](app/src/main/java/com/gurps/ficha/ui/features/traits/DesvantagemDialogs.kt) | múltiplos seletores | — |
| [SelectingMagicDialog.kt](app/src/main/java/com/gurps/ficha/ui/features/magic/SelectingMagicDialog.kt) | seleção de magia | — |
| [MagicDialogs.kt](app/src/main/java/com/gurps/ficha/ui/features/magic/MagicDialogs.kt) | seleção de escola | — |
| [DialogsCommon.kt](app/src/main/java/com/gurps/ficha/ui/DialogsCommon.kt) | confirmação | — |
| [RolagemPrimaryDialogs.kt](app/src/main/java/com/gurps/ficha/ui/features/rolagem/RolagemPrimaryDialogs.kt) | seletor de modificador | — |
| [RolagemSecondaryDialogs.kt](app/src/main/java/com/gurps/ficha/ui/features/rolagem/RolagemSecondaryDialogs.kt) | seletor canal | — |
| [DialogsTracos.kt](app/src/main/java/com/gurps/ficha/ui/DialogsTracos.kt) | legado | — |
| [DialogsMagias.kt](app/src/main/java/com/gurps/ficha/ui/DialogsMagias.kt) | magia | — |

### Estrutura repetida (esqueleto)
```kotlin
@Composable
fun SelecionarXDialog(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit
) {
    var busca by remember { mutableStateOf("") }
    var selecionado by remember { mutableStateOf<T?>(null) }
    var filtro by remember { mutableStateOf<String?>(null) }

    val items = remember { /* carrega catálogo */ }
    val filtrada = items.filter {
        val matchBusca = busca.isBlank() || contemBusca(it.nome, busca)
        val matchFiltro = filtro.isNullOrBlank() || /* ... */
        matchBusca && matchFiltro
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = {
            Column {
                OutlinedTextField(busca, { busca = it }, label = { Text("Buscar...") })
                LazyColumn {
                    items(filtrada) { item ->
                        ListItem(
                            headlineContent = { Text(item.nome) },
                            modifier = Modifier.clickable { selecionado = item }
                        )
                    }
                }
            }
        }
    )
}
```

### Refatoração Sugerida
Criar **um composable genérico** em `ui/components/CatalogPickerDialog.kt`:

```kotlin
@Composable
fun <T> CatalogPickerDialog(
    titulo: String,
    items: List<T>,
    nameOf: (T) -> String,
    filters: List<DialogFilter<T>> = emptyList(),    // dropdowns extras opcionais
    itemContent: @Composable (T) -> Unit = { DefaultItemRow(nameOf(it)) },
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
)

data class DialogFilter<T>(
    val label: String,
    val options: List<String>,
    val matches: (T, String) -> Boolean,
)
```

E reescrever todos os 13 diálogos como chamadas paramétricas a esse composable.

**Economia estimada:** ~120 linhas; UX 100% consistente; busca sempre via `TextNormalizer` centralizado.

---

## 4. 🟡 TraitRules — `calculateCost()` Duplicado em 12 Arquivos

### Evidência

| Arquivo | Linhas totais | Complexidade |
|---------|---------------|--------------|
| [ApararAmpliadoRule.kt](app/src/main/java/com/gurps/ficha/domain/rules/traits/ApararAmpliadoRule.kt) | ~22 | Simples |
| [BloqueioAmpliadoRule.kt](app/src/main/java/com/gurps/ficha/domain/rules/traits/BloqueioAmpliadoRule.kt) | ~22 | Simples |
| [EsquivaAmpliadaRule.kt](app/src/main/java/com/gurps/ficha/domain/rules/traits/EsquivaAmpliadaRule.kt) | ~22 | Simples |
| [FlexibilidadeRule.kt](app/src/main/java/com/gurps/ficha/domain/rules/traits/FlexibilidadeRule.kt) | ~40 | Intermediária |
| [TelecomunicacaoRule.kt](app/src/main/java/com/gurps/ficha/domain/rules/traits/TelecomunicacaoRule.kt) | ~40 | Intermediária |
| [AtaqueInatoRule.kt:17-54](app/src/main/java/com/gurps/ficha/domain/rules/traits/AtaqueInatoRule.kt#L17-L54) | ~95 | Complexa |
| [DentesRule.kt:17-42](app/src/main/java/com/gurps/ficha/domain/rules/traits/DentesRule.kt#L17-L42) | ~95 | Complexa |
| [GarrasRule.kt:16-38](app/src/main/java/com/gurps/ficha/domain/rules/traits/GarrasRule.kt#L16-L38) | ~95 | Complexa |
| [GolpeadoresRule.kt:17-51](app/src/main/java/com/gurps/ficha/domain/rules/traits/GolpeadoresRule.kt#L17-L51) | ~125 | Complexa |
| [MestreDeArmasRule.kt](app/src/main/java/com/gurps/ficha/domain/rules/traits/MestreDeArmasRule.kt) | 214 | Muito complexa |

### Padrão repetido em **todas** (com pequenas variações):
```kotlin
override fun calculateCost(
    selection: VantagemSelecionada,
    modifiers: List<ModificadorSelecao>
): Int {
    val custoBase = when (tipo.lowercase()) {
        "tipo1" -> valor1
        "tipo2" -> valor2
        else    -> valorPadrao
    }
    val somaPercentual = modifiers.sumOf {
        it.bonusBase + if (it.porNivel) it.valor * it.niveis else it.valor
    }
    val percentualFinal = somaPercentual.coerceAtLeast(-80)
    val multiplicadorMod = 1.0 + (percentualFinal / 100.0)
    return ceil(custoBase * multiplicadorMod).toInt().coerceAtLeast(1)
}
```

A parte **só varia no `when (tipo)`** — o restante (acúmulo de modificadores, regra do -80%, ceil) é **idêntico**.

### Refatoração Sugerida

Adicionar à interface `TraitRule.kt` (ou criar `AbstractTraitRule`):

```kotlin
abstract class AbstractTraitRule : TraitRule {
    protected abstract fun baseCost(selection: VantagemSelecionada): Int

    final override fun calculateCost(
        selection: VantagemSelecionada,
        modifiers: List<ModificadorSelecao>
    ): Int {
        val base = baseCost(selection)
        val pct  = modifiers.sumOf {
            it.bonusBase + if (it.porNivel) it.valor * it.niveis else it.valor
        }.coerceAtLeast(-80)
        return ceil(base * (1.0 + pct / 100.0)).toInt().coerceAtLeast(1)
    }
}
```

Cada Rule passa a implementar apenas `baseCost()` — a regra dos modificadores fica centralizada.

**Economia estimada:** ~100 linhas; corrige o risco de uma das Rules implementar errado a regra do -80%.

---

## 5. 🟡 Clientes HTTP — `MestreIAClient` e `DiscordRollApiClient`

### Evidência

**[MestreIAClient.kt:101-106](app/src/main/java/com/gurps/ficha/data/network/MestreIAClient.kt#L101-L106):**
```kotlin
val connection = url.openConnection() as HttpURLConnection
connection.requestMethod = "POST"
connection.doOutput = true
connection.connectTimeout = CONNECT_TIMEOUT_MS
connection.readTimeout = READ_TIMEOUT_MS
connection.setRequestProperty("Content-Type", "application/json")
```

**[DiscordRollApiClient.kt:78-84](app/src/main/java/com/gurps/ficha/data/network/DiscordRollApiClient.kt#L78-L84):**
```kotlin
connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
    requestMethod = "POST"
    doOutput = true
    connectTimeout = CONNECT_TIMEOUT_MS
    readTimeout = READ_TIMEOUT_MS
    setRequestProperty("Content-Type", "application/json; charset=utf-8")
    setRequestProperty("x-api-key", apiKey)
}
```

Padrão também aparece em **`VttRollService`**, **`VttSessionService`**, **`VttTokenBindService`** e **`VttHostAutoDetect`** — todos os 4 services de VTT fazem o mesmo POST/GET manualmente.

### Refatoração Sugerida
Criar `data/network/HttpHelper.kt`:

```kotlin
object HttpHelper {
    data class Result(
        val ok: Boolean,
        val statusCode: Int?,
        val body: String? = null,
        val error: String? = null,
    )

    fun postJson(
        url: String,
        jsonBody: String,
        headers: Map<String, String> = emptyMap(),
        connectTimeoutMs: Int = 10_000,
        readTimeoutMs: Int = 30_000,
    ): Result { /* ... */ }

    fun getJson(url: String, headers: Map<String, String> = emptyMap()): Result { /* ... */ }
}
```

**Economia estimada:** ~30–50 linhas e — mais importante — **comportamento uniforme** de timeout/retry/leitura de stream em todos os clientes.

---

## 6. 🟡 Filtros de Catálogo — 7 Funções com Mesmo Esqueleto

### Evidência

| Linha em [CatalogFilters.kt](app/src/main/java/com/gurps/ficha/domain/filters/CatalogFilters.kt) | Função |
|----|--------|
| 45-59 | `filtrarVantagens()` |
| 67-74 | `filtrarDesvantagens()` |
| 76-91 | `filtrarPericias()` |
| 93-107 | `filtrarTecnicasCatalogo()` |
| 109-125 | `filtrarArmasCatalogo()` |
| 127-138 | `filtrarEscudosCatalogo()` |
| 140-165 | `filtrarArmadurasCatalogo()` |

### Padrão comum
```kotlin
fun filtrarX(
    items: List<T>,
    busca: String = "",
    filtro1: String? = null,
    filtro2: String? = null,
): List<T> = items.filter { item ->
    val matchBusca = contemBusca(item.nome, busca)
    val matchF1 = filtro1.isNullOrBlank() || contemBusca(item.campo1, filtro1)
    val matchF2 = filtro2.isNullOrBlank() || contemBusca(item.campo2, filtro2)
    matchBusca && matchF1 && matchF2
}.sortedBy { it.nome.lowercase() }
```

### Refatoração Sugerida

```kotlin
data class CatalogFilter<T>(
    val items: List<T>,
    val nameOf: (T) -> String,
    val predicates: List<(T) -> Boolean> = emptyList(),
) {
    fun apply(busca: String): List<T> =
        items.filter { item ->
            val matchBusca = busca.isBlank() || TextNormalizer.contains(nameOf(item), busca)
            matchBusca && predicates.all { it(item) }
        }.sortedBy { nameOf(it).lowercase() }
}
```

E cada `filtrarX` vira só uma fábrica de `CatalogFilter` com os predicados certos.

**Economia estimada:** ~40 linhas; e **garante** que `contemBusca` use sempre a mesma normalização.

---

## 7. 🟡 SharedPreferences — 3 Stores com Mesmo Padrão

### Evidência

| Arquivo | Linhas |
|---------|--------|
| [VttSessionStorage.kt:27-51](app/src/main/java/com/gurps/ficha/vtt/VttSessionStorage.kt#L27-L51) | `load()`/`save()` campo a campo |
| [MetacaracteristicaStore.kt](app/src/main/java/com/gurps/ficha/data/storage/MetacaracteristicaStore.kt) | mesmo padrão sobre JSON |
| [FichaStorageRepository.kt:12-20](app/src/main/java/com/gurps/ficha/data/storage/FichaStorageRepository.kt#L12-L20) | leitura/escrita + migration |

### Refatoração Sugerida

Criar `data/storage/PrefStore.kt` como helper:

```kotlin
abstract class PrefStore<T>(private val prefName: String) {
    abstract fun toMap(value: T): Map<String, Any?>
    abstract fun fromPrefs(p: SharedPreferences): T

    fun load(ctx: Context): T = fromPrefs(prefsOf(ctx))
    fun save(ctx: Context, value: T) {
        prefsOf(ctx).edit().apply {
            toMap(value).forEach { (k, v) -> when (v) {
                is String -> putString(k, v); is Int -> putInt(k, v); /* ... */
            }}
        }.apply()
    }

    private fun prefsOf(ctx: Context) =
        ctx.getSharedPreferences(prefName, Context.MODE_PRIVATE)
}
```

**Economia estimada:** ~35 linhas; padrão único de keys e migração.

---

## 8. 🟡 Trait Dialogs V1 vs V2 — Duas Versões Convivendo

### Evidência

[TraitDialogs.kt:23-30](app/src/main/java/com/gurps/ficha/ui/features/traits/TraitDialogs.kt#L23-L30) contém comentário declarando que **foi modularizado** para `VantagemDialogs.kt` e `DesvantagemDialogs.kt`, mas:

- [TraitDialogsV2.kt](app/src/main/java/com/gurps/ficha/ui/features/traits/TraitDialogsV2.kt) ainda existe com **mais ~70 linhas** que duplicam o `SeletorListaTraitsDialog`.

### Diagnóstico
Há uma migração V1 → V2 **incompleta**: os 3 arquivos coexistem. A regra DRY recomenda concluir a transição.

### Sugestão
Após implementar o `CatalogPickerDialog` da seção 3, **deletar `TraitDialogsV2.kt`** e migrar os usos para o composable genérico. Documentar essa migração no MAPA_DETALHADO §20.

**Economia estimada:** ~50 linhas.

---

## 9. 🟡 Parsing JSON Manual em Models

### Evidência

Modelos que constroem `JSONObject` manualmente em `toJson()`/`fromJson()`:
- [Personagem.kt](app/src/main/java/com/gurps/ficha/model/Personagem.kt) — modelo raiz
- [MestreIAResponse.kt](app/src/main/java/com/gurps/ficha/data/network/MestreIAResponse.kt)
- [PersonagemInterop.kt](app/src/main/java/com/gurps/ficha/model/PersonagemInterop.kt) — encapsulador
- [ArmaCatalogoItem.kt](app/src/main/java/com/gurps/ficha/model/ArmaCatalogoItem.kt)
- [ArmaduraCatalogoItem.kt](app/src/main/java/com/gurps/ficha/model/ArmaduraCatalogoItem.kt)

### Diagnóstico
Não é exatamente duplicação literal, mas **padrão repetido** de:
- `JSONObject().put("campo", valor).put(...)`
- `try { JSONObject(s).optString("campo", "") }`
- `if (json.has("x")) json.getJSONArray("x").let { /* loop */ }`

Cada modelo reimplementa a serialização — o que **silenciosamente esconde divergências de schema** (ex: `Personagem.toJson` pode salvar um campo que `fromJson` não lê).

### Refatoração Sugerida
Migrar para `kotlinx.serialization` (anotações `@Serializable`) — elimina centenas de linhas de boilerplate em todo o projeto e garante simetria toJson↔fromJson em tempo de compilação.

**Trade-off:** mudança de biblioteca, requer adicionar plugin Gradle. Avaliar custo-benefício.

**Economia estimada:** ~25 linhas no curto prazo (e potencialmente centenas se migrar todos os models).

---

## 10. 🟢 `coerceIn` em Atributos — `FichaAttributeDelegate`

### Evidência

[FichaAttributeDelegate.kt:14-30](app/src/main/java/com/gurps/ficha/viewmodel/delegates/FichaAttributeDelegate.kt#L14-L30):

```kotlin
fun atualizarForca(p: Personagem, v: Int)        = p.copy(forca       = v.coerceIn(1, 30))
fun atualizarDestreza(p: Personagem, v: Int)     = p.copy(destreza    = v.coerceIn(1, 30))
fun atualizarInteligencia(p: Personagem, v: Int) = p.copy(inteligencia = v.coerceIn(1, 30))
fun atualizarVitalidade(p: Personagem, v: Int)   = p.copy(vitalidade  = v.coerceIn(1, 30))
```

### Sugestão
Aceitável como está — é **fortemente tipado** e a UI já chama métodos específicos. Refatorar com reflexão/data-driven seria *over-engineering* (vide guidance no `CLAUDE.md` global: "Don't add abstractions beyond what the task requires"). **Manter como está.**

**Economia estimada:** ~8 linhas — não vale a pena.

---

## 11. 🟢 `try/catch` Genéricos com Log

### Evidência
Padrão `try { ... } catch (e: Exception) { Log.e(TAG, e.message); return null }` aparece em:
- [MestreIAClient.kt:82-150](app/src/main/java/com/gurps/ficha/data/network/MestreIAClient.kt#L82-L150)
- [DiscordRollApiClient.kt:65-104](app/src/main/java/com/gurps/ficha/data/network/DiscordRollApiClient.kt#L65-L104)
- [DataRepository.kt](app/src/main/java/com/gurps/ficha/data/DataRepository.kt) (múltiplos)
- [MestreIARepository.kt](app/src/main/java/com/gurps/ficha/data/MestreIARepository.kt) (múltiplos)

### Sugestão
Em geral, **deixar como está**. Cada `catch` tem contexto específico (tag de log, ação de fallback). Forçar um helper único pode ofuscar.

**Apenas vale criar um helper se** o HttpHelper da seção 5 for adotado — aí o try/catch dos clientes some naturalmente.

**Economia estimada:** ~20 linhas (de brinde, se a seção 5 for feita).

---

## Plano de Refatoração Recomendado (em Lotes)

Seguindo o [feedback_lote_protocol.md](C:/Users/Rodolfo/.claude/projects/c--Users-Rodolfo-Desktop-ficha-gurps/memory/feedback_lote_protocol.md), sugere-se 3 lotes incrementais:

### **Lote A — Risco Crítico (RAG e Buscas)**
1. Criar `TextNormalizer` central → eliminar 7 clones (§1).
2. Criar `MestreIASynonyms` central → unificar 3 dicionários (§2).
3. Rodar `NexusArcanoLoteFCanonicScenarioTest.kt` + `RulesLayerTest.kt`.

### **Lote B — UI Consolidada**
4. Criar `CatalogPickerDialog<T>` genérico (§3).
5. Migrar 13 dialogs gradualmente, **um por commit**.
6. Concluir migração V1→V2 (§8) — deletar `TraitDialogsV2.kt`.

### **Lote C — Infra (opcional)**
7. `HttpHelper` para clientes HTTP (§5).
8. `AbstractTraitRule` para regras de traços (§4).
9. `PrefStore<T>` para SharedPreferences (§7).
10. `CatalogFilter<T>` para filtros (§6).

---

## Observações Finais

- **Não confundir DRY com over-engineering.** As seções §10 e §11 **deliberadamente não recomendam refatoração** — o ganho é cosmético e o código atual é claro.
- **Prioridade absoluta** é a seção §2 (sinônimos RAG): a memória [feedback_rag_maintenance.md](C:/Users/Rodolfo/.claude/projects/c--Users-Rodolfo-Desktop-ficha-gurps/memory/feedback_rag_maintenance.md) já marca como crítico.
- **Cobertura de testes**: antes de qualquer refatoração de regras, garantir que `RulesLayerTest.kt`, `PersonagemRulesTest.kt` e a suíte do Nexus Arcano passam. São a rede de segurança.
- **Validação visual**: refatorações de UI (§3, §8) **precisam ser testadas no app**, conforme guideline de `verify` skill — type-check não basta.

---

> ⚠️ **Este relatório é descritivo.** Nenhum arquivo de código foi alterado. Para iniciar a refatoração, defina um lote, abra um plano (`/plan`) e aplique as mudanças seguindo o protocolo de lotes do projeto.
