package com.gurps.ficha.domain.combat

import com.gurps.ficha.domain.magic.ContextoConjuracao
import com.gurps.ficha.domain.magic.MagiaMecanica
import com.gurps.ficha.domain.magic.MagicCasting
import com.gurps.ficha.domain.magic.MagicClassParser
import com.gurps.ficha.domain.magic.MagicEnergy
import com.gurps.ficha.domain.magic.MagicMechanics
import com.gurps.ficha.domain.magic.NivelMana
import com.gurps.ficha.domain.magic.TipoDuracao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Lote MA-3a: conjuração no combate — o motor resolve a espinha e aplica o dano de Projétil. */
class MagicCombatTest {

    private fun heroi() = Combatente(
        id = "heroi", nome = "Herói", ehHeroi = true, dx = 13, velocidadeBasica = 6.0,
        deslocamento = 6, pvMax = 12, pvAtual = 12, pfAtual = 10
    )

    private fun goblin(pv: Int = 7) = Combatente(
        id = "goblin", nome = "Goblin", dx = 11, velocidadeBasica = 5.0, deslocamento = 5, pvMax = pv, pvAtual = pv,
        stats = NpcStats(st = 11, dx = 11, ht = 11, pvMax = pv, rd = 0, armaDano = "1d-1", armaTipo = "corte", armaNh = 11)
    )

    private fun perfil() = HeroiPerfilCombate(esquiva = 9, apara = 11, ht = 12, rd = 2)

    private fun sessao(seed: Long, distGoblin: Int = 5): CombatSession {
        val enc = CombatEncounter(listOf(heroi(), goblin()), mapOf("goblin" to distGoblin), seed = 1L)
        return CombatSession(enc, perfil(), Random(seed))
    }

    private fun ctxProjetil(nh: Int, dist: Int = 5) = ContextoConjuracao(
        nhBasico = nh, classe = MagicClassParser.parse("Projétil"), mana = NivelMana.NORMAL,
        distanciaMetros = dist, tocando = false
    )

    @Test
    fun `projetil com NH alto causa dano no alvo e gasta fadiga do heroi`() {
        // NH 30: só a rolagem 18 falharia. Busca um seed com sucesso (a esmagadora maioria).
        var achou = false
        for (seed in 0L until 20L) {
            val s = sessao(seed)
            val goblinPvAntes = s.encounter.combatentes.first { it.id == "goblin" }.pvMax
            val pfAntes = s.heroi.pfAtual
            val r = s.heroiConjurar(ctxProjetil(nh = 30), MagicEnergy.parse("Varia"), energiaInvestida = 3, magiaNome = "Bola de Fogo", alvoId = "goblin")
            if (r.sucesso && r.danoCausado > 0) {
                assertTrue("goblin devia perder PV", s.encounter.combatentes.first { it.id == "goblin" }.pvAtual < goblinPvAntes)
                assertTrue("herói gasta PF", s.heroi.pfAtual <= pfAntes)
                assertTrue(s.log.any { it.contains("Bola de Fogo") })
                achou = true; break
            }
        }
        assertTrue("nenhum seed produziu um projétil bem-sucedido", achou)
    }

    @Test
    fun `RD do alvo reduz o dano do projetil`() {
        // Goblin com RD 6 sofre bem menos que sem RD, para a mesma energia/seed.
        val seed = 3L
        val encRd = CombatEncounter(
            listOf(heroi(), goblin().copy(stats = goblin().stats!!.copy(rd = 6))),
            mapOf("goblin" to 5), seed = 1L
        )
        val sRd = CombatSession(encRd, perfil(), Random(seed))
        val rRd = sRd.heroiConjurar(ctxProjetil(30), MagicEnergy.parse("Varia"), 3, "Bola de Fogo", "goblin")
        val sSem = sessao(seed)
        val rSem = sSem.heroiConjurar(ctxProjetil(30), MagicEnergy.parse("Varia"), 3, "Bola de Fogo", "goblin")
        if (rRd.sucesso && rSem.sucesso) {
            assertTrue("RD deveria reduzir o dano (${rRd.danoCausado} vs ${rSem.danoCausado})", rRd.danoCausado <= rSem.danoCausado)
        }
    }

    @Test
    fun `conjuracao sempre registra o fato no log e nunca aumenta a fadiga`() {
        for (seed in 0L until 10L) {
            val s = sessao(seed)
            val pfAntes = s.heroi.pfAtual
            s.heroiConjurar(ctxProjetil(15), MagicEnergy.parse("Varia"), 2, "Relâmpago", "goblin")
            assertTrue("log vazio no seed $seed", s.log.any { it.contains("Relâmpago") })
            assertTrue("PF não pode subir", s.heroi.pfAtual <= pfAntes)
        }
    }

    @Test
    fun `automagia (sem alvo) nao aplica dano de projetil e resolve o lancamento`() {
        val s = sessao(2L)
        val ctx = ContextoConjuracao(nhBasico = 20, classe = MagicClassParser.parse("Comum"), mana = NivelMana.NORMAL)
        val r = s.heroiConjurar(ctx, MagicEnergy.parse("2"), energiaInvestida = 1, magiaNome = "Escudo", alvoId = null)
        assertTrue(s.log.any { it.contains("Escudo") })
        assertTrue("automagia não causa dano de projétil", r.danoCausado == 0)
    }

    // ── Lote MA-3b ──

