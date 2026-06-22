package com.gurps.ficha.domain.combat

import com.gurps.ficha.domain.roll.CriticoRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Lote 364 (B5): defesas no fluxo + troca completa herói×NPC (com crítico forçado). */
class CombatResolverTest {

    @Test
    fun `modificadores de defesa - recuo, defesa total e apara extra`() {
        // Recuo: +3 esquiva, +1 apara/bloqueio.
        assertEquals(11, CombatResolver.valorDefesaFinal(CombatResolver.TipoDefesa.ESQUIVA, 8, recuo = true).first)
        assertEquals(12, CombatResolver.valorDefesaFinal(CombatResolver.TipoDefesa.APARA, 11, recuo = true).first)
        // Defesa Total Determinada: +2.
        assertEquals(13, CombatResolver.valorDefesaFinal(CombatResolver.TipoDefesa.APARA, 11, defesaTotalDeterminada = true).first)
        // 2ª apara com a mesma arma: −4.
        assertEquals(7, CombatResolver.valorDefesaFinal(CombatResolver.TipoDefesa.APARA, 11, aparasJaFeitas = 1).first)
        // 3ª apara: −8.
        assertEquals(3, CombatResolver.valorDefesaFinal(CombatResolver.TipoDefesa.APARA, 11, aparasJaFeitas = 2).first)
        // Arma de esgrima: apara extra −2 em vez de −4 (MB p.404).
        assertEquals(9, CombatResolver.valorDefesaFinal(CombatResolver.TipoDefesa.APARA, 11, aparasJaFeitas = 1, esgrima = true).first)
        assertEquals(7, CombatResolver.valorDefesaFinal(CombatResolver.TipoDefesa.APARA, 11, aparasJaFeitas = 2, esgrima = true).first)
    }

    // Lote 389 — Retirada (MB p.377): exceção marcial +3 ao aparar com esgrima + variantes "com recuo" no card.
    @Test
    fun `Retirada - esgrima apara +3 e opcoesDefesa emite variantes com recuo`() {
        // Aparar com esgrima ao recuar: +3 (em vez de +1).
        assertEquals(14, CombatResolver.valorDefesaFinal(CombatResolver.TipoDefesa.APARA, 11, recuo = true, esgrima = true).first)
        // Aparar normal ao recuar: +1.
        assertEquals(12, CombatResolver.valorDefesaFinal(CombatResolver.TipoDefesa.APARA, 11, recuo = true).first)
        // permitirRecuo emite as variantes: Esquiva 9→12 (+3), Aparar 11→12 (+1).
        val ops = CombatResolver.opcoesDefesa(
            esquivaBase = 9, aparaBase = 11, bloqueioBase = null,
            defesasUsadas = DefesasUsadas(), permitirRecuo = true
        )
        assertEquals(12, ops.first { it.tipo == CombatResolver.TipoDefesa.ESQUIVA && it.recuo }.valorFinal)
        assertEquals(12, ops.first { it.tipo == CombatResolver.TipoDefesa.APARA && it.recuo }.valorFinal)
        // Sem permitirRecuo: nenhuma variante com recuo.
        assertTrue(CombatResolver.opcoesDefesa(9, 11, null, DefesasUsadas()).none { it.recuo })
    }

    @Test
    fun `defesa anulada por critico ou surpresa e criticos da defesa`() {
        assertTrue(CombatResolver.defesaAnulada(criticoAtaque = true, surpresa = false))
        assertTrue(CombatResolver.defesaAnulada(criticoAtaque = false, surpresa = true))
        assertFalse(CombatResolver.defesaAnulada(criticoAtaque = false, surpresa = false))
        assertTrue(CombatResolver.defesaBemSucedida(8, 3))    // 3 sempre passa
        assertFalse(CombatResolver.defesaBemSucedida(20, 17)) // 17 sempre falha
        assertTrue(CombatResolver.defesaBemSucedida(10, 10))
        assertFalse(CombatResolver.defesaBemSucedida(10, 11))
    }

    @Test
    fun `bloqueio fica indisponivel apos bloquear no turno`() {
        val ops = CombatResolver.opcoesDefesa(
            esquivaBase = 9, aparaBase = 11, bloqueioBase = 12,
            defesasUsadas = DefesasUsadas(bloqueouEsteTurno = true)
        )
        val bloq = ops.first { it.tipo == CombatResolver.TipoDefesa.BLOQUEIO }
        assertFalse(bloq.disponivel)
    }

    @Test
    fun `round completo heroi x NPC com critico forcado anula defesa e fere`() {
        val goblin = Combatente("goblin", "Goblin", dx = 11, velocidadeBasica = 5.0, deslocamento = 5, pvMax = 9, pvAtual = 9)
        // Ataque do herói com soma 3 = DECISIVO (crítico) → anula a defesa do goblin.
        val ataqueCrit = CombatActions.RelatorioAtaque(
            calculo = CombatActions.calcularNH(14, Manobra.ATAQUE),
            dados = listOf(1, 1, 1), soma = 3,
            resultado = CombatActions.ResultadoAcerto.ACERTO, margem = 11,
            critico = CriticoRules.ResultadoCritico.DECISIVO,
            atacanteSemDefesaAtiva = false, semApararDepois = false, texto = "ataque decisivo"
        )
        val troca = CombatResolver.resolverTroca(
            defensor = goblin, htDefensor = 10, ataque = ataqueCrit,
            defesaTipo = CombatResolver.TipoDefesa.APARA, defesaValorFinal = 11, defesaSoma = 5, // defesa que passaria...
            surpresa = false, danoBaseRolado = 8, danoTipo = DanoTipo.CORT, local = LocalAtaque.TORSO, rdLocal = 1,
            randomFerimento = Random(1)
        )
        assertFalse("defesa deve ter sido anulada pelo crítico", troca.defendeu)
        assertNull(troca.defesaSoma) // anulada → nem conta a rolagem
        assertTrue(troca.dano!!.pvSubtrair > 0)
        assertTrue(goblin.pvAtual < 9) // levou dano de verdade

        // Round normal: ataque acerta, goblin APARA com sucesso → sem dano.
        val ataqueNormal = CombatActions.RelatorioAtaque(
            calculo = CombatActions.calcularNH(14, Manobra.ATAQUE),
            dados = listOf(4, 4, 2), soma = 10,
            resultado = CombatActions.ResultadoAcerto.ACERTO, margem = 4,
            critico = CriticoRules.ResultadoCritico.NORMAL,
            atacanteSemDefesaAtiva = false, semApararDepois = false, texto = "ataque normal"
        )
        val pvAntes = goblin.pvAtual
        val trocaDef = CombatResolver.resolverTroca(
            defensor = goblin, htDefensor = 10, ataque = ataqueNormal,
            defesaTipo = CombatResolver.TipoDefesa.APARA, defesaValorFinal = 11, defesaSoma = 8,
            surpresa = false, danoBaseRolado = 8, danoTipo = DanoTipo.CORT, local = LocalAtaque.TORSO, rdLocal = 1,
            randomFerimento = Random(1)
        )
        assertTrue(trocaDef.defendeu)
        assertNull(trocaDef.dano)
        assertEquals(pvAntes, goblin.pvAtual) // aparou: sem dano
    }
}
