package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem

/**
 * Desvantagens que impõem **teto** a um atributo (Lote TETO-HT).
 *
 * Não é bônus nem penalidade: é limite de criação de personagem. `Magro` diz que
 * *"o valor da HT do personagem não pode ser superior a 14"*; `Muito Gordo`
 * baixa esse teto para 13 (MB p.19).
 *
 * Por isso não cabe no campo `efeitos` — ele soma e subtrai, não limita.
 *
 * **Avisa, não impede.** O jogador pode ter comprado a HT antes da desvantagem,
 * ou o Mestre pode ter liberado. Bloquear a ficha por causa disso seria repetir
 * o erro do `conhecimento_oculto`. O app aponta o conflito e deixa a decisão com
 * a mesa — que é quem manda.
 *
 * Quando há mais de uma, **vale o teto mais baixo**: quem é Magro e Muito Gordo
 * ao mesmo tempo não existe, mas se a ficha tiver as duas o app não pode escolher
 * a mais generosa em silêncio.
 */
object TetoDeAtributoRules {

    /** Um teto violado, pronto para virar aviso na tela. */
    data class Violacao(
        val atributo: String,
        val valorAtual: Int,
        val teto: Int,
        val origem: String
    ) {
        val aviso: String
            get() = "$origem limita a $atributo em $teto, e a ficha está com $valorAtual."
    }

    /** id da desvantagem → teto de HT que ela impõe (MB p.19). */
    private val TETOS_DE_HT = mapOf(
        "magro" to 14,
        "muito_gordo" to 13
    )

    /**
     * Os tetos violados nesta ficha. Lista vazia = tudo certo.
     *
     * Lê `personagem.ht`, que é o valor final — o teto do livro é sobre o valor
     * que o personagem tem, não sobre os pontos gastos.
     */
    fun violacoes(personagem: Personagem): List<Violacao> {
        val comTeto = personagem.desvantagens
            .mapNotNull { d -> TETOS_DE_HT[d.definicaoId]?.let { teto -> d.nome to teto } }
        if (comTeto.isEmpty()) return emptyList()

        // Mais de uma desvantagem com teto: manda a mais restritiva.
        val (nome, teto) = comTeto.minByOrNull { it.second }!!
        val ht = personagem.ht
        return if (ht > teto) {
            listOf(Violacao(atributo = "HT", valorAtual = ht, teto = teto, origem = nome))
        } else {
            emptyList()
        }
    }
}
