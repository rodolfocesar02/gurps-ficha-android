package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.magic.MagicClassParser
import com.gurps.ficha.domain.magic.TipoClasseMagia
import com.gurps.ficha.domain.rules.MagiaEnergiaRules
import com.gurps.ficha.domain.rules.TamanhoDoAlvoRules

/**
 * **MG-4 — o que muda a conta do custo na hora de conjurar** (Magia p.8, p.11 e p.13), as duas perguntas que só o jogador sabe:
 *
 * - **"Operar como Bloqueio"** (só nas "Comum ou Bloqueio", 8 mágicas): como Bloqueio (de defesa, no turno de um inimigo) o NH
 *   alto não reduz o custo; como Comum, reduz como sempre.
 * - **O tamanho do alvo (MT)**, nas mágicas Comuns sem conta própria de custo: MT positivo multiplica o custo por `1 + MT`
 *   (×2 para +1, ×3 para +2...), ANTES de tirar o NH. MT negativo não reduz.
 *
 * A conta é a de [MagiaEnergiaRules.contaDoCusto], a mesma que a Mesa roda em JavaScript. Este composable só pergunta; quem
 * calcula é o diálogo do gasto, que lê o que se escolheu aqui.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustoPorTamanhoEBloqueio(
    magia: MagiaRollOption,
    comoBloqueio: Boolean,
    mtAlvo: Int,
    isPraCegoVariant: Boolean,
    onComoBloqueio: (Boolean) -> Unit,
    onMt: (Int) -> Unit
) {
    val classes = MagicClassParser.parse(magia.classe).classes
    val ehHibrida = TipoClasseMagia.BLOQUEIO in classes && TipoClasseMagia.COMUM in classes
    val perguntaOTamanho = MagiaEnergiaRules.tamanhoMultiplica(
        magia.classe, magia.definicaoId, temCondicao = magia.custoPorCondicao != null, comoBloqueio = comoBloqueio
    )
    if (!ehHibrida && !perguntaOTamanho) return

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (ehHibrida) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onComoBloqueio(!comoBloqueio) }
            ) {
                Checkbox(
                    checked = comoBloqueio,
                    onCheckedChange = onComoBloqueio,
                    modifier = Modifier.semantics {
                        if (isPraCegoVariant) {
                            contentDescription = "Operar ${magia.nome} como Bloqueio, de defesa: o nível de habilidade não reduz o custo"
                        }
                    }
                )
                Text(
                    "Operar como Bloqueio (defesa): o NH não reduz o custo (Magia p.8)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        if (perguntaOTamanho) {
            Text(
                "Tamanho do alvo (MT). Mágica Comum: MT positivo multiplica o custo por 1 + MT (Magia p.11). 0 = humano.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Do humano (0) para cima: MT negativo não muda o custo, então não é uma escolha.
                TamanhoDoAlvoRules.DEGRAUS.filter { it.mt >= 0 }.forEach { d ->
                    FilterChip(
                        selected = mtAlvo == d.mt,
                        onClick = { onMt(d.mt) },
                        label = { Text(d.rotulo) },
                        modifier = Modifier.semantics {
                            if (isPraCegoVariant) contentDescription = "Tamanho do alvo, MT ${d.mt}: ${d.exemplo}"
                        }
                    )
                }
            }
        }
    }
}
