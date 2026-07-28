package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.AutocontroleRules
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.ui.appCardColors

/**
 * Seção de Testes de Autocontrole, no FIM da aba Rolagem.
 *
 * **Não renderiza nada** quando o personagem não tem desvantagem com Número de
 * Autocontrole — decisão do usuário em 27/07/2026, mesma lógica da aba Magia,
 * que só existe com Aptidão Mágica.
 *
 * Preenche um buraco antigo: o app guardava o NA (usava no custo) mas nunca
 * rolava nada. 35 desvantagens do catálogo dependiam de o jogador fazer na mão.
 *
 * Toca no item → rola 3d6 pelo mesmo caminho das outras rolagens (log da sessão
 * e envio ao Discord).
 */
@Composable
fun PainelAutocontrole(
    personagem: Personagem,
    isPraCegoVariant: Boolean,
    modSituacional: Int,
    onRolar: (label: String, alvo: Int, mod: Int) -> Unit
) {
    val testes = remember(personagem.desvantagens) {
        AutocontroleRules.testesDisponiveis(personagem)
    }
    if (testes.isEmpty()) return

    if (isPraCegoVariant) SectionHeaderPraCego("Testes de Autocontrole")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = appCardColors()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (!isPraCegoVariant) {
                Text(
                    "Autocontrole",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            testes.forEach { teste ->
                val alvo = AutocontroleRules.alvoEfetivo(teste.na, modSituacional)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRolar("Autocontrole: ${teste.rotulo}", teste.na, modSituacional) }
                        .semantics {
                            contentDescription =
                                "Rolar autocontrole de ${teste.rotulo}. Alvo $alvo. ${teste.explicacao}"
                        }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // weight(1f): sem isso a coluna do texto ocupa a linha toda
                    // e espreme o "NA 15", que quebra letra a letra na vertical.
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            teste.rotulo,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        // A "notinha": NA baixo e PIOR, o contrario do resto do
                        // GURPS. Sem explicar, o jogador le errado.
                        Text(
                            teste.explicacao,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Text(
                        if (isPraCegoVariant) "Rolar ($alvo)" else "NA $alvo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        // Nome de desvantagem pode ser longo; o NA não pode
                        // quebrar de jeito nenhum — é o número da rolagem.
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
