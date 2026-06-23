package com.gurps.ficha.viewmodel.delegates

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.gurps.ficha.domain.combat.*
import com.gurps.ficha.domain.filters.CatalogFilters
import com.gurps.ficha.domain.loaders.BestiarioCatalogo
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.TipoEquipamento
import com.gurps.ficha.viewmodel.FichaViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.random.Random

/** Faixa de distância exibida no CombatTracker (mockup B7). */
enum class FaixaDistancia(val rotulo: String) {
    ENGAJADO("Engajado"), PERTO("Perto"), MEDIO("Médio"), LONGE("Longe"), EXTREMO("Extremo");
    companion object {
        fun de(metros: Int): FaixaDistancia = when {
            metros <= 1 -> ENGAJADO
            metros <= 3 -> PERTO
            metros <= 10 -> MEDIO
            metros <= 20 -> LONGE
            else -> EXTREMO
        }
    }
}

/** Snapshot imutável de um combatente para a UI (Compose não enxerga mutação in-place no Combatente). */
data class CombatenteUi(
    val id: String,
    val nome: String,
    val ehHeroi: Boolean,
    val pvAtual: Int,
    val pvMax: Int,
    val postura: String,
    val condicoes: List<String>,
    val distanciaM: Int,
    val faixa: FaixaDistancia,
    val vivo: Boolean
) {
    val fracaoPv: Float get() = if (pvMax <= 0) 0f else (pvAtual.toFloat() / pvMax).coerceIn(0f, 1f)
    /** Frase única para o TalkBack: "Goblin, faixa Médio, 8 metros, em pé, ferido". */
    val descricaoAcessivel: String get() = buildString {
        append(nome); append(", ")
        if (ehHeroi) append("você") else { append("faixa ${faixa.rotulo}, ${distanciaM} metros") }
        append(", "); append(postura)
        append(", "); append(when {
            !vivo -> "fora de combate"
            fracaoPv <= 0.33f -> "muito ferido"
            fracaoPv < 1f -> "ferido"
            else -> "ileso"
        })
    }
}

/** Estado completo do combate para a UI. */
data class CombatUiState(
    val rodada: Int,
    val combatentes: List<CombatenteUi>,
    val vezDoHeroi: Boolean,
    val manobrasHeroi: List<Manobra>,
    val alvos: List<CombatenteUi>,
    /** Alvos alcançáveis com Mover e Atacar (Lote 378): corpo-a-corpo = reach + Deslocamento; à distância = dentro do Máx. */
    val alvosMoverEAtacar: List<CombatenteUi>,
    val ataques: List<AtaqueHeroi>,
    val ataqueSelecionado: Int,
    val deslocamentoHeroi: Int,
    val posturaHeroi: String,
    val posturasAlcancaveis: List<Postura>,
    /** Ambidestria (Lote 377): zera o −4 da mão inábil no Ataque Total (Duplo). */
    val heroiAmbidestro: Boolean,
    val encerrado: Boolean,
    val resultado: ResultadoCombate?
) {
    val ataqueAtual: AtaqueHeroi? get() = ataques.getOrNull(ataqueSelecionado)
}

/** Card "Defenda-se!" pendente: o herói foi atacado e precisa escolher uma defesa. */
data class DefesaPendenteUi(
    val atacante: String,
    val descricaoAtaque: String,
    val opcoes: List<CombatResolver.OpcaoDefesa>,
    private val deferred: CompletableDeferred<CombatResolver.OpcaoDefesa>
) {
    fun escolher(opcao: CombatResolver.OpcaoDefesa) { deferred.complete(opcao) }
}

/**
 * Lote 365 (Saga B7): controller do combate — embrulha a [CombatSession] pura com estado Compose,
 * corrotinas e a ponte de defesa interativa ("Defenda-se!"). Lê o herói da ficha e devolve o PV ao
 * fim do combate. O Narrador conecta seus executores aqui no B8 (via [iniciarCombate]/[onTurnoNpc]).
 */
