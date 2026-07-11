package com.gurps.ficha.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max
import kotlin.math.min

/**
 * Lote TOK-1 (VTT 2D): gera e cacheia o TOKEN do combate tático a partir do retrato do personagem.
 *
 * O token é um recorte QUADRADO 1:1 do retrato, centrado no ROSTO quando o ML Kit acha um
 * (mesma detecção usada pelo [ImagemPersonagemStore] no cabeçalho). A máscara CIRCULAR em si é
 * aplicada na hora de desenhar (clip no Canvas do Compose) — o arquivo cacheado fica quadrado,
 * o que simplifica o recorte e permite reusar a mesma imagem em outros formatos no futuro.
 *
 * Cache: `filesDir/tokens/heroi_<hash>.png` onde o hash deriva do caminho do retrato original —
 * trocar o retrato da ficha regenera o token na primeira abertura seguinte do modo tático.
 * Falhas (sem retrato, arquivo sumiu, decode falhou) devolvem null e o canvas usa o fallback
 * (círculo colorido + inicial) — o combate NUNCA depende da imagem existir.
 */
object TokenImageStore {

    private const val DIR = "tokens"
    private const val LADO_TOKEN = 256 // px — token é pequeno na tela; 256 é nítido e leve

    /**
     * Recorte quadrado calculado em coordenadas da imagem original.
     * Extraído como dado puro para a matemática ser testável sem Android.
     */
    data class RecorteQuadrado(val left: Int, val top: Int, val lado: Int)

    /**
     * Matemática PURA do recorte 1:1.
     *  - Sem rosto: quadrado central com o maior lado possível (min de largura/altura), centrado.
     *  - Com rosto (bounding box rostoLeft..rostoRight por rostoTop..rostoBottom): quadrado com
     *    lado = 2.2 vezes o maior lado do rosto (rosto + moldura de cabelo/ombros), centrado no
     *    CENTRO do rosto, clampado aos limites da imagem e limitado ao menor lado dela.
     */
    fun calcularRecorteQuadrado(
        larguraImg: Int,
        alturaImg: Int,
        rostoLeft: Int? = null,
        rostoTop: Int? = null,
        rostoRight: Int? = null,
        rostoBottom: Int? = null,
    ): RecorteQuadrado {
        val ladoMax = min(larguraImg, alturaImg)
        if (rostoLeft == null || rostoTop == null || rostoRight == null || rostoBottom == null) {
            // Quadrado central.
            val left = (larguraImg - ladoMax) / 2
            val top = (alturaImg - ladoMax) / 2
            return RecorteQuadrado(left, top, ladoMax)
        }
        val rostoW = rostoRight - rostoLeft
        val rostoH = rostoBottom - rostoTop
        val lado = (max(rostoW, rostoH) * 2.2f).toInt().coerceIn(1, ladoMax)
        val centroX = (rostoLeft + rostoRight) / 2
        val centroY = (rostoTop + rostoBottom) / 2
        val left = (centroX - lado / 2).coerceIn(0, larguraImg - lado)
        val top = (centroY - lado / 2).coerceIn(0, alturaImg - lado)
        return RecorteQuadrado(left, top, lado)
    }

    /**
     * Devolve o bitmap do token do herói (quadrado [LADO_TOKEN]px), do cache quando possível.
     * [retratoUri] é o `Personagem.imagemPersonagemOriginalUri` (file:// em filesDir/portraits/).
     * Null se não há retrato ou qualquer etapa falhar (caller usa fallback).
     */
    suspend fun obterTokenHeroi(context: Context, retratoUri: String): Bitmap? = withContext(Dispatchers.IO) {
        if (retratoUri.isBlank()) return@withContext null
        val dir = File(context.filesDir, DIR).apply { mkdirs() }
        val cacheFile = File(dir, "heroi_${retratoUri.hashCode()}.png")

        // Cache hit.
        if (cacheFile.exists()) {
            val cached = runCatching { BitmapFactory.decodeFile(cacheFile.absolutePath) }.getOrNull()
            if (cached != null) return@withContext cached
        }

        // Miss: decodifica o retrato, acha o rosto, recorta quadrado, salva.
        val arquivoRetrato = runCatching { File(Uri.parse(retratoUri).path ?: return@withContext null) }
            .getOrNull() ?: return@withContext null
        if (!arquivoRetrato.exists()) return@withContext null
        val original = runCatching { BitmapFactory.decodeFile(arquivoRetrato.absolutePath) }
            .getOrNull() ?: return@withContext null

        val rosto = detectarRosto(original)
        val recorte = calcularRecorteQuadrado(
            original.width, original.height,
            rosto?.left, rosto?.top, rosto?.right, rosto?.bottom
        )
        val quadrado = runCatching {
            Bitmap.createBitmap(original, recorte.left, recorte.top, recorte.lado, recorte.lado)
        }.getOrNull() ?: run { original.recycle(); return@withContext null }
        val token = if (quadrado.width != LADO_TOKEN) {
            Bitmap.createScaledBitmap(quadrado, LADO_TOKEN, LADO_TOKEN, true)
        } else quadrado

        runCatching {
            cacheFile.outputStream().use { out -> token.compress(Bitmap.CompressFormat.PNG, 100, out) }
        }
        // Tokens de retratos ANTIGOS viram lixo órfão quando o retrato muda (o URI novo tem UUID
        // novo) — remove os irmãos "heroi_*" que não são o cache atual.
        runCatching {
            dir.listFiles()?.forEach { f ->
                if (f.name.startsWith("heroi_") && f.name != cacheFile.name) f.delete()
            }
        }
        if (quadrado != original) original.recycle()
        if (token != quadrado) quadrado.recycle()
        token
    }

    /** Limpa o cache de tokens (chamar quando o retrato do personagem muda, se desejado). */
    fun limparCache(context: Context) {
        runCatching {
            File(context.filesDir, DIR).listFiles()?.forEach { it.delete() }
        }
    }

    /** Mesmo detector do ImagemPersonagemStore — maior rosto da imagem, ou null. */
    private fun detectarRosto(bmp: Bitmap): android.graphics.Rect? {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .build()
        val detector = FaceDetection.getClient(options)
        return try {
            val input = InputImage.fromBitmap(bmp, 0)
            val faces = Tasks.await(detector.process(input))
            faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }?.boundingBox
        } catch (_: Exception) {
            null
        } finally {
            detector.close()
        }
    }
}
