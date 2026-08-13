package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.poderes.AmpliacoesTemporarias
import com.gurps.ficha.domain.rules.poderes.EsforcoAdicional
import com.gurps.ficha.domain.rules.poderes.ReservaDeEnergia
import com.gurps.ficha.domain.rules.poderes.UsoDoPoder
import com.gurps.ficha.model.Poder
import com.gurps.ficha.ui.AppBotaoPasso
import com.gurps.ficha.ui.AppSelectionDialog
import com.gurps.ficha.ui.AppSelectionRow
import com.gurps.ficha.ui.UiEstilos
import com.gurps.ficha.ui.contadorDe

/**
 * **O botão Poderes da aba Rolagem** — Lotes POD-12 e POD-13.
 *
 * ## Por que ele existe aqui, e não no diálogo de poderes
 *
 * Esforço adicional (p.160) e ampliação temporária (p.172) não são configuração
 * — são **ações no meio de uma rolagem**. Configurá-las na aba Traços seria
 * pô-las onde ninguém as usa.
 *
 * ⚠️ Segue o padrão dos botões **Técnicas** e **Magias**: só aparece quando há o
 * que mostrar. Personagem sem poder configurado não ganha um botão morto.
 *
 * ## O que este diálogo faz, e o que ele não faz
 *
 * Ele **diz o número** — a penalidade, o custo em PF, o que a fonte manda rolar.
 * Não rola por conta própria e não desconta PF sozinho: o app não sabe se o
 * jogador foi em frente com a proeza, e cobrar PF de uma tentativa que não
 * aconteceu seria pior do que não cobrar.
 */
@Composable
fun DialogoPoderes(
    poderes: List<Poder>,
    onDismiss: () -> Unit
) {
    var selecionado by remember { mutableStateOf<Poder?>(null) }

    val atual = selecionado
    if (atual == null) {
        AppSelectionDialog(
            titulo = "Poderes",
            subtitulo = "O que a fonte manda rolar, e o custo de forçar a habilidade",
            contador = contadorDe(poderes.size, "poder", "poderes"),
            onDismiss = onDismiss
        ) {
            items(poderes) { p ->
                AppSelectionRow(
                    nome = p.nome,
                    detalhe = detalheNaRolagem(p),
                    detalheADireita = if (p.nivelTalento > 0) "Talento ${p.nivelTalento}" else null,
                    onClick = { selecionado = p },
                    descricaoAcessivel = p.descricaoAcessivel
                )
            }
        }
    } else {
        DetalheDoPoderNaRolagem(atual) { selecionado = null }
    }
}

private fun detalheNaRolagem(p: Poder): String = buildString {
    if (p.fonte.isNotBlank()) append(p.fonte).append(" · ")
    append(
        when (UsoDoPoder.Incapacitacao.atributo(p.fonte)) {
            UsoDoPoder.Incapacitacao.Atributo.HT -> "incapacitação: HT"
            UsoDoPoder.Incapacitacao.Atributo.VONTADE -> "incapacitação: Vontade"
            UsoDoPoder.Incapacitacao.Atributo.IMUNE -> "imune a incapacitação"
            UsoDoPoder.Incapacitacao.Atributo.A_CRITERIO_DO_MESTRE -> "o Mestre decide"
        }
    )
    if (p.reservaDeEnergia > 0) append(" · RE ").append(p.reservaDeEnergia)
}

/**
 * A tela de um poder durante a rolagem: o esforço adicional (POD-12) e a
 * ampliação temporária (POD-13), com os números já resolvidos.
 */
@Composable
private fun DetalheDoPoderNaRolagem(poder: Poder, onVoltar: () -> Unit) {
    var aumento by remember { mutableStateOf(0) }     // % de efeito extra
    var ampliacao by remember { mutableStateOf(0) }   // % de ampliação temporária
    var pfGastos by remember { mutableStateOf(0) }

    AppSelectionDialog(
        titulo = poder.nome,
        subtitulo = detalheNaRolagem(poder),
        onDismiss = onVoltar
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(UsoDoPoder.Incapacitacao.explicar(poder.fonte), style = UiEstilos.detalheDoItem)

                if (poder.nivelTalento > 0) {
                    Text(
                        "Talento ${poder.nivelTalento}: soma em ativar, atacar, controlar e " +
                            "defender. Não soma no dano nem no teste do alvo (p.158).",
                        style = UiEstilos.detalheDoItem,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (poder.reservaDeEnergia > 0) {
                    val min = ReservaDeEnergia.minutosPorPonto(poder.limitacoesDaReserveResolvidas)
                    Text(
                        "Reserva de Energia: ${poder.reservaDeEnergia} PF · " +
                            if (min == null) "não recarrega com o tempo"
                            else "1 ponto a cada $min minutos",
                        style = UiEstilos.detalheDoItem,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ── POD-12: esforço adicional ──
                Text("Esforço adicional (p.160)", style = UiEstilos.subtituloDialogo)
                PassoDePercentual("Efeito extra", aumento, 5) { aumento = it.coerceIn(0, 200) }
                Text(
                    "Teste de Vontade com ${EsforcoAdicional.penalidade(aumento)} " +
                        "para +$aumento% de efeito.",
                    style = UiEstilos.detalheDoItem
                )
                EsforcoAdicional.avisoDoTeto(aumento)?.let {
                    Text(it, style = UiEstilos.detalheDoItem, color = MaterialTheme.colorScheme.error)
                }
                Text(
                    "+${EsforcoAdicional.BONUS_APENAS_EM_EMERGENCIAS} se a situação for de " +
                        "Apenas em Emergências. Não vale para habilidade passiva nem para a que " +
                        "exige teste de ataque — nessa, o equivalente é o Ataque Total (Determinado).",
                    style = UiEstilos.detalheDoItem,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                // ── POD-13: ampliação temporária ──
                Text("Ampliação temporária (p.172)", style = UiEstilos.subtituloDialogo)
                PassoDePercentual("Ampliação", ampliacao, 10) { ampliacao = it.coerceIn(0, 300) }
                PassoDePercentual("PF gastos de propósito", pfGastos, 1) { pfGastos = it.coerceIn(0, 20) }
                val fim = AmpliacoesTemporarias.modificadorFinal(ampliacao, poder.nivelTalento, pfGastos)
                Text(
                    "Concentrar + Vontade (mental) ou Preparar + HT (física), com $fim. " +
                        "Custa ${AmpliacoesTemporarias.pfDaTentativa(pfGastos)} PF — nada em " +
                        "sucesso decisivo.",
                    style = UiEstilos.detalheDoItem
                )
                AmpliacoesTemporarias.periciaDaFonte(poder.fonte)?.let {
                    Text(
                        "Se a mesa usa Perícias Aprimorando Habilidades, pode rolar $it no lugar.",
                        style = UiEstilos.detalheDoItem,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // ⚠️ O elo com o POD-11: a falha crítica não para na habilidade.
                Text(
                    "Falha crítica: a habilidade some por 1d segundos e o poder INTEIRO checa " +
                        "incapacitação (p.156).",
                    style = UiEstilos.detalheDoItem,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PassoDePercentual(rotulo: String, valor: Int, passo: Int, onMudar: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$rotulo: $valor", style = UiEstilos.detalheDoItem)
        Row {
            AppBotaoPasso("−", "Diminuir $rotulo", { onMudar(valor - passo) })
            AppBotaoPasso("+", "Aumentar $rotulo", { onMudar(valor + passo) })
        }
    }
}
