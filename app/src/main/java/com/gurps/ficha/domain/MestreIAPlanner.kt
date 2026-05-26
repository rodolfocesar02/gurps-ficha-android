package com.gurps.ficha.domain

import com.gurps.ficha.domain.filters.CatalogFilters
import com.gurps.ficha.model.Equipamento

/**
 * MestreIAPlanner — Extração semântica de intenção estruturada.
 * Sem chamada de rede. Instantâneo e sem ponto de falha.
 *
 * Mudança central (Lote 270): substitui detecção léxico-cêntrica por
 * IntencaoEstruturada (entidadePrimaria + entidadeSecundaria + relacao).
 * Termos recebem peso explícito (TermoPonderado) para que BM25 e FTS
 * priorizem o núcleo da pergunta, não o contexto.
 */
object MestreIAPlanner {

    // ─────────────────────────────────────────────────────────────────────────
    // Tipos públicos
    // ─────────────────────────────────────────────────────────────────────────

    enum class IntencaoBusca {
        QUER_TABELA,
        QUER_EXPLICACAO,
        QUER_REGRA,
        QUER_CALCULO,
        GERAL
    }

    /** Tipo de relação semântica entre as entidades da pergunta. */
    enum class RelacaoSemantica {
        USAR_CONTRA,      // "magia X contra Y" — X é o sujeito, Y é o alvo
        FUNCIONAMENTO,    // "como funciona X" — X é o sujeito explicado
        PENALIDADE_EM,    // "penalidade de X em ambiente Y" — X ação, Y contexto
        CALCULO_DE,       // "quanto de dano faz X" — X é o sujeito calculado
        TABELA_DE,        // "tabela de X" — X é o assunto listado
        GENERICO          // nenhuma relação específica detectada
    }

    /**
     * Termo com peso explícito.
     * peso=1.0 → núcleo da pergunta (sujeito principal)
     * peso=0.6 → entidade secundária (alvo ou contexto)
     * peso=0.3 → expansão semântica (sinônimos)
     */
    data class TermoPonderado(
        val termo: String,
        val peso: Double
    )

    /**
     * Intenção estruturada: extrai o sujeito real da dúvida (entidadePrimaria)
     * separado do contexto/alvo (entidadeSecundaria) e do tipo de relação.
     * Exemplo: "é possível usar magia de bloqueio contra golpe fulminante"
     *   entidadePrimaria   = "magia bloqueio"  (o que o jogador quer usar/entender)
     *   entidadeSecundaria = "golpe fulminante" (o alvo/contexto)
     *   relacao            = USAR_CONTRA
     */
    data class IntencaoEstruturada(
        val intencaoBusca: IntencaoBusca,
        val entidadePrimaria: String,
        val entidadeSecundaria: String,
        val relacao: RelacaoSemantica,
        val termosPonderados: List<TermoPonderado>
    )

    data class PlanoDeBusca(
        val termos: List<String>,
        val categorias: List<String>,
        val subQueriesStats: List<String> = emptyList(),
        val contextoEquipamentos: String = "",
        val subQueriesTemáticas: List<String> = emptyList(),
        val livrosRelevantes: List<String> = emptyList(),
        val intencaoEstruturada: IntencaoEstruturada? = null
    )

    // ─────────────────────────────────────────────────────────────────────────
    // Padrões relacionais — detectam estrutura "X contra Y", "X em Y", etc.
    // ─────────────────────────────────────────────────────────────────────────

    private val padroesUsarContra = Regex(
        "(usar?|utilizar?|aplicar?|funciona?r?|lançar?|conjurar?|usar?)\\s+(.+?)\\s+(contra|em|sobre|para bloquear|para parar|vs\\.?)\\s+(.+)",
        RegexOption.IGNORE_CASE
    )

    private val padroesPenalidadeEm = Regex(
        "(penalidade|modificador|redutor|bonus|malus)\\s+(de|do|da|para)\\s+(.+?)\\s+(em|dentro|submerso|na?|no?|sob)\\s+(.+)",
        RegexOption.IGNORE_CASE
    )

    private val padroesCalculoDe = Regex(
        "(quanto|qual\\s+o?a?)\\s+(dano|penalidade|bonus|modificador|valor|alcance|custo)\\s+(.+)",
        RegexOption.IGNORE_CASE
    )

    // ─────────────────────────────────────────────────────────────────────────
    // Detectores por grupo semântico — usados para classificar entidades
    // ─────────────────────────────────────────────────────────────────────────

    private val grupoMagia = setOf(
        "magia", "feitico", "encantamento", "conjuracao", "escola", "mana", "energia",
        "bloqueio", "fireball", "bola", "cura", "ilusao", "telecinese", "adivinhacao",
        "necromancia", "elemental", "protecao", "escudo"
    )

    private val grupoCombateDistancia = setOf(
        "tiro", "atirar", "disparo", "pistola", "revolver", "rifle", "espingarda",
        "metralhadora", "carabina", "fuzil", "submetralhadora", "garrucha",
        "arco", "besta", "funda", "zarabatana", "bodoque",
        "shuriken", "kunai", "dardo", "arremesso", "flecha", "virote"
    )

    private val grupoCombateCaC = setOf(
        "espada", "sabre", "florete", "estoque", "katana", "cimitarra", "cutelo",
        "faca", "adaga", "punhal", "machado", "clava", "maca", "porrete", "martelo",
        "mangual", "lanca", "alabarda", "naginata", "cajado", "bordao", "tridente",
        "arpao", "chicote", "combate", "ataque", "soco", "chute", "derrubada", "agarrar", "presa"
    )

    private val grupoAmbiente = setOf(
        "agua", "piscina", "submerso", "aquatico", "subaquatico", "mergulho", "mar",
        "rio", "lago", "oceano", "escuridao", "neblina", "fumaca", "noite", "luz",
        "fogo", "calor", "frio", "chuva", "vento", "gravidade"
    )

    private val grupoDefesa = setOf(
        "esquiva", "apara", "bloqueio", "defesa", "escudo", "armadura", "rd",
        "protecao", "cobertura", "db"
    )

    private val grupoMovimento = setOf(
        "correr", "mover", "deslocamento", "velocidade", "pular", "salto",
        "nadar", "natacao", "voar", "trepar", "escalar", "rastejar"
    )

