package com.gurps.ficha.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.model.PericiaSelecionada
import com.gurps.ficha.model.PericiaSuplementarItem
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.TecnicaCatalogoItem
import com.gurps.ficha.model.TecnicaSelecionada
import com.gurps.ficha.viewmodel.FichaViewModel

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
            tecnica.nome.contains(busca, ignoreCase = true) ||
            tecnica.descricao.contains(busca, ignoreCase = true)
        val matchFonte = filtroFonte.isNullOrBlank() || tecnica.sourceBook.equals(filtroFonte, ignoreCase = true)
        matchBusca && matchFonte
    }

    FullscreenDialogContainer(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Selecionar Técnica", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = busca,
                onValueChange = { busca = it },
                label = { Text("Buscar técnica...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, null) }
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = filtroFonte == null,
                    onClick = { filtroFonte = null },
                    label = { Text("Todas") }
                )
                fontes.forEach { fonte ->
                    FilterChip(
                        selected = filtroFonte == fonte,
                        onClick = { filtroFonte = fonte },
                        label = { Text(fonte) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("${tecnicas.size} técnicas encontradas", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(tecnicas) { tecnica ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tecnicaSelecionada = tecnica },
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(tecnica.nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                            Text(
                                "${tecnica.sourceBook} • ${tecnica.dificuldadeRaw}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Fechar") }
            }
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
    val pericias = viewModel.personagem.pericias
    var periciaSelecionadaId by remember { mutableStateOf<String?>(null) }
    var nivelRelativo by remember { mutableStateOf(0) }
    var erro by remember { mutableStateOf<String?>(null) }
    val preRequisitoExibicao = viewModel.preRequisitoExibicaoTecnica(definicao)

    val periciaBase = pericias.firstOrNull { pericia ->
        periciaTecnicaKey(pericia) == periciaSelecionadaId
    }
    val atendePreReq = periciaBase?.let { viewModel.tecnicaAtendePreRequisito(definicao, it) } ?: false
    val limiteMaximo = viewModel.limiteMaximoTecnica(definicao)
    val nivelMaximo = limiteMaximo ?: 12
    if (nivelRelativo > nivelMaximo) nivelRelativo = nivelMaximo

    val predefModificador = viewModel.dataRepository.extrairModificadorPredefinido(definicao.preDefinidoRaw)
    val custo = viewModel.custoTecnica(definicao, nivelRelativo)
    val nhTecnica = periciaBase?.let {
        viewModel.calcularNivelTecnicaPreview(definicao, it, nivelRelativo)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar Técnica") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(definicao.nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "${definicao.sourceBook} • ${definicao.dificuldadeRaw}",
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
                } else {
                    Text("Perícia base:", style = MaterialTheme.typography.labelMedium)
                    pericias.forEach { pericia ->
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

                Text("Nível acima do predefinido: +$nivelRelativo", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        enabled = nivelRelativo > 0,
                        onClick = { nivelRelativo = (nivelRelativo - 1).coerceAtLeast(0) }
                    ) { Text("-") }
                    TextButton(
                        enabled = nivelRelativo < nivelMaximo,
                        onClick = { nivelRelativo = (nivelRelativo + 1).coerceAtMost(nivelMaximo) }
                    ) { Text("+") }
                }
                if (limiteMaximo != null) {
                    Text("Limite máximo: predefinido +$limiteMaximo", style = MaterialTheme.typography.bodySmall)
                }

                Text("Custo automático: $custo ponto(s)", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Pré-definido base: ${if (predefModificador >= 0) "+$predefModificador" else predefModificador}",
                    style = MaterialTheme.typography.bodySmall
                )
                nhTecnica?.let {
                    Text("NH da Técnica: $it", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
            ) { Text("Adicionar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarTecnicaDialog(
    tecnica: TecnicaSelecionada,
    personagem: Personagem,
    onDismiss: () -> Unit,
    onSave: (TecnicaSelecionada) -> Unit
) {
    var nivelRelativo by remember { mutableStateOf(tecnica.nivelRelativoPredefinido.coerceAtLeast(0)) }
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
    val periciaBase = pericias.firstOrNull { periciaTecnicaKey(it) == periciaSelecionadaId }
    val predefModificador = tecnica.preDefinidoModificador
    val limiteMaximo = tecnica.limiteMaximoRelativo
    val nivelMaximo = limiteMaximo ?: 12
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
        title = { Text("Editar Técnica") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(tecnica.nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${tecnica.sourceBook} • ${tecnica.dificuldadeRaw}", style = MaterialTheme.typography.bodySmall)

                Text("Perícia base:", style = MaterialTheme.typography.labelMedium)
                pericias.forEach { pericia ->
                    val key = periciaTecnicaKey(pericia)
                    FilterChip(
                        selected = periciaSelecionadaId == key,
                        onClick = { periciaSelecionadaId = key },
                        label = { Text(periciaTecnicaLabel(pericia)) }
                    )
                }

                Text("Nível acima do predefinido: +$nivelRelativo", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        enabled = nivelRelativo > 0,
                        onClick = { nivelRelativo = (nivelRelativo - 1).coerceAtLeast(0) }
                    ) { Text("-") }
                    TextButton(
                        enabled = nivelRelativo < nivelMaximo,
                        onClick = { nivelRelativo = (nivelRelativo + 1).coerceAtMost(nivelMaximo) }
                    ) { Text("+") }
                }
                if (limiteMaximo != null) {
                    Text("Limite máximo: predefinido +$limiteMaximo", style = MaterialTheme.typography.bodySmall)
                }

                Text("Custo automático: $custo ponto(s)", style = MaterialTheme.typography.bodyMedium)
                nhTecnica?.let {
                    Text("NH da Técnica: $it", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
            ) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun PericiasSuplementaresDialog(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit
) {
    var busca by remember { mutableStateOf("") }
    val itens = viewModel.periciasSuplementaresArtesMarciais.filter { pericia ->
        busca.isBlank() ||
            pericia.nome.contains(busca, ignoreCase = true) ||
            pericia.descricao.contains(busca, ignoreCase = true)
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
            Text("${itens.size} perícias encontradas", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(itens) { pericia ->
                    PericiaSuplementarCard(item = pericia)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Fechar") }
            }
        }
    }
}

@Composable
private fun PericiaSuplementarCard(item: PericiaSuplementarItem) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Text(
                "${item.sourceBook} • ${item.dificuldadeRaw}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (item.preRequisitoRaw.isNotBlank()) {
                Text("Pré-requisito: ${item.preRequisitoRaw}", style = MaterialTheme.typography.bodySmall)
            }
            if (item.descricao.isNotBlank()) {
                Text(item.descricao, style = MaterialTheme.typography.bodySmall)
            }
        }
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
