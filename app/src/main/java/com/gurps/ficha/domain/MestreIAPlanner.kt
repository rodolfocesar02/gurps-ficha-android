package com.gurps.ficha.domain

import com.gurps.ficha.data.network.MestreIAClient
import org.json.JSONObject
import org.json.JSONArray

/**
 * MestreIAPlanner - O "Batedor" de Inteligência.
 * Responsável por transformar perguntas leigas em termos técnicos de GURPS.
 */
object MestreIAPlanner {

    data class PlanoDeBusca(
        val termos: List<String>,
        val categorias: List<String>
    )

    /**
     * Planeja a busca consultando uma IA rápida (Flash-Lite).
     */
    suspend fun planejarBusca(
        pergunta: String,
        iaUrl: String,
        iaKey: String,
        iaModel: String
    ): PlanoDeBusca {
        val promptSistema = """
            Você é o PLANEJADOR DE BUSCA do Mestre IA (GURPS 4ª Edição).
            Sua tarefa é ler a dúvida do usuário e extrair os TERMOS TÉCNICOS exatos e CATEGORIAS dos manuais.
            
            Exemplo: 
            Entrada: "cair na piscina de armadura"
            Saída: { "termos": ["Combate Subaquático", "Natação", "Carga", "Impedimento"], "categorias": ["Regra", "Perícia"] }
            
            Entrada: "tiro de fuzil em quem está mergulhado"
            Saída: { "termos": ["Combate Subaquático", "Armas de Fogo", "Penalidades de Distância", "Líquidos"], "categorias": ["Regra", "Arma"] }

            RESPONDA APENAS O JSON, SEM EXPLICAÇÕES.
        """.trimIndent()

        val promptUsuario = "Dúvida do Usuário: \"$pergunta\""
        android.util.Log.i("MestreIA_Planner", "Iniciando planejamento de busca para: $pergunta")

        return try {
            val resposta = MestreIAClient.perguntarAoMestre(
                baseUrl = iaUrl,
                apiKey = iaKey,
                workspaceSlug = iaModel,
                prompt = "$promptSistema\n\n$promptUsuario",
                history = emptyList(),
                contextoPersonagem = "{}",
                catalogo = MestreIAClient.CatalogoNomes(),
                modo = "planejamento"
            )

            // Extração manual do JSON para evitar conflito de tipos
            val textoIA = resposta.text
            val inicio = textoIA.indexOf("{")
            val fim = textoIA.lastIndexOf("}")
            
            if (inicio != -1 && fim != -1 && fim > inicio) {
                val jsonStr = textoIA.substring(inicio, fim + 1)
                val json = JSONObject(jsonStr)
                val termos = mutableListOf<String>()
                val cats = mutableListOf<String>()

                json.optJSONArray("termos")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        termos.add(arr.getString(i))
                    }
                }

                json.optJSONArray("categorias")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        cats.add(arr.getString(i))
                    }
                }
                
                android.util.Log.i("MestreIA_Planner", "ESTRATÉGIA DEFINIDA: Termos=$termos | Categorias=$cats")
                PlanoDeBusca(termos.distinct(), cats.distinct())
            } else {
                android.util.Log.w("MestreIA_Planner", "Falha ao extrair JSON da IA. Usando fallback.")
                fallback(pergunta)
            }
        } catch (e: Exception) {
            android.util.Log.e("MestreIA_Planner", "Erro no Planejador: ${e.message}")
            fallback(pergunta)
        }
    }

    private fun fallback(pergunta: String): PlanoDeBusca {
        return PlanoDeBusca(pergunta.split(" ").filter { it.length > 3 }, listOf("Regra"))
    }
}
