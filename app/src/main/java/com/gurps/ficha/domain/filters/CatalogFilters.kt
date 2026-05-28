package com.gurps.ficha.domain.filters

import com.gurps.ficha.model.*
import java.text.Normalizer
import com.gurps.ficha.data.DataRepository

object CatalogFilters {

    fun normalizarBusca(valor: String): String =
        TextNormalizer.normalize(valor, TextNormalizer.BUSCA_PADRAO)

    fun contemBusca(texto: String, busca: String): Boolean {
        if (busca.isBlank()) return true
        val buscaNorm = normalizarBusca(busca)
        if (buscaNorm.isBlank()) return true
        return normalizarBusca(texto).contains(buscaNorm)
    }

    fun igualNormalizado(texto: String, valor: String): Boolean {
        val textoNorm = normalizarBusca(texto)
        val valorNorm = normalizarBusca(valor)
        if (textoNorm.isBlank() || valorNorm.isBlank()) return false
        return textoNorm == valorNorm
    }

    fun filtrarVantagens(
        vantagens: List<VantagemDefinicao>,
        vantagensArtesMarciaisIds: Set<String>,
        busca: String = "",
        tipoCusto: TipoCusto? = null,
        tag: String? = null,
        somenteArtesMarciais: Boolean = false
    ): List<VantagemDefinicao> {
        return vantagens.filter { v ->
            val matchBusca = contemBusca(v.nome, busca)
            val matchTipo = tipoCusto == null || v.tipoCusto == tipoCusto
            val matchTag = tag.isNullOrBlank() || v.tags.any { contemBusca(it, tag) }
            val matchArtesMarciais = !somenteArtesMarciais || vantagensArtesMarciaisIds.contains(v.id.lowercase())
            matchBusca && matchTipo && matchTag && matchArtesMarciais
        }
    }

    fun filtrarDesvantagens(
        desvantagens: List<DesvantagemDefinicao>,
        busca: String = "",
        tipoCusto: TipoCusto? = null,
        tag: String? = null
    ): List<DesvantagemDefinicao> {
        return desvantagens.filter { d ->
            val matchBusca = contemBusca(d.nome, busca)
            val matchTipo = tipoCusto == null || d.tipoCusto == tipoCusto
            val matchTag = tag.isNullOrBlank() || d.tags.any { contemBusca(it, tag) }
            matchBusca && matchTipo && matchTag
        }
    }

    fun filtrarPericias(
        pericias: List<PericiaDefinicao>,
        busca: String = "",
        atributoBase: String? = null,
        dificuldade: String? = null
    ): List<PericiaDefinicao> {
        return pericias.filter { p ->
            val matchBusca = contemBusca(p.nome, busca)
            val matchAtributo = atributoBase.isNullOrBlank() ||
                contemBusca(p.atributoBase, atributoBase) ||
                p.atributosPossiveis?.any { contemBusca(it, atributoBase) } == true
            val matchDificuldade = dificuldade.isNullOrBlank() ||
                contemBusca(p.dificuldadeFixa.orEmpty(), dificuldade)
            matchBusca && matchAtributo && matchDificuldade
        }
    }

    fun filtrarTecnicasCatalogo(
        tecnicasCatalogo: List<TecnicaCatalogoItem>,
        busca: String = "",
        sourceBook: String? = null
    ): List<TecnicaCatalogoItem> {
        val b = busca.trim()
        return tecnicasCatalogo.filter { t ->
            val matchBusca = b.isBlank() ||
                contemBusca(t.nome, b) ||
                contemBusca(t.descricao, b) ||
                contemBusca(t.preRequisitoRaw, b)
            val matchSource = sourceBook.isNullOrBlank() || contemBusca(t.sourceBook, sourceBook)
            matchBusca && matchSource
        }.sortedBy { it.nome.lowercase() }
    }

