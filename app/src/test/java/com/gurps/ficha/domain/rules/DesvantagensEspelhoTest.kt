package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.DesvantagemSelecionada
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Lote D-ESPELHO** — as cinco desvantagens que são o reflexo exato de uma
 * vantagem já automatizada: Fora de Forma, Temor, Suscetível, Suscetibilidade à
 * Magia e Fácil de Matar.
 *
 * ## O que este arquivo está realmente guardando
 *
 * Espelho é a família mais perigosa do catálogo, porque o erro **não aparece**:
 * o número sai bonito na tela, só com o sinal trocado ou aplicado no teste
 * errado. Quatro armadilhas concretas, e cada uma tem teste aqui:
 *
 * 1. **Fácil de Matar toca SÓ o teste de morte**; Fora de Forma toca **todos**
 *    os de corpo. As duas são "−N no HT" na leitura preguiçosa.
 * 2. **Suscetível NÃO é Fora de Forma**: entra só em doença e veneno.
 * 3. **Suscetibilidade à Magia inverte o texto do card** — o mesmo campo que a
 *    Resistência à Magia usa, com o sinal ao contrário.
 * 4. **O piso de 3** existe em três regras diferentes e agora mora em um lugar
 *    só ([PisoDeTeste]).
 *
 * ⚠️ Os testes abaixo afirmam tanto o que **deve** acontecer quanto o que **não
 * pode** — o segundo é o que pega o erro de espelho.
 */
class DesvantagensEspelhoTest {

    private fun heroi(
        ht: Int = 10,
        iq: Int = 10,
        vantagens: List<VantagemSelecionada> = emptyList(),
        desvantagens: List<DesvantagemSelecionada> = emptyList()
    ) = Personagem(
        nome = "Teste",
        vitalidade = ht,
        inteligencia = iq,
        vantagens = vantagens,
        desvantagens = desvantagens
    )

    private fun desv(id: String, nome: String, nivel: Int = 1, custo: Int = 0) =
        DesvantagemSelecionada(
            definicaoId = id, nome = nome, nivel = nivel, custoEscolhido = custo
        )

    private fun testesDe(p: Personagem) = ResistenciaRules.testesDe(p)
    private fun alvoDe(p: Personagem, trecho: String) =
        testesDe(p).first { it.rotulo.contains(trecho) }.alvo

    // ------------------------------------------------------------------
    // 1. O piso, num lugar só
    // ------------------------------------------------------------------

    @Test
    fun `o piso e 3 porque 3 e o menor resultado possivel em 3d6`() {
        assertEquals(3, PisoDeTeste.MINIMO)
        assertEquals(3, PisoDeTeste.aplicar(-40))
        assertEquals(3, PisoDeTeste.aplicar(2))
        assertEquals(3, PisoDeTeste.aplicar(3))
        // Acima do piso ele nao mexe em nada -- e um piso, nao um teto.
        assertEquals(14, PisoDeTeste.aplicar(14))
    }

    @Test
    fun `o teto de niveis com HT 10 e 7 - o exemplo do proprio livro`() {
        // MB p.140 usa exatamente este numero ao falar de Facil de Matar.
        assertEquals(7, PisoDeTeste.tetoDeNiveis(10))
        // Atributo abaixo do piso nao devolve numero negativo: devolve zero.
        assertEquals(0, PisoDeTeste.tetoDeNiveis(2))
    }

    @Test
    fun `Facil de Matar exagerado nao derruba o alvo abaixo de 3`() {
        // Sem o piso, HT 10 com Facil de Matar 12 daria alvo -2: fracasso
        // automatico e permanente. O livro nao quer isso -- ele quer piorar as
        // chances, nao apaga-las.
        val p = heroi(desvantagens = listOf(desv("facil_de_matar", "Fácil de Matar", nivel = 12)))
        assertEquals(3, alvoDe(p, "Evitar a morte"))
    }

    // ------------------------------------------------------------------
    // 2. Fácil de Matar × Fora de Forma — o par que é fácil confundir
    // ------------------------------------------------------------------

