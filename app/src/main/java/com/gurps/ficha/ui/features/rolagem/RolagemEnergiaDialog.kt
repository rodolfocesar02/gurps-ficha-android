package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.MagiaEnergiaRules
import com.gurps.ficha.domain.rules.RotuloAcessivel

/**
 * O diálogo do gasto de energia da aba Rolagem (saiu do `RolagemSecondaryDialogs.kt`, que passou do teto de 1000
 * linhas). Quando a magia tem custo que DEPENDE DA CONDIÇÃO, os campos dela vêm de `RolagemCustoPorCondicao.kt`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RolagemEnergiaManualDialog(
    magiaEnergia: MagiaRollOption,
    pfAtualRolagem: Int,
    energiaManualInput: String,
    talismaMagiaVinculada: String?,
    repertorioParaTalisma: List<String>,
    isPraCegoVariant: Boolean,
    onInputMudou: (String) -> Unit,
    onTalismaVinculadoMudou: (String?) -> Unit,
    onAplicar: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val exigeVinculoTalisma = magiaEnergia.definicaoId.equals("talisma", ignoreCase = true)
    var menuTalismaExpandido by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gasto de energia") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Magia: ${magiaEnergia.nome}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                // Lote MAGIA-E1: a faixa lida do catálogo, em português. Antes
                // aparecia só o texto cru ("1 a 3") e o campo já vinha com 1 —
                // não dava para saber que 2 e 3 eram escolhas legítimas.
                val custoDoCatalogo = com.gurps.ficha.domain.rules.MagiaEnergiaRules
                    .parseCusto(magiaEnergia.energia)
                if (custoDoCatalogo.precisaEscolher && magiaEnergia.custoPorCondicao == null) {
                    Text(
                        "Você escolhe quanto gastar: ${custoDoCatalogo.comoTexto}. " +
                            "Os dados só rolam depois desta escolha.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                magiaEnergia.energia?.takeIf { it.isNotBlank() }?.let { energia ->
                    Text(
                        "Energia da ficha: $energia",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                // O custo que DEPENDE DA CONDIÇÃO (peso, tamanho, material...): o jogador informa e a conta do catálogo
                // preenche o custo base logo abaixo (que ele ainda pode corrigir à mão).
                magiaEnergia.custoPorCondicao?.let { spec ->
                    CustoPorCondicaoCampos(
                        spec = spec,
                        isPraCegoVariant = isPraCegoVariant,
                        onCusto = { custo -> onInputMudou(custo?.toString().orEmpty()) }
                    )
                }
                if (exigeVinculoTalisma) {
                    Text(
                        "Talismã: escolha uma magia do repertório para finalizar a rolagem.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ExposedDropdownMenuBox(
                        expanded = menuTalismaExpandido,
                        onExpandedChange = { menuTalismaExpandido = !menuTalismaExpandido },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = talismaMagiaVinculada.orEmpty(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Talismã: magia vinculada") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuTalismaExpandido)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = menuTalismaExpandido,
                            onDismissRequest = { menuTalismaExpandido = false }
                        ) {
                            repertorioParaTalisma.forEach { nomeMagia ->
                                DropdownMenuItem(
                                    text = { Text(nomeMagia) },
                                    onClick = {
                                        onTalismaVinculadoMudou(nomeMagia)
                                        menuTalismaExpandido = false
                                    }
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = energiaManualInput,
                    onValueChange = onInputMudou,
                    label = { Text("Custo base da magia agora") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            if (isPraCegoVariant) contentDescription = "Informar energia gasta para a magia ${magiaEnergia.nome}"
                        }
                )
                Text(
                    "PF da rolagem atual: $pfAtualRolagem",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                energiaManualInput.toIntOrNull()?.let { custoBase ->
                    val reducao = MagiaEnergiaRules.reducaoPorNh(magiaEnergia.target)
                    val custoFinal = MagiaEnergiaRules.custoAjustadoPorNh(custoBase, magiaEnergia.target)
                    Text(
                        "Redução por NH ${magiaEnergia.target}: -$reducao | custo final: $custoFinal",
                        modifier = Modifier.semantics {
                            // ⚠️ O visível mostra "-1"; o falado diz "menos 1".
                            contentDescription = "Redução por nível de habilidade " +
                                "${magiaEnergia.target}: ${RotuloAcessivel.modificador(-reducao)}. " +
                                "Custo final $custoFinal."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // 🔴 Aplicar NÃO chama o onDismiss.
                    //
                    // Chamava, e no Lote MAGIA-E2 isso virou defeito: o
                    // `onDismiss` passou a ser o "desisti da magia" e limpa a
                    // energia comprometida. Aplicar comprometia 2, disparava a
                    // rolagem, e no instante seguinte o próprio botão apagava o
                    // compromisso — quando os dados caíam não havia mais nada
                    // para cobrar, e o PF não baixava.
                    //
                    // ⚠️ O `onAplicar` já fecha o diálogo. Chamar os dois era
                    // inofensivo enquanto o cancelar não fazia nada; virou bug
                    // no dia em que ele passou a fazer.
                    val custoBase = energiaManualInput.toIntOrNull()
                    if (custoBase != null) {
                        onAplicar(MagiaEnergiaRules.custoAjustadoPorNh(custoBase, magiaEnergia.target))
                    } else {
                        onDismiss()
                    }
                },
                enabled = energiaManualInput.toIntOrNull() != null &&
                    (!exigeVinculoTalisma || !talismaMagiaVinculada.isNullOrBlank())
            ) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Ignorar")
            }
        }
    )
}
