package com.gurps.ficha.saga

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gurps.ficha.data.storage.CampaignFactEntity
import com.gurps.ficha.data.storage.CampanhaEntity
import com.gurps.ficha.data.storage.FichaDatabase
import com.gurps.ficha.data.storage.SagaDao
import com.gurps.ficha.domain.filters.CatalogFilters
import com.gurps.ficha.domain.saga.NarradorToolExecutor
import com.gurps.ficha.domain.saga.NarradorTools
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Lote 353 (Saga A4): testes instrumentados da fundação de dados — Room em memória
 * com FTS4 REAL do SQLite do aparelho (o FTS4 não existe no Room mockado da JVM).
 * Cobre o aceite do lote: gravar 5 fatos, buscar por termo, ordenação por peso,
 * roteamento do executor e roundtrip registrar_fato → consultar_mundo.
 */
@RunWith(AndroidJUnit4::class)
class SagaFoundationTest {

    private lateinit var db: FichaDatabase
    private lateinit var dao: SagaDao
    private var campanhaId: Long = 0

    private fun fato(camp: Long, s: String, p: String, o: String, peso: Int) = CampaignFactEntity(
        campanhaId = camp, sujeito = s, predicado = p, objeto = o, peso = peso,
        cenaId = null, texto = CatalogFilters.normalizarBusca("$s $p $o")
    )

    @Before
    fun setup() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FichaDatabase::class.java
        ).build()
        dao = db.sagaDao()
        campanhaId = dao.inserirCampanha(
            CampanhaEntity(nome = "Teste", cenarioId = "fendaverso", personagemId = "ficha_teste",
                criadaEm = 1L, seedMundo = 42L)
        )
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun grava5FatosBuscaPorTermoOrdenaPorPeso() = runBlocking {
        dao.inserirFato(fato(campanhaId, "Capitão Renn", "comanda", "a guarda do portão", 5))
        dao.inserirFato(fato(campanhaId, "Guarda Tomaz", "deve favores a", "Mira", 9))
        dao.inserirFato(fato(campanhaId, "O portão norte", "fecha ao", "anoitecer", 2))
        dao.inserirFato(fato(campanhaId, "Mira", "vende", "poções", 10))
        val outraCampanha = dao.inserirCampanha(
            CampanhaEntity(nome = "Outra", cenarioId = "x", personagemId = "y", criadaEm = 2L, seedMundo = 7L)
        )
        dao.inserirFato(fato(outraCampanha, "Guarda Real", "patrulha", "a capital", 10))

        assertEquals(4, dao.contarFatos(campanhaId))

        // Termo único: 2 fatos casam "guarda"; o de peso 9 vem antes do de peso 5.
        val porGuarda = dao.buscarFatos(campanhaId, "guarda", 5)
        assertEquals(listOf(9, 5), porGuarda.map { it.peso })
        // Fato de OUTRA campanha (peso 10) não vaza, mesmo casando o termo.
        assertTrue(porGuarda.none { it.campanhaId == outraCampanha })

        // AND com acento na consulta: normaliza e exige TODAS as palavras.
        val porGuardaPortao = dao.buscarFatos(campanhaId, "guarda portão", 5)
        assertEquals(1, porGuardaPortao.size)
        assertEquals("Capitão Renn", porGuardaPortao[0].sujeito)

        // Ordenação por peso em outro termo.
        val porPortao = dao.buscarFatos(campanhaId, "portão", 5)
        assertEquals(listOf(5, 2), porPortao.map { it.peso })
    }

    @Test
    fun executorFazRoundtripRegistrarEConsultar() = runBlocking {
        val executor = NarradorToolExecutor(sagaDao = dao)
        executor.campanhaId = campanhaId

        val r1 = executor.executar(
            NarradorTools.TOOL_REGISTRAR_FATO,
            JSONObject().put("sujeito", "Mira").put("predicado", "escondeu").put("objeto", "o amuleto na adega")
                .put("peso", 8).toString()
        )
        assertTrue(JSONObject(r1).optBoolean("ok"))

        val r2 = executor.executar(
            NarradorTools.TOOL_CONSULTAR_MUNDO,
            JSONObject().put("consulta", "amuleto adega").toString()
        )
        val json = JSONObject(r2)
        assertEquals(1, json.getInt("total"))
        assertTrue(json.getJSONArray("fatos").getJSONObject(0).getString("fato").contains("amuleto"))
    }

    @Test
    fun executorRoteiaNaoImplementadasEDesconhecidas() = runBlocking {
        val executor = NarradorToolExecutor(sagaDao = dao)
        executor.campanhaId = campanhaId

        // Tool do contrato ainda sem executor real → erro padronizado nao_implementado.
        val rolagem = JSONObject(executor.executar(NarradorTools.TOOL_PEDIR_ROLAGEM, "{}"))
        assertEquals("nao_implementado", rolagem.getString("erro"))
        assertEquals(NarradorTools.TOOL_PEDIR_ROLAGEM, rolagem.getString("tool"))

        // Tool fora do contrato → ferramenta_desconhecida.
        val desconhecida = JSONObject(executor.executar("abrir_portal", "{}"))
        assertEquals("ferramenta_desconhecida", desconhecida.getString("erro"))

        // Dependência ausente degrada com erro JSON (não exceção).
        val inspect = JSONObject(executor.executar(NarradorTools.TOOL_INSPECIONAR_PERSONAGEM, "{}"))
        assertEquals("sem_forjador", inspect.getString("erro"))

        // Sem campanha ativa → erro claro.
        val semCampanha = NarradorToolExecutor(sagaDao = dao)
        val rf = JSONObject(
            semCampanha.executar(
                NarradorTools.TOOL_REGISTRAR_FATO,
                JSONObject().put("sujeito", "a").put("predicado", "b").put("objeto", "c").put("peso", 1).toString()
            )
        )
        assertEquals("sem_campanha", rf.getString("erro"))
    }
}
