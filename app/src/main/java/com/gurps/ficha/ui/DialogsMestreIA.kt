package com.gurps.ficha.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
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
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth().padding(16.dp).clip(RoundedCornerShape(28.dp)),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Mestre Digital 2.0", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                }
                Row {
                    IconButton(onClick = { viewModel.limparChatMestreIA() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Limpar", modifier = Modifier.size(20.dp))
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp)) {
                if (chatHistory.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Face,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(Modifier.height(16.dp))
                            Text("Saudações, Viajante!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("Como posso ajudar com sua ficha hoje?", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                } else {
                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(chatHistory) { msg ->
                            val isUser = msg.role == "user"
                            ChatBubble(msg, isUser, viewModel)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // BARRA DE INPUT ESTILO CHATGPT
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = prompt,
                            onValueChange = { prompt = it },
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                            decorationBox = { innerTextField ->
                                if (prompt.isEmpty()) {
                                    Text("Mensagem...", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.bodyMedium)
                                }
                                innerTextField()
                            }
                        )

                        if (isAguardando) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(4.dp), strokeWidth = 2.dp)
                        } else {
                               IconButton(
                                onClick = {
                                    if (prompt.isNotBlank()) {
                                        isAguardando = true
                                        val userPrompt = prompt
                                        prompt = ""
                                        viewModel.conversarComMestreIA(userPrompt, "conversa") { _, _ -> isAguardando = false }
                                    }
                                },
                                enabled = prompt.isNotBlank(),
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = if (prompt.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (prompt.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fechar", fontWeight = FontWeight.Bold) }
        }
    )

    // Diálogo de Confirmação para Integrar Ficha Gerada
    viewModel.fichaGeradaPendente?.let { fichaPendente ->
        AlertDialog(
            onDismissRequest = { viewModel.descartarFichaPendente() },
            title = { Text("Ficha Gerada detectada") },
            text = { 
                Text("O Mestre IA gerou uma ficha completa para '${fichaPendente.nome}'. Deseja aplicar estes dados à sua ficha atual? Esta ação não pode ser desfeita.") 
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmarIntegracaoFicha() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Aplicar Ficha")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.descartarFichaPendente() }) {
                    Text("Descartar")
                }
            }
        )
    }
}


@Composable
fun ChatBubble(msg: MestreIAClient.ChatMessage, isUser: Boolean, viewModel: FichaViewModel) {
    val align = if (isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    val shape = if (isUser) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalAlignment = align
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isUser) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("M", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(Modifier.width(8.dp))
            }

            Surface(
                color = bubbleColor,
                shape = shape,
                shadowElevation = 1.dp
            ) {
                val fullMessage = msg.text
                
                // Regex para imagens, ações e sugestões
                val imageRegex = "(https?://[^\\s]+(?:pollinations\\.ai|\\.png|\\.jpg|\\.jpeg)[^\\s]*)".toRegex()
                val actionRegex = "\\[ACAO:[^\\]]+\\]".toRegex()
                val suggestionRegex = "\\[SUGESTAO:[^\\]]+\\]".toRegex()
                
                val matchResult = imageRegex.find(fullMessage)
                val imageUrl = matchResult?.value
                
                // Extrai todas as ações e sugestões
                val actions = actionRegex.findAll(fullMessage).map { it.value }.toList()
                val suggestions = suggestionRegex.findAll(fullMessage).map { it.value }.toList()
                
                // Limpa o texto das tags técnicas
                var textContent = if (imageUrl != null) fullMessage.replace(imageUrl, "") else fullMessage
                actions.forEach { textContent = textContent.replace(it, "") }
                suggestions.forEach { textContent = textContent.replace(it, "") }
                textContent = textContent.trim()

                Column(modifier = Modifier.padding(12.dp)) {
                    if (textContent.isNotEmpty()) {
                        Text(text = textContent, color = textColor, style = MaterialTheme.typography.bodyMedium)
                    }
                    
                    if (imageUrl != null) {
                        Spacer(Modifier.height(8.dp))
                        SubcomposeAsyncImage(
                            model = imageUrl,
                            contentDescription = "IA Gen",
                            modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp).clip(RoundedCornerShape(12.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }

                    if (actions.isNotEmpty() && !isUser) {
                        Spacer(Modifier.height(12.dp))
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (actionTag in actions) {
                                val label = actionTag
                                    .removePrefix("[ACAO:")
                                    .removeSuffix("]")
                                    .replace(":", " ")
                                    .trim()
                                
                                AssistChip(
                                    onClick = { viewModel.executarAcaoIA(actionTag) },
                                    label = { Text("✅ $label", fontSize = 11.sp) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        labelColor = MaterialTheme.colorScheme.primary
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                )
                            }
                        }
                    }

                    if (suggestions.isNotEmpty() && !isUser) {
                        Spacer(Modifier.height(8.dp))
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (suggestionTag in suggestions) {
                                val label = suggestionTag
                                    .removePrefix("[SUGESTAO:")
                                    .removeSuffix("]")
                                    .trim()
                                
                                SuggestionChip(
                                    onClick = { 
                                        // Envia a sugestão como uma nova mensagem
                                        viewModel.conversarComMestreIA(label, "conversa") { _, _ -> }
                                    },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    border = SuggestionChipDefaults.suggestionChipBorder(
                                        enabled = true,
                                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }
                    }

                    // ETIQUETAS DE RASTREABILIDADE PRIME (Fase 5)
                    if (!isUser && (msg.modelName != null || msg.isRagUsed)) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth().graphicsLayer(alpha = 0.6f)
                        ) {
                            if (msg.isRagUsed) {
                                Surface(
                                    color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "RAG LOCAL",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(Modifier.width(6.dp))
                            }
                            
                            if (msg.modelName != null) {
                                Icon(
                                    Icons.Default.Build, // Representa motor/tecnologia
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp),
                                    tint = textColor.copy(alpha = 0.7f)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = msg.modelName,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.5.sp,
                                    color = textColor.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            if (isUser) {
                Spacer(Modifier.width(8.dp))
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.secondary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                    }
                }
            }
        }
    }
}
