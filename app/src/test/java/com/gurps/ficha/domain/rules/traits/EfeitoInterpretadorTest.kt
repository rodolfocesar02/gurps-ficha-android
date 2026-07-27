package com.gurps.ficha.domain.rules.traits

import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Cobre o interpretador do campo `efeitos` (Lote V-0).
 *
 * Um erro aqui é pior que um erro numa regra Kotlin isolada: este código serve
 * TODAS as vantagens e desvantagens declarativas de uma vez. Um bônus que não
 * aplica não gera erro nenhum — só some.
 */
class EfeitoInterpretadorTest {

    private val p = Personagem(nome = "Teste")

    private fun sel(nivel: Int = 1) =
        VantagemSelecionada(definicaoId = "x", nome = "X", nivel = nivel)

    private fun regra(vararg efeitos: EfeitoDeclarado) =
        EfeitoInterpretador.regraDe("x", efeitos.toList())

    // --- perícia ---

    @Test
    fun `efeito simples de pericia soma no NH`() {
        val r = regra(EfeitoDeclarado(tipo = "pericia", alvo = "Escalada", valor = 2))
        assertEquals(mapOf("Escalada" to 2), r.getSkillModifiers(p, sel()))
    }

    @Test
    fun `porNivel multiplica pelo nivel da vantagem`() {
        val r = regra(EfeitoDeclarado(tipo = "pericia", alvo = "Furtividade", valor = 2, porNivel = true))
        assertEquals(mapOf("Furtividade" to 6), r.getSkillModifiers(p, sel(nivel = 3)))
    }

    @Test
    fun `sem porNivel o nivel e ignorado`() {
        val r = regra(EfeitoDeclarado(tipo = "pericia", alvo = "Escalada", valor = 2))
        assertEquals(mapOf("Escalada" to 2), r.getSkillModifiers(p, sel(nivel = 5)))
    }

    @Test
    fun `varios alvos viram varias entradas`() {
        // Caso real: Senso de Direção dá +3 em 4 perícias distintas.
        val r = regra(
            EfeitoDeclarado(tipo = "pericia", alvo = "Navegação (Ar)", valor = 3),
            EfeitoDeclarado(tipo = "pericia", alvo = "Navegação (Mar)", valor = 3),
            EfeitoDeclarado(tipo = "pericia", alvo = "Percepção do Corpo", valor = 3)
        )
        val mods = r.getSkillModifiers(p, sel())
        assertEquals(3, mods.size)
        assertEquals(3, mods["Navegação (Ar)"])
        assertEquals(3, mods["Percepção do Corpo"])
    }

    @Test
    fun `mesmo alvo declarado duas vezes soma`() {
        val r = regra(
            EfeitoDeclarado(tipo = "pericia", alvo = "Escalada", valor = 2),
            EfeitoDeclarado(tipo = "pericia", alvo = "Escalada", valor = 1)
        )
        assertEquals(mapOf("Escalada" to 3), r.getSkillModifiers(p, sel()))
    }

    // --- defesas ---

    @Test
    fun `efeito de defesa cai no gancho certo`() {
        val r = regra(
            EfeitoDeclarado(tipo = "defesa", alvo = "esquiva", valor = 1),
            EfeitoDeclarado(tipo = "defesa", alvo = "aparar", valor = 2),
            EfeitoDeclarado(tipo = "defesa", alvo = "bloqueio", valor = 3)
        )
        assertEquals(1, r.getDodgeModifier(p, sel()))
        assertEquals(2, r.getParryModifier(p, sel(), null))
        assertEquals(3, r.getBlockModifier(p, sel()))
    }

    @Test
    fun `defesa nao vaza para pericia nem o contrario`() {
        val r = regra(
            EfeitoDeclarado(tipo = "defesa", alvo = "esquiva", valor = 1),
            EfeitoDeclarado(tipo = "pericia", alvo = "Escalada", valor = 2)
        )
        assertEquals(mapOf("Escalada" to 2), r.getSkillModifiers(p, sel()))
        assertEquals(1, r.getDodgeModifier(p, sel()))
        assertEquals(0, r.getBlockModifier(p, sel()))
    }

