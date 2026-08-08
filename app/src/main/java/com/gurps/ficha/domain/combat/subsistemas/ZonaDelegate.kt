package com.gurps.ficha.domain.combat.subsistemas

import com.gurps.ficha.domain.rules.DanoTipo
import com.gurps.ficha.domain.rules.ToleranciaFerimentos

import com.gurps.ficha.domain.rules.LocalAtaque

import com.gurps.ficha.domain.combat.Combatente
import com.gurps.ficha.domain.combat.CombatSession
import com.gurps.ficha.domain.combat.HitLocationRules
import com.gurps.ficha.domain.combat.InjuryRules
import com.gurps.ficha.domain.combat.ZonaPersistente
import com.gurps.ficha.domain.magic.MagicMechanics
import kotlin.random.Random

/**
 * Lote MOTOR-1: **subsistema de ZONAS PERSISTENTES** (chuvas de fogo, nuvens, gás), extraído do
 * `CombatSession`, que passou de 3675 linhas e vai crescer muito mais (700 magias + vantagens/
 * perícias/itens pela frente).
 *
 * ## Por que é um delegate, não só "mais um arquivo"
 * As zonas têm ESTADO PRÓPRIO (a lista `zonasAtivas` e o ponto de injeção `ocupantesDaZona`) e um
 * ciclo próprio (registrar → tique a cada intervalo → encolher → dissipar). É um subsistema que
 * fecha em si mesmo. Ao contrário de partir o motor no meio, tirar as zonas inteiras não espalha
 * bagunça: leva o assunto completo para um lugar só.
 *
 * ## Como ele fala com o motor
 * O tique precisa de coisas que moram no `CombatSession` (o log, o RNG, o HT/RD do alvo, o "reavaliar
 * fim de combate"). Em vez de o delegate conhecer o motor inteiro, ele recebe essas peças por
 * LAMBDA no construtor — assim continua testável sozinho e o acoplamento é uma lista curta e visível.
 *
 * Nada de comportamento mudou: o código é o mesmo do `tiqueDasZonas`/`registrarZona`/etc. original,
 * só de lugar. A rede de invariantes (SIM-1) prova isso.
 */
