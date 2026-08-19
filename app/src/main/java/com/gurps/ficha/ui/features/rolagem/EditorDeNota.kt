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
import com.gurps.ficha.domain.rules.CorDaNota
import com.gurps.ficha.model.NotaDeJogo
import java.text.SimpleDateFormat
import java.util.*

/**
 * **A cor da nota** — as sete do Google Keep, e pelo mesmo motivo.
 *
 * São tons **100** do Material: claros o bastante para texto escuro por cima.
 * A cor aqui não é enfeite — é o único jeito de separar um caderno de notas
 * soltas sem inventar pastas nem marcadores.
 *
 * 🔴 **E aqui morou um defeito, corrigido no Lote NOTA-3.** O fundo é sempre
 * claro, mas o texto saía de `MaterialTheme.colorScheme.onSurface` — que no tema
 * **escuro** é quase branco. Num aparelho com o tema escuro do sistema, a nota
 * ficava branca sobre amarelo-claro: ilegível. O app segue o tema do sistema
 * (`isSystemInDarkTheme()` no `Theme.kt`), então acontecia de verdade — só que
 * **no aparelho de outra pessoa**, nunca no de quem escreveu a tela.
 *
 * A cura está em [com.gurps.ficha.domain.rules.CorDaNota]: a cor do texto sai da
 * luminância do **fundo**, e não do tema.
 */
val CoresNotaDeJogo = listOf(
    "#FFFFFF" to "Branco",
    "#FFF9C4" to "Amarelo", // Yellow 100
    "#F8BBD0" to "Rosa",    // Pink 100
    "#C8E6C9" to "Verde",   // Green 100
    "#BBDEFB" to "Azul",    // Blue 100
    "#E1BEE7" to "Roxo",    // Purple 100
    "#FFCCBC" to "Laranja"  // Deep Orange 100
)

/**
 * **Escrever uma nota** — Lote NOTA-1.
 *
 * A tela cheia de uma anotação: o texto, a cor, compartilhar e excluir. Abre
 * tanto para uma nota nova quanto para uma existente — do lado de cá não há
 * diferença, e é por isso que [FichaNotesDelegate.salvarNota] é uma porta só.
 *
 * ## Não existe botão de salvar, e é de propósito
 *
 * A gravação acontece no `onDispose`: fechar É salvar. Numa mesa de RPG a
 * anotação é interrompida — o Mestre chama, o dado rola, alguém pergunta uma
 * regra. Um botão de salvar transforma toda interrupção em texto perdido.
 *
 * 🔴 **E isso custou um defeito, corrigido no Lote NOTA-3: excluir não excluía.**
 *
 * O botão de excluir chama `onExcluir(nota.id)` e logo `onClose()`. O `onClose`
 * tira esta tela da composição, o `onDispose` dispara e **gravava a nota de
 * volta** — porque para o delegate um `id` que não está na lista quer dizer
 * "nota nova". Apagou, fechou, ressuscitou.
 *
 * A cura é a bandeira [foiExcluida], marcada **antes** do `onClose` e conferida
 * dentro do `onDispose`. ⚠️ A ordem é a correção inteira: marcá-la depois não
 * adiantaria, porque é o `onClose` que dispara o `onDispose`.
 *
 * ## Compartilhar para fora do app
 *
 * `Intent.ACTION_SEND` com `text/plain` e o seletor do próprio Android: cai no
 * WhatsApp, no e-mail, no bloco de notas do sistema, no que a pessoa tiver.
 * ⚠️ Vai **só o texto** — cor e datas ficam para trás, porque do outro lado
 * ninguém sabe o que fazer com elas.
 *
 * O envio para o **Discord** não é aqui: é no [DialogoBlocoDeNotas], a partir
 * da lista, e passa por uma confirmação.
 *
 * @param onSalvar chamado ao fechar, com a nota já com o texto e a cor atuais
 * @param onExcluir recebe o `id`; quem fecha a tela é o chamador
 */
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

    /**
     * 🔴 Excluir precisa avisar o `onDispose` — senão a nota volta.
     *
     * O botão de excluir apaga e fecha; fechar tira esta tela da composição, o
     * `onDispose` dispara e **grava a nota de volta**, porque para o delegate um
     * `id` que não está na lista quer dizer "nota nova". Apagou, fechou,
     * ressuscitou — e o jogador só descobre ao ver a nota de pé outra vez.
     */
    var foiExcluida by remember { mutableStateOf(false) }

    /**
     * 🔴 A cor do texto sai do FUNDO, e não do tema.
     *
     * O fundo é sempre um tom claro; `onSurface` no tema **escuro** é quase
     * branco. Ver [CorDaNota] — o defeito só aparecia em aparelho com tema
     * escuro, ou seja, no de outra pessoa.
     */
    val corDoTexto = Color(android.graphics.Color.parseColor(CorDaNota.textoSobre(corSelecionada)))

    val dataFormatada = remember(nota.dataCriacao) {
        val sdf = SimpleDateFormat("dd 'de' MMMM HH:mm", Locale("pt", "BR"))
        sdf.format(Date(nota.dataCriacao))
    }
    
    val caracteres = texto.length

    // Salvar automaticamente ao fechar
    DisposableEffect(Unit) {
        onDispose {
            if (!foiExcluida && (texto.isNotBlank() || nota.texto.isNotBlank())) {
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = corDoTexto)
                    }
                    
                    Row {
                        Box {
                            IconButton(onClick = { showColorPicker = true }) {
                                Icon(Icons.Default.Palette, contentDescription = "Cor", tint = corDoTexto)
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
                            Icon(Icons.Default.Share, contentDescription = "Compartilhar", tint = corDoTexto)
                        }
                        
                        IconButton(onClick = {
                            // ⚠️ ANTES de fechar: o `onClose` é que dispara o
                            // `onDispose`, e ele lê esta bandeira.
                            foiExcluida = true
                            onExcluir(nota.id)
                            onClose()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = corDoTexto)
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
                        color = corDoTexto.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "$dataFormatada | $caracteres caracteres",
                        style = MaterialTheme.typography.bodySmall,
                        color = corDoTexto.copy(alpha = 0.7f)
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
                            // ⚠️ Sem estas quatro, o texto que a pessoa escreve
                            // continua saindo do tema — e era ele o mais ilegivel.
                            focusedTextColor = corDoTexto,
                            unfocusedTextColor = corDoTexto,
                            cursorColor = corDoTexto,
                            focusedPlaceholderColor = corDoTexto.copy(alpha = 0.5f),
                            unfocusedPlaceholderColor = corDoTexto.copy(alpha = 0.5f),
                        ),
                        placeholder = { Text("Fazer notas do jogo...") }
                    )
                }
            }
        }
    }
}
