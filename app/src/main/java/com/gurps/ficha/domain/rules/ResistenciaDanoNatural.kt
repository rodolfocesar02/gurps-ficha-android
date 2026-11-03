package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem

/**
 * **Resistência a Dano do próprio corpo** — MB p.83. Lote POD-30.
 *
 * ## 🔴 O que faltava
 *
 * A vantagem estava no catálogo desde sempre (`resistencia_a_dano`, 5 pontos por
 * nível) e **não fazia nada**. Quem comprava RD 5 pagava 25 pontos e continuava
 * levando o dano inteiro no diálogo de ferimento, que só somava a armadura
 * vestida.
 *
 * O livro é direto sobre onde ela entra:
 *
 * > *"Subtraia esse valor do dano causado por qualquer ataque físico ou de
 * > energia **depois de aplicar a RD de armaduras artificiais**."*
 *
 * Ou seja: ela **soma** com a armadura, não substitui.
 *
 * ## Por que ela chegou pelo pilar Poderes
 *
 * É a habilidade que o livro de *Poderes* mais pede entre as que têm efeito
 * mecânico direto: **23 vezes** nos 567 exemplos dos 47 poderes. Mas a regra é
 * geral — vale igual para quem compra RD sem poder nenhum, e é por isso que
 * este arquivo mora em `domain/rules/` e não em `domain/rules/poderes/`.
 *
 * ## ⚠️ O que este arquivo NÃO decide
 *
 * Se a RD **vale contra aquele ataque**. Uma RD com *Limitada (só contra fogo)*
 * ou *Parcial* não protege de tudo, e o app não tem como saber o que está
 * chegando. Por isso os modificadores viram **ressalva na tela**: o número
 * aparece somado, e a ressalva ao lado, para o jogador decidir. Travar seria
 * repetir o erro do `conhecimento_oculto`; somar em silêncio seria pior ainda.
 */
object ResistenciaDanoNatural {

    /** O id da vantagem no `vantagens.v3.json`. */
    const val ID = "resistencia_a_dano"

    /**
     * De onde veio cada pedaço da RD natural.
     *
     * ⚠️ Uma lista, e não um número só: o personagem pode ter RD comprada com
     * pontos **e** RD da raça, e quem lê a ficha precisa saber qual é qual —
     * ainda mais quando uma delas tem ressalva e a outra não.
     */
    data class Fonte(
        val nome: String,
        val rd: Int,
        /** Os modificadores que podem restringir esta RD. Vazio quando não há. */
        val ressalva: String
    )

    /**
     * As RDs naturais do personagem, incluindo as que vêm da raça e as que são
     * habilidade de um poder.
     *
     * ⚠️ `vantagensTotais` de propósito: uma raça com couraça tem de contar
     * igual a quem comprou a vantagem com pontos. Foi o mesmo critério do
     * agregador de ataques.
     */
    fun fontes(personagem: Personagem): List<Fonte> =
        personagem.vantagensTotais
            .filter { it.definicaoId == ID }
            .mapNotNull { v ->
                val rd = v.nivel.coerceAtLeast(0)
                if (rd == 0) null else Fonte(
                    nome = v.nome,
                    rd = rd,
                    ressalva = v.modificadores.joinToString(", ") { it.nome }
                )
            }

    /** Quanto a RD natural desconta, somando todas as fontes. */
    fun total(personagem: Personagem): Int = fontes(personagem).sumOf { it.rd }

    /**
     * A linha que explica a soma, quando há RD natural.
     *
     * Null quando não há nenhuma — nada de linha vazia dizendo "RD natural 0".
     */
    fun explicar(personagem: Personagem): String? {
        val fontes = fontes(personagem)
        if (fontes.isEmpty()) return null
        val detalhe = fontes.joinToString(" · ") { f ->
            f.nome + " " + f.rd + (if (f.ressalva.isBlank()) "" else " (${f.ressalva})")
        }
        return buildString {
            append("RD natural ${total(personagem)}, somada à da armadura (p.83): ")
            append(detalhe)
            if (fontes.any { it.ressalva.isNotBlank() }) {
                append(". Confira se os modificadores valem contra este ataque.")
            }
        }
    }

    // == TR-3 (plano dos traços da Mesa): a RD natural POR LOCAL ==========================================

    /** A Membrana Nictitante: RD 1 por nível em cada olho, e só no olho (MB p.69). */
    const val ID_MEMBRANA = "membrana_nictitante"

    /**
     * A RD natural de UM local, já resolvida — a mesma conta do `rd-natural.js` da Mesa.
     *
     * @property flexivel quanto dessa RD é Pele Resistente ou Flexível: deixa passar o trauma por impacto (MB p.379)
     */
    data class NoLocal(val rd: Int, val flexivel: Int, val explicacao: String?)

