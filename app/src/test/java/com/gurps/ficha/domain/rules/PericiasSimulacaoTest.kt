package com.gurps.ficha.domain.rules

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **Simulação exaustiva** dos modificadores que nascem na perícia.
 *
 * Os testes do lote afirmam **casos** ("Punga +10 com a vítima dormindo"). Este
 * varre o espaço inteiro — **as 302 perícias do catálogo × os 5 degraus de
 * equipamento**, e todas as situações de todas as perícias — e afirma
 * **invariantes**: coisas que têm de valer para qualquer entrada.
 *
 * Determinístico de ponta a ponta: nada é sorteado.
 */
class PericiasSimulacaoTest {

    private fun catalogo(): List<String> {
        val direto = File("src/main/assets/pericias.json")
        val arquivo = if (direto.exists()) direto else File("app/src/main/assets/pericias.json")
        val tipo = object : TypeToken<List<Map<String, Any?>>>() {}.type
        return Gson().fromJson<List<Map<String, Any?>>>(arquivo.readText(Charsets.UTF_8), tipo)
            .mapNotNull { it["nome"] as? String }
    }

    private val niveis = QualidadeDoEquipamento.Nivel.entries

    // ==================================================================
    // 1. Equipamento — 302 perícias × 5 degraus
    // ==================================================================

    @Test
    fun `nenhuma pericia de fora da lista e tocada, em nenhum degrau`() {
        // A invariante que protege as outras 270 perícias do catálogo: o seletor
        // de equipamento não pode vazar para Lábia, Teologia ou Natação.
        val deFora = catalogo().filterNot { QualidadeDoEquipamento.dependeDeEquipamento(it) }
        deFora.forEach { pericia ->
            niveis.forEach { nivel ->
                assertEquals(
                    "$pericia mudou no degrau $nivel",
                    0, QualidadeDoEquipamento.modificador(pericia, nivel)
                )
            }
        }
        assertTrue("a lista de fora deveria ser a maioria", deFora.size > 200)
    }

    @Test
    fun `piorar o equipamento NUNCA melhora uma pericia`() {
        // Varre os degraus em ordem e exige monotonia: do pior para o melhor, o
        // número só pode subir. Um sinal trocado em qualquer célula da tabela
        // quebra aqui.
        QualidadeDoEquipamento.PERICIAS.forEach { pericia ->
            niveis.zipWithNext().forEach { (pior, melhor) ->
                assertTrue(
                    "$pericia: $pior deu mais que $melhor",
                    QualidadeDoEquipamento.modificador(pericia, pior) <=
                        QualidadeDoEquipamento.modificador(pericia, melhor)
                )
            }
        }
    }

    @Test
    fun `a coluna tecnologica nunca e MELHOR que a comum`() {
        // Nos degraus ruins ela é pior; nos bons, igual. Nunca melhor — seria
        // sinal de coluna trocada.
        niveis.forEach { nivel ->
            assertTrue(
                "no degrau $nivel a coluna tecnologica ficou melhor",
                nivel.penalidadeTecnologica <= nivel.penalidadeComum
            )
        }
    }

    @Test
    fun `so o degrau Basico deixa TODAS as pericias em zero`() {
        niveis.forEach { nivel ->
            val todasZero = QualidadeDoEquipamento.PERICIAS.all {
                QualidadeDoEquipamento.modificador(it, nivel) == 0
            }
            assertEquals(
                "degrau $nivel", nivel == QualidadeDoEquipamento.Nivel.BASICO, todasZero
            )
        }
    }

    // ==================================================================
    // 2. Situacionais — todas as perícias, todas as situações
    // ==================================================================

    @Test
    fun `nenhuma situacao tem valor ZERO`() {
        // Valor 0 não muda nada: ou é engano de digitação, ou a linha não
        // deveria existir. Mesma regra do campo `efeitos`.
        catalogo().forEach { pericia ->
            ModificadoresSituacionais.de(pericia).forEach {
                assertTrue("$pericia / ${it.rotulo}", it.valor != 0)
            }
        }
    }

    @Test
    fun `nenhuma situacao fica sem texto`() {
        // Caixinha sem rótulo é um número que o jogador marca sem saber o que é.
        catalogo().forEach { pericia ->
            ModificadoresSituacionais.de(pericia).forEach {
                assertTrue("$pericia tem situacao sem rotulo", it.rotulo.isNotBlank())
            }
        }
    }

    @Test
    fun `nenhuma pericia repete a mesma situacao duas vezes`() {
        // Duplicata daria caixinhas idênticas, e marcar as duas somaria o dobro.
        catalogo().forEach { pericia ->
            val s = ModificadoresSituacionais.de(pericia)
            assertEquals("$pericia tem situacao repetida", s.size, s.distinct().size)
            assertEquals(
                "$pericia repete o rotulo",
                s.size, s.map { it.rotulo }.distinct().size
            )
        }
    }

    @Test
    fun `toda pericia do catalogo situacional esta no catalogo de pericias`() {
        // 🔴 A varredura que pega o "Arco" vs "Arcos": um nome que não existe é
        // caixinha que nunca aparece, sem erro nenhum no log.
        //
        // ⚠️ `distinct()` é obrigatório: `pericias.json` tem TRÊS nomes
        // repetidos de propósito — `arco`/`arcos`, `luta_grecoramana`/
        // `luta_greco_romana` e `mimicapantomima`/`mimica_pantomima` são
        // apelidos legados, dois ids para o mesmo nome, mantidos para ficha
        // antiga não perder a perícia. Sem o `distinct()` o teste contava duas
        // vezes e acusava um furo que não existe.
        val comSituacao = catalogo().distinct().filter { ModificadoresSituacionais.tem(it) }
        assertEquals(
            "ha nome no catalogo situacional que nao existe em pericias.json",
            ModificadoresSituacionais.QUANTAS_PERICIAS, comSituacao.size
        )
    }

