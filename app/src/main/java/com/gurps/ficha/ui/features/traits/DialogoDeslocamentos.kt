package com.gurps.ficha.ui.features.traits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.DeslocamentosRules
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.ui.FullscreenDialogContainer
import com.gurps.ficha.ui.UiActionLabels
import com.gurps.ficha.ui.appCardColors

/**
 * **Deslocamentos** — a lista inteira, só leitura (Lote DESL-2).
 *
 * Ideia do usuário no T-L7: em vez de espremer *Voando · Escalando · Nadando* na
 * linha de Características Derivadas, um botão **"Desloc."** abre tudo.
 *
 * ## ⚠️ Só leitura, de propósito
 *
 * Decisão do usuário: *"ele não seleciona um tipo e fica fixo, apenas leitura"*.
 * Um deslocamento escolhido e depois esquecido viraria número errado em
 * silêncio — o mesmo risco que a distância do alvo tinha no MIRA-2, e que lá foi
 * resolvido deixando o valor visível no card. Aqui a saída é mais simples: nada
 * fica selecionado.
 *
 * ## Todas as linhas aparecem, inclusive as de valor zero
 *
 * *"Voando: 0 — sem a vantagem Voo, o aéreo é sempre zero"* **ensina a regra**. A
 * linha que não existe deixa o jogador sem saber se é zero ou se o app esqueceu.
 */
@Composable
fun DialogoDeslocamentos(
    personagem: Personagem,
    onDismiss: () -> Unit
) {
    val linhas = DeslocamentosRules.todos(personagem)
    val carga = DeslocamentosRules.tabelaDeCarga(personagem)

    FullscreenDialogContainer(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                "Deslocamentos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                DeslocamentosRules.resumoDaCarga(personagem),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(linhas) { LinhaDeDeslocamento(it) }

                item {
                    Text(
                        "Deslocamento por nível de carga",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
                items(carga) { LinhaDeDeslocamento(it) }

                item {
                    Text(
                        "A carga nunca reduz o Deslocamento nem a Esquiva a menos de 1 " +
                            "(MB p.17). Frações são descartadas.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text(UiActionLabels.FECHAR) }
            }
        }
    }
}

@Composable
private fun LinhaDeDeslocamento(linha: DeslocamentosRules.Linha) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = appCardColors()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // Sem `clickable`: a lista é só leitura, e um card que responde ao
                // toque promete uma ação que não existe.
                .semantics {
                    contentDescription = "${linha.rotulo}: ${linha.valor}. ${linha.conta}." +
                        if (linha.ehAtual) " É a carga atual." else ""
                }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    linha.rotulo + if (linha.ehAtual) "  ●" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (linha.ehAtual) FontWeight.Bold else FontWeight.Medium
                )
                Text(
                    linha.conta,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Text(
                linha.valor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (linha.ehAtual) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
