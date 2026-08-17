package com.gurps.ficha.ui.features.traits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.model.Poder
import com.gurps.ficha.ui.AppBotaoIcone
import com.gurps.ficha.ui.AppListItemCard
import com.gurps.ficha.viewmodel.FichaViewModel

/**
 * **Os poderes na aba Traços, ao lado das vantagens** — Lote POD-23.
 *
 * ## Por que eles saíram de dentro do botão
 *
 * Pedido do usuário, depois de ver a tela: *"pode se colocar o poder semelhante
 * fica as vantagens e desvantagens, pra fora do botão, na tela da aba traços"*.
 *
 * E ele está certo pela regra, não só pelo gosto. Um poder **é um traço do
 * personagem** — custa pontos (`pontosPoderes` entra em `pontosGastos` desde o
 * POD-3) e aparece na conta da ficha. Vantagem e desvantagem se listam na aba;
 * o poder ficava escondido atrás de *Configurar Poderes*, e quem abria a ficha
 * não tinha como saber que ele existia.
 *
 * ⚠️ O botão **continua**: é por ele que se cria um poder novo e se chega ao
 * catálogo dos 47. O que mudou é que a **lista** vive fora dele. Não são duas
 * rotas para a mesma coisa — é uma rota para criar e uma vitrine para ver.
 *
 * Arquivo próprio porque `TabTracos.kt` já passa de 400 linhas e o teto do
 * projeto é 1.000: seção nova nasce ao lado, não dentro.
 */
