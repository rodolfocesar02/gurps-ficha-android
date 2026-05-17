package com.gurps.ficha.domain.tools

import org.json.JSONArray
import org.json.JSONObject

object ForjadorTools {
    const val TOOL_LER_FICHA     = "forjador_ler_ficha"
    const val TOOL_BUSCAR        = "forjador_buscar_catalogo"
    const val TOOL_GPS_MAGIA     = "forjador_gps_magia"

    fun getGeminiTools(): JSONArray {
        val decls = JSONArray()

        decls.put(JSONObject().apply {
            put("name", TOOL_LER_FICHA)
            put("description", "Lê dados da ficha do personagem atual. Use para entender quem é o personagem antes de sugerir algo.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("secao", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Seção a ler: 'atributos', 'vantagens', 'desvantagens', 'pericias', 'magias', 'equipamentos', 'pontos'")
                    })
                })
                put("required", JSONArray().put("secao"))
            })
        })

        decls.put(JSONObject().apply {
            put("name", TOOL_BUSCAR)
            put("description", "Busca itens no catálogo oficial de GURPS. Use para encontrar vantagens, desvantagens, perícias ou magias antes de adicionar.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("tipo", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Tipo a buscar: 'vantagem', 'desvantagem', 'pericia', 'magia', 'tecnica'")
                    })
                    put("query", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Palavra-chave de busca (ex: 'combate', 'fogo', 'furtividade')")
                    })
                })
                put("required", JSONArray().put("tipo").put("query"))
            })
        })

        decls.put(JSONObject().apply {
            put("name", TOOL_GPS_MAGIA)
            put("description", "GPS de Magias: calcula o caminho de pré-requisitos para aprender uma magia alvo. Retorna cadeia de pré-requisitos, próximas ações e bloqueios.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("magia_alvo", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "ID da magia alvo (ex: 'tempestade_de_relampagos')")
                    })
                })
                put("required", JSONArray().put("magia_alvo"))
            })
        })

        return JSONArray().put(JSONObject().put("functionDeclarations", decls))
    }

    fun getOpenAITools(): JSONArray {
        val tools = JSONArray()

        tools.put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", TOOL_LER_FICHA)
                put("description", "Lê dados da ficha do personagem atual.")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("secao", JSONObject().apply {
                            put("type", "string")
                            put("enum", JSONArray().put("atributos").put("vantagens").put("desvantagens")
                                .put("pericias").put("magias").put("equipamentos").put("pontos"))
                        })
                    })
                    put("required", JSONArray().put("secao"))
                })
            })
        })

        tools.put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", TOOL_BUSCAR)
                put("description", "Busca itens no catálogo oficial de GURPS.")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("tipo", JSONObject().apply {
                            put("type", "string")
                            put("enum", JSONArray().put("vantagem").put("desvantagem").put("pericia").put("magia").put("tecnica"))
                        })
                        put("query", JSONObject().put("type", "string"))
                    })
                    put("required", JSONArray().put("tipo").put("query"))
                })
            })
        })

        tools.put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", TOOL_GPS_MAGIA)
                put("description", "GPS de Magias: cadeia de pré-requisitos para uma magia alvo.")
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
}
