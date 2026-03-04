package nexus.arcano

object NexusArcanoTestCatalog {
    fun base(): ArcanoCatalogo {
        data class M(val id: String, val nome: String, val escolas: List<String>, val pre: String)
        val magias = listOf(
            M("encantar", "Encantar", listOf("Encantamento"), "1 magica em 10 outras escolas"),
            M("pequeno_desejo", "Pequeno Desejo", listOf("Encantamento"), "Encantar"),
            M("desejo", "Desejo", listOf("Encantamento"), "Pequeno Desejo, 1 magica em 15 escolas"),
            M("desejo_superior", "Desejo Superior", listOf("Encantamento"), "AM3, Desejo, (DX + IQ):30+"),
            M("voo_do_falcao", "Voo do Falcao", listOf("Ar"), ""),
            M("teleporte", "Teleporte", listOf("Deslocamento", "Portal"), "Voo do Falcao ou IQ 13+ e 1 magica em 10 escolas diferentes"),
            M("translocacao", "Translocacao", listOf("Deslocamento", "Portal"), "Teleporte"),
            M("convocar_demonio", "Convocar Demonio", listOf("Necromancia"), "AM1, 1 magica em 10 escolas diferentes"),
            M("magia_ar", "Magia Ar", listOf("Ar"), ""),
            M("magia_terra", "Magia Terra", listOf("Terra"), ""),
            M("magia_agua", "Magia Agua", listOf("Agua"), ""),
            M("magia_fogo", "Magia Fogo", listOf("Fogo"), ""),
            M("magia_corpo", "Magia Corpo", listOf("Corpo"), ""),
            M("magia_luz", "Magia Luz", listOf("Luz"), ""),
            M("magia_som", "Magia Som", listOf("Som"), ""),
            M("magia_mente", "Magia Mente", listOf("Mente"), ""),
            M("magia_necro", "Magia Necro", listOf("Necromancia"), ""),
            M("magia_meta", "Magia Meta", listOf("Metamagica"), ""),
            M("magia_info", "Magia Info", listOf("Informacao"), ""),
            M("magia_portal", "Magia Portal", listOf("Portal"), "")
        )
        val byId = magias.associateBy { it.id }
        return object : ArcanoCatalogo {
            override fun preRequisitoRaw(magiaId: String): String = byId[magiaId]?.pre.orEmpty()
            override fun escolas(magiaId: String): List<String> = byId[magiaId]?.escolas.orEmpty()
            override fun nome(magiaId: String): String = byId[magiaId]?.nome ?: magiaId
            override fun existe(magiaId: String): Boolean = byId.containsKey(magiaId)
            override fun todasMagiasIds(): List<String> = byId.keys.sorted()
        }
    }
}