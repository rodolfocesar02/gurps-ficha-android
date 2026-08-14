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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.poderes.HabilidadesAlternativas
import com.gurps.ficha.domain.rules.poderes.HabilidadesDoPoder
import com.gurps.ficha.domain.rules.poderes.UsoDoPoder
import com.gurps.ficha.model.Poder
import com.gurps.ficha.model.PoderDefinicao
import com.gurps.ficha.ui.AppBotaoIcone
import com.gurps.ficha.ui.AppBotaoPrincipal
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
    onPedirParaLigar: () -> Unit,
    onPedirParaComprar: () -> Unit = {}
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
        val ehAlternativa = !h.ehDesvantagem &&
            viewModel.personagem.vantagens.getOrNull(h.indice)?.alternativaDoPoder == true
        AppSelectionRow(
            nome = h.nome,
            detalhe = "${h.custo} pts" +
                (if (h.ehDesvantagem) " · desvantagem" else "") +
                (if (ehAlternativa) " · alternativa" else ""),
            onClick = { },
            descricaoAcessivel = "${h.nome}, ${h.custo} pontos, " +
                (if (h.ehDesvantagem) "desvantagem" else "vantagem") +
                " ligada ao poder ${poder.nome}.",
            acoes = {
                if (!h.ehDesvantagem) {
                    // Lote POD-6: marcar a habilidade como uma das "configurações"
                    // mutuamente exclusivas do poder (p.11).
                    Checkbox(
                        checked = ehAlternativa,
                        onCheckedChange = { viewModel.marcarAlternativa(h.indice, it) }
                    )
                }
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

    // Lote POD-6: a economia do grupo alternativo, quando existe.
    val alternativas = resumo.habilidades.filterNot { it.ehDesvantagem }
        .filter { viewModel.personagem.vantagens.getOrNull(it.indice)?.alternativaDoPoder == true }
        .map { it.custo }
    if (HabilidadesAlternativas.ehGrupoValido(alternativas.size)) {
        Text(
            HabilidadesAlternativas.resumo(alternativas),
            style = UiEstilos.detalheDoItem,
            color = MaterialTheme.colorScheme.primary
        )
        HabilidadesAlternativas.INCONVENIENTES.forEach {
            Text("• $it", style = UiEstilos.detalheDoItem,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    if (resumo.quantidade > 0) {
        Text(
            "Habilidades: ${resumo.custoDasHabilidades} pts · " +
                "Talento: ${resumo.custoDoTalento} pts · Total: ${resumo.custoTotal} pts",
            style = UiEstilos.detalheDoItem,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    // Lote POD-15: o nome que o modificador leva na ficha.
    if (poder.nomeDoModificador.isNotBlank() && poder.nomeDoModificador != poder.nome) {
        Text(
            "Na ficha, as habilidades levam “${poder.nomeDoModificador}, " +
                "${poder.modificadorDePoder}%” — é o nome que o Módulo Básico dá " +
                "ao modificador deste poder (p.255).",
            style = UiEstilos.detalheDoItem,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    // Lote POD-11: o que a FONTE decide na hora de rolar.
    if (poder.fonte.isNotBlank()) {
        Text(
            UsoDoPoder.Incapacitacao.explicar(poder.fonte),
            style = UiEstilos.detalheDoItem,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    if (poder.nivelTalento > 0) {
        // ⚠️ O Talento soma em ativar, atacar, controlar e defender — e **não**
        // no dano, na reação, no teste que uma limitação exige, nem no teste do
        // alvo (p.158). A frase diz as duas metades de propósito.
        Text(
            "Talento ${poder.nivelTalento}: +${poder.nivelTalento} para ativar, atacar, " +
                "controlar e defender com estas habilidades. Não soma no dano nem no " +
                "teste do alvo (p.158).",
            style = UiEstilos.detalheDoItem,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Spacer(Modifier.height(8.dp))
    // Lote POD-14: comprar a habilidade PARA o poder, que é como o livro faz.
    // O "Ligar habilidade" continua, para quem já tem a vantagem na ficha.
    AppBotaoPrincipal("Adicionar habilidade", onPedirParaComprar, larguraTotal = true)
    Spacer(Modifier.height(4.dp))
    AppBotaoSecundario("Ligar uma que já tenho", onPedirParaLigar, larguraTotal = true)
}

/**
 * **O que o livro sugere para este poder** — Lote POD-10.
 *
 * A lista de habilidades de cada verbete (p.121-136), com os modificadores que o
 * livro já recomenda. São **567 habilidades** em 45 poderes, média de 12,6.
 *
 * ⚠️ É sugestão, não catálogo clicável: o texto do livro traz a vantagem **com o
 * modificador embutido** (*"Caminhar no Ar, com Específico, Vapor (-40%)"*), e
 * transformar isso em botão exigiria decidir por conta própria como montar cada
 * um. Aqui ele serve para o jogador saber **o que procurar** ao ligar habilidade.
 */
@Composable
fun ColumnScope.SugestoesDoLivro(definicao: PoderDefinicao?) {
    if (definicao == null) return
    if (definicao.habilidades.isEmpty() && definicao.notaDasHabilidades.isBlank()) return

    Spacer(Modifier.height(12.dp))
    Text(
        "O livro sugere (p.${definicao.pagina})",
        style = UiEstilos.subtituloDialogo
    )
    if (definicao.notaDasHabilidades.isNotBlank()) {
        Text(
            definicao.notaDasHabilidades,
            style = UiEstilos.detalheDoItem,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    if (definicao.habilidades.isNotEmpty()) {
        Text(
            definicao.habilidades.joinToString(" · "),
            style = UiEstilos.detalheDoItem
        )
    }
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
