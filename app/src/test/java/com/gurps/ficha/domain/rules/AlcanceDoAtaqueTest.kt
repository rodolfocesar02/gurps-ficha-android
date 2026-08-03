package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.model.PERICIAS_COMBATE
import com.gurps.ficha.model.PERICIAS_COMBATE_CORPO_A_CORPO
import com.gurps.ficha.model.PERICIAS_COMBATE_DISTANCIA
import com.gurps.ficha.model.TipoEquipamento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Quando a linha de distância aparece — e até onde a arma chega.
 *
 * A pergunta parece boba, mas ela decide se a tela mostra ou esconde um controle
 * inteiro. Errar para menos esconde a regra de quem precisa; errar para mais
 * enche a tela de ruído numa espada.
 */
class AlcanceDoAtaqueTest {

    private fun arma(
        nome: String = "Arma",
        tipoCombate: String? = null,
        meioDano: Int? = null,
        maximo: Int? = null,
        multStRaw: String? = null
    ) = Equipamento(
        nome = nome,
        tipo = TipoEquipamento.ARMA,
        armaTipoCombate = tipoCombate,
        armaMeioDanoMetros = meioDano,
        armaMaximoMetros = maximo,
        armaAlcanceMultStRaw = multStRaw
    )

    // --- as duas listas ---

    @Test
    fun `PERICIAS_COMBATE continua sendo a uniao das duas`() {
        // A separação não pode ter perdido nem inventado perícia: Apara e
        // Bloqueio dependem desta lista desde sempre.
        assertEquals(
            PERICIAS_COMBATE_CORPO_A_CORPO.size + PERICIAS_COMBATE_DISTANCIA.size,
            PERICIAS_COMBATE.size
        )
        assertTrue(PERICIAS_COMBATE.containsAll(PERICIAS_COMBATE_CORPO_A_CORPO))
        assertTrue(PERICIAS_COMBATE.containsAll(PERICIAS_COMBATE_DISTANCIA))
    }

    @Test
    fun `nenhuma pericia esta nos dois grupos`() {
        val nosDois = PERICIAS_COMBATE_CORPO_A_CORPO intersect PERICIAS_COMBATE_DISTANCIA
        assertTrue("estas ficaram nos dois: $nosDois", nosDois.isEmpty())
    }

    // --- pela perícia ---

    @Test
    fun `pericia de longe abre a linha, pericia de perto nao`() {
        listOf("arcos", "besta", "arremesso", "armas_de_fogo_nt", "shuriken", "funda")
            .forEach { assertTrue(it, AlcanceDoAtaque.periciaEhADistancia(it)) }

        listOf("espada_curta", "faca", "judo", "briga", "escudo", "lanca")
            .forEach { assertFalse(it, AlcanceDoAtaque.periciaEhADistancia(it)) }
    }

    @Test
    fun `o id da tela vem com a especializacao colada`() {
        // A aba Rolagem monta o id como "pericia_<id>_<especializacao>", e quem
        // chama já tirou o prefixo. Sobra "armas_de_fogo_nt_pistola".
        assertTrue(AlcanceDoAtaque.periciaEhADistancia("armas_de_fogo_nt_pistola"))
        assertTrue(AlcanceDoAtaque.periciaEhADistancia("arcos_"))
    }

    @Test
    fun `pericia racial tambem conta`() {
        // Uma raça pode conceder Arco; o id vem com o prefixo "racial_".
        assertTrue(AlcanceDoAtaque.periciaEhADistancia("racial_arco"))
    }

    @Test
    fun `sem pericia e sem arma, nao aparece`() {
        assertFalse(AlcanceDoAtaque.ehADistancia(null, null))
        assertFalse(AlcanceDoAtaque.periciaEhADistancia(""))
    }

    // --- pela arma ---

