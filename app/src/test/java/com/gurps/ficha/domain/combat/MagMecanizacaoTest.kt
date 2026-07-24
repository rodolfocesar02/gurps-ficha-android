package com.gurps.ficha.domain.combat

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.domain.magic.ContextoConjuracao
import com.gurps.ficha.domain.magic.MagiaMecanica
import com.gurps.ficha.domain.magic.MagicClassParser
import com.gurps.ficha.domain.magic.MagicEnergy
import com.gurps.ficha.domain.magic.MagicMechanics
import com.gurps.ficha.domain.magic.NivelMana
import com.gurps.ficha.domain.combat.subsistemas.EfeitosMagicosDelegate
import com.gurps.ficha.model.MagiaDefinicao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Test
import java.io.File
import kotlin.random.Random

/**
 * Testes dos lotes de MECANIZAÇÃO de magias (MAG-1..7), rodados contra o catálogo REAL
 * (`magias2versao.json`). Duas camadas por lote:
 *  1. **Curadoria** — trava os campos de `mecanica` no JSON real (anti-regressão, igual ao P3-1: uma
 *     curadoria só vale enquanto o dado sobrevive).
 *  2. **Efeito real** — pega a `mecanica` curada e prova que ela vira o número que o COMBATE lê
 *     (`calcularBuff` → `stEfetivo`/`dxEfetivo`/`htEfetivo`/defesas). Não basta o campo existir: ele
 *     tem que mover a estatística efetiva, senão é buff fantasma.
 *
 * Sem o arquivo (build headless), os testes são PULADOS (Assume) sem falhar.
 */
class MagMecanizacaoTest {

    private fun catalogo(): Map<String, MagiaDefinicao>? {
        val arq = listOf("src/main/assets/magias2versao.json", "app/src/main/assets/magias2versao.json")
            .map { File(it) }.firstOrNull { it.exists() } ?: return null
        val type = object : TypeToken<List<MagiaDefinicao>>() {}.type
        val list: List<MagiaDefinicao> = Gson().fromJson(arq.readText(Charsets.UTF_8), type)
        return list.associateBy { it.id }
    }

    private fun mec(cat: Map<String, MagiaDefinicao>, id: String): MagiaMecanica {
        val d = cat[id] ?: error("magia '$id' não existe no catálogo")
        return d.mecanica ?: error("magia '$id' está sem mecanica")
    }

    /** Alvo de teste com ST/DX/HT/IQ explícitos (o combate lê stEfetivo = stats.st + buffSt; Vontade ~ IQ). */
    private fun alvo(st: Int = 12, dx: Int = 12, ht: Int = 12, iq: Int = 10) = Combatente(
        id = "g", nome = "Alvo", dx = dx, velocidadeBasica = 6.0, deslocamento = 6, pvMax = 20, pvAtual = 20,
        stats = NpcStats(st = st, dx = dx, ht = ht, iq = iq, pvMax = 20)
    )

    // ═══════════════════════════ MAG-1 — buffs/debuffs de atributo de Corpo ═══════════════════════

