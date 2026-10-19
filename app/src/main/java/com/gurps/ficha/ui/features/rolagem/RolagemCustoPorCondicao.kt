package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.magic.CondicaoEntrada
import com.gurps.ficha.domain.magic.CustoPorCondicao
import com.gurps.ficha.domain.magic.CustoPorCondicaoRules

/**
 * **O custo que DEPENDE DA CONDIÇÃO, na hora de conjurar** — o que o jogador informa (o peso do objeto, o material,
 * o alcance...) e o PF que sai da conta do catálogo (`planos/magias/CUSTO-NA-HORA-DA-CONJURACAO.md`).
 *
 * A conta é a de [CustoPorCondicaoRules], a mesma que a Mesa roda em JavaScript. Cada mudança refaz a conta e
 * avisa o diálogo do gasto ([onCusto]): `null` quando o que foi informado não fecha (o botão Aplicar apaga).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustoPorCondicaoCampos(
    spec: CustoPorCondicao,
    isPraCegoVariant: Boolean,
    onCusto: (Int?) -> Unit
) {
    // `Any?` porque a entrada é número digitado (texto), id de opção (texto) ou marca (0/1).
    val condicoes = remember(spec) {
        mutableStateMapOf<String, Any?>().apply { putAll(CustoPorCondicaoRules.condicoesPadrao(spec)) }
    }
    val digitados = remember(spec) {
        mutableStateMapOf<String, String>().apply {
            spec.entradas.filter { it.tipo == "numero" || it.tipo == "inteiro" }.forEach { e ->
                put(e.id, CustoPorCondicaoRules.numeroEmTexto((CustoPorCondicaoRules.condicoesPadrao(spec)[e.id] as? Double) ?: 0.0))
            }
        }
    }
    val resultado = CustoPorCondicaoRules.calcular(spec, condicoes.toMap())
    LaunchedEffect(resultado) {
        onCusto((resultado as? CustoPorCondicaoRules.Resultado.Ok)?.operar)
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "O custo desta magia depende de:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        spec.entradas.forEach { e ->
            when (e.tipo) {
                "escolha" -> {
                    Text(e.rotulo, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        e.opcoes.orEmpty().forEach { o ->
                            FilterChip(
                                selected = condicoes[e.id] == o.id,
                                onClick = { condicoes[e.id] = o.id },
                                label = { Text(o.rotulo) },
                                modifier = Modifier.semantics {
                                    if (isPraCegoVariant) contentDescription = "${e.rotulo}: ${o.rotulo}"
                                }
                            )
                        }
                    }
                }
                "marca" -> {
                    val ligada = condicoes[e.id] == 1.0
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { condicoes[e.id] = if (ligada) 0.0 else 1.0 }
                    ) {
                        Checkbox(checked = ligada, onCheckedChange = { condicoes[e.id] = if (it) 1.0 else 0.0 })
                        Text(e.rotulo, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                else -> CampoNumerico(e, digitados[e.id].orEmpty(), isPraCegoVariant) { texto ->
                    digitados[e.id] = texto
                    condicoes[e.id] = texto
                }
            }
        }
        when (resultado) {
            is CustoPorCondicaoRules.Resultado.Ok -> {
                val resumo = CustoPorCondicaoRules.resumo(spec, condicoes.toMap())
                Text(
                    "Conta do custo" + (if (resumo.isNotBlank()) " ($resumo)" else "") + ": " +
                        "${resultado.operar} para operar" +
                        (resultado.manter?.takeIf { resultado.manterTipo != "nenhum" }?.let { ", $it para manter" } ?: "") +
                        (if (resultado.manterTipo == "nenhum") "; esta magia não se mantém" else "") + ".",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.semantics {
                        contentDescription = "Conta do custo: ${resultado.operar} de energia para operar."
                    }
                )
            }
            is CustoPorCondicaoRules.Resultado.Erro -> Text(
                if (resultado.erro == "condicao_invalida") {
                    "${resultado.rotulo ?: resultado.campo ?: "Uma condição"} ${resultado.motivo}."
                } else {
                    "A conta do custo não fecha (${resultado.motivo.ifBlank { resultado.erro }}): fale com o Mestre."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/** Um campo de número (o peso em kg, o comprimento em m, os pontos de RD). */
@Composable
private fun CampoNumerico(
    entrada: CondicaoEntrada,
    texto: String,
    isPraCegoVariant: Boolean,
    onMudou: (String) -> Unit
) {
    val rotulo = entrada.rotulo + (entrada.unidade?.takeIf { it.isNotBlank() }?.let { " ($it)" } ?: "")
    OutlinedTextField(
        value = texto,
        onValueChange = { onMudou(it.filter { c -> c.isDigit() || c == ',' || c == '.' || c == '-' }.take(9)) },
        label = { Text(rotulo) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (entrada.tipo == "inteiro") KeyboardType.Number else KeyboardType.Decimal
        ),
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                if (isPraCegoVariant) contentDescription = "Informar $rotulo"
            }
    )
}
