package com.gurps.ficha.domain.rules

/**
 * A **ST mínima** de uma arma — MB p.271.
 *
 * > *"A Força mínima necessária para se usar a arma da maneira apropriada. Se
 * > tentar usar uma arma que exige mais ST do que possui, o personagem sofre uma
 * > penalidade de -1 na perícia com a arma para cada ponto de ST que falta e
 * > perde um PF a mais no final de qualquer combate que dure o suficiente para
 * > fatigá-lo."*
 *
 * ## Por que isto virou regra em vez de ficar na tela
 *
 * A lista de escolha já mostrava `ST 11` e, no alto, *"ST do personagem: 9"*. Os
 * dois números estavam lá; **a conta entre eles, não**. Quem escolhe a arma tinha
 * de fazer a subtração de cabeça, e a consequência (o −2 no NH, o PF a mais) não
 * aparecia em lugar nenhum.
 *
 * ⚠️ Aqui só se responde *"a arma pesa mais do que este personagem aguenta?"*. A
 * outra metade da regra da p.271 — o **teto** de dano em 3× a ST mínima — mexe no
 * cálculo do dano e fica de fora deste lote de propósito.
 */
object StMinimaDaArma {

    /**
     * O que falta e o que isso custa.
     *
     * @param faltando pontos de ST que faltam (sempre positivo).
     * @param penalidadeNh o redutor na perícia — um por ponto que falta, logo
     *   sempre o negativo de [faltando]. Fica explícito para quem lê a tela não
     *   ter de saber que a razão é 1 para 1.
     * @param pfExtra o ponto de fadiga a mais no fim de um combate longo.
     */
    data class Falta(val faltando: Int, val penalidadeNh: Int, val pfExtra: Int = 1)

    /**
     * Null quando não há problema: a arma não exige ST, ou o personagem tem de
     * sobra. Só devolve algo quando **falta** força.
     */
    fun avaliar(stDoPersonagem: Int, stMinimaDaArma: Int?): Falta? {
        val exigida = stMinimaDaArma ?: return null
        if (exigida <= 0) return null
        val falta = exigida - stDoPersonagem
        if (falta <= 0) return null
        return Falta(faltando = falta, penalidadeNh = -falta)
    }

    /** O aviso curto que cabe numa linha da lista. */
    fun aviso(falta: Falta): String =
        "Falta ST ${falta.faltando}: ${falta.penalidadeNh} no ataque e ${falta.pfExtra} PF a mais"

    /**
     * O mesmo aviso para quem ouve a tela.
     *
     * ⚠️ Sem sinal cru: `-2` lido em voz alta vira *"dois"* ou *"traço dois"*.
     * `RotulosAcessiveisTest` reprova o sinal, e é por isso que este texto nasce
     * aqui e não dentro do `@Composable` — lá ele ficaria fora da varredura.
     */
    fun descricaoAcessivel(falta: Falta): String =
        "Falta " + RotuloAcessivel.valor(falta.faltando) + " de Força. " +
            "Atacar com esta arma dá " + RotuloAcessivel.modificador(falta.penalidadeNh) +
            " na perícia, e custa " + RotuloAcessivel.valor(falta.pfExtra) +
            " ponto de fadiga a mais no fim de um combate longo."
}
