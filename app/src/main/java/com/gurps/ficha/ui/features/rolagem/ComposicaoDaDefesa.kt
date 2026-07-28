package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.OrigemDosNumeros
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.ui.features.traits.textoDeOrigem
import com.gurps.ficha.viewmodel.DefenseType

/**
 * De onde vem cada ponto de uma defesa — dentro do diálogo de configuração.
 *
 * Fica **aqui e não no card** de propósito. Os três cards de defesa dividem a
 * largura da tela; uma linha como `+3 (Escudo Grande +2, Reflexos +1)` quebraria
 * em três linhas e estragaria a tela. O diálogo é onde o jogador vai justamente
 * para entender e ajustar o número, e lá sobra espaço.
 *
 * **Não renderiza nada** quando a defesa é só base — sem escudo, sem vantagem e
 * sem bônus digitado à mão não há o que explicar.
 */
@Composable
fun ComposicaoDaDefesa(
    personagem: Personagem,
    tipo: DefenseType,
    modifier: Modifier = Modifier
) {
    val origens = OrigemDosNumeros.daDefesa(personagem, tipo)
    if (origens.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Composição do bônus: " +
                    origens.joinToString(", ") { o ->
                        "${o.nomeDoTraco} ${if (o.valor >= 0) "mais" else "menos"} " +
                            "${kotlin.math.abs(o.valor)}"
                    }
            },
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Text(
            "Somado à base:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            textoDeOrigem(origens),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
