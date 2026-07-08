package com.gurps.ficha.domain.magic

/**
 * Lote MA-1 (motor de magia, kotlin PURO): parser tolerante do campo `classe` das entradas do
 * catálogo `magias2versao.json`.
 *
 * O campo `classe` vem em várias formas — algumas simples ("Comum", "Área"), várias com resistência
 * embutida ("Comum/R-Vont", "Área/R-HT"), outras multi-classe ("Comum ou Bloqueio", "Especial/Área"),
 * variantes com modificador ("Comum/R-Vont+1", "R-HT+2"), resistências combinadas ("R-HT ou IQ") e
 * até resistências totalmente customizadas ("R-Tranca Mágica", "R-Portal") que dependem de outra
 * magia ativa do alvo.
 *
 * Estratégia (definida pelo usuário): automatizar o que der + delegar ao Narrador o que não couber.
 * Parser NUNCA lança — sempre devolve algo. Casos exóticos caem em [TipoClasseMagia.ESPECIAL] com
 * `resistencia = null` e o motor aplica só o teste da mágica e o custo de FP, deixando o resultado
 * pro Narrador narrar.
 *
 * Também absorve typos reais encontrados no JSON: "Comm" → COMUM, "Projetil" → PROJETIL.
 */

/** Classes canônicas de mágica (Magia p.11–13). Uma mágica pode pertencer a mais de uma. */
enum class TipoClasseMagia {
    /** Afeta 1 alvo. Penalidade de distância = metros. */
    COMUM,
    /** Afeta uma área (raio). Custo = básico × raio. */
    AREA,
    /** Projétil arremessável (2 testes: mágica + Ataque Inato). Bloqueável/esquivável, nunca aparável. */
    PROJETIL,
    /** Carrega a mão/cajado; 2 testes (mágica + ataque corpo-a-corpo). */
    TOQUE,
    /** Reação instantânea contra ataque (defesa mágica). Custo NÃO reduz por NH alto. */
    BLOQUEIO,
    /** Pergunta ao mundo (adivinhação). Gasta energia mesmo em fracasso. */
    INFORMACAO,
    /** Cria/altera item mágico permanente. Fora do combate imediato. */
    ENCANTAMENTO,
    /** Não se encaixa nas anteriores — motor delega resolução ao Narrador. */
    ESPECIAL,
}

/**
 * Tipo de resistência ativa do defensor contra a mágica. Se null, mágica não é resistida.
 * O campo `modificador` é o `±N` embutido no JSON (ex.: R-Vont+1 → HT|null|+1).
 */
data class ResistenciaMagia(
    val atributo: AtributoResistencia,
    /** Modificador aplicado ao NH DEFENSOR (não ao teste do mago). +N deixa defensor mais forte. */
    val modificadorDefensor: Int = 0,
    /**
     * Quando o JSON traz uma resistência combinada ("R-HT ou IQ"), o defensor escolhe o melhor
     * atributo — este campo carrega os alternativos.
     */
    val alternativos: List<AtributoResistencia> = emptyList(),
    /** true quando a descrição da mágica tem nota extra (marcador `#` no JSON). */
    val temNotaEspecial: Boolean = false,
    /**
     * Quando não sabemos qual atributo (R-Tranca Mágica, R-Portal, R-Ocultar Rastros), guardamos o
     * texto original pro Narrador ver e resolver. Motor NÃO tenta automatizar.
     */
    val rotuloCustomizado: String? = null,
)

/** Atributos/perícias sobre os quais a resistência pode ser rolada. */
enum class AtributoResistencia {
    HT, VONTADE, IQ, DX, ST,
    /** Resistência = NH efetivo do mago que criou a magia ativa alvo (Contra-magia). */
    MAGICA,
    /** Resistência = maior valor entre atributo e alguma perícia do defensor. */
    VONTADE_OU_PERICIA,
    /** Fórmula composta que o motor não automatiza (ex.: (ST+Vont)/2). Delega ao Narrador. */
    COMPOSTA,
    /** Marcador para R-Especial / R-Espec. — descrição da mágica define. Delega ao Narrador. */
    ESPECIAL,
}

