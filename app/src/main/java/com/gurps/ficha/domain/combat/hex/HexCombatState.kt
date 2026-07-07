package com.gurps.ficha.domain.combat.hex

/**
 * Lote HEX-3 (Fase 2b do PILAR): estado POSICIONAL do combate tático — quem está onde, olhando pra onde.
 *
 * Kotlin PURO — sem Android. Trabalha em paralelo ao `CombatSession` (que continua intocado): antes de cada
 * ação, o controller sincroniza a distância em metros do encounter com a distância em hex daqui (1 hex = 1m).
 *
 * NÃO substitui o CombatSession — só adiciona a camada de POSIÇÃO. Todas as regras (ataque, defesa, dano,
 * ferimento) continuam no motor original; a diferença é que a distância vira uma FUNÇÃO das posições, não
 * um número que a UI escolhe.
 */
data class PosicaoCombatente(
    val id: String,               // mesmo id do Combatente no encounter
    val posicao: HexCoord,
    val facing: Direcao = Direcao.LESTE
)

data class HexCombatState(
    val posicoes: List<PosicaoCombatente>,
    val hexSelecionado: HexCoord? = null,
    val idSelecionado: String? = null,
    val raioGrade: Int = 7
) {
    /** Todos os hexes visíveis (para desenho). */
    val hexesVisiveis: List<HexCoord> get() = HexGrid.range(HexCoord.ORIGEM, raioGrade)

    /** Distância em HEXES (= metros) entre 2 combatentes. Retorna null se algum id não existir. */
    fun distanciaHex(a: String, b: String): Int? {
        val pa = posicoes.firstOrNull { it.id == a } ?: return null
        val pb = posicoes.firstOrNull { it.id == b } ?: return null
        return pa.posicao.distancia(pb.posicao)
    }

    /** Move o combatente [id] se [destino] estiver ADJACENTE e livre; senão devolve `this`. */
    fun mover(id: String, destino: HexCoord): HexCombatState {
        val p = posicoes.firstOrNull { it.id == id } ?: return this
        if (p.posicao.distancia(destino) != 1) return this
        if (posicoes.any { it.id != id && it.posicao == destino }) return this
        val novoFacing = Direcao.de(p.posicao, destino) ?: p.facing
        val novas = posicoes.map { if (it.id == id) it.copy(posicao = destino, facing = novoFacing) else it }
        return copy(posicoes = novas, hexSelecionado = destino, idSelecionado = id)
    }

    /** Toque num hex: seleciona token, move token selecionado para vizinho vazio, ou só destaca. */
    fun aoTocarHex(hex: HexCoord): HexCombatState {
        val tokenAli = posicoes.firstOrNull { it.posicao == hex }
        if (idSelecionado != null && tokenAli == null) {
            val movido = mover(idSelecionado, hex)
            if (movido != this) return movido
        }
        if (tokenAli != null) return copy(hexSelecionado = hex, idSelecionado = tokenAli.id)
        return copy(hexSelecionado = hex)
    }

    companion object {
        /** Cena inicial: herói na origem (olhando pro leste), 1 goblin 3 hexes ao leste (olhando pro oeste). */
        fun setupInicial(idHeroi: String = "heroi", idInimigo: String = "goblin_1"): HexCombatState =
            HexCombatState(posicoes = listOf(
                PosicaoCombatente(idHeroi, HexCoord.ORIGEM, Direcao.LESTE),
                PosicaoCombatente(idInimigo, HexCoord(3, 0), Direcao.OESTE)
            ))
    }
}
