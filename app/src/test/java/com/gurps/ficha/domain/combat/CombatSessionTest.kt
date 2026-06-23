package com.gurps.ficha.domain.combat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Lote 365 (B7): sessão de combate ponta a ponta — herói×NPC, defesas, fim de combate, parser de dano. */
class CombatSessionTest {

    private fun heroi() = Combatente(
        id = "heroi", nome = "Herói", ehHeroi = true, dx = 13, velocidadeBasica = 6.0,
        deslocamento = 6, pvMax = 12, pvAtual = 12
    )

    private fun goblin(id: String = "goblin", pv: Int = 7) = Combatente(
        id = id, nome = "Goblin", dx = 11, velocidadeBasica = 5.0, deslocamento = 5, pvMax = pv, pvAtual = pv,
        stats = NpcStats(st = 11, dx = 11, ht = 11, pvMax = pv, rd = 0, armaDano = "1d-1", armaTipo = "corte", armaNh = 11, alcanceMetros = 1, agressividade = 6, moral = 5)
    )

    private fun perfilHeroi() = HeroiPerfilCombate(esquiva = 9, apara = 11, bloqueio = null, ht = 12, rd = 2)

    private fun espada() = AtaqueHeroi(rotulo = "Espada", nh = 14, danoExpr = "2d", tipo = DanoTipo.CORT)
    private fun revolver() = AtaqueHeroi(rotulo = "Revólver", nh = 14, danoExpr = "2d-1 pa+", tipo = DanoTipo.PI_MAIS, aDistancia = true, alcance = 50)

    @Test
    fun `parser de dano respeita quantidade e modificador`() {
        // soma mínima e máxima de "2d-1": [1] => 2d=2 -1 = 1 ; máximo 12-1=11
        repeat(50) {
            val v = CombatSession.rolarDano("2d-1", Random(it.toLong()))
            assertTrue("v=$v", v in 1..11)
        }
        assertEquals(0, CombatSession.rolarDano("lixo"))
        assertTrue(CombatSession.rolarDano("3d") in 3..18)
    }

    @Test
    fun `mapa de tipo de dano cobre bestiario e ficha (corte, perf, pa)`() {
        assertEquals(DanoTipo.CORT, CombatSession.tipoDano("corte"))
        assertEquals(DanoTipo.PI_MAIS_MAIS, CombatSession.tipoDano("pi++"))
        assertEquals(DanoTipo.PERF, CombatSession.tipoDano("perf"))
        assertEquals(DanoTipo.CONT, CombatSession.tipoDano("desconhecido"))
        // expressões completas da ficha: tipo vem do token final
        assertEquals(DanoTipo.CORT, CombatSession.tipoDano("GeB+2 corte"))
        assertEquals(DanoTipo.PI_MAIS, CombatSession.tipoDano("2d-1 pa+")) // pa+ (Devir) = pi+
        assertEquals(DanoTipo.PI, CombatSession.tipoDano("2d pa"))
        assertEquals(DanoTipo.PI_MENOS, CombatSession.tipoDano("4d pa-"))
    }

    @Test
    fun `penalidade de distancia segue a tabela`() {
        assertEquals(0, CombatSession.penalidadeDistancia(2))
        assertEquals(-2, CombatSession.penalidadeDistancia(5))
        assertEquals(-4, CombatSession.penalidadeDistancia(10))
        assertEquals(-7, CombatSession.penalidadeDistancia(30))
    }

