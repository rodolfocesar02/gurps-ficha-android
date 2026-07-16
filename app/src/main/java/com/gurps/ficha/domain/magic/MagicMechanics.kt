package com.gurps.ficha.domain.magic

/**
 * Lote AR-1 (mecânica estruturada das magias): a `descricao` de cada feitiço no catálogo é FIEL ao
 * livro, mas é PROSA — o motor não a executa. Este é o campo `mecanica` legível pela máquina, ao lado
 * da descrição (que fica intocada): a versão CURADA das regras que o motor aplica.
 *
 * O `efeito` é um conjunto FECHADO; o que não couber vira "narrado" (honesto — Criar Ar, Convocar
 * Elemental). Escola por escola, começando por Ar.
 */
data class MagiaMecanica(
    /** "dano" | "condicao" | "buff" | "ambiente" | "controle" | "informacao" | "narrado". */
    val efeito: String = "narrado",

    // ── efeito "dano" ──
    /** Dado por unidade de energia (ex.: "1d-1" no Relâmpago, "1d+1" no Toque Chocante). */
    val danoPorEnergia: String? = null,
    /** Quantos pontos de energia compram 1 "unidade" de dado (1 no Relâmpago, 2 na Concussão). */
    val energiaPorDado: Int = 1,
    /**
     * true quando o dano NÃO escala com a energia investida — a energia compra outra coisa (alcance
     * no Chicote de Relâmpago, raio/duração na Nuvem de Faíscas) ou é custo fixo (Géiser = 3d sempre).
     * Sem isto o Géiser (custo 5) sairia como 15d.
     */
    val danoFixo: Boolean = false,
    /** "quei" (queimadura) | "cont" | "projecao" | "corte"… (default contusão no motor). */
    val tipoDano: String? = null,
    /** null = RD normal; "ignora" = armadura não protege (Toque Chocante); "metal_rd_1" = metal vira RD 1 (aprox.: mantém RD). */
    val armadura: String? = null,
    /** Como acerta: "projetil" | "toque" | "feixe" (DX−4) | "area" | "auto". Complementa a `classe`. */
    val entrega: String? = null,

    // ── condição embutida (rider no dano ou standalone) ──
    /** Condição imposta ("atordoado", "cego"…). */
    val condicao: String? = null,
    /** Teste para resistir à condição: "HT" | "HT-3" | "HT_por_pv" (Relâmpago: −1 por 2 PV). */
    val condicaoResistencia: String? = null,
    /** Raio (m) em que a condição se espalha (Concussão = 10). 0 = só o alvo. */
    val condicaoRaioM: Int = 0,

    // ── efeito "buff" (rastreado como magia ativa; bônus numérico quando houver) ──
    /** Rótulo curto do efeito, para o feed e para o Narrador ("Pele de Crocodilo RD 4"). */
    val buffRotulo: String? = null,
    /** Bônus de dano numa arma, aplicado DEPOIS de penetrar a RD (Arma de Relâmpago = +2). */
    val buffDanoArma: Int = 0,
    /**
     * Em que arma o [buffDanoArma] vale: "cac" (Arma Flamejante é só corpo a corpo) ou "distancia"
     * (Projéteis Flamejantes). null = qualquer. Sem isto o +2 do gume vazaria para o arco.
     */
    val buffArmaTipo: String? = null,
    /**
     * Lote MEC-2 — os números que faltavam. Desenho tirado dos 93 buffs JÁ curados, não de palpite:
     * quase todo buff do livro é "N de bônus POR nível, X de energia por nível, teto de M níveis"
     * (Força = 2 energia por +1 ST, máx 5) ou um valor fixo (Pele de Crocodilo RD 4; Voo = Desloc 10).
     *
     * Níveis = if (buffEnergiaPorNivel > 0) min(energia / buffEnergiaPorNivel, buffMaxNiveis) else 1.
     * Cada campo abaixo é o valor POR NÍVEL — o motor multiplica.
     */
    /** Energia que compra 1 nível do buff (0 = não escala: vale 1 nível fixo). */
    val buffEnergiaPorNivel: Int = 0,
    /** Teto de níveis (Força/Graça/Vigor = 5; Apressar = 3; Nublar = 5). 0 = sem teto. */
    val buffMaxNiveis: Int = 0,
    /** RD concedida por nível (Pele de Crocodilo 4, Proteger Animal 5). */
    val buffRd: Int = 0,
    /** Bônus de Esquiva por nível (Apressar +1/nível). */
    val buffEsquiva: Int = 0,
    /** Atributo alterado: "ST" | "DX" | "HT" (IQ não entra no motor de combate → narrado). */
    val buffAtributo: String? = null,
    /** Valor do atributo por nível — NEGATIVO nos debuffs (Debilitar = −1 ST/nível). */
    val buffAtributoValor: Int = 0,
    /** Delta de Deslocamento por nível (Apressar +1/nível). */
    val buffDeslocamento: Int = 0,
    /** Deslocamento ABSOLUTO — substitui o do combatente (Voo = 10; Voo do Falcão = 40). 0 = não usa. */
    val buffDeslocamentoFixo: Int = 0,
    /** Penalidade por nível ao NH de QUEM ATACA o alvo (Nublar = −1/nível, máx −5). Valor positivo. */
    val buffPenalidadeAtacantes: Int = 0,

    // ── notas para o Narrador (ambiente/controle/utilidade: o motor tagueia, o Mestre descreve) ──
    val notas: String? = null,
)

