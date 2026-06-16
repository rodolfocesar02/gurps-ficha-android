package com.gurps.ficha.viewmodel

import android.app.Application
import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.data.network.*
import com.gurps.ficha.BuildConfig
import com.gurps.ficha.domain.magias.NexusArcanoModoAlvoAdapter
import com.gurps.ficha.data.storage.FichaStorageRepository
import com.gurps.ficha.domain.rules.CharacterRules
import com.gurps.ficha.model.*
import com.gurps.ficha.regras_prerequisitos.ConditionStatus
import com.gurps.ficha.regras_prerequisitos.PreRequisitoType
import com.gurps.ficha.viewmodel.delegates.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import com.gurps.ficha.domain.MestreIAUseCase
import com.gurps.ficha.domain.engine.MagicEngine
import com.gurps.ficha.domain.engine.SkillEngine
import com.gurps.ficha.domain.magias.NexusArcanoModoAlvoSnapshot
import com.gurps.ficha.ui.features.rolagem.StDamageMode

enum class DefenseType { ESQUIVA, APARA, BLOQUEIO }

data class ActiveDefense(
    val type: DefenseType,
    val name: String,
    val baseValue: Int,
    val bonus: Int,
    val finalValue: Int,
    val detail: String? = null
)

data class RollDispatchStatus(val enviado: Boolean, val detalhe: String? = null)

@OptIn(FlowPreview::class)
class FichaViewModel(application: Application) : AndroidViewModel(application) {
    private val autoSaveRecuperacaoNome = "_autosave_recuperacao"
    var personagem by mutableStateOf(Personagem())
        private set

    var fichasSalvas by mutableStateOf<List<String>>(emptyList())
        private set
    var fichasNuvem by mutableStateOf<List<String>>(emptyList())
        private set

    var nomeFichaAtual by mutableStateOf<String?>(null)
        private set

    var estaCarregando by mutableStateOf(false)
        private set

    var mostrarConfirmacaoLimpezaMagias by mutableStateOf(false)

    private val configPrefs = application.getSharedPreferences("gurps_config", Context.MODE_PRIVATE)
    private val fichaStorage = FichaStorageRepository.getInstance(application)
    val dataRepository = DataRepository.getInstance(application)
    private val nexusArcanoModoAlvoAdapter by lazy { NexusArcanoModoAlvoAdapter(dataRepository.magias) }

    // Delegados
    private val traitDelegate = FichaTraitDelegate(dataRepository)
    private val skillDelegate = FichaSkillDelegate(dataRepository)
    private val magicDelegate = FichaMagicDelegate(dataRepository, nexusArcanoModoAlvoAdapter)
    private val equipmentDelegate = FichaEquipmentDelegate(dataRepository)
    private val persistenceDelegate = FichaPersistenceDelegate(fichaStorage)
    private val networkDelegate = FichaNetworkDelegate()
    private val searchDelegate = FichaSearchDelegate(dataRepository)
    private val attributeDelegate = FichaAttributeDelegate()
    private val combatDelegate = FichaCombatDelegate()
    
    // Novos Delegados (Refatoração Lote 15)
    private val iaDelegate = FichaIADelegate(this, dataRepository, viewModelScope, application)
    // Lote 354 (Saga A5): delegate do modo Saga (Narrador + campanhas + rolagem interativa)
    private val sagaDelegate = FichaSagaDelegate(this, dataRepository, viewModelScope, application)
    private val socialDelegate = FichaSocialDelegate(networkDelegate, configPrefs, viewModelScope)
    private val deviceId by lazy { Settings.Secure.getString(application.contentResolver, Settings.Secure.ANDROID_ID) ?: "DESCONHECIDO" }

    // Propriedades Estáticas/Config
    private val prefCanalDiscordId = "discord_canal_id"
    private val prefCanalDiscordNome = "discord_canal_nome"
    // MESTRE IA PRIME (Configurações Mascaradas via BuildConfig)
    val iaBaseUrl get() = BuildConfig.MESTRE_IA_URL
    val iaApiKey get() = BuildConfig.MESTRE_IA_KEY
    val iaWorkspaceSlug get() = BuildConfig.MESTRE_IA_MODEL
    private val prefNaoMostrarManualModoAlvo = "modo_alvo_manual_nao_mostrar"

    // Getters Delegados
    val advantageSearch get() = searchDelegate.advantageSearch
    val disadvantageSearch get() = searchDelegate.disadvantageSearch
    val skillSearch get() = searchDelegate.skillSearch
    val magicSearch get() = searchDelegate.magicSearch
    val techniqueSearch get() = searchDelegate.techniqueSearch
    val equipmentSearch get() = searchDelegate.equipmentSearch
    val shieldSearchQuery get() = searchDelegate.shieldSearchQuery

    val canaisDiscord get() = socialDelegate.canaisDiscord
    val canaisDiscordCarregando get() = socialDelegate.canaisDiscordCarregando
    val canaisDiscordErro get() = socialDelegate.canaisDiscordErro
    val canalDiscordSelecionadoId get() = socialDelegate.canalDiscordSelecionadoId
    val canalDiscordSelecionadoNome get() = socialDelegate.canalDiscordSelecionadoNome

