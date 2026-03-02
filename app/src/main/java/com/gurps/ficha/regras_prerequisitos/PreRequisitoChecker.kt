package com.gurps.ficha.regras_prerequisitos

import java.text.Normalizer

object PreRequisitoChecker {

    fun check(character: Any, requisitos: List<PreRequisitoType>): Boolean {
        println("[PreRequisitoChecker] check() legado chamado com $requisitos")
        return requisitos.isEmpty()
    }

    fun checkSimples(personagem: Map<String, Any>, requisitos: List<PreRequisitoType>): String {
        val faltando = requisitos.mapNotNull { requirementFailure(personagem, it) }
        return if (faltando.isEmpty()) {
            "todos requisitos atendidos"
        } else {
            "faltando: ${faltando.joinToString(", ")}"
        }
    }

    fun checkParseResult(personagem: Map<String, Any>, parsed: PreRequisitoParser.ParseResult): String {
        if (parsed.bypassValidation || parsed.terms.isEmpty()) {
            return "todos requisitos atendidos"
        }

        val faltandoTermos = mutableListOf<String>()

        parsed.terms.forEach { term ->
            var termoAtendido = false
            val faltasAlternativas = mutableListOf<String>()

            term.alternatives.forEach { alternativa ->
                val faltas = alternativa.mapNotNull { requirementFailure(personagem, it) }
                if (faltas.isEmpty()) {
                    termoAtendido = true
                    return@forEach
                }
                faltasAlternativas.add(faltas.joinToString(" e "))
            }

            if (!termoAtendido) {
                val texto = if (faltasAlternativas.size == 1) {
                    faltasAlternativas.first()
                } else {
                    faltasAlternativas.joinToString(" ou ")
                }
                faltandoTermos.add(texto)
            }
        }

        return if (faltandoTermos.isEmpty()) {
            "todos requisitos atendidos"
        } else {
            "faltando: ${faltandoTermos.joinToString(", ")}"
        }
    }

