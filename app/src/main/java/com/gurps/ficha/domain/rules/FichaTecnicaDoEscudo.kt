package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.model.EscudoCatalogoItem

/**
 * **A ficha técnica do escudo** — MB p.288. Lote EQP-6.
 *
 * Mesma divisão da arma e da armadura: o que vale **no meio da jogada** em cima,
 * o que vale **na hora de comprar** embaixo, e as notas do livro no fim.
 *
 * ## ⚠️ As duas RD do escudo são coisas diferentes
 *
 * O catálogo traz `RD/PV 9/60` para o Escudo Grande. Essa RD **não protege o
 * personagem** — protege o escudo, e só entra em jogo com a regra opcional *Dano
 * a Escudos* (p.484). Quem protege o personagem é o **BD**.
 *
 * Mostrar as duas lado a lado sem dizer isso seria convidar o jogador a somar RD
 * 9 na defesa dele. É por isso que a RD do escudo entra em *"na hora de comprar"*
 * e não nos destaques: no meio da jogada ela quase nunca é a resposta.
 */
object FichaTecnicaDoEscudo {

    /** [peca] = o escudo que o jogador tem; ver o porquê em [FichaTecnicaDaArmadura.de]. */
    fun de(
        escudo: EscudoCatalogoItem,
        stDoPersonagem: Int,
        peca: Equipamento? = null
    ): FichaDeEquipamento.Ficha =
        FichaDeEquipamento.Ficha(
            nome = peca?.nome?.takeIf { it.isNotBlank() } ?: escudo.nome,
            subtitulo = listOfNotNull("Escudo", escudo.nt?.let { "NT $it" }).joinToString(" · "),
            selo = escudo.cl?.let { "CL $it" },
            destaques = defesa(escudo, stDoPersonagem, peca),
            modos = emptyList(),
            detalhes = compra(escudo, peca),
            observacoes = NotasDoEscudo.explicar(escudo.observacoes)
        )

    // ──────────────────────────────────────────────────────────────────
    // No meio da jogada
    // ──────────────────────────────────────────────────────────────────

    private fun defesa(
        escudo: EscudoCatalogoItem,
        st: Int,
        peca: Equipamento?
    ): List<FichaDeEquipamento.Linha> {
        val linhas = mutableListOf<FichaDeEquipamento.Linha>()

        val bd = peca?.bonusDefesa ?: escudo.db
        linhas += FichaDeEquipamento.Linha(
            "BD",
            "+$bd",
            "soma em Bloqueio, Esquiva e Aparar contra ataques pela frente e pelos lados",
            // Campo no editor desde o EQP-8: escudo encantado de +1 BD nao
            // tinha onde ser registrado.
            editavel = true
        )
        // O escudo ocupa a mão mesmo sem ser usado para bloquear — é a
        // consequência que mais muda a jogada e não estava em lugar nenhum.
        linhas += FichaDeEquipamento.Linha(
            "Mão",
            "ocupada",
            "a mão do escudo não empunha arma, o que impede armas de duas mãos"
        )

        StMinimaDaArma.avaliar(st, escudo.stMinimo)?.let { falta ->
            linhas += FichaDeEquipamento.Linha(
                "ST mínima",
                "${escudo.stMinimo}",
                StMinimaDaArma.aviso(falta)
            )
        }
        return linhas
    }

    // ──────────────────────────────────────────────────────────────────
    // Na hora de comprar
    // ──────────────────────────────────────────────────────────────────

    private fun compra(escudo: EscudoCatalogoItem, peca: Equipamento?): List<FichaDeEquipamento.Linha> = listOf(
        FichaDeEquipamento.Linha(
            "Peso",
            (peca?.peso ?: escudo.pesoKg)?.let { "${FichaDeEquipamento.formatarKg(it)} kg" }
                ?: FichaDeEquipamento.AUSENTE,
            "entra na carga — um Escudo Grande sozinho já pesa mais que uma espada",
            editavel = true
        ),
        FichaDeEquipamento.Linha(
            "Custo",
            (peca?.custo ?: escudo.custo)?.let { FichaDeEquipamento.formatarDinheiro(it) }
                ?: FichaDeEquipamento.AUSENTE,
            editavel = true
        ),
        FichaDeEquipamento.Linha(
            "NT",
            escudo.nt?.toString() ?: FichaDeEquipamento.AUSENTE,
            "nível tecnológico em que o escudo é encontrado com facilidade"
        ),
        // 🔴 A linha que mais precisa da explicação. `RD 9` num escudo, ao lado
        // de uma armadura de RD 2, convida a somar 9 na defesa do personagem —
        // e essa RD protege o ESCUDO, não ele.
        FichaDeEquipamento.Linha(
            "RD / PV do escudo",
            listOfNotNull(escudo.rdDoEscudo?.toString(), escudo.pv?.toString())
                .joinToString(" / ")
                .ifBlank { FichaDeEquipamento.AUSENTE },
            "quanto o escudo aguenta antes de quebrar — não protege você. " +
                "Só vale com a regra opcional Dano a Escudos (p.484)"
        )
    )
}
