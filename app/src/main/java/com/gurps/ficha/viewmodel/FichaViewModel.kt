package com.gurps.ficha.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gurps.ficha.BuildConfig
import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.data.network.DiscordRollApiClient
import com.gurps.ficha.data.network.DiscordRollPayload
import com.gurps.ficha.data.network.DiscordVoiceChannel
import com.gurps.ficha.domain.magias.NexusArcanoModoAlvoAdapter
import com.gurps.ficha.data.storage.FichaStorageRepository
import com.gurps.ficha.domain.roll.RollDispatchPolicy
import com.gurps.ficha.domain.rules.CharacterRules
import com.gurps.ficha.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.gurps.ficha.data.network.MestreIAClient
import com.gurps.ficha.domain.MestreIAUseCase
import android.util.Log
import com.gurps.ficha.domain.engine.MagicEngine
import com.gurps.ficha.domain.engine.SkillEngine
import java.text.Normalizer

enum class DefenseType { ESQUIVA, APARA, BLOQUEIO }

data class ActiveDefense(
    val type: DefenseType,
    val name: String,
    val baseValue: Int,
    val bonus: Int,
    val finalValue: Int
)

data class RollDispatchStatus(
    val enviado: Boolean,
    val detalhe: String? = null
)


@OptIn(FlowPreview::class)
class FichaViewModel(application: Application) : AndroidViewModel(application) {
    private val autoSaveRecuperacaoNome = "_autosave_recuperacao"
    var personagem by mutableStateOf(Personagem())
        private set

    var fichasSalvas by mutableStateOf<List<String>>(emptyList())
        private set

    var mostrarConfirmacaoLimpezaMagias by mutableStateOf(false)
        private set

    // Estado de busca e filtros
    var advantageSearch by mutableStateOf(TraitSearchState())
        private set
    var disadvantageSearch by mutableStateOf(TraitSearchState())
        private set
    var skillSearch by mutableStateOf(SkillSearchState())
        private set
    var magicSearch by mutableStateOf(MagicSearchState())
        private set
    var techniqueSearch by mutableStateOf(TechniqueSearchState())
        private set
    var equipmentSearch by mutableStateOf(EquipmentSearchState())
        private set
    var shieldSearchQuery by mutableStateOf("")
        private set


