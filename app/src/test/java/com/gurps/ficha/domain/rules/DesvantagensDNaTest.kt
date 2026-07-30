package com.gurps.ficha.domain.rules

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.domain.rules.traits.EfeitoDeclarado
import com.gurps.ficha.domain.rules.traits.EfeitoInterpretador
import com.gurps.ficha.model.DesvantagemSelecionada
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * **Lote D-NA** — a penalidade que sai do **Número de Autocontrole**.
 *
 * ## O que este arquivo guarda
 *
 * O `porAutocontrole` é a terceira forma de um efeito variar (depois de
 * `porNivel` e `porOpcao`), e é a mais fácil de errar em silêncio: a tabela
 * devolve **0** quando o NA da ficha não está entre as chaves. Uma tabela sem o
 * `15` funciona perfeitamente para três dos quatro NAs e não faz nada para o
 * quarto — sem erro nenhum no log.
 *
 * ⚠️ **E a tabela NÃO é a mesma em todo lugar.** Eu tinha escrito no plano que
 * ela *"se repete literalmente igual"* nos seis clientes. Está errado, e há
 * teste para cada desvio:
 *
 * - **Xenofilia** (MB p.162) usa **+4/+3/+2/+1** — é **bônus**, não penalidade.
 * - **Egoísmo** (p.137) usa −5/−4/−3/−2, um degrau pior — e essa tabela é do
 *   lado do PdM, então nem entrou.
 */
class DesvantagensDNaTest {

    private val gson = Gson()

    private data class TracoCru(
        val id: String = "",
        val efeitos: List<EfeitoDeclarado> = emptyList()
    )

    private fun catalogo(): Map<String, TracoCru> {
        val direto = File("src/main/assets/desvantagens.v2.json")
        val arquivo = if (direto.exists()) direto else File("app/src/main/assets/desvantagens.v2.json")
        val tipo = object : TypeToken<List<TracoCru>>() {}.type
        return gson.fromJson<List<TracoCru>>(arquivo.readText(Charsets.UTF_8), tipo)
            .associateBy { it.id }
    }

    private fun efeitosDe(id: String): List<EfeitoDeclarado> {
        val t = catalogo()[id]
        assertTrue("$id nao existe no catalogo", t != null)
        assertTrue("$id esta sem efeitos", t!!.efeitos.isNotEmpty())
        return t.efeitos
    }

    private fun desv(id: String, na: Int?, nome: String = id) =
        DesvantagemSelecionada(definicaoId = id, nome = nome, autocontrole = na)

    /**
     * Liga o interpretador no catálogo REAL.
     *
     * Em produção quem alimenta o [EfeitoInterpretador] é o `DataRepository`,
     * que exige `Context` do Android e portanto não existe no teste de unidade.
     * Sem esta costura, `getBonusCondicionais` devolveria lista vazia e os
     * testes ponta a ponta passariam **por não achar nada** — o falso verde que
     * o bug do Lote V-1 ensinou a temer.
     */
    @Before
    fun ligarNoCatalogoReal() {
        val porId = catalogo()
        EfeitoInterpretador.buscador = { id, _ -> porId[id]?.efeitos }
    }

    @After
    fun desligar() {
        EfeitoInterpretador.restaurarBuscadorPadrao()
    }

    // ==================================================================
    // 1. O mecanismo
    // ==================================================================

    @Test
    fun `a tabela devolve o valor do NA que a ficha comprou`() {
        val efeito = efeitosDe("covardia").first { it.alvo == ResistenciaRules.ALVO_PANICO }
        assertEquals(-4, efeito.valorPara(desv("covardia", na = 6)))
        assertEquals(-3, efeito.valorPara(desv("covardia", na = 9)))
        assertEquals(-2, efeito.valorPara(desv("covardia", na = 12)))
        assertEquals(-1, efeito.valorPara(desv("covardia", na = 15)))
    }

    @Test
    fun `⚠️ NA baixo e PIOR - e o contrario do resto do GURPS`() {
        // Vale um teste próprio porque é contraintuitivo: em quase todo o
        // sistema, número alto é melhor. No autocontrole, NA 6 significa "quase
        // nunca resiste", e por isso a penalidade é a maior.
        val efeito = efeitosDe("covardia").first { it.alvo == ResistenciaRules.ALVO_PANICO }
        val comNa6 = efeito.valorPara(desv("covardia", na = 6))
        val comNa15 = efeito.valorPara(desv("covardia", na = 15))
        assertTrue("NA 6 tem de ser pior que NA 15", comNa6 < comNa15)
    }

    @Test
    fun `sem NA na ficha a tabela devolve ZERO, nao o pior valor`() {
        // Ficha antiga ou desvantagem cadastrada sem NA. Preferir não aplicar a
        // aplicar errado é a regra do interpretador inteiro — e chutar o −4
        // seria o pior chute possível.
        val efeito = efeitosDe("covardia").first { it.alvo == ResistenciaRules.ALVO_PANICO }
        assertEquals(0, efeito.valorPara(desv("covardia", na = null)))
    }

