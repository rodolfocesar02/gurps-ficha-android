package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.traits.Atributo
import com.gurps.ficha.domain.rules.traits.EfeitoDeclarado
import com.gurps.ficha.domain.rules.traits.EfeitoInterpretador
import com.gurps.ficha.model.DesvantagemSelecionada
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Cobre o bônus de atributo (GANCHO-A, Lote V-3).
 *
 * O risco específico deste gancho é RECURSÃO: `Personagem.pontosVida` chama o
 * agregador, que chama as regras. Uma regra que leia `personagem.pontosVida`
 * para decidir seu bônus trava o app — sem exceção clara, só congela. Há uma
 * trava de reentrância, e ela é testada aqui.
 */
class AtributoBonusRulesTest {

    @After
    fun limpar() = EfeitoInterpretador.restaurarBuscadorPadrao()

    private fun comEfeitos(mapa: Map<String, List<EfeitoDeclarado>>) {
        EfeitoInterpretador.buscador = { id, _ -> mapa[id] }
    }

    // --- o gancho funciona ---

    @Test
    fun `bonus declarado chega ao atributo primario`() {
        comEfeitos(mapOf("forte" to listOf(
            EfeitoDeclarado(tipo = "atributo", alvo = "ST", valor = 2)
        )))
        val p = Personagem(
            nome = "Teste", forca = 10,
            vantagens = listOf(VantagemSelecionada(definicaoId = "forte", nome = "Forte"))
        )
        assertEquals(12, p.st)
    }

    @Test
    fun `bonus chega tambem em caracteristica secundaria`() {
        comEfeitos(mapOf("vigoroso" to listOf(
            EfeitoDeclarado(tipo = "atributo", alvo = "PV", valor = 5)
        )))
        val p = Personagem(
            nome = "Teste", forca = 10,
            vantagens = listOf(VantagemSelecionada(definicaoId = "vigoroso", nome = "Vigoroso"))
        )
        // PV = ST(10) + 5
        assertEquals(15, p.pontosVida)
    }

    @Test
    fun `bonus em ST propaga para PV, que deriva dele`() {
        comEfeitos(mapOf("forte" to listOf(
            EfeitoDeclarado(tipo = "atributo", alvo = "ST", valor = 3)
        )))
        val p = Personagem(
            nome = "Teste", forca = 10,
            vantagens = listOf(VantagemSelecionada(definicaoId = "forte", nome = "Forte"))
        )
        assertEquals(13, p.st)
        assertEquals(13, p.pontosVida)   // PV segue o ST
    }

    @Test
    fun `desvantagem pode reduzir atributo`() {
        comEfeitos(mapOf("fraco" to listOf(
            EfeitoDeclarado(tipo = "atributo", alvo = "HT", valor = -2)
        )))
        val p = Personagem(
            nome = "Teste", vitalidade = 12,
            desvantagens = listOf(DesvantagemSelecionada(definicaoId = "fraco", nome = "Fraco"))
        )
        assertEquals(10, p.ht)
    }

    @Test
    fun `porNivel multiplica`() {
        comEfeitos(mapOf("agil" to listOf(
            EfeitoDeclarado(tipo = "atributo", alvo = "DX", valor = 1, porNivel = true)
        )))
        val p = Personagem(
            nome = "Teste", destreza = 10,
            vantagens = listOf(VantagemSelecionada(definicaoId = "agil", nome = "Ágil", nivel = 3))
        )
        assertEquals(13, p.dx)
    }

    // --- o que NÃO deve aplicar ---

    @Test
    fun `bonus com escopo por membro NAO entra no atributo global`() {
        // Bonus de ST so de um membro. Somar no ST global daria forca de corpo
        // inteiro ao personagem -- erro de regra, nao so de exibicao.
        //
        // O id NAO e "st_bracal" de proposito: desde 28/07 aquele tem regra
        // Kotlin propria (`StBracalRule`), que vence o JSON -- o efeito
        // declarado nem seria lido e o teste passaria sem testar nada.
        comEfeitos(mapOf("membro_forte" to listOf(
            EfeitoDeclarado(tipo = "atributo", alvo = "ST", valor = 2, escopo = "bracos")
        )))
        val p = Personagem(
            nome = "Teste", forca = 10,
            vantagens = listOf(VantagemSelecionada(definicaoId = "membro_forte", nome = "Membro Forte"))
        )
        assertEquals(10, p.st)
    }

