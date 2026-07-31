package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.DesvantagemSelecionada
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Lote RESIST-3** — os testes de lesão que faltavam no diálogo.
 *
 * ## Por que este lote existiu
 *
 * Ideia do usuário: *"o botão Reação e Resistência deveria ter todos os testes,
 * mesmo sem as vantagens presentes"*. Ao varrer o livro atrás de *"teste de
 * HT/Vontade para evitar"* (46 trechos), apareceu um buraco que não era
 * cosmético:
 *
 * > Uma vez que uma lesão incapacitante também constitui um ferimento grave,
 * > Friedrick deve fazer um **teste de HT para não ficar atordoado e cair**.
 * > — MB p.420
 *
 * Esse é provavelmente o teste de HT mais rolado numa sessão de combate, e o
 * app não o tinha.
 *
 * ## ⚠️ O modo de falhar deste lote é o EXCESSO
 *
 * Não é errar o alvo: é somar uma vantagem onde o livro não soma. Três
 * tentações concretas, e cada uma tem teste dizendo **não**:
 *
 * 1. **Difícil de Subjugar** parece caber no atordoamento — mas a vantagem fala
 *    de *"evitar a inconsciência"* (p.54), e o teste é de cair atordoado.
 * 2. **Duro / Fácil de Matar** parecem caber em tudo que é grave — mas os dois
 *    falam de testes onde o fracasso **mata**.
 * 3. **Destemor / Temor** parecem caber em "não perder a pontaria" — mas os dois
 *    falam de **medo**, e perder a mira é concentração.
 */
class ResistenciaLesaoTest {

    private fun heroi(
        ht: Int = 10,
        iq: Int = 10,
        vantagens: List<VantagemSelecionada> = emptyList(),
        desvantagens: List<DesvantagemSelecionada> = emptyList()
    ) = Personagem(
        nome = "T", vitalidade = ht, inteligencia = iq,
        vantagens = vantagens, desvantagens = desvantagens
    )

    /**
     * ⚠️ Casa o rótulo **exato**, e não `contains`.
     *
     * A primeira versão usava `contains`, e "Recuperar-se do atordoamento " (com
     * espaço no fim, para tentar excluir o MENTAL) casava **só** com o MENTAL —
     * porque é ele que tem espaço depois da palavra. O teste do físico media o
     * mental e vice-versa. Nome parecido pede comparação exata.
     */
    private fun alvoDe(p: Personagem, rotulo: String) =
        ResistenciaRules.testesDe(p).first { it.rotulo == rotulo }.alvo

    private fun temTeste(p: Personagem, rotulo: String) =
        ResistenciaRules.testesDe(p).any { it.rotulo == rotulo }

    private companion object {
        const val ATORDOAMENTO = "Evitar atordoamento e queda"
        const val RECUPERAR = "Recuperar-se do atordoamento"
        const val RECUPERAR_MENTAL = "Recuperar-se do atordoamento MENTAL"
        const val ACORDAR = "Acordar"
        const val FATAL = "Resistir ao ferimento fatal"
        const val PONTARIA = "Não perder a pontaria"

        /** Os cinco testes de HT/Vontade do lote — o mental fica de fora. */
        val NOVOS_DE_CORPO = listOf(ATORDOAMENTO, RECUPERAR, ACORDAR, FATAL)
    }

    private fun vant(id: String, nome: String, nivel: Int = 1) =
        VantagemSelecionada(definicaoId = id, nome = nome, nivel = nivel)

    // ==================================================================
    // 1. Os cinco existem para QUALQUER ficha
    // ==================================================================

    @Test
    fun `os testes de lesao aparecem numa ficha sem traco nenhum`() {
        // É o pedido do usuário: a lista ensina a regra, e a linha que não
        // existe deixa o jogador sem saber se é zero ou se o app esqueceu.
        val p = heroi()
        listOf(
            ATORDOAMENTO, RECUPERAR, RECUPERAR_MENTAL, ACORDAR, FATAL, PONTARIA
        ).forEach { assertTrue("faltou '$it'", temTeste(p, it)) }
    }

    @Test
    fun `a ficha limpa mostra TREZE testes, e nenhum deles e o do elixir`() {
        // 8 antigos (menos o elixir, que exige Abascanto) + 6 linhas novas.
        val testes = ResistenciaRules.testesDe(heroi())
        assertEquals(13, testes.size)
        assertTrue(
            "o elixir so aparece com Abascanto",
            testes.none { it.rotulo.contains("elixir") }
        )
    }

