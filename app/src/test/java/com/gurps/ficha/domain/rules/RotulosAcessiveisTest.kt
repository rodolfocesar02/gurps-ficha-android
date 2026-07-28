package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.traits.BracalRuleBase
import com.gurps.ficha.domain.rules.traits.DxBracalRule
import com.gurps.ficha.domain.rules.traits.StBracalRule
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Convenção dos textos lidos pelo TalkBack (auditoria de 28/07/2026).
 *
 * ## As duas regras
 *
 * **1. Nada de sinal cru.** Quem ouve não vê `-4`; o leitor de tela lê o hífen
 * como travessão ou simplesmente pula. Tem que ser "menos 4".
 *
 * **2. Não repetir o estado da caixinha.** Os seletores usam
 * `Modifier.linhaAlternavel`, que dá papel de caixa de seleção — o TalkBack já
 * anuncia "marcada"/"não marcada" sozinho. Escrever "Ativado." na descrição
 * fazia o leitor dizer as duas coisas seguidas, e uma delas podia estar
 * desatualizada.
 *
 * Este teste existe porque o eco é invisível para quem não usa leitor de tela:
 * na variante visual nada muda, e o defeito só aparece com o TalkBack ligado.
 */
class RotulosAcessiveisTest {

    private val comBracais = Personagem(
        nome = "Teste",
        forca = 10,
        destreza = 10,
        vantagens = listOf(
            VantagemSelecionada(
                definicaoId = StBracalRule.ID, nome = "ST Braçal", nivel = 3,
                metadados = mapOf(BracalRuleBase.CHAVE_BRACOS to "2")
            ),
            VantagemSelecionada(
                definicaoId = DxBracalRule.ID, nome = "DX Braçal", nivel = 3,
                metadados = mapOf(BracalRuleBase.CHAVE_BRACOS to "2")
            )
        )
    )

    private fun todosOsRotulos(): List<String> = listOf(
        StBracalRules.rotuloAcessivel(comBracais),
        DxBracalRules.rotuloAcessivel(comBracais),
        MaoInabilRules.rotuloAcessivel(comBracais)
    )

    @Test
    fun `nenhum rotulo acessivel vaza sinal cru`() {
        todosOsRotulos().forEach { r ->
            assertTrue("'$r' contem sinal cru", !Regex("[+\\-]\\d").containsMatchIn(r))
        }
    }

    @Test
    fun `nenhum rotulo acessivel repete o estado da caixinha`() {
        todosOsRotulos().forEach { r ->
            assertTrue("'$r' repete o estado", !r.contains("Ativado"))
            assertTrue("'$r' repete o estado", !r.contains("Desativado"))
            assertTrue("'$r' repete o estado", !r.lowercase().contains("marcad"))
        }
    }

    @Test
    fun `todo rotulo acessivel diz o numero por extenso`() {
        val st = StBracalRules.rotuloAcessivel(comBracais)
        assertTrue(st, st.contains("mais 3"))
        val dx = DxBracalRules.rotuloAcessivel(comBracais)
        assertTrue(dx, dx.contains("mais 3"))
    }

    @Test
    fun `o rotulo da DX Bracal avisa que nao vale para combate`() {
        // A pegadinha da vantagem tem que chegar a quem ouve tambem, nao so a
        // quem le a tela.
        val dx = DxBracalRules.rotuloAcessivel(comBracais)
        assertTrue(dx, dx.contains("combate"))
    }

    @Test
    fun `os testes de marco e de resistencia dizem alvo e motivo juntos`() {
        // Se o numero e o motivo fossem elementos separados, virariam dois
        // pontos de parada na navegacao por toque -- ruido puro.
        val p = Personagem(nome = "Teste", forca = 10, vitalidade = 10)
        MarcosDeVidaRules.testesPersistentes(p, pvAtual = 0).forEach { t ->
            assertTrue(t.descricaoAcessivel, t.descricaoAcessivel.contains("Alvo ${t.alvo}"))
            assertTrue(t.descricaoAcessivel, t.descricaoAcessivel.contains(t.rotulo))
        }
        ResistenciaRules.testesDe(p).forEach { t ->
            assertTrue(t.descricaoAcessivel, t.descricaoAcessivel.contains("Alvo ${t.alvo}"))
        }
    }
}
