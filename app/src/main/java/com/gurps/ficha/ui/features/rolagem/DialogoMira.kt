package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.MiraRules
import com.gurps.ficha.ui.FullscreenDialogContainer
import com.gurps.ficha.ui.UiActionLabels
import com.gurps.ficha.ui.appCardColors
import com.gurps.ficha.ui.linhaAlternavel
import com.gurps.ficha.domain.rules.TabelaVelocidadeDistancia
import com.gurps.ficha.domain.rules.AlcanceDoAtaque
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.foundation.layout.PaddingValues

/**
 * **Onde acertar** — o diálogo de mira (Lote MIRA-1, MB p.398-400).
 *
 * Ideia do usuário: tocar no NH do ataque abre esta lista, e cada linha mostra
 * **o número já reduzido**. Se a Faca é NH 12 e você quer o olho (−9), a linha
 * do olho mostra **3**. Sem penalidade entre parênteses, sem conta mental no
 * meio da mesa.
 *
 * Tocou, o diálogo fecha e a rolagem sai — um gesto só.
 *
 * **Não calcula dano de propósito.** O dano localizado depende da RD do
 * oponente, que a ficha não tem e nunca terá: é informação do Mestre. Mostrar um
 * número ali seria inventar.
 *
 * O `detalhe` de cada linha existe porque a escolha real é pelo **efeito**, não
 * pelo número: mirar no crânio vale a pena por causa do ×4 de ferimento, não
 * apesar do −7.
 */
