package com.gurps.ficha.domain.combat.hex

import com.gurps.ficha.domain.combat.LocalAtaque
import com.gurps.ficha.domain.combat.Manobra
import com.gurps.ficha.domain.combat.NpcCombatBrain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Lote HEX-5: IA posicional do NPC (flanquear/kite/aproximar/recuar). Kotlin puro. */
class HexTaticaNpcTest {

    private fun perfilCorpoACorpo(agressividade: Int = 7) = HexTaticaNpc.PerfilTatico(
        agressividade = agressividade, moral = 5, alcanceArmaMetros = 1, temArmaDistancia = false
    )
    private fun perfilArqueiro(alcance: Int = 15) = HexTaticaNpc.PerfilTatico(
        agressividade = 5, moral = 5, alcanceArmaMetros = alcance, temArmaDistancia = true
    )

    private fun intencaoMover(recuar: Boolean = false) = NpcCombatBrain.IntencaoNpc(
        manobra = Manobra.MOVER, alvoId = "heroi", local = LocalAtaque.TORSO,
        aDistancia = false, recuar = recuar, motivo = "teste"
    )

    private fun intencaoAtacar() = NpcCombatBrain.IntencaoNpc(
        manobra = Manobra.ATAQUE, alvoId = "heroi", local = LocalAtaque.TORSO,
        aDistancia = false, recuar = false, motivo = "teste"
    )

    @Test
    fun `nao move quando a intencao e ATAQUE (fica onde esta)`() {
        val estado = HexCombatState.setupInicial()
        val destino = HexTaticaNpc.decidirDestino(estado, "goblin_1", intencaoAtacar(), perfilCorpoACorpo())
        assertNull(destino)
    }

    @Test
    fun `NPC ausente do estado devolve null`() {
        val estado = HexCombatState.setupInicial()
        val destino = HexTaticaNpc.decidirDestino(estado, "fantasma", intencaoMover(), perfilCorpoACorpo())
        assertNull(destino)
    }

    @Test
    fun `recuar maximiza a distancia ao heroi`() {
        // Herói (0,0), goblin (3,0). Recuar deve escolher o vizinho MAIS LONGE do herói (4,0).
        val estado = HexCombatState.setupInicial()
        val destino = HexTaticaNpc.decidirDestino(estado, "goblin_1", intencaoMover(recuar = true), perfilCorpoACorpo())
        assertEquals(HexCoord(4, 0), destino)
    }

    @Test
    fun `agressivo aproxima quando nao consegue flanquear`() {
        // Herói (0,0) olhando LESTE; goblin (3,0). Vizinhos do goblin: (4,0), (2,0), (3,-1), (3,1), (2,1), (4,-1).
        // Aproximar → escolhe o mais próximo (distância 2): (2,0) → esse hex é FRENTE do herói (leste puro,
        // arco frontal de 3 hexes). Flanquear via NORDESTE (3,-1) → distância 3 → também frente pela regra
        // (facing 4 diff 1 = frente). Então cai no aproximar → (2,0).
        val estado = HexCombatState.setupInicial()
        val destino = HexTaticaNpc.decidirDestino(estado, "goblin_1", intencaoMover(), perfilCorpoACorpo(agressividade = 8))
        assertNotNull(destino)
        assertTrue("deve aproximar", HexCoord.ORIGEM.distancia(destino!!) <= HexCoord.ORIGEM.distancia(HexCoord(3, 0)))
    }

    @Test
    fun `agressivo prefere FLANCO ou COSTAS quando dá para chegar num`() {
        // Herói olhando LESTE, NPC vizinho ao sul-oeste em posição que permite flanquear.
        // Pongo o goblin em (0,-1) — vizinho NOROESTE do herói. Ele olha para o herói.
        // Vizinhos do goblin: incluem (-1,-1), (-1,0)=OESTE do herói (arco frontal — direção OESTE=3, facing LESTE=0, diff 3 → COSTAS!),
        //                     e outros.
        val estado = HexCombatState(posicoes = listOf(
            PosicaoCombatente("heroi", HexCoord.ORIGEM, Direcao.LESTE),
            PosicaoCombatente("goblin_1", HexCoord(0, -1), Direcao.LESTE)
        ))
        val destino = HexTaticaNpc.decidirDestino(estado, "goblin_1", intencaoMover(), perfilCorpoACorpo(agressividade = 8))
        assertNotNull(destino)
        // A escolha deve ser tal que o ataque a partir dela NÃO caia em FRENTE do herói.
        val facing = HexGrid.facingDoAtaque(origemAtaque = destino!!, alvo = HexCoord.ORIGEM, facingAlvo = Direcao.LESTE)
        assertNotEquals("agressivo deveria evitar chegar de frente quando pode flanquear", Facing.FRENTE, facing)
    }

    @Test
    fun `arqueiro em kite tenta ficar perto do alcance da arma`() {
        // Goblin a 3 hexes, arqueiro com alcance 3. Melhor kite: ficar em (3,0) (dist 3) ou mover para
        // (4,0)/(3,1)/(3,-1) todos a dist 3-4. O algoritmo prefere distDoIdeal=0.
        val estado = HexCombatState.setupInicial()
        val destino = HexTaticaNpc.decidirDestino(estado, "goblin_1", intencaoMover(), perfilArqueiro(alcance = 3))
        assertNotNull(destino)
        val distAoHeroi = destino!!.distancia(HexCoord.ORIGEM)
        // Não se aproxima abaixo do alcance (não faz sentido pro arqueiro).
        assertTrue("kite deve manter dist ≥ 3 (alcance)", distAoHeroi >= 3)
    }

    @Test
    fun `arqueiro em kite prefere vizinho com cobertura adjacente quando disponivel`() {
        // Coloca "cobertura" em (4,1). O candidato (4,0) tem esse hex como vizinho → deve ser preferido
        // sobre outros candidatos equivalentes em distância.
        val estado = HexCombatState.setupInicial()
        val cobertura = setOf(HexCoord(4, 1))
        val destino = HexTaticaNpc.decidirDestino(
            estado, "goblin_1", intencaoMover(), perfilArqueiro(alcance = 4),
            hexesComCobertura = cobertura
        )
        assertEquals(HexCoord(4, 0), destino)
    }
}
