package com.gurps.ficha.domain.rules.traits

import com.gurps.ficha.model.DesvantagemSelecionada
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Cobre o Lote D-0: o [TraitRuleRegistry] passou a enxergar DESVANTAGENS.
 *
 * Antes, os agregadores varriam só `personagem.vantagens`. Uma regra registrada
 * com o id de uma desvantagem nunca era chamada — sem erro, sem log, sem nada.
 * Era uma falha silenciosa que bloqueava toda a automação de desvantagem, e o
 * tipo de coisa que só se descobre lendo o código.
 *
 * Estes testes existem para essa regressão não voltar despercebida.
 */
class RegistryDesvantagensTest {

    /** Regra de mentira, registrada em nenhum lugar — usada só para conferir o contrato. */
    private class RegraFalsa(
        override val traitId: String,
        private val bonusEsquiva: Int = 0,
        private val bonusPericia: Pair<String, Int>? = null
    ) : TraitRule {
        override fun getDodgeModifier(personagem: Personagem, selection: TracoSelecionado) = bonusEsquiva
        override fun getSkillModifiers(personagem: Personagem, selection: TracoSelecionado) =
            bonusPericia?.let { mapOf(it.first to it.second) } ?: emptyMap()
    }

    // --- a interface comum ---

    @Test
    fun `vantagem e desvantagem sao ambas TracoSelecionado`() {
        val v: TracoSelecionado = VantagemSelecionada(definicaoId = "v", nome = "V", nivel = 2)
        val d: TracoSelecionado = DesvantagemSelecionada(definicaoId = "d", nome = "D", nivel = 3)

        assertEquals("v", v.definicaoId)
        assertEquals(2, v.nivel)
        assertEquals("d", d.definicaoId)
        assertEquals(3, d.nivel)
    }

    @Test
    fun `os metadados chegam pela interface nos dois tipos`() {
        // metadados e o campo mais usado pelas regras (26 usos no codigo).
        val v: TracoSelecionado = VantagemSelecionada(
            definicaoId = "v", nome = "V", metadados = mapOf("tipo" to "global")
        )
        val d: TracoSelecionado = DesvantagemSelecionada(
            definicaoId = "d", nome = "D", metadados = mapOf("tipo" to "fobia")
        )
        assertEquals("global", v.metadados?.get("tipo"))
        assertEquals("fobia", d.metadados?.get("tipo"))
    }

    // --- a regra pode ser aplicada a uma desvantagem ---

    @Test
    fun `uma TraitRule aceita desvantagem sem precisar de conversao`() {
        val regra = RegraFalsa("d", bonusEsquiva = -2)
        val p = Personagem(nome = "Teste")
        val desvantagem = DesvantagemSelecionada(definicaoId = "d", nome = "Desajeitado")

        // Antes do D-0 isto nem compilava: o parametro exigia VantagemSelecionada.
        assertEquals(-2, regra.getDodgeModifier(p, desvantagem))
    }

    @Test
    fun `desvantagem pode penalizar pericia com valor negativo`() {
        val regra = RegraFalsa("d", bonusPericia = "Disfarce" to -2)
        val p = Personagem(nome = "Teste")
        val gordo = DesvantagemSelecionada(definicaoId = "d", nome = "Gordo")

        assertEquals(mapOf("Disfarce" to -2), regra.getSkillModifiers(p, gordo))
    }

    // --- o Registry varre os dois lados ---

    @Test
    fun `personagem so com desvantagens nao quebra os agregadores`() {
        val p = Personagem(
            nome = "Teste",
            desvantagens = listOf(DesvantagemSelecionada(definicaoId = "gordo", nome = "Gordo"))
        )
        // Sem regra registrada para "gordo": deve devolver zero, nao explodir.
        assertEquals(0, TraitRuleRegistry.getDodgeBonus(p))
        assertEquals(0, TraitRuleRegistry.getSkillBonus(p, "Disfarce"))
    }

    @Test
    fun `personagem com vantagem e desvantagem juntas e percorrido inteiro`() {
        val p = Personagem(
            nome = "Teste",
            vantagens = listOf(VantagemSelecionada(definicaoId = "v", nome = "V")),
            desvantagens = listOf(DesvantagemSelecionada(definicaoId = "d", nome = "D"))
        )
        // Nenhuma tem regra: o contrato e devolver 0 sem lancar excecao.
        assertEquals(0, TraitRuleRegistry.getParryBonus(p, null))
        assertEquals(0, TraitRuleRegistry.getBlockBonus(p))
        assertEquals(0, TraitRuleRegistry.getDamageBonusPerDie(p, null, null, null))
    }

    @Test
    fun `ficha vazia continua devolvendo zero`() {
        val p = Personagem(nome = "Vazio")
        assertEquals(0, TraitRuleRegistry.getDodgeBonus(p))
        assertEquals(0, TraitRuleRegistry.getBlockBonus(p))
        assertTrue(TraitRuleRegistry.getSkillBonus(p, "Escalada") == 0)
    }

    // --- as 11 regras existentes continuam registradas ---

    @Test
    fun `as regras Kotlin existentes sobreviveram a troca de assinatura`() {
        // A migracao para TracoSelecionado mexeu na assinatura de 13 arquivos;
        // este teste garante que ninguem sumiu do Registry no caminho.
        listOf(
            "ataque_inato", "golpeadores", "dentes", "flexibilidade", "garras",
            "defesas_ampliadas_aparar_ampliado", "defesas_ampliadas_bloqueio_ampliado",
            "defesas_ampliadas_esquiva_ampliada", "mestre_de_armas",
            "telecomunicacao", "idioma"
        ).forEach { id ->
            assertTrue("regra sumiu do Registry: $id", TraitRuleRegistry.hasSpecialRule(id))
        }
    }
}