    @Test
    fun `os tres nomes repetidos do catalogo sao apelidos legados`() {
        // Documenta o que acabou de morder o teste acima. Como o casamento dos
        // efeitos é por NOME, os dois ids do mesmo nome recebem o mesmo bônus —
        // que é justamente o que se quer de um apelido.
        val repetidos = catalogo().groupingBy { it }.eachCount().filterValues { it > 1 }
        assertEquals(
            setOf("Arcos", "Luta Greco-Romana", "Mímica/Pantomima"),
            repetidos.keys
        )
    }

    @Test
    fun `a caixinha e sempre condicional - nada entra no NH base`() {
        // Toda situação vira `BonusCondicional`, e o interpretador nunca soma
        // condicional no NH. Se alguma virasse fixa, valeria sempre.
        catalogo().forEach { pericia ->
            ModificadoresSituacionais.condicionaisDe(pericia).forEach {
                assertTrue("$pericia: caixinha sem condicao", it.condicao.isNotBlank())
                assertEquals("$pericia: alvo errado", pericia, it.alvo)
            }
        }
    }

    // ==================================================================
    // 3. Cultura — as duas fichas possíveis, as 302 perícias
    // ==================================================================

    @Test
    fun `a cultura so aparece nas oito, para as duas fichas`() {
        val fichas = listOf(
            Personagem(nome = "sem"),
            Personagem(
                nome = "com",
                vantagens = listOf(
                    VantagemSelecionada(
                        definicaoId = FamiliaridadeCulturalRules.ID, nome = "FC"
                    )
                )
            )
        )
        fichas.forEach { p ->
            val comCaixinha = catalogo().filter {
                FamiliaridadeCulturalRules.condicionalDe(p, it) != null
            }.toSet()
            assertEquals(FamiliaridadeCulturalRules.PERICIAS, comCaixinha)
        }
    }

    // ==================================================================
    // 4. As três fontes juntas, sem se atropelar
    // ==================================================================

    @Test
    fun `⚠️ as tres fontes nunca produzem a MESMA caixinha duas vezes`() {
        // O risco de juntar fontes num diálogo só: a mesma situação chegando por
        // dois caminhos vira duas caixinhas idênticas, e marcar as duas dobra o
        // número. Varre as 302 perícias com as duas fichas possíveis.
        val fichas = listOf(
            Personagem(nome = "sem"),
            Personagem(
                nome = "com",
                vantagens = listOf(
                    VantagemSelecionada(definicaoId = FamiliaridadeCulturalRules.ID, nome = "FC")
                )
            )
        )
        fichas.forEach { p ->
            catalogo().forEach { pericia ->
                val todas = ModificadoresSituacionais.condicionaisDe(pericia) +
                    listOfNotNull(FamiliaridadeCulturalRules.condicionalDe(p, pericia))
                val rotulos = todas.map { it.rotulo }
                assertEquals(
                    "$pericia produziu caixinha repetida: $rotulos",
                    rotulos.size, rotulos.distinct().size
                )
            }
        }
    }

    @Test
    fun `⚠️ nenhuma situacao repete a QUALIDADE que o seletor ja cobre`() {
        // Se uma perícia tivesse "sem equipamento −5" nas duas fontes, o jogador
        // levaria −10 pelo mesmo motivo. As duas listas saíram do mesmo rodapé,
        // então a sobreposição é possível — e esta é a cerca.
        //
        // ⚠️ Familiaridade NÃO é qualidade. Mergulho tem as duas de propósito: o
        // seletor mede o aparelho (MB p.346) e a situação mede o mergulhador
        // nunca ter usado aquele modelo. Somam, e devem somar.
        val palavrasDeQualidade = listOf("improvisado", "boa qualidade", "superior", "sem equipamento")
        QualidadeDoEquipamento.PERICIAS.forEach { pericia ->
            ModificadoresSituacionais.de(pericia).forEach { s ->
                val texto = s.rotulo.lowercase()
                palavrasDeQualidade.forEach { palavra ->
                    assertTrue(
                        "$pericia: a situacao '${s.rotulo}' repete a qualidade do " +
                            "equipamento, que o seletor ja cobre",
                        !texto.contains(palavra)
                    )
                }
            }
        }
    }

    @Test
    fun `Mergulho soma qualidade E familiaridade, e as duas aparecem`() {
        // O caso concreto que a cerca acima precisou distinguir. Aparelho
        // improvisado que ele nunca usou: −2 do seletor (comum? não — Mergulho é
        // /NT, então −5) mais −2 da situação.
        assertTrue(QualidadeDoEquipamento.dependeDeEquipamento("Mergulho/NT"))
        assertTrue(ModificadoresSituacionais.tem("Mergulho/NT"))
        assertEquals(
            -5,
            QualidadeDoEquipamento.modificador(
                "Mergulho/NT", QualidadeDoEquipamento.Nivel.IMPROVISADO
            )
        )
        assertEquals(-2, ModificadoresSituacionais.de("Mergulho/NT").single().valor)
    }
}
