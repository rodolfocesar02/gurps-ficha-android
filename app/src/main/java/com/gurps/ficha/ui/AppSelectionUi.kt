package com.gurps.ficha.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * **A moldura dos diálogos de seleção** (Lote LAYOUT-1).
 *
 * ## Por que existe
 *
 * Havia **seis** diálogos de seleção — vantagem, desvantagem, perícia, técnica,
 * arma, armadura — e cada um escrevia à mão o mesmo bloco:
 *
 * ```
 * Card(modifier = …, elevation = CardDefaults.cardElevation(1.dp)) {
 *     Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), …)
 * ```
 *
 * Seis cópias. Cada uma foi ajustada uma vez e nunca as outras — e é daí que vinha
 * a diferença de tamanho entre o card de perícia e o de vantagem que o usuário
 * notou nos prints de 03/08.
 *
 * ⚠️ Havia `AppListItemCard` e `UiTokens` desde sempre, e **nenhum dos seis usava**.
 * O padrão estava no arquivo e a tela estava fora dele. Por isso este lote vem com
 * um teste que lê o código-fonte: componente que ninguém é obrigado a usar não é
 * padrão, é sugestão.
 *
 * ## O que NÃO faz
 *
 * Não obriga ninguém a caber. A ficha da arma tem quatro linhas, a magia tem o
 * aviso vermelho de pré-requisito — [AppSelectionRow] recebe um bloco livre para o
 * extra. Padrão que não acomoda a exceção vira gambiarra na primeira exceção.
 */

/**
 * O diálogo de seleção inteiro: título, subtítulo, busca, filtros, contador,
 * lista e o botão de fechar.
 *
 * @param contador o texto do contador já pronto — use [contadorDe] para acertar o
 *   plural sem pensar.
 */
@Composable
fun AppSelectionDialog(
    titulo: String,
    onDismiss: () -> Unit,
    subtitulo: String? = null,
    busca: String? = null,
    onBusca: ((String) -> Unit)? = null,
    rotuloDaBusca: String = "Buscar...",
    contador: String? = null,
    filtros: (@Composable RowScope.() -> Unit)? = null,
    filtrosSecundarios: (@Composable RowScope.() -> Unit)? = null,
    /**
     * Controles de largura inteira entre a busca e a lista.
     *
     * ⚠️ Existe pela tela de **mágicas**: ela tem um seletor de escola (lista
     * suspensa, porque são dezenas) e o interruptor do *Modo Alvo*. Nenhum dos
     * outros diálogos tem isso, e nenhum dos dois cabe numa fileira de chips —
     * forçá-los a virar chip seria empobrecer a tela para caber no molde.
     */
    cabecalhoExtra: (@Composable ColumnScope.() -> Unit)? = null,
    conteudo: LazyListScope.() -> Unit
) {
    FullscreenDialogContainer(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                titulo,
                style = UiEstilos.tituloDialogo,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() }
            )
            subtitulo?.let {
                Text(it, style = UiEstilos.subtituloDialogo, color = MaterialTheme.colorScheme.outline)
            }

            if (busca != null && onBusca != null) {
                Spacer(Modifier.height(UiTokens.SectionSpacing))
                OutlinedTextField(
                    value = busca,
                    onValueChange = onBusca,
                    label = { Text(rotuloDaBusca) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
            }

            filtros?.let { conteudoDosFiltros ->
                Spacer(Modifier.height(UiTokens.SectionSpacing))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.BotaoEspacamento),
                    verticalAlignment = Alignment.CenterVertically,
                    content = conteudoDosFiltros
                )
            }
            filtrosSecundarios?.let { conteudoDosFiltros ->
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.BotaoEspacamento),
                    verticalAlignment = Alignment.CenterVertically,
                    content = conteudoDosFiltros
                )
            }

            cabecalhoExtra?.let {
                Spacer(Modifier.height(UiTokens.ItemSpacing))
                it()
            }

            contador?.let {
                Spacer(Modifier.height(UiTokens.SectionSpacing))
                Text(it, style = UiEstilos.detalheDoItem, color = MaterialTheme.colorScheme.outline)
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(UiTokens.LinhaDeListaSpacing),
                content = conteudo
            )

            AppFileiraDeBotoes {
                AppBotaoDiscreto(texto = UiActionLabels.FECHAR, onClick = onDismiss)
            }
        }
    }
}

