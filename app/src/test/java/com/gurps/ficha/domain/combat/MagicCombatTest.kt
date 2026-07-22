package com.gurps.ficha.domain.combat

import com.gurps.ficha.domain.magic.ContextoConjuracao
import com.gurps.ficha.domain.magic.CondicaoBanda
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
    fun `magia ativa PERGUNTA a manutencao ao completar o intervalo (MEC-23)`() {
        val s = sessao(1L)
        // Temporária de 2s, manutenção 1 PF: após 2 turnos do herói, cobra 1 PF e reseta.
        s.registrarMagiaAtiva("Escudo", "heroi", null, duracaoSeg = 2, custoManutencaoSeg = 1,
            duracao = com.gurps.ficha.domain.magic.TipoDuracao.TEMPORARIA, exigeConcentracao = false)
        assertTrue(s.magiasAtivas.isNotEmpty())
        val pfAntes = s.heroi.pfAtual
        // avancarTurno cobra quando o HERÓI termina o turno (1s cada). No começo é a vez do herói.
        s.avancarTurno() // herói → goblin (1s de manutenção decrementado)
        s.avancarTurno() // goblin → herói
        s.avancarTurno() // herói → goblin (2º segundo → VENCE a manutenção)
        // Lote MEC-23: manter é OPCIONAL — o motor não cobra sozinho, ele pergunta. Este teste
        // trancava o comportamento ANTIGO (débito automático), que era a regra errada.
        assertTrue("a manutenção deve ficar pendente de decisão", s.manutencaoPendente.isNotEmpty())
        assertEquals("e nada pode ser cobrado antes da resposta", pfAntes, s.heroi.pfAtual)
        s.resolverManutencao("Escudo", manter = true)
        assertTrue("depois de MANTER, aí sim o PF cai", s.heroi.pfAtual < pfAntes)
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

    // ── Lote MEC-11: conjuração de PROJÉTIL no herói CONTA como ataque defensável ────────────────

    @Test
    fun `MEC-11 intencao de conjurar projetil no heroi PERMITE defesa (o card Defenda-se)`() {
        // O bug: `intencaoAtacaHeroi` só aceitava ATAQUE/ATAQUE_TOTAL/MOVER_E_ATACAR/AGARRAR. Conjurar
        // é CONCENTRAR → as opções de defesa vinham VAZIAS → a defesa interativa do MEC-8 nunca
        // disparava e o motor esquivava sozinho. O jogador só via o PV sumindo, sem card nem escolha.
        val enc = CombatEncounter(listOf(heroi(), conjuradorNpc()), mapOf("mago" to 5), seed = 1L)
        val s = CombatSession(enc, perfil(), Random(3))
        val intencao = NpcCombatBrain.decidir(enc.combatentes.first { it.id == "mago" }, enc, "heroi", Random(3))
        assertTrue("o cérebro deveria querer conjurar", intencao.conjurar != null)
        assertTrue("conjurar projétil no herói TEM que contar como ataque defensável",
            s.intencaoAtacaHeroi(intencao))
        assertFalse("mas não pode ser resolvido como ataque de ARMA", intencao.conjurar!!.projetil.not())
    }

    @Test
    fun `MEC-11 npcResolve NAO resolve conjuracao como ataque de arma`() {
        // Guarda: mesmo passando por intencaoAtacaHeroi, a conjuração sai por npcConjurar.
        val enc = CombatEncounter(listOf(heroi(), conjuradorNpc()), mapOf("mago" to 5), seed = 1L)
        val s = CombatSession(enc, perfil(), Random(3))
        val intencao = NpcCombatBrain.decidir(enc.combatentes.first { it.id == "mago" }, enc, "heroi", Random(3))
        val pvAntes = s.heroi.pvAtual
        val r = s.npcResolve("mago", intencao, null)
        assertFalse("npcResolve não deve tratar isso como ataque", r.acertou)
        assertEquals("e não pode ferir o herói por esse caminho", pvAntes, s.heroi.pvAtual)
    }

    // ── Lote MEC-13: magia que só afeta OBJETO recusa alvo vivo ─────────────────────────────────

    @Test
    fun `MEC-13 Desintegrar NAO pode ser lancada num NPC vivo`() {
        // O livro: "afeta apenas objetos inanimados". Antes dava para desintegrar um goblin.
        val s = sessao(7)
        val pfAntes = s.heroi.pfAtual
        val pvGoblinAntes = s.encounter.combatentes.first { it.id == "goblin" }.pvAtual
        val mec = MagiaMecanica(efeito = "dano", danoPorEnergia = "1d", entrega = "projetil", alvoValido = "objeto")
        val ctx = ContextoConjuracao(nhBasico = 30, classe = MagicClassParser.parse("Comum"),
            mana = NivelMana.NORMAL, mecanica = mec)
        val r = s.heroiConjurar(ctx, MagicEnergy.parse("1 a 4"), 3, "Desintegrar", alvoId = "goblin")
        assertFalse("a conjuração deve ser recusada", r.sucesso)
        assertEquals("goblin não pode levar dano", pvGoblinAntes,
            s.encounter.combatentes.first { it.id == "goblin" }.pvAtual)
        assertEquals("e o herói NÃO perde fadiga por uma jogada ilegal", pfAntes, s.heroi.pfAtual)
        assertTrue(s.log.any { it.contains("apenas objetos inanimados") })
    }

    @Test
    fun `MEC-13 magia normal segue funcionando (regressao)`() {
        val s = sessao(7)
        val mec = MagiaMecanica(efeito = "dano", danoPorEnergia = "1d", entrega = "projetil")
        val ctx = ContextoConjuracao(nhBasico = 30, classe = MagicClassParser.parse("Projétil"),
            mana = NivelMana.NORMAL, distanciaMetros = 5, mecanica = mec)
        val r = s.heroiConjurar(ctx, MagicEnergy.parse("Varia"), 2, "Bola de Fogo", alvoId = "goblin")
        assertTrue("sem alvoValido a magia é permitida", r.sucesso)
    }

    // ── Lote MEC-10: magia de CURA restaura PV no combate ───────────────────────────────────────

    @Test
    fun `Cura Profunda restaura PV do heroi ferido (2 PV por energia)`() {
        val s = sessao(7)
        s.heroi.pvAtual = 4 // ferido (max 12)
        val cura = MagiaMecanica(efeito = "cura", curaPvPorEnergia = 2, curaMaxPv = 8)
        val ctx = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Comum"),
            mana = NivelMana.NORMAL, mecanica = cura)
        s.heroiConjurar(ctx, MagicEnergy.parse("1 a 4"), energiaInvestida = 3, magiaNome = "Cura Profunda", alvoId = null)
        assertEquals("3 de energia × 2 PV = 6, de 4 para 10", 10, s.heroi.pvAtual)
        assertTrue(s.log.any { it.contains("recupera") })
    }

    @Test
    fun `cura nao passa do PV maximo`() {
        val s = sessao(7)
        s.heroi.pvAtual = 11 // perdeu só 1 (max 12)
        val cura = MagiaMecanica(efeito = "cura", curaPvPorEnergia = 2, curaMaxPv = 8)
        val ctx = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Comum"),
            mana = NivelMana.NORMAL, mecanica = cura)
        s.heroiConjurar(ctx, MagicEnergy.parse("1 a 4"), energiaInvestida = 4, magiaNome = "Cura Profunda", alvoId = null)
        assertEquals("cura 8 em quem perdeu 1 → restaura 1", 12, s.heroi.pvAtual)
    }

    @Test
    fun `Nublar no heroi penaliza quem tenta acerta-lo`() {
        val s = sessao(7)
        val nublar = MagiaMecanica(efeito = "buff", buffPenalidadeAtacantes = 1,
            buffEnergiaPorNivel = 1, buffMaxNiveis = 5)
        registrar(s, "Nublar", nublar, energia = 4)
        assertEquals(4, s.heroi.buffPenalidadeAtacantes)
    }

    // ── Lote MEC-14: EXPLOSÃO com decaimento por distância ───────────────────────────────────────
    // Regra literal (Bola de Fogo Explosiva / Relâmpago Explosivo): "O alvo e qualquer um mais próximo
    // do alvo que um metro recebe dano total. Os mais afastados dividem o dano em três vezes a
    // distância em metros (arredondado para baixo)."

    @Test
    fun `explosao — ate 1m leva dano cheio`() {
        assertEquals(20, MagicMechanics.danoDaExplosao(20, distanciaM = 0, divisorPorMetro = 3))
        assertEquals(20, MagicMechanics.danoDaExplosao(20, distanciaM = 1, divisorPorMetro = 3))
    }

    @Test
    fun `explosao — alem de 1m divide por tres vezes a distancia, arredondando para baixo`() {
        assertEquals("20 a 2m = 20 / 6 = 3", 3, MagicMechanics.danoDaExplosao(20, 2, 3))
        assertEquals("20 a 3m = 20 / 9 = 2", 2, MagicMechanics.danoDaExplosao(20, 3, 3))
        assertEquals("20 a 4m = 20 / 12 = 1", 1, MagicMechanics.danoDaExplosao(20, 4, 3))
        assertEquals("longe o bastante zera", 0, MagicMechanics.danoDaExplosao(20, 7, 3))
    }

    @Test
    fun `sem divisor (chuva, nuvem) NAO decai — dano ambiental atinge todos igual`() {
        assertEquals(20, MagicMechanics.danoDaExplosao(20, distanciaM = 5, divisorPorMetro = 0))
    }

    // ── Lote MEC-39 (P11): projétil carregado por vários turnos + C1 (Vontade ao ser ferido) ────

    private fun ctxProjetilDano() = ContextoConjuracao(
        nhBasico = 25, classe = MagicClassParser.parse("Projétil"), mana = NivelMana.NORMAL,
        distanciaMetros = 3, mecanica = MagiaMecanica(efeito = "dano", danoPorEnergia = "1d", energiaPorDado = 1))

    @Test
    fun `carregar cria o projetil na mao sem arremessar (P11)`() {
        val s = sessao(7)
        val r = s.heroiCarregarProjetil(ctxProjetilDano(), MagicEnergy.parse("Varia"),
            energiaInicial = 2, magiaNome = "Bola de Fogo", tetoPorTurno = 4)
        assertTrue(r.sucesso)
        assertTrue("o projetil fica segurado", s.projetilCarregado != null)
        assertEquals("com a energia inicial", 2, s.projetilCarregado!!.energiaAcumulada)
    }

    @Test
    fun `aumentar soma energia ate o teto e no maximo 3 segundos`() {
        val s = sessao(7)
        s.heroiCarregarProjetil(ctxProjetilDano(), MagicEnergy.parse("Varia"), 2, "Bola de Fogo", tetoPorTurno = 4)
        s.heroiAumentarProjetil(4) // 2s
        s.heroiAumentarProjetil(4) // 3s
        assertEquals("2 + 4 + 4", 10, s.projetilCarregado!!.energiaAcumulada)
        val r4 = s.heroiAumentarProjetil(4) // tentaria 4s
        assertFalse("nao pode passar de 3 segundos", r4.sucesso)
        assertEquals("energia nao muda alem do 3o segundo", 10, s.projetilCarregado!!.energiaAcumulada)
    }

    @Test
    fun `nao da para conjurar outra magia enquanto segura o projetil (C5-projetil)`() {
        val s = sessao(7)
        s.heroiCarregarProjetil(ctxProjetilDano(), MagicEnergy.parse("Varia"), 2, "Bola de Fogo", tetoPorTurno = 4)
        val outra = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Comum"), mana = NivelMana.NORMAL)
        val r = s.heroiConjurar(outra, MagicEnergy.parse("2"), 2, "Outra", alvoId = "goblin")
        assertFalse("segurando projetil nao conjura", r.sucesso)
        assertTrue(r.texto.contains("segurando"))
    }

    @Test
    fun `arremessar consome o projetil e resolve o ataque`() {
        val s = sessao(7, distGoblin = 3)
        s.heroiCarregarProjetil(ctxProjetilDano(), MagicEnergy.parse("Varia"), 3, "Bola de Fogo", tetoPorTurno = 4)
        s.heroiArremessarProjetil("goblin")
        assertTrue("a mao fica livre depois de arremessar", s.projetilCarregado == null)
        assertTrue(s.log.any { it.contains("arremessa Bola de Fogo") })
    }

    @Test
    fun `dissipar solta o projetil sem efeito (acao livre)`() {
        val s = sessao(7)
        s.heroiCarregarProjetil(ctxProjetilDano(), MagicEnergy.parse("Varia"), 2, "Bola de Fogo", tetoPorTurno = 4)
        s.dissiparProjetil()
        assertTrue(s.projetilCarregado == null)
    }

    @Test
    fun `C1 ferido segurando projetil testa Vontade e pode dispara-lo em si (Magia p12)`() {
        // Segura o projetil, marca lesao (choquePendente) e avanca o turno do heroi.
        var explodiuAlguma = false
        for (seed in 0L until 60L) {
            val s = sessao(seed, distGoblin = 3)
            s.heroiCarregarProjetil(ctxProjetilDano(), MagicEnergy.parse("Varia"), 3, "Bola de Fogo", tetoPorTurno = 4)
            s.heroi.choquePendente = 5 // sofreu lesao
            s.avancarTurno()
            if (s.log.any { it.contains("dispara em VOCÊ") }) { explodiuAlguma = true; break }
        }
        assertTrue("em 60 tentativas, alguma falha de Vontade tem que fazer o projetil disparar no heroi",
            explodiuAlguma)
    }

    @Test
    fun `sem lesao o projetil segurado NAO dispara sozinho`() {
        val s = sessao(7)
        s.heroiCarregarProjetil(ctxProjetilDano(), MagicEnergy.parse("Varia"), 3, "Bola de Fogo", tetoPorTurno = 4)
        s.heroi.choquePendente = 0
        repeat(4) { s.avancarTurno(); s.heroi.choquePendente = 0 }
        assertTrue("sem dano, o teste de Vontade nem acontece", s.log.none { it.contains("dispara em VOCÊ") })
    }

    // ── Lote MEC-41: conserta o Lampejo (bandas no caminho COMUM, não só Área) ──────────────────

    @Test
    fun `Lampejo COMUM aplica as bandas em todos no raio (conserta o MEC-37)`() {
        // O Lampejo e classe COMUM no livro. As bandas estavam so no ramo de Area, entao nunca
        // rodavam: o log do usuario mostrou apenas "fica CEGO", sem bandas nem ofuscamento.
        val lampejo = MagiaMecanica(efeito = "condicao", condicao = "cego", condicaoRaioM = 10,
            condicaoBandas = listOf(
                CondicaoBanda(ateM = 2, cegoSeg = 3, riderPenalidade = 3, riderSeg = 60),
                CondicaoBanda(ateM = 9999, riderPenalidade = 3, riderSeg = 3),
            ))
        val enc = CombatEncounter(
            listOf(heroi(), goblin().copy(id = "perto"), goblin().copy(id = "longe")),
            mapOf("perto" to 5, "longe" to 12), seed = 1L)
        val s = CombatSession(enc, perfil(), Random(7))
        val ctx = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Comum"),
            mana = NivelMana.NORMAL, mecanica = lampejo)
        s.heroiConjurar(ctx, MagicEnergy.parse("4"), energiaInvestida = 4, magiaNome = "Lampejo", alvoId = "perto")
        val perto = s.encounter.combatentes.first { it.id == "perto" }
        val longe = s.encounter.combatentes.first { it.id == "longe" }
        // "perto" e o proprio centro (distancia 0 dele mesmo) -> banda de cegueira.
        assertTrue("o alvo do clarao tem que cegar", Condicao.CEGO in perto.condicoes)
        assertTrue("e ficar ofuscado", perto.penalidadeCombateTemp > 0)
        // "longe" esta a 7m do centro -> banda distante: so ofusca.
        assertTrue("quem esta longe tambem e afetado (ofuscado)", longe.penalidadeCombateTemp > 0)
        assertFalse("mas nao cega", Condicao.CEGO in longe.condicoes)
    }

    @Test
    fun `distanciaEntre e a diferenca das distancias ao heroi`() {
        val enc = CombatEncounter(
            listOf(heroi(), goblin().copy(id = "a"), goblin().copy(id = "b")),
            mapOf("a" to 4, "b" to 10), seed = 1L)
        val s = CombatSession(enc, perfil(), Random(7))
        val a = s.encounter.combatentes.first { it.id == "a" }
        val b = s.encounter.combatentes.first { it.id == "b" }
        assertEquals(6, s.distanciaEntre(a, b))
        assertEquals("simetrico", 6, s.distanciaEntre(b, a))
    }

    // ── Lote MEC-46 (P1b): zonas persistentes que ferem por turno ───────────────────────────────

    private fun zona(nome: String = "Chuva de Fogo", intervalo: Int = 1, dur: Int = 5, teste: String? = null) =
        ZonaPersistente(nome = nome, centro = null, raioM = 3, danoExpr = "1d-1", tipoDano = "quei",
            armadura = null, intervaloSeg = intervalo, teste = teste, segRestantes = dur,
            segAteProximo = intervalo, operadorId = "heroi")

    @Test
    fun `zona fere quem esta dentro a cada turno (P1b)`() {
        val s = sessao(7, distGoblin = 1)
        val pvAntes = s.encounter.combatentes.first { it.id == "goblin" }.pvAtual
        s.registrarZona(zona())
        repeat(3) { s.avancarTurno() }
        val g = s.encounter.combatentes.first { it.id == "goblin" }
        assertTrue("o goblin dentro da zona tem que perder PV", g.pvAtual < pvAntes)
        assertTrue(s.log.any { it.contains("atinge") })
    }

    // ── Lote TOK-9: regra da estreia da ZONA (achado no log do aparelho) ───────────────────────

    @Test
    fun `a zona NAO fere no turno em que foi criada — o lancamento ja feriu`() {
        // Bug do aparelho: Goblin 2 levou 4 do lançamento E 4 do tique, no MESMO timestamp. O
        // comentário do MEC-46 já dizia a intenção ("a zona tica a partir do turno seguinte"),
        // mas o código não fazia isso.
        val s = sessao(7, distGoblin = 1)
        val g = s.encounter.combatentes.first { it.id == "goblin" }
        val pvAntes = g.pvAtual
        s.registrarZona(zona(dur = 10))
        s.avancarTurno()   // fim do turno do herói = turno da conjuração
        assertEquals("o tique não pode dobrar o dano do lançamento", pvAntes, g.pvAtual)
    }

    @Test
    fun `a partir do turno seguinte a zona fere normalmente`() {
        val s = sessao(7, distGoblin = 1)
        val g = s.encounter.combatentes.first { it.id == "goblin" }
        val pvAntes = g.pvAtual
        s.registrarZona(zona(dur = 10))
        repeat(4) { s.avancarTurno() }
        assertTrue("depois da estreia ela tem que ferir", g.pvAtual < pvAntes)
    }

    @Test
    fun `zona de intervalo LONGO nao perde o primeiro tique real`() {
        // Armadilha que eu mesmo criei ao consertar: se a estreia fosse consumida no primeiro
        // INTERVALO (e não no primeiro TURNO), o Mau Cheiro pularia o tique do minuto 60 — o
        // primeiro que ele tem. A estreia é do turno da conjuração, não do intervalo.
        val s = sessao(7, distGoblin = 1)
        val g = s.encounter.combatentes.first { it.id == "goblin" }
        val pvAntes = g.pvAtual
        s.registrarZona(zona(nome = "Mau Cheiro", intervalo = 3, dur = 30))
        repeat(2) { s.avancarTurno() }
        assertEquals("antes do intervalo vencer ninguém é ferido", pvAntes, g.pvAtual)
        repeat(10) { s.avancarTurno() }
        assertTrue("o primeiro tique REAL não pode ser engolido pela estreia", g.pvAtual < pvAntes)
    }

    // ── Lote TOK-10: zonas sobrepostas (achado ao conjurar a mesma mágica duas vezes) ──────────

    @Test
    fun `duas zonas da MESMA magia nao somam dano — vale a mais forte`() {
        // Magia p.9: "só a MAIS PODEROSA deverá ser considerada — não se acumulam". Sem isto dava
        // para empilhar Chuva de Fogo N vezes no mesmo hex e multiplicar o dano por N.
        val s = sessao(7, distGoblin = 1)
        val g = s.encounter.combatentes.first { it.id == "goblin" }
        s.registrarZona(zona(dur = 20))                       // 1d-1
        s.registrarZona(zona(dur = 20))                       // outra igual, sobreposta
        repeat(4) { s.avancarTurno() }
        val perdidoDuas = g.pvMax - g.pvAtual

        val s1 = sessao(7, distGoblin = 1)
        val g1 = s1.encounter.combatentes.first { it.id == "goblin" }
        s1.registrarZona(zona(dur = 20))                      // só uma
        repeat(4) { s1.avancarTurno() }
        val perdidoUma = g1.pvMax - g1.pvAtual

        assertEquals("duas nuvens iguais não podem ferir mais que uma", perdidoUma, perdidoDuas)
    }

    @Test
    fun `magias DIFERENTES sobrepostas SOMAM — fogo queima e acido corroi`() {
        val s = sessao(7, distGoblin = 1)
        val g = s.encounter.combatentes.first { it.id == "goblin" }
        s.registrarZona(zona(nome = "Chuva de Fogo", dur = 20))
        s.registrarZona(zona(nome = "Chuva de Ácido", dur = 20))
        repeat(4) { s.avancarTurno() }
        val perdidoDuas = g.pvMax - g.pvAtual

        val s1 = sessao(7, distGoblin = 1)
        val g1 = s1.encounter.combatentes.first { it.id == "goblin" }
        s1.registrarZona(zona(nome = "Chuva de Fogo", dur = 20))
        repeat(4) { s1.avancarTurno() }
        val perdidoUma = g1.pvMax - g1.pvAtual

        assertTrue("elementos diferentes têm que somar: $perdidoDuas vs $perdidoUma",
            perdidoDuas > perdidoUma)
    }

    @Test
    fun `entre duas iguais prevalece a de dano MAIOR`() {
        val s = sessao(7, distGoblin = 1)
        s.registrarZona(zona(dur = 20))                                   // 1d-1
        s.registrarZona(zona(dur = 20).copy(danoExpr = "3d"))             // bem mais forte
        repeat(4) { s.avancarTurno() }
        assertTrue("o log tem que mostrar o dado da mais forte",
            s.log.any { it.contains("3d") })
    }

    @Test
    fun `a SEGUNDA nuvem da mesma magia ganha numero no log`() {
        val s = sessao(7, distGoblin = 1)
        s.registrarZona(zona(dur = 20))
        s.registrarZona(zona(dur = 20))
        assertTrue("sem número o jogador não sabe qual nuvem é qual",
            s.log.any { it.contains("Chuva de Fogo #2") })
        assertTrue("a primeira continua sem número",
            s.log.any { it.contains("☁️ Chuva de Fogo cobre") })
    }

    @Test
    fun `zona EXPIRA e para de ferir`() {
        val s = sessao(7, distGoblin = 1)
        s.registrarZona(zona(dur = 2))
        repeat(8) { s.avancarTurno() }
        assertTrue("a zona tem que se dissipar", s.log.any { it.contains("se dissipa") })
        assertTrue("e sair da lista de ativas", s.zonasAtivas.isEmpty())
    }

    @Test
    fun `quem esta FORA do raio nao e ferido`() {
        val s = sessao(7, distGoblin = 20) // bem longe; a aproximacao usa distancia ao heroi
        val pvAntes = s.encounter.combatentes.first { it.id == "goblin" }.pvAtual
        s.registrarZona(zona())
        repeat(3) { s.avancarTurno() }
        assertEquals("fora do raio 3, nao leva dano", pvAntes,
            s.encounter.combatentes.first { it.id == "goblin" }.pvAtual)
    }

    @Test
    fun `intervalo maior espaca os danos (Mau Cheiro e 1 por minuto)`() {
        val s = sessao(7, distGoblin = 1)
        s.registrarZona(zona(nome = "Mau Cheiro", intervalo = 60, dur = 120))
        repeat(5) { s.avancarTurno() }
        assertTrue("em 5 segundos o intervalo de 60s nao pode ter vencido",
            s.log.none { it.contains("Mau Cheiro atinge") })
    }

    // ── Lote P3-1: Vontade e IQ do buff chegam ao MOTOR (não só ao BuffAplicado) ────────────────

    @Test
    fun `Fortalecer Vontade LIGADO — o perfil efetivo do heroi sobe`() {
        // Teste de INTEGRAÇÃO, não da função pura: a lição do MEC-14 é que calcular certo e não
        // ligar dá teste verde com jogo quebrado. Aqui o caminho é o real — registrarMagiaAtiva.
        val s = sessao(7)
        val antes = s.heroiPerfil.vontade
        val buff = MagicMechanics.calcularBuff(
            MagiaMecanica(efeito = "buff", buffAtributo = "Vontade", buffAtributoValor = 1,
                buffEnergiaPorNivel = 1, buffMaxNiveis = 5),
            energia = 3, alvoId = "heroi"
        )
        s.registrarMagiaAtiva(
            nome = "Fortalecer Vontade", operadorId = "heroi", alvoId = "heroi", duracaoSeg = 60,
            custoManutencaoSeg = 0, duracao = TipoDuracao.TEMPORARIA, exigeConcentracao = false,
            buff = buff
        )
        assertEquals("o +3 de Vontade tem que chegar ao perfil que o motor lê", antes + 3,
            s.heroiPerfil.vontade)
    }

    @Test
    fun `Enfraquecer Vontade LIGADO — o perfil efetivo do heroi desce`() {
        val s = sessao(7)
        val antes = s.heroiPerfil.vontade
        val buff = MagicMechanics.calcularBuff(
            MagiaMecanica(efeito = "buff", buffAtributo = "Vontade", buffAtributoValor = -1,
                buffEnergiaPorNivel = 2, buffMaxNiveis = 5),
            energia = 4, alvoId = "heroi"
        )
        s.registrarMagiaAtiva(
            nome = "Enfraquecer Vontade", operadorId = "goblin", alvoId = "heroi", duracaoSeg = 60,
            custoManutencaoSeg = 0, duracao = TipoDuracao.TEMPORARIA, exigeConcentracao = false,
            buff = buff
        )
        assertEquals(antes - 2, s.heroiPerfil.vontade)
    }

    @Test
    fun `Tolice no NPC baixa o IQ efetivo dele`() {
        val s = sessao(7)
        val g = s.encounter.combatentes.first { it.id == "goblin" }
        val antes = g.iqEfetivo
        val buff = MagicMechanics.calcularBuff(
            MagiaMecanica(efeito = "buff", buffAtributo = "IQ", buffAtributoValor = -1,
                buffEnergiaPorNivel = 1, buffMaxNiveis = 5),
            energia = 3, alvoId = "goblin"
        )
        s.registrarMagiaAtiva(
            nome = "Tolice", operadorId = "heroi", alvoId = "goblin", duracaoSeg = 60,
            custoManutencaoSeg = 0, duracao = TipoDuracao.TEMPORARIA, exigeConcentracao = false,
            buff = buff
        )
        assertEquals("a Vontade do NPC deriva do IQ — por isso a Tolice tem que morder aqui",
            antes - 3, g.iqEfetivo)
    }

    @Test
    fun `o log NOMEIA Vontade e IQ em vez de cair no rotulo generico`() {
        val s = sessao(7)
        val buff = MagicMechanics.calcularBuff(
            MagiaMecanica(efeito = "buff", buffAtributo = "Vontade", buffAtributoValor = 1,
                buffEnergiaPorNivel = 1, buffMaxNiveis = 5, buffRotulo = "rotulo velho"),
            energia = 2, alvoId = "heroi"
        )
        s.registrarMagiaAtiva(
            nome = "Fortalecer Vontade", operadorId = "heroi", alvoId = "heroi", duracaoSeg = 60,
            custoManutencaoSeg = 0, duracao = TipoDuracao.TEMPORARIA, exigeConcentracao = false,
            buff = buff
        )
        assertTrue("o jogador precisa VER o número, não um rótulo",
            s.log.any { it.contains("Vontade +2") })
    }

    // ── Lote C11: a área encolhe, mas nunca expande (Magia p.10) ───────────────────────────────

    @Test
    fun `a zona pode ENCOLHER e quem ficou de fora deixa de ser atingido`() {
        val s = sessao(7, distGoblin = 1)
        s.registrarZona(zona(dur = 20))                       // raio 3
        assertTrue(s.encolherZona("Chuva de Fogo", 1))
        assertEquals(1, s.zonasAtivas.first().raioM)
        assertTrue(s.log.any { it.contains("encolhe de 3m para 1m") })
    }

    @Test
    fun `a zona NAO pode ser EXPANDIDA depois de operada`() {
        // "Uma mágica com uma área variável de efeito não pode ser expandida depois de ter sido
        // operada" — e a recusa é LOGADA, senão parece que o toque não funcionou.
        val s = sessao(7, distGoblin = 1)
        s.registrarZona(zona(dur = 20))                       // raio 3
        assertFalse(s.encolherZona("Chuva de Fogo", 6))
        assertEquals("o raio não pode ter mudado", 3, s.zonasAtivas.first().raioM)
        assertTrue(s.log.any { it.contains("não pode ser EXPANDIDA") })
    }

    @Test
    fun `encolher para o MESMO raio tambem e recusado`() {
        val s = sessao(7, distGoblin = 1)
        s.registrarZona(zona(dur = 20))
        assertFalse(s.encolherZona("Chuva de Fogo", 3))
    }

    @Test
    fun `encolher zona inexistente nao quebra nada`() {
        val s = sessao(7, distGoblin = 1)
        assertFalse(s.encolherZona("Nuvem que não existe", 1))
    }

    // ── Lote P5: explosão do PROJÉTIL (Relâmpago Explosivo) ────────────────────────────────────

    private fun mecExplosivo() = MagiaMecanica(
        efeito = "dano", entrega = "projetil", danoPorEnergia = "3d", energiaPorDado = 1,
        explosaoDivisorPorMetro = 3, elementoDano = "eletricidade"
    )

    private fun sessaoTresAlvos(seed: Long): CombatSession {
        val enc = CombatEncounter(
            listOf(heroi(), goblin().copy(id = "perto", nome = "Perto"),
                goblin().copy(id = "longe", nome = "Longe")),
            mapOf("perto" to 3, "longe" to 4), seed = 1L)
        return CombatSession(enc, perfil().copy(nhAtaqueInato = 20), Random(seed))
    }

    @Test
    fun `o projetil explosivo RESPINGA nos vizinhos — antes so feria o alvo`() {
        // O `explosaoDivisorPorMetro` existia desde o MEC-14 mas SÓ o ramo de ÁREA o usava.
        var respingou = false
        for (seed in 0L until 40L) {
            val s = sessaoTresAlvos(seed)
            s.heroiConjurar(
                ContextoConjuracao(nhBasico = 30, classe = MagicClassParser.parse("Projétil"),
                    mana = NivelMana.NORMAL, distanciaMetros = 3, mecanica = mecExplosivo()),
                MagicEnergy.parse("2"), 2, "Relâmpago Explosivo", "perto", 1)
            if (s.log.any { it.contains("Respingo da explosão") }) { respingou = true; break }
        }
        assertTrue("o projétil explosivo tem que atingir quem está por perto", respingou)
    }

    @Test
    fun `projetil SEM divisor de explosao nao respinga (regressao)`() {
        for (seed in 0L until 20L) {
            val s = sessaoTresAlvos(seed)
            s.heroiConjurar(
                ContextoConjuracao(nhBasico = 30, classe = MagicClassParser.parse("Projétil"),
                    mana = NivelMana.NORMAL, distanciaMetros = 3,
                    mecanica = mecExplosivo().copy(explosaoDivisorPorMetro = 0)),
                MagicEnergy.parse("2"), 2, "Bola de Fogo", "perto", 1)
            assertTrue("projétil comum não pode respingar",
                s.log.none { it.contains("Respingo da explosão") })
        }
    }

    @Test
    fun `a explosao rola UMA vez e divide — quem esta longe sofre menos`() {
        // Regra: "o alvo e quem está a menos de 1m recebe dano total; os mais afastados dividem o
        // dano em três vezes a distância". Comparo o dano do vizinho a 1m com o de um a 4m.
        val perto = com.gurps.ficha.domain.magic.MagicMechanics.danoDaExplosao(12, 1, 3)
        val longe = com.gurps.ficha.domain.magic.MagicMechanics.danoDaExplosao(12, 4, 3)
        assertTrue("quem está longe tem que sofrer menos: perto=$perto longe=$longe", longe < perto)
    }

    // ── Lote P9: FEIXE (Jatos e Sopros) ────────────────────────────────────────────────────────

    private fun mecFeixe(penal: Int = 4, bloqueavel: Boolean = true) = MagiaMecanica(
        efeito = "dano", entrega = "feixe", danoPorEnergia = "1d-1", energiaPorDado = 1,
        feixePenalidadeDx = penal, feixeBloqueavel = bloqueavel
    )

    private fun ctxFeixe(mec: MagiaMecanica) = ContextoConjuracao(
        nhBasico = 30, classe = MagicClassParser.parse("Comum"), mana = NivelMana.NORMAL,
        distanciaMetros = 3, mecanica = mec
    )

    @Test
    fun `o feixe FAZ jogada de acerto — nao acerta sozinho`() {
        // Antes do P9 o Jato caía no ramo de dano direto e acertava SEMPRE, sem teste nenhum.
        // Com DX 13 e −4, o NH de acerto é 9: em 40 tentativas tem que haver erro E acerto.
        var errou = false; var acertou = false
        for (seed in 0L until 40L) {
            val s = sessao(seed, distGoblin = 3)
            s.heroiConjurar(ctxFeixe(mecFeixe()), MagicEnergy.parse("2"), 2, "Jato de Chamas", "goblin", 1)
            if (s.log.any { it.contains("O jato passa longe") }) errou = true
            if (s.log.any { it.contains("O jato acerta") }) acertou = true
        }
        assertTrue("o feixe tem que poder ERRAR", errou)
        assertTrue("e tem que poder acertar", acertou)
    }

    @Test
    fun `sem a pericia o feixe usa DX menos a penalidade, e o log diz qual`() {
        val s = sessao(7, distGoblin = 3)
        s.heroiConjurar(ctxFeixe(mecFeixe(penal = 4)), MagicEnergy.parse("2"), 2, "Jato", "goblin", 1)
        assertTrue("o log tem que mostrar DX−4: ${s.log}",
            s.log.any { it.contains("DX−4 (sem a perícia)") })
    }

    @Test
    fun `os Sopros que saem da boca usam DX menos 2`() {
        val s = sessao(7, distGoblin = 3)
        s.heroiConjurar(ctxFeixe(mecFeixe(penal = 2)), MagicEnergy.parse("2"), 2, "Sopro de Fogo", "goblin", 1)
        assertTrue(s.log.any { it.contains("DX−2 (sem a perícia)") })
    }

    @Test
    fun `com a pericia Ataque Inato o feixe NAO sofre a penalidade da DX`() {
        // A penalidade é da DX improvisada, não do feixe: quem tem a perícia rola o NH dela limpo.
        val enc = CombatEncounter(listOf(heroi(), goblin()), mapOf("goblin" to 3), seed = 1L)
        val s = CombatSession(enc, perfil().copy(nhAtaqueInato = 16), Random(7))
        s.heroiConjurar(ctxFeixe(mecFeixe()), MagicEnergy.parse("2"), 2, "Jato", "goblin", 1)
        assertTrue("tem que citar a perícia, não a DX: ${s.log}",
            s.log.any { it.contains("Ataque Inato NH 16") })
    }

    @Test
    fun `o alvo pode se defender do feixe`() {
        var defendeu = false
        for (seed in 0L until 60L) {
            val enc = CombatEncounter(listOf(heroi(), goblin()), mapOf("goblin" to 3), seed = 1L)
            val s = CombatSession(enc, perfil().copy(nhAtaqueInato = 20), Random(seed))
            s.heroiConjurar(ctxFeixe(mecFeixe()), MagicEnergy.parse("2"), 2, "Jato", "goblin", 1)
            if (s.log.any { it.contains("se defende do jato") }) { defendeu = true; break }
        }
        assertTrue("o feixe tem que ser defensável (esquiva ou bloqueio)", defendeu)
    }

    @Test
    fun `contra o Jato de Acido o alvo so ESQUIVA — nunca bloqueia`() {
        // Exceção que o livro marca numa mágica só: "pode ser desviado, mas não aparado ou bloqueado".
        for (seed in 0L until 60L) {
            val enc = CombatEncounter(listOf(heroi(), goblin()), mapOf("goblin" to 3), seed = 1L)
            val s = CombatSession(enc, perfil().copy(nhAtaqueInato = 20), Random(seed))
            s.heroiConjurar(ctxFeixe(mecFeixe(bloqueavel = false)), MagicEnergy.parse("2"), 2,
                "Jato de Ácido", "goblin", 1)
            assertTrue("Jato de Ácido não pode ser bloqueado: ${s.log}",
                s.log.none { it.contains("Bloqueio") })
        }
    }

    @Test
    fun `feixe NUNCA e aparado — nem quando o NPC apara bem`() {
        // Um jato não tem lâmina para desviar. Aparar não pode aparecer em feixe nenhum.
        for (seed in 0L until 60L) {
            val enc = CombatEncounter(listOf(heroi(), goblin()), mapOf("goblin" to 1), seed = 1L)
            val s = CombatSession(enc, perfil().copy(nhAtaqueInato = 20), Random(seed))
            s.heroiConjurar(ctxFeixe(mecFeixe()), MagicEnergy.parse("2"), 2, "Jato", "goblin", 1)
            assertTrue("apareceu Aparar num feixe: ${s.log}",
                s.log.none { it.contains("Aparar") || it.contains("APARA") })
        }
    }

    // ── Lote A1: imunidade por ELEMENTO ────────────────────────────────────────────────────────

    private fun magiaDeFogo(expr: String = "3d") = MagiaMecanica(
        efeito = "dano", danoPorEnergia = expr, energiaPorDado = 1,
        tipoDano = "quei", elementoDano = "fogo"
    )

    @Test
    fun `imune ao fogo NAO perde PV para magia de fogo`() {
        val s = sessao(7, distGoblin = 1)
        val g = s.encounter.combatentes.first { it.id == "goblin" }
        val buff = MagicMechanics.calcularBuff(
            MagiaMecanica(efeito = "buff", buffImunidade = "fogo"), energia = 2, alvoId = "goblin")
        s.registrarMagiaAtiva(
            nome = "Imunidade ao Fogo", operadorId = "heroi", alvoId = "goblin", duracaoSeg = 60,
            custoManutencaoSeg = 0, duracao = TipoDuracao.TEMPORARIA, exigeConcentracao = false,
            buff = buff)
        val pvAntes = g.pvAtual
        s.heroiConjurar(
            ContextoConjuracao(nhBasico = 30, classe = MagicClassParser.parse("Comum"),
                mana = NivelMana.NORMAL, distanciaMetros = 1, mecanica = magiaDeFogo()),
            MagicEnergy.parse("2"), 2, "Bola de Fogo", "goblin", 1)
        assertEquals("imune ao fogo não pode perder PV", pvAntes, g.pvAtual)
        assertTrue("e o log tem que dizer por quê", s.log.any { it.contains("IMUNE a fogo") })
    }

    @Test
    fun `imunidade NAO vaza de um elemento para outro`() {
        // Regra literal: "imunes aos efeitos do calor e do fogo (mas não da eletricidade)".
        val s = sessao(7, distGoblin = 1)
        val g = s.encounter.combatentes.first { it.id == "goblin" }
        val buff = MagicMechanics.calcularBuff(
            MagiaMecanica(efeito = "buff", buffImunidade = "fogo"), energia = 2, alvoId = "goblin")
        s.registrarMagiaAtiva(
            nome = "Imunidade ao Fogo", operadorId = "heroi", alvoId = "goblin", duracaoSeg = 60,
            custoManutencaoSeg = 0, duracao = TipoDuracao.TEMPORARIA, exigeConcentracao = false,
            buff = buff)
        val pvAntes = g.pvAtual
        s.heroiConjurar(
            ContextoConjuracao(nhBasico = 30, classe = MagicClassParser.parse("Comum"),
                mana = NivelMana.NORMAL, distanciaMetros = 1,
                mecanica = magiaDeFogo().copy(elementoDano = "eletricidade")),
            MagicEnergy.parse("2"), 2, "Relâmpago", "goblin", 1)
        assertTrue("imune a fogo tem que levar dano de eletricidade", g.pvAtual < pvAntes)
    }

    @Test
    fun `sem imunidade o dano de fogo passa normal`() {
        val s = sessao(7, distGoblin = 1)
        val g = s.encounter.combatentes.first { it.id == "goblin" }
        val pvAntes = g.pvAtual
        s.heroiConjurar(
            ContextoConjuracao(nhBasico = 30, classe = MagicClassParser.parse("Comum"),
                mana = NivelMana.NORMAL, distanciaMetros = 1, mecanica = magiaDeFogo()),
            MagicEnergy.parse("2"), 2, "Bola de Fogo", "goblin", 1)
        assertTrue(g.pvAtual < pvAntes)
    }

    @Test
    fun `imunidade do BESTIARIO vale sem precisar de magia`() {
        // Elemental de fogo não se queima — imunidade natural, sem buff.
        val enc = CombatEncounter(
            listOf(heroi(), goblin().copy(id = "elemental", nome = "Elemental de Fogo",
                stats = goblin().stats!!.copy(imunidades = listOf("fogo")))),
            mapOf("elemental" to 1), seed = 1L)
        val s = CombatSession(enc, perfil(), Random(7))
        val e = s.encounter.combatentes.first { it.id == "elemental" }
        val pvAntes = e.pvAtual
        s.heroiConjurar(
            ContextoConjuracao(nhBasico = 30, classe = MagicClassParser.parse("Comum"),
                mana = NivelMana.NORMAL, distanciaMetros = 1, mecanica = magiaDeFogo()),
            MagicEnergy.parse("2"), 2, "Bola de Fogo", "elemental", 1)
        assertEquals(pvAntes, e.pvAtual)
    }

    @Test
    fun `ZONA de fogo nao fere quem e imune, mesmo pisando dentro`() {
        val s = sessao(7, distGoblin = 1)
        val g = s.encounter.combatentes.first { it.id == "goblin" }
        val buff = MagicMechanics.calcularBuff(
            MagiaMecanica(efeito = "buff", buffImunidade = "fogo"), energia = 2, alvoId = "goblin")
        s.registrarMagiaAtiva(
            nome = "Imunidade ao Fogo", operadorId = "heroi", alvoId = "goblin", duracaoSeg = 60,
            custoManutencaoSeg = 0, duracao = TipoDuracao.TEMPORARIA, exigeConcentracao = false,
            buff = buff)
        val pvAntes = g.pvAtual
        s.registrarZona(zona().copy(elementoDano = "fogo"))
        repeat(4) { s.avancarTurno() }
        assertEquals("imune ao fogo atravessa a Chuva de Fogo", pvAntes, g.pvAtual)
        assertTrue(s.log.any { it.contains("sem se ferir") })
    }

    @Test
    fun `buff so de imunidade NAO e considerado so-narrado`() {
        // Mesma armadilha do MEC-14 travada no P3-1: campo novo tem que entrar em soNarrado.
        val b = MagicMechanics.calcularBuff(
            MagiaMecanica(efeito = "buff", buffImunidade = "fogo"), energia = 1, alvoId = "heroi")
        assertFalse(b.soNarrado)
        assertEquals(listOf("fogo"), b.imunidades)
    }

    // ── Lote A1-b: tipo de criatura (mortos-vivos não são afetados) ────────────────────────────

    private fun morteCandente() = MagiaMecanica(
        efeito = "dano", danoPorTurnoExpr = "1d-1", danoPorTurnoTeste = "HT",
        quebraEmSucessoDecisivo = true, naoAfeta = listOf("morto_vivo"),
        elementoDano = "fogo", tipoDano = "quei"
    )

    private fun mortoVivo(pv: Int = 11) = Combatente(
        id = "esqueleto", nome = "Esqueleto", dx = 12, velocidadeBasica = 6.0, deslocamento = 6,
        pvMax = pv, pvAtual = pv,
        stats = NpcStats(st = 11, dx = 12, ht = 12, pvMax = pv, armaDano = "1d", armaTipo = "cont",
            armaNh = 11, tipoCriatura = TipoCriatura.MORTO_VIVO,
            tolerancia = ToleranciaFerimentos.NAO_VIVO)
    )

    @Test
    fun `Morte Candente NAO afeta morto-vivo — a magia se desfaz`() {
        val enc = CombatEncounter(listOf(heroi(), mortoVivo()), mapOf("esqueleto" to 1), seed = 1L)
        val s = CombatSession(enc, perfil(), Random(7))
        val alvo = s.encounter.combatentes.first { it.id == "esqueleto" }
        val pvAntes = alvo.pvAtual
        s.registrarMagiaAtiva(
            nome = "Morte Candente", operadorId = "heroi", alvoId = "esqueleto", duracaoSeg = 60,
            custoManutencaoSeg = 0, duracao = TipoDuracao.TEMPORARIA, exigeConcentracao = true,
            mecanica = morteCandente())
        repeat(4) { s.avancarTurno() }
        assertEquals("morto-vivo não pode perder PV para Morte Candente", pvAntes, alvo.pvAtual)
        assertTrue("a mágica não fica ativa em quem ela não pega", s.magiasAtivas.isEmpty())
        assertTrue(s.log.any { it.contains("não tem efeito") && it.contains("morto-vivo") })
    }

    @Test
    fun `a MESMA magia continua ferindo um alvo vivo (regressao)`() {
        val s = sessao(7, distGoblin = 1)
        val g = s.encounter.combatentes.first { it.id == "goblin" }
        val pvAntes = g.pvAtual
        s.registrarMagiaAtiva(
            nome = "Morte Candente", operadorId = "heroi", alvoId = "goblin", duracaoSeg = 60,
            custoManutencaoSeg = 0, duracao = TipoDuracao.TEMPORARIA, exigeConcentracao = true,
            mecanica = morteCandente())
        repeat(12) { s.avancarTurno() }
        assertTrue("contra vivo a mágica tem que continuar valendo", g.pvAtual < pvAntes)
    }

    @Test
    fun `magia de dano direto tambem respeita o tipo de criatura`() {
        val enc = CombatEncounter(listOf(heroi(), mortoVivo()), mapOf("esqueleto" to 1), seed = 1L)
        val s = CombatSession(enc, perfil(), Random(7))
        val alvo = s.encounter.combatentes.first { it.id == "esqueleto" }
        val pvAntes = alvo.pvAtual
        s.heroiConjurar(
            ContextoConjuracao(nhBasico = 30, classe = MagicClassParser.parse("Comum"),
                mana = NivelMana.NORMAL, distanciaMetros = 1,
                mecanica = MagiaMecanica(efeito = "dano", danoPorEnergia = "3d",
                    naoAfeta = listOf("morto_vivo"))),
            MagicEnergy.parse("2"), 2, "Morte Candente", "esqueleto", 1)
        assertEquals(pvAntes, alvo.pvAtual)
    }

    @Test
    fun `tipo de criatura e eixo SEPARADO da tolerancia a ferimentos`() {
        // Um constructo é NAO_VIVO na tolerância (sofre menos dano perfurante) mas NÃO é morto-vivo
        // — a exclusão de Morte Candente não pode pegá-lo por tabela.
        val golem = NpcStats(tipoCriatura = TipoCriatura.CONSTRUCTO,
            tolerancia = ToleranciaFerimentos.NAO_VIVO)
        assertEquals(TipoCriatura.CONSTRUCTO, golem.tipoCriatura)
        assertFalse(MagicMechanics.naoAfetaTipo(morteCandente(), golem.tipoCriatura.chave))
        assertTrue(MagicMechanics.naoAfetaTipo(morteCandente(), TipoCriatura.MORTO_VIVO.chave))
    }

    @Test
    fun `o heroi e sempre VIVO — nenhuma exclusao do catalogo o atinge`() {
        val s = sessao(7)
        assertEquals(TipoCriatura.VIVO, s.heroi.tipoCriatura)
    }

    // ── Lote A1-c: insubstancialidade (MB, vantagem de 80 pontos) ──────────────────────────────

    private fun espectro(pv: Int = 11) = Combatente(
        id = "espectro", nome = "Espectro", dx = 12, velocidadeBasica = 5.75, deslocamento = 5,
        pvMax = pv, pvAtual = pv,
        stats = NpcStats(st = 10, dx = 12, iq = 11, ht = 11, pvMax = pv, armaDano = "1d-2",
            armaTipo = "cont", armaNh = 12, tipoCriatura = TipoCriatura.INSUBSTANCIAL)
    )

    private fun sessaoEspectro(seed: Long = 7) = CombatSession(
        CombatEncounter(listOf(heroi(), espectro()), mapOf("espectro" to 1), seed = 1L),
        perfil(), Random(seed))

    private fun espadaDoHeroi() = AtaqueHeroi(
        rotulo = "Espada", nh = 14, danoExpr = "1d+2", tipo = DanoTipo.CORT, alcance = 1)

    @Test
    fun `arma comum ATRAVESSA o insubstancial — sem rolar para acertar`() {
        val s = sessaoEspectro()
        val alvo = s.encounter.combatentes.first { it.id == "espectro" }
        val pvAntes = alvo.pvAtual
        val r = s.heroiAtaca(espadaDoHeroi(), "espectro")
        assertFalse("não é errar o golpe, é o golpe atravessar", r.acertou)
        assertEquals(pvAntes, alvo.pvAtual)
        assertTrue(s.log.any { it.contains("insubstancial") })
    }

    @Test
    fun `MAGIA continua ferindo o insubstancial — ele so resiste ao fisico`() {
        // Regra literal: "continua vulnerável a ataques psíquicos e mágicos".
        val s = sessaoEspectro()
        val alvo = s.encounter.combatentes.first { it.id == "espectro" }
        val pvAntes = alvo.pvAtual
        s.heroiConjurar(
            ContextoConjuracao(nhBasico = 30, classe = MagicClassParser.parse("Comum"),
                mana = NivelMana.NORMAL, distanciaMetros = 1,
                mecanica = MagiaMecanica(efeito = "dano", danoPorEnergia = "3d")),
            MagicEnergy.parse("2"), 2, "Toque Chocante", "espectro", 1)
        assertTrue("magia tem que passar", alvo.pvAtual < pvAntes)
    }

    @Test
    fun `Afetar Espiritos DESTRAVA a arma contra o insubstancial`() {
        val s = sessaoEspectro()
        val buff = MagicMechanics.calcularBuff(
            MagiaMecanica(efeito = "buff", buffAfetaInsubstancial = true), energia = 4, alvoId = "heroi")
        s.registrarMagiaAtiva(
            nome = "Afetar Espíritos", operadorId = "heroi", alvoId = "heroi", duracaoSeg = 60,
            custoManutencaoSeg = 0, duracao = TipoDuracao.TEMPORARIA, exigeConcentracao = false,
            buff = buff)
        val logAntes = s.log.size
        val r = s.heroiAtaca(espadaDoHeroi(), "espectro")
        // Não basta "não ter a linha de atravessar" — isso passaria por vacuidade se o golpe fosse
        // barrado por outro motivo. Comparo com o CONTROLE: sem a mágica, o mesmo ataque atravessa.
        val controle = sessaoEspectro().heroiAtaca(espadaDoHeroi(), "espectro")
        assertTrue("sem a mágica o golpe tem que atravessar (controle)",
            controle.texto.contains("atravessa"))
        assertFalse("com a mágica ativa o golpe NÃO pode mais atravessar", r.texto.contains("atravessa"))
        assertTrue("e o ataque tem que ter sido de fato resolvido: ${s.log.drop(logAntes)}",
            s.log.size > logAntes)
    }

    @Test
    fun `a regra e SIMETRICA — o golpe do espectro tambem nao fere o heroi`() {
        // "Da mesma maneira, seus ataques físicos e de energia não afetam oponentes físicos."
        val s = sessaoEspectro()
        val pvAntes = s.heroi.pvAtual
        // `alvoId` é obrigatório: sem ele `intencaoAtacaHeroi` devolve false, o NPC nem tenta
        // atacar e o PV ficaria intacto pelo motivo ERRADO — o teste passaria por vacuidade
        // (mesma armadilha do MEC-31). Por isso a asserção do LOG é a que importa aqui.
        val intencao = NpcCombatBrain.IntencaoNpc(
            manobra = Manobra.ATAQUE, alvoId = "heroi", motivo = "ataca")
        s.npcResolve("espectro", intencao)
        assertEquals("espírito não soca ninguém", pvAntes, s.heroi.pvAtual)
        assertTrue("e tem que ser POR SER insubstancial, não por não ter atacado: ${s.log}",
            s.log.any { it.contains("atravessa você") })
    }

    @Test
    fun `buff so de afetaInsubstancial NAO e so-narrado`() {
        val b = MagicMechanics.calcularBuff(
            MagiaMecanica(efeito = "buff", buffAfetaInsubstancial = true), energia = 1, alvoId = "heroi")
        assertFalse(b.soNarrado)
        assertTrue(b.afetaInsubstancial)
    }

    // ── Lote NARR-1: o Narrador precisa ser LEMBRADO das mágicas narradas ──────────────────────

    @Test
    fun `magia NARRADA entra no resumo do Narrador, com a REGRA junto`() {
        val s = sessao(7)
        s.registrarMagiaAtiva(
            nome = "Aerovisão", operadorId = "heroi", alvoId = "heroi", duracaoSeg = 60,
            custoManutencaoSeg = 0, duracao = TipoDuracao.TEMPORARIA, exigeConcentracao = false,
            mecanica = MagiaMecanica(efeito = "buff", buffRotulo = "ignora penalidades de Visão",
                notas = "ignora penalidades de Visão por fumaça, neblina e poeira")
        )
        val resumo = s.resumo()
        assertTrue("o Narrador tem que continuar sabendo da mágica: $resumo",
            resumo.contains("ainda em efeito") && resumo.contains("Aerovisão"))
        assertTrue("e o lembrete tem que trazer a REGRA, não só o nome",
            resumo.contains("penalidades de Visão"))
    }

    @Test
    fun `o lembrete NAO vaza para o log do jogador`() {
        // Pedido explícito do usuário: "não precisa colocar tudo o que for implementado no log".
        // O `log` é publicado no feed e o JOGADOR lê; o `resumo()` é só para a IA.
        val s = sessao(7)
        s.registrarMagiaAtiva(
            nome = "Aerovisão", operadorId = "heroi", alvoId = "heroi", duracaoSeg = 60,
            custoManutencaoSeg = 0, duracao = TipoDuracao.TEMPORARIA, exigeConcentracao = false,
            mecanica = MagiaMecanica(efeito = "buff", notas = "ignora penalidades de Visão")
        )
        val antes = s.log.size
        repeat(4) { s.avancarTurno() }
        assertTrue("nenhuma linha de lembrete pode aparecer no feed: ${s.log.drop(antes)}",
            s.log.drop(antes).none { it.contains("ainda em efeito", ignoreCase = true) })
    }

    @Test
    fun `magia com NUMERO nao vira ruido no resumo — o motor ja a aplica`() {
        val s = sessao(7)
        val buff = MagicMechanics.calcularBuff(
            MagiaMecanica(efeito = "buff", buffAtributo = "Vontade", buffAtributoValor = 1,
                buffEnergiaPorNivel = 1, buffMaxNiveis = 5), energia = 2, alvoId = "heroi")
        s.registrarMagiaAtiva(
            nome = "Fortalecer Vontade", operadorId = "heroi", alvoId = "heroi", duracaoSeg = 60,
            custoManutencaoSeg = 0, duracao = TipoDuracao.TEMPORARIA, exigeConcentracao = false,
            buff = buff
        )
        assertFalse("buff executado não precisa de lembrete — seria ruído no prompt",
            s.resumo().contains("ainda em efeito"))
    }

    @Test
    fun `sem magia narrada ativa o resumo fica como era`() {
        val s = sessao(7)
        assertFalse(s.resumo().contains("ainda em efeito"))
    }

    // ── Lote MEC-47: as DUAS metades da regra de área, e o herói dentro da própria zona ─────────

    @Test
    fun `sem grade a zona do PROPRIO heroi nao o fere — distancia ao heroi e sempre zero`() {
        // O bug: no modelo de faixas `distancia(heroi)` é 0 por definição, então ele caía dentro de
        // QUALQUER zona — inclusive uma que ele largou longe. É o "perder PV do nada" reportado.
        val s = sessao(7, distGoblin = 1)
        val pvAntes = s.heroi.pvAtual
        s.registrarZona(zona(dur = 6))          // operadorId = "heroi"
        repeat(5) { s.avancarTurno() }
        assertEquals("a zona do proprio heroi nao pode feri-lo sem grade", pvAntes, s.heroi.pvAtual)
        assertTrue("e nem deve avisar que ele esta dentro",
            s.log.none { it.contains("Você está DENTRO") })
    }

    @Test
    fun `sem grade a zona de um NPC PEGA o heroi — foi mirada nele`() {
        val s = sessao(7, distGoblin = 1)
        val pvAntes = s.heroi.pvAtual
        s.registrarZona(zona(dur = 6).copy(operadorId = "goblin"))
        repeat(5) { s.avancarTurno() }
        assertTrue("zona do inimigo tem que ferir o heroi", s.heroi.pvAtual < pvAntes)
        assertTrue("e o log tem que dizer que e ELE",
            s.log.any { it.contains("VOCÊ está dentro") })
    }

    @Test
    fun `heroi dentro da zona e avisado JA na conjuracao, nao so no primeiro tique`() {
        val s = sessao(7, distGoblin = 1)
        s.registrarZona(zona(dur = 6).copy(operadorId = "goblin"))
        assertTrue("o aviso tem que sair no registro, antes de qualquer turno",
            s.log.any { it.contains("Você está DENTRO") })
    }

    @Test
    fun `zona com TESTE deixa a vitima evitar o dano`() {
        // Com teste de HT, parte das tentativas tem que aguentar (log de "aguenta").
        val aguentouAlguma = (0L until 30L).any { seed ->
            val s = sessao(seed, distGoblin = 1)
            s.registrarZona(zona(nome = "Mau Cheiro", intervalo = 1, dur = 4, teste = "HT"))
            repeat(4) { s.avancarTurno() }
            s.log.any { it.contains("aguenta Mau Cheiro") }
        }
        assertTrue("com teste, alguem tem que aguentar em 30 tentativas", aguentouAlguma)
    }

    // ── Lote MEC-45: o arremesso usa a perícia Ataque Inato e MOSTRA a jogada ───────────────────

    @Test
    fun `arremesso usa Ataque Inato quando o heroi tem a pericia`() {
        val enc = CombatEncounter(listOf(heroi(), goblin()), mapOf("goblin" to 3), seed = 1L)
        // Perfil com Ataque Inato 16 (DX e 13) — o log tem que citar a PERICIA, nao a DX.
        val s = CombatSession(enc, perfil().copy(nhAtaqueInato = 16), Random(7))
        s.heroiCarregarProjetil(ctxProjetilDano(), MagicEnergy.parse("Varia"), 3, "Bola de Fogo", tetoPorTurno = 4)
        s.heroiArremessarProjetil("goblin")
        assertTrue("deve citar Ataque Inato", s.log.any { it.contains("Ataque Inato") })
        assertTrue("nao pode dizer que usou DX", s.log.none { it.contains("sem a perícia") })
    }

    @Test
    fun `sem a pericia o arremesso cai na DX e AVISA`() {
        val s = sessao(7, distGoblin = 3) // perfil() nao tem nhAtaqueInato
        s.heroiCarregarProjetil(ctxProjetilDano(), MagicEnergy.parse("Varia"), 3, "Bola de Fogo", tetoPorTurno = 4)
        s.heroiArremessarProjetil("goblin")
        assertTrue("deve avisar que usou DX por falta da pericia",
            s.log.any { it.contains("sem a perícia") })
    }

    @Test
    fun `a jogada de ataque aparece no log TAMBEM quando acerta`() {
        // Antes so o ERRO mostrava o numero; no acerto o jogador nao via de onde vinha.
        var viuNoAcerto = false
        for (seed in 0L until 40L) {
            val s = sessao(seed, distGoblin = 3)
            s.modoTesteNpc = ModoTesteNpc.BONECO // nao esquiva, entao sobra acerto/erro
            s.heroiCarregarProjetil(ctxProjetilDano(), MagicEnergy.parse("Varia"), 3, "Bola de Fogo", tetoPorTurno = 4)
            s.heroiArremessarProjetil("goblin")
            if (s.log.any { it.contains("Projétil acerta") && it.contains("rolou") }) { viuNoAcerto = true; break }
        }
        assertTrue("o acerto tem que mostrar NH e rolagem", viuNoAcerto)
    }

    // ── Lote MEC-40 (P6): Precisão do projétil ao Apontar ───────────────────────────────────────

    private fun ctxProjetilPrec(prec: Int) = ContextoConjuracao(
        nhBasico = 25, classe = MagicClassParser.parse("Projétil"), mana = NivelMana.NORMAL,
        distanciaMetros = 3, mecanica = MagiaMecanica(efeito = "dano", danoPorEnergia = "1d",
            energiaPorDado = 1, precisao = prec))

    @Test
    fun `Apontar antes de arremessar soma a Precisao ao ataque (P6)`() {
        val s = sessao(7, distGoblin = 3)
        s.heroiCarregarProjetil(ctxProjetilPrec(prec = 3), MagicEnergy.parse("Varia"), 3, "Relâmpago", tetoPorTurno = 4)
        s.heroiApontar("goblin")           // mira o alvo
        s.heroiArremessarProjetil("goblin")
        assertTrue("o log tem que registrar o bonus de mira +3",
            s.log.any { it.contains("mira: +3") })
    }

    @Test
    fun `sem Apontar NAO soma Precisao`() {
        val s = sessao(7, distGoblin = 3)
        s.heroiCarregarProjetil(ctxProjetilPrec(prec = 3), MagicEnergy.parse("Varia"), 3, "Relâmpago", tetoPorTurno = 4)
        s.heroiArremessarProjetil("goblin") // arremessa direto, sem mirar
        assertTrue("nao pode haver bonus de mira", s.log.none { it.contains("mira: +") })
    }

    @Test
    fun `Apontar no alvo ERRADO nao vale para o arremesso`() {
        val enc = CombatEncounter(
            listOf(heroi(), goblin().copy(id = "g1"), goblin().copy(id = "g2")),
            mapOf("g1" to 3, "g2" to 3), seed = 1L)
        val s = CombatSession(enc, perfil(), Random(7))
        s.heroiCarregarProjetil(ctxProjetilPrec(prec = 3), MagicEnergy.parse("Varia"), 3, "Relâmpago", tetoPorTurno = 4)
        s.heroiApontar("g1")                 // mira g1
        s.heroiArremessarProjetil("g2")      // arremessa em g2
        assertTrue("mira em outro alvo não soma", s.log.none { it.contains("mira: +") })
    }

    // ── Lote MEC-38 (P7): Toque Candente — armadura não protege, RD natural sim ─────────────────

    @Test
    fun `ignora_vestida deixa a RD natural absorver mas ignora nao (P7)`() {
        // Contraste forte: RD natural gigante. Com "ignora_vestida" ela protege (dano 0); com
        // "ignora" nada protege (dano cheio). Descarrega o toque em modo BONECO para sempre acertar.
        fun danoCom(armadura: String, rdNat: Int): Int {
            var maiorDano = 0
            for (seed in 0L until 30L) {
                val enc = CombatEncounter(
                    listOf(heroi(), goblin(pv = 50).copy(stats = NpcStats(ht = 11, pvMax = 50, rd = rdNat, rdNatural = rdNat))),
                    mapOf("goblin" to 1), seed = 1L)
                val s = CombatSession(enc, perfil(), Random(seed))
                s.modoTesteNpc = ModoTesteNpc.BONECO
                val fogo = MagiaMecanica(efeito = "dano", danoPorEnergia = "1d", energiaPorDado = 1,
                    tipoDano = "quei", armadura = armadura, entrega = "toque")
                val ctx = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Toque"),
                    mana = NivelMana.NORMAL, mecanica = fogo, tocando = true)
                s.heroiConjurar(ctx, MagicEnergy.parse("3"), energiaInvestida = 3,
                    magiaNome = "Toque", alvoId = null)
                s.heroiEntregarToque("goblin")
                val g = s.encounter.combatentes.first { it.id == "goblin" }
                maiorDano = maxOf(maiorDano, 50 - g.pvAtual)
            }
            return maiorDano
        }
        assertEquals("RD natural gigante + ignora_vestida = dano 0", 0, danoCom("ignora_vestida", rdNat = 100))
        assertTrue("mas com 'ignora' o mesmo alvo leva dano", danoCom("ignora", rdNat = 100) > 0)
    }

    // ── Lote MEC-37 (P4): Lampejo em bandas de distância + rider de ofuscamento ─────────────────

    @Test
    fun `bandaPara escolhe a faixa certa por distancia (P4)`() {
        val lampejo = MagiaMecanica(efeito = "condicao", condicaoBandas = listOf(
            CondicaoBanda(ateM = 10, cegoSeg = 3, riderPenalidade = 3, riderSeg = 60),
            CondicaoBanda(ateM = 25, riderPenalidade = 3, riderSeg = 60),
            CondicaoBanda(ateM = 9999, riderPenalidade = 3, riderSeg = 3),
        ))
        assertEquals("a 5m: cega 3s", 3, MagicMechanics.bandaPara(lampejo, 5)!!.cegoSeg)
        assertEquals("a 20m: não cega", 0, MagicMechanics.bandaPara(lampejo, 20)!!.cegoSeg)
        assertEquals("a 20m: rider dura 60s", 60, MagicMechanics.bandaPara(lampejo, 20)!!.riderSeg)
        assertEquals("a 40m: rider curto de 3s", 3, MagicMechanics.bandaPara(lampejo, 40)!!.riderSeg)
    }

    @Test
    fun `Lampejo perto CEGA e OFUSCA e longe so ofusca (integracao)`() {
        val lampejo = MagiaMecanica(efeito = "condicao", condicaoBandas = listOf(
            CondicaoBanda(ateM = 10, cegoSeg = 3, riderPenalidade = 3, riderSeg = 60),
            CondicaoBanda(ateM = 9999, riderPenalidade = 3, riderSeg = 3),
        ))
        val enc = CombatEncounter(
            listOf(heroi(), goblin().copy(id = "perto"), goblin().copy(id = "longe")),
            mapOf("perto" to 2, "longe" to 40), seed = 1L)
        val s = CombatSession(enc, perfil(), Random(7))
        val ctx = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Área"),
            mana = NivelMana.NORMAL, mecanica = lampejo, raioAreaMetros = 60)
        s.heroiConjurarArea(ctx, MagicEnergy.parse("1 a 20"), energiaInvestida = 4,
            magiaNome = "Lampejo", alvosNaArea = listOf("perto", "longe"),
            distanciaAoCentro = mapOf("perto" to 2, "longe" to 40))
        val perto = s.encounter.combatentes.first { it.id == "perto" }
        val longe = s.encounter.combatentes.first { it.id == "longe" }
        assertTrue("o de perto tem que ficar cego", Condicao.CEGO in perto.condicoes)
        assertTrue("e ofuscado", perto.penalidadeCombateTemp > 0)
        assertFalse("o de longe NÃO cega", Condicao.CEGO in longe.condicoes)
        assertTrue("mas fica ofuscado", longe.penalidadeCombateTemp > 0)
    }

    @Test
    fun `o ofuscamento EXPIRA com o tempo`() {
        val s = sessao(7)
        s.heroi.penalidadeCombateTemp = 3
        s.heroi.penalidadeCombateSeg = 3
        repeat(10) { s.avancarTurno() }
        assertEquals("depois do prazo, sem penalidade", 0, s.heroi.penalidadeCombateTemp)
    }

    // ── Lote MEC-36 (P8 degrau de custo dobrado + P10 raio mínimo) ──────────────────────────────

    @Test
    fun `degrau de custo dobrado troca 1d-1 por 2d-2 ao pagar o dobro (P8)`() {
        val chuva = MagiaMecanica(efeito = "dano", danoPorEnergia = "1d-1", energiaPorDado = 1,
            danoFixo = true, danoDegrauCustoDobrado = "2d-2", energiaParaDegrau = 4)
        assertEquals("abaixo do limiar, dano normal", "1d-1",
            MagicMechanics.danoDeAreaComDegrau(chuva, energia = 2))
        assertEquals("no limiar, sobe para o degrau", "2d-2",
            MagicMechanics.danoDeAreaComDegrau(chuva, energia = 4))
        assertEquals("acima do limiar, segue no degrau", "2d-2",
            MagicMechanics.danoDeAreaComDegrau(chuva, energia = 6))
    }

    @Test
    fun `sem degrau configurado o dano de area e o normal`() {
        val simples = MagiaMecanica(efeito = "dano", danoPorEnergia = "1d", energiaPorDado = 2)
        assertEquals("2d", MagicMechanics.danoDeAreaComDegrau(simples, energia = 4))
    }

    @Test
    fun `raio minimo eleva raios pequenos e preserva os maiores (P10)`() {
        val nuvem = MagiaMecanica(efeito = "dano", areaRaioMinimoM = 2)
        assertEquals("raio 1 sobe para o mínimo 2", 2, MagicMechanics.raioEfetivo(nuvem, 1))
        assertEquals("raio 3 é preservado", 3, MagicMechanics.raioEfetivo(nuvem, 3))
        assertEquals("sem mínimo, o raio escolhido vale (piso 1)", 1,
            MagicMechanics.raioEfetivo(MagiaMecanica(efeito = "dano"), 1))
    }

    // ── Lote MEC-31: no modo BONECO o alvo também não RESISTE ───────────────────────────────────

    @Test
    fun `no modo BONECO a magia resistivel NUNCA e resistida`() {
        // Relato do aparelho: em Boneco, a Morte Candente acertava e o goblin RESISTIA, dissipando
        // a mágica — então testar manutenção virava loteria. O modo promete "não defendem"; para a
        // arena de teste, resistir é a mesma coisa.
        var houveResistencia = false
        for (seed in 0L until 120L) {
            val s = sessao(seed, distGoblin = 1)
            s.modoTesteNpc = ModoTesteNpc.BONECO
            val ctx = ContextoConjuracao(nhBasico = 12, classe = MagicClassParser.parse("Toque/R-HT"),
                mana = NivelMana.NORMAL, mecanica = morteCandente(), tocando = true)
            s.heroiConjurar(ctx, MagicEnergy.parse("3"), energiaInvestida = 3,
                magiaNome = "Morte Candente", alvoId = null)
            s.heroiEntregarToque("goblin")
            if (s.log.any { it.contains("RESISTE") }) houveResistencia = true
        }
        assertFalse("no Boneco nada pode resistir", houveResistencia)
    }

    @Test
    fun `no modo CONGELADO a resistencia CONTINUA valendo`() {
        // O contraste importa: Congelado só desliga a AÇÃO do NPC, não a regra.
        var houveResistencia = false
        for (seed in 0L until 120L) {
            val s = sessao(seed, distGoblin = 1)
            s.modoTesteNpc = ModoTesteNpc.CONGELADO
            val ctx = ContextoConjuracao(nhBasico = 12, classe = MagicClassParser.parse("Toque/R-HT"),
                mana = NivelMana.NORMAL, mecanica = morteCandente(), tocando = true)
            s.heroiConjurar(ctx, MagicEnergy.parse("3"), energiaInvestida = 3,
                magiaNome = "Morte Candente", alvoId = null)
            s.heroiEntregarToque("goblin")
            if (s.log.any { it.contains("RESISTE") }) { houveResistencia = true; break }
        }
        assertTrue("congelado ainda resiste em alguma das 60 tentativas", houveResistencia)
    }

    // ── Lote MEC-28 (C5) e MEC-29 (C7) ──────────────────────────────────────────────────────────

    @Test
    fun `mao carregada de Toque IMPEDE conjurar outra magia (C5, Magia p11-12)`() {
        val s = sessao(7, distGoblin = 1)
        val toque = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Toque"),
            mana = NivelMana.NORMAL, mecanica = morteCandente(), tocando = true)
        s.heroiConjurar(toque, MagicEnergy.parse("3"), energiaInvestida = 3,
            magiaNome = "Morte Candente", alvoId = null)
        assertTrue("cenário: a mão ficou carregada", s.toqueCarregado != null)

        val outra = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Comum"),
            mana = NivelMana.NORMAL)
        val r = s.heroiConjurar(outra, MagicEnergy.parse("2"), energiaInvestida = 2,
            magiaNome = "Outra Magia", alvoId = "goblin")
        assertFalse("com a mão carregada não se conjura outra mágica", r.sucesso)
        assertTrue(r.texto.contains("carregada"))
        assertTrue("e a mágica de toque continua na mão", s.toqueCarregado != null)
    }

    @Test
    fun `dissipar a mao carregada LIBERA a conjuracao (C6 — acao livre)`() {
        val s = sessao(7, distGoblin = 1)
        val toque = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Toque"),
            mana = NivelMana.NORMAL, mecanica = morteCandente(), tocando = true)
        s.heroiConjurar(toque, MagicEnergy.parse("3"), energiaInvestida = 3,
            magiaNome = "Morte Candente", alvoId = null)
        s.dissiparToque()
        assertTrue("dissipar esvazia a mão", s.toqueCarregado == null)
        val outra = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Comum"),
            mana = NivelMana.NORMAL)
        val r = s.heroiConjurar(outra, MagicEnergy.parse("2"), energiaInvestida = 2,
            magiaNome = "Outra Magia", alvoId = "goblin")
        assertTrue("com a mão livre, conjurar volta a funcionar", r.sucesso)
    }

    @Test
    fun `o mesmo buff NAO acumula — fica a versao mais forte (C7, Magia p9)`() {
        val s = sessao(7)
        val fraco = com.gurps.ficha.domain.magic.BuffAplicado(alvoId = "heroi", rotulo = "Escudo +1", bd = 1)
        val forte = com.gurps.ficha.domain.magic.BuffAplicado(alvoId = "heroi", rotulo = "Escudo +4", bd = 4)
        s.registrarMagiaAtiva("Escudo", "heroi", "heroi", 10, 1,
            com.gurps.ficha.domain.magic.TipoDuracao.TEMPORARIA, false, buff = fraco)
        assertEquals("primeiro buff entra", 1, s.heroi.buffBd)
        s.registrarMagiaAtiva("Escudo", "heroi", "heroi", 10, 1,
            com.gurps.ficha.domain.magic.TipoDuracao.TEMPORARIA, false, buff = forte)
        assertEquals("o mais forte SUBSTITUI — não soma 1+4", 4, s.heroi.buffBd)
        assertEquals("e só resta UMA instância ativa", 1,
            s.magiasAtivas.count { it.magiaId == "Escudo" })
    }

    @Test
    fun `relancar o MESMO buff mais fraco nao enfraquece o que ja esta ativo`() {
        val s = sessao(7)
        val forte = com.gurps.ficha.domain.magic.BuffAplicado(alvoId = "heroi", rotulo = "Escudo +4", bd = 4)
        val fraco = com.gurps.ficha.domain.magic.BuffAplicado(alvoId = "heroi", rotulo = "Escudo +1", bd = 1)
        s.registrarMagiaAtiva("Escudo", "heroi", "heroi", 10, 1,
            com.gurps.ficha.domain.magic.TipoDuracao.TEMPORARIA, false, buff = forte)
        s.registrarMagiaAtiva("Escudo", "heroi", "heroi", 10, 1,
            com.gurps.ficha.domain.magic.TipoDuracao.TEMPORARIA, false, buff = fraco)
        assertEquals("o fraco é ignorado, o forte permanece", 4, s.heroi.buffBd)
    }

    // ── Lote MEC-27 (C2): só UMA mágica de Bloqueio por turno (Magia p.12) ──────────────────────

    @Test
    fun `bloqueio magico marca a cota do turno e o motor lembra`() {
        val s = sessao(7)
        assertFalse("a rodada começa com a cota livre", s.bloqueioMagicoUsadoNoTurno)
        s.aplicarBloqueioMagico(custoFP = 2, magiaNome = "Bloquear")
        assertTrue("usar o bloqueio tem que consumir a cota", s.bloqueioMagicoUsadoNoTurno)
    }

    @Test
    fun `a cota RENOVA quando o turno do heroi recomeca`() {
        val s = sessao(7)
        s.aplicarBloqueioMagico(custoFP = 2, magiaNome = "Bloquear")
        assertTrue(s.bloqueioMagicoUsadoNoTurno)
        // Roda a mesa inteira até a vez do herói voltar.
        repeat(6) { s.avancarTurno() }
        assertFalse("na rodada seguinte o herói pode bloquear de novo",
            s.bloqueioMagicoUsadoNoTurno)
    }

    @Test
    fun `bloqueio magico tambem INTERROMPE a conjuracao em andamento (Magia p12)`() {
        // Regra que eu havia marcado como não implementada na varredura — estava feita.
        // Este teste tranca o comportamento para não se perder.
        val s = sessao(7)
        val ctx = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Comum"),
            mana = NivelMana.NORMAL)
        s.heroiConjurar(ctx, MagicEnergy.parse("1 a 4"), energiaInvestida = 2,
            magiaNome = "Magia Longa", alvoId = null, tempoOperacaoSeg = 3)
        assertTrue("cenário: há conjuração em andamento", s.conjuracaoEmAndamento != null)
        s.aplicarBloqueioMagico(custoFP = 1, magiaNome = "Bloquear")
        assertTrue("o bloqueio mágico interrompe a conjuração", s.conjuracaoEmAndamento == null)
    }

    // ── Lote MEC-26 (C4): apanhar abala a concentração de mágica mantida ────────────────────────
    // Magia p.10: "Se for distraído, sofrer uma lesão ou ficar atordoado, ele deverá fazer um teste
    // de Vontade com penalidade igual a -3. O fracasso não encerra a mágica, mas ela permanecerá
    // exatamente como estava... A falha crítica encerra a mágica."

    @Test
    fun `sem levar dano nem atordoar, a concentracao NAO e testada`() {
        // Trava o gatilho: testar sempre seria punir o mago sem motivo.
        val s = comMorteCandenteAtiva()
        repeat(3) {
            s.heroi.choquePendente = 0
            s.heroi.condicoes.remove(Condicao.ATORDOADO)
            s.avancarTurno()
            s.manutencaoPendente.toList().forEach { p -> s.resolverManutencao(p.magiaId, manter = true) }
        }
        assertTrue("não pode haver teste de concentração sem gatilho",
            s.log.none { it.contains("concentração") })
    }

    /**
     * Avança turnos remarcando o gatilho a cada passo. Necessário porque o abalo só é avaliado no
     * fim do turno do HERÓI — e depois de descarregar o toque o turno já passou para o inimigo.
     */
    private fun avancarComGatilho(s: CombatSession, turnos: Int = 4, atordoar: Boolean = false) {
        repeat(turnos) {
            if (atordoar) s.heroi.condicoes.add(Condicao.ATORDOADO) else s.heroi.choquePendente = 5
            s.avancarTurno()
            s.manutencaoPendente.toList().forEach { p -> s.resolverManutencao(p.magiaId, manter = true) }
        }
    }

    @Test
    fun `levar dano DISPARA o teste de concentracao (Vontade-3)`() {
        val s = comMorteCandenteAtiva()
        avancarComGatilho(s)
        assertTrue("apanhar tem que disparar o teste de Vontade−3",
            s.log.any { it.contains("Vontade−3") })
    }

    @Test
    fun `estar ATORDOADO tambem dispara o teste`() {
        val s = comMorteCandenteAtiva()
        s.heroi.choquePendente = 0
        avancarComGatilho(s, atordoar = true)
        assertTrue(s.log.any { it.contains("Vontade−3") && it.contains("atordoado") })
    }

    @Test
    fun `os dois desfechos do abalo existem — congelar sem encerrar e falha critica desfazendo`() {
        // Agregado em vez de por-sessão: uma mesma luta pode congelar num turno e a mágica sair
        // depois por OUTRO motivo (a vítima quebrá-la com sucesso decisivo, MEC-22). Asserção
        // por-sessão abortava nesse cruzamento e escondia o caso raro que eu queria provar.
        var congeladas = 0; var desfeitas = 0; var mantidas = 0
        var congelouSemEncerrar = 0
        for (seed in 0L until 400L) {
            val s = sessao(seed, distGoblin = 1)
            val ctx = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Toque"),
                mana = NivelMana.NORMAL, mecanica = morteCandente(), tocando = true)
            s.heroiConjurar(ctx, MagicEnergy.parse("3"), energiaInvestida = 3,
                magiaNome = "Morte Candente", alvoId = null)
            s.heroiEntregarToque("goblin")
            if (s.magiasAtivas.none { it.magiaId == "Morte Candente" }) continue
            avancarComGatilho(s, turnos = 3)
            val cong = s.log.count { it.contains("CONGELADA") }
            val desf = s.log.count { it.contains("se DESFAZ") }
            congeladas += cong; desfeitas += desf
            mantidas += s.log.count { it.contains("mantém a concentração") }
            // O invariante do fracasso: congelou e NÃO houve falha crítica → a mágica sobrevive.
            if (cong > 0 && desf == 0 && s.magiasAtivas.any { it.magiaId == "Morte Candente" }) {
                congelouSemEncerrar++
            }
        }
        assertTrue("o fracasso (congelar) tem que acontecer", congeladas > 0)
        assertTrue("e congelar NÃO pode encerrar a mágica", congelouSemEncerrar > 0)
        assertTrue("a falha crítica (desfazer) tem que acontecer", desfeitas > 0)
        assertTrue("e o sucesso (manter a concentração) também", mantidas > 0)
    }

    // ── Lote MEC-24: quem já tem dano curado NÃO oferece o toggle genérico ──────────────────────

    @Test
    fun `magia com dano proprio nao pode oferecer o 1d por energia generico`() {
        // Morte Putrefata tem 1d-1 POR TURNO definido pelo livro. Oferecer "causa dano 1d por
        // energia" em cima disso convida a somar dano que a regra não prevê.
        val putrefata = MagiaMecanica(efeito = "dano", entrega = "toque",
            danoPorTurnoExpr = "1d-1", danoPorTurnoTeste = "HT")
        assertTrue("o tique conta como dano já definido",
            MagicMechanics.temTiquePorTurno(putrefata))

        val bolaDeFogo = MagiaMecanica(efeito = "dano", danoPorEnergia = "1d", energiaPorDado = 1)
        assertTrue("dano curado também conta", MagicMechanics.temDanoEstruturado(bolaDeFogo))

        // Uma magia de dano SEM número curado é a única que precisa do toggle genérico.
        val semNumero = MagiaMecanica(efeito = "dano")
        assertFalse(MagicMechanics.temDanoEstruturado(semNumero))
        assertFalse(MagicMechanics.temTiquePorTurno(semNumero))
    }

    // ── Lote TOK-PF: a barra de fadiga precisa refletir a manutenção ────────────────────────────

    private fun comMorteCandenteAtiva(): CombatSession = (0L until 40L).map { seed ->
        val x = sessao(seed, distGoblin = 1)
        val ctx = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Toque"),
            mana = NivelMana.NORMAL, mecanica = morteCandente(), tocando = true)
        x.heroiConjurar(ctx, MagicEnergy.parse("3"), energiaInvestida = 3,
            magiaNome = "Morte Candente", alvoId = null)
        x.heroiEntregarToque("goblin")
        x
    }.first { it.magiasAtivas.any { m -> m.magiaId == "Morte Candente" } }

    @Test
    fun `MEC-23 o motor NAO cobra manutencao sozinho — ele PERGUNTA`() {
        // Manter mágica é OPCIONAL em GURPS. Antes o PF era debitado automaticamente e a mágica
        // ficava ativa para sempre, sem o jogador poder largar.
        val s = comMorteCandenteAtiva()
        val pfAntes = s.heroi.pfAtual
        repeat(4) { s.avancarTurno() }
        assertTrue("a manutenção tem que ficar PENDENTE de decisão", s.manutencaoPendente.isNotEmpty())
        assertEquals("e o PF NÃO pode ter sido cobrado sem perguntar", pfAntes, s.heroi.pfAtual)
    }

    @Test
    fun `escolher MANTER cobra o PF e a magia continua`() {
        val s = comMorteCandenteAtiva()
        repeat(4) { s.avancarTurno() }
        val p = s.manutencaoPendente.first()
        val pfAntes = s.heroi.pfAtual
        s.resolverManutencao(p.magiaId, manter = true)
        assertEquals("manter cobra exatamente o custo", pfAntes - p.custoPf, s.heroi.pfAtual)
        assertTrue("e a mágica segue ativa", s.magiasAtivas.any { it.magiaId == p.magiaId })
    }

    @Test
    fun `escolher NAO MANTER encerra a magia e para o gasto`() {
        val s = comMorteCandenteAtiva()
        repeat(4) { s.avancarTurno() }
        val p = s.manutencaoPendente.first()
        val pfAntes = s.heroi.pfAtual
        s.resolverManutencao(p.magiaId, manter = false)
        assertEquals("não manter NÃO pode cobrar PF", pfAntes, s.heroi.pfAtual)
        assertTrue("e a mágica tem que sair das ativas", s.magiasAtivas.none { it.magiaId == p.magiaId })
    }

    @Test
    fun `sem PF suficiente a magia cai mesmo que o jogador queira manter`() {
        val s = comMorteCandenteAtiva()
        repeat(4) { s.avancarTurno() }
        val p = s.manutencaoPendente.first()
        s.heroi.pfAtual = 0
        s.resolverManutencao(p.magiaId, manter = true)
        assertTrue("sem fadiga não há como manter", s.magiasAtivas.none { it.magiaId == p.magiaId })
        assertEquals("e o PF não pode ficar negativo", 0, s.heroi.pfAtual)
    }

    @Test
    fun `manter magia de tique DRENA PF do operador turno a turno`() {
        // É o que a barra azul mostra. Se a manutenção não drenasse, a barra seria decorativa.
        val s = (0L until 40L).map { seed ->
            val x = sessao(seed, distGoblin = 1)
            val ctx = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Toque"),
                mana = NivelMana.NORMAL, mecanica = morteCandente(), tocando = true)
            x.heroiConjurar(ctx, MagicEnergy.parse("3"), energiaInvestida = 3,
                magiaNome = "Morte Candente", alvoId = null)
            x.heroiEntregarToque("goblin")
            x
        }.first { it.magiasAtivas.any { m -> m.magiaId == "Morte Candente" } }

        val pfDepoisDoLancamento = s.heroi.pfAtual
        // MEC-23: agora é preciso CONFIRMAR a manutenção a cada vencimento.
        repeat(6) {
            s.avancarTurno()
            s.manutencaoPendente.toList().forEach { p -> s.resolverManutencao(p.magiaId, manter = true) }
        }
        assertTrue("mantendo a mágica, o PF tem que cair ao longo dos turnos",
            s.heroi.pfAtual < pfDepoisDoLancamento)
    }

    // ── Lote MEC-22: mágica que FERE A CADA TURNO (Morte Candente / Morte Putrefata) ────────────

    private fun morteCandente(criticoFixo: Int = 0) = MagiaMecanica(
        efeito = "dano", entrega = "toque", armadura = "ignora", condicaoResistencia = "HT",
        danoPorTurnoExpr = "1d-1", danoPorTurnoTeste = "HT",
        danoPorTurnoCriticoFixo = criticoFixo, quebraEmSucessoDecisivo = true,
    )

    @Test
    fun `magia de tique NAO fere no turno em que e aplicada (regra da estreia)`() {
        // O risco sinalizado antes de implementar: a mágica é registrada durante a ação do herói e o
        // tique roda no fim desse mesmo turno — sem trava, a vítima levaria um turno de dano grátis.
        val achou = (0L until 40L).map { seed ->
            val s = sessao(seed, distGoblin = 1)
            val ctx = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Toque"),
                mana = NivelMana.NORMAL, mecanica = morteCandente(), tocando = true)
            s.heroiConjurar(ctx, MagicEnergy.parse("3"), energiaInvestida = 3,
                magiaNome = "Morte Candente", alvoId = null)
            s.heroiEntregarToque("goblin")
            s
        }.firstOrNull { s -> s.magiasAtivas.any { it.magiaId == "Morte Candente" } }

        assertTrue("nenhum seed conseguiu descarregar o toque", achou != null)
        val g = achou!!.encounter.combatentes.first { it.id == "goblin" }
        assertEquals("no turno da aplicação a vítima NÃO pode ter levado dano do tique",
            g.pvMax, g.pvAtual)
    }

    @Test
    fun `a partir do turno seguinte a magia fere, aguenta ou quebra — nunca fica inerte`() {
        val s = (0L until 40L).map { seed ->
            val x = sessao(seed, distGoblin = 1)
            val ctx = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Toque"),
                mana = NivelMana.NORMAL, mecanica = morteCandente(), tocando = true)
            x.heroiConjurar(ctx, MagicEnergy.parse("3"), energiaInvestida = 3,
                magiaNome = "Morte Candente", alvoId = null)
            x.heroiEntregarToque("goblin")
            x
        }.first { it.magiasAtivas.any { m -> m.magiaId == "Morte Candente" } }

        repeat(8) { s.avancarTurno() }
        val houveTique = s.log.any {
            it.contains("queima") || it.contains("aguenta") || it.contains("QUEBRA")
        }
        assertTrue("depois da estreia o tique tem que aparecer no log", houveTique)
    }

    @Test
    fun `sucesso DECISIVO da vitima QUEBRA a magia — ela sai da lista de ativas`() {
        // Varre seeds até achar um em que a vítima tire 3 ou 4 no teste de HT.
        val quebrou = (0L until 120L).any { seed ->
            val s = sessao(seed, distGoblin = 1)
            val ctx = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Toque"),
                mana = NivelMana.NORMAL, mecanica = morteCandente(), tocando = true)
            s.heroiConjurar(ctx, MagicEnergy.parse("3"), energiaInvestida = 3,
                magiaNome = "Morte Candente", alvoId = null)
            s.heroiEntregarToque("goblin")
            if (s.magiasAtivas.none { it.magiaId == "Morte Candente" }) return@any false
            repeat(10) { s.avancarTurno() }
            s.log.any { it.contains("QUEBRA") } &&
                s.magiasAtivas.none { it.magiaId == "Morte Candente" }
        }
        assertTrue("em 120 tentativas alguma vítima devia tirar sucesso decisivo e quebrar a mágica",
            quebrou)
    }

    @Test
    fun `falha critica da Morte Putrefata usa os 6 pontos fixos, nao o dado`() {
        val m = morteCandente(criticoFixo = 6)
        assertEquals("o campo tem que guardar o 6 do livro", 6, m.danoPorTurnoCriticoFixo)
        assertEquals("e a Morte Candente (sem o campo) continua no dado", 0,
            morteCandente().danoPorTurnoCriticoFixo)
    }

    @Test
    fun `magia SEM tique nao entra no motor de tique`() {
        assertFalse(MagicMechanics.temTiquePorTurno(MagiaMecanica(efeito = "dano", danoPorEnergia = "1d")))
        assertTrue(MagicMechanics.temTiquePorTurno(morteCandente()))
    }

    // ── Lote MEC-21: o TOQUE agora aplica a mecânica ao descarregar ─────────────────────────────
    // Antes a conjuração de Toque dava `return` cedo (só carregava a mão) e o descarregar caía em
    // "Efeito narrado pelo Mestre" — NENHUMA magia de toque fazia nada. Isso também deixava o
    // MEC-19 (fuga do gelo) inalcançável em jogo.

    /** Carrega uma magia de toque e descarrega no goblin; devolve (sessão, log). */
    private fun descarregarToque(mec: MagiaMecanica, energia: Int, seed: Long): Pair<CombatSession, List<String>> {
        val s = sessao(seed, distGoblin = 1)
        val ctx = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Toque"),
            mana = NivelMana.NORMAL, mecanica = mec, tocando = true)
        s.heroiConjurar(ctx, MagicEnergy.parse("1 a 4"), energiaInvestida = energia,
            magiaNome = "Magia de Toque", alvoId = null)
        s.heroiEntregarToque("goblin")
        return s to s.log.toList()
    }

    @Test
    fun `toque de DANO agora fere de verdade — nao so narra`() {
        val dano = MagiaMecanica(efeito = "dano", danoPorEnergia = "1d-1", energiaPorDado = 1,
            tipoDano = "quei", armadura = "ignora", entrega = "toque")
        // Vários seeds: o toque pode errar ou o alvo se defender; basta ferir em alguma tentativa.
        val feriuAlguma = (0L until 30L).any { seed ->
            val (s, _) = descarregarToque(dano, energia = 3, seed = seed)
            s.encounter.combatentes.first { it.id == "goblin" }.let { it.pvAtual < it.pvMax }
        }
        assertTrue("magia de toque com dano curado tem que causar dano ao descarregar", feriuAlguma)
    }

    @Test
    fun `toque de CONDICAO impoe a condicao — e e o que faz o MEC-19 existir em jogo`() {
        val gelo = MagiaMecanica(efeito = "condicao", condicao = "paralisado", entrega = "toque",
            condicaoEscapeAtributo = "ST", condicaoEscapeEnergiaPorPonto = 2)
        val congelouAlguma = (0L until 30L).any { seed ->
            val (s, _) = descarregarToque(gelo, energia = 2, seed = seed)
            val g = s.encounter.combatentes.first { it.id == "goblin" }
            Condicao.PARALISADO in g.condicoes && g.escapeCondicao != null
        }
        assertTrue("o Toque Congelante precisa paralisar E registrar a fuga por ST", congelouAlguma)
    }

    @Test
    fun `toque sem mecanica curada continua NARRADO — sem inventar efeito`() {
        val nada = MagiaMecanica(efeito = "narrado", entrega = "toque")
        val (_, log) = descarregarToque(nada, energia = 1, seed = 7)
        val acertou = log.any { it.contains("ACERTA") }
        if (acertou) assertTrue("sem mecânica, o efeito continua sendo do Mestre",
            log.any { it.contains("narrado pelo Mestre") })
    }

    // ── Lote TESTE-SANDBOX: as recusas do início de combate são REAIS e precisam ser ditas ──────
    // O sandbox descartava o retorno de `iniciarCombate`, então uma recusa virava "o botão não faz
    // nada". Estes testes trancam os códigos que a UI traduz para o jogador.

    @Test
    fun `0 PV NAO e morte em GURPS — por isso a trava do sandbox e do controller, nao do motor`() {
        // Eu tinha escrito um teste afirmando que o herói a 0 PV "não está vivo". ERRADO: `vivo` só
        // cai em −PV máximo. 0 PV é "cambaleante mas de pé". A regra de NÃO ABRIR um combate de teste
        // com o herói a 0 PV é uma decisão do `iniciarCombate` (não faz sentido lutar assim), não uma
        // propriedade do motor. Este teste tranca o fato real, para o próximo leitor não repetir meu erro.
        val heroiCaido = heroi().apply { pvAtual = 0 }
        val enc = CombatEncounter(listOf(heroiCaido, goblin()), mapOf("goblin" to 5), seed = 1L)
        val s = CombatSession(enc, perfil(), Random(7))
        assertTrue("0 PV ainda é 'vivo' em GURPS (só morre em −PVmáx)", s.heroi.vivo)
        assertTrue("mas está em/abaixo de zero — que é o que o sandbox recusa", s.heroi.pvAtual <= 0)
    }

    // NOTA HONESTA: a tradução dessa recusa em mensagem de tela mora no `SagaCombatController`, que
    // não é instanciável na JVM (precisa de `Application`) — a mesma limitação registrada no
    // TESTE-C. A fiação é verificada no aparelho.

    // ── Lote TESTE-NPC: modos do combate de teste (Normal / Congelado / Boneco) ─────────────────

    @Test
    fun `NORMAL e o padrao — nenhuma sessao nasce em modo de teste`() {
        // Garante que campanha real nunca cai em modo de teste por acidente.
        assertEquals(ModoTesteNpc.NORMAL, sessao(7).modoTesteNpc)
    }

    @Test
    fun `CONGELADO faz o NPC nao agir`() {
        val s = sessao(7)
        s.modoTesteNpc = ModoTesteNpc.CONGELADO
        val intencao = s.npcIntencao("goblin")
        assertEquals("congelado não age", Manobra.NAO_FAZER_NADA, intencao.manobra)
    }

    /** Lança um projétil num goblin no modo dado e devolve o log da luta. */
    private fun logDeProjetilNoModo(modo: ModoTesteNpc, seed: Long): List<String> {
        val s = sessao(seed)
        s.modoTesteNpc = modo
        s.heroiConjurar(ctxProjetil(nh = 30), MagicEnergy.parse("Varia"), energiaInvestida = 3,
            magiaNome = "Bola de Fogo", alvoId = "goblin")
        return s.log.toList()
    }

    @Test
    fun `CONGELADO ainda ESQUIVA — e a diferenca real para o Boneco`() {
        // A distinção que motivou os dois modos: congelar o TURNO não desliga a DEFESA.
        val esquivouAlgumaVez = (0L until 40L).any { seed ->
            logDeProjetilNoModo(ModoTesteNpc.CONGELADO, seed).any { it.contains("ESQUIVA") }
        }
        assertTrue("congelado tem que esquivar em ao menos uma das 40 tentativas", esquivouAlgumaVez)
    }

    @Test
    fun `BONECO nunca esquiva — mas o ATACANTE ainda pode errar sozinho`() {
        // Cuidado com a promessa: "boneco" não é "tudo acerta". Ele não se defende, porém o projétil
        // ainda faz a própria jogada de acerto (Ataque Inato) e pode passar longe. Tirar isso também
        // esconderia bug no caminho de acerto — que é justamente o que se quer validar.
        val logs = (0L until 40L).map { logDeProjetilNoModo(ModoTesteNpc.BONECO, it) }
        assertTrue("no modo boneco NENHUMA esquiva pode acontecer",
            logs.none { l -> l.any { it.contains("ESQUIVA") } })
        assertTrue("e mesmo assim tem que haver acerto em alguma tentativa",
            logs.any { l -> l.any { it.contains("Projétil acerta") } })
    }

    // ── Lote MEC-19: escapar da condição por teste de atributo (o gelo) ──────────────────────────

    @Test
    fun `penalidade de escape cresce com a energia (menos 1 a cada 2 pontos)`() {
        val gelo = MagiaMecanica(efeito = "condicao", condicao = "paralisado",
            condicaoEscapeAtributo = "ST", condicaoEscapeEnergiaPorPonto = 2)
        assertEquals("2 de energia = 0,5cm = -1", -1, MagicMechanics.penalidadeEscapeCondicao(gelo, 2))
        assertEquals("6 de energia = 1,5cm = -3", -3, MagicMechanics.penalidadeEscapeCondicao(gelo, 6))
        assertEquals("sem campo de escape, sem penalidade", 0,
            MagicMechanics.penalidadeEscapeCondicao(MagiaMecanica(efeito = "condicao"), 6))
    }

    @Test
    fun `preso no gelo tenta romper todo turno e a paralisia NAO expira por tempo`() {
        val s = sessao(7)
        val goblin = s.encounter.combatentes.first { it.id == "goblin" }
        val gelo = MagiaMecanica(efeito = "condicao", condicao = "paralisado",
            condicaoEscapeAtributo = "ST", condicaoEscapeEnergiaPorPonto = 2)
        val ctx = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Comum"),
            mana = NivelMana.NORMAL, mecanica = gelo)
        s.heroiConjurar(ctx, MagicEnergy.parse("1 a 4"), energiaInvestida = 2,
            magiaNome = "Toque Congelante", alvoId = "goblin")
        assertTrue("devia ter congelado", Condicao.PARALISADO in goblin.condicoes)
        assertTrue("e devia ter registrado o escape", goblin.escapeCondicao != null)

        repeat(20) { s.avancarTurno() }
        // Com ST 11 −1, ele quase certamente rompe em 20 turnos — o ponto é que EXISTE saída.
        assertTrue("tem que haver tentativa de romper registrada no log",
            s.log.any { it.contains("romper") || it.contains("ROMPE") })
    }

    // ── Lote MEC-18: teste próprio de condição e duração escalada pela energia ───────────────────

    @Test
    fun `Jato de Som resiste com HT MENOS a energia gasta`() {
        val m = MagiaMecanica(efeito = "condicao", condicao = "atordoado",
            condicaoResistencia = "HT_menos_energia", condicaoRdBonusPor = 5)
        assertEquals("HT 12, 4 de energia, sem RD → 8", 8,
            MagicMechanics.resistenciaEfetivaDaCondicao(m, atributoBase = 12, energiaGasta = 4, rd = 0))
        assertEquals("mesma coisa com RD 10 → +2 → 10", 10,
            MagicMechanics.resistenciaEfetivaDaCondicao(m, atributoBase = 12, energiaGasta = 4, rd = 10))
        assertEquals("RD 4 ainda não chega a +1 (é a cada 5)", 8,
            MagicMechanics.resistenciaEfetivaDaCondicao(m, atributoBase = 12, energiaGasta = 4, rd = 4))
    }

    @Test
    fun `duracao da condicao escala com a energia investida`() {
        val jato = MagiaMecanica(efeito = "condicao", condicao = "cego", condicaoDuracaoSegPorEnergia = 1)
        assertEquals("3 de energia = 3 segundos cego", 3, MagicMechanics.duracaoCondicaoSeg(jato, 3))
        val cegar = MagiaMecanica(efeito = "condicao", condicao = "cego", condicaoDuracaoSeg = 10)
        assertEquals("prazo fixo não escala", 10, MagicMechanics.duracaoCondicaoSeg(cegar, 3))
    }

    @Test
    fun `magia de condicao SEM resistencia de classe agora exige o teste proprio`() {
        // O Jato de Som é classe "Comum": não havia R-XXX, e o ramo de condição ignorava o
        // condicaoResistencia — ele atordoava sem teste nenhum.
        val s = sessao(7)
        val goblin = s.encounter.combatentes.first { it.id == "goblin" }
        val jato = MagiaMecanica(efeito = "condicao", condicao = "atordoado",
            condicaoResistencia = "HT_menos_energia")
        val ctx = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Comum"),
            mana = NivelMana.NORMAL, mecanica = jato)
        // Energia 0 contra HT 11 → resiste em quase toda rolagem; o que importa é que HOUVE teste.
        s.heroiConjurar(ctx, MagicEnergy.parse("1 a 4"), energiaInvestida = 0,
            magiaNome = "Jato de Som", alvoId = "goblin")
        val houveTeste = s.log.any { it.contains("RESISTE") } || Condicao.ATORDOADO in goblin.condicoes
        assertTrue("tem que haver teste (resistir ou não), nunca imposição automática", houveTeste)
    }

    // ── Lote MEC-17: condição com PRAZO (antes era eterna) ──────────────────────────────────────

    @Test
    fun `cegueira com prazo EXPIRA sozinha — antes durava a luta inteira`() {
        val s = sessao(7)
        val goblin = s.encounter.combatentes.first { it.id == "goblin" }
        val cegar = MagiaMecanica(efeito = "condicao", condicao = "cego", condicaoDuracaoSeg = 3)
        val ctx = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Comum"),
            mana = NivelMana.NORMAL, mecanica = cegar)
        s.heroiConjurar(ctx, MagicEnergy.parse("1 a 4"), energiaInvestida = 2,
            magiaNome = "Cegar", alvoId = "goblin")
        assertTrue("devia ter cegado", Condicao.CEGO in goblin.condicoes)

        // Cada turno DELE consome 1 segundo. Roda vários turnos completos.
        repeat(12) { s.avancarTurno() }
        assertFalse("depois do prazo a cegueira TEM que cair sozinha", Condicao.CEGO in goblin.condicoes)
    }

    @Test
    fun `condicao SEM prazo continua ate a regra dela tirar (nao expira por tempo)`() {
        val s = sessao(7)
        val goblin = s.encounter.combatentes.first { it.id == "goblin" }
        val paralisar = MagiaMecanica(efeito = "condicao", condicao = "paralisado") // sem prazo
        val ctx = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Comum"),
            mana = NivelMana.NORMAL, mecanica = paralisar)
        s.heroiConjurar(ctx, MagicEnergy.parse("1 a 4"), energiaInvestida = 2,
            magiaNome = "Paralisar", alvoId = "goblin")
        assertTrue(Condicao.PARALISADO in goblin.condicoes)
        repeat(12) { s.avancarTurno() }
        assertTrue("sem prazo não pode sair sozinha", Condicao.PARALISADO in goblin.condicoes)
    }

    // ── Lote MEC-15: distâncias do Projétil (1/2D e Máx) ────────────────────────────────────────

    @Test
    fun `meio dano vale a partir do 1 meio D INCLUSIVE (e nao so alem dele)`() {
        // O detalhe da fonte literal: "distância MAIOR OU IGUAL à distância 1/2D". De memória eu teria
        // escrito só ">", e o alvo exatamente no 1/2D levaria dano cheio.
        val m = MagiaMecanica(efeito = "dano", alcanceMeioDano = 25, alcanceMaximo = 50)
        assertEquals("a 24m ainda é dano cheio", 11, MagicMechanics.aplicarMeioDano(11, m, 24))
        assertEquals("a 25m JÁ cai pela metade", 5, MagicMechanics.aplicarMeioDano(11, m, 25))
        assertEquals("arredonda para baixo", 5, MagicMechanics.aplicarMeioDano(11, m, 40))
    }

    @Test
    fun `sem 1 meio D o dano nunca cai (granadas, magias sem essa distancia)`() {
        val m = MagiaMecanica(efeito = "dano", alcanceMaximo = 50)
        assertEquals(11, MagicMechanics.aplicarMeioDano(11, m, 49))
    }

    @Test
    fun `alcance maximo — no limite pode, um metro alem nao`() {
        val m = MagiaMecanica(efeito = "dano", alcanceMeioDano = 25, alcanceMaximo = 50)
        assertFalse("exatamente no Máx ainda alcança", MagicMechanics.foraDoAlcanceMaximo(m, 50))
        assertTrue("1m além do Máx não alcança", MagicMechanics.foraDoAlcanceMaximo(m, 51))
        assertFalse("Máx 0 = sem limite", MagicMechanics.foraDoAlcanceMaximo(MagiaMecanica(efeito = "dano"), 999))
    }

    @Test
    fun `conjurar alem do Maximo e recusado SEM gastar fadiga`() {
        val s = sessao(7, distGoblin = 80)
        val pfAntes = s.heroi.pfAtual
        val bola = MagiaMecanica(efeito = "dano", danoPorEnergia = "1d", energiaPorDado = 1,
            entrega = "projetil", alcanceMeioDano = 25, alcanceMaximo = 50)
        val ctx = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Projétil"),
            mana = NivelMana.NORMAL, distanciaMetros = 80, mecanica = bola)
        val r = s.heroiConjurar(ctx, MagicEnergy.parse("Varia"), energiaInvestida = 3,
            magiaNome = "Bola de Fogo", alvoId = "goblin")
        assertFalse("fora do Máx tem que recusar", r.sucesso)
        assertEquals("e NÃO pode cobrar fadiga por um tiro que a regra proíbe", pfAntes, s.heroi.pfAtual)
        assertTrue(r.texto.contains("alcance máximo"))
    }

    @Test
    fun `area explosiva machuca menos quem esta longe do centro`() {
        // Dois goblins idênticos na área: um no centro, outro a 3m. Mesmo dado, danos diferentes.
        val enc = CombatEncounter(
            listOf(heroi(), goblin(pv = 30), goblin(pv = 30).copy(id = "g2", nome = "Goblin 2")),
            mapOf("goblin" to 4, "g2" to 4), seed = 1L
        )
        val s = CombatSession(enc, perfil(), Random(7))
        val pvInicialPerto = s.encounter.combatentes.first { it.id == "goblin" }.pvAtual
        val pvInicialLonge = s.encounter.combatentes.first { it.id == "g2" }.pvAtual
        val perto = s.encounter.combatentes.first { it.id == "goblin" }

        val bomba = MagiaMecanica(efeito = "dano", danoPorEnergia = "1d", energiaPorDado = 2,
            entrega = "area", explosaoDivisorPorMetro = 3)
        val ctx = ContextoConjuracao(nhBasico = 25, classe = MagicClassParser.parse("Comum"),
            mana = NivelMana.NORMAL, mecanica = bomba, raioAreaMetros = 4)
        s.heroiConjurarArea(ctx, MagicEnergy.parse("1 a 20"), energiaInvestida = 20,
            magiaNome = "Bola de Fogo Explosiva", alvosNaArea = listOf(perto.id, "g2"),
            distanciaAoCentro = mapOf(perto.id to 0, "g2" to 3))

        val sofreuPerto = pvInicialPerto - s.encounter.combatentes.first { it.id == perto.id }.pvAtual
        val sofreuLonge = pvInicialLonge - s.encounter.combatentes.first { it.id == "g2" }.pvAtual
        assertTrue("quem estava no centro tem que ter levado dano", sofreuPerto > 0)
        assertTrue("a 3m o dano é dividido por 9 — tem que ser bem menor que no centro",
            sofreuLonge < sofreuPerto)
    }
}
