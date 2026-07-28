package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.MarcosDeVidaRules

/**
 * Aviso dos marcos de PV/PF, logo abaixo do cartão de atributos.
 *
 * Duas coisas diferentes na mesma faixa:
 *
 *  - **Testes exigidos** — aparecem quando a queda de PV cruza um marco
 *    (ferimento grave, 0 PV, múltiplos negativos). Ficam até o jogador rolar ou
 *    dispensar.
 *  - **Estado atual** — Cambaleante, Cansado, Exausto. Não é rolagem, é aviso;
 *    fica enquanto o valor estiver na faixa.
 *
 * **O app OFERECE, não rola.** O botão está aqui e o jogador toca. Rolar
 * sozinho esconderia de onde veio o número — o defeito que a zona de dano
 * invisível causou no TOK-9. E o Mestre pode simplesmente dispensar o teste.
 *
 * **Não renderiza nada** quando não há teste pendente nem estado.
 */
@Composable
fun PainelMarcosDeVida(
    testesPendentes: List<MarcosDeVidaRules.TesteExigido>,
    estados: List<MarcosDeVidaRules.EstadoAtual>,
    isPraCegoVariant: Boolean,
    onRolar: (MarcosDeVidaRules.TesteExigido) -> Unit,
    onDispensar: () -> Unit
) {
    if (testesPendentes.isEmpty() && estados.isEmpty()) return

    if (isPraCegoVariant && testesPendentes.isNotEmpty()) {
        SectionHeaderPraCego("Testes exigidos pelo ferimento")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (testesPendentes.isNotEmpty()) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Estado primeiro: é a informação que vale mesmo sem teste pendente.
            estados.forEach { estado ->
                Text(
                    "${estado.rotulo} — ${estado.efeito}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            testesPendentes.forEach { teste ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRolar(teste) }
                        .semantics { contentDescription = teste.descricaoAcessivel }
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            teste.rotulo,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        Text(
                            teste.explicacao,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        if (teste.origens.isNotEmpty()) {
                            Text(
                                teste.origens.joinToString(", "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    Text(
                        if (isPraCegoVariant) "Rolar (${teste.alvo})" else "HT ${teste.alvo}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // Dispensar existe porque o Mestre manda: às vezes ele simplesmente
            // não pede o teste, e o aviso não pode ficar preso na tela.
            if (testesPendentes.isNotEmpty()) {
                TextButton(onClick = onDispensar) { Text("Dispensar") }
            }
        }
    }
}
