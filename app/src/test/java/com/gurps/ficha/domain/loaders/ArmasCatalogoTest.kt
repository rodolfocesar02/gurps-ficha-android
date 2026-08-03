package com.gurps.ficha.domain.loaders

import com.gurps.ficha.model.ArmaCatalogoItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **Lote ARMA-1** — o catálogo de armas lido inteiro.
 *
 * ## Por que este teste roda sobre o asset REAL
 *
 * Os três furos que este lote fecha não apareceriam num JSON de mentira montado
 * por mim: eu montaria o exemplo do jeito que o código já lê. Eles só aparecem
 * varrendo as **150 armas de verdade**:
 *
 * 1. `Prec "6+1"` — 12 armas de fogo perdiam o `+N` da mira acoplada.
 * 2. `"GeB+1 corte/GdP+1 perf"` — 28 armas corpo a corpo perdiam o 2º ataque.
 * 3. `"2.900".toIntOrNull() == null` — 57 de 124 alcances viravam "desconhecido".
 *
 * É a mesma lição da trava de pares: teste que só olha o caso que eu imaginei
 * fica verde com o defeito em pé.
 */
class ArmasCatalogoTest {

    private fun asset(nome: String): String {
        val direto = File("src/main/assets/$nome")
        val f = if (direto.exists()) direto else File("app/src/main/assets/$nome")
        assertTrue("nao encontrei ${f.absolutePath}", f.exists())
        return f.readText(Charsets.UTF_8)
    }

    private fun fogo() = ArmasCatalogLoader.distancia(
        asset(ArmasCatalogLoader.ARQUIVO_FOGO), ArmasCatalogLoader.TIPO_FOGO
    )

    private fun distancia() = ArmasCatalogLoader.distancia(
        asset(ArmasCatalogLoader.ARQUIVO_DISTANCIA), ArmasCatalogLoader.TIPO_DISTANCIA
    )

    private fun corpoACorpo() = ArmasCatalogLoader.corpoACorpo(asset(ArmasCatalogLoader.ARQUIVO_CORPO_A_CORPO))

    private fun todas() = fogo() + distancia() + corpoACorpo()

    private fun porNome(lista: List<ArmaCatalogoItem>, nome: String): ArmaCatalogoItem {
        val achada = lista.firstOrNull { it.nome == nome }
        assertNotNull("nao achei '$nome' no catalogo", achada)
        return achada!!
    }

    // ==================================================================
    // 0. O catálogo inteiro chegou
    // ==================================================================

    @Test
    fun `as 150 armas do catalogo chegam ao modelo`() {
        assertEquals("armas de fogo", 62, fogo().size)
        assertEquals("armas a distancia", 28, distancia().size)
        assertEquals("corpo a corpo", 60, corpoACorpo().size)
    }

    @Test
    fun `toda arma tem pelo menos um modo de ataque`() {
        // Lista vazia aqui significaria uma arma sem nenhum jeito de atacar —
        // e a tela do detalhe não teria o que mostrar.
        todas().forEach { arma ->
            assertTrue("${arma.nome} ficou sem modo", arma.modos.isNotEmpty())
        }
    }

    @Test
    fun `o dano da ficha continua sendo o do PRIMEIRO modo`() {
        // ⚠️ Regra de ouro da migração: `armaDanoRaw` é o que está gravado nas
        // fichas salvas. Se o 1º modo deixasse de ser o dano principal, toda
        // Katana já criada mudaria de dano sozinha ao abrir a ficha.
        todas().forEach { arma ->
            assertEquals(
                "${arma.nome}: o danoRaw saiu do 1o modo",
                arma.modos.first().danoRaw, arma.danoRaw
            )
        }
    }

    // ==================================================================
    // 1. 🔴 A mira acoplada — o "+N" da Precisão
    // ==================================================================

    @Test
    fun `🔴 as 12 armas com mira acoplada trazem o bonus separado`() {
        val comMira = todas().filter { it.temMiraAcoplada }
        assertEquals("mudou a conta de armas com mira", 12, comMira.size)
        // Todas são de fogo — arco e besta não têm luneta de fábrica na tabela.
        assertTrue(
            "mira acoplada apareceu fora das armas de fogo",
            comMira.all { it.tipoCombate == ArmasCatalogLoader.TIPO_FOGO }
        )
    }

