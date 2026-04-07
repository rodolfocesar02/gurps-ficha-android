package com.gurps.ficha.viewmodel.delegates

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.model.*
import com.gurps.ficha.viewmodel.*

class FichaSearchDelegate(private val dataRepository: DataRepository) {
    var advantageSearch by mutableStateOf(TraitSearchState())
    var disadvantageSearch by mutableStateOf(TraitSearchState())
    var skillSearch by mutableStateOf(SkillSearchState())
    var magicSearch by mutableStateOf(MagicSearchState())
    var techniqueSearch by mutableStateOf(TechniqueSearchState())
    var equipmentSearch by mutableStateOf(EquipmentSearchState())
    var shieldSearchQuery by mutableStateOf("")

    private var magiasFiltradasCacheKey: String? = null
    private var magiasFiltradasCache: List<MagiaDefinicao> = emptyList()

    fun filtrarVantagens(): List<VantagemDefinicao> =
        dataRepository.filtrarVantagens(advantageSearch.query, advantageSearch.costType, null)

    fun filtrarDesvantagens(): List<DesvantagemDefinicao> =
        dataRepository.filtrarDesvantagens(disadvantageSearch.query, disadvantageSearch.costType)

    fun filtrarPericias(): List<PericiaDefinicao> =
        dataRepository.filtrarPericias(skillSearch.query, skillSearch.attribute, skillSearch.difficulty)

    fun filtrarMagias(): List<MagiaDefinicao> {
        val key = "${magicSearch.query.trim()}|${magicSearch.school.orEmpty()}|${magicSearch.magicClass.orEmpty()}"
        if (key != magiasFiltradasCacheKey) {
            magiasFiltradasCacheKey = key
            magiasFiltradasCache = dataRepository.filtrarMagias(magicSearch.query, magicSearch.school, magicSearch.magicClass)
        }
        return magiasFiltradasCache
    }

    fun filtrarTecnicas(): List<TecnicaCatalogoItem> =
        dataRepository.filtrarTecnicasCatalogo(techniqueSearch.query, techniqueSearch.source)

    fun filtrarArmas(personagemForca: Int, fireArmCategoryHelper: (ArmaCatalogoItem) -> String): List<ArmaCatalogoItem> {
        val base = dataRepository.filtrarArmasCatalogo(
            busca = equipmentSearch.query,
            tipoCombate = equipmentSearch.type,
            stMaximo = personagemForca
        )
        val categoriaFiltro = equipmentSearch.fireArmCategory
        if (categoriaFiltro.isNullOrBlank()) return base
        return base.filter { arma ->
            arma.tipoCombate == "armas_de_fogo" && fireArmCategoryHelper(arma) == categoriaFiltro
        }
    }

    fun filtrarEscudos(personagemForca: Int): List<EscudoCatalogoItem> =
        dataRepository.filtrarEscudosCatalogo(busca = shieldSearchQuery, stMaximo = personagemForca)

    fun filtrarArmaduras(): List<ArmaduraCatalogoItem> =
        dataRepository.filtrarArmadurasCatalogo(
            busca = equipmentSearch.query,
            nt = equipmentSearch.armorerNt,
            localFiltro = equipmentSearch.armorerLocation,
            tagFiltro = equipmentSearch.armorerTag
        )

    fun resetarCacheMagia() {
        magicSearch = magicSearch.copy()
        magiasFiltradasCacheKey = null
    }

    fun limparArmaduras() {
        equipmentSearch = EquipmentSearchState()
    }

    fun resetarTodosCaches() {
        advantageSearch = advantageSearch.copy()
        disadvantageSearch = disadvantageSearch.copy()
        skillSearch = skillSearch.copy()
        magicSearch = magicSearch.copy()
        techniqueSearch = techniqueSearch.copy()
        equipmentSearch = equipmentSearch.copy()
        shieldSearchQuery = ""
        magiasFiltradasCacheKey = null
    }
}
