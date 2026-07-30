package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.DesvantagemSelecionada
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

    // ==================================================================
    // Lote D-PAR — os doze pares que a leitura das desvantagens revelou
    // ==================================================================

    private fun fichaComDesvantagem(id: String) = Personagem(
        nome = "Teste",
        desvantagens = listOf(DesvantagemSelecionada(definicaoId = id, nome = id))
    )

    @Test
    fun `TODO par e simetrico - tanto faz qual das duas foi comprada primeiro`() {
        // A invariante que importa: a trava e uma so, olhada dos dois lados. Um
        // par que so pega em uma direcao deixa passar metade dos casos -- e do
        // jeito errado, porque depende da ORDEM em que o jogador monta a ficha.
        //
        // O teste poe cada id nos DOIS catalogos de proposito: `motivoParaRecusar`
        // varre `vantagensTotais` e `desvantagensTotais` juntos, e assim o teste
        // nao precisa saber de qual lado cada id mora.
        IncompatibilidadeDeTracos.PARES.forEach { par ->
            listOf(::fichaCom, ::fichaComDesvantagem).forEach { montar ->
                assertTrue(
                    "${par.umId} deveria ser recusado com ${par.outroId} na ficha",
                    IncompatibilidadeDeTracos.motivoParaRecusar(montar(par.outroId), par.umId) != null
                )
                assertTrue(
                    "${par.outroId} deveria ser recusado com ${par.umId} na ficha",
                    IncompatibilidadeDeTracos.motivoParaRecusar(montar(par.umId), par.outroId) != null
                )
            }
        }
    }

    @Test
    fun `todo motivo cita a pagina do livro que proibe`() {
        // O criterio para entrar na lista e "o livro PROIBE", nao "o Mestre
        // decide" -- foi assim que o `conhecimento_oculto` bloqueou uma compra
        // legitima. Exigir a pagina no texto forca quem adicionar um par a ter
        // ido conferir.
        IncompatibilidadeDeTracos.PARES.forEach { par ->
            assertTrue(
                "par ${par.umId}/${par.outroId} nao cita pagina: ${par.motivo}",
                par.motivo.contains(Regex("""p\.\d+"""))
            )
        }
    }

    @Test
    fun `nenhum par esta declarado duas vezes`() {
        // Par repetido nao quebra nada (o `firstOrNull` pega o primeiro), mas
        // significa que alguem colou o bloco e esqueceu de editar -- e a segunda
        // copia pode ter o motivo errado.
        val chaves = IncompatibilidadeDeTracos.PARES.map { setOf(it.umId, it.outroId) }
        assertEquals("ha par repetido em PARES", chaves.size, chaves.toSet().size)
    }

    @Test
    fun `nenhum par tem o mesmo id dos dois lados`() {
        // Um par consigo mesmo travaria a compra da propria desvantagem.
        IncompatibilidadeDeTracos.PARES.forEach {
            assertTrue("par degenerado: ${it.umId}", it.umId != it.outroId)
        }
    }

    @Test
    fun `Paralisia Frente ao Combate bloqueia Reflexos em Combate`() {
        // 🔴 O par mais grave da lista. Reflexos em Combate e das vantagens mais
        // compradas do jogo: sem esta trava dava para ter as duas, e o app
        // somava +1 nas defesas e -2 no panico ao mesmo tempo, com a ficha
        // achando que estava tudo certo.
        val ficha = fichaComDesvantagem("paralisia_frente_ao_combate")
        val motivo = IncompatibilidadeDeTracos.motivoParaRecusar(ficha, "reflexos_em_combate")
        assertTrue("deveria recusar", motivo != null)
        assertTrue(motivo!!, motivo.contains("p.153"))
    }

    @Test
    fun `Cegueira Noturna bloqueia as DUAS visoes, e sao pares separados`() {
        // Visao Noturna e Visao no Escuro sao vantagens diferentes; um par so
        // nao cobriria as duas.
        listOf("visao_noturna", "visao_no_escuro").forEach { visao ->
            assertTrue(
                "cegueira_noturna deveria bloquear $visao",
                IncompatibilidadeDeTracos.motivoParaRecusar(
                    fichaComDesvantagem("cegueira_noturna"), visao
                ) != null
            )
        }
    }

    @Test
    fun `Suscetibilidade a Magia bloqueia Abascanto mas NAO Aptidao Magica`() {
        // MB p.159, literal: "e possivel combinar Suscetibilidade a Magia com
        // Aptidao Magica, mas nao com Abascanto". Travar a Aptidao junto seria
        // proibir o que o livro autoriza na mesma frase.
        val ficha = fichaComDesvantagem("susceptibilidade_a_magia")
        assertTrue(IncompatibilidadeDeTracos.motivoParaRecusar(ficha, abascanto) != null)
        assertNull(
            "a Aptidao Magica e EXPRESSAMENTE permitida com a Suscetibilidade",
            IncompatibilidadeDeTracos.motivoParaRecusar(ficha, aptidao)
        )
    }

    @Test
    fun `Pouca Empatia bloqueia Insensivel e Oblivio - as tres sao desvantagens`() {
        // Caso incomum: aqui os dois lados do par sao DESVANTAGEM. Se a trava
        // so olhasse `vantagensTotais`, este par nunca dispararia.
        listOf("insensivel", "oblivio").forEach { outra ->
            assertTrue(
                "pouca_empatia deveria bloquear $outra",
                IncompatibilidadeDeTracos.motivoParaRecusar(
                    fichaComDesvantagem(outra), "pouca_empatia"
                ) != null
            )
        }
    }

    @Test
    fun `a trava nao impede ABRIR ficha antiga que ja tem as duas`() {
        // ⚠️ A invariante mais importante do lote, e a que nenhum outro teste
        // cobre: `motivoParaRecusar` responde sobre ADICIONAR. Uma ficha salva
        // antes desta regra continua abrindo -- bloquear a abertura seria perder
        // a ficha do jogador por causa de regra que entrou depois.
        val fichaAntiga = Personagem(
            nome = "Antiga",
            vantagens = listOf(VantagemSelecionada(definicaoId = "reflexos_em_combate", nome = "Reflexos")),
            desvantagens = listOf(
                DesvantagemSelecionada(definicaoId = "paralisia_frente_ao_combate", nome = "Paralisia")
            )
        )
        // Nada estoura e nenhum traco some: a ficha e lida inteira.
        assertEquals(1, fichaAntiga.vantagens.size)
        assertEquals(1, fichaAntiga.desvantagens.size)
        // E adicionar uma TERCEIRA coisa sem relacao continua permitido.
        assertNull(IncompatibilidadeDeTracos.motivoParaRecusar(fichaAntiga, "pendulear"))
    }
}
