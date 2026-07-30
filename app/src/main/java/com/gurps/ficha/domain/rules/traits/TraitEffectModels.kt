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
 * Um bônus que só vale em certa situação, oferecido na hora da rolagem.
 *
 * A maioria dos bônus do GURPS é assim ("ao tentar parecer honesto", "quando
 * não quer ser visto"). Eles NÃO entram no NH base — quem decide se a situação
 * se aplica é o jogador, no momento do teste.
 *
 * @param alvo perícia (ou "esquiva"/"aparar"/"bloqueio") a que o bônus se aplica.
 * @param condicao texto do livro, mostrado ao jogador para ele decidir.
 */
data class BonusCondicional(
    val nomeDoTraco: String,
    val alvo: String,
    val valor: Int,
    val condicao: String
) {
    /** Rótulo para a caixa de seleção: `Rosto Sincero +1 — para parecer honesto`. */
    val rotulo: String
        get() = "$nomeDoTraco ${if (valor >= 0) "+$valor" else "$valor"} — $condicao"
}

/**
 * Atributo ou característica secundária alvo de um efeito.
 *
 * PV/PF/VEL/DESL são derivados (PV vem de ST, PF de HT...), mas o GURPS permite
 * mexer neles direto — por isso entram na mesma lista.
 */
enum class Atributo {
    ST, DX, IQ, HT, VONT, PER, PV, PF, VEL, DESL;

    companion object {
        /** Tolerante às grafias que aparecem no livro e no JSON escrito à mão. */
        fun de(texto: String?): Atributo? = when (texto?.trim()?.uppercase()) {
            "ST", "FORCA", "FORÇA" -> ST
            "DX", "DESTREZA" -> DX
            "IQ", "INTELIGENCIA", "INTELIGÊNCIA" -> IQ
            "HT", "VITALIDADE" -> HT
            "VONT", "VONTADE" -> VONT
            "PER", "PERCEPCAO", "PERCEPÇÃO" -> PER
            "PV", "HP" -> PV
            "PF", "FP" -> PF
            "VEL", "VELOCIDADE" -> VEL
            "DESL", "DESLOCAMENTO" -> DESL
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
    val escopo: String? = null,
    @SerializedName(value = "porOpcao", alternate = ["por_opcao"])
    val porOpcao: Map<String, Int>? = null,
    /**
     * Tabela indexada pelo **Número de Autocontrole** (Lote D-NA).
     *
     * ```json
     * "porAutocontrole": { "6": -4, "9": -3, "12": -2, "15": -1 }
     * ```
     *
     * As chaves são os quatro NAs do GURPS. ⚠️ **A tabela NÃO é a mesma em todo
     * lugar** — eu tinha anotado no plano que "se repete literalmente igual", e
     * está errado: o Egoísmo (MB p.137) usa −5/−4/−3/−2, um degrau pior, e a
     * Xenofilia (p.162) usa **+4/+3/+2/+1**, que é bônus. Por isso a tabela é
     * dado do catálogo, e não uma constante única no código.
     */
    @SerializedName(value = "porAutocontrole", alternate = ["por_autocontrole"])
    val porAutocontrole: Map<String, Int>? = null
) {
    val tipoResolvido: TipoEfeito? get() = TipoEfeito.de(tipo)
    val escopoResolvido: EscopoEfeito get() = EscopoEfeito.de(escopo)

    /** Bônus condicional depende de situação; não entra no valor base. */
    val ehCondicional: Boolean get() = !condicao.isNullOrBlank()

    /** Valor final considerando o nível do traço na ficha. */
    fun valorPara(nivel: Int): Int = if (porNivel) valor * nivel.coerceAtLeast(1) else valor

    /**
     * Valor final considerando TUDO que a ficha sabe do traço.
     *
     * Existe por causa dos traços que não têm nível e sim **degraus de custo**:
     * Aparência (4/12/16/20 pts → +1/+2/+2/+2 de reação, MB p.21) e Hábitos
     * Detestáveis (−5/−10/−15 → −1/−2/−3). Neles o `valor` fixo não serve, e
     * `porNivel` também não — o que muda o efeito é a OPÇÃO comprada.
     *
     * Analogia: `porNivel` é preço por quilo; `porOpcao` é tabela de tamanhos —
     * P, M e G não são múltiplos um do outro, cada um tem seu número.
     *
     * Se o custo da ficha não estiver na tabela (opção que o livro não prevê,
     * ou ficha antiga com 0), devolve **0** em vez de chutar. Preferir não dar
     * o bônus a dar o bônus errado é a mesma regra do resto do interpretador.
     */
    fun valorPara(selecao: TracoSelecionado): Int {
        // A ORDEM importa: um efeito declara uma tabela só. `porAutocontrole`
        // vem primeiro porque é o mais específico — quem tem NA não usa nível
        // nem degrau de custo para decidir a penalidade.
        porAutocontrole?.let { tabela ->
            val na = selecao.autocontrole ?: return 0
            return tabela[na.toString()] ?: 0
        }
        val tabela = porOpcao ?: return valorPara(selecao.nivel)
        return tabela[selecao.custoEscolhido.toString()] ?: 0
    }

    /** Se o efeito depende da faixa de custo escolhida pelo jogador. */
    val ehPorOpcao: Boolean get() = !porOpcao.isNullOrEmpty()

    /** Se o efeito depende do Número de Autocontrole da desvantagem. */
    val ehPorAutocontrole: Boolean get() = !porAutocontrole.isNullOrEmpty()

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
