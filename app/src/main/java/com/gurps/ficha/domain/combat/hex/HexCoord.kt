package com.gurps.ficha.domain.combat.hex

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Lote HEX-1 (Fase 1 do PILAR): coordenadas de hexágono para o combate tático do Saga.
 *
 * Convenção AXIAL (q, r) com layout POINTY-TOP (topo pontudo) — padrão da indústria de RPG táticos.
 * Referência canônica: Red Blob Games — Hexagonal Grids (redblobgames.com/grids/hexagons).
 * A conversão para CUBE (x, y, z) é usada só para métricas (distância, arredondamento) — o storage é axial.
 *
 * Escala do Saga: 1 hex = 1 metro (padrão do tabuleiro GURPS, MB p.366).
 *
 * Kotlin PURO — zero dependência Android, testável sem UI, não toca no combate atual.
 */
data class HexCoord(val q: Int, val r: Int) {

    /** Componente `s` (cube) implícito: q + r + s = 0. Útil para distância e arredondamento. */
    val s: Int get() = -q - r

    /** Soma vetorial (útil para "vizinho na direção D" = pos + D.vetor). */
    operator fun plus(other: HexCoord): HexCoord = HexCoord(q + other.q, r + other.r)

    /** Diferença vetorial (útil para descobrir a direção relativa entre dois hexes adjacentes). */
    operator fun minus(other: HexCoord): HexCoord = HexCoord(q - other.q, r - other.r)

    /** Distância em HEXES (norma de cube ÷ 2). Simétrica e não-negativa. */
    fun distancia(outro: HexCoord): Int {
        val dq = q - outro.q; val dr = r - outro.r; val ds = s - outro.s
        return (abs(dq) + abs(dr) + abs(ds)) / 2
    }

    companion object {
        /** Origem da grade — usada como referência em testes e como spawn padrão. */
        val ORIGEM = HexCoord(0, 0)
    }
}

/**
 * Seis direções canônicas para POINTY-TOP (a partir de q=+1, sentido horário).
 * A ordem numérica (0..5) é usada como "facing" do combatente e para o cálculo de flanco/costas.
 *
 * Layout (pointy-top):
 *
 *       LESTE (0)
 *        /
 *   NE (5)  SE (1)
 *   |          |
 *   NO (4)  SO (2)
 *        \
 *       OESTE (3)
 */
enum class Direcao(val vetor: HexCoord) {
    LESTE(HexCoord(+1, 0)),
    SUDESTE(HexCoord(0, +1)),
    SUDOESTE(HexCoord(-1, +1)),
    OESTE(HexCoord(-1, 0)),
    NOROESTE(HexCoord(0, -1)),
    NORDESTE(HexCoord(+1, -1));

    /** Direção oposta (usada para "costas" na regra de facing). */
    val oposta: Direcao get() = values()[(ordinal + 3) % 6]

    companion object {
        /** Direção normalizada de A→B (para o hex adjacente ou para o mais próximo em linha reta). Null se A==B. */
        fun de(a: HexCoord, b: HexCoord): Direcao? {
            if (a == b) return null
            // Escolhe a direção cujo vetor mais se alinha com (b - a) — projeção em cube.
            val d = b - a
            var melhor: Direcao? = null
            var maiorProj = Int.MIN_VALUE
            for (dir in values()) {
                val proj = d.q * dir.vetor.q + d.r * dir.vetor.r + d.s * dir.vetor.s
                if (proj > maiorProj) { maiorProj = proj; melhor = dir }
            }
            return melhor
        }
    }
}

/**
 * Onde o ATAQUE de A pega B em relação ao FACING de B (MB p.390 e AM p.104):
 *   FRENTE  → sem modificador especial (defende normalmente)
 *   FLANCO  → −2 nas defesas de B (ver `Facing.penalidadeDefesa`)
 *   COSTAS  → defesa ANULADA (surpresa/pelas costas — cf. `CombatResolver.defesaAnulada`)
 *
 * Regra por HEXES do facing (pointy-top, 6 direções):
 *  - FRENTE  = a direção do FACING de B e as duas imediatamente vizinhas (arco de 3 hexes/180°).
 *  - COSTAS  = a direção OPOSTA ao FACING.
 *  - FLANCO  = as duas restantes (arcos laterais).
 */
enum class Facing(val penalidadeDefesa: Int, val defesaAnulada: Boolean) {
    FRENTE(0, false),
    FLANCO(-2, false),
    COSTAS(0, true);

    companion object {
        /** Facing do ataque de [origemAtaque] contra [alvo] considerando o [facingAlvo]. */
        fun calcular(origemAtaque: HexCoord, alvo: HexCoord, facingAlvo: Direcao): Facing {
            val dirDoAtaque = Direcao.de(alvo, origemAtaque) ?: return FRENTE // mesmo hex → frente por convenção
            // Distância angular entre facing e a direção do ataque (0..3).
            val diff = min(
                abs(dirDoAtaque.ordinal - facingAlvo.ordinal),
                6 - abs(dirDoAtaque.ordinal - facingAlvo.ordinal)
            )
            return when (diff) {
                0, 1 -> FRENTE   // exatamente à frente ou nos hexes vizinhos
                3    -> COSTAS   // exatamente atrás
                else -> FLANCO   // diff == 2 → lateral
            }
        }
    }
}

/** Arredondamento seguro de coordenadas fracionárias (linha reta / LoS) para o hex inteiro mais próximo (algoritmo cube-round). */
internal fun arredondarCube(x: Double, y: Double, z: Double): HexCoord {
    var rx = kotlin.math.round(x).toInt()
    var ry = kotlin.math.round(y).toInt()
    var rz = kotlin.math.round(z).toInt()
    val dx = abs(rx - x); val dy = abs(ry - y); val dz = abs(rz - z)
    // A componente com maior erro é recalculada a partir das outras duas (mantém a invariante x+y+z=0).
    if (dx > dy && dx > dz) rx = -ry - rz
    else if (dy > dz)       ry = -rx - rz
    else                    rz = -rx - ry
    return HexCoord(rx, rz) // (q, r) = (x, z) no mapeamento axial↔cube deste projeto
}

/** Linear interpolation entre dois hexes (usada por [HexGrid.linhaReta] e [HexGrid.linhaDeVisao]). */
internal fun lerpHex(a: HexCoord, b: HexCoord, t: Double): HexCoord {
    val ax = a.q.toDouble(); val az = a.r.toDouble(); val ay = (-a.q - a.r).toDouble()
    val bx = b.q.toDouble(); val bz = b.r.toDouble(); val by = (-b.q - b.r).toDouble()
    return arredondarCube(ax + (bx - ax) * t, ay + (by - ay) * t, az + (bz - az) * t)
}
