package com.gurps.ficha.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

@Composable
private fun SelecaoDeCampanha(viewModel: FichaViewModel) {
    var nome by remember { mutableStateOf("") }
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
            label = { Text("Nome da nova campanha") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Nome da nova campanha" }
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { viewModel.sagaCriarCampanha(nome) },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Criar campanha e começar a aventura" }
        ) { Text("Criar campanha") }

        val campanhas = viewModel.sagaCampanhas
        if (campanhas.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text("Continuar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            campanhas.forEach { camp ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .semantics { contentDescription = "Continuar campanha ${camp.nome}, capítulo ${camp.capituloAtual}" },
                    onClick = { viewModel.sagaContinuarCampanha(camp.id) }
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(camp.nome, fontWeight = FontWeight.Bold)
                        Text(
                            "Capítulo ${camp.capituloAtual} · cenário ${camp.cenarioId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
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