    private fun requirementFailure(personagem: Map<String, Any>, requisito: PreRequisitoType): String? {
        return when (requisito) {
            is PreRequisitoType.AttributeMin -> {
                val atual = (personagem[requisito.atributo] as? Int)
                    ?: (personagem[requisito.atributo.uppercase()] as? Int)
                    ?: 0
                if (atual < requisito.minimo) {
                    "${requisito.atributo} >= ${requisito.minimo} (atual $atual)"
                } else null
            }
            is PreRequisitoType.AptidaoMagica -> {
                val nivel = personagem["aptidao_magica"] as? Int ?: 0
                if (nivel < requisito.nivel) {
                    "Aptidao Magica >= ${requisito.nivel} (atual $nivel)"
                } else null
            }
            is PreRequisitoType.MagiasEscola -> {
                val q = magiasPorEscola(personagem)[normalizar(requisito.escola)] ?: 0
                if (q < requisito.quantidade) {
                    "${requisito.quantidade} magias de ${requisito.escola} (atual $q)"
                } else null
            }
            is PreRequisitoType.QuantidadeMagiasPorEscolas -> {
                val escolasMap = magiasPorEscola(personagem)
                val total = requisito.escolas.sumOf { escolasMap[normalizar(it)] ?: 0 }
                if (total < requisito.quantidade) {
                    "${requisito.quantidade} magias em ${requisito.escolas.joinToString(" ou ")} (atual $total)"
                } else null
            }
            is PreRequisitoType.QuantidadeMagiasPorTemas -> {
                val nomes = magiasNomes(personagem)
                val temas = requisito.temas.map { normalizar(it) }.filter { it.isNotBlank() }
                val encontrados = nomes.filter { nome -> temas.any { tema -> nome.contains(tema) } }.toSet().size
                if (encontrados < requisito.quantidade) {
                    "${requisito.quantidade} magias de tema ${requisito.temas.joinToString("/")} (atual $encontrados)"
                } else null
            }
            is PreRequisitoType.QuantidadeOutrasMagias -> {
                val nomes = magiasNomes(personagem)
                val total = if (requisito.contexto.isNullOrBlank()) {
                    nomes.size
                } else {
                    val ctx = normalizar(requisito.contexto)
                    val porNome = nomes.count { it.contains(ctx) }
                    val porEscola = magiasPorEscola(personagem)[ctx] ?: 0
                    maxOf(porNome, porEscola)
                }
                if (total < requisito.quantidade) {
                    "${requisito.quantidade} outras magias${requisito.contexto?.let { " de $it" } ?: ""} (atual $total)"
                } else null
            }
            is PreRequisitoType.QualquerMagiaComNome -> {
                val trecho = normalizar(requisito.trechoNome)
                val atende = magiasNomes(personagem).any { it.contains(trecho) }
                if (!atende) {
                    "Qualquer magia com nome contendo: ${requisito.trechoNome}"
                } else null
            }
            is PreRequisitoType.MagiaInclusaNaContagem -> {
                val magias = magiasNomes(personagem)
                val magiaNorm = normalizar(requisito.nomeMagia)
                if (magiaNorm !in magias) {
                    "Magia inclusa: ${requisito.nomeMagia}"
                } else {
                    val escolasPorMagia = escolasPorMagia(personagem)
                    val escolaCtx = requisito.escolaContexto?.let { normalizar(it) }
                    if (escolaCtx != null) {
                        val escolasMagia = escolasPorMagia[magiaNorm].orEmpty()
                        if (escolasMagia.isNotEmpty() && escolaCtx !in escolasMagia) {
                            "Magia ${requisito.nomeMagia} deve estar entre as magias de ${requisito.escolaContexto}"
                        } else null
                    } else null
                }
            }
            is PreRequisitoType.NaoPodeSer -> {
                val estado = condicoesEstado(personagem)
                val proibidas = requisito.condicoes.map { normalizar(it) }
                val violacoes = proibidas.filter { it in estado }
                if (violacoes.isNotEmpty()) {
                    "Nao pode ser: ${violacoes.joinToString(" ou ")}"
                } else null
            }
            is PreRequisitoType.VantagemConhecida -> {
                val vantagens = vantagensConhecidas(personagem)
                val alvo = normalizar(requisito.nomeVantagem)
                if (alvo !in vantagens) {
                    "Vantagem conhecida: ${requisito.nomeVantagem}"
                } else null
            }
            is PreRequisitoType.PericiaConhecida -> {
                val pericias = periciasConhecidas(personagem)
                val alvo = normalizar(requisito.nomePericia)
                if (alvo !in pericias) {
                    "Pericia conhecida: ${requisito.nomePericia}"
                } else null
            }
            is PreRequisitoType.MagiaConhecida -> {
                val alvo = normalizar(requisito.nomeMagia)
                val magias = magiasNomes(personagem)
                val escolas = escolasConhecidas(personagem)

                val magiaExataOuContem = magias.any { it == alvo || it.contains(alvo) }
                if (magiaExataOuContem) return null

                if (alvo in escolas) return null

                if (alvo == "escudo") {
                    return "Conhecimento magico requerido: ${requisito.nomeMagia}"
                }

                val vantagens = vantagensConhecidas(personagem)
                if (alvo in vantagens) return null

                val pericias = periciasConhecidas(personagem)
                if (alvo in pericias) return null

                "Conhecimento requerido: ${requisito.nomeMagia}"
            }
            is PreRequisitoType.NivelMin -> {
                val nivel = personagem["nivel_personagem"] as? Int ?: 0
                if (nivel < requisito.nivel) {
                    "Nivel >= ${requisito.nivel} (atual $nivel)"
                } else null
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun magiasNomes(personagem: Map<String, Any>): Set<String> {
        return personagem["magias_conhecidas_normalizadas"] as? Set<String> ?: emptySet()
    }

    @Suppress("UNCHECKED_CAST")
    private fun magiasPorEscola(personagem: Map<String, Any>): Map<String, Int> {
        return personagem["magias_por_escola_normalizada"] as? Map<String, Int> ?: emptyMap()
    }

    @Suppress("UNCHECKED_CAST")
    private fun escolasConhecidas(personagem: Map<String, Any>): Set<String> {
        return personagem["escolas_conhecidas_normalizadas"] as? Set<String> ?: emptySet()
    }

    @Suppress("UNCHECKED_CAST")
    private fun escolasPorMagia(personagem: Map<String, Any>): Map<String, Set<String>> {
        return personagem["escolas_por_magia_normalizadas"] as? Map<String, Set<String>> ?: emptyMap()
    }

    @Suppress("UNCHECKED_CAST")
    private fun vantagensConhecidas(personagem: Map<String, Any>): Set<String> {
        return personagem["vantagens_conhecidas_normalizadas"] as? Set<String> ?: emptySet()
    }

    @Suppress("UNCHECKED_CAST")
    private fun periciasConhecidas(personagem: Map<String, Any>): Set<String> {
        return personagem["pericias_conhecidas_normalizadas"] as? Set<String> ?: emptySet()
    }

    @Suppress("UNCHECKED_CAST")
    private fun condicoesEstado(personagem: Map<String, Any>): Set<String> {
        return personagem["condicoes_estado_normalizadas"] as? Set<String> ?: emptySet()
    }

    private fun normalizar(valor: String): String {
        val semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return semAcento
            .lowercase()
            .replace("-", " ")
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
