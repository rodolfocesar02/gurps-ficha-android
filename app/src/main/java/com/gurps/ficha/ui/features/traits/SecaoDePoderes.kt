package com.gurps.ficha.ui.features.traits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
fun SecaoDePoderes(viewModel: FichaViewModel) {
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
            PoderItem(
                viewModel = viewModel,
                poder = poder,
                onEdit = { editando = index },
                // ⚠️ Apagar o poder **não** apaga as vantagens dele: elas foram
                // compradas com os pontos do personagem e continuam na ficha.
                // É o que o `removerPoder` já garante desde o POD-5.
                onDelete = { viewModel.removerPoder(index) }
            )
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
