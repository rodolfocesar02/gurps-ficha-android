package com.gurps.ficha.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Lote TOK-3 (VTT 2D): gera e cacheia a IMAGEM DE FUNDO da cena para o combate tático.
 *
 * Quando o Narrador estabelece a cena (tool `definir_cena` — título/bioma/humor), um gatilho
 * assíncrono gera uma vista TOP-DOWN do chão do lugar (Gemini) e cacheia por CENA em
 * `filesDir/cenarios/c{campanhaId}_s{cenaId}.jpg`. O canvas desenha o fundo sob a grade com um
 * scrim escuro por cima (legibilidade). O fundo cinza permanece o fallback eterno — falha de
 * geração nunca afeta o combate.
 *
 * Mesmos princípios do [TokenImageStore] (TOK-2): Mutex por chave deduplica gerações concorrentes
 * (gatilho do delegate × canvas on-demand), escrita atômica (temp + rename), lambda de geração
 * injetada (desacoplado do Gemini). JPEG qualidade 85 — fundo é grande, PNG seria pesado à toa.
 */
object CenarioImageStore {

    private const val DIR = "cenarios"
    private const val MAIOR_LADO = 1024 // px — fundo de canvas; mais que isso é desperdício

    private val geracoesEmVoo = ConcurrentHashMap<String, Mutex>()

    /**
     * Chave do cache: campanha + cena + HASH do conteúdo FÍSICO (título|bioma). O hash importa
     * porque `definir_cena` ATUALIZA a mesma cena (mesmo ID) — a cena nasce "Início" e vira
     * "O Coliseu de Ferro"; sem o hash, um fundo genérico gerado cedo ficaria grudado pra sempre.
     * Quando o conteúdo muda, a chave muda → regenera; os irmãos velhos da MESMA cena são apagados.
     *
     * HUMOR fica FORA da chave (achado da revisão adversarial do TOK-3): é o campo mais volátil
     * ("tenso" → "desesperador" → "alívio" na MESMA locação física) e não muda o terreno — se
     * entrasse no hash, cada retoque de clima do Narrador regeneraria o fundo pago (~$0.067) e
     * apagaria o anterior. O humor ainda entra no PROMPT na primeira geração daquela chave.
     */
    fun chaveCena(campanhaId: Long, cenaId: Long, titulo: String, bioma: String?): String {
        val hash = "$titulo|${bioma.orEmpty()}".hashCode()
        return "c${campanhaId}_s${cenaId}_h$hash"
    }

    /** Prefixo comum de todas as versões de uma cena — usado pra apagar fundos obsoletos dela. */
    private fun prefixoCena(campanhaId: Long, cenaId: Long): String = "c${campanhaId}_s${cenaId}_"

    /**
     * Cena que ainda não foi estabelecida pelo Narrador não gera fundo — "Início" é o título
     * placeholder criado junto com a campanha, antes do primeiro `definir_cena`.
     */
    fun cenaValidaParaFundo(titulo: String): Boolean =
        titulo.isNotBlank() && !titulo.equals("Início", ignoreCase = true) &&
            !titulo.equals("Inicio", ignoreCase = true)

    /**
     * Prompt do fundo top-down — PURO (testável). Vista aérea do CHÃO, sem criaturas, sem grid,
     * sem texto: a grade de hexes e os tokens são desenhados POR CIMA pelo canvas.
     */
    fun promptFundoCena(titulo: String, bioma: String? = null, humor: String? = null): String {
        val partes = mutableListOf<String>()
        if (!bioma.isNullOrBlank()) partes.add("Biome/terrain: $bioma")
        if (!humor.isNullOrBlank()) partes.add("Mood: $humor")
        val extras = if (partes.isEmpty()) "" else "\n" + partes.joinToString("\n")
        return """Top-down battle map background for a tabletop RPG.
Location: ${titulo.ifBlank { "generic fantasy location" }}$extras

Style: aerial view looking straight down at the GROUND of the location, painterly fantasy
illustration, muted colors, consistent lighting.
Composition: only terrain/floor textures and environmental details (stones, grass, sand,
wood, cracks, props). NO creatures, NO people, NO grid lines, NO text, NO watermarks,
NO UI elements, NO borders."""
    }

