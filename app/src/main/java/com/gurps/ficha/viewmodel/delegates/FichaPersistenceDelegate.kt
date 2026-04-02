package com.gurps.ficha.viewmodel.delegates

import com.gurps.ficha.BuildConfig
import com.gurps.ficha.data.storage.FichaStorageRepository
import com.gurps.ficha.model.*
import kotlinx.coroutines.*

class FichaPersistenceDelegate(
    private val fichaStorage: FichaStorageRepository
) {

    suspend fun salvarFicha(nomeArquivo: String, personagem: Personagem): List<String> {
        fichaStorage.salvarFicha(nomeArquivo, personagem.toJson())
        return listarFichas()
    }

    suspend fun carregarFicha(nomeArquivo: String): Personagem? {
        val json = fichaStorage.carregarFicha(nomeArquivo)
        return if (json != null) Personagem.fromJson(json) else null
    }

    suspend fun excluirFicha(nomeArquivo: String): List<String> {
        fichaStorage.excluirFicha(nomeArquivo)
        return listarFichas()
    }

    suspend fun listarFichas(autoSaveRecuperacaoNome: String = "_autosave_recuperacao"): List<String> {
        return fichaStorage.listarFichas()
            .filterNot { it == autoSaveRecuperacaoNome }
    }

    suspend fun restaurarAutoSave(autoSaveRecuperacaoNome: String): Personagem? {
        val json = fichaStorage.carregarFicha(autoSaveRecuperacaoNome) ?: return null
        return try {
            Personagem.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    fun exportarJsonCompativel(personagem: Personagem): String {
        return personagem.toJson()
    }

    fun exportarJsonVersionado(personagem: Personagem): String {
        return PersonagemInterop.exportarJson(
            personagem = personagem,
            appVersion = BuildConfig.VERSION_NAME,
            uiVariant = BuildConfig.UI_VARIANT
        )
    }

    fun importarJson(json: String): Result<Personagem> {
        return try {
            val resultado = PersonagemInterop.importarJson(json)
            Result.success(resultado.personagem)
        } catch (e: UnsupportedOperationException) {
            Result.failure(Exception("Versão de arquivo não suportada por esta versão do app."))
        } catch (e: Exception) {
            Result.failure(Exception("Arquivo de ficha inválido ou corrompido."))
        }
    }
}
