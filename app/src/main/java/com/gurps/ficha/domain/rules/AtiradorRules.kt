package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem

/**
 * **Atirador** e **Atirador (Arqueiro Heroico)** — as duas vantagens
 * cinematográficas de tiro. MB p.43 e p.45.
 *
 * ## O que elas faziam antes deste lote: nada
 *
 * As duas estavam no catálogo com a descrição inteira e **zero automação** — sem
 * `efeitos`, sem `specialRule`, sem regra Kotlin. O jogador pagava 25 (ou 20)
 * pontos, lia o texto e fazia a conta na mão.
 *
 * ## Por que só agora deu para automatizar
 *
 * O primeiro benefício depende de saber se a arma é de **uma ou duas mãos** e
 * qual a **CdT**. Os dois campos só chegaram ao modelo no **Lote ARMA-1**. Antes
 * dele não havia como calcular — daria para escrever a regra, não para alimentá-la.
 *
 * ## ⚠️ As duas NÃO são a mesma vantagem com nome diferente
 *
 * | | Atirador (25) | Arqueiro Heroico (20) |
 * |---|---|---|
 * | Perícias | Armas de Feixe, Armas de Fogo, Canhoneiro, Projetor de Líquidos | Arco |
 * | Prec sem Apontar | inteira (1 mão) / **metade** (2 mãos ou automática) | **inteira**, sem metade |
 * | Segundos de Apontar | os normais (+1 com 2s, +2 com 3s+) | 🔴 **+1 com 1s, +2 com 2s+** |
 *
 * O terceiro item é o mais fácil de deixar passar: o arqueiro acumula os segundos
 * **um turno mais cedo** que a regra geral. Reaproveitar o `bonusPorTurnos` sem
 * olhar daria a ele o ritmo de todo mundo.
 */
object AtiradorRules {

    const val ID_ATIRADOR = "atirador"
    const val ID_ARQUEIRO_HEROICO = "atirador_arqueiro_heroico"

    /** A partir de que CdT a arma conta como **automática** (MB p.43: "CdT 1–3"). */
    const val CDT_MAXIMO_TIRO_A_TIRO = 3

    enum class Estilo { NENHUM, ATIRADOR, ARQUEIRO_HEROICO }

    /**
     * As perícias que o **Atirador** cobre (MB p.43). Prefixo, porque o id da
     * tela vem com a especialização colada (`armas_de_fogo_nt_pistola`).
     *
     * ⚠️ `canhoneiro_nt` esteve **fora** da lista de perícias à distância até o
     * Lote ARMA-6 — sem aquele conserto, um quarto desta vantagem não teria como
     * funcionar.
     */
    private val PERICIAS_DO_ATIRADOR = listOf(
        "armas_de_fogo_nt",
        "armas_de_feixe_nt",
        "canhoneiro_nt",
        "projetor_de_liquidos_nt"
    )

    /** O Arqueiro Heroico cobre **Arco**, e só (MB p.45). */
    private val PERICIAS_DO_ARQUEIRO = listOf("arco", "arcos")

    fun tem(personagem: Personagem, id: String): Boolean =
        personagem.vantagensTotais.any { it.definicaoId.equals(id, ignoreCase = true) }

    private fun normalizar(periciaId: String?): String =
        periciaId?.lowercase()?.removePrefix("racial_")?.trim().orEmpty()

    /**
     * Qual das duas vantagens vale **para este ataque**.
     *
     * A decisão é pela **perícia**, não pelo tipo da arma, porque é assim que o
     * livro escreve: *"qualquer arma que utilize as perícias…"*.
     *
     * ⚠️ *"Nenhum desses benefícios se aplica quando o personagem está usando
     * **armas motoras de projétil**"* (MB p.43) — arco, besta, funda e zarabatana
     * ficam de fora do Atirador. É o erro mais provável de quem lê rápido, e por
     * isso a checagem é por lista fechada, não por "é à distância".
     */
    fun estiloDe(personagem: Personagem?, periciaId: String?): Estilo {
        if (personagem == null) return Estilo.NENHUM
        val id = normalizar(periciaId)
        if (id.isBlank()) return Estilo.NENHUM
        if (tem(personagem, ID_ATIRADOR) && PERICIAS_DO_ATIRADOR.any { id.startsWith(it) }) {
            return Estilo.ATIRADOR
        }
        if (tem(personagem, ID_ARQUEIRO_HEROICO) && PERICIAS_DO_ARQUEIRO.any { id.startsWith(it) }) {
            return Estilo.ARQUEIRO_HEROICO
        }
        return Estilo.NENHUM
    }

