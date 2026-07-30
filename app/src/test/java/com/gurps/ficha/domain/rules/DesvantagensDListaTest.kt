package com.gurps.ficha.domain.rules

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.domain.rules.traits.EfeitoDeclarado
import com.gurps.ficha.domain.rules.traits.EfeitoInterpretador
import com.gurps.ficha.domain.rules.traits.MaoFracaRule
import com.gurps.ficha.domain.rules.traits.TraitRuleRegistry
import com.gurps.ficha.model.AtributoBase
import com.gurps.ficha.model.DesvantagemSelecionada
import com.gurps.ficha.model.Dificuldade
import com.gurps.ficha.model.PericiaSelecionada
import com.gurps.ficha.model.Personagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **Lote D-LISTA** — as desvantagens cujo efeito é uma **lista de perícias** ou
 * um **modificador de reação**, agora declaradas no catálogo.
 *
 * ## O que este arquivo guarda
 *
 * O modo de falhar desta família é sempre o mesmo e é **silencioso**: um nome de
 * perícia com uma letra fora do lugar existe no JSON, passa pelo interpretador
 * inteiro sem erro e simplesmente **nunca aplica**. Ninguém percebe até um
 * jogador reclamar que a Timidez não faz nada.
 *
 * Por isso os testes abaixo leem o **catálogo real**, não dado inventado, e
 * conferem **a lista inteira** de cada desvantagem contra a página do livro —
 * não uma perícia de amostra.
 *
 * ⚠️ Duas armadilhas concretas que já morderam neste lote:
 *
 * 1. **Gagueira tem SEIS perícias; Voz Melodiosa, a vantagem oposta, tem SETE.**
 *    O livro não inclui Política do lado da desvantagem. É assimétrico de
 *    propósito, e sem um teste dizendo isso alguém "conserta" mais tarde.
 * 2. **"Criminologia" no livro é `Criminologia/NT` no catálogo.** Sem o sufixo o
 *    efeito existiria e não aconteceria.
 */
class DesvantagensDListaTest {

    private val gson = Gson()

    private data class TracoCru(
        val id: String = "",
        val nome: String = "",
        val max: Int? = null,
        val efeitos: List<EfeitoDeclarado> = emptyList()
    )

    /** Lê do módulo `app/` — é o diretório de trabalho do Gradle nos testes. */
    private fun catalogo(): Map<String, TracoCru> {
        val direto = File("src/main/assets/desvantagens.v2.json")
        val arquivo = if (direto.exists()) direto else File("app/src/main/assets/desvantagens.v2.json")
        assertTrue("asset nao encontrado: ${arquivo.absolutePath}", arquivo.exists())
        val tipo = object : TypeToken<List<TracoCru>>() {}.type
        return gson.fromJson<List<TracoCru>>(arquivo.readText(Charsets.UTF_8), tipo)
            .associateBy { it.id }
    }

    private fun efeitosDe(id: String): List<EfeitoDeclarado> {
        val traco = catalogo()[id]
        assertTrue("$id nao existe em desvantagens.v2.json", traco != null)
        assertTrue("$id esta SEM efeitos declarados", traco!!.efeitos.isNotEmpty())
        return traco.efeitos
    }

    /** As perícias que o efeito atinge de fato, já com o valor aplicado. */
    private fun modificadoresDe(id: String, nivel: Int = 1, custo: Int = 0): Map<String, Int> =
        EfeitoInterpretador.regraDe(id, efeitosDe(id)).getSkillModifiers(
            Personagem(nome = "Teste"),
            DesvantagemSelecionada(definicaoId = id, nome = id, nivel = nivel, custoEscolhido = custo)
        )

    /** Só os alvos de reação, que não passam pelo NH das perícias. */
    private fun reacoesDe(id: String): List<EfeitoDeclarado> =
        efeitosDe(id).filter { it.alvo == ReacaoRules.ALVO_REACAO }

