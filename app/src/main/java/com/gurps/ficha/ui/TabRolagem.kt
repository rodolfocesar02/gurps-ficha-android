package com.gurps.ficha.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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

    var ataqueSelecionadoKey by remember { mutableStateOf<String?>(null) }
    var fonteDanoSelecionadaId by remember { mutableStateOf<String?>(null) }
    var modificadorAtaque by remember { mutableIntStateOf(0) }
    val atributosRapidos = listOf("ST", "DX", "IQ", "HT", "VON", "PER")
    val modificadoresAtributo = remember {
        mutableStateMapOf(
            "ST" to 0, "DX" to 0, "IQ" to 0, "HT" to 0, "VON" to 0, "PER" to 0
        )
    }
    val modificadoresDefesa = remember {
        mutableStateMapOf(
            DefenseType.ESQUIVA to 0,
            DefenseType.APARA to 0,
            DefenseType.BLOQUEIO to 0
        )
    }
    val defesasPorTipo = viewModel.defesasAtivasVisiveis.associateBy { it.type }
    var showPericiasDialog by remember { mutableStateOf(false) }
    var showTecnicasDialog by remember { mutableStateOf(false) }
    var showMagiasDialog by remember { mutableStateOf(false) }
    var showRolagemPersonalizadaDialog by remember { mutableStateOf(false) }
    var showMagiaAlmaDialog by remember { mutableStateOf(false) }
    var showEnergiaManualDialog by remember { mutableStateOf(false) }
    var showEditarPvRolagemDialog by remember { mutableStateOf(false) }
    var showEditarPfRolagemDialog by remember { mutableStateOf(false) }
    var magiaPendenteEnergia by remember { mutableStateOf<MagiaRollOption?>(null) }
    var energiaManualInput by remember { mutableStateOf("") }
    var talismaMagiaVinculada by remember { mutableStateOf<String?>(null) }
    var aspectoMagiaAlmaSelecionado by remember { mutableStateOf<SoulAspectOption?>(null) }
    var descricaoDialog by remember { mutableStateOf<RollDescricaoDialog?>(null) }
    var stDamageMode by remember { mutableStateOf(StDamageMode.GDP) }
    var modificadorMagiaAlma by remember { mutableIntStateOf(0) }
    var modificadorGlobalPraCego by remember { mutableIntStateOf(0) }
    var dadosPersonalizadosQuantidade by remember { mutableIntStateOf(1) }
    var dadosPersonalizadosFaces by remember { mutableIntStateOf(6) }
    var dadosPersonalizadosModificador by remember { mutableIntStateOf(0) }
    var dadosPersonalizadosQuantidadeInput by remember { mutableStateOf("1") }
    var dadosPersonalizadosFacesInput by remember { mutableStateOf("6") }
    var dadosPersonalizadosModificadorInput by remember { mutableStateOf("0") }
    var ultimoUsoRolagemPersonalizadaMs by remember { mutableStateOf<Long?>(null) }
    val modificadoresPericia = remember { mutableStateMapOf<String, Int>() }
    val modificadoresTecnica = remember { mutableStateMapOf<String, Int>() }
    val modificadoresMagia = remember { mutableStateMapOf<String, Int>() }
    val horizontalPadding = when {
        isTinyScreen -> 6.dp
        isVerySmallScreen -> 8.dp
        else -> 10.dp
    }
    val rowSpacing = when {
        isTinyScreen -> 4.dp
        else -> 6.dp
    }
    val innerCardPadding = when {
        isTinyScreen -> 4.dp
        else -> 6.dp
    }
    val outerCardVerticalPadding = when {
        isTinyScreen -> 4.dp
        isVerySmallScreen -> 5.dp
        else -> 6.dp
    }
    val innerCardVerticalPadding = when {
        isTinyScreen -> 2.dp
        isVerySmallScreen -> 3.dp
        else -> 4.dp
    }
    val statsNumberStyle = when {
        isTinyScreen -> MaterialTheme.typography.headlineSmall
        isVerySmallScreen -> MaterialTheme.typography.headlineMedium
        else -> MaterialTheme.typography.headlineLarge
    }
    val defenseNumberStyle = when {
        isTinyScreen -> MaterialTheme.typography.headlineSmall
        isVerySmallScreen -> MaterialTheme.typography.headlineMedium
        else -> MaterialTheme.typography.headlineMedium
    }
    val cardTitleStyle = when {
        isTinyScreen -> MaterialTheme.typography.titleSmall
        isVerySmallScreen -> MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp)
        else -> MaterialTheme.typography.titleMedium
    }
    val compactLabelStyle = if (isVerySmallScreen) {
        MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
    } else {
        MaterialTheme.typography.labelSmall
    }

    val pvFixoRolagem = p.pontosVida.coerceAtLeast(0)
    val pfFixoRolagem = p.pontosFadiga.coerceAtLeast(0)
    val maxPvRolagem = (pvFixoRolagem * 5).coerceAtLeast(0)
    val pvAtualRolagem = (p.pontosVidaRolagemAtual ?: pvFixoRolagem).coerceIn(0, maxPvRolagem)
    val pfAtualRolagem = (p.pontosFadigaRolagemAtual ?: pfFixoRolagem).coerceAtLeast(0)
    var pvAtualInput by remember { mutableStateOf(pvAtualRolagem.toString()) }
    var pfAtualInput by remember { mutableStateOf(pfAtualRolagem.toString()) }

    val periciasCombate = p.pericias.filter { it.definicaoId in PERICIAS_COMBATE }
    val basePericiasAtaque = periciasCombate
    val opcoesPericia = p.pericias.mapIndexed { index, pericia ->
        val nivel = pericia.calcularNivel(p)
        val descricaoRegra = viewModel.dataRepository
            .regraPericiaV2(pericia.definicaoId)
            ?.descricao
            .orEmpty()
        PericiaRollOption(
            id = "pericia_${periciaSelectionKey(pericia, index)}",
            nome = pericia.nome,
            especializacao = pericia.especializacao,
            contextLabel = "Pericia ${periciaLabel(pericia)}",
            target = nivel,
            descricao = descricaoRegra
        )
    }
    val opcoesMagia = p.magias.mapIndexedNotNull { index, magia ->
        val definicaoMagia = viewModel.dataRepository.getMagiaPorId(magia.definicaoId)
        // only include if prereqs satisfied
        if (definicaoMagia == null || !viewModel.prereqsSatisfied(definicaoMagia)) return@mapIndexedNotNull null
        val nivel = magia.calcularNivel(p, viewModel.nivelAptidaoMagica)
        val descricaoMagia = magia.texto?.trim().orEmpty().ifBlank { definicaoMagia.texto?.trim().orEmpty() }
        MagiaRollOption(
            id = "magia_${magia.definicaoId}_$index",
            definicaoId = magia.definicaoId,
            nome = magia.nome,
            contextLabel = "Magia ${magia.nome}",
            target = nivel,
            duracao = magia.duracao ?: definicaoMagia.duracao,
            energia = magia.energia ?: definicaoMagia.energia,
            tempoOperacao = magia.tempoOperacao ?: definicaoMagia.tempoOperacao,
            encantamentoAlvo = magia.encantamentoAlvo,
            descricao = descricaoMagia
        )
    }
    val repertorioParaTalisma = p.magias
        .map { it.nome }
        .filter { !it.equals("TalismÃ£", ignoreCase = true) && !it.equals("Talisma", ignoreCase = true) }
        .distinct()
        .sorted()
    val opcoesTecnica = p.tecnicas.mapIndexed { index, tecnica ->
        val descricaoTecnica = viewModel.tecnicasCatalogo
            .firstOrNull { it.id.equals(tecnica.definicaoId, ignoreCase = true) }
            ?.descricao
            .orEmpty()
        TecnicaRollOption(
            id = "tecnica_${tecnica.definicaoId}_$index",
            nome = tecnica.nome,
            periciaBaseNome = tecnica.periciaBaseNome,
            contextLabel = "Tecnica ${tecnica.nome}",
            target = tecnica.calcularNivel(p),
            descricao = descricaoTecnica
        )
    }
    val nivelMagiaDaAlma = 10 + viewModel.nivelAptidaoAstral
    val opcoesAtaque = basePericiasAtaque.mapIndexed { index, pericia ->
        val nivel = pericia.calcularNivel(p)
        RollMappedOption(
            id = periciaSelectionKey(pericia, index),
            label = "${periciaLabel(pericia)} ($nivel)",
            contextLabel = "Ataque ${periciaLabel(pericia)}",
            target = nivel
        )
    }
    val armasEquipadas = p.equipamentos
        .filter { it.tipo == TipoEquipamento.ARMA }
        .mapIndexed { index, equipamento ->
            val dano = equipamento.danoCalculadoComSt(p) ?: equipamento.armaDanoRaw?.trim().orEmpty()
            DamageSourceOption(
                id = "arma_$index",
                label = equipamento.nome,
                contextLabel = "Dano ${equipamento.nome}",
                damageExpression = dano.ifBlank { "-" }
            )
        }
    val fallbackSt = DamageSourceOption(
        id = "st_base",
        label = "Sem arma (${stDamageMode.label})",
        contextLabel = "Dano ST ${stDamageMode.label}",
        damageExpression = if (stDamageMode == StDamageMode.GDP) p.danoGdP else p.danoGeB
    )
    val ataquesInatos = p.vantagens
        .filter { it.definicaoId == "ataque_inato" }
        .mapIndexed { index, vantagem ->
            val diceRaw = vantagem.metadados?.get("dice") ?: "1"
            val dice = if (diceRaw.endsWith(".0")) diceRaw.substringBefore(".0") else diceRaw
            val bonus = vantagem.metadados?.get("bonus")?.toIntOrNull() ?: 0
            val tipo = vantagem.metadados?.get("tipoDano") ?: "cont"
            val nome = vantagem.metadados?.get("nomePersonalizado")?.takeIf { it.isNotBlank() } ?: vantagem.nome
            val expr = buildString {
                append("${dice}d")
                if (bonus > 0) append("+$bonus")
                else if (bonus < 0) append(bonus)
                append(" $tipo")
            }
            DamageSourceOption(
                id = "ataque_inato_$index",
                label = nome,
                contextLabel = "Ataque Inato $nome",
                damageExpression = expr
            )
        }

    val fontesDano = if (armasEquipadas.isNotEmpty() || ataquesInatos.isNotEmpty()) {
        listOf(fallbackSt) + armasEquipadas + ataquesInatos
    } else {
        listOf(fallbackSt)
    }

    val ataqueAtual = opcoesAtaque.firstOrNull { it.id == ataqueSelecionadoKey }
    val fonteDanoAtual = fontesDano.firstOrNull { it.id == fonteDanoSelecionadaId } ?: fontesDano.first()

    LaunchedEffect(opcoesAtaque) {
        if (opcoesAtaque.isNotEmpty() && opcoesAtaque.none { it.id == ataqueSelecionadoKey }) {
            ataqueSelecionadoKey = opcoesAtaque.first().id
        }
    }
    LaunchedEffect(fontesDano) {
        if (fontesDano.isNotEmpty() && fontesDano.none { it.id == fonteDanoSelecionadaId }) {
            fonteDanoSelecionadaId = fontesDano.first().id
        }
    }
    LaunchedEffect(opcoesPericia) {
        val ids = opcoesPericia.map { it.id }.toSet()
        modificadoresPericia.keys.toList().forEach { id ->
            if (id !in ids) modificadoresPericia.remove(id)
        }
        opcoesPericia.forEach { pericia ->
            if (modificadoresPericia[pericia.id] == null) {
                modificadoresPericia[pericia.id] = 0
            }
        }
    }
    LaunchedEffect(opcoesMagia) {
        val ids = opcoesMagia.map { it.id }.toSet()
        modificadoresMagia.keys.toList().forEach { id ->
            if (id !in ids) modificadoresMagia.remove(id)
        }
        opcoesMagia.forEach { magia ->
            if (modificadoresMagia[magia.id] == null) {
                modificadoresMagia[magia.id] = 0
            }
        }
    }
    LaunchedEffect(opcoesTecnica) {
        val ids = opcoesTecnica.map { it.id }.toSet()
        modificadoresTecnica.keys.toList().forEach { id ->
            if (id !in ids) modificadoresTecnica.remove(id)
        }
        opcoesTecnica.forEach { tecnica ->
            if (modificadoresTecnica[tecnica.id] == null) {
                modificadoresTecnica[tecnica.id] = 0
            }
        }
    }
    LaunchedEffect(pvAtualRolagem) {
        pvAtualInput = pvAtualRolagem.toString()
    }
    LaunchedEffect(pfAtualRolagem) {
        pfAtualInput = pfAtualRolagem.toString()
    }
    LaunchedEffect(p.pontosVidaRolagemAtual, pvFixoRolagem, maxPvRolagem) {
        val normalizado = (p.pontosVidaRolagemAtual ?: pvFixoRolagem).coerceIn(0, maxPvRolagem)
        if (p.pontosVidaRolagemAtual != normalizado) {
            viewModel.atualizarPontosVidaRolagemAtual(normalizado)
        }
    }
    LaunchedEffect(p.pontosFadigaRolagemAtual, pfFixoRolagem) {
        val normalizado = (p.pontosFadigaRolagemAtual ?: pfFixoRolagem).coerceAtLeast(0)
        if (p.pontosFadigaRolagemAtual != normalizado) {
            viewModel.atualizarPontosFadigaRolagemAtual(normalizado)
        }
    }

    fun registrarResultado(
        resultado: RolagemResultado,
        payload: DiscordRollPayload,
        statusEnvio: String?,
        detalheErro: String?,
        tipoLabel: String,
        contextoLabel: String,
        alvo: Int?,
        mod: Int
    ) {
        val hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val dadosTexto = resultado.dadosIndividuais.joinToString(" ")
        val resultadoTexto = if (alvo != null) {
            val margemTexto = if (resultado.margem >= 0) "+${resultado.margem}" else "${resultado.margem}"
            "${resultado.tipoResultado.name.replace("_", " ")} $margemTexto"
        } else {
            resultado.total.toString()
        }
        val linha = """
            $hora | ${payload.character}
            $tipoLabel ($contextoLabel)
            Dados: $dadosTexto = ${resultado.total}
            Resultado: $resultadoTexto
        """.trimIndent()
        historico.add(
            0,
            HistoricoRolagemItem(
                texto = linha,
                payload = payload,
                statusEnvio = statusEnvio,
                detalheErro = detalheErro
            )
        )
        if (historico.size > 20) {
            historico.removeLast()
        }
    }

    fun executarRolagem(tipo: TipoTeste, contextoLabel: String, alvo: Int?, mod: Int) {
        val modEfetivo = if (isPraCegoVariant) {
            (mod + modificadorGlobalPraCego).coerceIn(-999, 999)
        } else {
            mod
        }
        val resultado = rolarDados(3, modEfetivo, alvo)
        val payload = DiscordRollPayload(
            character = p.nome.ifBlank { "Personagem" },
            testType = tipo.label,
            context = contextoLabel,
            target = alvo,
            modifier = modEfetivo,
            dice = resultado.dadosIndividuais,
            total = resultado.total,
            outcome = resultado.tipoResultado.name,
            margin = if (resultado.alvo != null) resultado.margem else null,
            channelId = canalSelecionadoId
        )
        coroutineScope.launch {
            val envio = viewModel.enviarRolagemDiscord(payload)
            registrarResultado(
                resultado = resultado,
                payload = payload,
                statusEnvio = if (envio.enviado) "enviado" else "erro",
                detalheErro = envio.detalhe,
                tipoLabel = tipo.label,
                contextoLabel = contextoLabel,
                alvo = alvo,
                mod = modEfetivo
            )
        }
    }

    fun executarRolagemDano(contextoLabel: String, danoExpr: String) {
        val parsed = parseDamageExpression(danoExpr)
        val hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        if (parsed == null) {
            val linha = """
                $hora | ${p.nome.ifBlank { "Personagem" }}
                Dano ($contextoLabel)
                Dados: -
                Resultado: expressao nao rolavel ($danoExpr)
            """.trimIndent()
            val payload = DiscordRollPayload(
                character = p.nome.ifBlank { "Personagem" },
                testType = "Dano",
                context = contextoLabel,
                target = null,
                modifier = 0,
                dice = emptyList(),
                total = 0,
                outcome = TipoResultado.NENHUM.name,
                margin = null,
                channelId = canalSelecionadoId
            )
            coroutineScope.launch {
                val envio = viewModel.enviarRolagemDiscord(payload)
                historico.add(
                    0,
                    HistoricoRolagemItem(
                        texto = linha,
                        payload = payload,
                        statusEnvio = if (envio.enviado) "enviado" else "erro",
                        detalheErro = envio.detalhe
                    )
                )
            }
            return
        }

        val dados = (1..parsed.diceCount).map { Random.nextInt(1, 7) }
        val soma = dados.sum()
        val modEfetivo = if (isPraCegoVariant) {
            (parsed.modifier + modificadorGlobalPraCego).coerceIn(-999, 999)
        } else {
            parsed.modifier
        }
        val total = soma + modEfetivo
        val dadosTexto = dados.joinToString(" ")
        val resultadoTexto = buildString {
            append(total)
            if (parsed.suffix.isNotBlank()) append(" ${parsed.suffix}")
            if (modEfetivo != 0) append(" (mod ${if (modEfetivo > 0) "+$modEfetivo" else "$modEfetivo"})")
        }
        val linha = """
            $hora | ${p.nome.ifBlank { "Personagem" }}
            Dano ($contextoLabel)
            Dados: $dadosTexto = $total
            Resultado: $resultadoTexto
        """.trimIndent()
        val payload = DiscordRollPayload(
            character = p.nome.ifBlank { "Personagem" },
            testType = "Dano",
            context = contextoLabel,
            target = null,
            modifier = modEfetivo,
            dice = dados,
            total = total,
            outcome = TipoResultado.NENHUM.name,
            margin = null,
            channelId = canalSelecionadoId
        )
        coroutineScope.launch {
            val envio = viewModel.enviarRolagemDiscord(payload)
            historico.add(
                0,
                HistoricoRolagemItem(
                    texto = linha,
                    payload = payload,
                    statusEnvio = if (envio.enviado) "enviado" else "erro",
                    detalheErro = envio.detalhe
                )
            )
            if (historico.size > 20) {
                historico.removeLast()
            }
        }
    }

    fun executarRolagemPersonalizada(contextoLabel: String, quantidade: Int, faces: Int, mod: Int) {
        val qtdNormalizada = quantidade.coerceIn(1, 300)
        val facesNormalizadas = faces.coerceIn(1, 1000)
        val modNormalizado = mod.coerceIn(-999, 999)
        val modEfetivo = if (isPraCegoVariant) {
            (modNormalizado + modificadorGlobalPraCego).coerceIn(-999, 999)
        } else {
            modNormalizado
        }
        val dados = (1..qtdNormalizada).map { Random.nextInt(1, facesNormalizadas + 1) }
        val soma = dados.sum()
        val total = soma + modEfetivo
        val expr = buildString {
            append("${qtdNormalizada}d$facesNormalizadas")
            if (modEfetivo > 0) append("+$modEfetivo")
            if (modEfetivo < 0) append(modEfetivo)
        }
        val hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val linha = """
            $hora | ${p.nome.ifBlank { "Personagem" }}
            Livre ($contextoLabel)
            Dados: ${dados.joinToString(" ")} = $total
            Resultado: $total ($expr)
        """.trimIndent()
        val payload = DiscordRollPayload(
            character = p.nome.ifBlank { "Personagem" },
            testType = "Livre",
            context = contextoLabel,
            target = null,
            modifier = modEfetivo,
            dice = dados,
            total = total,
            outcome = TipoResultado.NENHUM.name,
            margin = null,
            channelId = canalSelecionadoId
        )
        coroutineScope.launch {
            val envio = viewModel.enviarRolagemDiscord(payload)
            historico.add(
                0,
                HistoricoRolagemItem(
                    texto = linha,
                    payload = payload,
                    statusEnvio = if (envio.enviado) "enviado" else "erro",
                    detalheErro = envio.detalhe
                )
            )
            if (historico.size > 20) {
                historico.removeLast()
            }
        }
    }

    fun resetarRolagemPersonalizadaParaPadrao() {
        dadosPersonalizadosQuantidade = 1
        dadosPersonalizadosFaces = 6
        dadosPersonalizadosModificador = 0
        dadosPersonalizadosQuantidadeInput = "1"
        dadosPersonalizadosFacesInput = "6"
        dadosPersonalizadosModificadorInput = "0"
    }

    fun reterRolagemPersonalizadaAindaValida(): Boolean {
        val ultimoUso = ultimoUsoRolagemPersonalizadaMs ?: return false
        return (System.currentTimeMillis() - ultimoUso) <= CUSTOM_ROLL_RETENTION_MS
    }

    fun atualizarQuantidadePorInput(raw: String) {
        val filtrado = raw.filter { it.isDigit() }.take(3)
        dadosPersonalizadosQuantidadeInput = filtrado
        filtrado.toIntOrNull()?.let {
            dadosPersonalizadosQuantidade = it.coerceIn(1, 300)
            dadosPersonalizadosQuantidadeInput = dadosPersonalizadosQuantidade.toString()
        }
    }

    fun atualizarFacesPorInput(raw: String) {
        val filtrado = raw.filter { it.isDigit() }.take(4)
        dadosPersonalizadosFacesInput = filtrado
        filtrado.toIntOrNull()?.let {
            dadosPersonalizadosFaces = it.coerceIn(1, 1000)
            dadosPersonalizadosFacesInput = dadosPersonalizadosFaces.toString()
        }
    }

    fun atualizarModificadorPorInput(raw: String) {
        var filtrado = raw.filterIndexed { index, c -> c.isDigit() || (index == 0 && c == '-') }
        if (filtrado.count { it == '-' } > 1) {
            filtrado = filtrado.replace("-", "")
        }
        if (filtrado.isNotEmpty() && !filtrado.startsWith("-")) {
            filtrado = filtrado.filter { it.isDigit() }
        }
        filtrado = if (filtrado.startsWith("-")) {
            "-" + filtrado.drop(1).filter { it.isDigit() }.take(3)
        } else {
            filtrado.filter { it.isDigit() }.take(3)
        }
        dadosPersonalizadosModificadorInput = filtrado
        filtrado.toIntOrNull()?.let {
            dadosPersonalizadosModificador = it.coerceIn(-999, 999)
            dadosPersonalizadosModificadorInput = dadosPersonalizadosModificador.toString()
        }
    }

    fun custoEnergiaFixo(energia: String?): Int? {
        val texto = energia?.trim().orEmpty()
        if (texto.isBlank()) return null
        return texto.toIntOrNull()
    }

    fun consumirEnergiaMagia(custoEnergia: Int) {
        if (custoEnergia <= 0) return
        val novoPf = (pfAtualRolagem - custoEnergia).coerceAtLeast(0)
        viewModel.atualizarPontosFadigaRolagemAtual(novoPf)
    }

    fun custoEnergiaComReducaoNh(custoBase: Int, nhBasico: Int): Int {
        return MagiaEnergiaRules.custoAjustadoPorNh(custoBase, nhBasico)
    }

    fun tratarCustoEnergiaAposRolagemMagia(magia: MagiaRollOption) {
        val nhBasico = magia.target
        val isTalisma = magia.definicaoId.equals("talisma", ignoreCase = true)
        val custoFixo = custoEnergiaFixo(magia.energia)
        if (custoFixo != null && !isTalisma) {
            consumirEnergiaMagia(custoEnergiaComReducaoNh(custoFixo, nhBasico))
            return
        }
        val energiaTexto = magia.energia?.trim().orEmpty()
        if (energiaTexto.isBlank() && !isTalisma) return
        magiaPendenteEnergia = magia
        energiaManualInput = custoFixo?.toString() ?: ""
        talismaMagiaVinculada = magia.encantamentoAlvo?.takeIf { it.isNotBlank() }
        showEnergiaManualDialog = true
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = horizontalPadding, top = 6.dp, end = horizontalPadding, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Button(
            onClick = { showEditarCanalDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (backendOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "EDITAR CANAL",
                    fontSize = if (isVerySmallScreen) 16.sp else 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = canalSelecionadoNome ?: "Selecionar canal de voz",
                    style = compactLabelStyle,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

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

                if (isPraCegoVariant) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = appCardColors()
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = innerCardVerticalPadding),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("PV: $pvFixoRolagem/$pvAtualRolagem", style = cardTitleStyle, fontWeight = FontWeight.SemiBold)
                                    TextButton(
                                        onClick = { showEditarPvRolagemDialog = true },
                                        modifier = Modifier.semantics { contentDescription = "Editar pontos de vida da rolagem" }
                                    ) {
                                        Text("Editar PV")
                                    }
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = appCardColors()
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = innerCardVerticalPadding),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("PF: $pfFixoRolagem/$pfAtualRolagem", style = cardTitleStyle, fontWeight = FontWeight.SemiBold)
                                    TextButton(
                                        onClick = { showEditarPfRolagemDialog = true },
                                        modifier = Modifier.semantics { contentDescription = "Editar pontos de fadiga da rolagem" }
                                    ) {
                                        Text("Editar PF")
                                    }
                                }
                            }
                        }
                        atributosRapidos.forEach { attr ->
                            val valor = p.getAtributo(attr)
                            val nomeAttr = atributoNomeCompleto(attr)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = appCardColors()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("$attr - $nomeAttr", style = cardTitleStyle, fontWeight = FontWeight.SemiBold)
                                    }
                                    Text(
                                        text = valor.toString(),
                                        modifier = Modifier
                                            .semantics {
                                                contentDescription = "Rolar $attr $valor"
                                            }
                                            .clickable {
                                                executarRolagem(
                                                    tipo = TipoTeste.ATRIBUTO,
                                                    contextoLabel = attr,
                                                    alvo = valor,
                                                    mod = 0
                                                )
                                            },
                                        textAlign = TextAlign.Center,
                                        style = statsNumberStyle,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(if (isTinyScreen) 4.dp else 8.dp)
                        ) {
                            atributosRapidos.forEach { attr ->
                                val valor = p.getAtributo(attr)
                                val modAttr = modificadoresAtributo[attr] ?: 0
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = innerCardVerticalPadding),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = attr,
                                        textAlign = TextAlign.Center,
                                        style = cardTitleStyle,
                                        maxLines = 1,
                                        overflow = TextOverflow.Clip,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = valor.toString(),
                                        modifier = Modifier
                                            .pointerInput(attr, modAttr) {
                                                var dragAcumulado = 0f
                                                val passoPx = 20f
                                                detectVerticalDragGestures(
                                                    onVerticalDrag = { change, dragAmount ->
                                                        change.consume()
                                                        dragAcumulado += dragAmount
                                                        while (abs(dragAcumulado) >= passoPx) {
                                                            val atual = modificadoresAtributo[attr] ?: 0
                                                            if (dragAcumulado < 0f) {
                                                                modificadoresAtributo[attr] = (atual + 1).coerceIn(-20, 20)
                                                                dragAcumulado += passoPx
                                                            } else {
                                                                modificadoresAtributo[attr] = (atual - 1).coerceIn(-20, 20)
                                                                dragAcumulado -= passoPx
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                            .clickable {
                                                executarRolagem(
                                                    tipo = TipoTeste.ATRIBUTO,
                                                    contextoLabel = attr,
                                                    alvo = valor,
                                                    mod = modAttr
                                                )
                                            },
                                        textAlign = TextAlign.Center,
                                        style = statsNumberStyle,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    if (modAttr != 0) {
                                        Text(
                                            text = "mod ${if (modAttr >= 0) "+$modAttr" else modAttr}",
                                            style = compactLabelStyle,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = appCardColors()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .pointerInput(pvAtualRolagem) {
                                            var dragAcumulado = 0f
                                            val passoPx = 20f
                                            detectVerticalDragGestures(
                                                onVerticalDrag = { change, dragAmount ->
                                                    change.consume()
                                                    dragAcumulado += dragAmount
                                                    while (abs(dragAcumulado) >= passoPx) {
                                                        ajustarPvRolagemPorSwipe(incrementar = dragAcumulado < 0f)
                                                        dragAcumulado += if (dragAcumulado < 0f) passoPx else -passoPx
                                                    }
                                                }
                                            )
                                        },
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("PV", style = cardTitleStyle, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "$pvFixoRolagem/$pvAtualRolagem",
                                        style = defenseNumberStyle,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = appCardColors()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .pointerInput(pfAtualRolagem) {
                                            var dragAcumulado = 0f
                                            val passoPx = 20f
                                            detectVerticalDragGestures(
                                                onVerticalDrag = { change, dragAmount ->
                                                    change.consume()
                                                    dragAcumulado += dragAmount
                                                    while (abs(dragAcumulado) >= passoPx) {
                                                        ajustarPfRolagemPorSwipe(incrementar = dragAcumulado < 0f)
                                                        dragAcumulado += if (dragAcumulado < 0f) passoPx else -passoPx
                                                    }
                                                }
                                            )
                                        },
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("PF", style = cardTitleStyle, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "$pfFixoRolagem/$pfAtualRolagem",
                                        style = defenseNumberStyle,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
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

        var showConfigAtaqueDialog by remember { mutableStateOf(false) }
        var showConfigDanoDialog by remember { mutableStateOf(false) }

        if (opcoesAtaque.isEmpty()) {
            Text(
                "Sem pericias para ataque. Adicione pericias de combate na aba Pericias.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(rowSpacing)
            ) {
                Button(
                    onClick = { showConfigAtaqueDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        "Ataque",
                        style = if (isVerySmallScreen) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                        maxLines = 1
                    )
                }
                Button(
                    onClick = { showConfigDanoDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        "Dano",
                        style = if (isVerySmallScreen) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            val modAtaqueAtual = if (isPraCegoVariant) 0 else modificadorAtaque
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (!isPraCegoVariant) {
                            Modifier.pointerInput(modAtaqueAtual) {
                                var dragAcumulado = 0f
                                val passoPx = 20f
                                detectVerticalDragGestures(
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        dragAcumulado += dragAmount
                                        while (abs(dragAcumulado) >= passoPx) {
                                            if (dragAcumulado < 0f) {
                                                modificadorAtaque = (modificadorAtaque + 1).coerceIn(-20, 20)
                                                dragAcumulado += passoPx
                                            } else {
                                                modificadorAtaque = (modificadorAtaque - 1).coerceIn(-20, 20)
                                                dragAcumulado -= passoPx
                                            }
                                        }
                                    }
                                )
                            }
                        } else {
                            Modifier
                        }
                    ),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(rowSpacing),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            colors = appCardColors()
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = innerCardPadding, vertical = innerCardVerticalPadding),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text(
                                    ataqueAtual?.contextLabel?.removePrefix("Ataque ") ?: "Ataque",
                                    style = cardTitleStyle,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "NH ${ataqueAtual?.target ?: "-"}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .semantics {
                                            contentDescription = "Rolar ${ataqueAtual?.contextLabel ?: "Ataque"} com nível ${ataqueAtual?.target ?: "-"}"
                                        }
                                        .clickable(enabled = ataqueAtual?.target != null) {
                                            executarRolagem(
                                                tipo = TipoTeste.ATAQUE,
                                                contextoLabel = ataqueAtual?.contextLabel ?: "Ataque",
                                                alvo = ataqueAtual?.target,
                                                mod = modAtaqueAtual
                                            )
                                        },
                                    style = defenseNumberStyle,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                                if (!isPraCegoVariant && modAtaqueAtual != 0) {
                                    Text(
                                        "mod ${if (modAtaqueAtual >= 0) "+$modAtaqueAtual" else "$modAtaqueAtual"}",
                                        style = compactLabelStyle,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            colors = appCardColors()
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = innerCardPadding, vertical = innerCardVerticalPadding),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text(
                                    fonteDanoAtual.label,
                                    style = cardTitleStyle,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (fonteDanoAtual.id == "st_base") {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        StDamageMode.entries.forEach { mode ->
                                            FilterChip(
                                                selected = stDamageMode == mode,
                                                onClick = { stDamageMode = mode },
                                                label = { Text(mode.label) }
                                            )
                                        }
                                    }
                                }
                                val danos = splitDamageEntries(fonteDanoAtual.damageExpression)
                                danos.forEach { danoLinha ->
                                    val danoRolavel = parseDamageExpression(danoLinha) != null
                                    Text(
                                        danoLinha,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .semantics {
                                                contentDescription = "Rolar dano ${fonteDanoAtual.contextLabel}: $danoLinha"
                                            }
                                            .clickable(enabled = danoRolavel) {
                                                executarRolagemDano(
                                                    contextoLabel = fonteDanoAtual.contextLabel,
                                                    danoExpr = danoLinha
                                                )
                                            },
                                        style = cardTitleStyle,
                                        color = if (danoRolavel) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (!isPraCegoVariant && modAtaqueAtual != 0) {
                                    Text(
                                        "mod ${if (modAtaqueAtual >= 0) "+$modAtaqueAtual" else "$modAtaqueAtual"}",
                                        style = compactLabelStyle,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showConfigAtaqueDialog) {
                RolagemConfigurarAtaqueDialog(
                    opcoesAtaque = opcoesAtaque,
                    ataqueAtual = ataqueAtual,
                    onAtaqueSelecionado = { id -> ataqueSelecionadoKey = id },
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

            if (showMagiaAlmaDialog) {
                RolagemMagiaAlmaDialog(
                    aspectos = SOUL_ASPECT_OPTIONS,
                    onAspectoSelecionado = { aspectoMagiaAlmaSelecionado = it },
                    onDismiss = { showMagiaAlmaDialog = false }
                )
            }

            aspectoMagiaAlmaSelecionado?.let { aspecto ->
                RolagemDescricaoDialogModal(
                    dialogInfo = RollDescricaoDialog(titulo = "Aspecto: ${aspecto.nome}", texto = aspecto.descricao),
                    onDismiss = { aspectoMagiaAlmaSelecionado = null }
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

            descricaoDialog?.let { dialog ->
                RolagemDescricaoDialogModal(
                    dialogInfo = dialog,
                    onDismiss = { descricaoDialog = null }
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

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(rowSpacing)
            ) {
                Button(
                    onClick = { showPericiasDialog = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(
                        "Perícias",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                if (opcoesTecnica.isNotEmpty()) {
                    Button(
                        onClick = { showTecnicasDialog = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text(
                            "Técnicas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (opcoesMagia.isNotEmpty()) {
                    Button(
                        onClick = { showMagiasDialog = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text(
                            "Magias",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Button(
                    onClick = { showRolagemPersonalizadaDialog = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(
                        "Rolagem Livre",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        SectionCard(title = "Historico da Sessao") {
            if (historico.isEmpty()) {
                Text(
                    "Nenhuma rolagem ainda.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                historico.forEachIndexed { index, item ->
                    Text(
                        item.texto,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.statusEnvio == "erro") {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    item.statusEnvio?.let { status ->
                        Text(
                            "envio: $status",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (status == "erro") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (item.statusEnvio == "erro" && !item.detalheErro.isNullOrBlank()) {
                        Text(
                            "detalhe: ${item.detalheErro}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        OutlinedButton(
                            onClick = {
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
                        ) {
                            Text("Reenviar")
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

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
}
}
