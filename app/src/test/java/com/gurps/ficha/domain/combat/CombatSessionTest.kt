package com.gurps.ficha.domain.combat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Lote 365 (B7): sessão de combate ponta a ponta — herói×NPC, defesas, fim de combate, parser de dano. */
class CombatSessionTest {

    private fun heroi() = Combatente(
        id = "heroi", nome = "Herói", ehHeroi = true, dx = 13, velocidadeBasica = 6.0,
        deslocamento = 6, pvMax = 12, pvAtual = 12
    )

    private fun goblin(id: String = "goblin", pv: Int = 7) = Combatente(
        id = id, nome = "Goblin", dx = 11, velocidadeBasica = 5.0, deslocamento = 5, pvMax = pv, pvAtual = pv,
        stats = NpcStats(st = 11, dx = 11, ht = 11, pvMax = pv, rd = 0, armaDano = "1d-1", armaTipo = "corte", armaNh = 11, alcanceMetros = 1, agressividade = 6, moral = 5)
    )

    private fun perfilHeroi() = HeroiPerfilCombate(
        nhArma = 14, danoArma = "2d", tipoDano = DanoTipo.CORT, esquiva = 9, apara = 11, bloqueio = null, ht = 12, rd = 2
    )

    @Test
    fun `parser de dano respeita quantidade e modificador`() {
        // soma mínima e máxima de "2d-1": [1] => 2d=2 -1 = 1 ; máximo 12-1=11
        repeat(50) {
            val v = CombatSession.rolarDano("2d-1", Random(it.toLong()))
            assertTrue("v=$v", v in 1..11)
        }
        assertEquals(0, CombatSession.rolarDano("lixo"))
        assertTrue(CombatSession.rolarDano("3d") in 3..18)
    }

    @Test
    fun `mapa de tipo de dano cobre o bestiario`() {
        assertEquals(DanoTipo.CORT, CombatSession.tipoDano("corte"))
        assertEquals(DanoTipo.PI_MAIS_MAIS, CombatSession.tipoDano("pi++"))
        assertEquals(DanoTipo.PERF, CombatSession.tipoDano("perf"))
        assertEquals(DanoTipo.CONT, CombatSession.tipoDano("desconhecido"))
    }

    @Test
    fun `heroi ataca goblin adjacente e o motor aplica o resultado`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(7))
        assertTrue("goblin deve ser alvo de corpo-a-corpo", s.alvosHeroi().any { it.id == "goblin" })
        val r = s.heroiAtaca("goblin", Manobra.ATAQUE, LocalAtaque.TORSO)
        // Com NH 14 a chance de acerto é alta; o que garantimos é que o motor resolveu e gerou log.
        assertTrue(s.log.isNotEmpty())
        if (r.acertou && !r.defendeu) assertTrue(g.pvAtual < g.pvMax)
    }

    @Test
    fun `vitoria quando todos os inimigos caem`() {
        val g = goblin(pv = 1) // 1 PV: cai fácil
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(2))
        // martela até alguém cair (limite de segurança)
        var i = 0
        while (!s.encerrado && i++ < 30) {
            if (g.vivo) s.heroiAtaca("goblin") else break
        }
        assertTrue("g.pv=${g.pvAtual}", !g.vivo || s.encerrado)
        if (!g.vivo) {
            assertTrue(s.encerrado)
            assertEquals(ResultadoCombate.VITORIA, s.resultado)
        }
    }

    @Test
    fun `npc ataca heroi e a defesa escolhida bloqueia ou nao o dano`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(3))
        val intencao = s.npcIntencao("goblin")
        // goblin engajado deve querer atacar o herói
        assertTrue(s.intencaoAtacaHeroi(intencao))
        val opcoes = s.opcoesDefesaHeroi()
        assertTrue(opcoes.any { it.tipo == CombatResolver.TipoDefesa.ESQUIVA })
        // defesa que SEMPRE passa (soma 3) → herói não sofre dano
        val defesa = DefesaHeroi(CombatResolver.TipoDefesa.ESQUIVA, 9, soma = 3)
        val pvAntes = s.heroi.pvAtual
        s.npcResolve("goblin", intencao, defesa)
        assertEquals("esquiva soma 3 sempre defende", pvAntes, s.heroi.pvAtual)
    }

    @Test
    fun `npc com moral baixa e PV no chao foge`() {
        val g = goblin(pv = 10).apply { pvAtual = 1 } // 10% de PV
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(1))
        val intencao = s.npcIntencao("goblin")
        assertTrue("deve recuar", intencao.manobra == Manobra.MOVER && intencao.recuar)
    }
}
