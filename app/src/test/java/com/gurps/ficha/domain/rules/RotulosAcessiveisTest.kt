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

    /**
     * ⚠️ **Lote ACESS-1: as telas novas entraram aqui.**
     *
     * Até 11/08 esta lista tinha três rótulos, e as telas do MB-6, MB-7 e PV-1b
     * ficaram de fora — então elas nasceram com dois defeitos que esta mesma
     * classe existe para impedir: o eco de *"vestindo/guardada"* por cima do
     * estado da caixinha, e sinal cru em três textos.
     *
     * **Rótulo escrito dentro de `@Composable` é rótulo sem rede.** Por isso eles
     * foram movidos para as regras puras — não por elegância, mas para caberem
     * nesta varredura.
     */
    private fun todosOsRotulos(): List<String> = listOf(
        StBracalRules.rotuloAcessivel(comBracais),
        DxBracalRules.rotuloAcessivel(comBracais),
        MaoInabilRules.rotuloAcessivel(comBracais)
    ) +
        // Lote MB-7 e PV-1a: as 16 regiões do corpo.
        MapaDaSilhueta.REGIOES.map { it.descricaoAcessivel } +
        // Lote MB-6: as fontes de fadiga, marcadas e desmarcadas.
        FadigaRules.FONTES.flatMap { listOf(it.descricaoAcessivel(0), it.descricaoAcessivel(3)) } +
        // Lote MB-7: o resultado do ferimento, nos casos que produzem sinal.
        listOf(
            FerimentoPorLocalRules.aplicar(10, 9, DanoTipo.CONT, LocalAtaque.BRACO)
                .descricaoAcessivel(pvNovo = 4, pvInicial = 10),
            FerimentoPorLocalRules.aplicar(10, 8, DanoTipo.CONT, LocalAtaque.CRANIO)
                .descricaoAcessivel(pvNovo = -22, pvInicial = 10),
            FerimentoPorLocalRules.aplicar(10, 5, DanoTipo.CONT, LocalAtaque.INGLE)
                .descricaoAcessivel(pvNovo = 5, pvInicial = 10)
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
            // 🔴 O eco que o Lote MB-7 trouxe de volta: a linha da armadura
            // dizia "Vestindo."/"Guardada." por cima do estado que o
            // `linhaAlternavel` já anuncia.
            assertTrue("'$r' repete o estado", !r.lowercase().contains("vestindo"))
            assertTrue("'$r' repete o estado", !r.lowercase().contains("guardada"))
            assertTrue("'$r' repete o estado", !r.lowercase().contains("selecionad"))
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
