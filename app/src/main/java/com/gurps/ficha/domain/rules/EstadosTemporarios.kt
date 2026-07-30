package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem

/**
 * **O interruptor de estado** (Lote D-ESTADO) — as desvantagens que só valem
 * **enquanto o jogador diz que estão valendo**.
 *
 * ## Por que esta família precisou de mecanismo próprio
 *
 * Toda automação anterior aplica o efeito **sempre**: quem tem Timidez tem −2 em
 * Lábia para o resto da vida. Estas nove não são assim. A Dor Crônica vale
 * *durante o surto*; o Dorminhoco, *até uma hora depois de acordar*; o Lunático,
 * *na lua cheia*. Somar sempre inventaria uma penalidade permanente que o livro
 * não dá; nunca somar deixaria o jogador fazendo a conta de cabeça no pior
 * momento.
 *
 * Analogia: é o botão da luz de emergência do carro. Ele não muda o carro — ele
 * liga e desliga uma condição, e enquanto está ligado tudo se comporta diferente.
 *
 * É o mesmo desenho da **ST Braçal** e da **mão inábil**, e a caixinha mora no
 * mesmo lugar — pedido do usuário na leitura: *"coloque a caixa no mesmo lugar
 * onde fica o ST e DX braçal"*.
 *
 * ## ⚠️ Por que esperar a leitura terminar valeu a pena
 *
 * Na p.140 eu conhecia **três** clientes e ia desenhar para DX, IQ e perícia. A
 * leitura completa achou **nove**, e dois deles quebrariam esse desenho:
 * **Lunático** mexe em **Vontade** e **Sangue Frio** em **Deslocamento** — duas
 * dimensões que não estavam no plano. Refazer depois de construído seria o caro.
 *
 * ## ⚠️ Penalidade de IQ NÃO desce para Vontade e Percepção
 *
 * Poderia parecer natural propagar, já que as duas nascem da IQ. O próprio livro
 * mostra que não é assim: o **Lunático** dá *"−2 em todos os testes de Vontade"*
 * e não fala em IQ. Se IQ arrastasse Vontade junto, essa frase seria redundante.
 * Cada estado penaliza exatamente o que a página dele nomeia.
 *
 * ## O que fica de fora, e por quê
 *
 * - **Fobias** já foi automatizada no Lote D-NA, e por outro caminho: a
 *   penalidade dela sai do **Número de Autocontrole**, não de um grau escolhido.
 * - **A IQ −3 de um segundo** dos Problemas na Coluna (Suave) não vira grau: um
 *   interruptor que o jogador tem de lembrar de desligar no turno seguinte erra
 *   mais do que acerta.
 * - **O colapso** do Supersensitivo (DX ou IQ reduzidas à metade) é desfecho de
 *   cena, não modificador — fica com o Mestre.
 *
 * Kotlin puro e testável.
 */
object EstadosTemporarios {

    /**
     * O que um estado ligado desconta.
     *
     * [atributos] usa os mesmos códigos da aba Rolagem (`ST`, `DX`, `IQ`, `HT`,
     * `VONT`, `PER`) para o painel poder aplicar sem tradução.
     */
    data class Mods(
        val atributos: Map<String, Int> = emptyMap(),
        val deslocamento: Int = 0,
        /** Desconto no Número de Autocontrole das OUTRAS desvantagens. */
        val autocontrole: Int = 0,
        /** "Todos os testes de habilidade" — vale para qualquer perícia. */
        val pericias: Int = 0,
        /** O grau Incapacitante dos Flashbacks: nenhuma perícia funciona. */
        val bloqueiaPericias: Boolean = false
    ) {
        operator fun plus(outro: Mods): Mods = Mods(
            atributos = (atributos.keys + outro.atributos.keys).associateWith {
                (atributos[it] ?: 0) + (outro.atributos[it] ?: 0)
            },
            deslocamento = deslocamento + outro.deslocamento,
            autocontrole = autocontrole + outro.autocontrole,
            pericias = pericias + outro.pericias,
            bloqueiaPericias = bloqueiaPericias || outro.bloqueiaPericias
        )

        val vazio: Boolean
            get() = atributos.values.all { it == 0 } && deslocamento == 0 &&
                autocontrole == 0 && pericias == 0 && !bloqueiaPericias

        /** A linha que explica o número — nunca só o número. */
        fun resumo(): String {
            if (bloqueiaPericias) return "nenhuma perícia funciona"
            val partes = buildList {
                atributos.filterValues { it != 0 }.forEach { (a, v) -> add("$a $v") }
                if (pericias != 0) add("perícias $pericias")
                if (vontade() != 0 && !atributos.containsKey(VONT)) Unit
                if (deslocamento != 0) add("Desloc. $deslocamento")
                if (autocontrole != 0) add("autocontrole $autocontrole")
            }
            return partes.joinToString(" · ")
        }

        private fun vontade() = atributos[VONT] ?: 0
    }

