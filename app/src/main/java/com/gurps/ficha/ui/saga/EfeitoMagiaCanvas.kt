package com.gurps.ficha.ui.saga

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Lote VFX-1 (protótipo): desenha um [EfeitoVfx] no Canvas do combate, entre [origem] (o conjurador)
 * e [alvo]. Partículas puras — sem dependência nova, mesma tecnologia do grid hexagonal.
 *
 * `progresso` (0f→1f) é a linha do tempo do efeito; o chamador anima com [EfeitoMagiaOverlay] ou
 * passa um valor fixo (útil no @Preview e nos testes de render). As partículas são DETERMINÍSTICAS
 * (semente fixa por efeito) — o mesmo efeito desenha igual todo frame, sem "chuvisco" aleatório.
 */
fun DrawScope.desenharEfeitoVfx(
    efeito: EfeitoVfx,
    origem: Offset,
    alvo: Offset,
    progresso: Float,
    raioHex: Float,
) {
    val p = progresso.coerceIn(0f, 1f)
    when (efeito.arquetipo) {
        ArquetipoVfx.PROJETIL -> desenharProjetil(efeito.paleta, origem, alvo, p, raioHex)
        ArquetipoVfx.EXPLOSAO -> desenharExplosao(efeito.paleta, alvo, p, raioHex)
        ArquetipoVfx.FLASH -> desenharFlash(efeito.paleta, alvo, p, raioHex)
        ArquetipoVfx.TOQUE -> desenharToque(efeito.paleta, alvo, p, raioHex)
        ArquetipoVfx.AURA -> desenharAura(efeito.paleta, alvo, p, raioHex)
        ArquetipoVfx.MENTAL -> desenharMental(efeito.paleta, alvo, p, raioHex)
    }
}

// ── PROJÉTIL: um núcleo brilhante viaja de origem→alvo, com rastro de faíscas ──────────────────────
private fun DrawScope.desenharProjetil(paleta: PaletaVfx, origem: Offset, alvo: Offset, p: Float, r: Float) {
    val viagem = (p / 0.8f).coerceIn(0f, 1f)   // 80% do tempo viajando, 20% no impacto
    val pos = Offset(origem.x + (alvo.x - origem.x) * viagem, origem.y + (alvo.y - origem.y) * viagem)
    // Rastro: pontos atrás do núcleo, esmaecendo.
    val n = 8
    for (i in 1..n) {
        val t = (viagem - i * 0.04f).coerceIn(0f, 1f)
        val rp = Offset(origem.x + (alvo.x - origem.x) * t, origem.y + (alvo.y - origem.y) * t)
        drawCircle(paleta.brilho.copy(alpha = 0.35f * (1f - i / n.toFloat())), r * 0.22f, rp)
    }
    // Núcleo + halo.
    drawCircle(paleta.brilho.copy(alpha = 0.5f), r * 0.5f, pos)
    drawCircle(paleta.nucleo, r * 0.28f, pos)
    // Impacto: pequeno estouro no fim.
    if (p > 0.8f) desenharExplosao(paleta, alvo, (p - 0.8f) / 0.2f, r * 0.8f)
}

// ── EXPLOSÃO: anel de choque + estilhaços radiais ────────────────────────────────────────────────
private fun DrawScope.desenharExplosao(paleta: PaletaVfx, centro: Offset, p: Float, r: Float) {
    val raio = r * (0.3f + p * 1.4f)
    val alpha = (1f - p).coerceIn(0f, 1f)
    drawCircle(paleta.brilho.copy(alpha = alpha * 0.4f), raio, centro)
    drawCircle(paleta.nucleo.copy(alpha = alpha * 0.7f), raio * 0.55f, centro)
    val rnd = Random(centro.hashCode())
    val faiscas = 12
    for (i in 0 until faiscas) {
        val ang = (i / faiscas.toFloat()) * 6.2832f + rnd.nextFloat()
        val d = raio * (0.7f + rnd.nextFloat() * 0.4f)
        val fp = Offset(centro.x + cos(ang) * d, centro.y + sin(ang) * d)
        drawCircle(paleta.nucleo.copy(alpha = alpha), r * 0.1f, fp)
    }
}

