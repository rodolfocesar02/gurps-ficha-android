package com.gurps.ficha.ui.features.equipamento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.gurps.ficha.domain.rules.FichaDeEquipamento
import com.gurps.ficha.domain.rules.FichaTecnicaDaArma
import com.gurps.ficha.ui.FullscreenDialogContainer
import com.gurps.ficha.ui.PrimaryActionButton
import com.gurps.ficha.ui.UiActionLabels
import com.gurps.ficha.ui.appCardColors

/**
 * **O card de detalhe de um item do catálogo** (Lote ARMA-3; generalizado no EQP-6).
 *
 * Serve arma, armadura e escudo. Ele não sabe qual é: recebe uma
 * [FichaDeEquipamento.Ficha] pronta e desenha os blocos que vierem preenchidos —
 * o de **modos de ataque** simplesmente não aparece para quem não ataca.
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
fun CardDetalheDoItem(
    ficha: FichaDeEquipamento.Ficha,
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

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                BlocosDaFicha(ficha)
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
            ?.let { FichaDeEquipamento.Linha("Dano", it) },
        equipamento.armaStMinimo?.let { FichaDeEquipamento.Linha("ST mínima", "$it") },
        equipamento.armaPrecisao?.let { FichaDeEquipamento.Linha("Precisão", "$it") },
        equipamento.armaAlcanceCorpoACorpo?.takeIf { it.isNotBlank() }
            ?.let { FichaDeEquipamento.Linha("Alcance", "$it m") },
        equipamento.armaTirosRaw?.takeIf { it.isNotBlank() }?.let {
            FichaDeEquipamento.Linha("Tiros", it, FichaTecnicaDaArma.explicarTiros(it))
        },
        equipamento.armaRecuo?.let {
            FichaDeEquipamento.Linha("Recuo", "$it", FichaTecnicaDaArma.explicarRecuo(it))
        },
        FichaDeEquipamento.Linha("Peso", "${FichaDeEquipamento.formatarKg(equipamento.peso)} kg"),
        FichaDeEquipamento.Linha("Custo", FichaDeEquipamento.formatarDinheiro(equipamento.custo))
    )

    CardDetalheDoItem(
        ficha = FichaDeEquipamento.Ficha(
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

/**
 * **Os blocos da ficha** — os mesmos no card de seleção e dentro do editor.
 *
 * 🔴 **Lote EQP-7.** O editor tinha o seu próprio desenho da ficha: uma lista
 * chapada de `rótulo → valor`, sem os cartões e sem os títulos de bloco. A mesma
 * *Túnica* aparecia de duas formas — completa ao escolher, crua ao editar —, que
 * é exatamente o defeito que o LAYOUT-7 tinha consertado para a arma.
 *
 * ⚠️ Voltou porque o conserto de lá foi feito **para a arma**, não para "um item
 * do catálogo". Agora há **um** desenho, e a única forma de divergirem de novo é
 * alguém escrever um terceiro.
 *
 * ⚠️ Emite conteúdo de `Column`, não `LazyColumn`, porque o editor já vive dentro
 * de um `verticalScroll` — lista preguiçosa dentro de coluna rolável estoura a
 * altura. As fichas têm menos de dez linhas; a preguiça não comprava nada.
 */
@Composable
fun ColumnScope.BlocosDaFicha(
    ficha: FichaDeEquipamento.Ficha,
    /**
     * `false` no editor: as linhas que tem campo logo abaixo somem daqui,
     * para o mesmo numero nao aparecer duas vezes na mesma tela (EQP-8).
     */
    mostrarEditaveis: Boolean = true
) {
    fun visiveis(linhas: List<FichaDeEquipamento.Linha>) =
        if (mostrarEditaveis) linhas else linhas.filterNot { it.editavel }

    val destaques = visiveis(ficha.destaques)
    val detalhes = visiveis(ficha.detalhes)

    if (destaques.isNotEmpty()) {
        Cabecalho("No meio da jogada")
        destaques.forEach { LinhaDaFicha(it) }
    }

    if (ficha.modos.isNotEmpty()) {
        Cabecalho(if (ficha.modos.size > 1) "Modos de ataque" else "Ataque")
        ficha.modos.forEach { ModoDeAtaque(it) }
    }

    if (detalhes.isNotEmpty()) {
        Cabecalho("Na hora de comprar")
        detalhes.forEach { LinhaDaFicha(it) }
    }

    if (ficha.observacoes.isNotEmpty()) {
        Cabecalho("Observações do livro")
        ficha.observacoes.forEach { texto ->
            Text(
                texto,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
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
private fun LinhaDaFicha(linha: FichaDeEquipamento.Linha) {
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
                color = if (linha.valor == FichaDeEquipamento.AUSENTE) {
                    MaterialTheme.colorScheme.outline
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }
    }
}

@Composable
private fun ModoDeAtaque(modo: FichaDeEquipamento.ModoNaTela) {
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
