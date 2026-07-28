package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.ResistenciaRules
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.ui.FullscreenDialogContainer
import com.gurps.ficha.ui.UiActionLabels
import com.gurps.ficha.ui.appCardColors

/**
 * O diálogo **Reação e Resistência** (Lote RESIST-1).
 *
 * Ideia do usuário: juntar num lugar só todos os testes que não são perícia nem
 * atributo puro. Antes disto, Reação e Autocontrole eram painéis soltos no fim
 * da aba Rolagem, e resistir a veneno, doença ou medo não existia em lugar
 * nenhum.
 *
 * A ordem é a de uso na mesa: primeiro o que a ficha **tem** (Reação e
 * Autocontrole dependem dos traços), depois o catálogo fixo de resistências,
 * que qualquer personagem pode precisar.
 *
 * Os painéis de Reação e Autocontrole continuam sendo os mesmos componentes —
 * só mudaram de lugar. Não foram reescritos.
 */
@Composable
fun DialogoReacaoEResistencia(
    personagem: Personagem,
    isPraCegoVariant: Boolean,
    modSituacional: Int,
    onRolar: (label: String, alvo: Int?, mod: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val testes = ResistenciaRules.testesDe(personagem)
    val resistenciaAMagia = ResistenciaRules.resistenciaAMagia(personagem)

    FullscreenDialogContainer(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                "Reação e Resistência",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // O que depende dos traços da ficha vem primeiro, e some quando
                // não há traço — mesma regra de sempre.
                item {
                    PainelReacao(
                        personagem = personagem,
                        isPraCegoVariant = isPraCegoVariant,
                        onRolar = onRolar
                    )
                }
                item {
                    PainelAutocontrole(
                        personagem = personagem,
                        isPraCegoVariant = isPraCegoVariant,
                        modSituacional = modSituacional,
                        onRolar = { label, alvo, mod -> onRolar(label, alvo, mod) }
                    )
                }

                // O número que o MESTRE aplica do outro lado. Fica visível para
                // o jogador informar no Discord — o app não tem a ficha do mago.
                if (resistenciaAMagia > 0) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = appCardColors()
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(
                                    "Resistência à Magia $resistenciaAMagia",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "O mago sofre −$resistenciaAMagia no NH ao conjurar em você " +
                                        "(MB p.85). Informe ao Mestre — não vale contra projétil " +
                                        "mágico, arma mágica nem adivinhação.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }

                ResistenciaRules.Familia.values().forEach { familia ->
                    val daFamilia = testes.filter { it.familia == familia }
                    if (daFamilia.isEmpty()) return@forEach

                    item {
                        Text(
                            familia.rotulo,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    items(daFamilia) { teste ->
                        LinhaDeResistencia(teste, isPraCegoVariant, onRolar)
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text(UiActionLabels.FECHAR) }
            }
        }
    }
}

@Composable
private fun LinhaDeResistencia(
    teste: ResistenciaRules.TesteDeResistencia,
    isPraCegoVariant: Boolean,
    onRolar: (String, Int?, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = appCardColors()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onRolar(teste.rotulo, teste.alvo, 0) }
                .semantics { contentDescription = teste.descricaoAcessivel }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    teste.rotulo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
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
                if (isPraCegoVariant) "Rolar (${teste.alvo})" else "${teste.alvo}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
