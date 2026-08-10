package com.gurps.ficha.domain.rules

import java.io.File
import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Lote PV-1a** — o mapa de toque da silhueta.
 *
 * ## 🔴 Por que este teste existe
 *
 * Um mapa de toque errado é o pior defeito que este app pode ter, porque ele
 * **não parece um defeito**: a tela continua bonita, o toque responde, o número
 * aparece. Só está no membro errado. O jogador descobre três sessões depois, com
 * o braço errado incapacitado na ficha, e nem consegue reconstruir de onde veio.
 *
 * Nada disso precisa de aparelho: a arte está no repositório e as regras são
 * aritmética. O teste confere região por região contra a **máscara do corpo de
 * verdade**, e o hash amarra essa máscara ao PNG que o app mostra.
 *
 * ## ⚠️ O caso que mais importa é o do lado
 *
 * "Esquerdo" é o lado **do personagem**, e a figura está de frente para quem
 * olha — então o braço esquerdo dele aparece à **direita** da imagem. Trocar os
 * dois é uma linha de código, não quebra nada, e não aparece em nenhum lugar
 * exceto na ficha de quem perdeu o braço. `o lado esquerdo dele aparece à
 * direita da imagem` é o teste que trava isso.
 */
class MapaDaSilhuetaTest {

    private fun achar(vararg caminhos: String): File? =
        caminhos.map { File(it) }.firstOrNull { it.exists() }

    private fun arquivoDaArte() = achar(
        "src/main/res/drawable-nodpi/silhueta_corpo.png",
        "app/src/main/res/drawable-nodpi/silhueta_corpo.png"
    )

    private fun arquivoDaMascara() = achar(
        "src/main/assets/silhueta_corpo_mascara.txt",
        "app/src/main/assets/silhueta_corpo_mascara.txt"
    )

    /**
     * A máscara do corpo, lida do arquivo que a ferramenta gerou.
     *
     * ## ⚠️ Por que não abrir o PNG aqui
     *
     * O teste roda no ambiente do Android, e o `android.jar` **não tem AWT** —
     * `ImageIO` simplesmente não existe. Sem um arquivo pré-calculado, os testes
     * que mais valem (o lado esquerdo/direito, a ordem anatômica) não poderiam
     * existir, e sobraria só conferir aritmética contra ela mesma.
     *
     * 🔴 O preço disso é que o arquivo pode **envelhecer** em silêncio: trocam a
     * arte, esquecem de rodar a ferramenta, e o teste segue verde conferindo um
     * corpo que não existe mais. Por isso o cabeçalho carrega o **sha256 da
     * arte** e `a mascara foi gerada DESTA arte` compara com o PNG de verdade.
     */
    private class Mascara(val largura: Int, val altura: Int, val sha: String) {
        val linhas = HashMap<Int, List<IntRange>>()
        fun dentro(x: Int, y: Int) = linhas[y]?.any { x in it } == true
    }

    private fun mascara(): Mascara? {
        val arq = arquivoDaMascara()
        assertNotNull("não encontrei a máscara — rode docs/arte/silhueta/mapa_silhueta.py", arq)
        var largura = 0
        var altura = 0
        var sha = ""
        val faixas = HashMap<Int, List<IntRange>>()
        arq!!.forEachLine { linha ->
            when {
                linha.startsWith("#") || linha.isBlank() -> Unit
                linha.startsWith("largura=") -> largura = linha.substringAfter("=").trim().toInt()
                linha.startsWith("altura=") -> altura = linha.substringAfter("=").trim().toInt()
                linha.startsWith("sha256=") -> sha = linha.substringAfter("=").trim()
                linha.startsWith("arte=") -> Unit
                else -> {
                    val y = linha.substringBefore(":").toInt()
                    faixas[y] = linha.substringAfter(":").split(",").map {
                        val (a, b) = it.split("-")
                        a.toInt()..b.toInt()
                    }
                }
            }
        }
        val m = Mascara(largura, altura, sha)
        m.linhas.putAll(faixas)
        return m
    }

