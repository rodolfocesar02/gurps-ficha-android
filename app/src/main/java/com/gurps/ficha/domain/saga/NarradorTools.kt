package com.gurps.ficha.domain.saga

import org.json.JSONArray
import org.json.JSONObject

/**
 * Lote 353 (Saga A4): contrato COMPLETO das ferramentas do Narrador do modo Saga.
 * Apenas os SCHEMAS (Gemini nativo e OpenAI/DeepSeek) — a execução está no
 * NarradorToolExecutor; a persona/prompt chega no A5.
 *
 * Divergência documentada: o plano referencia o §3.2 do PLANO_GURPS_SAGA_v2, que não
 * existe no repositório — os parâmetros foram projetados a partir dos nomes/objetivos
 * do PLANO_SAGA_CLAUDE_CODE.md. Descrições CATEGORIAIS, zero exemplos (lição do Lote 318).
 *
 * Reuso declarado: o toolset do Narrador INCLUI localizar_no_codex e ler_pagina
 * (mesmos nomes/contratos do Auditor — MestreIATools.TOOL_LOCALIZAR/TOOL_LER); a
 * execução delega ao MestreIARepository via NarradorToolExecutor.
 */
object NarradorTools {

    const val TOOL_PEDIR_ROLAGEM = "pedir_rolagem"
    const val TOOL_INICIAR_COMBATE = "iniciar_combate"
    const val TOOL_ACAO_NPC = "acao_npc"
    const val TOOL_APLICAR_DANO = "aplicar_dano"
    const val TOOL_APLICAR_CONDICAO = "aplicar_condicao"
    const val TOOL_GASTAR_RECURSO = "gastar_recurso"
    const val TOOL_CONSULTAR_MUNDO = "consultar_mundo"
    const val TOOL_REGISTRAR_FATO = "registrar_fato"
    const val TOOL_AVANCAR_RELOGIO = "avancar_relogio"
    const val TOOL_PASSAR_TEMPO = "passar_tempo"
    const val TOOL_CONCEDER_XP = "conceder_xp"
    const val TOOL_DEFINIR_CENA = "definir_cena"
    const val TOOL_FORJAR_NPC = "forjar_npc"
    const val TOOL_INSPECIONAR_PERSONAGEM = "inspecionar_personagem"

    // Reuso do motor do Auditor (mesmos nomes de MestreIATools.TOOL_LOCALIZAR/TOOL_LER)
    const val TOOL_LOCALIZAR = "localizar_no_codex"
    const val TOOL_LER = "ler_pagina"

    /** Todas as tools que o executor do Narrador conhece (14 próprias + 2 do Códex). */
    val TODAS: Set<String> = setOf(
        TOOL_PEDIR_ROLAGEM, TOOL_INICIAR_COMBATE, TOOL_ACAO_NPC, TOOL_APLICAR_DANO,
        TOOL_APLICAR_CONDICAO, TOOL_GASTAR_RECURSO, TOOL_CONSULTAR_MUNDO, TOOL_REGISTRAR_FATO,
        TOOL_AVANCAR_RELOGIO, TOOL_PASSAR_TEMPO, TOOL_CONCEDER_XP, TOOL_DEFINIR_CENA,
        TOOL_FORJAR_NPC, TOOL_INSPECIONAR_PERSONAGEM, TOOL_LOCALIZAR, TOOL_LER
    )

    // ── Especificação neutra (uma fonte, dois formatos) ─────────────────────

    private data class Param(
        val nome: String,
        val tipo: String,            // "string" | "integer" | "array_string" | "array_objeto"
        val descricao: String,
        val obrigatorio: Boolean = false,
        val enum: List<String>? = null,
        /** Para tipo "array_objeto": campos do item (nome → tipo "string"/"integer"). */
        val camposItem: Map<String, String>? = null
    )

    private data class ToolSpec(
        val nome: String,
        val descricao: String,
        val params: List<Param>
    )

