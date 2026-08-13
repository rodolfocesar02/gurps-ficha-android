package com.gurps.ficha.domain.rules

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.domain.rules.poderes.RegrasDePoder
import com.gurps.ficha.model.FonteDePoderDefinicao
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.Poder
import com.gurps.ficha.model.PoderDefinicao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **Poderes** — GURPS Poderes. Lotes POD-1, POD-2 e POD-3.
 *
 * ## O que estes testes guardam
 *
 * O catálogo antigo tinha 44 entradas e errava de quatro jeitos ao mesmo tempo:
 * 13 nomes truncados, 4 poderes faltando, uma linha de gabarito virada item, e
 * `modificadorDePoder = 0` com `pagina = 121` nas 44. **Nada acusava** — não
 * havia um único teste tocando neste catálogo.
 *
 * Por isso o peso aqui está na **varredura do asset de verdade**, e não em casos
 * escolhidos a dedo: é o formato que pega o catálogo inteiro escorregando.
 */
class PoderesTest {

    private fun asset(nome: String): File {
        val direto = File("src/main/assets/$nome")
        val f = if (direto.exists()) direto else File("app/src/main/assets/$nome")
        assertTrue("nao encontrei ${f.absolutePath}", f.exists())
        return f
    }

    private val poderes: List<PoderDefinicao> by lazy {
        Gson().fromJson(
            asset("poderes.v1.json").readText(Charsets.UTF_8),
            object : TypeToken<List<PoderDefinicao>>() {}.type
        )
    }

    private val fontes: List<FonteDePoderDefinicao> by lazy {
        Gson().fromJson(
            asset("fontes_de_poder.v1.json").readText(Charsets.UTF_8),
            object : TypeToken<List<FonteDePoderDefinicao>>() {}.type
        )
    }

    // ── O catálogo, varrido inteiro ────────────────────────────────────

    @Test
    fun `os 47 poderes do livro estao no catalogo`() {
        // 🔴 Faltavam quatro: Controle da Matéria, Cósmico, Divino e Magia.
        assertEquals(47, poderes.size)
        listOf("Controle da Matéria", "Cósmico", "Divino", "Magia").forEach { n ->
            assertTrue("'$n' sumiu do catálogo", poderes.any { it.nome == n })
        }
    }

    @Test
    fun `nenhum nome ficou truncado`() {
        // 🔴 Treze nomes tinham perdido o primeiro termo, porque no PDF o nome do
        // verbete é título e saiu cortado: "de Força" era Construtos de Força.
        val esperados = mapOf(
            "Construtos de Força" to "de Força",
            "Controle de Animais" to "Animais",
            "Controle de Plantas" to "Plantas",
            "Alteração Corporal" to "Corporal",
            "Controle Corporal" to "Corporal",
            "Alteração de Probabilidades" to "Probabilidades",
            "Domínio do Tempo" to "Tempo",
            "Energia Cinética" to "Cinética",
            "Projeção Astral" to "Astral",
            "Telepatia Sobre Máquinas" to "Máquinas",
            "Viagem Dimensional" to "Dimensional"
        )
        esperados.forEach { (cheio, cortado) ->
            assertTrue("'$cheio' nao esta no catalogo", poderes.any { it.nome == cheio })
            assertFalse("o nome truncado '$cortado' voltou", poderes.any { it.nome == cortado })
        }
    }

    @Test
    fun `a linha de gabarito nao e um poder`() {
        // 🔴 O item 0 do catálogo antigo era o texto do modelo do livro:
        // nome "poder.", descrição "O que o poder faz, alguns precedentes...".
        assertFalse(poderes.any { it.nome.equals("poder.", ignoreCase = true) })
        assertTrue(poderes.none { it.nome.endsWith(".") })
        assertTrue(poderes.none { it.nome.isBlank() })
    }

    @Test
    fun `todo poder tem foco, e o foco e so dele`() {
        // ⚠️ Foco repetido entre dois poderes significa que um herdou o verbete do
        // outro. Foi assim que, na extração, Divino ficou com o foco de Alteração
        // Corporal e Cura com o de Ar — e nada acusava.
        poderes.forEach {
            assertTrue("'${it.nome}' esta sem foco", it.foco.isNotBlank())
        }
        val repetidos = poderes.groupBy { it.foco }.filter { it.value.size > 1 }
        assertTrue(
            "focos repetidos: " + repetidos.map { (f, ps) -> "$f -> ${ps.map { it.nome }}" },
            repetidos.isEmpty()
        )
    }

