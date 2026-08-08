package com.gurps.ficha.domain.rules

import java.text.Normalizer

/**
 * **Que parte do corpo cada armadura cobre, e quanta RD ela dá lá** — Lote MB-7.
 *
 * O botão **PV** precisa responder a uma pergunta que a ficha nunca soube
 * responder: *"levei 8 de corte no braço — quanto disso a minha armadura
 * segurou?"*
 *
 * ## ⚠️ O problema: a ficha guarda o local como TEXTO LIVRE
 *
 * O catálogo tem o campo `locaisNorm` bonito e normalizado (`["bracos",
 * "tronco"]`), mas o que sobra dentro do `Equipamento` da ficha é o
 * `localRaw` — *"corpo, membros"*, *"traje completo"*, *"crnio, pescoo"*. Sim,
 * **com as letras faltando**: a extração do livro comeu o `â` de "crânio" e o
 * `ç` de "pescoço", e isso está gravado em 9 itens do catálogo.
 *
 * Então este arquivo é um **tradutor**, e ele tem um teste que não deixa mentir:
 * a varredura passa os 72 itens reais pelo tradutor e exige que o resultado bata
 * **exatamente** com o `locaisNorm` que o próprio catálogo publica. Se alguém
 * reescrever o JSON com acento, o teste avisa.
 *
 * ## ⚠️ Vitais não têm armadura própria
 *
 * O livro não vende "peitoral para os órgãos vitais": os vitais ficam **dentro
 * do tronco**, então quem protege o tronco protege os vitais. Sem essa linha, um
 * personagem de cota de malha levaria o triplo de dano perfurante nos vitais
 * como se estivesse nu.
 */
object CoberturaDaArmadura {

    /**
     * As palavras que o catálogo usa, e o que cada uma cobre.
     *
     * As chaves ficam **sem acento e em minúsculas** de propósito: é o único
     * jeito de "crânio", "crnio" e "CRÂNIO" caírem na mesma linha.
     */
    private val VOCABULARIO: List<Pair<String, Set<LocalAtaque>>> = listOf(
        // Compostos primeiro — "traje completo" precisa ganhar de "tronco".
        "traje completo" to setOf(
            LocalAtaque.TORSO, LocalAtaque.INGLE, LocalAtaque.PESCOCO,
            LocalAtaque.BRACO, LocalAtaque.PERNA, LocalAtaque.MAO, LocalAtaque.PE
        ),
        // ⚠️ "traje completo" NÃO inclui cabeça: o elmo é comprado à parte, e é
        // por isso que o crânio fica de fora desta lista.
        "membros" to setOf(LocalAtaque.BRACO, LocalAtaque.PERNA),
        "corpo" to setOf(LocalAtaque.TORSO, LocalAtaque.INGLE, LocalAtaque.PESCOCO),
        "cabeca" to setOf(LocalAtaque.CRANIO, LocalAtaque.ROSTO),
        "cranio" to setOf(LocalAtaque.CRANIO),
        "crnio" to setOf(LocalAtaque.CRANIO),
        "pescoco" to setOf(LocalAtaque.PESCOCO),
        "pescoo" to setOf(LocalAtaque.PESCOCO),
        "rosto" to setOf(LocalAtaque.ROSTO),
        "olhos" to setOf(LocalAtaque.OLHO),
        "olho" to setOf(LocalAtaque.OLHO),
        "tronco" to setOf(LocalAtaque.TORSO),
        "torso" to setOf(LocalAtaque.TORSO),
        "virilha" to setOf(LocalAtaque.INGLE),
        "bracos" to setOf(LocalAtaque.BRACO),
        "braco" to setOf(LocalAtaque.BRACO),
        "pernas" to setOf(LocalAtaque.PERNA),
        "perna" to setOf(LocalAtaque.PERNA),
        "maos" to setOf(LocalAtaque.MAO),
        "mao" to setOf(LocalAtaque.MAO),
        "pes" to setOf(LocalAtaque.PE),
        "pe" to setOf(LocalAtaque.PE)
    )

    private fun semAcento(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase()
            .trim()

    /**
     * Traduz o texto do catálogo para os locais do corpo.
     *
     * Aceita listas separadas por vírgula, " e " ou "/" — *"virilha, pernas"*,
     * *"braços e pernas"*.
     */
    fun locaisDe(localRaw: String?): Set<LocalAtaque> {
        val texto = semAcento(localRaw.orEmpty())
        if (texto.isBlank()) return emptySet()
        val pedacos = texto.split(",", ";", "/", " e ").map { it.trim() }.filter { it.isNotBlank() }
        val achados = mutableSetOf<LocalAtaque>()
        pedacos.forEach { pedaco ->
            // A primeira entrada que casa ganha; por isso os compostos vêm antes.
            VOCABULARIO.firstOrNull { (palavra, _) -> pedaco == palavra || pedaco.contains(palavra) }
                ?.let { achados += it.second }
        }
        return achados
    }

    /**
     * ⚠️ Quem protege o tronco protege os **vitais** — eles ficam lá dentro.
     *
     * Fora isso, o local pedido tem que estar na lista da peça.
     */
    fun cobre(localRaw: String?, local: LocalAtaque): Boolean {
        val locais = locaisDe(localRaw)
        if (local == LocalAtaque.VITAIS) return LocalAtaque.TORSO in locais
        return local in locais
    }

    /** A RD de uma peça, já separada do jeito que o catálogo escreve. */
    data class Rd(
        val principal: Int,
        val secundaria: Int?,
        val flexivel: Boolean,
        val frontalSomente: Boolean,
        val adicional: Boolean,
        val raw: String
    ) {
        val dividida: Boolean get() = secundaria != null
    }

    /**
     * Lê o `rdRaw` do catálogo: `"4"`, `"1*"`, `"4/2*"`, `"5D"`, `"+20"`.
     *
     * - `*` = **flexível** — não impede o trauma por impacto (MB p.379).
     * - `a/b` = RD **dividida**: `a` na frente, `b` atrás. O app usa a principal e
     *   mostra a outra, porque de que lado o golpe entrou é informação do Mestre.
     * - `D` = só **na frente**.
     * - `+N` = RD **adicional**, para vestir por cima de outra peça.
     */
    fun rdDe(rdRaw: String?): Rd? {
        val bruto = rdRaw?.trim().orEmpty()
        if (bruto.isBlank()) return null
        val numeros = Regex("\\d+").findAll(bruto).map { it.value.toInt() }.toList()
        if (numeros.isEmpty()) return null
        return Rd(
            principal = numeros[0],
            secundaria = numeros.getOrNull(1),
            flexivel = bruto.contains('*'),
            frontalSomente = bruto.contains('D', ignoreCase = false),
            adicional = bruto.startsWith("+"),
            raw = bruto
        )
    }

    /** Uma peça vestida que conta no local escolhido. */
    data class Peca(val nome: String, val rd: Rd)

    /**
     * A RD total no local, somando as peças vestidas.
     *
     * ⚠️ **Somar camadas é decisão do Mestre.** O livro não deixa empilhar
     * armadura à vontade — a `+N` existe justamente porque a maioria das peças
     * *não* soma. O app soma e **mostra as peças**, para o Mestre poder cortar o
     * que não vale. Esconder a conta seria pior.
     */
    fun rdTotal(pecas: List<Peca>): Int = pecas.sumOf { it.rd.principal }
}
