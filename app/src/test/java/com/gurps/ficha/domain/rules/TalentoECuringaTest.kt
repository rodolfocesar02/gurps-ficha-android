package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.traits.EfeitoDeclarado
import com.gurps.ficha.domain.rules.traits.EfeitoInterpretador
import com.gurps.ficha.domain.rules.traits.TraitRuleRegistry
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Os dez Talentos (MB p.91-92) e o curinga "qualquer perícia" (Lote TAL-1).
 *
 * Aqui os efeitos são **inventados no teste**, de propósito: o que se está
 * testando é o **mecanismo** — somar por nível, somar quando dois traços apontam
 * para a mesma perícia, e o curinga aparecer onde deve e só onde deve.
 *
 * Que os nomes de perícia do catálogo real estão certos é assunto de outro teste,
 * `AlvoDeEfeitoExisteTest`, que confere contra `pericias.json`. Separar os dois
 * importa: um teste que inventa o id **e** confia nele foi exatamente como o bug
 * do `abascanto` passou por onze testes verdes.
 */
class TalentoECuringaTest {

    @After
    fun limpar() = EfeitoInterpretador.restaurarBuscadorPadrao()

    private fun vant(id: String, nome: String, nivel: Int = 1) =
        VantagemSelecionada(definicaoId = id, nome = nome, nivel = nivel)

    // --- o bônus por nível ---

    @Test
    fun `Talento nivel 3 da mais 3 na pericia`() {
        EfeitoInterpretador.buscador = { id, _ ->
            if (id == "artifice") listOf(
                EfeitoDeclarado(tipo = "pericia", alvo = "Engenharia/NT", valor = 1, porNivel = true)
            ) else null
        }
        val p = Personagem(nome = "T", vantagens = listOf(vant("artifice", "Artífice", 3)))
        assertEquals(3, TraitRuleRegistry.getSkillBonus(p, "Engenharia/NT"))
    }

    @Test
    fun `⚠️ Talentos que se sobrepoem SOMAM`() {
        // Engenharia está em Artífice E em Habilidade Matemática; o livro deixa
        // claro que aí eles somam e podem passar de +4 -- "talentos que se
        // sobrepõem (e apenas eles)".
        //
        // São seis perícias nessa situação no catálogo real: Engenharia/NT,
        // Veterinária/NT, Naturalista, Análise de Mercado, Contabilidade e
        // Finanças.
        EfeitoInterpretador.buscador = { id, _ ->
            when (id) {
                "artifice", "habilidade_matematica" -> listOf(
                    EfeitoDeclarado(tipo = "pericia", alvo = "Engenharia/NT", valor = 1, porNivel = true)
                )
                else -> null
            }
        }
        val p = Personagem(
            nome = "T",
            vantagens = listOf(
                vant("artifice", "Artífice", 3),
                vant("habilidade_matematica", "Habilidade Matemática", 2)
            )
        )
        assertEquals("3 + 2, e passar de +4 aqui é permitido", 5,
            TraitRuleRegistry.getSkillBonus(p, "Engenharia/NT"))

        // ...e a notinha tem de nomear as DUAS origens, senão o jogador vê +5 e
        // não tem como conferir.
        val origens = TraitRuleRegistry.getSkillBonusOrigens(p, "Engenharia/NT")
        assertEquals(listOf("Artífice", "Habilidade Matemática"), origens.map { it.nomeDoTraco })
        assertEquals(listOf(3, 2), origens.map { it.valor })
    }

    @Test
    fun `o bonus de reacao do Talento e CONDICIONAL, nao entra sozinho`() {
        // O livro condiciona: só vale "se existir a chance de ela ficar
        // impressionada com sua aptidão (a critério do Mestre)". Aplicar sempre
        // seria errado -- e é por isso que vira caixinha.
        EfeitoInterpretador.buscador = { id, _ ->
            if (id == "curandeiro") listOf(
                EfeitoDeclarado(
                    tipo = "pericia", alvo = "reacao", valor = 1, porNivel = true,
                    condicao = "de ex-pacientes e pacientes atuais"
                )
            ) else null
        }
        val p = Personagem(nome = "T", vantagens = listOf(vant("curandeiro", "Curandeiro", 2)))

        assertEquals("nao pode entrar no valor base", 0,
            TraitRuleRegistry.getSkillBonus(p, "reacao"))

        val caixinhas = TraitRuleRegistry.getBonusCondicionais(p, "reacao")
        assertEquals(1, caixinhas.size)
        assertEquals(2, caixinhas.first().valor)
    }

