package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.EscolhaDoPedido
import com.gurps.ficha.domain.rules.PedidoDaMesa

/**
 * **O que a Mesa pediu, num toque** — lote CC-6.
 *
 * > *"Não seria mais fácil simplesmente abrir na tela, o cara clica, rola o
 * > teste, aparece no chat?"*
 *
 * 🔴 Era, e é. A versão anterior deste lote punha uma **faixa** no topo da aba
 * Rolagem e largava a pessoa no meio de trinta controlos, à procura do botão
 * certo — com um modificador solto num campo, que é exatamente o tipo de número
 * que alguém apaga sem querer.
 *
 * ⚠️ E aquele desenho tinha uma pergunta embutida que eu errei duas vezes: *"em
 * que campo é que eu ponho o modificador?"*. Pu-lo na lista de perícias, onde o
 * Ataque Inato nem existe; e ele apareceu debaixo do Dano, onde não tem nada que
 * fazer. **Um diálogo que rola ele próprio não tem campo nenhum para errar.**
 *
 * ## 🔴 Um toque, e vai para a conversa
 *
 * Decisão do Mestre: rolar e mandar de uma vez. O resultado é público, e ninguém
 * pode rolar outra vez até gostar do número.
 */
@Composable
fun DialogoPedidoDaMesa(
    pedido: PedidoDaMesa.Pedido,
    opcoes: List<EscolhaDoPedido.Opcao>,
    onRolar: (EscolhaDoPedido.Opcao) -> Unit,
    onDispensar: () -> Unit
) {
    val resposta = remember(pedido, opcoes) { EscolhaDoPedido.escolher(pedido, opcoes) }
    // ⚠️ Quando a Mesa acertou a perícia, ela já vem escolhida e o diálogo é um
    // botão só. Quando não acertou, a pessoa escolhe — e nada rola sozinho.
    var escolhida by remember(resposta) { mutableStateOf(resposta.escolhida) }

    AlertDialog(
        onDismissRequest = onDispensar,
        title = { Text("A Mesa pediu") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = EscolhaDoPedido.rotuloDaJogada(pedido, escolhida),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // 🔴 A conta à vista, e não só o número final: quem confere a
                // ficha contra a Mesa precisa de ver de onde saiu o alvo.
                escolhida?.nh?.let { nh ->
                    Text(
                        text = aConta(nh, pedido.mod),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                resposta.porqueNaoCasou?.let { porque ->
                    Text(
                        text = porque,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // A lista só aparece quando há escolha a fazer.
                if (resposta.escolhida == null && resposta.candidatas.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(resposta.candidatas) { o ->
                            UmaCandidata(
                                opcao = o,
                                mod = pedido.mod,
                                escolhida = o.id == escolhida?.id,
                                onEscolher = { escolhida = o }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = escolhida != null,
                onClick = { escolhida?.let(onRolar) }
            ) { Text("ROLAR") }
        },
        dismissButton = {
            // ⚠️ Dá para fechar sem rolar. Um diálogo que prende a tela até
            // alguém rolar seria uma armadilha no meio de uma sessão: basta a
            // Mesa mandar um pedido errado para o telefone ficar inútil.
            TextButton(onClick = onDispensar) { Text("Agora não") }
        }
    )
}

@Composable
private fun UmaCandidata(
    opcao: EscolhaDoPedido.Opcao,
    mod: Int,
    escolhida: Boolean,
    onEscolher: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEscolher),
        shape = MaterialTheme.shapes.small,
        color = if (escolhida) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(opcao.rotulo, style = MaterialTheme.typography.bodyMedium)
            opcao.nh?.let {
                Text(aConta(it, mod), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/**
 * `14 − 7 = 7`, e não só `7`.
 *
 * ⚠️ É o que deixa a pessoa ver que o modificador da Mesa entrou — e conferir o
 * número contra o que está no tabuleiro sem ter de o refazer de cabeça.
 */
internal fun aConta(nh: Int, mod: Int): String {
    if (mod == 0) return "NH $nh"
    val sinal = if (mod > 0) "+" else "−"
    return "NH $nh $sinal ${kotlin.math.abs(mod)} = ${nh + mod}"
}
