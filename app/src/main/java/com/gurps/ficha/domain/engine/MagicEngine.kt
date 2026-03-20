package com.gurps.ficha.domain.engine

import com.gurps.ficha.model.*
import com.gurps.ficha.data.DataRepository
import java.text.Normalizer

/**
 * Foundation for GURPS Magic Rules.
 * Centralizes calculations, school restrictions, and special magic validation rules.
 */
object MagicEngine {

    /**
     * Calculates the Magic Aptitude level for a specific spell, considering limitations like "One Single School".
     * Page 41 of GURPS Basic Set.
     */
    fun getNivelAptidaoMagicaParaMagia(personagem: Personagem, magia: MagiaDefinicao?): Int {
        val personAptidoes = personagem.vantagens.filter { it.definicaoId.equals("aptidao_magica", ignoreCase = true) }
        val racialAptidoes = personagem.modeloRacial.vantagens.filter { it.definicaoId.equals("aptidao_magica", ignoreCase = true) }
        val todas = personAptidoes + racialAptidoes

        if (todas.isEmpty()) return 0
        
        return todas.sumOf { aptidao ->
            val nivelBonus = (aptidao.nivel - 1).coerceAtLeast(0)
            val modificadorEscola = aptidao.modificadores.firstOrNull { it.id == "mod_aptidao_escola" }
            if (modificadorEscola != null) {
                val escolaPermitida = modificadorEscola.descricao?.trim()?.lowercase()
                if (escolaPermitida != null && magia != null) {
                    val ehEscolaPermitida = magia.escola?.any { it.trim().lowercase() == escolaPermitida } == true
                    val ehRecuperarEnergia = magia.nome.equals("Recuperar Energia", ignoreCase = true)
                    
                    if (ehEscolaPermitida || ehRecuperarEnergia) nivelBonus else 0
                } else {
                    nivelBonus
                }
            } else {
                nivelBonus
            }
        }
    }

