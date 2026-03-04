package nexus.arcano

/**
 * Catálogo de exemplo para validar a ordem hard-first do caso Desejo.
 * Uso manual: instanciar NexusArcanoEngine(ArcanoCatalogoDesejoExemplo())
 */
class ArcanoCatalogoDesejoExemplo : ArcanoCatalogo {
    private data class M(val id: String, val nome: String, val escolas: List<String>, val pre: String)

    private val magias = listOf(
        M("encantar", "Encantar", listOf("Encantamento"), "1 mágica em 10 outras escolas"),
        M("pequeno_desejo", "Pequeno Desejo", listOf("Encantamento"), "Encantar"),
        M("desejo", "Desejo", listOf("Encantamento"), "Pequeno Desejo, 1 mágica em 15 escolas")
    )
    private val byId = magias.associateBy { it.id }

    override fun preRequisitoRaw(magiaId: String): String = byId[magiaId]?.pre.orEmpty()
    override fun escolas(magiaId: String): List<String> = byId[magiaId]?.escolas.orEmpty()
    override fun nome(magiaId: String): String = byId[magiaId]?.nome ?: magiaId
    override fun existe(magiaId: String): Boolean = byId.containsKey(magiaId)
    override fun todasMagiasIds(): List<String> = byId.keys.sorted()
}
