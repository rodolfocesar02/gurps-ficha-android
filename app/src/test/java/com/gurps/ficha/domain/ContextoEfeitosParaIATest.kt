package com.gurps.ficha.domain

import com.gurps.ficha.domain.rules.traits.EfeitoDeclarado
import com.gurps.ficha.domain.rules.traits.EfeitoInterpretador
import com.gurps.ficha.model.DesvantagemSelecionada
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Cobre o resumo de efeitos enviado à IA (Lote IA-1).
 *
 * Antes disto o Narrador recebia só "Vantagens: Pendulear, Reflexos em Combate"
 * e tinha de adivinhar a mecânica pela prosa — ou inventar. Agora recebe o
 * número, e pode raciocinar sobre ele: pedir o teste certo, aplicar a
 * penalidade certa, provocar a situação da desvantagem.
 *
 * Provavelmente é o maior retorno dos efeitos declarativos, maior que o "+2" na
 * ficha.
 */
class ContextoEfeitosParaIATest {

    @After
    fun limpar() = EfeitoInterpretador.restaurarBuscadorPadrao()

    private fun comEfeitos(mapa: Map<String, List<EfeitoDeclarado>>) {
        EfeitoInterpretador.buscador = { id, _ -> mapa[id] }
    }

    @Test
    fun `traco com efeito aparece com o numero`() {
        comEfeitos(mapOf("pendulear" to listOf(
            EfeitoDeclarado(tipo = "pericia", alvo = "Escalada", valor = 2)
        )))
        val p = Personagem(
            nome = "Teste",
            vantagens = listOf(VantagemSelecionada(definicaoId = "pendulear", nome = "Pendulear"))
        )
        val texto = MestreIAContextFilter.resumoDeEfeitos(p)
        assertTrue(texto, texto.contains("- Pendulear: +2 Escalada"))
    }

    @Test
    fun `desvantagem entra com o sinal negativo`() {
        comEfeitos(mapOf("gordo" to listOf(
            EfeitoDeclarado(tipo = "pericia", alvo = "Disfarce/NT", valor = -2)
        )))
        val p = Personagem(
            nome = "Teste",
            desvantagens = listOf(DesvantagemSelecionada(definicaoId = "gordo", nome = "Gordo"))
        )
        assertTrue(MestreIAContextFilter.resumoDeEfeitos(p).contains("- Gordo: -2 Disfarce/NT"))
    }

    @Test
    fun `varios efeitos do mesmo traco ficam na mesma linha`() {
        comEfeitos(mapOf("reflexos" to listOf(
            EfeitoDeclarado(tipo = "defesa", alvo = "esquiva", valor = 1),
            EfeitoDeclarado(tipo = "defesa", alvo = "aparar", valor = 1)
        )))
        val p = Personagem(
            nome = "Teste",
            vantagens = listOf(VantagemSelecionada(definicaoId = "reflexos", nome = "Reflexos em Combate"))
        )
        val texto = MestreIAContextFilter.resumoDeEfeitos(p)
        assertEquals(2, texto.trim().lines().size)   // cabeçalho + 1 traço
        assertTrue(texto.contains("+1 esquiva, +1 aparar"))
    }

    @Test
    fun `porNivel multiplica pelo nivel da ficha`() {
        comEfeitos(mapOf("camaleao" to listOf(
            EfeitoDeclarado(tipo = "pericia", alvo = "Furtividade", valor = 2, porNivel = true)
        )))
        val p = Personagem(
            nome = "Teste",
            vantagens = listOf(VantagemSelecionada(definicaoId = "camaleao", nome = "Camaleão", nivel = 3))
        )
        assertTrue(MestreIAContextFilter.resumoDeEfeitos(p).contains("+6 Furtividade"))
    }

    @Test
    fun `bonus condicional e marcado, para a IA saber que NAO esta somado`() {
        // Este e o ponto mais importante: se a IA achar que o bonus condicional
        // ja esta no NH, vai calcular errado.
        comEfeitos(mapOf("rosto" to listOf(
            EfeitoDeclarado(
                tipo = "pericia", alvo = "Dissimulação", valor = 1,
                condicao = "para parecer honesto"
            )
        )))
        val p = Personagem(
            nome = "Teste",
            vantagens = listOf(VantagemSelecionada(definicaoId = "rosto", nome = "Rosto Sincero"))
        )
        val texto = MestreIAContextFilter.resumoDeEfeitos(p)
        assertTrue(texto, texto.contains("[só para parecer honesto]"))
    }

    @Test
    fun `traco sem efeito declarado nao polui o contexto`() {
        comEfeitos(emptyMap())
        val p = Personagem(
            nome = "Teste",
            vantagens = listOf(VantagemSelecionada(definicaoId = "riqueza", nome = "Riqueza"))
        )
        assertEquals("", MestreIAContextFilter.resumoDeEfeitos(p))
    }

    @Test
    fun `ficha sem tracos nao gera secao`() {
        comEfeitos(emptyMap())
        assertEquals("", MestreIAContextFilter.resumoDeEfeitos(Personagem(nome = "Vazio")))
    }

    @Test
    fun `o contexto completo inclui a secao de efeitos`() {
        comEfeitos(mapOf("pendulear" to listOf(
            EfeitoDeclarado(tipo = "pericia", alvo = "Escalada", valor = 2)
        )))
        val p = Personagem(
            nome = "Teste",
            vantagens = listOf(VantagemSelecionada(definicaoId = "pendulear", nome = "Pendulear"))
        )
        val contexto = MestreIAContextFilter.gerarContexto(p, modo = "conversa")
        assertTrue(contexto.contains("Efeitos mecânicos"))
        assertTrue(contexto.contains("+2 Escalada"))
        // E nao pode ter perdido o que ja mandava antes.
        assertTrue(contexto.contains("Vantagens: Pendulear"))
        assertFalse(contexto.isBlank())
    }
}
