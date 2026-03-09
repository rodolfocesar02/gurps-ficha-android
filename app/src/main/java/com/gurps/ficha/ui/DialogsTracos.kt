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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gurps.ficha.BuildConfig
import com.gurps.ficha.model.DesvantagemDefinicao
import com.gurps.ficha.model.DesvantagemSelecionada
import com.gurps.ficha.model.TipoCusto
import com.gurps.ficha.model.VantagemDefinicao
import com.gurps.ficha.model.VantagemSelecionada
import com.gurps.ficha.viewmodel.FichaViewModel
import kotlin.math.abs

private fun vantagemEhAptidaoMagica(definicaoId: String): Boolean {
    return definicaoId.equals("aptidao_magica", ignoreCase = true)
}

private fun nivelExibicaoVantagem(definicaoId: String, nivelInterno: Int): Int {
    return if (vantagemEhAptidaoMagica(definicaoId)) {
        (nivelInterno - 1).coerceAtLeast(0)
    } else {
        nivelInterno
    }
}

private fun custoVantagemPorNivelExibicao(definicao: VantagemDefinicao, nivelInterno: Int): Int {
    return if (vantagemEhAptidaoMagica(definicao.id)) {
        5 + (nivelInterno - 1).coerceAtLeast(0) * 10
    } else {
        definicao.getCustoPorNivel() * nivelInterno
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelecionarVantagemDialog(viewModel: FichaViewModel, onDismiss: () -> Unit) {
    var busca by remember { mutableStateOf("") }
    var filtroTipo by remember { mutableStateOf<TipoCusto?>(null) }
    var filtroTag by remember { mutableStateOf<String?>(null) }
    var vantagemSelecionada by remember { mutableStateOf<VantagemDefinicao?>(null) }

    val tagsDisponiveis = listOf("combate", "social", "fisica", "mental", "magica")
    val listaFiltrada = viewModel.dataRepository.filtrarVantagens(busca, filtroTipo, filtroTag)

    FullscreenDialogContainer(onDismiss = onDismiss) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text("Selecionar Vantagem", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = busca, onValueChange = { busca = it }, label = { Text("Buscar...") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) })

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)
                ) {
                    FilterChip(
                        selected = filtroTag == null,
                        onClick = { filtroTag = null },
                        label = { Text("Todas") }
                    )
                    tagsDisponiveis.forEach { tag ->
                        FilterChip(
                            selected = filtroTag == tag,
                            onClick = { filtroTag = tag },
                            label = { Text(tag) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("${listaFiltrada.size} vantagens encontradas", style = MaterialTheme.typography.bodySmall)

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(listaFiltrada) { definicao ->
                        val jaAdicionada = viewModel.vantagemJaAdicionada(definicao.id)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !jaAdicionada) { vantagemSelecionada = definicao },
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        definicao.nome,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = if (jaAdicionada) FontWeight.Normal else FontWeight.Medium
                                    )
                                    Text(
                                        "${definicao.custo} pts | ${definicao.tipoCusto.name.lowercase()} | pag. ${definicao.pagina}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (jaAdicionada) {
                                    Text("Adicionada", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Fechar") }
                }
            }
    }

    vantagemSelecionada?.let { definicao ->
        ConfigurarVantagemDialog(definicao = definicao, onDismiss = { vantagemSelecionada = null },
            onSave = { nivel, custoEscolhido, descricao ->
                viewModel.adicionarVantagem(definicao, nivel, custoEscolhido, descricao)
                vantagemSelecionada = null
            })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurarVantagemDialog(definicao: VantagemDefinicao, onDismiss: () -> Unit, onSave: (Int, Int, String) -> Unit) {
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    var nivel by remember { mutableStateOf(1) }
    var custoEscolhido by remember { mutableStateOf(definicao.getCustoBase()) }
    var descricao by remember { mutableStateOf("") }
    val descricaoCatalogo = definicao.descricao?.trim().orEmpty()
    var mostrarDescricaoCatalogo by remember { mutableStateOf(false) }

    val opcoesEscolha = definicao.getOpcoesEscolha()
    val intervalo = definicao.getIntervaloVariavel()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                definicao.nome,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .let {
                        if (descricaoCatalogo.isNotBlank()) {
                            it.clickable { mostrarDescricaoCatalogo = true }
                        } else {
                            it
                        }
                    }
                    .semantics {
                        if (isPraCegoVariant && descricaoCatalogo.isNotBlank()) {
                            contentDescription = "Nome da vantagem ${definicao.nome}. Toque para abrir descricao."
                        }
                    }
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)) {
                Text("Tipo: ${definicao.tipoCusto.name} | Custo base: ${definicao.custo} | Pag. ${definicao.pagina}", style = MaterialTheme.typography.bodySmall)

                when (definicao.tipoCusto) {
                    TipoCusto.POR_NIVEL -> {
                        Text("Nível:")
                        val nivelMinimo = 1
                        val nivelMaximo = if (vantagemEhAptidaoMagica(definicao.id)) 11 else 10
                        if (isPraCegoVariant) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { if (nivel > nivelMinimo) nivel-- },
                                    modifier = Modifier.semantics { contentDescription = "Diminuir nível de vantagem" }
                                ) { Text("-") }
                                Text(
                                    "${nivelExibicaoVantagem(definicao.id, nivel)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                TextButton(
                                    onClick = { if (nivel < nivelMaximo) nivel++ },
                                    modifier = Modifier.semantics { contentDescription = "Aumentar nível de vantagem" }
                                ) { Text("+") }
                            }
                        } else {
                            Text(
                                "${nivelExibicaoVantagem(definicao.id, nivel)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .width(56.dp)
                                    .pointerInput(nivel) {
                                        var dragAcumulado = 0f
                                        val passoPx = 24f
                                        detectVerticalDragGestures(
                                            onVerticalDrag = { change, dragAmount ->
                                                change.consume()
                                                dragAcumulado += dragAmount
                                                while (abs(dragAcumulado) >= passoPx) {
                                                    nivel = if (dragAcumulado < 0f) {
                                                        (nivel + 1).coerceAtMost(nivelMaximo)
                                                    } else {
                                                        (nivel - 1).coerceAtLeast(nivelMinimo)
                                                    }
                                                    dragAcumulado += if (dragAcumulado < 0f) passoPx else -passoPx
                                                }
                                            }
                                        )
                                    },
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                        Text("Custo: ${custoVantagemPorNivelExibicao(definicao, nivel)} pts", fontWeight = FontWeight.Bold)
                    }
                    TipoCusto.ESCOLHA -> {
                        Text("Escolha o custo:")
                        opcoesEscolha.forEach { opcao ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { custoEscolhido = opcao }) {
                                RadioButton(selected = custoEscolhido == opcao, onClick = { custoEscolhido = opcao })
                                Text("$opcao pts")
                            }
                        }
                    }
                    TipoCusto.VARIAVEL -> {
                        Text("Custo (${intervalo.first} a ${intervalo.second}):")
                        OutlinedTextField(value = custoEscolhido.toString(), onValueChange = { custoEscolhido = it.toIntOrNull() ?: intervalo.first },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                    TipoCusto.FIXO -> {
                        Text("Custo fixo: ${definicao.getCustoBase()} pts", fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(value = descricao, onValueChange = { descricao = it },
                    label = { Text("Descrição/Especialização") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val custoFinal = when (definicao.tipoCusto) {
                    TipoCusto.POR_NIVEL -> custoVantagemPorNivelExibicao(definicao, nivel)
                    TipoCusto.ESCOLHA, TipoCusto.VARIAVEL -> custoEscolhido
                    TipoCusto.FIXO -> definicao.getCustoBase()
                }
                onSave(nivel, custoFinal, descricao)
            }) { Text("Adicionar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )

    if (mostrarDescricaoCatalogo) {
        AlertDialog(
            onDismissRequest = { mostrarDescricaoCatalogo = false },
            title = { Text(definicao.nome, color = MaterialTheme.colorScheme.primary) },
            text = { Text(descricaoCatalogo.ifBlank { "Sem descrição disponível." }) },
            confirmButton = {
                TextButton(onClick = { mostrarDescricaoCatalogo = false }) { Text("Fechar") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelecionarDesvantagemDialog(viewModel: FichaViewModel, onDismiss: () -> Unit) {
    var busca by remember { mutableStateOf("") }
    var filtroTipo by remember { mutableStateOf<TipoCusto?>(null) }
    var filtroTag by remember { mutableStateOf<String?>(null) }
    var desvantagemSelecionada by remember { mutableStateOf<DesvantagemDefinicao?>(null) }

    val tagsDisponiveis = listOf("combate", "social", "fisica", "mental", "magica")
    val listaFiltrada = viewModel.dataRepository.filtrarDesvantagens(busca, filtroTipo, filtroTag)

    FullscreenDialogContainer(onDismiss = onDismiss) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text("Selecionar Desvantagem", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                Text("Atual: ${viewModel.pontosDesvantagens} pts", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = busca, onValueChange = { busca = it }, label = { Text("Buscar...") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Default.Search, null) })

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)
                ) {
                    FilterChip(
                        selected = filtroTag == null,
                        onClick = { filtroTag = null },
                        label = { Text("Todas") }
                    )
                    tagsDisponiveis.forEach { tag ->
                        FilterChip(
                            selected = filtroTag == tag,
                            onClick = { filtroTag = tag },
                            label = { Text(tag) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("${listaFiltrada.size} desvantagens encontradas", style = MaterialTheme.typography.bodySmall)

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(listaFiltrada) { definicao ->
                        val jaAdicionada = viewModel.desvantagemJaAdicionada(definicao.id)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !jaAdicionada) { desvantagemSelecionada = definicao },
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        definicao.nome,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = if (jaAdicionada) FontWeight.Normal else FontWeight.Medium
                                    )
                                    Text(
                                        "${definicao.custo} pts | ${definicao.tipoCusto.name.lowercase()} | pag. ${definicao.pagina}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (jaAdicionada) {
                                    Text("Adicionada", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Fechar") }
                }
            }
    }

    desvantagemSelecionada?.let { definicao ->
        ConfigurarDesvantagemDialog(definicao = definicao, onDismiss = { desvantagemSelecionada = null },
            onSave = { nivel, custoEscolhido, descricao, autocontrole ->
                viewModel.adicionarDesvantagem(definicao, nivel, custoEscolhido, descricao, autocontrole)
                desvantagemSelecionada = null
            })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurarDesvantagemDialog(definicao: DesvantagemDefinicao, onDismiss: () -> Unit, onSave: (Int, Int, String, Int?) -> Unit) {
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    var nivel by remember { mutableStateOf(1) }
    var custoEscolhido by remember { mutableStateOf(definicao.getCustoBase()) }
    var descricao by remember { mutableStateOf("") }
    var autocontrole by remember { mutableStateOf<Int?>(null) }
    val descricaoCatalogo = definicao.descricao?.trim().orEmpty()
    var mostrarDescricaoCatalogo by remember { mutableStateOf(false) }

    val opcoesEscolha = definicao.getOpcoesEscolha()
    val permiteAutocontrole = definicao.usaAutocontroleMental()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                definicao.nome,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .let {
                        if (descricaoCatalogo.isNotBlank()) {
                            it.clickable { mostrarDescricaoCatalogo = true }
                        } else {
                            it
                        }
                    }
                    .semantics {
                        if (isPraCegoVariant && descricaoCatalogo.isNotBlank()) {
                            contentDescription = "Nome da desvantagem ${definicao.nome}. Toque para abrir descricao."
                        }
                    }
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing), modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Tipo: ${definicao.tipoCusto.name} | Custo base: ${definicao.custo} | Pag. ${definicao.pagina}", style = MaterialTheme.typography.bodySmall)

                when (definicao.tipoCusto) {
                    TipoCusto.POR_NIVEL -> {
                        Text("Nível:")
                        if (isPraCegoVariant) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { if (nivel > 1) nivel-- },
                                    modifier = Modifier.semantics { contentDescription = "Diminuir nível de desvantagem" }
                                ) { Text("-") }
                                Text(
                                    "$nivel",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                TextButton(
                                    onClick = { if (nivel < 10) nivel++ },
                                    modifier = Modifier.semantics { contentDescription = "Aumentar nível de desvantagem" }
                                ) { Text("+") }
                            }
                        } else {
                            Text(
                                "$nivel",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .width(56.dp)
                                    .pointerInput(nivel) {
                                        var dragAcumulado = 0f
                                        val passoPx = 24f
                                        detectVerticalDragGestures(
                                            onVerticalDrag = { change, dragAmount ->
                                                change.consume()
                                                dragAcumulado += dragAmount
                                                while (abs(dragAcumulado) >= passoPx) {
                                                    nivel = if (dragAcumulado < 0f) {
                                                        (nivel + 1).coerceAtMost(10)
                                                    } else {
                                                        (nivel - 1).coerceAtLeast(1)
                                                    }
                                                    dragAcumulado += if (dragAcumulado < 0f) passoPx else -passoPx
                                                }
                                            }
                                        )
                                    },
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                        Text("Custo: ${definicao.getCustoPorNivel() * nivel} pts", fontWeight = FontWeight.Bold)
                    }
                    TipoCusto.ESCOLHA -> {
                        Text("Escolha o custo:")
                        opcoesEscolha.forEach { opcao ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { custoEscolhido = opcao }) {
                                RadioButton(selected = custoEscolhido == opcao, onClick = { custoEscolhido = opcao })
                                Text("$opcao pts")
                            }
                        }
                    }
                    TipoCusto.VARIAVEL -> {
                        Text("Custo:")
                        OutlinedTextField(value = custoEscolhido.toString(), onValueChange = { custoEscolhido = it.toIntOrNull() ?: -10 },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                    TipoCusto.FIXO -> {
                        Text("Custo fixo: ${definicao.getCustoBase()} pts", fontWeight = FontWeight.Bold)
                    }
                }

                if (permiteAutocontrole) {
                    Divider()
                    Text("Autocontrole (opcional):", style = MaterialTheme.typography.labelMedium)
                    Text("GURPS 4Ed pag. 120 - multiplicadores", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(null to "Nenhum", 6 to "6 (x2)", 9 to "9 (x1.5)", 12 to "12 (x1)", 15 to "15 (x0.5)").forEach { (valor, label) ->
                            FilterChip(selected = autocontrole == valor, onClick = { autocontrole = valor }, label = { Text(label, fontSize = 10.sp) })
                        }
                    }
                }

                OutlinedTextField(value = descricao, onValueChange = { descricao = it },
                    label = { Text("Descrição/Especialização") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val custoFinal = when (definicao.tipoCusto) {
                    TipoCusto.POR_NIVEL -> definicao.getCustoPorNivel() * nivel
                    TipoCusto.ESCOLHA, TipoCusto.VARIAVEL -> custoEscolhido
                    TipoCusto.FIXO -> definicao.getCustoBase()
                }
                onSave(nivel, custoFinal, descricao, if (permiteAutocontrole) autocontrole else null)
            }) { Text("Adicionar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )

    if (mostrarDescricaoCatalogo) {
        AlertDialog(
            onDismissRequest = { mostrarDescricaoCatalogo = false },
            title = { Text(definicao.nome, color = MaterialTheme.colorScheme.primary) },
            text = { Text(descricaoCatalogo.ifBlank { "Sem descrição disponível." }) },
            confirmButton = {
                TextButton(onClick = { mostrarDescricaoCatalogo = false }) { Text("Fechar") }
            }
        )
    }
}


@Composable
fun EditarVantagemDialog(
    vantagem: VantagemSelecionada,
    descricaoCatalogo: String = "",
    onDismiss: () -> Unit,
    onSave: (VantagemSelecionada) -> Unit
) {
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    var nivel by remember { mutableStateOf(vantagem.nivel) }
    var custoEscolhido by remember { mutableStateOf(vantagem.custoEscolhido) }
    var descricao by remember { mutableStateOf(vantagem.descricao) }
    var mostrarDescricaoCatalogo by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                vantagem.nome,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .let {
                        if (descricaoCatalogo.isNotBlank()) {
                            it.clickable { mostrarDescricaoCatalogo = true }
                        } else {
                            it
                        }
                    }
                    .semantics {
                        if (isPraCegoVariant && descricaoCatalogo.isNotBlank()) {
                            contentDescription = "Nome da vantagem ${vantagem.nome}. Toque para abrir descricao."
                        }
                    }
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)) {
                if (vantagem.tipoCusto == TipoCusto.POR_NIVEL) {
                    Text("Nível:")
                    val nivelMinimo = 1
                    val nivelMaximo = if (vantagemEhAptidaoMagica(vantagem.definicaoId)) 11 else 10
                    if (isPraCegoVariant) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { if (nivel > nivelMinimo) nivel-- },
                                modifier = Modifier.semantics { contentDescription = "Diminuir nível da vantagem" }
                            ) { Text("-") }
                            Text(
                                "${nivelExibicaoVantagem(vantagem.definicaoId, nivel)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            TextButton(
                                onClick = { if (nivel < nivelMaximo) nivel++ },
                                modifier = Modifier.semantics { contentDescription = "Aumentar nível da vantagem" }
                            ) { Text("+") }
                        }
                    } else {
                        Text(
                            "${nivelExibicaoVantagem(vantagem.definicaoId, nivel)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .width(56.dp)
                                .pointerInput(nivel) {
                                    var dragAcumulado = 0f
                                    val passoPx = 24f
                                    detectVerticalDragGestures(
                                        onVerticalDrag = { change, dragAmount ->
                                            change.consume()
                                            dragAcumulado += dragAmount
                                            while (abs(dragAcumulado) >= passoPx) {
                                                nivel = if (dragAcumulado < 0f) {
                                                    (nivel + 1).coerceAtMost(nivelMaximo)
                                                } else {
                                                    (nivel - 1).coerceAtLeast(nivelMinimo)
                                                }
                                                dragAcumulado += if (dragAcumulado < 0f) passoPx else -passoPx
                                            }
                                        }
                                    )
                                },
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                if (vantagem.tipoCusto == TipoCusto.VARIAVEL || vantagem.tipoCusto == TipoCusto.ESCOLHA) {
                    OutlinedTextField(value = custoEscolhido.toString(), onValueChange = { custoEscolhido = it.toIntOrNull() ?: custoEscolhido },
                        label = { Text("Custo") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                }
                OutlinedTextField(value = descricao, onValueChange = { descricao = it },
                    label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(vantagem.copy(nivel = nivel, custoEscolhido = custoEscolhido, descricao = descricao)) },
                modifier = Modifier.semantics {
                    if (isPraCegoVariant) contentDescription = "Salvar edição da vantagem"
                }
            ) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.semantics {
                    if (isPraCegoVariant) contentDescription = "Cancelar edição da vantagem"
                }
            ) { Text("Cancelar") }
        }
    )

    if (mostrarDescricaoCatalogo) {
        AlertDialog(
            onDismissRequest = { mostrarDescricaoCatalogo = false },
            title = { Text(vantagem.nome, color = MaterialTheme.colorScheme.primary) },
            text = { Text(descricaoCatalogo.ifBlank { "Sem descrição disponível." }) },
            confirmButton = {
                TextButton(
                    onClick = { mostrarDescricaoCatalogo = false },
                    modifier = Modifier.semantics {
                        if (isPraCegoVariant) contentDescription = "Fechar descrição da vantagem"
                    }
                ) { Text("Fechar") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarDesvantagemDialog(
    desvantagem: DesvantagemSelecionada,
    permiteAutocontrole: Boolean = true,
    descricaoCatalogo: String = "",
    onDismiss: () -> Unit,
    onSave: (DesvantagemSelecionada) -> Unit
) {
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    var nivel by remember { mutableStateOf(desvantagem.nivel) }
    var custoEscolhido by remember { mutableStateOf(desvantagem.custoEscolhido) }
    var descricao by remember { mutableStateOf(desvantagem.descricao) }
    var autocontrole by remember { mutableStateOf(if (permiteAutocontrole) desvantagem.autocontrole else null) }
    var mostrarDescricaoCatalogo by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                desvantagem.nome,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .let {
                        if (descricaoCatalogo.isNotBlank()) {
                            it.clickable { mostrarDescricaoCatalogo = true }
                        } else {
                            it
                        }
                    }
                    .semantics {
                        if (isPraCegoVariant && descricaoCatalogo.isNotBlank()) {
                            contentDescription = "Nome da desvantagem ${desvantagem.nome}. Toque para abrir descricao."
                        }
                    }
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing), modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (desvantagem.tipoCusto == TipoCusto.POR_NIVEL) {
                    Text("Nível:")
                    if (isPraCegoVariant) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { if (nivel > 1) nivel-- },
                                modifier = Modifier.semantics { contentDescription = "Diminuir nível da desvantagem" }
                            ) { Text("-") }
                            Text(
                                "$nivel",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            TextButton(
                                onClick = { if (nivel < 10) nivel++ },
                                modifier = Modifier.semantics { contentDescription = "Aumentar nível da desvantagem" }
                            ) { Text("+") }
                        }
                    } else {
                        Text(
                            "$nivel",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .width(56.dp)
                                .pointerInput(nivel) {
                                    var dragAcumulado = 0f
                                    val passoPx = 24f
                                    detectVerticalDragGestures(
                                        onVerticalDrag = { change, dragAmount ->
                                            change.consume()
                                            dragAcumulado += dragAmount
                                            while (abs(dragAcumulado) >= passoPx) {
                                                nivel = if (dragAcumulado < 0f) {
                                                    (nivel + 1).coerceAtMost(10)
                                                } else {
                                                    (nivel - 1).coerceAtLeast(1)
                                                }
                                                dragAcumulado += if (dragAcumulado < 0f) passoPx else -passoPx
                                            }
                                        }
                                    )
                                },
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                if (desvantagem.tipoCusto == TipoCusto.VARIAVEL || desvantagem.tipoCusto == TipoCusto.ESCOLHA) {
                    OutlinedTextField(value = custoEscolhido.toString(), onValueChange = { custoEscolhido = it.toIntOrNull() ?: custoEscolhido },
                        label = { Text("Custo") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                }
                if (permiteAutocontrole) {
                    Text("Autocontrole:")
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(null to "Nenhum", 6 to "6", 9 to "9", 12 to "12", 15 to "15").forEach { (valor, label) ->
                            FilterChip(selected = autocontrole == valor, onClick = { autocontrole = valor }, label = { Text(label) })
                        }
                    }
                }
                OutlinedTextField(value = descricao, onValueChange = { descricao = it },
                    label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        desvantagem.copy(
                            nivel = nivel,
                            custoEscolhido = custoEscolhido,
                            descricao = descricao,
                            autocontrole = if (permiteAutocontrole) autocontrole else null
                        )
                    )
                },
                modifier = Modifier.semantics {
                    if (isPraCegoVariant) contentDescription = "Salvar edição da desvantagem"
                }
            ) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.semantics {
                    if (isPraCegoVariant) contentDescription = "Cancelar edição da desvantagem"
                }
            ) { Text("Cancelar") }
        }
    )

    if (mostrarDescricaoCatalogo) {
        AlertDialog(
            onDismissRequest = { mostrarDescricaoCatalogo = false },
            title = { Text(desvantagem.nome, color = MaterialTheme.colorScheme.primary) },
            text = { Text(descricaoCatalogo.ifBlank { "Sem descrição disponível." }) },
            confirmButton = {
                TextButton(
                    onClick = { mostrarDescricaoCatalogo = false },
                    modifier = Modifier.semantics {
                        if (isPraCegoVariant) contentDescription = "Fechar descrição da desvantagem"
                    }
                ) { Text("Fechar") }
            }
        )
    }
}


@Composable
fun PeculiaridadeDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var texto by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Peculiaridade") },
        text = {
            Column {
                Text("Peculiaridades são mini-desvantagens (-1 pt cada, máx 5)", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = texto, onValueChange = { texto = it }, label = { Text("Peculiaridade") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { if (texto.isNotBlank()) onSave(texto) }) { Text("Adicionar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun QualidadeDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var texto by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Qualidade") },
        text = {
            Column {
                Text("Qualidades são traços positivos (+1 pt cada, máx 5)", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = texto, onValueChange = { texto = it }, label = { Text("Qualidade") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { if (texto.isNotBlank()) onSave(texto) }) { Text("Adicionar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}



