package com.gurps.ficha.domain.magic

import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Test
import java.io.File

/**
 * Lote MA-1 — REALITY CHECK: parseia TODAS as 879 magias do catálogo real
 * (`magias2versao.json`) e valida:
 *   1. Nenhuma faz o parser crashar.
 *   2. Toda `classe` bruta reconhece pelo menos UMA classe canônica OU cai em ESPECIAL (fallback ok).
 *   3. Toda resistência do tipo `R-XXX` produz `resistencia != null` (o parser não pode "engolir" resistência).
 *
 * Se o arquivo não existir (build headless), o teste é PULADO (Assume) sem falhar.
 */
class MagicCatalogRealityCheckTest {

    private fun carregarCatalogo(): JSONArray? {
        val paths = listOf(
            "src/main/assets/magias2versao.json",           // rodando de app/
            "app/src/main/assets/magias2versao.json",       // rodando da raiz do projeto
        )
        val arquivo = paths.map { File(it) }.firstOrNull { it.exists() } ?: return null
        val texto = arquivo.readText(Charsets.UTF_8)
        return JSONArray(texto)
    }

    @Test
    fun `parser aceita TODAS as classes do JSON real sem crashar`() {
        val catalogo = carregarCatalogo()
        Assume.assumeNotNull(catalogo)
        var total = 0
        var comParteNaoReconhecida = 0
        val exemplosNaoReconhecidos = mutableListOf<String>()

        for (i in 0 until catalogo!!.length()) {
            val magia = catalogo.getJSONObject(i)
            val classeStr = magia.optString("classe", null)
            val id = magia.optString("id", "?")
            val parsed = MagicClassParser.parse(classeStr) // não pode lançar
            total++
            if (parsed.temParteNaoReconhecida) {
                comParteNaoReconhecida++
                if (exemplosNaoReconhecidos.size < 10) {
                    exemplosNaoReconhecidos.add("$id [$classeStr] -> classes=${parsed.classes} res=${parsed.resistencia}")
                }
            }
        }

        println("[MagicCatalogRealityCheck] total=$total, comParteNaoReconhecida=$comParteNaoReconhecida")
        exemplosNaoReconhecidos.forEach { println("  ↳ $it") }

        // Limite honesto: até 3% de "parte não reconhecida" é aceitável (~25 magias raras). Se subir
        // além disso, o parser precisa de mais aliases.
        val fracao = comParteNaoReconhecida.toDouble() / total
        assertTrue("Fração de magias com parte não reconhecida acima de 3%: $fracao ($comParteNaoReconhecida / $total)",
            fracao <= 0.03)
    }

    @Test
    fun `parser NUNCA perde a resistencia R-XXX declarada no JSON`() {
        val catalogo = carregarCatalogo()
        Assume.assumeNotNull(catalogo)
        val perdidas = mutableListOf<String>()
        for (i in 0 until catalogo!!.length()) {
            val magia = catalogo.getJSONObject(i)
            val classeStr = magia.optString("classe", "")
            val id = magia.optString("id", "?")
            // Se a classe menciona R-, o parser TEM que devolver alguma resistencia.
            val temResistenciaNaString = "R-" in classeStr || "R/" in classeStr || " R " in classeStr
            val parsed = MagicClassParser.parse(classeStr)
            if (temResistenciaNaString && parsed.resistencia == null) {
                perdidas.add("$id [$classeStr]")
            }
        }
        if (perdidas.isNotEmpty()) {
            println("[MagicCatalogRealityCheck] perdidas: ${perdidas.size}")
            perdidas.take(20).forEach { println("  ↳ $it") }
        }
        assertTrue("Parser PERDEU resistência em ${perdidas.size} magia(s): ${perdidas.take(5)}",
            perdidas.isEmpty())
    }

    // ── Lote MEC-5: o custo do catálogo REAL ────────────────────────────────────────────────────

