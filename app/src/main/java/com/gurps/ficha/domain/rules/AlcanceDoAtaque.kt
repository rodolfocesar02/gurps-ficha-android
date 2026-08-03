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
    /**
     * A pergunta que a tela faz.
     *
     * ## 🔴 Por que a arma deixou de "mandar" (Lote ARMA-5)
     *
     * A versão anterior dizia *"arma na mão manda"* e, se a arma fosse de corpo
     * a corpo, **calava a perícia**:
     *
     * ```
     * if (!arma.armaTipoCombate.isNullOrBlank()) return false
     * ```
     *
     * Isso foi escrito para o caso *oposto* — a faca de arremesso empunhada com
     * a perícia Faca — e nunca previu a combinação incoerente. Achado por você
     * em 03/08 com um print: o cabeçalho dizia `Ataque Armas de Fogo/NT
     * (pistola)` e o diálogo abria com **Golpe Rápido**, sem linha de distância
     * e sem Apontar, porque a fonte de dano estava numa arma de corpo a corpo.
     *
     * Perder a distância, o 1/2D, o Máx e o Apontar inteiro **sem nenhum aviso**
     * é o pior tipo de erro. Agora **qualquer um dos dois lados** que diga
     * "longe" basta, e a divergência vira texto na tela ([conflito]).
     */
    fun ehADistancia(arma: Equipamento?, periciaId: String?): Boolean {
        if (arma != null) {
            if (tipoEhADistancia(arma.armaTipoCombate)) return true
            // Ficha antiga sem o campo: cai no alcance máximo, que só arma de
            // longe tem.
            if (arma.armaTipoCombate.isNullOrBlank() && arma.armaMaximoMetros != null) return true
        }
        return periciaEhADistancia(periciaId)
    }

    /**
     * **A perícia e a arma discordam?** — e, se sim, o que dizer.
     *
     * Devolve null quando estão coerentes (o normal). Quando não estão, devolve
     * a frase que a tela mostra: o app segue a perícia, que é o ataque que o
     * jogador tocou, mas **diz que seguiu** em vez de escolher calado.
     */
    fun conflito(arma: Equipamento?, periciaId: String?): String? {
        val nome = arma?.nome?.takeIf { it.isNotBlank() } ?: return null
        val tipo = arma.armaTipoCombate
        if (tipo.isNullOrBlank()) return null
        val armaDeLonge = tipoEhADistancia(tipo)
        val periciaDeLonge = periciaEhADistancia(periciaId)
        if (armaDeLonge == periciaDeLonge) return null
        return if (periciaDeLonge) {
            "O ataque é à distância, mas a fonte de dano é $nome, " +
                "que é de corpo a corpo — confira a arma."
        } else {
            "O ataque é de corpo a corpo, mas a fonte de dano é $nome, " +
                "que é de longe — confira a arma."
        }
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
        // ⚠️ Lote ARMA-5: só vale a escolhida se ela for mesmo de longe. Antes,
        // uma adaga selecionada na fonte de dano virava "a arma do tiro" e
        // levava Precisão nula e alcance vazio para o diálogo — com a pistola
        // ali do lado na mesma ficha.
        if (armaSelecionada != null && tipoEhADistancia(armaSelecionada.armaTipoCombate)) {
            return armaSelecionada
        }
        if (armaSelecionada != null && armaSelecionada.armaTipoCombate.isNullOrBlank() &&
            armaSelecionada.armaMaximoMetros != null
        ) {
            return armaSelecionada
        }

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
        val precisao: Int? = null,
        /**
         * O `+N` da **mira acoplada** (Lote ARMA-5). Nulo quando a arma não tem
         * mira embutida — e é o nulo que decide se a caixinha aparece na tela.
         */
        val precisaoAcessorio: Int? = null
    )

    fun alcanceDe(arma: Equipamento?, st: Int): Alcance {
        if (arma == null) return Alcance(null, null, null)

        val mult = arma.armaAlcanceMultStRaw?.let { multiplicadores(it) }
        val meio = arma.armaMeioDanoMetros ?: mult?.first?.let { it * st }
        val max = arma.armaMaximoMetros ?: mult?.second?.let { it * st }
        return Alcance(meio, max, arma.armaPrecisao, arma.armaPrecisaoAcessorio?.takeIf { it > 0 })
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
