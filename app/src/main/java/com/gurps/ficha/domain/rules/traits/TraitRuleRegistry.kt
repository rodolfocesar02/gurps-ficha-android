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
        personagem.vantagens + personagem.desvantagens

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
            getRuleFor(selection.definicaoId)
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
            getRuleFor(selection.definicaoId)
                ?.getBonusCondicionais(personagem, selection)
                ?.filter { it.alvo.equals(alvo, ignoreCase = true) }
                .orEmpty()
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
            val valor = getRuleFor(selection.definicaoId)
                ?.getSkillModifiers(personagem, selection)
                ?.get(skillName) ?: 0
            if (valor != 0) OrigemDeBonus(selection.nome, valor) else null
        }

    fun getSkillBonus(personagem: Personagem, skillName: String): Int {
        var total = 0
        todosOsTracos(personagem).forEach { selection ->
            val rule = getRuleFor(selection.definicaoId)
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
            val rule = getRuleFor(selection.definicaoId)
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
            val rule = getRuleFor(selection.definicaoId)
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
            val rule = getRuleFor(selection.definicaoId)
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
            val rule = getRuleFor(selection.definicaoId)
            if (rule != null) {
                total += rule.getDamageBonusPerDie(personagem, selection, periciaId, weaponName, armaGrupo)
            }
        }
        return total
    }
}
