package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.traits.EfeitoDeclarado
import com.gurps.ficha.domain.rules.traits.EfeitoInterpretador
import com.gurps.ficha.domain.rules.traits.TraitRuleRegistry
import com.gurps.ficha.model.DesvantagemSelecionada
import com.gurps.ficha.model.MetacaracteristicaRef
import com.gurps.ficha.model.ModeloRacial
import com.gurps.ficha.model.PericiaRacial
import com.gurps.ficha.model.TipoPericiaRacial
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import com.gurps.ficha.viewmodel.DefenseType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Vantagem que vem da RAÇA vale igual à comprada na ficha (bug de 28/07).
 *
 * ## O bug
 *
 * Reportado pelo usuário: *"adicionei uma raça com a vantagem ST de
 * Levantamento, e os bônus da vantagem não entraram"*.
 *
 * Investigando, era muito maior que aquela vantagem. O `TraitRuleRegistry` e as
 * dez regras de `domain/rules/` liam apenas `personagem.vantagens` — a lista do
 * que o **jogador comprou**. As vantagens da raça vivem em
 * `modeloRacial.vantagens`, uma lista separada, e **nenhuma delas era vista**.
 *
 * Ou seja: uma raça com Pendulear não dava +2 em Escalada; uma com Reflexos em
 * Combate não dava +1 nas defesas; uma com Carisma não mexia na reação. Os 91
 * efeitos declarados simplesmente não existiam para traço racial.
 *
 * ## Por que passou tanto tempo
 *
 * Três pontos já mesclavam as duas listas **à mão** (`SentidoRules`,
 * `bonusDeslocamentoAquatico`, `MagicEngine`), cada um do seu jeito. Como esses
 * funcionavam, nada denunciava que o resto não funcionava.
 *
 * O conserto foi criar um lugar único — `Personagem.vantagensTotais` —, irmão do
 * `periciasTotais`, que já fazia isso para perícias desde sempre.
 *
 * Este teste percorre **cada família de regra** com a vantagem só na raça.
 */
class VantagemRacialContaTest {

    @After
    fun limpar() = EfeitoInterpretador.restaurarBuscadorPadrao()

    private fun raçaCom(vararg vantagens: VantagemSelecionada) =
        ModeloRacial(nome = "Teste", vantagens = vantagens.toList())

    private fun vant(id: String, nome: String, nivel: Int = 1, custo: Int = 0) =
        VantagemSelecionada(definicaoId = id, nome = nome, nivel = nivel, custoEscolhido = custo)

    // --- a que o usuário achou ---

    @Test
    fun `ST de Levantamento RACIAL aumenta a Base de Carga`() {
        val p = Personagem(
            nome = "Teste", forca = 10,
            modeloRacial = raçaCom(vant(StEspecializadaRules.ID_LEVANTAMENTO, "ST de Levantamento", 5))
        )
        assertEquals(5, StEspecializadaRules.bonusDeLevantamento(p))
        assertEquals(15, StEspecializadaRules.stParaCarga(p))
        assertEquals(22.5f, p.baseCarga, 0.001f)
    }

    @Test
    fun `ST de Golpe RACIAL aumenta o dano`() {
        val p = Personagem(
            nome = "Teste", forca = 10,
            modeloRacial = raçaCom(vant(StEspecializadaRules.ID_GOLPE, "ST de Golpe", 4))
        )
        assertEquals(CharacterRules.calcularDanoGeB(14), p.danoGeB)
    }

    @Test
    fun `racial e comprada SOMAM`() {
        // Uma raça forte + pontos gastos na mesma vantagem = os dois valores.
        val p = Personagem(
            nome = "Teste", forca = 10,
            vantagens = listOf(vant(StEspecializadaRules.ID_LEVANTAMENTO, "ST de Levantamento", 2)),
            modeloRacial = raçaCom(vant(StEspecializadaRules.ID_LEVANTAMENTO, "ST de Levantamento", 5))
        )
        assertEquals(7, StEspecializadaRules.bonusDeLevantamento(p))
    }

    // --- o resto das famílias de regra ---

    @Test
    fun `efeito DECLARADO na raca chega a pericia`() {
        // O caso mais amplo: sao 91 efeitos declarados no catalogo, e nenhum
        // deles funcionava vindo da raca.
        EfeitoInterpretador.buscador = { id, _ ->
            if (id == "pendulear") listOf(EfeitoDeclarado(tipo = "pericia", alvo = "Escalada", valor = 2))
            else null
        }
        val p = Personagem(
            nome = "Teste",
            modeloRacial = raçaCom(vant("pendulear", "Pendulear"))
        )
        assertEquals(2, TraitRuleRegistry.getSkillBonus(p, "Escalada"))
    }

