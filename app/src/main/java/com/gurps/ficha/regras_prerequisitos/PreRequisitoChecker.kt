package com.gurps.ficha.regras_prerequisitos

import java.text.Normalizer

data class ConditionStatus(
    val label: String,
    val isMet: Boolean,
    val current: String,
    val required: String
)

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

    /**
     * @param contextoMagia true quando o pré-requisito validado é de MAGIA (não de
     * perícia/vantagem). Ativa a "exceção do Escudo": num pré-requisito de magia,
     * "Escudo" refere-se à MAGIA Escudo (Livro de Magia, escola Proteção) — a
     * perícia/equipamento Escudo não satisfaz. (Lote 351 — regra restaurada; existia
     * no caminho MagiaConhecida mas o fallback do parser passou a emitir VantagemConhecida.)
     */
    fun checkParseResult(personagem: Map<String, Any>, parsed: PreRequisitoParser.ParseResult, contextoMagia: Boolean = false): String {
        if (parsed.bypassValidation || parsed.terms.isEmpty()) {
            return "todos requisitos atendidos"
        }

        val faltandoTermos = mutableListOf<String>()

        parsed.terms.forEach { term ->
            var termoAtendido = false
            val faltasAlternativas = mutableListOf<String>()

            term.alternatives.forEach { alternativa ->
                val faltas = alternativa.mapNotNull { requirementFailure(personagem, it, contextoMagia) }
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

    fun checkDetailed(personagem: Map<String, Any>, parsed: PreRequisitoParser.ParseResult): List<ConditionStatus> {
        val results = mutableListOf<ConditionStatus>()
        if (parsed.bypassValidation) return results

        parsed.terms.forEach { term ->
            // Para simplificar na UI, vamos mostrar o status do primeiro conjunto de alternativas.
            // Se houver "OU", a lógica de "isMet" será verdadeira se qualquer alternativa for atendida.
            
            val alternativesStatus = term.alternatives.map { alt ->
                alt.map { req ->
                    val failure = requirementFailure(personagem, req)
                    val isMet = failure == null
                    val currentVal = when (req) {
                        is PreRequisitoType.SkillMinLevel -> {
                            val periciasNiveis = periciasNiveis(personagem)
                            val alvo = normalizar(req.nomePericia)
                            // Busca robusta
                            var valAtual = periciasNiveis[alvo] ?: 0
                            if (valAtual == 0) {
                                val key = periciasNiveis.keys.find { it == alvo || it.contains(alvo) || alvo.contains(it) }
                                if (key != null) valAtual = periciasNiveis[key] ?: 0
                            }
                            valAtual.toString()
                        }
                        is PreRequisitoType.AttributeMin -> valorAtributo(personagem, req.atributo).toString()
                        is PreRequisitoType.AptidaoMagica -> (personagem["aptidao_magica"] as? Int ?: 0).toString()
                        is PreRequisitoType.VantagemConhecida -> if (isMet) "Sim" else "Não"
                        is PreRequisitoType.MagiaConhecida -> if (isMet) "Sim" else "Não"
                        else -> if (isMet) "Sim" else "Não"
                    }
                    val requiredVal = when (req) {
                        is PreRequisitoType.SkillMinLevel -> req.nivelMin.toString()
                        is PreRequisitoType.AttributeMin -> req.minimo.toString()
                        is PreRequisitoType.AptidaoMagica -> req.nivel.toString()
                        is PreRequisitoType.NivelMin -> req.nivel.toString()
                        is PreRequisitoType.VantagemConhecida -> "Sim"
                        else -> ""
                    }
                    
                    val label = when (req) {
                        is PreRequisitoType.VantagemConhecida, is PreRequisitoType.PericiaConhecida -> {
                            val nome = if (req is PreRequisitoType.VantagemConhecida) req.nomeVantagem else (req as PreRequisitoType.PericiaConhecida).nomePericia
                            // O rótulo tem que dizer o que a coisa É, e para isso
                            // manda o CATÁLOGO. Antes ele olhava o que o
                            // personagem já tinha, então "Matemática (Aplicada)"
                            // virava "Vantagem" até você comprar a perícia.
                            val prefixo = when (ehPericiaDoCatalogo(personagem, nome)) {
                                true -> "Perícia"
                                false -> "Vantagem"
                                // Catálogo ausente (chamada antiga): volta ao
                                // palpite pela ficha, em vez de errar sempre.
                                null -> if (temPericiaCompativel(personagem, nome)) "Perícia" else "Vantagem"
                            }
                            "$prefixo: $nome"
                        }
                        else -> req.readableName()
                    }
                    
                    ConditionStatus(label, isMet, currentVal, requiredVal)
                }
            }

            // Se qualquer alternativa (OR) for atendida, o termo está OK.
            // Mas para exibição, vamos achatar os requisitos.
            alternativesStatus.forEach { alt ->
                results.addAll(alt)
            }
        }
        return results
    }

    private fun requirementFailure(personagem: Map<String, Any>, requisito: PreRequisitoType, contextoMagia: Boolean = false): String? {
        return when (requisito) {
            is PreRequisitoType.AttributeMin -> {
                val atual = valorAtributo(personagem, requisito.atributo)
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
            is PreRequisitoType.MagiasEmEscolasDiferentes -> {
                val porEscola = magiasPorEscola(personagem)
                val escolasAtendidas = porEscola.values.count { it >= requisito.magiasPorEscola }
                if (escolasAtendidas < requisito.escolasDiferentes) {
                    "${requisito.magiasPorEscola} magias em ${requisito.escolasDiferentes} escolas diferentes (atual $escolasAtendidas)"
                } else null
            }
            is PreRequisitoType.AtributosSomaMin -> {
                val atual = requisito.atributos.sumOf { valorAtributo(personagem, it) }
                if (atual < requisito.minimo) {
                    "${requisito.atributos.joinToString(" + ")} >= ${requisito.minimo} (atual $atual)"
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
                val estadoExpandido = estado
                    .asSequence()
                    .flatMap { aliasesCondicao(it).asSequence() }
                    .toSet()
                val proibidas = requisito.condicoes
                    .asSequence()
                    .map { normalizar(it) }
                    .flatMap { aliasesCondicao(it).asSequence() }
                    .toSet()
                val violacoes = proibidas.filter { it in estadoExpandido }
                if (violacoes.isNotEmpty()) {
                    "Nao pode ser: ${violacoes.joinToString(" ou ")}"
                } else null
            }
            is PreRequisitoType.VantagemConhecida -> {
                val alvo = normalizar(requisito.nomeVantagem)
                // Exceção do Escudo (Lote 351): em pré-requisito de MAGIA, "Escudo" é a
                // MAGIA Escudo — a perícia/vantagem homônima não satisfaz.
                if (contextoMagia && alvo == "escudo") {
                    val temMagiaEscudo = magiasNomes(personagem).any { it == alvo || it.contains(alvo) }
                    return if (temMagiaEscudo) null else "Conhecimento magico requerido: ${requisito.nomeVantagem}"
                }
                val emVantagens = vantagensConhecidas(personagem).any { it == alvo || it.contains(alvo) || alvo.contains(it) }
                // Compara pelo NÚCLEO: "Matemática (Aplicada)" tem que casar com
                // a perícia "Matemática/NT" da ficha. Ver `nucleoDoNome`.
                val emPericias = temPericiaCompativel(personagem, requisito.nomeVantagem)
                val emMagias = magiasNomes(personagem).any { it == alvo || it.contains(alvo) || alvo.contains(it) }

                if (!emVantagens && !emPericias && !emMagias) {
                    "Falta requisito: ${requisito.nomeVantagem}"
                } else null
            }
            is PreRequisitoType.PericiaConhecida -> {
                val alvo = normalizar(requisito.nomePericia)
                // Exceção do Escudo (Lote 351): idem ao caso VantagemConhecida acima.
                if (contextoMagia && alvo == "escudo") {
                    val temMagiaEscudo = magiasNomes(personagem).any { it == alvo || it.contains(alvo) }
                    return if (temMagiaEscudo) null else "Conhecimento magico requerido: ${requisito.nomePericia}"
                }
                val emPericias = temPericiaCompativel(personagem, requisito.nomePericia)
                val emVantagens = vantagensConhecidas(personagem).any { it == alvo || it.contains(alvo) || alvo.contains(it) }

                if (!emPericias && !emVantagens) {
                    "Falta requisito: ${requisito.nomePericia}"
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
                val pericias = periciasConhecidas(personagem)
                
                // Busca por similaridade (contains) para evitar erros de prefixo
                val encontrouEmVantagens = vantagens.any { it == alvo || it.contains(alvo) || alvo.contains(it) }
                val encontrouEmPericias = pericias.any { it == alvo || it.contains(alvo) || alvo.contains(it) }
                
                if (encontrouEmVantagens || encontrouEmPericias) return null

                "Conhecimento requerido: ${requisito.nomeMagia}"
            }
            is PreRequisitoType.NivelMin -> {
                val nivel = personagem["nivel_personagem"] as? Int ?: 0
                if (nivel < requisito.nivel) {
                    "Nivel >= ${requisito.nivel} (atual $nivel)"
                } else null
            }
            is PreRequisitoType.SkillMinLevel -> {
                val periciasNiveis = periciasNiveis(personagem)
                val alvo = normalizar(requisito.nomePericia)
                // Prioriza busca pelo nome normalizado, depois por ID, depois por "contains"
                var atualValue = periciasNiveis[alvo] ?: 0
                
                if (atualValue == 0) {
                    val key = periciasNiveis.keys.find { it == alvo || it.contains(alvo) || alvo.contains(it) }
                    if (key != null) atualValue = periciasNiveis[key] ?: 0
                }
                
                val atual = atualValue
                if (atual < requisito.nivelMin) {
                    "NH ${requisito.nomePericia} >= ${requisito.nivelMin} (atual $atual)"
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

    /**
     * O NÚCLEO do nome: sem sufixo `/NT` e sem a especialização entre parênteses.
     *
     * Existe por causa de um bug achado em 28/07: as perícias Astronomia,
     * Engenharia e Física pedem **"Matemática (Aplicada)"**, e o catálogo chama
     * a perícia de **"Matemática/NT"**. Normalizados viram `matematica aplicada`
     * e `matematica nt` — nenhum contém o outro, então o pré-requisito **nunca
     * era satisfeito**, mesmo com a perícia na ficha.
     *
     * São duas convenções diferentes se cruzando: o `/NT` é do catálogo (marca
     * perícia que depende de Nível Tecnológico) e o parêntese é do livro
     * (especialização). Nenhuma das duas faz parte do nome de verdade.
     *
     * `Matemática (Aplicada)` → `matematica`
     * `Matemática/NT`         → `matematica`
     */
    fun nucleoDoNome(valor: String): String {
        val semParenteses = valor.substringBefore("(").trim()
        val semNt = semParenteses.replace(Regex("(?i)/\\s*NT\\b"), " ")
        return normalizar(semNt).removeSuffix(" nt").trim()
    }

    /**
     * Se o nome pedido bate com alguma perícia da ficha, comparando pelo núcleo.
     *
     * A comparação por `contains` continua valendo depois do núcleo — é ela que
     * faz "Armas de Fogo" casar com "Armas de Fogo (Pistola)".
     */
    private fun temPericiaCompativel(personagem: Map<String, Any>, nomePedido: String): Boolean {
        val alvo = nucleoDoNome(nomePedido)
        if (alvo.isBlank()) return false
        return periciasConhecidas(personagem).any { conhecida ->
            val nucleo = nucleoDoNome(conhecida)
            nucleo == alvo || nucleo.contains(alvo) || alvo.contains(nucleo)
        }
    }

    /**
     * Se o nome pedido é uma PERÍCIA DO CATÁLOGO — independente de o personagem
     * já tê-la.
     *
     * O rótulo mostrado ("Perícia: X" ou "Vantagem: X") era decidido olhando o
     * que o personagem **já tinha**, não o que a coisa **é**. Resultado: antes de
     * comprar Matemática, o app dizia *"Vantagem: Matemática (Aplicada)"* — que
     * é exatamente o que o usuário reportou.
     *
     * Quando o catálogo não está no contexto (chamadas antigas), devolve null e
     * quem chama volta ao palpite anterior, em vez de quebrar.
     */
    @Suppress("UNCHECKED_CAST")
    private fun ehPericiaDoCatalogo(personagem: Map<String, Any>, nomePedido: String): Boolean? {
        val catalogo = personagem["pericias_catalogo_nucleos"] as? Set<String> ?: return null
        val alvo = nucleoDoNome(nomePedido)
        if (alvo.isBlank()) return null
        return catalogo.any { it == alvo || it.contains(alvo) || alvo.contains(it) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun periciasNiveis(personagem: Map<String, Any>): Map<String, Int> {
        return personagem["pericias_niveis_normalizadas"] as? Map<String, Int> ?: emptyMap()
    }

    @Suppress("UNCHECKED_CAST")
    private fun condicoesEstado(personagem: Map<String, Any>): Set<String> {
        return personagem["condicoes_estado_normalizadas"] as? Set<String> ?: emptySet()
    }

    fun normalizar(valor: String): String {
        val semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return semAcento
            .lowercase()
            .replace("-", " ")
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun valorAtributo(personagem: Map<String, Any>, atributo: String): Int {
        val atrNorm = normalizar(atributo).uppercase()
        return (personagem[atrNorm] as? Int)
            ?: (personagem[atributo] as? Int)
            ?: (personagem[atributo.uppercase()] as? Int)
            ?: 0
    }

    private fun aliasesCondicao(condicaoNorm: String): Set<String> {
        return when (condicaoNorm) {
            "cego", "cegueira" -> setOf("cego", "cegueira")
            "surdo", "surdez" -> setOf("surdo", "surdez")
            else -> setOf(condicaoNorm)
        }
    }
}