    @Test
    fun `🔴 a arma NAO cala mais a pericia (Lote ARMA-5)`() {
        // Este teste dizia o contrário até 03/08, com o comentário "quem está na
        // mão é a espada". A afirmação era razoável e estava errada: o print do
        // usuário mostrou o ataque `Armas de Fogo/NT (pistola)` abrindo o
        // diálogo de CORPO A CORPO, sem distância, sem 1/2D e sem Apontar,
        // porque a fonte de dano tinha ficado numa arma branca.
        //
        // Agora qualquer um dos dois lados que diga "longe" basta, e a
        // divergência vira aviso na tela em vez de decisão silenciosa.
        assertTrue(
            AlcanceDoAtaque.ehADistancia(arma(tipoCombate = "corpo_a_corpo"), "arcos")
        )
        assertNotNull(
            AlcanceDoAtaque.conflito(arma(tipoCombate = "corpo_a_corpo"), "arcos")
        )
        // O caso que a regra antiga existia para resolver continua valendo:
        // perícia Faca com uma faca de ARREMESSO na mão.
        assertTrue(
            AlcanceDoAtaque.ehADistancia(arma(tipoCombate = "distancia"), "faca")
        )
    }

    @Test
    fun `arma e pericia coerentes nao geram aviso nenhum`() {
        assertNull(AlcanceDoAtaque.conflito(arma(tipoCombate = "corpo_a_corpo"), "faca"))
        assertNull(AlcanceDoAtaque.conflito(arma(tipoCombate = "armas_de_fogo"), "armas_de_fogo_nt"))
    }

    @Test
    fun `arma de fogo tambem e a distancia`() {
        assertTrue(AlcanceDoAtaque.ehADistancia(arma(tipoCombate = "armas_de_fogo"), null))
    }

    @Test
    fun `ficha antiga sem o tipo cai no alcance maximo`() {
        // Fichas anteriores ao Lote 371 têm os campos nulos. Se a arma tem Máx,
        // ela atira -- é o único jeito de saber.
        assertTrue(AlcanceDoAtaque.ehADistancia(arma(maximo = 100), null))
        assertFalse(AlcanceDoAtaque.ehADistancia(arma(), null))
    }

    @Test
    fun `o ataque com Dano ST cai na pericia`() {
        // "Dano ST" não é arma nenhuma -- foi o caso do print do usuário:
        // ataque "Arcos" com a fonte de dano ainda em ST.
        assertTrue(AlcanceDoAtaque.ehADistancia(null, "arcos"))
    }

    // --- QUAL arma está atirando (achado de 29/07) ---

    private fun arco(nome: String = "Arco Longo") =
        arma(nome = nome, tipoCombate = "distancia", multStRaw = "×15/×20")
            .also { it.armaGrupo = "ARCO" }

    private fun espada() = arma(nome = "Espada", tipoCombate = "corpo_a_corpo")
        .also { it.armaGrupo = "ESPADA CURTA" }

    @Test
    fun `⚠️ acha o arco mesmo com a fonte de dano em Dano ST`() {
        // O defeito que o usuário achou no T-D8: os avisos de Máx e 1/2D nunca
        // apareciam. A tela só olhava a fonte de dano, e ela fica em "Dano ST"
        // na maioria das fichas -- então a arma vinha nula e não havia o que
        // avisar, mesmo com o arco na ficha e a perícia Arcos selecionada.
        val arco = arco()
        val achada = AlcanceDoAtaque.armaDoAtaque(
            armas = listOf(espada(), arco),
            armaSelecionada = null,
            periciaId = "arcos_"
        )
        assertEquals(arco, achada)
    }

    @Test
    fun `a arma escolhida na fonte de dano vence a busca`() {
        val outroArco = arco("Arco Curto")
        val achada = AlcanceDoAtaque.armaDoAtaque(
            armas = listOf(arco(), outroArco),
            armaSelecionada = outroArco,
            periciaId = "arcos_"
        )
        assertEquals(outroArco, achada)
    }

