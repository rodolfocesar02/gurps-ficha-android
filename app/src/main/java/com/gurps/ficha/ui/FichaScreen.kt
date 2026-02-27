package com.gurps.ficha.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gurps.ficha.viewmodel.FichaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FichaScreen(viewModel: FichaViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    var showMenuDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showLoadDialog by remember { mutableStateOf(false) }

    val temAptidaoMagica = viewModel.temAptidaoMagica
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val usarNavegacaoCompacta = configuration.screenWidthDp < 390 || density.fontScale > 1.1f
    val tabs = if (temAptidaoMagica) {
        listOf("Geral", "Traços", "Perícias", "Magia", "Equip.", "Defesas", "Rolagem")
    } else {
        listOf("Geral", "Traços", "Perícias", "Equip.", "Defesas", "Rolagem")
    }
    val rolagemTabIndex = if (temAptidaoMagica) 6 else 5
    val maxTabIndex = tabs.lastIndex

    LaunchedEffect(maxTabIndex) {
        if (selectedTab > maxTabIndex) {
            selectedTab = maxTabIndex
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = viewModel.personagem.nome.ifBlank { "GURPS - Nova Ficha" },
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { showMenuDialog = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = {
                            val icon = when (title) {
                                "Geral" -> Icons.Default.Person
                                "Traços" -> Icons.Default.Star
                                "Perícias" -> Icons.Default.Build
                                "Magia" -> Icons.Default.Star
                                "Equip." -> Icons.Default.ShoppingCart
                                "Defesas" -> Icons.Default.Favorite
                                "Rolagem" -> Icons.Default.Refresh
                                else -> Icons.Default.Build
                            }
                            Icon(
                                icon,
                                contentDescription = title
                            )
                        },
                        label = if (usarNavegacaoCompacta) null else {
                            {
                                Text(
                                    title,
                                    fontSize = 8.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        alwaysShowLabel = false
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (selectedTab != rolagemTabIndex) {
                PontosBar(viewModel)
            }
            when (selectedTab) {
                0 -> TabGeral(viewModel)
                1 -> TabTracos(viewModel)
                2 -> TabPericias(viewModel)
                3 -> if (temAptidaoMagica) TabMagias(viewModel) else TabEquipamentos(viewModel)
                4 -> if (temAptidaoMagica) TabEquipamentos(viewModel) else TabCombate(viewModel)
                5 -> if (temAptidaoMagica) TabCombate(viewModel) else TabRolagem(viewModel)
                6 -> TabRolagem(viewModel)
            }
        }
    }

    if (showMenuDialog) {
        MenuDialog(
            onDismiss = { showMenuDialog = false },
            onNovaFicha = { viewModel.novaFicha(); showMenuDialog = false },
            onSalvar = { showMenuDialog = false; showSaveDialog = true },
            onCarregar = { showMenuDialog = false; showLoadDialog = true }
        )
    }

    if (showSaveDialog) {
        SalvarDialog(
            nomeAtual = viewModel.personagem.nome,
            onDismiss = { showSaveDialog = false },
            onSalvar = { nome -> viewModel.salvarFicha(nome); showSaveDialog = false }
        )
    }

    if (showLoadDialog) {
        CarregarDialog(
            fichas = viewModel.fichasSalvas,
            onDismiss = { showLoadDialog = false },
            onCarregar = { nome -> viewModel.carregarFicha(nome); showLoadDialog = false },
            onExcluir = { nome -> viewModel.excluirFicha(nome) }
        )
    }

    if (viewModel.mostrarConfirmacaoLimpezaMagias) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelarLimpezaMagiasAoPerderAptidao() },
            title = { Text("Remover magias") },
            text = { Text("Ao perder Aptidão Mágica, as magias serão removidas. Deseja continuar?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmarLimpezaMagiasAoPerderAptidao() }) {
                    Text("Continuar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelarLimpezaMagiasAoPerderAptidao() }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun PontosBar(viewModel: FichaViewModel) {
    val p = viewModel.personagem
    val restantes = p.pontosRestantes
    val corRestantes = when {
        restantes < 0 -> MaterialTheme.colorScheme.error
        restantes == 0 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Pontos Iniciais: ${p.pontosIniciais}", style = MaterialTheme.typography.bodySmall)
                    Text("Gastos: ${p.pontosGastos}", style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    text = "Restantes: $restantes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = corRestantes
                )
            }
        }
    }
}


