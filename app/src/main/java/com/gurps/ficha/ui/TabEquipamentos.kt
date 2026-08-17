package com.gurps.ficha.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.CartaoDoItem
import com.gurps.ficha.domain.rules.StMinimaDaArma
import com.gurps.ficha.domain.rules.TextoDoCatalogo
import com.gurps.ficha.model.ArmaduraCatalogoItem
import com.gurps.ficha.model.ArmaCatalogoItem
import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.model.EscudoCatalogoItem
import com.gurps.ficha.model.TipoEquipamento
import com.gurps.ficha.viewmodel.FichaViewModel

private fun limparRuidoGrupoArma(texto: String): String {
    if (texto.isBlank()) return texto
    return texto
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .filterNot { linha ->
            linha.contains("Tabela de Armas", ignoreCase = true) ||
                Regex("\\((DX|IQ|HT|ST)-\\d+.*\\)", RegexOption.IGNORE_CASE).containsMatchIn(linha)
        }
        .joinToString("\n")
}

@Composable
private fun BotaoAdicionarPadrao(texto: String, onClick: () -> Unit) {
    PrimaryActionButton(text = texto, onClick = onClick)
}

@Composable
fun TabEquipamentos(viewModel: FichaViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var showArmaDialog by remember { mutableStateOf(false) }
    var showEscudoDialog by remember { mutableStateOf(false) }
    var showArmaduraDialog by remember { mutableStateOf(false) }
    var armaduraPendenteConfiguracao by remember { mutableStateOf<ArmaduraCatalogoItem?>(null) }
    var editingEquipamento by remember { mutableStateOf<Pair<Int, Equipamento>?>(null) }
    // Lote ARMA-3: a arma cuja ficha técnica está aberta (vinda do catálogo).
    var armaDetalhada by remember { mutableStateOf<ArmaCatalogoItem?>(null) }
    // Lote ARMA-4: a mesma ficha, aberta a partir do inventário.
    var armaDoInventarioDetalhada by remember { mutableStateOf<Equipamento?>(null) }
    // Lote EQP-6: a ficha técnica do escudo e da armadura, antes de adicionar.
    var escudoDetalhado by remember { mutableStateOf<EscudoCatalogoItem?>(null) }
    var armaduraDetalhada by remember { mutableStateOf<ArmaduraCatalogoItem?>(null) }

    val p = viewModel.personagem
    val errosCarga = viewModel.errosCargaCatalogos
    val equipamentosComIndice = p.equipamentos.withIndex().toList()
    val equipamentosManuais = equipamentosComIndice.filter { it.value.tipo == TipoEquipamento.GERAL }
    val armasEquipadas = equipamentosComIndice.filter { it.value.tipo == TipoEquipamento.ARMA }
    val escudosEquipados = equipamentosComIndice.filter { it.value.tipo == TipoEquipamento.ESCUDO }
    val armadurasEquipadas = equipamentosComIndice.filter { it.value.tipo == TipoEquipamento.ARMADURA }

    StandardTabColumn {
        if (errosCarga.isNotEmpty()) {
            SectionCard(title = "Aviso de Catálogo") {
                Text(
                    "Alguns catálogos não foram carregados corretamente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                errosCarga.forEach { (catalogo, mensagem) ->
                    Text(
                        "- $catalogo: $mensagem",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        BotaoAdicionarPadrao(texto = "Adicionar Itens", onClick = { showDialog = true })

        if (equipamentosManuais.isNotEmpty()) {
            Text("Equipamentos Manuais", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            equipamentosManuais.forEach { entry ->
                AppListItemCard {
                    EquipamentoItem(
                        equipamento = entry.value,
                        onEdit = { editingEquipamento = entry.index to entry.value },
                        onDelete = { viewModel.removerEquipamento(entry.index) },
                        viewModel = viewModel
                    )
                }
            }
        }

        BotaoAdicionarPadrao(texto = "Adicionar Arma", onClick = { showArmaDialog = true })

        if (armasEquipadas.isNotEmpty()) {
            Text("Armas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            armasEquipadas.forEach { entry ->
                AppListItemCard {
                    EquipamentoArmaItem(
                        equipamento = entry.value,
                        onEdit = { editingEquipamento = entry.index to entry.value },
                        onDelete = { viewModel.removerEquipamento(entry.index) },
                        onDetalhes = { armaDoInventarioDetalhada = entry.value },
                        viewModel = viewModel
                    )
                }
            }
        }

        BotaoAdicionarPadrao(texto = "Adicionar Escudo", onClick = { showEscudoDialog = true })

        if (escudosEquipados.isNotEmpty()) {
            Text("Escudos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            escudosEquipados.forEach { entry ->
                AppListItemCard {
                    EquipamentoItem(
                        equipamento = entry.value,
                        onEdit = { editingEquipamento = entry.index to entry.value },
                        onDelete = { viewModel.removerEquipamento(entry.index) },
                        viewModel = viewModel
                    )
                }
            }
        }

        BotaoAdicionarPadrao(texto = "Adicionar Armadura", onClick = { showArmaduraDialog = true })

        if (armadurasEquipadas.isNotEmpty()) {
            Text("Armaduras", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            armadurasEquipadas.forEach { entry ->
                AppListItemCard {
                    ArmaduraSelecionadaItem(
                        equipamento = entry.value,
                        onEdit = { editingEquipamento = entry.index to entry.value },
                        onDelete = { viewModel.removerEquipamento(entry.index) }
                    )
                }
            }
        }

        ResumoEquipamentosFooter(viewModel = viewModel)
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showDialog) {
        EquipamentoDialog(
            onDismiss = { showDialog = false },
            onSave = {
                viewModel.adicionarEquipamento(it)
                showDialog = false
            }
        )
    }

    editingEquipamento?.let { (index, equipamento) ->
        EquipamentoDialog(
            initialEquipamento = equipamento,
            // Lote LAYOUT-7: o editor mostra a MESMA ficha do card do seletor,
            // só leitura, acima dos campos. Nula quando o item foi criado à mão
            // e não casa com nada do catálogo.
            //
            // ⚠️ Lote EQP-7: aqui se perguntava só pela ARMA, e armadura e escudo
            // caíam em `null` — a mesma peça tinha ficha completa ao escolher e
            // formulário pelado ao editar. O `when` mora no ViewModel, num lugar
            // só, para não haver uma terceira vez.
            fichaTecnica = viewModel.fichaTecnicaDoItem(equipamento),
            onDismiss = { editingEquipamento = null },
            onSave = {
                viewModel.atualizarEquipamento(index, it)
                editingEquipamento = null
            }
        )
    }

    if (showArmaDialog) {
        SelecionarArmaEquipamentoDialog(
            viewModel = viewModel,
            onDismiss = { showArmaDialog = false },
            // ⚠️ Lote ARMA-3: o toque na lista NÃO adiciona mais direto. Ele
            // abre a ficha técnica, e o botão de adicionar mora lá dentro.
            // Dois toques em vez de um, para ninguém comprar uma arma sem saber
            // que ela pesa 7,3 kg e exige as duas mãos.
            onSelect = { armaDetalhada = it }
        )
    }

    // Lote ARMA-3/4: o mesmo card serve os dois lados. Na seleção ele adiciona;
    // no inventário ele é só leitura, porque a arma já está na ficha.
    armaDetalhada?.let { arma ->
        com.gurps.ficha.ui.features.equipamento.CardDetalheDoItem(
            ficha = viewModel.fichaTecnicaDaArma(arma),
            rotuloAcao = "Adicionar ao inventário",
            onAcao = {
                viewModel.adicionarEquipamentoArma(arma)
                armaDetalhada = null
                showArmaDialog = false
            },
            onDismiss = { armaDetalhada = null }
        )
    }

    armaDoInventarioDetalhada?.let { equipamento ->
        val doCatalogo = viewModel.armaDoCatalogoPara(equipamento)
        if (doCatalogo != null) {
            com.gurps.ficha.ui.features.equipamento.CardDetalheDoItem(
                ficha = viewModel.fichaTecnicaDaArma(doCatalogo),
                rotuloAcao = null,
                onDismiss = { armaDoInventarioDetalhada = null }
            )
        } else {
            // Arma que não casa com o catálogo (criada à mão, ou de uma versão
            // anterior). Dizer isso é melhor que abrir um card vazio.
            com.gurps.ficha.ui.features.equipamento.CardArmaForaDoCatalogo(
                equipamento = equipamento,
                onDismiss = { armaDoInventarioDetalhada = null }
            )
        }
    }

    if (showEscudoDialog) {
        SelecionarEscudoEquipamentoDialog(
            viewModel = viewModel,
            onDismiss = { showEscudoDialog = false },
            // Lote EQP-6: mesmo gesto da arma. O toque abre a ficha; o botão de
            // adicionar mora lá dentro. Antes, tocar num escudo já o punha na
            // ficha — dava para comprar um Escudo Grande sem descobrir que ele
            // pesa 12,5 kg, ocupa a mão e dá -2 nos ataques corpo a corpo.
            onSelect = { escudoDetalhado = it }
        )
    }

    escudoDetalhado?.let { escudo ->
        com.gurps.ficha.ui.features.equipamento.CardDetalheDoItem(
            ficha = viewModel.fichaTecnicaDoEscudo(escudo),
            rotuloAcao = "Adicionar ao inventário",
            onAcao = {
                viewModel.adicionarEquipamentoEscudo(escudo)
                escudoDetalhado = null
                showEscudoDialog = false
            },
            onDismiss = { escudoDetalhado = null }
        )
    }

    // A armadura tem um passo a mais: depois da ficha vem o Configurar Armadura,
    // porque uma peça de tronco+virilha pode entrar só num dos dois locais.
    armaduraDetalhada?.let { armadura ->
        com.gurps.ficha.ui.features.equipamento.CardDetalheDoItem(
            ficha = viewModel.fichaTecnicaDaArmadura(armadura),
            rotuloAcao = "Escolher os locais",
            onAcao = {
                armaduraDetalhada = null
                armaduraPendenteConfiguracao = armadura
            },
            onDismiss = { armaduraDetalhada = null }
        )
    }

    if (showArmaduraDialog) {
        SelecionarArmaduraEquipamentoDialog(
            viewModel = viewModel,
            onDismiss = { showArmaduraDialog = false },
            // Lote EQP-6: a ficha entra ANTES do Configurar Armadura. Escolher
            // os locais de uma peça sem ter visto a RD dela era decidir no escuro.
            onSelect = {
                armaduraDetalhada = it
                showArmaduraDialog = false
            }
        )
    }

    armaduraPendenteConfiguracao?.let { armadura ->
        ConfigurarArmaduraDialog(
            armadura = armadura,
            onDismiss = { armaduraPendenteConfiguracao = null },
            onConfirm = { locais ->
                viewModel.adicionarEquipamentoArmaduraComSelecao(armadura, locais)
                armaduraPendenteConfiguracao = null
            }
        )
    }
}

/**
 * O cartão de uma armadura já vestida.
 *
 * 🔴 **Lote EQP-2 — os dois defeitos que este cartão tinha.**
 *
 * 1. Ele não passou pelo EQP-1. Eu procurei "cartão de item", achei dois, e este
 *    tem outro nome e mora 200 linhas acima. Ficou com o nome em `bodyLarge`
 *    (quatro pontos maior, não um), sem `maxLines` e sem orçamento — na foto do
 *    usuário, *"Perneiras de Couro Reforçado (pernas)"* quebrava em duas linhas
 *    e o cartão inteiro ia a cinco.
 *
 * 2. Ele mostrava **dois RD diferentes para a mesma armadura**: `RD: 1*` numa
 *    linha e `Local: tronco; RD: 2*` na seguinte. Ver
 *    [CartaoDoItem.notaSemCabecalho] — o segundo é texto congelado, e só o
 *    primeiro alimenta o combate.
 *
 * ⚠️ O padrão que os dois defeitos têm em comum é o de sempre: **duas rotas para
 * a mesma coisa**. Cada uma, lida sozinha, estava certa.
 */
@Composable
private fun ArmaduraSelecionadaItem(
    equipamento: Equipamento,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            NomeDoItemPadrao(TextoDoCatalogo.corrigir(equipamento.nome))
            CorpoDoItemPadrao(CartaoDoItem.linhasDaArmadura(equipamento))
        }
        IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar armadura ${equipamento.nome}") }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Remover armadura ${equipamento.nome}") }
    }
}

@Composable
private fun ResumoEquipamentosFooter(viewModel: FichaViewModel) {
    val p = viewModel.personagem
    SummaryFooterCard(title = "Resumo de Equipamentos") {
        Text("ST atual: ${p.forca}", style = MaterialTheme.typography.labelSmall)
        Text("Peso total: ${viewModel.pesoTotal} kg", style = MaterialTheme.typography.labelSmall)
        Text(
            "Custo total: $${viewModel.custoTotalEquipamentos}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}


// ======================================================================
// O cartao padrao de um item -- Lotes EQP-1 e EQP-2
//
// Os TRES cartoes da aba (equipamento manual, arma, armadura) passam por aqui.
// Eram tres desenhos separados ate o EQP-2, e o da armadura tinha divergido
// sozinho: nome em `bodyLarge`, sem `maxLines`, sem orcamento nenhum.
// ======================================================================

/**
 * O corpo de um cartao de item, com **orcamento de linhas**.
 */
@Composable
fun CorpoDoItemPadrao(linhas: List<CartaoDoItem.Linha>) {
    // A conta mora em `domain/rules/CartaoDoItem.kt` (Lote EQP-2). Aqui só se
    // pinta: enquanto o orçamento vivia dentro deste @Composable, nenhum teste
    // conseguia perguntar quantas linhas o cartão mostra — e foi assim que o
    // cartão de armadura passou o gate inteiro com cinco.
    val visiveis = CartaoDoItem.cortar(linhas)
    visiveis.forEachIndexed { i, linha ->
        Text(
            linha.texto,
            style = MaterialTheme.typography.bodySmall,
            color = corDoPapel(linha.papel),
            maxLines = CartaoDoItem.alturaDe(i, visiveis.size),
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * O papel da linha vira cor **aqui**, e não em `domain/`.
 *
 * ⚠️ `MaterialTheme.colorScheme` só existe dentro de um `@Composable`, e é ele
 * que sabe se o tema está claro ou escuro. Escolher a cor lá atrás seria decidir
 * por um tema que aquele arquivo não enxerga.
 */
@Composable
private fun corDoPapel(papel: CartaoDoItem.Papel): Color = when (papel) {
    CartaoDoItem.Papel.NEUTRO -> MaterialTheme.colorScheme.onSurfaceVariant
    CartaoDoItem.Papel.DANO -> MaterialTheme.colorScheme.tertiary
    CartaoDoItem.Papel.CUSTO -> MaterialTheme.colorScheme.primary
    CartaoDoItem.Papel.PROTECAO -> MaterialTheme.colorScheme.primary
    CartaoDoItem.Papel.ALERTA -> MaterialTheme.colorScheme.error
}

/**
 * O nome do item: **um ponto maior** que o corpo, e o unico em negrito.
 *
 * ⚠️ Um ponto, nao quatro. Antes o nome era `bodyLarge` (16 sp) contra
 * `bodySmall` (12) do corpo — quatro pontos de diferenca, e o cartao lia como
 * titulo com legenda. O pedido do usuario foi de **hierarquia discreta**: o nome
 * se destaca pelo negrito, nao pelo tamanho.
 */
@Composable
fun NomeDoItemPadrao(nome: String) {
    Text(
        nome,
        style = MaterialTheme.typography.bodySmall,
        // "Um ponto maior", literalmente: o corpo do cartão é `bodySmall`, e o
        // nome é ele mais 1 sp. `TextUnit` não soma com `+`, então a conta é
        // feita no valor.
        fontSize = (MaterialTheme.typography.bodySmall.fontSize.value + 1f).sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun EquipamentoArmaItem(
    equipamento: Equipamento,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    viewModel: FichaViewModel,
    // Lote ARMA-4: abre a ficha técnica completa desta arma.
    onDetalhes: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onDetalhes)
                .semantics {
                    contentDescription = "${equipamento.nome}. Toque para ver a ficha técnica completa."
                }
        ) {
            NomeDoItemPadrao(equipamento.nome)
            val observacoesCatalogo = viewModel.observacoesArmaPorEquipamento(equipamento).trim()
            val observacoesFaltantes = if (observacoesCatalogo.isBlank()) emptyList() else {
                observacoesCatalogo.lineSequence().map { it.trim() }
                    .filter { it.isNotBlank() }
                    .filterNot { linha -> equipamento.notas.contains(linha) }
                    .toList()
            }
            CorpoDoItemPadrao(
                buildList {
                    // Saga: arma tirada pela narrativa. Continua na ficha, mas
                    // fora do combate — e a primeira coisa que precisa ser vista.
                    if (equipamento.confiscado) {
                        add(CartaoDoItem.Linha(
                            "⛓️ confiscado na história — fora do combate", CartaoDoItem.Papel.ALERTA))
                    }
                    val danoRaw = equipamento.armaDanoRaw
                    add(CartaoDoItem.Linha(
                        if (danoRaw.isNullOrBlank()) "Dano: -"
                        else "Dano: ${viewModel.calcularDanoArmaComSt(danoRaw)}",
                        if (danoRaw.isNullOrBlank()) CartaoDoItem.Papel.NEUTRO
                        else CartaoDoItem.Papel.DANO
                    ))
                    val notasLimpas = if (equipamento.armaCatalogoId != null) {
                        limparRuidoGrupoArma(equipamento.notas)
                    } else {
                        equipamento.notas
                    }
                    if (notasLimpas.isNotBlank()) {
                        add(CartaoDoItem.Linha(
                            notasLimpas.replace("\n", " · "), CartaoDoItem.Papel.NEUTRO))
                    }
                    if (observacoesFaltantes.isNotEmpty()) {
                        add(CartaoDoItem.Linha(
                            observacoesFaltantes.joinToString(" · "), CartaoDoItem.Papel.DANO))
                    }
                }
            )
        }
        IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar arma ${equipamento.nome}") }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Remover arma ${equipamento.nome}") }
    }
}

@Composable
fun EquipamentoItem(equipamento: Equipamento, onEdit: () -> Unit, onDelete: () -> Unit, viewModel: FichaViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            NomeDoItemPadrao(equipamento.nome)
            CorpoDoItemPadrao(
                buildList {
                    add(CartaoDoItem.Linha(
                        CartaoDoItem.pesoEQuantidade(equipamento.quantidade, equipamento.peso),
                        CartaoDoItem.Papel.NEUTRO
                    ))
                    equipamento.armaDanoRaw?.takeIf { it.isNotBlank() }?.let { raw ->
                        add(CartaoDoItem.Linha(
                            "Dano: $raw -> ${viewModel.calcularDanoArmaComSt(raw)}", CartaoDoItem.Papel.DANO))
                    }
                    if (equipamento.custo > 0) {
                        add(CartaoDoItem.Linha(
                            "Custo: $${equipamento.custo * equipamento.quantidade}", CartaoDoItem.Papel.CUSTO))
                    }
                    if (equipamento.notas.isNotBlank()) {
                        add(CartaoDoItem.Linha(
                            equipamento.notas.replace("\n", " · "), CartaoDoItem.Papel.NEUTRO))
                    }
                }
            )
        }
        IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar equipamento ${equipamento.nome}") }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Remover equipamento ${equipamento.nome}") }
    }
}

@Composable
fun SelecionarArmaEquipamentoDialog(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit,
    onSelect: (ArmaCatalogoItem) -> Unit
) {
    val stAtual = viewModel.personagem.forca
    val armas = viewModel.armasEquipamentosFiltradas
    val mostrarObsArmaFogo = viewModel.equipmentSearch.type == "armas_de_fogo"

    // Lote LAYOUT-6. Os filtros viram chips de verdade (eram texto solto, e por
    // isso não pareciam clicáveis) e a linha usa o bloco `extra`, porque a arma
    // tem mais de duas linhas de informação.
    AppSelectionDialog(
        titulo = "Selecionar Arma",
        subtitulo = "ST do personagem: $stAtual",
        busca = viewModel.equipmentSearch.query,
        onBusca = { viewModel.atualizarBuscaArmaEquipamento(it) },
        rotuloDaBusca = "Buscar por nome",
        contador = contadorDe(armas.size, "arma", "armas"),
        filtros = {
            AppFiltroChip("Todas", viewModel.equipmentSearch.type == null) { viewModel.atualizarFiltroTipoArmaEquipamento(null) }
            AppFiltroChip("Corpo a corpo", viewModel.equipmentSearch.type == "corpo_a_corpo") { viewModel.atualizarFiltroTipoArmaEquipamento("corpo_a_corpo") }
            AppFiltroChip("Distância", viewModel.equipmentSearch.type == "distancia") { viewModel.atualizarFiltroTipoArmaEquipamento("distancia") }
            AppFiltroChip("Armas de Fogo", viewModel.equipmentSearch.type == "armas_de_fogo") { viewModel.atualizarFiltroTipoArmaEquipamento("armas_de_fogo") }
        },
        filtrosSecundarios = if (viewModel.equipmentSearch.type != "armas_de_fogo") null else ({
            AppFiltroChip("Todas Fogo", viewModel.equipmentSearch.fireArmCategory == null) { viewModel.atualizarFiltroCategoriaArmaFogoEquipamento(null) }
            AppFiltroChip("Pistolas e MM", viewModel.equipmentSearch.fireArmCategory == "pistolas_mm") { viewModel.atualizarFiltroCategoriaArmaFogoEquipamento("pistolas_mm") }
            AppFiltroChip("Rifles e Espingardas", viewModel.equipmentSearch.fireArmCategory == "rifles_espingardas") { viewModel.atualizarFiltroCategoriaArmaFogoEquipamento("rifles_espingardas") }
            AppFiltroChip("Ultra-Tech", viewModel.equipmentSearch.fireArmCategory == "ultratech") { viewModel.atualizarFiltroCategoriaArmaFogoEquipamento("ultratech") }
            AppFiltroChip("Armas Pesadas", viewModel.equipmentSearch.fireArmCategory == "pesadas") { viewModel.atualizarFiltroCategoriaArmaFogoEquipamento("pesadas") }
        }),
        onDismiss = onDismiss
    ) {
        items(armas, key = { it.id }) { arma ->
            ArmaItemSelecao(
                arma = arma,
                danoCalculado = viewModel.calcularDanoArmaComSt(arma.danoRaw),
                // Lote EQP-3: o TEXTO da nota, não o número dela.
                observacoes = if (mostrarObsArmaFogo || arma.tipoCombate != "armas_de_fogo") {
                    viewModel.observacoesArmaDoCatalogo(arma)
                } else {
                    emptyList()
                },
                stDoPersonagem = stAtual,
                onClick = { onSelect(arma) }
            )
        }
    }
}

@Composable
private fun TipoArmaFiltroChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall
    )
}