    /**
     * Devolve o fundo da cena (bitmap), do cache quando possível. No miss chama [gerarImagem]
     * (injetado) com o prompt top-down, redimensiona para maior lado ≤ [MAIOR_LADO] e cacheia.
     * Null em qualquer falha (caller fica no fundo cinza).
     */
    suspend fun obterFundoCena(
        context: Context,
        campanhaId: Long,
        cenaId: Long,
        titulo: String,
        bioma: String? = null,
        humor: String? = null,
        gerarImagem: suspend (prompt: String) -> ByteArray?,
    ): Bitmap? = withContext(Dispatchers.IO) {
        if (!cenaValidaParaFundo(titulo)) return@withContext null
        val chave = chaveCena(campanhaId, cenaId, titulo, bioma)
        val dir = File(context.filesDir, DIR).apply { mkdirs() }
        val cacheFile = File(dir, "$chave.jpg")

        if (cacheFile.exists()) {
            val cached = runCatching { BitmapFactory.decodeFile(cacheFile.absolutePath) }.getOrNull()
            if (cached != null) return@withContext cached
        }

        val mutex = geracoesEmVoo.computeIfAbsent(chave) { Mutex() }
        mutex.withLock {
            if (cacheFile.exists()) {
                val cached = runCatching { BitmapFactory.decodeFile(cacheFile.absolutePath) }.getOrNull()
                if (cached != null) return@withLock cached
            }
            val bytes = runCatching { gerarImagem(promptFundoCena(titulo, bioma, humor)) }.getOrNull()
                ?: return@withLock null
            val original = runCatching { BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }
                .getOrNull() ?: return@withLock null

            val maior = max(original.width, original.height)
            val fundo = if (maior > MAIOR_LADO) {
                val escala = MAIOR_LADO.toFloat() / maior
                Bitmap.createScaledBitmap(
                    original,
                    (original.width * escala).roundToInt().coerceAtLeast(1),
                    (original.height * escala).roundToInt().coerceAtLeast(1),
                    true
                )
            } else original

            TokenImageStore.salvarBitmapAtomico(cacheFile, fundo, Bitmap.CompressFormat.JPEG, 85)
            // Fundos de VERSÕES ANTERIORES desta cena (definir_cena mudou título/bioma) viram lixo.
            runCatching {
                val prefixo = prefixoCena(campanhaId, cenaId)
                dir.listFiles()?.forEach { f ->
                    if (f.name.startsWith(prefixo) && f.name != cacheFile.name && !f.name.endsWith(".tmp")) f.delete()
                }
            }
            if (fundo != original) original.recycle()
            fundo
        }
    }

    /** Checagem BARATA de existência (sem decode) — o gatilho por turno usa pra virar no-op cedo. */
    fun temFundoCena(
        context: Context, campanhaId: Long, cenaId: Long,
        titulo: String, bioma: String? = null,
    ): Boolean {
        val chave = chaveCena(campanhaId, cenaId, titulo, bioma)
        return File(File(context.filesDir, DIR), "$chave.jpg").exists()
    }

    /** Cache hit síncrono (sem geração) — o canvas checa o que já existe. */
    suspend fun fundoCenaCacheado(
        context: Context, campanhaId: Long, cenaId: Long,
        titulo: String, bioma: String? = null,
    ): Bitmap? = withContext(Dispatchers.IO) {
        val chave = chaveCena(campanhaId, cenaId, titulo, bioma)
        val cacheFile = File(File(context.filesDir, DIR), "$chave.jpg")
        if (!cacheFile.exists()) return@withContext null
        runCatching { BitmapFactory.decodeFile(cacheFile.absolutePath) }.getOrNull()
    }

    /** Limpa todos os fundos (ex.: campanha excluída). */
    fun limparCache(context: Context) {
        runCatching { File(context.filesDir, DIR).listFiles()?.forEach { it.delete() } }
    }
}
