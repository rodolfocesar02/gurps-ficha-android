package com.gurps.ficha.ui.features.traits

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gurps.ficha.domain.rules.traits.TraitRuleRegistry
import com.gurps.ficha.model.Personagem

/**
 * Linha discreta que explica de onde vem o bônus de uma perícia.
 *
 * Sem isso a automação vira caixa preta: o NH da Escalada pula de 12 para 14 e
 * nada na tela diz por quê — e o jogador perde a capacidade de conferir se a
 * conta está certa.
 *
 * Um componente só, usado pela aba Perícias E pela aba Rolagem (decisão do
 * usuário em 27/07/2026). Se o texto fosse montado nos dois lugares, eles
 * divergiriam na primeira mudança de regra.
 *
 * Modelo: `SentidoRules` + `DialogoSentidos`, que já mostram os componentes
 * nomeados do cálculo.
 */
@Composable
fun OrigemDoBonusPericia(
    personagem: Personagem,
    nomeDaPericia: String,
    modifier: Modifier = Modifier
) {
    val origens = TraitRuleRegistry.getSkillBonusOrigens(personagem, nomeDaPericia)
    if (origens.isEmpty()) return   // perícia sem bônus não ganha linha nenhuma

    Text(
        text = textoDeOrigem(origens),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.outline,
        modifier = modifier
    )
}

/**
 * A mesma linha, para números que **não** são de perícia (Lote NOTA-2).
 *
 * Recebe as origens já apuradas em vez de buscá-las: quem sabe montar a lista é
 * a regra (`OrigemDosNumeros`, `TraitRuleRegistry`), e cada número tem a sua.
 *
 * [unidade] existe porque o bônus de dano é **por dado**: escrever "+1 Mestre
 * de Armas" numa arma de 3d seria mentira — o ganho real é +3. Passando
 * `"/dado"` a linha sai `+1/dado Mestre de Armas`, que é o que o livro diz.
 */
@Composable
fun OrigemDoBonusNumero(
    origens: List<TraitRuleRegistry.OrigemDeBonus>,
    modifier: Modifier = Modifier,
    unidade: String = ""
) {
    if (origens.isEmpty()) return

    Text(
        text = textoDeOrigem(origens, unidade),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.outline,
        modifier = modifier
    )
}

/**
 * Monta o rótulo. Kotlin puro, sem Compose, para ter teste — é aqui que mora a
 * decisão de formato.
 *
 *  - uma origem:      `+2 Pendulear`
 *  - várias:          `+3 (Pendulear +2, Reflexos +1)`
 *  - saldo negativo:  `-2 Gordo`
 *  - com unidade:     `+1/dado Mestre de Armas`
 *
 * O total aparece primeiro porque é o que o jogador quer saber; a decomposição
 * só é necessária quando há mais de uma fonte.
 */
internal fun textoDeOrigem(
    origens: List<TraitRuleRegistry.OrigemDeBonus>,
    unidade: String = ""
): String {
    if (origens.isEmpty()) return ""
    if (origens.size == 1) {
        val u = origens.first()
        return "${comSinal(u.valor)}$unidade ${u.nomeDoTraco}"
    }
    val total = origens.sumOf { it.valor }
    val detalhe = origens.joinToString(", ") { "${it.nomeDoTraco} ${comSinal(it.valor)}$unidade" }
    return "${comSinal(total)}$unidade ($detalhe)"
}

private fun comSinal(valor: Int): String = if (valor >= 0) "+$valor" else "$valor"

/**
 * Mesma informação em texto corrido, para entrar na descrição do card lida pelo
 * TalkBack. Não vira elemento separado de propósito: seria mais um ponto de
 * parada na navegação por toque, virando ruído.
 */
internal fun descricaoAcessivelDeOrigem(origens: List<TraitRuleRegistry.OrigemDeBonus>): String {
    if (origens.isEmpty()) return ""
    val partes = origens.joinToString(", ") { o ->
        val verbo = if (o.valor >= 0) "mais" else "menos"
        "$verbo ${kotlin.math.abs(o.valor)} de ${o.nomeDoTraco}"
    }
    return " Inclui $partes."
}
