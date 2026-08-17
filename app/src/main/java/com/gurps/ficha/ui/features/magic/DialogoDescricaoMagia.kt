package com.gurps.ficha.ui.features.magic

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gurps.ficha.ui.rolagemVertical

/**
 * Pop-up ÚNICO de descrição de mágica — a regra fiel do livro, com **barra de rolagem**.
 *
 * Existia um pop-up igual embutido no diálogo de edição da aba de Magias, mas: (a) não dava para
 * reusar (era código solto dentro de outro composable) e (b) **não rolava** — descrição longa
 * (Aporte, Relâmpago) ficava cortada. Este componente resolve os dois e passa a atender os dois
 * palcos: a aba de Magias e o diálogo "Conjurar magia" DENTRO do combate.
 *
 * Motivação (usuário, teste no aparelho): *"se eu quiser ler o que a magia faz, custo, tempo de
 * operação, eu preciso sair do combate, ir na aba de magias, clicar na magia, clicar no nome"* — no
 * meio de uma luta esse caminho custa o ritmo do jogo. Agora é segurar o card por 2 segundos.
 *
 * A [fichaTecnica] é a linha de cabeçalho (classe · NH · custo · duração · tempo de operação), que
 * responde à pergunta prática sem precisar ler a descrição inteira.
 */
@Composable
fun DialogoDescricaoMagia(
    nome: String,
    descricao: String?,
    onFechar: () -> Unit,
    fichaTecnica: String? = null,
) {
    val texto = remember(descricao) { descricao?.trim().orEmpty() }
    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text(nome, color = MaterialTheme.colorScheme.primary) },
        text = {
            // heightIn + verticalScroll: o diálogo não estoura a tela e a descrição longa ROLA.
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .rolagemVertical()
            ) {
                if (!fichaTecnica.isNullOrBlank()) {
                    Text(
                        fichaTecnica,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Text(
                    text = texto.ifBlank { "Sem descrição disponível." },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onFechar,
                modifier = Modifier.semantics { contentDescription = "Fechar descrição da magia" },
            ) { Text("Fechar") }
        },
    )
}
