package com.gurps.ficha.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.gson.Gson
import com.gurps.ficha.domain.rules.CartaoDoItem
import com.gurps.ficha.model.CatalogoDeMontarias
import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.model.MontariaDados
import com.gurps.ficha.model.TipoEquipamento

/**
 * **As montarias no inventário** — RG-11 do `mesa-virtual/planos/regras/PLANO.md` (MB pp.459-460).
 *
 * "Adicionar Montaria" abre a lista do livro (`assets/montarias.v1.json`) com a ficha técnica de cada animal; escolhida,
 * ela entra no inventário como item do tipo MONTARIA, **com peso 0** (o animal não pesa na carga de quem o tem) e com a
 * ficha técnica inteira dentro (`Equipamento.montaria`): é assim que a Mesa sabe que cavalo é, para pôr no mapa.
 */

/** Lê o catálogo uma vez por chamada (é pequeno: 12 animais). */
fun carregarMontarias(context: Context): List<MontariaDados> = try {
    context.assets.open("montarias.v1.json").bufferedReader().use { Gson().fromJson(it, CatalogoDeMontarias::class.java) }.montarias
} catch (_: Exception) {
    emptyList()
}

/** A montaria vira item do inventário: peso 0, o custo do livro, a ficha técnica dentro. */
fun equipamentoDaMontaria(m: MontariaDados): Equipamento = Equipamento(
    nome = m.nome, peso = 0f, custo = m.custo, quantidade = 1, tipo = TipoEquipamento.MONTARIA,
    notas = "MB p.${m.pagina}", montaria = m
)

private fun virgula(f: Float): String = if (f % 1f == 0f) f.toInt().toString() else f.toString().replace('.', ',')

/** As linhas da ficha técnica, as mesmas no catálogo e no inventário. */
fun linhasDaMontaria(m: MontariaDados): List<CartaoDoItem.Linha> = buildList {
    add(CartaoDoItem.Linha("ST ${m.st} · DX ${m.dx} · IQ ${m.iq} · HT ${m.ht} · PV ${m.pv}", CartaoDoItem.Papel.NEUTRO))
    add(CartaoDoItem.Linha("Vontade ${m.vontade} · Per ${m.percepcao} · Velocidade ${virgula(m.velocidadeBasica)}", CartaoDoItem.Papel.NEUTRO))
    add(CartaoDoItem.Linha("Esquiva ${m.esquiva} · Deslocamento ${m.deslocamento}" + if (m.rd > 0) " · RD ${m.rd}" else "", CartaoDoItem.Papel.PROTECAO))
    add(CartaoDoItem.Linha("MT ${if (m.mt >= 0) "+" else ""}${m.mt} (${m.hexes} hexágonos) · ${virgula(m.pesoKg)} kg", CartaoDoItem.Papel.NEUTRO))
    if (m.treinadaParaGuerra) add(CartaoDoItem.Linha("Treinada para a guerra", CartaoDoItem.Papel.DANO))
    if (!m.montavel) add(CartaoDoItem.Linha("Animal de carga: não se monta", CartaoDoItem.Papel.ALERTA))
    if (m.pericias.isNotEmpty()) add(CartaoDoItem.Linha("Perícias: " + m.pericias.joinToString("; "), CartaoDoItem.Papel.NEUTRO))
    if (m.caracteristicas.isNotEmpty()) add(CartaoDoItem.Linha(m.caracteristicas.joinToString("; "), CartaoDoItem.Papel.NEUTRO))
    add(CartaoDoItem.Linha("Custo: $${virgula(m.custo)} · MB p.${m.pagina}", CartaoDoItem.Papel.CUSTO))
}

@Composable
fun SelecionarMontariaDialog(onDismiss: () -> Unit, onSelect: (MontariaDados) -> Unit) {
    val context = LocalContext.current
    val todas = remember { carregarMontarias(context) }
    var busca by remember { mutableStateOf("") }
    val lista = todas.filter { busca.isBlank() || it.nome.contains(busca, ignoreCase = true) }
    AppSelectionDialog(
        titulo = "Selecionar Montaria",
        subtitulo = "Animais de Montaria e Carga (MB pp.459-460). Não pesam na sua carga.",
        busca = busca,
        onBusca = { busca = it },
        rotuloDaBusca = "Buscar montaria",
        contador = contadorDe(lista.size, "montaria", "montarias"),
        onDismiss = onDismiss
    ) {
        items(lista, key = { it.id }) { m ->
            AppSelectionRow(
                nome = m.nome,
                detalhe = "ST ${m.st} · Desl. ${m.deslocamento} · Esquiva ${m.esquiva} · ${m.hexes} hex · $${virgula(m.custo)}",
                descricaoAcessivel = m.nome + ". " + linhasDaMontaria(m).joinToString(". ") { it.texto },
                onClick = { onSelect(m) },
                extra = { CorpoDoItemPadrao(linhasDaMontaria(m).drop(4)) }
            )
        }
    }
}

/** O cartão da montaria no inventário: a ficha técnica e o remover. */
@Composable
fun MontariaItem(equipamento: Equipamento, onDelete: () -> Unit) {
    val m = equipamento.montaria
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            NomeDoItemPadrao(equipamento.nome)
            if (m != null) {
                CorpoDoItemPadrao(linhasDaMontaria(m))
            } else {
                Text("Sem a ficha técnica: remova e adicione de novo.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Remover montaria ${equipamento.nome}") }
    }
}
