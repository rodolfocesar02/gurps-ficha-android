package com.gurps.ficha.domain

import com.gurps.ficha.domain.filters.CatalogFilters

/**
 * MestreIAPlanner - Extração local de termos técnicos de GURPS.
 * Sem chamada de rede. Instantâneo e sem ponto de falha.
 */
object MestreIAPlanner {

    data class PlanoDeBusca(
        val termos: List<String>,
        val categorias: List<String>
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

        android.util.Log.i("MestreIA_Planner", "TERMOS EXTRAÍDOS (local): $termosExpandidos | Categorias: $categorias")

        return PlanoDeBusca(termosExpandidos.toList().take(15), categorias)
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
