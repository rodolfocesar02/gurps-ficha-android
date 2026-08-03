package com.gurps.ficha.domain.rules

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.model.DesvantagemSelecionada
import com.gurps.ficha.model.Personagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **Lote D-ESTADO** — o interruptor das nove desvantagens temporárias.
 *
 * ## O que este arquivo guarda
 *
 * Três modos de falhar, e cada um tem seção própria abaixo:
 *
 * 1. **O ciclo do grau.** O toque anda desligado → 1 → 2 → … → desligado. Um
 *    ciclo que não fecha deixa o jogador preso no grau mais grave, e um que
 *    fecha cedo demais some com um grau do livro.
 * 2. **A dimensão esquecida.** Lunático mexe em **Vontade** e Sangue Frio em
 *    **Deslocamento**. Se o desenho tivesse parado em DX/IQ/perícia — que era o
 *    plano quando eu conhecia só três clientes — os dois sairiam errados e
 *    ninguém veria.
 * 3. **A propagação indevida.** Penalidade de IQ **não** desce para Vontade e
 *    Percepção. O próprio Lunático prova: ele nomeia Vontade e não fala em IQ.
 */
class EstadosTemporariosTest {

    private fun comEstado(vararg ids: String) = Personagem(
        nome = "T",
        desvantagens = ids.map { DesvantagemSelecionada(definicaoId = it, nome = it) }
    )

    private fun estado(id: String) =
        EstadosTemporarios.CATALOGO.first { it.id == id }

    // ==================================================================
    // 1. O ciclo
    // ==================================================================

    @Test
    fun `o toque cicla pelos graus e volta a zero`() {
        val dor = estado("dor_cronica")   // três graus
        assertEquals(1, dor.proximoGrau(0))
        assertEquals(2, dor.proximoGrau(1))
        assertEquals(3, dor.proximoGrau(2))
        assertEquals("depois do ultimo grau, desliga", 0, dor.proximoGrau(3))
    }

    @Test
    fun `estado de um grau so e liga-desliga, como as Bracais`() {
        val lunatico = estado("lunatico")
        assertEquals(1, lunatico.graus.size)
        assertEquals(1, lunatico.proximoGrau(0))
        assertEquals(0, lunatico.proximoGrau(1))
    }

    @Test
    fun `grau zero nao desconta nada`() {
        assertTrue(EstadosTemporarios.totalDe(mapOf("dor_cronica" to 0)).vazio)
        assertTrue(EstadosTemporarios.totalDe(emptyMap()).vazio)
    }

    @Test
    fun `grau alem do que existe nao estoura, so nao aplica`() {
        // Ficha salva com um grau que o catálogo não tem mais. Devolver Mods()
        // é melhor que derrubar a aba.
        assertTrue(EstadosTemporarios.totalDe(mapOf("lunatico" to 9)).vazio)
        assertTrue(EstadosTemporarios.totalDe(mapOf("id_que_nao_existe" to 2)).vazio)
    }

    // ==================================================================
    // 2. Os números de cada página
    // ==================================================================

    @Test
    fun `Dor Cronica desconta DX, IQ e autocontrole pelo MESMO valor`() {
        // MB p.137: "-2 / -4 / -6 nos testes de DX, IQ e autocontrole". O
        // terceiro alvo é o que a varredura por palavra-chave nunca acharia.
        listOf(1 to -2, 2 to -4, 3 to -6).forEach { (grau, esperado) ->
            val m = EstadosTemporarios.totalDe(mapOf("dor_cronica" to grau))
            assertEquals(esperado, m.atributos["DX"])
            assertEquals(esperado, m.atributos["IQ"])
            assertEquals(esperado, m.autocontrole)
        }
    }

    @Test
    fun `Dorminhoco desconta 1 de IQ e 2 do autocontrole - numeros diferentes`() {
        // MB p.137. Se alguém "unificar" os dois números, a desvantagem muda.
        val m = EstadosTemporarios.totalDe(mapOf("dorminhoco" to 1))
        assertEquals(-1, m.atributos["IQ"])
        assertEquals(-2, m.autocontrole)
        assertNull("Dorminhoco nao toca DX", m.atributos["DX"])
    }

