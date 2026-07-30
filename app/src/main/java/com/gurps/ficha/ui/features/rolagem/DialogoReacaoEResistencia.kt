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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.PisoDeTeste
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
                // ⚠️ O número pode ser NEGATIVO: a Suscetibilidade à Magia é o
                // espelho da Resistência (MB p.159) e usa o mesmo campo. Sem o
                // texto invertido, o card diria "o mago sofre −3" para quem, na
                // verdade, facilita o feitiço — número certo, frase mentindo.
                if (resistenciaAMagia != 0) {
                    val suscetivel = ResistenciaRules.ehSuscetivelAMagia(personagem)
                    val modulo = kotlin.math.abs(resistenciaAMagia)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = appCardColors()
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(
                                    if (suscetivel) "Suscetibilidade à Magia $modulo"
                                    else "Resistência à Magia $modulo",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (suscetivel) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Text(
                                    if (suscetivel) {
                                        "O mago ganha +$modulo no NH ao conjurar em você, e você " +
                                            "sofre −$modulo para resistir (MB p.159). Informe ao " +
                                            "Mestre — não vale contra projétil mágico, arma mágica " +
                                            "nem adivinhação."
                                    } else {
                                        "O mago sofre −$modulo no NH ao conjurar em você " +
                                            "(MB p.85). Informe ao Mestre — não vale contra projétil " +
                                            "mágico, arma mágica nem adivinhação."
                                    },
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
    // As caixinhas do Lote D-NA. O estado é por linha e some junto com ela.
    var marcados by remember(teste.rotulo, teste.condicionais) { mutableStateOf(emptySet<Int>()) }
    val extra = teste.condicionais.filterIndexed { i, _ -> i in marcados }.sumOf { it.valor }
    val alvoFinal = PisoDeTeste.aplicar(teste.alvo + extra)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = appCardColors()
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            // Só o cabeçalho rola — marcar uma condição dentro do clicável
            // dispararia a rolagem junto. Mesmo desenho do PainelReacao.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRolar(teste.rotulo, alvoFinal, 0) }
                    .semantics { contentDescription = teste.descricaoAcessivel }
                    .padding(vertical = 2.dp),
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
                    if (isPraCegoVariant) "Rolar ($alvoFinal)" else "$alvoFinal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    softWrap = false
                )
            }

            PainelBonusCondicional(
                bonus = teste.condicionais,
                marcados = marcados,
                onAlternar = { i -> marcados = if (i in marcados) marcados - i else marcados + i }
            )
        }
    }
}
