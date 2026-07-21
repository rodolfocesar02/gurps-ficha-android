package com.gurps.ficha.data.storage

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import androidx.exifinterface.media.ExifInterface
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Armazena a imagem (retrato) do personagem.
 *
 * Decisão de design (com o usuário):
 *  - Ao escolher uma imagem da galeria, o app NÃO guarda a content:// URI
 *    (ela pode sumir se o arquivo for movido/apagado e não exporta junto
 *    com a ficha). Em vez disso COPIA os bytes para filesDir/portraits/,
 *    redimensionada — padrão já usado pelo app (MetacaracteristicaStore,
 *    filesDir/maps). Sem Room, sem migration.
 *  - O cabeçalho da ficha é uma faixa LARGA. Recortamos nessa proporção
 *    enquadrando o ASSUNTO PRINCIPAL da arte (ML Kit Subject Segmentation,
 *    on-device, offline) — funciona com qualquer arte de RPG, mesmo SEM
 *    rosto humano (criaturas, armaduras, monstros). Quando há rosto (ML Kit
 *    Face Detection), usamos o rosto como refino vertical. Sem nenhum dos
 *    dois → alinha ao topo.
 *  - bytesBase64() devolve um data: URI pronto para subir ao Discord
 *    (mesma estratégia do VTT em resolveTokenImagePayload).
 */
object ImagemPersonagemStore {

    private const val DIR = "portraits"
    /** Subpasta criada dentro de Imagens/ ao salvar o retrato na galeria. */
    private const val PASTA_GALERIA = "GURPS"
    /**
     * A faxina ignora arquivos criados na última hora: entre gravar a imagem e
     * a ficha ser salva existe uma janela, e apagar aí destruiria o retrato que
     * o usuário acabou de escolher.
     */
    private const val CARENCIA_FAXINA_MS = 60L * 60L * 1000L
    private const val LARGURA_ALVO = 1080       // px — largura do retrato recortado (cabeçalho)
    private const val MAIOR_LADO_ORIGINAL = 1600 // px — maior lado da imagem inteira (tela cheia)
    // Faixa do cabeçalho (largura/altura). Tem que bater com a proporção REAL do
    // CabecalhoComImagem (fillMaxWidth x 140.dp) — num celular comum isso dá
    // ~2,8. Quando não batia (era 2.0), o Compose recortava a faixa DE NOVO na
    // exibição e o enquadramento calculado aqui não era o que aparecia na tela.
    private const val PROPORCAO = 2.8f
    // Fração da ALTURA da faixa que o rosto deve ocupar (enquadramento
    // consistente "rosto + ombros" — independe de quão grande o rosto aparece
    // na arte original). 0.32 = rosto ocupa ~32% da faixa: 25% mais "afastado"
    // que o valor antigo (0.42), a pedido do usuário, para caber rosto inteiro
    // com folga em vez de um close cortado.
    private const val ROSTO_FRACAO_ALTURA = 0.32f
    /** Folga acima do topo do assunto, em frações da altura da faixa. */
    private const val MARGEM_TOPO_ASSUNTO = 0.08f
    private const val QUALIDADE_JPEG = 88
    private const val MAX_DECODE = 2048          // limita o bitmap carregado em memória

    /** Resultado do salvamento: caminho da imagem recortada (cabeçalho) e da original (tela cheia). */
    data class ImagensSalvas(val recortadaUri: String, val originalUri: String)

