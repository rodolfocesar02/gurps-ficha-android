package com.gurps.ficha.domain.rules

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.model.PoderDefinicao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.text.Normalizer

/**
 * **Os catálogos que GURPS Poderes acrescenta** — Lote POD-8.
 *
 * ## 🔴 O plano prometia 17 modificadores e 5 vantagens. Eram 1 e 4.
 *
 * A contagem do plano veio de listar os **títulos** das páginas 107-113 e
 * comparar com o asset. O erro: o livro repete o nome de um modificador em
 * ordem alfabética só para **apontar para o verbete real**:
 *
 * > *"Solavanco **veja Efeito Incômodo, pág. 103**"*
 * > *"Subaquático **veja pág. MB109**"*
 *
 * Dos 48 títulos, **16 são esse tipo de remissão** e não existem como
 * modificador próprio. Contando só os que têm valor, o livro tem 32 e o app já
 * tinha 31.
 *
 * Duas outras entradas também caíram na conferência:
 * - **Características Variantes** não é modificador: é seção sobre efeitos
 *   especiais (p.113).
 * - **Controle Divino** não é vantagem: é caixa lateral dizendo que o Mestre
 *   pode permitir Controle sobre elementos abstratos, por 30 pontos/nível.
 *
 * ⚠️ A lição: **contar títulos não é contar conteúdo**. O diff que eu apresentei
 * estava certo na forma e errado no número.
 */
class CatalogoDePoderesCompletoTest {

    private fun asset(nome: String): File {
        val direto = File("src/main/assets/$nome")
        val f = if (direto.exists()) direto else File("app/src/main/assets/$nome")
        assertTrue("nao encontrei ${f.absolutePath}", f.exists())
        return f
    }

