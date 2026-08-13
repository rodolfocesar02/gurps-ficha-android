package com.gurps.ficha.domain.rules.poderes

/**
 * **As habilidades de um poder** — GURPS Poderes, p.7-8 e p.34. Lote POD-5.
 *
 * ## 🔴 O buraco que este arquivo fecha
 *
 * Até aqui o app criava o poder como um **rótulo solto**: nome, fonte, foco,
 * percentual e Talento. Só que, no livro, o poder não é uma coisa que se compra —
 * é o que **une** vantagens que já foram compradas:
 *
 * > *"Uma vantagem precisa ter o respectivo modificador de poder para ser parte
 * > dele; **não há exceções**."* (p.8)
 *
 * > *"Os portadores do Talento para um poder, **ou qualquer de suas habilidades**
 * > (ou seja, qualquer vantagem com seu modificador de poder) são considerados
 * > possuidores daquele poder."* (p.34)
 *
 * A analogia: o poder é a **playlist**, e as habilidades são as músicas. A
 * playlist não contém as músicas — ela aponta. Apagar a playlist não apaga as
 * músicas, e é exatamente esse cuidado que a [aoRemoverOPoder] descreve.
 *
 * ## Por que isto é regra pura, e não código de tela
 *
 * A ligação já existia (`vantagem.poderId`), mas só dava para chegar nela **pelo
 * lado da vantagem**. Quem olhava o poder não via nada. A conta de "quais são as
 * habilidades deste poder" estava espalhada em `map`/`filter` dentro do
 * ViewModel; aqui ela vira uma coisa só, testável sem tela.
 */
object HabilidadesDoPoder {

    /** O prefixo do modificador que o app injeta ao ligar uma vantagem ao poder. */
    const val PREFIXO_DO_MODIFICADOR = "Mod. de Poder:"

    fun nomeDoModificador(nomeDoPoder: String) = "$PREFIXO_DO_MODIFICADOR $nomeDoPoder"

    fun idDoModificador(idDoPoder: String) = "mod_poder_$idDoPoder"

    /**
     * Uma habilidade na tela do poder: de que lado da ficha ela veio, onde está,
     * e quanto custa **já com** o modificador de poder aplicado.
     */
    data class Habilidade(
        val indice: Int,
        val nome: String,
        val custo: Int,
        val ehDesvantagem: Boolean
    )

    /** O que a tela do poder precisa mostrar. */
    data class Resumo(
        val habilidades: List<Habilidade>,
        val possuiOPoder: Boolean,
        val custoDasHabilidades: Int,
        val custoDoTalento: Int
    ) {
        val quantidade: Int get() = habilidades.size
        val custoTotal: Int get() = custoDasHabilidades + custoDoTalento
    }

    /**
     * Monta o resumo a partir das duas listas da ficha.
     *
     * ⚠️ Recebe pares `(nome, custo, poderId)` em vez dos tipos da ficha de
     * propósito: assim esta regra não depende do modelo inteiro do personagem e
     * o teste não precisa montar uma ficha para exercitá-la.
     */
    fun resumir(
        idDoPoder: String,
        vantagens: List<Triple<String, Int, String?>>,
        desvantagens: List<Triple<String, Int, String?>>,
        nivelDeTalento: Int,
        custoDoTalento: Int
    ): Resumo {
        val daqui = mutableListOf<Habilidade>()
        vantagens.forEachIndexed { i, (nome, custo, poder) ->
            if (poder == idDoPoder) daqui += Habilidade(i, nome, custo, ehDesvantagem = false)
        }
        desvantagens.forEachIndexed { i, (nome, custo, poder) ->
            if (poder == idDoPoder) daqui += Habilidade(i, nome, custo, ehDesvantagem = true)
        }
        return Resumo(
            habilidades = daqui,
            // p.34: basta UMA habilidade OU o Talento. Não precisa das duas.
            possuiOPoder = daqui.isNotEmpty() || nivelDeTalento > 0,
            custoDasHabilidades = daqui.sumOf { it.custo },
            custoDoTalento = custoDoTalento
        )
    }

    /**
     * A frase que a tela mostra quando o poder ainda não tem habilidade nenhuma.
     *
     * ⚠️ Não é erro, e por isso não é vermelho: o livro permite comprar o Talento
     * antes das habilidades (p.8, *"o Mestre pode permitir Talentos sem
     * habilidades"*). É um lembrete de que falta a metade que faz o poder existir.
     */
    fun avisoDePoderVazio(resumo: Resumo): String? = when {
        resumo.quantidade > 0 -> null
        resumo.custoDoTalento > 0 ->
            "Este poder só tem o Talento. Ligue as vantagens que são habilidades dele."
        else ->
            "Poder sem habilidades e sem Talento — ele ainda não faz nada na ficha."
    }

    /**
     * 🔴 O que precisa acontecer quando o poder é apagado.
     *
     * A vantagem **não** é apagada junto — ela existia antes e continua existindo,
     * só deixa de pertencer ao poder. O que sai é o vínculo e o modificador que o
     * app injetou, senão a vantagem fica pagando um percentual de um poder que não
     * existe mais.
     *
     * ⚠️ Isto já era feito para as **vantagens** e **não** para as desvantagens —
     * havia até um comentário `// Repetir para desvantagens` parado no
     * ViewModel. Uma desvantagem ligada a um poder apagado ficava com
     * `Mod. de Poder: X` para sempre, mexendo no custo em silêncio.
     */
    fun aoRemoverOPoder(nomeDoModificador: String): (String) -> Boolean =
        { nome -> !nome.startsWith(PREFIXO_DO_MODIFICADOR) }
}