/** Classe canônica parseada de uma string bruta do JSON. */
data class ClasseParseada(
    /** Classes (podem ser >1 quando o JSON traz "Comum ou Bloqueio" / "Especial/Área"). */
    val classes: Set<TipoClasseMagia>,
    /** Resistência ativa do alvo, ou null se a mágica não é resistida. */
    val resistencia: ResistenciaMagia?,
    /** true quando o parser NÃO conseguiu reconhecer parte da string — informação para debug/logs. */
    val temParteNaoReconhecida: Boolean = false,
    /** String original pra fallback narrativo. */
    val original: String,
)

/**
 * Parseia o campo `classe` bruto do JSON. Nunca lança.
 * Regras:
 *  - Divide por '/' e por ' ou ' (multi-classe).
 *  - Cada segmento é ou uma classe canônica (Comum, Área, Projétil…) ou uma resistência (começa com "R-").
 *  - Aliases: "Comm" → "Comum", "Projetil" → "Projétil", "Bloq." → "Bloqueio", "Encant." → "Encantamento",
 *    "Com." → "Comum", "Espec." → "Especial".
 *  - Resistência "R-XXX+N" → atributo XXX, modificadorDefensor=+N. Marcador '#' vira temNotaEspecial=true.
 *  - Se nenhuma classe conhecida for encontrada → ESPECIAL.
 */
object MagicClassParser {

    private val ALIASES_CLASSE: Map<String, TipoClasseMagia> = mapOf(
        "comum" to TipoClasseMagia.COMUM,
        "comm" to TipoClasseMagia.COMUM,        // typo real do JSON (6 magias)
        "com." to TipoClasseMagia.COMUM,
        "area" to TipoClasseMagia.AREA,
        "área" to TipoClasseMagia.AREA,
        "projetil" to TipoClasseMagia.PROJETIL,  // sem acento (9 magias)
        "projétil" to TipoClasseMagia.PROJETIL,
        "toque" to TipoClasseMagia.TOQUE,
        "bloqueio" to TipoClasseMagia.BLOQUEIO,
        "bloq." to TipoClasseMagia.BLOQUEIO,
        "informação" to TipoClasseMagia.INFORMACAO,
        "informacao" to TipoClasseMagia.INFORMACAO,
        "encantamento" to TipoClasseMagia.ENCANTAMENTO,
        "encant." to TipoClasseMagia.ENCANTAMENTO,
        "especial" to TipoClasseMagia.ESPECIAL,
        "espec." to TipoClasseMagia.ESPECIAL,
    )

    private val ALIASES_ATRIBUTO: Map<String, AtributoResistencia> = mapOf(
        "ht" to AtributoResistencia.HT,
        "vont" to AtributoResistencia.VONTADE,
        "vontade" to AtributoResistencia.VONTADE,
        "iq" to AtributoResistencia.IQ,
        "dx" to AtributoResistencia.DX,
        "st" to AtributoResistencia.ST,
        "mágica" to AtributoResistencia.MAGICA,
        "magica" to AtributoResistencia.MAGICA,
    )

    fun parse(classeString: String?): ClasseParseada {
        val original = classeString ?: ""
        if (original.isBlank()) {
            return ClasseParseada(
                classes = setOf(TipoClasseMagia.ESPECIAL),
                resistencia = null,
                temParteNaoReconhecida = true,
                original = original
            )
        }
        // Pré-normalização: aceitar "R/XXX" (2 magias no JSON usam barra em vez de hífen).
        // "Comum/R/HT" → "Comum/R-HT".
        val normalizado = original.replace(Regex("(^|/)\\s*R/(?=[A-Za-zÀ-ú])"), "$1R-")

        // Divide primeiro por '/' (separator claro). O " ou " só vira separator de MULTI-CLASSE se
        // o segmento correspondente NÃO for resistência (R-XXX). Resistências mantêm " ou " intacto
        // e o parser interno de resistência lida com "R-HT ou IQ".
        val segmentos = normalizado.split("/").map { it.trim() }.filter { it.isNotEmpty() }
        val classes = mutableSetOf<TipoClasseMagia>()
        var resistencia: ResistenciaMagia? = null
        var temParteNaoReconhecida = false

        for (seg in segmentos) {
            val segLower = seg.lowercase().trim()
            if (segLower.startsWith("r-") || segLower.startsWith("r ")) {
                // Resistência. Mantém " ou " intacto — parseResistencia trata combinadas.
                val rest = seg.substring(2).trim() // remove "R-" ou "R "
                val parsed = parseResistencia(rest)
                if (parsed != null) {
                    if (resistencia != null) temParteNaoReconhecida = true
                    resistencia = parsed
                } else {
                    temParteNaoReconhecida = true
                }
            } else {
                // Pode ser 1 classe simples OU "Comum ou Bloqueio" (multi-classe sem '/').
                val subSegmentos = seg.split(" ou ").map { it.trim() }
                var reconheciAlgum = false
                for (sub in subSegmentos) {
                    val classe = ALIASES_CLASSE[sub.lowercase()]
                    if (classe != null) {
                        classes.add(classe)
                        reconheciAlgum = true
                    }
                }
                if (!reconheciAlgum) temParteNaoReconhecida = true
            }
        }

        // Se nenhuma classe foi reconhecida, cai em ESPECIAL. O motor ainda automatiza o teste da
        // mágica e o custo, apenas delega o RESULTADO ao Narrador.
        val classesFinal = if (classes.isEmpty()) setOf(TipoClasseMagia.ESPECIAL) else classes
        return ClasseParseada(
            classes = classesFinal,
            resistencia = resistencia,
            temParteNaoReconhecida = temParteNaoReconhecida,
            original = original
        )
    }