    @Test
    fun `🔴 toda sigla de atributo e uma que o getAtributo RECONHECE`() {
        // O bug que este teste existe para nunca mais deixar passar: eu escrevi
        // `VONT` e a aba Rolagem usa `VON`. O casamento é por TEXTO, então a
        // chave errada não dá erro — o desconto simplesmente some no caminho.
        // No aparelho, o Lunático ficava ligado, o resumo dizia "VONT −2" e a
        // Vontade na tela não mudava.
        //
        // `getAtributo` devolve **10** para sigla desconhecida; uma sigla válida
        // num personagem com atributos diferentes de 10 devolve o valor real.
        val cobaia = Personagem(
            nome = "T", forca = 11, destreza = 12, inteligencia = 13, vitalidade = 14
        )
        val siglas = EstadosTemporarios.CATALOGO
            .flatMap { it.graus }
            .flatMap { it.mods.atributos.keys }
            .distinct()
        assertTrue("nenhum estado mexe em atributo?", siglas.isNotEmpty())
        siglas.forEach { sigla ->
            assertTrue(
                "sigla '$sigla' nao e reconhecida pelo getAtributo da ficha",
                sigla in setOf("ST", "DX", "IQ", "HT", "VON", "PER")
            )
            // E o valor tem de sair do atributo, não do fallback.
            if (sigla in setOf("ST", "DX", "IQ", "HT")) {
                assertTrue(
                    "getAtributo('$sigla') caiu no fallback de 10",
                    cobaia.getAtributo(sigla) != 10
                )
            }
        }
    }

    @Test
    fun `⚠️ Lunatico mexe em VONTADE, nao em IQ`() {
        // A dimensão que não estava no meu desenho de três clientes. E é esta
        // frase do livro que prova que IQ NÃO arrasta Vontade junto: se
        // arrastasse, "-2 em todos os testes de Vontade" seria redundante.
        val m = EstadosTemporarios.totalDe(mapOf("lunatico" to 1))
        assertEquals(-2, m.atributos["VON"])
        assertEquals(-2, m.autocontrole)
        assertNull("Lunatico NAO toca IQ", m.atributos["IQ"])
        assertNull("Lunatico NAO toca DX", m.atributos["DX"])
    }

    @Test
    fun `⚠️ Sangue Frio mexe em DESLOCAMENTO, e de 1 em 1 por 5 graus`() {
        // MB p.155: "-1 no Deslocamento Básico e na DX para CADA 5 °C abaixo".
        // A outra dimensão que faltava no desenho antigo.
        (1..4).forEach { grau ->
            val m = EstadosTemporarios.totalDe(mapOf("sangue_frio" to grau))
            assertEquals(-grau, m.atributos["DX"])
            assertEquals(-grau, m.deslocamento)
        }
    }

    @Test
    fun `Enjoo tem DOIS graus, e o pior e o do fracasso no HT`() {
        // MB p.138: sucesso = "apenas muito enjoado" (-2); fracasso = vomita (-5).
        val enjoado = EstadosTemporarios.totalDe(mapOf("enjoo" to 1))
        val vomitando = EstadosTemporarios.totalDe(mapOf("enjoo" to 2))
        assertEquals(-2, enjoado.pericias)
        assertEquals(-5, vomitando.pericias)
        assertEquals(-2, enjoado.atributos["DX"])
        assertEquals(-5, vomitando.atributos["IQ"])
    }

    @Test
    fun `Flashbacks Incapacitante BLOQUEIA em vez de descontar`() {
        // MB p.141: "as alucinações são tão graves que impedem a utilização de
        // QUALQUER perícia". Não é −20 nem −99: é "não pode".
        val suave = EstadosTemporarios.totalDe(mapOf("flashbacks" to 1))
        val incapacitante = EstadosTemporarios.totalDe(mapOf("flashbacks" to 3))
        assertEquals(-2, suave.pericias)
        assertFalse(suave.bloqueiaPericias)
        assertTrue(incapacitante.bloqueiaPericias)
        assertEquals("bloquear nao e o mesmo que descontar", 0, incapacitante.pericias)
    }

