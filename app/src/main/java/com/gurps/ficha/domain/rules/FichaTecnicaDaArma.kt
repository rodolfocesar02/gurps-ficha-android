package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.ArmaCatalogoItem

// O corpo deste arquivo continua escrevendo `Linha`, `ModoNaTela` e `Ficha` sem
// prefixo. Os apelidos sao privados ao arquivo: quem chama de fora usa os nomes
// de verdade, em `FichaDeEquipamento`.
private typealias Linha = FichaDeEquipamento.Linha
private typealias ModoNaTela = FichaDeEquipamento.ModoNaTela
private typealias Ficha = FichaDeEquipamento.Ficha
private const val AUSENTE = FichaDeEquipamento.AUSENTE

/**
 * **A ficha técnica da arma** — tudo que o catálogo sabe, em português (Lote ARMA-2).
 *
 * ## Por que é Kotlin puro e não Compose
 *
 * Aqui mora a **tradução do jargão**, que é regra do livro e não desenho de
 * tela: `Tiros 80(3)` virar *"80 tiros, 3 turnos para recarregar"* é a p.271
 * escrita em português, não uma escolha estética. Sendo função pura, dá para
 * travar cada frase num teste — e foi assim que a `CL 2` deixou de sair como
 * "militar" (é **restrito**; militar é a CL 1, MB p.508).
 *
 * ## As três regras de exibição
 *
 * 1. **Toda sigla vem com a tradução.** `Mag −3` sozinho não diz nada.
 * 2. **Campo ausente é `—`, nunca `0`.** Zero é um dado; "não sei" é outro. Uma
 *    besta com Recuo 0 seria uma besta que não coiceia; uma besta com Recuo `—`
 *    é uma arma para a qual o livro não cadastrou Recuo nenhum.
 * 3. **O que depende da ST é calculado com a ST de quem empunha.** O Arco Longo
 *    é `×15/×20` na tabela e **165/220 m** numa ST 11. Guardar o `×15` e nunca
 *    fazer a conta é deixar o trabalho para o jogador.
 */
object FichaTecnicaDaArma {

    // As estruturas (Linha, ModoNaTela, Ficha) e o AUSENTE moraram aqui ate o
    // Lote EQP-6. Sairam para `FichaDeEquipamento` quando a armadura e o escudo
    // passaram a ter ficha tambem: a FORMA e a mesma, so os montadores diferem.

    // ==================================================================
    // A montagem
    // ==================================================================

    /**
     * @param st a ST de quem vai empunhar — entra no alcance dos arcos e no dano.
     * @param resolverDano converte `"GdP+2 perf"` no dano real da ficha. Vem de
     *   fora porque quem sabe fazer essa conta é o ViewModel, e arrastá-lo para
     *   cá tornaria esta função impossível de testar.
     * @param observacoes as notas de rodapé `[n]` já casadas com o texto.
     */
    fun de(
        arma: ArmaCatalogoItem,
        st: Int,
        resolverDano: (String) -> String? = { null },
        observacoes: List<String> = emptyList()
    ): Ficha = Ficha(
        nome = arma.nome,
        subtitulo = subtitulo(arma),
        selo = arma.cl?.let { "CL $it · ${nomeDaCl(it)}" },
        destaques = if (arma.ehADistancia) destaquesDeDistancia(arma, st) else emptyList(),
        modos = modos(arma, resolverDano),
        detalhes = detalhes(arma),
        observacoes = observacoes
    )

    private val ArmaCatalogoItem.ehADistancia: Boolean
        get() = tipoCombate != "corpo_a_corpo"

    private fun subtitulo(arma: ArmaCatalogoItem): String {
        val tipo = when (arma.tipoCombate) {
            "corpo_a_corpo" -> "Corpo a corpo"
            "armas_de_fogo" -> "Arma de fogo"
            else -> "À distância"
        }
        val grupo = arma.grupo
            .substringBefore("(")
            .trim()
            .trim(',')
            .takeIf { it.isNotBlank() }
            ?.lowercase()
            ?.replaceFirstChar { it.uppercase() }
        val nt = arma.nt?.let { "NT $it" }
        return listOfNotNull(tipo, grupo, nt).joinToString(" · ")
    }

