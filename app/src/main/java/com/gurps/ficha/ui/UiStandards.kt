package com.gurps.ficha.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private const val COMPACT_SCREEN_MAX_WIDTH_DP = 360

object UiTokens {
    val ScreenPadding = 12.dp
    val SectionSpacing = 8.dp
    val ItemSpacing = 4.dp
    val CardPaddingHorizontal = 12.dp
    val CardPaddingVertical = 10.dp
    val CardElevation = 1.dp

    /**
     * O tamanho mínimo de um alvo de toque.
     *
     * ⚠️ Estava declarado aqui e era usado **zero vezes** até o Lote LAYOUT-1b —
     * enquanto isso havia botões de 32 e 36 dp espalhados, abaixo do mínimo, num
     * app que tem variante para quem não enxerga a tela.
     */
    val TouchMinHeight = 48.dp
    val DialogContentSpacing = 10.dp

    // ── Lote LAYOUT-1: a lista de seleção ──
    /**
     * O respiro dentro de uma linha de lista.
     *
     * ⚠️ Os seis diálogos de seleção escreviam `8.dp / 6.dp` à mão, cada um numa
     * cópia do mesmo `Card + Row`. Era daí que vinha a diferença de tamanho entre
     * o card de perícia e o de vantagem.
     */
    val LinhaDeListaPaddingH = 12.dp
    val LinhaDeListaPaddingV = 8.dp

    /** O espaço entre duas linhas da lista. */
    val LinhaDeListaSpacing = 2.dp

    // ── Lote LAYOUT-1b: os botões ──
    /** Altura de todo botão de ação. Igual ao [TouchMinHeight], e por isso mesmo. */
    val BotaoAltura = 48.dp

    /**
     * O espaço entre dois botões vizinhos.
     *
     * ⚠️ Nenhum lugar do app usava `spacedBy` numa fileira de botões: o espaço
     * entre *Cancelar* e *Salvar* era o que sobrasse, e mudava com o tamanho do
     * texto de cada um.
     */
    val BotaoEspacamento = 8.dp

    /** O respiro dentro do botão. Substitui dez combinações escritas à mão. */
    val BotaoPaddingH = 16.dp
    val BotaoPaddingV = 8.dp

    /** A distância entre a fileira de botões e a borda do diálogo. */
    val BotaoMargemDaBorda = 12.dp
}

/**
 * Os estilos de texto do app, em um lugar só (Lote LAYOUT-1).
 *
 * Existem para ninguém mais escolher `titleMedium` ou `bodySmall` no olho — foi
 * assim que a mesma informação acabou em seis tamanhos diferentes.
 */
object UiEstilos {
    /** O título de um diálogo. */
    val tituloDialogo: TextStyle
        @Composable get() = MaterialTheme.typography.titleLarge

    /** A linha logo abaixo do título ("ST do personagem: 11", "Atual: 0 pts"). */
    val subtituloDialogo: TextStyle
        @Composable get() = MaterialTheme.typography.labelMedium

    /** O nome do item numa lista de seleção. */
    val nomeDoItem: TextStyle
        @Composable get() = MaterialTheme.typography.titleMedium

    /** A informação secundária do item (custo, página, atributo). */
    val detalheDoItem: TextStyle
        @Composable get() = MaterialTheme.typography.bodySmall

    /**
     * O texto dentro de um botão.
     *
     * ⚠️ Antes do LAYOUT-1b havia botão com `displaySmall` e `headlineMedium` —
     * tamanho de manchete dentro de um botão.
     */
    val textoDeBotao: TextStyle
        @Composable get() = MaterialTheme.typography.labelLarge
}

@Composable
fun rememberIsCompactScreen(): Boolean = LocalConfiguration.current.screenWidthDp <= COMPACT_SCREEN_MAX_WIDTH_DP

@Composable
fun StandardTabColumn(
    modifier: Modifier = Modifier,
    contentSpacing: androidx.compose.ui.unit.Dp = UiTokens.ItemSpacing,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(UiTokens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(contentSpacing),
        content = content
    )
}

@Composable
fun appCardColors() = CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.secondaryContainer
)

@Composable
fun PrimaryActionButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = text },
        enabled = enabled
    ) { Text(text) }
}

@Composable
fun SummaryFooterCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    val isCompactScreen = rememberIsCompactScreen()
    val titleStyle = if (isCompactScreen) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = appCardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = UiTokens.CardElevation)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = UiTokens.CardPaddingHorizontal,
                vertical = UiTokens.CardPaddingVertical
            ),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                title,
                style = titleStyle,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.semantics { heading() }
            )
            content()
        }
    }
}

@Composable
fun AppListItemCard(
    modifier: Modifier = Modifier,
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = appCardColors(),
        border = border
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = UiTokens.CardPaddingHorizontal,
                vertical = UiTokens.CardPaddingVertical
            ),
            content = content
        )
    }
}

@Composable
fun StandardDialogColumn(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing),
        content = content
    )
}

@Composable
fun GuidedEmptyState(
    titulo: String,
    orientacao: String
) {
    AppListItemCard {
        Text(
            text = titulo,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = orientacao,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
