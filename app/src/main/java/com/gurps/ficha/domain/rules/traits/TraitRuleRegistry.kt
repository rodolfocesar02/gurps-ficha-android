package com.gurps.ficha.domain.rules.traits

import com.gurps.ficha.model.Personagem

/**
 * Registro central de regras de vantagens.
 * Permite localizar rapidamente qual regra se aplica a cada ID.
 */
object TraitRuleRegistry {
    private val rules = mutableMapOf<String, TraitRule>()

    init {
        // Registro de REGRAS COMPLEXAS (Alfabeto e migrações atuais)
        register(AtaqueInatoRule())
        register(GolpeadoresRule())
        register(DentesRule())
        register(FlexibilidadeRule())
        register(GarrasRule())
        register(ApararAmpliadoRule())
        register(BloqueioAmpliadoRule())
        register(EsquivaAmpliadaRule())
        register(MestreDeArmasRule())
        register(TelecomunicacaoRule())
        register(IdiomaRule())
        // Custo = preço-por-braço × níveis. Ver `BracalCustoRules.kt`.
        register(StBracalRule())
        register(DxBracalRule())
    }

    /**
     * Vantagens E desvantagens.
     *
     * Antes do Lote D-0 os agregadores varriam só `personagem.vantagens`: uma
     * regra registrada com id de desvantagem nunca era chamada — sem erro, sem
     * aviso. Isso bloqueava toda a automação de desvantagem.
     */
    private fun todosOsTracos(personagem: Personagem): List<TracoSelecionado> =
        personagem.vantagensTotais + personagem.desvantagensTotais

    private fun register(rule: TraitRule) {
        rules[rule.traitId] = rule
    }

    /**
     * Regra que vale para o traço: primeiro a classe Kotlin registrada; na
     * falta dela, os `efeitos` declarados no catálogo (arquitetura híbrida).
     *
     * PRECEDÊNCIA: Kotlin vence JSON. Isso permite migrar um caso de
     * declarativo para código sem precisar apagar o JSON — e o interpretador
     * avisa no log quando encontra os dois.
     */
    fun getRuleFor(traitId: String): TraitRule? {
        return rules[traitId] ?: EfeitoInterpretador.regraPara(traitId)
    }

    /**
     * Versão que sabe de qual catálogo o traço veio.
     *
     * Seis ids existem em vantagens E desvantagens (Aparência, Reputação,
     * Riqueza, Status, Destino, Forma de Sombras). Buscar só pelo id daria os
     * efeitos do lado errado da escala. Todos os agregadores daqui usam esta,
     * porque todos partem de uma seleção da ficha; a versão só-com-id continua
     * para quem tem apenas o id em mãos (cálculo de custo, opções de ataque).
     */
    fun getRuleFor(selection: TracoSelecionado): TraitRule? =
        rules[selection.definicaoId]
            ?: EfeitoInterpretador.regraPara(selection.definicaoId, selection.ehDesvantagem)

    /**
     * Retorna se a vantagem tem regras complexas/especiais.
     */
    fun hasSpecialRule(traitId: String): Boolean = rules.containsKey(traitId)

    /**
     * Retorna a soma de bônus em perícia vindo de todas as vantagens do personagem.
     */
    /**
     * Soma o bônus de um atributo vindo de todos os traços (GANCHO-A).
     *
     * Consumido por `AtributoBonusRules`, que protege contra recursão — chamar
     * este método direto de dentro de `Personagem` pularia essa proteção.
     */
    fun getAttributeBonus(personagem: Personagem, atributo: Atributo): Int =
        todosOsTracos(personagem).sumOf { selection ->
            getRuleFor(selection)
                ?.getAttributeModifiers(personagem, selection)
                ?.get(atributo) ?: 0
        }

    /**
     * Bônus condicionais que podem valer para [alvo] — perícia ou defesa.
     *
     * São oferecidos ao jogador na hora da rolagem, em vez de somados no NH:
     * "aplicar +1 de Rosto Sincero?". Aplicar sempre seria errado, porque a
     * condição pode não valer no teste em questão.
     */
    fun getBonusCondicionais(personagem: Personagem, alvo: String): List<BonusCondicional> =
        todosOsTracos(personagem).flatMap { selection ->
            getRuleFor(selection)
                ?.getBonusCondicionais(personagem, selection)
                ?.filter { casaAlvoCondicional(it.alvo, alvo) }
                .orEmpty()
        }

    /**
     * Curinga de alvo: **qualquer perícia** (Lote TAL-1).
     *
     * Existe porque três vantagens do livro não dão uma lista de perícias — dão
     * uma **situação**: Toque Sensível vale em *"qualquer tarefa que utiliza o
     * tato"*, Venturoso em *"qualquer teste"* de risco desnecessário, Versátil em
     * *"qualquer tarefa que exija criatividade"*.
     *
     * Enumerar as 278 perícias do catálogo seria absurdo e ficaria errado no dia
     * seguinte. Com o curinga, a caixinha aparece em **toda** perícia e quem
     * decide se vale é o Mestre — que é exatamente o que a caixinha existe para
     * resolver.
     */
    const val CURINGA_PERICIA = "*"

