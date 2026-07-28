package com.gurps.ficha.ui.features.traits

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.traits.BracalRuleBase
import com.gurps.ficha.domain.rules.traits.DxBracalRule
import com.gurps.ficha.domain.rules.traits.StBracalRule

/**
 * Configuração de **ST Braçal** e **DX Braçal** — braços × níveis.
 *
 * O diálogo padrão de ESCOLHA mostrava só três botões ("3 pts", "5 pts",
 * "8 pts") e cobrava aquele valor uma vez. Está errado: no livro esses números
 * são o preço de **cada +1**, e o que muda entre eles é quantos braços recebem
 * o aumento. Faltava, portanto, perguntar **quantos níveis**.
 *
 * Analogia: é a diferença entre "a passagem custa 5 reais" e "a passagem custa
 * 5 reais por parada". O app cobrava uma parada e deixava o jogador viajar o
 * quanto quisesse.
 *
 * Arquivo próprio porque `VantagemDialogs.kt` já está em 988 linhas, encostando
 * no teto de 1.000 do projeto.
 */
@Composable
fun BracalConfig(
    regra: BracalRuleBase,
    bracos: Int,
    nivel: Int,
    nomeAtributo: String,
    onChanged: (bracos: Int, nivel: Int) -> Unit
) {
    val nivelMinimo = 1
    val nivelMaximo = 20

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Braços beneficiados:", style = MaterialTheme.typography.bodyMedium)

        regra.opcoesDeBracos().forEach { qtd ->
            val porNivel = regra.custoPorNivel(qtd)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onChanged(qtd, nivel) }
                    .semantics {
                        contentDescription =
                            "${nomeDeBracos(qtd)}, $porNivel pontos por cada mais 1 de $nomeAtributo. " +
                                if (qtd == bracos) "Selecionado." else "Não selecionado."
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = qtd == bracos, onClick = { onChanged(qtd, nivel) })
                Text("${nomeDeBracos(qtd)} — $porNivel pts por +1")
            }
        }

        Text("Níveis de $nomeAtributo adicional:", style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { if (nivel > nivelMinimo) onChanged(bracos, nivel - 1) }) { Text("-") }
            Text(
                "+$nivel",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics {
                    contentDescription = "$nomeAtributo Braçal mais $nivel"
                }
            )
            TextButton(onClick = { if (nivel < nivelMaximo) onChanged(bracos, nivel + 1) }) { Text("+") }
        }

        // A conta na cara do jogador: sem isto ele só vê o total e não sabe se
        // o erro está no número de braços ou no de níveis.
        Text(
            "${regra.custoPorNivel(bracos)} × $nivel = ${regra.custoPorNivel(bracos) * nivel} pts",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

/**
 * A regra braçal do id, ou null quando a vantagem não é uma delas.
 *
 * Os dois diálogos (adicionar e editar) usam este mesmo teste — é o que decide
 * se o bloco de braços × níveis substitui os botões de custo fixo.
 */
fun regraBracalDe(definicaoId: String): BracalRuleBase? = when (definicaoId) {
    StBracalRule.ID -> StBracalRule()
    DxBracalRule.ID -> DxBracalRule()
    else -> null
}

/** "Um braço", "Dois braços", "Três braços" — a tabela do livro para em três. */
fun nomeDeBracos(qtd: Int): String = when (qtd) {
    1 -> "Um braço"
    2 -> "Dois braços"
    3 -> "Três braços"
    else -> "$qtd braços"
}
