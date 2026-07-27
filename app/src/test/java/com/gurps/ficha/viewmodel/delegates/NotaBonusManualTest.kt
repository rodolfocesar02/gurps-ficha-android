package com.gurps.ficha.viewmodel.delegates

import com.gurps.ficha.model.DefesasAtivas
import com.gurps.ficha.model.Personagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Cobre a nota do bônus manual (Lote M-1).
 *
 * Por que isso importa: os campos `bonusManual*` são um número solto — ninguém
 * sabe se o +1 na Esquiva veio de um anel, de uma magia ou de "Reflexos em
 * Combate" (que o app ainda não calcula). Quando as vantagens forem
 * automatizadas, o bônus digitado para algo que o app passou a calcular vira
 * CONTAGEM DUPLA silenciosa. A nota é o que permite decidir o que fazer.
 *
 * A regra central testada aqui: **bônus e nota são independentes**. Apagar a
 * nota não pode zerar o bônus, e mexer no bônus não pode apagar a nota.
 */
class NotaBonusManualTest {

    private val delegate = FichaCombatDelegate()
    private val base = Personagem(nome = "Teste")

    // --- gravação ---

    @Test
    fun `grava bonus e nota juntos na esquiva`() {
        val p = delegate.atualizarBonusManualEsquiva(base, 2, "anel encantado")
        assertEquals(2, p.defesasAtivas.bonusManualEsquiva)
        assertEquals("anel encantado", p.defesasAtivas.notaBonusManualEsquiva)
    }

    @Test
    fun `cada defesa tem a sua nota, sem vazar para as outras`() {
        var p = delegate.atualizarBonusManualEsquiva(base, 1, "botas élficas")
        p = delegate.atualizarBonusManualApara(p, 2, "espada abençoada")
        p = delegate.atualizarBonusManualBloqueio(p, 3, "escudo do rei")

        assertEquals("botas élficas", p.defesasAtivas.notaBonusManualEsquiva)
        assertEquals("espada abençoada", p.defesasAtivas.notaBonusManualApara)
        assertEquals("escudo do rei", p.defesasAtivas.notaBonusManualBloqueio)
    }

    // --- independência (o coração do lote) ---

    @Test
    fun `apagar a nota NAO zera o bonus`() {
        val comNota = delegate.atualizarBonusManualEsquiva(base, 3, "magia temporária")
        val semNota = delegate.atualizarBonusManualEsquiva(comNota, 3, "")

        assertEquals(3, semNota.defesasAtivas.bonusManualEsquiva)
        assertEquals("", semNota.defesasAtivas.notaBonusManualEsquiva)
    }

    @Test
    fun `zerar o bonus NAO apaga a nota`() {
        val comBonus = delegate.atualizarBonusManualApara(base, 2, "adaga élfica")
        val semBonus = delegate.atualizarBonusManualApara(comBonus, 0, "adaga élfica")

        assertEquals(0, semBonus.defesasAtivas.bonusManualApara)
        assertEquals("adaga élfica", semBonus.defesasAtivas.notaBonusManualApara)
    }

    // --- limites e higiene ---

    @Test
    fun `nota muito longa e cortada, sem quebrar`() {
        val enorme = "x".repeat(500)
        val p = delegate.atualizarBonusManualEsquiva(base, 1, enorme)
        assertTrue(
            "nota deveria ter sido cortada, veio com ${p.defesasAtivas.notaBonusManualEsquiva.length}",
            p.defesasAtivas.notaBonusManualEsquiva.length <= 120
        )
    }

    @Test
    fun `espacos em volta da nota sao removidos`() {
        val p = delegate.atualizarBonusManualBloqueio(base, 1, "   escudo grande   ")
        assertEquals("escudo grande", p.defesasAtivas.notaBonusManualBloqueio)
    }

    @Test
    fun `o teto do bonus continua valendo`() {
        // A nota não pode ter afrouxado o coerceIn(-20, 20) que ja existia.
        val p = delegate.atualizarBonusManualEsquiva(base, 999, "tentativa de furar o limite")
        assertEquals(20, p.defesasAtivas.bonusManualEsquiva)
    }

    // --- retrocompatibilidade ---

    @Test
    fun `ficha antiga sem o campo nasce com nota vazia`() {
        // Gson deserializa ficha antiga sem os campos novos -> default "".
        val antiga = DefesasAtivas(bonusManualEsquiva = 2)
        assertEquals("", antiga.notaBonusManualEsquiva)
        assertEquals("", antiga.notaBonusManualApara)
        assertEquals("", antiga.notaBonusManualBloqueio)
        assertEquals(2, antiga.bonusManualEsquiva)
    }

    @Test
    fun `chamada sem nota mantem o comportamento antigo`() {
        // O parametro tem default "": call sites antigos continuam compilando
        // e nao gravam nota por engano.
        val p = delegate.atualizarBonusManualEsquiva(base, 4)
        assertEquals(4, p.defesasAtivas.bonusManualEsquiva)
        assertEquals("", p.defesasAtivas.notaBonusManualEsquiva)
    }
}
