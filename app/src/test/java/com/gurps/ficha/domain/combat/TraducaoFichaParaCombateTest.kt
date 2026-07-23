package com.gurps.ficha.domain.combat

import com.gurps.ficha.model.MagiaDefinicao
import com.gurps.ficha.model.MagiaSelecionada
import com.gurps.ficha.model.Personagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Lote REFACTOR-2: a tradução ficha→motor que ANTES vivia presa no `SagaCombatController` e não
 * tinha como ser testada (o controller precisa de Android). Agora está em `TraducaoFichaParaCombate`.
 *
 * O foco: travar a regra que já causou bug (MEC-42/43 — o catálogo manda sobre a cópia da ficha) e a
 * normalização de nome que resolve fichas antigas.
 */
class TraducaoFichaParaCombateTest {

    // ── MEC-42: o catálogo manda; a ficha é só fallback ────────────────────────────────────────

    @Test
    fun `a classe vem do CATALOGO quando ele existe — nao da copia velha da ficha`() {
        // Foi o bug da Bola de Relâmpagos: a ficha dizia "Comum" e o catálogo, "Projétil". Consertar
        // o catálogo não bastava enquanto a tradução preferisse a cópia da ficha.
        val def = MagiaDefinicao(nome = "Bola de Relâmpagos", classe = "Projétil")
        val ficha = MagiaSelecionada(nome = "Bola de Relâmpagos", classe = "Comum")
        assertEquals("Projétil", TraducaoFichaParaCombate.classeDaMagia(def, ficha))
    }

    @Test
    fun `sem catalogo, cai na copia da ficha`() {
        val ficha = MagiaSelecionada(nome = "Magia Caseira", classe = "Comum")
        assertEquals("Comum", TraducaoFichaParaCombate.classeDaMagia(null, ficha))
    }

    @Test
    fun `catalogo com classe VAZIA nao suplanta a ficha`() {
        val def = MagiaDefinicao(nome = "X", classe = "")
        val ficha = MagiaSelecionada(nome = "X", classe = "Área")
        assertEquals("Área", TraducaoFichaParaCombate.classeDaMagia(def, ficha))
    }

    @Test
    fun `a energia segue a mesma regra do catalogo-manda`() {
        val def = MagiaDefinicao(nome = "X", energia = "1/I")
        val ficha = MagiaSelecionada(nome = "X", energia = "3")
        assertEquals("1/I", TraducaoFichaParaCombate.energiaDaMagia(def, ficha))
        assertEquals("3", TraducaoFichaParaCombate.energiaDaMagia(null, ficha))
    }

    // ── MEC-43: normalização de nome (fichas antigas com id vazio) ─────────────────────────────

    @Test
    fun `a chave do nome ignora acento, caixa e pontuacao`() {
        // "Bola de Relâmpagos" e "bola de relampagos" têm que casar.
        assertEquals(
            TraducaoFichaParaCombate.chaveNome("Bola de Relâmpagos"),
            TraducaoFichaParaCombate.chaveNome("bola de relampagos"))
        assertEquals("chuvadeacido", TraducaoFichaParaCombate.chaveNome("Chuva de Ácido"))
        assertEquals("maucheiro", TraducaoFichaParaCombate.chaveNome("Mau  Cheiro!"))
    }

    @Test
    fun `nomes DIFERENTES nao colidem na chave`() {
        assertTrue(TraducaoFichaParaCombate.chaveNome("Bola de Fogo") !=
            TraducaoFichaParaCombate.chaveNome("Bola de Fogo Explosiva"))
    }

    // ── Perfil e ataques: a tradução do herói ──────────────────────────────────────────────────

    @Test
    fun `o perfil do heroi sai da ficha — atributos e defesas`() {
        val p = Personagem(forca = 12, destreza = 13, inteligencia = 10, vitalidade = 11)
        val perfil = TraducaoFichaParaCombate.construirPerfilHeroi(p)
        assertEquals(11, perfil.ht)          // HT = vitalidade
        assertEquals(12, perfil.st)
        assertEquals(13, perfil.dx)
    }

    @Test
    fun `heroi sem arma sempre tem o ataque DESARMADO`() {
        val p = Personagem(forca = 10, destreza = 10, inteligencia = 10, vitalidade = 10)
        val ataques = TraducaoFichaParaCombate.construirAtaques(p)
        assertTrue("desarmado é o último recurso e nunca falta",
            ataques.any { it.desarmado })
    }

    @Test
    fun `agarrado so pode atacar DESARMADO`() {
        val p = Personagem(forca = 10, destreza = 10, inteligencia = 10, vitalidade = 10)
        val ataques = TraducaoFichaParaCombate.construirAtaques(p, agarrado = true)
        assertTrue("preso não empunha arma (MB p.371)", ataques.all { it.desarmado })
    }

    @Test
    fun `reach corpo-a-corpo vira metros — o maior numero`() {
        assertEquals(1, TraducaoFichaParaCombate.reachParaMetros("C"))
        assertEquals(1, TraducaoFichaParaCombate.reachParaMetros("1"))
        assertEquals(2, TraducaoFichaParaCombate.reachParaMetros("1,2"))
    }

    @Test
    fun `RD do heroi sem armadura e zero`() {
        val p = Personagem(forca = 10, destreza = 10, inteligencia = 10, vitalidade = 10)
        assertEquals(0, TraducaoFichaParaCombate.rdHeroi(p))
    }
}
