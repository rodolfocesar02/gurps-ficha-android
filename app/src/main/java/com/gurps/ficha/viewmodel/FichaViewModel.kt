package com.gurps.ficha.viewmodel

import com.gurps.ficha.domain.rules.poderes.HabilidadesDoPoder

import com.gurps.ficha.domain.rules.LocalAtaque

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
    private val _personagem = mutableStateOf(Personagem())
    var personagem: Personagem
        get() = _personagem.value
        private set(value) {
            if (estaCarregando) {
                _personagem.value = value
            } else {
                val old = _personagem.value
                _personagem.value = historyDelegate.diffAndLog(old, value)
            }
        }

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
    private val historyDelegate = FichaHistoryDelegate()
    
    // Novos Delegados (Refatoração Lote 15)
    private val iaDelegate = FichaIADelegate(this, dataRepository, viewModelScope, application)
    // Lote 354 (Saga A5): delegate do modo Saga (Narrador + campanhas + rolagem interativa)
    private val sagaDelegate = FichaSagaDelegate(this, dataRepository, viewModelScope, application)
    private val socialDelegate = FichaSocialDelegate(networkDelegate, configPrefs, viewModelScope)
    private val notesDelegate = com.gurps.ficha.viewmodel.delegates.FichaNotesDelegate()
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

    // Lote MESA-7: o segundo destino das rolagens, ao lado do Discord.
    val destinoDaRolagem get() = socialDelegate.destinoDaRolagem
    val mesaEndereco get() = socialDelegate.mesaEndereco
    val mesaToken get() = socialDelegate.mesaToken
    val oQueFaltaNoDestino get() = socialDelegate.oQueFaltaNoDestino
    fun escolherDestinoDaRolagem(d: com.gurps.ficha.domain.rules.DestinoDaRolagem) =
        socialDelegate.escolherDestino(d)
    fun configurarMesa(endereco: String?, token: String?) =
        socialDelegate.configurarMesa(endereco, token)
    suspend fun testarMesa() = socialDelegate.testarMesa()
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
    /** Lote HEX-2: flag "modo tático em hexágonos" da campanha atual (default false — modo faixas). */
    val sagaModoTaticoHex get() = sagaDelegate.configAtiva.modoTaticoHex
    /** Lote HEX-7: flag "render 3D SceneView" da campanha atual (efetivo só se `sagaModoTaticoHex` também). */
    val sagaModoTaticoHex3D get() = sagaDelegate.configAtiva.modoTaticoHex3D
    // Lote TOK-4: grade tática dirigida pelo combate REAL.
    val sagaEstadoTatico get() = sagaDelegate.combate.estadoTatico
    val sagaTokensTaticos get() = sagaDelegate.combate.tokensTaticos
    var sagaAvisoTatico
        get() = sagaDelegate.combate.avisoTatico
        set(v) { sagaDelegate.combate.avisoTatico = v }
    fun sagaHexesAlcancaveis() = sagaDelegate.combate.hexesAlcancaveisHeroi()
    fun sagaAoTocarHexTatico(hex: com.gurps.ficha.domain.combat.hex.HexCoord) = sagaDelegate.combate.aoTocarHexTatico(hex)
    fun sagaLimparSelecaoTatica() = sagaDelegate.combate.limparSelecaoTatica() // Lote TOK-6b-2: fecha o menu do token
    /** Lote HEX-FACING: vira o herói (ação LIVRE — MB p.387/388; não gasta o turno). */
    fun sagaCombateVirar(direcao: com.gurps.ficha.domain.combat.hex.Direcao) =
        sagaDelegate.combate.heroiVirar(direcao)
    // Lote HEX-FACING-2 (MB p.388): virada de FIM DE MOVIMENTO — o turno espera o jogador escolher
    // para onde fica olhando antes de os inimigos agirem.
    val sagaViradaFinalPendente get() = sagaDelegate.combate.viradaFinalPendente
    fun sagaDirecoesViradaFinal() = sagaDelegate.combate.direcoesDaViradaFinal()
    fun sagaConcluirViradaFinal(direcao: com.gurps.ficha.domain.combat.hex.Direcao?) =
        sagaDelegate.combate.concluirViradaFinal(direcao)
    fun sagaCombateAtacar(alvoId: String, manobra: com.gurps.ficha.domain.combat.Manobra, local: com.gurps.ficha.domain.rules.LocalAtaque, modo: com.gurps.ficha.domain.combat.AtaqueTotalModo = com.gurps.ficha.domain.combat.AtaqueTotalModo.DETERMINADO, enganoso: Int = 0, telegrafico: Boolean = false) =
        sagaDelegate.combate.heroiAtaca(alvoId, manobra, local, modo, enganoso, telegrafico)
    fun sagaCombateAtaqueDedicado(alvoId: String, local: com.gurps.ficha.domain.rules.LocalAtaque, dedicadoModo: com.gurps.ficha.domain.combat.DedicadoModo) = // Lote PONTE-4
        sagaDelegate.combate.heroiAtaca(alvoId, com.gurps.ficha.domain.combat.Manobra.ATAQUE_DEDICADO, local, dedicadoModo = dedicadoModo)
    fun sagaCombateAtaqueDefensivo(alvoId: String, local: com.gurps.ficha.domain.rules.LocalAtaque, benefDefensivo: com.gurps.ficha.domain.combat.CombatResolver.TipoDefesa?) = // Lote PONTE-4
        sagaDelegate.combate.heroiAtaca(alvoId, com.gurps.ficha.domain.combat.Manobra.ATAQUE_DEFENSIVO, local, benefDefensivo = benefDefensivo)
    fun sagaCombateAtacarDuplo(alvoId: String, local: com.gurps.ficha.domain.rules.LocalAtaque, offHandIndex: Int) =
        sagaDelegate.combate.heroiAtaqueDuplo(alvoId, local, offHandIndex)
    fun sagaCombateMover(alvoId: String?, afastar: Boolean, metros: Int) = sagaDelegate.combate.heroiMove(alvoId, afastar, metros)
    fun sagaCombateMoverEAtacar(alvoId: String, local: com.gurps.ficha.domain.rules.LocalAtaque) =
        sagaDelegate.combate.heroiMoverEAtacar(alvoId, local)
    fun sagaCombateAvaliar(alvoId: String) = sagaDelegate.combate.heroiAvaliar(alvoId)
    fun sagaCombateConjurar(magiaId: String, alvoId: String?, energiaInvestida: Int, pvQueimados: Int = 0,
                            danoPorEnergia: Boolean = false,
                            ritual: com.gurps.ficha.domain.magic.RitualDeConjuracao =
                                com.gurps.ficha.domain.magic.RitualDeConjuracao()) = // Lote MA-3a/3b/MA-6/C12
        sagaDelegate.combate.heroiConjurar(magiaId, alvoId, energiaInvestida, pvQueimados, danoPorEnergia, ritual)
    fun sagaCombateContinuarConjuracao() = sagaDelegate.combate.heroiContinuarConjuracao() // Lote MA-3c
    fun sagaCombateAbortarConjuracao() = sagaDelegate.combate.heroiAbortarConjuracao()      // Lote MA-3c
    val sagaMiraAreaPendente get() = sagaDelegate.combate.miraAreaPendente                  // Lote MA-3d
    fun sagaIniciarMiraArea(magiaId: String, raio: Int, energia: Int, pvQueimar: Int, causaDano: Boolean = false,
                            ritual: com.gurps.ficha.domain.magic.RitualDeConjuracao =
                                com.gurps.ficha.domain.magic.RitualDeConjuracao()) =
        sagaDelegate.combate.iniciarMiraArea(magiaId, raio, energia, pvQueimar, causaDano, ritual)
    /** Lote C11: zonas ativas (rótulo, raio) para a UI oferecer o encolhimento. */
    val sagaZonasAtivas get() = sagaDelegate.combate.zonasAtivasUi
    /** Lote C11: encolhe uma zona; expandir é recusado pela regra (Magia p.10). */
    fun sagaEncolherZona(nome: String, novoRaio: Int) = sagaDelegate.combate.encolherZona(nome, novoRaio)
    fun sagaCancelarMiraArea() = sagaDelegate.combate.cancelarMiraArea()
    /**
     * Lote TESTE-1: inicia um combate de TESTE direto do preview da grade tática, sem o Narrador.
     * Serve para o jogador exercitar as magias no aparelho. Cria [qtdGoblins] goblins a [distanciaM]m;
     * um deles é conjurador (Bola de Fogo) para testar a esquiva interativa contra magia de NPC.
     */
    fun sagaIniciarCombateTeste(qtdGoblins: Int = 2, distanciaM: Int = 5) {
        viewModelScope.launch {
            // Lote LIMPEZA-2: entrada PRÓPRIA do sandbox (a API de produção não carrega mais flag de
            // teste). O controller monta a grade, roda o loop e publica o estado sozinho — a UI reage
            // por `sagaCombateAtivo`/`sagaEstadoTatico`, sem refresh manual.
            sagaDelegate.combate.iniciarCombateSandbox(
                inimigos = listOf("goblin" to qtdGoblins),
                distanciaM = distanciaM,
                magiasDeclaradas = listOf("Bola de Fogo"),
            )
        }
    }
    /** Lote MEC-23: mágicas esperando o herói decidir se mantém (manter é OPCIONAL em GURPS). */
    val sagaManutencaoPendente: List<com.gurps.ficha.domain.combat.CombatSession.ManutencaoPendente>
        get() = sagaDelegate.combate.manutencaoPendente

    fun sagaResolverManutencao(magiaId: String, manter: Boolean) =
        sagaDelegate.combate.resolverManutencao(magiaId, manter)

    /** Lote TESTE-SANDBOX: motivo de o combate de teste não ter aberto (null = abriu). */
    val sagaAvisoSandbox: String?
        get() = sagaDelegate.combate.avisoSandbox

    fun sagaLimparAvisoSandbox() = sagaDelegate.combate.limparAvisoSandbox()

    /**
     * Lote TESTE-NPC: modo dos NPCs no combate de teste (Normal / Congelado / Boneco). Vale também
     * com a luta já em andamento — trocar não obriga a reiniciar.
     */
    val sagaModoTesteNpc: com.gurps.ficha.domain.combat.ModoTesteNpc
        get() = sagaDelegate.combate.modoTesteNpc

    fun sagaDefinirModoTesteNpc(modo: com.gurps.ficha.domain.combat.ModoTesteNpc) =
        sagaDelegate.combate.definirModoTesteNpc(modo)

    /** Lote MA-5: mana ambiente da cena atual (setada pelo Narrador via definir_cena). Alimenta a conjuração. */
    var sagaNivelMana: com.gurps.ficha.domain.magic.NivelMana = com.gurps.ficha.domain.magic.NivelMana.NORMAL
    fun sagaCombateEntregarToque(alvoId: String) = sagaDelegate.combate.heroiEntregarToque(alvoId)   // Lote MA-3d-2
    // Lote MEC-39 (P11): projétil carregado por vários turnos.
    fun sagaCarregarProjetil(magiaId: String, energia: Int) = sagaDelegate.combate.carregarProjetil(magiaId, energia)
    fun sagaAumentarProjetil(energia: Int) = sagaDelegate.combate.aumentarProjetil(energia)
    fun sagaArremessarProjetil(alvoId: String) = sagaDelegate.combate.arremessarProjetil(alvoId)
    fun sagaDissiparProjetil() = sagaDelegate.combate.dissiparProjetilCarregado()
    /** Lote MEC-46 (P1b): hexes cobertos por zona ativa (chuva/nuvem/gás) para pintar na grade. */
    val sagaHexesDeZona get() = sagaDelegate.combate.hexesDeZona
    fun sagaCombateDissiparToque() = sagaDelegate.combate.heroiDissiparToque()                        // Lote MA-3d-2
    fun sagaCombateApontar(alvoId: String, firmado: Boolean = false) = sagaDelegate.combate.heroiApontar(alvoId, firmado)
    fun sagaCombateFogoRetencao() = sagaDelegate.combate.heroiFogoRetencao()
    fun sagaCombateAguardar() = sagaDelegate.combate.heroiAguardar()
    fun sagaCombateGolpeRapido(alvoId: String, local: com.gurps.ficha.domain.rules.LocalAtaque) =
        sagaDelegate.combate.heroiGolpeRapido(alvoId, local)
    fun sagaCombateEncontrao(alvoId: String) = sagaDelegate.combate.heroiEncontrao(alvoId)
    fun sagaCombateEmpurrao(alvoId: String) = sagaDelegate.combate.heroiEmpurrao(alvoId)
    fun sagaCombateImobilizar(alvoId: String) = sagaDelegate.combate.heroiImobilizar(alvoId)
    fun sagaCombateEstrangular(alvoId: String) = sagaDelegate.combate.heroiEstrangular(alvoId)
    fun sagaCombateChaveMembro(alvoId: String, perna: Boolean = false) = sagaDelegate.combate.heroiChaveMembro(alvoId, perna) // Lote PONTE-1
    fun sagaCombateMataLeao(alvoId: String) = sagaDelegate.combate.heroiMataLeao(alvoId) // Lote PONTE-1
    fun sagaCombateFintar(alvoId: String) = sagaDelegate.combate.heroiFintar(alvoId)
    fun sagaCombateAgarrar(alvoId: String) = sagaDelegate.combate.heroiAgarrar(alvoId)
    fun sagaCombateDerrubar(alvoId: String) = sagaDelegate.combate.heroiDerrubar(alvoId)
    fun sagaCombateManobra(manobra: com.gurps.ficha.domain.combat.Manobra, novaPostura: com.gurps.ficha.domain.combat.Postura? = null) =
        sagaDelegate.combate.heroiManobra(manobra, novaPostura)
    fun sagaCombateDefesaTotal(modo: com.gurps.ficha.domain.combat.DefesaTotalModo, aumentadaEm: com.gurps.ficha.domain.combat.CombatResolver.TipoDefesa? = null) =
        sagaDelegate.combate.heroiDefesaTotal(modo, aumentadaEm)
    fun sagaCombateDefender(opcao: com.gurps.ficha.domain.combat.CombatResolver.OpcaoDefesa) = sagaDelegate.combate.escolherDefesa(opcao)
    fun sagaCombateSelecionarAtaque(indice: Int) = sagaDelegate.combate.selecionarAtaque(indice)
    fun sagaCombateSacarArma(indice: Int) = sagaDelegate.combate.sacarArma(indice)
    fun sagaCombateEncerrar() = sagaDelegate.combate.encerrarManual()
    fun sagaCombateDesvencilhar() = sagaDelegate.combate.heroiDesvencilhar() // Lote 422: herói preso se solta

    // Efeitos da Saga na ficha do herói (Lote 366 / B8) — mutam e SALVAM a ficha carregada.
    /**
     * O XP que o Mestre deu, digitado pelo jogador (Lote GER-1).
     *
     * ⚠️ **Define**, nao soma — ao contrario do `sagaConcederXp`, que e o Narrador
     * premiando e por isso acumula. Aqui o jogador esta corrigindo um numero na
     * ficha, e somar faria o campo crescer sozinho a cada toque.
     */
    fun atualizarXpGanhos(valor: Int) {
        personagem = personagem.copy(xpGanhos = valor.coerceAtLeast(0)); salvarFicha()
    }

    /** O NT da campanha (MB p.29). Lote GER-1. */
    fun atualizarNivelTecnologico(valor: Int) {
        personagem = personagem.copy(nivelTecnologico = valor.coerceIn(0, 12)); salvarFicha()
    }

    fun sagaConcederXp(pts: Int): Int {
        personagem = personagem.copy(xpGanhos = (personagem.xpGanhos + pts).coerceAtLeast(0)); salvarFicha(); return personagem.xpGanhos
    }
    fun sagaDefinirPvAtual(v: Int) { atualizarPontosVidaRolagemAtual(v); salvarFicha() }
    fun sagaDefinirPfAtual(v: Int) { atualizarPontosFadigaRolagemAtual(v); salvarFicha() }
    /** Lote 423: persiste o sangramento ativo do herói entre cenas/combates (MB p.420). */
    fun sagaDefinirSangramento(penalidadeLocal: Int, intervaloSeg: Int) {
        personagem = personagem.copy(sagaSangrando = true,
            sagaSangramentoPenalidadeLocal = penalidadeLocal, sagaSangramentoIntervaloSeg = intervaloSeg)
        salvarFicha()
    }
    /** Lote 423: estanca o sangramento persistido (cura/descanso). Retorna true se havia sangramento. */
    fun sagaLimparSangramento(): Boolean {
        if (!personagem.sagaSangrando) return false
        personagem = personagem.copy(sagaSangrando = false,
            sagaSangramentoPenalidadeLocal = null, sagaSangramentoIntervaloSeg = null)
        salvarFicha(); return true
    }
    fun sagaAdicionarItem(nome: String, qtd: Int) {
        if (nome.isBlank()) return
        adicionarEquipamento(com.gurps.ficha.model.Equipamento(nome = nome, quantidade = qtd.coerceAtLeast(1))); salvarFicha()
    }

    /**
     * Saga (item 1 do teste de batalha): o Narrador TIRA/DEVOLVE/DESTRÓI equipamento do herói para que o
     * combate respeite a narrativa (desarmado/capturado). operacao: "confiscar" (marca indisponível, mas
     * recuperável) | "devolver" (volta a usar) | "destruir" (some da ficha de vez). itemNome casa por nome
     * (igual/contém) ou por categoria ("armas" / "armaduras" / "tudo"). Persiste na ficha. Retorna os nomes
     * afetados (vazio = nada casou). O combate ignora ARMA/ARMADURA confiscada (ver construirAtaques/rdHeroi).
     */
    fun sagaGerirEquipamento(itemNome: String, operacao: String): List<String> {
        val alvoNorm = com.gurps.ficha.domain.filters.CatalogFilters.normalizarBusca(itemNome)
        fun ehAlvo(e: com.gurps.ficha.model.Equipamento): Boolean {
            val n = com.gurps.ficha.domain.filters.CatalogFilters.normalizarBusca(e.nome)
            return when (alvoNorm) {
                "armas", "arma", "todas as armas" -> e.tipo == com.gurps.ficha.model.TipoEquipamento.ARMA
                "armaduras", "armadura" -> e.tipo == com.gurps.ficha.model.TipoEquipamento.ARMADURA
                "tudo", "tudo que carrega", "equipamento", "equipamentos" -> e.tipo in listOf(
                    com.gurps.ficha.model.TipoEquipamento.ARMA, com.gurps.ficha.model.TipoEquipamento.ARMADURA,
                    com.gurps.ficha.model.TipoEquipamento.ESCUDO, com.gurps.ficha.model.TipoEquipamento.CAPA
                )
                else -> n.isNotBlank() && alvoNorm.isNotBlank() && (n == alvoNorm || n.contains(alvoNorm) || alvoNorm.contains(n))
            }
        }
        val afetados = personagem.equipamentos.filter { ehAlvo(it) }.map { it.nome }
        if (afetados.isEmpty()) return emptyList()
        val novaLista = when (operacao.lowercase().trim()) {
            "destruir", "descartar" -> personagem.equipamentos.filterNot { ehAlvo(it) }
            "devolver", "equipar" -> personagem.equipamentos.map { if (ehAlvo(it)) it.copy(confiscado = false) else it }
            else -> personagem.equipamentos.map { if (ehAlvo(it)) it.copy(confiscado = true) else it } // confiscar/desequipar/tirar
        }
        personagem = personagem.copy(equipamentos = novaLista)
        ajustarEscudo()
        salvarFicha()
        return afetados
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
            // Depois do auto-save restaurado: só aqui `personagem` já aponta para
            // o retrato em uso, e a faxina precisa dessa informação para não
            // apagá-lo.
            faxinaDeRetratosOrfaos()
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
    fun aplicarPainelDeFadiga(quantidades: Map<String, Int>, pfNovo: Int, pvPerdidos: Int) {
        personagem = attributeDelegate.aplicarPainelDeFadiga(personagem, quantidades, pfNovo, pvPerdidos); salvarFicha()
    }
    fun aplicarFerimentoPorLocal(pvNovo: Int, pfNovo: Int, guardadas: Set<String>) {
        personagem = attributeDelegate.aplicarFerimentoPorLocal(personagem, pvNovo, pfNovo, guardadas); salvarFicha()
    }
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
        // Lote POD-5: uma rota só (ver `comModificadorDoPoder`).
        val poder = personagem.poderes.find { it.id == v.poderId }
        val vToSave = v.copy(modificadores = comModificadorDoPoder(v.modificadores, poder))

        atualizarVantagensComConfirmacao(traitDelegate.atualizarVantagem(personagem, index, vToSave))
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
        // 🔴 POD-17: desvantagem nunca recebe o modificador de poder (p.28).
        val dToSave = d.copy(modificadores = comModificadorDoPoder(d.modificadores, null))

        personagem = personagem.copy(desvantagens = traitDelegate.atualizarDesvantagem(personagem, index, dToSave))
        salvarFicha()
    }

    // === QUALIDADES / PECULIARIDADES ===
    fun adicionarQualidade(q: String) { personagem = personagem.copy(qualidades = traitDelegate.adicionarQualidade(personagem, q)); salvarFicha() }
    fun removerQualidade(index: Int) { personagem = personagem.copy(qualidades = traitDelegate.removerQualidade(personagem, index)); salvarFicha() }
    fun atualizarQualidade(index: Int, q: String) { personagem = personagem.copy(qualidades = traitDelegate.atualizarQualidade(personagem, index, q)); salvarFicha() }
    fun adicionarPeculiaridade(p: String) { personagem = personagem.copy(peculiaridades = traitDelegate.adicionarPeculiaridade(personagem, p)); salvarFicha() }
    // ==========================================
    // PODERES
    // ==========================================

    fun adicionarPoder(poder: Poder) {
        personagem = personagem.copy(poderes = traitDelegate.adicionarPoder(personagem, poder))
        salvarFicha()
    }

    // ── Lote POD-5: uma rota só para o modificador de poder ────────────
    //
    // 🔴 Havia QUATRO cópias desta lógica, e cada uma esquecia um pedaço
    // diferente:
    //   · `atualizarPoder` só mexia nas vantagens — havia até uma tarefa
    //     pendente escrita em comentário, pedindo o mesmo para o outro lado;
    //   · `removerPoder` idem — desvantagem ligada a poder apagado ficava
    //     pagando `Mod. de Poder: X` de um poder que não existe mais;
    //   · `vincularDesvantagemPoder` **não aplicava o modificador nenhum**:
    //     ligar a desvantagem ao poder não mudava o custo dela.
    //
    // O defeito morava na diferença entre as rotas. Agora é uma.

    /** Troca o `Mod. de Poder:` de uma lista de modificadores pelo do [poder]. */
    private fun comModificadorDoPoder(
        mods: List<ModificadorSelecao>,
        poder: Poder?
    ): List<ModificadorSelecao> {
        val limpos = mods.filter { !it.nome.startsWith(HabilidadesDoPoder.PREFIXO_DO_MODIFICADOR) }
        if (poder == null || poder.modificadorDePoder == 0) return limpos
        return limpos + ModificadorSelecao(
            id = HabilidadesDoPoder.idDoModificador(poder.id),
            // 🔴 POD-15: o nome vem do livro quando ele dá um — "Telepático",
            // e não "Telepatia". O valor é o mesmo; o nome é o que vai na ficha.
            nome = HabilidadesDoPoder.nomeDoModificador(
                poder.nomeDoModificador.ifBlank { poder.nome }
            ),
            valor = poder.modificadorDePoder
        )
    }

    /**
     * Reaplica o vínculo nas duas listas de traços de uma vez.
     * `poder = null` desliga: tira o `poderId` **e** o modificador injetado.
     */
    private fun reaplicarPoderNosTracos(idDoPoder: String, poder: Poder?) {
        personagem = personagem.copy(
            vantagens = personagem.vantagens.map { v ->
                if (v.poderId != idDoPoder) v
                else v.copy(
                    poderId = if (poder == null) null else v.poderId,
                    modificadores = comModificadorDoPoder(v.modificadores, poder)
                )
            },
            // 🔴 POD-17: a desvantagem NUNCA recebe o modificador de poder.
            // "Ele aplica-se a todas as habilidades do poder (mas não ao seu
            // Talento, **desvantagens exigidas**, ou Antecedente Incomum)"
            // (Poderes p.28). No POD-5 eu tratei essa assimetria como defeito e
            // "consertei" — a assimetria original estava certa.
            //
            // O que a desvantagem perde ao apagar o poder é só o VÍNCULO, e
            // qualquer modificador injetado por engano na versão anterior.
            desvantagens = personagem.desvantagens.map { d ->
                if (d.poderId != idDoPoder) d
                else d.copy(
                    poderId = if (poder == null) null else d.poderId,
                    modificadores = comModificadorDoPoder(d.modificadores, null)
                )
            }
        )
    }

    fun atualizarPoder(index: Int, poder: Poder) {
        personagem = personagem.copy(poderes = traitDelegate.atualizarPoder(personagem, index, poder))
        reaplicarPoderNosTracos(poder.id, poder)
        salvarFicha()
    }

    fun removerPoder(index: Int) {
        val oldPoder = personagem.poderes.getOrNull(index)
        personagem = personagem.copy(poderes = traitDelegate.removerPoder(personagem, index))
        // ⚠️ A vantagem NÃO é apagada junto: ela existia antes do poder e
        // continua existindo. O que sai é o vínculo e o percentual injetado.
        if (oldPoder != null) reaplicarPoderNosTracos(oldPoder.id, null)
        salvarFicha()
    }

    fun vincularVantagemPoder(indexVantagem: Int, poderId: String?) {
        personagem = personagem.copy(
            vantagens = traitDelegate.vincularVantagemPoder(personagem, indexVantagem, poderId)
        )
        val poder = personagem.poderes.find { it.id == poderId }
        personagem = personagem.copy(vantagens = personagem.vantagens.mapIndexed { idx, v ->
            if (idx == indexVantagem) v.copy(modificadores = comModificadorDoPoder(v.modificadores, poder)) else v
        })
        salvarFicha()
    }

    /**
     * Liga a desvantagem ao poder — **sem** aplicar o modificador de poder.
     *
     * 🔴 POD-17. A desvantagem exigida é o que *gera* parte do modificador
     * (Poderes p.23); aplicá-lo de volta nela seria cobrar duas vezes.
     */
    fun vincularDesvantagemPoder(indexDesvantagem: Int, poderId: String?) {
        personagem = personagem.copy(
            desvantagens = traitDelegate.vincularDesvantagemPoder(personagem, indexDesvantagem, poderId)
        )
        personagem = personagem.copy(desvantagens = personagem.desvantagens.mapIndexed { idx, d ->
            if (idx == indexDesvantagem) d.copy(modificadores = comModificadorDoPoder(d.modificadores, null)) else d
        })
        salvarFicha()
    }

    /**
     * Marca (ou desmarca) uma vantagem como **habilidade alternativa** do poder
     * a que ela pertence. Lote POD-6.
     *
     * ⚠️ Só faz sentido em vantagem já ligada a um poder — alternativa é sempre
     * alternativa **dentro de um poder**.
     */
    fun marcarAlternativa(indexVantagem: Int, alternativa: Boolean) {
        personagem = personagem.copy(vantagens = personagem.vantagens.mapIndexed { i, v ->
            if (i == indexVantagem && v.poderId != null) v.copy(alternativaDoPoder = alternativa) else v
        })
        salvarFicha()
    }

    /**
     * **Compra uma habilidade PARA o poder** — Lote POD-14.
     *
     * 🔴 O fluxo estava invertido. Até aqui era preciso criar a vantagem na aba
     * Traços e **depois** voltar ao poder para ligá-la. O livro faz o contrário:
     *
     * > *"O personagem pode usar seus pontos de personagem adquiridos para
     * > comprar **novas habilidades de Telepatia**."* (MB p.257)
     *
     * A vantagem nasce já ligada ao poder e já com o modificador aplicado.
     *
     * ⚠️ Ela continua sendo uma vantagem **do personagem**, na lista de Traços —
     * o poder não a possui. Diferente do `ModeloRacial`, que possui os traços
     * dele. Aqui muda o caminho de compra, não a propriedade.
     */
    fun adicionarHabilidadeAoPoder(vantagem: VantagemSelecionada, poder: Poder) {
        val comVinculo = vantagem.copy(
            poderId = poder.id,
            modificadores = comModificadorDoPoder(vantagem.modificadores, poder)
        )
        atualizarVantagensComConfirmacao(personagem.vantagens + comVinculo)
        salvarFicha()
    }

    /** O resumo das habilidades de um poder, para a tela do POD-5. */
    fun habilidadesDoPoder(poder: Poder): HabilidadesDoPoder.Resumo =
        HabilidadesDoPoder.resumir(
            idDoPoder = poder.id,
            vantagens = personagem.vantagens.map { Triple(it.nome, it.custoFinal, it.poderId) },
            desvantagens = personagem.desvantagens.map { Triple(it.nome, it.custoFinal, it.poderId) },
            nivelDeTalento = poder.nivelTalento,
            custoDoTalento = poder.custoTotalTalento
        )

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

    // === PERSISTÊNCIA E LOG ===
    fun limparHistoricoLog() {
        personagem = historyDelegate.limparHistorico(personagem)
        salvarFicha()
    }
    
    fun exportarHistoricoParaTxt(uri: android.net.Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    val txt = personagem.historicoLog.joinToString("\n") { 
                        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                        val date = sdf.format(java.util.Date(it.timestamp))
                        "[$date] ${it.descricao}"
                    }
                    output.write(txt.toByteArray(Charsets.UTF_8))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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
                    // 🔴 E para a Mesa também, quando é ela o destino.
                    //
                    // O Discord já recebia a imagem aqui; a Mesa não, e por isso
                    // a rolagem chegava lá com a cara do personagem e aqui como
                    // texto pelado. A mesma imagem, os dois destinos.
                    socialDelegate.enviarRetratoParaAMesa(nome, dataUri)
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
                    personagem = ressincronizarMagiasComCatalogo(it) // MEC-44
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
    /**
     * Lote MEC-44: ressincroniza as magias da FICHA com o CATÁLOGO ao carregar.
     *
     * `MagiaSelecionada` guarda uma **cópia** de `classe`/`energia`/`tempoOperacao`, tirada quando a
     * magia foi adicionada. Toda correção de catálogo (as classes do D1, o custo da Bola de
     * Relâmpagos) ficava presa: a ABA MAGIAS seguia mostrando o valor velho — o usuário viu a Bola
     * de Relâmpagos como "Comum" na lista e como "Projétil" no combate, ao mesmo tempo.
     *
     * O MEC-42/43 resolveu isso só no caminho de COMBATE; aqui a ficha inteira passa a refletir o
     * catálogo. Busca por id e, falhando, pelo NOME normalizado (fichas antigas têm id de outro
     * esquema). Magia que o catálogo não conhece (caseira) fica intacta.
     */
    private fun ressincronizarMagiasComCatalogo(p: Personagem): Personagem {
        if (p.magias.isEmpty()) return p
        fun chave(x: String?) = (x ?: "").lowercase()
            .replace(Regex("[àáâãä]"), "a").replace(Regex("[éêë]"), "e").replace(Regex("[íî]"), "i")
            .replace(Regex("[óôõö]"), "o").replace(Regex("[úû]"), "u").replace("ç", "c")
            .replace(Regex("[^a-z0-9]"), "")
        val porId = dataRepository.magias.associateBy { it.id }
        val porNome = dataRepository.magias.associateBy { chave(it.nome) }
        var mudou = 0
        val novas = p.magias.map { m ->
            val def = porId[m.definicaoId] ?: porNome[chave(m.nome)] ?: return@map m
            val nova = m.copy(
                classe = def.classe?.takeIf { it.isNotBlank() } ?: m.classe,
                energia = def.energia?.takeIf { it.isNotBlank() } ?: m.energia,
                tempoOperacao = def.tempoOperacao?.takeIf { it.isNotBlank() } ?: m.tempoOperacao,
            )
            if (nova.classe != m.classe || nova.energia != m.energia) mudou++
            nova
        }
        if (mudou > 0) {
            com.gurps.ficha.domain.combat.SagaLog.mecanica(
                "ficha ressincronizada com o catálogo: $mudou magia(s) com classe/custo desatualizados")
        }
        return p.copy(magias = novas)
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
     * Apaga retratos que nenhuma ficha cita mais (ver
     * [com.gurps.ficha.data.storage.ImagemPersonagemStore.faxinaDeOrfaos]).
     * Roda uma vez por sessão, em segundo plano, e nunca derruba o app: se algo
     * falhar, a pasta só continua como está.
     */
    private suspend fun faxinaDeRetratosOrfaos() {
        runCatching {
            com.gurps.ficha.data.storage.ImagemPersonagemStore.faxinaDeOrfaos(
                context = getApplication(),
                jsonsDasFichas = fichaStorage.todosOsJsons(),
                emUso = listOf(
                    personagem.imagemPersonagemUri,
                    personagem.imagemPersonagemOriginalUri
                ).filter { it.isNotBlank() }
            )
        }
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
    /**
     * ⚠️ O retrato do personagem já foi para a Mesa nesta sessão?
     *
     * Guardado em memória: uma vez por arranque do app basta. Guardar em disco
     * mentiria no dia em que a sala fosse reiniciada e perdesse a imagem.
     */
    private val retratosJaEnviadosAMesa = mutableSetOf<String>()

    /**
     * 🔴 Garante que a Mesa tem a cara deste personagem ANTES da rolagem.
     *
     * A imagem só subia ao **salvar a ficha** — então quem já tinha salvo antes
     * de escolher a Mesa rolava a sessão inteira sem imagem, e não havia nada
     * na tela que explicasse por quê. O primeiro lançamento agora resolve isso
     * sozinho.
     *
     * ⚠️ Não segura a rolagem se falhar: rolagem sem imagem é um aborrecimento,
     * rolagem que não sai é a jogada perdida.
     */
    private suspend fun garantirRetratoNaMesa(nomeDoPersonagem: String) {
        if (nomeDoPersonagem.isBlank()) return
        if (!retratosJaEnviadosAMesa.add(nomeDoPersonagem)) return

        val imagemUri = personagem.imagemPersonagemUri
        if (imagemUri.isBlank()) return
        val dataUri = com.gurps.ficha.data.storage.ImagemPersonagemStore.bytesBase64(imagemUri)
            ?: return
        // Se não der certo, tenta de novo na próxima rolagem.
        if (!socialDelegate.enviarRetratoParaAMesa(nomeDoPersonagem, dataUri)) {
            retratosJaEnviadosAMesa.remove(nomeDoPersonagem)
        }
    }

    suspend fun enviarRolagemDiscord(p: DiscordRollPayload): RollDispatchStatus {
        garantirRetratoNaMesa(p.character)
        return socialDelegate.enviarRolagem(p)
    }

    // === UTILITÁRIOS / COMBATE ===
    fun calcularDanoArmaComSt(r: String?) = CharacterRules.resolverDanoPorSt(r?.trim().orEmpty(), personagem.forca)
    val nivelAptidaoMagica get() = MagicEngine.getNivelAptidaoMagicaParaMagia(personagem, null)
    /**
     * Gate da aba de Magias. É a PRESENÇA da vantagem, não o bônus: `nivelAptidaoMagica` vale 0 em
     * AM 0, e AM 0 é exatamente o pré-requisito para aprender magia (MB p.41). Com `> 0` a aba
     * sumia justamente de quem tinha comprado a vantagem no nível inicial.
     */
    val temAptidaoMagica get() = MagicEngine.possuiAptidaoMagica(personagem)
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

    fun atualizarBonusManualEsquiva(b: Int, nota: String = "") { personagem = combatDelegate.atualizarBonusManualEsquiva(personagem, b, nota) }
    fun atualizarPericiaApara(id: String?) { personagem = combatDelegate.atualizarPericiaApara(personagem, id) }
    fun atualizarBonusManualApara(b: Int, nota: String = "") { personagem = combatDelegate.atualizarBonusManualApara(personagem, b, nota) }
    fun atualizarPericiaBloqueio(id: String?) { personagem = combatDelegate.atualizarPericiaBloqueio(personagem, id); ajustarEscudo() }
    fun atualizarEscudoBloqueio(n: String?) { personagem = combatDelegate.atualizarEscudoBloqueio(personagem, n) }
    fun atualizarBonusManualBloqueio(b: Int, nota: String = "") { personagem = combatDelegate.atualizarBonusManualBloqueio(personagem, b, nota) }

    val armasEquipamentosFiltradas get() = searchDelegate.filtrarArmas(personagem.forca) { equipmentDelegate.categoriaArmaFogoParaFiltro(it) }
    val escudosEquipamentosFiltrados get() = searchDelegate.filtrarEscudos(personagem.forca)
    val armadurasEquipamentosFiltradas get() = searchDelegate.filtrarArmaduras()
    val pesoTotal get() = personagem.pesoTotalEquipamentos
    val custoTotalEquipamentos get() = personagem.custoTotalEquipamentos
    fun observacoesArmaPorEquipamento(e: Equipamento) = equipmentDelegate.observacoesArmaPorEquipamento(e)

    /**
     * As notas de rodapé da arma **já casadas com o texto do livro** (Lote EQP-3).
     *
     * ⚠️ Existia, e a lista de seleção não chamava: ela imprimia o
     * `arma.observacoes` cru, e o jogador via `Obs: [1]` — um número de rodapé
     * sem o rodapé. O resolvedor já estava pronto em `FichaEquipmentDelegate`,
     * usado pela ficha técnica e pelo cartão da arma equipada; só a lista tinha
     * caminho próprio.
     */
    fun observacoesArmaDoCatalogo(arma: ArmaCatalogoItem): List<String> =
        equipmentDelegate.observacoesArmaFormatadas(arma)
            .lineSequence().map { it.trim() }.filter { it.isNotBlank() }.toList()

    // ── Lote EQP-6: a armadura e o escudo ganham a mesma ficha da arma. ──
    // A montagem é regra pura; aqui só se entrega o que vem de fora dela (as
    // observações do catálogo e a ST de quem vai vestir).
    fun fichaTecnicaDaArmadura(armadura: ArmaduraCatalogoItem) =
        com.gurps.ficha.domain.rules.FichaTecnicaDaArmadura.de(
            armadura = armadura,
            observacoes = equipmentDelegate.observacoesArmaduraFormatadas(armadura)
        )

    fun fichaTecnicaDoEscudo(escudo: EscudoCatalogoItem) =
        com.gurps.ficha.domain.rules.FichaTecnicaDoEscudo.de(escudo, personagem.forca)

    /**
     * 🔴 **A ficha de um item que já está na ficha** (Lote EQP-7).
     *
     * Este é o **único** ponto de entrada do editor. Ele existe porque o
     * `EquipamentoDialog` perguntava só pela arma
     * (`armaDoCatalogoPara(...)?.let { fichaTecnicaDaArma(it) }`) e devolvia
     * `null` para armadura e escudo — então, depois do EQP-6, a mesma peça tinha
     * **duas caras**: ficha completa ao escolher, formulário pelado ao editar.
     *
     * ⚠️ É o mesmo defeito que o LAYOUT-7 já tinha consertado para a arma. Ele
     * voltou porque o conserto de lá foi feito **para a arma**, e não para "um
     * item do catálogo". Deixar o `when` aqui, num lugar só, é o que impede a
     * terceira vez.
     *
     * `null` = item criado à mão, ou de uma ficha cujo item não casa com nada.
     */
    fun fichaTecnicaDoItem(e: Equipamento): com.gurps.ficha.domain.rules.FichaDeEquipamento.Ficha? =
        when (e.tipo) {
            TipoEquipamento.ARMADURA ->
                equipmentDelegate.armaduraDoCatalogoPara(e)?.let { catalogo ->
                    com.gurps.ficha.domain.rules.FichaTecnicaDaArmadura.de(
                        armadura = catalogo,
                        // 🔴 Lote EQP-8: a PEÇA, não a entrada do catálogo. A
                        // Túnica do livro é `tronco, virilha`, 3 kg e $30; a
                        // metade que o jogador tem é `virilha`, 1,5 kg e $15.
                        peca = e,
                        observacoes = equipmentDelegate.observacoesArmaduraFormatadas(catalogo)
                    )
                }
            TipoEquipamento.ESCUDO ->
                equipmentDelegate.escudoDoCatalogoPara(e)?.let { catalogo ->
                    com.gurps.ficha.domain.rules.FichaTecnicaDoEscudo.de(catalogo, personagem.forca, e)
                }
            else ->
                equipmentDelegate.armaDoCatalogoPara(e)?.let { catalogo ->
                    com.gurps.ficha.domain.rules.FichaTecnicaDaArma.de(
                        arma = catalogo,
                        st = personagem.forca,
                        resolverDano = { calcularDanoArmaComSt(it) },
                        observacoes = observacoesArmaDoCatalogo(catalogo),
                        peca = e
                    )
                }
        }

    // ── Lote ARMA-2/3/4: a ficha técnica da arma, pronta para a tela. ──
    // A conta do dano e as observações do rodapé vêm de quem já sabe fazê-las;
    // a montagem das linhas é regra pura e mora em `FichaTecnicaDaArma`.
    fun fichaTecnicaDaArma(arma: ArmaCatalogoItem) = com.gurps.ficha.domain.rules.FichaTecnicaDaArma.de(
        arma = arma,
        st = personagem.forca,
        resolverDano = { calcularDanoArmaComSt(it) },
        observacoes = equipmentDelegate.observacoesArmaFormatadas(arma)
            .lineSequence().map { it.trim() }.filter { it.isNotBlank() }.toList()
    )

    /** O item do catálogo por trás de uma arma já equipada. Null = não casou. */
    fun armaDoCatalogoPara(e: Equipamento) = equipmentDelegate.armaDoCatalogoPara(e)

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

    // ==========================================
    // NOTAS DE JOGO
    // ==========================================
    fun salvarNotaDeJogo(nota: com.gurps.ficha.model.NotaDeJogo) {
        personagem = notesDelegate.salvarNota(personagem, nota)
    }

    fun excluirNotaDeJogo(notaId: String) {
        personagem = notesDelegate.excluirNota(personagem, notaId)
    }
    val tecnicasFiltradas get() = searchDelegate.filtrarTecnicas()
    val todasEscolasMagia get() = todasEscolasMagiaCache
    val todasClassesMagia get() = todasClassesMagiaCache
    val tecnicasCatalogo get() = dataRepository.tecnicasCatalogo
    val periciasSuplementaresArtesMarciais get() = dataRepository.periciasSuplementares
    val modificadoresGerais get() = dataRepository.modificadoresGerais
    val tagsArmadurasEquipamentos get() = equipmentDelegate.tagsArmaduras()
    val errosCargaCatalogos get() = dataRepository.getCatalogLoadErrors()
}