    @Test
    fun `🔴 o Rifle de Atirador nao perde mais os 3 pontos`() {
        // O caso mais caro: Prec "6+3". O app dava 6 e o livro dá 9 com a mira.
        val rifle = porNome(fogo(), "Rifle de Atirador, .338")
        assertEquals(6, rifle.precisao)
        assertEquals(3, rifle.precisaoAcessorio)
        assertEquals(9, rifle.precisaoComAcessorio)
    }

    @Test
    fun `arma sem mira tem acessorio NULO, nao zero`() {
        // "não tem mira" e "a mira dá +0" são coisas diferentes: a primeira
        // esconde a caixinha, a segunda mostraria uma caixinha inútil.
        val pederneira = porNome(fogo(), "Pistola de Pederneira, .51")
        assertEquals(1, pederneira.precisao)
        assertNull(pederneira.precisaoAcessorio)
        assertTrue(!pederneira.temMiraAcoplada)
        assertEquals(1, pederneira.precisaoComAcessorio)
    }

    @Test
    fun `a Precisao base NUNCA muda por causa do acessorio`() {
        // O acessório é uma parcela à parte. Se ele vazasse para `precisao`, o
        // Apontar sem mira passaria a dar bônus que o jogador não tem.
        todas().forEach { arma ->
            val base = arma.precisao ?: return@forEach
            assertTrue("${arma.nome}: precisao base negativa", base >= 0)
            assertEquals(
                "${arma.nome}: o acessorio vazou para a base",
                base + (arma.precisaoAcessorio ?: 0), arma.precisaoComAcessorio
            )
        }
    }

    @Test
    fun `o leitor de Precisao entende os formatos da tabela`() {
        // Direto na função, para o formato estranho não virar número inventado.
        assertEquals(null to null, ArmasCatalogLoader.precisaoDe(null))
    }

    // ==================================================================
    // 2. 🔴 O segundo modo de ataque
    // ==================================================================

    @Test
    fun `🔴 as 29 armas corpo a corpo com dois ataques trazem os dois`() {
        // 28 vêm com a barra separadora no catálogo. A 29ª é a linha em que a
        // barra sumiu na digitação e o desgrude recupera — contar 28 aqui seria
        // dar por perdido justamente o caso mais escondido.
        val varios = corpoACorpo().filter { it.modos.size > 1 }
        assertEquals("mudou a conta de armas com mais de um modo", 29, varios.size)
    }

    @Test
    fun `🔴 a Katana corta E estoca`() {
        val katana = porNome(corpoACorpo(), "Katana")
        assertEquals(2, katana.modos.size)
        assertEquals("GeB+1 corte", katana.modos[0].danoRaw)
        assertEquals("GdP+1 perf", katana.modos[1].danoRaw)
        // O alcance muda entre os modos — o corte alcança 1 ou 2, a estocada só 1.
        assertEquals("1, 2", katana.modos[0].alcanceCorpoACorpo)
        assertEquals("1", katana.modos[1].alcanceCorpoACorpo)
    }

    @Test
    fun `⚠️ do segundo modo em diante o custo e o peso ficam NULOS`() {
        // O livro escreve "$650 / —": é a mesma arma física. Nulo aqui quer
        // dizer "mesma arma", e quem exibe precisa dizer isso — nunca "grátis".
        val katana = porNome(corpoACorpo(), "Katana")
        assertEquals(650f, katana.modos[0].custo)
        assertNull(katana.modos[1].custo)
        assertNull(katana.modos[1].pesoKg)
        // E o custo do ITEM continua sendo o do 1º modo, senão a arma entraria
        // no inventário de graça.
        assertEquals(650f, katana.custoBase)
    }

    @Test
    fun `a Alabarda tem TRES modos`() {
        val alabarda = porNome(corpoACorpo(), "Alabarda")
        assertEquals(3, alabarda.modos.size)
        assertEquals(listOf("GeB+5 corte", "GeB+4 corte", "GdP+3 perf"), alabarda.modos.map { it.danoRaw })
    }

