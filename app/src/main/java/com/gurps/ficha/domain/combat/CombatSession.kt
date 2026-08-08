package com.gurps.ficha.domain.combat

import com.gurps.ficha.domain.rules.DanoTipo
import com.gurps.ficha.domain.rules.ToleranciaFerimentos

import com.gurps.ficha.domain.rules.LocalAtaque

import com.gurps.ficha.domain.magic.ClasseParseada
import com.gurps.ficha.domain.magic.ContextoConjuracao
import com.gurps.ficha.domain.magic.CustoEnergia
import com.gurps.ficha.domain.magic.EfeitoChoqueRetorno
import com.gurps.ficha.domain.magic.MagiaAtivaNoCombate
import com.gurps.ficha.domain.magic.MagicCasting
import com.gurps.ficha.domain.magic.TipoDuracao
import com.gurps.ficha.domain.magic.ResistenciaMagia
import com.gurps.ficha.domain.magic.ResultadoOperacao
import com.gurps.ficha.domain.magic.AtributoResistencia
import com.gurps.ficha.domain.magic.TipoClasseMagia
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
    /** Perfil da ficha, SEM buffs mágicos. Leia [heroiPerfil] — é ele que enxerga os buffs ativos. */
    val heroiPerfilBase: HeroiPerfilCombate,
    private val random: Random = Random.Default
) {
    /**
     * Lote MEC-2: perfil EFETIVO do herói = ficha + buffs mágicos ativos (Força +ST, Escudo +RD,
     * Apressar +Esquiva…). É propriedade computada de propósito: todo o motor já lia `heroiPerfil`,
     * então os buffs passam a valer em cada regra sem tocar em nenhum ponto de uso.
     */
    val heroiPerfil: HeroiPerfilCombate
        get() {
            val b = heroiPerfilBase
            val h = encounter.combatentes.firstOrNull { it.ehHeroi } ?: return b
            if (h.buffs.isEmpty()) return b
            // MEC-4: o BD mágico (Escudo) soma em TODAS as defesas ativas, como o BD do escudo real
            // (MB p.374) — por isso entra em esquiva, aparar E bloquear. Aparar/bloquear ficam null
            // quando o herói não tem a defesa: o BD não INVENTA uma defesa que ele não possui.
            val bd = h.buffBd
            return b.copy(
                rd = b.rd + h.buffRd,
                esquiva = b.esquiva + h.buffEsquiva + bd,
                apara = b.apara?.plus(bd),
                bloqueio = b.bloqueio?.plus(bd),
                st = b.st + h.buffSt,
                dx = b.dx + h.buffDx,
                ht = b.ht + h.buffHt,
                // Lote P3-1: Fortalecer/Enfraquecer Vontade entram AQUI e, por isso, valem de uma
                // vez em todo teste de Vontade do motor — concentração ao ser ferido (MEC-26),
                // segurar o projétil (C1), pontaria perdida pela dor. Zero pontos de uso tocados:
                // é o mesmo truque do MEC-2, que é o motivo de `heroiPerfil` ser computado.
                vontade = b.vontade + h.buffVontade,
            )
        }

    /** Registro factual, linha a linha — o Narrador transforma em prosa SEM inventar números. */
    // Lote LOG-1: espelha cada linha da narrativa no logcat (filtre por tag:Saga_Combate).
    val log: MutableList<String> = LogDeCombate()

    /**
     * Lote TESTE-NPC: só o combate de TESTE do preview mexe nisto. Em campanha fica [ModoTesteNpc.NORMAL]
     * — nenhum caminho de jogo real seta outro valor.
     */
    var modoTesteNpc: ModoTesteNpc = ModoTesteNpc.NORMAL

    init {
        // Marco de início: facilita achar onde a luta começa num logcat cheio.
        SagaLog.mecanica("═══ COMBATE INICIADO ═══ " +
            encounter.combatentes.joinToString(", ") { "${it.nome}(PV ${it.pvAtual}/${it.pvMax})" })
    }

    var encerrado: Boolean = false; private set
    var resultado: ResultadoCombate? = null; private set

    // Avaliar (Lote 370): bônus cumulativo (até +3) no PRÓXIMO ataque corpo-a-corpo ao alvo avaliado.
    private var avaliarAlvoId: String? = null
    private var avaliarStacks: Int = 0
    private fun limparAvaliar() { avaliarAlvoId = null; avaliarStacks = 0 }

    // Apontar (Lote 373/392/395): mira → +Precisão (Acc) + mira de vários turnos (+1/+2) + firmar arma de fogo (+1).
    private var apontarAlvoId: String? = null
    private var apontarStacks: Int = 0 // turnos consecutivos mirando o MESMO alvo
    private var apontarFirmado: Boolean = false // Lote 395: firmou a arma de fogo (+1 Acc, MB p.364)
    private fun limparApontar() { apontarAlvoId = null; apontarStacks = 0; apontarFirmado = false }

    // Fintar (Lote 383): venceu a Disputa Rápida → reduz a defesa do alvo no PRÓXIMO golpe ao mesmo alvo (MB p.366).
    private var fintaAlvoId: String? = null
    private var fintaPenalidade: Int = 0
    private fun limparFinta() { fintaAlvoId = null; fintaPenalidade = 0 }

    // Aparar desbalanceada (Lote 375): atacou com arma "D" → não pode aparar com ela até o próximo turno (MB p.270).
    private var atacouDesbalanceada = false

    // Ataque Total (Lote 377): depois de um Ataque Total o herói fica SEM defesa ativa até o próximo turno (MB p.366).
    var heroiSemDefesaAtiva: Boolean = false; private set

    // Mover e Atacar (Lote 378): na defesa seguinte só Esquiva/Bloqueio — sem aparar — até o próximo turno (MB p.367).
    var heroiSemAparar: Boolean = false; private set

    // Ataque Dedicado / Defensivo (Lote PONTE-4, AM p98): trade-off de defesa declarado no ataque, vale até a
    // defesa do próximo turno do herói. Dedicado → −2 em todas as defesas + sem Retirada; Defensivo → +1 numa defesa.
    var heroiPenalidadeDefesaDedicado: Int = 0; private set
    var heroiSemRetirada: Boolean = false; private set
    var heroiBonusDefesaDefensivo: CombatResolver.TipoDefesa? = null; private set

    // ── Modificadores SITUACIONAIS (Lote 424/T1-2): ações improvisadas do jogador narradas pelo Narrador ──
    // (cobertura, distração, terreno, areia nos olhos…) viram bônus/penalidade NOMEADO em ataque ou defesa
    // de um combatente, aplicados pela tool aplicar_modificador_combate. Expiram por rodadas ou duram a luta.
    data class ModSituacional(
        val alvoId: String,
        val categoria: String,     // "ataque" | "defesa"
        val valor: Int,
        val motivo: String,
        var rodadasRestantes: Int, // Int.MAX_VALUE = até o fim do combate
        var estreou: Boolean = false // o turno em que o mod nasce NÃO conta (o chat roda no turno do dono)
    )
    private val modsSituacionais = mutableListOf<ModSituacional>()

    /** Aplica um modificador situacional (tool do Narrador). Retorna o relatório factual ou null se o alvo é inválido. */
    fun aplicarModSituacional(alvoId: String, categoria: String, valor: Int, motivo: String, duracaoRodadas: Int?): String? {
        val alvo = encounter.combatentes.firstOrNull { it.id == alvoId && it.vivo } ?: return null
        val cat = if (categoria.lowercase().trim().startsWith("def")) "defesa" else "ataque"
        val dur = duracaoRodadas?.coerceIn(1, 100) ?: Int.MAX_VALUE
        val v = valor.coerceIn(-10, 10)
        modsSituacionais.add(ModSituacional(alvo.id, cat, v, motivo.ifBlank { "situação" }, dur))
        val sinal = if (v >= 0) "+$v" else "$v"
        val durTxt = if (dur == Int.MAX_VALUE) "até o fim do combate" else "por $dur rodada(s)"
        val txt = "🎯 ${alvo.nome}: $sinal em $cat (${motivo.ifBlank { "situação" }}), $durTxt."
        log += txt
        return txt
    }

    private fun modsSituacionaisAtaque(id: String): List<ModSituacional> =
        modsSituacionais.filter { it.alvoId == id && it.categoria == "ataque" }

    private fun modSituacionalDefesa(id: String): Int =
        modsSituacionais.filter { it.alvoId == id && it.categoria == "defesa" }.sumOf { it.valor }

    // Defesa Total (Lote 388, MB p.366): declarada no turno do herói, vale até a PRÓXIMA ação dele.
    // AUMENTADA = +2 numa defesa escolhida; DUPLA = 2ª defesa diferente se a 1ª falhar.
    private var defesaTotalModo: DefesaTotalModo? = null
    private var defesaTotalAumentadaEm: CombatResolver.TipoDefesa? = null
    /** True quando o herói está em Defesa Total (Dupla) — o controller prepara a 2ª defesa. */
    val heroiDefesaTotalDupla: Boolean get() = defesaTotalModo == DefesaTotalModo.DUPLA
    private fun limparDefesaTotal() { defesaTotalModo = null; defesaTotalAumentadaEm = null }

    // Disparada (Lote 394, MB p.353): Moves consecutivos NA MESMA DIREÇÃO (linha reta) acumulam +20% a partir do 2º.
    private var heroiMoveSeguidos = 0
    private var heroiMoveDirecao: Boolean? = null // afastar do último Move (mudar de direção quebra a disparada)

    // Fogo de Retenção (Lote 396, MB p.409): arma CdT 5+ cobre a área até o próximo turno; quem AVANÇA leva uma rajada.
    private var fogoRetencaoArma: AtaqueHeroi? = null

    // Concentrar (Lote 397, MB p.344): atividade mental de turno completo; ser forçado a defender/ferido exige Vontade-3.
    private var concentrando = false

    // Aguardar / Interromper Investida (Lote 399, MB p.392): arma perfurante firmada golpeia primeiro quem investe,
    // com +1 de dano por 2m percorridos. Vale até o próximo turno (o herói ainda pode se defender enquanto aguarda).
    private var aguardarInvestidaArma: AtaqueHeroi? = null
    private var bonusInvestidaPendente = 0 // +1/2m, somado ao dano básico no golpe da investida

    // Armas Preparadas / Preparar (Lote 398, MB p.270/366): arma desbalanceada fica DESPREPARADA após atacar
    // (a menos que ST ≥ 1,5× a ST mínima); precisa de uma manobra Preparar p/ atacar de novo. Persiste entre turnos.
    private var armaDespreparadaRotulo: String? = null
    /** Marca a arma como despreparada após um golpe desbalanceado, se o herói não for forte o bastante (MB p.270). */
    private fun marcarDespreparoSeNecessario(ataque: AtaqueHeroi) {
        if (ataque.aDistancia || ataque.apararTipo != ApararTipo.DESBALANCEADA || ataque.stMinimo <= 0) return
        val limiar = kotlin.math.ceil(1.5 * ataque.stMinimo).toInt() // ST ≥ 1,5× a mínima dispensa o re-preparo
        if (heroiPerfil.st < limiar) {
            armaDespreparadaRotulo = ataque.rotulo
            log += "  └ a arma desbalanceada ficou DESPREPARADA após o golpe — use Preparar para empunhá-la de novo (MB p.270)."
        }
    }
    /** Preparar/sacar re-empunha a arma despreparada (Lote 398). */
    fun prepararArmaEmpunhada() { armaDespreparadaRotulo = null }
    /** True se a arma [rotulo] está despreparada (precisa de Preparar antes de atacar). Lote 398. */
    fun armaDespreparada(rotulo: String): Boolean = armaDespreparadaRotulo == rotulo
    /** Lote 406 (MB p.383): cair/atordoar-se empunhando uma arma desbalanceada a deixa despreparada. */
    fun marcarArmaDespreparada(rotulo: String) { armaDespreparadaRotulo = rotulo }

    /** Início de uma ação do herói: zera as flags do turno anterior (desbalanceada + sem-defesa/sem-aparar + Defesa Total + Disparada). */
    private fun inicioAcaoHeroi() {
        atacouDesbalanceada = false; heroiSemDefesaAtiva = false; heroiSemAparar = false; limparDefesaTotal()
        heroiMoveSeguidos = 0 // qualquer ação que NÃO seja Mover quebra a Disparada (heroiMove restaura +1)
        fogoRetencaoArma = null // a cobertura do Fogo de Retenção dura só até a próxima ação do herói (Lote 396)
        concentrando = false // a concentração vale só no turno declarado; o herói re-declara Concentrar p/ continuar (Lote 397)
        aguardarInvestidaArma = null // a guarda da investida dura só até a próxima ação do herói (Lote 399)
        heroi.velocidadeAtual = 0 // só Mover define a velocidade do herói para a penalidade de Vel/Dist (Lote 403)
        // Ataque Dedicado/Defensivo (Lote PONTE-4): o trade-off de defesa do turno anterior expira ao agir de novo.
        heroiPenalidadeDefesaDedicado = 0; heroiSemRetirada = false; heroiBonusDefesaDefensivo = null
    }

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

    /**
     * Lote MEC-41: distância entre DOIS combatentes.
     *
     * ⚠️ **Aproximação honesta**: o encounter só guarda distância ao HERÓI (modelo de faixas), então
     * isto é `|dist(a) − dist(b)|` — exato quando os três estão em linha, subestimando quando não
     * estão. Serve para as bandas do Lampejo; a grade tática tem a posição real, mas ela vive no
     * controller, não aqui.
     */
    fun distanciaEntre(a: Combatente, b: Combatente): Int =
        kotlin.math.abs(distancia(a) - distancia(b))

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
    /**
     * Lote TOK-5a (VTT 2D): ponte POSICIONAL opcional com a grade tática. Null = modo faixas
     * (nenhuma regra de facing/linha entra — zero regressão). O SagaCombatController implementa
     * com o HexCombatState; o motor consulta nos pontos de ataque/defesa/retirada.
     */
    interface PosicaoBridge {
        /** Facing do ataque [atacanteId]→[alvoId] (FRENTE/FLANCO/COSTAS) ou null se fora da grade. */
        fun facingDoAtaque(atacanteId: String, alvoId: String): com.gurps.ficha.domain.combat.hex.Facing?
        /** Penalidade por atacar através de hex ocupado por INIMIGO (0 ou −4 — MB p.389). */
        fun penalidadeAtravesDeHex(atacanteId: String, alvoId: String, alcanceArmaMetros: Int): Int
        /** Vira o atacante de frente pro alvo na grade (mudar facing no próprio turno é livre). */
        fun aoAtacar(atacanteId: String, alvoId: String)
        /** Retirada (MB p.377): recua o defensor 1 hex na direção oposta ao atacante. Devolve as
         *  novas distâncias ao herói (ou null se o recuo era impossível/defensor não é o herói). */
        fun recuarUmHex(defensorId: String, atacanteId: String): Map<String, Int>?

        /**
         * Lote TOK-5b: IA POSICIONAL do NPC — move o NPC na grade (flanquear/kite/recuar via
         * HexTaticaNpc, HEX-5), passo a passo até o deslocamento, e devolve a NOVA distância ao
         * herói. Null = a grade não decidiu (cai no movimento abstrato do modo faixas).
         */
        fun moverNpcNaGrade(npcId: String, intencao: NpcCombatBrain.IntencaoNpc): Int?
    }
    var posicaoBridge: PosicaoBridge? = null

    fun heroiAtaca(
        ataque: AtaqueHeroi,
        alvoId: String,
        manobra: Manobra = Manobra.ATAQUE,
        local: LocalAtaque = LocalAtaque.TORSO,
        ataqueTotalModo: AtaqueTotalModo = AtaqueTotalModo.DETERMINADO,
        enganoso: Int = 0, // Lote 401: passos de Ataque Enganoso (MB p.369)
        telegrafico: Boolean = false, // Lote PONTE-3: Ataque Telegráfico (AM p.109)
        dedicadoModo: DedicadoModo = DedicadoModo.DETERMINADO, // Lote PONTE-4: modo do Ataque Dedicado (AM p98)
        benefDefensivo: CombatResolver.TipoDefesa? = null // Lote PONTE-4: defesa que ganha +1 no Ataque Defensivo (AM p98)
    ): AtaqueResultado {
        // Arma despreparada (Lote 398, MB p.270): não dá pra atacar até re-empunhá-la com um Preparar.
        if (ataque.rotulo == armaDespreparadaRotulo) {
            val t = "⚠️ ${ataque.rotulo.substringBefore(" (").trim()} está despreparada — use Preparar antes de atacar."
            log += t; return AtaqueResultado(false, false, 0, false, t)
        }
        inicioAcaoHeroi()
        val alvo = inimigos.firstOrNull { it.id == alvoId && it.vivo }
            ?: return AtaqueResultado(false, false, 0, false, "Alvo inválido ou já fora de combate.").also { log += it.texto }
        golpeForaDeAlcance(ataque, alvo)?.let { return it }
        golpeContraInsubstancial(alvo)?.let { return it }
        val r = resolverGolpeHeroi(ataque, alvo, manobra, local, ataqueTotalModo, enganoso = enganoso, telegrafico = telegrafico, dedicadoModo = dedicadoModo)
        limparAvaliar(); limparApontar(); limparFinta() // bônus de Avaliar/Mira consumidos neste ataque
        // Arma desbalanceada: quem atacou com ela não pode aparar até o próximo turno (MB p.270).
        if (!ataque.aDistancia && ataque.apararTipo == ApararTipo.DESBALANCEADA) atacouDesbalanceada = true
        marcarDespreparoSeNecessario(ataque) // Lote 398: desbalanceada fica despreparada se o herói não for forte
        // Ataque Total: sem defesa ativa até o próximo turno (MB p.366).
        if (manobra == Manobra.ATAQUE_TOTAL) heroiSemDefesaAtiva = true
        // Ataque Dedicado/Defensivo (Lote PONTE-4, AM p98): declara o trade-off de defesa para a defesa do próximo turno.
        if (manobra == Manobra.ATAQUE_DEDICADO) { heroiPenalidadeDefesaDedicado = 2; heroiSemRetirada = true }
        if (manobra == Manobra.ATAQUE_DEFENSIVO) heroiBonusDefesaDefensivo = benefDefensivo
        verificarFim()
        return r
    }

    /** Golpe Rápido (MB p.370): dois ataques corpo-a-corpo no mesmo turno, cada um a −6 — MANTÉM a defesa ativa. */
    fun heroiGolpeRapido(ataque: AtaqueHeroi, alvoId: String, local: LocalAtaque = LocalAtaque.TORSO): List<AtaqueResultado> {
        if (ataque.rotulo == armaDespreparadaRotulo) {
            val t = "⚠️ ${ataque.rotulo.substringBefore(" (").trim()} está despreparada — use Preparar antes de atacar."
            log += t; return listOf(AtaqueResultado(false, false, 0, false, t))
        }
        inicioAcaoHeroi()
        if (ataque.aDistancia) {
            val t = "⚠️ Golpe Rápido é só corpo-a-corpo."
            log += t; return listOf(AtaqueResultado(false, false, 0, false, t))
        }
        if (inimigos.none { it.id == alvoId && it.vivo })
            return listOf(AtaqueResultado(false, false, 0, false, "Alvo inválido.").also { log += it.texto })
        val nome = inimigos.first { it.id == alvoId }.nome
        log += "⚔️⚔️ Golpe Rápido: dois ataques contra $nome, cada um a −6 (MB p.370)."
        val resultados = mutableListOf<AtaqueResultado>()
        repeat(2) {
            val alvo = inimigos.firstOrNull { it.id == alvoId && it.vivo }
            if (alvo != null) resultados += (golpeForaDeAlcance(ataque, alvo)
                ?: resolverGolpeHeroi(ataque, alvo, Manobra.ATAQUE, local, AtaqueTotalModo.DETERMINADO,
                    modAdicional = -6, rotuloModAdicional = "golpe rápido"))
        }
        limparAvaliar(); limparApontar(); limparFinta()
        if (ataque.apararTipo == ApararTipo.DESBALANCEADA) atacouDesbalanceada = true
        marcarDespreparoSeNecessario(ataque)
        verificarFim()
        return resultados
    }

    /** Encontrão (MB p.371): colisão corporal. Dano mútuo por contusão = (PV×vel.relativa)/100 dados; quem leva mais cai. */
    fun heroiEncontrao(alvoId: String): AtaqueResultado {
        inicioAcaoHeroi()
        val alvo = inimigos.firstOrNull { it.id == alvoId && it.vivo }
            ?: return AtaqueResultado(false, false, 0, false, "Alvo inválido.").also { log += it.texto }
        val dist = encounter.distancia(alvo)
        val carga = dist.coerceIn(1, heroi.deslocamentoEfetivo.coerceAtLeast(1))
        if (dist > 1) encounter.definirDistancia(alvo.id, 1) // chega ao corpo-a-corpo carregando
        heroi.velocidadeAtual = carga
        val relVel = (carga + alvo.velocidadeAtual).coerceAtLeast(1) // colisão frontal soma a aproximação do alvo
        // Acerto por DX (sem o −4 / teto 9 do Avançar e Atacar; MB p.371).
        val somaAtk = rolar3d6()
        log += "💥 Encontrão! Você se lança contra ${alvo.nome} (DX ${heroiPerfil.dx}, rolou $somaAtk; vel. relativa ${relVel}m/s)."
        if (somaAtk > heroiPerfil.dx) { log += "  └ você erra o encontrão."; verificarFim(); return AtaqueResultado(false, false, 0, false, log.last()) }
        val (defTipo, defValor) = melhorDefesaNpc(alvo)
        if (npcSeDefendeu(defValor, rolar3d6())) {
            log += "  └ ${alvo.nome} se defende (${defTipo.rotulo} $defValor) e desvia do encontrão."
            verificarFim(); return AtaqueResultado(true, true, 0, false, log.last())
        }
        val danoHeroi = rolarDano(encontraoDanoDados(heroi.pvMax, relVel), random)
        val danoNpc = rolarDano(encontraoDanoDados(alvo.pvMax, relVel), random)
        val dnNpc = HitLocationRules.aplicarDano(alvo.pvMax, danoHeroi, DanoTipo.CONT, LocalAtaque.TORSO,
            alvo.stats?.rd ?: 0, alvo.stats?.tolerancia ?: ToleranciaFerimentos.NORMAL)
        InjuryRules.ferir(alvo, dnNpc.pvSubtrair, alvo.htEfetivo, random)
        val dnHeroi = HitLocationRules.aplicarDano(heroi.pvMax, danoNpc, DanoTipo.CONT, LocalAtaque.TORSO, heroiPerfil.rd)
        InjuryRules.ferir(heroi, dnHeroi.pvSubtrair, heroiPerfil.ht, random)
        log += "  └ você causa ${dnNpc.pvSubtrair} a ${alvo.nome} e sofre ${dnHeroi.pvSubtrair} (impacto recíproco)."
        // Derrubada (MB p.371): o alvo cai se levar o dobro; o herói cai se levar o dobro, ou testa DX se causou ≥.
        if (danoHeroi >= 2 * maxOf(1, danoNpc)) {
            alvo.postura = Postura.DEITADO; alvo.condicoes.add(Condicao.CAIDO); log += "  └ ${alvo.nome} é jogado no chão pelo impacto!"
        }
        if (danoNpc >= 2 * maxOf(1, danoHeroi)) {
            heroi.postura = Postura.DEITADO; heroi.condicoes.add(Condicao.CAIDO); log += "  └ o impacto te derruba!"
        } else if (danoHeroi >= danoNpc && rolar3d6() > heroiPerfil.dx) {
            heroi.postura = Postura.DEITADO; heroi.condicoes.add(Condicao.CAIDO); log += "  └ você se desequilibra no impacto e cai (falhou no teste de DX)."
        }
        verificarFim()
        return AtaqueResultado(true, false, dnNpc.pvSubtrair, !alvo.vivo, log.last())
    }

    /** Empurrão (MB p.371): empurra o alvo com as mãos. Acerto por DX; GdP×2 vira projeção (knockback), nunca lesão. */
    fun heroiEmpurrao(alvoId: String): AtaqueResultado {
        inicioAcaoHeroi()
        val alvo = inimigos.firstOrNull { it.id == alvoId && it.vivo }
            ?: return AtaqueResultado(false, false, 0, false, "Alvo inválido.").also { log += it.texto }
        if (encounter.distancia(alvo) > 1) {
            val t = "⚠️ Empurrão exige estar adjacente ao alvo."; log += t; return AtaqueResultado(false, false, 0, false, t)
        }
        val somaAtk = rolar3d6()
        log += "🙌 Empurrão! Você tenta empurrar ${alvo.nome} (DX ${heroiPerfil.dx}, rolou $somaAtk)."
        if (somaAtk > heroiPerfil.dx) { log += "  └ você erra o empurrão."; return AtaqueResultado(false, false, 0, false, log.last()) }
        val (defTipo, defValor) = melhorDefesaNpc(alvo)
        if (npcSeDefendeu(defValor, rolar3d6())) {
            log += "  └ ${alvo.nome} se defende (${defTipo.rotulo} $defValor) do empurrão."
            return AtaqueResultado(true, true, 0, false, log.last())
        }
        val forca = rolarDano(heroiPerfil.danoGdP, random) * 2 // GdP × 2 (duas mãos), MB p.371
        val stAlvo = (alvo.stEfetivo).coerceAtLeast(3)
        val knockback = forca / (stAlvo - 2) // projeção: 1m por múltiplo de (ST−2) no dano (MB p.378); sem lesão
        if (knockback > 0) {
            encounter.moverEmRelacaoAoHeroi(alvo.id, knockback)
            log += "  └ ${alvo.nome} é projetado ${knockback}m para trás (força $forca vs ST $stAlvo) — sem lesão (MB p.371/378)."
            if (knockback >= 2 && rolar3d6() > (alvo.dxEfetivoOuProprio) - (knockback - 1)) {
                alvo.postura = Postura.DEITADO; alvo.condicoes.add(Condicao.CAIDO); log += "  └ ${alvo.nome} cai com o tranco!"
            }
        } else log += "  └ ${alvo.nome} mal se move (força $forca insuficiente vs ST $stAlvo)."
        verificarFim()
        return AtaqueResultado(true, false, 0, false, log.last())
    }

    /** Imobilizar (MB p.371): prende no chão um oponente AGARRADO. Disputa Normal de ST (+3 por categoria de MT). */
    fun heroiImobilizar(alvoId: String): String {
        inicioAcaoHeroi()
        val alvo = inimigos.firstOrNull { it.id == alvoId && it.vivo } ?: return "Alvo inválido.".also { log += it }
        if (Condicao.AGARRADO !in alvo.condicoes)
            return "⚠️ Você precisa estar agarrando ${alvo.nome} (pelo tronco) para imobilizá-lo.".also { log += it }
        if (alvo.postura != Postura.DEITADO && Condicao.CAIDO !in alvo.condicoes)
            return "⚠️ Só dá pra imobilizar um oponente no chão — derrube-o antes.".also { log += it }
        val mtBonus = (heroiPerfil.modificadorTamanho - (alvo.stats?.modificadorTamanho ?: 0)).coerceAtLeast(0) * 3
        val stHeroi = heroiPerfil.st + mtBonus
        val stNpc = alvo.stEfetivo
        val rh = rolar3d6(); val rn = rolar3d6()
        val txt = if (vencaDisputaRapida(stHeroi, rh, stNpc, rn)) {
            alvo.condicoes.add(Condicao.IMOBILIZADO)
            "🔒 Você IMOBILIZA ${alvo.nome} no chão — indefeso! [ST $stHeroi rolou $rh vs ST $stNpc rolou $rn]"
        } else "🤼 ${alvo.nome} resiste e não é imobilizado. [ST $stHeroi rolou $rh vs ST $stNpc rolou $rn]"
        log += txt
        return txt
    }

    /** Estrangular/Asfixiar (MB p.371): agarrado pelo pescoço. Disputa de ST vs max(ST,HT) → dano (margem ×1,5) + sufoco. */
    fun heroiEstrangular(alvoId: String): String {
        inicioAcaoHeroi()
        val alvo = inimigos.firstOrNull { it.id == alvoId && it.vivo } ?: return "Alvo inválido.".also { log += it }
        if (Condicao.AGARRADO !in alvo.condicoes)
            return "⚠️ Você precisa estar agarrando ${alvo.nome} pelo pescoço para estrangulá-lo.".also { log += it }
        val stHeroi = heroiPerfil.st
        val resist = maxOf(alvo.stEfetivo, alvo.htEfetivo)
        val rh = rolar3d6(); val rn = rolar3d6()
        // Disputa Rápida: margem de vitória do estrangulador = dano por contusão (×1,5 no pescoço); RD protege.
        val margem = (stHeroi - rh) - (resist - rn)
        if (rh > stHeroi || margem <= 0) {
            log += "🫷 ${alvo.nome} resiste ao estrangulamento. [ST $stHeroi rolou $rh vs $resist rolou $rn]"
            return log.last()
        }
        val danoBruto = (margem * 1.5).toInt().coerceAtLeast(1)
        val dn = HitLocationRules.aplicarDano(alvo.pvMax, danoBruto, DanoTipo.CONT, LocalAtaque.TORSO,
            alvo.stats?.rd ?: 0, alvo.stats?.tolerancia ?: ToleranciaFerimentos.NORMAL)
        InjuryRules.ferir(alvo, dn.pvSubtrair, alvo.htEfetivo, random)
        log += "🫳 Você estrangula ${alvo.nome}: margem $margem → ${dn.pvSubtrair} de dano no pescoço. [ST $stHeroi rolou $rh vs $resist rolou $rn]"
        if (dn.pvSubtrair > 0 && alvo.vivo) {
            alvo.condicoes.add(Condicao.SUFOCANDO)
            log += "  └ ${alvo.nome} começa a SUFOCAR — perde fôlego a cada turno até escapar (MB p.437)."
        }
        verificarFim()
        return log.last()
    }

    /**
     * Chave de Membro (Lote PONTE-1, AM p.69-70/81): com o alvo JÁ AGARRADO, torce um membro. Disputa Rápida
     * de ST (a vítima resiste com o MAIOR entre ST e HT; +4 se for a perna) → dano por contusão = margem (caminho
     * de precisão de AM p.69, mesmo padrão do estrangular). Limitações honestas: sem o NH de perícia de luta no
     * perfil (Disputa por ST pura, força-bruta AM p.81); RD flexível não é distinguida da rígida (motor aplica RD
     * cheia); só braço/perna (mão/dedo/cabeça/pescoço deferidos — o Mata-Leão/Estrangular cobrem o pescoço).
     */
    fun heroiChaveMembro(alvoId: String, perna: Boolean = false): String {
        inicioAcaoHeroi()
        val alvo = inimigos.firstOrNull { it.id == alvoId && it.vivo } ?: return "Alvo inválido.".also { log += it }
        if (Condicao.AGARRADO !in alvo.condicoes)
            return "⚠️ Você precisa estar agarrando ${alvo.nome} para aplicar uma chave.".also { log += it }
        val stHeroi = heroiPerfil.st
        val resist = maxOf(alvo.stEfetivo, alvo.htEfetivo) + (if (perna) 4 else 0)
        val rh = rolar3d6(); val rn = rolar3d6()
        val membro = if (perna) LocalAtaque.PERNA else LocalAtaque.BRACO
        val margem = (stHeroi - rh) - (resist - rn)
        if (rh > stHeroi || margem <= 0) {
            log += "🦾 ${alvo.nome} resiste à chave de ${membro.rotulo}. [ST $stHeroi rolou $rh vs $resist rolou $rn]"
            return log.last()
        }
        val dn = HitLocationRules.aplicarDano(alvo.pvMax, margem, DanoTipo.CONT, membro,
            alvo.stats?.rd ?: 0, alvo.stats?.tolerancia ?: ToleranciaFerimentos.NORMAL)
        InjuryRules.ferir(alvo, dn.pvSubtrair, alvo.htEfetivo, random)
        log += "🦾 Você aplica uma chave no ${membro.rotulo} de ${alvo.nome}: margem $margem → ${dn.pvSubtrair} de dano. [ST $stHeroi rolou $rh vs $resist rolou $rn]"
        verificarFim()
        return log.last()
    }

    /**
     * Mata-Leão (Lote PONTE-1, AM p.77): estrangulamento com as DUAS mãos num alvo agarrado — uma Asfixia (MB
     * p.371) com +3 na ST de controle. Aqui só o ramo "aéreo" (contusão no pescoço + sufoco); o "sanguíneo"
     * (apagar por fadiga/PF) fica DEFERIDO (o motor não modela dano de fadiga em PF do NPC).
     */
    fun heroiMataLeao(alvoId: String): String {
        inicioAcaoHeroi()
        val alvo = inimigos.firstOrNull { it.id == alvoId && it.vivo } ?: return "Alvo inválido.".also { log += it }
        if (Condicao.AGARRADO !in alvo.condicoes)
            return "⚠️ Você precisa estar agarrando ${alvo.nome} pelo pescoço para o mata-leão.".also { log += it }
        val stHeroi = heroiPerfil.st + 3 // +3 pelo controle com as duas mãos (AM p.77)
        val resist = maxOf(alvo.stEfetivo, alvo.htEfetivo)
        val rh = rolar3d6(); val rn = rolar3d6()
        val margem = (stHeroi - rh) - (resist - rn)
        if (rh > stHeroi || margem <= 0) {
            log += "🫷 ${alvo.nome} resiste ao mata-leão. [ST $stHeroi rolou $rh vs $resist rolou $rn]"
            return log.last()
        }
        val danoBruto = (margem * 1.5).toInt().coerceAtLeast(1)
        val dn = HitLocationRules.aplicarDano(alvo.pvMax, danoBruto, DanoTipo.CONT, LocalAtaque.TORSO,
            alvo.stats?.rd ?: 0, alvo.stats?.tolerancia ?: ToleranciaFerimentos.NORMAL)
        InjuryRules.ferir(alvo, dn.pvSubtrair, alvo.htEfetivo, random)
        log += "🫳 Você aplica um mata-leão em ${alvo.nome}: margem $margem → ${dn.pvSubtrair} de dano no pescoço. [ST $stHeroi rolou $rh vs $resist rolou $rn]"
        if (dn.pvSubtrair > 0 && alvo.vivo) {
            alvo.condicoes.add(Condicao.SUFOCANDO)
            log += "  └ ${alvo.nome} começa a SUFOCAR — perde fôlego a cada turno até escapar (MB p.437)."
        }
        verificarFim()
        return log.last()
    }

    /**
     * Mover e Atacar (MB p.366): o herói se desloca e ataca em movimento. Corpo-a-corpo aproxima-se do
     * alvo (gastando até o Deslocamento) antes de golpear; a penalidade é tratada pelo motor (CaC −4 e
     * teto de NH 9; à distância −2 ou a Magnitude, o pior). Na defesa seguinte só Esquiva/Bloqueio (MB p.367).
     */
    fun heroiMoverEAtacar(
        ataque: AtaqueHeroi,
        alvoId: String,
        local: LocalAtaque = LocalAtaque.TORSO
    ): AtaqueResultado {
        inicioAcaoHeroi()
        val alvo = inimigos.firstOrNull { it.id == alvoId && it.vivo }
            ?: return AtaqueResultado(false, false, 0, false, "Alvo inválido ou já fora de combate.").also { log += it.texto }
        if (!ataque.aDistancia) {
            // Aproxima-se até o alcance da arma, gastando até o Deslocamento (assim o golpe alcança).
            val dist = encounter.distancia(alvo)
            if (dist > ataque.alcance) {
                val passo = (dist - ataque.alcance).coerceAtMost(heroi.deslocamentoEfetivo.coerceAtLeast(1))
                if (passo > 0) encounter.moverEmRelacaoAoHeroi(alvo.id, -passo)
            }
            log += "🏃🗡️ Você avança sobre ${alvo.nome} e ataca em movimento."
        } else {
            log += "🏃🎯 Você dispara em movimento contra ${alvo.nome}."
        }
        golpeForaDeAlcance(ataque, alvo)?.let { limparAvaliar(); limparApontar(); limparFinta(); verificarFim(); return it }
        val r = resolverGolpeHeroi(ataque, alvo, Manobra.MOVER_E_ATACAR, local, AtaqueTotalModo.DETERMINADO)
        limparAvaliar(); limparApontar(); limparFinta()
        if (!ataque.aDistancia && ataque.apararTipo == ApararTipo.DESBALANCEADA) atacouDesbalanceada = true
        marcarDespreparoSeNecessario(ataque) // Lote 398
        heroiSemAparar = true // Mover e Atacar: só Esquiva/Bloqueio até o próximo turno (MB p.367).
        verificarFim()
        return r
    }

    /**
     * Ataque Total (Duplo) — MB p.366: DOIS ataques contra o MESMO alvo, exigindo duas armas
     * preparadas. O 1º golpe sai com a mão hábil (NH normal); o 2º, com a arma na mão inábil, sofre
     * −4 a menos que o herói tenha Ambidestria (pág. 38). Sem defesa ativa até o próximo turno.
     * Os bônus de Avaliar/Mira valem só no 1º golpe ("seu próximo ataque").
     */
    fun heroiAtaqueDuplo(
        principal: AtaqueHeroi,
        secundaria: AtaqueHeroi,
        alvoId: String,
        local: LocalAtaque = LocalAtaque.TORSO,
        ambidestria: Boolean = false
    ): List<AtaqueResultado> {
        inicioAcaoHeroi()
        val alvo = inimigos.firstOrNull { it.id == alvoId && it.vivo }
            ?: return listOf(AtaqueResultado(false, false, 0, false, "Alvo inválido ou já fora de combate.").also { log += it.texto })
        log += "⚔️ Ataque Total (Duplo): você golpeia ${alvo.nome} com as duas armas!"
        val resultados = mutableListOf<AtaqueResultado>()
        // 1º golpe — mão hábil, NH normal (DUPLO não dá +4/+1). Consome Avaliar/Mira.
        val fora1 = golpeForaDeAlcance(principal, alvo)
        resultados += fora1 ?: resolverGolpeHeroi(principal, alvo, Manobra.ATAQUE_TOTAL, local, AtaqueTotalModo.DUPLO)
        limparAvaliar(); limparApontar(); limparFinta()
        // 2º golpe — mão inábil (−4 salvo Ambidestria), só se o alvo continua de pé.
        if (alvo.vivo) {
            val penOff = if (ambidestria) 0 else -4
            val fora2 = golpeForaDeAlcance(secundaria, alvo)
            resultados += fora2 ?: resolverGolpeHeroi(
                secundaria, alvo, Manobra.ATAQUE_TOTAL, local, AtaqueTotalModo.DUPLO,
                modAdicional = penOff, rotuloModAdicional = if (ambidestria) "mão inábil (Ambidestria)" else "mão inábil"
            )
        } else {
            log += "  └ ${alvo.nome} já caiu — o segundo golpe não chega a ser desferido."
        }
        // Desbalanceada: se qualquer golpe corpo-a-corpo usou arma "D" (não apara até o próximo turno).
        if ((!principal.aDistancia && principal.apararTipo == ApararTipo.DESBALANCEADA) ||
            (!secundaria.aDistancia && secundaria.apararTipo == ApararTipo.DESBALANCEADA)) atacouDesbalanceada = true
        marcarDespreparoSeNecessario(principal); marcarDespreparoSeNecessario(secundaria) // Lote 398
        heroiSemDefesaAtiva = true // Ataque Total: sem defesa ativa até o próximo turno (MB p.366).
        verificarFim()
        return resultados
    }

    /** Golpe fora de alcance (Máx à distância OU reach corpo-a-corpo): loga e devolve o resultado; null se está no alcance. */
    /**
     * Lote A1-c — INSUBSTANCIALIDADE (MB, vantagem de 80 pontos):
     * *"Ataques físicos e de energia não afetam o personagem, mas ele continua vulnerável a ataques
     * psíquicos e mágicos."*
     *
     * Vale ANTES de rolar para acertar: não é errar o golpe, é o golpe atravessar. A saída é a
     * mágica **Afetar Espíritos** — *"uma arma com essa mágica pode prejudicar um espírito
     * insubstancial"* — que marca `afetaInsubstancial` em quem a recebeu.
     *
     * Só barra ARMA. Magia continua passando: o funil `aplicarDanoMagico` não consulta isto.
     */
    private fun golpeContraInsubstancial(alvo: Combatente): AtaqueResultado? {
        if (alvo.tipoCriatura != TipoCriatura.INSUBSTANCIAL) return null
        if (heroi.afetaInsubstancial) return null
        val txt = "👻 Sua arma atravessa ${alvo.nome} sem resistência — ele é insubstancial. " +
            "Só magia (ou uma arma sob Afetar Espíritos) o alcança."
        log += txt
        return AtaqueResultado(false, false, 0, false, txt)
    }

    private fun golpeForaDeAlcance(ataque: AtaqueHeroi, alvo: Combatente): AtaqueResultado? {
        val dist = encounter.distancia(alvo)
        if (dist <= ataque.alcance) return null
        limparApontar()
        val txt = if (ataque.aDistancia)
            "🎯 ${alvo.nome} está fora de alcance (${dist}m > Máx ${ataque.alcance}m): o tiro não chega."
        else
            "🗡️ ${alvo.nome} está longe demais (${dist}m > alcance ${ataque.alcance}m da arma) — aproxime-se."
        log += txt
        return AtaqueResultado(false, false, 0, false, txt)
    }

    /**
     * Resolve UM golpe do herói já validado (alvo vivo e no alcance): rola acerto (B2) → defesa do
     * NPC (B5) → dano/ferimento (B3/B4) e narra. Não cuida do bookkeeping de turno — quem chama
     * consome Avaliar/Mira, marca desbalanceada/sem-defesa e verifica o fim. [modAdicional] aplica
     * uma penalidade nomeada extra (ex.: −4 da mão inábil no Ataque Total Duplo).
     */
    private fun resolverGolpeHeroi(
        ataque: AtaqueHeroi,
        alvo: Combatente,
        manobra: Manobra,
        local: LocalAtaque,
        ataqueTotalModo: AtaqueTotalModo,
        modAdicional: Int = 0,
        rotuloModAdicional: String = "",
        enganoso: Int = 0, // Lote 401: Ataque Enganoso — passos de −2 no NH por −1 na defesa do alvo (MB p.369)
        telegrafico: Boolean = false, // Lote PONTE-3: Ataque Telegráfico — +4 p/ acertar, mas +2 nas defesas do alvo (AM p.109)
        dedicadoModo: DedicadoModo = DedicadoModo.DETERMINADO // Lote PONTE-4: modo do Ataque Dedicado (AM p98)
    ): AtaqueResultado {
        val dist = encounter.distancia(alvo)
        // Telegráfico e Enganoso são mutuamente exclusivos (AM p.109): o telegráfico vence se ambos vierem.
        val eng = if (telegrafico) 0 else enganoso
        // Rajada (MB p.374): com CdT≥2 dispara a rajada cheia → bônus para acertar por nº de tiros.
        val tiros = if (ataque.aDistancia) ataque.cadenciaTiro.coerceAtLeast(1) else 1
        val modsExtra: List<CombatActions.ComponenteMod> = buildList {
            // Choque (Lote 382, MB p.419): PV perdidos no turno anterior penalizam DX/IQ deste ataque.
            InjuryRules.penalidadeChoque(heroi.choquePendente, heroi.pvMax).let {
                if (it != 0) add(CombatActions.ComponenteMod("choque", it))
            }
            // Cego (Lote COND-1, MB p.394): −4 para acertar (não vê o alvo).
            if (Condicao.CEGO in heroi.condicoes) add(CombatActions.ComponenteMod("cego", -4))
            // Lote MEC-37 (P4): rider de ofuscamento (Lampejo/Jatos) — −N nas perícias de combate.
            if (heroi.penalidadeCombateTemp > 0) add(CombatActions.ComponenteMod("ofuscado", -heroi.penalidadeCombateTemp))
            // MEC-2 — Nublar no ALVO (−1 a −5): vale para o herói também, não só contra ele.
            if (alvo.buffPenalidadeAtacantes > 0)
                add(CombatActions.ComponenteMod("alvo nublado", -alvo.buffPenalidadeAtacantes))
            if (ataque.aDistancia) {
                // Modificador de Tamanho do alvo (MB p.549): alvo grande é mais fácil de acertar, pequeno mais difícil.
                val mt = alvo.stats?.modificadorTamanho ?: 0
                if (mt != 0) add(CombatActions.ComponenteMod("tamanho do alvo (MT)", mt))
                // Velocidade e Distância (Lote 403, MB p.550): some a velocidade do alvo à distância e busque UMA penalidade.
                val velDist = dist + alvo.velocidadeAtual
                val pen = penalidadeDistancia(velDist)
                if (pen != 0) add(CombatActions.ComponenteMod(
                    if (alvo.velocidadeAtual > 0) "Vel/Dist (${dist}m+${alvo.velocidadeAtual}m/s)" else "distância ${dist}m", pen))
                // Agachar (Lote 416, MB p.368): alvo agachado/deitado é um alvo menor à distância.
                penalidadePosturaAlvejado(alvo.postura).let { if (it != 0) add(CombatActions.ComponenteMod("alvo ${alvo.postura.rotulo}", it)) }
                // Apontar no turno anterior ao mesmo alvo → soma a Precisão (Acc) da arma (MB p.364).
                if (apontarAlvoId == alvo.id) {
                    val acc = ataque.precisao
                    val miraExtra = (apontarStacks - 1).coerceIn(0, 2) // mira de vários turnos: +1 (2s) / +2 (3s+)
                    val firmar = if (apontarFirmado && ataque.armaDeFogo) 1 else 0
                    if (acc != 0) add(CombatActions.ComponenteMod("mira (Acc)", acc))
                    if (miraExtra != 0) add(CombatActions.ComponenteMod("mira contínua", miraExtra))
                    if (firmar != 0) add(CombatActions.ComponenteMod("firmar", firmar))
                    // Teto de pontaria (Lote 402, MB p.364): a soma dos bônus de pontaria não excede o DOBRO da Prec.
                    val excedente = if (acc > 0) (acc + miraExtra + firmar) - acc * 2 else 0
                    if (excedente > 0) add(CombatActions.ComponenteMod("teto de pontaria (2×Acc)", -excedente))
                }
                bonusCadenciaTiro(tiros).let { if (it != 0) add(CombatActions.ComponenteMod("rajada ${tiros} tiros", it)) }
            } else if (avaliarAlvoId == alvo.id && avaliarStacks > 0 && !telegrafico) {
                // Avaliar só vale corpo-a-corpo, contra o alvo avaliado, no ataque seguinte (MB p.365).
                // O +4 do Telegráfico NÃO acumula com o bônus de Avaliar (AM p.109).
                add(CombatActions.ComponenteMod("avaliar", avaliarStacks))
            }
            if (modAdicional != 0) add(CombatActions.ComponenteMod(rotuloModAdicional.ifBlank { "mod" }, modAdicional))
            // Ataque Enganoso (Lote 401, MB p.369): −2 no NH por passo (em troca de −1 na defesa do alvo).
            if (eng > 0) add(CombatActions.ComponenteMod("ataque enganoso", -2 * eng))
            // Ataque Telegráfico (Lote PONTE-3, AM p.109): +4 para acertar (em troca de +2 nas defesas do alvo).
            if (telegrafico) add(CombatActions.ComponenteMod("ataque telegráfico", 4))
            // Modificadores situacionais do Narrador (Lote 424): ação improvisada vira mod nomeado no ataque.
            modsSituacionaisAtaque(heroi.id).forEach { add(CombatActions.ComponenteMod(it.motivo, it.valor)) }
            // Lote TOK-5a (MB p.389): atacar ATRAVÉS de hex ocupado por inimigo (arma alcance ≥2) → −4.
            if (!ataque.aDistancia) {
                val penHex = posicaoBridge?.penalidadeAtravesDeHex(heroi.id, alvo.id, ataque.alcance) ?: 0
                if (penHex != 0) add(CombatActions.ComponenteMod("através de hex ocupado", penHex))
            }
        }
        val atkRaw = CombatActions.resolverAtaque(
            nhBaseArma = ataque.nh, manobra = manobra, postura = heroi.postura,
            local = local, visibilidade = Visibilidade.NORMAL, ataqueTotalModo = ataqueTotalModo, dedicadoModo = dedicadoModo,
            aDistancia = ataque.aDistancia, modsExtra = modsExtra,
            magnitudeArma = if (ataque.aDistancia) ataque.magnitude else null, random = random
        )
        // Telegráfico (AM p.109): o +4 ajuda a ACERTAR, mas NÃO concede golpe fulminante. Se o golpe só seria
        // DECISIVO por causa do +4 (NH antes do +4 não o classificaria), rebaixa para acerto NORMAL (mantém o
        // dano, perde a anulação de defesa). NUNCA promove para FALHA CRÍTICA — um sucesso jamais vira erro.
        val atk = if (telegrafico && atkRaw.critico == CriticoRules.ResultadoCritico.DECISIVO) {
            val recl = CriticoRules.classificar(atkRaw.soma, atkRaw.calculo.nhEfetivo - 4)
            atkRaw.copy(critico = if (recl == CriticoRules.ResultadoCritico.DECISIVO) CriticoRules.ResultadoCritico.DECISIVO else CriticoRules.ResultadoCritico.NORMAL)
        } else atkRaw
        // Lote TOK-5a: o herói VIRA para o alvo ao atacar (mudar facing no próprio turno é livre).
        posicaoBridge?.aoAtacar(heroi.id, alvo.id)
        // Facing do golpe contra o NPC (MB p.374/390): FLANCO −2 na defesa dele; COSTAS anula.
        val facingAlvoNpc = posicaoBridge?.facingDoAtaque(heroi.id, alvo.id)
        val penFlancoNpc = if (facingAlvoNpc == com.gurps.ficha.domain.combat.hex.Facing.FLANCO) 2 else 0
        val costasNpc = facingAlvoNpc == com.gurps.ficha.domain.combat.hex.Facing.COSTAS
        if (penFlancoNpc != 0) log += "  └ você ataca pelo FLANCO de ${alvo.nome}: defesa dele −2 (MB p.390)."
        if (costasNpc) log += "  └ você ataca ${alvo.nome} pelas COSTAS: defesa ANULADA (MB p.374)."
        // Contra ataque à distância o alvo só Esquiva; corpo-a-corpo usa a melhor defesa.
        val (defTipo, defValor) = if (ataque.aDistancia)
            CombatResolver.TipoDefesa.ESQUIVA to esquivaNpc(alvo) else melhorDefesaNpc(alvo)
        // Finta (Lote 383, MB p.366): a margem da finta contra este alvo reduz a defesa dele NESTE golpe.
        val penFinta = if (alvo.id == fintaAlvoId) fintaPenalidade else 0
        // Agarrado (Lote 386, MB p.370): o alvo preso defende-se mal (−4).
        val penAgarrado = if (Condicao.AGARRADO in alvo.condicoes) 4 else 0
        // Ataque Enganoso (−1/passo na defesa do alvo) OU Telegráfico (+2 na defesa do alvo) — exclusivos (eng já zerado se telegráfico).
        // Mods situacionais do Narrador (Lote 424) também ajustam a defesa do NPC alvejado (ex.: distraído −2).
        val defValorFinal = (defValor - penFinta - penAgarrado - eng - penFlancoNpc + (if (telegrafico) 2 else 0) + modSituacionalDefesa(alvo.id)).coerceAtLeast(0)
        val defSoma = rolar3d6()
        // Além de 1/2D, o dano cai pela metade (MB p.270) — aplica no dado básico antes de RD.
        val meioDano = ataque.aDistancia && ataque.meioDano > 0 && dist >= ataque.meioDano
        var danoBasico = (rolarDano(ataque.danoExpr, random) + bonusDanoForte(manobra, ataqueTotalModo, ataque.danoExpr, ataque.aDistancia)
            + modDanoManobra(manobra, dedicadoModo, ataque.danoExpr, ataque.aDistancia) + bonusInvestidaPendente).coerceAtLeast(0)
        // MEC-2: a RD de buff mágico (Proteger Animal RD 5, Armadura) soma à RD do bestiário.
        var rdAlvo = rdComDivisor((alvo.stats?.rd ?: 0) + alvo.buffRd, divisorArmadura(ataque.danoExpr)) // Lote 413: divisor de armadura
        var forcaGrave = false
        // Golpe Fulminante (Lote 384, MB p.558): a defesa já é anulada pelo crítico; a tabela modifica o DANO.
        if (atk.critico == CriticoRules.ResultadoCritico.DECISIVO) {
            val gf = aplicarGolpeFulminante(danoBasico, rdAlvo, ataque.danoExpr)
            danoBasico = gf.dano; rdAlvo = gf.rd; forcaGrave = gf.grave
            log += "  ⭐ Golpe Fulminante — ${gf.nota}"
        }
        val danoBruto = if (meioDano) danoBasico / 2 else danoBasico
        // Lote MEC-9 (CORRIGE o MEC-2) — Arma Flamejante/Congelante/de Relâmpago (+2). O livro diz
        // "após a penetração da armadura E OS MODIFICADORES DE FERIMENTO". O MEC-2 somava ANTES da RD
        // com a justificativa de que (dano+2)−RD == (dano−RD)+2 — verdade para subtração pura, mas o
        // multiplicador de ferimento vem DEPOIS da RD, então o +2 era multiplicado junto: corte ×1,5
        // virava +3, perfuração ×2 virava +4. Agora vai como bônus PÓS-RD no resolver.
        // `armaTipo` impede o +2 do gume vazar para o arco.
        val bonusArmaMagica = heroi.buffs.filter { it.danoArma > 0 && it.danoArmaVale(ataque.aDistancia) }.sumOf { it.danoArma }

        val troca = CombatResolver.resolverTroca(
            defensor = alvo, htDefensor = alvo.htEfetivo, ataque = atk,
            defesaTipo = defTipo, defesaValorFinal = defValorFinal, defesaSoma = defSoma,
            surpresa = costasNpc, // Lote TOK-5a: ataque pelas costas anula a defesa do NPC (MB p.374)
            danoBaseRolado = danoBruto, danoTipo = ataque.tipo,
            local = local, rdLocal = rdAlvo, randomFerimento = random, forcarFerimentoGrave = forcaGrave,
            tolerancia = alvo.stats?.tolerancia ?: ToleranciaFerimentos.NORMAL,
            bonusAposRd = bonusArmaMagica // MEC-9: entra depois da RD e do multiplicador de ferimento
        )
        if (penFinta > 0) log += "  └ finta: a defesa de ${alvo.nome} cai −$penFinta neste golpe (${defValor}→${defValorFinal})."
        log += narrarTroca("Você", alvo.nome, ataque.rotulo.substringBefore(" (").trim(), ataque.aDistancia, atk, defTipo, troca, local, ataque.tipo)
        // Projeção (Lote 417, MB p.378): contusão/corte que acerta pode jogar o alvo para trás (o helper filtra o tipo).
        if (troca.dano != null && alvo.vivo)
            aplicarProjecao(alvo, alvo, danoBruto, ataque.tipo, (troca.dano?.pvSubtrair ?: 0) > 0,
                alvo.stats?.st ?: alvo.pvMax, alvo.dxEfetivoOuProprio)
        // Erro Crítico (Lote 384, MB p.557): o próprio herói tropeça no golpe.
        if (atk.critico == CriticoRules.ResultadoCritico.FALHA_CRITICA)
            aplicarErroCritico(heroi, heroiPerfil.ht, ataque.danoExpr, ataque.desarmado, "Você")
        if (meioDano && troca.dano != null) log += "  └ além de 1/2D (${dist}m ≥ ${ataque.meioDano}m): dano pela metade."
        // Rajada: o primeiro tiro foi resolvido acima; o Recuo define quantos tiros EXTRAS acertam (MB p.374).
        if (tiros >= 2 && !troca.defendeu && troca.dano != null) {
            val extras = acertosDaRajada(atk.margem, ataque.recuo, tiros) - 1
            if (extras > 0 && alvo.vivo) {
                repeat(extras) {
                    if (!alvo.vivo) return@repeat
                    val d = (rolarDano(ataque.danoExpr, random)).let { if (meioDano) it / 2 else it }
                    val rd = HitLocationRules.aplicarDano(alvo.pvMax, d, ataque.tipo, local, alvo.stats?.rd ?: 0, alvo.stats?.tolerancia ?: ToleranciaFerimentos.NORMAL)
                    InjuryRules.ferir(alvo, rd.pvSubtrair, alvo.htEfetivo, random)
                }
                log += "  └ rajada: +$extras projétil(eis) acertam (Recuo ${ataque.recuo}, margem ${atk.margem}) → ${alvo.nome} PV ${alvo.pvAtual}/${alvo.pvMax}."
            }
        }
        val incap = !alvo.vivo
        return AtaqueResultado(
            acertou = atk.resultado == CombatActions.ResultadoAcerto.ACERTO,
            defendeu = troca.defendeu, danoAplicado = troca.dano?.pvSubtrair ?: 0,
            alvoIncapacitado = incap, texto = troca.texto
        )
    }

    /** Manobra não-ofensiva do herói (Defesa Total, Concentrar, Não Fazer Nada…) e Mudar de Postura. */
    fun heroiManobra(manobra: Manobra, novaPostura: Postura? = null): String {
        inicioAcaoHeroi()
        if (manobra == Manobra.MUDAR_POSTURA && novaPostura != null && novaPostura in posturasAlcancaveis()) {
            heroi.postura = novaPostura
        }
        limparAvaliar(); limparApontar(); limparFinta()
        // Concentrar (Lote 397, MB p.344): marca a concentração; o efeito (magia/psi/perícia IQ) é do Narrador,
        // mas a mecânica de combate é o teste de Vontade-3 ao ser perturbado (em npcResolve).
        if (manobra == Manobra.CONCENTRAR) concentrando = true
        // Preparar (Lote 398, MB p.270/366): re-empunha uma arma que ficou despreparada após um golpe desbalanceado.
        val reempunhou = manobra == Manobra.PREPARAR && armaDespreparadaRotulo != null
        if (manobra == Manobra.PREPARAR) prepararArmaEmpunhada()
        val txt = when (manobra) {
            Manobra.MUDAR_POSTURA -> "🧍 Você muda para ${heroi.postura.rotulo}."
            Manobra.CONCENTRAR -> "🧠 Você se concentra (atividade mental). Se for forçado a defender ou for ferido, teste Vontade-3 para não perder a concentração."
            Manobra.PREPARAR -> if (reempunhou) "🤚 Você prepara a arma (re-empunha a arma desbalanceada)." else "🤚 Você: Preparar."
            else -> "🛡️ Você: ${manobra.rotulo}."
        }
        log += txt
        return txt
    }

    /** Avaliar (MB p.365): +1 cumulativo (máx +3) no próximo ataque corpo-a-corpo ao alvo. */
    fun heroiAvaliar(alvoId: String): String {
        inicioAcaoHeroi()
        if (avaliarAlvoId == alvoId) avaliarStacks = (avaliarStacks + 1).coerceAtMost(3)
        else { avaliarAlvoId = alvoId; avaliarStacks = 1 }
        limparApontar(); limparFinta()
        val nome = inimigos.firstOrNull { it.id == alvoId }?.nome ?: "o alvo"
        val txt = "👁️ Você avalia $nome (+$avaliarStacks no próximo golpe corpo-a-corpo)."
        log += txt
        return txt
    }

    // ── MAGIA no combate (Lote MA-3a) ────────────────────────────────────────────────────────────

    /** Resultado factual de uma conjuração no combate, para o feed do Narrador. */
    data class ResultadoConjuracaoCombate(
        val sucesso: Boolean,
        val texto: String,
        val danoCausado: Int = 0,
        val alvoResistiu: Boolean = false,
        /** Lote MA-3c: true quando a conjuração ainda está EM ANDAMENTO (magia de vários segundos). */
        val emAndamento: Boolean = false,
    )

    /** Lote MA-3c: uma conjuração de vários segundos em andamento (N manobras Concentrar). */
    data class ConjuracaoEmAndamento(
        val ctx: ContextoConjuracao,
        val custo: CustoEnergia,
        val energia: Int,
        val nome: String,
        val alvoId: String?,
        val turnosRestantes: Int,
    )

    /** Conjuração multi-turno pendente do herói (null se ele não está concentrando). */
    var conjuracaoEmAndamento: ConjuracaoEmAndamento? = null; private set

    /** Lote MA-3d-2: mágica de TOQUE carregada na mão do herói, à espera de um ataque para descarregar. */
    /**
     * Lote MEC-21: a ENERGIA investida passa a viajar com a mão carregada. Sem ela o descarregar não
     * tinha como escalar dano nem duração de condição.
     */
    data class ToqueCarregado(
        val nome: String,
        val ctx: ContextoConjuracao,
        val nhEfetivoCast: Int,
        val energiaInvestida: Int = 1,
        /** Lote MEC-22: custo por turno para MANTER (Morte Candente: "03/02" → 2). */
        val custoManutencao: Int = 0,
    )
    var toqueCarregado: ToqueCarregado? = null; private set

    /**
     * Lote MEC-23: mágica do herói cuja manutenção venceu e **espera decisão dele**.
     * Manter mágica é OPCIONAL em GURPS — o motor não pode cobrar PF por conta própria.
     * (Tipo aninhado: o controller/UI referenciam `CombatSession.ManutencaoPendente`.)
     */
    data class ManutencaoPendente(val magiaId: String, val custoPf: Int)

    // Lote MOTOR-4: o subsistema INTEIRO de EFEITOS MÁGICOS ATIVOS (buffs, mágicas duradouras,
    // manutenção, dano por turno, abalo de concentração) foi para `subsistemas/EfeitosMagicosDelegate`,
    // testável sozinho. O motor injeta o que o ciclo precisa (log, RNG, combatentes, herói, "reavaliar
    // fim") por lambda e reexpõe a API pública por delegação — quem chamava `registrarMagiaAtiva`/
    // `magiasAtivas`/`resolverManutencao` continua chamando igual.
    private val efeitos = com.gurps.ficha.domain.combat.subsistemas.EfeitosMagicosDelegate(
        log = log,
        random = random,
        combatentes = { encounter.combatentes },
        heroi = { heroi },
        heroiHt = { heroiPerfil.ht },
        heroiVontade = { heroiPerfil.vontade },
        verificarFim = { verificarFim() },
    )

    /** Lote MA-3d-4: mágicas TEMPORÁRIAS/DURADOURAS ativas no combate (delegado ao MOTOR-4). */
    val magiasAtivas: List<MagiaAtivaNoCombate> get() = efeitos.ativas

    val manutencaoPendente: List<ManutencaoPendente> get() = efeitos.manutencaoPendente

    /** Lote MA-3d-4: registra uma mágica ativa (o controller chama após uma conjuração com duração). */
    fun registrarMagiaAtiva(
        nome: String, operadorId: String, alvoId: String?, duracaoSeg: Int,
        custoManutencaoSeg: Int, duracao: TipoDuracao, exigeConcentracao: Boolean,
        buff: com.gurps.ficha.domain.magic.BuffAplicado? = null,
        mecanica: com.gurps.ficha.domain.magic.MagiaMecanica? = null,
    ) = efeitos.registrar(nome, operadorId, alvoId, duracaoSeg, custoManutencaoSeg, duracao, exigeConcentracao, buff, mecanica)

    /** Lote MEC-6: buff de UM ÚNICO USO (Aumentar Força/Destreza/Vitalidade — vale para um teste). */
    fun aplicarBuffDeUmUso(nome: String, buff: com.gurps.ficha.domain.magic.BuffAplicado) =
        efeitos.aplicarBuffDeUmUso(nome, buff)

    /** Lote MEC-23: o jogador decidiu se mantém a mágica (paga o PF) ou a deixa acabar. */
    fun resolverManutencao(magiaId: String, manter: Boolean) = efeitos.resolverManutencao(magiaId, manter)

    /** Lote MEC-2: dissipa uma mágica ativa pelo nome, REVERTENDO o buff que ela aplicou. */
    fun dissiparMagiaAtiva(magiaId: String): Boolean = efeitos.dissipar(magiaId)

    /**
     * Lote MA-3a/3b/3c: o herói CONJURA uma magia no combate (manobra Concentrar). O CONTROLLER monta
     * o [ctx] a partir da ficha (NH básico, Aptidão, classe, custo lidos do catálogo); aqui o motor rola
     * os dados (via [MagicCasting], o resolvedor do MA-2), paga a fadiga e aplica o que é DERIVÁVEL:
     *  - **Projétil** (MA-3b): 2 testes (Magia p.12) — lançamento + Ataque Inato para acertar (aprox.
     *    DX + SSR de distância); o alvo pode ESQUIVAR, nunca aparar. Acertou → dano 1d × energia com RD.
     *  - **Resistível**: Disputa Rápida (HT/Vont/… do alvo, Regra do 16) — efeito além disso é narrado.
     *  - **Falha crítica**: choque de retorno (dano/atordoamento no operador).
     *  - **Multi-turno** (MA-3c): se [tempoOperacaoSeg] > 1, entra em concentração por N turnos
     *    ([conjuracaoEmAndamento]) e SÓ resolve no último (via [continuarConjuracao]); ser ferido ou
     *    atordoado no meio pode fazer perder a magia ([interromperConjuracaoSeConjurando], Magia p.7).
     * Efeitos bespoke (Sono, Cura, Criar Objeto…) ficam para o Narrador; o motor loga o fato.
     */
    fun heroiConjurar(
        ctx: ContextoConjuracao,
        custo: CustoEnergia,
        energiaInvestida: Int,
        magiaNome: String,
        alvoId: String?,
        tempoOperacaoSeg: Int = 1,
    ): ResultadoConjuracaoCombate {
        inicioAcaoHeroi()
        limparAvaliar(); limparApontar(); limparFinta()
        // Lote MEC-13: magia que só afeta OBJETO INANIMADO (Desintegrar, Fender, Enfraquecer,
        // Explodir) não pode ser lançada num combatente. Recusa ANTES de gastar fadiga — o jogador
        // não perde o turno por uma jogada que a regra não permite.
        if (alvoId != null && com.gurps.ficha.domain.magic.MagicMechanics.soAfetaObjeto(ctx.mecanica)) {
            val alvoNome = encounter.combatentes.firstOrNull { it.id == alvoId }?.nome ?: "esse alvo"
            val t = "🚫 $magiaNome afeta apenas objetos inanimados — não pode ser lançada em $alvoNome."
            log += t
            return ResultadoConjuracaoCombate(false, t)
        }
        // Lote MEC-15: alvo além da Distância Máxima do Projétil — "O alvo NÃO PODE estar a uma
        // distância maior que Distância Max". Recusa antes de gastar fadiga (o operador enxerga a
        // distância; não faz sentido queimar o turno num tiro que a regra proíbe).
        if (alvoId != null && com.gurps.ficha.domain.magic.MagicMechanics
                .foraDoAlcanceMaximo(ctx.mecanica, ctx.distanciaMetros)) {
            val alvoNome = encounter.combatentes.firstOrNull { it.id == alvoId }?.nome ?: "o alvo"
            val t = "🚫 $alvoNome está a ${ctx.distanciaMetros}m — além do alcance máximo de " +
                "$magiaNome (Máx ${ctx.mecanica?.alcanceMaximo}m)."
            log += t
            return ResultadoConjuracaoCombate(false, t)
        }
        // Lote MEC-28 (C5, Magia p.11-12): com a mão CARREGADA por uma mágica de Toque, o operador
        // *"não pode fazer outras mágicas"*. Ele pode atacar, sustentar ou dissipar — não conjurar.
        //
        // A metade do PROJÉTIL da mesma regra ("não poderá fazer outra operação mágica enquanto
        // segurar o projétil") não tem como ser violada hoje: o projétil é conjurado e arremessado no
        // mesmo turno, nunca fica sustentado (ver C1, bloqueada pelo mesmo motivo).
        toqueCarregado?.let { t ->
            val msg = "✋ Sua mão está carregada com ${t.nome} — não dá para conjurar outra mágica. " +
                "Ataque um adjacente para descarregar, ou dissipe (ação livre)."
            log += msg
            return ResultadoConjuracaoCombate(false, msg)
        }
        // Lote MEC-39 (P11): idem enquanto SEGURA um projétil — "não poderá fazer outra operação
        // mágica enquanto segurar o projétil" (Magia p.12).
        projetilCarregado?.let { p ->
            val msg = "✋ Você está segurando ${p.nome} — não dá para conjurar outra mágica. " +
                "Arremesse, aumente ou dissipe (ação livre)."
            log += msg
            return ResultadoConjuracaoCombate(false, msg)
        }
        // Lote COND-1: silenciado não conjura (o ritual mágico exige fala, Magia p.8).
        if (Condicao.SILENCIADO in heroi.condicoes) {
            val t = "🤐 Você está SILENCIADO — não consegue conjurar $magiaNome (o ritual exige fala)."
            log += t; return ResultadoConjuracaoCombate(false, t)
        }
        if (tempoOperacaoSeg > 1) {
            // O turno inicial já é a 1ª manobra Concentrar → restam (tempo − 1) turnos.
            conjuracaoEmAndamento = ConjuracaoEmAndamento(ctx, custo, energiaInvestida, magiaNome, alvoId, tempoOperacaoSeg - 1)
            val t = "🔮 Você começa a conjurar $magiaNome (${tempoOperacaoSeg}s de concentração — mantenha o foco)."
            log += t
            return ResultadoConjuracaoCombate(sucesso = false, texto = t, emAndamento = true)
        }
        return resolverConjuracao(ctx, custo, energiaInvestida, magiaNome, alvoId)
    }

    /**
     * Lote MA-3c: continua a conjuração multi-turno (mais uma manobra Concentrar). Resolve quando o
     * último turno de concentração termina; senão devolve null (ainda concentrando).
     */
    fun continuarConjuracao(): ResultadoConjuracaoCombate? {
        val c = conjuracaoEmAndamento ?: return null
        inicioAcaoHeroi(); limparAvaliar(); limparApontar(); limparFinta()
        val rest = c.turnosRestantes - 1
        if (rest > 0) {
            conjuracaoEmAndamento = c.copy(turnosRestantes = rest)
            log += "🔮 Você continua conjurando ${c.nome} (${rest}s restante(s))."
            return ResultadoConjuracaoCombate(sucesso = false, texto = log.last(), emAndamento = true)
        }
        conjuracaoEmAndamento = null
        return resolverConjuracao(c.ctx, c.custo, c.energia, c.nome, c.alvoId)
    }

    /** Lote MA-3c: aborta a conjuração inacabada sem custo (Magia p.8). NÃO consome o turno. */
    fun abortarConjuracao() {
        val c = conjuracaoEmAndamento ?: return
        conjuracaoEmAndamento = null
        log += "✋ Você aborta a conjuração de ${c.nome} (sem custo)."
    }

    /**
     * Lote MA-3c: o herói foi distraído durante a concentração (Magia p.7) — atordoado PERDE a magia
     * automaticamente; ferido/agarrado/projetado exige Vontade−3 para manter. O CONTROLLER chama após
     * o turno do NPC quando o herói levou dano ou ficou atordoado.
     */
    fun interromperConjuracaoSeConjurando(atordoado: Boolean, rolagemVontade: Int) {
        val c = conjuracaoEmAndamento ?: return
        if (atordoado) {
            conjuracaoEmAndamento = null
            log += "💫 Atordoado durante a concentração — você PERDE a conjuração de ${c.nome} (Magia p.7)."
            return
        }
        val alvo = heroiPerfil.vontade - 3
        if (rolagemVontade > alvo) {
            conjuracaoEmAndamento = null
            log += "😖 Distraído (Vontade−3: precisava $alvo, rolou $rolagemVontade) — você perde a conjuração de ${c.nome}."
        } else {
            log += "😤 Você mantém a concentração em ${c.nome} apesar do golpe (Vontade−3: $alvo, rolou $rolagemVontade)."
        }
    }

    /** Resolve de fato a conjuração (rolagem + custo + efeito). Usada pelo lançamento de 1s e pelo fim do multi-turno. */
    private fun resolverConjuracao(
        ctx: ContextoConjuracao,
        custo: CustoEnergia,
        energiaInvestida: Int,
        magiaNome: String,
        alvoId: String?,
    ): ResultadoConjuracaoCombate {
        val nhEf = MagicCasting.nhEfetivo(ctx)
        val custoTotal = MagicCasting.custoTotal(ctx, custo, energiaInvestida.takeIf { custo.variavel })
        val rol = rolar3d6()
        val r = MagicCasting.resolver(nhEf.valor, rol, custoTotal, ctx.classe, rolagemChoqueRetorno3d = rolar3d6())

        // Paga o custo: primeiro os PV que o mago escolheu QUEIMAR (dói — Magia p.8; a penalidade
        // de −1/PV no NH já está no NH efetivo via ctx.pvQueimados), o restante sai dos PF.
        val pvPagos = ctx.pvQueimados.coerceIn(0, r.custoAPagar)
        if (pvPagos > 0) InjuryRules.ferir(heroi, pvPagos, heroiPerfil.ht, random)
        heroi.pfAtual = (heroi.pfAtual - (r.custoAPagar - pvPagos)).coerceAtLeast(0)

        val modsTxt = if (nhEf.componentes.isEmpty()) "" else
            " [" + nhEf.componentes.joinToString(", ") { "${it.motivo} ${if (it.valor >= 0) "+" else ""}${it.valor}" } + "]"
        val alvo = alvoId?.let { id -> inimigos.firstOrNull { it.id == id && it.vivo } }
        val sb = StringBuilder()

        when (r.resultado) {
            ResultadoOperacao.FALHA_CRITICA -> {
                sb.append("💥 CHOQUE DE RETORNO ao conjurar $magiaNome! (NH ${nhEf.valor}$modsTxt, rolou $rol) ")
                aplicarChoqueRetorno(r.choqueRetorno, sb)
                verificarFim(); log += sb.toString().trim()
                return ResultadoConjuracaoCombate(false, sb.toString().trim())
            }
            ResultadoOperacao.FRACASSO -> {
                sb.append("✨ Você falha ao conjurar $magiaNome (NH ${nhEf.valor}$modsTxt, rolou $rol). Perde ${r.custoAPagar} PF.")
                log += sb.toString().trim()
                return ResultadoConjuracaoCombate(false, sb.toString().trim())
            }
            else -> { // SUCESSO ou SUCESSO_DECISIVO
                val decisivo = r.resultado == ResultadoOperacao.SUCESSO_DECISIVO
                sb.append(if (decisivo) "🌟 Sucesso DECISIVO em $magiaNome" else "🔮 Você conjura $magiaNome")
                sb.append(" (NH ${nhEf.valor}$modsTxt, rolou $rol; custo ${r.custoAPagar} PF).")

                // Toque (Lote MA-3d-2, Magia p.11-12): o sucesso CARREGA a mão; o efeito só ocorre ao
                // descarregar num ataque corpo-a-corpo ([heroiEntregarToque]). Resistência é no 2º teste, lá.
                if (TipoClasseMagia.TOQUE in ctx.classe.classes) {
                    // MEC-22: guarda tambem o custo de MANTER, que a magia de tique cobra por turno.
                    toqueCarregado = ToqueCarregado(magiaNome, ctx, nhEf.valor, energiaInvestida,
                        custoManutencao = custo.manutencao ?: (custo.base ?: custo.minimo).let { (it + 1) / 2 })
                    sb.append(" Sua mão fica CARREGADA — ataque um oponente adjacente para descarregar (Magia p.12).")
                    log += sb.toString().trim()
                    return ResultadoConjuracaoCombate(true, sb.toString().trim())
                }

                // Lote MAG-7: Mágica Penetrante não fere — PREPARA um divisor de armadura para a
                // PRÓXIMA magia de dano (MB p.378). Escala com a energia investida.
                if (ctx.mecanica?.concedeDivisorArmadura == true) {
                    divisorArmaduraPendente = com.gurps.ficha.domain.magic.MagicMechanics.divisorArmaduraPorEnergia(energiaInvestida)
                    sb.append(" A próxima magia de dano fura a armadura (÷$divisorArmaduraPendente).")
                    verificarFim(); log += sb.toString().trim()
                    return ResultadoConjuracaoCombate(true, sb.toString().trim())
                }

                var alvoResistiu = false
                var dano = 0

                if (r.exigeResistencia && alvo != null && ctx.classe.resistencia != null) {
                    val resist = resistenciaDoAlvo(alvo, ctx.classe.resistencia!!)
                    val rr = MagicCasting.resolverResistencia(nhEf.valor, rol, resist, rolar3d6(), regraDo16 = true)
                    // MEC-31: no modo BONECO nada resiste.
                    alvoResistiu = npcResistiu(rr.alvoResistiu) // MEC-31
                    sb.append(if (alvoResistiu) " ${alvo.nome} RESISTE (resistência $resist)."
                              else " ${alvo.nome} não resiste (resistência $resist).")
                }

                if (ctx.mecanica?.removeCondicoes?.isNotEmpty() == true) {
                    // Lote MAG-4: cura que LIMPA condição (Cessar Sangramento/Paralisia, Restaurar
                    // Visão). Sem alvo explícito, limpa o próprio operador (automagia). O delegate faz.
                    efeitos.removerCondicoes(alvo ?: heroi, ctx.mecanica!!.removeCondicoes, ctx.mecanica!!.curaAoLimpar, sb)
                } else if (com.gurps.ficha.domain.magic.MagicMechanics.temCuraEstruturada(ctx.mecanica)) {
                    // Lote MEC-10: magia de CURA restaura PV. Sem alvo explícito, cura o próprio
                    // operador (automagia) — é o caso comum: o mago se cura no meio da luta.
                    aplicarCuraMagica(alvo ?: heroi, energiaInvestida, ctx.mecanica!!, sb)
                } else if (!alvoResistiu && com.gurps.ficha.domain.magic.MagicMechanics.usaBandas(ctx.mecanica) && alvo != null) {
                    // Lote MEC-41 (conserta o MEC-37): o LAMPEJO é classe **Comum** no livro, não Área —
                    // então as bandas de distância, que eu havia posto SÓ no ramo de área, nunca rodavam.
                    // O clarão pega TODOS dentro de `condicaoRaioM`, cada um pela sua distância ao alvo.
                    val centro = alvo
                    val raio = ctx.mecanica!!.condicaoRaioM.coerceAtLeast(1)
                    val atingidos = inimigos.filter { it.vivo && distanciaEntre(centro, it) <= raio }
                    atingidos.forEach { a ->
                        val d = distanciaEntre(centro, a)
                        val banda = com.gurps.ficha.domain.magic.MagicMechanics.bandaPara(ctx.mecanica, d) ?: return@forEach
                        if (banda.cegoSeg > 0) imporCondicaoMagica(a, "cego", sb, banda.cegoSeg)
                        if (banda.riderPenalidade > 0 && banda.riderSeg > 0) {
                            a.penalidadeCombateTemp = maxOf(a.penalidadeCombateTemp, banda.riderPenalidade)
                            a.penalidadeCombateSeg = maxOf(a.penalidadeCombateSeg, banda.riderSeg)
                            sb.append(" ${a.nome} fica ofuscado (−${banda.riderPenalidade} nas perícias de combate por ${banda.riderSeg}s).")
                        }
                    }
                } else if (!alvoResistiu && ctx.mecanica?.efeito == "condicao" && alvo != null) {
                    // Lote COND-1: magia de CONDIÇÃO pura (Sono, Cegueira, Medo, Paralisar…) — no sucesso
                    // não resistido, impõe a condição (a resistência já veio da classe R-XXX, se houver).
                    // Lote MEC-18: quando a classe NÃO dá resistência mas a magia tem teste próprio
                    // (Jato de Som, classe "Comum": HT menos a energia gasta, +1 a cada 5 de RD), o
                    // alvo testa aqui. Sem isto ele era atordoado sem teste nenhum.
                    val mec = ctx.mecanica
                    val testeProprio = ctx.classe.resistencia == null &&
                        com.gurps.ficha.domain.magic.MagicMechanics.temTesteProprioDeCondicao(mec)
                    if (testeProprio) {
                        val rdAlvo = (alvo.stats?.rd ?: 0) + alvo.buffRd
                        val alvoHt = com.gurps.ficha.domain.magic.MagicMechanics
                            .resistenciaEfetivaDaCondicao(mec, alvo.htEfetivo, energiaInvestida, rdAlvo)
                        val rolCond = rolar3d6()
                        if (rolCond <= alvoHt) sb.append(" ${alvo.nome} RESISTE (teste $alvoHt, rolou $rolCond).")
                        else imporCondicaoMagica(alvo, mec.condicao, sb, com.gurps.ficha.domain.magic.MagicMechanics.duracaoCondicaoSeg(mec, energiaInvestida), escapeDaMecanica(mec, energiaInvestida))
                    } else {
                        imporCondicaoMagica(alvo, mec.condicao, sb, com.gurps.ficha.domain.magic.MagicMechanics.duracaoCondicaoSeg(mec, energiaInvestida), escapeDaMecanica(mec, energiaInvestida))
                    }
                } else if (!alvoResistiu && TipoClasseMagia.PROJETIL in ctx.classe.classes && alvo != null) {
                    // One-shot: conjura e arremessa no mesmo turno (o caso de "1 segundo").
                    dano = resolverArremessoProjetil(alvo, energiaInvestida.coerceAtLeast(1), ctx, sb)
                } else if (!alvoResistiu && alvo != null && ctx.mecanica?.entrega == "feixe") {
                    // Lote P9: FEIXE tem jogada de ACERTO própria (DX−4 ou Ataque Inato) e o alvo
                    // pode esquivar ou bloquear. Antes caía no ramo de dano direto logo abaixo, que
                    // aplica o dano SEM teste nenhum — o jato acertava sempre.
                    dano = resolverFeixe(alvo, energiaInvestida, ctx, sb)
                } else if (!alvoResistiu && alvo != null &&
                    (ctx.danoPorEnergia || com.gurps.ficha.domain.magic.MagicMechanics.temDanoEstruturado(ctx.mecanica))) {
                    // Lote MA-6/AR-1: magia de dano DIRETA (não-Projétil) — funciona no sucesso (sem teste
                    // de acerto). Usa a mecânica curada do catálogo quando houver; senão 1d × energia (p.14).
                    dano = aplicarDanoMagico(alvo, energiaInvestida, ctx.mecanica, sb)
                } else if (!alvoResistiu && TipoClasseMagia.PROJETIL !in ctx.classe.classes) {
                    sb.append(" Efeito narrado pelo Mestre" + (ctx.resumoEfeito?.let { " — $it" } ?: "") + ".")
                }

                verificarFim(); log += sb.toString().trim()
                return ResultadoConjuracaoCombate(true, sb.toString().trim(), dano, alvoResistiu)
            }
        }
    }

    /**
     * Lote MA-3d: conjuração de ÁREA (Magia p.11/13). UM único teste de lançamento; custo × raio;
     * TODOS os combatentes dentro do raio são afetados — cada um resiste sozinho contra a margem do
     * operador (p.14). O CONTROLLER calcula quem está na área pela grade e passa os ids em [alvosNaArea];
     * [ctx.raioAreaMetros] rege o custo. O efeito (dano/condição) é bespoke → narrado pelo Mestre; o
     * motor identifica quem foi atingido e quem resistiu.
     */
    fun heroiConjurarArea(
        ctx: ContextoConjuracao,
        custo: CustoEnergia,
        energiaInvestida: Int,
        magiaNome: String,
        alvosNaArea: List<String>,
        /**
         * Lote MEC-14: distância (m) de cada alvo ao CENTRO da explosão. Só o controller tático sabe
         * disso (tem as posições no grid). Vazio = sem decaimento (todos levam o dano cheio), que é o
         * certo para chuva/nuvem — dano ambiental, não onda de choque.
         */
        distanciaAoCentro: Map<String, Int> = emptyMap(),
    ): ResultadoConjuracaoCombate {
        inicioAcaoHeroi(); limparAvaliar(); limparApontar(); limparFinta()
        val nhEf = MagicCasting.nhEfetivo(ctx)
        val custoTotal = MagicCasting.custoTotal(ctx, custo, energiaInvestida.takeIf { custo.variavel })
        val rol = rolar3d6()
        val r = MagicCasting.resolver(nhEf.valor, rol, custoTotal, ctx.classe, rolagemChoqueRetorno3d = rolar3d6())
        val pvPagos = ctx.pvQueimados.coerceIn(0, r.custoAPagar)
        if (pvPagos > 0) InjuryRules.ferir(heroi, pvPagos, heroiPerfil.ht, random)
        heroi.pfAtual = (heroi.pfAtual - (r.custoAPagar - pvPagos)).coerceAtLeast(0)

        val sb = StringBuilder()
        when (r.resultado) {
            ResultadoOperacao.FALHA_CRITICA -> {
                sb.append("💥 CHOQUE DE RETORNO ao conjurar $magiaNome (área)! (NH ${nhEf.valor}, rolou $rol) ")
                aplicarChoqueRetorno(r.choqueRetorno, sb)
                verificarFim(); log += sb.toString().trim()
                return ResultadoConjuracaoCombate(false, sb.toString().trim())
            }
            ResultadoOperacao.FRACASSO -> {
                sb.append("✨ Você falha ao conjurar $magiaNome em área (NH ${nhEf.valor}, rolou $rol). Perde ${r.custoAPagar} PF.")
                log += sb.toString().trim()
                return ResultadoConjuracaoCombate(false, sb.toString().trim())
            }
            else -> {
                val decisivo = r.resultado == ResultadoOperacao.SUCESSO_DECISIVO
                sb.append(if (decisivo) "🌟 Sucesso DECISIVO —" else "🔮 Você conjura")
                sb.append(" $magiaNome em ÁREA (raio ${ctx.raioAreaMetros}m; NH ${nhEf.valor}, rolou $rol; custo ${r.custoAPagar} PF).")
                val alvos = alvosNaArea.mapNotNull { id -> inimigos.firstOrNull { it.id == id && it.vivo } }
                // Quem foi atingido (não resistiu). Resistíveis fazem a disputa; senão todos são atingidos.
                val atingidos = mutableListOf<Combatente>(); val resistiram = mutableListOf<String>()
                for (a in alvos) {
                    val resiste = r.exigeResistencia && ctx.classe.resistencia != null &&
                        npcResistiu(MagicCasting.resolverResistencia(nhEf.valor, rol, resistenciaDoAlvo(a, ctx.classe.resistencia!!), rolar3d6(), regraDo16 = true).alvoResistiu)
                    if (resiste) resistiram.add(a.nome) else atingidos.add(a)
                }
                if (alvos.isEmpty()) sb.append(" Nenhum inimigo na área.")
                else {
                    if (atingidos.isNotEmpty()) sb.append(" Atinge: ${atingidos.joinToString(", ") { it.nome }}.")
                    if (resistiram.isNotEmpty()) sb.append(" Resistiram: ${resistiram.joinToString(", ")}.")
                    // Lote MEC-37 (P4): Lampejo — efeito em BANDAS por distância ao centro. Cada
                    // atingido pega sua faixa (cegueira + ofuscamento −N por T segundos).
                    if (com.gurps.ficha.domain.magic.MagicMechanics.usaBandas(ctx.mecanica) && atingidos.isNotEmpty()) {
                        atingidos.forEach { a ->
                            val dist = distanciaAoCentro[a.id] ?: 0
                            val banda = com.gurps.ficha.domain.magic.MagicMechanics.bandaPara(ctx.mecanica, dist) ?: return@forEach
                            if (banda.cegoSeg > 0) imporCondicaoMagica(a, "cego", sb, banda.cegoSeg)
                            if (banda.riderPenalidade > 0 && banda.riderSeg > 0) {
                                a.penalidadeCombateTemp = maxOf(a.penalidadeCombateTemp, banda.riderPenalidade)
                                a.penalidadeCombateSeg = maxOf(a.penalidadeCombateSeg, banda.riderSeg)
                                sb.append(" ${a.nome} fica ofuscado (−${banda.riderPenalidade} nas perícias de combate por ${banda.riderSeg}s).")
                            }
                        }
                    // Lote COND-1: magia de CONDIÇÃO em área (Sono coletivo etc.) — impõe em cada atingido.
                    } else if (ctx.mecanica?.efeito == "condicao" && atingidos.isNotEmpty()) {
                        atingidos.forEach { imporCondicaoMagica(it, ctx.mecanica.condicao, sb, com.gurps.ficha.domain.magic.MagicMechanics.duracaoCondicaoSeg(ctx.mecanica, energiaInvestida)) }
                    } else if ((ctx.danoPorEnergia || com.gurps.ficha.domain.magic.MagicMechanics.temDanoEstruturado(ctx.mecanica)) && atingidos.isNotEmpty()) {
                        val energia = energiaInvestida.coerceAtLeast(1)
                        // AR-1: dado estruturado do catálogo quando houver; senão 1d × energia (p.14).
                        // MEC-2: `danoFixo` também aqui — o Géiser é de ÁREA, e este ramo tem a própria
                        // cópia do expandirDano; sem isto ele sairia 15d (custo 5) apesar do MEC-1.
                        // Lote MEC-36 (P8): `danoDeAreaComDegrau` já escolhe entre o dado normal e o
                        // "2d-2 por custo dobrado" da Chuva de Fogo/Pedras.
                        val expr = if (ctx.mecanica?.danoPorEnergia != null)
                            com.gurps.ficha.domain.magic.MagicMechanics.danoDeAreaComDegrau(ctx.mecanica, energia)
                        else "${energia}d"
                        val tipo = if (ctx.mecanica?.tipoDano == "corte") DanoTipo.CORT else if (ctx.mecanica?.tipoDano == "perf") DanoTipo.PERF else DanoTipo.CONT
                        val bruto = rolarDano(expr, random)
                        // Lote MEC-14: EXPLOSÃO — quem está além de 1m do centro divide o dano por
                        // (3 × distância), arredondando para baixo. Sem `explosaoDivisorPorMetro` (ou
                        // sem posições) todos levam cheio, que é o certo para chuva/nuvem.
                        val divisorExpl = ctx.mecanica?.explosaoDivisorPorMetro ?: 0
                        val partes = atingidos.map { a ->
                            // Lote A1: imunidade por elemento vale na ÁREA também. Estar dentro do
                            // raio não fere quem é imune ao que está caindo.
                            if (com.gurps.ficha.domain.magic.MagicMechanics
                                    .imuneAo(ctx.mecanica?.elementoDano, a.imunidades)) {
                                return@map "${a.nome} IMUNE"
                            }
                            val distCentro = distanciaAoCentro[a.id] ?: 0
                            val brutoAqui = com.gurps.ficha.domain.magic.MagicMechanics
                                .danoDaExplosao(bruto, distCentro, divisorExpl)
                            // LOG-1: a narrativa só mostra o dano final — aqui sai a conta da explosão.
                            if (divisorExpl > 0) SagaLog.mecanica(
                                "explosão: ${a.nome} a ${distCentro}m do centro — bruto $bruto → $brutoAqui " +
                                    "(divisor ${divisorExpl}×dist)")
                            val rd = rdContraMagia(a, ctx.mecanica) // MEC-38 (P7)
                            val dn = HitLocationRules.aplicarDano(a.pvMax, brutoAqui, tipo, LocalAtaque.TORSO,
                                rd, a.stats?.tolerancia ?: ToleranciaFerimentos.NORMAL)
                            InjuryRules.ferir(a, dn.pvSubtrair, a.htEfetivo, random)
                            "${a.nome} ${dn.pvSubtrair}" + if (!a.vivo) " (fora!)" else ""
                        }
                        sb.append(" Dano $expr: ${partes.joinToString(", ")}.")
                    } else {
                        sb.append(" Efeito narrado pelo Mestre" + (ctx.resumoEfeito?.let { " — $it" } ?: "") + ".")
                    }
                }
                verificarFim(); log += sb.toString().trim()
                return ResultadoConjuracaoCombate(true, sb.toString().trim())
            }
        }
    }

    /**
     * Lote MEC-39 (P11): resolução do ARREMESSO de um projétil mágico (Magia p.12): teste de Ataque
     * Inato para acertar (aprox. DX + SSR de distância), o alvo ESQUIVA ou bloqueia mas NUNCA apara,
     * e no acerto aplica o dano (1/2D pela distância). Reusado pelo one-shot e pelo projétil carregado.
     */
    // Lote MOTOR-2: o funil de dano mágico (imunidade → tipo de criatura → rolar/RD/1-2D → condição)
    // foi para `subsistemas/DanoMagicoResolver`, testável sozinho. As 4 entradas (magia direta, área,
    // feixe, explosão de projétil) chamam este `aplicarDanoMagico`, que agora só delega.
    private val danoMagico = com.gurps.ficha.domain.combat.subsistemas.DanoMagicoResolver(
        random = random,
        rdContraMagia = { alvo, mec -> rdContraMagia(alvo, mec) },
        imporCondicao = { alvo, cond, sb, dur -> imporCondicaoMagica(alvo, cond, sb, dur) },
    )

    // Lote MOTOR-3: a resolução de ACERTO+DEFESA da magia à distância (feixe, arremesso, explosão)
    // foi para `subsistemas/AtaqueMagicoResolver`, testável sozinho. O motor injeta o funil de dano
    // (MOTOR-2), a DX/Ataque Inato do herói e as defesas do NPC; a conjuração em si (NH/custo/PF)
    // continua aqui, porque é o coração e não um subsistema à parte.
    private val ataqueMagico = com.gurps.ficha.domain.combat.subsistemas.AtaqueMagicoResolver(
        random = random,
        danoMagico = danoMagico,
        heroiNhAtaqueInato = { heroiPerfil.nhAtaqueInato },
        heroiDx = { heroiPerfil.dx },
        esquivaNpc = { esquivaNpc(it) },
        bloqueioNpc = { bloqueioNpc(it) },
        npcSeDefendeu = { valor, rol -> npcSeDefendeu(valor, rol) },
    ).also {
        // Ocupação-padrão do respingo (faixas). O controller a troca pelo cálculo real por hex via
        // o `vizinhosDoImpacto` reexposto abaixo.
        it.vizinhosDoImpacto = { alvo ->
            encounter.combatentes.filter { c -> c.vivo && c.id != alvo.id }
                .map { c -> c to distanciaEntre(c, alvo) }
                .filter { (_, d) -> d <= RAIO_RESPINGO_M }
        }
    }

    private fun resolverArremessoProjetil(
        alvo: Combatente, energia: Int, ctx: ContextoConjuracao, sb: StringBuilder, bonusPrecisao: Int = 0,
    ): Int = ataqueMagico.resolverArremesso(alvo, energia, ctx, sb, bonusPrecisao)

    private fun resolverFeixe(alvo: Combatente, energia: Int, ctx: ContextoConjuracao, sb: StringBuilder): Int =
        ataqueMagico.resolverFeixe(alvo, energia, ctx, sb)

    /** Ponto de injeção do respingo (o controller troca pelo cálculo real por hex). */
    var vizinhosDoImpacto: (Combatente) -> List<Pair<Combatente, Int>>
        get() = ataqueMagico.vizinhosDoImpacto
        set(v) { ataqueMagico.vizinhosDoImpacto = v }

    /**
     * Lote P9 — **FEIXE** (Jatos e Sopros). Regra uniforme no livro:
     * *"A cada turno, o operador faz um teste de DX−4 ou a perícia Ataque Inato para acertar. Este
     * ataque pode ser esquivado ou bloqueado, mas **não aparado**."*
     *
     * Três coisas que o separam do projétil e por isso ele não podia reusar `resolverArremessoProjetil`:
     *  1. A **penalidade na DX** (−4, ou −2 nos Sopros que saem da boca). Quem tem a perícia Ataque
     *     Inato rola o NH dela **sem** redutor — a penalidade é da DX improvisada, não do feixe.
     *  2. O alvo pode **BLOQUEAR**, o que o projétil não modela.
     *  3. **Aparar nunca vale** — é um jato, não tem lâmina para desviar.
     *
     * Exceção do livro, marcada só numa mágica: o **Jato de Ácido** *"pode ser desviado, mas não
     * aparado ou bloqueado"* → `feixeBloqueavel = false`.
     *
     * Deferidos honestos: a **projeção** (knockback) que vários Jatos causam reusaria o Empurrão,
     * mas exige decidir a direção na grade; e o *"dobro de dano em criaturas de fogo"* depende do
     * eixo de VULNERABILIDADE, que o A1 deixou registrado como ausente.
     */
    /**
     * Lote P9: bloqueio do NPC (escudo). O bestiário não tem campo de escudo, então isto é uma
     * aproximação honesta: só quem tem arma de corpo-a-corpo tenta bloquear, com NH/2+3 como no
     * aparar, e ninguém bloqueia atordoado/cego sem penalidade. Sem escudo no dado, inventar um
     * valor alto seria pior que aproximar.
     */
    private fun bloqueioNpc(npc: Combatente): Int {
        if (modoTesteNpc == ModoTesteNpc.BONECO) return 0
        if (Condicao.IMOBILIZADO in npc.condicoes || Condicao.DORMINDO in npc.condicoes ||
            Condicao.PARALISADO in npc.condicoes) return 0
        val melee = (npc.stats?.alcanceMetros ?: 1) <= 2
        if (!melee) return 0
        return ((npc.stats?.armaNh ?: 0) / 2 + 3 - penDefesaAtordoado(npc)).coerceAtLeast(0)
    }

    // ── Lote MEC-39 (P11): projétil CARREGADO por vários turnos (Magia p.12) ─────────────────────

    /**
     * Um projétil mágico "segurado" na mão entre turnos. [energiaAcumulada] cresce a cada Aumentar
     * (máx. [tetoPorTurno] = Aptidão Mágica por segundo), até [MAX_TURNOS_PROJETIL] segundos.
     */
    data class ProjetilCarregado(
        val nome: String,
        val ctx: ContextoConjuracao,
        val nhEfetivoCast: Int,
        var energiaAcumulada: Int,
        var turnosConcentrado: Int,
        val tetoPorTurno: Int,
    )

    var projetilCarregado: ProjetilCarregado? = null; private set

    /**
     * Cria o projétil e o SEGURA na mão (não arremessa) — Magia p.12. O turno inicial já é a 1ª
     * concentração. Enquanto segura, o operador não pode conjurar outra mágica (a guarda no topo de
     * [heroiConjurar] cuida disso). Consome a ação como uma Concentração.
     */
    fun heroiCarregarProjetil(
        ctx: ContextoConjuracao, custo: CustoEnergia, energiaInicial: Int, magiaNome: String, tetoPorTurno: Int,
    ): ResultadoConjuracaoCombate {
        inicioAcaoHeroi(); limparAvaliar(); limparApontar(); limparFinta()
        if (projetilCarregado != null || toqueCarregado != null) {
            val t = "✋ Você já está segurando uma mágica — não dá para criar outra."
            log += t; return ResultadoConjuracaoCombate(false, t)
        }
        val nhEf = MagicCasting.nhEfetivo(ctx)
        val custoTotal = MagicCasting.custoTotal(ctx, custo, energiaInicial.takeIf { custo.variavel })
        val rol = rolar3d6()
        val r = MagicCasting.resolver(nhEf.valor, rol, custoTotal, ctx.classe, rolagemChoqueRetorno3d = rolar3d6())
        heroi.pfAtual = (heroi.pfAtual - r.custoAPagar).coerceAtLeast(0)
        if (r.resultado == ResultadoOperacao.FALHA_CRITICA) {
            val sb = StringBuilder("💥 CHOQUE DE RETORNO ao criar $magiaNome! (NH ${nhEf.valor}, rolou $rol) ")
            aplicarChoqueRetorno(r.choqueRetorno, sb); verificarFim(); log += sb.toString().trim()
            return ResultadoConjuracaoCombate(false, sb.toString().trim())
        }
        if (r.resultado == ResultadoOperacao.FRACASSO) {
            val t = "✨ Você falha ao criar $magiaNome (NH ${nhEf.valor}, rolou $rol). Perde ${r.custoAPagar} PF."
            log += t; return ResultadoConjuracaoCombate(false, t)
        }
        val energia = energiaInicial.coerceIn(1, tetoPorTurno.coerceAtLeast(1))
        projetilCarregado = ProjetilCarregado(magiaNome, ctx, nhEf.valor, energia, 1, tetoPorTurno.coerceAtLeast(1))
        val t = "🔮 $magiaNome cresce na sua mão ($energia de energia; máx +${tetoPorTurno} por turno). " +
            "Arremesse, aumente (até ${MAX_TURNOS_PROJETIL}s) ou segure."
        log += t; return ResultadoConjuracaoCombate(true, t)
    }

    /** Aumenta o projétil segurado: +energia (sem teste), até o teto por turno e o máximo de segundos. */
    fun heroiAumentarProjetil(energiaExtra: Int): ResultadoConjuracaoCombate {
        inicioAcaoHeroi()
        val p = projetilCarregado ?: return ResultadoConjuracaoCombate(false, "Nenhum projétil na mão.")
        if (p.turnosConcentrado >= MAX_TURNOS_PROJETIL) {
            val t = "Você não pode passar de ${MAX_TURNOS_PROJETIL}s criando o projétil — arremesse ou segure."
            log += t; return ResultadoConjuracaoCombate(false, t)
        }
        val extra = energiaExtra.coerceIn(1, p.tetoPorTurno)
        p.energiaAcumulada += extra; p.turnosConcentrado += 1
        heroi.pfAtual = (heroi.pfAtual - extra).coerceAtLeast(0)
        val t = "🔮 ${p.nome} cresce mais (+$extra → ${p.energiaAcumulada} de energia; ${p.turnosConcentrado}/${MAX_TURNOS_PROJETIL}s)."
        log += t; verificarFim(); return ResultadoConjuracaoCombate(true, t)
    }

    /** Arremessa o projétil segurado num alvo. Consome o turno como um ataque à distância. */
    fun heroiArremessarProjetil(alvoId: String): ResultadoConjuracaoCombate {
        // Lote MEC-40 (P6): captura a mira (Apontar) ANTES de inicioAcaoHeroi limpar o apontar.
        // Precisão da magia + mira de vários turnos (+1 no 2º segundo, +2 no 3º+, MB p.364).
        val mirou = apontarAlvoId == alvoId
        val bonusPrec = if (mirou) {
            (projetilCarregado?.ctx?.mecanica?.precisao ?: 0) + (apontarStacks - 1).coerceIn(0, 2)
        } else 0
        inicioAcaoHeroi(); limparAvaliar(); limparApontar(); limparFinta()
        val p = projetilCarregado ?: return ResultadoConjuracaoCombate(false, "Nenhum projétil na mão.")
        val alvo = inimigos.firstOrNull { it.id == alvoId && it.vivo }
            ?: return ResultadoConjuracaoCombate(false, "Alvo inválido.").also { log += it.texto }
        projetilCarregado = null
        val sb = StringBuilder("🔥 Você arremessa ${p.nome} em ${alvo.nome} (${p.energiaAcumulada} de energia).")
        val dano = resolverArremessoProjetil(alvo, p.energiaAcumulada, p.ctx, sb, bonusPrec)
        verificarFim(); log += sb.toString().trim()
        return ResultadoConjuracaoCombate(true, sb.toString().trim(), dano)
    }

    /** Dissipa o projétil segurado (ação livre; Magia p.14). */
    fun dissiparProjetil() {
        val p = projetilCarregado ?: return
        projetilCarregado = null
        log += "✋ Você deixa ${p.nome} se dissipar sem arremessar."
    }

    /**
     * Lote MA-3d-2: descarrega a mágica de TOQUE carregada num alvo adjacente (Magia p.11-12). Ataque
     * corpo-a-corpo com a mão (aprox. DX); o alvo usa QUALQUER defesa ativa. Se se defende, a mágica
     * NÃO dispara e continua carregada (tenta de novo). Se acerta, descarrega: resistíveis fazem o 2º
     * teste (fresh, p.12) e o efeito é narrado. Consome o turno como um ataque.
     */
    fun heroiEntregarToque(alvoId: String): ResultadoConjuracaoCombate {
        inicioAcaoHeroi(); limparAvaliar(); limparApontar(); limparFinta()
        val t = toqueCarregado ?: return ResultadoConjuracaoCombate(false, "Nenhuma mágica carregada na mão.")
        val alvo = inimigos.firstOrNull { it.id == alvoId && it.vivo }
            ?: return ResultadoConjuracaoCombate(false, "Alvo inválido.").also { log += it.texto }
        val sb = StringBuilder("✋ Você tenta descarregar ${t.nome} em ${alvo.nome}")
        val rolAtk = rolar3d6()
        if (rolAtk > heroiPerfil.dx) {
            sb.append(" — mas erra o toque (DX ${heroiPerfil.dx}, rolou $rolAtk). A mágica continua carregada.")
            log += sb.toString(); return ResultadoConjuracaoCombate(false, sb.toString())
        }
        val (defTipo, defValor) = melhorDefesaNpc(alvo)
        if (npcSeDefendeu(defValor, rolar3d6())) {
            sb.append(" — ${alvo.nome} se defende (${defTipo.rotulo} $defValor). A mágica continua carregada.")
            log += sb.toString(); return ResultadoConjuracaoCombate(false, sb.toString())
        }
        // Acertou → descarrega.
        toqueCarregado = null
        sb.append(" e ACERTA!")
        val resistencia = t.ctx.classe.resistencia
        var resistiu = false
        if (resistencia != null) {
            // 2º teste do operador (Magia p.12) vs a resistência do alvo.
            val resist = resistenciaDoAlvo(alvo, resistencia)
            val rr = MagicCasting.resolverResistencia(t.nhEfetivoCast, rolar3d6(), resist, rolar3d6(), regraDo16 = true)
            resistiu = npcResistiu(rr.alvoResistiu) // MEC-31
            if (resistiu) sb.append(" ${alvo.nome} RESISTE (resistência $resist) — a mágica se dissipa.")
            else sb.append(" ${alvo.nome} não resiste (resistência $resist).")
        }
        // Lote MEC-21: APLICA a mecânica curada ao descarregar.
        //
        // Bug que isto corrige: a conjuração de TOQUE dá `return` cedo (só carrega a mão), então o
        // descarregar caía sempre em "Efeito narrado pelo Mestre" — NENHUMA magia de toque fazia
        // nada mecanicamente. Toque Candente, Morte Candente, Toque Chocante e Toque Congelante
        // acertavam e não produziam efeito. Isso também deixava o MEC-19 (fuga do gelo) inalcançável
        // em jogo: a paralisia nunca chegava a ser imposta.
        val mec = t.ctx.mecanica
        val energia = t.energiaInvestida.coerceAtLeast(1)
        if (!resistiu) when {
            // Lote MEC-22: mágica que fere A CADA TURNO não resolve o dano agora — ela fica ATIVA
            // no alvo e tica no avanço de turno, cobrando manutenção do operador.
            com.gurps.ficha.domain.magic.MagicMechanics.temTiquePorTurno(mec) -> {
                // A manutenção vem do custo da magia ("03/02" → 2). Sem número, metade do operar.
                val manut = (t.custoManutencao).coerceAtLeast(0)
                registrarMagiaAtiva(
                    nome = t.nome, operadorId = "heroi", alvoId = alvo.id, duracaoSeg = 1,
                    custoManutencaoSeg = manut, duracao = com.gurps.ficha.domain.magic.TipoDuracao.TEMPORARIA,
                    exigeConcentracao = true, // "O operador deve se concentrar enquanto mantém"
                    mecanica = mec,
                )
                sb.append(" ${alvo.nome} começa a queimar por dentro — a cada turno ele testa HT.")
            }
            com.gurps.ficha.domain.magic.MagicMechanics.temCuraEstruturada(mec) ->
                aplicarCuraMagica(alvo, energia, mec!!, sb)

            mec?.efeito == "condicao" ->
                imporCondicaoMagica(alvo, mec.condicao, sb,
                    com.gurps.ficha.domain.magic.MagicMechanics.duracaoCondicaoSeg(mec, energia),
                    escapeDaMecanica(mec, energia))

            com.gurps.ficha.domain.magic.MagicMechanics.temDanoEstruturado(mec) ->
                aplicarDanoMagico(alvo, energia, mec, sb)

            else -> sb.append(" Efeito narrado pelo Mestre.")
        }
        verificarFim(); log += sb.toString()
        return ResultadoConjuracaoCombate(true, sb.toString())
    }

    /**
     * Lote MA-3d-3: paga uma mágica de BLOQUEIO usada como defesa reativa (Magia p.12). O custo NÃO é
     * reduzido por NH alto (exceção da regra) e o ato INTERROMPE automaticamente qualquer conjuração em
     * andamento do operador. O sucesso do bloqueio (rolar ≤ NH) é resolvido no fluxo de defesa normal.
     */
    /**
     * Lote MEC-27 (C2, Magia p.12): *"O personagem pode operar apenas **uma mágica de Bloqueio por
     * turno**, independentemente de seu nível de habilidade."*
     *
     * Sem esta trava o herói podia conjurar um bloqueio mágico contra CADA ataque da rodada —
     * defesa mágica ilimitada, que é justamente o que a regra proíbe.
     */
    var bloqueioMagicoUsadoNoTurno: Boolean = false; private set

    fun aplicarBloqueioMagico(custoFP: Int, magiaNome: String) {
        bloqueioMagicoUsadoNoTurno = true
        heroi.pfAtual = (heroi.pfAtual - custoFP.coerceAtLeast(0)).coerceAtLeast(0)
        conjuracaoEmAndamento?.let {
            conjuracaoEmAndamento = null
            log += "  └ o bloqueio mágico interrompe sua conjuração de ${it.nome} (Magia p.12)."
        }
        log += "🔮 Você conjura $magiaNome como defesa (bloqueio mágico; custa $custoFP PF)."
    }

    /** Lote MA-3d-2: dissipa a mágica de toque carregada (ação livre; Magia p.14). */
    fun dissiparToque() {
        val t = toqueCarregado ?: return
        toqueCarregado = null
        log += "✋ Você dissipa ${t.nome} da mão (sem efeito)."
    }

    /**
     * Lote AR-1: aplica o dano de uma mágica ao alvo usando a `mecanica` CURADA do catálogo quando
     * houver (dado exato escalado por energia, tipo, regra de armadura e condição embutida); sem
     * mecânica, cai no padrão 1d × energia (contusão). Devolve o dano aplicado.
     */
    /**
     * Lote MEC-38 (P7): RD que protege o alvo contra uma mágica, respeitando `armadura`:
     *  - "ignora"          → 0 (nem natural nem vestida);
     *  - "ignora_vestida"  → só a RD NATURAL (Toque Candente: armadura não detém, pele sim);
     *  - senão             → RD total + buff.
     */
    private fun rdContraMagia(alvo: Combatente, mecanica: com.gurps.ficha.domain.magic.MagiaMecanica?): Int {
        val total = (alvo.stats?.rd ?: 0) + alvo.buffRd
        return when (mecanica?.armadura) {
            "ignora" -> 0
            "ignora_vestida" -> (alvo.stats?.rdNatural ?: 0) + alvo.buffRd
            else -> total
        }
    }


    /** Lote MAG-7: divisor de armadura preparado pela Mágica Penetrante para a PRÓXIMA magia de dano. */
    var divisorArmaduraPendente: Int = 0; private set

    private fun aplicarDanoMagico(
        alvo: Combatente,
        energia: Int,
        mecanica: com.gurps.ficha.domain.magic.MagiaMecanica?,
        sb: StringBuilder,
        distanciaM: Int = 0,
        brutoForcado: Int? = null,
    ): Int {
        // Lote MAG-7: consome o divisor preparado pela Mágica Penetrante (vale para o próximo dano).
        val div = divisorArmaduraPendente.coerceAtLeast(1)
        if (divisorArmaduraPendente > 0) divisorArmaduraPendente = 0
        return danoMagico.aplicar(alvo, energia, mecanica, sb, distanciaM, brutoForcado, divisorArmadura = div)
    }

    /**
     * Lote MEC-10: aplica CURA mágica — restaura PV de verdade (Cura Superficial 1 PV/energia até 3;
     * Cura Profunda 2 PV/energia até 8; Cura Superior todos os PV perdidos).
     *
     * Nunca estoura o PV máximo nem "cura" quem está inteiro. Se o alvo estava INCONSCIENTE por PV
     * negativo e a cura o traz de volta acima de 0, a inconsciência sai (ela é consequência do PV).
     */
    private fun aplicarCuraMagica(
        alvo: Combatente, energia: Int, mecanica: com.gurps.ficha.domain.magic.MagiaMecanica, sb: StringBuilder,
    ): Int {
        val perdidos = (alvo.pvMax - alvo.pvAtual).coerceAtLeast(0)
        val curado = com.gurps.ficha.domain.magic.MagicMechanics.pvCurados(mecanica, energia, perdidos)
        if (curado <= 0) {
            sb.append(" ${alvo.nome} já está com os PV cheios — nada a curar.")
            return 0
        }
        val antes = alvo.pvAtual
        alvo.pvAtual = (alvo.pvAtual + curado).coerceAtMost(alvo.pvMax)
        // A inconsciência por PV negativo deixa de valer quando os PV voltam ao positivo (MB p.380).
        if (antes <= 0 && alvo.pvAtual > 0 && Condicao.INCONSCIENTE in alvo.condicoes) {
            alvo.condicoes.remove(Condicao.INCONSCIENTE)
            sb.append(" ${alvo.nome} recobra a consciência!")
        }
        sb.append(" ✚ ${alvo.nome} recupera $curado PV (${alvo.pvAtual}/${alvo.pvMax}).")
        return curado
    }

    /** Lote COND-1: mapeia a condição da `mecanica` para a enum e a impõe no alvo (Sono, Cegueira, Medo, Paralisar…). */
    /** Lote MEC-19: monta o escape da condição a partir da mecânica curada, se houver. */
    private fun escapeDaMecanica(
        m: com.gurps.ficha.domain.magic.MagiaMecanica?,
        energiaInvestida: Int,
    ): EscapeCondicao? {
        val attr = m?.condicaoEscapeAtributo ?: return null
        return EscapeCondicao(
            condicao = Condicao.PARALISADO, // sobrescrito pela condição real em imporCondicaoMagica
            atributo = attr,
            penalidade = com.gurps.ficha.domain.magic.MagicMechanics.penalidadeEscapeCondicao(m, energiaInvestida),
            descricao = "o gelo",
        )
    }

    private fun imporCondicaoMagica(
        alvo: Combatente,
        condicaoStr: String?,
        sb: StringBuilder,
        /** Lote MEC-17: segundos de duração. 0 = sem prazo (sai pela regra própria da condição). */
        duracaoSeg: Int = 0,
        /** Lote MEC-19: se a condição sai por teste de atributo (gelo), em vez de por tempo. */
        escape: EscapeCondicao? = null,
    ) {
        // Lote MAG-5: usa o mapa canônico Condicao.deChave (inverso do removerCondicoes) — antes era
        // um `when` duplicado aqui. Agora "removido"/"caido"/"imobilizado"/"sangrando" também entram.
        val cond = Condicao.deChave(condicaoStr) ?: return
        alvo.condicoes.add(cond)
        // MEC-17: com prazo, registra o relógio. Se já havia um, fica o MAIOR — a segunda Cegar não
        // pode encurtar a primeira.
        if (duracaoSeg > 0) {
            val atual = alvo.condicoesTemporarias[cond] ?: 0
            alvo.condicoesTemporarias[cond] = maxOf(atual, duracaoSeg)
        }
        // MEC-19: condição da qual se ESCAPA por teste de atributo (o gelo do Toque Congelante).
        if (escape != null) {
            alvo.escapeCondicao = escape.copy(condicao = cond)
            sb.append(" ${alvo.nome} fica ${cond.rotulo.uppercase()} — só sai com um teste de " +
                "${escape.atributo}${if (escape.penalidade != 0) "${escape.penalidade}" else ""} " +
                "(${escape.descricao}).")
            return
        }
        sb.append(" ${alvo.nome} fica ${cond.rotulo.uppercase()}" +
            (if (duracaoSeg > 0) " por ${duracaoSeg}s." else "."))
    }

    /** Valor de resistência do alvo (MA-3a): atributo indicado + Abascanto embutido; combinadas pegam o maior. */
    private fun resistenciaDoAlvo(alvo: Combatente, resist: ResistenciaMagia): Int {
        fun valor(a: AtributoResistencia): Int = when (a) {
            AtributoResistencia.HT -> alvo.htEfetivo
            AtributoResistencia.IQ, AtributoResistencia.VONTADE, AtributoResistencia.VONTADE_OU_PERICIA ->
                alvo.stats?.iq ?: 10 // Vontade ~ IQ para o NPC (não há campo de Vontade separado)
            AtributoResistencia.DX -> alvo.dxEfetivo
            AtributoResistencia.ST -> alvo.stEfetivo
            else -> alvo.htEfetivo // MÁGICA/COMPOSTA/ESPECIAL → fallback HT (delegado ao Mestre)
        }
        val opcoes = (listOf(resist.atributo) + resist.alternativos).map(::valor)
        return (opcoes.max()) + resist.modificadorDefensor
    }

    /** Aplica o choque de retorno (Magia p.7) ao operador: dano/atordoamento conforme a tabela do MA-1. */
    private fun aplicarChoqueRetorno(efeito: EfeitoChoqueRetorno?, sb: StringBuilder) {
        if (efeito == null) return
        sb.append(efeito.rotulo)
        if (efeito.danoAoOperadorDadosD6 > 0) {
            val d = rolarDano("${efeito.danoAoOperadorDadosD6}d", random)
            InjuryRules.ferir(heroi, d, heroiPerfil.ht, random)
            sb.append(" (você sofre $d de dano)")
        }
        if (efeito.danoAoOperadorPontos > 0) {
            InjuryRules.ferir(heroi, efeito.danoAoOperadorPontos, heroiPerfil.ht, random)
            sb.append(" (você sofre ${efeito.danoAoOperadorPontos} de dano)")
        }
        if (efeito.atordoaOperador) {
            heroi.condicoes.add(Condicao.ATORDOADO)
            sb.append(" (você fica atordoado)")
        }
    }

    /** Apontar (MB p.364): mira → +Precisão (Acc) + mira contínua (+1/+2) + firmar arma de fogo (+1). */
    fun heroiApontar(alvoId: String, firmado: Boolean = false): String {
        inicioAcaoHeroi()
        // Mirar o MESMO alvo por turnos seguidos acumula a mira (+1 no 2º segundo, +2 no 3º+; MB p.364).
        if (apontarAlvoId == alvoId) apontarStacks++ else { apontarAlvoId = alvoId; apontarStacks = 1 }
        apontarFirmado = firmado
        limparAvaliar(); limparFinta()
        val miraExtra = (apontarStacks - 1).coerceIn(0, 2)
        val firmadoTxt = if (firmado) " +1 firmando" else ""
        val nome = inimigos.firstOrNull { it.id == alvoId }?.nome ?: "o alvo"
        val txt = "🎯 Você mira em $nome (+Precisão${if (miraExtra > 0) " +$miraExtra de mira contínua" else ""}$firmadoTxt no próximo tiro)."
        log += txt
        return txt
    }

    /**
     * Defesa Total (MB p.366): AUMENTADA (+2 numa defesa escolhida) ou DUPLA (tenta uma 2ª defesa
     * diferente se a 1ª falhar). O benefício vale até a PRÓXIMA ação do herói.
     */
    fun heroiDefesaTotal(modo: DefesaTotalModo, aumentadaEm: CombatResolver.TipoDefesa? = null): String {
        inicioAcaoHeroi()
        limparAvaliar(); limparApontar(); limparFinta()
        defesaTotalModo = modo
        defesaTotalAumentadaEm = if (modo == DefesaTotalModo.AUMENTADA) aumentadaEm else null
        val det = when (modo) {
            DefesaTotalModo.AUMENTADA -> "Aumentada — +2 em ${aumentadaEm?.rotulo ?: "uma defesa"}"
            DefesaTotalModo.DUPLA -> "Dupla — se a 1ª defesa falhar, você tenta uma 2ª diferente"
        }
        val txt = "🛡️🛡️ Você assume Defesa Total ($det)."
        log += txt
        return txt
    }

    /** Fogo de Retenção (MB p.409): arma de fogo CdT 5+ cobre a área; quem AVANÇAR leva uma rajada até o próximo turno. */
    fun heroiFogoRetencao(ataque: AtaqueHeroi): String {
        inicioAcaoHeroi()
        limparAvaliar(); limparApontar(); limparFinta()
        if (!ataque.aDistancia || ataque.cadenciaTiro < 5) {
            val t = "⚠️ Fogo de Retenção exige uma arma à distância com CdT 5+."
            log += t; return t
        }
        fogoRetencaoArma = ataque
        heroiSemDefesaAtiva = true // é um Ataque Total: sem defesa ativa até o próximo turno (MB p.366)
        val txt = "🔫 Você abre FOGO DE RETENÇÃO com ${ataque.rotulo.substringBefore(" (").trim()} — cobre a área; quem avançar leva rajada (sem defesa sua até o próximo turno)."
        log += txt
        return txt
    }

    /** Aguardar / Interromper Investida (MB p.392): firma uma arma perfurante corpo-a-corpo p/ golpear primeiro quem investir. */
    fun heroiAguardar(ataque: AtaqueHeroi): String {
        inicioAcaoHeroi()
        limparAvaliar(); limparApontar(); limparFinta()
        // Interromper Investida só com arma perfurante corpo-a-corpo firmada; senão é só um Aguardar genérico (narrativo).
        val txt = if (!ataque.aDistancia && ataque.tipo == DanoTipo.PERF) {
            aguardarInvestidaArma = ataque
            "⏳ Você AGUARDA firmando ${ataque.rotulo.substringBefore(" (").trim()} para receber a investida — golpeia primeiro quem avançar (+1 de dano por 2m percorridos)."
        } else {
            "⏳ Você aguarda, pronto para reagir. (Sem arma perfurante firmada: não há o bônus de Interromper Investida.)"
        }
        log += txt
        return txt
    }

    /**
     * Fintar (MB p.366): Disputa Rápida entre o NH do herói com a arma e a defesa do alvo (maior entre
     * armaNh e DX do NPC). Se o herói vencer, a MARGEM DE VITÓRIA é subtraída da defesa do alvo no próximo
     * golpe corpo-a-corpo contra ele. Exige arma corpo-a-corpo no alcance. Não causa dano nem desprepara.
     */
    fun heroiFintar(ataque: AtaqueHeroi, alvoId: String): String {
        inicioAcaoHeroi()
        limparAvaliar(); limparApontar(); limparFinta()
        val alvo = inimigos.firstOrNull { it.id == alvoId && it.vivo }
            ?: return "🤺 Alvo inválido ou já fora de combate.".also { log += it }
        val dist = encounter.distancia(alvo)
        if (ataque.aDistancia || dist > ataque.alcance) {
            val txt = "🤺 Não dá para fintar ${alvo.nome}: a finta exige uma arma corpo-a-corpo no alcance."
            log += txt; return txt
        }
        val rolHeroi = rolar3d6()
        val nhDefensor = maxOf(alvo.stats?.armaNh ?: 10, alvo.dxEfetivo)
        val rolDefensor = rolar3d6()
        val penalidade = fintaResultado(ataque.nh, rolHeroi, nhDefensor, rolDefensor)
        val tec = "[NH ${ataque.nh} rolou $rolHeroi vs defesa $nhDefensor rolou $rolDefensor]"
        val txt = if (penalidade > 0) {
            fintaAlvoId = alvoId; fintaPenalidade = penalidade
            "🤺 Você finta e engana ${alvo.nome}! A defesa dele cai −$penalidade no seu próximo golpe. $tec"
        } else {
            "🤺 Você finta ${alvo.nome}, mas ele não se deixa enganar. $tec"
        }
        log += txt
        return txt
    }

    /**
     * Agarrar (MB p.370): manobra de ataque com o NH de luta. Se acertar e o alvo não se defender, ele
     * fica AGARRADO — não pode atacar (gasta o turno tentando se soltar) e fica −4 na defesa. Sem dano.
     * Exige estar adjacente. (Base do Lote 386; Imobilizar/Estrangular ficam para lotes futuros.)
     */
    fun heroiAgarrar(ataque: AtaqueHeroi, alvoId: String): String {
        inicioAcaoHeroi()
        limparAvaliar(); limparApontar(); limparFinta()
        val alvo = inimigos.firstOrNull { it.id == alvoId && it.vivo }
            ?: return "🤼 Alvo inválido ou já fora de combate.".also { log += it }
        if (ataque.aDistancia || encounter.distancia(alvo) > 1) {
            val txt = "🤼 Não dá para agarrar ${alvo.nome}: é preciso estar adjacente (corpo-a-corpo)."
            log += txt; return txt
        }
        // Lote 424: mods situacionais valem também no agarrão (ataque do herói e defesa do alvo).
        val atk = CombatActions.resolverAtaque(nhBaseArma = ataque.nh, manobra = Manobra.ATAQUE, postura = heroi.postura,
            modsExtra = modsSituacionaisAtaque(heroi.id).map { CombatActions.ComponenteMod(it.motivo, it.valor) }, random = random)
        val (_, defValorBase) = melhorDefesaNpc(alvo)
        val defValor = (defValorBase + modSituacionalDefesa(alvo.id)).coerceAtLeast(0)
        val defSoma = rolar3d6()
        val acertou = atk.resultado == CombatActions.ResultadoAcerto.ACERTO
        val defendeu = acertou && atk.critico != CriticoRules.ResultadoCritico.DECISIVO && npcSeDefendeu(defValor, defSoma)
        val tec = "[NH ${ataque.nh} rolou ${atk.soma}]"
        val txt = if (acertou && !defendeu) {
            alvo.condicoes.add(Condicao.AGARRADO)
            "🤼 Você agarra ${alvo.nome}! Ele fica preso (−4 na defesa) e gasta o turno tentando se soltar. $tec"
        } else {
            "🤼 Você tenta agarrar ${alvo.nome}, mas ${if (!acertou) "erra o bote" else "ele escapa"}. $tec"
        }
        log += txt
        return txt
    }

    /** Lote 422 (MB p.370): o NPC agarra o HERÓI. Ataque defensável (a UI já pediu a defesa); sem dano. */
    private fun npcAgarraHeroi(npc: Combatente, defesaHeroi: DefesaHeroi?): AtaqueResultado {
        val nh = npc.stats?.armaNh ?: npc.dx
        // Lote 424: mods situacionais valem também no agarrão do NPC (ex.: areia nos olhos −4).
        val atk = CombatActions.resolverAtaque(nhBaseArma = nh, manobra = Manobra.ATAQUE, postura = npc.postura,
            modsExtra = modsSituacionaisAtaque(npc.id).map { CombatActions.ComponenteMod(it.motivo, it.valor) }, random = random)
        // Fallback de defesa passiva soma o mod situacional do herói (ex.: cobertura +2).
        val def = defesaHeroi ?: DefesaHeroi(CombatResolver.TipoDefesa.ESQUIVA,
            (heroiPerfil.esquiva + modSituacionalDefesa(heroi.id)).coerceAtLeast(0), rolar3d6())
        val acertou = atk.resultado == CombatActions.ResultadoAcerto.ACERTO
        val anulada = atk.critico == CriticoRules.ResultadoCritico.DECISIVO || heroiSemDefesaAtiva
        val defendeu = acertou && !anulada && CombatResolver.defesaBemSucedida(def.valorFinal, def.soma)
        registrarDefesaUsada(def.tipo)
        val tec = "[NH $nh rolou ${atk.soma}]"
        val pegou = acertou && !defendeu
        val txt = if (pegou) {
            heroi.condicoes.add(Condicao.AGARRADO)
            "🤼 ${npc.nome} agarra você! Você fica preso (−4 nas defesas), só ataca desarmado e precisa se Desvencilhar. $tec"
        } else "🤼 ${npc.nome} tenta agarrar você, mas ${if (!acertou) "erra o bote" else "você escapa"}. $tec"
        log += txt
        verificarFim()
        return AtaqueResultado(pegou, false, 0, false, txt)
    }

    /** Lote 422 (MB p.371): o NPC imobiliza um herói JÁ agarrado — Disputa Rápida de ST; sem defesa ativa. */
    private fun npcImobilizaHeroi(npc: Combatente): AtaqueResultado {
        if (Condicao.AGARRADO !in heroi.condicoes) {
            log += "• ${npc.nome} tenta prendê-lo, mas ainda não o agarrou."
            return AtaqueResultado(false, false, 0, false, log.last())
        }
        val stNpc = npc.stEfetivo
        val rn = rolar3d6(); val rh = rolar3d6()
        val txt = if (vencaDisputaRapida(stNpc, rn, heroiPerfil.st, rh)) {
            heroi.condicoes.add(Condicao.IMOBILIZADO)
            "🔒 ${npc.nome} IMOBILIZA você! Mal dá para se mexer — Desvencilhar-se fica muito mais difícil. [ST $stNpc rolou $rn vs ST ${heroiPerfil.st} rolou $rh]"
        } else "🤼 Você resiste e não é imobilizado por ${npc.nome}. [ST $stNpc rolou $rn vs ST ${heroiPerfil.st} rolou $rh]"
        log += txt
        verificarFim()
        return AtaqueResultado(false, false, 0, false, txt)
    }

    /** Lote PONTE-1 (AM p.69-70): o NPC aplica uma chave num membro do HERÓI já agarrado (Disputa de ST → dano cont). */
    private fun npcChaveMembroHeroi(npc: Combatente, perna: Boolean = false): AtaqueResultado {
        if (Condicao.AGARRADO !in heroi.condicoes) {
            log += "• ${npc.nome} tenta uma chave, mas ainda não o agarrou."
            return AtaqueResultado(false, false, 0, false, log.last())
        }
        val stNpc = npc.stEfetivo
        val resist = maxOf(heroiPerfil.st, heroiPerfil.ht) + (if (perna) 4 else 0)
        val rn = rolar3d6(); val rh = rolar3d6()
        val membro = if (perna) LocalAtaque.PERNA else LocalAtaque.BRACO
        val margem = (stNpc - rn) - (resist - rh)
        if (rn > stNpc || margem <= 0) {
            log += "🦾 Você resiste à chave de ${npc.nome}. [ST $stNpc rolou $rn vs $resist rolou $rh]"
            return AtaqueResultado(false, false, 0, false, log.last())
        }
        val dn = HitLocationRules.aplicarDano(heroi.pvMax, margem, DanoTipo.CONT, membro, heroiPerfil.rd)
        InjuryRules.ferir(heroi, dn.pvSubtrair, heroiPerfil.ht, random)
        log += "🦾 ${npc.nome} torce seu ${membro.rotulo}: margem $margem → ${dn.pvSubtrair} de dano. [ST $stNpc rolou $rn vs $resist rolou $rh]"
        verificarFim()
        return AtaqueResultado(true, false, dn.pvSubtrair, false, log.last())
    }

    /** Lote PONTE-1 (AM p.77): o NPC aplica um mata-leão no HERÓI já agarrado (+3 ST, contusão no pescoço + sufoco). */
    private fun npcMataLeaoHeroi(npc: Combatente): AtaqueResultado {
        if (Condicao.AGARRADO !in heroi.condicoes) {
            log += "• ${npc.nome} tenta o mata-leão, mas ainda não o agarrou."
            return AtaqueResultado(false, false, 0, false, log.last())
        }
        val stNpc = (npc.stEfetivo) + 3
        val resist = maxOf(heroiPerfil.st, heroiPerfil.ht)
        val rn = rolar3d6(); val rh = rolar3d6()
        val margem = (stNpc - rn) - (resist - rh)
        if (rn > stNpc || margem <= 0) {
            log += "🫷 Você resiste ao mata-leão de ${npc.nome}. [ST $stNpc rolou $rn vs $resist rolou $rh]"
            return AtaqueResultado(false, false, 0, false, log.last())
        }
        val danoBruto = (margem * 1.5).toInt().coerceAtLeast(1)
        val dn = HitLocationRules.aplicarDano(heroi.pvMax, danoBruto, DanoTipo.CONT, LocalAtaque.TORSO, heroiPerfil.rd)
        InjuryRules.ferir(heroi, dn.pvSubtrair, heroiPerfil.ht, random)
        log += "🫳 ${npc.nome} aplica um mata-leão em você: margem $margem → ${dn.pvSubtrair} de dano no pescoço. [ST $stNpc rolou $rn vs $resist rolou $rh]"
        if (dn.pvSubtrair > 0 && heroi.vivo) {
            heroi.condicoes.add(Condicao.SUFOCANDO)
            log += "  └ você começa a SUFOCAR — perde fôlego a cada turno até escapar (MB p.437)."
        }
        verificarFim()
        return AtaqueResultado(true, false, dn.pvSubtrair, false, log.last())
    }

    /**
     * Desvencilhar-se (MB p.371): o herói AGARRADO/IMOBILIZADO se solta vencendo uma Disputa Rápida de ST.
     * Bônus do captor (proxy "agarra com 2 mãos"): +5 se agarrado, +10 se imobilizado; −4 se o captor está
     * atordoado; libertação automática se o captor estiver fora de combate. Sucesso → solta-se.
     * Simplificações honestas: sem a regra "1×/10s" quando imobilizado e sem o passo de 1m (posição abstraída).
     */
    fun heroiDesvencilhar(): String {
        inicioAcaoHeroi()
        if (Condicao.AGARRADO !in heroi.condicoes && Condicao.IMOBILIZADO !in heroi.condicoes)
            return "Você não está preso.".also { log += it }
        val captores = inimigos.filter { it.vivo && encounter.distancia(it) <= 1 }
        val captor = captores.maxByOrNull { it.stEfetivo }
        if (captor == null) {
            heroi.condicoes.remove(Condicao.AGARRADO); heroi.condicoes.remove(Condicao.IMOBILIZADO)
            return "🤸 Não há mais ninguém te segurando — você se solta.".also { log += it }
        }
        val imob = Condicao.IMOBILIZADO in heroi.condicoes
        var bonusCaptor = if (imob) 10 else 5
        if (Condicao.ATORDOADO in captor.condicoes) bonusCaptor -= 4
        val stCaptor = (captor.stEfetivo) + bonusCaptor
        val rh = rolar3d6(); val rc = rolar3d6()
        val txt = if (vencaDisputaRapida(heroiPerfil.st, rh, stCaptor, rc)) {
            heroi.condicoes.remove(Condicao.AGARRADO); heroi.condicoes.remove(Condicao.IMOBILIZADO)
            "🤸 Você se DESVENCILHA de ${captor.nome} e se solta! [ST ${heroiPerfil.st} rolou $rh vs $stCaptor rolou $rc]"
        } else "🤼 Você forceja contra ${captor.nome}, mas continua preso. [ST ${heroiPerfil.st} rolou $rh vs $stCaptor rolou $rc]"
        log += txt
        return txt
    }

    /**
     * Derrubar (MB p.371): Disputa Rápida usando o maior entre ST e DX de cada um. Se o herói vence, o alvo
     * vai ao chão (caído/deitado). Exige estar adjacente. Útil principalmente contra um alvo já agarrado.
     */
    fun heroiDerrubar(alvoId: String): String {
        inicioAcaoHeroi()
        limparAvaliar(); limparApontar(); limparFinta()
        val alvo = inimigos.firstOrNull { it.id == alvoId && it.vivo }
            ?: return "🤼 Alvo inválido ou já fora de combate.".also { log += it }
        if (encounter.distancia(alvo) > 1) {
            val txt = "🤼 Você precisa estar adjacente para derrubar ${alvo.nome}."
            log += txt; return txt
        }
        val heroVal = maxOf(heroiPerfil.st, heroiPerfil.dx)
        val npcVal = maxOf(alvo.stEfetivo, alvo.dxEfetivo)
        val rh = rolar3d6(); val rn = rolar3d6()
        val tec = "[$heroVal rolou $rh vs $npcVal rolou $rn]"
        val txt = if (vencaDisputaRapida(heroVal, rh, npcVal, rn)) {
            alvo.condicoes.add(Condicao.CAIDO); alvo.postura = Postura.DEITADO
            "🤼 Você derruba ${alvo.nome} no chão! $tec"
        } else {
            "🤼 Você tenta derrubar ${alvo.nome}, mas ele se mantém de pé. $tec"
        }
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
        val legsAnteriores = heroiMoveSeguidos // capturado ANTES de inicioAcaoHeroi (que zera o contador)
        val direcaoAnterior = heroiMoveDirecao
        inicioAcaoHeroi()
        // Disparada (MB p.353): só acumula em LINHA RETA (mesma direção); mudar de direção ou qualquer ação não-Move recomeça.
        heroiMoveSeguidos = if (direcaoAnterior == afastar) legsAnteriores + 1 else 1
        heroiMoveDirecao = afastar
        // A partir do 2º Move consecutivo na mesma direção, +20% de Deslocamento (arredonda p/ baixo).
        val sprint = if (heroiMoveSeguidos >= 2) heroi.deslocamentoEfetivo / 5 else 0
        val deslocMax = (heroi.deslocamentoEfetivo + sprint).coerceAtLeast(1) // metade se cambaleante (MB p.380)
        val passo = metros.coerceIn(1, deslocMax)
        heroi.velocidadeAtual = passo // Lote 403: o herói em movimento é mais difícil de alvejar (Vel/Dist)
        val alvos = alvoId?.let { id -> inimigos.filter { it.id == id } } ?: inimigosVivos
        alvos.forEach { encounter.moverEmRelacaoAoHeroi(it.id, if (afastar) passo else -passo) }
        limparAvaliar(); limparApontar(); limparFinta()
        val nome = alvoId?.let { id -> inimigos.firstOrNull { it.id == id }?.nome } ?: "os inimigos"
        val txt = "🏃 Você ${if (afastar) "recua ${passo}m de" else "avança ${passo}m até"} $nome${if (sprint > 0) " (disparada +${sprint}m)" else ""}."
        log += txt
        return txt
    }

    /**
     * Lote TOK-4 (VTT 2D): manobra Mover TÁTICA — o herói moveu-se pelo GRID e o grid é a fonte da
     * verdade das novas distâncias (uma por NPC, exatas), em vez do move relativo por faixa.
     * Consome o turno com as mesmas regras do [heroiMove] (disparada em linha reta, velocidade p/
     * Vel/Dist, limpa Avaliar/Apontar/Finta); [metrosPercorridos] é o quanto o herói andou no grid
     * (clampado ao Deslocamento efetivo pelo CALLER via hexes alcançáveis).
     */
    fun heroiMoveTatico(novasDistancias: Map<String, Int>, metrosPercorridos: Int): String {
        val legsAnteriores = heroiMoveSeguidos
        inicioAcaoHeroi()
        // Disparada: no grid não rastreamos direção exata da corrida — mantém o contador se o
        // turno anterior também foi Move (aproximação honesta; a linha reta é validada pelo caller).
        heroiMoveSeguidos = legsAnteriores + 1
        // Achado da revisão TOK-4: quebra a cadeia de "mesma direção" do Mover de FAIXA — sem isso,
        // um Mover tático em qualquer direção contaria como linha reta pro heroiMove seguinte e
        // daria Disparada indevida (heroiMoveDirecao ficava obsoleto).
        heroiMoveDirecao = null
        val passo = metrosPercorridos.coerceAtLeast(1)
        heroi.velocidadeAtual = passo
        novasDistancias.forEach { (id, dist) ->
            if (inimigos.any { it.id == id && it.vivo }) encounter.definirDistancia(id, dist.coerceAtLeast(0))
        }
        limparAvaliar(); limparApontar(); limparFinta()
        val txt = "🏃 Você se desloca ${passo}m pelo campo."
        log += txt
        return txt
    }

    // ── Turno do NPC ───────────────────────────────────────────────────────

    /** Decide a intenção do NPC: usa o override do Narrador (B8) ou o cérebro tático (B6). */
    fun npcIntencao(npcId: String, override: NpcCombatBrain.IntencaoNpc? = null): NpcCombatBrain.IntencaoNpc {
        val npc = inimigos.first { it.id == npcId }
        // Lote TESTE-NPC: no sandbox congelado/boneco o NPC não age (mas em CONGELADO ainda defende
        // — a defesa é resolvida noutro ponto, `melhorDefesaNpc`/`esquivaNpc`).
        if (modoTesteNpc != ModoTesteNpc.NORMAL) {
            return NpcCombatBrain.IntencaoNpc(Manobra.NAO_FAZER_NADA,
                motivo = "modo de teste: ${modoTesteNpc.rotulo.lowercase()}")
        }
        return override ?: NpcCombatBrain.decidir(npc, encounter, alvoId = heroi.id, random = random)
    }

    /** true quando a intenção do NPC é um ataque que atinge o herói → a UI deve pedir "Defenda-se!". */
    fun intencaoAtacaHeroi(intencao: NpcCombatBrain.IntencaoNpc): Boolean =
        intencao.alvoId == heroi.id &&
            (intencao.manobra == Manobra.ATAQUE || intencao.manobra == Manobra.ATAQUE_TOTAL ||
                intencao.manobra == Manobra.MOVER_E_ATACAR || intencao.manobra == Manobra.AGARRAR ||
                // Lote MEC-11: conjurar um PROJÉTIL mágico no herói TAMBÉM é um ataque que ele pode
                // defender (Esquiva ou magia de Bloqueio, Magia p.12). A manobra é CONCENTRAR, que
                // não estava nesta lista — então as opções de defesa vinham VAZIAS e a defesa
                // interativa do MEC-8 NUNCA disparava: o motor esquivava sozinho e o jogador só via
                // o PV sumindo, sem card e sem escolha. Foi o que o usuário pegou no aparelho.
                (intencao.conjurar?.projetil == true))

    /**
     * Lote MA-7: um NPC CONJURADOR lança uma mágica ofensiva no herói. Usa o mesmo resolvedor
     * ([MagicCasting]) do herói. O NPC paga a própria fadiga; o herói se defende: **Projétil** → ESQUIVA
     * (nunca aparar, Magia p.12); **Comum de dano** → efeito direto no sucesso. Acertou → dano 1d ×
     * [NpcMagia.danoDados] com a RD do herói. Falha crítica → choque de retorno NO NPC. Resolução
     * SÍNCRONA (a esquiva do herói é rolada pelo motor) — a defesa interativa vs mágica de NPC é um
     * refinamento futuro.
     */
    fun npcConjurar(npcId: String, magia: NpcMagia, defesaHeroi: DefesaHeroi? = null): AtaqueResultado {
        val npc = inimigos.firstOrNull { it.id == npcId && it.vivo }
            ?: return AtaqueResultado(false, false, 0, false, "NPC inválido.")
        // Lote COND-1: silenciado não conjura (o ritual exige fala).
        if (Condicao.SILENCIADO in npc.condicoes) {
            log += "🤐 ${npc.nome} está silenciado e não consegue conjurar ${magia.nome}."
            return AtaqueResultado(false, false, 0, false, log.last())
        }
        npc.pfAtual = (npc.pfAtual - magia.custoFP).coerceAtLeast(0)
        val dist = encounter.distancia(npc)
        val classe = ClasseParseada(
            classes = setOf(if (magia.projetil) TipoClasseMagia.PROJETIL else TipoClasseMagia.COMUM),
            resistencia = null, original = ""
        )
        // Projétil cria na mão (sem distância); Comum sofre a penalidade de distância.
        // Lote A1-c: *"Suas habilidades psíquicas e mágicas afetam o mundo físico, mas todas as
        // jogadas sofrem uma penalidade de −3"* (MB, Insubstancialidade). É a contrapartida: o
        // espírito NÃO fica inofensivo, mas conjura pior.
        val penalInsub = if (npc.tipoCriatura == TipoCriatura.INSUBSTANCIAL) -3 else 0
        val nhEf = magia.nh + (if (magia.projetil) 0 else penalidadeDistancia(dist)) + penalInsub
        if (penalInsub != 0) log += "👻 ${npc.nome} conjura do plano insubstancial (−3 nas jogadas)."
        val rol = rolar3d6()
        val r = MagicCasting.resolver(nhEf, rol, magia.custoFP, classe, rolagemChoqueRetorno3d = rolar3d6())
        val sb = StringBuilder()
        when (r.resultado) {
            ResultadoOperacao.FALHA_CRITICA -> {
                sb.append("💥 ${npc.nome} tem um CHOQUE DE RETORNO ao conjurar ${magia.nome}! ")
                r.choqueRetorno?.let { ef ->
                    sb.append(ef.rotulo)
                    if (ef.danoAoOperadorDadosD6 > 0) { val d = rolarDano("${ef.danoAoOperadorDadosD6}d", random); InjuryRules.ferir(npc, d, npc.htEfetivo, random); sb.append(" (${npc.nome} sofre $d)") }
                    if (ef.danoAoOperadorPontos > 0) { InjuryRules.ferir(npc, ef.danoAoOperadorPontos, npc.htEfetivo, random); sb.append(" (${npc.nome} sofre ${ef.danoAoOperadorPontos})") }
                    if (ef.atordoaOperador) { npc.condicoes.add(Condicao.ATORDOADO); sb.append(" (${npc.nome} atordoado)") }
                }
            }
            ResultadoOperacao.FRACASSO -> sb.append("✨ ${npc.nome} falha ao conjurar ${magia.nome} (NH $nhEf, rolou $rol).")
            else -> {
                sb.append("🔮 ${npc.nome} conjura ${magia.nome} em você (NH $nhEf, rolou $rol).")
                var acertou = true
                if (magia.projetil) {
                    // Lote MEC-8: Projétil mágico só pode ser ESQUIVADO (Magia p.12). A defesa agora é
                    // INTERATIVA como contra arma: usa a rolagem que o JOGADOR fez no card "Defenda-se!"
                    // (defesaHeroi). Sem card (fallback), o motor rola — mas o controller sempre passa.
                    val esq = defesaHeroi?.valorFinal ?: heroiPerfil.esquiva
                    val rolDef = defesaHeroi?.soma ?: rolar3d6()
                    if (CombatResolver.defesaBemSucedida(esq, rolDef)) {
                        sb.append(" Você ESQUIVA (Esquiva $esq, rolou $rolDef)."); acertou = false
                    } else {
                        sb.append(" Você não esquiva (Esquiva $esq, rolou $rolDef).")
                    }
                }
                if (acertou) {
                    val bruto = rolarDano("${magia.danoDados.coerceAtLeast(1)}d", random)
                    val dn = HitLocationRules.aplicarDano(heroi.pvMax, bruto, DanoTipo.CONT, LocalAtaque.TORSO, heroiPerfil.rd)
                    InjuryRules.ferir(heroi, dn.pvSubtrair, heroiPerfil.ht, random)
                    sb.append(" ${magia.danoDados}d → ${dn.pvSubtrair} de dano em você" + (if (!heroi.vivo) " — você cai!" else "."))
                }
            }
        }
        verificarFim(); log += sb.toString().trim()
        return AtaqueResultado(true, false, 0, !heroi.vivo, sb.toString().trim())
    }

    /** Opções de defesa do herói para o card "Defenda-se!" (aplica recuo/Defesa Total/aparas extras). */
    fun opcoesDefesaHeroi(
        armaPronta: AtaqueHeroi? = null,
        contraAtaqueCorpoACorpo: Boolean = false, // Lote 389: Retirada só vale contra ataque corpo-a-corpo
        defesaTotalEm: CombatResolver.TipoDefesa? = null,
        contraArmaDeFogo: Boolean = false,
        atacanteAdjacente: Boolean = true, // Lote 390: aparar à distância só se o atacante estiver a 1m (default permissivo p/ corpo-a-corpo)
        ataqueComArma: Boolean = false, // Lote 391: o ataque do NPC usa arma? (−3 ao aparar com as mãos nuas)
        ambidestro: Boolean = false, // Lote 405: Ambidestria anula o −2 da apara com a mão inábil
        ataqueGdP: Boolean = false // Lote 407: ataque por ponta (GdP) → dispensa o −3 da apara desarmada (MB p.376)
    ): List<CombatResolver.OpcaoDefesa> {
        // Após um Ataque Total o herói não tem NENHUMA defesa ativa até o próximo turno (MB p.366).
        if (heroiSemDefesaAtiva) return emptyList()
        // Lote COND-1: dormindo/paralisado → INDEFESO (sem defesa ativa; o motor resolve com surpresa).
        if (Condicao.DORMINDO in heroi.condicoes || Condicao.PARALISADO in heroi.condicoes) return emptyList()
        val tipoAparar = armaPronta?.apararTipo ?: ApararTipo.NORMAL
        val ranged = armaPronta?.aDistancia == true
        // Aparar um ataque À DISTÂNCIA só se o atacante estiver adjacente (≤1m): apara-se a ARMA, não o projétil
        // (MB p.376). Contra corpo-a-corpo, sempre vale. Bloqueio (escudo) continua valendo contra tiro.
        val podeApararPeloAlcance = contraAtaqueCorpoACorpo || atacanteAdjacente
        // Aparar indisponível: arma à distância, arma "Não", desbalanceada já usada para atacar, ou Mover e
        // Atacar no turno anterior (que permite só Esquiva/Bloqueio, MB p.367/270).
        val podeAparar = !ranged && podeApararPeloAlcance && tipoAparar != ApararTipo.NAO && !heroiSemAparar &&
            !(tipoAparar == ApararTipo.DESBALANCEADA && atacouDesbalanceada)
        // Aparar Desarmado (Lote 391/407, MB p.376): aparar uma ARMA com as mãos nuas sofre −3, salvo Caratê/Judô
        // OU se o ataque é por ponta (GdP). GdP é inferido do dano PERF (perfuração = sempre por ponta).
        val penAparaDesarmada = if (armaPronta?.desarmado == true && ataqueComArma &&
            armaPronta.aparaMarcial != true && !ataqueGdP) 3 else 0
        // BD do escudo (MB p.375): só vale com o escudo PREPARADO — uma mão livre (arma de 2 mãos não tem) —
        // e NÃO contra armas de fogo. Quando não vale, removemos o BD que já vem embutido nas defesas da ficha.
        val semMaoParaEscudo = armaPronta?.duasMaos == true
        val bdRemovido = if (!semMaoParaEscudo && !contraArmaDeFogo) 0 else heroiPerfil.bonusEscudo
        // Cambaleante (MB p.380): com < 1/3 do PV, a Vel.Básica cai à metade → a Esquiva também.
        val reducaoCambaleante = if (heroi.cambaleante)
            floor(heroi.velocidadeBasica).toInt() - floor(heroi.velocidadeBasica / 2).toInt() else 0
        // Atordoado (Lote 393, MB p.364/"Fazer Nada"): TODAS as defesas ativas sofrem −4 enquanto atordoado.
        val penAtordoado = if (Condicao.ATORDOADO in heroi.condicoes) 4 else 0
        // Agarrado/Imobilizado (Lote 422, MB p.370/371): herói preso sofre −4 nas defesas (espelha o NPC agarrado).
        val penPreso = if (Condicao.AGARRADO in heroi.condicoes || Condicao.IMOBILIZADO in heroi.condicoes) 4 else 0
        // Cego (Lote COND-1, MB p.394): −4 em todas as defesas (não vê o golpe chegar).
        val penCego = if (Condicao.CEGO in heroi.condicoes) 4 else 0
        // Retirada (Lote 389, MB p.377): só vs corpo-a-corpo, 1×/turno e não atordoado (postura sentado/ajoelhado
        // = limitação futura). O passo de recuo em si fica abstraído (o herói está sempre engajado no tracker).
        // Ataque Dedicado (Lote PONTE-4, AM p98): −2 em TODAS as defesas e proíbe a Retirada, no turno seguinte ao ataque.
        val penDedicado = heroiPenalidadeDefesaDedicado
        // Modificadores situacionais do Narrador (Lote 424): ex.: cobertura → +2 nas defesas do herói.
        val modSitDef = modSituacionalDefesa(heroi.id)
        // Ataque Defensivo (Lote PONTE-4, AM p98): +1 numa defesa escolhida — só Aparar ou Bloquear (não Esquiva).
        val bonusDefApara = if (heroiBonusDefesaDefensivo == CombatResolver.TipoDefesa.APARA) 1 else 0
        val bonusDefBloqueio = if (heroiBonusDefesaDefensivo == CombatResolver.TipoDefesa.BLOQUEIO) 1 else 0
        val permitirRecuo = contraAtaqueCorpoACorpo && !heroi.defesasUsadas.retracaoUsada &&
            Condicao.ATORDOADO !in heroi.condicoes && !heroiSemRetirada
        // Esquiva e Queda (Lote 404, MB p.377): só contra ataque À DISTÂNCIA, se ainda não está deitado nem atordoado.
        val permitirJogarSeAoChao = !contraAtaqueCorpoACorpo && heroi.postura != Postura.DEITADO &&
            Condicao.ATORDOADO !in heroi.condicoes
        return CombatResolver.opcoesDefesa(
            esquivaBase = heroiPerfil.esquiva - bdRemovido - reducaoCambaleante - penAtordoado - penPreso - penCego - penDedicado + modSitDef,
            aparaBase = if (podeAparar) heroiPerfil.apara?.let { it - bdRemovido - penAparaDesarmada - penAtordoado - penPreso - penCego - penDedicado + bonusDefApara + modSitDef } else null,
            bloqueioBase = heroiPerfil.bloqueio?.let { it - bdRemovido - penAtordoado - penPreso - penCego - penDedicado + bonusDefBloqueio + modSitDef },
            defesasUsadas = heroi.defesasUsadas,
            defesaTotalEm = defesaTotalEm ?: defesaTotalAumentadaEm, // Lote 388: +2 da Defesa Total (Aumentada)
            esgrima = tipoAparar == ApararTipo.ESGRIMA,
            permitirRecuo = permitirRecuo,
            permitirJogarSeAoChao = permitirJogarSeAoChao,
            ambidestro = ambidestro,
            // Esquiva Acrobática (Lote 414): só se o herói tem Acrobacia e não está atordoado.
            permitirAcrobatica = heroiPerfil.acrobacia != null && Condicao.ATORDOADO !in heroi.condicoes
        )
    }

    /**
     * Resolve o turno do NPC [npcId] com a [intencao] já decidida. Se for ataque ao herói, exige
     * [defesaHeroi] (escolha + rolagem feitas na UI). Movimentos atualizam a faixa de distância.
     */
    fun npcResolve(
        npcId: String,
        intencao: NpcCombatBrain.IntencaoNpc,
        defesaHeroi: DefesaHeroi? = null,
        defesaSecundaria: DefesaHeroi? = null // Lote 388: Defesa Total (Dupla) — 2ª defesa se a 1ª falhar
    ): AtaqueResultado {
        val npc = inimigos.firstOrNull { it.id == npcId && it.vivo }
            ?: return AtaqueResultado(false, false, 0, false, "NPC fora de combate.")
        npc.velocidadeAtual = 0 // Lote 403: só Mover redefine a velocidade do NPC (penalidade de Vel/Dist ao ser alvejado)

        // Agarrado/Imobilizado (Lotes 386/411, MB p.371): o NPC preso gasta o turno tentando se desvencilhar
        // (Disputa Rápida do maior entre ST/DX; imobilizar é só ST e dá −3 ao NPC). Em ambos os casos, não ataca.
        if (Condicao.AGARRADO in npc.condicoes || Condicao.IMOBILIZADO in npc.condicoes) {
            // Estrangulado (Lote 412, MB p.437): perde fôlego a cada turno enquanto preso (proxy de PF = −1 PV).
            if (Condicao.SUFOCANDO in npc.condicoes && npc.vivo) {
                InjuryRules.ferir(npc, 1, npc.htEfetivo, random)
                log += "😮‍💨 ${npc.nome} sufoca e perde fôlego (−1)."
            }
            val imob = Condicao.IMOBILIZADO in npc.condicoes
            val nv = (if (imob) (npc.stEfetivo) - 3 else maxOf(npc.stEfetivo, npc.dxEfetivoOuProprio))
            val hv = maxOf(heroiPerfil.st, heroiPerfil.dx)
            val rn = rolar3d6(); val rh = rolar3d6()
            val o = if (imob) "imobilização" else "agarrão"
            if (vencaDisputaRapida(nv, rn, hv, rh)) {
                npc.condicoes.remove(Condicao.AGARRADO); npc.condicoes.remove(Condicao.IMOBILIZADO); npc.condicoes.remove(Condicao.SUFOCANDO)
                log += "🤼 ${npc.nome} se desvencilha e se solta da $o! [$nv rolou $rn vs $hv rolou $rh]"
            } else {
                log += "🤼 ${npc.nome} forceja, mas continua preso na $o. [$nv rolou $rn vs $hv rolou $rh]"
            }
            verificarFim()
            return AtaqueResultado(false, false, 0, false, log.last())
        }

        // Fogo de Retenção (Lote 396, MB p.409): a zona coberta alveja quem AVANÇA, antes mesmo de o NPC agir.
        fogoRetencaoArma?.let { arma ->
            val avanca = (intencao.manobra == Manobra.MOVER && !intencao.recuar) || intencao.manobra == Manobra.MOVER_E_ATACAR
            if (avanca) {
                log += "🔫 Fogo de retenção: ${npc.nome} avança na zona coberta e é alvejado!"
                resolverGolpeHeroi(arma, npc, Manobra.ATAQUE, LocalAtaque.TORSO, AtaqueTotalModo.DETERMINADO)
                if (!npc.vivo) { verificarFim(); return AtaqueResultado(true, false, 0, true, log.last()) }
            }
        }
        // Aguardar / Interromper Investida (Lote 399, MB p.392): com a arma perfurante firmada, o herói golpeia
        // PRIMEIRO quem investe (+1 de dano por 2m percorridos), antes de o NPC atacar.
        aguardarInvestidaArma?.let { arma ->
            val avanca = (intencao.manobra == Manobra.MOVER && !intencao.recuar) || intencao.manobra == Manobra.MOVER_E_ATACAR
            if (avanca) {
                val metros = npc.deslocamentoEfetivo.coerceAtMost(encounter.distancia(npc)).coerceAtLeast(0)
                bonusInvestidaPendente = metros / 2
                log += "🛡️→🗡️ Investida! ${npc.nome} avança e você golpeia primeiro com a arma firmada (+$bonusInvestidaPendente de dano por ${metros}m)."
                val rInv = resolverGolpeHeroi(arma, npc, Manobra.ATAQUE, LocalAtaque.TORSO, AtaqueTotalModo.DETERMINADO)
                bonusInvestidaPendente = 0
                aguardarInvestidaArma = null
                if (!npc.vivo) { verificarFim(); return AtaqueResultado(true, false, 0, true, log.last()) }
                // Lote TOK-5b — Manter um Oponente à Distância (AM p.101, HexManterADistancia do
                // HEX-6): se o golpe de interrupção CAUSOU DANO, a arma está no caminho e o NPC
                // precisa vencer para continuar avançando:
                //  - arma NÃO-perfurante → Disputa Rápida de ST vs o herói; perdeu → o avanço PARA.
                //  - arma de estocada perfurante (cravada) → teste de Vontade−3 do NPC; falhou → PARA.
                //    (Simplificação honesta: sem o dano máximo adicional/arma presa do avanço forçado.)
                if (rInv.danoAplicado > 0 && posicaoBridge != null) {
                    val tipo = if (arma.tipo == DanoTipo.PERF)
                        com.gurps.ficha.domain.combat.hex.HexManterADistancia.TipoInterrupcao.APAROU_COM_ESTOCADA_PERFURANTE
                    else com.gurps.ficha.domain.combat.hex.HexManterADistancia.TipoInterrupcao.APAROU_COM_DANO_NAO_ESTOCADA
                    val regra = com.gurps.ficha.domain.combat.hex.HexManterADistancia.avaliar(tipo)
                    val passou = if (regra.disputaSTNecessaria) {
                        val stN = npc.stEfetivo; val rn = rolar3d6()
                        val stH = heroiPerfil.st; val rh = rolar3d6()
                        val ok = vencaDisputaRapida(stN, rn, stH, rh)
                        log += "  └ arma no caminho (AM p.101): Disputa de ST — ${npc.nome} $stN rolou $rn vs você $stH rolou $rh → ${if (ok) "ele passa" else "ele NÃO passa"}."
                        ok
                    } else if (regra.testeVontadeMod != null) {
                        // Lote P3-1: `iqEfetivo` no lugar do IQ cru do bestiário — a Vontade do NPC
                        // deriva do IQ, então Tolice (−IQ) e Sabedoria (+IQ) mordem aqui de fato.
                        val vontade = (if (npc.stats != null) npc.iqEfetivo else 8 + npc.buffIq) +
                            regra.testeVontadeMod!!
                        val rv = rolar3d6(); val ok = rv <= vontade
                        log += "  └ a lâmina está cravada (AM p.101): Vontade−3 de ${npc.nome} ($vontade, rolou $rv) → ${if (ok) "ele avança mesmo assim" else "ele recua da arma"}."
                        ok
                    } else regra.podeAvancar
                    if (!passou) {
                        log += "  └ ${npc.nome} é MANTIDO À DISTÂNCIA — o avanço para aqui."
                        verificarFim()
                        return AtaqueResultado(false, false, 0, false, log.last())
                    }
                }
            }
        }

        when (intencao.manobra) {
            Manobra.MOVER -> {
                val passo = npc.deslocamentoEfetivo.coerceAtLeast(1) // metade se cambaleante (MB p.380)
                npc.velocidadeAtual = passo // Lote 403: NPC em movimento é mais difícil de alvejar (Vel/Dist)
                // Lote TOK-5b: com a grade ativa, quem decide PRA ONDE é a IA POSICIONAL
                // (HexTaticaNpc — flanquear/kite/recuar); a distância nova vem da posição real.
                val distGrade = posicaoBridge?.moverNpcNaGrade(npc.id, intencao)
                if (distGrade != null) {
                    encounter.definirDistancia(npc.id, distGrade)
                    log += "🏃 ${npc.nome} ${if (intencao.recuar) "recua" else "se move"} pelo campo (${intencao.motivo})."
                } else if (intencao.recuar) {
                    encounter.moverEmRelacaoAoHeroi(npc.id, passo)
                    log += "🏃 ${npc.nome} recua ${passo}m (${intencao.motivo})."
                } else {
                    encounter.moverEmRelacaoAoHeroi(npc.id, -passo)
                    log += "🏃 ${npc.nome} avança ${passo}m (${intencao.motivo})."
                }
                if (intencao.recuar && encounter.distancia(npc) >= FUGA_METROS) {
                    npc.condicoes.add(Condicao.INCONSCIENTE) // sai do encontro (fugiu)
                    log += "  └ ${npc.nome} fugiu do combate."
                }
                verificarFim()
                return AtaqueResultado(false, false, 0, false, log.last())
            }
            Manobra.MOVER_E_ATACAR -> {
                // Lote TOK-5b: com a grade, o NPC avança pelo campo (podendo FLANQUEAR); se não
                // alcançar o herói neste turno, o avanço consome a manobra (fiel ao Avançar-e-Atacar).
                val distGrade = posicaoBridge?.moverNpcNaGrade(npc.id, intencao)
                if (distGrade != null) {
                    encounter.definirDistancia(npc.id, distGrade)
                    val alcanceNpc = (npc.stats?.alcanceMetros ?: 1).coerceAtLeast(1)
                    if (distGrade > alcanceNpc) {
                        log += "🏃 ${npc.nome} avança pelo campo, mas não alcança você neste turno."
                        verificarFim()
                        return AtaqueResultado(false, false, 0, false, log.last())
                    }
                } else {
                    encounter.definirDistancia(npc.id, 1) // modo faixas: chega ao corpo-a-corpo
                }
            }
            else -> { /* ATAQUE / ATAQUE_TOTAL: resolve abaixo */ }
        }

        // Lote 422 (MB p.370/371): NPC AGARRA o herói (defensável — a UI já pediu a defesa) ou o IMOBILIZA
        // (Disputa de ST, exige tê-lo agarrado). Espelho de heroiAgarrar/heroiImobilizar.
        if (intencao.manobra == Manobra.AGARRAR && intencao.alvoId == heroi.id) return npcAgarraHeroi(npc, defesaHeroi)
        if (intencao.manobra == Manobra.IMOBILIZAR && intencao.alvoId == heroi.id) return npcImobilizaHeroi(npc)
        if (intencao.manobra == Manobra.CHAVE_MEMBRO && intencao.alvoId == heroi.id) return npcChaveMembroHeroi(npc)
        if (intencao.manobra == Manobra.MATA_LEAO && intencao.alvoId == heroi.id) return npcMataLeaoHeroi(npc)

        // Lote MEC-11: conjuração NÃO se resolve aqui (é `npcConjurar`, com o resolvedor de magia).
        // Guarda explícita porque `intencaoAtacaHeroi` passou a aceitar Projétil mágico — sem isto,
        // uma intenção de conjurar cairia no fluxo de ataque com ARMA.
        if (intencao.conjurar != null || !intencaoAtacaHeroi(intencao)) {
            log += "• ${npc.nome}: ${intencao.manobra.rotulo} (${intencao.motivo})."
            return AtaqueResultado(false, false, 0, false, log.last())
        }

        val stats = npc.stats ?: return AtaqueResultado(false, false, 0, false, "${npc.nome} sem stats de ataque.")
        // Lote A1-c: a regra da insubstancialidade é SIMÉTRICA — *"Da mesma maneira, seus ataques
        // físicos e de energia não afetam oponentes físicos"* (MB). Um espírito não soca ninguém;
        // ele precisa de Solidificar (que o motor ainda não modela) ou de magia. Sem esta metade, o
        // fantasma seria invulnerável E letal, o que não é regra nenhuma.
        if (npc.tipoCriatura == TipoCriatura.INSUBSTANCIAL && !npc.afetaInsubstancial) {
            val t = "👻 ${npc.nome} é insubstancial — o golpe atravessa você sem causar dano."
            log += t
            return AtaqueResultado(false, false, 0, false, t)
        }
        // Lote TOK-5a: o NPC vira de frente pro herói ao atacar (facing é livre no próprio turno);
        // e se MESMO ASSIM o golpe pega o herói de FLANCO/COSTAS (posição real na grade), a defesa
        // do herói sofre (o ajuste das OPÇÕES é feito pelo controller via HexRegrasFacing; aqui só
        // a anulação por COSTAS entra no resolverTroca).
        posicaoBridge?.aoAtacar(npc.id, heroi.id)
        val facingHeroiAlvo = posicaoBridge?.facingDoAtaque(npc.id, heroi.id)
        val costasHeroi = facingHeroiAlvo == com.gurps.ficha.domain.combat.hex.Facing.COSTAS
        if (costasHeroi) log += "⚠️ ${npc.nome} ataca você pelas COSTAS — defesa ANULADA (MB p.374)!"
        val modsNpc: List<CombatActions.ComponenteMod> = buildList {
            // Choque (Lote 382, MB p.419): PV perdidos no turno anterior penalizam o acerto do NPC.
            InjuryRules.penalidadeChoque(npc.choquePendente, npc.pvMax).let {
                if (it != 0) add(CombatActions.ComponenteMod("choque", it))
            }
            // Modificadores situacionais do Narrador (Lote 424): ex.: areia nos olhos → −4 no ataque do NPC.
            modsSituacionaisAtaque(npc.id).forEach { add(CombatActions.ComponenteMod(it.motivo, it.valor)) }
            // Cego (Lote COND-1, MB p.394): −4 para acertar (não vê o herói).
            if (Condicao.CEGO in npc.condicoes) add(CombatActions.ComponenteMod("cego", -4))
            // Lote MEC-37 (P4): rider de ofuscamento.
            if (npc.penalidadeCombateTemp > 0) add(CombatActions.ComponenteMod("ofuscado", -npc.penalidadeCombateTemp))
            // MEC-2 — Nublar (−1 a −5): a magia no HERÓI penaliza quem tenta acertá-lo.
            if (heroi.buffPenalidadeAtacantes > 0)
                add(CombatActions.ComponenteMod("alvo nublado", -heroi.buffPenalidadeAtacantes))
            if (intencao.aDistancia) {
                // Atirando NO herói: soma o MT do herói (alvo) ao acerto (MB p.549).
                if (heroiPerfil.modificadorTamanho != 0)
                    add(CombatActions.ComponenteMod("tamanho do alvo (MT)", heroiPerfil.modificadorTamanho))
                // Velocidade e Distância (Lote 403, MB p.550): o herói em movimento é mais difícil de alvejar.
                val distH = encounter.distancia(npc)
                val pen = penalidadeDistancia(distH + heroi.velocidadeAtual)
                if (pen != 0) add(CombatActions.ComponenteMod(
                    if (heroi.velocidadeAtual > 0) "Vel/Dist (${distH}m+${heroi.velocidadeAtual}m/s)" else "distância", pen))
                // Agachar (Lote 416, MB p.368): o herói agachado/deitado é um alvo menor à distância.
                penalidadePosturaAlvejado(heroi.postura).let { if (it != 0) add(CombatActions.ComponenteMod("herói ${heroi.postura.rotulo}", it)) }
            }
        }
        val atk = CombatActions.resolverAtaque(
            nhBaseArma = stats.armaNh, manobra = intencao.manobra, postura = npc.postura,
            local = intencao.local, visibilidade = Visibilidade.NORMAL,
            aDistancia = intencao.aDistancia, modsExtra = modsNpc, random = random
        )
        // Sem escolha de defesa (herói atordoado/sem opção) → só Esquiva passiva da ficha (+mod situacional, Lote 424).
        // TOK-5a: a esquiva PASSIVA também sofre o −2 de FLANCO (as opções do card já vêm ajustadas
        // pelo controller via HexRegrasFacing — aqui só o caminho sem card, para não aplicar 2×).
        val penFlancoPassiva = if (defesaHeroi == null &&
            facingHeroiAlvo == com.gurps.ficha.domain.combat.hex.Facing.FLANCO) 2 else 0
        var def = defesaHeroi ?: DefesaHeroi(CombatResolver.TipoDefesa.ESQUIVA,
            (heroiPerfil.esquiva + modSituacionalDefesa(heroi.id) - penFlancoPassiva).coerceAtLeast(0), rolar3d6())
        // Esquiva Acrobática (Lote 414, MB p.377): testa Acrobacia ANTES da esquiva → +2 (sucesso) / −2 (falha).
        if (def.acrobatica && heroiPerfil.acrobacia != null) {
            val rolAcro = rolar3d6(); val ok = rolAcro <= heroiPerfil.acrobacia!!
            def = def.copy(valorFinal = (def.valorFinal + if (ok) 2 else -2).coerceAtLeast(0))
            log += "🤸 Esquiva acrobática: Acrobacia ${heroiPerfil.acrobacia}, rolou $rolAcro → ${if (ok) "+2" else "−2"} (esquiva ${def.valorFinal})."
        }
        var danoBasicoNpc = rolarDano(stats.armaDano, random) + bonusDanoForte(intencao.manobra, AtaqueTotalModo.FORTE, stats.armaDano, intencao.aDistancia)
        var rdHeroiAlvo = rdComDivisor(heroiPerfil.rd, divisorArmadura(stats.armaDano)) // Lote 413: divisor de armadura
        var forcaGraveNpc = false
        // Golpe Fulminante do NPC (Lote 384, MB p.558).
        if (atk.critico == CriticoRules.ResultadoCritico.DECISIVO) {
            val gf = aplicarGolpeFulminante(danoBasicoNpc, rdHeroiAlvo, stats.armaDano)
            danoBasicoNpc = gf.dano; rdHeroiAlvo = gf.rd; forcaGraveNpc = gf.grave
            log += "  ⭐ Golpe Fulminante de ${npc.nome} — ${gf.nota}"
        }
        // Ataque Total no turno anterior ANULA a defesa do herói até o próximo turno (MB p.366).
        if (heroiSemDefesaAtiva) log += "🛡️ Você está sem defesa ativa (Ataque Total) — resta torcer pelo erro do oponente!"

        // Defesa Dupla (Lote 388, MB p.366): se a 1ª defesa falhou e o ataque NÃO foi anulado por golpe
        // decisivo, o herói tenta automaticamente a 2ª defesa diferente já preparada pelo controller.
        if (defesaSecundaria != null && atk.resultado != CombatActions.ResultadoAcerto.FALHA &&
            atk.critico != CriticoRules.ResultadoCritico.DECISIVO &&
            !CombatResolver.defesaBemSucedida(def.valorFinal, def.soma)) {
            log += "🛡️🛡️ Defesa Dupla: ${def.tipo.rotulo} ${def.valorFinal} falhou (rolou ${def.soma}) — você tenta ${defesaSecundaria.tipo.rotulo}!"
            registrarDefesaUsada(def.tipo) // a 1ª defesa também conta como usada neste turno
            def = defesaSecundaria
        }

        val troca = CombatResolver.resolverTroca(
            defensor = heroi, htDefensor = heroiPerfil.ht, ataque = atk,
            defesaTipo = def.tipo, defesaValorFinal = def.valorFinal, defesaSoma = def.soma,
            surpresa = heroiSemDefesaAtiva || costasHeroi, // TOK-5a: costas anula (MB p.374)
            danoBaseRolado = danoBasicoNpc, danoTipo = tipoDano(stats.armaTipo),
            local = intencao.local, rdLocal = rdHeroiAlvo, randomFerimento = random, forcarFerimentoGrave = forcaGraveNpc
        )
        // marca a defesa usada (bloqueio/recuo 1×/turno; aparas extras cumulativas)
        registrarDefesaUsada(def.tipo)
        // Retirada (Lote 389, MB p.377): recuar é 1×/turno; o bônus já está no valorFinal da defesa.
        if (def.recuo) {
            heroi.defesasUsadas = heroi.defesasUsadas.copy(retracaoUsada = true)
            log += "  └ você recua um passo (Retirada, defesa ${def.valorFinal})."
            // Lote TOK-5a: o passo de Retirada é REAL na grade — recua 1 hex na direção oposta ao
            // atacante e as novas distâncias (a TODOS os NPCs) entram no encounter. Se o hex atrás
            // está ocupado/fora da grade, o recuo fica só narrativo (bônus mantido — MB abstrai).
            posicaoBridge?.recuarUmHex(heroi.id, npc.id)?.let { novas ->
                novas.forEach { (id, d) ->
                    if (inimigos.any { it.id == id && it.vivo }) encounter.definirDistancia(id, d.coerceAtLeast(0))
                }
                log += "  └ (o recuo abre 1m no campo)"
            }
        }
        // Esquiva e Queda (Lote 404, MB p.377): após a defesa contra o tiro, o herói termina DEITADO.
        if (def.jogarSeAoChao && heroi.postura != Postura.DEITADO) {
            heroi.postura = Postura.DEITADO
            log += "  └ você se joga ao chão (+3 na esquiva vs tiro) e termina deitado."
        }
        log += narrarTroca(npc.nome, "você", stats.armaNome, intencao.aDistancia, atk, def.tipo, troca, intencao.local, tipoDano(stats.armaTipo))
        // Projeção (Lote 417, MB p.378): o golpe contuso/cortante do NPC pode jogar o herói para trás.
        if (troca.dano != null && heroi.vivo)
            aplicarProjecao(heroi, npc, danoBasicoNpc, tipoDano(stats.armaTipo), (troca.dano?.pvSubtrair ?: 0) > 0,
                heroiPerfil.st, heroiPerfil.dx)
        // Lote 390 (MB p.376): aparar um tiro à queima-roupa = desviar a ARMA do atacante, não o projétil.
        if (def.tipo == CombatResolver.TipoDefesa.APARA && intencao.aDistancia && troca.defendeu)
            log += "  └ você desvia a arma do atirador (não o projétil) — só dá pra aparar à queima-roupa."
        // Apontar (Lote 392, MB p.364): usar uma defesa ativa faz o herói PERDER a pontaria acumulada.
        if (troca.defesaTentada && apontarAlvoId != null) {
            limparApontar()
            log += "  └ você perde a mira (usou uma defesa ativa)."
        }
        // Apontar (Lote 395, MB p.364): se foi FERIDO ainda mirando (sem usar defesa), testa Vontade p/ não perder a mira.
        if (apontarAlvoId != null && (troca.dano?.pvSubtrair ?: 0) > 0) {
            val rolVont = rolar3d6()
            if (rolVont > heroiPerfil.vontade) {
                limparApontar(); log += "  └ a dor faz você perder a mira (Vontade ${heroiPerfil.vontade}, rolou $rolVont)."
            } else log += "  └ você aguenta a dor e mantém a mira (Vontade ${heroiPerfil.vontade}, rolou $rolVont)."
        }
        // Concentrar (Lote 397, MB p.344): ser forçado a defender OU ser ferido exige Vontade-3 p/ manter a concentração.
        if (concentrando && (troca.defesaTentada || (troca.dano?.pvSubtrair ?: 0) > 0)) {
            val alvoVont = heroiPerfil.vontade - 3
            val rol = rolar3d6()
            if (rol > alvoVont) {
                concentrando = false
                log += "  └ você PERDE a concentração (Vontade-3 = $alvoVont, rolou $rol) — a ação recomeça."
            } else log += "  └ você mantém a concentração apesar da interrupção (Vontade-3 = $alvoVont, rolou $rol)."
        }
        // Erro Crítico do NPC (Lote 384, MB p.557): o oponente tropeça no próprio golpe.
        if (atk.critico == CriticoRules.ResultadoCritico.FALHA_CRITICA)
            aplicarErroCritico(npc, stats.ht, stats.armaDano, stats.armaNome.isBlank(), npc.nome)
        // Sucesso DECISIVO na defesa (Lote 415, MB p.374): crítico ao defender um ataque CaC → o atacante joga
        // na Tabela de Erro Crítico (você o desarmou/tapeou). Não vale contra ataque à distância.
        else if (troca.defendeu && !intencao.aDistancia && CombatResolver.defesaDecisiva(def.soma, def.valorFinal)) {
            log += "✨ Defesa DECISIVA! Você surpreende ${npc.nome} — ele joga na Tabela de Erro Crítico:"
            aplicarErroCritico(npc, stats.ht, stats.armaDano, stats.armaNome.isBlank(), npc.nome)
        }
        val incap = !heroi.vivo
        verificarFim()
        return AtaqueResultado(
            acertou = atk.resultado == CombatActions.ResultadoAcerto.ACERTO,
            defendeu = troca.defendeu, danoAplicado = troca.dano?.pvSubtrair ?: 0,
            alvoIncapacitado = incap, texto = troca.texto
        )
    }

    // ── Avanço de turno / fim ─────────────────────────────────────────────────

    /**
     * Lote MEC-22: resolve o tique das mágicas que ferem a cada turno.
     *
     * Regra literal (Morte Candente / Morte Putrefata): *"Toda vez, a vítima deve fazer um teste de
     * HT; em uma falha (crítica ou não), ele recebe 1d-1 de dano... Em um sucesso, ele não leva dano
     * naquele turno; em um sucesso decisivo, a mágica está quebrada."* A Morte Putrefata troca o dado
     * por **6 pontos** na falha crítica. **RD não protege** em nenhuma das duas.
     */
    /**
     * Lote MEC-26 (C4, Magia p.10 "Concentração e Manutenção"): mágica que exige concentração
     * contínua é abalada quando o operador apanha ou é atordoado.
     *
     * Regra literal: *"Se for distraído, **sofrer uma lesão** ou ficar **atordoado**, ele deverá
     * fazer um teste de **Vontade com uma penalidade igual a −3**. O **fracasso não encerra** a
     * mágica, mas ela permanecerá **exatamente como estava** e não irá se alterar até que ele possa
     * se concentrar novamente. A **falha crítica encerra** a mágica."*
     *
     * Traduzido para o motor: fracasso **congela** o tique deste turno (a mágica não avança nem
     * fere); falha crítica **derruba** a mágica. Sucesso segue normal.
     *
     * @return ids das mágicas CONGELADAS neste turno (o tique as pula).
     */
    // Lote MOTOR-4: abalo de concentração e tique de dano por turno agora moram no
    // EfeitosMagicosDelegate; avancarTurno chama efeitos.abaloDeConcentracao()/tiquePorTurno().

    // ── Lote MEC-46 (P1b): ZONAS PERSISTENTES (chuvas, nuvens, gás) ──────────────────────────────

    // Lote MOTOR-1: o subsistema INTEIRO de zonas (estado + registrar/encolher/tique/suplantação)
    // foi para `subsistemas/ZonaDelegate`, testável sozinho. O motor injeta o que o tique precisa
    // (log, RNG, HT/RD do alvo, reavaliar fim) por lambda, e reexpõe a API pública por delegação —
    // quem chamava `s.registrarZona`/`s.zonasAtivas`/`s.ocupantesDaZona` continua chamando igual.
    private val zonaDelegate = com.gurps.ficha.domain.combat.subsistemas.ZonaDelegate(
        log = log,
        random = random,
        combatentes = { encounter.combatentes },
        distanciaAoHeroi = { distancia(it) },
        htDoAlvo = { if (it.ehHeroi) heroiPerfil.ht else it.htEfetivo },
        rdDaZona = { alvo, z ->
            when (z.armadura) {
                "ignora" -> 0
                "ignora_vestida" -> (alvo.stats?.rdNatural ?: 0) + alvo.buffRd
                else -> if (alvo.ehHeroi) heroiPerfil.rd else ((alvo.stats?.rd ?: 0) + alvo.buffRd)
            }
        },
        aoMudarEstado = { verificarFim() },
    )

    val zonasAtivas: List<ZonaPersistente> get() = zonaDelegate.zonasAtivas
    /** Ponto de injeção da OCUPAÇÃO (o controller troca pelo cálculo real por hex). */
    var ocupantesDaZona: (ZonaPersistente) -> List<Combatente>
        get() = zonaDelegate.ocupantesDaZona
        set(v) { zonaDelegate.ocupantesDaZona = v }

    fun registrarZona(z: ZonaPersistente) = zonaDelegate.registrarZona(z)
    fun limparZonas() = zonaDelegate.limparZonas()
    fun encolherZona(nomeDaZona: String, novoRaioM: Int): Boolean = zonaDelegate.encolherZona(nomeDaZona, novoRaioM)
    private fun tiqueDasZonas() = zonaDelegate.tiqueDasZonas()

    // Lote MOTOR-4: tiquePorTurnoDasMagias virou efeitos.tiquePorTurno(congeladas) no delegate.

    /** Avança até o próximo combatente que ainda pode agir; ao fim de cada turno, recupera atordoamento. */
    fun avancarTurno(): Combatente {
        if (encerrado) return combatenteAtual()
        // tenta recuperar atordoamento de quem acabou de agir
        val anterior = combatenteAtual()
        if (Condicao.ATORDOADO in anterior.condicoes) {
            val ht = if (anterior.ehHeroi) heroiPerfil.ht else (anterior.htEfetivo)
            // Lote MEC-41: loga TAMBÉM a falha. Antes só o sucesso aparecia, então o jogador não via
            // que houve teste de HT — parecia tempo fixo (dúvida real do usuário no teste).
            val rolAtd = rolar3d6()
            if (rolAtd <= ht) {
                anterior.condicoes.remove(Condicao.ATORDOADO)
                log += "• ${anterior.nome} recupera-se do atordoamento (HT $ht, rolou $rolAtd)."
            } else {
                log += "• ${anterior.nome} continua ATORDOADO (HT $ht, rolou $rolAtd)."
            }
        }
        // Lote MEC-19: quem está preso por uma condição de ESCAPE gasta o turno tentando romper
        // (o gelo do Toque Congelante). Sucesso liberta na hora; falha, tenta de novo no próximo.
        anterior.escapeCondicao?.let { esc ->
            val base = when (esc.atributo.uppercase()) {
                "ST" -> if (anterior.ehHeroi) heroiPerfil.st else (anterior.stats?.st ?: 10)
                "HT" -> if (anterior.ehHeroi) heroiPerfil.ht else anterior.htEfetivo
                else -> if (anterior.ehHeroi) heroiPerfil.ht else anterior.htEfetivo
            }
            val alvoTeste = base + esc.penalidade
            val rol = rolar3d6()
            if (rol <= alvoTeste) {
                anterior.condicoes.remove(esc.condicao)
                anterior.escapeCondicao = null
                log += "• ${anterior.nome} ROMPE ${esc.descricao} (${esc.atributo} $alvoTeste, rolou $rol) e se liberta."
            } else {
                log += "• ${anterior.nome} tenta romper ${esc.descricao} e não consegue (${esc.atributo} $alvoTeste, rolou $rol)."
            }
        }
        // Lote MEC-17: condições com PRAZO correm o relógio quando o turno de quem as sofre termina
        // (1 turno = 1 segundo) e caem sozinhas ao zerar. Sem isto a Cegar era eterna.
        // Lote MEC-37 (P4): o rider de ofuscamento corre o relógio junto (1 turno = 1 s) e some.
        if (anterior.penalidadeCombateSeg > 0) {
            anterior.penalidadeCombateSeg -= 1
            if (anterior.penalidadeCombateSeg <= 0) {
                anterior.penalidadeCombateTemp = 0
                log += "• ${anterior.nome} recupera a mira (o ofuscamento passou)."
            }
        }
        anterior.condicoesTemporarias.entries.toList().forEach { (cond, resta) ->
            val novo = resta - 1
            if (novo > 0) anterior.condicoesTemporarias[cond] = novo
            else {
                anterior.condicoesTemporarias.remove(cond)
                anterior.condicoes.remove(cond)
                log += "• ${anterior.nome} não está mais ${cond.rotulo.uppercase()}."
            }
        }
        // Lote COND-1: quem DORMINDO levou dano (choquePendente > 0) ACORDA (MB p.428). Paralisia NÃO acorda.
        encounter.combatentes.filter { Condicao.DORMINDO in it.condicoes && it.choquePendente > 0 }.forEach {
            it.condicoes.remove(Condicao.DORMINDO); log += "• ${it.nome} ACORDA com o golpe."
        }
        // Lote MEC-39 (C1, Magia p.12): se o herói SEGURA um projétil e sofreu lesão desde o próprio
        // turno anterior, testa Vontade — falhando, *"o projétil irá afetá-lo imediatamente!"*.
        // Usa o mesmo sinal de lesão (`choquePendente`) do abalo de concentração, e ANTES do reset.
        if (anterior.ehHeroi && projetilCarregado != null && anterior.choquePendente > 0) {
            val p = projetilCarregado!!
            val rol = rolar3d6()
            if (rol <= heroiPerfil.vontade) {
                log += "🔮 Ferido, você segura firme ${p.nome} (Vontade ${heroiPerfil.vontade}, rolou $rol)."
            } else {
                projetilCarregado = null
                val expr = com.gurps.ficha.domain.magic.MagicMechanics.expandirDano(
                    p.ctx.mecanica?.danoPorEnergia ?: "1d", p.energiaAcumulada,
                    p.ctx.mecanica?.energiaPorDado ?: 1, p.ctx.mecanica?.danoFixo ?: false)
                val bruto = rolarDano(expr, random)
                val dn = HitLocationRules.aplicarDano(heroi.pvMax, bruto, DanoTipo.CONT, LocalAtaque.TORSO, heroiPerfil.rd)
                InjuryRules.ferir(heroi, dn.pvSubtrair, heroiPerfil.ht, random)
                log += "💥 Ferido, você PERDE o controle de ${p.nome} (Vontade ${heroiPerfil.vontade}, rolou $rol) — " +
                    "ela dispara em VOCÊ: $expr → ${dn.pvSubtrair} de dano!" + (if (!heroi.vivo) " Você cai!" else "")
            }
        }
        // Lote MEC-26 (C4): o abalo de concentração precisa rodar ANTES do reset do choque logo
        // abaixo — senão o gatilho "sofreu uma lesão" já foi apagado e o teste nunca dispara.
        // (Foi exatamente esse o bug: o teste ficava vermelho porque eu chequei tarde demais.)
        val congeladasPorAbalo =
            if (anterior.ehHeroi && efeitos.temAtivas()) efeitos.abaloDeConcentracao() else emptySet()

        // Choque (Lote 382): expira ao fim do turno de quem agiu (valeu só no turno seguinte ao ferimento).
        anterior.choquePendente = 0
        // Lote MEC-6: buff de UM ÚNICO USO some ao fim da PRÓXIMA ação do dono. O turno em que foi
        // conjurado não conta (mesma armadilha do Lote 424): o herói conjura Aumentar Força gastando
        // a ação, então o bônus tem que sobreviver até o turno seguinte para ele poder usá-lo.
        anterior.buffs.removeAll { it.umUnicoUso && it.estreou }
        anterior.buffs.filter { it.umUnicoUso && !it.estreou }.forEach { it.estreou = true }
        // Magias ativas (Lote MA-3d-4/MOTOR-4): o fim do turno do herói = 1 segundo de jogo. O tique de
        // dano (Morte Candente/Putrefata, MEC-22) resolve ANTES do relógio de manutenção; depois o
        // delegate cobra manutenção e expira as duradouras (MagicActive, MB Magia p.9-10).
        if (anterior.ehHeroi && efeitos.temAtivas()) efeitos.tiquePorTurno(congeladasPorAbalo)
        // Lote MEC-46 (P1b): as ZONAS correm o mesmo relógio.
        if (anterior.ehHeroi) tiqueDasZonas()

        if (anterior.ehHeroi && efeitos.temAtivas()) efeitos.avancarUmSegundo()
        // Modificadores situacionais (Lote 424): decrementam ao fim do turno do DONO; expirados saem da lista.
        // O turno da CRIAÇÃO não conta (o chat roda no turno do dono — senão "1 rodada" expiraria antes de valer).
        modsSituacionais.removeAll { m ->
            when {
                m.alvoId != anterior.id || m.rodadasRestantes == Int.MAX_VALUE -> false
                !m.estreou -> { m.estreou = true; false }
                else -> { m.rodadasRestantes -= 1; m.rodadasRestantes <= 0 }
            }
        }
        // zera defesas do turno de quem vai começar
        var prox = encounter.proximoTurno()
        var guarda = 0
        while (!prox.vivo && guarda++ < encounter.combatentes.size) {
            // Inconsciente mas ainda VIVO (PV > −PVmáx) continua sangrando — pode sangrar até a morte (MB p.420).
            if (prox.pvAtual > -prox.pvMax) tickSangramentoNoTurno(prox)
            prox = encounter.proximoTurno()
        }
        prox.defesasUsadas = DefesasUsadas()
        // Lote MEC-27: a cota de UMA mágica de Bloqueio renova quando o turno do herói recomeça.
        if (prox.ehHeroi) bloqueioMagicoUsadoNoTurno = false
        tickSangramentoNoTurno(prox)
        return prox
    }

    /**
     * Lote NARR-1: **o Narrador esquecia as mágicas narradas.**
     *
     * O motor executa o que tem número; o resto sai como *"Efeito narrado pelo Mestre — <nota>"* **na
     * hora da conjuração** e nunca mais. Uma Aerovisão de 1 minuto era anunciada uma vez e ficava
     * 60 turnos em silêncio — o Narrador aplicava no primeiro turno e esquecia. Não é falta de regra:
     * é falta de LEMBRETE. (Decisão do usuário: assumir o narrativo e fazê-lo bem.)
     *
     * ⚠️ **Vai no [resumo], NÃO no [log].** São canais diferentes: o `log` é publicado no feed e **o
     * jogador lê**; o `resumo()` é o estado factual servido ao Narrador pela tool `acao_npc` e
     * ninguém mais vê. A primeira versão deste lembrete saía no `log` a cada turno do herói, e o
     * usuário cortou na hora: *"não precisa colocar tudo o que for implementado no log"*. Contexto
     * que existe para a IA aplicar regra não é linha de log — mesmo princípio do status que virou
     * overlay no TOK-6b-3.
     *
     * Só entra o que o motor **não** aplica: buff sem número e sem tique. O que tem número já mexe
     * na ficha sozinho, e repetir seria ruído no prompt.
     */
    fun lembreteDeMagiasNarradas(): String? {
        if (magiasAtivas.isEmpty()) return null
        val narradas = magiasAtivas.filter { m ->
            (m.buff == null || m.buff!!.soNarrado) &&
                !com.gurps.ficha.domain.magic.MagicMechanics.temTiquePorTurno(m.mecanica)
        }
        if (narradas.isEmpty()) return null
        val itens = narradas.joinToString("; ") { m ->
            val alvo = encounter.combatentes.firstOrNull { it.id == m.alvoId }?.nome
            val regra = m.mecanica?.notas?.takeIf { it.isNotBlank() }
                ?: m.buff?.rotulo?.takeIf { it.isNotBlank() }
            m.magiaId +
                (alvo?.let { " em $it" } ?: "") +
                (regra?.let { " — ${it.take(160)}" } ?: "")
        }
        return "Mágicas ainda em efeito (aplique ao narrar): $itens"
    }

    /**
     * Sangramento (Lote PONTE-2, MB p.420): se o ferido fechou um intervalo (1 rodada ≈ 1s), testa HT ou perde PV.
     * Em combates curtos quase nunca dispara (intervalo de 60s/30s) — fiel à regra; importa em lutas longas.
     */
    private fun tickSangramentoNoTurno(c: Combatente) {
        if (!c.sangramentoAtivo) return
        val rodada = encounter.rodadaAtual
        if (c.sangramentoUltimaRodada == Int.MIN_VALUE) { c.sangramentoUltimaRodada = rodada; return }
        if (rodada - c.sangramentoUltimaRodada < c.sangramentoIntervaloSeg) return
        c.sangramentoUltimaRodada = rodada
        val ht = if (c.ehHeroi) heroiPerfil.ht else (c.htEfetivo)
        InjuryRules.tickSangramento(c, ht, random)?.logs?.forEach { log += "🩸 ${c.nome}: $it" }
        verificarFim()
    }

    private fun verificarFim() {
        if (encerrado) return
        if (!heroi.vivo) { encerrado = true; resultado = ResultadoCombate.DERROTA; log += "💀 Combate encerrado: o herói foi derrotado." }
        else if (inimigosVivos.isEmpty()) { encerrado = true; resultado = ResultadoCombate.VITORIA; log += "🏆 Combate encerrado: vitória do herói." }
    }

    /** Reavalia o fim após um efeito aplicado FORA do loop de turnos (ex.: dano do Narrador). B8. */
    fun reavaliarFim() = verificarFim()

    /** Resumo factual do estado atual — base do que o Narrador vai narrar. */
    fun resumo(): String = encounter.estadoResumo() +
        // Lote NARR-1: o que o motor NÃO executa entra aqui, para o Narrador continuar aplicando
        // enquanto durar. Este texto vai só para a IA (tool `acao_npc`) — nunca para o feed.
        (lembreteDeMagiasNarradas()?.let { "\n$it" } ?: "")

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

    /** Esquiva de um NPC = Velocidade Básica + 3 (MB p.374); Vel.Básica pela metade se cambaleante (MB p.380). */
    /** Penalidade de defesa por atordoamento (Lote 393, MB p.364) + cegueira (COND-1): −4 cada. */
    private fun penDefesaAtordoado(c: Combatente): Int =
        (if (Condicao.ATORDOADO in c.condicoes) 4 else 0) + (if (Condicao.CEGO in c.condicoes) 4 else 0)

    /**
     * Lote TESTE-NPC: rolagem de defesa DO NPC (nunca a do herói).
     *
     * No modo BONECO ele não se defende — e isso exige **pular a rolagem**, não zerar o valor:
     * em GURPS um **3 ou 4 é sucesso automático**, então uma Esquiva 0 ainda escaparia de vez em
     * quando. Foi exatamente o bug que o teste pegou; no aparelho apareceria como "botei Boneco e
     * mesmo assim ele esquivou".
     */
    /**
     * Lote MEC-31: no modo BONECO o alvo também **não resiste**.
     *
     * Motivo (teste do usuário): o modo promete *"não agem nem defendem"*, mas o goblin ainda vencia
     * a disputa de resistência e dissipava a mágica — então testar Morte Candente virava loteria
     * (errar o toque, ou acertar e o alvo resistir). Resistência não é defesa ATIVA pela regra, mas
     * na arena de teste a intenção é a mesma: nada bloqueia. Só vale no sandbox.
     */
    private fun npcResistiu(resistiuDeVerdade: Boolean): Boolean =
        modoTesteNpc != ModoTesteNpc.BONECO && resistiuDeVerdade

    private fun npcSeDefendeu(valor: Int, rolagem: Int): Boolean =
        modoTesteNpc != ModoTesteNpc.BONECO && CombatResolver.defesaBemSucedida(valor, rolagem)

    private fun esquivaNpc(npc: Combatente): Int {
        // Lote TESTE-NPC: no modo BONECO o valor vai a 0 (o log mostra "Esquiva 0"), mas quem
        // realmente impede a defesa é `npcSeDefendeu` — zerar sozinho NÃO basta (regra do 3/4).
        if (modoTesteNpc == ModoTesteNpc.BONECO) return 0
        val velB = if (npc.cambaleante) npc.velocidadeBasica / 2 else npc.velocidadeBasica
        return floor(velB).toInt() + 3 - penDefesaAtordoado(npc)
    }

    /** Melhor defesa de um NPC: Esquiva (Vel.Básica+3) vs Aparar (NH/2+3, só corpo-a-corpo); −4 se atordoado/cego. */
    private fun melhorDefesaNpc(npc: Combatente): Pair<CombatResolver.TipoDefesa, Int> {
        // Lote TESTE-NPC: BONECO não apara nem esquiva.
        if (modoTesteNpc == ModoTesteNpc.BONECO) return CombatResolver.TipoDefesa.ESQUIVA to 0
        // Indefeso (MB p.371/428/429): imobilizado, dormindo ou paralisado — sem defesa ativa.
        if (Condicao.IMOBILIZADO in npc.condicoes || Condicao.DORMINDO in npc.condicoes || Condicao.PARALISADO in npc.condicoes)
            return CombatResolver.TipoDefesa.ESQUIVA to 0
        val esquiva = esquivaNpc(npc)
        val melee = (npc.stats?.alcanceMetros ?: 1) <= 2
        val apara = if (melee) (npc.stats?.armaNh ?: 0) / 2 + 3 - penDefesaAtordoado(npc) else 0
        return if (apara > esquiva) CombatResolver.TipoDefesa.APARA to apara
        else CombatResolver.TipoDefesa.ESQUIVA to esquiva
    }

    private fun rolar3d6(): Int = (1..3).sumOf { random.nextInt(1, 7) }

    /** Resultado de aplicar a Tabela de Golpe Fulminante (Lote 384): dano/RD ajustados + flag de ferimento grave. */
    private data class GolpeFulminanteAplicado(val dano: Int, val rd: Int, val grave: Boolean, val nota: String)

    /** Rola a Tabela de Golpe Fulminante (MB p.558) e aplica ao dano básico / RD do alvo. */
    private fun aplicarGolpeFulminante(danoBasico: Int, rd: Int, danoExpr: String): GolpeFulminanteAplicado {
        val soma = rolar3d6()
        return when (CriticoRules.golpeFulminante(soma)) {
            CriticoRules.EfeitoGolpeFulminante.TRIPLO -> GolpeFulminanteAplicado(danoBasico * 3, rd, false, "×3 no dano! (tabela $soma)")
            CriticoRules.EfeitoGolpeFulminante.DOBRO -> GolpeFulminanteAplicado(danoBasico * 2, rd, false, "×2 no dano! (tabela $soma)")
            CriticoRules.EfeitoGolpeFulminante.MAXIMO -> GolpeFulminanteAplicado(maxOf(danoBasico, danoMaximo(danoExpr)), rd, false, "dano máximo! (tabela $soma)")
            CriticoRules.EfeitoGolpeFulminante.RD_METADE -> GolpeFulminanteAplicado(danoBasico, rd / 2, false, "RD do alvo pela metade! (tabela $soma)")
            CriticoRules.EfeitoGolpeFulminante.FERIMENTO_GRAVE -> GolpeFulminanteAplicado(danoBasico, rd, true, "trata como ferimento grave! (tabela $soma)")
            CriticoRules.EfeitoGolpeFulminante.NORMAL -> GolpeFulminanteAplicado(danoBasico, rd, false, "golpe certeiro (dano normal, tabela $soma)")
        }
    }

    /**
     * Projeção / knockback (Lote 417, MB p.378): contusão SEMPRE projeta; corte só se NÃO penetrou a RD.
     * 1m por múltiplo completo de (ST−2) do dano BÁSICO; o projetado testa DX (−1/m após o 1º) ou cai.
     */
    private fun aplicarProjecao(alvoProjetado: Combatente, npcDaTroca: Combatente, danoBasico: Int,
                                tipo: DanoTipo, penetrou: Boolean, stAlvo: Int, dxAlvo: Int) {
        val projeta = tipo == DanoTipo.CONT || (tipo == DanoTipo.CORT && !penetrou)
        if (!projeta || danoBasico <= 0) return
        val metros = danoBasico / (stAlvo - 2).coerceAtLeast(1)
        if (metros <= 0) return
        encounter.moverEmRelacaoAoHeroi(npcDaTroca.id, metros) // aumenta a distância herói↔NPC
        log += "  ➡️ ${alvoProjetado.nome} é projetado ${metros}m pelo impacto (dano básico $danoBasico vs ST $stAlvo)."
        if (rolar3d6() > dxAlvo - (metros - 1)) {
            alvoProjetado.postura = Postura.DEITADO; alvoProjetado.condicoes.add(Condicao.CAIDO)
            log += "  └ ${alvoProjetado.nome} cai com a projeção!"
        }
    }

    /** Rola a Tabela de Erro Crítico (MB p.557) e aplica ao ATACANTE o que o motor suporta; narra o resto. */
    private fun aplicarErroCritico(atacante: Combatente, htAtacante: Int, danoExpr: String, desarmado: Boolean, nome: String) {
        val soma = rolar3d6()
        when (CriticoRules.erroCritico(soma, desarmado)) {
            CriticoRules.EfeitoErroCritico.ACERTA_A_SI -> {
                val d = rolarDano(danoExpr, random).coerceAtLeast(1)
                InjuryRules.ferir(atacante, d, htAtacante, random)
                log += "  💀 Erro crítico: $nome atinge a si mesmo — $d de dano! (tabela $soma)"
            }
            CriticoRules.EfeitoErroCritico.ACERTA_A_SI_METADE -> {
                val d = (rolarDano(danoExpr, random) / 2).coerceAtLeast(1)
                InjuryRules.ferir(atacante, d, htAtacante, random)
                log += "  💀 Erro crítico: $nome se machuca — $d de dano! (tabela $soma)"
            }
            CriticoRules.EfeitoErroCritico.CAI -> {
                atacante.condicoes.add(Condicao.CAIDO); atacante.postura = Postura.DEITADO
                log += "  💀 Erro crítico: $nome perde o apoio e cai! (tabela $soma)"
            }
            CriticoRules.EfeitoErroCritico.QUEBRA_ARMA -> log += "  💀 Erro crítico: a arma de $nome se quebra! (tabela $soma)"
            CriticoRules.EfeitoErroCritico.LARGA_ARMA -> log += "  💀 Erro crítico: $nome deixa a arma cair! (tabela $soma)"
            CriticoRules.EfeitoErroCritico.DESEQUILIBRIO -> log += "  💀 Erro crítico: $nome perde o equilíbrio! (tabela $soma)"
        }
    }

    companion object {
        /** Lote MEC-39 (P11): máximo de segundos criando/aumentando um projétil (Magia p.12). */
        const val MAX_TURNOS_PROJETIL = 3
        /**
         * Lote P5: até onde o respingo da explosão é procurado. Além disso o divisor `3×distância`
         * já reduziria o dano a zero em qualquer expressão realista — varrer mais seria só custo.
         */
        const val RAIO_RESPINGO_M = 5
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
         * Resultado de uma Finta (Lote 383, MB p.366): Disputa Rápida entre atacante e defensor (rolagens
         * 3d6 já feitas). Devolve a penalidade na defesa do alvo no próximo golpe: 0 se a finta falhou;
         * a margem de sucesso do atacante se o defensor falhou; ou a margem de VITÓRIA (margem do atacante −
         * margem do defensor) se ambos tiveram sucesso.
         */
        fun fintaResultado(nhAtacante: Int, rolAtacante: Int, nhDefensor: Int, rolDefensor: Int): Int {
            if (rolAtacante > nhAtacante) return 0 // o fintador falhou no próprio teste
            val margemAtacante = nhAtacante - rolAtacante
            val defensorTeveSucesso = rolDefensor <= nhDefensor
            if (!defensorTeveSucesso) return margemAtacante
            val margemDefensor = nhDefensor - rolDefensor
            return (margemAtacante - margemDefensor).coerceAtLeast(0)
        }

        /**
         * Disputa Rápida (MB p.348, Lote 386): A vence se obtém sucesso e (B falha OU a margem de A é maior
         * que a de B). Empate ou A sem sucesso = A NÃO vence. Usada na luta agarrada (Derrubar/desvencilhar).
         */
        fun vencaDisputaRapida(valorA: Int, rolA: Int, valorB: Int, rolB: Int): Boolean {
            if (rolA > valorA) return false
            if (rolB > valorB) return true
            return (valorA - rolA) > (valorB - rolB)
        }

        /**
         * Remove o token de TIPO do fim da expressão de dano (Lote 378), deixando só os dados ("2d-1 pa" →
         * "2d-1"). O tipo é mostrado à parte ([tipoDano]); sem isso o app exibia o tipo duplicado ("2d-1 pa pi").
         */
        fun semTokenTipo(expr: String): String =
            expr.trim().replace(
                Regex("\\s+(pa\\+\\+|pa\\+|pa-|pa|pi\\+\\+|pi\\+|pi-|pi|corte|cort|perf|imp|cont|esm|qmd|cor|tox|fad|queimadura)\\s*$", RegexOption.IGNORE_CASE),
                ""
            ).trim()

        /**
         * O ataque do NPC parece de arma de fogo? (Lote 380 — o BD do escudo do herói não vale contra fogo,
         * MB p.375.) Heurística pelo nome da arma, já que o bestiário atual não traz a flag explícita.
         */
        fun pareceArmaDeFogo(nome: String?): Boolean {
            val n = (nome ?: "").lowercase()
            if (n.isBlank()) return false
            return listOf(
                "revolver", "revólver", "pistola", "rifle", "mosquete", "fuzil", "carabina", "espingarda",
                "metralhad", "submetralhad", "arma de fogo", "winchester", "colt", "canhao", "canhão",
                "lanca-chama", "lança-chama", "lança-chamas"
            ).any { n.contains(it) }
        }

        /** Bônus PARA ACERTAR por nº de tiros numa rajada (MB p.374). Tiros <=4 = +0. */
        fun bonusCadenciaTiro(tiros: Int): Int = when {
            tiros <= 4 -> 0
            tiros <= 8 -> 1
            tiros <= 12 -> 2
            tiros <= 16 -> 3
            tiros <= 24 -> 4
            tiros <= 49 -> 5
            tiros <= 99 -> 6
            else -> 7
        }

        /** Nº de tiros que ACERTAM numa rajada: 1 + ⌊margem/Recuo⌋, limitado aos tiros disparados (MB p.374). */
        fun acertosDaRajada(margem: Int, recuo: Int, tirosDisparados: Int): Int =
            (1 + margem / recuo.coerceAtLeast(1)).coerceIn(1, tirosDisparados.coerceAtLeast(1))

        /** Interpreta a coluna Aparar do catálogo ("0", "-1", "0D", "0E", "F", "Não") → (mod, tipo). MB p.270. */
        fun parseAparar(raw: String?): Pair<Int, ApararTipo> {
            val s = (raw ?: "").trim()
            if (s.isEmpty()) return 0 to ApararTipo.NORMAL
            val low = s.lowercase()
            val tipo = when {
                low.contains("nao") || low.contains("não") -> ApararTipo.NAO
                low.contains("e") || low.contains("f") -> ApararTipo.ESGRIMA      // E (esgrima Devir) / F (fencing)
                low.contains("d") || low.contains("u") -> ApararTipo.DESBALANCEADA // D (Devir) / U (unbalanced)
                else -> ApararTipo.NORMAL
            }
            val mod = Regex("-?\\d+").find(s)?.value?.toIntOrNull() ?: 0
            return mod to tipo
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

        /** Dano MÁXIMO de uma expressão "<n>d[±m]" (cada dado = 6). Usado pelo Golpe Fulminante "dano máximo" (Lote 384). */
        fun danoMaximo(expr: String): Int {
            val m = Regex("""(\d+)d([+-]\d+)?""").find(expr.lowercase().replace(" ", "")) ?: return 0
            val qtd = m.groupValues[1].toIntOrNull() ?: 0
            val mod = m.groupValues.getOrNull(2)?.toIntOrNull() ?: 0
            return (qtd * 6 + mod).coerceAtLeast(0)
        }

        /**
         * Postura do ALVO como modificador no acerto À DISTÂNCIA (Lote 416, MB p.368): agachado/ajoelhado/
         * rastejando/sentado = alvo menor (−2); deitado = ainda menor (−4); em pé = 0. (Agachar)
         */
        fun penalidadePosturaAlvejado(postura: Postura): Int = when (postura) {
            Postura.EM_PE -> 0
            Postura.DEITADO -> -4
            else -> -2
        }

        /** Divisor de armadura na expressão de dano (Lote 413, MB p.378): "(2)"→2,0; "(0,5)"→0,5; sem→1,0. */
        fun divisorArmadura(expr: String): Double {
            val m = Regex("""\((\d*[.,]?\d+)\)""").find(expr) ?: return 1.0
            return m.groupValues[1].replace(",", ".").toDoubleOrNull()?.takeIf { it > 0 } ?: 1.0
        }

        /**
         * RD efetiva após o divisor de armadura (MB p.378): divisor ≥1 reduz a RD (÷divisor, arredonda p/ baixo);
         * divisor fracionário (0,5/0,2/0,1) MELHORA a RD (×2/×5/×10) e trata RD 0 como 1.
         */
        fun rdComDivisor(rd: Int, divisor: Double): Int {
            if (divisor == 1.0) return rd
            if (divisor > 1.0) return (rd / divisor).toInt().coerceAtLeast(0)
            val base = if (rd == 0) 1 else rd // RD 0 vira 1 contra divisor fracionário
            return (base / divisor).toInt()
        }

        /**
         * Dano do Encontrão (MB p.371, Lote 409): (PV × velocidade)/100 dados. <1d → 1d-3/1d-2/1d-1 pela fração;
         * ≥1d arredonda a fração de dado (0,5+ p/ cima). Retorna a expressão de dano por contusão.
         */
        fun encontraoDanoDados(pv: Int, velocidade: Int): String {
            val dados = pv * velocidade / 100.0
            if (dados < 1.0) return when {
                dados <= 0.25 -> "1d-3"
                dados <= 0.5 -> "1d-2"
                else -> "1d-1"
            }
            val inteiro = dados.toInt()
            val d = if (dados - inteiro >= 0.5) inteiro + 1 else inteiro
            return "${d}d"
        }

        /**
         * Bônus de dano do Ataque Total (Forte): +2 de dano OU +1 por dado, o que for maior (MB p.365, Lote 387).
         * Só vale para corpo-a-corpo (à distância não tem "Forte"). Espada de energia/dano de queimadura ficaria
         * de fora pela regra, mas o motor só modela dano por ST (GdP/GeB) — todo ataque corpo-a-corpo aqui é elegível.
         */
        fun bonusDanoForte(manobra: Manobra, modo: AtaqueTotalModo, danoExpr: String, aDistancia: Boolean): Int {
            if (manobra != Manobra.ATAQUE_TOTAL || modo != AtaqueTotalModo.FORTE || aDistancia) return 0
            val nDados = Regex("""(\d+)d""").find(danoExpr.lowercase())?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 1
            return maxOf(2, nDados)
        }

        /**
         * Mod de dano por manobra (Lote PONTE-4, AM p98): Ataque Dedicado (Forte) = +1 FIXO (não o +2/+1-por-dado do
         * Ataque Total); Ataque Defensivo = −(o pior entre 2 e nº de dados). Só corpo-a-corpo. (TODO opcional AM p98:
         * +1/2-dados no Dedicado Forte com ST alta — deferido para não chutar a interação com mods de arma.)
         */
        fun modDanoManobra(manobra: Manobra, dedicadoModo: DedicadoModo, danoExpr: String, aDistancia: Boolean): Int {
            if (aDistancia) return 0
            val nDados = Regex("""(\d+)d""").find(danoExpr.lowercase())?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 1
            return when (manobra) {
                Manobra.ATAQUE_DEDICADO -> if (dedicadoModo == DedicadoModo.FORTE) 1 else 0
                Manobra.ATAQUE_DEFENSIVO -> -maxOf(2, nDados)
                else -> 0
            }
        }
    }
}

// Lote MOTOR-5: HeroiPerfilCombate, ApararTipo, AtaqueHeroi, DefesaHeroi e ResultadoCombate
// (puro dado de fronteira) foram para CombatSessionTipos.kt no mesmo pacote.
