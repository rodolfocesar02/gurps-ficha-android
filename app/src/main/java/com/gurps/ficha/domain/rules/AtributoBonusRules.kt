package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.traits.Atributo
import com.gurps.ficha.domain.rules.traits.TraitRuleRegistry
import com.gurps.ficha.model.Personagem

/**
 * Soma dos bônus de atributo vindos de vantagens e desvantagens (GANCHO-A).
 *
 * Mora aqui, e não dentro do `Personagem`, por causa do teto de 1.000 linhas:
 * aquele arquivo já tem mais de 1.100 e só pode ganhar a chamada de uma linha
 * por propriedade. A lógica fica fora.
 *
 * ## Cuidado com recursão
 *
 * `Personagem.pontosVida` chama este objeto, que chama o Registry, que chama as
 * regras. Se uma regra ler `personagem.pontosVida` para decidir seu bônus, o
 * laço nunca fecha e o app trava — sem exceção clara, só congela.
 *
 * A proteção é dupla: o KDoc de `TraitRule.getAttributeModifiers` avisa, e há
 * uma trava de reentrância aqui que devolve 0 em vez de estourar a pilha. Ela
 * não conserta a regra errada, mas evita o app travar por causa de uma.
 */
object AtributoBonusRules {

    /**
     * Guarda por thread: se o cálculo de um atributo reentrar nele mesmo,
     * devolve 0 e sai. Por thread porque combate e UI podem calcular ao mesmo
     * tempo, e um travar o outro seria pior que o bug original.
     */
    private val emCalculo = ThreadLocal.withInitial { mutableSetOf<Atributo>() }

    /** Bônus total de [atributo] vindo dos traços da ficha. */
    fun bonusDe(personagem: Personagem, atributo: Atributo): Int {
        val ativos = emCalculo.get()
        if (!ativos.add(atributo)) {
            // Reentrância: alguma regra leu o atributo que ela mesma modifica.
            return 0
        }
        return try {
            TraitRuleRegistry.getAttributeBonus(personagem, atributo)
        } finally {
            ativos.remove(atributo)
        }
    }
}
