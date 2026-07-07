package com.gurps.ficha.domain.combat.hex

import com.gurps.ficha.domain.combat.CombatEncounter
import com.gurps.ficha.domain.combat.Combatente
import com.gurps.ficha.domain.combat.NpcStats
import org.junit.Assert.assertEquals
import org.junit.Test

/** Lote HEX-3: sincronia hex ↔ metros do encounter. Kotlin puro. */
class HexCombatSyncTest {

    private fun heroi() = Combatente("heroi", "Herói", ehHeroi = true, dx = 13, velocidadeBasica = 6.0,
        deslocamento = 6, pvMax = 12, pvAtual = 12)

    private fun goblin() = Combatente("goblin_1", "Goblin", dx = 11, velocidadeBasica = 5.0,
        deslocamento = 5, pvMax = 7, pvAtual = 7,
        stats = NpcStats(st = 11, dx = 11, ht = 11, pvMax = 7, armaDano = "1d-1", armaTipo = "corte", armaNh = 11))

    @Test
    fun `projetarSetupInicial transporta a distancia em hex para o encounter`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin_1" to 999), seed = 1L)
        val estado = HexCombatState.setupInicial(idHeroi = "heroi", idInimigo = "goblin_1")
        HexCombatSync.projetarSetupInicial(estado, enc)
        // Setup inicial: herói (0,0), goblin (3,0) → distância = 3 hexes = 3 metros.
        assertEquals(3, enc.distancia(g))
    }

    @Test
    fun `mover heroi 1 hex ao leste reduz a distancia em 1`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin_1" to 999), seed = 1L)
        var estado = HexCombatState.setupInicial()
        estado = estado.mover("heroi", HexCoord(1, 0))
        HexCombatSync.projetarSetupInicial(estado, enc)
        assertEquals(2, enc.distancia(g))
    }

    @Test
    fun `projetar DEPOIS de uma manobra que mexe em distancia SOBRESCREVERIA a mudanca do motor`() {
        // Tranca contratual (bug pego pela revisão do HEX-3): re-projetar após heroiEncontrao (que força
        // distância=1) devolveria o NPC para a posição estática do hex, desfazendo o corpo-a-corpo. Por isso
        // `projetarSetupInicial` é APENAS para SETUP; a portabilidade das manobras é pré-requisito do HEX-4/5.
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin_1" to 999), seed = 1L)
        val estado = HexCombatState.setupInicial()
        HexCombatSync.projetarSetupInicial(estado, enc)
        assertEquals(3, enc.distancia(g)) // setup: 3 hexes = 3m
        // Simula heroiEncontrao (que forçaria distância=1m no motor):
        enc.definirDistancia("goblin_1", 1)
        assertEquals(1, enc.distancia(g))
        // Se re-projetássemos agora, a mudança do motor seria APAGADA (por isso NÃO chamamos após ações).
        HexCombatSync.projetarSetupInicial(estado, enc)
        assertEquals("re-projeção desfaz a mudança do motor — este teste TRANCA o contrato de setup-only", 3, enc.distancia(g))
    }

    @Test
    fun `combatente sem posicao na grade e ignorado (mantem distancia anterior)`() {
        val outroGoblin = Combatente("goblin_2", "Goblin 2", dx = 11, velocidadeBasica = 5.0,
            deslocamento = 5, pvMax = 7, pvAtual = 7, stats = goblin().stats)
        val enc = CombatEncounter(listOf(heroi(), goblin(), outroGoblin),
            mapOf("goblin_1" to 999, "goblin_2" to 5), seed = 1L)
        val estado = HexCombatState.setupInicial() // só tem goblin_1
        HexCombatSync.projetarSetupInicial(estado, enc)
        assertEquals(3, enc.distancia(enc.combatentes.first { it.id == "goblin_1" }))
        assertEquals(5, enc.distancia(outroGoblin)) // intocado
    }
}
