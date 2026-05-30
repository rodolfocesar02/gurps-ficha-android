package com.gurps.ficha.data.network

import org.json.JSONArray
import org.json.JSONObject

/**
 * Definição das Ferramentas (Tools) do Agente Mestre IA.
 * Contém os Schemas JSON compatíveis com Gemini (Google Native) e OpenAI/DeepSeek.
 */
object MestreIATools {

    const val TOOL_MANUAL_DIRETO = "consultar_manual_direto"
    const val TOOL_FILL_SHEET = "fill_character_sheet"
    const val TOOL_NEXUS_ARCANO = "consultar_nexus_arcano"
    const val TOOL_INSPECT_CHARACTER = "inspecionar_personagem"
    const val TOOL_ANALYZE_ENV = "analisar_ambiente"

    // Lote 317: tools especializadas por livro — modelo escolhe pela natureza
    // da pergunta, não por palavra-chave isolada. Cada uma força filtroLivro fixo.
    // (Lote 325: usadas apenas pelo Forjador agora — Auditor migrou para localizar/ler.)
    const val TOOL_REGRAS_MAGIA = "consultar_regras_magia"
    const val TOOL_REGRAS_ARMAS_FOGO = "consultar_regras_armas_fogo"
    const val TOOL_REGRAS_ARTES_MARCIAIS = "consultar_regras_artes_marciais"
    const val TOOL_REGRAS_AQUATICO = "consultar_regras_aquatico"

    // Lote 325: NOVO motor de busca por palavra-chave (Auditor). Substitui a busca
    // semântica (embedding/HNSW) por "grep + leitura dirigida":
    //  - localizar_no_codex: lista páginas que casam (AND de palavras), trecho curto
    //  - ler_pagina: abre o texto completo de uma página/intervalo escolhido
    const val TOOL_LOCALIZAR = "localizar_no_codex"
    const val TOOL_LER = "ler_pagina"

    private val LIVROS_ENUM = JSONArray()
        .put("Módulo Básico").put("Artes Marciais").put("Magia").put("Gun Fu").put("Pyramid Aquático")

