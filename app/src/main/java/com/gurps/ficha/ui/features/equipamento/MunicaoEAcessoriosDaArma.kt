package com.gurps.ficha.ui.features.equipamento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.loaders.ItensCatalogLoader
import com.gurps.ficha.domain.rules.MunicaoDaArmaRules
import com.gurps.ficha.model.AcessorioDaArma
import com.gurps.ficha.model.ItemDoCatalogo
import com.gurps.ficha.model.MunicaoDaArma
import com.gurps.ficha.ui.AppBotaoDiscreto
import com.gurps.ficha.ui.AppBotaoIcone
import com.gurps.ficha.ui.AppBotaoPasso
import com.gurps.ficha.ui.AppSelectionDialog
import com.gurps.ficha.ui.AppSelectionRow
import com.gurps.ficha.ui.CorpoDoItemPadrao
import com.gurps.ficha.ui.UiEstilos
import com.gurps.ficha.ui.UiTokens
import com.gurps.ficha.ui.contadorDe

/**
 * **"Munição desta arma" e "Acessórios desta arma"**, no editor da arma — IC-3B do `mesa-virtual/planos/itens/PLANO.md`.
 *
 * O pedido do Rodolfo (11/nov): a munição deixa de ser um item à parte e vira um campo DENTRO da arma. O "Tiros" continua
 * sendo o carregador; aqui o jogador diz quanta munição carrega para ela. Cada linha é um tipo (o comum, e os especiais do
 * catálogo de itens); os acessórios (mira laser, luneta, silenciador) vão pelo mesmo caminho.
 *
 * @param pesoPorTiro o peso de um tiro pelo livro, ou `null` quando o livro não dá (aí a munição não pesa na carga).
 */
@Composable
fun MunicaoEAcessoriosDaArma(
    municoes: List<MunicaoDaArma>,
    onMunicoes: (List<MunicaoDaArma>) -> Unit,
    acessorios: List<AcessorioDaArma>,
    onAcessorios: (List<AcessorioDaArma>) -> Unit,
    tirosRaw: String?,
    pesoPorTiro: Float?
) {
    var escolhendo by remember { mutableStateOf<String?>(null) }   // "municao" | "acessorio"
    val total = municoes.sumOf { it.quantidade.coerceAtLeast(0) }

    Column(verticalArrangement = Arrangement.spacedBy(UiTokens.BotaoEspacamento)) {
        Text(
            "Munição desta arma" + (tirosRaw?.takeIf { it.isNotBlank() }?.let { " (cabem $it no carregador)" } ?: ""),
            style = UiEstilos.detalheDoItem, color = MaterialTheme.colorScheme.outline
        )
        val linhas = municoes.ifEmpty { listOf(MunicaoDaArma()) }
        linhas.forEachIndexed { i, m ->
            val mudar: (Int) -> Unit = { q ->
                onMunicoes(linhas.toMutableList().also { it[i] = m.copy(quantidade = q.coerceIn(0, 99999)) })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(UiTokens.BotaoEspacamento)
            ) {
                Text(m.nome, style = UiEstilos.nomeDoItem, modifier = Modifier.weight(1f))
                AppBotaoPasso(sinal = "−", descricao = "Diminuir ${m.nome}", onClick = { mudar(m.quantidade - 1) }, enabled = m.quantidade > 0)
                OutlinedTextField(
                    value = if (m.quantidade == 0) "" else m.quantidade.toString(),
                    onValueChange = { txt -> mudar(txt.filter { it.isDigit() }.take(5).toIntOrNull() ?: 0) },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.width(96.dp).semantics { contentDescription = "Quantidade de ${m.nome}: ${m.quantidade}" }
                )
                AppBotaoPasso(sinal = "+", descricao = "Aumentar ${m.nome}", onClick = { mudar(m.quantidade + 1) })
                if (linhas.size > 1) {
                    AppBotaoIcone(Icons.Default.Delete, "Tirar ${m.nome} desta arma", { onMunicoes(linhas.filterIndexed { j, _ -> j != i }) })
                }
            }
        }
        Text(
            when {
                pesoPorTiro == null -> "O livro não dá o peso desta munição: ela não soma na carga."
                else -> "Pesa ${"%.2f".format(pesoPorTiro * total).replace('.', ',')} kg na carga · custo $${"%.0f".format(pesoPorTiro * total * MunicaoDaArmaRules.CUSTO_POR_KG)} (MB p.279). A Mesa gasta 1 por tiro."
            },
            style = UiEstilos.detalheDoItem
        )
        AppBotaoDiscreto("Adicionar outro tipo de munição", { escolhendo = "municao" })

        Text("Acessórios desta arma", style = UiEstilos.detalheDoItem, color = MaterialTheme.colorScheme.outline)
        if (acessorios.isEmpty()) Text("Nenhum. Mira laser, luneta e silenciador entram aqui.", style = UiEstilos.detalheDoItem)
        acessorios.forEachIndexed { i, a ->
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(a.nome, style = UiEstilos.nomeDoItem, modifier = Modifier.weight(1f))
                AppBotaoIcone(Icons.Default.Delete, "Tirar ${a.nome} desta arma", { onAcessorios(acessorios.filterIndexed { j, _ -> j != i }) })
            }
        }
        AppBotaoDiscreto("Adicionar acessório", { escolhendo = "acessorio" })
    }

    escolhendo?.let { tipo ->
        EscolherDoCatalogoDialog(
            tipo = tipo,
            onDismiss = { escolhendo = null },
            onEscolher = { item ->
                if (tipo == "municao") {
                    val base = municoes.ifEmpty { listOf(MunicaoDaArma()) }
                    if (base.none { it.itemCatalogoId == item.id }) onMunicoes(base + MunicaoDaArmaRules.municaoDoItem(item))
                } else if (acessorios.none { it.itemCatalogoId == item.id }) {
                    onAcessorios(acessorios + MunicaoDaArmaRules.acessorioDoItem(item))
                }
                escolhendo = null
            }
        )
    }
}

/** A lista do catálogo de itens, só do tipo pedido (as 4 munições especiais, ou os 4 acessórios de arma). */
@Composable
private fun EscolherDoCatalogoDialog(tipo: String, onDismiss: () -> Unit, onEscolher: (ItemDoCatalogo) -> Unit) {
    val context = LocalContext.current
    val itens = remember(tipo) { carregarItensDoLivro(context).filter { ItensCatalogLoader.tipoDe(it) == tipo } }
    AppSelectionDialog(
        titulo = if (tipo == "municao") "Tipo de munição" else "Acessório de arma",
        subtitulo = "Do livro (Módulo Básico), com a regra que ele faz valer.",
        contador = contadorDe(itens.size, "item", "itens"),
        onDismiss = onDismiss
    ) {
        items(itens, key = { it.id }) { i ->
            val linhas = ItensCatalogLoader.linhasDoItem(i)
            AppSelectionRow(
                nome = i.nome,
                detalhe = ItensCatalogLoader.referencia(i),
                descricaoAcessivel = i.nome + ". " + linhas.joinToString(". ") { it.texto },
                onClick = { onEscolher(i) },
                extra = { CorpoDoItemPadrao(linhas) }
            )
        }
    }
}
