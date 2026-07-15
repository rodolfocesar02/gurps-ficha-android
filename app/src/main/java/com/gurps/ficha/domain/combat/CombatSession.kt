package com.gurps.ficha.domain.combat

import com.gurps.ficha.domain.magic.ContextoConjuracao
import com.gurps.ficha.domain.magic.CustoEnergia
import com.gurps.ficha.domain.magic.EfeitoChoqueRetorno
import com.gurps.ficha.domain.magic.MagicCasting
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
        if (CombatResolver.defesaBemSucedida(defValor, rolar3d6())) {
            log += "  └ ${alvo.nome} se defende (${defTipo.rotulo} $defValor) e desvia do encontrão."
            verificarFim(); return AtaqueResultado(true, true, 0, false, log.last())
        }
        val danoHeroi = rolarDano(encontraoDanoDados(heroi.pvMax, relVel), random)
        val danoNpc = rolarDano(encontraoDanoDados(alvo.pvMax, relVel), random)
        val dnNpc = HitLocationRules.aplicarDano(alvo.pvMax, danoHeroi, DanoTipo.CONT, LocalAtaque.TORSO,
            alvo.stats?.rd ?: 0, alvo.stats?.tolerancia ?: ToleranciaFerimentos.NORMAL)
        InjuryRules.ferir(alvo, dnNpc.pvSubtrair, alvo.stats?.ht ?: 10, random)
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
        if (CombatResolver.defesaBemSucedida(defValor, rolar3d6())) {
            log += "  └ ${alvo.nome} se defende (${defTipo.rotulo} $defValor) do empurrão."
            return AtaqueResultado(true, true, 0, false, log.last())
        }
        val forca = rolarDano(heroiPerfil.danoGdP, random) * 2 // GdP × 2 (duas mãos), MB p.371
        val stAlvo = (alvo.stats?.st ?: 10).coerceAtLeast(3)
        val knockback = forca / (stAlvo - 2) // projeção: 1m por múltiplo de (ST−2) no dano (MB p.378); sem lesão
        if (knockback > 0) {
            encounter.moverEmRelacaoAoHeroi(alvo.id, knockback)
            log += "  └ ${alvo.nome} é projetado ${knockback}m para trás (força $forca vs ST $stAlvo) — sem lesão (MB p.371/378)."
            if (knockback >= 2 && rolar3d6() > (alvo.stats?.dx ?: alvo.dx) - (knockback - 1)) {
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
        val stNpc = alvo.stats?.st ?: 10
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
        val resist = maxOf(alvo.stats?.st ?: 10, alvo.stats?.ht ?: 10)
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
        InjuryRules.ferir(alvo, dn.pvSubtrair, alvo.stats?.ht ?: 10, random)
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
        val resist = maxOf(alvo.stats?.st ?: 10, alvo.stats?.ht ?: 10) + (if (perna) 4 else 0)
        val rh = rolar3d6(); val rn = rolar3d6()
        val membro = if (perna) LocalAtaque.PERNA else LocalAtaque.BRACO
        val margem = (stHeroi - rh) - (resist - rn)
        if (rh > stHeroi || margem <= 0) {
            log += "🦾 ${alvo.nome} resiste à chave de ${membro.rotulo}. [ST $stHeroi rolou $rh vs $resist rolou $rn]"
            return log.last()
        }
        val dn = HitLocationRules.aplicarDano(alvo.pvMax, margem, DanoTipo.CONT, membro,
            alvo.stats?.rd ?: 0, alvo.stats?.tolerancia ?: ToleranciaFerimentos.NORMAL)
        InjuryRules.ferir(alvo, dn.pvSubtrair, alvo.stats?.ht ?: 10, random)
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
        val resist = maxOf(alvo.stats?.st ?: 10, alvo.stats?.ht ?: 10)
        val rh = rolar3d6(); val rn = rolar3d6()
        val margem = (stHeroi - rh) - (resist - rn)
        if (rh > stHeroi || margem <= 0) {
            log += "🫷 ${alvo.nome} resiste ao mata-leão. [ST $stHeroi rolou $rh vs $resist rolou $rn]"
            return log.last()
        }
        val danoBruto = (margem * 1.5).toInt().coerceAtLeast(1)
        val dn = HitLocationRules.aplicarDano(alvo.pvMax, danoBruto, DanoTipo.CONT, LocalAtaque.TORSO,
            alvo.stats?.rd ?: 0, alvo.stats?.tolerancia ?: ToleranciaFerimentos.NORMAL)
        InjuryRules.ferir(alvo, dn.pvSubtrair, alvo.stats?.ht ?: 10, random)
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
        var rdAlvo = rdComDivisor(alvo.stats?.rd ?: 0, divisorArmadura(ataque.danoExpr)) // Lote 413: divisor de armadura
        var forcaGrave = false
        // Golpe Fulminante (Lote 384, MB p.558): a defesa já é anulada pelo crítico; a tabela modifica o DANO.
        if (atk.critico == CriticoRules.ResultadoCritico.DECISIVO) {
            val gf = aplicarGolpeFulminante(danoBasico, rdAlvo, ataque.danoExpr)
            danoBasico = gf.dano; rdAlvo = gf.rd; forcaGrave = gf.grave
            log += "  ⭐ Golpe Fulminante — ${gf.nota}"
        }
        val danoBruto = if (meioDano) danoBasico / 2 else danoBasico

        val troca = CombatResolver.resolverTroca(
            defensor = alvo, htDefensor = alvo.stats?.ht ?: 10, ataque = atk,
            defesaTipo = defTipo, defesaValorFinal = defValorFinal, defesaSoma = defSoma,
            surpresa = costasNpc, // Lote TOK-5a: ataque pelas costas anula a defesa do NPC (MB p.374)
            danoBaseRolado = danoBruto, danoTipo = ataque.tipo,
            local = local, rdLocal = rdAlvo, randomFerimento = random, forcarFerimentoGrave = forcaGrave,
            tolerancia = alvo.stats?.tolerancia ?: ToleranciaFerimentos.NORMAL
        )
        if (penFinta > 0) log += "  └ finta: a defesa de ${alvo.nome} cai −$penFinta neste golpe (${defValor}→${defValorFinal})."
        log += narrarTroca("Você", alvo.nome, ataque.rotulo.substringBefore(" (").trim(), ataque.aDistancia, atk, defTipo, troca, local, ataque.tipo)
        // Projeção (Lote 417, MB p.378): contusão/corte que acerta pode jogar o alvo para trás (o helper filtra o tipo).
        if (troca.dano != null && alvo.vivo)
            aplicarProjecao(alvo, alvo, danoBruto, ataque.tipo, (troca.dano?.pvSubtrair ?: 0) > 0,
                alvo.stats?.st ?: alvo.pvMax, alvo.stats?.dx ?: alvo.dx)
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
                    InjuryRules.ferir(alvo, rd.pvSubtrair, alvo.stats?.ht ?: 10, random)
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

                var alvoResistiu = false
                var dano = 0

                if (r.exigeResistencia && alvo != null && ctx.classe.resistencia != null) {
                    val resist = resistenciaDoAlvo(alvo, ctx.classe.resistencia!!)
                    val rr = MagicCasting.resolverResistencia(nhEf.valor, rol, resist, rolar3d6(), regraDo16 = true)
                    alvoResistiu = rr.alvoResistiu
                    sb.append(if (alvoResistiu) " ${alvo.nome} RESISTE (resistência $resist)."
                              else " ${alvo.nome} não resiste (resistência $resist).")
                }

                if (!alvoResistiu && TipoClasseMagia.PROJETIL in ctx.classe.classes && alvo != null) {
                    val energia = energiaInvestida.coerceAtLeast(1)
                    // 2º teste (Magia p.12): Ataque Inato para ACERTAR (aprox. DX + SSR de distância).
                    val nhAcerto = heroiPerfil.dx + penalidadeDistancia(ctx.distanciaMetros)
                    val rolAcerto = rolar3d6()
                    if (rolAcerto > nhAcerto) {
                        sb.append(" O projétil passa longe (Ataque Inato NH $nhAcerto, rolou $rolAcerto).")
                    } else {
                        // O alvo pode ESQUIVAR (ou bloquear), NUNCA aparar (Magia p.12).
                        val esq = esquivaNpc(alvo)
                        if (CombatResolver.defesaBemSucedida(esq, rolar3d6())) {
                            sb.append(" ${alvo.nome} ESQUIVA do projétil (Esquiva $esq).")
                        } else {
                            val bruto = rolarDano("${energia}d", random)
                            val dn = HitLocationRules.aplicarDano(alvo.pvMax, bruto, DanoTipo.CONT, LocalAtaque.TORSO,
                                alvo.stats?.rd ?: 0, alvo.stats?.tolerancia ?: ToleranciaFerimentos.NORMAL)
                            InjuryRules.ferir(alvo, dn.pvSubtrair, alvo.stats?.ht ?: 10, random)
                            dano = dn.pvSubtrair
                            sb.append(" Projétil de ${energia}d acerta → ${dn.pvSubtrair} de dano em ${alvo.nome}" +
                                (if (!alvo.vivo) " (fora de combate!)." else "."))
                        }
                    }
                } else if (!alvoResistiu && TipoClasseMagia.PROJETIL !in ctx.classe.classes) {
                    sb.append(" Efeito narrado pelo Mestre.")
                }

                verificarFim(); log += sb.toString().trim()
                return ResultadoConjuracaoCombate(true, sb.toString().trim(), dano, alvoResistiu)
            }
        }
    }

    /** Valor de resistência do alvo (MA-3a): atributo indicado + Abascanto embutido; combinadas pegam o maior. */
    private fun resistenciaDoAlvo(alvo: Combatente, resist: ResistenciaMagia): Int {
        fun valor(a: AtributoResistencia): Int = when (a) {
            AtributoResistencia.HT -> alvo.stats?.ht ?: 10
            AtributoResistencia.IQ, AtributoResistencia.VONTADE, AtributoResistencia.VONTADE_OU_PERICIA ->
                alvo.stats?.iq ?: 10 // Vontade ~ IQ para o NPC (não há campo de Vontade separado)
            AtributoResistencia.DX -> alvo.stats?.dx ?: 10
            AtributoResistencia.ST -> alvo.stats?.st ?: 10
            else -> alvo.stats?.ht ?: 10 // MÁGICA/COMPOSTA/ESPECIAL → fallback HT (delegado ao Mestre)
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
        val nhDefensor = maxOf(alvo.stats?.armaNh ?: 10, alvo.stats?.dx ?: 10)
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
        val defendeu = acertou && atk.critico != CriticoRules.ResultadoCritico.DECISIVO && CombatResolver.defesaBemSucedida(defValor, defSoma)
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
        val stNpc = npc.stats?.st ?: 10
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
        val stNpc = npc.stats?.st ?: 10
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
        val stNpc = (npc.stats?.st ?: 10) + 3
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
        val captor = captores.maxByOrNull { it.stats?.st ?: 10 }
        if (captor == null) {
            heroi.condicoes.remove(Condicao.AGARRADO); heroi.condicoes.remove(Condicao.IMOBILIZADO)
            return "🤸 Não há mais ninguém te segurando — você se solta.".also { log += it }
        }
        val imob = Condicao.IMOBILIZADO in heroi.condicoes
        var bonusCaptor = if (imob) 10 else 5
        if (Condicao.ATORDOADO in captor.condicoes) bonusCaptor -= 4
        val stCaptor = (captor.stats?.st ?: 10) + bonusCaptor
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
        val npcVal = maxOf(alvo.stats?.st ?: 10, alvo.stats?.dx ?: 10)
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
        return override ?: NpcCombatBrain.decidir(npc, encounter, alvoId = heroi.id, random = random)
    }

    /** true quando a intenção do NPC é um ataque que atinge o herói → a UI deve pedir "Defenda-se!". */
    fun intencaoAtacaHeroi(intencao: NpcCombatBrain.IntencaoNpc): Boolean =
        intencao.alvoId == heroi.id &&
            (intencao.manobra == Manobra.ATAQUE || intencao.manobra == Manobra.ATAQUE_TOTAL ||
                intencao.manobra == Manobra.MOVER_E_ATACAR || intencao.manobra == Manobra.AGARRAR)

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
            esquivaBase = heroiPerfil.esquiva - bdRemovido - reducaoCambaleante - penAtordoado - penPreso - penDedicado + modSitDef,
            aparaBase = if (podeAparar) heroiPerfil.apara?.let { it - bdRemovido - penAparaDesarmada - penAtordoado - penPreso - penDedicado + bonusDefApara + modSitDef } else null,
            bloqueioBase = heroiPerfil.bloqueio?.let { it - bdRemovido - penAtordoado - penPreso - penDedicado + bonusDefBloqueio + modSitDef },
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
                InjuryRules.ferir(npc, 1, npc.stats?.ht ?: 10, random)
                log += "😮‍💨 ${npc.nome} sufoca e perde fôlego (−1)."
            }
            val imob = Condicao.IMOBILIZADO in npc.condicoes
            val nv = (if (imob) (npc.stats?.st ?: 10) - 3 else maxOf(npc.stats?.st ?: 10, npc.stats?.dx ?: npc.dx))
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
                        val stN = npc.stats?.st ?: 10; val rn = rolar3d6()
                        val stH = heroiPerfil.st; val rh = rolar3d6()
                        val ok = vencaDisputaRapida(stN, rn, stH, rh)
                        log += "  └ arma no caminho (AM p.101): Disputa de ST — ${npc.nome} $stN rolou $rn vs você $stH rolou $rh → ${if (ok) "ele passa" else "ele NÃO passa"}."
                        ok
                    } else if (regra.testeVontadeMod != null) {
                        val vontade = (npc.stats?.iq ?: 8) + regra.testeVontadeMod!!
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

        if (!intencaoAtacaHeroi(intencao)) {
            log += "• ${npc.nome}: ${intencao.manobra.rotulo} (${intencao.motivo})."
            return AtaqueResultado(false, false, 0, false, log.last())
        }

        val stats = npc.stats ?: return AtaqueResultado(false, false, 0, false, "${npc.nome} sem stats de ataque.")
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
        // Choque (Lote 382): expira ao fim do turno de quem agiu (valeu só no turno seguinte ao ferimento).
        anterior.choquePendente = 0
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
        tickSangramentoNoTurno(prox)
        return prox
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
        val ht = if (c.ehHeroi) heroiPerfil.ht else (c.stats?.ht ?: 10)
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

    /** Esquiva de um NPC = Velocidade Básica + 3 (MB p.374); Vel.Básica pela metade se cambaleante (MB p.380). */
    /** Penalidade de defesa por atordoamento (Lote 393, MB p.364): todas as defesas ativas a −4. */
    private fun penDefesaAtordoado(c: Combatente): Int = if (Condicao.ATORDOADO in c.condicoes) 4 else 0

    private fun esquivaNpc(npc: Combatente): Int {
        val velB = if (npc.cambaleante) npc.velocidadeBasica / 2 else npc.velocidadeBasica
        return floor(velB).toInt() + 3 - penDefesaAtordoado(npc)
    }

    /** Melhor defesa de um NPC: Esquiva (Vel.Básica+3) vs Aparar (NH/2+3, só corpo-a-corpo); −4 se atordoado. */
    private fun melhorDefesaNpc(npc: Combatente): Pair<CombatResolver.TipoDefesa, Int> {
        // Imobilizado (Lote 411, MB p.371): indefeso — não tem defesa ativa.
        if (Condicao.IMOBILIZADO in npc.condicoes) return CombatResolver.TipoDefesa.ESQUIVA to 0
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

/** Defesas do herói — o controller extrai da ficha; mantém a sessão pura. (Lote 368: só defesa.) */
data class HeroiPerfilCombate(
    val esquiva: Int,
    val apara: Int? = null,
    val bloqueio: Int? = null,
    val ht: Int = 10,
    val rd: Int = 0,
    /** Lote 380: BD do escudo JÁ embutido em esquiva/apara/bloqueio — guardado à parte para poder
     *  REMOVÊ-LO quando o escudo não conta (arma de 2 mãos sem mão livre, ou ataque de arma de fogo; MB p.375). */
    val bonusEscudo: Int = 0,
    /** Lote 381: Modificador de Tamanho (MT) do herói — somado ao acerto quando um NPC atira NELE (MB p.549). */
    val modificadorTamanho: Int = 0,
    /** Lote 386: ST e DX do herói — para as Disputas Rápidas de luta agarrada (Agarrar/Derrubar, MB p.370/371). */
    val st: Int = 10,
    val dx: Int = 10,
    /** Lote 395: Vontade — teste para não perder a pontaria (Apontar) ao ser ferido (MB p.364). */
    val vontade: Int = 10,
    /** Lote 410: dano por GdP (golpe de ponta/empurrão) do herói, p/ o Empurrão (MB p.371). */
    val danoGdP: String = "1d-2",
    /** Lote 414: NH em Acrobacia (null se o herói não tem) — p/ a Esquiva Acrobática (MB p.377). */
    val acrobacia: Int? = null
)

/**
 * Um ataque utilizável do herói (Lote 368): arma empunhada + perícia. O jogador ESCOLHE qual usar.
 * @param aDistancia true para arma de fogo/arremesso (defesa do alvo só por Esquiva; sofre penal. de distância).
 * @param precisao Acc da arma (bônus ao Apontar — usado no lote de manobras).
 */
/** Comportamento de Aparar da arma (coluna Aparar, MB p.270): normal / esgrima (E) / desbalanceada (D) / não. */
enum class ApararTipo { NORMAL, ESGRIMA, DESBALANCEADA, NAO }

data class AtaqueHeroi(
    val rotulo: String,          // ex.: "Revólver (Pistola)"
    val nh: Int,
    val danoExpr: String,        // expressão já resolvida por ST, ex.: "2d-1 pa+"
    val tipo: DanoTipo,
    val aDistancia: Boolean = false,
    val alcance: Int = 1,        // à distância: alcance Máximo (m). Além disso, não acerta.
    val precisao: Int = 0,       // Acc — bônus ao Apontar (MB p.364)
    val meioDano: Int = 0,       // à distância: 1/2D (m). A partir daí, dano pela metade. 0 = sempre cheio.
    val magnitude: Int = 0,      // Bulk — penalidade no Avançar e Atacar à distância (MB p.271)
    val apararTipo: ApararTipo = ApararTipo.NORMAL,
    val cadenciaTiro: Int = 1,   // CdT/RoF — tiros por ataque (MB p.373). >=2 permite rajada.
    val recuo: Int = 1,          // Rco/Rcl — controla quantos tiros da rajada acertam (MB p.374).
    val duasMaos: Boolean = false, // Lote 380: ocupa as duas mãos → sem mão livre p/ o escudo (MB p.375).
    val desarmado: Boolean = false, // Lote 384: ataque desarmado (usa a Tabela de Erro Crítico desarmada).
    val aparaMarcial: Boolean = false, // Lote 391: apara desarmada por Caratê/Judô → sem o −3 vs armas (MB p.376).
    val armaDeFogo: Boolean = false, // Lote 395: arma de fogo → pode "firmar" ao Apontar (+1 Acc, MB p.364).
    val stMinimo: Int = 0, // Lote 398: ST mínima da arma — desbalanceada fica despreparada se ST < 1,5× isto (MB p.270).
    val temPericia: Boolean = true
)

/** Defesa escolhida pelo jogador no card "Defenda-se!" (tipo + valor final + 3d6 rolado). */
data class DefesaHeroi(
    val tipo: CombatResolver.TipoDefesa,
    val valorFinal: Int,
    val soma: Int,
    val recuo: Boolean = false, // Lote 389: a defesa veio com Retirada (recuar um passo) — marca 1×/turno (MB p.377)
    val jogarSeAoChao: Boolean = false, // Lote 404: Esquiva e Queda — após defender, o herói fica deitado (MB p.377)
    val acrobatica: Boolean = false // Lote 414: Esquiva Acrobática — testa Acrobacia (+2/−2) antes da esquiva (MB p.377)
)

enum class ResultadoCombate { VITORIA, DERROTA, FUGA }
