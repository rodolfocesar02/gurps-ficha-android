package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.SorteRules
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.ui.appCardColors

/**
 * **Usar Sorte** na última rolagem (Lote SORTE-1, MB p.90).
 *
 * A Sorte é a vantagem mais usada e a mais chata de operar na mão: rolar mais
 * duas vezes, comparar os três resultados, escolher o certo — e ainda olhar no
 * relógio para saber se já podia.
 *
 * Aparece só depois de uma rolagem, porque é sobre ela que a Sorte age. Some se
 * o personagem não tiver a vantagem.
 *
 * ## Por que o relógio está na tela
 *
 * O livro é explícito que é tempo **real**: *"o jogador precisa esperar uma hora
 * do tempo real (…). O personagem não pode utilizar Sorte às 11:58 e novamente às
 * 12:01."* Sem o relógio a vantagem vira honra — e ninguém lembra da hora no meio
 * da mesa.
 *
 * O botão desabilita e diz **quantos minutos faltam**, em vez de só desabilitar:
 * botão morto sem explicação é bug aos olhos de quem usa.
 */
@Composable
fun PainelSorte(
    personagem: Personagem,
    minutosDesdeUltimoUso: Long?,
    temRolagemParaRefazer: Boolean,
    onUsar: () -> Unit
) {
    if (!SorteRules.temAlguma(personagem)) return
    val grau = SorteRules.grauDe(personagem) ?: return

    val pode = SorteRules.podeUsar(personagem, minutosDesdeUltimoUso) && temRolagemParaRefazer

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = appCardColors()
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
            OutlinedButton(
                onClick = onUsar,
                enabled = pode,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription =
                            SorteRules.descricaoAcessivel(personagem, minutosDesdeUltimoUso)
                    }
            ) {
                Text(SorteRules.rotulo(personagem, minutosDesdeUltimoUso))
            }

            val explicacao = when {
                !temRolagemParaRefazer ->
                    "Role alguma coisa primeiro — a Sorte refaz a última rolagem."
                pode ->
                    "Rola mais duas vezes e fica com o melhor dos três. " +
                        "Depois, só em ${grau.minutos} min."
                else -> "Tempo real, como o livro manda (MB p.90)."
            }
            Text(
                explicacao,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
