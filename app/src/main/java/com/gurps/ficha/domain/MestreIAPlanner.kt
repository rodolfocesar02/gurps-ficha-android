package com.gurps.ficha.domain

import com.gurps.ficha.domain.filters.CatalogFilters

/**
 * MestreIAPlanner - Extração local de termos técnicos de GURPS.
 * Sem chamada de rede. Instantâneo e sem ponto de falha.
 */
object MestreIAPlanner {

    data class PlanoDeBusca(
        val termos: List<String>,
        val categorias: List<String>,
        val subQueriesStats: List<String> = emptyList()
    )

    // Detector de itens com stats em tabela → gera query de pré-busca específica
    // Chave: palavra normalizada detectada na pergunta | Valor: query para buscar os stats
    private val itemDetector = mapOf(
        // ARMAS DE FOGO (regra: alcance ÷ 1.000 subaquático, precisam de ½D e Max da tabela)
        "revolver"       to "revolver pistola alcance dano tabela armas fogo",
        "pistola"        to "pistola revolver alcance dano tabela armas fogo",
        "rifle"          to "rifle carabina alcance dano tabela armas fogo",
        "espingarda"     to "espingarda shotgun alcance dano tabela armas fogo",
        "metralhadora"   to "metralhadora smg alcance dano cadencia tabela armas",
        "carabina"       to "carabina rifle alcance dano tabela armas fogo",
        "fuzil"          to "fuzil rifle alcance dano tabela armas fogo",
        "submetralhadora" to "submetralhadora smg alcance dano tabela armas",
        "garrucha"       to "garrucha pistola alcance dano tabela armas fogo",
        // ARMAS DE ARCO E BESTA (regra: metade do dano subaquático, precisam de ½D e Max)
        "arco"           to "arco flecha alcance dano tabela armas distancia",
        "besta"          to "besta virote alcance dano tabela armas distancia",
        "funda"          to "funda pedra alcance dano tabela armas arremesso",
        "zarabatana"     to "zarabatana dardo alcance dano tabela armas",
        "bodoque"        to "bodoque funda alcance dano tabela armas",
        // ARMAS DE ARREMESSO
        "shuriken"       to "shuriken estrela arremesso alcance dano tabela armas",
        "kunai"          to "kunai faca arremesso alcance dano tabela armas",
        "dardo"          to "dardo arremesso alcance dano tabela armas",
        // ARMAS CORPO A CORPO — espadas e facas (precisam de dano e alcance)
        "espada"         to "espada dano alcance tabela armas corpo combate",
        "sabre"          to "sabre espada dano tabela armas corpo",
        "florete"        to "florete estoque dano tabela armas corpo",
        "estoque"        to "estoque florete dano tabela armas corpo",
        "katana"         to "katana espada dano tabela armas corpo",
        "cimitarra"      to "cimitarra espada sabre dano tabela armas corpo",
        "cutelo"         to "cutelo machado dano tabela armas corpo",
        "faca"           to "faca adaga dano alcance tabela armas corpo",
        "adaga"          to "adaga faca dano tabela armas corpo",
        "punhal"         to "punhal adaga dano tabela armas corpo",
        // ARMAS CORPO A CORPO — contundentes
        "machado"        to "machado dano alcance tabela armas corpo",
        "clava"          to "clava porrete dano tabela armas corpo",
        "maca"           to "maca porrete dano tabela armas corpo",
        "porrete"        to "porrete clava dano tabela armas corpo",
        "martelo"        to "martelo guerra dano tabela armas corpo",
        "mangual"        to "mangual corrente dano tabela armas corpo",
        // ARMAS CORPO A CORPO — haste (têm alcance especial)
        "lanca"          to "lanca alabarda dano alcance haste tabela armas corpo",
        "alabarda"       to "alabarda lanca dano alcance haste tabela armas corpo",
        "naginata"       to "naginata lanca dano alcance haste tabela armas",
        "cajado"         to "cajado bordao dano alcance haste tabela armas",
        "bordao"         to "bordao cajado dano alcance tabela armas corpo",
        "tridente"       to "tridente lanca dano alcance haste tabela armas",
        "arpao"          to "arpao lanca arremesso dano alcance tabela armas",
        "chicote"        to "chicote dano alcance tabela armas corpo",
        // ARMADURAS (precisam de RD e peso para cálculo de carga/penalidade)
        "armadura"       to "armadura RD resistencia dano peso tabela armaduras",
        "colete"         to "colete armadura RD peso tabela armaduras",
        "elmo"           to "elmo capacete armadura RD tabela armaduras",
        "capacete"       to "capacete elmo armadura RD tabela armaduras",
        "cota"           to "cota malha armadura RD peso tabela armaduras",
        "lorica"         to "lorica segmentata armadura RD tabela armaduras",
        "brigantina"     to "brigantina armadura RD peso tabela armaduras",
        "placa"          to "placa armadura RD peso tabela armaduras",
        // ESCUDOS (precisam de DB e bônus de bloqueio)
        "broquel"        to "broquel escudo DB bloqueio tabela escudos",
        "rodela"         to "rodela escudo DB bloqueio tabela escudos"
    )

