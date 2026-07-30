package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem

/**
 * **Disopia** (MB p.135) — a parte que o `SentidoRules` não cobre.
 *
 * O −6 do teste de Visão já é tratado lá. O que falta é o efeito de **combate**, e
 * ele depende de qual das duas variantes o personagem tem:
 *
 * | | Perto | Longe |
 * |---|---|---|
 * | **Hipermetrope** | −3 em DX para tarefa manual próxima, inclusive combate corporal | — |
 * | **Míope** | −2 nos ataques corpo a corpo | **dobra a distância** até o alvo |
 *
 * ## ⚠️ Por que é caixinha e não automático
 *
 * As duas variantes custam **−25 pontos** e vivem numa **entrada única** do
 * catálogo — a ficha não guarda qual delas o jogador escolheu. Adivinhar pelo
 * texto livre da descrição seria chutar.
 *
 * Então o app **oferece**: no diálogo de Mira aparece a caixinha "Míope — dobra a
 * distância", e quem sabe qual é a sua marca. Mesma filosofia do Rosto Sincero e
 * da ST Braçal: o app faz a conta, o jogador diz quando ela vale.
 */
object DisopiaRules {

    const val ID = "disopia"

    /** Se a ficha tem Disopia — a caixinha só aparece então. */
    fun tem(personagem: Personagem): Boolean =
        personagem.desvantagensTotais.any { it.definicaoId == ID }

    /** O rótulo da caixinha, no diálogo de Mira. */
    const val ROTULO_MIOPE = "Míope — dobra a distância até o alvo (MB p.135)"

    /** O mesmo, para o TalkBack: sem dizer se está marcado. */
    const val ROTULO_ACESSIVEL_MIOPE =
        "Míope. Dobra a distância até o alvo no cálculo do modificador de distância. " +
            "Marque se a sua Disopia for miopia."
}