    // Getters do modo Saga (Lote 354)
    val sagaCampanhas get() = sagaDelegate.campanhas
    val sagaCampanhaAtiva get() = sagaDelegate.campanhaAtiva
    val sagaCenaAtiva get() = sagaDelegate.cenaAtiva
    val sagaFeed get() = sagaDelegate.feed
    val sagaRolagemPendente get() = sagaDelegate.rolagemPendente
    val sagaFase get() = sagaDelegate.fase
    val sagaProcessando get() = sagaDelegate.processando
    fun sagaCarregarCampanhas() = sagaDelegate.carregarCampanhas()
    fun sagaCriarCampanha(nome: String, config: com.gurps.ficha.domain.saga.CampanhaConfig) = sagaDelegate.criarCampanha(nome, config)
    fun sagaContinuarCampanha(id: Long) = sagaDelegate.continuarCampanha(id)
    fun sagaExcluirCampanha(id: Long) = sagaDelegate.excluirCampanha(id)
    fun sagaSair() = sagaDelegate.sairDaCampanha()
    fun sagaEnviar(texto: String) = sagaDelegate.enviarMensagem(texto)
    fun sagaRolarDado() = sagaDelegate.rolarDadoPendente()

    // Getters/ações do combate da Saga (Lote 365 / B7)
    val sagaCombateEstado get() = sagaDelegate.combate.estado
    val sagaCombateDefesaPendente get() = sagaDelegate.combate.defesaPendente
    val sagaCombateAtivo get() = sagaDelegate.combate.ativo
    fun sagaCombateAtacar(alvoId: String, manobra: com.gurps.ficha.domain.combat.Manobra, local: com.gurps.ficha.domain.combat.LocalAtaque, modo: com.gurps.ficha.domain.combat.AtaqueTotalModo = com.gurps.ficha.domain.combat.AtaqueTotalModo.DETERMINADO) =
        sagaDelegate.combate.heroiAtaca(alvoId, manobra, local, modo)
    fun sagaCombateMover(alvoId: String?, afastar: Boolean, metros: Int) = sagaDelegate.combate.heroiMove(alvoId, afastar, metros)
    fun sagaCombateAvaliar(alvoId: String) = sagaDelegate.combate.heroiAvaliar(alvoId)
    fun sagaCombateApontar(alvoId: String) = sagaDelegate.combate.heroiApontar(alvoId)
    fun sagaCombateManobra(manobra: com.gurps.ficha.domain.combat.Manobra, novaPostura: com.gurps.ficha.domain.combat.Postura? = null) =
        sagaDelegate.combate.heroiManobra(manobra, novaPostura)
    fun sagaCombateDefender(opcao: com.gurps.ficha.domain.combat.CombatResolver.OpcaoDefesa) = sagaDelegate.combate.escolherDefesa(opcao)
    fun sagaCombateSelecionarAtaque(indice: Int) = sagaDelegate.combate.selecionarAtaque(indice)
    fun sagaCombateEncerrar() = sagaDelegate.combate.encerrarManual()

    // Efeitos da Saga na ficha do herói (Lote 366 / B8) — mutam e SALVAM a ficha carregada.
    fun sagaConcederXp(pts: Int): Int {
        personagem = personagem.copy(xpGanhos = (personagem.xpGanhos + pts).coerceAtLeast(0)); salvarFicha(); return personagem.xpGanhos
    }
    fun sagaDefinirPvAtual(v: Int) { atualizarPontosVidaRolagemAtual(v); salvarFicha() }
    fun sagaDefinirPfAtual(v: Int) { atualizarPontosFadigaRolagemAtual(v); salvarFicha() }
    fun sagaAdicionarItem(nome: String, qtd: Int) {
        if (nome.isBlank()) return
        adicionarEquipamento(com.gurps.ficha.model.Equipamento(nome = nome, quantidade = qtd.coerceAtLeast(1))); salvarFicha()
    }

    val mestreIAChatHistory get() = iaDelegate.mestreIAChatHistory
    val fichaGeradaPendente get() = iaDelegate.fichaGeradaPendente
    val mostrarDialogRetrato get() = iaDelegate.mostrarDialogRetrato
    val retratoGerandoStatus get() = iaDelegate.retratoGerandoStatus
    var mestreIAMode 
        get() = iaDelegate.mestreIAMode
        set(value) { 
            iaDelegate.mestreIAMode = value 
            if (value != "fechado") { // Se estiver abrindo qualquer modo de IA
                iaDelegate.verificarSincroniaAutomatica()
            }
        }

    // Estados de Sessão (Interface) - Não salvos no JSON
    var ataqueSelecionadoId by mutableStateOf<String?>(null)
    var fonteDanoSelecionadaId by mutableStateOf<String?>("st_base")
    var stDamageMode by mutableStateOf(StDamageMode.GDP)

    fun atualizarAtaqueSelecionado(id: String?) {
        ataqueSelecionadoId = id
        // Sincronia Automática com Apara
        id?.let { selectedId ->
            if (selectedId.startsWith("pericia_")) {
                // Extração precisa do ID: remove o prefixo e tudo após o último sublinhado (que separa a especialização)
                val skillId = selectedId.removePrefix("pericia_").substringBeforeLast("_").lowercase()
                
                if (PERICIAS_COMBATE.contains(skillId) && skillId != "escudo") {
                    atualizarPericiaApara(skillId)
                } else if (skillId == "escudo" || skillId == "capa") {
                    atualizarPericiaBloqueio(skillId)
                }
            }
        }
    }
    fun atualizarFonteDanoSelecionada(id: String?) { fonteDanoSelecionadaId = id }
    fun atualizarStDamageMode(mode: StDamageMode) { stDamageMode = mode }

    // Outros caches
    private val magiasByIdCache by lazy { dataRepository.magias.associateBy { it.id } }
    private val todasEscolasMagiaCache by lazy { magicDelegate.todasEscolasMagia() }
    private val todasClassesMagiaCache by lazy { magicDelegate.todasClassesMagia() }
    private var personagemPendenteLimpezaMagias: Personagem? = null
    private var prereqCacheAssinatura: String? = null
    private val prereqFailureCache = HashMap<String, String?>()

