package com.gurps.ficha.data.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gurps.ficha.domain.filters.CatalogFilters

/**
 * Lote 353 (Saga A4): DAO das tabelas da campanha.
 * `buscarFatos` segue o padrão do motor do Auditor (ManualChunkDao + rankearPorBM25):
 * o MATCH FTS4 entrega o pool e o ranking final é feito em Kotlin —
 * peso canônico do fato manda; frequência dos termos desempata.
 */
@Dao
interface SagaDao {

    // ── Campanhas ──────────────────────────────────────────────────────────
    @Insert
    suspend fun inserirCampanha(campanha: CampanhaEntity): Long

    @Update
    suspend fun atualizarCampanha(campanha: CampanhaEntity)

    @Query("SELECT * FROM campanhas ORDER BY criadaEm DESC")
    suspend fun listarCampanhas(): List<CampanhaEntity>

    @Query("SELECT * FROM campanhas WHERE id = :id")
    suspend fun getCampanha(id: Long): CampanhaEntity?

    @Query("DELETE FROM campanhas WHERE id = :id")
    suspend fun excluirCampanha(id: Long)

    @Query("DELETE FROM cenas WHERE campanhaId = :id")
    suspend fun excluirCenasDaCampanha(id: Long)

    @Query("DELETE FROM campaign_facts WHERE campanhaId = :id")
    suspend fun excluirFatosDaCampanha(id: Long)

    @Query("DELETE FROM world_state WHERE campanhaId = :id")
    suspend fun excluirWorldState(id: Long)

    /** Lote 356: remove a campanha e tudo que pendura nela (sem deixar órfãos). */
    @Transaction
    suspend fun excluirCampanhaCompleta(id: Long) {
        excluirFatosDaCampanha(id)
        excluirCenasDaCampanha(id)
        excluirWorldState(id)
        excluirCampanha(id)
    }

    // ── Cenas ──────────────────────────────────────────────────────────────
    @Insert
    suspend fun inserirCena(cena: CenaEntity): Long

    @Update
    suspend fun atualizarCena(cena: CenaEntity)

    @Query("SELECT * FROM cenas WHERE campanhaId = :campanhaId ORDER BY indice")
    suspend fun listarCenas(campanhaId: Long): List<CenaEntity>

    @Query("SELECT * FROM cenas WHERE campanhaId = :campanhaId AND fechadaEm IS NULL ORDER BY indice DESC LIMIT 1")
    suspend fun cenaAberta(campanhaId: Long): CenaEntity?

    /** Lote 355: definir_cena — atualiza os campos descritivos da cena aberta. */
    @Query("UPDATE cenas SET titulo = :titulo, bioma = :bioma, humor = :humor, resumo = :resumo WHERE id = :cenaId")
    suspend fun atualizarDescricaoCena(cenaId: Long, titulo: String, bioma: String, humor: String, resumo: String)

    // ── Fatos (FTS4) ───────────────────────────────────────────────────────
    @Insert
    suspend fun inserirFato(fato: CampaignFactEntity)

    /** MATCH cru — pool sem ranking (ranking em buscarFatos). */
    @Query("SELECT * FROM campaign_facts WHERE campanhaId = :campanhaId AND texto MATCH :query LIMIT :limite")
    suspend fun matchFatos(campanhaId: Long, query: String, limite: Int): List<CampaignFactEntity>

    @Query("SELECT * FROM campaign_facts WHERE campanhaId = :campanhaId")
    suspend fun todosFatos(campanhaId: Long): List<CampaignFactEntity>

    @Query("SELECT COUNT(*) FROM campaign_facts WHERE campanhaId = :campanhaId")
    suspend fun contarFatos(campanhaId: Long): Int

    // ── Estado do mundo ────────────────────────────────────────────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvarWorldState(estado: WorldStateEntity)

    @Query("SELECT * FROM world_state WHERE campanhaId = :campanhaId")
    suspend fun getWorldState(campanhaId: Long): WorldStateEntity?

    /**
     * Busca de fatos para o `consultar_mundo` do Narrador.
     * 1. Normaliza a consulta (mesma normalização usada ao gravar `texto`).
     * 2. MATCH AND (todas as palavras); fallback OR se vazio — padrão do localizarNoCodex.
     * 3. Ranking em Kotlin: peso DESC (fato canônico manda) e, no empate,
     *    frequência dos termos no texto (BM25 simplificado, como rankearPorBM25).
     */
    suspend fun buscarFatos(campanhaId: Long, query: String, limite: Int = 5): List<CampaignFactEntity> {
        val tokens = CatalogFilters.normalizarBusca(query)
            .split(Regex("\\s+"))
            .filter { it.length >= 2 }
        if (tokens.isEmpty()) return emptyList()

        val poolMax = (limite * 5).coerceAtLeast(25)
        var pool = matchFatos(campanhaId, tokens.joinToString(" "), poolMax)
        if (pool.isEmpty() && tokens.size > 1) {
            pool = matchFatos(campanhaId, tokens.joinToString(" OR "), poolMax)
        }

        fun freqTermos(texto: String): Int = tokens.sumOf { t ->
            Regex(Regex.escape(t)).findAll(texto).count()
        }
        return pool
            .sortedWith(
                compareByDescending<CampaignFactEntity> { it.peso }
                    .thenByDescending { freqTermos(it.texto) }
            )
            .take(limite)
    }
}