    @Test
    fun `todo poder aceita ao menos uma fonte, e so fontes que existem`() {
        // 🔴 No catálogo antigo os 44 tinham `modificadorDePoder = 0` — o campo
        // que o app usa para calcular custo estava zerado no catálogo inteiro.
        val validas = fontes.map { it.nome }.toSet()
        poderes.forEach { p ->
            assertTrue("'${p.nome}' nao aceita fonte nenhuma", p.modificadores.isNotEmpty())
            p.modificadores.forEach { m ->
                assertTrue("'${p.nome}' cita a fonte inexistente '${m.fonte}'", m.fonte in validas)
            }
        }
    }

    @Test
    fun `o valor de cada fonte bate com o do livro, em todos os poderes`() {
        // A varredura que importa: a MESMA fonte tem de valer o MESMO tanto nos
        // 47 verbetes. Espiritual é -25% em todo lugar, ou está errado em algum.
        poderes.forEach { p ->
            p.modificadores.forEach { m ->
                assertEquals(
                    "'${p.nome}': a fonte ${m.fonte} esta com valor fora do livro",
                    RegrasDePoder.VALOR_DA_FONTE[m.fonte],
                    m.valor
                )
            }
        }
    }

    @Test
    fun `a pagina nao e a mesma para todo mundo`() {
        // 🔴 As 44 entradas antigas tinham `pagina = 121`, todas.
        val paginas = poderes.map { it.pagina }.toSet()
        assertTrue("todo poder aponta para a mesma pagina: $paginas", paginas.size > 5)
        poderes.forEach {
            assertTrue("'${it.nome}' com pagina fora do capitulo: ${it.pagina}",
                it.pagina in 121..136)
        }
    }

    @Test
    fun `a descricao e do proprio verbete, nao do vizinho`() {
        // 🔴 Achado pelo usuário NA TELA: a descrição da Água emendava
        // "Habilidades de Alteração Corporal Adaptação ao Terreno…" — texto do
        // verbete SEGUINTE. O corte da extração não pegava e a descrição caía no
        // teto de 700 caracteres, levando junto o começo do vizinho.
        val marcasDeOutroVerbete = listOf(
            // ⚠️ Exige LETRA MAIÚSCULA depois: "Habilidades de Alteração Corporal"
            // é verbete vizinho, mas "Habilidades de cura permitem…" é o texto do
            // próprio livro na entrada Cura -- falso positivo meu.
            //
            // 🔴 Esta linha nasceu quebrada: o `` que eu tinha posto no fim virou
            // um BACKSPACE literal (0x08) ao passar por escape de shell, e o
            // marcador não casava com nada. O teste parecia verde por estar CEGO.
            Regex("""Habilidades\s+d[eoa]\s+[A-ZÁÉÍÓÚÂÊÔÃÕÇ]"""),
            Regex("""Cada registro inclui"""),
            Regex("""Todos estes poderes"""),
            Regex("""Criação de Poderes \d+"""),
            Regex("""\d+\s*pontos/nível""")
        )
        poderes.forEach { p ->
            marcasDeOutroVerbete.forEach { marca ->
                assertFalse(
                    "'${p.nome}' tem texto de outro verbete na descrição: ${p.descricao.take(160)}",
                    marca.containsMatchIn(p.descricao)
                )
            }
            assertTrue("'${p.nome}' ficou sem descrição", p.descricao.length > 60)
        }
    }

    @Test
    fun `o Cosmico e a excecao que o livro cobra caro`() {
        // ⚠️ 🔴 Eu tinha INVENTADO o foco do Cósmico ("A criação e as leis da
        // realidade") e o da Magia ("Mana."). O livro diz outra coisa, e o
        // Talento do Cósmico custa o TRIPLO do padrão (p.127).
        val cosmico = poderes.first { it.nome == "Cósmico" }
        assertEquals("Tudo!", cosmico.foco)
        assertEquals(15, cosmico.custoTalentoPorNivel)
        assertEquals(50, cosmico.modificadores.single().valor)

        val magia = poderes.first { it.nome == "Magia" }
        assertTrue(magia.foco, magia.foco.contains("mágicas"))
        assertFalse("o foco inventado voltou", magia.foco.contains("Mana"))
    }

    // ── As onze fontes genéricas (p.26-30) ─────────────────────────────

    @Test
    fun `as onze fontes do livro, com o valor fechado`() {
        assertEquals(11, fontes.size)
        assertEquals(-25, RegrasDePoder.valorDaFonte("Espiritual"))
        assertEquals(-20, RegrasDePoder.valorDaFonte("Moral"))
        assertEquals(-20, RegrasDePoder.valorDaFonte("Natureza"))
        assertEquals(+50, RegrasDePoder.valorDaFonte("Cósmico"))
        listOf("Biológico", "Chi", "Divino", "Elemental", "Mágico", "Psíquico", "Super")
            .forEach { assertEquals("$it deveria ser -10%", -10, RegrasDePoder.valorDaFonte(it)) }
    }