    /**
     * Lê a imagem da [uri] e salva DOIS arquivos em filesDir/portraits/:
     *  - a versão RECORTADA (enquadra o assunto/rosto) para o cabeçalho;
     *  - a imagem INTEIRA (apenas redimensionada) para a tela cheia.
     * Retorna ambos os caminhos file://, ou null em caso de falha.
     */
    suspend fun salvarImagem(context: Context, uri: Uri): ImagensSalvas? = withContext(Dispatchers.IO) {
        val original = decodeBitmap(context, uri) ?: return@withContext null
        val dir = File(context.filesDir, DIR).apply { mkdirs() }

        // 1) Imagem INTEIRA (tela cheia) — só redimensiona, sem recortar.
        val inteira = redimensionarMaiorLado(original, MAIOR_LADO_ORIGINAL)
        val arquivoOriginal = File(dir, "original_${UUID.randomUUID()}.jpg")
        val okOriginal = runCatching {
            arquivoOriginal.outputStream().use { out ->
                inteira.compress(Bitmap.CompressFormat.JPEG, QUALIDADE_JPEG, out)
            }
        }.isSuccess

        // 2) Imagem RECORTADA (cabeçalho) — enquadra assunto/rosto.
        // Subject Segmentation (beta) crashava em alguns aparelhos/emulador
        // (MediaPipeException numa thread interna que escapa do try/catch);
        // por isso ficou OPCIONAL e protegida por detectarAssuntoSeguro, que
        // a desliga permanentemente ao primeiro erro. Rosto é o caminho
        // principal e estável.
        val rostoRect = runCatching { RostoDetector.detectarRosto(original) }.getOrNull()
        // Só vale gastar com o assunto se o rosto falhou — é ele que decide o
        // enquadramento quando existe. Com a segmentação desligada, a saliência
        // é o que impede o recorte cego "colado no topo" que cortava o rosto.
        val assuntoRect = if (rostoRect != null) null else {
            detectarAssuntoSeguro(original)
                ?: runCatching { RostoDetector.estimarAssuntoPorSaliencia(original) }.getOrNull()
        }
        val recortada = recortarFaixa(original, assuntoRect, rostoRect)
        val finalBmp = redimensionar(recortada)
        val arquivoRecorte = File(dir, "retrato_${UUID.randomUUID()}.jpg")
        val okRecorte = runCatching {
            arquivoRecorte.outputStream().use { out ->
                finalBmp.compress(Bitmap.CompressFormat.JPEG, QUALIDADE_JPEG, out)
            }
        }.isSuccess

        // Libera bitmaps intermediários
        if (inteira != original) inteira.recycle()
        if (recortada != original) recortada.recycle()
        if (finalBmp != recortada) finalBmp.recycle()
        original.recycle()

        if (okOriginal && okRecorte) {
            ImagensSalvas(
                recortadaUri = Uri.fromFile(arquivoRecorte).toString(),
                originalUri = Uri.fromFile(arquivoOriginal).toString()
            )
        } else null
    }

    /** Apaga o arquivo de retrato apontado pela [caminhoUri] (file://). */
    fun excluirImagem(caminhoUri: String) {
        if (caminhoUri.isBlank()) return
        runCatching {
            val file = caminhoUri.toUri()?.let { File(it.path ?: return) } ?: return
            if (file.exists() && file.parentFile?.name == DIR) file.delete()
        }
    }

    /**
     * Lê o retrato salvo e devolve um data:image/jpeg;base64,... pronto
     * para enviar ao servidor Discord. Retorna null se não houver imagem.
     */
    suspend fun bytesBase64(caminhoUri: String): String? = withContext(Dispatchers.IO) {
        if (caminhoUri.isBlank()) return@withContext null
        val file = caminhoUri.toUri()?.let { File(it.path ?: return@withContext null) }
            ?: return@withContext null
        if (!file.exists()) return@withContext null
        val bytes = runCatching { file.readBytes() }.getOrNull() ?: return@withContext null
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        "data:image/jpeg;base64,$base64"
    }

    /**
     * Restaura uma imagem vinda EMBUTIDA num arquivo exportado (campo
     * imagemPersonagemBase64 = "data:image/...;base64,..."). Decodifica os
     * bytes, grava num arquivo temporário e roda o mesmo fluxo de
     * [salvarImagem] (recorte por rosto + 2 versões: recortada + inteira).
     * Retorna os dois URIs salvos, ou null em caso de falha.
     */
    suspend fun salvarDeBase64(context: Context, dataUri: String): ImagensSalvas? = withContext(Dispatchers.IO) {
        if (dataUri.isBlank()) return@withContext null
        // Aceita "data:...;base64,XXXX" ou só "XXXX".
        val base64 = dataUri.substringAfter("base64,", dataUri)
        val bytes = runCatching { Base64.decode(base64, Base64.DEFAULT) }.getOrNull()
            ?: return@withContext null
        if (bytes.isEmpty()) return@withContext null

        // Grava num arquivo temporário no cache e reusa o pipeline de salvarImagem.
        val temp = File(context.cacheDir, "import_retrato_${UUID.randomUUID()}.jpg")
        val ok = runCatching { temp.writeBytes(bytes) }.isSuccess
        if (!ok) return@withContext null
        try {
            salvarImagem(context, Uri.fromFile(temp))
        } finally {
            runCatching { temp.delete() }
        }
    }

