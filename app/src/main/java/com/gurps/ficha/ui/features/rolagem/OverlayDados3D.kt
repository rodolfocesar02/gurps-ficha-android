package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.gurps.ficha.domain.roll.CriticoRules
import com.gurps.ficha.domain.rules.TiroContinuoRules
import com.gurps.ficha.domain.rules.MauFuncionamentoRules
import com.gurps.ficha.ui.PendingRollState
import com.gurps.ficha.ui.features.dice3d.Dice3DScene

/**
 * Camada que cobre a tela enquanto os dados 3D rolam, e mostra o resultado.
 *
 * Extraído de `TabRolagem.kt` no Lote UI-2 — o arquivo tinha 1.128 linhas e
 * estourava o teto de 1.000. Comportamento idêntico ao anterior: só mudou de
 * casa. A tela não muda para o usuário.
 *
 * Quem controla o estado continua sendo a `TabRolagem`; aqui só se lê e se
 * avisa por callback.
 */
@Composable
fun OverlayDados3D(
    pendingRoll: PendingRollState,
    pendingResults: List<Int>?,
    isPraCegoVariant: Boolean,
    modificadorGlobalPraCego: Int,
    onResultadosProntos: (List<Int>) -> Unit,
    onCancelar: () -> Unit,
    onConfirmar: (PendingRollState, List<Int>) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(100f)
            .background(Color.Black.copy(alpha = 0.3f))
    ) {
        Dice3DScene(
            modifier = Modifier.fillMaxSize(),
            diceCount = pendingRoll.diceCount,
            onRollFinished = { resultados ->
                if (pendingResults == null) onResultadosProntos(resultados)
            }
        )

        if (pendingResults != null) {
            val soma = pendingResults.sum()
            val msgPrincipal = textoDoResultado(
                pr = pendingRoll,
                soma = soma,
                isPraCegoVariant = isPraCegoVariant,
                modificadorGlobalPraCego = modificadorGlobalPraCego
            )

            val view = LocalView.current
            LaunchedEffect(pendingResults) {
                view.announceForAccessibility(
                    anuncioDoResultado(
                        pr = pendingRoll,
                        soma = soma,
                        isPraCegoVariant = isPraCegoVariant,
                        modificadorGlobalPraCego = modificadorGlobalPraCego
                    )
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = msgPrincipal,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = { onConfirmar(pendingRoll, pendingResults) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9))
                ) {
                    Text("Confirmar")
                }
            }
        } else {
            Button(
                onClick = onCancelar,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .padding(top = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.5f))
            ) {
                Text("Cancelar Teste")
            }
        }
    }
}

/**
 * Texto grande exibido na tela ("Sucesso\n(por 3)").
 *
 * Kotlin puro, sem Compose, de propósito: é a única parte com regra de verdade
 * (classificação de crítico e margem) e assim fica coberta por teste unitário.
 */
internal fun textoDoResultado(
    pr: PendingRollState,
    soma: Int,
    isPraCegoVariant: Boolean,
    modificadorGlobalPraCego: Int,
    // ── Lotes MB-2 e MB-8: só chegam preenchidos num ataque com arma de fogo. ──
    /** Rcl da arma; nulo em quem não atira. */
    recuoDaArma: Int? = null,
    /** CdT usada neste ataque. Acima de 1 vira rajada (MB p.549). */
    tirosDisparados: Int? = null,
    /** NT da arma — é dele que sai o Mauf (MB p.407). Nulo desliga a regra. */
    ntDaArma: Int? = null,
    /** A regra de mau funcionamento é OPCIONAL no livro; o Mestre decide. */
    mauFuncionamentoLigado: Boolean = false,
    /** 3d6 já rolados para a tabela de falha, quando ela dispara. */
    somaDaTabelaDeFalha: Int = 10
): String = when {
    pr.isDano -> "Dano: ${(soma + pr.mod).coerceAtLeast(1)}"
    pr.isPersonalizada -> "Resultado: ${soma + pr.mod}"
    else -> {
        val alvoEfetivo = alvoEfetivoDe(pr, isPraCegoVariant, modificadorGlobalPraCego)
        if (alvoEfetivo == null) {
            "Rolagem: $soma"
        } else {
            val dist = alvoEfetivo - soma
            val margem = kotlin.math.abs(dist)
            val base = when (CriticoRules.classificar(soma, alvoEfetivo)) {
                CriticoRules.ResultadoCritico.DECISIVO -> "Sucesso Crítico!\n(por $margem)"
                CriticoRules.ResultadoCritico.FALHA_CRITICA -> "Falha Crítica!\n(por $margem)"
                else -> if (dist >= 0) "Sucesso\n(por $margem)" else "Falha\n(por $margem)"
            }
            base + extrasDoTiro(
                soma, dist, recuoDaArma, tirosDisparados,
                ntDaArma, mauFuncionamentoLigado, somaDaTabelaDeFalha
            )
        }
    }
}