    /**
     * Alvos que **não** são perícia — o curinga não vale para eles.
     *
     * ⚠️ Sem esta lista o `*` do Venturoso apareceria também na Esquiva e no
     * teste de reação. O livro fala de *"testes de habilidade"*: defesa ativa e
     * reação não entram, e um +1 indevido na Esquiva é o tipo de erro que passa
     * despercebido porque parece plausível.
     */
    private val ALVOS_QUE_NAO_SAO_PERICIA = setOf(
        "esquiva", "apara", "aparar", "bloqueio", "bloquear", "reacao", "reação"
    )

    private fun casaAlvoCondicional(alvoDoEfeito: String, alvoPedido: String): Boolean {
        if (alvoDoEfeito.equals(alvoPedido, ignoreCase = true)) return true
        return alvoDoEfeito.trim() == CURINGA_PERICIA &&
            alvoPedido.trim().lowercase() !in ALVOS_QUE_NAO_SAO_PERICIA
    }

    /** De onde veio um pedaço do bônus: o traço que o concedeu e quanto. */
    data class OrigemDeBonus(val nomeDoTraco: String, val valor: Int)

    /**
     * As origens do bônus de uma perícia, em vez do total somado.
     *
     * Existe para a ficha poder EXPLICAR o número: sem isso, o NH da Escalada
     * pula de 12 para 14 e nada na tela diz por quê — a automação vira caixa
     * preta e o jogador perde como conferir se está certa.
     *
     * Só devolve quem realmente contribuiu (valor != 0), na ordem em que os
     * traços estão na ficha.
     */
    fun getSkillBonusOrigens(personagem: Personagem, skillName: String): List<OrigemDeBonus> =
        todosOsTracos(personagem).mapNotNull { selection ->
            val valor = getRuleFor(selection)
                ?.getSkillModifiers(personagem, selection)
                ?.get(skillName) ?: 0
            if (valor != 0) OrigemDeBonus(selection.nome, valor) else null
        }

    /**
     * As origens do bônus de dano por dado, em vez do total somado.
     *
     * Irmã de [getSkillBonusOrigens], para a arma poder EXPLICAR o número: sem
     * isto a Faca passa de `1d-3` para `2d-1` e nada na tela diz que foi o
     * Mestre de Armas. Automação que o jogador não consegue conferir é caixa
     * preta — foi a decisão C6 do plano, aplicada agora às armas (Lote NOTA-2).
     */
    fun getDamageBonusOrigens(
        personagem: Personagem,
        periciaId: String?,
        weaponName: String? = null,
        armaGrupo: String? = null
    ): List<OrigemDeBonus> =
        todosOsTracos(personagem).mapNotNull { selection ->
            val valor = getRuleFor(selection)
                ?.getDamageBonusPerDie(personagem, selection, periciaId, weaponName, armaGrupo) ?: 0
            if (valor != 0) OrigemDeBonus(selection.nome, valor) else null
        }

    fun getSkillBonus(personagem: Personagem, skillName: String): Int {
        var total = 0
        todosOsTracos(personagem).forEach { selection ->
            val rule = getRuleFor(selection)
            if (rule != null) {
                val bonuses = rule.getSkillModifiers(personagem, selection)
                val bonus = bonuses[skillName] ?: 0
                total += bonus
            }
        }
        return total
    }

    /**
     * Retorna a soma de bônus em Aparar (Parry) vindo de todas as vantagens do personagem.
     */
    fun getParryBonus(personagem: Personagem, periciaId: String?): Int {
        var total = 0
        todosOsTracos(personagem).forEach { selection ->
            val rule = getRuleFor(selection)
            if (rule != null) {
                total += rule.getParryModifier(personagem, selection, periciaId)
            }
        }
        return total
    }

    /**
     * Retorna a soma de bônus em Esquiva (Dodge) vindo de todas as vantagens do personagem.
     */
    fun getDodgeBonus(personagem: Personagem): Int {
        var total = 0
        todosOsTracos(personagem).forEach { selection ->
            val rule = getRuleFor(selection)
            if (rule != null) {
                total += rule.getDodgeModifier(personagem, selection)
            }
        }
        return total
    }

    /**
     * Retorna a soma de bônus em Bloqueio (Block) vindo de todas as vantagens do personagem.
     */
    fun getBlockBonus(personagem: Personagem): Int {
        var total = 0
        todosOsTracos(personagem).forEach { selection ->
            val rule = getRuleFor(selection)
            if (rule != null) {
                total += rule.getBlockModifier(personagem, selection)
            }
        }
        return total
    }

    /**
     * Retorna a soma de bônus de dano por dado (ex: Mestre de Armas) acumulado.
     */
    fun getDamageBonusPerDie(
        personagem: Personagem,
        periciaId: String?,
        weaponName: String? = null,
        armaGrupo: String? = null
    ): Int {
        var total = 0
        todosOsTracos(personagem).forEach { selection ->
            val rule = getRuleFor(selection)
            if (rule != null) {
                total += rule.getDamageBonusPerDie(personagem, selection, periciaId, weaponName, armaGrupo)
            }
        }
        return total
    }
}
