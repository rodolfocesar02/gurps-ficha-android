package com.gurps.ficha.ui.features.traits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.gurps.ficha.domain.rules.poderes.MontadorDeModificador
import com.gurps.ficha.domain.rules.poderes.ReservaDeEnergia
import com.gurps.ficha.model.Poder
import com.gurps.ficha.ui.AppBotaoPrincipal
import com.gurps.ficha.ui.AppBotaoSecundario
import com.gurps.ficha.ui.AppCampoCompacto
import com.gurps.ficha.ui.AppFileiraDeBotoes
import com.gurps.ficha.ui.UiEstilos

/**
 * **A Reserva de Energia e o montador de modificador, na tela** — Lotes POD-9 e
 * POD-7.
 *
 * ⚠️ Arquivo próprio: o `DialogsPoderes.kt` já passa de 500 linhas e o teto do
 * projeto é 1000. Painel novo nasce ao lado.
 */
@Composable
fun ColumnScope.PainelDaReserva(poder: Poder, onMudar: (Poder) -> Unit) {
    val limitacoes = poder.limitacoesDaReserveResolvidas
    val conflitos = ReservaDeEnergia.conflitos(limitacoes)

    Spacer(Modifier.height(12.dp))
    Text("Reserva de Energia (p.119)", style = UiEstilos.subtituloDialogo)

    AppCampoCompacto(
        value = if (poder.reservaDeEnergia == 0) "" else poder.reservaDeEnergia.toString(),
        onValueChange = { txt ->
            onMudar(poder.copy(reservaDeEnergia = txt.filter(Char::isDigit).take(3).toIntOrNull() ?: 0))
        },
        label = "PF de reserva (0 = usa os PF normais)",
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )

    if (poder.reservaDeEnergia > 0) {
        val minutos = ReservaDeEnergia.minutosPorPonto(limitacoes)
        Text(
            "Custo: ${poder.custoDaReserva} pontos · " +
                if (minutos == null) "não recarrega com o tempo"
                else "recarrega 1 ponto a cada $minutos minutos",
            style = UiEstilos.detalheDoItem,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ReservaDeEnergia.Limitacao.entries.forEach { lim ->
            val marcada = lim in limitacoes
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = marcada,
                    onCheckedChange = { ligar ->
                        val nova = if (ligar) poder.limitacoesDaReserva + lim.name
                        else poder.limitacoesDaReserva - lim.name
                        onMudar(poder.copy(limitacoesDaReserva = nova))
                    }
                )
                Column {
                    Text(
                        "${lim.rotulo} (${lim.valor}%)",
                        style = UiEstilos.detalheDoItem
                    )
                    Text(
                        lim.explicacao,
                        style = UiEstilos.detalheDoItem,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ⚠️ Conflito é AVISO em vermelho, não bloqueio: o livro diz que as duas
        // são incompatíveis, e quem decide na mesa é o Mestre. Mas sem o aviso o
        // jogador somaria −70% com −60% sem perceber.
        conflitos.forEach { par ->
            Text(
                "O livro não permite juntar " +
                    par.joinToString(" e ") { it.rotulo } + ".",
                style = UiEstilos.detalheDoItem,
                color = MaterialTheme.colorScheme.error
            )
        }
        Text(
            "Esgotar a Reserva não causa os efeitos de PF baixo, e tê-la cheia não " +
                "protege contra eles.",
            style = UiEstilos.detalheDoItem,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * **Montar o modificador de poder somando componentes** — Lote POD-7, p.20-26.
 *
 * Para quem cria poder **personalizado**. Quem usa um dos 11 prontos não precisa
 * disto — o valor já vem do livro ao escolher a fonte.
 */
@Composable
fun MontadorDeModificadorDialog(
    valorAtual: Int,
    onDismiss: () -> Unit,
    onAplicar: (Int) -> Unit
) {
    var escolhidos by remember { mutableStateOf(listOf<MontadorDeModificador.Componente>()) }
    val total = MontadorDeModificador.total(escolhidos)
    val conflitos = MontadorDeModificador.conflitosDeGrupo(escolhidos)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().heightIn(max = 620.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Montar o modificador", style = UiEstilos.tituloDialogo)
                Text(
                    "Some os componentes do livro (p.20-26). Atual: $valorAtual%",
                    style = UiEstilos.subtituloDialogo
                )
                Spacer(Modifier.height(8.dp))

                Column(
                    modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())
                ) {
                    MontadorDeModificador.Grupo.entries.forEach { grupo ->
                        val doGrupo = MontadorDeModificador.CATALOGO.filter { it.grupo == grupo }
                        if (doGrupo.isEmpty()) return@forEach
                        Text(grupo.rotulo, style = UiEstilos.subtituloDialogo)
                        doGrupo.forEach { c ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = c in escolhidos,
                                    onCheckedChange = { ligar ->
                                        escolhidos = if (ligar) {
                                            // ⚠️ Dentro de um grupo a escolha é ÚNICA — marcar
                                            // duas cobraria o mesmo inconveniente duas vezes.
                                            // Só o grupo "Outros" acumula.
                                            if (grupo == MontadorDeModificador.Grupo.EXTRAS) escolhidos + c
                                            else escolhidos.filterNot { it.grupo == grupo } + c
                                        } else escolhidos - c
                                    }
                                )
                                Column {
                                    Text(
                                        "${c.rotulo} (${if (c.valor > 0) "+" else ""}${c.valor}%)",
                                        style = UiEstilos.detalheDoItem
                                    )
                                    if (c.explicacao.isNotBlank()) {
                                        Text(
                                            c.explicacao,
                                            style = UiEstilos.detalheDoItem,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }

                Text(
                    "Total: ${if (total > 0) "+" else ""}$total%",
                    style = UiEstilos.nomeDoItem
                )
                MontadorDeModificador.avisoDaFaixa(total)?.let {
                    Text(it, style = UiEstilos.detalheDoItem,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                conflitos.forEach {
                    Text("Escolha só uma opção em “${it.rotulo}”.",
                        style = UiEstilos.detalheDoItem,
                        color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(12.dp))
                AppFileiraDeBotoes {
                    AppBotaoSecundario("Cancelar", onDismiss)
                    AppBotaoPrincipal("Usar $total%", { onAplicar(total) })
                }
            }
        }
    }
}
