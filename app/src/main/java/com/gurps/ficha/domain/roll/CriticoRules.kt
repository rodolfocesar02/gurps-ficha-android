package com.gurps.ficha.domain.roll

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.random.Random

/**
 * Regras de crítico do GURPS 4ª Ed. (Sucesso Decisivo / Falha Crítica) e
 * disparo automático das tabelas de Golpe Fulminante / Erro Crítico.
 *
 * Regra completa (Módulo Básico p.347 / Artes Marciais):
 *  - Sucesso Decisivo: soma 3-4 (sempre); 5 se NH efetivo >= 15; 6 se NH >= 16.
 *  - Falha Crítica: soma 18 (sempre); 17 se NH efetivo <= 15; e qualquer
 *    soma >= NH+10 (ex.: NH 6 -> 16+).
 */
object CriticoRules {

    enum class ResultadoCritico { DECISIVO, FALHA_CRITICA, NORMAL }

    /** Tipos de teste que disparam as tabelas críticas (combate). */
    private val TIPOS_COMBATE = setOf("Ataque", "Defesa", "Tecnica", "Técnica", "Magia")

    fun ehTesteDeCombate(tipoLabel: String): Boolean = tipoLabel in TIPOS_COMBATE

    /**
     * Classifica a rolagem aplicando a regra COMPLETA com NH efetivo.
     * [nhEfetivo] = alvo já com modificadores; null = sem alvo (não classifica crítico).
     */
    fun classificar(soma: Int, nhEfetivo: Int?): ResultadoCritico {
        // Decisivo
        if (soma == 3 || soma == 4) return ResultadoCritico.DECISIVO
        if (nhEfetivo != null) {
            if (soma == 5 && nhEfetivo >= 15) return ResultadoCritico.DECISIVO
            if (soma == 6 && nhEfetivo >= 16) return ResultadoCritico.DECISIVO
        }
        // Falha crítica
        if (soma == 18) return ResultadoCritico.FALHA_CRITICA
        if (nhEfetivo != null) {
            if (soma == 17 && nhEfetivo <= 15) return ResultadoCritico.FALHA_CRITICA
            // "10 ou mais acima do NH efetivo"
            if (soma >= nhEfetivo + 10) return ResultadoCritico.FALHA_CRITICA
        } else {
            // Sem NH (raro p/ combate): cai na regra simples
            if (soma == 17) return ResultadoCritico.FALHA_CRITICA
        }
        return ResultadoCritico.NORMAL
    }

    // --- Aplicação MECÂNICA das tabelas no combate da Saga (Lote 384, puro/testável) ---

    /** Efeito de um Golpe Fulminante sobre o DANO (MB p.558, Tabela de Golpe Fulminante geral). */
    enum class EfeitoGolpeFulminante { NORMAL, DOBRO, TRIPLO, MAXIMO, RD_METADE, FERIMENTO_GRAVE }

    /** Rola o efeito do Golpe Fulminante a partir de 3d6 (MB p.558). Defesa já é anulada pelo crítico. */
    fun golpeFulminante(soma3d6: Int): EfeitoGolpeFulminante = when (soma3d6) {
        3, 18 -> EfeitoGolpeFulminante.TRIPLO
        5, 16 -> EfeitoGolpeFulminante.DOBRO
        6, 15 -> EfeitoGolpeFulminante.MAXIMO
        4, 17 -> EfeitoGolpeFulminante.RD_METADE
        7, 13, 14 -> EfeitoGolpeFulminante.FERIMENTO_GRAVE
        else -> EfeitoGolpeFulminante.NORMAL // 8/9/10/11/12: dano normal (efeitos de choque/largar tratados à parte)
    }

    /**
     * Efeito de um Erro Crítico sobre o ATACANTE (MB p.557). O motor da Saga aplica os mecânicos
     * (ACERTA_A_SI[_METADE] = dano em si; CAI = derrubado) e NARRA os demais (sem rastrear durabilidade
     * de arma): QUEBRA_ARMA/LARGA_ARMA/DESEQUILIBRIO.
     */
    enum class EfeitoErroCritico { LARGA_ARMA, QUEBRA_ARMA, ACERTA_A_SI, ACERTA_A_SI_METADE, CAI, DESEQUILIBRIO }