/**
 * Uma linha da lista de seleção.
 *
 * O [detalhe] vai **abaixo** do nome; o [detalheADireita] vai na mesma linha, à
 * direita — é a diferença entre o card de vantagem ("10 pts | por nível | pag.
 * 34") e o de perícia ("DX/D" encostado na borda). As duas formas existem porque
 * as duas fazem sentido, mas agora com o mesmo padding e a mesma altura.
 *
 * [extra] é o bloco livre: as linhas de dano da arma, o aviso vermelho da magia.
 */
@Composable
fun AppSelectionRow(
    nome: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    detalhe: String? = null,
    detalheADireita: String? = null,
    habilitado: Boolean = true,
    descricaoAcessivel: String? = null,
    extra: (@Composable ColumnScope.() -> Unit)? = null,
    /**
     * Botões no fim da linha — lápis, lixeira.
     *
     * ⚠️ Existe porque a lista de **poderes já na ficha** tem ações na linha, e
     * sem este espaço ela seria a sétima cópia do `Card + Row` à mão. Padrão que
     * não acomoda a exceção vira gambiarra na primeira exceção — e esta apareceu
     * no mesmo dia em que o padrão nasceu.
     */
    acoes: (@Composable RowScope.() -> Unit)? = null
) {
    val acessivel = descricaoAcessivel
        ?: listOfNotNull(nome, detalhe, detalheADireita).joinToString(". ")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = habilitado, onClick = onClick)
            .semantics { contentDescription = acessivel },
        colors = appCardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = UiTokens.CardElevation)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(
                horizontal = UiTokens.LinhaDeListaPaddingH,
                vertical = UiTokens.LinhaDeListaPaddingV
            ),
            horizontalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    nome,
                    style = UiEstilos.nomeDoItem,
                    fontWeight = if (habilitado) FontWeight.Medium else FontWeight.Normal,
                    color = if (habilitado) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
                detalhe?.let {
                    Text(
                        it,
                        style = UiEstilos.detalheDoItem,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                extra?.invoke(this)
            }
            detalheADireita?.let {
                Text(
                    it,
                    style = UiEstilos.detalheDoItem,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            acoes?.invoke(this)
        }
    }
}

/** Um chip de filtro no padrão. Existe para nenhuma tela voltar ao texto solto. */
@Composable
fun AppFiltroChip(
    rotulo: String,
    selecionado: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selecionado,
        onClick = onClick,
        modifier = modifier,
        // ⚠️ Uma linha só, e o rótulo centralizado. Sem isto o chip encolhe até o
        // tamanho do texto: numa grade, "Pé" ficava com um terço da largura de
        // "Extrem. perf. ×2" e a tela virava um mosaico torto.
        label = {
            Text(
                rotulo,
                style = UiEstilos.textoDeBotao,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

/**
 * O contador da lista, com o plural certo.
 *
 * ⚠️ Antes deste lote: vantagem e perícia diziam *"N encontradas"*, armadura dizia
 * *"Resultados: N"*, e desvantagem, arma e magia **não diziam nada**. Três formas
 * e três silêncios para a mesma informação.
 */
fun contadorDe(quantidade: Int, singular: String, plural: String): String =
    if (quantidade == 1) "1 $singular encontrad${terminacao(singular)}"
    else "$quantidade $plural encontrad${terminacao(plural)}s"

private fun terminacao(palavra: String): String =
    if (palavra.endsWith("a") || palavra.endsWith("as")) "a" else "o"
