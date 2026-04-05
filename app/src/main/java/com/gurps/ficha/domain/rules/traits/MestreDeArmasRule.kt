package com.gurps.ficha.domain.rules.traits

import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada

/**
 * Regra para a vantagem Mestre de Armas (Weapon Master).
 * Bônus de dano: +1 por dado se NH=DX+1, +2 por dado se NH=DX+2 ou mais.
 */
class MestreDeArmasRule : TraitRule {
    override val traitId: String = "mestre_de_armas"

    override fun getDamageBonusPerDie(
        personagem: Personagem,
        selection: VantagemSelecionada,
        periciaId: String?
    ): Int {
        if (periciaId == null) return 0

        // 1. Verificar se a perícia está coberta (por metadados ou se for "todas")
        val cobertas = selection.metadados?.get("pericias_cobertas") ?: ""
        val listaCobertas = cobertas.split(",").map { it.trim().lowercase() }
        
        val estaCoberta = cobertas.isBlank() || 
                         cobertas.equals("todas", ignoreCase = true) || 
                         listaCobertas.contains(periciaId.lowercase())

        if (!estaCoberta) return 0

        // 2. Encontrar a perícia do personagem para ver o nível (NH)
        val pericia = personagem.pericias.find { it.definicaoId.lowercase() == periciaId.lowercase() } ?: return 0
        val nh = pericia.calcularNivel(personagem)
        val dx = personagem.dx

        // 3. Aplicar regra GURPS
        return when {
            nh >= dx + 2 -> 2
            nh >= dx + 1 -> 1
            else -> 0
        }
    }
}
