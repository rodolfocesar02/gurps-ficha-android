package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.gurps.ficha.domain.rules.ReacaoRules
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.ui.appCardColors

/**
 * Teste de Reação (MB p.494), na aba Rolagem.
 *
 * O app nunca teve isto: o jogador rolava 3d6, consultava a tabela no livro e
 * lembrava de cabeça os modificadores das vantagens sociais. Agora o total já
 * vem somado, com a lista de onde cada ponto veio.
 *
 * Os modificadores vêm em dois sabores:
 *  - **fixos** (Carisma) entram direto no total;
 *  - **condicionais** (Voz Melodiosa, "de quem pode ouvir sua voz") viram
 *    caixinha para o jogador marcar — igual ao bônus condicional de perícia.
 *    Somar sempre daria bônus contra surdos e contra máquinas.
 *
 * ## ⚠️ Este painel aparece SEMPRE — e é exceção de propósito
 *
 * Ele já foi condicional, como o de Autocontrole. Pedido do usuário em 31/07:
 * *"é um teste que não depende de nenhuma habilidade ou perícia, zero, mas
 * sempre pode ocorrer do Mestre pedir — pode deixar no topo"*.
 *
 * E a diferença entre os dois painéis é real, não gosto:
 *
 * - **Reação** existe para **qualquer** personagem. Sem nenhum traço social, o
 *   modificador é **+0** — e +0 é um número, não uma ausência. O Mestre pede o
 *   teste do mesmo jeito.
 * - **Autocontrole** só existe se a ficha tiver desvantagem com NA. Sem ela não
 *   há **nada** para rolar, e a tela continua escondendo o painel.
 *
 * É a mesma decisão das linhas de valor zero do DESL-2: *"Voando: 0"* ensina a
 * regra, enquanto a linha que simplesmente não existe deixa o jogador sem saber
 * se é zero ou se o app esqueceu.
 */
@Composable
fun PainelReacao(
    personagem: Personagem,
    isPraCegoVariant: Boolean,
    onRolar: (label: String, alvo: Int?, mod: Int) -> Unit
) {
    val chave = personagem.vantagensTotais to personagem.desvantagensTotais
    val fixos = remember(chave) { ReacaoRules.modificadoresDe(personagem) }
    val condicionais = remember(chave) { ReacaoRules.condicionaisDe(personagem) }
    // Sem `return` aqui: ver o ⚠️ no topo do arquivo. O painel vale para toda
    // ficha, com +0 quando não há traço social nenhum.

    var marcados by remember(chave) { mutableStateOf(emptySet<Int>()) }

    val totalFixo = fixos.sumOf { it.valor }
    val total = totalFixo + somaDosMarcados(condicionais, marcados)
    val sinal = if (total >= 0) "+$total" else "$total"

    if (isPraCegoVariant) SectionHeaderPraCego("Teste de Reação")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = appCardColors()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            // Só o cabeçalho rola. As caixinhas ficam FORA do clicável: dentro
            // dele, marcar uma condição dispararia a rolagem junto.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // Reação NÃO tem NH: rola 3d6 e consulta a tabela. O
                    // modificador vai como `mod`, e o alvo fica nulo de propósito.
                    .clickable { onRolar("Reação ($sinal)", null, total) }
                    .semantics {
                        contentDescription = "Rolar teste de reação. Modificador $sinal."
                    }
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Teste de Reação",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Text(
                    if (isPraCegoVariant) "Rolar ($sinal)" else sinal,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    softWrap = false
                )
            }

            // A "notinha": de onde vem cada ponto do modificador fixo. Com a
            // ficha sem traço social, ela explica o ZERO — senão o jogador olha
            // um "+0" solto e não sabe se o app calculou ou desistiu.
            Text(
                if (fixos.isEmpty()) {
                    "Sem modificador de traço. Role 3d6 e consulte a tabela (MB p.494)."
                } else {
                    fixos.joinToString(", ") {
                        "${it.nomeDoTraco} ${if (it.valor >= 0) "+${it.valor}" else "${it.valor}"}"
                    }
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1
            )

            PainelBonusCondicional(
                bonus = condicionais,
                marcados = marcados,
                onAlternar = { i -> marcados = if (i in marcados) marcados - i else marcados + i }
            )
        }
    }
}
