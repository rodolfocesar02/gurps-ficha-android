package com.gurps.ficha.domain.combat.subsistemas

import com.gurps.ficha.domain.combat.Combatente
import com.gurps.ficha.domain.combat.NpcStats
import com.gurps.ficha.domain.magic.ContextoConjuracao
import com.gurps.ficha.domain.magic.MagiaMecanica
import com.gurps.ficha.domain.magic.MagicClassParser
import com.gurps.ficha.domain.magic.NivelMana
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Lote MOTOR-3: a resolução de acerto+defesa da magia à distância agora é testável SOZINHA — sem
 * montar um `CombatSession`. Antes, o feixe e o arremesso só eram cobertos por combate completo.
 */
class AtaqueMagicoResolverTest {

    private fun alvo() = Combatente(
        id = "g", nome = "Goblin", dx = 11, velocidadeBasica = 5.0, deslocamento = 5, pvMax = 20, pvAtual = 20,
        stats = NpcStats(st = 11, dx = 11, ht = 11, pvMax = 20)
    )

    private val funil = DanoMagicoResolver(
        random = Random(1), rdContraMagia = { _, _ -> 0 }, imporCondicao = { _, _, _, _ -> })

    /** Resolver do herói sem a perícia (usa DX 13), NPC que nunca se defende, seed fixa. */
    private fun resolver(seed: Long = 7, nhInato: Int? = null, npcDefende: Boolean = false) =
        AtaqueMagicoResolver(
            random = Random(seed),
            danoMagico = DanoMagicoResolver(Random(seed), { _, _ -> 0 }, { _, _, _, _ -> }),
            heroiNhAtaqueInato = { nhInato },
            heroiDx = { 13 },
            esquivaNpc = { 6 },
            bloqueioNpc = { 0 },
            npcSeDefendeu = { _, _ -> npcDefende },
        )

    private fun ctxFeixe(penal: Int = 4, bloqueavel: Boolean = true) = ContextoConjuracao(
        nhBasico = 30, classe = MagicClassParser.parse("Comum"), mana = NivelMana.NORMAL,
        distanciaMetros = 3,
        mecanica = MagiaMecanica(efeito = "dano", entrega = "feixe", danoPorEnergia = "1d-1",
            feixePenalidadeDx = penal, feixeBloqueavel = bloqueavel))

    @Test
    fun `o feixe pode ERRAR e ACERTAR — nao acerta sozinho`() {
        var errou = false; var acertou = false
        for (seed in 0L until 40L) {
            val sb = StringBuilder()
            resolver(seed).resolverFeixe(alvo(), 2, ctxFeixe(), sb)
            if (sb.toString().contains("passa longe")) errou = true
            if (sb.toString().contains("acerta")) acertou = true
        }
        assertTrue("tem que poder errar", errou)
        assertTrue("e acertar", acertou)
    }

    @Test
    fun `com a pericia Ataque Inato o feixe NAO sofre a penalidade da DX`() {
        val sb = StringBuilder()
        resolver(nhInato = 16).resolverFeixe(alvo(), 2, ctxFeixe(), sb)
        assertTrue("cita a perícia, não a DX: $sb", sb.toString().contains("Ataque Inato NH 16"))
    }

    @Test
    fun `os Sopros da boca usam DX menos 2`() {
        val sb = StringBuilder()
        resolver().resolverFeixe(alvo(), 2, ctxFeixe(penal = 2), sb)
        assertTrue(sb.toString().contains("DX−2"))
    }

    @Test
    fun `o alvo pode se defender do feixe`() {
        val sb = StringBuilder()
        resolver(npcDefende = true, nhInato = 20).resolverFeixe(alvo(), 2, ctxFeixe(), sb)
        assertTrue(sb.toString().contains("se defende do jato"))
    }

    @Test
    fun `o arremesso do projetil resolve acerto e defesa`() {
        var acertou = false
        for (seed in 0L until 40L) {
            val sb = StringBuilder()
            val ctx = ContextoConjuracao(nhBasico = 30, classe = MagicClassParser.parse("Projétil"),
                mana = NivelMana.NORMAL, distanciaMetros = 3,
                mecanica = MagiaMecanica(efeito = "dano", danoPorEnergia = "3d"))
            resolver(seed, nhInato = 20).resolverArremesso(alvo(), 2, ctx, sb)
            if (sb.toString().contains("Projétil acerta")) acertou = true
        }
        assertTrue(acertou)
    }

    @Test
    fun `a explosao respinga nos vizinhos injetados`() {
        val vizinho = alvo().copy(id = "v", nome = "Vizinho")
        val r = resolver(nhInato = 20)
        r.vizinhosDoImpacto = { listOf(vizinho to 1) }  // um vizinho a 1m
        var respingou = false
        for (seed in 0L until 30L) {
            val rr = AtaqueMagicoResolver(Random(seed), funil, { 20 }, { 13 }, { 6 }, { 0 }, { _, _ -> false })
            rr.vizinhosDoImpacto = { listOf(vizinho.copy(pvAtual = 20) to 1) }
            val sb = StringBuilder()
            val ctx = ContextoConjuracao(nhBasico = 30, classe = MagicClassParser.parse("Projétil"),
                mana = NivelMana.NORMAL, distanciaMetros = 3,
                mecanica = MagiaMecanica(efeito = "dano", danoPorEnergia = "3d", explosaoDivisorPorMetro = 3))
            rr.resolverArremesso(alvo(), 2, ctx, sb)
            if (sb.toString().contains("Respingo da explosão")) { respingou = true; break }
        }
        assertTrue("a explosão tem que respingar no vizinho", respingou)
    }
}
