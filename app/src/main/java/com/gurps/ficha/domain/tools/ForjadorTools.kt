package com.gurps.ficha.domain.tools

import org.json.JSONArray
import org.json.JSONObject

object ForjadorTools {
    const val TOOL_LER_FICHA     = "forjador_ler_ficha"
    const val TOOL_BUSCAR        = "forjador_buscar_catalogo"
    const val TOOL_GPS_MAGIA     = "forjador_gps_magia"
    const val TOOL_EDITAR        = "forjador_editar_ficha"

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
                        put("description", "Seção a ler: 'atributos', 'vantagens', 'desvantagens', 'pericias', 'tecnicas', 'magias', 'equipamentos', 'qualidades', 'peculiaridades', 'pontos'")
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

        decls.put(JSONObject().apply {
            put("name", TOOL_EDITAR)
            put("description", "Edita a ficha DIRETAMENTE (aplica na hora, sem confirmação). Use para remover, adicionar ou alterar UM item específico que o jogador pediu. Para remover duplicata, chame uma vez por cópia extra — remove só a última ocorrência do alvo.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("operacao", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "remover | adicionar | alterar")
                    })
                    put("secao", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "atributos | vantagens | desvantagens | pericias | tecnicas | magias | equipamentos | qualidades | peculiaridades")
                    })
                    put("alvo", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "ID/nome do item, OU o atributo (forca/destreza/inteligencia/vitalidade). Ex: 'destemor', 'forca', 'Arco Longo'")
                    })
                    put("valor", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Atributos: novo valor ('14'). Perícia: 'nivel=14;esp=Florestas'. Vantagem: 'nivel=3;custo=15'. Técnica: 'nivel=4;periciaBase=<id>' (auto-escolhe se omitir). Magia: a ferramenta BARRA se faltar pré-requisito (igual ao app) — adicione a cadeia antes; só 'forcar=true' adiciona sem pré-requisito (gatilho narrativo).")
                    })
                })
                put("required", JSONArray().put("operacao").put("secao").put("alvo"))
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
                                .put("pericias").put("tecnicas").put("magias").put("equipamentos")
                                .put("qualidades").put("peculiaridades").put("pontos"))
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

        tools.put(JSONObject().apply {
            put("type", "function")
            put("function", JSONObject().apply {
                put("name", TOOL_EDITAR)
                put("description", "Edita a ficha DIRETAMENTE (aplica na hora). remover/adicionar/alterar UM item. Para remover duplicata, chame 1x por cópia extra (remove só a última ocorrência).")
                put("parameters", JSONObject().apply {
                    put("type", "object")
                    put("properties", JSONObject().apply {
                        put("operacao", JSONObject().apply {
                            put("type", "string")
                            put("enum", JSONArray().put("remover").put("adicionar").put("alterar"))
                        })
                        put("secao", JSONObject().apply {
                            put("type", "string")
                            put("enum", JSONArray().put("atributos").put("vantagens").put("desvantagens").put("pericias")
                                .put("tecnicas").put("magias").put("equipamentos").put("qualidades").put("peculiaridades"))
                        })
                        put("alvo", JSONObject().apply {
                            put("type", "string")
                            put("description", "ID/nome do item OU atributo (forca/destreza/inteligencia/vitalidade)")
                        })
                        put("valor", JSONObject().apply {
                            put("type", "string")
                            put("description", "Atributo: '14'. Perícia: 'nivel=14;esp=Florestas'. Vantagem: 'nivel=3;custo=15'. Técnica: 'nivel=4;periciaBase=<id>'. Magia: barra se faltar pré-requisito; 'forcar=true' p/ adicionar sem (narrativo)")
                        })
                    })
                    put("required", JSONArray().put("operacao").put("secao").put("alvo"))
                })
            })
        })

        return tools
    }
}
