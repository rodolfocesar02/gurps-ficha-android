package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.poderes.HabilidadesAlternativas
import com.gurps.ficha.domain.rules.poderes.MontadorDeModificador
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **As correções depois de ler o que eu tinha pulado** — Lotes POD-14, 16, 17, 18.
 *
 * ## Por que nenhum teste anterior pegou isto
 *
 * Os erros não eram de **número** — eram de **estrutura**. Os testes guardavam o
 * que eu tinha escrito, e o que eu tinha escrito vinha de ler os trechos que
 * respondiam à pergunta que eu já tinha, nunca a seção inteira.
 *
 * Quem achou foi o usuário, seguindo uma remissão que estava na primeira página
 * que eu li.
 */
class CorrecaoDosPoderesTest {

    private fun fonte(caminho: String): String {
        val direto = File("src/main/java/$caminho")
        val f = if (direto.exists()) direto else File("app/src/main/java/$caminho")
        assertTrue("nao encontrei ${f.absolutePath}", f.exists())
        return f.readText(Charsets.UTF_8)
    }

    // ══ POD-16 — o montador contra a Referência Rápida (p.25) ══════════

    @Test
    fun `contramedidas SOMAM, nao sao escolha unica`() {
        // 🔴 A 1ª versão fez escolha única. A Referência Rápida não marca
        // "(escolha um)" neste grupo, e o cabeçalho diz: "a menos que seja
        // indicado o contrário, todos os modificadores serão CUMULATIVOS".
        assertFalse(MontadorDeModificador.Grupo.CONTRAMEDIDAS.escolhaUnica)
        val todas = MontadorDeModificador.CATALOGO
            .filter { it.grupo == MontadorDeModificador.Grupo.CONTRAMEDIDAS }
        assertEquals("as tres contramedidas do livro", 3, todas.size)
        // -10% + -5% + -5% = -20%, e nenhuma conflita com a outra.
        assertEquals(-20, MontadorDeModificador.total(todas))
        assertTrue(MontadorDeModificador.conflitosDeGrupo(todas).isEmpty())
    }

    @Test
    fun `energias canalizadas sao escolha unica, e incluem Voluvel`() {
        assertTrue(MontadorDeModificador.Grupo.ENERGIAS_CANALIZADAS.escolhaUnica)
        val g = MontadorDeModificador.CATALOGO
            .filter { it.grupo == MontadorDeModificador.Grupo.ENERGIAS_CANALIZADAS }
        // 🔴 Volúvel −20% faltava por inteiro na 1ª versão.
        assertTrue("falta Volúvel -20%", g.any { it.rotulo == "Volúvel" && it.valor == -20 })
        // E o Cósmico está AQUI, não em Contramedidas.
        assertTrue("o Cósmico saiu do grupo certo",
            g.any { it.rotulo.contains("Cósmico") && it.valor == 50 })
        assertEquals(listOf(MontadorDeModificador.Grupo.ENERGIAS_CANALIZADAS),
            MontadorDeModificador.conflitosDeGrupo(g.take(2)))
    }

    @Test
    fun `o Cosmico nao convive com contramedidas`() {
        // "Poderes cósmicos não podem ser bloqueados nem possuem contramedidas."
        val cosmico = MontadorDeModificador.CATALOGO.first { it.rotulo.contains("Cósmico") }
        val mundanas = MontadorDeModificador.CATALOGO
            .first { it.grupo == MontadorDeModificador.Grupo.CONTRAMEDIDAS && it.valor == -10 }
        assertTrue(MontadorDeModificador.cosmicoComContramedidas(listOf(cosmico, mundanas)))
        assertFalse(MontadorDeModificador.cosmicoComContramedidas(listOf(cosmico)))
        assertFalse(MontadorDeModificador.cosmicoComContramedidas(listOf(mundanas)))
    }

    @Test
    fun `a desvantagem exigida e valor livre, nao uma lista`() {
        // "Código de conduta: VALOR EM PONTOS das desvantagens exigidas,
        // expresso como percentual." Um Voto de −10 pontos vale −10%.
        assertEquals(-10, MontadorDeModificador.componenteDaDesvantagem(10).valor)
        assertEquals(-15, MontadorDeModificador.componenteDaDesvantagem(-15).valor)
        assertEquals(-7, MontadorDeModificador.componenteDaDesvantagem(7).valor)
        // 🔴 E não existe mais lista fixa de -5/-10/-15 no catálogo.
        val naLista = MontadorDeModificador.CATALOGO
            .count { it.grupo == MontadorDeModificador.Grupo.DESVANTAGEM_TRACO }
        assertEquals("o traço voltou a ser lista fechada", 0, naLista)
    }

    @Test
    fun `a linha inventada de Antipoderes sumiu`() {
        // 🔴 Eu tinha criado "Antipoderes −10% (as duas situações)" para tapar
        // um buraco que eu mesmo criei ao proibir as duas de coexistirem. Com
        // contramedidas cumulativas, −5% + −5% já dá os −10%.
        assertFalse(
            "a linha inventada voltou",
            MontadorDeModificador.CATALOGO.any { it.rotulo.contains("as duas situações") }
        )
    }

    // ══ POD-17 — o modificador NÃO vai na desvantagem ══════════════════