    @Test
    fun `Facil de Matar entra SO no teste de morte`() {
        val p = heroi(desvantagens = listOf(desv("facil_de_matar", "Fácil de Matar", nivel = 3)))
        assertEquals("o teste de morte tem de cair", 7, alvoDe(p, "Evitar a morte"))
        // ⚠️ O contrario e o que o livro faz questao de dizer: "isso nao afeta a
        // maioria dos testes normais de HT".
        assertEquals("veneno NAO pode cair", 10, alvoDe(p, "veneno"))
        assertEquals("doença NAO pode cair", 10, alvoDe(p, "doença"))
        assertEquals("esforço NAO pode cair", 10, alvoDe(p, "esforço"))
        assertEquals("consciência NAO pode cair", 10, alvoDe(p, "consciência"))
    }

    @Test
    fun `Fora de Forma entra em TODOS os testes de corpo`() {
        val p = heroi(desvantagens = listOf(desv("fora_de_forma", "Fora de Forma", custo = -5)))
        testesDe(p).filter { it.familia == ResistenciaRules.Familia.CORPO }.forEach {
            assertEquals("${it.rotulo} deveria ter -1", 9, it.alvo)
        }
    }

    @Test
    fun `Muito Fora de Forma vale 2, e quem decide isso e o CUSTO`() {
        // MB p.143: -1 (Fora de Forma) ou -2 (Muito Fora de Forma). O catalogo
        // guarda a escolha como custo (-5 ou -15), igual a Boa Forma do outro
        // lado -- ler o `nivel` daria -1 sempre.
        val leve = heroi(desvantagens = listOf(desv("fora_de_forma", "Fora de Forma", custo = -5)))
        val grave = heroi(desvantagens = listOf(desv("fora_de_forma", "Fora de Forma", custo = -15)))
        assertEquals(9, alvoDe(leve, "veneno"))
        assertEquals(8, alvoDe(grave, "veneno"))
    }

    @Test
    fun `Fora de Forma nao toca os testes de mente`() {
        // "Isso nao reduz sua HT nem as pericias baseadas nesse atributo" -- e
        // Vontade nem entra na conversa.
        val p = heroi(desvantagens = listOf(desv("fora_de_forma", "Fora de Forma", custo = -15)))
        assertEquals(10, alvoDe(p, "Pânico"))
        assertEquals(10, alvoDe(p, "Intimidação"))
    }

    @Test
    fun `Boa Forma e Fora de Forma na mesma ficha se anulam, sem explodir`() {
        // Ficha antiga pode ter as duas. O livro nao proibe o par, entao o app
        // soma os dois e segue -- nao e caso de trava.
        val p = heroi(
            vantagens = listOf(
                VantagemSelecionada(definicaoId = "boa_forma", nome = "Boa Forma", custoEscolhido = 15)
            ),
            desvantagens = listOf(desv("fora_de_forma", "Fora de Forma", custo = -15))
        )
        assertEquals(10, alvoDe(p, "veneno"))
    }

    // ------------------------------------------------------------------
    // 3. Suscetível — o espelho que quase virou "outro Fora de Forma"
    // ------------------------------------------------------------------

    @Test
    fun `Suscetivel entra SO em doenca e veneno`() {
        val p = heroi(desvantagens = listOf(desv("suscetivel", "Suscetível", nivel = 2)))
        assertEquals(8, alvoDe(p, "doença"))
        assertEquals(8, alvoDe(p, "veneno"))
        // ⚠️ Os tres que NAO podem cair. O app nao guarda a QUAL objeto o
        // personagem e suscetivel; espalhar seria inventar regra.
        assertEquals(10, alvoDe(p, "esforço"))
        assertEquals(10, alvoDe(p, "consciência"))
        assertEquals(10, alvoDe(p, "Evitar a morte"))
    }

    @Test
    fun `Suscetivel avisa que a decisao final e do Mestre`() {
        val p = heroi(desvantagens = listOf(desv("suscetivel", "Suscetível", nivel = 1)))
        val doenca = testesDe(p).first { it.rotulo.contains("doença") }
        assertTrue(doenca.explicacao, doenca.explicacao.contains("Mestre"))
        // Sem a desvantagem, o aviso nao pode aparecer -- seria ruido para
        // todo mundo.
        val semNada = testesDe(heroi()).first { it.rotulo.contains("doença") }
        assertFalse(semNada.explicacao, semNada.explicacao.contains("Mestre"))
    }