    @Test
    fun `⚠️ as duas listas do JSON nao sao simetricas, e nenhum modo se perde`() {
        // O Arreador Conjunto tem 2 danos e 1 linha de alcance; uma Espada
        // Bastarda tem 1 dano e 2 linhas. Assumir simetria perderia exatamente
        // as exceções.
        val arreador = porNome(corpoACorpo(), "Arreador Conjunto")
        assertEquals(2, arreador.modos.size)
        assertEquals("1d-3 qmd", arreador.modos[0].danoRaw)
        assertEquals("HT-3(0,5)at", arreador.modos[1].danoRaw)
        // O 2º modo herda o alcance do 1º em vez de ficar sem nada.
        assertEquals("1", arreador.modos[1].alcanceCorpoACorpo)
    }

    @Test
    fun `🔴 o dano colado sem barra tambem e desgrudado`() {
        // Há uma linha do catálogo em que a barra sumiu na digitação:
        // "GeB+2 corteGdP+3 perf". Sem desgrudar, essa arma perderia a estocada
        // em silêncio — o defeito que este lote existe para acabar.
        assertEquals(
            listOf("GeB+2 corte", "GdP+3 perf"),
            ArmasCatalogLoader.desgrudarDano("GeB+2 corteGdP+3 perf")
        )
        // E quem já vem com barra continua funcionando igual.
        assertEquals(
            listOf("GeB+1 corte", "GdP+1 perf"),
            ArmasCatalogLoader.desgrudarDano("GeB+1 corte/GdP+1 perf")
        )
        // Dano de um modo só continua sendo um modo só.
        assertEquals(listOf("GeB+2 corte"), ArmasCatalogLoader.desgrudarDano("GeB+2 corte"))
    }

    @Test
    fun `nenhuma arma do catalogo ficou com dano vazio`() {
        todas().forEach { arma ->
            assertTrue("${arma.nome} ficou sem dano", arma.danoRaw.isNotBlank())
            arma.modos.forEach { m ->
                assertTrue("${arma.nome} modo ${m.ordem} sem dano", m.danoRaw.isNotBlank())
            }
        }
    }

    // ==================================================================
    // 3. 🔴 O alcance de quatro dígitos
    // ==================================================================

    @Test
    fun `🔴 o Max de milhar deixa de virar desconhecido`() {
        // "1.300" com toIntOrNull dava null, e o aviso "Fora de alcance" nunca
        // podia disparar para pistola nenhuma.
        val revolver = porNome(fogo(), "Revólver, .36")
        assertEquals(1300, revolver.maximoMetros)
        val adp = porNome(fogo(), "ADP Gauss, 4 mm")
        assertEquals(700, adp.meioDanoMetros)
        assertEquals(2900, adp.maximoMetros)
    }

    @Test
    fun `toda arma de fogo com alcance na tabela tem Max cadastrado`() {
        // A varredura que prova que o buraco fechou por inteiro, não só nas
        // três que eu olhei.
        val semMax = fogo().filter { it.alcanceMultStRaw == null && it.maximoMetros == null }
        // Sobram só as que o próprio catálogo deixou em branco.
        assertTrue(
            "armas de fogo ainda sem Max: ${semMax.map { it.nome }}",
            semMax.size <= 3
        )
    }

    @Test
    fun `o Max nunca e MENOR que o meio dano`() {
        // Invariante do livro: 1/2D vem antes do Máx. Um sinal trocado na
        // leitura apareceria aqui.
        todas().forEach { arma ->
            val meio = arma.meioDanoMetros ?: return@forEach
            val max = arma.maximoMetros ?: return@forEach
            assertTrue("${arma.nome}: 1/2D $meio > Max $max", meio <= max)
        }
    }

    @Test
    fun `arco continua com alcance por multiplo de ST`() {
        // O ×15/×20 não vira metro fixo: ele depende da ST de quem atira.
        val arco = porNome(distancia(), "Arco Longo")
        assertEquals("×15/×20", arco.alcanceMultStRaw)
        assertNull(arco.meioDanoMetros)
        assertNull(arco.maximoMetros)
    }