    @Test
    fun `nenhuma magia do catalogo real perde o custo no formato operar-manter`() {
        // O bug: "04/02" (operar 4, manter 2) casava no regex de FRAÇÃO e virava 2,0 com base=null —
        // 307 das 879. Esta trava roda contra o catálogo real para não voltar a passar batido.
        val catalogo = carregarCatalogo()
        Assume.assumeNotNull(catalogo)
        val quebradas = mutableListOf<String>()
        var comOperarManter = 0
        val formato = Regex("""^\s*\d+\s*/\s*\d+\s*#?\s*$""")
        for (i in 0 until catalogo!!.length()) {
            val magia = catalogo.getJSONObject(i)
            val bruto = magia.optString("energia", "")
            if (!formato.matches(bruto)) continue
            comOperarManter++
            val c = MagicEnergy.parse(bruto)
            val esperadoOperar = bruto.trim().removeSuffix("#").split("/")[0].trim().toInt()
            val esperadoManter = bruto.trim().removeSuffix("#").split("/")[1].trim().toInt()
            if (c.base != esperadoOperar || c.manutencao != esperadoManter || c.fracao != null)
                quebradas += "${magia.optString("nome")}: '$bruto' → base=${c.base} manut=${c.manutencao} fracao=${c.fracao}"
        }
        println("MEC-5: $comOperarManter mágicas no formato operar/manter no catálogo real.")
        assertTrue("o catálogo real precisa ter mágicas nesse formato para o teste valer", comOperarManter > 100)
        assertTrue("Custo lido ERRADO em ${quebradas.size} magia(s): ${quebradas.take(5)}", quebradas.isEmpty())
    }

    /**
     * Lote P3-1: as 6 mágicas curadas neste lote têm que continuar EXECUTÁVEIS no catálogo real.
     * Uma curadoria só vale enquanto o dado sobrevive — foi assim que 156 buffs viraram rótulo.
     */
    @Test
    fun `as 6 magias curadas no P3-1 continuam executaveis no catalogo real`() {
        val catalogo = carregarCatalogo()
        Assume.assumeNotNull(catalogo)
        // id → (campo que precisa existir, valor esperado)
        val esperado = mapOf(
            "bloquear" to Triple("buffBd", 1, 1),                    // BD +1 por 1 de energia
            "robustez" to Triple("buffRd", 1, 1),                    // RD +1 por 1 de energia
            "fortalecer_vontade" to Triple("buffAtributoValor", 1, 1),
            "enfraquecer_vontade" to Triple("buffAtributoValor", -1, 2), // -1 a cada 2 de energia
            "sabedoria" to Triple("buffAtributoValor", 1, 4),        // +1 de IQ a cada 4
            "tolice" to Triple("buffAtributoValor", -1, 1),
        )
        val faltando = mutableListOf<String>()
        var achadas = 0
        for (i in 0 until catalogo!!.length()) {
            val magia = catalogo.getJSONObject(i)
            val alvo = esperado[magia.optString("id")] ?: continue
            achadas++
            val mec = magia.optJSONObject("mecanica")
            if (mec == null) { faltando += "${magia.optString("id")}: sem mecanica"; continue }
            val (campo, valor, porNivel) = alvo
            if (mec.optInt(campo, 0) != valor)
                faltando += "${magia.optString("id")}: $campo=${mec.optInt(campo, 0)}, esperado $valor"
            if (mec.optInt("buffEnergiaPorNivel", 0) != porNivel)
                faltando += "${magia.optString("id")}: energiaPorNivel=${mec.optInt("buffEnergiaPorNivel", 0)}, esperado $porNivel"
            if (mec.optInt("buffMaxNiveis", 0) != 5)
                faltando += "${magia.optString("id")}: maxNiveis deveria ser 5 (teto do livro)"
        }
        assertTrue("as 6 mágicas têm que existir no catálogo (achei $achadas)", achadas == 6)
        assertTrue("curadoria do P3-1 regrediu: $faltando", faltando.isEmpty())
    }

