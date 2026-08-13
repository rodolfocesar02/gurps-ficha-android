package com.gurps.ficha.ui.features.traits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.gurps.ficha.domain.rules.poderes.RegrasDePoder
import com.gurps.ficha.model.FonteDoPoder
import com.gurps.ficha.model.Poder
import com.gurps.ficha.model.PoderDefinicao
import com.gurps.ficha.ui.AppBotaoIcone
import com.gurps.ficha.ui.AppCampoCompacto
import com.gurps.ficha.ui.AppSelectionRow
import com.gurps.ficha.ui.FullscreenDialogContainer
import com.gurps.ficha.ui.UiEstilos
import com.gurps.ficha.viewmodel.FichaViewModel

/**
 * **Configurar Poderes** — GURPS Poderes. Lotes POD-1 a POD-3.
 *
 * ## 🔴 O que mudou, e por quê
 *
 * O modificador de poder **deixou de ser um número que o jogador digita**. O
 * livro diz, em cada verbete, quais fontes aquele poder aceita e quanto cada uma
 * vale: *"Este modificador normalmente é Divino (-10%), Elemental (-10%),
 * Espiritual (-25%), Mágico (-10%), ou Super (-10%)"* (Água, p.121). Agora se
 * **escolhe a fonte** e o percentual vem do livro.
 *
 * O campo de digitar continua existindo, porque o Mestre pode montar um
 * modificador próprio somando componentes (p.20-25) — mas ele deixou de ser o
 * único caminho, que era o que fazia todo poder nascer com 0%.
 *
 * E o Talento **ganhou campo**: ele existia no modelo, era salvo e nunca era
 * desenhado, então nunca saía de zero.
 */
