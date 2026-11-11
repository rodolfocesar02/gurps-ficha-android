package com.gurps.ficha.model

import com.google.gson.JsonObject

/**
 * **A munição que a arma leva consigo** — IC-3B do `mesa-virtual/planos/itens/PLANO.md` (pedido do Rodolfo em 11/nov:
 * "os itens que dependem de munição tenham um campo para aquele tipo de munição, em vez de um item específico 'munição'").
 *
 * É um sub-campo da arma (`Equipamento.municoes`). O "Tiros" da arma continua sendo o carregador (`armaTirosRaw`, "30+1(3)");
 * a `quantidade` daqui é o estoque que o personagem carrega para ela. A lista existe para a munição especial (ponta oca,
 * perfurante, flecha de ponta fina): cada tipo é uma linha, com o `itemCatalogoId` e a `regra` do catálogo de itens.
 * A Mesa gasta 1 por tiro, do primeiro tipo da lista que ainda tem.
 */
data class MunicaoDaArma(
    val nome: String = NOME_COMUM,
    val quantidade: Int = 0,
    val itemCatalogoId: String? = null,
    val regra: JsonObject? = null
) {
    companion object {
        const val NOME_COMUM = "Munição comum"
    }
}

/**
 * **O acessório preso à arma** (mira laser, luneta, silenciador) — IC-3B. Mora dentro da arma (`Equipamento.acessorios`),
 * e por isso não há dúvida de em qual arma ele está. A `regra` vem do catálogo de itens (tipo "acessorio").
 */
data class AcessorioDaArma(
    val nome: String = "",
    val itemCatalogoId: String? = null,
    val regra: JsonObject? = null,
    val pesoKg: Float = 0f,
    val custo: Float = 0f
)
