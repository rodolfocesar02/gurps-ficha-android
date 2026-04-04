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
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.res.painterResource
import com.gurps.ficha.R
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
            var showPlusMenu by remember { mutableStateOf(false) }
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
                            ChatBubble(msg, isUser)
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
                        // BOTÃO "+" COM MENU
                        Box {
                            IconButton(onClick = { showPlusMenu = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Mais opções", tint = MaterialTheme.colorScheme.primary)
                            }
                            DropdownMenu(
                                expanded = showPlusMenu,
                                onDismissRequest = { showPlusMenu = false },
                                modifier = Modifier.clip(RoundedCornerShape(16.dp))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("🛠️  Analisar Ficha") },
                                    onClick = { 
                                        showPlusMenu = false
                                        isAguardando = true
                                        viewModel.analisarFichaComIA { _, _ -> isAguardando = false }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("👤  Criar Personagem") },
                                    onClick = { 
                                        showPlusMenu = false
                                        prompt = "Gere uma ficha completa de um personagem de GURPS equilibrado (150 pts)."
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("🎨  Gerar Arte") },
                                    onClick = { 
                                        showPlusMenu = false
                                        prompt = "Me dê uma descrição visual épica para meu personagem e gere a arte."
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("⚔️  Sugerir Equipamento") },
                                    onClick = { 
                                        showPlusMenu = false
                                        isAguardando = true
                                        viewModel.conversarComMestreIA("Sugira equipamentos úteis para minha ficha atual.", "conversa") { _, _ -> isAguardando = false }
                                    }
                                )
                            }
                        }

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
                                        val m = if (prompt.lowercase().contains("gere") || prompt.lowercase().contains("ficha")) "geracao" else "conversa"
                                        isAguardando = true
                                        val userPrompt = prompt
                                        prompt = ""
                                        viewModel.conversarComMestreIA(userPrompt, m) { _, _ -> isAguardando = false }
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

    }
}


@Composable
fun ChatBubble(msg: MestreIAClient.ChatMessage, isUser: Boolean) {
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
                val imageRegex = "(https?://[^\\s]+(?:pollinations\\.ai|\\.png|\\.jpg|\\.jpeg)[^\\s]*)".toRegex()
                val matchResult = imageRegex.find(fullMessage)
                val imageUrl = matchResult?.value
                val textContent = if (imageUrl != null) fullMessage.replace(imageUrl, "").trim() else fullMessage

                Column(modifier = Modifier.padding(12.dp)) {
                    if (textContent.isNotEmpty()) {
                        Text(
                            text = textContent,
                            color = textColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    if (imageUrl != null) {
                        if (textContent.isNotEmpty()) Spacer(Modifier.height(8.dp))
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Imagem da IA",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
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
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSecondary)
                    }
                }
            }
        }
    }
}

