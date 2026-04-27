package com.gurps.ficha.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
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
                    Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Mestre Digital 2.0", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    var showHistory by remember { mutableStateOf(false) }
                    IconButton(onClick = { 
                        viewModel.carregarHistoricoMestreIA()
                        showHistory = true 
                    }) {
                        Icon(Icons.Default.History, null, modifier = Modifier.size(20.dp))
                    }
                    if (showHistory) {
                        HistorySelectorDialog(viewModel) { showHistory = false }
                    }
                    IconButton(onClick = { viewModel.limparChatMestreIA() }) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(20.dp))
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp)) {
                if (chatHistory.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Face, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(Modifier.height(16.dp)); Text("Saudações!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    LazyColumn(state = scrollState, modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(chatHistory) { msg -> ChatBubble(msg, msg.role == "user", viewModel) }
                    }
                }
                Spacer(Modifier.height(12.dp))
                ChatInputBar(prompt, onValueChange = { prompt = it }, isAguardando, onSend = {
                    val p = prompt; prompt = ""; isAguardando = true
                    viewModel.conversarComMestreIA(p, viewModel.mestreIAMode) { _, _ -> isAguardando = false }
                }, mode = viewModel.mestreIAMode, onModeChange = { viewModel.mestreIAMode = it })
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}

@Composable
fun ChatInputBar(value: String, onValueChange: (String) -> Unit, isAguardando: Boolean, onSend: () -> Unit, mode: String, onModeChange: (String) -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            var showMenu by remember { mutableStateOf(false) }
            IconButton(onClick = { showMenu = true }) { Icon(if (mode == "conversa") Icons.Default.Add else Icons.Default.Settings, null) }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(text = { Text("📖 Dúvida") }, onClick = { onModeChange("conversa"); showMenu = false })
                DropdownMenuItem(text = { Text("🏗️ Criar") }, onClick = { onModeChange("geracao"); showMenu = false })
            }
            androidx.compose.foundation.text.BasicTextField(value = value, onValueChange = onValueChange, modifier = Modifier.weight(1f).padding(8.dp), decorationBox = { if (value.isEmpty()) Text("Fale com o mestre..."); it() })
            if (isAguardando) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else IconButton(onClick = onSend, enabled = value.isNotBlank()) { Icon(Icons.AutoMirrored.Filled.Send, null) }
        }
    }
}

