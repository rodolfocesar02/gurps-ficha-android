package com.gurps.ficha.data.network

import com.google.gson.*
import java.lang.reflect.Type

/**
 * MestreIAResponse - Lote Complexo: schema rico para fichas completas.
 * Carrega especialização, modificadores, autocontrole e técnicas para o tradutor
 * fazer lookup por ID real no catálogo (cálculo de pontos fica no CharacterRules).
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
    val notas: String = "",
    val pontosIniciais: Int = 0,
    val versaoApp: String = "v1.6.0-Complexo" // Assinatura para Debug
)

/**
 * Modificador de uma vantagem/desvantagem (ampliação ou limitação).
 * O tradutor casa [id]/[nome] com modificadoresEspecificos do catálogo.
 */
data class MestreIAMod(
    val id: String? = null,
    val nome: String = "",
    val niveis: Int = 1
)

/**
 * Item flexível que se auto-ajusta ao formato enviado pela IA.
 * Campo [id] é preferido para lookup direto no catálogo.
 * Campos novos: especializacao, autocontrole, modificadores e perícia-base de técnica.
 */
data class MestreIAItem(
    val id: String? = null,
    val nome: String = "",
    val custo: Int? = null,
    val descricao: String? = null,
    val nivel: Int = 0,
    val especializacao: String? = null,
    val autocontrole: Int? = null,
    val modificadores: List<MestreIAMod> = emptyList(),
    // Técnica: aponta para a perícia-base já presente na ficha
    val periciaBaseId: String? = null,
    val periciaBaseEspecializacao: String? = null
)

/**
 * O "Tradutor" Universal: Converte qualquer lixo da IA em um MestreIAItem válido.
 */
class MestreIAItemDeserializer : JsonDeserializer<MestreIAItem> {

    private fun JsonObject.str(vararg keys: String): String? {
        for (k in keys) {
            val v = get(k)
            if (v != null && !v.isJsonNull) return try { v.asString } catch (e: Exception) { null }
        }
        return null
    }

    private fun JsonObject.intOrNull(vararg keys: String): Int? {
        for (k in keys) {
            val v = get(k)
            if (v != null && !v.isJsonNull) {
                return try { v.asInt } catch (e: Exception) {
                    // Aceita strings tipo "1 fp", "-15 pts"
                    Regex("-?\\d+").find(v.asString)?.value?.toIntOrNull()
                }
            }
        }
        return null
    }

    private fun parseMods(arr: JsonArray?): List<MestreIAMod> {
        if (arr == null) return emptyList()
        return arr.mapNotNull { el ->
            when {
                el.isJsonObject -> {
                    val mo = el.asJsonObject
                    MestreIAMod(
                        id = mo.str("id"),
                        nome = mo.str("nome") ?: mo.str("id") ?: "",
                        niveis = (mo.intOrNull("niveis") ?: 1).coerceAtLeast(1)
                    )
                }
                el.isJsonPrimitive -> MestreIAMod(nome = el.asString)
                else -> null
            }
        }
    }

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
                val id = obj.str("id")
                val nome = obj.str("nome") ?: id ?: "Item sem nome"
                val modsArr = obj.get("modificadores")?.takeIf { it.isJsonArray }?.asJsonArray
                MestreIAItem(
                    id = id,
                    nome = nome,
                    custo = obj.intOrNull("custo"),
                    descricao = obj.str("descricao", "desc"),
                    nivel = obj.intOrNull("nivel", "nh") ?: 0,
                    especializacao = obj.str("especializacao", "esp"),
                    autocontrole = obj.intOrNull("autocontrole", "ac"),
                    modificadores = parseMods(modsArr),
                    periciaBaseId = obj.str("periciaBaseId", "pericia_base", "periciaBase"),
                    periciaBaseEspecializacao = obj.str("periciaBaseEspecializacao", "pericia_base_especializacao")
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
    val aparar: String? = null,
    val tipo: String? = null,          // "ARMA" | "ARMADURA" | "ESCUDO" | "CAPA" | "GERAL"
    val tipoCombate: String? = null,   // "corpo_a_corpo" | "distancia"
    val catalogoId: String? = null,
    val bonusDefesa: Int? = null,
    val notas: String? = null
)

data class MestreIAAtributos(
    val st: Int = 10,
    val dx: Int = 10,
    val iq: Int = 10,
    val ht: Int = 10
)