@Composable
fun DialogsPoderes(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit
) {
    val personagem = viewModel.personagem
    var showEditDialog by remember { mutableStateOf<Poder?>(null) }
    var showSelecionarDialog by remember { mutableStateOf(false) }
    var showNovoDialog by remember { mutableStateOf(false) }
    var novoPoderPreenchido by remember { mutableStateOf<Poder?>(null) }
    var definicaoEmUso by remember { mutableStateOf<PoderDefinicao?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Poderes do Personagem",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (personagem.pontosPoderes != 0) {
                    // O total dos Talentos, que passou a custar pontos no POD-3.
                    Text(
                        "Talentos: ${personagem.pontosPoderes} pontos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    itemsIndexed(personagem.poderes) { index, poder ->
                        AppSelectionRow(
                            nome = poder.nome,
                            detalhe = detalheDoPoder(poder),
                            onClick = { showEditDialog = poder },
                            descricaoAcessivel = poder.descricaoAcessivel + ". Toque para editar.",
                            acoes = {
                                AppBotaoIcone(
                                    icone = Icons.Default.Edit,
                                    descricao = "Editar o poder ${poder.nome}",
                                    onClick = { showEditDialog = poder }
                                )
                                AppBotaoIcone(
                                    icone = Icons.Default.Delete,
                                    descricao = "Apagar o poder ${poder.nome}",
                                    onClick = { viewModel.removerPoder(index) }
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showSelecionarDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Novo Poder")
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Fechar")
                }
            }
        }
    }

    if (showSelecionarDialog) {
        SelecionarPoderDialog(
            viewModel = viewModel,
            onDismiss = { showSelecionarDialog = false },
            onSelect = { definicao ->
                showSelecionarDialog = false
                definicaoEmUso = definicao
                novoPoderPreenchido = definicao?.let { d ->
                    // ⚠️ Já nasce com a PRIMEIRA fonte do livro e o percentual
                    // dela. Antes nascia com 0%, e o jogador tinha de saber o
                    // número de cabeça para o poder valer alguma coisa.
                    val padrao = d.fontePadrao
                    Poder(
                        nome = d.nome,
                        fonte = padrao?.fonte.orEmpty(),
                        foco = d.foco,
                        modificadorDePoder = padrao?.valor ?: 0,
                        custoTalentoNivel = d.custoTalentoPorNivel
                    )
                }
                showNovoDialog = true
            }
        )
    }

    if (showNovoDialog) {
        PoderEditDialog(
            poderBase = novoPoderPreenchido,
            definicao = definicaoEmUso,
            fontesGerais = viewModel.dataRepository.fontesDePoder.map {
                FonteDoPoder(it.nome, it.valor)
            },
            isNew = true,
            onDismiss = { showNovoDialog = false },
            onSave = { novoPoder ->
                viewModel.adicionarPoder(novoPoder)
                showNovoDialog = false
            }
        )
    }

    showEditDialog?.let { poderEdit ->
        val index = personagem.poderes.indexOfFirst { it.id == poderEdit.id }
        PoderEditDialog(
            poderBase = poderEdit,
            definicao = viewModel.dataRepository.poderes
                .firstOrNull { it.nome.equals(poderEdit.nome, ignoreCase = true) },
            fontesGerais = viewModel.dataRepository.fontesDePoder.map {
                FonteDoPoder(it.nome, it.valor)
            },
            isNew = false,
            onDismiss = { showEditDialog = null },
            onSave = { modPoder ->
                if (index >= 0) viewModel.atualizarPoder(index, modPoder)
                showEditDialog = null
            }
        )
    }
}

private fun detalheDoPoder(poder: Poder): String = buildString {
    if (poder.fonte.isNotBlank()) append(poder.fonte) else append("sem fonte")
    append(" — ")
    append(if (poder.modificadorDePoder > 0) "+" else "")
    append("${poder.modificadorDePoder}%")
    if (poder.nivelTalento > 0) {
        append(" · Talento ${poder.nivelTalento} (${poder.custoTotalTalento} pts)")
    }
}

@Composable
fun PoderEditDialog(
    poderBase: Poder?,
    definicao: PoderDefinicao? = null,
    fontesGerais: List<FonteDoPoder> = emptyList(),
    isNew: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (Poder) -> Unit
) {
    var nome by remember(poderBase) { mutableStateOf(poderBase?.nome ?: "") }
    var fonte by remember(poderBase) { mutableStateOf(poderBase?.fonte ?: "") }
    var foco by remember(poderBase) { mutableStateOf(poderBase?.foco ?: "") }
    var modificador by remember(poderBase) {
        mutableStateOf(poderBase?.modificadorDePoder?.toString() ?: "0")
    }
    var nivelTalento by remember(poderBase) {
        mutableStateOf(poderBase?.nivelTalento?.toString() ?: "0")
    }
    val custoNivel = poderBase?.custoTalentoNivel
        ?: definicao?.custoTalentoPorNivel
        ?: RegrasDePoder.CUSTO_PADRAO_POR_NIVEL

    // As fontes que ESTE poder aceita. Quando o poder é personalizado (sem
    // verbete no livro), valem as onze genéricas.
    val fontesDoPoder = definicao?.modificadores?.takeIf { it.isNotEmpty() } ?: fontesGerais
    val nivel = nivelTalento.toIntOrNull() ?: 0

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    if (isNew) "Novo Poder" else "Editar Poder",
                    style = MaterialTheme.typography.titleLarge
                )
                if (definicao != null && definicao.pagina > 0) {
                    Text(
                        "GURPS Poderes, p.${definicao.pagina}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                AppCampoCompacto(
                    value = nome,
                    onValueChange = { nome = it },
                    label = "Nome do Poder",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                EscolhaDaFonte(
                    fonteAtual = fonte,
                    fontes = fontesDoPoder,
                    onEscolher = { escolhida ->
                        fonte = escolhida.fonte
                        // 🔴 O percentual acompanha a fonte. Era este o elo que
                        // não existia: o app guardava os dois lados soltos.
                        modificador = escolhida.valor.toString()
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                AppCampoCompacto(
                    value = foco,
                    onValueChange = { foco = it },
                    label = "Foco (ex: Fogo, Mentes)",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppCampoCompacto(
                        value = modificador,
                        onValueChange = { modificador = it.filter { c -> c.isDigit() || c == '-' } },
                        label = "Modificador (%)",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    AppCampoCompacto(
                        value = nivelTalento,
                        onValueChange = { nivelTalento = it.filter(Char::isDigit).take(2) },
                        label = "Talento (nível)",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                // O custo do Talento, à vista. Ele passou a entrar no total da
                // ficha no POD-3 — antes não custava nada.
                Text(
                    "Talento: $custoNivel pontos/nível · " +
                        "${RegrasDePoder.custoDoTalento(nivel, custoNivel)} pontos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                RegrasDePoder.avisoDoTeto(nivel)?.let { aviso ->
                    Text(
                        aviso,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val base = poderBase ?: Poder()
                        onSave(
                            base.copy(
                                nome = nome,
                                fonte = fonte,
                                foco = foco,
                                modificadorDePoder = modificador.toIntOrNull() ?: 0,
                                nivelTalento = nivel,
                                custoTalentoNivel = custoNivel
                            )
                        )
                    }) {
                        Text("Salvar")
                    }
                }
            }
        }
    }
}

/**
 * A escolha da fonte. É aqui que o percentual do livro entra na ficha.
 *
 * ⚠️ Mostra o valor ao lado do nome porque o número é a razão da escolha —
 * Espiritual custa -25% e Cósmico dá +50%, e isso muda o preço de toda
 * habilidade do poder.
 */
@Composable
private fun EscolhaDaFonte(
    fonteAtual: String,
    fontes: List<FonteDoPoder>,
    onEscolher: (FonteDoPoder) -> Unit
) {
    var aberto by remember { mutableStateOf(false) }
    val rotulo = if (fonteAtual.isBlank()) "Escolher a fonte" else fonteAtual

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { aberto = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(rotulo, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Escolher a fonte do poder")
        }
        DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
            fontes.forEach { f ->
                val sinal = if (f.valor > 0) "+" else ""
                DropdownMenuItem(
                    text = { Text("${f.fonte} ($sinal${f.valor}%)") },
                    onClick = {
                        onEscolher(f)
                        aberto = false
                    }
                )
            }
        }
    }
}

@Composable
fun SelecionarPoderDialog(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit,
    onSelect: (PoderDefinicao?) -> Unit
) {
    var busca by remember { mutableStateOf("") }
    val listaPoderes = viewModel.dataRepository.poderes.filter {
        busca.isBlank() ||
            it.nome.contains(busca, ignoreCase = true) ||
            it.foco.contains(busca, ignoreCase = true)
    }

    FullscreenDialogContainer(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                "Selecionar Poder",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            AppCampoCompacto(
                value = busca,
                onValueChange = { busca = it },
                label = "Buscar por nome ou foco...",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { onSelect(null) }, modifier = Modifier.fillMaxWidth()) {
                Text("Criar Poder Personalizado")
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "${listaPoderes.size} poderes (GURPS Poderes, p.121-136)",
                style = MaterialTheme.typography.bodySmall
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(listaPoderes) { definicao ->
                    AppSelectionRow(
                        nome = definicao.nome,
                        detalhe = "Foco: ${definicao.foco}",
                        onClick = { onSelect(definicao) },
                        descricaoAcessivel = descricaoAcessivelDaDefinicao(definicao),
                        extra = {
                            // As fontes válidas COM o percentual: é o que o
                            // jogador precisa para decidir, e o que o catálogo
                            // antigo não tinha (todo poder vinha com 0%).
                            Text(
                                definicao.modificadores.joinToString(" · ") { f ->
                                    "${f.fonte} ${if (f.valor > 0) "+" else ""}${f.valor}%"
                                },
                                style = UiEstilos.detalheDoItem,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (definicao.descricao.isNotBlank()) {
                                Text(definicao.descricao, style = UiEstilos.detalheDoItem)
                            }
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) { Text("Fechar") }
            }
        }
    }
}

private fun descricaoAcessivelDaDefinicao(d: PoderDefinicao): String = buildString {
    append(d.nome)
    append(". Foco: ").append(d.foco)
    if (d.modificadores.isNotEmpty()) {
        append(". Fontes: ")
        append(d.modificadores.joinToString(", ") { f ->
            val v = if (f.valor < 0) "menos ${-f.valor}" else "${f.valor}"
            "${f.fonte}, $v por cento"
        })
    }
    append(". ").append(d.descricao)
}
