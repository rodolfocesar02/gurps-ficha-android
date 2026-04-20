package com.gurps.ficha.domain

import com.gurps.ficha.model.Personagem

/**
 * Utilitário para gerar o contexto da ficha que será enviado para a IA.
 * Filtra metadados técnicos e foca no que é relevante para a narrativa e regras.
 */
object MestreIAContextFilter {

    fun gerarContexto(personagem: Personagem, modo: String): String {
        val sb = StringBuilder()
        
        sb.append("--- FICHA ATUAL ---\n")
        sb.append("Nome: ${personagem.nome}\n")
        sb.append("Atributos: ST ${personagem.st}, DX ${personagem.dx}, IQ ${personagem.iq}, HT ${personagem.ht}\n")
        val hpAtual = personagem.pontosVidaRolagemAtual ?: personagem.pontosVida
        val fpAtual = personagem.pontosFadigaRolagemAtual ?: personagem.pontosFadiga
        sb.append("HP: $hpAtual/${personagem.pontosVida}, FP: $fpAtual/${personagem.pontosFadiga}\n")
        
        if (personagem.vantagens.isNotEmpty()) {
            sb.append("Vantagens: ${personagem.vantagens.joinToString { it.nome }}\n")
        }
        
        if (personagem.desvantagens.isNotEmpty()) {
            sb.append("Desvantagens: ${personagem.desvantagens.joinToString { it.nome }}\n")
        }
        
        if (personagem.pericias.isNotEmpty()) {
            sb.append("Perícias Principais: ${personagem.pericias.take(15).joinToString { "${it.nome} (NH ${it.calcularNivel(personagem)})" }}\n")
        }
        
        if (modo == "conversa") {
            sb.append("Aparência: ${personagem.aparencia}\n")
            sb.append("Histórico: ${personagem.historico}\n")
        }

        return sb.toString()
    }
}
