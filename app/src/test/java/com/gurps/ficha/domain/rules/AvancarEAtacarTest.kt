package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.loaders.ArmasCatalogLoader
import com.gurps.ficha.model.ArmaCatalogoItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **Lote ARMA-7** — Avançar e Atacar, e a Magnitude que finalmente é usada.
 *
 * ## A regra (MB p.366)
 *
 * > Se estiver realizando um Ataque **à distância**, a penalidade é de **-2 ou
 * > igual à Magnitude da arma, o que for pior**. Se estiver realizando um Ataque
 * > **corpo a corpo**, a penalidade é de **-4** e o nível de habilidade ajustado
 * > **não pode ser maior que 9**.
 *
 * ## 🔴 E o catálogo estava torto
 *
 * Três armas de fogo tinham **Magnitude +10** com a CdT vazia: a linha da
 * planilha escorregou **uma coluna inteira** a partir da CdT. O que estava em
 * `magnitude` era a **ST**, e o Rifle de Atirador .338 acabou com **ST 41** —
 * como a lista de armas filtra por ST, ele era **impossível de adicionar** a
 * qualquer ficha normal.
 *
 * O JSON foi corrigido com a linha do livro (p.280). Os testes abaixo travam os
 * valores certos **e** a guarda que impede um `+10` de virar bônus.
 */
class AvancarEAtacarTest {

    private fun asset(nome: String): String {
        val direto = File("src/main/assets/$nome")
        val f = if (direto.exists()) direto else File("app/src/main/assets/$nome")
        assertTrue("nao encontrei ${f.absolutePath}", f.exists())
        return f.readText(Charsets.UTF_8)
    }

    private val fogo by lazy {
        ArmasCatalogLoader.distancia(asset(ArmasCatalogLoader.ARQUIVO_FOGO), ArmasCatalogLoader.TIPO_FOGO)
    }
    private val distancia by lazy {
        ArmasCatalogLoader.distancia(asset(ArmasCatalogLoader.ARQUIVO_DISTANCIA), ArmasCatalogLoader.TIPO_DISTANCIA)
    }

    private fun arma(lista: List<ArmaCatalogoItem>, nome: String): ArmaCatalogoItem {
        val a = lista.firstOrNull { it.nome == nome }
        assertNotNull("nao achei '$nome'", a)
        return a!!
    }

    // ==================================================================
    // 1. A regra à distância
    // ==================================================================

    @Test
    fun `sem Magnitude cadastrada vale o menos 2 basico`() {
        assertEquals(-2, AvancarEAtacarRules.penalidadeADistancia(null))
    }

    @Test
    fun `Magnitude fraca perde para o menos 2 — o livro manda o PIOR`() {
        // Magnitude −1 é melhor que −2, então vale o −2.
        assertEquals(-2, AvancarEAtacarRules.penalidadeADistancia(-1))
        assertEquals(-2, AvancarEAtacarRules.penalidadeADistancia(0))
    }

    @Test
    fun `Magnitude pior que o basico e quem manda`() {
        assertEquals(-6, AvancarEAtacarRules.penalidadeADistancia(-6))
        assertEquals(-8, AvancarEAtacarRules.penalidadeADistancia(-8))
        assertTrue(AvancarEAtacarRules.magnitudeMandou(-6))
        assertTrue(!AvancarEAtacarRules.magnitudeMandou(-1))
    }

    @Test
    fun `🔴 Magnitude positiva NUNCA vira bonus`() {
        // A guarda que impede o dado torto de dar +10 para atirar correndo.
        assertEquals(-2, AvancarEAtacarRules.penalidadeADistancia(10))
        assertTrue(AvancarEAtacarRules.magnitudeSuspeita(10))
        assertTrue(!AvancarEAtacarRules.magnitudeSuspeita(-3))
        assertTrue(!AvancarEAtacarRules.magnitudeSuspeita(null))
        // E o rótulo denuncia em vez de calar.
        val r = AvancarEAtacarRules.rotulo(ehADistancia = true, magnitude = 10, nhBase = 14)
        assertTrue(r, r.contains("o livro não admite"))
    }