    private class Caixa {
        var x0 = Int.MAX_VALUE; var x1 = Int.MIN_VALUE
        var y0 = Int.MAX_VALUE; var y1 = Int.MIN_VALUE
        var n = 0L; var somaX = 0L; var somaY = 0L
        fun soma(x: Int, y: Int) {
            if (x < x0) x0 = x; if (x > x1) x1 = x
            if (y < y0) y0 = y; if (y > y1) y1 = y
            n++; somaX += x; somaY += y
        }
        val centroX: Double get() = somaX.toDouble() / n
        val centroY: Double get() = somaY.toDouble() / n
    }

    /** Caixas das regiões considerando só o que cai **em cima do corpo**. */
    private fun caixasNoCorpo(): Map<String, Caixa> {
        val m = mascara() ?: return emptyMap()
        val r = HashMap<String, Caixa>()
        for (y in 0 until m.altura) {
            for (x in 0 until m.largura) {
                if (!m.dentro(x, y)) continue
                val id = MapaDaSilhueta.idEm(x, y)
                r.getOrPut(id) { Caixa() }.soma(x, y)
            }
        }
        return r
    }

    // ==================================================================
    // 1. A arte é a que o mapa mediu
    // ==================================================================

    @Test
    fun `🔴 a arte tem o tamanho que o mapa espera`() {
        // Se alguém trocar a imagem por outra de tamanho diferente, TODAS as
        // linhas de corte apontam para o lugar errado — e nada quebra sozinho.
        val m = mascara() ?: return
        assertEquals("largura da arte", MapaDaSilhueta.LARGURA, m.largura)
        assertEquals("altura da arte", MapaDaSilhueta.ALTURA, m.altura)
    }

    @Test
    fun `🔴 a mascara foi gerada DESTA arte`() {
        // Sem isto, trocar o desenho e esquecer de rodar a ferramenta deixaria
        // todo o resto deste arquivo verde, conferindo um corpo que não existe
        // mais. É a única amarra entre o que o app mostra e o que o teste mede.
        val m = mascara() ?: return
        val png = arquivoDaArte()
        assertNotNull("não encontrei a arte no drawable", png)
        val sha = MessageDigest.getInstance("SHA-256").digest(png!!.readBytes())
            .joinToString("") { "%02x".format(it) }
        assertEquals(
            "a arte mudou e a máscara não: rode docs/arte/silhueta/mapa_silhueta.py",
            m.sha, sha
        )
    }

    @Test
    fun `os tres recortes cabem dentro da arte`() {
        MapaDaSilhueta.Tela.entries.forEach {
            assertTrue("${it.name} sai pela esquerda", it.x0 >= 0)
            assertTrue("${it.name} sai pela direita", it.x1 <= MapaDaSilhueta.LARGURA)
            assertTrue("${it.name} sai por cima", it.y0 >= 0)
            assertTrue("${it.name} sai por baixo", it.y1 <= MapaDaSilhueta.ALTURA)
        }
    }

    // ==================================================================
    // 2. 🔴 O lado
    // ==================================================================

    @Test
    fun `🔴 o lado esquerdo DELE aparece a direita da imagem`() {
        // A figura está de frente para quem olha. Trocar isto é uma linha, não
        // quebra nada, e só aparece na ficha de quem perdeu o braço.
        val c = caixasNoCorpo()
        if (c.isEmpty()) return
        listOf("BRACO" to "braço", "MAO" to "mão", "PERNA" to "perna", "PE" to "pé")
            .forEach { (base, nome) ->
                val esq = c["${base}_E"]!!
                val dir = c["${base}_D"]!!
                assertTrue(
                    "o $nome esquerdo dele deveria estar à direita da imagem " +
                        "(esq=${esq.centroX.toInt()} dir=${dir.centroX.toInt()})",
                    esq.centroX > dir.centroX
                )
            }
        val olhoE = c["OLHO_E"]!!
        val olhoD = c["OLHO_D"]!!
        assertTrue("o olho esquerdo dele também", olhoE.centroX > olhoD.centroX)
    }