    @Test
    fun `efeito de DEFESA na raca chega a Esquiva`() {
        EfeitoInterpretador.buscador = { id, _ ->
            if (id == "reflexos") listOf(EfeitoDeclarado(tipo = "defesa", alvo = "esquiva", valor = 1))
            else null
        }
        val p = Personagem(
            nome = "Teste",
            modeloRacial = raçaCom(vant("reflexos", "Reflexos em Combate"))
        )
        assertEquals(1, TraitRuleRegistry.getDodgeBonus(p))
        // ...e a notinha diz de onde veio, com o nome do traço racial.
        val origens = OrigemDosNumeros.daDefesa(p, DefenseType.ESQUIVA)
        assertEquals(listOf("Reflexos em Combate"), origens.map { it.nomeDoTraco })
    }

    @Test
    fun `bonus CONDICIONAL na raca vira caixinha`() {
        EfeitoInterpretador.buscador = { id, _ ->
            if (id == "rosto") listOf(
                EfeitoDeclarado(tipo = "pericia", alvo = "Dissimulação", valor = 1, condicao = "x")
            ) else null
        }
        val p = Personagem(nome = "Teste", modeloRacial = raçaCom(vant("rosto", "Rosto Sincero")))
        assertEquals(1, TraitRuleRegistry.getBonusCondicionais(p, "Dissimulação").size)
    }

    @Test
    fun `Ambidestria RACIAL zera a penalidade da mao inabil`() {
        val p = Personagem(nome = "Teste", modeloRacial = raçaCom(vant("ambidestria", "Ambidestria")))
        assertEquals(0, MaoInabilRules.penalidadeDe(p, usandoMaoInabil = true))
    }

    @Test
    fun `Bracais raciais aparecem no seletor`() {
        val p = Personagem(
            nome = "Teste", forca = 10, destreza = 10,
            modeloRacial = raçaCom(
                vant("st_bracal", "ST Braçal", 3),
                vant("dx_bracal", "DX Braçal", 2)
            )
        )
        assertTrue(StBracalRules.temStBracal(p))
        assertTrue(DxBracalRules.temDxBracal(p))
        assertEquals(13, StBracalRules.stDosBracos(p))
        assertEquals(12, DxBracalRules.dxDosBracos(p))
    }

    @Test
    fun `Boa Forma e Duro de Matar raciais entram nos testes de resistir`() {
        val p = Personagem(
            nome = "Teste", vitalidade = 10,
            modeloRacial = raçaCom(
                vant("boa_forma", "Boa Forma", custo = 15),
                vant("duro_de_matar", "Duro de Matar", nivel = 3)
            )
        )
        val morte = ResistenciaRules.testesDe(p).first { it.rotulo.contains("morte") }
        assertEquals("HT 10 + Boa Forma 2 + Duro de Matar 3", 15, morte.alvo)

        // ...e tambem no marco de PV, que usa a mesma familia de bonus.
        val marco = MarcosDeVidaRules.testesAoPerderPv(p, -9, -10).first()
        assertEquals(15, marco.alvo)
    }

    @Test
    fun `desvantagem racial com autocontrole aparece na lista`() {
        val p = Personagem(
            nome = "Teste",
            modeloRacial = ModeloRacial(
                nome = "Teste",
                desvantagens = listOf(
                    DesvantagemSelecionada(definicaoId = "avareza", nome = "Avareza", autocontrole = 12)
                )
            )
        )
        assertTrue(AutocontroleRules.temAlgumTeste(p))
        assertEquals("Avareza", AutocontroleRules.testesDisponiveis(p).first().nome)
    }

    @Test
    fun `Aptidao Magica RACIAL tambem bloqueia o Abascanto`() {
        val p = Personagem(
            nome = "Teste",
            modeloRacial = raçaCom(vant(IncompatibilidadeDeTracos.ID_APTIDAO_MAGICA, "Aptidão Mágica"))
        )
        assertTrue(
            IncompatibilidadeDeTracos.motivoParaRecusar(
                p, IncompatibilidadeDeTracos.ID_ABASCANTO
            ) != null
        )
    }

    // --- metacaracterística: o pacote DENTRO do pacote (MB p.262) ---

