package com.gurps.ficha.domain.combat.hex

/**
 * Lote HEX-1 (Fase 1 do PILAR): motor de grade de hexágonos.
 *
 * Kotlin PURO — zero dependência Android. Operações do motor posicional que o combate tático usa:
 *   - vizinhos, range(n), linha reta entre 2 hexes
 *   - LINHA DE VISÃO (LoS) com bloqueio por obstáculos
 *   - facing (frente/flanco/costas) via [Facing.calcular]
 *
 * Convenção: axial pointy-top, 1 hex = 1 metro (padrão GURPS, MB p.366).
 * Ver `HexCoord.kt` para as coordenadas e as 6 direções.
 *
 * A grade não tem tamanho pré-definido — trabalha com o conjunto de hexes "reconhecidos" pelo motor. O
 * bloqueio de LoS é fornecido pelo caller via lambda: HexGrid não decide o que é obstáculo. Isso mantém o
 * motor genérico (obstáculo = parede, uma criatura, cobertura alta…), decidível pela camada superior.
 */
object HexGrid {

    /** Distância em HEXES entre dois pontos. Delegada para [HexCoord.distancia]; exposta aqui por conveniência. */
    fun distancia(a: HexCoord, b: HexCoord): Int = a.distancia(b)

    /** Os 6 vizinhos adjacentes de [c] — na ordem de [Direcao] (0..5). */
    fun vizinhos(c: HexCoord): List<HexCoord> = Direcao.values().map { c + it.vetor }

    /**
     * Todos os hexes a distância ≤ [raio] de [centro], incluindo o próprio centro.
     * Contagem: 3·raio²+3·raio+1 hexes.
     */
    fun range(centro: HexCoord, raio: Int): List<HexCoord> {
        require(raio >= 0) { "raio não pode ser negativo (era $raio)" }
        val out = mutableListOf<HexCoord>()
        for (dq in -raio..raio) {
            val rMin = maxOf(-raio, -dq - raio)
            val rMax = minOf(raio, -dq + raio)
            for (dr in rMin..rMax) out.add(HexCoord(centro.q + dq, centro.r + dr))
        }
        return out
    }

    /**
     * Linha reta em hexes de [a] até [b] INCLUSIVE (RedBlob "hex line drawing").
     * Sempre devolve `distancia(a,b) + 1` hexes; o primeiro é `a` e o último é `b`.
     */
    fun linhaReta(a: HexCoord, b: HexCoord): List<HexCoord> {
        val n = distancia(a, b)
        if (n == 0) return listOf(a)
        val passo = 1.0 / n
        return (0..n).map { i -> lerpHex(a, b, i * passo) }
    }

    /**
     * Linha de visão de [de] até [ate], considerando [bloqueado] como predicado de "esse hex bloqueia visão".
     * Convenção: os hexes das PONTAS ([de] e [ate]) NÃO bloqueiam (senão nunca haveria LoS pra dentro/fora
     * do próprio hex do herói ou do alvo). Se algum hex intermediário bloquear, retorna `false`.
     */
    fun linhaDeVisao(de: HexCoord, ate: HexCoord, bloqueado: (HexCoord) -> Boolean): Boolean {
        val linha = linhaReta(de, ate)
        // Ignora as pontas (índices 0 e last).
        for (i in 1 until linha.lastIndex) {
            if (bloqueado(linha[i])) return false
        }
        return true
    }

    /**
     * Facing do ataque de [origemAtaque] contra [alvo] considerando o [facingAlvo].
     * Espelha [Facing.calcular] — exposto pelo motor para o combate consumir sem depender do enum.
     */
    fun facingDoAtaque(origemAtaque: HexCoord, alvo: HexCoord, facingAlvo: Direcao): Facing =
        Facing.calcular(origemAtaque, alvo, facingAlvo)
}
