package com.gurps.ficha.domain.combat.subsistemas

import com.gurps.ficha.domain.combat.CombatSession
import com.gurps.ficha.domain.combat.Combatente
import com.gurps.ficha.domain.combat.Condicao
import com.gurps.ficha.domain.combat.InjuryRules
import com.gurps.ficha.domain.magic.BuffAplicado
import com.gurps.ficha.domain.magic.MagiaAtivaNoCombate
import com.gurps.ficha.domain.magic.MagicActive
import com.gurps.ficha.domain.magic.MagicMechanics
import com.gurps.ficha.domain.magic.TipoDuracao
import com.gurps.ficha.domain.roll.CriticoRules
import kotlin.random.Random

/**
 * Lote MOTOR-4: **subsistema de EFEITOS MÁGICOS ATIVOS** (buffs, mágicas duradouras, manutenção,
 * dano por turno), extraído do `CombatSession`.
 *
 * ## Por que é um delegate, e não "mais um arquivo"
 * É a mesma ideia das ZONAS (MOTOR-1): tem ESTADO PRÓPRIO — a lista [ativas] e a fila de
 * [manutencaoPendente] — e um CICLO DE VIDA fechado em si mesmo:
 *
 *  registrar → (a cada turno do herói) tique de dano + relógio de manutenção/expiração → dissipar.
 *
 * Além disso guarda três regras sutis que só se testam bem isoladas — exatamente o motivo de tirar
 * daqui:
 *  - **Não acumula** (Magia p.9, MEC-29): duas versões da mesma mágica no mesmo alvo → fica só a
 *    mais forte; a fraca é revertida.
 *  - **Regra da estreia** (MEC-22): mágica que fere a cada turno NÃO fere no turno em que foi lançada.
 *  - **Abalo de concentração** (Magia p.7, MEC-26): ferido/atordoado testa Vontade−3 e pode congelar
 *    ou desfazer a mágica que exige concentração.
 *
 * ## Como fala com o motor
 * O que ele precisa e mora no `CombatSession` (o log, o RNG, a lista de combatentes, o herói e seus
 * atributos, o "reavaliar fim") entra por LAMBDA no construtor — a lista de acoplamento fica curta e
 * visível, e o subsistema continua testável sozinho.
 *
 * A REVERSÃO do buff é o ponto delicado: expirar ou dissipar tem que tirar da ficha do alvo
 * exatamente o `BuffAplicado` que a mágica pôs (por identidade `===`), senão o bônus fica para
 * sempre. Isso vive todo aqui dentro ([removerBuffDe]).
 *
 * Nada de comportamento mudou: é o `registrarMagiaAtiva`/`tiquePorTurnoDasMagias`/`abaloDeConcentracao`/
 * etc. original, só de lugar. A rede de invariantes (SIM-1) e o `MagicCombatTest` provam.
 */