// ── FLASH: clarão que preenche e some (Cegar, Lampejo) ───────────────────────────────────────────
private fun DrawScope.desenharFlash(paleta: PaletaVfx, centro: Offset, p: Float, r: Float) {
    val alpha = (1f - p) * 0.9f
    drawCircle(paleta.nucleo.copy(alpha = alpha), r * (1f + p * 3f), centro)
}

// ── TOQUE: descarga curta em torno do alvo (a mão do conjurador o toca) ──────────────────────────
private fun DrawScope.desenharToque(paleta: PaletaVfx, centro: Offset, p: Float, r: Float) {
    val alpha = (1f - p).coerceIn(0f, 1f)
    val rnd = Random(centro.hashCode() xor 0x5A5A)
    for (i in 0 until 6) {
        val ang = (i / 6f) * 6.2832f + p * 3f
        val d = r * (0.4f + rnd.nextFloat() * 0.3f)
        val fp = Offset(centro.x + cos(ang) * d, centro.y + sin(ang) * d)
        drawCircle(paleta.brilho.copy(alpha = alpha), r * 0.12f, fp)
    }
    drawCircle(paleta.nucleo.copy(alpha = alpha * 0.6f), r * 0.5f, centro)
}

// ── AURA: anel pulsante em volta do alvo (Escudo, Armadura, Força) — buff, dura mais ──────────────
private fun DrawScope.desenharAura(paleta: PaletaVfx, centro: Offset, p: Float, r: Float) {
    // Pulso senoidal: sobe e desce em vez de simplesmente sumir (buff é contínuo).
    val pulso = 0.5f + 0.5f * sin(p * 6.2832f)
    drawCircle(paleta.brilho.copy(alpha = 0.15f + 0.2f * pulso), r * (1.1f + 0.15f * pulso), centro)
    // Partículas orbitando.
    val orb = 6
    for (i in 0 until orb) {
        val ang = (i / orb.toFloat()) * 6.2832f + p * 6.2832f
        val fp = Offset(centro.x + cos(ang) * r * 1.05f, centro.y + sin(ang) * r * 1.05f)
        drawCircle(paleta.nucleo.copy(alpha = 0.6f), r * 0.09f, fp)
    }
}

// ── MENTAL: partículas suaves subindo + escurecimento leve (Sono, Medo, Atordoar) ────────────────
private fun DrawScope.desenharMental(paleta: PaletaVfx, centro: Offset, p: Float, r: Float) {
    val alpha = (1f - p).coerceIn(0f, 1f)
    val rnd = Random(centro.hashCode() xor 0x3C3C)
    for (i in 0 until 7) {
        val fase = (p + i / 7f) % 1f
        val fx = centro.x + (rnd.nextFloat() - 0.5f) * r * 1.2f
        val fy = centro.y + r * 0.6f - fase * r * 1.4f   // sobem
        drawCircle(paleta.nucleo.copy(alpha = alpha * (1f - fase)), r * 0.1f, Offset(fx, fy))
    }
}

/**
 * Progresso 0→1 animado uma vez quando [chaveEfeito] muda (cada mágica lançada é uma chave nova).
 * O chamador desenha [desenharEfeitoVfx] com este valor; ao chegar em 1 o efeito terminou.
 */
@Composable
fun animarProgressoEfeito(chaveEfeito: Any?, duracaoMs: Int): Float {
    // O alvo é 1f assim que a chave "estreia"; o remember por chave reinicia a animação a cada mágica.
    val alvo = remember(chaveEfeito) { 1f }
    val progresso by animateFloatAsState(
        targetValue = alvo,
        animationSpec = tween(durationMillis = duracaoMs, easing = LinearEasing),
        label = "vfxProgresso",
    )
    return progresso
}
