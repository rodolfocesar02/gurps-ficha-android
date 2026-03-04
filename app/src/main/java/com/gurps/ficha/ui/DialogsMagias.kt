package com.gurps.ficha.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.gurps.ficha.BuildConfig
import com.gurps.ficha.model.Dificuldade
import com.gurps.ficha.model.MagiaDefinicao
import com.gurps.ficha.model.MagiaSelecionada
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.viewmodel.FichaViewModel
import java.text.Normalizer
import java.util.Locale
import kotlin.math.abs

private val PONTOS_PRESETS = listOf(1, 2, 4, 8, 12)
private const val MODO_ALVO_HABILITADO = false
private const val AJUDA_VOZ_HABILITADA = false
private const val MAX_OPCOES_MODO_ALVO = 3

private fun ajustarPontosPreset(atual: Int, incrementar: Boolean): Int {
    val indice = PONTOS_PRESETS.indexOf(atual).let { if (it == -1) 0 else it }
    return if (incrementar) {
        PONTOS_PRESETS[(indice + 1).coerceAtMost(PONTOS_PRESETS.lastIndex)]
    } else {
        PONTOS_PRESETS[(indice - 1).coerceAtLeast(0)]
    }
}

private fun formatarFalhaPreReq(falha: String): String {
    return falha
        .replace(Regex("(?i)^pré[-‑ ]requisito\\s+não\\s+atendido\\s*:\\s*"), "")
        .replace(Regex("(?i)^pre[-‑ ]requisito\\s+nao\\s+atendido\\s*:\\s*"), "")
        .replace(Regex("(?i)conhecimento\\s+magico\\s+requerido\\s*:\\s*"), "")
        .replace(Regex("(?i)conhecimento\\s+requerido\\s*:\\s*"), "")
        .replace(Regex("\\s*,\\s*"), ", ")
        .replace(Regex("\\s+"), " ")
        .trim()
}