class EfeitosMagicosDelegate(
    /** O log do combate (referência compartilhada com o motor — as linhas caem no mesmo feed). */
    private val log: MutableList<String>,
    /** MESMO RNG do motor: o tique de dano tem que consumir a sequência igual. */
    private val random: Random,
    /** Combatentes agora (o motor tem o encounter; o delegate só pede a lista). */
    private val combatentes: () -> List<Combatente>,
    /** O herói — para PF de manutenção, choque pendente e condições no abalo de concentração. */
    private val heroi: () -> Combatente,
    /** HT EFETIVO do herói (vem do perfil, não da ficha crua) — vítima do tique de dano. */
    private val heroiHt: () -> Int,
    /** Vontade EFETIVA do herói — o teste do abalo de concentração é Vontade−3. */
    private val heroiVontade: () -> Int,
    /** Reavalia fim de combate depois que o tique fere alguém (a vítima pode cair). */
    private val verificarFim: () -> Unit,
) {
    /** Mágicas TEMPORÁRIAS/DURADOURAS ativas (o efeito é narrado; aqui rastreia manutenção/expiração). */
    var ativas: List<MagiaAtivaNoCombate> = emptyList()
        private set

    /** Mágicas do herói cuja manutenção venceu e ESPERAM a decisão dele (manter é opcional em GURPS). */
    var manutencaoPendente: List<CombatSession.ManutencaoPendente> = emptyList()
        private set

    /** Atalho para os guardas do motor ("só roda o relógio se houver mágica ativa"). */
    fun temAtivas(): Boolean = ativas.isNotEmpty()

    private fun rolar3d6(): Int = (1..3).sumOf { random.nextInt(1, 7) }

    /**
     * Lote MA-3d-4: registra uma mágica ativa (o controller chama após uma conjuração com duração).
     * O tique de manutenção roda em `avancarUmSegundo`.
     */
    fun registrar(
        nome: String, operadorId: String, alvoId: String?, duracaoSeg: Int,
        custoManutencaoSeg: Int, duracao: TipoDuracao, exigeConcentracao: Boolean,
        buff: BuffAplicado? = null,
        mecanica: com.gurps.ficha.domain.magic.MagiaMecanica? = null,
    ) {
        // MEC-2: aplica o buff no alvo AGORA (a lista dele passa a somar no perfil efetivo).
        val aplicado = buff?.takeIf { !it.soNarrado }
        if (aplicado != null) {
            val alvo = combatentes().firstOrNull { it.id == aplicado.alvoId }
            if (alvo != null) {
                // Lote MEC-29 (C7, Magia p.9): mágica de efeito variável no mesmo alvo mais de uma vez
                // → só a MAIS PODEROSA vale; não se acumulam. (Cura/dano/permanente são exceção e não
                // passam por aqui: só buff chega neste ponto.) Sem isto, Escudo 3× somava +3 sobre +3.
                val anterior = ativas.firstOrNull {
                    it.magiaId.equals(nome, ignoreCase = true) && it.buff?.alvoId == aplicado.alvoId
                }
                val forcaAnterior = anterior?.buff?.let { forcaDoBuff(it) } ?: -1
                if (forcaAnterior >= forcaDoBuff(aplicado)) {
                    log += "✨ ${alvo.nome} já está sob $nome igual ou mais forte — as mágicas não se acumulam (Magia p.9)."
                    return
                }
                if (anterior != null) {
                    removerBuffDe(anterior)
                    ativas = ativas - anterior
                    log += "✨ $nome substitui a versão mais fraca em ${alvo.nome} (não acumula)."
                }
                alvo.buffs.add(aplicado)
                log += "✨ ${alvo.nome}: $nome — ${descreverBuff(aplicado)}."
            }
        }
        ativas = ativas + MagiaAtivaNoCombate(
            magiaId = nome, operadorId = operadorId, alvoId = alvoId, energiaInvestida = 0,
            custoManutencaoSeg = custoManutencaoSeg, segundosParaProximaCobranca = duracaoSeg.coerceAtLeast(1),
            duracaoTotalSeg = duracaoSeg.coerceAtLeast(1), duracao = duracao, exigeConcentracao = exigeConcentracao,
            buff = aplicado,
            mecanica = mecanica,
            // MEC-22: regra da estreia — não fere no turno em que foi aplicada.
            pularPrimeiroTique = MagicMechanics.temTiquePorTurno(mecanica),
        )
        val manut = if (custoManutencaoSeg > 0) "manutenção $custoManutencaoSeg PF a cada ${duracaoSeg}s" else "sem custo de manutenção"
        log += "✨ $nome fica ATIVA ($manut)."
    }

    /**
     * Lote MEC-6: buff de UM ÚNICO USO (Aumentar Força/Destreza/Vitalidade — vale para um teste).
     * NÃO é mágica ativa: sem manutenção nem relógio. Entra na lista do alvo (o perfil efetivo já o
     * enxerga) e sai ao fim da PRÓXIMA ação do dono — tratado em `avancarTurno`.
     */
    fun aplicarBuffDeUmUso(nome: String, buff: BuffAplicado) {
        if (buff.soNarrado) return
        val alvo = combatentes().firstOrNull { it.id == buff.alvoId } ?: return
        alvo.buffs.add(buff)
        log += "✨ ${alvo.nome}: $nome — ${descreverBuff(buff)} (vale para a próxima ação)."
    }

    /** Lote MEC-2: descreve os deltas de um buff em texto factual (o Narrador transforma em prosa). */
    private fun descreverBuff(b: BuffAplicado): String {
        val partes = buildList {
            if (b.st != 0) add("ST ${sinal(b.st)}"); if (b.dx != 0) add("DX ${sinal(b.dx)}")
            if (b.ht != 0) add("HT ${sinal(b.ht)}"); if (b.rd != 0) add("RD ${sinal(b.rd)}")
            // Lote P3-1
            if (b.iq != 0) add("IQ ${sinal(b.iq)}")
            if (b.vontade != 0) add("Vontade ${sinal(b.vontade)}")
            if (b.esquiva != 0) add("Esquiva ${sinal(b.esquiva)}")
            if (b.deslocamentoFixo != null) add("Deslocamento ${b.deslocamentoFixo}")
            else if (b.deslocamento != 0) add("Deslocamento ${sinal(b.deslocamento)}")
            if (b.danoArma != 0) add("arma ${sinal(b.danoArma)} de dano")
            if (b.penalidadeAtacantes != 0) add("−${b.penalidadeAtacantes} a quem o atacar")
        }
        return if (partes.isEmpty()) (b.rotulo.ifBlank { "efeito narrado" }) else partes.joinToString(", ")
    }

    private fun sinal(n: Int) = if (n >= 0) "+$n" else "$n"

    /**
     * Lote MEC-29: "a mais poderosa" de duas versões da MESMA mágica. Soma os efeitos numéricos —
     * compara Escudo +2 contra Escudo +4 sem precisar saber qual campo cada magia usa.
     */
    private fun forcaDoBuff(b: BuffAplicado): Int =
        kotlin.math.abs(b.rd) + kotlin.math.abs(b.esquiva) + kotlin.math.abs(b.bd) +
            kotlin.math.abs(b.st) + kotlin.math.abs(b.dx) + kotlin.math.abs(b.ht) +
            kotlin.math.abs(b.iq) + kotlin.math.abs(b.vontade) +   // Lote P3-1
            kotlin.math.abs(b.deslocamento) + kotlin.math.abs(b.danoArma) +
            kotlin.math.abs(b.penalidadeAtacantes) + (b.deslocamentoFixo ?: 0)

    /**
     * Lote MEC-23: o jogador decidiu sobre uma manutenção pendente. [manter] = paga o PF e a mágica
     * segue; senão ela ACABA e o gasto para. Sem PF suficiente, a mágica cai (não há como pagar).
     */
    fun resolverManutencao(magiaId: String, manter: Boolean) {
        val p = manutencaoPendente.firstOrNull { it.magiaId == magiaId } ?: return
        manutencaoPendente = manutencaoPendente - p
        if (!manter) {
            log += "✋ Você deixa $magiaId acabar — o gasto de manutenção para."
            dissipar(magiaId)
            return
        }
        val heroiAtual = heroi()
        if (heroiAtual.pfAtual < p.custoPf) {
            log += "😮‍💨 Fadiga insuficiente para manter $magiaId (precisa de ${p.custoPf} PF) — a mágica acaba."
            dissipar(magiaId)
            return
        }
        heroiAtual.pfAtual -= p.custoPf
        log += "✨ Você mantém $magiaId (−${p.custoPf} PF)."
    }

    /**
     * Lote MEC-2: dissipa uma mágica ativa pelo nome, REVERTENDO o buff que ela aplicou. Buff
     * permanente sai por aqui (não expira sozinho).
     */
    fun dissipar(magiaId: String): Boolean {
        val alvo = ativas.firstOrNull { it.magiaId.equals(magiaId, ignoreCase = true) } ?: return false
        removerBuffDe(alvo)
        ativas = ativas - alvo
        log += "✨ $magiaId é dissipada."
        return true
    }

    /** Tira da lista do combatente exatamente o BuffAplicado que esta mágica pôs (por identidade). */
    private fun removerBuffDe(m: MagiaAtivaNoCombate) {
        val b = m.buff ?: return
        combatentes().firstOrNull { it.id == b.alvoId }?.buffs?.removeIf { it === b }
    }

    /**
     * Lote MEC-26 (C4/Magia p.7): abalo de concentração. Ferido ou atordoado, cada mágica do herói que
     * EXIGE concentração testa Vontade−3: falha crítica a DESFAZ; falha simples a CONGELA neste turno
     * (retorna o conjunto de congeladas); sucesso mantém. Roda ANTES do reset do choque em `avancarTurno`.
     */
    fun abaloDeConcentracao(): Set<String> {
        val comConcentracao = ativas.filter { it.exigeConcentracao && it.operadorId == "heroi" }
        if (comConcentracao.isEmpty()) return emptySet()
        val heroiAtual = heroi()
        // O gatilho: levou dano desde o próprio turno anterior, ou está atordoado.
        val feriu = heroiAtual.choquePendente > 0
        val atordoado = Condicao.ATORDOADO in heroiAtual.condicoes
        if (!feriu && !atordoado) return emptySet()

        val congeladas = mutableSetOf<String>()
        val derrubadas = mutableListOf<MagiaAtivaNoCombate>()
        val motivo = if (atordoado) "atordoado" else "ferido"
        for (m in comConcentracao) {
            val alvo = heroiVontade() - 3
            val rol = rolar3d6()
            when {
                CriticoRules.classificar(rol, alvo) == CriticoRules.ResultadoCritico.FALHA_CRITICA -> {
                    derrubadas.add(m)
                    log += "💥 $motivo — FALHA CRÍTICA de concentração (Vontade−3 $alvo, rolou $rol): ${m.magiaId} se DESFAZ."
                }
                rol > alvo -> {
                    congeladas.add(m.magiaId)
                    log += "😖 $motivo — você perde a concentração (Vontade−3 $alvo, rolou $rol): ${m.magiaId} fica CONGELADA neste turno."
                }
                else -> log += "🧘 Mesmo $motivo, você mantém a concentração em ${m.magiaId} (Vontade−3 $alvo, rolou $rol)."
            }
        }
        if (derrubadas.isNotEmpty()) {
            derrubadas.forEach { removerBuffDe(it) }
            ativas = ativas.filter { a -> derrubadas.none { it === a } }
        }
        return congeladas
    }

    /**
     * Lote MEC-22: mágicas que FEREM A CADA TURNO (Morte Candente/Putrefata). A vítima testa HT; falha
     * → leva dano (RD não protege, regra explícita); sucesso decisivo pode QUEBRAR a mágica. [congeladas]
     * (do abalo) não avançam nem ferem neste turno.
     */
    fun tiquePorTurno(congeladas: Set<String> = emptySet()) {
        val comTique = ativas.filter {
            MagicMechanics.temTiquePorTurno(it.mecanica) &&
                // MEC-26: mágica congelada por perda de concentração NÃO avança nem fere neste turno.
                it.magiaId !in congeladas
        }
        if (comTique.isEmpty()) return
        val quebradas = mutableListOf<MagiaAtivaNoCombate>()

        for (ativa in comTique) {
            // Regra da estreia: não fere no turno em que foi aplicada.
            if (ativa.pularPrimeiroTique) {
                ativas = ativas.map {
                    if (it === ativa) it.copy(pularPrimeiroTique = false) else it
                }
                continue
            }
            val vitima = combatentes().firstOrNull { it.id == ativa.alvoId && it.vivo }
            if (vitima == null) { quebradas.add(ativa); continue } // alvo morreu/sumiu → mágica cai
            val mec = ativa.mecanica!!
            // Lote A1-b: Morte Candente/Putrefata dizem que "mortos-vivos não são afetados". A mágica
            // não fica ativa em quem ela não pega — cai fora.
            if (MagicMechanics.naoAfetaTipo(mec, vitima.tipoCriatura.chave)) {
                log += "✨ ${ativa.magiaId} não tem efeito em ${vitima.nome} (${vitima.tipoCriatura.rotulo}) — a mágica se desfaz."
                quebradas.add(ativa); continue
            }
            val atributo = if (vitima.ehHeroi) heroiHt() else vitima.htEfetivo
            val rol = rolar3d6()
            val crit = CriticoRules.classificar(rol, atributo)

            when {
                // Sucesso DECISIVO da vítima quebra a mágica (e não causa dano).
                crit == CriticoRules.ResultadoCritico.DECISIVO && mec.quebraEmSucessoDecisivo -> {
                    log += "✨ ${vitima.nome} resiste DECISIVAMENTE (HT $atributo, rolou $rol) — ${ativa.magiaId} se QUEBRA."
                    quebradas.add(ativa)
                }
                // Sucesso simples: sem dano neste turno.
                rol <= atributo -> {
                    log += "✨ ${vitima.nome} aguenta ${ativa.magiaId} neste turno (HT $atributo, rolou $rol)."
                }
                // Falha: leva dano. RD NÃO protege (regra explícita das duas mágicas).
                else -> {
                    val critFalha = crit == CriticoRules.ResultadoCritico.FALHA_CRITICA
                    val dano = if (critFalha && mec.danoPorTurnoCriticoFixo > 0) mec.danoPorTurnoCriticoFixo
                    else CombatSession.rolarDano(mec.danoPorTurnoExpr!!, random)
                    InjuryRules.ferir(vitima, dano, vitima.htEfetivo, random)
                    val etiqueta = if (critFalha) " (FALHA CRÍTICA)" else ""
                    log += "🔥 ${ativa.magiaId} queima ${vitima.nome} por dentro$etiqueta: $dano de dano " +
                        "(HT $atributo, rolou $rol; RD não protege)" + (if (!vitima.vivo) " — fora de combate!" else "") + "."
                }
            }
        }
        if (quebradas.isNotEmpty()) ativas = ativas.filter { a -> quebradas.none { it === a } }
        verificarFim()
    }

    /**
     * Lote MA-3d-4: fim do turno do herói = 1 segundo de jogo. Cobra a MANUTENÇÃO e EXPIRA as
     * duradouras (MagicActive, Magia p.9-10). As do HERÓI que venceram ficam PENDENTES (manter é
     * opcional — a tela pergunta); as do NPC cobram na hora (não há a quem perguntar). Expirar REVERTE
     * o buff, senão o bônus fica para sempre.
     */
    fun avancarUmSegundo() {
        val res = MagicActive.avancarTurnoSegundos(ativas, 1)
        ativas = res.ativasApos
        // Lote MEC-23: MANTER É OPCIONAL. As do herói ficam pendentes; o NPC segue automático.
        manutencaoPendente = res.venceramManutencao
            .filter { (m, _) -> m.operadorId == "heroi" }
            .map { (m, custo) -> CombatSession.ManutencaoPendente(m.magiaId, custo) }
        res.cobrancasPorOperador.filterKeys { it != "heroi" }.forEach { (id, fp) ->
            val npc = combatentes().firstOrNull { it.id == id } ?: return@forEach
            if (fp > 0) npc.pfAtual = (npc.pfAtual - fp).coerceAtLeast(0)
        }
        // MEC-2: expirar tem que REVERTER o buff — senão o bônus fica para sempre.
        res.expiradas.forEach { exp ->
            removerBuffDe(exp)
            val volta = exp.buff?.let { " — ${it.rotulo.ifBlank { "o efeito" }} se desfaz" } ?: ""
            log += "✨ ${exp.magiaId} termina$volta."
        }
    }

    /**
     * Lote MAG-4: cura que LIMPA condições (Cessar Sangramento, Cessar Paralisia, Restaurar Visão).
     * Remove do alvo cada condição conhecida (e o relógio/escape dela), zera o sangramento interno se
     * limpar SANGRANDO, e restaura [curaPv] PV. Devolve true se limpou/curou algo. É o inverso do
     * imporCondicaoMagica — por isso usa o mesmo mapa canônico `Condicao.deChave`.
     */
    fun removerCondicoes(alvo: Combatente, chaves: List<String>, curaPv: Int, sb: StringBuilder): Boolean {
        val limpas = mutableListOf<Condicao>()
        for (ch in chaves) {
            val cond = Condicao.deChave(ch) ?: continue
            if (alvo.condicoes.remove(cond)) {
                alvo.condicoesTemporarias.remove(cond)
                if (alvo.escapeCondicao?.condicao == cond) alvo.escapeCondicao = null
                if (cond == Condicao.SANGRANDO) alvo.sangramentoAtivo = false
                limpas += cond
            }
        }
        var curou = 0
        if (curaPv > 0 && alvo.pvAtual < alvo.pvMax) {
            curou = minOf(curaPv, alvo.pvMax - alvo.pvAtual)
            alvo.pvAtual += curou
        }
        if (limpas.isEmpty() && curou == 0) return false
        val partes = buildList {
            if (limpas.isNotEmpty()) add(limpas.joinToString(", ") { it.rotulo })
            if (curou > 0) add("+$curou PV")
        }
        sb.append(" ✨ ${alvo.nome}: ${partes.joinToString("; ")} (curado).")
        return true
    }
}
