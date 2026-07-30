package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.EstadosTemporarios
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.ui.linhaAlternavel

/**
 * O painel do **interruptor de estado** (Lote D-ESTADO), no mesmo lugar das
 * Braçais — pedido do usuário: *"coloque a caixa no mesmo lugar onde fica o ST e
 * DX braçal"*.
 *
 * ## Uma caixinha que tem GRAU
 *
 * Ao contrário das Braçais, que são liga/desliga, quase todo estado aqui tem
 * gravidade: a Dor Crônica é Suave, Grave ou Excruciante. E o grau **não está na
 * ficha** — a compra guarda custo, não o que aconteceu na mesa.
 *
 * Então o toque **cicla**: desligado → 1º grau → 2º → … → desligado. É o mesmo
 * gesto de sempre, e o rótulo diz em voz alta em que grau está e quanto ele
 * custa, para ninguém precisar contar toques.
 *
 * ⚠️ O `Checkbox` fica marcado em **qualquer** grau maior que zero: ele responde
 * "está valendo?", e o texto ao lado responde "quanto?". Um checkbox de três
 * estados não existe no Material, e inventar um confundiria o TalkBack.
 *
 * **Não renderiza nada** quando a ficha não tem nenhum dos nove estados — mesma
 * regra do painel de Reação, do de Autocontrole e das próprias Braçais.
 */
@Composable
fun PainelEstadosTemporarios(
    personagem: Personagem,
    graus: Map<String, Int>,
    onAlternar: (id: String, novoGrau: Int) -> Unit
) {
    val estados = EstadosTemporarios.disponiveis(personagem)
    if (estados.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        estados.forEach { estado ->
            val grau = graus[estado.id] ?: 0
            val ligado = grau > 0
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 32.dp)
                    .linhaAlternavel(
                        marcado = ligado,
                        descricao = descricaoAcessivel(estado, grau),
                        onAlternar = { onAlternar(estado.id, estado.proximoGrau(grau)) }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = ligado, onCheckedChange = null)
                Text(
                    estado.rotulo(grau),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (ligado) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }

        // A notinha do total. Só aparece com algo ligado, e existe porque um
        // número que muda sozinho sem dizer por quê é o defeito que este
        // projeto mais persegue.
        EstadosTemporarios.resumoAtivo(graus)?.let { resumo ->
            Text(
                resumo,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth().padding(top = 1.dp)
            )
        }
    }
}

/**
 * O que o TalkBack lê.
 *
 * ⚠️ Anuncia **o que o próximo toque faz**, e não só o estado atual: numa lista
 * que cicla, "marcado" sozinho não diz que ainda há graus pela frente. Quem
 * anuncia marcado/desmarcado é o papel de caixa de seleção, dado pelo
 * `linhaAlternavel`.
 */
private fun descricaoAcessivel(estado: EstadosTemporarios.Estado, grau: Int): String {
    val proximo = estado.proximoGrau(grau)
    val atual = if (grau == 0) {
        "${estado.nome} desligado."
    } else {
        "${estado.nome} no grau ${estado.graus[grau - 1].rotulo}, " +
            "${estado.modsDo(grau).resumo()}."
    }
    val oQueVem = if (proximo == 0) {
        "Tocar desliga."
    } else {
        "Tocar muda para ${estado.graus[proximo - 1].rotulo}."
    }
    return "$atual $oQueVem Vale ${estado.quando}."
}
