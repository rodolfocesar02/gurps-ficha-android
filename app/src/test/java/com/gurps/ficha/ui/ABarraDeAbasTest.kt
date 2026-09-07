package com.gurps.ficha.ui

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **A barra de abas não pode voltar a se mexer** — lote BARRA-1.
 *
 * ## 🟥 O defeito, achado por ele num vídeo de 10 segundos
 *
 * > *"to achando muito desalinhado, os efeitos muito mal feito!"*
 *
 * Lido quadro a quadro, era uma coisa só com três caras:
 *
 * 1. O ícone escolhido ia de 31dp a **62dp de layout** — ocupava o dobro do
 *    espaço e **empurrava os vizinhos**. A fila inteira deslizava a cada toque.
 * 2. As abas estavam com `Arrangement.End`, encostadas à direita: com sete
 *    ficava um buraco à esquerda, e só com nove a barra parecia cheia.
 * 3. O nome saía da caixa do ícone e caía **por cima** dos vizinhos —
 *    *PERÍCIAS* escrito sobre os três últimos, *TÉCNICAS* cortado em *CNICAS*.
 *
 * ⚠️ Estas sondas não medem se está bonito — isso é olho, e é dele. Elas guardam
 * as três decisões **estruturais**, que são as que se desfazem sem querer.
 */
class ABarraDeAbasTest {

    private val fonte by lazy {
        val f = File("src/main/java/com/gurps/ficha/ui/components/FichaCustomNavigationBar.kt")
        assertTrue("nao achei a barra", f.exists())
        f.readText()
    }

    /** O código sem as linhas de comentário — elas falam do defeito para o explicar. */
    private val codigo by lazy {
        fonte.lines()
            .filterNot {
                val t = it.trim()
                t.startsWith("*") || t.startsWith("//") || t.startsWith("/*")
            }
            .joinToString("\n")
    }

    @Test
    fun `🟥 o escolhido cresce SEM pedir espaco, e a fila nao se mexe`() {
        // 🔴 A causa do "desalinhado". `graphicsLayer` pinta maior sem mudar o
        // layout; um tamanho animado voltaria a empurrar os vizinhos.
        assertTrue(
            "o crescimento do escolhido deixou de ser so pintura",
            codigo.contains("scaleX = escala")
        )
        assertFalse(
            "voltou a animar o TAMANHO do icone -- isso empurra a fila inteira",
            codigo.contains("animateDpAsState") && codigo.contains("targetSize")
        )
        assertTrue(
            "a caixa do icone deixou de ter tamanho fixo",
            codigo.contains("Modifier.size(TAMANHO_DO_ICONE)")
        )
    }

    @Test
    fun `🟥 as abas ficam no CENTRO, e nao encostadas na direita`() {
        assertFalse(
            "as abas voltaram para a direita -- e o buraco a esquerda com elas",
            codigo.contains("Arrangement.End")
        )
        // E o contrapeso: sem ele, "centro" e o centro do espaco que sobra, e a
        // fila fica sempre um pouco a direita.
        assertTrue(
            "o contrapeso da direita sumiu, e o centro ficou torto",
            codigo.contains("Spacer(modifier = Modifier.width(LARGURA_DA_ANCORA))")
        )
    }

    @Test
    fun `🟥 o nome tem uma LINHA so, e nao flutua ao lado do icone`() {
        // 🔴 Era posicionado por conta de pixel escrita a mao e saia da caixa com
        // `unbounded` -- dai escrever por cima dos vizinhos e sair cortado.
        assertFalse(
            "o nome voltou a escapar da caixa e pode cair por cima dos vizinhos",
            codigo.contains("unbounded = true")
        )
        assertFalse(
            "voltou a conta de pixel a mao para posicionar o nome",
            codigo.contains("lateralOffset")
        )
        // A linha e reservada mesmo com o nome escondido: se ela crescesse e
        // encolhesse, a fila subiria e desceria a cada troca.
        assertTrue(
            "a linha do nome deixou de ter altura reservada",
            codigo.contains("Modifier.height(ALTURA_DO_NOME)")
        )
    }

    @Test
    fun `🔴 o nome some sozinho depois de 2 segundos`() {
        // Pedido dele: "precisamos dos nomes, mas nao quero eles fixos".
        assertTrue(codigo.contains("LaunchedEffect(currentIndex)"))
        assertTrue("o nome deixou de sumir", codigo.contains("nomeAVista = false"))
        assertTrue("o tempo de espera mudou", codigo.contains("delay(2000)"))
    }

    @Test
    fun `🟥 o brilho fica PRESO ao tamanho do icone`() {
        // 🔴 Achado dele: "o brilho esta passando pra fora do icone". A causa era
        // um `scale(1.4)` no desenho -- ele nascia maior que a caixa.
        assertFalse(
            "o brilho voltou a ser desenhado maior que a caixa, e vaza",
            codigo.contains("scale(glowScale)")
        )
        assertTrue(
            "o raio do brilho deixou de ser o da propria caixa",
            codigo.contains("radius = size.minDimension / 2f")
        )
    }

    @Test
    fun `🔴 o pulinho saiu, e o cometa entrou`() {
        assertFalse(
            "o pulinho voltou -- e o icone flutuando sozinho e o que parecia mal feito",
            codigo.contains("bobbingOffset")
        )
        assertTrue("o cometa sumiu", codigo.contains("OTrilhoDoCometa"))
        // 🔴 O trilho e as abas usam a MESMA largura. Uma segunda conta seria uma
        // conta para divergir no dia em que a largura mudasse.
        assertTrue(
            "o cometa deixou de usar a largura da aba, e vai sair do lugar",
            codigo.contains("larguraDaAba * escolhida")
        )
    }

    @Test
    fun `⚠️ mas o anel do microfone FICA -- e o unico efeito que avisa algo`() {
        assertTrue(codigo.contains("vozRingScale"))
        assertTrue(codigo.contains("vozRingAlpha"))
    }
}
