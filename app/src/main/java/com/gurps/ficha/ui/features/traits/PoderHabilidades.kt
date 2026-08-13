package com.gurps.ficha.ui.features.traits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.poderes.HabilidadesDoPoder
import com.gurps.ficha.model.Poder
import com.gurps.ficha.ui.AppBotaoIcone
import com.gurps.ficha.ui.AppBotaoSecundario
import com.gurps.ficha.ui.AppSelectionDialog
import com.gurps.ficha.ui.AppSelectionRow
import com.gurps.ficha.ui.UiEstilos
import com.gurps.ficha.ui.contadorDe
import com.gurps.ficha.viewmodel.FichaViewModel

/**
 * **As habilidades do poder, dentro do diálogo do poder** — Lote POD-5.
 *
 * ## 🔴 O que faltava
 *
 * A ligação vantagem→poder **já existia** (`vantagem.poderId`), mas só dava para
 * chegar nela **pelo lado da vantagem**: abrir a vantagem, achar o seletor de
 * poder lá dentro. Quem abria o poder não via habilidade nenhuma.
 *
 * O resultado é que o poder era um rótulo solto — nome, fonte, percentual — e o
 * jogador não tinha como saber o que ele reunia. No livro é o contrário: o poder
 * **é** o conjunto das habilidades (Poderes, p.34).
 *
 * ⚠️ Arquivo separado do `DialogsPoderes.kt` de propósito: aquele já passa de
 * 400 linhas e o teto do projeto é 1000. Painel novo nasce ao lado, não dentro.
 */
@Composable
fun ColumnScope.PainelDeHabilidades(
    viewModel: FichaViewModel,
    poder: Poder,
    onPedirParaLigar: () -> Unit
) {
    val resumo = viewModel.habilidadesDoPoder(poder)

    Text(
        "Habilidades — ${contadorDe(resumo.quantidade, "vantagem ligada", "vantagens ligadas")}",
        style = UiEstilos.subtituloDialogo
    )

    HabilidadesDoPoder.avisoDePoderVazio(resumo)?.let { aviso ->
        // Não é erro, e por isso não é vermelho: o livro permite comprar o
        // Talento antes das habilidades (p.8).
        Text(
            aviso,
            style = UiEstilos.detalheDoItem,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    resumo.habilidades.forEach { h ->
        AppSelectionRow(
            nome = h.nome,
            detalhe = "${h.custo} pts" + if (h.ehDesvantagem) " · desvantagem" else "",
            onClick = { },
            descricaoAcessivel = "${h.nome}, ${h.custo} pontos, " +
                (if (h.ehDesvantagem) "desvantagem" else "vantagem") +
                " ligada ao poder ${poder.nome}.",
            acoes = {
                AppBotaoIcone(
                    icone = Icons.Default.Clear,
                    descricao = "Desligar ${h.nome} do poder ${poder.nome}",
                    onClick = {
                        // ⚠️ Desligar NÃO apaga a vantagem: ela existia antes do
                        // poder e continua existindo, só deixa de pertencer a ele.
                        if (h.ehDesvantagem) viewModel.vincularDesvantagemPoder(h.indice, null)
                        else viewModel.vincularVantagemPoder(h.indice, null)
                    }
                )
            }
        )
    }

    if (resumo.quantidade > 0) {
        Text(
            "Habilidades: ${resumo.custoDasHabilidades} pts · " +
                "Talento: ${resumo.custoDoTalento} pts · Total: ${resumo.custoTotal} pts",
            style = UiEstilos.detalheDoItem,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Spacer(Modifier.height(8.dp))
    AppBotaoSecundario("Ligar habilidade", onPedirParaLigar, larguraTotal = true)
}

/**
 * A escolha de qual vantagem/desvantagem da ficha vira habilidade deste poder.
 *
 * ⚠️ Só lista o que **ainda não pertence a nenhum poder**. O livro não proíbe
 * mudar de poder, mas oferecer uma habilidade que já é de outro poder na mesma
 * lista faria o jogador roubar de si mesmo sem perceber.
 */
@Composable
fun LigarHabilidadeDialog(
    viewModel: FichaViewModel,
    poder: Poder,
    onDismiss: () -> Unit
) {
    var busca by remember { mutableStateOf("") }
    val p = viewModel.personagem

    data class Candidata(val indice: Int, val nome: String, val custo: Int, val ehDesvantagem: Boolean)

    val livres = buildList {
        p.vantagens.forEachIndexed { i, v ->
            if (v.poderId == null) add(Candidata(i, v.nome, v.custoFinal, false))
        }
        p.desvantagens.forEachIndexed { i, d ->
            if (d.poderId == null) add(Candidata(i, d.nome, d.custoFinal, true))
        }
    }.filter { busca.isBlank() || it.nome.contains(busca, ignoreCase = true) }

    AppSelectionDialog(
        titulo = "Ligar habilidade a ${poder.nome}",
        subtitulo = "Fonte ${poder.fonte} · ${poder.modificadorDePoder}% aplicado ao custo",
        busca = busca,
        onBusca = { busca = it },
        rotuloDaBusca = "Buscar na ficha...",
        contador = contadorDe(livres.size, "traço livre", "traços livres"),
        onDismiss = onDismiss
    ) {
        items(livres) { c ->
            AppSelectionRow(
                nome = c.nome,
                detalhe = if (c.ehDesvantagem) "desvantagem · ${c.custo} pts" else "${c.custo} pts",
                detalheADireita = "${if (poder.modificadorDePoder > 0) "+" else ""}${poder.modificadorDePoder}%",
                onClick = {
                    if (c.ehDesvantagem) viewModel.vincularDesvantagemPoder(c.indice, poder.id)
                    else viewModel.vincularVantagemPoder(c.indice, poder.id)
                    onDismiss()
                },
                descricaoAcessivel = "${c.nome}. Ligar ao poder ${poder.nome}, " +
                    "aplicando " +
                    (if (poder.modificadorDePoder < 0) "menos ${-poder.modificadorDePoder}"
                     else "${poder.modificadorDePoder}") + " por cento ao custo."
            )
        }
    }
}

/** Quantas habilidades este poder reúne — para a linha da lista de poderes. */
@Composable
fun resumoCurtoDoPoder(viewModel: FichaViewModel, poder: Poder): String {
    val r = viewModel.habilidadesDoPoder(poder)
    return if (r.quantidade == 0) "sem habilidades"
    else contadorDe(r.quantidade, "habilidade", "habilidades")
}
