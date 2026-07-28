package com.gurps.ficha.domain.rules.traits

import com.google.gson.JsonParser
import com.gurps.ficha.domain.loaders.parseDesvantagemParaTeste
import com.gurps.ficha.domain.loaders.parseVantagemParaTeste
import com.gurps.ficha.model.AtributoBase
import com.gurps.ficha.model.Dificuldade
import com.gurps.ficha.model.PericiaSelecionada
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.DesvantagemSelecionada
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * O teste que estava faltando: percorre o caminho INTEIRO, do arquivo de
 * catálogo até o NH que a tela mostra.
 *
 * ```
 *   vantagens.v3.json
 *      -> parser do CatalogLoaders
 *          -> VantagemDefinicao.efeitos
 *              -> EfeitoInterpretador
 *                  -> TraitRuleRegistry.getSkillBonus
 *                      -> PericiaSelecionada.calcularNivel   <- o que aparece na tela
 * ```
 *
 * Por que existe: no Lote V-1 cada pedaço desse caminho tinha teste verde e
 * mesmo assim o bônus não aplicava no aparelho — o campo `efeitos` era
 * descartado em duas conversões do loader. Os testes cobriam as pontas, nunca
 * a linha toda.
 *
 * Se qualquer elo quebrar, este teste falha. É a rede de segurança de todo
 * lote de automação daqui em diante.
 */
class EfeitoPontaAPontaTest {

    @After
    fun limpar() = EfeitoInterpretador.restaurarBuscadorPadrao()

    private fun asset(nome: String): File {
        val direto = File("src/main/assets/$nome")
        return if (direto.exists()) direto else File("app/src/main/assets/$nome")
    }

    /** Lê a vantagem do catálogo REAL, passando pelo parser REAL do loader. */
    private fun definicaoDoCatalogo(id: String) =
        JsonParser.parseString(asset("vantagens.v3.json").readText(Charsets.UTF_8))
            .asJsonArray
            .mapNotNull { parseVantagemParaTeste(it) }
            .firstOrNull { it.id == id }

    private fun definicaoDesvantagem(id: String) =
        JsonParser.parseString(asset("desvantagens.v2.json").readText(Charsets.UTF_8))
            .asJsonArray
            .mapNotNull { parseDesvantagemParaTeste(it) }
            .firstOrNull { it.id == id }

    /**
     * Liga o interpretador ao catálogo real, como acontece no app — mas sem o
     * `DataRepository`, que exigiria Context.
     */
    private fun ligarCatalogoReal() {
        val cache = mutableMapOf<String, List<EfeitoDeclarado>?>()
        EfeitoInterpretador.buscador = { id, _ ->
            cache.getOrPut(id) {
                definicaoDoCatalogo(id)?.efeitos?.takeIf { e -> e.isNotEmpty() }
                    ?: definicaoDesvantagem(id)?.efeitos
            }
        }
    }

    private fun escalada(pontos: Int = 2) = PericiaSelecionada(
        definicaoId = "escalada",
        nome = "Escalada",
        atributoBase = AtributoBase.DX,
        dificuldade = Dificuldade.MEDIA,
        pontosGastos = pontos
    )

    @Test
    fun `Pendulear na ficha aumenta o NH de Escalada em 2`() {
        ligarCatalogoReal()

        val semVantagem = Personagem(nome = "Teste", destreza = 12, pericias = listOf(escalada()))
        val comVantagem = semVantagem.copy(
            vantagens = listOf(VantagemSelecionada(definicaoId = "pendulear", nome = "Pendulear"))
        )

        val nhSem = semVantagem.pericias.first().calcularNivel(semVantagem)
        val nhCom = comVantagem.pericias.first().calcularNivel(comVantagem)

        assertEquals(
            "o bonus de Pendulear nao chegou ao NH da pericia",
            nhSem + 2, nhCom
        )
    }

    @Test
    fun `o bonus so vale para a pericia declarada`() {
        ligarCatalogoReal()

        val outra = PericiaSelecionada(
            definicaoId = "faca", nome = "Faca",
            atributoBase = AtributoBase.DX, dificuldade = Dificuldade.FACIL, pontosGastos = 1
        )
        val p = Personagem(
            nome = "Teste", destreza = 12,
            pericias = listOf(outra),
            vantagens = listOf(VantagemSelecionada(definicaoId = "pendulear", nome = "Pendulear"))
        )
        val semNada = p.copy(vantagens = emptyList())

        assertEquals(
            "Pendulear nao pode mexer em Faca",
            semNada.pericias.first().calcularNivel(semNada),
            p.pericias.first().calcularNivel(p)
        )
    }

    @Test
    fun `Voz Melodiosa entrega o bonus nas 7 pericias sociais`() {
        ligarCatalogoReal()

        val sociais = listOf("Atuação", "Canto", "Diplomacia", "Lábia", "Oratória", "Política", "Sex Appeal")
        val p = Personagem(
            nome = "Teste", inteligencia = 12,
            vantagens = listOf(VantagemSelecionada(definicaoId = "voz_melodiosa", nome = "Voz Melodiosa"))
        )
        sociais.forEach { nome ->
            assertEquals(
                "faltou o bonus em $nome",
                2, TraitRuleRegistry.getSkillBonus(p, nome)
            )
        }
    }

    @Test
    fun `sem a vantagem na ficha nao ha bonus nenhum`() {
        ligarCatalogoReal()
        val p = Personagem(nome = "Teste", destreza = 12, pericias = listOf(escalada()))
        assertEquals(0, TraitRuleRegistry.getSkillBonus(p, "Escalada"))
    }

