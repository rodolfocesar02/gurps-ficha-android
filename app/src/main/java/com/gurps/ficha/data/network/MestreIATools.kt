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

    /**
     * Retorna a lista de Function Declarations no formato nativo do Gemini.
     */
    fun getGeminiTools(modo: String): JSONArray {
        val functionDeclarations = JSONArray()

        // Ferramenta 1: Busca Direta no Códex (Lote 271)
        functionDeclarations.put(JSONObject().apply {
            put("name", TOOL_MANUAL_DIRETO)
            put("description", "Busca páginas completas no Códex de GURPS. Use queries CURTAS e ESPECÍFICAS por conceito isolado. Decomponha a pergunta em partes e busque cada uma separadamente. Ex: 'aparar chicote penalidade', 'ajoelhado defesa modificador'. NÃO use queries longas com a pergunta inteira.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("query", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Conceito técnico isolado para busca. Máximo 6 palavras. Ex: 'escavar solo velocidade', 'queda cavalo dano'.")
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
                put("description", "Busca páginas completas no Códex de GURPS. Use queries CURTAS e ESPECÍFICAS por conceito isolado. Decomponha a pergunta em partes e busque cada uma separadamente. Ex: 'aparar chicote penalidade', 'ajoelhado defesa modificador'. NÃO use queries longas com a pergunta inteira.")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("query", JSONObject().put("type", "string").put("description", "Conceito técnico isolado. Máximo 6 palavras."))
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
                        put("secao", JSONObject().put("type", "string").put("enum", JSONArray().put("atributos").put("vantagens").put("pericias").put("status").put("armas").put("armaduras")))
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
