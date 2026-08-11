package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.ArmaduraCatalogoItem

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

    fun de(armadura: ArmaduraCatalogoItem, observacoes: List<String> = emptyList()): FichaDeEquipamento.Ficha =
        FichaDeEquipamento.Ficha(
            nome = TextoDoCatalogo.corrigir(armadura.nome),
            subtitulo = subtitulo(armadura),
            selo = armadura.nt?.let { "NT $it" },
            destaques = protecao(armadura),
            modos = emptyList(),
            detalhes = compra(armadura),
            observacoes = observacoes
        )

    private fun subtitulo(armadura: ArmaduraCatalogoItem): String {
        val locais = TextoDoCatalogo.corrigir(armadura.local).takeIf { it.isNotBlank() }
        return listOfNotNull("Armadura", locais).joinToString(" · ")
    }

    // ──────────────────────────────────────────────────────────────────
    // No meio da jogada
    // ──────────────────────────────────────────────────────────────────

    private fun protecao(armadura: ArmaduraCatalogoItem): List<FichaDeEquipamento.Linha> {
        val linhas = mutableListOf<FichaDeEquipamento.Linha>()

        linhas += FichaDeEquipamento.Linha(
            "RD",
            armadura.rd.ifBlank { FichaDeEquipamento.AUSENTE },
            explicarRd(armadura.rd)
        )
        linhas += FichaDeEquipamento.Linha(
            "Local",
            TextoDoCatalogo.corrigir(armadura.local).ifBlank { FichaDeEquipamento.AUSENTE },
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

    private fun compra(armadura: ArmaduraCatalogoItem): List<FichaDeEquipamento.Linha> = listOf(
        FichaDeEquipamento.Linha(
            "NT",
            armadura.nt?.toString() ?: FichaDeEquipamento.AUSENTE,
            "nível tecnológico em que a peça é encontrada com facilidade"
        ),
        FichaDeEquipamento.Linha(
            "Peso",
            armadura.pesoBaseKg?.let { "${FichaDeEquipamento.formatarKg(it)} kg" }
                ?: FichaDeEquipamento.AUSENTE,
            "entra na carga — e a carga mexe no Deslocamento e na Esquiva"
        ),
        FichaDeEquipamento.Linha(
            "Custo",
            armadura.custoBase?.let { FichaDeEquipamento.formatarDinheiro(it) }
                ?: FichaDeEquipamento.AUSENTE
        )
    )
}