    @Test
    fun `Suscetivel usa o NIVEL, nao o custo escolhido`() {
        // Armadilha real do catalogo: `suscetivel` tem options [-4,-2,-1], que
        // sao PRECOS POR NIVEL (raridade do objeto), nao a penalidade. Ler o
        // custo daria -4 para quem comprou um unico nivel de "Muito Comum".
        val p = heroi(desvantagens = listOf(desv("suscetivel", "Suscetível", nivel = 1, custo = -4)))
        assertEquals(9, alvoDe(p, "veneno"))
    }

    // ------------------------------------------------------------------
    // 4. Temor — o espelho do Destemor
    // ------------------------------------------------------------------

    @Test
    fun `Temor subtrai da Vontade nos dois testes de mente`() {
        val p = heroi(iq = 12, desvantagens = listOf(desv("temor", "Temor", nivel = 3)))
        assertEquals(9, alvoDe(p, "Pânico"))
        assertEquals(9, alvoDe(p, "Intimidação"))
        // E nao encosta no corpo.
        assertEquals(10, alvoDe(p, "veneno"))
    }

    @Test
    fun `Temor e Destemor se cancelam - sao o mesmo numero com sinal trocado`() {
        val p = heroi(
            vantagens = listOf(VantagemSelecionada(definicaoId = "destemor", nome = "Destemor", nivel = 2)),
            desvantagens = listOf(desv("temor", "Temor", nivel = 2))
        )
        assertEquals(10, alvoDe(p, "Pânico"))
    }

    @Test
    fun `Temor gigante ainda respeita o piso de 3`() {
        // MB p.159: "nao e permitido reduzir o numero alvo do teste de Vontade a
        // um valor menor que 3".
        val p = heroi(desvantagens = listOf(desv("temor", "Temor", nivel = 20)))
        assertEquals(3, alvoDe(p, "Pânico"))
    }

    @Test
    fun `a notinha nomeia a desvantagem, nao so o numero`() {
        // Numero sem origem e caixa preta -- a mesma regra das pericias.
        val p = heroi(desvantagens = listOf(desv("temor", "Temor", nivel = 2)))
        val panico = testesDe(p).first { it.rotulo.contains("Pânico") }
        assertTrue(panico.origens.toString(), panico.origens.any { it.contains("Temor") })
    }

    // ------------------------------------------------------------------
    // 5. Suscetibilidade à Magia — o card que mentiria
    // ------------------------------------------------------------------

    @Test
    fun `Suscetibilidade a Magia devolve numero NEGATIVO no mesmo campo`() {
        val p = heroi(
            desvantagens = listOf(desv("susceptibilidade_a_magia", "Suscetibilidade à Magia", nivel = 3))
        )
        assertEquals(-3, ResistenciaRules.resistenciaAMagia(p))
        assertTrue(ResistenciaRules.ehSuscetivelAMagia(p))
    }

    @Test
    fun `sem nenhum dos dois o campo e zero, e o card nao aparece`() {
        val p = heroi()
        assertEquals(0, ResistenciaRules.resistenciaAMagia(p))
        assertFalse(ResistenciaRules.ehSuscetivelAMagia(p))
    }

    @Test
    fun `Abascanto sozinho continua positivo - o espelho nao inverteu o lado bom`() {
        val p = heroi(
            vantagens = listOf(
                VantagemSelecionada(
                    definicaoId = ResistenciaRules.ID_RESISTENCIA_MAGIA,
                    nome = "Abascanto", nivel = 4
                )
            )
        )
        assertEquals(4, ResistenciaRules.resistenciaAMagia(p))
        assertFalse("com Abascanto o texto NAO pode inverter", ResistenciaRules.ehSuscetivelAMagia(p))
    }

    @Test
    fun `o teste de elixir so existe com Abascanto, nunca com a Suscetibilidade`() {
        // O elixir e um bonus que sai da vantagem. A desvantagem nao cria teste
        // nenhum -- ela so muda o numero que o Mestre aplica do outro lado.
        val comSusc = heroi(
            desvantagens = listOf(desv("susceptibilidade_a_magia", "Suscetibilidade à Magia", nivel = 2))
        )
        assertTrue(
            testesDe(comSusc).none { it.familia == ResistenciaRules.Familia.SOBRENATURAL }
        )
    }
}
