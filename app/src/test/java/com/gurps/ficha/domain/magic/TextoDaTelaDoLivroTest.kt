package com.gurps.ficha.domain.magic

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.domain.rules.MagiaEnergiaRules
import com.gurps.ficha.model.MagiaDefinicao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Test
import java.io.File

/**
 * A tela mostra o número que é COBRADO.
 *
 * O cabeçalho do catálogo (`energia`, `duracao`, `tempoOperacao`) é a linha da tabela-resumo do livro.
 * Onde a entrada do livro — que vale — diz outro número, o script `numeros.js` grava o texto do número
 * certo (`energiaTela`…) e o carregador troca o cabeçalho por ele ([CicloDaMagia.comTextoDaTela]).
 * Este teste lê o catálogo como o app o lê (Gson em [MagiaDefinicao]) e prova, magia a magia, que o texto
 * da tela é lido pelos leitores do app (a conta da aba Rolagem, a Saga) como o número conferido.
 */
class TextoDaTelaDoLivroTest {

    private val arquivo = listOf("src/main/assets/magias2versao.json", "app/src/main/assets/magias2versao.json")
        .map { File(it) }.firstOrNull { it.exists() }

    private val catalogo: List<MagiaDefinicao> by lazy {
        val tipo = object : TypeToken<List<MagiaDefinicao>>() {}.type
        Gson().fromJson<List<MagiaDefinicao>>(arquivo!!.readText(Charsets.UTF_8), tipo)
    }

    private fun comoATelaLe(nome: String): MagiaDefinicao =
        CicloDaMagia.comTextoDaTela(catalogo.first { it.nome == nome })

    @Test
    fun `Respirar Ar mostra 3 e 1 e a conta da aba Rolagem cobra 3`() {
        Assume.assumeNotNull(arquivo)
        val bruta = catalogo.first { it.nome == "Respirar Ar" }
        assertEquals("o cabeçalho é o da tabela", "4/2", bruta.energia)
        val tela = comoATelaLe("Respirar Ar")
        assertEquals("3/1", tela.energia)
        val custo = MagiaEnergiaRules.parseCusto(tela.energia)
        assertEquals(3, custo.fixo)
        assertFalse(custo.precisaEscolher)
        assertEquals("com o cabeçalho de antes, cobrava 4", 4, MagiaEnergiaRules.parseCusto(bruta.energia).fixo)
        // o motor da Saga lê o mesmo número
        val saga = MagicEnergy.parse(tela.energia)
        assertEquals(3, saga.base)
        assertEquals(1, saga.manutencao)
    }

    @Test
    fun `Desintegrar nao tem texto de tela - a entrada do livro diz 1 a 4 como a tabela`() {
        // O exemplo de antes (tabela 1 a 4, entrada 1 a 10) era erro do texto antigo da p.120, transcrito por IA:
        // lido o PDF em 19/out/2026, a entrada diz "1 a 4".
        Assume.assumeNotNull(arquivo)
        val bruta = catalogo.first { it.nome == "Desintegrar" }
        assertEquals("1 a 4", bruta.energia)
        assertEquals(null, bruta.energiaTela)
        assertEquals(4, bruta.custoMaximo)
    }

    @Test
    fun `todo texto de tela e lido pelo app como o numero conferido`() {
        Assume.assumeNotNull(arquivo)
        var viu = 0
        catalogo.forEach { bruta ->
            val m = CicloDaMagia.comTextoDaTela(bruta)
            if (bruta.energiaTela != null) {
                viu++
                val c = MagiaEnergiaRules.parseCusto(m.energia)
                val s = MagicEnergy.parse(m.energia)
                if (bruta.custoTipo == "faixa") {
                    assertEquals("${bruta.nome}: piso", bruta.custoMinimo, c.minimo)
                    assertEquals("${bruta.nome}: teto", bruta.custoMaximo, c.maximo)
                    assertEquals("${bruta.nome}: teto (Saga)", bruta.custoMaximo, s.maximo)
                } else {
                    assertEquals("${bruta.nome}: custo de operar", bruta.custoOperar, c.fixo)
                    assertEquals("${bruta.nome}: custo de operar (Saga)", bruta.custoOperar, s.base)
                    // quando o texto escreve a manutenção em número, é o número conferido
                    if (s.manutencao != null) assertEquals("${bruta.nome}: manter", bruta.custoManter, s.manutencao)
                }
            }
            if (bruta.duracaoTela != null) {
                viu++
                val (tipo, seg) = MagicTime.parseDuracao(m.duracao)
                val esperado = CicloDaMagia.duracao(bruta.duracaoTipo, bruta.duracaoSeg, bruta.duracao)
                assertEquals("${bruta.nome}: duração", esperado, tipo to seg)
            }
            if (bruta.tempoOperacaoTela != null) {
                viu++
                assertEquals("${bruta.nome}: tempo de operação", bruta.tempoOperacaoSeg, MagicTime.parseTempoSeg(m.tempoOperacao))
            }
        }
        assertTrue("o catálogo tem texto de tela para ler", viu >= 10)
    }

    @Test
    fun `sem texto de tela o cabecalho passa intacto`() {
        Assume.assumeNotNull(arquivo)
        catalogo.filter { it.energiaTela == null && it.duracaoTela == null && it.tempoOperacaoTela == null }.forEach { bruta ->
            val m = CicloDaMagia.comTextoDaTela(bruta)
            assertEquals(bruta.nome, bruta.energia, m.energia)
            assertEquals(bruta.nome, bruta.duracao, m.duracao)
            assertEquals(bruta.nome, bruta.tempoOperacao, m.tempoOperacao)
        }
    }

    /**
     * A GUARDA: nenhuma magia pode ficar com um custo de operar conferido que o cabeçalho (legível)
     * contradiz e sem texto de tela -- era o buraco: a tela dizia uma coisa e a Saga cobrava outra.
     */
    @Test
    fun `nenhum custo conferido fica contradito pelo cabecalho sem texto de tela`() {
        Assume.assumeNotNull(arquivo)
        catalogo.filter { it.custoTipo == "fixo" && it.custoOperar != null && it.energiaTela == null }.forEach { m ->
            val c = MagiaEnergiaRules.parseCusto(m.energia)
            if (!c.desconhecido && !c.variavel) {
                assertEquals("${m.nome} (p.${m.pagina}): cabeçalho '${m.energia}' × conferido ${m.custoOperar}", m.custoOperar, c.fixo)
            }
        }
        assertNotNull(catalogo.firstOrNull { it.nome == "Desintegrar" })
    }
}
