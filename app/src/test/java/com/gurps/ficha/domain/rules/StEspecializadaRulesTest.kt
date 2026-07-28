package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.TipoEquipamento
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ST de Golpe (MB p.88) e ST de Levantamento (MB p.65).
 *
 * São complementares, e o teste que mais importa é o de **separação**: a de
 * Golpe não pode mexer na carga, a de Levantamento não pode mexer no dano, e
 * **nenhuma das duas** pode mexer nos PV.
 *
 * É a mesma família de armadilha da ST Braçal — força especializada que, se
 * vazar para o lugar errado, dá vantagem que o livro não concede.
 */
class StEspecializadaRulesTest {

    private fun vant(id: String, nome: String, nivel: Int) =
        VantagemSelecionada(definicaoId = id, nome = nome, nivel = nivel)

    private fun heroi(golpe: Int = 0, levantamento: Int = 0) = Personagem(
        nome = "Teste",
        forca = 10,
        vantagens = buildList {
            if (golpe > 0) add(vant(StEspecializadaRules.ID_GOLPE, "ST de Golpe", golpe))
            if (levantamento > 0) {
                add(vant(StEspecializadaRules.ID_LEVANTAMENTO, "ST de Levantamento", levantamento))
            }
        }
    )

    private val semNada = heroi()

    // --- ST de Golpe: dano sim ---

    @Test
    fun `ST de Golpe aumenta o dano`() {
        val p = heroi(golpe = 4)
        assertEquals(14, StEspecializadaRules.stParaDano(p))
        assertEquals(CharacterRules.calcularDanoGdP(14), p.danoGdP)
        assertEquals(CharacterRules.calcularDanoGeB(14), p.danoGeB)
        assertTrue("o dano tinha que mudar", p.danoGeB != semNada.danoGeB)
    }

    @Test
    fun `ST de Golpe NAO mexe na Base de Carga`() {
        assertEquals(semNada.baseCarga, heroi(golpe = 4).baseCarga, 0.001f)
    }

    @Test
    fun `ST de Golpe NAO mexe nos PV nem no ST`() {
        val p = heroi(golpe = 4)
        assertEquals(10, p.st)
        assertEquals(10, p.pontosVida)
    }

    @Test
    fun `ST de Golpe entra na arma empunhada`() {
        // MB p.88: vale para "armas que utilizam a ST do personagem".
        val p = heroi(golpe = 4)
        val faca = Equipamento(
            nome = "Faca", tipo = TipoEquipamento.ARMA, armaDanoRaw = "GeB-1 corte"
        )
        // ST 10 -> GeB 1d ; ST 14 -> GeB 2d. A faca tira 1.
        assertEquals("1d-1 corte", faca.danoCalculadoComSt(semNada))
        assertEquals("2d-1 corte", faca.danoCalculadoComSt(p))
    }

    @Test
    fun `ST de Golpe e ST Bracal SOMAM na arma`() {
        // Sao vantagens diferentes; quem paga as duas recebe as duas.
        val p = heroi(golpe = 2)
        val faca = Equipamento(
            nome = "Faca", tipo = TipoEquipamento.ARMA, armaDanoRaw = "GeB corte"
        )
        // ST 10 + golpe 2 + bracal 2 = 14 -> GeB 2d.
        assertEquals(CharacterRules.calcularDanoGeB(14) + " corte",
            faca.danoCalculadoComSt(p, null, stExtra = 2))
    }

    // --- ST de Levantamento: carga sim ---

    @Test
    fun `ST de Levantamento aumenta a Base de Carga`() {
        val p = heroi(levantamento = 5)
        assertEquals(15, StEspecializadaRules.stParaCarga(p))
        // Base de Carga = ST^2 / 10 -> 15^2/10 = 22,5
        assertEquals(22.5f, p.baseCarga, 0.001f)
    }

    @Test
    fun `ST de Levantamento NAO mexe no dano`() {
        val p = heroi(levantamento = 5)
        assertEquals(semNada.danoGdP, p.danoGdP)
        assertEquals(semNada.danoGeB, p.danoGeB)
    }

    @Test
    fun `ST de Levantamento NAO mexe nos PV nem no ST`() {
        val p = heroi(levantamento = 5)
        assertEquals(10, p.st)
        assertEquals(10, p.pontosVida)
    }

    // --- as duas juntas ---

    @Test
    fun `cada uma vai para o seu lado, sem se misturar`() {
        val p = heroi(golpe = 3, levantamento = 5)
        assertEquals("dano usa 10+3", 13, StEspecializadaRules.stParaDano(p))
        assertEquals("carga usa 10+5", 15, StEspecializadaRules.stParaCarga(p))
        assertEquals("ST e PV intactos", 10, p.st)
        assertEquals(10, p.pontosVida)
    }

    @Test
    fun `ficha sem nenhuma das duas nao muda nada`() {
        assertEquals(0, StEspecializadaRules.bonusDeGolpe(semNada))
        assertEquals(0, StEspecializadaRules.bonusDeLevantamento(semNada))
        assertEquals(10.0f, semNada.baseCarga, 0.001f)
        assertTrue(!StEspecializadaRules.temAlguma(semNada))
    }

    // --- a linha da tela ---

    @Test
    fun `sem as vantagens nao ha resumo para mostrar`() {
        assertEquals(null, StEspecializadaRules.resumo(semNada))
    }

    @Test
    fun `o resumo automatico e SO da ST de Golpe`() {
        // A de Levantamento saiu do resumo porque ganhou SELETOR: a Base de Carga
        // e automatica, mas os testes de ST (erguer, forcar porta, agarrar)
        // dependem da intencao, e so o jogador sabe.
        val texto = StEspecializadaRules.resumo(heroi(golpe = 3, levantamento = 5))!!
        assertTrue(texto, texto.contains("+3") && texto.contains("13"))
        assertTrue("Levantamento nao entra no resumo", !texto.contains("Levantamento"))
    }

    @Test
    fun `so com ST de Levantamento nao ha resumo automatico`() {
        assertEquals(null, StEspecializadaRules.resumo(heroi(levantamento = 5)))
    }

    // --- o seletor de Levantamento (os testes de ST) ---

    @Test
    fun `o seletor so aparece com a vantagem na ficha`() {
        assertTrue(!StEspecializadaRules.temLevantamento(semNada))
        assertTrue(StEspecializadaRules.temLevantamento(heroi(levantamento = 1)))
    }

    @Test
    fun `o rotulo do seletor diz o numero final e os usos`() {
        val texto = StEspecializadaRules.rotuloLevantamento(heroi(levantamento = 5))
        assertTrue(texto, texto.contains("+5") && texto.contains("15"))
        assertTrue("precisa dizer para que serve", texto.contains("erguer"))
    }

    @Test
    fun `a descricao acessivel avisa que a carga ja e automatica`() {
        // Senao o jogador acha que precisa marcar a caixinha para a Base de
        // Carga valer -- e ela vale sempre.
        val d = StEspecializadaRules.rotuloAcessivelLevantamento(heroi(levantamento = 5))
        assertTrue(d, d.contains("Base de Carga"))
        assertTrue(d, d.contains("sempre"))
        assertTrue("nao pode repetir o estado", !d.lowercase().contains("marcad"))
    }
}
