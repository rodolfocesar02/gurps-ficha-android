package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.traits.DxBracalRule
import com.gurps.ficha.model.Personagem

/**
 * O que a **DX Braçal** faz na mesa (MB p.56).
 *
 * O custo dela mora em `traits/BracalCustoRules.kt`; aqui fica o efeito.
 *
 * Irmã de [StBracalRules], **com uma diferença de regra que o espelho ingênuo
 * erraria**. O livro é explícito:
 *
 * > "As perícias de combate dependem da DX corporal e **não se beneficiam de
 * > forma alguma** da destreza braçal."
 *
 * Ou seja: enquanto a ST Braçal aumenta o **dano** das armas, a DX Braçal
 * **não** aumenta o NH de ataque. Ela vale para ações de mão que não são
 * combate — Arrombamento, Prestidigitação, Cirurgia, Costura.
 *
 * E também **não afeta a Velocidade Básica**, pelo mesmo motivo da ST Braçal
 * não afetar PV: o bônus é do membro, não do corpo.
 *
 * Por isso o seletor da DX Braçal mexe **só no valor de DX rolado** no painel
 * de atributos. Nenhum NH de perícia passa por aqui — o NH vem de
 * `PericiaSelecionada.calcularNivel`, que lê `personagem.dx`, e a DX Braçal
 * nunca entra no `dx` (é escopada, e a regra Kotlin não implementa
 * `getAttributeModifiers`).
 *
 * Kotlin puro e testável, mesmo desenho de [StBracalRules].
 */
object DxBracalRules {

    /**
     * Soma dos níveis de DX Braçal da ficha.
     *
     * O nível É o bônus: "DX Braçal +3" é nível 3.
     */
    fun bonusDe(personagem: Personagem): Int =
        personagem.vantagensTotais
            .filter { it.definicaoId == DxBracalRule.ID }
            .sumOf { it.nivel.coerceAtLeast(1) }

    /** Se a ficha tem alguma DX Braçal — o seletor só aparece então. */
    fun temDxBracal(personagem: Personagem): Boolean = bonusDe(personagem) > 0

    /** A DX que os braços usam: a do corpo mais o bônus. */
    fun dxDosBracos(personagem: Personagem): Int = personagem.dx + bonusDe(personagem)

    /**
     * Rótulo curto do seletor.
     *
     * Traz o aviso do livro junto porque é a pegadinha da vantagem: quem lê
     * "+3 DX" no meio da mesa assume que o ataque melhora, e não melhora.
     */
    fun rotulo(personagem: Personagem): String =
        "DX Braçal +${bonusDe(personagem)} (braços agem como DX " +
            "${dxDosBracos(personagem)}; não vale para combate)"

    /**
     * O mesmo rótulo, escrito para o TalkBack ler.
     *
     * ⚠️ **Não diz se está marcado** — quem anuncia o estado é o TalkBack, pelo
     * papel de caixa de seleção. Ver `UiA11y.linhaAlternavel`.
     */
    fun rotuloAcessivel(personagem: Personagem): String =
        "DX Braçal, mais ${bonusDe(personagem)}. Braços agem como DX " +
            "${dxDosBracos(personagem)}. Vale para tarefas de mão. " +
            "Não vale para perícias de combate."
}