    @Test
    fun `MAG-1 curadoria — as 11 magias de Corpo estao mecanizadas com os numeros do livro`() {
        val cat = catalogo(); Assume.assumeNotNull(cat)
        // id -> (atributo, valorPorNivel, energiaPorNivel, maxNiveis)
        val esperado = mapOf(
            "forca" to arrayOf("ST", 1, 2, 5), "graca" to arrayOf("DX", 1, 4, 5),
            "vigor" to arrayOf("HT", 1, 2, 5), "debilitar" to arrayOf("ST", -1, 1, 5),
            "inabilidade" to arrayOf("DX", -1, 1, 5), "fragilidade" to arrayOf("HT", -1, 2, 5),
            "aumentar_forca" to arrayOf("ST", 1, 1, 5), "aumentar_destreza" to arrayOf("DX", 1, 1, 5),
            "aumentar_vitalidade" to arrayOf("HT", 1, 1, 5),
        )
        val erros = mutableListOf<String>()
        for ((id, e) in esperado) {
            val m = mec(cat!!, id)
            if (m.efeito != "buff") erros += "$id: efeito=${m.efeito}"
            if (!m.buffAtributo.equals(e[0] as String, true)) erros += "$id: atributo=${m.buffAtributo}"
            if (m.buffAtributoValor != e[1]) erros += "$id: valor=${m.buffAtributoValor} (esperado ${e[1]})"
            if (m.buffEnergiaPorNivel != e[2]) erros += "$id: energiaPorNivel=${m.buffEnergiaPorNivel} (esperado ${e[2]})"
            if (m.buffMaxNiveis != e[3]) erros += "$id: maxNiveis=${m.buffMaxNiveis} (esperado ${e[3]})"
        }
        // Os "Aumentar X" são de um único uso.
        for (id in listOf("aumentar_forca", "aumentar_destreza", "aumentar_vitalidade"))
            if (!mec(cat!!, id).buffUmUnicoUso) erros += "$id: falta buffUmUnicoUso"
        // Estorvar: −Desloc/−Esquiva, e a classe tem que carregar o R-HT do livro.
        val est = mec(cat!!, "estorvar")
        if (est.buffDeslocamento != -1 || est.buffEsquiva != -1) erros += "estorvar: desloc/esquiva errados"
        if (MagicClassParser.parse(cat[ "estorvar"]!!.classe).resistencia == null)
            erros += "estorvar: classe perdeu o R-HT"
        // Reflexos = +1 a TODAS as defesas ativas = buffBd (não buffEsquiva, que dobraria a esquiva).
        val ref = mec(cat!!, "reflexos")
        if (ref.buffBd != 1 || ref.buffEsquiva != 0) erros += "reflexos: deveria ser buffBd=1 e buffEsquiva=0"
        assertTrue("MAG-1 curadoria regrediu: $erros", erros.isEmpty())
    }

    @Test
    fun `MAG-1 efeito — Forca sobe e Debilitar baixa o ST EFETIVO que o combate le`() {
        val cat = catalogo(); Assume.assumeNotNull(cat)
        // Força: +1 ST por 2 de energia. 4 de energia = +2 ST.
        val g1 = alvo(st = 12)
        g1.buffs.add(MagicMechanics.calcularBuff(mec(cat!!, "forca"), energia = 4, g1.id))
        assertEquals("Força 4 energia = +2 ST", 14, g1.stEfetivo)
        // Debilitar: −1 ST por energia. 3 de energia = −3 ST (reduz o dano do alvo em combate).
        val g2 = alvo(st = 12)
        g2.buffs.add(MagicMechanics.calcularBuff(mec(cat, "debilitar"), energia = 3, g2.id))
        assertEquals("Debilitar 3 energia = −3 ST", 9, g2.stEfetivo)
    }

    @Test
    fun `MAG-1 efeito — Graca sobe e Inabilidade baixa a DX EFETIVA`() {
        val cat = catalogo(); Assume.assumeNotNull(cat)
        // Graça: +1 DX por 4 de energia. 8 = +2 DX.
        val g1 = alvo(dx = 11)
        g1.buffs.add(MagicMechanics.calcularBuff(mec(cat!!, "graca"), energia = 8, g1.id))
        assertEquals("Graça 8 energia = +2 DX", 13, g1.dxEfetivo)
        // Inabilidade: −1 DX por energia. 4 = −4 DX (piora ataque e defesa do alvo).
        val g2 = alvo(dx = 11)
        g2.buffs.add(MagicMechanics.calcularBuff(mec(cat, "inabilidade"), energia = 4, g2.id))
        assertEquals("Inabilidade 4 energia = −4 DX", 7, g2.dxEfetivo)
    }