    /**
     * Lote A1: as 4 mágicas de Imunidade têm que conceder o elemento certo, e as 23 de dano
     * elemental têm que declarar o elemento — senão a imunidade não tem o que consultar.
     */
    @Test
    fun `no catalogo real as imunidades concedem o elemento certo e o dano elemental o declara`() {
        val catalogo = carregarCatalogo()
        Assume.assumeNotNull(catalogo)
        val esperadoImun = mapOf(
            "imunidade_ao_fogo" to "fogo", "imunidade_ao_frio" to "frio",
            "imunidade_a_relampagos" to "eletricidade", "imunidade_a_acido" to "acido",
        )
        val erros = mutableListOf<String>()
        var comElemento = 0
        var imunConferidas = 0
        for (i in 0 until catalogo!!.length()) {
            val magia = catalogo.getJSONObject(i)
            val id = magia.optString("id")
            val mec = magia.optJSONObject("mecanica") ?: continue
            val elem = mec.optString("elementoDano", "").ifBlank { null }
            if (elem != null) comElemento++
            esperadoImun[id]?.let { esperado ->
                imunConferidas++
                if (mec.optString("buffImunidade", "") != esperado)
                    erros += "$id: buffImunidade='${mec.optString("buffImunidade", "")}', esperado '$esperado'"
            }
            // O livro proíbe tratar estas como elemento — travar para ninguém "corrigir" por engano.
            if (id in setOf("adaga_de_gelo", "esfera_de_gelo", "jato_de_vapor", "sopro_de_vapor") && elem != null)
                erros += "$id NÃO pode ter elementoDano ('$elem'): o livro a exclui da imunidade"
        }
        assertEquals("as 4 mágicas de Imunidade têm que existir", 4, imunConferidas)
        assertTrue("poucas mágicas com elemento ($comElemento) — a curadoria regrediu", comElemento >= 20)
        assertTrue("curadoria de elemento regrediu: $erros", erros.isEmpty())
    }

    /**
     * Lote A1-b: as duas mágicas de tique que o motor EXECUTA dizem, com todas as letras, que
     * mortos-vivos não são afetados. Se a curadoria sumir, o tique volta a bater em esqueleto.
     */
    @Test
    fun `Morte Candente e Morte Putrefata excluem morto-vivo no catalogo real`() {
        val catalogo = carregarCatalogo()
        Assume.assumeNotNull(catalogo)
        var conferidas = 0
        val erros = mutableListOf<String>()
        for (i in 0 until catalogo!!.length()) {
            val magia = catalogo.getJSONObject(i)
            if (magia.optString("id") !in setOf("morte_candente", "morte_putrefata")) continue
            conferidas++
            val lista = magia.optJSONObject("mecanica")?.optJSONArray("naoAfeta")
            val tem = (0 until (lista?.length() ?: 0)).any { lista!!.optString(it) == "morto_vivo" }
            if (!tem) erros += magia.optString("id")
        }
        assertEquals("as duas mágicas têm que existir no catálogo", 2, conferidas)
        assertTrue("perderam a exclusão de morto-vivo: $erros", erros.isEmpty())
    }

    /**
     * Lote TOK-ZONA: a duração das mágicas de ZONA tem que bater com o livro.
     *
     * Achado no teste de aparelho: o usuário relatou não ver as zonas acabarem. A investigação
     * mostrou que quase todas estão certas (Chuva de Fogo dura 1 minuto = 60 turnos, e uma luta não
     * dura tanto), MAS a Tempestade de Faíscas estava com **1 min.** contra os **10 segundos** que a
     * descrição do catálogo dizia.
     *
     * **MC-1 (13/out/2026) desfez isso:** a tabela-resumo do livro (p.197) e a própria entrada dizem
     * "1 minuto após atingir a força total". Os 10 segundos eram da Nuvem de Faíscas, ao lado, e a
     * descrição do catálogo (transcrita por IA) tinha herdado o número errado. O livro manda: 60.
     */
    @Test
    fun `duracao das magias de zona bate com o livro`() {
        val catalogo = carregarCatalogo()
        Assume.assumeNotNull(catalogo)
        val esperado = mapOf(
            "chuva_de_acido" to 60, "chuva_de_fogo" to 60, "chuva_de_pedras" to 60,
            "mau_cheiro" to 300, "nuvem_de_faiscas" to 10, "nuvem_de_fogo" to 10,
            "tempestade_de_faiscas" to 60,
        )
        val erros = mutableListOf<String>()
        var vistas = 0
        for (i in 0 until catalogo!!.length()) {
            val magia = catalogo.getJSONObject(i)
            val seg = esperado[magia.optString("id")] ?: continue
            vistas++
            val (_, lido) = MagicTime.parseDuracao(magia.optString("duracao", ""))
            if (lido != seg) erros += "${magia.optString("id")}: leu ${lido}s, o livro diz ${seg}s"
        }
        assertEquals("as 7 mágicas de zona têm que existir", 7, vistas)
        assertTrue("duração divergente do livro: $erros", erros.isEmpty())
    }

