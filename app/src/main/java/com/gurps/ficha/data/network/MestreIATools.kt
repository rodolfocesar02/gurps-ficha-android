package com.gurps.ficha.data.network

import org.json.JSONArray
import org.json.JSONObject

/**
 * Definição das Ferramentas (Tools) do Agente Mestre IA.
 * Contém os Schemas JSON compatíveis com Gemini (Google Native) e OpenAI/DeepSeek.
 */
object MestreIATools {

    const val TOOL_SEARCH_RULES = "consultar_grafo_regras"
    const val TOOL_FILL_SHEET = "fill_character_sheet"
    const val TOOL_NEXUS_ARCANO = "consultar_nexus_arcano"

    /**
     * Retorna a lista de Function Declarations no formato nativo do Gemini.
     */
    fun getGeminiTools(modo: String): JSONArray {
        val functionDeclarations = JSONArray()

        // Ferramenta 1: Pesquisa de Regras (Sempre disponível)
        functionDeclarations.put(JSONObject().apply {
            put("name", TOOL_SEARCH_RULES)
            put("description", "OBRIGATÓRIO: Use esta ferramenta para QUALQUER dúvida de regras, cálculos ou manuais. PROIBIDO responder de cabeça sem consultar o Codex primeiro. Se não encontrar de primeira, tente sinônimos ou páginas específicas.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("query", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Termo de busca ou dúvida técnica. Ex: 'Como funciona o Recuo?', 'Tabela de Dano por Queda'.")
                    })
                    put("pagina", JSONObject().apply {
                        put("type", "INTEGER")
                        put("description", "Página de resultados (cada página traz 5 recortes novos). Use pagina=2 se os primeiros resultados não forem suficientes.")
                    })
                })
                put("required", JSONArray().put("query"))
            })
        })

        // Ferramenta 2: Criação de Fichas (Apenas modo Geração)
        functionDeclarations.put(JSONObject().apply {
            put("name", TOOL_FILL_SHEET)
            put("description", "Preenche a ficha de personagem completa e estruturada. Use esta ferramenta quando o usuário pedir para criar um personagem.")
            put("parameters", getSheetSchemaGemini())
        })

        // Ferramenta 3: Nexus Arcano (Modo Alvo / Gabarito Técnico)
        functionDeclarations.put(JSONObject().apply {
            put("name", TOOL_NEXUS_ARCANO)
            put("description", "OBRIGATÓRIO: Use esta ferramenta para obter o gabarito técnico exato de pré-requisitos de magias. Ela calcula o caminho mais curto de dependências (incluindo requisitos de escolas). Use SEMPRE que o usuário perguntar 'o que preciso para aprender X' ou 'como chego na magia Y'.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("magia_alvo", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "O nome ou ID da magia alvo desejada.")
                    })
                })
                put("required", JSONArray().put("magia_alvo"))
            })
        })

        return JSONArray().put(JSONObject().put("functionDeclarations", functionDeclarations))
    }

    /**
     * Retorna a lista de Tools no formato OpenAI.
     */
    fun getOpenAITools(modo: String): JSONArray {
        val tools = JSONArray()

        tools.put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", TOOL_SEARCH_RULES)
                put("description", "OBRIGATÓRIO: Use esta ferramenta para QUALQUER dúvida de regras, cálculos ou manuais. PROIBIDO responder de cabeça sem consultar o Codex primeiro. Se não encontrar de primeira, tente sinônimos ou páginas específicas.")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("query", JSONObject().apply {
                            put("type", "string")
                            put("description", "Termo de busca ou dúvida técnica. Ex: 'Como funciona o Recuo?', 'Tabela de Dano por Queda'.")
                        })
                        put("pagina", JSONObject().apply {
                            put("type", "integer")
                            put("description", "Página de resultados (cada página traz 5 recortes novos). Use pagina=2 se os primeiros resultados não forem suficientes.")
                        })
                    })
                    put("required", JSONArray().put("query"))
                })
            })
        })

        tools.put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", TOOL_FILL_SHEET)
                put("description", "Preenche a ficha de personagem completa e estruturada. Use esta ferramenta quando o usuário pedir para criar um personagem.")
                put("parameters", getSheetSchemaOpenAI())
            })
        })

        tools.put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", TOOL_NEXUS_ARCANO)
                put("description", "OBRIGATÓRIO: Use esta ferramenta para obter o gabarito técnico exato de pré-requisitos de magias. Ela calcula o caminho mais curto de dependências (incluindo requisitos de escolas). Use SEMPRE que o usuário perguntar 'o que preciso para aprender X' ou 'como chego na magia Y'.")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("magia_alvo", JSONObject().apply {
                            put("type", "string")
                            put("description", "O nome ou ID da magia alvo desejada.")
                        })
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
