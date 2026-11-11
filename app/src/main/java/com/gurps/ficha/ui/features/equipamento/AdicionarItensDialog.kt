package com.gurps.ficha.ui.features.equipamento

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.gurps.ficha.domain.loaders.ItensCatalogLoader
import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.model.ItemDoCatalogo
import com.gurps.ficha.ui.AppBotaoPasso
import com.gurps.ficha.ui.AppBotaoPrincipal
import com.gurps.ficha.ui.AppBotaoSecundario
import com.gurps.ficha.ui.AppFileiraDeBotoes
import com.gurps.ficha.ui.AppFiltroChip
import com.gurps.ficha.ui.AppSelectionDialog
import com.gurps.ficha.ui.AppSelectionRow
import com.gurps.ficha.ui.CorpoDoItemPadrao
import com.gurps.ficha.ui.UiEstilos
import com.gurps.ficha.ui.UiTokens
import com.gurps.ficha.ui.contadorDe

/**
 * **"Adicionar Itens" com o catálogo do livro** — IC-2 do `mesa-virtual/planos/itens/PLANO.md`.
 *
 * Antes este botão abria só um formulário de texto livre: o app não sabia o que o item era, e a Mesa tinha de adivinhar
 * a regra pelo nome digitado. Agora abre os 207 itens do livro (busca e categorias), cada um com o que ele faz; o
 * escolhido entra no inventário com a `regra` dentro (ver `ItensCatalogLoader.equipamentoDoItem`).
 *
 * O texto livre continua existindo, na primeira linha da lista ("Item sem catálogo"): item inventado não tem regra,
 * e o Mestre conduz.
 */

/** Lê o catálogo uma vez por chamada (91 KB, 207 itens). */
fun carregarItensDoLivro(context: Context): List<ItemDoCatalogo> = try {
    ItensCatalogLoader.ler(context.assets.open(ItensCatalogLoader.ARQUIVO).bufferedReader().use { it.readText() })
} catch (_: Exception) {
    emptyList()
}

@Composable
fun AdicionarItensDialog(
    onDismiss: () -> Unit,
    onAdicionar: (Equipamento) -> Unit,
    onSemCatalogo: () -> Unit,
    // IC-4B: o que o personagem já carrega (os venenos dele entram na lista do "Serve para" do antídoto)
    equipamentosDoInventario: List<Equipamento> = emptyList()
) {
    val context = LocalContext.current
    val todos = remember { carregarItensDoLivro(context) }
    val categorias = remember(todos) { ItensCatalogLoader.categorias(todos) }
    var busca by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf<String?>(null) }
    var escolhido by remember { mutableStateOf<ItemDoCatalogo?>(null) }
    val lista = ItensCatalogLoader.filtrar(todos, busca, categoria)

    AppSelectionDialog(
        titulo = "Adicionar Itens",
        subtitulo = "Do livro, com a regra de cada um (Módulo Básico e Magia).",
        busca = busca,
        onBusca = { busca = it },
        rotuloDaBusca = "Buscar item",
        contador = contadorDe(lista.size, "item", "itens"),
        filtros = {
            AppFiltroChip(ItensCatalogLoader.TODAS, categoria == null) { categoria = null }
            categorias.forEach { c -> AppFiltroChip(c, categoria == c) { categoria = c } }
        },
        onDismiss = onDismiss
    ) {
        item(key = "sem-catalogo") {
            AppSelectionRow(
                nome = "Item sem catálogo",
                detalhe = "Texto livre, para um item inventado. Sem regra: o Mestre conduz.",
                onClick = onSemCatalogo
            )
        }
        items(lista, key = { it.id }) { i ->
            val linhas = ItensCatalogLoader.linhasDoItem(i)
            AppSelectionRow(
                nome = i.nome,
                detalhe = "${i.categoria} · ${ItensCatalogLoader.referencia(i)}",
                descricaoAcessivel = i.nome + ". " + i.categoria + ". " + linhas.joinToString(". ") { it.texto },
                onClick = { escolhido = i },
                extra = { CorpoDoItemPadrao(linhas) }
            )
        }
    }

    escolhido?.let { item ->
        val doLivro = remember(todos) { ServeAoVenenoRules.doLivro(todos) }
        ConfirmarItemDialog(
            item = item,
            venenosDoLivro = doLivro,
            venenosDoInventario = remember(equipamentosDoInventario, doLivro) { ServeAoVenenoRules.doInventario(equipamentosDoInventario, doLivro) },
            onDismiss = { escolhido = null },
            onConfirm = { quantidade, serveAo, forma ->
                onAdicionar(ItensCatalogLoader.equipamentoDoItem(item, quantidade, serveAo, forma ?: FormaDoElixirRules.unica(item)))
                escolhido = null
            }
        )
    }
}

/** A confirmação: o cartão do item e a quantidade (doses, granadas, flechas). */
@Composable
private fun ConfirmarItemDialog(
    item: ItemDoCatalogo,
    venenosDoLivro: List<com.gurps.ficha.model.ServeAoVeneno>,
    venenosDoInventario: List<com.gurps.ficha.model.ServeAoVeneno>,
    onDismiss: () -> Unit,
    onConfirm: (Int, com.gurps.ficha.model.ServeAoVeneno?, String?) -> Unit
) {
    var quantidade by remember(item.id) { mutableStateOf(1) }
    // IC-4B: o antídoto serve a UM veneno (MB p.289): a escolha é obrigatória na compra
    val antidoto = ServeAoVenenoRules.ehAntidoto(item)
    var serveAo by remember(item.id) { mutableStateOf<com.gurps.ficha.model.ServeAoVeneno?>(null) }
    // IC-5b: o elixir vem numa forma só (Magia p.213); quando o livro aceita mais de uma, a escolha é obrigatória
    val formas = FormaDoElixirRules.formas(item)
    var forma by remember(item.id) { mutableStateOf<String?>(null) }
    val precisaForma = formas.size > 1
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.nome, style = UiEstilos.tituloDialogo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(UiTokens.BotaoEspacamento)) {
                CorpoDoItemPadrao(ItensCatalogLoader.linhasDoItem(item))
                if (antidoto) ServeAoVenenoCampo(serveAo, { serveAo = it }, venenosDoLivro, venenosDoInventario)
                if (precisaForma) FormaDoElixirCampo(formas, forma) { forma = it }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = false) { contentDescription = "Quantidade: $quantidade" },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.BotaoEspacamento)
                ) {
                    Text("Quantidade", style = UiEstilos.detalheDoItem, modifier = Modifier.weight(1f))
                    AppBotaoPasso(sinal = "−", descricao = "Diminuir a quantidade", onClick = { quantidade = (quantidade - 1).coerceAtLeast(1) }, enabled = quantidade > 1)
                    Text(quantidade.toString(), style = UiEstilos.nomeDoItem)
                    AppBotaoPasso(sinal = "+", descricao = "Aumentar a quantidade", onClick = { quantidade = (quantidade + 1).coerceAtMost(999) })
                }
            }
        },
        confirmButton = {
            AppFileiraDeBotoes {
                AppBotaoSecundario("Cancelar", onDismiss)
                AppBotaoPrincipal("Adicionar", { onConfirm(quantidade, serveAo, forma) }, enabled = (!antidoto || serveAo?.escolhido == true) && (!precisaForma || forma != null))
            }
        }
    )
}
