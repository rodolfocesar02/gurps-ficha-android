package com.gurps.ficha.data.network

import com.gurps.ficha.domain.tools.ForjadorTools
import org.json.JSONArray
import org.json.JSONObject

/**
 * Definição das Ferramentas (Tools) do Agente Mestre IA.
 * Contém os Schemas JSON compatíveis com Gemini (Google Native) e OpenAI/DeepSeek.
 */
object MestreIATools {

    const val TOOL_FILL_SHEET = "fill_character_sheet"
    const val TOOL_NEXUS_ARCANO = "consultar_nexus_arcano"
    const val TOOL_INSPECT_CHARACTER = "inspecionar_personagem"
    const val TOOL_ANALYZE_ENV = "analisar_ambiente"

    // Lote 325: NOVO motor de busca por palavra-chave (Auditor). Substitui a busca
    // semântica (embedding/HNSW) por "grep + leitura dirigida":
    //  - localizar_no_codex: lista páginas que casam (AND de palavras), trecho curto
    //  - ler_pagina: abre o texto completo de uma página/intervalo escolhido
    const val TOOL_LOCALIZAR = "localizar_no_codex"
    const val TOOL_LER = "ler_pagina"

    private val LIVROS_ENUM = JSONArray()
        .put("Módulo Básico").put("Artes Marciais").put("Magia").put("Gun Fu").put("Pyramid Aquático")

