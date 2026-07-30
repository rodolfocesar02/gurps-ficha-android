package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.roll.CriticoRules
import com.gurps.ficha.model.AtributoBase
import com.gurps.ficha.model.PericiaSelecionada
import com.gurps.ficha.model.Personagem

/**
 * **Completamente Desastrado** (MB p.133) — o fracasso que vira falha crítica.
 *
 * > **Completamente Desastrado:** da mesma maneira que o caso anterior, mas
 * > **qualquer fracasso em um teste de DX ou em uma perícia com base em DX é
 * > considerado uma falha crítica.** −15 pontos.
 *
 * ## Por que esta é diferente de todas as outras
 *
 * Toda desvantagem automatizada até aqui muda **um número** — o NH, o alvo, a
 * reação. Esta não muda número nenhum: ela muda **o que o resultado significa**.
 * A rolagem que daria *"Falha por 2"* passa a mandar rolar na Tabela de Erro
 * Crítico, e o desfecho da cena muda junto.
 *
 * Analogia: as outras mexem no preço do produto; esta troca o produto por outro
 * depois que a compra já foi feita.
 *
 * É por isso que ela mora aqui, e não dentro do [CriticoRules]: a classificação
 * de crítico é regra do sistema, igual para todo mundo, e este é um caso
 * particular de **uma ficha**. Misturar os dois faria toda rolagem do app
 * consultar a ficha para saber o que é um 17.
 *
 * ## ⚠️ As três cercas que o livro põe, e que sem código somem
 *
 * 1. **Só DX.** Um fracasso em Teologia (IQ) ou em Corrida (HT) continua sendo
 *    fracasso comum. Reclassificar tudo dobraria a desvantagem de tamanho.
 * 2. **Só FRACASSO.** Um Sucesso Decisivo continua decisivo — a desvantagem não
 *    tem poder de estragar acerto.
 * 3. **Só o nível de −15 pontos.** O `desastrado` de −5 pontos *"não tem
 *    número"*: é o Mestre inventando trapalhadas na narrativa. Automatizá-lo
 *    seria inventar regra.
 *
 * ## O aviso no log
 *
 * Pedido do usuário na leitura: *"é possível colocar pra aparecer o erro na
 * jogada no discord, pra lembrar o mestre da desvantagem?"*. Sim — e é
 * importante que apareça, porque senão o Mestre vê uma falha crítica em um 12 e
 * acha que o app errou a conta. O [MOTIVO] explica de onde ela veio.
 */
object DesastradoRules {

    /** O id do nível de −15 pontos. O `desastrado` de −5 NÃO entra. */
    const val ID = "completamente_desastrado"

    /** A frase que acompanha a rolagem, para o Mestre não achar que é bug. */
    const val MOTIVO = "Completamente Desastrado: todo fracasso em DX é crítico (MB p.133)"

    /** Se a ficha tem a desvantagem — em vantagens, raça ou metacaracterística. */
    fun ativo(personagem: Personagem): Boolean =
        personagem.desvantagensTotais.any { it.definicaoId == ID }

    /**
     * Os rótulos de rolagem que a regra alcança, montados a partir da ficha.
     *
     * ⚠️ Recebe [rotuloDe] em vez de montar o texto aqui: o rótulo tem de ser
     * **exatamente** o mesmo que o diálogo de perícias usou, senão a comparação
     * falha em silêncio para toda perícia com especialização — "Faca" nunca
     * casaria com "Faca (Arremesso)". Quem sabe montar o rótulo é a UI, então
     * ela empresta a função.
     */
    fun rotulosDeBaseDX(
        pericias: List<PericiaSelecionada>,
        rotuloDe: (PericiaSelecionada) -> String
    ): Set<String> = pericias.filter { it.atributoBase == AtributoBase.DX }
        .map(rotuloDe)
        .toSet()

    /** O rótulo do atributo DX na aba Rolagem — é o código curto, como "PER". */
    const val ROTULO_ATRIBUTO_DX = "DX"

    /**
     * A classificação final, já com a desvantagem aplicada.
     *
     * Devolve [original] sem tocar em nada quando a regra não vale — que é o
     * caso da esmagadora maioria das rolagens.
     *
     * @param ehBaseDX se o teste sai de DX (o atributo ou uma perícia dele).
     * @param alvoEfetivo o NH já com modificadores; null = rolagem sem alvo, em
     *   que não existe "fracasso" para reclassificar.
     */
    fun reclassificar(
        personagem: Personagem,
        ehBaseDX: Boolean,
        original: CriticoRules.ResultadoCritico,
        soma: Int,
        alvoEfetivo: Int?
    ): CriticoRules.ResultadoCritico {
        if (!ehBaseDX || alvoEfetivo == null) return original
        // Sucesso — decisivo ou comum — não é assunto desta desvantagem.
        if (soma <= alvoEfetivo) return original
        if (original == CriticoRules.ResultadoCritico.DECISIVO) return original
        if (!ativo(personagem)) return original
        return CriticoRules.ResultadoCritico.FALHA_CRITICA
    }

    /**
     * Se o resultado só é falha crítica **por causa da desvantagem**.
     *
     * Serve para o aviso: um 18 já seria falha crítica sozinho, e avisar ali
     * seria ruído. O aviso só faz sentido quando o número, por si, não explica.
     */
    fun explicaOResultado(
        personagem: Personagem,
        ehBaseDX: Boolean,
        soma: Int,
        alvoEfetivo: Int?
    ): Boolean {
        val original = CriticoRules.classificar(soma, alvoEfetivo)
        if (original == CriticoRules.ResultadoCritico.FALHA_CRITICA) return false
        return reclassificar(personagem, ehBaseDX, original, soma, alvoEfetivo) ==
            CriticoRules.ResultadoCritico.FALHA_CRITICA
    }
}
