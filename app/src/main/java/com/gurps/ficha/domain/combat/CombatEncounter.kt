package com.gurps.ficha.domain.combat

import kotlin.random.Random

/**
 * Lote 359 (Saga B1): encontro de combate — ordem de turnos e legalidade de manobras.
 * Kotlin PURO, determinístico (desempate aleatório por seed). Sem rolagem de dados ainda
 * (isso é B2+); aqui só a sequência de iniciativa e o que cada um PODE fazer.
 *
 * @param combatentes participantes do encontro.
 * @param distanciaAoHeroi distância (metros) de cada combatente ao herói (herói = 0).
 *        Engajamento corpo-a-corpo = distância <= 1 (MB: adjacente).
 * @param seed semente do desempate aleatório de iniciativa (reprodutível).
 */
class CombatEncounter(
    val combatentes: List<Combatente>,
    distanciaAoHeroi: Map<String, Int> = emptyMap(),
    seed: Long = 0L
) {
    private val porId = combatentes.associateBy { it.id }

    /** Distâncias ao herói (mutáveis: a manobra Mover altera a faixa — B7). */
    private val distanciaAoHeroi: MutableMap<String, Int> = distanciaAoHeroi.toMutableMap()

    /** Reposiciona um combatente (clamp em 0; herói é sempre 0). MB: 1 hexágono ≈ 1 m. */
    fun definirDistancia(id: String, metros: Int) {
        if (porId[id]?.ehHeroi == true) return
        distanciaAoHeroi[id] = metros.coerceAtLeast(0)
    }

    /** Aproxima (delta negativo) ou afasta (positivo) do herói, respeitando o mínimo de 0. */
    fun moverEmRelacaoAoHeroi(id: String, delta: Int) {
        val atual = distanciaAoHeroi[id] ?: return
        definirDistancia(id, atual + delta)
    }

    /** Ordem de iniciativa: Velocidade Básica desc, desempate DX desc, depois aleatório (seed). MB p.363. */
    val ordemTurnos: List<String> = run {
        val rnd = Random(seed)
        val tie = combatentes.associate { it.id to rnd.nextInt() }
        combatentes.sortedWith(
            compareByDescending<Combatente> { it.velocidadeBasica }
                .thenByDescending { it.dx }
                .thenBy { tie.getValue(it.id) }
        ).map { it.id }
    }

    var rodadaAtual: Int = 1
        private set
    private var indice: Int = 0

    val combatenteAtual: Combatente get() = porId.getValue(ordemTurnos[indice])

    /** Avança para o próximo combatente; ao dar a volta, incrementa a rodada. */
    fun proximoTurno(): Combatente {
        indice++
        if (indice >= ordemTurnos.size) {
            indice = 0
            rodadaAtual++
        }
        return combatenteAtual
    }

    /** Distância do combatente ao herói (herói = 0; desconhecido = "muito longe"). */
    fun distancia(c: Combatente): Int = if (c.ehHeroi) 0 else (distanciaAoHeroi[c.id] ?: Int.MAX_VALUE)

    /** Engajado em corpo-a-corpo: herói se há inimigo vivo adjacente; inimigo se está adjacente ao herói. */
    fun engajado(c: Combatente): Boolean = if (c.ehHeroi) {
        combatentes.any { !it.ehHeroi && it.vivo && (distanciaAoHeroi[it.id] ?: Int.MAX_VALUE) <= 1 }
    } else {
        (distanciaAoHeroi[c.id] ?: Int.MAX_VALUE) <= 1
    }

    /**
     * Manobras legais para [c] no estado atual:
     *  - inconsciente/morto → nenhuma;
     *  - atordoado → só Defesa Total ou Não Fazer Nada (recuperar) — MB p.420;
     *  - caído → sem Ataque Total nem Mover e Atacar; Mudar de Postura p/ levantar;
     *  - sem alvo engajado → sem ataque corpo-a-corpo (Ataque/Ataque Total).
     */
    fun manobrasLegais(c: Combatente): List<Manobra> {
        if (!c.vivo) return emptyList()
        // Lote COND-1: incapacitado por magia — dormindo/paralisado não agem (só "nada", o turno passa).
        if (Condicao.DORMINDO in c.condicoes || Condicao.PARALISADO in c.condicoes) return listOf(Manobra.NAO_FAZER_NADA)
        if (Condicao.ATORDOADO in c.condicoes) return listOf(Manobra.DEFESA_TOTAL, Manobra.NAO_FAZER_NADA)
        // Amedrontado: não ataca — só se afasta ou se defende (medo/pânico).
        if (Condicao.AMEDRONTADO in c.condicoes) return listOf(Manobra.MOVER, Manobra.DEFESA_TOTAL, Manobra.AGUARDAR, Manobra.NAO_FAZER_NADA)

        val legais = mutableListOf(
            Manobra.AVALIAR, Manobra.AGUARDAR, Manobra.CONCENTRAR, Manobra.PREPARAR,
            Manobra.MUDAR_POSTURA, Manobra.DEFESA_TOTAL, Manobra.MOVER, Manobra.NAO_FAZER_NADA
        )
        val engaj = engajado(c)
        if (engaj) {
            legais.add(Manobra.ATAQUE)                       // ataque corpo-a-corpo exige alvo adjacente
            if (!c.caido) {
                legais.add(Manobra.ATAQUE_TOTAL)
                legais.add(Manobra.ATAQUE_DEDICADO)          // Lote PONTE-4 (AM p98): entre Ataque e Ataque Total
                legais.add(Manobra.ATAQUE_DEFENSIVO)         // Lote PONTE-4 (AM p98): entre Ataque e Defesa Total
            }
        }
        if (!c.caido) legais.add(Manobra.MOVER_E_ATACAR)     // caído não pode mover-e-atacar
        return legais.distinct()
    }

    /** Relatório factual e determinístico do estado (para o Narrador IA narrar sem inventar números). */
    fun estadoResumo(): String = buildString {
        append("Rodada $rodadaAtual.")
        ordemTurnos.forEach { id ->
            val c = porId.getValue(id)
            val cond = if (c.condicoes.isEmpty()) "—" else c.condicoes.joinToString(", ") { it.rotulo }
            val dist = if (c.ehHeroi) "herói" else {
                val d = distanciaAoHeroi[c.id]
                if (d == null) "dist. desconhecida" else "${d}m do herói"
            }
            append("\n• ${c.nome}: $dist, ${c.postura.rotulo}, condições: $cond, PV ${c.pvAtual}/${c.pvMax}")
        }
    }
}
