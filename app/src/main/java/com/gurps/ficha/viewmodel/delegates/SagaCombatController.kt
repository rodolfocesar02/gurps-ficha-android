package com.gurps.ficha.viewmodel.delegates

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.gurps.ficha.domain.combat.*
import com.gurps.ficha.domain.engine.MagicEngine
import com.gurps.ficha.domain.magic.ContextoConjuracao
import com.gurps.ficha.domain.magic.MagicCasting
import com.gurps.ficha.domain.magic.MagicClassParser
import com.gurps.ficha.domain.magic.MagicCost
import com.gurps.ficha.domain.magic.TipoDuracao
import com.gurps.ficha.domain.magic.MagicEnergy
import com.gurps.ficha.domain.magic.NivelMana
import com.gurps.ficha.domain.magic.TipoClasseMagia
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
    val vivo: Boolean,
    /** Lote TOK-PF: fração de fadiga do HERÓI (null nos NPCs — o bestiário não rastreia PF). */
    val pfPct: Float? = null,
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
        // Lote TOK-PF: a barra azul é só visual — o TalkBack precisa da fadiga em palavras.
        if (ehHeroi && pfPct != null) {
            append(", fadiga "); append(when {
                pfPct <= 0.33f -> "quase esgotada"
                pfPct < 1f -> "parcial"
                else -> "cheia"
            })
        }
    }
}

/** Lote MA-3a: uma magia que o herói pode conjurar no combate, pronta para o seletor. */
data class MagiaConjuravelUi(
    val id: String,
    val nome: String,
    val classe: String,
    val nhBasico: Int,
    val custoTexto: String,
    /** Projétil habilita o controle de energia investida (1d por ponto). */
    val ehProjetil: Boolean,
    /** Área habilita o controle de raio + a mira no grid (Lote MA-3d). */
    val ehArea: Boolean,
    /** Toque carrega a mão e é entregue num ataque corpo-a-corpo (Lote MA-3d-2). */
    val ehToque: Boolean,
    /** Teto de energia do Projétil = nível de Aptidão Mágica (Magia p.12). */
    val aptidaoMagica: Int,
    /**
     * Lote MEC-7: o EFEITO desta magia escala com a energia investida (Escudo: 2 PF por +1 de Defesa;
     * Aumentar Força: 1 PF por +1 de ST). Antes só Projétil ganhava o seletor, então o jogador levava
     * o MÍNIMO sem poder escolher — pagava e recebia o efeito mais fraco possível.
     */
    val escalaComEnergia: Boolean = false,
    /** Teto de energia que ainda compra efeito (acima disso é desperdício). */
    val energiaMax: Int = 1,
    /** O que a energia compra, em português ("+1 de Defesa a cada 2 PF, até +4"). */
    val dicaEnergia: String? = null,
    /** Lote MEC-10: magia de CURA — o seletor de energia também vale aqui (PV por ponto de energia). */
    val ehCura: Boolean = false,
    /**
     * Lote MEC-20: o `efeito` curado do catálogo ("dano", "condicao", "buff", "cura", "ambiente",
     * "controle", "narrado", "informacao"). A tela usa isto para só oferecer "Causa dano" a quem
     * pode causar dano — antes o toggle aparecia até em Localizar Ar e Criar Ar.
     */
    val efeito: String? = null,
    /** Custo aproximado (para limitar o quanto de PV o mago pode queimar). */
    val custoEstimado: Int,
    val castavel: Boolean,
    val motivo: String,
)

/** Lote MA-3c: conjuração multi-turno em andamento (o herói está concentrando). */
data class ConjurandoUi(val nome: String, val turnosRestantes: Int)

/** Lote MA-3d: mira de magia de área em andamento — o app espera o toque no hex central. */
data class MiraAreaUi(val magiaId: String, val magiaNome: String, val raio: Int, val energia: Int, val pvQueimar: Int, val causaDano: Boolean = false)