    @Test
    fun `o asset e a regra nao podem discordar`() {
        // Duas rotas para o mesmo número: o JSON e a constante em Kotlin. O
        // defeito moraria na diferença, então o teste cobra que sejam iguais.
        fontes.forEach {
            assertEquals("a fonte ${it.nome} discorda entre asset e regra",
                RegrasDePoder.VALOR_DA_FONTE[it.nome], it.valor)
        }
        assertEquals(RegrasDePoder.VALOR_DA_FONTE.keys, fontes.map { it.nome }.toSet())
    }

    @Test
    fun `o feminino da linha Fontes e a mesma fonte do modificador`() {
        // O livro escreve "Divina" em "Fontes:" e "Divino (-10%)" no modificador.
        assertEquals(-10, RegrasDePoder.valorDaFonte("Divina"))
        assertEquals(-10, RegrasDePoder.valorDaFonte("Psiquismo"))
        assertEquals(RegrasDePoder.valorDaFonte("Mágico"), RegrasDePoder.valorDaFonte("Mágica"))
        assertNull("uma fonte inventada nao pode ter valor", RegrasDePoder.valorDaFonte("Banana"))
        assertNull(RegrasDePoder.valorDaFonte(null))
        assertNull(RegrasDePoder.valorDaFonte("  "))
    }

    // ── POD-2: a fonte manda no percentual ─────────────────────────────

    @Test
    fun `cada poder aceita so o subconjunto de fontes do livro`() {
        // ⚠️ Não é "escolha qualquer uma das onze". Água aceita cinco; Antipsi,
        // três. É o que transforma o campo de digitar num campo de escolher.
        val agua = poderes.first { it.nome == "Água" }
        assertEquals(
            setOf("Divino", "Elemental", "Espiritual", "Mágico", "Super"),
            agua.modificadores.map { it.fonte }.toSet()
        )
        assertEquals(-25, agua.valorDaFonte("Espiritual"))
        assertEquals(-10, agua.valorDaFonte("Elemental"))
        // A média do catálogo: escolher entre poucas, não entre todas.
        val media = poderes.sumOf { it.modificadores.size }.toDouble() / poderes.size
        assertTrue("media de fontes por poder fora do esperado: $media", media < 6.0)
    }

    @Test
    fun `o poder ja nasce com a fonte e o percentual do livro`() {
        // 🔴 Antes todo poder nascia com 0%, e o jogador tinha de saber o número
        // de cabeça para o poder valer alguma coisa no custo.
        poderes.forEach {
            val padrao = it.fontePadrao
            assertNotNull("'${it.nome}' nao tem fonte padrao", padrao)
            assertTrue("'${it.nome}' nasceria com 0%", padrao!!.valor != 0)
        }
    }

    @Test
    fun `o teto de menos oitenta por cento e um numero so`() {
        // O MB p.102 e o Poderes p.28 dizem a mesma coisa. Se alguém mexer num,
        // o outro tem de acompanhar — por isso a constante é única.
        assertEquals(-80, RegrasDePoder.PIOR_MODIFICADOR_TOTAL)
        assertEquals(-80, RegrasDePoder.limitarModificadorTotal(-85))
        assertEquals(-80, RegrasDePoder.limitarModificadorTotal(-80))
        assertEquals(-60, RegrasDePoder.limitarModificadorTotal(-60))
        assertEquals(+50, RegrasDePoder.limitarModificadorTotal(+50))
        assertTrue(RegrasDePoder.oTetoCortou(-85))
        assertFalse(RegrasDePoder.oTetoCortou(-80))
    }

    // ── POD-3: o Talento ───────────────────────────────────────────────

    @Test
    fun `o Talento custa cinco por nivel, ou dez se for amplo`() {
        assertEquals(5, RegrasDePoder.CUSTO_PADRAO_POR_NIVEL)
        assertEquals(10, RegrasDePoder.CUSTO_AMPLO_POR_NIVEL)
        assertEquals(15, RegrasDePoder.custoDoTalento(3))
        assertEquals(30, RegrasDePoder.custoDoTalento(3, RegrasDePoder.CUSTO_AMPLO_POR_NIVEL))
        assertEquals("nivel zero nao custa", 0, RegrasDePoder.custoDoTalento(0))
        assertEquals("nivel negativo nao devolve credito", 0, RegrasDePoder.custoDoTalento(-2))
    }

    @Test
    fun `o teto de quatro niveis avisa, mas nao trava`() {
        // "Não é possível comprar mais do que quatro níveis (…) sem a permissão
        // do Mestre" (p.8). Quem decide é a mesa — o app avisa e deixa passar.
        assertNull(RegrasDePoder.avisoDoTeto(4))
        assertNotNull(RegrasDePoder.avisoDoTeto(5))
        assertEquals("o teto travou em vez de avisar", 25, RegrasDePoder.custoDoTalento(5))
        val aviso = RegrasDePoder.avisoDoTeto(6)!!
        assertTrue(aviso, aviso.contains("Mestre"))
        assertFalse("a fala tem sinal cru", RotuloAcessivel.temSinalCru(aviso))
    }

