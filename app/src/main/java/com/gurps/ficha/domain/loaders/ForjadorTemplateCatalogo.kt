package com.gurps.ficha.domain.loaders

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ForjadorTemplatePericia(
    val id: String = "",
    val pts: Int = 1,
    val esp: String = ""
)

data class ForjadorTemplateVantagem(
    val id: String = "",
    val nivel: Int = 1,
    val custoEscolhido: Int? = null
)

data class ForjadorTemplateDesvantagem(
    val id: String = "",
    val nivel: Int = 1,
    val custoEscolhido: Int? = null,
    val autocontrole: Int? = null
)

data class ForjadorTemplate(
    val id: String = "",
    val nome: String = "",
    val descricao: String = "",
    val tags: List<String> = emptyList(),
    val pontosBase: Int = 150,
    val racaId: String? = null,
    val atributos: Map<String, Int> = emptyMap(),
    val vantagens: List<ForjadorTemplateVantagem> = emptyList(),
    val desvantagens: List<ForjadorTemplateDesvantagem> = emptyList(),
    val pericias: List<ForjadorTemplatePericia> = emptyList(),
    val magias: List<String> = emptyList(),
    val equipamentos: List<String> = emptyList(),
    val variacoes: List<String> = emptyList()
)

object ForjadorTemplateCatalogo {

    private var _templates: List<ForjadorTemplate>? = null

    fun carregar(context: Context): List<ForjadorTemplate> {
        _templates?.let { return it }
        return try {
            val json = context.assets.open("forjador_templates.json")
                .bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<ForjadorTemplate>>() {}.type
            val lista = Gson().fromJson<List<ForjadorTemplate>>(json, type) ?: emptyList()
            _templates = lista
            lista
        } catch (e: Exception) {
            android.util.Log.w("ForjadorTemplate", "Falha ao carregar templates: ${e.message}")
            emptyList()
        }
    }

    /**
     * Escolhe o template mais adequado para um prompt livre.
     * Usa matching de palavras-chave contra id, nome, descricao e tags.
     * Retorna null se nenhum template tiver pontuação > 0.
     */
    fun escolher(prompt: String, templates: List<ForjadorTemplate>): ForjadorTemplate? {
        val promptNorm = prompt.lowercase()
            .replace(Regex("[àáâãä]"), "a")
            .replace(Regex("[èéêë]"), "e")
            .replace(Regex("[ìíîï]"), "i")
            .replace(Regex("[òóôõö]"), "o")
            .replace(Regex("[ùúûü]"), "u")
            .replace(Regex("[ç]"), "c")

        val scores = templates.map { t ->
            val campos = listOf(t.id, t.nome, t.descricao) + t.tags
            val camposNorm = campos.joinToString(" ").lowercase()
                .replace(Regex("[àáâãä]"), "a")
                .replace(Regex("[èéêë]"), "e")
                .replace(Regex("[ìíîï]"), "i")
                .replace(Regex("[òóôõö]"), "o")
                .replace(Regex("[ùúûü]"), "u")
                .replace(Regex("[ç]"), "c")

            // Palavras do prompt que aparecem nos campos do template
            val palavras = promptNorm.split(Regex("\\s+")).filter { it.length >= 4 }
            val hits = palavras.count { camposNorm.contains(it) }
            t to hits
        }

        val melhor = scores.maxByOrNull { it.second }
        return if ((melhor?.second ?: 0) > 0) melhor?.first else null
    }

    /**
     * Serializa o template escolhido num bloco de texto para injetar no prompt.
     * O modelo recebe a base e as sugestões de variação — não um JSON rígido.
     */
    fun formatarParaPrompt(t: ForjadorTemplate): String {
        val sb = StringBuilder()
        sb.appendLine("=== TEMPLATE BASE: ${t.nome} ===")
        sb.appendLine("Arquétipo: ${t.descricao}")
        sb.appendLine("Pontos base: ${t.pontosBase}")

        if (t.racaId != null) sb.appendLine("Raça sugerida: ${t.racaId}")

        if (t.atributos.isNotEmpty()) {
            val ats = t.atributos.entries.joinToString(", ") { "${it.key.uppercase()}=${it.value}" }
            sb.appendLine("Atributos base: $ats")
        }

        if (t.vantagens.isNotEmpty()) {
            val v = t.vantagens.joinToString(", ") { vt ->
                if (vt.nivel > 1) "${vt.id} (nível ${vt.nivel})" else vt.id
            }
            sb.appendLine("Vantagens base: $v")
        }

        if (t.desvantagens.isNotEmpty()) {
            val d = t.desvantagens.joinToString(", ") { it.id }
            sb.appendLine("Desvantagens base: $d")
        }

        if (t.pericias.isNotEmpty()) {
            val p = t.pericias.joinToString(", ") { pt ->
                if (pt.esp.isNotBlank()) "${pt.id} (${pt.esp}, ${pt.pts}pts)"
                else "${pt.id} (${pt.pts}pts)"
            }
            sb.appendLine("Perícias base: $p")
        }

        if (t.magias.isNotEmpty()) {
            sb.appendLine("Magias base: ${t.magias.joinToString(", ")}")
        }

        if (t.equipamentos.isNotEmpty()) {
            sb.appendLine("Equipamentos sugeridos: ${t.equipamentos.joinToString(", ")}")
        }

        if (t.variacoes.isNotEmpty()) {
            sb.appendLine("Variações disponíveis:")
            t.variacoes.forEachIndexed { i, v -> sb.appendLine("  ${i + 1}. $v") }
        }

        sb.appendLine()
        sb.appendLine("INSTRUÇÕES PARA O TEMPLATE:")
        sb.appendLine("• Aplique TODOS os itens do template como ponto de partida.")
        sb.appendLine("• Adapte atributos, vantagens e perícias ao conceito específico do usuário.")
        sb.appendLine("• Use as variações para personalizar e diferenciar do template padrão.")
        sb.appendLine("• O objetivo é que o personagem final seja único, não uma cópia exata do template.")
        sb.appendLine("• Respeite o budget total de ${t.pontosBase} pontos.")

        return sb.toString()
    }
}
