package com.gurps.ficha.domain.rules

import kotlin.math.floor

/**
 * **Ferimento por local do corpo** — Lote MB-7 (MB p.399-400 e 419-422).
 *
 * O botão **PV** da aba Rolagem. O jogador diz *onde* levou e *quanto*; o app
 * faz a conta inteira, na ordem certa, e diz o que aconteceu com o membro.
 *
 * ## A ordem importa, e ela não é óbvia
 *
 * ```
 * dano bruto − RD (da armadura + a natural do crânio)
 *            → × multiplicador de ferimento (do tipo E do local)
 *            → teto do membro (o excesso é DESPERDIÇADO)
 *            → PV perdidos
 * ```
 *
 * Quem multiplica antes de tirar a RD infla o ferimento; quem aplica o teto do
 * membro antes de multiplicar o encolhe. As duas trocas dão números plausíveis —
 * e errados.
 *
 * ## 🔴 O achado deste lote: o teto do membro é `PV/2 + 1`, não `PV/2`
 *
 * O livro dá o **mínimo necessário para incapacitar**, e ele é o menor inteiro
 * **acima** da fração. Está no exemplo trabalhado da própria página:
 *
 * > Ele teve (…) PV 14. (…) o orc atinge o braço direito (…) 11 PV. Esse valor é
 * > bem acima de PV/2 (…) **No caso de Friedrick, PV/2 é 7. Dano maior que PV/2 é
 * > 8 PV, então ele perde apenas 8 PV.**
 *
 * Não 7 — **8**. E de novo na p.421: *"se um homem com 10 PV sofrer 9 pontos de
 * dano no braço direito, ele só perde 6 PV"* (PV/2 = 5, mínimo = **6**).
 *
 * ⚠️ Arredondar "para cima" acerta com PV ímpar e **erra 1 ponto com PV par** —
 * que é metade dos personagens. Por isso a conta aqui é `floor(PV × fração) + 1`,
 * e há teste com os dois exemplos do livro.
 *
 * Kotlin puro, sem Android — testável.
 */
object FerimentoPorLocalRules {

    // ==================================================================
    // 1. Os limiares
    // ==================================================================

    /** A fração dos PV que aquele local aguenta antes de ser incapacitado. */
    private fun fracaoQueIncapacita(local: LocalAtaque): Double? = when (local) {
        // Membro: braço, perna, asa, golpeador ou cauda preênsil.
        LocalAtaque.BRACO, LocalAtaque.PERNA -> 1.0 / 2
        // Extremidade: mão, pé, cauda, nadadeira ou cabeça desconexa.
        LocalAtaque.MAO, LocalAtaque.PE -> 1.0 / 3
        LocalAtaque.OLHO -> 1.0 / 10
        else -> null
    }

    /**
     * O **menor** dano que incapacita aquele local, ou `null` se ele não
     * incapacita (torso, crânio, rosto, pescoço, vitais, virilha).
     *
     * 🔴 É `floor(PV × fração) + 1` — o menor inteiro **estritamente acima** da
     * fração. Ver o exemplo do livro no cabeçalho do arquivo.
     */
    fun minimoQueIncapacita(local: LocalAtaque, pvInicial: Int): Int? {
        val fracao = fracaoQueIncapacita(local) ?: return null
        if (pvInicial <= 0) return null
        return floor(pvInicial * fracao).toInt() + 1
    }

    /**
     * O dano que **destrói** o membro em vez de só incapacitar (MB p.421).
     *
     * > Se a lesão causada a um membro do corpo **antes da limitação** for pelo
     * > menos **o dobro do necessário** para incapacitá-lo, o membro não terá
     * > sido somente incapacitado, mas **destruído**.
     *
     * ⚠️ "O necessário para incapacitá-lo" é o **mínimo** da linha acima — a
     * mesma expressão que o parágrafo anterior usa. Então é `2 × mínimo`, e não
     * `2 × PV/2`; a diferença é de 2 pontos e aparece em toda mesa que joga com
     * decepamento. Está documentado aqui porque é leitura, não cálculo.
     *
     * E "antes da limitação" é literal: quem aplica o teto do membro primeiro
     * **nunca** consegue decepar nada, porque o teto é metade do gatilho.
     */
    fun minimoQueDeceps(local: LocalAtaque, pvInicial: Int): Int? =
        minimoQueIncapacita(local, pvInicial)?.let { it * 2 }

    // ==================================================================
    // 2. RD natural e multiplicador
    // ==================================================================

