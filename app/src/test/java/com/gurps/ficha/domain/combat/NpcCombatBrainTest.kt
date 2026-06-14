package com.gurps.ficha.domain.combat

import com.gurps.ficha.model.BestiarioLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.random.Random

private fun lerBestiarioJson(): String {
    val candidatos = listOf(
        Path.of("src", "main", "assets", "bestiario.v1.json"),
        Path.of("app", "src", "main", "assets", "bestiario.v1.json")
    )
    val p = candidatos.firstOrNull { Files.exists(it) }
        ?: error("bestiario.v1.json não encontrado para o teste")
    return String(Files.readAllBytes(p), StandardCharsets.UTF_8)
}

/** Lote 363 (B6): cérebro tático (arqueiro/bruto/covarde) + carga do bestiário + simulação. */
class NpcCombatBrainTest {

    private val heroi = Combatente("heroi", "Herói", ehHeroi = true, dx = 12, velocidadeBasica = 6.0, deslocamento = 6, pvMax = 12)

    private fun npc(
        id: String, agress: Int, moral: Int, alcance: Int, desloc: Int = 5, pvMax: Int = 12, pvAtual: Int = pvMax
    ): Combatente {
        val stats = NpcStats(dx = 11, ht = 11, pvMax = pvMax, alcanceMetros = alcance, agressividade = agress, moral = moral)
        return Combatente(id, id, dx = 11, velocidadeBasica = 5.5, deslocamento = desloc, pvMax = pvMax, pvAtual = pvAtual, stats = stats)
    }

    @Test
    fun `arqueiro mantem distancia`() {
        val arq = npc("arq", agress = 4, moral = 7, alcance = 30)
        // Longe (não engajado), alvo dentro do alcance → atira à distância.
        val encLonge = CombatEncounter(listOf(heroi, arq), mapOf("arq" to 10), seed = 0L)
        val iLonge = NpcCombatBrain.decidir(arq, encLonge, "heroi", Random(0))
        assertEquals(Manobra.ATAQUE, iLonge.manobra)
        assertTrue(iLonge.aDistancia)

        // Engajado (corpo-a-corpo) → abre distância (não fica trocando golpes).
        val encColado = CombatEncounter(listOf(heroi, arq), mapOf("arq" to 1), seed = 0L)
        val iColado = NpcCombatBrain.decidir(arq, encColado, "heroi", Random(0))
        assertEquals(Manobra.MOVER, iColado.manobra)
        assertTrue(iColado.recuar)
    }

    @Test
    fun `bruto avanca`() {
        val bruto = npc("bruto", agress = 9, moral = 8, alcance = 1, desloc = 5)
        // Não engajado a 5m (dentro de deslocamento+1) → avança e ataca.
        val enc = CombatEncounter(listOf(heroi, bruto), mapOf("bruto" to 5), seed = 0L)
        val i = NpcCombatBrain.decidir(bruto, enc, "heroi", Random(0))
        assertEquals(Manobra.MOVER_E_ATACAR, i.manobra)

        // Engajado e muito agressivo → Ataque Total.
        val encColado = CombatEncounter(listOf(heroi, bruto), mapOf("bruto" to 1), seed = 0L)
        val iColado = NpcCombatBrain.decidir(bruto, encColado, "heroi", Random(0))
        assertEquals(Manobra.ATAQUE_TOTAL, iColado.manobra)
    }

    @Test
    fun `covarde foge abaixo de 30 por cento do PV`() {
        // moral 4 -> limiar 30%. pvMax 10, pvAtual 3 (=30%) -> foge.
        val covarde = npc("cov", agress = 3, moral = 4, alcance = 1, pvMax = 10, pvAtual = 3)
        val enc = CombatEncounter(listOf(heroi, covarde), mapOf("cov" to 1), seed = 0L)
        val i = NpcCombatBrain.decidir(covarde, enc, "heroi", Random(0))
        assertEquals(Manobra.MOVER, i.manobra)
        assertTrue(i.recuar)

        // Com 4/10 (40%) NÃO foge (engajado -> ataca).
        val firme = npc("cov2", agress = 3, moral = 4, alcance = 1, pvMax = 10, pvAtual = 4)
        val enc2 = CombatEncounter(listOf(heroi, firme), mapOf("cov2" to 1), seed = 0L)
        val i2 = NpcCombatBrain.decidir(firme, enc2, "heroi", Random(0))
        assertFalse(i2.recuar)
        assertEquals(Manobra.ATAQUE, i2.manobra)
    }

    @Test
    fun `bestiario carrega e passa integridade basica`() {
        val best = BestiarioLoader.parse(lerBestiarioJson())
        assertTrue(best.criaturas.size >= 15)
        val goblin = best.get("goblin")
        assertNotNull(goblin)
        assertTrue(goblin!!.ataques.isNotEmpty())
        // converte para Combatente sem explodir
        val c = goblin.novoCombatente("g1")
        assertEquals(goblin.pv, c.pvMax)
        assertEquals(goblin.agressividade, c.stats?.agressividade)
    }

    @Test
    fun `tres goblins lutam sozinhos de forma coerente`() {
        val best = BestiarioLoader.parse(lerBestiarioJson())
        val goblinDef = best.get("goblin")!!
        val goblins = (1..3).map { goblinDef.novoCombatente("goblin$it") }
        // 3 goblins a 4m do herói (dentro do deslocamento+1) → todos devem AVANÇAR coerentemente.
        val enc = CombatEncounter(
            listOf(heroi) + goblins,
            goblins.associate { it.id to 4 },
            seed = 1L
        )
        var avancaram = 0
        goblins.forEach { g ->
            val i = NpcCombatBrain.decidir(g, enc, "heroi", Random(1))
            assertNotNull(i.manobra)
            if (i.manobra == Manobra.MOVER_E_ATACAR || i.manobra == Manobra.MOVER) avancaram++
        }
        assertEquals(3, avancaram) // ninguém trava nem foge com PV cheio
    }
}