@Composable
fun SecaoDePoderes(
    viewModel: FichaViewModel,
    onEditarVantagem: (Int) -> Unit = {},
    onEditarDesvantagem: (Int) -> Unit = {}
) {
    val poderes = viewModel.personagem.poderes
    if (poderes.isEmpty()) return

    var editando by remember { mutableStateOf<Int?>(null) }

    Text(
        "Poderes",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    poderes.forEachIndexed { index, poder ->
        AppListItemCard {
            Column {
                PoderItem(
                    viewModel = viewModel,
                    poder = poder,
                    onEdit = { editando = index },
                    // ⚠️ Apagar o poder **não** apaga as vantagens dele: elas
                    // foram compradas com os pontos do personagem e continuam na
                    // ficha. É o que o `removerPoder` já garante desde o POD-5.
                    onDelete = { viewModel.removerPoder(index) }
                )
                // 🔴 Lote POD-28: as habilidades saíram da lista de Vantagens e
                // passam a viver aqui. Elas PRECISAM aparecer, com lápis e
                // lixeira: tirar da lista sem mostrar aqui deixaria a vantagem
                // comprada sem nenhum caminho até ela.
                HabilidadesDoPoderNaAba(
                    viewModel = viewModel,
                    poder = poder,
                    onEditarVantagem = onEditarVantagem,
                    onEditarDesvantagem = onEditarDesvantagem
                )
            }
        }
    }

    editando?.let { indice ->
        poderes.getOrNull(indice)?.let { poder ->
            PoderEditDialog(
                poderBase = poder,
                definicao = viewModel.dataRepository.poderes
                    .firstOrNull { it.nome == poder.nome },
                viewModelParaHabilidades = viewModel,
                onDismiss = { editando = null },
                onSave = {
                    viewModel.atualizarPoder(indice, it)
                    editando = null
                }
            )
        }
    }
}

/**
 * **As habilidades do poder, listadas debaixo dele** — Lote POD-28.
 *
 * Elas são vantagens e desvantagens de verdade, compradas com os pontos do
 * personagem — e continuam contando no total da ficha. O que mudou é **onde
 * elas aparecem**: dentro do poder a que pertencem, e não na lista geral.
 *
 * ⚠️ É assim que uma ficha de GURPS é escrita à mão: o poder é um cabeçalho, e
 * as habilidades vêm indentadas embaixo dele.
 */
@Composable
private fun HabilidadesDoPoderNaAba(
    viewModel: FichaViewModel,
    poder: Poder,
    onEditarVantagem: (Int) -> Unit,
    onEditarDesvantagem: (Int) -> Unit
) {
    val resumo = viewModel.habilidadesDoPoder(poder)
    if (resumo.habilidades.isEmpty()) return

    Column(modifier = Modifier.padding(start = RECUO_DA_HABILIDADE, top = 4.dp)) {
        resumo.habilidades.forEach { h ->
            val ehAlternativa = !h.ehDesvantagem &&
                viewModel.personagem.vantagens.getOrNull(h.indice)?.alternativaDoPoder == true
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Habilidade do poder ${poder.nome}: " +
                            "${h.nome}, ${h.custo} pontos" +
                            (if (h.ehDesvantagem) ", desvantagem exigida" else "") +
                            (if (ehAlternativa) ", alternativa" else "") + "."
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "• ${h.nome}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "${h.custo} pts" +
                            (if (h.ehDesvantagem) " · desvantagem exigida" else "") +
                            (if (ehAlternativa) " · alternativa" else ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AppBotaoIcone(
                    icone = Icons.Default.Edit,
                    descricao = "Editar a habilidade ${h.nome} do poder ${poder.nome}",
                    onClick = {
                        if (h.ehDesvantagem) onEditarDesvantagem(h.indice)
                        else onEditarVantagem(h.indice)
                    }
                )
                AppBotaoIcone(
                    icone = Icons.Default.Delete,
                    descricao = "Remover a habilidade ${h.nome} do poder ${poder.nome}",
                    onClick = {
                        // ⚠️ Aqui a lixeira APAGA mesmo, e não só desliga do
                        // poder. O X de dentro do diálogo do poder é que
                        // desliga. São ações diferentes e o rótulo diz qual é.
                        if (h.ehDesvantagem) viewModel.removerDesvantagem(h.indice)
                        else viewModel.removerVantagem(h.indice)
                    }
                )
            }
        }
    }
}

/** O recuo que mostra que a habilidade pertence ao poder de cima. */
private val RECUO_DA_HABILIDADE = 16.dp

/**
 * A linha de um poder na aba, no mesmo desenho de [VantagemItem].
 *
 * Mostra o que muda a ficha: a fonte, o percentual que vai para as habilidades,
 * quantas habilidades ele reúne e quanto ele custa em pontos.
 */
@Composable
private fun PoderItem(
    viewModel: FichaViewModel,
    poder: Poder,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val resumo = viewModel.habilidadesDoPoder(poder)
    val custo = poder.custoTotalTalento + poder.custoDaReserva
    // ⚠️ Fora do `semantics`: ele é uma lambda comum, e `resumoCurtoDoPoder` é
    // @Composable — chamada lá dentro não compila.
    val resumoCurto = resumoCurtoDoPoder(viewModel, poder)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = poder.descricaoAcessivel + ". " +
                    resumoCurto + ", " +
                    "${resumo.custoDasHabilidades} pontos em habilidades."
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                poder.nome,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (poder.fonte.isNotBlank()) {
                Text(
                    "${poder.nomeDoModificador}, ${poder.modificadorDePoder}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            // ⚠️ Duas contas separadas de propósito: o custo do poder em si
            // (Talento + Reserva) e o das habilidades, que são vantagens e já
            // aparecem na lista de cima. Somar tudo aqui contaria duas vezes.
            Text(
                buildString {
                    append(resumoCurto)
                    if (custo != 0) append(" · $custo pts em Talento e Reserva")
                    if (resumo.custoDasHabilidades != 0) {
                        append(" · ${resumo.custoDasHabilidades} pts em habilidades")
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        AppBotaoIcone(
            icone = Icons.Default.Edit,
            descricao = "Editar o poder ${poder.nome}",
            onClick = onEdit
        )
        AppBotaoIcone(
            icone = Icons.Default.Delete,
            descricao = "Remover o poder ${poder.nome}",
            onClick = onDelete
        )
    }
}