    @Test
    fun `NA fora dos quatro do GURPS tambem devolve zero`() {
        val efeito = efeitosDe("covardia").first { it.alvo == ResistenciaRules.ALVO_PANICO }
        assertEquals(0, efeito.valorPara(desv("covardia", na = 10)))
    }

    @Test
    fun `porAutocontrole vence porNivel e porOpcao - e o mais especifico`() {
        // Um efeito declara UMA tabela. Se as três estivessem presentes, a ordem
        // de resolução decidiria o número — e o teste trava essa ordem.
        val efeito = EfeitoDeclarado(
            tipo = "pericia", alvo = "reacao", valor = 99, porNivel = true,
            porOpcao = mapOf("-5" to 77),
            porAutocontrole = mapOf("6" to -4, "9" to -3, "12" to -2, "15" to -1)
        )
        val comTudo = DesvantagemSelecionada(
            definicaoId = "x", nome = "x", nivel = 3, custoEscolhido = -5, autocontrole = 9
        )
        assertEquals(-3, efeito.valorPara(comTudo))
    }

    @Test
    fun `vantagem nunca tem NA, entao a tabela nao aplica nela`() {
        val efeito = EfeitoDeclarado(
            tipo = "pericia", alvo = "reacao",
            porAutocontrole = mapOf("6" to -4, "9" to -3, "12" to -2, "15" to -1)
        )
        assertEquals(0, efeito.valorPara(VantagemSelecionada(definicaoId = "v", nome = "v")))
    }

    // ==================================================================
    // 2. Os três clientes
    // ==================================================================

    @Test
    fun `Covardia desconta da Verificacao de Panico, e SO com risco fisico`() {
        val panico = efeitosDe("covardia").first { it.alvo == ResistenciaRules.ALVO_PANICO }
        assertTrue("tem de ser condicional", panico.ehCondicional)
        assertTrue(panico.condicao!!, panico.condicao!!.contains("dano físico"))
    }

    @Test
    fun `Covardia tambem tem o lado da reacao, com a MESMA tabela`() {
        // "soldados, policiais, etc., reagem com a mesma penalidade quando
        // descobrem que um indivíduo é covarde" (MB p.130).
        val efeitos = efeitosDe("covardia")
        assertEquals(2, efeitos.size)
        val reacao = efeitos.first { it.alvo == ReacaoRules.ALVO_REACAO }
        val panico = efeitos.first { it.alvo == ResistenciaRules.ALVO_PANICO }
        assertEquals(panico.porAutocontrole, reacao.porAutocontrole)
        assertTrue("a reacao tambem e condicional", reacao.ehCondicional)
    }

    @Test
    fun `⚠️ Xenofilia e BONUS - a tabela vai para cima`() {
        // MB p.162: "para compensar tudo isso, o personagem recebe um BÔNUS nas
        // Verificações de Pânico quando encontra criaturas estranhas". É a única
        // tabela positiva do lote, e a que prova que a tabela não é uma só.
        val efeito = efeitosDe("xenofilia").single()
        assertEquals(ResistenciaRules.ALVO_PANICO, efeito.alvo)
        assertEquals(4, efeito.valorPara(desv("xenofilia", na = 6)))
        assertEquals(1, efeito.valorPara(desv("xenofilia", na = 15)))
        assertTrue("todos os valores tem de ser positivos",
            efeito.porAutocontrole!!.values.all { it > 0 })
    }

    @Test
    fun `Fobias usa o curinga e e condicional`() {
        // MB p.141: a penalidade que SOBRA quando ele PASSA no autocontrole,
        // "em todos os testes de habilidade, DX e IQ enquanto a causa de seu
        // medo persistir".
        val efeito = efeitosDe("fobias").single()
        assertEquals("*", efeito.alvo)
        assertTrue("o curinga TEM de ser condicional", efeito.ehCondicional)
        assertEquals(-3, efeito.valorPara(desv("fobias", na = 9)))
    }

    // ==================================================================
    // 3. A Verificação de Pânico ponta a ponta
    // ==================================================================

    // A vararg vem PRIMEIRO de proposito: `heroi(desv(...))` sem nome de
    // parametro caia no `iq` e o compilador reclamava.
    private fun heroi(vararg d: DesvantagemSelecionada, iq: Int = 10) =
        Personagem(nome = "T", inteligencia = iq, desvantagens = d.toList())

    private fun panicoDe(p: Personagem) =
        ResistenciaRules.testesDe(p).first { it.rotulo.contains("Pânico") }

