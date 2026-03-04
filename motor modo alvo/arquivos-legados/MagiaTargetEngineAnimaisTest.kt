package com.gurps.ficha.domain.magias

import com.gurps.ficha.model.MagiaDefinicao
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.regras_prerequisitos.PreRequisitoChecker
import com.gurps.ficha.regras_prerequisitos.PreRequisitoParser
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.Normalizer

class MagiaTargetEngineAnimaisTest {

    @Test
    fun `modo alvo para falar com animais ar segue cadeia do caminho ar`() {
        val repo = FakeMagiaRepo(catalogoBaseAnimais())
        val engine = MagiaTargetEngine(repo)
        val personagem = Personagem()
        val alvo = repo.magias.first { it.id == "falar_com_animais_ar" }

        val resultado = engine.calcularModoAlvo(alvo, personagem)

        assertTrue(resultado.ids.contains("falar_com_animais_ar"))
        assertTrue(resultado.ids.contains("convocar_animal_ar"))
        assertTrue(resultado.ids.contains("controle_de_animal_ar"))
        assertTrue(resultado.ids.contains("acalmar_animal"))
        assertFalse(resultado.ids.contains("controle_de_animal_terra"))
        assertFalse(resultado.ids.contains("controle_de_animal_mar"))
    }

    @Test
    fun `modo alvo para dominar animal terra nao vaza para outros caminhos`() {
        val repo = FakeMagiaRepo(catalogoBaseAnimais())
        val engine = MagiaTargetEngine(repo)
        val personagem = Personagem()
        val alvo = repo.magias.first { it.id == "dominar_animal_terra" }

        val resultado = engine.calcularModoAlvo(alvo, personagem)

        assertTrue(resultado.ids.contains("dominar_animal_terra"))
        assertTrue(resultado.ids.contains("controle_de_animal_terra"))
        assertTrue(resultado.ids.contains("acalmar_animal"))
        assertFalse(resultado.ids.contains("controle_de_animal_ar"))
        assertFalse(resultado.ids.contains("controle_de_animal_mar"))
    }

    @Test
    fun `modo alvo para convocar animal mar usa controle do mesmo caminho`() {
        val repo = FakeMagiaRepo(catalogoBaseAnimais())
        val engine = MagiaTargetEngine(repo)
        val personagem = Personagem()
        val alvo = repo.magias.first { it.id == "convocar_animal_mar" }

        val resultado = engine.calcularModoAlvo(alvo, personagem)

        assertTrue(resultado.ids.contains("convocar_animal_mar"))
        assertTrue(resultado.ids.contains("controle_de_animal_mar"))
        assertTrue(resultado.ids.contains("acalmar_animal"))
        assertFalse(resultado.ids.contains("controle_de_animal_ar"))
        assertFalse(resultado.ids.contains("controle_de_animal_terra"))
    }

    private fun catalogoBaseAnimais(): List<MagiaDefinicao> {
        fun spell(id: String, nome: String, pre: String): MagiaDefinicao {
            return MagiaDefinicao(
                id = id,
                nome = nome,
                dificuldadeFixa = "D",
                pagina = 30,
                texto = "",
                classe = "Comum",
                escola = listOf("Animais"),
                duracao = "1 min.",
                energia = "1",
                tempoOperacao = "1 seg.",
                preRequisitos = pre
            )
        }

        return listOf(
            spell("acalmar_animal", "Acalmar Animal", "-"),
            spell("controle_de_animal_terra", "Controle de Animal (Criaturas da Terra)", "Acalmar Animal"),
            spell("controle_de_animal_ar", "Controle de Animal (Criaturas do Ar)", "Acalmar Animal"),
            spell("controle_de_animal_mar", "Controle de Animal (Criaturas do Mar)", "Acalmar Animal"),
            spell("convocar_animal_terra", "Convocar Animal (Criaturas da Terra)", "Controle de Animal (Criaturas da Terra)"),
            spell("convocar_animal_ar", "Convocar Animal (Criaturas do Ar)", "Controle de Animal (Criaturas do Ar)"),
            spell("convocar_animal_mar", "Convocar Animal (Criaturas do Mar)", "Controle de Animal (Criaturas do Mar)"),
            spell("dominar_animal_terra", "Dominar Animal (Criaturas da Terra)", "Controle de Animal (Criaturas da Terra)"),
            spell("dominar_animal_ar", "Dominar Animal (Criaturas do Ar)", "Controle de Animal (Criaturas do Ar)"),
            spell("dominar_animal_mar", "Dominar Animal (Criaturas do Mar)", "Controle de Animal (Criaturas do Mar)"),
            spell("falar_com_animais_terra", "Falar com Animais (Criaturas da Terra)", "Convocar Animal (Criaturas da Terra)"),
            spell("falar_com_animais_ar", "Falar com Animais (Criaturas do Ar)", "Convocar Animal (Criaturas do Ar)"),
            spell("falar_com_animais_mar", "Falar com Animais (Criaturas do Mar)", "Convocar Animal (Criaturas do Mar)")
        )
    }
}

private class FakeMagiaRepo(
    override val magias: List<MagiaDefinicao>
) : MagiaPlannerDataSource {

    override fun validarPreRequisitosMagia(definicao: MagiaDefinicao, personagem: Personagem): String? {
        val raw = preRequisitoNormalizadoParaAnalise(definicao)
        if (magiaSemPreRequisito(definicao)) return null
        val parsed = PreRequisitoParser.parse(raw)
        val report = PreRequisitoChecker.checkParseResult(buildCtx(personagem), parsed)
        return if (report.startsWith("faltando")) raw else null
    }

    override fun preRequisitoNormalizadoParaAnalise(definicao: MagiaDefinicao): String {
        return definicao.preRequisitos.orEmpty().trim()
    }

    override fun magiaSemPreRequisito(definicao: MagiaDefinicao): Boolean {
        val raw = preRequisitoNormalizadoParaAnalise(definicao)
        if (raw.isBlank()) return true
        return raw in setOf("-", "—", "–", "−", "?", "??", "???")
    }

    private fun buildCtx(personagem: Personagem): Map<String, Any> {
        val magiasConhecidasNormalizadas = personagem.magias
            .map { normalizar(it.nome) }
            .filter { it.isNotBlank() }
            .toSet()
        val magiasPorEscola = mutableMapOf<String, Int>()
        personagem.magias.forEach { magia ->
            magia.escola.orEmpty().forEach { escola ->
                val key = normalizar(escola)
                magiasPorEscola[key] = (magiasPorEscola[key] ?: 0) + 1
            }
        }
        return mapOf(
            "aptidao_magica" to 0,
            "magias_conhecidas_normalizadas" to magiasConhecidasNormalizadas,
            "magias_por_escola_normalizada" to magiasPorEscola,
            "escolas_conhecidas_normalizadas" to magiasPorEscola.keys.toSet(),
            "escolas_por_magia_normalizadas" to emptyMap<String, Set<String>>(),
            "vantagens_conhecidas_normalizadas" to emptySet<String>(),
            "pericias_conhecidas_normalizadas" to emptySet<String>(),
            "condicoes_estado_normalizadas" to emptySet<String>()
        )
    }

    private fun normalizar(valor: String): String {
        val semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return semAcento
            .lowercase()
            .replace("-", " ")
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}