    init {
        CharacterRules.DATA_REPOSITORY_INSTANCE = dataRepository
        socialDelegate.atualizarCanais()
        iaDelegate.verificarSincroniaAutomatica()
        
        viewModelScope.launch(Dispatchers.Default) {
            runCatching { dataRepository.magias; dataRepository.filtrarMagias() }
        }

        viewModelScope.launch {
            fichaStorage.migrarDeSharedPreferencesSeNecessario()
            restaurarAutoSaveSeExistir()
            carregarListaFichas()
        }

        viewModelScope.launch {
            snapshotFlow { personagem.toJson() }.distinctUntilChanged().debounce(1000).collect { json ->
                if (estaCarregando) return@collect // Bloqueia auto-save EM MASSA durante trocas
                
                // Salva sempre no arquivo de recuperação (emergência)
                fichaStorage.salvarFicha(autoSaveRecuperacaoNome, json)
                
                // Se houver uma ficha ativa carregada, salva nela também (Auto-save real)
                nomeFichaAtual?.let { nome ->
                    if (nome != autoSaveRecuperacaoNome) {
                        fichaStorage.salvarFicha(nome, json)
                    }
                    // Sincronização Invisível com a Nuvem Railway
                    sincronizarComNuvem()
                }
            }
        }
        atualizarListaFichasUnificada()
    }

    // === MÉTODOS DE FILTRO ===
    fun atualizarBuscaVantagem(busca: String) { searchDelegate.advantageSearch = searchDelegate.advantageSearch.copy(query = busca) }
    fun atualizarFiltroTipoCustoVantagem(tipo: TipoCusto?) { searchDelegate.advantageSearch = searchDelegate.advantageSearch.copy(costType = tipo) }
    fun atualizarBuscaDesvantagem(busca: String) { searchDelegate.disadvantageSearch = searchDelegate.disadvantageSearch.copy(query = busca) }
    fun atualizarFiltroTipoCustoDesvantagem(tipo: TipoCusto?) { searchDelegate.disadvantageSearch = searchDelegate.disadvantageSearch.copy(costType = tipo) }
    fun atualizarBuscaPericia(busca: String) { searchDelegate.skillSearch = searchDelegate.skillSearch.copy(query = busca) }
    fun atualizarFiltroAtributoPericia(attr: String?) { searchDelegate.skillSearch = searchDelegate.skillSearch.copy(attribute = attr) }
    fun atualizarFiltroDificuldadePericia(dif: String?) { searchDelegate.skillSearch = searchDelegate.skillSearch.copy(difficulty = dif) }
    fun atualizarBuscaMagia(busca: String) { searchDelegate.magicSearch = searchDelegate.magicSearch.copy(query = busca) }
    fun atualizarFiltroEscolaMagia(esc: String?) { searchDelegate.magicSearch = searchDelegate.magicSearch.copy(school = esc) }
    fun atualizarFiltroClasseMagia(cls: String?) { searchDelegate.magicSearch = searchDelegate.magicSearch.copy(magicClass = cls) }
    fun atualizarBuscaTecnica(busca: String) { searchDelegate.techniqueSearch = searchDelegate.techniqueSearch.copy(query = busca) }
    fun atualizarBuscaArmaEquipamento(busca: String) { searchDelegate.equipmentSearch = searchDelegate.equipmentSearch.copy(query = busca) }
    fun atualizarFiltroTipoArmaEquipamento(tipo: String?) { searchDelegate.equipmentSearch = searchDelegate.equipmentSearch.copy(type = tipo, fireArmCategory = if (tipo == "armas_de_fogo") searchDelegate.equipmentSearch.fireArmCategory else null) }
    fun atualizarFiltroCategoriaArmaFogoEquipamento(cat: String?) { searchDelegate.equipmentSearch = searchDelegate.equipmentSearch.copy(fireArmCategory = cat) }
    fun atualizarBuscaEscudoEquipamento(busca: String) { searchDelegate.shieldSearchQuery = busca }
    fun atualizarBuscaArmaduraEquipamento(busca: String) { searchDelegate.equipmentSearch = searchDelegate.equipmentSearch.copy(query = busca) }
    fun configurarFiltroArmadura(nt: Int?, local: String?, tag: String?) { searchDelegate.equipmentSearch = searchDelegate.equipmentSearch.copy(armorerNt = nt, armorerLocation = local, armorerTag = tag?.takeIf { it.isNotBlank() }) }
    fun limparFiltrosArmaduraEquipamento() { searchDelegate.limparArmaduras() }
    fun atualizarFiltroLocalArmaduraEquipamento(local: String?) { searchDelegate.equipmentSearch = searchDelegate.equipmentSearch.copy(armorerLocation = local) }
    fun atualizarFiltroNtArmaduraEquipamento(nt: Int?) { searchDelegate.equipmentSearch = searchDelegate.equipmentSearch.copy(armorerNt = nt) }

    // === INFORMAÇÕES BÁSICAS ===
    fun atualizarNome(nome: String) { personagem = attributeDelegate.atualizarNome(personagem, nome) }
    fun atualizarJogador(jogador: String) { personagem = attributeDelegate.atualizarJogador(personagem, jogador) }
    fun atualizarCampanha(campanha: String) { personagem = attributeDelegate.atualizarCampanha(personagem, campanha) }
    fun atualizarHistorico(historico: String) { personagem = attributeDelegate.atualizarHistorico(personagem, historico) }
    fun atualizarAparencia(aparencia: String) { personagem = attributeDelegate.atualizarAparencia(personagem, aparencia) }
    fun atualizarImagemPersonagem(uri: String, originalUri: String) { personagem = attributeDelegate.atualizarImagemPersonagem(personagem, uri, originalUri) }
    fun atualizarNotas(notas: String) { personagem = attributeDelegate.atualizarNotas(personagem, notas) }
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
    fun atualizarModeloRacial(novo: ModeloRacial) { personagem = attributeDelegate.atualizarModeloRacial(personagem, novo); salvarFicha() }

