package com.gurps.ficha.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gurps.ficha.BuildConfig
import com.gurps.ficha.domain.filters.CatalogFilters.contemBusca
import com.gurps.ficha.model.PericiaSelecionada
import com.gurps.ficha.model.PericiaSuplementarItem
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.TecnicaCatalogoItem
import com.gurps.ficha.model.TecnicaSelecionada
import com.gurps.ficha.viewmodel.FichaViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelecionarTecnicaDialog(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit
) {
    var busca by remember { mutableStateOf("") }
    var filtroFonte by remember { mutableStateOf<String?>(null) }
    var tecnicaSelecionada by remember { mutableStateOf<TecnicaCatalogoItem?>(null) }

    val fontes = remember(viewModel.tecnicasCatalogo) {
        viewModel.tecnicasCatalogo.map { it.sourceBook }.distinct().sorted()
    }
    val tecnicas = viewModel.tecnicasCatalogo.filter { tecnica ->
        val matchBusca = busca.isBlank() ||
            contemBusca(tecnica.nome, busca) ||
            contemBusca(tecnica.descricao, busca)
        val matchFonte = filtroFonte.isNullOrBlank() || tecnica.sourceBook.equals(filtroFonte, ignoreCase = true)
        matchBusca && matchFonte
    }.sortedBy { com.gurps.ficha.domain.filters.CatalogFilters.normalizarBusca(it.nome) }

    // Lote LAYOUT-6.
    AppSelectionDialog(
        titulo = "Selecionar Técnica",
        busca = busca,
        onBusca = { busca = it },
        rotuloDaBusca = "Buscar técnica...",
        contador = contadorDe(tecnicas.size, "técnica", "técnicas"),
        filtros = {
            AppFiltroChip("Todas", filtroFonte == null) { filtroFonte = null }
            fontes.forEach { fonte ->
                AppFiltroChip(fonte, filtroFonte == fonte) { filtroFonte = fonte }
            }
        },
        onDismiss = onDismiss
    ) {
        items(tecnicas) { tecnica ->
            AppSelectionRow(
                nome = tecnica.nome,
                detalheADireita = "${tecnica.sourceBook} | ${tecnica.dificuldadeRaw}",
                onClick = { tecnicaSelecionada = tecnica }
            )
        }
    }

    tecnicaSelecionada?.let { definicao ->
        ConfigurarTecnicaDialog(
            viewModel = viewModel,
            definicao = definicao,
            onDismiss = { tecnicaSelecionada = null },
            onSave = { tecnicaSelecionada = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurarTecnicaDialog(
    viewModel: FichaViewModel,
    definicao: TecnicaCatalogoItem,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    val pericias = viewModel.personagem.pericias
    val periciasCompativeis = remember(pericias, definicao.id, definicao.preRequisitoRaw) {
        pericias.filter { viewModel.tecnicaAtendePreRequisito(definicao, it) }
    }
    var periciaSelecionadaId by remember { mutableStateOf<String?>(null) }
    var nivelRelativo by remember { mutableStateOf(0) }
    var erro by remember { mutableStateOf<String?>(null) }
    var mostrarDescricao by remember { mutableStateOf(false) }
    val preRequisitoExibicao = viewModel.preRequisitoExibicaoTecnica(definicao)

    val periciaBase = pericias.firstOrNull { pericia ->
        periciaTecnicaKey(pericia) == periciaSelecionadaId
    }
    LaunchedEffect(periciasCompativeis) {
        val selecionadaAtualValida = periciasCompativeis.any { periciaTecnicaKey(it) == periciaSelecionadaId }
        if (!selecionadaAtualValida) {
            periciaSelecionadaId = periciasCompativeis.firstOrNull()?.let { periciaTecnicaKey(it) } ?: periciaSelecionadaId
        }
    }
    val atendePreReq = periciaBase?.let { viewModel.tecnicaAtendePreRequisito(definicao, it) } ?: false
    val limiteMaximo = viewModel.limiteMaximoTecnica(definicao)
    val nivelMaximo = limiteMaximo ?: 24
    if (nivelRelativo > nivelMaximo) nivelRelativo = nivelMaximo

    val predefModificador = viewModel.dataRepository.extrairModificadorPredefinido(definicao.preDefinidoRaw)
    val custo = viewModel.custoTecnica(definicao, nivelRelativo)
    val nhTecnica = periciaBase?.let {
        viewModel.calcularNivelTecnicaPreview(definicao, it, nivelRelativo)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    definicao.nome,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize * 1.1f,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .let {
                            if (definicao.descricao.isNotBlank()) {
                                it.clickable { mostrarDescricao = true }
                            } else {
                                it
                            }
                        }
                        .semantics {
                            if (isPraCegoVariant && definicao.descricao.isNotBlank()) {
                                contentDescription = "Nome da técnica ${definicao.nome}. Toque para abrir descrição."
                            }
                        }
                )
                Text(
                    "${definicao.sourceBook} | ${definicao.dificuldadeRaw}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (preRequisitoExibicao.isNotBlank()) {
                    Text("Pré-requisito: $preRequisitoExibicao", style = MaterialTheme.typography.bodySmall)
                }
                if (definicao.preDefinidoRaw.isNotBlank()) {
                    Text("Pré-definido: ${definicao.preDefinidoRaw}", style = MaterialTheme.typography.bodySmall)
                }

                if (pericias.isEmpty()) {
                    Text(
                        "Adicione ao menos uma perícia antes de configurar técnicas.",
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (periciasCompativeis.isEmpty()) {
                    Text(
                        "Nenhuma perícia da ficha atende ao pré-requisito desta técnica.",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text("Perícia base:", style = MaterialTheme.typography.labelMedium)
                    periciasCompativeis.forEach { pericia ->
                        val key = periciaTecnicaKey(pericia)
                        FilterChip(
                            selected = periciaSelecionadaId == key,
                            onClick = {
                                periciaSelecionadaId = key
                                erro = null
                            },
                            label = { Text(periciaTecnicaLabel(pericia)) }
                        )
                    }
                }

                Text("Nível acima do predefinido:", style = MaterialTheme.typography.labelMedium)
                if (isPraCegoVariant) {
                    Row(horizontalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)) {
                        TextButton(
                            enabled = nivelRelativo > 0,
                            onClick = { nivelRelativo = (nivelRelativo - 1).coerceAtLeast(0) },
                            modifier = Modifier.semantics { contentDescription = "Diminuir nível da técnica" }
                        ) { Text("-") }
                        Text(
                            "+$nivelRelativo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            enabled = nivelRelativo < nivelMaximo,
                            onClick = { nivelRelativo = (nivelRelativo + 1).coerceAtMost(nivelMaximo) },
                            modifier = Modifier.semantics { contentDescription = "Aumentar nível da técnica" }
                        ) { Text("+") }
                    }
                } else {
                    Text(
                        text = "+$nivelRelativo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .width(56.dp)
                            .pointerInput(nivelRelativo) {
                                var dragAcumulado = 0f
                                val passoPx = 24f
                                detectVerticalDragGestures(
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        dragAcumulado += dragAmount
                                        while (abs(dragAcumulado) >= passoPx) {
                                            nivelRelativo = if (dragAcumulado < 0f) {
                                                (nivelRelativo + 1).coerceAtMost(nivelMaximo)
                                            } else {
                                                (nivelRelativo - 1).coerceAtLeast(0)
                                            }
                                            dragAcumulado += if (dragAcumulado < 0f) passoPx else -passoPx
                                        }
                                    }
                                )
                            },
                        textAlign = TextAlign.Center
                    )
                }
                if (limiteMaximo != null) {
                    Text("Limite máximo: predefinido +$limiteMaximo", style = MaterialTheme.typography.bodySmall)
                }

                Text("Custo automático: $custo ponto(s)", style = MaterialTheme.typography.bodyMedium)
                nhTecnica?.let {
                    Text(
                        "${definicao.nome} NH $it",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (periciaBase != null && !atendePreReq) {
                    Text(
                        "A perícia selecionada não atende ao pré-requisito.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                erro?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = periciaBase != null && atendePreReq,
                onClick = {
                    val pericia = periciaBase ?: run {
                        erro = "Selecione uma perícia base."
                        return@TextButton
                    }
                    val erroAdicionar = viewModel.adicionarTecnica(
                        definicao = definicao,
                        periciaBase = pericia,
                        nivelRelativoPredefinido = nivelRelativo
                    )
                    if (erroAdicionar != null) {
                        erro = erroAdicionar
                    } else {
                        onSave()
                    }
                }
            ) { Text(UiActionLabels.ADICIONAR) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(UiActionLabels.CANCELAR) }
        }
    )

    if (mostrarDescricao) {
        AlertDialog(
            onDismissRequest = { mostrarDescricao = false },
            title = { Text(definicao.nome) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        definicao.descricao.ifBlank { "Sem descrição detalhada disponível." },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (preRequisitoExibicao.isNotBlank()) {
                        Text("Pré-requisito: $preRequisitoExibicao", style = MaterialTheme.typography.bodySmall)
                    }
                    if (definicao.preDefinidoRaw.isNotBlank()) {
                        Text("Pré-definido: ${definicao.preDefinidoRaw}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { mostrarDescricao = false },
                    modifier = Modifier.semantics {
                        if (isPraCegoVariant) contentDescription = "Fechar descrição da técnica"
                    }
                ) { Text(UiActionLabels.FECHAR) }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarTecnicaDialog(
    viewModel: FichaViewModel,
    tecnica: TecnicaSelecionada,
    personagem: Personagem,
    onDismiss: () -> Unit,
    onSave: (TecnicaSelecionada) -> Unit
) {
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    val definicaoCatalogo = remember(viewModel.tecnicasCatalogo, tecnica.definicaoId) {
        viewModel.tecnicasCatalogo.firstOrNull { it.id.equals(tecnica.definicaoId, ignoreCase = true) }
    }
    val preRequisitoExibicao = definicaoCatalogo?.let { viewModel.preRequisitoExibicaoTecnica(it) }
        ?: tecnica.preRequisitoRaw
    val descricaoCatalogo = definicaoCatalogo?.descricao.orEmpty()

    var nivelRelativo by remember { mutableStateOf(tecnica.nivelRelativoPredefinido.coerceAtLeast(0)) }
    var mostrarDescricao by remember { mutableStateOf(false) }
    var periciaSelecionadaId by remember {
        mutableStateOf(
            periciaTecnicaKey(
                PericiaSelecionada(
                    definicaoId = tecnica.periciaBaseDefinicaoId,
                    nome = tecnica.periciaBaseNome,
                    especializacao = tecnica.periciaBaseEspecializacao
                )
            )
        )
    }

    val pericias = personagem.pericias
    val periciasCompativeis = remember(pericias, tecnica.definicaoId, definicaoCatalogo, tecnica.preRequisitoRaw) {
        definicaoCatalogo?.let { definicao ->
            pericias.filter { viewModel.tecnicaAtendePreRequisito(definicao, it) }
        } ?: pericias
    }
    LaunchedEffect(periciasCompativeis) {
        val selecionadaAtualValida = periciasCompativeis.any { periciaTecnicaKey(it) == periciaSelecionadaId }
        if (!selecionadaAtualValida) {
            val primeiraCompativel = periciasCompativeis.firstOrNull()
            if (primeiraCompativel != null) {
                periciaSelecionadaId = periciaTecnicaKey(primeiraCompativel)
            }
        }
    }
    val periciaBase = pericias.firstOrNull { periciaTecnicaKey(it) == periciaSelecionadaId }
    val predefModificador = tecnica.preDefinidoModificador
    val limiteMaximo = tecnica.limiteMaximoRelativo
    val nivelMaximo = limiteMaximo ?: 24
    if (nivelRelativo > nivelMaximo) nivelRelativo = nivelMaximo
    val dificuldadeDificil = tecnica.dificuldadeRaw.lowercase().contains("dif")
    val custo = if (nivelRelativo == 0) 0 else if (dificuldadeDificil) nivelRelativo + 1 else nivelRelativo
    val nhTecnica = periciaBase?.let {
        tecnica.copy(
            periciaBaseDefinicaoId = it.definicaoId,
            periciaBaseNome = it.nome,
            periciaBaseEspecializacao = it.especializacao,
            nivelRelativoPredefinido = nivelRelativo
        ).calcularNivel(personagem)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    tecnica.nome,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize * 1.1f,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .let {
                            if (descricaoCatalogo.isNotBlank()) {
                                it.clickable { mostrarDescricao = true }
                            } else {
                                it
                            }
                        }
                        .semantics {
                            if (isPraCegoVariant && descricaoCatalogo.isNotBlank()) {
                                contentDescription = "Nome da técnica ${tecnica.nome}. Toque para abrir descrição."
                            }
                        }
                )
                Text("${tecnica.sourceBook} | ${tecnica.dificuldadeRaw}", style = MaterialTheme.typography.bodySmall)
                if (preRequisitoExibicao.isNotBlank()) {
                    Text("Pré-requisito: $preRequisitoExibicao", style = MaterialTheme.typography.bodySmall)
                }
                if (tecnica.preDefinidoRaw.isNotBlank()) {
                    Text("Pré-definido: ${tecnica.preDefinidoRaw}", style = MaterialTheme.typography.bodySmall)
                }

                if (pericias.isEmpty()) {
                    Text(
                        "Adicione ao menos uma perícia antes de editar técnicas.",
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (periciasCompativeis.isEmpty()) {
                    Text(
                        "Nenhuma perícia da ficha atende ao pré-requisito desta técnica.",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text("Perícia base:", style = MaterialTheme.typography.labelMedium)
                    periciasCompativeis.forEach { pericia ->
                        val key = periciaTecnicaKey(pericia)
                        FilterChip(
                            selected = periciaSelecionadaId == key,
                            onClick = { periciaSelecionadaId = key },
                            label = { Text(periciaTecnicaLabel(pericia)) }
                        )
                    }
                }

                Text("Nível acima do predefinido:", style = MaterialTheme.typography.labelMedium)
                if (isPraCegoVariant) {
                    Row(horizontalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)) {
                        TextButton(
                            enabled = nivelRelativo > 0,
                            onClick = { nivelRelativo = (nivelRelativo - 1).coerceAtLeast(0) },
                            modifier = Modifier.semantics { contentDescription = "Diminuir nível da técnica" }
                        ) { Text("-") }
                        Text(
                            "+$nivelRelativo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            enabled = nivelRelativo < nivelMaximo,
                            onClick = { nivelRelativo = (nivelRelativo + 1).coerceAtMost(nivelMaximo) },
                            modifier = Modifier.semantics { contentDescription = "Aumentar nível da técnica" }
                        ) { Text("+") }
                    }
                } else {
                    Text(
                        text = "+$nivelRelativo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .width(56.dp)
                            .pointerInput(nivelRelativo) {
                                var dragAcumulado = 0f
                                val passoPx = 24f
                                detectVerticalDragGestures(
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        dragAcumulado += dragAmount
                                        while (abs(dragAcumulado) >= passoPx) {
                                            nivelRelativo = if (dragAcumulado < 0f) {
                                                (nivelRelativo + 1).coerceAtMost(nivelMaximo)
                                            } else {
                                                (nivelRelativo - 1).coerceAtLeast(0)
                                            }
                                            dragAcumulado += if (dragAcumulado < 0f) passoPx else -passoPx
                                        }
                                    }
                                )
                            },
                        textAlign = TextAlign.Center
                    )
                }
                if (limiteMaximo != null) {
                    Text("Limite máximo: predefinido +$limiteMaximo", style = MaterialTheme.typography.bodySmall)
                }

                Text("Custo automático: $custo ponto(s)", style = MaterialTheme.typography.bodyMedium)
                nhTecnica?.let {
                    Text(
                        "${tecnica.nome} NH $it",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = periciaBase != null,
                onClick = {
                    val pericia = periciaBase ?: return@TextButton
                    onSave(
                        tecnica.copy(
                            nivelRelativoPredefinido = nivelRelativo,
                            pontosGastos = custo,
                            periciaBaseDefinicaoId = pericia.definicaoId,
                            periciaBaseNome = pericia.nome,
                            periciaBaseEspecializacao = pericia.especializacao
                        )
                    )
                }
            ) { Text(UiActionLabels.SALVAR) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(UiActionLabels.CANCELAR) }
        }
    )

    if (mostrarDescricao) {
        AlertDialog(
            onDismissRequest = { mostrarDescricao = false },
            title = { Text(tecnica.nome) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        descricaoCatalogo.ifBlank { "Sem descrição detalhada disponível." },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (preRequisitoExibicao.isNotBlank()) {
                        Text("Pré-requisito: $preRequisitoExibicao", style = MaterialTheme.typography.bodySmall)
                    }
                    if (tecnica.preDefinidoRaw.isNotBlank()) {
                        Text("Pré-definido: ${tecnica.preDefinidoRaw}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { mostrarDescricao = false },
                    modifier = Modifier.semantics {
                        if (isPraCegoVariant) contentDescription = "Fechar descrição da técnica"
                    }
                ) { Text(UiActionLabels.FECHAR) }
            }
        )
    }
}

@Composable
fun PericiasSuplementaresDialog(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit
) {
    var busca by remember { mutableStateOf("") }
    var itemDetalhes by remember { mutableStateOf<PericiaSuplementarItem?>(null) }
    val itens = viewModel.periciasSuplementaresArtesMarciais.filter { pericia ->
        busca.isBlank() ||
            contemBusca(pericia.nome, busca) ||
            contemBusca(pericia.descricao, busca)
    }

    FullscreenDialogContainer(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Perícias Suplementares", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = busca,
                onValueChange = { busca = it },
                label = { Text("Buscar perícia...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, null) }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(contadorDe(itens.size, "perícia", "perícias"), style = UiEstilos.detalheDoItem)
            Spacer(modifier = Modifier.height(4.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(itens) { pericia ->
                    PericiaSuplementarCard(
                        item = pericia,
                        onOpenDetails = { itemDetalhes = pericia }
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text(UiActionLabels.FECHAR) }
            }
        }
    }

    itemDetalhes?.let { item ->
        PericiaSuplementarDetalhesDialog(
            item = item,
            onDismiss = { itemDetalhes = null }
        )
    }
}

@Composable
private fun PericiaSuplementarCard(
    item: PericiaSuplementarItem,
    onOpenDetails: () -> Unit
) {
    // Lote LAYOUT-6: o botão "Detalhes" some — o toque na linha inteira já faz
    // isso, e é o gesto das outras listas. Um botão para a única ação da linha
    // era um alvo pequeno dentro de um alvo grande.
    AppSelectionRow(
        nome = item.nome,
        detalheADireita = "${item.sourceBook} | ${item.dificuldadeRaw}",
        onClick = onOpenDetails,
        descricaoAcessivel = "${item.nome}. ${item.sourceBook}, ${item.dificuldadeRaw}. " +
            "Toque para abrir os detalhes."
    )
}

@Composable
private fun PericiaSuplementarDetalhesDialog(
    item: PericiaSuplementarItem,
    onDismiss: () -> Unit
) {
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.nome) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)
            ) {
                Text(
                    "${item.sourceBook} • ${item.dificuldadeRaw}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                PericiaDetalhesSecao(
                    titulo = "O que faz",
                    conteudo = item.descricao.ifBlank { "Sem descrição detalhada." }
                )
                PericiaDetalhesSecao(
                    titulo = "Pré-requisito",
                    conteudo = item.preRequisitoRaw.ifBlank { "Sem pré-requisito." }
                )
                PericiaDetalhesSecao(
                    titulo = "Pré-definido",
                    conteudo = item.preDefinidoRaw.ifBlank { "Sem pré-definido." }
                )
                if (item.modificadores.isNotBlank()) {
                    PericiaDetalhesSecao(
                        titulo = "Modificadores",
                        conteudo = item.modificadores
                    )
                }
                if (isPraCegoVariant) {
                    Text(
                        "Resumo rápido: ${item.nome}. ${item.preRequisitoRaw.ifBlank { "Sem pré-requisito." }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.semantics {
                    contentDescription = "Fechar detalhes da perícia ${item.nome}"
                }
            ) { Text(UiActionLabels.FECHAR) }
        }
    )
}

@Composable
private fun PericiaDetalhesSecao(
    titulo: String,
    conteudo: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            titulo,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { heading() }
        )
        Text(conteudo, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun periciaTecnicaKey(pericia: PericiaSelecionada): String {
    return "${pericia.definicaoId}|${pericia.especializacao.lowercase()}"
}

private fun periciaTecnicaLabel(pericia: PericiaSelecionada): String {
    return if (pericia.especializacao.isBlank()) {
        pericia.nome
    } else {
        "${pericia.nome} (${pericia.especializacao})"
    }
}





