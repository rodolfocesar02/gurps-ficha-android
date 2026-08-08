package com.gurps.ficha.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * **Os botões do app** (Lote LAYOUT-1b).
 *
 * ## O que estava acontecendo
 *
 * Medido no código de `ui/` antes deste lote: **sete** tipos de botão convivendo —
 * `TextButton` (216), `Button` puro (72), `IconButton` (62), `OutlinedButton` (32),
 * `FilledTonalButton` (4), `ElevatedButton` (4) — e o componente que existia
 * justamente para padronizar, o `PrimaryActionButton`, era o **menos usado de
 * todos** (15).
 *
 * Junto com isso:
 *
 * - **Seis alturas** escritas à mão: 32, 36, 42, 48, 50 e 56 dp.
 * - **Dez** combinações de `contentPadding` (`8/2`, `4/0`, `10/0`, `6/0`, `2/0`…).
 * - **Seis** estilos de texto dentro de botão, incluindo `displaySmall` e
 *   `headlineMedium` — tamanho de manchete.
 * - **Nenhum** `spacedBy` numa fileira de botões: o espaço entre *Cancelar* e
 *   *Salvar* era o que sobrasse, e mudava com o tamanho do texto.
 * - **Nove** lugares forçando `ButtonDefaults.buttonColors(...)` fora do tema.
 *
 * ## 🔴 E o token de acessibilidade existia sem uso
 *
 * `UiTokens.TouchMinHeight = 48.dp` estava declarado e era referenciado **zero
 * vezes**. Enquanto isso havia botões de **32 e 36 dp** espalhados — abaixo do
 * mínimo de toque, num app que tem uma variante inteira para quem não enxerga a
 * tela e navega por toque e TalkBack.
 *
 * ## Quatro papéis, não sete tipos
 *
 * A escolha passa a ser sobre **o que o botão faz**, não sobre qual widget do
 * Material usar:
 *
 * | Papel | Quando |
 * |---|---|
 * | [AppBotaoPrincipal] | a ação que a tela existe para fazer — Adicionar, Salvar, Rolar |
 * | [AppBotaoSecundario] | alternativa legítima — Cancelar, Voltar |
 * | [AppBotaoDiscreto] | saída ou apoio — Fechar, Limpar filtros |
 * | [AppBotaoIcone] | lápis, lixeira, `−`/`+` |
 *
 * ⚠️ **A cor sai do tema.** Não há parâmetro de cor de propósito: cor escolhida no
 * olho quebra o modo escuro sem ninguém perceber. Ação destrutiva usa
 * [AppBotaoDestrutivo], que é a única exceção e tem motivo escrito.
 */

/** A ação principal da tela. Preenchido, cor do tema, altura de toque cheia. */
@Composable
fun AppBotaoPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    larguraTotal: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .then(if (larguraTotal) Modifier.fillMaxWidth() else Modifier)
            .defaultMinSize(minHeight = UiTokens.BotaoAltura)
            .semantics { contentDescription = texto },
        enabled = enabled,
        contentPadding = PaddingValues(
            horizontal = UiTokens.BotaoPaddingH,
            vertical = UiTokens.BotaoPaddingV
        )
    ) { Text(texto, style = UiEstilos.textoDeBotao) }
}

/** A alternativa legítima à ação principal: Cancelar, Voltar. Contornado. */
@Composable
fun AppBotaoSecundario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    larguraTotal: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .then(if (larguraTotal) Modifier.fillMaxWidth() else Modifier)
            .defaultMinSize(minHeight = UiTokens.BotaoAltura)
            .semantics { contentDescription = texto },
        enabled = enabled,
        contentPadding = PaddingValues(
            horizontal = UiTokens.BotaoPaddingH,
            vertical = UiTokens.BotaoPaddingV
        )
    ) { Text(texto, style = UiEstilos.textoDeBotao) }
}

/** Saída ou apoio: Fechar, Limpar filtros. Só texto — mas com a altura de toque. */
@Composable
fun AppBotaoDiscreto(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = UiTokens.BotaoAltura)
            .semantics { contentDescription = texto },
        enabled = enabled,
        contentPadding = PaddingValues(
            horizontal = UiTokens.BotaoPaddingH,
            vertical = UiTokens.BotaoPaddingV
        )
    ) { Text(texto, style = UiEstilos.textoDeBotao) }
}

/**
 * Ação **destrutiva** — a única exceção de cor.
 *
 * Vermelho aqui não é enfeite: é a diferença entre "salvar" e "apagar para
 * sempre". Qualquer outra cor de botão fora do tema é defeito.
 */
@Composable
fun AppBotaoDestrutivo(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = UiTokens.BotaoAltura)
            .semantics { contentDescription = texto },
        enabled = enabled,
        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.error
        ),
        contentPadding = PaddingValues(
            horizontal = UiTokens.BotaoPaddingH,
            vertical = UiTokens.BotaoPaddingV
        )
    ) { Text(texto, style = UiEstilos.textoDeBotao) }
}

/**
 * Botão de ícone — lápis, lixeira, `−`/`+`.
 *
 * ⚠️ [descricao] é **obrigatória**, não opcional. Um ícone sem rótulo é um botão
 * mudo para o TalkBack, e a variante `pracego` depende inteiramente disso.
 */
@Composable
fun AppBotaoIcone(
    icone: ImageVector,
    descricao: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(UiTokens.BotaoAltura),
        enabled = enabled
    ) {
        Icon(icone, contentDescription = descricao)
    }
}

/**
 * Um botão de **passo** (`−` / `+`), com a altura de toque cheia.
 *
 * ⚠️ Existe por causa de um defeito real: no diálogo de adicionar vantagem, na
 * variante **visual**, o nível só mudava **arrastando o dedo** — um gesto sem
 * nenhuma dica na tela. Os botões existiam apenas no ramo do `pracego`. Achado
 * pelo usuário nos prints do Abafador de Mana em 03/08.
 */
@Composable
fun AppBotaoPasso(
    sinal: String,
    descricao: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .defaultMinSize(minWidth = UiTokens.BotaoAltura, minHeight = UiTokens.BotaoAltura)
            .semantics { contentDescription = descricao },
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = UiTokens.BotaoPaddingV)
    ) {
        Text(sinal, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
    }
}

/**
 * A fileira de botões no rodapé de um diálogo.
 *
 * Alinha à direita, separa por [UiTokens.BotaoEspacamento] e afasta da borda por
 * [UiTokens.BotaoMargemDaBorda] — os três números que cada tela escolhia sozinha.
 */
@Composable
fun AppFileiraDeBotoes(
    modifier: Modifier = Modifier,
    alinhamento: Arrangement.Horizontal = Arrangement.End,
    conteudo: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = UiTokens.BotaoMargemDaBorda),
        horizontalArrangement = if (alinhamento == Arrangement.End) {
            Arrangement.spacedBy(UiTokens.BotaoEspacamento, Alignment.End)
        } else {
            Arrangement.spacedBy(UiTokens.BotaoEspacamento, Alignment.CenterHorizontally)
        },
        verticalAlignment = Alignment.CenterVertically,
        content = conteudo
    )
}
