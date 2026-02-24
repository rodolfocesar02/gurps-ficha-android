package com.gurps.ficha.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonagemRulesTest {

    @Test
    fun `calcula pontos de atributos e secundarios corretamente`() {
        val personagem = Personagem(
            forca = 12,
            destreza = 11,
            inteligencia = 9,
            vitalidade = 10,
            modPontosVida = 2,
            modVontade = -1,
            modPercepcao = 1,
            modPontosFadiga = 0,
            modVelocidadeBasica = 0.5f,
            modDeslocamentoBasico = -1
        )

        // Primarios: ST +20, DX +20, IQ -20, HT 0
        assertEquals(20, personagem.pontosAtributos)
        // Secundarios: PV +4, VON -5, PER +5, PF 0, Vel +10, Desloc -5
        assertEquals(9, personagem.pontosSecundarios)
    }

    @Test
    fun `calcula carga e deslocamento com base nos equipamentos`() {
        val personagem = Personagem(
            forca = 10,
            destreza = 10,
            vitalidade = 10,
            equipamentos = mutableListOf(
                Equipamento(nome = "Mochila", peso = 25f, quantidade = 1)
            )
        )

        assertEquals(10f, personagem.baseCarga)
        assertEquals(25f, personagem.pesoTotal)
        assertEquals(2, personagem.nivelCarga) // ate 3x BC
        assertEquals(3, personagem.deslocamentoAtual) // 5 * 0.6
        assertEquals(8, personagem.esquiva) // esquiva base sem penalidade
    }

    @Test
    fun `calcula custo especial de aptidao magica`() {
        val vantagem = VantagemSelecionada(
            definicaoId = "aptidao_magica",
            nome = "Aptidao Magica",
            custoBase = 10,
            nivel = 3,
            custoEscolhido = 0,
            tipoCusto = TipoCusto.POR_NIVEL
        )

        assertEquals(25, vantagem.custoFinal)
    }

    @Test
    fun `calcula autocontrole de desvantagem com multiplicador`() {
        val desvantagem = DesvantagemSelecionada(
            definicaoId = "teste",
            nome = "Teste",
            custoBase = -10,
            nivel = 1,
            custoEscolhido = -10,
            autocontrole = 6,
            tipoCusto = TipoCusto.FIXO
        )

        assertEquals(-20, desvantagem.custoFinal)
    }

    @Test
    fun `calcula nivel de pericia pela tabela de pontos`() {
        val personagem = Personagem(destreza = 12)
        val pericia = PericiaSelecionada(
            definicaoId = "espada_larga",
            nome = "Espada Larga",
            atributoBase = AtributoBase.DX,
            dificuldade = Dificuldade.DIFICIL,
            pontosGastos = 8
        )

        assertEquals(13, pericia.calcularNivel(personagem))
        assertEquals("+1", pericia.getNivelRelativo(personagem))
    }

    @Test
    fun `calcula nivel de magia com aptidao magica`() {
        val personagem = Personagem(inteligencia = 12)
        val magia = MagiaSelecionada(
            definicaoId = "raio",
            nome = "Raio",
            dificuldade = Dificuldade.DIFICIL,
            pontosGastos = 4
        )

        // Base IQ + AM = 14; Dificil com 4 pontos = atributo +0
        assertEquals(14, magia.calcularNivel(personagem, nivelAptidaoMagica = 2))
        assertEquals("+0", magia.getNivelRelativo(personagem, nivelAptidaoMagica = 2))
    }

    @Test
    fun `normaliza custo minimo de pontos para magias no total da ficha`() {
        val personagem = Personagem(
            magias = listOf(
                MagiaSelecionada(definicaoId = "m1", nome = "Magia 1", pontosGastos = 0),
                MagiaSelecionada(definicaoId = "m2", nome = "Magia 2", pontosGastos = -3),
                MagiaSelecionada(definicaoId = "m3", nome = "Magia 3", pontosGastos = 4)
            )
        )

        // Cada magia custa no mínimo 1 ponto: 1 + 1 + 4 = 6
        assertEquals(6, personagem.pontosMagias)
    }

    @Test
    fun `calcula defesas ativas com carga bonus e escudo`() {
        val personagem = Personagem(
            destreza = 10,
            vitalidade = 10,
            equipamentos = mutableListOf(
                Equipamento(nome = "Escudo Medio", tipo = TipoEquipamento.ESCUDO, bonusDefesa = 2),
                Equipamento(nome = "Carga", peso = 25f)
            ),
            pericias = mutableListOf(
                PericiaSelecionada(
                    definicaoId = "espada_larga",
                    nome = "Espada Larga",
                    atributoBase = AtributoBase.DX,
                    dificuldade = Dificuldade.MEDIA,
                    pontosGastos = 4
                ),
                PericiaSelecionada(
                    definicaoId = "escudo",
                    nome = "Escudo",
                    atributoBase = AtributoBase.DX,
                    dificuldade = Dificuldade.MEDIA,
                    pontosGastos = 4
                )
            ),
            defesasAtivas = DefesasAtivas(
                bonusManualEsquiva = 1,
                periciaAparaId = "espada_larga",
                bonusManualApara = 0,
                periciaBloqueioId = "escudo",
                escudoSelecionadoNome = "Escudo Medio",
                bonusManualBloqueio = 1
            )
        )

        // Esquiva base 8, carga 2, bonus +1
        assertEquals(7, personagem.defesasAtivas.calcularEsquiva(personagem))
        // NH 11 -> (11/2)+3 = 8
        assertEquals(8, personagem.defesasAtivas.calcularApara(personagem))
        // Base 8 + DB 2 + bonus manual 1
        assertEquals(11, personagem.defesasAtivas.calcularBloqueio(personagem))
    }

    @Test
    fun `respeita limite de desvantagens`() {
        val personagem = Personagem(
            limiteDesvantagens = -50,
            desvantagens = mutableListOf(
                DesvantagemSelecionada(
                    definicaoId = "odioso",
                    nome = "Odioso",
                    custoBase = -20,
                    custoEscolhido = -20
                ),
                DesvantagemSelecionada(
                    definicaoId = "impulsivo",
                    nome = "Impulsivo",
                    custoBase = -40,
                    custoEscolhido = -40
                )
            )
        )

        assertEquals(-60, personagem.pontosDesvantagens)
        assertTrue(personagem.desvantagensExcedemLimite)

        val dentroDoLimite = personagem.copy(
            desvantagens = mutableListOf(
                DesvantagemSelecionada(
                    definicaoId = "odioso",
                    nome = "Odioso",
                    custoBase = -20,
                    custoEscolhido = -20
                ),
                DesvantagemSelecionada(
                    definicaoId = "impulsivo",
                    nome = "Impulsivo",
                    custoBase = -30,
                    custoEscolhido = -30
                )
            )
        )
        assertEquals(-50, dentroDoLimite.pontosDesvantagens)
        assertFalse(dentroDoLimite.desvantagensExcedemLimite)
    }

    @Test
    fun `usa parte inteira da velocidade para deslocamento basico`() {
        val personagem = Personagem(
            destreza = 11,
            vitalidade = 10,
            modVelocidadeBasica = 0.5f, // (11+10)/4 + 0.5 = 5.75
            modDeslocamentoBasico = 1
        )

        assertEquals(5.75f, personagem.velocidadeBasica, 0.0001f)
        assertEquals(6, personagem.deslocamentoBasico) // floor(5.75) + 1
        assertEquals(8, personagem.esquiva) // floor(5.75 + 3)
    }

    @Test
    fun `calcula secundarios com velocidade em passos de 0_25`() {
        val personagem = Personagem(
            modPontosVida = 0,
            modVontade = 0,
            modPercepcao = 0,
            modPontosFadiga = 0,
            modVelocidadeBasica = 0.25f,
            modDeslocamentoBasico = 0
        )

        assertEquals(5, personagem.pontosSecundarios)
    }
}
