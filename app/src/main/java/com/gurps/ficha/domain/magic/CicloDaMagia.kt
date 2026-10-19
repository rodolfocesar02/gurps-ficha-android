package com.gurps.ficha.domain.magic

/**
 * Lote MC-5 — o DONO do ciclo de uma magia: custo para operar/manter, duração e tempo de operação.
 *
 * Vivia como três funções privadas dentro do `SagaCombatController` (MEC-5b). Aqui elas ficam à vista,
 * sem Android, para que a **pedra de toque** da Mesa possa extrair do teste deste objeto os casos que
 * a Mesa tem de repetir (`CicloDaMagiaTest` → `pedra-de-toque.json`). O controlador só delega.
 *
 * A regra é uma só: **o número conferido contra o livro (MC-2) manda; o parser do texto do cabeçalho
 * é o plano B** quando o catálogo não tem número (custo variável, tempo "Varia", duração em fórmula).
 * Nenhum número é inventado aqui: ou vem do catálogo, ou vem do parser do texto.
 */
object CicloDaMagia {

    /**
     * Custo CONFERIDO contra o livro, ou null quando a magia não tem `custoOperar` (custo variável,
     * por unidade, em fórmula) — aí o chamador cai em [MagicEnergy.parse] do texto.
     *
     * @param operar `custoOperar` do catálogo
     * @param manter `custoManter` do catálogo (0 = não pode ser mantida; null = metade, Magia p.15)
     * @param energiaTexto o cabeçalho `energia`, guardado em `original`
     */
    fun custo(operar: Int?, manter: Int?, energiaTexto: String?): CustoEnergia? {
        val base = operar ?: return null
        return CustoEnergia(
            base = base, variavel = false, minimo = base.coerceAtLeast(1),
            manutencao = manter, original = energiaTexto.orEmpty(),
        )
    }

    /**
     * (tipo, segundos) da duração: o `duracaoTipo`/`duracaoSeg` conferidos; sem eles, o texto do
     * cabeçalho passa por [MagicTime.parseDuracao].
     */
    fun duracao(tipo: String?, segundos: Int?, texto: String?): Pair<TipoDuracao, Int> {
        val t = when (tipo) {
            "instantanea" -> TipoDuracao.INSTANTANEA
            "temporaria" -> TipoDuracao.TEMPORARIA
            "permanente" -> TipoDuracao.PERMANENTE
            else -> return MagicTime.parseDuracao(texto)
        }
        return t to (segundos ?: 0)
    }

    /**
     * Tempo de operação em segundos: o `tempoOperacaoSeg` conferido (> 0); sem ele, o texto do
     * cabeçalho passa por [MagicTime.parseTempoSeg] (que nunca devolve menos de 1 s).
     */
    fun tempoSeg(segundos: Int?, texto: String?): Int =
        segundos?.takeIf { it > 0 } ?: MagicTime.parseTempoSeg(texto)

    /**
     * O texto que a TELA mostra (e que as contas leem): o texto do número conferido
     * (`energiaTela`/`duracaoTela`/`tempoOperacaoTela`) quando existe, senão o cabeçalho do livro.
     *
     * O script do catálogo (`numeros.js`) só grava a `*Tela` onde o cabeçalho (a tabela-resumo) diz
     * um número e a entrada do livro, que vale, diz outro: Desintegrar tem "1 a 4" na tabela e
     * "1 a 10" na entrada, e a tela tem de mostrar 1 a 10 — o que é cobrado.
     */
    fun textoDaTela(cabecalho: String?, tela: String?): String? =
        tela?.takeIf { it.isNotBlank() } ?: cabecalho

    /** A magia com `energia`, `duracao` e `tempoOperacao` já trocados pelo texto da tela. */
    fun comTextoDaTela(def: com.gurps.ficha.model.MagiaDefinicao): com.gurps.ficha.model.MagiaDefinicao =
        def.copy(
            energia = textoDaTela(def.energia, def.energiaTela),
            duracao = textoDaTela(def.duracao, def.duracaoTela),
            tempoOperacao = textoDaTela(def.tempoOperacao, def.tempoOperacaoTela),
        )
}
