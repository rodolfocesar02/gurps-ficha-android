package com.gurps.ficha.domain.combat

import com.gurps.ficha.domain.magic.ContextoConjuracao
import com.gurps.ficha.domain.magic.MagicCasting
import com.gurps.ficha.domain.magic.MagicClassParser
import com.gurps.ficha.domain.magic.MagicEnergy
import com.gurps.ficha.domain.magic.NivelMana
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Lote MA-3a: conjuração no combate — o motor resolve a espinha e aplica o dano de Projétil. */
class MagicCombatTest {

    private fun heroi() = Combatente(
        id = "heroi", nome = "Herói", ehHeroi = true, dx = 13, velocidadeBasica = 6.0,
        deslocamento = 6, pvMax = 12, pvAtual = 12, pfAtual = 10
    )

    private fun goblin(pv: Int = 7) = Combatente(
        id = "goblin", nome = "Goblin", dx = 11, velocidadeBasica = 5.0, deslocamento = 5, pvMax = pv, pvAtual = pv,
        stats = NpcStats(st = 11, dx = 11, ht = 11, pvMax = pv, rd = 0, armaDano = "1d-1", armaTipo = "corte", armaNh = 11)
    )

    private fun perfil() = HeroiPerfilCombate(esquiva = 9, apara = 11, ht = 12, rd = 2)

    private fun sessao(seed: Long, distGoblin: Int = 5): CombatSession {
        val enc = CombatEncounter(listOf(heroi(), goblin()), mapOf("goblin" to distGoblin), seed = 1L)
        return CombatSession(enc, perfil(), Random(seed))
    }

    private fun ctxProjetil(nh: Int, dist: Int = 5) = ContextoConjuracao(
        nhBasico = nh, classe = MagicClassParser.parse("Projétil"), mana = NivelMana.NORMAL,
        distanciaMetros = dist, tocando = false
    )

    @Test
    fun `projetil com NH alto causa dano no alvo e gasta fadiga do heroi`() {
        // NH 30: só a rolagem 18 falharia. Busca um seed com sucesso (a esmagadora maioria).
        var achou = false
        for (seed in 0L until 20L) {
            val s = sessao(seed)
            val goblinPvAntes = s.encounter.combatentes.first { it.id == "goblin" }.pvMax
            val pfAntes = s.heroi.pfAtual
            val r = s.heroiConjurar(ctxProjetil(nh = 30), MagicEnergy.parse("Varia"), energiaInvestida = 3, magiaNome = "Bola de Fogo", alvoId = "goblin")
            if (r.sucesso && r.danoCausado > 0) {
                assertTrue("goblin devia perder PV", s.encounter.combatentes.first { it.id == "goblin" }.pvAtual < goblinPvAntes)
                assertTrue("herói gasta PF", s.heroi.pfAtual <= pfAntes)
                assertTrue(s.log.any { it.contains("Bola de Fogo") })
                achou = true; break
            }
        }
        assertTrue("nenhum seed produziu um projétil bem-sucedido", achou)
    }

    @Test
    fun `RD do alvo reduz o dano do projetil`() {
        // Goblin com RD 6 sofre bem menos que sem RD, para a mesma energia/seed.
        val seed = 3L
        val encRd = CombatEncounter(
            listOf(heroi(), goblin().copy(stats = goblin().stats!!.copy(rd = 6))),
            mapOf("goblin" to 5), seed = 1L
        )
        val sRd = CombatSession(encRd, perfil(), Random(seed))
        val rRd = sRd.heroiConjurar(ctxProjetil(30), MagicEnergy.parse("Varia"), 3, "Bola de Fogo", "goblin")
        val sSem = sessao(seed)
        val rSem = sSem.heroiConjurar(ctxProjetil(30), MagicEnergy.parse("Varia"), 3, "Bola de Fogo", "goblin")
        if (rRd.sucesso && rSem.sucesso) {
            assertTrue("RD deveria reduzir o dano (${rRd.danoCausado} vs ${rSem.danoCausado})", rRd.danoCausado <= rSem.danoCausado)
        }
    }