    /**
     * Validates special magic rules mandated by specific spell definitions.
     */
    fun validarRegrasEspeciaisMagia(
        personagem: Personagem, 
        definicao: MagiaDefinicao, 
        dataRepository: DataRepository,
        nivelAptidaoMagicaGeral: Int
    ): String? {
        val id = definicao.id.lowercase()
        val magias = personagem.magias
        
        fun hasMagia(nome: String): Boolean =
            magias.any { it.nome.equals(nome, ignoreCase = true) }
            
        fun nivelMagia(nome: String): Int? {
            val magicSel = magias.firstOrNull { it.nome.equals(nome, ignoreCase = true) } ?: return null
            val def = dataRepository.getMagiaPorId(magicSel.definicaoId)
            val amEstaMagia = def?.let { getNivelAptidaoMagicaParaMagia(personagem, it) } ?: nivelAptidaoMagicaGeral
            return magicSel.calcularNivel(personagem, amEstaMagia)
        }
        
        fun countEscola(escola: String): Int =
            magias.count { it.escola.orEmpty().any { e -> e.equals(escola, ignoreCase = true) } }

        fun normalizarTokenAnimal(raw: String): String {
            return raw
                .lowercase()
                .replace("á", "a")
                .replace("ã", "a")
                .replace("â", "a")
                .replace("é", "e")
                .replace("ê", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ô", "o")
                .replace("õ", "o")
                .replace("ú", "u")
                .replace(Regex("[^a-z0-9\\s]"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
        }

        fun caminhoControleAnimal(magia: MagiaSelecionada): String? {
            val did = magia.definicaoId.lowercase()
            return when {
                did.endsWith("_ar") -> "ar"
                did.endsWith("_terra") -> "terra"
                did.endsWith("_mar") -> "mar"
                did == "controle_de_animal" -> {
                    val tokens = listOf(
                        magia.especializacaoMagia.orEmpty(),
                        magia.nome
                    ).joinToString(" ")
                    val norm = normalizarTokenAnimal(tokens)
                    when {
                        norm.contains("criaturas do ar") || norm.contains(" do ar") -> "ar"
                        norm.contains("criaturas da terra") || norm.contains(" da terra") -> "terra"
                        norm.contains("criaturas do mar") || norm.contains(" do mar") -> "mar"
                        else -> "generic"
                    }
                }
                else -> null
            }
        }

        fun totalControleAnimal(): Int = magias.count {
            val did = it.definicaoId.lowercase()
            did == "controle_de_animal" || did.startsWith("controle_de_animal_")
        }

        fun caminhosControleAnimalDistintos(): Int = magias
            .mapNotNull(::caminhoControleAnimal)
            .toSet()
            .size

        return when (id) {
            "corpo_de_vento" -> {
                if (nivelAptidaoMagicaGeral < 3) "Pré-requisito não atendido: Aptidão Mágica 3."
                else if ((nivelMagia("Corpo de Ar") ?: 0) < 16) "Pré-requisito não atendido: Corpo de Ar NH 16+."
                else if ((nivelMagia("Furacão") ?: 0) < 16 && (nivelMagia("Furacao") ?: 0) < 16) "Pré-requisito não atendido: Furacão NH 16+."
                else null
            }
            "criar_elemental" -> {
                if (nivelAptidaoMagicaGeral < 2) "Pré-requisito não atendido: Aptidão Mágica 2."
                else if (!hasMagia("Controle de Elemental")) "Pré-requisito não atendido: Controle de Elemental."
                else null
            }
            "convocar_elemental" -> {
                if (nivelAptidaoMagicaGeral < 1) "Pré-requisito não atendido: Aptidão Mágica 1."
                else null
            }
            "controle_de_elemental" -> {
                if (!hasMagia("Convocar Elemental")) "Pré-requisito não atendido: Convocar Elemental."
                else null
            }
            "adivinhacao" -> {
                val temHistoria = personagem.pericias.any { it.nome.equals("História", ignoreCase = true) || it.nome.equals("Historia", ignoreCase = true) }
                if (!temHistoria) "Pré-requisito não atendido: História."
                else null
            }
            "anular_possessao" -> {
                if (!hasMagia("Passageiro da Alma")) "Pré-requisito não atendido: Passageiro da Alma."
                else if (!hasMagia("Possessão") && !hasMagia("Possessao")) "Pré-requisito não atendido: Possessão."
                else null
            }
            "cavalgar" -> {
                if (totalControleAnimal() < 1) "Pré-requisito não atendido: pelo menos 1 magia de Controle de Animal."
                else null
            }
            "controle_de_hibrido" -> {
                if (caminhosControleAnimalDistintos() < 2) "Pré-requisito não atendido: 2 caminhos distintos de Controle de Animal (Ar/Terra/Mar)."
                else null
            }
            "espantar_zumbi" -> if (!hasMagia("Zumbi")) "Pré-requisito não atendido: Zumbi." else null
            "golem" -> {
                if (!hasMagia("Encantar")) "Pré-requisito não atendido: Encantar."
                else if (!hasMagia("Moldar Terra")) "Pré-requisito não atendido: Moldar Terra."
                else if (!hasMagia("Animação") && !hasMagia("Animacao")) "Pré-requisito não atendido: Animação."
                else null
            }
            "passageiro_interno" -> {
                if (caminhosControleAnimalDistintos() < 2) "Pré-requisito não atendido: 2 caminhos distintos de Controle de Animal (Ar/Terra/Mar)."
                else null
            }
            "reconstruirnt" -> {
                if (nivelAptidaoMagicaGeral < 3) "Pré-requisito não atendido: Aptidão Mágica 3."
                else if (!hasMagia("Consertar")) "Pré-requisito não atendido: Consertar."
                else if (!hasMagia("Criar Objeto")) "Pré-requisito não atendido: Criar Objeto."
                else {
                    val escolasOk = listOf("Ar", "Fogo", "Terra", "Água", "Agua").map { countEscola(it) }.chunked(2).map { it.maxOrNull() ?: 0 }
                    if (escolasOk.any { it < 3 }) "Pré-requisito não atendido: 3 magias de cada escola (Ar/Fogo/Terra/Água)."
                    else null
                }
            }
            "repelir_animal" -> {
                if (totalControleAnimal() < 1) "Pré-requisito não atendido: Controle de Animal."
                else null
            }
            "transformar_outro" -> {
                if (!hasMagia("Metamorfosear Outro")) "Pré-requisito não atendido: Metamorfosear Outro."
                else if (!hasMagia("Transformar Corpo")) "Pré-requisito não atendido: Transformar Corpo."
                else null
            }
            "restauracao" -> {
                if (hasMagia("Cura Profunda")) {
                    null
                } else {
                    val temAliviarParalisia = hasMagia("Aliviar Paralisia")
                    val totalRestaurar = magias.count { it.nome.contains("Restaurar", ignoreCase = true) }
                    val contagem = (if (temAliviarParalisia) 1 else 0) + totalRestaurar
                    if (contagem < 2) {
                        "Pré-requisito não atendido: Cura Profunda ou 2 entre Aliviar Paralisia e mágicas de Restaurar."
                    } else {
                        null
                    }
                }
            }
            else -> null
        }
    }
    
    fun validarEspecializacaoObrigatoria(definicaoId: String, especializacaoMagia: String?): String? {
        val exigeSubEscolaAnimais = definicaoId.equals("controle_de_animal", ignoreCase = true)
        if (exigeSubEscolaAnimais) {
            if (especializacaoMagia.isNullOrBlank()) {
                return "Selecione a sub-escola: Criaturas da Terra, Criaturas do Ar ou Criaturas do Mar."
            }
            return null
        }

        val exigeEspecializacao = definicaoId.lowercase() in setOf(
            "adivinhacao",
            "cavalgar",
            "controle_de_hibrido",
            "golem",
            "passageiro_interno",
            "criar_elemental",
            "convocar_elemental",
            "controle_de_elemental"
        )
        if (!exigeEspecializacao) return null
        if (especializacaoMagia.isNullOrBlank()) {
            return "Informe a especializacao desta magia."
        }
        return null
    }

    fun permiteMultiplasInstanciasMagia(definicaoId: String): Boolean {
        return definicaoId.lowercase() in setOf(
            "criar_elemental",
            "convocar_elemental",
            "controle_de_elemental",
            "anular_possessao",
            "cavalgar"
        )
    }

    fun permiteMultiplasInstanciasPorEscola(definicaoId: String): Boolean {
        return definicaoId.lowercase() in setOf("criar_elemental", "convocar_elemental", "controle_de_elemental")
    }
}
