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

    /**
     * PV atual da rolagem, com o piso do LIVRO e não zero.
     *
     * O piso era 0 e travava a ficha no marco mais banal do GURPS: o
     * personagem some de 0 PV para baixo — testa HT para não desmaiar, e a cada
     * múltiplo negativo do PV máximo testa para não morrer, até −5× que é morte
     * automática (MB p.423). Com o piso em zero, nada disso podia ser
     * registrado, e o teste de morte do Lote MARCOS-1 era inalcançável.
     *
     * Achado no aparelho em 28/07: *"não consigo descer o PV pra −10"*.
     */
    fun atualizarPontosVidaRolagemAtual(personagem: Personagem, valor: Int?): Personagem {
        val pvMax = personagem.pontosVida.coerceAtLeast(1)
        val teto = pvMax * 5
        val piso = -pvMax * 5     // morte automática; abaixo disso não há regra
        return personagem.copy(pontosVidaRolagemAtual = valor?.coerceIn(piso, teto))
    }

    /**
     * PF atual, idem: o livro vai até −1× o PF máximo, onde o personagem
     * desmaia (MB p.426). Zero não é o fundo.
     */
    fun atualizarPontosFadigaRolagemAtual(personagem: Personagem, valor: Int?): Personagem {
        val pfMax = personagem.pontosFadiga.coerceAtLeast(1)
        return personagem.copy(pontosFadigaRolagemAtual = valor?.coerceIn(-pfMax, pfMax * 5))
    }

    /**
     * Lote MB-6: salva o painel do botão PF de uma vez — a origem do cansaço, o
     * PF resultante e o PV que a sede severa levou junto.
     *
     * ⚠️ Vai tudo numa chamada só de propósito. Em dois `copy()` separados o
     * segundo leria o personagem de antes do primeiro e desfaria a metade da
     * alteração — é o mesmo tropeço de gravar duas vezes o mesmo formulário.
     */
    fun aplicarPainelDeFadiga(
        personagem: Personagem,
        quantidades: Map<String, Int>,
        pfNovo: Int,
        pvPerdidos: Int
    ): Personagem {
        val pfMax = personagem.pontosFadiga.coerceAtLeast(1)
        val pvMax = personagem.pontosVida.coerceAtLeast(1)
        val pvAtual = personagem.pontosVidaRolagemAtual ?: personagem.pontosVida
        return personagem.copy(
            fadigaPorFonte = quantidades.filterValues { it > 0 },
            pontosFadigaRolagemAtual = pfNovo.coerceIn(-pfMax, pfMax * 5),
            pontosVidaRolagemAtual = if (pvPerdidos > 0) {
                (pvAtual - pvPerdidos).coerceIn(-pvMax * 10, pvMax * 5)
            } else {
                personagem.pontosVidaRolagemAtual
            }
        )
    }

    /** Lote MB-7: o PV depois do ferimento + quais peças ficaram na mochila. */
    fun aplicarFerimentoPorLocal(
        personagem: Personagem,
        pvNovo: Int,
        guardadas: Set<String>
    ): Personagem {
        val pvMax = personagem.pontosVida.coerceAtLeast(1)
        return personagem.copy(
            pontosVidaRolagemAtual = pvNovo.coerceIn(-pvMax * 10, pvMax * 5),
            armadurasGuardadas = guardadas.toList().sorted()
        )
    }


    fun atualizarModeloRacial(personagem: Personagem, novoModelo: ModeloRacial): Personagem {
        val novaAparencia = if (novoModelo.descricao.isNotBlank() && personagem.aparencia.isBlank()) {
            novoModelo.descricao
        } else {
            personagem.aparencia
        }
        return personagem.copy(modeloRacial = novoModelo, aparencia = novaAparencia)
    }
}
