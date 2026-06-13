package com.gurps.ficha.viewmodel.delegates

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.data.storage.CampanhaEntity
import com.gurps.ficha.data.storage.CenaEntity
import com.gurps.ficha.data.storage.ChatMessageEntity
import com.gurps.ficha.data.storage.ChatSessionEntity
import com.gurps.ficha.data.storage.FichaDatabase
import com.gurps.ficha.data.storage.SagaDao
import com.gurps.ficha.domain.MestreIANarradorUseCase
import com.gurps.ficha.domain.filters.CatalogFilters
import com.gurps.ficha.domain.magias.NexusArcanoModoAlvoAdapter
import com.gurps.ficha.domain.roll.CriticoRules
import com.gurps.ficha.domain.saga.NarradorToolExecutor
import com.gurps.ficha.viewmodel.FichaViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.random.Random

/** Um turno do feed da Saga. role: "jogador" | "narrador" | "sistema". */
data class SagaTurn(
    val role: String,
    val texto: String,
    val uid: String = java.util.UUID.randomUUID().toString()
)

/** Pedido de rolagem pendente — a UI mostra o card e resolve ao toque. */
data class SagaRollRequest(
    val pericia: String,
    val nhBase: Int,
    val mods: List<Pair<String, Int>>,
    val alvo: Int,
    val motivo: String,
    val deferred: CompletableDeferred<SagaRollResult>,
    val periciaEncontrada: Boolean
)

data class SagaRollResult(
    val dados: List<Int>,
    val soma: Int,
    val alvo: Int,
    val margem: Int,
    val sucesso: Boolean,
    val critico: String
)

/**
 * Lote 354 (Saga A5): delegate do modo Saga. Dono do estado observável da aba,
 * da ponte de rolagem interativa (implementa NarradorToolExecutor.RollBridge) e
 * da persistência dos turnos (tabelas de chat, sessão por campanha — sem migração).
 */