    fun injetarEventoMestreIA(texto: String) = iaDelegate.injetarEvento(texto)

    // === VANTAGENS ===
    fun adicionarVantagem(def: VantagemDefinicao, nivel: Int = 1, custo: Int = 0, desc: String = "", mods: List<ModificadorSelecao> = emptyList(), meta: Map<String, String>? = null): String? {
        val res = traitDelegate.adicionarVantagem(personagem, def, nivel, custo, desc, mods, meta)
        return res.fold(
            onSuccess = {
                atualizarVantagensComConfirmacao(it)
                injetarEventoMestreIA("[SISTEMA] '${def.nome}' adicionada à ficha ($custo pts).")
                null
            },
            onFailure = { it.message }
        )
    }
    fun removerVantagem(index: Int) { atualizarVantagensComConfirmacao(traitDelegate.removerVantagem(personagem, index)) }
    fun atualizarVantagem(index: Int, v: VantagemSelecionada) { 
        atualizarVantagensComConfirmacao(traitDelegate.atualizarVantagem(personagem, index, v))
        salvarFicha()
    }
    
    // === DESVANTAGENS ===
    fun adicionarDesvantagem(def: DesvantagemDefinicao, nivel: Int = 1, custo: Int = 0, desc: String = "", ctrl: Int? = null, mods: List<ModificadorSelecao> = emptyList(), meta: Map<String, String>? = null): String? {
        val res = traitDelegate.adicionarDesvantagem(personagem, def, nivel, custo, desc, ctrl, mods, meta)
        return res.fold(
            onSuccess = {
                personagem = personagem.copy(desvantagens = it)
                injetarEventoMestreIA("[SISTEMA] Desvantagem '${def.nome}' adicionada à ficha ($custo pts).")
                null
            },
            onFailure = { it.message }
        )
    }
    fun removerDesvantagem(index: Int) { personagem = personagem.copy(desvantagens = traitDelegate.removerDesvantagem(personagem, index)) }
    fun atualizarDesvantagem(index: Int, d: DesvantagemSelecionada) { 
        personagem = personagem.copy(desvantagens = traitDelegate.atualizarDesvantagem(personagem, index, d))
        salvarFicha()
    }

    // === QUALIDADES / PECULIARIDADES ===
    fun adicionarQualidade(q: String) { personagem = personagem.copy(qualidades = traitDelegate.adicionarQualidade(personagem, q)); salvarFicha() }
    fun removerQualidade(index: Int) { personagem = personagem.copy(qualidades = traitDelegate.removerQualidade(personagem, index)); salvarFicha() }
    fun atualizarQualidade(index: Int, q: String) { personagem = personagem.copy(qualidades = traitDelegate.atualizarQualidade(personagem, index, q)); salvarFicha() }
    fun adicionarPeculiaridade(p: String) { personagem = personagem.copy(peculiaridades = traitDelegate.adicionarPeculiaridade(personagem, p)); salvarFicha() }
    fun removerPeculiaridade(index: Int) { personagem = personagem.copy(peculiaridades = traitDelegate.removerPeculiaridade(personagem, index)); salvarFicha() }
    fun atualizarPeculiaridade(index: Int, p: String) { personagem = personagem.copy(peculiaridades = traitDelegate.atualizarPeculiaridade(personagem, index, p)); salvarFicha() }

    // === PERÍCIAS ===
    fun adicionarPericia(def: PericiaDefinicao, pts: Int = 1, esp: String = "", attr: AtributoBase? = null, dif: Dificuldade? = null): String? {
        val res = skillDelegate.adicionarPericia(personagem, def, pts, esp, attr, dif)
        return res.fold(
            onSuccess = {
                personagem = personagem.copy(pericias = it)
                injetarEventoMestreIA("[SISTEMA] Perícia '${def.nome}' adicionada à ficha ($pts pts).")
                null
            },
            onFailure = { it.message }
        )
    }

    fun validarPreRequisitosPericia(def: PericiaDefinicao): String? {
        return dataRepository.validarPreRequisitosPericia(def, personagem)
    }

    fun validarPreRequisitosPericiaDetailed(definicaoId: String): List<ConditionStatus> {
        return dataRepository.validarPreRequisitosPericiaDetailed(definicaoId, personagem)
    }
    fun adicionarPericiaCustomizada(p: PericiaSelecionada) { personagem = personagem.copy(pericias = skillDelegate.adicionarPericiaCustomizada(personagem, p)) }
    fun removerPericia(i: Int) { personagem = personagem.copy(pericias = skillDelegate.removerPericia(personagem, i)) }
    fun atualizarPericia(i: Int, p: PericiaSelecionada) { personagem = personagem.copy(pericias = skillDelegate.atualizarPericia(personagem, i, p)) }

    // === MAGIAS ===
    fun adicionarMagia(def: MagiaDefinicao, pts: Int = 1, alvo: String? = null, esp: String? = null, ignora: Boolean = false): String? {
        val res = magicDelegate.adicionarMagia(personagem, def, pts, alvo, esp, ignora, nivelAptidaoMagica)
        return res.fold(
            onSuccess = {
                personagem = personagem.copy(magias = it)
                injetarEventoMestreIA("[SISTEMA] Magia '${def.nome}' adicionada à ficha ($pts pts).")
                null
            },
            onFailure = { it.message }
        )
    }
    fun removerMagia(i: Int) { personagem = personagem.copy(magias = magicDelegate.removerMagia(personagem, i)) }
    fun atualizarMagia(i: Int, m: MagiaSelecionada) { personagem = personagem.copy(magias = magicDelegate.atualizarMagia(personagem, i, m)) }

