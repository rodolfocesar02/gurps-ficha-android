package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.TipoCusto
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Cobre o custo de desvantagem com Número de Autocontrole (Lote D-3).
 *
 * O código já trazia um comentário sobre a "DUPLA APLICAÇÃO de autocontrole":
 * o diálogo persiste em `custoEscolhido` o valor JÁ multiplicado, então
 * reaplicar o multiplicador ali daria −5 × 0,5 = −2. O contorno era usar sempre
 * o `custoBase` cru quando há autocontrole.
 *
 * Só que isso quebrava o caso oposto: desvantagem de ESCOLHA, onde
 * `custoEscolhido` é a escolha do jogador e NÃO está multiplicada. Flashbacks
 * (−5/−10/−20) escolhido em −20 com NA 12 devolvia **−5** — a escolha ia para o
 * lixo. São 5 desvantagens do catálogo com essa combinação: Atavismo por
 * Estresse, Flashbacks, Obsessão, Vício e Vozes Fantasmagóricas.
 *
 * Os dois casos precisam conviver, e é isso que estes testes travam.
 */
class CustoAutocontroleTest {

    private fun custo(
        tipo: TipoCusto,
        base: Int,
        escolhido: Int,
        na: Int?,
        nivel: Int = 1
    ) = CharacterRules.calcularCustoDesvantagem(
        tipoCusto = tipo, custoBase = base, custoEscolhido = escolhido,
        nivel = nivel, autocontrole = na
    )

    // --- o caso que estava quebrado ---

    @Test
    fun `ESCOLHA com autocontrole respeita a escolha do jogador`() {
        // Flashbacks: opcoes -5/-10/-20. Jogador escolheu -20 (grave), NA 12 (x1).
        assertEquals(-20, custo(TipoCusto.ESCOLHA, base = -5, escolhido = -20, na = 12))
    }

    @Test
    fun `ESCOLHA com autocontrole aplica o multiplicador SOBRE a escolha`() {
        // -20 com NA 15 (x0,5) = -10. Antes dava -2 (base -5 x 0,5).
        assertEquals(-10, custo(TipoCusto.ESCOLHA, base = -5, escolhido = -20, na = 15))
        // -20 com NA 6 (x2) = -40.
        assertEquals(-40, custo(TipoCusto.ESCOLHA, base = -5, escolhido = -20, na = 6))
    }

    @Test
    fun `ESCOLHA sem autocontrole continua valendo a escolha`() {
        assertEquals(-20, custo(TipoCusto.ESCOLHA, base = -5, escolhido = -20, na = null))
    }

    // --- o caso que o contorno original protegia (nao pode regredir) ---

    @Test
    fun `FIXO com autocontrole usa a base crua, nao o custoEscolhido`() {
        // Avareza -10 com NA 15 (x0,5) = -5. O dialogo ja persiste -5 em
        // custoEscolhido; se a conta partisse dali, viraria -2.
        assertEquals(-5, custo(TipoCusto.FIXO, base = -10, escolhido = -5, na = 15))
    }

    @Test
    fun `FIXO com autocontrole 6 dobra a partir da base`() {
        assertEquals(-20, custo(TipoCusto.FIXO, base = -10, escolhido = -20, na = 6))
    }

    @Test
    fun `NA 12 e neutro`() {
        assertEquals(-10, custo(TipoCusto.FIXO, base = -10, escolhido = -10, na = 12))
    }

    // --- outros tipos nao podem ter mudado ---

    @Test
    fun `POR_NIVEL com autocontrole multiplica base vezes nivel`() {
        // -5 por nivel, nivel 3, NA 9 (x1,5) = -22 (truncado).
        assertEquals(-22, custo(TipoCusto.POR_NIVEL, base = -5, escolhido = 0, na = 9, nivel = 3))
    }

    @Test
    fun `FIXO sem autocontrole preserva o custoEscolhido`() {
        // Intolerancia: custoBase -10 mas o escolhido -5 e a verdade.
        assertEquals(-5, custo(TipoCusto.FIXO, base = -10, escolhido = -5, na = null))
    }

    @Test
    fun `ESCOLHA sem valor escolhido cai na base`() {
        // Ficha antiga ou dado incompleto: nao pode virar zero.
        assertEquals(-5, custo(TipoCusto.ESCOLHA, base = -5, escolhido = 0, na = 12))
    }

    @Test
    fun `desvantagem nunca fica com custo positivo`() {
        // Blindagem antiga: valor positivo e invertido.
        assertEquals(-10, custo(TipoCusto.FIXO, base = 10, escolhido = 10, na = 12))
    }
}