    /**
     * Lote 325: Toolset do AUDITOR (modo dúvida/conversa). Formato OpenAI/DeepSeek.
     * Motor "grep + leitura dirigida" — sem busca semântica:
     *   1. localizar_no_codex(termos, livros?) → páginas que casam (AND), trecho curto
     *   2. ler_pagina(livro, pagina, pagina_final?) → texto completo pra interpretar
     * + inspecionar_personagem e consultar_nexus_arcano (auxiliares).
     */
    fun getAuditorToolsOpenAI(): JSONArray {
        val tools = JSONArray()

        // LOCALIZAR — a "página de resultados". AND: mais palavras = menos páginas.
        tools.put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", TOOL_LOCALIZAR)
                put("description", "Localiza páginas do Códex que contêm TODAS as palavras informadas (igual a uma busca AND: cada palavra a mais restringe o resultado). Retorna uma lista compacta de páginas com um trecho curto de cada — NÃO o texto completo. Use para descobrir EM QUAIS páginas está a regra; depois use ler_pagina para ler as escolhidas. Comece amplo e adicione palavras para estreitar quando vierem páginas demais; remova/troque palavras quando vier nenhuma.")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("termos", JSONObject().put("type", "string").put("description", "Palavras-chave separadas por espaço. Use os termos técnicos exatos da regra. Mais palavras = busca mais restrita."))
                        put("livros", JSONObject().apply {
                            put("type", "array")
                            put("description", "Opcional. Restringe a busca a estes livros. Omita para procurar em todos.")
                            put("items", JSONObject().put("type", "string").put("enum", LIVROS_ENUM))
                        })
                    })
                    put("required", JSONArray().put("termos"))
                })
            })
        })

        // LER — abre a página inteira (ou intervalo curto) pra interpretar a regra.
        tools.put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", TOOL_LER)
                put("description", "Lê o TEXTO COMPLETO de uma página específica de um livro (ou um intervalo curto de páginas, quando a regra/tabela atravessa páginas). Use depois de localizar_no_codex, nas páginas que você julgou relevantes. É aqui que você lê a regra inteira para interpretar e citar.")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("livro", JSONObject().put("type", "string").put("description", "Livro da página.").put("enum", LIVROS_ENUM))
                        put("pagina", JSONObject().put("type", "integer").put("description", "Número da página a ler."))
                        put("pagina_final", JSONObject().put("type", "integer").put("description", "Opcional. Para ler um intervalo (ex.: tabela que continua na página seguinte). Máximo de 4 páginas por leitura."))
                    })
                    put("required", JSONArray().put("livro").put("pagina"))
                })
            })
        })

        // INSPECIONAR PERSONAGEM (auxiliar — reaproveita schema do Auditor)
        tools.put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", TOOL_INSPECT_CHARACTER)
                put("description", "Lê dados da ficha atual para contextualizar a resposta.")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("secao", JSONObject().put("type", "string").put("enum", JSONArray().put("atributos").put("vantagens").put("pericias").put("status").put("armas").put("armaduras").put("completo")))
                    })
                })
            })
        })

        // NEXUS ARCANO (auxiliar — pré-requisitos de magias)
        tools.put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", TOOL_NEXUS_ARCANO)
                put("description", "Trilha técnica de pré-requisitos de magias.")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("magia_alvo", JSONObject().put("type", "string"))
                    })
                    put("required", JSONArray().put("magia_alvo"))
                })
            })
        })

        return tools
    }

    /**
     * Toolset UNIFICADA do Auditor (modo "analise"):
     * tools do Forjador (ler_ficha, buscar_catalogo, gps_magia, editar_ficha,
     * buscar_racas, aplicar_modelo_racial)
     * + busca no manual (localizar_no_codex, ler_pagina) + nexus_arcano.
     * Formato OpenAI/DeepSeek.
     *
     * PROMPT que usa este toolset: MestreIAPromptsForjador.gerarPromptConsultor()
     * EXECUTOR: MestreIAGeneratorUseCase — filtra TOOL_LOCALIZAR + TOOL_LER além das ForjadorTools.
     */
    fun getAuditorUnificadoToolsOpenAI(): JSONArray {
        // Lote 350: base trocada para as ForjadorTools reais — o commit d9d999c montava a
        // base com o toolset legado de embedding (8 schemas que o executor não roda).
        val tools = ForjadorTools.getOpenAITools()

        // Adiciona as 2 tools de consulta ao manual (grep + leitura dirigida)
        tools.put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", TOOL_LOCALIZAR)
                put("description", "Localiza páginas do manual GURPS que contêm TODAS as palavras informadas (busca AND). Retorna lista compacta com livro, número de página e um trecho curto — NÃO o texto completo. Use para descobrir EM QUAIS páginas está a regra; depois use ler_pagina para ler. Comece amplo e adicione palavras para estreitar; troque por sinônimos técnicos quando vier nenhuma.")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("termos", JSONObject().put("type", "string").put("description", "Palavras-chave separadas por espaço. Use termos técnicos exatos. Mais palavras = busca mais restrita."))
                        put("livros", JSONObject().apply {
                            put("type", "array")
                            put("description", "Opcional. Restringe a estes livros. Omita para procurar em todos.")
                            put("items", JSONObject().put("type", "string").put("enum", LIVROS_ENUM))
                        })
                    })
                    put("required", JSONArray().put("termos"))
                })
            })
        })

        tools.put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", TOOL_LER)
                put("description", "Lê o TEXTO COMPLETO de uma página do manual GURPS (ou intervalo curto quando a regra/tabela atravessa páginas). Use depois de localizar_no_codex, nas páginas que você julgou relevantes. É aqui que você lê a regra inteira para citar e embasar a análise.")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("livro", JSONObject().put("type", "string").put("description", "Livro da página.").put("enum", LIVROS_ENUM))
                        put("pagina", JSONObject().put("type", "integer").put("description", "Número da página a ler."))
                        put("pagina_final", JSONObject().put("type", "integer").put("description", "Opcional. Para ler um intervalo (ex.: tabela que continua na seguinte). Máximo 4 páginas."))
                    })
                    put("required", JSONArray().put("livro").put("pagina"))
                })
            })
        })

        // Nexus Arcano (pré-requisitos de magias)
        tools.put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", TOOL_NEXUS_ARCANO)
                put("description", "Trilha técnica de pré-requisitos de magias. Use para planejar a cadeia antes de sugerir ao jogador.")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("magia_alvo", JSONObject().put("type", "string").put("description", "ID da magia (ex: 'tempestade_de_relampagos')"))
                    })
                    put("required", JSONArray().put("magia_alvo"))
                })
            })
        })

        return tools
    }

    /**
     * Mesma toolset unificada do Auditor, no formato NATIVO do Gemini.
     * Usada quando o Auditor cai em backup com endpoint Google-native.
     */
    fun getAuditorUnificadoToolsGemini(): JSONArray {
        // Lote 350: base trocada para as ForjadorTools reais (mesma correção da variante OpenAI).
        val forjadorDecls = ForjadorTools.getGeminiTools()
            .optJSONObject(0)?.optJSONArray("functionDeclarations") ?: JSONArray()
        val fns = JSONArray()
        for (i in 0 until forjadorDecls.length()) fns.put(forjadorDecls.getJSONObject(i))

        fns.put(JSONObject().apply {
            put("name", TOOL_LOCALIZAR)
            put("description", "Localiza páginas do manual GURPS que contêm TODAS as palavras informadas (busca AND). Retorna lista compacta (livro|página|trecho curto). Use para descobrir onde está a regra; depois use ler_pagina. Adicione palavras para estreitar; remova/troque quando vier nenhuma.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("termos", JSONObject().put("type", "STRING").put("description", "Palavras-chave separadas por espaço. Termos técnicos exatos. Mais palavras = mais restrito."))
                    put("livros", JSONObject().apply {
                        put("type", "ARRAY")
                        put("description", "Opcional. Restringe a estes livros. Omita para todos.")
                        put("items", JSONObject().put("type", "STRING").put("enum", LIVROS_ENUM))
                    })
                })
                put("required", JSONArray().put("termos"))
            })
        })

        fns.put(JSONObject().apply {
            put("name", TOOL_LER)
            put("description", "Lê o TEXTO COMPLETO de uma página do manual GURPS (ou intervalo curto). Use depois de localizar_no_codex nas páginas relevantes. É aqui que você lê a regra inteira para citar.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("livro", JSONObject().put("type", "STRING").put("description", "Livro da página.").put("enum", LIVROS_ENUM))
                    put("pagina", JSONObject().put("type", "INTEGER").put("description", "Número da página a ler."))
                    put("pagina_final", JSONObject().put("type", "INTEGER").put("description", "Opcional. Intervalo (tabela que continua). Máximo 4 páginas."))
                })
                put("required", JSONArray().put("livro").put("pagina"))
            })
        })

        fns.put(JSONObject().apply {
            put("name", TOOL_NEXUS_ARCANO)
            put("description", "Trilha técnica de pré-requisitos de magias.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("magia_alvo", JSONObject().put("type", "STRING").put("description", "ID da magia"))
                })
                put("required", JSONArray().put("magia_alvo"))
            })
        })

        return JSONArray().put(JSONObject().put("functionDeclarations", fns))
    }

    /**
     * Lote 325: mesma toolset do Auditor (localizar + ler + inspect + nexus),
     * no formato NATIVO do Gemini (functionDeclarations, tipos OBJECT/STRING/ARRAY).
     * Usada quando o Auditor cai num backup com endpoint Google-native.
     * @deprecated Substituído por getAuditorUnificadoToolsGemini()
     */
    fun getAuditorToolsGemini(): JSONArray {
        val fns = JSONArray()

        fns.put(JSONObject().apply {
            put("name", TOOL_LOCALIZAR)
            put("description", "Localiza páginas do Códex que contêm TODAS as palavras informadas (busca AND: cada palavra a mais restringe). Retorna lista compacta de páginas com um trecho curto — NÃO o texto completo. Use para descobrir EM QUAIS páginas está a regra; depois use ler_pagina nas escolhidas. Comece amplo e adicione palavras para estreitar; remova/troque palavras quando vier nenhuma.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("termos", JSONObject().put("type", "STRING").put("description", "Palavras-chave separadas por espaço. Termos técnicos exatos da regra. Mais palavras = mais restrito."))
                    put("livros", JSONObject().apply {
                        put("type", "ARRAY")
                        put("description", "Opcional. Restringe a busca a estes livros. Omita para procurar em todos.")
                        put("items", JSONObject().put("type", "STRING").put("enum", LIVROS_ENUM))
                    })
                })
                put("required", JSONArray().put("termos"))
            })
        })

        fns.put(JSONObject().apply {
            put("name", TOOL_LER)
            put("description", "Lê o TEXTO COMPLETO de uma página específica de um livro (ou intervalo curto, quando a regra/tabela atravessa páginas). Use depois de localizar_no_codex, nas páginas que julgou relevantes. É aqui que você lê a regra inteira para interpretar e citar.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("livro", JSONObject().put("type", "STRING").put("description", "Livro da página.").put("enum", LIVROS_ENUM))
                    put("pagina", JSONObject().put("type", "INTEGER").put("description", "Número da página a ler."))
                    put("pagina_final", JSONObject().put("type", "INTEGER").put("description", "Opcional. Intervalo (tabela que continua). Máximo 4 páginas."))
                })
                put("required", JSONArray().put("livro").put("pagina"))
            })
        })

        fns.put(JSONObject().apply {
            put("name", TOOL_INSPECT_CHARACTER)
            put("description", "Lê dados da ficha atual para contextualizar a resposta.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("secao", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Seção: 'atributos', 'vantagens', 'pericias', 'status', 'armas', 'armaduras', 'completo'.")
                    })
                })
            })
        })

        fns.put(JSONObject().apply {
            put("name", TOOL_NEXUS_ARCANO)
            put("description", "Trilha técnica de pré-requisitos de magias.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("magia_alvo", JSONObject().put("type", "STRING"))
                })
                put("required", JSONArray().put("magia_alvo"))
            })
        })

        return JSONArray().put(JSONObject().put("functionDeclarations", fns))
    }
}
