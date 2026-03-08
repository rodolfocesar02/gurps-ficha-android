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
    val TouchMinHeight = 48.dp
    val DialogContentSpacing = 10.dp
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