    const val ST = "ST"
    const val DX = "DX"
    const val IQ = "IQ"
    const val HT = "HT"
    const val VONT = "VONT"
    const val PER = "PER"

    /** Um degrau do estado — quase sempre a "gravidade" que o livro tabela. */
    data class Grau(val rotulo: String, val mods: Mods)

    /**
     * Um estado que a ficha pode ligar.
     *
     * ⚠️ O grau **não** sai do catálogo. Dor Crônica tem `costKind: special`
     * (gravidade × intervalo × frequência), e Enjoo e Repugnância nem têm grau
     * na compra — a diferença é o que aconteceu na mesa. Então quem responde é
     * o jogador, no toque: o interruptor **cicla** desligado → 1 → 2 → … → 0.
     */
    data class Estado(
        val id: String,
        val nome: String,
        val pagina: Int,
        val quando: String,
        val graus: List<Grau>
    ) {
        /** Rótulo da linha no grau [grau] (1-based); grau 0 é desligado. */
        fun rotulo(grau: Int): String {
            val g = graus.getOrNull(grau - 1)
                ?: return "$nome — desligado (MB p.$pagina): $quando"
            return "$nome · ${g.rotulo} (${g.mods.resumo()})"
        }

        fun modsDo(grau: Int): Mods = graus.getOrNull(grau - 1)?.mods ?: Mods()

        /** O próximo toque: cicla e volta a zero depois do último grau. */
        fun proximoGrau(atual: Int): Int = if (atual >= graus.size) 0 else atual + 1
    }

    private fun mods(
        vararg atributos: Pair<String, Int>,
        deslocamento: Int = 0,
        autocontrole: Int = 0,
        pericias: Int = 0,
        bloqueia: Boolean = false
    ) = Mods(atributos.toMap(), deslocamento, autocontrole, pericias, bloqueia)

