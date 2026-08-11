package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Equipamento

/**
 * O **conteúdo** de um cartão de item da aba Equipamentos.
 *
 * ## Por que isto não mora no Compose
 *
 * O Lote EQP-1 nasceu com a regra das quatro linhas escrita **dentro** de um
 * `@Composable`. Funcionava, e era intestável: `PadraoDeTelaTest` lê o
 * código-fonte da `ui/` procurando violações de layout, mas nenhum teste
 * conseguia perguntar *"o que este cartão mostra?"* — só o aparelho respondia.
 *
 * 🔴 E o EQP-2 mostrou o preço disso. O cartão de armadura tinha a **sua
 * própria** cópia do desenho, com o nome quatro pontos maior e sem orçamento
 * nenhum, e nada acusou: o gate ficou verde com o cartão de armadura em cinco
 * linhas e o nome quebrando em duas. Um defeito que só a foto do usuário pegou.
 *
 * Aqui é Kotlin puro. O que o cartão diz vira teste; o Compose só pinta.
 */
object CartaoDoItem {

    /**
     * Quantas linhas um cartão pode ocupar, **contando o nome**.
     *
     * Decisão do usuário (11/08): quatro. O que não couber vira reticências, e
     * quem quiser ler tudo abre o lápis.
     *
     * O motivo é concreto: a *Máscara "olhos da noite"* tem uma nota de dez
     * linhas e sozinha ocupava mais tela que os quatro itens acima dela juntos.
     * Uma lista em que um item empurra os outros para fora não é uma lista — é
     * um texto com títulos.
     */
    const val LINHAS = 4

    /** O que sobra para o corpo depois que o nome gasta a dele. */
    const val LINHAS_DO_CORPO = LINHAS - 1

    /**
     * O papel de uma linha. **Não é cor** — é o que a linha significa.
     *
     * ⚠️ A cor fica na `ui/`, onde ela sabe se o tema está claro ou escuro.
     * Escolher `Color.Red` aqui seria decidir por um tema que este arquivo não
     * enxerga (é a violação nº 4 do padrão de tela).
     */
    enum class Papel { NEUTRO, DANO, CUSTO, PROTECAO, ALERTA }

    data class Linha(val texto: String, val papel: Papel = Papel.NEUTRO)

    /**
     * Encaixa as linhas no orçamento.
     *
     * ## ⚠️ Por que orçamento, e não `maxLines` em cada linha
     *
     * `maxLines` por linha não limita o cartão: cinco textos de uma linha dão
     * cinco linhas. O corte tem que ser do **conjunto**.
     *
     * Cada entrada ganha uma linha; se sobrarem entradas, o excedente é
     * **juntado** na última em vez de sumir. Descartar seria mais simples de
     * escrever — e faria o jogador achar que a nota nem existe. Cortada com
     * reticências, ele vê que há mais e abre o lápis.
     */
    fun cortar(linhas: List<Linha>, orcamento: Int = LINHAS_DO_CORPO): List<Linha> {
        val uteis = linhas.filter { it.texto.isNotBlank() }
        if (orcamento <= 0 || uteis.isEmpty()) return emptyList()
        if (uteis.size <= orcamento) return uteis

        val cabeca = uteis.take(orcamento - 1)
        val cauda = uteis.drop(orcamento - 1)
        return cabeca + Linha(cauda.joinToString(" · ") { it.texto }, cauda.first().papel)
    }

    /**
     * Quantas linhas de texto a entrada de índice [indice] pode ocupar.
     *
     * Só a última fica com a sobra do orçamento — é ela que carrega a nota
     * comprida e termina em reticências.
     */
    fun alturaDe(indice: Int, total: Int, orcamento: Int = LINHAS_DO_CORPO): Int =
        if (indice == total - 1) (orcamento - indice).coerceAtLeast(1) else 1

    // ──────────────────────────────────────────────────────────────────
    // O conteúdo de cada tipo de cartão
    // ──────────────────────────────────────────────────────────────────

    /**
     * A linha de peso e quantidade.
     *
     * ⚠️ Com quantidade 1, `1x | 0.2kg cada | Total: 0.2kg` diz o **mesmo
     * número três vezes** e queima uma das três linhas do corpo — justamente a
     * que faltava para a nota da Máscara. Com um item só, o peso é o peso.
     */
    fun pesoEQuantidade(quantidade: Int, pesoUnitario: Float): String {
        val total = pesoUnitario * quantidade
        return if (quantidade <= 1) {
            "${formatarPeso(pesoUnitario)} kg"
        } else {
            "${quantidade}x · ${formatarPeso(pesoUnitario)} kg cada · total ${formatarPeso(total)} kg"
        }
    }

