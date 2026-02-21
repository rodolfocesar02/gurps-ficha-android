package com.gurps.ficha.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

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
    var vantagens: MutableList<VantagemSelecionada> = mutableListOf(),
    var desvantagens: MutableList<DesvantagemSelecionada> = mutableListOf(),
    var peculiaridades: MutableList<String> = mutableListOf(),
    var pericias: MutableList<PericiaSelecionada> = mutableListOf(),
    var magias: MutableList<MagiaSelecionada> = mutableListOf(),
    var equipamentos: MutableList<Equipamento> = mutableListOf(),

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
    val esquiva: Int get() = (velocidadeBasica + 3).toInt()
    val baseCarga: Float get() = (forca * forca) / 10f
    val danoGdP: String get() = calcularDanoGdP(forca)
    val danoGeB: String get() = calcularDanoGeB(forca)

    // === CALCULO DE PONTOS ===
    val pontosAtributos: Int get() {
        return (forca - 10) * 10 + (destreza - 10) * 20 +
               (inteligencia - 10) * 20 + (vitalidade - 10) * 10
    }

    val pontosSecundarios: Int get() {
        return modPontosVida * 2 + modVontade * 5 + modPercepcao * 5 +
               modPontosFadiga * 3 + (modVelocidadeBasica / 0.25f).toInt() * 5 +
               modDeslocamentoBasico * 5
    }

    val pontosVantagens: Int get() = vantagens.sumOf { it.custoFinal }
    val pontosDesvantagens: Int get() = desvantagens.sumOf { it.custoFinal }
    val pontosPeculiaridades: Int get() = peculiaridades.size * -1
    val pontosPericias: Int get() = pericias.sumOf { it.pontosGastos }
    val pontosMagias: Int get() = magias.sumOf { it.pontosGastos }

    val pontosGastos: Int get() =
        pontosAtributos + pontosSecundarios + pontosVantagens +
        pontosDesvantagens + pontosPeculiaridades + pontosPericias + pontosMagias

    val pontosRestantes: Int get() = pontosIniciais - pontosGastos
    val desvantagensExcedemLimite: Boolean get() = pontosDesvantagens < limiteDesvantagens

    // === TABELA DE DANO GURPS 4Ed ===
    private fun calcularDanoGdP(st: Int): String {
        return when {
            st <= 0 -> "0"
            st in 1..2 -> "1d-6"
            st in 3..4 -> "1d-5"
            st in 5..6 -> "1d-4"
            st in 7..8 -> "1d-3"
            st in 9..10 -> "1d-2"
            st in 11..12 -> "1d-1"
            st in 13..14 -> "1d"
            st in 15..16 -> "1d+1"
            st in 17..18 -> "1d+2"
            st in 19..20 -> "2d-1"
            else -> {
                val extra = (st - 20) / 2
                "${2 + extra}d${if ((st - 20) % 2 == 0) "" else "+1"}"
            }
        }
    }

    private fun calcularDanoGeB(st: Int): String {
        return when {
            st <= 0 -> "0"
            st in 1..2 -> "1d-5"
            st in 3..4 -> "1d-4"
            st in 5..6 -> "1d-3"
            st in 7..8 -> "1d-2"
            st == 9 -> "1d-1"
            st == 10 -> "1d"
            st == 11 -> "1d+1"
            st == 12 -> "1d+2"
            st == 13 -> "2d-1"
            st == 14 -> "2d"
            st == 15 -> "2d+1"
            st == 16 -> "2d+2"
            st == 17 -> "3d-1"
            st == 18 -> "3d"
            st == 19 -> "3d+1"
            st == 20 -> "3d+2"
            else -> {
                val extra = (st - 20) / 2
                "${3 + extra}d${if ((st - 20) % 2 == 0) "+2" else "+1"}"
            }
        }
    }

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
    val pagina: Int = 0
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
    var custoBase: Int = 0,
    var nivel: Int = 1,
    var custoEscolhido: Int = 0,
    var descricao: String = "",
    val tipoCusto: TipoCusto = TipoCusto.FIXO,
    val pagina: Int = 0
) {
    val custoFinal: Int get() {
        val valor = when (tipoCusto) {
            TipoCusto.FIXO -> custoBase
            TipoCusto.ESCOLHA -> custoEscolhido
            TipoCusto.VARIAVEL -> custoEscolhido
            TipoCusto.POR_NIVEL -> custoBase * nivel
        }
        return if (valor < 0) 0 else valor // Vantagens DEVEM ser positivas
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
    val pagina: Int = 0
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
        val custoSemAutocontrole = when (tipoCusto) {
            TipoCusto.FIXO -> custoBase
            TipoCusto.ESCOLHA -> custoEscolhido
            TipoCusto.VARIAVEL -> custoEscolhido
            TipoCusto.POR_NIVEL -> custoBase * nivel
        }
        // Modificador de autocontrole GURPS 4Ed pag. 120
        val custoComAutocontrole = autocontrole?.let { ac ->
            val mult = when (ac) {
                6 -> 2.0; 9 -> 1.5; 12 -> 1.0; 15 -> 0.5; else -> 1.0
            }
            (custoSemAutocontrole * mult).toInt()
        } ?: custoSemAutocontrole
        return if (custoComAutocontrole > 0) -custoComAutocontrole else custoComAutocontrole
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
    val atributosPossiveis: List<String>? = null,
    val atributoEscolhaObrigatoria: Boolean = false,
    val dificuldadeFixa: String? = "M",
    val dificuldadeVariavel: Boolean = false,
    val exigeEspecializacao: Boolean = false,
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
        val pts = pontosGastos.coerceAtLeast(1)

        val bonus = when (dificuldade) {
            Dificuldade.FACIL -> when {
                pts == 1 -> 0; pts == 2 -> 1; pts == 4 -> 2
                else -> 2 + (pts - 4) / 4
            }
            Dificuldade.MEDIA -> when {
                pts == 1 -> -1; pts == 2 -> 0; pts == 4 -> 1
                else -> 1 + (pts - 4) / 4
            }
            Dificuldade.DIFICIL -> when {
                pts == 1 -> -2; pts == 2 -> -1; pts == 4 -> 0
                else -> (pts - 4) / 4
            }
            Dificuldade.MUITO_DIFICIL -> when {
                pts == 1 -> -3; pts == 2 -> -2; pts == 4 -> -1
                else -> -1 + (pts - 4) / 4
            }
        }
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
    val atributoBase: String = "IQ",
    val atributosPossiveis: List<String>? = null,
    val atributoEscolhaObrigatoria: Boolean = false,
    val dificuldadeFixa: String? = "D",
    val dificuldadeVariavel: Boolean = false,
    val exigeEspecializacao: Boolean = false,
    val preDefinicoes: List<PreDefinicao> = emptyList(),
    val pagina: Int = 0,
    val texto: String = ""
)

data class MagiaSelecionada(
    val definicaoId: String = "",
    var nome: String = "",
    var dificuldade: Dificuldade = Dificuldade.DIFICIL,
    var pontosGastos: Int = 1,
    val pagina: Int = 0,
    val texto: String = ""
) {
    /**
     * Calcula o nivel da magia seguindo a mesma logica das pericias (IQ + Aptidao Magica).
     * Magias sao geralmente IQ/D ou IQ/MD.
     */
    fun calcularNivel(personagem: Personagem, nivelAptidaoMagica: Int): Int {
        val valorAtributo = personagem.inteligencia + nivelAptidaoMagica
        val pts = pontosGastos.coerceAtLeast(1)

        val bonus = when (dificuldade) {
            Dificuldade.DIFICIL -> when {
                pts == 1 -> -2; pts == 2 -> -1; pts == 4 -> 0
                else -> (pts - 4) / 4
            }
            Dificuldade.MUITO_DIFICIL -> when {
                pts == 1 -> -3; pts == 2 -> -2; pts == 4 -> -1
                else -> -1 + (pts - 4) / 4
            }
            else -> when { // Fallback para F/M se necessario
                dificuldade == Dificuldade.FACIL -> if (pts == 1) 0 else if (pts == 2) 1 else if (pts == 4) 2 else 2 + (pts-4)/4
                dificuldade == Dificuldade.MEDIA -> if (pts == 1) -1 else if (pts == 2) 0 else if (pts == 4) 1 else 1 + (pts-4)/4
                else -> -2 // Padrao Dificil
            }
        }
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
    var bonusDefesa: Int = 0 // Para escudos (DB - Defense Bonus)
)

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
     * Calcula Esquiva = Movimentacao + 3 + Bonus Manual
     * GURPS 4Ed: Esquiva basica = Velocidade Basica + 3 (arredondado para baixo)
     */
    fun calcularEsquiva(personagem: Personagem): Int {
        val movimentacao = personagem.deslocamentoBasico
        val esquivaBase = movimentacao + 3
        return esquivaBase + bonusManualEsquiva
    }

    fun getEsquivaBase(personagem: Personagem): Int {
        return personagem.deslocamentoBasico + 3
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
        val aparaBase = (nh / 2) + 3 // Arredondamento padrao (trunca)
        return aparaBase + bonusManualApara
    }

    fun getAparaBase(personagem: Personagem): Int? {
        val pericia = periciaAparaId?.let { id ->
            personagem.pericias.find { it.definicaoId == id }
        } ?: return null

        val nh = pericia.calcularNivel(personagem)
        return (nh / 2) + 3
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
        val bloqueioBase = (nh / 2) + 3
        val bonusEscudo = getBonusEscudo(personagem)
        return bloqueioBase + bonusEscudo + bonusManualBloqueio
    }

    fun getBloqueioBase(personagem: Personagem): Int? {
        val pericia = periciaBloqueioId?.let { id ->
            personagem.pericias.find { it.definicaoId == id }
        } ?: return null

        val nh = pericia.calcularNivel(personagem)
        return (nh / 2) + 3
    }

    fun getBonusEscudo(personagem: Personagem): Int {
        val escudo = escudoSelecionadoNome?.let { nome ->
            personagem.equipamentos.find { it.nome == nome && it.tipo == TipoEquipamento.ESCUDO }
        }
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
    "espada_curta", "espada_larga", "espada_bastarda", "espada_de_duas_maos",
    "faca", "adaga", "machado_ou_machadinha", "machado_de_duas_maos",
    "maca", "mangual", "lanca", "bastao", "cajado", "alabarda",
    "escudo", "briga", "karate", "boxe", "judo", "wrestling",
    "armas_de_haste", "armas_de_duas_maos", "rapieira", "sabre",
    "florete", "armas_de_corrente", "chicote", "nunchaku",
    "tonfa", "sai", "kama", "kusarigama"
)