    /**
     * Os nove estados, com os números copiados da página de cada um.
     *
     * A ordem é a do livro, não a de "importância": qualquer ordem inventada
     * envelhece mal, e a do livro é conferível.
     */
    val CATALOGO: List<Estado> = listOf(
        // p.137: "-2 / -4 / -6 nos testes de DX, IQ e autocontrole".
        Estado(
            "dor_cronica", "Dor Crônica", 137, "durante o surto de dor",
            listOf(
                Grau("Suave", mods(DX to -2, IQ to -2, autocontrole = -2)),
                Grau("Grave", mods(DX to -4, IQ to -4, autocontrole = -4)),
                Grau("Excruciante", mods(DX to -6, IQ to -6, autocontrole = -6))
            )
        ),
        // p.137: "-2 em todos os testes de autocontrole e -1 em IQ".
        Estado(
            "dorminhoco", "Dorminhoco", 137, "até 1 hora depois de acordar",
            listOf(Grau("Acordou agora", mods(IQ to -1, autocontrole = -2)))
        ),
        // p.138: sucesso no HT = "apenas muito enjoado" -2; fracasso = vomita -5.
        // "em todos os testes de habilidade, DX e IQ".
        Estado(
            "enjoo", "Enjoo", 138, "em veículo em movimento",
            listOf(
                Grau("Muito enjoado", mods(DX to -2, IQ to -2, pericias = -2)),
                Grau("Vomitando", mods(DX to -5, IQ to -5, pericias = -5))
            )
        ),
        // p.141: -2 / -5 "nos testes de habilidade"; o Incapacitante "impede a
        // utilização de qualquer perícia".
        Estado(
            "flashbacks", "Flashbacks", 141, "durante o flashback",
            listOf(
                Grau("Suave (2d segundos)", mods(pericias = -2)),
                Grau("Grave (1d minutos)", mods(pericias = -5)),
                Grau("Incapacitante (3d minutos)", mods(bloqueia = true))
            )
        ),
        // p.149: "-2 em todos os testes de Vontade e autocontrole".
        // ⚠️ Vontade, NÃO IQ -- e é esta frase que prova que IQ não arrasta
        // Vontade junto.
        Estado(
            "lunatico", "Lunático", 149, "na lua cheia",
            listOf(Grau("Lua cheia", mods(VONT to -2, autocontrole = -2)))
        ),
        // p.154. Suave: "-3 até conseguir descansar". Grave: "a DX e a IQ sofrem
        // uma penalidade de -4, até o personagem conseguir descansar".
        Estado(
            "problemas_na_coluna", "Problemas na Coluna", 154, "com a coluna travada",
            listOf(
                Grau("Suave", mods(DX to -3)),
                Grau("Grave", mods(DX to -4, IQ to -4))
            )
        ),
        // p.155. Tocou/respirou: "-5 em todas as perícias e atributos".
        // Ingeriu: "-5 em todos os atributos e -10 em todas as perícias".
        Estado(
            "repugnancia", "Repugnância", 155, "por 10 minutos após o contato",
            listOf(
                Grau(
                    "Tocou ou respirou",
                    mods(ST to -5, DX to -5, IQ to -5, HT to -5, pericias = -5)
                ),
                Grau(
                    "Ingeriu",
                    mods(ST to -5, DX to -5, IQ to -5, HT to -5, pericias = -10)
                )
            )
        ),
        // p.155: "-1 no Deslocamento Básico e na DX para cada 5 °C abaixo de sua
        // temperatura limite". Quatro degraus cobrem 20 °C abaixo do limite --
        // além disso o livro manda testar HT para não perder PV, que é outra
        // conta e fica com o Mestre.
        Estado(
            "sangue_frio", "Sangue Frio", 155, "após 30 min no frio",
            (1..4).map { n ->
                Grau("${n * 5} °C abaixo do limite", mods(DX to -n, deslocamento = -n))
            }
        ),
        // p.158: "-1 (...) -2 para 10 ou mais pessoas, -3 para 100 ou mais, -4
        // para 1.000 ou mais".
        Estado(
            "supersensitivismo", "Supersensitivo", 158, "com gente por perto",
            listOf(
                Grau("1+ pessoa a 20 m", mods(DX to -1, IQ to -1)),
                Grau("10+ pessoas", mods(DX to -2, IQ to -2)),
                Grau("100+ pessoas", mods(DX to -3, IQ to -3)),
                Grau("1.000+ pessoas", mods(DX to -4, IQ to -4))
            )
        )
    )

    private val POR_ID = CATALOGO.associateBy { it.id }

    /**
     * Os estados que ESTA ficha pode ligar — nada mais.
     *
     * Mostrar os nove sempre encheria a tela de quem não tem nenhum, que é a
     * maioria. Mesma regra do painel de Reação e do de Autocontrole.
     */
    fun disponiveis(personagem: Personagem): List<Estado> =
        personagem.desvantagensTotais
            .mapNotNull { POR_ID[it.definicaoId] }
            .distinctBy { it.id }

    /** Se vale desenhar o painel. */
    fun temAlgum(personagem: Personagem): Boolean = disponiveis(personagem).isNotEmpty()

    /**
     * A soma de tudo que está ligado.
     *
     * [ligados] é `id do estado → grau` (1-based). Grau 0 ou id desconhecido é
     * ignorado — ficha que perdeu a desvantagem não carrega o estado junto.
     */
    fun totalDe(ligados: Map<String, Int>): Mods =
        ligados.entries.fold(Mods()) { acc, (id, grau) ->
            acc + (POR_ID[id]?.modsDo(grau) ?: Mods())
        }

    /** O desconto num atributo específico, pelo código da aba Rolagem. */
    fun penalidadeDeAtributo(ligados: Map<String, Int>, atributo: String): Int =
        totalDe(ligados).atributos[atributo] ?: 0

    /**
     * A linha de resumo do painel, quando há algo ligado.
     *
     * Existe porque um número que muda sozinho, sem dizer por quê, é o defeito
     * que este projeto mais persegue.
     */
    fun resumoAtivo(ligados: Map<String, Int>): String? {
        val total = totalDe(ligados)
        if (total.vazio) return null
        val nomes = ligados.filterValues { it > 0 }
            .mapNotNull { (id, g) -> POR_ID[id]?.let { "${it.nome} (${it.graus[g - 1].rotulo})" } }
        return "${nomes.joinToString(", ")} → ${total.resumo()}"
    }
}
