package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A trava entre **Abascanto** e **Aptidão Mágica** (Lote RESIST-2, MB p.85).
 *
 * Decisão do usuário em 28/07: bloqueio mútuo e automático.
 *
 * Eu havia levantado o risco de repetir o erro do `conhecimento_oculto`, que
 * bloqueava uma compra legítima. **Não é o mesmo caso:** lá o pré-requisito era
 * *"a critério do Mestre"* — decisão de mesa, que o app não conhece. Aqui o
 * livro é categórico.
 *
 * O outro cuidado que os testes cobrem: a trava vale para **adicionar**, nunca
 * para ficha já salva.
 */
class IncompatibilidadeDeTracosTest {

    private val abascanto = IncompatibilidadeDeTracos.ID_ABASCANTO
    private val aptidao = IncompatibilidadeDeTracos.ID_APTIDAO_MAGICA

    private fun fichaCom(id: String) = Personagem(
        nome = "Teste",
        vantagens = listOf(VantagemSelecionada(definicaoId = id, nome = id))
    )

    @Test
    fun `com Aptidao Magica na ficha, o Abascanto e recusado`() {
        val motivo = IncompatibilidadeDeTracos.motivoParaRecusar(fichaCom(aptidao), abascanto)
        assertTrue("deveria recusar", motivo != null)
        assertTrue("a mensagem tem que dizer o porque: $motivo", motivo!!.contains("p.85"))
    }

    @Test
    fun `com Abascanto na ficha, a Aptidao Magica e recusada`() {
        // A trava e MUTUA: tanto faz qual das duas foi comprada primeiro.
        assertTrue(
            IncompatibilidadeDeTracos.motivoParaRecusar(fichaCom(abascanto), aptidao) != null
        )
    }

    @Test
    fun `as duas mensagens sao a mesma - e uma regra so`() {
        assertEquals(
            IncompatibilidadeDeTracos.motivoParaRecusar(fichaCom(aptidao), abascanto),
            IncompatibilidadeDeTracos.motivoParaRecusar(fichaCom(abascanto), aptidao)
        )
    }

    @Test
    fun `ficha vazia aceita qualquer uma das duas`() {
        val vazia = Personagem(nome = "Teste")
        assertNull(IncompatibilidadeDeTracos.motivoParaRecusar(vazia, abascanto))
        assertNull(IncompatibilidadeDeTracos.motivoParaRecusar(vazia, aptidao))
    }

    @Test
    fun `vantagem sem relacao nenhuma passa`() {
        assertNull(IncompatibilidadeDeTracos.motivoParaRecusar(fichaCom(aptidao), "pendulear"))
    }

    @Test
    fun `a mesma vantagem duas vezes nao e caso desta trava`() {
        // Duplicata ja tem tratamento proprio no delegate; aqui nao pode dar
        // mensagem de incompatibilidade, que confundiria o jogador.
        assertNull(IncompatibilidadeDeTracos.motivoParaRecusar(fichaCom(abascanto), abascanto))
    }

    @Test
    fun `a mensagem diz o que fazer, nao so que nao pode`() {
        val motivo = IncompatibilidadeDeTracos.motivoParaRecusar(fichaCom(aptidao), abascanto)!!
        assertTrue(motivo, motivo.contains("Remova uma"))
    }

    @Test
    fun `todo par declarado tem motivo escrito`() {
        // Sem motivo, o jogador leva um "nao pode" sem explicacao -- e a pior
        // experiencia possivel numa ficha de RPG.
        IncompatibilidadeDeTracos.PARES.forEach { par ->
            assertTrue("par ${par.umId}/${par.outroId} sem motivo", par.motivo.length > 20)
        }
    }
}
