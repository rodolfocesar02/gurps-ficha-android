package com.gurps.ficha.data.storage

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

/**
 * Lote 353 (Saga A4): fundação de dados do modo GURPS Saga.
 * 4 tabelas novas (Room v25): campanhas, cenas, campaign_facts (FTS4) e world_state.
 * Nenhuma tabela existente foi alterada — a migração 24→25 é puramente aditiva.
 */

/** Uma campanha do modo Saga: 1 personagem + 1 cenário + progresso de capítulos. */
@Entity(tableName = "campanhas")
data class CampanhaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val cenarioId: String,
    /** nomeArquivo da ficha (FichaEntity.nomeArquivo) que joga esta campanha. */
    val personagemId: String,
    val criadaEm: Long,
    val capituloAtual: Int = 1,
    /** Resumo acumulado do capítulo (memória hierárquica — preenchido no C5). */
    val resumoCapitulo: String = "",
    /** Tempo DE JOGO decorrido, em minutos (relógio do mundo, não da sessão). */
    val tempoJogoMin: Long = 0,
    /** Seed determinística do mundo (clima/ecologia/encontros reproduzíveis). */
    val seedMundo: Long
)

/** Uma cena da campanha (unidade narrativa). fechadaEm == null → cena aberta. */
@Entity(tableName = "cenas")
data class CenaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val campanhaId: Long,
    /** Ordem da cena dentro da campanha (1, 2, 3...). */
    val indice: Int,
    val titulo: String,
    /** Resumo da cena ao fechar (≤150 palavras — preenchido no C5). */
    val resumo: String = "",
    val bioma: String = "",
    val humor: String = "",
    val fechadaEm: Long? = null
)

/**
 * Fato canônico do mundo no formato sujeito-predicado-objeto, indexado por FTS4
 * (mesmo padrão da ManualChunkEntity). `texto` é a concatenação NORMALIZADA
 * (sem acento/caixa, via CatalogFilters.normalizarBusca) usada pelo MATCH —
 * os demais campos preservam a grafia original para exibição.
 * `peso` 1-10: relevância canônica (10 = evento de relógio de facção).
 */
@Fts4
@Entity(tableName = "campaign_facts")
data class CampaignFactEntity(
    val campanhaId: Long,
    val sujeito: String,
    val predicado: String,
    val objeto: String,
    val peso: Int,
    val cenaId: Long? = null,
    val texto: String
)

/** Estado vivo do mundo por campanha (clima/relógios/ecologia/economia como JSON). */
@Entity(tableName = "world_state")
data class WorldStateEntity(
    @PrimaryKey val campanhaId: Long,
    val climaPorRegiaoJson: String = "{}",
    val relogiosJson: String = "{}",
    val ecologiaJson: String = "{}",
    val economiaJson: String = "{}",
    /** Minuto de jogo do último tick processado pelo WorldTickEngine (C2). */
    val ultimoTickMin: Long = 0
)