    // ==================================================================
    // Os números que se olha no meio da jogada
    // ==================================================================

    private fun destaquesDeDistancia(arma: ArmaCatalogoItem, st: Int): List<Linha> {
        val linhas = mutableListOf<Linha>()

        linhas += Linha("Dano", arma.danoRaw.ifBlank { AUSENTE }, explicarDano(arma.danoRaw))
        linhas += linhaDePrecisao(arma)
        linhas += linhaDeAlcance(arma, st)
        linhas += Linha(
            "CdT",
            arma.cadenciaTiro?.toString() ?: AUSENTE,
            arma.cadenciaTiro?.let {
                if (it <= 1) "um tiro por segundo" else "até $it tiros por segundo"
            }
        )
        linhas += Linha("Tiros", arma.tirosRaw?.ifBlank { null } ?: AUSENTE, explicarTiros(arma.tirosRaw))
        linhas += Linha("Recuo", arma.recuo?.toString() ?: AUSENTE, explicarRecuo(arma.recuo))
        return linhas
    }

    /**
     * 🔴 A Precisão, com a mira acoplada **à parte**.
     *
     * O livro (p.270): *"Se a arma tiver uma mira embutida, o bônus devido a
     * isto aparecerá como um modificador separado ao lado da Prec básica"*. É
     * exatamente o `+N` que o app descartava, em 12 armas de fogo.
     */
    private fun linhaDePrecisao(arma: ArmaCatalogoItem): Linha {
        val base = arma.precisao ?: return Linha("Precisão", AUSENTE, "esta arma não tem Prec na tabela")
        val acessorio = arma.precisaoAcessorio
        return Linha(
            "Precisão",
            if (acessorio != null) "$base +$acessorio" else "$base",
            if (acessorio != null) {
                "$base da arma e +$acessorio da mira acoplada — só valem se você Apontar"
            } else {
                "some ao NH se você Apontar no turno anterior"
            }
        )
    }

    /**
     * O alcance, com a conta do arco já feita.
     *
     * `×15/×20` numa ST 11 é **165/220 m**. O jogador não deveria precisar
     * multiplicar no meio da mesa.
     */
    private fun linhaDeAlcance(arma: ArmaCatalogoItem, st: Int): Linha {
        val mult = arma.alcanceMultStRaw
        if (mult != null) {
            val emMetros = multiplicadores(mult)?.let { (a, b) -> "${a * st} / ${b * st} m" }
            return Linha(
                "Alcance",
                mult,
                if (emMetros != null) "com a sua ST $st → $emMetros" else "múltiplo da sua ST"
            )
        }
        val meio = arma.meioDanoMetros
        val max = arma.maximoMetros
        if (meio == null && max == null) {
            return Linha("Alcance", AUSENTE, "alcance não cadastrado para esta arma")
        }
        val valor = listOfNotNull(meio?.toString(), max?.toString()).joinToString(" / ") + " m"
        return Linha(
            "Alcance",
            valor,
            if (meio != null && max != null) {
                "além de $meio m o dano sai pela metade; $max m é o limite do tiro"
            } else if (max != null) {
                "limite do tiro"
            } else {
                "além de $meio m o dano sai pela metade"
            }
        )
    }

    private fun multiplicadores(raw: String): Pair<Int, Int>? {
        val numeros = Regex("\\d+").findAll(raw).mapNotNull { it.value.toIntOrNull() }.toList()
        if (numeros.size < 2) return null
        return numeros[0] to numeros[1]
    }

    // ==================================================================
    // Os modos de ataque
    // ==================================================================

    private fun modos(arma: ArmaCatalogoItem, resolverDano: (String) -> String?): List<ModoNaTela> {
        if (arma.modos.size <= 1 && arma.ehADistancia) return emptyList()
        return arma.modos.map { modo ->
            ModoNaTela(
                ordem = modo.ordem,
                dano = modo.danoRaw,
                danoComSt = resolverDano(modo.danoRaw)?.takeIf { it.isNotBlank() && it != modo.danoRaw },
                detalhe = listOfNotNull(
                    modo.alcanceCorpoACorpo?.let { "Alcance $it" },
                    modo.aparar?.let { "Aparar $it" }
                ).joinToString(" · ").takeIf { it.isNotBlank() },
                // Do 2º em diante o catálogo traz custo/peso nulos porque o
                // livro escreve "—": é a mesma arma na mão, não um item novo.
                mesmaArma = modo.ordem > 1
            )
        }
    }

