package com.gurps.ficha.domain.rules

import com.google.gson.JsonParser
import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.TipoEquipamento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **Qualidade das Armas** — MB p.275-276. Lote EQP-11.
 */
class QualidadeDaArmaTest {

    // ── O bônus só vale para lâmina ────────────────────────────────────

    @Test
    fun `superior da mais um so em corte e perfuracao`() {
        // "Uma LÂMINA (arma de corte ou perfuração) (…) recebe um bônus de +1".
        assertEquals(1, QualidadeDaArma.bonusDeDano(QualidadeDaArma.Nivel.SUPERIOR, DanoTipo.CORT))
        assertEquals(1, QualidadeDaArma.bonusDeDano(QualidadeDaArma.Nivel.SUPERIOR, DanoTipo.PERF))
    }

    @Test
    fun `maca superior nao ganha dano`() {
        // 🔴 O erro fácil: dar o +1 a tudo daria a um porrete caro o mesmo ganho
        // de uma katana. Ela ganha só o -1 no teste de quebra.
        assertEquals(0, QualidadeDaArma.bonusDeDano(QualidadeDaArma.Nivel.SUPERIOR, DanoTipo.CONT))
        assertEquals(-1, QualidadeDaArma.Nivel.SUPERIOR.modificadorDeQuebra)
    }

    @Test
    fun `perfurante nao e lamina`() {
        // Bala e dardo não são lâmina — o livro fala de corte e perfuração.
        listOf(DanoTipo.PI_MENOS, DanoTipo.PI, DanoTipo.PI_MAIS, DanoTipo.PI_MAIS_MAIS).forEach {
            assertEquals("$it ganhou bônus", 0, QualidadeDaArma.bonusDeDano(QualidadeDaArma.Nivel.SUPERIOR, it))
        }
    }

    @Test
    fun `barata e boa nao mexem no dano`() {
        DanoTipo.entries.forEach { tipo ->
            assertEquals(0, QualidadeDaArma.bonusDeDano(QualidadeDaArma.Nivel.BARATA, tipo))
            assertEquals(0, QualidadeDaArma.bonusDeDano(QualidadeDaArma.Nivel.BOA, tipo))
        }
        assertEquals(2, QualidadeDaArma.Nivel.BARATA.modificadorDeQuebra)
        assertEquals(0, QualidadeDaArma.Nivel.BOA.modificadorDeQuebra)
    }

    // ── A altíssima é só de espada ─────────────────────────────────────

    @Test
    fun `altissima so vale para espada e esgrima`() {
        // "Somente as armas de esgrima e as espadas podem ser consideradas de
        // altíssima qualidade."
        assertEquals(
            2,
            QualidadeDaArma.bonusDeDano(QualidadeDaArma.Nivel.ALTISSIMA, DanoTipo.CORT, ehEspadaOuEsgrima = true)
        )
        assertEquals(
            0,
            QualidadeDaArma.bonusDeDano(QualidadeDaArma.Nivel.ALTISSIMA, DanoTipo.CORT, ehEspadaOuEsgrima = false)
        )
    }

    @Test
    fun `sem grupo cadastrado nao se assume espada`() {
        // Melhor negar um bônus do que dar um que o livro não permite.
        assertTrue(QualidadeDaArma.ehEspadaOuEsgrima("ESPADAS DE LÂMINA LARGA"))
        assertTrue(QualidadeDaArma.ehEspadaOuEsgrima("Rapieira"))
        assertTrue(QualidadeDaArma.ehEspadaOuEsgrima("Adaga de Esgrima"))
        assertEquals(false, QualidadeDaArma.ehEspadaOuEsgrima("MAÇA/MACHADO"))
        assertEquals(false, QualidadeDaArma.ehEspadaOuEsgrima(null))
        assertEquals(false, QualidadeDaArma.ehEspadaOuEsgrima("  "))
    }

    // ── Onde o bônus entra no texto do dano ────────────────────────────

    @Test
    fun `o bonus soma no acrescimo, nunca nos dados`() {
        // ⚠️ `1d+2` vira `1d+3`, e não `2d`. Trocar dados mudaria a curva inteira.
        assertEquals("1d+3 corte", QualidadeDaArma.aplicarAoDano("1d+2 corte", 1))
        assertEquals("2d+1 perf", QualidadeDaArma.aplicarAoDano("2d-1 perf", 2))
        assertEquals("1d corte", QualidadeDaArma.aplicarAoDano("1d-1 corte", 1))
        assertEquals("1d-1 corte", QualidadeDaArma.aplicarAoDano("1d-2 corte", 1))
    }

    @Test
    fun `bonus zero devolve o texto intacto`() {
        assertEquals("1d+2 corte", QualidadeDaArma.aplicarAoDano("1d+2 corte", 0))
        assertEquals("", QualidadeDaArma.aplicarAoDano("", 1))
    }

    @Test
    fun `dano que nao casa com o formato passa sem mexer`() {
        // "espec.", "—" e afins existem no catálogo. Não inventar é o certo.
        assertEquals("espec.", QualidadeDaArma.aplicarAoDano("espec.", 1))
        assertEquals("—", QualidadeDaArma.aplicarAoDano("—", 1))
    }

    // ── A leitura do tipo no texto ─────────────────────────────────────

    @Test
    fun `o tipo sai do texto do catalogo`() {
        assertEquals(DanoTipo.CORT, TipoDeDanoNoTexto.ler("GeB+2 corte"))
        assertEquals(DanoTipo.PERF, TipoDeDanoNoTexto.ler("GdP-1 perf"))
        assertEquals(DanoTipo.CONT, TipoDeDanoNoTexto.ler("GeB+1 cont"))
        assertNull(TipoDeDanoNoTexto.ler("espec."))
        assertNull(TipoDeDanoNoTexto.ler(null))
    }