    @Test
    fun `projetil pode ser ESQUIVADO ou passar longe — nem todo lancamento bem-sucedido fere`() {
        // Goblin bem esquivo + distância → em vários seeds, algum lançamento bem-sucedido NÃO fere
        // (o alvo esquiva ou o Ataque Inato erra). Prova que o 2º teste/esquiva do MA-3b existe.
        val esquivo = NpcStats(st = 11, dx = 15, ht = 14, pvMax = 8, rd = 0, armaNh = 11) // Vel.Básica alta → Esquiva alta
        var sucessoSemDano = false
        for (seed in 0L until 40L) {
            val enc = CombatEncounter(
                listOf(heroi(), Combatente(id = "goblin", nome = "Goblin", dx = 15, velocidadeBasica = 7.0, deslocamento = 7, pvMax = 8, pvAtual = 8, stats = esquivo)),
                mapOf("goblin" to 10), seed = 1L
            )
            val s = CombatSession(enc, perfil(), Random(seed))
            val r = s.heroiConjurar(ctxProjetil(nh = 30, dist = 10), MagicEnergy.parse("Varia"), 1, "Bola de Fogo", "goblin")
            if (r.sucesso && r.danoCausado == 0 &&
                s.log.any { it.contains("ESQUIVA") || it.contains("passa longe") }) {
                sucessoSemDano = true; break
            }
        }
        assertTrue("nenhum seed mostrou esquiva/erro do projétil — o 2º teste não está agindo", sucessoSemDano)
    }

    // ── Lote MA-3d: magia de área ──

    private fun sessaoComDoisGoblins(seed: Long): CombatSession {
        val g1 = goblin().copy(id = "g1", nome = "Goblin 1")
        val g2 = goblin().copy(id = "g2", nome = "Goblin 2")
        val enc = CombatEncounter(listOf(heroi(), g1, g2), mapOf("g1" to 5, "g2" to 5), seed = 1L)
        return CombatSession(enc, perfil(), Random(seed))
    }