    @Test
    fun `MAG-1 efeito — teto de niveis respeitado (Debilitar nao passa de menos 5 ST)`() {
        val cat = catalogo(); Assume.assumeNotNull(cat)
        val g = alvo(st = 12)
        // 20 de energia, mas o teto do livro é 5 níveis → no máximo −5 ST.
        g.buffs.add(MagicMechanics.calcularBuff(mec(cat!!, "debilitar"), energia = 20, g.id))
        assertEquals("teto de −5 ST", 7, g.stEfetivo)
    }

    @Test
    fun `MAG-1 efeito — Aumentar Forca e buff de UM UNICO USO`() {
        val cat = catalogo(); Assume.assumeNotNull(cat)
        val buff = MagicMechanics.calcularBuff(mec(cat!!, "aumentar_forca"), energia = 3, "heroi")
        assertEquals("Aumentar Força 3 energia = +3 ST", 3, buff.st)
        assertTrue("tem que ser buff de um único uso", buff.umUnicoUso)
    }

    @Test
    fun `MAG-1 efeito — Reflexos da mais 1 em TODAS as defesas via buffBd`() {
        val cat = catalogo(); Assume.assumeNotNull(cat)
        val buff = MagicMechanics.calcularBuff(mec(cat!!, "reflexos"), energia = 5, "heroi")
        assertEquals("Reflexos = +1 de BD (soma em esquiva/aparar/bloquear)", 1, buff.bd)
        assertEquals("não pode empilhar +1 de esquiva separado (dobraria)", 0, buff.esquiva)
    }

    @Test
    fun `MAG-1 efeito — Estorvar baixa Deslocamento e Esquiva por energia`() {
        val cat = catalogo(); Assume.assumeNotNull(cat)
        val buff = MagicMechanics.calcularBuff(mec(cat!!, "estorvar"), energia = 3, "g")
        assertEquals("−3 Deslocamento", -3, buff.deslocamento)
        assertEquals("−3 Esquiva", -3, buff.esquiva)
    }

    // ═══════════════════════════ MAG-2 — buffs de dano de arma elementais ═════════════════════════

    @Test
    fun `MAG-2 curadoria — os 6 buffs de arma dao +2 de dano com o tipo de arma certo`() {
        val cat = catalogo(); Assume.assumeNotNull(cat)
        // id -> tipo de arma esperado (cac = corpo a corpo, distancia = projetil)
        val esperado = mapOf(
            "arma_flamejante" to "cac", "projeteis_flamejantes" to "distancia",
            "arma_congelante" to "cac", "projeteis_congelantes" to "distancia",
            "arma_de_relampago" to "cac", "projeteis_de_relampago" to "distancia",
        )
        val erros = mutableListOf<String>()
        for ((id, tipo) in esperado) {
            val m = mec(cat!!, id)
            if (m.efeito != "buff") erros += "$id: efeito=${m.efeito}"
            if (m.buffDanoArma != 2) erros += "$id: buffDanoArma=${m.buffDanoArma} (esperado 2)"
            if (m.buffArmaTipo != tipo) erros += "$id: buffArmaTipo=${m.buffArmaTipo} (esperado $tipo)"
        }
        assertTrue("MAG-2 curadoria regrediu: $erros", erros.isEmpty())
    }

    @Test
    fun `MAG-2 efeito — Arma Flamejante so vale corpo a corpo, Projeteis so a distancia`() {
        val cat = catalogo(); Assume.assumeNotNull(cat)
        val cac = MagicMechanics.calcularBuff(mec(cat!!, "arma_flamejante"), energia = 4, "heroi")
        assertEquals("+2 de dano de arma", 2, cac.danoArma)
        assertTrue("Arma Flamejante vale no golpe corpo a corpo", cac.danoArmaVale(aDistancia = false))
        assertTrue("mas NÃO vaza para o ataque à distância", !cac.danoArmaVale(aDistancia = true))

        val dist = MagicMechanics.calcularBuff(mec(cat, "projeteis_flamejantes"), energia = 4, "heroi")
        assertEquals(2, dist.danoArma)
        assertTrue("Projéteis Flamejantes valem à distância", dist.danoArmaVale(aDistancia = true))
        assertTrue("mas NÃO no corpo a corpo", !dist.danoArmaVale(aDistancia = false))
    }

