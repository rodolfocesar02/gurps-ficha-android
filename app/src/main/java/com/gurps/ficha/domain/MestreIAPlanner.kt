package com.gurps.ficha.domain

import com.gurps.ficha.domain.filters.CatalogFilters
import com.gurps.ficha.model.Equipamento

/**
 * MestreIAPlanner - Extração local de termos técnicos de GURPS.
 * Sem chamada de rede. Instantâneo e sem ponto de falha.
 */
object MestreIAPlanner {

    data class PlanoDeBusca(
        val termos: List<String>,
        val categorias: List<String>,
        val subQueriesStats: List<String> = emptyList(),
        val contextoEquipamentos: String = "",
        val subQueriesTemáticas: List<String> = emptyList(),
        val livrosRelevantes: List<String> = emptyList()  // source_ids a priorizar na busca FTS
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

    // Mapa categoria → source_ids relevantes para filtro de busca por livro.
    // Mantido aqui para que planejarBusca() possa expor os livros relevantes ao GraphEngine.
    val livrosPorCategoria: Map<String, List<String>> = mapOf(
        "tiro"         to listOf("pt_modulo_basico", "pt_gun_fu", "pt_pyramid_26_underwater"),
        "arma_fogo"    to listOf("pt_modulo_basico", "pt_gun_fu"),
        "subaquatico"  to listOf("pt_modulo_basico", "pt_pyramid_26_underwater"),
        "magia"        to listOf("pt_modulo_basico", "pt_magia"),
        "combate"      to listOf("pt_modulo_basico", "pt_artes_marciais", "pt_gun_fu"),
        "artes_marciais" to listOf("pt_modulo_basico", "pt_artes_marciais"),
        "pericia"      to listOf("pt_modulo_basico"),
        "raca"         to listOf("pt_modulo_basico"),
        "equipamento"  to listOf("pt_modulo_basico", "pt_gun_fu", "pt_artes_marciais")
    )

    // Dicionário técnico de GURPS — 90+ entradas cobrindo os grandes temas dos livros disponíveis.
    private val dicionarioTecnico = mapOf(
        // ── FERIMENTO E SANGUE ──────────────────────────────────────────────
        "sangramento"   to listOf("hemorragia", "ferimento", "saude", "pv"),
        "hemorragia"    to listOf("sangramento", "ferimento", "pv"),
        "incapacitado"  to listOf("ferimento", "pv", "morte", "desmaiado", "inconsciente"),
        "inconsciente"  to listOf("desmaiado", "incapacitado", "pv", "ferimento"),
        "morte"         to listOf("morto", "incapacitado", "pv", "fatal"),
        "morto"         to listOf("morte", "pv", "fatal"),
        // ── MOVIMENTO E SALTO ───────────────────────────────────────────────
        "pular"         to listOf("salto", "distancia", "altura", "acrobacia"),
        "salto"         to listOf("pular", "distancia", "altura", "acrobacia"),
        "correr"        to listOf("corrida", "deslocamento", "velocidade", "sprint"),
        "corrida"       to listOf("correr", "deslocamento", "velocidade"),
        "nadar"         to listOf("natacao", "agua", "deslocamento", "subaquatico"),
        "natacao"       to listOf("nadar", "agua", "subaquatico", "pericia"),
        "trepar"        to listOf("escalar", "subir", "altura", "pericia"),
        "escalar"       to listOf("trepar", "subir", "altura", "acrobacia"),
        "rastejar"      to listOf("agachar", "pronar", "movimento", "penalidade"),
        "voar"          to listOf("voo", "altitude", "asa", "deslocamento"),
        "voo"           to listOf("voar", "altitude", "asa", "deslocamento"),
        // ── COLISÃO, QUEDA E IMPACTO ────────────────────────────────────────
        "impacto"       to listOf("colisao", "batida", "queda", "atropelamento", "dano"),
        "colisao"       to listOf("impacto", "queda", "atropelamento", "encontro", "dano"),
        "queda"         to listOf("impacto", "colisao", "altitude", "precipicio", "dano"),
        "atropelamento" to listOf("colisao", "impacto", "veiculo", "dano"),
        "altitude"      to listOf("queda", "altura", "precipicio"),
        // ── RESPIRAÇÃO, FOGO E AMBIENTE ─────────────────────────────────────
        "asfixia"       to listOf("afogamento", "sufocamento", "respiracao", "folego", "ar"),
        "afogamento"    to listOf("asfixia", "agua", "submerso", "subaquatico", "sufocamento"),
        "sufocamento"   to listOf("asfixia", "afogamento", "respiracao", "ar", "folego"),
        "fogo"          to listOf("queimadura", "chama", "calor", "dano", "incendio"),
        "queimadura"    to listOf("fogo", "calor", "dano", "chama"),
        "frio"          to listOf("congelamento", "hipotermia", "temperatura", "penalidade"),
        "calor"         to listOf("fogo", "temperatura", "dano", "exaustao"),
        "veneno"        to listOf("toxina", "envenenamento", "efeito", "resistencia"),
        "doenca"        to listOf("infeccao", "enfermidade", "resistencia", "ht"),
        "radiacao"      to listOf("radioatividade", "dano", "rad", "envenenamento"),
        // ── ATRIBUTOS PRIMÁRIOS ─────────────────────────────────────────────
        "st"            to listOf("forca", "levantamento", "carga", "dano", "gdp", "geb"),
        "forca"         to listOf("st", "levantamento", "carga", "muscular", "dano"),
        "dx"            to listOf("destreza", "agilidade", "coordenacao", "pericia"),
        "destreza"      to listOf("dx", "agilidade", "coordenacao"),
        "iq"            to listOf("inteligencia", "vontade", "percepcao", "raciocinio"),
        "inteligencia"  to listOf("iq", "vontade", "percepcao", "raciocinio"),
        "ht"            to listOf("vitalidade", "saude", "fadiga", "pf", "sobrevivencia"),
        "vitalidade"    to listOf("ht", "saude", "pf", "resistencia"),
        // ── METACARACTERÍSTICAS ─────────────────────────────────────────────
        "vontade"       to listOf("iq", "resistencia", "mental", "medo"),
        "percepcao"     to listOf("iq", "sentido", "visao", "audicao", "alerta"),
        "velocidade"    to listOf("deslocamento", "esquiva", "movimento", "rapidez"),
        "deslocamento"  to listOf("velocidade", "movimento", "passo", "corrida"),
        "movimento"     to listOf("velocidade", "deslocamento", "passo", "corrida"),
        // ── AMBIENTE AQUÁTICO ───────────────────────────────────────────────
        "submerso"      to listOf("agua", "aquatico", "mergulho", "piscina", "mar", "subaquatico", "underwater"),
        "aquatico"      to listOf("submerso", "agua", "subaquatico", "underwater"),
        "piscina"       to listOf("agua", "submerso", "aquatico", "subaquatico", "mergulho", "underwater"),
        "agua"          to listOf("submerso", "aquatico", "subaquatico", "piscina", "mar", "underwater"),
        "subaquatico"   to listOf("agua", "submerso", "aquatico", "piscina", "mergulho", "underwater"),
        "mergulho"      to listOf("agua", "submerso", "aquatico", "subaquatico", "underwater"),
        "mar"           to listOf("agua", "submerso", "aquatico", "oceano"),
        // ── DEFESAS ─────────────────────────────────────────────────────────
        "esquiva"       to listOf("defesa", "apara", "bloqueio", "evasao", "dx"),
        "apara"         to listOf("defesa", "esquiva", "bloqueio", "escudo", "arma"),
        "bloqueio"      to listOf("defesa", "esquiva", "apara", "escudo"),
        "defesa"        to listOf("esquiva", "apara", "bloqueio", "protecao"),
        "escudo"        to listOf("bloqueio", "defesa", "apara", "db"),
        "db"            to listOf("escudo", "bloqueio", "bonus defesa"),
        // ── MAGIA ───────────────────────────────────────────────────────────
        "magia"         to listOf("feitico", "encantamento", "conjuracao", "escola", "energia"),
        "feitico"       to listOf("magia", "encantamento", "conjuracao", "escola"),
        "encantamento"  to listOf("magia", "feitico", "conjuracao"),
        "escola"        to listOf("magia", "categoria", "tipo", "feitico"),
        "energia"       to listOf("magia", "custo", "pf", "mana"),
        "mana"          to listOf("energia", "magia", "pf", "custo"),
        "prereq"        to listOf("prerequisito", "requisito", "magia", "pericia"),
        "prerequisito"  to listOf("prereq", "requisito", "magia", "habilidade"),
        // ── PERÍCIAS E HABILIDADES ──────────────────────────────────────────
        "pericia"       to listOf("habilidade", "nivel", "nh", "aptidao", "dx", "iq"),
        "habilidade"    to listOf("pericia", "nivel", "nh", "aptidao"),
        "nh"            to listOf("nivel", "pericia", "habilidade", "pontos"),
        "nivel"         to listOf("nh", "pericia", "habilidade"),
        "acrobacia"     to listOf("salto", "equilibrio", "dx", "pericia"),
        "furtividade"   to listOf("esconder", "sorrateiro", "dx", "pericia"),
        "persuasao"     to listOf("conversa", "negociacao", "iq", "pericia"),
        "intimidacao"   to listOf("medo", "ameaca", "iq", "pericia"),
        "primeiros socorros" to listOf("cura", "medicina", "ferimento", "pv"),
        // ── COMBATE GERAL ───────────────────────────────────────────────────
        "ataque"        to listOf("dano", "acerto", "combate", "ofensiva", "manobra"),
        "combate"       to listOf("ataque", "defesa", "luta", "batalha", "manobra"),
        "manobra"       to listOf("combate", "ataque", "movimento", "turno"),
        "turno"         to listOf("manobra", "combate", "acao", "tempo"),
        "critico"       to listOf("acerto critico", "falha critica", "dado", "3d6"),
        "acerto"        to listOf("ataque", "dado", "nh", "sucesso"),
        "falha"         to listOf("erro", "dado", "nh", "penalidade"),
        // ── COMBATE À DISTÂNCIA ─────────────────────────────────────────────
        "tiro"          to listOf("disparo", "arma", "fogo", "projetil", "atirar", "arremesso"),
        "atirar"        to listOf("tiro", "disparo", "fogo", "acertar", "alcance"),
        "disparo"       to listOf("tiro", "atirar", "fogo", "projetil", "municao"),
        "alcance"       to listOf("distancia", "range", "metro", "faixa", "distante", "meia distancia"),
        "municao"       to listOf("bala", "cartucho", "disparo", "capacidade"),
        "cadencia"      to listOf("tiro", "disparo", "municao", "rajada"),
        "rajada"        to listOf("cadencia", "rafaga", "municao", "tiro"),
        "mira"          to listOf("tiro", "alcance", "penalidade", "alvo"),
        "pistola"       to listOf("revolver", "arma", "fogo", "disparo", "tiro"),
        "revolver"      to listOf("pistola", "arma", "fogo", "disparo", "tiro"),
        "rifle"         to listOf("arma", "fogo", "disparo", "tiro", "longa distancia"),
        "espingarda"    to listOf("arma", "fogo", "disparo", "tiro", "chumbinho"),
        "metralhadora"  to listOf("arma", "fogo", "rajada", "cadencia", "municao"),
        "submetralhadora" to listOf("arma", "fogo", "rajada", "pistola"),
        "arco"          to listOf("flecha", "arremesso", "alcance", "dano"),
        "besta"         to listOf("virote", "arremesso", "alcance", "dano"),
        "arremesso"     to listOf("arco", "funda", "lancamento", "alcance", "tiro"),
        // ── ARTES MARCIAIS ──────────────────────────────────────────────────
        "artes marciais" to listOf("luta", "marcial", "combate", "tecnica"),
        "tecnica"       to listOf("pericia", "combate", "manobra", "nh"),
        "presa"         to listOf("agarrar", "imobilizar", "luta", "combate"),
        "agarrar"       to listOf("presa", "imobilizar", "luta", "st"),
        "derrubada"     to listOf("jogada", "queda", "combate", "dx"),
        "chute"         to listOf("perna", "dano", "combate", "dx"),
        "soco"          to listOf("pugno", "dano", "combate", "st"),
        // ── DANO E VIDA ─────────────────────────────────────────────────────
        "dano"          to listOf("ataque", "ferimento", "pv", "lesao", "gdp", "geb"),
        "ferimento"     to listOf("dano", "lesao", "pv", "sangramento", "cura"),
        "pv"            to listOf("vida", "saude", "ferimento", "pontos de vida"),
        "pf"            to listOf("fadiga", "cansaco", "energia", "pontos de fadiga"),
        "fadiga"        to listOf("pf", "cansaco", "energia", "exaustao", "ht"),
        "gdp"           to listOf("dado de penetracao", "dano", "st", "arma"),
        "geb"           to listOf("dado de esmagar", "dano", "st", "arma"),
        // ── PENALIDADES E MODIFICADORES ─────────────────────────────────────
        "penalidade"    to listOf("modificador", "bonus", "malus", "reducao", "ajuste"),
        "modificador"   to listOf("penalidade", "bonus", "ajuste", "fator"),
        "redutor"       to listOf("penalidade", "modificador", "subtracao"),
        "bonus"         to listOf("modificador", "penalidade", "ajuste", "adicional"),
        // ── CURA E MEDICINA ─────────────────────────────────────────────────
        "cura"          to listOf("recuperacao", "primeiros socorros", "medicina", "ferimento", "pv"),
        "recuperacao"   to listOf("cura", "descanso", "medicina", "pv"),
        "medicina"      to listOf("cura", "primeiros socorros", "recuperacao", "iq"),
        "descanso"      to listOf("recuperacao", "cura", "pv", "pf"),
        // ── VISIBILIDADE E AMBIENTE ─────────────────────────────────────────
        "escuridao"     to listOf("visibilidade", "noite", "penalidade", "iluminacao"),
        "visibilidade"  to listOf("escuridao", "iluminacao", "claridade", "neblina"),
        "neblina"       to listOf("visibilidade", "penalidade", "fumaca"),
        "fumaca"        to listOf("visibilidade", "neblina", "penalidade"),
        "iluminacao"    to listOf("visibilidade", "escuridao", "luz", "lanterna"),
        // ── ARMADURA E PROTEÇÃO ─────────────────────────────────────────────
        "armadura"      to listOf("rd", "protecao", "cobertura", "blindagem", "peso"),
        "rd"            to listOf("armadura", "resistencia", "protecao", "reducao de dano"),
        "cobertura"     to listOf("armadura", "rd", "local", "protecao"),
        // ── CAVALARIA E MONTARIA ─────────────────────────────────────────────
        "cavalo"        to listOf("montaria", "cavaleiro", "cavalgar", "animal"),
        "montaria"      to listOf("cavalo", "cavaleiro", "cavalgar", "animal"),
        "cavalgar"      to listOf("cavalo", "montaria", "pericia", "dx"),
        // ── VEÍCULOS ────────────────────────────────────────────────────────
        "veiculo"       to listOf("carro", "moto", "nave", "pilotagem", "velocidade"),
        "pilotagem"     to listOf("veiculo", "pericia", "dx", "iq"),
        // ── PSICOLOGIA E MENTAL ──────────────────────────────────────────────
        "medo"          to listOf("fobia", "terror", "vontade", "panique"),
        "fobia"         to listOf("medo", "terror", "vontade", "desvantagem"),
        "panico"        to listOf("medo", "fobia", "terror", "vontade"),
        "sansidade"     to listOf("sanidade", "mental", "horror", "vontade"),
        "sanidade"      to listOf("sansidade", "mental", "horror", "vontade"),
        // ── SOCIAL E REPUTAÇÃO ───────────────────────────────────────────────
        "reputacao"     to listOf("status", "social", "reacao", "fama"),
        "status"        to listOf("reputacao", "social", "riqueza", "classe"),
        "reacao"        to listOf("npc", "social", "reputacao", "carisma"),
        "carisma"       to listOf("persuasao", "reacao", "social", "iq"),
        // ── ECONOMIA E EQUIPAMENTO ───────────────────────────────────────────
        "custo"         to listOf("preco", "dinheiro", "compra", "equipamento"),
        "peso"          to listOf("carga", "encumbrance", "st", "penalidade"),
        "carga"         to listOf("peso", "encumbrance", "st", "velocidade"),
        "encumbrance"   to listOf("carga", "peso", "penalidade", "velocidade")
    )

    fun planejarBusca(pergunta: String, equipamentos: List<Equipamento> = emptyList()): PlanoDeBusca {
        val perguntaNorm = CatalogFilters.normalizarBusca(pergunta)
        val termosBrutos = perguntaNorm
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

        // LOTE 130: Cruzamento com inventário do personagem
        val temPossessivo = listOf("meu", "minha", "meus", "minhas").any { perguntaNorm.contains(it) }
        val equipamentosMatchados = mutableListOf<Equipamento>()

        for (equip in equipamentos) {
            val nomeNorm = CatalogFilters.normalizarBusca(equip.nome)
            val nomeParts = nomeNorm.split(Regex("\\s+")).filter { it.length >= 3 }
            val grupoNorm = equip.armaGrupo?.let { CatalogFilters.normalizarBusca(it) } ?: ""

            val matchNome = nomeParts.any { part -> termosBrutos.any { it.contains(part) || part.contains(it) } }
            val matchGrupo = grupoNorm.isNotEmpty() && termosBrutos.any { it.contains(grupoNorm) || grupoNorm.contains(it) }

            if (matchNome || matchGrupo) {
                equipamentosMatchados.add(equip)
            }
        }

        // Possessivo + nenhum match por nome → inclui TODAS as armas/armaduras para o jogador decidir
        if (temPossessivo && equipamentosMatchados.isEmpty()) {
            val armas = equipamentos.filter { it.armaTipoCombate != null }
            val armaduras = equipamentos.filter { it.armaduraRd != null }
            if (armas.isNotEmpty()) equipamentosMatchados.addAll(armas)
            else if (armaduras.isNotEmpty()) equipamentosMatchados.addAll(armaduras)
        }

        // Formata contexto legível dos equipamentos matchados
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

        // subQueriesStats: itens do inventário têm prioridade (query específica com nome real)
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

        // Fallback: itemDetector para itens que não vieram do inventário
        if (equipamentosMatchados.isEmpty()) {
            termosBrutos.forEach { termo ->
                itemDetector.entries.forEach { (chave, query) ->
                    if (termo == chave || (termo.length > 3 && termo.contains(chave))) {
                        if (!subQueriesStats.contains(query)) subQueriesStats.add(query)
                    }
                }
            }
        }

        if (subQueriesStats.isNotEmpty()) {
            android.util.Log.i("MestreIA_Planner", "PRÉ-STATS: $subQueriesStats")
        }

        val subQueriesTemáticas = gerarSubQueriesTemáticas(perguntaNorm, termosBrutos, termosExpandidos.toList())

        // Monta lista de source_ids relevantes baseada nos detectores de cenário
        val livrosRelevantes = mutableSetOf<String>()
        val temTiro = termosBrutos.any { it in setOf("tiro", "atirar", "disparo", "pistola", "revolver", "rifle", "espingarda", "arco", "besta") }
        val temSubaquatico = termosBrutos.any { it in setOf("agua", "piscina", "submerso", "aquatico", "subaquatico", "mergulho", "mar") }
        val temMagia = termosBrutos.any { it in setOf("magia", "feitico", "encantamento", "escola", "mana", "energia") }
        val temArtesMarciais = termosBrutos.any { it in setOf("tecnica", "artes", "marciais", "luta", "presa", "agarrar", "derrubada") }

        when {
            temTiro && temSubaquatico -> livrosRelevantes.addAll(livrosPorCategoria["subaquatico"] ?: emptyList())
            temTiro -> livrosRelevantes.addAll(livrosPorCategoria["tiro"] ?: emptyList())
            temMagia -> livrosRelevantes.addAll(livrosPorCategoria["magia"] ?: emptyList())
            temArtesMarciais -> livrosRelevantes.addAll(livrosPorCategoria["artes_marciais"] ?: emptyList())
            else -> livrosRelevantes.addAll(livrosPorCategoria["pericia"] ?: emptyList())
        }
        // Módulo básico sempre incluído — é a referência principal de regras
        livrosRelevantes.add("pt_modulo_basico")

        android.util.Log.i("MestreIA_Planner", "TERMOS EXTRAÍDOS (local): $termosExpandidos | Categorias: $categorias | Livros: $livrosRelevantes")
        if (subQueriesTemáticas.isNotEmpty()) {
            android.util.Log.i("MestreIA_Planner", "MULTI-QUERY temáticas: $subQueriesTemáticas")
        }

        return PlanoDeBusca(termosExpandidos.toList().take(15), categorias, subQueriesStats, contextoEquipamentos, subQueriesTemáticas, livrosRelevantes.toList())
    }

    /**
     * Gera 2-4 sub-queries temáticas cobrindo ângulos diferentes da pergunta.
     * Ex: "atirar revólver numa piscina" → [cenário direto, penalidade/ambiente, mecânica afetada, tipo de arma]
     * Chunks que aparecem em múltiplas sub-queries recebem bonus de relevância no scoring.
     */
    private fun gerarSubQueriesTemáticas(
        perguntaNorm: String,
        termosBrutos: List<String>,
        termosExpandidos: List<String>
    ): List<String> {
        val queries = mutableListOf<String>()

        // Detectores de cenário por padrões de termos-chave
        val temAmbienteAquático = termosBrutos.any { it in setOf("agua", "piscina", "submerso", "aquatico", "subaquatico", "mergulho", "mar", "rio", "lago") }
        val temTiroDistância = termosBrutos.any { it in setOf("tiro", "atirar", "disparo", "arco", "besta", "flecha", "revolver", "pistola", "rifle", "espingarda") }
        val temCombate = termosBrutos.any { it in setOf("combate", "ataque", "dano", "acertar", "lutar", "espada", "faca", "maca", "lanca") }
        val temMovimento = termosBrutos.any { it in setOf("correr", "mover", "deslocamento", "velocidade", "pular", "salto", "nadar") }
        val temVisibilidade = termosBrutos.any { it in setOf("escuridao", "visibilidade", "neblina", "fogo", "fumaca", "noite", "luz") }
        val temCura = termosBrutos.any { it in setOf("cura", "curar", "medicina", "ferimento", "recuperar", "primeiros") }
        val temMagia = termosBrutos.any { it in setOf("magia", "feitico", "conjurar", "escola", "encantamento") }
        val temCarga = termosBrutos.any { it in setOf("carga", "peso", "encumbrance", "carregando", "levantamento") }
        val temArmadura = termosBrutos.any { it in setOf("armadura", "rd", "colete", "elmo", "placa", "malha") }
        val temPericia = termosBrutos.any { it in setOf("pericia", "habilidade", "nh", "nivel", "aptidao") }

        when {
            // CENÁRIO: tiro aquático — o caso clássico da falha
            temTiroDistância && temAmbienteAquático -> {
                queries.add("tiro subaquatico arma fogo penalidade")
                queries.add("penalidade ambiente liquido agua combate")
                queries.add("alcance distancia arma fogo agua divisor")
                queries.add("underwater ranged weapon penalty")
            }
            // CENÁRIO: combate aquático
            temCombate && temAmbienteAquático -> {
                queries.add("combate subaquatico penalidade agua")
                queries.add("lutar agua mergulho penalidade")
                queries.add("movimento nadar combate submerso")
            }
            // CENÁRIO: tiro com penalidade de visibilidade/escuridão
            temTiroDistância && temVisibilidade -> {
                queries.add("tiro escuridao penalidade visibilidade")
                queries.add("modificador tiro ambiente penalidade")
                queries.add("alcance ataque distancia visibilidade")
                queries.add("penalidade mira alvo encoberto")
            }
            // CENÁRIO: tiro puro (sem ambiente especial)
            temTiroDistância -> {
                val armaTipo = termosBrutos.firstOrNull { it in setOf("revolver", "pistola", "rifle", "arco", "besta", "espingarda") } ?: "arma"
                queries.add("$armaTipo alcance dano tabela armas distancia")
                queries.add("tiro distância penalidade modificador alvo")
                queries.add("ataque distancia acerto alcance")
            }
            // CENÁRIO: movimento aquático
            temMovimento && temAmbienteAquático -> {
                queries.add("nadar natacao movimento agua penalidade")
                queries.add("deslocamento subaquatico metro turno")
                queries.add("mergulho pericia natacao")
            }
            // CENÁRIO: combate corpo a corpo
            temCombate && !temTiroDistância -> {
                queries.add("combate ataque dano modificador")
                queries.add("manobra combate corpo a corpo")
                queries.add("defesa esquiva apara bloqueio penalidade")
            }
            // CENÁRIO: carga/peso
            temCarga -> {
                queries.add("carga peso encumbrance penalidade")
                queries.add("levantamento st forca peso maximo")
                queries.add("carga encumbrance velocidade penalidade")
            }
            // CENÁRIO: armadura
            temArmadura -> {
                queries.add("armadura rd resistencia dano tabela")
                queries.add("armadura peso penalidade destreza")
                queries.add("cobertura local protecao armadura")
            }
            // CENÁRIO: cura/medicina
            temCura -> {
                queries.add("cura primeiros socorros medicina ferimento")
                queries.add("recuperacao pv ferimento descanso")
                queries.add("medicina pericia cura penalidade")
            }
            // CENÁRIO: magia
            temMagia -> {
                val termoMagia = termosBrutos.firstOrNull { it !in stopWords } ?: "feitico"
                queries.add("$termoMagia magia escola pre-requisito")
                queries.add("custo energia magia lancamento")
                queries.add("resistir magia feitico defesa")
            }
            // CENÁRIO: perícia
            temPericia -> {
                val termoP = termosBrutos.firstOrNull { it !in stopWords && it !in setOf("pericia", "habilidade") } ?: ""
                if (termoP.isNotBlank()) queries.add("$termoP pericia nivel dificuldade atributo")
                queries.add("pericia nivel dificuldade calculo custo")
                queries.add("especialização pericia bonus")
            }
        }

        // Remove queries que são idênticas à query principal já planejada
        val termosPrincipaisNorm = termosBrutos.take(5).joinToString(" ")
        return queries.filter { q ->
            val qNorm = CatalogFilters.normalizarBusca(q)
            qNorm != termosPrincipaisNorm && q.isNotBlank()
        }.distinct().take(4)
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
