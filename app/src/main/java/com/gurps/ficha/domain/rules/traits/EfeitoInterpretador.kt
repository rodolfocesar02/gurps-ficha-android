package com.gurps.ficha.domain.rules.traits

import android.util.Log
import com.gurps.ficha.domain.rules.CharacterRules
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada

/**
 * Transforma o campo `efeitos` do catálogo numa [TraitRule], sem escrever uma
 * classe Kotlin por vantagem.
 *
 * Metade da arquitetura híbrida: bônus simples viram DADO no JSON e passam por
 * aqui; casos complexos (Ataque Inato, Aliados, Garras) continuam sendo classe
 * própria. Quem tem os dois, a classe Kotlin vence — ver
 * [TraitRuleRegistry.getRuleFor].
 *
 * Fecha um buraco silencioso: os agregadores do Registry fazem
 * `rules[definicaoId]` e ignoram quem não está no mapa. Antes disto, vantagem
 * sem classe Kotlin simplesmente não produzia efeito nenhum, sem erro.
 *
 * Diagnóstico: filtrar o Logcat por `EfeitoInterpretador`.
 */
object EfeitoInterpretador {

    private const val TAG = "EfeitoInterpretador"

    /**
     * Regra construída a partir dos `efeitos` declarados para [traitId], ou
     * null quando o traço não declara nenhum.
     *
     * Busca no catálogo de vantagens e, se não achar, no de desvantagens — o
     * mesmo id nunca existe nos dois.
     */
    fun regraPara(traitId: String): TraitRule? {
        if (traitId.isBlank()) return null
        val repo = CharacterRules.DATA_REPOSITORY_INSTANCE ?: return null
        val efeitos = repo.getVantagemPorId(traitId)?.efeitos?.takeIf { it.isNotEmpty() }
            ?: repo.getDesvantagemPorId(traitId)?.efeitos?.takeIf { it.isNotEmpty() }
            ?: return null
        return regraDe(traitId, efeitos)
    }

    /**
     * Monta a regra a partir de efeitos já em mãos, sem passar pelo catálogo.
     *
     * Separado de [regraPara] para o teste poder exercitar a interpretação sem
     * precisar de um `DataRepository` (que exige Context do Android). É aqui
     * que mora a lógica; [regraPara] só faz a busca.
     */
    internal fun regraDe(traitId: String, efeitos: List<EfeitoDeclarado>): TraitRule =
        RegraDeclarativa(traitId, efeitos)

    /**
     * Regra genérica que aplica os efeitos declarados.
     *
     * Ignora, com aviso no log, o que ainda não sabe tratar — nunca aplica pela
     * metade nem chuta. É preferível não dar o bônus a dar o bônus errado.
     */
    private class RegraDeclarativa(
        override val traitId: String,
        private val efeitos: List<EfeitoDeclarado>
    ) : TraitRule {

        override fun getSkillModifiers(
            personagem: Personagem,
            selection: VantagemSelecionada
        ): Map<String, Int> {
            val mapa = mutableMapOf<String, Int>()
            efeitos.filter { it.tipoResolvido == TipoEfeito.PERICIA }.forEach { efeito ->
                if (!aplicavel(efeito)) return@forEach
                if (efeito.alvo.isBlank()) {
                    Log.w(TAG, "$traitId: efeito de pericia sem alvo, ignorado")
                    return@forEach
                }
                // Soma quando o mesmo alvo aparece mais de uma vez.
                mapa[efeito.alvo] = (mapa[efeito.alvo] ?: 0) + efeito.valorPara(selection.nivel)
            }
            return mapa
        }

        override fun getDodgeModifier(personagem: Personagem, selection: VantagemSelecionada): Int =
            somaDefesa(selection, ALVO_ESQUIVA)

        override fun getBlockModifier(personagem: Personagem, selection: VantagemSelecionada): Int =
            somaDefesa(selection, ALVO_BLOQUEIO)

        override fun getParryModifier(
            personagem: Personagem,
            selection: VantagemSelecionada,
            periciaId: String?
        ): Int = somaDefesa(selection, ALVO_APARAR)

        private fun somaDefesa(selection: VantagemSelecionada, alvos: Set<String>): Int =
            efeitos.filter { it.tipoResolvido == TipoEfeito.DEFESA }
                .filter { aplicavel(it) && it.alvo.trim().lowercase() in alvos }
                .sumOf { it.valorPara(selection.nivel) }

        /**
         * Um efeito só é aplicado quando é incondicional e global.
         *
         *  - CONDICIONAL ("ao tentar parecer honesto") não pode entrar no NH
         *    base: valeria sempre e inflaria a ficha. Vai virar opção marcável
         *    na hora da rolagem (Lote V-5).
         *  - ESCOPO por membro (+1 ST só dos braços) ainda não tem como entrar
         *    no cálculo — falta decidir como um bônus por membro se aplica.
         */
        private fun aplicavel(efeito: EfeitoDeclarado): Boolean = when {
            efeito.ehCondicional -> {
                Log.d(TAG, "$traitId: '${efeito.alvo}' e condicional (${efeito.condicao}), fora do NH base")
                false
            }
            efeito.escopoResolvido != EscopoEfeito.GLOBAL -> {
                Log.d(TAG, "$traitId: '${efeito.alvo}' tem escopo ${efeito.escopoResolvido}, ainda nao suportado")
                false
            }
            efeito.tipoResolvido == null -> {
                Log.w(TAG, "$traitId: tipo '${efeito.tipo}' desconhecido, ignorado")
                false
            }
            else -> true
        }

        private companion object {
            val ALVO_ESQUIVA = setOf("esquiva")
            val ALVO_APARAR = setOf("aparar", "apara")
            val ALVO_BLOQUEIO = setOf("bloqueio", "bloquear")
        }
    }
}
