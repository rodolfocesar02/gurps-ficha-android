package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.model.ModeloRacial
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.TipoEquipamento
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Todos os deslocamentos (Lote DESL-2, MB p.17-19, p.352 e p.395).
 */
class DeslocamentosRulesTest {

    private fun vant(id: String, nivel: Int = 1) =
        VantagemSelecionada(definicaoId = id, nome = id, nivel = nivel)

    /** DX 10 + HT 10 = Velocidade 5,00 → Deslocamento Básico 5, Base de Carga 10 kg. */
    private fun p(vararg v: VantagemSelecionada, pesoKg: Float = 0f) = Personagem(
        nome = "T", forca = 10, destreza = 10, vitalidade = 10,
        vantagens = v.toList(),
        equipamentos = if (pesoKg > 0f) listOf(
            Equipamento(nome = "Carga", tipo = TipoEquipamento.GERAL, peso = pesoKg, quantidade = 1)
        ) else emptyList()
    )

    // --- carga ---

    @Test
    fun `os cinco fatores de carga do livro`() {
        // MB p.17: Nenhuma cheio, Leve x0,8, Media x0,6, Pesada x0,4, Muito
        // Pesada x0,2. Com Deslocamento 5.
        assertEquals(5, DeslocamentosRules.deslocamentoComCarga(5, DeslocamentosRules.NivelCarga.NENHUMA))
        assertEquals(4, DeslocamentosRules.deslocamentoComCarga(5, DeslocamentosRules.NivelCarga.LEVE))
        assertEquals(3, DeslocamentosRules.deslocamentoComCarga(5, DeslocamentosRules.NivelCarga.MEDIA))
        assertEquals(2, DeslocamentosRules.deslocamentoComCarga(5, DeslocamentosRules.NivelCarga.PESADA))
        assertEquals(1, DeslocamentosRules.deslocamentoComCarga(5, DeslocamentosRules.NivelCarga.MUITO_PESADA))
    }

    @Test
    fun `⚠️ a carga nunca reduz o Deslocamento abaixo de 1`() {
        // "A Carga nunca reduz o Deslocamento ou a Esquiva a um valor inferior a
        // 1." Sem o piso, Deslocamento 4 com carga muito pesada daria 0 -- e o
        // personagem ficaria imovel por arredondamento.
        assertEquals(1, DeslocamentosRules.deslocamentoComCarga(4, DeslocamentosRules.NivelCarga.MUITO_PESADA))
        assertEquals(1, DeslocamentosRules.deslocamentoComCarga(1, DeslocamentosRules.NivelCarga.MUITO_PESADA))
    }

    @Test
    fun `⚠️ a fracao e DESCARTADA, nao arredondada`() {
        // Deslocamento 7 com carga Leve: 7 x 0,8 = 5,6 -> 5. Arredondar daria 6.
        assertEquals(5, DeslocamentosRules.deslocamentoComCarga(7, DeslocamentosRules.NivelCarga.LEVE))
        // 7 x 0,6 = 4,2 -> 4
        assertEquals(4, DeslocamentosRules.deslocamentoComCarga(7, DeslocamentosRules.NivelCarga.MEDIA))
    }

    @Test
    fun `a Esquiva de cada nivel de carga esta na tabela`() {
        // -1 por nivel, do livro. Fica na tabela porque o jogador precisa ver o
        // custo TOTAL de estar carregado, nao so o do Deslocamento.
        assertEquals(0, DeslocamentosRules.NivelCarga.NENHUMA.esquiva)
        assertEquals(-4, DeslocamentosRules.NivelCarga.MUITO_PESADA.esquiva)
    }

    @Test
    fun `o numero da ficha ja vem descontado pela carga que ele carrega`() {
        // Base de Carga 10 kg. 25 kg = entre 2x e 3x -> carga Media (2) -> x0,6.
        val pj = p(pesoKg = 25f)
        assertEquals(2, pj.nivelCarga)
        assertEquals(3, DeslocamentosRules.deslocamentoAtual(pj))
        // Sem peso, e o Deslocamento cheio.
        assertEquals(5, DeslocamentosRules.deslocamentoAtual(p()))
    }

    @Test
    fun `a tabela marca o nivel atual, e traz os cinco`() {
        val tabela = DeslocamentosRules.tabelaDeCarga(p(pesoKg = 25f))
        assertEquals("os cinco niveis, sempre", 5, tabela.size)
        assertEquals(1, tabela.count { it.ehAtual })
        assertTrue(tabela.first { it.ehAtual }.rotulo.startsWith("Média"))
    }

    // --- os outros deslocamentos ---