    // ==================================================================
    // O que se olha uma vez, na hora de comprar
    // ==================================================================

    private fun detalhes(arma: ArmaCatalogoItem): List<Linha> {
        val linhas = mutableListOf<Linha>()

        linhas += Linha(
            "ST mínima",
            // ⚠️ Glaive e Alabarda vêm com `valor` nulo porque o livro dá uma ST
            // por modo ("13‡ / 12"). Cair para o texto cru mostra a verdade do
            // livro; o travessão esconderia a ST de uma alabarda inteira.
            arma.stMinimo?.let { st -> "$st${simbolosDeSt(arma.stFlags)}" }
                ?: arma.stRaw
                ?: AUSENTE,
            explicarSt(arma)
        )
        if (!arma.ehADistancia) {
            arma.alcanceCorpoACorpo?.let {
                // ⚠️ "C" NÃO leva metro. C é combate corporal — agarrado ao
                // alvo, distância zero. A tela dizia "C m", que não é distância
                // nenhuma. Achado nos prints de 08/08.
                val soCorporal = it.trim().equals("C", ignoreCase = true)
                linhas += Linha(
                    "Alcance",
                    if (soCorporal) it else "$it m",
                    explicarAlcanceCorpoACorpo(it)
                )
            }
            arma.aparar?.let { linhas += Linha("Aparar", it, explicarAparar(it)) }
        } else {
            linhas += Linha(
                "Magnitude",
                arma.magnitude?.toString() ?: AUSENTE,
                arma.magnitude?.let {
                    "$it ao Avançar e Atacar, e a mesma penalidade para ocultar a arma"
                }
            )
        }
        linhas += Linha("Peso", pesoNaTela(arma), arma.municaoKg?.let { "o segundo número é a munição" })
        linhas += Linha(
            "Custo",
            arma.custoBase?.let { FichaDeEquipamento.formatarDinheiro(it) } ?: (arma.custoRaw ?: AUSENTE),
            null
        )
        arma.nt?.let { linhas += Linha("NT", "$it", null) }
        return linhas
    }

    private fun pesoNaTela(arma: ArmaCatalogoItem): String {
        val arm = arma.pesoBaseKg ?: return arma.pesoRaw ?: AUSENTE
        val mun = arma.municaoKg
        return if (mun != null && mun > 0f) {
            "${FichaDeEquipamento.formatarKg(arm)} kg + ${FichaDeEquipamento.formatarKg(mun)} kg"
        } else {
            "${FichaDeEquipamento.formatarKg(arm)} kg"
        }
    }

    // ==================================================================
    // O glossário — cada frase é a p.270-272 em português
    // ==================================================================

    /** MB p.508. ⚠️ CL 1 é militar; CL 2 é **restrito**. Trocar isso mente na tela. */
    fun nomeDaCl(cl: Int): String = when (cl) {
        0 -> "banido"
        1 -> "militar"
        2 -> "restrito"
        3 -> "licenciado"
        4 -> "aberto"
        else -> "fora da escala do livro"
    }

    /**
     * Os símbolos da coluna ST. **† e ‡ não são a mesma coisa** (MB p.271): os
     * dois exigem duas mãos, mas o ‡ ainda deixa a arma **despreparada** depois
     * do ataque.
     */
    fun simbolosDeSt(flags: List<String>): String = buildString {
        if (flags.any { it.equals("dagger", true) }) append(" †")
        if (flags.any { it.equals("double_dagger", true) }) append(" ‡")
    }

