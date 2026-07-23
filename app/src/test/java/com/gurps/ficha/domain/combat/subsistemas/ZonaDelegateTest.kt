package com.gurps.ficha.domain.combat.subsistemas

import com.gurps.ficha.domain.combat.Combatente
import com.gurps.ficha.domain.combat.NpcStats
import com.gurps.ficha.domain.combat.ZonaPersistente
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Lote MOTOR-1: o subsistema de ZONAS agora é testável SOZINHO — sem montar um `CombatSession`
 * inteiro. É o ganho concreto da extração: antes, cobrir isto exigia rodar um combate completo
 * (via os invariantes de integração); agora dá para testar a regra da zona em isolamento.
 */
class ZonaDelegateTest {

    private fun goblin(id: String = "g", pv: Int = 10, distancia: Int = 0) = Combatente(
        id = id, nome = id, dx = 11, velocidadeBasica = 5.0, deslocamento = 5, pvMax = pv, pvAtual = pv,
        stats = NpcStats(st = 11, dx = 11, ht = 11, pvMax = pv)
    )

    private fun zona(nome: String = "Chuva de Fogo", dano: String = "1d-1", intervalo: Int = 1,
                    dur: Int = 20, raio: Int = 3, teste: String? = null, operador: String = "heroi") =
        ZonaPersistente(nome = nome, centro = null, raioM = raio, danoExpr = dano, tipoDano = "quei",
            armadura = null, intervaloSeg = intervalo, teste = teste,
            segRestantes = dur, segAteProximo = intervalo, operadorId = operador)

    /** Delegate com um goblin dentro do raio; distância ao herói = 0 (o goblin está colado no centro). */
    private fun delegateCom(vararg combs: Combatente, log: MutableList<String> = mutableListOf()): ZonaDelegate =
        ZonaDelegate(
            log = log,
            random = Random(7),
            combatentes = { combs.toList() },
            distanciaAoHeroi = { 0 },                 // todos dentro do raio, no modelo de faixas
            htDoAlvo = { it.stats?.ht ?: 10 },
            rdDaZona = { _, _ -> 0 },
            aoMudarEstado = {},
        )

    @Test
    fun `registrar loga a cobertura e a zona fica ativa`() {
        val log = mutableListOf<String>()
        val d = delegateCom(goblin(), log = log)
        d.registrarZona(zona())
        assertEquals(1, d.zonasAtivas.size)
        assertTrue(log.any { it.contains("cobre a área") })
    }

    @Test
    fun `estreia — NAO fere no turno da criacao, fere a partir do seguinte`() {
        val g = goblin(pv = 12)
        val d = delegateCom(g)
        d.registrarZona(zona(dur = 10))
        d.tiqueDasZonas()                              // turno da conjuração: só relógio, sem dano
        assertEquals("não fere na estreia", 12, g.pvAtual)
        d.tiqueDasZonas()
        assertTrue("a partir do 2º turno fere", g.pvAtual < 12)
    }

    @Test
    fun `duas zonas da MESMA magia nao acumulam — vale a mais forte`() {
        val g = goblin(pv = 30)
        val d = delegateCom(g)
        d.registrarZona(zona(dur = 20))                // 1d-1
        d.registrarZona(zona(dur = 20))                // outra igual
        repeat(4) { d.tiqueDasZonas() }
        val perdidoDuas = 30 - g.pvAtual

        val g1 = goblin(pv = 30)
        val d1 = delegateCom(g1)
        d1.registrarZona(zona(dur = 20))               // só uma
        repeat(4) { d1.tiqueDasZonas() }
        assertEquals("duas iguais não ferem mais que uma", 30 - g1.pvAtual, perdidoDuas)
    }

    @Test
    fun `a area ENCOLHE mas nunca EXPANDE`() {
        val d = delegateCom(goblin())
        d.registrarZona(zona(raio = 3))
        assertTrue(d.encolherZona("Chuva de Fogo", 1))
        assertEquals(1, d.zonasAtivas.first().raioM)
        assertFalse("expandir é recusado", d.encolherZona("Chuva de Fogo", 5))
        assertEquals(1, d.zonasAtivas.first().raioM)
    }

    @Test
    fun `zona expira e some da lista`() {
        val d = delegateCom(goblin())
        d.registrarZona(zona(dur = 2))
        repeat(6) { d.tiqueDasZonas() }
        assertTrue(d.zonasAtivas.isEmpty())
    }

    @Test
    fun `a ocupacao pode ser SUBSTITUIDA (ponto de injecao da grade)`() {
        val g = goblin(pv = 12)
        val d = delegateCom(g)
        d.ocupantesDaZona = { emptyList() }            // "ninguém dentro" (a grade diz assim)
        d.registrarZona(zona(dur = 10))
        repeat(4) { d.tiqueDasZonas() }
        assertEquals("sem ocupantes, ninguém é ferido", 12, g.pvAtual)
    }
}
