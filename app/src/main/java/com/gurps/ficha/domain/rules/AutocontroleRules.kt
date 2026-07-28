package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.DesvantagemSelecionada
import com.gurps.ficha.model.Personagem

/**
 * Testes de autocontrole (GURPS MB p.120).
 *
 * Desvantagem mental com Número de Autocontrole (NA): 6, 9, 12 ou 15. Quando a
 * situação surge, o jogador rola 3d6 contra o NA — passou, resistiu; falhou,
 * cede à desvantagem.
 *
 * O app já GUARDA o NA (usado no multiplicador de custo) mas nunca rolou nada:
 * o jogador tinha de fazer na mão.
 *
 * QUANTAS SÃO: **30** desvantagens, não 35. O sinal correto é o `*` no custo
 * (convenção do GURPS: "-10*" = usa número de autocontrole), não a palavra
 * "autocontrole" na descrição. Cinco desvantagens só CITAM autocontrole sem ter
 * o seu: Flashbacks e Vozes Fantasmagóricas disparam quando o personagem falha
 * no autocontrole de OUTRA desvantagem; Dorminhoco e Vício PENALIZAM testes de
 * autocontrole alheios; Atavismo por Estresse tem redação ambígua no catálogo.
 * `DesvantagemDefinicao.usaAutocontroleMental()` já usa o critério certo.
 *
 * Kotlin puro, sem Android, para poder ser testado — o mesmo desenho de
 * `SentidoRules`, que resolveu o problema equivalente para os Sentidos.
 */
object AutocontroleRules {

    /** Uma desvantagem da ficha que pode ser testada. */
    data class TesteDisponivel(
        /** Índice na lista `personagem.desvantagens` — identifica a instância. */
        val indice: Int,
        val nome: String,
        /** Número de autocontrole: 6, 9, 12 ou 15. */
        val na: Int,
        /** Especialização, quando houver (ex.: a fobia específica). */
        val detalhe: String,
        /** A "notinha": explica o que o número significa. */
        val explicacao: String
    ) {
        /** Rótulo para a UI: `Fobia (Altura) — NA 12`. */
        val rotulo: String
            get() = if (detalhe.isBlank()) nome else "$nome ($detalhe)"
    }

    /**
     * As desvantagens do personagem que têm NA, na ordem da ficha.
     *
     * Duas instâncias da mesma desvantagem (duas Fobias diferentes) aparecem
     * SEPARADAS, cada uma com seu NA — é o caso comum e seria erro agrupar.
     *
     * Desvantagem mental sem NA definido fica de fora: sem número não há o que
     * rolar.
     */
    fun testesDisponiveis(personagem: Personagem): List<TesteDisponivel> =
        personagem.desvantagens.mapIndexedNotNull { indice, d ->
            val na = d.autocontrole ?: return@mapIndexedNotNull null
            if (na !in NAS_VALIDOS) return@mapIndexedNotNull null
            TesteDisponivel(
                indice = indice,
                nome = d.nome,
                na = na,
                detalhe = d.descricao.trim(),
                explicacao = explicacaoDo(na)
            )
        }

    /** True quando a seção de autocontrole deve aparecer na tela. */
    fun temAlgumTeste(personagem: Personagem): Boolean =
        testesDisponiveis(personagem).isNotEmpty()

    /**
     * O que o NA significa, em palavras.
     *
     * O jogador precisa entender que NA BAIXO é PIOR — é contraintuitivo, já
     * que na maior parte do GURPS número alto é melhor. Aqui, quanto menor o
     * NA, mais difícil resistir e mais pontos a desvantagem vale.
     */
    fun explicacaoDo(na: Int): String = when (na) {
        6 -> "NA 6: resiste raramente. Role 3d6 e passe em 6 ou menos."
        9 -> "NA 9: resiste com dificuldade. Role 3d6 e passe em 9 ou menos."
        12 -> "NA 12: costuma resistir. Role 3d6 e passe em 12 ou menos."
        15 -> "NA 15: quase sempre resiste. Role 3d6 e passe em 15 ou menos."
        else -> "Role 3d6 e passe em $na ou menos."
    }

    /**
     * O NA é FIXO. Vontade alta NÃO ajuda — erro comum de quem conhece outros
     * testes do GURPS. Só modificador situacional dado pelo Mestre entra.
     */
    fun alvoEfetivo(na: Int, modificadorSituacional: Int = 0): Int =
        (na + modificadorSituacional).coerceIn(3, 18)

    private val NAS_VALIDOS = setOf(6, 9, 12, 15)
}
