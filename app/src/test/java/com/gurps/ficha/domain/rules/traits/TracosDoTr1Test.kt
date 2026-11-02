package com.gurps.ficha.domain.rules.traits

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.model.AtributoBase
import com.gurps.ficha.model.Dificuldade
import com.gurps.ficha.model.ModeloRacial
import com.gurps.ficha.model.PericiaSelecionada
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * **TR-1 do plano dos traços da Mesa** (`mesa-virtual/planos/tracos/PLANO.md`): quatro defeitos do catálogo, o
 * caminho inteiro (catálogo real → interpretador → NH da perícia).
 *
 * - **Memória Eidética/Fotográfica** (MB p.69): sem bônus em Leitura Dinâmica; o +5/+10 é condicional de IQ.
 * - **Visão 360°** (MB p.97): sem +5 na Perseguição de quem tem a vantagem.
 * - **Olfato Discriminatório** (MB p.74): +4 em Rastreamento, pessoal ou racial (o troll). O Paladar não rastreia.
 * - **Talento dos Pequeninos** (Cataclismo p.185): +1/nível em Arcos, Arremesso, Funda e Arma de Arremesso só com
 *   Dardo, Faca e Vara. É a pedra de toque do `efeitos-na-rolagem.test.js` da Mesa: os mesmos números dos dois lados.
 */
class TracosDoTr1Test {

    private data class TracoCru(val id: String = "", val efeitos: List<EfeitoDeclarado> = emptyList())

    /** Liga o interpretador no catálogo REAL: sem isto tudo dá 0 e o teste passa por não achar nada. */
    @Before
    fun ligarNoCatalogoReal() {
        val direto = File("src/main/assets/vantagens.v3.json")
        val arquivo = if (direto.exists()) direto else File("app/src/main/assets/vantagens.v3.json")
        val tipo = object : TypeToken<List<TracoCru>>() {}.type
        val porId = Gson().fromJson<List<TracoCru>>(arquivo.readText(Charsets.UTF_8), tipo).associateBy { it.id }
        EfeitoInterpretador.buscador = { id, _ -> porId[id]?.efeitos }
    }

    @After
    fun desligar() {
        EfeitoInterpretador.restaurarBuscadorPadrao()
    }

    private fun vant(id: String, nivel: Int = 1) = VantagemSelecionada(definicaoId = id, nome = id, nivel = nivel)

    private fun pericia(nome: String, dificuldade: Dificuldade, pontos: Int, especializacao: String = "") =
        PericiaSelecionada(nome = nome, atributoBase = AtributoBase.DX, dificuldade = dificuldade, pontosGastos = pontos,
            especializacao = especializacao)

    /** O Pequenino de teste: DX comprada 10 + racial 1 = 11, e o talento racial no nível pedido. */
    private fun pequenino(nivel: Int = 2) = Personagem(
        destreza = 10,
        modeloRacial = ModeloRacial(nome = "Pequenino", modDestreza = 1,
            vantagens = listOf(vant("talento_dos_pequeninos_para_armas_de_longa_distancia", nivel)))
    )

    @Test
    fun `Talento dos Pequeninos - Arcos, Faca e faca 13, Machado-Maca 11, Funda 11`() {
        val p = pequenino()
        assertEquals(13, pericia("Arcos", Dificuldade.MEDIA, 2).calcularNivel(p))
        assertEquals(13, pericia("Arma de Arremesso", Dificuldade.FACIL, 1, "Faca").calcularNivel(p))
        assertEquals(13, pericia("Arma de Arremesso", Dificuldade.FACIL, 1, "faca").calcularNivel(p))
        assertEquals(11, pericia("Arma de Arremesso", Dificuldade.FACIL, 1, "Machado/Maça").calcularNivel(p))
        assertEquals(11, pericia("Arma de Arremesso", Dificuldade.FACIL, 1).calcularNivel(p))
        assertEquals(11, pericia("Funda", Dificuldade.DIFICIL, 1).calcularNivel(p))
        assertEquals(13, pericia("Arremesso", Dificuldade.MEDIA, 1).calcularNivel(pequenino(3)))
    }

    @Test
    fun `Talento dos Pequeninos - a origem do bonus diz o talento so na especializacao certa`() {
        val p = pequenino()
        assertEquals(2, TraitRuleRegistry.getSkillBonusOrigens(p, "Arma de Arremesso", "Vara").single().valor)
        assertEquals(0, TraitRuleRegistry.getSkillBonusOrigens(p, "Arma de Arremesso", "Shuriken").size)
    }

    @Test
    fun `Olfato Discriminatorio da +4 em Rastreamento, pessoal ou racial, e o Paladar nao`() {
        assertEquals(4, TraitRuleRegistry.getSkillBonus(Personagem(vantagens = listOf(vant("oflato_discriminatorio"))), "Rastreamento"))
        val troll = Personagem(modeloRacial = ModeloRacial(vantagens = listOf(vant("oflato_discriminatorio"))))
        assertEquals(4, TraitRuleRegistry.getSkillBonus(troll, "Rastreamento"))
        assertEquals(0, TraitRuleRegistry.getSkillBonus(Personagem(vantagens = listOf(vant("paladar_discriminatorio"))), "Rastreamento"))
    }

    @Test
    fun `Visao 360 nao da mais +5 na Perseguicao de quem a tem`() {
        assertEquals(0, TraitRuleRegistry.getSkillBonus(Personagem(vantagens = listOf(vant("visao_360_graus"))), "Perseguição"))
    }

    @Test
    fun `as Memorias nao dao bonus em Leitura Dinamica nem mudam o IQ`() {
        for (id in listOf("memoria_eidetica", "memoria_fotografica")) {
            val p = Personagem(inteligencia = 12, vantagens = listOf(vant(id)))
            assertEquals(0, TraitRuleRegistry.getSkillBonus(p, "Leitura Dinâmica"))
            assertEquals(12, p.getAtributo("IQ"))
        }
    }
}