    @Test
    fun `area sem resistencia atinge todos os alvos passados e gasta custo x raio`() {
        val s = sessaoComDoisGoblins(1L)
        val pfAntes = s.heroi.pfAtual
        val ctx = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Área"),
            mana = NivelMana.NORMAL, raioAreaMetros = 2)
        val r = s.heroiConjurarArea(ctx, MagicEnergy.parse("2"), energiaInvestida = 2, magiaNome = "Tremor", alvosNaArea = listOf("g1", "g2"))
        if (r.sucesso) {
            assertTrue(s.log.any { it.contains("Atinge") && it.contains("Goblin 1") && it.contains("Goblin 2") })
            // custo base 2 × raio 2 = 4 (NH 25 reduz −3 → 1). PF caiu.
            assertTrue("gastou PF pelo custo de área", s.heroi.pfAtual < pfAntes)
        }
    }

    @Test
    fun `area vazia (nenhum alvo no raio) resolve mas nao atinge ninguem`() {
        val s = sessaoComDoisGoblins(2L)
        val ctx = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Área"),
            mana = NivelMana.NORMAL, raioAreaMetros = 1)
        val r = s.heroiConjurarArea(ctx, MagicEnergy.parse("2"), 1, "Tremor", alvosNaArea = emptyList())
        if (r.sucesso) assertTrue(s.log.any { it.contains("Nenhum inimigo na área") })
    }

    @Test
    fun `area resistivel separa quem foi atingido de quem resistiu`() {
        // Alvos com resistências diferentes → em algum seed, um é atingido e outro resiste.
        var separou = false
        for (seed in 0L until 40L) {
            val fraco = goblin().copy(id = "g1", nome = "Fraco", stats = goblin().stats!!.copy(ht = 8, iq = 8))
            val forte = goblin().copy(id = "g2", nome = "Forte", stats = goblin().stats!!.copy(ht = 15, iq = 15))
            val enc = CombatEncounter(listOf(heroi(), fraco, forte), mapOf("g1" to 5, "g2" to 5), seed = 1L)
            val s = CombatSession(enc, perfil(), Random(seed))
            val ctx = ContextoConjuracao(nhBasico = 16, classe = MagicClassParser.parse("Área/R-HT"),
                mana = NivelMana.NORMAL, raioAreaMetros = 2)
            val r = s.heroiConjurarArea(ctx, MagicEnergy.parse("2"), 2, "Sono Coletivo", listOf("g1", "g2"))
            if (r.sucesso && s.log.any { it.contains("Atinge") } && s.log.any { it.contains("Resistiram") }) {
                separou = true; break
            }
        }
        assertTrue("nenhum seed separou atingidos de resistentes na área", separou)
    }

    // ── Lote MA-7: NPC conjurador ──

    // Lote MEC-8: a magia do NPC é uma magia REAL do catálogo (Bola de Fogo), não a inventada
    // "Dardo Mágico" (que nem existe em GURPS).
    private fun conjuradorNpc() = Combatente(
        id = "mago", nome = "Mago", dx = 11, velocidadeBasica = 5.0, deslocamento = 5, pvMax = 9, pvAtual = 9, pfAtual = 10,
        stats = NpcStats(st = 10, dx = 11, iq = 13, ht = 11, pvMax = 9, rd = 0, armaNh = 10,
            magias = listOf(NpcMagia(nome = "Bola de Fogo", nh = 15, projetil = true, custoFP = 1, danoDados = 1)))
    )

    @Test
    fun `cerebro do NPC conjurador decide LANCAR a magia no heroi`() {
        val enc = CombatEncounter(listOf(heroi(), conjuradorNpc()), mapOf("mago" to 5), seed = 1L)
        val intencao = NpcCombatBrain.decidir(enc.combatentes.first { it.id == "mago" }, enc, "heroi", Random(3))
        assertTrue("deveria intencionar conjurar", intencao.conjurar != null)
        assertEquals("Bola de Fogo", intencao.conjurar!!.nome)
    }

    @Test
    fun `npcConjurar gasta o PF do NPC e resolve (dano no heroi ou esquiva)`() {
        val perfilBaixaEsquiva = HeroiPerfilCombate(esquiva = 3, apara = 11, ht = 12, rd = 0) // esquiva baixa → costuma acertar
        var feriu = false
        for (seed in 0L until 20L) {
            val e = CombatEncounter(listOf(heroi(), conjuradorNpc()), mapOf("mago" to 5), seed = 1L)
            val s = CombatSession(e, perfilBaixaEsquiva, Random(seed))
            val pfNpcAntes = s.inimigos.first { it.id == "mago" }.pfAtual
            val pvHeroiAntes = s.heroi.pvAtual
            s.npcConjurar("mago", NpcMagia("Bola de Fogo", nh = 30, projetil = true, custoFP = 1, danoDados = 2))
            assertTrue("NPC gasta PF", s.inimigos.first { it.id == "mago" }.pfAtual < pfNpcAntes)
            assertTrue(s.log.any { it.contains("Bola de Fogo") })
            if (s.heroi.pvAtual < pvHeroiAntes) { feriu = true; break }
        }
        assertTrue("com esquiva 3 e NH 30, algum seed deveria ferir o herói", feriu)
    }

    @Test
    fun `MEC-8 a esquiva do NPC-magia usa a ROLAGEM do jogador, nao uma automatica`() {
        // A reclamação do usuário: "não tive rolagem de esquiva". Agora a defesa é interativa — a soma
        // vem do card. Esquiva alta + rolagem baixa (3) = sempre esquiva; o herói não pode ser ferido.
        val enc = CombatEncounter(listOf(heroi(), conjuradorNpc()), mapOf("mago" to 5), seed = 1L)
        val s = CombatSession(enc, HeroiPerfilCombate(esquiva = 12, apara = 11, ht = 12, rd = 0), Random(1))
        val pvAntes = s.heroi.pvAtual
        val defesaBoa = DefesaHeroi(CombatResolver.TipoDefesa.ESQUIVA, valorFinal = 12, soma = 3)
        val r = s.npcConjurar("mago", NpcMagia("Bola de Fogo", nh = 30, projetil = true, custoFP = 1, danoDados = 2), defesaBoa)
        assertEquals("esquiva com rolagem 3 vs Esquiva 12 é sucesso garantido", pvAntes, s.heroi.pvAtual)
        assertTrue(s.log.any { it.contains("ESQUIVA") && it.contains("rolou 3") })
    }

    @Test
    fun `MEC-8 rolagem RUIM do jogador deixa a Bola de Fogo acertar`() {
        val enc = CombatEncounter(listOf(heroi(), conjuradorNpc()), mapOf("mago" to 5), seed = 1L)
        val s = CombatSession(enc, HeroiPerfilCombate(esquiva = 8, apara = 11, ht = 12, rd = 0), Random(1))
        val pvAntes = s.heroi.pvAtual
        val defesaRuim = DefesaHeroi(CombatResolver.TipoDefesa.ESQUIVA, valorFinal = 8, soma = 18) // 18 falha sempre
        s.npcConjurar("mago", NpcMagia("Bola de Fogo", nh = 30, projetil = true, custoFP = 1, danoDados = 2), defesaRuim)
        assertTrue("rolagem 18 falha a esquiva → a magia fere", s.heroi.pvAtual < pvAntes)
    }

    @Test
    fun `NPC sem PF nao conjura (o cerebro cai para acao mundana)`() {
        val semPf = conjuradorNpc().let { it.copy(pfAtual = 0) }
        val enc = CombatEncounter(listOf(heroi(), semPf), mapOf("mago" to 5), seed = 1L)
        val intencao = NpcCombatBrain.decidir(enc.combatentes.first { it.id == "mago" }, enc, "heroi", Random(1))
        assertTrue("sem PF não conjura", intencao.conjurar == null)
    }

    // ── Lote COND-1: condições mágicas ──

    @Test
    fun `magia de condicao (Sono) impoe DORMINDO no alvo nao resistido`() {
        val s = sessao(1L)
        val mec = com.gurps.ficha.domain.magic.MagiaMecanica(efeito = "condicao", condicao = "sono")
        // Comum sem resistência (classe sem R-XXX) → sucesso não resistido impõe a condição.
        val ctx = ContextoConjuracao(nhBasico = 30, classe = MagicClassParser.parse("Comum"),
            mana = NivelMana.NORMAL, distanciaMetros = 2, mecanica = mec)
        val r = s.heroiConjurar(ctx, MagicEnergy.parse("2"), 1, "Sono", alvoId = "goblin")
        if (r.sucesso) {
            assertTrue("alvo deve ficar DORMINDO", s.encounter.combatentes.first { it.id == "goblin" }.condicoes.contains(Condicao.DORMINDO))
        }
    }

    @Test
    fun `quem dorme so pode nao fazer nada (indefeso)`() {
        val g = goblin().apply { condicoes.add(Condicao.DORMINDO) }
        val enc = CombatEncounter(listOf(heroi(), g), mapOf("goblin" to 1), seed = 1L)
        assertEquals(listOf(Manobra.NAO_FAZER_NADA), enc.manobrasLegais(g))
    }

    @Test
    fun `dormindo ACORDA ao levar dano (choque pendente)`() {
        val s = sessao(1L)
        val g = s.inimigos.first { it.id == "goblin" }
        g.condicoes.add(Condicao.DORMINDO); g.choquePendente = 3 // levou dano
        s.avancarTurno()
        assertFalse("deveria acordar", g.condicoes.contains(Condicao.DORMINDO))
    }

    @Test
    fun `paralisado NAO acorda ao levar dano`() {
        val s = sessao(1L)
        val g = s.inimigos.first { it.id == "goblin" }
        g.condicoes.add(Condicao.PARALISADO); g.choquePendente = 3
        s.avancarTurno()
        assertTrue("paralisia não acorda com dano", g.condicoes.contains(Condicao.PARALISADO))
    }

    @Test
    fun `silenciado bloqueia a conjuracao do heroi`() {
        val h = heroi().apply { condicoes.add(Condicao.SILENCIADO) }
        val enc = CombatEncounter(listOf(h, goblin()), mapOf("goblin" to 5), seed = 1L)
        val s = CombatSession(enc, perfil(), Random(1))
        val ctx = ContextoConjuracao(nhBasico = 30, classe = MagicClassParser.parse("Comum"), mana = NivelMana.NORMAL)
        val r = s.heroiConjurar(ctx, MagicEnergy.parse("2"), 1, "Luz", alvoId = null)
        assertFalse(r.sucesso)
        assertTrue(s.log.any { it.contains("SILENCIADO") })
    }

    // ── Lote MA-8: descrição do efeito no log (para o Narrador) ──

    @Test
    fun `magia narrada leva o RESUMO do efeito ao log (o Narrador le o feed)`() {
        val s = sessao(2L)
        val ctx = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Comum"),
            mana = NivelMana.NORMAL, resumoEfeito = "Faz o alvo cair no sono se falhar num teste de HT.")
        val r = s.heroiConjurar(ctx, MagicEnergy.parse("2"), 1, "Sono", alvoId = "goblin")
        if (r.sucesso) {
            assertTrue("o log deve conter o resumo do efeito pro Narrador",
                s.log.any { it.contains("Faz o alvo cair no sono") })
        }
    }

    // ── Lote AR-1: dano estruturado (mecanica curada do catálogo) ──

    @Test
    fun `Toque Chocante (mecanica ignora armadura) fere mesmo com RD alta`() {
        // Comum de dano com mecanica: 1d+1/energia, ignora armadura. Goblin RD 6 → dano mesmo assim.
        val gobRd = goblin().copy(stats = goblin().stats!!.copy(rd = 6))
        val enc = CombatEncounter(listOf(heroi(), gobRd), mapOf("goblin" to 1), seed = 1L)
        val s = CombatSession(enc, perfil(), Random(1))
        val mec = com.gurps.ficha.domain.magic.MagiaMecanica(efeito = "dano", danoPorEnergia = "1d+1",
            energiaPorDado = 1, tipoDano = "quei", armadura = "ignora")
        val ctx = ContextoConjuracao(nhBasico = 30, classe = MagicClassParser.parse("Comum"),
            mana = NivelMana.NORMAL, distanciaMetros = 1, mecanica = mec)
        val r = s.heroiConjurar(ctx, MagicEnergy.parse("1 a 3"), energiaInvestida = 2, magiaNome = "Toque Chocante", alvoId = "goblin")
        if (r.sucesso) assertTrue("ignora armadura deve ferir mesmo com RD 6", r.danoCausado > 0)
    }

    @Test
    fun `Relampago (mecanica) pode ATORDOAR o alvo alem do dano`() {
        var atordoou = false
        for (seed in 0L until 40L) {
            val enc = CombatEncounter(listOf(heroi(), goblin(pv = 20)), mapOf("goblin" to 5), seed = 1L)
            val s = CombatSession(enc, perfil(), Random(seed))
            val mec = com.gurps.ficha.domain.magic.MagiaMecanica(efeito = "dano", danoPorEnergia = "1d-1",
                energiaPorDado = 1, tipoDano = "quei", condicao = "atordoado", condicaoResistencia = "HT_por_pv")
            val ctx = ContextoConjuracao(nhBasico = 30, classe = MagicClassParser.parse("Projétil"),
                mana = NivelMana.NORMAL, distanciaMetros = 5, mecanica = mec)
            s.heroiConjurar(ctx, MagicEnergy.parse("1 a AM"), energiaInvestida = 5, magiaNome = "Relâmpago", alvoId = "goblin")
            if (s.encounter.combatentes.first { it.id == "goblin" }.condicoes.contains(Condicao.ATORDOADO)) { atordoou = true; break }
        }
        assertTrue("Relâmpago com 5 de energia deveria atordoar em algum seed", atordoou)
    }

    // ── Lote MA-6: dano de magia direta (não-Projétil) ──

    @Test
    fun `magia Comum com causa dano aplica dano direto no alvo (sem teste de acerto)`() {
        val s = sessao(1L)
        val goblinPvMax = s.encounter.combatentes.first { it.id == "goblin" }.pvMax
        // Jato de Chamas é Comum; com danoPorEnergia = true, sucesso aplica 1d×energia com RD.
        val ctx = ContextoConjuracao(nhBasico = 30, classe = MagicClassParser.parse("Comum"),
            mana = NivelMana.NORMAL, distanciaMetros = 2, danoPorEnergia = true)
        val r = s.heroiConjurar(ctx, MagicEnergy.parse("1 a 3"), energiaInvestida = 3, magiaNome = "Jato de Chamas", alvoId = "goblin")
        if (r.sucesso) {
            assertTrue("Comum com dano deve ferir o alvo", s.encounter.combatentes.first { it.id == "goblin" }.pvAtual < goblinPvMax)
            assertTrue(r.danoCausado > 0)
        }
    }

    @Test
    fun `magia Comum SEM causa dano continua narrada (nao fere)`() {
        val s = sessao(1L)
        val pvMax = s.encounter.combatentes.first { it.id == "goblin" }.pvMax
        val ctx = ContextoConjuracao(nhBasico = 30, classe = MagicClassParser.parse("Comum"),
            mana = NivelMana.NORMAL, distanciaMetros = 2, danoPorEnergia = false)
        val r = s.heroiConjurar(ctx, MagicEnergy.parse("2"), 1, "Detectar Magia", alvoId = "goblin")
        if (r.sucesso) {
            assertEquals("sem causa dano, não fere", pvMax, s.encounter.combatentes.first { it.id == "goblin" }.pvAtual)
            assertTrue(s.log.any { it.contains("narrado pelo Mestre") })
        }
    }

    @Test
    fun `area com causa dano fere TODOS os atingidos`() {
        val s = sessaoComDoisGoblins(1L)
        val ctx = ContextoConjuracao(nhBasico = 30, classe = MagicClassParser.parse("Área"),
            mana = NivelMana.NORMAL, raioAreaMetros = 2, danoPorEnergia = true)
        val r = s.heroiConjurarArea(ctx, MagicEnergy.parse("2"), energiaInvestida = 2, magiaNome = "Tempestade de Fogo", alvosNaArea = listOf("g1", "g2"))
        if (r.sucesso) {
            assertTrue("g1 ferido", s.encounter.combatentes.first { it.id == "g1" }.pvAtual < 7)
            assertTrue("g2 ferido", s.encounter.combatentes.first { it.id == "g2" }.pvAtual < 7)
            assertTrue(s.log.any { it.contains("Dano") })
        }
    }

    // ── Lote MA-3d-4: magias ativas + tick ──

    @Test
    fun `magia ativa cobra manutencao ao completar o intervalo e expira a duradoura`() {
        val s = sessao(1L)
        // Temporária de 2s, manutenção 1 PF: após 2 turnos do herói, cobra 1 PF e reseta.
        s.registrarMagiaAtiva("Escudo", "heroi", null, duracaoSeg = 2, custoManutencaoSeg = 1,
            duracao = com.gurps.ficha.domain.magic.TipoDuracao.TEMPORARIA, exigeConcentracao = false)
        assertTrue(s.magiasAtivas.isNotEmpty())
        val pfAntes = s.heroi.pfAtual
        // avancarTurno cobra quando o HERÓI termina o turno (1s cada). No começo é a vez do herói.
        s.avancarTurno() // herói → goblin (1s de manutenção decrementado)
        s.avancarTurno() // goblin → herói
        s.avancarTurno() // herói → goblin (2º segundo → cobra 1 PF, reseta)
        assertTrue("manutenção deve ter cobrado PF em algum tick", s.heroi.pfAtual < pfAntes)
    }

    @Test
    fun `magia ativa permanente nao cobra nem expira`() {
        val s = sessao(1L)
        s.registrarMagiaAtiva("Zumbi", "heroi", null, duracaoSeg = 999, custoManutencaoSeg = 0,
            duracao = com.gurps.ficha.domain.magic.TipoDuracao.PERMANENTE, exigeConcentracao = false)
        val pfAntes = s.heroi.pfAtual
        repeat(6) { s.avancarTurno() }
        assertTrue("permanente não expira", s.magiasAtivas.any { it.magiaId == "Zumbi" })
        assertEquals("permanente não cobra manutenção", pfAntes, s.heroi.pfAtual)
    }

    // ── Lote MA-3d-3: Bloqueio mágico ──

    @Test
    fun `bloqueio magico paga o custo em PF e loga a defesa`() {
        val s = sessao(1L)
        val pfAntes = s.heroi.pfAtual
        s.aplicarBloqueioMagico(custoFP = 2, magiaNome = "Escudo Reflexivo")
        assertEquals("bloqueio cobra o custo cheio (não reduz por NH)", pfAntes - 2, s.heroi.pfAtual)
        assertTrue(s.log.any { it.contains("Escudo Reflexivo") && it.contains("bloqueio mágico") })
    }

    @Test
    fun `bloqueio magico INTERROMPE uma conjuracao em andamento`() {
        val s = sessao(1L)
        val ctx = ContextoConjuracao(nhBasico = 20, classe = MagicClassParser.parse("Comum"), mana = NivelMana.NORMAL)
        s.heroiConjurar(ctx, MagicEnergy.parse("2"), 1, "Voar", null, tempoOperacaoSeg = 4)
        assertTrue(s.conjuracaoEmAndamento != null)
        s.aplicarBloqueioMagico(1, "Parede de Força")
        assertTrue("o bloqueio quebra a concentração (Magia p.12)", s.conjuracaoEmAndamento == null)
    }

    // ── Lote MA-3d-2: Toque ──

    @Test
    fun `conjurar magia de Toque CARREGA a mao (nao aplica efeito na hora)`() {
        val s = sessao(1L)
        val ctx = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Toque"), mana = NivelMana.NORMAL)
        val r = s.heroiConjurar(ctx, MagicEnergy.parse("2"), 1, "Golpe Mortal", alvoId = null)
        if (r.sucesso) {
            assertTrue("mão carregada", s.toqueCarregado != null)
            assertTrue(s.log.any { it.contains("CARREGADA") })
        }
    }

    @Test
    fun `entregar toque num alvo adjacente descarrega a mao (acerto ou defesa)`() {
        // NH/DX altos → acerta e descarrega em algum seed; noutro o goblin defende e mantém carregada.
        var descarregou = false; var manteve = false
        for (seed in 0L until 30L) {
            val s = sessao(seed)
            val ctxCast = ContextoConjuracao(nhBasico = 30, classe = MagicClassParser.parse("Toque"), mana = NivelMana.NORMAL)
            s.heroiConjurar(ctxCast, MagicEnergy.parse("2"), 1, "Toque Gélido", alvoId = null)
            if (s.toqueCarregado == null) continue
            s.heroiEntregarToque("goblin")
            if (s.toqueCarregado == null) descarregou = true else manteve = true
            if (descarregou && manteve) break
        }
        assertTrue("nenhum seed descarregou o toque", descarregou)
        assertTrue("nenhum seed manteve carregado após defesa/erro", manteve)
    }

    @Test
    fun `dissipar toque limpa a mao sem efeito`() {
        val s = sessao(1L)
        val ctx = ContextoConjuracao(nhBasico = 30, classe = MagicClassParser.parse("Toque"), mana = NivelMana.NORMAL)
        s.heroiConjurar(ctx, MagicEnergy.parse("2"), 1, "Toque Gélido", alvoId = null)
        if (s.toqueCarregado != null) {
            s.dissiparToque()
            assertTrue(s.toqueCarregado == null)
            assertTrue(s.log.any { it.contains("dissipa") })
        }
    }

    // ── Lote MA-3c: conjuração multi-turno ──

    private fun ctxComum(nh: Int) = ContextoConjuracao(
        nhBasico = nh, classe = MagicClassParser.parse("Comum"), mana = NivelMana.NORMAL
    )

    @Test
    fun `magia de varios segundos entra em concentracao e so resolve no ultimo turno`() {
        val s = sessao(1L)
        val r0 = s.heroiConjurar(ctxComum(20), MagicEnergy.parse("2"), 1, "Grande Cura", null, tempoOperacaoSeg = 3)
        assertTrue("deveria estar em andamento", r0.emAndamento)
        assertTrue(s.conjuracaoEmAndamento != null)
        // turno 2 e 3 de concentração; resolve no 3º.
        val r1 = s.continuarConjuracao(); assertTrue(r1!!.emAndamento) // ainda 1 restante
        assertTrue(s.conjuracaoEmAndamento != null)
        val r2 = s.continuarConjuracao(); assertFalse(r2!!.emAndamento) // resolveu
        assertTrue("terminou a concentração", s.conjuracaoEmAndamento == null)
        assertTrue(s.log.any { it.contains("Grande Cura") })
    }

    @Test
    fun `atordoar durante a concentracao PERDE a magia automaticamente`() {
        val s = sessao(2L)
        s.heroiConjurar(ctxComum(20), MagicEnergy.parse("2"), 1, "Voar", null, tempoOperacaoSeg = 4)
        assertTrue(s.conjuracaoEmAndamento != null)
        s.interromperConjuracaoSeConjurando(atordoado = true, rolagemVontade = 3) // atordoado ignora a rolagem
        assertTrue("atordoado perde a conjuração", s.conjuracaoEmAndamento == null)
        assertTrue(s.log.any { it.contains("PERDE a conjuração") })
    }

    @Test
    fun `distracao exige Vontade-3 — falha perde, sucesso mantem`() {
        // Vontade 10 → alvo 7. Rolar 18 (>7) perde; rolar 3 (<=7) mantém.
        val sPerde = sessao(2L)
        sPerde.heroiConjurar(ctxComum(20), MagicEnergy.parse("2"), 1, "Voar", null, tempoOperacaoSeg = 4)
        sPerde.interromperConjuracaoSeConjurando(atordoado = false, rolagemVontade = 18)
        assertTrue("Vontade−3 falhou → perde", sPerde.conjuracaoEmAndamento == null)

        val sMantem = sessao(2L)
        sMantem.heroiConjurar(ctxComum(20), MagicEnergy.parse("2"), 1, "Voar", null, tempoOperacaoSeg = 4)
        sMantem.interromperConjuracaoSeConjurando(atordoado = false, rolagemVontade = 3)
        assertTrue("Vontade−3 passou → mantém", sMantem.conjuracaoEmAndamento != null)
    }

    @Test
    fun `abortar limpa a conjuracao sem custo`() {
        val s = sessao(1L)
        val pfAntes = s.heroi.pfAtual
        s.heroiConjurar(ctxComum(20), MagicEnergy.parse("2"), 1, "Voar", null, tempoOperacaoSeg = 5)
        s.abortarConjuracao()
        assertTrue(s.conjuracaoEmAndamento == null)
        assertEquals("abortar não gasta PF", pfAntes, s.heroi.pfAtual)
    }

    @Test
    fun `magia de 1 segundo resolve na hora (nao entra em concentracao)`() {
        val s = sessao(1L)
        val r = s.heroiConjurar(ctxComum(20), MagicEnergy.parse("2"), 1, "Criar Fogo", null, tempoOperacaoSeg = 1)
        assertFalse(r.emAndamento)
        assertTrue(s.conjuracaoEmAndamento == null)
    }

    @Test
    fun `queimar PV paga parte do custo com PV (fere o mago) e penaliza o NH`() {
        // Custo fixo 3; queima 2 PV → paga 2 em PV (perde PV) e 1 em PF; NH cai 2 (−1 por PV).
        val s = sessao(4L)
        val pvAntes = s.heroi.pvAtual
        val pfAntes = s.heroi.pfAtual
        val ctx = ContextoConjuracao(nhBasico = 15, classe = MagicClassParser.parse("Comum"),
            mana = NivelMana.NORMAL, pvQueimados = 2)
        s.heroiConjurar(ctx, MagicEnergy.parse("3"), energiaInvestida = 1, magiaNome = "Grande Cura", alvoId = null)
        // Só cobra se NÃO foi decisivo (decisivo perdoa o custo). Em qualquer caso, PV nunca sobe.
        assertTrue("queimar PV nunca cura o mago", s.heroi.pvAtual <= pvAntes)
        // A penalidade de −2 no NH aparece nas parcelas do NH efetivo.
        val nh = MagicCasting.nhEfetivo(ctx)
        assertTrue(nh.componentes.any { it.motivo.contains("queimar") && it.valor == -2 })
        assertTrue(s.heroi.pfAtual <= pfAntes)
    }

    // ── Lote MEC-2: o buff sai do JSON e vira regra viva no combate ──────────────────────────────

    private fun registrar(s: CombatSession, nome: String, mec: MagiaMecanica, energia: Int = 1,
                          alvo: String = "heroi", durSeg: Int = 60) =
        s.registrarMagiaAtiva(nome, "heroi", alvo, durSeg, 0, TipoDuracao.DURADOURA, false,
            MagicMechanics.calcularBuff(mec, energia, alvo))

    @Test
    fun `buff de RD sobe o perfil do heroi e a dissipacao devolve ao normal`() {
        val s = sessao(7)
        val rdBase = s.heroiPerfil.rd
        registrar(s, "Escudo", MagiaMecanica(efeito = "buff", buffRotulo = "Escudo", buffRd = 4))
        assertEquals(rdBase + 4, s.heroiPerfil.rd)
        assertTrue(s.dissiparMagiaAtiva("Escudo"))
        assertEquals("dissipar tem que reverter a RD", rdBase, s.heroiPerfil.rd)
    }

    @Test
    fun `buff EXPIRA no tick e o bonus some sozinho`() {
        val s = sessao(7)
        val rdBase = s.heroiPerfil.rd
        registrar(s, "Escudo", MagiaMecanica(efeito = "buff", buffRd = 4), durSeg = 1)
        assertEquals(rdBase + 4, s.heroiPerfil.rd)
        repeat(8) { s.avancarTurno() } // o tick roda ao fim do turno do herói
        assertEquals("expirar tem que reverter — senão o bônus fica para sempre", rdBase, s.heroiPerfil.rd)
        assertTrue(s.magiasAtivas.none { it.magiaId == "Escudo" })
    }

    @Test
    fun `Forca sobe o ST do heroi conforme a energia investida`() {
        val s = sessao(7)
        val stBase = s.heroiPerfil.st
        val forca = MagiaMecanica(efeito = "buff", buffAtributo = "ST", buffAtributoValor = 1,
            buffEnergiaPorNivel = 2, buffMaxNiveis = 5)
        registrar(s, "Força", forca, energia = 6)
        assertEquals(stBase + 3, s.heroiPerfil.st)
    }

    @Test
    fun `Voo troca o Deslocamento do heroi e devolve ao dissipar`() {
        val s = sessao(7)
        val base = s.heroi.deslocamentoEfetivo
        registrar(s, "Voo", MagiaMecanica(efeito = "buff", buffRotulo = "Voo", buffDeslocamentoFixo = 10))
        assertEquals(10, s.heroi.deslocamentoEfetivo)
        s.dissiparMagiaAtiva("Voo")
        assertEquals(base, s.heroi.deslocamentoEfetivo)
    }

    @Test
    fun `buff so narrado nao entra na lista do combatente (nada a reverter depois)`() {
        val s = sessao(7)
        registrar(s, "Corpo de Água", MagiaMecanica(efeito = "buff", buffRotulo = "Corpo de Água"))
        assertTrue("efeito sem número não vira delta", s.heroi.buffs.isEmpty())
        assertTrue("mas continua rastreada como ativa", s.magiasAtivas.any { it.magiaId == "Corpo de Água" })
    }

    @Test
    fun `Debilitar no NPC derruba o ST EFETIVO dele — nao fica so gravado`() {
        val s = sessao(7)
        val goblin = s.encounter.combatentes.first { it.id == "goblin" }
        val stBase = goblin.stEfetivo
        val deb = MagiaMecanica(efeito = "buff", buffAtributo = "ST", buffAtributoValor = -1,
            buffEnergiaPorNivel = 1, buffMaxNiveis = 5)
        registrar(s, "Debilitar", deb, energia = 3, alvo = "goblin")
        assertEquals(stBase - 3, goblin.stEfetivo)
    }

    @Test
    fun `RD de buff no NPC protege contra a magia de dano`() {
        val s = sessao(7)
        val goblin = s.encounter.combatentes.first { it.id == "goblin" }
        registrar(s, "Proteger Animal", MagiaMecanica(efeito = "buff", buffRd = 5), alvo = "goblin")
        assertEquals(5, goblin.buffRd)
    }

    @Test
    fun `Escudo soma BD em TODAS as defesas ativas e some ao dissipar`() {
        val s = sessao(7)
        val esqBase = s.heroiPerfil.esquiva
        val aparaBase = s.heroiPerfil.apara!!
        val escudo = MagiaMecanica(efeito = "buff", buffRotulo = "Escudo", buffBd = 1,
            buffEnergiaPorNivel = 2, buffMaxNiveis = 4)
        registrar(s, "Escudo", escudo, energia = 6) // 6/2 = 3 níveis → BD +3
        assertEquals(esqBase + 3, s.heroiPerfil.esquiva)
        assertEquals(aparaBase + 3, s.heroiPerfil.apara)
        s.dissiparMagiaAtiva("Escudo")
        assertEquals(esqBase, s.heroiPerfil.esquiva)
        assertEquals(aparaBase, s.heroiPerfil.apara)
    }

    @Test
    fun `BD magico respeita o teto de 4 e nao inventa defesa que o heroi nao tem`() {
        val enc = CombatEncounter(listOf(heroi(), goblin()), mapOf("goblin" to 5), seed = 1L)
        // Herói SEM aparar e SEM bloquear (não empunha arma nem escudo).
        val s = CombatSession(enc, HeroiPerfilCombate(esquiva = 9, apara = null, bloqueio = null, ht = 12), Random(7))
        val escudo = MagiaMecanica(efeito = "buff", buffBd = 1, buffEnergiaPorNivel = 2, buffMaxNiveis = 4)
        registrar(s, "Escudo", escudo, energia = 40) // teto: +4, não +20
        assertEquals(13, s.heroiPerfil.esquiva)
        assertNull("BD não cria um Aparar do nada", s.heroiPerfil.apara)
        assertNull("BD não cria um Bloquear do nada", s.heroiPerfil.bloqueio)
    }

    // ── Lote MEC-6: buff de UM ÚNICO USO (Aumentar Força/Destreza/Vitalidade) ───────────────────

    private fun umUso(atributo: String) = MagiaMecanica(
        efeito = "buff", buffAtributo = atributo, buffAtributoValor = 1,
        buffEnergiaPorNivel = 1, buffMaxNiveis = 5, buffUmUnicoUso = true,
    )

    @Test
    fun `Aumentar Forca aplica o ST na hora — antes o heroi pagava o PF e nada acontecia`() {
        val s = sessao(7)
        val stBase = s.heroiPerfil.st
        s.aplicarBuffDeUmUso("Aumentar Força", MagicMechanics.calcularBuff(umUso("ST"), 3, "heroi"))
        assertEquals(stBase + 3, s.heroiPerfil.st)
    }

    @Test
    fun `o buff de um uso SOBREVIVE ao turno em que foi conjurado`() {
        // A armadilha: conjurar gasta a ação. Se o buff sumisse no fim desse turno, o herói nunca
        // conseguiria usá-lo — pagaria o PF por nada (que era exatamente o bug).
        val s = sessao(7)
        val stBase = s.heroiPerfil.st
        s.aplicarBuffDeUmUso("Aumentar Força", MagicMechanics.calcularBuff(umUso("ST"), 3, "heroi"))
        // Fecha a rodada inteira: o herói volta a agir com o bônus ainda de pé.
        repeat(s.encounter.combatentes.size) { s.avancarTurno() }
        assertEquals("o bônus tem que valer na ação SEGUINTE", stBase + 3, s.heroiPerfil.st)
    }

    @Test
    fun `o buff de um uso some depois da acao seguinte`() {
        val s = sessao(7)
        val stBase = s.heroiPerfil.st
        s.aplicarBuffDeUmUso("Aumentar Força", MagicMechanics.calcularBuff(umUso("ST"), 3, "heroi"))
        // Duas rodadas: a 1ª é a da conjuração, a 2ª é a ação que consome o bônus.
        repeat(s.encounter.combatentes.size * 2 + 1) { s.avancarTurno() }
        assertEquals("um único uso não pode virar buff permanente", stBase, s.heroiPerfil.st)
        assertTrue(s.heroi.buffs.none { it.umUnicoUso })
    }

    @Test
    fun `buff de um uso NAO vira magia ativa (nao tem manutencao nem relogio)`() {
        val s = sessao(7)
        s.aplicarBuffDeUmUso("Aumentar Destreza", MagicMechanics.calcularBuff(umUso("DX"), 2, "heroi"))
        assertTrue("é instantânea: não entra no rastreio de manutenção", s.magiasAtivas.isEmpty())
        assertEquals(2, s.heroi.buffDx)
    }

    @Test
    fun `Aumentar Vitalidade sobe o HT do heroi`() {
        val s = sessao(7)
        val htBase = s.heroiPerfil.ht
        s.aplicarBuffDeUmUso("Aumentar Vitalidade", MagicMechanics.calcularBuff(umUso("HT"), 4, "heroi"))
        assertEquals(htBase + 4, s.heroiPerfil.ht)
    }

    @Test
    fun `Nublar no heroi penaliza quem tenta acerta-lo`() {
        val s = sessao(7)
        val nublar = MagiaMecanica(efeito = "buff", buffPenalidadeAtacantes = 1,
            buffEnergiaPorNivel = 1, buffMaxNiveis = 5)
        registrar(s, "Nublar", nublar, energia = 4)
        assertEquals(4, s.heroi.buffPenalidadeAtacantes)
    }
}