    fun explicarSt(arma: ArmaCatalogoItem): String? {
        val partes = mutableListOf<String>()
        if (arma.stMinimo == null && arma.stRaw?.contains("/") == true) {
            partes += "o livro dá uma ST por modo de ataque"
        }
        if (arma.stFlags.any { it.equals("dagger", true) }) partes += "usa as duas mãos"
        if (arma.stFlags.any { it.equals("double_dagger", true) }) {
            partes += "fica despreparada depois de atacar, a não ser que sua ST seja 1,5× a exigida"
        }
        if (partes.isEmpty() && arma.duasMaos) partes += "usa as duas mãos"
        if (partes.isEmpty() && arma.stMinimo != null) {
            partes += "abaixo disso, −1 no NH por ponto de ST que faltar"
        }
        return partes.joinToString("; ").takeIf { it.isNotBlank() }
    }

    /** MB p.271. */
    fun explicarTiros(raw: String?): String? {
        val t = raw?.trim().orEmpty()
        if (t.isBlank()) return null
        if (t.equals("A", ignoreCase = true)) {
            return "arma de arremesso — \"recarregar\" é ir buscá-la ou pegar outra"
        }
        val casou = Regex("^(\\d+)(\\+1)?\\s*\\((\\d+)(i)?\\)$").find(t) ?: return null
        val tiros = casou.groupValues[1].toIntOrNull() ?: return null
        val extra = casou.groupValues[2].isNotBlank()
        val turnos = casou.groupValues[3].toIntOrNull() ?: return null
        val individual = casou.groupValues[4].isNotBlank()
        return buildString {
            append(if (tiros == 1) "1 tiro" else "$tiros tiros")
            if (extra) append(" (+1 na câmara)")
            append(", ")
            append(if (turnos == 1) "1 turno" else "$turnos turnos")
            append(" para recarregar")
            if (individual) append(" — e esse tempo é POR TIRO, não pelo total")
        }
    }

    /** MB p.272: cada múltiplo inteiro do Recuo na margem vira mais um acerto. */
    fun explicarRecuo(recuo: Int?): String? = when {
        recuo == null -> null
        recuo <= 1 -> "recuo fraco: cada ponto de margem vira mais um acerto no tiro múltiplo"
        else -> "no tiro múltiplo, cada $recuo pontos de margem viram mais um acerto"
    }

    fun explicarDano(danoRaw: String): String? {
        val d = danoRaw.lowercase()
        val divisor = Regex("\\((\\d+(?:,\\d+)?)\\)").find(danoRaw)?.groupValues?.get(1)
        val tipo = when {
            d.contains("pa++") -> "perfuração enorme: ×2 de ferimento"
            d.contains("pa+") -> "perfuração grande: ×1,5 de ferimento"
            d.contains("pa-") -> "perfuração pequena: ×0,5 de ferimento"
            d.contains(" pa") -> "perfuração: ferimento igual ao dano"
            d.contains("corte") -> "corte: ×1,5 de ferimento no que passar da RD"
            d.contains("perf") -> "perfuração: ×1,5 de ferimento no que passar da RD"
            d.contains("cont") -> "contusão: causa choque e pode derrubar"
            d.contains("quei") || d.contains("qmd") -> "queimadura"
            else -> null
        }
        val comDivisor = divisor?.let { "divisor de armadura $it — a RD do alvo vale menos" }
        return listOfNotNull(tipo, comDivisor).joinToString("; ").takeIf { it.isNotBlank() }
    }

    /** MB p.270. O asterisco é a parte que escapa na mesa. */
    fun explicarAlcanceCorpoACorpo(alcance: String): String? {
        val precisaPreparar = alcance.contains("*")
        val corporal = alcance.contains("C", ignoreCase = true)
        val partes = mutableListOf<String>()
        if (corporal) partes += "C é combate corporal, agarrado ao alvo"
        if (precisaPreparar) partes += "o asterisco exige uma manobra Preparar para mudar de alcance"
        return partes.joinToString("; ").takeIf { it.isNotBlank() }
    }

    /** MB p.270 e p.376. */
    fun explicarAparar(valor: String): String? {
        val v = valor.trim().uppercase()
        return when {
            v == "NÃO" || v == "NAO" -> "não dá para aparar com esta arma"
            v.endsWith("D") -> "arma desbalanceada: aparou, não ataca no mesmo turno"
            v.endsWith("E") || v == "F" -> "arma de esgrima"
            v == "0" -> "sem modificador na aparada"
            v.startsWith("+") || v.startsWith("-") || v.startsWith("−") -> "modificador na aparada"
            else -> null
        }
    }

}
