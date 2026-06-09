package com.gurps.ficha.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.res.painterResource
import com.gurps.ficha.R
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import com.gurps.ficha.BuildConfig
import com.gurps.ficha.data.network.MestreIAClient
import com.gurps.ficha.ui.components.EstadoLive
import com.gurps.ficha.viewmodel.FichaViewModel

@Composable
fun DialogMestreIA(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit,
    estadoLive: EstadoLive = EstadoLive.OCIOSO,
    onEncerrarLive: () -> Unit = {}
) {
    var prompt by remember { mutableStateOf("") }
    var isAguardando by remember { mutableStateOf(false) }
    val chatHistory = viewModel.mestreIAChatHistory
    val scrollState = rememberLazyListState()
    val liveAtivo = estadoLive != EstadoLive.OCIOSO && estadoLive != EstadoLive.ERRO

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
                    Image(painter = painterResource(id = R.drawable.tab_mestre_ia), contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Mestre IA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    // Indicador de estado do Live
                    if (liveAtivo) {
                        Spacer(Modifier.width(8.dp))
                        val (dotColor, dotLabel) = when (estadoLive) {
                            EstadoLive.CONECTANDO    -> Color(0xFFFF8F00) to "conectando..."
                            EstadoLive.OUVINDO       -> Color(0xFF2E7D32) to "ouvindo"
                            EstadoLive.FALANDO       -> Color(0xFF1565C0) to "falando"
                            EstadoLive.PROCESSANDO   -> Color(0xFFFFA000) to "processando..."
                            else                     -> Color.Gray to ""
                        }
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(dotColor))
                        Spacer(Modifier.width(4.dp))
                        Text(dotLabel, fontSize = 11.sp, color = dotColor)
                    }
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
                                Image(painter = painterResource(id = R.drawable.tab_mestre_ia), contentDescription = null, modifier = Modifier.size(48.dp))
                            }
                            Spacer(Modifier.height(16.dp))
                            Text("Saudações!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            if (liveAtivo) {
                                Spacer(Modifier.height(8.dp))
                                Text("🎙️ Modo voz ativo — fale com o Mestre", fontSize = 13.sp, color = Color(0xFF2E7D32))
                            }
                        }
                    }
                } else {
                    LazyColumn(state = scrollState, modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(chatHistory) { msg -> ChatBubble(msg, msg.role == "user", viewModel) }
                    }
                }
                Spacer(Modifier.height(12.dp))
                // Barra de input — oculta enquanto Live estiver ouvindo (usuário fala por voz)
                if (!liveAtivo) {
                    ChatInputBar(prompt, onValueChange = { prompt = it }, isAguardando, onSend = {
                        if (viewModel.mestreIAMode == "pintor") {
                            // Mestre Pintor: ignora o texto e abre o dialog de retrato
                            viewModel.dispensarDialogRetrato()  // reseta flag, o delegate reexibe
                            viewModel.gerarRetratoIA()
                        } else {
                            val p = prompt; prompt = ""; isAguardando = true
                            viewModel.conversarComMestreIA(p, viewModel.mestreIAMode) { _, _ -> isAguardando = false }
                        }
                    }, mode = viewModel.mestreIAMode, onModeChange = { viewModel.mestreIAMode = it })
                } else {
                    // Mostra status de voz e botão encerrar no lugar do input
                    val statusTexto = when (estadoLive) {
                        EstadoLive.CONECTANDO  -> "Conectando ao Mestre..."
                        EstadoLive.OUVINDO     -> "🎙️ Ouvindo — fale agora"
                        EstadoLive.FALANDO     -> "🔊 Mestre falando..."
                        EstadoLive.PROCESSANDO -> "⚙️ Processando..."
                        else                   -> ""
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(statusTexto, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = { onEncerrarLive(); onDismiss() }) {
                            Text("Encerrar voz", color = Color(0xFFC62828))
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!liveAtivo) TextButton(onClick = onDismiss) { Text("Fechar") }
        }
    )
}