    private val grupoAtributo = setOf(
        "st", "forca", "dx", "destreza", "iq", "inteligencia", "ht", "vitalidade",
        "vontade", "percepcao", "velocidade", "pv", "pf", "fadiga"
    )

    // ─────────────────────────────────────────────────────────────────────────
    // Vantagens/Desvantagens GURPS conhecidas — detectadas na query para busca direta
    // Quando um nome exato bate aqui, o Planner usa o nome como entidade primária e
    // gera sub-queries específicas (não genéricas de combate).
    // ─────────────────────────────────────────────────────────────────────────

    private val vantagensConhecidas: Map<String, String> = mapOf(
        "reflexos em combate"        to "reflexos em combate vantagem bonus defesa",
        "treinado por um mestre"     to "treinado por um mestre vantagem combate",
        "mestre de armas"            to "mestre de armas vantagem combate",
        "duro de matar"              to "duro de matar vantagem pv",
        "hipoalgia"                  to "hipoalgia alto limiar dor vantagem",
        "alto limiar de dor"         to "hipoalgia alto limiar dor vantagem",
        "ambidestria"                to "ambidestria vantagem mao inabil",
        "visao noturna"              to "visao noturna vantagem penalidade escuridao",
        "sentido aguado"             to "sentido aguado vantagem percepcao",
        "equilibrio perfeito"        to "equilibrio perfeito vantagem acrobacia",
        "queda de gato"              to "queda de gato vantagem queda dano",
        "aptidao magica"             to "aptidao magica vantagem magia requisito",
        "bencao"                     to "bencao vantagem teste dado bonus",
        "sorte"                      to "sorte vantagem teste dado bonus",
        "ataque adicional"           to "ataque adicional vantagem combate manobra",
        "nocao do tempo ampliada"    to "nocao do tempo ampliada vantagem tempo",
        "nta"                        to "nocao do tempo ampliada vantagem tempo",
        "empatia"                    to "empatia vantagem social reacao",
        "regeneracao"                to "regeneracao vantagem cura pv",
        "recuperacao acelerada"      to "recuperacao acelerada vantagem cura pv",
        "defesas ampliadas"          to "defesas ampliadas vantagem bonus defesa",
        "vantagem em combate"        to "vantagem em combate manobra bonus",
        "resistencia a magia"        to "resistencia magica vantagem magia",
        "paralisia frente ao combate" to "paralisia frente ao combate desvantagem",
        "covardia"                   to "covardia desvantagem medo combate",
        "impulsividade"              to "impulsividade desvantagem controle",
        "honestidade"                to "honestidade desvantagem codigo",
        "fanatismo"                  to "fanatismo desvantagem codigo",
        "fobia"                      to "fobia desvantagem medo panico",
        "berserk"                    to "berserk desvantagem furia combate",
        "dependencia"                to "dependencia desvantagem habito",
        "vicio"                      to "vicio desvantagem habito",
        "memoria fotografica"        to "memoria fotografica vantagem iq",
        "calculo rapido"             to "calculo rapido vantagem iq matematica",
        "percepcao aguada"           to "percepcao aguada vantagem per sentido",
        "fleuma"                     to "fleuma vantagem panico verificacao",
        "venturoso"                  to "venturoso vantagem iniciativa combate"
    )

    // ─────────────────────────────────────────────────────────────────────────
    // Padrões relacionais para sub-queries (golpe fulminante, queda livre, etc.)
    // ─────────────────────────────────────────────────────────────────────────

    private val termosCombinados = mapOf(
        "golpe fulminante"     to "golpe fulminante ataque dano critico",
        "golpe_fulminante"     to "golpe fulminante ataque dano critico",
        "ataque determinado"   to "ataque determinado manobra combate",
        "ataque all-out"       to "ataque all out manobra combate",
        "defesa total"         to "defesa total manobra combate esquiva",
        "desvio fisico"        to "desvio fisico esquiva manobra",
        "acerto critico"       to "acerto critico tabela resultado dado",
        "falha critica"        to "falha critica tabela resultado dado",
        "queda livre"          to "queda livre altitude dano impacto",
        "magia de bloqueio"    to "magia bloqueio defesa feitico escola",
        "bloqueio magico"      to "bloqueio magico defesa feitico",
        "escudo magico"        to "escudo magico feitico protecao",
        "bola de fogo"         to "bola fogo fireball magia dano",
        "cone de fogo"         to "cone fogo magia area dano"
    )

    // ─────────────────────────────────────────────────────────────────────────
    // Detector de itens (query de pré-busca de stats)
    // ─────────────────────────────────────────────────────────────────────────

