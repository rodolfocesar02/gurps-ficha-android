package com.gurps.ficha.data.storage

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FichaStorageRepository private constructor(
    private val context: Context,
    private val dao: FichaDao
) {
    suspend fun migrarDeSharedPreferencesSeNecessario() = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_MIGRACAO_CONCLUIDA, false)) return@withContext

        val entries = extrairFichasDoPrefs(prefs.all, System.currentTimeMillis())

        entries.forEach { dao.upsert(it) }
        prefs.edit().putBoolean(KEY_MIGRACAO_CONCLUIDA, true).apply()
    }

    suspend fun salvarFicha(nomeArquivo: String, json: String) = withContext(Dispatchers.IO) {
        dao.upsert(
            FichaEntity(
                nomeArquivo = Companion.normalizarNomeArquivoCompat(nomeArquivo),
                json = json,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun carregarFicha(nomeArquivo: String): String? = withContext(Dispatchers.IO) {
        dao.getJson(Companion.normalizarNomeArquivoCompat(nomeArquivo))
    }

    suspend fun excluirFicha(nomeArquivo: String) = withContext(Dispatchers.IO) {
        dao.deleteByName(Companion.normalizarNomeArquivoCompat(nomeArquivo))
    }

    suspend fun listarFichas(): List<String> = withContext(Dispatchers.IO) { dao.listNames() }

    companion object {
        private const val PREFS_NAME = "gurps_fichas"
        private const val FICHA_PREFIX = "ficha_"
        private const val KEY_MIGRACAO_CONCLUIDA = "room_migracao_v1_concluida"

        internal fun normalizarNomeArquivoCompat(nomeArquivo: String): String {
            val trimmed = nomeArquivo.trim()
            val base = if (trimmed.isBlank()) "Sem_Nome" else trimmed
            return base.replace(" ", "_")
        }

        internal fun extrairFichasDoPrefs(
            prefsEntries: Map<String, *>,
            timestamp: Long
        ): List<FichaEntity> {
            return prefsEntries
                .filterKeys { it.startsWith(FICHA_PREFIX) }
                .mapNotNull { (key, value) ->
                    val json = value as? String ?: return@mapNotNull null
                    val nomeArquivo = key.removePrefix(FICHA_PREFIX)
                    FichaEntity(
                        nomeArquivo = nomeArquivo,
                        json = json,
                        updatedAt = timestamp
                    )
                }
        }

        @Volatile
        private var INSTANCE: FichaStorageRepository? = null

        fun getInstance(context: Context): FichaStorageRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FichaStorageRepository(
                    context = context.applicationContext,
                    dao = FichaDatabase.getInstance(context).fichaDao()
                ).also { INSTANCE = it }
            }
        }
    }
}
