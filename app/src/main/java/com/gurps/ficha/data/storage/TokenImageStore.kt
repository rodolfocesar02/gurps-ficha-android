package com.gurps.ficha.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.text.Normalizer
import java.util.concurrent.ConcurrentHashMap
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

        salvarPngAtomico(cacheFile, token)
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

    /**
     * Grava o bitmap num arquivo TEMPORÁRIO e renomeia por cima do destino — rename no mesmo
     * diretório é atômico, então um cancelamento de corrotina no meio da escrita nunca deixa um
     * arquivo truncado sendo servido como cache (o pior caso é o .tmp órfão, sobrescrito depois).
     * `internal` para o [CenarioImageStore] (TOK-3) reusar com JPEG.
     */
    internal fun salvarBitmapAtomico(
        destino: File,
        bitmap: Bitmap,
        formato: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        qualidade: Int = 100,
    ) {
        val tmp = File(destino.parentFile, destino.name + ".tmp")
        val ok = runCatching {
            tmp.outputStream().use { out -> bitmap.compress(formato, qualidade, out) }
        }.isSuccess
        if (ok) runCatching { tmp.renameTo(destino) } else runCatching { tmp.delete() }
    }

    private fun salvarPngAtomico(destino: File, bitmap: Bitmap) =
        salvarBitmapAtomico(destino, bitmap, Bitmap.CompressFormat.PNG, 100)

    /** Limpa o cache de tokens (chamar quando o retrato do personagem muda, se desejado). */
    fun limparCache(context: Context) {
        runCatching {
            File(context.filesDir, DIR).listFiles()?.forEach { it.delete() }
            File(context.filesDir, DIR_INIMIGOS).listFiles()?.forEach { it.delete() }
        }
    }

    // ── Lote TOK-2: tokens de INIMIGOS gerados por gatilho (agente secundário) ──────────────

    private const val DIR_INIMIGOS = "tokens/inimigos"

    /** Um Mutex por tipo — 3 goblins ao mesmo tempo = 1 única geração (as outras esperam o cache). */
    private val geracoesEmVoo = ConcurrentHashMap<String, Mutex>()

    /**
     * Normaliza o TIPO do inimigo para virar chave de cache: minúsculas, sem acento, espaços e
     * qualquer caractere fora de a-z/0-9 viram '_' (colapsados). PURO — testável sem Android.
     * Ex.: "Orc Bruto" → "orc_bruto"; "Goblin 2" → "goblin_2"; "Dragão!" → "dragao".
     */
    fun normalizarTipo(tipo: String): String {
        val semAcento = Normalizer.normalize(tipo.trim().lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return semAcento.replace(Regex("[^a-z0-9]+"), "_").trim('_')
    }

    /**
     * Prompt do token do inimigo — busto frontal, fundo neutro, sem texto. PURO — testável.
     * [descricao] opcional refina (vem do bestiário quando existir).
     */
    fun promptTokenInimigo(nome: String, descricao: String? = null): String {
        val desc = descricao?.takeIf { it.isNotBlank() } ?: "criatura hostil de fantasia medieval"
        return """Fantasy RPG enemy token portrait.
Creature: $nome
Description: $desc

Style: detailed fantasy illustration, dramatic lighting, painterly style.
Composition: head and shoulders bust, front view, centered, neutral dark background.
Do NOT include any text, watermarks, logos, borders, or UI elements."""
    }

    /**
     * Devolve o bitmap do token do inimigo do [tipo] (quadrado [LADO_TOKEN]px), do cache quando
     * possível. No cache miss, chama [gerarImagem] (injetado — o caller pluga o GeminiImageService)
     * com o prompt do busto, recorta quadrado centrado no rosto (mesmo pipeline do herói) e cacheia
     * em filesDir/tokens/inimigos/{tipoNormalizado}.png — POR TIPO, não por instância: 3 goblins
     * custam 1 imagem.
     *
     * Dedup de corrida: um Mutex por tipo garante que chamadas simultâneas do mesmo tipo façam
     * UMA geração (as demais acham o cache ao entrar no lock). Qualquer falha devolve null e o
     * canvas fica no fallback círculo+inicial — o combate NUNCA depende da imagem.
     */
    suspend fun obterTokenInimigo(
        context: Context,
        tipo: String,
        nomeVisivel: String = tipo,
        descricao: String? = null,
        gerarImagem: suspend (prompt: String) -> ByteArray?,
    ): Bitmap? = withContext(Dispatchers.IO) {
        val chave = normalizarTipo(tipo)
        if (chave.isBlank()) return@withContext null
        val dir = File(context.filesDir, DIR_INIMIGOS).apply { mkdirs() }
        val cacheFile = File(dir, "$chave.png")

        // Cache hit rápido (sem lock).
        if (cacheFile.exists()) {
            val cached = runCatching { BitmapFactory.decodeFile(cacheFile.absolutePath) }.getOrNull()
            if (cached != null) return@withContext cached
        }

        val mutex = geracoesEmVoo.computeIfAbsent(chave) { Mutex() }
        mutex.withLock {
            // Re-checa dentro do lock — outra corrotina pode ter acabado de gerar.
            if (cacheFile.exists()) {
                val cached = runCatching { BitmapFactory.decodeFile(cacheFile.absolutePath) }.getOrNull()
                if (cached != null) return@withLock cached
            }
            val bytes = runCatching { gerarImagem(promptTokenInimigo(nomeVisivel, descricao)) }.getOrNull()
                ?: return@withLock null
            val original = runCatching { BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }
                .getOrNull() ?: return@withLock null

            val rosto = detectarRosto(original)
            val recorte = calcularRecorteQuadrado(
                original.width, original.height,
                rosto?.left, rosto?.top, rosto?.right, rosto?.bottom
            )
            val quadrado = runCatching {
                Bitmap.createBitmap(original, recorte.left, recorte.top, recorte.lado, recorte.lado)
            }.getOrNull() ?: run { original.recycle(); return@withLock null }
            val token = if (quadrado.width != LADO_TOKEN) {
                Bitmap.createScaledBitmap(quadrado, LADO_TOKEN, LADO_TOKEN, true)
            } else quadrado

            runCatching {
                cacheFile.outputStream().use { out -> token.compress(Bitmap.CompressFormat.PNG, 100, out) }
            }
            if (quadrado != original) original.recycle()
            if (token != quadrado) quadrado.recycle()
            token
        }
    }

    /** Cache hit síncrono (sem geração) — usado pelo canvas pra checar o que já existe em disco. */
    suspend fun tokenInimigoCacheado(context: Context, tipo: String): Bitmap? = withContext(Dispatchers.IO) {
        val chave = normalizarTipo(tipo)
        if (chave.isBlank()) return@withContext null
        val cacheFile = File(File(context.filesDir, DIR_INIMIGOS), "$chave.png")
        if (!cacheFile.exists()) return@withContext null
        runCatching { BitmapFactory.decodeFile(cacheFile.absolutePath) }.getOrNull()
    }

    /** Delega ao [RostoDetector] — mesma cascata usada no cabeçalho da ficha. */
    private fun detectarRosto(bmp: Bitmap): android.graphics.Rect? =
        runCatching { RostoDetector.detectarRosto(bmp) }.getOrNull()
}
