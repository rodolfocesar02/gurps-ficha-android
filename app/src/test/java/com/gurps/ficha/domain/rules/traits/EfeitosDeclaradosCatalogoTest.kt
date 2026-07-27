package com.gurps.ficha.domain.rules.traits

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Valida os `efeitos` declarados no CATÁLOGO REAL, não em dado inventado.
 *
 * O [EfeitoInterpretadorTest] prova que o interpretador funciona; este prova que
 * o que foi escrito no JSON está certo. São coisas diferentes: um `alvo` com
 * erro de digitação passa por todo o interpretador sem erro e simplesmente
 * nunca aplica — falha invisível.
 *
 * Espelha o `scripts/validar_efeitos.py`, mas roda no gate de testes: quem
 * declarar efeito novo e errar o nome da perícia descobre no build, não no
 * aparelho.
 */
class EfeitosDeclaradosCatalogoTest {

    private val gson = Gson()

    /** Lê do módulo `app/` — é o diretório de trabalho do Gradle nos testes. */
    private fun asset(nome: String): File {
        val direto = File("src/main/assets/$nome")
        return if (direto.exists()) direto else File("app/src/main/assets/$nome")
    }

    private fun <T> lerLista(nome: String, tipo: java.lang.reflect.Type): List<T> {
        val arquivo = asset(nome)
        assertTrue("asset nao encontrado: ${arquivo.absolutePath}", arquivo.exists())
        return gson.fromJson(arquivo.readText(Charsets.UTF_8), tipo)
    }

    private data class TracoCru(
        val id: String = "",
        val nome: String = "",
        val efeitos: List<EfeitoDeclarado> = emptyList()
    )

    private data class PericiaCrua(val nome: String = "")

    private fun tracosComEfeitos(): List<TracoCru> {
        val tipo = object : TypeToken<List<TracoCru>>() {}.type
        return (lerLista<TracoCru>("vantagens.v3.json", tipo) +
                lerLista<TracoCru>("desvantagens.v2.json", tipo))
            .filter { it.efeitos.isNotEmpty() }
    }

    private fun nomesDePericia(): Set<String> {
        val tipo = object : TypeToken<List<PericiaCrua>>() {}.type
        return lerLista<PericiaCrua>("pericias.json", tipo)
            .mapNotNull { it.nome.takeIf { n -> n.isNotBlank() } }
            .toSet()
    }

    // --- as invariantes ---

    @Test
    fun `todo alvo de pericia existe com o nome EXATO no catalogo`() {
        // A armadilha: o catalogo usa "Navegação/NT" e "Perícia Forense/NT".
        // Declarar "Navegação" nao pega nada -- e nao gera erro nenhum.
        val pericias = nomesDePericia()
        val erros = mutableListOf<String>()
        tracosComEfeitos().forEach { traco ->
            traco.efeitos
                .filter { it.tipoResolvido == TipoEfeito.PERICIA }
                .forEach { efeito ->
                    if (efeito.alvo !in pericias) {
                        erros.add("${traco.nome} [${traco.id}] -> pericia '${efeito.alvo}' nao existe")
                    }
                }
        }
        assertTrue(erros.joinToString("\n"), erros.isEmpty())
    }

    @Test
    fun `todo efeito tem tipo reconhecido`() {
        val erros = tracosComEfeitos().flatMap { traco ->
            traco.efeitos.filter { it.tipoResolvido == null }
                .map { "${traco.nome} [${traco.id}] -> tipo '${it.tipo}' desconhecido" }
        }
        assertTrue(erros.joinToString("\n"), erros.isEmpty())
    }

    @Test
    fun `nenhum efeito tem alvo em branco`() {
        val erros = tracosComEfeitos().flatMap { traco ->
            traco.efeitos.filter { it.alvo.isBlank() }
                .map { "${traco.nome} [${traco.id}] -> efeito sem alvo" }
        }
        assertTrue(erros.joinToString("\n"), erros.isEmpty())
    }

    @Test
    fun `nenhum efeito tem valor zero`() {
        // Valor 0 nao muda nada: ou e engano de digitacao, ou o efeito nao
        // deveria estar declarado.
        val erros = tracosComEfeitos().flatMap { traco ->
            traco.efeitos.filter { it.valor == 0 }
                .map { "${traco.nome} [${traco.id}] -> '${it.alvo}' com valor 0" }
        }
        assertTrue(erros.joinToString("\n"), erros.isEmpty())
    }

    @Test
    fun `traco com efeitos declarados NAO pode ter regra Kotlin`() {
        // A Kotlin vence o JSON: se os dois existem, o JSON e ignorado em
        // silencio e quem declarou acha que automatizou.
        val erros = tracosComEfeitos()
            .filter { TraitRuleRegistry.hasSpecialRule(it.id) }
            .map { "${it.nome} [${it.id}] tem `efeitos` E regra Kotlin -- o JSON seria ignorado" }
        assertTrue(erros.joinToString("\n"), erros.isEmpty())
    }

    // --- prova de que o trilho entrega o bônus ---

    @Test
    fun `Pendulear declarado no catalogo devolve mais 2 em Escalada`() {
        val pendulear = tracosComEfeitos().firstOrNull { it.id == "pendulear" }
        assertTrue("pendulear deveria estar declarado no catalogo", pendulear != null)

        val regra = EfeitoInterpretador.regraDe("pendulear", pendulear!!.efeitos)
        val mods = regra.getSkillModifiers(
            Personagem(nome = "Teste"),
            VantagemSelecionada(definicaoId = "pendulear", nome = "Pendulear")
        )
        assertEquals(2, mods["Escalada"])
    }

    @Test
    fun `Voz Melodiosa entrega bonus em varias pericias sociais`() {
        val voz = tracosComEfeitos().firstOrNull { it.id == "voz_melodiosa" }
        assertTrue("voz_melodiosa deveria estar declarada", voz != null)

        val mods = EfeitoInterpretador.regraDe("voz_melodiosa", voz!!.efeitos)
            .getSkillModifiers(
                Personagem(nome = "Teste"),
                VantagemSelecionada(definicaoId = "voz_melodiosa", nome = "Voz Melodiosa")
            )
        listOf("Atuação", "Canto", "Diplomacia", "Lábia", "Oratória", "Política", "Sex Appeal")
            .forEach { assertEquals("faltou bonus em $it", 2, mods[it]) }
    }
}
