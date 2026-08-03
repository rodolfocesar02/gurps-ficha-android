package com.gurps.ficha.domain.rules

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Atalho para o enum caber nas linhas. `val` não aliasa classe em Kotlin. */
private typealias N = QualidadeDoEquipamento.Nivel

/**
 * **Lotes P-EQUIP, P-CULT e P-SIT** — os modificadores que vêm da **perícia**, e
 * não do traço.
 *
 * ## O que muda de lugar aqui
 *
 * Até agora todo modificador automatizado nascia num **traço** da ficha: a
 * Timidez sabe que penaliza Lábia. Estes três nascem na **perícia**: Arrombamento
 * sabe que depende de ferramenta, Punga sabe que a vítima pode estar dormindo.
 * É a primeira vez que o app lê a página da perícia, e não a da vantagem.
 *
 * ## O modo de falhar de cada um
 *
 * - **P-EQUIP**: a tabela tem **duas colunas** (tecnológica e comum), e aplicar a
 *   errada dá ao cirurgião de mãos vazias a chance do pedreiro sem colher.
 * - **P-CULT**: a vantagem **apaga** uma penalidade em vez de dar bônus.
 *   Declará-la como `+3` inverteria o livro.
 * - **P-SIT**: o nome tem de casar **exatamente** com `pericias.json` — "Arco"
 *   do rodapé é **"Arcos"** no catálogo.
 */
class PericiasEquipCultSitTest {

    private data class CatalogoV3(val items: List<Map<String, Any?>> = emptyList())

    /**
     * ⚠️ Lê o **`pericias.v3.json`**, que é o que o app carrega desde o Passo 3.
     * Apontar para o `pericias.json` antigo faria o teste validar um arquivo que
     * ninguém mais lê — verde mentiroso.
     */
    private fun nomesDoCatalogo(): Set<String> = catalogoV3().toSet()

    private fun catalogoV3(): List<String> {
        val direto = java.io.File("src/main/assets/pericias.v3.json")
        val arquivo = if (direto.exists()) direto else java.io.File("app/src/main/assets/pericias.v3.json")
        return Gson().fromJson(arquivo.readText(Charsets.UTF_8), CatalogoV3::class.java)
            .items.mapNotNull { it["nome"] as? String }
    }

    // ==================================================================
    // P-EQUIP — a tabela do MB p.346
    // ==================================================================

    @Test
    fun `a tabela do livro sai igual nos cinco degraus`() {
        val cirurgia = "Cirurgia/NT"      // tecnológica
        val alvenaria = "Alvenaria"       // comum
        assertEquals(-10, QualidadeDoEquipamento.modificador(cirurgia, N.SEM_NENHUM))
        assertEquals(-5, QualidadeDoEquipamento.modificador(alvenaria, N.SEM_NENHUM))
        assertEquals(-5, QualidadeDoEquipamento.modificador(cirurgia, N.IMPROVISADO))
        assertEquals(-2, QualidadeDoEquipamento.modificador(alvenaria, N.IMPROVISADO))
        assertEquals(0, QualidadeDoEquipamento.modificador(cirurgia, N.BASICO))
        assertEquals(1, QualidadeDoEquipamento.modificador(cirurgia, N.BOA))
        assertEquals(2, QualidadeDoEquipamento.modificador(cirurgia, N.SUPERIOR))
    }

    @Test
    fun `⚠️ a coluna tecnologica e o DOBRO da comum quando falta tudo`() {
        // A pegadinha da tabela. Uma coluna só daria ao cirurgião de mãos vazias
        // a mesma chance do pedreiro sem colher de pedreiro.
        assertEquals(
            QualidadeDoEquipamento.modificador("Alvenaria", N.SEM_NENHUM) * 2,
            QualidadeDoEquipamento.modificador("Cirurgia/NT", N.SEM_NENHUM)
        )
        // E do degrau "Básico" para cima as duas colunas são iguais — o bônus de
        // ferramenta boa não depende de a perícia ser tecnológica.
        listOf(N.BASICO, N.BOA, N.SUPERIOR).forEach {
            assertEquals(
                QualidadeDoEquipamento.modificador("Alvenaria", it),
                QualidadeDoEquipamento.modificador("Cirurgia/NT", it)
            )
        }
    }

    @Test
    fun `⚠️ pericia que NAO depende de equipamento fica em zero, sempre`() {
        // Lábia não piora por o personagem estar de mãos vazias. Se o seletor
        // valesse para tudo, "sem equipamento" viraria −5 global.
        N.entries.forEach {
            assertEquals("Lábia com $it", 0, QualidadeDoEquipamento.modificador("Lábia", it))
            assertEquals("Escalada com $it", 0, QualidadeDoEquipamento.modificador("Escalada", it))
        }
        assertFalse(QualidadeDoEquipamento.dependeDeEquipamento("Lábia"))
    }