    @Test
    fun `a Covardia NAO entra no alvo - ela vira caixinha`() {
        // ⚠️ A invariante do lote. O livro amarra a penalidade a "risco de dano
        // físico", e Verificação de Pânico também acontece diante de horror sem
        // risco nenhum. Somar sempre daria o −3 onde o livro não dá.
        val p = heroi(desv("covardia", na = 9, nome = "Covardia"), iq = 12)
        val panico = panicoDe(p)
        assertEquals("o alvo NAO pode ter mudado", 12, panico.alvo)
        assertEquals(1, panico.condicionais.size)
        assertEquals(-3, panico.condicionais.single().valor)
    }

    @Test
    fun `a caixinha da Covardia diz o nome do traco e a condicao`() {
        val p = heroi(desv("covardia", na = 6, nome = "Covardia"))
        val c = panicoDe(p).condicionais.single()
        assertEquals("Covardia", c.nomeDoTraco)
        assertTrue(c.rotulo, c.rotulo.contains("Covardia"))
        assertTrue(c.rotulo, c.rotulo.contains("dano físico"))
    }

    @Test
    fun `Xenofilia aparece como caixinha POSITIVA no mesmo teste`() {
        val p = heroi(desv("xenofilia", na = 12, nome = "Xenofilia"))
        val c = panicoDe(p).condicionais.single()
        assertEquals(2, c.valor)
        assertTrue("o rotulo tem de mostrar o sinal +", c.rotulo.contains("+2"))
    }

    @Test
    fun `Covardia e Xenofilia na mesma ficha dao DUAS caixinhas separadas`() {
        // Elas se cancelariam se fossem somadas no alvo — e são situações
        // diferentes: uma é risco físico, a outra é encontrar alienígena.
        val p = heroi(
            desv("covardia", na = 9, nome = "Covardia"),
            desv("xenofilia", na = 9, nome = "Xenofilia")
        )
        val panico = panicoDe(p)
        assertEquals(2, panico.condicionais.size)
        assertEquals(setOf(-3, 3), panico.condicionais.map { it.valor }.toSet())
        assertEquals("o alvo continua limpo", 10, panico.alvo)
    }

    @Test
    fun `ficha sem nenhum dos dois nao ganha caixinha nenhuma`() {
        assertTrue(panicoDe(heroi()).condicionais.isEmpty())
    }

    @Test
    fun `⚠️ o alvo PANICO nao vaza para os outros testes de resistencia`() {
        // `panico` é alvo reservado; se algum agregador o tratasse como perícia
        // comum, ele apareceria em "Resistir a Intimidação", que também é
        // Vontade. Este teste é a cerca.
        val p = heroi(desv("covardia", na = 6, nome = "Covardia"))
        val intimidacao = ResistenciaRules.testesDe(p).first { it.rotulo.contains("Intimidação") }
        assertEquals(10, intimidacao.alvo)
        assertTrue(intimidacao.condicionais.isEmpty())
    }

    @Test
    fun `o Egoismo mantem so o menos 3 do lado certo, sem a tabela de NA`() {
        // ⚠️ A tabela de NA do Egoísmo (MB p.137) descreve "PdMs egoístas
        // REAGEM diante de desfeitos": é o modificador de quem TEM a
        // desvantagem reagindo aos outros — rolagem do Mestre para PdM. O painel
        // de Reação do app rola o contrário, quanto os outros gostam de VOCÊ.
        // Declarar lá inverteria a direção.
        val efeitos = efeitosDe("egoismo")
        assertTrue("nenhum efeito do Egoismo usa porAutocontrole",
            efeitos.none { it.ehPorAutocontrole })
        assertEquals(-3, efeitos.single().valor)
    }

    @Test
    fun `Solitario continua SEM efeitos, pelo mesmo motivo`() {
        assertNull(catalogo()["solitario"]?.efeitos?.takeIf { it.isNotEmpty() })
    }

    @Test
    fun `Gastar Compulsivamente nao virou efeito da Compulsao inteira`() {
        // ⚠️ O catálogo tem UMA entrada `compulsao` para as ~15 variantes do
        // livro, e não guarda qual foi escolhida. Declarar −4 em Comércio
        // penalizaria também quem tem Compulsão (Limpeza).
        val compulsao = catalogo()["compulsao"]
        assertTrue("compulsao nao pode ter ganhado efeitos",
            compulsao == null || compulsao.efeitos.isEmpty())
    }

    @Test
    fun `nenhum efeito declarado no lote entra no NH base`() {
        // Todos são condicionais de propósito. Se algum deixasse de ser, ele
        // passaria a valer sempre — que é exatamente o que o livro não manda.
        listOf("covardia", "fobias", "xenofilia").forEach { id ->
            assertFalse(
                "$id tem efeito nao-condicional",
                efeitosDe(id).any { !it.ehCondicional }
            )
            // E nenhum deles pode produzir modificador de perícia base.
            val mods = EfeitoInterpretador.regraDe(id, efeitosDe(id)).getSkillModifiers(
                Personagem(nome = "T"), desv(id, na = 6)
            )
            assertTrue("$id vazou para o NH base: $mods", mods.isEmpty())
        }
    }
}
