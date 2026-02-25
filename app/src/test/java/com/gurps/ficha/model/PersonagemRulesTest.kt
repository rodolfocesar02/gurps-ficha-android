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
    fun `calcula nivel de magia sem aptidao magica`() {
        val personagem = Personagem(inteligencia = 12)
        val magia = MagiaSelecionada(
            definicaoId = "misseis",
            nome = "Mísseis",
            dificuldade = Dificuldade.DIFICIL,
            pontosGastos = 1
        )

        // Sem AM: base IQ = 12; Dificil com 1 ponto = -2
        assertEquals(10, magia.calcularNivel(personagem, nivelAptidaoMagica = 0))
        assertEquals("-2", magia.getNivelRelativo(personagem, nivelAptidaoMagica = 0))
    }

    @Test
    fun `calcula nivel de magia com aptidao magica em varios niveis`() {
        val personagem = Personagem(inteligencia = 12)
        val magia = MagiaSelecionada(
            definicaoId = "luz",
            nome = "Luz",
            dificuldade = Dificuldade.MEDIA,
            pontosGastos = 8
        )

        // Média com 8 pontos = +2 relativo ao atributo-base da magia (IQ+AM)
        assertEquals(14, magia.calcularNivel(personagem, nivelAptidaoMagica = 0))
        assertEquals("+2", magia.getNivelRelativo(personagem, nivelAptidaoMagica = 0))

        assertEquals(16, magia.calcularNivel(personagem, nivelAptidaoMagica = 2))
        assertEquals("+2", magia.getNivelRelativo(personagem, nivelAptidaoMagica = 2))
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
    fun `defesas retornam nulo sem pericia configurada e DB somente de escudo`() {
        val personagem = Personagem(
            destreza = 10,
            vitalidade = 10,
            equipamentos = listOf(
                Equipamento(nome = "Escudo Leve", tipo = TipoEquipamento.ESCUDO, bonusDefesa = 1),
                Equipamento(nome = "Escudo Leve", tipo = TipoEquipamento.GERAL, bonusDefesa = 99)
            ),
            defesasAtivas = DefesasAtivas(
                periciaAparaId = null,
                periciaBloqueioId = null,
                escudoSelecionadoNome = "Escudo Leve"
            )
        )

        assertEquals(null, personagem.defesasAtivas.calcularApara(personagem))
        assertEquals(null, personagem.defesasAtivas.calcularBloqueio(personagem))
        // Deve considerar apenas o item do tipo ESCUDO (DB=1), ignorando item GERAL com mesmo nome.
        assertEquals(1, personagem.defesasAtivas.getBonusEscudo(personagem))
    }

    @Test
    fun `busca de DB do escudo ignora caixa e espacos e mantem filtro por tipo`() {
        val personagem = Personagem(
            equipamentos = listOf(
                Equipamento(nome = " Escudo Pesado ", tipo = TipoEquipamento.ESCUDO, bonusDefesa = 3),
                Equipamento(nome = "escudo pesado", tipo = TipoEquipamento.GERAL, bonusDefesa = 99)
            ),
            defesasAtivas = DefesasAtivas(escudoSelecionadoNome = "  eScUdO pEsAdO  ")
        )

        assertEquals(3, personagem.defesasAtivas.getBonusEscudo(personagem))
    }

    @Test
    fun `carga afeta esquiva mas nao altera apara e bloqueio base da pericia`() {
        val base = Personagem(
            destreza = 10,
            vitalidade = 10,
            equipamentos = listOf(
                Equipamento(nome = "Escudo Medio", tipo = TipoEquipamento.ESCUDO, bonusDefesa = 2)
            ),
            pericias = listOf(
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
                periciaAparaId = "espada_larga",
                periciaBloqueioId = "escudo",
                escudoSelecionadoNome = "Escudo Medio"
            )
        )
        val comCarga = base.copy(
            equipamentos = base.equipamentos + Equipamento(nome = "Carga", peso = 25f)
        )

        assertEquals(8, base.defesasAtivas.calcularEsquiva(base))
        assertEquals(6, comCarga.defesasAtivas.calcularEsquiva(comCarga))

        assertEquals(base.defesasAtivas.calcularApara(base), comCarga.defesasAtivas.calcularApara(comCarga))
        assertEquals(base.defesasAtivas.calcularBloqueio(base), comCarga.defesasAtivas.calcularBloqueio(comCarga))
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

    @Test
    fun `mantem consistencia da ficha em persistencia round trip json`() {
        val original = Personagem(
            nome = "Teste Persistencia",
            jogador = "Rodolfo",
            campanha = "Campanha X",
            pontosIniciais = 200,
            limiteDesvantagens = -80,
            forca = 11,
            destreza = 12,
            inteligencia = 13,
            vitalidade = 10,
            modPontosVida = 1,
            modVontade = -1,
            modPercepcao = 2,
            modPontosFadiga = 1,
            modVelocidadeBasica = 0.25f,
            modDeslocamentoBasico = 1,
            vantagens = listOf(
                VantagemSelecionada(
                    definicaoId = "aptidao_magica",
                    nome = "Aptidão Mágica",
                    custoBase = 10,
                    nivel = 2,
                    tipoCusto = TipoCusto.POR_NIVEL
                )
            ),
            desvantagens = listOf(
                DesvantagemSelecionada(
                    definicaoId = "odioso",
                    nome = "Odioso",
                    custoBase = -10,
                    custoEscolhido = -10,
                    autocontrole = 12,
                    tipoCusto = TipoCusto.FIXO
                )
            ),
            pericias = listOf(
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
                    pontosGastos = 2
                )
            ),
            magias = listOf(
                MagiaSelecionada(
                    definicaoId = "luz",
                    nome = "Luz",
                    dificuldade = Dificuldade.MEDIA,
                    pontosGastos = 2
                )
            ),
            equipamentos = listOf(
                Equipamento(nome = "Escudo Médio", tipo = TipoEquipamento.ESCUDO, bonusDefesa = 2, peso = 6f, custo = 60f),
                Equipamento(nome = "Mochila", tipo = TipoEquipamento.GERAL, peso = 10f, custo = 50f)
            ),
            defesasAtivas = DefesasAtivas(
                bonusManualEsquiva = 1,
                periciaAparaId = "espada_larga",
                bonusManualApara = 0,
                periciaBloqueioId = "escudo",
                escudoSelecionadoNome = "Escudo Médio",
                bonusManualBloqueio = 1
            ),
            notas = "rodada de teste"
        )

        val restaurado = Personagem.fromJson(original.toJson())

        // Identidade estrutural de campos persistidos
        assertEquals(original.nome, restaurado.nome)
        assertEquals(original.pontosIniciais, restaurado.pontosIniciais)
        assertEquals(original.vantagens.size, restaurado.vantagens.size)
        assertEquals(original.desvantagens.size, restaurado.desvantagens.size)
        assertEquals(original.pericias.size, restaurado.pericias.size)
        assertEquals(original.magias.size, restaurado.magias.size)
        assertEquals(original.equipamentos.size, restaurado.equipamentos.size)
        assertEquals(original.defesasAtivas, restaurado.defesasAtivas)

        // Consistência de cálculos derivados após round-trip
        assertEquals(original.nivelCarga, restaurado.nivelCarga)
        assertEquals(original.deslocamentoAtual, restaurado.deslocamentoAtual)
        assertEquals(original.defesasAtivas.calcularEsquiva(original), restaurado.defesasAtivas.calcularEsquiva(restaurado))
        assertEquals(original.defesasAtivas.calcularApara(original), restaurado.defesasAtivas.calcularApara(restaurado))
        assertEquals(original.defesasAtivas.calcularBloqueio(original), restaurado.defesasAtivas.calcularBloqueio(restaurado))
        assertEquals(original.pontosGastos, restaurado.pontosGastos)
        assertEquals(original.pontosRestantes, restaurado.pontosRestantes)
    }

    @Test
    fun `equipamento de arma resolve dano com ST do personagem`() {
        val personagem = Personagem(forca = 10)
        val arma = Equipamento(
            nome = "Espada Larga",
            tipo = TipoEquipamento.ARMA,
            armaDanoRaw = "GeB+1 corte"
        )

        assertEquals("1d+1 corte", arma.danoCalculadoComSt(personagem))
    }

    @Test
    fun `equipamento de armadura usa RD estruturado quando disponivel`() {
        val armadura = Equipamento(
            nome = "Colete",
            tipo = TipoEquipamento.ARMADURA,
            notas = "Local: tronco; RD: 8/2*",
            armaduraRd = "12/4*"
        )

        assertEquals("12/4*", armadura.rdArmaduraExibicao())
    }

    @Test
    fun `equipamento de armadura faz fallback para RD em notas legado`() {
        val armadura = Equipamento(
            nome = "Colete legado",
            tipo = TipoEquipamento.ARMADURA,
            notas = "Local: tronco; RD: 8/2*; Obs: teste",
            armaduraRd = null
        )

        assertEquals("8/2*", armadura.rdArmaduraExibicao())
    }

    @Test
    fun `bloqueio usa melhor DB de escudo quando nenhum escudo foi selecionado explicitamente`() {
        val personagem = Personagem(
            destreza = 10,
            equipamentos = listOf(
                Equipamento(nome = "Escudo Leve", tipo = TipoEquipamento.ESCUDO, bonusDefesa = 1),
                Equipamento(nome = "Escudo Grande", tipo = TipoEquipamento.ESCUDO, bonusDefesa = 3)
            ),
            pericias = listOf(
                PericiaSelecionada(
                    definicaoId = "escudo",
                    nome = "Escudo",
                    atributoBase = AtributoBase.DX,
                    dificuldade = Dificuldade.MEDIA,
                    pontosGastos = 2
                )
            ),
            defesasAtivas = DefesasAtivas(
                periciaBloqueioId = "escudo",
                escudoSelecionadoNome = null
            )
        )

        // NH 10 -> base 8, + DB 3
        assertEquals(11, personagem.defesasAtivas.calcularBloqueio(personagem))
    }
}
