package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem

/**
 * Os testes de **resistir** que a ficha sabe montar (Lote RESIST-1).
 *
 * O GURPS é cheio de teste que não é perícia nem atributo puro: manter
 * consciência, evitar a morte, resistir a doença, veneno, medo. Antes deste
 * lote a aba Rolagem não tinha onde pô-los — o Teste de Reação e o Autocontrole
 * acabaram como painéis soltos no fim da tela, e o resto simplesmente não
 * existia.
 *
 * Este objeto é o catálogo desses testes. A UI (`DialogoReacaoEResistencia`)
 * só desenha o que vem daqui.
 *
 * **Todos são testes que o JOGADOR rola** — é o que serve à mesa via Discord.
 * O que depende de um PdM agindo (o −N no NH do mago inimigo, por exemplo) fica
 * de fora de propósito: só existiria dentro do combate tático.
 */
object ResistenciaRules {

    /** De onde o teste sai — serve para agrupar na tela. */
    enum class Familia(val rotulo: String) {
        CORPO("Corpo"),
        MENTE("Mente"),
        SOBRENATURAL("Sobrenatural")
    }

    /**
     * Um teste de resistência já montado.
     *
     * [origens] existe pelo mesmo motivo da notinha das perícias: número que
     * não diz de onde veio é caixa preta.
     */
    data class TesteDeResistencia(
        val rotulo: String,
        val alvo: Int,
        val explicacao: String,
        val familia: Familia,
        val origens: List<String> = emptyList()
    ) {
        val descricaoAcessivel: String
            get() = "$rotulo. Alvo $alvo. $explicacao" +
                if (origens.isEmpty()) "" else " Inclui ${origens.joinToString(", ")}."
    }

    private const val ID_BOA_FORMA = "boa_forma"
    private const val ID_DESTEMOR = "destemor"
    /**
     * ⚠️ O id do catálogo é `abascanto_resistencia_a_magia`, não `abascanto`.
     *
     * Escrevi `abascanto` no Lote RESIST-1 e o teste passou — porque o teste
     * inventava o id em vez de ler o catálogo. Mesma falha do bug do V-1: cada
     * pedaço verde e o conjunto quebrado. Agora há um teste que confronta esta
     * constante com `vantagens.v3.json`.
     */
    internal const val ID_RESISTENCIA_MAGIA = "abascanto_resistencia_a_magia"
    private const val ID_DIFICIL_DE_SUBJUGAR = "dificil_de_subjugar"
    private const val ID_DURO_DE_MATAR = "duro_de_matar"

    /**
     * **Fácil de Matar** (MB p.140) — o espelho do Duro de Matar.
     *
     * > Cada nível impõe **-1 nos testes de HT feitos para verificar a
     * > sobrevivência** (…). **Isso não afeta a maioria dos testes normais de HT**
     * > — apenas aqueles que servem para evitar a morte. Os testes de HT **não
     * > podem ser reduzidos abaixo de 3**.
     *
     * ⚠️ Duas ressalvas que o livro faz questão de deixar claras, e que sem código
     * viram erro silencioso: ela **não** toca resistir a veneno, doença nem
     * esforço, e o alvo **nunca desce abaixo de 3**.
     */
    private const val ID_FACIL_DE_MATAR = "facil_de_matar"

    /**
     * **Fora de Forma** (MB p.143) — o espelho da Boa Forma.
     *
     * > **-1** (Fora de Forma) ou **-2** (Muito Fora de Forma) em todos os testes
     * > de HT para permanecer consciente, evitar a morte, resistir aos efeitos de
     * > doenças e venenos, etc.
     *
     * ⚠️ **Contraste de propósito com o Fácil de Matar**, que está logo acima: a
     * Fácil de Matar toca **só** os testes de morte; esta toca **todos** os de
     * resistência do corpo. Ter as duas lado a lado, com a diferença escrita,
     * é o que impede alguém "unificar" as duas por engano mais tarde.
     *
     * O livro também é explícito no que ela **não** faz: *"Isso não reduz sua HT
     * nem as perícias baseadas nesse atributo"*.
     */
    private const val ID_FORA_DE_FORMA = "fora_de_forma"

    /**
     * **Temor** (MB p.159) — o espelho do Destemor.
     *
     * > Subtraia o nível de Temor da Vontade sempre que fizer uma **Verificação
     * > de Pânico** ou tiver que **resistir à perícia Intimidação** ou a um poder
     * > sobrenatural que cause medo.
     */
    private const val ID_TEMOR = "temor"

    /**
     * **Suscetibilidade à Magia** (MB p.159) — o espelho da Resistência à Magia.
     *
     * > Acrescente o nível ao NH de quem estiver fazendo uma mágica contra ele e
     * > **subtraia o mesmo valor dos testes para resistir**.
     *
     * ⚠️ O id do catálogo grafa "susceptibilidade" (com **p**); o livro escreve
     * "Suscetibilidade". O id fica como está para não quebrar ficha salva.
     */
    private const val ID_SUSCETIBILIDADE_MAGIA = "susceptibilidade_a_magia"

