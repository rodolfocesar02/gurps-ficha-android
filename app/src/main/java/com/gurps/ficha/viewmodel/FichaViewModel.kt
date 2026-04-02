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
import com.gurps.ficha.viewmodel.delegates.*
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

    // Estado de busca (Delegado)
    val advantageSearch get() = searchDelegate.advantageSearch
    val disadvantageSearch get() = searchDelegate.disadvantageSearch
    val skillSearch get() = searchDelegate.skillSearch
    val magicSearch get() = searchDelegate.magicSearch
    val techniqueSearch get() = searchDelegate.techniqueSearch
    val equipmentSearch get() = searchDelegate.equipmentSearch
    val shieldSearchQuery get() = searchDelegate.shieldSearchQuery


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
    private val magiasByIdCache: Map<String, MagiaDefinicao> by lazy {
        dataRepository.magias.associateBy { it.id }
    }
    private val todasEscolasMagiaCache by lazy { magicDelegate.todasEscolasMagia() }
    private val todasClassesMagiaCache by lazy { magicDelegate.todasClassesMagia() }

    private val fichaStorage = FichaStorageRepository.getInstance(application)
    val dataRepository = DataRepository.getInstance(application)
    private val nexusArcanoModoAlvoAdapter by lazy {
        NexusArcanoModoAlvoAdapter(dataRepository.magias)
    }

    // Delegados de Lógica (Refatoração Etapa 3)
    private val traitDelegate = FichaTraitDelegate(dataRepository)
    private val skillDelegate = FichaSkillDelegate(dataRepository)
    private val magicDelegate = FichaMagicDelegate(dataRepository, nexusArcanoModoAlvoAdapter)
    private val equipmentDelegate = FichaEquipmentDelegate(dataRepository)
    private val persistenceDelegate = FichaPersistenceDelegate(fichaStorage)
    private val networkDelegate = FichaNetworkDelegate()
    private val searchDelegate = FichaSearchDelegate(dataRepository)
    private val attributeDelegate = FichaAttributeDelegate()
    private val combatDelegate = FichaCombatDelegate()

    private val tecnicasNomesNormalizados: Set<String> get() = dataRepository.tecnicasCatalogo.map { skillDelegate.normalizarTexto(it.nome) }.filter { it.isNotBlank() }.toSet()
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
    val vantagensFiltradas get() = searchDelegate.filtrarVantagens()
    val desvantagensFiltradas get() = searchDelegate.filtrarDesvantagens()
    val periciasFiltradas get() = searchDelegate.filtrarPericias()
    val periciasSuplementaresArtesMarciais get() = dataRepository.periciasSuplementares
    val magiasFiltradas get() = searchDelegate.filtrarMagias()
    val tecnicasCatalogo get() = dataRepository.tecnicasCatalogo
    val tecnicasFiltradas get() = searchDelegate.filtrarTecnicas()
    val modificadoresGerais get() = dataRepository.modificadoresGerais

    val armasEquipamentosFiltradas get() = searchDelegate.filtrarArmas(personagem.forca) {
        equipmentDelegate.categoriaArmaFogoParaFiltro(it)
    }
    val escudosEquipamentosFiltrados get() = searchDelegate.filtrarEscudos(personagem.forca)
    val armadurasEquipamentosFiltradas get() = searchDelegate.filtrarArmaduras()

    val tagsArmadurasEquipamentos: List<String> get() = equipmentDelegate.tagsArmaduras()

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
    fun atualizarBuscaVantagem(busca: String) { searchDelegate.advantageSearch = searchDelegate.advantageSearch.copy(query = busca) }
    fun atualizarFiltroTipoCustoVantagem(tipo: TipoCusto?) { searchDelegate.advantageSearch = searchDelegate.advantageSearch.copy(costType = tipo) }
    fun atualizarBuscaDesvantagem(busca: String) { searchDelegate.disadvantageSearch = searchDelegate.disadvantageSearch.copy(query = busca) }
    fun atualizarFiltroTipoCustoDesvantagem(tipo: TipoCusto?) { searchDelegate.disadvantageSearch = searchDelegate.disadvantageSearch.copy(costType = tipo) }
    fun atualizarBuscaPericia(busca: String) { searchDelegate.skillSearch = searchDelegate.skillSearch.copy(query = busca) }
    fun atualizarFiltroAtributoPericia(atributo: String?) { searchDelegate.skillSearch = searchDelegate.skillSearch.copy(attribute = atributo) }
    fun atualizarFiltroDificuldadePericia(dificuldade: String?) { searchDelegate.skillSearch = searchDelegate.skillSearch.copy(difficulty = dificuldade) }
    fun atualizarBuscaMagia(busca: String) { searchDelegate.magicSearch = searchDelegate.magicSearch.copy(query = busca) }
    fun atualizarFiltroEscolaMagia(escola: String?) { searchDelegate.magicSearch = searchDelegate.magicSearch.copy(school = escola) }
    fun atualizarFiltroClasseMagia(classe: String?) { searchDelegate.magicSearch = searchDelegate.magicSearch.copy(magicClass = classe) }
    fun atualizarBuscaTecnica(busca: String) { searchDelegate.techniqueSearch = searchDelegate.techniqueSearch.copy(query = busca) }
    fun atualizarFiltroFonteTecnica(fonte: String?) { searchDelegate.techniqueSearch = searchDelegate.techniqueSearch.copy(source = fonte) }
    fun atualizarBuscaArmaEquipamento(busca: String) { searchDelegate.equipmentSearch = searchDelegate.equipmentSearch.copy(query = busca) }
    fun atualizarFiltroTipoArmaEquipamento(tipo: String?) {
        searchDelegate.equipmentSearch = if (tipo != "armas_de_fogo") {
            searchDelegate.equipmentSearch.copy(type = tipo, fireArmCategory = null)
        } else {
            searchDelegate.equipmentSearch.copy(type = tipo)
        }
    }
    fun atualizarFiltroCategoriaArmaFogoEquipamento(cat: String?) { searchDelegate.equipmentSearch = searchDelegate.equipmentSearch.copy(fireArmCategory = cat) }
    fun atualizarBuscaEscudoEquipamento(busca: String) { searchDelegate.shieldSearchQuery = busca }
    fun atualizarBuscaArmaduraEquipamento(busca: String) { searchDelegate.equipmentSearch = searchDelegate.equipmentSearch.copy(query = busca) }
    fun atualizarFiltroNtArmaduraEquipamento(nt: Int?) { searchDelegate.equipmentSearch = searchDelegate.equipmentSearch.copy(armorerNt = nt) }
    fun atualizarFiltroLocalArmaduraEquipamento(local: String?) { searchDelegate.equipmentSearch = searchDelegate.equipmentSearch.copy(armorerLocation = local) }
    fun atualizarFiltroTagArmaduraEquipamento(tag: String?) { searchDelegate.equipmentSearch = searchDelegate.equipmentSearch.copy(armorerTag = tag?.trim()?.takeIf { it.isNotBlank() }) }
    fun limparFiltrosArmaduraEquipamento() { searchDelegate.limparArmaduras() }
 

    // === INFORMACOES BASICAS ===
    fun atualizarNome(nome: String) { personagem = attributeDelegate.atualizarNome(personagem, nome) }
    fun atualizarJogador(jogador: String) { personagem = attributeDelegate.atualizarJogador(personagem, jogador) }
    fun atualizarCampanha(campanha: String) { personagem = attributeDelegate.atualizarCampanha(personagem, campanha) }
    fun atualizarPontosIniciais(pontos: Int) { personagem = attributeDelegate.atualizarPontosIniciais(personagem, pontos) }
    fun atualizarLimiteDesvantagens(limite: Int) { personagem = attributeDelegate.atualizarLimiteDesvantagens(personagem, limite) }

    fun atualizarForca(v: Int) { personagem = attributeDelegate.atualizarForca(personagem, v) }
    fun atualizarDestreza(v: Int) { personagem = attributeDelegate.atualizarDestreza(personagem, v) }
    fun atualizarInteligencia(v: Int) { personagem = attributeDelegate.atualizarInteligencia(personagem, v) }
    fun atualizarVitalidade(v: Int) { personagem = attributeDelegate.atualizarVitalidade(personagem, v) }

    fun definirBasesAtributosPrimarios(forcaBase: Int, destrezaBase: Int, inteligenciaBase: Int, vitalidadeBase: Int) {
        personagem = attributeDelegate.definirBasesAtributosPrimarios(personagem, forcaBase, destrezaBase, inteligenciaBase, vitalidadeBase)
    }

    fun atualizarModPontosVida(v: Int) { personagem = attributeDelegate.atualizarModPontosVida(personagem, v) }
    fun atualizarModVontade(v: Int) { personagem = attributeDelegate.atualizarModVontade(personagem, v) }
    fun atualizarModPercepcao(v: Int) { personagem = attributeDelegate.atualizarModPercepcao(personagem, v) }
    fun atualizarModPontosFadiga(v: Int) { personagem = attributeDelegate.atualizarModPontosFadiga(personagem, v) }
    fun atualizarModVelocidadeBasica(v: Float) { personagem = attributeDelegate.atualizarModVelocidadeBasica(personagem, v) }
    fun atualizarModDeslocamentoBasico(v: Int) { personagem = attributeDelegate.atualizarModDeslocamentoBasico(personagem, v) }
    fun atualizarPontosVidaRolagemAtual(v: Int?) { personagem = attributeDelegate.atualizarPontosVidaRolagemAtual(personagem, v) }
    fun atualizarPontosFadigaRolagemAtual(v: Int?) { personagem = attributeDelegate.atualizarPontosFadigaRolagemAtual(personagem, v) }

    fun atualizarModeloRacial(novo: ModeloRacial) {
        personagem = attributeDelegate.atualizarModeloRacial(personagem, novo)
        salvarFicha()
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
        val lista = traitDelegate.adicionarVantagem(personagem, definicao, nivel, custoEscolhido, descricao, modificadores, metadados)
        atualizarVantagensComConfirmacao(lista)
    }

    fun removerVantagem(index: Int) {
        val lista = traitDelegate.removerVantagem(personagem, index)
        atualizarVantagensComConfirmacao(lista)
    }

    fun atualizarVantagem(index: Int, vantagem: VantagemSelecionada) {
        val lista = traitDelegate.atualizarVantagem(personagem, index, vantagem)
        atualizarVantagensComConfirmacao(lista)
    }

    fun adicionarModificadorAVantagem(index: Int, mod: ModificadorSelecao) {
        val lista = traitDelegate.adicionarModificadorAVantagem(personagem, index, mod)
        atualizarVantagensComConfirmacao(lista)
    }

    fun removerModificadorDeVantagem(vantagemIndex: Int, modificadorIndex: Int) {
        val lista = traitDelegate.removerModificadorDeVantagem(personagem, vantagemIndex, modificadorIndex)
        atualizarVantagensComConfirmacao(lista)
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
        personagem = personagem.copy(
            desvantagens = traitDelegate.adicionarDesvantagem(personagem, definicao, nivel, custoEscolhido, descricao, autocontrole, modificadores, metadados)
        )
    }

    fun removerDesvantagem(index: Int) {
        personagem = personagem.copy(desvantagens = traitDelegate.removerDesvantagem(personagem, index))
    }

    fun atualizarDesvantagem(index: Int, desvantagem: DesvantagemSelecionada) {
        personagem = personagem.copy(desvantagens = traitDelegate.atualizarDesvantagem(personagem, index, desvantagem))
    }

    fun adicionarModificadorADesvantagem(index: Int, mod: ModificadorSelecao) {
        personagem = personagem.copy(desvantagens = traitDelegate.adicionarModificadorADesvantagem(personagem, index, mod))
    }

    fun removerModificadorDeDesvantagem(desvantagemIndex: Int, modificadorIndex: Int) {
        personagem = personagem.copy(desvantagens = traitDelegate.removerModificadorDeDesvantagem(personagem, desvantagemIndex, modificadorIndex))
    }

    // === PECULIARIDADES ===

    fun adicionarQualidade(qualidade: String) {
        personagem = personagem.copy(qualidades = traitDelegate.adicionarQualidade(personagem, qualidade))
    }

    fun removerQualidade(index: Int) {
        personagem = personagem.copy(qualidades = traitDelegate.removerQualidade(personagem, index))
    }

    fun adicionarPeculiaridade(peculiaridade: String) {
        personagem = personagem.copy(peculiaridades = traitDelegate.adicionarPeculiaridade(personagem, peculiaridade))
    }

    fun removerPeculiaridade(index: Int) {
        personagem = personagem.copy(peculiaridades = traitDelegate.removerPeculiaridade(personagem, index))
    }

    // === PERICIAS ===

    fun adicionarPericia(
        definicao: PericiaDefinicao,
        pontosGastos: Int = 1,
        especializacao: String = "",
        atributoEscolhido: AtributoBase? = null,
        dificuldadeEscolhida: Dificuldade? = null
    ): String? {
        val result = skillDelegate.adicionarPericia(personagem, definicao, pontosGastos, especializacao, atributoEscolhido, dificuldadeEscolhida)
        return result.fold(
            onSuccess = { 
                personagem = personagem.copy(pericias = it)
                null 
            },
            onFailure = { it.message }
        )
    }

    fun adicionarPericiaCustomizada(pericia: PericiaSelecionada) {
        personagem = personagem.copy(pericias = skillDelegate.atualizarPericia(personagem, personagem.pericias.size, pericia))
    }

    fun removerPericia(index: Int) {
        personagem = personagem.copy(pericias = skillDelegate.removerPericia(personagem, index))
    }

    fun atualizarPericia(index: Int, pericia: PericiaSelecionada) {
        personagem = personagem.copy(pericias = skillDelegate.atualizarPericia(personagem, index, pericia))
    }

    // === MAGIAS ===

    fun adicionarMagia(
        definicao: MagiaDefinicao,
        pontosGastos: Int = 1,
        encantamentoAlvo: String? = null,
        especializacaoMagia: String? = null,
        ignorarPreRequisito: Boolean = false
    ): String? {
        val result = magicDelegate.adicionarMagia(personagem, definicao, pontosGastos, encantamentoAlvo, especializacaoMagia, ignorarPreRequisito, nivelAptidaoMagica)
        return result.fold(
            onSuccess = {
                personagem = personagem.copy(magias = it)
                null
            },
            onFailure = { it.message }
        )
    }

    fun removerMagia(index: Int) {
        personagem = personagem.copy(magias = magicDelegate.removerMagia(personagem, index))
    }

    fun atualizarMagia(index: Int, magia: MagiaSelecionada) {
        personagem = personagem.copy(magias = magicDelegate.atualizarMagia(personagem, index, magia))
    }

    fun prereqFailureForMagia(def: MagiaDefinicao): String? {
        val assinatura = assinaturaEstadoMagiasParaModoAlvo()
        if (assinatura != prereqCacheAssinatura) {
            prereqCacheAssinatura = assinatura
            prereqFailureCache.clear()
        }
        return prereqFailureCache.getOrPut(def.id) {
            magicDelegate.prereqFailureForMagiaUnificada(personagem, def, nivelAptidaoMagica)
        }
    }
    fun prereqsSatisfied(def: MagiaDefinicao): Boolean = prereqFailureForMagia(def) == null
    fun prereqFailureForMagiaRapida(def: MagiaDefinicao) = prereqFailureForMagia(def)
    fun listaRelacionadosMagiaAlvo(alvo: MagiaDefinicao) = magicDelegate.calcularSnapshotModoAlvo(alvo.id, personagem, nivelAptidaoMagica).relacionadosIds
    fun idsRelacionadosMagiaAlvo(alvo: MagiaDefinicao) = listaRelacionadosMagiaAlvo(alvo).toSet()

    fun assinaturaEstadoMagiasParaModoAlvo() = magicDelegate.assinaturaEstadoMagias(personagem, nivelAptidaoMagica)

    fun requisitarModoAlvo(alvoId: String?, ativo: Boolean) {
        modoAlvoJob?.cancel()
        if (!ativo || alvoId.isNullOrBlank()) {
            limparModoAlvo()
            return
        }

        if (alvoId == modoAlvoUltimaChave && modoAlvoRelacionadosIds.isNotEmpty()) return
        modoAlvoUltimaChave = alvoId
        modoAlvoErro = null
        modoAlvoAviso = null
        modoAlvoCarregando = true

        modoAlvoJob = viewModelScope.launch(Dispatchers.Default) {
            try {
                val snapshot = magicDelegate.calcularSnapshotModoAlvo(alvoId, personagem, nivelAptidaoMagica)
                withContext(Dispatchers.Main) {
                    modoAlvoRelacionadosIds = snapshot.relacionadosIds
                    modoAlvoChavesAtivas = snapshot.chavesAtivas.map { it.descricao }
                    modoAlvoChavesFaltantes = snapshot.chavesFaltantes.map { it.descricao }
                    modoAlvoProximasAcoesIds = snapshot.proximasAcoesIds
                    modoAlvoProximasAcoes = snapshot.proximasAcoesIds.map { id -> magiasByIdCache[id]?.nome ?: id }
                    modoAlvoProgressoCadeia = snapshot.progressoCadeia
                    modoAlvoProgressoEscolas = snapshot.progressoEscolas
                    modoAlvoProximaObrigatoria = snapshot.proximaObrigatoriaId?.let { id -> magiasByIdCache[id]?.nome ?: id }
                    modoAlvoProximaLateralUtil = snapshot.proximaLateralUtilId?.let { id -> magiasByIdCache[id]?.nome ?: id }
                    modoAlvoBloqueioCurto = snapshot.bloqueioCurto
                    modoAlvoAviso = snapshot.aviso
                    modoAlvoCarregando = false
                }
            } catch (_: CancellationException) {
            } catch (t: Throwable) {
                withContext(Dispatchers.Main) {
                    modoAlvoErro = "Erro: ${t.message}"
                    modoAlvoCarregando = false
                }
            }
        }
    }

    private fun limparModoAlvo() {
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
    }

    fun recalcularModoAlvoAgora(alvoId: String?) {
        if (alvoId.isNullOrBlank()) return
        modoAlvoUltimaChave = null
        requisitarModoAlvo(alvoId, ativo = true)
    }

    // === TECNICAS ===

    fun adicionarTecnica(
        definicao: TecnicaCatalogoItem,
        periciaBase: PericiaSelecionada,
        nivelRelativoPredefinido: Int = 0
    ): String? {
        val result = skillDelegate.adicionarTecnica(personagem, definicao, periciaBase, nivelRelativoPredefinido)
        return result.fold(
            onSuccess = {
                personagem = personagem.copy(tecnicas = it)
                null
            },
            onFailure = { it.message }
        )
    }

    fun removerTecnica(index: Int) {
        personagem = personagem.copy(tecnicas = skillDelegate.removerTecnica(personagem, index))
    }

    fun atualizarTecnica(index: Int, tecnica: TecnicaSelecionada) {
        personagem = personagem.copy(tecnicas = skillDelegate.atualizarTecnica(personagem, index, tecnica))
    }

    // === EQUIPAMENTOS ===

    fun adicionarEquipamento(equipamento: Equipamento) {
        personagem = personagem.copy(equipamentos = equipmentDelegate.adicionarEquipamento(personagem, equipamento))
        personagem = combatDelegate.ajustarEscudoAutomatico(personagem, escudosEquipados)
    }

    fun adicionarEquipamentoArma(arma: ArmaCatalogoItem) {
        personagem = personagem.copy(equipamentos = equipmentDelegate.adicionarEquipamentoArma(personagem, arma))
        personagem = combatDelegate.ajustarEscudoAutomatico(personagem, escudosEquipados)
    }

    fun adicionarEquipamentoEscudo(escudo: EscudoCatalogoItem) {
        personagem = personagem.copy(equipamentos = equipmentDelegate.adicionarEquipamentoEscudo(personagem, escudo))
        personagem = combatDelegate.ajustarEscudoAutomatico(personagem, escudosEquipados)
    }

    fun adicionarEquipamentoArmadura(armadura: ArmaduraCatalogoItem) {
        personagem = personagem.copy(equipamentos = equipmentDelegate.adicionarEquipamentoArmadura(personagem, armadura))
    }

    fun adicionarEquipamentoArmaduraComSelecao(armadura: ArmaduraCatalogoItem, locaisSelecionados: List<String>) {
        personagem = personagem.copy(equipamentos = equipmentDelegate.adicionarEquipamentoArmaduraComSelecao(personagem, armadura, locaisSelecionados))
    }

    fun removerEquipamento(index: Int) {
        personagem = personagem.copy(equipamentos = equipmentDelegate.removerEquipamento(personagem, index))
        personagem = combatDelegate.ajustarEscudoAutomatico(personagem, escudosEquipados)
    }

    fun atualizarEquipamento(index: Int, equipamento: Equipamento) {
        personagem = personagem.copy(equipamentos = equipmentDelegate.atualizarEquipamento(personagem, index, equipamento))
        personagem = combatDelegate.ajustarEscudoAutomatico(personagem, escudosEquipados)
    }

    fun observacoesArmaPorEquipamento(equipamento: Equipamento): String {
        return equipmentDelegate.observacoesArmaPorEquipamento(equipamento)
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
            fichasSalvas = persistenceDelegate.salvarFicha(nomeArquivo, personagem)
        }
    }

    fun carregarFicha(nomeArquivo: String) {
        viewModelScope.launch {
            persistenceDelegate.carregarFicha(nomeArquivo)?.let { 
                personagem = it 
            }
        }
    }

    fun excluirFicha(nomeArquivo: String) {
        viewModelScope.launch {
            fichasSalvas = persistenceDelegate.excluirFicha(nomeArquivo)
        }
    }

    fun novaFicha() {
        personagem = Personagem()
        personagemPendenteLimpezaMagias = null
        mostrarConfirmacaoLimpezaMagias = false
    }

    fun exportarFichaJsonCompativel(): String {
        return persistenceDelegate.exportarJsonCompativel(personagem)
    }

    fun exportarFichaJsonVersionada(): String {
        return persistenceDelegate.exportarJsonVersionado(personagem)
    }

    fun importarFichaJson(json: String): String? {
        val result = persistenceDelegate.importarJson(json)
        return result.fold(
            onSuccess = { novo ->
                personagem = novo
                searchDelegate.resetarTodosCaches()
                personagemPendenteLimpezaMagias = null
                mostrarConfirmacaoLimpezaMagias = false
                salvarFicha(autoSaveRecuperacaoNome)
                "Ficha importada com sucesso."
            },
            onFailure = { it.message }
        )
    }

    private suspend fun carregarListaFichas() {
        fichasSalvas = persistenceDelegate.listarFichas()
    }

    private suspend fun restaurarAutoSaveSeExistir() {
        persistenceDelegate.restaurarAutoSave(autoSaveRecuperacaoNome)?.let {
            personagem = it
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
        val result = networkDelegate.enviarRolagemDiscord(payload)
        return result.fold(
            onSuccess = { RollDispatchStatus(enviado = true) },
            onFailure = { RollDispatchStatus(enviado = false, detalhe = it.message) }
        )
    }

    fun atualizarCanaisDiscord() {
        viewModelScope.launch {
            canaisDiscordCarregando = true
            canaisDiscordErro = null
            val result = networkDelegate.buscarCanaisDiscord()
            canaisDiscordCarregando = false

            result.fold(
                onSuccess = { channels ->
                    canaisDiscord = channels
                    val selecionadoAtual = canalDiscordSelecionadoId
                    if (!selecionadoAtual.isNullOrBlank()) {
                        channels.firstOrNull { it.id == selecionadoAtual }?.let { canal ->
                            canalDiscordSelecionadoNome = "${canal.guildName} / ${canal.name}"
                            configPrefs.edit().putString(prefCanalDiscordNome, canalDiscordSelecionadoNome).apply()
                        }
                    }
                },
                onFailure = { canaisDiscordErro = it.message }
            )
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

    fun tecnicaAtendePreRequisito(definicao: TecnicaCatalogoItem, periciaBase: PericiaSelecionada): Boolean =
        skillDelegate.tecnicaAtendePreRequisito(definicao, periciaBase)

    private fun periciaCompativelComFamilia(prereq: String, pericia: PericiaSelecionada): Boolean = skillDelegate.periciaCompativelComFamilia(prereq, pericia)

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
    fun atualizarBonusManualEsquiva(b: Int) { personagem = combatDelegate.atualizarBonusManualEsquiva(personagem, b) }
    fun atualizarPericiaApara(id: String?) { personagem = combatDelegate.atualizarPericiaApara(personagem, id) }
    fun atualizarBonusManualApara(b: Int) { personagem = combatDelegate.atualizarBonusManualApara(personagem, b) }
    fun atualizarPericiaBloqueio(id: String?) {
        personagem = combatDelegate.atualizarPericiaBloqueio(personagem, id)
        personagem = combatDelegate.ajustarEscudoAutomatico(personagem, escudosEquipados)
    }
    fun atualizarEscudoBloqueio(n: String?) { personagem = combatDelegate.atualizarEscudoBloqueio(personagem, n) }
    fun atualizarBonusManualBloqueio(b: Int) { personagem = combatDelegate.atualizarBonusManualBloqueio(personagem, b) }
    fun atualizarBonusDefesa(t: DefenseType, b: Int) {
        personagem = when(t) {
            DefenseType.ESQUIVA -> combatDelegate.atualizarBonusManualEsquiva(personagem, b)
            DefenseType.APARA -> combatDelegate.atualizarBonusManualApara(personagem, b)
            DefenseType.BLOQUEIO -> combatDelegate.atualizarBonusManualBloqueio(personagem, b)
        }
    }

    val esquivaCalculada get() = personagem.defesasAtivas.calcularEsquiva(personagem)
    val aparaCalculada get() = personagem.defesasAtivas.calcularApara(personagem)
    val bloqueioCalculado get() = personagem.defesasAtivas.calcularBloqueio(personagem)
    val defesasAtivasVisiveis get() = combatDelegate.calcularDefesasVisiveis(personagem)
    val periciasParaApara get() = personagem.pericias.filter { p -> p.definicaoId.lowercase() in PERICIAS_COMBATE && p.definicaoId.lowercase() != "escudo" }
    val periciasParaBloqueio get() = personagem.pericias.filter { it.definicaoId.equals("escudo", ignoreCase = true) }
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

    private fun atualizarVantagensComConfirmacao(novas: List<VantagemSelecionada>) {
        if (personagem.magias.isNotEmpty() && !novas.any { it.definicaoId.equals("aptidao_magica", ignoreCase = true) }) {
            personagemPendenteLimpezaMagias = personagem.copy(vantagens = novas)
            mostrarConfirmacaoLimpezaMagias = true
        } else {
            personagem = personagem.copy(vantagens = novas)
        }
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

        mestreIAChatHistory = mestreIAChatHistory + MestreIAClient.ChatMessage("user", pergunta)

        viewModelScope.launch {
            val contexto = if (modo != "geracao") "Ficha de ${personagem.nome}: ${personagem.toJson()}" else null
            val respostaTexto = networkDelegate.conversarComMestreIA(
                baseUrl, apiKey, workspaceSlug, pergunta, mestreIAChatHistory.dropLast(1), 
                contexto, getCatalogNames(), modo
            )

            if (respostaTexto != null) {
                mestreIAChatHistory = mestreIAChatHistory + MestreIAClient.ChatMessage("model", respostaTexto)
                if (modo == "geracao") {
                    MestreIAClient.extrairJsonFicha(respostaTexto)?.let { json ->
                        mestreIAUseCase.integrarRespostaNaFicha(json)
                        autoSaveIA()
                        onResult(true, "Ficha gerada com sucesso!")
                    } ?: onResult(true, "Ficha gerada, mas houve erro no formato JSON.")
                } else onResult(true, "Resposta recebida.")
            } else onResult(false, "Falha na conexão com Mestre Digital.")
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
            val timestamp = java.text.SimpleDateFormat("dd-MM_HH-mm", java.util.Locale.getDefault()).format(java.util.Date())
            val nomeAutoSave = "IA_${personagem.nome.ifBlank { "Sem_Nome" }}_$timestamp"
            fichaStorage.salvarFicha(nomeAutoSave, personagem.toJson())
            carregarListaFichas()
        }
    }

    fun gerarFichaComIA(historia: String, onResult: (Boolean, String) -> Unit) = conversarComMestreIA(historia, "geracao", onResult)
    fun analisarFichaComIA(onResult: (Boolean, String) -> Unit) = conversarComMestreIA("Analise minha ficha atual e dê sugestões.", "analise", onResult)
}
