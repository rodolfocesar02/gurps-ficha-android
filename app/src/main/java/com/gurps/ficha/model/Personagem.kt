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
    /**
     * **O NT da campanha** — Lote GER-1 (MB p.29).
     *
     * Nao muda nenhuma conta hoje. Existe porque varias regras do livro dependem
     * dele e ficaram de fora justamente por falta deste numero:
     *
     * - o multiplicador de **preco** por qualidade de arma (p.275-276) muda em
     *   NT6 ou menos contra NT7+;
     * - o degrau *"melhores equipamentos possiveis para o seu NT: +NT/2"* do
     *   modificador de pericia (p.346);
     * - a **composicao da lamina** (p.276): pedra em NT0, bronze em NT1, ferro em
     *   NT2, aco em NT3+.
     *
     * ⚠️ Padrao **3**: e o NT medieval, o da maior parte do catalogo de armas e
     * armaduras do Modulo Basico. Ficha antiga desserializa neste valor.
     */
    var nivelTecnologico: Int = 3,
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
    var poderes: List<Poder> = emptyList(),
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
    // Lote MB-6: DE ONDE veio o cansaco (`id da fonte -> quantas unidades`). O
    // total ja esta no pontosFadigaRolagemAtual; isto guarda a ORIGEM, porque PF
    // de fome nao volta com descanso e PF de sono nao volta com comida.
    // Aditivo -> ficha antiga desserializa vazio e o painel se reconcilia sozinho.
    var fadigaPorFonte: Map<String, Int> = emptyMap(),
    // Lote MB-7: as armaduras que estao GUARDADAS (compradas, mas nao vestidas).
    // Guarda o inverso de proposito: lista vazia = vestindo tudo, que e o caso
    // comum E o que mantem toda ficha existente com a RD que ja tinha.
    var armadurasGuardadas: List<String> = emptyList(),
    // Saga (Lote 423): sangramento ATIVO persistido entre cenas/combates (MB p.420). O combate restaura ao
    // iniciar; o passar_tempo do Narrador processa os testes fora de combate. Aditivo — ficha antiga = false/null.
    var sagaSangrando: Boolean = false,
    var sagaSangramentoPenalidadeLocal: Int? = null,
    var sagaSangramentoIntervaloSeg: Int? = null,
    var modeloRacial: ModeloRacial = ModeloRacial(),
    var historicoLog: List<RegistroLog> = emptyList()
) {
    /**
     * Lista consolidada de todas as perícias (pessoais + raciais).
     * Permite que perícias da raça apareçam automaticamente para rolagens.
     */
    val periciasTotais: List<PericiaSelecionada> get() {
        // Só CONCEDIDA entra como perícia própria (p.454). BONUS (p.453)
        // NÃO concede a perícia — é só +N no NH quando o personagem usa
        // a perícia (aplicado em calcularNivel via bonusRacial).
        // todasAsPericias: inclui as de dentro das metacaracteristicas.
        val raciais = modeloRacial.todasAsPericias()
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

    /**
     * TODAS as vantagens: as compradas na ficha **mais as da raça**.
     *
     * Irmã de [periciasTotais], e existe pelo mesmo motivo — só chegou depois.
     *
     * ## O bug que ela conserta (28/07/2026)
     *
     * Achado pelo usuário: uma raça com **ST de Levantamento** não dava bônus
     * nenhum. Investigando, o problema era muito maior que aquela vantagem: o
     * `TraitRuleRegistry` e as dez regras de `domain/rules/` liam apenas
     * `personagem.vantagens`. Ou seja, **toda a automação ignorava a raça** —
     * os 91 efeitos declarados, as defesas, a reação, o autocontrole.
     *
     * Alguns pontos já mesclavam à mão (`SentidoRules`, `bonusDeslocamentoAquatico`,
     * `MagicEngine`), cada um do seu jeito. Era o sinal de que faltava um lugar
     * único — este.
     *
     * Inclui também os componentes das **metacaracterísticas** da raça, que
     * são um pacote dentro do pacote. Ver `ModeloRacial.todasAsVantagens`.
     *
     * ⚠️ **Quem soma efeito de traço deve usar esta lista, não `vantagens`.**
     * A lista crua continua existindo para quem precisa distinguir o que o
     * jogador comprou (custo em pontos, edição, remoção).
     */
    val vantagensTotais: List<VantagemSelecionada>
        get() = vantagens + modeloRacial.todasAsVantagens()

    /** O mesmo para desvantagens. Ver [vantagensTotais]. */
    val desvantagensTotais: List<DesvantagemSelecionada>
        get() = desvantagens + modeloRacial.todasAsDesvantagens()

    // Atributos combinados (Personagem + Modelo Racial)
    val st: Int get() = forca + modeloRacial.modForcaTotal() + com.gurps.ficha.domain.rules.AtributoBonusRules.bonusDe(this, com.gurps.ficha.domain.rules.traits.Atributo.ST)
    val dx: Int get() = destreza + modeloRacial.modDestrezaTotal() + com.gurps.ficha.domain.rules.AtributoBonusRules.bonusDe(this, com.gurps.ficha.domain.rules.traits.Atributo.DX)
    val iq: Int get() = inteligencia + modeloRacial.modInteligenciaTotal() + com.gurps.ficha.domain.rules.AtributoBonusRules.bonusDe(this, com.gurps.ficha.domain.rules.traits.Atributo.IQ)
    val ht: Int get() = vitalidade + modeloRacial.modVitalidadeTotal() + com.gurps.ficha.domain.rules.AtributoBonusRules.bonusDe(this, com.gurps.ficha.domain.rules.traits.Atributo.HT)

    // === CALCULOS AUTOMATICOS ===
    val pontosVida: Int get() = st + modPontosVida + modeloRacial.modPontosVidaTotal() + com.gurps.ficha.domain.rules.AtributoBonusRules.bonusDe(this, com.gurps.ficha.domain.rules.traits.Atributo.PV)
    val vontade: Int get() = iq + modVontade + modeloRacial.modVontadeTotal() + com.gurps.ficha.domain.rules.AtributoBonusRules.bonusDe(this, com.gurps.ficha.domain.rules.traits.Atributo.VONT)
    val percepcao: Int get() = iq + modPercepcao + modeloRacial.modPercepcaoTotal() + com.gurps.ficha.domain.rules.AtributoBonusRules.bonusDe(this, com.gurps.ficha.domain.rules.traits.Atributo.PER)
    val pontosFadiga: Int get() = ht + modPontosFadiga + modeloRacial.modPontosFadigaTotal() + com.gurps.ficha.domain.rules.AtributoBonusRules.bonusDe(this, com.gurps.ficha.domain.rules.traits.Atributo.PF)
    val velocidadeBasica: Float get() = (ht + dx) / 4f + modVelocidadeBasica + modeloRacial.modVelocidadeBasicaTotal()
    val deslocamentoBasico: Int get() = velocidadeBasica.toInt() + modDeslocamentoBasico + modeloRacial.modDeslocamentoBasicoTotal() + com.gurps.ficha.domain.rules.AtributoBonusRules.bonusDe(this, com.gurps.ficha.domain.rules.traits.Atributo.DESL)
    val esquiva: Int get() = (velocidadeBasica + 3).toInt() // Esquiva Básica (sem carga)

    // Deslocamento Aquático: floor(desloc/5) + bônus da vantagem deslocamento_aquatico (racial + pessoal)
    val bonusDeslocamentoAquatico: Int get() {
        val racial = modeloRacial.vantagens.filter { it.definicaoId == "deslocamento_aquatico" }.sumOf { it.nivel }
        val pessoal = vantagens.filter { it.definicaoId == "deslocamento_aquatico" }.sumOf { it.nivel }
        return racial + pessoal
    }
    val deslocamentoAquatico: Int get() = deslocamentoBasico / 5 + bonusDeslocamentoAquatico
    // ST de Levantamento entra AQUI e so aqui (MB p.65): carga sim, dano nao, PV nao.
    val baseCarga: Float get() {
        val stCarga = com.gurps.ficha.domain.rules.StEspecializadaRules.stParaCarga(this)
        return (stCarga * stCarga) / 10f
    }
    val modificadorTamanho: Int get() = modeloRacial.modificadorTamanho
    // ST de Golpe entra AQUI e so aqui (MB p.88): dano sim, carga nao, PV nao.
    val danoGdP: String get() =
        CharacterRules.calcularDanoGdP(com.gurps.ficha.domain.rules.StEspecializadaRules.stParaDano(this))
    val danoGeB: String get() =
        CharacterRules.calcularDanoGeB(com.gurps.ficha.domain.rules.StEspecializadaRules.stParaDano(this))

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

    /**
     * O Talento de cada poder. 🔴 Lote POD-3: **existia e não era cobrado**.
     * `Poder.custoTotalTalento` estava definido no modelo desde sempre e não era
     * chamado em lugar nenhum do projeto — quem comprava Talento não pagava por
     * ele. GURPS Poderes, p.8 e p.29: 5 pontos/nível, 10 se for amplo.
     *
     * ⚠️ Ligar isto **muda o Restantes** de qualquer ficha que já tenha poder
     * com Talento. É o número certo passando a aparecer, não um número novo.
     */
    val pontosPoderes: Int get() = poderes.sumOf { it.custoTotalTalento }

    val pontosGastos: Int get() =
        pontosAtributos + pontosSecundarios + pontosVantagens +
        pontosDesvantagens + pontosQualidades + pontosPeculiaridades + pontosPericias + pontosTecnicas + pontosMagias +
        pontosPoderes +
        modeloRacial.custoTotal

    val pontosTotaisDisponiveis: Int get() = pontosIniciais + xpGanhos
    val pontosRestantes: Int get() = pontosTotaisDisponiveis - pontosGastos

    /**
     * O que o cabecalho escreve sobre os pontos — Lote GER-1.
     *
     * 🔴 Sem XP, "Pontos Iniciais: 314" e a verdade inteira. Com XP, ela vira
     * **meia verdade**: o personagem tem 317 para gastar, e o numero grande da
     * tela continuaria dizendo 314. Quem olhasse o cabecalho concluiria que o
     * XP nao entrou — quando ele ja entrava em `pontosRestantes` desde sempre.
     *
     * Por isso a linha mostra o total **com a conta**, no mesmo espirito do resto
     * do app: o numero e de onde ele veio.
     */
    val rotuloDePontos: String
        get() = if (xpGanhos > 0) {
            "Pontos: $pontosTotaisDisponiveis ($pontosIniciais + $xpGanhos XP)"
        } else {
            "Pontos Iniciais: $pontosIniciais"
        }

    /** O mesmo, falado — sem sinal cru nem parenteses soltos. */
    val rotuloDePontosAcessivel: String
        get() = if (xpGanhos > 0) {
            "$pontosTotaisDisponiveis pontos no total: $pontosIniciais iniciais mais $xpGanhos de experiencia"
        } else {
            "$pontosIniciais pontos iniciais"
        }
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
            if (!jsonObject.has("historicoLog")) {
                jsonObject.add("historicoLog", com.google.gson.JsonArray())
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
    val descricao: String? = null,
    // Id da vantagem/desvantagem DONA, quando o modificador só existe para ela
    // (no livro, as seções "Ampliações/Limitações Especiais" de um traço — ex.:
    // Guelras só vale para Não Respira). Null = modificador geral, vale para
    // qualquer traço. Serve à UI para não oferecer opção sem sentido; o item
    // continua no catálogo geral porque `RacaCatalogo` o procura por id ali.
    @SerializedName(value = "donoId", alternate = ["dono_id"])
    val donoId: String? = null
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
    // Teto de niveis vindo do catalogo (MB): Talentos 4, Mao Fraca 3,
    // Suscetibilidade a Magia 5. Era lido do JSON e DESCARTADO no loader --
    // ver `TetoDeNivelDoTraco`. Null quando o livro nao poe limite.
    @SerializedName(value = "max", alternate = ["nivelMaximo"])
    val max: Int? = null,
    @SerializedName(value = "modificadoresEspecificos", alternate = ["modificadores_especificos"])
    val modificadoresEspecificos: List<ModificadorDefinicao> = emptyList(),
    // Efeitos mecanicos declarados no catalogo (bonus simples lidos pelo
    // EfeitoInterpretador). Regra complexa continua sendo classe Kotlin.
    val efeitos: List<com.gurps.ficha.domain.rules.traits.EfeitoDeclarado> = emptyList()
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
    override val definicaoId: String = "",
    override val nome: String = "",
    var custoBase: Int = 0, // Custo unitario (por nivel) ou custo fixo
    override var nivel: Int = 1,
    override var custoEscolhido: Int = 0, // Custo total escolhido (para VARIAVEL/ESCOLHA)
    var descricao: String = "",
    val tipoCusto: TipoCusto = TipoCusto.FIXO,
    val pagina: Int = 0,
    val specialRule: String? = null,
    var modificadores: List<ModificadorSelecao> = emptyList(),
    override var metadados: Map<String, String>? = null, // Para regras especiais como Ataque Inato
    var poderId: String? = null // Referência ao Poder que possui esta Vantagem
) : com.gurps.ficha.domain.rules.traits.TracoSelecionado {
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
    // Teto de niveis vindo do catalogo (MB): Talentos 4, Mao Fraca 3,
    // Suscetibilidade a Magia 5. Era lido do JSON e DESCARTADO no loader --
    // ver `TetoDeNivelDoTraco`. Null quando o livro nao poe limite.
    @SerializedName(value = "max", alternate = ["nivelMaximo"])
    val max: Int? = null,
    @SerializedName(value = "modificadoresEspecificos", alternate = ["modificadores_especificos"])
    val modificadoresEspecificos: List<ModificadorDefinicao> = emptyList(),
    // Efeitos mecanicos declarados no catalogo (bonus simples lidos pelo
    // EfeitoInterpretador). Regra complexa continua sendo classe Kotlin.
    val efeitos: List<com.gurps.ficha.domain.rules.traits.EfeitoDeclarado> = emptyList()
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
    override val definicaoId: String = "",
    override val nome: String = "",
    var custoBase: Int = 0,
    override var nivel: Int = 1,
    override var custoEscolhido: Int = 0,
    var descricao: String = "",
    // `override` desde o Lote D-NA: o interpretador precisa do NA para resolver
    // as tabelas `porAutocontrole` do catálogo. O campo já existia com este nome.
    override var autocontrole: Int? = null,
    val tipoCusto: TipoCusto = TipoCusto.FIXO,
    val pagina: Int = 0,
    val specialRule: String? = null,
    var modificadores: List<ModificadorSelecao> = emptyList(),
    override var metadados: Map<String, String>? = null,
    var poderId: String? = null // Referência ao Poder que possui esta Desvantagem
) : com.gurps.ficha.domain.rules.traits.TracoSelecionado {
    /** Seis ids existem nos dois catálogos; ver `TracoSelecionado.ehDesvantagem`. */
    override val ehDesvantagem: Boolean get() = true

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
    val preRequisitos: String? = null,
    /** Lote AR-1: regra estruturada legível pela máquina (ao lado da `descricao` fiel). Null = usa o comportamento padrão. */
    val mecanica: com.gurps.ficha.domain.magic.MagiaMecanica? = null,

    // ── Lote MEC-5b: os números CANÔNICOS, extraídos da `descricao` (que é fiel ao livro) ──
    // Os campos de texto acima (`duracao`, `energia`, `tempoOperacao`) são a transcrição do
    // cabeçalho, e a auditoria provou que ela DIVERGE do livro em dezenas de mágicas (Arma
    // Congelante: cabeçalho diz custo 3, o livro diz 4). Estes campos são a verdade conferida
    // contra a descrição; o motor prefere eles e só cai no parser de texto quando são null.
    /** Custo em energia para OPERAR. null = variável/não informado → o motor parseia o texto. */
    val custoOperar: Int? = null,
    /** Custo para MANTER por período. 0 = "não pode ser mantida". null = não informado. */
    val custoManter: Int? = null,
    /** Duração em SEGUNDOS ("1 hora" = 3600). 0 com [duracaoTipo] instantânea/permanente. */
    val duracaoSeg: Int? = null,
    /** "instantanea" | "temporaria" | "permanente". null = não informado. */
    val duracaoTipo: String? = null,
    /** Tempo de operação em SEGUNDOS ("1 hora" de ritual = 3600). */
    val tempoOperacaoSeg: Int? = null,
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
    // Lote EQP-7: os ids de catalogo que faltavam. A arma tinha o dela desde
    // sempre (`armaCatalogoId`) e por isso a ficha tecnica sabia voltar ao
    // catalogo na hora de editar; armadura e escudo nao tinham, e o editor
    // abria sem ficha nenhuma. Aditivos/anulaveis -> ficha antiga desserializa
    // como null e cai no casamento por nome.
    var armaduraCatalogoId: String? = null,
    // Lote EQP-10: se a peça pode ser escondida sob a roupa. É metade do
    // requisito para ser camada de baixo (MB p.287) -- a outra metade é o
    // `*` de flexível, que já vive no `armaduraRd`. A informação existe só
    // no TEXTO da nota do catálogo, então é lida na hora de adicionar e
    // guardada aqui. Ficha antiga desserializa como false.
    var armaduraOcultavel: Boolean = false,
    var escudoCatalogoId: String? = null,
    // ── Stats de combate da arma (Lote 371) — anuláveis: ficha antiga carrega como null (= desconhecido). ──
    var armaAlcanceCorpoACorpo: String? = null, // "C" | "1" | "1,2"
    var armaDuasMaos: Boolean = false,
    var armaPrecisao: Int? = null,              // Acc (Apontar)
    // Lote ARMA-1: o "+N" da mira acoplada, que o catálogo escreve como "6+1" e
    // o app descartava. Fica SEPARADO do armaPrecisao porque usar a mira é
    // escolha do jogador na hora do tiro — somar aqui daria o bônus de graça.
    var armaPrecisaoAcessorio: Int? = null,     // Acc do acessório (luneta, red dot)
    var armaMeioDanoMetros: Int? = null,        // 1/2D
    var armaMaximoMetros: Int? = null,          // Máx
    var armaAlcanceMultStRaw: String? = null,   // arcos/arremesso: "×10/×15"
    var armaCadenciaTiro: Int? = null,          // CdT/RoF
    var armaTirosRaw: String? = null,           // "6(3)"
    var armaMagnitude: Int? = null,             // Bulk
    var armaRecuo: Int? = null,                 // Rcl
    // Lote EQP-11: a qualidade da arma (MB p.275-276), guardada pelo NOME do
    // enum para nao prender o Gson a uma ordem. Null = a qualidade padrao
    // do livro (boa), que e como o catalogo publica os precos.
    var armaQualidade: String? = null,
    var armaAparar: String? = null,             // coluna Aparar: "0", "-1", "0D" (desbal.), "0E"/"F" (esgrima), "Não"
    // Saga (item 1 do teste de batalha): item TIRADO do herói pela narrativa (desarmado/capturado). Continua
    // na ficha (recuperável) mas indisponível: o combate ignora arma confiscada (some dos ataques) e armadura
    // confiscada (não dá RD). Aditivo/anulável → fichas antigas (Gson sem o campo) desserializam como false.
    var confiscado: Boolean = false
) {
    /**
     * Dano da arma já resolvido contra a ST de quem a empunha.
     *
     * [stExtra] existe para a **ST Braçal** (MB p.89): empunhar uma arma é ação
     * de braço, então quando o jogador liga o seletor na aba Rolagem a arma
     * passa a bater com a força dos braços. Fica em zero por padrão — o combate
     * tático e a tela de equipamento continuam usando a ST do corpo.
     */
    fun danoCalculadoComSt(
        personagem: Personagem,
        periciaId: String? = null,
        stExtra: Int = 0
    ): String? {
        val raw = armaDanoRaw?.trim().orEmpty()
        if (raw.isBlank()) return null

        // Consulta bônus de vantagens (ex: Mestre de Armas)
        val bonusPorDado = com.gurps.ficha.domain.rules.traits.TraitRuleRegistry.getDamageBonusPerDie(
            personagem,
            periciaId,
            nome,
            armaGrupo
        )

        // A ST de Golpe vale para "armas que utilizam a ST do personagem para
        // determinar seu potencial ofensivo" (MB p.88), entao entra aqui junto
        // com o stExtra da ST Bracal -- sao vantagens diferentes e somam.
        val stGolpe = com.gurps.ficha.domain.rules.StEspecializadaRules.bonusDeGolpe(personagem)
        val resolvido = CharacterRules.resolverDanoPorSt(
            raw, personagem.forca + stExtra + stGolpe, bonusPorDado
        ) ?: return null

        // 🔴 Lote EQP-11. A qualidade da arma (MB p.275-276) some ao dano, e so
        // em LAMINA: uma maca superior ganha o -1 na quebra e nenhum dano. Antes
        // disto o jogador anotava "+1 Dano" na NOTA, que o combate nao le.
        return com.gurps.ficha.domain.rules.QualidadeDaArma.aplicarAoDano(
            resolvido,
            bonusDeQualidade()
        )
    }

    /** O bônus de dano da qualidade desta arma, já filtrado pelo tipo (EQP-11). */
    fun bonusDeQualidade(): Int {
        val nivel = qualidadeDaArma() ?: return 0
        val tipo = com.gurps.ficha.domain.rules.TipoDeDanoNoTexto.ler(armaDanoRaw) ?: return 0
        return com.gurps.ficha.domain.rules.QualidadeDaArma.bonusDeDano(
            nivel = nivel,
            tipo = tipo,
            ehEspadaOuEsgrima = com.gurps.ficha.domain.rules.QualidadeDaArma
                .ehEspadaOuEsgrima(armaGrupo ?: nome)
        )
    }

    /** A qualidade guardada, ou null quando é a padrão do livro. */
    fun qualidadeDaArma(): com.gurps.ficha.domain.rules.QualidadeDaArma.Nivel? =
        armaQualidade?.let { nome ->
            com.gurps.ficha.domain.rules.QualidadeDaArma.Nivel.entries.firstOrNull { it.name == nome }
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
    var bonusManualBloqueio: Int = 0,

    // Notas do bônus manual: o jogador anota DE ONDE vem cada bônus digitado
    // (item, magia temporária, decisão do Mestre...). Existem porque, quando as
    // vantagens forem automatizadas, um bônus digitado à mão para algo que o app
    // passou a calcular sozinho vira contagem dupla — e sem a nota ninguém tem
    // como saber a origem para decidir. Aditivas: ficha antiga desserializa "".
    var notaBonusManualEsquiva: String = "",
    var notaBonusManualApara: String = "",
    var notaBonusManualBloqueio: String = ""
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

    /**
     * BD (Bônus de Defesa) do escudo/capa. Lote 379: só conta quando o jogador ESCOLHE explicitamente o
     * escudo na defesa de Bloqueio (`escudoSelecionadoNome`). Antes havia um fallback que pegava o escudo de
     * maior BD só por estar na lista de equipamentos — isso confundia, pois o BD aparecia em Esquiva/Apara
     * sem o jogador declarar que estava usando o escudo. (GURPS MB p.375: o BD vale em TODAS as defesas
     * quando o escudo está pronto; aqui "pronto" = selecionado na defesa de Bloqueio.)
     */
    fun getBonusEscudo(personagem: Personagem): Int {
        val nomeSelecionado = escudoSelecionadoNome?.trim()?.takeIf { it.isNotEmpty() } ?: return 0
        val escudo = personagem.equipamentos
            .filter { it.tipo == TipoEquipamento.ESCUDO || it.tipo == TipoEquipamento.CAPA }
            .find { it.nome.trim().equals(nomeSelecionado, ignoreCase = true) }
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

// A lista PERICIAS_COMBATE mudou de casa: agora vive em
// `PericiasDeCombate.kt`, dividida em corpo a corpo e a distancia (Lote
// MIRA-2). O nome e o conteudo continuam identicos -- quem usava, continua
// usando sem mudar nada.
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