private fun observacoesFormatadas(armadura: ArmaduraCatalogoItem): List<String> {
    val refs = Regex("\\[(\\d+)]")
        .findAll(armadura.observacoes)
        .mapNotNull { it.groupValues.getOrNull(1)?.toIntOrNull() }
        .toList()
    val detalhesOriginais = armadura.observacoesDetalhadas
        .map { it.trim() }
        .filter { it.isNotBlank() }
    if (detalhesOriginais.isEmpty()) return emptyList()

    val saida = mutableListOf<String>()
    var detalhes = detalhesOriginais

    // Para NT alto, a observacao global (NT7+) deve aparecer sem numero.
    val primeira = detalhes.firstOrNull()
    if (primeira != null && primeira.contains("NT7+", ignoreCase = true)) {
        saida.add(primeira)
        detalhes = detalhes.drop(1)
    }

    if (refs.isEmpty() || detalhes.isEmpty()) return saida

    val pareadas = refs.zip(detalhes).map { (ref, texto) -> "[$ref] $texto" }
    saida.addAll(pareadas)
    if (detalhes.size > refs.size) {
        detalhes.drop(refs.size).forEach { extra -> saida.add(extra) }
    }
    return saida
}

/**
 * Uma arma na lista de escolha.
 *
 * 🔴 **Lote EQP-3 — `Obs: [1]` não dizia nada.** A linha imprimia
 * `arma.observacoes` **cru**, e o catálogo guarda ali só o número do rodapé. O
 * jogador via uma referência sem a referência.
 *
 * ⚠️ O texto do livro já existia — `FichaEquipmentDelegate` casa `[1]` com a
 * nota certa, e a ficha técnica e o cartão da arma equipada já o usavam. Só esta
 * lista tinha caminho próprio. **Terceira vez** que o defeito estava na
 * diferença entre duas rotas para a mesma coisa.
 *
 * @param observacoes as notas **já casadas com o texto**. Vazia = nada a mostrar,
 *   e aí a linha some em vez de exibir um número solto.
 */