    @Test
    fun `nenhuma rota aplica o modificador de poder a desvantagem`() {
        // "Ele aplica-se a todas as habilidades do poder (mas NÃO ao seu Talento,
        // desvantagens exigidas, ou Antecedente Incomum)." (p.28)
        val vm = fonte("com/gurps/ficha/viewmodel/FichaViewModel.kt")
        listOf(
            "fun vincularDesvantagemPoder(",
            "fun atualizarDesvantagem("
        ).forEach { assinatura ->
            val i = vm.indexOf(assinatura)
            assertTrue("$assinatura sumiu", i > 0)
            val corpo = vm.substring(i, i + 700)
            assertFalse(
                "$assinatura voltou a injetar o modificador do poder",
                Regex("""comModificadorDoPoder\([^,]+,\s*poder\)""").containsMatchIn(corpo)
            )
        }
    }

    // ══ POD-18 — Várias Cópias da Mesma Vantagem (p.12) ════════════════

    @Test
    fun `a mesma vantagem em poderes diferentes tambem paga um quinto`() {
        // Mesma conta das alternativas: cheio na mais cara, 1/5 nas outras.
        assertEquals(36 + 4, HabilidadesAlternativas.custoDasCopias(listOf(36, 18)))
    }

    @Test
    fun `so conta como copia se estiver em poderes DIFERENTES`() {
        // ⚠️ Duas cópias no MESMO poder não são "várias cópias" — seriam
        // alternativas, ou simplesmente a mesma coisa comprada duas vezes.
        val mesmoPoder = listOf(
            Triple("Cura", 30, "p1"), Triple("Cura", 30, "p1")
        )
        assertTrue(HabilidadesAlternativas.agruparCopias(mesmoPoder).isEmpty())

        val doisPoderes = listOf(
            Triple("Cura", 30, "p1"), Triple("Cura", 20, "p2"), Triple("Voo", 40, "p1")
        )
        val g = HabilidadesAlternativas.agruparCopias(doisPoderes)
        assertEquals(setOf("cura"), g.keys)
        assertEquals(listOf(30, 20), g["cura"])
    }

    @Test
    fun `copia solta, sem poder, nao entra`() {
        val soltas = listOf(Triple("Cura", 30, null), Triple("Cura", 30, null))
        assertTrue(HabilidadesAlternativas.agruparCopias(soltas).isEmpty())
    }

    @Test
    fun `o resumo das copias avisa que elas NAO sao alternativas`() {
        // 🔴 A diferença que mais importa: alternativas são mutuamente
        // exclusivas; cópias funcionam ao mesmo tempo. Quem confundir vai achar
        // que o desconto veio de graça.
        val r = HabilidadesAlternativas.resumoDasCopias("Cura", listOf(30, 20))
        assertTrue(r, r.contains("ao mesmo tempo"))
        assertTrue(r, r.contains("não são alternativas"))
        assertFalse("a fala tem sinal cru", RotuloAcessivel.temSinalCru(r))
    }

    // ══ POD-14 — a habilidade é comprada PARA o poder ══════════════════

    @Test
    fun `da para comprar a habilidade de dentro do poder`() {
        // 🔴 O fluxo estava invertido: era preciso criar a vantagem em Traços e
        // voltar ao poder para ligar. "O personagem pode usar seus pontos
        // adquiridos para comprar NOVAS HABILIDADES de Telepatia" (MB p.257).
        val vm = fonte("com/gurps/ficha/viewmodel/FichaViewModel.kt")
        val i = vm.indexOf("fun adicionarHabilidadeAoPoder(")
        assertTrue("nao da para comprar a habilidade pelo poder", i > 0)
        val corpo = vm.substring(i, i + 520)
        assertTrue("a vantagem nao nasce ligada ao poder", corpo.contains("poderId = poder.id"))
        assertTrue("a vantagem nao nasce com o modificador",
            corpo.contains("comModificadorDoPoder(vantagem.modificadores, poder)"))

        val p = fonte("com/gurps/ficha/ui/features/traits/PoderHabilidades.kt")
        assertTrue("o botao de comprar nao existe", p.contains("Adicionar habilidade"))
        assertTrue("o caminho antigo sumiu", p.contains("Ligar uma que já tenho"))
        val d = fonte("com/gurps/ficha/ui/features/traits/DialogsPoderes.kt")
        assertTrue("o catalogo de vantagens nao foi reusado",
            d.contains("SelecionarVantagemDialog("))
        assertTrue("a compra nao chama o viewmodel",
            d.contains("adicionarHabilidadeAoPoder("))
    }

    @Test
    fun `a habilidade continua sendo uma vantagem DO PERSONAGEM`() {
        // ⚠️ Diferente do ModeloRacial, que POSSUI os traços dele. Aqui o poder
        // não possui: a habilidade entra na lista de vantagens da ficha e
        // continua aparecendo na aba Traços. Muda o caminho de compra, não a
        // propriedade — copiar a Raça inteira faria a habilidade sumir de lá.
        val vm = fonte("com/gurps/ficha/viewmodel/FichaViewModel.kt")
        val i = vm.indexOf("fun adicionarHabilidadeAoPoder(")
        val corpo = vm.substring(i, i + 520)
        assertTrue(
            "a habilidade nao entrou na lista de vantagens do personagem",
            corpo.contains("personagem.vantagens + comVinculo")
        )
    }
}
