package com.gurps.ficha.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

@Composable
private fun SelecaoDeCampanha(viewModel: FichaViewModel) {
    var nome by remember { mutableStateOf("") }
    var config by remember { mutableStateOf(CampanhaConfig()) }
    var mostrarConfig by remember { mutableStateOf(false) }
    var idParaExcluir by remember { mutableStateOf<Long?>(null) }
    // Lote HEX-9b: preview STANDALONE do combate 3D — sem precisar campanha ativa nem combate iniciado.
    var mostrarPreview3D by remember { mutableStateOf(false) }

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

        Spacer(Modifier.height(12.dp))
        // Tela limpa: as definições do jogo ficam atrás de um botão.
        OutlinedButton(
            onClick = { mostrarConfig = true },
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Abrir Configuração do Jogo" }
        ) {
            Icon(Icons.Default.Tune, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Configuração do Jogo")
        }
        Text(
            resumoConfig(config),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
        )

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { viewModel.sagaCriarCampanha(nome, config) },
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Criar campanha e começar a aventura" }
        ) { Text("Criar campanha") }

        // Preview standalone da grade tática — pra validar visualmente sem depender de combate real.
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { mostrarPreview3D = true },
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Preview grade tática demo" }
        ) {
            Text("⬢ Preview grade tática (demo)")
        }

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

    if (mostrarConfig) {
        ConfiguracaoJogoDialog(
            config = config,
            onConfigChange = { config = it },
            onFechar = { mostrarConfig = false }
        )
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

    // Preview standalone da grade tática (TOK-1: agora o canvas 2D com token de imagem).
    // Não depende de campanha ativa nem combate.
    if (mostrarPreview3D) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { mostrarPreview3D = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                com.gurps.ficha.ui.saga.HexCanvasTatico(viewModel, Modifier.fillMaxSize())
                // Botão fechar no canto superior direito.
                IconButton(
                    onClick = { mostrarPreview3D = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color(0xAA000000), CircleShape)
                        .semantics { contentDescription = "Fechar preview da grade tática" }
                ) { Icon(Icons.Default.Close, contentDescription = null, tint = Color.White) }
            }
        }
    }
}

@Composable
private fun SecaoConfig(titulo: String, conteudo: @Composable () -> Unit) {
    Spacer(Modifier.height(12.dp))
    Text(titulo, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(4.dp))
    conteudo()
}

