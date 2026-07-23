package com.gurps.ficha.domain.combat.subsistemas

import com.gurps.ficha.domain.combat.Combatente
import com.gurps.ficha.domain.combat.NpcStats
import com.gurps.ficha.domain.combat.TipoCriatura
import com.gurps.ficha.domain.magic.MagiaMecanica
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Lote MOTOR-2: o funil de dano mágico agora é testável SOZINHO — sem um `CombatSession`. Estas
 * regras (imunidade, tipo de criatura) antes só eram cobertas rodando um combate completo.
 */
class DanoMagicoResolverTest {

    private fun alvo(pv: Int = 20, imunidades: List<String> = emptyList(),
                    tipo: TipoCriatura = TipoCriatura.VIVO) = Combatente(
        id = "g", nome = "Alvo", dx = 11, velocidadeBasica = 5.0, deslocamento = 5, pvMax = pv, pvAtual = pv,
        stats = NpcStats(st = 11, dx = 11, ht = 11, pvMax = pv, imunidades = imunidades, tipoCriatura = tipo)
    )

    /** Resolver com RD zero e um "impor condição" que só marca que foi chamado. */
    private fun resolver(condicoesImpostas: MutableList<String> = mutableListOf(), seed: Long = 7) =
        DanoMagicoResolver(
            random = Random(seed),
            rdContraMagia = { _, _ -> 0 },
            imporCondicao = { _, cond, _, _ -> if (cond != null) condicoesImpostas.add(cond) },
        )

    @Test
    fun `dano de fogo comum fere o alvo`() {
        val a = alvo(pv = 20)
        val d = resolver().aplicar(a, energia = 2,
            mecanica = MagiaMecanica(efeito = "dano", danoPorEnergia = "3d", elementoDano = "fogo"),
            sb = StringBuilder())
        assertTrue("tem que ferir", d > 0)
        assertEquals(20 - d, a.pvAtual)
    }

    @Test
    fun `IMUNE ao fogo NAO perde PV e o dano retornado e zero`() {
        val a = alvo(pv = 20, imunidades = listOf("fogo"))
        val sb = StringBuilder()
        val d = resolver().aplicar(a, 2,
            MagiaMecanica(efeito = "dano", danoPorEnergia = "3d", elementoDano = "fogo"), sb)
        assertEquals(0, d)
        assertEquals(20, a.pvAtual)
        assertTrue(sb.toString().contains("IMUNE"))
    }

    @Test
    fun `imunidade NAO vaza entre elementos — imune a fogo leva raio`() {
        val a = alvo(pv = 20, imunidades = listOf("fogo"))
        val d = resolver().aplicar(a, 2,
            MagiaMecanica(efeito = "dano", danoPorEnergia = "3d", elementoDano = "eletricidade"),
            StringBuilder())
        assertTrue("dano de outro elemento passa", d > 0)
    }

    @Test
    fun `mortos-vivos NAO sao afetados por magia que os exclui`() {
        val a = alvo(pv = 20, tipo = TipoCriatura.MORTO_VIVO)
        val sb = StringBuilder()
        val d = resolver().aplicar(a, 2,
            MagiaMecanica(efeito = "dano", danoPorEnergia = "3d", naoAfeta = listOf("morto_vivo")), sb)
        assertEquals(0, d)
        assertTrue(sb.toString().contains("não o afeta"))
    }

    @Test
    fun `bruto FORCADO nao rola dado — a explosao usa o mesmo valor`() {
        // P5: a explosão rola uma vez e passa o bruto para cada vítima.
        val a = alvo(pv = 50)
        val d = resolver().aplicar(a, 1,
            MagiaMecanica(efeito = "dano", danoPorEnergia = "3d"), StringBuilder(), brutoForcado = 10)
        assertEquals("RD 0, bruto forçado 10 → 10 de dano", 10, d)
    }

    @Test
    fun `condicao embutida e imposta quando o dano passa`() {
        val impostas = mutableListOf<String>()
        val a = alvo(pv = 30)
        // NH de resistência baixo garante que o teste falhe e a condição entre; seed fixa.
        resolver(impostas).aplicar(a, 3,
            MagiaMecanica(efeito = "dano", danoPorEnergia = "3d", condicao = "atordoado",
                condicaoResistencia = "HT-3"), StringBuilder())
        // Pode ou não impor dependendo da rolagem; o que importa é que o caminho não quebra e, com
        // dano > 0, a condição é AVALIADA. Rodamos vários seeds para garantir que ao menos uma impõe.
        var impôsAlguma = impostas.isNotEmpty()
        for (seed in 0L until 20L) {
            val imp = mutableListOf<String>()
            resolver(imp, seed).aplicar(alvo(30), 3,
                MagiaMecanica(efeito = "dano", danoPorEnergia = "3d", condicao = "atordoado",
                    condicaoResistencia = "HT-3"), StringBuilder())
            if (imp.isNotEmpty()) { impôsAlguma = true; break }
        }
        assertTrue("em 20 seeds, a condição embutida tem que entrar ao menos uma vez", impôsAlguma)
    }
}
