package com.gurps.ficha.ui.features.equipamento

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
import com.gurps.ficha.domain.rules.FichaTecnicaDaArma
import com.gurps.ficha.ui.FullscreenDialogContainer
import com.gurps.ficha.ui.PrimaryActionButton
import com.gurps.ficha.ui.UiActionLabels
import com.gurps.ficha.ui.appCardColors

/**
 * **O card de detalhe da arma** (Lote ARMA-3).
 *
 * ## O que muda no gesto
 *
 * Antes, tocar numa arma da lista **já a jogava no inventário**. Agora o toque
 * abre este card, e o botão de adicionar mora aqui dentro. São dois toques em
 * vez de um — e é o certo: hoje dá para comprar uma arma sem descobrir que ela
 * pesa 7,3 kg, exige as duas mãos e é CL 1.
 *
 * ## Por que este arquivo é fino
 *
 * Ele não decide nada. Quem monta as linhas, traduz as siglas e faz a conta do
 * alcance com a ST é o `FichaTecnicaDaArma`, que é Kotlin puro e tem teste. Aqui
 * só se desenha o que já veio pronto.
 *
 * ⚠️ Nasceu em arquivo próprio porque o `TabEquipamentos.kt` está em 801 linhas
 * e o teto do projeto é 1000.
 */
@Composable
fun CardDetalheArma(
    ficha: FichaTecnicaDaArma.Ficha,
    /** Texto do botão principal. Nulo esconde o botão (modo só-leitura, ARMA-4). */
    rotuloAcao: String? = UiActionLabels.ADICIONAR,
    onAcao: () -> Unit = {},
    onDismiss: () -> Unit
) {
    FullscreenDialogContainer(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                ficha.nome,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    ficha.subtitulo,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f)
                )
                ficha.selo?.let { selo ->
                    Text(
                        selo,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f).padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (ficha.destaques.isNotEmpty()) {
                    item { Cabecalho("No meio da jogada") }
                    items(ficha.destaques) { LinhaDaFicha(it) }
                }

                if (ficha.modos.isNotEmpty()) {
                    item {
                        Cabecalho(
                            if (ficha.modos.size > 1) "Modos de ataque" else "Ataque"
                        )
                    }
                    items(ficha.modos) { ModoDeAtaque(it) }
                }

                item { Cabecalho("Na hora de comprar") }
                items(ficha.detalhes) { LinhaDaFicha(it) }

                if (ficha.observacoes.isNotEmpty()) {
                    item { Cabecalho("Observações do livro") }
                    items(ficha.observacoes) { texto ->
                        Text(
                            texto,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (rotuloAcao != null) {
                PrimaryActionButton(text = rotuloAcao, onClick = onAcao)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text(UiActionLabels.FECHAR) }
            }
        }
    }
}

/**
 * **A arma que não casa com o catálogo** (Lote ARMA-4).
 *
 * Acontece com arma criada à mão e com item de ficha antiga cujo id mudou. Aqui
 * a tela mostra **o que a ficha guarda** e diz por que o resto falta — abrir um
 * card vazio, ou não abrir nada, faria o jogador achar que o app quebrou.
 */
@Composable
fun CardArmaForaDoCatalogo(
    equipamento: com.gurps.ficha.model.Equipamento,
    onDismiss: () -> Unit
) {
    val linhas = listOfNotNull(
        equipamento.armaDanoRaw?.takeIf { it.isNotBlank() }
            ?.let { FichaTecnicaDaArma.Linha("Dano", it) },
        equipamento.armaStMinimo?.let { FichaTecnicaDaArma.Linha("ST mínima", "$it") },
        equipamento.armaPrecisao?.let { FichaTecnicaDaArma.Linha("Precisão", "$it") },
        equipamento.armaAlcanceCorpoACorpo?.takeIf { it.isNotBlank() }
            ?.let { FichaTecnicaDaArma.Linha("Alcance", "$it m") },
        equipamento.armaTirosRaw?.takeIf { it.isNotBlank() }?.let {
            FichaTecnicaDaArma.Linha("Tiros", it, FichaTecnicaDaArma.explicarTiros(it))
        },
        equipamento.armaRecuo?.let {
            FichaTecnicaDaArma.Linha("Recuo", "$it", FichaTecnicaDaArma.explicarRecuo(it))
        },
        FichaTecnicaDaArma.Linha("Peso", "${FichaTecnicaDaArma.formatarKg(equipamento.peso)} kg"),
        FichaTecnicaDaArma.Linha("Custo", FichaTecnicaDaArma.formatarDinheiro(equipamento.custo))
    )

    CardDetalheArma(
        ficha = FichaTecnicaDaArma.Ficha(
            nome = equipamento.nome,
            subtitulo = "Arma fora do catálogo",
            selo = null,
            destaques = emptyList(),
            modos = emptyList(),
            detalhes = linhas,
            observacoes = listOf(
                "Esta arma não casou com nenhuma linha do catálogo, então só " +
                    "aparece o que está gravado na ficha. Alcance, CdT, " +
                    "Magnitude e Classe de Legalidade vêm do catálogo e por " +
                    "isso ficam de fora."
            )
        ),
        rotuloAcao = null,
        onDismiss = onDismiss
    )
}

@Composable
private fun Cabecalho(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
    )
}

/**
 * Uma linha da ficha.
 *
 * ⚠️ O `contentDescription` cobre a linha **inteira**. Uma tabela lida célula a
 * célula pelo TalkBack — "Precisão", pausa, "6 mais 3", pausa — não diz nada;
 * é preciso ouvir rótulo, valor e explicação de uma vez.
 */
@Composable
private fun LinhaDaFicha(linha: FichaTecnicaDaArma.Linha) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = linha.descricaoAcessivel },
        shape = RoundedCornerShape(12.dp),
        colors = appCardColors()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    linha.rotulo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                linha.explicacao?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Text(
                linha.valor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (linha.valor == FichaTecnicaDaArma.AUSENTE) {
                    MaterialTheme.colorScheme.outline
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }
    }
}

@Composable
private fun ModoDeAtaque(modo: FichaTecnicaDaArma.ModoNaTela) {
    val acessivel = buildString {
        append("Modo ${modo.ordem}: ${modo.dano}")
        modo.danoComSt?.let { append(", que com a sua força é $it") }
        modo.detalhe?.let { append(". $it") }
        if (modo.mesmaArma) append(". Mesma arma, não custa nem pesa de novo.")
    }
    Card(
        modifier = Modifier.fillMaxWidth().semantics { contentDescription = acessivel },
        shape = RoundedCornerShape(12.dp),
        colors = appCardColors()
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modo.dano,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                modo.danoComSt?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            modo.detalhe?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            // Sem esta linha, o custo nulo do 2º modo viraria "de graça" na
            // cabeça de quem lê.
            if (modo.mesmaArma) {
                Text(
                    "mesma arma — não custa nem pesa de novo",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