    /**
     * Retorna a lista de Function Declarations no formato nativo do Gemini.
     */
    fun getGeminiTools(modo: String): JSONArray {
        val functionDeclarations = JSONArray()

        // Ferramenta 1: Busca Direta no Códex (Lote 271)
        functionDeclarations.put(JSONObject().apply {
            put("name", TOOL_MANUAL_DIRETO)
            put("description", "FERRAMENTA GENÉRICA. Use APENAS quando a pergunta não cabe nas especializadas (consultar_regras_magia, consultar_regras_armas_fogo, consultar_regras_artes_marciais, consultar_regras_aquatico). Boa para: atributos, vantagens, desvantagens, perícias gerais, manobras de combate genéricas, tabelas, equipamentos não-armas, regras transversais. Use queries CURTAS e ESPECÍFICAS por conceito isolado.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("query", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Conceito técnico isolado para busca. Máximo 6 palavras.")
                    })
                    put("livro", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Opcional. Filtra por livro. Omita para buscar em todos os 5 livros.")
                        put("enum", JSONArray().put("Módulo Básico").put("Artes Marciais").put("Magia").put("Gun Fu").put("Pyramid Aquático"))
                    })
                })
                put("required", JSONArray().put("query"))
            })
        })

        // Lote 317/318 — Tool especializada: Magia
        functionDeclarations.put(JSONObject().apply {
            put("name", TOOL_REGRAS_MAGIA)
            put("description", "Busca no LIVRO DE MAGIA do GURPS. Use sempre que o tema central da pergunta for qualquer aspecto mágico/sobrenatural do sistema. Prefira esta sobre a genérica quando o foco da pergunta for magia.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("query", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Conceito específico de magia. Máximo 6 palavras.")
                    })
                })
                put("required", JSONArray().put("query"))
            })
        })

        // Lote 317/318 — Tool especializada: Armas de Fogo
        functionDeclarations.put(JSONObject().apply {
            put("name", TOOL_REGRAS_ARMAS_FOGO)
            put("description", "Busca no livro GUN FU (regras especializadas de armas de fogo). Use sempre que o tema central da pergunta envolver uso, mecânica ou manuseio de qualquer arma de fogo. Prefira esta sobre a genérica quando o foco da pergunta for arma de fogo.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("query", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Conceito específico de armas de fogo. Máximo 6 palavras.")
                    })
                })
                put("required", JSONArray().put("query"))
            })
        })

        // Lote 317/318 — Tool especializada: Artes Marciais
        functionDeclarations.put(JSONObject().apply {
            put("name", TOOL_REGRAS_ARTES_MARCIAIS)
            put("description", "Busca no livro ARTES MARCIAIS. Use sempre que a pergunta envolver técnicas corpo a corpo nomeadas, estilos marciais específicos, combate desarmado ou manobras avançadas além das básicas do Módulo Básico. Prefira esta sobre a genérica quando o foco da pergunta for luta corporal ou técnica marcial.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("query", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Conceito específico de artes marciais. Máximo 6 palavras.")
                    })
                })
                put("required", JSONArray().put("query"))
            })
        })

        // Lote 317/318 — Tool especializada: Aquático
        functionDeclarations.put(JSONObject().apply {
            put("name", TOOL_REGRAS_AQUATICO)
            put("description", "Busca no PYRAMID AQUÁTICO (ambientes submersos). Use sempre que água/submersão for elemento estrutural da regra perguntada (não apenas cenário visual ou personagem molhado). Prefira esta sobre a genérica quando o ambiente aquático for parte da mecânica da pergunta.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("query", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Conceito específico de combate/movimento aquático. Máximo 6 palavras.")
                    })
                })
                put("required", JSONArray().put("query"))
            })
        })

        // Ferramenta 2: Inspeção de Ficha
        functionDeclarations.put(JSONObject().apply {
            put("name", TOOL_INSPECT_CHARACTER)
            put("description", "Lê detalhes da ficha do jogador. Use 'armas' para ver as armas do inventário com dano e tipo, 'armaduras' para ver RD e localização, 'pericias' para NHs, 'status' para PV/PF, 'atributos' para ST/DX/IQ/HT.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("secao", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Seção: 'atributos', 'vantagens', 'pericias', 'status', 'armas', 'armaduras'.")
                    })
                })
            })
        })

        // Ferramenta 3: Nexus Arcano
        functionDeclarations.put(JSONObject().apply {
            put("name", TOOL_NEXUS_ARCANO)
            put("description", "Gabarito técnico de pré-requisitos de magias.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("magia_alvo", JSONObject().put("type", "STRING"))
                })
                put("required", JSONArray().put("magia_alvo"))
            })
        })

        // Ferramenta 4: Criação de Fichas
        functionDeclarations.put(JSONObject().apply {
            put("name", TOOL_FILL_SHEET)
            put("description", "Preenche a ficha completa.")
            put("parameters", getSheetSchemaGemini())
        })

        return JSONArray().put(JSONObject().put("functionDeclarations", functionDeclarations))
    }

    /**
     * Retorna a lista de Tools no formato OpenAI/DeepSeek.
     */
    fun getOpenAITools(modo: String): JSONArray {
        val tools = JSONArray()

        // Manual Direto (Lote 271)
        tools.put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", TOOL_MANUAL_DIRETO)
                put("description", "FERRAMENTA GENÉRICA. Use APENAS quando a pergunta não cabe nas especializadas (consultar_regras_magia, consultar_regras_armas_fogo, consultar_regras_artes_marciais, consultar_regras_aquatico). Boa para: atributos, vantagens, desvantagens, perícias gerais, manobras de combate genéricas, tabelas, equipamentos não-armas, regras transversais.")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("query", JSONObject().put("type", "string").put("description", "Conceito técnico isolado. Máximo 6 palavras."))
                        put("livro", JSONObject().apply {
                            put("type", "string")
                            put("description", "Opcional. Filtra por livro. Omita para buscar em todos os 5 livros.")
                            put("enum", JSONArray().put("Módulo Básico").put("Artes Marciais").put("Magia").put("Gun Fu").put("Pyramid Aquático"))
                        })
                    })
                    put("required", JSONArray().put("query"))
                })
            })
        })

        // Lote 317/318 — Tool especializada: Magia
        tools.put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", TOOL_REGRAS_MAGIA)
                put("description", "Busca no LIVRO DE MAGIA do GURPS. Use sempre que o tema central da pergunta for qualquer aspecto mágico/sobrenatural do sistema. Prefira esta sobre a genérica quando o foco da pergunta for magia.")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("query", JSONObject().put("type", "string").put("description", "Conceito específico de magia. Máximo 6 palavras."))
                    })
                    put("required", JSONArray().put("query"))
                })
            })
        })

        // Lote 317/318 — Tool especializada: Armas de Fogo
        tools.put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", TOOL_REGRAS_ARMAS_FOGO)
                put("description", "Busca no livro GUN FU (regras especializadas de armas de fogo). Use sempre que o tema central da pergunta envolver uso, mecânica ou manuseio de qualquer arma de fogo. Prefira esta sobre a genérica quando o foco da pergunta for arma de fogo.")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("query", JSONObject().put("type", "string").put("description", "Conceito específico de armas de fogo. Máximo 6 palavras."))
                    })
                    put("required", JSONArray().put("query"))
                })
            })
        })

        // Lote 317/318 — Tool especializada: Artes Marciais
        tools.put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", TOOL_REGRAS_ARTES_MARCIAIS)
                put("description", "Busca no livro ARTES MARCIAIS. Use sempre que a pergunta envolver técnicas corpo a corpo nomeadas, estilos marciais específicos, combate desarmado ou manobras avançadas além das básicas do Módulo Básico. Prefira esta sobre a genérica quando o foco da pergunta for luta corporal ou técnica marcial.")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("query", JSONObject().put("type", "string").put("description", "Conceito específico de artes marciais. Máximo 6 palavras."))
                    })
                    put("required", JSONArray().put("query"))
                })
            })
        })

        // Lote 317/318 — Tool especializada: Aquático
        tools.put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", TOOL_REGRAS_AQUATICO)
                put("description", "Busca no PYRAMID AQUÁTICO (ambientes submersos). Use sempre que água/submersão for elemento estrutural da regra perguntada (não apenas cenário visual ou personagem molhado). Prefira esta sobre a genérica quando o ambiente aquático for parte da mecânica da pergunta.")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("query", JSONObject().put("type", "string").put("description", "Conceito específico de combate/movimento aquático. Máximo 6 palavras."))
                    })
                    put("required", JSONArray().put("query"))
                })
            })
        })

        // Inspecionar Personagem
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

        // Nexus
        tools.put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", TOOL_NEXUS_ARCANO)
                put("description", "Trilha técnica de magias.")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("magia_alvo", JSONObject().put("type", "string"))
                    })
                    put("required", JSONArray().put("magia_alvo"))
                })
            })
        })

        // Fill Sheet
        tools.put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", TOOL_FILL_SHEET)
                put("description", "Preenche a ficha completa.")
                put("parameters", getSheetSchemaOpenAI())
            })
        })

        return tools
    }

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

    private fun getSheetSchemaGemini(): JSONObject {
        return JSONObject().apply {
            put("type", "OBJECT")
            put("properties", JSONObject().apply {
                put("nome", JSONObject().put("type", "STRING").put("description", "Nome do personagem"))
                put("historico", JSONObject().put("type", "STRING").put("description", "História/Background narrativo do personagem"))
                put("aparencia", JSONObject().put("type", "STRING").put("description", "Aparência física"))
                put("atributos", JSONObject().apply {
                    put("type", "OBJECT")
                    put("properties", JSONObject().apply {
                        put("st", JSONObject().put("type", "INTEGER"))
                        put("dx", JSONObject().put("type", "INTEGER"))
                        put("iq", JSONObject().put("type", "INTEGER"))
                        put("ht", JSONObject().put("type", "INTEGER"))
                    })
                    put("required", JSONArray().put("st").put("dx").put("iq").put("ht"))
                })
                put("vantagens", getArrayOfTraitsGemini())
                put("desvantagens", getArrayOfTraitsGemini())
                put("pericias", getArrayOfSkillsGemini())
                put("tecnicas", getArrayOfSkillsGemini())
                put("magias", getArrayOfSpellsGemini())
                put("equipamentos", getArrayOfEquipmentsGemini())
            })
            put("required", JSONArray().put("nome").put("atributos").put("historico"))
        }
    }

    private fun getSheetSchemaOpenAI(): JSONObject {
        return JSONObject().apply {
            put("type", "object")
            put("properties", JSONObject().apply {
                put("nome", JSONObject().put("type", "string").put("description", "Nome do personagem"))
                put("historico", JSONObject().put("type", "string").put("description", "História/Background narrativo do personagem"))
                put("aparencia", JSONObject().put("type", "string").put("description", "Aparência física"))
                put("atributos", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("st", JSONObject().put("type", "integer"))
                        put("dx", JSONObject().put("type", "integer"))
                        put("iq", JSONObject().put("type", "integer"))
                        put("ht", JSONObject().put("type", "integer"))
                    })
                    put("required", JSONArray().put("st").put("dx").put("iq").put("ht"))
                })
                put("vantagens", getArrayOfTraitsOpenAI())
                put("desvantagens", getArrayOfTraitsOpenAI())
                put("pericias", getArrayOfSkillsOpenAI())
                put("tecnicas", getArrayOfSkillsOpenAI())
                put("magias", getArrayOfSpellsOpenAI())
                put("equipamentos", getArrayOfEquipmentsOpenAI())
            })
            put("required", JSONArray().put("nome").put("atributos").put("historico"))
        }
    }

    private fun getArrayOfTraitsGemini() = JSONObject().apply {
        put("type", "ARRAY")
        put("items", JSONObject().apply {
            put("type", "OBJECT")
            put("properties", JSONObject().apply {
                put("nome", JSONObject().put("type", "STRING"))
                put("custo", JSONObject().put("type", "INTEGER"))
                put("descricao", JSONObject().put("type", "STRING"))
            })
            put("required", JSONArray().put("nome").put("custo"))
        })
    }

    private fun getArrayOfSkillsGemini() = JSONObject().apply {
        put("type", "ARRAY")
        put("items", JSONObject().apply {
            put("type", "OBJECT")
            put("properties", JSONObject().apply {
                put("nome", JSONObject().put("type", "STRING"))
                put("nivel", JSONObject().put("type", "INTEGER"))
            })
            put("required", JSONArray().put("nome").put("nivel"))
        })
    }

    private fun getArrayOfSpellsGemini() = JSONObject().apply {
        put("type", "ARRAY")
        put("items", JSONObject().apply {
            put("type", "OBJECT")
            put("properties", JSONObject().apply {
                put("nome", JSONObject().put("type", "STRING"))
                put("custo", JSONObject().put("type", "STRING"))
                put("tempo", JSONObject().put("type", "STRING"))
            })
            put("required", JSONArray().put("nome"))
        })
    }

    private fun getArrayOfEquipmentsGemini() = JSONObject().apply {
        put("type", "ARRAY")
        put("items", JSONObject().apply {
            put("type", "OBJECT")
            put("properties", JSONObject().apply {
                put("nome", JSONObject().put("type", "STRING"))
                put("peso", JSONObject().put("type", "NUMBER"))
                put("custo", JSONObject().put("type", "NUMBER"))
                put("quantidade", JSONObject().put("type", "INTEGER"))
                put("rd", JSONObject().put("type", "INTEGER"))
                put("dano", JSONObject().put("type", "STRING"))
                put("st_min", JSONObject().put("type", "INTEGER"))
                put("aparar", JSONObject().put("type", "STRING"))
            })
            put("required", JSONArray().put("nome").put("peso").put("custo"))
        })
    }

    private fun getArrayOfTraitsOpenAI() = JSONObject().apply {
        put("type", "array")
        put("items", JSONObject().apply {
            put("type", "object")
            put("properties", JSONObject().apply {
                put("nome", JSONObject().put("type", "string"))
                put("custo", JSONObject().put("type", "integer"))
                put("descricao", JSONObject().put("type", "string"))
            })
            put("required", JSONArray().put("nome").put("custo"))
        })
    }

    private fun getArrayOfSkillsOpenAI() = JSONObject().apply {
        put("type", "array")
        put("items", JSONObject().apply {
            put("type", "object")
            put("properties", JSONObject().apply {
                put("nome", JSONObject().put("type", "string"))
                put("nivel", JSONObject().put("type", "integer"))
            })
            put("required", JSONArray().put("nome").put("nivel"))
        })
    }

    private fun getArrayOfSpellsOpenAI() = JSONObject().apply {
        put("type", "array")
        put("items", JSONObject().apply {
            put("type", "object")
            put("properties", JSONObject().apply {
                put("nome", JSONObject().put("type", "string"))
                put("custo", JSONObject().put("type", "string"))
                put("tempo", JSONObject().put("type", "string"))
            })
            put("required", JSONArray().put("nome"))
        })
    }

    private fun getArrayOfEquipmentsOpenAI() = JSONObject().apply {
        put("type", "array")
        put("items", JSONObject().apply {
            put("type", "object")
            put("properties", JSONObject().apply {
                put("nome", JSONObject().put("type", "string"))
                put("peso", JSONObject().put("type", "number"))
                put("custo", JSONObject().put("type", "number"))
                put("quantidade", JSONObject().put("type", "integer"))
                put("rd", JSONObject().put("type", "integer"))
                put("dano", JSONObject().put("type", "string"))
                put("st_min", JSONObject().put("type", "integer"))
                put("aparar", JSONObject().put("type", "string"))
            })
            put("required", JSONArray().put("nome").put("peso").put("custo"))
        })
    }
}