    // ═══════════════ MAG-3 — controle que impõe PARALISADO (resistência pela CLASSE) ══════════════

    private fun heroiComb() = Combatente(
        id = "heroi", nome = "Herói", dx = 12, velocidadeBasica = 6.0, deslocamento = 6, pvMax = 20, pvAtual = 20,
        ehHeroi = true, pfAtual = 30, stats = NpcStats(st = 12, dx = 12, ht = 12, pvMax = 20)
    )

    @Test
    fun `MAG-3 curadoria — 5 magias de controle impoem paralisado e a classe resiste`() {
        val cat = catalogo(); Assume.assumeNotNull(cat)
        // id -> duração em segundos (0 = indefinida, sai pela regra própria)
        val esperado = mapOf(
            "carne_para_pedra" to 0, "soterramento" to 0, "enclausuramento_arboreo" to 0,
            "agonizar" to 60, "cocegas" to 60,
        )
        val erros = mutableListOf<String>()
        for ((id, dur) in esperado) {
            val m = mec(cat!!, id)
            if (m.efeito != "condicao") erros += "$id: efeito=${m.efeito}"
            if (m.condicao != "paralisado") erros += "$id: condicao=${m.condicao}"
            if (m.condicaoDuracaoSeg != dur) erros += "$id: duracao=${m.condicaoDuracaoSeg} (esperado $dur)"
            // A resistência é da CLASSE (o campo condicaoResistencia não é usado aqui) — tem que existir.
            if (MagicClassParser.parse(cat[id]!!.classe).resistencia == null)
                erros += "$id: classe '${cat[id]!!.classe}' perdeu a resistência"
        }
        assertTrue("MAG-3 curadoria regrediu: $erros", erros.isEmpty())
    }

    @Test
    fun `MAG-3 efeito — conjurar Agonizar num alvo nao resistido o deixa PARALISADO`() {
        val cat = catalogo(); Assume.assumeNotNull(cat)
        val m = mec(cat!!, "agonizar")
        val classe = MagicClassParser.parse(cat["agonizar"]!!.classe) // Comum/R-HT
        var paralisouAlgumaVez = false
        for (seed in 0L until 40L) {
            val g = alvo(ht = 11) // HT baixo → às vezes falha em resistir
            val enc = CombatEncounter(listOf(heroiComb(), g), mapOf("g" to 2), seed = seed)
            val s = CombatSession(enc, HeroiPerfilCombate(esquiva = 9, apara = 11, ht = 12, rd = 0), Random(seed))
            val ctx = ContextoConjuracao(nhBasico = 30, classe = classe, mana = NivelMana.NORMAL,
                distanciaMetros = 2, mecanica = m)
            s.heroiConjurar(ctx, MagicEnergy.parse("3"), energiaInvestida = 2, magiaNome = "Agonizar", alvoId = "g")
            if (s.encounter.combatentes.first { it.id == "g" }.condicoes.contains(Condicao.PARALISADO)) {
                paralisouAlgumaVez = true; break
            }
        }
        assertTrue("em 40 seeds, um alvo de HT 11 tem que ficar PARALISADO ao menos uma vez", paralisouAlgumaVez)
    }

    @Test
    fun `MAG-3 efeito — alvo PARALISADO fica indefeso (so pode nao fazer nada)`() {
        val g = alvo().apply { condicoes.add(Condicao.PARALISADO) }
        val enc = CombatEncounter(listOf(heroiComb(), g), mapOf("g" to 2), seed = 1L)
        assertEquals(listOf(Manobra.NAO_FAZER_NADA), enc.manobrasLegais(g))
    }

