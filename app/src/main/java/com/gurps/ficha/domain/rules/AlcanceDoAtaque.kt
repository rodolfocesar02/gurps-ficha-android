package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.model.PERICIAS_COMBATE_DISTANCIA

/**
 * **É um ataque à distância?** — e, se for, até onde ele chega.
 *
 * ## Por que precisa existir
 *
 * A linha de distância do alvo (Lote MIRA-2) só faz sentido para arco, besta e
 * arma de fogo. Numa Espada, perguntar "a que distância?" é ruído na tela.
 *
 * O app já sabia responder isso — mas a resposta morava em
 * `domain/combat/TraducaoFichaParaCombate.kt`, arquivo **da Saga**. Deixar a
 * ficha perguntando para lá recriaria a amarra que o Lote MIRA-1 acabou de
 * desfazer quando trouxe o `LocalAtaque` para cá.
 *
 * ## Como decide
 *
 * Duas fontes, nesta ordem:
 *
 * 1. **A arma escolhida**, se houver. É o dado mais confiável: vem do catálogo,
 *    que é dividido em três arquivos, e o campo só assume `corpo_a_corpo`,
 *    `distancia` ou `armas_de_fogo`.
 * 2. **A perícia do ataque**, quando não há arma.
 *
 * As duas juntas cobrem os dois furos: o ataque "Arcos" com a fonte de dano
 * ainda em "Dano ST" (a arma não diria nada) e a faca de arremesso empunhada com
 * a perícia Faca (a perícia não diria nada).
 *
 * Kotlin puro e testável.
 */
object AlcanceDoAtaque {

    /** Os dois valores de `armaTipoCombate` que significam "atira". */
    private fun tipoEhADistancia(tipoCombate: String?): Boolean {
        val t = tipoCombate?.lowercase().orEmpty()
        return t.contains("dist") || t.contains("fogo")
    }

    /** Se a perícia é de ataque à distância, pelo id do catálogo. */
    fun periciaEhADistancia(periciaId: String?): Boolean {
        val id = periciaId?.lowercase()?.removePrefix("racial_")?.trim().orEmpty()
        if (id.isBlank()) return false
        return PERICIAS_COMBATE_DISTANCIA.any { it == id } ||
            // O id da tela pode vir com a especialização colada
            // ("armas_de_fogo_nt_pistola"). Prefixo basta.
            PERICIAS_COMBATE_DISTANCIA.any { id.startsWith(it) }
    }

    /**
     * A pergunta que a tela faz.
     *
     * [arma] é a fonte de dano escolhida (null quando é "Dano ST"), [periciaId] é
     * o ataque selecionado.
     */
    fun ehADistancia(arma: Equipamento?, periciaId: String?): Boolean {
        if (arma != null) {
            // Arma na mão manda: se ela é de corpo a corpo, é corpo a corpo,
            // mesmo que a perícia selecionada seja de arco.
            if (tipoEhADistancia(arma.armaTipoCombate)) return true
            if (!arma.armaTipoCombate.isNullOrBlank()) return false
            // Ficha antiga sem o campo: cai no alcance máximo, que só arma de
            // longe tem.
            if (arma.armaMaximoMetros != null) return true
        }
        return periciaEhADistancia(periciaId)
    }

    /**
     * **Qual arma está atirando** — a pergunta que faltava (achado de 29/07).
     *
     * ## O defeito que isto conserta
     *
     * Os avisos de `Máx` e `1/2D` nunca apareciam. A tela só olhava a **fonte de
     * dano** escolhida, e o normal na aba Rolagem é ela ficar em **"Dano ST"** —
     * o padrão. Com isso a arma vinha nula, o alcance vinha vazio, e nenhum aviso
     * tinha como sair. O usuário achou testando o T-D8 com uma arqueira que tem
     * arco na ficha.
     *
     * Analogia: era como perguntar "qual arma?" olhando só para o campo do dano.
     * Se o jogador não trocou aquele campo, a resposta era "nenhuma" — mesmo com
     * o arco na mão e a perícia Arcos selecionada.
     *
     * ## A escada de decisão
     *
     * 1. **Arma escolhida na fonte de dano.** É a resposta mais precisa: o
     *    jogador disse explicitamente com o que está batendo.
     * 2. **A arma de longe cujo grupo casa com a perícia** do ataque — "ARCO"
     *    para Arcos, "PISTOLA" para Armas de Fogo (Pistola).
     * 3. **A única arma de longe da ficha.** Quem tem um arco só não deveria ter
     *    de explicar qual arco.
     *
     * Devolve null quando nenhuma das três resolve — e aí a tela simplesmente
     * não avisa nada, que é melhor que avisar errado.
     */
    fun armaDoAtaque(
        armas: List<Equipamento>,
        armaSelecionada: Equipamento?,
        periciaId: String?
    ): Equipamento? {
        if (armaSelecionada != null) return armaSelecionada

        val deLonge = armas.filter { tipoEhADistancia(it.armaTipoCombate) || it.armaMaximoMetros != null }
        if (deLonge.isEmpty()) return null

        val pericia = normalizar(periciaId)
        if (pericia.isNotBlank()) {
            deLonge.firstOrNull { arma ->
                val grupo = normalizar(arma.armaGrupo)
                grupo.isNotBlank() && (pericia.startsWith(grupo) || grupo.startsWith(pericia) ||
                    pericia.contains(grupo) || grupo.contains(pericia))
            }?.let { return it }
        }

        return deLonge.singleOrNull()
    }

    /** Minúsculas, sem acento, sublinhado vira espaço. */
    private fun normalizar(raw: String?): String {
        val semAcento = java.text.Normalizer.normalize(raw.orEmpty(), java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return semAcento.lowercase()
            .removePrefix("racial_")
            .replace('_', ' ')
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * O alcance da arma em metros: `1/2D` e `Máx`, já resolvidos.
     *
     * ⚠️ **Arco não tem alcance fixo.** O da tabela vem como múltiplo da ST de
     * quem empunha (`×10/×15`): o mesmo arco vai muito mais longe numa ST 12 do
     * que numa ST 9. Por isso [st] entra aqui — e quando o jogador liga a ST
     * Braçal na aba Rolagem, é a força dos braços que deve chegar.
     *
     * Devolve null nos campos que a ficha não conhece; ficha antiga tem tudo
     * nulo, e nesse caso a tela simplesmente não avisa nada.
     */
    data class Alcance(
        val meioDano: Int?,
        val maximo: Int?,
        /** Prec da arma — o bônus que só vale se o personagem Apontou. */
        val precisao: Int? = null
    )

    fun alcanceDe(arma: Equipamento?, st: Int): Alcance {
        if (arma == null) return Alcance(null, null, null)

        val mult = arma.armaAlcanceMultStRaw?.let { multiplicadores(it) }
        val meio = arma.armaMeioDanoMetros ?: mult?.first?.let { it * st }
        val max = arma.armaMaximoMetros ?: mult?.second?.let { it * st }
        return Alcance(meio, max, arma.armaPrecisao)
    }

    /**
     * Lê "×10/×15" e devolve (10, 15).
     *
     * Aceita `x` e `×`, com ou sem espaço. Formato estranho devolve null em vez
     * de chutar — número inventado de alcance é pior que nenhum.
     */
    private fun multiplicadores(raw: String): Pair<Int, Int>? {
        val numeros = Regex("\\d+").findAll(raw).map { it.value.toIntOrNull() }.toList()
        if (numeros.size < 2) return null
        val a = numeros[0] ?: return null
        val b = numeros[1] ?: return null
        return a to b
    }
}
