package com.gurps.ficha.domain.rules

/**
 * **A forma de uma ficha técnica** — a mesma para arma, armadura e escudo.
 *
 * ## Por que a forma saiu de dentro da arma (Lote EQP-6)
 *
 * O `FichaTecnicaDaArma` nasceu no Lote ARMA-2 e trouxe as três estruturas
 * (`Linha`, `ModoNaTela`, `Ficha`) dentro de si, porque só a arma tinha ficha.
 * Quando o usuário pediu o mesmo padrão para armadura e escudo, havia dois
 * caminhos:
 *
 * 1. Dar a cada um a **sua** estrutura — e aí o `CardDetalheDoItem` precisaria de
 *    três versões, ou de um `when`. É como as seis cópias do `Card + Row` dos
 *    diálogos de seleção começaram.
 * 2. Ter **uma** forma e três montadores.
 *
 * O (2), e não por elegância: o card que desenha isto é o mesmo, com o mesmo
 * cabeçalho, os mesmos blocos e o mesmo botão. Se a forma fosse diferente, a
 * diferença apareceria na tela — e o usuário pediu exatamente o contrário.
 *
 * ⚠️ Aqui não há **nenhuma** regra de GURPS. Só o formato. As regras — o que é
 * destaque, o que é detalhe, o que a sigla significa — ficam em
 * [FichaTecnicaDaArma], [FichaTecnicaDaArmadura] e [FichaTecnicaDoEscudo], cada
 * uma com o seu teste.
 */
object FichaDeEquipamento {

    /**
     * O travessão de **campo ausente**.
     *
     * ⚠️ Zero é um dado; "não sei" é outro. Uma besta com Recuo 0 seria uma besta
     * que não coiceia; uma besta com Recuo `—` é uma arma para a qual o livro não
     * cadastrou Recuo nenhum.
     */
    const val AUSENTE = "—"

    /**
     * Uma linha da ficha: rótulo à esquerda, valor à direita.
     *
     * [explicacao] é o que transforma a sigla em regra — sem ela a tela fica
     * bonita e continua ilegível para quem não decorou a p.271.
     */
    data class Linha(
        val rotulo: String,
        val valor: String,
        val explicacao: String? = null
    ) {
        /**
         * Como o leitor de tela lê a linha inteira, de uma vez.
         *
         * ⚠️ Uma tabela lida célula a célula — "Precisão", pausa, "6 mais 3",
         * pausa — não diz nada. É preciso ouvir rótulo, valor e explicação juntos.
         */
        val descricaoAcessivel: String
            get() = listOfNotNull(
                "$rotulo: $valor".takeIf { valor.isNotBlank() },
                explicacao
            ).joinToString(". ")
    }

    /**
     * Um modo de ataque já traduzido para a tela.
     *
     * Só a arma usa — armadura e escudo não atacam. Fica aqui porque a `Ficha` é
     * uma só; para eles a lista vem vazia e o bloco simplesmente não aparece.
     */
    data class ModoNaTela(
        val ordem: Int,
        val dano: String,
        val danoComSt: String?,
        val detalhe: String?,
        /** `true` do 2º modo em diante: mesma arma, não se paga nem se carrega de novo. */
        val mesmaArma: Boolean
    )

    data class Ficha(
        val nome: String,
        val subtitulo: String,
        val selo: String?,
        /** O que se olha **no meio da jogada**. */
        val destaques: List<Linha>,
        val modos: List<ModoNaTela>,
        /** O que se olha **uma vez, na hora de comprar**. */
        val detalhes: List<Linha>,
        val observacoes: List<String>
    )

    // ──────────────────────────────────────────────────────────────────
    // Formatação — repetida em três montadores se não morasse aqui
    // ──────────────────────────────────────────────────────────────────

    /** `2.0` vira `2`; `0.125` vira `0,125` — vírgula, que é o livro em português. */
    fun formatarKg(v: Float): String =
        if (v == v.toLong().toFloat()) v.toLong().toString() else v.toString().replace('.', ',')

    /** `10000` vira `$10.000` — sem o ponto, uma armadura de $10000 lê como $1000. */
    fun formatarDinheiro(v: Float): String {
        val inteiro = v.toLong()
        val comPonto = inteiro.toString().reversed().chunked(3).joinToString(".").reversed()
        return "$$comPonto"
    }
}
