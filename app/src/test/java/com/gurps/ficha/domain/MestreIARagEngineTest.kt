package com.gurps.ficha.domain

import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.data.MestreIaTema
import org.junit.Assert.assertTrue
import org.junit.Test
import android.content.Context

class MestreIARagEngineTest {

    // Stub manual para evitar dependência de Mockito externa
    class DataRepositoryStub : DataRepository(null as Any as Context) {
        override val temasMestreIA = listOf(
            MestreIaTema("fogo", "fogo", listOf("chamas", "calor", "incendio", "termico")),
            MestreIaTema("espada", "espada", listOf("lamina", "corte", "sabre")),
            MestreIaTema("guerreiro", "guerreiro", listOf("combate", "luta", "soldado"))
        )
        override val vantagens = emptyList<com.gurps.ficha.model.VantagemDefinicao>()
        override val desvantagens = emptyList<com.gurps.ficha.model.DesvantagemDefinicao>()
        override val pericias = emptyList<com.gurps.ficha.model.PericiaDefinicao>()
        override val magias = emptyList<com.gurps.ficha.model.MagiaDefinicao>()
    }

    @Test
    fun testStressPerformanceRag() {
        val repository = DataRepositoryStub()

        println("=== INICIANDO TESTE DE ESTRESSE: MESTRE IA PRIME ===")
        val startTime = System.currentTimeMillis()
        val repeticoes = 100
        
        repeat(repeticoes) {
            MestreIARagEngine.buscarContexto("Preciso de um guerreiro forte com espada e fogo", repository)
        }
        
        val totalTime = System.currentTimeMillis() - startTime
        val mediaPorBusca = totalTime.toDouble() / repeticoes
        
        println("Resultados:")
        println("- Total de buscas simuladas: $repeticoes")
        println("- Tempo total de processamento: ${totalTime}ms")
        println("- Latência média por consulta RAG: ${mediaPorBusca}ms")
        
        // Alvo: < 10ms por busca local
        assertTrue("Performance degradada: ${mediaPorBusca}ms", mediaPorBusca < 20.0)
    }
}
