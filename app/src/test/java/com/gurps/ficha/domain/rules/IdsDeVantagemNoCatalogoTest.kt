package com.gurps.ficha.domain.rules

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Todo id de vantagem escrito em código existe no catálogo.
 *
 * **Por que este teste existe.** No Lote RESIST-1 eu escrevi
 * `ID_RESISTENCIA_MAGIA = "abascanto"`. O id real é
 * `abascanto_resistencia_a_magia`. Os onze testes daquele lote passaram —
 * porque **eles inventavam o id** ao montar o personagem de teste, em vez de ler
 * o catálogo. Cada pedaço verde, o conjunto quebrado: o mesmo perfil do bug do
 * Lote V-1.
 *
 * Um id errado não dá erro em lugar nenhum. Ele simplesmente nunca casa, e a
 * vantagem fica sem efeito para sempre. É a falha mais silenciosa desta base.
 *
 * Este teste fecha a porta: a lista abaixo é confrontada com
 * `vantagens.v3.json`. **Ao escrever uma regra nova que casa por id, acrescente
 * o id aqui.**
 */
class IdsDeVantagemNoCatalogoTest {

    private data class TracoCru(val id: String = "")

    private fun idsDoCatalogo(arquivo: String): Set<String> {
        val direto = File("src/main/assets/$arquivo")
        val f = if (direto.exists()) direto else File("app/src/main/assets/$arquivo")
        assertTrue("asset nao encontrado: ${f.absolutePath}", f.exists())
        val tipo = object : TypeToken<List<TracoCru>>() {}.type
        return Gson().fromJson<List<TracoCru>>(f.readText(Charsets.UTF_8), tipo)
            .map { it.id }.toSet()
    }

    @Test
    fun `todo id usado em regra Kotlin existe em vantagens v3`() {
        val ids = idsDoCatalogo("vantagens.v3.json")

        val usados = mapOf(
            // ResistenciaRules (RESIST-1)
            "abascanto_resistencia_a_magia" to "ResistenciaRules.ID_RESISTENCIA_MAGIA",
            "boa_forma" to "ResistenciaRules / MarcosDeVidaRules",
            "destemor" to "ResistenciaRules",
            "dificil_de_subjugar" to "ResistenciaRules / MarcosDeVidaRules",
            "duro_de_matar" to "ResistenciaRules / MarcosDeVidaRules",
            "aptidao_magica" to "ResistenciaRules / FichaTraitDelegate",
            // Braçais (STB-1, STB-2, DX-BRACAL)
            "st_bracal" to "StBracalRule / StBracalRules",
            "dx_bracal" to "DxBracalRule / DxBracalRules",
            // Mão inábil (MAO-1)
            "ambidestria" to "MaoInabilRules",
            // Deslocamento (regra antiga do Personagem)
            "deslocamento_aquatico" to "Personagem.bonusDeslocamentoAquatico"
        )

        val erros = usados.filterKeys { it !in ids }
            .map { (id, onde) -> "'$id' usado em $onde nao existe em vantagens.v3.json" }
        assertTrue(erros.joinToString("\n"), erros.isEmpty())
    }

    @Test
    fun `todo id usado em regra Kotlin existe em desvantagens v2`() {
        val ids = idsDoCatalogo("desvantagens.v2.json")
        val usados = mapOf(
            "habitos_detestaveis" to "declaracao porOpcao",
            "estigma_social" to "declaracao porOpcao",
            "aparencia" to "declaracao porOpcao (lado desvantagem)"
        )
        val erros = usados.filterKeys { it !in ids }
            .map { (id, onde) -> "'$id' usado em $onde nao existe em desvantagens.v2.json" }
        assertTrue(erros.joinToString("\n"), erros.isEmpty())
    }
}
