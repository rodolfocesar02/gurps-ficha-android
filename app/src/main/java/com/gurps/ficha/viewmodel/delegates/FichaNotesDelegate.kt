package com.gurps.ficha.viewmodel.delegates

import com.gurps.ficha.model.NotaDeJogo
import com.gurps.ficha.model.Personagem

/**
 * **O caderno de anotações** — Lote NOTA-1.
 *
 * Guarda, edita e apaga as [NotaDeJogo] de um personagem. Sem estado próprio:
 * recebe o [Personagem], devolve outro. É a fatia de lógica que o
 * `FichaViewModel` não precisa carregar.
 *
 * ## Salvar e editar são a MESMA porta
 *
 * [salvarNota] procura pelo `id`: achou, substitui; não achou, acrescenta. Uma
 * função só, porque do lado da tela a diferença não existe — o editor abre com
 * uma nota nova (`NotaDeJogo()`, id sorteado) ou com uma existente, e fecha do
 * mesmo jeito nos dois casos.
 *
 * 🔴 **E daí veio um defeito, corrigido no Lote NOTA-3.** Como "não achei o id"
 * quer dizer *"então é nota nova"*, salvar uma nota **já apagada** a trazia de
 * volta: o `EditorDeNota` grava ao fechar, e o botão de excluir fecha a tela.
 * Apagou, fechou, ressuscitou.
 *
 * ⚠️ A cura **não** foi mexer aqui, e é o ponto: este arquivo está certo.
 * Acrescentar um `id` desconhecido é justamente o que faz criar nota funcionar.
 * O defeito era a **ordem** em que a tela chamava as duas coisas, e a bandeira
 * que resolve mora lá (`foiExcluida`, no `EditorDeNota`).
 *
 * ⚠️ A ordenação é por `dataModificacao`, decrescente, **a cada gravação**.
 * Ordenar na tela em vez de aqui deixaria a lista depender de quem a desenha —
 * e há dois lugares que a desenham.
 */
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
