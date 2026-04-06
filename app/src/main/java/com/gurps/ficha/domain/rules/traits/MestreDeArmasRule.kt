package com.gurps.ficha.domain.rules.traits

import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import java.text.Normalizer

/**
 * Regra para a vantagem Mestre de Armas (Weapon Master).
 * Bônus de dano: +1 por dado se NH=DX+1, +2 por dado se NH=DX+2 ou mais.
 */
class MestreDeArmasRule : TraitRule {
    override val traitId: String = "mestre_de_armas"

    override fun calculateCost(
        selection: com.gurps.ficha.model.VantagemSelecionada,
        modifiers: List<com.gurps.ficha.model.ModificadorSelecao>
    ): Int? {
        val classId = selection.metadados?.get("classId") ?: "todas"
        return when (classId) {
            "todas" -> 45
            "amp_classe" -> 40
            "int_classe" -> 35
            "peq_classe" -> 30
            "set_two" -> 25
            "single" -> 20
            else -> null
        }
    }

    private fun normalize(text: String?): String {
        if (text == null) return ""
        
        // 1. Remover informações em parênteses antes de normalizar (ex: "MACHADO (DX-5)")
        var res = text.lowercase()
        if (res.contains("(")) {
            res = res.substringBefore("(").trim()
        }

        // 2. Normalização Unicode (NFD) para separar acentos dos caracteres
        val nfdNormalizedString = Normalizer.normalize(res, Normalizer.Form.NFD)
        val regex = Regex("\\p{InCombiningDiacriticalMarks}+")
        res = regex.replace(nfdNormalizedString, "")
        
        // 3. Limpeza de caracteres não alfanuméricos (mantendo espaços)
        res = res.replace(Regex("[^a-z0-9\\s]"), " ")
        
        // 4. Remoção de plurais em cada palavra (ex: "espadas" -> "espada", "macas" -> "maca")
        // Isso ajuda a casar "Espadas de Lâmina Larga" com "Espada Larga"
        res = res.split(" ").filter { it.isNotBlank() }.map { word ->
            if (word.length > 3 && word.endsWith("s")) {
                word.substring(0, word.length - 1)
            } else {
                word
            }
        }.joinToString(" ")
        
        return res.replace(Regex("\\s+"), " ").trim()
    }

    // Mapeamento oficial baseado na lista do usuário: Grupo da Arma (normalizado) -> Perícia Correta (normalizado)
    private val MAPA_GRUPOS_PERICIAS = mapOf(
        "maca machado" to "maca machado",
        "espada de lamina larga" to "espada de lamina larga",
        "mangual" to "mangual",
        "espada de energia" to "espada de energia",
        "garrote" to "garrote",
        "faca" to "faca",
        "kusari" to "kusari",
        "lanca de justa" to "lanca de justa",
        "chicote monofio" to "chicote monofio",
        "arma de haste" to "arma de haste",
        "rapieira" to "rapieira",
        "sabre" to "sabre",
        "escudo" to "escudo",
        "espada curta" to "espada curta",
        "tercado" to "tercado",
        "lanca" to "lanca",
        "bastao" to "bastao",
        "maca machado de dua mao" to "maca machado de dua mao",
        "mangual de dua mao" to "mangual de dua mao",
        "espada de dua mao" to "espada de dua mao",
        "chicote" to "chicote",
        "zarabatana" to "zarabatana",
        "boleadeira" to "boleadeira",
        "arco" to "arco",
        "besta" to "besta",
        "rede" to "rede",
        "funda" to "funda",
        "arremessador de lanca" to "arremessador de lanca",
        "arma de arremesso" to "arma de arremesso"
    )