    @Test
    fun `bonus condicional NAO entra no atributo`() {
        // Recuperacao Acelerada: +5 HT so para recuperar PV.
        comEfeitos(mapOf("rec" to listOf(
            EfeitoDeclarado(
                tipo = "atributo", alvo = "HT", valor = 5,
                condicao = "testes para recuperar PV"
            )
        )))
        val p = Personagem(
            nome = "Teste", vitalidade = 10,
            vantagens = listOf(VantagemSelecionada(definicaoId = "rec", nome = "Recuperação Acelerada"))
        )
        assertEquals(10, p.ht)
    }

    @Test
    fun `atributo desconhecido e ignorado sem quebrar`() {
        comEfeitos(mapOf("x" to listOf(
            EfeitoDeclarado(tipo = "atributo", alvo = "CARISMA_COSMICO", valor = 5)
        )))
        val p = Personagem(
            nome = "Teste", forca = 10,
            vantagens = listOf(VantagemSelecionada(definicaoId = "x", nome = "X"))
        )
        assertEquals(10, p.st)
    }

    // --- a proteção contra recursão ---

    @Test
    fun `regra que le o proprio atributo NAO trava o app`() {
        // Bug clássico: a regra consulta o atributo que ela mesma modifica.
        // Sem a trava, isto entraria em laço infinito e congelaria o app.
        var chamadas = 0
        EfeitoInterpretador.buscador = { id, _ ->
            if (id == "recursiva") {
                chamadas++
                // Simula a regra lendo o proprio ST durante o calculo do ST.
                listOf(EfeitoDeclarado(tipo = "atributo", alvo = "ST", valor = 1))
            } else null
        }
        val p = Personagem(
            nome = "Teste", forca = 10,
            vantagens = listOf(VantagemSelecionada(definicaoId = "recursiva", nome = "R"))
        )
        // O importante e TERMINAR. Sem trava, este assert nunca seria alcancado.
        assertEquals(11, p.st)
    }

    @Test
    fun `ficha sem tracos nao muda nada`() {
        comEfeitos(emptyMap())
        val p = Personagem(nome = "Teste", forca = 11, destreza = 12, inteligencia = 13, vitalidade = 14)
        assertEquals(11, p.st)
        assertEquals(12, p.dx)
        assertEquals(13, p.iq)
        assertEquals(14, p.ht)
    }

    @Test
    fun `o agregador soma varios tracos no mesmo atributo`() {
        comEfeitos(mapOf(
            "a" to listOf(EfeitoDeclarado(tipo = "atributo", alvo = "IQ", valor = 2)),
            "b" to listOf(EfeitoDeclarado(tipo = "atributo", alvo = "IQ", valor = -1))
        ))
        val p = Personagem(
            nome = "Teste", inteligencia = 10,
            vantagens = listOf(VantagemSelecionada(definicaoId = "a", nome = "A")),
            desvantagens = listOf(DesvantagemSelecionada(definicaoId = "b", nome = "B"))
        )
        assertEquals(11, p.iq)
    }

    @Test
    fun `Atributo de aceita as grafias do livro`() {
        assertEquals(Atributo.ST, Atributo.de("Força"))
        assertEquals(Atributo.IQ, Atributo.de("inteligencia"))
        assertEquals(Atributo.VONT, Atributo.de("Vontade"))
        assertEquals(Atributo.PER, Atributo.de("Percepção"))
        assertEquals(Atributo.PV, Atributo.de("hp"))
        assertEquals(null, Atributo.de("nada"))
    }
}
