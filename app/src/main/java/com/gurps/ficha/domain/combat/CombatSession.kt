package com.gurps.ficha.domain.combat

import kotlin.math.floor
import kotlin.random.Random

/**
 * Lote 365 (Saga B7): SESSÃO de combate — orquestra um encontro inteiro encadeando o que os
 * lotes B1–B6 entregaram (encounter, ataque, dano localizado, ferimento, defesas, cérebro do NPC).
 * Kotlin PURO: nenhuma dependência de Android, de Compose nem da ficha. O herói chega como
 * [HeroiPerfilCombate] (o controller extrai da ficha) e os NPCs trazem [NpcStats] do bestiário.
 *
 * Quem dirige o tempo:
 *  - turno do HERÓI → a UI escolhe a manobra/alvo/local e chama [heroiAtaca]/[heroiMove]/[heroiManobra];
 *  - turno do NPC → o controller pede [npcIntencao] (cérebro ou override do Narrador), e quando a
 *    intenção é atacar o herói, a UI mostra "Defenda-se!" e devolve a defesa em [npcResolve].
 * Toda mutação de PV/condição acontece nos `Combatente` do encounter (CombatResolver.resolverTroca).
 */
class CombatSession(
    val encounter: CombatEncounter,
    val heroiPerfil: HeroiPerfilCombate,
    private val random: Random = Random.Default
) {
    /** Registro factual, linha a linha — o Narrador transforma em prosa SEM inventar números. */
    val log: MutableList<String> = mutableListOf()

    var encerrado: Boolean = false; private set
    var resultado: ResultadoCombate? = null; private set

    val heroi: Combatente get() = encounter.combatentes.first { it.ehHeroi }
    val inimigos: List<Combatente> get() = encounter.combatentes.filter { !it.ehHeroi }
    val inimigosVivos: List<Combatente> get() = inimigos.filter { it.vivo }

    fun combatenteAtual(): Combatente = encounter.combatenteAtual
    fun manobrasDoAtual(): List<Manobra> = encounter.manobrasLegais(combatenteAtual())
    fun manobrasHeroi(): List<Manobra> = encounter.manobrasLegais(heroi)

    /** Alvos válidos do herói para corpo-a-corpo (engajados) ou à distância (todos vivos). */
    fun alvosHeroi(corpoACorpo: Boolean = true): List<Combatente> =
        if (corpoACorpo) inimigosVivos.filter { encounter.distancia(it) <= 1 } else inimigosVivos

    fun distancia(c: Combatente): Int = encounter.distancia(c)

    // ── Turno do herói ───────────────────────────────────────────────────────

    /** Resultado de uma ação ofensiva (do herói ou de um NPC) já resolvida no motor. */
    data class AtaqueResultado(
        val acertou: Boolean,
        val defendeu: Boolean,
        val danoAplicado: Int,
        val alvoIncapacitado: Boolean,
        val texto: String
    )

    /**
     * O herói ataca [alvoId]. Encadeia B2 (rolar acerto) → B5 (defesa do NPC) → B3/B4 (dano/ferimento)
     * via [CombatResolver.resolverTroca]. O NPC defende automaticamente com a melhor defesa.
     */
    fun heroiAtaca(
        alvoId: String,
        manobra: Manobra = Manobra.ATAQUE,
        local: LocalAtaque = LocalAtaque.TORSO,
        ataqueTotalModo: AtaqueTotalModo = AtaqueTotalModo.DETERMINADO
    ): AtaqueResultado {
        val alvo = inimigos.firstOrNull { it.id == alvoId && it.vivo }
            ?: return AtaqueResultado(false, false, 0, false, "Alvo inválido ou já fora de combate.").also { log += it.texto }

        val aDistancia = heroiPerfil.alcanceArma >= 3 && encounter.distancia(alvo) > 1
        val atk = CombatActions.resolverAtaque(
            nhBaseArma = heroiPerfil.nhArma, manobra = manobra, postura = heroi.postura,
            local = local, visibilidade = Visibilidade.NORMAL, ataqueTotalModo = ataqueTotalModo,
            aDistancia = aDistancia, random = random
        )
        val (defTipo, defValor) = melhorDefesaNpc(alvo)
        val defSoma = rolar3d6()
        val danoBruto = rolarDano(heroiPerfil.danoArma, random) + bonusDanoForte(manobra, ataqueTotalModo)

        val troca = CombatResolver.resolverTroca(
            defensor = alvo, htDefensor = alvo.stats?.ht ?: 10, ataque = atk,
            defesaTipo = defTipo, defesaValorFinal = defValor, defesaSoma = defSoma,
            surpresa = false, danoBaseRolado = danoBruto, danoTipo = heroiPerfil.tipoDano,
            local = local, rdLocal = alvo.stats?.rd ?: 0, randomFerimento = random
        )
        log += "🗡️ Herói → ${alvo.nome}: ${troca.texto}"
        val incap = !alvo.vivo
        if (incap) log += "  └ ${alvo.nome} está fora de combate."
        verificarFim()
        return AtaqueResultado(
            acertou = atk.resultado == CombatActions.ResultadoAcerto.ACERTO,
            defendeu = troca.defendeu, danoAplicado = troca.dano?.pvSubtrair ?: 0,
            alvoIncapacitado = incap, texto = troca.texto
        )
    }

    /** Manobra não-ofensiva do herói (Defesa Total, Avaliar, Mudar de Postura, Não Fazer Nada…). */
    fun heroiManobra(manobra: Manobra, novaPostura: Postura? = null): String {
        if (manobra == Manobra.MUDAR_POSTURA && novaPostura != null) heroi.postura = novaPostura
        val txt = "🛡️ Herói: ${manobra.rotulo}" + (novaPostura?.let { " (${it.rotulo})" } ?: "")
        log += txt
        return txt
    }

    /** Herói se aproxima do alvo (ou de todos) pela sua margem de deslocamento. */
    fun heroiMove(alvoId: String? = null, afastar: Boolean = false): String {
        val passo = heroi.deslocamento.coerceAtLeast(1)
        val alvos = alvoId?.let { id -> inimigos.filter { it.id == id } } ?: inimigosVivos
        alvos.forEach { encounter.moverEmRelacaoAoHeroi(it.id, if (afastar) passo else -passo) }
        val txt = "🏃 Herói ${if (afastar) "recua" else "avança"} ${passo}m."
        log += txt
        return txt
    }

    // ── Turno do NPC ───────────────────────────────────────────────────────

    /** Decide a intenção do NPC: usa o override do Narrador (B8) ou o cérebro tático (B6). */
    fun npcIntencao(npcId: String, override: NpcCombatBrain.IntencaoNpc? = null): NpcCombatBrain.IntencaoNpc {
        val npc = inimigos.first { it.id == npcId }
        return override ?: NpcCombatBrain.decidir(npc, encounter, alvoId = heroi.id, random = random)
    }

    /** true quando a intenção do NPC é um ataque que atinge o herói → a UI deve pedir "Defenda-se!". */
    fun intencaoAtacaHeroi(intencao: NpcCombatBrain.IntencaoNpc): Boolean =
        intencao.alvoId == heroi.id &&
            (intencao.manobra == Manobra.ATAQUE || intencao.manobra == Manobra.ATAQUE_TOTAL ||
                intencao.manobra == Manobra.MOVER_E_ATACAR)

    /** Opções de defesa do herói para o card "Defenda-se!" (aplica recuo/Defesa Total/aparas extras). */
    fun opcoesDefesaHeroi(recuo: Boolean = false, defesaTotalEm: CombatResolver.TipoDefesa? = null): List<CombatResolver.OpcaoDefesa> =
        CombatResolver.opcoesDefesa(
            esquivaBase = heroiPerfil.esquiva, aparaBase = heroiPerfil.apara, bloqueioBase = heroiPerfil.bloqueio,
            defesasUsadas = heroi.defesasUsadas, recuo = recuo, defesaTotalEm = defesaTotalEm
        )

    /**
     * Resolve o turno do NPC [npcId] com a [intencao] já decidida. Se for ataque ao herói, exige
     * [defesaHeroi] (escolha + rolagem feitas na UI). Movimentos atualizam a faixa de distância.
     */
    fun npcResolve(
        npcId: String,
        intencao: NpcCombatBrain.IntencaoNpc,
        defesaHeroi: DefesaHeroi? = null
    ): AtaqueResultado {
        val npc = inimigos.firstOrNull { it.id == npcId && it.vivo }
            ?: return AtaqueResultado(false, false, 0, false, "NPC fora de combate.")

        when (intencao.manobra) {
            Manobra.MOVER -> {
                val passo = npc.deslocamento.coerceAtLeast(1)
                if (intencao.recuar) {
                    encounter.moverEmRelacaoAoHeroi(npc.id, passo)
                    log += "🏃 ${npc.nome} recua ${passo}m (${intencao.motivo})."
                    if (encounter.distancia(npc) >= FUGA_METROS) {
                        npc.condicoes.add(Condicao.INCONSCIENTE) // sai do encontro (fugiu)
                        log += "  └ ${npc.nome} fugiu do combate."
                    }
                } else {
                    encounter.moverEmRelacaoAoHeroi(npc.id, -passo)
                    log += "🏃 ${npc.nome} avança ${passo}m (${intencao.motivo})."
                }
                verificarFim()
                return AtaqueResultado(false, false, 0, false, log.last())
            }
            Manobra.MOVER_E_ATACAR -> {
                encounter.definirDistancia(npc.id, 1) // chega ao corpo-a-corpo antes de golpear
            }
            else -> { /* ATAQUE / ATAQUE_TOTAL: resolve abaixo */ }
        }

        if (!intencaoAtacaHeroi(intencao)) {
            log += "• ${npc.nome}: ${intencao.manobra.rotulo} (${intencao.motivo})."
            return AtaqueResultado(false, false, 0, false, log.last())
        }

        val stats = npc.stats ?: return AtaqueResultado(false, false, 0, false, "${npc.nome} sem stats de ataque.")
        val atk = CombatActions.resolverAtaque(
            nhBaseArma = stats.armaNh, manobra = intencao.manobra, postura = npc.postura,
            local = intencao.local, visibilidade = Visibilidade.NORMAL,
            aDistancia = intencao.aDistancia, random = random
        )
        // Sem escolha de defesa (herói atordoado/sem opção) → só Esquiva passiva da ficha.
        val def = defesaHeroi ?: DefesaHeroi(CombatResolver.TipoDefesa.ESQUIVA, heroiPerfil.esquiva, rolar3d6())
        val danoTotal = rolarDano(stats.armaDano, random) + bonusDanoForte(intencao.manobra, AtaqueTotalModo.FORTE)

        val troca = CombatResolver.resolverTroca(
            defensor = heroi, htDefensor = heroiPerfil.ht, ataque = atk,
            defesaTipo = def.tipo, defesaValorFinal = def.valorFinal, defesaSoma = def.soma,
            surpresa = false, danoBaseRolado = danoTotal, danoTipo = tipoDano(stats.armaTipo),
            local = intencao.local, rdLocal = heroiPerfil.rd, randomFerimento = random
        )
        // marca a defesa usada (bloqueio/recuo 1×/turno; aparas extras cumulativas)
        registrarDefesaUsada(def.tipo)
        log += "⚔️ ${npc.nome} → Herói: ${troca.texto}"
        val incap = !heroi.vivo
        if (incap) log += "  └ O herói caiu!"
        verificarFim()
        return AtaqueResultado(
            acertou = atk.resultado == CombatActions.ResultadoAcerto.ACERTO,
            defendeu = troca.defendeu, danoAplicado = troca.dano?.pvSubtrair ?: 0,
            alvoIncapacitado = incap, texto = troca.texto
        )
    }

    // ── Avanço de turno / fim ─────────────────────────────────────────────────

    /** Avança até o próximo combatente que ainda pode agir; ao fim de cada turno, recupera atordoamento. */
    fun avancarTurno(): Combatente {
        if (encerrado) return combatenteAtual()
        // tenta recuperar atordoamento de quem acabou de agir
        val anterior = combatenteAtual()
        if (Condicao.ATORDOADO in anterior.condicoes) {
            val ht = if (anterior.ehHeroi) heroiPerfil.ht else (anterior.stats?.ht ?: 10)
            if (InjuryRules.recuperaAtordoamento(ht, random)) {
                anterior.condicoes.remove(Condicao.ATORDOADO)
                log += "• ${anterior.nome} recupera-se do atordoamento."
            }
        }
        // zera defesas do turno de quem vai começar
        var prox = encounter.proximoTurno()
        var guarda = 0
        while (!prox.vivo && guarda++ < encounter.combatentes.size) prox = encounter.proximoTurno()
        prox.defesasUsadas = DefesasUsadas()
        return prox
    }

    private fun verificarFim() {
        if (encerrado) return
        if (!heroi.vivo) { encerrado = true; resultado = ResultadoCombate.DERROTA; log += "💀 Combate encerrado: o herói foi derrotado." }
        else if (inimigosVivos.isEmpty()) { encerrado = true; resultado = ResultadoCombate.VITORIA; log += "🏆 Combate encerrado: vitória do herói." }
    }

    /** Reavalia o fim após um efeito aplicado FORA do loop de turnos (ex.: dano do Narrador). B8. */
    fun reavaliarFim() = verificarFim()

    /** Resumo factual do estado atual — base do que o Narrador vai narrar. */
    fun resumo(): String = encounter.estadoResumo()

    // ── Helpers internos ───────────────────────────────────────────────────

    private fun registrarDefesaUsada(tipo: CombatResolver.TipoDefesa) {
        heroi.defesasUsadas = when (tipo) {
            CombatResolver.TipoDefesa.BLOQUEIO -> heroi.defesasUsadas.copy(bloqueouEsteTurno = true)
            CombatResolver.TipoDefesa.ESQUIVA -> heroi.defesasUsadas.copy(esquivouEsteTurno = true)
            CombatResolver.TipoDefesa.APARA -> {
                val mapa = heroi.defesasUsadas.aparasPorArma.toMutableMap()
                mapa["arma"] = (mapa["arma"] ?: 0) + 1
                heroi.defesasUsadas.copy(aparasPorArma = mapa)
            }
        }
    }

    /** Melhor defesa de um NPC: Esquiva (Vel.Básica+3) vs Aparar (NH/2+3, só corpo-a-corpo). */
    private fun melhorDefesaNpc(npc: Combatente): Pair<CombatResolver.TipoDefesa, Int> {
        val esquiva = floor(npc.velocidadeBasica).toInt() + 3
        val melee = (npc.stats?.alcanceMetros ?: 1) <= 2
        val apara = if (melee) (npc.stats?.armaNh ?: 0) / 2 + 3 else 0
        return if (apara > esquiva) CombatResolver.TipoDefesa.APARA to apara
        else CombatResolver.TipoDefesa.ESQUIVA to esquiva
    }

    private fun bonusDanoForte(manobra: Manobra, modo: AtaqueTotalModo): Int =
        if (manobra == Manobra.ATAQUE_TOTAL && modo == AtaqueTotalModo.FORTE) 2 else 0

    private fun rolar3d6(): Int = (1..3).sumOf { random.nextInt(1, 7) }

    companion object {
        /** A partir desta distância um NPC em fuga é considerado fora do encontro. */
        const val FUGA_METROS = 20

        /** Mapeia a string de tipo do bestiário/ficha para o enum de dano (B3). */
        fun tipoDano(tipo: String): DanoTipo = when (tipo.lowercase().trim()) {
            "corte", "cort" -> DanoTipo.CORT
            "pi-" -> DanoTipo.PI_MENOS
            "pi" -> DanoTipo.PI
            "pi+" -> DanoTipo.PI_MAIS
            "pi++" -> DanoTipo.PI_MAIS_MAIS
            "perf" -> DanoTipo.PERF
            else -> DanoTipo.CONT
        }

        /** Rola uma expressão de dano GURPS "<n>d[±m]" (ex.: "2d-1", "1d+2", "3d"). Mínimo 0. */
        fun rolarDano(expr: String, random: Random = Random.Default): Int {
            val m = Regex("""(\d+)d([+-]\d+)?""").find(expr.lowercase().replace(" ", "")) ?: return 0
            val qtd = m.groupValues[1].toIntOrNull() ?: 0
            val mod = m.groupValues.getOrNull(2)?.toIntOrNull() ?: 0
            val rol = (1..qtd).sumOf { random.nextInt(1, 7) }
            return (rol + mod).coerceAtLeast(0)
        }
    }
}

/** Perfil de combate do herói — o controller extrai da ficha; mantém a sessão pura. */
data class HeroiPerfilCombate(
    val nhArma: Int,
    val danoArma: String,        // expressão "2d-1"
    val tipoDano: DanoTipo,
    val esquiva: Int,
    val apara: Int? = null,
    val bloqueio: Int? = null,
    val ht: Int = 10,
    val rd: Int = 0,
    val alcanceArma: Int = 1
)

/** Defesa escolhida pelo jogador no card "Defenda-se!" (tipo + valor final + 3d6 rolado). */
data class DefesaHeroi(
    val tipo: CombatResolver.TipoDefesa,
    val valorFinal: Int,
    val soma: Int
)

enum class ResultadoCombate { VITORIA, DERROTA, FUGA }
