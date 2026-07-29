package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem

/**
 * Deslocamentos que **não** são o de andar: voando e escalando.
 *
 * Dois números que o app já tinha tudo para calcular e deixava o jogador fazer de
 * cabeça:
 *
 * - **Voo** (MB p.99): *"O Deslocamento do personagem em voo é igual à sua
 *   Velocidade Básica × 2; descarte todas as frações."*
 * - **Super Escalada** (MB p.91): *"Cada nível adiciona um bônus de +1 ao
 *   Deslocamento do personagem quando ele estiver escalando ou usando a vantagem
 *   Aderência."*
 *
 * ## Por que aparecem só para quem tem
 *
 * Irmão do Deslocamento Aquático, que já fazia assim: a linha só existe se o
 * bônus existir. Uma ficha de humano comum não ganha duas linhas mortas.
 *
 * ## O que NÃO entra aqui
 *
 * As dezenas de ampliações e limitações do Voo — Alado, Planar, Voo Espacial,
 * Mais Leve que o Ar — mudam **como** ele voa, não o número. Isso é narrativa, e
 * o app não tem o que calcular.
 *
 * Kotlin puro e testável.
 */
object DeslocamentosEspeciais {

    const val ID_VOO = "voo"
    const val ID_SUPER_ESCALADA = "super_escalada"
    const val ID_ADERENCIA = "aderencia"

    /** Se a ficha tem Voo — a linha só aparece então. */
    fun podeVoar(personagem: Personagem): Boolean =
        personagem.vantagensTotais.any { it.definicaoId == ID_VOO }

    /**
     * Deslocamento no ar: **Velocidade Básica × 2**, sem frações.
     *
     * ⚠️ Corta a fração (`toInt()`), não arredonda: o livro diz "descarte todas
     * as frações". Velocidade 5,75 dá 11, não 12.
     */
    fun deslocamentoVoando(personagem: Personagem): Int =
        (personagem.velocidadeBasica * 2).toInt()

    /** Níveis de Super Escalada. O nível É o bônus. */
    fun bonusEscalada(personagem: Personagem): Int =
        personagem.vantagensTotais
            .filter { it.definicaoId == ID_SUPER_ESCALADA }
            .sumOf { it.nivel.coerceAtLeast(1) }

    /** Se a ficha tem Super Escalada. */
    fun temSuperEscalada(personagem: Personagem): Boolean = bonusEscalada(personagem) > 0

    /**
     * Deslocamento escalando: o de andar mais o bônus.
     *
     * Parte do **Deslocamento Básico** de propósito, e não da metade dele: o
     * livro fala de bônus "ao Deslocamento", sem dividir. Escalar devagar é
     * assunto da perícia Escalada, não desta vantagem.
     */
    fun deslocamentoEscalando(personagem: Personagem): Int =
        personagem.deslocamentoBasico + bonusEscalada(personagem)

    /** O texto de apoio da linha, que explica quando o bônus vale. */
    fun explicacaoEscalada(personagem: Personagem): String =
        "Deslocamento ${personagem.deslocamentoBasico} + Super Escalada " +
            "${bonusEscalada(personagem)}. Vale escalando ou usando Aderência."
}