    @Test
    fun `o Talento entra nos pontos gastos`() {
        // 🔴 `custoTotalTalento` existia no modelo e NAO era chamado em lugar
        // nenhum do projeto: quem comprava Talento nao pagava por ele.
        val sem = Personagem(pontosIniciais = 150)
        val com = sem.copy(
            poderes = listOf(Poder(nome = "Telepatia", fonte = "Psíquico", nivelTalento = 3))
        )
        assertEquals(0, sem.pontosPoderes)
        assertEquals(15, com.pontosPoderes)
        assertEquals(sem.pontosGastos + 15, com.pontosGastos)
        assertEquals(sem.pontosRestantes - 15, com.pontosRestantes)
    }

    @Test
    fun `o custo por nivel do poder e respeitado no total`() {
        val amplo = Personagem(
            poderes = listOf(
                Poder(nome = "Magia", nivelTalento = 2, custoTalentoNivel = 10)
            )
        )
        assertEquals(20, amplo.pontosPoderes)
    }

    @Test
    fun `poder sem Talento nao mexe em nada`() {
        // Regressão: quem só tem o modificador de poder, e nenhum nível de
        // Talento, não pode ver o Restantes mudar.
        val base = Personagem(pontosIniciais = 150, forca = 12)
        val comPoder = base.copy(
            poderes = listOf(Poder(nome = "PES", fonte = "Psíquico", modificadorDePoder = -10))
        )
        assertEquals(base.pontosGastos, comPoder.pontosGastos)
        assertEquals(base.pontosRestantes, comPoder.pontosRestantes)
    }

    // ── A fiação: a tela pergunta o que a regra sabe responder ─────────

    private fun fonte(caminho: String): String {
        val direto = File("src/main/java/$caminho")
        val f = if (direto.exists()) direto else File("app/src/main/java/$caminho")
        assertTrue("nao encontrei ${f.absolutePath}", f.exists())
        return f.readText(Charsets.UTF_8)
    }

    @Test
    fun `o dialogo tem campo de Talento`() {
        // 🔴 `nivelTalento` estava no modelo, no estado do diálogo e no salvar —
        // e NAO era desenhado. Nunca saia de zero.
        val d = fonte("com/gurps/ficha/ui/features/traits/DialogsPoderes.kt")
        assertTrue("o campo de Talento nao existe", d.contains("label = \"Talento (nível)\""))
        assertTrue("o custo do Talento nao aparece", d.contains("RegrasDePoder.custoDoTalento("))
        assertTrue("o aviso do teto nao aparece", d.contains("RegrasDePoder.avisoDoTeto("))
    }

    @Test
    fun `escolher a fonte preenche o percentual`() {
        // ⚠️ Era este o elo que não existia: o app guardava fonte e percentual
        // como dois campos soltos, e nada ligava um ao outro.
        val d = fonte("com/gurps/ficha/ui/features/traits/DialogsPoderes.kt")
        val i = d.indexOf("onEscolher = { escolhida ->")
        assertTrue("a escolha de fonte sumiu do dialogo", i > 0)
        val corpo = d.substring(i, i + 320)
        assertTrue("a fonte nao grava o percentual: $corpo",
            corpo.contains("modificador = escolhida.valor.toString()"))
    }

    @Test
    fun `o custo do Talento chega ao total da ficha`() {
        val p = fonte("com/gurps/ficha/model/Personagem.kt")
        assertTrue("pontosPoderes nao existe", p.contains("val pontosPoderes"))
        val i = p.indexOf("val pontosGastos")
        assertTrue(i > 0)
        assertTrue("pontosPoderes nao entra na soma",
            p.substring(i, i + 420).contains("pontosPoderes"))
    }

    @Test
    fun `o teto de menos oitenta continua sendo aplicado no custo`() {
        // O teto ja existia em CharacterRules citando o MB p.102. O que mudou foi
        // passar a usar a constante unica — se alguem devolver o -80 cru, o
        // numero volta a poder divergir entre os dois livros.
        val c = fonte("com/gurps/ficha/domain/rules/CharacterRules.kt")
        assertFalse("voltou o -80 cravado", c.contains("coerceAtLeast(-80)"))
        assertEquals(
            "os quatro sitios de soma de modificador precisam do teto",
            4,
            Regex("coerceAtLeast\\(RegrasDePoder\\.PIOR_MODIFICADOR_TOTAL\\)").findAll(c).count()
        )
    }
}