    /**
     * RD que o corpo dá de graça naquele local.
     *
     * ⚠️ Só o crânio, e **o olho não herda**: *"trate como um golpe no crânio
     * **sem** a RD adicional de 2"* (MB p.400). É a diferença que faz uma flecha
     * no olho passar onde a mesma flecha na testa pararia.
     */
    fun rdExtraNatural(local: LocalAtaque): Int = if (local == LocalAtaque.CRANIO) 2 else 0

    /**
     * O multiplicador de ferimento do **tipo** de dano combinado com o **local**.
     *
     * O que o local muda (MB p.399-400):
     * - **Crânio**: ×4 para todos os tipos.
     * - **Olho**: como o crânio (×4), mas sem a RD extra.
     * - **Pescoço**: ×2 no corte, ×1,5 na contusão.
     * - **Vitais**: ×3 para perfurante e perfuração.
     * - **Braço, perna, mão, pé**: ⚠️ perfuração e muito/extremamente perfurante
     *   caem para **×1** contra alvo vivo — atravessar um braço não mata.
     *
     * ⚠️ **Só para ser vivo comum.** Morto-vivo, máquina e enxame têm a sua
     * própria tabela (Tolerância a Ferimentos, MB p.381) e ela já vive no motor
     * de combate — o botão PV é do personagem do jogador, então trazer aquela
     * tabela para cá seria duplicar regra para um caso que esta tela não tem.
     */
    fun multiplicador(tipo: DanoTipo, local: LocalAtaque): Double {
        return when (local) {
            LocalAtaque.CRANIO, LocalAtaque.OLHO -> 4.0
            LocalAtaque.PESCOCO -> when (tipo) {
                DanoTipo.CORT -> 2.0
                DanoTipo.CONT -> 1.5
                else -> tipo.multBase
            }
            LocalAtaque.VITAIS -> if (tipo.perfuranteOuPerf) 3.0 else tipo.multBase
            LocalAtaque.BRACO, LocalAtaque.PERNA, LocalAtaque.MAO, LocalAtaque.PE ->
                when (tipo) {
                    DanoTipo.PERF, DanoTipo.PI_MAIS, DanoTipo.PI_MAIS_MAIS -> 1.0
                    else -> tipo.multBase
                }
            else -> tipo.multBase
        }
    }

    // ==================================================================
    // 3. Choque e nocaute
    // ==================================================================

    /**
     * A penalidade de **choque** do próximo turno (MB p.419).
     *
     * > Sempre que sofrer dano, um personagem sofre uma penalidade na DX e IQ
     * > igual aos PV perdidos — até um máximo de **-4**. (…) Um personagem com
     * > **20 ou mais** Pontos de Vida sofre -1 para cada **PV/10** pontos.
     *
     * ⚠️ Na **virilha**, homens humanos sofrem o **dobro** do choque por contusão,
     * até -8 (MB p.400) — é a única exceção ao teto de -4.
     */
    fun choque(
        pvPerdidos: Int,
        pvInicial: Int,
        local: LocalAtaque = LocalAtaque.TORSO,
        tipo: DanoTipo = DanoTipo.CONT,
        masculino: Boolean = true
    ): Int {
        if (pvPerdidos <= 0) return 0
        val divisor = if (pvInicial >= 20) (pvInicial / 10).coerceAtLeast(1) else 1
        val bruto = (pvPerdidos / divisor).coerceAtMost(4)
        val dobra = local == LocalAtaque.INGLE && tipo == DanoTipo.CONT && masculino
        return if (dobra) -(bruto * 2).coerceAtMost(8) else -bruto
    }

    /** Um teste de HT que o livro exige, já com o modificador somado. */
    data class TesteDeNocaute(val modificador: Int, val motivo: String) {
        fun alvoCom(ht: Int): Int = ht + modificador
    }

    /**
     * O modificador do teste de nocaute pelo local (MB p.420).
     *
     * > -5 para um ferimento grave no **rosto ou órgãos vitais** (ou na
     * > **virilha** no caso de um humanoide macho); **-10** para um ferimento
     * > grave no **crânio ou olhos**.
     */
    private fun modificadorDeNocaute(local: LocalAtaque, masculino: Boolean): Int = when (local) {
        LocalAtaque.CRANIO, LocalAtaque.OLHO -> -10
        LocalAtaque.ROSTO, LocalAtaque.VITAIS -> -5
        LocalAtaque.INGLE -> if (masculino) -5 else 0
        else -> 0
    }

    /** Locais em que basta haver choque para exigir o teste (MB p.420). */
    private fun locaisSensiveis(local: LocalAtaque): Boolean = local in setOf(
        LocalAtaque.CRANIO, LocalAtaque.ROSTO, LocalAtaque.OLHO, LocalAtaque.VITAIS
    )

