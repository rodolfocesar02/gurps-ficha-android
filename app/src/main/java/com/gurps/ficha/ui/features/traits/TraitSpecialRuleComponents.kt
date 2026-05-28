package com.gurps.ficha.ui.features.traits

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gurps.ficha.BuildConfig
import com.gurps.ficha.model.ModificadorSelecao
import com.gurps.ficha.model.ModificadorDefinicao
import kotlin.math.abs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DependenciaConfig(
    rarity: Int,
    frequency: Float,
    isIllegal: Boolean,
    onChanged: (Int, Float, Boolean) -> Unit
) {
    Column {
        Text("Raridade da Substância:", style = MaterialTheme.typography.labelMedium)
        listOf(-5 to "Comum (-5 pts)", -10 to "Incomum (-10 pts)", -20 to "Rara (-20 pts)", -30 to "Muito Rara (-30 pts)").forEach { (pts, label) ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onChanged(pts, frequency, isIllegal) }) {
                RadioButton(selected = rarity == pts, onClick = { onChanged(pts, frequency, isIllegal) })
                Text(label)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Tempo sem Substância:", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(1.0f to "Diário (x1)", 2.0f to "Hora em Hora (x2)", 3.0f to "Minuto em Minuto (x3)").forEach { (m, label) ->
                FilterChip(selected = frequency == m, onClick = { onChanged(rarity, m, isIllegal) }, label = { Text(label) })
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isIllegal, onCheckedChange = { onChanged(rarity, frequency, it) })
            Text("Substância Ilegal (-5 pts)")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InimigosConfig(
    basePower: Int,
    intention: Float,
    frequency: Float,
    isEnemy: Boolean,
    onChanged: (Int, Float, Float) -> Unit
) {
    Column {
        Text(if (isEnemy) "Poder do Inimigo:" else "Poder do Dependente:", style = MaterialTheme.typography.labelMedium)
        val inimOpcoes = if (isEnemy) listOf(-5 to "Individual (50% pts)", -10 to "Indiv. (100%) / Grupo (3-5)", -20 to "Indiv. (150%) / Grupo (6-20)", -30 to "Grupo (21-1000)", -40 to "Governo")
                         else listOf(-2 to "Indiv. (25% pts)", -5 to "Indiv. (50%)", -10 to "Indiv. (75%)", -15 to "Indiv. (100% ou mais)")
        inimOpcoes.forEach { (pts, label) ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onChanged(pts, intention, frequency) }) {
                RadioButton(selected = basePower == pts, onClick = { onChanged(pts, intention, frequency) })
                Text(label, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Intenção/Vizinhança:", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(0.25f to "Obs./Viz. (x1/4)", 0.5f to "Rival (x1/2)", 1.0f to "Perseguidor (x1)").forEach { (m, label) ->
                FilterChip(selected = intention == m, onClick = { onChanged(basePower, m, frequency) }, label = { Text(label, fontSize = 10.sp) })
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Frequência:", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(0.5f to "6- (x1/2)", 1.0f to "9- (x1)", 2.0f to "12- (x2)", 3.0f to "15- (x3)", 4.0f to "Const. (x4)").forEach { (m, label) ->
                FilterChip(selected = frequency == m, onClick = { onChanged(basePower, intention, m) }, label = { Text(label, fontSize = 10.sp) })
            }
        }
    }
}