    private val itemDetector = mapOf(
        "revolver"        to "revolver pistola alcance dano tabela armas fogo",
        "pistola"         to "pistola revolver alcance dano tabela armas fogo",
        "rifle"           to "rifle carabina alcance dano tabela armas fogo",
        "espingarda"      to "espingarda shotgun alcance dano tabela armas fogo",
        "metralhadora"    to "metralhadora smg alcance dano cadencia tabela armas",
        "carabina"        to "carabina rifle alcance dano tabela armas fogo",
        "fuzil"           to "fuzil rifle alcance dano tabela armas fogo",
        "submetralhadora" to "submetralhadora smg alcance dano tabela armas",
        "garrucha"        to "garrucha pistola alcance dano tabela armas fogo",
        "arco"            to "arco flecha alcance dano tabela armas distancia",
        "besta"           to "besta virote alcance dano tabela armas distancia",
        "funda"           to "funda pedra alcance dano tabela armas arremesso",
        "zarabatana"      to "zarabatana dardo alcance dano tabela armas",
        "bodoque"         to "bodoque funda alcance dano tabela armas",
        "shuriken"        to "shuriken estrela arremesso alcance dano tabela armas",
        "kunai"           to "kunai faca arremesso alcance dano tabela armas",
        "dardo"           to "dardo arremesso alcance dano tabela armas",
        "espada"          to "espada dano alcance tabela armas corpo combate",
        "sabre"           to "sabre espada dano tabela armas corpo",
        "florete"         to "florete estoque dano tabela armas corpo",
        "estoque"         to "estoque florete dano tabela armas corpo",
        "katana"          to "katana espada dano tabela armas corpo",
        "cimitarra"       to "cimitarra espada sabre dano tabela armas corpo",
        "cutelo"          to "cutelo machado dano tabela armas corpo",
        "faca"            to "faca adaga dano alcance tabela armas corpo",
        "adaga"           to "adaga faca dano tabela armas corpo",
        "punhal"          to "punhal adaga dano tabela armas corpo",
        "machado"         to "machado dano alcance tabela armas corpo",
        "clava"           to "clava porrete dano tabela armas corpo",
        "maca"            to "maca porrete dano tabela armas corpo",
        "porrete"         to "porrete clava dano tabela armas corpo",
        "martelo"         to "martelo guerra dano tabela armas corpo",
        "mangual"         to "mangual corrente dano tabela armas corpo",
        "lanca"           to "lanca alabarda dano alcance haste tabela armas corpo",
        "alabarda"        to "alabarda lanca dano alcance haste tabela armas corpo",
        "naginata"        to "naginata lanca dano alcance haste tabela armas",
        "cajado"          to "cajado bordao dano alcance haste tabela armas",
        "bordao"          to "bordao cajado dano alcance tabela armas corpo",
        "tridente"        to "tridente lanca dano alcance haste tabela armas",
        "arpao"           to "arpao lanca arremesso dano alcance tabela armas",
        "chicote"         to "chicote dano alcance tabela armas corpo",
        "armadura"        to "armadura RD resistencia dano peso tabela armaduras",
        "colete"          to "colete armadura RD peso tabela armaduras",
        "elmo"            to "elmo capacete armadura RD tabela armaduras",
        "capacete"        to "capacete elmo armadura RD tabela armaduras",
        "cota"            to "cota malha armadura RD peso tabela armaduras",
        "lorica"          to "lorica segmentata armadura RD tabela armaduras",
        "brigantina"      to "brigantina armadura RD peso tabela armaduras",
        "placa"           to "placa armadura RD peso tabela armaduras",
        "broquel"         to "broquel escudo DB bloqueio tabela escudos",
        "rodela"          to "rodela escudo DB bloqueio tabela escudos"
    )

    val livrosPorCategoria: Map<String, List<String>> = mapOf(
        "tiro"           to listOf("pt_modulo_basico", "pt_gun_fu", "pt_pyramid_26_underwater"),
        "arma_fogo"      to listOf("pt_modulo_basico", "pt_gun_fu"),
        "subaquatico"    to listOf("pt_modulo_basico", "pt_pyramid_26_underwater"),
        "magia"          to listOf("pt_modulo_basico", "pt_magia"),
        "combate"        to listOf("pt_modulo_basico", "pt_artes_marciais", "pt_gun_fu"),
        "artes_marciais" to listOf("pt_modulo_basico", "pt_artes_marciais"),
        "pericia"        to listOf("pt_modulo_basico"),
        "raca"           to listOf("pt_modulo_basico"),
        "equipamento"    to listOf("pt_modulo_basico", "pt_gun_fu", "pt_artes_marciais")
    )