    fun erroCritico(soma3d6: Int, desarmado: Boolean): EfeitoErroCritico = if (desarmado) when (soma3d6) {
        3, 18 -> EfeitoErroCritico.CAI            // nocaute → aproximado como queda
        5, 16 -> EfeitoErroCritico.ACERTA_A_SI    // atinge objeto sólido / a si
        6 -> EfeitoErroCritico.ACERTA_A_SI_METADE
        8 -> EfeitoErroCritico.CAI
        else -> EfeitoErroCritico.DESEQUILIBRIO   // 4/7/9-15/17: tropeço/perde equilíbrio/distensão
    } else when (soma3d6) {
        3, 4, 17, 18 -> EfeitoErroCritico.QUEBRA_ARMA
        5 -> EfeitoErroCritico.ACERTA_A_SI
        6 -> EfeitoErroCritico.ACERTA_A_SI_METADE
        7, 13, 15 -> EfeitoErroCritico.DESEQUILIBRIO
        16 -> EfeitoErroCritico.CAI
        else -> EfeitoErroCritico.LARGA_ARMA      // 8/9/10/11/12/14: arma gira/cai
    }

    // --- Tabelas (carregadas do asset) ---

    private data class TabelaCritica(
        val nome: String = "",
        val nota: String? = null,
        val entradas: Map<String, String> = emptyMap()
    )

    private var tabelas: Map<String, TabelaCritica>? = null

    private fun carregar(context: Context): Map<String, TabelaCritica> {
        tabelas?.let { return it }
        return try {
            val json = context.assets.open("tabelas_criticas.json")
                .bufferedReader().use { it.readText() }
            val tipo = object : TypeToken<Map<String, TabelaCritica>>() {}.type
            val m = Gson().fromJson<Map<String, TabelaCritica>>(json, tipo) ?: emptyMap()
            tabelas = m
            m
        } catch (e: Exception) {
            android.util.Log.w("CriticoRules", "Falha ao carregar tabelas_criticas.json: ${e.message}")
            emptyMap()
        }
    }

    /** Resultado de uma rolagem de tabela crítica, pronto para exibir/enviar. */
    data class RolagemTabela(
        val titulo: String,      // ex.: "💥 Golpe Fulminante (3d6 = 11)"
        val dados: List<Int>,    // os 3 dados rolados
        val soma: Int,
        val textoCompleto: String // as duas tabelas formatadas
    )

    /**
     * Rola 3d6 e monta o texto das DUAS tabelas correspondentes ao [resultado]
     * (decisivo -> Golpe Fulminante normal + na Cabeça; falha crítica ->
     * Erro Crítico normal + Desarmado). O jogador escolhe qual aplicar.
     * Retorna null se não for decisivo/crítico ou se as tabelas não carregarem.
     */
    fun rolarTabela(context: Context, resultado: ResultadoCritico): RolagemTabela? {
        if (resultado == ResultadoCritico.NORMAL) return null
        val tbs = carregar(context)
        if (tbs.isEmpty()) return null

        val d1 = Random.nextInt(1, 7); val d2 = Random.nextInt(1, 7); val d3 = Random.nextInt(1, 7)
        val soma = d1 + d2 + d3
        val chave = soma.toString()

        val (emoji, idA, idB) = when (resultado) {
            ResultadoCritico.DECISIVO -> Triple("💥", "golpe_fulminante", "golpe_fulminante_cabeca")
            else -> Triple("💀", "erro_critico", "erro_critico_desarmado")
        }
        val a = tbs[idA]; val b = tbs[idB]
        val tituloGeral = if (resultado == ResultadoCritico.DECISIVO) "Golpe Fulminante" else "Erro Crítico"
        val titulo = "$emoji $tituloGeral (3d6 = $soma)"

        val texto = buildString {
            a?.let {
                append("**${it.nome}**\n")
                append(it.entradas[chave] ?: "(sem entrada para $soma)")
                append("\n\n")
            }
            b?.let {
                append("**${it.nome}**\n")
                it.nota?.let { n -> append("_${n}_\n") }
                append(it.entradas[chave] ?: "(sem entrada para $soma)")
            }
        }.trim()

        return RolagemTabela(titulo = titulo, dados = listOf(d1, d2, d3), soma = soma, textoCompleto = texto)
    }
}
