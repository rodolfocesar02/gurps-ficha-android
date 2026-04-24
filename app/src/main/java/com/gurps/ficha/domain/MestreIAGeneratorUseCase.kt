package com.gurps.ficha.domain

import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.data.network.MestreIAClient
import com.gurps.ficha.data.network.MestreIAResponse
import com.gurps.ficha.domain.rules.CharacterRules
import com.gurps.ficha.model.*
import com.gurps.ficha.viewmodel.FichaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class MestreIAGeneratorUseCase(
    private val viewModel: FichaViewModel,
    private val repository: DataRepository
) {
    suspend fun gerarOuAnalisarFicha(
        prompt: String,
        modo: String,
        onStatusUpdate: (String) -> Unit,
        onChunk: (String) -> Unit,
        onResultado: (Boolean, MestreIAClient.ChatResponse) -> Unit
    ) = withContext(Dispatchers.IO) {
        onStatusUpdate("Consultando o Códex para $modo...")
        val catalogoLocal = MestreIAUseCase(viewModel, repository).gerarCatalogoLocal(prompt, viewModel.mestreIAChatHistory)
        
        val fila = listOf(
            Triple(com.gurps.ficha.BuildConfig.MESTRE_IA_DEEPSEEK_URL, com.gurps.ficha.BuildConfig.MESTRE_IA_DEEPSEEK_KEY, com.gurps.ficha.BuildConfig.MESTRE_IA_DEEPSEEK_MODEL),
            Triple(com.gurps.ficha.BuildConfig.MESTRE_IA_LITE_1_URL, com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_KEY, "gemini-1.5-pro")
        )

        var sucesso = false
        for (config in fila) {
            if (config.second.isBlank()) continue
            onStatusUpdate("Mestre ${if (config.third.contains("gemini")) "Arcano" else "Forjador"} está criando...")

            try {
                val response = MestreIAClient.perguntarAoMestre(
                    baseUrl = config.first, apiKey = config.second, workspaceSlug = config.third,
                    prompt = prompt, history = viewModel.mestreIAChatHistory.takeLast(5).map { it.role to it.text },
                    contextoPersonagem = viewModel.personagem.toJson(), catalogo = catalogoLocal.catalogo,
                    modo = modo, onChunk = onChunk
                )

                if (!response.text.contains("Erro de API")) {
                    onResultado(true, response)
                    sucesso = true
                    break
                }
            } catch (e: Exception) {
                Log.e("MestreIA", "Falha no Gerador: ${e.message}")
            }
        }
        if (!sucesso) onResultado(false, MestreIAClient.ChatResponse("Erro: Falha na conexão com os forjadores."))
    }

    fun integrarRespostaNaFicha(ficha: MestreIAResponse) {
        viewModel.atualizarNome(ficha.nome)
        viewModel.atualizarHistorico(ficha.historico)
        viewModel.atualizarAparencia(ficha.aparencia ?: "")
        
        viewModel.atualizarForca(ficha.atributos.st)
        viewModel.atualizarDestreza(ficha.atributos.dx)
        viewModel.atualizarInteligencia(ficha.atributos.iq)
        viewModel.atualizarVitalidade(ficha.atributos.ht)

        ficha.vantagens.forEach { v -> adicionarVantagem(v.nome, v.descricao ?: "", v.custo ?: 0) }
        ficha.desvantagens.forEach { d -> adicionarVantagem(d.nome, d.descricao ?: "", d.custo ?: 0) }

        ficha.pericias.forEach { p -> adicionarPericia(p.nome, p.nivel) }
        
        ficha.magias.forEach { m -> 
            val nomeLimpo = limparNome(m.nome)
            val def = repository.magias.find { limparNome(it.nome) == nomeLimpo }
            if (def != null) {
                viewModel.adicionarMagia(def)
            } else {
                viewModel.adicionarQualidade("Magia: ${m.nome} (${m.custo ?: 0} fp)")
            }
        }

        ficha.equipamentos.forEach { eq ->
            viewModel.adicionarEquipamento(Equipamento(
                nome = eq.nome, peso = eq.peso, custo = eq.custo, 
                quantidade = eq.quantidade, armaDanoRaw = eq.dano,
                armaStMinimo = eq.st_min, notas = if ((eq.rd ?: 0) > 0) "RD: ${eq.rd}" else ""
            ))
        }
    }

    private fun adicionarVantagem(nomeFull: String, desc: String, custo: Int) {
        val nomeLimpo = limparNome(nomeFull)
        
        // 1. Tenta achar no catálogo de vantagens
        val vDef = repository.vantagens.find { limparNome(it.nome) == nomeLimpo }
        if (vDef != null) {
            viewModel.adicionarVantagem(vDef, custo = if (custo != 0) custo else vDef.getCustoBase(), desc = desc)
            return
        }

        // 2. Tenta achar no catálogo de desvantagens
        val dDef = repository.desvantagens.find { limparNome(it.nome) == nomeLimpo }
        if (dDef != null) {
            viewModel.adicionarDesvantagem(dDef, custo = if (custo != 0) custo else dDef.getCustoBase(), desc = desc)
            return
        }

        // 3. Se não achou, adiciona como Qualidade (Vantagem Customizada) ou Peculiaridade (Desvantagem Customizada)
        if (custo >= 0) {
            viewModel.adicionarQualidade("$nomeFull ($custo pts): $desc")
        } else {
            viewModel.adicionarPeculiaridade("$nomeFull ($custo pts): $desc")
        }
    }

    private fun adicionarPericia(nomeFull: String, nivel: Int) {
        val nomeLimpo = limparNome(nomeFull)
        val def = repository.pericias.find { limparNome(it.nome) == nomeLimpo }
        
        if (def != null) {
            val pts = CharacterRules.calcularPontosParaNivel(
                Dificuldade.fromSigla(def.dificuldadeFixa),
                viewModel.personagem.getAtributo(def.atributoBase),
                nivel
            )
            viewModel.adicionarPericia(def, pts)
        } else {
            // Perícia Customizada (Adiciona como Qualidade para não perder a info)
            viewModel.adicionarQualidade("Perícia: $nomeFull (NH $nivel)")
        }
    }

    private fun limparNome(nome: String): String {
        return nome.lowercase()
            .replace(Regex("\\(.*?\\)"), "") // Remove parênteses
            .replace(Regex("\\d+"), "")      // Remove números
            .trim()
            .replace(" ", "")
    }
}
