package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.PericiaDefinicao
import com.gurps.ficha.model.Personagem

/**
 * **Talento Instintivo** (MB p.92) — fazer o que não sabe fazer.
 *
 * > Uma vez a cada sessão de jogo **por nível** desta vantagem, ele pode tentar
 * > fazer um teste contra **qualquer perícia**, utilizando o valor do **atributo
 * > apropriado**: IQ para as perícias baseadas em IQ, DX para as baseadas em DX e
 * > assim por diante. Ele **não sofre nenhuma penalidade** pela utilização do
 * > valor predefinido (…). O nível tecnológico é irrelevante: um monge com NT3
 * > pode fazer um teste de IQ para utilizar Programação de Computadores/NT12!
 * >
 * > Esta vantagem **não surte efeito nas perícias que o personagem já conhece**.
 *
 * ## O que a vantagem faz, em uma frase
 *
 * Ela **apaga a penalidade** de usar uma perícia que não está na ficha: o teste
 * passa a ser contra o atributo cheio.
 *
 * Analogia: é como entrar num carro que você nunca dirigiu e mesmo assim dirigir
 * como se conhecesse o modelo. Não é ficar melhor — é deixar de ser pior.
 *
 * ## Por que ela era impossível e ficou possível
 *
 * Eu havia registrado no plano que ela dependia de o app calcular o **valor
 * predefinido** de cada perícia. Fui conferir: o campo `preDefinicoes` existe no
 * catálogo e está **vazio nas 281 perícias** — nenhum dado.
 *
 * Mas a vantagem **não precisa do predefinido**: ela o substitui pelo atributo.
 * O que ela precisa é da lista de perícias que o personagem **não tem** e do
 * **atributo base** de cada uma, e essas duas coisas o catálogo já dá.
 *
 * ## O contador
 *
 * ⚠️ **O app não sabe quando a sessão começou** — e não tem como saber. Então o
 * contador é honesto: soma os usos e tem um botão de **zerar**, em vez de tentar
 * adivinhar meia-noite ou tempo de tela. Chutar o começo da sessão devolveria
 * usos que o jogador já gastou.
 *
 * Kotlin puro e testável.
 */
object TalentoInstintivoRules {

    const val ID = "talento_instintivo"

    /** Níveis na ficha — e o nível É o número de usos por sessão. */
    fun usosPorSessao(personagem: Personagem): Int =
        personagem.vantagensTotais
            .filter { it.definicaoId == ID }
            .sumOf { it.nivel.coerceAtLeast(1) }

    /** Se a ficha tem a vantagem. */
    fun tem(personagem: Personagem): Boolean = usosPorSessao(personagem) > 0

    /** Quantos usos ainda sobram. */
    fun usosRestantes(personagem: Personagem, jaUsados: Int): Int =
        (usosPorSessao(personagem) - jaUsados).coerceAtLeast(0)

    /** Uma perícia que o personagem não tem, pronta para a lista da tela. */
    data class Opcao(
        val id: String,
        val nome: String,
        val atributo: String,
        val nh: Int
    )

    /**
     * As perícias que o personagem **não** tem, com o NH do atributo base.
     *
     * ⚠️ Filtra as que ele **já conhece** porque o livro é explícito: *"Esta
     * vantagem não surte efeito nas perícias que o personagem já conhece"*.
     * Oferecê-las seria oferecer um NH pior que o que ele já tem.
     *
     * A comparação é pelo **id** e pelo **nome**, porque perícia racial entra na
     * ficha com o id prefixado (`racial_rastreamento`) e casaria só pelo nome.
     */
    fun opcoesDe(
        personagem: Personagem,
        catalogo: List<PericiaDefinicao>
    ): List<Opcao> {
        val conhecidasPorId = personagem.periciasTotais
            .map { it.definicaoId.lowercase().removePrefix("racial_") }.toSet()
        val conhecidasPorNome = personagem.periciasTotais
            .map { it.nome.trim().lowercase() }.toSet()

        return catalogo
            .filter { def ->
                def.id.lowercase() !in conhecidasPorId &&
                    def.nome.trim().lowercase() !in conhecidasPorNome
            }
            .map { def ->
                val atributo = def.atributoBase.uppercase().ifBlank { "IQ" }
                Opcao(
                    id = def.id,
                    nome = def.nome,
                    atributo = atributo,
                    nh = personagem.getAtributo(atributo)
                )
            }
            .sortedBy { it.nome }
    }

    /** O rótulo do botão, com os usos restantes à vista. */
    fun rotulo(personagem: Personagem, jaUsados: Int): String {
        val restantes = usosRestantes(personagem, jaUsados)
        val total = usosPorSessao(personagem)
        return "Talento Instintivo ($restantes de $total nesta sessão)"
    }

    /** O rótulo de uma linha da lista. */
    fun rotuloDaOpcao(opcao: Opcao): String =
        "${opcao.nome} — rolar ${opcao.atributo} ${opcao.nh}"

    /** O mesmo, para o TalkBack. */
    fun descricaoAcessivel(opcao: Opcao): String =
        "${opcao.nome}. Rolar contra ${opcao.atributo} ${opcao.nh}, " +
            "sem penalidade por não conhecer a perícia."

    /** A ressalva que precisa estar na tela. */
    const val AVISO =
        "Só vale para perícia que o personagem NÃO tem, e gasta um uso da sessão. " +
            "O NT é irrelevante (MB p.92)."
}
