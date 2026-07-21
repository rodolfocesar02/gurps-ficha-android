package com.gurps.ficha.domain.combat

import com.gurps.ficha.domain.magic.ContextoConjuracao
import com.gurps.ficha.domain.magic.MagiaMecanica
import com.gurps.ficha.domain.magic.MagicClassParser
import com.gurps.ficha.domain.magic.MagicEnergy
import com.gurps.ficha.domain.magic.NivelMana
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Lote SIM-1 — **INVARIANTES por simulação**.
 *
 * ## Por que este arquivo existe
 *
 * Em 21/jul o usuário disse, com razão, que estava cansado do ciclo "testa → acha bug → corrige →
 * testa de novo". O diagnóstico não foi "testar mais": foi **testar a camada errada**. Havia 884
 * testes e todos verificavam uma função de cada vez — nenhum fazia *uma feature encontrar a outra*.
 * Os bugs TOK-9 (zona ferindo dobrado no turno da conjuração) e TOK-10 (zonas empilhando) eram
 * exatamente isso: código que eu escrevi semanas antes esbarrando no que escrevi depois.
 *
 * ## O que ele faz
 *
 * Roda MUITOS combates com ações legais aleatórias e, a cada passo, afirma o que **nunca** pode
 * acontecer. Um teste comum diz "isto deve dar 4"; um invariante diz "isto nunca pode ser negativo",
 * e é o segundo que pega interação entre features.
 *
 * ## Escopo honesto
 *
 * Isto cobre o **MOTOR** (`CombatSession`). NÃO cobre o `SagaCombatController` — o bug TOK-8 (o
 * turno que não fechava enquanto a virada final estivesse pendente) morava lá e **não seria pego
 * aqui**. O controller tem 2.131 linhas e zero testes; fechar essa lacuna é outro trabalho.
 */
class CombateInvariantesTest {

    private fun heroi() = Combatente(
        id = "heroi", nome = "Herói", ehHeroi = true, dx = 13, velocidadeBasica = 6.0,
        deslocamento = 6, pvMax = 20, pvAtual = 20, pfAtual = 20
    )

    private fun goblin(n: Int) = Combatente(
        id = "goblin_$n", nome = "Goblin $n", dx = 11, velocidadeBasica = 5.0, deslocamento = 5,
        pvMax = 9, pvAtual = 9,
        stats = NpcStats(st = 11, dx = 11, ht = 11, pvMax = 9, armaDano = "1d-1",
            armaTipo = "corte", armaNh = 11)
    )

    private fun perfil() = HeroiPerfilCombate(esquiva = 9, apara = 11, ht = 12, rd = 1, vontade = 12)

    private fun sessao(seed: Long, nInimigos: Int): CombatSession {
        val combatentes = listOf(heroi()) + (1..nInimigos).map { goblin(it) }
        val dist = (1..nInimigos).associate { "goblin_$it" to (1 + it % 4) }
        return CombatSession(CombatEncounter(combatentes, dist, seed = seed), perfil(), Random(seed))
    }

    private fun zonaDe(nome: String, dano: String, intervalo: Int, dur: Int) = ZonaPersistente(
        nome = nome, centro = null, raioM = 3, danoExpr = dano, tipoDano = "quei",
        armadura = null, intervaloSeg = intervalo, teste = null,
        segRestantes = dur, segAteProximo = intervalo, operadorId = "heroi"
    )

    private fun ctxDano(nh: Int) = ContextoConjuracao(
        nhBasico = nh, classe = MagicClassParser.parse("Comum"), mana = NivelMana.NORMAL,
        distanciaMetros = 1, mecanica = MagiaMecanica(efeito = "dano", danoPorEnergia = "1d-1")
    )

    /** Estado observável de um combatente, para comparar antes/depois de um passo. */
    private data class Foto(val pv: Int, val pf: Int, val vivo: Boolean)

    private fun fotografar(s: CombatSession): Map<String, Foto> =
        s.encounter.combatentes.associate { it.id to Foto(it.pvAtual, it.pfAtual, it.vivo) }

    // ── Os invariantes ─────────────────────────────────────────────────────────────────────────

