package com.gurps.ficha.domain.rules.traits

import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import com.gurps.ficha.ui.features.rolagem.somaDosMarcados
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Cobre o bônus condicional (Lote V-5).
 *
 * É a maior fatia dos bônus do GURPS: *Rosto Sincero* dá +1 em Dissimulação
 * **"para parecer inocente"**; *Camaleão* dá +2 em Furtividade **"quando não
 * quer ser visto e está imóvel"**.
 *
 * Somar isso no NH da ficha seria mentir sobre o personagem — ele não tem o
 * bônus sempre. Então o valor **fica fora** do NH e vira caixa marcável na hora
 * de rolar. Estes testes travam essa separação nos dois sentidos: o condicional
 * não pode vazar para o NH, e precisa estar disponível para a rolagem.
 */
class BonusCondicionalTest {

    @After
    fun limpar() = EfeitoInterpretador.restaurarBuscadorPadrao()

    private fun comEfeitos(mapa: Map<String, List<EfeitoDeclarado>>) {
        EfeitoInterpretador.buscador = { id, _ -> mapa[id] }
    }

    private fun fichaRostoSincero(): Personagem {
        comEfeitos(mapOf("rosto" to listOf(
            EfeitoDeclarado(
                tipo = "pericia", alvo = "Dissimulação", valor = 1,
                condicao = "para parecer inocente"
            )
        )))
        return Personagem(
            nome = "Teste",
            vantagens = listOf(VantagemSelecionada(definicaoId = "rosto", nome = "Rosto Sincero"))
        )
    }

    // --- a separação ---

    @Test
    fun `condicional NAO entra no NH base`() {
        val p = fichaRostoSincero()
        assertEquals(0, TraitRuleRegistry.getSkillBonus(p, "Dissimulação"))
    }

    @Test
    fun `mas fica disponivel para a rolagem`() {
        val p = fichaRostoSincero()
        val bonus = TraitRuleRegistry.getBonusCondicionais(p, "Dissimulação")
        assertEquals(1, bonus.size)
        assertEquals(1, bonus.first().valor)
        assertEquals("Rosto Sincero", bonus.first().nomeDoTraco)
    }

    @Test
    fun `condicional nao aparece para outra pericia`() {
        val p = fichaRostoSincero()
        assertTrue(TraitRuleRegistry.getBonusCondicionais(p, "Escalada").isEmpty())
    }

    @Test
    fun `bonus incondicional NAO aparece como opcao - ja esta no NH`() {
        // Oferecer de novo faria o jogador somar duas vezes.
        comEfeitos(mapOf("pendulear" to listOf(
            EfeitoDeclarado(tipo = "pericia", alvo = "Escalada", valor = 2)
        )))
        val p = Personagem(
            nome = "Teste",
            vantagens = listOf(VantagemSelecionada(definicaoId = "pendulear", nome = "Pendulear"))
        )
        assertEquals(2, TraitRuleRegistry.getSkillBonus(p, "Escalada"))
        assertTrue(TraitRuleRegistry.getBonusCondicionais(p, "Escalada").isEmpty())
    }

    // --- múltiplas condições ---

    @Test
    fun `duas condicoes do mesmo traco viram duas opcoes`() {
        // Camaleao: +2 imovel OU +1 em movimento. Sao alternativas -- o jogador
        // marca a que vale, nunca as duas.
        comEfeitos(mapOf("camaleao" to listOf(
            EfeitoDeclarado(tipo = "pericia", alvo = "Furtividade", valor = 2,
                porNivel = true, condicao = "imóvel"),
            EfeitoDeclarado(tipo = "pericia", alvo = "Furtividade", valor = 1,
                porNivel = true, condicao = "em movimento")
        )))
        val p = Personagem(
            nome = "Teste",
            vantagens = listOf(VantagemSelecionada(definicaoId = "camaleao", nome = "Camaleão", nivel = 2))
        )
        val bonus = TraitRuleRegistry.getBonusCondicionais(p, "Furtividade")
        assertEquals(2, bonus.size)
        // porNivel vale tambem no condicional: nivel 2 -> +4 e +2.
        assertEquals(setOf(4, 2), bonus.map { it.valor }.toSet())
    }

    @Test
    fun `efeito com escopo por membro nao vira opcao de rolagem`() {
        // Escopo e problema diferente de condicao: nao basta o jogador dizer
        // "vale agora", o calculo precisaria saber qual membro age.
        comEfeitos(mapOf("st_bracal" to listOf(
            EfeitoDeclarado(tipo = "atributo", alvo = "ST", valor = 2,
                escopo = "bracos", condicao = "usando os braços")
        )))
        val p = Personagem(
            nome = "Teste",
            vantagens = listOf(VantagemSelecionada(definicaoId = "st_bracal", nome = "ST Braçal"))
        )
        assertTrue(TraitRuleRegistry.getBonusCondicionais(p, "ST").isEmpty())
    }

    // --- o que vai para a rolagem ---

    @Test
    fun `soma so o que esta marcado`() {
        val bonus = listOf(
            BonusCondicional("A", "Furtividade", 2, "imóvel"),
            BonusCondicional("B", "Furtividade", 1, "em movimento"),
            BonusCondicional("C", "Furtividade", 5, "invisível")
        )
        assertEquals(0, somaDosMarcados(bonus, emptySet()))
        assertEquals(2, somaDosMarcados(bonus, setOf(0)))
        assertEquals(7, somaDosMarcados(bonus, setOf(0, 2)))
        assertEquals(8, somaDosMarcados(bonus, setOf(0, 1, 2)))
    }

    @Test
    fun `indice invalido nao quebra a soma`() {
        // Defesa contra estado velho: a lista muda quando o jogador edita a
        // ficha com o dialogo aberto.
        val bonus = listOf(BonusCondicional("A", "Furtividade", 2, "imóvel"))
        assertEquals(2, somaDosMarcados(bonus, setOf(0, 99)))
    }

    @Test
    fun `o rotulo mostra o traco, o valor e a condicao`() {
        assertEquals(
            "Rosto Sincero +1 — para parecer inocente",
            BonusCondicional("Rosto Sincero", "Dissimulação", 1, "para parecer inocente").rotulo
        )
    }

    @Test
    fun `condicional negativo tambem funciona`() {
        // Desvantagem condicional: penalidade que so vale em certa situacao.
        assertEquals(
            "Fobia -4 — na presença do objeto do medo",
            BonusCondicional("Fobia", "Vontade", -4, "na presença do objeto do medo").rotulo
        )
    }
}