/**
 * O que a arma de fogo acrescenta ao resultado: rajada e enguiço.
 *
 * ⚠️ **O enguiço vem primeiro, e por um motivo de regra.** Ele depende do dado
 * **cru**, não de ter acertado — a arma pode enguiçar num resultado que teria
 * acertado (MB p.407). Contar os tiros de uma arma que travou seria mentir sobre
 * o que aconteceu no turno.
 */
private fun extrasDoTiro(
    soma: Int,
    dist: Int,
    recuo: Int?,
    tiros: Int?,
    nt: Int?,
    mauFuncionamentoLigado: Boolean,
    somaDaTabela: Int
): String {
    if (mauFuncionamentoLigado) {
        val mauf = MauFuncionamentoRules.maufPorNt(nt)
        if (MauFuncionamentoRules.enguicou(soma, mauf)) {
            return "\n" + MauFuncionamentoRules.explicacao(soma, mauf, somaDaTabela)
        }
    }
    if (dist >= 0 && TiroContinuoRules.ehRajada(tiros)) {
        return "\n" + TiroContinuoRules.explicacao(dist, recuo, tiros)
    }
    return ""
}

/**
 * Texto falado pelo TalkBack. Formato diferente do [textoDoResultado]: inclui o
 * rótulo do teste e escreve por extenso, porque quem ouve não vê a tela.
 */
internal fun anuncioDoResultado(
    pr: PendingRollState,
    soma: Int,
    isPraCegoVariant: Boolean,
    modificadorGlobalPraCego: Int
): String = when {
    pr.isDano -> "${pr.contextoLabel} causou ${(soma + pr.mod).coerceAtLeast(1)} de Dano"
    pr.isPersonalizada -> "${pr.contextoLabel} rolou ${soma + pr.mod}"
    else -> {
        val alvoEfetivo = alvoEfetivoDe(pr, isPraCegoVariant, modificadorGlobalPraCego)
        if (alvoEfetivo == null) {
            "${pr.contextoLabel}. Rolou $soma"
        } else {
            val dist = alvoEfetivo - soma
            val margem = kotlin.math.abs(dist)
            val status = when (CriticoRules.classificar(soma, alvoEfetivo)) {
                CriticoRules.ResultadoCritico.DECISIVO -> "Sucesso Crítico por $margem"
                CriticoRules.ResultadoCritico.FALHA_CRITICA -> "Falha Crítica por $margem"
                else -> if (dist >= 0) "Passou por $margem" else "Falhou por $margem"
            }
            "${pr.contextoLabel} (NH $alvoEfetivo). $status"
        }
    }
}

/** NH alvo já com o modificador do teste e o modificador global da variante PraCego. */
private fun alvoEfetivoDe(
    pr: PendingRollState,
    isPraCegoVariant: Boolean,
    modificadorGlobalPraCego: Int
): Int? {
    val modEfetivo = pr.mod + (if (isPraCegoVariant) modificadorGlobalPraCego else 0)
    return pr.alvo?.plus(modEfetivo)
}
