package com.gurps.ficha.domain.rules

/**
 * **Qualidade das Armas de Combate Corpo a Corpo** — MB p.275-276. Lote EQP-11.
 *
 * ## ⚠️ Não confundir com [QualidadeDoEquipamento]
 *
 * Aquele é o modificador de **perícia** por ferramenta (p.346) — *"todos de
 * equipamento"*. Este é a qualidade da **arma**, e mexe em **dano**. Nomes
 * parecidos, páginas diferentes, contas diferentes.
 *
 * ## 🔴 O que o app fazia
 *
 * O dano saía do catálogo sem qualidade nenhuma. Uma espada de qualidade
 * superior era idêntica a uma comum — e o jogador que a comprasse anotava
 * *"+1 Dano"* **na nota**, que o combate não lê. É o mesmo problema que o RD
 * tinha antes do EQP-8.
 *
 * ## O bônus só vale para lâmina
 *
 * > *"Uma **lâmina** (arma de corte ou perfuração) que se classifica como tal
 * > também recebe um bônus de +1 nos danos causados por corte e perfuração."*
 *
 * ⚠️ Uma maça de qualidade superior **não** ganha dano — ganha só o -1 no teste
 * de quebra. Ela não tem lâmina para ser bem-feita. Aplicar o +1 em tudo daria a
 * um porrete caro o mesmo ganho de uma katana.
 */
object QualidadeDaArma {

    /**
     * @param bonusDeDano soma ao dano, **só** em corte e perfuração.
     * @param modificadorDeQuebra entra no teste de quebra ao aparar arma pesada
     *   (p.376) — positivo é bom, a arma resiste mais.
     * @param soEspadas a altíssima qualidade *"somente as armas de esgrima e as
     *   espadas podem ser consideradas"*.
     */
    enum class Nivel(
        val rotulo: String,
        val bonusDeDano: Int,
        val modificadorDeQuebra: Int,
        val soEspadas: Boolean = false
    ) {
        BARATA("Barata", 0, +2),
        BOA("Boa", 0, 0),
        SUPERIOR("Superior", +1, -1),
        ALTISSIMA("Altíssima", +2, -2, soEspadas = true);

        val explicacao: String
            get() = when (this) {
                BARATA -> "aguenta mais ao aparar, mas se for de arremesso perde 1 de Precisão"
                BOA -> "a qualidade padrão das tabelas do livro"
                SUPERIOR -> "lâmina bem-feita: mais 1 no dano por corte e perfuração"
                ALTISSIMA -> "só espadas e esgrima: mais 2 no dano por corte e perfuração"
            }
    }

    /** A qualidade que o livro assume quando ninguém escolheu (p.275). */
    val PADRAO = Nivel.BOA

    /**
     * O bônus de dano **desta arma** com **este** tipo de dano.
     *
     * Zero em três casos, e cada um por um motivo diferente:
     * - a qualidade não dá bônus (barata, boa);
     * - o dano não é de corte nem perfuração — não há lâmina em jogo;
     * - a altíssima foi escolhida para algo que não é espada nem esgrima.
     */
    fun bonusDeDano(nivel: Nivel, tipo: DanoTipo, ehEspadaOuEsgrima: Boolean = true): Int {
        if (nivel.soEspadas && !ehEspadaOuEsgrima) return 0
        if (!ehLamina(tipo)) return 0
        return nivel.bonusDeDano
    }

    /** *"arma de corte ou perfuração"* — é o que o livro chama de lâmina. */
    fun ehLamina(tipo: DanoTipo): Boolean = tipo == DanoTipo.CORT || tipo == DanoTipo.PERF

    /**
     * Aplica o bônus a um dano já escrito, tipo `"1d+2 corte"`.
     *
     * ⚠️ Soma no **acréscimo**, nunca nos dados: `1d+2` vira `1d+3`, e não `2d`.
     * O livro é claro que o bônus é ao dano, e trocar dados mudaria a curva
     * inteira da rolagem.
     */
    fun aplicarAoDano(danoResolvido: String, bonus: Int): String {
        if (bonus == 0 || danoResolvido.isBlank()) return danoResolvido
        val m = Regex("""^\s*(\d+)d\s*([+-]\s*\d+)?(.*)$""").find(danoResolvido) ?: return danoResolvido
        val dados = m.groupValues[1]
        val acrescimoAtual = m.groupValues[2].replace(" ", "").toIntOrNull() ?: 0
        val resto = m.groupValues[3]
        val novo = acrescimoAtual + bonus
        val sufixo = when {
            novo > 0 -> "+$novo"
            novo < 0 -> "$novo"
            else -> ""
        }
        return "${dados}d$sufixo$resto"
    }

    /**
     * Se a arma conta como espada ou esgrima, pelo grupo do catálogo.
     *
     * ⚠️ É leitura de **texto**, e está escrito aqui para ninguém achar que saiu
     * do livro: o catálogo agrupa as armas pelo nome da perícia (*"ESPADAS DE
     * LÂMINA LARGA"*, *"ADAGA DE ESGRIMA"*, *"RAPIEIRA"*), e é isso que se
     * procura. Uma arma sem grupo cadastrado responde `false` — melhor negar um
     * bônus do que dar um que o livro não permite.
     */
    fun ehEspadaOuEsgrima(grupoOuNome: String?): Boolean {
        val t = TextoDoCatalogo.corrigir(grupoOuNome.orEmpty()).lowercase()
        if (t.isBlank()) return false
        return listOf("espada", "esgrima", "rapieira", "sabre", "tercado", "terçado", "katana")
            .any { t.contains(it) }
    }
}

/**
 * Descobre o **tipo de dano** a partir do texto do catálogo — Lote EQP-11.
 *
 * O catálogo escreve `"GeB+2 corte"`, `"GdP-1 perf"`, `"4d(3) pa-"`. O tipo é a
 * palavra no fim, e é ela que decide se a qualidade da arma dá bônus: só corte e
 * perfuração são lâmina (MB p.275).
 *
 * ⚠️ Fica separado do [DanoTipo] de propósito. Lá o enum é o **domínio** — o que
 * o dano faz. Aqui é **leitura de texto sujo**, com abreviação e acento variando
 * de linha para linha do catálogo, e por isso tem o seu próprio teste.
 */
object TipoDeDanoNoTexto {

    fun ler(danoRaw: String?): DanoTipo? {
        val t = TextoDoCatalogo.corrigir(danoRaw.orEmpty()).lowercase()
        if (t.isBlank()) return null
        return when {
            // A ordem importa: "pa-" e "pa+" antes de "pa", senão o curto casa
            // primeiro e um perfurante pequeno viraria perfurante comum.
            Regex("""\bpa\+\+""").containsMatchIn(t) -> DanoTipo.PI_MAIS_MAIS
            Regex("""\bpa\+""").containsMatchIn(t) -> DanoTipo.PI_MAIS
            Regex("""\bpa-""").containsMatchIn(t) -> DanoTipo.PI_MENOS
            Regex("""\bpa\b""").containsMatchIn(t) -> DanoTipo.PI
            t.contains("corte") -> DanoTipo.CORT
            t.contains("perf") -> DanoTipo.PERF
            t.contains("cont") -> DanoTipo.CONT
            // Lote EQP-12: as 11 armas de energia do catálogo (laser, feixe
            // iônico, lança-chamas, espada de energia).
            t.contains("qmd") -> DanoTipo.QMD
            else -> null
        }
    }
}