    @Test
    fun `as subclasses de perfurante nao se confundem`() {
        // ⚠️ A ordem do `when` importa: "pa" casaria dentro de "pa-" e um dardo
        // de zarabatana viraria bala de rifle.
        assertEquals(DanoTipo.PI_MENOS, TipoDeDanoNoTexto.ler("4d(3) pa-"))
        assertEquals(DanoTipo.PI, TipoDeDanoNoTexto.ler("2d pa"))
        assertEquals(DanoTipo.PI_MAIS, TipoDeDanoNoTexto.ler("5d pa+"))
        assertEquals(DanoTipo.PI_MAIS_MAIS, TipoDeDanoNoTexto.ler("7d pa++"))
    }

    @Test
    fun `todo dano do catalogo tem tipo legivel, ou e excecao conhecida`() {
        // Varredura do asset de verdade: se o catálogo ganhar uma abreviação nova,
        // este teste acusa em vez de o bônus sumir em silêncio.
        val semTipo = mutableListOf<String>()
        listOf("armas_corpo_a_corpo", "armas_distancia", "armas_fogo").forEach { nome ->
            val arq = File("src/main/assets/$nome.v1.normalized.json").takeIf { it.exists() }
                ?: File("app/src/main/assets/$nome.v1.normalized.json")
            assertTrue("nao encontrei ${arq.absolutePath}", arq.exists())
            JsonParser.parseString(arq.readText(Charsets.UTF_8))
                .asJsonObject.getAsJsonArray("items").forEach { el ->
                    val o = el.asJsonObject
                    val raw = o.getAsJsonObject("dano")?.get("raw")?.asString.orEmpty()
                    if (raw.isBlank()) return@forEach
                    if (TipoDeDanoNoTexto.ler(raw) == null) {
                        semTipo.add("${o.get("nome").asString}: '$raw'")
                    }
                }
        }
        // ⚠️ ACHADO DESTE TESTE. Duas famílias não têm tipo porque o `DanoTipo`
        // do app **não as modela**:
        //
        // - `qmd` (queimadura) — laser, feixe iônico, lança-chamas, espada de
        //   energia. É um tipo de dano de verdade (MB p.43), com modificador de
        //   ferimento ×1, e o diálogo de ferimento não o oferece.
        // - `at` (atribulação) — pistola paralisante, eletrolaser. Não causa PV;
        //   exige teste de HT.
        //
        // Para a REGRA DESTE LOTE isso não muda nada: nenhuma das duas é lâmina,
        // então o bônus de qualidade seria zero de qualquer jeito. Fica anotado
        // como possibilidade a apresentar, não como defeito escondido.
        val naoModelados = listOf("at", "espec", "—", "--")
        val inesperadas = semTipo.filterNot { linha -> naoModelados.any { linha.contains(it) } }
        assertTrue("danos sem tipo legível:\n" + inesperadas.joinToString("\n"), inesperadas.isEmpty())

        // E a trava que interessa: TODA arma de lâmina do livro é reconhecida.
        assertTrue(
            "o catálogo tem armas de corte/perfuração sem tipo legível",
            semTipo.none { it.contains("corte") || it.contains("perf") }
        )
    }

    // ── Ligado à ficha ─────────────────────────────────────────────────

    private fun espada(qualidade: QualidadeDaArma.Nivel?) = Equipamento(
        nome = "Espada Larga",
        tipo = TipoEquipamento.ARMA,
        armaGrupo = "ESPADAS DE LÂMINA LARGA",
        armaDanoRaw = "GeB+1 corte",
        armaQualidade = qualidade?.name
    )

    @Test
    fun `a espada superior bate mais forte na ficha`() {
        val heroi = Personagem(forca = 12)
        val comum = espada(null).danoCalculadoComSt(heroi)
        val boa = espada(QualidadeDaArma.Nivel.SUPERIOR).danoCalculadoComSt(heroi)
        assertNotNull(comum)
        assertNotNull(boa)
        assertTrue("a qualidade não mudou nada: $comum vs $boa", comum != boa)
        assertEquals(1, espada(QualidadeDaArma.Nivel.SUPERIOR).bonusDeQualidade())
        assertEquals(2, espada(QualidadeDaArma.Nivel.ALTISSIMA).bonusDeQualidade())
        assertEquals(0, espada(null).bonusDeQualidade())
    }

    @Test
    fun `a maca superior nao muda nada na ficha`() {
        val maca = Equipamento(
            nome = "Machado",
            tipo = TipoEquipamento.ARMA,
            armaGrupo = "MAÇA/MACHADO",
            armaDanoRaw = "GeB+2 cont",
            armaQualidade = QualidadeDaArma.Nivel.SUPERIOR.name
        )
        assertEquals(0, maca.bonusDeQualidade())
    }

    @Test
    fun `ficha antiga sem qualidade continua igual`() {
        // Regressão: o campo é aditivo, e null tem de significar "como sempre foi".
        val heroi = Personagem(forca = 11)
        val antiga = Equipamento(
            nome = "Espada Larga", tipo = TipoEquipamento.ARMA,
            armaGrupo = "ESPADAS DE LÂMINA LARGA", armaDanoRaw = "GeB+1 corte"
        )
        assertNull(antiga.qualidadeDaArma())
        assertEquals(0, antiga.bonusDeQualidade())
        assertEquals(
            antiga.danoCalculadoComSt(heroi),
            espada(QualidadeDaArma.Nivel.BOA).danoCalculadoComSt(heroi)
        )
    }
}