    @Test
    fun `⚠️ os pares esquerdo e direito sao do mesmo tamanho`() {
        // O corpo é simétrico; se um lado tiver muito mais área que o outro, a
        // fronteira foi posta torta.
        val c = caixasNoCorpo()
        if (c.isEmpty()) return
        listOf("BRACO", "MAO", "PERNA", "PE").forEach { base ->
            val e = c["${base}_E"]!!.n
            val d = c["${base}_D"]!!.n
            val razao = maxOf(e, d).toDouble() / minOf(e, d)
            assertTrue("$base: um lado tem ${"%.2f".format(razao)}× o outro", razao < 1.10)
        }
    }

    // ==================================================================
    // 3. 🔴 Toda região existe e está no lugar certo
    // ==================================================================

    @Test
    fun `🔴 nenhuma regiao ficou inalcancavel`() {
        val c = caixasNoCorpo()
        if (c.isEmpty()) return
        MapaDaSilhueta.REGIOES.forEach {
            assertTrue("'${it.id}' não cobre nenhum pixel do corpo", c.containsKey(it.id))
        }
        assertEquals("apareceu id que não está no catálogo", MapaDaSilhueta.REGIOES.size, c.size)
    }

    @Test
    fun `🔴 a ordem de cima para baixo bate com a anatomia`() {
        val c = caixasNoCorpo()
        if (c.isEmpty()) return
        val ordem = listOf("CRANIO", "OLHO_E", "ROSTO", "PESCOCO", "VITAIS", "TRONCO", "VIRILHA")
        ordem.zipWithNext().forEach { (a, b) ->
            assertTrue(
                "$a deveria estar acima de $b (${c[a]!!.centroY.toInt()} vs ${c[b]!!.centroY.toInt()})",
                c[a]!!.centroY < c[b]!!.centroY
            )
        }
        // E os membros, na ordem braço → mão e perna → pé.
        assertTrue(c["BRACO_E"]!!.centroY < c["MAO_E"]!!.centroY)
        assertTrue(c["PERNA_E"]!!.centroY < c["PE_E"]!!.centroY)
    }

    @Test
    fun `⚠️ a boca e o queixo ficam no ROSTO, nao no pescoco`() {
        // 🔴 O erro que a conferência visual pegou: cortar o queixo no ponto
        // mais estreito da cabeça (y 176) jogava a boca (y 168) para dentro do
        // pescoço, porque neste desenho o maxilar é quase tão fino quanto ele.
        assertEquals("a boca", "ROSTO", MapaDaSilhueta.idEm(295, 168))
        assertEquals("o queixo", "ROSTO", MapaDaSilhueta.idEm(295, 185))
        assertEquals("aí sim o pescoço", "PESCOCO", MapaDaSilhueta.idEm(295, 210))
    }

    @Test
    fun `⚠️ as sobrancelhas separam cranio de rosto`() {
        // As sobrancelhas foram medidas em y 85..93.
        assertEquals("acima da sobrancelha", "CRANIO", MapaDaSilhueta.idEm(295, 60))
        assertEquals("na altura do olho", "ROSTO", MapaDaSilhueta.idEm(295, 105))
    }

    @Test
    fun `os olhos sao alcancaveis onde a arte os desenhou`() {
        // Centro dos olhos medidos por varredura da tinta interna da cabeça.
        assertEquals("OLHO_D", MapaDaSilhueta.idEm(264, 104))
        assertEquals("OLHO_E", MapaDaSilhueta.idEm(325, 104))
    }

    // ==================================================================
    // 4. 🔴 O alvo de toque
    // ==================================================================

    /** A largura útil do diálogo no celular mais estreito que o app suporta. */
    private val LARGURA_DE_REFERENCIA_DP = 320.0

