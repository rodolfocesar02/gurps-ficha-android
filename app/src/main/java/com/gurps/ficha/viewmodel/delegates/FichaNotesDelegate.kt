package com.gurps.ficha.viewmodel.delegates

import com.gurps.ficha.model.NotaDeJogo
import com.gurps.ficha.model.Personagem

class FichaNotesDelegate {
    
    fun salvarNota(personagem: Personagem, nota: NotaDeJogo): Personagem {
        val notasAtuais = personagem.notasDeJogo.toMutableList()
        val index = notasAtuais.indexOfFirst { it.id == nota.id }
        
        if (index >= 0) {
            notasAtuais[index] = nota.copy(dataModificacao = System.currentTimeMillis())
        } else {
            notasAtuais.add(nota.copy(dataModificacao = System.currentTimeMillis()))
        }
        
        // Ordenar por data de modificação, mais recentes primeiro
        notasAtuais.sortByDescending { it.dataModificacao }
        
        return personagem.copy(notasDeJogo = notasAtuais)
    }

    fun excluirNota(personagem: Personagem, notaId: String): Personagem {
        val notasAtuais = personagem.notasDeJogo.filterNot { it.id == notaId }
        return personagem.copy(notasDeJogo = notasAtuais)
    }
}