    /** Sem casa decimal inútil: `2.0` vira `2`, `0.25` continua `0.25`. */
    fun formatarPeso(kg: Float): String =
        if (kg == kg.toInt().toFloat()) kg.toInt().toString() else kg.toString()

    /**
     * O cabeçalho `Local: X; RD: Y` que o app escreve **dentro das notas**
     * quando a armadura sai do catálogo.
     *
     * 🔴 É a fonte do defeito que o usuário viu: a Túnica mostrava `RD: 1*` numa
     * linha e `Local: tronco; RD: 2*` na seguinte. O `1*` vem do campo
     * `armaduraRd` — o que o combate e o diálogo de ferimento realmente leem. O
     * `2*` é texto congelado no dia em que a armadura foi escolhida; editar o RD
     * muda o campo e **não** muda a frase.
     *
     * É o preço na etiqueta da prateleira contra o preço no caixa.
     *
     * ⚠️ O cabeçalho é removido **só da exibição**, nunca do que está salvo:
     * numa ficha antiga sem o campo `armaduraRd`, `rdArmaduraExibicao()` volta a
     * ler o RD justamente dessa frase. Apagá-la do disco apagaria o RD junto.
     */
    private val CABECALHO_ARMADURA =
        Regex("""^\s*Local:\s*[^;\n]*;\s*RD:\s*[^\n]*$""", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))

    private fun linhasSemCabecalho(notas: String): List<String> =
        notas.lineSequence()
            .filterNot { CABECALHO_ARMADURA.matches(it) }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toList()

    fun notaSemCabecalho(notas: String): String =
        linhasSemCabecalho(notas).joinToString(" · ")

    /**
     * As notas como o **editor** deve mostrá-las: sem o cabeçalho automático,
     * mas com as quebras de linha preservadas (Lote EQP-7).
     *
     * ⚠️ Só se tira o cabeçalho quando os campos estruturados existem. Numa ficha
     * antiga sem `armaduraRd`, aquela frase **é** o RD — e o editor grava o que
     * mostra, então escondê-la ali apagaria o dado de verdade.
     */
    fun notasParaEditar(eq: Equipamento): String {
        val temOsCampos = !eq.armaduraRd.isNullOrBlank() && !eq.armaduraLocal.isNullOrBlank()
        if (!temOsCampos) return eq.notas
        return linhasSemCabecalho(eq.notas).joinToString("\n")
    }

    /** O local da armadura: o campo de verdade, e a frase antiga como reserva. */
    fun localDaArmadura(eq: Equipamento): String? {
        val campo = eq.armaduraLocal?.trim().orEmpty()
        if (campo.isNotBlank()) return TextoDoCatalogo.corrigir(campo)
        val legado = Regex("""Local:\s*([^;\n]+)""", RegexOption.IGNORE_CASE)
            .find(eq.notas)?.groupValues?.getOrNull(1)?.trim().orEmpty()
        return legado.ifBlank { null }?.let { TextoDoCatalogo.corrigir(it) }
    }

    /**
     * O corpo do cartão de uma armadura — **um** lugar só, para os três cartões
     * não voltarem a divergir.
     */
    fun linhasDaArmadura(eq: Equipamento): List<Linha> = buildList {
        // Saga: item tirado pela narrativa. Continua na ficha, mas não dá RD.
        if (eq.confiscado) {
            add(Linha("confiscado na história — não dá RD no combate", Papel.ALERTA))
        }
        val rd = eq.rdArmaduraExibicao()?.trim().orEmpty()
        val local = localDaArmadura(eq)
        val protecao = listOfNotNull(
            local?.let { "Local: $it" },
            if (rd.isNotBlank()) "RD: $rd" else null
        ).joinToString(" · ")
        if (protecao.isNotBlank()) add(Linha(protecao, Papel.PROTECAO))

        val nota = notaSemCabecalho(eq.notas)
        if (nota.isNotBlank()) add(Linha(TextoDoCatalogo.corrigir(nota), Papel.NEUTRO))
    }
}
