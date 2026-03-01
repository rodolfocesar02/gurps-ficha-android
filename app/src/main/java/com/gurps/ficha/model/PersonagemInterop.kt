package com.gurps.ficha.model

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.time.Instant

data class PersonagemImportMetadata(
    val schema: String,
    val schemaVersion: Int,
    val exportedAtUtc: String?
)

data class PersonagemImportResult(
    val personagem: Personagem,
    val metadata: PersonagemImportMetadata?,
    val aviso: String? = null
)

data class PersonagemInteropEnvelope(
    val schema: String,
    val schemaVersion: Int,
    val exportedAtUtc: String,
    val appVersion: String?,
    val uiVariant: String?,
    val character: Personagem
)

object PersonagemInterop {
    const val SCHEMA = "gurps.personagem"
    const val SCHEMA_VERSION_ATUAL = 1

    private val gson = Gson()

    fun exportarJson(personagem: Personagem, appVersion: String? = null, uiVariant: String? = null): String {
        val envelope = PersonagemInteropEnvelope(
            schema = SCHEMA,
            schemaVersion = SCHEMA_VERSION_ATUAL,
            exportedAtUtc = Instant.now().toString(),
            appVersion = appVersion?.trim()?.takeIf { it.isNotBlank() },
            uiVariant = uiVariant?.trim()?.takeIf { it.isNotBlank() },
            character = personagem
        )
        return gson.toJson(envelope)
    }

    fun importarJson(json: String): PersonagemImportResult {
        if (json.isBlank()) {
            throw IllegalArgumentException("JSON vazio.")
        }

        val jsonElement = JsonParser.parseString(json)
        if (!jsonElement.isJsonObject) {
            throw IllegalArgumentException("JSON invalido.")
        }

        val root = jsonElement.asJsonObject
        val hasEnvelope = root.has("character")
        if (!hasEnvelope) {
            return PersonagemImportResult(
                personagem = Personagem.fromJson(json),
                metadata = null
            )
        }

        val schema = root.stringOrNull("schema") ?: SCHEMA
        if (!schema.equals(SCHEMA, ignoreCase = true)) {
            throw IllegalArgumentException("Formato de arquivo nao suportado.")
        }

        val schemaVersion = root.intOrNull("schemaVersion") ?: 1
        if (schemaVersion > SCHEMA_VERSION_ATUAL) {
            throw UnsupportedOperationException(
                "Versao $schemaVersion nao suportada (maximo: $SCHEMA_VERSION_ATUAL)."
            )
        }

        val characterElement = root.get("character")
            ?: throw IllegalArgumentException("Arquivo sem objeto character.")
        if (!characterElement.isJsonObject) {
            throw IllegalArgumentException("Campo character invalido.")
        }

        val personagem = Personagem.fromJson(characterElement.toString())
        val metadata = PersonagemImportMetadata(
            schema = schema,
            schemaVersion = schemaVersion,
            exportedAtUtc = root.stringOrNull("exportedAtUtc")
        )

        val aviso = if (schemaVersion < SCHEMA_VERSION_ATUAL) {
            "Arquivo de versao antiga importado em modo de compatibilidade."
        } else {
            null
        }

        return PersonagemImportResult(
            personagem = personagem,
            metadata = metadata,
            aviso = aviso
        )
    }

    private fun JsonObject.stringOrNull(field: String): String? {
        val raw = this.get(field) ?: return null
        if (!raw.isJsonPrimitive) return null
        if (!raw.asJsonPrimitive.isString) return null
        return raw.asString?.trim()?.takeIf { it.isNotBlank() }
    }

    private fun JsonObject.intOrNull(field: String): Int? {
        val raw = this.get(field) ?: return null
        if (!raw.isJsonPrimitive) return null
        return runCatching { raw.asInt }.getOrNull()
    }
}