    private fun semAcento(s: String): String =
        java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "").lowercase()

    /** Os locais que a descrição de uma RD Parcial pode nomear; "o Tronco" protege também os órgãos vitais (MB p.83). */
    private val LOCAIS_DA_PARCIAL: List<Pair<Regex, List<LocalAtaque>>> = listOf(
        Regex("\\b(tronco|torso)\\b") to listOf(LocalAtaque.TORSO, LocalAtaque.VITAIS),
        Regex("\\bvitais\\b") to listOf(LocalAtaque.VITAIS),
        Regex("\\bcranio\\b") to listOf(LocalAtaque.CRANIO),
        Regex("\\brosto\\b") to listOf(LocalAtaque.ROSTO),
        Regex("\\bpescoco\\b") to listOf(LocalAtaque.PESCOCO),
        Regex("\\bvirilha\\b") to listOf(LocalAtaque.INGLE),
        Regex("\\bbracos?\\b") to listOf(LocalAtaque.BRACO),
        Regex("\\bpernas?\\b") to listOf(LocalAtaque.PERNA),
        Regex("\\bmaos?\\b") to listOf(LocalAtaque.MAO),
        Regex("\\bpes?\\b") to listOf(LocalAtaque.PE),
        Regex("\\bolhos?\\b") to listOf(LocalAtaque.OLHO)
    )

    /** Os locais que uma RD Parcial protege, lidos da descrição ("Apenas o Crânio"); `null` quando não reconhece nenhum. */
    fun locaisDaParcial(descricao: String): Set<LocalAtaque>? {
        val d = semAcento(descricao)
        val locais = LOCAIS_DA_PARCIAL.filter { it.first.containsMatchIn(d) }.flatMap { it.second }.toSet()
        return locais.ifEmpty { null }
    }

    private fun temMod(v: com.gurps.ficha.model.VantagemSelecionada, re: Regex): Boolean =
        v.modificadores.any { re.containsMatchIn(semAcento(it.id)) || re.containsMatchIn(semAcento(it.nome)) }

    /**
     * **A RD natural naquele local** (MB p.83). Cada entrada vale por si: o Minotauro (RD 2 "Apenas o Crânio" + RD 3)
     * leva 5 no crânio e 3 no resto. Não protege o olho, a não ser com Campo de Força ou Parcial dos olhos. Limitada
     * e Direcional somam com a ressalva escrita, porque o app não sabe de onde vem o golpe.
     */
    fun noLocal(personagem: Personagem, local: LocalAtaque): NoLocal {
        var rd = 0
        var flexivel = 0
        val partes = mutableListOf<String>()
        val avisos = mutableListOf<String>()
        personagem.vantagensTotais.forEach { v ->
            val nivel = v.nivel.coerceAtLeast(0)
            if (nivel == 0) return@forEach
            if (v.definicaoId == ID_MEMBRANA) {
                if (local == LocalAtaque.OLHO) { rd += nivel; partes += "Membrana Nictitante $nivel" }
                return@forEach
            }
            if (v.definicaoId != ID) return@forEach
            val parcial = temMod(v, Regex("parcial"))
            val locais = if (parcial) locaisDaParcial(v.descricao) else null
            if (parcial && locais == null) {
                avisos += "RD natural $nivel Parcial sem local na ficha (${v.descricao}): confira"
                return@forEach
            }
            if (parcial && local !in locais!!) return@forEach
            if (local == LocalAtaque.OLHO && !parcial && !temMod(v, Regex("campo.de.forca"))) {
                avisos += "RD natural $nivel não protege o olho (MB p.83)"
                return@forEach
            }
            rd += nivel
            if (temMod(v, Regex("flexivel|pele.resistente"))) flexivel += nivel
            val ressalva = v.modificadores.filter { Regex("limitad|direcional").containsMatchIn(semAcento(it.id + " " + it.nome)) }
                .joinToString(", ") { it.nome }
            partes += "RD natural $nivel" + (if (parcial) " (${v.descricao})" else "") +
                (if (ressalva.isBlank()) "" else " ($ressalva: confira se vale contra este ataque)")
        }
        if (partes.isEmpty() && avisos.isEmpty()) return NoLocal(0, 0, null)
        val texto = buildString {
            if (partes.isNotEmpty()) append("RD natural $rd em ${local.rotulo}, somada à da armadura (MB p.83): " + partes.joinToString(" · "))
            if (avisos.isNotEmpty()) { if (isNotEmpty()) append(". "); append(avisos.joinToString(". ")) }
            if (flexivel > 0) append(". Pele Resistente ou Flexível: o trauma por impacto passa (MB p.379)")
        }
        return NoLocal(rd, flexivel, texto)
    }
}
