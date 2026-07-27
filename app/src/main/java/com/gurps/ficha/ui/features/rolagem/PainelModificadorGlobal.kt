package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gurps.ficha.ui.appCardColors
import kotlin.math.abs

/**
 * Ajuste rápido do modificador aplicado à PRÓXIMA rolagem.
 *
 * Existe só na variante PraCego: quem enxerga ajusta o modificador deslizando
 * o dedo sobre o atributo, gesto que não funciona bem com TalkBack. Aqui os
 * degraus viram botões rotulados (−5 −2 −1 C +1 +2 +5), cada um com descrição
 * própria para leitura em voz.
 *
 * Extraído de `TabRolagem.kt` no Lote UI-2 (o arquivo estourava 1.000 linhas).
 * Comportamento idêntico ao anterior — a tela não muda.
 *
 * Quem decide exibir é a `TabRolagem`; este componente não conhece a variante.
 */
@Composable
fun PainelModificadorGlobal(
    modificador: Int,
    cardTitleStyle: TextStyle,
    verticalPadding: Dp,
    onModificadorChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = appCardColors()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = verticalPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Modificador para a próxima rolagem: ${if (modificador >= 0) "+$modificador" else "$modificador"}",
                style = cardTitleStyle,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DEGRAUS.forEach { delta ->
                    val label = when {
                        delta == 0 -> "C"
                        delta > 0 -> "+$delta"
                        else -> "$delta"
                    }
                    val descricao = when {
                        delta < 0 -> "Diminuir modificador em ${abs(delta)}"
                        delta > 0 -> "Aumentar modificador em $delta"
                        else -> "Limpar modificadores"
                    }
                    OutlinedButton(
                        onClick = {
                            // 0 = "C" de limpar, não um passo de zero.
                            onModificadorChange(
                                if (delta == 0) 0 else (modificador + delta).coerceIn(-999, 999)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = descricao },
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                    ) {
                        Text(label)
                    }
                }
            }
        }
    }
}

private val DEGRAUS = listOf(-5, -2, -1, 0, 1, 2, 5)
