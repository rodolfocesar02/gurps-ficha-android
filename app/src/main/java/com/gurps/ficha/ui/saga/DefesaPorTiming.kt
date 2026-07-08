package com.gurps.ficha.ui.saga

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gurps.ficha.domain.combat.CombatResolver
import com.gurps.ficha.viewmodel.delegates.DefesaPendenteUi
import kotlinx.coroutines.isActive

/**
 * Lote HEX-9 (Fase 7 do PILAR — polimento): DEFESA POR TIMING inspirada em Clair Obscur.
 *
 * Quando o NPC ataca, o herói tem uma JANELA CURTA (1 segundo) pra tocar em ESQUIVAR/APARAR/BLOQUEAR.
 * A rapidez do toque vira BÔNUS ou PENALIDADE na defesa (via `ComponenteMod("timing")` para o feed
 * do Narrador mostrar o abatimento). Se a janela expirar, o sistema auto-seleciona a opção com maior
 * `valorFinal` disponível (defesa padrão) SEM bônus — o herói tem defesa mesmo se não reagir.
 *
 * A regra GURPS não muda — só a UX. O card mexe apenas no `valorFinal` da `OpcaoDefesa` escolhida
 * antes de chamar `escolherDefesa(opcao)` no controller.
 */

/** Regras puras da janela de timing (testável sem Compose). */
object DefesaPorTimingRegras {
    /** Duração da janela reativa (MS). */
    const val JANELA_MS: Long = 1000L

    /** Delta aplicado à defesa em função do tempo de reação. */
    data class Bonus(val delta: Int, val rotulo: String)

    /** Marcador especial: janela expirada — não aplica bônus e a UI seleciona a opção padrão. */
    val BONUS_EXPIRADO = Bonus(delta = 0, rotulo = "expirado")

    /**
     * Bônus/penalidade pela rapidez do toque:
     *   < 300 ms  → +1 (perfeito — reflexo felino)
     *   < 600 ms  →  0 (bom — reação normal)
     *   < 1000 ms → −1 (tarde — defesa apressada)
     *   ≥ 1000 ms → EXPIRADO (fallback sem bônus, defesa padrão)
     */
    fun bonusPorTempoMs(reacaoMs: Long): Bonus = when {
        reacaoMs < 300L -> Bonus(delta = +1, rotulo = "perfeito")
        reacaoMs < 600L -> Bonus(delta =  0, rotulo = "bom")
        reacaoMs < JANELA_MS -> Bonus(delta = -1, rotulo = "tarde")
        else -> BONUS_EXPIRADO
    }

    /** Aplica o [bonus] à [opcao], somando no `valorFinal` e adicionando um `ComponenteMod("timing …")`. */
    fun aplicarBonus(opcao: CombatResolver.OpcaoDefesa, bonus: Bonus): CombatResolver.OpcaoDefesa {
        if (bonus === BONUS_EXPIRADO || bonus.delta == 0) return opcao
        return opcao.copy(
            valorFinal = opcao.valorFinal + bonus.delta,
            componentes = opcao.componentes + CombatResolver.ComponenteMod("timing (${bonus.rotulo})", bonus.delta)
        )
    }

    /** Opção de defesa padrão para o auto-select quando a janela expira: maior `valorFinal` disponível. */
    fun opcaoPadrao(opcoes: List<CombatResolver.OpcaoDefesa>): CombatResolver.OpcaoDefesa? =
        opcoes.filter { it.disponivel }.maxByOrNull { it.valorFinal }

    /**
     * Fallback total: [opcaoPadrao] preferida, ou a PRIMEIRA opção (mesmo indisponível) para que o
     * motor de combate receba uma resposta e o `CompletableDeferred` complete — evita o card ficar
     * ETERNAMENTE aberto quando o motor mandou uma lista com todas indisponíveis (situação rara mas
     * possível: apara/bloqueio consumidos e o esquiva marcada indisponível por deitado/atordoado).
     */
    fun opcaoPadraoOuFallback(opcoes: List<CombatResolver.OpcaoDefesa>): CombatResolver.OpcaoDefesa? =
        opcaoPadrao(opcoes) ?: opcoes.firstOrNull()
}

/** Cor da barra de urgência: verde → amarelo → vermelho conforme [fracaoDecorrida] (0..1). */
private fun corUrgencia(fracaoDecorrida: Float): Color {
    val f = fracaoDecorrida.coerceIn(0f, 1f)
    return when {
        f < 0.3f -> Color(0xFF10B981) // verde: janela do "perfeito"
        f < 0.6f -> Color(0xFFF59E0B) // âmbar: janela do "bom"
        else -> Color(0xFFEF4444)      // vermelho: janela do "tarde"
    }
}