class SagaCombatController(
    private val viewModel: FichaViewModel,
    private val context: Context?,
    private val scope: CoroutineScope,
    /** Empurra cada linha factual nova para o feed da Saga (turnos "sistema"). */
    private val onLinhasNovas: (List<String>) -> Unit
) {
    private var sessao: CombatSession? = null
    private var logPublicado = 0
    private var finalizado = false

    var estado by mutableStateOf<CombatUiState?>(null); private set
    var defesaPendente by mutableStateOf<DefesaPendenteUi?>(null); private set

    /** Ataques utilizáveis do herói (armas empunhadas + desarmado) e o índice escolhido (Lote 368). */
    private var ataques: List<AtaqueHeroi> = emptyList()
    private var ataqueSelecionado: Int = 0

    /** Troca a arma EMPUNHADA sem custo (uso interno / arma já pronta). */
    fun selecionarAtaque(indice: Int) {
        if (indice in ataques.indices) { ataqueSelecionado = indice; atualizarEstado() }
    }

    /**
     * Lote 374: saca/prepara a arma [indice] para a mão. Com Saque Rápido é ação livre; senão é a
     * manobra Preparar e CONSOME o turno (MB p.366: para atacar, a arma precisa estar preparada).
     */
    fun sacarArma(indice: Int) {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        if (indice !in ataques.indices || indice == ataqueSelecionado) return
        ataqueSelecionado = indice
        s.prepararArmaEmpunhada() // Lote 398: sacar/empunhar uma arma re-prepara (a nova arma está pronta)
        val nome = ataques[indice].rotulo.substringBefore(" (").trim()
        if (temSaqueRapido(viewModel.personagem)) {
            s.log += "🤚 Você saca $nome rapidamente (Saque Rápido — ação livre)."
            publicarLog(); atualizarEstado() // não consome o turno
        } else {
            s.log += "🤚 Você saca $nome (Preparar — gasta o turno)."
            depoisDaAcaoDoHeroi() // Preparar consome o turno
        }
    }

    /** Saque Rápido (Fast-Draw) deixa sacar a arma como ação livre. */
    private fun temSaqueRapido(p: Personagem): Boolean = p.periciasTotais.any {
        CatalogFilters.normalizarBusca(it.nome).contains("saque rapido") ||
            it.definicaoId.lowercase().contains("saque")
    }

    /** Notificado quando o combate encerra — o delegate dispara a narração do desfecho (B8). */
    var onFim: ((CombatFim) -> Unit)? = null

    /** Resumo factual entregue ao Narrador no fim do combate. */
    data class CombatFim(
        val resultado: ResultadoCombate?,
        val resumoFactual: String,
        val logCompleto: List<String>,
        val saque: List<String>
    )

    val ativo: Boolean get() = sessao != null
    /** Combate AINDA em andamento (a UI joga). Difere de [ativo], que segue true no estado final. */
    val emCurso: Boolean get() = sessao?.let { !it.encerrado } ?: false

    fun resumoFactual(): String? = sessao?.resumo()

    /**
     * Abre um encontro. [inimigos] = pares (id do bestiário, quantidade). Devolve um resumo factual
     * (o Narrador transforma em prosa). Distâncias e surpresa preparam o estado inicial.
     */
    fun iniciarCombate(inimigos: List<Pair<String, Int>>, distanciaM: Int = 5, surpresa: String = "ninguem"): String {
        val ctx = context ?: return "sem_contexto"
        val p = viewModel.personagem
        val bestiario = BestiarioCatalogo.carregar(ctx)

        val heroiComb = Combatente(
            id = "heroi", nome = p.nome.ifBlank { "Herói" }, ehHeroi = true,
            dx = p.dx, velocidadeBasica = p.velocidadeBasica.toDouble(),
            deslocamento = p.deslocamentoAtual.coerceAtLeast(1), pvMax = p.pontosVida,
            pvAtual = (p.pontosVidaRolagemAtual ?: p.pontosVida),
            pfAtual = (p.pontosFadigaRolagemAtual ?: p.pontosFadiga)
        )

        val distancias = mutableMapOf<String, Int>()
        val combatentes = mutableListOf(heroiComb)
        var n = 0
        inimigos.forEach { (idOuConceito, qtd) ->
            val criatura = bestiario.get(idOuConceito.lowercase().trim())
            repeat(qtd.coerceIn(1, 12)) {
                n++
                val cid = "${idOuConceito.lowercase().trim()}_$n"
                val nome = if (qtd > 1) "${criatura?.nome ?: idOuConceito} $n" else (criatura?.nome ?: idOuConceito)
                val comb = criatura?.novoCombatente(cid, nome) ?: Combatente(
                    id = cid, nome = nome, dx = 10, velocidadeBasica = 5.0, deslocamento = 5, pvMax = 10,
                    stats = NpcStats(armaDano = "1d-1", armaTipo = "cont", armaNh = 10)
                )
                combatentes.add(comb)
                distancias[cid] = distanciaM.coerceAtLeast(1)
            }
        }

        val perfil = construirPerfilHeroi(p)
        val encounter = CombatEncounter(combatentes, distancias, seed = Random.nextLong())
        val s = CombatSession(encounter, perfil, Random.Default)
        if (surpresa == "inimigos") combatentes.filter { !it.ehHeroi }.forEach { it.condicoes.add(Condicao.SURPRESO) }
        if (surpresa == "heroi") heroiComb.condicoes.add(Condicao.SURPRESO)
        sessao = s
        logPublicado = 0
        finalizado = false
        ataques = construirAtaques(p)
        ataqueSelecionado = 0
        s.log += "⚔️ Combate iniciado: ${inimigos.joinToString(", ") { "${it.second}× ${it.first}" }} a ${distanciaM}m."
        scope.launch { rodarLoop() }
        return s.resumo()
    }

    /** Perfil de combate do herói montado da ficha ATUAL (o bridge usa p/ dano fora do loop). */
    fun perfilHeroi(): HeroiPerfilCombate = construirPerfilHeroi(viewModel.personagem)

    // ── Efeitos aplicados pelo Narrador (B8), fora do loop de turnos ────────────

    /** Aplica dano a um combatente vivo do encontro (NPC ou herói). Retorna relatório factual ou null. */
    fun aplicarDanoCombatente(alvoId: String, danoBase: Int, tipo: DanoTipo, local: LocalAtaque): String? {
        val s = sessao ?: return null
        val alvo = s.encounter.combatentes.firstOrNull { it.id == alvoId && it.vivo } ?: return null
        val ht = if (alvo.ehHeroi) s.heroiPerfil.ht else (alvo.stats?.ht ?: 10)
        val rd = if (alvo.ehHeroi) s.heroiPerfil.rd else (alvo.stats?.rd ?: 0)
        val dano = HitLocationRules.aplicarDano(alvo.pvMax, danoBase, tipo, local, rd, alvo.stats?.tolerancia ?: ToleranciaFerimentos.NORMAL)
        val fer = InjuryRules.ferir(alvo, dano.pvSubtrair, ht, Random.Default)
        val txt = "✴️ ${alvo.nome}: ${dano.texto} | ${fer.efeito}"
        s.log += txt
        if (alvo.ehHeroi) viewModel.sagaDefinirPvAtual(alvo.pvAtual.coerceAtLeast(0))
        s.reavaliarFim()
        publicarLog(); atualizarEstado()
        if (s.encerrado) finalizar()
        return txt
    }

    /** Aplica/remove uma condição de um combatente do encontro. Retorna relatório ou null. */
    fun aplicarCondicaoCombatente(alvoId: String, cond: Condicao, aplicar: Boolean): String? {
        val s = sessao ?: return null
        val alvo = s.encounter.combatentes.firstOrNull { it.id == alvoId } ?: return null
        if (aplicar) alvo.condicoes.add(cond) else alvo.condicoes.remove(cond)
        val txt = "• ${alvo.nome}: ${if (aplicar) "ganha" else "perde"} a condição ${cond.rotulo}."
        s.log += txt
        s.reavaliarFim()
        publicarLog(); atualizarEstado()
        if (s.encerrado) finalizar()
        return txt
    }

    // ── Ações do herói (a UI chama) ──────────────────────────────────────────

    fun heroiAtaca(alvoId: String, manobra: Manobra, local: LocalAtaque, modo: AtaqueTotalModo = AtaqueTotalModo.DETERMINADO) {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        val ataque = ataques.getOrNull(ataqueSelecionado) ?: return
        if (armaDespreparadaBloqueia(ataque)) return // Lote 398: arma despreparada → precisa Preparar antes
        s.heroiAtaca(ataque, alvoId, manobra, local, modo)
        depoisDaAcaoDoHeroi()
    }

    /** Lote 398: se a arma empunhada ficou despreparada (golpe desbalanceado), avisa e bloqueia o ataque sem gastar o turno. */
    private fun armaDespreparadaBloqueia(ataque: AtaqueHeroi): Boolean {
        val s = sessao ?: return false
        if (s.armaDespreparada(ataque.rotulo)) {
            s.log += "⚠️ ${ataque.rotulo.substringBefore(" (").trim()} está despreparada — use \"Trocar arma\" (Preparar) antes de atacar."
            publicarLog(); atualizarEstado()
            return true
        }
        return false
    }

    /** Lote 378: Mover e Atacar — desloca-se até o alvo (corpo-a-corpo) e ataca em movimento com a arma empunhada. */
    fun heroiMoverEAtacar(alvoId: String, local: LocalAtaque) {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        val ataque = ataques.getOrNull(ataqueSelecionado) ?: return
        if (armaDespreparadaBloqueia(ataque)) return // Lote 398
        s.heroiMoverEAtacar(ataque, alvoId, local)
        depoisDaAcaoDoHeroi()
    }

    /**
     * Lote 377: Ataque Total (Duplo) — golpeia o alvo com a arma EMPUNHADA (mão hábil) e com a arma
     * [offHandIndex] (mão inábil, −4 salvo Ambidestria). MB p.366. Consome o turno como qualquer ataque.
     */
    fun heroiAtaqueDuplo(alvoId: String, local: LocalAtaque, offHandIndex: Int) {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        if (offHandIndex == ataqueSelecionado) return // a 2ª arma precisa ser diferente da empunhada
        val principal = ataques.getOrNull(ataqueSelecionado) ?: return
        val secundaria = ataques.getOrNull(offHandIndex) ?: return
        if (armaDespreparadaBloqueia(principal)) return // Lote 398
        s.heroiAtaqueDuplo(principal, secundaria, alvoId, local, temAmbidestria(viewModel.personagem))
        depoisDaAcaoDoHeroi()
    }

    /** Ambidestria (MB p.38) — anula o −4 da mão inábil. Mesmo padrão de varredura por id de [SentidoRules]. */
    private fun temAmbidestria(p: Personagem): Boolean =
        (p.vantagens + p.modeloRacial.vantagens).any { it.definicaoId == "ambidestria" }

    fun heroiMove(alvoId: String?, afastar: Boolean, metros: Int) {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        s.heroiMove(alvoId = alvoId, afastar = afastar, metros = metros)
        depoisDaAcaoDoHeroi()
    }

    fun heroiAvaliar(alvoId: String) {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        s.heroiAvaliar(alvoId)
        depoisDaAcaoDoHeroi()
    }

    /** Lote 383: Finta — Disputa Rápida com a arma empunhada (corpo-a-corpo) que reduz a defesa do alvo. */
    fun heroiFintar(alvoId: String) {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        val ataque = ataques.getOrNull(ataqueSelecionado) ?: return
        s.heroiFintar(ataque, alvoId)
        depoisDaAcaoDoHeroi()
    }

    /** Lote 386: Agarrar — usa o NH da arma/luta empunhada; deixa o alvo AGARRADO (−4 na defesa). */
    fun heroiAgarrar(alvoId: String) {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        val ataque = ataques.getOrNull(ataqueSelecionado) ?: return
        s.heroiAgarrar(ataque, alvoId)
        depoisDaAcaoDoHeroi()
    }

    /** Lote 386: Derrubar — Disputa Rápida (ST/DX) que joga um oponente adjacente no chão. */
    fun heroiDerrubar(alvoId: String) {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        s.heroiDerrubar(alvoId)
        depoisDaAcaoDoHeroi()
    }

    fun heroiApontar(alvoId: String, firmado: Boolean = false) {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        s.heroiApontar(alvoId, firmado)
        depoisDaAcaoDoHeroi()
    }

    /** Lote 396: Fogo de Retenção — arma de fogo CdT 5+ cobre a área (quem avançar leva rajada). MB p.409. */
    fun heroiFogoRetencao() {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        val ataque = ataques.getOrNull(ataqueSelecionado) ?: return
        s.heroiFogoRetencao(ataque)
        depoisDaAcaoDoHeroi()
    }

    /** Lote 399: Aguardar (Interromper Investida) — firma a arma perfurante empunhada p/ golpear primeiro quem investir. */
    fun heroiAguardar() {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        val ataque = ataques.getOrNull(ataqueSelecionado) ?: return
        s.heroiAguardar(ataque)
        depoisDaAcaoDoHeroi()
    }

    fun heroiManobra(manobra: Manobra, novaPostura: Postura? = null) {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        s.heroiManobra(manobra, novaPostura)
        depoisDaAcaoDoHeroi()
    }

    /** Lote 388: Defesa Total — Aumentada (+2 numa defesa) ou Dupla (2ª defesa se a 1ª falhar). MB p.366. */
    fun heroiDefesaTotal(modo: DefesaTotalModo, aumentadaEm: CombatResolver.TipoDefesa? = null) {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        s.heroiDefesaTotal(modo, aumentadaEm)
        depoisDaAcaoDoHeroi()
    }

    private fun depoisDaAcaoDoHeroi() {
        val s = sessao ?: return
        publicarLog()
        if (s.encerrado) { atualizarEstado(); finalizar(); return }
        s.avancarTurno()
        scope.launch { rodarLoop() }
    }

    /** A UI chama ao tocar uma opção do card "Defenda-se!". */
    fun escolherDefesa(opcao: CombatResolver.OpcaoDefesa) {
        defesaPendente?.escolher(opcao)
    }

    fun encerrarManual() {
        sessao = null; estado = null; defesaPendente = null; logPublicado = 0; finalizado = false
    }

    // ── Loop de turnos ────────────────────────────────────────────────────────

    private suspend fun rodarLoop() {
        val s = sessao ?: return
        while (!s.encerrado) {
            val atual = s.combatenteAtual()
            if (!atual.vivo) { s.avancarTurno(); continue }
            if (atual.ehHeroi) { atualizarEstado(); return } // espera a UI
            executarTurnoNpc(atual.id)
            if (s.encerrado) break
            s.avancarTurno()
        }
        atualizarEstado()
        if (s.encerrado) finalizar()
    }

    private suspend fun executarTurnoNpc(npcId: String) {
        val s = sessao ?: return
        val intencao = s.npcIntencao(npcId)
        val npc = s.inimigos.first { it.id == npcId }
        // BD do escudo do herói não vale contra arma de fogo (MB p.375): detecta pela flag ou pelo nome da arma.
        val contraFogo = npc.stats?.let { it.armaDeFogo || CombatSession.pareceArmaDeFogo(it.armaNome) } ?: false
        // Passa a arma EMPUNHADA p/ as regras de Aparar (esgrima/desbalanceada/Não/à distância).
        // Sem opções (ex.: herói sem defesa ativa após Ataque Total) → resolve direto, sem card.
        val opcoes = if (s.intencaoAtacaHeroi(intencao)) s.opcoesDefesaHeroi(
            armaPronta = ataques.getOrNull(ataqueSelecionado), contraArmaDeFogo = contraFogo,
            contraAtaqueCorpoACorpo = !intencao.aDistancia, // Lote 389: Retirada só vs corpo-a-corpo
            atacanteAdjacente = s.distancia(npc) <= 1, // Lote 390: aparar tiro só se o atirador está a 1m
            ataqueComArma = npc.stats?.armaNome?.isNotBlank() == true // Lote 391: −3 ao aparar arma com as mãos nuas
        ) else emptyList()
        if (s.intencaoAtacaHeroi(intencao) && opcoes.isNotEmpty()) {
            val deferred = CompletableDeferred<CombatResolver.OpcaoDefesa>()
            val nomeNpc = npc.nome
            defesaPendente = DefesaPendenteUi(
                atacante = nomeNpc,
                descricaoAtaque = "${nomeNpc} ataca! Escolha sua defesa.",
                opcoes = opcoes, deferred = deferred
            )
            atualizarEstado()
            val escolha = deferred.await()
            defesaPendente = null
            val soma = (1..3).sumOf { Random.nextInt(1, 7) }
            // Defesa Total (Dupla, Lote 388): prepara a melhor 2ª defesa de TIPO diferente — usada só se a 1ª falhar.
            // Sem variante "com recuo" na 2ª (recuo é 1×/turno e já pode ter ido na 1ª).
            val secundaria = if (s.heroiDefesaTotalDupla)
                opcoes.filter { it.tipo != escolha.tipo && !it.recuo }.maxByOrNull { it.valorFinal }
                    ?.let { DefesaHeroi(it.tipo, it.valorFinal, (1..3).sumOf { Random.nextInt(1, 7) }) }
            else null
            s.npcResolve(npcId, intencao, DefesaHeroi(escolha.tipo, escolha.valorFinal, soma, escolha.recuo), secundaria)
        } else {
            s.npcResolve(npcId, intencao, null)
        }
        publicarLog()
        atualizarEstado()
    }

    // ── Estado / persistência ──────────────────────────────────────────────────

    private fun publicarLog() {
        val s = sessao ?: return
        if (s.log.size > logPublicado) {
            val novas = s.log.subList(logPublicado, s.log.size).toList()
            logPublicado = s.log.size
            onLinhasNovas(novas)
        }
    }

    private fun atualizarEstado() {
        val s = sessao ?: run { estado = null; return }
        val vezHeroi = s.combatenteAtual().ehHeroi && !s.encerrado
        val combs = s.encounter.combatentes.map { c ->
            val dist = s.distancia(c)
            CombatenteUi(
                id = c.id, nome = c.nome, ehHeroi = c.ehHeroi,
                pvAtual = c.pvAtual, pvMax = c.pvMax, postura = c.postura.rotulo,
                condicoes = c.condicoes.map { it.rotulo },
                distanciaM = if (c.ehHeroi) 0 else dist,
                faixa = if (c.ehHeroi) FaixaDistancia.ENGAJADO else FaixaDistancia.de(dist),
                vivo = c.vivo
            )
        }
        val ataqueSel = ataques.getOrNull(ataqueSelecionado)
        val ranged = ataqueSel?.aDistancia == true
        val reachMelee = ataqueSel?.alcance ?: 1 // "C"/"1"/"2" já convertido em metros
        // Alvos: à distância = qualquer inimigo vivo; corpo-a-corpo = dentro do ALCANCE da arma.
        val deslocHeroi = s.heroi.deslocamentoEfetivo.coerceAtLeast(1) // metade se cambaleante (Lote 382, MB p.380)
        val alvos = if (!vezHeroi) emptyList()
            else if (ranged) combs.filter { !it.ehHeroi && it.vivo }
            else combs.filter { !it.ehHeroi && it.vivo && it.distanciaM <= reachMelee }
        // Mover e Atacar (Lote 378): corpo-a-corpo alcança quem está a até reach + Deslocamento (avança e golpeia).
        val alvosMover = if (!vezHeroi) emptyList()
            else if (ranged) combs.filter { !it.ehHeroi && it.vivo }
            else combs.filter { !it.ehHeroi && it.vivo && it.distanciaM <= reachMelee + deslocHeroi }
        // Manobras: Atacar fica disponível se há alvo no alcance (cobre reach > 1); à distância também Apontar.
        val manobras = if (!vezHeroi) emptyList() else s.manobrasHeroi().toMutableList().also {
            if (alvos.isNotEmpty() && Manobra.ATAQUE !in it) it.add(Manobra.ATAQUE)
            if (ranged && alvos.isNotEmpty() && Manobra.APONTAR !in it) it.add(Manobra.APONTAR)
            // Fogo de Retenção (Lote 396): arma à distância com CdT 5+ cobre a área (não precisa de alvo).
            if (ranged && (ataqueSel?.cadenciaTiro ?: 1) >= 5 && Manobra.FOGO_RETENCAO !in it) it.add(Manobra.FOGO_RETENCAO)
            if (!ranged && alvos.isNotEmpty() && Manobra.FINTAR !in it) it.add(Manobra.FINTAR) // Lote 383: finta corpo-a-corpo
            if (!ranged && alvos.isNotEmpty() && Manobra.AGARRAR !in it) it.add(Manobra.AGARRAR) // Lote 386: agarrar
            if (!ranged && alvos.isNotEmpty() && Manobra.DERRUBAR !in it) it.add(Manobra.DERRUBAR) // Lote 386: derrubar
        }
        estado = CombatUiState(
            rodada = s.encounter.rodadaAtual,
            combatentes = combs,
            vezDoHeroi = vezHeroi,
            manobrasHeroi = manobras,
            alvos = alvos,
            alvosMoverEAtacar = alvosMover,
            ataques = ataques,
            ataqueSelecionado = ataqueSelecionado,
            deslocamentoHeroi = deslocHeroi,
            posturaHeroi = s.heroi.postura.rotulo,
            posturasAlcancaveis = if (vezHeroi) s.posturasAlcancaveis() else emptyList(),
            heroiAmbidestro = temAmbidestria(viewModel.personagem),
            encerrado = s.encerrado,
            resultado = s.resultado
        )
    }

    private fun finalizar() {
        val s = sessao ?: return
        if (finalizado) return
        finalizado = true
        // Devolve o PV do herói à ficha (clamp em 0 — a ficha não guarda PV negativo) e SALVA.
        viewModel.sagaDefinirPvAtual(s.heroi.pvAtual.coerceAtLeast(0))
        // Saque (B8): armas dos inimigos derrotados, entregues à ficha de verdade.
        val saque = if (s.resultado == ResultadoCombate.VITORIA) computarSaque(s) else emptyList()
        saque.forEach { viewModel.sagaAdicionarItem(it, 1) }
        // Narrador converte o relatório factual agregado em prosa (+ XP) — sem inventar números.
        onFim?.invoke(CombatFim(s.resultado, s.resumo(), s.log.toList(), saque))
    }

    /** Saque simples e factual: arma de cada inimigo derrotado (agrupada por nome). */
    private fun computarSaque(s: CombatSession): List<String> =
        s.inimigos.filter { !it.vivo }
            .mapNotNull { it.stats?.armaNome?.trim()?.takeIf { n -> n.isNotBlank() } }
            .groupingBy { it }.eachCount()
            .map { (nome, q) -> if (q > 1) "$nome (x$q)" else nome }

    // ── Perfil de combate do herói (a partir da ficha) ─────────────────────────

    /** Defesas do herói (Lote 368: o ataque agora é uma lista escolhível, ver [construirAtaques]). */
    private fun construirPerfilHeroi(p: Personagem): HeroiPerfilCombate = HeroiPerfilCombate(
        esquiva = p.defesasAtivas.calcularEsquiva(p),
        apara = p.defesasAtivas.calcularApara(p),
        bloqueio = p.defesasAtivas.calcularBloqueio(p),
        ht = p.ht,
        rd = rdHeroi(p),
        // BD do escudo já está embutido acima; guardado à parte p/ removê-lo quando não vale (Lote 380, MB p.375).
        bonusEscudo = p.defesasAtivas.getBonusEscudo(p),
        // MT do herói (alvo) — somado ao acerto quando um NPC atira nele (Lote 381, MB p.549).
        modificadorTamanho = p.modificadorTamanho,
        // ST/DX para as Disputas de luta agarrada (Lote 386).
        st = p.forca, dx = p.dx,
        // Vontade — teste p/ não perder a mira ao ser ferido (Lote 395).
        vontade = p.vontade
    )

    /**
     * Lista de ataques utilizáveis (Lote 368): cada arma EQUIPADA (corpo-a-corpo e à distância/fogo)
     * com sua perícia, NH, dano resolvido por ST e tipo correto; mais o desarmado como último recurso.
     */
    private fun construirAtaques(p: Personagem): List<AtaqueHeroi> {
        val out = mutableListOf<AtaqueHeroi>()
        p.equipamentos.filter { it.tipo == TipoEquipamento.ARMA }.forEach { arma ->
            // Modo do catálogo: "corpo_a_corpo" | "distancia" (arcos/arremesso) | "armas_de_fogo".
            val modo = arma.armaTipoCombate?.lowercase().orEmpty()
            val aDistancia = modo.contains("dist") || modo.contains("fogo")
            val pericia = acharPericiaDaArma(p, arma)
            val nh = pericia?.calcularNivel(p) ?: p.dx
            // Dano bruto traz o token de tipo ("2d-1 pa"): o tipo vem dele; a expressão fica só com os dados.
            val danoBruto = (arma.danoCalculadoComSt(p, pericia?.definicaoId) ?: arma.armaDanoRaw).orEmpty()
            if (danoBruto.isBlank()) return@forEach
            val tipoArma = CombatSession.tipoDano(danoBruto)
            val danoExpr = CombatSession.semTokenTipo(danoBruto)
            // Alcance real (Lote 371): à distância usa o Máx do catálogo; corpo-a-corpo, o reach ("C"/"1"/"1,2").
            val alcanceReal = if (aDistancia) (arma.armaMaximoMetros ?: 50)
                else (arma.armaAlcanceCorpoACorpo?.let { reachParaMetros(it) } ?: 1)
            out.add(AtaqueHeroi(
                rotulo = arma.nome + (pericia?.let { " (${it.nome})" } ?: " (sem perícia, usa DX)"),
                nh = nh, danoExpr = danoExpr, tipo = tipoArma,
                aDistancia = aDistancia, alcance = alcanceReal, precisao = arma.armaPrecisao ?: 0,
                meioDano = if (aDistancia) (arma.armaMeioDanoMetros ?: 0) else 0,
                magnitude = arma.armaMagnitude ?: 0,
                apararTipo = CombatSession.parseAparar(arma.armaAparar).second,
                cadenciaTiro = arma.armaCadenciaTiro ?: 1,
                recuo = arma.armaRecuo ?: 1,
                duasMaos = ehDuasMaos(arma),
                armaDeFogo = modo.contains("fogo"), // Lote 395: arma de fogo → pode firmar ao Apontar (+1)
                stMinimo = arma.armaStMinimo ?: 0, // Lote 398: ST mínima → desbalanceada fica despreparada se ST < 1,5×
                temPericia = pericia != null
            ))
        }
        // Desarmado (sempre disponível): melhor perícia de luta sem arma, ou DX.
        val desarmada = melhorPericiaDesarmada(p)
        val aparaMarcial = desarmada?.let {
            CatalogFilters.normalizarBusca(it.definicaoId).removePrefix("racial_") in MARCIAIS_APARA
        } ?: false
        out.add(AtaqueHeroi(
            rotulo = (desarmada?.nome ?: "Desarmado"),
            nh = desarmada?.calcularNivel(p) ?: p.dx,
            danoExpr = p.danoGdP, tipo = DanoTipo.CONT, aDistancia = false, alcance = 1,
            desarmado = true, aparaMarcial = aparaMarcial, temPericia = desarmada != null
        ))
        // Armas à distância primeiro quando há (pistoleiro saca o revólver, não soca).
        return out.sortedByDescending { it.aDistancia }
    }

    /** Converte o alcance corpo-a-corpo ("C", "1", "1,2") em metros (maior alcance da arma). "C" → 1 (adjacente). */
    private fun reachParaMetros(raw: String): Int =
        Regex("\\d+").findAll(raw).mapNotNull { it.value.toIntOrNull() }.maxOrNull() ?: 1

    /**
     * Lote 380: a arma ocupa as DUAS mãos (sem mão livre para o escudo, MB p.375)? Orientado a DADO: a flag
     * do catálogo (`armaDuasMaos`, do †/‡ corpo-a-corpo ou já resolvida por grupo no loader) e, para fichas
     * antigas, o GRUPO da arma (não o nome) via [ArmaCatalogoItem.duasMaosPorGrupo].
     */
    private fun ehDuasMaos(arma: com.gurps.ficha.model.Equipamento): Boolean =
        arma.armaDuasMaos ||
            com.gurps.ficha.model.ArmaCatalogoItem.duasMaosPorGrupo(
                arma.armaTipoCombate.orEmpty(), arma.armaGrupo.orEmpty()
            )

    /**
     * Casa uma arma com a perícia do herói (Lote 378 — robusto). Confere grupo/nome da arma contra
     * nome/ESPECIALIZAÇÃO/id da perícia: armas de fogo usam a perícia "Armas de Fogo/NT" com a
     * especialização ("Pistola"/"Rifle") guardada à parte, então só comparar o nome falhava. Se a ficha
     * veio sem o grupo da arma (ex.: criada pela IA), cai num fallback por FAMÍLIA derivada do tipo de
     * combate (fogo → "Armas de Fogo"; distância → arco/besta/arremesso…), preferindo a especialização
     * que casa a arma, senão a perícia de maior NH.
     */
    private fun acharPericiaDaArma(p: Personagem, arma: com.gurps.ficha.model.Equipamento): com.gurps.ficha.model.PericiaSelecionada? {
        val tokens = listOfNotNull(arma.armaGrupo, arma.nome)
            .map { CatalogFilters.normalizarBusca(it) }.filter { it.isNotBlank() }

        fun casa(per: com.gurps.ficha.model.PericiaSelecionada): Boolean {
            val campos = listOf(per.nome, per.especializacao, per.definicaoId)
                .map { CatalogFilters.normalizarBusca(it) }.filter { it.isNotBlank() }
            return campos.any { c -> tokens.any { a -> c == a || c.contains(a) || a.contains(c) } }
        }
        // 1) Match direto: grupo/nome da arma × nome/especialização/id da perícia.
        p.periciasTotais.firstOrNull { casa(it) }?.let { return it }

        // 2) Fallback por FAMÍLIA (grupo pode vir vazio): tipo de combate da arma → perícia base.
        val modo = arma.armaTipoCombate?.lowercase().orEmpty()
        val familia: List<String> = when {
            modo.contains("fogo") -> listOf("armas de fogo", "arma de fogo")
            modo.contains("dist") -> listOf("arco", "besta", "arremesso", "funda", "zarabatana")
            else -> emptyList()
        }
        if (familia.isEmpty()) return null
        val candidatas = p.periciasTotais.filter { per ->
            val n = CatalogFilters.normalizarBusca(per.nome)
            familia.any { n.contains(it) }
        }
        if (candidatas.isEmpty()) return null
        return candidatas.firstOrNull { per ->
            val esp = CatalogFilters.normalizarBusca(per.especializacao)
            esp.isNotBlank() && tokens.any { a -> esp == a || esp.contains(a) || a.contains(esp) }
        } ?: candidatas.maxByOrNull { it.calcularNivel(p) }
    }

    private fun melhorPericiaDesarmada(p: Personagem): com.gurps.ficha.model.PericiaSelecionada? =
        p.periciasTotais.filter {
            CatalogFilters.normalizarBusca(it.definicaoId).removePrefix("racial_") in DESARMADAS
        }.maxByOrNull { it.calcularNivel(p) }

    /** RD do herói: maior RD entre as armaduras equipadas (aproximação de torso). */
    private fun rdHeroi(p: Personagem): Int = p.equipamentos
        .filter { it.tipo == TipoEquipamento.ARMADURA }
        .mapNotNull { it.rdArmaduraExibicao()?.let { s -> Regex("\\d+").find(s)?.value?.toIntOrNull() } }
        .maxOrNull() ?: 0

    private companion object {
        val DESARMADAS = setOf("briga", "boxe", "carate", "judo", "luta_grecoromana", "caratê", "judô")
        // Lote 391: aparar uma ARMA desarmado tem o valor cheio (sem −3) com Caratê ou Judô (MB p.376).
        val MARCIAIS_APARA = setOf("carate", "caratê", "judo", "judô")
    }
}