    /** Parseia "Vont+1", "HT ou IQ", "Vont+AM", "Especial", "Tranca Mágica", "#". */
    private fun parseResistencia(rest: String): ResistenciaMagia? {
        if (rest.isBlank()) return null
        var texto = rest.trim()
        val temNotaEspecial = texto.endsWith("#")
        if (temNotaEspecial) texto = texto.dropLast(1).trim()

        // R-Especial / R-Espec. → COMPOSTA, delega ao Narrador
        val textoLower = texto.lowercase()
        if (textoLower.startsWith("espec")) {
            return ResistenciaMagia(
                atributo = AtributoResistencia.ESPECIAL,
                temNotaEspecial = temNotaEspecial,
                rotuloCustomizado = texto,
            )
        }

        // Resistência combinada: "HT ou IQ", "ST ou Vont"
        if (" ou " in textoLower) {
            val partes = textoLower.split(" ou ").map { it.trim() }
            val atributos = partes.mapNotNull { ALIASES_ATRIBUTO[it] }
            if (atributos.isNotEmpty()) {
                // Se algum item não parseou (ex.: "Vont ou perícia"), guardamos como alternativo com
                // rotulo customizado + temParteNaoReconhecida sinaliza depois.
                val primeiro = atributos.first()
                val outros = atributos.drop(1)
                return ResistenciaMagia(
                    atributo = primeiro,
                    alternativos = outros,
                    temNotaEspecial = temNotaEspecial,
                    rotuloCustomizado = if (atributos.size < partes.size) texto else null,
                )
            }
        }

        // Resistência composta com "+" ou "-" ou "/" ou parênteses — parser não automatiza a
        // fórmula, delega ao Narrador (motor só sabe QUAL a base).
        val (baseTexto, modificador) = extrairModificador(texto)

        // Casos R-Tranca Mágica, R-Portal, R-Ocultar Rastros — palavras que não são atributos.
        val atributoBase = ALIASES_ATRIBUTO[baseTexto.lowercase().trim()]
        if (atributoBase == null) {
            // Verifica se é COMPOSTA — presença de parênteses ou "+AM".
            val ehComposta = "(" in baseTexto || "+" in baseTexto || "am" in baseTexto.lowercase()
            return ResistenciaMagia(
                atributo = if (ehComposta) AtributoResistencia.COMPOSTA else AtributoResistencia.ESPECIAL,
                temNotaEspecial = temNotaEspecial,
                rotuloCustomizado = texto,
            )
        }
        return ResistenciaMagia(
            atributo = atributoBase,
            modificadorDefensor = modificador,
            temNotaEspecial = temNotaEspecial,
            rotuloCustomizado = if (modificador == 0) null else null,
        )
    }

    /** Extrai "Vont+1" → ("Vont", +1); "HT-2" → ("HT", -2); "Vont" → ("Vont", 0). */
    private fun extrairModificador(texto: String): Pair<String, Int> {
        val regex = Regex("([+-]\\d+)$")
        val match = regex.find(texto)
        if (match != null) {
            val mod = match.groupValues[1].toIntOrNull() ?: 0
            val base = texto.substring(0, match.range.first).trim()
            return base to mod
        }
        return texto to 0
    }
}
