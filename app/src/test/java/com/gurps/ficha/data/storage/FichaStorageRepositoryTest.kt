package com.gurps.ficha.data.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FichaStorageRepositoryTest {

    @Test
    fun `normaliza nome de arquivo no formato legado`() {
        assertEquals("Sem_Nome", FichaStorageRepository.normalizarNomeArquivoCompat("   "))
        assertEquals("Meu_Heroi", FichaStorageRepository.normalizarNomeArquivoCompat("Meu Heroi"))
    }

    @Test
    fun `extrai apenas fichas validas do shared preferences`() {
        val prefsMap = mapOf(
            "ficha_Guerreiro" to """{"nome":"Guerreiro"}""",
            "ficha_Mago" to """{"nome":"Mago"}""",
            "outra_chave" to "ignorar",
            "ficha_invalida" to 42
        )

        val fichas = FichaStorageRepository.extrairFichasDoPrefs(prefsMap, timestamp = 123L)

        assertEquals(2, fichas.size)
        assertTrue(fichas.any { it.nomeArquivo == "Guerreiro" && it.updatedAt == 123L })
        assertTrue(fichas.any { it.nomeArquivo == "Mago" && it.updatedAt == 123L })
    }
}