    // ═══════════════════ MAG-4 — cura que LIMPA condição (removeCondicoes) ════════════════════════

    /** Delegate mínimo, só para exercitar removerCondicoes. */
    private fun delegateVazio(mundo: List<Combatente>) = EfeitosMagicosDelegate(
        log = mutableListOf(), random = Random(1), combatentes = { mundo },
        heroi = { mundo.first { it.ehHeroi } }, heroiHt = { 12 }, heroiVontade = { 12 }, verificarFim = { })

    @Test
    fun `MAG-4 curadoria — as 3 magias de cura limpam a condicao certa`() {
        val cat = catalogo(); Assume.assumeNotNull(cat)
        val esperado = mapOf(
            "cessar_sangramento" to "sangrando", "cessar_paralisia" to "paralisado", "restaurar_visao" to "cego",
        )
        val erros = mutableListOf<String>()
        for ((id, cond) in esperado) {
            val m = mec(cat!!, id)
            if (m.efeito != "cura") erros += "$id: efeito=${m.efeito}"
            if (!m.removeCondicoes.contains(cond)) erros += "$id: removeCondicoes=${m.removeCondicoes} (falta $cond)"
        }
        if (mec(cat!!, "cessar_sangramento").curaAoLimpar != 1) erros += "cessar_sangramento: curaAoLimpar != 1"
        assertTrue("MAG-4 curadoria regrediu: $erros", erros.isEmpty())
    }

    @Test
    fun `MAG-4 efeito — Cessar Sangramento remove SANGRANDO e restaura 1 PV`() {
        val a = alvo().apply { condicoes.add(Condicao.SANGRANDO); sangramentoAtivo = true; pvAtual = 15 }
        val sb = StringBuilder()
        val fez = delegateVazio(listOf(heroiComb(), a)).removerCondicoes(a, listOf("sangrando"), curaPv = 1, sb)
        assertTrue("tem que reportar que curou", fez)
        assertTrue("SANGRANDO removido", !a.condicoes.contains(Condicao.SANGRANDO))
        assertTrue("flag interna de sangramento zerada", !a.sangramentoAtivo)
        assertEquals("+1 PV", 16, a.pvAtual)
    }

    @Test
    fun `MAG-4 efeito — Cessar Paralisia e Restaurar Visao removem PARALISADO e CEGO`() {
        val a = alvo().apply { condicoes.add(Condicao.PARALISADO); condicoes.add(Condicao.CEGO) }
        val d = delegateVazio(listOf(heroiComb(), a))
        d.removerCondicoes(a, listOf("paralisado"), curaPv = 0, StringBuilder())
        assertTrue("PARALISADO removido", !a.condicoes.contains(Condicao.PARALISADO))
        assertTrue("CEGO ainda está lá (Cessar Paralisia não mexe nele)", a.condicoes.contains(Condicao.CEGO))
        d.removerCondicoes(a, listOf("cego"), curaPv = 0, StringBuilder())
        assertTrue("CEGO removido por Restaurar Visão", !a.condicoes.contains(Condicao.CEGO))
    }

    @Test
    fun `MAG-4 integracao — o heroi sangrando conjura Cessar Sangramento em si e sara`() {
        val cat = catalogo(); Assume.assumeNotNull(cat)
        val h = heroiComb().apply { condicoes.add(Condicao.SANGRANDO); sangramentoAtivo = true; pvAtual = 15 }
        val g = alvo()
        val enc = CombatEncounter(listOf(h, g), mapOf("g" to 3), seed = 1L)
        val s = CombatSession(enc, HeroiPerfilCombate(esquiva = 9, apara = 11, ht = 12, rd = 0), Random(1))
        val ctx = ContextoConjuracao(nhBasico = 30, classe = MagicClassParser.parse("Comum"),
            mana = NivelMana.NORMAL, mecanica = mec(cat!!, "cessar_sangramento"))
        val r = s.heroiConjurar(ctx, MagicEnergy.parse("1"), energiaInvestida = 1, magiaNome = "Cessar Sangramento", alvoId = null)
        assertTrue("a conjuração deve ter sucesso", r.sucesso)
        val heroi = s.encounter.combatentes.first { it.ehHeroi }
        assertTrue("herói não sangra mais", !heroi.condicoes.contains(Condicao.SANGRANDO))
        assertEquals("recuperou 1 PV", 16, heroi.pvAtual)
    }

