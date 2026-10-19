package com.gurps.ficha.domain.magic

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.model.MagiaDefinicao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assume
import org.junit.Test
import java.io.File

/**
 * Lote MC-5 — o ciclo de 10 magias (custo, manutenção, tempo, duração) como o JOGO as lê.
 *
 * É daqui que a **pedra de toque** da Mesa extrai casos (`server/scripts/extrair-pedra-de-toque.js`
 * lê só `assertEquals(<literal>, Dono.metodo(<literais>))`): a Mesa tem de dar o MESMO número que
 * este teste, e se um dos dois lados mudar, o outro fica vermelho com o nome da magia.
 *
 * Os números NÃO foram digitados: saíram do `magias2versao.json` pelas mesmas regras de
 * [CicloDaMagia] (script de geração no relatório do lote) — e é este teste, rodando o código do app,
 * que confirma cada um. A conferência contra o LIVRO é de `MagicCatalogNumerosDoLivroTest`.
 *
 * As 10 cobrem: custo fixo com manutenção (Arma Congelante, Lealdade, Passo Leve), "não pode ser
 * mantida" (Alarme, Acelerar), duração "Indefinida" (Animação Suspensa), magia renomeada pelo livro
 * (Aquecer), tempo "Instant" (Tempestade de Faíscas), "Perm." que o livro não confirma (Criar
 * Elemental Água) e permanente de verdade (Cura Superficial).
 */
class CicloDaMagiaTest {

    /** O catálogo lido como o app o lê (Gson em [MagiaDefinicao]); as funções são as do [CicloDaMagia]. */
    private object NoCatalogo {
        private val arquivo = listOf("src/main/assets/magias2versao.json", "app/src/main/assets/magias2versao.json")
            .map { File(it) }.firstOrNull { it.exists() }

        val existe: Boolean get() = arquivo != null

        private val porNome: Map<String, MagiaDefinicao> by lazy {
            val tipo = object : TypeToken<List<MagiaDefinicao>>() {}.type
            val lista: List<MagiaDefinicao> = Gson().fromJson(arquivo!!.readText(Charsets.UTF_8), tipo)
            lista.associateBy { it.nome }
        }

        private fun def(nome: String): MagiaDefinicao = porNome[nome] ?: error("magia '$nome' não existe no catálogo")

        fun custoOperar(nome: String): Int =
            CicloDaMagia.custo(def(nome).custoOperar, def(nome).custoManter, def(nome).energia)?.base
                ?: error("$nome: sem custoOperar conferido")

        fun custoManter(nome: String): Int =
            CicloDaMagia.custo(def(nome).custoOperar, def(nome).custoManter, def(nome).energia)?.manutencao
                ?: error("$nome: sem custoManter conferido")

        fun tempoSeg(nome: String): Int = CicloDaMagia.tempoSeg(def(nome).tempoOperacaoSeg, def(nome).tempoOperacao)

        fun duracaoTipo(nome: String): TipoDuracao =
            CicloDaMagia.duracao(def(nome).duracaoTipo, def(nome).duracaoSeg, def(nome).duracao).first

        fun duracaoSeg(nome: String): Int =
            CicloDaMagia.duracao(def(nome).duracaoTipo, def(nome).duracaoSeg, def(nome).duracao).second
    }

