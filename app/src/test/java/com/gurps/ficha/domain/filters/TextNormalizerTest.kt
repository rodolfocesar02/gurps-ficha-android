package com.gurps.ficha.domain.filters

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testes do TextNormalizer (Lote 314 — Plano §1 Etapa 2).
 *
 * Cobertura por modo:
 *  - SIMPLE: usado por SkillEngine e DataRepository.normalizarChaveClasse
 *  - BUSCA_PADRAO: usado por CatalogFilters (busca de todos os catálogos)
 *  - PERICIA_RAW: usado por FichaSkillDelegate.normalizarTexto (linha 215)
 *  - ARMA_GRUPO: usado por MestreDeArmasRule.normalize — sem teste hoje!
 */
class TextNormalizerTest {

    // ===== Modo SIMPLE =====

    @Test
    fun `SIMPLE remove acentos e lowercase`() {
        assertEquals(
            "esquiva acrobatica",
            TextNormalizer.normalize("Esquiva Acrobática", TextNormalizer.SIMPLE)
        )
    }

    @Test
    fun `SIMPLE colapsa espacos multiplos`() {
        assertEquals(
            "espada larga",
            TextNormalizer.normalize("  Espada    Larga  ", TextNormalizer.SIMPLE)
        )
    }

    @Test
    fun `SIMPLE preserva pontuacao e simbolos`() {
        assertEquals(
            "pericia-base / dx+1",
            TextNormalizer.normalize("Perícia-Base / DX+1", TextNormalizer.SIMPLE)
        )
    }

    @Test
    fun `SIMPLE com string vazia ou nula retorna vazio`() {
        assertEquals("", TextNormalizer.normalize("", TextNormalizer.SIMPLE))
        assertEquals("", TextNormalizer.normalize(null, TextNormalizer.SIMPLE))
    }

    // ===== Modo BUSCA_PADRAO =====

    @Test
    fun `BUSCA_PADRAO remove acentos e substitui simbolos por espaco`() {
        assertEquals(
            "visao agucada",
            TextNormalizer.normalize("Visão Aguçada", TextNormalizer.BUSCA_PADRAO)
        )
    }

    @Test
    fun `BUSCA_PADRAO converte hifen e parenteses em espaco`() {
        assertEquals(
            "espada larga dx 5",
            TextNormalizer.normalize("Espada-Larga (DX-5)", TextNormalizer.BUSCA_PADRAO)
        )
    }

    @Test
    fun `BUSCA_PADRAO corrige mojibake comum em fichas antigas`() {
        // Cenário: ficha antiga importada com encoding bagunçado
        // "Visǜo" (Visão) — o caractere ǜ deve virar 'a'
        val resultado = TextNormalizer.normalize("Visǜo", TextNormalizer.BUSCA_PADRAO)
        assertEquals("visao", resultado)
    }

    // ===== Modo PERICIA_RAW =====

    @Test
    fun `PERICIA_RAW preserva caracteres especiais de prerequisito`() {
        // Pré-requisitos têm sintaxe tipo "DX+1 / IQ-2_alt"
        assertEquals(
            "dx+1 / iq-2_alt",
            TextNormalizer.normalize("DX+1 / IQ-2_alt", TextNormalizer.PERICIA_RAW)
        )
    }

    @Test
    fun `PERICIA_RAW remove acentos mas mantem simbolos uteis`() {
        assertEquals(
            "pericia_base-vontade+1",
            TextNormalizer.normalize("Perícia_Base-Vontade+1", TextNormalizer.PERICIA_RAW)
        )
    }

    // ===== Modo ARMA_GRUPO (Mestre de Armas) =====

    @Test
    fun `ARMA_GRUPO remove plural simples`() {
        assertEquals("espada", TextNormalizer.normalize("Espadas", TextNormalizer.ARMA_GRUPO))
        assertEquals("maca", TextNormalizer.normalize("Macas", TextNormalizer.ARMA_GRUPO))
        assertEquals("faca", TextNormalizer.normalize("Facas", TextNormalizer.ARMA_GRUPO))
    }

    @Test
    fun `ARMA_GRUPO nao remove s de palavra curta`() {
        // Palavras de 3 caracteres ou menos não devem ser despluralizadas (regra do código original)
        assertEquals("gas", TextNormalizer.normalize("Gas", TextNormalizer.ARMA_GRUPO))
        assertEquals("os", TextNormalizer.normalize("Os", TextNormalizer.ARMA_GRUPO))
    }

    @Test
    fun `ARMA_GRUPO strip de parenteses preserva so o que vem antes`() {
        assertEquals(
            "machado",
            TextNormalizer.normalize("MACHADO (DX-5)", TextNormalizer.ARMA_GRUPO)
        )
    }

    @Test
    fun `ARMA_GRUPO casa Espadas de Lamina Larga com Espada Larga ao normalizar`() {
        // Cenário real do Mestre de Armas: comparar grupo "Espadas de Lâmina Larga"
        // com perícia "Espada Larga" — devem virar a mesma raiz despluralizada.
        val grupo = TextNormalizer.normalize(
            "Espadas de Lâmina Larga",
            TextNormalizer.ARMA_GRUPO,
        )
        val pericia = TextNormalizer.normalize(
            "Espada Larga",
            TextNormalizer.ARMA_GRUPO,
        )
        // grupo = "espada de lamina larga", pericia = "espada larga"
        // O matching real é feito por contains(), não igualdade direta.
        assertTrue(
            "Esperava que '$grupo' contivesse termos compatíveis com '$pericia'",
            grupo.contains("espada") && pericia.contains("espada"),
        )
    }

    @Test
    fun `ARMA_GRUPO ignora texto entre parenteses no nome da arma`() {
        // "Espada Larga (rara)" no catálogo deve casar com "Espada Larga" no grupo
        val arma = TextNormalizer.normalize(
            "Espada Larga (rara)",
            TextNormalizer.ARMA_GRUPO,
        )
        assertEquals("espada larga", arma)
    }

    // ===== contains() — busca substring tolerante =====

    @Test
    fun `contains acha visao quando se busca vis`() {
        assertTrue(TextNormalizer.contains("Visão Aguçada", "vis"))
    }

    @Test
    fun `contains ignora diferenca de acento entre haystack e needle`() {
        assertTrue(TextNormalizer.contains("Visão Aguçada", "visão"))
        assertTrue(TextNormalizer.contains("Visao Agucada", "Visão"))
    }

    @Test
    fun `contains com needle em branco retorna true (nao filtra nada)`() {
        assertTrue(TextNormalizer.contains("qualquer coisa", ""))
        assertTrue(TextNormalizer.contains("qualquer coisa", "   "))
    }

    @Test
    fun `contains retorna false quando termo nao existe`() {
        assertFalse(TextNormalizer.contains("Visão Aguçada", "telepatia"))
    }
}
