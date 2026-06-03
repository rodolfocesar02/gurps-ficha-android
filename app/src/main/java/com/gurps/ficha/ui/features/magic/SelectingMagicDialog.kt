package com.gurps.ficha.ui.features.magic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gurps.ficha.BuildConfig
import com.gurps.ficha.model.MagiaDefinicao
import com.gurps.ficha.ui.FullscreenDialogContainer
import com.gurps.ficha.ui.UiActionLabels
import com.gurps.ficha.ui.UiTokens
import com.gurps.ficha.ui.appCardColors
import com.gurps.ficha.viewmodel.FichaViewModel
import java.text.Normalizer

private const val AJUDA_VOZ_HABILITADA = false
private const val MAX_OPCOES_MODO_ALVO = 5
private const val ESCOLA_NUNCA_RECOMENDAR = "tecnologica"
private const val MANUAL_MODO_ALVO_CURTO =
    "Ative Modo Alvo, defina o Alvo e adicione só as recomendadas. O app reaproveita seu repertório e mostra o próximo passo até liberar a magia."

private fun escolasNormalizadas(def: MagiaDefinicao): Set<String> {
    return def.escola
        .orEmpty()
        .asSequence()
        .map { valor ->
            Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replace(Regex("\\p{M}+"), "")
                .lowercase()
                .trim()
        }
        .filter { it.isNotBlank() }
        .toSet()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelecionarMagiaDialog(viewModel: FichaViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    val modoAlvoHabilitado = (BuildConfig.MODO_ALVO_NEXUS_HABILITADO == true) || BuildConfig.DEBUG
    var magiaSelecionada by remember { mutableStateOf<MagiaDefinicao?>(null) }
    var erroAdicionarMagia by remember { mutableStateOf<String?>(null) }
    var modoAlvoAtivo by remember { mutableStateOf(false) }
    var magiaAlvoId by remember { mutableStateOf<String?>(null) }
    var mostrarManualModoAlvoDialog by remember { mutableStateOf(false) }
    val modoAlvoAtivoEfetivo = modoAlvoHabilitado && modoAlvoAtivo

    val listaFiltrada = viewModel.magiasFiltradas
    val catalogoMagias = viewModel.dataRepository.magias
    val precisaCatalogoPorId = modoAlvoAtivoEfetivo || magiaAlvoId != null
    val catalogoPorId = remember(catalogoMagias, precisaCatalogoPorId) {
        if (precisaCatalogoPorId) catalogoMagias.associateBy { it.id } else emptyMap()
    }
    val escolas = viewModel.todasEscolasMagia
    val classes = viewModel.todasClassesMagia
    val magiaAlvoSelecionada = magiaAlvoId?.let { catalogoPorId[it] }
    val ordemRelacionadosAlvo: List<String> = if (modoAlvoAtivoEfetivo && magiaAlvoSelecionada != null) {
        viewModel.modoAlvoRelacionadosIds
    } else {
        emptyList()
    }
    val assinaturaModoAlvo = viewModel.assinaturaEstadoMagiasParaModoAlvo()
    val idsJaAdicionadas = remember(assinaturaModoAlvo) {
        viewModel.personagem.magias.map { it.definicaoId }.toSet()
    }
    
    LaunchedEffect(modoAlvoAtivoEfetivo, magiaAlvoId, assinaturaModoAlvo) {
        viewModel.requisitarModoAlvo(magiaAlvoId, modoAlvoAtivoEfetivo)
    }

    val listaExibicao = remember(
        modoAlvoAtivoEfetivo,
        magiaAlvoSelecionada?.id,
        assinaturaModoAlvo,
        ordemRelacionadosAlvo,
        viewModel.modoAlvoProximasAcoesIds,
        listaFiltrada
    ) {
        if (modoAlvoAtivoEfetivo && magiaAlvoSelecionada != null) {
            val relacionadas = (ordemRelacionadosAlvo + magiaAlvoSelecionada.id)
                .asSequence()
                .distinct()
                .filter { it !in idsJaAdicionadas }
                .mapNotNull { catalogoPorId[it] }
                .toList()

            val magiasLivres = listaFiltrada.asSequence()
                .filter { it.id !in idsJaAdicionadas }
                .filter { viewModel.prereqsSatisfied(it) }
                .toList()

            (relacionadas + magiasLivres).distinctBy { it.id }
        } else {
            listaFiltrada.filter { it.id !in idsJaAdicionadas }
        }
    }

    FullscreenDialogContainer(onDismiss = onDismiss) {
        // Título
        Text(
            "Mágicas Disponíveis",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))

        // Busca
        OutlinedTextField(
            value = viewModel.magicSearch.query,
            onValueChange = { viewModel.atualizarBuscaMagia(it) },
            label = { Text("Buscar mágica...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))

        // Filtro escola
        var escolaExpandida by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = escolaExpandida,
            onExpandedChange = { escolaExpandida = !escolaExpandida },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = viewModel.magicSearch.school ?: "Todas Escolas",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = escolaExpandida) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = escolaExpandida, onDismissRequest = { escolaExpandida = false }) {
                DropdownMenuItem(text = { Text("Todas Escolas") }, onClick = { viewModel.atualizarFiltroEscolaMagia(null); escolaExpandida = false })
                escolas.forEach { escola ->
                    DropdownMenuItem(text = { Text(escola) }, onClick = { viewModel.atualizarFiltroEscolaMagia(escola); escolaExpandida = false })
                }
            }
        }

        // Modo Alvo Toggle
        if (modoAlvoHabilitado) {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = modoAlvoAtivo, onCheckedChange = { modoAlvoAtivo = it })
                Spacer(Modifier.width(8.dp))
                Text("Modo Alvo (Nexus)")
            }
            if (modoAlvoAtivo && !magiaAlvoId.isNullOrBlank()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Alvo: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        Text(catalogoPorId[magiaAlvoId]?.nome ?: magiaAlvoId!!, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { magiaAlvoId = null }, contentPadding = PaddingValues(0.dp)) {
                            Text("Limpar", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // Lista
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(listaExibicao) { magia ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { magiaSelecionada = magia },
                    colors = appCardColors()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = UiTokens.CardPaddingHorizontal, vertical = UiTokens.CardPaddingVertical),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(magia.nome, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text(
                                magia.escola?.joinToString(", ") ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val falha = viewModel.prereqFailureForMagia(magia)
                            if (!falha.isNullOrBlank()) {
                                Text("Falta: $falha", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("✓ Requisitos Atendidos", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                            }
                        }
                        if (modoAlvoAtivo) {
                            TextButton(onClick = { magiaAlvoId = magia.id; viewModel.atualizarBuscaMagia("") }) {
                                Text(
                                    if (magiaAlvoId == magia.id) "ALVO" else "OBJETIVO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (magiaAlvoId == magia.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Rodapé
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text(UiActionLabels.FECHAR) }
        }
    }

    magiaSelecionada?.let { def ->
        val prereqFalha = viewModel.prereqFailureForMagia(def)
        AdicionarMagiaDialog(
            definicao = def,
            personagem = viewModel.personagem,
            nivelAptidaoMagica = viewModel.nivelAptidaoMagicaParaMagia(def),
            prereqFalha = prereqFalha,
            onDismiss = { magiaSelecionada = null },
            onSave = { pontos, encantamento, especializacao, forcada ->
                viewModel.adicionarMagia(def, pontos, encantamento, especializacao, forcada)
                magiaSelecionada = null
                if (!forcada && !prereqFalha.isNullOrBlank()) {
                    // Tratar erro se necessário
                }
            }
        )
    }
}
