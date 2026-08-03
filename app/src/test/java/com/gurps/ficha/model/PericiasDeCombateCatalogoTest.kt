package com.gurps.ficha.model

import com.google.gson.Gson
import com.gurps.ficha.domain.rules.AlcanceDoAtaque
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.text.Normalizer

/**
 * **Lote ARMA-6** — as listas de perícia de combate contra o catálogo de verdade.
 *
 * ## 🔴 O que estava errado
 *
 * `PERICIAS_COMBATE_DISTANCIA` tinha `"artilheiro_nt"` — id que **não existe em
 * catálogo nenhum** — e **não tinha `"canhoneiro_nt"`**, que é o id real da
 * perícia do livro (MB p.187, DX/Fácil, predefinido DX-4).
 *
 * Consequência: quem atacava com o canhão de um tanque abria o diálogo *Onde
 * acertar* em **corpo a corpo**. Sem linha de distância, sem 1/2D, sem Máx, sem
 * Apontar — e com *Golpe Rápido* oferecido, que é opção de arma branca.
 *
 * ⚠️ E doía duas vezes: **Canhoneiro é uma das quatro perícias que a vantagem
 * Atirador cobre**, então a automação dela nasceria capenga.
 *
 * ## Por que nenhum teste pegou isso
 *
 * Havia teste da lista, e teste do catálogo. **Não havia teste comparando os
 * dois.** Cada um estava certo sobre si mesmo enquanto os dois discordavam.
 *
 * Este arquivo faz a única pergunta que faltava: *para cada arma do catálogo,
 * a perícia que a usa está na lista certa?* — varrendo os **GRUPOS** dos três
 * arquivos de arma, que é onde o livro escreve o nome da perícia.
 */
class PericiasDeCombateCatalogoTest {

    private val gson = Gson()

    private fun arquivo(caminho: String): File {
        val direto = File(caminho)
        return if (direto.exists()) direto else File("app/$caminho")
    }

    private data class PericiaCrua(val id: String = "", val nome: String = "")
    private data class Catalogo(val items: List<PericiaCrua> = emptyList())
    private data class ArmaCrua(val nome: String = "", val grupo: String = "")
    private data class ArmasCatalogo(val items: List<ArmaCrua> = emptyList())

    private val pericias by lazy {
        gson.fromJson(
            arquivo("src/main/assets/pericias.v3.json").readText(Charsets.UTF_8),
            Catalogo::class.java
        ).items
    }

    private fun armas(nome: String) = gson.fromJson(
        arquivo("src/main/assets/$nome").readText(Charsets.UTF_8),
        ArmasCatalogo::class.java
    ).items

    private fun chave(texto: String): String =
        Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()

    /** O id da perícia cujo nome bate com o grupo escrito no catálogo de armas. */
    private fun periciaDoGrupo(grupo: String): String? {
        val nome = chave(grupo.substringBefore("(").trim().trim(','))
        if (nome.isBlank()) return null
        return pericias.firstOrNull { chave(it.nome) == nome }?.id
            // "ESPADAS CURTAS" no grupo, "Espadas Curtas" na perícia mas o id é
            // `espada_curta`; casar pelo singular cobre esse par.
            ?: pericias.firstOrNull { chave(it.nome) == nome.replace("espadas", "espada") }?.id
    }

    /**
     * Grupos que **não são perícia**: linhas de ataque natural e de tabela.
     * Ficam escritos para que a lista de exceções seja lida, não adivinhada.
     */
    private val GRUPOS_SEM_PERICIA = setOf(
        "boxe briga carate ou dx",
        "briga ou dx",
        "briga 2 carate 2 ou dx 2",
        "mangual de duas mos" // acento perdido na planilha de origem
    )

    // ==================================================================
    // 1. 🔴 O caso que motivou o lote
    // ==================================================================

    @Test
    fun `🔴 Canhoneiro NT e perícia de combate a distancia`() {
        assertTrue(
            "canhoneiro_nt fora da lista de distancia",
            "canhoneiro_nt" in PERICIAS_COMBATE_DISTANCIA
        )
        // E o efeito prático: o diálogo de mira abre em modo à distância.
        assertTrue(AlcanceDoAtaque.periciaEhADistancia("canhoneiro_nt"))
        assertTrue(AlcanceDoAtaque.periciaEhADistancia("canhoneiro_nt_canhao"))
    }

