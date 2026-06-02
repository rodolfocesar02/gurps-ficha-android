package nexus.arcano

data class ArcanoEstadoPersonagem(
    val magiasConhecidasIds: Set<String>,
    val am: Int,
    val iq: Int,
    val dx: Int = 0,
    // Lote 334: vantagens/perícias da ficha (nomes NORMALIZADOS) para o motor validar
    // pré-requisitos do tipo "ou Vantagem X" (ex: Empatia, Noção do Perigo). Default
    // vazio = compatível com chamadas antigas (que só passavam magias).
    val vantagensConhecidasNorm: Set<String> = emptySet(),
    val periciasConhecidasNorm: Set<String> = emptySet()
)

data class ArcanoChave(
    val id: String,
    val descricao: String,
    val ativa: Boolean
)

data class ArcanoAcao(
    val magiaId: String,
    val motivo: String,
    val prioridade: Int
)

data class ArcanoResultado(
    val chavesAtivas: List<ArcanoChave>,
    val chavesFaltantes: List<ArcanoChave>,
    val proximasAcoes: List<ArcanoAcao>,
    val motivoBloqueio: String? = null,
    val motivoCodigo: String? = null
)

data class ArcanoRankingDiagnostico(
    val magiaId: String,
    val nome: String,
    val escola: String,
    val escolaNova: Boolean,
    val aprendivelAgora: Boolean,
    val custo: Int,
    val elegivel: Boolean,
    val motivoExclusao: String?
)

data class ArcanoCacheStats(
    val entradas: Int,
    val hits: Long,
    val misses: Long
)

data class ArcanoTimingStats(
    val amostras: Int,
    val mediaMs: Double,
    val p95Ms: Double,
    val maxMs: Double
)

data class ArcanoDeltaResultado(
    val resultado: ArcanoResultado,
    val modo: String,
    val chavesRecalculadas: Int
)

data class ArcanoPlanoResultado(
    val trilhaMagiasIds: List<String>,
    val explorados: Int,
    val motivo: String? = null,
    val proximaAcaoMagiaId: String? = trilhaMagiasIds.firstOrNull(),
    val metasImpactadasProximaAcao: List<String> = emptyList()
)

enum class ArcanoMetaTipo {
    CADEIA_MAGIA,
    ESCOLAS_DISTINTAS,
    NUMERICO_AM,
    NUMERICO_IQ,
    NUMERICO_SOMA,
    ALVO_LIBERADO
}

data class ArcanoMetaProgress(
    val id: String,
    val tipo: ArcanoMetaTipo,
    val origemMagiaId: String,
    val descricao: String,
    val requerido: Int,
    val atual: Int,
    val atendida: Boolean,
    val bloqueadaPorUpstream: Boolean = false
)

interface ArcanoCatalogo {
    fun preRequisitoRaw(magiaId: String): String
    fun escolas(magiaId: String): List<String>
    fun nome(magiaId: String): String
    fun existe(magiaId: String): Boolean
    fun todasMagiasIds(): List<String>
}
