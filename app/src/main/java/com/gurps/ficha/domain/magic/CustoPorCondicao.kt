package com.gurps.ficha.domain.magic

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import kotlin.math.ceil
import kotlin.math.floor

/**
 * **O custo que DEPENDE DA CONDIÇÃO** (peso, tamanho, material, alcance...) — `planos/magias/CUSTO-NA-HORA-DA-CONJURACAO.md`.
 *
 * Onde o livro escreve "1 por 0,5 kg de peso original, por duplicação da dimensão", não existe "o custo": existe uma
 * CONTA, e o número depende do que o jogador informa na hora de conjurar. O catálogo guarda, por magia, a lista do
 * que se pergunta ([CondicaoEntrada]) e a conta ([CustoPorCondicao.operar]/[CustoPorCondicao.manter]). A Mesa lê a
 * MESMA estrutura em JavaScript (`magia-energia.js`) e dá o MESMO número: os 73 casos de
 * `src/test/resources/custo_por_condicao_casos.json` rodam nos dois lados.
 *
 * ## A conta
 *
 * Expressão = número | `"$id"` (o valor de uma entrada) | `["op", expressão...]`, com `op` em `+ - * / cima max min`.
 * Vocabulário FECHADO: nada aqui executa texto. Um id que não existe, ou um operador desconhecido, dá erro e
 * nenhum PF é cobrado: o silêncio nunca vira "custo 0".
 *
 * ## Arredondamento (decisão dele, 19/out/2026): sempre PARA CIMA
 *
 * O livro só diz "1 por 0,5 kg (mínimo 0,5 kg)", "metade disso para manter" e, nas magias de área, "é necessário
 * gastar, no mínimo, um ponto". Onde sobra fração, sobe.
 */
data class CondicaoOpcao(
    val id: String = "",
    val rotulo: String = "",
    val valor: Double = 0.0
)

/** O que se pergunta ao jogador: `tipo` é `numero`, `inteiro`, `escolha` ou `marca`. */
data class CondicaoEntrada(
    val id: String = "",
    val tipo: String = "",
    val rotulo: String = "",
    val unidade: String? = null,
    val min: Double? = null,
    val max: Double? = null,
    val passo: Double? = null,
    /** O valor de partida: número, id da opção (escolha) ou nada (marca: desligada). */
    val padrao: JsonElement? = null,
    val opcoes: List<CondicaoOpcao>? = null
)

/**
 * A conta de uma magia: [entradas] (o que se pergunta), [operar] (a expressão do custo para operar) e [manter]
 * (`"igual"`, `"metade"`, `"nenhum"`, uma expressão com `$operar`, ou ausente: o livro não diz).
 */
data class CustoPorCondicao(
    val entradas: List<CondicaoEntrada> = emptyList(),
    val operar: JsonElement? = null,
    val manter: JsonElement? = null
)

object CustoPorCondicaoRules {

    /** Acima disto a conta não é de PF de gente: avisa em vez de cobrar (o mesmo teto da Mesa). */
    const val TETO_DA_CONTA = 99

    /** O resultado da conta: [Ok] com o custo, ou [Erro] com o porquê (e nada é cobrado). */
    sealed class Resultado {
        /** [manter] é nulo quando o livro não diz, e 0 com [manterTipo] `nenhum` quando a magia não se mantém. */
        data class Ok(
            val operar: Int,
            val manter: Int?,
            val manterTipo: String?,
            val valores: Map<String, Double>
        ) : Resultado()

        /** [erro]: `condicao_invalida` (com [campo], [rotulo] e [motivo]), `conta_invalida`, `custo_fora_da_faixa`. */
        data class Erro(
            val erro: String,
            val campo: String? = null,
            val rotulo: String? = null,
            val motivo: String = ""
        ) : Resultado()
    }

    /** Arredonda para cima, perdoando o ruído do ponto flutuante (`1.4000000000000001`). */
    internal fun paraCima(x: Double): Double = ceil(x - 1e-9)

