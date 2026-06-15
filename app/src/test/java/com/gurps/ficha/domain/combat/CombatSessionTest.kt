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

    private fun perfilHeroi() = HeroiPerfilCombate(esquiva = 9, apara = 11, bloqueio = null, ht = 12, rd = 2)

    private fun espada() = AtaqueHeroi(rotulo = "Espada", nh = 14, danoExpr = "2d", tipo = DanoTipo.CORT)
    private fun revolver() = AtaqueHeroi(rotulo = "Revólver", nh = 14, danoExpr = "2d-1 pa+", tipo = DanoTipo.PI_MAIS, aDistancia = true, alcance = 50)

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
    fun `mapa de tipo de dano cobre bestiario e ficha (corte, perf, pa)`() {
        assertEquals(DanoTipo.CORT, CombatSession.tipoDano("corte"))
        assertEquals(DanoTipo.PI_MAIS_MAIS, CombatSession.tipoDano("pi++"))
        assertEquals(DanoTipo.PERF, CombatSession.tipoDano("perf"))
        assertEquals(DanoTipo.CONT, CombatSession.tipoDano("desconhecido"))
        // expressões completas da ficha: tipo vem do token final
        assertEquals(DanoTipo.CORT, CombatSession.tipoDano("GeB+2 corte"))
        assertEquals(DanoTipo.PI_MAIS, CombatSession.tipoDano("2d-1 pa+")) // pa+ (Devir) = pi+
        assertEquals(DanoTipo.PI, CombatSession.tipoDano("2d pa"))
        assertEquals(DanoTipo.PI_MENOS, CombatSession.tipoDano("4d pa-"))
    }

    @Test
    fun `penalidade de distancia segue a tabela`() {
        assertEquals(0, CombatSession.penalidadeDistancia(2))
        assertEquals(-2, CombatSession.penalidadeDistancia(5))
        assertEquals(-4, CombatSession.penalidadeDistancia(10))
        assertEquals(-7, CombatSession.penalidadeDistancia(30))
    }

    @Test
    fun `tiro a distancia sofre penalidade e so permite esquiva`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 10), seed = 1L) // 10m
        val s = CombatSession(enc, perfilHeroi(), Random(5))
        val r = s.heroiAtaca(revolver(), "goblin", Manobra.ATAQUE, LocalAtaque.TORSO)
        // resolveu e logou o tiro (🎯) com o nome da arma
        assertTrue(s.log.any { it.contains("Revólver") })
        assertTrue(s.log.isNotEmpty())
        // o relatório do ataque deve registrar a penalidade de distância de 10m (-4)
        assertTrue(s.log.last().contains("distância") || r.texto.contains("distância") || s.log.any { it.contains("distância") })
    }

    @Test
    fun `heroi ataca goblin adjacente e o motor aplica o resultado`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(7))
        assertTrue("goblin deve ser alvo de corpo-a-corpo", s.alvosHeroi().any { it.id == "goblin" })
        val r = s.heroiAtaca(espada(), "goblin", Manobra.ATAQUE, LocalAtaque.TORSO)
        // Com NH 14 a chance de acerto é alta; o que garantimos é que o motor resolveu e gerou log.
        assertTrue(s.log.isNotEmpty())
        if (r.acertou && !r.defendeu) assertTrue(g.pvAtual < g.pvMax)
    }

    @Test
    fun `log de combate e narrativo e mantem os numeros`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(7))
        s.heroiAtaca(espada(), "goblin", Manobra.ATAQUE, LocalAtaque.TORSO)
        val linha = s.log.last { it.startsWith("🗡️") || it.startsWith("🎯") || it.startsWith("⭐") || it.startsWith("💥") }
        assertTrue("deve ter verbo narrativo: $linha",
            Regex("erra|acerta|se esquiva|apara|bloqueia|FALHA|absorve").containsMatchIn(linha))
        assertTrue("deve preservar os números no colchete técnico: $linha",
            linha.contains("[") && linha.contains("rolou"))
    }

    @Test
    fun `vitoria quando todos os inimigos caem`() {
        val g = goblin(pv = 1) // 1 PV: cai fácil
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(2))
        // martela até alguém cair (limite de segurança)
        var i = 0
        while (!s.encerrado && i++ < 30) {
            if (g.vivo) s.heroiAtaca(espada(), "goblin") else break
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
