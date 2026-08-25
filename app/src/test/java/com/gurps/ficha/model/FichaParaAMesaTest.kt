package com.gurps.ficha.model

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **A ficha vai para a Mesa** — lote CAMPO-17.
 *
 * ⚠️ Le codigo como texto. Isso apanha uma classe de defeito concreta — o app a
 * mandar para `/api/fichas` e o servidor a servir `/api/ficha` — que nenhum
 * teste dos dois lados apanha, porque cada um esta internamente certo e eles
 * nunca se falam num teste.
 *
 * 🔴 O que ele **nao** apanha e se o botao funciona no aparelho. Isso continua a
 * ser um dedo.
 */
class FichaParaAMesaTest {

    private fun fonte(caminho: String): String {
        val f = File("src/main/java/$caminho")
        assertTrue("nao achei $caminho", f.exists())
        return f.readText()
    }

    private val cliente by lazy { fonte("com/gurps/ficha/data/network/MesaApiClient.kt") }
    private val delegate by lazy {
        fonte("com/gurps/ficha/viewmodel/delegates/FichaSocialDelegate.kt")
    }
    private val viewModel by lazy { fonte("com/gurps/ficha/viewmodel/FichaViewModel.kt") }
    private val dialogo by lazy {
        fonte("com/gurps/ficha/ui/features/rolagem/RolagemDestinoDialog.kt")
    }

    @Test
    fun `o cliente sabe mandar a ficha, e para a rota certa`() {
        assertTrue("o cliente da Mesa nao sabe mandar ficha", cliente.contains("fun postFicha("))
        assertTrue("a ficha nao vai para /api/ficha", cliente.contains("/api/ficha"))
    }

    @Test
    fun `🔴 o token vai no CORPO, e nao na URL`() {
        // 🔴 Query string fica no historico do navegador e nos registros de
        // qualquer intermediario — e este token e a senha da sala inteira.
        val trecho = cliente.substringAfter("fun postFicha(").substringBefore("private fun enviar(")
        assertTrue("o token nao vai no corpo", trecho.contains("\"token\" to token"))
        assertFalse("o token entrou na URL", trecho.contains("?token="))
    }

    @Test
    fun `🔴 manda o bloco CALCULADO, e nao a ficha crua`() {
        // 🔴 A Mesa precisa da Velocidade Basica para ordenar a iniciativa e da
        // Esquiva para mostrar ao lado do boneco. O JSON cru **nao os tem**:
        // sao propriedades, e o Gson serializa campos (v. CAMPO-16).
        assertTrue(
            "o postFicha nao recebe o bloco calculado",
            cliente.contains("ficha: com.gurps.ficha.model.FichaCalculada")
        )
        assertTrue(
            "o app nao calcula o bloco antes de mandar",
            viewModel.contains("FichaCalculada.de(personagem)")
        )
    }

    @Test
    fun `🔴 o autor e o NOME NA MESA, e nao o do personagem`() {
        // 🔴 E por ele que a Mesa acha o token que a pessoa criou. O nome do
        // personagem nao serve: na mesa a pessoa e "Rodolfo" e o boneco chama-se
        // "Aria". Mandar o errado faria a ficha nunca colar em token nenhum — e o
        // sintoma seria "nao acontece nada", o pior de todos.
        assertTrue("nao ha nome na mesa guardado", delegate.contains("var mesaNome"))
        assertTrue(
            "o envio nao usa o nome na mesa",
            viewModel.contains("socialDelegate.mesaNome")
        )
        val envio = viewModel.substringAfter("val nomeNaMesa = socialDelegate.mesaNome")
            .take(500)
        assertTrue(
            "o envio da ficha nao passa o nome na mesa",
            envio.contains("enviarFichaParaAMesa(") && envio.contains("nomeNaMesa")
        )
    }

