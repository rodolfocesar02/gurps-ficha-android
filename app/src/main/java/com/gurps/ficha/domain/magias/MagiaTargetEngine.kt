package com.gurps.ficha.domain.magias

import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.model.MagiaDefinicao
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.regras_prerequisitos.PreRequisitoParser
import java.util.LinkedHashMap

class MagiaTargetEngine(
    private val dataRepository: DataRepository
) {
    private val parseCache = object : LinkedHashMap<String, PreRequisitoParser.ParseResult>(512, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, PreRequisitoParser.ParseResult>?): Boolean {
            return size > 512
        }
    }

    private val modoAlvoCache = object : LinkedHashMap<String, ModoAlvoResult>(196, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ModoAlvoResult>?): Boolean {
            return size > 196
        }
    }

    data class ModoAlvoResult(
        val ids: List<String>,
        val parcial: Boolean = false,
        val aviso: String? = null
    )

    private data class GuardrailBudget(
        val startedAtMs: Long = System.currentTimeMillis(),
        val maxMs: Long = 1800,
        val maxNodes: Int = 18000,
        val maxDepth: Int = 6,
        var nodes: Int = 0,
        var limiteMotivo: String? = null
    ) : MagiaDependencyPlanner.Budget {
        override fun step(amount: Int): Boolean {
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

        override fun allowDepth(depth: Int): Boolean {
            if (depth > maxDepth) {
                limiteMotivo = "limite de profundidade"
                return false
            }
            return true
        }

        fun avisoParcial(): String {
            val motivo = limiteMotivo ?: "limite de segurança"
            return "Trilha parcial (guardrail: $motivo)."
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

        val ids = mutableListOf<String>()
        ids.add(alvo.id)
        if (dataRepository.magiaSemPreRequisito(alvo)) {
            val result = ModoAlvoResult(ids = ids)
            if (chaveCache != null) synchronized(this) { modoAlvoCache[chaveCache] = result }
            return result
        }

        val budget = GuardrailBudget()
        val planner = MagiaDependencyPlanner(
            dataRepository = dataRepository,
            parseFn = ::parseCached,
            budget = budget
        )
        val plan = planner.planForTarget(alvo, personagem)
        plan.prerequisiteIds.forEach { id ->
            if (id !in ids) ids.add(id)
        }

        val result = if (plan.parcial || budget.limiteMotivo != null) {
            ModoAlvoResult(
                ids = ids,
                parcial = true,
                aviso = budget.avisoParcial()
            )
        } else {
            ModoAlvoResult(ids = ids)
        }

        if (chaveCache != null) {
            synchronized(this) {
                modoAlvoCache[chaveCache] = result
            }
        }
        return result
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