    @Test
    fun `o padrao e Basico, e nele nada muda`() {
        assertEquals(N.BASICO, QualidadeDoEquipamento.PADRAO)
        QualidadeDoEquipamento.PERICIAS.forEach {
            assertEquals("$it deveria estar em zero", 0,
                QualidadeDoEquipamento.modificador(it, QualidadeDoEquipamento.PADRAO))
        }
    }

    @Test
    fun `o seletor gira e volta ao comeco`() {
        var atual = N.SEM_NENHUM
        val visitados = mutableListOf(atual)
        repeat(N.entries.size - 1) { atual = atual.proximo(); visitados += atual }
        assertEquals(N.entries.toList(), visitados)
        assertEquals("depois do ultimo volta ao primeiro", N.SEM_NENHUM, N.SUPERIOR.proximo())
    }

    @Test
    fun `🔴 toda pericia da lista existe com o nome EXATO do catalogo`() {
        // A armadilha do lote: duas delas carregam o obelisco (†) em
        // `pericias.json` e não no mapa de regras. Sem ele o modificador ficaria
        // mudo justo nas duas mais tecnológicas — Conserto de Equipamento
        // Eletrônico e Operação de Aparelhos Eletrônicos.
        val catalogo = nomesDoCatalogo()
        val erros = QualidadeDoEquipamento.PERICIAS.filterNot { it in catalogo }
        assertTrue("fora do catalogo: $erros", erros.isEmpty())
    }

    @Test
    fun `a linha da pericia diz a coluna, nao so o numero`() {
        val linha = QualidadeDoEquipamento.rotuloNaPericia("Cirurgia/NT", N.SEM_NENHUM)
        assertTrue(linha, linha.contains("-10"))
        assertTrue(linha, linha.contains("tecnológica"))
        assertTrue(linha, linha.contains("p.346"))
        // Sem nada a dizer, a linha some.
        assertEquals("", QualidadeDoEquipamento.rotuloNaPericia("Cirurgia/NT", N.BASICO))
        assertEquals("", QualidadeDoEquipamento.rotuloNaPericia("Lábia", N.SEM_NENHUM))
    }

    // ==================================================================
    // P-CULT — a vantagem que APAGA uma penalidade
    // ==================================================================

    private fun comFamiliaridade() = Personagem(
        nome = "T",
        vantagens = listOf(
            VantagemSelecionada(
                definicaoId = FamiliaridadeCulturalRules.ID, nome = "Familiaridade Cultural"
            )
        )
    )

    @Test
    fun `a penalidade de cultura vale para QUALQUER ficha`() {
        // ⚠️ O ponto do lote. O −3 é de todo mundo que está fora da sua cultura;
        // a vantagem é que isenta. Se a caixinha só aparecesse para quem tem a
        // vantagem, o app estaria cobrando de quem não deve e isentando o resto.
        val semNada = Personagem(nome = "T")
        val c = FamiliaridadeCulturalRules.condicionalDe(semNada, "Trato-Social")
        assertNotNull(c)
        assertEquals(-3, c!!.valor)
    }

    @Test
    fun `⚠️ quem TEM a vantagem continua vendo o mesmo NUMERO - muda o texto`() {
        // Zerar por conta própria assumiria que a vantagem cobre AQUELA cultura,
        // e o catálogo tem uma entrada só, sem guardar quais. Quem decide é o
        // Mestre, então o app avisa em vez de decidir.
        val com = FamiliaridadeCulturalRules.condicionalDe(comFamiliaridade(), "Trato-Social")!!
        val sem = FamiliaridadeCulturalRules.condicionalDe(Personagem(nome = "T"), "Trato-Social")!!
        assertEquals(sem.valor, com.valor)
        assertTrue(com.condicao, com.condicao.contains("confirme com o Mestre"))
        assertTrue(sem.condicao, sem.condicao.contains("sem Familiaridade"))
    }

    @Test
    fun `⚠️ a vantagem NUNCA vira bonus positivo`() {
        // Declará-la como +3 no catálogo seria o oposto do livro: daria bônus a
        // quem comprou, em vez de tirar a penalidade de quem já a tinha.
        listOf(Personagem(nome = "T"), comFamiliaridade()).forEach { p ->
            FamiliaridadeCulturalRules.PERICIAS.forEach { pericia ->
                val c = FamiliaridadeCulturalRules.condicionalDe(p, pericia)
                assertTrue("$pericia devolveu ${c?.valor}", (c?.valor ?: 0) < 0)
            }
        }
    }

