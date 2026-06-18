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

    // Apontar (Lote 373): mira numa arma à distância → +Precisão (Acc) no próximo tiro ao mesmo alvo.
    private var apontarAlvoId: String? = null
    private fun limparApontar() { apontarAlvoId = null }

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

    /** Início de uma ação do herói: zera as flags do turno anterior (desbalanceada + sem-defesa/sem-aparar). */
    private fun inicioAcaoHeroi() { atacouDesbalanceada = false; heroiSemDefesaAtiva = false; heroiSemAparar = false }

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
        inicioAcaoHeroi()
        val alvo = inimigos.firstOrNull { it.id == alvoId && it.vivo }
            ?: return AtaqueResultado(false, false, 0, false, "Alvo inválido ou já fora de combate.").also { log += it.texto }
        golpeForaDeAlcance(ataque, alvo)?.let { return it }
        val r = resolverGolpeHeroi(ataque, alvo, manobra, local, ataqueTotalModo)
        limparAvaliar(); limparApontar(); limparFinta() // bônus de Avaliar/Mira consumidos neste ataque
        // Arma desbalanceada: quem atacou com ela não pode aparar até o próximo turno (MB p.270).
        if (!ataque.aDistancia && ataque.apararTipo == ApararTipo.DESBALANCEADA) atacouDesbalanceada = true
        // Ataque Total: sem defesa ativa até o próximo turno (MB p.366).
        if (manobra == Manobra.ATAQUE_TOTAL) heroiSemDefesaAtiva = true
        verificarFim()
        return r
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
        rotuloModAdicional: String = ""
    ): AtaqueResultado {
        val dist = encounter.distancia(alvo)
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
                val pen = penalidadeDistancia(dist)
                if (pen != 0) add(CombatActions.ComponenteMod("distância ${dist}m", pen))
                // Apontar no turno anterior ao mesmo alvo → soma a Precisão (Acc) da arma (MB p.364).
                if (apontarAlvoId == alvo.id && ataque.precisao != 0)
                    add(CombatActions.ComponenteMod("mira (Acc)", ataque.precisao))
                bonusCadenciaTiro(tiros).let { if (it != 0) add(CombatActions.ComponenteMod("rajada ${tiros} tiros", it)) }
            } else if (avaliarAlvoId == alvo.id && avaliarStacks > 0) {
                // Avaliar só vale corpo-a-corpo, contra o alvo avaliado, no ataque seguinte (MB p.365).
                add(CombatActions.ComponenteMod("avaliar", avaliarStacks))
            }
            if (modAdicional != 0) add(CombatActions.ComponenteMod(rotuloModAdicional.ifBlank { "mod" }, modAdicional))
        }
        val atk = CombatActions.resolverAtaque(
            nhBaseArma = ataque.nh, manobra = manobra, postura = heroi.postura,
            local = local, visibilidade = Visibilidade.NORMAL, ataqueTotalModo = ataqueTotalModo,
            aDistancia = ataque.aDistancia, modsExtra = modsExtra,
            magnitudeArma = if (ataque.aDistancia) ataque.magnitude else null, random = random
        )
        // Contra ataque à distância o alvo só Esquiva; corpo-a-corpo usa a melhor defesa.
        val (defTipo, defValor) = if (ataque.aDistancia)
            CombatResolver.TipoDefesa.ESQUIVA to esquivaNpc(alvo) else melhorDefesaNpc(alvo)
        // Finta (Lote 383, MB p.366): a margem da finta contra este alvo reduz a defesa dele NESTE golpe.
        val penFinta = if (alvo.id == fintaAlvoId) fintaPenalidade else 0
        val defValorFinal = (defValor - penFinta).coerceAtLeast(0)
        val defSoma = rolar3d6()
        // Além de 1/2D, o dano cai pela metade (MB p.270) — aplica no dado básico antes de RD.
        val meioDano = ataque.aDistancia && ataque.meioDano > 0 && dist >= ataque.meioDano
        var danoBasico = rolarDano(ataque.danoExpr, random) + bonusDanoForte(manobra, ataqueTotalModo)
        var rdAlvo = alvo.stats?.rd ?: 0
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
            surpresa = false, danoBaseRolado = danoBruto, danoTipo = ataque.tipo,
            local = local, rdLocal = rdAlvo, randomFerimento = random, forcarFerimentoGrave = forcaGrave,
            tolerancia = alvo.stats?.tolerancia ?: ToleranciaFerimentos.NORMAL
        )
        if (penFinta > 0) log += "  └ finta: a defesa de ${alvo.nome} cai −$penFinta neste golpe (${defValor}→${defValorFinal})."
        log += narrarTroca("Você", alvo.nome, ataque.rotulo.substringBefore(" (").trim(), ataque.aDistancia, atk, defTipo, troca, local, ataque.tipo)
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
        val txt = if (manobra == Manobra.MUDAR_POSTURA) "🧍 Você muda para ${heroi.postura.rotulo}."
            else "🛡️ Você: ${manobra.rotulo}."
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

    /** Apontar (MB p.364): mira numa arma à distância → +Precisão (Acc) no próximo tiro ao alvo. */
    fun heroiApontar(alvoId: String): String {
        inicioAcaoHeroi()
        apontarAlvoId = alvoId
        limparAvaliar(); limparFinta()
        val nome = inimigos.firstOrNull { it.id == alvoId }?.nome ?: "o alvo"
        val txt = "🎯 Você mira em $nome (+Precisão da arma no próximo tiro)."
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
     * Posturas para as quais o herói PODE mudar agora (MB p.365): de deitado não se levanta direto —
     * só vai para ajoelhado/sentado/rastejando antes de ficar em pé.
     */
    fun posturasAlcancaveis(): List<Postura> = when (heroi.postura) {
        Postura.DEITADO -> listOf(Postura.RASTEJANDO, Postura.SENTADO, Postura.AJOELHADO)
        else -> Postura.values().filter { it != heroi.postura }
    }

    /** Herói se move até [metros] (clamp no Deslocamento) aproximando/afastando do alvo (ou de todos). */
    fun heroiMove(alvoId: String? = null, afastar: Boolean = false, metros: Int = Int.MAX_VALUE): String {
        inicioAcaoHeroi()
        val passo = metros.coerceIn(1, heroi.deslocamentoEfetivo.coerceAtLeast(1)) // metade se cambaleante (MB p.380)
        val alvos = alvoId?.let { id -> inimigos.filter { it.id == id } } ?: inimigosVivos
        alvos.forEach { encounter.moverEmRelacaoAoHeroi(it.id, if (afastar) passo else -passo) }
        limparAvaliar(); limparApontar(); limparFinta()
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
    fun opcoesDefesaHeroi(
        armaPronta: AtaqueHeroi? = null,
        recuo: Boolean = false,
        defesaTotalEm: CombatResolver.TipoDefesa? = null,
        contraArmaDeFogo: Boolean = false
    ): List<CombatResolver.OpcaoDefesa> {
        // Após um Ataque Total o herói não tem NENHUMA defesa ativa até o próximo turno (MB p.366).
        if (heroiSemDefesaAtiva) return emptyList()
        val tipoAparar = armaPronta?.apararTipo ?: ApararTipo.NORMAL
        val ranged = armaPronta?.aDistancia == true
        // Aparar indisponível: arma à distância, arma "Não", desbalanceada já usada para atacar, ou Mover e
        // Atacar no turno anterior (que permite só Esquiva/Bloqueio, MB p.367/270).
        val podeAparar = !ranged && tipoAparar != ApararTipo.NAO && !heroiSemAparar &&
            !(tipoAparar == ApararTipo.DESBALANCEADA && atacouDesbalanceada)
        // BD do escudo (MB p.375): só vale com o escudo PREPARADO — uma mão livre (arma de 2 mãos não tem) —
        // e NÃO contra armas de fogo. Quando não vale, removemos o BD que já vem embutido nas defesas da ficha.
        val semMaoParaEscudo = armaPronta?.duasMaos == true
        val bdRemovido = if (!semMaoParaEscudo && !contraArmaDeFogo) 0 else heroiPerfil.bonusEscudo
        // Cambaleante (MB p.380): com < 1/3 do PV, a Vel.Básica cai à metade → a Esquiva também.
        val reducaoCambaleante = if (heroi.cambaleante)
            floor(heroi.velocidadeBasica).toInt() - floor(heroi.velocidadeBasica / 2).toInt() else 0
        return CombatResolver.opcoesDefesa(
            esquivaBase = heroiPerfil.esquiva - bdRemovido - reducaoCambaleante,
            aparaBase = if (podeAparar) heroiPerfil.apara?.let { it - bdRemovido } else null,
            bloqueioBase = heroiPerfil.bloqueio?.let { it - bdRemovido },
            defesasUsadas = heroi.defesasUsadas, recuo = recuo, defesaTotalEm = defesaTotalEm,
            esgrima = tipoAparar == ApararTipo.ESGRIMA
        )
    }

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
                val passo = npc.deslocamentoEfetivo.coerceAtLeast(1) // metade se cambaleante (MB p.380)
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
        val modsNpc: List<CombatActions.ComponenteMod> = buildList {
            // Choque (Lote 382, MB p.419): PV perdidos no turno anterior penalizam o acerto do NPC.
            InjuryRules.penalidadeChoque(npc.choquePendente, npc.pvMax).let {
                if (it != 0) add(CombatActions.ComponenteMod("choque", it))
            }
            if (intencao.aDistancia) {
                // Atirando NO herói: soma o MT do herói (alvo) ao acerto (MB p.549).
                if (heroiPerfil.modificadorTamanho != 0)
                    add(CombatActions.ComponenteMod("tamanho do alvo (MT)", heroiPerfil.modificadorTamanho))
                val pen = penalidadeDistancia(encounter.distancia(npc))
                if (pen != 0) add(CombatActions.ComponenteMod("distância", pen))
            }
        }
        val atk = CombatActions.resolverAtaque(
            nhBaseArma = stats.armaNh, manobra = intencao.manobra, postura = npc.postura,
            local = intencao.local, visibilidade = Visibilidade.NORMAL,
            aDistancia = intencao.aDistancia, modsExtra = modsNpc, random = random
        )
        // Sem escolha de defesa (herói atordoado/sem opção) → só Esquiva passiva da ficha.
        val def = defesaHeroi ?: DefesaHeroi(CombatResolver.TipoDefesa.ESQUIVA, heroiPerfil.esquiva, rolar3d6())
        var danoBasicoNpc = rolarDano(stats.armaDano, random) + bonusDanoForte(intencao.manobra, AtaqueTotalModo.FORTE)
        var rdHeroiAlvo = heroiPerfil.rd
        var forcaGraveNpc = false
        // Golpe Fulminante do NPC (Lote 384, MB p.558).
        if (atk.critico == CriticoRules.ResultadoCritico.DECISIVO) {
            val gf = aplicarGolpeFulminante(danoBasicoNpc, rdHeroiAlvo, stats.armaDano)
            danoBasicoNpc = gf.dano; rdHeroiAlvo = gf.rd; forcaGraveNpc = gf.grave
            log += "  ⭐ Golpe Fulminante de ${npc.nome} — ${gf.nota}"
        }
        // Ataque Total no turno anterior ANULA a defesa do herói até o próximo turno (MB p.366).
        if (heroiSemDefesaAtiva) log += "🛡️ Você está sem defesa ativa (Ataque Total) — resta torcer pelo erro do oponente!"

        val troca = CombatResolver.resolverTroca(
            defensor = heroi, htDefensor = heroiPerfil.ht, ataque = atk,
            defesaTipo = def.tipo, defesaValorFinal = def.valorFinal, defesaSoma = def.soma,
            surpresa = heroiSemDefesaAtiva, danoBaseRolado = danoBasicoNpc, danoTipo = tipoDano(stats.armaTipo),
            local = intencao.local, rdLocal = rdHeroiAlvo, randomFerimento = random, forcarFerimentoGrave = forcaGraveNpc
        )
        // marca a defesa usada (bloqueio/recuo 1×/turno; aparas extras cumulativas)
        registrarDefesaUsada(def.tipo)
        log += narrarTroca(npc.nome, "você", stats.armaNome, intencao.aDistancia, atk, def.tipo, troca, intencao.local, tipoDano(stats.armaTipo))
        // Erro Crítico do NPC (Lote 384, MB p.557): o oponente tropeça no próprio golpe.
        if (atk.critico == CriticoRules.ResultadoCritico.FALHA_CRITICA)
            aplicarErroCritico(npc, stats.ht, stats.armaDano, stats.armaNome.isBlank(), npc.nome)
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

    /** Esquiva de um NPC = Velocidade Básica + 3 (MB p.374); Vel.Básica pela metade se cambaleante (MB p.380). */
    private fun esquivaNpc(npc: Combatente): Int {
        val velB = if (npc.cambaleante) npc.velocidadeBasica / 2 else npc.velocidadeBasica
        return floor(velB).toInt() + 3
    }

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
    val modificadorTamanho: Int = 0
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
    val temPericia: Boolean = true
)

/** Defesa escolhida pelo jogador no card "Defenda-se!" (tipo + valor final + 3d6 rolado). */
data class DefesaHeroi(
    val tipo: CombatResolver.TipoDefesa,
    val valorFinal: Int,
    val soma: Int
)

enum class ResultadoCombate { VITORIA, DERROTA, FUGA }