    private val SPECS: List<ToolSpec> = listOf(
        ToolSpec(
            TOOL_PEDIR_ROLAGEM,
            "Pede uma rolagem REAL de 3d6 ao jogador quando o resultado de uma ação é mecanicamente incerto. O dado é rolado pelo app e o resultado volta com soma, alvo, margem e classificação de crítico. Nunca declare resultado de teste sem esta ferramenta.",
            listOf(
                Param("pericia", "string", "Nome da perícia, atributo ou defesa a testar, como consta na ficha.", obrigatorio = true),
                Param("modificadores", "array_objeto", "Modificadores situacionais NOMEADOS que compõem o alvo. Lista vazia quando não houver.",
                    camposItem = mapOf("motivo" to "string", "valor" to "integer")),
                Param("motivo", "string", "Descrição curta da ação que está sendo testada, para o card de rolagem.")
            )
        ),
        ToolSpec(
            TOOL_INICIAR_COMBATE,
            "Abre um encontro de combate estruturado com os oponentes informados. A partir daí, ações ofensivas passam pelo motor de combate em vez de prosa livre.",
            listOf(
                Param("inimigos", "array_objeto", "Oponentes do encontro: identificador do bestiário ou conceito, com quantidade.",
                    obrigatorio = true, camposItem = mapOf("id_ou_conceito" to "string", "quantidade" to "integer")),
                Param("distancia_m", "integer", "Distância inicial aproximada em metros entre o herói e os oponentes."),
                Param("surpresa", "string", "Quem está surpreso no início, se houver surpresa.", enum = listOf("ninguem", "heroi", "inimigos"))
            )
        ),
        ToolSpec(
            TOOL_ACAO_NPC,
            "Declara a intenção de UM NPC no turno dele dentro de um combate aberto. O motor valida a legalidade, executa e devolve o relatório factual.",
            listOf(
                Param("npc_id", "string", "Identificador do NPC no encontro atual.", obrigatorio = true),
                Param("intencao", "string", "Manobra/ação pretendida em termos de combate.", obrigatorio = true),
                Param("alvo_id", "string", "Identificador do alvo da ação, quando houver."),
                Param("detalhes", "string", "Detalhe tático opcional (local visado, arma usada).")
            )
        ),
        ToolSpec(
            TOOL_APLICAR_DANO,
            "Aplica dano REAL a um combatente após um acerto confirmado. O motor calcula RD, multiplicador por local e efeitos — nunca declare perda de PV em prosa.",
            listOf(
                Param("alvo_id", "string", "Identificador do combatente que sofre o dano.", obrigatorio = true),
                Param("dano", "string", "Expressão de dano da arma/ataque conforme a ficha ou bestiário.", obrigatorio = true),
                Param("tipo", "string", "Tipo do dano.", obrigatorio = true, enum = listOf("cont", "corte", "perf", "imp", "quei", "tox", "cor", "fad")),
                Param("local", "string", "Local atingido, quando definido pelo ataque.")
            )
        ),
        ToolSpec(
            TOOL_APLICAR_CONDICAO,
            "Aplica ou remove uma condição de estado em um combatente ou no herói (efeito que o motor passa a considerar nas rolagens e manobras legais).",
            listOf(
                Param("alvo_id", "string", "Identificador de quem recebe a condição.", obrigatorio = true),
                Param("condicao", "string", "Nome da condição em termos de GURPS.", obrigatorio = true),
                Param("operacao", "string", "Aplicar ou remover.", enum = listOf("aplicar", "remover")),
                Param("duracao_turnos", "integer", "Duração em turnos, quando limitada.")
            )
        ),
        ToolSpec(
            TOOL_GASTAR_RECURSO,
            "Debita um recurso REAL da ficha do herói (fadiga, vida fora de combate, dinheiro, munição ou item consumível). O app aplica e confirma o novo total.",
            listOf(
                Param("recurso", "string", "Categoria do recurso a debitar.", obrigatorio = true, enum = listOf("pf", "pv", "dinheiro", "municao", "item")),
                Param("quantidade", "integer", "Quantidade a debitar.", obrigatorio = true),
                Param("motivo", "string", "Causa do gasto, para o extrato do jogador.", obrigatorio = true),
                Param("item_nome", "string", "Nome do item, quando recurso = item ou municao.")
            )
        ),
        ToolSpec(
            TOOL_CONSULTAR_MUNDO,
            "Busca fatos CANÔNICOS já registrados da campanha (pessoas, lugares, eventos, promessas). O que esta ferramenta devolve é verdade estabelecida — não contradiga nem reinvente.",
            listOf(
                Param("consulta", "string", "Termos do que se quer lembrar sobre o mundo.", obrigatorio = true),
                Param("limite", "integer", "Máximo de fatos a retornar.")
            )
        ),
        ToolSpec(
            TOOL_REGISTRAR_FATO,
            "Grava um fato novo e canônico do mundo no formato sujeito-predicado-objeto. Registre tudo que precisará ser lembrado em cenas futuras; o peso reflete a importância.",
            listOf(
                Param("sujeito", "string", "Quem/o quê.", obrigatorio = true),
                Param("predicado", "string", "Relação ou ação.", obrigatorio = true),
                Param("objeto", "string", "Complemento.", obrigatorio = true),
                Param("peso", "integer", "Importância canônica de 1 (detalhe) a 10 (evento estrutural).", obrigatorio = true),
                Param("detalhe", "string", "Contexto adicional curto, se necessário.")
            )
        ),
        ToolSpec(
            TOOL_AVANCAR_RELOGIO,
            "Avança um relógio de facção/ameaça do cenário em razão de ações ou omissões do herói. Relógio cheio dispara o evento da facção.",
            listOf(
                Param("relogio_id", "string", "Identificador do relógio no cenário ativo.", obrigatorio = true),
                Param("passos", "integer", "Quantos segmentos avançar.", obrigatorio = true),
                Param("motivo", "string", "O que causou o avanço.", obrigatorio = true)
            )
        ),
        ToolSpec(
            TOOL_PASSAR_TEMPO,
            "Avança o tempo DE JOGO do mundo. O motor processa clima, relógios e ecologia no intervalo e devolve um delta textual do que mudou.",
            listOf(
                Param("minutos", "integer", "Minutos de jogo a avançar.", obrigatorio = true),
                Param("modo", "string", "Natureza do intervalo.", enum = listOf("descanso", "viagem", "atividade"))
            )
        ),
        ToolSpec(
            TOOL_CONCEDER_XP,
            "Concede pontos de personagem ao herói por marco do arco ou interpretação. Sujeito a teto por sessão e validação do motor de XP.",
            listOf(
                Param("pontos", "integer", "Pontos a conceder.", obrigatorio = true),
                Param("motivo", "string", "Justificativa vinculada a marco do arco ou desvantagem honrada.", obrigatorio = true)
            )
        ),
        ToolSpec(
            TOOL_DEFINIR_CENA,
            "Abre uma cena nova (ou redefine a atual) com título, bioma e humor — controla arte, áudio e o cabeçalho do feed.",
            listOf(
                Param("titulo", "string", "Título curto da cena.", obrigatorio = true),
                Param("bioma", "string", "Ambiente físico predominante da cena.", obrigatorio = true),
                Param("humor", "string", "Tom emocional predominante da cena.", obrigatorio = true),
                Param("resumo", "string", "Síntese de abertura opcional.")
            )
        ),
        ToolSpec(
            TOOL_FORJAR_NPC,
            "Cria um NPC com ficha real via Forjador quando o bestiário não cobre o necessário. Devolve o identificador do NPC pronto para uso em combate.",
            listOf(
                Param("conceito", "string", "Conceito do NPC em uma frase.", obrigatorio = true),
                Param("pontos", "integer", "Orçamento de pontos aproximado."),
                Param("nome", "string", "Nome próprio, se já definido na narrativa.")
            )
        ),
        ToolSpec(
            TOOL_INSPECIONAR_PERSONAGEM,
            "Lê uma seção da ficha REAL do herói. Use antes de afirmar qualquer capacidade, recurso ou número do personagem.",
            listOf(
                Param("secao", "string", "Seção da ficha a ler.", obrigatorio = true,
                    enum = listOf("atributos", "vantagens", "desvantagens", "pericias", "magias", "equipamentos", "pontos", "completo"))
            )
        ),
        ToolSpec(
            TOOL_LOCALIZAR,
            "Localiza páginas do Códex GURPS que contêm TODAS as palavras informadas. Retorna lista compacta de páginas com trecho curto — use para descobrir ONDE está a regra e depois ler_pagina.",
            listOf(
                Param("termos", "string", "Palavras-chave técnicas separadas por espaço.", obrigatorio = true),
                Param("livros", "array_string", "Restringe a busca a estes livros; omita para todos.")
            )
        ),
        ToolSpec(
            TOOL_LER,
            "Lê o texto COMPLETO de uma página do Códex (ou intervalo curto). É a única fonte válida para citar regra.",
            listOf(
                Param("livro", "string", "Livro da página.", obrigatorio = true,
                    enum = listOf("Módulo Básico", "Artes Marciais", "Magia", "Gun Fu", "Pyramid Aquático")),
                Param("pagina", "integer", "Número da página.", obrigatorio = true),
                Param("pagina_final", "integer", "Final do intervalo, quando a regra atravessa páginas.")
            )
        )
    )