    @Test
    fun `so as oito pericias que o livro nomeia ganham a caixinha`() {
        // ⚠️ Não é "toda perícia social". Lábia e Diplomacia parecem candidatas
        // e NÃO estão na lista do livro — alargar seria inventar regra.
        val p = Personagem(nome = "T")
        assertEquals(8, FamiliaridadeCulturalRules.PERICIAS.size)
        listOf("Lábia", "Diplomacia", "Escalada").forEach {
            assertNull("$it nao deveria ter caixinha",
                FamiliaridadeCulturalRules.condicionalDe(p, it))
        }
        listOf("Trato-Social", "Dança", "Heráldica", "Poesia").forEach {
            assertNotNull("$it deveria ter caixinha",
                FamiliaridadeCulturalRules.condicionalDe(p, it))
        }
    }

    @Test
    fun `as oito existem no catalogo de pericias`() {
        val catalogo = nomesDoCatalogo()
        val erros = FamiliaridadeCulturalRules.PERICIAS.filterNot { it in catalogo }
        assertTrue("fora do catalogo: $erros", erros.isEmpty())
    }

    // ==================================================================
    // P-SIT — as situações da própria perícia
    // ==================================================================

    @Test
    fun `🔴 todo nome do catalogo situacional existe em pericias json`() {
        // A armadilha que já mordeu: o rodapé diz "Arco", o catálogo tem
        // "Arcos". Nome errado aqui é caixinha que nunca aparece.
        val catalogo = nomesDoCatalogo()
        val erros = ModificadoresSituacionais.QUANTAS_PERICIAS.let {
            listOf(
                "Arcos", "Punga", "Boxe", "Furtividade", "Adestramento de Animais",
                "Golpe Poderoso", "Passos Leves", "Auto-Hipnose", "Sumô"
            ).filterNot { n -> n in catalogo }
        }
        assertTrue("fora do catalogo: $erros", erros.isEmpty())
    }

    @Test
    fun `Punga da mais 5 com a vitima distraida e mais 10 dormindo`() {
        val s = ModificadoresSituacionais.de("Punga")
        assertEquals(2, s.size)
        assertEquals(5, s[0].valor)
        assertEquals(10, s[1].valor)
        assertTrue(s[1].rotulo, s[1].rotulo.contains("dormindo"))
    }

    @Test
    fun `as quatro pericias de chi tem a MESMA regra do instantaneo`() {
        // "−10 se instantâneo, reduz com o tempo de concentração" é a mesma
        // frase nas quatro. Se uma delas divergir, alguém copiou errado.
        val quatro = listOf("Golpe Poderoso", "Pontaria Zen", "Salto Voador", "Arqueiro Zen")
        val primeira = ModificadoresSituacionais.de(quatro.first())
        assertEquals(-10, primeira.single().valor)
        quatro.forEach { assertEquals("$it divergiu", primeira, ModificadoresSituacionais.de(it)) }
    }

    @Test
    fun `Boxe e Sumo aparam chute e arma, Briga e Greco-Romana so arma`() {
        // Boxe e Sumô têm as duas linhas; Briga e Luta Greco-Romana só a de
        // arma. É o livro, e some fácil ao copiar.
        listOf("Boxe", "Sumô").forEach {
            assertEquals("$it deveria ter duas", 2, ModificadoresSituacionais.de(it).size)
        }
        listOf("Briga", "Luta Greco-Romana").forEach {
            val s = ModificadoresSituacionais.de(it)
            assertEquals("$it deveria ter uma", 1, s.size)
            assertEquals(-3, s.single().valor)
        }
    }

    @Test
    fun `Adestramento de Animais tem os TRES degraus de perigo`() {
        val s = ModificadoresSituacionais.de("Adestramento de Animais")
        assertEquals(listOf(-5, -5, -10), s.map { it.valor })
        assertTrue(s.last().rotulo, s.last().rotulo.contains("ataca"))
    }

    @Test
    fun `a esmagadora maioria das pericias NAO tem situacao nenhuma`() {
        // A caixinha é exceção. Se qualquer perícia começasse a devolver algo, o
        // diálogo viraria um paredão de caixas.
        listOf("Escalada", "Lábia", "Teologia", "Natação", "Diplomacia").forEach {
            assertTrue("$it nao devia ter situacao", ModificadoresSituacionais.de(it).isEmpty())
            assertFalse(ModificadoresSituacionais.tem(it))
        }
    }

    @Test
    fun `a caixinha nomeia a pericia e traz a situacao por extenso`() {
        val c = ModificadoresSituacionais.condicionaisDe("Furtividade")
        assertEquals(2, c.size)
        c.forEach {
            assertEquals("Furtividade", it.nomeDoTraco)
            assertEquals("Furtividade", it.alvo)
            assertTrue(it.condicao, it.condicao.isNotBlank())
        }
        assertTrue(c.first().rotulo, c.first().rotulo.contains("-5"))
    }
}
