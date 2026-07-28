package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.traits.EfeitoDeclarado
import com.gurps.ficha.domain.rules.traits.EfeitoInterpretador
import com.gurps.ficha.model.DefesasAtivas
import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.TipoEquipamento
import com.gurps.ficha.model.VantagemSelecionada
import com.gurps.ficha.viewmodel.DefenseType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Composição dos números de defesa (Lote NOTA-2).
 *
 * O app mostra **8** e o jogador não tem como conferir se 8 está certo. Estas
 * invariantes garantem que a explicação nomeia cada parcela — escudo, vantagem
 * e o que foi digitado à mão — e que não inventa parcela nenhuma.
 */
class OrigemDosNumerosTest {

    @After
    fun limpar() = EfeitoInterpretador.restaurarBuscadorPadrao()

    private fun comEfeitos(mapa: Map<String, List<EfeitoDeclarado>>) {
        EfeitoInterpretador.buscador = { id -> mapa[id] }
    }

    private fun comEscudo(bd: Int, nome: String = "Escudo Grande") = Personagem(
        nome = "Teste",
        equipamentos = listOf(
            Equipamento(nome = nome, tipo = TipoEquipamento.ESCUDO, bonusDefesa = bd)
        ),
        defesasAtivas = DefesasAtivas(escudoSelecionadoNome = nome)
    )

    @Test
    fun `defesa so com a base nao gera explicacao nenhuma`() {
        comEfeitos(emptyMap())
        val p = Personagem(nome = "Teste")
        DefenseType.values().forEach { tipo ->
            assertTrue("$tipo nao deveria ter origem", OrigemDosNumeros.daDefesa(p, tipo).isEmpty())
        }
    }

    @Test
    fun `o escudo aparece pelo NOME, nao como Escudo generico`() {
        // Quem tem dois escudos precisa saber QUAL esta contando.
        comEfeitos(emptyMap())
        val origens = OrigemDosNumeros.daDefesa(comEscudo(2), DefenseType.ESQUIVA)
        assertEquals(1, origens.size)
        assertEquals("Escudo Grande", origens.first().nomeDoTraco)
        assertEquals(2, origens.first().valor)
    }

    @Test
    fun `escudo NAO selecionado nao entra na conta`() {
        // MB p.375: o BD so vale com o escudo pronto. No app, "pronto" e ter
        // sido escolhido na defesa de Bloqueio.
        comEfeitos(emptyMap())
        val p = Personagem(
            nome = "Teste",
            equipamentos = listOf(
                Equipamento(nome = "Broquel", tipo = TipoEquipamento.ESCUDO, bonusDefesa = 1)
            )
        )
        assertTrue(OrigemDosNumeros.daDefesa(p, DefenseType.ESQUIVA).isEmpty())
    }

    @Test
    fun `a vantagem aparece com o nome dela`() {
        comEfeitos(mapOf("reflexos" to listOf(
            EfeitoDeclarado(tipo = "defesa", alvo = "esquiva", valor = 1)
        )))
        val p = Personagem(
            nome = "Teste",
            vantagens = listOf(
                VantagemSelecionada(definicaoId = "reflexos", nome = "Reflexos em Combate")
            )
        )
        val origens = OrigemDosNumeros.daDefesa(p, DefenseType.ESQUIVA)
        assertEquals(listOf("Reflexos em Combate"), origens.map { it.nomeDoTraco })
    }

    @Test
    fun `o bonus manual entra com a NOTA que o jogador escreveu`() {
        // Era o ponto do Lote M-1: sem a nota, o numero e um misterio ate para
        // quem digitou, meses depois.
        comEfeitos(emptyMap())
        val p = Personagem(
            nome = "Teste",
            defesasAtivas = DefesasAtivas(
                bonusManualEsquiva = 2,
                notaBonusManualEsquiva = "poção do Mestre"
            )
        )
        val origens = OrigemDosNumeros.daDefesa(p, DefenseType.ESQUIVA)
        assertEquals("poção do Mestre", origens.first().nomeDoTraco)
        assertEquals(2, origens.first().valor)
    }

    @Test
    fun `bonus manual SEM nota ainda aparece, so que generico`() {
        comEfeitos(emptyMap())
        val p = Personagem(
            nome = "Teste",
            defesasAtivas = DefesasAtivas(bonusManualEsquiva = -1)
        )
        val origens = OrigemDosNumeros.daDefesa(p, DefenseType.ESQUIVA)
        assertEquals("Manual", origens.first().nomeDoTraco)
        assertEquals(-1, origens.first().valor)
    }

    @Test
    fun `escudo, vantagem e manual convivem na mesma explicacao`() {
        comEfeitos(mapOf("reflexos" to listOf(
            EfeitoDeclarado(tipo = "defesa", alvo = "esquiva", valor = 1)
        )))
        val p = comEscudo(2).copy(
            vantagens = listOf(
                VantagemSelecionada(definicaoId = "reflexos", nome = "Reflexos em Combate")
            ),
            defesasAtivas = DefesasAtivas(
                escudoSelecionadoNome = "Escudo Grande",
                bonusManualEsquiva = 1,
                notaBonusManualEsquiva = "bênção"
            )
        )
        val origens = OrigemDosNumeros.daDefesa(p, DefenseType.ESQUIVA)
        assertEquals(
            listOf("Escudo Grande", "Reflexos em Combate", "bênção"),
            origens.map { it.nomeDoTraco }
        )
        assertEquals(4, origens.sumOf { it.valor })
    }

    @Test
    fun `bonus de esquiva nao vaza para bloqueio`() {
        comEfeitos(mapOf("esq" to listOf(
            EfeitoDeclarado(tipo = "defesa", alvo = "esquiva", valor = 3)
        )))
        val p = Personagem(
            nome = "Teste",
            vantagens = listOf(VantagemSelecionada(definicaoId = "esq", nome = "Esquiva Ampliada"))
        )
        assertEquals(1, OrigemDosNumeros.daDefesa(p, DefenseType.ESQUIVA).size)
        assertTrue(OrigemDosNumeros.daDefesa(p, DefenseType.BLOQUEIO).isEmpty())
    }

    @Test
    fun `a soma da explicacao bate com o que o app somou de fato`() {
        // A invariante que realmente importa: se a notinha disser +3 e o card
        // mostrar +4, a explicacao vira mentira -- pior que nao ter explicacao.
        comEfeitos(mapOf("reflexos" to listOf(
            EfeitoDeclarado(tipo = "defesa", alvo = "esquiva", valor = 1)
        )))
        val base = Personagem(nome = "Teste")
        val comTudo = comEscudo(2).copy(
            vantagens = listOf(
                VantagemSelecionada(definicaoId = "reflexos", nome = "Reflexos em Combate")
            ),
            defesasAtivas = DefesasAtivas(
                escudoSelecionadoNome = "Escudo Grande",
                bonusManualEsquiva = 1
            )
        )
        val diferencaReal = comTudo.defesasAtivas.calcularEsquiva(comTudo) -
            base.defesasAtivas.calcularEsquiva(base)
        val somaExplicada = OrigemDosNumeros.daDefesa(comTudo, DefenseType.ESQUIVA).sumOf { it.valor }
        assertEquals(diferencaReal, somaExplicada)
    }
}
