package com.gurps.ficha.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import com.gurps.ficha.model.ArmaduraCatalogoItem
import com.gurps.ficha.model.ArmaCatalogoItem
import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.model.EscudoCatalogoItem
import com.gurps.ficha.model.TipoEquipamento
import com.gurps.ficha.viewmodel.FichaViewModel
import java.text.Normalizer

private fun limparRuidoGrupoArma(texto: String): String {
    if (texto.isBlank()) return texto
    return texto
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .filterNot { linha ->
            linha.contains("Tabela de Armas", ignoreCase = true) ||
                Regex("\\((DX|IQ|HT|ST)-\\d+.*\\)", RegexOption.IGNORE_CASE).containsMatchIn(linha)
        }
        .joinToString("\n")
}

@Composable
private fun BotaoAdicionarPadrao(texto: String, onClick: () -> Unit) {
    PrimaryActionButton(text = texto, onClick = onClick)
}

@Composable
fun TabEquipamentos(viewModel: FichaViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var showArmaDialog by remember { mutableStateOf(false) }
    var showEscudoDialog by remember { mutableStateOf(false) }
    var showArmaduraDialog by remember { mutableStateOf(false) }
    var armaduraPendenteConfiguracao by remember { mutableStateOf<ArmaduraCatalogoItem?>(null) }
    var editingEquipamento by remember { mutableStateOf<Pair<Int, Equipamento>?>(null) }
    // Lote ARMA-3: a arma cuja ficha técnica está aberta (vinda do catálogo).
    var armaDetalhada by remember { mutableStateOf<ArmaCatalogoItem?>(null) }
    // Lote ARMA-4: a mesma ficha, aberta a partir do inventário.
    var armaDoInventarioDetalhada by remember { mutableStateOf<Equipamento?>(null) }

    val p = viewModel.personagem
    val errosCarga = viewModel.errosCargaCatalogos
    val equipamentosComIndice = p.equipamentos.withIndex().toList()
    val equipamentosManuais = equipamentosComIndice.filter { it.value.tipo == TipoEquipamento.GERAL }
    val armasEquipadas = equipamentosComIndice.filter { it.value.tipo == TipoEquipamento.ARMA }
    val escudosEquipados = equipamentosComIndice.filter { it.value.tipo == TipoEquipamento.ESCUDO }
    val armadurasEquipadas = equipamentosComIndice.filter { it.value.tipo == TipoEquipamento.ARMADURA }

    StandardTabColumn {
        if (errosCarga.isNotEmpty()) {
            SectionCard(title = "Aviso de Catálogo") {
                Text(
                    "Alguns catálogos não foram carregados corretamente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                errosCarga.forEach { (catalogo, mensagem) ->
                    Text(
                        "- $catalogo: $mensagem",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        BotaoAdicionarPadrao(texto = "Adicionar Itens", onClick = { showDialog = true })

        if (equipamentosManuais.isNotEmpty()) {
            Text("Equipamentos Manuais", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            equipamentosManuais.forEach { entry ->
                AppListItemCard {
                    EquipamentoItem(
                        equipamento = entry.value,
                        onEdit = { editingEquipamento = entry.index to entry.value },
                        onDelete = { viewModel.removerEquipamento(entry.index) },
                        viewModel = viewModel
                    )
                }
            }
        }

        BotaoAdicionarPadrao(texto = "Adicionar Arma", onClick = { showArmaDialog = true })

        if (armasEquipadas.isNotEmpty()) {
            Text("Armas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Itens equipados: ${armasEquipadas.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            armasEquipadas.forEach { entry ->
                AppListItemCard {
                    EquipamentoArmaItem(
                        equipamento = entry.value,
                        onEdit = { editingEquipamento = entry.index to entry.value },
                        onDelete = { viewModel.removerEquipamento(entry.index) },
                        onDetalhes = { armaDoInventarioDetalhada = entry.value },
                        viewModel = viewModel
                    )
                }
            }
        }

        BotaoAdicionarPadrao(texto = "Adicionar Escudo", onClick = { showEscudoDialog = true })

        if (escudosEquipados.isNotEmpty()) {
            Text("Escudos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Itens equipados: ${escudosEquipados.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            escudosEquipados.forEach { entry ->
                AppListItemCard {
                    EquipamentoItem(
                        equipamento = entry.value,
                        onEdit = { editingEquipamento = entry.index to entry.value },
                        onDelete = { viewModel.removerEquipamento(entry.index) },
                        viewModel = viewModel
                    )
                }
            }
        }

        BotaoAdicionarPadrao(texto = "Adicionar Armadura", onClick = { showArmaduraDialog = true })

        if (armadurasEquipadas.isNotEmpty()) {
            Text("Armaduras", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Itens selecionados: ${armadurasEquipadas.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Selecao por NT e Local (regra do livro).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            armadurasEquipadas.forEach { entry ->
                AppListItemCard {
                    ArmaduraSelecionadaItem(
                        equipamento = entry.value,
                        onEdit = { editingEquipamento = entry.index to entry.value },
                        onDelete = { viewModel.removerEquipamento(entry.index) }
                    )
                }
            }
        }

        ResumoEquipamentosFooter(viewModel = viewModel)
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showDialog) {
        EquipamentoDialog(
            onDismiss = { showDialog = false },
            onSave = {
                viewModel.adicionarEquipamento(it)
                showDialog = false
            }
        )
    }

    editingEquipamento?.let { (index, equipamento) ->
        EquipamentoDialog(
            initialEquipamento = equipamento,
            onDismiss = { editingEquipamento = null },
            onSave = {
                viewModel.atualizarEquipamento(index, it)
                editingEquipamento = null
            }
        )
    }

    if (showArmaDialog) {
        SelecionarArmaEquipamentoDialog(
            viewModel = viewModel,
            onDismiss = { showArmaDialog = false },
            // ⚠️ Lote ARMA-3: o toque na lista NÃO adiciona mais direto. Ele
            // abre a ficha técnica, e o botão de adicionar mora lá dentro.
            // Dois toques em vez de um, para ninguém comprar uma arma sem saber
            // que ela pesa 7,3 kg e exige as duas mãos.
            onSelect = { armaDetalhada = it }
        )
    }

    // Lote ARMA-3/4: o mesmo card serve os dois lados. Na seleção ele adiciona;
    // no inventário ele é só leitura, porque a arma já está na ficha.
    armaDetalhada?.let { arma ->
        com.gurps.ficha.ui.features.equipamento.CardDetalheArma(
            ficha = viewModel.fichaTecnicaDaArma(arma),
            rotuloAcao = "Adicionar ao inventário",
            onAcao = {
                viewModel.adicionarEquipamentoArma(arma)
                armaDetalhada = null
                showArmaDialog = false
            },
            onDismiss = { armaDetalhada = null }
        )
    }

    armaDoInventarioDetalhada?.let { equipamento ->
        val doCatalogo = viewModel.armaDoCatalogoPara(equipamento)
        if (doCatalogo != null) {
            com.gurps.ficha.ui.features.equipamento.CardDetalheArma(
                ficha = viewModel.fichaTecnicaDaArma(doCatalogo),
                rotuloAcao = null,
                onDismiss = { armaDoInventarioDetalhada = null }
            )
        } else {
            // Arma que não casa com o catálogo (criada à mão, ou de uma versão
            // anterior). Dizer isso é melhor que abrir um card vazio.
            com.gurps.ficha.ui.features.equipamento.CardArmaForaDoCatalogo(
                equipamento = equipamento,
                onDismiss = { armaDoInventarioDetalhada = null }
            )
        }
    }

    if (showEscudoDialog) {
        SelecionarEscudoEquipamentoDialog(
            viewModel = viewModel,
            onDismiss = { showEscudoDialog = false },
            onSelect = {
                viewModel.adicionarEquipamentoEscudo(it)
                showEscudoDialog = false
            }
        )
    }

    if (showArmaduraDialog) {
        SelecionarArmaduraEquipamentoDialog(
            viewModel = viewModel,
            onDismiss = { showArmaduraDialog = false },
            onSelect = {
                armaduraPendenteConfiguracao = it
                showArmaduraDialog = false
            }
        )
    }

    armaduraPendenteConfiguracao?.let { armadura ->
        ConfigurarArmaduraDialog(
            armadura = armadura,
            onDismiss = { armaduraPendenteConfiguracao = null },
            onConfirm = { locais ->
                viewModel.adicionarEquipamentoArmaduraComSelecao(armadura, locais)
                armaduraPendenteConfiguracao = null
            }
        )
    }
}

private fun corrigirTextoQuebrado(texto: String): String {
    if (texto.isBlank()) return texto
    val reparado = texto
        .replace("cr?nio", "cranio", ignoreCase = true)
        .replace("cr�nio", "cranio", ignoreCase = true)
        .replace("crânio", "cranio", ignoreCase = true)
        .replace("pesco?o", "pescoco", ignoreCase = true)
        .replace("pesco�o", "pescoco", ignoreCase = true)
        .replace("bra?os", "bracos", ignoreCase = true)
        .replace("bra�os", "bracos", ignoreCase = true)
        .replace("m?os", "maos", ignoreCase = true)
        .replace("m�os", "maos", ignoreCase = true)
        .replace("p?s", "pes", ignoreCase = true)
        .replace("p�s", "pes", ignoreCase = true)
    return Normalizer.normalize(reparado, Normalizer.Form.NFC)
}

@Composable
private fun ArmaduraSelecionadaItem(
    equipamento: Equipamento,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val rd = equipamento.rdArmaduraExibicao().orEmpty()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(corrigirTextoQuebrado(equipamento.nome), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            // Saga: item tirado pela narrativa (Narrador). Continua na ficha, mas não conta no combate.
            if (equipamento.confiscado) Text(
                "⛓️ confiscado na história — não dá RD no combate",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error
            )
            Text(
                if (rd.isNotBlank()) "RD: $rd" else "RD: -",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            if (equipamento.notas.isNotBlank()) {
                Text(
                    corrigirTextoQuebrado(equipamento.notas),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar armadura ${equipamento.nome}") }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Remover armadura ${equipamento.nome}") }
    }
}

@Composable
private fun ResumoEquipamentosFooter(viewModel: FichaViewModel) {
    val p = viewModel.personagem
    SummaryFooterCard(title = "Resumo de Equipamentos") {
        Text("ST atual: ${p.forca}", style = MaterialTheme.typography.labelSmall)
        Text("Peso total: ${viewModel.pesoTotal} kg", style = MaterialTheme.typography.labelSmall)
        Text(
            "Custo total: $${viewModel.custoTotalEquipamentos}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun EquipamentoArmaItem(
    equipamento: Equipamento,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    viewModel: FichaViewModel,
    // Lote ARMA-4: abre a ficha técnica completa desta arma.
    onDetalhes: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onDetalhes)
                .semantics {
                    contentDescription = "${equipamento.nome}. Toque para ver a ficha técnica completa."
                }
        ) {
            Text(equipamento.nome, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            // Saga: arma tirada pela narrativa (Narrador). Continua na ficha, mas não aparece no combate.
            if (equipamento.confiscado) Text(
                "⛓️ confiscado na história — fora do combate",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error
            )
            val danoRaw = equipamento.armaDanoRaw
            if (!danoRaw.isNullOrBlank()) {
                val danoCalc = viewModel.calcularDanoArmaComSt(danoRaw)
                Text(
                    "Dano: $danoCalc",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            } else {
                Text(
                    "Dano: -",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (equipamento.notas.isNotBlank()) {
                val notasLimpas = if (equipamento.armaCatalogoId != null) {
                    limparRuidoGrupoArma(equipamento.notas)
                } else {
                    equipamento.notas
                }
                if (notasLimpas.isNotBlank()) {
                Text(
                    notasLimpas,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                }
            }
            val observacoesCatalogo = viewModel.observacoesArmaPorEquipamento(equipamento).trim()
            val observacoesFaltantes = if (observacoesCatalogo.isBlank()) {
                emptyList()
            } else {
                observacoesCatalogo
                    .lineSequence()
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .filterNot { linha -> equipamento.notas.contains(linha) }
                    .toList()
            }
            if (observacoesFaltantes.isNotEmpty()) {
                Text(
                    observacoesFaltantes.joinToString("\n"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
        IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar arma ${equipamento.nome}") }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Remover arma ${equipamento.nome}") }
    }
}

@Composable
fun EquipamentoItem(equipamento: Equipamento, onEdit: () -> Unit, onDelete: () -> Unit, viewModel: FichaViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(equipamento.nome, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                "${equipamento.quantidade}x | ${equipamento.peso}kg cada | Total: ${equipamento.peso * equipamento.quantidade}kg",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (equipamento.custo > 0) {
                Text(
                    "Custo: $${equipamento.custo * equipamento.quantidade}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (equipamento.notas.isNotBlank()) {
                Text(
                    equipamento.notas,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val danoRaw = equipamento.armaDanoRaw
            if (!danoRaw.isNullOrBlank()) {
                val danoCalc = viewModel.calcularDanoArmaComSt(danoRaw)
                Text(
                    "Dano: $danoRaw -> $danoCalc",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
        IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar equipamento ${equipamento.nome}") }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Remover equipamento ${equipamento.nome}") }
    }
}

@Composable
fun SelecionarArmaEquipamentoDialog(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit,
    onSelect: (ArmaCatalogoItem) -> Unit
) {
    val stAtual = viewModel.personagem.forca
    val armas = viewModel.armasEquipamentosFiltradas
    val mostrarObsArmaFogo = viewModel.equipmentSearch.type == "armas_de_fogo"

    FullscreenDialogContainer(onDismiss = onDismiss) {
        Text("Selecionar Arma", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("ST do personagem: $stAtual", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = viewModel.equipmentSearch.query,
            onValueChange = { viewModel.atualizarBuscaArmaEquipamento(it) },
            label = { Text("Buscar por nome") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            TipoArmaFiltroChip("Todas", viewModel.equipmentSearch.type == null) { viewModel.atualizarFiltroTipoArmaEquipamento(null) }
            TipoArmaFiltroChip("Corpo a corpo", viewModel.equipmentSearch.type == "corpo_a_corpo") { viewModel.atualizarFiltroTipoArmaEquipamento("corpo_a_corpo") }
            TipoArmaFiltroChip("Distancia", viewModel.equipmentSearch.type == "distancia") { viewModel.atualizarFiltroTipoArmaEquipamento("distancia") }
            TipoArmaFiltroChip("Armas de Fogo", viewModel.equipmentSearch.type == "armas_de_fogo") { viewModel.atualizarFiltroTipoArmaEquipamento("armas_de_fogo") }
        }
        if (viewModel.equipmentSearch.type == "armas_de_fogo") {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                TipoArmaFiltroChip("Todas Fogo", viewModel.equipmentSearch.fireArmCategory == null) { viewModel.atualizarFiltroCategoriaArmaFogoEquipamento(null) }
                TipoArmaFiltroChip("Pistolas e MM", viewModel.equipmentSearch.fireArmCategory == "pistolas_mm") { viewModel.atualizarFiltroCategoriaArmaFogoEquipamento("pistolas_mm") }
                TipoArmaFiltroChip("Rifles e Espingardas", viewModel.equipmentSearch.fireArmCategory == "rifles_espingardas") { viewModel.atualizarFiltroCategoriaArmaFogoEquipamento("rifles_espingardas") }
                TipoArmaFiltroChip("Ultra-Tech", viewModel.equipmentSearch.fireArmCategory == "ultratech") { viewModel.atualizarFiltroCategoriaArmaFogoEquipamento("ultratech") }
                TipoArmaFiltroChip("Armas Pesadas", viewModel.equipmentSearch.fireArmCategory == "pesadas") { viewModel.atualizarFiltroCategoriaArmaFogoEquipamento("pesadas") }
            }
        }
        Spacer(Modifier.height(4.dp))
        if (armas.isEmpty()) {
            Text("Nenhuma arma disponivel para o ST atual e filtros aplicados.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(armas, key = { it.id }) { arma ->
                    ArmaItemSelecao(arma = arma, danoCalculado = viewModel.calcularDanoArmaComSt(arma.danoRaw), mostrarObsArmaFogo = mostrarObsArmaFogo, onClick = { onSelect(arma) })
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text("Fechar") }
        }
    }
}

@Composable
private fun TipoArmaFiltroChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall
    )
}

private fun observacoesFormatadas(armadura: ArmaduraCatalogoItem): List<String> {
    val refs = Regex("\\[(\\d+)]")
        .findAll(armadura.observacoes)
        .mapNotNull { it.groupValues.getOrNull(1)?.toIntOrNull() }
        .toList()
    val detalhesOriginais = armadura.observacoesDetalhadas
        .map { it.trim() }
        .filter { it.isNotBlank() }
    if (detalhesOriginais.isEmpty()) return emptyList()

    val saida = mutableListOf<String>()
    var detalhes = detalhesOriginais

    // Para NT alto, a observacao global (NT7+) deve aparecer sem numero.
    val primeira = detalhes.firstOrNull()
    if (primeira != null && primeira.contains("NT7+", ignoreCase = true)) {
        saida.add(primeira)
        detalhes = detalhes.drop(1)
    }

    if (refs.isEmpty() || detalhes.isEmpty()) return saida

    val pareadas = refs.zip(detalhes).map { (ref, texto) -> "[$ref] $texto" }
    saida.addAll(pareadas)
    if (detalhes.size > refs.size) {
        detalhes.drop(refs.size).forEach { extra -> saida.add(extra) }
    }
    return saida
}

@Composable
private fun ArmaItemSelecao(
    arma: ArmaCatalogoItem,
    danoCalculado: String,
    mostrarObsArmaFogo: Boolean,
    onClick: () -> Unit
) {
    val tipoLabel = when (arma.tipoCombate) {
        "corpo_a_corpo" -> "Corpo a corpo"
        "armas_de_fogo" -> "Armas de Fogo"
        else -> "Distancia"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(arma.nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Text(
                "ST ${arma.stMinimo ?: "-"} | $tipoLabel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Dano: ${arma.danoRaw} -> $danoCalculado | Custo: $${arma.custoBase ?: 0f} | Peso: ${arma.pesoBaseKg ?: 0f} kg",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary
            )
            if (!arma.aparar.isNullOrBlank()) {
                Text(
                    "Aparar: ${arma.aparar}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            val podeMostrarObs = when (arma.tipoCombate) {
                "armas_de_fogo" -> mostrarObsArmaFogo
                "corpo_a_corpo", "distancia" -> true
                else -> false
            }
            if (podeMostrarObs && arma.observacoes.isNotBlank()) {
                Text(
                    "Obs: ${arma.observacoes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SelecionarEscudoEquipamentoDialog(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit,
    onSelect: (EscudoCatalogoItem) -> Unit
) {
    val escudos = viewModel.escudosEquipamentosFiltrados
    val stAtual = viewModel.personagem.forca
    FullscreenDialogContainer(onDismiss = onDismiss) {
        Text("Selecionar Escudo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("ST do personagem: $stAtual", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = viewModel.equipmentSearch.query,
            onValueChange = { viewModel.atualizarBuscaEscudoEquipamento(it) },
            label = { Text("Buscar escudo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        if (escudos.isEmpty()) {
            Text("Nenhum escudo disponivel para o ST atual.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(escudos, key = { it.id }) { escudo ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { onSelect(escudo) }, elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(escudo.nome, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text("DB ${escudo.db} | Custo: $${escudo.custo ?: 0f} | Peso: ${escudo.pesoKg ?: 0f} kg", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text("Fechar") }
        }
    }
}

private val LOCAIS_ARMADURA = listOf(
    "cabeca" to "Cabeca",
    "corpo" to "Corpo",
    "pescoco" to "Pescoco",
    "tronco" to "Tronco",
    "virilha" to "Virilha",
    "membros" to "Membros",
    "bracos" to "Bracos",
    "pernas" to "Pernas",
    "pes" to "Pes",
    "maos" to "Maos",
    "traje_completo" to "Traje Completo"
)

@Composable
private fun SelecionarArmaduraEquipamentoDialog(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit,
    onSelect: (ArmaduraCatalogoItem) -> Unit
) {
    val armaduras = viewModel.armadurasEquipamentosFiltradas
    val filtrosAtivos = viewModel.equipmentSearch.query.isNotBlank() ||
        viewModel.equipmentSearch.armorerLocation != null ||
        viewModel.equipmentSearch.armorerNt != null
    FullscreenDialogContainer(onDismiss = onDismiss) {
        Text("Selecionar Armadura", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Use Local e NT para refinar mais rapido.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = viewModel.equipmentSearch.query,
            onValueChange = { viewModel.atualizarBuscaArmaduraEquipamento(it) },
            label = { Text("Buscar armadura") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            TipoArmaFiltroChip("Local: Todos", viewModel.equipmentSearch.armorerLocation == null) { viewModel.atualizarFiltroLocalArmaduraEquipamento(null) }
            LOCAIS_ARMADURA.forEach { (id, label) ->
                TipoArmaFiltroChip(label, viewModel.equipmentSearch.armorerLocation == id) { viewModel.atualizarFiltroLocalArmaduraEquipamento(id) }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            TipoArmaFiltroChip("NT: Todas", viewModel.equipmentSearch.armorerNt == null) { viewModel.atualizarFiltroNtArmaduraEquipamento(null) }
            for (nt in 0..10) {
                TipoArmaFiltroChip("NT $nt", viewModel.equipmentSearch.armorerNt == nt) { viewModel.atualizarFiltroNtArmaduraEquipamento(nt) }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Resultados: ${armaduras.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (filtrosAtivos) {
                TextButton(onClick = { viewModel.limparFiltrosArmaduraEquipamento() }) { Text("Limpar filtros") }
            }
        }
        if (armaduras.isEmpty()) {
            Text("Nenhuma armadura encontrada para o filtro aplicado.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(armaduras, key = { it.id }) { armadura ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { onSelect(armadura) }, elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(corrigirTextoQuebrado(armadura.nome), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text("NT ${armadura.nt ?: "-"} | RD ${armadura.rd} | Peso ${armadura.pesoBaseKg ?: 0f} kg | Custo $${armadura.custoBase ?: 0f}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            Text("Local: ${corrigirTextoQuebrado(armadura.local)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val observacoes = observacoesFormatadas(armadura)
                            if (observacoes.isNotEmpty()) {
                                observacoes.forEach { linha -> Text(linha, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary) }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text("Fechar") }
        }
    }
}

@Composable
private fun ConfigurarArmaduraDialog(
    armadura: ArmaduraCatalogoItem,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val locais = remember(armadura.id, armadura.local, armadura.componentes) {
        val locaisBase = armadura.local
            .split(Regex("[,;/|]"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
        val viaComponentes = armadura.componentes.flatMap { c ->
            c.local
                .split(Regex("[,;/|]"))
                .map { it.trim() }
                .filter { it.isNotBlank() }
        }
        (locaisBase + viaComponentes).distinct().ifEmpty { listOf("corpo") }
    }
    val conjuntoObrigatorio = remember(armadura.id, armadura.nome, armadura.componentes) {
        armadura.componentes.isNotEmpty() && armadura.nome.contains("+")
    }

    var selecionados by remember(armadura.id, conjuntoObrigatorio) { mutableStateOf(locais.toSet()) }

    val divisor = selecionados.size.coerceAtLeast(1).toFloat()
    val custoPrevisto = armadura.custoBase ?: 0f
    val pesoPrevisto = armadura.pesoBaseKg ?: 0f

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar Armadura") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(armadura.nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    if (conjuntoObrigatorio)
                        "Este conjunto adiciona todas as partes automaticamente."
                    else
                        "Escolha os locais para adicionar no inventario.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    locais.forEach { local ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !conjuntoObrigatorio) {
                                    selecionados = if (selecionados.contains(local)) selecionados - local else selecionados + local
                                }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selecionados.contains(local),
                                enabled = !conjuntoObrigatorio,
                                onCheckedChange = { checked ->
                                    selecionados = if (checked) selecionados + local else selecionados - local
                                }
                            )
                            Text(local, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                HorizontalDivider()
                Text(
                    "Previsto total -> Custo: $${custoPrevisto} | Peso: ${pesoPrevisto} kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Por local selecionado: Custo ${String.format("%.2f", custoPrevisto / divisor)} | Peso ${String.format("%.2f", pesoPrevisto / divisor)} kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selecionados.toList()) },
                enabled = selecionados.isNotEmpty()
            ) { Text("Adicionar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