    fun filtrarArmasCatalogo(
        armasCatalogo: List<ArmaCatalogoItem>,
        busca: String = "",
        tipoCombate: String? = null,
        stMaximo: Int? = null
    ): List<ArmaCatalogoItem> {
        val buscaNormalizada = busca.trim()
        return armasCatalogo.filter { a ->
            val matchBusca = buscaNormalizada.isBlank() ||
                contemBusca(a.nome, buscaNormalizada) ||
                contemBusca(a.grupo, buscaNormalizada) ||
                contemBusca(a.categoria, buscaNormalizada)
            val matchTipo = tipoCombate.isNullOrBlank() || contemBusca(a.tipoCombate, tipoCombate)
            val matchSt = stMaximo == null || a.stMinimo == null || a.stMinimo <= stMaximo
            matchBusca && matchTipo && matchSt
        }.sortedBy { it.nome.lowercase() }
    }

    fun filtrarEscudosCatalogo(
        escudosCatalogo: List<EscudoCatalogoItem>,
        busca: String = "",
        stMaximo: Int? = null
    ): List<EscudoCatalogoItem> {
        val b = busca.trim()
        return escudosCatalogo.filter { e ->
            val matchBusca = b.isBlank() || contemBusca(e.nome, b)
            val matchSt = stMaximo == null || e.stMinimo == null || e.stMinimo <= stMaximo
            matchBusca && matchSt
        }.sortedBy { it.nome.lowercase() }
    }

    fun filtrarArmadurasCatalogo(
        armadurasCatalogo: List<ArmaduraCatalogoItem>,
        busca: String = "",
        nt: Int? = null,
        localFiltro: String? = null,
        tagFiltro: String? = null
    ): List<ArmaduraCatalogoItem> {
        val b = busca.trim()
        val tag = tagFiltro?.trim().orEmpty()
        return armadurasCatalogo.filter { a ->
            val matchBusca = b.isBlank() ||
                contemBusca(a.nome, b) ||
                contemBusca(a.local, b) ||
                contemBusca(a.rd, b) ||
                a.tags.any { contemBusca(it, b) } ||
                a.observacoesDetalhadas.any { contemBusca(it, b) } ||
                a.componentes.any { c ->
                    c.tags.any { contemBusca(it, b) } ||
                        c.observacoesDetalhadas.any { contemBusca(it, b) }
                }
            val matchNt = nt == null || a.nt == nt
            val matchLocal = localFiltro.isNullOrBlank() || armaduraCobreLocal(a, localFiltro)
            val matchTag = tag.isBlank() || armaduraTemTag(a, tag)
            matchBusca && matchNt && matchLocal && matchTag
        }.sortedBy { it.nome.lowercase() }
    }

    private fun armaduraTemTag(armadura: ArmaduraCatalogoItem, tagFiltro: String): Boolean {
        if (tagFiltro.isBlank()) return true
        return armadura.tags.any { contemBusca(it, tagFiltro) } ||
            armadura.componentes.any { c -> c.tags.any { contemBusca(it, tagFiltro) } }
    }

    private fun armaduraCobreLocal(armadura: ArmaduraCatalogoItem, localFiltro: String): Boolean {
        val filtro = normalizarLocal(localFiltro)
        if (filtro.isBlank()) return true
        val locaisBrutos = mutableSetOf<String>()
        locaisBrutos.addAll(extrairLocais(armadura.local))
        armadura.componentes.forEach { c -> locaisBrutos.addAll(extrairLocais(c.local)) }

        val locaisExpandidos = locaisBrutos
            .flatMap { expandirLocalMacro(it) }
            .map { normalizarLocal(it) }
            .filter { it.isNotBlank() }
            .toSet()

        val filtroExpandido = expandirLocalMacro(filtro).map { normalizarLocal(it) }.toSet()
        return filtroExpandido.any { it in locaisExpandidos }
    }

    private fun extrairLocais(raw: String): List<String> {
        return raw
            .split(Regex("[,;/|]"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    private fun expandirLocalMacro(local: String): List<String> {
        return when (normalizarLocal(local)) {
            "cabeca" -> listOf("cranio", "olhos", "rosto")
            "corpo" -> listOf("pescoco", "tronco", "virilha")
            "membros" -> listOf("bracos", "pernas")
            "traje_completo" -> listOf("pescoco", "tronco", "virilha", "bracos", "maos", "pernas", "pes")
            else -> listOf(local)
        }
    }

    private fun normalizarLocal(local: String): String {
        return Normalizer.normalize(local, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase()
            .replace(Regex("\\s+"), "_")
            .trim()
    }
}