/**
 * Lote MEC-2: os deltas CONCRETOS que um buff aplicou num combatente. Guardado na mágica ativa para
 * que a expiração reverta EXATAMENTE o que entrou (e não um recálculo, que daria drift se a energia,
 * a postura ou outro buff mudarem no meio).
 */
data class BuffAplicado(
    val alvoId: String,
    val rotulo: String = "",
    val rd: Int = 0,
    val esquiva: Int = 0,
    val st: Int = 0,
    val dx: Int = 0,
    val ht: Int = 0,
    val deslocamento: Int = 0,
    /** Deslocamento absoluto imposto (Voo). null = não mexeu. */
    val deslocamentoFixo: Int? = null,
    val danoArma: Int = 0,
    /** "cac" | "distancia" | null (qualquer) — em que arma o [danoArma] vale. */
    val armaTipo: String? = null,
    val penalidadeAtacantes: Int = 0,
) {
    /** true se o bônus de dano vale para um ataque com este alcance. */
    fun danoArmaVale(aDistancia: Boolean): Boolean = when (armaTipo) {
        "cac" -> !aDistancia
        "distancia" -> aDistancia
        else -> true
    }
    /** true se nada numérico foi aplicado — o efeito é só narrado (Corpo de Água, Ambidestria). */
    val soNarrado: Boolean get() = rd == 0 && esquiva == 0 && st == 0 && dx == 0 && ht == 0 &&
        deslocamento == 0 && deslocamentoFixo == null && danoArma == 0 && penalidadeAtacantes == 0
}

object MagicMechanics {

    /** true se a mágica tem dano estruturado que o motor aplica automaticamente. */
    fun temDanoEstruturado(m: MagiaMecanica?): Boolean = m?.efeito == "dano" && m.danoPorEnergia != null

    /** true se o buff tem NÚMERO que o motor aplica (senão é narrado — regra de ouro). */
    fun temBuffEstruturado(m: MagiaMecanica?): Boolean = m != null && m.efeito == "buff" &&
        (m.buffRd != 0 || m.buffEsquiva != 0 || m.buffAtributoValor != 0 || m.buffDeslocamento != 0 ||
         m.buffDeslocamentoFixo != 0 || m.buffDanoArma != 0 || m.buffPenalidadeAtacantes != 0)

