package com.gurps.ficha.domain.rules

import java.io.File
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **A costura entre a Mesa e o aplicativo** — teste de esforço, 22/set.
 *
 * ## 🟥 O encontro nunca teve dono
 *
 * A Mesa monta o endereço (`link-do-pedido.js`); este aplicativo lê-o
 * ([PedidoDaMesa]). Cada lado tem a sua suíte verde, e **nenhum dos dois prova
 * que o encontro funciona**. Este projeto já perdeu três meses assim.
 *
 * 🔴 Então aqui não há endereço escrito à mão. O arquivo
 * `src/test/resources/links-da-mesa.json` é **gerado pelo código de verdade da
 * Mesa**, com quase duzentos casos maldosos — aspas, `&`, `=`, acentos, emoji,
 * textos compridos demais, modificadores fora da faixa — e ao lado de cada
 * endereço fica **o que a Mesa acha que está mandando**.
 *
 * ⚠️ Se um dia a Mesa mudar o jeito de montar, este teste fica vermelho aqui, do
 * lado do aplicativo — que é o lado que ninguém estaria olhando.
 *
 * ## Como refazer o arquivo
 *
 * `node scripts/gerar-links-da-mesa.js` no repositório da Mesa.
 */
class ACosturaDoLinkTest {

    private val casos: List<JSONObject> by lazy {
        val f = File("src/test/resources/links-da-mesa.json")
        assertTrue(
            "nao achei a fixture da Mesa -- ver o cabecalho deste arquivo",
            f.exists()
        )
        val raiz = JSONObject(f.readText())
        val lista = raiz.getJSONArray("casos")
        (0 until lista.length()).map { lista.getJSONObject(it) }
    }

    /** Os casos em que a Mesa conseguiu montar um endereço. */
    private val comLink by lazy {
        casos.filter { !it.isNull("link") && it.isNull("erro") }
    }

    private fun texto(o: JSONObject, campo: String): String? =
        if (o.isNull(campo)) null else o.getString(campo)

    @Test
    fun `a fixture tem casos que cheguem`() {
        assertTrue("a fixture veio vazia", casos.size >= 150)
        assertTrue("nenhum caso gerou endereco", comLink.size >= 100)
    }

    @Test
    fun `🟥 todo endereco que a Mesa monta, o aplicativo LE`() {
        // 🔴 Um `null` aqui e o defeito mais caro que esta costura tem: a pessoa
        // toca no botao, o aplicativo abre, e nao acontece nada -- porque o
        // leitor devolveu "isto nao e um pedido" e o caminho morre calado.
        val mudos = comLink.filter {
            PedidoDaMesa.ler(it.getString("link")) == null
        }
        assertTrue(
            "a Mesa montou ${mudos.size} enderecos que o aplicativo NAO le:\n" +
                mudos.take(8).joinToString("\n") {
                    "  [" + it.getString("rotulo") + "] " + it.getString("link")
                },
            mudos.isEmpty()
        )
    }

    @Test
    fun `🟥 e o que chega e o MESMO que a Mesa disse que mandou`() {
        val divergem = mutableListOf<String>()

        comLink.forEach { caso ->
            val esperado = caso.getJSONObject("esperado")
            // Um tipo que o aplicativo nao conhece nem vira endereco do lado de
            // la, entao aqui so chegam os tres que ele conhece.
            val p = PedidoDaMesa.ler(caso.getString("link")) ?: return@forEach
            val rotulo = caso.getString("rotulo")

            fun confere(campo: String, daMesa: String?, doApp: String?) {
                if (daMesa != doApp) {
                    divergem += "  [$rotulo] $campo: a Mesa mandou ${daMesa?.let { "\"$it\"" }} " +
                        "e o app leu ${doApp?.let { "\"$it\"" }}"
                }
            }

            assertEquals(
                "[$rotulo] o TIPO mudou no caminho",
                esperado.getString("oQue"), p.oQue.name.lowercase()
            )
            // 🔴 A pericia chega em minusculas de proposito (o `lowercase()` do
            // leitor), entao a comparacao tem de saber disso -- senao este teste
            // acusaria o aplicativo de um defeito que e regra.
            confere("pericia", texto(esperado, "pericia")?.lowercase(), p.pericia)
            confere("onde", texto(esperado, "onde"), p.onde)
            confere("alvo", texto(esperado, "alvo"), p.alvo)
            confere("acao", texto(esperado, "acao"), p.acao)
            if (esperado.getInt("mod") != p.mod) {
                divergem += "  [$rotulo] mod: a Mesa mandou ${esperado.getInt("mod")} " +
                    "e o app leu ${p.mod}"
            }
        }

        assertTrue(
            "${divergem.size} campos chegaram diferentes do que a Mesa mandou:\n" +
                divergem.take(15).joinToString("\n"),
            divergem.isEmpty()
        )
    }

    @Test
    fun `⚠️ o aplicativo nunca estoura, por pior que seja o endereco`() {
        // 🔴 Nem os enderecos que a Mesa NAO conseguiu montar, nem lixo puro. O
        // leitor devolve `null`; ele nao tem o direito de derrubar a tela.
        val lixo = listOf(
            "", "   ", "gurpsapp://", "gurpsapp://rolar",
            "gurpsapp://rolar?", "gurpsapp://rolar?o=", "gurpsapp://rolar?=x",
            "gurpsapp://rolar?o=ataque&mod=", "gurpsapp://rolar?o=ataque&mod=abc",
            "gurpsapp://rolar?o=ataque&alvo=%", "gurpsapp://rolar?o=ataque&alvo=%ZZ",
            "gurpsapp://rolar?o=ataque&alvo=%E0%A4%A",
            "gurpsapp://outracoisa?o=ataque", "http://mesagurps.duckdns.org",
            "gurpsapp://rolar?" + "a=1&".repeat(5000),
            "gurpsapp://rolar?o=ataque&alvo=" + "x".repeat(100000)
        ) + casos.mapNotNull { if (it.isNull("link")) null else it.getString("link") }

        lixo.forEach { cru ->
            // Não há asserção sobre o resultado: o que se prova é que ele VOLTA.
            PedidoDaMesa.ler(cru)
        }
        assertNotNull("chegou ao fim sem estourar", lixo)
    }
}