    var modoAlvoRelacionadosIds by mutableStateOf<List<String>>(emptyList())
        private set
    var modoAlvoCarregando by mutableStateOf(false)
        private set
    var modoAlvoErro by mutableStateOf<String?>(null)
        private set
    var modoAlvoAviso by mutableStateOf<String?>(null)
        private set
    var modoAlvoChavesAtivas by mutableStateOf<List<String>>(emptyList())
        private set
    var modoAlvoChavesFaltantes by mutableStateOf<List<String>>(emptyList())
        private set
    var modoAlvoProximasAcoes by mutableStateOf<List<String>>(emptyList())
        private set
    var modoAlvoProximasAcoesIds by mutableStateOf<List<String>>(emptyList())
        private set
    var modoAlvoProgressoCadeia by mutableStateOf<String?>(null)
        private set
    var modoAlvoProgressoEscolas by mutableStateOf<List<String>>(emptyList())
        private set
    var modoAlvoProximaObrigatoria by mutableStateOf<String?>(null)
        private set
    var modoAlvoProximaLateralUtil by mutableStateOf<String?>(null)
        private set
    var modoAlvoBloqueioCurto by mutableStateOf<String?>(null)
    private var modoAlvoJob: Job? = null
    private var modoAlvoUltimaChave: String? = null
    private var prereqCacheAssinatura: String? = null
    private val prereqFailureCache = HashMap<String, String?>()
    private var magiasFiltradasCacheKey: String? = null
    private var magiasFiltradasCache: List<MagiaDefinicao> = emptyList()
    private val magiasByIdCache: Map<String, MagiaDefinicao> by lazy {
        dataRepository.magias.associateBy { it.id }
    }
    private val todasEscolasMagiaCache: List<String> by lazy {
        dataRepository.magias
            .flatMap { it.escola ?: emptyList() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }
    private val todasClassesMagiaCache: List<String> by lazy {
        dataRepository.magias
            .mapNotNull { dataRepository.agruparClasseMagia(it.classe) }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }

    private val fichaStorage = FichaStorageRepository.getInstance(application)
    val dataRepository = DataRepository.getInstance(application)
    private val nexusArcanoModoAlvoAdapter by lazy {
        NexusArcanoModoAlvoAdapter(dataRepository.magias)
    }
    private val tecnicasNomesNormalizados: Set<String>
        get() = dataRepository.tecnicasCatalogo
            .asSequence()
            .map { normalizarTexto(it.nome) }
            .filter { it.isNotBlank() }
            .toSet()
    private val configPrefs = application.getSharedPreferences("gurps_config", Context.MODE_PRIVATE)
    private var personagemPendenteLimpezaMagias: Personagem? = null
    private val prefCanalDiscordId = "discord_canal_id"
    private val prefCanalDiscordNome = "discord_canal_nome"
    private val prefNaoMostrarManualModoAlvo = "modo_alvo_manual_nao_mostrar"
    private val prefIABaseUrl = "ia_base_url"
    private val prefIAApiKey = "ia_api_key"
    private val prefIAWorkspaceSlug = "ia_workspace_slug"

    var canaisDiscord by mutableStateOf<List<DiscordVoiceChannel>>(emptyList())
        private set
    var canaisDiscordCarregando by mutableStateOf(false)
        private set
    var canaisDiscordErro by mutableStateOf<String?>(null)
        private set
    var canalDiscordSelecionadoId by mutableStateOf<String?>(null)
        private set
    var canalDiscordSelecionadoNome by mutableStateOf(configPrefs.getString(prefCanalDiscordNome, null))
        private set

    var iaBaseUrl by mutableStateOf(configPrefs.getString(prefIABaseUrl, "https://rodolfocesar02-mestre-gurps-ia.hf.space") ?: "https://rodolfocesar02-mestre-gurps-ia.hf.space")
        private set
    var iaApiKey by mutableStateOf(configPrefs.getString(prefIAApiKey, "781B4KF-X81M4RB-QTNSSHR-843BQG5") ?: "781B4KF-X81M4RB-QTNSSHR-843BQG5")
        private set
    var iaWorkspaceSlug by mutableStateOf(configPrefs.getString(prefIAWorkspaceSlug, "meu-workspace") ?: "meu-workspace")
        private set

    // Listas filtradas
    val vantagensFiltradas: List<VantagemDefinicao>
        get() = dataRepository.filtrarVantagens(advantageSearch.query, advantageSearch.costType, null)

    val desvantagensFiltradas: List<DesvantagemDefinicao>
        get() = dataRepository.filtrarDesvantagens(disadvantageSearch.query, disadvantageSearch.costType)

    val periciasFiltradas: List<PericiaDefinicao>
        get() = dataRepository.filtrarPericias(skillSearch.query, skillSearch.attribute, skillSearch.difficulty)

    val periciasSuplementaresArtesMarciais: List<PericiaSuplementarItem>
        get() = dataRepository.periciasSuplementares

    val magiasFiltradas: List<MagiaDefinicao>
        get() {
            val key = "${magicSearch.query.trim()}|${magicSearch.school.orEmpty()}|${magicSearch.magicClass.orEmpty()}"
            if (key != magiasFiltradasCacheKey) {
                magiasFiltradasCacheKey = key
                magiasFiltradasCache = dataRepository.filtrarMagias(magicSearch.query, magicSearch.school, magicSearch.magicClass)
            }
            return magiasFiltradasCache
        }

    val tecnicasCatalogo: List<TecnicaCatalogoItem>
        get() = dataRepository.tecnicasCatalogo

    val tecnicasFiltradas: List<TecnicaCatalogoItem>
        get() = dataRepository.filtrarTecnicasCatalogo(techniqueSearch.query, techniqueSearch.source)

    val modificadoresGerais: List<ModificadorDefinicao>
        get() = dataRepository.modificadoresGerais

    val armasEquipamentosFiltradas: List<ArmaCatalogoItem>
        get() {
            val base = dataRepository.filtrarArmasCatalogo(
                busca = equipmentSearch.query,
                tipoCombate = equipmentSearch.type,
                stMaximo = personagem.forca
            )
            val categoriaFiltro = equipmentSearch.fireArmCategory
            if (categoriaFiltro.isNullOrBlank()) return base
            return base.filter { arma ->
                arma.tipoCombate == "armas_de_fogo" &&
                    categoriaArmaFogoParaFiltro(arma) == categoriaFiltro
            }
        }

    val escudosEquipamentosFiltrados: List<EscudoCatalogoItem>
        get() = dataRepository.filtrarEscudosCatalogo(
            busca = shieldSearchQuery,
            stMaximo = personagem.forca
        )

    val armadurasEquipamentosFiltradas: List<ArmaduraCatalogoItem>
        get() = dataRepository.filtrarArmadurasCatalogo(
            busca = equipmentSearch.query,
            nt = equipmentSearch.armorerNt,
            localFiltro = equipmentSearch.armorerLocation,
            tagFiltro = equipmentSearch.armorerTag
        )

    val tagsArmadurasEquipamentos: List<String>
        get() = dataRepository.armadurasCatalogo
            .asSequence()
            .flatMap { armadura ->
                sequenceOf(armadura.tags) + armadura.componentes.map { it.tags }
            }
            .flatMap { it.asSequence() }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot { it.startsWith("local:", ignoreCase = true) }
            .filterNot { it.startsWith("local_exp:", ignoreCase = true) }
            .filterNot { it.startsWith("nt:", ignoreCase = true) }
            .filterNot { it.startsWith("tipo:", ignoreCase = true) }
            .distinct()
            .sorted()
            .toList()

    val errosCargaCatalogos: Map<String, String>
        get() = dataRepository.getCatalogLoadErrors()

    val escudosEquipados: List<Equipamento>
        get() = personagem.equipamentos
            .filter { it.tipo == TipoEquipamento.ESCUDO }
            .sortedBy { it.nome.lowercase() }

    val todasEscolasMagia: List<String>
        get() = todasEscolasMagiaCache

    val todasClassesMagia: List<String>
        get() = todasClassesMagiaCache

    init {
        CharacterRules.DATA_REPOSITORY_INSTANCE = dataRepository
        canalDiscordSelecionadoId = configPrefs.getString(prefCanalDiscordId, null)
        canalDiscordSelecionadoNome = configPrefs.getString(prefCanalDiscordNome, null)

        // Pré-aquece catálogo/índices de magia fora da UI para reduzir lentidão
        // na primeira abertura do seletor de magias.
        viewModelScope.launch(Dispatchers.Default) {
            runCatching {
                dataRepository.magias
                dataRepository.filtrarMagias()
            }
        }

        viewModelScope.launch {
            fichaStorage.migrarDeSharedPreferencesSeNecessario()
            restaurarAutoSaveSeExistir()
            carregarListaFichas()
        }

        viewModelScope.launch {
            snapshotFlow { personagem.toJson() }
                .distinctUntilChanged()
                .debounce(600)
                .collect { json ->
                    fichaStorage.salvarFicha(autoSaveRecuperacaoNome, json)
                }
        }
    }

    // === FILTROS ===
    fun atualizarBuscaVantagem(busca: String) {
        advantageSearch = advantageSearch.copy(query = busca)
    }

    fun atualizarFiltroTipoCustoVantagem(tipo: TipoCusto?) {
        advantageSearch = advantageSearch.copy(costType = tipo)
    }

    fun atualizarBuscaDesvantagem(busca: String) {
        disadvantageSearch = disadvantageSearch.copy(query = busca)
    }

    fun atualizarFiltroTipoCustoDesvantagem(tipo: TipoCusto?) {
        disadvantageSearch = disadvantageSearch.copy(costType = tipo)
    }

    fun atualizarBuscaPericia(busca: String) {
        skillSearch = skillSearch.copy(query = busca)
    }

    fun atualizarFiltroAtributoPericia(atributo: String?) {
        skillSearch = skillSearch.copy(attribute = atributo)
    }

    fun atualizarFiltroDificuldadePericia(dificuldade: String?) {
        skillSearch = skillSearch.copy(difficulty = dificuldade)
    }

    fun atualizarBuscaMagia(busca: String) {
        magicSearch = magicSearch.copy(query = busca)
    }

    fun atualizarFiltroEscolaMagia(escola: String?) {
        magicSearch = magicSearch.copy(school = escola)
    }

    fun atualizarFiltroClasseMagia(classe: String?) {
        magicSearch = magicSearch.copy(magicClass = classe)
    }

    fun atualizarBuscaTecnica(busca: String) {
        techniqueSearch = techniqueSearch.copy(query = busca)
    }

    fun atualizarFiltroFonteTecnica(fonte: String?) {
        techniqueSearch = techniqueSearch.copy(source = fonte)
    }

    fun atualizarBuscaArmaEquipamento(busca: String) {
        equipmentSearch = equipmentSearch.copy(query = busca)
    }

    fun atualizarFiltroTipoArmaEquipamento(tipo: String?) {
        equipmentSearch = if (tipo != "armas_de_fogo") {
            equipmentSearch.copy(type = tipo, fireArmCategory = null)
        } else {
            equipmentSearch.copy(type = tipo)
        }
    }

    fun atualizarFiltroCategoriaArmaFogoEquipamento(categoria: String?) {
        equipmentSearch = equipmentSearch.copy(fireArmCategory = categoria)
    }

    fun atualizarBuscaEscudoEquipamento(busca: String) {
        shieldSearchQuery = busca
    }

    fun atualizarBuscaArmaduraEquipamento(busca: String) {
        equipmentSearch = equipmentSearch.copy(query = busca)
    }

    fun atualizarFiltroNtArmaduraEquipamento(nt: Int?) {
        equipmentSearch = equipmentSearch.copy(armorerNt = nt)
    }

    fun atualizarFiltroLocalArmaduraEquipamento(local: String?) {
        equipmentSearch = equipmentSearch.copy(armorerLocation = local)
    }

    fun atualizarFiltroTagArmaduraEquipamento(tag: String?) {
        equipmentSearch = equipmentSearch.copy(armorerTag = tag?.trim()?.takeIf { it.isNotBlank() })
    }

    fun limparFiltrosArmaduraEquipamento() {
        equipmentSearch = equipmentSearch.copy(
            query = "",
            armorerNt = null,
            armorerLocation = null,
            armorerTag = null
        )
    }
 

    // === INFORMACOES BASICAS ===

    fun atualizarNome(nome: String) {
        personagem = personagem.copy(nome = nome)
    }

    fun atualizarJogador(jogador: String) {
        personagem = personagem.copy(jogador = jogador)
    }

    fun atualizarCampanha(campanha: String) {
        personagem = personagem.copy(campanha = campanha)
    }

    fun atualizarPontosIniciais(pontos: Int) {
        personagem = personagem.copy(pontosIniciais = pontos.coerceIn(0, 1000))
    }

    fun atualizarLimiteDesvantagens(limite: Int) {
        personagem = personagem.copy(limiteDesvantagens = limite.coerceIn(-200, 0))
    }

    // === ATRIBUTOS PRIMARIOS ===

    fun atualizarForca(valor: Int) {
        personagem = personagem.copy(forca = valor.coerceIn(1, 30))
    }

    fun atualizarDestreza(valor: Int) {
        personagem = personagem.copy(destreza = valor.coerceIn(1, 30))
    }

    fun atualizarInteligencia(valor: Int) {
        personagem = personagem.copy(inteligencia = valor.coerceIn(1, 30))
    }

    fun atualizarVitalidade(valor: Int) {
        personagem = personagem.copy(vitalidade = valor.coerceIn(1, 30))
    }

    fun definirBasesAtributosPrimarios(
        forcaBase: Int,
        destrezaBase: Int,
        inteligenciaBase: Int,
        vitalidadeBase: Int
    ) {
        val novaForcaBase = forcaBase.coerceIn(1, 30)
        val novaDestrezaBase = destrezaBase.coerceIn(1, 30)
        val novaInteligenciaBase = inteligenciaBase.coerceIn(1, 30)
        val novaVitalidadeBase = vitalidadeBase.coerceIn(1, 30)

        personagem = personagem.copy(
            forcaBase = novaForcaBase,
            destrezaBase = novaDestrezaBase,
            inteligenciaBase = novaInteligenciaBase,
            vitalidadeBase = novaVitalidadeBase,
            forca = novaForcaBase,
            destreza = novaDestrezaBase,
            inteligencia = novaInteligenciaBase,
            vitalidade = novaVitalidadeBase
        )
    }

    // === MODIFICADORES SECUNDARIOS ===

    fun atualizarModPontosVida(valor: Int) {
        personagem = personagem.copy(modPontosVida = valor.coerceIn(-20, 20))
    }

    fun atualizarModVontade(valor: Int) {
        personagem = personagem.copy(modVontade = valor.coerceIn(-20, 20))
    }

    fun atualizarModPercepcao(valor: Int) {
        personagem = personagem.copy(modPercepcao = valor.coerceIn(-20, 20))
    }

    fun atualizarModPontosFadiga(valor: Int) {
        personagem = personagem.copy(modPontosFadiga = valor.coerceIn(-20, 20))
    }

    fun atualizarModVelocidadeBasica(valor: Float) {
        val valorNormalizado = CharacterRules
            .calcularPassosVelocidadeBasica(valor.coerceIn(-5f, 5f)) * 0.25f
        personagem = personagem.copy(modVelocidadeBasica = valorNormalizado)
    }

    fun atualizarModDeslocamentoBasico(valor: Int) {
        personagem = personagem.copy(modDeslocamentoBasico = valor.coerceIn(-10, 10))
    }

    fun atualizarPontosVidaRolagemAtual(valor: Int?) {
        val maxPvRolagem = (personagem.pontosVida.coerceAtLeast(0) * 5).coerceAtLeast(0)
        personagem = personagem.copy(
            pontosVidaRolagemAtual = valor?.coerceIn(0, maxPvRolagem)
        )
    }

    fun atualizarPontosFadigaRolagemAtual(valor: Int?) {
        personagem = personagem.copy(
            pontosFadigaRolagemAtual = valor?.coerceAtLeast(0)
        )
    }

    // === VANTAGENS ===

    fun adicionarVantagem(
        definicao: VantagemDefinicao,
        nivel: Int = 1,
        custoEscolhido: Int = 0,
        descricao: String = "",
        modificadores: List<ModificadorSelecao> = emptyList(),
        metadados: Map<String, String>? = null
    ) {
        val ehAcumulativa = definicao.tipoCusto == TipoCusto.POR_NIVEL
        val jaExiste = personagem.hasVantagem(definicao.id)

        // Se for única (nâo-acumulativa) e já existir (na raça ou na ficha), bloqueia duplicata
        if (jaExiste && !ehAcumulativa) {
            return 
        }

        // Se já existe EXATAMENTE a mesma vantagem com a mesma descrição na ficha comprada, evita duplicar
        if (personagem.vantagens.any { it.definicaoId == definicao.id && it.descricao == descricao }) {
            return
        }
        val nivelNormalizado = normalizarNivelVantagem(definicao.id, nivel)
        val vantagem = dataRepository.criarVantagemSelecionada(
            definicao,
            nivelNormalizado,
            custoEscolhido,
            descricao,
            modificadores,
            metadados
        )
        val lista = personagem.vantagens.toMutableList()
        lista.add(vantagem)
        atualizarVantagensComConfirmacao(lista)
    }

    fun removerVantagem(index: Int) {
        val lista = personagem.vantagens.toMutableList()
        if (index in lista.indices) {
            lista.removeAt(index)
            atualizarVantagensComConfirmacao(lista)
        }
    }

    fun atualizarVantagem(index: Int, vantagem: VantagemSelecionada) {
        val lista = personagem.vantagens.toMutableList()
        if (index in lista.indices) {
            val nivelNormalizado = normalizarNivelVantagem(vantagem.definicaoId, vantagem.nivel)
            lista[index] = vantagem.copy(nivel = nivelNormalizado)
            atualizarVantagensComConfirmacao(lista)
        }
    }

    fun adicionarModificadorAVantagem(index: Int, mod: ModificadorSelecao) {
        val lista = personagem.vantagens.toMutableList()
        if (index in lista.indices) {
            val vantagem = lista[index]
            val mods = vantagem.modificadores.toMutableList()
            mods.add(mod)
            lista[index] = vantagem.copy(modificadores = mods)
            atualizarVantagensComConfirmacao(lista)
        }
    }

    fun removerModificadorDeVantagem(vantagemIndex: Int, modificadorIndex: Int) {
        val lista = personagem.vantagens.toMutableList()
        if (vantagemIndex in lista.indices) {
            val vantagem = lista[vantagemIndex]
            val mods = vantagem.modificadores.toMutableList()
            if (modificadorIndex in mods.indices) {
                mods.removeAt(modificadorIndex)
                lista[vantagemIndex] = vantagem.copy(modificadores = mods)
                atualizarVantagensComConfirmacao(lista)
            }
        }
    }

    // === DESVANTAGENS ===

    fun adicionarDesvantagem(
        definicao: DesvantagemDefinicao,
        nivel: Int = 1,
        custoEscolhido: Int = 0,
        descricao: String = "",
        autocontrole: Int? = null,
        modificadores: List<ModificadorSelecao> = emptyList(),
        metadados: Map<String, String>? = null
    ) {
        val ehAcumulativa = definicao.tipoCusto == TipoCusto.POR_NIVEL
        val jaExiste = personagem.hasDesvantagem(definicao.id)
        
        if (jaExiste && !ehAcumulativa) {
            return 
        }

        if (personagem.desvantagens.any { it.definicaoId == definicao.id && it.descricao == descricao }) {
            return
        }
        val autocontroleNormalizado = if (definicao.usaAutocontroleMental()) autocontrole else null
        val desvantagem = dataRepository.criarDesvantagemSelecionada(
            definicao,
            nivel,
            custoEscolhido,
            descricao,
            autocontroleNormalizado,
            modificadores
        )
        val lista = personagem.desvantagens.toMutableList()
        lista.add(desvantagem)
        personagem = personagem.copy(desvantagens = lista)
    }

    fun removerDesvantagem(index: Int) {
        val lista = personagem.desvantagens.toMutableList()
        if (index in lista.indices) {
            lista.removeAt(index)
            personagem = personagem.copy(desvantagens = lista)
        }
    }

    fun atualizarDesvantagem(index: Int, desvantagem: DesvantagemSelecionada) {
        val lista = personagem.desvantagens.toMutableList()
        if (index in lista.indices) {
            val definicao = dataRepository.desvantagens.firstOrNull { it.id == desvantagem.definicaoId }
            val autocontroleNormalizado = if (definicao?.usaAutocontroleMental() == true) desvantagem.autocontrole else null
            lista[index] = desvantagem.copy(autocontrole = autocontroleNormalizado)
            personagem = personagem.copy(desvantagens = lista)
        }
    }

    fun adicionarModificadorADesvantagem(index: Int, mod: ModificadorSelecao) {
        val lista = personagem.desvantagens.toMutableList()
        if (index in lista.indices) {
            val desvantagem = lista[index]
            val mods = desvantagem.modificadores.toMutableList()
            mods.add(mod)
            lista[index] = desvantagem.copy(modificadores = mods)
            personagem = personagem.copy(desvantagens = lista)
        }
    }

    fun removerModificadorDeDesvantagem(desvantagemIndex: Int, modificadorIndex: Int) {
        val lista = personagem.desvantagens.toMutableList()
        if (desvantagemIndex in lista.indices) {
            val desvantagem = lista[desvantagemIndex]
            val mods = desvantagem.modificadores.toMutableList()
            if (modificadorIndex in mods.indices) {
                mods.removeAt(modificadorIndex)
                lista[desvantagemIndex] = desvantagem.copy(modificadores = mods)
                personagem = personagem.copy(desvantagens = lista)
            }
        }
    }

    // === PECULIARIDADES ===

    fun adicionarQualidade(qualidade: String) {
        if (personagem.qualidades.size >= 5) return // Maximo 5
        if (personagem.qualidades.contains(qualidade)) return // Duplicata
        val lista = personagem.qualidades.toMutableList()
        lista.add(qualidade)
        personagem = personagem.copy(qualidades = lista)
    }

    fun removerQualidade(index: Int) {
        val lista = personagem.qualidades.toMutableList()
        if (index in lista.indices) {
            lista.removeAt(index)
            personagem = personagem.copy(qualidades = lista)
        }
    }

    fun adicionarPeculiaridade(peculiaridade: String) {
        if (personagem.peculiaridades.size >= 5) return // Maximo 5
        if (personagem.peculiaridades.contains(peculiaridade)) return // Duplicata
        val lista = personagem.peculiaridades.toMutableList()
        lista.add(peculiaridade)
        personagem = personagem.copy(peculiaridades = lista)
    }

    fun removerPeculiaridade(index: Int) {
        val lista = personagem.peculiaridades.toMutableList()
        if (index in lista.indices) {
            lista.removeAt(index)
            personagem = personagem.copy(peculiaridades = lista)
        }
    }

    // === PERICIAS ===

    fun adicionarPericia(
        definicao: PericiaDefinicao,
        pontosGastos: Int = 1,
        especializacao: String = "",
        atributoEscolhido: AtributoBase? = null,
        dificuldadeEscolhida: Dificuldade? = null
    ): String? {
        // Verifica duplicatas (mesmo id e especializacao)
        if (personagem.pericias.any { it.definicaoId == definicao.id && it.especializacao == especializacao }) {
            return "Essa perícia já foi adicionada."
        }
        val erroPreRequisito = dataRepository.validarPreRequisitosPericia(definicao, personagem)
        if (erroPreRequisito != null) {
            return "Pré-requisito não atendido: $erroPreRequisito"
        }
        val pericia = dataRepository.criarPericiaSelecionada(definicao, pontosGastos, especializacao, atributoEscolhido, dificuldadeEscolhida)
        val lista = personagem.pericias.toMutableList()
        lista.add(pericia)
        personagem = personagem.copy(pericias = lista)
        return null
    }

    fun adicionarPericiaCustomizada(pericia: PericiaSelecionada) {
        val lista = personagem.pericias.toMutableList()
        lista.add(pericia)
        personagem = personagem.copy(pericias = lista)
    }

    fun removerPericia(index: Int) {
        val lista = personagem.pericias.toMutableList()
        if (index in lista.indices) {
            lista.removeAt(index)
            personagem = personagem.copy(pericias = lista)
        }
    }

    fun atualizarPericia(index: Int, pericia: PericiaSelecionada) {
        val lista = personagem.pericias.toMutableList()
        if (index in lista.indices) {
            lista[index] = pericia
            personagem = personagem.copy(pericias = lista)
        }
    }

    // === MAGIAS ===

    /**
     * Tenta adicionar uma magia; retorna mensagem de erro em caso de falha
     * (pré-requisito não atendido ou já existente).
     */
    fun adicionarMagia(
        definicao: MagiaDefinicao,
        pontosGastos: Int = 1,
        encantamentoAlvo: String? = null,
        especializacaoMagia: String? = null,
        ignorarPreRequisito: Boolean = false
    ): String? {
        if (!MagicEngine.permiteMultiplasInstanciasMagia(definicao.id) && personagem.magias.any { it.definicaoId == definicao.id }) {
            return "Magia já adicionada."
        }

        if (MagicEngine.permiteMultiplasInstanciasPorEscola(definicao.id)) {
            val escolaNorm = especializacaoMagia?.trim()?.lowercase()
            if (escolaNorm.isNullOrBlank()) return "Informe a escola da magia."
            val duplicadaEscola = personagem.magias.any {
                it.definicaoId == definicao.id &&
                    it.especializacaoMagia?.trim()?.equals(escolaNorm, ignoreCase = true) == true
            }
            if (duplicadaEscola) return "Esta magia já foi adicionada para essa escola."
        }

        if (definicao.id.equals("imunidade_a_encantamento", ignoreCase = true) &&
            encantamentoAlvo.isNullOrBlank()
        ) {
            return "Informe qual encantamento sera protegido."
        }

        if (!ignorarPreRequisito) {
            val erroEspecializacao = MagicEngine.validarEspecializacaoObrigatoria(definicao.id, especializacaoMagia)
            if (erroEspecializacao != null) return erroEspecializacao

            val erroRegraEspecial = MagicEngine.validarRegrasEspeciaisMagia(personagem, definicao, dataRepository, nivelAptidaoMagica)
            if (erroRegraEspecial != null) return erroRegraEspecial

            // Verificação de Uma Única Escola
            val aptidaoComEscola = personagem.vantagens.firstOrNull { 
                it.definicaoId.equals("aptidao_magica", ignoreCase = true) && 
                it.modificadores.any { m -> m.id == "mod_aptidao_escola" }
            }
            if (aptidaoComEscola != null) {
                val modEscola = aptidaoComEscola.modificadores.first { it.id == "mod_aptidao_escola" }
                val escolaPermitida = modEscola.descricao?.trim()?.lowercase()
                if (escolaPermitida != null) {
                    val ehEscolaPermitida = definicao.escola?.any { it.trim().lowercase() == escolaPermitida } == true
                    val ehRecuperarEnergia = definicao.nome.equals("Recuperar Energia", ignoreCase = true)
                    
                    if (!ehEscolaPermitida && !ehRecuperarEnergia) {
                        // Conforme GURPS p. 41, o personagem pode aprender, mas não recebe bônus de Aptidão.
                        // Iremos avisar. Retornar uma string aborta a adição na UI atual, mas vamos tratar como bônus.
                        // Se o usuário quiser impedir, podemos retornar erro. O usuário pediu "impedir... (ou aplica penalidades)".
                        // Vou aplicar penalidade de AM=0 automaticamente e avisar no log/UI.
                    }
                }
            }
        }
        val magia = dataRepository.criarMagiaSelecionada(
            definicao = definicao,
            pontosGastos = pontosGastos.coerceAtLeast(1),
            encantamentoAlvo = encantamentoAlvo,
            especializacaoMagia = especializacaoMagia
        )
        val lista = personagem.magias.toMutableList()
        lista.add(magia)
        personagem = personagem.copy(magias = lista)
        return null
    }

    /** Retorna mensagem de pré‑requisitos faltantes ou `null`. */
    fun prereqFailureForMagia(def: MagiaDefinicao): String? {
        return prereqFailureForMagiaUnificada(def)
    }

    private fun prereqFailureForMagiaUnificada(def: MagiaDefinicao): String? {
        val assinaturaAtual = assinaturaEstadoMagiasParaModoAlvo()
        if (assinaturaAtual != prereqCacheAssinatura) {
            prereqCacheAssinatura = assinaturaAtual
            prereqFailureCache.clear()
        }
        prereqFailureCache[def.id]?.let { return it }

        val regraEspecial = MagicEngine.validarRegrasEspeciaisMagia(personagem, def, dataRepository, nivelAptidaoMagica)
        if (regraEspecial != null) {
            prereqFailureCache[def.id] = regraEspecial
            return regraEspecial
        }

        val erroHierarquico = nexusArcanoModoAlvoAdapter.falhaPreRequisitoHierarquica(
            alvoId = def.id,
            magiasConhecidasIds = personagem.magias.asSequence().map { it.definicaoId }.toSet(),
            iq = personagem.inteligencia,
            dx = personagem.destreza,
            am = nivelAptidaoMagica
        )
        prereqFailureCache[def.id] = erroHierarquico
        return erroHierarquico
    }

    /** Indica se todos os pré‑requisitos da magia estão satisfeitos. */
    fun prereqsSatisfied(def: MagiaDefinicao): Boolean {
        return prereqFailureForMagia(def) == null
    }

    /** Mesmo contrato da validação principal, com cache compartilhado por estado. */
    fun prereqFailureForMagiaRapida(def: MagiaDefinicao): String? {
        return prereqFailureForMagiaUnificada(def)
    }

    /** Retorna ids de magias relacionadas ao alvo em ordem de progressão sugerida. */
    fun listaRelacionadosMagiaAlvo(alvo: MagiaDefinicao): List<String> {
        val snapshot = nexusArcanoModoAlvoAdapter.calcular(
            alvoId = alvo.id,
            magiasConhecidasIds = personagem.magias.asSequence().map { it.definicaoId }.toSet(),
            iq = personagem.inteligencia,
            dx = personagem.destreza,
            am = nivelAptidaoMagica
        )
        return snapshot.relacionadosIds
    }

    /** Compatibilidade com chamadas antigas. */
    fun idsRelacionadosMagiaAlvo(alvo: MagiaDefinicao): Set<String> {
        return listaRelacionadosMagiaAlvo(alvo).toSet()
    }

    fun assinaturaEstadoMagiasParaModoAlvo(): String {
        val ids = personagem.magias.asSequence()
            .map { it.definicaoId }
            .distinct()
            .sorted()
            .joinToString("|")
        return "$ids#am=$nivelAptidaoMagica#iq=${personagem.inteligencia}#dx=${personagem.destreza}"
    }

    fun requisitarModoAlvo(alvoId: String?, ativo: Boolean) {
        modoAlvoJob?.cancel()
        if (!ativo || alvoId.isNullOrBlank()) {
            modoAlvoRelacionadosIds = emptyList()
            modoAlvoCarregando = false
            modoAlvoErro = null
            modoAlvoAviso = null
            modoAlvoChavesAtivas = emptyList()
            modoAlvoChavesFaltantes = emptyList()
            modoAlvoProximasAcoes = emptyList()
            modoAlvoProximasAcoesIds = emptyList()
            modoAlvoProgressoCadeia = null
            modoAlvoProgressoEscolas = emptyList()
            modoAlvoProximaObrigatoria = null
            modoAlvoProximaLateralUtil = null
            modoAlvoBloqueioCurto = null
            modoAlvoUltimaChave = null
            return
        }

        val chave = "$alvoId|${assinaturaEstadoMagiasParaModoAlvo()}"
        if (chave == modoAlvoUltimaChave && modoAlvoRelacionadosIds.isNotEmpty()) return
        modoAlvoUltimaChave = chave
        modoAlvoErro = null
        modoAlvoAviso = null
        modoAlvoCarregando = true

        val magiasConhecidasSnapshot = personagem.magias.asSequence().map { it.definicaoId }.toSet()
        val iqSnapshot = personagem.inteligencia
        val dxSnapshot = personagem.destreza
        val amSnapshot = nivelAptidaoMagica
        modoAlvoJob = viewModelScope.launch(Dispatchers.Default) {
            try {
                val snapshot = nexusArcanoModoAlvoAdapter.calcular(
                    alvoId = alvoId,
                    magiasConhecidasIds = magiasConhecidasSnapshot,
                    iq = iqSnapshot,
                    dx = dxSnapshot,
                    am = amSnapshot
                )
                withContext(Dispatchers.Main) {
                    modoAlvoRelacionadosIds = snapshot.relacionadosIds
                    modoAlvoChavesAtivas = snapshot.chavesAtivas.map { it.descricao }
                    modoAlvoChavesFaltantes = snapshot.chavesFaltantes.map { it.descricao }
                    modoAlvoProximasAcoesIds = snapshot.proximasAcoesIds
                    modoAlvoProximasAcoes = snapshot.proximasAcoesIds.map { id ->
                        magiasByIdCache[id]?.nome ?: id
                    }
                    modoAlvoProgressoCadeia = snapshot.progressoCadeia
                    modoAlvoProgressoEscolas = snapshot.progressoEscolas
                    modoAlvoProximaObrigatoria = snapshot.proximaObrigatoriaId?.let { id ->
                        magiasByIdCache[id]?.nome ?: id
                    }
                    modoAlvoProximaLateralUtil = snapshot.proximaLateralUtilId?.let { id ->
                        magiasByIdCache[id]?.nome ?: id
                    }
                    modoAlvoBloqueioCurto = snapshot.bloqueioCurto
                    modoAlvoAviso = snapshot.aviso
                    modoAlvoCarregando = false
                }
            } catch (_: CancellationException) {
                // cancelamento esperado ao trocar alvo/estado
            } catch (t: Throwable) {
                withContext(Dispatchers.Main) {
                    modoAlvoErro = "Erro ao calcular modo alvo: ${t.message ?: "falha inesperada"}"
                    modoAlvoCarregando = false
                }
            }
        }
    }

    fun recalcularModoAlvoAgora(alvoId: String?) {
        if (alvoId.isNullOrBlank()) return
        modoAlvoUltimaChave = null
        requisitarModoAlvo(alvoId, ativo = true)
    }


    fun removerMagia(index: Int) {
        val lista = personagem.magias.toMutableList()
        if (index in lista.indices) {
            lista.removeAt(index)
            personagem = personagem.copy(magias = lista)
        }
    }

    fun atualizarMagia(index: Int, magia: MagiaSelecionada) {
        val lista = personagem.magias.toMutableList()
        if (index in lista.indices) {
            lista[index] = magia.copy(pontosGastos = magia.pontosGastos.coerceAtLeast(1))
            personagem = personagem.copy(magias = lista)
        }
    }

    // === TECNICAS ===

    fun adicionarTecnica(
        definicao: TecnicaCatalogoItem,
        periciaBase: PericiaSelecionada,
        nivelRelativoPredefinido: Int = 0
    ): String? {
        if (personagem.tecnicas.any { it.definicaoId == definicao.id && it.periciaBaseDefinicaoId == periciaBase.definicaoId && it.periciaBaseEspecializacao == periciaBase.especializacao }) {
            return "Esta técnica já foi adicionada para esta perícia base."
        }
        if (!tecnicaAtendePreRequisito(definicao, periciaBase)) {
            return "A perícia selecionada não atende o pré-requisito desta técnica."
        }
        val limiteMaximo = limiteMaximoTecnica(definicao)
        if (limiteMaximo != null && nivelRelativoPredefinido > limiteMaximo) {
            return "Esta técnica permite no máximo predefinido +$limiteMaximo."
        }
        val tecnica = dataRepository.criarTecnicaSelecionada(
            definicao = definicao,
            periciaBase = periciaBase,
            nivelRelativoPredefinido = nivelRelativoPredefinido
        )
        val lista = personagem.tecnicas.toMutableList()
        lista.add(tecnica)
        personagem = personagem.copy(tecnicas = lista)
        return null
    }

    fun removerTecnica(index: Int) {
        val lista = personagem.tecnicas.toMutableList()
        if (index in lista.indices) {
            lista.removeAt(index)
            personagem = personagem.copy(tecnicas = lista)
        }
    }

    fun atualizarTecnica(index: Int, tecnica: TecnicaSelecionada) {
        val lista = personagem.tecnicas.toMutableList()
        if (index in lista.indices) {
            val dificuldade = dataRepository.tecnicaDificuldade(tecnica.dificuldadeRaw)
            val nivelRelativo = tecnica.nivelRelativoPredefinido.coerceAtLeast(0)
            val custo = dataRepository.calcularCustoTecnica(dificuldade, nivelRelativo)
            lista[index] = tecnica.copy(
                pontosGastos = custo,
                nivelRelativoPredefinido = nivelRelativo
            )
            personagem = personagem.copy(tecnicas = lista)
        }
    }

    // === EQUIPAMENTOS ===

    fun adicionarEquipamento(equipamento: Equipamento) {
        val lista = personagem.equipamentos.toMutableList()
        lista.add(equipamento)
        personagem = personagem.copy(equipamentos = lista)
        ajustarEscudoSelecionadoAutomatico()
    }

    fun adicionarEquipamentoArma(arma: ArmaCatalogoItem) {
        val notasArma = buildString {
            if (!arma.aparar.isNullOrBlank()) {
                if (isNotBlank()) append("\n")
                append("Aparar: ${arma.aparar} (${explicarAparar(arma.aparar)})")
            }
            val observacoes = observacoesArmaFormatadas(arma)
            if (observacoes.isNotBlank()) {
                if (isNotBlank()) append("\n")
                append(observacoes)
            }
        }
        val equipamento = Equipamento(
            nome = arma.nome,
            peso = arma.pesoBaseKg ?: 0f,
            custo = arma.custoBase ?: 0f,
            quantidade = 1,
            notas = notasArma,
            tipo = if (arma.nome.contains("escudo", ignoreCase = true)) TipoEquipamento.ESCUDO else TipoEquipamento.ARMA,
            bonusDefesa = 0,
            armaCatalogoId = arma.id,
            armaTipoCombate = arma.tipoCombate,
            armaDanoRaw = arma.danoRaw,
            armaStMinimo = arma.stMinimo
        )
        adicionarEquipamento(equipamento)
    }

    private fun explicarAparar(valor: String): String {
        val v = valor.trim().uppercase()
        return when {
            v == "NÃO" || v == "NAO" -> "Nao pode aparar"
            v.endsWith("E") -> "Arma de esgrima"
            v.endsWith("D") -> "Arma desbalanceada"
            v == "0" -> "Sem modificador"
            v.startsWith("+") || v.startsWith("-") -> "Modificador na aparada"
            else -> "Valor de aparar"
        }
    }

    private fun observacoesArmaFormatadas(arma: ArmaCatalogoItem): String {
        if (arma.tipoCombate != "corpo_a_corpo" && arma.tipoCombate != "distancia" && arma.tipoCombate != "armas_de_fogo") {
            return ""
        }
        val refs = extrairReferenciasObservacoes(arma.observacoes)
        if (arma.tipoCombate == "armas_de_fogo") {
            val classe = classificarArmaDeFogo(arma)
            val linhas = mutableListOf<String>()
            if (classe == ClasseArmaFogo.ULTRATECH) {
                linhas.add("Todas as armas de feixe incluem sistemas eletronicos das armas inteligentes (pag. 278).")
            }
            val mapa = when (classe) {
                ClasseArmaFogo.PISTOLA_MM -> OBS_ARMA_FOGO_PISTOLA_MM
                ClasseArmaFogo.RIFLE_ESPINGARDA -> OBS_ARMA_FOGO_RIFLE
                ClasseArmaFogo.ULTRATECH -> OBS_ARMA_FOGO_ULTRATECH
                ClasseArmaFogo.PESADA -> OBS_ARMA_FOGO_PESADA
            }
            refs.mapNotNull { ref -> mapa[ref]?.let { "[$ref] $it" } }.forEach { linhas.add(it) }
            return linhas.joinToString("\n")
        }

        if (refs.isEmpty()) return ""
        val mapa = if (arma.tipoCombate == "distancia") OBS_ARMA_DISTANCIA else OBS_ARMA_CORPO_A_CORPO
        return refs.mapNotNull { ref -> mapa[ref]?.let { "[$ref] $it" } }.joinToString("\n")
    }

    private fun extrairReferenciasObservacoes(observacoes: String): List<Int> {
        if (observacoes.isBlank() || !observacoes.contains("[")) return emptyList()
        return Regex("\\d+")
            .findAll(observacoes)
            .mapNotNull { it.value.toIntOrNull() }
            .distinct()
            .toList()
    }

    private enum class ClasseArmaFogo {
        PISTOLA_MM,
        RIFLE_ESPINGARDA,
        ULTRATECH,
        PESADA
    }

    private fun classificarArmaDeFogo(arma: ArmaCatalogoItem): ClasseArmaFogo {
        val grupo = arma.grupo.lowercase()
        val nome = arma.nome.lowercase()
        if (
            grupo.contains("feixe") ||
            nome.contains("laser") ||
            nome.contains("eletrolaser") ||
            nome.contains("ionico") ||
            nome.contains("iônico")
        ) return ClasseArmaFogo.ULTRATECH

        if (
            grupo.contains("artilharia") ||
            grupo.contains("canhoneiro") ||
            grupo.contains("lancador") ||
            grupo.contains("lançador") ||
            grupo.contains("ala")
        ) return ClasseArmaFogo.PESADA

        if (grupo.contains("rifle")) return ClasseArmaFogo.RIFLE_ESPINGARDA

        return ClasseArmaFogo.PISTOLA_MM
    }

    private fun categoriaArmaFogoParaFiltro(arma: ArmaCatalogoItem): String {
        return when (classificarArmaDeFogo(arma)) {
            ClasseArmaFogo.PISTOLA_MM -> "pistolas_mm"
            ClasseArmaFogo.RIFLE_ESPINGARDA -> "rifles_espingardas"
            ClasseArmaFogo.ULTRATECH -> "ultratech"
            ClasseArmaFogo.PESADA -> "pesadas"
        }
    }

    fun observacoesArmaPorEquipamento(equipamento: Equipamento): String {
        val porId = equipamento.armaCatalogoId
            ?.takeIf { it.isNotBlank() }
            ?.let { id -> dataRepository.armasCatalogo.firstOrNull { it.id == id } }
        if (porId != null) return observacoesArmaFormatadas(porId)

        val nomeBase = equipamento.nome.substringBefore(" (").trim()
        if (nomeBase.isBlank()) return ""
        val nomeBaseNorm = normalizarChaveTexto(nomeBase)

        val porNome = dataRepository.armasCatalogo.firstOrNull { arma ->
            val tipoOk = equipamento.armaTipoCombate.isNullOrBlank() ||
                arma.tipoCombate.equals(equipamento.armaTipoCombate, ignoreCase = true)
            val danoOk = equipamento.armaDanoRaw.isNullOrBlank() ||
                arma.danoRaw.equals(equipamento.armaDanoRaw, ignoreCase = true)
            val nomeOk = normalizarChaveTexto(arma.nome) == nomeBaseNorm
            tipoOk && danoOk && nomeOk
        } ?: dataRepository.armasCatalogo.firstOrNull { arma ->
            normalizarChaveTexto(arma.nome) == nomeBaseNorm
        }

        return if (porNome != null) observacoesArmaFormatadas(porNome) else ""
    }

    private fun normalizarChaveTexto(valor: String): String {
        val semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return semAcento
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun adicionarEquipamentoEscudo(escudo: EscudoCatalogoItem) {
        val equipamento = Equipamento(
            nome = escudo.nome,
            peso = escudo.pesoKg ?: 0f,
            custo = escudo.custo ?: 0f,
            quantidade = 1,
            notas = escudo.observacoes,
            tipo = TipoEquipamento.ESCUDO,
            bonusDefesa = escudo.db
        )
        adicionarEquipamento(equipamento)
    }

    fun adicionarEquipamentoArmadura(armadura: ArmaduraCatalogoItem) {
        val componentesTexto = if (armadura.componentes.isEmpty()) {
            ""
        } else {
            armadura.componentes.joinToString(" | ") { c ->
                val custo = c.custoBase?.let { "$$it" } ?: "—"
                val peso = c.pesoKg?.let { "${it}kg" } ?: "—"
                "${c.local} RD ${c.rd} Custo $custo Peso $peso"
            }
        }
        val observacoes = montarObservacoesArmadura(armadura)
        val notas = buildString {
            append("Local: ${armadura.local}; RD: ${armadura.rd}")
            if (observacoes.isNotBlank()) append("\n$observacoes")
            if (componentesTexto.isNotBlank()) append("\nComponentes: $componentesTexto")
        }
        val equipamento = Equipamento(
            nome = armadura.nome,
            peso = armadura.pesoBaseKg ?: 0f,
            custo = armadura.custoBase ?: 0f,
            quantidade = 1,
            notas = notas,
            tipo = TipoEquipamento.ARMADURA,
            armaduraLocal = armadura.local,
            armaduraRd = armadura.rd
        )
        adicionarEquipamento(equipamento)
    }

    fun adicionarEquipamentoArmaduraComSelecao(armadura: ArmaduraCatalogoItem, locaisSelecionados: List<String>) {
        val selecionadosNorm = locaisSelecionados.map { it.trim() }.filter { it.isNotBlank() }
        val locaisFinais = if (selecionadosNorm.isEmpty()) listOf(armadura.local) else selecionadosNorm
        val custoBase = armadura.custoBase ?: 0f
        val pesoBase = armadura.pesoBaseKg ?: 0f
        val possuiComponentes = armadura.componentes.isNotEmpty()
        val divisor = locaisFinais.size.coerceAtLeast(1).toFloat()

        locaisFinais.forEach { localSel ->
            val componente = armadura.componentes.firstOrNull { it.local.equals(localSel, ignoreCase = true) }
            val custoLocal = when {
                componente?.custoBase != null -> componente.custoBase
                possuiComponentes -> custoBase
                else -> (custoBase / divisor)
            }
            val pesoLocal = when {
                componente?.pesoKg != null -> componente.pesoKg
                possuiComponentes -> pesoBase
                else -> (pesoBase / divisor)
            }
            val rdLocal = componente?.rd ?: armadura.rd
            val observacoes = montarObservacoesArmadura(armadura)
            val notas = buildString {
                append("Local: $localSel; RD: $rdLocal")
                if (observacoes.isNotBlank()) append("\n$observacoes")
            }
            val equipamento = Equipamento(
                nome = "${armadura.nome} ($localSel)",
                peso = pesoLocal,
                custo = custoLocal,
                quantidade = 1,
                notas = notas,
                tipo = TipoEquipamento.ARMADURA,
                armaduraLocal = localSel,
                armaduraRd = rdLocal
            )
            adicionarEquipamento(equipamento)
        }
    }

    private fun montarObservacoesArmadura(armadura: ArmaduraCatalogoItem): String {
        val refs = Regex("\\[(\\d+)]")
            .findAll(armadura.observacoes)
            .mapNotNull { it.groupValues.getOrNull(1)?.toIntOrNull() }
            .toList()
        var detalhes = armadura.observacoesDetalhadas
            .map { it.trim() }
            .filter { it.isNotBlank() }
        if (detalhes.isEmpty()) return ""

        val linhas = mutableListOf<String>()
        val primeira = detalhes.firstOrNull()
        if (primeira != null && primeira.contains("NT7+", ignoreCase = true)) {
            linhas.add(primeira)
            detalhes = detalhes.drop(1)
        }

        if (refs.isEmpty()) {
            linhas.addAll(detalhes)
            return linhas.joinToString("\n")
        }

        refs.zip(detalhes).forEach { (ref, texto) ->
            linhas.add("[$ref] $texto")
        }
        if (detalhes.size > refs.size) {
            linhas.addAll(detalhes.drop(refs.size))
        }
        return linhas.joinToString("\n")
    }

    fun removerEquipamento(index: Int) {
        val lista = personagem.equipamentos.toMutableList()
        if (index in lista.indices) {
            lista.removeAt(index)
            personagem = personagem.copy(equipamentos = lista)
            ajustarEscudoSelecionadoAutomatico()
        }
    }

    fun atualizarEquipamento(index: Int, equipamento: Equipamento) {
        val lista = personagem.equipamentos.toMutableList()
        if (index in lista.indices) {
            lista[index] = equipamento
            personagem = personagem.copy(equipamentos = lista)
            ajustarEscudoSelecionadoAutomatico()
        }
    }

    // === DESCRICAO ===

    fun atualizarAparencia(aparencia: String) {
        personagem = personagem.copy(aparencia = aparencia)
    }

    fun atualizarHistorico(historico: String) {
        personagem = personagem.copy(historico = historico)
    }

    fun atualizarNotas(notas: String) {
        personagem = personagem.copy(notas = notas)
    }

    // === PERSISTENCIA ===

    fun salvarFicha(nomeArquivo: String = personagem.nome.ifBlank { "Sem_Nome" }) {
        viewModelScope.launch {
            fichaStorage.salvarFicha(nomeArquivo, personagem.toJson())
            carregarListaFichas()
        }
    }

    fun atualizarModeloRacial(novoModelo: ModeloRacial) {
        // Se a descrição da raça não estiver vazia, sincroniza com a aparência do personagem
        val novaAparencia = if (novoModelo.descricao.isNotBlank()) {
            if (personagem.aparencia.isBlank()) {
                novoModelo.descricao
            } else {
                personagem.aparencia // Mantém o que o usuário já escreveu se não estiver vazio
            }
        } else {
            personagem.aparencia
        }
        
        personagem = personagem.copy(
            modeloRacial = novoModelo,
            aparencia = novaAparencia
        )
        salvarFicha()
    }

    fun carregarFicha(nomeArquivo: String) {
        viewModelScope.launch {
            val json = fichaStorage.carregarFicha(nomeArquivo)
            if (json != null) {
                personagem = Personagem.fromJson(json)
            }
        }
    }

    fun excluirFicha(nomeArquivo: String) {
        viewModelScope.launch {
            fichaStorage.excluirFicha(nomeArquivo)
            carregarListaFichas()
        }
    }

    fun novaFicha() {
        personagem = Personagem()
        personagemPendenteLimpezaMagias = null
        mostrarConfirmacaoLimpezaMagias = false
    }

    fun exportarFichaJsonCompativel(): String {
        return personagem.toJson()
    }

    fun exportarFichaJsonVersionada(): String {
        return PersonagemInterop.exportarJson(
            personagem = personagem,
            appVersion = BuildConfig.VERSION_NAME,
            uiVariant = BuildConfig.UI_VARIANT
        )
    }

    fun importarFichaJson(json: String): String? {
        return try {
            val resultado = PersonagemInterop.importarJson(json)
            personagem = resultado.personagem
            personagemPendenteLimpezaMagias = null
            mostrarConfirmacaoLimpezaMagias = false
            resultado.aviso?.let { "Ficha importada com sucesso. $it" }
        } catch (_: UnsupportedOperationException) {
            "Versao de arquivo nao suportada por esta versao do app."
        } catch (_: Exception) {
            "Arquivo de ficha invalido ou corrompido."
        }
    }

    private suspend fun carregarListaFichas() {
        fichasSalvas = fichaStorage
            .listarFichas()
            .filterNot { it == autoSaveRecuperacaoNome }
    }

    private suspend fun restaurarAutoSaveSeExistir() {
        val json = fichaStorage.carregarFicha(autoSaveRecuperacaoNome) ?: return
        runCatching {
            personagem = Personagem.fromJson(json)
        }
    }

    // === UTILITARIOS ===

    val pesoTotal: Float get() = personagem.pesoTotal

    val custoTotalEquipamentos: Float get() = personagem.equipamentos.sumOf {
        (it.custo * it.quantidade).toDouble()
    }.toFloat()

    fun calcularDanoArmaComSt(danoRaw: String?): String {
        val raw = danoRaw?.trim().orEmpty()
        if (raw.isBlank()) return ""
        return CharacterRules.resolverDanoPorSt(raw, personagem.forca)
    }

    suspend fun enviarRolagemDiscord(payload: DiscordRollPayload): RollDispatchStatus {
        return withContext(Dispatchers.IO) {
            val primeiraTentativa = DiscordRollApiClient.postRoll(
                baseUrl = BuildConfig.DISCORD_ROLL_API_BASE_URL,
                apiKey = BuildConfig.DISCORD_ROLL_API_KEY,
                payload = payload
            )
            if (primeiraTentativa.ok) {
                RollDispatchStatus(enviado = true)
            } else {
                // Retentativa unica somente para falha de rede/timeout (sem resposta HTTP)
                val precisaRetentativaRede = RollDispatchPolicy.deveRetentar(primeiraTentativa.statusCode)
                val resultadoFinal = if (precisaRetentativaRede) {
                    DiscordRollApiClient.postRoll(
                        baseUrl = BuildConfig.DISCORD_ROLL_API_BASE_URL,
                        apiKey = BuildConfig.DISCORD_ROLL_API_KEY,
                        payload = payload
                    )
                } else {
                    primeiraTentativa
                }

                if (resultadoFinal.ok) {
                    RollDispatchStatus(enviado = true)
                } else {
                    RollDispatchStatus(
                        enviado = false,
                        detalhe = RollDispatchPolicy.mensagemErro(
                            statusCode = resultadoFinal.statusCode,
                            erroBruto = resultadoFinal.error
                        )
                    )
                }
            }
        }
    }

    fun atualizarCanaisDiscord() {
        viewModelScope.launch {
            canaisDiscordCarregando = true
            canaisDiscordErro = null
            val resultado = withContext(Dispatchers.IO) {
                DiscordRollApiClient.fetchVoiceChannels(
                    baseUrl = BuildConfig.DISCORD_ROLL_API_BASE_URL,
                    apiKey = BuildConfig.DISCORD_ROLL_API_KEY
                )
            }
            canaisDiscordCarregando = false

            if (!resultado.ok) {
                canaisDiscordErro = resultado.error ?: "erro_ao_carregar_canais"
                return@launch
            }

            canaisDiscord = resultado.channels
            val selecionadoAtual = canalDiscordSelecionadoId
            if (!selecionadoAtual.isNullOrBlank()) {
                val canal = resultado.channels.firstOrNull { it.id == selecionadoAtual }
                if (canal != null) {
                    canalDiscordSelecionadoNome = "${canal.guildName} / ${canal.name}"
                    configPrefs.edit()
                        .putString(prefCanalDiscordNome, canalDiscordSelecionadoNome)
                        .apply()
                }
            }
        }
    }

    fun selecionarCanalDiscord(canal: DiscordVoiceChannel?) {
        canalDiscordSelecionadoId = canal?.id
        canalDiscordSelecionadoNome = canal?.let { "${it.guildName} / ${it.name}" }
        configPrefs.edit()
            .putString(prefCanalDiscordId, canalDiscordSelecionadoId)
            .putString(prefCanalDiscordNome, canalDiscordSelecionadoNome)
            .apply()
    }

    fun deveMostrarManualModoAlvo(): Boolean {
        return !configPrefs.getBoolean(prefNaoMostrarManualModoAlvo, false)
    }

    fun definirNaoMostrarManualModoAlvo(naoMostrar: Boolean) {
        configPrefs.edit()
            .putBoolean(prefNaoMostrarManualModoAlvo, naoMostrar)
            .apply()
    }

    val nivelCarga: Int get() = personagem.nivelCarga

    val deslocamentoAtual: Int get() = personagem.deslocamentoAtual

    val esquivaAtual: Int get() = (personagem.esquiva - personagem.nivelCarga).coerceAtLeast(1)

    // === VALIDACAO ===

    val desvantagensExcedemLimite: Boolean
        get() = personagem.desvantagensExcedemLimite

    val pontosDesvantagens: Int
        get() = personagem.pontosDesvantagens

    val limiteDesvantagens: Int
        get() = personagem.limiteDesvantagens

    fun vantagemJaAdicionada(id: String, descricao: String = ""): Boolean {
        return personagem.vantagens.any { it.definicaoId == id && it.descricao == descricao }
    }

    fun desvantagemJaAdicionada(id: String, descricao: String = ""): Boolean {
        return personagem.desvantagens.any { it.definicaoId == id && it.descricao == descricao }
    }

    fun periciaJaAdicionada(id: String, especializacao: String = ""): Boolean {
        return personagem.pericias.any { it.definicaoId == id && it.especializacao == especializacao }
    }

    fun magiaJaAdicionada(id: String): Boolean {
        return personagem.magias.any { it.definicaoId == id }
    }

    fun tecnicaJaAdicionada(id: String, periciaBaseDefinicaoId: String? = null, periciaBaseEspecializacao: String = ""): Boolean {
        return personagem.tecnicas.any { tecnica ->
            if (tecnica.definicaoId != id) {
                false
            } else if (periciaBaseDefinicaoId.isNullOrBlank()) {
                true
            } else {
                tecnica.periciaBaseDefinicaoId == periciaBaseDefinicaoId &&
                    tecnica.periciaBaseEspecializacao.equals(periciaBaseEspecializacao, ignoreCase = true)
            }
        }
    }

    fun custoTecnica(definicao: TecnicaCatalogoItem, nivelRelativoPredefinido: Int): Int {
        val dificuldade = dataRepository.tecnicaDificuldade(definicao.dificuldadeRaw)
        return dataRepository.calcularCustoTecnica(dificuldade, nivelRelativoPredefinido.coerceAtLeast(0))
    }

    fun limiteMaximoTecnica(definicao: TecnicaCatalogoItem): Int? {
        return SkillEngine.getRegraPerfilTecnica(definicao, dataRepository).limiteRelativo
    }

    fun preRequisitoExibicaoTecnica(definicao: TecnicaCatalogoItem): String {
        return SkillEngine.getRegraPerfilTecnica(definicao, dataRepository).preRequisitoExibicao
    }

    fun calcularNivelTecnicaPreview(
        definicao: TecnicaCatalogoItem,
        periciaBase: PericiaSelecionada,
        nivelRelativoPredefinido: Int
    ): Int? {
        val tecnica = dataRepository.criarTecnicaSelecionada(
            definicao = definicao,
            periciaBase = periciaBase,
            nivelRelativoPredefinido = nivelRelativoPredefinido
        )
        return tecnica.calcularNivel(personagem)
    }

    fun tecnicaAtendePreRequisito(definicao: TecnicaCatalogoItem, periciaBase: PericiaSelecionada): Boolean {
        val prerequisitoRaw = definicao.preRequisitoRaw
        val prerequisito = normalizarTexto(prerequisitoRaw)
        if (prerequisito.isBlank() || prerequisito == "-") return true

        // Guarda de coerencia por familia de tecnica (catalogo Artes Marciais/Gun Fu).
        // Evita aceitar pericias de tiro em requisitos de arma corpo a corpo e vice-versa.
        if (!periciaCompativelComFamilia(prerequisito, periciaBase)) return false

        val ancoraPericia = extrairAncoraPericiaNoLimite(prerequisito)
        if (!ancoraPericia.isNullOrBlank()) {
            val matchAncora = SkillEngine.periciaCorrespondeTermo(periciaBase, ancoraPericia, tecnicasNomesNormalizados)
            if (matchAncora != null) return matchAncora
        }

        val blocoPrincipal = normalizarTexto(prerequisitoRaw.substringBefore(";"))
        val termos = blocoPrincipal
            .replace(" ou ", ",")
            .replace(" e ", ",")
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
        if (termos.isEmpty()) return true

        val avaliacao = termos
            .mapNotNull { termo -> SkillEngine.periciaCorrespondeTermo(periciaBase, termo, tecnicasNomesNormalizados) }
        if (avaliacao.isNotEmpty()) {
            return avaliacao.any { it }
        }
        return true
    }

    private fun periciaCompativelComFamilia(
        prerequisitoNormalizado: String,
        periciaBase: PericiaSelecionada
    ): Boolean {
        val exigeTiro =
            prerequisitoNormalizado.contains("pericia de tiro") ||
                prerequisitoNormalizado.contains("qualquer pericia de tiro") ||
                prerequisitoNormalizado.contains("arma de longo alcance")
        if (exigeTiro && !SkillEngine.periciaEhTiro(periciaBase)) return false

        val exigeEsgrima = prerequisitoNormalizado.contains("arma de esgrima")
        if (exigeEsgrima && !SkillEngine.periciaEhArmaEsgrima(periciaBase)) return false

        val exigeDefesaAtiva =
            prerequisitoNormalizado.contains("defesa ativa") ||
                prerequisitoNormalizado.contains("bloquear ou aparar")
        if (exigeDefesaAtiva && !SkillEngine.periciaEhDefesaAtiva(periciaBase)) return false

        val exigeCorpoACorpo =
            prerequisitoNormalizado.contains("arma corpo a corpo") ||
                prerequisitoNormalizado.contains("arma de combate corpo a corpo") ||
                prerequisitoNormalizado.contains("ataque corpo a corpo") ||
                (
                    (prerequisitoNormalizado.contains("pericia com arma apropriada") ||
                        prerequisitoNormalizado.contains("pericia de arma apropriada") ||
                        prerequisitoNormalizado.contains("arma apropriada")) &&
                        !prerequisitoNormalizado.contains("tiro") &&
                        !prerequisitoNormalizado.contains("longo alcance") &&
                        !prerequisitoNormalizado.contains("arma de fogo") &&
                        !prerequisitoNormalizado.contains("armas de fogo") &&
                        !prerequisitoNormalizado.contains("arma de esgrima")
                    )
        if (exigeCorpoACorpo) {
            val permiteDesarmado =
                prerequisitoNormalizado.contains("desarmado") ||
                    prerequisitoNormalizado.contains("judo") ||
                    prerequisitoNormalizado.contains("luta greco romana") ||
                    prerequisitoNormalizado.contains("carate") ||
                    prerequisitoNormalizado.contains("briga") ||
                    prerequisitoNormalizado.contains("boxe")
            val ok =
                SkillEngine.periciaEhCorpoACorpo(periciaBase) ||
                    (permiteDesarmado && SkillEngine.periciaEhDesarmado(periciaBase))
            if (!ok) return false
        }

        return true
    }

    val nivelAptidaoMagica: Int
        get() = nivelAptidaoMagicaParaMagia(null)

    fun nivelAptidaoMagicaParaMagia(magia: MagiaDefinicao?): Int {
        return MagicEngine.getNivelAptidaoMagicaParaMagia(personagem, magia)
    }

    val temAptidaoMagica: Boolean
        get() = personagem.hasVantagem("aptidao_magica")

    val nivelAptidaoAstral: Int
        get() {
            val pAptidoes = personagem.vantagens.filter { it.definicaoId.equals("aptidao_astral", ignoreCase = true) }
            val rAptidoes = personagem.modeloRacial.vantagens.filter { it.definicaoId.equals("aptidao_astral", ignoreCase = true) }
            val todas = pAptidoes + rAptidoes
            return todas.sumOf { (it.nivel - 1).coerceAtLeast(0) }
        }

    val temAptidaoAstral: Boolean
        get() = personagem.hasVantagem("aptidao_astral")

    // === COMBATE - DEFESAS ATIVAS ===

    fun atualizarBonusManualEsquiva(bonus: Int) {
        val defesas = personagem.defesasAtivas.copy(bonusManualEsquiva = bonus.coerceIn(-20, 20))
        personagem = personagem.copy(defesasAtivas = defesas)
    }

    fun atualizarPericiaApara(periciaId: String?) {
        val defesas = personagem.defesasAtivas.copy(periciaAparaId = periciaId)
        personagem = personagem.copy(defesasAtivas = defesas)
    }

    fun atualizarBonusManualApara(bonus: Int) {
        val defesas = personagem.defesasAtivas.copy(bonusManualApara = bonus.coerceIn(-20, 20))
        personagem = personagem.copy(defesasAtivas = defesas)
    }

    fun atualizarPericiaBloqueio(periciaId: String?) {
        val defesas = personagem.defesasAtivas.copy(periciaBloqueioId = periciaId)
        personagem = personagem.copy(defesasAtivas = defesas)
        ajustarEscudoSelecionadoAutomatico()
    }

    fun atualizarEscudoBloqueio(escudoNome: String?) {
        val defesas = personagem.defesasAtivas.copy(escudoSelecionadoNome = escudoNome)
        personagem = personagem.copy(defesasAtivas = defesas)
    }

    fun atualizarBonusManualBloqueio(bonus: Int) {
        val defesas = personagem.defesasAtivas.copy(bonusManualBloqueio = bonus.coerceIn(-20, 20))
        personagem = personagem.copy(defesasAtivas = defesas)
    }

    // Valores calculados de defesas
    val esquivaCalculada: Int get() = personagem.defesasAtivas.calcularEsquiva(personagem)
    val aparaCalculada: Int? get() = personagem.defesasAtivas.calcularApara(personagem)
    val bloqueioCalculado: Int? get() = personagem.defesasAtivas.calcularBloqueio(personagem)

    // Estado derivado para a UI
    val defesasAtivasVisiveis: List<ActiveDefense> get() {
        val lista = mutableListOf<ActiveDefense>()
        
        // Esquiva
        lista.add(ActiveDefense(
            type = DefenseType.ESQUIVA,
            name = "Esquiva",
            baseValue = personagem.defesasAtivas.getEsquivaBase(personagem),
            bonus = personagem.defesasAtivas.bonusManualEsquiva,
            finalValue = esquivaCalculada
        ))
        
        // Apara
        aparaCalculada?.let { finalVal ->
            personagem.defesasAtivas.getAparaBase(personagem)?.let { baseVal ->
                lista.add(ActiveDefense(
                    type = DefenseType.APARA,
                    name = "Apara",
                    baseValue = baseVal,
                    bonus = personagem.defesasAtivas.bonusManualApara,
                    finalValue = finalVal
                ))
            }
        }
        
        // Bloqueio
        bloqueioCalculado?.let { finalVal ->
            personagem.defesasAtivas.getBloqueioBase(personagem)?.let { baseVal ->
                val db = personagem.defesasAtivas.getBonusEscudo(personagem)
                lista.add(ActiveDefense(
                    type = DefenseType.BLOQUEIO,
                    name = "Bloqueio",
                    baseValue = baseVal + db,
                    bonus = personagem.defesasAtivas.bonusManualBloqueio,
                    finalValue = finalVal
                ))
            }
        }
        return lista
    }

    fun atualizarBonusDefesa(type: DefenseType, bonus: Int) {
        when(type) {
            DefenseType.ESQUIVA -> atualizarBonusManualEsquiva(bonus)
            DefenseType.APARA -> atualizarBonusManualApara(bonus)
            DefenseType.BLOQUEIO -> atualizarBonusManualBloqueio(bonus)
        }
    }

    // Perícias para o dropdown de Apara (Combate exceto Escudo)
    val periciasParaApara: List<PericiaSelecionada> get() {
        return personagem.pericias.filter { pericia ->
            val idNormalizado = pericia.definicaoId.trim().lowercase()
            PERICIAS_COMBATE.contains(idNormalizado) && idNormalizado != "escudo"
        }
    }

    // Perícias para o dropdown de Bloqueio (Somente Escudo)
    val periciasParaBloqueio: List<PericiaSelecionada> get() {
        return personagem.pericias.filter { pericia ->
            pericia.definicaoId.trim().equals("escudo", ignoreCase = true)
        }
    }
    fun confirmarLimpezaMagiasAoPerderAptidao() {
        val pendente = personagemPendenteLimpezaMagias ?: return
        personagem = pendente.copy(magias = emptyList())
        personagemPendenteLimpezaMagias = null
        mostrarConfirmacaoLimpezaMagias = false
    }

    fun cancelarLimpezaMagiasAoPerderAptidao() {
        personagemPendenteLimpezaMagias = null
        mostrarConfirmacaoLimpezaMagias = false
    }

    private fun ajustarEscudoSelecionadoAutomatico() {
        if (personagem.defesasAtivas.periciaBloqueioId.isNullOrBlank()) return
        val escudos = escudosEquipados
        if (escudos.isEmpty()) {
            val def = personagem.defesasAtivas.copy(escudoSelecionadoNome = null)
            personagem = personagem.copy(defesasAtivas = def)
            return
        }
        val atual = personagem.defesasAtivas.escudoSelecionadoNome
        val existeAtual = atual?.let { nomeSel ->
            escudos.any { it.nome.equals(nomeSel.trim(), ignoreCase = true) }
        } == true
        if (existeAtual) return
        val melhor = escudos.maxByOrNull { it.bonusDefesa }?.nome ?: escudos.first().nome
        val def = personagem.defesasAtivas.copy(escudoSelecionadoNome = melhor)
        personagem = personagem.copy(defesasAtivas = def)
    }

    private fun atualizarVantagensComConfirmacao(vantagensAtualizadas: List<VantagemSelecionada>) {
        if (deveConfirmarLimpezaMagias(vantagensAtualizadas)) {
            personagemPendenteLimpezaMagias = personagem.copy(vantagens = vantagensAtualizadas)
            mostrarConfirmacaoLimpezaMagias = true
            return
        }
        personagem = personagem.copy(vantagens = vantagensAtualizadas)
    }

    private fun deveConfirmarLimpezaMagias(vantagensAtualizadas: List<VantagemSelecionada>): Boolean {
        if (personagem.magias.isEmpty()) return false
        val possuiAptidaoAposMudanca = vantagensAtualizadas
            .any { it.definicaoId.equals("aptidao_magica", ignoreCase = true) }
        return !possuiAptidaoAposMudanca
    }

    private fun normalizarNivelVantagem(definicaoId: String, nivel: Int): Int {
        if (definicaoId.equals("aptidao_astral", ignoreCase = true)) {
            return nivel.coerceIn(1, 4)
        }
        return nivel.coerceAtLeast(1)
    }

    private fun normalizarTexto(valor: String): String {
        val semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return semAcento
            .lowercase()
            .replace("-", " ")
            .replace(Regex("[^a-z0-9\\s/+_-]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun extrairAncoraPericiaNoLimite(prerequisitoNormalizado: String): String? {
        val trechoLimite = prerequisitoNormalizado.substringAfter("nao pode exceder", "")
        if (trechoLimite.isBlank()) return null
        val semPrefixo = trechoLimite
            .trim()
            .replace(Regex("^(o|a|os|as)\\s+"), "")
            .replace(Regex("^nivel\\s+de\\s+"), "")
            .replace(Regex("^nivel\\s+da\\s+"), "")
            .replace(Regex("^nivel\\s+do\\s+"), "")
            .replace(Regex("^pericias?\\s+"), "")
            .trim()
        val candidata = semPrefixo
            .substringBefore(" +")
            .substringBefore(" -")
            .substringBefore(" baseada")
            .substringBefore(" ou ")
            .trim()
        if (candidata.isBlank()) return null
        val candidataSemBonus = candidata
            .replace(Regex("[+-]\\d+.*$"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
        val termosGenericos = listOf(
            "pre requisito",
            "pre requisito aparar",
            "pre requisito bloquear",
            "pericia pre requisito",
            "pericia de tiro",
            "pericia com arma",
            "defesa ativa",
            "bloquear",
            "aparar",
            "st",
            "dx",
            "ht",
            "iq",
            "per"
        )
        if (termosGenericos.any { termo ->
                candidata == termo ||
                    candidata.startsWith("$termo ") ||
                    candidataSemBonus == termo ||
                    candidataSemBonus.startsWith("$termo ")
            }) {
            return null
        }
        return candidataSemBonus.ifBlank { candidata }
    }


    // === MESTRE IA 2.0 ===
    var mestreIAChatHistory by mutableStateOf<List<MestreIAClient.ChatMessage>>(emptyList())
        private set
    
    private val mestreIAUseCase by lazy { MestreIAUseCase(this, dataRepository) }

    fun limparChatMestreIA() {
        mestreIAChatHistory = emptyList()
    }

    private fun getCatalogNames() = MestreIAClient.CatalogoNomes(
        vantagens = dataRepository.vantagens.map { it.nome },
        desvantagens = dataRepository.desvantagens.map { it.nome },
        pericias = dataRepository.pericias.map { it.nome },
        magias = dataRepository.magias.map { it.nome }
    )

    fun conversarComMestreIA(pergunta: String, modo: String = "conversa", onResult: (Boolean, String) -> Unit) {
        val baseUrl = configPrefs.getString(prefIABaseUrl, iaBaseUrl) ?: iaBaseUrl
        val apiKey = configPrefs.getString(prefIAApiKey, iaApiKey) ?: iaApiKey
        val workspaceSlug = configPrefs.getString(prefIAWorkspaceSlug, iaWorkspaceSlug) ?: iaWorkspaceSlug

        // Adiciona pergunta do usuário ao chat local
        mestreIAChatHistory = mestreIAChatHistory + MestreIAClient.ChatMessage("user", pergunta)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val contexto = if (modo != "geracao") {
                    "Ficha de ${personagem.nome}: ${personagem.toJson()}"
                } else null

                val respostaTexto = MestreIAClient.perguntarAoMestre(
                    baseUrl = baseUrl,
                    apiKey = apiKey,
                    workspaceSlug = workspaceSlug,
                    prompt = pergunta,
                    history = mestreIAChatHistory.dropLast(1), // Histórico real (sem a última pergunta)
                    contextoPersonagem = contexto,
                    catalogo = getCatalogNames(),
                    modo = modo
                )

                withContext(Dispatchers.Main) {
                    if (respostaTexto != null) {
                        // Adiciona resposta da IA ao chat
                        mestreIAChatHistory = mestreIAChatHistory + MestreIAClient.ChatMessage("model", respostaTexto)
                        
                        // Se for modo geração, tenta extrair JSON e aplicar
                        if (modo == "geracao") {
                            val jsonExtraido = MestreIAClient.extrairJsonFicha(respostaTexto)
                            if (jsonExtraido != null) {
                                mestreIAUseCase.integrarRespostaNaFicha(jsonExtraido)
                                autoSaveIA()
                                onResult(true, "Ficha gerada com sucesso!")
                            } else {
                                onResult(true, "Ficha gerada, mas houve erro no formato JSON. Veja a resposta.")
                            }
                        } else {
                            onResult(true, "Resposta recebida.")
                        }
                    } else {
                        onResult(false, "Mestre Digital está offline ou houve falha na conexão.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, "Erro: ${e.message}")
                }
            }
        }
    }

    fun salvarConfiguracaoIA(baseUrl: String, apiKey: String, workspaceSlug: String) {
        iaBaseUrl = baseUrl.trim()
        iaApiKey = apiKey.trim()
        iaWorkspaceSlug = workspaceSlug.trim()
        configPrefs.edit().apply {
            putString(prefIABaseUrl, iaBaseUrl)
            putString(prefIAApiKey, iaApiKey)
            putString(prefIAWorkspaceSlug, iaWorkspaceSlug)
            apply()
        }
    }

    private fun autoSaveIA() {
        viewModelScope.launch {
            val nomePersonagem = personagem.nome.ifBlank { "Sem_Nome" }
            val timestamp = java.text.SimpleDateFormat("dd-MM_HH-mm", java.util.Locale.getDefault()).format(java.util.Date())
            val nomeAutoSave = "IA_${nomePersonagem}_$timestamp"
            fichaStorage.salvarFicha(nomeAutoSave, personagem.toJson())
            carregarListaFichas()
        }
    }

    fun gerarFichaComIA(historia: String, onResult: (Boolean, String) -> Unit) {
        conversarComMestreIA(historia, "geracao", onResult)
    }

    fun analisarFichaComIA(onResult: (Boolean, String) -> Unit) {
        conversarComMestreIA("Analise minha ficha atual e dê sugestões.", "analise", onResult)
    }

    companion object {
        private val OBS_ARMA_CORPO_A_CORPO = mapOf(
            1 to "Pode ser de arremesso. Veja Tabela de Armas Motoras de Combate a Distancia (pag. 275).",
            2 to "Pode ficar presa; veja Picaretas (pag. 406).",
            3 to "Briga aumenta dano sem armas; Garras e Carate aumentam dano de socos/chutes; Boxe aumenta dano por soco.",
            4 to "Se fracassar em um chute, precisa passar em teste de DX para nao cair.",
            5 to "Em fracasso de HT, a vitima fica atordoada enquanto houver contato e por mais (20-HT) segundos. Depois testa HT-3 para recuperar.",
            6 to "Aparar manguais sofre -4 e armas de esgrima (E) nao apararam. Bloquear manguais sofre -2. No nunchaku, redutores pela metade.",
            7 to "Lamina de energia. Exige manobra Preparar para ativar/desativar. Lamina inquebravel e danifica armas/corpo ao aparar/bloquear. Celula extra: $100, 0,25 kg, 300s.",
            8 to "Corda para estrangular; veja Garrote (pag. 406).",
            9 to "Dano maior quando usada montado; veja Armas de Cavalaria (pag. 397).",
            10 to "O cabo da arma pode ser usado como soco ingles em combate corporal.",
            11 to "Muito barulhento. Funciona 2 horas com 2,5 l de gasolina.",
            12 to "Especifique alcance maximo (ate 7 m) na compra. Custo/peso por metro. ST 5 +1 por metro. Veja Chicotes (pag. 405)."
        )

        private val OBS_ARMA_DISTANCIA = mapOf(
            1 to "Ataque de acompanhamento para dopar/envenenar se o dano ultrapassar a RD. Efeito depende do veneno (pag. 437).",
            2 to "Exige duas maos para preparar, mas apenas uma para atacar.",
            3 to "Municao: flecha/virote $2; dardo/bolinha $0,1; pedra de funda e gratuita.",
            4 to "Pode enredar ou apanhar o alvo (pag. 410).",
            5 to "Sarilho/pole para recarregar besta de ST alta. Permite recarregar arma com ST ate +4 da sua em 20 manobras Preparar.",
            6 to "Rede nao tem 1/2D. Distancia Max: (ST/2 + NH/5) rede grande; (ST + NH/5) rede de combate.",
            7 to "Pode disparar pedras (NT0) ou balas de chumbo (NT2). Bala de chumbo: +1 dano e dobra distancia.",
            8 to "Preparado: exige manobra Preparar e teste de ST para disparar; remover projetil causa metade do dano de entrada."
        )

        private val OBS_ARMA_FOGO_PISTOLA_MM = mapOf(
            1 to "Inclui sistemas eletronicos das armas inteligentes (veja o quadro).",
            2 to "Os foguetes demoram um pouco para acelerar. Divida o dano por 3 a 1-2 metros e por 2 a 3-10 metros.",
            3 to "A versao civil de uma arma semiautomatica tem CdT 3, -25% no custo e recebe um bonus de +1 na CL."
        )

        private val OBS_ARMA_FOGO_RIFLE = mapOf(
            1 to "A versao civil de uma arma semiautomatica tem CdT 3, -25% no custo e um bonus de +1 na CL.",
            2 to "Se o dano ultrapassar a RD, o dardo injeta uma droga ou veneno como ataque de acompanhamento. No caso de dardo tranquilizador, faca um teste de HT-3; um fracasso deixa o alvo inconsciente por uma quantidade de minutos igual a margem pela qual o teste falhou.",
            3 to "Inclui os sistemas eletronicos das armas inteligentes (pag. 278).",
            4 to "Inclui um lancador de granadas completo de 25 mm (pag. 281)."
        )

        private val OBS_ARMA_FOGO_ULTRATECH = mapOf(
            1 to "A arma precisa de atmosfera para funcionar. Ela nao produz nenhum efeito em atmosferas rarefeitas ou no vacuo.",
            2 to "O dano por queimadura recebe o modificador de dano de Sobretensao (pag. 108). Alem disso, mesmo quando nenhum dano penetre, o alvo deve obter sucesso em um teste de HT-4 mais metade da RD do local atingido (devido ao divisor de armadura). No caso de fracasso, o choque eletrico deixa o alvo atordoado. O alvo pode fazer novo teste de HT a cada turno sob a mesma penalidade (mas sem o bonus de RD) para se recuperar.",
            3 to "Fumaca, nevoa, chuva, nuvens etc. concedem ao alvo uma RD adicional igual a penalidade de visibilidade. Exemplo: se a chuva impuser -1 a cada 100 metros, um laser percorrendo 2.000 metros de chuva deve superar RD adicional de 20.",
            4 to "O dano por queimadura recebe modificador de dano de Sobretensao (pag. 108).",
            5 to "Em aventuras com superciencia, um onidisparador custa o dobro, mas tem regulagem para atordoamento: o dano se torna HT-3(3) at para pistola e HT-6(3) at para rifle. Um fracasso em teste de HT deixa a vitima inconsciente por uma quantidade de minutos igual a margem de erro."
        )

        private val OBS_ARMA_FOGO_PESADA = mapOf(
            1 to "Tem uma distancia minima: 10 metros no caso de um LG de 40 mm, 30 metros no caso de um MTA de 115 mm e 200 metros no caso de um MAS de 70 mm.",
            2 to "Contra-disparo de risco: 1d ponto de dano por queimadura em qualquer pessoa que se encontre atras do atirador a uma distancia de ate 15 metros (30 no caso da MTA).",
            3 to "Ataque Guiado (pag. 412). O Canhoneiro usa Artilharia (Missil Guiado) para atacar. 1/2D e igual a velocidade do projetil (m/s). O peso se refere ao lancador vazio/um projetil.",
            4 to "Ataque Teleguiado (Visao Hiperespectral) (pag. 413) com NH 10 do projetil. O atirador faz teste de Bombardeiro (Missil Guiado) para apontar. Em sucesso, o missil recebe bonus de Prec. 1/2D e igual a velocidade (m/s) do projetil. O peso se refere ao lancador vazio/um projetil.",
            5 to "Um tripe destacavel pesa mais 22 kg.",
            6 to "Pode ser anexada a parte inferior do cano de qualquer rifle ou carabina de NT7+. Utilize a Magnitude do Rifle.",
            7 to "O dano nao e reduzido pela metade na distancia de 1/2D, mas perdera seu divisor de armadura que e de (10).",
            8 to "Embutido na ACI de NT9 (pag. 279). Utilize a Magnitude da ACI. Possui sistemas eletronicos das armas inteligentes (pag. 278)."
        )
    }
}