    // ==================================================================
    // 1. Os espelhos de voz
    // ==================================================================

    @Test
    fun `Gagueira penaliza as SEIS pericias que o livro nomeia, e so elas`() {
        // MB p.144, literal: "-2 (...) nos testes de Atuação, Canto, Diplomacia,
        // Lábia, Oratória e Sex Appeal".
        val esperado = setOf("Atuação", "Canto", "Diplomacia", "Lábia", "Oratória", "Sex Appeal")
        val mods = modificadoresDe("gagueira")
        assertEquals(esperado, mods.keys)
        esperado.forEach { assertEquals("faltou -2 em $it", -2, mods[it]) }
    }

    @Test
    fun `⚠️ Gagueira NAO inclui Politica - a Voz Melodiosa inclui`() {
        // A assimetria e do livro, nao descuido: a vantagem oposta lista sete
        // pericias, a desvantagem lista seis. Este teste existe para que a
        // "correcao" nunca aconteca sem alguem ler a pagina antes.
        assertFalse(
            "o livro nao poe Politica do lado da desvantagem",
            modificadoresDe("gagueira").containsKey("Política")
        )
    }

    @Test
    fun `Voz Irritante e Gagueira sao a MESMA lista - o livro diz identicos`() {
        // MB p.162: "os efeitos causados no jogo, em todos os casos, sao
        // identicos aos relacionados a Gagueira".
        assertEquals(modificadoresDe("gagueira"), modificadoresDe("voz_irritante"))
    }

    @Test
    fun `as duas descontam reacao so quando a conversa e necessaria`() {
        listOf("gagueira", "voz_irritante").forEach { id ->
            val reacao = reacoesDe(id).single()
            assertEquals(-2, reacao.valor)
            assertTrue(
                "$id: a reacao tem de ser condicional",
                reacao.ehCondicional && reacao.condicao!!.contains("conversa")
            )
        }
    }

    // ==================================================================
    // 2. As listas grandes
    // ==================================================================

    @Test
    fun `Timidez tem as QUINZE pericias de lidar com o publico`() {
        // MB p.160. A lista e citada uma vez e reaproveitada pelos tres niveis.
        val esperado = setOf(
            "Atuação", "Boemia", "Comércio", "Diplomacia", "Dissimulação", "Intimidação",
            "Lábia", "Liderança", "Manha", "Mendicância", "Oratória", "Pedagogia",
            "Política", "Sex Appeal", "Trato-Social"
        )
        assertEquals(esperado, modificadoresDe("timidez", custo = -5).keys)
    }

    @Test
    fun `Timidez muda com o CUSTO comprado, nao com o nivel`() {
        // MB p.160: Suave -1, Grave -2, Incapacitante -4. Sao tres compras
        // diferentes da mesma desvantagem -- `porNivel` daria -1, -2, -3.
        assertEquals(-1, modificadoresDe("timidez", custo = -5)["Lábia"])
        assertEquals(-2, modificadoresDe("timidez", custo = -10)["Lábia"])
        assertEquals(-4, modificadoresDe("timidez", custo = -20)["Lábia"])
    }

    @Test
    fun `Timidez com custo fora da tabela nao chuta um numero`() {
        // Ficha antiga com custo 0 devolve 0 -- preferir nao aplicar a aplicar
        // errado e a regra do interpretador inteiro.
        assertEquals(0, modificadoresDe("timidez", custo = 0)["Lábia"])
    }

    @Test
    fun `Pouca Empatia tem as DEZESSEIS pericias de motivacao emocional`() {
        // MB p.154.
        val esperado = setOf(
            "Boemia", "Comércio", "Criminologia/NT", "Deslumbrar", "Detecção de Mentiras",
            "Diplomacia", "Dissimulação", "Interrogatório", "Lábia", "Liderança",
            "Manha", "Política", "Psicologia", "Sex Appeal", "Sociologia", "Trato-Social"
        )
        val mods = modificadoresDe("pouca_empatia")
        assertEquals(esperado, mods.keys)
        esperado.forEach { assertEquals("faltou -3 em $it", -3, mods[it]) }
    }

