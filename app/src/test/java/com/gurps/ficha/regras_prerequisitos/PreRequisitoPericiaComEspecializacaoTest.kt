package com.gurps.ficha.regras_prerequisitos

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pré-requisito que cita perícia **com especialização** (bug de 28/07).
 *
 * Reportado pelo usuário: *"a perícia Engenharia está pedindo uma Vantagem
 * Matemática Aplicada, sendo que é a perícia Matemática com a especialização
 * Aplicada"*.
 *
 * ## Eram dois defeitos, e o segundo é o pior
 *
 * **1. O rótulo mentia.** Ele decidia entre "Perícia" e "Vantagem" olhando o que
 * o personagem **já tinha** na ficha, não o que a coisa **é**. Sem Matemática
 * comprada, o app dizia "Vantagem: Matemática (Aplicada)".
 *
 * **2. A validação nunca casava.** Duas convenções se cruzando: o catálogo chama
 * a perícia de `Matemática/NT` (o `/NT` marca dependência de Nível Tecnológico) e
 * o livro escreve `Matemática (Aplicada)` (o parêntese é a especialização).
 * Normalizados viram `matematica nt` e `matematica aplicada` — nenhum contém o
 * outro. **Mesmo com a perícia na ficha, o pré-requisito continuava faltando.**
 *
 * Três perícias do catálogo dependem disso: Astronomia/NT, Engenharia/NT e
 * Física/NT.
 */
class PreRequisitoPericiaComEspecializacaoTest {

    // --- o núcleo do nome, que é a peça nova ---

    @Test
    fun `o nucleo ignora o sufixo de Nivel Tecnologico`() {
        assertEquals("matematica", PreRequisitoChecker.nucleoDoNome("Matemática/NT"))
        assertEquals("cirurgia", PreRequisitoChecker.nucleoDoNome("Cirurgia/NT"))
    }

    @Test
    fun `o nucleo ignora a especializacao entre parenteses`() {
        assertEquals("matematica", PreRequisitoChecker.nucleoDoNome("Matemática (Aplicada)"))
        assertEquals("armas de fogo", PreRequisitoChecker.nucleoDoNome("Armas de Fogo (Pistola)"))
    }

    @Test
    fun `as duas convencoes chegam ao MESMO nucleo`() {
        // E o coracao do conserto: o catalogo e o livro escrevem diferente, mas
        // falam da mesma pericia.
        assertEquals(
            PreRequisitoChecker.nucleoDoNome("Matemática/NT"),
            PreRequisitoChecker.nucleoDoNome("Matemática (Aplicada)")
        )
    }

    @Test
    fun `nome sem sufixo nem parenteses passa intacto`() {
        assertEquals("acrobacia", PreRequisitoChecker.nucleoDoNome("Acrobacia"))
    }

    // --- a validação, com o caso real da Engenharia ---

    private fun fichaCom(vararg periciasDaFicha: String): Map<String, Any> = mapOf(
        "pericias_conhecidas_normalizadas" to periciasDaFicha
            .map { PreRequisitoChecker.normalizar(it) }.toSet(),
        "vantagens_conhecidas_normalizadas" to emptySet<String>(),
        "magias_conhecidas_normalizadas" to emptySet<String>(),
        "pericias_catalogo_nucleos" to setOf("matematica", "acrobacia", "cirurgia")
    )

    private fun checar(ficha: Map<String, Any>, requisito: String): String {
        return PreRequisitoChecker.checkParseResult(ficha, PreRequisitoParser.parse(requisito))
    }

    @Test
    fun `ter Matematica NT satisfaz o requisito de Matematica Aplicada`() {
        // O bug: antes, "matematica nt" nao casava com "matematica aplicada" e a
        // Engenharia ficava bloqueada mesmo com a pericia comprada.
        val resultado = checar(fichaCom("Matemática/NT"), "Matemática (Aplicada)")
        assertTrue("deveria estar atendido, veio: $resultado", !resultado.contains("faltando"))
    }

    @Test
    fun `sem Matematica o requisito continua faltando`() {
        // A correcao nao pode virar "aceita tudo".
        val resultado = checar(fichaCom("Acrobacia"), "Matemática (Aplicada)")
        assertTrue("deveria faltar, veio: $resultado", resultado.contains("faltando"))
    }

    @Test
    fun `pericia de nome parecido NAO satisfaz`() {
        // "Cirurgia" nao pode satisfazer um pedido de "Matematica".
        val resultado = checar(fichaCom("Cirurgia/NT"), "Matemática (Aplicada)")
        assertTrue(resultado.contains("faltando"))
    }

    // --- o rótulo ---

    @Test
    fun `o rotulo diz PERICIA mesmo sem ter a pericia na ficha`() {
        // Era o sintoma reportado: aparecia "Vantagem: Matemática (Aplicada)".
        val status = PreRequisitoChecker.checkDetailed(
            fichaCom(),                       // ficha VAZIA
            PreRequisitoParser.parse("Matemática (Aplicada)")
        )
        assertEquals(1, status.size)
        assertTrue(
            "rotulo veio: ${status.first().label}",
            status.first().label.startsWith("Perícia:")
        )
    }

    @Test
    fun `o que NAO esta no catalogo de pericias continua sendo Vantagem`() {
        val status = PreRequisitoChecker.checkDetailed(
            fichaCom(),
            PreRequisitoParser.parse("Visão Aguçada")
        )
        assertTrue(
            "rotulo veio: ${status.first().label}",
            status.first().label.startsWith("Vantagem:")
        )
    }

    @Test
    fun `sem o catalogo no contexto nada quebra`() {
        // Chamadas antigas nao passam `pericias_catalogo_nucleos`. O checker tem
        // que voltar ao palpite anterior, nao estourar.
        val semCatalogo = mapOf<String, Any>(
            "pericias_conhecidas_normalizadas" to setOf("matematica nt"),
            "vantagens_conhecidas_normalizadas" to emptySet<String>()
        )
        val status = PreRequisitoChecker.checkDetailed(
            semCatalogo, PreRequisitoParser.parse("Matemática (Aplicada)")
        )
        assertEquals(1, status.size)
        assertNull(null)   // o que importa e nao lancar excecao
    }
}