@Composable
fun ChatInputBar(value: String, onValueChange: (String) -> Unit, isAguardando: Boolean, onSend: () -> Unit, mode: String, onModeChange: (String) -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            var showMenu by remember { mutableStateOf(false) }
            // Modos disponíveis:
            // "conversa"  → Mestre Bibliotecário  — dúvidas de regras via RAG (MestreIAUseCase)
            // "analise"   → Mestre Auditor        — analisa/edita ficha existente (MestreIAGeneratorUseCase / modo analise)
            // "geracao"   → Mestre Forjador       — cria ficha do zero incrementalmente (MestreIAGeneratorUseCase / modo geracao)
            // "pintor"    → Mestre Pintor         — gera retrato via Gemini Image (GeminiImageService)
            val modoLabel = when (mode) {
                "analise"  -> "Mestre Auditor"
                "geracao"  -> "Mestre Forjador"
                "pintor"   -> "Mestre Pintor"
                else       -> "Mestre Bibliotecário"
            }
            IconButton(onClick = { showMenu = true }, modifier = Modifier.semantics { contentDescription = "Abrir menu de modo do Mestre IA (modo: $modoLabel)" }) {
                Icon(if (mode == "conversa") Icons.Default.Add else Icons.Default.Settings, null)
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    leadingIcon = { Image(painterResource(R.drawable.mestre_bibliotecario), null, modifier = Modifier.size(28.dp)) },
                    text = { Text("Mestre Bibliotecário") },
                    onClick = { onModeChange("conversa"); showMenu = false }
                )
                DropdownMenuItem(
                    leadingIcon = { Image(painterResource(R.drawable.mestre_auditor), null, modifier = Modifier.size(28.dp)) },
                    text = { Text("Mestre Auditor") },
                    onClick = { onModeChange("analise"); showMenu = false }
                )
                DropdownMenuItem(
                    leadingIcon = { Image(painterResource(R.drawable.mestre_forjador), null, modifier = Modifier.size(28.dp)) },
                    text = { Text("Mestre Forjador") },
                    onClick = { onModeChange("geracao"); showMenu = false }
                )
                DropdownMenuItem(
                    leadingIcon = { Image(painterResource(R.drawable.mestre_pintor), null, modifier = Modifier.size(28.dp)) },
                    text = { Text("Mestre Pintor") },
                    onClick = { onModeChange("pintor"); showMenu = false }
                )
            }
            val isPintor = mode == "pintor"
            androidx.compose.foundation.text.BasicTextField(
                value = value, onValueChange = onValueChange,
                modifier = Modifier.weight(1f).padding(8.dp),
                enabled = !isPintor,
                decorationBox = {
                    if (value.isEmpty()) Text(if (isPintor) "Toque em ▶ para gerar o retrato" else "Fale com o mestre...")
                    it()
                }
            )
            if (isAguardando) CircularProgressIndicator(modifier = Modifier.size(24.dp))
            else IconButton(onClick = onSend, enabled = isPintor || value.isNotBlank(), modifier = Modifier.semantics { contentDescription = "Enviar mensagem para o Mestre IA" }) {
                Icon(Icons.AutoMirrored.Filled.Send, null)
            }
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
                    if (textContent.isNotEmpty()) {
                        if (!isUser) MarkdownContent(textContent, textColor)
                        else Text(textContent, color = textColor, style = MaterialTheme.typography.bodyMedium)
                    }
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
fun MarkdownContent(text: String, textColor: Color) {
    val bodyStyle = MaterialTheme.typography.bodyMedium
    val lines = text.lines()
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            when {
                // Tabela: detecta bloco de linhas com |
                line.trimStart().startsWith("|") -> {
                    val tableLines = mutableListOf<String>()
                    while (i < lines.size && lines[i].trimStart().startsWith("|")) {
                        tableLines.add(lines[i])
                        i++
                    }
                    MarkdownTable(tableLines, textColor)
                    continue
                }
                // Heading ## ou ###
                line.startsWith("### ") -> {
                    Text(
                        line.removePrefix("### ").parseBold(),
                        color = textColor,
                        style = bodyStyle,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                line.startsWith("## ") -> {
                    Text(
                        line.removePrefix("## ").parseBold(),
                        color = textColor,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                line.startsWith("# ") -> {
                    Text(
                        line.removePrefix("# ").parseBold(),
                        color = textColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                // Separador ---
                line.trim() == "---" || line.trim() == "***" -> {
                    HorizontalDivider(color = textColor.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
                }
                // Linha vazia
                line.isBlank() -> {
                    Spacer(Modifier.height(4.dp))
                }
                // Texto normal (com bold inline)
                else -> {
                    Text(
                        buildInlineMarkdown(line, textColor),
                        style = bodyStyle
                    )
                }
            }
            i++
        }
    }
}

@Composable
fun MarkdownTable(lines: List<String>, textColor: Color) {
    val rows = lines
        .filter { !it.trim().replace("|", "").replace("-", "").replace(":", "").replace(" ", "").isEmpty() }
        .map { line -> line.trim().trim('|').split("|").map { it.trim() } }
    if (rows.isEmpty()) return

    val cols = rows.maxOf { it.size }
    Surface(
        color = textColor.copy(alpha = 0.06f),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column {
            rows.forEachIndexed { rowIdx, row ->
                if (rowIdx > 0) HorizontalDivider(color = textColor.copy(alpha = 0.15f), thickness = 0.5.dp)
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp)) {
                    for (col in 0 until cols) {
                        val cell = row.getOrElse(col) { "" }
                        Text(
                            buildInlineMarkdown(cell, textColor),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (rowIdx == 0) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

fun String.parseBold(): String = replace("**", "").replace("*", "")

@Composable
fun buildInlineMarkdown(text: String, textColor: Color): AnnotatedString {
    return buildAnnotatedString {
        val boldColor = textColor
        var remaining = text
        while (remaining.isNotEmpty()) {
            val boldStart = remaining.indexOf("**")
            if (boldStart < 0) { append(remaining); break }
            append(remaining.substring(0, boldStart))
            val boldEnd = remaining.indexOf("**", boldStart + 2)
            if (boldEnd < 0) { append(remaining.substring(boldStart)); break }
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = boldColor)) {
                append(remaining.substring(boldStart + 2, boldEnd))
            }
            remaining = remaining.substring(boldEnd + 2)
        }
    }
}

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

@Composable
fun DialogRetratoIA(
    nomePersonagem: String,
    onGerar: () -> Unit,
    onDispensар: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDispensар,
        icon = {
            Image(
                painter = painterResource(R.drawable.mestre_pintor),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                "Gerar retrato de $nomePersonagem?",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "O Mestre IA pode gerar um retrato artístico do personagem usando a descrição e história que acabaram de ser criadas.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Modelo: Gemini 3.1 Flash Image\nCusto: ~\$0,07 por imagem\nTempo: ~10-35 segundos",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onGerar) {
                Text("Gerar retrato")
            }
        },
        dismissButton = {
            TextButton(onClick = onDispensар) {
                Text("Agora não")
            }
        }
    )
}