    private val dicionarioTecnico = mapOf(
        "sangramento"    to listOf("hemorragia", "ferimento", "saude", "pv"),
        "hemorragia"     to listOf("sangramento", "ferimento", "pv"),
        "incapacitado"   to listOf("ferimento", "pv", "morte", "desmaiado", "inconsciente"),
        "inconsciente"   to listOf("desmaiado", "incapacitado", "pv", "ferimento"),
        "morte"          to listOf("morto", "incapacitado", "pv", "fatal"),
        "morto"          to listOf("morte", "pv", "fatal"),
        "pular"          to listOf("salto", "distancia", "altura", "acrobacia"),
        "salto"          to listOf("pular", "distancia", "altura", "acrobacia"),
        "correr"         to listOf("corrida", "deslocamento", "velocidade", "sprint"),
        "corrida"        to listOf("correr", "deslocamento", "velocidade"),
        "nadar"          to listOf("natacao", "agua", "deslocamento", "subaquatico"),
        "natacao"        to listOf("nadar", "agua", "subaquatico", "pericia"),
        "trepar"         to listOf("escalar", "subir", "altura", "pericia"),
        "escalar"        to listOf("trepar", "subir", "altura", "acrobacia"),
        "rastejar"       to listOf("agachar", "pronar", "movimento", "penalidade"),
        "voar"           to listOf("voo", "altitude", "asa", "deslocamento"),
        "voo"            to listOf("voar", "altitude", "asa", "deslocamento"),
        "impacto"        to listOf("colisao", "batida", "queda", "atropelamento", "dano"),
        "colisao"        to listOf("impacto", "queda", "atropelamento", "encontro", "dano"),
        "queda"          to listOf("impacto", "colisao", "altitude", "precipicio", "dano"),
        "atropelamento"  to listOf("colisao", "impacto", "veiculo", "dano"),
        "altitude"       to listOf("queda", "altura", "precipicio"),
        "asfixia"        to listOf("afogamento", "sufocamento", "respiracao", "folego", "ar"),
        "afogamento"     to listOf("asfixia", "agua", "submerso", "subaquatico", "sufocamento"),
        "sufocamento"    to listOf("asfixia", "afogamento", "respiracao", "ar", "folego"),
        "fogo"           to listOf("queimadura", "chama", "calor", "dano", "incendio"),
        "queimadura"     to listOf("fogo", "calor", "dano", "chama"),
        "frio"           to listOf("congelamento", "hipotermia", "temperatura", "penalidade"),
        "calor"          to listOf("fogo", "temperatura", "dano", "exaustao"),
        "veneno"         to listOf("toxina", "envenenamento", "efeito", "resistencia"),
        "doenca"         to listOf("infeccao", "enfermidade", "resistencia", "ht"),
        "radiacao"       to listOf("radioatividade", "dano", "rad", "envenenamento"),
        "st"             to listOf("forca", "levantamento", "carga", "dano", "gdp", "geb"),
        "forca"          to listOf("st", "levantamento", "carga", "muscular", "dano"),
        "dx"             to listOf("destreza", "agilidade", "coordenacao", "pericia"),
        "destreza"       to listOf("dx", "agilidade", "coordenacao"),
        "iq"             to listOf("inteligencia", "vontade", "percepcao", "raciocinio"),
        "inteligencia"   to listOf("iq", "vontade", "percepcao", "raciocinio"),
        "ht"             to listOf("vitalidade", "saude", "fadiga", "pf", "sobrevivencia"),
        "vitalidade"     to listOf("ht", "saude", "pf", "resistencia"),
        "vontade"        to listOf("iq", "resistencia", "mental", "medo"),
        "percepcao"      to listOf("iq", "sentido", "visao", "audicao", "alerta"),
        "velocidade"     to listOf("deslocamento", "esquiva", "movimento", "rapidez"),
        "deslocamento"   to listOf("velocidade", "movimento", "passo", "corrida"),
        "movimento"      to listOf("velocidade", "deslocamento", "passo", "corrida"),
        "submerso"       to listOf("agua", "aquatico", "mergulho", "piscina", "mar", "subaquatico", "underwater"),
        "aquatico"       to listOf("submerso", "agua", "subaquatico", "underwater"),
        "piscina"        to listOf("agua", "submerso", "aquatico", "subaquatico", "mergulho", "underwater"),
        "agua"           to listOf("submerso", "aquatico", "subaquatico", "piscina", "mar", "underwater"),
        "subaquatico"    to listOf("agua", "submerso", "aquatico", "piscina", "mergulho", "underwater"),
        "mergulho"       to listOf("agua", "submerso", "aquatico", "subaquatico", "underwater"),
        "mar"            to listOf("agua", "submerso", "aquatico", "oceano"),
        "esquiva"        to listOf("defesa", "apara", "bloqueio", "evasao", "dx"),
        "apara"          to listOf("defesa", "esquiva", "bloqueio", "escudo", "arma"),
        "bloqueio"       to listOf("defesa", "esquiva", "apara", "escudo"),
        "defesa"         to listOf("esquiva", "apara", "bloqueio", "protecao"),
        "escudo"         to listOf("bloqueio", "defesa", "apara", "db"),
        "db"             to listOf("escudo", "bloqueio", "bonus defesa"),
        "magia"          to listOf("feitico", "encantamento", "conjuracao", "escola", "energia"),
        "feitico"        to listOf("magia", "encantamento", "conjuracao", "escola"),
        "encantamento"   to listOf("magia", "feitico", "conjuracao"),
        "escola"         to listOf("magia", "categoria", "tipo", "feitico"),
        "energia"        to listOf("magia", "custo", "pf", "mana"),
        "mana"           to listOf("energia", "magia", "pf", "custo"),
        "prereq"         to listOf("prerequisito", "requisito", "magia", "pericia"),
        "prerequisito"   to listOf("prereq", "requisito", "magia", "habilidade"),
        "pericia"        to listOf("habilidade", "nivel", "nh", "aptidao", "dx", "iq"),
        "habilidade"     to listOf("pericia", "nivel", "nh", "aptidao"),
        "nh"             to listOf("nivel", "pericia", "habilidade", "pontos"),
        "nivel"          to listOf("nh", "pericia", "habilidade"),
        "acrobacia"      to listOf("salto", "equilibrio", "dx", "pericia"),
        "furtividade"    to listOf("esconder", "sorrateiro", "dx", "pericia"),
        "persuasao"      to listOf("conversa", "negociacao", "iq", "pericia"),
        "intimidacao"    to listOf("medo", "ameaca", "iq", "pericia"),
        "primeiros socorros" to listOf("cura", "medicina", "ferimento", "pv"),
        "ataque"         to listOf("dano", "acerto", "combate", "ofensiva", "manobra"),
        "combate"        to listOf("ataque", "defesa", "luta", "batalha", "manobra"),
        "manobra"        to listOf("combate", "ataque", "movimento", "turno"),
        "turno"          to listOf("manobra", "combate", "acao", "tempo"),
        "critico"        to listOf("acerto critico", "falha critica", "dado", "3d6"),
        "acerto"         to listOf("ataque", "dado", "nh", "sucesso"),
        "falha"          to listOf("erro", "dado", "nh", "penalidade"),
        "tiro"           to listOf("disparo", "arma", "fogo", "projetil", "atirar", "arremesso"),
        "atirar"         to listOf("tiro", "disparo", "fogo", "acertar", "alcance"),
        "disparo"        to listOf("tiro", "atirar", "fogo", "projetil", "municao"),
        "alcance"        to listOf("distancia", "range", "metro", "faixa", "distante", "meia distancia"),
        "municao"        to listOf("bala", "cartucho", "disparo", "capacidade"),
        "cadencia"       to listOf("tiro", "disparo", "municao", "rajada"),
        "rajada"         to listOf("cadencia", "rafaga", "municao", "tiro"),
        "mira"           to listOf("tiro", "alcance", "penalidade", "alvo"),
        "pistola"        to listOf("revolver", "arma", "fogo", "disparo", "tiro"),
        "revolver"       to listOf("pistola", "arma", "fogo", "disparo", "tiro"),
        "rifle"          to listOf("arma", "fogo", "disparo", "tiro", "longa distancia"),
        "espingarda"     to listOf("arma", "fogo", "disparo", "tiro", "chumbinho"),
        "metralhadora"   to listOf("arma", "fogo", "rajada", "cadencia", "municao"),
        "submetralhadora" to listOf("arma", "fogo", "rajada", "pistola"),
        "arco"           to listOf("flecha", "arremesso", "alcance", "dano"),
        "besta"          to listOf("virote", "arremesso", "alcance", "dano"),
        "arremesso"      to listOf("arco", "funda", "lancamento", "alcance", "tiro"),
        "artes marciais" to listOf("luta", "marcial", "combate", "tecnica"),
        "tecnica"        to listOf("pericia", "combate", "manobra", "nh"),
        "presa"          to listOf("agarrar", "imobilizar", "luta", "combate"),
        "agarrar"        to listOf("presa", "imobilizar", "luta", "st"),
        "derrubada"      to listOf("jogada", "queda", "combate", "dx"),
        "chute"          to listOf("perna", "dano", "combate", "dx"),
        "soco"           to listOf("pugno", "dano", "combate", "st"),
        "dano"           to listOf("ataque", "ferimento", "pv", "lesao", "gdp", "geb"),
        "ferimento"      to listOf("dano", "lesao", "pv", "sangramento", "cura"),
        "pv"             to listOf("vida", "saude", "ferimento", "pontos de vida"),
        "pf"             to listOf("fadiga", "cansaco", "energia", "pontos de fadiga"),
        "fadiga"         to listOf("pf", "cansaco", "energia", "exaustao", "ht"),
        "gdp"            to listOf("dado de penetracao", "dano", "st", "arma"),
        "geb"            to listOf("dado de esmagar", "dano", "st", "arma"),
        "penalidade"     to listOf("modificador", "bonus", "malus", "reducao", "ajuste"),
        "modificador"    to listOf("penalidade", "bonus", "ajuste", "fator"),
        "redutor"        to listOf("penalidade", "modificador", "subtracao"),
        "bonus"          to listOf("modificador", "penalidade", "ajuste", "adicional"),
        "cura"           to listOf("recuperacao", "primeiros socorros", "medicina", "ferimento", "pv"),
        "recuperacao"    to listOf("cura", "descanso", "medicina", "pv"),
        "medicina"       to listOf("cura", "primeiros socorros", "recuperacao", "iq"),
        "descanso"       to listOf("recuperacao", "cura", "pv", "pf"),
        "escuridao"      to listOf("visibilidade", "noite", "penalidade", "iluminacao"),
        "visibilidade"   to listOf("escuridao", "iluminacao", "claridade", "neblina"),
        "neblina"        to listOf("visibilidade", "penalidade", "fumaca"),
        "fumaca"         to listOf("visibilidade", "neblina", "penalidade"),
        "iluminacao"     to listOf("visibilidade", "escuridao", "luz", "lanterna"),
        "armadura"       to listOf("rd", "protecao", "cobertura", "blindagem", "peso"),
        "rd"             to listOf("armadura", "resistencia", "protecao", "reducao de dano"),
        "cobertura"      to listOf("armadura", "rd", "local", "protecao"),
        "cavalo"         to listOf("montaria", "cavaleiro", "cavalgar", "animal"),
        "montaria"       to listOf("cavalo", "cavaleiro", "cavalgar", "animal"),
        "cavalgar"       to listOf("cavalo", "montaria", "pericia", "dx"),
        "veiculo"        to listOf("carro", "moto", "nave", "pilotagem", "velocidade"),
        "pilotagem"      to listOf("veiculo", "pericia", "dx", "iq"),
        "medo"           to listOf("fobia", "terror", "vontade", "panique"),
        "fobia"          to listOf("medo", "terror", "vontade", "desvantagem"),
        "panico"         to listOf("medo", "fobia", "terror", "vontade"),
        "sanidade"       to listOf("mental", "horror", "vontade"),
        "reputacao"      to listOf("status", "social", "reacao", "fama"),
        "status"         to listOf("reputacao", "social", "riqueza", "classe"),
        "reacao"         to listOf("npc", "social", "reputacao", "carisma"),
        "carisma"        to listOf("persuasao", "reacao", "social", "iq"),
        "custo"          to listOf("preco", "dinheiro", "compra", "equipamento"),
        "peso"           to listOf("carga", "encumbrance", "st", "penalidade"),
        "carga"          to listOf("peso", "encumbrance", "st", "velocidade"),
        "encumbrance"    to listOf("carga", "peso", "penalidade", "velocidade")
    )