    // --- o curinga ---

    @Test
    fun `o curinga aparece em QUALQUER pericia`() {
        // Toque Sensível vale em "qualquer tarefa que utiliza o tato" -- o livro
        // não dá lista, dá situação. Enumerar as 278 perícias do catálogo seria
        // absurdo e ficaria errado no dia seguinte.
        EfeitoInterpretador.buscador = { id, _ ->
            if (id == "toque_sensivel") listOf(
                EfeitoDeclarado(
                    tipo = "pericia", alvo = TraitRuleRegistry.CURINGA_PERICIA,
                    valor = 4, condicao = "se a tarefa for pelo tato"
                )
            ) else null
        }
        val p = Personagem(nome = "T", vantagens = listOf(vant("toque_sensivel", "Toque Sensível")))

        listOf("Revistar", "Perícia Forense/NT", "Escalada", "Uma Perícia Que Nem Existe").forEach {
            assertEquals("deveria oferecer em '$it'", 1,
                TraitRuleRegistry.getBonusCondicionais(p, it).size)
        }
        assertEquals(4, TraitRuleRegistry.getBonusCondicionais(p, "Revistar").first().valor)
    }

    @Test
    fun `⚠️ o curinga NAO vale para defesa nem para reacao`() {
        // O livro fala de "testes de habilidade". Um +1 indevido na Esquiva é o
        // tipo de erro que passa porque parece plausível -- e a Esquiva é rolada
        // toda hora.
        EfeitoInterpretador.buscador = { id, _ ->
            if (id == "venturoso") listOf(
                EfeitoDeclarado(
                    tipo = "pericia", alvo = TraitRuleRegistry.CURINGA_PERICIA,
                    valor = 1, condicao = "risco desnecessário"
                )
            ) else null
        }
        val p = Personagem(nome = "T", vantagens = listOf(vant("venturoso", "Venturoso")))

        listOf("esquiva", "apara", "aparar", "bloqueio", "reacao").forEach {
            assertTrue("o curinga escapou para '$it'",
                TraitRuleRegistry.getBonusCondicionais(p, it).isEmpty())
        }
        // ...mas continua valendo numa perícia comum.
        assertEquals(1, TraitRuleRegistry.getBonusCondicionais(p, "Escalada").size)
    }

    @Test
    fun `o curinga nunca entra no valor base da pericia`() {
        // Ele é condicional por natureza: quem decide se vale é o Mestre. Se
        // entrasse no NH direto, o bônus valeria sempre e em tudo.
        EfeitoInterpretador.buscador = { id, _ ->
            if (id == "versatil") listOf(
                EfeitoDeclarado(
                    tipo = "pericia", alvo = TraitRuleRegistry.CURINGA_PERICIA,
                    valor = 1, condicao = "criatividade"
                )
            ) else null
        }
        val p = Personagem(nome = "T", vantagens = listOf(vant("versatil", "Versátil")))
        assertEquals(0, TraitRuleRegistry.getSkillBonus(p, "Artista"))
        assertEquals(0, TraitRuleRegistry.getSkillBonus(p, TraitRuleRegistry.CURINGA_PERICIA))
    }

    @Test
    fun `sem a vantagem, nada aparece`() {
        val p = Personagem(nome = "T")
        assertTrue(TraitRuleRegistry.getBonusCondicionais(p, "Escalada").isEmpty())
        assertEquals(0, TraitRuleRegistry.getSkillBonus(p, "Engenharia/NT"))
    }
}