    // ══════════════ MAG-5 — condições novas: Náusea (debuff DX) e REMOVIDO (fora do combate) ══════════

    @Test
    fun `MAG-5 curadoria — Nausear e debuff de DX e as duas de banimento removem do combate`() {
        val cat = catalogo(); Assume.assumeNotNull(cat)
        val erros = mutableListOf<String>()
        val n = mec(cat!!, "nausear")
        if (n.efeito != "buff" || !n.buffAtributo.equals("DX", true) || n.buffAtributoValor != -2)
            erros += "nausear: esperado buff DX −2, veio ${n.efeito}/${n.buffAtributo}/${n.buffAtributoValor}"
        for (id in listOf("viagem_planar_para_outro", "transportar_outro_no_tempo")) {
            val m = mec(cat, id)
            if (m.efeito != "condicao" || m.condicao != "removido") erros += "$id: esperado condicao=removido"
            if (MagicClassParser.parse(cat[id]!!.classe).resistencia == null) erros += "$id: classe perdeu o R-Vont"
        }
        assertTrue("MAG-5 curadoria regrediu: $erros", erros.isEmpty())
    }

    @Test
    fun `MAG-5 efeito — Nausear baixa a DX efetiva do alvo em 2`() {
        val cat = catalogo(); Assume.assumeNotNull(cat)
        val a = alvo(dx = 11)
        a.buffs.add(MagicMechanics.calcularBuff(mec(cat!!, "nausear"), energia = 2, a.id))
        assertEquals("−2 DX (piora ataque e defesa do alvo)", 9, a.dxEfetivo)
    }

    @Test
    fun `MAG-5 efeito — REMOVIDO tira o combatente do combate (vivo falso)`() {
        val g = alvo().apply { condicoes.add(Condicao.REMOVIDO) }
        assertTrue("banido não conta como vivo/ativo", !g.vivo)
        // pvAtual intacto: não está morto, está FORA.
        assertEquals(20, g.pvAtual)
    }

    @Test
    fun `MAG-5 integracao — banir o unico inimigo encerra o combate em VITORIA`() {
        val cat = catalogo(); Assume.assumeNotNull(cat)
        val m = mec(cat!!, "viagem_planar_para_outro")
        val classe = MagicClassParser.parse(cat["viagem_planar_para_outro"]!!.classe) // Comum/R-Vont+1
        var venceuBanindo = false
        for (seed in 0L until 40L) {
            val g = alvo(iq = 8) // Vontade baixa → às vezes não resiste ao banimento
            val enc = CombatEncounter(listOf(heroiComb(), g), mapOf("g" to 2), seed = seed)
            val s = CombatSession(enc, HeroiPerfilCombate(esquiva = 9, apara = 11, ht = 12, rd = 0), Random(seed))
            val ctx = ContextoConjuracao(nhBasico = 30, classe = classe, mana = NivelMana.NORMAL,
                distanciaMetros = 2, mecanica = m)
            s.heroiConjurar(ctx, MagicEnergy.parse("Varia"), energiaInvestida = 3, magiaNome = "Banir", alvoId = "g")
            if (s.encerrado && s.resultado == ResultadoCombate.VITORIA) { venceuBanindo = true; break }
        }
        assertTrue("em 40 seeds, banir o único inimigo (Vont 8) tem que encerrar em VITÓRIA ao menos uma vez", venceuBanindo)
    }
}