/** Resumo de uma linha mostrado sob o botão "Configuração do Jogo". */
private fun resumoConfig(c: CampanhaConfig): String {
    val partes = mutableListOf<String>()
    if (c.genero.isNotBlank()) partes.add(c.genero)
    partes.add(c.tom)
    partes.add(c.dificuldade)
    partes.add("NT${c.nivelTecnologico}")
    partes.add(if (c.magiaPermitida) "com magia" else "sem magia")
    return partes.joinToString(" · ")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConfiguracaoJogoDialog(
    config: CampanhaConfig,
    onConfigChange: (CampanhaConfig) -> Unit,
    onFechar: () -> Unit
) {
    Dialog(onDismissRequest = onFechar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxSize()) {
                // Barra superior fixa
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Configuração do Jogo",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onFechar, modifier = Modifier.semantics { contentDescription = "Concluir configuração" }) { Text("Concluir") }
                    IconButton(onClick = onFechar, modifier = Modifier.semantics { contentDescription = "Fechar configuração" }) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }
                HorizontalDivider()
                // Conteúdo rolável + barra de rolagem visível à direita
                BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(16.dp)
                    ) {
                // Gênero
                SecaoConfig("Gênero") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CampanhaConfig.GENEROS.forEach { g ->
                            FilterChip(
                                selected = config.genero == g,
                                onClick = { onConfigChange(config.copy(genero = if (config.genero == g) "" else g)) },
                                label = { Text(g) },
                                modifier = Modifier.semantics { contentDescription = "Gênero $g" }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = config.conceito,
                    onValueChange = { onConfigChange(config.copy(conceito = it)) },
                    label = { Text("Conceito (opcional)") },
                    placeholder = { Text("ex.: caçador de recompensas num faroeste sombrio") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).semantics { contentDescription = "Conceito da campanha" },
                    maxLines = 3
                )

                // Tom
                SecaoConfig("Tom") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CampanhaConfig.TONS.forEach { t ->
                            FilterChip(selected = config.tom == t, onClick = { onConfigChange(config.copy(tom = t)) },
                                label = { Text(t) }, modifier = Modifier.semantics { contentDescription = "Tom $t" })
                        }
                    }
                }

                // Dificuldade
                SecaoConfig("Dificuldade") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CampanhaConfig.DIFICULDADES.forEach { d ->
                            FilterChip(selected = config.dificuldade == d, onClick = { onConfigChange(config.copy(dificuldade = d)) },
                                label = { Text(d) }, modifier = Modifier.semantics { contentDescription = "Dificuldade $d" })
                        }
                    }
                }

                // Magia
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Magia neste mundo", modifier = Modifier.weight(1f))
                    Switch(checked = config.magiaPermitida, onCheckedChange = { onConfigChange(config.copy(magiaPermitida = it)) },
                        modifier = Modifier.semantics { contentDescription = "Permitir magia" })
                }

                // Lote HEX-2: modo tático em hexágonos (demo — sem regras plugadas ainda; padrão OFF).
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Combate tático em hexágonos (⬢)")
                        Text("EXPERIMENTAL: grade DEMO sem regras plugadas (HEX-2). Padrão: OFF.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = config.modoTaticoHex, onCheckedChange = { onConfigChange(config.copy(modoTaticoHex = it)) },
                        modifier = Modifier.semantics { contentDescription = "Modo tático em hexágonos" })
                }

                // Lote TOK-1: o Switch do render 3D (HEX-7) foi removido — o 3D virou legado após
                // teste no aparelho; o modo tático agora é o canvas 2D com tokens de imagem.
                // A flag modoTaticoHex3D segue existindo no CampanhaConfig só por compat de fichas.

                // Nível tecnológico
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nível tecnológico (NT)", modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = { if (config.nivelTecnologico > 0) onConfigChange(config.copy(nivelTecnologico = config.nivelTecnologico - 1)) },
                        modifier = Modifier.semantics { contentDescription = "Diminuir nível tecnológico" }) { Text("−") }
                    Text("NT${config.nivelTecnologico}", modifier = Modifier.padding(horizontal = 12.dp), fontWeight = FontWeight.Bold)
                    OutlinedButton(onClick = { if (config.nivelTecnologico < 12) onConfigChange(config.copy(nivelTecnologico = config.nivelTecnologico + 1)) },
                        modifier = Modifier.semantics { contentDescription = "Aumentar nível tecnológico" }) { Text("+") }
                }

                // Livros liberados
                SecaoConfig("Regras/livros liberados") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CampanhaConfig.LIVROS_DISPONIVEIS.forEach { livro ->
                            val obrigatorio = livro == CampanhaConfig.MODULO_BASICO
                            val sel = livro in config.livros
                            FilterChip(
                                selected = sel,
                                onClick = {
                                    if (!obrigatorio) {
                                        val novos = if (sel) config.livros - livro else config.livros + livro
                                        onConfigChange(config.copy(livros = novos))
                                    }
                                },
                                label = { Text(livro) },
                                modifier = Modifier.semantics { contentDescription = "Livro $livro ${if (sel) "incluído" else "fora"}" }
                            )
                        }
                    }
                }
                        Spacer(Modifier.height(24.dp))
                    }
                    BarraDeRolagem(scrollState, maxHeight, Modifier.align(Alignment.TopEnd))
                }
            }
        }
    }
}

