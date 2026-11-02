package com.gurps.ficha.data.storage

import androidx.sqlite.db.SupportSQLiteDatabase
import java.io.File
import java.lang.reflect.Proxy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **A troca de versão do banco não pode levar as fichas junto** — lote SEM-IA.
 *
 * ## 🟥 O risco
 *
 * O Mestre IA e a Saga saíram, e com eles nove tabelas do banco (o livro
 * inteiro importado do `chunks.jsonl`, as conversas, as campanhas). As fichas
 * salvas moram no MESMO banco, na tabela `fichas`.
 *
 * 🔴 O banco está com `fallbackToDestructiveMigration`: se a versão sobe e o Room
 * não acha uma migração, ele **apaga o banco inteiro**. Um esquecimento aqui não
 * dá erro na tela — o jogador abre o app e as fichas não estão mais lá.
 *
 * ⚠️ A migração RODA nesta bancada, sobre a interface de verdade do
 * `SupportSQLiteDatabase`: o que ela manda executar é gravado e conferido. Só o
 * `execSQL` responde; qualquer outra chamada estoura, para a bancada não fingir
 * um banco que não imita.
 */
class MigracaoSemDonoTest {

    private fun rodarAMigracao(): List<String> {
        val executado = mutableListOf<String>()
        val banco = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, metodo, argumentos ->
            if (metodo.name == "execSQL") {
                executado += argumentos!![0] as String
                null
            } else {
                throw UnsupportedOperationException(
                    "a migracao chamou ${metodo.name}, que esta bancada nao imita"
                )
            }
        } as SupportSQLiteDatabase

        FichaDatabase.MIGRATION_26_27.migrate(banco)
        return executado
    }

    /** As nove tabelas que o esquema 26 tinha além da `fichas` (FichaDatabase_Impl da versão 198). */
    private val DO_ESQUEMA_26_SEM_AS_FICHAS = setOf(
        "manual_chunks", "graph_nodes", "chat_sessions", "chat_messages", "vec_chunks",
        "campanhas", "cenas", "campaign_facts", "world_state"
    )

    private fun tabelaDerrubada(sql: String) =
        Regex("""^DROP TABLE IF EXISTS `([a-z_]+)`$""").find(sql)?.groupValues?.get(1)

    @Test
    fun `🟥 a migracao NAO toca na tabela das fichas`() {
        val sql = rodarAMigracao()
        assertTrue("a migracao nao executou nada", sql.isNotEmpty())
        sql.forEach {
            assertFalse("a migracao mexe nas fichas salvas: $it", it.contains("`fichas`"))
            assertFalse("a migracao mexe nas fichas salvas: $it", Regex("""\bfichas\b""").containsMatchIn(it))
        }
    }

    @Test
    fun `🟥 derruba as nove tabelas sem dono, cada uma uma vez, e so isso`() {
        val sql = rodarAMigracao()
        val derrubadas = sql.map { tabelaDerrubada(it) }
        assertFalse(
            "a migracao faz algo alem de derrubar tabela: $sql",
            derrubadas.any { it == null }
        )
        assertEquals("tabela derrubada duas vezes", derrubadas.size, derrubadas.toSet().size)
        assertEquals(DO_ESQUEMA_26_SEM_AS_FICHAS, derrubadas.filterNotNull().toSet())
    }

    @Test
    fun `⚠️ as mensagens saem antes das sessoes a que pertencem`() {
        val derrubadas = rodarAMigracao().map { tabelaDerrubada(it) }
        assertTrue(
            "chat_sessions saiu antes de chat_messages",
            derrubadas.indexOf("chat_messages") < derrubadas.indexOf("chat_sessions")
        )
    }

    @Test
    fun `🔴 a migracao vai da 26 para a 27, e o banco a usa`() {
        assertEquals(26, FichaDatabase.MIGRATION_26_27.startVersion)
        assertEquals(27, FichaDatabase.MIGRATION_26_27.endVersion)

        // ⚠️ O registro no construtor do Room não roda fora do Android: aqui se
        // confere o texto. Sem ele, a migração existe e ninguém a chama — e o
        // destrutivo apaga as fichas do mesmo jeito.
        val fonte = File("src/main/java/com/gurps/ficha/data/storage/FichaDatabase.kt").readText()
        assertTrue("o banco nao esta na versao 27", fonte.contains("version = 27,"))
        assertTrue(
            "a migracao 26->27 nao foi registrada no banco",
            fonte.contains(".addMigrations(MIGRATION_24_25, MIGRATION_25_26, MIGRATION_26_27)")
        )
    }
}
