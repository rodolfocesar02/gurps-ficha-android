package com.gurps.ficha.regras_prerequisitos

/**
 * Tipos de pré-requisitos possíveis que uma magia (ou outra regra) pode
 * exigir. Esta enumeração/selada representa as categorias reconhecidas pelo
 * motor de regras.
 *
 * No futuro, quando o parser estiver pronto, as instâncias criadas a partir do
 * texto serão convertidas para esses tipos, possivelmente contendo parâmetros
 * adicionais (valor mínimo de atributo, nome da magia requerida, etc.).
 *
 * Exemplo de uso futuro:
 * ```
 * val prereq: PreRequisito = PreRequisitoType.AttributeMin("IQ", 12)
 * ```
 *
 * Atualmente esta classe serve apenas como esboço; campos específicos
 * devem ser adicionados conforme surgirem os requisitos reais.
 */
sealed class PreRequisitoType {

    /** Precisa de um atributo com valor mínimo. */
    data class AttributeMin(val atributo: String, val minimo: Int) : PreRequisitoType()

    /** Vantagem Aptidão Mágica com certo nível. */
    data class AptidaoMagica(val nivel: Int) : PreRequisitoType()

    /** Requer outra magia já conhecida. */
    data class MagiaConhecida(val nomeMagia: String) : PreRequisitoType()

    /** Requer um número x de magias de certa escola/elemento. */
    data class MagiasEscola(val quantidade: Int, val escola: String) : PreRequisitoType()

    /** Requer nível de personagem mínimo. */
    data class NivelMin(val nivel: Int) : PreRequisitoType()

    // TODO: adicionar outros tipos de requisito observados no material GURPS.
}