    @Test
    fun `Repugnancia ingerida e PIOR nas pericias, mas igual nos atributos`() {
        // MB p.155: tocou = "-5 em todas as perícias e atributos"; ingeriu =
        // "-5 em todos os atributos e -10 em todas as perícias". A assimetria é
        // do livro e some fácil na hora de copiar.
        val tocou = EstadosTemporarios.totalDe(mapOf("repugnancia" to 1))
        val ingeriu = EstadosTemporarios.totalDe(mapOf("repugnancia" to 2))
        assertEquals(-5, tocou.pericias)
        assertEquals(-10, ingeriu.pericias)
        assertEquals(-5, tocou.atributos["ST"])
        assertEquals("os atributos NAO dobram", -5, ingeriu.atributos["ST"])
        assertEquals(-5, ingeriu.atributos["HT"])
    }

    @Test
    fun `Problemas na Coluna Grave pega DX e IQ, a Suave so DX`() {
        // MB p.154. A IQ −3 da Suave dura "só durante o próximo segundo" e por
        // isso ficou de fora: um interruptor que o jogador tem de lembrar de
        // desligar no turno seguinte erra mais do que acerta.
        val suave = EstadosTemporarios.totalDe(mapOf("problemas_na_coluna" to 1))
        val grave = EstadosTemporarios.totalDe(mapOf("problemas_na_coluna" to 2))
        assertEquals(-3, suave.atributos["DX"])
        assertNull(suave.atributos["IQ"])
        assertEquals(-4, grave.atributos["DX"])
        assertEquals(-4, grave.atributos["IQ"])
    }

    @Test
    fun `Supersensitivo vai de menos 1 a menos 4, em DX e IQ juntos`() {
        // MB p.158: "-1 (...) -2 para 10 ou mais pessoas, -3 para 100, -4 para
        // 1.000".
        (1..4).forEach { grau ->
            val m = EstadosTemporarios.totalDe(mapOf("supersensitivismo" to grau))
            assertEquals(-grau, m.atributos["DX"])
            assertEquals(-grau, m.atributos["IQ"])
        }
    }

    // ==================================================================
    // 3. O que NÃO pode acontecer
    // ==================================================================

    @Test
    fun `⚠️ penalidade de IQ nao desce para Vontade nem Percepcao`() {
        // Seria "natural" propagar, já que as duas nascem da IQ — e estaria
        // errado. Cada estado penaliza exatamente o que a página dele nomeia.
        val m = EstadosTemporarios.totalDe(mapOf("dor_cronica" to 3))
        assertEquals(-6, m.atributos["IQ"])
        assertNull("Vontade nao pode cair junto", m.atributos["VON"])
        assertNull("Percepcao nao pode cair junto", m.atributos["PER"])
    }

    @Test
    fun `dois estados ligados SOMAM, sem um apagar o outro`() {
        val m = EstadosTemporarios.totalDe(
            mapOf("dor_cronica" to 1, "supersensitivismo" to 2)
        )
        assertEquals("-2 da dor e -2 do zumbido", -4, m.atributos["DX"])
        assertEquals(-4, m.atributos["IQ"])
        assertEquals("so a dor mexe no autocontrole", -2, m.autocontrole)
    }

    @Test
    fun `so aparecem os estados que a ficha realmente tem`() {
        val p = comEstado("lunatico", "enjoo")
        val ids = EstadosTemporarios.disponiveis(p).map { it.id }
        assertEquals(setOf("lunatico", "enjoo"), ids.toSet())
        assertTrue(EstadosTemporarios.temAlgum(p))
    }

    @Test
    fun `ficha sem nenhum dos nove nao desenha o painel`() {
        val p = comEstado("timidez", "mau_cheiro")
        assertTrue(EstadosTemporarios.disponiveis(p).isEmpty())
        assertFalse(EstadosTemporarios.temAlgum(p))
    }

    @Test
    fun `a mesma desvantagem duas vezes nao duplica a linha do painel`() {
        val p = comEstado("dor_cronica", "dor_cronica")
        assertEquals(1, EstadosTemporarios.disponiveis(p).size)
    }

    @Test
    fun `o resumo nomeia o traco e o grau, nunca so o numero`() {
        val resumo = EstadosTemporarios.resumoAtivo(mapOf("dor_cronica" to 2))
        assertNotNull(resumo)
        assertTrue(resumo!!, resumo.contains("Dor Crônica"))
        assertTrue(resumo, resumo.contains("Grave"))
        assertTrue(resumo, resumo.contains("DX -4"))
    }

