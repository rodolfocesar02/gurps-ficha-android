package com.gurps.ficha.domain.rules

/**
 * **Modificadores de Equipamentos** (MB p.346) — Lote P-EQUIP.
 *
 * ## Trinta e duas perícias, UMA tabela
 *
 * Trinta e duas perícias trazem no rodapé a frase *"Todos de equipamento"* ou
 * *"modificadores de equipamento"*. Parece trinta e duas regras; é **uma
 * remissão à mesma tabela**, que o livro escreve uma vez só:
 *
 * | Equipamento | Perícia tecnológica | Outras |
 * |---|---|---|
 * | Sem nenhum | −10 | −5 |
 * | Improvisado | −5 | −2 |
 * | Básico | 0 | 0 |
 * | Boa qualidade (5× o preço) | +1 | +1 |
 * | Qualidade superior (20× o preço) | +2 | +2 |
 *
 * Por isso o app não ganha trinta e duas caixinhas: ganha **um seletor**, que o
 * jogador gira uma vez e vale para todas.
 *
 * ## ⚠️ A coluna dupla é a pegadinha
 *
 * Sem equipamento, uma perícia **tecnológica** perde **−10** e uma comum perde
 * **−5** — o dobro. Aplicar a mesma coluna nas duas daria a um cirurgião de mãos
 * vazias a mesma chance de um pedreiro sem colher de pedreiro.
 *
 * O app decide pelo sufixo **`/NT`** do nome, que é como o catálogo marca
 * perícia dependente de Nível Tecnológico. É um **atalho**, e está escrito aqui
 * para ninguém achar que saiu do livro: Alvenaria, Carpintaria, Artista,
 * Camuflagem, Pescaria, Contrabando, Ciclismo, Sobrevivência e Trabalhos em
 * Couro caem na coluna comum, e é onde o livro as coloca.
 *
 * ## O nível que ficou de fora
 *
 * O livro tem um sexto degrau — *"melhores equipamentos possíveis para o seu NT:
 * **+NT/2**, mínimo +2"*. A ficha **não guarda o NT da campanha**, e chutar um
 * bônus que depende de um número inexistente seria pior que não oferecê-lo.
 * Fica documentado; o Mestre soma na mão.
 *
 * Kotlin puro e testável.
 */
object QualidadeDoEquipamento {

    /**
     * Os cinco degraus que o app sabe calcular.
     *
     * A ordem é a do livro, do pior ao melhor — e é a ordem em que o seletor
     * gira, para o jogador não ter de decorar nada.
     */
    enum class Nivel(
        val rotulo: String,
        val penalidadeTecnologica: Int,
        val penalidadeComum: Int,
        val detalhe: String
    ) {
        SEM_NENHUM("Sem equipamento", -10, -5, "muitas perícias nem podem ser usadas assim"),
        IMPROVISADO("Improvisado", -5, -2, "o que dava para achar na hora"),
        BASICO("Básico", 0, 0, "o normal — é o padrão"),
        BOA("Boa qualidade", 1, 1, "cerca de 5× o preço básico"),
        SUPERIOR("Qualidade superior", 2, 2, "cerca de 20× o preço básico");

        /** O próximo degrau; depois do último volta ao começo. */
        fun proximo(): Nivel = entries[(ordinal + 1) % entries.size]
    }

    /** O degrau em que o app começa: nada muda até o jogador dizer o contrário. */
    val PADRAO = Nivel.BASICO

    /**
     * As perícias que o livro amarra ao equipamento.
     *
     * ⚠️ Os nomes são os de **`pericias.json`**, não os do mapa de regras — duas
     * delas carregam o obelisco `(†)` de especialização no catálogo e não no
     * mapa. O casamento é por nome exato: sem o obelisco, o modificador ficaria
     * mudo justo nas duas mais tecnológicas da lista.
     */
    val PERICIAS: Set<String> = setOf(
        "Alvenaria",
        "Armadilhas/NT",
        "Arrombamento/NT",
        "Artista",
        "Camuflagem",
        "Carpintaria",
        "Ciclismo",
        "Cirurgia/NT",
        "Conserto de Equipamento Eletrônico/NT (†)",
        "Contrabando",
        "Costura/NT",
        "Diagnose/NT",
        "Disfarce/NT",
        "Eletricista/NT",
        "Engenharia/NT",
        "Explosivos/NT",
        "Falsificação de Dinheiro/NT",
        "Falsificação/NT",
        "Hacking de Computador/NT",
        "Maquinista/NT",
        "Mecânica/NT",
        "Mergulho/NT",
        "Meteorologia/NT",
        "Navegação/NT",
        "Observador Avançado/NT",
        // ⚠️ **Sem espaço** antes do parêntese. O catálogo foi normalizado em
        // 31/07 (`…/NT (†)` → `…/NT(†)`) e o casamento é por nome EXATO: o
        // modificador ficou mudo até este nome ser acertado. Foi o teste
        // `toda pericia da lista existe com o nome EXATO do catalogo` que pegou.
        "Operação de Aparelhos Eletrônicos/NT(†)",
        "Paleontologia/NT",
        "Pescaria",
        "Primeiros Socorros/NT",
        "Prospecção/NT",
        "Sobrevivência",
        "Trabalhos em Couro"
    )

    /** Se o equipamento mexe nesta perícia. */
    fun dependeDeEquipamento(nomeDaPericia: String): Boolean = nomeDaPericia in PERICIAS

    /**
     * Se a perícia entra na coluna **tecnológica** da tabela.
     *
     * O sufixo `/NT` é o atalho — ver o ⚠️ no topo do arquivo.
     */
    fun ehTecnologica(nomeDaPericia: String): Boolean = nomeDaPericia.contains("/NT")

    /**
     * Quanto o equipamento vale nesta perícia, neste degrau.
     *
     * Zero para perícia que não depende de equipamento, sempre — mesmo com o
     * seletor em "sem nenhum". Lábia não piora por o personagem estar de mãos
     * vazias.
     */
    fun modificador(nomeDaPericia: String, nivel: Nivel): Int = when {
        !dependeDeEquipamento(nomeDaPericia) -> 0
        ehTecnologica(nomeDaPericia) -> nivel.penalidadeTecnologica
        else -> nivel.penalidadeComum
    }

    /** A linha do seletor: o degrau e o que ele significa. */
    fun rotuloDoSeletor(nivel: Nivel): String =
        "Equipamento: ${nivel.rotulo} — ${nivel.detalhe}"

    /**
     * A linha que aparece **na perícia**, já com o número dela.
     *
     * Vazia quando não há o que dizer. O número precisa aparecer ao lado da
     * perícia, e não só no seletor lá em cima: é a diferença entre o jogador
     * conferir a conta e ter de confiar nela.
     */
    fun rotuloNaPericia(nomeDaPericia: String, nivel: Nivel): String {
        val m = modificador(nomeDaPericia, nivel)
        if (m == 0) return ""
        val sinal = if (m > 0) "+$m" else "$m"
        val coluna = if (ehTecnologica(nomeDaPericia)) "tecnológica" else "comum"
        return "Equipamento ${nivel.rotulo.lowercase()}: $sinal ($coluna, MB p.346)"
    }

    /** O que o TalkBack lê no seletor — inclui o que o próximo toque faz. */
    fun rotuloAcessivel(nivel: Nivel): String =
        "Qualidade do equipamento: ${nivel.rotulo}. ${nivel.detalhe}. " +
            "Tocar muda para ${nivel.proximo().rotulo}."
}
