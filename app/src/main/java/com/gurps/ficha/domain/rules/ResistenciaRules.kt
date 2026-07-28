package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem

/**
 * Os testes de **resistir** que a ficha sabe montar (Lote RESIST-1).
 *
 * O GURPS é cheio de teste que não é perícia nem atributo puro: manter
 * consciência, evitar a morte, resistir a doença, veneno, medo. Antes deste
 * lote a aba Rolagem não tinha onde pô-los — o Teste de Reação e o Autocontrole
 * acabaram como painéis soltos no fim da tela, e o resto simplesmente não
 * existia.
 *
 * Este objeto é o catálogo desses testes. A UI (`DialogoReacaoEResistencia`)
 * só desenha o que vem daqui.
 *
 * **Todos são testes que o JOGADOR rola** — é o que serve à mesa via Discord.
 * O que depende de um PdM agindo (o −N no NH do mago inimigo, por exemplo) fica
 * de fora de propósito: só existiria dentro do combate tático.
 */
object ResistenciaRules {

    /** De onde o teste sai — serve para agrupar na tela. */
    enum class Familia(val rotulo: String) {
        CORPO("Corpo"),
        MENTE("Mente"),
        SOBRENATURAL("Sobrenatural")
    }

    /**
     * Um teste de resistência já montado.
     *
     * [origens] existe pelo mesmo motivo da notinha das perícias: número que
     * não diz de onde veio é caixa preta.
     */
    data class TesteDeResistencia(
        val rotulo: String,
        val alvo: Int,
        val explicacao: String,
        val familia: Familia,
        val origens: List<String> = emptyList()
    ) {
        val descricaoAcessivel: String
            get() = "$rotulo. Alvo $alvo. $explicacao" +
                if (origens.isEmpty()) "" else " Inclui ${origens.joinToString(", ")}."
    }

    private const val ID_BOA_FORMA = "boa_forma"
    private const val ID_DESTEMOR = "destemor"
    /**
     * ⚠️ O id do catálogo é `abascanto_resistencia_a_magia`, não `abascanto`.
     *
     * Escrevi `abascanto` no Lote RESIST-1 e o teste passou — porque o teste
     * inventava o id em vez de ler o catálogo. Mesma falha do bug do V-1: cada
     * pedaço verde e o conjunto quebrado. Agora há um teste que confronta esta
     * constante com `vantagens.v3.json`.
     */
    internal const val ID_RESISTENCIA_MAGIA = "abascanto_resistencia_a_magia"
    private const val ID_DIFICIL_DE_SUBJUGAR = "dificil_de_subjugar"
    private const val ID_DURO_DE_MATAR = "duro_de_matar"

    /**
     * Todos os testes que esta ficha pode rolar.
     *
     * Os de HT existem sempre — qualquer personagem pode precisar resistir a
     * veneno. Os que dependem de vantagem (Resistência à Magia) só aparecem
     * quando a vantagem está na ficha.
     */
    fun testesDe(personagem: Personagem): List<TesteDeResistencia> {
        val ht = personagem.ht
        val vontade = personagem.vontade
        val lista = mutableListOf<TesteDeResistencia>()

        val (bonusHt, origensHt) = bonusBoaForma(personagem)

        // --- Corpo: tudo sai do HT (MB p.419-443) ---
        lista += TesteDeResistencia(
            "Manter a consciência", ht + bonusHt + nivelDe(personagem, ID_DIFICIL_DE_SUBJUGAR),
            "Com PV em 0 ou menos, a cada turno. Falha: desmaia.",
            Familia.CORPO,
            origensHt + origemDe(personagem, ID_DIFICIL_DE_SUBJUGAR)
        )
        lista += TesteDeResistencia(
            "Evitar a morte", ht + bonusHt + nivelDe(personagem, ID_DURO_DE_MATAR),
            "Ao passar de cada múltiplo negativo do PV máximo. Falha: morre.",
            Familia.CORPO,
            origensHt + origemDe(personagem, ID_DURO_DE_MATAR)
        )
        lista += TesteDeResistencia(
            "Resistir a doença", ht + bonusHt,
            "Contra infecção e contágio. O modificador vem da doença.",
            Familia.CORPO, origensHt
        )
        lista += TesteDeResistencia(
            "Resistir a veneno", ht + bonusHt,
            "O modificador vem do veneno.",
            Familia.CORPO, origensHt
        )
        lista += TesteDeResistencia(
            "Aguentar o esforço", ht + bonusHt,
            "Correr, segurar a respiração, calor, exaustão. Falha: perde PF.",
            Familia.CORPO, origensHt
        )

        // --- Mente ---
        val destemor = nivelDe(personagem, ID_DESTEMOR)
        val origemDestemor = origemDe(personagem, ID_DESTEMOR)
        lista += TesteDeResistencia(
            "Verificação de Pânico", vontade + destemor,
            "Diante de horror ou do sobrenatural. NÃO é disparada por dano.",
            Familia.MENTE, origemDestemor
        )
        lista += TesteDeResistencia(
            "Resistir a Intimidação", vontade + destemor,
            "Contra a perícia Intimidação de outro personagem.",
            Familia.MENTE, origemDestemor
        )

        // --- Sobrenatural: só com a vantagem ---
        val rm = nivelDe(personagem, ID_RESISTENCIA_MAGIA)
        if (rm > 0) {
            lista += TesteDeResistencia(
                "Resistir a elixir mágico", ht + bonusHt + rm,
                "MB p.85: teste de HT somado à Resistência à Magia.",
                Familia.SOBRENATURAL,
                origensHt + origemDe(personagem, ID_RESISTENCIA_MAGIA)
            )
        }

        return lista
    }

    /**
     * O nível de Resistência à Magia, para o Mestre aplicar do outro lado.
     *
     * O livro manda subtrair este número do NH de quem lança magia no
     * personagem. Isso acontece na ficha DO MAGO, não nesta — então aqui o
     * número só é **exibido**, para o jogador informar ao Mestre no Discord.
     * Zero quando não há a vantagem.
     */
    fun resistenciaAMagia(personagem: Personagem): Int =
        nivelDe(personagem, ID_RESISTENCIA_MAGIA)

    /** Se a ficha tem Aptidão Mágica — usado pela trava do Abascanto. */
    fun temAptidaoMagica(personagem: Personagem): Boolean =
        personagem.vantagens.any { it.definicaoId == "aptidao_magica" }

    private fun nivelDe(personagem: Personagem, id: String): Int =
        personagem.vantagens.filter { it.definicaoId == id }
            .sumOf { it.nivel.coerceAtLeast(1) }

    private fun origemDe(personagem: Personagem, id: String): List<String> =
        personagem.vantagens.filter { it.definicaoId == id }
            .map { "${it.nome} +${it.nivel.coerceAtLeast(1)}" }

    /** Boa Forma: +1 (5 pts) ou +2 (15 pts) em **todos** os testes de HT. */
    private fun bonusBoaForma(personagem: Personagem): Pair<Int, List<String>> {
        var total = 0
        val origens = mutableListOf<String>()
        personagem.vantagens.filter { it.definicaoId == ID_BOA_FORMA }.forEach { v ->
            val b = if (v.custoEscolhido >= 15) 2 else 1
            total += b
            origens += "${v.nome} +$b"
        }
        return total to origens
    }
}