    private fun metaCom(nome: String, conteudo: ModeloRacial) =
        MetacaracteristicaRef(id = nome.lowercase(), nome = nome, conteudo = conteudo)

    @Test
    fun `vantagem dentro de METACARACTERISTICA conta`() {
        // O usuário perguntou por raça E metacaracterística. A metacaracterística
        // é um segundo nível: os traços não estão em `modeloRacial.vantagens`,
        // estão em `metacaracteristicas[i].conteudo.vantagens`.
        val p = Personagem(
            nome = "Teste", forca = 10,
            modeloRacial = ModeloRacial(
                nome = "Espírito",
                metacaracteristicas = listOf(
                    metaCom("Corpo de Pedra", ModeloRacial(
                        vantagens = listOf(vant(StEspecializadaRules.ID_LEVANTAMENTO, "ST de Levantamento", 5))
                    ))
                )
            )
        )
        assertEquals(5, StEspecializadaRules.bonusDeLevantamento(p))
    }

    @Test
    fun `modificador de atributo da METACARACTERISTICA entra na conta`() {
        // "Corpo de Madeira" do catálogo dá −1 de Velocidade Básica. O custo já
        // era cobrado (custoTotal soma custoMeta); o EFEITO não chegava.
        val p = Personagem(
            nome = "Teste", forca = 10, destreza = 10, vitalidade = 10,
            modeloRacial = ModeloRacial(
                nome = "Boneco",
                metacaracteristicas = listOf(
                    metaCom("Corpo de Madeira", ModeloRacial(modVelocidadeBasica = -1f))
                )
            )
        )
        assertEquals(4.0f, p.velocidadeBasica, 0.001f)
    }

    @Test
    fun `metacaracteristica dentro de metacaracteristica ainda conta`() {
        val interna = ModeloRacial(vantagens = listOf(vant("ambidestria", "Ambidestria")))
        val p = Personagem(
            nome = "Teste",
            modeloRacial = ModeloRacial(
                metacaracteristicas = listOf(
                    metaCom("Externa", ModeloRacial(
                        metacaracteristicas = listOf(metaCom("Interna", interna))
                    ))
                )
            )
        )
        assertEquals(0, MaoInabilRules.penalidadeDe(p, usandoMaoInabil = true))
    }

    @Test
    fun `pericia concedida dentro da metacaracteristica aparece para rolar`() {
        val p = Personagem(
            nome = "Teste",
            modeloRacial = ModeloRacial(
                metacaracteristicas = listOf(
                    metaCom("Bicho", ModeloRacial(
                        pericias = listOf(
                            PericiaRacial(
                                nome = "Rastreamento", nivelRelativo = 0,
                                tipo = TipoPericiaRacial.CONCEDIDA
                            )
                        )
                    ))
                )
            )
        )
        assertTrue(p.periciasTotais.any { it.nome == "Rastreamento" })
    }

    // --- a rede de segurança ---

    @Test
    fun `ficha sem raca continua igual`() {
        // A correcao nao pode mudar nada para quem nao usa modelo racial.
        val p = Personagem(nome = "Teste", forca = 10, vitalidade = 10)
        assertTrue(p.vantagensTotais.isEmpty())
        assertTrue(p.desvantagensTotais.isEmpty())
        assertEquals(10.0f, p.baseCarga, 0.001f)
    }

    @Test
    fun `vantagem racial NAO e cobrada duas vezes`() {
        // ⚠️ A armadilha da correção. `vantagensTotais` serve para EFEITO, nunca
        // para CUSTO: a vantagem da raça já está paga dentro do preço do modelo
        // racial (`modeloRacial.custoTotal`). Trocar `pontosVantagens` para a
        // lista total cobraria o jogador duas vezes pela mesma vantagem.
        val p = Personagem(
            nome = "Teste",
            modeloRacial = raçaCom(vant("visao_aguda", "Visão Aguda", custo = 20))
        )
        assertEquals(0, p.pontosVantagens)
    }

    @Test
    fun `vantagensTotais nao mexe na lista crua`() {
        // `vantagens` continua sendo so o que o jogador comprou -- e o que o
        // calculo de custo e a tela de edicao usam.
        val p = Personagem(
            nome = "Teste",
            vantagens = listOf(vant("a", "Comprada")),
            modeloRacial = raçaCom(vant("b", "Racial"))
        )
        assertEquals(1, p.vantagens.size)
        assertEquals(2, p.vantagensTotais.size)
    }
}