    @Test
    fun `sem nada ligado nao ha resumo - o painel fica limpo`() {
        assertNull(EstadosTemporarios.resumoAtivo(emptyMap()))
        assertNull(EstadosTemporarios.resumoAtivo(mapOf("lunatico" to 0)))
    }

    @Test
    fun `o rotulo desligado ainda ensina QUANDO o estado vale`() {
        // A linha existe mesmo desligada, e é aí que ela ensina a regra —
        // mesmo motivo das linhas de valor zero do DESL-2.
        val rotulo = estado("lunatico").rotulo(0)
        assertTrue(rotulo, rotulo.contains("lua cheia"))
        assertTrue(rotulo, rotulo.contains("p.149"))
    }

    // ==================================================================
    // 4. Contra o catálogo real
    // ==================================================================

    private data class TracoCru(val id: String = "", val nome: String = "")

    @Test
    fun `todo estado do catalogo existe em desvantagens v2 json`() {
        // Um id errado aqui faria o painel nunca aparecer para quem tem a
        // desvantagem — falha invisível, sem erro nenhum.
        val direto = File("src/main/assets/desvantagens.v2.json")
        val arquivo = if (direto.exists()) direto else File("app/src/main/assets/desvantagens.v2.json")
        val tipo = object : TypeToken<List<TracoCru>>() {}.type
        val ids = Gson().fromJson<List<TracoCru>>(arquivo.readText(Charsets.UTF_8), tipo)
            .map { it.id }.toSet()
        val faltando = EstadosTemporarios.CATALOGO.map { it.id }.filterNot { it in ids }
        assertTrue("ids que nao existem no catalogo: $faltando", faltando.isEmpty())
    }

    @Test
    fun `nenhum estado tem grau vazio, que seria uma linha sem efeito`() {
        EstadosTemporarios.CATALOGO.forEach { e ->
            assertTrue("${e.id} sem graus", e.graus.isNotEmpty())
            e.graus.forEach { g ->
                assertTrue("${e.id}/${g.rotulo} nao desconta nada", !g.mods.vazio)
                assertTrue("${e.id}/${g.rotulo} sem rotulo", g.rotulo.isNotBlank())
            }
        }
    }

    @Test
    fun `nenhum estado desconta numero POSITIVO`() {
        // São desvantagens: um sinal trocado viraria bônus silencioso.
        EstadosTemporarios.CATALOGO.forEach { e ->
            e.graus.forEach { g ->
                g.mods.atributos.forEach { (a, v) ->
                    assertTrue("${e.id}/${g.rotulo}: $a com $v", v < 0)
                }
                assertTrue("${e.id}: pericias positivo", g.mods.pericias <= 0)
                assertTrue("${e.id}: autocontrole positivo", g.mods.autocontrole <= 0)
                assertTrue("${e.id}: deslocamento positivo", g.mods.deslocamento <= 0)
            }
        }
    }

    @Test
    fun `os graus pioram na ordem - nunca um grau maior que alivia`() {
        EstadosTemporarios.CATALOGO.forEach { e ->
            e.graus.zipWithNext().forEach { (antes, depois) ->
                if (depois.mods.bloqueiaPericias) return@forEach
                val somaAntes = antes.mods.atributos.values.sum() + antes.mods.pericias +
                    antes.mods.autocontrole + antes.mods.deslocamento
                val somaDepois = depois.mods.atributos.values.sum() + depois.mods.pericias +
                    depois.mods.autocontrole + depois.mods.deslocamento
                assertTrue(
                    "${e.id}: ${depois.rotulo} nao e pior que ${antes.rotulo}",
                    somaDepois <= somaAntes
                )
            }
        }
    }

    @Test
    fun `Fobias NAO esta aqui - ela foi pelo caminho do autocontrole`() {
        // Ela é o décimo cliente da família na leitura, mas a penalidade dela sai
        // do NA, não de um grau escolhido. Fazer as duas coisas daria penalidade
        // dobrada em quem tem fobia.
        assertTrue(EstadosTemporarios.CATALOGO.none { it.id == "fobias" })
    }
}
