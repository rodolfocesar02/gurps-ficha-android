package com.gurps.ficha.ui.features.traits

import com.gurps.ficha.model.ModificadorDefinicao
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Cobre [modificadorCabeEm] — a regra que decide se um modificador do catálogo
 * geral pode ser oferecido no traço aberto.
 *
 * Antes dela, o catálogo geral inteiro (218 itens) aparecia em QUALQUER
 * vantagem: abrir "Abafador de Mana" oferecia Guelras (só de Não Respira) e
 * Pele Resistente (só de Resistência a Dano). Um erro aqui volta a poluir a
 * lista ou, pior, esconde modificador legítimo.
 */
class ModificadorDonoTest {

    private fun mod(dono: String? = null) =
        ModificadorDefinicao(id = "m", nome = "M", tipo = "ampliação", valor = "+10%", donoId = dono)

    @Test
    fun `modificador geral serve para qualquer traco`() {
        assertTrue(modificadorCabeEm(mod(dono = null), "resistencia_a_dano"))
        assertTrue(modificadorCabeEm(mod(dono = null), "abafador_de_mana"))
        assertTrue(modificadorCabeEm(mod(dono = null), null))
    }

    @Test
    fun `modificador com dono aparece no traco dono`() {
        assertTrue(modificadorCabeEm(mod(dono = "nao_respira"), "nao_respira"))
    }

    @Test
    fun `modificador com dono nao aparece em outro traco`() {
        // O caso que motivou a regra: Guelras oferecida em Abafador de Mana.
        assertFalse(modificadorCabeEm(mod(dono = "nao_respira"), "abafador_de_mana"))
    }

    @Test
    fun `modificador com dono nao aparece quando o traco e desconhecido`() {
        // Sem saber o traço, o seguro é não oferecer o que é de alguém.
        assertFalse(modificadorCabeEm(mod(dono = "nao_respira"), null))
    }

    @Test
    fun `donoId em branco conta como geral`() {
        // Robustez: JSON com "donoId": "" não deve sumir da lista.
        assertTrue(modificadorCabeEm(mod(dono = ""), "qualquer_coisa"))
        assertTrue(modificadorCabeEm(mod(dono = "   "), "qualquer_coisa"))
    }

    @Test
    fun `comparacao de dono e sensivel ao id exato`() {
        assertFalse(modificadorCabeEm(mod(dono = "retencao"), "retencao_x"))
        assertFalse(modificadorCabeEm(mod(dono = "retencao"), "Retencao"))
    }
}
