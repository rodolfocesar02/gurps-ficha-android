package com.gurps.ficha.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **O link que a Mesa manda** — lote CC-5.
 *
 * 🔴 Tudo o que chega aqui vem **de fora**: o link pode ter sido escrito por
 * qualquer um, e é texto que passa pelo navegador antes de chegar. Estes testes
 * são sobre o que acontece quando ele vem torto.
 */
class PedidoDaMesaTest {

    @Test
    fun `o pedido inteiro le-se`() {
        val p = PedidoDaMesa.ler(
            "gurpsapp://rolar?o=ataque&pericia=espada_larga&mod=-7" +
                "&onde=no%20cr%C3%A2nio&alvo=Orc&acao=p1"
        )!!
        assertEquals(PedidoDaMesa.Oque.ATAQUE, p.oQue)
        assertEquals("espada_larga", p.pericia)
        assertEquals(-7, p.mod)
        // 🔴 O `%20` e o `%C3%A2` voltam a ser espaço e `â`.
        assertEquals("no crânio", p.onde)
        assertEquals("Orc", p.alvo)
        assertEquals("p1", p.acao)
    }

    /**
     * ⚠️ **A barra depois do host**, quando ela vier.
     *
     * Um `Uri` pode trazer o caminho `/` conforme quem o monta, e recusá-lo
     * faria o aplicativo abrir na aba de sempre sem dizer porquê.
     *
     * 🔴 Escrevi isto a achar que era a causa de o link não pegar no emulador.
     * **Não era** — o registo mostrou o endereço a chegar inteiro e sem barra.
     * O teste fica porque a tolerância é boa; a explicação é que estava errada.
     */
    @Test
    fun `o endereco com barra depois do host tambem se le`() {
        val comBarra = PedidoDaMesa.ler("gurpsapp://rolar/?o=ataque&mod=-7")
        assertEquals(-7, comBarra?.mod)
        assertEquals(PedidoDaMesa.Oque.ATAQUE, comBarra?.oQue)

        // E sem consulta nenhuma continua a não ser pedido.
        assertNull(PedidoDaMesa.ler("gurpsapp://rolar/"))
        // ⚠️ Mas um host que só COMEÇA igual não passa.
        assertNull(PedidoDaMesa.ler("gurpsapp://rolarx/?o=ataque"))
    }

    /**
     * ⚠️ A frase diz **de onde veio o número**. Um modificador que aparece
     * sozinho num campo parece defeito, e a pessoa apaga-o — e aí a Mesa e o
     * telefone passam a dizer números diferentes para o mesmo golpe.
     */
    @Test
    fun `a frase diz o que a Mesa pediu`() {
        val ataque = PedidoDaMesa.ler(
            "gurpsapp://rolar?o=ataque&mod=-7&onde=no%20cr%C3%A2nio&alvo=Orc")!!
        assertEquals("Atacar em Orc no crânio (-7)", ataque.frase())

        val semMod = PedidoDaMesa.ler("gurpsapp://rolar?o=dano&alvo=Orc")!!
        assertEquals("Rolar o dano em Orc", semMod.frase())

        val so = PedidoDaMesa.ler("gurpsapp://rolar?o=defesa&mod=2")!!
        assertEquals("Defender (+2)", so.frase())
    }

    /**
     * 🔴 Devolver `null` é o normal, e não um erro.
     *
     * ⚠️ O mesmo esquema serve o `gurpsapp://conectar` de outro lote, e um link
     * estragado tem de deixar o aplicativo abrir na tela de sempre — e não numa
     * mensagem de erro que ninguém sabe o que fazer com ela.
     */
    @Test
    fun `um link que nao e deste tipo nao vira pedido`() {
        assertNull(PedidoDaMesa.ler(null))
        assertNull(PedidoDaMesa.ler(""))
        assertNull(PedidoDaMesa.ler("gurpsapp://conectar?id=123&token=abc"))
        assertNull(PedidoDaMesa.ler("https://mesagurps.duckdns.org/rolar?o=ataque"))
        assertNull(PedidoDaMesa.ler("gurpsapp://rolarx?o=ataque"))
        // Sem dizer O QUE rolar, não há pedido: escolher um por omissão poria a
        // pessoa a rolar dano quando a Mesa pediu um ataque.
        assertNull(PedidoDaMesa.ler("gurpsapp://rolar?pericia=espada_larga&mod=-7"))
        assertNull(PedidoDaMesa.ler("gurpsapp://rolar?o=voar"))
        assertNull(PedidoDaMesa.ler("gurpsapp://rolar"))
    }

