package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.FadigaRules
import com.gurps.ficha.ui.AppBotaoPasso
import com.gurps.ficha.ui.AppBotaoPrincipal
import com.gurps.ficha.ui.AppBotaoSecundario
import com.gurps.ficha.ui.AppFileiraDeBotoes
import com.gurps.ficha.ui.UiEstilos
import com.gurps.ficha.ui.UiTokens
import com.gurps.ficha.ui.linhaAlternavel

/**
 * **O botão PF** — Lote MB-6 (MB p.426-428).
 *
 * O jogador marca de onde veio o cansaço, ajusta a quantidade, salva — e o PF da
 * ficha desce sozinho. Abrindo de novo e desmarcando, ele sobe de volta.
 *
 * ## 🔴 Por que a tela mostra as origens separadas
 *
 * Porque **PF perdido não é tudo igual**. O rodapé é a parte que muda o jogo:
 * ele diz que aqueles 3 PF de fome **não voltam com descanso** — só com um dia
 * parado e três refeições. Um herói faminto pode sentar a tarde inteira e não
 * subir um ponto, e sem esta tela ele descobre isso na pior hora.
 *
 * ## ⚠️ Salvar não apaga o que foi gasto fora daqui
 *
 * O PF também cai por magia, por combate e pelo deslize no cartão. Ao abrir, o
 * que falta e esta lista não explica cai na linha **"Perda anotada à mão"**,
 * visível — em vez de o painel devolver de graça o PF que o personagem gastou.
 */
@Composable
fun DialogoFadiga(
    pfMax: Int,
    pfAtual: Int,
    quantidadesSalvas: Map<String, Int>,
    dorminhoco: Boolean,
    onSalvar: (quantidades: Map<String, Int>, pfNovo: Int, pvPerdidos: Int) -> Unit,
    onFechar: () -> Unit
) {
    // Reconcilia UMA vez, na abertura: é a foto de quando o jogador entrou.
    val inicial = remember(pfMax, pfAtual, quantidadesSalvas) {
        FadigaRules.reconciliar(pfMax, pfAtual, quantidadesSalvas)
    }
    val quantidades = remember(inicial) { mutableStateMapOf<String, Int>().apply { putAll(inicial) } }
    var mostrarDetalhes by remember { mutableStateOf(false) }

    val total = FadigaRules.totalDe(quantidades)
    val pfPrevisto = pfMax - total.pf
    val estado = FadigaRules.estadoDe(pfPrevisto, pfMax)
    val alertaSono = FadigaRules.alertaDeSono(
        pfPerdidoPorSono = quantidades["sono"] ?: 0,
        pfMax = pfMax,
        dorminhoco = dorminhoco
    )

    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text("Pontos de Fadiga", style = UiEstilos.tituloDialogo) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(UiTokens.ItemSpacing)
            ) {
                Text(
                    "PF ficará em $pfPrevisto de $pfMax",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (pfPrevisto <= 0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                if (total.pv > 0) {
                    Text(
                        "⚠️ E ${total.pv} PV a menos: a sede severa tira PV junto (MB p.426).",
                        style = UiEstilos.detalheDoItem,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                FadigaRules.FONTES.forEach { fonte ->
                    LinhaDeFadiga(
                        fonte = fonte,
                        quantidade = quantidades[fonte.id] ?: 0,
                        mostrarExplicacao = mostrarDetalhes,
                        onAlternar = {
                            if ((quantidades[fonte.id] ?: 0) > 0) {
                                quantidades.remove(fonte.id)
                            } else {
                                quantidades[fonte.id] = 1
                            }
                        },
                        onQuantidade = { nova ->
                            if (nova <= 0) quantidades.remove(fonte.id) else quantidades[fonte.id] = nova
                        }
                    )
                }

                AppBotaoSecundario(
                    texto = if (mostrarDetalhes) "Ocultar as regras" else "Mostrar as regras",
                    onClick = { mostrarDetalhes = !mostrarDetalhes },
                    larguraTotal = true
                )

                // 🔴 O rodapé é a razão de este painel existir.
                val recuperacao = FadigaRules.resumoDaRecuperacao(quantidades)
                if (recuperacao.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = UiTokens.ItemSpacing))
                    Text(
                        "Como cada perda volta",
                        style = UiEstilos.subtituloDialogo,
                        fontWeight = FontWeight.SemiBold
                    )
                    recuperacao.forEach {
                        Text(it, style = UiEstilos.detalheDoItem, color = MaterialTheme.colorScheme.outline)
                    }
                }

                alertaSono?.let {
                    Text(
                        it,
                        style = UiEstilos.detalheDoItem,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = UiTokens.ItemSpacing)
                    )
                }
                estado.avisos.forEach {
                    Text(
                        it,
                        style = UiEstilos.detalheDoItem,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = UiTokens.ItemSpacing)
                    )
                }
            }
        },
        confirmButton = {
            AppFileiraDeBotoes {
                AppBotaoSecundario("Cancelar", onFechar)
                AppBotaoPrincipal("Salvar", {
                    onSalvar(quantidades.toMap(), pfPrevisto, total.pv)
                })
            }
        }
    )
}

@Composable
private fun LinhaDeFadiga(
    fonte: FadigaRules.Fonte,
    quantidade: Int,
    mostrarExplicacao: Boolean,
    onAlternar: () -> Unit,
    onQuantidade: (Int) -> Unit
) {
    val marcado = quantidade > 0
    val pf = fonte.pfDe(quantidade)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .linhaAlternavel(
                marcado = marcado,
                descricao = fonte.descricaoAcessivel(quantidade),
                onAlternar = onAlternar
            )
            .padding(
                horizontal = UiTokens.LinhaDeListaPaddingH,
                vertical = UiTokens.LinhaDeListaPaddingV
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = marcado, onCheckedChange = null)
        Column(modifier = Modifier.weight(1f).padding(start = 2.dp)) {
            Text(
                fonte.rotulo + if (marcado) "  −$pf PF" else "",
                style = UiEstilos.nomeDoItem,
                color = if (marcado) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                "${fonte.recuperacao.rotulo} · 1 PF por ${fonte.unidade}",
                style = UiEstilos.detalheDoItem,
                color = MaterialTheme.colorScheme.outline
            )
            if (mostrarExplicacao) {
                Text(
                    fonte.explicacao,
                    style = UiEstilos.detalheDoItem,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        // O contador só aparece marcado: "quantas" só faz sentido quando vale.
        if (marcado) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppBotaoPasso(
                    sinal = "−",
                    descricao = "Diminuir ${fonte.unidade} de ${fonte.rotulo}",
                    onClick = { onQuantidade(quantidade - 1) }
                )
                Text(
                    "$quantidade",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                AppBotaoPasso(
                    sinal = "+",
                    descricao = "Aumentar ${fonte.unidade} de ${fonte.rotulo}",
                    onClick = { onQuantidade(quantidade + 1) }
                )
            }
        }
    }
}