    /** **Suscetível** (MB p.159) — o espelho do Resistente. */
    private const val ID_SUSCETIVEL = "suscetivel"

    /**
     * Penalidade de uma DESVANTAGEM por nível — devolvida negativa.
     *
     * Irmã de `nivelDe`, que lê o lado das vantagens. Separadas porque o sinal é
     * decidido aqui: quem chama soma, sempre.
     */
    private fun penalidadeDe(personagem: Personagem, id: String): Int =
        -personagem.desvantagensTotais.filter { it.definicaoId == id }
            .sumOf { it.nivel.coerceAtLeast(1) }

    /** A origem, para a notinha poder nomear a desvantagem. */
    private fun origemDaDesvantagem(personagem: Personagem, id: String): List<String> =
        personagem.desvantagensTotais.filter { it.definicaoId == id }
            .map { "${it.nome} -${it.nivel.coerceAtLeast(1)}" }

    /**
     * **Fora de Forma**: -1 ou -2, escolhido pelo custo pago (MB p.143).
     *
     * ⚠️ Lê o `custoEscolhido`, não o nível: o catálogo guarda esta desvantagem
     * como escolha de custo (-5 ou -15), igual à Boa Forma do outro lado.
     */
    private fun penalidadeForaDeForma(personagem: Personagem): Pair<Int, List<String>> {
        var total = 0
        val origens = mutableListOf<String>()
        personagem.desvantagensTotais.filter { it.definicaoId == ID_FORA_DE_FORMA }.forEach { d ->
            val p = if (d.custoEscolhido <= -15) -2 else -1
            total += p
            origens += "${d.nome} $p"
        }
        return total to origens
    }

    /** Níveis de Fácil de Matar — devolvidos como número NEGATIVO. */
    internal fun penalidadeFacilDeMatar(personagem: Personagem): Int =
        -personagem.vantagensTotais.filter { it.definicaoId == ID_FACIL_DE_MATAR }
            .sumOf { it.nivel.coerceAtLeast(1) } -
            personagem.desvantagensTotais.filter { it.definicaoId == ID_FACIL_DE_MATAR }
                .sumOf { it.nivel.coerceAtLeast(1) }

    /**
     * Todos os testes que esta ficha pode rolar.
     *
     * Os de HT existem sempre — qualquer personagem pode precisar resistir a
     * veneno. Os que dependem de vantagem (Resistência à Magia) só aparecem
     * quando a vantagem está na ficha.
     */
    fun testesDe(personagem: Personagem): List<TesteDeResistencia> {
        val ht = personagem.ht
        val vontade = personagem.vontade
        val lista = mutableListOf<TesteDeResistencia>()

        val (bonusBoa, origensBoa) = bonusBoaForma(personagem)
        // Fora de Forma acompanha a Boa Forma em TODOS os testes de corpo -- e é
        // por isso que ela entra aqui, e não só no teste de morte.
        val (penalFora, origensFora) = penalidadeForaDeForma(personagem)
        val bonusHt = bonusBoa + penalFora
        val origensHt = origensBoa + origensFora

        // --- Corpo: tudo sai do HT (MB p.419-443) ---
        lista += TesteDeResistencia(
            "Manter a consciência", ht + bonusHt + nivelDe(personagem, ID_DIFICIL_DE_SUBJUGAR),
            "Com PV em 0 ou menos, a cada turno. Falha: desmaia.",
            Familia.CORPO,
            origensHt + origemDe(personagem, ID_DIFICIL_DE_SUBJUGAR)
        )
        // ⚠️ Fácil de Matar entra SÓ aqui: o livro diz que ela não afeta os
        // testes normais de HT, apenas os que evitam a morte.
        val facil = penalidadeFacilDeMatar(personagem)
        lista += TesteDeResistencia(
            "Evitar a morte",
            (ht + bonusHt + nivelDe(personagem, ID_DURO_DE_MATAR) + facil)
                .let { PisoDeTeste.aplicar(it) },
            "Ao passar de cada múltiplo negativo do PV máximo. Falha: morre.",
            Familia.CORPO,
            origensHt + origemDe(personagem, ID_DURO_DE_MATAR) +
                if (facil != 0) listOf("Fácil de Matar $facil") else emptyList()
        )
        // Suscetível entra SÓ nestes dois: são os exemplos que o livro dá, e o
        // app não guarda a qual objeto o personagem é suscetível.
        val suscetivel = penalidadeSuscetivel(personagem)
        val origemSuscetivel = origemDaDesvantagem(personagem, ID_SUSCETIVEL)
        lista += TesteDeResistencia(
            "Resistir a doença", PisoDeTeste.aplicar(ht + bonusHt + suscetivel),
            "Contra infecção e contágio. O modificador vem da doença." +
                if (suscetivel != 0) " Confirme com o Mestre se a sua Suscetibilidade vale aqui." else "",
            Familia.CORPO, origensHt + origemSuscetivel
        )
        lista += TesteDeResistencia(
            "Resistir a veneno", PisoDeTeste.aplicar(ht + bonusHt + suscetivel),
            "O modificador vem do veneno." +
                if (suscetivel != 0) " Confirme com o Mestre se a sua Suscetibilidade vale aqui." else "",
            Familia.CORPO, origensHt + origemSuscetivel
        )
        lista += TesteDeResistencia(
            "Aguentar o esforço", ht + bonusHt,
            "Correr, segurar a respiração, calor, exaustão. Falha: perde PF.",
            Familia.CORPO, origensHt
        )

        // --- Mente ---
        // Temor é o espelho do Destemor: um soma na Vontade contra o medo, o
        // outro subtrai. Os dois no mesmo número, com o piso de 3 no fim.
        val destemor = nivelDe(personagem, ID_DESTEMOR) + penalidadeDe(personagem, ID_TEMOR)
        val origemDestemor = origemDe(personagem, ID_DESTEMOR) +
            origemDaDesvantagem(personagem, ID_TEMOR)
        lista += TesteDeResistencia(
            "Verificação de Pânico", PisoDeTeste.aplicar(vontade + destemor),
            "Diante de horror ou do sobrenatural. NÃO é disparada por dano.",
            Familia.MENTE, origemDestemor
        )
        lista += TesteDeResistencia(
            "Resistir a Intimidação", vontade + destemor,
            "Contra a perícia Intimidação de outro personagem.",
            Familia.MENTE, origemDestemor
        )

        // --- Sobrenatural: só com a vantagem ---
        val rm = nivelDe(personagem, ID_RESISTENCIA_MAGIA)
        if (rm > 0) {
            lista += TesteDeResistencia(
                "Resistir a elixir mágico", ht + bonusHt + rm,
                "MB p.85: teste de HT somado à Resistência à Magia.",
                Familia.SOBRENATURAL,
                origensHt + origemDe(personagem, ID_RESISTENCIA_MAGIA)
            )
        }

        return lista
    }

