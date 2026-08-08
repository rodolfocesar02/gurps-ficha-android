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
import com.gurps.ficha.ui.AppSelectionDialog
import com.gurps.ficha.ui.AppSelectionRow
import com.gurps.ficha.ui.AppBotaoDiscreto
import com.gurps.ficha.ui.UiEstilos
import com.gurps.ficha.ui.contadorDe
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
            // Lote 338: a lista do Modo Alvo deve LIDERAR com as PRÓXIMAS AÇÕES do pathfinder
            // (a magia certa a aprender agora). Seguir a 1ª da lista repetidamente leva ao alvo.
            // Antes, a lista misturava "relacionadas" + TODAS as magias livres, afogando a
            // recomendação e fazendo parecer que o caminho não avançava.
            val proximas = viewModel.modoAlvoProximasAcoesIds
                .asSequence()
                .filter { it !in idsJaAdicionadas }
                .mapNotNull { catalogoPorId[it] }
                .toList()

            val relacionadas = (ordemRelacionadosAlvo + magiaAlvoSelecionada.id)
                .asSequence()
                .distinct()
                .filter { it !in idsJaAdicionadas }
                .mapNotNull { catalogoPorId[it] }
                .toList()

            // Lote 338: no Modo Alvo NÃO varremos o catálogo inteiro com prereqsSatisfied
            // (custava ~28s rodando o motor em 879 magias, na main thread). As recomendações
            // que levam ao alvo já vêm do pathfinder (proximas + relacionadas). Se houver
            // BUSCA por texto, aí sim filtramos a lista digitada (poucas magias) por prereq.
            val porBusca = if (viewModel.magicSearch.query.isBlank()) emptyList() else
                listaFiltrada.asSequence()
                    .filter { it.id !in idsJaAdicionadas }
                    .filter { viewModel.prereqsSatisfied(it) }
                    .toList()

            (proximas + relacionadas + porBusca).distinctBy { it.id }
        } else {
            listaFiltrada.filter { it.id !in idsJaAdicionadas }
        }
    }

    // Lote LAYOUT-5: a tela de mágicas entra no padrão. Era a última lista de
    // seleção que ainda montava título, card e rodapé à mão — e a única com o
    // título em `primary` e o nome do item em `bodyLarge`, o que fazia o card
    // parecer maior que o de vantagem ao lado.
    var escolaExpandida by remember { mutableStateOf(false) }

    AppSelectionDialog(
        titulo = "Mágicas Disponíveis",
        busca = viewModel.magicSearch.query,
        onBusca = { viewModel.atualizarBuscaMagia(it) },
        rotuloDaBusca = "Buscar mágica...",
        contador = contadorDe(listaExibicao.size, "mágica", "mágicas"),
        // O seletor de escola e o Modo Alvo não cabem numa fileira de chips —
        // são dezenas de escolas e um interruptor. Vão no cabeçalho livre.
        cabecalhoExtra = {
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
            if (modoAlvoHabilitado) {
                Spacer(Modifier.height(UiTokens.ItemSpacing))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = modoAlvoAtivo, onCheckedChange = { modoAlvoAtivo = it })
                    Spacer(Modifier.width(UiTokens.BotaoEspacamento))
                    Text("Modo Alvo (Nexus)", style = UiEstilos.detalheDoItem)
                }
                if (modoAlvoAtivo && !magiaAlvoId.isNullOrBlank()) {
                    Card(colors = appCardColors()) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = UiTokens.LinhaDeListaPaddingH,
                                vertical = UiTokens.LinhaDeListaPaddingV
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Alvo: ", fontWeight = FontWeight.Bold, style = UiEstilos.detalheDoItem)
                            Text(catalogoPorId[magiaAlvoId]?.nome ?: magiaAlvoId!!, style = UiEstilos.detalheDoItem)
                            Spacer(Modifier.weight(1f))
                            AppBotaoDiscreto(texto = "Limpar", onClick = { magiaAlvoId = null })
                        }
                    }
                }
            }
        },
        onDismiss = onDismiss
    ) {
        items(listaExibicao) { magia ->
            val falha = viewModel.prereqFailureForMagia(magia)
            val atende = falha.isNullOrBlank()
            AppSelectionRow(
                nome = magia.nome,
                detalhe = magia.escola?.joinToString(", ").orEmpty().ifBlank { null },
                onClick = { magiaSelecionada = magia },
                descricaoAcessivel = "${magia.nome}. ${magia.escola?.joinToString(", ").orEmpty()}. " +
                    if (atende) "Requisitos atendidos." else "Falta: $falha",
                extra = {
                    if (atende) {
                        Text(
                            "✓ Requisitos Atendidos",
                            style = UiEstilos.detalheDoItem,
                            // ⚠️ Era `Color(0xFF2E7D32)` cravado no código: verde
                            // escuro sobre fundo escuro no modo noturno. O tema
                            // sabe escolher; o número na mão, não.
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            "Falta: $falha",
                            style = UiEstilos.detalheDoItem,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                acoes = {
                    if (modoAlvoAtivo) {
                        AppBotaoDiscreto(
                            texto = if (magiaAlvoId == magia.id) "ALVO" else "OBJETIVO",
                            onClick = { magiaAlvoId = magia.id; viewModel.atualizarBuscaMagia("") }
                        )
                    }
                }
            )
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