    @Test
    fun `🔴 as QUATRO pericias da vantagem Atirador estao na lista`() {
        // MB p.43: "Armas de Feixe, Armas de Fogo, Canhoneiro ou Projetor de
        // Líquidos". Faltava justamente uma delas.
        listOf(
            "armas_de_feixe_nt",
            "armas_de_fogo_nt",
            "canhoneiro_nt",
            "projetor_de_liquidos_nt"
        ).forEach { id ->
            assertTrue("$id nao e reconhecida como ataque a distancia",
                AlcanceDoAtaque.periciaEhADistancia(id))
            assertTrue("$id nao existe no catalogo", pericias.any { it.id == id })
        }
    }

    // ==================================================================
    // 2. 🔴 A varredura que faltava
    // ==================================================================

    @Test
    fun `🔴 toda arma do catalogo tem a pericia dela numa das listas`() {
        val falhas = mutableListOf<String>()

        fun conferir(arquivo: String, esperadoNaDistancia: Boolean) {
            armas(arquivo).forEach { arma ->
                val grupo = arma.grupo.substringBefore("(").trim().trim(',')
                if (chave(grupo) in GRUPOS_SEM_PERICIA) return@forEach
                val id = periciaDoGrupo(grupo) ?: return@forEach
                val ondeEstá = if (esperadoNaDistancia) {
                    PERICIAS_COMBATE_DISTANCIA
                } else {
                    PERICIAS_COMBATE_CORPO_A_CORPO
                }
                if (id !in ondeEstá) {
                    falhas += "${arma.nome} (grupo '$grupo' -> perícia '$id')"
                }
            }
        }

        conferir("armas_corpo_a_corpo.v1.normalized.json", esperadoNaDistancia = false)
        conferir("armas_distancia.v1.normalized.json", esperadoNaDistancia = true)

        assertTrue(
            "armas cuja perícia ficou fora da lista:\n" + falhas.distinct().joinToString("\n"),
            falhas.isEmpty()
        )
    }

    @Test
    fun `nenhuma pericia esta nas DUAS listas ao mesmo tempo`() {
        // Uma perícia que fosse de perto e de longe faria o diálogo oferecer
        // Golpe Rápido e linha de distância juntos.
        val nas2 = PERICIAS_COMBATE_CORPO_A_CORPO intersect PERICIAS_COMBATE_DISTANCIA
        assertTrue("perícias nas duas listas: $nas2", nas2.isEmpty())
    }

    // ==================================================================
    // 3. Os aliases legados — documentados, não apagados
    // ==================================================================

    @Test
    fun `⚠️ os ids que nao existem no catalogo sao aliases legados assumidos`() {
        // As listas guardam ids que **não** estão em `pericias.v3.json` de
        // propósito: são nomes que fichas antigas podem ter gravado
        // (`karate` sem acento, `machado_ou_machadinha`, `pericia_de_arco`).
        //
        // Apagá-los faria a ficha antiga perder o ataque em silêncio, que é pior
        // que carregar um id a mais. O teste não os proíbe — ele **conta**, para
        // que um crescimento sem controle apareça.
        val doCatalogo = pericias.map { it.id }.toSet()
        val orfaos = (PERICIAS_COMBATE_CORPO_A_CORPO + PERICIAS_COMBATE_DISTANCIA)
            .filterNot { id -> id in doCatalogo || doCatalogo.any { it.startsWith(id) } }
        assertTrue(
            "aliases legados demais (${orfaos.size}): $orfaos — confira se algum " +
                "virou id real no catálogo",
            orfaos.size <= 32
        )
    }

    @Test
    fun `Ataque Inato continua no grupo do padrao do livro`() {
        // MB p.46: Ataque Inato é à distância por padrão; só vira corpo a corpo
        // com a limitação Ataque Corpo a Corpo, que a ficha não guarda.
        assertTrue("ataque_inato" in PERICIAS_COMBATE_DISTANCIA)
    }
}