    // ==================================================================
    // 4. A conta inteira
    // ==================================================================

    /** O que aconteceu com o membro. */
    enum class EfeitoNoLocal { NENHUM, INCAPACITADO, DECEPADO, CEGOU }

    data class Resultado(
        val danoBruto: Int,
        val rdEfetiva: Int,
        val penetrante: Int,
        val multiplicador: Double,
        /** A lesão ANTES do teto do membro — é ela que decide o decepamento. */
        val lesaoAntesDoTeto: Int,
        val pvPerdidos: Int,
        val desperdicado: Int,
        val efeito: EfeitoNoLocal,
        val ferimentoGrave: Boolean,
        val choque: Int,
        val testeDeNocaute: TesteDeNocaute?,
        val avisos: List<String>
    ) {
        val conta: String
            get() = "$danoBruto − RD $rdEfetiva = $penetrante × $multiplicador = " +
                "$lesaoAntesDoTeto" + if (desperdicado > 0) " → $pvPerdidos (o resto é desperdiçado)" else ""
    }

    /**
     * Aplica um golpe.
     *
     * @param pvInicial o PV **máximo** — é dele que saem todos os limiares.
     * @param rdArmadura a RD das peças vestidas naquele local (a natural do
     *   crânio é somada aqui dentro, não passe ela junto).
     */
    fun aplicar(
        pvInicial: Int,
        danoBruto: Int,
        tipo: DanoTipo,
        local: LocalAtaque,
        rdArmadura: Int = 0,
        masculino: Boolean = true
    ): Resultado {
        val rdEfetiva = rdArmadura.coerceAtLeast(0) + rdExtraNatural(local)
        val penetrante = (danoBruto - rdEfetiva).coerceAtLeast(0)
        val mult = multiplicador(tipo, local)
        // "Arredonde as frações para baixo, com um dano mínimo de 1 PV para
        // qualquer ataque que penetre a RD" (MB p.379).
        val lesao = if (penetrante <= 0) 0 else floor(penetrante * mult).toInt().coerceAtLeast(1)

        val minimo = minimoQueIncapacita(local, pvInicial)
        val limiteDeceps = minimoQueDeceps(local, pvInicial)
        var pvPerdidos = lesao
        var efeito = EfeitoNoLocal.NENHUM
        val avisos = mutableListOf<String>()

        if (minimo != null && lesao >= minimo) {
            efeito = when {
                local == LocalAtaque.OLHO -> EfeitoNoLocal.CEGOU
                limiteDeceps != null && lesao >= limiteDeceps -> EfeitoNoLocal.DECEPADO
                else -> EfeitoNoLocal.INCAPACITADO
            }
            // ⚠️ O teto NÃO vale para o olho: "esse limite não se aplica aos
            // olhos!" (MB p.421). Uma flecha no olho mata.
            if (local != LocalAtaque.OLHO) pvPerdidos = minimo
        }

        val desperdicado = lesao - pvPerdidos

        // Ferimento grave: um único ferimento acima de PVinicial/2 — OU qualquer
        // lesão incapacitante (MB p.420).
        val grave = pvPerdidos * 2 > pvInicial || efeito != EfeitoNoLocal.NENHUM
        val choquePenalidade = choque(pvPerdidos, pvInicial, local, tipo, masculino)

        val teste = when {
            grave -> TesteDeNocaute(
                modificadorDeNocaute(local, masculino),
                if (efeito != EfeitoNoLocal.NENHUM) {
                    "Lesão incapacitante também é ferimento grave"
                } else {
                    "Ferimento grave (mais de metade do PV inicial num golpe só)"
                }
            )
            locaisSensiveis(local) && choquePenalidade != 0 -> TesteDeNocaute(
                modificadorDeNocaute(local, masculino),
                "Golpe na cabeça ou nos vitais com dano suficiente para causar choque"
            )
            else -> null
        }

        avisos += avisosDe(local, tipo, efeito, desperdicado, pvInicial)

        return Resultado(
            danoBruto = danoBruto,
            rdEfetiva = rdEfetiva,
            penetrante = penetrante,
            multiplicador = mult,
            lesaoAntesDoTeto = lesao,
            pvPerdidos = pvPerdidos,
            desperdicado = desperdicado,
            efeito = efeito,
            ferimentoGrave = grave,
            choque = choquePenalidade,
            testeDeNocaute = teste,
            avisos = avisos
        )
    }

