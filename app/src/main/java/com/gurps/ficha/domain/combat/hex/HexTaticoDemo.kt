package com.gurps.ficha.domain.combat.hex

/**
 * Lote HEX-2 (Fase 2a do PILAR): estado DEMO da grade tática — o mínimo para provar que a grade desenha,
 * posiciona tokens e responde ao toque. NÃO integra com CombatSession ainda (isso é o HEX-3).
 *
 * Kotlin PURO — a UI (HexCanvas) consome este estado. Mantém a lógica testável sem Android.
 */
data class TokenDemo(
    val id: String,
    val nome: String,
    val posicao: HexCoord,
    val ehHeroi: Boolean,
    val facing: Direcao = Direcao.LESTE
)

/**
 * Estado imutável da tela tática DEMO. A UI redesenha quando este estado muda. `hexSelecionado` é o hex
 * que o jogador tocou (destaque visual) ou null se nada tocado. `tokenSelecionadoId` acompanha para o
 * caso de o jogador tocar num token para "escolher quem mover" antes de tocar no hex-destino.
 */
data class HexTaticoState(
    val tokens: List<TokenDemo>,
    val hexSelecionado: HexCoord? = null,
    val tokenSelecionadoId: String? = null,
    /** Raio da grade em torno da origem. HEX-2 usa raio pequeno (7) para caber na tela — depois será dinâmico. */
    val raioGrade: Int = 7
) {
    /** Todos os hexes visíveis na grade DEMO — origem em (0,0) e raio [raioGrade]. */
    val hexesVisiveis: List<HexCoord> get() = HexGrid.range(HexCoord.ORIGEM, raioGrade)

    /** Movimenta o token [id] para [destino] SE for adjacente à posição atual dele. Sem validação de regra ainda. */
    fun mover(id: String, destino: HexCoord): HexTaticoState {
        val t = tokens.firstOrNull { it.id == id } ?: return this
        // Movimento demo: só permite ir a um hex vizinho (adjacente). Suficiente pra provar input.
        if (t.posicao.distancia(destino) != 1) return this
        // Se algum outro token já ocupa o destino, não move (colisão ingênua).
        if (tokens.any { it.id != id && it.posicao == destino }) return this
        val novoFacing = Direcao.de(t.posicao, destino) ?: t.facing
        val novos = tokens.map { if (it.id == id) it.copy(posicao = destino, facing = novoFacing) else it }
        return copy(tokens = novos, hexSelecionado = destino, tokenSelecionadoId = id)
    }

    /**
     * Lógica de toque num hex: se há token selecionado e o hex é vizinho → tenta mover; senão só
     * seleciona/deseleciona o hex e o token ali (se houver). A UI chama este método e re-renderiza.
     */
    fun aoTocarHex(hex: HexCoord): HexTaticoState {
        val tokenAli = tokens.firstOrNull { it.posicao == hex }
        // 1) Toquei num hex vizinho ao token selecionado e o hex está livre → move.
        if (tokenSelecionadoId != null && tokenAli == null) {
            val movido = mover(tokenSelecionadoId, hex)
            if (movido != this) return movido
        }
        // 2) Toquei num token → seleciona ele.
        if (tokenAli != null) return copy(hexSelecionado = hex, tokenSelecionadoId = tokenAli.id)
        // 3) Toquei em hex vazio → apenas destaca o hex, mantém token selecionado.
        return copy(hexSelecionado = hex)
    }

    companion object {
        /** Configuração inicial DEMO: herói na origem, 1 goblin 3 hexes ao leste. */
        fun demoInicial(): HexTaticoState = HexTaticoState(
            tokens = listOf(
                TokenDemo("heroi", "Herói", HexCoord.ORIGEM, ehHeroi = true, facing = Direcao.LESTE),
                TokenDemo("inimigo1", "Goblin", HexCoord(3, 0), ehHeroi = false, facing = Direcao.OESTE)
            )
        )
    }
}
