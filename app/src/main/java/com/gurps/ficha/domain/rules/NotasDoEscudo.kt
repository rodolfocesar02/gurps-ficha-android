package com.gurps.ficha.domain.rules

/**
 * As notas de rodapé da **Tabela de Escudos** — MB p.288.
 *
 * ## 🔴 Por que este arquivo precisou existir
 *
 * O escudo era o único item do app cuja nota chegava **crua** à ficha: ao
 * adicionar o *Escudo Grande*, o campo *Notas* ficava literalmente com
 * `[2, 4, 6]`. Abrir o lápis e ver isso não diz nada — e, pior, parece dado
 * corrompido.
 *
 * A arma já tinha os seus mapas (em `FichaEquipmentDelegate`) e a armadura traz o
 * texto pronto do catálogo (`observacoesDetalhadas`). **Só o escudo não tinha
 * ninguém**, porque o `escudos.v1.json` guarda a referência e não o texto.
 *
 * ⚠️ O texto abaixo é **transcrição** do livro, não paráfrase minha. Onde a nota
 * é longa o corte fica a cargo de quem exibe — cortar aqui seria decidir pelo
 * leitor qual metade da regra ele pode ler.
 */
object NotasDoEscudo {

    /** MB p.288, seção *Observações* da Tabela de Escudos. */
    private val LEGENDA = mapOf(
        1 to "Pode ser usado defensivamente para prender o inimigo; v. Capas (pág. 405).",
        2 to "Pode ser usado para aplicar um golpe com o escudo (v. a Tabela de Armas de " +
            "Combate Corpo a Corpo) ou arremeter com o escudo (v. Encontrão, pág. 371). Em " +
            "NT1+, é possível prover um escudo pequeno, médio ou grande com espigos para " +
            "aumentar o dano causado: acrescente $20 e 2,5 kg.",
        3 to "Também disponível como um broquel. É possível preparar um broquel em um turno e " +
            "soltá-lo com uma ação livre, exatamente como uma arma, mas ele sempre ocupa uma " +
            "das mãos do personagem e não permite que ele realize uma arremetida. Utilize " +
            "Escudo (Broquel) em vez da perícia Escudo usual. Não afeta as estatísticas.",
        4 to "Em NT3+, os escudos de ferro estão disponíveis, embora não sejam muito comuns: " +
            "custo ×5, peso ×2, RD +3. Em NT7+, os escudos de plástico (feitos de Lexan, etc.) " +
            "terão peso ×1/2 (as outras estatísticas continuam idênticas). A composição do " +
            "escudo nunca afeta o BD.",
        5 to "Preso ao pulso, ele deixa a mão livre. A RD é enrijecida (trate como um nível da " +
            "ampliação Enrijecida, pág. 83).",
        6 to "Um personagem com um Escudo Grande empunhado sofre uma penalidade de -2 em todos " +
            "os ataques corpo a corpo (v. Modificadores de Ataque Corpo a Corpo, pág. 547)."
    )

    /** Quantas notas o livro traz. Serve à trava que confere se falta alguma. */
    val TOTAL = LEGENDA.size

    /**
     * `"[2, 4, 6]"` vira as três notas por extenso.
     *
     * ⚠️ Referência que **não** existe na legenda é descartada em silêncio, e é
     * de propósito: melhor faltar uma linha do que a ficha do jogador ganhar um
     * `[9]` solto — que é exatamente o defeito que este arquivo veio consertar.
     */
    fun explicar(observacoesRaw: String?): List<String> {
        val texto = observacoesRaw.orEmpty()
        if (texto.isBlank()) return emptyList()
        return Regex("\\d+").findAll(texto)
            .mapNotNull { it.value.toIntOrNull() }
            .distinct()
            .mapNotNull { ref -> LEGENDA[ref]?.let { "[$ref] $it" } }
            .toList()
    }

    /** O mesmo, pronto para o campo `notas` do equipamento. */
    fun paraAsNotas(observacoesRaw: String?): String = explicar(observacoesRaw).joinToString("\n")
}
