package nexus.arcano

data class ArcanoEstadoPersonagem(
    val magiasConhecidasIds: Set<String>,
    val am: Int,
    val iq: Int
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
    val motivoBloqueio: String? = null
)

interface ArcanoCatalogo {
    fun preRequisitoRaw(magiaId: String): String
    fun escolas(magiaId: String): List<String>
    fun existe(magiaId: String): Boolean
    fun todasMagiasIds(): List<String>
}

class NexusArcanoEngine(
    private val catalogo: ArcanoCatalogo
) {
    fun calcularEstadoAlvo(alvoId: String, estado: ArcanoEstadoPersonagem): ArcanoResultado {
        // Lote 1: núcleo em construção.
        return ArcanoResultado(
            chavesAtivas = emptyList(),
            chavesFaltantes = listOf(
                ArcanoChave(
                    id = "cadeia_obrigatoria",
                    descricao = "Avaliação da cadeia obrigatória pendente",
                    ativa = false
                )
            ),
            proximasAcoes = emptyList(),
            motivoBloqueio = "Núcleo em implementação (Lote 1)."
        )
    }
}
