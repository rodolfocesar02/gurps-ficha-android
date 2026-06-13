package com.gurps.ficha.domain.saga

import android.util.Log
import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.data.storage.CampaignFactEntity
import com.gurps.ficha.data.storage.SagaDao
import com.gurps.ficha.domain.filters.CatalogFilters
import com.gurps.ficha.domain.tools.ForjadorToolExecutor
import org.json.JSONArray
import org.json.JSONObject

/**
 * Lote 353 (Saga A4): roteador de execução das tools do Narrador, no padrão do
 * ForjadorToolExecutor (nome + args JSON → resultado String para o loop de tool-use).
 *
 * Implementação REAL neste lote: registrar_fato, consultar_mundo,
 * inspecionar_personagem (delega ao ForjadorToolExecutor.lerSecao) e
 * localizar_no_codex/ler_pagina (delegam ao motor do Auditor via DataRepository).
 * As demais devolvem {"erro":"nao_implementado","tool":...} — os executores reais
 * chegam nos lotes A5 (rolagem), B (combate), C (mundo) e D (XP).
 *
 * Dependências anuláveis de propósito: cada tool valida o que precisa e devolve
 * erro JSON em vez de quebrar — permite testar o roteamento com o mínimo de setup.
 */
class NarradorToolExecutor(
    private val sagaDao: SagaDao?,
    private val repository: DataRepository? = null,
    private val forjador: ForjadorToolExecutor? = null,
    private val rollBridge: RollBridge? = null
) {
    /**
     * Lote 354 (A5): ponte INTERATIVA da rolagem. pedir_rolagem suspende o loop da IA
     * até o jogador tocar o dado na UI. Implementada pelo FichaSagaDelegate (que tem
     * acesso à ficha p/ resolver o NH e ao mesmo caminho de rolagem da TabRolagem).
     * Retorna o JSON {soma, alvo, margem, resultado, critico} pronto para o loop.
     */
    interface RollBridge {
        suspend fun pedirRolagem(pericia: String, mods: List<Pair<String, Int>>, motivo: String): String
    }

    /** Campanha ativa — obrigatória para fatos. Setada ao abrir/criar campanha (A5). */
    var campanhaId: Long = 0L

    /** Cena aberta — carimba os fatos registrados. */
    var cenaAtualId: Long? = null

    suspend fun executar(nome: String, argsJson: String): String {
        Log.d("Narrador_Tools", "Executando tool: $nome | args: ${argsJson.take(300)}")
        val args = try {
            JSONObject(argsJson.ifBlank { "{}" })
        } catch (e: Exception) {
            return erro("args_invalidos", "JSON de argumentos inválido para $nome")
        }
        val resultado = try {
            when (nome) {
                NarradorTools.TOOL_REGISTRAR_FATO -> registrarFato(args)
                NarradorTools.TOOL_CONSULTAR_MUNDO -> consultarMundo(args)
                NarradorTools.TOOL_INSPECIONAR_PERSONAGEM -> inspecionarPersonagem(args)
                NarradorTools.TOOL_DEFINIR_CENA -> definirCena(args)
                NarradorTools.TOOL_PEDIR_ROLAGEM -> pedirRolagem(args)
                NarradorTools.TOOL_LOCALIZAR -> localizarNoCodex(args)
                NarradorTools.TOOL_LER -> lerPagina(args)
                in NarradorTools.TODAS -> {
                    Log.w("Narrador_Tools", "Tool ainda não implementada: $nome")
                    """{"erro":"nao_implementado","tool":"$nome"}"""
                }
                else -> erro("ferramenta_desconhecida", nome)
            }
        } catch (e: Exception) {
            Log.e("Narrador_Tools", "Falha em $nome: ${e.message}")
            erro("falha_execucao", e.message ?: "erro desconhecido")
        }
        Log.d("Narrador_Tools", "Resultado $nome: ${resultado.take(300)}")
        return resultado
    }

    // ── Implementações reais ────────────────────────────────────────────────

    private suspend fun registrarFato(args: JSONObject): String {
        val dao = sagaDao ?: return erro("sem_dao", "SagaDao indisponível")
        if (campanhaId <= 0L) return erro("sem_campanha", "Nenhuma campanha ativa")
        val sujeito = args.optString("sujeito").trim()
        val predicado = args.optString("predicado").trim()
        val objeto = args.optString("objeto").trim()
        if (sujeito.isBlank() || predicado.isBlank() || objeto.isBlank()) {
            return erro("campos_obrigatorios", "sujeito, predicado e objeto são obrigatórios")
        }
        val peso = args.optInt("peso", 3).coerceIn(1, 10)
        val detalhe = args.optString("detalhe").trim()
        // `texto` é o campo indexado pelo MATCH: concatenação NORMALIZADA (mesma
        // normalização do search_text do Códex) de todos os campos relevantes.
        val texto = CatalogFilters.normalizarBusca(
            listOf(sujeito, predicado, objeto, detalhe).filter { it.isNotBlank() }.joinToString(" ")
        )
        dao.inserirFato(
            CampaignFactEntity(
                campanhaId = campanhaId,
                sujeito = sujeito,
                predicado = predicado,
                objeto = objeto,
                peso = peso,
                cenaId = cenaAtualId,
                texto = texto
            )
        )
        return JSONObject()
            .put("ok", true)
            .put("fato", "$sujeito $predicado $objeto")
            .put("peso", peso)
            .toString()
    }

    private suspend fun consultarMundo(args: JSONObject): String {
        val dao = sagaDao ?: return erro("sem_dao", "SagaDao indisponível")
        if (campanhaId <= 0L) return erro("sem_campanha", "Nenhuma campanha ativa")
        val consulta = args.optString("consulta").trim()
        if (consulta.isBlank()) return erro("campos_obrigatorios", "consulta é obrigatória")
        val limite = args.optInt("limite", 5).coerceIn(1, 20)
        val fatos = dao.buscarFatos(campanhaId, consulta, limite)
        val arr = JSONArray()
        fatos.forEach { f ->
            arr.put(JSONObject()
                .put("fato", "${f.sujeito} ${f.predicado} ${f.objeto}")
                .put("peso", f.peso)
                .apply { f.cenaId?.let { put("cenaId", it) } })
        }
        return JSONObject()
            .put("total", fatos.size)
            .put("fatos", arr)
            .put("instrucao", if (fatos.isEmpty())
                "Nenhum fato registrado sobre isso — trate como território novo e registre o que estabelecer."
            else
                "Estes fatos são CANÔNICOS: não os contradiga.")
            .toString()
    }

    private fun inspecionarPersonagem(args: JSONObject): String {
        val f = forjador ?: return erro("sem_forjador", "ForjadorToolExecutor indisponível")
        val secao = args.optString("secao", "atributos")
        // Delega ao leitor de ficha existente (mesma fonte usada pelo Forjador/Voz).
        return f.lerSecao(secao)
    }

    private suspend fun definirCena(args: JSONObject): String {
        val dao = sagaDao ?: return erro("sem_dao", "SagaDao indisponível")
        val cid = cenaAtualId ?: return erro("sem_cena", "Nenhuma cena ativa")
        if (campanhaId <= 0L) return erro("sem_campanha", "Nenhuma campanha ativa")
        val atual = dao.cenaAberta(campanhaId)
        // Preserva o campo anterior quando a IA omite algum (definir_cena costuma vir parcial).
        val titulo = args.optString("titulo").trim().ifBlank { atual?.titulo.orEmpty() }
        val bioma = args.optString("bioma").trim().ifBlank { atual?.bioma.orEmpty() }
        val humor = args.optString("humor").trim().ifBlank { atual?.humor.orEmpty() }
        val resumo = args.optString("resumo").trim().ifBlank { atual?.resumo.orEmpty() }
        dao.atualizarDescricaoCena(cid, titulo, bioma, humor, resumo)
        return JSONObject().put("ok", true).put("cena", titulo).put("bioma", bioma).put("humor", humor).toString()
    }

    private suspend fun pedirRolagem(args: JSONObject): String {
        val bridge = rollBridge ?: return erro("sem_ui_rolagem", "Nenhuma ponte de rolagem ativa")
        val pericia = args.optString("pericia").trim()
        if (pericia.isBlank()) return erro("campos_obrigatorios", "pericia é obrigatória")
        val motivo = args.optString("motivo").trim()
        val mods = mutableListOf<Pair<String, Int>>()
        args.optJSONArray("modificadores")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val m = o.optString("motivo").trim()
                val v = o.optInt("valor", 0)
                if (m.isNotBlank() || v != 0) mods.add(m to v)
            }
        }
        // Suspende até a UI devolver o dado; o JSON de resultado vem pronto da ponte.
        return bridge.pedirRolagem(pericia, mods, motivo)
    }

    private suspend fun localizarNoCodex(args: JSONObject): String {
        val repo = repository ?: return erro("sem_repositorio", "DataRepository indisponível")
        val termos = args.optString("termos").trim()
        if (termos.isBlank()) return erro("campos_obrigatorios", "termos é obrigatório")
        val livros = args.optJSONArray("livros")?.let { arr ->
            (0 until arr.length()).mapNotNull { arr.optString(it, "").takeIf { s -> s.isNotBlank() } }
        }
        val res = repo.localizarNoCodex(termos, livros)
        if (res.hits.isEmpty()) return JSONObject().put("encontrado", false)
            .put("mensagem", "Nenhuma página do Códex casa com esses termos.").toString()
        val sb = StringBuilder("PÁGINAS ENCONTRADAS (${res.total} no total")
        if (res.modo == "OR") sb.append(", busca aproximada")
        sb.append("):\n")
        res.hits.forEach { h -> sb.append("• [${h.livro}, pág. ${h.pagina}] ${h.trecho}\n") }
        return JSONObject().put("encontrado", true).put("paginas", sb.toString()).toString()
    }

    private suspend fun lerPagina(args: JSONObject): String {
        val repo = repository ?: return erro("sem_repositorio", "DataRepository indisponível")
        val livro = args.optString("livro").trim()
        val pagina = args.optInt("pagina", -1)
        if (livro.isBlank() || pagina < 0) return erro("campos_obrigatorios", "livro e pagina são obrigatórios")
        val paginaFinal = if (args.has("pagina_final")) args.optInt("pagina_final") else null
        val chunks = repo.lerPaginas(livro, pagina, paginaFinal)
        if (chunks.isEmpty()) return JSONObject().put("encontrado", false)
            .put("mensagem", "Página não encontrada nesse livro.").toString()
        val texto = chunks.joinToString("\n\n") { c ->
            "--- [${c.source_title}, pág. ${c.page_number}] ---\n${c.text}"
        }
        return JSONObject().put("encontrado", true).put("texto", texto).toString()
    }

    private fun erro(codigo: String, detalhe: String): String =
        JSONObject().put("erro", codigo).put("detalhe", detalhe).toString()
}