    fun prereqFailureForMagia(def: MagiaDefinicao): String? {
        val assinatura = magicDelegate.assinaturaEstadoMagias(personagem, nivelAptidaoMagica)
        if (assinatura != prereqCacheAssinatura) { prereqCacheAssinatura = assinatura; prereqFailureCache.clear() }
        return prereqFailureCache.getOrPut(def.id) { magicDelegate.prereqFailureForMagiaUnificada(personagem, def, nivelAptidaoMagica) }
    }
    fun prereqsSatisfied(def: MagiaDefinicao): Boolean = prereqFailureForMagia(def) == null

    // === MODO ALVO (NEXUS ARCANO) ===
    var modoAlvoId by mutableStateOf<String?>(null)
        private set
    var modoAlvoSnapshot by mutableStateOf<NexusArcanoModoAlvoSnapshot?>(null)
        private set
    val modoAlvoProximasAcoesIds get() = modoAlvoSnapshot?.proximasAcoesIds ?: emptyList()
    val modoAlvoRelacionadosIds get() = modoAlvoSnapshot?.relacionadosIds ?: emptyList()
    // Lote 338: assinatura do estado usado no último snapshot, para recalcular quando o
    // jogador APRENDE uma magia (antes só recalculava ao trocar o alvo → lista congelava).
    private var modoAlvoAssinatura: String? = null

    fun requisitarModoAlvo(id: String?, habilitado: Boolean) {
        if (!habilitado) {
            modoAlvoId = null
            modoAlvoSnapshot = null
            modoAlvoAssinatura = null
            return
        }
        val assinaturaAtual = assinaturaEstadoMagiasParaModoAlvo()
        // Recalcula se mudou o ALVO ou o ESTADO (magias/AM/IQ/DX) desde o último snapshot.
        if (modoAlvoId != id || modoAlvoAssinatura != assinaturaAtual) {
            modoAlvoId = id
            modoAlvoAssinatura = assinaturaAtual
            atualizarModoAlvoSnapshot()
        }
    }

    fun assinaturaEstadoMagiasParaModoAlvo(): String = magicDelegate.assinaturaEstadoMagias(personagem, nivelAptidaoMagica)

    private fun atualizarModoAlvoSnapshot() {
        val alvo = modoAlvoId ?: run { modoAlvoSnapshot = null; return }
        val idsConhecidas = personagem.magias.map { it.definicaoId }.toSet()
        modoAlvoSnapshot = nexusArcanoModoAlvoAdapter.calcular(
            alvoId = alvo,
            magiasConhecidasIds = idsConhecidas,
            iq = personagem.inteligencia,
            dx = personagem.destreza,
            am = nivelAptidaoMagica
        )
    }

    // === TÉCNICAS ===
    fun adicionarTecnica(definicao: TecnicaCatalogoItem, periciaBase: PericiaSelecionada, nivelRelativoPredefinido: Int = 0): String? {
        val res = skillDelegate.adicionarTecnica(personagem, definicao, periciaBase, nivelRelativoPredefinido)
        return res.fold(onSuccess = { personagem = personagem.copy(tecnicas = it); null }, onFailure = { it.message })
    }
    fun removerTecnica(i: Int) { personagem = personagem.copy(tecnicas = skillDelegate.removerTecnica(personagem, i)) }
    fun atualizarTecnica(i: Int, t: TecnicaSelecionada) { personagem = personagem.copy(tecnicas = skillDelegate.atualizarTecnica(personagem, i, t)) }

    // === EQUIPAMENTOS ===
    fun adicionarEquipamento(e: Equipamento) { personagem = personagem.copy(equipamentos = equipmentDelegate.adicionarEquipamento(personagem, e)); ajustarEscudo() }
    fun adicionarEquipamentoArma(a: ArmaCatalogoItem) { personagem = personagem.copy(equipamentos = equipmentDelegate.adicionarEquipamentoArma(personagem, a)); ajustarEscudo() }
    fun adicionarEquipamentoEscudo(e: EscudoCatalogoItem) { personagem = personagem.copy(equipamentos = equipmentDelegate.adicionarEquipamentoEscudo(personagem, e)); ajustarEscudo() }
    fun adicionarEquipamentoArmadura(a: ArmaduraCatalogoItem) { personagem = personagem.copy(equipamentos = equipmentDelegate.adicionarEquipamentoArmadura(personagem, a)) }
    fun adicionarEquipamentoArmaduraComSelecao(armadura: ArmaduraCatalogoItem, locais: List<String>) {
        personagem = personagem.copy(equipamentos = equipmentDelegate.adicionarEquipamentoArmaduraComSelecao(personagem, armadura, locais))
    }
    fun removerEquipamento(i: Int) { personagem = personagem.copy(equipamentos = equipmentDelegate.removerEquipamento(personagem, i)); ajustarEscudo() }
    fun atualizarEquipamento(i: Int, e: Equipamento) { personagem = personagem.copy(equipamentos = equipmentDelegate.atualizarEquipamento(personagem, i, e)); ajustarEscudo() }
    private fun ajustarEscudo() { personagem = combatDelegate.ajustarEscudoAutomatico(personagem, escudosEquipados) }

