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
import androidx.compose.material3.OutlinedTextField
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
import com.gurps.ficha.domain.rules.TalentoInstintivoRules
import com.gurps.ficha.ui.FullscreenDialogContainer
import com.gurps.ficha.ui.UiActionLabels
import com.gurps.ficha.ui.appCardColors

/**
 * **Talento Instintivo** — rolar uma perícia que o personagem não tem (MB p.92).
 *
 * A lista traz **só** as perícias que ele **não** conhece, cada uma com o NH do
 * atributo base — que é justamente o que a vantagem concede: o atributo cheio,
 * sem a penalidade de não saber.
 *
 * Tocar numa linha rola e gasta um uso. O contador de usos fica no topo, com um
 * botão de **zerar** — o app não sabe quando a sessão começou, e chutar
 * devolveria usos que o jogador já gastou.
 *
 * ## Por que tem busca
 *
 * São ~250 perícias que o personagem não tem. Sem campo de busca a lista é
 * inutilizável no meio da mesa.
 */
@Composable
fun DialogoTalentoInstintivo(
    opcoes: List<TalentoInstintivoRules.Opcao>,
    rotuloDeUsos: String,
    temUsoDisponivel: Boolean,
    isPraCegoVariant: Boolean,
    onRolar: (TalentoInstintivoRules.Opcao) -> Unit,
    onZerarUsos: () -> Unit,
    onDismiss: () -> Unit
) {
    var busca by remember { mutableStateOf("") }
    val filtradas = remember(busca, opcoes) {
        if (busca.isBlank()) opcoes
        else opcoes.filter { it.nome.contains(busca.trim(), ignoreCase = true) }
    }

    FullscreenDialogContainer(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                "Talento Instintivo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    rotuloDeUsos,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (temUsoDisponivel) {
                        MaterialTheme.colorScheme.outline
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = onZerarUsos,
                    modifier = Modifier.semantics {
                        contentDescription = "Zerar os usos do Talento Instintivo. " +
                            "Use ao começar uma sessão nova."
                    }
                ) { Text("Nova sessão") }
            }
            Text(
                TalentoInstintivoRules.AVISO,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )

            OutlinedTextField(
                value = busca,
                onValueChange = { busca = it },
                label = { Text("Buscar perícia") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .semantics { contentDescription = "Buscar entre as perícias não conhecidas" }
            )

            if (filtradas.isEmpty()) {
                Text(
                    if (opcoes.isEmpty()) {
                        "Este personagem já tem todas as perícias do catálogo."
                    } else {
                        "Nenhuma perícia não conhecida com esse nome."
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filtradas) { opcao ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = appCardColors()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = temUsoDisponivel) { onRolar(opcao) }
                                .semantics {
                                    contentDescription =
                                        TalentoInstintivoRules.descricaoAcessivel(opcao)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    opcao.nome,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "Rola ${opcao.atributo}, sem penalidade",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Text(
                                if (isPraCegoVariant) "Rolar (${opcao.nh})" else "${opcao.nh}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text(UiActionLabels.FECHAR) }
            }
        }
    }
}
