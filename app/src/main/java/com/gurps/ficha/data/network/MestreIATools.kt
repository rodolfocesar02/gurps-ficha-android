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
    const val TOOL_REGRAS_MAGIA = "consultar_regras_magia"
    const val TOOL_REGRAS_ARMAS_FOGO = "consultar_regras_armas_fogo"
    const val TOOL_REGRAS_ARTES_MARCIAIS = "consultar_regras_artes_marciais"
    const val TOOL_REGRAS_AQUATICO = "consultar_regras_aquatico"

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

        // Lote 317 — Tool especializada: Magia
        functionDeclarations.put(JSONObject().apply {
            put("name", TOOL_REGRAS_MAGIA)
            put("description", "Busca no LIVRO DE MAGIA do GURPS. Use para qualquer pergunta sobre: magias específicas, escolas de magia, energia/PF para conjurar, pré-requisitos de magias, alquimia, encantamentos, runas, mana, contramágica, dissipação, conjuração, resistência mágica. Sempre prefira esta sobre a genérica quando a pergunta envolver magia.")
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

        // Lote 317 — Tool especializada: Armas de Fogo
        functionDeclarations.put(JSONObject().apply {
            put("name", TOOL_REGRAS_ARMAS_FOGO)
            put("description", "Busca no livro GUN FU (regras de armas de fogo). Use para qualquer pergunta sobre: tiro, pistola, revólver, rifle, espingarda, metralhadora, submetralhadora, mosquete, pólvora, recarga de fogo, cadência de tiro, supressão, recuo, tiro cinematográfico, técnicas com duas armas, disparo rápido, mira. Prefira esta sobre a genérica quando a pergunta envolver disparo de arma de fogo.")
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

        // Lote 317 — Tool especializada: Artes Marciais
        functionDeclarations.put(JSONObject().apply {
            put("name", TOOL_REGRAS_ARTES_MARCIAIS)
            put("description", "Busca no livro ARTES MARCIAIS (técnicas corpo a corpo). Use para qualquer pergunta sobre: técnicas marciais específicas (Ataque Furacão, Joelhada, Mata-leão, Golpe Fulminante, Chave de Braço), estilos marciais, combate desarmado, agarrar, derrubar, imobilizar, judô, karatê, boxe, esgrima, golpes específicos, manobras de combate avançadas. Prefira esta sobre a genérica quando a pergunta envolver luta corporal ou técnica marcial nomeada.")
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

        // Lote 317 — Tool especializada: Aquático
        functionDeclarations.put(JSONObject().apply {
            put("name", TOOL_REGRAS_AQUATICO)
            put("description", "Busca no PYRAMID AQUÁTICO (ambientes submersos). Use para qualquer pergunta sobre: combate subaquático, pressão da água, narcose, descompressão, mergulho, movimentação na água, criaturas aquáticas, respiração subaquática, tiro debaixo d'água, alcance reduzido em água. Prefira esta sobre a genérica quando a pergunta envolver explicitamente água/submersão como ELEMENTO DA REGRA (não apenas estar molhado).")
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

        // Lote 317 — Tool especializada: Magia
        tools.put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", TOOL_REGRAS_MAGIA)
                put("description", "Busca no LIVRO DE MAGIA. Use para qualquer pergunta sobre: magias específicas, escolas de magia, energia/PF para conjurar, pré-requisitos de magias, alquimia, encantamentos, runas, mana, contramágica, dissipação, conjuração, resistência mágica. Sempre prefira esta sobre a genérica quando envolver magia.")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("query", JSONObject().put("type", "string").put("description", "Conceito específico de magia. Máximo 6 palavras."))
                    })
                    put("required", JSONArray().put("query"))
                })
            })
        })

        // Lote 317 — Tool especializada: Armas de Fogo
        tools.put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", TOOL_REGRAS_ARMAS_FOGO)
                put("description", "Busca no livro GUN FU (armas de fogo). Use para: tiro, pistola, revólver, rifle, espingarda, metralhadora, mosquete, pólvora, recarga de fogo, cadência, supressão, recuo, tiro cinematográfico, duas armas, disparo rápido, mira. Prefira esta sobre a genérica quando envolver disparo de arma de fogo.")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("query", JSONObject().put("type", "string").put("description", "Conceito específico de armas de fogo. Máximo 6 palavras."))
                    })
                    put("required", JSONArray().put("query"))
                })
            })
        })

        // Lote 317 — Tool especializada: Artes Marciais
        tools.put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", TOOL_REGRAS_ARTES_MARCIAIS)
                put("description", "Busca no livro ARTES MARCIAIS. Use para: técnicas marciais específicas (Ataque Furacão, Joelhada, Mata-leão, Golpe Fulminante, Chave de Braço), estilos marciais, combate desarmado, agarrar, derrubar, imobilizar, judô, karatê, boxe, esgrima, manobras avançadas. Prefira esta sobre a genérica quando envolver luta corporal ou técnica marcial nomeada.")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("query", JSONObject().put("type", "string").put("description", "Conceito específico de artes marciais. Máximo 6 palavras."))
                    })
                    put("required", JSONArray().put("query"))
                })
            })
        })

        // Lote 317 — Tool especializada: Aquático
        tools.put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", TOOL_REGRAS_AQUATICO)
                put("description", "Busca no PYRAMID AQUÁTICO. Use para: combate subaquático, pressão da água, narcose, descompressão, mergulho, movimentação na água, criaturas aquáticas, respiração subaquática, tiro debaixo d'água, alcance reduzido em água. Prefira esta sobre a genérica quando água/submersão for ELEMENTO DA REGRA (não apenas estar molhado).")
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
