package com.gurps.ficha.data.network

import com.google.gson.*
import java.lang.reflect.Type

/**
 * MestreIAResponse - Lote 83: Blindagem Ultra-Resiliente.
 * Resolve o erro "Expected a string but was BEGIN_OBJECT" ao permitir que 
 * listas de Traços e Perícias aceitem tanto Texto quanto Objetos.
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
    val historico: String = "",
    val versaoApp: String = "v1.5.0-Lote84" // Assinatura para Debug
)

/**
 * Item flexível que se auto-ajusta ao formato enviado pela IA.
 */
data class MestreIAItem(
    val nome: String = "",
    val custo: Int? = null,
    val descricao: String? = null,
    val nivel: Int = 0
)

/**
 * O "Tradutor" Universal: Converte qualquer lixo da IA em um MestreIAItem válido.
 */
class MestreIAItemDeserializer : JsonDeserializer<MestreIAItem> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): MestreIAItem {
        return try {
            if (json.isJsonPrimitive) {
                val raw = json.asString
                if (raw.contains(":")) {
                    val partes = raw.split(":")
                    val nome = partes[0].trim()
                    val nivel = partes[1].trim().filter { it.isDigit() }.toIntOrNull() ?: 0
                    MestreIAItem(nome = nome, nivel = nivel)
                } else {
                    MestreIAItem(nome = raw)
                }
            } else if (json.isJsonObject) {
                val obj = json.asJsonObject
                MestreIAItem(
                    nome = obj.get("nome")?.asString ?: obj.get("id")?.asString ?: "Item sem nome",
                    custo = obj.get("custo")?.asInt,
                    descricao = obj.get("descricao")?.asString ?: obj.get("desc")?.asString,
                    nivel = obj.get("nivel")?.asInt ?: obj.get("nh")?.asInt ?: 0
                )
            } else {
                MestreIAItem(nome = "Erro de Formato")
            }
        } catch (e: Exception) {
            MestreIAItem(nome = "Erro no Parse: ${e.message}")
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
