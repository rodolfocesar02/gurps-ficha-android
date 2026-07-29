package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.VisualizacaoRules
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.ui.FullscreenDialogContainer
import com.gurps.ficha.ui.UiActionLabels
import com.gurps.ficha.ui.appCardColors
import kotlin.random.Random

/**
 * **Visualização** — a calculadora do bônus (Lote VIS-1, MB p.99).
 *
 * Um minuto de concentração, um teste de IQ, e o bônus sai da **margem de
 * sucesso** — com três regras de arredondamento diferentes, duas delas exceções.
 * É a conta que mais erra para cima quando feita de cabeça.
 *
 * ## O fluxo
 *
 * 1. **Rolar IQ** — o diálogo rola 3d6 e mostra a margem.
 * 2. **Quanto a cena real parece com a imaginada** — três botões.
 * 3. O bônus sai com a conta escrita, e fica **guardado na tela** até o jogador
 *    limpar.
 *
 * ## Por que o resultado fica guardado
 *
 * Porque ele vale para uma ação **futura**: sem ficar à vista, o jogador anota num
 * papel e esquece — que é o problema que a Visualização tem na mesa hoje.
 */
@Composable
fun DialogoVisualizacao(
    personagem: Personagem,
    onGuardar: (bonus: Int, explicacao: String) -> Unit,
    onDismiss: () -> Unit
) {
    var rolagem by remember { mutableStateOf<List<Int>?>(null) }
    var semelhanca by remember { mutableStateOf<VisualizacaoRules.Semelhanca?>(null) }
    var iqAlvo by remember { mutableIntStateOf(personagem.iq) }

    val soma = rolagem?.sum()
    val margem = soma?.let { iqAlvo - it }

    FullscreenDialogContainer(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Visualização",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Um minuto de concentração e um teste de IQ $iqAlvo. " +
                    "O bônus sai da margem de sucesso.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                VisualizacaoRules.AVISO_COMBATE,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )

            OutlinedButton(
                onClick = {
                    rolagem = List(3) { Random.nextInt(1, 7) }
                    semelhanca = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Rolar o teste de IQ da Visualização" }
            ) {
                Text(if (rolagem == null) "Rolar IQ" else "Rolar de novo")
            }

            if (soma != null && margem != null) {
                Card(shape = RoundedCornerShape(12.dp), colors = appCardColors()) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            "3d6 = $soma contra IQ $iqAlvo",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            if (margem > 0) "Sucesso por $margem" else "Fracassou",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (margem > 0) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }

                if (margem > 0) {
                    Text(
                        "A cena real parece com a que ele imaginou?",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    VisualizacaoRules.Semelhanca.entries.forEach { opcao ->
                        val bonus = VisualizacaoRules.bonusDe(margem, opcao)
                        OutlinedButton(
                            onClick = { semelhanca = opcao },
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = "${opcao.rotulo}. ${opcao.descricao} " +
                                        "Resultado: mais $bonus."
                                }
                        ) {
                            Text("${opcao.rotulo}  →  +$bonus")
                        }
                    }
                }
            }

            semelhanca?.let { esc ->
                val m = margem ?: 0
                val bonus = VisualizacaoRules.bonusDe(m, esc)
                Card(shape = RoundedCornerShape(12.dp), colors = appCardColors()) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            if (bonus > 0) "+$bonus na ação visualizada" else "Sem bônus",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        // A conta à vista: um bônus que o jogador não consegue
                        // conferir é um bônus em que ele não confia.
                        Text(
                            VisualizacaoRules.explicacao(m, esc),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            esc.descricao,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                if (bonus > 0) {
                    OutlinedButton(
                        onClick = {
                            onGuardar(bonus, VisualizacaoRules.explicacao(m, esc))
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar +$bonus para a próxima rolagem")
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text(UiActionLabels.FECHAR) }
            }
        }
    }
}