    private val stopWords = setOf(
        "como", "funciona", "regra", "regras", "quais", "preciso", "para", "sobre", "qual",
        "uma", "um", "com", "dos", "das", "pela", "pelo", "onde", "quando", "quem", "sao",
        "gurps", "edicao", "calculo", "calcular", "lista", "tabela", "de", "da", "do",
        "nos", "nas", "aos", "pra", "fale", "meu", "meus", "minha", "minhas",
        "queria", "quero", "saber", "ajuda", "tem", "existe", "existem", "por",
        "diga", "explique", "mostre", "entao", "voce", "isso", "esse", "essa",
        "que", "acontece", "num", "numa", "ser", "seja", "pode", "fazer",
        "nao", "mais", "muito", "cada", "deve", "seja", "sejam", "me"
    )

    // Dicionário técnico de GURPS para expansão de termos de busca
    private val dicionarioTecnico = mapOf(
        // Ferimento e sangue
        "sangramento" to listOf("hemorragia", "ferimento", "saude"),
        "hemorragia" to listOf("sangramento", "ferimento"),
        // Movimento e salto
        "pular" to listOf("salto", "distancia", "altura"),
        "salto" to listOf("pular", "distancia", "altura", "acrobacia"),
        // Colisão e queda
        "impacto" to listOf("colisao", "batida", "queda", "atropelamento"),
        "colisao" to listOf("impacto", "queda", "atropelamento", "encontro"),
        "queda" to listOf("impacto", "colisao", "altitude", "precipicio"),
        // Respiração e afogamento
        "asfixia" to listOf("afogamento", "sufocamento", "respiracao", "folego", "ar"),
        "afogamento" to listOf("asfixia", "agua", "submerso", "subaquatico"),
        "sufocamento" to listOf("asfixia", "afogamento", "respiracao", "ar"),
        // Atributos primários
        "st" to listOf("forca", "levantamento", "carga", "dano", "gdp", "geb"),
        "forca" to listOf("st", "levantamento", "carga", "muscular"),
        "dx" to listOf("destreza", "agilidade", "coordenacao"),
        "destreza" to listOf("dx", "agilidade", "coordenacao"),
        "iq" to listOf("inteligencia", "vontade", "percepcao", "raciocinio"),
        "ht" to listOf("vitalidade", "saude", "fadiga", "pf", "sobrevivencia"),
        // Movimento e velocidade
        "velocidade" to listOf("deslocamento", "esquiva", "movimento", "rapidez"),
        "deslocamento" to listOf("velocidade", "movimento", "passo", "corrida"),
        "movimento" to listOf("velocidade", "deslocamento", "passo", "corrida"),
        // Ambiente aquático (inclui "underwater" para encontrar chunks do Pyramid)
        "submerso" to listOf("agua", "aquatico", "mergulho", "piscina", "mar", "subaquatico", "underwater"),
        "aquatico" to listOf("submerso", "agua", "subaquatico", "underwater"),
        "piscina" to listOf("agua", "submerso", "aquatico", "subaquatico", "mergulho", "underwater"),
        "agua" to listOf("submerso", "aquatico", "subaquatico", "piscina", "mar", "underwater"),
        "subaquatico" to listOf("agua", "submerso", "aquatico", "piscina", "mergulho", "underwater"),
        "mergulho" to listOf("agua", "submerso", "aquatico", "subaquatico", "underwater"),
        // Defesas
        "esquiva" to listOf("defesa", "apara", "bloqueio", "evasao"),
        "apara" to listOf("defesa", "esquiva", "bloqueio", "escudo"),
        "bloqueio" to listOf("defesa", "esquiva", "apara", "escudo"),
        "defesa" to listOf("esquiva", "apara", "bloqueio", "protecao"),
        "escudo" to listOf("bloqueio", "defesa", "apara"),
        // Magia e encantamento
        "magia" to listOf("feitico", "encantamento", "conjuracao", "escola"),
        "feitico" to listOf("magia", "encantamento", "conjuracao"),
        "encantamento" to listOf("magia", "feitico", "magia"),
        "escola" to listOf("magia", "categoria", "tipo"),
        // Perícias e habilidades
        "pericia" to listOf("habilidade", "nivel", "nh", "aptidao"),
        "habilidade" to listOf("pericia", "nivel", "nh"),
        // Combate e ataque
        "ataque" to listOf("dano", "acerto", "combate", "ofensiva"),
        "combate" to listOf("ataque", "defesa", "luta", "batalha"),
        // Combate à distância
        "tiro" to listOf("disparo", "arma", "fogo", "projetil", "atirar", "arremesso"),
        "atirar" to listOf("tiro", "disparo", "fogo", "acertar"),
        "disparo" to listOf("tiro", "atirar", "fogo", "projetil"),
        "alcance" to listOf("distancia", "range", "metro", "faixa", "distante"),
        "pistola" to listOf("revolver", "arma", "fogo", "disparo", "tiro"),
        "revolver" to listOf("pistola", "arma", "fogo", "disparo", "tiro"),
        "rifle" to listOf("arma", "fogo", "disparo", "tiro", "longa distancia"),
        "espingarda" to listOf("arma", "fogo", "disparo", "tiro"),
        // Dano e vida
        "dano" to listOf("ataque", "ferimento", "pv", "lesao"),
        "ferimento" to listOf("dano", "lesao", "pv", "sangramento"),
        "pv" to listOf("vida", "saude", "ferimento", "pontos de vida"),
        "pf" to listOf("fadiga", "cansaco", "energia", "pontos de fadiga"),
        "fadiga" to listOf("pf", "cansaco", "energia", "exaustao"),
        // Penalidades e modificadores
        "penalidade" to listOf("modificador", "bonus", "malus", "reducao", "ajuste"),
        "modificador" to listOf("penalidade", "bonus", "ajuste", "fator"),
        "redutor" to listOf("penalidade", "modificador", "subtracao"),
        // Cura e medicina
        "cura" to listOf("recuperacao", "primeiros socorros", "medicina", "ferimento"),
        "recuperacao" to listOf("cura", "descanso", "medicina"),
        "medicina" to listOf("cura", "primeiros socorros", "recuperacao"),
        // Visibilidade e ambiente
        "escuridao" to listOf("visibilidade", "noite", "penalidade", "iluminacao"),
        "visibilidade" to listOf("escuridao", "iluminacao", "claridade", "neblina"),
        // Armadura e proteção
        "armadura" to listOf("rd", "protecao", "cobertura", "blindagem"),
        "rd" to listOf("armadura", "resistencia", "protecao", "reducao de dano")
    )

