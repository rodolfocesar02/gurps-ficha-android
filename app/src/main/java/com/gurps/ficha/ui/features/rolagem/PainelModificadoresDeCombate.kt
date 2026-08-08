package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.ModificadoresDeCombate
import com.gurps.ficha.ui.AppBotaoPasso
import com.gurps.ficha.ui.UiEstilos
import com.gurps.ficha.ui.linhaAlternavel

/**
 * **Os modificadores condicionais de combate** na tela (Lotes MB-1 e MB-4).
 *
 * É a página que fica aberta na mesa (MB p.547-549), virada em caixinhas.
 *
 * ## Por que arquivo próprio
 *
 * O `DialogoMira` já passa de 900 linhas e é o lugar onde **cinco** fontes de
 * modificador se encontram. Enfiar mais quinze caixinhas lá dentro o levaria
 * direto ao teto de 1.000 — e este painel tem vida própria: é uma lista, não uma
 * regra.
 *
 * ## ⚠️ A lista mostra só o que AINDA não é automático
 *
 * Metade da tabela do livro já é calculada em outro lugar (Apontar, distância,
 * tamanho do alvo, mão inábil, Avançar e Atacar, Golpe Rápido, luz). Oferecer de
 * novo aqui faria o jogador aplicar **duas vezes** o mesmo redutor — e ele não
 * teria como perceber. O `ModificadoresDeCombateTest` trava isso.
 */
@Composable
fun PainelModificadoresDeCombate(
    ehADistancia: Boolean,
    escolhas: Map<String, Int>,
    onAlternar: (ModificadoresDeCombate.Modificador) -> Unit,
    onQuantidade: (ModificadoresDeCombate.Modificador, Int) -> Unit
) {
    val lista = if (ehADistancia) {
        ModificadoresDeCombate.A_DISTANCIA
    } else {
        ModificadoresDeCombate.CORPO_A_CORPO
    }

    lista.groupBy { it.grupo }.forEach { (grupo, doGrupo) ->
        Text(
            grupo.titulo,
            style = UiEstilos.subtituloDialogo,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = 6.dp)
        )
        doGrupo.forEach { m ->
            LinhaDeModificador(
                modificador = m,
                quantidade = escolhas[m.id] ?: 0,
                onAlternar = { onAlternar(m) },
                onQuantidade = { onQuantidade(m, it) }
            )
        }
    }
}

@Composable
private fun LinhaDeModificador(
    modificador: ModificadoresDeCombate.Modificador,
    quantidade: Int,
    onAlternar: () -> Unit,
    onQuantidade: (Int) -> Unit
) {
    val marcado = quantidade > 0
    val escolha = ModificadoresDeCombate.Escolha(modificador, quantidade.coerceAtLeast(1))
    val valorMostrado = if (marcado) escolha.total else modificador.valor
    val sinal = if (valorMostrado > 0) "+$valorMostrado" else "$valorMostrado"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .linhaAlternavel(
                marcado = marcado,
                descricao = descricaoAcessivel(modificador, quantidade),
                onAlternar = onAlternar
            )
            .padding(top = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = marcado, onCheckedChange = null)
        Column(modifier = Modifier.weight(1f).padding(start = 2.dp)) {
            Text(
                "${modificador.rotulo}: $sinal" +
                    // O asterisco do livro na tela, porque ele é o que derruba
                    // o NH para 9 — e é a informação mais cara desta lista.
                    if (modificador.asterisco) " *" else "",
                style = UiEstilos.detalheDoItem,
                color = if (marcado) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
            modificador.explicacao?.let {
                Text(it, style = UiEstilos.detalheDoItem, color = MaterialTheme.colorScheme.outline)
            }
        }
        // Os que se repetem ganham o contador — e só depois de marcados, porque
        // "quantas vezes" só faz sentido quando a coisa está valendo.
        if (modificador.porUnidade && marcado) {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppBotaoPasso(
                    sinal = "−",
                    descricao = "Diminuir a quantidade de ${modificador.rotulo}",
                    enabled = quantidade > 1,
                    onClick = { onQuantidade((quantidade - 1).coerceAtLeast(1)) }
                )
                Text("$quantidade", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                AppBotaoPasso(
                    sinal = "+",
                    descricao = "Aumentar a quantidade de ${modificador.rotulo}",
                    onClick = { onQuantidade(quantidade + 1) }
                )
            }
        }
    }
}

private fun descricaoAcessivel(
    m: ModificadoresDeCombate.Modificador,
    quantidade: Int
): String {
    val escolha = ModificadoresDeCombate.Escolha(m, quantidade.coerceAtLeast(1))
    val valor = if (quantidade > 0) escolha.total else m.valor
    val comoLer = if (valor > 0) "mais $valor" else "menos ${-valor}"
    return buildString {
        append("${m.rotulo}. Modificador $comoLer. ")
        if (m.porUnidade) append("Repete por unidade. ")
        if (m.asterisco) append("⚠️ Este derruba o seu nível para no máximo nove. ")
        m.explicacao?.let { append(it) }
    }
}
