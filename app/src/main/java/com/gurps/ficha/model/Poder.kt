package com.gurps.ficha.model

import androidx.compose.runtime.Stable
import com.google.gson.annotations.SerializedName
import com.gurps.ficha.domain.rules.poderes.RegrasDePoder
import com.gurps.ficha.domain.rules.poderes.ReservaDeEnergia

/**
 * Um poder **na ficha do personagem**. Lotes POD-1 a POD-3.
 *
 * ⚠️ `modificadorDePoder` continua sendo um número gravado aqui, e não uma
 * conta feita na hora, porque o Mestre pode montar um modificador próprio
 * somando componentes (Poderes, p.20-25). O que mudou é **de onde ele vem por
 * padrão**: escolher a [fonte] no catálogo preenche o percentual do livro, em
 * vez de o jogador ter de saber o número de cabeça.
 */
@Stable
data class Poder(
    val id: String = java.util.UUID.randomUUID().toString(),
    var nome: String = "",
    var fonte: String = "",
    var foco: String = "",
    var modificadorDePoder: Int = 0, // Ex: -10 para -10%
    var nivelTalento: Int = 0,
    var custoTalentoNivel: Int = RegrasDePoder.CUSTO_PADRAO_POR_NIVEL,
    /**
     * Reserva de Energia deste poder, em PF (Poderes, p.119). Lote POD-9.
     * Zero = o poder usa os PF normais.
     */
    var reservaDeEnergia: Int = 0,
    /** Ids de [ReservaDeEnergia.Limitacao] escolhidas para a RE. */
    var limitacoesDaReserva: List<String> = emptyList()
) {
    /**
     * 🔴 Existia desde sempre e **não era chamado em lugar nenhum** — o Talento
     * do poder nunca custou ponto. Ligado no POD-3, entra em `pontosGastos`.
     */
    val custoTotalTalento: Int
        get() = RegrasDePoder.custoDoTalento(nivelTalento, custoTalentoNivel)

    /** `null` quando está dentro do teto do livro. */
    val avisoDeTalento: String? get() = RegrasDePoder.avisoDoTeto(nivelTalento)

    /** As limitações da RE já resolvidas — ignora id que não existe mais. */
    val limitacoesDaReserveResolvidas: Set<ReservaDeEnergia.Limitacao>
        get() = limitacoesDaReserva.mapNotNull { id ->
            ReservaDeEnergia.Limitacao.entries.firstOrNull { it.name == id }
        }.toSet()

    /**
     * O custo em pontos da Reserva de Energia. Lote POD-9.
     *
     * ⚠️ Entra em `pontosGastos` junto com o Talento: a RE **é** uma vantagem
     * comprada (*"trate-os como uma nova vantagem"*, p.119), não um recurso de
     * graça.
     */
    val custoDaReserva: Int
        get() = ReservaDeEnergia.custo(reservaDeEnergia, limitacoesDaReserveResolvidas)

    val descricaoAcessivel: String
        get() = buildString {
            append(nome)
            if (fonte.isNotBlank()) append(". Fonte $fonte")
            if (foco.isNotBlank()) append(". Foco $foco")
            append(". Modificador de poder ")
            append(if (modificadorDePoder < 0) "menos ${-modificadorDePoder}" else "$modificadorDePoder")
            append(" por cento")
            if (nivelTalento > 0) append(". Talento nível $nivelTalento, $custoTotalTalento pontos")
        }
}

/** Uma fonte genérica do livro: o par nome/percentual de Poderes, p.26-30. */
@Stable
data class FonteDePoderDefinicao(
    val id: String = "",
    val nome: String = "",
    val valor: Int = 0,
    val pagina: Int = 0,
    val descricao: String = ""
)

/** Uma das fontes que **este** poder aceita, com o valor que ela vale nele. */
@Stable
data class FonteDoPoder(
    val fonte: String = "",
    val valor: Int = 0
)

/**
 * Um poder **do catálogo** (GURPS Poderes, cap. 8, p.121-136).
 *
 * ## 🔴 O que este verbete conserta
 *
 * O catálogo antigo tinha 44 entradas e errava de quatro jeitos: **13 nomes
 * truncados** (`"de Força"` era *Construtos de Força*; `"Animais"` era *Controle
 * de Animais*), **4 poderes faltando** (Controle da Matéria, Cósmico, Divino,
 * Magia), **uma linha de gabarito** virada item (`nome: "poder."`), e
 * `modificadorDePoder = 0` com `pagina = 121` nas 44.
 *
 * O campo que mais importa é [modificadores]: as fontes que o livro aceita para
 * este poder, **cada uma com o seu percentual**.
 */
@Stable
data class PoderDefinicao(
    val id: String = "",
    val nome: String = "",
    @SerializedName("fontes_possiveis") val fontesPossiveis: String = "",
    val foco: String = "",
    val descricao: String = "",
    @SerializedName("custo_talento_por_nivel") val custoTalentoPorNivel: Int =
        RegrasDePoder.CUSTO_PADRAO_POR_NIVEL,
    val modificadores: List<FonteDoPoder> = emptyList(),
    /**
     * As vantagens que o livro sugere para este poder, já com os modificadores
     * recomendados: *"Anfíbio; Caminhar no Ar, com Específico, Vapor (-40%);
     * Controle (Água)…"* (Água, p.121). Lote POD-10.
     *
     * 🔴 Dois poderes têm a lista **vazia de propósito**, e isso é do livro:
     * Cósmico (*"qualquer vantagem pode ser uma habilidade Cósmica"*) e Magia,
     * cujo verbete manda usar outro poder. Nos dois, a explicação está em
     * [notaDasHabilidades] — vazio não é falha de extração.
     */
    val habilidades: List<String> = emptyList(),
    @SerializedName("nota_das_habilidades") val notaDasHabilidades: String = "",
    val pagina: Int = 0
) {
    fun normalizada(): PoderDefinicao = copy(nome = nome.trim())

    /** A fonte sugerida quando o jogador ainda não escolheu: a primeira do livro. */
    val fontePadrao: FonteDoPoder? get() = modificadores.firstOrNull()

    fun valorDaFonte(nome: String?): Int? =
        modificadores.firstOrNull { it.fonte.equals(nome?.trim(), ignoreCase = true) }?.valor
            ?: RegrasDePoder.valorDaFonte(nome)
}