    @Test
    fun `tiro a distancia sofre penalidade e so permite esquiva`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 10), seed = 1L) // 10m
        val s = CombatSession(enc, perfilHeroi(), Random(5))
        val r = s.heroiAtaca(revolver(), "goblin", Manobra.ATAQUE, LocalAtaque.TORSO)
        // resolveu e logou o tiro (🎯) com o nome da arma
        assertTrue(s.log.any { it.contains("Revólver") })
        assertTrue(s.log.isNotEmpty())
        // o relatório do ataque deve registrar a penalidade de distância de 10m (-4)
        assertTrue(s.log.last().contains("distância") || r.texto.contains("distância") || s.log.any { it.contains("distância") })
    }

    @Test
    fun `heroi ataca goblin adjacente e o motor aplica o resultado`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(7))
        assertTrue("goblin deve ser alvo de corpo-a-corpo", s.alvosHeroi().any { it.id == "goblin" })
        val r = s.heroiAtaca(espada(), "goblin", Manobra.ATAQUE, LocalAtaque.TORSO)
        // Com NH 14 a chance de acerto é alta; o que garantimos é que o motor resolveu e gerou log.
        assertTrue(s.log.isNotEmpty())
        if (r.acertou && !r.defendeu) assertTrue(g.pvAtual < g.pvMax)
    }

    @Test
    fun `log de combate e narrativo e mantem os numeros`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(7))
        s.heroiAtaca(espada(), "goblin", Manobra.ATAQUE, LocalAtaque.TORSO)
        val linha = s.log.last { it.startsWith("🗡️") || it.startsWith("🎯") || it.startsWith("⭐") || it.startsWith("💥") }
        assertTrue("deve ter verbo narrativo: $linha",
            Regex("erra|acerta|se esquiva|apara|bloqueia|FALHA|absorve").containsMatchIn(linha))
        assertTrue("deve preservar os números no colchete técnico: $linha",
            linha.contains("[") && linha.contains("rolou"))
    }

    @Test
    fun `vitoria quando todos os inimigos caem`() {
        val g = goblin(pv = 1) // 1 PV: cai fácil
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(2))
        // martela até alguém cair (limite de segurança)
        var i = 0
        while (!s.encerrado && i++ < 30) {
            if (g.vivo) s.heroiAtaca(espada(), "goblin") else break
        }
        assertTrue("g.pv=${g.pvAtual}", !g.vivo || s.encerrado)
        if (!g.vivo) {
            assertTrue(s.encerrado)
            assertEquals(ResultadoCombate.VITORIA, s.resultado)
        }
    }

    @Test
    fun `npc ataca heroi e a defesa escolhida bloqueia ou nao o dano`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(3))
        val intencao = s.npcIntencao("goblin")
        // goblin engajado deve querer atacar o herói
        assertTrue(s.intencaoAtacaHeroi(intencao))
        val opcoes = s.opcoesDefesaHeroi()
        assertTrue(opcoes.any { it.tipo == CombatResolver.TipoDefesa.ESQUIVA })
        // defesa que SEMPRE passa (soma 3) → herói não sofre dano
        val defesa = DefesaHeroi(CombatResolver.TipoDefesa.ESQUIVA, 9, soma = 3)
        val pvAntes = s.heroi.pvAtual
        s.npcResolve("goblin", intencao, defesa)
        assertEquals("esquiva soma 3 sempre defende", pvAntes, s.heroi.pvAtual)
    }

    @Test
    fun `avaliar acumula ate 3, reseta em alvo novo e entra no ataque`() {
        val g1 = goblin("g1"); val g2 = goblin("g2")
        val enc = CombatEncounter(listOf(heroi(), g1, g2), mapOf("g1" to 1, "g2" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(4))
        s.heroiAvaliar("g1"); assertTrue(s.log.last().contains("+1"))
        s.heroiAvaliar("g1"); assertTrue(s.log.last().contains("+2"))
        s.heroiAvaliar("g1"); assertTrue(s.log.last().contains("+3"))
        s.heroiAvaliar("g1"); assertTrue("limita em +3", s.log.last().contains("+3"))
        s.heroiAvaliar("g2"); assertTrue("alvo novo reseta", s.log.last().contains("+1"))
        // o bônus de avaliar aparece na conta do ataque corpo-a-corpo ao alvo avaliado
        s.heroiAtaca(espada(), "g2", Manobra.ATAQUE, LocalAtaque.TORSO)
        val golpe = s.log.last { it.startsWith("🗡️") || it.startsWith("⭐") || it.startsWith("💥") }
        assertTrue("ataque deve somar avaliar: $golpe", golpe.contains("avaliar"))
    }

    @Test
    fun `nao se levanta direto de deitado (MB p365)`() {
        val enc = CombatEncounter(listOf(heroi(), goblin()), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(1))
        s.heroiManobra(Manobra.MUDAR_POSTURA, Postura.DEITADO)
        val alcancaveis = s.posturasAlcancaveis()
        assertFalse("não pode ficar em pé direto de deitado", alcancaveis.contains(Postura.EM_PE))
        assertTrue(alcancaveis.contains(Postura.AJOELHADO))
    }

    @Test
    fun `mover dirigido respeita metros e o teto de deslocamento`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 10), seed = 1L) // herói desloc 6
        val s = CombatSession(enc, perfilHeroi(), Random(1))
        s.heroiMove("goblin", afastar = false, metros = 4); assertEquals(6, s.distancia(g)) // 10-4
        s.heroiMove("goblin", afastar = true, metros = 3); assertEquals(9, s.distancia(g))  // 6+3
        s.heroiMove("goblin", afastar = false, metros = 100); assertEquals(3, s.distancia(g)) // clamp 6: 9-6
    }

    @Test
    fun `apontar soma a precisao da arma no proximo tiro`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 10), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(3))
        val rev = AtaqueHeroi("Revólver", nh = 14, danoExpr = "2d-1 pa+", tipo = DanoTipo.PI_MAIS, aDistancia = true, alcance = 1700, precisao = 2, meioDano = 150)
        s.heroiApontar("goblin")
        s.heroiAtaca(rev, "goblin", Manobra.ATAQUE, LocalAtaque.TORSO)
        val tiro = s.log.last { it.startsWith("🎯") || it.startsWith("⭐") || it.startsWith("💥") }
        assertTrue("o tiro deve somar a Precisão da mira: $tiro", tiro.contains("mira (Acc)"))
    }

    @Test
    fun `parseAparar interpreta os codigos da coluna`() {
        assertEquals(ApararTipo.DESBALANCEADA, CombatSession.parseAparar("0D").second)
        assertEquals(ApararTipo.ESGRIMA, CombatSession.parseAparar("0E").second)
        assertEquals(ApararTipo.ESGRIMA, CombatSession.parseAparar("F").second)
        assertEquals(ApararTipo.NAO, CombatSession.parseAparar("Não").second)
        assertEquals(-1, CombatSession.parseAparar("-1").first)
        assertEquals(ApararTipo.NORMAL, CombatSession.parseAparar("-1").second)
    }

    @Test
    fun `aparar indisponivel com arma a distancia`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(1))
        val espada = AtaqueHeroi("Espada", nh = 14, danoExpr = "2d", tipo = DanoTipo.CORT)
        assertTrue(s.opcoesDefesaHeroi(armaPronta = espada).any { it.tipo == CombatResolver.TipoDefesa.APARA })
        val rev = AtaqueHeroi("Revólver", nh = 14, danoExpr = "2d", tipo = DanoTipo.PI, aDistancia = true, alcance = 100)
        assertFalse("não se apara com arma à distância", s.opcoesDefesaHeroi(armaPronta = rev).any { it.tipo == CombatResolver.TipoDefesa.APARA })
    }

    @Test
    fun `arma desbalanceada nao apara depois de atacar`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(1))
        val machado = AtaqueHeroi("Machado", nh = 14, danoExpr = "2d", tipo = DanoTipo.CORT, apararTipo = ApararTipo.DESBALANCEADA)
        assertTrue(s.opcoesDefesaHeroi(armaPronta = machado).any { it.tipo == CombatResolver.TipoDefesa.APARA })
        s.heroiAtaca(machado, "goblin", Manobra.ATAQUE, LocalAtaque.TORSO)
        assertFalse("desbalanceada não apara após atacar no mesmo turno",
            s.opcoesDefesaHeroi(armaPronta = machado).any { it.tipo == CombatResolver.TipoDefesa.APARA })
    }

    @Test
    fun `bonus de cadencia de tiro segue a tabela`() {
        assertEquals(0, CombatSession.bonusCadenciaTiro(1))
        assertEquals(0, CombatSession.bonusCadenciaTiro(4))
        assertEquals(1, CombatSession.bonusCadenciaTiro(5))
        assertEquals(2, CombatSession.bonusCadenciaTiro(9))
        assertEquals(3, CombatSession.bonusCadenciaTiro(16))
        assertEquals(4, CombatSession.bonusCadenciaTiro(24))
        assertEquals(6, CombatSession.bonusCadenciaTiro(50))
    }

    @Test
    fun `acertos da rajada = 1 + margem por recuo, limitado aos tiros`() {
        assertEquals(1, CombatSession.acertosDaRajada(margem = 1, recuo = 2, tirosDisparados = 3)) // 1 + 0
        assertEquals(2, CombatSession.acertosDaRajada(margem = 2, recuo = 2, tirosDisparados = 3)) // 1 + 1
        assertEquals(3, CombatSession.acertosDaRajada(margem = 4, recuo = 2, tirosDisparados = 3)) // 1 + 2
        assertEquals(3, CombatSession.acertosDaRajada(margem = 20, recuo = 2, tirosDisparados = 3)) // teto = tiros
        assertEquals(3, CombatSession.acertosDaRajada(margem = 2, recuo = 1, tirosDisparados = 10)) // 1 + 2
    }

    @Test
    fun `rajada aplica multiplos acertos quando a margem cobre o recuo`() {
        var viuRajada = false
        for (seed in 0L..25L) {
            val g = goblin(pv = 40) // sobrevive p/ mostrar a rajada
            val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 3), seed = 1L)
            val s = CombatSession(enc, perfilHeroi(), Random(seed))
            // SMG: NH alto + Recuo 1 (toda margem vira acerto extra) + CdT 10.
            val smg = AtaqueHeroi("SMG", nh = 18, danoExpr = "3d", tipo = DanoTipo.PI, aDistancia = true, alcance = 200, meioDano = 50, cadenciaTiro = 10, recuo = 1)
            s.heroiAtaca(smg, "goblin", Manobra.ATAQUE, LocalAtaque.TORSO)
            if (s.log.any { it.contains("rajada: +") }) { viuRajada = true; break }
        }
        assertTrue("uma rajada com Recuo 1 e margem boa deve cravar tiros extras", viuRajada)
    }

    @Test
    fun `corpo-a-corpo respeita o alcance da arma (reach)`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 2), seed = 1L) // 2m
        val s = CombatSession(enc, perfilHeroi(), Random(2))
        // Espada (reach 1) não alcança um alvo a 2m.
        val r1 = s.heroiAtaca(espada(), "goblin", Manobra.ATAQUE, LocalAtaque.TORSO)
        assertFalse(r1.acertou)
        assertTrue(s.log.last().contains("longe demais"))
        // Lança (reach 2) alcança a 2m e resolve o ataque.
        val lanca = AtaqueHeroi("Lança", nh = 14, danoExpr = "1d+2", tipo = DanoTipo.PERF, alcance = 2)
        s.heroiAtaca(lanca, "goblin", Manobra.ATAQUE, LocalAtaque.TORSO)
        assertTrue("a lança deve alcançar e resolver", s.log.any { it.contains("Lança") })
    }

    @Test
    fun `tiro alem do maximo nao alcanca`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 200), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(1))
        val pistola = AtaqueHeroi("Pistola", nh = 14, danoExpr = "2d", tipo = DanoTipo.PI, aDistancia = true, alcance = 150, precisao = 2, meioDano = 50)
        val r = s.heroiAtaca(pistola, "goblin", Manobra.ATAQUE, LocalAtaque.TORSO)
        assertFalse("além do Máx não acerta", r.acertou)
        assertTrue(s.log.last().contains("fora de alcance"))
    }

    @Test
    fun `tiro alem de meio dano corta o dano pela metade`() {
        var achouMetade = false
        for (seed in 0L..25L) {
            val g = goblin(pv = 20)
            val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 7), seed = 1L)
            val s = CombatSession(enc, perfilHeroi(), Random(seed))
            val arco = AtaqueHeroi("Arco", nh = 16, danoExpr = "2d", tipo = DanoTipo.PERF, aDistancia = true, alcance = 100, precisao = 2, meioDano = 5)
            s.heroiAtaca(arco, "goblin", Manobra.ATAQUE, LocalAtaque.TORSO)
            if (s.log.any { it.contains("1/2D") }) { achouMetade = true; break }
        }
        assertTrue("em algum acerto a 7m (≥ 1/2D 5m) o log deve marcar dano pela metade", achouMetade)
    }

    @Test
    fun `npc com moral baixa e PV no chao foge`() {
        val g = goblin(pv = 10).apply { pvAtual = 1 } // 10% de PV
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(1))
        val intencao = s.npcIntencao("goblin")
        assertTrue("deve recuar", intencao.manobra == Manobra.MOVER && intencao.recuar)
    }

    // ── Lote 377: Ataque Total (Duplo) + sem defesa ativa após Ataque Total (MB p.366) ──

    private fun faca() = AtaqueHeroi("Faca", nh = 14, danoExpr = "1d-1", tipo = DanoTipo.PERF)
    private fun espadaCurta() = AtaqueHeroi("Espada", nh = 14, danoExpr = "1d", tipo = DanoTipo.CORT)

    @Test
    fun `ataque total duplo desfere dois golpes e a 2a arma sofre -4 da mao inabil`() {
        val g = goblin(pv = 40) // sobrevive aos dois golpes p/ expor as duas linhas
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(7))
        val res = s.heroiAtaqueDuplo(espadaCurta(), faca(), "goblin", LocalAtaque.TORSO, ambidestria = false)
        assertEquals("dois golpes resolvidos", 2, res.size)
        assertTrue("cabeçalho do Duplo", s.log.any { it.contains("Ataque Total (Duplo)") })
        // a 2ª arma (Faca) carrega o −4 da mão inábil no colchete técnico
        assertTrue("a 2ª arma deve mostrar a penalidade de mão inábil",
            s.log.any { it.contains("Faca") && it.contains("mão inábil") })
        // Ataque Total → herói sem defesa ativa neste turno
        assertTrue(s.heroiSemDefesaAtiva)
        assertTrue("sem opções de defesa após Ataque Total",
            s.opcoesDefesaHeroi(armaPronta = espadaCurta()).isEmpty())
    }

    @Test
    fun `com ambidestria a 2a arma nao sofre o -4 da mao inabil`() {
        val g = goblin(pv = 40)
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(7))
        s.heroiAtaqueDuplo(espadaCurta(), faca(), "goblin", LocalAtaque.TORSO, ambidestria = true)
        assertFalse("Ambidestria zera a penalidade de mão inábil",
            s.log.any { it.contains("mão inábil") })
    }

    @Test
    fun `apos ataque total o heroi fica sem defesa ativa ate o proximo turno`() {
        val g = goblin(pv = 40)
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(7))
        // antes do Ataque Total a defesa normal está disponível
        assertTrue(s.opcoesDefesaHeroi(armaPronta = espadaCurta()).isNotEmpty())
        s.heroiAtaca(espadaCurta(), "goblin", Manobra.ATAQUE_TOTAL, LocalAtaque.TORSO)
        assertTrue("Ataque Total tira a defesa ativa", s.heroiSemDefesaAtiva)
        assertTrue("sem opções de defesa", s.opcoesDefesaHeroi(armaPronta = espadaCurta()).isEmpty())
        // NPC ataca o herói indefeso → o log avisa a ausência de defesa
        val intencao = s.npcIntencao("goblin")
        s.npcResolve("goblin", intencao, null)
        assertTrue("avisa que o herói está sem defesa", s.log.any { it.contains("sem defesa ativa") })
        // a próxima ação do herói restaura a defesa (passou o turno)
        s.heroiManobra(Manobra.NAO_FAZER_NADA)
        assertFalse(s.heroiSemDefesaAtiva)
        assertTrue(s.opcoesDefesaHeroi(armaPronta = espadaCurta()).isNotEmpty())
    }

    // ── Lote 378: bugfix — Mover e Atacar funcional + expressão de dano sem token duplicado ──

    @Test
    fun `semTokenTipo remove o token de tipo deixando so os dados`() {
        assertEquals("2d-1", CombatSession.semTokenTipo("2d-1 pa"))   // o app mostrava "2d-1 pa pi"
        assertEquals("2d-1", CombatSession.semTokenTipo("2d-1 pa+"))
        assertEquals("1d+2", CombatSession.semTokenTipo("1d+2 corte"))
        assertEquals("3d", CombatSession.semTokenTipo("3d cont"))
        assertEquals("4d", CombatSession.semTokenTipo("4d pa-"))
        assertEquals("2d-1", CombatSession.semTokenTipo("2d-1"))      // sem token: inalterado (não corta o "-1")
    }

    @Test
    fun `mover e atacar corpo-a-corpo aproxima-se do alvo, golpeia e nao apara depois`() {
        val g = goblin(pv = 40)
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 4), seed = 1L) // 4m; herói desloc 6
        val s = CombatSession(enc, perfilHeroi(), Random(7))
        // Espada (reach 1) não alcançaria a 4m num Ataque normal; com Mover e Atacar, avança e golpeia.
        s.heroiMoverEAtacar(espada(), "goblin", LocalAtaque.TORSO)
        assertTrue("avança e ataca em movimento", s.log.any { it.contains("avança sobre") })
        assertTrue("o golpe é resolvido (não 'longe demais')", s.log.any { it.contains("Espada") })
        assertFalse("não pode ficar 'longe demais'", s.log.any { it.contains("longe demais") })
        assertEquals("aproximou-se até o alcance da arma (1m)", 1, s.distancia(g))
        // Mover e Atacar: na defesa seguinte só Esquiva/Bloqueio — sem aparar (MB p.367).
        assertTrue(s.heroiSemAparar)
        val opc = s.opcoesDefesaHeroi(armaPronta = espada())
        assertTrue("Esquiva segue disponível", opc.any { it.tipo == CombatResolver.TipoDefesa.ESQUIVA })
        assertFalse("não apara após Mover e Atacar", opc.any { it.tipo == CombatResolver.TipoDefesa.APARA })
        // a próxima ação restaura o Aparar.
        s.heroiManobra(Manobra.NAO_FAZER_NADA)
        assertFalse(s.heroiSemAparar)
    }

    @Test
    fun `mover e atacar a distancia aplica a penalidade e dispara em movimento`() {
        val g = goblin(pv = 40)
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 5), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(7))
        val rev = AtaqueHeroi("Revólver", nh = 14, danoExpr = "2d-1", tipo = DanoTipo.PI_MAIS, aDistancia = true, alcance = 100, magnitude = -1)
        s.heroiMoverEAtacar(rev, "goblin", LocalAtaque.TORSO)
        assertTrue("dispara em movimento", s.log.any { it.contains("dispara em movimento") })
        assertTrue("o colchete técnico mostra a penalidade de Mover e Atacar", s.log.any { it.contains("Mover e Atacar") })
    }

    // ── Lote 380: BD do escudo só com mão livre e não contra arma de fogo (MB p.375) ──

    @Test
    fun `pareceArmaDeFogo detecta revolver e rifle, ignora arco e espada`() {
        assertTrue(CombatSession.pareceArmaDeFogo("Revólver, .36"))
        assertTrue(CombatSession.pareceArmaDeFogo("Rifle-Mosquete, .577"))
        assertFalse(CombatSession.pareceArmaDeFogo("Arco Longo"))
        assertFalse(CombatSession.pareceArmaDeFogo("Espada Larga"))
        assertFalse(CombatSession.pareceArmaDeFogo(null))
    }

    @Test
    fun `BD do escudo sai da defesa contra arma de fogo e com arma de duas maos (MB p375)`() {
        val perfilComEscudo = HeroiPerfilCombate(esquiva = 9, apara = 11, bloqueio = 10, ht = 12, rd = 2, bonusEscudo = 1)
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilComEscudo, Random(1))
        val espada1mao = AtaqueHeroi("Espada", nh = 14, danoExpr = "2d", tipo = DanoTipo.CORT, duasMaos = false)
        val rifle = AtaqueHeroi("Rifle", nh = 14, danoExpr = "5d", tipo = DanoTipo.PI_MAIS, aDistancia = true, alcance = 100, duasMaos = true)

        fun esquivaDe(opcoes: List<CombatResolver.OpcaoDefesa>) =
            opcoes.first { it.tipo == CombatResolver.TipoDefesa.ESQUIVA }.valorFinal

        // Arma de 1 mão, ataque corpo-a-corpo: o BD (1) entra na Esquiva → 9.
        assertEquals(9, esquivaDe(s.opcoesDefesaHeroi(armaPronta = espada1mao)))
        // Contra arma de fogo: BD sai → Esquiva 8.
        assertEquals(8, esquivaDe(s.opcoesDefesaHeroi(armaPronta = espada1mao, contraArmaDeFogo = true)))
        // Arma de duas mãos (sem mão livre p/ o escudo): BD sai → Esquiva 8.
        assertEquals(8, esquivaDe(s.opcoesDefesaHeroi(armaPronta = rifle)))
    }

    // ── Lote 381: Modificador de Tamanho (MT) do alvo no acerto à distância (MB p.549) ──

    private fun ogro(id: String = "ogro", mt: Int = 2) = Combatente(
        id = id, nome = "Ogro", dx = 11, velocidadeBasica = 5.0, deslocamento = 5, pvMax = 30, pvAtual = 30,
        stats = NpcStats(st = 18, dx = 11, ht = 13, pvMax = 30, armaDano = "2d", armaTipo = "cont", armaNh = 12, alcanceMetros = 1, modificadorTamanho = mt)
    )

    @Test
    fun `MT do alvo grande entra no acerto a distancia`() {
        val g = ogro(mt = 2)
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("ogro" to 10), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(3))
        val arco = AtaqueHeroi("Arco", nh = 14, danoExpr = "1d+1", tipo = DanoTipo.PERF, aDistancia = true, alcance = 100)
        s.heroiAtaca(arco, "ogro", Manobra.ATAQUE, LocalAtaque.TORSO)
        assertTrue("o MT +2 do alvo grande deve entrar na conta do tiro",
            s.log.any { it.contains("tamanho do alvo (MT)") && it.contains("+2") })
    }

    @Test
    fun `MT nao entra no corpo-a-corpo (so a distancia, MB p548-549)`() {
        val g = ogro(mt = 2)
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("ogro" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(3))
        s.heroiAtaca(espada(), "ogro", Manobra.ATAQUE, LocalAtaque.TORSO)
        assertFalse("corpo-a-corpo não soma MT", s.log.any { it.contains("tamanho do alvo (MT)") })
    }

    @Test
    fun `npc que atira no heroi soma o MT do heroi`() {
        val perfilMT = perfilHeroi().copy(modificadorTamanho = 1) // herói é um alvo maior (MT +1)
        val arqueiro = Combatente(
            id = "arqueiro", nome = "Arqueiro", dx = 12, velocidadeBasica = 5.5, deslocamento = 5, pvMax = 10, pvAtual = 10,
            stats = NpcStats(dx = 12, ht = 11, pvMax = 10, armaNome = "Arco", armaDano = "1d+1", armaTipo = "perf", armaNh = 13, alcanceMetros = 100)
        )
        val enc = CombatEncounter(listOf(heroi(), arqueiro), mapOf("arqueiro" to 8), seed = 1L)
        val s = CombatSession(enc, perfilMT, Random(3))
        val intencao = NpcCombatBrain.IntencaoNpc(manobra = Manobra.ATAQUE, alvoId = "heroi", aDistancia = true, motivo = "atira no herói")
        s.npcResolve("arqueiro", intencao, DefesaHeroi(CombatResolver.TipoDefesa.ESQUIVA, 9, soma = 3))
        assertTrue("o MT +1 do herói deve entrar na conta do tiro do NPC",
            s.log.any { it.contains("tamanho do alvo (MT)") && it.contains("+1") })
    }

    // ── Lote 382: Choque (penalidade no próximo turno) + Cambaleante (<1/3 PV) — MB p.419/380 ──

    @Test
    fun `choque do turno anterior penaliza o acerto e expira ao fim do turno`() {
        val g = goblin(pv = 40)
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(7))
        s.heroi.choquePendente = 3 // perdeu 3 PV no turno anterior (PV Inicial 12 < 20 → −3)
        s.heroiAtaca(espada(), "goblin", Manobra.ATAQUE, LocalAtaque.TORSO)
        assertTrue("o golpe deve mostrar a penalidade de choque",
            s.log.any { (it.startsWith("🗡️") || it.startsWith("⭐") || it.startsWith("💥")) && it.contains("choque") && it.contains("-3") })
        s.avancarTurno() // fim do turno do herói
        assertEquals("choque expira após o turno", 0, s.heroi.choquePendente)
    }

    @Test
    fun `ferir acumula choque pendente no alvo`() {
        val g = goblin(pv = 40)
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(7))
        InjuryRules.ferir(g, 5, g.stats?.ht ?: 10, Random(1))
        assertEquals(5, g.choquePendente)
    }

    @Test
    fun `cambaleante reduz esquiva e deslocamento (MB p380)`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 5), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(1))
        val esqNormal = s.opcoesDefesaHeroi(armaPronta = espada())
            .first { it.tipo == CombatResolver.TipoDefesa.ESQUIVA }.valorFinal
        assertFalse(s.heroi.cambaleante)
        s.heroi.pvAtual = 3 // < 1/3 de 12 PV → cambaleante
        assertTrue(s.heroi.cambaleante)
        val esqCambaleante = s.opcoesDefesaHeroi(armaPronta = espada())
            .first { it.tipo == CombatResolver.TipoDefesa.ESQUIVA }.valorFinal
        assertTrue("Esquiva cai quando cambaleante ($esqCambaleante < $esqNormal)", esqCambaleante < esqNormal)
        assertEquals("Deslocamento pela metade (6 → 3)", 3, s.heroi.deslocamentoEfetivo)
    }

    // ── Lote 383: Fintar (Disputa Rápida → reduz a defesa do alvo no próximo golpe, MB p.366) ──

    @Test
    fun `fintaResultado segue a disputa rapida (MB p366)`() {
        assertEquals("fintador falha no próprio teste", 0, CombatSession.fintaResultado(14, 16, 11, 10))
        assertEquals("defensor falhou → margem do atacante", 4, CombatSession.fintaResultado(14, 10, 11, 13))
        assertEquals("ambos passam → margem de vitória (5−2)", 3, CombatSession.fintaResultado(15, 10, 14, 12))
        assertEquals("defensor venceu a disputa → 0", 0, CombatSession.fintaResultado(15, 13, 14, 8))
    }

    @Test
    fun `finta bem-sucedida reduz a defesa do alvo no proximo golpe corpo-a-corpo`() {
        var verificou = false
        for (seed in 0L..30L) {
            val g = goblin(pv = 40)
            val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
            val s = CombatSession(enc, perfilHeroi(), Random(seed))
            s.heroiFintar(espada(), "goblin")
            if (s.log.any { it.contains("engana") }) { // finta venceu a disputa
                s.heroiAtaca(espada(), "goblin", Manobra.ATAQUE, LocalAtaque.TORSO)
                assertTrue("o golpe após a finta deve abater a defesa do alvo",
                    s.log.any { it.contains("finta: a defesa de") })
                verificou = true
                break
            }
        }
        assertTrue("alguma seed deve produzir uma finta bem-sucedida (NH 14 vs 11)", verificou)
    }

    @Test
    fun `nao finta com arma a distancia`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(1))
        val rev = AtaqueHeroi("Revólver", nh = 14, danoExpr = "2d", tipo = DanoTipo.PI, aDistancia = true, alcance = 50)
        s.heroiFintar(rev, "goblin")
        assertTrue("finta exige arma corpo-a-corpo", s.log.any { it.contains("exige uma arma corpo-a-corpo") })
    }

    // ── Lote 384: Tabelas de crítico aplicadas no combate (MB p.557–558) ──

    @Test
    fun `danoMaximo calcula o teto da expressao`() {
        assertEquals(11, CombatSession.danoMaximo("2d-1"))
        assertEquals(12, CombatSession.danoMaximo("2d"))
        assertEquals(8, CombatSession.danoMaximo("1d+2"))
        assertEquals(0, CombatSession.danoMaximo("lixo"))
    }

    @Test
    fun `golpe fulminante aparece no log do combate (NH alto - crítico de 6 ou menos)`() {
        val arma = AtaqueHeroi("Espada", nh = 16, danoExpr = "2d", tipo = DanoTipo.CORT) // NH 16 → decisivo em 6-
        var viu = false
        var seed = 0L
        while (!viu && seed <= 300L) {
            val g = goblin(pv = 80)
            val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
            val s = CombatSession(enc, perfilHeroi(), Random(seed))
            s.heroiAtaca(arma, "goblin", Manobra.ATAQUE, LocalAtaque.TORSO)
            if (s.log.any { it.contains("Golpe Fulminante") }) viu = true
            seed++
        }
        assertTrue("um Golpe Fulminante deve ocorrer com NH 16 em 300 seeds", viu)
    }

    @Test
    fun `erro critico aparece no log do combate (NH baixo - falha por 10+)`() {
        val arma = AtaqueHeroi("Espada", nh = 6, danoExpr = "2d", tipo = DanoTipo.CORT) // NH 6 → falha crítica em 16+
        var viu = false
        var seed = 0L
        while (!viu && seed <= 300L) {
            val g = goblin(pv = 80)
            val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
            val s = CombatSession(enc, perfilHeroi(), Random(seed))
            s.heroiAtaca(arma, "goblin", Manobra.ATAQUE, LocalAtaque.TORSO)
            if (s.log.any { it.contains("Erro crítico") }) viu = true
            seed++
        }
        assertTrue("um Erro crítico deve ocorrer com NH 6 em 300 seeds", viu)
    }

    // ── Lote 386: Luta agarrada (base) — Agarrar/Derrubar/desvencilhar (MB p.370–371) ──

    @Test
    fun `vencaDisputaRapida segue a regra (MB p348)`() {
        assertTrue(CombatSession.vencaDisputaRapida(14, 8, 12, 11))   // A margem 6 > B margem 1
        assertFalse(CombatSession.vencaDisputaRapida(14, 8, 12, 6))   // empate (6 = 6) → A não vence
        assertTrue(CombatSession.vencaDisputaRapida(14, 8, 12, 13))   // B falhou → A vence
        assertFalse(CombatSession.vencaDisputaRapida(14, 16, 12, 13)) // A falhou → não vence
    }

    @Test
    fun `agarrar deixa o NPC preso (AGARRADO)`() {
        var ok = false
        for (seed in 0L..40L) {
            val g = goblin(pv = 40)
            val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
            val s = CombatSession(enc, perfilHeroi(), Random(seed))
            s.heroiAgarrar(espada(), "goblin")
            if (Condicao.AGARRADO in g.condicoes) {
                assertTrue("log do agarrão", s.log.any { it.contains("agarra") })
                ok = true; break
            }
        }
        assertTrue("alguma seed deve conseguir agarrar (NH 14)", ok)
    }

    @Test
    fun `npc agarrado gasta o turno tentando se soltar e nao ataca`() {
        val g = goblin(pv = 40)
        g.condicoes.add(Condicao.AGARRADO)
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(3))
        val intencao = NpcCombatBrain.IntencaoNpc(manobra = Manobra.ATAQUE, alvoId = "heroi", motivo = "ataca")
        val pvAntes = s.heroi.pvAtual
        s.npcResolve("goblin", intencao, null)
        assertTrue("o NPC preso forceja ou se solta",
            s.log.any { it.contains("se desvencilha") || it.contains("forceja") })
        assertEquals("um NPC preso não ataca → herói intacto", pvAntes, s.heroi.pvAtual)
    }

    @Test
    fun `derrubar joga o alvo no chao quando o heroi vence a disputa`() {
        val perfilForte = perfilHeroi().copy(st = 16, dx = 16) // herói forte vs goblin (ST/DX 11)
        var derrubou = false
        for (seed in 0L..40L) {
            val g = goblin(pv = 40)
            val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
            val s = CombatSession(enc, perfilForte, Random(seed))
            s.heroiDerrubar("goblin")
            if (g.postura == Postura.DEITADO && Condicao.CAIDO in g.condicoes) {
                assertTrue(s.log.any { it.contains("derruba") })
                derrubou = true; break
            }
        }
        assertTrue("herói forte deve derrubar o goblin em alguma seed", derrubou)
    }

    // Lote 387 — Ataque Total (Forte): +2 de dano OU +1 por dado, o que for maior; só corpo-a-corpo (MB p.365).
    @Test
    fun `Ataque Total Forte da +2 ou +1 por dado, o que for maior`() {
        val f = { expr: String, dist: Boolean ->
            CombatSession.bonusDanoForte(Manobra.ATAQUE_TOTAL, AtaqueTotalModo.FORTE, expr, dist)
        }
        assertEquals("1 dado: o piso de +2 vale", 2, f("1d+2", false))
        assertEquals("2 dados: +1/dado = +2 (empata com o piso)", 2, f("2d", false))
        assertEquals("3 dados: +1/dado = +3 supera o piso", 3, f("3d-1", false))
        assertEquals("4 dados: +4", 4, f("4d pa+", false))
        // À distância não tem Forte; nem manobra/modo diferente dão bônus.
        assertEquals("à distância não tem Forte", 0, f("3d", true))
        assertEquals(0, CombatSession.bonusDanoForte(Manobra.ATAQUE, AtaqueTotalModo.FORTE, "3d", false))
        assertEquals(0, CombatSession.bonusDanoForte(Manobra.ATAQUE_TOTAL, AtaqueTotalModo.DETERMINADO, "3d", false))
    }

    // Lote 388 — Defesa Total (MB p.366): Aumentada (+2 numa defesa) e Dupla (2ª defesa se a 1ª falhar).
    @Test
    fun `Defesa Total Aumentada soma +2 na defesa escolhida`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(3))
        val base = s.opcoesDefesaHeroi().first { it.tipo == CombatResolver.TipoDefesa.ESQUIVA }.valorFinal
        s.heroiDefesaTotal(DefesaTotalModo.AUMENTADA, CombatResolver.TipoDefesa.ESQUIVA)
        val comBonus = s.opcoesDefesaHeroi().first { it.tipo == CombatResolver.TipoDefesa.ESQUIVA }.valorFinal
        assertEquals("Aumentada soma +2 na Esquiva", base + 2, comBonus)
        // o +2 não vaza para outra defesa
        val apara = s.opcoesDefesaHeroi().first { it.tipo == CombatResolver.TipoDefesa.APARA }
        assertEquals("o +2 vale só na defesa escolhida", 11, apara.valorFinal)
    }

    @Test
    fun `Defesa Total Dupla tenta a 2a defesa quando a 1a falha`() {
        var salvou = false
        for (seed in 0L..60L) {
            val base = goblin()
            val g = base.copy(stats = base.stats!!.copy(armaNh = 16)) // acerta com frequência
            val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
            val s = CombatSession(enc, perfilHeroi(), Random(seed))
            val intencao = NpcCombatBrain.IntencaoNpc(
                manobra = Manobra.ATAQUE, alvoId = "heroi", local = LocalAtaque.TORSO, motivo = "teste"
            )
            val primaria = DefesaHeroi(CombatResolver.TipoDefesa.ESQUIVA, 9, soma = 18)   // sempre falha
            val secundaria = DefesaHeroi(CombatResolver.TipoDefesa.APARA, 11, soma = 3)    // sempre passa
            val pvAntes = s.heroi.pvAtual
            val r = s.npcResolve("goblin", intencao, primaria, secundaria)
            if (r.acertou && s.log.any { it.contains("Defesa Dupla") } && s.heroi.pvAtual == pvAntes) {
                salvou = true; break
            }
        }
        assertTrue("a Defesa Dupla deve salvar o herói em alguma seed com acerto não-crítico", salvou)
    }

    // Lote 389 — Retirada (MB p.377): só contra ataque corpo-a-corpo e 1×/turno.
    @Test
    fun `Retirada so vs corpo-a-corpo e 1x por turno`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(3))
        assertTrue("corpo-a-corpo oferece recuo", s.opcoesDefesaHeroi(contraAtaqueCorpoACorpo = true).any { it.recuo })
        assertFalse("à distância não oferece recuo", s.opcoesDefesaHeroi(contraAtaqueCorpoACorpo = false).any { it.recuo })
        s.heroi.defesasUsadas = s.heroi.defesasUsadas.copy(retracaoUsada = true)
        assertFalse("recuo é 1×/turno", s.opcoesDefesaHeroi(contraAtaqueCorpoACorpo = true).any { it.recuo })
    }

    // Lote 390 — Aparar (MB p.376): aparar um ataque à distância só com o atacante adjacente (≤1m).
    @Test
    fun `aparar tiro so com atacante adjacente`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(3))
        fun temApara(corpoACorpo: Boolean, adjacente: Boolean) =
            s.opcoesDefesaHeroi(contraAtaqueCorpoACorpo = corpoACorpo, atacanteAdjacente = adjacente)
                .any { it.tipo == CombatResolver.TipoDefesa.APARA }
        assertTrue("corpo-a-corpo: apara", temApara(corpoACorpo = true, adjacente = true))
        assertTrue("tiro à queima-roupa: apara a arma", temApara(corpoACorpo = false, adjacente = true))
        assertFalse("tiro de longe: sem aparar", temApara(corpoACorpo = false, adjacente = false))
    }

    // Lote 391 — Aparar Desarmado (MB p.376): aparar uma ARMA com as mãos nuas sofre −3, salvo Caratê/Judô.
    @Test
    fun `aparar desarmado uma arma sofre -3 salvo Carate ou Judo`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(3))
        val punho = AtaqueHeroi(rotulo = "Briga", nh = 12, danoExpr = "1d-1", tipo = DanoTipo.CONT, desarmado = true)
        fun valorApara(arma: AtaqueHeroi, comArma: Boolean) =
            s.opcoesDefesaHeroi(armaPronta = arma, contraAtaqueCorpoACorpo = true, ataqueComArma = comArma)
                .first { it.tipo == CombatResolver.TipoDefesa.APARA && !it.recuo }.valorFinal
        val base = 11 // perfilHeroi().apara
        assertEquals("vs arma, mãos nuas: −3", base - 3, valorApara(punho, comArma = true))
        assertEquals("vs ataque desarmado: sem −3", base, valorApara(punho, comArma = false))
        assertEquals("Caratê/Judô: valor cheio vs arma", base, valorApara(punho.copy(aparaMarcial = true), comArma = true))
    }

    // Lote 392 — Apontar (MB p.364): mira de vários turnos acumula +1 (2º seg) / +2 (3º+); defender perde a mira.
    @Test
    fun `mira de varios turnos acumula +1 depois +2 com teto`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 5), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(3))
        s.heroiApontar("goblin"); assertFalse("1º segundo: sem extra", s.log.last().contains("mira contínua"))
        s.heroiApontar("goblin"); assertTrue("2º segundo: +1", s.log.last().contains("+1 de mira contínua"))
        s.heroiApontar("goblin"); assertTrue("3º segundo: +2", s.log.last().contains("+2 de mira contínua"))
        s.heroiApontar("goblin"); assertTrue("4º: teto +2", s.log.last().contains("+2 de mira contínua"))
    }

    @Test
    fun `defender com defesa ativa perde a mira`() {
        var perdeu = false
        for (seed in 0L..40L) {
            val base = goblin()
            val g = base.copy(stats = base.stats!!.copy(armaNh = 16))
            val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
            val s = CombatSession(enc, perfilHeroi(), Random(seed))
            s.heroiApontar("goblin")
            val intencao = NpcCombatBrain.IntencaoNpc(
                manobra = Manobra.ATAQUE, alvoId = "heroi", local = LocalAtaque.TORSO, motivo = "t"
            )
            s.npcResolve("goblin", intencao, DefesaHeroi(CombatResolver.TipoDefesa.ESQUIVA, 9, 10))
            if (s.log.any { it.contains("perde a mira") }) { perdeu = true; break }
        }
        assertTrue("usar uma defesa ativa deve fazer perder a mira", perdeu)
    }

    // Lote 393 — Fazer Nada / Atordoado (MB p.364): todas as defesas ativas sofrem −4 enquanto atordoado.
    @Test
    fun `atordoado reduz as defesas ativas em -4`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(3))
        fun esquiva() = s.opcoesDefesaHeroi().first { it.tipo == CombatResolver.TipoDefesa.ESQUIVA && !it.recuo }.valorFinal
        fun apara() = s.opcoesDefesaHeroi().first { it.tipo == CombatResolver.TipoDefesa.APARA && !it.recuo }.valorFinal
        val esqN = esquiva(); val aparaN = apara()
        s.heroi.condicoes.add(Condicao.ATORDOADO)
        assertEquals("esquiva −4 atordoado", esqN - 4, esquiva())
        assertEquals("apara −4 atordoado", aparaN - 4, apara())
    }

    // Lote 394 — Disparada (MB p.353): Moves consecutivos dão +20% de Deslocamento a partir do 2º.
    @Test
    fun `disparada da +20% de deslocamento no 2o move consecutivo`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 20), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(3)) // herói deslocamento 6 → +20% = +1
        s.heroiMove(afastar = true, metros = 100)
        assertFalse("1º move: sem disparada", s.log.last().contains("disparada"))
        s.heroiMove(afastar = true, metros = 100)
        assertTrue("2º move consecutivo: disparada +1m", s.log.last().contains("disparada +1m"))
        s.heroiManobra(Manobra.AGUARDAR) // ação não-Move quebra a disparada
        s.heroiMove(afastar = true, metros = 100)
        assertFalse("após outra ação, a disparada reinicia", s.log.last().contains("disparada"))
    }

    // Lote 395 — Apontar: firmar a arma de fogo (+1 Acc) e teste de Vontade ao ser ferido (MB p.364).
    @Test
    fun `firmar a arma de fogo da +1 na precisao do tiro`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 10), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(3))
        val rev = AtaqueHeroi("Revólver", nh = 14, danoExpr = "2d-1 pa+", tipo = DanoTipo.PI_MAIS,
            aDistancia = true, alcance = 1700, precisao = 2, armaDeFogo = true)
        s.heroiApontar("goblin", firmado = true)
        assertTrue("a declaração mostra o firmar", s.log.last().contains("firmando"))
        s.heroiAtaca(rev, "goblin", Manobra.ATAQUE, LocalAtaque.TORSO)
        val tiro = s.log.last { it.startsWith("🎯") || it.startsWith("⭐") || it.startsWith("💥") }
        assertTrue("o tiro deve somar o firmar: $tiro", tiro.contains("firmar"))
    }

    @Test
    fun `ser ferido mirando dispara o teste de Vontade da mira`() {
        var testou = false
        for (seed in 0L..250L) {
            val base = goblin()
            val g = base.copy(stats = base.stats!!.copy(armaNh = 16)) // crítico ocasional anula a defesa
            val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
            val s = CombatSession(enc, perfilHeroi().copy(rd = 0), Random(seed))
            s.heroiApontar("goblin")
            val intencao = NpcCombatBrain.IntencaoNpc(
                manobra = Manobra.ATAQUE, alvoId = "heroi", local = LocalAtaque.TORSO, motivo = "t"
            )
            s.npcResolve("goblin", intencao, DefesaHeroi(CombatResolver.TipoDefesa.ESQUIVA, 9, 18))
            if (s.log.any { it.contains("Vontade") && (it.contains("perder a mira") || it.contains("mantém a mira")) }) {
                testou = true; break
            }
        }
        assertTrue("um acerto que fere o herói mirando deve testar a Vontade da mira", testou)
    }

    // Lote 396 — Fogo de Retenção (MB p.409): arma CdT 5+ cobre a área; quem AVANÇA leva uma rajada.
    @Test
    fun `fogo de retencao alveja o NPC que avanca`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 10), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(3))
        val mg = AtaqueHeroi("Metralhadora", nh = 14, danoExpr = "5d", tipo = DanoTipo.PI,
            aDistancia = true, alcance = 1000, cadenciaTiro = 10, recuo = 2, armaDeFogo = true)
        s.heroiFogoRetencao(mg)
        assertTrue("declara o fogo de retenção", s.log.last().contains("FOGO DE RETENÇÃO"))
        val avanca = NpcCombatBrain.IntencaoNpc(manobra = Manobra.MOVER, alvoId = "goblin", recuar = false, motivo = "avança")
        s.npcResolve("goblin", avanca)
        assertTrue("o NPC que avança leva fogo de retenção", s.log.any { it.contains("Fogo de retenção") && it.contains("alvejado") })
    }

    @Test
    fun `fogo de retencao exige arma CdT 5+`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 10), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(3))
        val pistola = AtaqueHeroi("Pistola", nh = 14, danoExpr = "2d", tipo = DanoTipo.PI,
            aDistancia = true, alcance = 100, cadenciaTiro = 3, armaDeFogo = true)
        s.heroiFogoRetencao(pistola)
        assertTrue("CdT < 5 recusa", s.log.last().contains("exige uma arma à distância com CdT 5+"))
    }

    // Lote 397 — Concentrar (MB p.344): ser forçado a defender / ser ferido exige Vontade-3 p/ manter a concentração.
    @Test
    fun `concentrar testa Vontade-3 ao ser perturbado`() {
        var testou = false
        for (seed in 0L..60L) {
            val base = goblin()
            val g = base.copy(stats = base.stats!!.copy(armaNh = 16)) // acerta com frequência → perturba
            val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
            val s = CombatSession(enc, perfilHeroi(), Random(seed))
            s.heroiManobra(Manobra.CONCENTRAR)
            assertTrue("declara a concentração", s.log.last().contains("concentra"))
            val intencao = NpcCombatBrain.IntencaoNpc(
                manobra = Manobra.ATAQUE, alvoId = "heroi", local = LocalAtaque.TORSO, motivo = "t"
            )
            s.npcResolve("goblin", intencao, DefesaHeroi(CombatResolver.TipoDefesa.ESQUIVA, 9, 10))
            if (s.log.any { it.contains("Vontade-3") }) { testou = true; break }
        }
        assertTrue("ser perturbado durante a concentração deve testar Vontade-3", testou)
    }

    // Lote 398 — Armas Preparadas / Preparar (MB p.270): desbalanceada fica despreparada após atacar (ST < 1,5× mín) → Preparar.
    @Test
    fun `arma desbalanceada fica despreparada e exige Preparar`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(3)) // herói ST 10 < 1,5×12 = 18 → despreparada
        val machado = AtaqueHeroi("Machado grande", nh = 12, danoExpr = "3d", tipo = DanoTipo.CORT,
            apararTipo = ApararTipo.DESBALANCEADA, stMinimo = 12)
        s.heroiAtaca(machado, "goblin", Manobra.ATAQUE, LocalAtaque.TORSO)
        assertTrue("fica despreparada após o golpe", s.armaDespreparada(machado.rotulo))
        val r = s.heroiAtaca(machado, "goblin", Manobra.ATAQUE, LocalAtaque.TORSO)
        assertTrue("atacar de novo é bloqueado: ${r.texto}", r.texto.contains("despreparada"))
        s.heroiManobra(Manobra.PREPARAR)
        assertFalse("Preparar re-empunha a arma", s.armaDespreparada(machado.rotulo))
    }

    @Test
    fun `arma desbalanceada nao desprepara se o heroi e forte o bastante`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi().copy(st = 18), Random(3)) // ST 18 ≥ 1,5×12 = 18
        val machado = AtaqueHeroi("Machado grande", nh = 12, danoExpr = "3d", tipo = DanoTipo.CORT,
            apararTipo = ApararTipo.DESBALANCEADA, stMinimo = 12)
        s.heroiAtaca(machado, "goblin", Manobra.ATAQUE, LocalAtaque.TORSO)
        assertFalse("ST ≥ 1,5× a mínima não desprepara", s.armaDespreparada(machado.rotulo))
    }

    // Lote 399 — Aguardar / Interromper Investida (MB p.392): arma perfurante firmada golpeia primeiro quem avança.
    @Test
    fun `aguardar interrompe a investida de quem avanca`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 6), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(3))
        val lanca = AtaqueHeroi("Lança", nh = 14, danoExpr = "1d+2", tipo = DanoTipo.PERF, alcance = 2)
        s.heroiAguardar(lanca)
        assertTrue("firma a arma perfurante", s.log.last().contains("AGUARDA firmando"))
        val avanca = NpcCombatBrain.IntencaoNpc(manobra = Manobra.MOVER, alvoId = "goblin", recuar = false, motivo = "investe")
        s.npcResolve("goblin", avanca)
        assertTrue("golpeia primeiro quem investe", s.log.any { it.contains("Investida!") && it.contains("golpeia primeiro") })
    }

    @Test
    fun `aguardar sem arma perfurante e so um aguardar generico`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 6), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(3))
        val espada = AtaqueHeroi("Espada", nh = 14, danoExpr = "2d", tipo = DanoTipo.CORT, alcance = 1)
        s.heroiAguardar(espada)
        assertTrue("aguardar genérico sem bônus", s.log.last().contains("sem o bônus de Interromper Investida") ||
            s.log.last().contains("não há o bônus"))
    }

    // Lote 400 — Movimento (MB p.368): a postura reduz o Deslocamento (1/3 ajoelhado/rastejando, 1 deitado, 0 sentado).
    @Test
    fun `postura reduz o deslocamento efetivo`() {
        val c = Combatente(id = "c", nome = "C", dx = 10, velocidadeBasica = 6.0, deslocamento = 6, pvMax = 10, pvAtual = 10)
        c.postura = Postura.EM_PE; assertEquals("em pé: cheio", 6, c.deslocamentoEfetivo)
        c.postura = Postura.AJOELHADO; assertEquals("ajoelhado: 1/3", 2, c.deslocamentoEfetivo)
        c.postura = Postura.RASTEJANDO; assertEquals("rastejando: 1/3", 2, c.deslocamentoEfetivo)
        c.postura = Postura.DEITADO; assertEquals("deitado: 1", 1, c.deslocamentoEfetivo)
        c.postura = Postura.SENTADO; assertEquals("sentado: 0", 0, c.deslocamentoEfetivo)
    }

    // Lote 401 — Ataque Enganoso (MB p.369): −2 no acerto por passo, em troca de −1 na defesa do alvo.
    @Test
    fun `ataque enganoso registra a penalidade de acerto`() {
        val g = goblin()
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfilHeroi(), Random(3))
        val espada = AtaqueHeroi("Espada", nh = 16, danoExpr = "2d", tipo = DanoTipo.CORT)
        s.heroiAtaca(espada, "goblin", Manobra.ATAQUE, LocalAtaque.TORSO, enganoso = 2)
        assertTrue("o golpe registra o ataque enganoso (−4 no acerto)", s.log.any { it.contains("ataque enganoso") })
    }
}
