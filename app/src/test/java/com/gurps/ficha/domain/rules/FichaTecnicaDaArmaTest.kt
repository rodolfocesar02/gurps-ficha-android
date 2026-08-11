package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.loaders.ArmasCatalogLoader
import com.gurps.ficha.model.ArmaCatalogoItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **Lote ARMA-2** — a ficha técnica em português.
 *
 * ## O que este arquivo protege
 *
 * A tradução do jargão é **regra do livro**, não escolha de desenho. Uma frase
 * errada aqui mente para o jogador com a mesma cara de quem está certa — foi
 * assim que o meu primeiro rascunho escreveu *"CL 2 · militar"*, quando a p.508
 * diz que **CL 1 é militar e CL 2 é restrito**.
 *
 * Por isso cada frase tem asserção, e a varredura no fim exige que **nenhuma
 * arma do catálogo** produza uma linha com número sem explicação.
 */
class FichaTecnicaDaArmaTest {

    private fun asset(nome: String): String {
        val direto = File("src/main/assets/$nome")
        val f = if (direto.exists()) direto else File("app/src/main/assets/$nome")
        assertTrue("nao encontrei ${f.absolutePath}", f.exists())
        return f.readText(Charsets.UTF_8)
    }

    private val fogo by lazy {
        ArmasCatalogLoader.distancia(asset(ArmasCatalogLoader.ARQUIVO_FOGO), ArmasCatalogLoader.TIPO_FOGO)
    }
    private val distancia by lazy {
        ArmasCatalogLoader.distancia(asset(ArmasCatalogLoader.ARQUIVO_DISTANCIA), ArmasCatalogLoader.TIPO_DISTANCIA)
    }
    private val corpoACorpo by lazy {
        ArmasCatalogLoader.corpoACorpo(asset(ArmasCatalogLoader.ARQUIVO_CORPO_A_CORPO))
    }

    private fun arma(lista: List<ArmaCatalogoItem>, nome: String): ArmaCatalogoItem {
        val a = lista.firstOrNull { it.nome == nome }
        assertNotNull("nao achei '$nome'", a)
        return a!!
    }

    private fun destaque(f: FichaDeEquipamento.Ficha, rotulo: String) =
        f.destaques.first { it.rotulo == rotulo }

    private fun detalhe(f: FichaDeEquipamento.Ficha, rotulo: String) =
        f.detalhes.first { it.rotulo == rotulo }

    // ==================================================================
    // 1. 🔴 A Classe de Legalidade
    // ==================================================================

    @Test
    fun `🔴 a escala de CL e a do livro, e CL 2 NAO e militar`() {
        // MB p.508. Militar é a CL 1. Este teste existe porque eu escrevi
        // "CL 2 · militar" antes de conferir a página.
        assertEquals("banido", FichaTecnicaDaArma.nomeDaCl(0))
        assertEquals("militar", FichaTecnicaDaArma.nomeDaCl(1))
        assertEquals("restrito", FichaTecnicaDaArma.nomeDaCl(2))
        assertEquals("licenciado", FichaTecnicaDaArma.nomeDaCl(3))
        assertEquals("aberto", FichaTecnicaDaArma.nomeDaCl(4))
    }

    @Test
    fun `o selo de CL so aparece onde o livro cadastrou`() {
        // p.272: "Somente as armas de fogo e granadas contêm a indicação".
        // Inventar CL numa espada diria ao jogador que ela é controlada.
        val adp = FichaTecnicaDaArma.de(arma(fogo, "ADP Gauss, 4 mm"), st = 11)
        assertEquals("CL 2 · restrito", adp.selo)
        val katana = FichaTecnicaDaArma.de(arma(corpoACorpo, "Katana"), st = 11)
        assertEquals(null, katana.selo)
    }

    // ==================================================================
    // 2. 🔴 A Precisão com mira acoplada
    // ==================================================================

    @Test
    fun `🔴 a mira acoplada aparece como parcela separada`() {
        // MB p.270: "o bônus [...] aparecerá como um modificador separado ao
        // lado da Prec básica; ex.: 7+2".
        val f = FichaTecnicaDaArma.de(arma(fogo, "Rifle de Atirador, .338"), st = 11)
        val prec = destaque(f, "Precisão")
        assertEquals("6 +3", prec.valor)
        assertTrue(prec.explicacao!!, prec.explicacao.contains("mira acoplada"))
        assertTrue(prec.explicacao, prec.explicacao.contains("Apontar"))
    }

    @Test
    fun `arma sem mira nao inventa a parcela`() {
        val f = FichaTecnicaDaArma.de(arma(fogo, "Pistola de Pederneira, .51"), st = 11)
        val prec = destaque(f, "Precisão")
        assertEquals("1", prec.valor)
        assertTrue(prec.explicacao!!, !prec.explicacao.contains("mira"))
    }

    // ==================================================================
    // 3. O alcance com a ST de quem empunha
    // ==================================================================

