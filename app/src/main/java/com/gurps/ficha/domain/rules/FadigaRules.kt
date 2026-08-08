package com.gurps.ficha.domain.rules

import kotlin.math.ceil

/**
 * **Fadiga** — Lote MB-6 (MB p.426-428).
 *
 * O botão **PF** da aba Rolagem. O jogador marca *de onde* veio o cansaço e o
 * app faz duas coisas que ninguém faz de cabeça no meio da mesa:
 *
 * 1. **Soma** os PF perdidos e devolve o número na ficha.
 * 2. Diz **como cada perda se recupera** — que é a parte que muda o jogo.
 *
 * ## 🔴 PF perdido não é tudo igual
 *
 * É a descoberta deste lote, e ela contraria o instinto de quem só olha o
 * número na ficha. **Dez PF perdidos podem ser dez coisas diferentes:**
 *
 * > Um personagem só pode se recuperar da perda de fadiga provocada por **sono
 * > perdido** ao **dormir**. (…) Um personagem também precisa de **comida ou
 * > água** para recuperar PF perdido por fome ou desidratação.
 *
 * Descansar dez minutos recupera 1 PF de esforço — e **zero** PF de fome. Um
 * herói faminto pode sentar a tarde inteira e não subir um ponto. Quem trata o
 * PF como um balde só descobre isso na pior hora possível.
 *
 * Por isso a lista guarda a **origem** de cada ponto, não só o total.
 *
 * ## ⚠️ E, a zero PF, a fadiga vira ferimento
 *
 * > **0 PF ou menos** — Se ele sofrer mais fadiga, **cada PF perdido também
 * > resultará na perda de 1 PV**. Desta forma, toda fadiga devido à fome,
 * > desidratação, etc., uma hora pode levar o personagem à morte — é até mesmo
 * > possível **trabalhar até a morte**!
 *
 * Kotlin puro, sem Android — testável.
 */
object FadigaRules {

    /**
     * **Como aquele PF volta.** O campo mais importante do catálogo.
     *
     * O jogador nunca precisou saber disso porque o app nunca perguntou de onde
     * veio a perda. Agora pergunta.
     */
    enum class Recuperacao(val rotulo: String, val comoVolta: String) {
        DESCANSO(
            "Descanso",
            "1 PF a cada 10 minutos de descanso calmo. Ler, falar e pensar valem; " +
                "andar por aí, não. Uma refeição decente durante o descanso dá +1 PF."
        ),
        SONO(
            "Dormir",
            "⚠️ SÓ dormindo. Um período completo de sono devolve 1 PF; cada hora " +
                "ininterrupta a mais devolve mais 1 PF. Descansar acordado não adianta."
        ),
        COMIDA(
            "Comer",
            "⚠️ SÓ com um dia de descanso — nada de combate nem viagem — com três " +
                "refeições completas. Cada dia desses compensa três refeições perdidas."
        ),
        AGUA(
            "Beber",
            "⚠️ SÓ com um dia de descanso com água em abundância — e aí volta TUDO " +
                "de uma vez. O PV perdido por desidratação volta na velocidade normal."
        )
    }

    /**
     * Uma origem de fadiga.
     *
     * [unidade] é o que o jogador conta — refeições, períodos de 8 h, turnos de
     * esforço. O app multiplica por [pfPorUnidade].
     */
    data class Fonte(
        val id: String,
        val rotulo: String,
        val unidade: String,
        val pfPorUnidade: Int,
        val recuperacao: Recuperacao,
        val explicacao: String,
        /** Desidratação severa também tira PV, e isso não é fadiga (MB p.426). */
        val pvPorUnidade: Int = 0
    ) {
        fun pfDe(quantidade: Int): Int = pfPorUnidade * quantidade.coerceAtLeast(0)
        fun pvDe(quantidade: Int): Int = pvPorUnidade * quantidade.coerceAtLeast(0)
    }

    /** Id da linha que guarda o que o jogador baixou na mão, fora do painel. */
    const val ID_OUTROS = "outros"

