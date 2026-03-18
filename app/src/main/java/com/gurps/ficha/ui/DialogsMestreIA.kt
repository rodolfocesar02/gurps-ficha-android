package com.gurps.ficha.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gurps.ficha.BuildConfig
import com.gurps.ficha.data.network.MestreIAClient
import com.gurps.ficha.viewmodel.FichaViewModel

@Composable
fun DialogMestreIA(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit
) {
    var prompt by remember { mutableStateOf("") }
    var isAguardando by remember { mutableStateOf(false) }
    var showConfig by remember { mutableStateOf(false) }
    val chatHistory = viewModel.mestreIAChatHistory
    val scrollState = rememberLazyListState()
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)

    // Auto-scroll para o fim quando novas mensagens chegam
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            scrollState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Mestre Digital GURPS 2.0")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showConfig = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurações da IA")
                    }
                    IconButton(onClick = { viewModel.limparChatMestreIA() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Limpar Conversa")
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
                if (chatHistory.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                            Text("Como posso ajudar com sua ficha hoje?", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                } else {
                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chatHistory) { msg ->
                            val isUser = msg.role == "user"
                            val align = if (isUser) Alignment.End else Alignment.Start
                            val color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                            val textColor = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                            
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = align) {
                                Text(
                                    text = if (isUser) "Você" else "Mestre",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(color)
                                        .padding(8.dp)
                                ) {
                                    val fullMessage = msg.text
                                    val imageRegex = "(https?://[^\\s]+(?:pollinations\\.ai|\\.png|\\.jpg|\\.jpeg)[^\\s]*)".toRegex()
                                    val matchResult = imageRegex.find(fullMessage)
                                    val imageUrl = matchResult?.value
                                    val textContent = if (imageUrl != null) fullMessage.replace(imageUrl, "").trim() else fullMessage

                                    Column {
                                        if (textContent.isNotEmpty()) {
                                            Text(
                                                text = textContent,
                                                color = textColor,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(bottom = if (imageUrl != null) 8.dp else 0.dp)
                                            )
                                        }
                                        
                                        if (imageUrl != null) {
                                            AsyncImage(
                                                model = imageUrl,
                                                contentDescription = "Imagem da IA",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(max = 300.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Fit
                                            )
                                            Text(
                                                text = "Arte gerada pelo Mestre",
                                                color = textColor.copy(alpha = 0.7f),
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Sugestões rápidas
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SuggestionChip(
                        onClick = { 
                            isAguardando = true
                            viewModel.analisarFichaComIA { _, _ -> isAguardando = false } 
                        },
                        label = { Text("Analisar Ficha", fontSize = 10.sp) },
                        enabled = !isAguardando
                    )
                    SuggestionChip(
                        onClick = { 
                            prompt = "Gere uma arte épica do meu personagem atual. Estilo pintura de fantasia."
                        },
                        label = { Text("Gerar Arte", fontSize = 10.sp) },
                        icon = { Icon(Icons.Default.Face, null, modifier = Modifier.size(16.dp)) },
                        enabled = !isAguardando
                    )
                    SuggestionChip(
                        onClick = { 
                            prompt = "Gere uma ficha de Guerreiro Nórdico sobrevivente."
                        },
                        label = { Text("Novo Personagem", fontSize = 10.sp) },
                        enabled = !isAguardando
                    )
                    SuggestionChip(
                        onClick = { 
                            isAguardando = true
                            viewModel.conversarComMestreIA("Sugira 5 itens de equipamento úteis para meu personagem.", "conversa") { _, _ -> isAguardando = false }
                        },
                        label = { Text("Sugerir Equipamento", fontSize = 10.sp) },
                        enabled = !isAguardando
                    )
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    placeholder = { Text("Pergunte algo ou descreva um novo conceito...", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (isAguardando) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            IconButton(
                                onClick = {
                                    if (prompt.isNotBlank()) {
                                        val m = if (prompt.lowercase().contains("gere") || prompt.lowercase().contains("ficha")) "geracao" else "conversa"
                                        isAguardando = true
                                        val userPrompt = prompt
                                        prompt = ""
                                        viewModel.conversarComMestreIA(userPrompt, m) { _, _ -> isAguardando = false }
                                    }
                                },
                                enabled = prompt.isNotBlank()
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Enviar")
                            }
                        }
                    },
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fechar") }
        }
    )

    if (showConfig) {
        MestreIAConfigDialog(
            viewModel = viewModel,
            onDismiss = { showConfig = false }
        )
    }
}

@Composable
fun MestreIAConfigDialog(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit
) {
    var baseUrl by remember { mutableStateOf(viewModel.iaBaseUrl) }
    var apiKey by remember { mutableStateOf(viewModel.iaApiKey) }
    var workspaceSlug by remember { mutableStateOf(viewModel.iaWorkspaceSlug) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurações do Servidor de IA", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Base URL do AnythingLLM (ex: Railway):", style = MaterialTheme.typography.labelSmall)
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    placeholder = { Text("https://seu-mestre-ia.up.railway.app") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Chave de API do AnythingLLM:", style = MaterialTheme.typography.labelSmall)
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    placeholder = { Text("Bearer ...") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Nome do Workspace (Slug):", style = MaterialTheme.typography.labelSmall)
                OutlinedTextField(
                    value = workspaceSlug,
                    onValueChange = { workspaceSlug = it },
                    placeholder = { Text("meu-workspace") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Dica: O Workspace Slug é o nome que aparece na URL do AnythingLLM após /workspace/.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.salvarConfiguracaoIA(baseUrl, apiKey, workspaceSlug)
                    onDismiss()
                }
            ) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
