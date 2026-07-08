package com.gurps.ficha.domain.combat.hex

/**
 * Lote HEX-6 (Fase 4 do PILAR): regras POSICIONAIS que o modelo de FAIXAS abstratas do Saga não cobre.
 *
 * Kotlin PURO — sem Android, sem CombatSession. Motor de regras 2D em cima de [HexCoord]/[HexGrid]. Cada
 * objeto expõe funções que o caller (UI tática ou controlador) invoca na hora de resolver a troca ou de
 * decidir a manobra.
 *
 * Regras cobertas:
 *  - [HexCobertura]        — MB p.407–408: linha de visão bloqueada / cobertura parcial (−2 no ataque à distância).
 *  - [HexAtaqueAtravesHex] — MB p.389: atacando através de hex ocupado (armas com alcance ≥ 2m).
 *  - [HexManterADistancia] — AM p.101: defensor com interromper-investida/aparar/obstrução bem sucedido
 *    impõe custo de MV extra ao atacante que insiste em avançar.
 *
 * Ataque Telegráfico (AM p.109) NÃO está aqui — já implementado no PONTE-3 (opcoesAtaqueHeroi/opcoesDefesa
 * do CombatSession). O hex só o COMPLEMENTA (fica útil para compensar penalidade de atacar pelas costas).
 */

/** Regras de linha de visão + cobertura contra ataques à distância (MB p.407–408). */
object HexCobertura {
    /** Grau de cobertura entre atacante e alvo, com a penalidade padrão do MB. */
    enum class Grau(val penalidadeAtaque: Int, val podeAtacar: Boolean) {
        /** Nada bloqueia a LoS. */
        LIMPA(0, true),
        /** Um obstáculo adjacente ao alvo está entre atacante e alvo — alvo se agacha atrás. −2 no ataque. */
        PARCIAL(-2, true),
        /** Um obstáculo NO CAMINHO da LoS — alvo invisível. Só tiro cego (MB p.408: penalidade normal ~ −10). */
        TOTAL(-10, false),
    }

    /**
     * Classifica a cobertura entre [atacante] e [alvo] considerando o conjunto de [hexesBloqueadores]
     * (paredes, pilares, árvores altas — qualquer hex sólido de LoS).
     *
     * Convenção (espelha [HexGrid.linhaDeVisao]): os HEXES DAS PONTAS (atacante e alvo) NÃO contam como
     * bloqueadores, mesmo que estejam em [hexesBloqueadores] — é válido o atacante estar em cima de um
     * pilar/telhado semanticamente sólido, ou o alvo se agachar dentro do próprio obstáculo.
     *
     * Regras:
     *  - Se atacante == alvo → LIMPA (não faz sentido cobertura contra si mesmo).
     *  - Se algum hex INTERMEDIÁRIO da linha reta (excluindo endpoints) é bloqueador → TOTAL.
     *  - Se algum vizinho do alvo (que não seja o próprio atacante nem o próprio alvo) é bloqueador E está
     *    MAIS PERTO do atacante que o próprio alvo → PARCIAL.
     *  - Caso contrário → LIMPA.
     */
    fun grauEntre(atacante: HexCoord, alvo: HexCoord, hexesBloqueadores: Set<HexCoord>): Grau {
        if (atacante == alvo) return Grau.LIMPA
        val linha = HexGrid.linhaReta(atacante, alvo)
        // Total: bloqueador entre atacante e alvo (exclui os dois endpoints).
        for (i in 1 until linha.lastIndex) {
            if (linha[i] in hexesBloqueadores) return Grau.TOTAL
        }
        // Parcial: bloqueador adjacente ao alvo E mais perto do atacante — mas nem o atacante nem o alvo
        // contam como cobertura de si mesmos (convenção dos endpoints).
        val distAtkAlvo = atacante.distancia(alvo)
        val vizinhosAlvo = HexGrid.vizinhos(alvo)
        val temCoberturaParcial = vizinhosAlvo.any { v ->
            v != atacante && v != alvo && v in hexesBloqueadores && atacante.distancia(v) < distAtkAlvo
        }
        return if (temCoberturaParcial) Grau.PARCIAL else Grau.LIMPA
    }
}

/** Ataque através de hexágono ocupado — MB p.389 (armas com alcance ≥ 2 metros). */
object HexAtaqueAtravesHex {

