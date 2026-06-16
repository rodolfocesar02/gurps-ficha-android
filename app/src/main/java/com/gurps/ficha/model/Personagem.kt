package com.gurps.ficha.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.gurps.ficha.domain.rules.CharacterRules
import com.gurps.ficha.domain.rules.CombatRules
import com.gurps.ficha.domain.rules.traits.TraitRuleRegistry
import java.text.Normalizer

import androidx.compose.runtime.Stable

/**
 * Modelo de dados do personagem GURPS 4Ed.
 */
@Stable
data class Personagem(
    // Informacoes Basicas
    var nome: String = "",
    var jogador: String = "",
    var campanha: String = "",
    var pontosIniciais: Int = 150,
    var xpGanhos: Int = 0, // Pontos ganhos durante a campanha
    var limiteDesvantagens: Int = -75, // Limite padrao GURPS 4Ed

    // Atributos Primarios (valor 10 = media humana, gratuito)
    var forca: Int = 10,      // ST - +/-10 pontos/nivel
    var destreza: Int = 10,   // DX - +/-20 pontos/nivel
    var inteligencia: Int = 10, // IQ - +/-20 pontos/nivel
    var vitalidade: Int = 10,  // HT - +/-10 pontos/nivel
    var forcaBase: Int = 10,
    var destrezaBase: Int = 10,
    var inteligenciaBase: Int = 10,
    var vitalidadeBase: Int = 10,

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
    var qualidades: List<String> = emptyList(),
    var peculiaridades: List<String> = emptyList(),
    var pericias: List<PericiaSelecionada> = emptyList(),
    var tecnicas: List<TecnicaSelecionada> = emptyList(),
    var magias: List<MagiaSelecionada> = emptyList(),
    var equipamentos: List<Equipamento> = emptyList(),

    // Descricao
    var aparencia: String = "",
    var historico: String = "",
    var notas: String = "",

    // Imagem do personagem. Caminhos file:// em filesDir/portraits/.
    //  - imagemPersonagemUri: versao RECORTADA (faixa do cabecalho).
    //  - imagemPersonagemOriginalUri: imagem INTEIRA (mostrada em tela cheia).
    var imagemPersonagemUri: String = "",
    var imagemPersonagemOriginalUri: String = "",
    // Imagem ORIGINAL embutida (data:image/...;base64,...) — preenchida APENAS
    // na EXPORTACAO, para a foto viajar junto com a ficha (.json). No IMPORT,
    // o app salva como arquivo + re-recorta o rosto e LIMPA este campo (fica
    // vazio na ficha em uso, para nao inchar a persistencia local).
    var imagemPersonagemBase64: String = "",

    // Combate
    var defesasAtivas: DefesasAtivas = DefesasAtivas(),

    // Rolagem (estado de sessao salvo por ficha)
    var pontosVidaRolagemAtual: Int? = null,
    var pontosFadigaRolagemAtual: Int? = null,
    var modeloRacial: ModeloRacial = ModeloRacial()
) {
    /**
     * Lista consolidada de todas as perícias (pessoais + raciais).
     * Permite que perícias da raça apareçam automaticamente para rolagens.
     */
    val periciasTotais: List<PericiaSelecionada> get() {
        // Só CONCEDIDA entra como perícia própria (p.454). BONUS (p.453)
        // NÃO concede a perícia — é só +N no NH quando o personagem usa
        // a perícia (aplicado em calcularNivel via bonusRacial).
        val raciais = modeloRacial.pericias
            .filter { it.tipo == TipoPericiaRacial.CONCEDIDA }
            .map { pr ->
            PericiaSelecionada(
                definicaoId = "racial_${pr.nome.lowercase()}",
                nome = pr.nome,
                // BLINDAGEM: baseAtributo inválido (ex: ficha salva com "M"
                // por bug de construtor com args trocados) NÃO pode derrubar
                // o app. valueOf lança IllegalArgumentException -> crash ao
                // abrir Perícias/Rolagem. Fallback seguro = DX.
                atributoBase = runCatching {
                    AtributoBase.valueOf(pr.baseAtributo.uppercase())
                }.getOrDefault(AtributoBase.DX),
                dificuldade = when(pr.diff.uppercase()) {
                    "F" -> Dificuldade.FACIL
                    "M" -> Dificuldade.MEDIA
                    "D" -> Dificuldade.DIFICIL
                    "MD" -> Dificuldade.MUITO_DIFICIL
                    else -> Dificuldade.MEDIA
                },
                pontosGastos = 1, // Valor simbólico, o nível já vem do racial
                especializacao = "(Racial)"
            )
        }
        return pericias + raciais
    }

    // Atributos combinados (Personagem + Modelo Racial)
    val st: Int get() = forca + modeloRacial.modForca
    val dx: Int get() = destreza + modeloRacial.modDestreza
    val iq: Int get() = inteligencia + modeloRacial.modInteligencia
    val ht: Int get() = vitalidade + modeloRacial.modVitalidade

    // === CALCULOS AUTOMATICOS ===
    val pontosVida: Int get() = st + modPontosVida + modeloRacial.modPontosVida
    val vontade: Int get() = iq + modVontade + modeloRacial.modVontade
    val percepcao: Int get() = iq + modPercepcao + modeloRacial.modPercepcao
    val pontosFadiga: Int get() = ht + modPontosFadiga + modeloRacial.modPontosFadiga
    val velocidadeBasica: Float get() = (ht + dx) / 4f + modVelocidadeBasica + modeloRacial.modVelocidadeBasica
    val deslocamentoBasico: Int get() = velocidadeBasica.toInt() + modDeslocamentoBasico + modeloRacial.modDeslocamentoBasico
    val esquiva: Int get() = (velocidadeBasica + 3).toInt() // Esquiva Básica (sem carga)

    // Deslocamento Aquático: floor(desloc/5) + bônus da vantagem deslocamento_aquatico (racial + pessoal)
    val bonusDeslocamentoAquatico: Int get() {
        val racial = modeloRacial.vantagens.filter { it.definicaoId == "deslocamento_aquatico" }.sumOf { it.nivel }
        val pessoal = vantagens.filter { it.definicaoId == "deslocamento_aquatico" }.sumOf { it.nivel }
        return racial + pessoal
    }
    val deslocamentoAquatico: Int get() = deslocamentoBasico / 5 + bonusDeslocamentoAquatico
    val baseCarga: Float get() = (st * st) / 10f
    val modificadorTamanho: Int get() = modeloRacial.modificadorTamanho
    val danoGdP: String get() = CharacterRules.calcularDanoGdP(st)
    val danoGeB: String get() = CharacterRules.calcularDanoGeB(st)

    val pesoTotalEquipamentos: Float get() = equipamentos.sumOf {
        (it.peso * it.quantidade).toDouble()
    }.toFloat()

    val custoTotalEquipamentos: Float get() = equipamentos.sumOf {
        (it.custo * it.quantidade).toDouble()
    }.toFloat()

    val nivelCarga: Int get() = CharacterRules.calcularNivelCarga(baseCarga, pesoTotalEquipamentos)

    val deslocamentoAtual: Int get() = CharacterRules.calcularDeslocamentoAtual(
        deslocamentoBasico = deslocamentoBasico,
        nivelCarga = nivelCarga
    )

    // === CALCULO DE PONTOS ===
    val pontosAtributos: Int get() = CharacterRules.calcularPontosAtributos(
        forca = forca,
        destreza = destreza,
        inteligencia = inteligencia,
        vitalidade = vitalidade,
        forcaBase = forcaBase,
        destrezaBase = destrezaBase,
        inteligenciaBase = inteligenciaBase,
        vitalidadeBase = vitalidadeBase
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
    val pontosQualidades: Int get() = qualidades.size
    val pontosPeculiaridades: Int get() = peculiaridades.size * -1
    val pontosPericias: Int get() = pericias.sumOf { it.pontosGastos }
    val pontosTecnicas: Int get() = tecnicas.sumOf { it.pontosGastos.coerceAtLeast(0) }
    val pontosMagias: Int get() = magias.sumOf { it.pontosGastos.coerceAtLeast(1) }

    val pontosGastos: Int get() =
        pontosAtributos + pontosSecundarios + pontosVantagens +
        pontosDesvantagens + pontosQualidades + pontosPeculiaridades + pontosPericias + pontosTecnicas + pontosMagias +
        modeloRacial.custoTotal

    val pontosTotaisDisponiveis: Int get() = pontosIniciais + xpGanhos
    val pontosRestantes: Int get() = pontosTotaisDisponiveis - pontosGastos
    val desvantagensExcedemLimite: Boolean get() = pontosDesvantagens < limiteDesvantagens

    fun getAtributo(sigla: String): Int {
        return when (sigla.uppercase()) {
            "ST" -> st
            "DX" -> dx
            "IQ" -> iq
            "HT" -> ht
            "PER" -> percepcao
            "VON" -> vontade
            else -> 10
        }
    }

    /**
     * Retorna o nível total de uma vantagem, somando o que está na ficha com o que vem da raça.
     */
    fun getVantagemNivel(id: String): Int {
        val personLevel = vantagens.filter { it.definicaoId.equals(id, ignoreCase = true) }.sumOf { it.nivel }
        val racialLevel = modeloRacial.vantagens.filter { it.definicaoId.equals(id, ignoreCase = true) }.sumOf { it.nivel }
        return personLevel + racialLevel
    }

    /**
     * Verifica se o personagem possui uma vantagem (na ficha ou na raça).
     */
    fun hasVantagem(id: String): Boolean {
        return vantagens.any { it.definicaoId.equals(id, ignoreCase = true) } ||
               modeloRacial.vantagens.any { it.definicaoId.equals(id, ignoreCase = true) }
    }

    /**
     * Retorna o nível total de uma desvantagem, somando o que está na ficha com o que vem da raça.
     */
    fun getDesvantagemNivel(id: String): Int {
        val personLevel = desvantagens.filter { it.definicaoId.equals(id, ignoreCase = true) }.sumOf { it.nivel }
        val racialLevel = modeloRacial.desvantagens.filter { it.definicaoId.equals(id, ignoreCase = true) }.sumOf { it.nivel }
        return personLevel + racialLevel
    }

    /**
     * Verifica se o personagem possui uma desvantagem (na ficha ou na raça).
     */
    fun hasDesvantagem(id: String): Boolean {
        return desvantagens.any { it.definicaoId.equals(id, ignoreCase = true) } ||
               modeloRacial.desvantagens.any { it.definicaoId.equals(id, ignoreCase = true) }
    }

    fun toJson(): String = Gson().toJson(this)

    companion object {
        fun fromJson(json: String): Personagem {
            val gson = Gson()
            val jsonObject = com.google.gson.JsonParser.parseString(json).asJsonObject
            if (!jsonObject.has("magias")) {
                jsonObject.add("magias", com.google.gson.JsonArray())
            }
            if (!jsonObject.has("qualidades")) {
                jsonObject.add("qualidades", com.google.gson.JsonArray())
            }
            if (!jsonObject.has("tecnicas")) {
                jsonObject.add("tecnicas", com.google.gson.JsonArray())
            }
            if (!jsonObject.has("forcaBase")) {
                jsonObject.addProperty("forcaBase", 10)
            }
            if (!jsonObject.has("destrezaBase")) {
                jsonObject.addProperty("destrezaBase", 10)
            }
            if (!jsonObject.has("inteligenciaBase")) {
                jsonObject.addProperty("inteligenciaBase", 10)
            }
            if (!jsonObject.has("vitalidadeBase")) {
                jsonObject.addProperty("vitalidadeBase", 10)
            }
            if (!jsonObject.has("modeloRacial")) {
                jsonObject.add("modeloRacial", com.google.gson.JsonObject())
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
// MODIFICADORES
// ============================================================

@Stable
data class ModificadorSelecao(
    val id: String = "",
    val nome: String = "",
    val valor: Int = 0,
    val porNivel: Boolean = false,
    val niveis: Int = 1,
    val descricao: String? = null,
    val pagina: Int? = null,
    // Bônus FIXO (base) somado UMA vez, independente de níveis.
    // Pra modificadores com "+X% base + Y%/nível" (ex: Cone = +50%
    // base + 10%/m). Default 0 = comportamento antigo (só valor*níveis).
    // No FIM da lista pra não quebrar call sites posicionais antigos.
    val bonusBase: Int = 0
)

// ============================================================
// VANTAGENS
// ============================================================

@Stable
data class ModificadorDefinicao(
    val id: String = "",
    val nome: String = "",
    val tipo: String = "", // "ampliação" ou "limitação"
    val valor: String = "0",
    @SerializedName(value = "porNivel", alternate = ["por_nivel"])
    val porNivel: Boolean = false,
    // Bônus FIXO (base) — ex: Cone = +50% base + 10%/m.
    @SerializedName(value = "bonusBase", alternate = ["bonus_base"])
    val bonusBase: Int = 0,
    val pagina: Int? = null,
    val tags: List<String> = emptyList(),
    val descricao: String? = null
)

@Stable
data class VantagemDefinicao(
    val id: String = "",
    val nome: String = "",
    val custo: String = "0",
    val tipoCusto: TipoCusto = TipoCusto.FIXO,
    val pagina: Int = 0,
    val tags: List<String> = emptyList(),
    val descricao: String? = "",
    val specialRule: String? = null,
    @SerializedName(value = "modificadoresEspecificos", alternate = ["modificadores_especificos"])
    val modificadoresEspecificos: List<ModificadorDefinicao> = emptyList()
) {
    fun getCustoBase(): Int {
        val cleaned = custo.replace(Regex("[^0-9-]"), " ").trim()
        val match = Regex("(-?\\d+)").find(cleaned)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    fun getCustoPorNivel(): Int {
        val match = Regex("(\\d+)/(n|nivel|nível)").find(custo.lowercase())
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

@Stable
data class VantagemSelecionada(
    val definicaoId: String = "",
    val nome: String = "",
    var custoBase: Int = 0, // Custo unitario (por nivel) ou custo fixo
    var nivel: Int = 1,
    var custoEscolhido: Int = 0, // Custo total escolhido (para VARIAVEL/ESCOLHA)
    var descricao: String = "",
    val tipoCusto: TipoCusto = TipoCusto.FIXO,
    val pagina: Int = 0,
    val specialRule: String? = null,
    var modificadores: List<ModificadorSelecao> = emptyList(),
    var metadados: Map<String, String>? = null // Para regras especiais como Ataque Inato
) {
    val custoFinal: Int get() {
        val rule = specialRule ?: CharacterRules.DATA_REPOSITORY_INSTANCE?.getVantagemPorId(definicaoId)?.specialRule
        return CharacterRules.calcularCustoVantagem(
            definicaoId = definicaoId,
            tipoCusto = tipoCusto,
            custoBase = custoBase,
            custoEscolhido = custoEscolhido,
            nivel = nivel,
            modificadores = modificadores,
            specialRule = rule,
            metadados = metadados
        )
    }
}

// ============================================================
// DESVANTAGENS
// ============================================================

@Stable
data class DesvantagemDefinicao(
    val id: String = "",
    val nome: String = "",
    val custo: String = "0",
    val tipoCusto: TipoCusto = TipoCusto.FIXO,
    val pagina: Int = 0,
    val tags: List<String> = emptyList(),
    val descricao: String? = "",
    val specialRule: String? = null,
    @SerializedName(value = "modificadoresEspecificos", alternate = ["modificadores_especificos"])
    val modificadoresEspecificos: List<ModificadorDefinicao> = emptyList()
) {
    fun usaAutocontroleMental(): Boolean {
        val ehMental = tags.any { it.equals("mental", ignoreCase = true) }
        val temMarcadorAutocontrole = custo.contains("*")
        return ehMental && temMarcadorAutocontrole
    }

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

@Stable
data class DesvantagemSelecionada(
    val definicaoId: String = "",
    val nome: String = "",
    var custoBase: Int = 0,
    var nivel: Int = 1,
    var custoEscolhido: Int = 0,
    var descricao: String = "",
    var autocontrole: Int? = null,
    val tipoCusto: TipoCusto = TipoCusto.FIXO,
    val pagina: Int = 0,
    val specialRule: String? = null,
    var modificadores: List<ModificadorSelecao> = emptyList(),
    var metadados: Map<String, String>? = null
) {
    val custoFinal: Int get() {
        val rule = specialRule ?: CharacterRules.DATA_REPOSITORY_INSTANCE?.getDesvantagemPorId(definicaoId)?.specialRule
        return CharacterRules.calcularCustoDesvantagem(
            tipoCusto = tipoCusto,
            custoBase = custoBase,
            custoEscolhido = custoEscolhido,
            nivel = nivel,
            autocontrole = autocontrole,
            modificadores = modificadores,
            specialRule = rule,
            metadados = metadados
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

@Stable
data class PericiaDefinicao(
    val id: String = "",
    val nome: String = "",
    val atributoBase: String = "IQ",
    @SerializedName(value = "atributosPossiveis", alternate = ["atributosPossíveis"])
    val atributosPossiveis: List<String>? = null,
    val atributoEscolhaObrigatoria: Boolean = false,
    val dificuldadeFixa: String? = "M",
    val dificuldadeVariavel: Boolean = false,
    val exigeEspecializacao: Boolean = false,
    @SerializedName(value = "preDefinicoes", alternate = ["preDefinições"])
    val preDefinicoes: List<PreDefinicao> = emptyList()
)

data class PreDefinicao(val atributo: String = "", val modificador: Int = 0)

@Stable
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

        // Perícia RACIAL (Innate Skill): o nível vem DIRETO da raça como
        // atributo + nivelRelativo. Ela não tem "pontos gastos" no sentido
        // normal — periciasTotais a converte com pontosGastos=1 só por
        // exigência do data class. Aplicar o bônus-por-pontos injetava um
        // -1 parasita (Média, 1 pt) -> Comércio IQ10 vinha NH9 em vez de
        // NH10 (NR 0 da planilha do livro).
        val ehRacial = definicaoId.startsWith("racial_")
        val bonus = if (ehRacial) 0
                    else CharacterRules.calcularBonusPorDificuldade(dificuldade, pontosGastos)

        // Bonus racial (Innate Skill) — o nivelRelativo definido na raça
        val bonusRacial = personagem.modeloRacial.pericias.find {
            it.nome.equals(nome, ignoreCase = true)
        }?.nivelRelativo ?: 0

        // Bônus de vantagens automatizadas
        val bonusVantagens = com.gurps.ficha.domain.rules.traits.TraitRuleRegistry.getSkillBonus(personagem, nome)

        return valorAtributo + bonus + bonusRacial + bonusVantagens
    }

    fun getNivelRelativo(personagem: Personagem): String {
        val dif = calcularNivel(personagem) - personagem.getAtributo(atributoBase.sigla)
        return when { dif > 0 -> "+$dif"; dif < 0 -> "$dif"; else -> "+0" }
    }
}

@Stable
data class TecnicaSelecionada(
    val definicaoId: String = "",
    var nome: String = "",
    var pontosGastos: Int = 0,
    var nivelRelativoPredefinido: Int = 0,
    var periciaBaseDefinicaoId: String = "",
    var periciaBaseNome: String = "",
    var periciaBaseEspecializacao: String = "",
    var preDefinidoModificador: Int = 0,
    var limiteMaximoRelativo: Int? = null,
    var dificuldadeRaw: String = "",
    var preDefinidoRaw: String = "",
    var preRequisitoRaw: String = "",
    var sourceBook: String = ""
) {
    fun encontrarPericiaBase(personagem: Personagem): PericiaSelecionada? {
        if (periciaBaseDefinicaoId.isBlank()) return null
        return personagem.pericias.firstOrNull { pericia ->
            pericia.definicaoId == periciaBaseDefinicaoId &&
                pericia.especializacao.equals(periciaBaseEspecializacao, ignoreCase = true)
        }
    }

    fun calcularNivel(personagem: Personagem): Int? {
        val periciaBase = encontrarPericiaBase(personagem) ?: return null
        val nhPericiaBase = periciaBase.calcularNivel(personagem)
        val preDefinidoNormalizado = normalizarPredefinido(preDefinidoRaw)
        val basePreDefinido = when {
            preDefinidoNormalizado.contains("aparar") -> CombatRules.calcularAparaBase(nhPericiaBase)
            preDefinidoNormalizado.contains("bloquear") -> CombatRules.calcularBloqueioBase(nhPericiaBase)
            preDefinidoNormalizado.contains("esquiva") -> personagem.esquiva
            else -> nhPericiaBase
        }
        return basePreDefinido + preDefinidoModificador + nivelRelativoPredefinido
    }

    private fun normalizarPredefinido(valor: String): String {
        val semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return semAcento
            .lowercase()
            .replace("-", " ")
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}

// ============================================================
// MAGIAS
// ============================================================

@Stable
data class MagiaDefinicao(
    val id: String = "",
    val nome: String = "",
    @SerializedName("dificuldade") val dificuldadeFixa: String? = "D",
    val pagina: Int? = 0,
    val texto: String? = "",
    val descricao: String? = "",
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

@Stable
data class MagiaSelecionada(
    val definicaoId: String = "",
    var nome: String = "",
    var dificuldade: Dificuldade = Dificuldade.DIFICIL,
    var pontosGastos: Int = 1,
    val pagina: Int? = 0,
    val texto: String? = "",
    val classe: String? = null,
    val escola: List<String>? = null,
    val duracao: String? = null,
    val energia: String? = null,
    val tempoOperacao: String? = null,
    val encantamentoAlvo: String? = null,
    val especializacaoMagia: String? = null
) {
    /**
     * Calcula o nivel da magia seguindo a mesma logica das pericias (IQ + Aptidao Magica).
     * Magias sao geralmente IQ/D ou IQ/MD.
     */
    fun calcularNivel(personagem: Personagem, nivelAptidaoMagica: Int): Int {
        val valorAtributo = personagem.inteligencia + nivelAptidaoMagica
        val bonus = CharacterRules.calcularBonusPorDificuldade(dificuldade, pontosGastos)
        
        // Bonus racial para magias (raro mas possivel)
        val bonusRacial = personagem.modeloRacial.pericias.find { 
            it.nome.equals(nome, ignoreCase = true) 
        }?.nivelRelativo ?: 0
        
        return valorAtributo + bonus + bonusRacial
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
    ARMADURA,
    CAPA
}

@Stable
data class Equipamento(
    var nome: String = "",
    var peso: Float = 0f,
    var custo: Float = 0f,
    var quantidade: Int = 1,
    var notas: String = "",
    var tipo: TipoEquipamento = TipoEquipamento.GERAL,
    var bonusDefesa: Int = 0, // Para escudos (DB - Defense Bonus)
    var armaCatalogoId: String? = null,
    var armaGrupo: String? = null, // Novo campo para Mestre de Armas
    var armaTipoCombate: String? = null,
    var armaDanoRaw: String? = null,
    var armaStMinimo: Int? = null,
    var armaduraLocal: String? = null,
    var armaduraRd: String? = null,
    // ── Stats de combate da arma (Lote 371) — anuláveis: ficha antiga carrega como null (= desconhecido). ──
    var armaAlcanceCorpoACorpo: String? = null, // "C" | "1" | "1,2"
    var armaDuasMaos: Boolean = false,
    var armaPrecisao: Int? = null,              // Acc (Apontar)
    var armaMeioDanoMetros: Int? = null,        // 1/2D
    var armaMaximoMetros: Int? = null,          // Máx
    var armaAlcanceMultStRaw: String? = null,   // arcos/arremesso: "×10/×15"
    var armaCadenciaTiro: Int? = null,          // CdT/RoF
    var armaTirosRaw: String? = null,           // "6(3)"
    var armaMagnitude: Int? = null,             // Bulk
    var armaRecuo: Int? = null,                 // Rcl
    var armaAparar: String? = null              // coluna Aparar: "0", "-1", "0D" (desbal.), "0E"/"F" (esgrima), "Não"
) {
    fun danoCalculadoComSt(personagem: Personagem, periciaId: String? = null): String? {
        val raw = armaDanoRaw?.trim().orEmpty()
        if (raw.isBlank()) return null
        
        // Consulta bônus de vantagens (ex: Mestre de Armas)
        val bonusPorDado = com.gurps.ficha.domain.rules.traits.TraitRuleRegistry.getDamageBonusPerDie(
            personagem,
            periciaId, 
            nome,
            armaGrupo
        )
        
        return CharacterRules.resolverDanoPorSt(raw, personagem.forca, bonusPorDado)
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

@Stable
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
        val bonusEscudo = getBonusEscudo(personagem)
        val base = CombatRules.calcularEsquiva(
            esquivaBase = (personagem.deslocamentoBasico + 3).coerceAtLeast(1),
            nivelCarga = personagem.nivelCarga,
            bonusEscudo = bonusEscudo,
            bonusManual = bonusManualEsquiva
        )
        val traitBonus = TraitRuleRegistry.getDodgeBonus(personagem)
        return base + traitBonus
    }

    fun getEsquivaBase(personagem: Personagem): Int {
        val bonusEscudo = getBonusEscudo(personagem)
        return CombatRules.calcularEsquivaBase(
            esquivaBase = (personagem.deslocamentoBasico + 3).coerceAtLeast(1),
            nivelCarga = personagem.nivelCarga
        ) + bonusEscudo
    }

    /**
     * Calcula Apara = (NH / 2) + 3 + Bonus Manual
     * GURPS 4Ed pag. 376: Apara = 3 + (metade do NH da arma ou pericia de combate)
     */
    fun calcularApara(personagem: Personagem): Int? {
        val pericia = getPericiaApara(personagem) ?: return null

        val nh = pericia.calcularNivel(personagem)
        val bonusEscudo = getBonusEscudo(personagem)
        val baseApara = CombatRules.calcularApara(nh, bonusEscudo, bonusManualApara)
        val traitBonus = TraitRuleRegistry.getParryBonus(personagem, pericia.definicaoId)
        
        return baseApara + traitBonus
    }

    fun getAparaBase(personagem: Personagem): Int? {
        val pericia = getPericiaApara(personagem) ?: return null

        val nh = pericia.calcularNivel(personagem)
        val bonusEscudo = getBonusEscudo(personagem)
        return CombatRules.calcularAparaBase(nh) + bonusEscudo
    }

    fun getPericiaApara(personagem: Personagem): PericiaSelecionada? {
        // 1. Tenta a perícia sincronizada (selecionada no Ataque)
        val selecionada = periciaAparaId?.let { id ->
            personagem.periciasTotais.find { it.definicaoId == id }
        }
        if (selecionada != null) return selecionada

        // 2. Fallback: Busca automática pela melhor perícia de combate (exceto escudo que é bloqueio)
        return personagem.periciasTotais
            .filter { it.definicaoId.lowercase() in PERICIAS_COMBATE && it.definicaoId.lowercase() != "escudo" }
            .maxByOrNull { it.calcularNivel(personagem) }
    }

    /**
     * Calcula Bloqueio = (NH / 2) + 3 + Bonus Escudo + Bonus Manual
     * GURPS 4Ed pag. 375: Bloqueio = 3 + (metade do NH de Escudo)
     * O DB do escudo e somado ao bloqueio
     */
    fun calcularBloqueio(personagem: Personagem): Int? {
        val pericia = getPericiaBloqueio(personagem) ?: return null

        val nh = pericia.calcularNivel(personagem)
        val bonusEscudo = getBonusEscudo(personagem)
        val baseBloqueio = CombatRules.calcularBloqueio(nh, bonusEscudo, bonusManualBloqueio)
        val traitBonus = TraitRuleRegistry.getBlockBonus(personagem)
        
        return baseBloqueio + traitBonus
    }

    fun getBloqueioBase(personagem: Personagem): Int? {
        val pericia = getPericiaBloqueio(personagem) ?: return null

        val nh = pericia.calcularNivel(personagem)
        return CombatRules.calcularBloqueioBase(nh)
    }

    fun getBonusEscudo(personagem: Personagem): Int {
        val escudos = personagem.equipamentos.filter { it.tipo == TipoEquipamento.ESCUDO || it.tipo == TipoEquipamento.CAPA }
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
        // 1. Tenta a perícia sincronizada
        val selecionada = periciaBloqueioId?.let { id ->
            personagem.periciasTotais.find { it.definicaoId == id }
        }
        if (selecionada != null) return selecionada

        // 2. Fallback: Melhor perícia de escudo ou capa
        return personagem.periciasTotais
            .filter { it.definicaoId.lowercase() == "escudo" || it.definicaoId.lowercase() == "capa" }
            .maxByOrNull { it.calcularNivel(personagem) }
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
    "caratê",
    "chicote",
    "escudo",
    "espada_curta",
    "espada_de_duas_maos",
    "espada_de_energia",
    "espada_de_lamina_larga",
    "faca",
    "jittesai",
    "judo",
    "judô",
    "kusari",
    "lanca",
    "lanca_de_justa",
    "luta_grecoromana",
    "luta_greco_romana",
    "macamachado",
    "macamachado_de_duas_maos",
    "mangual",
    "mangual_de_duas_maos",
    "sumo",
    "sumô",
    "luta_de_sumo",
    "garrote",
    "sopro",
    "lancador_de_lancas",
    "projetor_de_pressao",
    // Aliases legados para fichas antigas
    "adaga",
    "alabarda",
    "armas_de_corrente",
    "armas_de_duas_maos",
    "cajado",
    "espada_larga",
    "kama",
    "karate",
    "karatê",
    "kusarigama",
    "maca",
    "machado_de_duas_maos",
    "machado_ou_machadinha",
    "nunchaku",
    "rapieira",
    "sabre",
    "sai",
    "tonfa",
    "wrestling",
    // Pericias de Ataque a Distancia
    "arco",
    "arcos",
    "besta",
    "zarabatana",
    "funda",
    "armas_de_fogo_nt",
    "armas_de_feixe_nt",
    "artilharia_nt",
    "artilheiro_nt",
    "projetor_de_liquidos_nt",
    "bolas",
    "laco",
    "rede",
    "arma_de_fogo_pistola",
    "arma_de_fogo_fuzil",
    "arma_de_fogo_espingarda",
    "arma_de_fogo_submetralhadora",
    "arremesso",
    "facas_de_arremesso",
    "shuriken",
    "pericia_de_arma_de_fogo",
    "pericia_de_arco",
    "pericia_de_besta",
    "ataque_inato"
)
// RACIAL SKILLS
/**
 * GURPS Módulo Básico tem DOIS sistemas distintos de perícia racial:
 *
 * - CONCEDIDA (p.454): a raça JÁ SABE a perícia num nível. Custo pela
 *   Tabela de Perícias normal (p.170 — dificuldade importa).
 *   Ex.: Anão "Comércio (M) IQ [2]-10" (2 pts = NH no atributo).
 *
 * - BONUS (p.453): a raça tem um DOM, só um bônus no NH quando usa a
 *   perícia. NÃO concede a perícia. Tabela LINEAR própria, NÃO depende
 *   da dificuldade: +1=2, +2=4, +3=6 pts (máx +3).
 *   Ex.: Elfo "+1 em Arco [2]".
 */
enum class TipoPericiaRacial { CONCEDIDA, BONUS }

@Stable
data class PericiaRacial(
    val nome: String = "",
    val diff: String = "M", // F, M, D, MD
    val baseAtributo: String = "DX",
    val nivelRelativo: Int = 0, // Ex: DX+1 -> 1, DX-1 -> -1
    val custo: Int = 0,
    val tipo: TipoPericiaRacial = TipoPericiaRacial.CONCEDIDA
)

// ============================================================
// MODELO RACIAL
// ============================================================

/** Atributos que aceitam limitação percentual de custo (GURPS p.19). */
enum class AtributoLimitavel { ST, DX, PV }

/**
 * Limitação de custo de atributo (GURPS p.19/B262). São poucas e
 * fixas — hardcoded (não vão em modificadores.v1.json, que é catálogo
 * de modificador de VANTAGEM; poluiria toda vantagem e nem se aplica
 * a atributo). `aceitaEm` define onde cada uma pode ser usada.
 */
enum class TipoLimitacaoAtributo(
    val rotulo: String,
    val aceitaEm: Set<AtributoLimitavel>
) {
    // Tamanho: −10% × Modificador de Tamanho, máx −80%. ST e PV.
    TAMANHO("Tamanho", setOf(AtributoLimitavel.ST, AtributoLimitavel.PV)),
    // Manuseadores Precários: −40%. ST ou DX.
    MANUSEADORES_PRECARIOS("Manuseadores Precários", setOf(AtributoLimitavel.ST, AtributoLimitavel.DX))
}

@Stable
data class LimitacaoAtributo(
    val atributo: AtributoLimitavel = AtributoLimitavel.ST,
    val tipo: TipoLimitacaoAtributo = TipoLimitacaoAtributo.TAMANHO,
    val percentual: Int = 0 // negativo: -10, -40, -80...
)

/** Um ModeloRacial pode ser uma RAÇA (pacote racial da ficha) ou uma
 *  METACARACTERÍSTICA (mesmo pacote salvo como traço reutilizável). */
enum class TipoModeloRacial { RACA, METACARACTERISTICA }

/**
 * Metacaracterística embutida numa raça (GURPS p.262).
 *
 * NA FICHA aparece como UMA linha (nome + custo) — "anote a
 * metacaracterística, não seus componentes". MAS os componentes
 * EXISTEM, estruturados, em [conteudo] (um ModeloRacial interno): o
 * Mestre pode abrir, ver e MODIFICAR os elementos — "é possível
 * modificar os elementos, alterando o custo". O custo efetivo é o
 * `conteudo.custoTotal` (recalcula sozinho ao editar um componente).
 */
@Stable
data class MetacaracteristicaRef(
    val id: String = "",
    val nome: String = "",
    val descricao: String = "",
    val conteudo: ModeloRacial = ModeloRacial()
) {
    /** Custo efetivo = soma dos componentes (recalcula ao editar). */
    val custo: Int get() = conteudo.custoTotal
}

@Stable
data class ModeloRacial(
    val nome: String = "Humano",
    val modForca: Int = 0,
    val modDestreza: Int = 0,
    val modInteligencia: Int = 0,
    val modVitalidade: Int = 0,
    val modPontosVida: Int = 0,
    val modVontade: Int = 0,
    val modPercepcao: Int = 0,
    val modPontosFadiga: Int = 0,
    val modVelocidadeBasica: Float = 0f,
    val modDeslocamentoBasico: Int = 0,
    val vantagens: List<VantagemSelecionada> = emptyList(),
    val desvantagens: List<DesvantagemSelecionada> = emptyList(),
    val pericias: List<PericiaRacial> = emptyList(),
    // Espelham o padrão da ficha normal (Personagem.qualidades/
    // peculiaridades): texto livre. Custo fixo GURPS: qualidade = +1,
    // peculiaridade = -1 (igual a pontosQualidades/pontosPeculiaridades).
    // Ex.: Anão tem 2 peculiaridades = -2 pts, antes sem onde entrar.
    val qualidades: List<String> = emptyList(),
    val peculiaridades: List<String> = emptyList(),
    // Limitações percentuais SOBRE o custo de um atributo racial
    // (GURPS p.19/B262). Tamanho: ST e PV (−10%×ModTam, até −80%);
    // Manuseadores Precários: ST e DX (−40%). Lista vazia = sem
    // limitação (comportamento idêntico ao anterior). Cada item
    // afeta SÓ o atributo nomeado.
    val limitacoesAtributo: List<LimitacaoAtributo> = emptyList(),
    // MT racial (GURPS B19). 0 = humano padrão (oculto na ficha).
    // Valor positivo = alvo maior/mais fácil de acertar; negativo = menor.
    val modificadorTamanho: Int = 0,
    // Metacaracterísticas embutidas (GURPS p.262). Cada uma entra como
    // UM item de custo único — NÃO expande componentes ("anote a
    // metacaracterística, não seus componentes"). Ex.: Espírito = +261.
    val metacaracteristicas: List<MetacaracteristicaRef> = emptyList(),
    // RACA = pacote racial normal; METACARACTERISTICA = este mesmo
    // pacote, mas salvo como traço reutilizável (mesma estrutura, só
    // muda o rótulo e onde é gravado). GURPS: "funciona quase da mesma
    // maneira que uma vantagem/desvantagem".
    val tipo: TipoModeloRacial = TipoModeloRacial.RACA,
    val descricao: String = ""
) {
    /** % total de limitação aplicável a um atributo (soma, piso −80). */
    private fun pctLimitacao(attr: AtributoLimitavel): Int =
        limitacoesAtributo.filter { it.atributo == attr }
            .sumOf { it.percentual }
            .coerceAtLeast(-80)

    /** Aplica a limitação % ao custo bruto do atributo. GURPS
     *  "elimine frações" → arredonda p/ baixo. Sem limitação → bruto. */
    private fun custoComLimite(bruto: Int, attr: AtributoLimitavel): Int {
        val pct = pctLimitacao(attr)
        return if (pct == 0) bruto
            else kotlin.math.floor(bruto * (1.0 + pct / 100.0)).toInt()
    }

    val custoTotal: Int get() {
        val custoForca = custoComLimite(modForca * 10, AtributoLimitavel.ST)
        val custoDestrezaV = custoComLimite(modDestreza * 20, AtributoLimitavel.DX)
        val custoAtributos = custoForca + custoDestrezaV + modInteligencia * 20 + modVitalidade * 10
        val custoPV = custoComLimite(modPontosVida * 2, AtributoLimitavel.PV)
        val custoSecundarios = custoPV + modVontade * 5 + modPercepcao * 5 +
                               modPontosFadiga * 3 + kotlin.math.round(modVelocidadeBasica / 0.25f).toInt() * 5 +
                               modDeslocamentoBasico * 5
        val custoVantagens = vantagens.sumOf { it.custoFinal }
        val custoDesvantagens = desvantagens.sumOf { it.custoFinal }
        val custoPericias = pericias.sumOf { it.custo }
        val custoQualidades = qualidades.size            // +1 cada (GURPS)
        val custoPeculiaridades = peculiaridades.size * -1 // -1 cada (GURPS)
        val custoMeta = metacaracteristicas.sumOf { it.custo } // = soma dos componentes de cada meta
        return custoAtributos + custoSecundarios + custoVantagens +
               custoDesvantagens + custoPericias + custoQualidades +
               custoPeculiaridades + custoMeta
    }
}
