package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.Poder
import com.gurps.ficha.model.TipoCusto
import com.gurps.ficha.model.VantagemSelecionada
import com.gurps.ficha.ui.contadorDe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **A habilidade mora dentro do poder** — Lote POD-28.
 *
 * 🔴 Achado pelo usuario na tela: *"ele esta adicionando tbm na lista de
 * vantagens do personagem! ela deveria ficar apenas associado ao poder, nao
 * entrando na lista de vantagens, sendo que e uma habilidade do poder"*.
 *
 * ⚠️ Isto **desfaz uma decisao que eu tinha escrito no POD-14**, onde eu
 * argumentei que a habilidade "tem de continuar aparecendo na aba Tracos". O
 * argumento estava errado na conclusao: o que precisa continuar e ela **contar
 * pontos**, nao ela aparecer duas vezes. Numa ficha de GURPS escrita a mao o
 * poder e um cabecalho e as habilidades vem indentadas embaixo dele.
 */
class HabilidadeDentroDoPoderTest {

    private fun fonte(caminho: String): String {
        val direto = File("src/main/java/$caminho")
        val f = if (direto.exists()) direto else File("app/src/main/java/$caminho")
        assertTrue("nao encontrei ${f.absolutePath}", f.exists())
        return f.readText(Charsets.UTF_8)
    }

    private fun vant(nome: String, custo: Int, poder: String?) = VantagemSelecionada(
        definicaoId = nome, nome = nome, custoEscolhido = custo,
        tipoCusto = TipoCusto.FIXO, poderId = poder
    )

    // == O que NAO pode mudar: os pontos ================================

    @Test
    fun `a habilidade do poder continua contando no total da ficha`() {
        // 🔴 O risco desta mudanca era a ficha passar a MENTIR: some da tela e
        // some da conta. Ela foi comprada com os pontos do personagem.
        val p = Personagem(
            pontosIniciais = 150,
            poderes = listOf(Poder(id = "p1", nome = "Água")),
            vantagens = listOf(vant("Abençoado", 9, "p1"), vant("Reputação", 5, null))
        )
        assertEquals(14, p.pontosVantagens)
        assertEquals(14, p.pontosGastos)
    }

    @Test
    fun `esconder da lista nao mexe no modelo`() {
        // A vantagem continua em `vantagens` -- so a EXIBICAO muda. Se ela
        // saisse do modelo, todo o resto (modificadores, alternativas, custo)
        // deixaria de funcionar junto.
        val p = Personagem(
            poderes = listOf(Poder(id = "p1", nome = "Água")),
            vantagens = listOf(vant("Abençoado", 9, "p1"))
        )
        assertEquals(1, p.vantagens.size)
        assertEquals("p1", p.vantagens.first().poderId)
    }

    // == A fiacao da tela ================================================

    @Test
    fun `a aba filtra as habilidades da lista de Vantagens`() {
        val aba = fonte("com/gurps/ficha/ui/TabTracos.kt")
        assertTrue(
            "a lista de Vantagens voltou a mostrar habilidade de poder",
            aba.contains("p.vantagens.withIndex().filter { it.value.poderId == null }")
        )
        assertTrue(
            "a lista de Desvantagens voltou a mostrar habilidade de poder",
            aba.contains("p.desvantagens.withIndex().filter { it.value.poderId == null }")
        )
    }

    @Test
    fun `o indice do lixeiro e o da lista COMPLETA`() {
        // 🔴 A armadilha desta correcao: filtrar primeiro e usar a posicao da
        // lista filtrada apagaria a vantagem ERRADA. Por isso `withIndex()`
        // vem ANTES do `filter` -- o indice tem de ser o da lista inteira.
        val aba = fonte("com/gurps/ficha/ui/TabTracos.kt")
        assertFalse(
            "o filtro voltou a vir antes do withIndex",
            aba.contains("filter { it.poderId == null }.withIndex()")
        )
        // E o forEachIndexed sobre a lista crua nao pode voltar.
        assertFalse(
            "a lista crua voltou a ser percorrida com forEachIndexed",
            aba.contains("p.vantagens.forEachIndexed")
        )
    }

    @Test
    fun `a habilidade escondida aparece dentro do poder`() {
        // ⚠️ Tirar da lista SEM mostrar aqui deixaria a vantagem comprada sem
        // nenhum caminho ate ela -- nem editar, nem apagar.
        val secao = fonte("com/gurps/ficha/ui/features/traits/SecaoDePoderes.kt")
        assertTrue("as habilidades nao aparecem no poder",
            secao.contains("HabilidadesDoPoderNaAba("))
        assertTrue("nao da para editar a habilidade pelo poder",
            secao.contains("onEditarVantagem(h.indice)"))
        assertTrue("nao da para apagar a habilidade pelo poder",
            secao.contains("viewModel.removerVantagem(h.indice)"))
        // E a desvantagem exigida tem os mesmos dois caminhos.
        assertTrue(secao.contains("onEditarDesvantagem(h.indice)"))
        assertTrue(secao.contains("viewModel.removerDesvantagem(h.indice)"))
    }

    // == O contador que concordava errado ================================

    @Test
    fun `o contador concorda com o genero das doze palavras do app`() {
        // 🔴 "1 habilidade encontrado" -- na imagem que o usuario mandou.
        // Tres dos doze rotulos saiam errados: habilidade, vantagem e
        // desvantagem sao femininos e nenhum termina em -a.
        val esperado = listOf(
            Triple("arma", "armas", "a"),
            Triple("armadura", "armaduras", "a"),
            Triple("desvantagem", "desvantagens", "a"),
            Triple("escudo", "escudos", "o"),
            Triple("habilidade", "habilidades", "a"),
            Triple("mágica", "mágicas", "a"),
            Triple("perícia", "perícias", "a"),
            Triple("poder", "poderes", "o"),
            Triple("traço livre", "traços livres", "o"),
            Triple("técnica", "técnicas", "a"),
            Triple("vantagem ligada", "vantagens ligadas", "a"),
            Triple("vantagem", "vantagens", "a")
        )
        esperado.forEach { (singular, plural, genero) ->
            assertEquals(
                "concordancia errada no singular de '$singular'",
                "1 $singular encontrad$genero",
                contadorDe(1, singular, plural)
            )
            assertEquals(
                "concordancia errada no plural de '$plural'",
                "4 $plural encontrad${genero}s",
                contadorDe(4, singular, plural)
            )
        }
    }

    @Test
    fun `zero usa o plural`() {
        assertEquals("0 habilidades encontradas", contadorDe(0, "habilidade", "habilidades"))
    }
}