/** Barra de rolagem vertical simples (o Compose Android não traz uma nativa). */
@Composable
private fun BarraDeRolagem(
    scroll: androidx.compose.foundation.ScrollState,
    alturaVisivel: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    if (scroll.maxValue <= 0) return
    val density = LocalDensity.current
    val viewportPx = with(density) { alturaVisivel.toPx() }
    val totalPx = viewportPx + scroll.maxValue
    val minThumbPx = with(density) { 32.dp.toPx() }
    val thumbPx = (viewportPx * viewportPx / totalPx).coerceAtLeast(minThumbPx)
    val trackPx = (viewportPx - thumbPx).coerceAtLeast(0f)
    val topPx = trackPx * (scroll.value.toFloat() / scroll.maxValue.toFloat())
    Box(
        modifier
            .padding(end = 2.dp)
            .offset(y = with(density) { topPx.toDp() })
            .width(4.dp)
            .height(with(density) { thumbPx.toDp() })
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
    )
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
        // Lote TOK-6a — MODO JOGO: header COMPACTO (uma linha fina) com o X de sair no canto
        // direito (o chrome do app está escondido; este X é a única saída do "jogo").
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    viewModel.sagaCenaAtiva?.titulo ?: (viewModel.sagaCampanhaAtiva?.nome ?: "Campanha"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            IconButton(
                onClick = { viewModel.sagaSair() },
                modifier = Modifier.semantics { contentDescription = "Sair da campanha" }
            ) { Icon(Icons.Default.Close, contentDescription = null) }
        }
        HorizontalDivider()

        // Lote TOK-6b-3: no combate TÁTICO o grid é o protagonista — o feed encolhe (menos peso +
        // cards compactos) e a caixa do Narrador sobe pro topo (ver mais abaixo).
        val taticoAtivo = viewModel.sagaCombateAtivo &&
            (viewModel.sagaModoTaticoHex || viewModel.sagaModoTaticoHex3D)

        // Feed
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(if (taticoAtivo) 0.7f else 1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(if (taticoAtivo) 4.dp else 8.dp),
            contentPadding = PaddingValues(vertical = if (taticoAtivo) 4.dp else 8.dp)
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
                TurnoBolha(turno, animar = ehUltimoNarrador, compacto = taticoAtivo)
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

        // Combate ativo (B7): tracker + manobras/defesa. Recebe weight p/ dividir a tela com o feed.
        // Lote TOK-1 (VTT 2D): o modo tático agora é o canvas 2D com TOKENS DE IMAGEM (retrato do
        // jogador circular). O render 3D (HEX-7..9) virou LEGADO após teste no aparelho — a flag
        // `modoTaticoHex3D` ainda existe nas fichas antigas e cai aqui no MESMO canvas 2D novo.
        // A cena tática fica em cima; o CombatePainel embaixo dá acesso a Ataque/Manobra/Defesa.
        if (taticoAtivo) {
            // Lote TOK-6b-3: GRID PROTAGONISTA. A caixa do Narrador sobe pra CÁ (fina, acima do grid)
            // e o painel fixo de baixo saiu — o grid ocupa daqui até o rodapé. As MANOBRAS moram nos
            // tokens (menu translúcido) e o status (Defenda-se!/fim/vez dos inimigos) vira OVERLAY.
            BarraDeEnvio(
                viewModel,
                habilitado = !processando && rolagem == null,
                emCombate = true,
                compacto = true
            )
            Box(Modifier.weight(3f).fillMaxWidth()) {
                com.gurps.ficha.ui.saga.HexCanvasTatico(viewModel, Modifier.fillMaxSize())
                // Status do combate (só quando exige atenção) — no topo, abaixo do cabeçalho da grade,
                // pra não cobrir os hexes de movimento embaixo.
                com.gurps.ficha.ui.saga.CombateStatusTatico(
                    viewModel, Modifier.align(Alignment.TopCenter).padding(top = 42.dp)
                )
                // Lote MA-3d: mira de magia de ÁREA — instrução + Cancelar sobre a grade.
                val mira = viewModel.sagaMiraAreaPendente
                if (mira != null) {
                    Surface(
                        color = Color(0xE6B23A00), contentColor = Color.White, shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 42.dp)
                    ) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🎯 Toque o centro de ${mira.magiaNome} (raio ${mira.raio}m)",
                                style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.width(8.dp))
                            OutlinedButton(onClick = { viewModel.sagaCancelarMiraArea() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.semantics { contentDescription = "Cancelar a mira de área" }) { Text("Cancelar") }
                        }
                    }
                }
                val tokenSelecionado = viewModel.sagaEstadoTatico?.idSelecionado
                // Lote MA-3c: concentrando numa magia → esconde o menu do token (só Continuar/Abortar).
                if (mira == null && tokenSelecionado != null && viewModel.sagaCombateEstado?.conjurando == null) {
                    // Menu do HERÓI vai no TOPO (deixa os hexes verdes de movimento livres embaixo);
                    // menu do INIMIGO fica embaixo (perto do polegar; atacar não precisa mover). O
                    // respiro de 42dp no topo evita cobrir o cabeçalho "Combate tático".
                    val heroi = tokenSelecionado == "heroi"
                    val modMenu = if (heroi) Modifier.align(Alignment.TopCenter).padding(top = 42.dp)
                        else Modifier.align(Alignment.BottomCenter)
                    com.gurps.ficha.ui.saga.MenuTaticoDoToken(
                        viewModel, tokenSelecionado,
                        onFechar = { viewModel.sagaLimparSelecaoTatica() },
                        modifier = modMenu
                    )
                }
            }
        } else {
            if (viewModel.sagaCombateAtivo) {
                com.gurps.ficha.ui.saga.CombatePainel(viewModel, Modifier.weight(1.5f))
            }
            // Fora do tático a caixa fica no rodapé (fluxo de narração / modo faixas).
            BarraDeEnvio(
                viewModel,
                habilitado = !processando && rolagem == null,
                emCombate = viewModel.sagaCombateAtivo
            )
        }
    }

    // Lote HEX-9 (Fase 7 do PILAR): defesa por timing (Clair Obscur). Só entra no modo 3D — no
    // modo 2D e no painel de faixas o UX antigo (botões dentro do painel) permanece. O card OVERLAY
    // toma prioridade sobre a interação da grade enquanto a janela reativa está aberta.
    val defesaPendente = viewModel.sagaCombateDefesaPendente
    if (defesaPendente != null && viewModel.sagaModoTaticoHex3D) {
        com.gurps.ficha.ui.saga.DefesaPorTimingCard(
            pendente = defesaPendente,
            onEscolher = { opcao -> viewModel.sagaCombateDefender(opcao) }
        )
    }
}

