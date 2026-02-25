package com.gurps.ficha.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.gurps.ficha.domain.rules.CharacterRules
import com.gurps.ficha.domain.rules.CombatRules

/**
 * Modelo de dados para a Ficha de Personagem GURPS 4a Edicao
 */
data class Personagem(
    // Informacoes Basicas
    var nome: String = "",
    var jogador: String = "",
    var campanha: String = "",
    var pontosIniciais: Int = 150,
    var limiteDesvantagens: Int = -75, // Limite padrao GURPS 4Ed

    // Atributos Primarios (valor 10 = media humana, gratuito)
    var forca: Int = 10,      // ST - +/-10 pontos/nivel
    var destreza: Int = 10,   // DX - +/-20 pontos/nivel
    var inteligencia: Int = 10, // IQ - +/-20 pontos/nivel
    var vitalidade: Int = 10,  // HT - +/-10 pontos/nivel

    // Modificadores dos Atributos Secundarios
    var modPontosVida: Int = 0,
    var modVontade: Int = 0,
    var modPercepcao: Int = 0,
    var modPontosFadiga: Int = 0,
    var modVelocidadeBasica: Float = 0f,
    var modDeslocamentoBasico: Int = 0,

    // Listas
    var vantagens: List<VantagemSelecionada> = emptyList(),
    var desvantagens: List<DesvantagemSelecionada> = emptyList(),
    var peculiaridades: List<String> = emptyList(),
    var pericias: List<PericiaSelecionada> = emptyList(),
    var magias: List<MagiaSelecionada> = emptyList(),
    var equipamentos: List<Equipamento> = emptyList(),

    // Descricao
    var aparencia: String = "",
    var historico: String = "",
    var notas: String = "",

    // Combate
    var defesasAtivas: DefesasAtivas = DefesasAtivas()
) {
    // === CALCULOS AUTOMATICOS ===
    val pontosVida: Int get() = forca + modPontosVida
    val vontade: Int get() = inteligencia + modVontade
    val percepcao: Int get() = inteligencia + modPercepcao
    val pontosFadiga: Int get() = vitalidade + modPontosFadiga
    val velocidadeBasica: Float get() = (vitalidade + destreza) / 4f + modVelocidadeBasica
    val deslocamentoBasico: Int get() = velocidadeBasica.toInt() + modDeslocamentoBasico
    val esquiva: Int get() = (velocidadeBasica + 3).toInt() // Esquiva Básica (sem carga)
    val baseCarga: Float get() = (forca * forca) / 10f
    val danoGdP: String get() = CharacterRules.calcularDanoGdP(forca)
    val danoGeB: String get() = CharacterRules.calcularDanoGeB(forca)

    val pesoTotal: Float get() = equipamentos.sumOf {
        (it.peso * it.quantidade).toDouble()
    }.toFloat()

    val nivelCarga: Int get() = CharacterRules.calcularNivelCarga(baseCarga, pesoTotal)

    val deslocamentoAtual: Int get() = CharacterRules.calcularDeslocamentoAtual(
        deslocamentoBasico = deslocamentoBasico,
        nivelCarga = nivelCarga
    )

    // === CALCULO DE PONTOS ===
    val pontosAtributos: Int get() = CharacterRules.calcularPontosAtributos(
        forca = forca,
        destreza = destreza,
        inteligencia = inteligencia,
        vitalidade = vitalidade
    )

    val pontosSecundarios: Int get() = CharacterRules.calcularPontosSecundarios(
        modPontosVida = modPontosVida,
        modVontade = modVontade,
        modPercepcao = modPercepcao,
        modPontosFadiga = modPontosFadiga,
        modVelocidadeBasica = modVelocidadeBasica,
        modDeslocamentoBasico = modDeslocamentoBasico
    )

    val pontosVantagens: Int get() = vantagens.sumOf { it.custoFinal }
    val pontosDesvantagens: Int get() = desvantagens.sumOf { it.custoFinal }
    val pontosPeculiaridades: Int get() = peculiaridades.size * -1
    val pontosPericias: Int get() = pericias.sumOf { it.pontosGastos }
    val pontosMagias: Int get() = magias.sumOf { it.pontosGastos.coerceAtLeast(1) }

    val pontosGastos: Int get() =
        pontosAtributos + pontosSecundarios + pontosVantagens +
        pontosDesvantagens + pontosPeculiaridades + pontosPericias + pontosMagias

    val pontosRestantes: Int get() = pontosIniciais - pontosGastos
    val desvantagensExcedemLimite: Boolean get() = pontosDesvantagens < limiteDesvantagens

    fun getAtributo(sigla: String): Int {
        return when (sigla.uppercase()) {
            "ST" -> forca
            "DX" -> destreza
            "IQ" -> inteligencia
            "HT" -> vitalidade
            "PER" -> percepcao
            "VON" -> vontade
            else -> 10
        }
    }

    fun toJson(): String = Gson().toJson(this)

    companion object {
        fun fromJson(json: String): Personagem {
            val gson = Gson()
            val jsonObject = com.google.gson.JsonParser.parseString(json).asJsonObject
            if (!jsonObject.has("magias")) {
                jsonObject.add("magias", com.google.gson.JsonArray())
            }
            return gson.fromJson(jsonObject, Personagem::class.java)
        }
    }
}