    /**
     * Níveis efetivos do buff dada a energia investida: `energia / buffEnergiaPorNivel`, com teto em
     * `buffMaxNiveis`. Sem escala por energia (buffEnergiaPorNivel = 0) o buff vale 1 nível fixo.
     * Piso 1 — quem conjurou pagou, leva pelo menos 1 nível.
     */
    fun niveisDoBuff(m: MagiaMecanica, energia: Int): Int {
        if (m.buffEnergiaPorNivel <= 0) return 1
        val n = energia / m.buffEnergiaPorNivel
        val teto = if (m.buffMaxNiveis > 0) m.buffMaxNiveis else Int.MAX_VALUE
        return n.coerceIn(1, teto)
    }

    /**
     * Converte a `mecanica` curada + energia investida nos deltas concretos a aplicar no alvo.
     * Cada campo é "por nível" no catálogo; aqui multiplica pelos níveis comprados com a energia.
     */
    fun calcularBuff(m: MagiaMecanica, energia: Int, alvoId: String): BuffAplicado {
        val n = niveisDoBuff(m, energia)
        val atr = m.buffAtributoValor * n
        return BuffAplicado(
            alvoId = alvoId,
            rotulo = m.buffRotulo ?: "",
            rd = m.buffRd * n,
            esquiva = m.buffEsquiva * n,
            st = if (m.buffAtributo.equals("ST", true)) atr else 0,
            dx = if (m.buffAtributo.equals("DX", true)) atr else 0,
            ht = if (m.buffAtributo.equals("HT", true)) atr else 0,
            deslocamento = m.buffDeslocamento * n,
            deslocamentoFixo = if (m.buffDeslocamentoFixo > 0) m.buffDeslocamentoFixo else null,
            danoArma = m.buffDanoArma * n,
            armaTipo = m.buffArmaTipo,
            penalidadeAtacantes = m.buffPenalidadeAtacantes * n,
        )
    }

    /**
     * Expande o dano por energia para a expressão total, escalando pela energia investida.
     * Ex.: "1d-1" por 1 energia, energia 3 → "3d-3"; "1d" por 2 energia, energia 4 → "2d".
     * Regras: dados = (energia / energiaPorDado) coerçado a ≥1; o modificador escala com a contagem.
     *
     * `danoFixo` trava a contagem em 1 (Géiser = 3d sempre, custe o que custar).
     * Dano em PONTOS (Nuvem de Faíscas = "1" ponto/seg) vira "0d+N" — o rolador exige `<n>d` e
     * devolveria 0 para um "1" pelado.
     */
    fun expandirDano(danoPorEnergia: String, energia: Int, energiaPorDado: Int, danoFixo: Boolean = false): String {
        val n = if (danoFixo) 1 else (energia / energiaPorDado.coerceAtLeast(1)).coerceAtLeast(1)
        val txt = danoPorEnergia.replace(" ", "").lowercase()
        val m = Regex("""(\d*)d([+-]\d+)?""").find(txt)
            ?: return txt.toIntOrNull()?.let { "0d+${it * n}" } ?: "${n}d"
        val dadosBase = m.groupValues[1].toIntOrNull() ?: 1
        val modBase = m.groupValues[2].toIntOrNull() ?: 0
        val dados = dadosBase * n
        val mod = modBase * n
        return "${dados}d" + when {
            mod > 0 -> "+$mod"
            mod < 0 -> "$mod"
            else -> ""
        }
    }

    /** Penalidade da condição imposta pela mágica, dado o dano sofrido (ex.: Relâmpago −1 por 2 PV). */
    fun penalidadeCondicaoPorPv(resistencia: String?, pvSofridos: Int): Int = when {
        resistencia == "HT_por_pv" -> -(pvSofridos / 2)
        resistencia != null && resistencia.startsWith("HT-") -> resistencia.removePrefix("HT-").toIntOrNull()?.let { -it } ?: 0
        else -> 0
    }
}