    /**
     * 🔴 **A Precisão que entra SEM Apontar.**
     *
     * > Quando estiver disparando um tiro por vez (CdT 1–3) usando armas de uma
     * > mão, o personagem recebe o bônus de Precisão da arma mesmo sem usar a
     * > manobra Apontar. Quando estiver utilizando armas de duas mãos ou
     * > automáticas, recebe **metade** do bônus de Precisão (**arredondado para
     * > cima**). — MB p.43
     *
     * ⚠️ O arredondamento é **para cima**: Prec 5 num rifle dá **3**, não 2.
     *
     * ⚠️ O Arqueiro Heroico **não** tem a metade — o arco é de duas mãos e ele
     * recebe a Prec inteira mesmo assim (MB p.45).
     */
    fun precisaoSemApontar(
        estilo: Estilo,
        precisao: Int?,
        duasMaos: Boolean,
        cadenciaTiro: Int?
    ): Int {
        val prec = precisao?.takeIf { it > 0 } ?: return 0
        return when (estilo) {
            Estilo.NENHUM -> 0
            Estilo.ARQUEIRO_HEROICO -> prec
            Estilo.ATIRADOR -> {
                val automatica = (cadenciaTiro ?: 1) > CDT_MAXIMO_TIRO_A_TIRO
                if (duasMaos || automatica) (prec + 1) / 2 else prec
            }
        }
    }

    /** `true` quando a metade do livro está sendo aplicada — o rótulo precisa dizer. */
    fun aplicouMetade(estilo: Estilo, duasMaos: Boolean, cadenciaTiro: Int?): Boolean =
        estilo == Estilo.ATIRADOR &&
            (duasMaos || (cadenciaTiro ?: 1) > CDT_MAXIMO_TIRO_A_TIRO)

    /**
     * 🔴 Os segundos extras de Apontar, que **mudam** com o Arqueiro Heroico.
     *
     * Regra geral (MB p.364): +1 com **dois** segundos, +2 com **três** ou mais.
     * Arqueiro Heroico (MB p.45): +1 com **um**, +2 com **dois** ou mais.
     */
    fun bonusPorTurnos(estilo: Estilo, turnos: Int): Int = when (estilo) {
        Estilo.ARQUEIRO_HEROICO -> when {
            turnos <= 0 -> 0
            turnos == 1 -> 1
            else -> 2
        }
        else -> ApontarRules.bonusPorTurnos(turnos)
    }

    /**
     * ⚠️ **A troca que é fácil errar.**
     *
     * O livro diz, duas vezes: *"Tudo isso é **em vez de** receber o bônus da
     * Prec"*. Com Atirador, ignorar o Avançar e Atacar e ganhar a Precisão de
     * graça são **exclusivos** — somar os dois seria dar duas vantagens pelo
     * preço de uma.
     */
    fun ignoraAvancarEAtacar(estilo: Estilo): Boolean = estilo != Estilo.NENHUM

    /**
     * O que de fato entra no NH, já resolvida a exclusividade.
     *
     * @param avancarEAtacar o jogador marcou a manobra de atacar em movimento.
     * @param apontou já está acumulando segundos (aí a Prec vem pelo Apontar,
     *   não por aqui — dar as duas contaria a Precisão duas vezes).
     */
    fun bonusNoAtaque(
        estilo: Estilo,
        precisao: Int?,
        duasMaos: Boolean,
        cadenciaTiro: Int?,
        avancarEAtacar: Boolean,
        apontou: Boolean
    ): Int {
        if (estilo == Estilo.NENHUM) return 0
        if (avancarEAtacar) return 0
        if (apontou) return 0
        return precisaoSemApontar(estilo, precisao, duasMaos, cadenciaTiro)
    }

    /** O rótulo da linha na tela, dizendo o que a vantagem está fazendo agora. */
    fun rotulo(
        estilo: Estilo,
        precisao: Int?,
        duasMaos: Boolean,
        cadenciaTiro: Int?,
        avancarEAtacar: Boolean,
        apontou: Boolean
    ): String? {
        if (estilo == Estilo.NENHUM) return null
        val nome = if (estilo == Estilo.ARQUEIRO_HEROICO) "Arqueiro Heroico" else "Atirador"

        if (avancarEAtacar) {
            return "$nome: penalidade de Avançar e Atacar ignorada — e por isso a " +
                "Precisão não entra de graça (MB p.43)"
        }
        if (apontou) {
            return "$nome: apontando, vale o bônus cheio de Precisão" +
                if (estilo == Estilo.ARQUEIRO_HEROICO) {
                    ", e os segundos contam um turno mais cedo"
                } else ""
        }
        val bonus = precisaoSemApontar(estilo, precisao, duasMaos, cadenciaTiro)
        if (bonus <= 0) {
            return "$nome: esta arma não tem Precisão cadastrada"
        }
        val motivo = if (aplicouMetade(estilo, duasMaos, cadenciaTiro)) {
            val porque = if (duasMaos) "arma de duas mãos" else "arma automática"
            " (metade de ${precisao ?: 0}, $porque)"
        } else ""
        return "$nome: +$bonus de Precisão sem precisar Apontar$motivo"
    }

    fun rotuloAcessivel(
        estilo: Estilo,
        precisao: Int?,
        duasMaos: Boolean,
        cadenciaTiro: Int?,
        avancarEAtacar: Boolean,
        apontou: Boolean
    ): String =
        rotulo(estilo, precisao, duasMaos, cadenciaTiro, avancarEAtacar, apontou)
            ?: "Sem vantagem de tiro cinematográfico neste ataque."
}
