package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.ArmaduraCatalogoItem
import com.gurps.ficha.model.Equipamento

/**
 * **A ficha técnica da armadura** — MB p.283-287. Lote EQP-6.
 *
 * O usuário pediu a armadura *"no mesmo padrão de como ficou as armas"*, e o
 * padrão da arma não é o desenho — é a **divisão**:
 *
 * - **No meio da jogada:** o que se consulta quando o golpe já veio. Para a
 *   armadura, RD e local. É o único bloco que importa com o dado na mão.
 * - **Na hora de comprar:** NT, peso, custo. Olha-se uma vez.
 * - **Observações do livro:** as notas de rodapé por extenso.
 *
 * ⚠️ A arma tem ainda o bloco de **modos de ataque**. A armadura não ataca, então
 * ele vem vazio e some da tela. Foi para isso que a forma virou uma só
 * ([FichaDeEquipamento]): a diferença fica no montador, não no card.
 */
object FichaTecnicaDaArmadura {

    /**
     * 🔴 **[peca] é o que o jogador realmente tem** (Lote EQP-8).
     *
     * A *Túnica* do catálogo é uma peça só, `tronco, virilha`, 3 kg, $30. Ao
     * escolher os dois locais, o app a parte em duas metades de 1,5 kg e $15.
     *
     * Até o EQP-7 a ficha era montada **só do catálogo**, e ao editar a metade da
     * virilha a tela dizia *tronco, virilha*, *3 kg* e *$30* — três linhas
     * mentindo, com os campos certos logo abaixo.
     *
     * ⚠️ Para arma isso passava despercebido porque uma espada não se parte em
     * duas. A armadura se parte, e a ficha passava a descrever uma peça que o
     * jogador não tem.
     *
     * O catálogo continua sendo dono do que **só ele** sabe: NT, observações e a
     * lista de componentes. Nome, local, RD, peso e custo saem da peça quando ela
     * existe.
     */
    fun de(
        armadura: ArmaduraCatalogoItem,
        peca: Equipamento? = null,
        observacoes: List<String> = emptyList()
    ): FichaDeEquipamento.Ficha {
        val local = localDe(armadura, peca)
        return FichaDeEquipamento.Ficha(
            nome = TextoDoCatalogo.corrigir(peca?.nome?.takeIf { it.isNotBlank() } ?: armadura.nome),
            subtitulo = listOfNotNull("Armadura", local.takeIf { it.isNotBlank() }).joinToString(" · "),
            selo = armadura.nt?.let { "NT $it" },
            destaques = protecao(armadura, peca, local),
            modos = emptyList(),
            detalhes = compra(armadura, peca),
            observacoes = observacoes
        )
    }

    private fun localDe(armadura: ArmaduraCatalogoItem, peca: Equipamento?): String {
        val daPeca = peca?.let { CartaoDoItem.localDaArmadura(it) }
        return daPeca ?: TextoDoCatalogo.corrigir(armadura.local)
    }

    // ──────────────────────────────────────────────────────────────────
    // No meio da jogada
    // ──────────────────────────────────────────────────────────────────

    private fun protecao(
        armadura: ArmaduraCatalogoItem,
        peca: Equipamento?,
        local: String
    ): List<FichaDeEquipamento.Linha> {
        val linhas = mutableListOf<FichaDeEquipamento.Linha>()

        val rd = peca?.rdArmaduraExibicao()?.takeIf { it.isNotBlank() } ?: armadura.rd
        linhas += FichaDeEquipamento.Linha(
            "RD",
            rd.ifBlank { FichaDeEquipamento.AUSENTE },
            explicarRd(rd),
            // Tem campo no editor desde o EQP-8 — antes disso o RD era o único
            // número da armadura que o jogador não conseguia mexer, e uma peça
            // encantada de +1 RD não tinha onde ser registrada.
            editavel = true
        )
        linhas += FichaDeEquipamento.Linha(
            "Local",
            local.ifBlank { FichaDeEquipamento.AUSENTE },
            "só protege aqui — golpe em outro local ignora esta RD"
        )

        // Uma peça composta (elmo + gorjal, por exemplo) traz RD diferente por
        // parte. Esconder isso faria a ficha prometer uma RD que ela não dá em
        // todo lugar.
        armadura.componentes.forEach { c ->
            linhas += FichaDeEquipamento.Linha(
                TextoDoCatalogo.corrigir(c.local).replaceFirstChar { it.uppercase() },
                "RD ${c.rd}",
                "parte desta mesma peça"
            )
        }
        return linhas
    }

    /**
     * O jargão da coluna RD, em português.
     *
     * ⚠️ O `*` e a barra **não** são enfeite: são as notas [1] e [3] da p.286
     * (*RD dividida*), e mudam quanto dano passa. Mostrar `4/2*` sem dizer o que
     * é deixa o jogador escolher a armadura no escuro.
     */
    fun explicarRd(rdRaw: String): String? {
        val temAsterisco = rdRaw.contains("*")
        val temBarra = rdRaw.contains("/")
        return when {
            temBarra && temAsterisco ->
                "RD dividida: o maior contra perfuração e corte, o menor contra o resto — " +
                    "e o asterisco avisa que a peça é flexível (dano por contusão passa)"
            temBarra ->
                "RD dividida: use o maior contra perfuração e corte, o menor contra o resto"
            temAsterisco ->
                "peça flexível: contra dano por contusão o golpe ainda machuca através dela"
            else -> null
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Na hora de comprar
    // ──────────────────────────────────────────────────────────────────

    private fun compra(armadura: ArmaduraCatalogoItem, peca: Equipamento?): List<FichaDeEquipamento.Linha> {
        // O peso e o custo da PEÇA, não os da armadura inteira do catálogo.
        val peso = peca?.peso ?: armadura.pesoBaseKg
        val custo = peca?.custo ?: armadura.custoBase
        return listOf(
            FichaDeEquipamento.Linha(
                "NT",
                armadura.nt?.toString() ?: FichaDeEquipamento.AUSENTE,
                "nível tecnológico em que a peça é encontrada com facilidade"
            ),
            FichaDeEquipamento.Linha(
                "Peso",
                peso?.let { "${FichaDeEquipamento.formatarKg(it)} kg" } ?: FichaDeEquipamento.AUSENTE,
                "entra na carga — e a carga mexe no Deslocamento e na Esquiva",
                editavel = true
            ),
            FichaDeEquipamento.Linha(
                "Custo",
                custo?.let { FichaDeEquipamento.formatarDinheiro(it) } ?: FichaDeEquipamento.AUSENTE,
                editavel = true
            )
        )
    }
}
