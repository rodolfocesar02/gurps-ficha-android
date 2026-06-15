package com.gurps.ficha.domain.combat

import com.gurps.ficha.domain.roll.CriticoRules
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

    // Avaliar (Lote 370): bônus cumulativo (até +3) no PRÓXIMO ataque corpo-a-corpo ao alvo avaliado.
    private var avaliarAlvoId: String? = null
    private var avaliarStacks: Int = 0
    private fun limparAvaliar() { avaliarAlvoId = null; avaliarStacks = 0 }

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
     * O herói ataca [alvoId] com o [ataque] escolhido (arma/perícia). Encadeia B2 (rolar acerto) →
     * B5 (defesa do NPC) → B3/B4 (dano/ferimento). À distância sofre penalidade por metro e o NPC
     * só pode Esquivar; corpo-a-corpo o NPC usa a melhor defesa (Esquiva/Aparar).
     */
    fun heroiAtaca(
        ataque: AtaqueHeroi,
        alvoId: String,
        manobra: Manobra = Manobra.ATAQUE,
        local: LocalAtaque = LocalAtaque.TORSO,
        ataqueTotalModo: AtaqueTotalModo = AtaqueTotalModo.DETERMINADO
    ): AtaqueResultado {
        val alvo = inimigos.firstOrNull { it.id == alvoId && it.vivo }
            ?: return AtaqueResultado(false, false, 0, false, "Alvo inválido ou já fora de combate.").also { log += it.texto }

        val dist = encounter.distancia(alvo)
        val modsExtra: List<CombatActions.ComponenteMod> = buildList {
            if (ataque.aDistancia) {
                val pen = penalidadeDistancia(dist)
                if (pen != 0) add(CombatActions.ComponenteMod("distância ${dist}m", pen))
            } else if (avaliarAlvoId == alvoId && avaliarStacks > 0) {
                // Avaliar só vale corpo-a-corpo, contra o alvo avaliado, no ataque seguinte (MB p.365).
                add(CombatActions.ComponenteMod("avaliar", avaliarStacks))
            }
        }
        val atk = CombatActions.resolverAtaque(
            nhBaseArma = ataque.nh, manobra = manobra, postura = heroi.postura,
            local = local, visibilidade = Visibilidade.NORMAL, ataqueTotalModo = ataqueTotalModo,
            aDistancia = ataque.aDistancia, modsExtra = modsExtra, random = random
        )
        // Contra ataque à distância o alvo só Esquiva; corpo-a-corpo usa a melhor defesa.
        val (defTipo, defValor) = if (ataque.aDistancia)
            CombatResolver.TipoDefesa.ESQUIVA to esquivaNpc(alvo) else melhorDefesaNpc(alvo)
        val defSoma = rolar3d6()
        val danoBruto = rolarDano(ataque.danoExpr, random) + bonusDanoForte(manobra, ataqueTotalModo)

        val troca = CombatResolver.resolverTroca(
            defensor = alvo, htDefensor = alvo.stats?.ht ?: 10, ataque = atk,
            defesaTipo = defTipo, defesaValorFinal = defValor, defesaSoma = defSoma,
            surpresa = false, danoBaseRolado = danoBruto, danoTipo = ataque.tipo,
            local = local, rdLocal = alvo.stats?.rd ?: 0, randomFerimento = random
        )
        log += narrarTroca("Você", alvo.nome, ataque.rotulo.substringBefore(" (").trim(), ataque.aDistancia, atk, defTipo, troca, local, ataque.tipo)
        val incap = !alvo.vivo
        limparAvaliar() // o bônus de Avaliar é consumido neste ataque
        verificarFim()
        return AtaqueResultado(
            acertou = atk.resultado == CombatActions.ResultadoAcerto.ACERTO,
            defendeu = troca.defendeu, danoAplicado = troca.dano?.pvSubtrair ?: 0,
            alvoIncapacitado = incap, texto = troca.texto
        )
    }

    /** Manobra não-ofensiva do herói (Defesa Total, Concentrar, Não Fazer Nada…) e Mudar de Postura. */
    fun heroiManobra(manobra: Manobra, novaPostura: Postura? = null): String {
        if (manobra == Manobra.MUDAR_POSTURA && novaPostura != null && novaPostura in posturasAlcancaveis()) {
            heroi.postura = novaPostura
        }
        limparAvaliar()
        val txt = if (manobra == Manobra.MUDAR_POSTURA) "🧍 Você muda para ${heroi.postura.rotulo}."
            else "🛡️ Você: ${manobra.rotulo}."
        log += txt
        return txt
    }

    /** Avaliar (MB p.365): +1 cumulativo (máx +3) no próximo ataque corpo-a-corpo ao alvo. */
    fun heroiAvaliar(alvoId: String): String {
        if (avaliarAlvoId == alvoId) avaliarStacks = (avaliarStacks + 1).coerceAtMost(3)
        else { avaliarAlvoId = alvoId; avaliarStacks = 1 }
        val nome = inimigos.firstOrNull { it.id == alvoId }?.nome ?: "o alvo"
        val txt = "👁️ Você avalia $nome (+$avaliarStacks no próximo golpe corpo-a-corpo)."
        log += txt
        return txt
    }

    /**
     * Posturas para as quais o herói PODE mudar agora (MB p.365): de deitado não se levanta direto —
     * só vai para ajoelhado/sentado/rastejando antes de ficar em pé.
     */
    fun posturasAlcancaveis(): List<Postura> = when (heroi.postura) {
        Postura.DEITADO -> listOf(Postura.RASTEJANDO, Postura.SENTADO, Postura.AJOELHADO)
        else -> Postura.values().filter { it != heroi.postura }
    }

    /** Herói se move até [metros] (clamp no Deslocamento) aproximando/afastando do alvo (ou de todos). */
    fun heroiMove(alvoId: String? = null, afastar: Boolean = false, metros: Int = Int.MAX_VALUE): String {
        val passo = metros.coerceIn(1, heroi.deslocamento.coerceAtLeast(1))
        val alvos = alvoId?.let { id -> inimigos.filter { it.id == id } } ?: inimigosVivos
        alvos.forEach { encounter.moverEmRelacaoAoHeroi(it.id, if (afastar) passo else -passo) }
        limparAvaliar()
        val nome = alvoId?.let { id -> inimigos.firstOrNull { it.id == id }?.nome } ?: "os inimigos"
        val txt = "🏃 Você ${if (afastar) "recua ${passo}m de" else "avança ${passo}m até"} $nome."
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
        val modsNpc: List<CombatActions.ComponenteMod> = if (intencao.aDistancia) {
            val pen = penalidadeDistancia(encounter.distancia(npc))
            if (pen != 0) listOf(CombatActions.ComponenteMod("distância", pen)) else emptyList()
        } else emptyList()
        val atk = CombatActions.resolverAtaque(
            nhBaseArma = stats.armaNh, manobra = intencao.manobra, postura = npc.postura,
            local = intencao.local, visibilidade = Visibilidade.NORMAL,
            aDistancia = intencao.aDistancia, modsExtra = modsNpc, random = random
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
        log += narrarTroca(npc.nome, "você", stats.armaNome, intencao.aDistancia, atk, def.tipo, troca, intencao.local, tipoDano(stats.armaTipo))
        val incap = !heroi.vivo
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

    /**
     * Lote 369: compõe uma linha de combate EVOCATIVA (voz de mestre) e DETERMINÍSTICA, preservando
     * os números num colchete técnico. Sem IA — instantânea. "você" leva verbo na 3ª pessoa (PT-BR),
     * então as mesmas formas servem para o herói e para os NPCs.
     */
    private fun narrarTroca(
        atacante: String,
        alvo: String,
        arma: String,
        aDistancia: Boolean,
        atk: CombatActions.RelatorioAtaque,
        defesaTipo: CombatResolver.TipoDefesa,
        troca: CombatResolver.RelatorioTroca,
        local: LocalAtaque,
        tipo: DanoTipo
    ): String {
        val icone = if (aDistancia) "🎯" else "🗡️"
        val verbo = if (aDistancia) "dispara" else "ataca"
        val comArma = if (arma.isNotBlank()) " com $arma" else ""
        // Colchete técnico: conta completa do acerto (mostra postura/local/distância) + dado.
        val tecAtk = "${atk.calculo.descricao()}; rolou ${atk.soma}"

        if (atk.resultado == CombatActions.ResultadoAcerto.FALHA) {
            return if (atk.critico == CriticoRules.ResultadoCritico.FALHA_CRITICA)
                "💥 $atacante $verbo$comArma contra $alvo e comete uma FALHA CRÍTICA! [$tecAtk]"
            else "$icone $atacante $verbo$comArma e erra $alvo. [$tecAtk]"
        }
        if (troca.defendeu) {
            val def = when (defesaTipo) {
                CombatResolver.TipoDefesa.ESQUIVA -> "$alvo se esquiva"
                CombatResolver.TipoDefesa.APARA -> "$alvo apara o golpe"
                CombatResolver.TipoDefesa.BLOQUEIO -> "$alvo bloqueia"
            }
            return "$icone $atacante $verbo$comArma, mas $def! [$tecAtk · def ${troca.defesaValor}, rolou ${troca.defesaSoma}]"
        }
        val dano = troca.dano
        val cabeca = if (atk.critico == CriticoRules.ResultadoCritico.DECISIVO) "⭐ GOLPE CERTEIRO! $atacante" else "$icone $atacante"
        val onde = if (local == LocalAtaque.TORSO) "" else " ${preposicaoLocal(local)} ${local.rotulo}"
        if (dano == null || dano.pvSubtrair <= 0) {
            return "$cabeca acerta $alvo$onde$comArma, mas a proteção absorve tudo (0 de dano). [$tecAtk · RD ${dano?.rdEfetiva ?: 0}]"
        }
        val efeito = when (troca.ferimento?.efeito) {
            InjuryRules.EfeitoFerimento.MORTO -> " $alvo tomba sem vida!"
            InjuryRules.EfeitoFerimento.INCONSCIENTE -> " $alvo desaba inconsciente."
            InjuryRules.EfeitoFerimento.ATORDOADO_CAIDO -> " $alvo cambaleia e cai, atordoado."
            else -> if (dano.incapacitouMembro) " O membro fica inutilizado!" else ""
        }
        val tec = "$tecAtk · ${dano.penetrante} pen ×${dano.multiplicador} = ${dano.pvSubtrair}"
        return "$cabeca acerta $alvo$onde$comArma — ${dano.pvSubtrair} de dano (${tipo.rotulo})!$efeito [$tec]"
    }

    /** Preposição contraída para o local do golpe ("no rosto", "na perna", "nos vitais"). */
    private fun preposicaoLocal(local: LocalAtaque): String = when (local) {
        LocalAtaque.PERNA, LocalAtaque.MAO, LocalAtaque.INGLE -> "na"
        LocalAtaque.VITAIS -> "nos"
        else -> "no"
    }

    /** Esquiva de um NPC = Velocidade Básica + 3 (MB p.374). */
    private fun esquivaNpc(npc: Combatente): Int = floor(npc.velocidadeBasica).toInt() + 3

    /** Melhor defesa de um NPC: Esquiva (Vel.Básica+3) vs Aparar (NH/2+3, só corpo-a-corpo). */
    private fun melhorDefesaNpc(npc: Combatente): Pair<CombatResolver.TipoDefesa, Int> {
        val esquiva = esquivaNpc(npc)
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

        /**
         * Mapeia o tipo de dano vindo do bestiário OU da ficha (Lote 368).
         * Aceita a string inteira do dano ("2d-1 pa+", "GeB+2 corte") e extrai o token de tipo.
         * Vocabulário Devir PT-BR das tabelas: corte, cont, perf, e pa-/pa/pa+/pa++ (= perfurante "pi").
         */
        fun tipoDano(tipo: String): DanoTipo {
            val t = tipo.lowercase().trim()
            // procura o token de tipo no fim da expressão (ex.: "2d-1 pa+")
            val token = Regex("(pa\\+\\+|pa\\+|pa-|pa|pi\\+\\+|pi\\+|pi-|pi|corte|cort|perf|imp|cont|esm)\\s*$")
                .find(t)?.groupValues?.getOrNull(1) ?: t
            return when (token) {
                "corte", "cort" -> DanoTipo.CORT
                "pa-", "pi-" -> DanoTipo.PI_MENOS
                "pa", "pi" -> DanoTipo.PI
                "pa+", "pi+" -> DanoTipo.PI_MAIS
                "pa++", "pi++" -> DanoTipo.PI_MAIS_MAIS
                "perf", "imp" -> DanoTipo.PERF
                else -> DanoTipo.CONT // cont/esm/queimadura/tóxico/etc. → multiplicador ×1.0
            }
        }

        /**
         * Penalidade de PARA ACERTAR pela distância em ataque à distância (Tabela
         * Tamanho/Velocidade-Distância, MB p.550). Resumo determinístico em metros.
         */
        fun penalidadeDistancia(metros: Int): Int = when {
            metros <= 2 -> 0
            metros <= 3 -> -1
            metros <= 5 -> -2
            metros <= 7 -> -3
            metros <= 10 -> -4
            metros <= 15 -> -5
            metros <= 20 -> -6
            metros <= 30 -> -7
            metros <= 50 -> -8
            metros <= 70 -> -9
            metros <= 100 -> -10
            metros <= 150 -> -11
            else -> -12 - ((metros - 200) / 100).coerceAtLeast(0)
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

/** Defesas do herói — o controller extrai da ficha; mantém a sessão pura. (Lote 368: só defesa.) */
data class HeroiPerfilCombate(
    val esquiva: Int,
    val apara: Int? = null,
    val bloqueio: Int? = null,
    val ht: Int = 10,
    val rd: Int = 0
)

/**
 * Um ataque utilizável do herói (Lote 368): arma empunhada + perícia. O jogador ESCOLHE qual usar.
 * @param aDistancia true para arma de fogo/arremesso (defesa do alvo só por Esquiva; sofre penal. de distância).
 * @param precisao Acc da arma (bônus ao Apontar — usado no lote de manobras).
 */
data class AtaqueHeroi(
    val rotulo: String,          // ex.: "Revólver (Pistola)"
    val nh: Int,
    val danoExpr: String,        // expressão já resolvida por ST, ex.: "2d-1 pa+"
    val tipo: DanoTipo,
    val aDistancia: Boolean = false,
    val alcance: Int = 1,
    val precisao: Int = 0,
    val temPericia: Boolean = true
)

/** Defesa escolhida pelo jogador no card "Defenda-se!" (tipo + valor final + 3d6 rolado). */
data class DefesaHeroi(
    val tipo: CombatResolver.TipoDefesa,
    val valorFinal: Int,
    val soma: Int
)

enum class ResultadoCombate { VITORIA, DERROTA, FUGA }