    @Test
    fun `a penalidade a distancia nunca e melhor que menos 2`() {
        // Varredura: qualquer valor de Magnitude, inclusive lixo.
        listOf(null, -12, -8, -6, -3, -1, 0, 1, 5, 10, 99).forEach { m ->
            assertTrue(
                "magnitude $m deu ${AvancarEAtacarRules.penalidadeADistancia(m)}",
                AvancarEAtacarRules.penalidadeADistancia(m) <= -2
            )
        }
    }

    // ==================================================================
    // 2. ⚠️ O corpo a corpo: são DUAS coisas
    // ==================================================================

    @Test
    fun `⚠️ corpo a corpo tem menos 4 E teto de 9`() {
        // NH 12 − 4 = 8, abaixo do teto: fica 8.
        assertEquals(8, AvancarEAtacarRules.nhCorpoACorpo(12))
        // NH 20 − 4 = 16, mas o teto corta para 9. É a parte que mais escapa:
        // parece penalidade e é limite.
        assertEquals(9, AvancarEAtacarRules.nhCorpoACorpo(20))
        assertEquals(-11, AvancarEAtacarRules.penalidadeCorpoACorpo(20))
    }

    @Test
    fun `o teto do corpo a corpo e mencionado so quando corta`() {
        val cortado = AvancarEAtacarRules.rotulo(ehADistancia = false, magnitude = null, nhBase = 20)
        assertTrue(cortado, cortado.contains("teto de 9"))
        val inteiro = AvancarEAtacarRules.rotulo(ehADistancia = false, magnitude = null, nhBase = 12)
        assertTrue(inteiro, !inteiro.contains("teto"))
    }

    @Test
    fun `atacar em movimento nunca MELHORA o ataque`() {
        (3..20).forEach { nh ->
            assertTrue("nh $nh", AvancarEAtacarRules.penalidadeCorpoACorpo(nh) <= 0)
            assertTrue("nh $nh", AvancarEAtacarRules.nhCorpoACorpo(nh) <= nh)
        }
    }

    // ==================================================================
    // 3. O rótulo diz de onde veio o número
    // ==================================================================

    @Test
    fun `o rotulo separa o padrao da Magnitude da arma`() {
        val daArma = AvancarEAtacarRules.rotulo(true, -6, 14)
        assertTrue(daArma, daArma.contains("-6"))
        assertTrue(daArma, daArma.contains("Magnitude da arma"))

        val doPadrao = AvancarEAtacarRules.rotulo(true, -1, 14)
        assertTrue(doPadrao, doPadrao.contains("-2"))
        assertTrue(doPadrao, doPadrao.contains("padrão"))

        val semCadastro = AvancarEAtacarRules.rotulo(true, null, 14)
        assertTrue(semCadastro, semCadastro.contains("não tem Magnitude cadastrada"))
    }

    // ==================================================================
    // 4. 🔴 O catálogo depois do conserto
    // ==================================================================

    @Test
    fun `🔴 nenhuma arma do catalogo tem Magnitude positiva`() {
        (fogo + distancia).forEach { a ->
            val m = a.magnitude ?: return@forEach
            assertTrue("${a.nome} com Magnitude $m", m <= 0)
        }
    }

    @Test
    fun `🔴 as tres linhas deslocadas voltaram aos valores do livro`() {
        // MB p.280, conferido linha a linha contra o chunks.jsonl.
        val rifle = arma(fogo, "Rifle de Atirador, .338")
        assertEquals("CdT", 1, rifle.cadenciaTiro)
        assertEquals("Tiros", "4+1(3)", rifle.tirosRaw)
        assertEquals("ST", 11, rifle.stMinimo)
        assertEquals("Magnitude", -6, rifle.magnitude)
        assertEquals("Recuo", 4, rifle.recuo)
        assertEquals("CL", 3, rifle.cl)

        val aci = arma(fogo, "ACI, 6,8 mm")
        assertEquals(15, aci.cadenciaTiro)
        assertEquals("25+1(3)", aci.tirosRaw)
        assertEquals(10, aci.stMinimo)
        assertEquals(-5, aci.magnitude)
        assertEquals(2, aci.recuo)
        assertEquals(1, aci.cl)

        val gauss = arma(fogo, "Rifle de Gauss, 4 mm")
        assertEquals(12, gauss.cadenciaTiro)
        assertEquals("60(3)", gauss.tirosRaw)
        assertEquals(10, gauss.stMinimo)
        assertEquals(-4, gauss.magnitude)
        assertEquals(2, gauss.recuo)
        assertEquals(2, gauss.cl)
    }