    override fun getDamageBonusPerDie(
        personagem: com.gurps.ficha.model.Personagem,
        selection: com.gurps.ficha.model.VantagemSelecionada,
        periciaId: String?,
        weaponName: String?,
        armaGrupo: String?
    ): Int {
        val metadados = selection.metadados ?: emptyMap()
        val classId = metadados["classId"] ?: "todas"
        val periciasCobertasRaw = metadados["pericias_cobertas"] ?: ""

        val nomeArmaN = normalize(weaponName)
        val grupoArmaN = normalize(armaGrupo)
        val periciaIdN = normalize(periciaId)

        // 1. Bloqueio Estrito (Weapon Master não se aplica a combate desarmado)
        val proibidas = setOf("briga", "boxe", "carate", "karate", "judo", "luta_grecoromana", "wrestling", "ataque_inato", "golpeadores", "sumo", "krav", "aikido")
        if (proibidas.contains(periciaIdN) || 
            nomeArmaN.contains("soco") || 
            nomeArmaN.contains("chute") || 
            nomeArmaN.contains("mordida") ||
            periciaIdN.contains("desarmado")) return 0

        // 2. Verificar Cobertura
        val estaCoberta = when (classId) {
            "todas" -> true
            "amp_classe", "int_classe", "peq_classe" -> {
                val cobertas = periciasCobertasRaw.split(",").map { normalize(it) }
                cobertas.any { it.isNotBlank() && (nomeArmaN.contains(it) || grupoArmaN.contains(it) || periciaIdN.contains(it)) }
            }
            "set_two", "single" -> {
                val especificas = periciasCobertasRaw.split(",").map { normalize(it) }
                especificas.any { it.isNotBlank() && (nomeArmaN.contains(it) || grupoArmaN.contains(it)) }
            }
            else -> true
        }

        if (!estaCoberta) return 0

        // 3. IDENTIFICAÇÃO DA PERÍCIA (Strict Mapping)
        val periciaAlvoDoGrupo = MAPA_GRUPOS_PERICIAS[grupoArmaN]

        // Função auxiliar robusta para verificar compatibilidade
        fun isSkillCompatible(pNome: String, pDefinicaoId: String): Boolean {
            val pNomeN = normalize(pNome)
            
            // 1. Prioridade Máxima: Mapeamento Direto
            // Se o grupo da arma é conhecido, a perícia DEVE ser a correta para aquele grupo.
            if (periciaAlvoDoGrupo != null) {
                // Ex: "espada de lamina larga" deve ser a perícia para o grupo "espada de lamina larga"
                return pNomeN == periciaAlvoDoGrupo || pNomeN.contains(periciaAlvoDoGrupo)
            }
            
            // 2. Fallback por Nome (apenas se não houver mapeamento oficial)
            // Usamos comparação de conjuntos de palavras (com > 3 letras) para evitar bônus cruzados.
            // Ex: "Arco" ([arco]) não intersecta com "Espada Larga" ([espada, larga])
            val skillWords = pNomeN.split(" ").filter { it.length > 3 }.toSet()
            val weaponWords = nomeArmaN.split(" ").filter { it.length > 3 }.toSet()
            val groupWords = grupoArmaN.split(" ").filter { it.length > 2 }.toSet() // grupo costuma ter ids curtos
            
            return (skillWords.intersect(weaponWords).isNotEmpty()) || 
                   (skillWords.intersect(groupWords).isNotEmpty())
        }

        // Encontrar a perícia que está sendo usada
        val pericia = if (periciaIdN.isNotBlank()) {
            // Se uma perícia foi selecionada no UI, ela SÓ será usada se for compatível com a arma atual
            personagem.pericias.find { p -> 
                val pIdN = normalize(p.definicaoId)
                pIdN == periciaIdN && isSkillCompatible(p.nome, p.definicaoId)
            }
        } else {
            // Busca geral pela melhor perícia compatível (apenas em visualização de ficha/inventário)
            personagem.pericias.filter { p -> isSkillCompatible(p.nome, p.definicaoId) }
                .maxByOrNull { it.calcularNivel(personagem) }
        } ?: return 0

        val nh = pericia.calcularNivel(personagem)
        val dx = personagem.destreza

        return when {
            nh >= dx + 2 -> 2
            nh >= dx + 1 -> 1
            else -> 0
        }
    }

    // Função utilitária para o catálogo
    fun isWeaponCovered(personagem: com.gurps.ficha.model.Personagem, selection: com.gurps.ficha.model.VantagemSelecionada, weaponName: String?, armaGrupo: String?): Boolean {
        val metadados = selection.metadados ?: emptyMap()
        val classId = metadados["classId"] ?: "todas"
        val periciasCobertasRaw = metadados["pericias_cobertas"] ?: ""

        val nomeArmaN = normalize(weaponName)
        val grupoArmaN = normalize(armaGrupo)
        
        // Regra de exclusão (Desarmado/Fogo)
        val proibidas = setOf("soco", "chute", "mordida")
        if (proibidas.any { nomeArmaN.contains(it) }) return false

        return when (classId) {
            "todas" -> true
            "amp_classe", "int_classe", "peq_classe" -> {
                val cobertas = periciasCobertasRaw.split(",").map { normalize(it) }
                cobertas.any { it.isNotBlank() && (
                    nomeArmaN.contains(it) || grupoArmaN.contains(it) ||
                    it.split(" ").containsAll(nomeArmaN.split(" ")) ||
                    it.split(" ").containsAll(grupoArmaN.split(" "))
                ) }
            }
            "set_two", "single" -> {
                val especificas = periciasCobertasRaw.split(",").map { normalize(it) }
                especificas.any { it.isNotBlank() && (
                    nomeArmaN.contains(it) || grupoArmaN.contains(it) ||
                    it.split(" ").containsAll(nomeArmaN.split(" ")) ||
                    it.split(" ").containsAll(grupoArmaN.split(" "))
                ) }
            }
            else -> true
        }
    }
}