class FichaSagaDelegate(
    private val viewModel: FichaViewModel,
    private val repository: DataRepository,
    private val scope: CoroutineScope,
    private val context: Context?
) : NarradorToolExecutor.RollBridge {

    private val sagaDao: SagaDao? = context?.let { FichaDatabase.getInstance(it).sagaDao() }
    private val chatDao = context?.let { FichaDatabase.getInstance(it).chatHistoryDao() }
    private val narrador by lazy { MestreIANarradorUseCase(viewModel, repository, context) }

    private val executor: NarradorToolExecutor by lazy {
        val nexus = NexusArcanoModoAlvoAdapter(repository.magias)
        val forjador = com.gurps.ficha.domain.tools.ForjadorToolExecutor(viewModel, repository, nexus, context)
        NarradorToolExecutor(sagaDao, repository, forjador, this)
    }

    // ── Estado observável (lido pela TabSaga via getters no ViewModel) ──
    var campanhas by mutableStateOf<List<CampanhaEntity>>(emptyList()); private set
    var campanhaAtiva by mutableStateOf<CampanhaEntity?>(null); private set
    var cenaAtiva by mutableStateOf<CenaEntity?>(null); private set
    var feed by mutableStateOf<List<SagaTurn>>(emptyList()); private set
    var rolagemPendente by mutableStateOf<SagaRollRequest?>(null); private set
    var fase by mutableStateOf(""); private set
    var processando by mutableStateOf(false); private set

    private var sessionIdAtual: Long? = null

    fun carregarCampanhas() {
        val dao = sagaDao ?: return
        scope.launch {
            campanhas = dao.listarCampanhas()
        }
    }

    fun criarCampanha(nome: String, cenarioId: String = "fendaverso") {
        val dao = sagaDao ?: return
        scope.launch {
            val personagemId = viewModel.nomeFichaAtual ?: viewModel.personagem.nome.ifBlank { "heroi" }
            val id = dao.inserirCampanha(
                CampanhaEntity(
                    nome = nome.ifBlank { "Nova Campanha" },
                    cenarioId = cenarioId,
                    personagemId = personagemId,
                    criadaEm = System.currentTimeMillis(),
                    seedMundo = Random.nextLong()
                )
            )
            val cenaId = dao.inserirCena(
                CenaEntity(campanhaId = id, indice = 1, titulo = "Início", bioma = "", humor = "")
            )
            val sessionId = chatDao?.insertSession(
                ChatSessionEntity(title = "saga#$id", lastUpdate = System.currentTimeMillis())
            )
            executor.campanhaId = id
            executor.cenaAtualId = cenaId
            sessionIdAtual = sessionId
            campanhaAtiva = dao.getCampanha(id)
            cenaAtiva = dao.cenaAberta(id)
            feed = emptyList()
            campanhas = dao.listarCampanhas()
        }
    }

    fun continuarCampanha(id: Long) {
        val dao = sagaDao ?: return
        scope.launch {
            val camp = dao.getCampanha(id) ?: return@launch
            campanhaAtiva = camp
            cenaAtiva = dao.cenaAberta(id) ?: run {
                val cid = dao.inserirCena(CenaEntity(campanhaId = id, indice = 1, titulo = "Início"))
                dao.cenaAberta(id)
            }
            executor.campanhaId = id
            executor.cenaAtualId = cenaAtiva?.id
            // Recupera (ou cria) a sessão de chat desta campanha e carrega o feed.
            val existente = chatDao?.getAllSessions()?.firstOrNull { it.title == "saga#$id" }
            sessionIdAtual = existente?.id ?: chatDao?.insertSession(
                ChatSessionEntity(title = "saga#$id", lastUpdate = System.currentTimeMillis())
            )
            val msgs = sessionIdAtual?.let { chatDao?.getMessagesForSession(it) } ?: emptyList()
            feed = msgs.map { SagaTurn(if (it.role == "user") "jogador" else "narrador", it.text) }
        }
    }

    fun sairDaCampanha() {
        campanhaAtiva = null
        cenaAtiva = null
        feed = emptyList()
        sessionIdAtual = null
    }

    fun enviarMensagem(texto: String) {
        val msg = texto.trim()
        if (msg.isBlank() || processando || campanhaAtiva == null) return
        feed = feed + SagaTurn("jogador", msg)
        persistirTurno("user", msg)
        processando = true
        scope.launch {
            try {
                val cenaResumo = cenaAtiva?.let { "${it.titulo} ${it.resumo}".trim() } ?: ""
                val historico = feed.takeLast(16).map {
                    (if (it.role == "jogador") "user" else "model") to it.texto
                }
                val r = narrador.narrar(
                    mensagemJogador = msg,
                    executor = executor,
                    cenaResumo = cenaResumo,
                    ultimosTurnos = historico,
                    onStatus = { fase = it }
                )
                feed = feed + SagaTurn("narrador", r.prosa)
                persistirTurno("model", r.prosa)
            } finally {
                processando = false
                fase = ""
            }
        }
    }

    private fun persistirTurno(role: String, texto: String) {
        val dao = chatDao ?: return
        val sid = sessionIdAtual ?: return
        scope.launch {
            dao.insertMessage(
                ChatMessageEntity(sessionId = sid, role = role, text = texto, timestamp = System.currentTimeMillis())
            )
            dao.updateSessionTimestamp(sid, System.currentTimeMillis())
        }
    }

    // ── RollBridge: pedir_rolagem suspende aqui até a UI tocar o dado ──
    override suspend fun pedirRolagem(pericia: String, mods: List<Pair<String, Int>>, motivo: String): String {
        val (nhBase, encontrada) = resolverNH(pericia)
        val somaMods = mods.sumOf { it.second }
        val alvo = (nhBase + somaMods).coerceAtLeast(3)
        val deferred = CompletableDeferred<SagaRollResult>()
        rolagemPendente = SagaRollRequest(pericia, nhBase, mods, alvo, motivo, deferred, encontrada)
        val resultado = deferred.await()
        rolagemPendente = null
        return org.json.JSONObject()
            .put("soma", resultado.soma)
            .put("alvo", resultado.alvo)
            .put("margem", resultado.margem)
            .put("resultado", if (resultado.sucesso) "sucesso" else "falha")
            .put("critico", resultado.critico)
            .put("dados", org.json.JSONArray(resultado.dados))
            .toString()
    }

    /** Chamado pela UI ao tocar o dado — mesmo caminho da TabRolagem (3d6 + CriticoRules). */
    fun rolarDadoPendente() {
        val req = rolagemPendente ?: return
        val d = List(3) { Random.nextInt(1, 7) }
        val soma = d.sum()
        val margem = req.alvo - soma
        val sucesso = soma <= req.alvo
        val critico = when (CriticoRules.classificar(soma, req.alvo)) {
            CriticoRules.ResultadoCritico.DECISIVO -> "decisivo"
            CriticoRules.ResultadoCritico.FALHA_CRITICA -> "falha_critica"
            CriticoRules.ResultadoCritico.NORMAL -> "normal"
        }
        // Registra o dado no feed para o jogador ver, e completa a ponte (libera o loop da IA).
        val resumo = "🎲 ${req.pericia}: rolou $soma vs alvo ${req.alvo} → " +
            (if (sucesso) "sucesso" else "falha") + " (margem ${kotlin.math.abs(margem)})" +
            when (critico) { "decisivo" -> " — DECISIVO!"; "falha_critica" -> " — FALHA CRÍTICA!"; else -> "" }
        feed = feed + SagaTurn("sistema", resumo)
        req.deferred.complete(SagaRollResult(d, soma, req.alvo, margem, sucesso, critico))
    }

    /** Resolve o NH (alvo base) de uma perícia/atributo pelo nome, na ficha atual. */
    private fun resolverNH(nome: String): Pair<Int, Boolean> {
        val p = viewModel.personagem
        val alvoNorm = CatalogFilters.normalizarBusca(nome)
        // 1) perícia (inclui as do modelo racial via periciasTotais)
        p.periciasTotais.firstOrNull {
            val n = CatalogFilters.normalizarBusca(it.nome)
            n == alvoNorm || n.contains(alvoNorm) || alvoNorm.contains(n)
        }?.let { return it.calcularNivel(p) to true }
        // 2) atributo por nome ou sigla
        val atributo = when {
            alvoNorm.contains("forca") || alvoNorm == "st" -> p.st
            alvoNorm.contains("destreza") || alvoNorm == "dx" -> p.dx
            alvoNorm.contains("inteligencia") || alvoNorm == "iq" -> p.iq
            alvoNorm.contains("vitalidade") || alvoNorm.contains("saude") || alvoNorm == "ht" -> p.ht
            alvoNorm.contains("percepcao") || alvoNorm == "per" -> p.percepcao
            alvoNorm.contains("vontade") || alvoNorm == "von" -> p.vontade
            else -> null
        }
        if (atributo != null) return atributo to true
        // 3) não encontrou: usa IQ como base neutra e sinaliza
        return p.iq to false
    }
}