    @Test
    fun `o arco tem a conta do alcance FEITA com a ST da ficha`() {
        // ×15/×20 numa ST 11 é 165/220 m. Guardar o "×15" e não multiplicar
        // deixa a conta para o jogador no meio da mesa.
        val f = FichaTecnicaDaArma.de(arma(distancia, "Arco Longo"), st = 11)
        val alc = destaque(f, "Alcance")
        assertEquals("×15/×20", alc.valor)
        assertTrue(alc.explicacao!!, alc.explicacao.contains("165 / 220 m"))
        // E muda junto com a ST: o mesmo arco vai mais longe numa ST 14.
        val forte = FichaTecnicaDaArma.de(arma(distancia, "Arco Longo"), st = 14)
        assertTrue(destaque(forte, "Alcance").explicacao!!.contains("210 / 280 m"))
    }

    @Test
    fun `🔴 o Max de milhar chega inteiro na tela`() {
        val f = FichaTecnicaDaArma.de(arma(fogo, "ADP Gauss, 4 mm"), st = 11)
        val alc = destaque(f, "Alcance")
        assertEquals("700 / 2900 m", alc.valor)
        assertTrue(alc.explicacao!!, alc.explicacao.contains("metade"))
    }

    // ==================================================================
    // 4. O glossário
    // ==================================================================

    @Test
    fun `Tiros vira frase, com o caso do i que e por tiro`() {
        assertEquals(
            "80 tiros, 3 turnos para recarregar",
            FichaTecnicaDaArma.explicarTiros("80(3)")
        )
        assertEquals(
            "1 tiro, 20 turnos para recarregar",
            FichaTecnicaDaArma.explicarTiros("1(20)")
        )
        // ⚠️ O "i" muda o sentido inteiro (MB p.271): o tempo é POR TIRO.
        val individual = FichaTecnicaDaArma.explicarTiros("6(3i)")
        assertTrue(individual!!, individual.contains("POR TIRO"))
        // "A" é arma de arremesso.
        assertTrue(FichaTecnicaDaArma.explicarTiros("A")!!.contains("arremesso"))
    }

    @Test
    fun `⚠️ Tiros em formato desconhecido fica CALADO, nao chuta`() {
        // Explicação inventada é pior que explicação nenhuma: o jogador confia.
        assertEquals(null, FichaTecnicaDaArma.explicarTiros("3×9 estranho"))
        assertEquals(null, FichaTecnicaDaArma.explicarTiros(""))
        assertEquals(null, FichaTecnicaDaArma.explicarTiros(null))
    }

    @Test
    fun `Recuo vira a regra do tiro multiplo`() {
        // MB p.272: cada múltiplo inteiro do Recuo na margem = mais um acerto.
        assertTrue(FichaTecnicaDaArma.explicarRecuo(2)!!.contains("cada 2 pontos"))
        assertTrue(FichaTecnicaDaArma.explicarRecuo(1)!!.contains("recuo fraco"))
        assertEquals(null, FichaTecnicaDaArma.explicarRecuo(null))
    }

    @Test
    fun `⚠️ o cruz-duplo NAO e a mesma coisa que a adaga`() {
        // MB p.271: os dois exigem duas mãos, mas o ‡ ainda deixa a arma
        // DESPREPARADA depois do ataque. Tratar como sinônimo esconde metade.
        assertEquals(" †", FichaTecnicaDaArma.simbolosDeSt(listOf("dagger")))
        assertEquals(" ‡", FichaTecnicaDaArma.simbolosDeSt(listOf("double_dagger")))
        assertEquals(" † ‡", FichaTecnicaDaArma.simbolosDeSt(listOf("dagger", "double_dagger")))

        val alabarda = FichaTecnicaDaArma.de(arma(corpoACorpo, "Alabarda"), st = 13)
        val st = detalhe(alabarda, "ST mínima")
        assertTrue(st.explicacao!!, st.explicacao.contains("despreparada"))
        // 🔴 A Alabarda vem com `valor` NULO no catálogo: o livro dá "13‡ / 12",
        // uma ST por modo. Antes disso a tela mostrava só um travessão e a arma
        // ficava sem ST nenhuma.
        assertEquals("13‡ / 12", st.valor)
        assertTrue(st.explicacao, st.explicacao.contains("uma ST por modo"))
    }

    @Test
    fun `o asterisco do alcance corpo a corpo e explicado`() {
        // MB p.270: o asterisco exige manobra Preparar para mudar de alcance.
        // É a informação que mais escapa na mesa.
        val texto = FichaTecnicaDaArma.explicarAlcanceCorpoACorpo("1–3*")
        assertTrue(texto!!, texto.contains("Preparar"))
        assertEquals(null, FichaTecnicaDaArma.explicarAlcanceCorpoACorpo("1"))
    }

    @Test
    fun `Aparar desbalanceada diz o que custa`() {
        assertTrue(FichaTecnicaDaArma.explicarAparar("0D")!!.contains("não ataca no mesmo turno"))
        assertTrue(FichaTecnicaDaArma.explicarAparar("Não")!!.contains("não dá para aparar"))
        assertTrue(FichaTecnicaDaArma.explicarAparar("0")!!.contains("sem modificador"))
    }