    /**
     * 🟥 **O modificador é preso entre −20 e +20**, como o `coerceIn` que o
     * `FichaCombatDelegate` já faz.
     *
     * ⚠️ Um `+999` vindo de um link daria um NH que nenhuma tela do aplicativo
     * mostra — e a regra de crítico do servidor lê `nh >= 16` como decisivo em 6.
     */
    @Test
    fun `o modificador tem teto e piso`() {
        val alto = PedidoDaMesa.ler("gurpsapp://rolar?o=ataque&mod=999")!!
        assertEquals(PedidoDaMesa.MOD_MAXIMO, alto.mod)
        val baixo = PedidoDaMesa.ler("gurpsapp://rolar?o=ataque&mod=-999")!!
        assertEquals(-PedidoDaMesa.MOD_MAXIMO, baixo.mod)

        // ⚠️ O que não é número é ZERO, e não um pedido recusado: perder o
        // ataque inteiro por um campo torto é pior do que rolar sem o
        // modificador — a pessoa vê a frase e corrige à mão.
        assertEquals(0, PedidoDaMesa.ler("gurpsapp://rolar?o=ataque&mod=muito")!!.mod)
        assertEquals(0, PedidoDaMesa.ler("gurpsapp://rolar?o=ataque&mod=")!!.mod)
        assertEquals(0, PedidoDaMesa.ler("gurpsapp://rolar?o=ataque")!!.mod)
    }

    /**
     * 🟥 **Um texto de fora não pode ser comprido.**
     *
     * ⚠️ A frase vai para a tela. Um `alvo` de dez mil letras empurraria a aba
     * Rolagem inteira para fora do telefone.
     */
    @Test
    fun `texto de fora e cortado`() {
        val enorme = "x".repeat(500)
        val p = PedidoDaMesa.ler("gurpsapp://rolar?o=ataque&alvo=$enorme&onde=$enorme")!!
        assertTrue("o alvo passou de 60", (p.alvo?.length ?: 0) <= 60)
        assertTrue("o onde passou de 60", (p.onde?.length ?: 0) <= 60)

        // Espaço em branco vira nulo, e não uma frase com um buraco.
        val vazio = PedidoDaMesa.ler("gurpsapp://rolar?o=ataque&alvo=%20%20&onde=")!!
        assertNull(vazio.alvo)
        assertNull(vazio.onde)
    }

    /**
     * ⚠️ **Só o primeiro de cada chave.**
     *
     * Um link com `mod=0&mod=-99` aceitaria o último se eu não dissesse nada — e
     * um link torto não escolhe por nós.
     */
    @Test
    fun `uma chave repetida fica pelo primeiro valor`() {
        val p = PedidoDaMesa.ler("gurpsapp://rolar?o=ataque&mod=0&mod=-99")!!
        assertEquals(0, p.mod)
    }

    /** ⚠️ O que a Mesa não mandou fica nulo — e não vira texto vazio na tela. */
    @Test
    fun `os campos que faltam ficam nulos`() {
        val p = PedidoDaMesa.ler("gurpsapp://rolar?o=dano")!!
        assertNull(p.pericia)
        assertNull(p.onde)
        assertNull(p.alvo)
        assertNull(p.acao)
        assertEquals(0, p.mod)
        assertEquals("Rolar o dano", p.frase())
    }

    /** Campos que o aplicativo não conhece são ignorados, e não estouram. */
    @Test
    fun `um campo desconhecido nao atrapalha`() {
        val p = PedidoDaMesa.ler(
            "gurpsapp://rolar?o=ataque&coisaNova=1&mod=-2&outra=xyz&=vazio&sozinho")!!
        assertEquals(-2, p.mod)
        assertEquals(PedidoDaMesa.Oque.ATAQUE, p.oQue)
    }

    /**
     * 🔴 **O token da sala não viaja no link.**
     *
     * ⚠️ Um link é texto: fica no histórico do navegador e em tudo o que vê a
     * URL. O aplicativo já sabe a que mesa está ligado — foi a pessoa que a
     * configurou — e é de lá que o token sai na hora de mandar a rolagem.
     */
    @Test
    fun `o pedido nao tem campo nenhum de token`() {
        val p = PedidoDaMesa.ler(
            "gurpsapp://rolar?o=ataque&token=SEGREDO123&mesa=OUTRO")!!
        val campos = p.toString()
        assertTrue("o token entrou no pedido: $campos", !campos.contains("SEGREDO123"))
        assertTrue("a mesa entrou no pedido: $campos", !campos.contains("OUTRO"))
    }
}