    /**
     * Retorna a penalidade no ataque corpo-a-corpo de [atacante] contra [alvo] considerando os hexes
     * intermediários da linha reta:
     *
     *  - `null` — não pode atacar (distância > 1 e [alcanceArmaMetros] < 2).
     *  - `0`    — linha limpa OU só aliados no meio (treino básico ignora aliado, MB p.389).
     *  - `-4`   — algum hex intermediário é ocupado por INIMIGO.
     *
     * `atacante` e `alvo` são os ENDPOINTS — não contam como "meio". Se distância == 1, não há meio, retorna 0.
     * NÃO trata linha tangente a dois hexes (edge case cinematográfico do MB); [HexGrid.linhaReta] devolve
     * a sequência determinística e essa aproximação basta para o motor.
     */
    fun penalidade(
        atacante: HexCoord,
        alvo: HexCoord,
        alcanceArmaMetros: Int,
        ocupantesAliados: Set<HexCoord>,
        ocupantesInimigos: Set<HexCoord>,
    ): Int? {
        val dist = atacante.distancia(alvo)
        if (dist == 0) return 0 // combate corporal, sem hex intermediário
        if (dist > alcanceArmaMetros) return null // fora de alcance
        if (dist == 1) return 0 // vizinho direto, sem hex intermediário
        if (alcanceArmaMetros < 2) return null // arma curta não atinge fora do vizinho
        val linha = HexGrid.linhaReta(atacante, alvo)
        var temInimigo = false
        for (i in 1 until linha.lastIndex) {
            val h = linha[i]
            if (h in ocupantesInimigos) { temInimigo = true; break }
            // aliado no meio: sem penalidade — continuo verificando o resto (pode haver inimigo depois)
        }
        return if (temInimigo) -4 else 0
    }
}

/**
 * Manter um oponente à distância — AM p.101.
 *
 * Quando o defensor usa interromper-investida / aparar-desarmado / obstrução com sucesso, o resultado
 * modifica o custo de MV do atacante que insistir em avançar. Este objeto encapsula a tabela de resultados.
 */
object HexManterADistancia {

    /** Como o defensor bloqueou a carga (após a defesa dele ter tido sucesso). */
    enum class TipoInterrupcao {
        /** Nada aconteceu — atacante pode avançar normal. */
        NENHUMA,

        /** Defesa ativa pegou o ataque mas SEM dano OU sem penetração de RD. Arma no caminho, decisão do Mestre. */
        APAROU_SEM_DANO,

        /** Defesa causou dano com arma NÃO-perfurante (ou perfurante que não é estocada) — GdP/GeB. Disputa ST. */
        APAROU_COM_DANO_NAO_ESTOCADA,

        /** Defesa causou dano com arma de estocada por perfuração — arma cravada. Recuar OU testar Vontade-3. */
        APAROU_COM_ESTOCADA_PERFURANTE,

        /** Interromper-investida gerou nocaute ou projeção — atacante caiu. Não pode avançar este turno. */
        NOCAUTE_OU_PROJECAO,
    }

    /**
     * Resultado da avaliação: quanto MV extra o atacante gasta e quais testes ele precisa vencer.
     *  - [podeAvancar]         — false só se atacante caiu (nocaute/projeção).
     *  - [movimentoExtra]      — 0 ou 2 (MB p.389: "dois pontos de movimento para se desviar").
     *  - [disputaSTNecessaria] — true quando defensor com arma GdP/GeB precisa perder a disputa ST.
     *  - [testeVontadeMod]     — modificador do teste de Vontade quando arma perfurante ficou cravada
     *    (-3 base, ±3 por Hipoalgia, -4 por Hiperalgia — caller aplica os modificadores do herói).
     */
    data class Resultado(
        val podeAvancar: Boolean,
        val movimentoExtra: Int,
        val disputaSTNecessaria: Boolean,
        val testeVontadeMod: Int?,
    )

    fun avaliar(tipo: TipoInterrupcao): Resultado = when (tipo) {
        TipoInterrupcao.NENHUMA -> Resultado(
            podeAvancar = true, movimentoExtra = 0,
            disputaSTNecessaria = false, testeVontadeMod = null,
        )
        TipoInterrupcao.APAROU_SEM_DANO -> Resultado(
            podeAvancar = true, movimentoExtra = 0,
            disputaSTNecessaria = false, testeVontadeMod = null,
        )
        TipoInterrupcao.APAROU_COM_DANO_NAO_ESTOCADA -> Resultado(
            podeAvancar = true, movimentoExtra = 2,
            disputaSTNecessaria = true, testeVontadeMod = null,
        )
        TipoInterrupcao.APAROU_COM_ESTOCADA_PERFURANTE -> Resultado(
            podeAvancar = true, movimentoExtra = 2,
            disputaSTNecessaria = false, testeVontadeMod = -3,
        )
        TipoInterrupcao.NOCAUTE_OU_PROJECAO -> Resultado(
            podeAvancar = false, movimentoExtra = 0,
            disputaSTNecessaria = false, testeVontadeMod = null,
        )
    }
}