    /**
     * O catálogo do capítulo, na ordem em que aparece no livro.
     *
     * ⚠️ **Esforço e habilidades não têm contador de tempo aqui.** Custo de
     * magia já sai do `MagicCasting`, e o tempo de esforço extenuante é decisão
     * do Mestre — o app só recebe o número de PF já decidido. Inventar um
     * cronômetro seria fingir uma regra que o livro não dá.
     */
    val FONTES: List<Fonte> = listOf(
        Fonte(
            id = "esforco",
            rotulo = "Esforço extenuante / estafa",
            unidade = "PF",
            pfPorUnidade = 1,
            recuperacao = Recuperacao.DESCANSO,
            explicacao = "Carregar mais que a carga muito pesada, ou empurrar/puxar algo " +
                "muito pesado, custa 1 PF por segundo (MB p.426)."
        ),
        Fonte(
            id = "corrida",
            rotulo = "Corrida ou natação — falhas no teste de HT",
            unidade = "falha",
            pfPorUnidade = 1,
            recuperacao = Recuperacao.DESCANSO,
            explicacao = "Um teste de HT a cada 15 s de disparada, ou a cada minuto de " +
                "corrida/natação ritmada. Cada falha custa 1 PF (MB p.426)."
        ),
        Fonte(
            id = "habilidade",
            rotulo = "Magias e habilidades que custam PF",
            unidade = "PF",
            pfPorUnidade = 1,
            recuperacao = Recuperacao.DESCANSO,
            explicacao = "Mágicas, vantagens como Cura e perícias cinematográficas. " +
                "O que é lançado pelo app já desconta sozinho — esta linha é para o resto."
        ),
        Fonte(
            id = "fome",
            rotulo = "Refeições perdidas",
            unidade = "refeição",
            pfPorUnidade = 1,
            recuperacao = Recuperacao.COMIDA,
            explicacao = "Um ser humano precisa de três refeições por dia e perde 1 PF a " +
                "cada refeição perdida (MB p.426)."
        ),
        Fonte(
            id = "desidratacao",
            rotulo = "Água insuficiente — períodos de 8 h",
            unidade = "período de 8 h",
            pfPorUnidade = 1,
            recuperacao = Recuperacao.AGUA,
            explicacao = "São 2 litros por dia — 3 em clima quente, 5 no calor do deserto. " +
                "Bebendo menos que isso, 1 PF a cada oito horas (MB p.426)."
        ),
        Fonte(
            id = "sede_severa",
            rotulo = "⚠️ Menos de 1 litro no dia",
            unidade = "dia",
            pfPorUnidade = 1,
            recuperacao = Recuperacao.AGUA,
            explicacao = "Além do acima: 1 PF E 1 PV a mais por dia. É a única fonte de " +
                "fadiga que tira PV sem passar por zero (MB p.426).",
            pvPorUnidade = 1
        ),
        Fonte(
            id = "sono",
            rotulo = "Sono perdido",
            unidade = "PF",
            pfPorUnidade = 1,
            recuperacao = Recuperacao.SONO,
            explicacao = "1 PF por não dormir, mais 1 PF a cada quarto de dia útil " +
                "(4 h) acordado além das 16 h normais (MB p.427)."
        ),
        Fonte(
            id = ID_OUTROS,
            rotulo = "Perda anotada à mão",
            unidade = "PF",
            pfPorUnidade = 1,
            recuperacao = Recuperacao.DESCANSO,
            explicacao = "O que já estava faltando na ficha e este painel não explica. " +
                "Fica aqui para o deslize no número do PF não sumir ao salvar."
        )
    )

    fun fonte(id: String): Fonte? = FONTES.firstOrNull { it.id == id }

    // ==================================================================
    // Sono perdido
    // ==================================================================

    /** O dia útil do ser humano comum, em horas (MB p.427). */
    const val DIA_UTIL_HORAS = 16

    /**
     * Quantos PF custam [horasAcordado] horas em pé.
     *
     * > O ser humano comum é capaz de funcionar normalmente durante um "dia" de
     * > 16 horas. (…) Ele perde **1 PF se não for dormir**, mais **1 PF a cada
     * > quarto de dia útil** (normalmente quatro horas) que ficar acordado depois
     * > disso.
     *
     * ⚠️ **Acordar cedo encurta o dia** (MB p.427): quem dormiu menos que o
     * período completo subtrai **o dobro** das horas perdidas do dia seguinte.
     * Quem tem período de 8 h e dormiu 6 h começa a cansar com **12 h** acordado,
     * não 16. É para isso que serve [horasDeSonoPerdidasAntes].
     */
    fun pfPorSonoPerdido(horasAcordado: Int, horasDeSonoPerdidasAntes: Int = 0): Int {
        val limite = DIA_UTIL_HORAS - 2 * horasDeSonoPerdidasAntes.coerceAtLeast(0)
        if (horasAcordado <= limite) return 0
        val excedente = horasAcordado - limite
        // 1 PF pelo próprio fato de não dormir + 1 a cada quarto de dia útil.
        return 1 + (excedente - 1) / (DIA_UTIL_HORAS / 4)
    }

    // ==================================================================
    // O estado do personagem
    // ==================================================================

    /**
     * O que o PF baixo faz. Os efeitos do livro são **cumulativos**.
     */
    data class Estado(
        val pfAtual: Int,
        val pfMax: Int,
        val muitoCansado: Boolean,
        val aBeiraDoColapso: Boolean,
        val avisos: List<String>
    )

    /** "Menos de 1/3 do seu PF remanescente" — comparação inteira, sem divisão. */
    fun muitoCansado(pfAtual: Int, pfMax: Int): Boolean = pfAtual * 3 < pfMax && pfMax > 0

    /**
     * ⚠️ Metade **arredondada para cima**, e só em três coisas.
     *
     * > Divida seu **Deslocamento, Esquiva e ST** pela metade (arredondado para
     * > cima). Isso **não** afeta as características baseadas na ST, como **PV e
     * > dano**.
     *
     * A última frase é a que evita o erro: quem corta a ST pela metade e recalcula
     * o dano está aplicando duas vezes o cansaço.
     */
    fun metadeCansada(valor: Int): Int = ceil(valor / 2.0).toInt()

