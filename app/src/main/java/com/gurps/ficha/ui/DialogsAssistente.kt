package com.gurps.ficha.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.agent.GurpsAgentService
import com.gurps.ficha.agent.GurpsAgentSource
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AssistenteGurpsDialog(
    onDismiss: () -> Unit
) {
    var question by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf("regras") }
    var loading by remember { mutableStateOf(false) }
    var answer by remember { mutableStateOf("") }
    var confidence by remember { mutableStateOf("baixa") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var sources by remember { mutableStateOf<List<GurpsAgentSource>>(emptyList()) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assistente GURPS") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Pergunta") },
                    placeholder = { Text("Ex.: Qual o custo de Aptidão Mágica?") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading
                )

                Text(
                    "Dica: use Regras para dúvidas de sistema.",
                    style = MaterialTheme.typography.labelSmall
                )

                Text("Modo", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)) {
                    FilterChip(
                        selected = mode == "regras",
                        onClick = { mode = "regras" },
                        enabled = !loading,
                        label = { Text("Regras") }
                    )
                    FilterChip(
                        selected = mode == "criacao",
                        onClick = { mode = "criacao" },
                        enabled = !loading,
                        label = { Text("Criação") }
                    )
                    FilterChip(
                        selected = mode == "lore",
                        onClick = { mode = "lore" },
                        enabled = !loading,
                        label = { Text("Lore") }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)) {
                    AssistChip(
                        onClick = { question = "Como funciona a perícia Judô?" },
                        enabled = !loading,
                        label = { Text("Exemplo Judô") },
                        colors = AssistChipDefaults.assistChipColors()
                    )
                    TextButton(
                        onClick = {
                            question = ""
                            answer = ""
                            sources = emptyList()
                            errorMessage = null
                        },
                        enabled = !loading
                    ) { Text("Limpar") }
                }

                if (loading) {
                    Text("Consultando assistente...", style = MaterialTheme.typography.bodyMedium)
                }

                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }

                if (answer.isNotBlank()) {
                    Text("Confiança: $confidence", style = MaterialTheme.typography.labelMedium)
                    Text(answer, style = MaterialTheme.typography.bodyMedium)
                }

                if (sources.isNotEmpty()) {
                    Text("Fontes", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    sources.forEachIndexed { idx, src ->
                        Text(
                            "${idx + 1}. ${src.sourceTitle} (pág. ${src.pageNumber}) [score ${"%.2f".format(src.score)}]",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !loading && question.trim().length >= 3,
                onClick = {
                    loading = true
                    errorMessage = null
                    scope.launch {
                        val result = GurpsAgentService.ask(question.trim(), mode = mode, topK = 6)
                        result.onSuccess { resp ->
                            answer = resp.answer
                            confidence = resp.confidence
                            sources = resp.sources
                        }.onFailure { err ->
                            errorMessage = err.message ?: "Falha ao consultar o assistente."
                        }
                        loading = false
                    }
                }
            ) {
                Text("Perguntar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Fechar") }
        }
    )
}