    @Test
    fun `⚠️ o nome na mesa NAO vai para maiusculas`() {
        // ⚠️ Ao contrario do token, que a sala compara em maiusculas, o nome e
        // comparado byte a byte com o que a pessoa digitou ao entrar — e
        // "RODOLFO" nao casa com "Rodolfo".
        val trecho = delegate.substringAfter("fun configurarMesa(").substringBefore("}")
        assertTrue("o token devia ir para maiusculas", trecho.contains("token?.trim()?.uppercase()"))
        assertFalse(
            "o nome na mesa foi para maiusculas e deixa de casar com quem entrou",
            trecho.contains("nome?.trim()?.uppercase()")
        )
    }

    @Test
    fun `🔴 a ficha e mandada ao SALVAR, sem botao proprio`() {
        // 🔴 "Ao salvar a ficha, o app reenvia e a mesa acompanha sozinha." Salvar
        // e o momento em que os numeros mudaram; um botao a parte seria mais uma
        // coisa para lembrar, e a Mesa ficaria desatualizada em silencio.
        val salvar = viewModel.substringAfter("fun salvarFicha(").substringBefore("fun carregarFicha")
        assertTrue(
            "a ficha nao e mandada ao salvar",
            salvar.contains("enviarFichaParaAMesa(")
        )
    }

    @Test
    fun `⚠️ o envio da ficha NAO depende de haver retrato`() {
        // ⚠️ Uma ficha sem retrato tambem tem Velocidade Basica. Prender uma a
        // outra faria a iniciativa da mesa depender de a pessoa ter escolhido
        // uma foto — e ninguem ligaria as duas coisas.
        val salvar = viewModel.substringAfter("fun salvarFicha(").substringBefore("fun carregarFicha")
        val ondeImagem = salvar.indexOf("val imagemUri")
        val ondeFicha = salvar.indexOf("enviarFichaParaAMesa(")
        val ondeFechaImagem = salvar.indexOf("val nomeNaMesa")
        assertTrue("nao achei os dois envios", ondeImagem >= 0 && ondeFicha >= 0)
        assertTrue(
            "o envio da ficha esta dentro do `if` da imagem",
            ondeFechaImagem in (ondeImagem + 1) until ondeFicha
        )
    }

    @Test
    fun `⚠️ mandar a ficha nunca segura o salvar`() {
        // ⚠️ Ficar sem ficha na Mesa e um aborrecimento; nao salvar e perder
        // trabalho. Como o retrato, isto e best-effort e corre a parte.
        val envio = delegate.substringAfter("suspend fun enviarFichaParaAMesa(")
            .substringBefore("suspend fun enviarRolagem")
        assertTrue(
            "o envio da ficha nao sai fora da linha principal",
            envio.contains("Dispatchers.IO")
        )
        // E desiste sem barulho quando falta configuracao.
        assertTrue("nao desiste sem endereco", envio.contains("mesaEndereco ?: return false"))
        assertTrue("nao desiste sem token", envio.contains("mesaToken ?: return false"))
        assertTrue("nao desiste sem nome", envio.contains("nomeNaMesa.isBlank()"))
    }

    @Test
    fun `o campo do nome na mesa existe na tela, e explica-se`() {
        assertTrue("nao ha campo do nome na mesa", dialogo.contains("Seu nome na mesa"))
        // ⚠️ O leitor de tela precisa de saber QUAL nome. "Nome" sozinho nao
        // distingue do nome do personagem, que e o que a pessoa acabou de digitar
        // tres telas atras.
        assertTrue(
            "o campo do nome nao se explica para o leitor de tela",
            dialogo.contains("Seu nome na mesa virtual, o mesmo com que voce entra na sala")
        )
    }

    @Test
    fun `⚠️ o POST nao foi copiado uma quarta vez`() {
        // ⚠️ Eram tres copias da mesma abertura de conexao, do mesmo tratamento
        // de 401 e do mesmo `finally`. A quarta ia nascer diferente das outras —
        // que e como comeca o defeito numero um deste projeto.
        assertTrue("o POST nao foi extraido", cliente.contains("private fun enviar("))
        val aberturas = Regex("openConnection\\(\\) as HttpURLConnection").findAll(cliente).count()
        assertTrue(
            "ha $aberturas aberturas de conexao no cliente da Mesa; deviam ser poucas",
            aberturas <= 2
        )
    }
}
