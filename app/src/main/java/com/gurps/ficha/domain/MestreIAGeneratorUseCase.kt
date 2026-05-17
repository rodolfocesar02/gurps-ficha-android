package com.gurps.ficha.domain

import android.util.Log
import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.data.network.MestreIAClient
import com.gurps.ficha.data.network.MestreIAItem
import com.gurps.ficha.data.network.MestreIAPromptsForjador
import com.gurps.ficha.data.network.MestreIAResponse
import com.gurps.ficha.domain.rules.CharacterRules
import com.gurps.ficha.model.*
import com.gurps.ficha.viewmodel.FichaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
        val catalogoLocal = MestreIAUseCase(viewModel, repository).gerarCatalogoDireto(prompt, viewModel.mestreIAChatHistory)

        // Lote A: injeta catálogo real de IDs no prompt do Forjador
        // vantagens já inclui as de Artes Marciais (merge feito no CatalogLoaders)
        // periciasSuplementares (AM) são tipo diferente — incluídas só no catálogo textual
        // Lote D: inclui budget de pontos para o modelo respeitar o limite
        val pontosIniciais = viewModel.personagem.pontosIniciais
        val promptForjador = MestreIAPromptsForjador.gerarPromptComCatalogo(
            vantagens    = repository.vantagens.map { it.id to it.nome },
            desvantagens = repository.desvantagens.map { it.id to it.nome },
            pericias     = repository.pericias.map { it.id to it.nome } +
                           repository.periciasSuplementares.map { it.id to it.nome },
            magias       = repository.magias.map { it.id to it.nome },
            pontosIniciais = pontosIniciais
        )

        val fila = listOf(
            Triple(com.gurps.ficha.BuildConfig.MESTRE_IA_DEEPSEEK_URL, com.gurps.ficha.BuildConfig.MESTRE_IA_DEEPSEEK_KEY, com.gurps.ficha.BuildConfig.MESTRE_IA_DEEPSEEK_MODEL),
            Triple(com.gurps.ficha.BuildConfig.MESTRE_IA_LITE_1_URL, com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_KEY, com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_3_1_PRO)
        )

        var sucesso = false
        for (config in fila) {
            if (config.second.isBlank()) continue
            onStatusUpdate("Mestre ${if (config.third.contains("gemini")) "Arcano" else "Forjador"} está criando...")

            try {
                val response = MestreIAClient.perguntarAoMestre(
                    baseUrl = config.first, apiKey = config.second, workspaceSlug = config.third,
                    prompt = prompt,
                    history = viewModel.mestreIAChatHistory.takeLast(5).map { it.role to it.text },
                    contextoPersonagem = viewModel.personagem.toJson(),
                    catalogo = catalogoLocal.catalogo,
                    modo = modo,
                    promptSistema = promptForjador,
                    onChunk = onChunk
                )

                if (!response.text.contains("Erro de API")) {
                    onResultado(true, response)
                    sucesso = true
                    break
                }
            } catch (e: Exception) {
                Log.e("MestreIA_Forjador", "Falha no Gerador: ${e.message}")
            }
        }
        if (!sucesso) onResultado(false, MestreIAClient.ChatResponse("Erro: Falha na conexão com os forjadores."))
    }

    fun validarBudget(ficha: MestreIAResponse): String? {
        val st = ficha.atributos.st; val dx = ficha.atributos.dx
        val iq = ficha.atributos.iq; val ht = ficha.atributos.ht
        val custoAtributos = ((st - 10).coerceAtLeast(0) * 10) +
                             ((dx - 10).coerceAtLeast(0) * 20) +
                             ((iq - 10).coerceAtLeast(0) * 20) +
                             ((ht - 10).coerceAtLeast(0) * 10)
        val custoVantagens    = ficha.vantagens.sumOf    { it.custo ?: 0 }
        val custoDesvantagens = ficha.desvantagens.sumOf { it.custo ?: 0 } // já negativo
        val custoPericias     = ficha.pericias.sumOf     { it.nivel * 2 }  // estimativa
        val total = custoAtributos + custoVantagens + custoDesvantagens + custoPericias
        val max = viewModel.personagem.pontosIniciais
        return if (total > max) {
            Log.w("MestreIA_Forjador", "Budget excedido: $total pts (máximo: $max pts)")
            "⚠️ Ficha usa ~$total pts (máximo: $max pts)"
        } else null
    }

    fun integrarRespostaNaFicha(ficha: MestreIAResponse) {
        viewModel.atualizarNome(ficha.nome)
        viewModel.atualizarHistorico(ficha.historico)
        viewModel.atualizarAparencia(ficha.aparencia ?: "")

        viewModel.atualizarForca(ficha.atributos.st)
        viewModel.atualizarDestreza(ficha.atributos.dx)
        viewModel.atualizarInteligencia(ficha.atributos.iq)
        viewModel.atualizarVitalidade(ficha.atributos.ht)

        ficha.vantagens.forEach    { v -> adicionarVantagem(v, v.descricao ?: "", v.custo ?: 0) }
        ficha.desvantagens.forEach { d -> adicionarVantagem(d, d.descricao ?: "", d.custo ?: 0) }
        ficha.pericias.forEach     { p -> adicionarPericia(p, p.nivel) }
        ficha.magias.forEach       { m -> adicionarMagia(m) }

        ficha.equipamentos.forEach { eq ->
            viewModel.adicionarEquipamento(Equipamento(
                nome = eq.nome, peso = eq.peso, custo = eq.custo,
                quantidade = eq.quantidade, armaDanoRaw = eq.dano,
                armaStMinimo = eq.st_min,
                notas = if ((eq.rd ?: 0) > 0) "RD: ${eq.rd}" else ""
            ))
        }
    }

    private fun adicionarVantagem(item: MestreIAItem, desc: String, custo: Int) {
        // 1. Lookup por ID direto — caminho feliz (Lote A)
        if (!item.id.isNullOrBlank()) {
            repository.vantagens.find { it.id == item.id }?.let {
                Log.d("MestreIA_Forjador", "Vantagem por ID: ${item.id}")
                viewModel.adicionarVantagem(it, custo = if (custo != 0) custo else it.getCustoBase(), desc = desc)
                return
            }
            repository.desvantagens.find { it.id == item.id }?.let {
                Log.d("MestreIA_Forjador", "Desvantagem por ID: ${item.id}")
                viewModel.adicionarDesvantagem(it, custo = if (custo != 0) custo else it.getCustoBase(), desc = desc)
                return
            }
        }

        // 2. Fallback: fuzzy match por nome (comportamento legado)
        val nomeLimpo = limparNome(item.nome)
        repository.vantagens.find { limparNome(it.nome) == nomeLimpo }?.let {
            Log.d("MestreIA_Forjador", "Vantagem por nome fuzzy: ${item.nome}")
            viewModel.adicionarVantagem(it, custo = if (custo != 0) custo else it.getCustoBase(), desc = desc)
            return
        }
        repository.desvantagens.find { limparNome(it.nome) == nomeLimpo }?.let {
            Log.d("MestreIA_Forjador", "Desvantagem por nome fuzzy: ${item.nome}")
            viewModel.adicionarDesvantagem(it, custo = if (custo != 0) custo else it.getCustoBase(), desc = desc)
            return
        }

        // 3. Não achou — Qualidade ou Peculiaridade
        Log.w("MestreIA_Forjador", "Não encontrado no catálogo, fallback: ${item.id ?: item.nome}")
        if (custo >= 0) viewModel.adicionarQualidade("${item.nome.ifBlank { item.id ?: "?" }} ($custo pts): $desc")
        else            viewModel.adicionarPeculiaridade("${item.nome.ifBlank { item.id ?: "?" }} ($custo pts): $desc")
    }

    private fun adicionarPericia(item: MestreIAItem, nivel: Int) {
        // 1. Lookup por ID direto (Lote A)
        if (!item.id.isNullOrBlank()) {
            repository.pericias.find { it.id == item.id }?.let { def ->
                Log.d("MestreIA_Forjador", "Perícia por ID: ${item.id}")
                val pts = CharacterRules.calcularPontosParaNivel(
                    Dificuldade.fromSigla(def.dificuldadeFixa),
                    viewModel.personagem.getAtributo(def.atributoBase),
                    nivel
                )
                viewModel.adicionarPericia(def, pts)
                return
            }
        }

        // 2. Fallback: fuzzy match por nome
        val nomeLimpo = limparNome(item.nome)
        repository.pericias.find { limparNome(it.nome) == nomeLimpo }?.let { def ->
            Log.d("MestreIA_Forjador", "Perícia por nome fuzzy: ${item.nome}")
            val pts = CharacterRules.calcularPontosParaNivel(
                Dificuldade.fromSigla(def.dificuldadeFixa),
                viewModel.personagem.getAtributo(def.atributoBase),
                nivel
            )
            viewModel.adicionarPericia(def, pts)
            return
        }

        Log.w("MestreIA_Forjador", "Perícia não encontrada, fallback Qualidade: ${item.id ?: item.nome}")
        viewModel.adicionarQualidade("Perícia: ${item.nome.ifBlank { item.id ?: "?" }} (NH $nivel)")
    }

    private fun adicionarMagia(item: MestreIAItem) {
        // 1. Lookup por ID direto (Lote A)
        if (!item.id.isNullOrBlank()) {
            repository.magias.find { it.id == item.id }?.let {
                Log.d("MestreIA_Forjador", "Magia por ID: ${item.id}")
                viewModel.adicionarMagia(it)
                return
            }
        }

        // 2. Fallback: fuzzy match por nome
        val nomeLimpo = limparNome(item.nome)
        repository.magias.find { limparNome(it.nome) == nomeLimpo }?.let {
            Log.d("MestreIA_Forjador", "Magia por nome fuzzy: ${item.nome}")
            viewModel.adicionarMagia(it)
            return
        }

        Log.w("MestreIA_Forjador", "Magia não encontrada, fallback Qualidade: ${item.id ?: item.nome}")
        viewModel.adicionarQualidade("Magia: ${item.nome.ifBlank { item.id ?: "?" }} (${item.custo ?: 0} fp)")
    }

    private fun limparNome(nome: String): String =
        nome.lowercase()
            .replace(Regex("\\(.*?\\)"), "")
            .replace(Regex("\\d+"), "")
            .trim()
            .replace(" ", "")
}
