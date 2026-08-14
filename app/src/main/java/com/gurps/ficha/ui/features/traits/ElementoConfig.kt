package com.gurps.ficha.ui.features.traits

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.traits.ElementoRuleBase
import com.gurps.ficha.ui.AppBotaoPasso
import com.gurps.ficha.ui.UiEstilos

/**
 * Configuração de **Controle** e **Criar** — faixa do elemento × níveis.
 *
 * 🔴 Achado pelo usuário na tela: *"a vantagem é por nível, porém está como
 * variável, tenho que subir ponto a ponto e não 20/15/10!"*
 *
 * As duas caíam no diálogo de custo **variável**, com o `−1 / +1` somando um
 * ponto por vez. No livro o preço vem de **duas** escolhas: o quanto o elemento
 * é comum (que fixa o preço de cada nível) e quantos níveis o personagem tem.
 *
 * ⚠️ Mesma forma do [BracalConfig], de propósito — o projeto já tinha resolvido
 * este formato de custo uma vez, e duas telas diferentes para a mesma pergunta
 * seria mais uma daquelas "duas rotas para a mesma coisa".
 */
@Composable
fun ElementoConfig(
    regra: ElementoRuleBase,
    faixaAtual: String,
    nivel: Int,
    onChanged: (faixa: String, nivel: Int) -> Unit
) {
    val nivelMinimo = 1
    val nivelMaximo = 20
    val escolhida = regra.faixas.firstOrNull { it.nome == faixaAtual } ?: regra.faixas.last()

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            "O quanto o elemento é comum (p.${regra.pagina}):",
            style = MaterialTheme.typography.bodyMedium
        )

        regra.faixas.forEach { faixa ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onChanged(faixa.nome, nivel) }
                    .semantics {
                        contentDescription =
                            "${faixa.nome}, ${faixa.custoPorNivel} pontos por nível. " +
                                "Exemplos: ${faixa.exemplos}."
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = faixa.nome == escolhida.nome,
                    onClick = { onChanged(faixa.nome, nivel) }
                )
                Column {
                    Text(
                        "${faixa.nome} — ${faixa.custoPorNivel} pontos/nível",
                        style = UiEstilos.nomeDoItem
                    )
                    Text(
                        faixa.exemplos,
                        style = UiEstilos.detalheDoItem,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Níveis:", style = MaterialTheme.typography.bodyMedium)
            AppBotaoPasso(
                sinal = "−",
                descricao = "Menos um nível",
                onClick = { onChanged(escolhida.nome, (nivel - 1).coerceAtLeast(nivelMinimo)) }
            )
            Text("$nivel", style = UiEstilos.nomeDoItem)
            AppBotaoPasso(
                sinal = "+",
                descricao = "Mais um nível",
                onClick = { onChanged(escolhida.nome, (nivel + 1).coerceAtMost(nivelMaximo)) }
            )
        }

        Text(
            "${escolhida.custoPorNivel} × $nivel = " +
                "${escolhida.custoPorNivel * nivel} pontos",
            style = UiEstilos.detalheDoItem,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