    // ── Renderizadores ──────────────────────────────────────────────────────

    private fun tipoJson(tipo: String, gemini: Boolean): String = when (tipo) {
        "integer" -> if (gemini) "INTEGER" else "integer"
        "array_string", "array_objeto" -> if (gemini) "ARRAY" else "array"
        else -> if (gemini) "STRING" else "string"
    }

    private fun paramSchema(p: Param, gemini: Boolean): JSONObject = JSONObject().apply {
        put("type", tipoJson(p.tipo, gemini))
        put("description", p.descricao)
        p.enum?.let { put("enum", JSONArray(it)) }
        if (p.tipo == "array_string") {
            put("items", JSONObject().put("type", tipoJson("string", gemini)))
        }
        if (p.tipo == "array_objeto" && p.camposItem != null) {
            put("items", JSONObject().apply {
                put("type", if (gemini) "OBJECT" else "object")
                put("properties", JSONObject().apply {
                    p.camposItem.forEach { (nome, tipo) ->
                        put(nome, JSONObject().put("type", tipoJson(tipo, gemini)))
                    }
                })
            })
        }
    }

    private fun parametros(spec: ToolSpec, gemini: Boolean): JSONObject = JSONObject().apply {
        put("type", if (gemini) "OBJECT" else "object")
        put("properties", JSONObject().apply {
            spec.params.forEach { p -> put(p.nome, paramSchema(p, gemini)) }
        })
        val obrigatorios = spec.params.filter { it.obrigatorio }.map { it.nome }
        if (obrigatorios.isNotEmpty()) put("required", JSONArray(obrigatorios))
    }

    /** Formato nativo Gemini: [{functionDeclarations: [...]}]. */
    fun getGeminiTools(): JSONArray {
        val decls = JSONArray()
        SPECS.forEach { spec ->
            decls.put(JSONObject().apply {
                put("name", spec.nome)
                put("description", spec.descricao)
                put("parameters", parametros(spec, gemini = true))
            })
        }
        return JSONArray().put(JSONObject().put("functionDeclarations", decls))
    }

    /** Formato OpenAI/DeepSeek: [{type: function, function: {...}}]. */
    fun getOpenAITools(): JSONArray {
        val tools = JSONArray()
        SPECS.forEach { spec ->
            tools.put(JSONObject().apply {
                put("type", "function")
                put("function", JSONObject().apply {
                    put("name", spec.nome)
                    put("description", spec.descricao)
                    put("parameters", parametros(spec, gemini = false))
                })
            })
        }
        return tools
    }
}