    private fun semAcento(s: String) =
        Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}"), "").trim()

    private val vantagens by lazy {
        JsonParser.parseString(asset("vantagens.v3.json").readText(Charsets.UTF_8)).asJsonArray
    }

    private val modificadores by lazy {
        JsonParser.parseString(
            asset("modificadores_poderes.v1.json").readText(Charsets.UTF_8)
        ).asJsonArray
    }

    private fun nomesDe(arr: com.google.gson.JsonArray) =
        arr.map { semAcento(it.asJsonObject.get("nome").asString) }.toSet()

    // ── As quatro vantagens novas do capítulo 2 ────────────────────────

    @Test
    fun `as quatro vantagens de Poderes entraram no catalogo`() {
        val nomes = nomesDe(vantagens)
        listOf("Controle", "Criar", "Estática", "Ilusão").forEach {
            assertTrue("a vantagem '$it' (GURPS Poderes) nao esta no catalogo",
                semAcento(it) in nomes)
        }
        // Neutralizar já existia — não pode ter virado duplicata.
        assertEquals(
            "Neutralizar duplicou",
            1,
            vantagens.count { semAcento(it.asJsonObject.get("nome").asString) == "neutralizar" }
        )
    }

    @Test
    fun `Estatica e Ilusao tem custo fechado, do livro`() {
        val porNome = vantagens.associateBy { semAcento(it.asJsonObject.get("nome").asString) }
        val estatica = porNome[semAcento("Estática")]!!.asJsonObject
        assertEquals("fixed", estatica.get("costKind").asString)
        assertEquals(30, estatica.get("fixed").asInt)     // p.94
        assertEquals(94, estatica.get("pagina").asInt)

        val ilusao = porNome[semAcento("Ilusão")]!!.asJsonObject
        assertEquals("fixed", ilusao.get("costKind").asString)
        assertEquals(25, ilusao.get("fixed").asInt)       // p.95
    }

    @Test
    fun `Controle e Criar nao fingem ter um custo unico`() {
        // ⚠️ Nas duas o custo por nível depende de uma ESCOLHA que o app não
        // modela (raridade do elemento, amplitude da categoria): Controle é
        // 20/15/10 e Criar é 40/20/10. Cravar um número seria inventar.
        // Ficam como `special`, com a tabela inteira na descrição.
        val porNome = vantagens.associateBy { semAcento(it.asJsonObject.get("nome").asString) }
        listOf("Controle" to "20 pontos/nível", "Criar" to "40 pontos/nível").forEach { (n, tabela) ->
            val v = porNome[semAcento(n)]!!.asJsonObject
            assertEquals("'$n' cravou um custo que o livro nao da",
                "special", v.get("costKind").asString)
            assertTrue("'$n' esta sem a tabela de custo na descricao",
                v.get("descricao").asString.contains(tabela))
        }
    }

    // ── O modificador que faltava ──────────────────────────────────────

    @Test
    fun `Normalmente Ativa entrou, e diz que o valor e derivado`() {
        val porNome = modificadores.associateBy { semAcento(it.asJsonObject.get("nome").asString) }
        val m = porNome[semAcento("Normalmente Ativa")]
        assertTrue("o modificador 'Normalmente Ativa' (p.109) nao esta no catalogo", m != null)
        val d = m!!.asJsonObject.get("descricao").asString
        // 🔴 O valor NAO e fixo: sai do Sempre Ativa daquela vantagem (metade
        // dele, ou +10% se for maior). Sem esta explicacao, alguem cravaria um
        // numero e estaria errado em metade dos casos.
        assertTrue(d, d.contains("metade"))
        assertTrue(d, d.contains("+10%"))
    }

    @Test
    fun `a remissao aponta para onde o modificador esta DEFINIDO`() {
        // 🔴🔴 ESTE TESTE DIZIA O CONTRARIO, E ESTAVA ERRADO — lote POD-8b.
        //
        // No POD-8 eu concluí que 16 títulos eram "só remissão" e **proibi que
        // virassem modificador**. Errado: a remissão aponta para onde o
        // modificador está definido **dentro do próprio livro**.
        //
        // "Solavanco veja Efeito Incômodo, pág. 103" → lá está: **+30%**.
        // "Difícil de Usar veja Destreinado, pág. 102" → lá está: **−5% por −3**.
        //
        // Oito dos dezesseis eram modificadores de verdade, e o teste os
        // **bloqueava**: quem tentasse acrescentá-los seria reprovado pelo gate.
        // Uma conclusão minha virou trava, e a trava tinha mais autoridade que o
        // livro.
        //
        // ⚠️ A lição: teste que afirma que algo **não existe** é o mais perigoso
        // que dá para escrever. Ele não protege regra nenhuma — protege a minha
        // leitura.
        val definidosSobOutroTitulo = mapOf(
            "Ataque Surpresa" to 150,
            "Solavanco" to 30,
            "Fogo Instantâneo" to 10,
            "Defesa Ativa" to -40,
            "Efeito do Dano Ausente" to -20,
            "Difícil de Usar" to -5,
            "Exige Teste de Reação" to -5,
            "Gatilho Incontrolável" to -5
        )
        val porNome = modificadores.associateBy { semAcento(it.asJsonObject.get("nome").asString) }
        definidosSobOutroTitulo.forEach { (nome, valor) ->
            val m = porNome[semAcento(nome)]
            assertTrue("'$nome' e modificador de verdade e sumiu do catalogo", m != null)
            assertEquals(
                "'$nome' esta com valor fora do livro",
                valor,
                m!!.asJsonObject.get("valor").asString.toInt()
            )
            // A remissão faz parte do dado: o livro lista numa página e define
            // noutra. Sem registrar isso, ninguém acha de volta.
            val d = m.asJsonObject.get("descricao").asString
            assertTrue("'$nome' nao diz sob que titulo esta definido", d.contains("definido sob"))
        }
    }

    @Test
    fun `as remissoes para o Modulo Basico continuam fora`() {
        // Estas oito **realmente** não são modificador próprio de Poderes: ou
        // apontam para o Módulo Básico ("Subaquático veja pág. MB109"), ou não
        // são modificador (Características Variantes é seção sobre efeitos
        // especiais, p.113).
        val naoSaoDaqui = listOf(
            "Subaquático", "Sempre Ativa", "Variável", "Uso Limitado",
            "Efeito Seletivo", "Desvantagem Exigida", "Magnético",
            "Características Variantes"
        )
        val nomes = nomesDe(modificadores)
        naoSaoDaqui.forEach {
            assertFalse("'$it' nao e modificador proprio de GURPS Poderes",
                semAcento(it) in nomes)
        }
    }

    @Test
    fun `modificador sem valor precisa explicar por que`() {
        // ⚠️ **Zero não é "sem valor".** "Tempo de Jogo" vale **+0%** no livro, e
        // isso é um valor de verdade — o modificador existe, marca a habilidade e
        // não muda o preço. A primeira versão deste teste tratou `"0"` como
        // ausência e reprovou uma entrada correta.
        //
        // Ausência é campo **vazio**, e só um modificador tem direito a isso:
        // Normalmente Ativa, cujo percentual é derivado do Sempre Ativa daquela
        // vantagem em particular (p.109).
        modificadores.forEach { el ->
            val o = el.asJsonObject
            val nome = o.get("nome").asString
            val valor = o.get("valor")?.asString.orEmpty()
            if (valor.isBlank()) {
                val d = o.get("descricao")?.asString.orEmpty()
                assertTrue(
                    "'$nome' esta sem valor E sem explicacao de por que",
                    d.contains("derivado") || d.contains("metade") || d.contains("Variável")
                )
            }
        }
    }

    // ── As sugestões do POD-10 citam estes nomes ───────────────────────

    @Test
    fun `as habilidades sugeridas citam vantagens que agora existem`() {
        // O motivo de o POD-8 vir logo depois do POD-10: as listas sugeridas
        // citam Controle, Criar, Estática e Ilusão nome por nome.
        val poderes: List<PoderDefinicao> = Gson().fromJson(
            asset("poderes.v1.json").readText(Charsets.UTF_8),
            object : TypeToken<List<PoderDefinicao>>() {}.type
        )
        val todas = poderes.flatMap { it.habilidades }
        val nomes = nomesDe(vantagens)
        listOf("Controle", "Criar", "Estática", "Ilusão").forEach { v ->
            assertTrue("nenhuma sugestao cita '$v' — o diff estaria errado",
                todas.any { it.startsWith(v) })
            assertTrue("'$v' e citado nas sugestoes mas nao existe no catalogo",
                semAcento(v) in nomes)
        }
    }
}