    private fun ehNumero(x: Double): Boolean = !x.isNaN() && !x.isInfinite()

    private fun JsonElement?.comoTexto(): String? =
        if (this is JsonPrimitive && this.isString) this.asString else null

    private fun JsonElement?.comoNumero(): Double? =
        if (this is JsonPrimitive && this.isNumber) this.asDouble else null

    /** Os valores de partida: o `padrao` de cada entrada (a escolha pelo id da opção, a marca desligada). */
    fun condicoesPadrao(spec: CustoPorCondicao): Map<String, Any> = spec.entradas.associate { e ->
        e.id to when (e.tipo) {
            "marca" -> 0.0
            "escolha" -> e.padrao.comoTexto() ?: ""
            else -> e.padrao.comoNumero() ?: 0.0
        }
    }

    /**
     * Valida o que o jogador informou e o põe em número, entrada por entrada. Entrada que falta vale o padrão.
     * @param condicoes `id → valor`: número (`numero`/`inteiro`), id da opção (`escolha`), `Boolean`/0/1 (`marca`)
     */
    fun lerAsCondicoes(spec: CustoPorCondicao, condicoes: Map<String, Any?>): Pair<Map<String, Double>?, Resultado.Erro?> {
        val valores = LinkedHashMap<String, Double>()
        for (e in spec.entradas) {
            val cru: Any? = if (condicoes.containsKey(e.id)) condicoes[e.id] else condicoesPadrao(spec)[e.id]
            fun falha(motivo: String) = Resultado.Erro("condicao_invalida", e.id, e.rotulo, motivo)
            when (e.tipo) {
                "escolha" -> {
                    val op = e.opcoes.orEmpty().firstOrNull { it.id == cru } ?: return null to falha("opção desconhecida")
                    valores[e.id] = op.valor
                }
                "marca" -> {
                    val v = when (cru) {
                        true -> 1.0
                        false -> 0.0
                        is Number -> if (cru.toDouble() == 0.0 || cru.toDouble() == 1.0) cru.toDouble() else return null to falha("marca inválida")
                        else -> return null to falha("marca inválida")
                    }
                    valores[e.id] = v
                }
                else -> {
                    val n: Double = when (cru) {
                        is Number -> cru.toDouble()
                        is String -> cru.trim().replace(',', '.').toDoubleOrNull() ?: return null to falha("não é um número")
                        else -> return null to falha("não é um número")
                    }
                    if (!ehNumero(n)) return null to falha("não é um número")
                    if (e.tipo == "inteiro" && floor(n) != n) return null to falha("precisa ser um número inteiro")
                    if (e.min != null && n < e.min) return null to falha("abaixo do mínimo (${numeroEmTexto(e.min)})")
                    if (e.max != null && n > e.max) return null to falha("acima do máximo (${numeroEmTexto(e.max)})")
                    valores[e.id] = n
                }
            }
        }
        return valores to null
    }

    /** Avalia uma expressão do vocabulário fechado. Lança [IllegalArgumentException] em qualquer coisa fora dele. */
    internal fun avaliar(expr: JsonElement, valores: Map<String, Double>): Double {
        if (expr is JsonPrimitive) {
            if (expr.isNumber) return expr.asDouble
            if (expr.isString) {
                val t = expr.asString
                require(t.startsWith("$")) { "termo desconhecido: $t" }
                return valores[t.substring(1)] ?: throw IllegalArgumentException("valor desconhecido: $t")
            }
        }
        require(expr is JsonArray && expr.size() >= 2) { "expressão inválida" }
        val op = (expr[0] as? JsonPrimitive)?.takeIf { it.isString }?.asString ?: throw IllegalArgumentException("expressão inválida")
        val args = (1 until expr.size()).map { avaliar(expr[it], valores) }
        return when (op) {
            "cima" -> { require(args.size == 1) { "cima leva um argumento" }; paraCima(args[0]) }
            "-" -> { require(args.size == 2) { "- leva dois argumentos" }; args[0] - args[1] }
            "/" -> {
                require(args.size == 2) { "/ leva dois argumentos" }
                require(args[1] != 0.0) { "divisão por zero" }
                args[0] / args[1]
            }
            "+" -> args.fold(0.0) { a, b -> a + b }
            "*" -> args.fold(1.0) { a, b -> a * b }
            "max" -> args.max()
            "min" -> args.min()
            else -> throw IllegalArgumentException("operador desconhecido: $op")
        }
    }