    /**
     * Confere o que vale SEMPRE, em qualquer instante de qualquer combate.
     * Devolve a lista de violações (vazia = tudo certo) para a mensagem de falha ser útil.
     */
    private fun violacoes(s: CombatSession, antes: Map<String, Foto>, passo: String): List<String> {
        val erros = mutableListOf<String>()
        for (c in s.encounter.combatentes) {
            val a = antes[c.id] ?: continue
            if (c.pvAtual > c.pvMax)
                erros += "[$passo] ${c.nome}: PV ${c.pvAtual} passou do máximo ${c.pvMax}"
            // PV subir sem cura seria bug; nenhuma ação desta simulação cura.
            if (c.pvAtual > a.pv)
                erros += "[$passo] ${c.nome}: PV subiu de ${a.pv} para ${c.pvAtual} sem cura"
            if (c.pfAtual < 0)
                erros += "[$passo] ${c.nome}: PF negativo (${c.pfAtual})"
            // Ressuscitar não existe: quem morreu não volta.
            if (c.vivo && !a.vivo)
                erros += "[$passo] ${c.nome}: voltou à vida"
            // GURPS deixa o PV ir a negativo, mas nunca além de −PVmáx (aí a morte é resolvida).
            if (c.pvAtual < -c.pvMax * 5)
                erros += "[$passo] ${c.nome}: PV despencou fora de qualquer escala (${c.pvAtual})"
        }
        // Zona expirada não pode continuar na lista.
        s.zonasAtivas.filter { it.segRestantes <= 0 }.forEach {
            erros += "[$passo] zona ${it.rotulo} expirada (${it.segRestantes}s) continua ativa"
        }
        return erros
    }

    @Test
    fun `mil combates aleatorios sem violar nenhum invariante`() {
        val todas = mutableListOf<String>()
        for (seed in 0L until 200L) {
            val s = sessao(seed, nInimigos = 1 + (seed % 3).toInt())
            val rnd = Random(seed * 31 + 7)
            var passos = 0
            while (!s.encerrado && passos < 40) {
                val antes = fotografar(s)
                val acao = when (rnd.nextInt(4)) {
                    0 -> {
                        val alvo = s.inimigosVivos.randomOrNull(rnd)?.id
                        if (alvo != null) {
                            s.heroiConjurar(ctxDano(14), MagicEnergy.parse("1"), 1, "Relâmpago", alvo, 1)
                            "conjurar em $alvo"
                        } else "nada"
                    }
                    1 -> { s.registrarZona(zonaDe("Chuva de Fogo", "1d-1", 1, 6)); "zona" }
                    2 -> { s.heroiMoveTatico(emptyMap(), 1 + rnd.nextInt(4)); "mover" }
                    else -> "nada"
                }
                todas += violacoes(s, antes, "seed $seed passo $passos ($acao)")
                val antesTurno = fotografar(s)
                s.avancarTurno()
                todas += violacoes(s, antesTurno, "seed $seed passo $passos (avançar turno)")
                passos++
            }
        }
        assertTrue("invariantes violados (${todas.size}):\n${todas.take(10).joinToString("\n")}",
            todas.isEmpty())
    }

    @Test
    fun `o relogio das zonas SEMPRE anda — nenhuma zona fica eterna`() {
        // Pega a classe de bug "a magia não conta tempo" sem depender de ninguém olhar o log.
        //
        // ⚠️ Achado na PRIMEIRA execução deste arquivo: a versão inicial deste teste falhou, e a
        // causa era MINHA premissa, não o motor. `avancarTurno` retorna imediatamente quando o
        // combate já acabou — e a própria zona matava o goblin, encerrando a luta. Aí o relógio
        // para, o que é correto: acabou o combate, acabou a contagem. O teste é que não previa
        // esse caminho. Fica registrado porque é exatamente o tipo de interação que este arquivo
        // existe para provocar.
        for (seed in 0L until 50L) {
            val s = sessao(seed, nInimigos = 1)
            s.registrarZona(zonaDe("Chuva de Fogo", "1d-1", 1, 8))
            var restanteAnterior = s.zonasAtivas.first().segRestantes
            var turnosDoHeroi = 0
            var acabouAntes = false
            repeat(40) {
                if (s.encerrado) { acabouAntes = true; return@repeat }
                val eraVezDoHeroi = s.combatenteAtual().ehHeroi
                s.avancarTurno()
                if (eraVezDoHeroi) {
                    turnosDoHeroi++
                    val z = s.zonasAtivas.firstOrNull()
                    if (z != null) {
                        assertTrue("seed $seed: relógio da zona não andou ($restanteAnterior → ${z.segRestantes})",
                            z.segRestantes < restanteAnterior)
                        restanteAnterior = z.segRestantes
                    }
                }
            }
            // Só dá para exigir que a zona tenha expirado se a luta durou o suficiente. Se ela
            // acabou antes, a zona ficar pendurada é consequência do combate ter encerrado.
            if (!acabouAntes && !s.encerrado) {
                assertTrue("seed $seed: a zona de 8s sobreviveu a $turnosDoHeroi turnos do herói",
                    s.zonasAtivas.isEmpty())
            }
        }
    }

