package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem

/**
 * Os marcos de PV e PF, e os testes que eles exigem (MB p.419-423).
 *
 * O GURPS não pede teste "quando você toma dano" — pede quando o dano **cruza
 * um marco**. Cair de 10 para 9 PV não é nada; cair de 10 para 4 é ferimento
 * grave; chegar a 0 é risco de desmaio; chegar a −PV é risco de morte.
 *
 * **Por que isto vive na ficha e não no combate.** A aba Rolagem já tem os
 * controles de PV/PF, e o valor atual é persistido no personagem. Baixar o PV
 * ali **é** o evento de dano — funciona no Discord, no papel, em qualquer mesa,
 * sem depender do combate tático do app.
 *
 * **Regra de desenho: isto OFERECE, não rola.** Devolver a lista de testes é
 * tudo o que este objeto faz; quem toca é o jogador. Rolar sozinho esconderia
 * de onde veio o número — o defeito que a zona de dano invisível causou no
 * TOK-9.
 *
 * Kotlin puro e testável, sem Android. Se um dia a frente da Saga quiser o
 * disparo automático dentro do combate, é só chamar daqui.
 */
object MarcosDeVidaRules {

    /** Um teste que o marco exige, já com bônus somado e origem explicada. */
    data class TesteExigido(
        val rotulo: String,
        val alvo: Int,
        val explicacao: String,
        val origens: List<String> = emptyList()
    ) {
        /** Para o TalkBack: número e motivo juntos, sem virar dois toques. */
        val descricaoAcessivel: String
            get() = "$rotulo. Alvo $alvo. $explicacao" +
                if (origens.isEmpty()) "" else " Inclui ${origens.joinToString(", ")}."
    }

    /** Estado contínuo do personagem — mostrado, não rolado. */
    data class EstadoAtual(val rotulo: String, val efeito: String)

    /** Ids das vantagens que somam nos testes de marco. */
    private const val ID_DIFICIL_DE_SUBJUGAR = "dificil_de_subjugar"
    private const val ID_DURO_DE_MATAR = "duro_de_matar"
    private const val ID_BOA_FORMA = "boa_forma"

    /**
     * Testes exigidos ao o PV cair de [pvAntes] para [pvDepois].
     *
     * Só devolve o que o movimento **cruzou**. Cair de 4 para 3 quando já se
     * estava cambaleante não exige nada de novo; e **subir** o PV (cura) nunca
     * exige nada.
     */
    fun testesAoPerderPv(personagem: Personagem, pvAntes: Int, pvDepois: Int): List<TesteExigido> {
        if (pvDepois >= pvAntes) return emptyList()   // cura não dispara nada

        val maximo = personagem.pontosVida.coerceAtLeast(1)
        val ht = personagem.ht
        val testes = mutableListOf<TesteExigido>()

        // 1. Ferimento grave: perda de metade do PV MÁXIMO num golpe só.
        val perda = pvAntes - pvDepois
        if (perda >= maximo / 2) {
            val (bonus, origens) = bonusDe(personagem, ID_DIFICIL_DE_SUBJUGAR)
            testes += TesteExigido(
                rotulo = "Ferimento grave — não cair",
                alvo = ht + bonus,
                explicacao = "Perdeu $perda PV num golpe (metade do PV máximo). " +
                    "Falha: cai no chão e fica atordoado.",
                origens = origens
            )
        }

        // 2. Chegou a 0 ou menos: teste a cada turno para não desmaiar.
        if (pvAntes > 0 && pvDepois <= 0) {
            val (bonus, origens) = bonusDe(personagem, ID_DIFICIL_DE_SUBJUGAR)
            testes += TesteExigido(
                rotulo = "Manter a consciência",
                alvo = ht + bonus,
                explicacao = "PV chegou a $pvDepois. Repete a cada turno; falha = desmaia.",
                origens = origens
            )
        }

        // 3. Cada múltiplo negativo do PV máximo é um teste de morte.
        //    -1×, -2×, -3×, -4× exigem teste; -5× é morte automática.
        marcosDeMorteCruzados(maximo, pvAntes, pvDepois).forEach { multiplo ->
            val (bonus, origens) = bonusDe(personagem, ID_DURO_DE_MATAR)
            testes += TesteExigido(
                rotulo = "Evitar a morte (−${multiplo}× PV)",
                alvo = ht + bonus,
                explicacao = "PV passou de −${multiplo * maximo}. Falha: morre.",
                origens = origens
            )
        }

        return testes
    }

    /**
     * Quais múltiplos de morte o movimento cruzou.
     *
     * Separado para poder testar sozinho: é a conta mais fácil de errar aqui —
     * um golpe grande pode cruzar dois múltiplos de uma vez, e cada um exige
     * seu próprio teste.
     */
    internal fun marcosDeMorteCruzados(maximo: Int, pvAntes: Int, pvDepois: Int): List<Int> =
        (1..4).filter { multiplo ->
            val limite = -multiplo * maximo
            pvAntes > limite && pvDepois <= limite
        }

    /** Se o personagem já morreu de vez (−5× o PV máximo). Sem teste. */
    fun morteAutomatica(personagem: Personagem, pvAtual: Int): Boolean =
        pvAtual <= -5 * personagem.pontosVida.coerceAtLeast(1)

    /**
     * Estados contínuos, para a tela avisar.
     *
     * O PF quase não gera teste — gera estado. É a diferença de entrega entre
     * PV e PF: um pede rolagem, o outro pede aviso.
     */
    fun estadosDe(personagem: Personagem, pvAtual: Int, pfAtual: Int): List<EstadoAtual> {
        val estados = mutableListOf<EstadoAtual>()
        val pvMax = personagem.pontosVida.coerceAtLeast(1)
        val pfMax = personagem.pontosFadiga.coerceAtLeast(1)

        when {
            pvAtual <= -5 * pvMax -> estados += EstadoAtual("Morto", "PV abaixo de −5× o máximo.")
            pvAtual <= 0 -> estados += EstadoAtual(
                "Caído", "Teste de HT a cada turno para continuar consciente."
            )
            pvAtual <= pvMax / 3 -> estados += EstadoAtual(
                "Cambaleante", "Deslocamento e Esquiva pela metade."
            )
        }

        when {
            pfAtual <= -pfMax -> estados += EstadoAtual("Desmaiado", "PF abaixo de −1× o máximo.")
            pfAtual <= 0 -> estados += EstadoAtual(
                "Exausto", "Qualquer esforço extra passa a custar PV."
            )
            pfAtual <= pfMax / 3 -> estados += EstadoAtual(
                "Cansado", "ST e DX caem pela metade."
            )
        }

        return estados
    }

    /**
     * Bônus de uma vantagem específica, mais o da Boa Forma.
     *
     * A Boa Forma dá +1 (5 pts) ou +2 (15 pts) em **todos** os testes de HT, e
     * estes são todos testes de HT — então ela entra em cima de qualquer um.
     */
    private fun bonusDe(personagem: Personagem, idEspecifico: String): Pair<Int, List<String>> {
        val origens = mutableListOf<String>()
        var total = 0

        personagem.vantagens.filter { it.definicaoId == idEspecifico }.forEach { v ->
            val nivel = v.nivel.coerceAtLeast(1)
            total += nivel
            origens += "${v.nome} +$nivel"
        }

        personagem.vantagens.filter { it.definicaoId == ID_BOA_FORMA }.forEach { v ->
            val bonus = if (v.custoEscolhido >= 15) 2 else 1
            total += bonus
            origens += "${v.nome} +$bonus"
        }

        return total to origens
    }
}