    /** Lote P3-1: Bloquear e Robustez são de Bloqueio — valem UM ataque só, não a duração toda. */
    @Test
    fun `Bloquear e Robustez sao marcadas como buff de um unico uso`() {
        val catalogo = carregarCatalogo()
        Assume.assumeNotNull(catalogo)
        val faltando = mutableListOf<String>()
        for (i in 0 until catalogo!!.length()) {
            val magia = catalogo.getJSONObject(i)
            if (magia.optString("id") !in setOf("bloquear", "robustez")) continue
            val mec = magia.optJSONObject("mecanica")
            if (mec?.optBoolean("buffUmUnicoUso", false) != true)
                faltando += magia.optString("id")
        }
        assertTrue("sem buffUmUnicoUso viram RD/BD persistente — regra errada: $faltando",
            faltando.isEmpty())
    }

    /**
     * Lote MG-0 (Mesa): a `classe` do catálogo só tem as grafias CERTAS.
     *
     * O parser aceita "Comm", "Projetil", "Bloq.", "Encant." — mas a Mesa lê a `classe` com regex
     * simples, e "Comm" fazia a penalidade de distância não valer em 7 magias, em silêncio. Os
     * apelidos continuam aceitos pelo parser (os testes dele provam); o que não pode é voltarem para
     * o DADO. Para consertar: `node scripts/auditar-magias.js --normalizar-classe`, em `mesa-virtual/server`.
     */
    @Test
    fun `toda classe do catalogo real usa so as grafias certas`() {
        val catalogo = carregarCatalogo()
        Assume.assumeNotNull(catalogo)
        val certas = setOf(
            "Comum", "Área", "Projétil", "Toque", "Bloqueio", "Informação", "Encantamento", "Especial",
        )
        val sujas = mutableListOf<String>()
        for (i in 0 until catalogo!!.length()) {
            val magia = catalogo.getJSONObject(i)
            val classe = magia.optString("classe", "")
            val segmentos = classe.split("/")
            // "Área/R-(ST+Vont)/2": o "2" é continuação da resistência, não classe.
            val ok = segmentos.all { seg ->
                seg == seg.trim() && (
                    seg.startsWith("R-") || seg.matches(Regex("""\d+""")) ||
                        seg.split(" ou ").all { it in certas }
                    )
            }
            if (!ok) sujas += "${magia.optString("id")}: '$classe'"
        }
        assertTrue("classe fora das grafias certas em ${sujas.size} magia(s): ${sujas.take(8)}", sujas.isEmpty())
    }

    /**
     * Lote MG-0 (Mesa, fecho): nenhum `id` do catálogo se repete.
     *
     * A magia da p.31 (Animais) vinha com o id `passo_leve` — o do Passo Leve da p.144. A Mesa guarda
     * por `id` e a segunda sobrescrevia a primeira; e a regra de pré-requisito do `MagicEngine` e o
     * texto do `DataRepository`, presos ao id `passageiro_interno`, nunca valiam para ninguém.
     */
    @Test
    fun `nenhum id do catalogo real se repete e o Passageiro Interno tem o id dele`() {
        val catalogo = carregarCatalogo()
        Assume.assumeNotNull(catalogo)
        val magias = (0 until catalogo!!.length()).map { catalogo.getJSONObject(it) }
        val repetidos = magias.map { it.optString("id") }.groupBy { it }.filter { it.value.size > 1 }.keys
        assertTrue("id repetido no catalogo real: $repetidos", repetidos.isEmpty())

        val porId = magias.associateBy { it.optString("id") }
        // O Passageiro Interno é o da p.31; o Passo Leve é o da Escola de Deslocamento.
        assertEquals(31, porId.getValue("passageiro_interno").optInt("pagina"))
        assertEquals("Passageiro Interno", porId.getValue("passageiro_interno").optString("nome"))
        // MC-1: a tabela e o título do livro estão na p.145 (o catálogo dizia 144)
        assertEquals(145, porId.getValue("passo_leve").optInt("pagina"))
    }
}
