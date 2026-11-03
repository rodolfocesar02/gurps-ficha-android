package com.gurps.ficha.domain.rules.traits

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.model.DesvantagemSelecionada
import com.gurps.ficha.model.Personagem
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * **TR-6 do plano dos traços da Mesa** (`mesa-virtual/planos/tracos/PLANO.md`): a Deficiência Física no catálogo.
 *
 * MB p.130: Perna Incapacitada (−10 pts) dá −3, Pernas Faltando (−20) e Nenhuma Perna ou Paraplégico (−30) dão −6 em
 * todos os testes que exigem as pernas, "o que inclui todas as perícias com armas de combate corpo a corpo e todas as
 * perícias de combate desarmado"; as de projétil não. Os mesmos números do `tracos-tr6.test.js` da Mesa.
 */
class TracosDoTr6Test {

    private data class TracoCru(val id: String = "", val efeitos: List<EfeitoDeclarado> = emptyList())

    /** Liga o interpretador no catálogo REAL das desvantagens: sem isto tudo dá 0 e o teste passa por não achar nada. */
    @Before
    fun ligarNoCatalogoReal() {
        val direto = File("src/main/assets/desvantagens.v2.json")
        val arquivo = if (direto.exists()) direto else File("app/src/main/assets/desvantagens.v2.json")
        val tipo = object : TypeToken<List<TracoCru>>() {}.type
        val porId = Gson().fromJson<List<TracoCru>>(arquivo.readText(Charsets.UTF_8), tipo).associateBy { it.id }
        EfeitoInterpretador.buscador = { id, _ -> porId[id]?.efeitos }
    }

    @After
    fun desligar() {
        EfeitoInterpretador.restaurarBuscadorPadrao()
    }

    private fun comDeficiencia(custo: Int) = Personagem(desvantagens = listOf(
        DesvantagemSelecionada(definicaoId = "deficiencia_fisica", nome = "Deficiência Física", custoEscolhido = custo)
    ))

    @Test
    fun `Deficiencia Fisica - menos 3 na Perna Incapacitada, menos 6 sem pernas, nas pericias de corpo a corpo e desarmadas`() {
        assertEquals(-3, TraitRuleRegistry.getSkillBonus(comDeficiencia(-10), "Briga"))
        assertEquals(-6, TraitRuleRegistry.getSkillBonus(comDeficiencia(-20), "Espadas Curtas"))
        assertEquals(-6, TraitRuleRegistry.getSkillBonus(comDeficiencia(-30), "Judô"))
        assertEquals(-3, TraitRuleRegistry.getSkillBonus(comDeficiencia(-10), "Acrobacia"))
        // As de projétil não são prejudicadas (MB p.130).
        assertEquals(0, TraitRuleRegistry.getSkillBonus(comDeficiencia(-10), "Arcos"))
        // Ficha antiga sem o custo escolhido: nenhum número calado.
        assertEquals(0, TraitRuleRegistry.getSkillBonus(comDeficiencia(0), "Briga"))
    }
}
