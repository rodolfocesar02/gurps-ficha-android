package com.gurps.ficha.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gurps.ficha.data.network.DiscordRollPayload
import com.gurps.ficha.data.network.DiscordVoiceChannel

import com.gurps.ficha.domain.rules.MagiaEnergiaRules
import com.gurps.ficha.model.PericiaSelecionada
import com.gurps.ficha.model.PERICIAS_COMBATE
import com.gurps.ficha.model.TipoEquipamento
import com.gurps.ficha.viewmodel.DefenseType
import com.gurps.ficha.viewmodel.FichaViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlinx.coroutines.launch
import kotlin.random.Random
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gurps.ficha.BuildConfig

import com.gurps.ficha.ui.features.rolagem.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabRolagem(viewModel: FichaViewModel) {
    val p = viewModel.personagem
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val isSmallScreen = screenWidthDp <= 380
    val isVerySmallScreen = screenWidthDp <= 360
    val isTinyScreen = screenWidthDp <= 320
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    val historico = remember { mutableStateListOf<HistoricoRolagemItem>() }
    val coroutineScope = rememberCoroutineScope()
    val canaisDiscord = viewModel.canaisDiscord
    val canalSelecionadoId = viewModel.canalDiscordSelecionadoId
    val canalSelecionadoNome = viewModel.canalDiscordSelecionadoNome
    val canaisCarregando = viewModel.canaisDiscordCarregando
    val canaisErro = viewModel.canaisDiscordErro
    val backendOnline = canaisErro.isNullOrBlank()
    var showEditarCanalDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (canaisDiscord.isEmpty() && !canaisCarregando) {
            viewModel.atualizarCanaisDiscord()
        }
    }

    // --- State ---
    val atributosRapidos = listOf("ST", "DX", "IQ", "HT", "VON", "PER")
    val modificadoresAtributo = remember { mutableStateMapOf<String, Int>() }
    val modificadoresPericia = remember { mutableStateMapOf<String, Int>() }
    val modificadoresMagia = remember { mutableStateMapOf<String, Int>() }
    val modificadoresTecnica = remember { mutableStateMapOf<String, Int>() }
    var modificadorGlobalPraCego by remember { mutableIntStateOf(0) }

    val pvFixoRolagem = p.pontosVida
    val pvAtualRolagem = p.pontosVidaRolagemAtual ?: p.pontosVida
    val pfAtualRolagem = p.pontosFadigaRolagemAtual ?: p.pontosFadiga
    val pfFixoRolagem = p.pontosFadiga
    val maxPvRolagem = p.pontosVida

    val armas = p.equipamentos.filter { it.tipo == TipoEquipamento.ARMA }
    val periciasCombate = p.pericias.filter { per -> 
        PERICIAS_COMBATE.contains(per.definicaoId.lowercase()) || 
        PERICIAS_COMBATE.any { it.equals(per.nome.replace(" ", "_"), ignoreCase = true) }
    }

    val opcoesAtaque = remember(periciasCombate, p.vantagens) {
        val list = mutableListOf<RollMappedOption>()
        periciasCombate.forEach { per ->
            list.add(RollMappedOption(
                id = "pericia_${per.definicaoId}_${per.especializacao}",
                label = periciaLabel(per),
                contextLabel = "Ataque ${periciaLabel(per)}",
                target = per.calcularNivel(p)
            ))
        }

        p.vantagens.filter { it.definicaoId == "ataque_inato" }.forEach { vant ->
            val nomePers = vant.metadados?.get("nomePersonalizado") ?: vant.nome
            val periciaCorrespondente = p.pericias.find { 
                it.definicaoId == "ataque_inato" && 
                (it.especializacao.contains(nomePers, ignoreCase = true) || nomePers.contains(it.especializacao, ignoreCase = true))
            } ?: p.pericias.find { it.definicaoId == "ataque_inato" }

            val nh = periciaCorrespondente?.calcularNivel(p) ?: (p.destreza - 4)

            list.add(RollMappedOption(
                id = "vant_inato_${nomePers}", 
                label = nomePers,
                contextLabel = "Ataque $nomePers",
                target = nh
            ))
        }
        list
    }

    var ataqueSelecionadoKey by remember { mutableStateOf<String?>(null) }
    val ataqueAtual = remember(ataqueSelecionadoKey, opcoesAtaque) {
        opcoesAtaque.find { it.id == ataqueSelecionadoKey } ?: opcoesAtaque.firstOrNull()
    }

    val fontesDano = remember(armas, p.vantagens) {
        val list = mutableListOf<DamageSourceOption>()
        list.add(DamageSourceOption(id = "st_base", label = "Dano ST", contextLabel = "Dano ST", damageExpression = ""))
        
        armas.forEach { arma ->
            if (!arma.armaDanoRaw.isNullOrBlank()) {
                list.add(DamageSourceOption(
                    id = "arma_${arma.nome}",
                    label = arma.nome,
                    contextLabel = "Dano ${arma.nome}",
                    damageExpression = arma.armaDanoRaw ?: ""
                ))
            }
        }

        p.vantagens.filter { it.definicaoId == "ataque_inato" }.forEach { vant ->
            val dice = vant.metadados?.get("dice") ?: "1"
            val bonus = vant.metadados?.get("bonus")?.toIntOrNull() ?: 0
            val tipo = vant.metadados?.get("tipoDano") ?: "cont"
            val nomePers = vant.metadados?.get("nomePersonalizado") ?: vant.nome
            
            val bonusStr = if (bonus > 0) "+$bonus" else if (bonus < 0) "$bonus" else ""
            val expr = "${dice}d${bonusStr} $tipo"
            
            list.add(DamageSourceOption(
                id = "vant_inato_${nomePers}",
                label = nomePers,
                contextLabel = "Dano $nomePers",
                damageExpression = expr
            ))
        }
        list
    }

    var fonteDanoSelecionadaId by remember { mutableStateOf("st_base") }
    var stDamageMode by remember { mutableStateOf(StDamageMode.GDP) }
    val fonteDanoAtual = remember(fonteDanoSelecionadaId, fontesDano, stDamageMode, p.st) {
        val base = fontesDano.find { it.id == fonteDanoSelecionadaId } ?: fontesDano.first()
        if (base.id == "st_base") {
            val expr = if (stDamageMode == StDamageMode.GDP) p.danoGdP else p.danoGeB
            base.copy(damageExpression = expr)
        } else {
            base
        }
    }

    var modificadorAtaque by remember { mutableIntStateOf(0) }

    val opcoesPericia = p.pericias.map { per ->
        PericiaRollOption(
            id = periciaSelectionKey(per, 0),
            nome = per.nome,
            especializacao = per.especializacao,
            contextLabel = periciaLabel(per),
            target = per.calcularNivel(p),
            descricao = ""
        )
    }

    val aptMagica = p.getVantagemNivel("aptidao_magica")
    val opcoesMagia = p.magias.map { mag ->
        MagiaRollOption(
            id = mag.definicaoId,
            definicaoId = mag.definicaoId,
            nome = mag.nome,
            contextLabel = mag.nome,
            target = mag.calcularNivel(p, aptMagica),
            duracao = mag.duracao,
            energia = mag.energia,
            tempoOperacao = mag.tempoOperacao,
            encantamentoAlvo = mag.encantamentoAlvo,
            descricao = ""
        )
    }

    val opcoesTecnica = p.tecnicas.map { tec ->
        TecnicaRollOption(
            id = tec.nome,
            nome = tec.nome,
            periciaBaseNome = tec.periciaBaseNome,
            contextLabel = tec.nome,
            target = tec.calcularNivel(p),
            descricao = ""
        )
    }

    var showEditarPvRolagemDialog by remember { mutableStateOf(false) }
    var pvAtualInput by remember { mutableStateOf(pvAtualRolagem.toString()) }
    var showEditarPfRolagemDialog by remember { mutableStateOf(false) }
    var pfAtualInput by remember { mutableStateOf(pfAtualRolagem.toString()) }

    var showPericiasDialog by remember { mutableStateOf(false) }
    var showMagiasDialog by remember { mutableStateOf(false) }
    var showTecnicasDialog by remember { mutableStateOf(false) }
    var showMagiaAlmaDialog by remember { mutableStateOf(false) }
    var showRolagemPersonalizadaDialog by remember { mutableStateOf(false) }
    var showConfigAtaqueDialog by remember { mutableStateOf(false) }
    var showConfigDanoDialog by remember { mutableStateOf(false) }

    var aspectoMagiaAlmaSelecionado by remember { mutableStateOf<SoulAspectOption?>(null) }
    var descricaoDialog by remember { mutableStateOf<RollDescricaoDialog?>(null) }

    var dadosPersonalizadosQuantidade by remember { mutableIntStateOf(3) }
    var dadosPersonalizadosFaces by remember { mutableIntStateOf(6) }
    var dadosPersonalizadosModificador by remember { mutableIntStateOf(0) }
    var dadosPersonalizadosQuantidadeInput by remember { mutableStateOf("3") }
    var dadosPersonalizadosFacesInput by remember { mutableStateOf("6") }
    var dadosPersonalizadosModificadorInput by remember { mutableStateOf("0") }
    var ultimaRolagemPersonalizadaMs by remember { mutableStateOf(0L) }

    var magiaPendenteEnergia by remember { mutableStateOf<MagiaRollOption?>(null) }
    var showEnergiaManualDialog by remember { mutableStateOf(false) }
    var energiaManualInput by remember { mutableStateOf("") }
    var talismaMagiaVinculada by remember { mutableStateOf<String?>(null) }
    val repertorioParaTalisma = p.equipamentos
        .filter { it.tipo == TipoEquipamento.GERAL && it.nome.contains("alisma", ignoreCase = true) }
        .map { it.nome }

    // --- Helper Logic ---
    fun registrarResultado(texto: String, payload: DiscordRollPayload) {
        coroutineScope.launch {
            val envio = viewModel.enviarRolagemDiscord(payload)
            val item = HistoricoRolagemItem(
                texto = texto,
                payload = payload,
                statusEnvio = if (envio.enviado) "enviado" else "erro",
                detalheErro = envio.detalhe
            )
            historico.add(0, item)
            if (historico.size > 50) historico.removeAt(historico.size - 1)
        }
    }

    fun executarRolagem(tipo: TipoTeste, contextoLabel: String, alvo: Int?, mod: Int = 0) {
        val d1 = Random.nextInt(1, 7); val d2 = Random.nextInt(1, 7); val d3 = Random.nextInt(1, 7)
        val soma = d1 + d2 + d3
        val modEfetivo = mod + (if (isPraCegoVariant) modificadorGlobalPraCego else 0)
        val alvoEfetivo = if (alvo != null) alvo + modEfetivo else null
        
        val timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val modStr = if (modEfetivo == 0) "" else if (modEfetivo > 0) "+$modEfetivo" else "$modEfetivo"
        val labelComMod = if (modStr.isEmpty()) contextoLabel else "$contextoLabel ($modStr)"
        val statusText = if (alvoEfetivo != null) {
            val dist = alvoEfetivo - soma
            when {
                soma <= 4 -> "Sucesso Crítico!"
                soma >= 17 -> "Falha Crítica!"
                dist >= 0 -> "Sucesso (por $dist)"
                else -> "Falha (por ${abs(dist)})"
            }
        } else ""

        val outcome = if (alvoEfetivo != null) {
            val dist = alvoEfetivo - soma
            when {
                soma <= 4 -> "crítico"
                soma >= 17 -> "falha_crítica"
                dist >= 0 -> "sucesso"
                else -> "falha"
            }
        } else "sucesso"
        val margin = if (alvoEfetivo != null) alvoEfetivo - soma else null

        val textoHist = "[$timestamp] $labelComMod: $soma $statusText"
        val payload = DiscordRollPayload(
            character = p.nome,
            testType = tipo.label,
            context = contextoLabel,
            dice = listOf(d1, d2, d3),
            total = soma,
            modifier = modEfetivo,
            target = alvoEfetivo,
            outcome = outcome,
            margin = margin,
            channelId = viewModel.canalDiscordSelecionadoId
        )
        registrarResultado(textoHist, payload)
    }

    fun executarRolagemDano(contextoLabel: String, danoExpr: String) {
        val parsed = parseDamageExpression(danoExpr) ?: return
        val rolagens = List(parsed.diceCount) { Random.nextInt(1, 7) }
        val somaDados = rolagens.sum()
        val total = (somaDados + parsed.modifier).coerceAtLeast(1)
        
        val timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val textoHist = "[$timestamp] Dano $contextoLabel ($danoExpr): $total"
        
        val payload = DiscordRollPayload(
            character = p.nome,
            testType = TipoTeste.ATAQUE.label,
            context = "Dano $contextoLabel",
            dice = rolagens,
            total = total,
            modifier = parsed.modifier,
            target = null,
            outcome = "dano",
            margin = null,
            channelId = viewModel.canalDiscordSelecionadoId
        )
        registrarResultado(textoHist, payload)
    }

    fun executarRolagemPersonalizada(label: String, qtd: Int, faces: Int, mod: Int) {
        val rolagens = List(qtd) { Random.nextInt(1, faces + 1) }
        val soma = rolagens.sum()
        val total = soma + mod
        val timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val modStr = if (mod == 0) "" else if (mod > 0) "+$mod" else "$mod"
        val expr = "${qtd}d${faces}$modStr"
        val textoHist = "[$timestamp] Livre ($expr): $total"
        
        val payload = DiscordRollPayload(
            character = p.nome,
            testType = TipoTeste.LIVRE.label,
            context = label,
            dice = rolagens,
            total = total,
            modifier = mod,
            target = null,
            outcome = "livre",
            margin = null,
            channelId = viewModel.canalDiscordSelecionadoId
        )
        registrarResultado(textoHist, payload)
        ultimaRolagemPersonalizadaMs = System.currentTimeMillis()
    }

    fun consumirEnergiaMagia(custo: Int) {
        viewModel.atualizarPontosFadigaRolagemAtual(pfAtualRolagem - custo)
        showEnergiaManualDialog = false
        magiaPendenteEnergia = null
        energiaManualInput = ""
        talismaMagiaVinculada = null
    }

    fun tratarCustoEnergiaAposRolagemMagia(magia: MagiaRollOption) {
        val custoBase = MagiaEnergiaRules.parseCusto(magia.energia) ?: return
        val nhEfetivo = (magia.target + (modificadoresMagia[magia.id] ?: 0) + (if (isPraCegoVariant) modificadorGlobalPraCego else 0))
        val reducao = MagiaEnergiaRules.reducaoPorNh(nhEfetivo)
        val custoReduzido = (custoBase - reducao).coerceAtLeast(0)
        
        if (custoReduzido > 0) {
            magiaPendenteEnergia = magia
            energiaManualInput = custoReduzido.toString()
            showEnergiaManualDialog = true
        }
    }

    fun ajustarPvRolagemPorSwipe(incrementar: Boolean) {
        val atual = pvAtualRolagem
        val novo = if (incrementar) atual + 1 else atual - 1
        viewModel.atualizarPontosVidaRolagemAtual(novo)
    }

    fun ajustarPfRolagemPorSwipe(incrementar: Boolean) {
        val atual = pfAtualRolagem
        val novo = if (incrementar) atual + 1 else atual - 1
        viewModel.atualizarPontosFadigaRolagemAtual(novo)
    }

    // --- Styling ---
    val cardTitleStyle = MaterialTheme.typography.titleSmall.copy(fontSize = if (isSmallScreen) 13.sp else 14.sp)
    val statsNumberStyle = MaterialTheme.typography.headlineMedium.copy(fontSize = if (isSmallScreen) 24.sp else 28.sp)
    val defenseNumberStyle = MaterialTheme.typography.headlineSmall.copy(fontSize = if (isSmallScreen) 20.sp else 22.sp)
    val compactLabelStyle = MaterialTheme.typography.labelSmall.copy(fontSize = if (isSmallScreen) 10.sp else 11.sp, lineHeight = 12.sp)
    val horizontalPadding = if (isTinyScreen) 8.dp else 12.dp
    val outerCardVerticalPadding = if (isSmallScreen) 8.dp else 12.dp
    val innerCardVerticalPadding = if (isSmallScreen) 4.dp else 6.dp
    val innerCardPadding = if (isSmallScreen) 4.dp else 8.dp
    val rowSpacing = if (isTinyScreen) 4.dp else 8.dp

    // --- UI Layout ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = horizontalPadding, top = 6.dp, end = horizontalPadding, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        RolagemHeader(
            canalSelecionadoNome = canalSelecionadoNome,
            backendOnline = backendOnline,
            isVerySmallScreen = isVerySmallScreen,
            compactLabelStyle = compactLabelStyle,
            onEditCanal = { showEditarCanalDialog = true }
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = appCardColors()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = outerCardVerticalPadding),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!isPraCegoVariant) {
                    Text(
                        text = "Deslize para cima/baixo em cada atributo para ajustar o modificador.",
                        style = compactLabelStyle,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                AtributosQuickRollPanel(
                    personagem = p,
                    atributosRapidos = atributosRapidos,
                    modificadoresAtributo = modificadoresAtributo,
                    isPraCegoVariant = isPraCegoVariant,
                    cardTitleStyle = cardTitleStyle,
                    statsNumberStyle = statsNumberStyle,
                    compactLabelStyle = compactLabelStyle,
                    innerCardVerticalPadding = innerCardVerticalPadding,
                    onExecutarRolagem = { attr, valor, modAttr ->
                        executarRolagem(
                            tipo = TipoTeste.ATRIBUTO,
                            contextoLabel = attr,
                            alvo = valor,
                            mod = modAttr
                        )
                    }
                )

                PvPfQuickRollPanel(
                    pvFixo = pvFixoRolagem,
                    pvAtual = pvAtualRolagem,
                    pfFixo = pfFixoRolagem,
                    pfAtual = pfAtualRolagem,
                    isPraCegoVariant = isPraCegoVariant,
                    cardTitleStyle = cardTitleStyle,
                    defenseNumberStyle = defenseNumberStyle,
                    innerCardVerticalPadding = innerCardVerticalPadding,
                    onEditPv = { showEditarPvRolagemDialog = true },
                    onEditPf = { showEditarPfRolagemDialog = true },
                    onAjustarPv = { inc -> ajustarPvRolagemPorSwipe(incrementar = inc) },
                    onAjustarPf = { inc -> ajustarPfRolagemPorSwipe(incrementar = inc) }
                )
            }
        }

        if (isPraCegoVariant) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = appCardColors()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = outerCardVerticalPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Modificador Global: ${if (modificadorGlobalPraCego >= 0) "+$modificadorGlobalPraCego" else "$modificadorGlobalPraCego"}",
                        style = cardTitleStyle,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(-5, -2, -1, 0, 1, 2, 5).forEach { delta ->
                            val label = if (delta == 0) "C" else if (delta > 0) "+$delta" else "$delta"
                            val descricao = when {
                                delta < 0 -> "Diminuir modificador em ${abs(delta)}"
                                delta > 0 -> "Aumentar modificador em $delta"
                                else -> "Limpar modificadores"
                            }
                            OutlinedButton(
                                onClick = {
                                    modificadorGlobalPraCego = if (delta == 0) 0 else (modificadorGlobalPraCego + delta).coerceIn(-999, 999)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics { contentDescription = descricao },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                            ) {
                                Text(label)
                            }
                        }
                    }
                }
            }
        }

        AtaqueDanoQuickArea(
            opcoesAtaque = opcoesAtaque,
            ataqueAtual = ataqueAtual,
            fonteDanoAtual = fonteDanoAtual,
            stDamageMode = stDamageMode,
            modificadorAtaque = modificadorAtaque,
            isPraCegoVariant = isPraCegoVariant,
            isVerySmallScreen = isVerySmallScreen,
            cardTitleStyle = cardTitleStyle,
            defenseNumberStyle = defenseNumberStyle,
            compactLabelStyle = compactLabelStyle,
            rowSpacing = rowSpacing,
            innerCardPadding = innerCardPadding,
            innerCardVerticalPadding = innerCardVerticalPadding,
            onConfigAtaque = { showConfigAtaqueDialog = true },
            onConfigDano = { showConfigDanoDialog = true },
            onUpdateStDamageMode = { stDamageMode = it },
            onModificarAtaque = { modificadorAtaque = it },
            onExecutarAtaque = { att, mod ->
                executarRolagem(
                    tipo = TipoTeste.ATAQUE,
                    contextoLabel = att.contextLabel,
                    alvo = att.target,
                    mod = mod
                )
            },
            onExecutarDano = { dano ->
                executarRolagemDano(
                    contextoLabel = fonteDanoAtual.contextLabel,
                    danoExpr = dano
                )
            }
        )

        MenuBotoesNavegacaoRolagem(
            showTecnicas = opcoesTecnica.isNotEmpty(),
            showMagias = opcoesMagia.isNotEmpty(),
            onShowPericias = { showPericiasDialog = true },
            onShowTecnicas = { showTecnicasDialog = true },
            onShowMagias = { showMagiasDialog = true },
            onShowRolagemLivre = { showRolagemPersonalizadaDialog = true }
        )

        HistoricoRolagemPanel(
            historico = historico,
            onReenviar = { index, item ->
                coroutineScope.launch {
                    val envio = viewModel.enviarRolagemDiscord(item.payload)
                    val atualizado = item.copy(
                        statusEnvio = if (envio.enviado) "enviado" else "erro",
                        detalheErro = envio.detalhe
                    )
                    if (index in historico.indices) {
                        historico[index] = atualizado
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    // --- Dialogs (Z-axis orchestration) ---
    if (showEditarCanalDialog) {
        RolagemEditarCanalDialog(
            canaisDiscord = canaisDiscord,
            canalSelecionadoNome = canalSelecionadoNome,
            canaisCarregando = canaisCarregando,
            canaisErro = canaisErro,
            backendOnline = backendOnline,
            onAtualizarCanais = { viewModel.atualizarCanaisDiscord() },
            onCanalSelecionado = { canal -> viewModel.selecionarCanalDiscord(canal) },
            onDismiss = { showEditarCanalDialog = false }
        )
    }

    if (showConfigAtaqueDialog) {
        RolagemConfigurarAtaqueDialog(
            opcoesAtaque = opcoesAtaque,
            ataqueAtual = ataqueAtual,
            onAtaqueSelecionado = { id -> 
                ataqueSelecionadoKey = id
                if (id?.startsWith("vant_inato_") == true) {
                    fonteDanoSelecionadaId = id
                }
                showConfigAtaqueDialog = false 
            },
            onDismiss = { showConfigAtaqueDialog = false }
        )
    }

    if (showConfigDanoDialog) {
        RolagemConfigurarDanoDialog(
            fontesDano = fontesDano,
            fonteDanoAtual = fonteDanoAtual,
            onFonteDanoSelecionada = { id -> fonteDanoSelecionadaId = id },
            onDismiss = { showConfigDanoDialog = false }
        )
    }

    if (showPericiasDialog) {
        RolagemPericiasDialog(
            opcoesPericia = opcoesPericia,
            modificadoresPericia = modificadoresPericia,
            isPraCegoVariant = isPraCegoVariant,
            onShowDescricao = { descricaoDialog = it },
            onExecutarRolagem = { contexto, alvo, mod -> executarRolagem(TipoTeste.PERICIA, contexto, alvo, mod) },
            onDismiss = { showPericiasDialog = false }
        )
    }

    if (showMagiasDialog) {
        RolagemMagiasDialog(
            opcoesMagia = opcoesMagia,
            modificadoresMagia = modificadoresMagia,
            isPraCegoVariant = isPraCegoVariant,
            onShowDescricao = { descricaoDialog = it },
            onExecutarRolagem = { magia, modMagia ->
                executarRolagem(
                    tipo = TipoTeste.MAGIA,
                    contextoLabel = magia.contextLabel,
                    alvo = magia.target,
                    mod = modMagia
                )
                tratarCustoEnergiaAposRolagemMagia(magia)
            },
            onDismiss = { showMagiasDialog = false }
        )
    }

    if (showTecnicasDialog) {
        RolagemTecnicasDialog(
            opcoesTecnica = opcoesTecnica,
            modificadoresTecnica = modificadoresTecnica,
            isPraCegoVariant = isPraCegoVariant,
            onShowDescricao = { descricaoDialog = it },
            onExecutarRolagem = { contexto, alvo, mod -> executarRolagem(TipoTeste.TECNICA, contexto, alvo, mod) },
            onDismiss = { showTecnicasDialog = false }
        )
    }

    if (showMagiaAlmaDialog) {
        RolagemMagiaAlmaDialog(
            aspectos = SOUL_ASPECT_OPTIONS,
            onAspectoSelecionado = { aspectoMagiaAlmaSelecionado = it },
            onDismiss = { showMagiaAlmaDialog = false }
        )
    }

    if (showRolagemPersonalizadaDialog) {
        RolagemPersonalizadaDialog(
            dadosPersonalizadosQuantidade = dadosPersonalizadosQuantidade,
            dadosPersonalizadosFaces = dadosPersonalizadosFaces,
            dadosPersonalizadosModificador = dadosPersonalizadosModificador,
            dadosPersonalizadosQuantidadeInput = dadosPersonalizadosQuantidadeInput,
            dadosPersonalizadosFacesInput = dadosPersonalizadosFacesInput,
            dadosPersonalizadosModificadorInput = dadosPersonalizadosModificadorInput,
            expressaoPersonalizada = "${dadosPersonalizadosQuantidade}d${dadosPersonalizadosFaces}${if (dadosPersonalizadosModificador >= 0) "+$dadosPersonalizadosModificador" else dadosPersonalizadosModificador}",
            isPraCegoVariant = isPraCegoVariant,
            onUpdateQuantidade = { dadosPersonalizadosQuantidade = it },
            onUpdateFaces = { dadosPersonalizadosFaces = it },
            onUpdateModificador = { dadosPersonalizadosModificador = it },
            onInputQuantidade = { raw -> dadosPersonalizadosQuantidadeInput = raw.filter { it.isDigit() }.take(3) },
            onInputFaces = { raw -> dadosPersonalizadosFacesInput = raw.filter { it.isDigit() }.take(3) },
            onInputModificador = { raw -> 
                val allowed = setOf('-') + ('0'..'9')
                dadosPersonalizadosModificadorInput = raw.filter { it in allowed }.take(4)
            },
            onExecutarRolagem = {
                val inputQtd = dadosPersonalizadosQuantidadeInput.toIntOrNull()
                if (inputQtd != null && inputQtd in 1..300) dadosPersonalizadosQuantidade = inputQtd
                val inputFaces = dadosPersonalizadosFacesInput.toIntOrNull()
                if (inputFaces != null && inputFaces in 1..1000) dadosPersonalizadosFaces = inputFaces
                val inputMod = dadosPersonalizadosModificadorInput.toIntOrNull()
                if (inputMod != null && inputMod in -999..999) dadosPersonalizadosModificador = inputMod
                executarRolagemPersonalizada("Livre", dadosPersonalizadosQuantidade, dadosPersonalizadosFaces, dadosPersonalizadosModificador)
            },
            onDismiss = { showRolagemPersonalizadaDialog = false }
        )
    }

    if (showEnergiaManualDialog && magiaPendenteEnergia != null) {
        RolagemEnergiaManualDialog(
            magiaEnergia = magiaPendenteEnergia!!,
            pfAtualRolagem = pfAtualRolagem,
            energiaManualInput = energiaManualInput,
            talismaMagiaVinculada = talismaMagiaVinculada,
            repertorioParaTalisma = repertorioParaTalisma,
            isPraCegoVariant = isPraCegoVariant,
            onInputMudou = { raw -> energiaManualInput = raw.filter { it.isDigit() }.take(4) },
            onTalismaVinculadoMudou = { talismaMagiaVinculada = it },
            onAplicar = { custoFinal -> consumirEnergiaMagia(custoFinal) },
            onDismiss = {
                showEnergiaManualDialog = false
                magiaPendenteEnergia = null
                energiaManualInput = ""
                talismaMagiaVinculada = null
            }
        )
    }

    if (showEditarPvRolagemDialog) {
        RolagemEditarPvDialog(
            pvFixoRolagem = pvFixoRolagem,
            maxPvRolagem = maxPvRolagem,
            pvAtualInput = pvAtualInput,
            onInputMudou = { raw -> pvAtualInput = raw.filter { it.isDigit() }.take(4) },
            onSalvar = {
                val valor = pvAtualInput.toIntOrNull() ?: pvAtualRolagem
                viewModel.atualizarPontosVidaRolagemAtual(valor)
                showEditarPvRolagemDialog = false
            },
            onDismiss = {
                pvAtualInput = pvAtualRolagem.toString()
                showEditarPvRolagemDialog = false
            }
        )
    }

    if (showEditarPfRolagemDialog) {
        RolagemEditarPfDialog(
            pfFixoRolagem = pfFixoRolagem,
            pfAtualInput = pfAtualInput,
            onInputMudou = { raw -> pfAtualInput = raw.filter { it.isDigit() }.take(4) },
            onSalvar = {
                val valor = pfAtualInput.toIntOrNull() ?: pfAtualRolagem
                viewModel.atualizarPontosFadigaRolagemAtual(valor)
                showEditarPfRolagemDialog = false
            },
            onDismiss = {
                pfAtualInput = pfAtualRolagem.toString()
                showEditarPfRolagemDialog = false
            }
        )
    }

    descricaoDialog?.let { dialog ->
        RolagemDescricaoDialogModal(
            dialogInfo = dialog,
            onDismiss = { descricaoDialog = null }
        )
    }

    aspectoMagiaAlmaSelecionado?.let { aspecto ->
        RolagemDescricaoDialogModal(
            dialogInfo = RollDescricaoDialog(titulo = "Aspecto: ${aspecto.nome}", texto = aspecto.descricao),
            onDismiss = { aspectoMagiaAlmaSelecionado = null }
        )
    }
}
