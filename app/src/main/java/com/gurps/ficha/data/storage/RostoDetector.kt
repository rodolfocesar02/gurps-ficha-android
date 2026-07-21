package com.gurps.ficha.data.storage

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Localiza o rosto (ou, na falta dele, o assunto) para enquadrar retratos.
 * Compartilhado pelo [ImagemPersonagemStore] (cabeçalho da ficha) e pelo
 * [TokenImageStore] (tokens do VTT) — antes cada um tinha a sua cópia.
 *
 * Por que uma CASCATA e não uma chamada só: o ML Kit Face Detection é treinado
 * em FOTOGRAFIA. Arte de RPG — pintura digital, 3/4 de perfil, barba/capuz/elmo,
 * criatura não-humana — escapa do modelo com frequência. Cada degrau abaixo é
 * uma tentativa mais cara que a anterior; a primeira que achar rosto ganha.
 *
 * Quando nenhuma acha, entra [estimarAssuntoPorSaliencia]: heurística PURA (sem
 * ML, sem risco de crash) que acha o topo do assunto pela densidade de detalhe.
 * Isso importa para acessibilidade: quem usa TalkBack não tem como arrastar a
 * foto para corrigir o enquadramento, então o automático precisa acertar sozinho.
 *
 * Diagnóstico no aparelho: filtrar o Logcat por `Saga_Retrato`.
 */
object RostoDetector {

    const val TAG = "Saga_Retrato"

    /** Fração mínima da largura que um rosto precisa ter. O default do ML Kit é
     *  0.1 — alto demais para arte de corpo inteiro, onde a cabeça é pequena. */
    private const val MIN_ROSTO = 0.05f

    /** Fatia de cima usada na 3ª tentativa (a cabeça quase sempre está aí). */
    private const val FATIA_TOPO = 0.55f

    /** Teto de largura da ampliação 2x, para não estourar memória. */
    private const val MAX_LARGURA_AMPLIADA = 3000

    /** Largura da miniatura usada na heurística de saliência. */
    private const val LARGURA_AMOSTRA = 96

    /** Percentil de energia tratado como "fundo" da imagem. */
    private const val PERCENTIL_FUNDO = 0.15f

    /** Quanto a energia precisa subir acima do fundo para valer como assunto. */
    private const val MARGEM_ASSUNTO = 0.30f

    /** Se o topo estimado cair abaixo disso, a estimativa é descartada. */
    private const val TOPO_MAXIMO_ACEITAVEL = 0.60f

    // --- rosto ---

    /** Bounding box do maior rosto da imagem, ou null se nenhuma tentativa achar. */
    fun detectarRosto(bmp: Bitmap): Rect? {
        detectarCom(bmp, FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)?.let {
            Log.d(TAG, "rosto por ACCURATE: $it")
            return it
        }
        // Modo rápido é OUTRA rede, não uma versão pior da mesma — às vezes
        // acerta justamente onde a precisa erra.
        detectarCom(bmp, FaceDetectorOptions.PERFORMANCE_MODE_FAST)?.let {
            Log.d(TAG, "rosto por FAST: $it")
            return it
        }
        // Metade de cima ampliada 2x: mais pixels no rosto ajudam o detector
        // quando a arte é de corpo inteiro e a cabeça sai pequena e suave.
        detectarNoTopoAmpliado(bmp)?.let {
            Log.d(TAG, "rosto por TOPO_2X: $it")
            return it
        }
        Log.d(TAG, "nenhum rosto detectado em ${bmp.width}x${bmp.height}")
        return null
    }

