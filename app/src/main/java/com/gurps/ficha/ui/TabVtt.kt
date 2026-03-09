package com.gurps.ficha.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.gurps.ficha.viewmodel.FichaViewModel

@Composable
fun TabVtt(viewModel: FichaViewModel) {
    var serverUrl by remember { mutableStateOf("") }
    var roomKey by remember { mutableStateOf("") }
    var playerId by remember(viewModel.personagem.nome) { mutableStateOf(viewModel.personagem.nome) }
    var statusConexao by remember { mutableStateOf("Desconectado") }

    StandardTabColumn {
        SectionCard(title = "Conexão VTT") {
            Text(
                text = "Aba VTT (shell) ativa. Integração de sessão e rolagem será ligada pelos próximos lotes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text("Servidor (URL)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = roomKey,
                onValueChange = { roomKey = it },
                label = { Text("Sala (roomKey)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = playerId,
                onValueChange = { playerId = it },
                label = { Text("Player ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status: $statusConexao",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            PrimaryActionButton(
                text = "Conectar (em breve)",
                onClick = { statusConexao = "Configuração salva (integração pendente)" }
            )
        }

        SummaryFooterCard(title = "Próximos passos") {
            Text(
                text = "1. Join de sessão VTT",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "2. Vínculo player e token",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "3. Rolagem contextual via contrato v1",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
