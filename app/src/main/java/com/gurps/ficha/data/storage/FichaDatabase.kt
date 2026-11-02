package com.gurps.ficha.data.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FichaEntity::class],
    version = 27,  // Lote SEM-IA: saem as tabelas do Mestre IA e da Saga; as fichas ficam
    exportSchema = false
)
abstract class FichaDatabase : RoomDatabase() {
    abstract fun fichaDao(): FichaDao

    companion object {
        @Volatile
        private var INSTANCE: FichaDatabase? = null

        /**
         * Lote 353: PRIMEIRA migração explícita do projeto. O "padrão anterior" era
         * fallbackToDestructiveMigration (bump de versão APAGAVA o banco — fichas
         * sobreviviam pela sincronização em nuvem). Como a 24→25 é puramente ADITIVA,
         * preservamos tudo criando só as 4 tabelas novas. O SQL espelha EXATAMENTE o
         * createAllTables gerado pelo Room (FichaDatabase_Impl) — divergência = crash
         * de validação na abertura.
         *
         * ⚠️ Fica mesmo com a Saga fora (lote SEM-IA): um aparelho na versão 24 passa
         * por aqui, pela 25→26 e pela 26→27, nesta ordem. Sem ela, o Room não acharia
         * caminho e cairia no destrutivo — que apaga as FICHAS junto.
         */
        val MIGRATION_24_25 = object : androidx.room.migration.Migration(24, 25) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `campanhas` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nome` TEXT NOT NULL, `cenarioId` TEXT NOT NULL, `personagemId` TEXT NOT NULL, `criadaEm` INTEGER NOT NULL, `capituloAtual` INTEGER NOT NULL, `resumoCapitulo` TEXT NOT NULL, `tempoJogoMin` INTEGER NOT NULL, `seedMundo` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `cenas` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `campanhaId` INTEGER NOT NULL, `indice` INTEGER NOT NULL, `titulo` TEXT NOT NULL, `resumo` TEXT NOT NULL, `bioma` TEXT NOT NULL, `humor` TEXT NOT NULL, `fechadaEm` INTEGER)")
                db.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `campaign_facts` USING FTS4(`campanhaId` INTEGER NOT NULL, `sujeito` TEXT NOT NULL, `predicado` TEXT NOT NULL, `objeto` TEXT NOT NULL, `peso` INTEGER NOT NULL, `cenaId` INTEGER, `texto` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `world_state` (`campanhaId` INTEGER NOT NULL, `climaPorRegiaoJson` TEXT NOT NULL, `relogiosJson` TEXT NOT NULL, `ecologiaJson` TEXT NOT NULL, `economiaJson` TEXT NOT NULL, `ultimoTickMin` INTEGER NOT NULL, PRIMARY KEY(`campanhaId`))")
            }
        }

        /**
         * Lote 356: adiciona campanhas.configJson (TEXT NOT NULL DEFAULT '{}'). Aditiva e
         * idempotente — SQL espelha o createAllTables do FichaDatabase_Impl gerado pelo Room.
         */
        val MIGRATION_25_26 = object : androidx.room.migration.Migration(25, 26) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `campanhas` ADD COLUMN `configJson` TEXT NOT NULL DEFAULT '{}'")
            }
        }

        /**
         * As tabelas que o Mestre IA e a Saga deixaram no aparelho — lote SEM-IA.
         *
         * 🔴 O livro inteiro (`manual_chunks`, importado do `chunks.jsonl` na primeira
         * abertura) mora aqui dentro, e ninguém mais o lê. Deixá-lo seria guardar
         * megabytes mortos no telefone de cada jogador para sempre.
         *
         * ⚠️ `chat_messages` antes de `chat_sessions`: as mensagens penduram-se nas
         * sessões pelo `sessionId`.
         */
        internal val TABELAS_SEM_DONO = listOf(
            "manual_chunks", "graph_nodes", "vec_chunks",
            "chat_messages", "chat_sessions",
            "campaign_facts", "world_state", "cenas", "campanhas"
        )

        /**
         * 🟥 **Derruba só as tabelas sem dono. A `fichas` não é tocada.**
         *
         * Sem esta migração, o `fallbackToDestructiveMigration` lá embaixo resolveria
         * a troca de versão APAGANDO O BANCO INTEIRO — e as fichas salvas moram nele.
         *
         * ⚠️ `IF EXISTS`: um aparelho que nunca abriu a Saga ou o Mestre IA pode não
         * ter todas. E as tabelas FTS4 levam as tabelas-sombra junto no `DROP`.
         */
        val MIGRATION_26_27 = object : androidx.room.migration.Migration(26, 27) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                TABELAS_SEM_DONO.forEach { db.execSQL("DROP TABLE IF EXISTS `$it`") }
            }
        }

        fun getInstance(context: Context): FichaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FichaDatabase::class.java,
                    "gurps_fichas.db"
                )
                .addMigrations(MIGRATION_24_25, MIGRATION_25_26, MIGRATION_26_27)
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
            }
        }
    }
}