    @Test
    fun `🔴 nenhum alvo de toque fica abaixo de 48 dp`() {
        // O mesmo mínimo que o PadraoDeTelaTest cobra de qualquer botão. Aqui a
        // conta usa a geometria PURA (sem recortar no corpo), que é o que decide
        // o toque — a mão tem só 44 dp de traço desenhado, e exigir acerto no
        // traço deixaria o alvo pequeno demais.
        val caixas = HashMap<String, Caixa>()
        MapaDaSilhueta.Tela.entries.forEach { tela ->
            for (y in tela.y0 until tela.y1) {
                for (x in tela.x0 until tela.x1) {
                    val id = MapaDaSilhueta.idEm(x, y)
                    if (MapaDaSilhueta.de(id)?.tela != tela) continue
                    caixas.getOrPut(id) { Caixa() }.soma(x, y)
                }
            }
        }
        val pequenos = mutableListOf<String>()
        MapaDaSilhueta.REGIOES.forEach { r ->
            val cx = caixas[r.id] ?: return@forEach
            val escala = LARGURA_DE_REFERENCIA_DP / r.tela.largura
            val larguraDp = (cx.x1 - cx.x0 + 1) * escala
            val alturaDp = (cx.y1 - cx.y0 + 1) * escala
            if (minOf(larguraDp, alturaDp) < 48.0) {
                pequenos += "${r.id} = ${"%.1f".format(larguraDp)} × ${"%.1f".format(alturaDp)} dp"
            }
        }
        assertTrue("alvos pequenos demais: $pequenos", pequenos.isEmpty())
    }

    @Test
    fun `⚠️ a silhueta indice nao tem faixa morta`() {
        // Os recortes têm um vão de 11 px entre a cabeça (termina em 258) e o
        // tronco (começa em 269). Na tela índice isso viraria uma faixa em que o
        // toque não faz nada — e o jogador não teria como entender por quê.
        (0 until MapaDaSilhueta.ALTURA).forEach { y ->
            assertNotNull("y=$y não leva a tela nenhuma", MapaDaSilhueta.telaEm(y))
        }
        assertEquals(MapaDaSilhueta.Tela.CABECA, MapaDaSilhueta.telaEm(262))
        assertEquals(MapaDaSilhueta.Tela.TRONCO, MapaDaSilhueta.telaEm(500))
        assertEquals(MapaDaSilhueta.Tela.PERNAS, MapaDaSilhueta.telaEm(1200))
    }

    // ==================================================================
    // 5. O catálogo bate com o livro
    // ==================================================================

    @Test
    fun `🔴 todo local do livro que a silhueta deve ter esta la`() {
        val esperados = setOf(
            LocalAtaque.CRANIO, LocalAtaque.ROSTO, LocalAtaque.OLHO, LocalAtaque.PESCOCO,
            LocalAtaque.TORSO, LocalAtaque.VITAIS, LocalAtaque.INGLE,
            LocalAtaque.BRACO, LocalAtaque.MAO, LocalAtaque.PERNA, LocalAtaque.PE
        )
        assertEquals(esperados, MapaDaSilhueta.REGIOES.map { it.local }.toSet())
    }

    @Test
    fun `⚠️ os locais de ESCUDO ficam de fora — sao penalidade de acertar`() {
        // "Braço com escudo" (−4) e "mão com escudo" (−8) atrapalham quem ataca.
        // Depois que o golpe entrou, o dano segue a regra de braço comum. Se
        // aparecerem aqui, o jogador escolhe algo que não muda nada no ferimento.
        val rotulos = MapaDaSilhueta.REGIOES.map { it.rotulo.lowercase() }
        assertTrue(rotulos.none { it.contains("escudo") })
        assertTrue(rotulos.none { it.contains("arma") })
        assertEquals(3, MapaDaSilhueta.FORA_DA_SILHUETA.size)
    }

    @Test
    fun `os lados so existem onde o corpo tem dois`() {
        MapaDaSilhueta.REGIOES.forEach { r ->
            val precisaDeLado = r.local in setOf(
                LocalAtaque.BRACO, LocalAtaque.MAO, LocalAtaque.PERNA,
                LocalAtaque.PE, LocalAtaque.OLHO
            )
            assertEquals("${r.id}: lado", precisaDeLado, r.lado != null)
        }
    }

    @Test
    fun `ids unicos, rotulos preenchidos`() {
        val ids = MapaDaSilhueta.REGIOES.map { it.id }
        assertEquals(ids.distinct().size, ids.size)
        MapaDaSilhueta.REGIOES.forEach {
            assertTrue("${it.id} sem rótulo", it.rotulo.isNotBlank())
            assertEquals("${it.id}: de() não devolve ele mesmo", it, MapaDaSilhueta.de(it.id))
        }
    }
}
