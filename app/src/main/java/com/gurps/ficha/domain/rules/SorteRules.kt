package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem

/**
 * **Sorte** (MB p.90) — refazer duas vezes e ficar com o melhor dos três.
 *
 * > Uma vez a cada hora de jogo, o personagem pode **refazer duas vezes** um teste
 * > ruim e **ficar com o melhor dos três resultados**. (…) A Sorte se aplica
 * > somente a **testes de habilidade, avaliações de dano e testes de reação**.
 * >
 * > Sorte Extraordinária: a cada **30 minutos**. Sorte Impossível: a cada **10
 * > minutos**.
 *
 * ## 🔴 "Melhor" muda de sinal conforme o tipo de rolagem
 *
 * Esta é a pegadinha da vantagem, e ela é fácil de errar porque a palavra é a
 * mesma nos dois casos:
 *
 * | Tipo de rolagem | "Melhor" é |
 * |---|---|
 * | Teste de habilidade, ataque, defesa, reação | o **menor** total |
 * | **Avaliação de dano** | o **maior** total |
 *
 * Num teste de perícia você quer tirar pouco; num dano, muito. Programar "melhor
 * = maior" faria a Sorte **piorar** todos os testes do personagem — e o jogador
 * levaria sessões para desconfiar, porque a vantagem "funcionou" (rolou três
 * vezes) e o resultado é plausível.
 *
 * ## O relógio
 *
 * ⚠️ **Sem o relógio a vantagem vira honra.** O livro é explícito que é tempo
 * REAL: *"o jogador precisa esperar uma hora do tempo real (…) antes de recorrer
 * a ela novamente. O personagem não pode utilizar Sorte às 11:58 e novamente às
 * 12:01"*. Ninguém lembra da hora no meio da mesa, então o app marca.
 *
 * ## O que ela NÃO faz
 *
 * - **Não se compartilha.** *"Sam, o Forte, está tentando abrir uma porta com um
 *   pontapé; Lou, o Sortudo, não pode ficar atrás dele e passar um pouco de
 *   sorte."* O app é uma ficha só, então isso se resolve sozinho.
 * - **Não acumula.** Não dá para jogar horas sem usar e depois usar várias vezes
 *   seguidas — por isso o relógio guarda **o último uso**, não um saldo.
 *
 * Kotlin puro e testável. Quem sabe que horas são é a tela; aqui só entram
 * minutos decorridos.
 */
object SorteRules {

    const val ID_SORTE = "sorte"
    const val ID_SUPER_SORTE = "super_sorte"

    /** Os três graus, pelo custo pago (MB p.90). */
    enum class Grau(val custo: Int, val minutos: Int, val rotulo: String) {
        NORMAL(15, 60, "Sorte"),
        EXTRAORDINARIA(30, 30, "Sorte Extraordinária"),
        IMPOSSIVEL(60, 10, "Sorte Impossível")
    }

    /**
     * O grau da Sorte na ficha, ou null se não tem.
     *
     * O catálogo guarda a Sorte como escolha de custo (15/30/60), então é o
     * `custoEscolhido` que diz o grau — mesma leitura que a Boa Forma faz.
     */
    fun grauDe(personagem: Personagem): Grau? {
        val selecao = personagem.vantagensTotais.firstOrNull { it.definicaoId == ID_SORTE }
            ?: return null
        // Pega o maior grau que o custo pago alcança. Custo fora da tabela (ficha
        // antiga com 0) cai no grau mais baixo em vez de devolver null: ter a
        // vantagem e não poder usá-la seria pior.
        return Grau.entries.lastOrNull { selecao.custoEscolhido >= it.custo } ?: Grau.NORMAL
    }

    /** Se a ficha tem Super Sorte (MB p.91) — usa o mesmo relógio de 1 hora. */
    fun temSuperSorte(personagem: Personagem): Boolean =
        personagem.vantagensTotais.any { it.definicaoId == ID_SUPER_SORTE }

    /** Se há algo a mostrar na tela. */
    fun temAlguma(personagem: Personagem): Boolean =
        grauDe(personagem) != null || temSuperSorte(personagem)

    /**
     * Se pode usar agora.
     *
     * [minutosDesdeUltimoUso] null = nunca usou nesta sessão.
     */
    fun podeUsar(personagem: Personagem, minutosDesdeUltimoUso: Long?): Boolean {
        val grau = grauDe(personagem) ?: return false
        return minutosDesdeUltimoUso == null || minutosDesdeUltimoUso >= grau.minutos
    }

    /** Quantos minutos ainda faltam, ou 0 se já pode. */
    fun minutosRestantes(personagem: Personagem, minutosDesdeUltimoUso: Long?): Long {
        val grau = grauDe(personagem) ?: return 0
        if (minutosDesdeUltimoUso == null) return 0
        return (grau.minutos - minutosDesdeUltimoUso).coerceAtLeast(0)
    }

    /**
     * Qual das três jogadas fica.
     *
     * ⚠️ [ehDano] inverte o critério — ver a tabela na documentação da classe.
     * Ela é o único parâmetro que existe aqui, e existe só por causa disso.
     */
    fun melhorDe(totais: List<Int>, ehDano: Boolean): Int =
        if (ehDano) totais.max() else totais.min()

    /**
     * A jogada que fica quando o personagem está **sendo atacado**.
     *
     * > (…) ou quando o personagem está sendo atacado (nesse caso, o jogador faz
     * > as jogadas três vezes **pelo atacante** e fica com o **pior** resultado).
     *
     * Não tem uso na aba Rolagem hoje, porque quem rola o ataque do inimigo é o
     * Mestre. Fica aqui escrita para não se perder — e porque o dia em que a Saga
     * precisar dela, a regra já existe conferida.
     */
    fun piorDoAtacante(totais: List<Int>): Int = totais.max()

    /** O rótulo do botão. */
    fun rotulo(personagem: Personagem, minutosDesdeUltimoUso: Long?): String {
        val grau = grauDe(personagem) ?: return ""
        val falta = minutosRestantes(personagem, minutosDesdeUltimoUso)
        return if (falta <= 0) {
            "Usar ${grau.rotulo}"
        } else {
            "${grau.rotulo}: disponível em $falta min"
        }
    }

    /** O mesmo, para o TalkBack. */
    fun descricaoAcessivel(personagem: Personagem, minutosDesdeUltimoUso: Long?): String {
        val grau = grauDe(personagem) ?: return ""
        val falta = minutosRestantes(personagem, minutosDesdeUltimoUso)
        return if (falta <= 0) {
            "Usar ${grau.rotulo}. Rola mais duas vezes e fica com o melhor dos três " +
                "resultados. Depois de usar, só volta a estar disponível em ${grau.minutos} minutos."
        } else {
            "${grau.rotulo} indisponível. Faltam $falta minutos."
        }
    }

    /** A explicação do resultado, nomeando as três jogadas e a escolhida. */
    fun explicacaoDoResultado(totais: List<Int>, escolhido: Int, ehDano: Boolean): String {
        val criterio = if (ehDano) "maior" else "menor"
        return "Sorte: ${totais.joinToString(", ")} → ficou $escolhido (o $criterio)"
    }
}
