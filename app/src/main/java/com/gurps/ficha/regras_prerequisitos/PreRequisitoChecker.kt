package com.gurps.ficha.regras_prerequisitos

/**
 * Futuro responsável por verificar se um personagem satisfaz os
 * [PreRequisitoType] gerados pelo parser acima.
 *
 * Esta classe/objeto ainda é um stub; atualmente está vazia e não é usada
 * em nenhum lugar do código. Quando integrado, ele receberá uma representação
 * do personagem (talvez a própria classe `Personagem` ou um DTO leve) e uma
 * lista de pré-requisitos, e retornará `true`/`false` ou um relatório detalhado.
 *
 * Exemplo de interface futuro:
 * ```kotlin
 * fun check(character: Personagem, prereqs: List<PreRequisitoType>): Boolean
 * ```
 *
 * Além disso, poderão existir variantes para diferentes tipos de magia (mágicas
 * básicas versus de escola, por exemplo) ou para outras entidades além de magias.
 */
object PreRequisitoChecker {

    /**
     * Verifica se o objeto `character` cumpre todos os requisitos.
     * Por enquanto apenas retorna `false` e registra um comentário.
     */
    fun check(character: Any /* TODO: substituir por classe real */,
              requisitos: List<PreRequisitoType>): Boolean {
        // TODO: implementar lógica de verificação usando atributos do `character`
        // exemplos de verificações necessárias no futuro:
        //   - if requisito é AptidaoMagica(n), consultar vantagem do personagem
        //   - if requisito é AttributeMin("IQ",12), comparar atributo IQ
        //   - if requisito é MagiasEscola(q,e), contar magias da escola
        println("[PreRequisitoChecker] chamado com $requisitos (não implementado)")
        return false
    }

    // TODO: adicionar helpers como `hasAttributeMin`, `knowsSpell`,
    // `hasAptidaoMagica(nivel: Int)`, etc.

    /**
     * Função de teste localizada que aceita um mapa simples representando um
     * personagem e uma lista de requisitos. Retorna um relatório textual
     * explicando quais requisitos estão faltando ou se todos foram satisfeitos.
     *
     * Este método NÃO é usado pelo aplicativo; serve apenas para exemplificar o
     * futuro comportamento e facilitar testes manuais isolados.
     */
    fun checkSimples(personagem: Map<String, Any>,
                     requisitos: List<PreRequisitoType>): String {
        val faltando = mutableListOf<String>()
        for (r in requisitos) {
            when (r) {
                is PreRequisitoType.AttributeMin -> {
                    val atual = personagem[r.atributo] as? Int ?: 0
                    if (atual < r.minimo) {
                        faltando.add("${r.atributo} >= ${r.minimo} (atual $atual)")
                    }
                }
                is PreRequisitoType.AptidaoMagica -> {
                    val nivel = personagem["aptidao_magica"] as? Int ?: 0
                    if (nivel < r.nivel) {
                        faltando.add("Aptidão Mágica >= ${r.nivel} (atual $nivel)")
                    }
                }
                is PreRequisitoType.MagiasEscola -> {
                    // aqui só verificamos se existe campo de contagem
                    val q = personagem["magias_${r.escola.lowercase()}"] as? Int ?: 0
                    if (q < r.quantidade) {
                        faltando.add("${r.quantidade} magias de ${r.escola} (atual $q)")
                    }
                }
                is PreRequisitoType.MagiaConhecida -> {
                    val conhecidas = personagem["magias_conhecidas"] as? Set<String> ?: emptySet()
                    if (r.nomeMagia !in conhecidas) {
                        faltando.add("Magia conhecida: ${r.nomeMagia}")
                    }
                }
                else -> {
                    faltando.add("Tipo de requisito não suportado: $r")
                }
            }
        }
        return if (faltando.isEmpty()) "todos requisitos atendidos" else "faltando: ${faltando.joinToString(", ")}"
    }
}
