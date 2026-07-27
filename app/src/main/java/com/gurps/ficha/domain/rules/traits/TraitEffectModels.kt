package com.gurps.ficha.domain.rules.traits

import com.google.gson.annotations.SerializedName

/**
 * Modelos do campo `efeitos`, declarado em vantagens.v3.json / desvantagens.v2.json.
 *
 * Arquitetura HÍBRIDA (decisão do usuário em 2026-07-27): bônus simples são
 * DADO no JSON, lidos por um interpretador único; casos complexos continuam
 * sendo classe Kotlin. Ver `docs/pendencias/Automações_Vantagens.md` §10.
 *
 * Vai para JSON quando as três valem: é bônus numérico fixo, sobre alvo
 * nomeado, sem escolha do jogador e sem precisar ler outra parte da ficha.
 * Qualquer outra coisa é Kotlin.
 */

/** O que o efeito atinge. */
enum class TipoEfeito {
    @SerializedName("pericia") PERICIA,
    @SerializedName("defesa") DEFESA,
    @SerializedName("atributo") ATRIBUTO;

    companion object {
        /** Tolerante a maiúscula/acento — o JSON é escrito à mão. */
        fun de(texto: String?): TipoEfeito? = when (texto?.trim()?.lowercase()) {
            "pericia", "perícia" -> PERICIA
            "defesa" -> DEFESA
            "atributo" -> ATRIBUTO
            else -> null
        }
    }
}

/**
 * Parte do corpo a que o efeito se restringe.
 *
 * Existe desde o começo porque o problema apareceu nos DOIS planos e foi adiado
 * duas vezes: `st_bracal` dá +1 ST só dos braços e `sem_um_dedo` dá −1 DX só
 * daquela mão. Sem esta dimensão, esses casos ficariam eternamente pendentes.
 *
 * O interpretador ainda NÃO aplica efeito com escopo diferente de GLOBAL —
 * exige decidir como um bônus por membro entra no cálculo. Até lá o efeito é
 * ignorado com aviso, em vez de ser aplicado errado.
 */
enum class EscopoEfeito {
    @SerializedName("global") GLOBAL,
    @SerializedName("bracos") BRACOS,
    @SerializedName("mao_habil") MAO_HABIL,
    @SerializedName("mao_inabil") MAO_INABIL,
    @SerializedName("pernas") PERNAS;

    companion object {
        fun de(texto: String?): EscopoEfeito = when (texto?.trim()?.lowercase()) {
            null, "", "global" -> GLOBAL
            "bracos", "braços" -> BRACOS
            "mao_habil", "mão_hábil" -> MAO_HABIL
            "mao_inabil", "mão_inábil" -> MAO_INABIL
            "pernas" -> PERNAS
            else -> GLOBAL
        }
    }
}

/**
 * Um efeito declarado no catálogo.
 *
 * ```json
 * { "tipo": "pericia", "alvo": "Escalada", "valor": 2 }
 * { "tipo": "pericia", "alvo": "Furtividade", "valor": 2, "porNivel": true,
 *   "condicao": "quando não quer ser visto" }
 * ```
 *
 * @param alvo para `pericia`, o NOME EXATO como está em pericias.json — o
 *   casamento é por nome, então "Navegação" não pega "Navegação (Ar)".
 * @param porNivel multiplica o valor pelo nível da vantagem selecionada.
 * @param condicao quando presente, o bônus NÃO entra no NH base: só vale em
 *   certas situações ("ao tentar parecer honesto"). Aplicar sempre seria errado
 *   e inflaria a ficha. Enquanto a UI de escolha na rolagem não existir, o
 *   interpretador ignora esses efeitos.
 */
data class EfeitoDeclarado(
    val tipo: String = "",
    val alvo: String = "",
    val valor: Int = 0,
    @SerializedName(value = "porNivel", alternate = ["por_nivel"])
    val porNivel: Boolean = false,
    val condicao: String? = null,
    val escopo: String? = null
) {
    val tipoResolvido: TipoEfeito? get() = TipoEfeito.de(tipo)
    val escopoResolvido: EscopoEfeito get() = EscopoEfeito.de(escopo)

    /** Bônus condicional depende de situação; não entra no valor base. */
    val ehCondicional: Boolean get() = !condicao.isNullOrBlank()

    /** Valor final considerando o nível do traço na ficha. */
    fun valorPara(nivel: Int): Int = if (porNivel) valor * nivel.coerceAtLeast(1) else valor

    /**
     * Uma linha legível do efeito, para o contexto enviado à IA:
     * `+2 Escalada`, `-5 Lábia`, `+1 Esquiva`, `+2 Furtividade (por nível)`,
     * `+1 Dissimulação [só para parecer honesto]`.
     *
     * A condição entra entre colchetes de propósito: o Narrador precisa saber
     * que aquele bônus NÃO está somado na ficha, e em que situação vale.
     */
    fun resumo(nivel: Int = 1): String {
        val v = valorPara(nivel)
        val sinal = if (v >= 0) "+$v" else "$v"
        val porNivelTxt = if (porNivel && nivel <= 1) " (por nível)" else ""
        val condicaoTxt = if (ehCondicional) " [só $condicao]" else ""
        val escopoTxt = if (escopoResolvido != EscopoEfeito.GLOBAL) {
            " (${escopoResolvido.name.lowercase().replace('_', ' ')})"
        } else ""
        return "$sinal $alvo$porNivelTxt$escopoTxt$condicaoTxt"
    }
}