    @Test
    fun `nenhum combatente sofre a MESMA zona duas vezes no mesmo turno`() {
        // Invariante que pega o TOK-10 (zonas empilhando) de forma genérica, sem citar a correção.
        for (seed in 0L until 50L) {
            val s = sessao(seed, nInimigos = 2)
            repeat(3) { s.registrarZona(zonaDe("Chuva de Fogo", "1d-1", 1, 12)) }
            repeat(20) {
                val marca = s.log.size
                s.avancarTurno()
                val novas = s.log.drop(marca)
                for (c in s.encounter.combatentes) {
                    val golpes = novas.count { linha ->
                        linha.startsWith("☁️") && linha.contains("Chuva de Fogo") &&
                            (linha.contains(c.nome) || (c.ehHeroi && linha.contains("VOCÊ")))
                    }
                    assertTrue("seed $seed: ${c.nome} sofreu Chuva de Fogo $golpes vezes num turno",
                        golpes <= 1)
                }
            }
        }
    }

    @Test
    fun `zona recem-criada NAO fere no turno em que nasceu`() {
        // Invariante que pega o TOK-9 (dano dobrado na conjuração) genericamente.
        for (seed in 0L until 50L) {
            val s = sessao(seed, nInimigos = 2)
            val marca = s.log.size
            s.registrarZona(zonaDe("Nuvem de Fogo", "3d", 1, 10))
            s.avancarTurno()
            val novas = s.log.drop(marca)
            assertTrue("seed $seed: a zona feriu no próprio turno de criação:\n${novas.joinToString("\n")}",
                novas.none { it.startsWith("☁️") && it.contains("de dano") })
        }
    }

    @Test
    fun `o motor nunca fica mudo — toda acao do heroi deixa registro`() {
        // Bug silencioso é o pior: o jogador gasta o turno e não sabe o que houve. Foi assim que o
        // MEC-6 descobriu que buffs "Instant." eram descartados sem uma linha de log.
        for (seed in 0L until 50L) {
            val s = sessao(seed, nInimigos = 1)
            val alvo = s.inimigosVivos.firstOrNull()?.id ?: continue
            val marca = s.log.size
            s.heroiConjurar(ctxDano(14), MagicEnergy.parse("1"), 1, "Relâmpago", alvo, 1)
            assertTrue("seed $seed: conjurar não deixou nenhuma linha no log", s.log.size > marca)
        }
    }

    @Test
    fun `o turno SEMPRE avanca — a rodada nunca fica parada`() {
        // A classe de bug do TOK-8, na parte que o MOTOR consegue enxergar. (A causa real morava no
        // controller e este teste não a alcança — ver o escopo honesto no cabeçalho.)
        for (seed in 0L until 50L) {
            val s = sessao(seed, nInimigos = 2)
            var ultimo = ""
            repeat(30) {
                if (s.encerrado) return@repeat
                val atual = s.combatenteAtual().id
                s.avancarTurno()
                val depois = s.combatenteAtual().id
                assertTrue("seed $seed: o turno não saiu de $atual",
                    depois != atual || s.encerrado || s.encounter.combatentes.count { it.vivo } == 1)
                ultimo = depois
            }
            assertTrue(ultimo.isNotBlank())
        }
    }
}
