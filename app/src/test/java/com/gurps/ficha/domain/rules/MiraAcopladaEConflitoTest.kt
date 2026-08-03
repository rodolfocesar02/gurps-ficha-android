package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.TipoEquipamento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Lote ARMA-5** — a mira acoplada e a briga entre a perícia e a arma.
 *
 * ## Os dois defeitos
 *
 * 1. **A mira embutida sumia.** O catálogo escreve `Prec "6+3"` e o app lia 6.
 *    O Rifle de Atirador .338 apontava com **3 pontos a menos** do que o livro
 *    dá (MB p.270).
 * 2. **A arma calava a perícia.** Com o ataque em `Armas de Fogo/NT (pistola)` e
 *    a fonte de dano numa adaga, o diálogo abria em **corpo a corpo**: sem
 *    distância, sem 1/2D, sem Máx, sem Apontar. E sem avisar.
 *
 * ⚠️ Os testes antigos passaram verdes com o defeito 2 em pé porque **todos
 * usavam pares coerentes** — perícia de arco com arco, faca com faca. A matriz
 * abaixo cobre os quatro cruzamentos, inclusive os dois que ninguém tinha
 * escrito.
 */
class MiraAcopladaEConflitoTest {

    private fun arma(
        nome: String,
        tipo: String?,
        max: Int? = null,
        prec: Int? = null,
        acessorio: Int? = null
    ) = Equipamento(
        nome = nome,
        tipo = TipoEquipamento.ARMA,
        armaTipoCombate = tipo,
        armaMaximoMetros = max,
        armaPrecisao = prec,
        armaPrecisaoAcessorio = acessorio
    )

    private val pistola = arma("Pistola Auto., 9 mm", "armas_de_fogo", max = 1850, prec = 2)
    private val rifle = arma("Rifle de Atirador, .338", "armas_de_fogo", max = 5100, prec = 6, acessorio = 3)
    private val adaga = arma("Adaga", "corpo_a_corpo")
    private val arco = arma("Arco Longo", "distancia", prec = 3)

    private val PERICIA_PISTOLA = "armas_de_fogo_nt_pistola"
    private val PERICIA_FACA = "faca"

    private fun semNada() = Personagem(nome = "T")

    // ==================================================================
    // 1. 🔴 A mira acoplada
    // ==================================================================

    @Test
    fun `🔴 a mira acoplada soma no Apontar`() {
        // Rifle de Atirador: Prec 6, mira +3. Um turno, sem firmar.
        // Sem a mira: 6. Com a mira: 9.
        assertEquals(6, ApontarRules.bonusDePontaria(6, turnos = 1, armaFirmada = false))
        assertEquals(9, ApontarRules.bonusDePontaria(6, turnos = 1, armaFirmada = false, miraAcoplada = 3))
    }

    @Test
    fun `sem apontar, a mira nao vale nada`() {
        // MB p.270: a Prec (e o que vem colado nela) só entra se você Apontar.
        assertEquals(0, ApontarRules.bonusDePontaria(6, turnos = 0, armaFirmada = true, miraAcoplada = 3))
    }

    @Test
    fun `⚠️ a mira entra COMO EXTRA, sem afrouxar o teto do livro`() {
        // MB p.373: o teto é o dobro do "parâmetro Prec da arma". A mira é um
        // dos "demais bônus de pontaria" — se ela entrasse ANTES de dobrar, o
        // teto do rifle viraria 18 em vez de 12, e o livro não diz isso.
        //
        // ACI 6,8 mm: Prec 4, mira +2. Três segundos e firmada:
        // bruto 4+2+1+2 = 9, teto 4×2 = 8.
        assertEquals(8, ApontarRules.bonusDePontaria(4, turnos = 3, armaFirmada = true, miraAcoplada = 2))
        // E o rifle .338 (Prec 6, mira +3) fecha exatamente no teto de 12.
        assertEquals(12, ApontarRules.bonusDePontaria(6, turnos = 3, armaFirmada = true, miraAcoplada = 3))
    }

    @Test
    fun `o rotulo mostra a mira como parcela propria`() {
        val r = ApontarRules.rotuloApontar(
            semNada(), 6, 0, turnos = 1, armaFirmada = false, miraAcoplada = 3
        )
        assertTrue(r, r.contains("Precisão +6"))
        assertTrue(r, r.contains("mira +3"))
    }

    @Test
    fun `⚠️ arma sem mira nao ganha a parcela nem por acidente`() {
        val r = ApontarRules.rotuloApontar(semNada(), 2, 0, turnos = 1, armaFirmada = false)
        assertTrue(r, !r.contains("mira"))
        assertEquals(2, ApontarRules.bonusDePontaria(2, turnos = 1, armaFirmada = false))
    }

    @Test
    fun `a mira chega ao dialogo pelo Alcance`() {
        // É o `precisaoAcessorio` nulo que faz a caixinha NÃO aparecer.
        val comMira = AlcanceDoAtaque.alcanceDe(rifle, st = 11)
        assertEquals(6, comMira.precisao)
        assertEquals(3, comMira.precisaoAcessorio)

        val semMira = AlcanceDoAtaque.alcanceDe(pistola, st = 11)
        assertEquals(2, semMira.precisao)
        assertNull(semMira.precisaoAcessorio)
    }

