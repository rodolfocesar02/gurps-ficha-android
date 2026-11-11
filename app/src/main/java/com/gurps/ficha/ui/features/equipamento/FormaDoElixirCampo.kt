package com.gurps.ficha.ui.features.equipamento

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gurps.ficha.ui.AppFiltroChip
import com.gurps.ficha.ui.UiEstilos
import com.gurps.ficha.ui.UiTokens

/**
 * **"Forma do elixir"** — IC-5b do `mesa-virtual/planos/itens/PLANO.md` (Magia p.213).
 *
 * Os chips são só as formas que o livro aceita para este elixir. Aparece no "Adicionar Itens" (obrigatório quando há mais de uma) e na
 * caixa de edição do item.
 */
@Composable
fun FormaDoElixirCampo(
    formas: List<String>,
    escolhida: String?,
    onEscolher: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(UiTokens.BotaoEspacamento)) {
        Text("Forma do elixir", style = UiEstilos.detalheDoItem, color = MaterialTheme.colorScheme.outline)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(UiTokens.BotaoEspacamento)
        ) {
            formas.forEach { f -> AppFiltroChip(FormaDoElixirRules.rotulo(f), escolhida == f) { onEscolher(f) } }
        }
        val ok = escolhida != null && escolhida in formas
        Text(
            if (ok) "${FormaDoElixirRules.rotulo(escolhida!!)}: ${FormaDoElixirRules.explica(escolhida)} (Magia p.213)."
            else "Escolha a forma: é ela que diz à Mesa se é beber, tomar, passar na pele ou acender.",
            style = UiEstilos.detalheDoItem,
            color = if (ok) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
        )
    }
}