    @Test
    fun `o ciclo das dez magias da pedra de toque`() {
        Assume.assumeTrue(NoCatalogo.existe)
        assertEquals(3, NoCatalogo.custoOperar("Arma Congelante"))
        assertEquals(1, NoCatalogo.custoManter("Arma Congelante"))
        assertEquals(3, NoCatalogo.tempoSeg("Arma Congelante"))
        assertEquals(TipoDuracao.TEMPORARIA, NoCatalogo.duracaoTipo("Arma Congelante"))
        assertEquals(60, NoCatalogo.duracaoSeg("Arma Congelante"))
        assertEquals(2, NoCatalogo.custoOperar("Lealdade"))
        assertEquals(2, NoCatalogo.custoManter("Lealdade"))
        assertEquals(2, NoCatalogo.tempoSeg("Lealdade"))
        assertEquals(TipoDuracao.TEMPORARIA, NoCatalogo.duracaoTipo("Lealdade"))
        assertEquals(3600, NoCatalogo.duracaoSeg("Lealdade"))
        assertEquals(4, NoCatalogo.custoOperar("Passo Leve"))
        assertEquals(1, NoCatalogo.custoManter("Passo Leve"))
        assertEquals(1, NoCatalogo.tempoSeg("Passo Leve"))
        assertEquals(TipoDuracao.TEMPORARIA, NoCatalogo.duracaoTipo("Passo Leve"))
        assertEquals(600, NoCatalogo.duracaoSeg("Passo Leve"))
        assertEquals(1, NoCatalogo.custoOperar("Alarme"))
        assertEquals(0, NoCatalogo.custoManter("Alarme"))
        assertEquals(1, NoCatalogo.tempoSeg("Alarme"))
        assertEquals(TipoDuracao.TEMPORARIA, NoCatalogo.duracaoTipo("Alarme"))
        assertEquals(604800, NoCatalogo.duracaoSeg("Alarme"))
        assertEquals(5, NoCatalogo.custoOperar("Acelerar"))
        assertEquals(0, NoCatalogo.custoManter("Acelerar"))
        assertEquals(3, NoCatalogo.tempoSeg("Acelerar"))
        assertEquals(TipoDuracao.TEMPORARIA, NoCatalogo.duracaoTipo("Acelerar"))
        assertEquals(10, NoCatalogo.duracaoSeg("Acelerar"))
        assertEquals(6, NoCatalogo.custoOperar("Animação Suspensa"))
        assertEquals(30, NoCatalogo.tempoSeg("Animação Suspensa"))
        assertEquals(TipoDuracao.PERMANENTE, NoCatalogo.duracaoTipo("Animação Suspensa"))
        assertEquals(0, NoCatalogo.duracaoSeg("Animação Suspensa"))
        assertEquals(60, NoCatalogo.tempoSeg("Aquecer"))
        assertEquals(TipoDuracao.TEMPORARIA, NoCatalogo.duracaoTipo("Aquecer"))
        assertEquals(3600, NoCatalogo.duracaoSeg("Aquecer"))
        assertEquals(1, NoCatalogo.tempoSeg("Tempestade de Faíscas"))
        assertEquals(TipoDuracao.TEMPORARIA, NoCatalogo.duracaoTipo("Tempestade de Faíscas"))
        assertEquals(60, NoCatalogo.duracaoSeg("Tempestade de Faíscas"))
        assertEquals(1, NoCatalogo.tempoSeg("Criar Elemental Água"))
        assertEquals(TipoDuracao.TEMPORARIA, NoCatalogo.duracaoTipo("Criar Elemental Água"))
        assertEquals(3600, NoCatalogo.duracaoSeg("Criar Elemental Água"))
        assertEquals(1, NoCatalogo.tempoSeg("Cura Superficial"))
        assertEquals(TipoDuracao.PERMANENTE, NoCatalogo.duracaoTipo("Cura Superficial"))
        assertEquals(0, NoCatalogo.duracaoSeg("Cura Superficial"))
    }

    @Test
    fun `sem numero conferido o custo cai no parser do texto e a duracao tambem`() {
        // O contrato do plano B: CicloDaMagia não inventa número. Sem `custoOperar` devolve null (o
        // chamador usa MagicEnergy.parse) e sem `duracaoTipo` devolve o que o parser do texto dá.
        // (Variáveis locais de propósito: a pedra de toque só extrai `assertEquals(literal, Dono.metodo(...))`.)
        val semCusto = CicloDaMagia.custo(null, 2, "Varia")
        assertNull(semCusto)
        val duracao = CicloDaMagia.duracao(null, null, "1 min.")
        assertEquals(MagicTime.parseDuracao("1 min."), duracao)
        val tempo = CicloDaMagia.tempoSeg(null, "2 seg.")
        assertEquals(MagicTime.parseTempoSeg("2 seg."), tempo)
        val instantaneo = CicloDaMagia.tempoSeg(0, "Instant")
        assertEquals(MagicTime.parseTempoSeg("Instant"), instantaneo)
    }

    @Test
    fun `a tela mostra o texto do numero conferido quando ele existe`() {
        // Desintegrar: a tabela diz "1 a 4", a entrada do livro diz "1 a 10" -- a tela mostra o que se cobra.
        assertEquals("1 a 10", CicloDaMagia.textoDaTela("1 a 4", "1 a 10"))
        assertEquals("6/3", CicloDaMagia.textoDaTela("4/1", "6/3"))
        assertEquals("1 hora", CicloDaMagia.textoDaTela("Perm.", "1 hora"))
        // sem texto da tela o cabeçalho vale, do jeito que o livro o escreve
        assertEquals("1 a 3/I", CicloDaMagia.textoDaTela("1 a 3/I", null))
        assertEquals("Varia", CicloDaMagia.textoDaTela("Varia", ""))
        assertNull(CicloDaMagia.textoDaTela(null, null))
    }

    @Test
    fun `o numero conferido manda sobre o texto do cabecalho`() {
        val custo = CicloDaMagia.custo(7, 3, "9/9")
        assertEquals(7, custo?.base)
        assertEquals(3, custo?.manutencao)
        val duracao = CicloDaMagia.duracao("temporaria", 90, "Perm.")
        assertEquals(TipoDuracao.TEMPORARIA to 90, duracao)
        val tempo = CicloDaMagia.tempoSeg(5, "30 min.")
        assertEquals(5, tempo)
    }
}