    // === PERSISTÊNCIA ===
    fun salvarFicha(nome: String = personagem.nome.ifBlank { "Sem_Nome" }) {
        nomeFichaAtual = nome
        viewModelScope.launch { fichasSalvas = persistenceDelegate.salvarFicha(nome, personagem) }
        // Sobe o retrato ao Discord UMA vez, junto do salvar (best-effort, não
        // bloqueia o salvamento). O bot guarda e usa nos embeds de rolagem.
        val imagemUri = personagem.imagemPersonagemUri
        if (imagemUri.isNotBlank() && nome.isNotBlank()) {
            viewModelScope.launch {
                val dataUri = com.gurps.ficha.data.storage.ImagemPersonagemStore.bytesBase64(imagemUri)
                if (dataUri != null) {
                    networkDelegate.enviarRetratoDiscord(nome, dataUri)
                }
            }
        }
    }
    fun carregarFicha(nome: String, onResult: (Boolean, String) -> Unit) { 
        estaCarregando = true
        viewModelScope.launch {
            val result = persistenceDelegate.carregarFicha(nome)
            result.fold(
                onSuccess = { 
                    personagem = it 
                    nomeFichaAtual = nome
                    estaCarregando = false
                    onResult(true, "Ficha carregada.")
                },
                onFailure = { 
                    estaCarregando = false
                    onResult(false, "Erro ao carregar: ${it.message}")
                }
            )
        }
    }
    fun excluirFicha(nome: String) { viewModelScope.launch { fichasSalvas = persistenceDelegate.excluirFicha(nome) } }
    fun novaFicha() { personagem = Personagem(); personagemPendenteLimpezaMagias = null; mostrarConfirmacaoLimpezaMagias = false; nomeFichaAtual = null; nomeSessaoIA = null }
    
    fun atualizarListaFichasUnificada() {
        viewModelScope.launch {
            fichasSalvas = persistenceDelegate.listarFichas()
            fichasNuvem = networkDelegate.buscarFichasNuvem(deviceId)
        }
    }

    fun verificarConflitoNome(nome: String): Boolean {
        val nomeSanitizado = nome.trim()
        val existeLocal = fichasSalvas.any { it.equals(nomeSanitizado, ignoreCase = true) }
        val existeNuvem = fichasNuvem.any { it.equals(nomeSanitizado, ignoreCase = true) }
        // Se houver conflito mas for o nome da ficha que JÁ estamos editando, não é conflito
        if (nomeSanitizado.equals(nomeFichaAtual, ignoreCase = true)) return false
        return existeLocal || existeNuvem
    }

    fun gerarSugeridoComIndice(nomeOriginal: String): String {
        var indice = 2
        var novoNome = "$nomeOriginal ($indice)"
        while (verificarConflitoNome(novoNome)) {
            indice++
            novoNome = "$nomeOriginal ($indice)"
        }
        return novoNome
    }
    fun exportarFichaJsonCompativel() = persistenceDelegate.exportarJsonCompativel(personagem)
    fun exportarFichaJsonVersionada() = persistenceDelegate.exportarJsonVersionado(personagem)

    /**
     * Versões SUSPEND da exportação que EMBUTEM a imagem original do
     * personagem (base64) no arquivo, para a foto viajar junto com a ficha.
     * Use estas ao exportar/compartilhar.
     */
    suspend fun exportarFichaJsonCompativelComImagem(): String {
        return persistenceDelegate.exportarJsonCompativel(comImagemEmbutida())
    }
    suspend fun exportarFichaJsonVersionadaComImagem(): String {
        return persistenceDelegate.exportarJsonVersionado(comImagemEmbutida())
    }

    /** Devolve uma cópia do personagem com a imagem ORIGINAL embutida em base64. */
    private suspend fun comImagemEmbutida(): Personagem {
        val original = personagem.imagemPersonagemOriginalUri.ifBlank { personagem.imagemPersonagemUri }
        if (original.isBlank()) return personagem
        val dataUri = com.gurps.ficha.data.storage.ImagemPersonagemStore.bytesBase64(original)
            ?: return personagem
        return personagem.copy(imagemPersonagemBase64 = dataUri)
    }
    fun importarFichaJson(json: String): String? {
        estaCarregando = true
        val res = persistenceDelegate.importarJson(json)
        return res.fold(onSuccess = { novo ->
            personagem = novo
            nomeFichaAtual = novo.nome.ifBlank { "Importada_${System.currentTimeMillis()}" }
            searchDelegate.resetarTodosCaches()
            estaCarregando = false
            restaurarImagemEmbutidaSeHouver(novo)
            "Sucesso"
        }, onFailure = {
            estaCarregando = false
            it.message
        })
    }

    /**
     * Se a ficha importada trouxe a imagem EMBUTIDA (imagemPersonagemBase64),
     * salva a imagem no aparelho (re-recorta o rosto → gera as 2 versões),
     * aponta os URIs e LIMPA o base64. Assíncrono — a foto aparece logo após.
     */
    private fun restaurarImagemEmbutidaSeHouver(ficha: Personagem) {
        val base64 = ficha.imagemPersonagemBase64
        if (base64.isBlank()) return
        viewModelScope.launch {
            val imagens = com.gurps.ficha.data.storage.ImagemPersonagemStore
                .salvarDeBase64(getApplication(), base64)
            // Atualiza só se a ficha ativa ainda for esta (usuário não trocou).
            if (personagem.nome == ficha.nome) {
                personagem = if (imagens != null) {
                    personagem.copy(
                        imagemPersonagemUri = imagens.recortadaUri,
                        imagemPersonagemOriginalUri = imagens.originalUri,
                        imagemPersonagemBase64 = ""
                    )
                } else {
                    personagem.copy(imagemPersonagemBase64 = "")
                }
            }
        }
    }
    private suspend fun carregarListaFichas() { fichasSalvas = persistenceDelegate.listarFichas() }
    private suspend fun restaurarAutoSaveSeExistir() { persistenceDelegate.restaurarAutoSave(autoSaveRecuperacaoNome)?.let { personagem = it } }