    private fun detectarCom(bmp: Bitmap, modo: Int): Rect? {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(modo)
            .setMinFaceSize(MIN_ROSTO)
            .build()
        val detector = FaceDetection.getClient(options)
        return try {
            val faces = Tasks.await(detector.process(InputImage.fromBitmap(bmp, 0)))
            faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }?.boundingBox
        } catch (_: Exception) {
            null
        } finally {
            runCatching { detector.close() }
        }
    }

    private fun detectarNoTopoAmpliado(bmp: Bitmap): Rect? {
        val alturaTopo = (bmp.height * FATIA_TOPO).roundToInt()
        if (alturaTopo < 32 || alturaTopo >= bmp.height) return null
        if (bmp.width * 2 > MAX_LARGURA_AMPLIADA) return null

        var topo: Bitmap? = null
        var ampliado: Bitmap? = null
        return try {
            topo = Bitmap.createBitmap(bmp, 0, 0, bmp.width, alturaTopo)
            ampliado = Bitmap.createScaledBitmap(topo, bmp.width * 2, alturaTopo * 2, true)
            detectarCom(ampliado, FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)?.let {
                // Volta para as coordenadas da imagem original.
                Rect(it.left / 2, it.top / 2, it.right / 2, it.bottom / 2)
            }
        } catch (_: Throwable) {
            null
        } finally {
            if (ampliado !== bmp && ampliado !== topo) ampliado?.recycle()
            if (topo !== bmp) topo?.recycle()
        }
    }

    // --- assunto (fallback sem rosto) ---

    /**
     * Estima onde o assunto começa pela DENSIDADE DE DETALHE, quando nenhum
     * rosto foi encontrado. Reduz a imagem a [LARGURA_AMOSTRA] px de largura,
     * soma o gradiente de cada linha e acha a primeira que sobe claramente
     * acima do nível do fundo — em arte de personagem (fundo liso, assunto
     * detalhado) essa linha é o topo da cabeça/silhueta.
     *
     * O Rect devolvido tem TOPO e CENTRO X confiáveis; a base é só o fim da
     * imagem, porque não estimamos a altura do assunto.
     */
    fun estimarAssuntoPorSaliencia(bmp: Bitmap): Rect? {
        if (bmp.width < LARGURA_AMOSTRA || bmp.height < 32) return null
        val escala = LARGURA_AMOSTRA.toFloat() / bmp.width
        val altura = (bmp.height * escala).roundToInt().coerceAtLeast(16)

        val pixels = IntArray(LARGURA_AMOSTRA * altura)
        val miniatura = runCatching {
            Bitmap.createScaledBitmap(bmp, LARGURA_AMOSTRA, altura, true)
        }.getOrNull() ?: return null
        val lido = runCatching {
            miniatura.getPixels(pixels, 0, LARGURA_AMOSTRA, 0, 0, LARGURA_AMOSTRA, altura)
        }.isSuccess
        if (miniatura !== bmp) miniatura.recycle()
        if (!lido) return null

        // Energia por linha (a última fica de fora: o gradiente vertical precisa
        // da linha seguinte).
        val energiaLinha = FloatArray(altura - 1)
        for (y in 0 until altura - 1) {
            var soma = 0f
            for (x in 0 until LARGURA_AMOSTRA - 1) {
                val c = luma(pixels[y * LARGURA_AMOSTRA + x])
                soma += abs(c - luma(pixels[y * LARGURA_AMOSTRA + x + 1])) +
                    abs(c - luma(pixels[(y + 1) * LARGURA_AMOSTRA + x]))
            }
            energiaLinha[y] = soma
        }

        val linhaTopo = primeiraLinhaDoAssunto(energiaLinha) ?: return null
        // Assunto começando na metade de baixo não é cabeça — provavelmente a
        // imagem é um close que já preenche o quadro. Melhor não chutar.
        if (linhaTopo > energiaLinha.size * TOPO_MAXIMO_ACEITAVEL) {
            Log.d(TAG, "saliencia descartada: topo cairia em $linhaTopo/${energiaLinha.size}")
            return null
        }

        val centroX = centroDaCabeca(pixels, altura, linhaTopo)
        val topoReal = (linhaTopo / escala).roundToInt().coerceIn(0, bmp.height - 1)
        val centroReal = (centroX / escala).roundToInt().coerceIn(0, bmp.width - 1)
        Log.d(TAG, "assunto por saliencia: topo=$topoReal centroX=$centroReal")

        val meia = (bmp.width * 0.15f).roundToInt().coerceAtLeast(1)
        return Rect(
            (centroReal - meia).coerceAtLeast(0),
            topoReal,
            (centroReal + meia).coerceAtMost(bmp.width),
            bmp.height
        )
    }

    /**
     * Primeira linha em que o assunto aparece. A energia precisa passar do nível
     * do fundo por uma margem E se sustentar na linha seguinte — assim um
     * respingo de textura do fundo não é confundido com a cabeça.
     *
     * Kotlin puro (sem Android) de propósito: é a única parte com risco de erro
     * de lógica, e assim fica coberta por teste unitário.
     */
    fun primeiraLinhaDoAssunto(energia: FloatArray): Int? {
        val n = energia.size
        if (n < 8) return null
        val ordenado = energia.sortedArray()
        val fundo = ordenado[(n * PERCENTIL_FUNDO).roundToInt().coerceIn(0, n - 1)]
        val maximo = ordenado[n - 1]
        if (maximo <= fundo) return null
        val limiar = fundo + (maximo - fundo) * MARGEM_ASSUNTO
        for (y in 0 until n - 1) {
            if (energia[y] > limiar && energia[y + 1] > limiar) return y
        }
        return null
    }

    /**
     * Centro horizontal da cabeça: centroide de energia das linhas logo abaixo
     * do topo do assunto. Restringir à faixa da cabeça evita que um corpo
     * deslocado (braço estendido, capa) puxe o enquadramento para o lado.
     */
    private fun centroDaCabeca(pixels: IntArray, altura: Int, linhaTopo: Int): Float {
        val fim = (linhaTopo + altura * 0.20f).roundToInt().coerceAtMost(altura - 2)
        val energiaColuna = FloatArray(LARGURA_AMOSTRA)
        for (y in linhaTopo until fim) {
            for (x in 0 until LARGURA_AMOSTRA - 1) {
                val c = luma(pixels[y * LARGURA_AMOSTRA + x])
                energiaColuna[x] += abs(c - luma(pixels[y * LARGURA_AMOSTRA + x + 1])) +
                    abs(c - luma(pixels[(y + 1) * LARGURA_AMOSTRA + x]))
            }
        }
        var soma = 0f
        var pesado = 0f
        for (x in energiaColuna.indices) {
            soma += energiaColuna[x]
            pesado += energiaColuna[x] * x
        }
        return if (soma <= 0f) LARGURA_AMOSTRA / 2f else pesado / soma
    }

    private fun luma(pixel: Int): Float {
        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8) and 0xFF
        val b = pixel and 0xFF
        return 0.299f * r + 0.587f * g + 0.114f * b
    }
}