    private val stopWords = setOf(
        "como", "funciona", "regra", "regras", "quais", "preciso", "para", "sobre", "qual",
        "uma", "um", "com", "dos", "das", "pela", "pelo", "onde", "quando", "quem", "sao",
        "gurps", "edicao", "calculo", "calcular", "lista", "tabela", "de", "da", "do",
        "nos", "nas", "aos", "pra", "fale", "meu", "meus", "minha", "minhas",
        "queria", "quero", "saber", "ajuda", "tem", "existe", "existem", "por",
        "diga", "explique", "mostre", "entao", "voce", "isso", "esse", "essa",
        "que", "acontece", "num", "numa", "ser", "seja", "pode", "fazer",
        "nao", "mais", "muito", "cada", "deve", "seja", "sejam", "me",
        "posso", "possivel", "usar", "utilizar", "aplicar", "contra", "em", "sobre",
        "quando", "se", "ou", "e", "a", "o", "as", "os", "ao", "e"
    )

    // ─────────────────────────────────────────────────────────────────────────
    // API pública
    // ─────────────────────────────────────────────────────────────────────────

    fun planejarBusca(pergunta: String, equipamentos: List<Equipamento> = emptyList()): PlanoDeBusca {
        val perguntaNorm = CatalogFilters.normalizarBusca(pergunta)
        val intencao = extrairIntencaoEstruturada(pergunta, perguntaNorm)

        // Termos ponderados: núcleo primeiro, contexto depois, expansão por último
        val termosPonderados = intencao.termosPonderados
        val termosOrdenados = termosPonderados.sortedByDescending { it.peso }.map { it.termo }

        // Expansão semântica apenas do núcleo (entidade primária)
        val termosExpandidos = mutableSetOf<String>()
        termosOrdenados.forEach { termosExpandidos.add(it) }

        val termosPrimarios = intencao.entidadePrimaria.split(Regex("\\s+")).filter { it.length >= 2 }
        termosPrimarios.forEach { t ->
            dicionarioTecnico[t]?.let { termosExpandidos.addAll(it) }
        }

        // Expansão da secundária com peso menor — sem expansão bidirecional
        val termosSecundarios = intencao.entidadeSecundaria.split(Regex("\\s+")).filter { it.length >= 2 }
        termosSecundarios.forEach { t ->
            dicionarioTecnico[t]?.take(3)?.let { termosExpandidos.addAll(it) }
        }

        val categorias = inferirCategorias(termosPrimarios)

        // Inventário do personagem
        val temPossessivo = listOf("meu", "minha", "meus", "minhas").any { perguntaNorm.contains(it) }
        val equipamentosMatchados = mutableListOf<Equipamento>()
        val termosBrutos = perguntaNorm.split(Regex("\\s+")).filter { it.length >= 2 && it !in stopWords }

        for (equip in equipamentos) {
            val nomeNorm = CatalogFilters.normalizarBusca(equip.nome)
            val nomeParts = nomeNorm.split(Regex("\\s+")).filter { it.length >= 3 }
            val grupoNorm = equip.armaGrupo?.let { CatalogFilters.normalizarBusca(it) } ?: ""
            val matchNome = nomeParts.any { part -> termosBrutos.any { it.contains(part) || part.contains(it) } }
            val matchGrupo = grupoNorm.isNotEmpty() && termosBrutos.any { it.contains(grupoNorm) || grupoNorm.contains(it) }
            if (matchNome || matchGrupo) equipamentosMatchados.add(equip)
        }

        if (temPossessivo && equipamentosMatchados.isEmpty()) {
            val armas = equipamentos.filter { it.armaTipoCombate != null }
            val armaduras = equipamentos.filter { it.armaduraRd != null }
            if (armas.isNotEmpty()) equipamentosMatchados.addAll(armas)
            else if (armaduras.isNotEmpty()) equipamentosMatchados.addAll(armaduras)
        }

        val contextoEquipamentos = if (equipamentosMatchados.isNotEmpty()) {
            android.util.Log.i("MestreIA_Planner", "INVENTÁRIO MATCH: ${equipamentosMatchados.map { it.nome }}")
            equipamentosMatchados.joinToString("\n") { equip ->
                buildString {
                    append("• ${equip.nome}")
                    equip.armaTipoCombate?.let { append(" | Tipo: $it") }
                    equip.armaDanoRaw?.let { append(" | Dano: $it") }
                    equip.armaGrupo?.let { append(" | Grupo: $it") }
                    equip.armaStMinimo?.let { append(" | ST mín: $it") }
                    equip.armaduraRd?.let { append(" | RD: $it") }
                    equip.armaduraLocal?.let { append(" | Local: $it") }
                }
            }
        } else ""

        val subQueriesStats = mutableListOf<String>()
        val tiposJaCobertos = mutableSetOf<String>()
        equipamentosMatchados.forEach { equip ->
            val nomeReal = equip.nome
            val grupo = equip.armaGrupo ?: ""
            when {
                equip.armaTipoCombate == "distancia" -> {
                    subQueriesStats.add("$nomeReal $grupo alcance dano tabela armas")
                    tiposJaCobertos.add("distancia")
                }
                equip.armaTipoCombate == "corpo_a_corpo" -> {
                    subQueriesStats.add("$nomeReal $grupo dano alcance tabela armas corpo")
                    tiposJaCobertos.add("corpo_a_corpo")
                }
                equip.armaduraRd != null -> {
                    subQueriesStats.add("$nomeReal RD armadura tabela protecao")
                    tiposJaCobertos.add("armadura")
                }
            }
        }

        if (equipamentosMatchados.isEmpty()) {
            termosBrutos.forEach { termo ->
                itemDetector[termo]?.let { query ->
                    if (!subQueriesStats.contains(query)) subQueriesStats.add(query)
                }
            }
        }

        val subQueriesTemáticas = gerarSubQueriesSemanticas(intencao, perguntaNorm)

        val livrosRelevantes = mutableSetOf<String>()
        val primNorm = intencao.entidadePrimaria
        val secNorm = intencao.entidadeSecundaria

        val temTiro = termosBrutos.any { it in grupoCombateDistancia }
        val temSubaquatico = termosBrutos.any { it in grupoAmbiente && it in setOf("agua", "piscina", "submerso", "aquatico", "subaquatico", "mergulho", "mar") }
        val temMagia = primNorm.split(" ").any { it in grupoMagia } || secNorm.split(" ").any { it in grupoMagia }
        val temArtesMarciais = termosBrutos.any { it in setOf("tecnica", "artes", "marciais", "luta", "presa", "agarrar", "derrubada") }

        when {
            temTiro && temSubaquatico -> livrosRelevantes.addAll(livrosPorCategoria["subaquatico"] ?: emptyList())
            temTiro -> livrosRelevantes.addAll(livrosPorCategoria["tiro"] ?: emptyList())
            temMagia -> livrosRelevantes.addAll(livrosPorCategoria["magia"] ?: emptyList())
            temArtesMarciais -> livrosRelevantes.addAll(livrosPorCategoria["artes_marciais"] ?: emptyList())
            else -> livrosRelevantes.addAll(livrosPorCategoria["pericia"] ?: emptyList())
        }
        livrosRelevantes.add("pt_modulo_basico")

        android.util.Log.i("MestreIA_Planner",
            "INTENÇÃO: primária='${intencao.entidadePrimaria}' secundária='${intencao.entidadeSecundaria}' relação=${intencao.relacao}")
        android.util.Log.i("MestreIA_Planner",
            "TERMOS PONDERADOS: ${termosPonderados.map { "${it.termo}(${it.peso})" }}")
        if (subQueriesTemáticas.isNotEmpty()) {
            android.util.Log.i("MestreIA_Planner", "MULTI-QUERY semânticas: $subQueriesTemáticas")
        }

        // Termos para o plano: núcleo primeiro (garante que FTS comece pelo sujeito real)
        val termosParaPlano = (termosOrdenados + termosExpandidos.toList())
            .distinct()
            .take(20)

        return PlanoDeBusca(
            termos = termosParaPlano,
            categorias = categorias,
            subQueriesStats = subQueriesStats,
            contextoEquipamentos = contextoEquipamentos,
            subQueriesTemáticas = subQueriesTemáticas,
            livrosRelevantes = livrosRelevantes.toList(),
            intencaoEstruturada = intencao
        )
    }

