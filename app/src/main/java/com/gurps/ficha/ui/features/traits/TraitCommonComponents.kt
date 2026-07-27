package com.gurps.ficha.ui.features.traits

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gurps.ficha.model.ModificadorDefinicao
import com.gurps.ficha.model.ModificadorSelecao
import com.gurps.ficha.domain.rules.CharacterRules
import com.gurps.ficha.ui.UiActionLabels

// --- Classificação de modificador: ampliação x limitação ---

/**
 * Um modificador é ampliação (encarece a vantagem) ou limitação (barateia).
 * Os catálogos separam isso pelo campo `tipo`, mas ele não é confiável sozinho:
 * há um registro com "limitation" em inglês (mod_gatilho_30) e específicos de
 * vantagem futuros podem vir sem tipo. Por isso a classificação cai em cascata:
 *   1. `tipo` começando com "amp"/"lim" (cobre "ampliação"/"limitação"/"limitation");
 *   2. como rede, o SINAL do valor — limitação é sempre negativa no GURPS.
 * Assim nenhum item some dos dois botões por causa de dado torto.
 */
enum class TipoModificador { AMPLIACAO, LIMITACAO }

fun classificarModificador(mod: ModificadorDefinicao): TipoModificador {
    val t = mod.tipo.trim().lowercase()
    return when {
        t.startsWith("amp") -> TipoModificador.AMPLIACAO
        t.startsWith("lim") -> TipoModificador.LIMITACAO
        // Sem tipo utilizável: decide pelo sinal do primeiro número do valor.
        else -> {
            val n = Regex("-?\\d+").find(mod.valor)?.value?.toIntOrNull() ?: 0
            if (n < 0) TipoModificador.LIMITACAO else TipoModificador.AMPLIACAO
        }
    }
}

/**
 * Um modificador do catálogo geral só cabe no traço aberto se não pertencer a
 * outro. `donoId` nulo = modificador geral, serve a qualquer traço.
 *
 * Sem esse filtro o catálogo geral inteiro aparecia em toda vantagem: abrir
 * "Abafador de Mana" oferecia Guelras (só de Não Respira), Pele Resistente (só
 * de Resistência a Dano), Incapaz de Aparar (só de Golpeadores)...
 */
fun modificadorCabeEm(mod: ModificadorDefinicao, tracoId: String?): Boolean =
    mod.donoId.isNullOrBlank() || mod.donoId == tracoId

/**
 * Os dois botões da ficha — "Ampliações" e "Limitações" — no lugar dos antigos
 * "Modificadores (%)" e "Modificadores de Poder". A diferença: os antigos
 * separavam por ARQUIVO de origem (catálogo geral x poderes) e jogavam
 * ampliações e limitações misturadas na mesma lista. Estes separam pelo que o
 * modificador FAZ, juntando os dois arquivos e filtrando por tipo.
 *
 * @param especificos modificadores próprios da vantagem/desvantagem aberta.
 * @param gerais catálogo geral (modificadores.v1.json).
 * @param poderes catálogo de poderes (modificadores_poderes.v1.json).
 * @param tracoId id da vantagem/desvantagem aberta. Os modificadores do catálogo
 *   geral que pertencem a OUTRO traço são escondidos; os que pertencem a ESTE
 *   sobem para a seção "Específicos desta característica".
 * @param onEscolher chamado com o modificador escolhido pelo usuário.
 * @param onModAptidaoEscola caso especial: o modificador de Aptidão Mágica por
 *   escola abre um seletor de escola em vez de entrar direto. Null = trata como
 *   um modificador comum (usado nas desvantagens, onde ele não aparece).
 */
@Composable
fun BotoesModificadoresPorTipo(
    especificos: List<ModificadorDefinicao>,
    gerais: List<ModificadorDefinicao>,
    poderes: List<ModificadorDefinicao>,
    tracoId: String? = null,
    onEscolher: (ModificadorDefinicao) -> Unit,
    onModAptidaoEscola: ((ModificadorDefinicao) -> Unit)? = null
) {
    var tipoAberto by remember { mutableStateOf<TipoModificador?>(null) }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("Ampliações", style = MaterialTheme.typography.labelLarge)
        TextButton(onClick = { tipoAberto = TipoModificador.AMPLIACAO }) {
            Icon(androidx.compose.material.icons.Icons.Default.Add, null)
            Text("Add")
        }
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("Limitações", style = MaterialTheme.typography.labelLarge)
        TextButton(onClick = { tipoAberto = TipoModificador.LIMITACAO }) {
            Icon(androidx.compose.material.icons.Icons.Default.Add, null)
            Text("Add")
        }
    }

    tipoAberto?.let { tipo ->
        val filtro = { lista: List<ModificadorDefinicao> ->
            lista.filter { classificarModificador(it) == tipo && modificadorCabeEm(it, tracoId) }
        }
        // Os do catálogo geral que pertencem a ESTE traço são dele: sobem para a
        // seção "Específicos" em vez de ficarem perdidos no meio dos gerais.
        val dosDois = (especificos + gerais.filter { !it.donoId.isNullOrBlank() && it.donoId == tracoId })
            .distinctBy { it.id to it.valor }
        EscopoModificadoresDialog(
            especificos = filtro(dosDois),
            gerais = filtro(gerais).filter { it.donoId.isNullOrBlank() },
            poderes = filtro(poderes),
            titulo = if (tipo == TipoModificador.AMPLIACAO) "Adicionar Ampliação" else "Adicionar Limitação",
            onDismiss = { tipoAberto = null },
            onSelect = { modDef ->
                if (modDef.id == "mod_aptidao_escola" && onModAptidaoEscola != null) {
                    onModAptidaoEscola(modDef)
                } else {
                    onEscolher(modDef)
                }
                tipoAberto = null
            }
        )
    }
}