    fun estadoDe(pfAtual: Int, pfMax: Int): Estado {
        val cansado = muitoCansado(pfAtual, pfMax)
        val colapso = pfAtual <= 0
        val avisos = buildList {
            if (cansado) {
                add(
                    "Menos de 1/3 do PF: Deslocamento, Esquiva e ST pela metade " +
                        "(arredondado para cima). ⚠️ PV e dano NÃO mudam (MB p.426)."
                )
            }
            if (colapso) {
                add(
                    "0 PF ou menos: cada PF perdido daqui em diante também custa 1 PV. " +
                        "Para fazer qualquer coisa além de conversar ou descansar é preciso " +
                        "um teste de Vontade — em combate, a cada manobra diferente de Fazer " +
                        "Nada. Falhou, cai por esgotamento (MB p.426)."
                )
            }
        }
        return Estado(pfAtual, pfMax, cansado, colapso, avisos)
    }

    /**
     * Os testes de Vontade de quem está sem dormir (MB p.427).
     *
     * Depende do PF perdido **por sono**, não do PF total — por isso não cabe em
     * [estadoDe]. Quem está a 1 PF de exaustão por ter carregado pedra o dia
     * inteiro não corre risco de apagar no meio da guarda.
     */
    fun alertaDeSono(pfPerdidoPorSono: Int, pfMax: Int, dorminhoco: Boolean = false): String? {
        if (pfMax <= 0 || pfPerdidoPorSono <= 0) return null
        val penalidade = if (dorminhoco) -3 else -2
        val restante = pfMax - pfPerdidoPorSono
        return when {
            restante * 3 < pfMax ->
                "Sono perdido abaixo de 1/3 do PF: teste de Vontade a cada 30 minutos parado " +
                    "ou a cada 2 horas de ação. Sucesso mantém acordado, mas com $penalidade " +
                    "em DX, IQ e autocontrole. Isso é muito perigoso (MB p.427)."
            pfPerdidoPorSono * 2 >= pfMax ->
                "Metade ou mais do PF perdida por sono: teste de Vontade a cada 2 horas parado. " +
                    "Falhou, adormece; passou, fica acordado com $penalidade em DX, IQ e " +
                    "autocontrole (MB p.427)."
            else -> null
        }
    }

    // ==================================================================
    // A conta do painel
    // ==================================================================

    data class Total(
        val pf: Int,
        val pv: Int,
        val porRecuperacao: Map<Recuperacao, Int>
    )

    /** Soma o painel. [quantidades] é `id da fonte -> quantas unidades`. */
    fun totalDe(quantidades: Map<String, Int>): Total {
        var pf = 0
        var pv = 0
        val porRec = mutableMapOf<Recuperacao, Int>()
        quantidades.forEach { (id, qtd) ->
            val f = fonte(id) ?: return@forEach
            val perdaPf = f.pfDe(qtd)
            pf += perdaPf
            pv += f.pvDe(qtd)
            if (perdaPf > 0) porRec[f.recuperacao] = (porRec[f.recuperacao] ?: 0) + perdaPf
        }
        return Total(pf, pv, porRec)
    }

    /**
     * 🔴 **A reconciliação** — a parte que impede o painel de apagar trabalho do
     * jogador.
     *
     * O PF da ficha também muda por fora: o deslize no cartão, o custo de uma
     * magia, o combate. Se o painel simplesmente escrevesse `pfMax − soma`, todo
     * PF gasto fora dele **voltaria** na primeira vez que o jogador abrisse a
     * lista para marcar uma refeição perdida.
     *
     * Então, ao abrir, o que falta e o painel **não explica** cai na linha
     * [ID_OUTROS], visível e editável. Nada some, e nada aparece de graça.
     */
    fun reconciliar(
        pfMax: Int,
        pfAtual: Int,
        quantidades: Map<String, Int>
    ): Map<String, Int> {
        val explicado = totalDe(quantidades - ID_OUTROS).pf
        val faltando = (pfMax - pfAtual).coerceAtLeast(0)
        val naoExplicado = (faltando - explicado).coerceAtLeast(0)
        val base = quantidades - ID_OUTROS
        return if (naoExplicado > 0) base + (ID_OUTROS to naoExplicado) else base
    }

    /** O PF que a ficha passa a mostrar depois de salvar o painel. */
    fun pfDepoisDoPainel(pfMax: Int, quantidades: Map<String, Int>): Int =
        pfMax - totalDe(quantidades).pf

    /** O texto do rodapé: o que precisa de quê para voltar. */
    fun resumoDaRecuperacao(quantidades: Map<String, Int>): List<String> =
        totalDe(quantidades).porRecuperacao
            .entries
            .sortedByDescending { it.value }
            .map { (rec, pf) -> "$pf PF — ${rec.rotulo}: ${rec.comoVolta}" }
}