    // --- o que NÃO deve ser aplicado ---

    @Test
    fun `bonus condicional NAO entra no NH base`() {
        // "Rosto Sincero: +1 Dissimulação PARA PARECER HONESTO" — somar sempre
        // seria errado. Vira opção na rolagem (Lote V-5).
        val r = regra(
            EfeitoDeclarado(
                tipo = "pericia", alvo = "Dissimulação", valor = 1,
                condicao = "para parecer honesto"
            )
        )
        assertTrue(r.getSkillModifiers(p, sel()).isEmpty())
    }

    @Test
    fun `efeito com escopo por membro NAO e aplicado ainda`() {
        // "ST Braçal: +1 ST só dos braços" — não há como um bônus por membro
        // entrar no cálculo global. Melhor não dar do que dar errado.
        val r = regra(
            EfeitoDeclarado(tipo = "atributo", alvo = "ST", valor = 1, escopo = "bracos")
        )
        assertEquals(0, r.getDodgeModifier(p, sel()))
        assertTrue(r.getSkillModifiers(p, sel()).isEmpty())
    }

    @Test
    fun `tipo desconhecido nao quebra e nao aplica`() {
        val r = regra(EfeitoDeclarado(tipo = "telepatia_quantica", alvo = "Escalada", valor = 5))
        assertTrue(r.getSkillModifiers(p, sel()).isEmpty())
    }

    @Test
    fun `alvo vazio e ignorado sem quebrar`() {
        val r = regra(EfeitoDeclarado(tipo = "pericia", alvo = "", valor = 3))
        assertTrue(r.getSkillModifiers(p, sel()).isEmpty())
    }

    @Test
    fun `sem efeitos devolve tudo zerado`() {
        val r = regra()
        assertTrue(r.getSkillModifiers(p, sel()).isEmpty())
        assertEquals(0, r.getDodgeModifier(p, sel()))
        assertEquals(0, r.getParryModifier(p, sel(), null))
        assertEquals(0, r.getBlockModifier(p, sel()))
    }

    // --- tolerância do JSON escrito à mão ---

    @Test
    fun `tipo aceita acento e maiuscula`() {
        assertEquals(TipoEfeito.PERICIA, TipoEfeito.de("Perícia"))
        assertEquals(TipoEfeito.PERICIA, TipoEfeito.de("PERICIA"))
        assertEquals(TipoEfeito.DEFESA, TipoEfeito.de(" defesa "))
        assertEquals(null, TipoEfeito.de("coisa"))
    }

    @Test
    fun `alvo de defesa aceita apara e aparar`() {
        val comA = regra(EfeitoDeclarado(tipo = "defesa", alvo = "apara", valor = 1))
        val comR = regra(EfeitoDeclarado(tipo = "defesa", alvo = "Aparar", valor = 1))
        assertEquals(1, comA.getParryModifier(p, sel(), null))
        assertEquals(1, comR.getParryModifier(p, sel(), null))
    }

    @Test
    fun `escopo ausente conta como global`() {
        assertEquals(EscopoEfeito.GLOBAL, EscopoEfeito.de(null))
        assertEquals(EscopoEfeito.GLOBAL, EscopoEfeito.de(""))
        // Valor desconhecido também cai em global: melhor aplicar do que sumir.
        assertEquals(EscopoEfeito.GLOBAL, EscopoEfeito.de("perna_esquerda_do_tio"))
    }

    @Test
    fun `valor negativo funciona - e assim que desvantagem penaliza`() {
        val r = regra(EfeitoDeclarado(tipo = "pericia", alvo = "Disfarce", valor = -2))
        assertEquals(mapOf("Disfarce" to -2), r.getSkillModifiers(p, sel()))
    }
}
