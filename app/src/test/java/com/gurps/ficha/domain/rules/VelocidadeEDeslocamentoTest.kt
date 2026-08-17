package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **Comprar Velocidade Básica e Deslocamento Básico** — MB p.17. Lote ATR-1.
 *
 * 🔴 Achado pelo usuário: os dois existiam em Raças e Metacaracterísticas e
 * **não existiam na ficha normal**.
 *
 * ⚠️ E a regra estava toda pronta: o modelo, o custo em `CharacterRules` e os
 * setters do ViewModel. Faltava só a TELA PERGUNTAR. É a quinta vez que este
 * formato aparece no projeto — antes foram o XP, o campo de RD, o
 * `custoTotalTalento` e o `nivelTalento`.
 */
class VelocidadeEDeslocamentoTest {

    private fun fonte(caminho: String): String {
        val direto = File("src/main/java/$caminho")
        val f = if (direto.exists()) direto else File("app/src/main/java/$caminho")
        assertTrue("nao encontrei ${f.absolutePath}", f.exists())
        return f.readText(Charsets.UTF_8)
    }

    // == O calculo do livro =============================================

    @Test
    fun `a Velocidade Basica e HT mais DX dividido por 4, sem arredondar`() {
        // "Nao arredonde esse valor. 5,25 e melhor que 5!"
        val p = Personagem(destreza = 11, vitalidade = 10)
        assertEquals(5.25f, p.velocidadeBasica, 0.001f)
    }

    @Test
    fun `o Deslocamento IGNORA a fracao, e nao arredonda`() {
        // "Um personagem com Velocidade Basica 5,75 tem um Deslocamento Basico
        // de 5." ⚠️ Arredondar daria 6 -- um metro por segundo de graca.
        assertEquals(5, VelocidadeEDeslocamento.deslocamentoDe(5.75f))
        assertEquals(5, VelocidadeEDeslocamento.deslocamentoDe(5.25f))
        assertEquals(5, VelocidadeEDeslocamento.deslocamentoDe(5.0f))
        val p = Personagem(destreza = 12, vitalidade = 11)          // 23/4 = 5,75
        assertEquals(5.75f, p.velocidadeBasica, 0.001f)
        assertEquals(5, p.deslocamentoBasico)
    }

    // == O custo, que ja existia e ninguem alcancava =====================

    @Test
    fun `cada 0,25 de Velocidade custa 5 pontos`() {
        assertEquals(5, VelocidadeEDeslocamento.custoDaVelocidade(0.25f))
        assertEquals(20, VelocidadeEDeslocamento.custoDaVelocidade(1.0f))
        assertEquals(-10, VelocidadeEDeslocamento.custoDaVelocidade(-0.5f))
        assertEquals(0, VelocidadeEDeslocamento.custoDaVelocidade(0f))
    }

    @Test
    fun `cada metro por segundo custa 5 pontos`() {
        assertEquals(5, VelocidadeEDeslocamento.custoDoDeslocamento(1))
        assertEquals(-15, VelocidadeEDeslocamento.custoDoDeslocamento(-3))
    }

    @Test
    fun `o custo entra nos pontos gastos da ficha`() {
        // O elo que importa: comprar tem de PESAR. Se a tela deixasse ajustar e
        // o total nao mudasse, seria pior do que nao ter a tela.
        val base = Personagem(pontosIniciais = 150, destreza = 10, vitalidade = 10)
        val rapido = base.copy(modVelocidadeBasica = 1.0f, modDeslocamentoBasico = 2)
        assertEquals(base.pontosGastos + 20 + 10, rapido.pontosGastos)
        assertEquals(6.0f, rapido.velocidadeBasica, 0.001f)
        assertEquals(8, rapido.deslocamentoBasico)      // 6 + 2 comprados
    }

    @Test
    fun `nao ha segunda conta do custo`() {
        // ⚠️ `CharacterRules` ja cobrava. Reescrever a conta aqui criaria duas
        // rotas para o mesmo numero -- e o defeito mora na diferenca.
        val src = fonte("com/gurps/ficha/domain/rules/VelocidadeEDeslocamento.kt")
        assertTrue(
            "a conta do custo foi reescrita em vez de reusada",
            src.contains("CharacterRules.calcularPassosVelocidadeBasica(")
        )
    }

    // == O limite da campanha realista ===================================

    @Test
    fun `o limite realista AVISA, e nao trava`() {
        // "O Mestre nao deve permitir ... mais que 2 pontos positivos ou
        // negativos. Personagens nao-humanos e supers nao estao sujeitos a essa
        // limitacao." ⚠️ O proprio livro abre a excecao na mesma frase.
        assertNull(VelocidadeEDeslocamento.avisoDoLimiteRealista(2.0f))
        assertNull(VelocidadeEDeslocamento.avisoDoLimiteRealista(-2.0f))
        assertNull(VelocidadeEDeslocamento.avisoDoLimiteRealista(0f))

        val aviso = VelocidadeEDeslocamento.avisoDoLimiteRealista(2.25f)
        assertNotNull(aviso)
        assertTrue(aviso!!, aviso.contains("não-humanos"))
        assertTrue(aviso, aviso.contains("p.17"))
        assertFalse("a fala tem sinal cru", RotuloAcessivel.temSinalCru(aviso))

        // E o custo continua saindo, porque o aviso nao bloqueia nada.
        assertEquals(45, VelocidadeEDeslocamento.custoDaVelocidade(2.25f))
    }

    @Test
    fun `a reducao tambem avisa`() {
        val aviso = VelocidadeEDeslocamento.avisoDoLimiteRealista(-3.0f)
        assertNotNull(aviso)
        assertTrue(aviso!!, aviso.contains("redução"))
    }

