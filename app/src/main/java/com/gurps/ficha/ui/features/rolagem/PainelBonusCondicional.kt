package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.traits.BonusCondicional
import com.gurps.ficha.ui.linhaAlternavel

/**
 * Caixas para o jogador marcar os bônus que valem NESTA rolagem.
 *
 * A maioria dos bônus do GURPS é condicional: *Rosto Sincero* dá +1 em
 * Dissimulação **"para parecer inocente"*; *Camaleão* dá +2 em Furtividade
 * **"quando não quer ser visto e está imóvel"**. Somar isso no NH da ficha
 * seria mentir sobre o personagem — ele não tem esse bônus sempre.
 *
 * Quem sabe se a condição vale é o jogador, no momento do teste. Por isso vira
 * escolha aqui, e não número na ficha.
 *
 * **Não renderiza nada** quando a perícia não tem bônus condicional.
 */
@Composable
fun PainelBonusCondicional(
    bonus: List<BonusCondicional>,
    marcados: Set<Int>,
    onAlternar: (indice: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (bonus.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            "Vale nesta rolagem?",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
        bonus.forEachIndexed { indice, b ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // A linha inteira passa a ser o alvo do toque. Antes ela só
                    // tinha descrição, e a caixinha era um SEGUNDO foco sem
                    // rótulo nenhum.
                    //
                    // O "Marcado."/"Não marcado." saiu do texto: o próprio
                    // TalkBack anuncia o estado quando o papel é Checkbox, e
                    // repetir virava eco.
                    .linhaAlternavel(
                        marcado = indice in marcados,
                        descricao = "${b.nomeDoTraco}, " +
                            "${if (b.valor >= 0) "mais" else "menos"} " +
                            "${kotlin.math.abs(b.valor)}, ${b.condicao}",
                        onAlternar = { onAlternar(indice) }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = indice in marcados,
                    onCheckedChange = null
                )
                Text(
                    b.rotulo,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }
    }
}

/** Soma dos bônus marcados — o que vai para o modificador da rolagem. */
fun somaDosMarcados(bonus: List<BonusCondicional>, marcados: Set<Int>): Int =
    marcados.filter { it in bonus.indices }.sumOf { bonus[it].valor }
