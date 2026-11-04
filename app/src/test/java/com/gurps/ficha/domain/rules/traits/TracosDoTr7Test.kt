package com.gurps.ficha.domain.rules.traits

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * **TR-7 do plano dos traços da Mesa** (`mesa-virtual/planos/tracos/PLANO.md`): a Ultravisão no catálogo.
 *
 * MB p.96: o +2 em Observação, Perícia Forense e Revistar vale "sob luz UV". O catálogo dava o +2 sempre, no NH;
 * agora é bônus condicional (a caixinha da aba Rolagem). Os mesmos números do `tracos-tr7.test.js` da Mesa.
 */
class TracosDoTr7Test {

    private data class TracoCru(val id: String = "", val efeitos: List<EfeitoDeclarado> = emptyList())

    /** Liga o interpretador no catálogo REAL das vantagens: sem isto tudo dá 0 e o teste passa por não achar nada. */
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

    private val comUltravisao = Personagem(vantagens = listOf(
        VantagemSelecionada(definicaoId = "ultravisao", nome = "Ultravisão", custoEscolhido = 10)
    ))

    @Test
    fun `Ultravisao - o mais 2 sai do NH e vira condicional sob luz UV`() {
        listOf("Observação", "Perícia Forense/NT", "Revistar").forEach { pericia ->
            assertEquals("$pericia: nada no NH sem a luz UV", 0, TraitRuleRegistry.getSkillBonus(comUltravisao, pericia))
            val cond = TraitRuleRegistry.getBonusCondicionais(comUltravisao, pericia)
            assertTrue("$pericia: a caixinha da luz UV", cond.any { it.valor == 2 && it.condicao.contains("sob luz UV") })
        }
    }
}