@Composable
private fun ArmaItemSelecao(
    arma: ArmaCatalogoItem,
    danoCalculado: String,
    observacoes: List<String>,
    stDoPersonagem: Int,
    onClick: () -> Unit
) {
    val tipoLabel = when (arma.tipoCombate) {
        "corpo_a_corpo" -> "Corpo a corpo"
        "armas_de_fogo" -> "Armas de Fogo"
        else -> "Distancia"
    }
    // Lote EQP-4: os dois números já estavam na tela — "ST 11" aqui e "ST do
    // personagem: 9" no alto. Faltava a conta entre eles, e a consequência.
    val falta = StMinimaDaArma.avaliar(stDoPersonagem, arma.stMinimo)

    AppSelectionRow(
        nome = arma.nome,
        detalhe = "ST ${arma.stMinimo ?: "—"} | $tipoLabel",
        onClick = onClick,
        descricaoAcessivel = "${arma.nome}. ST ${arma.stMinimo ?: "não cadastrada"}, $tipoLabel. " +
            "Dano $danoCalculado. " +
            (falta?.let { StMinimaDaArma.descricaoAcessivel(it) + " " } ?: "") +
            "Toque para ver a ficha técnica.",
        extra = {
            falta?.let {
                Text(
                    StMinimaDaArma.aviso(it),
                    style = UiEstilos.detalheDoItem,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                "Dano: ${arma.danoRaw} → $danoCalculado | Custo: $${arma.custoBase ?: 0f} | Peso: ${arma.pesoBaseKg ?: 0f} kg",
                style = UiEstilos.detalheDoItem,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!arma.aparar.isNullOrBlank()) {
                Text(
                    "Aparar: ${arma.aparar}",
                    style = UiEstilos.detalheDoItem,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            observacoes.forEach { nota ->
                Text(
                    nota,
                    style = UiEstilos.detalheDoItem,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    // Duas linhas: a nota do livro pode ter 300 caracteres, e
                    // numa lista de 70 armas isso vira parede. O texto inteiro
                    // está a um toque, na ficha técnica.
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    )
}

@Composable
private fun SelecionarEscudoEquipamentoDialog(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit,
    onSelect: (EscudoCatalogoItem) -> Unit
) {
    val escudos = viewModel.escudosEquipamentosFiltrados
    val stAtual = viewModel.personagem.forca
    // Lote LAYOUT-6.
    AppSelectionDialog(
        titulo = "Selecionar Escudo",
        subtitulo = "ST do personagem: $stAtual",
        busca = viewModel.equipmentSearch.query,
        onBusca = { viewModel.atualizarBuscaEscudoEquipamento(it) },
        rotuloDaBusca = "Buscar escudo",
        contador = contadorDe(escudos.size, "escudo", "escudos"),
        onDismiss = onDismiss
    ) {
        items(escudos, key = { it.id }) { escudo ->
            AppSelectionRow(
                nome = escudo.nome,
                detalhe = "BD +${escudo.db} | Custo: $${escudo.custo ?: 0f} | Peso: ${escudo.pesoKg ?: 0f} kg",
                onClick = { onSelect(escudo) }
            )
        }
    }
}

private val LOCAIS_ARMADURA = listOf(
    "cabeca" to "Cabeca",
    "corpo" to "Corpo",
    "pescoco" to "Pescoco",
    "tronco" to "Tronco",
    "virilha" to "Virilha",
    "membros" to "Membros",
    "bracos" to "Bracos",
    "pernas" to "Pernas",
    "pes" to "Pes",
    "maos" to "Maos",
    "traje_completo" to "Traje Completo"
)

@Composable
private fun SelecionarArmaduraEquipamentoDialog(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit,
    onSelect: (ArmaduraCatalogoItem) -> Unit
) {
    val armaduras = viewModel.armadurasEquipamentosFiltradas
    val filtrosAtivos = viewModel.equipmentSearch.query.isNotBlank() ||
        viewModel.equipmentSearch.armorerLocation != null ||
        viewModel.equipmentSearch.armorerNt != null
    // Lote LAYOUT-6. O "Resultados: N" vira o contador padrão, e o "Limpar
    // filtros" entra no cabeçalho livre — ele é uma ação, não um filtro.
    AppSelectionDialog(
        titulo = "Selecionar Armadura",
        subtitulo = "Use Local e NT para refinar mais rápido.",
        busca = viewModel.equipmentSearch.query,
        onBusca = { viewModel.atualizarBuscaArmaduraEquipamento(it) },
        rotuloDaBusca = "Buscar armadura",
        contador = contadorDe(armaduras.size, "armadura", "armaduras"),
        filtros = {
            AppFiltroChip("Local: Todos", viewModel.equipmentSearch.armorerLocation == null) { viewModel.atualizarFiltroLocalArmaduraEquipamento(null) }
            LOCAIS_ARMADURA.forEach { (id, label) ->
                AppFiltroChip(label, viewModel.equipmentSearch.armorerLocation == id) { viewModel.atualizarFiltroLocalArmaduraEquipamento(id) }
            }
        },
        filtrosSecundarios = {
            AppFiltroChip("NT: Todas", viewModel.equipmentSearch.armorerNt == null) { viewModel.atualizarFiltroNtArmaduraEquipamento(null) }
            for (nt in 0..10) {
                AppFiltroChip("NT $nt", viewModel.equipmentSearch.armorerNt == nt) { viewModel.atualizarFiltroNtArmaduraEquipamento(nt) }
            }
        },
        cabecalhoExtra = if (!filtrosAtivos) null else ({
            AppFileiraDeBotoes(alinhamento = Arrangement.Start) {
                AppBotaoDiscreto(texto = "Limpar filtros", onClick = { viewModel.limparFiltrosArmaduraEquipamento() })
            }
        }),
        onDismiss = onDismiss
    ) {
        items(armaduras, key = { it.id }) { armadura ->
            val observacoes = observacoesFormatadas(armadura)
            AppSelectionRow(
                nome = TextoDoCatalogo.corrigir(armadura.nome),
                detalhe = "NT ${armadura.nt ?: "—"} | RD ${armadura.rd} | Peso ${armadura.pesoBaseKg ?: 0f} kg | Custo $${armadura.custoBase ?: 0f}",
                onClick = { onSelect(armadura) },
                descricaoAcessivel = "${TextoDoCatalogo.corrigir(armadura.nome)}. NT ${armadura.nt ?: "não cadastrado"}, " +
                    "RD ${armadura.rd}. Local: ${TextoDoCatalogo.corrigir(armadura.local)}.",
                extra = {
                    Text(
                        "Local: ${TextoDoCatalogo.corrigir(armadura.local)}",
                        style = UiEstilos.detalheDoItem,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    observacoes.forEach { linha ->
                        Text(
                            linha,
                            style = UiEstilos.detalheDoItem,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun ConfigurarArmaduraDialog(
    armadura: ArmaduraCatalogoItem,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    // ⚠️ Lote EQP-5: o conserto de acento entra AQUI, na origem da lista.
    // O EQP-3 arrumou a lista de escolha e esqueceu esta — e o `crnio` reapareceu
    // na caixinha do Configurar Armadura (foto do usuário, 8h33). Mesma falha de
    // sempre: dois caminhos lendo o mesmo campo, e eu corrigi um.
    //
    // Corrigir aqui também alinha o que é **guardado**: o local escolhido vira o
    // `armaduraLocal` do equipamento, que é o campo que `CoberturaDaArmadura` usa
    // para casar a armadura com o local do ferimento.
    val locais = remember(armadura.id, armadura.local, armadura.componentes) {
        val locaisBase = TextoDoCatalogo.corrigir(armadura.local)
            .split(Regex("[,;/|]"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
        val viaComponentes = armadura.componentes.flatMap { c ->
            TextoDoCatalogo.corrigir(c.local)
                .split(Regex("[,;/|]"))
                .map { it.trim() }
                .filter { it.isNotBlank() }
        }
        (locaisBase + viaComponentes).distinct().ifEmpty { listOf("corpo") }
    }
    val conjuntoObrigatorio = remember(armadura.id, armadura.nome, armadura.componentes) {
        armadura.componentes.isNotEmpty() && armadura.nome.contains("+")
    }

    var selecionados by remember(armadura.id, conjuntoObrigatorio) { mutableStateOf(locais.toSet()) }

    val divisor = selecionados.size.coerceAtLeast(1).toFloat()
    val custoPrevisto = armadura.custoBase ?: 0f
    val pesoPrevisto = armadura.pesoBaseKg ?: 0f

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar Armadura") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(armadura.nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    if (conjuntoObrigatorio)
                        "Este conjunto adiciona todas as partes automaticamente."
                    else
                        "Escolha os locais para adicionar no inventario.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .rolagemVertical(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    locais.forEach { local ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !conjuntoObrigatorio) {
                                    selecionados = if (selecionados.contains(local)) selecionados - local else selecionados + local
                                }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selecionados.contains(local),
                                enabled = !conjuntoObrigatorio,
                                onCheckedChange = { checked ->
                                    selecionados = if (checked) selecionados + local else selecionados - local
                                }
                            )
                            Text(local, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                HorizontalDivider()
                Text(
                    "Previsto total -> Custo: $${custoPrevisto} | Peso: ${pesoPrevisto} kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Por local selecionado: Custo ${String.format("%.2f", custoPrevisto / divisor)} | Peso ${String.format("%.2f", pesoPrevisto / divisor)} kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selecionados.toList()) },
                enabled = selecionados.isNotEmpty()
            ) { Text("Adicionar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
