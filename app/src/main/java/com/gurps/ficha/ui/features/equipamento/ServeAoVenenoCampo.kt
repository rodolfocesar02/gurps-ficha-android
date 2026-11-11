package com.gurps.ficha.ui.features.equipamento

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.gurps.ficha.model.ServeAoVeneno
import com.gurps.ficha.ui.AppFiltroChip
import com.gurps.ficha.ui.UiEstilos
import com.gurps.ficha.ui.UiTokens

/**
 * **"Serve para (o veneno deste kit)"** — IC-4B do `mesa-virtual/planos/itens/PLANO.md`.
 *
 * Os chips: os venenos do livro (do catálogo de itens), os que o personagem já tem no inventário, e "Outro" para a toxina que o Mestre
 * inventou (digitando o nome). Um só por kit: para dois venenos, dois kits (MB p.289: "antídoto para um veneno específico").
 *
 * Aparece na caixa de edição do item e no "Adicionar Itens" (aí a escolha é obrigatória: o botão Adicionar fica apagado até escolher).
 */
@Composable
fun ServeAoVenenoCampo(
    escolhido: ServeAoVeneno?,
    onEscolher: (ServeAoVeneno?) -> Unit,
    venenosDoLivro: List<ServeAoVeneno>,
    venenosDoInventario: List<ServeAoVeneno>
) {
    val opcoes = remember(venenosDoLivro, venenosDoInventario) { ServeAoVenenoRules.opcoes(venenosDoLivro, venenosDoInventario) }
    val ehOutro = escolhido != null && escolhido.escolhido && opcoes.none { ServeAoVenenoRules.mesmo(it, escolhido) }
    var digitando by remember { mutableStateOf(ehOutro) }
    var texto by remember { mutableStateOf(if (ehOutro) escolhido?.nome.orEmpty() else "") }

    Column(verticalArrangement = Arrangement.spacedBy(UiTokens.BotaoEspacamento)) {
        Text(
            "Serve para (o veneno deste kit)",
            style = UiEstilos.detalheDoItem, color = MaterialTheme.colorScheme.outline
        )
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(UiTokens.BotaoEspacamento)
        ) {
            opcoes.forEach { o ->
                AppFiltroChip(o.nome, !digitando && escolhido != null && ServeAoVenenoRules.mesmo(o, escolhido)) {
                    digitando = false
                    onEscolher(o)
                }
            }
            AppFiltroChip("Outro (digitar)", digitando) {
                digitando = true
                onEscolher(texto.trim().takeIf { it.isNotEmpty() }?.let { ServeAoVeneno(null, it) })
            }
        }
        if (digitando) {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it.take(60); onEscolher(texto.trim().takeIf { t -> t.isNotEmpty() }?.let { t -> ServeAoVeneno(null, t) }) },
                label = { androidx.compose.material3.Text("Nome do veneno ou da toxina") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Text(
            if (escolhido?.escolhido == true) "Este kit só vale contra ${escolhido.nome}; contra outro veneno a Mesa não deixa usar (MB p.289)."
            else "Escolha o veneno: sem isso a Mesa não deixa usar o kit.",
            style = UiEstilos.detalheDoItem,
            color = if (escolhido?.escolhido == true) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
        )
    }
}
