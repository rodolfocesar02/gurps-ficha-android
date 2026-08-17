package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.SentidoRules
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.ui.rolagemVertical

/**
 * Lote 372: diálogo de Testes de Sentidos (abre ao tocar "PER"). Cada sentido rola contra a
 * Percepção ± vantagens/desvantagens, com a "notinha" do motivo do bônus/redutor. Tem variante
 * PraCego (botões rotulados grandes + semântica) além da visual.
 *
 * @param modSituacional modificador situacional já ajustado no PER (swipe) — soma a todos os sentidos.
 * @param onRolar (rótuloDoTeste, alvo, mod) → segue o MESMO caminho de rolagem dos atributos (→ Discord).
 */
@Composable
fun DialogoSentidos(
    personagem: Personagem,
    isPraCegoVariant: Boolean,
    modSituacional: Int,
    onRolar: (label: String, alvo: Int, mod: Int) -> Unit,
    onFechar: () -> Unit
) {
    val resultados = remember(personagem) { SentidoRules.todos(personagem) }

    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text("Testes de Sentidos") },
        text = {
            Column(Modifier.rolagemVertical()) {
                Text(
                    "Cada sentido rola contra a Percepção, somando Sentidos Aguçados e descontando limitações.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (modSituacional != 0) {
                    Text(
                        "Modificador situacional do PER: ${if (modSituacional >= 0) "+$modSituacional" else "$modSituacional"} (aplicado a todos).",
                        style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic
                    )
                }
                Spacer(Modifier.height(8.dp))
                resultados.forEach { r ->
                    SentidoItem(
                        resultado = r,
                        modSituacional = modSituacional,
                        isPraCegoVariant = isPraCegoVariant,
                        onRolar = onRolar
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onFechar) { Text("Fechar") } }
    )
}

@Composable
private fun SentidoItem(
    resultado: SentidoRules.ResultadoSentido,
    modSituacional: Int,
    isPraCegoVariant: Boolean,
    onRolar: (String, Int, Int) -> Unit
) {
    val nota = resultado.nota()
    val alvo = resultado.valorFinal
    // Rótulo do teste enviado à rolagem/Discord — carrega o motivo (a "notinha").
    val rotuloTeste = resultado.sentido.rotulo + if (nota.isNotBlank()) " [$nota]" else ""
    val descAcessivel = if (resultado.bloqueado)
        "${resultado.sentido.rotulo}: ${resultado.motivoBloqueio}, não pode rolar"
    else
        "Rolar ${resultado.sentido.rotulo}, alvo $alvo" + if (nota.isNotBlank()) ", $nota" else ""

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).semantics { contentDescription = descAcessivel }
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(resultado.sentido.rotulo, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                when {
                    resultado.bloqueado -> Text(
                        resultado.motivoBloqueio ?: "indisponível",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error
                    )
                    nota.isNotBlank() -> Text(
                        "Percepção ${resultado.percepcao} ($nota)",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    else -> Text(
                        "Percepção ${resultado.percepcao}",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            if (resultado.bloqueado) {
                Text("—", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
            } else if (isPraCegoVariant) {
                // PraCego: botão rotulado e grande (não depende de tocar o número).
                Button(onClick = { onRolar(rotuloTeste, alvo, modSituacional) }) { Text("Rolar ($alvo)") }
            } else {
                Text(
                    alvo.toString(),
                    style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .clickable { onRolar(rotuloTeste, alvo, modSituacional) }
                )
            }
        }
    }
}
