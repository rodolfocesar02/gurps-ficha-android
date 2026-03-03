package com.gurps.ficha.domain.magias

import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.model.MagiaDefinicao
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.regras_prerequisitos.PreRequisitoParser
import com.gurps.ficha.regras_prerequisitos.PreRequisitoType
import java.text.Normalizer
import java.util.LinkedHashMap

class MagiaTargetEngine(
    private val dataRepository: DataRepository
) {
    private val parseCache = object : LinkedHashMap<String, PreRequisitoParser.ParseResult>(256, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, PreRequisitoParser.ParseResult>?): Boolean {
            return size > 256
        }
    }
    private val modoAlvoCache = object : LinkedHashMap<String, ModoAlvoResult>(128, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ModoAlvoResult>?): Boolean {
            return size > 128
        }
    }

    data class ModoAlvoResult(
        val ids: List<String>,
        val parcial: Boolean = false,
        val aviso: String? = null
    )

    private data class GuardrailBudget(
        val startedAtMs: Long = System.currentTimeMillis(),
        val maxMs: Long = 420,
        val maxNodes: Int = 1600,
        val maxDepth: Int = 3,
        var nodes: Int = 0,
        var limiteMotivo: String? = null
    ) {
        fun step(amount: Int = 1): Boolean {
            nodes += amount
            if (nodes > maxNodes) {
                limiteMotivo = "limite de análise de nós"
                return false
            }
            if (System.currentTimeMillis() - startedAtMs > maxMs) {
                limiteMotivo = "limite de tempo"
                return false
            }
            return true
        }

        fun allowDepth(depth: Int): Boolean {
            if (depth > maxDepth) {
                limiteMotivo = "limite de profundidade"
                return false
            }
            return true
        }

        fun parcialResult(ids: List<String>): ModoAlvoResult {
            val motivo = limiteMotivo ?: "limite de segurança"
            return ModoAlvoResult(
                ids = ids,
                parcial = true,
                aviso = "Trilha parcial (guardrail: $motivo)."
            )
        }
    }

    fun listaRelacionadosMagiaAlvo(
        alvo: MagiaDefinicao,
        personagem: Personagem
    ): List<String> {
        return calcularModoAlvo(alvo, personagem).ids
    }

    fun calcularModoAlvo(
        alvo: MagiaDefinicao,
        personagem: Personagem,
        contextoKey: String? = null
    ): ModoAlvoResult {
        val chaveCache = contextoKey?.takeIf { it.isNotBlank() }
        if (chaveCache != null) {
            synchronized(this) {
                modoAlvoCache[chaveCache]?.let { return it }
            }
        }
        val budget = GuardrailBudget()
        val prereqRaw = dataRepository.preRequisitoNormalizadoParaAnalise(alvo)
        val idsRelacionados = mutableListOf<String>()
        val nomesObrigatorios = mutableSetOf<String>()
        val familiasNome = mutableSetOf<String>()
        val escolasComQtd = mutableListOf<Pair<String, Int>>()
        val reqEscolasDiferentes = mutableListOf<PreRequisitoType.MagiasEmEscolasDiferentes>()
        fun addId(id: String) {
            if (id !in idsRelacionados) idsRelacionados.add(id)
        }

        addId(alvo.id)
        if (dataRepository.magiaSemPreRequisito(alvo)) return ModoAlvoResult(idsRelacionados)

        val parsed = parseCached(prereqRaw)
        parsed.tipos.forEach { tipo ->
            if (!budget.step()) return budget.parcialResult(idsRelacionados)
            when (tipo) {
                is PreRequisitoType.MagiaConhecida -> nomesObrigatorios.add(normalizarTexto(tipo.nomeMagia))
                is PreRequisitoType.MagiaInclusaNaContagem -> {
                    nomesObrigatorios.add(normalizarTexto(tipo.nomeMagia))
                    tipo.escolaContexto?.let { escolasComQtd.add(normalizarTexto(it) to 1) }
                }
                is PreRequisitoType.MagiasEscola -> {
                    escolasComQtd.add(normalizarTexto(tipo.escola) to tipo.quantidade.coerceAtLeast(1))
                }
                is PreRequisitoType.QuantidadeMagiasPorEscolas -> {
                    tipo.escolas.forEach { escola ->
                        escolasComQtd.add(normalizarTexto(escola) to tipo.quantidade.coerceAtLeast(1))
                    }
                }
                is PreRequisitoType.QuantidadeMagiasPorTemas -> {
                    tipo.temas.forEach { familiasNome.add(normalizarTexto(it)) }
                }
                is PreRequisitoType.QualquerMagiaComNome -> familiasNome.add(normalizarTexto(tipo.trechoNome))
                is PreRequisitoType.QuantidadeOutrasMagias -> {
                    val ctx = tipo.contexto?.let(::normalizarTexto)
                    if (!ctx.isNullOrBlank()) familiasNome.add(ctx)
                }
                is PreRequisitoType.MagiasEmEscolasDiferentes -> reqEscolasDiferentes.add(tipo)
                else -> Unit
            }
        }

        val escolasCatalogo = dataRepository.magias
            .flatMap { it.escola.orEmpty() }
            .map(::normalizarTexto)
            .filter { it.length >= 3 }
            .distinct()
            .toSet()
        val familiasSomenteNome = familiasNome.filter { token ->
            token.length >= 3 && token !in escolasCatalogo
        }

        val nomesDiretos = dataRepository.magias.filter { magia ->
            if (!budget.step()) return budget.parcialResult(idsRelacionados)
            val nomeNormalizado = normalizarTexto(magia.nome)
            nomesObrigatorios.any { nomeReq ->
                nomeReq.isNotBlank() &&
                    (nomeNormalizado == nomeReq || nomeNormalizado.contains(nomeReq) || nomeReq.contains(nomeNormalizado))
            }
        }
        nomesDiretos.sortedBy { prioridadeMagiaParaAlvo(it) }.forEach { addId(it.id) }

        familiasSomenteNome.forEach { token ->
            if (!budget.step()) return budget.parcialResult(idsRelacionados)
            val candidatas = dataRepository.magias
                .filter { magia ->
                    if (!budget.step()) return budget.parcialResult(idsRelacionados)
                    nomeCombinaFamilia(normalizarTexto(magia.nome), token)
                }
                .sortedBy { prioridadeMagiaParaAlvo(it) }
                .take(8)
            candidatas.forEach { addId(it.id) }
        }

        escolasComQtd.forEach { (escolaNorm, qtd) ->
            if (!budget.step()) return budget.parcialResult(idsRelacionados)
            val trilha = gerarTrilhaProgressaoPorEscola(
                escolaNorm = escolaNorm,
                quantidadeDesejada = qtd,
                estadoInicial = personagem,
                limiteMaxSugestoes = (qtd + 8).coerceIn(10, 20),
                budget = budget
            )
            trilha.forEach { addId(it.id) }
        }

        if (reqEscolasDiferentes.isNotEmpty()) {
            val escolasConhecidas = mutableMapOf<String, Int>()
            personagem.magias.forEach { magia ->
                if (!budget.step()) return budget.parcialResult(idsRelacionados)
                magia.escola.orEmpty().map(::normalizarTexto).forEach { escola ->
                    escolasConhecidas[escola] = (escolasConhecidas[escola] ?: 0) + 1
                }
            }
            val escolasCatalogoOrdenadas = dataRepository.magias
                .flatMap { it.escola.orEmpty() }
                .map(::normalizarTexto)
                .filter { it.isNotBlank() }
                .distinct()
                .sortedBy { escola -> escolasConhecidas[escola] ?: 0 }

            reqEscolasDiferentes.forEach { req ->
                if (!budget.step()) return budget.parcialResult(idsRelacionados)
                var escolasSugeridas = 0
                val limiteEscolas = req.escolasDiferentes.coerceIn(1, 15)
                escolasCatalogoOrdenadas.forEach { escolaNorm ->
                    if (!budget.step()) return budget.parcialResult(idsRelacionados)
                    if (escolasSugeridas >= limiteEscolas) return@forEach
                    val countAtual = escolasConhecidas[escolaNorm] ?: 0
                    if (req.outrasEscolas && countAtual > 0) return@forEach
                    if (countAtual >= req.magiasPorEscola) return@forEach
                    val trilha = gerarTrilhaProgressaoPorEscola(
                        escolaNorm = escolaNorm,
                        quantidadeDesejada = req.magiasPorEscola,
                        estadoInicial = personagem,
                        limiteMaxSugestoes = (req.magiasPorEscola + 2).coerceIn(3, 8),
                        budget = budget
                    )
                    if (trilha.isNotEmpty()) {
                        trilha.forEach { addId(it.id) }
                        escolasSugeridas++
                    }
                }
            }
        }

        if (idsRelacionados.size <= 1) {
            val escolasAlvo = alvo.escola.orEmpty().map(::normalizarTexto).toSet()
            if (escolasAlvo.isNotEmpty()) {
                escolasAlvo.forEach { escolaNorm ->
                    if (!budget.step()) return budget.parcialResult(idsRelacionados)
                    val fallback = gerarTrilhaProgressaoPorEscola(
                        escolaNorm = escolaNorm,
                        quantidadeDesejada = 6,
                        estadoInicial = personagem,
                        limiteMaxSugestoes = 12,
                        budget = budget
                    )
                    fallback.forEach { addId(it.id) }
                }
            }
        }

        val result = if (budget.limiteMotivo != null) budget.parcialResult(idsRelacionados)
        else ModoAlvoResult(ids = idsRelacionados)
        if (chaveCache != null) {
            synchronized(this) {
                modoAlvoCache[chaveCache] = result
            }
        }
        return result
    }

    private fun gerarTrilhaProgressaoPorEscola(
        escolaNorm: String,
        quantidadeDesejada: Int,
        estadoInicial: Personagem,
        limiteMaxSugestoes: Int,
        budget: GuardrailBudget
    ): List<MagiaDefinicao> {
        if (escolaNorm.isBlank()) return emptyList()
        val candidatas = dataRepository.magias
            .filter { magia ->
                if (!budget.step()) return emptyList()
                magia.escola.orEmpty().map(::normalizarTexto).any { it == escolaNorm }
            }
            .sortedBy { prioridadeMagiaParaAlvo(it) }
        if (candidatas.isEmpty()) return emptyList()

        val trilha = mutableListOf<MagiaDefinicao>()
        var estado = estadoInicial
        val limite = limiteMaxSugestoes.coerceAtLeast(6)
        var guard = 0
        while (trilha.size < limite && guard < 120) {
            guard++
            if (!budget.step()) break
            val proxima = candidatas.firstOrNull { magia ->
                if (!budget.step()) return@firstOrNull false
                trilha.none { it.id == magia.id } &&
                    estado.magias.none { it.definicaoId == magia.id } &&
                    magiaPodeSerAprendidaNoEstado(magia, estado)
            }
            if (proxima != null) {
                trilha.add(proxima)
                estado = adicionarMagiaNoEstado(estado, proxima)
                val totalNaEscola = contarMagiasDaEscolaNoEstado(estado, escolaNorm)
                if (totalNaEscola >= quantidadeDesejada && trilha.size >= (quantidadeDesejada + 2).coerceAtMost(limite)) break
                continue
            }

            val bloqueadasDaEscola = candidatas.filter { magia ->
                trilha.none { it.id == magia.id } &&
                    estado.magias.none { it.definicaoId == magia.id }
            }
            val ponte = primeiraMagiaPonteAprendivel(
                bloqueadas = bloqueadasDaEscola,
                estado = estado,
                idsJaPlanejados = trilha.map { it.id }.toSet(),
                budget = budget
            ) ?: break
            trilha.add(ponte)
            estado = adicionarMagiaNoEstado(estado, ponte)
        }
        return trilha
    }

    private fun contarMagiasDaEscolaNoEstado(estado: Personagem, escolaNorm: String): Int {
        return estado.magias.count { magia ->
            magia.escola.orEmpty().map(::normalizarTexto).any { it == escolaNorm }
        }
    }

    private fun primeiraMagiaPonteAprendivel(
        bloqueadas: List<MagiaDefinicao>,
        estado: Personagem,
        idsJaPlanejados: Set<String>,
        budget: GuardrailBudget
    ): MagiaDefinicao? {
        val visitados = mutableSetOf<String>()
        val fila = ArrayDeque<Pair<MagiaDefinicao, Int>>()
        bloqueadas.sortedBy { prioridadeMagiaParaAlvo(it) }.forEach { fila.add(it to 0) }
        while (fila.isNotEmpty()) {
            if (!budget.step()) return null
            val (atual, profundidade) = fila.removeFirst()
            if (!budget.allowDepth(profundidade)) return null
            if (!visitados.add(atual.id)) continue
            val dependencias = dependenciasDiretasMagia(atual, budget)
            dependencias.forEach { dep ->
                if (!budget.step()) return null
                if (dep.id in idsJaPlanejados) return@forEach
                if (estado.magias.any { it.definicaoId == dep.id }) return@forEach
                if (magiaPodeSerAprendidaNoEstado(dep, estado)) return dep
                if (profundidade < budget.maxDepth) fila.add(dep to (profundidade + 1))
            }
        }
        return null
    }

    private fun dependenciasDiretasMagia(magia: MagiaDefinicao, budget: GuardrailBudget): List<MagiaDefinicao> {
        val prereq = dataRepository.preRequisitoNormalizadoParaAnalise(magia)
        if (prereq.isBlank()) return emptyList()
        val parsed = parseCached(prereq)
        val nomes = linkedSetOf<String>()
        parsed.tipos.forEach { tipo ->
            if (!budget.step()) return emptyList()
            when (tipo) {
                is PreRequisitoType.MagiaConhecida -> nomes.add(normalizarTexto(tipo.nomeMagia))
                is PreRequisitoType.MagiaInclusaNaContagem -> nomes.add(normalizarTexto(tipo.nomeMagia))
                else -> Unit
            }
        }
        if (nomes.isEmpty()) return emptyList()
        return dataRepository.magias
            .asSequence()
            .filter { candidata ->
                if (!budget.step()) return@filter false
                val nome = normalizarTexto(candidata.nome)
                nomes.any { req ->
                    req.isNotBlank() &&
                        (nome == req || nome.contains(req) || req.contains(nome))
                }
            }
            .sortedBy { prioridadeMagiaParaAlvo(it) }
            .toList()
    }

    private fun magiaPodeSerAprendidaNoEstado(definicao: MagiaDefinicao, estado: Personagem): Boolean {
        return dataRepository.validarPreRequisitosMagia(definicao, estado) == null
    }

    private fun adicionarMagiaNoEstado(estado: Personagem, definicao: MagiaDefinicao): Personagem {
        if (estado.magias.any { it.definicaoId == definicao.id }) return estado
        val nova = dataRepository.criarMagiaSelecionada(
            definicao = definicao,
            pontosGastos = 1,
            encantamentoAlvo = null,
            especializacaoMagia = null
        )
        return estado.copy(magias = estado.magias + nova)
    }

    private fun prioridadeMagiaParaAlvo(magia: MagiaDefinicao): Int {
        val pre = magia.preRequisitos.orEmpty().trim()
        if (pre.isBlank()) return 0
        val texto = normalizarTexto(pre)
        val conectores = Regex("\\bou\\b|\\be\\b|,").findAll(texto).count()
        return conectores + (texto.length / 30)
    }

    private fun limparTokenFamilia(valor: String): String {
        return valor
            .replace(Regex("\\b(incl|inclusive|outras|outra|diferentes|diferente)\\b"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun variantesFamilia(token: String): Set<String> {
        val base = limparTokenFamilia(normalizarTexto(token))
        if (base.isBlank()) return emptySet()
        val variantes = linkedSetOf(base)
        if (base.endsWith("s") && base.length > 3) variantes.add(base.dropLast(1))
        if (!base.endsWith("s")) variantes.add("${base}s")
        return variantes
    }

    private fun nomeCombinaFamilia(nomeNormalizado: String, token: String): Boolean {
        if (nomeNormalizado.isBlank() || token.isBlank()) return false
        return variantesFamilia(token).any { variante ->
            variante.isNotBlank() && (
                nomeNormalizado.contains(variante) ||
                    Regex("\\b${Regex.escape(variante)}\\b").containsMatchIn(nomeNormalizado)
                )
        }
    }

    private fun normalizarTexto(valor: String): String {
        val semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return semAcento
            .lowercase()
            .replace("-", " ")
            .replace(Regex("[^a-z0-9\\s/+_-]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun parseCached(raw: String): PreRequisitoParser.ParseResult {
        synchronized(this) {
            parseCache[raw]?.let { return it }
        }
        val parsed = PreRequisitoParser.parse(raw)
        synchronized(this) {
            parseCache[raw] = parsed
        }
        return parsed
    }
}