    @Test
    fun `os quatro de lesao saem da HT e o da pontaria da Vontade`() {
        val p = heroi(ht = 12, iq = 14)   // Vontade = IQ = 14
        assertEquals(12, alvoDe(p, ATORDOAMENTO))
        assertEquals(12, alvoDe(p, ACORDAR))
        assertEquals(12, alvoDe(p, FATAL))
        assertEquals(14, alvoDe(p, PONTARIA))
    }

    @Test
    fun `⚠️ o atordoamento MENTAL sai da IQ, e nao da HT nem da Vontade`() {
        // O único teste da tela que usa um terceiro atributo. O livro separa as
        // duas metades na mesma frase, e trocar uma pela outra passaria
        // despercebido em qualquer ficha com IQ igual a HT.
        val p = heroi(ht = 12, iq = 9)
        assertEquals(9, alvoDe(p, RECUPERAR_MENTAL))
        assertEquals("o fisico continua na HT", 12, alvoDe(p, RECUPERAR))
    }

    @Test
    fun `⚠️ Boa Forma NAO toca o atordoamento mental`() {
        // Boa Forma e Fora de Forma são do CORPO. O teste mental é da cabeça, e
        // levar `bonusHt` nele seria dar forma física a um teste de IQ.
        val p = heroi(
            iq = 10,
            vantagens = listOf(
                VantagemSelecionada(definicaoId = "boa_forma", nome = "Boa Forma", custoEscolhido = 15)
            )
        )
        assertEquals(10, alvoDe(p, RECUPERAR_MENTAL))
        assertEquals("mas o fisico ganha os +2", 12, alvoDe(p, RECUPERAR))
    }

    // ==================================================================
    // 2. As três tentações — o que NÃO pode somar
    // ==================================================================

    @Test
    fun `⚠️ Dificil de Subjugar NAO entra no atordoamento nem no acordar`() {
        // MB p.54: a vantagem dá +1 nos testes "para verificar se o personagem
        // EVITA A INCONSCIÊNCIA". Cair atordoado não é desmaiar, e acordar é
        // sair da inconsciência, não evitá-la.
        val p = heroi(vantagens = listOf(vant("dificil_de_subjugar", "Difícil de Subjugar", 3)))
        assertEquals("atordoamento nao pode subir", 10, alvoDe(p, ATORDOAMENTO))
        assertEquals("acordar nao pode subir", 10, alvoDe(p, ACORDAR))
        // E onde ela vale de verdade, continua valendo.
        assertEquals("manter a consciencia SIM", 13, alvoDe(p, "Manter a consciência"))
    }

    @Test
    fun `⚠️ Duro e Facil de Matar NAO entram no atordoamento`() {
        // Os dois falam de testes onde o fracasso MATA. Aqui o fracasso derruba.
        val p = heroi(
            vantagens = listOf(vant("duro_de_matar", "Duro de Matar", 4)),
            desvantagens = listOf(
                DesvantagemSelecionada(definicaoId = "facil_de_matar", nome = "Fácil de Matar", nivel = 2)
            )
        )
        assertEquals(10, alvoDe(p, ATORDOAMENTO))
        assertEquals(10, alvoDe(p, RECUPERAR))
        assertEquals(10, alvoDe(p, ACORDAR))
    }

    @Test
    fun `o ferimento fatal SIM leva Duro e Facil de Matar - la o fracasso mata`() {
        // "Em qualquer fracasso, ele morre" (MB p.424). É teste de morte, e leva
        // o mesmo trio do "Evitar a morte".
        val comDuro = heroi(vantagens = listOf(vant("duro_de_matar", "Duro de Matar", 4)))
        assertEquals(14, alvoDe(comDuro, FATAL))
        assertEquals("igual ao Evitar a morte", alvoDe(comDuro, "Evitar a morte"),
            alvoDe(comDuro, FATAL))

        val comFacil = heroi(
            desvantagens = listOf(
                DesvantagemSelecionada(definicaoId = "facil_de_matar", nome = "Fácil de Matar", nivel = 3)
            )
        )
        assertEquals(7, alvoDe(comFacil, FATAL))
    }

