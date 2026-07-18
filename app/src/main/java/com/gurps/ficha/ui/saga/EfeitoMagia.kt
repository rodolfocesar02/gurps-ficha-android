package com.gurps.ficha.ui.saga

import androidx.compose.ui.graphics.Color
import com.gurps.ficha.domain.magic.MagiaMecanica

/**
 * ⚠️ PROTÓTIPO NÃO LIGADO (Lote VFX-1 / marcado no LIMPEZA-3).
 *
 * **Este arquivo e o [EfeitoMagiaCanvas] ainda NÃO têm nenhum chamador em produção.** O mapeamento
 * abaixo está correto e coberto por testes (`VfxMapperTest`), mas nada dispara os efeitos no combate:
 * falta a integração com o grid (o motor emitir "magia X saiu do hexágono A para o B", a UI converter
 * para pixels e animar). Decisão consciente do usuário: **mecânica 100% primeiro, arte visual depois.**
 *
 * Se você chegou aqui procurando por que os efeitos não aparecem no jogo: é isto. Não é bug — é lote
 * pendente. Ao ligar, remova este aviso.
 *
 * ── Lote VFX-1 (protótipo): efeitos visuais das mágicas no combate ──
 *
 * A ideia central: NÃO precisamos de 84 efeitos únicos. As mágicas mecânicas caem em poucos
 * ARQUÉTIPOS de movimento (projétil, explosão, toque, flash, aura…), e a cor vem do ELEMENTO. Um
 * único componente parametrizado por (arquétipo, paleta) cobre dezenas de mágicas — Bola de Fogo e
 * Relâmpago compartilham o arquétipo "projétil", mudando só a cor.
 *
 * Tudo é derivado da `mecanica` CURADA (efeito/entrega/tipoDano) e do nome do ELEMENTO — não de
 * heurística frágil. É a mesma disciplina do resto do pilar: a regra estruturada manda.
 */

/** Como o efeito se MOVE na tela. Fechado — mapeia o que o motor já distingue. */
enum class ArquetipoVfx { PROJETIL, EXPLOSAO, TOQUE, FLASH, AURA, MENTAL }

/** A cor do efeito, por elemento. Cada uma traz o par (núcleo brilhante, brilho externo). */
enum class PaletaVfx(val nucleo: Color, val brilho: Color) {
    FOGO(Color(0xFFFFD54F), Color(0xFFFF5722)),
    RAIO(Color(0xFFB3E5FC), Color(0xFF2979FF)),
    GELO(Color(0xFFE1F5FE), Color(0xFF00B0FF)),
    ACIDO(Color(0xFFCCFF90), Color(0xFF64DD17)),
    LUZ(Color(0xFFFFFFFF), Color(0xFFFFF59D)),
    TERRA(Color(0xFFD7CCC8), Color(0xFF6D4C41)),
    SOM(Color(0xFFE1BEE7), Color(0xFF7C4DFF)),
    MENTE(Color(0xFFD1C4E9), Color(0xFF512DA8)),
    ARCANO(Color(0xFFFFE082), Color(0xFFFFA000)); // buff genérico / fallback dourado
}

/** O par (arquétipo, paleta) que descreve o efeito de uma mágica. */
data class EfeitoVfx(val arquetipo: ArquetipoVfx, val paleta: PaletaVfx)

object VfxMapper {

    /** Deriva a paleta do ELEMENTO, lendo nome + notas curadas (o mesmo texto fiel ao livro). */
    fun paletaDe(nome: String, mec: MagiaMecanica?): PaletaVfx {
        val t = (nome + " " + (mec?.notas ?: "")).lowercase()
        return when {
            listOf("relâmp", "relamp", "raio", "eletr", "faísc", "faisc").any { it in t } -> PaletaVfx.RAIO
            listOf("fogo", "chama", "candente", "flamej", "ígne", "igne").any { it in t } -> PaletaVfx.FOGO
            listOf("gelo", "congel", "gelad", "frio", "neve").any { it in t } -> PaletaVfx.GELO
            listOf("ácid", "acid").any { it in t } -> PaletaVfx.ACIDO
            listOf("luz", "solar", "lampej", "cegar", "brilho").any { it in t } -> PaletaVfx.LUZ
            listOf("pedra", "terra", "areia", "lama").any { it in t } -> PaletaVfx.TERRA
            listOf("som", "grito", "estrondo", "sônic", "sonic").any { it in t } -> PaletaVfx.SOM
            listOf("sono", "mente", "medo", "terror", "pânico", "panico", "atordoa").any { it in t } -> PaletaVfx.MENTE
            else -> PaletaVfx.ARCANO
        }
    }

    /** Deriva o arquétipo de MOVIMENTO da mecânica curada (efeito/entrega). */
    fun arquetipoDe(mec: MagiaMecanica?): ArquetipoVfx = when {
        mec == null -> ArquetipoVfx.AURA
        mec.efeito == "buff" -> ArquetipoVfx.AURA
        mec.efeito == "condicao" -> ArquetipoVfx.MENTAL
        mec.efeito == "dano" -> when (mec.entrega) {
            "area" -> ArquetipoVfx.EXPLOSAO
            "toque" -> ArquetipoVfx.TOQUE
            "feixe", "projetil" -> if (mec.condicao == "cego") ArquetipoVfx.FLASH else ArquetipoVfx.PROJETIL
            else -> ArquetipoVfx.PROJETIL
        }
        else -> ArquetipoVfx.AURA
    }

    /** O efeito completo de uma mágica. null quando ela não tem mecânica executável (nada a mostrar). */
    fun efeitoDe(nome: String, mec: MagiaMecanica?): EfeitoVfx =
        EfeitoVfx(arquetipoDe(mec), paletaDe(nome, mec))
}