// --- Seletores Genéricos de Catálogo ---

@Composable
fun EscopoModificadoresDialog(especificos: List<ModificadorDefinicao>, gerais: List<ModificadorDefinicao>, poderes: List<ModificadorDefinicao> = emptyList(), titulo: String = "Adicionar Modificador", onDismiss: () -> Unit, onSelect: (ModificadorDefinicao) -> Unit) {
    var busca by remember { mutableStateOf("") }

    val especificosFiltrados = especificos.filter { it.nome.contains(busca, ignoreCase = true) }
    val especificosIds = especificos.map { it.id }.toSet()
    val geraisFiltrados = gerais.filter { it.id !in especificosIds && it.nome.contains(busca, ignoreCase = true) }
    val poderesFiltrados = poderes.filter { it.id !in especificosIds && it.nome.contains(busca, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = busca,
                    onValueChange = { busca = it },
                    label = { Text("Buscar...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = { if (busca.isNotEmpty()) IconButton(onClick = { busca = "" }, modifier = Modifier.semantics { contentDescription = "Limpar busca" }) { Icon(Icons.Default.Close, null) } }
                )
                
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(300.dp)) {
                    if (especificosFiltrados.isNotEmpty()) {
                        item { Text("Específicos desta característica", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
                        items(especificosFiltrados) { mod -> ModificadorItemRow(mod) { onSelect(mod) } }
                    }
                    if (geraisFiltrados.isNotEmpty()) {
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                        item { Text("Catálogo Geral", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
                        items(geraisFiltrados) { mod -> ModificadorItemRow(mod) { onSelect(mod) } }
                    }
                    if (poderesFiltrados.isNotEmpty()) {
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                        item { Text("Poderes", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
                        items(poderesFiltrados) { mod -> ModificadorItemRow(mod) { onSelect(mod) } }
                    }
                    if (especificosFiltrados.isEmpty() && geraisFiltrados.isEmpty() && poderesFiltrados.isEmpty()) {
                        item { 
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("Nenhum modificador encontrado", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun SeletorEscolaMagiaDialog(onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    val escolas = listOf("Ar", "Água", "Alimento", "Animais", "Arremesso", "Comunicação e Empatia", "Controle da Mente", "Corpo", "Cura", "Deslocamento", "Encantamentos", "Esculpir", "Fogo", "Ilusão e Criação", "Impregnação Mágica", "Inviabilização", "Luz e Trevas", "Meta-Mágicas", "Necromancia", "Planta", "Proteção e Advertência", "Som", "Tecnomagia", "Terra").sorted()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecione a Escola") },
        text = {
            LazyColumn(modifier = Modifier.height(300.dp)) {
                items(escolas) { escola ->
                    TextButton(onClick = { onSelect(escola) }, modifier = Modifier.fillMaxWidth()) {
                        Text(escola, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// --- Componentes de Item ---

@Composable
fun ModificadorItemRow(mod: ModificadorDefinicao, onClick: () -> Unit) {
    var mostrarDescricao by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(mod.nome, fontWeight = FontWeight.Bold)
                Text("${mod.tipo} | ${mod.valor}" + (if (mod.porNivel) " p/ nível" else ""), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            if (mod.descricao?.isNotBlank() == true) {
                IconButton(onClick = { mostrarDescricao = true }, modifier = Modifier.semantics { contentDescription = "Ver descrição de ${mod.nome}" }) { Icon(Icons.Default.Info, null) }
            }
        }
    }
    if (mostrarDescricao) {
        CatalogoDescricaoDialog(nome = mod.nome, descricao = mod.descricao ?: "", onDismiss = { mostrarDescricao = false })
    }
}

@Composable
fun ModificadorSelecionadoItem(mod: ModificadorSelecao, onUpdate: (ModificadorSelecao) -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(mod.nome, fontWeight = FontWeight.Bold)
                    val totalMod = mod.bonusBase + if (mod.porNivel) mod.valor * mod.niveis else mod.valor
                    Text("${if (totalMod >= 0) "+" else ""}${totalMod}%", style = MaterialTheme.typography.bodySmall)
                }
                if (mod.porNivel) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { if (mod.niveis > 1) onUpdate(mod.copy(niveis = mod.niveis - 1)) }) { Text("-") }
                        Text("${mod.niveis}")
                        TextButton(onClick = { onUpdate(mod.copy(niveis = mod.niveis + 1)) }) { Text("+") }
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.semantics { contentDescription = "Remover modificador ${mod.nome}" }) { Icon(androidx.compose.material.icons.Icons.Default.Delete, null) }
            }
            // GURPS p.262/B102: vários modificadores (Cíclico, Acompanhamento,
            // Curável, Imprecação, etc.) exigem que o jogador especifique uma
            // condição/detalhe. Campo livre opcional pra qualquer modificador
            // — usuário preenche quando precisa, deixa vazio quando não.
            OutlinedTextField(
                value = mod.descricao.orEmpty(),
                onValueChange = { onUpdate(mod.copy(descricao = it)) },
                label = { Text("Descrição/Especificação (opcional)") },
                placeholder = { Text("ex.: interrompido por cuidados médicos") },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                singleLine = false,
                maxLines = 2,
                textStyle = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun TraitRadioButtonOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 2.dp)
    ) {
        RadioButton(selected = selected, onClick = { onClick() })
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

// --- Seletores para Contatos e Patronos ---

@Composable
fun SeletorNhContatoDialog(current: Int, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    val opcoes = listOf(
        12 to 1,
        15 to 2,
        18 to 3,
        21 to 5
    )
    AlertDialog(onDismissRequest = onDismiss, title = { Text("NH do Contato") }, text = {
        Column {
            opcoes.forEach { (nh, custo) -> 
                ListItem(headlineContent = { Text("NH $nh ($custo pts de base)") }, modifier = Modifier.clickable { onSelect(nh) }) 
            }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } })
}

@Composable
fun SeletorFrequenciaAparecimentoDialog(current: Float, onDismiss: () -> Unit, onSelect: (Float) -> Unit) {
    val opcoes = listOf(
        "Quase nunca (6-) | x1/2" to 0.5f,
        "Às vezes (9-) | x1" to 1f,
        "Frequentemente (12-) | x2" to 2f,
        "Sempre (15-) | x3" to 3f
    )
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Frequência de Aparecimento") }, text = {
        Column {
            opcoes.forEach { (label, mult) ->
                ListItem(headlineContent = { Text(label) }, modifier = Modifier.clickable { onSelect(mult) })
            }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } })
}

@Composable
fun SeletorConfiabilidadeDialog(current: Float, onDismiss: () -> Unit, onSelect: (Float) -> Unit) {
    val opcoes = listOf(
        "Não Confiável (Pode mentir) | x1/2" to 0.5f,
        "Geralmente Confiável | x1" to 1f,
        "Totalmente Confiável | x2" to 2f,
        "Mestre (Vantagem Especial) | x3" to 3f
    )
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Confiabilidade") }, text = {
        Column {
            opcoes.forEach { (label, mult) ->
                ListItem(headlineContent = { Text(label) }, modifier = Modifier.clickable { onSelect(mult) })
            }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } })
}

@Composable
fun PeculiaridadeDialog(textoInicial: String = "", onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var texto by remember { mutableStateOf(textoInicial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (textoInicial.isEmpty()) "Adicionar Peculiaridade (-1 pt)" else "Editar Peculiaridade") },
        text = { OutlinedTextField(value = texto, onValueChange = { texto = it }, label = { Text("Descreva a peculiaridade") }, modifier = Modifier.fillMaxWidth()) },
        confirmButton = { TextButton(onClick = { onSave(texto); onDismiss() }, enabled = texto.isNotBlank()) { Text(if (textoInicial.isEmpty()) UiActionLabels.ADICIONAR else "Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(UiActionLabels.CANCELAR) } }
    )
}

@Composable
fun QualidadeDialog(textoInicial: String = "", onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var texto by remember { mutableStateOf(textoInicial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (textoInicial.isEmpty()) "Adicionar Qualidade (1 pt)" else "Editar Qualidade") },
        text = { OutlinedTextField(value = texto, onValueChange = { texto = it }, label = { Text("Descreva a qualidade") }, modifier = Modifier.fillMaxWidth()) },
        confirmButton = { TextButton(onClick = { onSave(texto); onDismiss() }, enabled = texto.isNotBlank()) { Text(if (textoInicial.isEmpty()) UiActionLabels.ADICIONAR else "Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(UiActionLabels.CANCELAR) } }
    )
}

@Composable
fun CatalogoDescricaoDialog(nome: String, descricao: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    nome,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        descricao.ifBlank { "Sem descrição disponível." },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Fechar") }
                }
            }
        }
    }
}