// ============================================================
// TIPOS DE CUSTO
// ============================================================

enum class TipoCusto {
    @SerializedName("fixo") FIXO,
    @SerializedName("escolha") ESCOLHA,
    @SerializedName("variavel") VARIAVEL,
    @SerializedName("por_nivel") POR_NIVEL
}

// ============================================================
// VANTAGENS
// ============================================================

data class VantagemDefinicao(
    val id: String = "",
    val nome: String = "",
    val custo: String = "0",
    val tipoCusto: TipoCusto = TipoCusto.FIXO,
    val pagina: Int = 0,
    val tags: List<String> = emptyList()
) {
    fun getCustoBase(): Int {
        val cleaned = custo.replace(Regex("[^0-9-]"), " ").trim()
        val match = Regex("(-?\\d+)").find(cleaned)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    fun getCustoPorNivel(): Int {
        val match = Regex("(\\d+)/n").find(custo.lowercase())
        return match?.groupValues?.get(1)?.toIntOrNull() ?: getCustoBase()
    }

    fun getOpcoesEscolha(): List<Int> {
        val opcoes = mutableListOf<Int>()
        Regex("-?\\d+").findAll(custo).forEach {
            it.value.toIntOrNull()?.let { v -> opcoes.add(v) }
        }
        return opcoes.distinct().sorted()
    }

    fun getIntervaloVariavel(): Pair<Int, Int> {
        val numeros = Regex("\\d+").findAll(custo).map { it.value.toInt() }.toList()
        return if (numeros.size >= 2) {
            Pair(numeros.minOrNull() ?: 0, numeros.maxOrNull() ?: 100)
        } else {
            Pair(numeros.firstOrNull() ?: 1, 100)
        }
    }
}

data class VantagemSelecionada(
    val definicaoId: String = "",
    val nome: String = "",
    var custoBase: Int = 0, // Custo unitario (por nivel) ou custo fixo
    var nivel: Int = 1,
    var custoEscolhido: Int = 0, // Custo total escolhido (para VARIAVEL/ESCOLHA)
    var descricao: String = "",
    val tipoCusto: TipoCusto = TipoCusto.FIXO,
    val pagina: Int = 0
) {
    val custoFinal: Int get() {
        // Regra Especial: Aptidão Mágica (Magery)
        // Nível 1 (Magery 0) = 5 pts
        // Nível 2 (Magery 1) = 15 pts (5 + 10)
        // Nível 3 (Magery 2) = 25 pts (5 + 20)...
        return CharacterRules.calcularCustoVantagem(
            definicaoId = definicaoId,
            tipoCusto = tipoCusto,
            custoBase = custoBase,
            custoEscolhido = custoEscolhido,
            nivel = nivel
        )
    }
}

// ============================================================
// DESVANTAGENS
// ============================================================

data class DesvantagemDefinicao(
    val id: String = "",
    val nome: String = "",
    val custo: String = "0",
    val tipoCusto: TipoCusto = TipoCusto.FIXO,
    val pagina: Int = 0,
    val tags: List<String> = emptyList()
) {
    fun getCustoBase(): Int {
        val cleaned = custo.replace("?", "").replace("verificar", "").trim()
        val match = Regex("(-?\\d+)").find(cleaned)
        val valor = match?.groupValues?.get(1)?.toIntOrNull() ?: 0
        return if (valor > 0) -valor else valor // Desvantagens DEVEM ser negativas
    }

    fun getCustoPorNivel(): Int {
        val match = Regex("(-?\\d+)/n").find(custo.lowercase())
        val valor = match?.groupValues?.get(1)?.toIntOrNull() ?: getCustoBase()
        return if (valor > 0) -valor else valor
    }

    fun getOpcoesEscolha(): List<Int> {
        val opcoes = mutableListOf<Int>()
        Regex("-?\\d+").findAll(custo).forEach {
            val v = it.value.toIntOrNull() ?: return@forEach
            opcoes.add(if (v > 0) -v else v)
        }
        return opcoes.distinct().sortedDescending()
    }

    fun getIntervaloVariavel(): Pair<Int, Int> {
        val numeros = Regex("\\d+").findAll(custo).map { -it.value.toInt() }.toList()
        return if (numeros.size >= 2) {
            Pair(numeros.minOrNull() ?: -100, numeros.maxOrNull() ?: -1)
        } else {
            Pair(-100, numeros.firstOrNull() ?: -1)
        }
    }
}

data class DesvantagemSelecionada(
    val definicaoId: String = "",
    val nome: String = "",
    var custoBase: Int = 0,
    var nivel: Int = 1,
    var custoEscolhido: Int = 0,
    var descricao: String = "",
    var autocontrole: Int? = null,
    val tipoCusto: TipoCusto = TipoCusto.FIXO,
    val pagina: Int = 0
) {
    val custoFinal: Int get() {
        return CharacterRules.calcularCustoDesvantagem(
            tipoCusto = tipoCusto,
            custoBase = custoBase,
            custoEscolhido = custoEscolhido,
            nivel = nivel,
            autocontrole = autocontrole
        )
    }
}

// ============================================================
// PERICIAS
// ============================================================

enum class Dificuldade(val sigla: String, val nomeCompleto: String) {
    FACIL("F", "Facil"),
    MEDIA("M", "Media"),
    DIFICIL("D", "Dificil"),
    MUITO_DIFICIL("MD", "Muito Dificil");

    companion object {
        fun fromSigla(sigla: String?): Dificuldade = when (sigla?.uppercase()) {
            "F" -> FACIL; "M" -> MEDIA; "D" -> DIFICIL; "MD" -> MUITO_DIFICIL; else -> MEDIA
        }
    }
}

enum class AtributoBase(val sigla: String, val nomeCompleto: String) {
    ST("ST", "Forca"), DX("DX", "Destreza"), IQ("IQ", "Inteligencia"),
    HT("HT", "Vitalidade"), PER("PER", "Percepcao"), VON("VON", "Vontade");

    companion object {
        fun fromSigla(sigla: String?): AtributoBase = when (sigla?.uppercase()) {
            "ST" -> ST; "DX" -> DX; "IQ" -> IQ; "HT" -> HT; "PER" -> PER; "VON" -> VON; else -> IQ
        }
    }
}

data class PericiaDefinicao(
    val id: String = "",
    val nome: String = "",
    val atributoBase: String = "IQ",
    @SerializedName(
        value = "atributosPossiveis",
        alternate = ["atributosPossíveis", "atributosPossÃ­veis"]
    )
    val atributosPossiveis: List<String>? = null,
    val atributoEscolhaObrigatoria: Boolean = false,
    val dificuldadeFixa: String? = "M",
    val dificuldadeVariavel: Boolean = false,
    val exigeEspecializacao: Boolean = false,
    @SerializedName(
        value = "preDefinicoes",
        alternate = ["preDefinições", "preDefiniÃ§Ãµes"]
    )
    val preDefinicoes: List<PreDefinicao> = emptyList()
)

data class PreDefinicao(val atributo: String = "", val modificador: Int = 0)

data class PericiaSelecionada(
    val definicaoId: String = "",
    var nome: String = "",
    var atributoBase: AtributoBase = AtributoBase.IQ,
    var dificuldade: Dificuldade = Dificuldade.MEDIA,
    var pontosGastos: Int = 1,
    var especializacao: String = "",
    val exigeEspecializacao: Boolean = false
) {
    /**
     * Calcula o NH conforme GURPS 4Ed pag. 170-171
     * Pontos | F    | M    | D    | MD
     *   1    | Atr  | Atr-1| Atr-2| Atr-3
     *   2    | Atr+1| Atr  | Atr-1| Atr-2
     *   4    | Atr+2| Atr+1| Atr  | Atr-1
     *   8    | Atr+3| Atr+2| Atr+1| Atr
     *  +4    |  +1  |  +1  |  +1  |  +1
     */
    fun calcularNivel(personagem: Personagem): Int {
        val valorAtributo = personagem.getAtributo(atributoBase.sigla)
        val bonus = CharacterRules.calcularBonusPorDificuldade(dificuldade, pontosGastos)
        return valorAtributo + bonus
    }

    fun getNivelRelativo(personagem: Personagem): String {
        val dif = calcularNivel(personagem) - personagem.getAtributo(atributoBase.sigla)
        return when { dif > 0 -> "+$dif"; dif < 0 -> "$dif"; else -> "+0" }
    }
}

// ============================================================
// MAGIAS
// ============================================================

data class MagiaDefinicao(
    val id: String = "",
    val nome: String = "",
    @SerializedName("dificuldade") val dificuldadeFixa: String? = "D",
    val pagina: Int? = 0,
    val texto: String? = "",
    // Campos novos
    val classe: String? = null,
    val escola: List<String>? = null,
    val duracao: String? = null,
    val energia: String? = null,
    val tempoOperacao: String? = null,
    val preRequisitos: String? = null
) {
    // Mantendo atributo base IQ fixo para magias
    val atributoBase: String get() = "IQ"
}

data class MagiaSelecionada(
    val definicaoId: String = "",
    var nome: String = "",
    var dificuldade: Dificuldade = Dificuldade.DIFICIL,
    var pontosGastos: Int = 1,
    val pagina: Int? = 0,
    val texto: String? = "",
    val classe: String? = null,
    val escola: List<String>? = null
) {
    /**
     * Calcula o nivel da magia seguindo a mesma logica das pericias (IQ + Aptidao Magica).
     * Magias sao geralmente IQ/D ou IQ/MD.
     */
    fun calcularNivel(personagem: Personagem, nivelAptidaoMagica: Int): Int {
        val valorAtributo = personagem.inteligencia + nivelAptidaoMagica
        val bonus = CharacterRules.calcularBonusPorDificuldade(dificuldade, pontosGastos)
        return valorAtributo + bonus
    }

    fun getNivelRelativo(personagem: Personagem, nivelAptidaoMagica: Int): String {
        val base = personagem.inteligencia + nivelAptidaoMagica
        val nivel = calcularNivel(personagem, nivelAptidaoMagica)
        val dif = nivel - base
        return when { dif > 0 -> "+$dif"; dif < 0 -> "$dif"; else -> "+0" }
    }
}

// ============================================================
// EQUIPAMENTO
// ============================================================

enum class TipoEquipamento {
    GERAL,
    ARMA,
    ESCUDO,
    ARMADURA
}

data class Equipamento(
    var nome: String = "",
    var peso: Float = 0f,
    var custo: Float = 0f,
    var quantidade: Int = 1,
    var notas: String = "",
    var tipo: TipoEquipamento = TipoEquipamento.GERAL,
    var bonusDefesa: Int = 0, // Para escudos (DB - Defense Bonus)
    var armaCatalogoId: String? = null,
    var armaTipoCombate: String? = null,
    var armaDanoRaw: String? = null,
    var armaStMinimo: Int? = null,
    var armaduraLocal: String? = null,
    var armaduraRd: String? = null
){
    fun danoCalculadoComSt(personagem: Personagem): String? {
        val raw = armaDanoRaw?.trim().orEmpty()
        if (raw.isBlank()) return null
        return CharacterRules.resolverDanoPorSt(raw, personagem.forca)
    }

    fun rdArmaduraExibicao(): String? {
        val estruturado = armaduraRd?.trim().orEmpty()
        if (estruturado.isNotBlank()) return estruturado
        val legado = Regex("RD:\\s*([^;]+)", RegexOption.IGNORE_CASE)
            .find(notas)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
            .orEmpty()
        return legado.ifBlank { null }
    }
}

// ============================================================
// COMBATE - DEFESAS ATIVAS
// ============================================================

data class DefesasAtivas(
    // Esquiva
    var bonusManualEsquiva: Int = 0,

    // Apara
    var periciaAparaId: String? = null,
    var bonusManualApara: Int = 0,

    // Bloqueio
    var periciaBloqueioId: String? = null,
    var escudoSelecionadoNome: String? = null,
    var bonusManualBloqueio: Int = 0
) {
    /**
     * Calcula Esquiva = Esquiva Básica - Penalidade de Carga + Bonus Manual
     * GURPS 4Ed: Esquiva básica = floor(Velocidade Básica + 3)
     */
    fun calcularEsquiva(personagem: Personagem): Int {
        return CombatRules.calcularEsquiva(
            esquivaBase = personagem.esquiva,
            nivelCarga = personagem.nivelCarga,
            bonusManual = bonusManualEsquiva
        )
    }

    fun getEsquivaBase(personagem: Personagem): Int {
        return CombatRules.calcularEsquivaBase(
            esquivaBase = personagem.esquiva,
            nivelCarga = personagem.nivelCarga
        )
    }

    /**
     * Calcula Apara = (NH / 2) + 3 + Bonus Manual
     * GURPS 4Ed pag. 376: Apara = 3 + (metade do NH da arma ou pericia de combate)
     */
    fun calcularApara(personagem: Personagem): Int? {
        val pericia = periciaAparaId?.let { id ->
            personagem.pericias.find { it.definicaoId == id }
        } ?: return null

        val nh = pericia.calcularNivel(personagem)
        return CombatRules.calcularApara(nh, bonusManualApara)
    }

    fun getAparaBase(personagem: Personagem): Int? {
        val pericia = periciaAparaId?.let { id ->
            personagem.pericias.find { it.definicaoId == id }
        } ?: return null

        val nh = pericia.calcularNivel(personagem)
        return CombatRules.calcularAparaBase(nh)
    }

    fun getPericiaApara(personagem: Personagem): PericiaSelecionada? {
        return periciaAparaId?.let { id ->
            personagem.pericias.find { it.definicaoId == id }
        }
    }

    /**
     * Calcula Bloqueio = (NH / 2) + 3 + Bonus Escudo + Bonus Manual
     * GURPS 4Ed pag. 375: Bloqueio = 3 + (metade do NH de Escudo)
     * O DB do escudo e somado ao bloqueio
     */
    fun calcularBloqueio(personagem: Personagem): Int? {
        val pericia = periciaBloqueioId?.let { id ->
            personagem.pericias.find { it.definicaoId == id }
        } ?: return null

        val nh = pericia.calcularNivel(personagem)
        val bonusEscudo = getBonusEscudo(personagem)
        return CombatRules.calcularBloqueio(nh, bonusEscudo, bonusManualBloqueio)
    }

    fun getBloqueioBase(personagem: Personagem): Int? {
        val pericia = periciaBloqueioId?.let { id ->
            personagem.pericias.find { it.definicaoId == id }
        } ?: return null

        val nh = pericia.calcularNivel(personagem)
        return CombatRules.calcularBloqueioBase(nh)
    }

    fun getBonusEscudo(personagem: Personagem): Int {
        val escudos = personagem.equipamentos.filter { it.tipo == TipoEquipamento.ESCUDO }
        if (escudos.isEmpty()) return 0

        val nomeSelecionado = escudoSelecionadoNome
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        val escudoSelecionado = nomeSelecionado?.let { nome ->
            escudos.find { it.nome.trim().equals(nome, ignoreCase = true) }
        }
        val escudo = escudoSelecionado ?: escudos.maxByOrNull { it.bonusDefesa }
        return escudo?.bonusDefesa ?: 0
    }

    fun getPericiaBloqueio(personagem: Personagem): PericiaSelecionada? {
        return periciaBloqueioId?.let { id ->
            personagem.pericias.find { it.definicaoId == id }
        }
    }
}

// Lista de pericias que sao consideradas "pericias de combate" para Apara/Bloqueio
val PERICIAS_COMBATE = setOf(
    // IDs atuais do dataset
    "adaga_de_esgrima",
    "armas_de_haste",
    "bastao",
    "boxe",
    "briga",
    "capa",
    "carate",
    "chicote",
    "escudo",
    "espada_curta",
    "espada_de_duas_maos",
    "espada_de_energia",
    "espada_de_lamina_larga",
    "faca",
    "jittesai",
    "judo",
    "kusari",
    "lanca",
    "lanca_de_justa",
    "luta_grecoromana",
    "macamachado",
    "macamachado_de_duas_maos",
    "mangual",
    "mangual_de_duas_maos",
    // Aliases legados para fichas antigas
    "adaga",
    "alabarda",
    "armas_de_corrente",
    "armas_de_duas_maos",
    "cajado",
    "espada_larga",
    "kama",
    "karate",
    "kusarigama",
    "maca",
    "machado_de_duas_maos",
    "machado_ou_machadinha",
    "nunchaku",
    "rapieira",
    "sabre",
    "sai",
    "tonfa",
    "wrestling"
)

