package com.gurps.ficha.domain.combat.subsistemas

import com.gurps.ficha.domain.combat.Combatente
import com.gurps.ficha.domain.combat.Condicao
import com.gurps.ficha.domain.combat.NpcStats
import com.gurps.ficha.domain.magic.BuffAplicado
import com.gurps.ficha.domain.magic.MagiaMecanica
import com.gurps.ficha.domain.magic.TipoDuracao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Lote MOTOR-4: o subsistema de efeitos mágicos ativos agora é testável SOZINHO — sem montar um
 * `CombatSession`. As três regras sutis (não-acúmulo, regra da estreia, abalo de concentração) antes
 * só eram exercidas rodando um combate inteiro.
 */
class EfeitosMagicosDelegateTest {

    private fun heroi() = Combatente(
        id = "h", nome = "Herói", dx = 12, velocidadeBasica = 6.0, deslocamento = 6, pvMax = 20, pvAtual = 20,
        ehHeroi = true, pfAtual = 10, stats = NpcStats(st = 12, dx = 12, ht = 12, pvMax = 20)
    )

    private fun goblin() = Combatente(
        id = "g", nome = "Goblin", dx = 11, velocidadeBasica = 5.0, deslocamento = 5, pvMax = 20, pvAtual = 20,
        stats = NpcStats(st = 11, dx = 11, ht = 11, pvMax = 20)
    )

    /** Delegate ligado a uma lista mutável de combatentes; o herói é o índice 0. */
    private fun cenario(
        combatentes: MutableList<Combatente>, seed: Long = 7,
        log: MutableList<String> = mutableListOf(),
    ) = EfeitosMagicosDelegate(
        log = log,
        random = Random(seed),
        combatentes = { combatentes },
        heroi = { combatentes.first { it.ehHeroi } },
        heroiHt = { 12 },
        heroiVontade = { 12 },
        verificarFim = { },
    )

    // ── Não acumula (Magia p.9, MEC-29) ──────────────────────────────────────────────────────────

    @Test
    fun `duas versoes da mesma magica no mesmo alvo — fica so a mais forte`() {
        val g = goblin()
        val mundo = mutableListOf(heroi(), g)
        val log = mutableListOf<String>()
        val d = cenario(mundo, log = log)
        d.registrar("Escudo", "heroi", g.id, 10, 0, TipoDuracao.TEMPORARIA, false, BuffAplicado(alvoId = g.id, bd = 2))
        d.registrar("Escudo", "heroi", g.id, 10, 0, TipoDuracao.TEMPORARIA, false, BuffAplicado(alvoId = g.id, bd = 4))
        assertEquals("só uma Escudo fica ativa", 1, d.ativas.size)
        assertEquals("vale a mais forte (BD 4)", 4, g.buffBd)
        assertTrue(log.any { it.contains("substitui") })
    }

    @Test
    fun `a versao mais FRACA lancada depois e rejeitada`() {
        val g = goblin()
        val mundo = mutableListOf(heroi(), g)
        val log = mutableListOf<String>()
        val d = cenario(mundo, log = log)
        d.registrar("Escudo", "heroi", g.id, 10, 0, TipoDuracao.TEMPORARIA, false, BuffAplicado(alvoId = g.id, bd = 4))
        d.registrar("Escudo", "heroi", g.id, 10, 0, TipoDuracao.TEMPORARIA, false, BuffAplicado(alvoId = g.id, bd = 2))
        assertEquals(1, d.ativas.size)
        assertEquals("a fraca não derruba a forte", 4, g.buffBd)
        assertTrue(log.any { it.contains("não se acumulam") })
    }

    @Test
    fun `dissipar REVERTE o buff que a magica aplicou`() {
        val g = goblin()
        val mundo = mutableListOf(heroi(), g)
        val d = cenario(mundo)
        d.registrar("Escudo", "heroi", g.id, 10, 0, TipoDuracao.TEMPORARIA, false, BuffAplicado(alvoId = g.id, bd = 3))
        assertEquals(3, g.buffBd)
        assertTrue(d.dissipar("Escudo"))
        assertEquals("o BD tem que voltar a zero", 0, g.buffBd)
        assertTrue(d.ativas.isEmpty())
    }

    // ── Regra da estreia (MEC-22) ────────────────────────────────────────────────────────────────