    /**
     * O nível de Resistência à Magia, para o Mestre aplicar do outro lado.
     *
     * O livro manda subtrair este número do NH de quem lança magia no
     * personagem. Isso acontece na ficha DO MAGO, não nesta — então aqui o
     * número só é **exibido**, para o jogador informar ao Mestre no Discord.
     * Zero quando não há a vantagem.
     */
    fun resistenciaAMagia(personagem: Personagem): Int =
        nivelDe(personagem, ID_RESISTENCIA_MAGIA) +
            penalidadeDe(personagem, ID_SUSCETIBILIDADE_MAGIA)

    /**
     * Se o número acima é **negativo** — ou seja, a ficha tem Suscetibilidade à
     * Magia e a tela precisa dizer o contrário do texto habitual.
     *
     * ⚠️ Sem isto o card exibiria *"o mago sofre −3 ao conjurar em você"* para
     * quem, na verdade, **facilita** o feitiço. Número certo, frase invertida —
     * pior que não mostrar nada.
     */
    fun ehSuscetivelAMagia(personagem: Personagem): Boolean =
        resistenciaAMagia(personagem) < 0

    /**
     * **Suscetível** (MB p.159) — o espelho do Resistente.
     *
     * > **-1 por nível** nos testes de HT para resistir aos efeitos negativos de
     * > uma classe de objetos ou substâncias (doença, veneno, etc.).
     *
     * ⚠️ O app **não guarda a qual objeto** o personagem é suscetível — o
     * catálogo tem uma entrada só. Então a penalidade entra em **doença e
     * veneno**, que são os dois exemplos que o livro dá, e o texto do card avisa
     * que o Mestre decide se vale naquele caso.
     */
    private fun penalidadeSuscetivel(personagem: Personagem): Int =
        penalidadeDe(personagem, ID_SUSCETIVEL)

    /** Se a ficha tem Aptidão Mágica — usado pela trava do Abascanto. */
    fun temAptidaoMagica(personagem: Personagem): Boolean =
        personagem.vantagensTotais.any { it.definicaoId == "aptidao_magica" }

    private fun nivelDe(personagem: Personagem, id: String): Int =
        personagem.vantagensTotais.filter { it.definicaoId == id }
            .sumOf { it.nivel.coerceAtLeast(1) }

    private fun origemDe(personagem: Personagem, id: String): List<String> =
        personagem.vantagensTotais.filter { it.definicaoId == id }
            .map { "${it.nome} +${it.nivel.coerceAtLeast(1)}" }

    /** Boa Forma: +1 (5 pts) ou +2 (15 pts) em **todos** os testes de HT. */
    private fun bonusBoaForma(personagem: Personagem): Pair<Int, List<String>> {
        var total = 0
        val origens = mutableListOf<String>()
        personagem.vantagensTotais.filter { it.definicaoId == ID_BOA_FORMA }.forEach { v ->
            val b = if (v.custoEscolhido >= 15) 2 else 1
            total += b
            origens += "${v.nome} +$b"
        }
        return total to origens
    }
}
