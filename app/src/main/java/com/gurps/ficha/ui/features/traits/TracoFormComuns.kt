package com.gurps.ficha.ui.features.traits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.gurps.ficha.model.TipoCusto
import com.gurps.ficha.ui.AppBotaoPasso
import com.gurps.ficha.ui.AppBotaoPrincipal
import com.gurps.ficha.ui.AppBotaoSecundario
import com.gurps.ficha.ui.AppFileiraDeBotoes
import com.gurps.ficha.ui.UiActionLabels
import com.gurps.ficha.ui.UiEstilos

/**
 * **O miolo comum dos diálogos de traço** (Lote LAYOUT-3).
 *
 * ## O defeito que motivou o arquivo
 *
 * Configurar e editar uma vantagem eram **dois arquivos** — `VantagemDialogs.kt`
 * e `VantagemEditarDialog.kt` — nascidos de uma cópia. Divergiram em quatro
 * pontos, e o usuário achou os quatro num print do *Abafador de Mana* (03/08):
 *
 * 1. 🔴 **O `−` e o `+` do nível só existiam na variante `pracego`.** Na `visual`,
 *    ao adicionar, o nível mudava **arrastando o dedo** — um gesto sem nenhuma
 *    dica na tela. Ao editar, os botões estavam lá.
 * 2. O rótulo do campo de texto tinha **três** grafias:
 *    `"Descrição/Especializações"`, `"Descrição/Especialização"` e `"Descrição"`.
 * 3. *"Nenhum modificador aplicado."* aparecia só ao adicionar.
 * 4. 🔴 **O teto do catálogo se perdia na edição** — ver [NivelDoTraco].
 *
 * A única diferença legítima entre os dois é o rótulo do botão: *Adicionar* ou
 * *Salvar*. Tudo o mais mora aqui.
 */

/** Um rótulo só para o campo livre. As três grafias antigas viram esta. */
const val ROTULO_DESCRICAO_TRACO = "Descrição / Especialização"

/**
 * O nome do tipo de custo **em português**.
 *
 * ⚠️ A tela mostrava `definicao.tipoCusto.name.lowercase()`, e o jogador via
 * **`por_nivel`** — nome de constante do código vazando para a ficha.
 */
fun rotuloDoTipoDeCusto(tipo: TipoCusto): String = when (tipo) {
    TipoCusto.FIXO -> "custo fixo"
    TipoCusto.POR_NIVEL -> "por nível"
    TipoCusto.ESCOLHA -> "custo à escolha"
    TipoCusto.VARIAVEL -> "custo variável"
}

/**
 * O seletor de nível: `−  N  +`.
 *
 * ⚠️ Os botões aparecem **nas duas variantes e nos dois modos**. O arrastar
 * vertical que existia na variante visual era o único jeito de mudar o nível ao
 * adicionar, e não tinha como o jogador descobrir — gesto sem affordance é
 * funcionalidade escondida.
 *
 * @param exibicao como o número aparece; alguns traços mostram algo diferente do
 *   nível cru (ver `nivelExibicaoVantagem`).
 */
@Composable
fun NivelDoTraco(
    nivel: Int,
    nivelMaximo: Int,
    onNivel: (Int) -> Unit,
    nivelMinimo: Int = 1,
    exibicao: String = "$nivel"
) {
    Text("Nível:", style = UiEstilos.detalheDoItem)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppBotaoPasso(
            sinal = "−",
            descricao = "Diminuir o nível. Nível atual: $exibicao.",
            enabled = nivel > nivelMinimo,
            onClick = { if (nivel > nivelMinimo) onNivel(nivel - 1) }
        )
        Text(
            exibicao,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        AppBotaoPasso(
            sinal = "+",
            descricao = if (nivel >= nivelMaximo) {
                "Aumentar o nível. Já está no máximo do livro, que é $nivelMaximo."
            } else {
                "Aumentar o nível. Nível atual: $exibicao."
            },
            enabled = nivel < nivelMaximo,
            onClick = { if (nivel < nivelMaximo) onNivel(nivel + 1) }
        )
    }
    // O teto à vista: sem isto, o `+` desligado parece o app travado.
    if (nivel >= nivelMaximo) {
        Text(
            "Máximo do livro para este traço: $nivelMaximo.",
            style = UiEstilos.detalheDoItem,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

/** O campo livre de descrição/especialização, com o rótulo único. */
@Composable
fun CampoDeDescricaoDoTraco(
    valor: String,
    onValor: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValor,
        label = { Text(ROTULO_DESCRICAO_TRACO) },
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * O rodapé do diálogo de traço.
 *
 * @param modoEdicao decide entre *Salvar* e *Adicionar* — a **única** diferença
 *   que deve existir entre configurar e editar.
 */
@Composable
fun RodapeDoTraco(
    modoEdicao: Boolean,
    onCancelar: () -> Unit,
    onConfirmar: () -> Unit,
    confirmarHabilitado: Boolean = true
) {
    AppFileiraDeBotoes {
        AppBotaoSecundario(texto = UiActionLabels.CANCELAR, onClick = onCancelar)
        AppBotaoPrincipal(
            texto = if (modoEdicao) UiActionLabels.SALVAR else UiActionLabels.ADICIONAR,
            onClick = onConfirmar,
            enabled = confirmarHabilitado
        )
    }
}
