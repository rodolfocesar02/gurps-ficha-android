package com.gurps.ficha.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gurps.ficha.BuildConfig

/**
 * Dialog para interação com o Mestre IA (Gemini).
 * Suporta as variantes Visual e Pracego.
 */
@Composable
fun DialogMestreIA(
    onDismiss: () -> Unit,
    onGerarFicha: (String) -> Unit
) {
    var historiaTexto by remember { mutableStateOf("") }
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isPraCegoVariant) "Assistente de Criação: Mestre Digital" else "Mestre Digital GURPS",
                modifier = Modifier.semantics { 
                    contentDescription = "Título: Mestre Digital GURPS. Use este assistente para gerar sua ficha a partir da sua história."
                }
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Conte a história do seu personagem ou descreva o conceito dele abaixo:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = historiaTexto,
                    onValueChange = { historiaTexto = it },
                    label = { Text("História ou Detalhes") },
                    placeholder = { Text("Ex: Skarner nasceu no norte, luta com machado...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .semantics { 
                            contentDescription = if (isPraCegoVariant) 
                                "Campo de texto para a história. Digite aqui a biografia do seu personagem." 
                            else ""
                        }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (historiaTexto.isNotBlank()) {
                        onGerarFicha(historiaTexto)
                    }
                },
                enabled = historiaTexto.isNotBlank(),
                modifier = Modifier.semantics { 
                    contentDescription = if (isPraCegoVariant) "Botão: Gerar ficha agora" else "" 
                }
            ) {
                Text("Gerar Ficha")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.semantics { 
                    contentDescription = if (isPraCegoVariant) "Botão: Cancelar e fechar" else "" 
                }
            ) {
                Text("Cancelar")
            }
        }
    )
}
