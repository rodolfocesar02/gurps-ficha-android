package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.poderes.TalentoNaRolagem
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.Poder
import com.gurps.ficha.ui.features.rolagem.RollMappedOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **O Talento do poder chega na rolagem** — Lote POD-29, Poderes p.158.
 *
 * 🔴 A regra existia INTEIRA desde o POD-11 e o nivel do Talento desde o POD-3.
 * Os dois nunca se encontraram: quem comprava Telepatia com Talento 3 e um
 * Ataque Inato dentro dela via o ataque com o NH sem os +3.
 */
class TalentoNaRolagemTest {

    private fun fonte(caminho: String): String {
        val direto = File("src/main/java/$caminho")
        val f = if (direto.exists()) direto else File("app/src/main/java/$caminho")
        assertTrue("nao encontrei ${f.absolutePath}", f.exists())
        return f.readText(Charsets.UTF_8)
    }

    private val telepatia = Poder(
        id = "p1", nome = "Telepatia", fonte = "Psíquico",
        modificadorDePoder = -10, nivelTalento = 3
    )
    private val personagem = Personagem(poderes = listOf(telepatia))

    private fun opcao(nh: Int? = 12) = RollMappedOption(
        id = "vant_ataque_inato_Rajada",
        label = "Rajada",
        contextLabel = "Ataque Rajada",
        target = nh
    )

    // == O bonus ========================================================

    @Test
    fun `a habilidade do poder recebe o Talento no NH`() {
        val r = TalentoNaRolagem.aplicar(personagem, "p1", listOf(opcao(12)))
        assertEquals(15, r.single().target)          // 12 + Talento 3
    }

    @Test
    fun `habilidade solta nao pega carona no Talento de ninguem`() {
        assertEquals(listOf(opcao(12)), TalentoNaRolagem.aplicar(personagem, null, listOf(opcao(12))))
        assertEquals(listOf(opcao(12)), TalentoNaRolagem.aplicar(personagem, "", listOf(opcao(12))))
        // ⚠️ E um poderId que aponta para um poder que nao existe mais tambem
        // nao pode inventar bonus -- nem derrubar a ficha.
        assertEquals(listOf(opcao(12)), TalentoNaRolagem.aplicar(personagem, "sumiu", listOf(opcao(12))))
    }

    @Test
    fun `poder sem Talento nao muda o NH, mas ainda diz de onde veio`() {
        // O Antipsi do Modulo Basico nao tem Talento nenhum (MB p.256).
        val antipsi = Poder(id = "p2", nome = "Antipsi", fonte = "", nivelTalento = 0)
        val p = Personagem(poderes = listOf(antipsi))
        val r = TalentoNaRolagem.aplicar(p, "p2", listOf(opcao(12))).single()
        assertEquals(12, r.target)
        assertTrue("perdeu o poder de origem", r.label.contains("Antipsi"))
    }

    @Test
    fun `Talento negativo nao vira penalidade`() {
        // ⚠️ Talento nao existe negativo no livro; se a ficha vier com lixo, o
        // seguro e ignorar, e nao PIORAR o NH de quem nao pediu nada.
        val quebrado = Poder(id = "p3", nome = "X", nivelTalento = -2)
        val p = Personagem(poderes = listOf(quebrado))
        assertEquals(12, TalentoNaRolagem.aplicar(p, "p3", listOf(opcao(12))).single().target)
    }

    @Test
    fun `opcao sem alvo continua sem alvo`() {
        assertNull(TalentoNaRolagem.aplicar(personagem, "p1", listOf(opcao(null))).single().target)
    }

    // == O que o livro EXCLUI ===========================================

    @Test
    fun `o Talento NAO entra no dano`() {
        // "Não soma no dano nem no teste do alvo" (p.158). A exclusao ja estava
        // escrita em UsoDoPoder.TipoDeTeste desde o POD-11 -- este teste guarda
        // que a fiacao nova nao a atropelou.
        val agregador = fonte("com/gurps/ficha/ui/TabRolagem.kt")
        val iDano = agregador.indexOf("rule.getDamageOptions(p, vant)")
        assertTrue("o agregador de dano sumiu", iDano > 0)
        val trecho = agregador.substring(maxOf(0, iDano - 500), iDano + 200)
        assertFalse(
            "o Talento passou a somar no DANO, e o livro proibe",
            trecho.contains("TalentoNaRolagem")
        )
    }

    @Test
    fun `so o ATACAR recebe, e e a regra do POD-11 que decide`() {
        // ⚠️ Nao ha segunda tabela: quem decide continua sendo
        // UsoDoPoder.TipoDeTeste. Se alguem escrever a decisao de novo aqui,
        // viram duas rotas -- e o defeito mora na diferenca.
        val src = fonte("com/gurps/ficha/domain/rules/poderes/TalentoNaRolagem.kt")
        assertTrue("parou de reusar a regra do POD-11",
            src.contains("UsoDoPoder.bonusDoTalento("))
        assertTrue("nao usa o tipo ATACAR", src.contains("TipoDeTeste.ATACAR"))
    }

    // == O rotulo =======================================================

    @Test
    fun `o rotulo diz de que poder a habilidade e`() {
        // Pela p.156 a falha critica derruba o PODER INTEIRO. Sem o rotulo o
        // jogador nao tem como saber o que cai junto.
        val r = TalentoNaRolagem.aplicar(personagem, "p1", listOf(opcao())).single()
        assertEquals("Rajada (Telepatia)", r.label)
        assertTrue(r.contextLabel, r.contextLabel.contains("poder Telepatia"))
        assertTrue("nao explica o bonus", r.descricao.contains("Talento 3"))
        assertTrue("nao diz o que a fonte manda rolar",
            r.descricao.contains("Vontade"))
        assertFalse("a fala tem sinal cru", RotuloAcessivel.temSinalCru(r.descricao))
    }

    // == A fiacao =======================================================

    @Test
    fun `o agregador de ataque passa pelo Talento`() {
        val agregador = fonte("com/gurps/ficha/ui/TabRolagem.kt")
        assertTrue(
            "o ataque voltou a ignorar o Talento do poder",
            agregador.contains("TalentoNaRolagem.aplicar(")
        )
        assertTrue("nao passa o poder da habilidade", agregador.contains("poderId = vant.poderId"))
    }
}