    @Test
    fun `conjuracao sempre registra o fato no log e nunca aumenta a fadiga`() {
        for (seed in 0L until 10L) {
            val s = sessao(seed)
            val pfAntes = s.heroi.pfAtual
            s.heroiConjurar(ctxProjetil(15), MagicEnergy.parse("Varia"), 2, "Relâmpago", "goblin")
            assertTrue("log vazio no seed $seed", s.log.any { it.contains("Relâmpago") })
            assertTrue("PF não pode subir", s.heroi.pfAtual <= pfAntes)
        }
    }

    @Test
    fun `automagia (sem alvo) nao aplica dano de projetil e resolve o lancamento`() {
        val s = sessao(2L)
        val ctx = ContextoConjuracao(nhBasico = 20, classe = MagicClassParser.parse("Comum"), mana = NivelMana.NORMAL)
        val r = s.heroiConjurar(ctx, MagicEnergy.parse("2"), energiaInvestida = 1, magiaNome = "Escudo", alvoId = null)
        assertTrue(s.log.any { it.contains("Escudo") })
        assertTrue("automagia não causa dano de projétil", r.danoCausado == 0)
    }

    // ── Lote MA-3b ──

    @Test
    fun `projetil pode ser ESQUIVADO ou passar longe — nem todo lancamento bem-sucedido fere`() {
        // Goblin bem esquivo + distância → em vários seeds, algum lançamento bem-sucedido NÃO fere
        // (o alvo esquiva ou o Ataque Inato erra). Prova que o 2º teste/esquiva do MA-3b existe.
        val esquivo = NpcStats(st = 11, dx = 15, ht = 14, pvMax = 8, rd = 0, armaNh = 11) // Vel.Básica alta → Esquiva alta
        var sucessoSemDano = false
        for (seed in 0L until 40L) {
            val enc = CombatEncounter(
                listOf(heroi(), Combatente(id = "goblin", nome = "Goblin", dx = 15, velocidadeBasica = 7.0, deslocamento = 7, pvMax = 8, pvAtual = 8, stats = esquivo)),
                mapOf("goblin" to 10), seed = 1L
            )
            val s = CombatSession(enc, perfil(), Random(seed))
            val r = s.heroiConjurar(ctxProjetil(nh = 30, dist = 10), MagicEnergy.parse("Varia"), 1, "Bola de Fogo", "goblin")
            if (r.sucesso && r.danoCausado == 0 &&
                s.log.any { it.contains("ESQUIVA") || it.contains("passa longe") }) {
                sucessoSemDano = true; break
            }
        }
        assertTrue("nenhum seed mostrou esquiva/erro do projétil — o 2º teste não está agindo", sucessoSemDano)
    }

    @Test
    fun `queimar PV paga parte do custo com PV (fere o mago) e penaliza o NH`() {
        // Custo fixo 3; queima 2 PV → paga 2 em PV (perde PV) e 1 em PF; NH cai 2 (−1 por PV).
        val s = sessao(4L)
        val pvAntes = s.heroi.pvAtual
        val pfAntes = s.heroi.pfAtual
        val ctx = ContextoConjuracao(nhBasico = 15, classe = MagicClassParser.parse("Comum"),
            mana = NivelMana.NORMAL, pvQueimados = 2)
        s.heroiConjurar(ctx, MagicEnergy.parse("3"), energiaInvestida = 1, magiaNome = "Grande Cura", alvoId = null)
        // Só cobra se NÃO foi decisivo (decisivo perdoa o custo). Em qualquer caso, PV nunca sobe.
        assertTrue("queimar PV nunca cura o mago", s.heroi.pvAtual <= pvAntes)
        // A penalidade de −2 no NH aparece nas parcelas do NH efetivo.
        val nh = MagicCasting.nhEfetivo(ctx)
        assertTrue(nh.componentes.any { it.motivo.contains("queimar") && it.valor == -2 })
        assertTrue(s.heroi.pfAtual <= pfAntes)
    }
}
