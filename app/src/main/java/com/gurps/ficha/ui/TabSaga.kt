package com.gurps.ficha.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.saga.CampanhaConfig
import com.gurps.ficha.viewmodel.FichaViewModel
import com.gurps.ficha.viewmodel.delegates.SagaTurn
import kotlinx.coroutines.delay

/**
 * Lote 354 (Saga A5): Aba Saga. Sem campanha ativa → escolha/criação; com campanha →
 * feed da cena + barra de envio + card de rolagem (toque rola o dado real). Máquina de
 * escrever local no último turno do Narrador. Semântica TalkBack em tudo.
 */
@Composable
fun TabSaga(viewModel: FichaViewModel) {
    LaunchedEffect(Unit) { viewModel.sagaCarregarCampanhas() }

    if (viewModel.sagaCampanhaAtiva == null) {
        SelecaoDeCampanha(viewModel)
    } else {
        FeedDaCampanha(viewModel)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelecaoDeCampanha(viewModel: FichaViewModel) {
    var nome by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("") }
    var conceito by remember { mutableStateOf("") }
    var tom by remember { mutableStateOf("Heroico") }
    var dificuldade by remember { mutableStateOf("Normal") }
    var magia by remember { mutableStateOf(true) }
    var nt by remember { mutableStateOf(3) }
    val livros = remember { mutableStateListOf(CampanhaConfig.MODULO_BASICO) }
    var idParaExcluir by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("GURPS Saga", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(
            "Jogue uma aventura solo narrada pela IA, usando a ficha atual como herói.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome da campanha") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Nome da nova campanha" }
        )

        // Gênero
        SecaoConfig("Gênero") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CampanhaConfig.GENEROS.forEach { g ->
                    FilterChip(
                        selected = genero == g,
                        onClick = { genero = if (genero == g) "" else g },
                        label = { Text(g) },
                        modifier = Modifier.semantics { contentDescription = "Gênero $g" }
                    )
                }
            }
        }

        OutlinedTextField(
            value = conceito,
            onValueChange = { conceito = it },
            label = { Text("Conceito (opcional)") },
            placeholder = { Text("ex.: caçador de recompensas num faroeste sombrio") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).semantics { contentDescription = "Conceito da campanha" },
            maxLines = 3
        )

        // Tom
        SecaoConfig("Tom") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CampanhaConfig.TONS.forEach { t ->
                    FilterChip(selected = tom == t, onClick = { tom = t }, label = { Text(t) },
                        modifier = Modifier.semantics { contentDescription = "Tom $t" })
                }
            }
        }

        // Dificuldade
        SecaoConfig("Dificuldade") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CampanhaConfig.DIFICULDADES.forEach { d ->
                    FilterChip(selected = dificuldade == d, onClick = { dificuldade = d }, label = { Text(d) },
                        modifier = Modifier.semantics { contentDescription = "Dificuldade $d" })
                }
            }
        }

        // Conteúdo: magia
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Magia neste mundo", modifier = Modifier.weight(1f))
            Switch(checked = magia, onCheckedChange = { magia = it },
                modifier = Modifier.semantics { contentDescription = "Permitir magia" })
        }

        // Nível tecnológico (stepper)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Nível tecnológico (NT)", modifier = Modifier.weight(1f))
            OutlinedButton(onClick = { if (nt > 0) nt-- },
                modifier = Modifier.semantics { contentDescription = "Diminuir nível tecnológico" }) { Text("−") }
            Text("NT$nt", modifier = Modifier.padding(horizontal = 12.dp), fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = { if (nt < 12) nt++ },
                modifier = Modifier.semantics { contentDescription = "Aumentar nível tecnológico" }) { Text("+") }
        }

        // Livros liberados
        SecaoConfig("Regras/livros liberados") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CampanhaConfig.LIVROS_DISPONIVEIS.forEach { livro ->
                    val obrigatorio = livro == CampanhaConfig.MODULO_BASICO
                    val sel = livro in livros
                    FilterChip(
                        selected = sel,
                        onClick = {
                            if (!obrigatorio) {
                                if (sel) livros.remove(livro) else livros.add(livro)
                            }
                        },
                        label = { Text(livro) },
                        modifier = Modifier.semantics { contentDescription = "Livro $livro ${if (sel) "incluído" else "fora"}" }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.sagaCriarCampanha(
                    nome,
                    CampanhaConfig(
                        genero = genero, conceito = conceito.trim(), tom = tom,
                        dificuldade = dificuldade, magiaPermitida = magia,
                        nivelTecnologico = nt, livros = livros.toList()
                    )
                )
            },
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Criar campanha e começar a aventura" }
        ) { Text("Criar campanha") }

        val campanhas = viewModel.sagaCampanhas
        if (campanhas.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text("Continuar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            campanhas.forEach { camp ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.sagaContinuarCampanha(camp.id) }
                                .padding(12.dp)
                                .semantics { contentDescription = "Continuar campanha ${camp.nome}, capítulo ${camp.capituloAtual}" }
                        ) {
                            Text(camp.nome, fontWeight = FontWeight.Bold)
                            Text(
                                "Capítulo ${camp.capituloAtual} · cenário ${camp.cenarioId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { idParaExcluir = camp.id },
                            modifier = Modifier.semantics { contentDescription = "Excluir campanha ${camp.nome}" }
                        ) { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    }

    idParaExcluir?.let { id ->
        val nomeCamp = viewModel.sagaCampanhas.firstOrNull { it.id == id }?.nome ?: "campanha"
        AlertDialog(
            onDismissRequest = { idParaExcluir = null },
            title = { Text("Excluir campanha?") },
            text = { Text("\"$nomeCamp\" e todo o seu progresso (cena, fatos e histórico) serão apagados. Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = { viewModel.sagaExcluirCampanha(id); idParaExcluir = null }) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { idParaExcluir = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun SecaoConfig(titulo: String, conteudo: @Composable () -> Unit) {
    Spacer(Modifier.height(12.dp))
    Text(titulo, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(4.dp))
    conteudo()
}

@Composable
private fun FeedDaCampanha(viewModel: FichaViewModel) {
    val feed = viewModel.sagaFeed
    val processando = viewModel.sagaProcessando
    val fase = viewModel.sagaFase
    val rolagem = viewModel.sagaRolagemPendente
    val listState = rememberLazyListState()

    LaunchedEffect(feed.size) {
        if (feed.isNotEmpty()) listState.animateScrollToItem(feed.size - 1)
    }

    Column(Modifier.fillMaxSize()) {
        // Cabeçalho da campanha
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    viewModel.sagaCampanhaAtiva?.nome ?: "Campanha",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                viewModel.sagaCenaAtiva?.let {
                    Text("Cena: ${it.titulo}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            TextButton(
                onClick = { viewModel.sagaSair() },
                modifier = Modifier.semantics { contentDescription = "Sair para a lista de campanhas" }
            ) { Text("Trocar") }
        }
        HorizontalDivider()

        // Feed
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            if (feed.isEmpty() && !processando) {
                item {
                    Text(
                        "O Narrador está preparando a cena de abertura…",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            items(feed, key = { it.uid }) { turno ->
                val ehUltimoNarrador = turno.role == "narrador" && turno.uid == feed.lastOrNull()?.uid
                TurnoBolha(turno, animar = ehUltimoNarrador)
            }
        }

        // Indicador de fase (alimentado pelos nomes das tools)
        if (processando && fase.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                        contentDescription = fase
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text(fase, style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
            }
        }

        // Card de rolagem (quando o Narrador pede)
        if (rolagem != null) {
            CardDeRolagem(viewModel, rolagem)
        }

        // Barra de envio
        BarraDeEnvio(viewModel, habilitado = !processando && rolagem == null)
    }
}

@Composable
private fun TurnoBolha(turno: SagaTurn, animar: Boolean) {
    val (rotulo, cor) = when (turno.role) {
        "jogador" -> "Você" to MaterialTheme.colorScheme.primaryContainer
        "sistema" -> "Dado" to MaterialTheme.colorScheme.tertiaryContainer
        else -> "Narrador" to MaterialTheme.colorScheme.surfaceVariant
    }
    // Máquina de escrever local: revela 2-3 palavras a cada ~30ms no último turno do Narrador.
    var textoVisivel by remember(turno.uid) { mutableStateOf(if (animar) "" else turno.texto) }
    if (animar) {
        LaunchedEffect(turno.uid) {
            val palavras = turno.texto.split(" ")
            val sb = StringBuilder()
            var i = 0
            while (i < palavras.size) {
                val fim = minOf(i + 3, palavras.size)
                for (j in i until fim) { if (sb.isNotEmpty()) sb.append(" "); sb.append(palavras[j]) }
                textoVisivel = sb.toString()
                i = fim
                delay(30)
            }
            textoVisivel = turno.texto
        }
    }
    Surface(
        color = cor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "$rotulo: ${turno.texto}" }
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(rotulo, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(if (animar) textoVisivel else turno.texto, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun CardDeRolagem(viewModel: FichaViewModel, req: com.gurps.ficha.viewmodel.delegates.SagaRollRequest) {
    val descricao = buildString {
        append("Pedido de rolagem: ${req.pericia}, alvo ${req.alvo}.")
        if (req.mods.isNotEmpty()) append(" Modificadores: " + req.mods.joinToString(", ") { "${it.first} ${it.second}" } + ".")
        append(" Toque para rolar três dados.")
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .semantics { contentDescription = descricao },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("🎲 ${req.pericia}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (req.motivo.isNotBlank()) {
                Text(req.motivo, style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
            }
            Spacer(Modifier.height(6.dp))
            Text("NH base: ${req.nhBase}" + if (!req.periciaEncontrada) " (perícia não encontrada na ficha — usando base neutra)" else "",
                style = MaterialTheme.typography.bodySmall)
            req.mods.forEach { (motivo, valor) ->
                val sinal = if (valor >= 0) "+$valor" else "$valor"
                Text("• $motivo: $sinal", style = MaterialTheme.typography.bodySmall)
            }
            Text("Alvo final: ${req.alvo}", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = { viewModel.sagaRolarDado() },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Rolar 3d6 contra alvo ${req.alvo}" }
            ) { Text("Rolar 3d6") }
        }
    }
}

@Composable
private fun BarraDeEnvio(viewModel: FichaViewModel, habilitado: Boolean) {
    var texto by remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = texto,
            onValueChange = { texto = it },
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = "O que seu herói faz" },
            placeholder = { Text("O que você faz?") },
            enabled = habilitado,
            maxLines = 4
        )
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = {
                val t = texto.trim()
                if (t.isNotEmpty()) { viewModel.sagaEnviar(t); texto = "" }
            },
            enabled = habilitado && texto.isNotBlank(),
            modifier = Modifier.semantics { contentDescription = "Enviar ação ao Narrador" }
        ) { Text("Agir") }
    }
}