    // === MESTRE IA / DISCORD ===
    fun conversarComMestreIA(p: String, m: String = "conversa", onRes: (Boolean, String) -> Unit) = iaDelegate.conversar(p, m, onRes)
    fun executarAcaoIA(c: String) = iaDelegate.executarAcao(c)
    fun confirmarIntegracaoFicha() = iaDelegate.confirmarIntegracao()
    fun descartarFichaPendente() = iaDelegate.descartarPendente()
    fun gerarRetratoIA(promptCustom: String? = null, onFim: (() -> Unit)? = null) =
        iaDelegate.gerarRetratoIA(promptCustom, onFim)
    fun dispensarDialogRetrato() { iaDelegate.mostrarDialogRetrato = false }
    fun limparChatMestreIA() = iaDelegate.limparChat()
    fun gerarSaudacaoMestreIA() = iaDelegate.gerarSaudacaoSeVazio()
    fun adicionarMensagemVoz(texto: String, role: String) = iaDelegate.adicionarMensagemVoz(texto, role)
    fun atualizarUltimaMensagemVozUsuario(texto: String) = iaDelegate.atualizarUltimaMensagemVozUsuario(texto)
    val mestreIASavedSessions get() = iaDelegate.savedSessions
    fun carregarHistoricoMestreIA() = iaDelegate.carregarHistorico()
    fun carregarSessaoMestreIA(id: Long) = iaDelegate.carregarSessao(id)
    /**
     * Função desabilitada na arquitetura PRIME (Segurança Sete Chaves).
     * As chaves agora são injetadas via BuildConfig.
     */
    fun salvarConfiguracaoIA(baseUrl: String, apiKey: String, workspaceSlug: String) {
        // Log ou ação de segurança se necessário
    }
    // Nome FIXO da sessão de criação/edição por IA. Antes (bug): cada autoSaveIA
    // usava timestamp novo → ~20 arquivos-lixo por ficha criada (o loop salva a
    // cada edição). Agora o nome é fixado na 1ª chamada e REUSADO nas seguintes,
    // sobrescrevendo o MESMO arquivo. Zera em novaFicha() (início de nova criação).
    private var nomeSessaoIA: String? = null
    fun autoSaveIA() {
        val nome = nomeSessaoIA ?: "IA_${personagem.nome.ifBlank { "Sem_Nome" }}_${System.currentTimeMillis()}".also { nomeSessaoIA = it }
        viewModelScope.launch { fichaStorage.salvarFicha(nome, personagem.toJson()); carregarListaFichas() }
    }
    fun gerarFichaComIA(h: String, onRes: (Boolean, String) -> Unit) = iaDelegate.conversar(h, "geracao", onRes)
    fun analisarFichaComIA(onRes: (Boolean, String) -> Unit) = iaDelegate.conversar("Analise minha ficha atual e dê sugestões.", "analise", onRes)

    fun atualizarCanaisDiscord() = socialDelegate.atualizarCanais()
    fun selecionarCanalDiscord(c: DiscordVoiceChannel?) = socialDelegate.selecionarCanal(c)
    suspend fun enviarRolagemDiscord(p: DiscordRollPayload) = socialDelegate.enviarRolagem(p)

    // === UTILITÁRIOS / COMBATE ===
    fun calcularDanoArmaComSt(r: String?) = CharacterRules.resolverDanoPorSt(r?.trim().orEmpty(), personagem.forca)
    val nivelAptidaoMagica get() = MagicEngine.getNivelAptidaoMagicaParaMagia(personagem, null)
    val temAptidaoMagica get() = nivelAptidaoMagica > 0
    val nivelCarga get() = personagem.nivelCarga
    val deslocamentoAtual get() = personagem.deslocamentoAtual
    val esquivaAtual get() = (personagem.esquiva - personagem.nivelCarga).coerceAtLeast(1)
    val esquivaCalculada get() = personagem.defesasAtivas.calcularEsquiva(personagem)
    val aparaCalculada get() = personagem.defesasAtivas.calcularApara(personagem)
    val bloqueioCalculado get() = personagem.defesasAtivas.calcularBloqueio(personagem)
    val defesasAtivasVisiveis get() = combatDelegate.calcularDefesasVisiveis(personagem)
    val escudosEquipados get() = personagem.equipamentos.filter { it.tipo == TipoEquipamento.ESCUDO || it.tipo == TipoEquipamento.CAPA }.sortedBy { it.nome.lowercase() }
    val periciasParaApara get() = personagem.pericias.filter { it.definicaoId.lowercase() in PERICIAS_COMBATE && it.definicaoId.lowercase() != "escudo" }
    val periciasParaBloqueio get() = personagem.pericias.filter { it.definicaoId.lowercase() == "escudo" || it.definicaoId.lowercase() == "capa" }
    
    // === NUVEM INVISÍVEL (SINCRO V19: Múltiplas Fichas) ===
    fun sincronizarComNuvem() {
        val nomeFicha = personagem.nome.ifBlank { return }
        viewModelScope.launch {
            networkDelegate.salvarFichaNuvem(deviceId, nomeFicha, personagem)
            fichasNuvem = networkDelegate.buscarFichasNuvem(deviceId)
        }
    }

