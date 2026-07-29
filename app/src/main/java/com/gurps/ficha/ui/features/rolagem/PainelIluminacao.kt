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
import com.gurps.ficha.domain.rules.IluminacaoRules
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.ui.appCardColors

/**
 * **A luz da cena** — o seletor de escuridão (Lote LUZ-1, MB p.395/549).
 *
 * O jogador escolhe a luz **uma vez** e o modificador entra em todas as rolagens
 * de visão e combate. A Visão Noturna do personagem já vem descontada, com a
 * conta escrita embaixo: *"Escuro -7 + Visão Noturna 4 → -3"*.
 *
 * ## Por que uma faixa e não quatro botões nomeados
 *
 * O livro não batiza degraus de escuridão: ele diz *"-1 a -9, a critério do
 * Mestre"* e reserva o **-10** para escuridão total. Inventar quatro nomes com
 * valores fixos seria pôr regra na boca do livro. Os rótulos que aparecem são
 * referência do que aquele número costuma significar, não tabela.
 *
 * ## O card desaparece quando está claro
 *
 * Em 0 ele fica recolhido numa linha só. Combate de dia é a maioria dos casos, e
 * um card grande dizendo "sem penalidade" é ruído permanente na tela.
 */
@Composable
fun PainelIluminacao(
    personagem: Personagem,
    penalidadeBruta: Int,
    isPraCegoVariant: Boolean,
    onMudar: (Int) -> Unit
) {
    val r = IluminacaoRules.penalidadeEfetiva(personagem, penalidadeBruta)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = appCardColors()
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription =
                            IluminacaoRules.descricaoAcessivel(personagem, penalidadeBruta)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Luz da cena",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                TextButton(
                    onClick = { onMudar((penalidadeBruta - 1).coerceAtLeast(IluminacaoRules.ESCURIDAO_TOTAL)) },
                    modifier = Modifier.semantics { contentDescription = "Escurecer a cena" },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) { Text("−", style = MaterialTheme.typography.titleMedium) }

                Text(
                    IluminacaoRules.rotuloDaLuz(r.bruta),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                TextButton(
                    onClick = { onMudar((penalidadeBruta + 1).coerceAtMost(0)) },
                    modifier = Modifier.semantics { contentDescription = "Clarear a cena" },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) { Text("+", style = MaterialTheme.typography.titleMedium) }

                Text(
                    if (r.efetiva == 0) "0" else "${r.efetiva}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (r.efetiva < 0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }

            // A conta à vista sempre que houver algo a explicar: sem isso o
            // jogador lê "-3" onde escolheu "-7" e não sabe por quê.
            if (r.bruta != 0) {
                Text(
                    r.explicacao,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                if (!isPraCegoVariant) {
                    Text(
                        "Entra no ataque e nas defesas.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
