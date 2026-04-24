package com.gurps.ficha.data.network

import com.google.gson.*
import java.lang.reflect.Type

/**
 * MestreIAResponse - Lote 82: Flexibilidade Total.
 * Suporta deserialização de itens como String ou Objeto.
 */
data class MestreIAResponse(
    val nome: String = "",
    val atributos: MestreIAAtributos = MestreIAAtributos(),
    val vantagens: List<MestreIAItem> = emptyList(),
    val desvantagens: List<MestreIAItem> = emptyList(),
    val pericias: List<MestreIAItem> = emptyList(),
    val tecnicas: List<MestreIAItem> = emptyList(),
    val magias: List<MestreIAItem> = emptyList(),
    val qualidades: List<MestreIAItem> = emptyList(),
    val peculiaridades: List<MestreIAItem> = emptyList(),
    val equipamentos: List<MestreIAEquipamento> = emptyList(),
    val aparencia: String = "",
    val historico: String = ""
)

data class MestreIAItem(
    val nome: String = "",
    val custo: Int? = null,
    val descricao: String? = null,
    val nivel: Int = 0 // Usado para perícias e técnicas
)

/**
 * Deserializador que permite que MestreIAItem seja uma String simples ou um Objeto JSON.
 */
class MestreIAItemDeserializer : JsonDeserializer<MestreIAItem> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): MestreIAItem {
        return if (json.isJsonPrimitive) {
            val raw = json.asString
            if (raw.contains(":")) {
                val partes = raw.split(":")
                val nome = partes[0].trim()
                val nivel = partes[1].trim().filter { it.isDigit() }.toIntOrNull() ?: 0
                MestreIAItem(nome = nome, nivel = nivel)
            } else {
                MestreIAItem(nome = raw)
            }
        } else {
            val obj = json.asJsonObject
            MestreIAItem(
                nome = obj.get("nome")?.asString ?: obj.get("id")?.asString ?: "Desconhecido",
                custo = obj.get("custo")?.asInt,
                descricao = obj.get("descricao")?.asString,
                nivel = obj.get("nivel")?.asInt ?: 0
            )
        }
    }
}

data class MestreIAEquipamento(
    val nome: String = "",
    val peso: Float = 0f,
    val custo: Float = 0f,
    val quantidade: Int = 1,
    val rd: Int? = null,
    val dano: String? = null,
    val st_min: Int? = null,
    val aparar: String? = null
)

data class MestreIAAtributos(
    val st: Int = 10,
    val dx: Int = 10,
    val iq: Int = 10,
    val ht: Int = 10
)
