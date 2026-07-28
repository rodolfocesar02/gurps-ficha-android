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
import androidx.compose.material3.Checkbox
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
import com.gurps.ficha.domain.rules.MiraRules
import com.gurps.ficha.ui.FullscreenDialogContainer
import com.gurps.ficha.ui.UiActionLabels
import com.gurps.ficha.ui.appCardColors
import com.gurps.ficha.ui.linhaAlternavel

/**
 * **Onde acertar** — o diálogo de mira (Lote MIRA-1, MB p.398-400).
 *
 * Ideia do usuário: tocar no NH do ataque abre esta lista, e cada linha mostra
 * **o número já reduzido**. Se a Faca é NH 12 e você quer o olho (−9), a linha
 * do olho mostra **3**. Sem penalidade entre parênteses, sem conta mental no
 * meio da mesa.
 *
 * Tocou, o diálogo fecha e a rolagem sai — um gesto só.
 *
 * **Não calcula dano de propósito.** O dano localizado depende da RD do
 * oponente, que a ficha não tem e nunca terá: é informação do Mestre. Mostrar um
 * número ali seria inventar.
 *
 * O `detalhe` de cada linha existe porque a escolha real é pelo **efeito**, não
 * pelo número: mirar no crânio vale a pena por causa do ×4 de ferimento, não
 * apesar do −7.
 */
@Composable
fun DialogoMira(
    rotuloDoAtaque: String,
    nhBase: Int,
    isPraCegoVariant: Boolean,
    onEscolher: (rotulo: String, nh: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var desarmar by remember { mutableStateOf(false) }
    val opcoes = MiraRules.opcoes(desarmar)

    FullscreenDialogContainer(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                "Onde acertar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "$rotuloDoAtaque — NH $nhBase no torso",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MiraRules.Grupo.values().forEach { grupo ->
                    val doGrupo = opcoes.filter { it.grupo == grupo }
                    if (doGrupo.isEmpty()) return@forEach

                    item {
                        Text(
                            grupo.rotulo,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // A opção de desarmar só faz sentido sobre a arma do
                    // oponente — no corpo não existe "desarmar".
                    if (grupo == MiraRules.Grupo.ARMA) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .linhaAlternavel(
                                        marcado = desarmar,
                                        descricao = "Golpear para desarmar em vez de quebrar. " +
                                            "Penalidade adicional de menos 2.",
                                        onAlternar = { desarmar = !desarmar }
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(checked = desarmar, onCheckedChange = null)
                                Text(
                                    "Desarmar em vez de quebrar (−2)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        }
                    }

                    items(doGrupo) { opcao ->
                        LinhaDeMira(opcao, nhBase, isPraCegoVariant) {
                            onEscolher(
                                "$rotuloDoAtaque — ${opcao.rotulo}",
                                opcao.nhCom(nhBase)
                            )
                        }
                    }
                }

                item {
                    Text(
                        "O dano não entra aqui: ele depende da RD do oponente, que " +
                            "só o Mestre tem.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text(UiActionLabels.FECHAR) }
            }
        }
    }
}

@Composable
private fun LinhaDeMira(
    opcao: MiraRules.Opcao,
    nhBase: Int,
    isPraCegoVariant: Boolean,
    onEscolher: () -> Unit
) {
    val nh = opcao.nhCom(nhBase)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = appCardColors()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEscolher() }
                .semantics { contentDescription = opcao.descricaoAcessivel(nhBase) }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    opcao.rotulo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                opcao.detalhe?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Text(
                if (isPraCegoVariant) "Rolar ($nh)" else "$nh",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                // NH negativo é informação, não erro: mostra em vermelho para o
                // jogador ver na hora que aquele alvo está fora de alcance.
                color = if (nh < 3) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