@Composable
private fun TurnoBolha(turno: SagaTurn, animar: Boolean, compacto: Boolean = false) {
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
        Column(Modifier.padding(if (compacto) 8.dp else 12.dp)) {
            Text(rotulo, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(
                if (animar) textoVisivel else turno.texto,
                style = if (compacto) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium
            )
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
private fun BarraDeEnvio(
    viewModel: FichaViewModel,
    habilitado: Boolean,
    emCombate: Boolean = false,
    // Lote TOK-6b-3: no combate tático a caixa sobe pro topo (acima do grid) e fica FINA — 1 linha,
    // menos padding — pra roubar o mínimo de espaço da grade.
    compacto: Boolean = false,
) {
    var texto by remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = if (compacto) 2.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = texto,
            onValueChange = { texto = it },
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = if (emCombate) "Falar com o Narrador durante o combate" else "O que seu herói faz" },
            // Em combate as manobras são os botões dos tokens; a caixa serve para FALAR com o Narrador.
            placeholder = { Text(if (emCombate) "Falar com o Narrador…" else "O que você faz?") },
            enabled = habilitado,
            maxLines = if (compacto) 2 else 4,
            textStyle = if (compacto) MaterialTheme.typography.bodySmall else LocalTextStyle.current
        )
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = {
                val t = texto.trim()
                if (t.isNotEmpty()) { viewModel.sagaEnviar(t); texto = "" }
            },
            enabled = habilitado && texto.isNotBlank(),
            contentPadding = if (compacto) PaddingValues(horizontal = 12.dp, vertical = 4.dp) else ButtonDefaults.ContentPadding,
            modifier = Modifier.semantics { contentDescription = "Enviar ação ao Narrador" }
        ) { Text(if (emCombate) "Falar" else "Agir") }
    }
}
