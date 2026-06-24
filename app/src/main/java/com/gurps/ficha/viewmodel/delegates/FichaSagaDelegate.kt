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
import com.gurps.ficha.domain.combat.CombatSession
import com.gurps.ficha.domain.combat.HitLocationRules
import com.gurps.ficha.domain.filters.CatalogFilters
import com.gurps.ficha.domain.saga.CampanhaConfig
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
) : NarradorToolExecutor.RollBridge, NarradorToolExecutor.CombatBridge {

    private val sagaDao: SagaDao? = context?.let { FichaDatabase.getInstance(it).sagaDao() }
    private val chatDao = context?.let { FichaDatabase.getInstance(it).chatHistoryDao() }
    private val narrador by lazy { MestreIANarradorUseCase(viewModel, repository, context) }

    private val executor: NarradorToolExecutor by lazy {
        val nexus = NexusArcanoModoAlvoAdapter(repository.magias)
        val forjador = com.gurps.ficha.domain.tools.ForjadorToolExecutor(viewModel, repository, nexus, context)
        NarradorToolExecutor(sagaDao, repository, forjador, this, this)
    }

    /**
     * Lote 365 (B7) / 366 (B8): controller do combate. As linhas factuais do motor entram no feed
     * como turnos "sistema" (efêmeros — a prosa narrada do B8 é o que persiste). Ao encerrar, o
     * Narrador narra o desfecho ([narrarFimDeCombate]).
     */
    val combate: SagaCombatController by lazy {
        SagaCombatController(viewModel, context, scope) { linhas ->
            linhas.forEach { feed = feed + SagaTurn("sistema", it) }
        }.also { it.onFim = { fim -> narrarFimDeCombate(fim) } }
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
    // Fim de combate que chegou enquanto um turno de texto ainda rodava (processando=true): fica pendente
    // e é narrado no finally de rodarTurno, senão a prosa de desfecho + XP seriam descartadas (revisão Frente 2).
    private var fimDeCombatePendente: SagaCombatController.CombatFim? = null

    fun carregarCampanhas() {
        val dao = sagaDao ?: return
        scope.launch {
            campanhas = dao.listarCampanhas()
        }
    }

    fun criarCampanha(nome: String, config: CampanhaConfig = CampanhaConfig(), cenarioId: String = "fendaverso") {
        val dao = sagaDao ?: return
        scope.launch {
            val personagemId = viewModel.nomeFichaAtual ?: viewModel.personagem.nome.ifBlank { "heroi" }
            val id = dao.inserirCampanha(
                CampanhaEntity(
                    nome = nome.ifBlank { "Nova Campanha" },
                    cenarioId = cenarioId,
                    personagemId = personagemId,
                    criadaEm = System.currentTimeMillis(),
                    seedMundo = Random.nextLong(),
                    configJson = config.toJson()
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
            // Lote 355 (B): Narrador abre a cena automaticamente (não deixa o jogador no escuro).
            abrirCenaInicial()
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

    /** Lote 356: exclui a campanha e tudo que pendura nela (cenas, fatos, estado, chat). */
    fun excluirCampanha(id: Long) {
        val dao = sagaDao ?: return
        scope.launch {
            val sessao = chatDao?.getAllSessions()?.firstOrNull { it.title == "saga#$id" }
            sessao?.id?.let { chatDao?.deleteFullSession(it) }
            dao.excluirCampanhaCompleta(id)
            if (campanhaAtiva?.id == id) sairDaCampanha()
            campanhas = dao.listarCampanhas()
        }
    }

    fun enviarMensagem(texto: String) {
        val msg = texto.trim()
        if (msg.isBlank() || processando || campanhaAtiva == null) return
        feed = feed + SagaTurn("jogador", msg)
        persistirTurno("user", msg)
        rodarTurno(msg)
    }

    /**
     * Lote 355 (B): cena de abertura automática. Ao criar a campanha, o Narrador
     * enquadra onde o herói está e oferece o 1º gancho — o jogador não começa perdido.
     * O prompt-semente NÃO vira bolha de jogador (instrução de sistema).
     */
    private fun abrirCenaInicial() {
        if (campanhaAtiva == null) return
        rodarTurno(
            "[ABERTURA DA CAMPANHA] Apresente a cena de abertura desta aventura solo: enquadre " +
            "onde o herói está agora e o que ele percebe ao redor (use o conceito da ficha dele), " +
            "chame definir_cena para fixar título/bioma/humor, e termine oferecendo a primeira " +
            "escolha ao jogador. NÃO peça rolagem nesta abertura."
        )
    }

    /** Roda um turno do Narrador (compartilhado por enviarMensagem e abrirCenaInicial). */
    private fun rodarTurno(mensagem: String) {
        if (processando) return
        processando = true
        scope.launch {
            try {
                val cenaResumo = cenaAtiva?.let { "${it.titulo} ${it.resumo}".trim() } ?: ""
                val historico = feed.takeLast(16).map {
                    (if (it.role == "jogador") "user" else "model") to it.texto
                }
                val configBloco = CampanhaConfig.fromJson(campanhaAtiva?.configJson).paraPromptBloco()
                val r = narrador.narrar(
                    mensagemJogador = mensagem,
                    executor = executor,
                    cenaResumo = cenaResumo,
                    ultimosTurnos = historico,
                    onStatus = { fase = it },
                    configBloco = configBloco
                )
                feed = feed + SagaTurn("narrador", r.prosa)
                persistirTurno("model", r.prosa)
                // Lote 355 (A): definir_cena pode ter mudado a cena — reflete no cabeçalho.
                campanhaAtiva?.let { cenaAtiva = sagaDao?.cenaAberta(it.id) }
            } finally {
                processando = false
                fase = ""
                // Drena um fim de combate que chegou no meio deste turno (ver fimDeCombatePendente).
                fimDeCombatePendente?.let { fimDeCombatePendente = null; narrarFimDeCombate(it) }
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

    // ── CombatBridge (Lote 366 / B8): tools do Narrador → motor de combate + ficha ──

    override suspend fun iniciarCombate(inimigos: List<Pair<String, Int>>, distanciaM: Int, surpresa: String): String =
        combate.iniciarCombate(inimigos, distanciaM, surpresa)

    override fun combateAtivo(): Boolean = combate.emCurso

    override fun acaoNpc(npcId: String, intencao: String, alvoId: String?, detalhes: String?): String {
        // Divergência (regra 12): a tática dos NPCs é dirigida pelo motor (NpcCombatBrain, B6) e
        // jogada na UI em tempo real. acao_npc devolve o ESTADO FACTUAL para o Narrador narrar,
        // em vez de dirigir o turno (o que conflitaria com a UI interativa do B7).
        val resumo = combate.resumoFactual() ?: return jsonErro("sem_combate_ativo", "Nenhum combate em andamento")
        return org.json.JSONObject()
            .put("ok", true)
            .put("estado", resumo)
            .put("instrucao", "Os NPCs agem automaticamente pelo motor de combate; narre com base neste estado factual, sem inventar números.")
            .toString()
    }

    override fun aplicarDano(alvoId: String?, dano: String, tipo: String, local: String?): String {
        if (tipo.lowercase().trim() == "fad") {
            // dano de fadiga → debita PF do herói
            val pf = (viewModel.personagem.pontosFadigaRolagemAtual ?: viewModel.personagem.pontosFadiga)
            val perda = CombatSession.rolarDano(dano)
            viewModel.sagaDefinirPfAtual((pf - perda).coerceAtLeast(0))
            return org.json.JSONObject().put("ok", true).put("recurso", "pf").put("perda", perda)
                .put("pf_atual", (pf - perda).coerceAtLeast(0)).toString()
        }
        val danoBase = CombatSession.rolarDano(dano)
        val tipoEnum = CombatSession.tipoDano(tipo)
        val localEnum = localDeString(local)
        // Em combate: aplica ao combatente nomeado (NPC ou herói).
        if (combate.emCurso) {
            val alvo = alvoId?.ifBlank { null } ?: "heroi"
            combate.aplicarDanoCombatente(alvo, danoBase, tipoEnum, localEnum)?.let { rel ->
                return org.json.JSONObject().put("ok", true).put("relatorio", rel).toString()
            }
        }
        // Fora de combate (armadilha, queda, veneno): aplica ao herói na ficha.
        val perfil = combate.perfilHeroi()
        val dano = HitLocationRules.aplicarDano(viewModel.personagem.pontosVida, danoBase, tipoEnum, localEnum, perfil.rd)
        val pvAtual = (viewModel.personagem.pontosVidaRolagemAtual ?: viewModel.personagem.pontosVida)
        val pvNovo = (pvAtual - dano.pvSubtrair).coerceAtLeast(0)
        viewModel.sagaDefinirPvAtual(pvNovo)
        return org.json.JSONObject().put("ok", true).put("relatorio", dano.texto)
            .put("pv_atual", pvNovo).put("pv_max", viewModel.personagem.pontosVida).toString()
    }

    override fun aplicarCondicao(alvoId: String?, condicao: String, operacao: String): String {
        val cond = condicaoDeString(condicao)
            ?: return jsonErro("condicao_desconhecida", "O motor não modela a condição '$condicao'; trate-a narrativamente.")
        val aplicar = operacao.lowercase().trim() != "remover"
        if (combate.emCurso) {
            val alvo = alvoId?.ifBlank { null } ?: "heroi"
            combate.aplicarCondicaoCombatente(alvo, cond, aplicar)?.let { rel ->
                return org.json.JSONObject().put("ok", true).put("relatorio", rel).toString()
            }
        }
        return org.json.JSONObject().put("ok", true)
            .put("nota", "Fora de combate a condição ${cond.rotulo} é só narrativa (o motor a aplica dentro do combate).")
            .toString()
    }

    override fun gastarRecurso(recurso: String, quantidade: Int, motivo: String, itemNome: String?): String {
        val p = viewModel.personagem
        return when (recurso.lowercase().trim()) {
            "pf" -> {
                // delta>0 restaura, delta<0 debita (quantidade>0 = gasto; quantidade NEGATIVA = cura/descanso).
                // Em combate o motor é a fonte da verdade do PF — roteia para lá (senão a cura seria sobrescrita).
                val delta = -quantidade
                if (combate.emCurso) combate.ajustarRecursoHeroiEmCombate("pf", delta, p.pontosFadiga)
                else viewModel.sagaDefinirPfAtual(((p.pontosFadigaRolagemAtual ?: p.pontosFadiga) + delta).coerceIn(0, p.pontosFadiga))
                val novo = (viewModel.personagem.pontosFadigaRolagemAtual ?: p.pontosFadiga)
                org.json.JSONObject().put("ok", true).put("recurso", "pf").put("pf_atual", novo).put("pf_max", p.pontosFadiga).toString()
            }
            "pv" -> {
                // delta>0 restaura, delta<0 debita. Em combate o motor controla o PV — roteia para lá. Item 5 do teste.
                val delta = -quantidade
                if (combate.emCurso) combate.ajustarRecursoHeroiEmCombate("pv", delta, p.pontosFadiga)
                else viewModel.sagaDefinirPvAtual(((p.pontosVidaRolagemAtual ?: p.pontosVida) + delta).coerceIn(0, p.pontosVida))
                val novo = (viewModel.personagem.pontosVidaRolagemAtual ?: p.pontosVida)
                org.json.JSONObject().put("ok", true).put("recurso", "pv").put("pv_atual", novo).put("pv_max", p.pontosVida).toString()
            }
            "dinheiro", "municao", "item" ->
                // Divergência documentada: a ficha não modela dinheiro/munição como recurso vivo;
                // o Narrador acompanha pela narrativa/`registrar_fato`. Itens de saque entram via combate.
                org.json.JSONObject().put("ok", true).put("recurso", recurso)
                    .put("nota", "Registrado narrativamente; a ficha não rastreia $recurso como número.").toString()
            else -> jsonErro("recurso_desconhecido", recurso)
        }
    }

    override fun concederXp(pontos: Int, motivo: String): String {
        val total = viewModel.sagaConcederXp(pontos)
        feed = feed + SagaTurn("sistema", "✨ +$pontos pontos de personagem (${motivo.ifBlank { "marco do arco" }}). Total ganho: $total.")
        return org.json.JSONObject().put("ok", true).put("xp_concedido", pontos).put("xp_total", total).toString()
    }

    override fun gerirEquipamento(itemNome: String, operacao: String): String {
        val afetados = viewModel.sagaGerirEquipamento(itemNome, operacao)
        if (afetados.isEmpty())
            return jsonErro("item_nao_encontrado", "Nenhum equipamento do herói casa com '$itemNome'. Veja os nomes exatos com inspecionar_personagem (seção equipamentos).")
        val op = operacao.lowercase().trim()
        val (verbo, icone) = when (op) {
            "devolver", "equipar" -> "devolvido(s)" to "🎒"
            "destruir", "descartar" -> "destruído(s)" to "🔥"
            else -> "confiscado(s)" to "⛓️"
        }
        feed = feed + SagaTurn("sistema", "$icone Equipamento $verbo: ${afetados.joinToString(", ")}.")
        return org.json.JSONObject().put("ok", true).put("operacao", op)
            .put("itens", org.json.JSONArray(afetados))
            .put("nota", "Itens $verbo na ficha: ${afetados.joinToString(", ")}. O combate já reflete (arma confiscada não ataca; armadura confiscada não dá RD).")
            .toString()
    }

    /** Fim de combate: o Narrador converte o relatório factual agregado em prosa (+ XP), sem inventar números. */
    private fun narrarFimDeCombate(fim: SagaCombatController.CombatFim) {
        // Se um turno de texto ainda roda (o golpe fatal pode ter vindo de uma tool nesse turno), enfileira:
        // rodarTurno está bloqueado por processando e descartaria esta narração. Será drenado no finally dele.
        if (processando) { fimDeCombatePendente = fim; return }
        val saque = if (fim.saque.isEmpty()) "nenhum espólio" else fim.saque.joinToString(", ")
        rodarTurno(
            "[FIM DE COMBATE] Resultado: ${fim.resultado}. Estado final factual:\n${fim.resumoFactual}\n" +
            "Espólio JÁ entregue à ficha do herói: $saque.\n" +
            "Narre o desfecho do combate em PROSA, fiel a estes números (NÃO invente PV nem dano). " +
            "Se foi um marco do arco ou houve bom proveito tático/interpretação, conceda pontos com conceder_xp. " +
            "Encerre devolvendo o controle ao jogador com um gancho para a próxima ação."
        )
    }

    private fun localDeString(local: String?): com.gurps.ficha.domain.combat.LocalAtaque {
        val n = CatalogFilters.normalizarBusca(local ?: "")
        return com.gurps.ficha.domain.combat.LocalAtaque.values().firstOrNull {
            CatalogFilters.normalizarBusca(it.rotulo) == n
        } ?: com.gurps.ficha.domain.combat.LocalAtaque.TORSO
    }

    private fun condicaoDeString(condicao: String): com.gurps.ficha.domain.combat.Condicao? {
        val n = CatalogFilters.normalizarBusca(condicao)
        return com.gurps.ficha.domain.combat.Condicao.values().firstOrNull {
            CatalogFilters.normalizarBusca(it.rotulo) == n || it.name.lowercase() == n
        }
    }

    private fun jsonErro(codigo: String, detalhe: String): String =
        org.json.JSONObject().put("erro", codigo).put("detalhe", detalhe).toString()
}