    private fun avisosDe(
        local: LocalAtaque,
        tipo: DanoTipo,
        efeito: EfeitoNoLocal,
        desperdicado: Int,
        pvInicial: Int
    ): List<String> = buildList {
        if (desperdicado > 0) {
            add(
                "$desperdicado ponto(s) de lesão foram desperdiçados: um golpe num membro " +
                    "nunca causa mais que o mínimo necessário para incapacitá-lo (MB p.421)."
            )
        }
        when (efeito) {
            EfeitoNoLocal.INCAPACITADO -> add(consequencia(local))
            EfeitoNoLocal.DECEPADO -> add(
                "⚠️ MEMBRO DESTRUÍDO — a lesão passou do dobro do necessário para incapacitar. " +
                    "Corte ou explosão separam o membro; os outros tipos o deixam irrecuperável. " +
                    consequencia(local)
            )
            EfeitoNoLocal.CEGOU -> add(
                "O olho atingido está cego. Até ser curado, o personagem tem Zarolho — " +
                    "ou Cegueira, se perder os dois (MB p.421)."
            )
            EfeitoNoLocal.NENHUM -> Unit
        }
        if (local == LocalAtaque.ROSTO) {
            add(
                "Muitos elmos são abertos na frente: pergunte ao Mestre se a RD do capacete " +
                    "conta neste golpe (MB p.399)."
            )
        }
        if (local == LocalAtaque.PESCOCO && tipo == DanoTipo.CORT) {
            add("O Mestre pode decidir que um corte no pescoço que mata também decapita (MB p.399).")
        }
        if (local == LocalAtaque.CRANIO) {
            add("O crânio já teve a sua RD natural de +2 descontada aqui — não some de novo.")
        }
        if (local == LocalAtaque.OLHO) {
            add("Dano acima de PV/${10} (aqui: ${minimoQueIncapacita(LocalAtaque.OLHO, pvInicial)}) cega o olho.")
        }
    }

    private fun consequencia(local: LocalAtaque): String = when (local) {
        LocalAtaque.MAO ->
            "Derruba o que estava nessa mão e não segura mais nada com ela. Ainda pode " +
                "bloquear com um escudo preso ao braço, mas não atacar com ele. Ganha Maneta " +
                "(Uma Mão) até ser curado (MB p.421)."
        LocalAtaque.BRACO ->
            "Derruba o que estava na mão e não carrega mais nada com esse braço. Não deixa o " +
                "escudo cair, mas não bloqueia com ele — e o Bônus de Defesa cai 1. Ganha " +
                "Maneta (Um Braço) até ser curado (MB p.421)."
        LocalAtaque.PE ->
            "Cai! Não fica em pé nem anda sem muleta ou apoio. Ainda luta escorado numa " +
                "parede, de joelhos ou sentado. Ganha Deficiente Físico (Perna Incapacitada) " +
                "até ser curado (MB p.421)."
        LocalAtaque.PERNA ->
            "Cai! Ainda luta sentado ou deitado. Ganha Deficiente Físico (Perna Faltando) " +
                "até ser curado (MB p.421)."
        else -> "Lesão incapacitante (MB p.421)."
    }

    // ==================================================================
    // 5. O estado do personagem depois do golpe
    // ==================================================================

    /**
     * Os marcos de PV baixo (MB p.419-420).
     *
     * A ficha já tem o [MarcosDeVidaRules] cuidando dos testes; aqui fica só o
     * texto do que está valendo agora, para o diálogo poder mostrar sem
     * disparar teste nenhum.
     */
    fun situacao(pvAtual: Int, pvInicial: Int): List<String> = buildList {
        if (pvInicial <= 0) return@buildList
        if (pvAtual * 3 < pvInicial && pvAtual > 0) {
            add("Menos de 1/3 dos PV: Deslocamento e Esquiva pela metade (arredondado para cima).")
        }
        if (pvAtual <= 0) {
            add(
                "0 PV ou menos: teste de HT no início de cada turno para continuar agindo, " +
                    "com -1 para cada múltiplo inteiro do PV inicial abaixo de zero. " +
                    "Fazer Nada sem defesa ativa dispensa o teste."
            )
        }
        if (pvAtual <= -pvInicial) {
            val multiplo = (-pvAtual) / pvInicial
            add(
                "-${multiplo}×PV inicial: teste de HT imediato para não morrer. Refaz a cada " +
                    "novo múltiplo negativo."
            )
        }
        if (pvAtual <= -5 * pvInicial) add("⚠️ -5×PV inicial: morte imediata.")
        if (pvAtual <= -10 * pvInicial) add("⚠️ -10×PV inicial: destruição corporal total.")
    }
}
