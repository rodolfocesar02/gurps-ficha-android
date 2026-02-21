package com.gurps.ficha.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.model.*

/**
 * Repositorio para carregar dados de Vantagens, Desvantagens, Pericias e Magias
 * a partir dos arquivos JSON em assets/
 */
class DataRepository(private val context: Context) {

    private val gson = Gson()

    private var _vantagens: List<VantagemDefinicao>? = null
    private var _desvantagens: List<DesvantagemDefinicao>? = null
    private var _pericias: List<PericiaDefinicao>? = null
    private var _magias: List<MagiaDefinicao>? = null

    val vantagens: List<VantagemDefinicao>
        get() = _vantagens ?: carregarVantagens().also { _vantagens = it }

    val desvantagens: List<DesvantagemDefinicao>
        get() = _desvantagens ?: carregarDesvantagens().also { _desvantagens = it }

    val pericias: List<PericiaDefinicao>
        get() = _pericias ?: carregarPericias().also { _pericias = it }

    val magias: List<MagiaDefinicao>
        get() = _magias ?: carregarMagias().also { _magias = it }

    private fun carregarVantagens(): List<VantagemDefinicao> {
        return try {
            val json = context.assets.open("vantagens.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<VantagemDefinicao>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun carregarDesvantagens(): List<DesvantagemDefinicao> {
        return try {
            val json = context.assets.open("desvantagens.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<DesvantagemDefinicao>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun carregarPericias(): List<PericiaDefinicao> {
        return try {
            val json = context.assets.open("pericias.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<PericiaDefinicao>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun carregarMagias(): List<MagiaDefinicao> {
        return try {
            val json = context.assets.open("magias.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<MagiaDefinicao>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // === FILTROS DE VANTAGENS ===

    fun filtrarVantagens(
        busca: String = "",
        tipoCusto: TipoCusto? = null
    ): List<VantagemDefinicao> {
        return vantagens.filter { v ->
            val matchBusca = busca.isBlank() || v.nome.contains(busca, ignoreCase = true)
            val matchTipo = tipoCusto == null || v.tipoCusto == tipoCusto
            matchBusca && matchTipo
        }
    }

    // === FILTROS DE DESVANTAGENS ===

    fun filtrarDesvantagens(
        busca: String = "",
        tipoCusto: TipoCusto? = null
    ): List<DesvantagemDefinicao> {
        return desvantagens.filter { d ->
            val matchBusca = busca.isBlank() || d.nome.contains(busca, ignoreCase = true)
            val matchTipo = tipoCusto == null || d.tipoCusto == tipoCusto
            matchBusca && matchTipo
        }
    }

    // === FILTROS DE PERICIAS ===

    fun filtrarPericias(
        busca: String = "",
        atributoBase: String? = null,
        dificuldade: String? = null
    ): List<PericiaDefinicao> {
        return pericias.filter { p ->
            val matchBusca = busca.isBlank() || p.nome.contains(busca, ignoreCase = true)
            val matchAtributo = atributoBase.isNullOrBlank() ||
                p.atributoBase.equals(atributoBase, ignoreCase = true) ||
                p.atributosPossiveis?.any { it.equals(atributoBase, ignoreCase = true) } == true
            val matchDificuldade = dificuldade.isNullOrBlank() ||
                p.dificuldadeFixa.equals(dificuldade, ignoreCase = true)
            matchBusca && matchAtributo && matchDificuldade
        }
    }

    // === FILTROS DE MAGIAS ===

    fun filtrarMagias(
        busca: String = ""
    ): List<MagiaDefinicao> {
        return magias.filter { m ->
            busca.isBlank() || m.nome.contains(busca, ignoreCase = true)
        }
    }

    // === CONVERSORES PARA SELECAO ===

    fun criarVantagemSelecionada(
        definicao: VantagemDefinicao,
        nivel: Int = 1,
        custoEscolhido: Int = 0,
        descricao: String = ""
    ): VantagemSelecionada {
        return VantagemSelecionada(
            definicaoId = definicao.id,
            nome = definicao.nome,
            custoBase = definicao.getCustoBase(),
            nivel = nivel,
            custoEscolhido = custoEscolhido.takeIf { it != 0 } ?: definicao.getCustoBase(),
            descricao = descricao,
            tipoCusto = definicao.tipoCusto,
            pagina = definicao.pagina
        )
    }

    fun criarDesvantagemSelecionada(
        definicao: DesvantagemDefinicao,
        nivel: Int = 1,
        custoEscolhido: Int = 0,
        descricao: String = "",
        autocontrole: Int? = null
    ): DesvantagemSelecionada {
        return DesvantagemSelecionada(
            definicaoId = definicao.id,
            nome = definicao.nome,
            custoBase = definicao.getCustoBase(),
            nivel = nivel,
            custoEscolhido = custoEscolhido.takeIf { it != 0 } ?: definicao.getCustoBase(),
            descricao = descricao,
            autocontrole = autocontrole,
            tipoCusto = definicao.tipoCusto,
            pagina = definicao.pagina
        )
    }

    fun criarPericiaSelecionada(
        definicao: PericiaDefinicao,
        pontosGastos: Int = 1,
        especializacao: String = "",
        atributoEscolhido: AtributoBase? = null,
        dificuldadeEscolhida: Dificuldade? = null
    ): PericiaSelecionada {
        val atributo = atributoEscolhido
            ?: AtributoBase.fromSigla(definicao.atributoBase)
        val dificuldade = dificuldadeEscolhida
            ?: Dificuldade.fromSigla(definicao.dificuldadeFixa)

        return PericiaSelecionada(
            definicaoId = definicao.id,
            nome = definicao.nome,
            atributoBase = atributo,
            dificuldade = dificuldade,
            pontosGastos = pontosGastos,
            especializacao = especializacao,
            exigeEspecializacao = definicao.exigeEspecializacao
        )
    }

    fun criarMagiaSelecionada(
        definicao: MagiaDefinicao,
        pontosGastos: Int = 1
    ): MagiaSelecionada {
        return MagiaSelecionada(
            definicaoId = definicao.id,
            nome = definicao.nome,
            pontosGastos = pontosGastos,
            pagina = definicao.pagina,
            texto = definicao.texto
        )
    }

    // === BUSCA POR ID ===

    fun getVantagemPorId(id: String): VantagemDefinicao? {
        return vantagens.find { it.id == id }
    }

    fun getDesvantagemPorId(id: String): DesvantagemDefinicao? {
        return desvantagens.find { it.id == id }
    }

    fun getPericiaPorId(id: String): PericiaDefinicao? {
        return pericias.find { it.id == id }
    }

    fun getMagiaPorId(id: String): MagiaDefinicao? {
        return magias.find { it.id == id }
    }

    companion object {
        @Volatile
        private var INSTANCE: DataRepository? = null

        fun getInstance(context: Context): DataRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DataRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