    /** **A conta**: o custo para operar e para manter, com o que o jogador informou. */
    fun calcular(spec: CustoPorCondicao?, condicoes: Map<String, Any?>): Resultado {
        if (spec == null || spec.operar == null) return Resultado.Erro("conta_invalida", motivo = "sem conta")
        val (valores, erro) = lerAsCondicoes(spec, condicoes)
        if (erro != null || valores == null) return erro ?: Resultado.Erro("conta_invalida", motivo = "sem valores")
        val operar: Double
        var manter: Double? = null
        var manterTipo: String? = null
        try {
            operar = paraCima(avaliar(spec.operar, valores))
            val m = spec.manter
            val texto = m.comoTexto()
            when {
                texto == "igual" -> { manter = operar; manterTipo = "igual" }
                texto == "metade" -> { manter = paraCima(operar / 2); manterTipo = "metade" }
                texto == "nenhum" -> { manter = 0.0; manterTipo = "nenhum" }
                m != null && !m.isJsonNull -> {
                    manter = paraCima(avaliar(m, valores + ("operar" to operar)))
                    manterTipo = "conta"
                }
            }
        } catch (e: IllegalArgumentException) {
            return Resultado.Erro("conta_invalida", motivo = e.message ?: "conta inválida")
        }
        if (!ehNumero(operar) || operar < 0 || (manter != null && (!ehNumero(manter) || manter < 0))) {
            return Resultado.Erro("conta_invalida", motivo = "resultado fora do possível")
        }
        if (operar > TETO_DA_CONTA || (manter != null && manter > TETO_DA_CONTA)) {
            return Resultado.Erro("custo_fora_da_faixa", motivo = "a conta deu ${operar.toInt()}, acima de $TETO_DA_CONTA PF")
        }
        return Resultado.Ok(operar.toInt(), manter?.toInt(), manterTipo, valores)
    }

    /** "1,5 kg": o número do jeito brasileiro, com a unidade. */
    fun numeroEmTexto(n: Double, unidade: String? = null): String {
        val arredondado = Math.round(n * 1000.0) / 1000.0
        val t = (if (arredondado == floor(arredondado) && !arredondado.isInfinite()) arredondado.toLong().toString() else arredondado.toString())
            .replace('.', ',')
        return if (unidade.isNullOrBlank()) t else "$t $unidade"
    }

    /** O que foi informado, em uma frase: "Peso original do objeto 1,5 kg; Duplicações da dimensão escolhida 2". */
    fun resumo(spec: CustoPorCondicao, condicoes: Map<String, Any?>): String {
        val (valores, erro) = lerAsCondicoes(spec, condicoes)
        if (erro != null || valores == null) return ""
        val padrao = condicoesPadrao(spec)
        return spec.entradas.mapNotNull { e ->
            val v = valores[e.id] ?: return@mapNotNull null
            when (e.tipo) {
                "escolha" -> {
                    val id = if (condicoes.containsKey(e.id)) condicoes[e.id] else padrao[e.id]
                    "${e.rotulo}: ${e.opcoes.orEmpty().firstOrNull { it.id == id }?.rotulo ?: id}"
                }
                "marca" -> if (v != 0.0) e.rotulo else null
                else -> "${e.rotulo} ${numeroEmTexto(v, e.unidade)}"
            }
        }.joinToString("; ")
    }
}
