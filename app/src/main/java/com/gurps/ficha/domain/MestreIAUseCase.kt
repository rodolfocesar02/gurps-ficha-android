package com.gurps.ficha.domain

import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.data.network.MestreIAResponse
import com.gurps.ficha.viewmodel.FichaViewModel

/**
 * Atua como o "Juiz" entre a IA e a Ficha Real.
 * Valida os nomes sugeridos pela IA contra os JSONs oficiais do App.
 * Possui busca fuzzy para equiparar nomes semelhantes.
 */
class MestreIAUseCase(
    private val viewModel: FichaViewModel,
    private val repository: DataRepository
) {

    /**
     * Calcula a "distância" entre dois textos normalizados.
     * Retorna um valor entre 0.0 (totalmente diferente) e 1.0 (idêntico).
     * Usa a técnica de maior subsequência comum (LCS simplificado).
     */
    private fun similaridade(a: String, b: String): Double {
        val na = a.trim().lowercase().replace(Regex("[^a-záàâãéèêíïóôõúüç\\s]"), "").replace("\\s+".toRegex(), " ")
        val nb = b.trim().lowercase().replace(Regex("[^a-záàâãéèêíïóôõúüç\\s]"), "").replace("\\s+".toRegex(), " ")
        if (na == nb) return 1.0
        if (na.isEmpty() || nb.isEmpty()) return 0.0

        // Verifica se um contém o outro
        if (na.contains(nb) || nb.contains(na)) return 0.85

        // Distância de Levenshtein simplificada para textos curtos
        val maxLen = maxOf(na.length, nb.length)
        if (maxLen == 0) return 1.0
        
        val dp = Array(na.length + 1) { IntArray(nb.length + 1) }
        for (i in 0..na.length) dp[i][0] = i
        for (j in 0..nb.length) dp[0][j] = j
        for (i in 1..na.length) {
            for (j in 1..nb.length) {
                val cost = if (na[i - 1] == nb[j - 1]) 0 else 1
                dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
            }
        }
        return 1.0 - (dp[na.length][nb.length].toDouble() / maxLen)
    }

    fun integrarRespostaNaFicha(resposta: MestreIAResponse) {
        android.util.Log.d("MestreIA", "=== Integrando resposta na ficha ===")
        
        // 1. Atualizar Nome e Atributos
        viewModel.atualizarNome(resposta.nome)
        viewModel.definirBasesAtributosPrimarios(
            forcaBase = resposta.atributos.st,
            destrezaBase = resposta.atributos.dx,
            inteligenciaBase = resposta.atributos.iq,
            vitalidadeBase = resposta.atributos.ht
        )
        android.util.Log.d("MestreIA", "Nome: ${resposta.nome}, ST=${resposta.atributos.st}, DX=${resposta.atributos.dx}, IQ=${resposta.atributos.iq}, HT=${resposta.atributos.ht}")

        // 2. Integrar Vantagens (com busca fuzzy)
        var vantagensAceitas = 0
        resposta.vantagens.forEach { nomeSugerido ->
            // Busca exata primeiro
            var definicaoOficial = repository.vantagens.firstOrNull { 
                it.nome.trim().equals(nomeSugerido.trim(), ignoreCase = true) 
            }
            // Se não achou exato, tenta fuzzy (similaridade >= 80%)
            if (definicaoOficial == null) {
                val melhorMatch = repository.vantagens
                    .map { it to similaridade(it.nome, nomeSugerido) }
                    .filter { it.second >= 0.80 }
                    .maxByOrNull { it.second }
                if (melhorMatch != null) {
                    definicaoOficial = melhorMatch.first
                    android.util.Log.d("MestreIA", "🔄 Fuzzy match vantagem: '$nomeSugerido' → '${definicaoOficial.nome}' (${(melhorMatch.second * 100).toInt()}%)")
                }
            }
            if (definicaoOficial != null) {
                viewModel.adicionarVantagem(definicaoOficial)
                vantagensAceitas++
                android.util.Log.d("MestreIA", "✅ Vantagem aceita: ${definicaoOficial.nome}")
            } else {
                android.util.Log.w("MestreIA", "❌ Vantagem rejeitada: $nomeSugerido")
            }
        }
        android.util.Log.d("MestreIA", "Vantagens: $vantagensAceitas/${resposta.vantagens.size} aceitas")

        // 3. Integrar Desvantagens (com busca fuzzy)
        var desvantagensAceitas = 0
        resposta.desvantagens.forEach { nomeSugerido ->
            var definicaoOficial = repository.desvantagens.firstOrNull { 
                it.nome.trim().equals(nomeSugerido.trim(), ignoreCase = true) 
            }
            if (definicaoOficial == null) {
                val melhorMatch = repository.desvantagens
                    .map { it to similaridade(it.nome, nomeSugerido) }
                    .filter { it.second >= 0.80 }
                    .maxByOrNull { it.second }
                if (melhorMatch != null) {
                    definicaoOficial = melhorMatch.first
                    android.util.Log.d("MestreIA", "🔄 Fuzzy match desvantagem: '$nomeSugerido' → '${definicaoOficial.nome}' (${(melhorMatch.second * 100).toInt()}%)")
                }
            }
            if (definicaoOficial != null) {
                viewModel.adicionarDesvantagem(definicaoOficial)
                desvantagensAceitas++
                android.util.Log.d("MestreIA", "✅ Desvantagem aceita: ${definicaoOficial.nome}")
            } else {
                android.util.Log.w("MestreIA", "❌ Desvantagem rejeitada: $nomeSugerido")
            }
        }
        android.util.Log.d("MestreIA", "Desvantagens: $desvantagensAceitas/${resposta.desvantagens.size} aceitas")

        // 4. Integrar Perícias (com busca fuzzy)
        var periciasAceitas = 0
        resposta.pericias.forEach { periciaSugerida ->
            var definicaoOficial = repository.pericias.firstOrNull { 
                it.nome.trim().equals(periciaSugerida.nome.trim(), ignoreCase = true) 
            }
            if (definicaoOficial == null) {
                val melhorMatch = repository.pericias
                    .map { it to similaridade(it.nome, periciaSugerida.nome) }
                    .filter { it.second >= 0.80 }
                    .maxByOrNull { it.second }
                if (melhorMatch != null) {
                    definicaoOficial = melhorMatch.first
                    android.util.Log.d("MestreIA", "🔄 Fuzzy match perícia: '${periciaSugerida.nome}' → '${definicaoOficial.nome}' (${(melhorMatch.second * 100).toInt()}%)")
                }
            }
            if (definicaoOficial != null) {
                viewModel.adicionarPericia(definicaoOficial)
                periciasAceitas++
                android.util.Log.d("MestreIA", "✅ Perícia aceita: ${definicaoOficial.nome}")
            } else {
                android.util.Log.w("MestreIA", "❌ Perícia rejeitada: ${periciaSugerida.nome}")
            }
        }
        android.util.Log.d("MestreIA", "Perícias: $periciasAceitas/${resposta.pericias.size} aceitas")

        // 5. Integrar Magias (com busca fuzzy)
        var magiasAceitas = 0
        resposta.magias.forEach { nomeSugerido ->
            var definicaoOficial = repository.magias.firstOrNull {
                it.nome.trim().equals(nomeSugerido.trim(), ignoreCase = true)
            }
            if (definicaoOficial == null) {
                val melhorMatch = repository.magias
                    .map { it to similaridade(it.nome, nomeSugerido) }
                    .filter { it.second >= 0.80 }
                    .maxByOrNull { it.second }
                if (melhorMatch != null) {
                    definicaoOficial = melhorMatch.first
                    android.util.Log.d("MestreIA", "🔄 Fuzzy match magia: '$nomeSugerido' → '${definicaoOficial.nome}' (${(melhorMatch.second * 100).toInt()}%)")
                }
            }
            if (definicaoOficial != null) {
                val erro = viewModel.adicionarMagia(definicaoOficial, ignorarPreRequisito = true)
                if (erro == null) {
                    magiasAceitas++
                    android.util.Log.d("MestreIA", "✅ Magia aceita: ${definicaoOficial.nome}")
                } else {
                    android.util.Log.w("MestreIA", "⚠️ Magia não adicionada: ${definicaoOficial.nome} ($erro)")
                }
            } else {
                android.util.Log.w("MestreIA", "❌ Magia rejeitada: $nomeSugerido")
            }
        }
        if (resposta.magias.isNotEmpty()) {
            android.util.Log.d("MestreIA", "Magias: $magiasAceitas/${resposta.magias.size} aceitas")
        }

        // 6. Qualidades
        resposta.qualidades.forEach { qualidade ->
            if (qualidade.isNotBlank()) {
                viewModel.adicionarQualidade(qualidade.trim())
                android.util.Log.d("MestreIA", "✅ Qualidade: $qualidade")
            }
        }

        // 7. Peculiaridades
        resposta.peculiaridades.forEach { peculiaridade ->
            if (peculiaridade.isNotBlank()) {
                viewModel.adicionarPeculiaridade(peculiaridade.trim())
                android.util.Log.d("MestreIA", "✅ Peculiaridade: $peculiaridade")
            }
        }

        // 8. Aparência e Histórico
        if (resposta.aparencia.isNotBlank()) {
            viewModel.atualizarAparencia(resposta.aparencia.trim())
            android.util.Log.d("MestreIA", "✅ Aparência preenchida")
        }
        if (resposta.historico.isNotBlank()) {
            viewModel.atualizarHistorico(resposta.historico.trim())
            android.util.Log.d("MestreIA", "✅ Histórico preenchido")
        }

        android.util.Log.d("MestreIA", "=== Integração concluída ===")
    }
}