    @Test
    fun `duas vantagens que tocam a mesma pericia somam`() {
        // Senso de Direcao e Nocao Tridimensional nao colidem hoje, mas o
        // contrato de SOMA precisa valer -- e o que permite empilhar bonus.
        EfeitoInterpretador.buscador = { id, _ ->
            when (id) {
                "a" -> listOf(EfeitoDeclarado(tipo = "pericia", alvo = "Escalada", valor = 2))
                "b" -> listOf(EfeitoDeclarado(tipo = "pericia", alvo = "Escalada", valor = 3))
                else -> null
            }
        }
        val p = Personagem(
            nome = "Teste",
            vantagens = listOf(
                VantagemSelecionada(definicaoId = "a", nome = "A"),
                VantagemSelecionada(definicaoId = "b", nome = "B")
            )
        )
        assertEquals(5, TraitRuleRegistry.getSkillBonus(p, "Escalada"))
    }

    // --- desvantagens (caminho aberto pelo Lote D-0) ---

    @Test
    fun `Gordo na ficha REDUZ o NH de Disfarce em 2`() {
        ligarCatalogoReal()

        val disfarce = PericiaSelecionada(
            definicaoId = "disfarce", nome = "Disfarce/NT",
            atributoBase = AtributoBase.IQ, dificuldade = Dificuldade.MEDIA, pontosGastos = 2
        )
        val limpo = Personagem(nome = "Teste", inteligencia = 12, pericias = listOf(disfarce))
        val gordo = limpo.copy(
            desvantagens = listOf(DesvantagemSelecionada(definicaoId = "gordo", nome = "Gordo"))
        )

        assertEquals(
            "a penalidade de Gordo nao chegou ao NH",
            limpo.pericias.first().calcularNivel(limpo) - 2,
            gordo.pericias.first().calcularNivel(gordo)
        )
    }

    @Test
    fun `Veracidade impoe menos 5 permanentes em Labia`() {
        ligarCatalogoReal()
        val p = Personagem(
            nome = "Teste",
            desvantagens = listOf(DesvantagemSelecionada(definicaoId = "veracidade", nome = "Veracidade"))
        )
        assertEquals(-5, TraitRuleRegistry.getSkillBonus(p, "Lábia"))
    }

    @Test
    fun `vantagem e desvantagem na mesma ficha se somam`() {
        // Prova que o Registry varre os dois lados (Lote D-0) no caminho real.
        EfeitoInterpretador.buscador = { id, _ ->
            when (id) {
                "bonus" -> listOf(EfeitoDeclarado(tipo = "pericia", alvo = "Disfarce/NT", valor = 3))
                "gordo" -> listOf(EfeitoDeclarado(tipo = "pericia", alvo = "Disfarce/NT", valor = -2))
                else -> null
            }
        }
        val p = Personagem(
            nome = "Teste",
            vantagens = listOf(VantagemSelecionada(definicaoId = "bonus", nome = "B")),
            desvantagens = listOf(DesvantagemSelecionada(definicaoId = "gordo", nome = "Gordo"))
        )
        assertEquals(1, TraitRuleRegistry.getSkillBonus(p, "Disfarce/NT"))
    }

    // --- defesas (Lote V-2) ---

    @Test
    fun `Reflexos em Combate soma mais 1 nas TRES defesas ativas`() {
        ligarCatalogoReal()

        val limpo = Personagem(nome = "Teste", destreza = 12, vitalidade = 12)
        val comReflexos = limpo.copy(
            vantagens = listOf(
                VantagemSelecionada(definicaoId = "reflexos_em_combate", nome = "Reflexos em Combate")
            )
        )

        // Esquiva percorre o caminho completo ate o valor exibido na ficha.
        assertEquals(
            "o bonus nao chegou na Esquiva",
            limpo.defesasAtivas.calcularEsquiva(limpo) + 1,
            comReflexos.defesasAtivas.calcularEsquiva(comReflexos)
        )

        // Aparar e Bloqueio pelo agregador (dependem de pericia/escudo para o
        // valor final, mas o bonus tem de estar la).
        assertEquals(1, TraitRuleRegistry.getParryBonus(comReflexos, null))
        assertEquals(1, TraitRuleRegistry.getBlockBonus(comReflexos))
        assertEquals(1, TraitRuleRegistry.getDodgeBonus(comReflexos))
    }

    @Test
    fun `Reflexos em Combate tambem da mais 1 em Sacar Rapido`() {
        ligarCatalogoReal()
        val p = Personagem(
            nome = "Teste",
            vantagens = listOf(
                VantagemSelecionada(definicaoId = "reflexos_em_combate", nome = "Reflexos em Combate")
            )
        )
        assertEquals(1, TraitRuleRegistry.getSkillBonus(p, "Sacar Rápido"))
    }

    @Test
    fun `bonus de defesa nao vaza para pericia`() {
        ligarCatalogoReal()
        val p = Personagem(
            nome = "Teste",
            vantagens = listOf(
                VantagemSelecionada(definicaoId = "reflexos_em_combate", nome = "Reflexos em Combate")
            )
        )
        // "esquiva" e alvo de DEFESA; nao pode aparecer como bonus de pericia.
        assertEquals(0, TraitRuleRegistry.getSkillBonus(p, "esquiva"))
        assertEquals(0, TraitRuleRegistry.getSkillBonus(p, "Escalada"))
    }
}
