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
        
        val efeitos = resumoDeEfeitos(personagem)
        if (efeitos.isNotBlank()) sb.append(efeitos)

        if (modo == "conversa") {
            sb.append("Aparência: ${personagem.aparencia}\n")
            sb.append("Histórico: ${personagem.historico}\n")
        }

        return sb.toString()
    }

    /**
     * O que os traços do personagem FAZEM em números.
     *
     * Sem isto, a IA recebe só "Vantagens: Pendulear, Reflexos em Combate" e
     * tem de adivinhar a mecânica pela prosa da descrição — ou inventar. Com o
     * campo `efeitos` declarado no catálogo, ela passa a saber que Pendulear é
     * "+2 Escalada" e pode raciocinar sobre isso.
     *
     * ⚠️ Só os traços que o personagem TEM. Mandar o catálogo inteiro (272
     * vantagens) estouraria o contexto.
     *
     * Traços sem efeito declarado não aparecem — a maioria é narrativa e já
     * está listada acima pelo nome.
     */
    internal fun resumoDeEfeitos(personagem: Personagem): String {
        val linhas = (personagem.vantagens + personagem.desvantagens).mapNotNull { traco ->
            val efeitos = com.gurps.ficha.domain.rules.traits.EfeitoInterpretador
                .efeitosDe(traco.definicaoId)
            if (efeitos.isEmpty()) null
            else "- ${traco.nome}: ${efeitos.joinToString(", ") { it.resumo(traco.nivel) }}"
        }
        if (linhas.isEmpty()) return ""
        return buildString {
            append("Efeitos mecânicos dos traços (já somados na ficha, exceto os marcados com [só ...]):\n")
            linhas.forEach { append("$it\n") }
        }
    }
}