@Composable
fun DialogoMira(
    rotuloDoAtaque: String,
    nhBase: Int,
    isPraCegoVariant: Boolean,
    onEscolher: (rotulo: String, nh: Int) -> Unit,
    onDismiss: () -> Unit,
    // --- Lote MIRA-2: só chegam preenchidos em ataque à distância. ---
    ehADistancia: Boolean = false,
    alcance: AlcanceDoAtaque.Alcance = AlcanceDoAtaque.Alcance(null, null),
    // O seletor anda de DEGRAU da tabela, não de metro em metro: cada toque
    // vale exatamente −1. Ver `TabelaVelocidadeDistancia`.
    //
    // ⚠️ O estado mora na ABA, não aqui. Se morasse aqui, ele sumiria ao fechar
    // o diálogo e o toque simples no NH voltaria a rolar sem a distância — sem
    // avisar ninguém. Erro silencioso é o pior tipo.
    indiceDistancia: Int = TabelaVelocidadeDistancia.INDICE_PADRAO,
    indiceVelocidade: Int = -1,
    onIndices: (distancia: Int, velocidade: Int) -> Unit = { _, _ -> }
) {
    var desarmar by remember { mutableStateOf(false) }
    val opcoes = MiraRules.opcoes(desarmar)

    val metros = TabelaVelocidadeDistancia.degrau(indiceDistancia).metros
    val velocidade = if (indiceVelocidade < 0) 0 else
        TabelaVelocidadeDistancia.degrau(indiceVelocidade).metros
    val penalidadeDistancia = if (ehADistancia) {
        TabelaVelocidadeDistancia.penalidadeCombinada(metros, velocidade)
    } else {
        0
    }
    val nhComDistancia = nhBase + penalidadeDistancia

    FullscreenDialogContainer(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                "Onde acertar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "$rotuloDoAtaque — NH $nhBase no torso",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )

            if (ehADistancia) {
                LinhaDeDistancia(
                    metros = metros,
                    velocidade = velocidade,
                    velocidadeVisivel = indiceVelocidade >= 0,
                    penalidade = penalidadeDistancia,
                    alcance = alcance,
                    onDistancia = { delta ->
                        onIndices(
                            (indiceDistancia + delta)
                                .coerceIn(0, TabelaVelocidadeDistancia.DEGRAUS.lastIndex),
                            indiceVelocidade
                        )
                    },
                    onVelocidade = { delta ->
                        onIndices(
                            indiceDistancia,
                            (indiceVelocidade + delta)
                                .coerceIn(-1, TabelaVelocidadeDistancia.DEGRAUS.lastIndex)
                        )
                    },
                    onMostrarVelocidade = { onIndices(indiceDistancia, 0) }
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MiraRules.Grupo.values().forEach { grupo ->
                    val doGrupo = opcoes.filter { it.grupo == grupo }
                    if (doGrupo.isEmpty()) return@forEach

                    item {
                        Text(
                            grupo.rotulo,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // A opção de desarmar só faz sentido sobre a arma do
                    // oponente — no corpo não existe "desarmar".
                    if (grupo == MiraRules.Grupo.ARMA) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .linhaAlternavel(
                                        marcado = desarmar,
                                        descricao = "Golpear para desarmar em vez de quebrar. " +
                                            "Penalidade adicional de menos 2.",
                                        onAlternar = { desarmar = !desarmar }
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(checked = desarmar, onCheckedChange = null)
                                Text(
                                    "Desarmar em vez de quebrar (−2)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        }
                    }

                    items(doGrupo) { opcao ->
                        LinhaDeMira(opcao, nhComDistancia, isPraCegoVariant) {
                            // O rótulo leva a distância junto: sem isso o log do
                            // Discord diria "Crânio 5" e ninguém saberia de onde
                            // saiu o 5.
                            val ondeEQuando = if (ehADistancia && penalidadeDistancia != 0) {
                                "$rotuloDoAtaque — ${opcao.rotulo} a ${metros}m"
                            } else {
                                "$rotuloDoAtaque — ${opcao.rotulo}"
                            }
                            onEscolher(ondeEQuando, opcao.nhCom(nhComDistancia))
                        }
                    }
                }

                item {
                    Text(
                        "O dano não entra aqui: ele depende da RD do oponente, que " +
                            "só o Mestre tem.",
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

/**
 * A linha de **distância do alvo** (Lote MIRA-2, MB p.550-551).
 *
 * ## Por que o `−/+` anda de degrau, e não de metro
 *
 * A tabela do livro é logarítmica. Se cada toque valesse 1 metro, chegar a 100
 * metros seriam 98 toques. Andando pela tabela — 2, 3, 5, 7, 10, 15, 20… — são
 * 10 toques, e **cada toque vale exatamente −1**. O botão deixa de ser um
 * contador de metros e passa a ser a própria regra.
 *
 * ## Por que a velocidade começa escondida
 *
 * O livro: *"Na maioria dos combates que envolve combatentes a pé e objetos
 * inanimados, é preferível ignorar a velocidade"*. Ela aparece só quando o
 * Mestre disser que o alvo está correndo.
 *
 * ⚠️ E quando aparece, ela **soma à distância** antes de consultar a tabela —
 * não é uma segunda penalidade. Por isso a explicação mostra a conta inteira.
 */
@Composable
private fun LinhaDeDistancia(
    metros: Int,
    velocidade: Int,
    velocidadeVisivel: Boolean,
    penalidade: Int,
    alcance: AlcanceDoAtaque.Alcance,
    onDistancia: (Int) -> Unit,
    onVelocidade: (Int) -> Unit,
    onMostrarVelocidade: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = appCardColors()
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Passo(
                rotulo = "Alvo a",
                valor = TabelaVelocidadeDistancia.degrau(
                    TabelaVelocidadeDistancia.indiceDoDegrau(metros)
                ).rotulo,
                descricaoAcessivel = "Distância do alvo: $metros metros",
                penalidade = if (velocidadeVisivel) null else penalidade,
                onPasso = onDistancia
            )

            if (velocidadeVisivel) {
                Passo(
                    rotulo = "Velocidade",
                    valor = "$velocidade m/s",
                    descricaoAcessivel = "Velocidade do alvo: $velocidade metros por segundo",
                    penalidade = penalidade,
                    onPasso = onVelocidade
                )
                // A conta inteira à vista: o jogador precisa poder desconfiar do
                // número. Mesma razão das notinhas de origem do Lote NOTA-1.
                Text(
                    TabelaVelocidadeDistancia.explicacao(metros, velocidade),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                TextButton(
                    onClick = onMostrarVelocidade,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text("+ alvo em movimento", style = MaterialTheme.typography.labelSmall)
                }
            }

            AvisoDeAlcance(metros, alcance)
        }
    }
}

/** Uma linha `[−] valor [+]` com a penalidade à direita. */
@Composable
private fun Passo(
    rotulo: String,
    valor: String,
    descricaoAcessivel: String,
    penalidade: Int?,
    onPasso: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            rotulo,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline
        )
        TextButton(
            onClick = { onPasso(-1) },
            modifier = Modifier.semantics { contentDescription = "Diminuir. $descricaoAcessivel" },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
        ) { Text("−", style = MaterialTheme.typography.titleMedium) }
        Text(
            valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { contentDescription = descricaoAcessivel }
        )
        TextButton(
            onClick = { onPasso(1) },
            modifier = Modifier.semantics { contentDescription = "Aumentar. $descricaoAcessivel" },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
        ) { Text("+", style = MaterialTheme.typography.titleMedium) }

        if (penalidade != null) {
            Text(
                if (penalidade == 0) "0" else "$penalidade",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (penalidade < 0) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = "Modificador de distância: " +
                            if (penalidade < 0) "menos ${-penalidade}" else "zero"
                    },
                textAlign = TextAlign.End
            )
        } else {
            Text("", modifier = Modifier.weight(1f))
        }
    }
}

/**
 * Os dois avisos que a ficha já podia dar e não dava.
 *
 * O **1/2D** é o que mais escapa na mesa, porque não muda o ataque — muda o
 * **dano**, que sai pela metade. O jogador rola, acerta, e comemora um dano que
 * na verdade é metade daquilo.
 */
@Composable
private fun AvisoDeAlcance(metros: Int, alcance: AlcanceDoAtaque.Alcance) {
    val max = alcance.maximo
    val meio = alcance.meioDano

    when {
        max != null && metros > max -> Text(
            "Fora de alcance: o Máx da arma é $max m.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Medium
        )
        meio != null && metros > meio -> Text(
            "Além do 1/2D ($meio m): o dano sai pela metade.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun LinhaDeMira(
    opcao: MiraRules.Opcao,
    nhBase: Int,
    isPraCegoVariant: Boolean,
    onEscolher: () -> Unit
) {
    val nh = opcao.nhCom(nhBase)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = appCardColors()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEscolher() }
                .semantics { contentDescription = opcao.descricaoAcessivel(nhBase) }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    opcao.rotulo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                opcao.detalhe?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Text(
                if (isPraCegoVariant) "Rolar ($nh)" else "$nh",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                // NH negativo é informação, não erro: mostra em vermelho para o
                // jogador ver na hora que aquele alvo está fora de alcance.
                color = if (nh < 3) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