    // ==================================================================
    // 4. Os campos que o app nunca lia
    // ==================================================================

    @Test
    fun `NT, CL e peso da municao chegam ao modelo`() {
        val adp = porNome(fogo(), "ADP Gauss, 4 mm")
        assertEquals(10, adp.nt)
        assertEquals(2, adp.cl)
        assertEquals(2.3f, adp.pesoBaseKg)
        assertEquals(0.5f, adp.municaoKg)
        assertEquals("2,3/0,5", adp.pesoRaw)
        assertEquals("$3.600", adp.custoRaw)
        assertEquals("700/2.900", adp.alcanceRaw)
    }

    @Test
    fun `o NT chegou em praticamente todo o catalogo`() {
        val semNt = todas().count { it.nt == null }
        assertTrue("armas sem NT: $semNt", semNt <= 2)
    }

    @Test
    fun `⚠️ CL so existe onde o livro cadastrou`() {
        // 45 armas de fogo têm CL. Corpo a corpo não tem coluna nenhuma — e
        // inventar CL 0 ali diria "arma proibida" para uma faca de cozinha.
        //
        // ⚠️ Eram 42 até o **Lote ARMA-7**: o Rifle de Atirador .338, a ACI 6,8 mm
        // e o Rifle de Gauss tinham a linha deslocada uma coluna e perdiam a CL
        // no fim dela. O conserto devolveu as três (CL 3, 1 e 2).
        assertEquals(45, todas().count { it.cl != null })
        assertTrue(corpoACorpo().all { it.cl == null })
    }

    @Test
    fun `as flags da coluna ST sobrevivem`() {
        val comFlag = todas().filter { it.stFlags.isNotEmpty() }
        // 46 armas carregam 48 flags: a Glaive e a Alabarda têm as DUAS. Guardar
        // só a primeira perderia metade da informação delas.
        //
        // ⚠️ Eram 43/45 até o **Lote ARMA-7**: as três armas de linha deslocada
        // tinham perdido o † junto com a ST (uma delas chegou a marcar ST 41).
        assertEquals(46, comFlag.size)
        assertEquals(48, comFlag.sumOf { it.stFlags.size })
        assertEquals(
            listOf("dagger", "double_dagger"),
            porNome(corpoACorpo(), "Alabarda").stFlags
        )
        assertTrue(
            "flag desconhecida no catalogo",
            comFlag.flatMap { it.stFlags }.all { it == "dagger" || it == "double_dagger" }
        )
        // E a flag continua respondendo pelas duas mãos.
        assertTrue(porNome(fogo(), "ADP Gauss, 4 mm").duasMaos)
    }

    // ==================================================================
    // 5. O que NÃO podia mudar
    // ==================================================================

    @Test
    fun `os ids do catalogo continuam os mesmos`() {
        // `Equipamento.armaCatalogoId` está gravado nas fichas salvas. Um id
        // que mude faz o jogador perder a arma ao abrir a ficha.
        assertEquals("cc_katana", porNome(corpoACorpo(), "Katana").id)
        assertEquals("dist_arco_longo", porNome(distancia(), "Arco Longo").id)
        assertEquals("dist_adp_gauss_4_mm", porNome(fogo(), "ADP Gauss, 4 mm").id)
        assertTrue(corpoACorpo().all { it.id.startsWith("cc_") })
        assertTrue((fogo() + distancia()).all { it.id.startsWith("dist_") })
    }

    @Test
    fun `nenhum id do catalogo esta repetido`() {
        val todas = todas()
        val repetidos = todas.groupBy { it.id }.filter { it.value.size > 1 }.keys
        assertTrue("ids repetidos: $repetidos", repetidos.isEmpty())
    }

    @Test
    fun `o tipo de combate continua sendo um dos tres conhecidos`() {
        val validos = setOf(
            ArmasCatalogLoader.TIPO_CORPO_A_CORPO,
            ArmasCatalogLoader.TIPO_DISTANCIA,
            ArmasCatalogLoader.TIPO_FOGO
        )
        todas().forEach {
            assertTrue("${it.nome}: tipo '${it.tipoCombate}'", it.tipoCombate in validos)
        }
    }
}
