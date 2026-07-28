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
 * **Não renderiza nada** quando o personagem não tem nenhum traço que mexa em
 * reação — mesma regra do painel de autocontrole e da aba Magia.
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
    if (fixos.isEmpty() && condicionais.isEmpty()) return

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

            // A "notinha": de onde vem cada ponto do modificador fixo.
            if (fixos.isNotEmpty()) {
                Text(
                    fixos.joinToString(", ") {
                        "${it.nomeDoTraco} ${if (it.valor >= 0) "+${it.valor}" else "${it.valor}"}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )
            }

            PainelBonusCondicional(
                bonus = condicionais,
                marcados = marcados,
                onAlternar = { i -> marcados = if (i in marcados) marcados - i else marcados + i }
            )
        }
    }
}