    fun planejarBusca(pergunta: String): PlanoDeBusca {
        val termosBrutos = CatalogFilters.normalizarBusca(pergunta)
            .split(Regex("\\s+"))
            .filter { it.length >= 2 && it !in stopWords }

        val termosExpandidos = mutableSetOf<String>()
        termosExpandidos.addAll(termosBrutos)

        termosBrutos.forEach { termo ->
            dicionarioTecnico[termo]?.let { termosExpandidos.addAll(it) }
            dicionarioTecnico.entries.forEach { (chave, sinonimos) ->
                if (termo.length > 4 && (termo.contains(chave) || chave.contains(termo))) {
                    termosExpandidos.addAll(sinonimos)
                }
            }
        }

        val categorias = inferirCategorias(termosBrutos)

        // Detecta itens com stats em tabela e gera sub-queries de pré-busca
        val subQueriesStats = mutableListOf<String>()
        termosBrutos.forEach { termo ->
            itemDetector.entries.forEach { (chave, query) ->
                if (termo == chave || (termo.length > 3 && (termo.contains(chave) || chave.contains(termo)))) {
                    if (!subQueriesStats.contains(query)) subQueriesStats.add(query)
                }
            }
        }
        if (subQueriesStats.isNotEmpty()) {
            android.util.Log.i("MestreIA_Planner", "PRÉ-STATS detectados: $subQueriesStats")
        }

        android.util.Log.i("MestreIA_Planner", "TERMOS EXTRAÍDOS (local): $termosExpandidos | Categorias: $categorias")

        return PlanoDeBusca(termosExpandidos.toList().take(15), categorias, subQueriesStats)
    }

    private fun inferirCategorias(termos: List<String>): List<String> {
        val cats = mutableSetOf<String>()
        termos.forEach { t ->
            when {
                t in listOf("magia", "feitico", "escola", "conjuracao", "encantamento") -> cats.add("Magia")
                t in listOf("arma", "espada", "faca", "pistola", "rifle", "arco", "dano", "ataque") -> cats.add("Equipamento")
                t in listOf("pericia", "habilidade", "nh", "nivel") -> cats.add("Perícia")
                t in listOf("vantagem", "desvantagem", "traco") -> cats.add("Traço")
                else -> cats.add("Regra")
            }
        }
        return cats.toList().ifEmpty { listOf("Regra") }
    }
}