    @Test
    fun `⚠️ Criminologia esta com o sufixo NT que o catalogo usa`() {
        // O livro escreve "Criminologia"; o catalogo, "Criminologia/NT". Sem o
        // sufixo o efeito existe no JSON e nunca casa com a pericia da ficha.
        val mods = modificadoresDe("pouca_empatia")
        assertTrue(mods.containsKey("Criminologia/NT"))
        assertFalse("sem sufixo o bonus fica mudo", mods.containsKey("Criminologia"))
    }

    @Test
    fun `Oblivio penaliza as SEIS pericias de Influenciar, em 1`() {
        // MB p.152: "Diplomacia, Intimidacao, Labia, Manha, Sex Appeal e Trato
        // Social". Sao as seis pericias de Influenciar do MB p.359.
        val esperado = setOf(
            "Diplomacia", "Intimidação", "Lábia", "Manha", "Sex Appeal", "Trato-Social"
        )
        val mods = modificadoresDe("oblivio")
        assertEquals(esperado, mods.keys)
        esperado.forEach { assertEquals(-1, mods[it]) }
    }

    @Test
    fun `Pouca Empatia e mais pesada que Oblivio - e sao desvantagens diferentes`() {
        // As duas se parecem e o livro as declara mutuamente excludentes. Se
        // alguem colar a lista errada, este teste cai.
        assertEquals(-3, modificadoresDe("pouca_empatia")["Lábia"])
        assertEquals(-1, modificadoresDe("oblivio")["Lábia"])
        assertTrue(
            "Pouca Empatia alcanca mais pericias",
            modificadoresDe("pouca_empatia").size > modificadoresDe("oblivio").size
        )
    }

    // ==================================================================
    // 3. Insensível — a desvantagem que também dá BÔNUS
    // ==================================================================

    @Test
    fun `Insensivel desconta 3 em Pedagogia direto, sem condicao`() {
        // MB p.148: o -3 de Pedagogia e seco. So o de Psicologia tem "para
        // ajudar" grudado.
        assertEquals(-3, modificadoresDe("insensivel")["Pedagogia"])
    }

    @Test
    fun `Insensivel da mais 1 em Interrogatorio e Intimidacao com ameaca`() {
        // ⚠️ Bonus dentro de uma DESVANTAGEM, e o livro e explicito: "a crueldade
        // tambem tem suas vantagens". Como sao condicionais, NAO entram no NH
        // base -- viram caixinha na hora da rolagem.
        val condicionais = efeitosDe("insensivel").filter { it.ehCondicional }
        val positivos = condicionais.filter { it.valor > 0 }.map { it.alvo }.toSet()
        assertEquals(setOf("Interrogatório", "Intimidação"), positivos)
        condicionais.filter { it.valor > 0 }.forEach {
            assertTrue(it.condicao!!, it.condicao!!.contains("ameaça") || it.condicao!!.contains("tortura"))
        }
        // E nenhum dos dois pode ter entrado no NH base.
        val base = modificadoresDe("insensivel")
        assertFalse(base.containsKey("Interrogatório"))
        assertFalse(base.containsKey("Intimidação"))
    }

    @Test
    fun `a Psicologia do Insensivel so cai quando o teste e para AJUDAR`() {
        // "nos testes de Psicologia feitos para ajudar outras pessoas (e nao
        // para identificar fraquezas ou realizar pesquisas cientificas)".
        val psico = efeitosDe("insensivel").single { it.alvo == "Psicologia" }
        assertEquals(-3, psico.valor)
        assertTrue(psico.condicao!!, psico.ehCondicional)
        assertFalse(
            "condicional nao pode entrar no NH base",
            modificadoresDe("insensivel").containsKey("Psicologia")
        )
    }