@Composable
fun VicioConfig(
    base: Int,
    effect: Int,
    legality: Int,
    onChanged: (Int, Int, Int) -> Unit
) {
    Column {
        Text("Custo Base do Vício:", style = MaterialTheme.typography.labelMedium)
        listOf(-5 to "Barato (-5 pts)", -10 to "Caro (-10 pts)", -20 to "Muito Caro (-20 pts)").forEach { (pts, label) ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onChanged(pts, effect, legality) }) {
                RadioButton(selected = base == pts, onClick = { onChanged(pts, effect, legality) })
                Text(label, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Efeito Extra:", style = MaterialTheme.typography.labelMedium)
        listOf(0 to "Estimulante/Mínimo (+0 pts)", -5 to "Incidência (x1.5 ou -5 pts)", -10 to "Efeito Grave (-10 pts)").forEach { (pts, label) ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onChanged(base, pts, legality) }) {
                RadioButton(selected = effect == pts, onClick = { onChanged(base, pts, legality) })
                Text(label, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Legalidade:", style = MaterialTheme.typography.labelMedium)
        listOf(0 to "Legal (+0 pts)", -5 to "Ilegal (-5 pts)").forEach { (pts, label) ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onChanged(base, effect, pts) }) {
                RadioButton(selected = legality == pts, onClick = { onChanged(base, effect, pts) })
                Text(label, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReputacaoConfig(
    base: Int,
    group: Float,
    onChanged: (Int, Float) -> Unit
) {
    Column {
        Text("Tipo de Reputação:", style = MaterialTheme.typography.labelMedium)
        listOf(-5 to "Ruim (-5 pts)", -10 to "Muito Ruim (-10 pts)", -15 to "Péssima (-15 pts)").forEach { (pts, label) ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onChanged(pts, group) }) {
                RadioButton(selected = base == pts, onClick = { onChanged(pts, group) })
                Text(label)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Grupo que Reconhece:", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(1f / 3f to "Pequeno (x1/3)", 0.5f to "Médio (x1/2)", 1.0f to "Grande (x1)").forEach { (m, label) ->
                FilterChip(selected = kotlin.math.abs(group - m) < 0.02f, onClick = { onChanged(base, m) }, label = { Text(label) })
            }
        }
    }
}

@Composable
fun DeverConfig(
    base: Int,   
    isHazardous: Boolean,
    isInvoluntary: Boolean,
    onChanged: (Int, Boolean, Boolean) -> Unit
) {
    Column {
        Text("Custo Base (Perigo):", style = MaterialTheme.typography.labelMedium)
        listOf(-5 to "Mínimo (-5 pts)", -10 to "Moderado (-10 pts)", -15 to "Extremo (-15 pts)").forEach { (pts, label) ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onChanged(pts, isHazardous, isInvoluntary) }) {
                RadioButton(selected = base == pts, onClick = { onChanged(pts, isHazardous, isInvoluntary) })
                Text(label)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = isHazardous, onCheckedChange = { onChanged(base, it, isInvoluntary) }); Text("Perigoso (+5 pts)") }
        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = isInvoluntary, onCheckedChange = { onChanged(base, isHazardous, it) }); Text("Involuntário (+5 pts)") }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManutencaoConfig(
    base: Int,
    interval: Float,
    onChanged: (Int, Float) -> Unit
) {
    Column {
        Text("Custo Base (Pessoas):", style = MaterialTheme.typography.labelMedium)
        listOf(-2 to "1 pessoa (-2 pts)", -5 to "2-5 pessoas (-5 pts)", -10 to "6-10 pessoas (-10 pts)").forEach { (pts, label) ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onChanged(pts, interval) }) {
                RadioButton(selected = base == pts, onClick = { onChanged(pts, interval) })
                Text(label)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Intervalo:", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(1.0f to "Semanal (x1)", 0.5f to "Quinzenal (x1/2)", 0.25f to "Mensal (x1/4)").forEach { (m, label) ->
                FilterChip(selected = interval == m, onClick = { onChanged(base, m) }, label = { Text(label) })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DorCronicaConfig(
    intensity: Int,
    frequency: Float,
    onChanged: (Int, Float) -> Unit
) {
    Column {
        Text("Intensidade da Dor:", style = MaterialTheme.typography.labelMedium)
        listOf(-5 to "Leve (-5 pts)", -10 to "Moderada (-10 pts)", -15 to "Grave (-15 pts)", -20 to "Agonizante (-20 pts)").forEach { (pts, label) ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onChanged(pts, frequency) }) {
                RadioButton(selected = intensity == pts, onClick = { onChanged(pts, frequency) })
                Text(label)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Frequência:", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(0.5f to "6- (x1/2)", 1.0f to "9- (x1)", 2.0f to "12- (x2)", 3.0f to "15- (x3)").forEach { (m, label) ->
                FilterChip(selected = frequency == m, onClick = { onChanged(intensity, m) }, label = { Text(label) })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResistenteConfig(
    currentRarity: Int,
    currentDegree: Float,
    currentAttr: String,
    onChanged: (Int, Float, String) -> Unit
) {
    Column {
        Text("Raridade da Categoria:", style = MaterialTheme.typography.labelMedium)
        listOf(30 to "Muito Comum (30 pts)", 15 to "Comum (15 pts)", 10 to "Ocasional (10 pts)", 5 to "Rara (5 pts)").forEach { (pts, label) ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onChanged(pts, currentDegree, currentAttr) }) {
                RadioButton(selected = currentRarity == pts, onClick = { onChanged(pts, currentDegree, currentAttr) })
                Text(label, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Grau de Resistência:", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(1.0f to "Imunidade (x1)", 0.5f to "+8 no teste (x1/2)", 1f / 3f to "+3 no teste (x1/3)").forEach { (m, label) ->
                // Tolerância: ficha salva com 0.33f não é == a 1f/3f (0.3333).
                FilterChip(selected = kotlin.math.abs(currentDegree - m) < 0.02f, onClick = { onChanged(currentRarity, m, currentAttr) }, label = { Text(label, fontSize = 10.sp) })
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Atributo Base:", style = MaterialTheme.typography.labelMedium)
        Row {
            FilterChip(selected = currentAttr == "HT", onClick = { onChanged(currentRarity, currentDegree, "HT") }, label = { Text("Físico (HT)") })
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(selected = currentAttr == "IQ", onClick = { onChanged(currentRarity, currentDegree, "IQ") }, label = { Text("Mental (IQ/Von)") })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FraquezaConfig(
    rarity: Int,
    frequency: Float,
    onChanged: (Int, Float) -> Unit
) {
    Column {
        Text("Raridade:", style = MaterialTheme.typography.labelMedium)
        listOf(-1 to "Muito Comum (-1 pts)", -2 to "Comum (-2 pts)", -5 to "Incomum (-5 pts)", -10 to "Raro (-10 pts)").forEach { (pts, label) ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onChanged(pts, frequency) }) {
                RadioButton(selected = rarity == pts, onClick = { onChanged(pts, frequency) })
                Text(label)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Dano por Minuto:", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(1.0f to "1d/min (x1)", 2.0f to "1d/5s (x2)", 3.0f to "1d/s (x3)").forEach { (m, label) ->
                FilterChip(selected = frequency == m, onClick = { onChanged(rarity, m) }, label = { Text(label) })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VulnerabilidadeConfig(
    rarity: Int,
    dmgMult: Float,
    onChanged: (Int, Float) -> Unit
) {
    Column {
        Text("Raridade:", style = MaterialTheme.typography.labelMedium)
        listOf(-5 to "Muito Comum (-5 pts)", -10 to "Comum (-10 pts)", -15 to "Incomum (-15 pts)", -20 to "Raro (-20 pts)").forEach { (pts, label) ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onChanged(pts, dmgMult) }) {
                RadioButton(selected = rarity == pts, onClick = { onChanged(pts, dmgMult) })
                Text(label)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Multiplicador de Dano:", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(2.0f to "x2 (x2)", 3.0f to "x3 (x3)", 4.0f to "x4 (x4)").forEach { (m, label) ->
                FilterChip(selected = dmgMult == m, onClick = { onChanged(rarity, m) }, label = { Text(label) })
            }
        }
    }
}

@Composable
fun AtribulacaoConfig(modifiers: List<ModificadorSelecao>, onAddModifier: (ModificadorSelecao) -> Unit, descricaoContent: @Composable () -> Unit = {}) {
    var showCondList by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        descricaoContent()
        Button(onClick = { showCondList = true }, modifier = Modifier.fillMaxWidth()) { Text("Condições (Ampliação)") }
    }
    
    if (showCondList) {
        val repo = com.gurps.ficha.domain.rules.CharacterRules.DATA_REPOSITORY_INSTANCE
        val todasAmp = repo?.modificadoresGerais ?: emptyList()
        val ampCond = todasAmp.filter { it.id.startsWith("mod_condicao_") || it.id.startsWith("mod_vantagem_") || it.id.startsWith("mod_desvantagem_") }
        
        EscopoModificadoresDialog(
            especificos = ampCond,
            gerais = emptyList(),
            onDismiss = { showCondList = false },
            onSelect = { modDef ->
                val valorInt = Regex("-?\\d+").find(modDef.valor)?.value?.toIntOrNull() ?: 0
                onAddModifier(ModificadorSelecao(modDef.id, modDef.nome, valorInt, modDef.porNivel, 1, modDef.descricao, modDef.pagina, bonusBase = modDef.bonusBase))
                showCondList = false
            }
        )
    }
}

@Composable
fun RetencaoConfig(modifiers: List<ModificadorSelecao>, onAddModifier: (ModificadorSelecao) -> Unit, descricaoContent: @Composable () -> Unit = {}) {
    var showAmpList by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        descricaoContent()
        Button(onClick = { showAmpList = true }, modifier = Modifier.fillMaxWidth()) { Text("Ampliações de Retenção") }
    }
    
    if (showAmpList) {
        val repo = com.gurps.ficha.domain.rules.CharacterRules.DATA_REPOSITORY_INSTANCE
        val todasAmp = repo?.modificadoresGerais ?: emptyList()
        val ampIds = listOf("mod_engolfar", "mod_grudento", "mod_inquebravel")
        val ampRetencao = todasAmp.filter { it.id in ampIds || it.id.startsWith("mod_so_sofre_dano") }
        
        EscopoModificadoresDialog(
            especificos = ampRetencao,
            gerais = emptyList(),
            onDismiss = { showAmpList = false },
            onSelect = { modDef ->
                val valorInt = Regex("-?\\d+").find(modDef.valor)?.value?.toIntOrNull() ?: 0
                onAddModifier(ModificadorSelecao(modDef.id, modDef.nome, valorInt, modDef.porNivel, 1, modDef.descricao, modDef.pagina, bonusBase = modDef.bonusBase))
                showAmpList = false
            }
        )
    }
}

@Composable
fun AliadosConfig(
    currentRatio: Int,
    currentFreq: Float,
    currentGroup: Int,
    onChanged: (Int, Float, Int) -> Unit
) {
    var showRatioList by remember { mutableStateOf(false) }
    var showFreqList by remember { mutableStateOf(false) }
    var showGroupList by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Button(onClick = { showRatioList = true }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            val label = when(currentRatio) {
                1 -> "25% pts (1 pt)"
                2 -> "50% pts (2 pts)"
                3 -> "75% pts (3 pts)"
                5 -> "100% pts (5 pts)"
                10 -> "150% pts (10 pts)"
                else -> "Custom ($currentRatio pts)"
            }
            Text("Poder do Aliado: $label")
        }

        Button(onClick = { showFreqList = true }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            val label = when(currentFreq) {
                0.5f -> "6- (x1/2)"
                1.0f -> "9- (x1)"
                2.0f -> "12- (x2)"
                3.0f -> "15- (x3)"
                else -> "9- (x1)"
            }
            Text("Frequência: $label")
        }

        Button(onClick = { showGroupList = true }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            val label = when(currentGroup) {
                1 -> "Único (x1)"
                2 -> "Grupo 2-5 (x2)"
                6 -> "Grupo 6-10 (x6)"
                12 -> "Grupo 11-20 (x12)"
                else -> "Custom (x$currentGroup)"
            }
            Text("Tamanho do Grupo: $label")
        }
    }

    if (showRatioList) SeletorPoderAliadoDialog(currentRatio, onDismiss = { showRatioList = false }, onSelect = { onChanged(it, currentFreq, currentGroup); showRatioList = false })
    if (showFreqList) SeletorFrequenciaAparecimentoDialog(currentFreq, onDismiss = { showFreqList = false }, onSelect = { onChanged(currentRatio, it, currentGroup); showFreqList = false })
    if (showGroupList) SeletorGrupoAliadoDialog(currentGroup, onDismiss = { showGroupList = false }, onSelect = { onChanged(currentRatio, currentFreq, it); showGroupList = false })
}

@Composable
fun ContatosConfig(
    currentNh: Int,
    currentFreq: Float,
    currentConf: Float,
    onChanged: (Int, Float, Float) -> Unit
) {
    var showNhList by remember { mutableStateOf(false) }
    var showFreqList by remember { mutableStateOf(false) }
    var showConfList by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Button(onClick = { showNhList = true }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("Perícia (NH $currentNh)")
        }
        Button(onClick = { showFreqList = true }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            val label = when(currentFreq) {
                0.5f -> "6-"
                1f -> "9-"
                2f -> "12-"
                3f -> "15-"
                else -> "9-"
            }
            Text("Frequência ($label)")
        }
        Button(onClick = { showConfList = true }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("Confiabilidade")
        }
    }

    if (showNhList) SeletorNhContatoDialog(currentNh, onDismiss = { showNhList = false }, onSelect = { onChanged(it, currentFreq, currentConf); showNhList = false })
    if (showFreqList) SeletorFrequenciaAparecimentoDialog(currentFreq, onDismiss = { showFreqList = false }, onSelect = { onChanged(currentNh, it, currentConf); showFreqList = false })
    if (showConfList) SeletorConfiabilidadeDialog(currentConf, onDismiss = { showConfList = false }, onSelect = { onChanged(currentNh, currentFreq, it); showConfList = false })
}

@Composable
fun PatronosConfig(
    currentPower: Int,
    currentFreq: Float,
    currentMod: Float,
    isSecret: Boolean,
    onChanged: (Int, Float, Float, Boolean) -> Unit
) {
    var showPowerList by remember { mutableStateOf(false) }
    var showFreqList by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Button(onClick = { showPowerList = true }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("Poder do Patrono ($currentPower pts)")
        }
        Button(onClick = { showFreqList = true }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            val label = when(currentFreq) {
                0.5f -> "6-"
                1f -> "9-"
                2f -> "12-"
                3f -> "15-"
                else -> "9-"
            }
            Text("Frequência ($label)")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isSecret, onCheckedChange = { onChanged(currentPower, currentFreq, currentMod, it) })
            Text("Patrono Secreto")
        }
    }

    if (showPowerList) SeletorPoderPatronoDialog(currentPower, onDismiss = { showPowerList = false }, onSelect = { onChanged(it, currentFreq, currentMod, isSecret); showPowerList = false })
    if (showFreqList) SeletorFrequenciaAparecimentoDialog(currentFreq, onDismiss = { showFreqList = false }, onSelect = { onChanged(currentPower, it, currentMod, isSecret); showFreqList = false })
}

@Composable
fun FavorConfig(
    currentPower: Int,
    currentMod: Float,
    isSecret: Boolean,
    isContact: Boolean,
    onChanged: (Int, Float, Boolean, Boolean) -> Unit
) {
    var showPowerList by remember { mutableStateOf(false) }
    var showTypeList by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Button(onClick = { showPowerList = true }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("Poder Base ($currentPower pts)")
        }
        Button(onClick = { showTypeList = true }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text(if (isContact) "Como Contato (1/15)" else "Como Patrono (1/10)")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isSecret, onCheckedChange = { onChanged(currentPower, currentMod, it, isContact) })
            Text("Favor Secreto")
        }
    }

    if (showPowerList) SeletorPoderPatronoDialog(currentPower, onDismiss = { showPowerList = false }, onSelect = { onChanged(it, currentMod, isSecret, isContact); showPowerList = false })
    if (showTypeList) SeletorTipoFavorDialog(isContact, onDismiss = { showTypeList = false }, onSelect = { onChanged(currentPower, currentMod, isSecret, it); showTypeList = false })
}

@Composable
fun DentesConfig(currentType: String, onChanged: (String) -> Unit) {
    val options = listOf("rombo" to "Rombos (cont) - 0 pts", "bico_afiado" to "Bico Afiado (pa+) - 1 pt", "dentes_afiados" to "Dentes Afiados (cort) - 1 pt", "presas" to "Presas (perf) - 2 pts")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Tipo de Dentes:", style = MaterialTheme.typography.titleSmall)
        options.forEach { (id, label) ->
            Card(modifier = Modifier.fillMaxWidth().clickable { onChanged(id) }, colors = CardDefaults.cardColors(containerColor = if (currentType == id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), border = if (currentType == id) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = currentType == id, onClick = { onChanged(id) })
                    Spacer(modifier = Modifier.width(12.dp)); Text(label, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun GarrasConfig(currentType: String, onChanged: (String) -> Unit) {
    val options = listOf("cascos" to "Cascos (+1 dps, Cont) - 3 pts", "cegas" to "Garras Cegas (+1 dps, Cont) - 3 pts", "afiadas" to "Garras Afiadas (cort) - 5 pts", "pontudas" to "Garras Pontudas (cort/perf) - 8 pts", "longas_pontudas" to "Longas Garras Pontudas (+1 dps, cort/perf) - 11 pts")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Tipo de Garras:", style = MaterialTheme.typography.titleSmall)
        options.forEach { (id, label) ->
            Card(modifier = Modifier.fillMaxWidth().clickable { onChanged(id) }, colors = CardDefaults.cardColors(containerColor = if (currentType == id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), border = if (currentType == id) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = currentType == id, onClick = { onChanged(id) })
                    Spacer(modifier = Modifier.width(12.dp)); Text(label, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun TelecomunicacaoConfig(currentType: String, onChanged: (String) -> Unit) {
    val options = listOf(
        "laser" to "Comunicação a Laser - 15 pts",
        "diapsiquia" to "Diapsiquia (Transmissão Telepática) - 30 pts",
        "radio" to "Rádio - 10 pts"
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Tipo de Telecomunicação:", style = MaterialTheme.typography.titleSmall)
        options.forEach { (id, label) ->
            Card(modifier = Modifier.fillMaxWidth().clickable { onChanged(id) }, colors = CardDefaults.cardColors(containerColor = if (currentType == id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), border = if (currentType == id) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = currentType == id, onClick = { onChanged(id) })
                    Spacer(modifier = Modifier.width(12.dp)); Text(label, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun ApararAmpliadoConfig(currentType: String, currentSkill: String, onChanged: (String, String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Tipo de Bônus:", style = MaterialTheme.typography.titleSmall)
        Card(modifier = Modifier.fillMaxWidth().clickable { onChanged("global", currentSkill) }, colors = CardDefaults.cardColors(containerColor = if (currentType == "global") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = currentType == "global", onClick = { onChanged("global", currentSkill) })
                Spacer(modifier = Modifier.width(12.dp))
                Column { Text("Todas as Manobras Aparar", fontWeight = FontWeight.Bold); Text("Bônus +1 em todos os Aparar (10 pts)", style = MaterialTheme.typography.bodySmall) }
            }
        }
        Card(modifier = Modifier.fillMaxWidth().clickable { onChanged("especifica", currentSkill) }, colors = CardDefaults.cardColors(containerColor = if (currentType == "especifica") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = currentType == "especifica", onClick = { onChanged("especifica", currentSkill) })
                Spacer(modifier = Modifier.width(12.dp))
                Column { Text("Perícia Específica", fontWeight = FontWeight.Bold); Text("Bônus +1 em uma perícia (5 pts)", style = MaterialTheme.typography.bodySmall) }
            }
        }
        if (currentType == "especifica") OutlinedTextField(value = currentSkill, onValueChange = { onChanged("especifica", it) }, label = { Text("Nome da Perícia ou 'Desarmado'") }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun AtaqueInatoConfig(nome: String, tipoDano: String, dados: Int, bonus: Int, onChanged: (String, String, Int, Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = nome, onValueChange = { onChanged(it, tipoDano, dados, bonus) }, label = { Text("Nome do Ataque") }, modifier = Modifier.fillMaxWidth())
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = dados.toString(), onValueChange = { onChanged(nome, tipoDano, it.toIntOrNull() ?: 0, bonus) }, label = { Text("Dados") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = bonus.toString(), onValueChange = { onChanged(nome, tipoDano, dados, it.toIntOrNull() ?: 0) }, label = { Text("Bônus") }, modifier = Modifier.weight(1f))
        }
        Text("Tipo de Dano:", style = MaterialTheme.typography.labelMedium)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("cont", "cort", "perf", "pa-", "pa+", "pa++", "fad", "tox").forEach { t ->
                FilterChip(selected = tipoDano == t, onClick = { onChanged(nome, t, dados, bonus) }, label = { Text(t) })
            }
        }
    }
}

@Composable
fun GolpeadoresConfig(nome: String, tipoDano: String, dados: Int, bonus: Int, onChanged: (String, String, Int, Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = nome, onValueChange = { onChanged(it, tipoDano, dados, bonus) }, label = { Text("Nome do Golpeador") }, modifier = Modifier.fillMaxWidth())
        Text("Tipo de Dano:", style = MaterialTheme.typography.labelMedium)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("cont", "cort", "perf", "pa-", "pa+", "pa++").forEach { t ->
                FilterChip(selected = tipoDano == t, onClick = { onChanged(nome, t, dados, bonus) }, label = { Text(t) })
            }
        }
    }
}

// Representa a seleção de cada tipo dentro de Habilidades Modulares
data class HabModTipoSel(val ativo: Boolean, val niveis: Int)

data class HabModTipoInfo(val id: String, val nome: String, val formula: String, val descricao: String)

@Composable
private fun HabModTipoCard(
    tipo: HabModTipoInfo,
    sel: HabModTipoSel,
    isPracego: Boolean,
    onToggle: (HabModTipoSel) -> Unit,
    onDescricao: () -> Unit
) {
    // Estado interno isolado por card — evita contaminação entre recomposições do forEach
    var ativo by remember { mutableStateOf(sel.ativo) }
    var niveis by remember { mutableIntStateOf(sel.niveis) }

    // Sincroniza se o pai enviar um valor novo (ex: edição carregada)
    LaunchedEffect(sel.ativo, sel.niveis) {
        if (ativo != sel.ativo) ativo = sel.ativo
        if (niveis != sel.niveis) niveis = sel.niveis
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (ativo) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = ativo,
                onClick = {
                    ativo = !ativo
                    onToggle(HabModTipoSel(ativo = ativo, niveis = niveis.coerceAtLeast(1)))
                }
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(tipo.nome, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(
                    tipo.formula,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onDescricao() }
                )
            }
            if (ativo) {
                Spacer(modifier = Modifier.width(12.dp))
                if (isPracego) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = {
                            niveis = (niveis - 1).coerceAtLeast(1)
                            onToggle(HabModTipoSel(ativo = true, niveis = niveis))
                        }, enabled = niveis > 1) { Text("-") }
                        Text("$niveis", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
                        TextButton(onClick = {
                            niveis++
                            onToggle(HabModTipoSel(ativo = true, niveis = niveis))
                        }) { Text("+") }
                    }
                } else {
                    Text(
                        "$niveis",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.pointerInput(tipo.id, niveis) {
                            var dragAcumulado = 0f
                            val passoPx = 40f
                            var niveisAtual = niveis.coerceAtLeast(1)
                            detectVerticalDragGestures(
                                onVerticalDrag = { change, dragAmount ->
                                    change.consume()
                                    dragAcumulado += dragAmount
                                    if (abs(dragAcumulado) >= passoPx) {
                                        niveisAtual = if (dragAcumulado < 0f) niveisAtual + 1 else (niveisAtual - 1).coerceAtLeast(1)
                                        onToggle(HabModTipoSel(ativo = true, niveis = niveisAtual))
                                        dragAcumulado = 0f
                                    }
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HabilidadesModularesConfig(
    selecoes: Map<String, HabModTipoSel>,
    onChanged: (Map<String, HabModTipoSel>) -> Unit
) {
    val isPracego = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)

    // rememberUpdatedState garante que o onChanged e selecoes dentro das lambdas
    // sempre apontam para a versão mais recente, sem capturar valores stale
    val selecoesAtual by rememberUpdatedState(selecoes)
    val onChangedAtual by rememberUpdatedState(onChanged)

    val tipos = remember {
        listOf(
            HabModTipoInfo("cerebro_eletronico", "Cérebro Eletrônico", "6 + 4×níveis",
                "As habilidades são programas de computador. Com Telecomunicação, o personagem pode baixar programas de uma rede (1 segundo por ponto). Custo padrão: \$100 por ponto."),
            HabModTipoInfo("chips", "Entradas para Chips", "5 + 3×níveis",
                "Programas armazenados em chips físicos encaixados num soquete no crânio (3 segundos). Chips custam entre \$100 e \$1.000 por ponto."),
            HabModTipoInfo("poder_cosmico", "Poder Cósmico", "10×níveis",
                "O personagem simplesmente deseja e novas habilidades aparecem (1 segundo por habilidade). Uma única entrada, reorganiza todos os pontos em quantas habilidades desejar."),
            HabModTipoInfo("supermemorizar", "Supermemorização", "5 + 3×níveis",
                "Adquire habilidades por estudo rápido (1 segundo por ponto) com obra de referência. Pode \"esquecer\" instantaneamente.")
        )
    }

    var descricaoExpandida by remember { mutableStateOf<HabModTipoInfo?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tipos.forEach { tipo ->
            key(tipo.id) {
                HabModTipoCard(
                    tipo = tipo,
                    sel = selecoesAtual[tipo.id] ?: HabModTipoSel(false, 1),
                    isPracego = isPracego,
                    onToggle = { novoSel -> onChangedAtual(selecoesAtual + (tipo.id to novoSel)) },
                    onDescricao = { descricaoExpandida = tipo }
                )
            }
        }
    }

    descricaoExpandida?.let { tipo ->
        AlertDialog(
            onDismissRequest = { descricaoExpandida = null },
            title = { Text(tipo.nome) },
            text = { Text(tipo.descricao) },
            confirmButton = { TextButton(onClick = { descricaoExpandida = null }) { Text("Fechar") } }
        )
    }
}

@Composable
fun MestreDeArmasConfig(currentClass: String, currentSkills: String, weaponSuggestions: List<String> = emptyList(), onChanged: (String, String) -> Unit) {
    val classes = listOf("todas" to "Todas as Armas Motoras (45 pts)", "amp_classe" to "Classe Ampla (40 pts)", "int_classe" to "Classe Intermediária (35 pts)", "peq_classe" to "Classe Pequena (30 pts)", "set_two" to "Duas Armas (25 pts)", "single" to "Uma Arma (20 pts)")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Categoria:", style = MaterialTheme.typography.titleSmall)
        classes.forEach { (id, label) ->
            Card(modifier = Modifier.fillMaxWidth().clickable { onChanged(id, "") }, colors = CardDefaults.cardColors(containerColor = if (currentClass == id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = currentClass == id, onClick = { onChanged(id, "") })
                    Spacer(modifier = Modifier.width(8.dp)); Text(label, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        if (currentClass != "todas") OutlinedTextField(value = currentSkills, onValueChange = { onChanged(currentClass, it) }, label = { Text("Perícias/Armas") }, modifier = Modifier.fillMaxWidth())
    }
}

// --- Diálogos Auxiliares ---

@Composable
fun SeletorPoderAliadoDialog(current: Int, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    val opcoes = listOf(25 to 1, 50 to 2, 75 to 3, 100 to 5, 150 to 10)
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Poder do Aliado") }, text = {
        Column { opcoes.forEach { (pts, custo) -> ListItem(headlineContent = { Text("$pts% dos pts ($custo pts)") }, modifier = Modifier.clickable { onSelect(custo) }) } }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } })
}

@Composable
fun SeletorGrupoAliadoDialog(current: Int, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    val opcoes = listOf("Único | x1" to 1, "Grupo (2-5) | x2" to 2, "Grupo (6-10) | x6" to 6, "Grupo (11-20) | x12" to 12)
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Tamanho do Grupo") }, text = {
        Column { opcoes.forEach { (label, mult) -> ListItem(headlineContent = { Text(label) }, modifier = Modifier.clickable { onSelect(mult) }) } }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } })
}

@Composable
fun SeletorPoderPatronoDialog(current: Int, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    val opcoes = listOf(10 to "Menor", 15 to "Médio", 20 to "Poderoso", 25 to "Muito Poderoso")
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Poder do Patrono") }, text = {
        Column { opcoes.forEach { (pts, label) -> ListItem(headlineContent = { Text("$label ($pts pts)") }, modifier = Modifier.clickable { onSelect(pts) }) } }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } })
}

@Composable
fun SeletorTipoFavorDialog(isContact: Boolean, onDismiss: () -> Unit, onSelect: (Boolean) -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Tipo de Favor") }, text = {
        Column {
            ListItem(headlineContent = { Text("Como Contato") }, modifier = Modifier.clickable { onSelect(true) })
            ListItem(headlineContent = { Text("Como Patrono") }, modifier = Modifier.clickable { onSelect(false) })
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } })
}

@Composable
fun SeletorRaridadeResistenteDialog(current: Int, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    val opcoes = listOf(
        Triple(30, "Muito Comum", "Danos ao Metabolismo (doenças, venenos, síndromes como altitude, descompressão, enjoo marítimo, etc.)."),
        Triple(15, "Comum", "Venenos (toxinas, mas não asfixiantes/corrosivos) ou Enjoos (todos os tipos e síndromes ambientais)."),
        Triple(10, "Ocasional", "Doenças (infecções por bactérias, vírus, fungos, etc.) ou Venenos Ingeridos."),
        Triple(5, "Raro", "Aceleração (G elevado), Doença da Altitude, Descompressão, Enjoo Marítimo, Nanomáquinas.")
    )
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Raridade da Ameaça") }, text = {
        Column {
            opcoes.forEach { (pts, label, desc) ->
                ListItem(
                    headlineContent = { Text("$label ($pts pts)") },
                    supportingContent = { Text(desc, fontSize = 11.sp) },
                    modifier = Modifier.clickable { onSelect(pts) }
                )
            }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } })
}

@Composable
fun SeletorGrauResistenteDialog(current: Float, onDismiss: () -> Unit, onSelect: (Float) -> Unit) {
    val opcoes = listOf(
        Triple(1f, "Imunidade Total", "O personagem nunca precisa fazer testes de resistência: x1"),
        Triple(0.5f, "Bônus +8 nos testes", "O personagem recebe um bônus de +8 nos testes: x1/2"),
        Triple(0.3333f, "Bônus +3 nos testes", "O personagem recebe um bônus de +3 nos testes: x1/3")
    )
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Grau da Resistência") }, text = {
        Column {
            opcoes.forEach { (mult, label, desc) ->
                ListItem(
                    headlineContent = { Text("$label") },
                    supportingContent = { Text(desc, fontSize = 11.sp) },
                    modifier = Modifier.clickable { onSelect(mult) }
                )
            }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } })
}