    private fun morteCandente(alvoId: String) = MagiaMecanica(
        efeito = "dano", danoPorTurnoExpr = "3d", danoPorTurnoTeste = "HT", quebraEmSucessoDecisivo = true)

    @Test
    fun `magica que fere a cada turno NAO fere no turno em que foi lancada`() {
        val g = goblin()
        val mundo = mutableListOf(heroi(), g)
        val d = cenario(mundo)
        d.registrar("Morte Candente", "heroi", g.id, 30, 0, TipoDuracao.TEMPORARIA, false, mecanica = morteCandente(g.id))
        d.tiquePorTurno()  // primeiro tique = estreia, não fere
        assertEquals("no turno da estreia o alvo não perde PV", 20, g.pvAtual)
        assertTrue("a mágica segue ativa", d.ativas.isNotEmpty())
    }

    @Test
    fun `depois da estreia a magica passa a ferir`() {
        var feriuAlgumaVez = false
        for (seed in 0L until 30L) {
            val g = goblin()
            val mundo = mutableListOf(heroi(), g)
            val d = cenario(mundo, seed = seed)
            d.registrar("Morte Candente", "heroi", g.id, 30, 0, TipoDuracao.TEMPORARIA, false, mecanica = morteCandente(g.id))
            d.tiquePorTurno() // estreia
            d.tiquePorTurno() // agora pode ferir
            if (g.pvAtual < 20) { feriuAlgumaVez = true; break }
        }
        assertTrue("passada a estreia, em 30 seeds tem que ferir ao menos uma vez", feriuAlgumaVez)
    }

    // ── Abalo de concentração (Magia p.7, MEC-26) ────────────────────────────────────────────────

    @Test
    fun `heroi ILESO e lucido nao perde a concentracao`() {
        val h = heroi() // choquePendente 0, sem condições
        val mundo = mutableListOf(h)
        val d = cenario(mundo)
        d.registrar("Aerovisão", "heroi", h.id, 60, 0, TipoDuracao.TEMPORARIA, exigeConcentracao = true)
        val congeladas = d.abaloDeConcentracao()
        assertTrue("sem gatilho, nada congela", congeladas.isEmpty())
        assertTrue("a mágica continua ativa", d.ativas.isNotEmpty())
    }

    @Test
    fun `heroi FERIDO pode perder a concentracao — congela ou desfaz`() {
        var afetouAlgumaVez = false
        for (seed in 0L until 40L) {
            val h = heroi().apply { choquePendente = 2 } // gatilho: levou dano
            val mundo = mutableListOf(h)
            val d = cenario(mundo, seed = seed)
            d.registrar("Aerovisão", "heroi", h.id, 60, 0, TipoDuracao.TEMPORARIA, exigeConcentracao = true)
            val congeladas = d.abaloDeConcentracao()
            if (congeladas.isNotEmpty() || d.ativas.isEmpty()) { afetouAlgumaVez = true; break }
        }
        assertTrue("ferido, em 40 seeds a concentração tem que abalar ao menos uma vez", afetouAlgumaVez)
    }

    @Test
    fun `heroi ATORDOADO tambem dispara o abalo`() {
        val h = heroi().apply { condicoes.add(Condicao.ATORDOADO) }
        val mundo = mutableListOf(h)
        val log = mutableListOf<String>()
        val d = cenario(mundo, log = log)
        d.registrar("Aerovisão", "heroi", h.id, 60, 0, TipoDuracao.TEMPORARIA, exigeConcentracao = true)
        d.abaloDeConcentracao()
        assertTrue("o abalo tem que ter rodado (logou algo sobre concentração)",
            log.any { it.contains("concentração") })
    }

    @Test
    fun `so magica do heroi que EXIGE concentracao sofre o abalo`() {
        val h = heroi().apply { choquePendente = 2 }
        val mundo = mutableListOf(h)
        val d = cenario(mundo)
        // Duradoura SEM concentração: o abalo não a toca.
        d.registrar("Escudo", "heroi", h.id, 60, 0, TipoDuracao.TEMPORARIA, exigeConcentracao = false,
            buff = BuffAplicado(alvoId = h.id, bd = 2))
        val congeladas = d.abaloDeConcentracao()
        assertTrue(congeladas.isEmpty())
        assertFalse("Escudo não exige concentração — segue firme", d.ativas.isEmpty())
    }
}