    // ==================================================================
    // 4. As de reação
    // ==================================================================

    @Test
    fun `quem o livro nomeia o publico e CONDICIONAL, quem nao nomeia e fixo`() {
        // O criterio, e a razao dele: somar sempre um modificador que o livro
        // amarra a um publico daria o numero contra quem o traco nem alcanca.
        // Mau Cheiro ("a maioria das pessoas e animais") e fixo; Sadismo ("quem
        // fica sabendo") e condicional.
        listOf("mau_cheiro", "teimosia", "ingenuo").forEach { id ->
            assertTrue("$id deveria ser fixo", reacoesDe(id).none { it.ehCondicional })
        }
        listOf("sadismo", "magnetismo_sobrenatural", "incapaz_de_sentir_prazer").forEach { id ->
            assertTrue("$id deveria ser condicional", reacoesDe(id).all { it.ehCondicional })
        }
    }

    @Test
    fun `Mau Cheiro tira 2 e Teimosia tira 1 da reacao`() {
        assertEquals(-2, reacoesDe("mau_cheiro").single().valor)
        assertEquals(-1, reacoesDe("teimosia").single().valor)
    }

    @Test
    fun `Megalomania tem DOIS publicos opostos, um positivo e um negativo`() {
        // MB p.151: +2 de jovens, ingenuos e Fanaticos em busca de causa; -2 de
        // todas as outras pessoas. Se os dois fossem fixos, somariam zero e a
        // desvantagem sumiria da tela.
        val r = reacoesDe("megalomania")
        assertEquals(2, r.size)
        assertEquals(setOf(2, -2), r.map { it.valor }.toSet())
        assertTrue("os dois precisam ser condicionais", r.all { it.ehCondicional })
    }

    @Test
    fun `No Limite e Viciado em Trabalho tambem tem os dois lados`() {
        listOf("no_limite", "viciado_em_trabalho").forEach { id ->
            val r = reacoesDe(id)
            assertEquals("$id deveria ter dois publicos", 2, r.size)
            assertTrue("$id precisa de um lado positivo", r.any { it.valor > 0 })
            assertTrue("$id precisa de um lado negativo", r.any { it.valor < 0 })
            assertTrue("$id: os dois lados sao condicionais", r.all { it.ehCondicional })
        }
    }

    @Test
    fun `Ingenuo desconta 4 em Trato-Social e 2 de reacao`() {
        // MB p.147. O "+4 para resistir a Sex Appeal" ficou de fora: e defesa
        // contra a pericia de OUTRA pessoa, canal que o app ainda nao tem.
        assertEquals(-4, modificadoresDe("ingenuo")["Trato-Social"])
        assertEquals(-2, reacoesDe("ingenuo").single().valor)
    }

    @Test
    fun `Incapaz de Sentir Prazer atinge as QUATRO pericias de prazer`() {
        // MB p.146: Boemia, Connoisseur, Arte Erotica e Jogos de Azar.
        val mods = modificadoresDe("incapaz_de_sentir_prazer")
        assertEquals(setOf("Boemia", "Connoisseur", "Arte Erótica", "Jogos de Azar"), mods.keys)
        mods.values.forEach { assertEquals(-3, it) }
    }

    @Test
    fun `Maneta tira 4 de Escalada e Luta Greco-Romana, nas duas versoes`() {
        // MB p.149: "-4 nas tarefas que podem ser realizadas com um braco so, mas
        // normalmente exigem ambos (ex.: grande parte dos testes de Escalada e
        // Luta Greco-Romana)". Uma Mao manda "utilizar as regras de Um Braco".
        listOf("maneta_um_braco", "maneta_uma_mao").forEach { id ->
            val mods = modificadoresDe(id)
            assertEquals(setOf("Escalada", "Luta Greco-Romana"), mods.keys)
            mods.values.forEach { assertEquals("$id deveria dar -4", -4, it) }
        }
    }