    /** Como terminou o [salvarNaGaleria] — separado para a UI dar a mensagem certa. */
    enum class ResultadoGaleria { OK, SEM_PERMISSAO, FALHOU }

    /**
     * Só até o Android 9 (API 28) gravar na galeria exige WRITE_EXTERNAL_STORAGE.
     * Do Android 10 em diante o MediaStore cuida disso sem permissão nenhuma.
     */
    fun precisaPermissaoGaleria(): Boolean = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P

    /**
     * Copia o retrato de [caminhoUri] (que vive em filesDir/portraits/, invisível
     * para o usuário) para a GALERIA do aparelho, em Imagens/[PASTA_GALERIA].
     *
     * Existe porque o retrato — principalmente o gerado pelo Mestre Pintor — ficava
     * preso no armazenamento privado do app: não aparecia na galeria, em nenhum
     * gerenciador de arquivos, nem no backup de fotos. A única saída era exportar a
     * ficha inteira.
     */
    suspend fun salvarNaGaleria(
        context: Context,
        caminhoUri: String,
        nomePersonagem: String
    ): ResultadoGaleria = withContext(Dispatchers.IO) {
        if (caminhoUri.isBlank()) return@withContext ResultadoGaleria.FALHOU
        val origem = caminhoUri.toUri()?.path?.let { File(it) }
            ?: return@withContext ResultadoGaleria.FALHOU
        if (!origem.exists()) return@withContext ResultadoGaleria.FALHOU
        val bytes = runCatching { origem.readBytes() }.getOrNull()
            ?: return@withContext ResultadoGaleria.FALHOU

        val valores = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, nomeDeArquivo(nomePersonagem))
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/$PASTA_GALERIA"
                )
                // Segura a foto como "incompleta" até os bytes estarem gravados,
                // para a galeria não exibir uma imagem pela metade.
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val destino = runCatching {
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, valores)
        }.getOrNull()
        if (destino == null) {
            // Antes do Android 10 a falta da permissão aparece exatamente assim.
            return@withContext if (precisaPermissaoGaleria()) {
                ResultadoGaleria.SEM_PERMISSAO
            } else {
                ResultadoGaleria.FALHOU
            }
        }

        val gravou = runCatching {
            resolver.openOutputStream(destino)?.use { it.write(bytes) }
                ?: error("sem stream de escrita")
        }.isSuccess

        if (!gravou) {
            runCatching { resolver.delete(destino, null, null) }
            return@withContext ResultadoGaleria.FALHOU
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching {
                resolver.update(
                    destino,
                    ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) },
                    null,
                    null
                )
            }
        }
        ResultadoGaleria.OK
    }

    /** Quanto a faxina apagou. */
    data class ResultadoFaxina(val apagados: Int, val bytesLiberados: Long)

    /**
     * Apaga retratos ÓRFÃOS de filesDir/portraits/ — arquivos que nenhuma ficha
     * cita mais.
     *
     * Cada gravação cria um par novo (`retrato_<uuid>`/`original_<uuid>`) e a
     * ficha só guarda o caminho do último. Só a troca de foto pela galeria
     * apagava o par anterior; gerar retrato pela IA, importar ficha com imagem
     * embutida e excluir ficha deixavam os arquivos para trás — invisíveis para
     * o usuário, porque a pasta é armazenamento privado do app.
     *
     * Varrer tudo de uma vez resolve os quatro casos e ainda limpa o que já
     * ficou para trás, em vez de tapar cada buraco separadamente.
     *
     * @param jsonsDasFichas JSON cru de TODAS as fichas (inclusive o auto-save).
     *   Passar null quando a leitura falhou — aí a faxina não roda. Uma lista
     *   vazia significa "não há fichas mesmo" e é válida.
     * @param emUso URIs do personagem carregado agora, que pode ainda não ter
     *   sido salvo em ficha nenhuma.
     */
    suspend fun faxinaDeOrfaos(
        context: Context,
        jsonsDasFichas: List<String>?,
        emUso: Collection<String>
    ): ResultadoFaxina = withContext(Dispatchers.IO) {
        // Na dúvida, NÃO apaga: sem a lista de fichas não dá para saber o que
        // está em uso, e um falso positivo aqui apaga o retrato do usuário.
        if (jsonsDasFichas == null) return@withContext ResultadoFaxina(0, 0)

        val dir = File(context.filesDir, DIR)
        val arquivos = dir.takeIf { it.isDirectory }?.listFiles()
            ?: return@withContext ResultadoFaxina(0, 0)

        val nomesEmUso = emUso.mapNotNull { it.toUri()?.lastPathSegment }.toSet()
        val nascidoAntesDe = System.currentTimeMillis() - CARENCIA_FAXINA_MS

        var apagados = 0
        var bytes = 0L
        for (arquivo in arquivos) {
            if (!arquivo.isFile) continue
            if (!ehOrfao(
                    nome = arquivo.name,
                    modificadoEm = arquivo.lastModified(),
                    nascidoAntesDe = nascidoAntesDe,
                    nomesEmUso = nomesEmUso,
                    jsonsDasFichas = jsonsDasFichas
                )
            ) continue

            val tamanho = arquivo.length()
            if (runCatching { arquivo.delete() }.getOrDefault(false)) {
                apagados++
                bytes += tamanho
            }
        }
        if (apagados > 0) {
            android.util.Log.d(
                RostoDetector.TAG,
                "faxina de retratos: $apagados arquivo(s) orfao(s), ${bytes / 1024} KB liberados"
            )
        }
        ResultadoFaxina(apagados, bytes)
    }

    /**
     * Decide se um arquivo da pasta de retratos pode ser apagado. Separado do
     * I/O e SEM dependência de Android porque é a única linha de código do app
     * que destrói dado do usuário sem confirmação — precisa de teste.
     *
     * Toda regra aqui é uma razão para MANTER; só some o que não bate em nenhuma.
     */
    internal fun ehOrfao(
        nome: String,
        modificadoEm: Long,
        nascidoAntesDe: Long,
        nomesEmUso: Set<String>,
        jsonsDasFichas: List<String>
    ): Boolean {
        // Só mexe no que este store cria — nunca em arquivo de terceiros.
        if (!nome.startsWith("retrato_") && !nome.startsWith("original_")) return false
        // Recém-criado pode ainda estar a caminho da ficha.
        if (modificadoEm > nascidoAntesDe) return false
        // Retrato do personagem carregado agora, que talvez nem esteja salvo.
        if (nome in nomesEmUso) return false
        // Busca por SUBSTRING no JSON cru em vez de desserializar a ficha: o nome
        // é um UUID, não há risco de colisão, e assim uma ficha que falhasse no
        // parse não faria a faxina apagar retrato em uso.
        if (jsonsDasFichas.any { it.contains(nome) }) return false
        return true
    }

    // --- internos ---

    /** Nome amigável e único: o personagem some da bagunça da galeria. */
    private fun nomeDeArquivo(nomePersonagem: String): String {
        val base = nomePersonagem.trim()
            .replace(Regex("[^\\p{L}\\p{N} _-]"), "")
            .replace(' ', '_')
            .take(40)
            .ifBlank { "personagem" }
        val carimbo = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "GURPS_${base}_$carimbo.jpg"
    }

    private fun String.toUri(): Uri? = runCatching { Uri.parse(this) }.getOrNull()

    private fun decodeBitmap(context: Context, uri: Uri): Bitmap? {
        val resolver = context.contentResolver
        // 1ª passada: só dimensões para calcular inSampleSize
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        runCatching {
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        }
        var sample = 1
        val maior = max(bounds.outWidth, bounds.outHeight)
        while (maior / sample > MAX_DECODE) sample *= 2

        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val bmp = runCatching {
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
        }.getOrNull() ?: return null

        return corrigirOrientacao(context, uri, bmp)
    }

    /** Aplica a rotação do EXIF (fotos de câmera vêm rotacionadas). */
    private fun corrigirOrientacao(context: Context, uri: Uri, bmp: Bitmap): Bitmap {
        val orientation = runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            }
        }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL

        val graus = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (graus == 0f) return bmp
        val matrix = android.graphics.Matrix().apply { postRotate(graus) }
        val rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
        if (rotated != bmp) bmp.recycle()
        return rotated
    }

    // Subject Segmentation é BETA e instável (crashava com MediaPipeException
    // numa thread interna do ML Kit, que escapa do try/catch e derruba o app).
    // Por isso fica DESLIGADA por padrão; se algum dia for reativada, qualquer
    // falha a desliga permanentemente nesta sessão.
    @Volatile private var segmentacaoDesabilitada = true

    /**
     * Wrapper à prova de crash do [detectarAssunto]. Enquanto a segmentação
     * estiver desabilitada, retorna null direto (usa-se só rosto + fallback).
     * Mesmo se reativada, blinda o processo contra exceções lançadas em
     * threads internas do ML Kit instalando um handler temporário.
     */
    private fun detectarAssuntoSeguro(bmp: Bitmap): Rect? {
        if (segmentacaoDesabilitada) return null
        val threadHandlerAnterior = Thread.getDefaultUncaughtExceptionHandler()
        return try {
            // Engole exceções de threads do ML Kit (pool-*) para o crash async
            // de close() não matar o app.
            Thread.setDefaultUncaughtExceptionHandler { t, e ->
                val ehMlkit = t.name.startsWith("pool-") ||
                    e.javaClass.name.contains("mediapipe", ignoreCase = true)
                if (!ehMlkit) threadHandlerAnterior?.uncaughtException(t, e)
            }
            detectarAssunto(bmp)
        } catch (_: Throwable) {
            segmentacaoDesabilitada = true
            null
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(threadHandlerAnterior)
        }
    }

    /**
     * Detecta o ASSUNTO PRINCIPAL da imagem (pessoa, criatura, objeto) via
     * ML Kit Subject Segmentation e devolve o bounding box dos pixels do
     * assunto. Funciona mesmo sem rosto humano. Null se nada for achado.
     */
    private fun detectarAssunto(bmp: Bitmap): Rect? {
        val options = SubjectSegmenterOptions.Builder()
            .enableForegroundConfidenceMask()
            .build()
        val segmenter = SubjectSegmentation.getClient(options)
        return try {
            val input = InputImage.fromBitmap(bmp, 0)
            val result = Tasks.await(segmenter.process(input))
            val mask = result.foregroundConfidenceMask ?: return null
            boundingBoxDaMascara(mask, bmp.width, bmp.height)
        } catch (_: Throwable) {
            segmentacaoDesabilitada = true
            null
        } finally {
            runCatching { segmenter.close() }
        }
    }

    /**
     * Varre a máscara de confiança (FloatBuffer, 1 valor por pixel) e calcula
     * o retângulo que envolve os pixels com confiança > limiar. Amostra de
     * 2 em 2 pixels para ser rápido.
     */
    private fun boundingBoxDaMascara(mask: java.nio.FloatBuffer, w: Int, h: Int): Rect? {
        val limiar = 0.5f
        var minX = w; var minY = h; var maxX = -1; var maxY = -1
        val passo = 2
        var y = 0
        while (y < h) {
            var x = 0
            while (x < w) {
                val conf = mask.get(y * w + x)
                if (conf > limiar) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
                x += passo
            }
            y += passo
        }
        mask.rewind()
        if (maxX < 0 || maxY < 0) return null
        return Rect(minX, minY, maxX, maxY)
    }

    /**
     * Recorta [bmp] na proporção do cabeçalho, enquadrando o assunto de forma
     * CONSISTENTE entre artes diferentes.
     *  - COM rosto: a altura da faixa é derivada do TAMANHO do rosto (o rosto
     *    ocupa sempre ~ROSTO_FRACAO_ALTURA da faixa). Assim, um rosto pequeno
     *    na arte é "aproximado" e um rosto grande é "afastado" — todos ficam na
     *    mesma escala (rosto + ombros). Centraliza horizontal e verticalmente
     *    no rosto.
     *  - SEM rosto: faixa padrão (largura/PROPORCAO), topo do assunto ou da
     *    imagem.
     * Em ambos os casos o redimensionamento final (LARGURA_ALVO) normaliza a
     * resolução.
     */
    private fun recortarFaixa(bmp: Bitmap, assunto: Rect?, rosto: Rect?): Bitmap {
        val w = bmp.width
        val h = bmp.height

        var cropW: Int
        var cropH: Int

        if (rosto != null) {
            // Altura da faixa para o rosto ocupar ROSTO_FRACAO_ALTURA dela.
            cropH = (rosto.height() / ROSTO_FRACAO_ALTURA).roundToInt()
            cropW = (cropH * PROPORCAO).roundToInt()
            // Não pode exceder a imagem: ajusta mantendo a proporção.
            if (cropW > w) { cropW = w; cropH = (w / PROPORCAO).roundToInt() }
            if (cropH > h) { cropH = h; cropW = (h * PROPORCAO).roundToInt().coerceAtMost(w) }
        } else {
            cropW = w
            cropH = (w / PROPORCAO).roundToInt()
            if (cropH > h) { cropH = h; cropW = (h * PROPORCAO).roundToInt().coerceAtMost(w) }
        }

        // Centro horizontal: rosto > assunto > centro da imagem.
        val centroX = rosto?.centerX() ?: assunto?.centerX() ?: (w / 2)
        var left = (centroX - cropW / 2)

        val top: Int = when {
            // Rosto centralizado na vertical (levemente acima do meio: cabelo
            // em cima, ombros embaixo). 0.46 = rosto um pouco acima do centro.
            rosto != null -> rosto.centerY() - (cropH * 0.46f).roundToInt()
            // Um respiro acima do topo do assunto: encostar a faixa exatamente
            // na linha do cabelo dá um enquadramento sufocado.
            assunto != null -> assunto.top - (cropH * MARGEM_TOPO_ASSUNTO).roundToInt()
            else -> 0
        }

        left = max(0, min(left, w - cropW))
        val topClamped = max(0, min(top, h - cropH))
        android.util.Log.d(
            RostoDetector.TAG,
            "recorte ${cropW}x$cropH em ($left,$topClamped) de ${w}x$h " +
                "[rosto=${rosto != null} assunto=${assunto != null}]"
        )

        return if (left == 0 && topClamped == 0 && cropW == w && cropH == h) {
            bmp
        } else {
            Bitmap.createBitmap(bmp, left, topClamped, cropW, cropH)
        }
    }

    private fun redimensionar(bmp: Bitmap): Bitmap {
        if (bmp.width <= LARGURA_ALVO) return bmp
        val escala = LARGURA_ALVO.toFloat() / bmp.width
        val novaAltura = (bmp.height * escala).roundToInt()
        return Bitmap.createScaledBitmap(bmp, LARGURA_ALVO, novaAltura, true)
    }

    /** Redimensiona mantendo a proporção para que o MAIOR lado fique <= [maiorLado]. */
    private fun redimensionarMaiorLado(bmp: Bitmap, maiorLado: Int): Bitmap {
        val maior = max(bmp.width, bmp.height)
        if (maior <= maiorLado) return bmp
        val escala = maiorLado.toFloat() / maior
        val novaLargura = (bmp.width * escala).roundToInt().coerceAtLeast(1)
        val novaAltura = (bmp.height * escala).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bmp, novaLargura, novaAltura, true)
    }
}