/** Estado completo do combate para a UI. */
data class CombatUiState(
    val rodada: Int,
    val combatentes: List<CombatenteUi>,
    val vezDoHeroi: Boolean,
    val manobrasHeroi: List<Manobra>,
    /** Lote MA-3a: magias conjuráveis do herói (vazio se ele não é mago / não é a vez dele). */
    val magiasConjuraveis: List<MagiaConjuravelUi> = emptyList(),
    /** Lote MA-3c: conjuração multi-turno em andamento (não-null → o herói só continua/aborta). */
    val conjurando: ConjurandoUi? = null,
    /** Lote MA-3d-2: nome da mágica de Toque carregada na mão (null se nenhuma). */
    val toqueCarregado: String? = null,
    /** Lote MA-3d-4: mágicas ativas no combate ("Escudo (58s)") — manutenção cobrada por turno. */
    val magiasAtivas: List<String> = emptyList(),
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
    /**
     * Lote TESTE-1c: OBSERVÁVEL pelo Compose (`mutableStateOf`), não um `var` comum.
     *
     * Bug que isto corrige (achado pelo usuário no aparelho): `ativo` é `get() = sessao != null`.
     * Com `sessao` sendo `var` puro, `ativo` NÃO era observável — e a tela testa
     * `if (sagaCombateAtivo && sagaEstadoTatico != null)`. Pelo CURTO-CIRCUITO do `&&`, enquanto
     * `ativo` era falso o `estadoTatico` (esse sim observável) NUNCA era lido, então o composable
     * não se inscrevia nele. Resultado: o combate começava e a grade **nunca redesenhava** — o
     * preview ficava no demo para sempre.
     *
     * Como `CombatSession` é mutável por dentro, o `mutableStateOf` notifica na ATRIBUIÇÃO (começar/
     * encerrar combate), que é exatamente o que a UI precisa saber.
     */
    private var sessao: CombatSession? by mutableStateOf(null)
    private var logPublicado = 0
    private var finalizado = false

    var estado by mutableStateOf<CombatUiState?>(null); private set
    var defesaPendente by mutableStateOf<DefesaPendenteUi?>(null); private set

    // ── Lote TOK-4 (VTT 2D): estado POSICIONAL do combate na grade ─────────────────────────────
    /** Posições reais dos combatentes (null fora de combate). Fonte do canvas tático. */
    var estadoTatico by mutableStateOf<com.gurps.ficha.domain.combat.hex.HexCombatState?>(null); private set
    /** Aviso transitório do grid ("Muito longe", "Não é seu turno") — a UI limpa após ~2s. */
    var avisoTatico by mutableStateOf<String?>(null)

    /** Lote MA-3d: mira de magia de ÁREA pendente — enquanto não-null, o próximo toque num hex é o centro. */
    var miraAreaPendente by mutableStateOf<MiraAreaUi?>(null); private set

    /** Token pronto pro desenho: posição + facing + nome + PV% + condições (o canvas cruza com as imagens). */
    data class TokenTatico(
        val id: String, val nome: String, val ehHeroi: Boolean,
        val pvPct: Float, val posicao: com.gurps.ficha.domain.combat.hex.HexCoord,
        /**
         * Lote TOK-PF: fração de PF (barra azul sob a de PV). **Só o herói** — o bestiário não
         * rastreia fadiga de NPC, então mostrar barra neles seria inventar dado. `null` = sem barra.
         */
        val pfPct: Float? = null,
        val facing: com.gurps.ficha.domain.combat.hex.Direcao,
        /** Lote TOK-6b-1: condições como mini-ícones sobre a barra de HP (🩸💫🤼😮‍💨⬇). */
        val condicoesIcones: String = "",
    )

    /** Tokens do combate REAL, na ordem do estado tático. Vazio fora de combate. */
    val tokensTaticos: List<TokenTatico> get() {
        val s = sessao ?: return emptyList()
        val est = estadoTatico ?: return emptyList()
        return est.posicoes.mapNotNull { pos ->
            val c = s.encounter.combatentes.firstOrNull { it.id == pos.id } ?: return@mapNotNull null
            val icones = buildString {
                if (Condicao.SANGRANDO in c.condicoes) append("🩸")
                if (Condicao.ATORDOADO in c.condicoes) append("💫")
                if (Condicao.AGARRADO in c.condicoes || Condicao.IMOBILIZADO in c.condicoes) append("🤼")
                if (Condicao.SUFOCANDO in c.condicoes) append("😮‍💨")
                if (c.postura != Postura.EM_PE) append("⬇")
            }
            TokenTatico(
                id = c.id, nome = c.nome, ehHeroi = c.ehHeroi,
                pvPct = if (c.pvMax > 0) (c.pvAtual.toFloat() / c.pvMax).coerceIn(0f, 1f) else 0f,
                // TOK-PF: o máximo vem da ficha (o Combatente não guarda pfMax).
                pfPct = if (c.ehHeroi) {
                    val max = viewModel.personagem.pontosFadiga
                    if (max > 0) (c.pfAtual.toFloat() / max).coerceIn(0f, 1f) else null
                } else null,
                posicao = pos.posicao, facing = pos.facing,
                condicoesIcones = icones,
            )
        }
    }

    /** Hexes que o herói alcança AGORA (vazio se não é o turno dele) — destaque verde do canvas. */
    /**
     * Lote HEX-FACING: o herói MUDA DE DIREÇÃO (facing). É uma ação **LIVRE** — não gasta o turno.
     *
     * Regra conferida na fonte literal (Módulo Básico, a pedido do usuário):
     *  - **p.368/364**: o "passo" que quase toda manobra concede é "um movimento de até 1/10 do
     *    Deslocamento, **uma mudança de direção (ex.: virar-se), ou as duas coisas**".
     *  - **p.387** ("Passo" no Combate Tático): "Algumas manobras, como Ataque ou Preparar, permitem
     *    que o personagem dê um passo em qualquer direção... Ele pode **mudar de direção livremente**
     *    antes ou depois do movimento."
     *  - **p.388**: "Mudar de direção no final do movimento: **Livre!**" (para qualquer direção se não
     *    usou mais que metade dos pontos de movimento).
     *
     * Ou seja: virar e atacar no mesmo turno é legal — virar NÃO consome a ação. Neste app o
     * movimento no grid já gasta o turno (manobra Deslocamento), então o herói vira ANTES de agir,
     * quando ainda não gastou ponto de movimento nenhum: o caso "livre para qualquer direção".
     *
     * BLOQUEIOS (também da fonte):
     *  - **p.86**: quem está agarrado/retido "não pode utilizar as manobras Deslocamento e Mudança de
     *    Posição **nem mudar de direção**".
     *  - **p.386**: em *Avançar e Atacar* "o personagem NÃO pode mudar de direção no final do
     *    deslocamento" — aqui isso não se aplica porque a virada acontece antes de escolher a manobra.
     */
    /**
     * Lote HEX-FACING-2: virada de FIM DE MOVIMENTO pendente (MB p.388). Enquanto != null, o turno do
     * herói ainda NÃO passou: ele já andou e agora escolhe para onde fica olhando.
     */
    data class ViradaFinalUi(
        val andou: Int,
        /** true = pode virar para qualquer direção (andou ≤ metade do Deslocamento). */
        val livreParaQualquer: Boolean,
        val facingAtual: com.gurps.ficha.domain.combat.hex.Direcao,
    )
    var viradaFinalPendente: ViradaFinalUi? by mutableStateOf(null); private set

    /**
     * Direções que o herói PODE escolher na virada de fim de movimento (MB p.388): todas se andou até
     * metade do Deslocamento; senão só um lado de hexágono para cada lado (± 1 na roda das 6).
     */
    fun direcoesDaViradaFinal(): List<com.gurps.ficha.domain.combat.hex.Direcao> {
        val v = viradaFinalPendente ?: return emptyList()
        // Lote TESTE-C: a regra mora no domain (testável na JVM) — aqui só o estado da UI.
        return com.gurps.ficha.domain.combat.hex.RegrasMovimentoTatico
            .direcoesDaViradaFinal(v.facingAtual, v.livreParaQualquer)
    }

    /** Conclui a virada de fim de movimento (ou a dispensa mantendo a direção) e PASSA o turno. */
    fun concluirViradaFinal(direcao: com.gurps.ficha.domain.combat.hex.Direcao?) {
        val s = sessao ?: return
        val v = viradaFinalPendente ?: return
        val est = estadoTatico
        if (direcao != null && est != null && direcao != v.facingAtual && direcao in direcoesDaViradaFinal()) {
            estadoTatico = est.copy(
                posicoes = est.posicoes.map { if (it.id == "heroi") it.copy(facing = direcao) else it }
            )
            s.log += "🧭 Ao fim do movimento você se vira para ${direcao.name.lowercase()} " +
                (if (v.livreParaQualquer) "(livre — andou ${v.andou}m)" else "(um lado de hexágono — andou ${v.andou}m)") +
                " [MB p.388]."
        }
        viradaFinalPendente = null
        depoisDaAcaoDoHeroi()
    }

    fun heroiVirar(direcao: com.gurps.ficha.domain.combat.hex.Direcao) {
        val s = sessao ?: return
        val est = estadoTatico ?: return
        if (s.encerrado || !s.combatenteAtual().ehHeroi) { avisoTatico = "Não é seu turno"; return }
        // MB p.86: agarrado/imobilizado não muda de direção.
        val preso = Condicao.AGARRADO in s.heroi.condicoes || Condicao.IMOBILIZADO in s.heroi.condicoes
        if (preso) { avisoTatico = "Agarrado — você não consegue mudar de direção (MB p.86)"; return }
        val atual = est.posicoes.firstOrNull { it.id == "heroi" } ?: return
        if (atual.facing == direcao) { avisoTatico = null; return }
        estadoTatico = est.copy(
            posicoes = est.posicoes.map { if (it.id == "heroi") it.copy(facing = direcao) else it }
        )
        // Ação livre: NÃO chama depoisDaAcaoDoHeroi() — o turno continua com o jogador.
        s.log += "🧭 Você se vira para ${direcao.name.lowercase()} (ação livre — MB p.387)."
        publicarLog()
        avisoTatico = null
        atualizarEstado()
    }

    fun hexesAlcancaveisHeroi(): Set<com.gurps.ficha.domain.combat.hex.HexCoord> {
        val s = sessao ?: return emptySet()
        val est = estadoTatico ?: return emptySet()
        if (s.encerrado || !s.combatenteAtual().ehHeroi) return emptySet()
        // Só quando o HERÓI está selecionado no grid (toque no próprio token) — evita poluir a grade.
        if (est.idSelecionado != "heroi") return emptySet()
        // Lote TOK-6b-2 (achado da varredura): mesmas travas do MOVER de faixas — atordoado não
        // se move (MB p.420) e agarrado/imobilizado não desloca sem se Desvencilhar (MB p.371).
        // Sem isso a grade driblava a luta agarrada: bastava tocar num hex verde e sair andando.
        // Lote MA-3c: concentrando numa magia multi-turno → não se move (só continuar/abortar).
        // Lote TESTE-C: as travas moram no domain e são testadas lá.
        if (!com.gurps.ficha.domain.combat.hex.RegrasMovimentoTatico
                .podeMoverNaGrade(s.heroi.condicoes, s.conjuracaoEmAndamento != null)) return emptySet()
        return com.gurps.ficha.domain.combat.hex.HexSetup.hexesAlcancaveis(
            est, s.heroi.deslocamentoEfetivo.coerceAtLeast(1)
        )
    }

    /**
     * Toque num hex do canvas tático (combate REAL):
     *  - hex com token → seleciona;
     *  - herói selecionado + turno dele + hex alcançável → manobra MOVER TÁTICA (o grid vira a
     *    fonte das distâncias — substitui o botão de faixa, docs/fonte-regras/Combate.md "Deslocamento"/"Passo");
     *  - senão → destaca o hex / avisa por que não moveu.
     */
    fun aoTocarHexTatico(hex: com.gurps.ficha.domain.combat.hex.HexCoord) {
        val s = sessao ?: return
        // Lote MA-3d: com uma mira de ÁREA pendente, o toque é o CENTRO da magia — resolve e sai.
        if (miraAreaPendente != null) { resolverMiraAreaNoHex(hex); return }
        val est = estadoTatico ?: return
        val tokenAli = est.posicoes.firstOrNull { it.posicao == hex }
        if (tokenAli != null) {
            estadoTatico = est.copy(hexSelecionado = hex, idSelecionado = tokenAli.id)
            return
        }
        if (est.idSelecionado == "heroi") {
            if (s.encerrado || !s.combatenteAtual().ehHeroi) {
                avisoTatico = "Não é seu turno"
                estadoTatico = est.copy(hexSelecionado = hex)
                return
            }
            val pHeroi = est.posicoes.firstOrNull { it.id == "heroi" } ?: return
            val distancia = pHeroi.posicao.distancia(hex)
            if (hex in hexesAlcancaveisHeroi()) {
                val movido = com.gurps.ficha.domain.combat.hex.HexSetup.moverHeroi(est, hex)
                estadoTatico = movido
                avisoTatico = null
                s.heroiMoveTatico(
                    novasDistancias = com.gurps.ficha.domain.combat.hex.HexSetup.distanciasAoHeroi(movido),
                    metrosPercorridos = distancia
                )
                // Lote HEX-FACING-2 (MB p.388): "Mudar de direção NO FINAL DO MOVIMENTO: Livre!" — o
                // turno NÃO passa direto. O jogador escolhe para onde fica olhando antes dos inimigos
                // agirem; sem isto o facing gruda na direção da fuga e ele leva flanco sem poder reagir.
                // Livre para QUALQUER direção se andou até METADE do Deslocamento; se andou mais, só
                // UM lado de hexágono.
                // Se o combate acabou com o movimento (ou o herói caiu), NÃO pede virada — seria um
                // prompt preso numa luta encerrada.
                if (s.encerrado) { depoisDaAcaoDoHeroi(); return }
                viradaFinalPendente = ViradaFinalUi(
                    andou = distancia,
                    // Lote TESTE-C: a regra da metade (MB p.388) mora no domain e é testada lá,
                    // contra o deslocamento EFETIVO (carga/ferimento), não o cru da ficha.
                    livreParaQualquer = com.gurps.ficha.domain.combat.hex.RegrasMovimentoTatico
                        .viradaFinalLivre(distancia, s.heroi.deslocamentoEfetivo),
                    facingAtual = movido.posicoes.firstOrNull { it.id == "heroi" }?.facing
                        ?: com.gurps.ficha.domain.combat.hex.Direcao.LESTE,
                )
                atualizarEstado()
                return
            }
            avisoTatico = "Muito longe — deslocamento ${s.heroi.deslocamentoEfetivo}m"
        }
        // Lote TOK-6b-2: tocar num hex vazio com INIMIGO selecionado fecha o menu dele (limpa a
        // seleção); com o herói selecionado a seleção fica (pra tentar outro hex de movimento).
        estadoTatico = est.copy(
            hexSelecionado = hex,
            idSelecionado = if (est.idSelecionado == "heroi") "heroi" else null
        )
    }

    /** Lote TOK-6b-2: fecha o menu do token — limpa a seleção da grade. */
    fun limparSelecaoTatica() {
        estadoTatico = estadoTatico?.copy(hexSelecionado = null, idSelecionado = null)
    }

    /**
     * Lote TOK-5a: implementação da ponte POSICIONAL que o CombatSession consulta. Todas as
     * funções degradam para "sem efeito" quando a grade não está montada (modo faixas intacto).
     */
    private val bridgeTatico = object : CombatSession.PosicaoBridge {
        override fun facingDoAtaque(atacanteId: String, alvoId: String): com.gurps.ficha.domain.combat.hex.Facing? {
            val est = estadoTatico ?: return null
            val pa = est.posicoes.firstOrNull { it.id == atacanteId }?.posicao ?: return null
            val alvo = est.posicoes.firstOrNull { it.id == alvoId } ?: return null
            return com.gurps.ficha.domain.combat.hex.HexGrid.facingDoAtaque(pa, alvo.posicao, alvo.facing)
        }

        override fun penalidadeAtravesDeHex(atacanteId: String, alvoId: String, alcanceArmaMetros: Int): Int {
            val est = estadoTatico ?: return 0
            val pa = est.posicoes.firstOrNull { it.id == atacanteId }?.posicao ?: return 0
            val pb = est.posicoes.firstOrNull { it.id == alvoId }?.posicao ?: return 0
            // No Saga o herói luta sozinho: todo ocupante intermediário que não é o par é INIMIGO.
            val inimigosNoMeio = est.posicoes
                .filter { it.id != atacanteId && it.id != alvoId }
                .map { it.posicao }.toSet()
            return com.gurps.ficha.domain.combat.hex.HexAtaqueAtravesHex.penalidade(
                pa, pb, alcanceArmaMetros, ocupantesAliados = emptySet(), ocupantesInimigos = inimigosNoMeio
            ) ?: 0 // null = fora de alcance — o motor já tem golpeForaDeAlcance pra isso
        }

        override fun aoAtacar(atacanteId: String, alvoId: String) {
            val est = estadoTatico ?: return
            val pa = est.posicoes.firstOrNull { it.id == atacanteId } ?: return
            val alvoPos = est.posicoes.firstOrNull { it.id == alvoId }?.posicao ?: return
            val dir = com.gurps.ficha.domain.combat.hex.Direcao.de(pa.posicao, alvoPos) ?: return
            if (pa.facing == dir) return
            estadoTatico = est.copy(posicoes = est.posicoes.map {
                if (it.id == atacanteId) it.copy(facing = dir) else it
            })
        }

        override fun recuarUmHex(defensorId: String, atacanteId: String): Map<String, Int>? {
            val est = estadoTatico ?: return null
            val pd = est.posicoes.firstOrNull { it.id == defensorId } ?: return null
            val pa = est.posicoes.firstOrNull { it.id == atacanteId }?.posicao ?: return null
            val dir = com.gurps.ficha.domain.combat.hex.Direcao.de(pa, pd.posicao) ?: return null
            val destino = pd.posicao + dir.vetor
            if (est.posicoes.any { it.id != defensorId && it.posicao == destino }) return null
            if (com.gurps.ficha.domain.combat.hex.HexCoord.ORIGEM.distancia(destino) > est.raioGrade) return null
            val novo = est.copy(posicoes = est.posicoes.map {
                if (it.id == defensorId) it.copy(posicao = destino) else it
            })
            estadoTatico = novo
            return if (defensorId == "heroi")
                com.gurps.ficha.domain.combat.hex.HexSetup.distanciasAoHeroi(novo) else null
        }

        override fun moverNpcNaGrade(
            npcId: String,
            intencao: com.gurps.ficha.domain.combat.NpcCombatBrain.IntencaoNpc,
        ): Int? {
            var est = estadoTatico ?: return null
            val s = sessao ?: return null
            val npc = s.encounter.combatentes.firstOrNull { it.id == npcId && it.vivo } ?: return null
            val stats = npc.stats ?: return null
            val perfil = com.gurps.ficha.domain.combat.hex.HexTaticaNpc.PerfilTatico(
                agressividade = stats.agressividade,
                moral = stats.moral,
                alcanceArmaMetros = stats.alcanceMetros.coerceAtLeast(1),
                // Proxy honesto: arma de fogo OU alcance longo caracterizam o combatente "à distância"
                // (o kite do HexTaticaNpc exige temArmaDistancia && alcance ≥ 3).
                temArmaDistancia = stats.armaDeFogo || stats.alcanceMetros >= 4,
            )
            // A IA decide VIZINHO a VIZINHO (decisão local do HEX-5); itera até o deslocamento —
            // flanquear/kite/recuar emergem da sequência de passos.
            val passos = npc.deslocamentoEfetivo.coerceAtLeast(1)
            var moveu = false
            for (i in 0 until passos) {
                val atual = est.posicoes.firstOrNull { it.id == npcId }?.posicao ?: break
                val destino = com.gurps.ficha.domain.combat.hex.HexTaticaNpc.decidirDestino(
                    est, npcId, intencao, perfil
                ) ?: break
                if (destino == atual) break // a IA decidiu ficar (posição já é a melhor)
                if (com.gurps.ficha.domain.combat.hex.HexCoord.ORIGEM.distancia(destino) > est.raioGrade) break
                est = est.copy(posicoes = est.posicoes.map {
                    if (it.id == npcId) it.copy(posicao = destino) else it
                })
                moveu = true
            }
            // Facing final: o NPC termina o movimento ENCARANDO o herói (facing é livre no fim do
            // próprio turno — um NPC que recuasse de costas daria flanco de graça).
            val posFinal = est.posicoes.firstOrNull { it.id == npcId } ?: return null
            val posHeroi = est.posicoes.firstOrNull { it.id == "heroi" }?.posicao ?: return null
            val dirHeroi = com.gurps.ficha.domain.combat.hex.Direcao.de(posFinal.posicao, posHeroi)
            if (dirHeroi != null && posFinal.facing != dirHeroi) {
                est = est.copy(posicoes = est.posicoes.map {
                    if (it.id == npcId) it.copy(facing = dirHeroi) else it
                })
            }
            estadoTatico = est
            // FUGA na grade: FUGA_METROS (20) é INALCANÇÁVEL num raio 7 (dist máx ~14) — sem este
            // ajuste, o NPC covarde recuaria em círculos pra sempre. Recuar já ESTANDO na borda
            // (não dá mais pra se afastar) = saiu do campo → devolve FUGA_METROS e o motor marca
            // a fuga pelo critério normal.
            if (intencao.recuar && !moveu &&
                com.gurps.ficha.domain.combat.hex.HexCoord.ORIGEM.distancia(posFinal.posicao) >= est.raioGrade - 1) {
                return CombatSession.FUGA_METROS
            }
            // Mesmo sem se mover, a manobra foi consumida "se posicionando" — devolve a distância real.
            return est.distanciaHex("heroi", npcId)
        }
    }

    /**
     * Reprojeta a grade a partir do encounter DEPOIS de ações do motor que mudam distância
     * (Encontrão =1, Empurrão/Projeção knockback, Mover do NPC): cada NPC anda na linha reta
     * herói↔NPC até a distância nova ([HexPortabilidade]); mortos saem da grade.
     */
    private fun sincronizarGridComEncounter() {
        val s = sessao ?: return
        var est = estadoTatico ?: return
        val vivos = s.encounter.combatentes.filter { !it.ehHeroi && it.vivo }
        est = com.gurps.ficha.domain.combat.hex.HexSetup.manterApenas(est, vivos.map { it.id }.toSet())
        for (npc in vivos) {
            val dist = s.encounter.distancia(npc)
            if (dist == Int.MAX_VALUE) continue
            est = com.gurps.ficha.domain.combat.hex.HexPortabilidade.aplicarNovaDistancia(est, npc.id, dist)
        }
        estadoTatico = est
    }

    /** Ataques utilizáveis do herói (armas empunhadas + desarmado) e o índice escolhido (Lote 368). */
    private var ataques: List<AtaqueHeroi> = emptyList()
    private var ataqueSelecionado: Int = 0
    private var ataquesAgarrado = false // Lote 422: lembra se 'ataques' já foi filtrado para o estado preso (só desarmado)

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
    /**
     * Lote MEC-8: procura a magia no CATÁLOGO real e monta a [NpcMagia] com a mecânica CURADA (dado,
     * entrega, custo). Devolve null se o nome não existe nas 879 — o app NÃO inventa magia.
     */
    private fun npcMagiaDoCatalogo(nomeMagia: String, nh: Int): NpcMagia? {
        val ctx = context ?: return null
        val alvo = com.gurps.ficha.domain.filters.CatalogFilters.normalizarBusca(nomeMagia)
        val def = runCatching {
            com.gurps.ficha.data.DataRepository.getInstance(ctx).magias
                .firstOrNull { com.gurps.ficha.domain.filters.CatalogFilters.normalizarBusca(it.nome) == alvo }
        }.getOrNull() ?: return null
        val mec = def.mecanica ?: return null
        // Só magia de DANO serve como ataque do NPC — o motor não sabe executar o resto no cérebro dele.
        if (mec.efeito != "dano" || mec.danoPorEnergia == null) return null
        val projetil = mec.entrega == "projetil" ||
            com.gurps.ficha.domain.magic.TipoClasseMagia.PROJETIL in MagicClassParser.parse(def.classe).classes
        // Custo e dados vêm do livro: o custo canônico (ou 1) compra os dados que a curadoria mandar.
        val custo = (def.custoOperar ?: 1).coerceIn(1, 6)
        return NpcMagia(nome = def.nome, nh = nh, projetil = projetil, custoFP = custo,
            danoDados = custo.coerceIn(1, 3))
    }

    /**
     * Lote LIMPEZA-2: combate SANDBOX (o "Combate de teste" do preview da grade). Ponto de entrada
     * PRÓPRIO para não sujar a API de produção com um parâmetro que só o teste usa — o `forcarTatico`
     * do TESTE-1 era exatamente esse cheiro.
     *
     * A única diferença para um combate normal é forçar a grade tática: fora de campanha não há
     * `modoTaticoHex` (é config da campanha), e sem grade o sandbox cairia no modo faixas.
     */
    /**
     * Lote TESTE-NPC: modo dos NPCs no combate de TESTE. Observável para o seletor da tela refletir
     * a troca; aplicado à sessão viva na hora, para não obrigar a reiniciar a luta.
     */
    var modoTesteNpc by mutableStateOf(com.gurps.ficha.domain.combat.ModoTesteNpc.NORMAL)
        private set

    fun definirModoTesteNpc(modo: com.gurps.ficha.domain.combat.ModoTesteNpc) {
        modoTesteNpc = modo
        sessao?.modoTesteNpc = modo // vale já nesta luta, sem reiniciar
        atualizarEstado()
    }

    /**
     * Lote TESTE-SANDBOX: por que o combate de teste não abriu. `null` = abriu normalmente.
     *
     * Existe porque o retorno de [iniciarCombate] era **descartado** aqui: quando o motor recusava
     * (herói a 0 PV de uma luta anterior, sessão presa, bestiário ausente), o botão simplesmente
     * não fazia nada e o jogador não recebia explicação nenhuma.
     */
    var avisoSandbox by mutableStateOf<String?>(null)
        private set

    fun limparAvisoSandbox() { avisoSandbox = null }

    /**
     * Lote MEC-23: mágicas do herói esperando ele decidir se MANTÉM (paga PF) ou deixa acabar.
     * Manter é opcional em GURPS — antes o motor cobrava sozinho e a mágica nunca largava.
     */
    val manutencaoPendente: List<com.gurps.ficha.domain.combat.CombatSession.ManutencaoPendente>
        get() = sessao?.manutencaoPendente.orEmpty()

    fun resolverManutencao(magiaId: String, manter: Boolean) {
        sessao?.resolverManutencao(magiaId, manter)
        atualizarEstado()
    }

    fun iniciarCombateSandbox(
        inimigos: List<Pair<String, Int>>, distanciaM: Int = 5,
        magiasDeclaradas: List<String> = emptyList(),
    ): String {
        avisoSandbox = null
        // Lote TESTE-SANDBOX: numa ARENA DE TESTE, uma luta presa não pode bloquear a próxima —
        // o botão existe justamente para recomeçar à vontade. Em campanha a recusa continua valendo.
        if (sessao != null) encerrarManual()
        taticoForcadoUmaVez = true
        val r = iniciarCombate(inimigos, distanciaM, "ninguem", magiasDeclaradas)
        // Lote TESTE-NPC: o sandbox é o ÚNICO lugar que sai do NORMAL.
        sessao?.modoTesteNpc = modoTesteNpc
        avisoSandbox = explicarRecusaSandbox(r)
        com.gurps.ficha.domain.combat.SagaLog.mecanica(
            if (avisoSandbox == null) "combate de teste iniciado" else "combate de teste RECUSADO: $r")
        atualizarEstado()
        return r
    }

    /** Traduz o código de recusa do motor em algo acionável na tela (null = não houve recusa). */
    private fun explicarRecusaSandbox(retorno: String): String? = when {
        retorno.startsWith("heroi_incapacitado") -> {
            val p = viewModel.personagem
            "⚠️ Combate de teste não abriu: seu herói está com ${p.pontosVidaRolagemAtual ?: p.pontosVida} " +
                "de ${p.pontosVida} PV (0 ou menos). Restaure os PV na ficha e tente de novo."
        }
        retorno.startsWith("sem_contexto") ->
            "⚠️ Combate de teste não abriu: o app não conseguiu carregar o bestiário. Reabra a aba Saga."
        retorno.startsWith("combate_ja_ativo") ->
            "⚠️ Já havia um combate em andamento. Feche-o e tente de novo."
        else -> null
    }

    /** Consumido (e zerado) pelo próximo [iniciarCombate] — não vaza para combates seguintes. */
    private var taticoForcadoUmaVez = false

    fun iniciarCombate(
        inimigos: List<Pair<String, Int>>, distanciaM: Int = 5, surpresa: String = "ninguem",
        /** Lote MEC-8: nomes de magias REAIS do catálogo que os conjuradores do encontro sabem. */
        magiasDeclaradas: List<String> = emptyList(),
    ): String {
        // Lote LIMPEZA-2: consome o "forçado do sandbox" JÁ AQUI — antes das saídas antecipadas
        // (sem contexto / combate ativo / herói incapacitado). Se consumisse lá embaixo, um retorno
        // antecipado deixaria a flag ligada e ela vazaria para o PRÓXIMO combate, que seria real.
        val taticoForcado = taticoForcadoUmaVez.also { taticoForcadoUmaVez = false }
        val ctx = context ?: return "sem_contexto"
        // Combate anterior já encerrado mas não fechado pela UI: limpa antes (senão o painel da luta
        // passada fica preso, sobretudo agora que o jogador pode digitar após cair sem tocar "Fechar").
        if (sessao?.encerrado == true) encerrarManual()
        // Não reabrir POR CIMA de um combate EM CURSO. Como a caixa de texto agora fica sempre disponível
        // (Frente 2), o jogador pode falar com o Narrador no meio da luta e ele chamar iniciar_combate de
        // novo — o que sobrescreveria a sessão e dispararia um 2º loop. Recusa, como acao_npc já faz.
        if (sessao != null && !sessao!!.encerrado) {
            return "combate_ja_ativo: já existe um combate em andamento; conduza-o pelos botões do painel ou aguarde o fim."
        }
        val p = viewModel.personagem
        val bestiario = BestiarioCatalogo.carregar(ctx)

        val heroiComb = Combatente(
            id = "heroi", nome = p.nome.ifBlank { "Herói" }, ehHeroi = true,
            dx = p.dx, velocidadeBasica = p.velocidadeBasica.toDouble(),
            deslocamento = p.deslocamentoAtual.coerceAtLeast(1), pvMax = p.pontosVida,
            pvAtual = (p.pontosVidaRolagemAtual ?: p.pontosVida),
            pfAtual = (p.pontosFadigaRolagemAtual ?: p.pontosFadiga)
        )
        // Lote 423: sangramento persistido na ficha (de uma cena/luta anterior) entra no combate já ativo.
        if (p.sagaSangrando) {
            heroiComb.sangramentoAtivo = true
            heroiComb.condicoes.add(Condicao.SANGRANDO)
            heroiComb.sangramentoPenalidadeLocal = p.sagaSangramentoPenalidadeLocal ?: 0
            heroiComb.sangramentoIntervaloSeg = p.sagaSangramentoIntervaloSeg ?: 60
        }

        // Item 5 do teste de batalha: NÃO abrir um combate que o herói já perdeu de saída. Se ele
        // entra a 0 PV ou abaixo (PV residual de uma luta anterior, sem cura/descanso), o motor
        // declararia "derrotado sem dano". Em vez disso, declina SEM criar sessão (nada de painel
        // preso nem derrota fantasma) e devolve o estado factual para o Narrador narrar a
        // consequência e, se for o caso, curar/descansar antes de qualquer luta (lei 9 do prompt).
        val pvEntrada = heroiComb.pvAtual
        if (pvEntrada <= 0) {
            return "heroi_incapacitado: o herói está caído à beira da morte (PV $pvEntrada de ${heroiComb.pvMax}) " +
                "e NÃO pode lutar — não há combate. Narre a consequência (ele desfalece, é capturado ou precisa ser " +
                "resgatado) e só inicie uma luta depois que ele recuperar PV (gastar_recurso com quantidade NEGATIVA = descanso/cura)."
        }

        val distancias = mutableMapOf<String, Int>()
        val combatentes = mutableListOf(heroiComb)
        var n = 0
        inimigos.forEach { (idOuConceito, qtd) ->
            val criatura = bestiario.get(idOuConceito.lowercase().trim())
            repeat(qtd.coerceIn(1, 12)) {
                n++
                val cid = "${idOuConceito.lowercase().trim()}_$n"
                val nome = if (qtd > 1) "${criatura?.nome ?: idOuConceito} $n" else (criatura?.nome ?: idOuConceito)
                var comb = criatura?.novoCombatente(cid, nome) ?: Combatente(
                    id = cid, nome = nome, dx = 10, velocidadeBasica = 5.0, deslocamento = 5, pvMax = 10,
                    stats = NpcStats(iq = 12, armaDano = "1d-1", armaTipo = "cont", armaNh = 10)
                )
                // Lote MEC-8: as mágicas do NPC vêm do CATÁLOGO (879 magias reais, curadas), nunca de
                // invenção. O Narrador declara os nomes em `iniciar_combate`; o app procura cada um e
                // usa a mecânica REAL do livro. Nome que não existe é RECUSADO (e dito no log).
                //
                // Isto substitui o "Dardo Mágico" do MA-7: aquilo era uma magia que NÃO EXISTE em GURPS
                // (é nome de D&D), com números que eu inventei (NH = IQ+3, 1d, 1 PF), disparada por um
                // regex no nome que o usuário digitava. Fidelidade ao livro é a base do projeto.
                val st = comb.stats
                if (st != null && st.magias.isEmpty() && magiasDeclaradas.isNotEmpty()) {
                    val nh = (st.iq + 3).coerceAtLeast(12)
                    val doCatalogo = magiasDeclaradas.mapNotNull { nomeMagia -> npcMagiaDoCatalogo(nomeMagia, nh) }
                    if (doCatalogo.isNotEmpty()) comb = comb.copy(stats = st.copy(magias = doCatalogo))
                }
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

        // Lote TOK-4 (VTT 2D): monta a grade tática REAL — herói na origem, NPCs espalhados a
        // distanciaM hexes — e projeta as distâncias reais (pós-colisão) de volta no encounter.
        // SÓ quando o modo tático da campanha está ligado: com estadoTatico != null o MOVER de
        // faixa some do painel (o hex verde é o Mover) — sem a grade visível isso deixaria o
        // herói sem forma de se deslocar.
        if (viewModel.sagaModoTaticoHex || viewModel.sagaModoTaticoHex3D || taticoForcado) {
            val estTatico = com.gurps.ficha.domain.combat.hex.HexSetup.setupDoEncontro(
                idsInimigos = combatentes.filter { !it.ehHeroi }.map { it.id },
                distanciaM = distanciaM
            )
            com.gurps.ficha.domain.combat.hex.HexCombatSync.projetarSetupInicial(estTatico, encounter)
            estadoTatico = estTatico
            // Lote TOK-5a: pluga a ponte posicional no motor (facing/através-de-hex/retirada real).
            s.posicaoBridge = bridgeTatico
        } else {
            estadoTatico = null
        }
        avisoTatico = null

        scope.launch { rodarLoop() }

        // Lote TOK-2: gatilho ASSÍNCRONO ("agente secundário") — pré-gera o token de IMAGEM de cada
        // TIPO de inimigo via Gemini e cacheia em disco (filesDir/tokens/inimigos/{tipo}.png).
        // Por TIPO, não por instância: 3 goblins = 1 imagem (~$0.067). Fire-and-forget: qualquer
        // falha (sem chave, sem rede, geração ruim) deixa o canvas no fallback círculo+inicial —
        // o combate NUNCA espera nem depende disso.
        val imgKey = com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_IMAGE_KEY
        val imgModel = com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_IMAGE_MODEL
        if (imgKey.isNotBlank()) {
            inimigos.map { it.first.lowercase().trim() }.distinct().forEach { tipoId ->
                val criatura = bestiario.get(tipoId)
                scope.launch {
                    runCatching {
                        com.gurps.ficha.data.storage.TokenImageStore.obterTokenInimigo(
                            ctx, tipo = tipoId,
                            nomeVisivel = criatura?.nome ?: tipoId,
                            descricao = criatura?.descricao
                        ) { prompt ->
                            com.gurps.ficha.data.network.GeminiImageService
                                .gerarImagem(imgKey, imgModel, prompt, rotuloLog = "token:$tipoId")?.bytes
                        }
                    }
                }
            }
        }
        return s.resumo()
    }

    /** Perfil de combate do herói montado da ficha ATUAL (o bridge usa p/ dano fora do loop). */
    fun perfilHeroi(): HeroiPerfilCombate = construirPerfilHeroi(viewModel.personagem)

    /** Lote 424 (T1-2): modificador situacional do Narrador (ação improvisada → mecânica). Null = alvo inválido. */
    fun aplicarModificador(alvoId: String, valor: Int, aplicaEm: String, motivo: String, duracaoRodadas: Int?): String? {
        val s = sessao ?: return null
        val txt = s.aplicarModSituacional(alvoId, aplicaEm, valor, motivo, duracaoRodadas) ?: return null
        publicarLog(); atualizarEstado()
        return txt
    }

    // ── Efeitos aplicados pelo Narrador (B8), fora do loop de turnos ────────────

    /** Aplica dano a um combatente vivo do encontro (NPC ou herói). Retorna relatório factual ou null. */
    fun aplicarDanoCombatente(alvoId: String, danoBase: Int, tipo: DanoTipo, local: LocalAtaque): String? {
        val s = sessao ?: return null
        val alvo = s.encounter.combatentes.firstOrNull { it.id == alvoId && it.vivo } ?: return null
        val ht = if (alvo.ehHeroi) s.heroiPerfil.ht else (alvo.stats?.ht ?: 10)
        val rd = if (alvo.ehHeroi) s.heroiPerfil.rd else (alvo.stats?.rd ?: 0)
        val dano = HitLocationRules.aplicarDano(alvo.pvMax, danoBase, tipo, local, rd, alvo.stats?.tolerancia ?: ToleranciaFerimentos.NORMAL)
        // Lote PONTE-2: passa tipo/local p/ o dano narrado (corte/perfuração) também marcar sangramento, igual ao combate.
        val fer = InjuryRules.ferir(alvo, dano.pvSubtrair, ht, Random.Default, tipo = tipo, local = local)
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

    /**
     * Ajusta PV/PF do HERÓI dentro de um combate em curso (cura/descanso ou dreno fora do fluxo de dano).
     * Em combate o motor é a fonte da verdade do PV — escrever só na ficha (gastarRecurso) seria sobrescrito
     * no próximo dano/fim. Aqui altera o combatente e sincroniza a ficha. delta>0 restaura, delta<0 debita.
     * pfMaxFicha = teto de PF (o Combatente não guarda PF máx). Retorna o relatório ou null se não há combate.
     */
    fun ajustarRecursoHeroiEmCombate(recurso: String, delta: Int, pfMaxFicha: Int): String? {
        val s = sessao ?: return null
        val h = s.heroi
        when (recurso.lowercase().trim()) {
            "pv" -> {
                h.pvAtual = (h.pvAtual + delta).coerceAtMost(h.pvMax); viewModel.sagaDefinirPvAtual(h.pvAtual.coerceAtLeast(0))
                // Lote PONTE-2: cura em combate (primeiros socorros MB p.424 / magia / vantagem Cura MB p.52) estanca o sangramento.
                if (delta > 0 && com.gurps.ficha.domain.combat.InjuryRules.estancarSangramento(h)) s.log += "🩹 ${h.nome} estanca o sangramento."
            }
            "pf" -> { h.pfAtual = (h.pfAtual + delta).coerceIn(0, pfMaxFicha); viewModel.sagaDefinirPfAtual(h.pfAtual) }
            else -> return null
        }
        val txt = if (delta >= 0) "➕ ${h.nome} recupera $delta de ${recurso.uppercase()}."
                  else "➖ ${h.nome} perde ${-delta} de ${recurso.uppercase()}."
        s.log += txt
        s.reavaliarFim()
        publicarLog(); atualizarEstado()
        if (s.encerrado) finalizar()
        return txt
    }

    // ── Ações do herói (a UI chama) ──────────────────────────────────────────

    fun heroiAtaca(alvoId: String, manobra: Manobra, local: LocalAtaque, modo: AtaqueTotalModo = AtaqueTotalModo.DETERMINADO, enganoso: Int = 0, telegrafico: Boolean = false,
                   dedicadoModo: DedicadoModo = DedicadoModo.DETERMINADO, benefDefensivo: CombatResolver.TipoDefesa? = null) {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        val ataque = ataques.getOrNull(ataqueSelecionado) ?: return
        if (armaDespreparadaBloqueia(ataque)) return // Lote 398: arma despreparada → precisa Preparar antes
        s.heroiAtaca(ataque, alvoId, manobra, local, modo, enganoso, telegrafico, dedicadoModo, benefDefensivo)
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

    /**
     * Lote MA-3a: o herói CONJURA uma magia no combate (manobra Concentrar, consome o turno). O
     * controller extrai da ficha o NH básico e a Aptidão, monta o contexto puro e delega ao motor
     * ([CombatSession.heroiConjurar], que usa o resolvedor do MA-2). [alvoId] = null para automagia.
     */
    fun heroiConjurar(magiaId: String, alvoId: String?, energiaInvestida: Int, pvQueimados: Int = 0, danoPorEnergia: Boolean = false) {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        val p = viewModel.personagem
        val magia = p.magias.firstOrNull { it.definicaoId == magiaId || it.nome == magiaId } ?: return
        val aptidao = MagicEngine.getNivelAptidaoMagicaParaMagia(p, null)
        val classe = MagicClassParser.parse(magia.classe)
        val distancia = if (alvoId == null) 0
            else s.encounter.combatentes.firstOrNull { it.id == alvoId }?.let { s.distancia(it) } ?: 0
        val mana = viewModel.sagaNivelMana // Lote MA-5: mana ambiente da cena
        if (!com.gurps.ficha.domain.magic.MagicMana.podeOperar(mana, ehMago = aptidao > 0)) {
            s.log += "🚫 Aqui a mana está ${mana.name.lowercase()} — você não consegue conjurar ${magia.nome}."
            publicarLog(); atualizarEstado(); return
        }
        // Lote AR-1/MA-8: pega do catálogo a `mecanica` (regra estruturada) e um RESUMO do efeito (da
        // descrição fiel do livro) — o resumo vai pro log e chega ao Narrador (que lê o feed do combate).
        val def = context?.let { c ->
            runCatching { com.gurps.ficha.data.DataRepository.getInstance(c).getMagiaPorId(magia.definicaoId) }.getOrNull()
        }
        val mecanica = def?.mecanica
        // Lote MEC-5b: o custo CANÔNICO (conferido contra a descrição fiel do livro) manda; o parser
        // do texto do cabeçalho é só fallback. A auditoria achou dezenas de transcrições erradas —
        // Arma Congelante tem "03/01" no cabeçalho, mas o livro diz "4 para operar, 1 para manter".
        val custo = custoCanonico(def) ?: MagicEnergy.parse(magia.energia)
        // Lote MEC-2: a `notas` da curadoria é a REGRA destilada (ambiente/controle/informação, que o
        // motor não executa); a descrição é o sabor. O Narrador recebe as duas — antes a nota era
        // gravada e nunca lida por ninguém.
        val resumoEfeito = listOfNotNull(
            mecanica?.notas?.takeIf { it.isNotBlank() },
            resumoDaDescricao(def?.descricao ?: magia.texto)?.takeIf { it.isNotBlank() },
        ).joinToString(" — ").take(400).ifBlank { null }
        val ctx = ContextoConjuracao(
            nhBasico = magia.calcularNivel(p, aptidao),
            classe = classe,
            mana = mana,
            distanciaMetros = distancia,
            tocando = false,
            veOuToca = true,
            pvQueimados = pvQueimados.coerceAtLeast(0), // Lote MA-3b: queimar PV (−1 NH/PV, paga em PV)
            raioAreaMetros = 1,               // MA-3d: área centrada num hex
            danoPorEnergia = danoPorEnergia,  // Lote MA-6: magia de dano direta (1d/energia)
            mecanica = mecanica,              // Lote AR-1
            resumoEfeito = resumoEfeito,      // Lote MA-8: descrição do efeito pro Narrador
        )
        // Tempo de operação (Magia p.9): base do catálogo, reduzido por NH alto. >1s → multi-turno.
        val tempoBase = tempoDe(def, magia.tempoOperacao) // MEC-5b: canônico > texto do cabeçalho
        val tempo = MagicCasting.tempoOperacaoAjustado(tempoBase, magia.calcularNivel(p, aptidao))
        val res = s.heroiConjurar(ctx, custo, energiaInvestida, magia.nome, alvoId, tempo)
        registrarSeMagiaAtiva(s, magia, classe, custo, res, alvoId, magia.calcularNivel(p, aptidao), mecanica, energiaInvestida, def)
        sincronizarRecursosHeroi(s)
        depoisDaAcaoDoHeroi()
    }

    /** Lote MA-3d-4: após uma conjuração bem-sucedida de magia com DURAÇÃO, registra-a como ativa (tick de manutenção). */
    private fun registrarSeMagiaAtiva(
        s: CombatSession, magia: com.gurps.ficha.model.MagiaSelecionada,
        classe: com.gurps.ficha.domain.magic.ClasseParseada,
        custo: com.gurps.ficha.domain.magic.CustoEnergia,
        res: CombatSession.ResultadoConjuracaoCombate, alvoId: String?, nhBasico: Int,
        mecanica: com.gurps.ficha.domain.magic.MagiaMecanica? = null, energiaInvestida: Int = 0,
        def: com.gurps.ficha.model.MagiaDefinicao? = null,
    ) {
        if (!res.sucesso || res.emAndamento) return
        // Lote MEC-2: o alvo RESISTIU → a magia não pega. Sem isto um Debilitar resistido ainda
        // aplicaria −3 ST, porque `sucesso` só quer dizer "a conjuração deu certo".
        if (res.alvoResistiu) return
        // Lote MEC-6: buff de UM ÚNICO USO (Aumentar Força/Destreza/Vitalidade). Tem que vir ANTES do
        // filtro de duração: são "Instant." e por isso eram descartados aqui — o herói pagava o PF e
        // não acontecia nada.
        if (mecanica?.buffUmUnicoUso == true &&
            com.gurps.ficha.domain.magic.MagicMechanics.temBuffEstruturado(mecanica)) {
            s.aplicarBuffDeUmUso(
                magia.nome,
                com.gurps.ficha.domain.magic.MagicMechanics.calcularBuff(mecanica, energiaInvestida, alvoId ?: "heroi"),
            )
            return
        }
        // Projétil/Toque/Área não são "buffs ativos" (dano imediato / carregam / mira própria).
        if (TipoClasseMagia.PROJETIL in classe.classes || TipoClasseMagia.TOQUE in classe.classes ||
            TipoClasseMagia.AREA in classe.classes) return
        val (dur, durSeg) = duracaoDe(def, magia.duracao) // MEC-5b: canônico > texto do cabeçalho
        // Lote MEC-5: buff PERMANENTE/indefinido vale até ser dissipado (`dissiparMagiaAtiva`), então
        // precisa ser registrado. Mas só quando TEM buff estruturado — senão as 154 mágicas "Perm."
        // de ambiente/narrado (Criar Água…) virariam ruído de "fica ATIVA" no feed.
        val temBuffNumerico = mecanica != null &&
            com.gurps.ficha.domain.magic.MagicMechanics.temBuffEstruturado(mecanica)
        val duracaoRastreavel = dur == TipoDuracao.TEMPORARIA || dur == TipoDuracao.DURADOURA ||
            (dur == TipoDuracao.PERMANENTE && temBuffNumerico)
        if (!duracaoRastreavel) return
        // Lote MEC-5: usa a manutenção REAL do catálogo ("04/02" = manter 2) quando ela existe; só
        // estima metade do custo (Magia p.15) quando o campo não informa. Antes a manutenção real
        // nunca era lida — o "/02" era engolido pelo parser de fração.
        val base = (custo.base ?: custo.minimo).coerceAtLeast(1)
        val manutBruta = custo.manutencao ?: kotlin.math.ceil(base / 2.0).toInt()
        val manut = if (manutBruta <= 0) 0 // "0/1" e afins: não pode ser mantida de graça — 0 é 0.
            else MagicCost.custoAjustadoPorNH(manutBruta, nhBasico)
        // Lote MEC-2: buff com NÚMERO curado → o motor aplica os deltas e reverte ao expirar.
        // Sem número (Corpo de Água, Ambidestria) o buff segue só narrado — regra de ouro.
        val buff = mecanica
            ?.takeIf { com.gurps.ficha.domain.magic.MagicMechanics.temBuffEstruturado(it) }
            ?.let {
                com.gurps.ficha.domain.magic.MagicMechanics.calcularBuff(
                    it, energiaInvestida, alvoId ?: "heroi" // sem alvo explícito, o buff é no operador
                )
            }
        s.registrarMagiaAtiva(magia.nome, "heroi", alvoId, durSeg, if (dur == TipoDuracao.DURADOURA) 0 else manut, dur, exigeConcentracao = false, buff = buff)
    }

    /** Extrai (tipo de duração, segundos) do campo `duracao` do catálogo ("1 min.", "permanente", "instantâneo"). */
    private fun parseDuracao(txt: String?): Pair<TipoDuracao, Int> =
        com.gurps.ficha.domain.magic.MagicTime.parseDuracao(txt)

    // ── Lote MEC-5b: preferir o número CANÔNICO do catálogo ao parser do texto ───────────────────
    // Os campos de texto (`duracao`, `energia`, `tempoOperacao`) são a transcrição do cabeçalho, e a
    // auditoria contra a `descricao` (fiel ao livro) provou que ela erra em dezenas de mágicas. Onde
    // o número conferido existe, ele manda; onde não existe (240 mágicas, confiança < alta), o
    // parser continua valendo — fallback honesto, não invenção.

    /** Custo conferido contra o livro, ou null quando a mágica não tem número canônico. */
    private fun custoCanonico(def: com.gurps.ficha.model.MagiaDefinicao?): com.gurps.ficha.domain.magic.CustoEnergia? {
        val operar = def?.custoOperar ?: return null
        return com.gurps.ficha.domain.magic.CustoEnergia(
            base = operar, variavel = false, minimo = operar.coerceAtLeast(1),
            manutencao = def.custoManter, original = def.energia.orEmpty(),
        )
    }

    /** (tipo, segundos) conferidos contra o livro; cai no parser do texto quando não há canônico. */
    private fun duracaoDe(def: com.gurps.ficha.model.MagiaDefinicao?, txt: String?): Pair<TipoDuracao, Int> {
        val tipo = when (def?.duracaoTipo) {
            "instantanea" -> TipoDuracao.INSTANTANEA
            "temporaria" -> TipoDuracao.TEMPORARIA
            "permanente" -> TipoDuracao.PERMANENTE
            else -> return parseDuracao(txt)
        }
        return tipo to (def.duracaoSeg ?: 0)
    }

    /** Tempo de operação conferido contra o livro; cai no parser do texto quando não há canônico. */
    private fun tempoDe(def: com.gurps.ficha.model.MagiaDefinicao?, txt: String?): Int =
        def?.tempoOperacaoSeg?.takeIf { it > 0 } ?: parseTempoSeg(txt)

    /**
     * Lote MA-3d: começa a MIRA de uma magia de ÁREA — o app entra em modo "toque um hex". A magia só
     * é lançada quando o jogador toca o centro no grid ([resolverMiraAreaNoHex]).
     */
    fun iniciarMiraArea(magiaId: String, raio: Int, energia: Int, pvQueimar: Int, causaDano: Boolean = false) {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        val nome = viewModel.personagem.magias.firstOrNull { it.definicaoId == magiaId || it.nome == magiaId }?.nome ?: "magia"
        miraAreaPendente = MiraAreaUi(magiaId, nome, raio.coerceAtLeast(1), energia, pvQueimar.coerceAtLeast(0), causaDano)
        avisoTatico = "Toque um hex para o centro de $nome (raio ${raio}m)"
    }

    /** Lote MA-3d: cancela a mira de área sem lançar. */
    fun cancelarMiraArea() { miraAreaPendente = null; avisoTatico = null }

    /** Lote MA-3d: resolve a magia de área centrada no [centro] tocado — calcula quem está no raio pela grade. */
    private fun resolverMiraAreaNoHex(centro: com.gurps.ficha.domain.combat.hex.HexCoord) {
        val s = sessao ?: return
        val mira = miraAreaPendente ?: return
        miraAreaPendente = null; avisoTatico = null
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        val p = viewModel.personagem
        val magia = p.magias.firstOrNull { it.definicaoId == mira.magiaId || it.nome == mira.magiaId } ?: return
        val aptidao = MagicEngine.getNivelAptidaoMagicaParaMagia(p, null)
        val def = context?.let { c -> runCatching { com.gurps.ficha.data.DataRepository.getInstance(c).getMagiaPorId(magia.definicaoId) }.getOrNull() }
        val est = estadoTatico
        // raio em metros = raio em hexes (1 hex = 1 m). raio 1 = só o hex central → alcance 0 de hex.
        val hexRaio = (mira.raio - 1).coerceAtLeast(0)
        val hexesArea = com.gurps.ficha.domain.combat.hex.HexGrid.range(centro, hexRaio).toSet()
        val alvos = est?.posicoes?.filter { it.id != "heroi" && it.posicao in hexesArea }?.map { it.id } ?: emptyList()
        // Penalidade de distância = herói até a BORDA mais próxima da área (Magia p.11).
        val distBorda = est?.posicoes?.firstOrNull { it.id == "heroi" }
            ?.let { (it.posicao.distancia(centro) - hexRaio).coerceAtLeast(0) } ?: 0
        val ctx = ContextoConjuracao(
            nhBasico = magia.calcularNivel(p, aptidao),
            classe = MagicClassParser.parse(magia.classe),
            mana = viewModel.sagaNivelMana, // Lote MA-5
            distanciaMetros = distBorda,
            raioAreaMetros = mira.raio,
            pvQueimados = mira.pvQueimar,
            danoPorEnergia = mira.causaDano, // Lote MA-6
            mecanica = def?.mecanica,        // Lote AR-1
            resumoEfeito = resumoDaDescricao(def?.descricao ?: magia.texto), // Lote MA-8
        )
        // MEC-5b: canônico > texto também na área.
        // Lote MEC-14: distância de cada alvo ao CENTRO (1 hex = 1 m) — a explosão decai com ela.
        val distCentro = est?.posicoes?.filter { it.id in alvos }
            ?.associate { it.id to it.posicao.distancia(centro) } ?: emptyMap()
        s.heroiConjurarArea(ctx, custoCanonico(def) ?: MagicEnergy.parse(magia.energia), mira.energia, magia.nome, alvos, distCentro)
        sincronizarRecursosHeroi(s)
        depoisDaAcaoDoHeroi()
    }

    /** Lote MA-3d-2: descarrega a mágica de Toque carregada num inimigo adjacente (consome o turno). */
    fun heroiEntregarToque(alvoId: String) {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        s.heroiEntregarToque(alvoId)
        sincronizarRecursosHeroi(s)
        depoisDaAcaoDoHeroi()
    }

    /** Lote MA-3d-2: dissipa a mágica de Toque carregada (ação livre — não gasta o turno). */
    fun heroiDissiparToque() {
        val s = sessao ?: return
        s.dissiparToque(); publicarLog(); atualizarEstado()
    }

    /** Lote MA-3d-3: as mágicas de BLOQUEIO conhecidas viram opções no card "Defenda-se!" (valor = NH da magia). */
    private fun opcoesBloqueioMagico(s: CombatSession): List<CombatResolver.OpcaoDefesa> {
        val p = viewModel.personagem
        if (p.magias.isEmpty()) return emptyList()
        val aptidao = MagicEngine.getNivelAptidaoMagicaParaMagia(p, null)
        val temPf = s.heroi.pfAtual > 0
        return p.magias.mapNotNull { m ->
            // Lote MEC-12: só magia de Bloqueio PURO é oferecida como DEFESA.
            //
            // Bug pego no aparelho: o card "Defenda-se!" contra uma Bola de Fogo oferecia
            // "🔮 Aumentar Força (bloqueio) 15" — que não defende de nada (só aumenta ST) e, com NH 15,
            // deixaria o herói praticamente imune a magia de graça.
            //
            // A distinção está nos DADOS: classe `"Bloqueio"` pura = reação que PROTEGE de um ataque
            // chegando (Desviar Energia cita "mágica Bola de Fogo ou Relâmpago"; Desviar/Devolver
            // Projétil; Bloquear = BD instantâneo; Robustez = RD instantânea; Braço de Ferro;
            // Apanhar Projétil). Já `"Comum ou Bloqueio"` significa que a magia PODE ser lançada como
            // reação — mas o efeito dela não é defensivo (Aumentar Força/Destreza/IQ/Vitalidade,
            // Fascinar, Dominar Animal). Essas saem da lista.
            val classes = MagicClassParser.parse(m.classe).classes
            val bloqueioPuro = TipoClasseMagia.BLOQUEIO in classes && TipoClasseMagia.COMUM !in classes
            if (!bloqueioPuro) return@mapNotNull null
            CombatResolver.OpcaoDefesa(
                tipo = CombatResolver.TipoDefesa.BLOQUEIO,
                valorFinal = m.calcularNivel(p, aptidao),
                componentes = emptyList(),
                disponivel = temPf,
                motivoIndisponivel = if (!temPf) "sem PF" else null,
                magiaBloqueioId = m.definicaoId.ifBlank { m.nome },
                magiaBloqueioNome = m.nome,
            )
        }
    }

    /** Lote MA-3c: continua a conjuração multi-turno (mais uma manobra Concentrar). */
    fun heroiContinuarConjuracao() {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        s.continuarConjuracao()
        sincronizarRecursosHeroi(s)
        depoisDaAcaoDoHeroi()
    }

    /** Lote MA-3c: aborta a conjuração inacabada (sem custo). NÃO consome o turno — o herói reescolhe. */
    fun heroiAbortarConjuracao() {
        val s = sessao ?: return
        s.abortarConjuracao()
        publicarLog(); atualizarEstado()
    }

    private fun sincronizarRecursosHeroi(s: CombatSession) {
        viewModel.sagaDefinirPfAtual(s.heroi.pfAtual)
        viewModel.sagaDefinirPvAtual(s.heroi.pvAtual.coerceAtLeast(0))
    }

    /**
     * Lote MA-8: resumo do EFEITO da magia a partir da descrição fiel — o texto ANTES das seções
     * mecânicas (Duração/Custo/Item/Pré-requisito), sem a 1ª linha de classe, limitado a ~300 chars.
     * Vai pro log da conjuração narrada e chega ao Narrador (IA) via o feed do combate.
     */
    private fun resumoDaDescricao(desc: String?): String? {
        val d = desc?.trim().orEmpty()
        if (d.isBlank()) return null
        val corte = listOf("Duração:", "Duracao:", "Custo:", "Custo básico", "Custo basico", "Tempo de operação",
            "Tempo de operacao", "Pré-requisito", "Pre-requisito", "\nItem\n", "\nItem ")
            .mapNotNull { m -> d.indexOf(m).takeIf { it > 0 } }.minOrNull() ?: d.length
        val linhas = d.substring(0, corte).split("\n").map { it.trim() }.filter { it.isNotBlank() }
        val corpo = if (linhas.size > 1 && linhas[0].length < 30) linhas.drop(1) else linhas
        val s = corpo.joinToString(" ").trim()
        if (s.isBlank()) return null
        return if (s.length > 300) s.take(300).trimEnd() + "…" else s
    }

    /** Extrai os segundos do campo `tempoOperacao` do catálogo ("1 seg.", "3 seg.", "1 min."). */
    private fun parseTempoSeg(txt: String?): Int =
        com.gurps.ficha.domain.magic.MagicTime.parseTempoSeg(txt)

    /** Rótulo curto da classe de magia para o seletor de conjuração. */
    private fun rotuloClasse(c: TipoClasseMagia): String = when (c) {
        TipoClasseMagia.COMUM -> "Comum"; TipoClasseMagia.AREA -> "Área"
        TipoClasseMagia.PROJETIL -> "Projétil"; TipoClasseMagia.TOQUE -> "Toque"
        TipoClasseMagia.BLOQUEIO -> "Bloqueio"; TipoClasseMagia.INFORMACAO -> "Informação"
        TipoClasseMagia.ENCANTAMENTO -> "Encantamento"; TipoClasseMagia.ESPECIAL -> "Especial"
    }

    /** Lote MA-3a: monta a lista de magias conjuráveis do herói para o seletor (só na vez dele). */
    private fun montarMagiasConjuraveis(s: CombatSession, vezHeroi: Boolean): List<MagiaConjuravelUi> {
        if (!vezHeroi) return emptyList()
        val p = viewModel.personagem
        if (p.magias.isEmpty()) return emptyList()
        val aptidao = MagicEngine.getNivelAptidaoMagicaParaMagia(p, null)
        val temPf = s.heroi.pfAtual > 0
        return p.magias.map { m ->
            val classe = MagicClassParser.parse(m.classe)
            val custo = MagicEnergy.parse(m.energia)
            val ehProjetil = TipoClasseMagia.PROJETIL in classe.classes
            val ehArea = TipoClasseMagia.AREA in classe.classes
            val ehToque = TipoClasseMagia.TOQUE in classe.classes
            val custoTxt = when {
                custo.variavel && ehProjetil -> "Varia (1d/pto)"
                custo.variavel -> "Varia"
                custo.base != null -> "${custo.base} PF"
                custo.fracao != null -> "${custo.fracao}×raio PF"
                else -> "Varia"
            }
            // Lote MEC-7: a curadoria diz se o EFEITO escala com energia e até onde (Escudo: 2 PF por
            // +1 de Defesa, teto +4). Sem isto o jogador não tinha como escolher e levava o mínimo.
            val mecUi = context?.let { c ->
                runCatching { com.gurps.ficha.data.DataRepository.getInstance(c).getMagiaPorId(m.definicaoId)?.mecanica }.getOrNull()
            }
            val escala = com.gurps.ficha.domain.magic.MagicMechanics.escalaDeEnergia(mecUi)
            // Lote MEC-10: cura também escala com energia — e o jogador precisa poder ESCOLHER
            // quanto gastar (era a queixa: as magias de cura não davam essa opção).
            val ehCuraMagia = com.gurps.ficha.domain.magic.MagicMechanics.temCuraEstruturada(mecUi)
            val escalaCura = ehCuraMagia && mecUi != null && !mecUi.curaTotal
            val tetoCura = if (escalaCura) com.gurps.ficha.domain.magic.MagicMechanics.tetoEnergiaCura(mecUi!!) else 1
            val dicaCura = if (escalaCura)
                "cada 1 PF = ${mecUi!!.curaPvPorEnergia} PV (até ${mecUi.curaMaxPv})" else null
            MagiaConjuravelUi(
                id = m.definicaoId.ifBlank { m.nome },
                nome = m.nome,
                classe = classe.classes.joinToString("/") { rotuloClasse(it) },
                nhBasico = m.calcularNivel(p, aptidao),
                custoTexto = custoTxt,
                ehProjetil = ehProjetil,
                ehArea = ehArea,
                ehToque = ehToque,
                // Lote MEC-9: o teto do seletor de DANO vem da REGRA da magia ("1 a 3" → 3;
                // "2 a 2×AM" → 2×Aptidão), não mais da Aptidão pura. Sem isto o MEC-7 deixava
                // despejar 10 num Toque Candente (custo 1 a 3) e sair 10d.
                aptidaoMagica = MagicEnergy.tetoDeEnergiaDano(m.energia, aptidao),
                efeito = mecUi?.efeito, // MEC-20
                custoEstimado = (custo.base ?: custo.minimo).coerceAtLeast(1),
                castavel = temPf,
                motivo = if (!temPf) "sem PF" else "",
                escalaComEnergia = escala != null || escalaCura,
                energiaMax = escala?.energiaMax ?: tetoCura,
                dicaEnergia = escala?.dica ?: dicaCura,
                ehCura = ehCuraMagia,
            )
        }
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

    /** Lote 409: Encontrão — colisão corporal (dano mútuo por contusão + derrubada). MB p.371. */
    fun heroiEncontrao(alvoId: String) {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        s.heroiEncontrao(alvoId)
        depoisDaAcaoDoHeroi()
    }

    /** Lote 410: Empurrão — empurra o alvo (projeção/knockback, sem lesão). MB p.371. */
    fun heroiEmpurrao(alvoId: String) {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        s.heroiEmpurrao(alvoId)
        depoisDaAcaoDoHeroi()
    }

    /** Lote 411: Imobilizar — prende no chão um oponente agarrado (Disputa de ST). MB p.371. */
    fun heroiImobilizar(alvoId: String) {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        s.heroiImobilizar(alvoId)
        depoisDaAcaoDoHeroi()
    }

    /** Lote 412: Estrangular — asfixia um oponente agarrado pelo pescoço (Disputa de ST → sufoco). MB p.371. */
    fun heroiEstrangular(alvoId: String) {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        s.heroiEstrangular(alvoId)
        depoisDaAcaoDoHeroi()
    }

    /** Lote PONTE-1: Chave de Membro num alvo agarrado (Disputa de ST → dano cont). AM p.69-70. */
    fun heroiChaveMembro(alvoId: String, perna: Boolean = false) {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        s.heroiChaveMembro(alvoId, perna)
        depoisDaAcaoDoHeroi()
    }

    /** Lote PONTE-1: Mata-Leão (estrangular com 2 mãos, +3 ST) num alvo agarrado. AM p.77. */
    fun heroiMataLeao(alvoId: String) {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        s.heroiMataLeao(alvoId)
        depoisDaAcaoDoHeroi()
    }

    /** Lote 408: Golpe Rápido — dois ataques corpo-a-corpo a −6 cada (mantém a defesa). MB p.370. */
    fun heroiGolpeRapido(alvoId: String, local: LocalAtaque) {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        val ataque = ataques.getOrNull(ataqueSelecionado) ?: return
        if (armaDespreparadaBloqueia(ataque)) return
        s.heroiGolpeRapido(ataque, alvoId, local)
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

    /** Lote 422: Desvencilhar-se — o herói AGARRADO/IMOBILIZADO tenta se soltar (Disputa de ST). */
    fun heroiDesvencilhar() {
        val s = sessao ?: return
        if (!s.combatenteAtual().ehHeroi || s.encerrado) return
        s.heroiDesvencilhar()
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
        estadoTatico = null; avisoTatico = null // Lote TOK-4: limpa a grade tática
        viradaFinalPendente = null // HEX-FACING-2: pendência não pode vazar para a próxima luta
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
        // Lote MA-3c: para a interrupção da conjuração — foto do herói ANTES do golpe do NPC.
        val pvHeroiAntes = s.heroi.pvAtual
        val atordoadoAntes = Condicao.ATORDOADO in s.heroi.condicoes
        // BD do escudo do herói não vale contra arma de fogo (MB p.375): detecta pela flag ou pelo nome da arma.
        val contraFogo = npc.stats?.let { it.armaDeFogo || CombatSession.pareceArmaDeFogo(it.armaNome) } ?: false
        // Passa a arma EMPUNHADA p/ as regras de Aparar (esgrima/desbalanceada/Não/à distância).
        // Sem opções (ex.: herói sem defesa ativa após Ataque Total) → resolve direto, sem card.
        val opcoesBase = if (s.intencaoAtacaHeroi(intencao)) s.opcoesDefesaHeroi(
            armaPronta = ataques.getOrNull(ataqueSelecionado), contraArmaDeFogo = contraFogo,
            contraAtaqueCorpoACorpo = !intencao.aDistancia, // Lote 389: Retirada só vs corpo-a-corpo
            atacanteAdjacente = s.distancia(npc) <= 1, // Lote 390: aparar tiro só se o atirador está a 1m
            ataqueComArma = npc.stats?.armaNome?.isNotBlank() == true, // Lote 391: −3 ao aparar arma com as mãos nuas
            ambidestro = temAmbidestria(viewModel.personagem), // Lote 405: anula o −2 da apara com a mão inábil
            // Lote 407: ataque por ponta (GdP) dispensa o −3 da apara desarmada; inferido do dano PERF (perfuração).
            ataqueGdP = !intencao.aDistancia && npc.stats?.let { CombatSession.tipoDano(it.armaTipo) == DanoTipo.PERF } == true
        ) else emptyList()
        // Lote TOK-5a (MB p.374/375/390): FACING do ataque contra o herói na grade —
        //  FLANCO → todas as defesas −2 e o BD do escudo sai (HexRegrasFacing, do HEX-4);
        //  COSTAS → defesa ANULADA: sem card (o motor resolve com surpresa=true e narra).
        val facingAtaque = if (estadoTatico != null) bridgeTatico.facingDoAtaque(npcId, "heroi") else null
        val opcoesBaseFacing = when (facingAtaque) {
            com.gurps.ficha.domain.combat.hex.Facing.COSTAS -> emptyList()
            com.gurps.ficha.domain.combat.hex.Facing.FLANCO ->
                com.gurps.ficha.domain.combat.hex.HexRegrasFacing.ajustarOpcoesDefesa(
                    opcoesBase, com.gurps.ficha.domain.combat.hex.Facing.FLANCO,
                    bonusEscudoEmbutido = s.heroiPerfil.bonusEscudo
                )
            else -> opcoesBase
        }
        // Lote MA-3d-3: acrescenta as mágicas de BLOQUEIO conhecidas como opções de defesa (Magia p.12).
        // Só quando há defesa possível (não vale contra golpe fulminante / pelas costas — opcoes vazia).
        val opcoes = if (opcoesBaseFacing.isNotEmpty()) opcoesBaseFacing + opcoesBloqueioMagico(s) else opcoesBaseFacing
        if (intencao.conjurar != null) {
            val magiaNpc = intencao.conjurar!!
            // Lote MEC-8: a defesa contra mágica de NPC agora é INTERATIVA, igual à defesa contra arma.
            // Projétil mágico → o herói ESQUIVA (nunca apara/bloqueia com escudo, Magia p.12), mas PODE
            // usar uma mágica de Bloqueio. Mágica de dano NÃO-projétil (Comum resistível) não tem defesa
            // ativa — resiste por atributo, resolução direta. Antes o motor esquivava sozinho: o jogador
            // reclamou (com razão) que não teve rolagem de esquiva nem opção de defesa.
            if (magiaNpc.projetil) {
                val soEsquiva = opcoes.filter {
                    it.tipo == CombatResolver.TipoDefesa.ESQUIVA || it.magiaBloqueioId != null
                }
                if (soEsquiva.isNotEmpty()) {
                    val deferred = CompletableDeferred<CombatResolver.OpcaoDefesa>()
                    defesaPendente = DefesaPendenteUi(
                        atacante = npc.nome,
                        descricaoAtaque = "${npc.nome} conjura ${magiaNpc.nome} em você! Esquive ou bloqueie com magia.",
                        opcoes = soEsquiva, deferred = deferred
                    )
                    atualizarEstado()
                    val escolha = deferred.await()
                    defesaPendente = null
                    // A rolagem de defesa é feita AQUI (o jogador escolheu; o motor rola os 3d6), igual
                    // ao fluxo contra arma — o herói vê a soma no log.
                    val soma = (1..3).sumOf { Random.nextInt(1, 7) }
                    if (escolha.magiaBloqueioId != null) {
                        val mb = viewModel.personagem.magias.firstOrNull { it.definicaoId == escolha.magiaBloqueioId || it.nome == escolha.magiaBloqueioId }
                        val custoFP = mb?.let { MagicEnergy.parse(it.energia) }?.let { it.base ?: it.minimo } ?: 1
                        s.aplicarBloqueioMagico(custoFP, escolha.magiaBloqueioNome ?: "magia")
                        viewModel.sagaDefinirPfAtual(s.heroi.pfAtual)
                    }
                    s.npcConjurar(npcId, magiaNpc, DefesaHeroi(escolha.tipo, escolha.valorFinal, soma))
                } else {
                    s.npcConjurar(npcId, magiaNpc) // sem defesa possível (raro) → resolução direta
                }
            } else {
                s.npcConjurar(npcId, magiaNpc) // dano não-projétil: resiste por atributo, sem defesa ativa
            }
        } else if (s.intencaoAtacaHeroi(intencao) && opcoes.isNotEmpty()) {
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
            // Lote MA-3d-3: escolheu um bloqueio mágico → paga o custo (NÃO reduz por NH) e quebra a concentração.
            if (escolha.magiaBloqueioId != null) {
                val magia = viewModel.personagem.magias.firstOrNull { it.definicaoId == escolha.magiaBloqueioId || it.nome == escolha.magiaBloqueioId }
                val custoFP = magia?.let { MagicEnergy.parse(it.energia) }?.let { it.base ?: it.minimo } ?: 1
                s.aplicarBloqueioMagico(custoFP, escolha.magiaBloqueioNome ?: "magia")
                viewModel.sagaDefinirPfAtual(s.heroi.pfAtual)
            }
            val soma = (1..3).sumOf { Random.nextInt(1, 7) }
            // Defesa Total (Dupla, Lote 388): prepara a melhor 2ª defesa de TIPO diferente — usada só se a 1ª falhar.
            // Sem variante "com recuo" na 2ª (recuo é 1×/turno e já pode ter ido na 1ª).
            val secundaria = if (s.heroiDefesaTotalDupla)
                opcoes.filter { it.tipo != escolha.tipo && !it.recuo }.maxByOrNull { it.valorFinal }
                    ?.let { DefesaHeroi(it.tipo, it.valorFinal, (1..3).sumOf { Random.nextInt(1, 7) }) }
            else null
            s.npcResolve(npcId, intencao, DefesaHeroi(escolha.tipo, escolha.valorFinal, soma, escolha.recuo, escolha.jogarSeAoChao, escolha.acrobatica), secundaria)
        } else {
            s.npcResolve(npcId, intencao, null)
        }
        // Lote MA-3c: se o herói estava CONJURANDO e levou dano / ficou atordoado, testa a concentração
        // (Vontade−3; atordoado perde automático — Magia p.7).
        val atordoadoAgora = Condicao.ATORDOADO in s.heroi.condicoes
        if (s.conjuracaoEmAndamento != null && (s.heroi.pvAtual < pvHeroiAntes || (atordoadoAgora && !atordoadoAntes))) {
            s.interromperConjuracaoSeConjurando(atordoado = atordoadoAgora, rolagemVontade = (1..3).sumOf { Random.nextInt(1, 7) })
            sincronizarRecursosHeroi(s)
        }
        verificarDesprepararPorEstado(s) // Lote 406: cair/atordoar com arma desbalanceada a deixa despreparada
        publicarLog()
        atualizarEstado()
    }

    /** Lote 406 (MB p.383): se o herói caiu/atordoou empunhando uma arma desbalanceada, ela fica despreparada. */
    private fun verificarDesprepararPorEstado(s: CombatSession) {
        val arma = ataques.getOrNull(ataqueSelecionado) ?: return
        if (arma.aDistancia || arma.apararTipo != ApararTipo.DESBALANCEADA || s.armaDespreparada(arma.rotulo)) return
        val abalado = Condicao.ATORDOADO in s.heroi.condicoes || s.heroi.caido || s.heroi.postura == Postura.DEITADO
        if (abalado) {
            s.marcarArmaDespreparada(arma.rotulo)
            s.log += "  └ você foi abalado empunhando ${arma.rotulo.substringBefore(" (").trim()} (desbalanceada) — ela ficou despreparada (MB p.383)."
        }
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
        // Lote TOK-4: reprojeta a grade tática a partir do encounter (Encontrão/Empurrão/Projeção/
        // Mover do NPC mudaram distâncias; mortos saem). Roda ANTES de montar o CombatUiState.
        sincronizarGridComEncounter()
        val vezHeroi = s.combatenteAtual().ehHeroi && !s.encerrado
        // Lote 422: herói preso só ataca desarmado — reconstrói os ataques na TRANSIÇÃO preso↔livre.
        val heroiPreso = Condicao.AGARRADO in s.heroi.condicoes || Condicao.IMOBILIZADO in s.heroi.condicoes
        if (heroiPreso != ataquesAgarrado) {
            ataquesAgarrado = heroiPreso
            ataques = construirAtaques(viewModel.personagem, agarrado = heroiPreso)
            ataqueSelecionado = ataqueSelecionado.coerceIn(0, (ataques.size - 1).coerceAtLeast(0))
        }
        val combs = s.encounter.combatentes.map { c ->
            val dist = s.distancia(c)
            CombatenteUi(
                id = c.id, nome = c.nome, ehHeroi = c.ehHeroi,
                pvAtual = c.pvAtual, pvMax = c.pvMax, postura = c.postura.rotulo,
                condicoes = c.condicoes.map { it.rotulo },
                distanciaM = if (c.ehHeroi) 0 else dist,
                faixa = if (c.ehHeroi) FaixaDistancia.ENGAJADO else FaixaDistancia.de(dist),
                vivo = c.vivo,
                pfPct = if (c.ehHeroi) viewModel.personagem.pontosFadiga
                    .takeIf { it > 0 }?.let { (c.pfAtual.toFloat() / it).coerceIn(0f, 1f) } else null,
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
            if (!ranged && alvos.isNotEmpty() && Manobra.GOLPE_RAPIDO !in it) it.add(Manobra.GOLPE_RAPIDO) // Lote 408
            if (!ranged && alvos.isNotEmpty() && Manobra.ENCONTRAO !in it) it.add(Manobra.ENCONTRAO) // Lote 409
            if (!ranged && alvos.isNotEmpty() && Manobra.EMPURRAO !in it) it.add(Manobra.EMPURRAO) // Lote 410
            // Ataque Dedicado/Defensivo (Lote PONTE-4, AM p98): vêm de manobrasLegais (engajado), mas são corpo-a-corpo
            // e exigem alvo no alcance — removidos à distância ou sem alvo adjacente.
            if (ranged || alvos.isEmpty()) it.removeAll(listOf(Manobra.ATAQUE_DEDICADO, Manobra.ATAQUE_DEFENSIVO))
            // Imobilizar/Estrangular/Chaves (Lotes 411/412/PONTE-1): só fazem sentido com um inimigo já AGARRADO.
            if (s.inimigos.any { e -> e.vivo && Condicao.AGARRADO in e.condicoes }) {
                if (Manobra.IMOBILIZAR !in it) it.add(Manobra.IMOBILIZAR)
                if (Manobra.ESTRANGULAR !in it) it.add(Manobra.ESTRANGULAR)
                if (Manobra.CHAVE_MEMBRO !in it) it.add(Manobra.CHAVE_MEMBRO)
                if (Manobra.MATA_LEAO !in it) it.add(Manobra.MATA_LEAO)
            }
            // Herói AGARRADO/IMOBILIZADO (Lote 422, MB p.371): manobras restritas + Desvencilhar-se. Sem
            // Apontar/Aguardar/Concentrar/Fintar/à distância nem ações de avanço; imobilizado mal age.
            if (Condicao.AGARRADO in s.heroi.condicoes || Condicao.IMOBILIZADO in s.heroi.condicoes) {
                it.removeAll(listOf(Manobra.APONTAR, Manobra.AGUARDAR, Manobra.CONCENTRAR, Manobra.FINTAR,
                    Manobra.FOGO_RETENCAO, Manobra.ENCONTRAO, Manobra.EMPURRAO, Manobra.MOVER_E_ATACAR,
                    Manobra.GOLPE_RAPIDO, Manobra.DERRUBAR, Manobra.AGARRAR, Manobra.CHAVE_MEMBRO, Manobra.MATA_LEAO,
                    Manobra.ATAQUE_DEDICADO, Manobra.ATAQUE_DEFENSIVO))
                if (Condicao.IMOBILIZADO in s.heroi.condicoes)
                    it.removeAll(listOf(Manobra.MOVER, Manobra.ATAQUE, Manobra.ATAQUE_TOTAL, Manobra.MUDAR_POSTURA))
                else it.remove(Manobra.MOVER) // agarrado não desloca sem 2× a ST do oponente (abstraído)
                if (Manobra.DESVENCILHAR !in it) it.add(Manobra.DESVENCILHAR)
            }
            // Lote TOK-4 (achado da revisão): com a grade tática ativa, o MOVER de faixa some do
            // painel — o toque no hex verde É a manobra Mover (a diretiva do lote é SUBSTITUIR;
            // manter os dois misturaria a semântica da Disparada e duplicaria o caminho).
            if (estadoTatico != null) it.remove(Manobra.MOVER)
        }
        estado = CombatUiState(
            rodada = s.encounter.rodadaAtual,
            combatentes = combs,
            vezDoHeroi = vezHeroi,
            manobrasHeroi = manobras,
            magiasConjuraveis = montarMagiasConjuraveis(s, vezHeroi),
            conjurando = s.conjuracaoEmAndamento?.let { ConjurandoUi(it.nome, it.turnosRestantes) },
            toqueCarregado = s.toqueCarregado?.nome,
            magiasAtivas = s.magiasAtivas.map { m ->
                m.magiaId + if (m.duracao == com.gurps.ficha.domain.magic.TipoDuracao.TEMPORARIA) " (${m.segundosParaProximaCobranca}s)" else ""
            },
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
        // Lote 423: o sangramento SOBREVIVE ao combate — persiste na ficha (o passar_tempo do Narrador processa).
        // Herói desmaiado sangrando PERSISTE (pode ser tratado); a −1×PV ou pior (morto/beira da morte, e a ficha
        // clampa o PV em 0 de toda forma) NÃO persiste — evita "cadáver sangrando" na cena seguinte.
        if (s.heroi.sangramentoAtivo && s.heroi.pvAtual > -s.heroi.pvMax)
            viewModel.sagaDefinirSangramento(s.heroi.sangramentoPenalidadeLocal, s.heroi.sangramentoIntervaloSeg)
        else viewModel.sagaLimparSangramento()
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
        vontade = p.vontade,
        // Dano por GdP do herói — usado no Empurrão (Lote 410).
        danoGdP = p.danoGdP,
        // NH em Acrobacia (null se não tem) — Esquiva Acrobática (Lote 414).
        acrobacia = p.periciasTotais.firstOrNull {
            CatalogFilters.normalizarBusca(it.definicaoId).removePrefix("racial_") == "acrobacia"
        }?.calcularNivel(p)
    )

    /**
     * Lista de ataques utilizáveis (Lote 368): cada arma EQUIPADA (corpo-a-corpo e à distância/fogo)
     * com sua perícia, NH, dano resolvido por ST e tipo correto; mais o desarmado como último recurso.
     */
    private fun construirAtaques(p: Personagem, agarrado: Boolean = false): List<AtaqueHeroi> {
        val out = mutableListOf<AtaqueHeroi>()
        // Armas CONFISCADAS (tiradas pela narrativa — desarmado/capturado) não aparecem: herói luta no soco.
        p.equipamentos.filter { it.tipo == TipoEquipamento.ARMA && !it.confiscado }.forEach { arma ->
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
        val resultado = out.sortedByDescending { it.aDistancia }
        // Lote 422 (MB p.371): herói AGARRADO/IMOBILIZADO não empunha nem golpeia arma — só ataque desarmado.
        return if (agarrado) resultado.filter { it.desarmado } else resultado
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

    /** RD do herói: maior RD entre as armaduras equipadas (aproximação de torso). Armadura CONFISCADA não conta. */
    private fun rdHeroi(p: Personagem): Int = p.equipamentos
        .filter { it.tipo == TipoEquipamento.ARMADURA && !it.confiscado }
        .mapNotNull { it.rdArmaduraExibicao()?.let { s -> Regex("\\d+").find(s)?.value?.toIntOrNull() } }
        .maxOrNull() ?: 0

    private companion object {
        val DESARMADAS = setOf("briga", "boxe", "carate", "judo", "luta_grecoromana", "caratê", "judô")
        // Lote 391: aparar uma ARMA desarmado tem o valor cheio (sem −3) com Caratê ou Judô (MB p.376).
        val MARCIAIS_APARA = setOf("carate", "caratê", "judo", "judô")
    }
}
