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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
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

import com.gurps.ficha.domain.roll.CriticoRules
import com.gurps.ficha.domain.rules.MagiaEnergiaRules
import com.gurps.ficha.domain.rules.StBracalRules
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
import com.gurps.ficha.ui.features.dice3d.Dice3DScene
import androidx.compose.ui.draw.blur
import androidx.compose.ui.zIndex

data class PendingRollState(
    val tipo: TipoTeste? = null,
    val contextoLabel: String,
    val alvo: Int? = null,
    val mod: Int = 0,
    val diceCount: Int = 3,
    val isDano: Boolean = false,
    val isPersonalizada: Boolean = false,
    val faces: Int = 6,
    val danoExpr: String? = null
)

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
    val context = LocalContext.current
    val canaisDiscord = viewModel.canaisDiscord
    val canalSelecionadoId = viewModel.canalDiscordSelecionadoId
    val canalSelecionadoNome = viewModel.canalDiscordSelecionadoNome
    val canaisCarregando = viewModel.canaisDiscordCarregando
    val canaisErro = viewModel.canaisDiscordErro
    val backendOnline = canaisErro.isNullOrBlank()
    var showEditarCanalDialog by remember { mutableStateOf(false) }

    var pendingRoll by remember { mutableStateOf<PendingRollState?>(null) }
    var pendingResults by remember { mutableStateOf<List<Int>?>(null) }

    LaunchedEffect(Unit) {
        if (canaisDiscord.isEmpty() && !canaisCarregando) {
            viewModel.atualizarCanaisDiscord()
        }
    }

    // --- State ---
    val atributosRapidos = listOf("ST", "DX", "IQ", "HT", "VON", "PER")
    val modificadoresAtributo = remember { mutableStateMapOf<String, Int>() }
    val modificadoresDefesa = remember { mutableStateMapOf<com.gurps.ficha.viewmodel.DefenseType, Int>() }
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
    val periciasCombate = p.periciasTotais.filter { per -> 
        val idNorm = per.definicaoId.lowercase().removePrefix("racial_")
        val correspondenteCombate = PERICIAS_COMBATE.contains(idNorm) || 
                                   PERICIAS_COMBATE.any { it.equals(per.nome.replace(" ", "_"), ignoreCase = true) }
        
        if (correspondenteCombate) {
            val defId = if (per.definicaoId.startsWith("racial_")) idNorm else per.definicaoId
            val def = viewModel.dataRepository.getPericiaPorId(defId)
            def == null || viewModel.validarPreRequisitosPericia(def) == null
        } else false
    }

    val opcoesAtaque = remember(periciasCombate, p.vantagens) {
        val list = mutableListOf<RollMappedOption>()
        periciasCombate.forEach { per ->
            list.add(RollMappedOption(
                id = "pericia_${per.definicaoId}_${per.especializacao}",
                label = periciaLabel(per),
                contextLabel = "Ataque ${periciaLabel(per)}",
                target = per.calcularNivel(p),
                descricao = viewModel.dataRepository.regraPericiaV2(per.definicaoId)?.descricao.orEmpty()
            ))
        }

        p.vantagens.forEach { vant ->
            com.gurps.ficha.domain.rules.traits.TraitRuleRegistry.getRuleFor(vant.definicaoId)?.let { rule ->
                list.addAll(rule.getAttackOptions(p, vant))
            }
        }
        list
    }

    val defesasAtivas = remember(viewModel.defesasAtivasVisiveis) {
        viewModel.defesasAtivasVisiveis
    }

    // Validação de segurança: Se o ID selecionado sumir da lista (ex: vantagem excluída), limpa a seleção.
    LaunchedEffect(opcoesAtaque) {
        if (viewModel.ataqueSelecionadoId != null && opcoesAtaque.none { it.id == viewModel.ataqueSelecionadoId }) {
            viewModel.atualizarAtaqueSelecionado(null)
        }
    }

    val ataqueAtual = remember(viewModel.ataqueSelecionadoId, opcoesAtaque) {
        opcoesAtaque.find { it.id == viewModel.ataqueSelecionadoId } ?: opcoesAtaque.firstOrNull()
    }

    // ST Bracal: ligada, o ST rolado e o Dano ST passam a usar a forca dos
    // bracos (MB p.89). Fica desligada por padrao -- nem toda acao e de braco.
    var stBracalAtivo by remember { mutableStateOf(false) }
    val bonusStBracal = if (stBracalAtivo) StBracalRules.bonusDe(p) else 0

    val fontesDano = remember(armas, p.vantagens, viewModel.ataqueSelecionadoId, bonusStBracal) {
        val list = mutableListOf<DamageSourceOption>()
        list.add(DamageSourceOption(id = "st_base", label = "Dano ST", contextLabel = "Dano ST", damageExpression = ""))
        
        val selectedSkillId = viewModel.ataqueSelecionadoId?.let { id ->
            if (id.startsWith("pericia_")) id.removePrefix("pericia_") else id
        }

        armas.forEach { arma ->
            // stExtra: empunhar arma e acao de braco, entao a ST Bracal conta.
            val danoExibicao = arma.danoCalculadoComSt(p, selectedSkillId, bonusStBracal)
            if (!danoExibicao.isNullOrBlank()) {
                list.add(DamageSourceOption(
                    id = "arma_${arma.nome}",
                    label = arma.nome,
                    contextLabel = "Dano ${arma.nome}",
                    damageExpression = danoExibicao
                ))
            }
        }
        p.vantagens.forEach { vant ->
            com.gurps.ficha.domain.rules.traits.TraitRuleRegistry.getRuleFor(vant.definicaoId)?.let { rule ->
                list.addAll(rule.getDamageOptions(p, vant))
            }
        }
        list
    }

    // Validação de segurança para Dano
    LaunchedEffect(fontesDano) {
        if (viewModel.fonteDanoSelecionadaId != "st_base" && fontesDano.none { it.id == viewModel.fonteDanoSelecionadaId }) {
            viewModel.atualizarFonteDanoSelecionada("st_base")
        }
    }

    // Lote NOTA-2: de onde veio o bonus de dano da fonte escolhida.
    val origensDoDano = remember(p.vantagens, p.desvantagens, viewModel.ataqueSelecionadoId, viewModel.fonteDanoSelecionadaId) {
        val periciaId = viewModel.ataqueSelecionadoId
            ?.takeIf { it.startsWith("pericia_") }?.removePrefix("pericia_")
        val nomeArma = viewModel.fonteDanoSelecionadaId
            ?.takeIf { it.startsWith("arma_") }?.removePrefix("arma_")
        val grupo = nomeArma?.let { n -> armas.find { it.nome == n }?.armaGrupo }
        com.gurps.ficha.domain.rules.traits.TraitRuleRegistry
            .getDamageBonusOrigens(p, periciaId, nomeArma, grupo)
    }

    // Dano de ST com a forca dos bracos quando a ST Bracal esta ligada: no GURPS
    // o dano corpo a corpo sai da ST, e atacar com arma e acao de braco.
    val danoGdPEfetivo = if (stBracalAtivo) StBracalRules.danoGdPDosBracos(p) else p.danoGdP
    val danoGeBEfetivo = if (stBracalAtivo) StBracalRules.danoGeBDosBracos(p) else p.danoGeB

    val fonteDanoAtual = remember(
        viewModel.fonteDanoSelecionadaId, fontesDano, viewModel.stDamageMode,
        danoGdPEfetivo, danoGeBEfetivo
    ) {
        val base = fontesDano.find { it.id == viewModel.fonteDanoSelecionadaId } ?: fontesDano.first()
        if (base.id == "st_base") {
            val expr = if (viewModel.stDamageMode == StDamageMode.GDP) danoGdPEfetivo else danoGeBEfetivo
            base.copy(damageExpression = expr)
        } else {
            base
        }
    }

    var modificadorAtaque by remember { mutableIntStateOf(0) }


    val opcoesPericia = p.periciasTotais.filter { per ->
        val defId = if (per.definicaoId.startsWith("racial_")) per.definicaoId.removePrefix("racial_") else per.definicaoId
        val def = viewModel.dataRepository.getPericiaPorId(defId)
        def == null || viewModel.validarPreRequisitosPericia(def) == null
    }.map { per ->
        PericiaRollOption(
            id = periciaSelectionKey(per, 0),
            nome = per.nome,
            especializacao = per.especializacao,
            contextLabel = periciaLabel(per),
            target = per.calcularNivel(p),
            descricao = viewModel.dataRepository.regraPericiaV2(per.definicaoId)?.descricao.orEmpty()
        )
    }

    val aptMagica = p.getVantagemNivel("aptidao_magica")
    val opcoesMagia = p.magias.filter { mag ->
        val def = viewModel.dataRepository.magias.find { it.id == mag.definicaoId }
        def == null || viewModel.prereqsSatisfied(def)
    }.map { mag ->
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
            descricao = mag.texto.orEmpty()
        )
    }

    val opcoesTecnica = p.tecnicas.filter { tec ->
        val periciaBase = p.pericias.find { it.definicaoId == tec.periciaBaseDefinicaoId }
        periciaBase?.let { pb ->
            val def = viewModel.dataRepository.getPericiaPorId(pb.definicaoId)
            def == null || viewModel.validarPreRequisitosPericia(def) == null
        } ?: true
    }.map { tec ->
        TecnicaRollOption(
            id = tec.nome,
            nome = tec.nome,
            periciaBaseNome = tec.periciaBaseNome,
            contextLabel = tec.nome,
            target = tec.calcularNivel(p),
            descricao = viewModel.dataRepository.tecnicasCatalogo.find { it.id == tec.definicaoId }?.descricao.orEmpty()
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
    var showSentidosDialog by remember { mutableStateOf(false) } // Lote 372: diálogo de sentidos ao tocar PER
    var showEditarEsquivaDialog by remember { mutableStateOf(false) }
    var showEditarAparaDialog by remember { mutableStateOf(false) }
    var showEditarBloqueioDialog by remember { mutableStateOf(false) }

    var aspectoMagiaAlmaSelecionado by remember { mutableStateOf<SoulAspectOption?>(null) }
    var descricaoDialog by remember { mutableStateOf<RollDescricaoDialog?>(null) }

    var dadosPersonalizadosQuantidade by remember { mutableIntStateOf(3) }
    var dadosPersonalizadosFaces by remember { mutableIntStateOf(6) }
    var dadosPersonalizadosModificador by remember { mutableIntStateOf(0) }
    var dadosPersonalizadosQuantidadeInput by remember { mutableStateOf("3") }
    var dadosPersonalizadosFacesInput by remember { mutableStateOf("6") }
    var dadosPersonalizadosModificadorInput by remember { mutableStateOf("0") }
    var dadosPersonalizadosMotivo by remember { mutableStateOf("") }
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

    /** Rola 3d6 nas tabelas críticas e envia uma 2ª mensagem ao Discord/histórico. */
    fun dispararTabelaCritica(
        critico: CriticoRules.ResultadoCritico,
        contextoLabel: String
    ) {
        val tabela = CriticoRules.rolarTabela(context, critico) ?: return
        val ts = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val textoHist = "[$ts] ${tabela.titulo} — $contextoLabel\n${tabela.textoCompleto}"
        val payload = DiscordRollPayload(
            character = p.nome,
            testType = tabela.titulo,           // ex.: "💥 Golpe Fulminante (3d6 = 11)"
            context = contextoLabel,
            dice = tabela.dados,
            total = tabela.soma,
            modifier = 0,
            target = null,
            outcome = tabela.textoCompleto,     // o texto das duas tabelas
            margin = null,
            channelId = viewModel.canalDiscordSelecionadoId
        )
        registrarResultado(textoHist, payload)
    }

    fun executarRolagem(tipo: TipoTeste, contextoLabel: String, alvo: Int?, mod: Int = 0) {
        pendingRoll = PendingRollState(
            tipo = tipo,
            contextoLabel = contextoLabel,
            alvo = alvo,
            mod = mod,
            diceCount = 3,
            isDano = false
        )
    }

    fun finalizarRolagem(pending: PendingRollState, rolagens: List<Int>) {
        val soma = rolagens.sum()
        val modEfetivo = pending.mod + (if (isPraCegoVariant) modificadorGlobalPraCego else 0)
        val alvoEfetivo = if (pending.alvo != null) pending.alvo + modEfetivo else null
        
        val timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val modStr = if (modEfetivo == 0) "" else if (modEfetivo > 0) "+$modEfetivo" else "$modEfetivo"
        val labelComMod = if (modStr.isEmpty()) pending.contextoLabel else "${pending.contextoLabel} ($modStr)"

        // Classificação de crítico pela regra COMPLETA (considera o NH efetivo).
        val critico = CriticoRules.classificar(soma, alvoEfetivo)
        val statusText = if (alvoEfetivo != null) {
            val dist = alvoEfetivo - soma
            val marginPart = "(por ${abs(dist)})"
            when (critico) {
                CriticoRules.ResultadoCritico.DECISIVO -> "Sucesso Crítico! $marginPart"
                CriticoRules.ResultadoCritico.FALHA_CRITICA -> "Falha Crítica! $marginPart"
                else -> if (dist >= 0) "Sucesso $marginPart" else "Falha $marginPart"
            }
        } else ""

        val outcome = if (alvoEfetivo != null) {
            val dist = alvoEfetivo - soma
            val marginPart = " (por ${abs(dist)})"
            when (critico) {
                CriticoRules.ResultadoCritico.DECISIVO -> "crítico$marginPart"
                CriticoRules.ResultadoCritico.FALHA_CRITICA -> "falha_crítica$marginPart"
                else -> if (dist >= 0) "sucesso$marginPart" else "falha$marginPart"
            }
        } else "sucesso"
        val margin = if (alvoEfetivo != null) alvoEfetivo - soma else null

        val textoHist = if (alvoEfetivo != null) {
            "[$timestamp] $labelComMod (NH $alvoEfetivo): $soma $statusText"
        } else {
            "[$timestamp] $labelComMod: $soma $statusText"
        }
        val payload = DiscordRollPayload(
            character = p.nome,
            testType = pending.tipo?.label ?: TipoTeste.ATAQUE.label,
            context = pending.contextoLabel,
            dice = rolagens,
            total = soma,
            modifier = modEfetivo,
            target = alvoEfetivo,
            outcome = outcome,
            margin = margin,
            channelId = viewModel.canalDiscordSelecionadoId
        )
        registrarResultado(textoHist, payload)

        // AUTOMAÇÃO: em testes de COMBATE (Ataque/Defesa/Técnica/Magia), um
        // Sucesso Decisivo ou Falha Crítica dispara automaticamente a rolagem
        // 3d6 nas tabelas (Golpe Fulminante / Erro Crítico) — 2ª mensagem.
        if (critico != CriticoRules.ResultadoCritico.NORMAL &&
            pending.tipo != null && CriticoRules.ehTesteDeCombate(pending.tipo.label)
        ) {
            dispararTabelaCritica(critico, pending.contextoLabel)
        }
    }

    fun executarRolagemDano(contextoLabel: String, danoExpr: String, periciaId: String? = null) {
        val parsed = parseDamageExpression(danoExpr) ?: return
        
        pendingRoll = PendingRollState(
            contextoLabel = contextoLabel,
            danoExpr = danoExpr,
            mod = parsed.modifier,
            diceCount = parsed.diceCount,
            isDano = true
        )
    }

    fun finalizarRolagemDano(pending: PendingRollState, rolagens: List<Int>) {
        val parsed = parseDamageExpression(pending.danoExpr ?: "") ?: return
        
        val somaDados = rolagens.sum()
        val total = (somaDados + pending.mod).coerceAtLeast(1)
        
        val timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val textoHist = "[$timestamp] Dano ${pending.contextoLabel} (${pending.danoExpr}): $total"
        
        val payload = DiscordRollPayload(
            character = p.nome,
            testType = TipoTeste.ATAQUE.label,
            context = "Dano ${pending.contextoLabel}",
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

    fun finalizarRolagemPersonalizada(label: String, qtd: Int, faces: Int, mod: Int, rolagens: List<Int>) {
        val soma = rolagens.sum()
        val total = soma + mod
        val timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val modStr = if (mod == 0) "" else if (mod > 0) "+$mod" else "$mod"
        val expr = "${qtd}d${faces}$modStr"
        val labelEfetivo = label.ifBlank { "Livre" }
        val textoHist = "[$timestamp] $labelEfetivo ($expr): $total"
        
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

    fun executarRolagemPersonalizada(label: String, qtd: Int, faces: Int, mod: Int) {
        if (faces == 6) {
            pendingRoll = PendingRollState(
                contextoLabel = label,
                diceCount = qtd,
                faces = faces,
                mod = mod,
                isPersonalizada = true
            )
        } else {
            // Se não for D6, rola invisível mesmo (não temos modelos 3D de D10, D20 ainda)
            val rolagens = List(qtd) { Random.nextInt(1, faces + 1) }
            finalizarRolagemPersonalizada(label, qtd, faces, mod, rolagens)
        }
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

    var showTestDado3d by remember { mutableStateOf(false) }

    // --- UI Layout ---
    Box(modifier = Modifier.fillMaxSize()) {
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

            Button(
                onClick = { showTestDado3d = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Testar Física 3D e Som (Lote 2 e 3)",
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isSmallScreen) 14.sp else 16.sp
                )
            }

            if (isPraCegoVariant) SectionHeaderPraCego("Atributos e Status")
        PainelAtributosEStatus(
            personagem = p,
            atributosRapidos = atributosRapidos,
            modificadoresAtributo = modificadoresAtributo,
            pvFixo = pvFixoRolagem,
            pvAtual = pvAtualRolagem,
            pfFixo = pfFixoRolagem,
            pfAtual = pfAtualRolagem,
            isPraCegoVariant = isPraCegoVariant,
            cardTitleStyle = cardTitleStyle,
            statsNumberStyle = statsNumberStyle,
            defenseNumberStyle = defenseNumberStyle,
            compactLabelStyle = compactLabelStyle,
            outerCardVerticalPadding = outerCardVerticalPadding,
            innerCardVerticalPadding = innerCardVerticalPadding,
            stBracalAtivo = stBracalAtivo,
            onAlternarStBracal = { stBracalAtivo = !stBracalAtivo },
            onRolarAtributo = { attr, valor, modAttr ->
                if (attr == "PER") {
                    // Lote 372: PER abre o diálogo de Testes de Sentidos.
                    showSentidosDialog = true
                } else {
                    executarRolagem(TipoTeste.ATRIBUTO, attr, valor, modAttr)
                }
            },
            onEditPv = { showEditarPvRolagemDialog = true },
            onEditPf = { showEditarPfRolagemDialog = true },
            onAjustarPv = { inc -> ajustarPvRolagemPorSwipe(incrementar = inc) },
            onAjustarPf = { inc -> ajustarPfRolagemPorSwipe(incrementar = inc) }
        )

        DefesasAtivasQuickRollPanel(
            defesasAtivas = defesasAtivas,
            modificadoresDefesa = modificadoresDefesa,
            isPraCegoVariant = isPraCegoVariant,
            cardTitleStyle = cardTitleStyle,
            defenseNumberStyle = defenseNumberStyle,
            compactLabelStyle = compactLabelStyle,
            innerCardVerticalPadding = innerCardVerticalPadding,
            onConfigEsquiva = { showEditarEsquivaDialog = true },
            onConfigApara = { showEditarAparaDialog = true },
            onConfigBloqueio = { showEditarBloqueioDialog = true },
            onExecutarRolagem = { defesa, mod ->
                executarRolagem(
                    tipo = TipoTeste.DEFESA,
                    contextoLabel = defesa.name,
                    alvo = defesa.finalValue,
                    mod = mod
                )
            }
        )

        if (isPraCegoVariant) {
            SectionHeaderPraCego("Modificador Global")
            PainelModificadorGlobal(
                modificador = modificadorGlobalPraCego,
                cardTitleStyle = cardTitleStyle,
                verticalPadding = outerCardVerticalPadding,
                onModificadorChange = { modificadorGlobalPraCego = it }
            )
        }

        if (isPraCegoVariant) SectionHeaderPraCego("Combate: Ataque e Dano")
        AtaqueDanoQuickArea(
            opcoesAtaque = opcoesAtaque,
            ataqueAtual = ataqueAtual,
            fonteDanoAtual = fonteDanoAtual,
            gdp = danoGdPEfetivo,
            geb = danoGeBEfetivo,
            stDamageMode = viewModel.stDamageMode,
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
            onUpdateStDamageMode = { viewModel.atualizarStDamageMode(it) },
            onModificarAtaque = { modificadorAtaque = it },
            onShowDescricao = { descricaoDialog = it },
            onExecutarAtaque = { att, mod ->
                executarRolagem(
                    tipo = TipoTeste.ATAQUE,
                    contextoLabel = att.contextLabel,
                    alvo = att.target,
                    mod = mod
                )
            },
            origensDoDano = origensDoDano,
            onExecutarDano = { dano ->
                val perId = if (ataqueAtual?.id?.startsWith("pericia_") == true) {
                    ataqueAtual.id.removePrefix("pericia_")
                } else null
                
                executarRolagemDano(
                    contextoLabel = fonteDanoAtual.contextLabel,
                    danoExpr = dano,
                    periciaId = perId
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

        // Reacao: so aparece se algum traco mexer em reacao.
        PainelReacao(
            personagem = p,
            isPraCegoVariant = isPraCegoVariant,
            onRolar = { label, alvo, mod ->
                executarRolagem(tipo = TipoTeste.ATRIBUTO, contextoLabel = label, alvo = alvo, mod = mod)
            }
        )

        // Autocontrole: so aparece se a ficha tiver desvantagem com NA.
        PainelAutocontrole(
            personagem = p,
            isPraCegoVariant = isPraCegoVariant,
            modSituacional = if (isPraCegoVariant) modificadorGlobalPraCego else 0,
            onRolar = { label, alvo, mod ->
                executarRolagem(tipo = TipoTeste.ATRIBUTO, contextoLabel = label, alvo = alvo, mod = mod)
            }
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

    val blurModifier = if (pendingRoll != null) Modifier.blur(20.dp) else Modifier

    pendingRoll?.let { pr ->
        OverlayDados3D(
            pendingRoll = pr,
            pendingResults = pendingResults,
            isPraCegoVariant = isPraCegoVariant,
            modificadorGlobalPraCego = modificadorGlobalPraCego,
            onResultadosProntos = { resultados -> pendingResults = resultados },
            onCancelar = { pendingRoll = null },
            onConfirmar = { prState, resultadosFinais ->
                pendingRoll = null
                pendingResults = null
                when {
                    prState.isDano -> finalizarRolagemDano(prState, resultadosFinais)
                    prState.isPersonalizada -> finalizarRolagemPersonalizada(
                        prState.contextoLabel, prState.diceCount, prState.faces, prState.mod, resultadosFinais
                    )
                    else -> finalizarRolagem(prState, resultadosFinais)
                }
            }
        )
    }

    if (showConfigAtaqueDialog) {
        RolagemConfigurarAtaqueDialog(
            opcoesAtaque = opcoesAtaque,
            ataqueAtual = ataqueAtual,
            onAtaqueSelecionado = { id -> 
                viewModel.atualizarAtaqueSelecionado(id)
                showConfigAtaqueDialog = false 
            },
            onDismiss = { showConfigAtaqueDialog = false }
        )
    }

    if (showConfigDanoDialog) {
        RolagemConfigurarDanoDialog(
            fontesDano = fontesDano,
            fonteDanoAtual = fonteDanoAtual ?: fontesDano.first(),
            onFonteDanoSelecionada = { id -> 
                viewModel.atualizarFonteDanoSelecionada(id)
                showConfigDanoDialog = false
            },
            onDismiss = { showConfigDanoDialog = false }
        )
    }

    if (showPericiasDialog) {
        RolagemPericiasDialog(
            personagem = p,
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
            motivo = dadosPersonalizadosMotivo,
            onUpdateQuantidade = { dadosPersonalizadosQuantidade = it },
            onUpdateFaces = { dadosPersonalizadosFaces = it },
            onUpdateModificador = { dadosPersonalizadosModificador = it },
            onInputQuantidade = { raw -> dadosPersonalizadosQuantidadeInput = raw.filter { it.isDigit() }.take(3) },
            onInputFaces = { raw -> dadosPersonalizadosFacesInput = raw.filter { it.isDigit() }.take(3) },
            onInputModificador = { raw ->
                val allowed = setOf('-') + ('0'..'9')
                dadosPersonalizadosModificadorInput = raw.filter { it in allowed }.take(4)
            },
            onUpdateMotivo = { dadosPersonalizadosMotivo = it },
            onExecutarRolagem = {
                val inputQtd = dadosPersonalizadosQuantidadeInput.toIntOrNull()
                if (inputQtd != null && inputQtd in 1..300) dadosPersonalizadosQuantidade = inputQtd
                val inputFaces = dadosPersonalizadosFacesInput.toIntOrNull()
                if (inputFaces != null && inputFaces in 1..1000) dadosPersonalizadosFaces = inputFaces
                val inputMod = dadosPersonalizadosModificadorInput.toIntOrNull()
                if (inputMod != null && inputMod in -999..999) dadosPersonalizadosModificador = inputMod
                executarRolagemPersonalizada(dadosPersonalizadosMotivo, dadosPersonalizadosQuantidade, dadosPersonalizadosFaces, dadosPersonalizadosModificador)
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

    if (showSentidosDialog) {
        com.gurps.ficha.ui.features.rolagem.DialogoSentidos(
            personagem = p,
            isPraCegoVariant = isPraCegoVariant,
            modSituacional = modificadoresAtributo["PER"] ?: 0,
            onRolar = { label, alvo, mod ->
                executarRolagem(tipo = TipoTeste.ATRIBUTO, contextoLabel = label, alvo = alvo, mod = mod)
                showSentidosDialog = false
            },
            onFechar = { showSentidosDialog = false }
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

    // --- Diálogos de Configuração de Defesa ---
    if (showEditarEsquivaDialog) {
        EditarEsquivaBonusDialog(
            personagem = p,
            bonusAtual = p.defesasAtivas.bonusManualEsquiva,
            notaAtual = p.defesasAtivas.notaBonusManualEsquiva,
            onDismiss = { showEditarEsquivaDialog = false },
            onConfirm = { bonus, nota ->
                viewModel.atualizarBonusManualEsquiva(bonus, nota)
                showEditarEsquivaDialog = false
            }
        )
    }

    if (showEditarAparaDialog) {
        EditarAparaDialog(
            personagem = p,
            pericias = viewModel.periciasParaApara,
            periciaSelecionadaId = p.defesasAtivas.periciaAparaId,
            bonusAtual = p.defesasAtivas.bonusManualApara,
            notaAtual = p.defesasAtivas.notaBonusManualApara,
            onDismiss = { showEditarAparaDialog = false },
            onConfirm = { periciaId, bonus, nota ->
                viewModel.atualizarPericiaApara(periciaId)
                viewModel.atualizarBonusManualApara(bonus, nota)
                showEditarAparaDialog = false
            }
        )
    }

    if (showEditarBloqueioDialog) {
        EditarBloqueioDialog(
            personagem = p,
            pericias = viewModel.periciasParaBloqueio,
            escudos = viewModel.escudosEquipados,
            periciaSelecionadaId = p.defesasAtivas.periciaBloqueioId,
            escudoSelecionadoNome = p.defesasAtivas.escudoSelecionadoNome,
            bonusAtual = p.defesasAtivas.bonusManualBloqueio,
            notaAtual = p.defesasAtivas.notaBonusManualBloqueio,
            onDismiss = { showEditarBloqueioDialog = false },
            onConfirm = { periciaId, escudoNome, bonus, nota ->
                viewModel.atualizarPericiaBloqueio(periciaId)
                viewModel.atualizarEscudoBloqueio(escudoNome)
                viewModel.atualizarBonusManualBloqueio(bonus, nota)
                showEditarBloqueioDialog = false
            }
        )
    }
    }
}
