package com.gurps.ficha.viewmodel.delegates

import com.gurps.ficha.domain.rules.CharacterRules
import com.gurps.ficha.model.*

class FichaAttributeDelegate {

    fun atualizarNome(personagem: Personagem, nome: String) = personagem.copy(nome = nome)
    fun atualizarJogador(personagem: Personagem, jogador: String) = personagem.copy(jogador = jogador)
    fun atualizarCampanha(personagem: Personagem, campanha: String) = personagem.copy(campanha = campanha)
    fun atualizarHistorico(personagem: Personagem, historico: String) = personagem.copy(historico = historico)
    fun atualizarAparencia(personagem: Personagem, aparencia: String) = personagem.copy(aparencia = aparencia)
    fun atualizarImagemPersonagem(personagem: Personagem, uri: String, originalUri: String) =
        personagem.copy(imagemPersonagemUri = uri, imagemPersonagemOriginalUri = originalUri)
    fun atualizarNotas(personagem: Personagem, notas: String) = personagem.copy(notas = notas)
    fun atualizarPontosIniciais(personagem: Personagem, pontos: Int) = personagem.copy(pontosIniciais = pontos.coerceIn(0, 1000))
    fun atualizarLimiteDesvantagens(personagem: Personagem, limite: Int) = personagem.copy(limiteDesvantagens = limite.coerceIn(-200, 0))

    fun atualizarForca(personagem: Personagem, valor: Int) = personagem.copy(forca = valor.coerceIn(1, 30))
    fun atualizarDestreza(personagem: Personagem, valor: Int) = personagem.copy(destreza = valor.coerceIn(1, 30))
    fun atualizarInteligencia(personagem: Personagem, valor: Int) = personagem.copy(inteligencia = valor.coerceIn(1, 30))
    fun atualizarVitalidade(personagem: Personagem, valor: Int) = personagem.copy(vitalidade = valor.coerceIn(1, 30))

    fun definirBasesAtributosPrimarios(
        personagem: Personagem,
        forcaBase: Int,
        destrezaBase: Int,
        inteligenciaBase: Int,
        vitalidadeBase: Int
    ): Personagem {
        val f = forcaBase.coerceIn(1, 30)
        val d = destrezaBase.coerceIn(1, 30)
        val i = inteligenciaBase.coerceIn(1, 30)
        val v = vitalidadeBase.coerceIn(1, 30)
        return personagem.copy(
            forcaBase = f, destrezaBase = d, inteligenciaBase = i, vitalidadeBase = v,
            forca = f, destreza = d, inteligencia = i, vitalidade = v
        )
    }

    fun atualizarModPontosVida(personagem: Personagem, valor: Int) = personagem.copy(modPontosVida = valor.coerceIn(-20, 20))
    fun atualizarModVontade(personagem: Personagem, valor: Int) = personagem.copy(modVontade = valor.coerceIn(-20, 20))
    fun atualizarModPercepcao(personagem: Personagem, valor: Int) = personagem.copy(modPercepcao = valor.coerceIn(-20, 20))
    fun atualizarModPontosFadiga(personagem: Personagem, valor: Int) = personagem.copy(modPontosFadiga = valor.coerceIn(-20, 20))

    fun atualizarModVelocidadeBasica(personagem: Personagem, valor: Float): Personagem {
        val valorNormalizado = CharacterRules.calcularPassosVelocidadeBasica(valor.coerceIn(-5f, 5f)) * 0.25f
        return personagem.copy(modVelocidadeBasica = valorNormalizado)
    }

    fun atualizarModDeslocamentoBasico(personagem: Personagem, valor: Int) = personagem.copy(modDeslocamentoBasico = valor.coerceIn(-10, 10))

    fun atualizarPontosVidaRolagemAtual(personagem: Personagem, valor: Int?): Personagem {
        val maxPvRolagem = (personagem.pontosVida.coerceAtLeast(0) * 5).coerceAtLeast(0)
        return personagem.copy(pontosVidaRolagemAtual = valor?.coerceIn(0, maxPvRolagem))
    }

    fun atualizarPontosFadigaRolagemAtual(personagem: Personagem, valor: Int?) =
        personagem.copy(pontosFadigaRolagemAtual = valor?.coerceAtLeast(0))
        
    fun atualizarModeloRacial(personagem: Personagem, novoModelo: ModeloRacial): Personagem {
        val novaAparencia = if (novoModelo.descricao.isNotBlank() && personagem.aparencia.isBlank()) {
            novoModelo.descricao
        } else {
            personagem.aparencia
        }
        return personagem.copy(modeloRacial = novoModelo, aparencia = novaAparencia)
    }
}
