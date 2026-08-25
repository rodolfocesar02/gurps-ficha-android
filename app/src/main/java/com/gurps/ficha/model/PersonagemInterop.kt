package com.gurps.ficha.model

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
    val character: Personagem,
    /**
     * **Os numeros que a ficha calcula** -- lote CAMPO-16.
     *
     * 🔴 SO DE SAIDA. Ao importar ele e **ignorado**, e tudo e recalculado
     * dos dados crus. Sem isso, um arquivo mexido a mao poria uma Esquiva 20
     * aqui e o app acreditaria -- e ela sobreviveria a tudo, porque nao ha nada
     * nos dados crus que a contradiga.
     *
     * ⚠️ Anulavel para as fichas antigas: um arquivo exportado antes deste
     * lote nao o tem, e tem de continuar a abrir.
     */
    val calculado: FichaCalculada? = null
)

object PersonagemInterop {
    const val SCHEMA = "gurps.personagem"
    const val SCHEMA_VERSION_ATUAL = 1

    private val gson = Gson()

    fun exportarJson(personagem: Personagem, appVersion: String? = null, uiVariant: String? = null): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val envelope = PersonagemInteropEnvelope(
            schema = SCHEMA,
            schemaVersion = SCHEMA_VERSION_ATUAL,
            exportedAtUtc = sdf.format(Date()),
            appVersion = appVersion?.trim()?.takeIf { it.isNotBlank() },
            uiVariant = uiVariant?.trim()?.takeIf { it.isNotBlank() },
            character = personagem,
            // 🔴 Calculado AQUI, chamando as mesmas propriedades que a tela usa.
            // A alternativa era a Mesa Virtual recalcular tudo do lado dela -- duas
            // contas para a mesma coisa, que e o defeito numero um deste projeto.
            calculado = FichaCalculada.de(personagem)
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