    fun analisarIntencao(pergunta: String): IntencaoBusca {
        val p = pergunta.lowercase()
        return when {
            Regex("(tabela|lista completa|todos os resultados|me (da|dá|mostre|traga)|complete|inteira|todinha)").containsMatchIn(p)
                -> IntencaoBusca.QUER_TABELA
            Regex("(como funciona|explica|o que (é|e) |como se usa|o que significa|qual é a|funcionalidade)").containsMatchIn(p)
                -> IntencaoBusca.QUER_EXPLICACAO
            Regex("(posso|é possível|funciona contra|se eu|consigo|permite|proibido|permite|conseguir|é permitido)").containsMatchIn(p)
                -> IntencaoBusca.QUER_REGRA
            Regex("(quanto|qual (o valor|a penalidade|o modificador)|calcul|formula|quanto de)").containsMatchIn(p)
                -> IntencaoBusca.QUER_CALCULO
            else -> IntencaoBusca.GERAL
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Extração de intenção estruturada (núcleo semântico da pergunta)
    // ─────────────────────────────────────────────────────────────────────────

    private fun extrairIntencaoEstruturada(perguntaOriginal: String, perguntaNorm: String): IntencaoEstruturada {
        val intencaoBusca = analisarIntencao(perguntaOriginal)

        // Tenta nome exato de vantagem/desvantagem GURPS — máxima prioridade
        val nomeVantagem = vantagensConhecidas.keys.firstOrNull { nome ->
            perguntaNorm.contains(nome, ignoreCase = true)
        }
        if (nomeVantagem != null) {
            val queryEspecifica = vantagensConhecidas[nomeVantagem]!!
            val termosPonderados = nomeVantagem.split(" ")
                .filter { it.length >= 2 }
                .map { TermoPonderado(it, 1.2) }
            android.util.Log.i("MestreIA_Planner", "VANTAGEM_NOMEADA detectada: '$nomeVantagem' → query='$queryEspecifica'")
            return IntencaoEstruturada(
                intencaoBusca = intencaoBusca,
                entidadePrimaria = nomeVantagem,
                entidadeSecundaria = "",
                relacao = RelacaoSemantica.FUNCIONAMENTO,
                termosPonderados = termosPonderados
            )
        }

        // Tenta padrão "X contra/em/sobre Y"
        val matchContra = padroesUsarContra.find(perguntaOriginal)
        if (matchContra != null) {
            val acao = CatalogFilters.normalizarBusca(matchContra.groupValues[1])
            val sujeito = CatalogFilters.normalizarBusca(matchContra.groupValues[2])
            val alvo = CatalogFilters.normalizarBusca(matchContra.groupValues[4])
            val termosPonderados = construirTermosPonderados(sujeito, alvo)
            return IntencaoEstruturada(
                intencaoBusca = intencaoBusca,
                entidadePrimaria = sujeito,
                entidadeSecundaria = alvo,
                relacao = RelacaoSemantica.USAR_CONTRA,
                termosPonderados = termosPonderados
            )
        }

        // Tenta padrão "penalidade de X em Y"
        val matchPenalidade = padroesPenalidadeEm.find(perguntaOriginal)
        if (matchPenalidade != null) {
            val acao = CatalogFilters.normalizarBusca(matchPenalidade.groupValues[3])
            val ambiente = CatalogFilters.normalizarBusca(matchPenalidade.groupValues[5])
            val termosPonderados = construirTermosPonderados(acao, ambiente)
            return IntencaoEstruturada(
                intencaoBusca = intencaoBusca,
                entidadePrimaria = acao,
                entidadeSecundaria = ambiente,
                relacao = RelacaoSemantica.PENALIDADE_EM,
                termosPonderados = termosPonderados
            )
        }

        // Tenta padrão "quanto de dano/penalidade X"
        val matchCalculo = padroesCalculoDe.find(perguntaOriginal)
        if (matchCalculo != null) {
            val sujeito = CatalogFilters.normalizarBusca(matchCalculo.groupValues[3])
            val termosPonderados = construirTermosPonderados(sujeito, "")
            return IntencaoEstruturada(
                intencaoBusca = IntencaoBusca.QUER_CALCULO,
                entidadePrimaria = sujeito,
                entidadeSecundaria = "",
                relacao = RelacaoSemantica.CALCULO_DE,
                termosPonderados = termosPonderados
            )
        }

        // Sem padrão relacional — inferência por grupo semântico
        val termosBrutos = perguntaNorm.split(Regex("\\s+"))
            .filter { it.length >= 3 && it !in stopWords }

        val (primaria, secundaria) = inferirEntidadesPorGrupo(termosBrutos)
        val termosPonderados = construirTermosPonderados(primaria, secundaria)

        val relacao = when (intencaoBusca) {
            IntencaoBusca.QUER_TABELA -> RelacaoSemantica.TABELA_DE
            IntencaoBusca.QUER_EXPLICACAO -> RelacaoSemantica.FUNCIONAMENTO
            IntencaoBusca.QUER_CALCULO -> RelacaoSemantica.CALCULO_DE
            else -> RelacaoSemantica.GENERICO
        }

        return IntencaoEstruturada(
            intencaoBusca = intencaoBusca,
            entidadePrimaria = primaria,
            entidadeSecundaria = secundaria,
            relacao = relacao,
            termosPonderados = termosPonderados
        )
    }

    /**
     * Infere entidade primária e secundária por grupo semântico.
     * Prioridade: magia > defesa > combate distância > combate CaC > ambiente > movimento > atributo.
     * A entidade que pertence ao grupo de maior prioridade é o sujeito (primária).
     */
    private fun inferirEntidadesPorGrupo(termos: List<String>): Pair<String, String> {
        data class Candidato(val termos: List<String>, val prioridade: Int)

        val grupos = listOf(
            Candidato(termos.filter { it in grupoMagia }, 6),
            Candidato(termos.filter { it in grupoDefesa }, 5),
            Candidato(termos.filter { it in grupoCombateDistancia }, 4),
            Candidato(termos.filter { it in grupoCombateCaC }, 3),
            Candidato(termos.filter { it in grupoMovimento }, 2),
            Candidato(termos.filter { it in grupoAmbiente }, 1),
            Candidato(termos.filter { it in grupoAtributo }, 0)
        ).filter { it.termos.isNotEmpty() }

        if (grupos.isEmpty()) {
            val primaria = termos.take(3).joinToString(" ")
            return Pair(primaria, "")
        }

        val sorted = grupos.sortedByDescending { it.prioridade }
        val primaria = sorted[0].termos.joinToString(" ")
        val secundaria = if (sorted.size > 1) sorted[1].termos.joinToString(" ") else ""
        return Pair(primaria, secundaria)
    }

    /**
     * Constrói lista de TermoPonderado com pesos explícitos.
     * Termos do núcleo (primária): peso 1.0
     * Termos do contexto (secundária): peso 0.6
     * Termos de expressões compostas reconhecidas: peso 1.2 (boost)
     */
    private fun construirTermosPonderados(primaria: String, secundaria: String): List<TermoPonderado> {
        val resultado = mutableListOf<TermoPonderado>()
        val perguntaCompleta = "$primaria $secundaria"

        // Detecta expressões compostas reconhecidas (boost 1.2)
        termosCombinados.forEach { (expr, _) ->
            if (perguntaCompleta.contains(expr)) {
                expr.split(" ").forEach { t ->
                    if (t.isNotBlank()) resultado.add(TermoPonderado(t, 1.2))
                }
            }
        }

        val jaAdicionados = resultado.map { it.termo }.toSet()

        primaria.split(Regex("\\s+"))
            .filter { it.length >= 2 && it !in stopWords && it !in jaAdicionados }
            .forEach { resultado.add(TermoPonderado(it, 1.0)) }

        secundaria.split(Regex("\\s+"))
            .filter { it.length >= 2 && it !in stopWords && it !in resultado.map { t -> t.termo } }
            .forEach { resultado.add(TermoPonderado(it, 0.6)) }

        return resultado
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sub-queries semânticas geradas a partir da IntencaoEstruturada
    // ─────────────────────────────────────────────────────────────────────────

    private fun gerarSubQueriesSemanticas(
        intencao: IntencaoEstruturada,
        perguntaNorm: String
    ): List<String> {
        val queries = mutableListOf<String>()
        val primaria = intencao.entidadePrimaria
        val secundaria = intencao.entidadeSecundaria

        // Expressões compostas reconhecidas → sub-query específica
        val perguntaCompleta = "$primaria $secundaria $perguntaNorm"
        termosCombinados.forEach { (expr, queryEspecifica) ->
            if (perguntaCompleta.contains(expr) && queryEspecifica.isNotBlank()) {
                queries.add(queryEspecifica)
            }
        }

        when (intencao.relacao) {
            RelacaoSemantica.USAR_CONTRA -> {
                // Pergunta sobre "X contra Y": busca como X funciona E como Y é afetado
                if (primaria.isNotBlank()) queries.add("$primaria regra mecanica como funciona")
                if (secundaria.isNotBlank()) queries.add("$secundaria defesa protecao resistir")
                if (primaria.isNotBlank() && secundaria.isNotBlank()) {
                    queries.add("$primaria $secundaria interacao regra")
                }
            }
            RelacaoSemantica.PENALIDADE_EM -> {
                if (primaria.isNotBlank()) queries.add("$primaria penalidade modificador")
                if (secundaria.isNotBlank()) queries.add("ambiente $secundaria penalidade combate")
                if (primaria.isNotBlank() && secundaria.isNotBlank()) {
                    queries.add("$primaria $secundaria regra penalidade")
                }
            }
            RelacaoSemantica.CALCULO_DE -> {
                if (primaria.isNotBlank()) {
                    queries.add("$primaria formula calculo tabela")
                    queries.add("$primaria dano bonus penalidade modificador")
                }
            }
            RelacaoSemantica.TABELA_DE -> {
                if (primaria.isNotBlank()) {
                    queries.add("$primaria tabela completa lista")
                    queries.add("$primaria stats valores numericos")
                }
            }
            RelacaoSemantica.FUNCIONAMENTO -> {
                // Se a intenção primária bate com uma vantagem conhecida, usa a sub-query específica
                val nomeVant = vantagensConhecidas.keys.firstOrNull { primaria.contains(it, ignoreCase = true) }
                if (nomeVant != null) {
                    queries.add(vantagensConhecidas[nomeVant]!!)
                    // Sub-query adicional para apanhar menções cruzadas (ex: p394 menciona a vantagem)
                    queries.add("$nomeVant bonus penalidade efeito")
                } else {
                    if (primaria.isNotBlank()) {
                        queries.add("$primaria regra mecanica como funciona")
                        queries.add("$primaria definicao uso quando aplicar")
                    }
                }
            }
            RelacaoSemantica.GENERICO -> {
                // Fallback: sub-queries por grupo semântico detectado
                val termosBrutos = perguntaNorm.split(Regex("\\s+")).filter { it.length >= 3 && it !in stopWords }
                val temAmbiente = termosBrutos.any { it in grupoAmbiente }
                val temDistancia = termosBrutos.any { it in grupoCombateDistancia }
                val temCaC = termosBrutos.any { it in grupoCombateCaC }
                val temMagia = termosBrutos.any { it in grupoMagia }
                val temDefesa = termosBrutos.any { it in grupoDefesa }

                when {
                    temDistancia && temAmbiente -> {
                        queries.add("tiro subaquatico arma fogo penalidade")
                        queries.add("penalidade ambiente liquido agua combate")
                        queries.add("alcance distancia arma fogo agua divisor")
                    }
                    temDistancia -> {
                        val arma = termosBrutos.firstOrNull { it in grupoCombateDistancia } ?: "arma"
                        queries.add("$arma alcance dano tabela armas distancia")
                        queries.add("tiro distancia penalidade modificador alvo")
                    }
                    temMagia && temDefesa -> {
                        queries.add("$primaria escola pre-requisito magia")
                        queries.add("defesa magica protecao feitico")
                    }
                    temMagia -> {
                        queries.add("$primaria escola pre-requisito custo energia")
                        queries.add("$primaria magia lancamento resistencia")
                    }
                    temCaC -> {
                        queries.add("combate ataque dano modificador manobra")
                        queries.add("defesa esquiva apara bloqueio penalidade")
                    }
                    temDefesa -> {
                        queries.add("$primaria defesa regra mecanica")
                        queries.add("defesa esquiva apara bloqueio escudo")
                    }
                    else -> {
                        if (primaria.isNotBlank()) queries.add("$primaria regra mecanica")
                    }
                }
            }
        }

        return queries
            .filter { it.isNotBlank() }
            .distinct()
            .take(4)
    }

    private fun inferirCategorias(termos: List<String>): List<String> {
        val cats = mutableSetOf<String>()
        termos.forEach { t ->
            when {
                t in grupoMagia || t in listOf("magia", "feitico", "escola", "conjuracao", "encantamento") -> cats.add("Magia")
                t in grupoCombateDistancia || t in grupoCombateCaC ||
                        t in listOf("arma", "espada", "faca", "pistola", "rifle", "arco", "dano", "ataque") -> cats.add("Equipamento")
                t in listOf("pericia", "habilidade", "nh", "nivel") -> cats.add("Perícia")
                t in listOf("vantagem", "desvantagem", "traco") -> cats.add("Traço")
                else -> cats.add("Regra")
            }
        }
        return cats.toList().ifEmpty { listOf("Regra") }
    }
}
