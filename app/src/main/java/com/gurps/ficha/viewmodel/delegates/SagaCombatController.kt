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
    val ataques: List<AtaqueHeroi>,
    val ataqueSelecionado: Int,
    val deslocamentoHeroi: Int,
    val posturaHeroi: String,
    val posturasAlcancaveis: List<Postura>,
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
        val dano = HitLocationRules.aplicarDano(alvo.pvMax, danoBase, tipo, local, rd)
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
        s.heroiAtaca(ataque, alvoId, manobra, local, modo)
        depoisDaAcaoDoHeroi()
    }

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

    fun heroiApontar(alvoId: String) {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        s.heroiApontar(alvoId)
        depoisDaAcaoDoHeroi()
    }

    fun heroiManobra(manobra: Manobra, novaPostura: Postura? = null) {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        s.heroiManobra(manobra, novaPostura)
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
        if (s.intencaoAtacaHeroi(intencao)) {
            // Passa a arma EMPUNHADA p/ as regras de Aparar (esgrima/desbalanceada/Não/à distância).
            val opcoes = s.opcoesDefesaHeroi(armaPronta = ataques.getOrNull(ataqueSelecionado))
            val deferred = CompletableDeferred<CombatResolver.OpcaoDefesa>()
            val nomeNpc = s.inimigos.first { it.id == npcId }.nome
            defesaPendente = DefesaPendenteUi(
                atacante = nomeNpc,
                descricaoAtaque = "${nomeNpc} ataca! Escolha sua defesa.",
                opcoes = opcoes, deferred = deferred
            )
            atualizarEstado()
            val escolha = deferred.await()
            defesaPendente = null
            val soma = (1..3).sumOf { Random.nextInt(1, 7) }
            s.npcResolve(npcId, intencao, DefesaHeroi(escolha.tipo, escolha.valorFinal, soma))
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
        val alvos = if (!vezHeroi) emptyList()
            else if (ranged) combs.filter { !it.ehHeroi && it.vivo }
            else combs.filter { !it.ehHeroi && it.vivo && it.distanciaM <= reachMelee }
        // Manobras: Atacar fica disponível se há alvo no alcance (cobre reach > 1); à distância também Apontar.
        val manobras = if (!vezHeroi) emptyList() else s.manobrasHeroi().toMutableList().also {
            if (alvos.isNotEmpty() && Manobra.ATAQUE !in it) it.add(Manobra.ATAQUE)
            if (ranged && alvos.isNotEmpty() && Manobra.APONTAR !in it) it.add(Manobra.APONTAR)
        }
        estado = CombatUiState(
            rodada = s.encounter.rodadaAtual,
            combatentes = combs,
            vezDoHeroi = vezHeroi,
            manobrasHeroi = manobras,
            alvos = alvos,
            ataques = ataques,
            ataqueSelecionado = ataqueSelecionado,
            deslocamentoHeroi = s.heroi.deslocamento.coerceAtLeast(1),
            posturaHeroi = s.heroi.postura.rotulo,
            posturasAlcancaveis = if (vezHeroi) s.posturasAlcancaveis() else emptyList(),
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
        rd = rdHeroi(p)
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
            val danoExpr = (arma.danoCalculadoComSt(p, pericia?.definicaoId) ?: arma.armaDanoRaw).orEmpty()
            if (danoExpr.isBlank()) return@forEach
            // Alcance real (Lote 371): à distância usa o Máx do catálogo; corpo-a-corpo, o reach ("C"/"1"/"1,2").
            val alcanceReal = if (aDistancia) (arma.armaMaximoMetros ?: 50)
                else (arma.armaAlcanceCorpoACorpo?.let { reachParaMetros(it) } ?: 1)
            out.add(AtaqueHeroi(
                rotulo = arma.nome + (pericia?.let { " (${it.nome})" } ?: " (sem perícia, usa DX)"),
                nh = nh, danoExpr = danoExpr, tipo = CombatSession.tipoDano(danoExpr),
                aDistancia = aDistancia, alcance = alcanceReal, precisao = arma.armaPrecisao ?: 0,
                meioDano = if (aDistancia) (arma.armaMeioDanoMetros ?: 0) else 0,
                magnitude = arma.armaMagnitude ?: 0,
                apararTipo = CombatSession.parseAparar(arma.armaAparar).second,
                temPericia = pericia != null
            ))
        }
        // Desarmado (sempre disponível): melhor perícia de luta sem arma, ou DX.
        val desarmada = melhorPericiaDesarmada(p)
        out.add(AtaqueHeroi(
            rotulo = (desarmada?.nome ?: "Desarmado"),
            nh = desarmada?.calcularNivel(p) ?: p.dx,
            danoExpr = p.danoGdP, tipo = DanoTipo.CONT, aDistancia = false, alcance = 1,
            temPericia = desarmada != null
        ))
        // Armas à distância primeiro quando há (pistoleiro saca o revólver, não soca).
        return out.sortedByDescending { it.aDistancia }
    }

    /** Converte o alcance corpo-a-corpo ("C", "1", "1,2") em metros (maior alcance da arma). "C" → 1 (adjacente). */
    private fun reachParaMetros(raw: String): Int =
        Regex("\\d+").findAll(raw).mapNotNull { it.value.toIntOrNull() }.maxOrNull() ?: 1

    /** Casa uma arma com a perícia do herói por grupo/nome (fuzzy, normalizado). */
    private fun acharPericiaDaArma(p: Personagem, arma: com.gurps.ficha.model.Equipamento): com.gurps.ficha.model.PericiaSelecionada? {
        val alvos = listOfNotNull(arma.armaGrupo, arma.nome)
            .map { CatalogFilters.normalizarBusca(it) }.filter { it.isNotBlank() }
        if (alvos.isEmpty()) return null
        return p.periciasTotais.firstOrNull { per ->
            val n = CatalogFilters.normalizarBusca(per.nome)
            alvos.any { a -> n == a || n.contains(a) || a.contains(n) }
        }
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
    }
}
