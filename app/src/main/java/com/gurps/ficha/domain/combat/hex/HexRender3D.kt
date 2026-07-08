package com.gurps.ficha.domain.combat.hex

import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Lote HEX-7 (Fase 5 do PILAR): projeção de [HexCombatState] em coordenadas 3D — kotlin PURO.
 *
 * Isola a matemática de "hex axial → metros no mundo 3D" e "facing → yaw em radianos" para poder testar
 * sem tocar em Android/Filament/SceneView. A camada UI ([com.gurps.ficha.ui.saga.HexScene3D]) chama esta
 * projeção uma vez por frame relevante e monta os `CylinderNode`s do SceneView a partir dos [Token3D].
 *
 * Convenção do mundo 3D:
 *   - Y-up (SceneView/Filament).
 *   - Plano do tabuleiro é XZ, chão em Y=0.
 *   - +X aponta LESTE, +Z aponta SUL (câmera olha do céu para baixo).
 *   - 1 hex = 1 metro (MB p.366) → distância entre centros de vizinhos = 1.0 metro.
 *
 * Fórmula de projeção (pointy-top, axial):
 *   x_mundo = q + r/2
 *   z_mundo = r · √3/2
 *
 * O tamanho "circunscrito" fica 1/√3 ≈ 0.577 m, mas essa constante NÃO precisa ser usada porque as duas
 * fórmulas acima já garantem distância unitária entre vizinhos (verificado nos testes).
 */
object HexRender3D {

    /** Categoria do token — o caller mapeia para MaterialInstance colorida no SceneView. */
    enum class Cor { HEROI, ALIADO, INIMIGO }

    /** Token 3D projetado. Posição em METROS (mundo XZ, Y=0); yaw em RADIANOS. */
    data class Token3D(
        val id: String,
        val x: Float,
        val z: Float,
        val yawRad: Float,
        val cor: Cor,
    )

    /** Converte um [HexCoord] axial em coordenadas (x, z) no mundo 3D, em metros. */
    fun hexParaMundo(c: HexCoord): Pair<Float, Float> {
        val x = (c.q + c.r / 2.0).toFloat()
        val z = (c.r * sqrt(3.0) / 2.0).toFloat()
        return x to z
    }

    /**
     * Yaw em radianos correspondente à [Direcao] no plano XZ (medido de +X anti-horário quando visto de
     * baixo pra cima, ou horário quando visto do céu — depende da convenção da câmera; o caller pode
     * negar/somar π conforme o sistema de rotação do SceneView).
     *
     * LESTE=0, SUDESTE=+π/3, SUDOESTE=+2π/3, OESTE=+π, NOROESTE=−2π/3, NORDESTE=−π/3.
     */
    fun facingParaYaw(d: Direcao): Float {
        val (x, z) = hexParaMundo(HexCoord.ORIGEM + d.vetor)
        return atan2(z.toDouble(), x.toDouble()).toFloat()
    }

    /**
     * Projeta cada combatente do [estado] em um [Token3D]. O caller informa quem é o [idHeroi] e o
     * conjunto de [idsInimigos] — os demais viram ALIADO por padrão (típico: NPCs neutros).
     *
     * A ordem dos tokens segue a ordem de `estado.posicoes` para render determinístico.
     */
    fun projetar(
        estado: HexCombatState,
        idHeroi: String = "heroi",
        idsInimigos: Set<String> = emptySet(),
    ): List<Token3D> = estado.posicoes.map { pos ->
        val (x, z) = hexParaMundo(pos.posicao)
        val cor = when {
            pos.id == idHeroi -> Cor.HEROI
            pos.id in idsInimigos -> Cor.INIMIGO
            else -> Cor.ALIADO
        }
        Token3D(id = pos.id, x = x, z = z, yawRad = facingParaYaw(pos.facing), cor = cor)
    }
}
