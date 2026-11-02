package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.gurps.ficha.domain.magic.FaixaColuna
import com.gurps.ficha.domain.magic.FaixaDoLivro
import com.gurps.ficha.ui.UiEstilos
import com.gurps.ficha.ui.UiTokens

/**
 * P-9 — a faixa **Operar / Dura / Manter** do diálogo de energia: o que o livro diz da magia, em três colunas (a mesma do cartão da Mesa).
 * Os textos vêm prontos do catálogo; o "Manter" é refeito pelo diálogo quando o jogador escolhe o custo (`FaixaDoLivro.colunas`).
 *
 * Para quem ouve a tela, as três colunas viram **uma frase só** ("Operar 3 s, tempo de concentração. Dura 1 minuto, ..."), como na Mesa.
 */
@Composable
fun FaixaDoLivroLinha(colunas: List<FaixaColuna>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = "Do livro: " + FaixaDoLivro.fala(colunas) },
        horizontalArrangement = Arrangement.spacedBy(UiTokens.SectionSpacing)
    ) {
        colunas.forEach { c ->
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(UiTokens.ItemSpacing / 2)) {
                Text(c.rotulo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(c.valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                if (c.nota.isNotBlank()) {
                    Text(c.nota, style = UiEstilos.detalheDoItem, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
