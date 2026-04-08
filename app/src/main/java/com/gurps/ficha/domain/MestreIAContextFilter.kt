package com.gurps.ficha.domain

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.gurps.ficha.model.Personagem

/**
 * Utilitário para filtrar e resumir o contexto do personagem enviado para a IA.
 * Implementa a Economia de Contexto (Lote 53).
 */
object MestreIAContextFilter {
    private val gson = Gson()

    fun gerarContexto(personagem: Personagem, modo: String): String {
        return try {
            val json = JsonObject()
            
            // 1. Informações Básicas (Obrigatórias)
            json.addProperty("nome", personagem.nome)
            json.addProperty("pontosIniciais", personagem.pontosIniciais)
            json.addProperty("pontosGastos", personagem.pontosGastos)
            // Lote 53: Incluir saldo de pontos (pode ser negativo)
            json.addProperty("pontosRestantes", personagem.pontosRestantes)
            
            // 2. Atributos Primários
            val atributos = JsonObject()
            atributos.addProperty("st", personagem.st)
            atributos.addProperty("dx", personagem.dx)
            atributos.addProperty("iq", personagem.iq)
            atributos.addProperty("ht", personagem.ht)
            json.add("atributos", atributos)

            // 3. Atributos Secundários / Defesas (Essencial para contexto de combate)
            val defesas = JsonObject()
            defesas.addProperty("esquiva", personagem.esquiva)
            defesas.addProperty("hp", personagem.pontosVida)
            defesas.addProperty("fadiga", personagem.pontosFadiga)
            json.add("defesas_ativas", defesas)

            // 4. Histórico (Truncado conforme solicitação do usuário no Lote 53)
            val limiteHistoria = if (modo == "analise" || modo == "geracao") 1000 else 300
            val hist = personagem.historico.trim()
            if (hist.isNotBlank()) {
                json.addProperty("historico_resumo", if (hist.length > limiteHistoria) {
                    hist.take(limiteHistoria) + "... [LIMITE DE CONTEXTO ATINGIDO]"
                } else hist)
            }

            // 5. Listas (Resumidas para economizar tokens significativamente)
            if (modo == "conversa") {
                // Apenas nomes: IA já deve conhecer o resto via RAG ou base de conhecimento dela
                json.add("vantagens", gson.toJsonTree(personagem.vantagens.map { it.nome }))
                json.add("desvantagens", gson.toJsonTree(personagem.desvantagens.map { it.nome }))
                json.add("pericias", gson.toJsonTree(personagem.pericias.map { "${it.nome} (NH:${it.calcularNivel(personagem)})" }))
                json.add("magias", gson.toJsonTree(personagem.magias.map { it.nome }))
                json.add("equipamentos", gson.toJsonTree(personagem.equipamentos.map { it.nome }))
            } else {
                // Modos Geração/Análise: Mais detalhes técnicos para precisão cirúrgica
                json.add("vantagens", gson.toJsonTree(personagem.vantagens.map { 
                    mapOf("nome" to it.nome, "nivel" to it.nivel, "custo" to it.custoFinal) 
                }))
                json.add("pericias", gson.toJsonTree(personagem.pericias.map { 
                    mapOf("nome" to it.nome, "nh" to it.calcularNivel(personagem), "pts" to it.pontosGastos) 
                }))
                json.add("equipamentos", gson.toJsonTree(personagem.equipamentos.map { 
                    mapOf("nome" to it.nome, "peso" to it.peso, "qtd" to it.quantidade) 
                }))
            }

            gson.toJson(json)
        } catch (e: Exception) {
            // Fallback seguro: Envia o JSON completo se o filtro falhar
            android.util.Log.e("MestreIA", "Falha no filtro de contexto: ${e.message}")
            personagem.toJson()
        }
    }
}
