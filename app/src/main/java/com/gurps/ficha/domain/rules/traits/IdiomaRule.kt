package com.gurps.ficha.domain.rules.traits

import com.gurps.ficha.model.ModificadorSelecao
import com.gurps.ficha.model.VantagemSelecionada

/**
 * Regra para Idioma (GURPS p.23).
 *
 * Cada instância representa UM idioma adicional (a língua materna é de graça
 * e fica como anotação). O custo depende do nível de compreensão, que pode ser
 * separado entre FALA e ESCRITA (paga-se metade de cada).
 *
 * Custo cheio por nível: Rudimentar 2, Com Sotaque 4, Materna 6.
 * Cada "metade" (fala OU escrita): Rudimentar 1, Com Sotaque 2, Materna 3.
 * Nenhum = 0. O custo final é a soma das duas metades (fala + escrita).
 *
 * Metadados:
 *   - "nomeIdioma"   -> texto livre (ex: "Inglês")
 *   - "nivelFalado"  -> nenhum | rudimentar | sotaque | materna
 *   - "nivelEscrito" -> nenhum | rudimentar | sotaque | materna
 */
class IdiomaRule : TraitRule {
    override val traitId: String = "idioma"

    override fun calculateCost(
        selection: VantagemSelecionada,
        modifiers: List<ModificadorSelecao>
    ): Int {
        val falado = metadeCusto(selection.metadados?.get("nivelFalado"))
        val escrito = metadeCusto(selection.metadados?.get("nivelEscrito"))
        return falado + escrito
    }

    companion object {
        /** Custo de UMA metade (fala ou escrita) por nível de compreensão. */
        fun metadeCusto(nivel: String?): Int = when (nivel?.lowercase()) {
            "rudimentar" -> 1
            "sotaque", "com_sotaque", "com sotaque" -> 2
            "materna" -> 3
            else -> 0 // nenhum / nulo
        }
    }
}