    fun restaurarDaNuvem(nome: String, onRes: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val json = networkDelegate.baixarFichaNuvem(deviceId, nome)
            if (json != null) {
                val resMsg = importarFichaJson(json)
                if (resMsg == "Sucesso") {
                    nomeFichaAtual = nome
                    onRes(true, "Ficha '$nome' restaurada da nuvem!")
                }
                else onRes(false, "Erro ao processar dados da nuvem: $resMsg")
            } else {
                onRes(false, "Ficha '$nome' não encontrada na nuvem.")
            }
        }
    }

    fun atualizarBonusManualEsquiva(b: Int) { personagem = combatDelegate.atualizarBonusManualEsquiva(personagem, b) }
    fun atualizarPericiaApara(id: String?) { personagem = combatDelegate.atualizarPericiaApara(personagem, id) }
    fun atualizarBonusManualApara(b: Int) { personagem = combatDelegate.atualizarBonusManualApara(personagem, b) }
    fun atualizarPericiaBloqueio(id: String?) { personagem = combatDelegate.atualizarPericiaBloqueio(personagem, id); ajustarEscudo() }
    fun atualizarEscudoBloqueio(n: String?) { personagem = combatDelegate.atualizarEscudoBloqueio(personagem, n) }
    fun atualizarBonusManualBloqueio(b: Int) { personagem = combatDelegate.atualizarBonusManualBloqueio(personagem, b) }

    val armasEquipamentosFiltradas get() = searchDelegate.filtrarArmas(personagem.forca) { equipmentDelegate.categoriaArmaFogoParaFiltro(it) }
    val escudosEquipamentosFiltrados get() = searchDelegate.filtrarEscudos(personagem.forca)
    val armadurasEquipamentosFiltradas get() = searchDelegate.filtrarArmaduras()
    val pesoTotal get() = personagem.pesoTotalEquipamentos
    val custoTotalEquipamentos get() = personagem.custoTotalEquipamentos
    fun observacoesArmaPorEquipamento(e: Equipamento) = equipmentDelegate.observacoesArmaPorEquipamento(e)

    fun confirmarLimpezaMagiasAoPerderAptidao() { personagem = personagemPendenteLimpezaMagias?.copy(magias = emptyList()) ?: personagem; personagemPendenteLimpezaMagias = null; mostrarConfirmacaoLimpezaMagias = false }
    fun cancelarLimpezaMagiasAoPerderAptidao() { personagemPendenteLimpezaMagias = null; mostrarConfirmacaoLimpezaMagias = false }
    private fun atualizarVantagensComConfirmacao(novas: List<VantagemSelecionada>) {
        if (personagem.magias.isNotEmpty() && !novas.any { it.definicaoId.equals("aptidao_magica", ignoreCase = true) }) {
            personagemPendenteLimpezaMagias = personagem.copy(vantagens = novas); mostrarConfirmacaoLimpezaMagias = true
        } else { personagem = personagem.copy(vantagens = novas) }
    }

    fun nivelAptidaoMagicaParaMagia(magia: MagiaDefinicao?) = MagicEngine.getNivelAptidaoMagicaParaMagia(personagem, magia)
    fun nivelAptidaoMagicaParaDefinicao(def: MagiaDefinicao?) = MagicEngine.getNivelAptidaoMagicaParaMagia(personagem, def)




    fun deveMostrarManualModoAlvo() = !configPrefs.getBoolean(prefNaoMostrarManualModoAlvo, false)
    fun definirNaoMostrarManualModoAlvo(b: Boolean) = configPrefs.edit().putBoolean(prefNaoMostrarManualModoAlvo, b).apply()
    
    fun vantagemJaAdicionada(id: String, d: String = ""): Boolean = personagem.vantagens.any { it.definicaoId == id && it.descricao == d }
    fun desvantagemJaAdicionada(id: String, d: String = ""): Boolean = personagem.desvantagens.any { it.definicaoId == id && it.descricao == d }
    fun periciaJaAdicionada(id: String, e: String = ""): Boolean = personagem.pericias.any { it.definicaoId == id && it.especializacao == e }
    fun magiaJaAdicionada(id: String): Boolean = personagem.magias.any { it.definicaoId == id }
    fun tecnicaJaAdicionada(id: String, bId: String? = null, bEsp: String = ""): Boolean = personagem.tecnicas.any { t -> t.definicaoId == id && (bId == null || (t.periciaBaseDefinicaoId == bId && t.periciaBaseEspecializacao.equals(bEsp, true))) }

    fun tecnicaAtendePreRequisito(def: TecnicaCatalogoItem, base: PericiaSelecionada) = skillDelegate.tecnicaAtendePreRequisito(def, base)
    fun custoTecnica(def: TecnicaCatalogoItem, nv: Int) = skillDelegate.custoTecnica(def, nv)
    fun limiteMaximoTecnica(def: TecnicaCatalogoItem) = skillDelegate.limiteMaximoTecnica(def)
    fun preRequisitoExibicaoTecnica(def: TecnicaCatalogoItem) = skillDelegate.preRequisitoExibicaoTecnica(def)
    fun calcularNivelTecnicaPreview(def: TecnicaCatalogoItem, base: PericiaSelecionada, nv: Int) = skillDelegate.calcularNivelTecnicaPreview(personagem, def, base, nv)

    val desvantagensExcedemLimite get() = personagem.desvantagensExcedemLimite
    val pontosDesvantagens get() = personagem.pontosDesvantagens
    val limiteDesvantagens get() = personagem.limiteDesvantagens
    val vantagensFiltradas get() = searchDelegate.filtrarVantagens()
    val desvantagensFiltradas get() = searchDelegate.filtrarDesvantagens()
    val periciasFiltradas get() = searchDelegate.filtrarPericias()
    val magiasFiltradas get() = searchDelegate.filtrarMagias()
    val tecnicasFiltradas get() = searchDelegate.filtrarTecnicas()
    val todasEscolasMagia get() = todasEscolasMagiaCache
    val todasClassesMagia get() = todasClassesMagiaCache
    val tecnicasCatalogo get() = dataRepository.tecnicasCatalogo
    val periciasSuplementaresArtesMarciais get() = dataRepository.periciasSuplementares
    val modificadoresGerais get() = dataRepository.modificadoresGerais
    val tagsArmadurasEquipamentos get() = equipmentDelegate.tagsArmaduras()
    val errosCargaCatalogos get() = dataRepository.getCatalogLoadErrors()
}