    @Test
    fun `o divisor de armadura do dano e traduzido`() {
        val texto = FichaTecnicaDaArma.explicarDano("4d(3) pa-")
        assertTrue(texto!!, texto.contains("divisor de armadura 3"))
        assertTrue(texto, texto.contains("0,5"))
    }

    // ==================================================================
    // 5. Os modos de ataque na tela
    // ==================================================================

    @Test
    fun `🔴 a Katana mostra os DOIS ataques`() {
        val f = FichaTecnicaDaArma.de(
            arma(corpoACorpo, "Katana"), st = 11,
            resolverDano = { if (it.startsWith("GeB")) "1d+2 corte" else "1d+1 perf" }
        )
        assertEquals(2, f.modos.size)
        assertEquals("GeB+1 corte", f.modos[0].dano)
        assertEquals("GdP+1 perf", f.modos[1].dano)
        assertEquals("1d+2 corte", f.modos[0].danoComSt)
        // O alcance de cada modo entra no detalhe da linha.
        assertTrue(f.modos[0].detalhe!!, f.modos[0].detalhe!!.contains("Alcance 1, 2"))
        assertTrue(f.modos[1].detalhe!!, f.modos[1].detalhe!!.contains("Alcance 1"))
    }

    @Test
    fun `⚠️ do segundo modo em diante a tela sabe que e a MESMA arma`() {
        // Sem essa marca, o custo nulo do 2º modo viraria "grátis" na tela.
        val f = FichaTecnicaDaArma.de(arma(corpoACorpo, "Katana"), st = 11)
        assertTrue(!f.modos[0].mesmaArma)
        assertTrue(f.modos[1].mesmaArma)
        // E o custo do ITEM continua um só.
        assertEquals("$650", detalhe(f, "Custo").valor)
    }

    @Test
    fun `arma de fogo nao vira lista de modos a toa`() {
        // Uma pistola tem um jeito de atirar. Uma seção "modos de ataque" com
        // um item só é ruído.
        val f = FichaTecnicaDaArma.de(arma(fogo, "ADP Gauss, 4 mm"), st = 11)
        assertTrue(f.modos.isEmpty())
    }

    // ==================================================================
    // 6. O peso da munição, que sumia
    // ==================================================================

    @Test
    fun `o peso mostra arma e municao separados`() {
        val f = FichaTecnicaDaArma.de(arma(fogo, "ADP Gauss, 4 mm"), st = 11)
        val peso = detalhe(f, "Peso")
        assertEquals("2,3 kg + 0,5 kg", peso.valor)
        assertTrue(peso.explicacao!!, peso.explicacao.contains("munição"))
    }

    @Test
    fun `arma sem municao nao inventa o segundo numero`() {
        val f = FichaTecnicaDaArma.de(arma(corpoACorpo, "Katana"), st = 11)
        assertEquals("2,5 kg", detalhe(f, "Peso").valor)
    }

    @Test
    fun `o custo sai com separador de milhar`() {
        assertEquals("$3.600", FichaDeEquipamento.formatarDinheiro(3600f))
        assertEquals("$50", FichaDeEquipamento.formatarDinheiro(50f))
    }

    // ==================================================================
    // 7. 🔴 A varredura: nenhuma arma pode quebrar a tela
    // ==================================================================

    @Test
    fun `🔴 as 150 armas montam a ficha sem estourar`() {
        val todas = fogo + distancia + corpoACorpo
        assertEquals(150, todas.size)
        todas.forEach { a ->
            val f = FichaTecnicaDaArma.de(a, st = 10)
            assertTrue("${a.nome}: ficou sem nome", f.nome.isNotBlank())
            assertTrue("${a.nome}: ficou sem subtitulo", f.subtitulo.isNotBlank())
            assertTrue("${a.nome}: ficou sem nenhuma linha", f.detalhes.isNotEmpty())
        }
    }

    @Test
    fun `🔴 campo ausente sai como travessao, NUNCA como zero`() {
        // Zero é um dado; "não sei" é outro. Um Recuo 0 diria "esta arma não
        // coiceia", e não é isso que o catálogo está dizendo.
        val todas = fogo + distancia + corpoACorpo
        todas.forEach { a ->
            val f = FichaTecnicaDaArma.de(a, st = 10)
            (f.destaques + f.detalhes).forEach { linha ->
                if (linha.valor == FichaDeEquipamento.AUSENTE) return@forEach
                assertTrue(
                    "${a.nome} / ${linha.rotulo}: valor vazio na tela",
                    linha.valor.isNotBlank()
                )
            }
        }
        // E a arma sem Recuo cadastrado (arco) mostra travessão de verdade.
        val arco = FichaTecnicaDaArma.de(arma(distancia, "Arco Longo"), st = 11)
        assertEquals(FichaDeEquipamento.AUSENTE, destaque(arco, "Recuo").valor)
    }

    @Test
    fun `a linha acessivel junta rotulo, valor e explicacao`() {
        // TalkBack lendo "Precisão", pausa, "6 +3", pausa, é inútil.
        val linha = FichaDeEquipamento.Linha("Precisão", "6 +3", "só vale se você Apontar")
        assertEquals("Precisão: 6 +3. só vale se você Apontar", linha.descricaoAcessivel)
    }
}