/** Rótulo pt-BR do TipoDefesa para o botão. */
private fun rotuloDefesa(tipo: CombatResolver.TipoDefesa): String = when (tipo) {
    CombatResolver.TipoDefesa.ESQUIVA -> "ESQUIVAR"
    CombatResolver.TipoDefesa.APARA -> "APARAR"
    CombatResolver.TipoDefesa.BLOQUEIO -> "BLOQUEAR"
}

/**
 * Card modal que aparece durante a janela de defesa. Renderiza uma barra de progresso que decai
 * em `JANELA_MS`, botões grandes por opção disponível, e uma legenda mostrando o bônus corrente
 * ("perfeito"/"bom"/"tarde"). Se a janela expirar, invoca [onEscolher] automaticamente com a opção
 * padrão SEM bônus.
 */
@Composable
fun DefesaPorTimingCard(
    pendente: DefesaPendenteUi,
    onEscolher: (CombatResolver.OpcaoDefesa) -> Unit,
) {
    // Fase "arrancada": marcamos o instante em que o card apareceu para o defensor.
    val inicio = remember(pendente) { System.currentTimeMillis() }

    // Fração 0..1 do tempo decorrido; recomputada a cada frame via withFrameNanos.
    var fracao by remember(pendente) { mutableFloatStateOf(0f) }
    var expirou by remember(pendente) { mutableStateOf(false) }
    // Trava contra dupla chamada de onEscolher — o CompletableDeferred do controller só completa uma
    // vez, mas evitar segunda chamada mantém a UX limpa (botão vira "morto" após clique/expiração).
    var jaEscolheu by remember(pendente) { mutableStateOf(false) }

    val escolherUmaVez: (CombatResolver.OpcaoDefesa) -> Unit = { opcao ->
        if (!jaEscolheu) {
            jaEscolheu = true
            onEscolher(opcao)
        }
    }

    LaunchedEffect(pendente) {
        while (isActive && !expirou) {
            androidx.compose.runtime.withFrameNanos { /* ignora nanos, usa clock real */ }
            val decorrido = System.currentTimeMillis() - inicio
            val f = (decorrido.toFloat() / DefesaPorTimingRegras.JANELA_MS).coerceIn(0f, 1f)
            fracao = f
            if (decorrido >= DefesaPorTimingRegras.JANELA_MS) {
                expirou = true
                // Fallback total: usa opcaoPadrao, ou a 1ª opção (mesmo indisponível), para evitar o
                // card ficar eternamente aberto se todas indisponíveis. Só não chama se a lista de
                // opções está VAZIA (não deveria acontecer — o motor sempre manda ao menos uma).
                val padrao = DefesaPorTimingRegras.opcaoPadraoOuFallback(pendente.opcoes)
                if (padrao != null) escolherUmaVez(padrao)
                break
            }
        }
    }

    val corBarra = corUrgencia(fracao)
    val bonusCorrente = DefesaPorTimingRegras.bonusPorTempoMs(System.currentTimeMillis() - inicio)
    val rotuloBonus = if (bonusCorrente === DefesaPorTimingRegras.BONUS_EXPIRADO) "—" else bonusCorrente.rotulo
    val opcoesDisponiveis = pendente.opcoes.filter { it.disponivel }

    Dialog(
        onDismissRequest = { /* jogador NÃO dispensa por back — deixe expirar */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A2632), RoundedCornerShape(12.dp))
                .border(2.dp, corBarra, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⚔ Defenda-se!", color = Color.White, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Text(rotuloBonus.uppercase(), color = corBarra, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge)
            }
            Text(pendente.descricaoAtaque, color = Color(0xCCFFFFFF),
                style = MaterialTheme.typography.bodyMedium)
            LinearProgressIndicator(
                progress = { 1f - fracao },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = corBarra,
                trackColor = Color(0x33FFFFFF)
            )
            for (opcao in opcoesDisponiveis) {
                Button(
                    onClick = {
                        val agora = System.currentTimeMillis()
                        val bonus = DefesaPorTimingRegras.bonusPorTempoMs(agora - inicio)
                        escolherUmaVez(DefesaPorTimingRegras.aplicarBonus(opcao, bonus))
                    },
                    // Botões ficam DESABILITADOS após escolha (ou expiração) para bloquear
                    // qualquer segundo tap enquanto o ViewModel ainda não removeu defesaPendente.
                    enabled = !jaEscolheu,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = corBarra),
                ) {
                    Text(
                        "${rotuloDefesa(opcao.tipo)}  ${opcao.valorFinal}" +
                            if (opcao.recuo) "  (recuo)" else "",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (opcoesDisponiveis.isEmpty()) {
                Text("Nenhuma defesa disponível — o golpe passa.",
                    color = Color(0xFFEF4444), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