    @Test
    fun `com uma arma de longe so, nao precisa casar o grupo`() {
        // Quem tem um arco só não deveria ter de explicar qual arco.
        val a = arma(nome = "Arco caseiro", tipoCombate = "distancia", maximo = 90)
        assertEquals(
            a,
            AlcanceDoAtaque.armaDoAtaque(listOf(espada(), a), null, "sem_grupo_nenhum")
        )
    }

    @Test
    fun `com duas armas de longe e nenhuma casando, nao chuta`() {
        // Avisar "fora de alcance" com a arma errada é pior que não avisar.
        val besta = arma(nome = "Besta", tipoCombate = "distancia", maximo = 100)
            .also { it.armaGrupo = "BESTA" }
        val funda = arma(nome = "Funda", tipoCombate = "distancia", maximo = 30)
            .also { it.armaGrupo = "FUNDA" }
        assertNull(AlcanceDoAtaque.armaDoAtaque(listOf(besta, funda), null, "zarabatana"))
    }

    @Test
    fun `arma de corpo a corpo nunca e escolhida`() {
        assertNull(AlcanceDoAtaque.armaDoAtaque(listOf(espada()), null, "espada_curta"))
        assertNull(AlcanceDoAtaque.armaDoAtaque(emptyList(), null, "arcos"))
    }

    @Test
    fun `casa grupo com acento e sublinhado`() {
        val pistola = arma(nome = "Pistola", tipoCombate = "armas_de_fogo", maximo = 150)
            .also { it.armaGrupo = "Pistola" }
        assertEquals(
            pistola,
            AlcanceDoAtaque.armaDoAtaque(
                listOf(pistola, arco()), null, "armas_de_fogo_nt_pistola"
            )
        )
    }

    // --- o alcance ---

    @Test
    fun `arma com alcance fixo devolve o que esta na ficha`() {
        val a = AlcanceDoAtaque.alcanceDe(arma(meioDano = 50, maximo = 200), st = 10)
        assertEquals(50, a.meioDano)
        assertEquals(200, a.maximo)
    }

    @Test
    fun `⚠️ o arco nao tem alcance fixo -- e multiplo da ST`() {
        // O mesmo arco vai muito mais longe numa ST 12 do que numa ST 9. Tratar
        // "×10/×15" como número fixo daria o mesmo alcance para todo mundo.
        val forte = AlcanceDoAtaque.alcanceDe(arma(multStRaw = "×10/×15"), st = 12)
        assertEquals(120, forte.meioDano)
        assertEquals(180, forte.maximo)

        val fraco = AlcanceDoAtaque.alcanceDe(arma(multStRaw = "×10/×15"), st = 9)
        assertEquals(90, fraco.meioDano)
        assertEquals(135, fraco.maximo)
    }

    @Test
    fun `aceita x comum no lugar do sinal de multiplicar`() {
        val a = AlcanceDoAtaque.alcanceDe(arma(multStRaw = "x10/x15"), st = 10)
        assertEquals(100, a.meioDano)
        assertEquals(150, a.maximo)
    }

    @Test
    fun `formato estranho devolve nulo em vez de chutar`() {
        // Alcance inventado é pior que nenhum: o jogador confiaria no aviso.
        val a = AlcanceDoAtaque.alcanceDe(arma(multStRaw = "depende"), st = 10)
        assertNull(a.meioDano)
        assertNull(a.maximo)

        val semArma = AlcanceDoAtaque.alcanceDe(null, st = 10)
        assertNull(semArma.maximo)
    }

    @Test
    fun `o numero da ficha vence o multiplicador`() {
        // Quando o catálogo trouxe os dois, o valor absoluto é o mais específico.
        val a = AlcanceDoAtaque.alcanceDe(
            arma(meioDano = 7, maximo = 20, multStRaw = "×10/×15"), st = 12
        )
        assertEquals(7, a.meioDano)
        assertEquals(20, a.maximo)
    }
}