    // ==================================================================
    // 5. Mão Fraca — a que ficou em Kotlin
    // ==================================================================

    private fun pericia(id: String, nome: String) = PericiaSelecionada(
        definicaoId = id, nome = nome,
        atributoBase = AtributoBase.DX, dificuldade = Dificuldade.MEDIA, pontosGastos = 4
    )

    private fun comMaoFraca(nivel: Int, vararg pericias: PericiaSelecionada) = Personagem(
        nome = "T", destreza = 10, vitalidade = 10,
        desvantagens = listOf(
            DesvantagemSelecionada(definicaoId = MaoFracaRule.ID, nome = "Mão Fraca", nivel = nivel)
        ),
        pericias = pericias.toList()
    )

    @Test
    fun `Mao Fraca NAO tem efeitos no JSON - a regra e Kotlin`() {
        // ⚠️ Se alguem declarar `efeitos` para ela, o JSON sera ignorado em
        // silencio (Kotlin vence) e quem declarou vai achar que automatizou.
        assertTrue(
            "mao_fraca tem de ficar sem `efeitos`",
            catalogo()["mao_fraca"]!!.efeitos.isEmpty()
        )
        assertTrue(TraitRuleRegistry.hasSpecialRule(MaoFracaRule.ID))
    }

    @Test
    fun `Mao Fraca tira 2 por nivel das armas de corpo a corpo`() {
        // MB p.151: "cada nivel (ate no maximo 3) implica numa penalidade de -2".
        val pj = comMaoFraca(2, pericia("espada_curta", "Espada Curta"))
        assertEquals(-4, TraitRuleRegistry.getSkillBonus(pj, "Espada Curta"))
    }

    @Test
    fun `⚠️ Mao Fraca NAO penaliza arma a distancia - o livro diz corpo a corpo`() {
        // O contraste com a Cegueira, que usa a lista UNIAO porque la o livro
        // fala de TODAS as pericias de combate. Usar a uniao aqui penalizaria o
        // arqueiro que o livro nao penaliza.
        val pj = comMaoFraca(1, pericia("arcos", "Arcos"))
        assertEquals(0, TraitRuleRegistry.getSkillBonus(pj, "Arcos"))
    }

    @Test
    fun `Mao Fraca pega Escalada e Acrobacia mesmo sem a pericia na ficha`() {
        // Os dois exemplos nominais do livro ("escalar" e "um teste de Acrobacia
        // para segurar um trapezio"). O NH predefinido tambem e afetado, entao
        // eles entram mesmo com a ficha vazia de pericias.
        val pj = comMaoFraca(1)
        assertEquals(-2, TraitRuleRegistry.getSkillBonus(pj, "Escalada"))
        assertEquals(-2, TraitRuleRegistry.getSkillBonus(pj, "Acrobacia"))
    }

    @Test
    fun `Mao Fraca para de piorar no nivel 3`() {
        // O teto e do livro. Sem ele, uma ficha com nivel 9 daria -18.
        val tres = comMaoFraca(3, pericia("faca", "Faca"))
        val nove = comMaoFraca(9, pericia("faca", "Faca"))
        assertEquals(-6, TraitRuleRegistry.getSkillBonus(tres, "Faca"))
        assertEquals("o teto de 3 niveis e do livro", -6, TraitRuleRegistry.getSkillBonus(nove, "Faca"))
        assertEquals(3, catalogo()["mao_fraca"]!!.max)
    }

    @Test
    fun `sem Mao Fraca nada muda`() {
        val pj = Personagem(
            nome = "T", destreza = 10,
            pericias = listOf(pericia("espada_curta", "Espada Curta"))
        )
        assertEquals(0, TraitRuleRegistry.getSkillBonus(pj, "Espada Curta"))
        assertEquals(0, TraitRuleRegistry.getSkillBonus(pj, "Escalada"))
    }
}