@Composable
fun ChatBubble(msg: MestreIAClient.ChatMessage, isUser: Boolean, viewModel: FichaViewModel) {
    val align = if (isUser) Alignment.End else Alignment.Start
    val color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalAlignment = align) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start, modifier = Modifier.fillMaxWidth()) {
            if (!isUser) {
                Surface(modifier = Modifier.size(28.dp), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primary) { 
                    Box(contentAlignment = Alignment.Center) { Text("M", color = MaterialTheme.colorScheme.onPrimary, fontSize = 10.sp) } 
                }
                Spacer(Modifier.width(8.dp))
            }
            Surface(color = color, shape = RoundedCornerShape(16.dp), shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val rawText = msg.text
                    val actions = "\\[ACAO:[^\\]]+\\]".toRegex().findAll(rawText).map { it.value }.toList()
                    val suggestions = "\\[SUGESTAO:[^\\]]+\\]".toRegex().findAll(rawText).map { it.value }.toList()
                    val imageUrl = "(https?://[^\\s]+(?:pollinations\\.ai|\\.png|\\.jpg|\\.jpeg)[^\\s]*)".toRegex().find(rawText)?.value
                    
                    var textContent = rawText.replace("\\[ACAO:[^\\]]+\\]".toRegex(), "").replace("\\[SUGESTAO:[^\\]]+\\]".toRegex(), "").replace("```json.*?```".toRegex(RegexOption.DOT_MATCHES_ALL), "").replace("```json.*".toRegex(RegexOption.DOT_MATCHES_ALL), "")
                    if (imageUrl != null) textContent = textContent.replace(imageUrl, "")
                    textContent = textContent.trim()

                    var showRaw by remember { mutableStateOf(false) }
                    if (textContent.isNotEmpty()) Text(textContent, color = textColor, style = MaterialTheme.typography.bodyMedium)
                    else if (!isUser && msg.data != null) Text("📦 Ficha Pronta", fontWeight = FontWeight.Bold, color = textColor)
                    else if (!isUser && msg.rawJson != null) Text("⚠️ Erro de código", color = Color.Red, fontSize = 10.sp)

                    if (imageUrl != null) {
                        Spacer(Modifier.height(8.dp))
                        SubcomposeAsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                    }

                    if (showRaw && msg.rawJson != null) {
                        Spacer(Modifier.height(8.dp))
                        Surface(color = Color.Black.copy(0.6f), shape = RoundedCornerShape(8.dp)) { 
                            SelectionContainer { Text(msg.rawJson, color = Color.Green, fontSize = 9.sp, modifier = Modifier.padding(8.dp)) }
                        }
                    }

                    if (!isUser) {
                        val clipboardManager = LocalClipboardManager.current
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp), 
                            horizontalArrangement = Arrangement.End, 
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (msg.modelName != null) {
                                Surface(
                                    color = textColor.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        "AUDITOR: " + msg.modelName.substringAfterLast("/").uppercase(),
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = textColor.copy(0.8f),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { 
                                    clipboardManager.setText(AnnotatedString(textContent))
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copiar",
                                    tint = textColor.copy(0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        
                        if (msg.data != null) {
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { viewModel.confirmarIntegracaoFicha() }, modifier = Modifier.fillMaxWidth()) { Text("INTEGRAR") }
                        } else if (msg.rawJson != null) {
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { showRaw = !showRaw }, modifier = Modifier.weight(1f)) { Text(if (showRaw) "Fechar" else "Código") }
                                Button(onClick = { viewModel.conversarComMestreIA("Corrija o JSON.", "geracao") { _, _ -> } }, modifier = Modifier.weight(1f)) { Text("Corrigir") }
                            }
                        }
                    }
                }
            }
        }
        if (!isUser && (rawTagExists(msg.text, "[ACAO:") || rawTagExists(msg.text, "[SUGESTAO:"))) {
            val combinedActions = "\\[ACAO:[^\\]]+\\]".toRegex().findAll(msg.text).map { it.value }.toList()
            val combinedSuggestions = "\\[SUGESTAO:[^\\]]+\\]".toRegex().findAll(rawTextFromMsg(msg)).map { it.value }.toList()
            
            Spacer(Modifier.height(8.dp))
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (tag in combinedActions) {
                    val label = tag.removePrefix("[ACAO:").removeSuffix("]").replace(":", " ").trim()
                    AssistChip(onClick = { viewModel.executarAcaoIA(tag) }, label = { Text(label, fontSize = 10.sp) })
                }
                for (tag in combinedSuggestions) {
                    val label = tag.removePrefix("[SUGESTAO:").removeSuffix("]").trim()
                    SuggestionChip(onClick = { viewModel.conversarComMestreIA(label, "conversa") { _, _ -> } }, label = { Text(label, fontSize = 10.sp) })
                }
            }
        }
    }
}

fun rawTagExists(text: String, tag: String): Boolean = text.contains(tag)
fun rawTextFromMsg(msg: MestreIAClient.ChatMessage): String = msg.text

@Composable
fun HistorySelectorDialog(viewModel: FichaViewModel, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Histórico de Conversas", style = MaterialTheme.typography.titleLarge) },
        text = {
            val sessions = viewModel.mestreIASavedSessions
            if (sessions.isEmpty()) {
                Text("Nenhuma conversa salva ainda.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                    items(sessions) { session ->
                        Surface(
                            onClick = { 
                                viewModel.carregarSessaoMestreIA(session.id)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(session.title, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text(
                                    java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date(session.lastUpdate)),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}
