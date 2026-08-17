package com.gurps.ficha.ui.features.rolagem

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gurps.ficha.model.NotaDeJogo
import java.text.SimpleDateFormat
import java.util.*

val CoresNotaDeJogo = listOf(
    "#FFFFFF" to "Branco",
    "#FFF9C4" to "Amarelo", // Yellow 100
    "#F8BBD0" to "Rosa",    // Pink 100
    "#C8E6C9" to "Verde",   // Green 100
    "#BBDEFB" to "Azul",    // Blue 100
    "#E1BEE7" to "Roxo",    // Purple 100
    "#FFCCBC" to "Laranja"  // Deep Orange 100
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorDeNota(
    nota: NotaDeJogo,
    onSalvar: (NotaDeJogo) -> Unit,
    onExcluir: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var texto by remember { mutableStateOf(nota.texto) }
    var corSelecionada by remember { mutableStateOf(nota.corHex ?: "#FFFFFF") }
    var showColorPicker by remember { mutableStateOf(false) }

    val dataFormatada = remember(nota.dataCriacao) {
        val sdf = SimpleDateFormat("dd 'de' MMMM HH:mm", Locale("pt", "BR"))
        sdf.format(Date(nota.dataCriacao))
    }
    
    val caracteres = texto.length

    // Salvar automaticamente ao fechar
    DisposableEffect(Unit) {
        onDispose {
            if (texto.isNotBlank() || nota.texto.isNotBlank()) {
                onSalvar(nota.copy(texto = texto, corHex = corSelecionada.takeIf { it != "#FFFFFF" }))
            }
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(android.graphics.Color.parseColor(corSelecionada))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // TopBar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                    
                    Row {
                        Box {
                            IconButton(onClick = { showColorPicker = true }) {
                                Icon(Icons.Default.Palette, contentDescription = "Cor")
                            }
                            DropdownMenu(
                                expanded = showColorPicker,
                                onDismissRequest = { showColorPicker = false }
                            ) {
                                CoresNotaDeJogo.forEach { (hex, nome) ->
                                    DropdownMenuItem(
                                        text = { Text(nome) },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                            )
                                        },
                                        onClick = {
                                            corSelecionada = hex
                                            showColorPicker = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        IconButton(onClick = {
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, texto)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Compartilhar nota")
                            context.startActivity(shareIntent)
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Compartilhar")
                        }
                        
                        IconButton(onClick = {
                            onExcluir(nota.id)
                            onClose()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Excluir")
                        }
                    }
                }
                
                // Content
                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)) {
                    
                    val tituloExibicao = if (texto.isBlank()) "Título" else nota.copy(texto = texto).titulo
                    Text(
                        text = tituloExibicao,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "$dataFormatada | $caracteres caracteres",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextField(
                        value = texto,
                        onValueChange = { texto = it },
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        placeholder = { Text("Fazer notas do jogo...") }
                    )
                }
            }
        }
    }
}