    // == A tela, que era o que faltava ==================================

    @Test
    fun `a aba Geral deixa COMPRAR os dois`() {
        // 🔴 Antes eles apareciam so em "Caracteristicas Derivadas", como numero
        // para ler. O jogador nao tinha onde gastar os 5 pontos que o livro pede.
        val aba = fonte("com/gurps/ficha/ui/TabGeral.kt")
        assertTrue(
            "a compra da Velocidade Basica sumiu da tela",
            aba.contains("viewModel.atualizarModVelocidadeBasica(")
        )
        assertTrue(
            "a compra do Deslocamento Basico sumiu da tela",
            aba.contains("viewModel.atualizarModDeslocamentoBasico(")
        )
        assertTrue("o aviso do limite realista nao chega na tela",
            aba.contains("avisoDoLimiteRealista("))
    }

    @Test
    fun `o passo da Velocidade e 0,25 e nao 1`() {
        // ⚠️ Passo de 1 cobraria 20 pontos por toque, quatro vezes o degrau do
        // livro -- e o jogador so descobriria olhando o total.
        val aba = fonte("com/gurps/ficha/ui/TabGeral.kt")
        assertTrue(
            "o botao voltou a andar de 1 em 1 na Velocidade Basica",
            aba.contains("VelocidadeEDeslocamento.PASSO_DA_VELOCIDADE")
        )
        assertEquals(0.25f, VelocidadeEDeslocamento.PASSO_DA_VELOCIDADE, 0.0001f)
    }

    @Test
    fun `o botao tem descricao para quem nao ve a tela`() {
        val aba = fonte("com/gurps/ficha/ui/TabGeral.kt")
        assertTrue(
            "os botoes de compra ficaram sem descricao acessivel",
            aba.contains("descricaoMenos") && aba.contains("descricaoMais")
        )
    }

    // == O controle segue o costume de cada variante ====================

    /**
     * 🔴 Pedido do usuário depois de ver a tela: *"pra versao visual use o padrao
     * dos atributos, do pv, vont, per, pf onde arrasta o dedo pra cima ou pra
     * baixo ... e no pracego, vc deixa o +- como de costume nessa versao"*.
     *
     * ⚠️ E ele está certo pela tela, não só pelo gosto: quatro atributos se
     * ajustando de um jeito e dois logo abaixo de outro ensina **duas** coisas
     * onde havia uma. A primeira versão que eu fiz punha botões nos dois.
     */
    @Test
    fun `a compra usa arraste na visual e botoes na pracego`() {
        val aba = fonte("com/gurps/ficha/ui/TabGeral.kt")
        val i = aba.indexOf("private fun LinhaDeCompra(")
        assertTrue("a linha de compra sumiu", i > 0)
        val corpo = aba.substring(i)

        assertTrue(
            "a linha de compra deixou de separar as duas variantes",
            corpo.contains("if (isPraCegoVariant)")
        )
        assertTrue(
            "a variante visual perdeu o arraste dos atributos",
            corpo.contains("detectVerticalDragGestures(")
        )
        assertTrue(
            "a variante pracego perdeu os botoes de menos e mais",
            corpo.contains("AppBotaoPasso(")
        )
    }

    @Test
    fun `o arraste usa o MESMO passo dos outros atributos`() {
        // ⚠️ 40 px por degrau e o numero do `AtributoSecundarioEditor`. Um valor
        // proprio aqui faria a Velocidade responder diferente do PV no mesmo
        // dedo, na mesma tela.
        val aba = fonte("com/gurps/ficha/ui/TabGeral.kt")
        // O invariante e "todo arraste desta tela anda o mesmo tanto", e nao um
        // numero de ocorrencias -- contar ocorrencias quebraria no dia em que
        // alguem acrescentasse um quarto ajuste, sem nada de errado ter
        // acontecido. Sao tres hoje: primarios, secundarios e esta compra.
        val passos = Regex("""val passoPx = (\S+)""").findAll(aba)
            .map { it.groupValues[1] }.toList()
        assertTrue("sumiu o arraste da aba Geral", passos.size >= 3)
        assertEquals(
            "algum arraste da aba Geral anda diferente dos outros: $passos",
            1, passos.toSet().size
        )
        assertEquals("40f", passos.first())
    }

    @Test
    fun `o arraste conta DEGRAUS, e nao fracoes de Velocidade`() {
        // 🔴 Se o gesto somasse 0,25 direto no valor, o acumulo de float faria a
        // Velocidade parar em 0,7499998 e o custo virar um misterio. O arraste
        // anda em degraus inteiros e a fracao sai da multiplicacao.
        val aba = fonte("com/gurps/ficha/ui/TabGeral.kt")
        assertTrue(
            "a compra da Velocidade voltou a somar fracao direto",
            aba.contains("CharacterRules.calcularPassosVelocidadeBasica(modVelocidade)")
        )
        assertTrue(
            "o degrau deixou de virar fracao na hora de salvar",
            aba.contains("onVelocidade(degrau * VelocidadeEDeslocamento.PASSO_DA_VELOCIDADE)")
        )
    }

    @Test
    fun `o valor tem descricao acessivel nas duas variantes`() {
        // Na visual o numero E o controle; sem descricao ele seria um texto mudo
        // que por acaso responde ao dedo.
        val aba = fonte("com/gurps/ficha/ui/TabGeral.kt")
        assertTrue(
            "o valor da compra ficou sem descricao acessivel",
            aba.contains("descricaoDoValor")
        )
    }
}