    @Test
    fun `🔴 o Rifle de Atirador voltou a caber numa ficha`() {
        // Com ST 41 ele NUNCA aparecia: a lista de armas filtra por ST, e nenhum
        // personagem normal chega perto. Era invisível, não "difícil".
        val rifle = arma(fogo, "Rifle de Atirador, .338")
        assertTrue("ST ${rifle.stMinimo} ainda inviável", (rifle.stMinimo ?: 99) <= 20)
        assertTrue("perdeu o simbolo de duas maos", rifle.duasMaos)
    }

    @Test
    fun `⚠️ nenhuma arma de fogo tem ST absurda, fora a montada`() {
        // ST alta só faz sentido em arma montada (o "M" do livro manda ignorar a
        // ST). Qualquer outra acima de 20 é sinal de linha torta.
        val absurdas = fogo.filter { (it.stMinimo ?: 0) > 20 && !(it.stRaw?.contains("M") == true) }
        assertTrue("armas com ST irreal: ${absurdas.map { it.nome to it.stMinimo }}", absurdas.isEmpty())
    }

    @Test
    fun `a Precisao das tres corrigidas nao se perdeu no conserto`() {
        // O conserto mexeu de CdT para a direita. Prec e mira acoplada ficam à
        // esquerda e não podiam mudar.
        assertEquals(3, arma(fogo, "Rifle de Atirador, .338").precisaoAcessorio)
        assertEquals(2, arma(fogo, "ACI, 6,8 mm").precisaoAcessorio)
        assertEquals(2, arma(fogo, "Rifle de Gauss, 4 mm").precisaoAcessorio)
    }

    @Test
    fun `⚠️ a linha sem fonte no livro fica MARCADA, nao inventada`() {
        // A Carabina de Assalto 5,56 mm também parece deslocada, mas os campos
        // dela (alcance 45/145, custo $1.200) não batem com nenhuma linha do
        // livro — corrigi-la seria inventar. Ela leva reviewFlag e segue como
        // está, com a guarda da Magnitude protegendo a conta.
        val carabina = arma(fogo, "Carabina de Assalto, 5,56 mm")
        assertTrue(
            "a guarda tem de segurar mesmo com o dado suspeito",
            AvancarEAtacarRules.penalidadeADistancia(carabina.magnitude) <= -2
        )
    }

    // == O rotulo que mentia para quem tem Atirador (ARMA-11) ============

    @Test
    fun `com a vantagem, o rotulo diz ZERO e nao a penalidade`() {
        // 🔴 A conta ja era zero; o ROTULO e que continuava anunciando "-2". O
        // jogador marcava a caixinha, via "-2" escrito e o total nao mudar, e
        // ficava sem saber em qual dos dois acreditar.
        val comVantagem = AvancarEAtacarRules.rotulo(
            ehADistancia = true, magnitude = -2, nhBase = 18, ignoraPelaVantagem = true
        )
        assertTrue(comVantagem, comVantagem.contains("0"))
        assertTrue(comVantagem, !comVantagem.contains("-2"))
    }

    @Test
    fun `o rotulo diz o que se paga em troca`() {
        // ⚠️ O livro nao da as duas coisas: "tudo isso e em vez de receber o
        // bonus da Prec" (MB p.43).
        val comVantagem = AvancarEAtacarRules.rotulo(true, -2, 18, ignoraPelaVantagem = true)
        assertTrue(comVantagem, comVantagem.contains("Precisão"))
        assertTrue(comVantagem, comVantagem.contains("p.43"))
    }

    @Test
    fun `sem a vantagem, o rotulo antigo continua igual`() {
        val semVantagem = AvancarEAtacarRules.rotulo(true, -2, 18)
        val explicito = AvancarEAtacarRules.rotulo(true, -2, 18, ignoraPelaVantagem = false)
        assertEquals(semVantagem, explicito)
        assertTrue(semVantagem, semVantagem.contains("-2"))
    }

    @Test
    fun `o rotulo falado tambem conta a troca`() {
        val falado = AvancarEAtacarRules.rotuloAcessivel(true, -2, 18, ignoraPelaVantagem = true)
        assertTrue(falado, falado.contains("apaga a penalidade"))
    }
}