    @Test
    fun `disparada e 20 por cento a mais, com piso de mais 1`() {
        // MB p.395: "velocidade até 20% maior que seu Deslocamento (no mínimo,
        // Deslocamento +1)". Com 5: 5 x 1,2 = 6. Com 3: 3,6 -> 3, mas o piso
        // manda 4.
        assertEquals(6, DeslocamentosRules.disparada(5))
        assertEquals(4, DeslocamentosRules.disparada(3))
        assertEquals(2, DeslocamentosRules.disparada(1))
    }

    @Test
    fun `nadando e o Deslocamento dividido por cinco`() {
        assertEquals(1, DeslocamentosRules.deslocamentoNadando(p()))
    }

    @Test
    fun `🔴 Anfibio nada igual ao terrestre`() {
        // O app calculava SEMPRE Basico/5 e ignorava o Anfibio, que iguala os
        // dois (MB p.19). Achado ao escrever este lote.
        val pj = p(vant(DeslocamentosRules.ID_ANFIBIO))
        assertTrue(DeslocamentosRules.ehAnfibio(pj))
        assertEquals(5, DeslocamentosRules.deslocamentoNadando(pj))
    }

    @Test
    fun `⚠️ sem a vantagem Voo, o aereo e ZERO`() {
        // A linha aparece mesmo assim, e e ai que ela ensina a regra.
        assertEquals(0, DeslocamentosRules.deslocamentoVoando(p()))
        val linhaVoo = DeslocamentosRules.todos(p()).first { it.rotulo == "Voando" }
        assertEquals("0", linhaVoo.valor)
        assertTrue(linhaVoo.conta.contains("sempre zero"))
    }

    @Test
    fun `Caminhar no Ar iguala o aereo ao terrestre`() {
        val pj = p(vant(DeslocamentosRules.ID_CAMINHAR_NO_AR))
        assertTrue(DeslocamentosRules.temCaminharNoAr(pj))
        assertEquals(5, DeslocamentosRules.deslocamentoVoando(pj))
    }

    @Test
    fun `Voo usa a Velocidade Basica, nao o Deslocamento`() {
        // MB p.19 avisa entre parenteses: "(não Deslocamento Básico × 2)".
        // Velocidade 5,00 -> 10, e nao Deslocamento 5 x 2 (que daria o mesmo aqui,
        // por isso o teste usa DX 13: Velocidade 5,75 -> 11, Deslocamento 5 -> 10).
        val pj = Personagem(
            nome = "T", destreza = 13, vitalidade = 10,
            vantagens = listOf(vant(DeslocamentosEspeciais.ID_VOO))
        )
        assertEquals(5, pj.deslocamentoBasico)
        assertEquals(11, DeslocamentosRules.deslocamentoVoando(pj))
    }

    @Test
    fun `escalando sem a vantagem usa o Deslocamento cheio`() {
        val linha = DeslocamentosRules.todos(p()).first { it.rotulo == "Escalando" }
        assertEquals("5 m/s", linha.valor)
        assertTrue(linha.conta.contains("sem Super Escalada"))
    }

    @Test
    fun `a marcha de um dia e 15 vezes o Deslocamento`() {
        // MB p.352. Com Deslocamento 5: 75 km.
        val linha = DeslocamentosRules.todos(p()).first { it.rotulo == "Marcha de um dia" }
        assertEquals("75 km", linha.valor)
    }

    // --- a lista ---

    @Test
    fun `todas as linhas trazem a conta`() {
        // Numero sem conta e numero que o jogador nao consegue conferir.
        DeslocamentosRules.todos(p()).forEach {
            assertTrue("${it.rotulo} sem conta", it.conta.isNotBlank())
        }
        DeslocamentosRules.tabelaDeCarga(p()).forEach {
            assertTrue("${it.rotulo} sem conta", it.conta.isNotBlank())
        }
    }

    @Test
    fun `a lista existe inteira mesmo numa ficha sem vantagem nenhuma`() {
        // O botao nao pode abrir uma tela vazia.
        val linhas = DeslocamentosRules.todos(p())
        assertEquals(6, linhas.size)
        assertFalse(linhas.any { it.valor.isBlank() })
    }

    @Test
    fun `vantagem RACIAL de Anfibio tambem conta`() {
        val pj = Personagem(
            nome = "T", destreza = 10, vitalidade = 10,
            modeloRacial = ModeloRacial(
                nome = "Tritão",
                vantagens = listOf(vant(DeslocamentosRules.ID_ANFIBIO))
            )
        )
        assertEquals(5, DeslocamentosRules.deslocamentoNadando(pj))
    }
}