private fun motivoBloqueioCurto(falha: String): String {
    val limpo = formatarFalhaPreReq(falha)
    if (limpo.isBlank()) return "Pré-requisito pendente."
    return limpo
        .substringBefore(".")
        .substringBefore(";")
        .substringBefore(",")
        .trim()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelecionarMagiaDialog(viewModel: FichaViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    val ajudaVozAtiva = isPraCegoVariant && AJUDA_VOZ_HABILITADA
    var magiaSelecionada by remember { mutableStateOf<MagiaDefinicao?>(null) }
    var erroAdicionarMagia by remember { mutableStateOf<String?>(null) }
    var modoAlvoAtivo by remember { mutableStateOf(false) }
    var magiaAlvoId by remember { mutableStateOf<String?>(null) }
    var statusAjudaVoz by remember { mutableStateOf<String?>(null) }
    val modoAlvoAtivoEfetivo = MODO_ALVO_HABILITADO && modoAlvoAtivo

    val listaFiltrada = viewModel.magiasFiltradas
    val catalogoMagias = viewModel.dataRepository.magias
    val escolas = viewModel.todasEscolasMagia
    val classes = viewModel.todasClassesMagia
    val magiaAlvoSelecionada = catalogoMagias.firstOrNull { it.id == magiaAlvoId }
    val ordemRelacionadosAlvo: List<String> = if (modoAlvoAtivoEfetivo && magiaAlvoSelecionada != null) {
        viewModel.modoAlvoRelacionadosIds
    } else {
        emptyList()
    }
    val assinaturaModoAlvo = viewModel.assinaturaEstadoMagiasParaModoAlvo()
    LaunchedEffect(modoAlvoAtivoEfetivo, magiaAlvoId, assinaturaModoAlvo) {
        viewModel.requisitarModoAlvo(magiaAlvoId, modoAlvoAtivoEfetivo)
    }
    fun normalizarEscola(raw: String): String {
        val semAcento = Normalizer.normalize(raw, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return semAcento
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
    val listaExibicao = if (modoAlvoAtivoEfetivo && magiaAlvoSelecionada != null) {
        val relacionadas = ordemRelacionadosAlvo
            .mapNotNull { id -> catalogoMagias.firstOrNull { it.id == id } }
        val pool = if (relacionadas.isNotEmpty()) relacionadas else catalogoMagias
        val idsJaAdicionadas = viewModel.personagem.magias.map { it.definicaoId }.toSet()
        val escolasConhecidas = viewModel.personagem.magias
            .flatMap { it.escola.orEmpty() }
            .map(::normalizarEscola)
            .filter { it.isNotBlank() }
            .toMutableSet()
        val candidatas = pool.filter { magia ->
            magia.id != magiaAlvoSelecionada.id &&
                magia.id !in idsJaAdicionadas
        }
        val aprendiveis = candidatas.filter { viewModel.prereqFailureForMagia(it) == null }
        val selecionadas = mutableListOf<MagiaDefinicao>()
        val fila = aprendiveis.toMutableList()
        while (selecionadas.size < MAX_OPCOES_MODO_ALVO && fila.isNotEmpty()) {
            val idxEscolaNova = fila.indexOfFirst { magia ->
                val escolaPrincipal = magia.escola?.firstOrNull()?.let(::normalizarEscola).orEmpty()
                escolaPrincipal.isBlank() || escolaPrincipal !in escolasConhecidas
            }
            val escolhida = if (idxEscolaNova >= 0) fila.removeAt(idxEscolaNova) else fila.removeAt(0)
            selecionadas.add(escolhida)
            escolhida.escola?.firstOrNull()?.let(::normalizarEscola)?.takeIf { it.isNotBlank() }?.let {
                escolasConhecidas.add(it)
            }
        }
        val fallbackBloqueadas = if (selecionadas.isEmpty()) {
            candidatas.take(MAX_OPCOES_MODO_ALVO)
        } else {
            emptyList()
        }
        listOf(magiaAlvoSelecionada) + if (selecionadas.isNotEmpty()) selecionadas else fallbackBloqueadas
    } else {
        listaFiltrada
    }
    val proximaSugerida = if (modoAlvoAtivoEfetivo && magiaAlvoSelecionada != null) {
        listaExibicao.firstOrNull { magia ->
            magia.id != magiaAlvoSelecionada.id &&
                !viewModel.magiaJaAdicionada(magia.id) &&
                viewModel.prereqFailureForMagia(magia) == null
        }
    } else {
        null
    }

    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        var localEngine: TextToSpeech? = null
        val engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                localEngine?.language = Locale("pt", "BR")
            }
        }
        localEngine = engine
        ttsEngine = engine
        onDispose {
            engine.stop()
            engine.shutdown()
            ttsEngine = null
        }
    }
    fun falarAjuda(texto: String) {
        statusAjudaVoz = texto
        ttsEngine?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "ajuda_voz_magias")
    }
    fun normalizarComandoVoz(valor: String): String {
        val semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return semAcento
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
    fun encontrarMagiaPorComando(raw: String): MagiaDefinicao? {
        val alvo = normalizarComandoVoz(raw)
        if (alvo.isBlank()) return null
        return catalogoMagias
            .sortedBy { magia ->
                val nome = normalizarComandoVoz(magia.nome)
                when {
                    nome == alvo -> 0
                    nome.startsWith(alvo) -> 1
                    nome.contains(alvo) -> 2
                    alvo.contains(nome) -> 3
                    else -> 50 + kotlin.math.abs(nome.length - alvo.length)
                }
            }
            .firstOrNull { magia ->
                val nome = normalizarComandoVoz(magia.nome)
                nome == alvo || nome.contains(alvo) || alvo.contains(nome)
            }
    }
    fun processarComandoVoz(comandoRaw: String) {
        val comando = normalizarComandoVoz(comandoRaw)
        if (!MODO_ALVO_HABILITADO) {
            falarAjuda("Modo Alvo está desativado temporariamente.")
            return
        }
        when {
            comando.startsWith("quero ") || comando.startsWith("objetivo ") -> {
                val termo = comando.removePrefix("quero ").removePrefix("objetivo ").trim()
                val magia = encontrarMagiaPorComando(termo)
                if (magia == null) {
                    falarAjuda("Não encontrei a magia solicitada.")
                    return
                }
                modoAlvoAtivo = true
                magiaAlvoId = magia.id
                viewModel.atualizarBuscaMagia("")
                val falha = viewModel.prereqFailureForMagia(magia)
                val resposta = buildString {
                    append("Alvo definido: ${magia.nome}. ")
                    if (falha.isNullOrBlank()) append("Pré requisitos atendidos. ")
                    else append("Falta: ${formatarFalhaPreReq(falha)}. ")
                    append("Calculando trilha recomendada.")
                }
                falarAjuda(resposta)
            }
            comando.contains("proxima") || comando.contains("próxima") -> {
                if (proximaSugerida != null) {
                    falarAjuda("Próxima recomendada: ${proximaSugerida.nome}.")
                } else {
                    falarAjuda("Não há recomendação imediata.")
                }
            }
            comando.contains("adicionar sugerida") || comando == "adicionar" -> {
                val alvo = proximaSugerida
                if (alvo == null) {
                    falarAjuda("Não existe magia sugerida para adicionar agora.")
                    return
                }
                val erro = viewModel.adicionarMagia(definicao = alvo, pontosGastos = 1)
                if (erro == null) {
                    falarAjuda("Magia ${alvo.nome} adicionada.")
                } else {
                    falarAjuda("Não foi possível adicionar ${alvo.nome}. ${formatarFalhaPreReq(erro)}")
                }
            }
            comando.contains("por que bloqueada") || comando.contains("porque bloqueada") || comando.contains("faltando") -> {
                val alvo = magiaAlvoSelecionada
                if (alvo == null) {
                    falarAjuda("Defina uma magia alvo primeiro.")
                    return
                }
                val falha = viewModel.prereqFailureForMagia(alvo)
                if (falha.isNullOrBlank()) falarAjuda("${alvo.nome} está liberada.")
                else falarAjuda("Para ${alvo.nome} falta ${formatarFalhaPreReq(falha)}.")
            }
            else -> {
                falarAjuda("Comandos: quero nome da magia, próxima, adicionar sugerida, por que bloqueada.")
            }
        }
    }
    val reconhecimentoVozLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val texto = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
            ?.trim()
        if (texto.isNullOrBlank()) {
            falarAjuda("Não consegui entender o comando.")
        } else {
            processarComandoVoz(texto)
        }
    }
    val permissaoAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            falarAjuda("Permissão de microfone não concedida.")
            return@rememberLauncherForActivityResult
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Fale o comando de magia")
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }
        reconhecimentoVozLauncher.launch(intent)
    }
    fun iniciarAjudaVoz() {
        val possuiPermissao = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (!possuiPermissao) {
            permissaoAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Fale o comando de magia")
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }
        reconhecimentoVozLauncher.launch(intent)
    }

    FullscreenDialogContainer(onDismiss = onDismiss) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text("Selecionar Magia", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.buscaMagia,
                    onValueChange = { viewModel.atualizarBuscaMagia(it) },
                    label = { Text("Buscar...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (MODO_ALVO_HABILITADO) {
                        FilterChip(
                            selected = modoAlvoAtivo,
                            onClick = {
                                modoAlvoAtivo = !modoAlvoAtivo
                                if (!modoAlvoAtivo) magiaAlvoId = null
                                else viewModel.atualizarBuscaMagia("")
                            },
                            label = { Text("Modo Alvo") }
                        )
                        if (modoAlvoAtivoEfetivo && magiaAlvoSelecionada != null) {
                            Text(
                                "Alvo: ${magiaAlvoSelecionada.nome}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else if (modoAlvoAtivoEfetivo) {
                            Text(
                                "Toque em \"Alvo\" na magia desejada.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (ajudaVozAtiva) {
                        TextButton(
                            onClick = { iniciarAjudaVoz() },
                            modifier = Modifier.semantics {
                                contentDescription = "Ajuda por voz. Diga: quero nome da magia, próxima, adicionar sugerida, por que bloqueada."
                            }
                        ) {
                            Text("Ajuda por Voz")
                        }
                    }
                }
                statusAjudaVoz?.let { msg ->
                    if (ajudaVozAtiva) {
                        Text(
                            msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.semantics {
                                contentDescription = "Resposta da ajuda por voz: $msg"
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                // Filtro por Classe
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = viewModel.filtroClasseMagia == null,
                            onClick = { viewModel.atualizarFiltroClasseMagia(null) },
                            label = { Text("Todas classes") }
                        )
                    }
                    items(classes) { classe ->
                        FilterChip(
                            selected = viewModel.filtroClasseMagia == classe,
                            onClick = { viewModel.atualizarFiltroClasseMagia(classe) },
                            label = { Text(classe) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Filtro por Escola
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = viewModel.filtroEscolaMagia == null,
                            onClick = { viewModel.atualizarFiltroEscolaMagia(null) },
                            label = { Text("Todos") }
                        )
                    }
                    items(escolas) { escola ->
                        FilterChip(
                            selected = viewModel.filtroEscolaMagia == escola,
                            onClick = { viewModel.atualizarFiltroEscolaMagia(escola) },
                            label = { Text(escola) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                val textoContagem = if (modoAlvoAtivoEfetivo && magiaAlvoSelecionada != null) {
                    "${(listaExibicao.size - 1).coerceAtLeast(0)} opções imediatas"
                } else {
                    "${listaExibicao.size} magias encontradas"
                }
                Text(textoContagem, style = MaterialTheme.typography.bodySmall)
                if (modoAlvoAtivoEfetivo && magiaAlvoSelecionada != null) {
                    if (viewModel.modoAlvoCarregando) {
                        Text(
                            "Calculando trilha do alvo...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (!viewModel.modoAlvoErro.isNullOrBlank()) {
                        Text(
                            viewModel.modoAlvoErro.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else if (!viewModel.modoAlvoAviso.isNullOrBlank()) {
                        Text(
                            viewModel.modoAlvoAviso.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    val textoGuia = proximaSugerida?.let { "Próxima recomendada: ${it.nome}" }
                        ?: "Sem recomendação imediata. Verifique magias básicas liberadas."
                    Text(
                        textoGuia,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics {
                            if (isPraCegoVariant) {
                                contentDescription = "Guia do modo alvo. $textoGuia"
                            }
                        }
                    )
                    if (viewModel.modoAlvoProximasAcoes.isNotEmpty()) {
                        Text(
                            "3 próximas ações: ${viewModel.modoAlvoProximasAcoes.take(3).joinToString(" | ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (viewModel.modoAlvoChavesFaltantes.isNotEmpty()) {
                        Text(
                            "Chaves faltantes: ${viewModel.modoAlvoChavesFaltantes.take(3).joinToString(" | ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(listaExibicao) { indice, definicao ->
                        val jaAdicionada = viewModel.magiaJaAdicionada(definicao.id)
                        val prereqFalha = viewModel.prereqFailureForMagia(definicao)
                        val prereqOk = prereqFalha == null
                        val recomendada = proximaSugerida?.id == definicao.id
                        
                        // Formatando Classe e Escola
                        val classeEscola = listOfNotNull(
                            "IQ/${definicao.dificuldadeFixa ?: "D"}",
                            definicao.classe?.takeIf { it.isNotBlank() },
                            definicao.escola?.joinToString(" · ")?.takeIf { it.isNotBlank() }
                        ).joinToString(" · ")

                        ListItem(
                            headlineContent = {
                                Text(
                                    definicao.nome
                                )
                            },
                            supportingContent = {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("$classeEscola | pag. ${definicao.pagina}")
                                    if (!prereqOk && !prereqFalha.isNullOrBlank()) {
                                        Text(
                                            "Falta: ${motivoBloqueioCurto(prereqFalha)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            },
                            trailingContent = {
                                Column(horizontalAlignment = Alignment.End) {
                                    if (modoAlvoAtivoEfetivo && !jaAdicionada) {
                                        TextButton(
                                            onClick = {
                                                magiaAlvoId = definicao.id
                                                viewModel.atualizarBuscaMagia("")
                                            },
                                            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                                            modifier = Modifier.semantics {
                                                if (isPraCegoVariant) {
                                                    contentDescription = "Definir alvo para magia ${definicao.nome}"
                                                }
                                            }
                                        ) {
                                            Text(
                                                if (magiaAlvoId == definicao.id) "Alvo" else "Definir Alvo",
                                                color = if (magiaAlvoId == definicao.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (jaAdicionada) {
                                        Text("Adicionada", color = MaterialTheme.colorScheme.outline)
                                    } else if (!prereqOk) {
                                        Text("Bloqueada", color = MaterialTheme.colorScheme.error)
                                    } else if (recomendada) {
                                        Text("Recomendada", color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            },
                            modifier = Modifier
                                .semantics {
                                    if (isPraCegoVariant) {
                                        contentDescription = if (prereqOk) {
                                            if (recomendada) {
                                                "Posição ${indice + 1}. Magia ${definicao.nome}. Recomendação atual para avançar no alvo. Pré requisitos atendidos. Toque no nome para configurar."
                                            } else {
                                                "Posição ${indice + 1}. Magia ${definicao.nome}. Pre requisitos atendidos. Toque no nome para configurar."
                                            }
                                        } else {
                                            "Posição ${indice + 1}. Magia ${definicao.nome}. Pre requisitos nao atendidos: ${prereqFalha ?: "nao informado"}. Abra o dialogo e use adicao forcada se autorizado."
                                        }
                                    }
                                }
                                .then(if (!jaAdicionada) Modifier.clickable {
                                    erroAdicionarMagia = null
                                    magiaSelecionada = definicao
                                } else Modifier)
                                .then(
                                    if (!prereqOk) Modifier.alpha(0.45f) else Modifier
                                )
                        )
                        Divider()
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Fechar") }
                }
            }
    }

    magiaSelecionada?.let { definicao ->
        val prereqFalha = viewModel.prereqFailureForMagia(definicao)
        ConfigurarMagiaDialog(
            definicao = definicao,
            personagem = viewModel.personagem,
            nivelAptidaoMagica = viewModel.nivelAptidaoMagica,
            prereqFalha = prereqFalha,
            erroPersistente = erroAdicionarMagia,
            onDismiss = { magiaSelecionada = null },
            onSave = { pontosGastos, encantamentoAlvo, especializacaoMagia, ignorarPreReq ->
                val erro = viewModel.adicionarMagia(
                    definicao = definicao,
                    pontosGastos = pontosGastos,
                    encantamentoAlvo = encantamentoAlvo,
                    especializacaoMagia = especializacaoMagia,
                    ignorarPreRequisito = ignorarPreReq
                )
                if (erro == null) {
                    erroAdicionarMagia = null
                    magiaSelecionada = null
                } else {
                    erroAdicionarMagia = erro
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurarMagiaDialog(
    definicao: MagiaDefinicao,
    personagem: Personagem,
    nivelAptidaoMagica: Int,
    prereqFalha: String?,
    erroPersistente: String?,
    onDismiss: () -> Unit,
    onSave: (Int, String?, String?, Boolean) -> Unit
) {
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    val opcoesSubEscolaAnimais = listOf("Criaturas da Terra", "Criaturas do Ar", "Criaturas do Mar")
    var pontosGastos by remember { mutableStateOf(1) }
    var encantamentoAlvoInput by remember { mutableStateOf("") }
    var especializacaoMagiaInput by remember { mutableStateOf("") }
    var subEscolaAnimaisExpandida by remember { mutableStateOf(false) }
    var adicaoForcadaSemPrereq by remember { mutableStateOf(false) }
    var confirmarAdicaoForcada by remember { mutableStateOf(false) }
    val exigeEncantamentoAlvo = definicao.id.equals("imunidade_a_encantamento", ignoreCase = true)
    val exigeSubEscolaAnimais = definicao.id.equals("controle_de_animal", ignoreCase = true)
    val exigeEspecializacao = definicao.id.lowercase() in setOf(
        "adivinhacao",
        "cavalgar",
        "controle_de_hibrido",
        "golem",
        "passageiro_interno",
        "criar_elemental",
        "convocar_elemental",
        "controle_de_elemental"
    )
    val labelEspecializacao = when {
        exigeSubEscolaAnimais -> "Sub-escola de Animais"
        else -> when (definicao.id.lowercase()) {
        "cavalgar", "controle_de_hibrido", "passageiro_interno" -> "Animal (especializacao)"
        "criar_elemental", "convocar_elemental", "controle_de_elemental" -> "Escola (Ar/Fogo/Terra/Agua)"
        else -> "Especializacao"
        }
    }
    val dificuldade = Dificuldade.fromSigla(definicao.dificuldadeFixa ?: "D")
    
    // Calcula nível preview
    val previewMagia = MagiaSelecionada(definicao.id, definicao.nome, dificuldade, pontosGastos, definicao.pagina, definicao.texto, definicao.classe, definicao.escola)
    val nivelPreview = previewMagia.calcularNivel(personagem, nivelAptidaoMagica)
    val nivelRelativo = previewMagia.getNivelRelativo(personagem, nivelAptidaoMagica)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar: ${definicao.nome}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Dificuldade: ${dificuldade.nomeCompleto}", style = MaterialTheme.typography.bodyMedium)

                Divider()
                Text("Pontos Gastos:", style = MaterialTheme.typography.labelMedium)
                if (!isPraCegoVariant) {
                    Text(
                        "$pontosGastos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .width(56.dp)
                            .pointerInput(pontosGastos) {
                                var dragAcumulado = 0f
                                val passoPx = 24f
                                detectVerticalDragGestures(
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        dragAcumulado += dragAmount
                                        while (abs(dragAcumulado) >= passoPx) {
                                            pontosGastos = ajustarPontosPreset(
                                                atual = pontosGastos,
                                                incrementar = dragAcumulado < 0f
                                            )
                                            dragAcumulado += if (dragAcumulado < 0f) passoPx else -passoPx
                                        }
                                    }
                                )
                            },
                        textAlign = TextAlign.Center
                    )
                }
                if (isPraCegoVariant) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        TextButton(
                            onClick = { pontosGastos = ajustarPontosPreset(pontosGastos, incrementar = false) },
                            modifier = Modifier.semantics { contentDescription = "Diminuir pontos gastos da magia" }
                        ) { Text("-") }
                        Text(
                            "$pontosGastos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(horizontal = 8.dp)
                                .semantics { contentDescription = "Pontos gastos atuais da magia: $pontosGastos" }
                        )
                        TextButton(
                            onClick = { pontosGastos = ajustarPontosPreset(pontosGastos, incrementar = true) },
                            modifier = Modifier.semantics { contentDescription = "Aumentar pontos gastos da magia" }
                        ) { Text("+") }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    PONTOS_PRESETS.forEach { pts ->
                        TextButton(
                            onClick = { pontosGastos = pts },
                            modifier = Modifier.padding(horizontal = 1.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("$pts", fontSize = 12.sp)
                        }
                    }
                }

                Divider()
                val erroPreReq = erroPersistente ?: prereqFalha?.let {
                    "Pre-requisito nao atendido: ${formatarFalhaPreReq(it)}"
                }
                if (!erroPreReq.isNullOrBlank()) {
                    Text(
                        text = erroPreReq,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (!prereqFalha.isNullOrBlank()) {
                    TextButton(
                        onClick = { confirmarAdicaoForcada = true },
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = if (adicaoForcadaSemPrereq) {
                                "Adição Forçada sem pré-requisito (ATIVADA)"
                            } else {
                                "Adição Forçada sem pré-requisito"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                if (exigeEncantamentoAlvo) {
                    OutlinedTextField(
                        value = encantamentoAlvoInput,
                        onValueChange = { encantamentoAlvoInput = it.take(80) },
                        label = { Text("Qual encantamento?") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (exigeSubEscolaAnimais) {
                    ExposedDropdownMenuBox(
                        expanded = subEscolaAnimaisExpandida,
                        onExpandedChange = { subEscolaAnimaisExpandida = !subEscolaAnimaisExpandida },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = especializacaoMagiaInput,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(labelEspecializacao) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = subEscolaAnimaisExpandida)
                            },
                            singleLine = true,
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = subEscolaAnimaisExpandida,
                            onDismissRequest = { subEscolaAnimaisExpandida = false }
                        ) {
                            opcoesSubEscolaAnimais.forEach { opcao ->
                                DropdownMenuItem(
                                    text = { Text(opcao) },
                                    onClick = {
                                        especializacaoMagiaInput = opcao
                                        subEscolaAnimaisExpandida = false
                                    }
                                )
                            }
                        }
                    }
                } else if (exigeEspecializacao) {
                    OutlinedTextField(
                        value = especializacaoMagiaInput,
                        onValueChange = { especializacaoMagiaInput = it.take(80) },
                        label = { Text(labelEspecializacao) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Card(colors = appCardColors()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "NH: $nivelPreview (IQ+AM$nivelRelativo)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(pontosGastos, encantamentoAlvoInput, especializacaoMagiaInput, adicaoForcadaSemPrereq) },
                enabled = (prereqFalha.isNullOrBlank() || adicaoForcadaSemPrereq) &&
                    (!exigeEncantamentoAlvo || encantamentoAlvoInput.isNotBlank()) &&
                    (!(exigeEspecializacao || exigeSubEscolaAnimais) || especializacaoMagiaInput.isNotBlank())
            ) { Text("Adicionar") }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text("Cancelar") }
        }
    )

    if (confirmarAdicaoForcada) {
        AlertDialog(
            onDismissRequest = { confirmarAdicaoForcada = false },
            title = { Text("CONFIRMAÇÃO") },
            text = {
                Text(
                    "SEU MESTRE AUTORIZOU?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    adicaoForcadaSemPrereq = true
                    confirmarAdicaoForcada = false
                }) { Text("SIM") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarAdicaoForcada = false }) { Text("NAO") }
            }
        )
    }
}


@Composable
fun EditarMagiaDialog(
    magia: MagiaSelecionada,
    personagem: Personagem,
    nivelAptidaoMagica: Int,
    onDismiss: () -> Unit,
    onSave: (MagiaSelecionada) -> Unit
) {
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    var pontosGastos by remember { mutableStateOf(magia.pontosGastos) }
    
    // Calcula nível preview
    val previewMagia = magia.copy(pontosGastos = pontosGastos)
    val nivelPreview = previewMagia.calcularNivel(personagem, nivelAptidaoMagica)
    val nivelRelativo = previewMagia.getNivelRelativo(personagem, nivelAptidaoMagica)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar: ${magia.nome}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                val difNome = magia.dificuldade.sigla
                Text("IQ/$difNome", style = MaterialTheme.typography.bodyMedium)

                Divider()
                Text("Pontos Gastos:")
                if (!isPraCegoVariant) {
                    Text(
                        "$pontosGastos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .width(56.dp)
                            .pointerInput(pontosGastos) {
                                var dragAcumulado = 0f
                                val passoPx = 24f
                                detectVerticalDragGestures(
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        dragAcumulado += dragAmount
                                        while (abs(dragAcumulado) >= passoPx) {
                                            pontosGastos = ajustarPontosPreset(
                                                atual = pontosGastos,
                                                incrementar = dragAcumulado < 0f
                                            )
                                            dragAcumulado += if (dragAcumulado < 0f) passoPx else -passoPx
                                        }
                                    }
                                )
                            },
                        textAlign = TextAlign.Center
                    )
                }
                if (isPraCegoVariant) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        TextButton(
                            onClick = { pontosGastos = ajustarPontosPreset(pontosGastos, incrementar = false) },
                            modifier = Modifier.semantics { contentDescription = "Diminuir pontos gastos da magia" }
                        ) { Text("-") }
                        Text(
                            "$pontosGastos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(horizontal = 8.dp)
                                .semantics { contentDescription = "Pontos gastos atuais da magia: $pontosGastos" }
                        )
                        TextButton(
                            onClick = { pontosGastos = ajustarPontosPreset(pontosGastos, incrementar = true) },
                            modifier = Modifier.semantics { contentDescription = "Aumentar pontos gastos da magia" }
                        ) { Text("+") }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    PONTOS_PRESETS.forEach { pts ->
                        TextButton(
                            onClick = { pontosGastos = pts },
                            modifier = Modifier.padding(horizontal = 1.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("$pts", fontSize = 12.sp)
                        }
                    }
                }

                Divider()
                Card(colors = appCardColors()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "NH: $nivelPreview (IQ+AM$nivelRelativo)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(magia.copy(pontosGastos = pontosGastos)) }) {
                Text("Salvar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// === DIALOGS SIMPLES ===


