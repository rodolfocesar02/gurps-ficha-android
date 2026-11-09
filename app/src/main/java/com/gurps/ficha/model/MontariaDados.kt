package com.gurps.ficha.model

/**
 * **A ficha técnica de uma montaria** — RG-11 do `mesa-virtual/planos/regras/PLANO.md` (MB pp.459-460, Animais de Montaria e Carga).
 *
 * Vem do catálogo `assets/montarias.v1.json` e viaja DENTRO do item do inventário (`Equipamento.montaria`), para a Mesa
 * saber o animal sem ler catálogo nenhum. O item entra com peso 0: os 600 kg do cavalo são dele, não do que o jogador
 * carrega (decisão do Rodolfo, 09/nov). Todos os campos têm padrão: ficha antiga (sem o campo) lê como `null`.
 */
data class MontariaDados(
    val id: String = "",
    val nome: String = "",
    val pagina: Int = 0,
    val st: Int = 10,
    val dx: Int = 10,
    val iq: Int = 3,
    val ht: Int = 10,
    val pv: Int = 10,
    val pf: Int = 10,
    val vontade: Int = 10,
    val percepcao: Int = 10,
    val velocidadeBasica: Float = 5f,
    val esquiva: Int = 8,
    val deslocamento: Int = 5,
    val mt: Int = 1,
    val hexes: Int = 3,
    val pesoKg: Float = 0f,
    val caracteristicas: List<String> = emptyList(),
    val pericias: List<String> = emptyList(),
    val custo: Float = 0f,
    val treinadaParaGuerra: Boolean = false,
    val montavel: Boolean = true,
    val rd: Int = 0,
    val descricao: String = ""
)

/** O catálogo como está no arquivo. */
data class CatalogoDeMontarias(val versao: Int = 1, val fonte: String = "", val montarias: List<MontariaDados> = emptyList())