    @Test
    fun `mira acoplada nunca PIORA o tiro`() {
        // Varredura: a mira só soma, nunca subtrai.
        listOf(null, 0, 1, 2, 4, 6, 12).forEach { prec ->
            (0..3).forEach { turnos ->
                listOf(false, true).forEach { firmada ->
                    (0..3).forEach { mira ->
                        val com = ApontarRules.bonusDePontaria(prec, turnos, firmada, mira)
                        val sem = ApontarRules.bonusDePontaria(prec, turnos, firmada, 0)
                        assertTrue(
                            "prec=$prec turnos=$turnos firmada=$firmada mira=$mira",
                            com >= sem
                        )
                    }
                }
            }
        }
    }

    // ==================================================================
    // 2. 🔴 A matriz perícia × arma
    // ==================================================================

    @Test
    fun `🔴 pericia de pistola com adaga na fonte de dano AINDA e a distancia`() {
        // O defeito exato do print de 03/08. Antes: false, e o diálogo abria
        // com Golpe Rápido.
        assertTrue(AlcanceDoAtaque.ehADistancia(adaga, PERICIA_PISTOLA))
    }

    @Test
    fun `🔴 e a divergencia vira aviso, em vez de escolha calada`() {
        val aviso = AlcanceDoAtaque.conflito(adaga, PERICIA_PISTOLA)
        assertNotNull(aviso)
        assertTrue(aviso!!, aviso.contains("Adaga"))
        assertTrue(aviso, aviso.contains("corpo a corpo"))
    }

    @Test
    fun `a matriz inteira, inclusive os pares que ninguem tinha escrito`() {
        // arma de longe + perícia de longe → longe, sem aviso
        assertTrue(AlcanceDoAtaque.ehADistancia(pistola, PERICIA_PISTOLA))
        assertNull(AlcanceDoAtaque.conflito(pistola, PERICIA_PISTOLA))

        // arma de perto + perícia de perto → perto, sem aviso
        assertTrue(!AlcanceDoAtaque.ehADistancia(adaga, PERICIA_FACA))
        assertNull(AlcanceDoAtaque.conflito(adaga, PERICIA_FACA))

        // ⚠️ arma de longe + perícia de perto → longe (a faca de arremesso
        // empunhada com a perícia Faca; era esse o caso que a regra antiga
        // existia para resolver, e ele continua funcionando)
        assertTrue(AlcanceDoAtaque.ehADistancia(arco, PERICIA_FACA))
        assertNotNull(AlcanceDoAtaque.conflito(arco, PERICIA_FACA))

        // 🔴 arma de perto + perícia de longe → longe (o caso quebrado)
        assertTrue(AlcanceDoAtaque.ehADistancia(adaga, PERICIA_PISTOLA))
    }

    @Test
    fun `sem arma escolhida, quem responde e a pericia`() {
        // A fonte de dano fica em "Dano ST" na maioria das fichas.
        assertTrue(AlcanceDoAtaque.ehADistancia(null, PERICIA_PISTOLA))
        assertTrue(!AlcanceDoAtaque.ehADistancia(null, PERICIA_FACA))
        assertNull(AlcanceDoAtaque.conflito(null, PERICIA_PISTOLA))
    }

    @Test
    fun `⚠️ ficha antiga sem tipoCombate nao inventa conflito`() {
        // Sem o campo, o app não sabe o que a arma é — e acusar divergência
        // seria acusar sem prova.
        val antiga = arma("Espada de antes do Lote 371", tipo = null)
        assertNull(AlcanceDoAtaque.conflito(antiga, PERICIA_PISTOLA))
        // E continua caindo na perícia para decidir.
        assertTrue(AlcanceDoAtaque.ehADistancia(antiga, PERICIA_PISTOLA))
    }

    // ==================================================================
    // 3. 🔴 Qual arma está atirando
    // ==================================================================

    @Test
    fun `🔴 a adaga escolhida no dano NAO vira a arma do tiro`() {
        // Antes, a adaga selecionada virava "a arma" e levava Precisão nula e
        // alcance vazio para o diálogo — com a pistola na mesma ficha.
        val mochila = listOf(adaga, pistola)
        val achada = AlcanceDoAtaque.armaDoAtaque(mochila, armaSelecionada = adaga, periciaId = PERICIA_PISTOLA)
        assertEquals(pistola.nome, achada?.nome)
    }

    @Test
    fun `a arma de longe escolhida continua mandando`() {
        val mochila = listOf(arco, pistola)
        val achada = AlcanceDoAtaque.armaDoAtaque(mochila, armaSelecionada = arco, periciaId = PERICIA_PISTOLA)
        assertEquals(arco.nome, achada?.nome)
    }

    @Test
    fun `sem nenhuma arma de longe, nao se inventa uma`() {
        val achada = AlcanceDoAtaque.armaDoAtaque(listOf(adaga), armaSelecionada = adaga, periciaId = PERICIA_PISTOLA)
        assertNull(achada)
    }
}
