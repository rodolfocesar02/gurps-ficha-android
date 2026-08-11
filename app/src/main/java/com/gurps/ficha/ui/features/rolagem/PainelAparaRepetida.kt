package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.GolpeRapidoEAparaRules
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.ui.appCardColors

/**
 * **Qual apara do turno é esta** (Lote MESTRE-1, MB p.377).
 *
 * A segunda apara do turno custa -4, a terceira -8, a quarta -12 — e tudo zera no
 * turno seguinte. Hoje isso é conta de cabeça no meio do combate, e é a conta que
 * mais se esquece, porque acontece justamente quando o jogador está sob pressão.
 *
 * ## Os quatro resultados possíveis
 *
 * O degrau não é sempre -4. O livro dá quatro casos:
 *
 * | | Arma comum | Arma de esgrima |
 * |---|---|---|
 * | **Sem vantagem** | -4 | -2 |
 * | **Treinado por um Mestre / Mestre de Armas** | -2 | **-1** |
 *
 * O painel resolve os quatro sozinho e mostra a conta, porque um jogador com
 * rapieira **e** Treinado por um Mestre paga **um quarto** do que a tabela sugere
 * de cabeça — e ninguém lembra disso na mesa.
 *
 * ## Por que fica visível e não escondido num diálogo
 *
 * Porque ele **acumula**, e um número acumulado que não está na tela é um número
 * que vai ser esquecido no turno seguinte. Aparece em 1 (sem penalidade) e só
 * chama atenção quando sai de 1.
 */
@Composable
fun PainelAparaRepetida(
    personagem: Personagem,
    numeroDaApara: Int,
    armaDeEsgrima: Boolean,
    onMudar: (Int) -> Unit,
    onNovoTurno: () -> Unit = {},
    // Lote ROL-3: o chamador decide a largura — os dois viraram meia
    // fileira na aba Rolagem, lado a lado.
    modifier: Modifier = Modifier
) {
    val degrau = GolpeRapidoEAparaRules.penalidadePorAparaExtra(personagem, armaDeEsgrima)
    val total = GolpeRapidoEAparaRules.penalidadeAcumulada(personagem, numeroDaApara, armaDeEsgrima)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = appCardColors()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Mesma estrutura do PainelIluminacao -- ver o comentario la. Os dois
            // ficam lado a lado em meia largura, e qualquer diferenca de
            // alinhamento entre eles salta aos olhos.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Apara nº do turno",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    if (total == 0) "0" else "$total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    color = if (total < 0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.semantics {
                        contentDescription = "Penalidade na Apara: " +
                            if (total < 0) "menos ${-total}" else "nenhuma"
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { onMudar((numeroDaApara - 1).coerceAtLeast(1)) },
                    modifier = Modifier.semantics {
                        contentDescription = "Voltar uma apara. Agora é a ${numeroDaApara}ª do turno."
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) { Text("−", style = MaterialTheme.typography.titleMedium) }

                Text(
                    "$numeroDaApara",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            contentDescription = "${numeroDaApara}ª apara deste turno"
                        },
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                TextButton(
                    onClick = { onMudar((numeroDaApara + 1).coerceAtMost(9)) },
                    modifier = Modifier.semantics {
                        contentDescription = "Contar mais uma apara neste turno."
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) { Text("+", style = MaterialTheme.typography.titleMedium) }
            }

            // A conta só aparece quando há conta: em 1 não há nada a explicar.
            if (numeroDaApara > 1) {
                val mestria = GolpeRapidoEAparaRules.nomeDaMestria(personagem)
                val motivo = listOfNotNull(
                    mestria,
                    if (armaDeEsgrima) "arma de esgrima" else null
                )
                Text(
                    "${numeroDaApara - 1} apara(s) extra × $degrau" +
                        if (motivo.isEmpty()) "" else " (reduzido por ${motivo.joinToString(" e ")})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                // O contador ANDA sozinho a cada apara rolada, entao o botao de
                // zerar e a unica coisa que o jogador precisa lembrar -- e ele
                // esta aqui, na frente, em vez de virar regra decorada.
                TextButton(
                    onClick = onNovoTurno,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    modifier = Modifier.semantics {
                        contentDescription = "Novo turno: zera a penalidade de apara repetida."
                    }
                ) {
                    Text("Novo turno — zerar", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
