package com.gurps.ficha.domain.saga

import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Lote 366 (B8): contrato de ROTEAMENTO das tools de combate do Narrador.
 * Garante que iniciar_combate/acao_npc/aplicar_dano/aplicar_condicao/gastar_recurso/conceder_xp
 * deixam de cair em "nao_implementado" e chamam o CombatBridge com os argumentos certos.
 */
class NarradorToolExecutorCombatTest {

    /** Bridge falsa que registra as chamadas e devolve JSON previsível. */
    private class FakeBridge : NarradorToolExecutor.CombatBridge {
        val chamadas = mutableListOf<String>()
        var ativo = false
        override suspend fun iniciarCombate(inimigos: List<Pair<String, Int>>, distanciaM: Int, surpresa: String): String {
            chamadas.add("iniciar:$inimigos:$distanciaM:$surpresa"); ativo = true; return "estado-inicial"
        }
        override fun combateAtivo(): Boolean = ativo
        override fun acaoNpc(npcId: String, intencao: String, alvoId: String?, detalhes: String?): String {
            chamadas.add("acao:$npcId:$intencao"); return JSONObject().put("ok", true).toString()
        }
        override fun aplicarDano(alvoId: String?, dano: String, tipo: String, local: String?): String {
            chamadas.add("dano:$alvoId:$dano:$tipo:$local"); return JSONObject().put("ok", true).toString()
        }
        override fun aplicarCondicao(alvoId: String?, condicao: String, operacao: String): String {
            chamadas.add("cond:$alvoId:$condicao:$operacao"); return JSONObject().put("ok", true).toString()
        }
        override fun gastarRecurso(recurso: String, quantidade: Int, motivo: String, itemNome: String?): String {
            chamadas.add("recurso:$recurso:$quantidade"); return JSONObject().put("ok", true).toString()
        }
        override fun concederXp(pontos: Int, motivo: String): String {
            chamadas.add("xp:$pontos"); return JSONObject().put("ok", true).put("xp_total", pontos).toString()
        }
    }

    private fun exec(bridge: FakeBridge) = NarradorToolExecutor(
        sagaDao = null, repository = null, forjador = null, rollBridge = null, combatBridge = bridge
    )

    @Test
    fun `iniciar_combate parseia inimigos e chama a bridge`() = runBlocking {
        val b = FakeBridge()
        val r = exec(b).executar(
            NarradorTools.TOOL_INICIAR_COMBATE,
            """{"inimigos":[{"id_ou_conceito":"goblin","quantidade":3}],"distancia_m":8,"surpresa":"ninguem"}"""
        )
        assertTrue(JSONObject(r).optBoolean("ok"))
        assertEquals(listOf("iniciar:[(goblin, 3)]:8:ninguem"), b.chamadas)
    }

    @Test
    fun `iniciar_combate sem inimigos devolve erro de campos`() = runBlocking {
        val b = FakeBridge()
        val r = exec(b).executar(NarradorTools.TOOL_INICIAR_COMBATE, """{"distancia_m":5}""")
        assertEquals("campos_obrigatorios", JSONObject(r).optString("erro"))
        assertTrue(b.chamadas.isEmpty())
    }

    @Test
    fun `aplicar_dano conceder_xp aplicar_condicao gastar_recurso roteiam`() = runBlocking {
        val b = FakeBridge()
        val e = exec(b)
        e.executar(NarradorTools.TOOL_APLICAR_DANO, """{"alvo_id":"goblin_1","dano":"2d-1","tipo":"corte","local":"torso"}""")
        e.executar(NarradorTools.TOOL_APLICAR_CONDICAO, """{"alvo_id":"heroi","condicao":"atordoado","operacao":"aplicar"}""")
        e.executar(NarradorTools.TOOL_GASTAR_RECURSO, """{"recurso":"pf","quantidade":2,"motivo":"corrida"}""")
        e.executar(NarradorTools.TOOL_CONCEDER_XP, """{"pontos":3,"motivo":"marco"}""")
        assertEquals(
            listOf("dano:goblin_1:2d-1:corte:torso", "cond:heroi:atordoado:aplicar", "recurso:pf:2", "xp:3"),
            b.chamadas
        )
    }

    @Test
    fun `acao_npc exige combate ativo`() = runBlocking {
        val b = FakeBridge() // ativo = false
        val r = exec(b).executar(NarradorTools.TOOL_ACAO_NPC, """{"npc_id":"goblin_1","intencao":"ataca"}""")
        assertEquals("sem_combate_ativo", JSONObject(r).optString("erro"))
        b.ativo = true
        val r2 = exec(b.apply { ativo = true }).executar(NarradorTools.TOOL_ACAO_NPC, """{"npc_id":"goblin_1","intencao":"ataca"}""")
        assertTrue(JSONObject(r2).optBoolean("ok"))
    }

    @Test
    fun `sem bridge as tools de combate degradam com erro claro`() = runBlocking {
        val e = NarradorToolExecutor(sagaDao = null) // combatBridge nulo
        val r = e.executar(NarradorTools.TOOL_CONCEDER_XP, """{"pontos":1}""")
        assertEquals("sem_combate", JSONObject(r).optString("erro"))
    }
}