class ZonaDelegate(
    /** O log do combate (referência compartilhada com o motor — as linhas aparecem no mesmo feed). */
    private val log: MutableList<String>,
    /** MESMO RNG do motor: o tique tem que consumir a sequência igual, senão o dano muda. */
    private val random: Random,
    /** Combatentes vivos agora (o motor tem o encounter; o delegate só pede a lista). */
    private val combatentes: () -> List<Combatente>,
    /** Distância de um combatente ao herói (para a ocupação-padrão no modelo de faixas). */
    private val distanciaAoHeroi: (Combatente) -> Int,
    /** HT efetivo do alvo (herói vem do perfil; NPC do bestiário) — para o teste de evitar dano. */
    private val htDoAlvo: (Combatente) -> Int,
    /** RD que vale contra ESTA zona no alvo (trata "ignora"/"ignora_vestida"/normal, herói × NPC). */
    private val rdDaZona: (Combatente, ZonaPersistente) -> Int,
    /** Reavaliar fim de combate após o tique (uma zona pode derrubar o último inimigo). */
    private val aoMudarEstado: () -> Unit,
) {
    var zonasAtivas: List<ZonaPersistente> = emptyList(); private set

    /** 3d6 com o MESMO RNG do motor — idêntico ao `rolar3d6()` do CombatSession. */
    private fun rolar3d6(): Int = (1..3).sumOf { random.nextInt(1, 7) }

    /**
     * Quem está DENTRO da zona agora. Ponto de injeção: o controller substitui pelo cálculo real por
     * hex (a grade). O padrão usa a distância ao herói — aproximação honesta do modelo de faixas.
     *
     * ⚠️ MEC-47 — o herói é caso especial: em faixas `distancia(heroi)` é 0 por definição, então ele
     * caía dentro de QUALQUER zona, inclusive uma que largou longe. Sem grade, vale o DONO: zona do
     * próprio herói o poupa; zona de NPC o pega. Com grade, a posição é real e este padrão nem roda.
     */
    var ocupantesDaZona: (ZonaPersistente) -> List<Combatente> = { z ->
        combatentes().filter {
            it.vivo && distanciaAoHeroi(it) <= z.raioM && !(it.ehHeroi && z.operadorId == it.id)
        }
    }

    fun registrarZona(z: ZonaPersistente) {
        // Lote TOK-10: com duas zonas da MESMA mágica, o log ficava ambíguo. A partir da segunda,
        // cada uma ganha um número.
        val jaTem = zonasAtivas.count { it.nome.equals(z.nome, ignoreCase = true) }
        if (jaTem > 0) z.ordinal = jaTem + 1
        zonasAtivas = zonasAtivas + z
        log += "☁️ ${z.rotulo} cobre a área (raio ${z.raioM}m) por ${z.segRestantes}s — " +
            "${z.danoExpr} a cada ${z.intervaloSeg}s em quem estiver dentro."
        // Lote MEC-47: o operador se poupa na conjuração, mas a nuvem no chão é perigo contínuo. Se
        // ele está lá dentro, avisa AGORA — senão o primeiro tique parece dano do nada.
        if (ocupantesDaZona(z).any { it.ehHeroi }) {
            log += "⚠️ Você está DENTRO da ${z.rotulo} — vai queimar a cada ${z.intervaloSeg}s " +
                "enquanto ficar aí. Saia da área."
        }
    }

    fun limparZonas() { zonasAtivas = emptyList() }

    /**
     * Lote C11 (Magia p.10): a área **encolhe, nunca cresce**. Tentar expandir é recusado e logado —
     * senão parece que o toque não funcionou. Devolve `true` se encolheu.
     */
    fun encolherZona(nomeDaZona: String, novoRaioM: Int): Boolean {
        val z = zonasAtivas.firstOrNull {
            it.rotulo.equals(nomeDaZona, ignoreCase = true) || it.nome.equals(nomeDaZona, ignoreCase = true)
        } ?: return false
        if (novoRaioM >= z.raioM) {
            log += "⚠️ ${z.rotulo} não pode ser EXPANDIDA depois de operada (Magia p.10) — " +
                "está com raio ${z.raioM}m."
            return false
        }
        if (novoRaioM < 1) return false
        val antes = z.raioM
        z.raioM = novoRaioM
        log += "☁️ ${z.rotulo} encolhe de ${antes}m para ${novoRaioM}m — quem ficou de fora deixa de ser atingido."
        return true
    }

    /**
     * Lote TOK-10: existe OUTRA zona da MESMA mágica cobrindo [alvo] que é mais forte? A mesma mágica
     * não acumula (vale a mais forte, por dano máximo; empate → a primeira registrada). Mágicas
     * diferentes somam (fogo + ácido).
     */
    private fun zonaSuplantadaPara(z: ZonaPersistente, alvo: Combatente): Boolean {
        val rivais = zonasAtivas.filter {
            it !== z && it.nome.equals(z.nome, ignoreCase = true) && ocupantesDaZona(it).any { o -> o.id == alvo.id }
        }
        if (rivais.isEmpty()) return false
        val minha = CombatSession.danoMaximo(z.danoExpr)
        return rivais.any { r ->
            val dela = CombatSession.danoMaximo(r.danoExpr)
            dela > minha || (dela == minha && zonasAtivas.indexOf(r) < zonasAtivas.indexOf(z))
        }
    }

    /**
     * Lote MEC-46: corre o relógio (1 turno = 1 segundo), fere quem está dentro quando o intervalo
     * vence, e remove as expiradas. Roda no avanço de turno.
     */
    fun tiqueDasZonas() {
        if (zonasAtivas.isEmpty()) return
        val sobrevivem = mutableListOf<ZonaPersistente>()
        for (z in zonasAtivas) {
            // Lote TOK-9 — estreia consumida no primeiro TURNO (não no primeiro intervalo, senão o
            // Mau Cheiro de 60s pularia seu primeiro tique real).
            val turnoDaConjuracao = !z.estreou
            z.estreou = true
            z.segRestantes -= 1
            z.segAteProximo -= 1
            // O relógio corre; só o DANO deste primeiro segundo é pulado (já saiu na conjuração).
            if (z.segAteProximo <= 0 && !turnoDaConjuracao) {
                z.segAteProximo = z.intervaloSeg.coerceAtLeast(1)
                for (alvo in ocupantesDaZona(z)) {
                    // TOK-10: a mesma mágica não acumula.
                    if (zonaSuplantadaPara(z, alvo)) continue
                    // A1: imune ao elemento não é ferido, mesmo pisando dentro.
                    if (MagicMechanics.imuneAo(z.elementoDano, alvo.imunidades)) {
                        log += "☁️ ${alvo.nome} atravessa ${z.rotulo} sem se ferir — imune a ${z.elementoDano}."
                        continue
                    }
                    // Teste para evitar (Mau Cheiro: HT uma vez por minuto). Sem teste, o dano é certo.
                    if (!z.teste.isNullOrBlank()) {
                        val atributo = htDoAlvo(alvo)
                        val rol = rolar3d6()
                        if (rol <= atributo) {
                            log += "☁️ ${alvo.nome} aguenta ${z.rotulo} (${z.teste} $atributo, rolou $rol)."
                            continue
                        }
                    }
                    val bruto = CombatSession.rolarDano(z.danoExpr, random)
                    val rd = rdDaZona(alvo, z)
                    val tipo = when (z.tipoDano) {
                        "corte" -> DanoTipo.CORT; "perf" -> DanoTipo.PERF; else -> DanoTipo.CONT
                    }
                    val dn = HitLocationRules.aplicarDano(alvo.pvMax, bruto, tipo, LocalAtaque.TORSO, rd,
                        alvo.stats?.tolerancia ?: ToleranciaFerimentos.NORMAL)
                    InjuryRules.ferir(alvo, dn.pvSubtrair, htDoAlvo(alvo), random)
                    // Lote MEC-47: quando é o herói, o log diz que ele está na ZONA DELE.
                    log += if (alvo.ehHeroi) {
                        "☁️ VOCÊ está dentro da ${z.rotulo}: ${z.danoExpr} → ${dn.pvSubtrair} de dano" +
                            (if (!alvo.vivo) " — você caiu!" else ". Saia da área.")
                    } else {
                        "☁️ ${z.rotulo} atinge ${alvo.nome}: ${z.danoExpr} → ${dn.pvSubtrair} de dano" +
                            (if (!alvo.vivo) " — fora de combate!" else ".")
                    }
                }
            }
            if (z.segRestantes > 0) sobrevivem.add(z) else log += "☁️ ${z.rotulo} se dissipa."
        }
        zonasAtivas = sobrevivem
        aoMudarEstado()
    }
}
