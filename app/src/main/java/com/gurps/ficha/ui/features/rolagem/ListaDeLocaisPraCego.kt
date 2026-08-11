package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.MapaDaSilhueta
import com.gurps.ficha.ui.AppBotaoSecundario
import com.gurps.ficha.ui.UiEstilos
import com.gurps.ficha.ui.UiTokens
import com.gurps.ficha.ui.linhaAlternavel

/**
 * **Onde o golpe acertou, para quem não vê a tela** — Lote ACESS-2.
 *
 * ## 🔴 A pracego não conseguia registrar LADO
 *
 * Este é o motivo de o lote existir, e não é acessibilidade de rótulo: é
 * **informação faltando**. A silhueta da variante visual tem **16 regiões** —
 * braço esquerdo e direito, mão esquerda e direita, os dois olhos. A lista da
 * pracego tinha **11 locais sem lado**.
 *
 * Quem não enxerga não conseguia anotar *qual* braço foi decepado. As duas
 * variantes gravavam coisas diferentes na mesma ficha, e o combate depois não
 * teria como saber.
 *
 * ## A estrutura é a mesma da silhueta, a entrada é que muda
 *
 * Os mesmos três grupos ([MapaDaSilhueta.Tela]) e as mesmas 16 regiões, lidos da
 * **mesma fonte**. Tocar num grupo abre as partes dele — igual ao zoom, sem
 * imagem.
 *
 * ⚠️ Dois níveis, e não uma lista de 16, por causa de quem navega por toque
 * explorando a tela: dezesseis paradas em fila viram um corredor. Cinco grupos
 * de quatro ou cinco são achados por eliminação, que é como se navega de ouvido.
 *
 * ## ⚠️ Por que não reaproveitei a silhueta com `contentDescription`
 *
 * Porque uma imagem não se tateia. O TalkBack pode até ler "silhueta do corpo",
 * mas não existe o gesto "encoste no braço esquerdo" numa figura — a exploração
 * por toque devolve o que está sob o dedo, e o dedo não sabe onde o braço está.
 */
@Composable
fun ListaDeLocaisPraCego(
    selecionada: MapaDaSilhueta.Regiao?,
    onSelecionar: (MapaDaSilhueta.Regiao) -> Unit,
    modifier: Modifier = Modifier
) {
    var grupoAberto by remember { mutableStateOf<MapaDaSilhueta.Tela?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(UiTokens.ItemSpacing)
    ) {
        val aberto = grupoAberto
        if (aberto == null) {
            Text(
                "Onde o golpe acertou",
                style = UiEstilos.subtituloDialogo,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.semantics { heading() }
            )
            MapaDaSilhueta.Tela.entries.forEach { tela ->
                val quantas = MapaDaSilhueta.REGIOES.count { it.tela == tela }
                AppBotaoSecundario(
                    texto = "${tela.rotulo} ($quantas)",
                    onClick = { grupoAberto = tela },
                    larguraTotal = true
                )
            }
            selecionada?.let {
                Text(
                    "Escolhido: ${it.nomeCompleto}",
                    style = UiEstilos.nomeDoItem,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Text(
                aberto.rotulo,
                style = UiEstilos.subtituloDialogo,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.semantics { heading() }
            )
            MapaDaSilhueta.REGIOES.filter { it.tela == aberto }.forEach { regiao ->
                LinhaDeLocal(
                    regiao = regiao,
                    marcada = regiao.id == selecionada?.id,
                    onEscolher = { onSelecionar(regiao) }
                )
            }
            AppBotaoSecundario(
                texto = "Voltar para os grupos",
                onClick = { grupoAberto = null },
                larguraTotal = true
            )
        }
    }
}

/**
 * ⚠️ Usa `RadioButton`, não `Checkbox`: só **uma** parte do corpo pode estar
 * escolhida, e o leitor de tela anuncia os dois papéis de formas diferentes —
 * "caixa de seleção" sugere que dá para marcar várias.
 */
@Composable
private fun LinhaDeLocal(
    regiao: MapaDaSilhueta.Regiao,
    marcada: Boolean,
    onEscolher: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .linhaAlternavel(
                marcado = marcada,
                // A descrição vem da regra pura, que está no
                // `RotulosAcessiveisTest` — ver o Lote ACESS-1.
                descricao = regiao.descricaoAcessivel,
                onAlternar = onEscolher
            )
            .padding(
                horizontal = UiTokens.LinhaDeListaPaddingH,
                vertical = UiTokens.LinhaDeListaPaddingV
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = marcada, onClick = null)
        Column(modifier = Modifier.padding(start = 2.dp)) {
            Text(regiao.nomeCompleto, style = UiEstilos.nomeDoItem)
            Text(
                "Ataque ${sinalDe(regiao.local.penalidadeAtaque)}",
                style = UiEstilos.detalheDoItem,
                color = MaterialTheme.colorScheme.outline,
                // O visível mostra "-7"; o falado já foi dito na linha inteira.
                modifier = Modifier.semantics { contentDescription = "" }
            )
        }
    }
}

private fun sinalDe(n: Int): String = if (n > 0) "+$n" else "$n"