    @Test
    fun `o ferimento fatal respeita o piso de 3`() {
        val p = heroi(
            desvantagens = listOf(
                DesvantagemSelecionada(definicaoId = "facil_de_matar", nome = "Fácil de Matar", nivel = 20)
            )
        )
        assertEquals(3, alvoDe(p, FATAL))
    }

    @Test
    fun `⚠️ Destemor e Temor NAO entram no nao perder a pontaria`() {
        // Os dois falam de MEDO (p.55 e p.159). Perder a mira é concentração.
        val comDestemor = heroi(vantagens = listOf(vant("destemor", "Destemor", 3)))
        assertEquals(10, alvoDe(comDestemor, PONTARIA))
        assertEquals("mas o panico SIM sobe", 13, alvoDe(comDestemor, "Verificação de Pânico"))

        val comTemor = heroi(
            desvantagens = listOf(
                DesvantagemSelecionada(definicaoId = "temor", nome = "Temor", nivel = 3)
            )
        )
        assertEquals(10, alvoDe(comTemor, PONTARIA))
        assertEquals(7, alvoDe(comTemor, "Verificação de Pânico"))
    }

    @Test
    fun `⚠️ Suscetivel continua so em doenca e veneno`() {
        // O lote acrescentou quatro testes de corpo; nenhum deles pode ter
        // herdado a penalidade que o app aplica só a dois.
        val p = heroi(
            desvantagens = listOf(
                DesvantagemSelecionada(definicaoId = "suscetivel", nome = "Suscetível", nivel = 3)
            )
        )
        assertEquals(7, alvoDe(p, "Resistir a doença"))
        assertEquals(7, alvoDe(p, "Resistir a veneno"))
        NOVOS_DE_CORPO
            .forEach { assertEquals("$it nao devia cair", 10, alvoDe(p, it)) }
    }

    // ==================================================================
    // 3. O que os testes novos SIM herdam
    // ==================================================================

    @Test
    fun `Fora de Forma pesa nos quatro testes de corpo novos`() {
        // Ela é a única que o livro escreve com "etc.": "-1 em todos os testes
        // de HT para permanecer consciente, evitar a morte, resistir a doenças e
        // venenos, etc." (MB p.143).
        val p = heroi(
            desvantagens = listOf(
                DesvantagemSelecionada(
                    definicaoId = "fora_de_forma", nome = "Fora de Forma", custoEscolhido = -15
                )
            )
        )
        NOVOS_DE_CORPO
            .forEach { assertEquals("$it deveria ter -2", 8, alvoDe(p, it)) }
        // E o mental continua limpo, porque não é teste de corpo.
        assertEquals(10, alvoDe(p, RECUPERAR_MENTAL))
    }

    @Test
    fun `todo teste novo explica QUANDO e rolado, e cita a pagina`() {
        // A explicação é metade do valor da tela: o jogador abre para saber "que
        // teste eu rolo agora?", e um rótulo sem o gatilho não responde.
        val nomesNovos = NOVOS_DE_CORPO + listOf(RECUPERAR_MENTAL, PONTARIA)
        val novos = ResistenciaRules.testesDe(heroi()).filter { it.rotulo in nomesNovos }
        assertEquals(6, novos.size)
        novos.forEach {
            assertTrue("${it.rotulo} sem pagina", it.explicacao.contains("MB p."))
            assertTrue("${it.rotulo} sem explicacao", it.explicacao.length > 30)
        }
    }

    @Test
    fun `nenhum teste da tela desce abaixo do piso, com a ficha mais fraca possivel`() {
        // Varredura: HT e IQ baixos, com todas as desvantagens que empurram para
        // baixo no nível máximo.
        val p = Personagem(
            nome = "T", vitalidade = 3, inteligencia = 3,
            desvantagens = listOf(
                DesvantagemSelecionada(definicaoId = "facil_de_matar", nome = "FM", nivel = 12),
                DesvantagemSelecionada(definicaoId = "temor", nome = "Temor", nivel = 12),
                DesvantagemSelecionada(definicaoId = "suscetivel", nome = "S", nivel = 12),
                DesvantagemSelecionada(
                    definicaoId = "fora_de_forma", nome = "FF", custoEscolhido = -15
                )
            )
        )
        ResistenciaRules.testesDe(p).forEach {
            assertTrue("${it.rotulo} caiu para ${it.alvo}", it.alvo >= PisoDeTeste.MINIMO)
        }
    }
}
