package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.roll.CriticoRules
import com.gurps.ficha.model.AtributoBase
import com.gurps.ficha.model.DesvantagemSelecionada
import com.gurps.ficha.model.Dificuldade
import com.gurps.ficha.model.PericiaSelecionada
import com.gurps.ficha.model.Personagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Lotes D-MIRA e D-CRIT** — as quatro que conversam com telas prontas, e a que
 * muda o desfecho da rolagem.
 *
 * ## Por que os dois lotes no mesmo arquivo
 *
 * Porque a pergunta que os une é a mesma: **o que o app pode afirmar sozinho, e
 * o que ele tem de perguntar?** Zarolho ele afirma (a penalidade não depende de
 * nada que a ficha não saiba). Assassino Relutante e Sem Um Dedo ele pergunta —
 * o livro condiciona a coisas que não estão na ficha. Completamente Desastrado
 * ele afirma, mas só depois de saber que o teste é de DX.
 *
 * Errar essa divisão é o defeito que este projeto mais persegue: **automação que
 * chuta**.
 */
class DesvantagensDMiraECritTest {

    private fun com(vararg d: DesvantagemSelecionada) =
        Personagem(nome = "T", destreza = 10, desvantagens = d.toList())

    private fun desv(id: String, custo: Int = 0, nome: String = id) =
        DesvantagemSelecionada(definicaoId = id, nome = nome, custoEscolhido = custo)

    // ==================================================================
    // Zarolho (MB p.163)
    // ==================================================================

    @Test
    fun `Zarolho tira 3 do ataque a distancia sem Apontar`() {
        val p = com(desv(ZarolhoRules.ID))
        assertEquals(-3, ZarolhoRules.penalidadeNoAtaque(p, ehADistancia = true, apontou = false))
    }

    @Test
    fun `⚠️ Apontar cancela o menos 3, mas o menos 1 de combate FICA`() {
        // A ressalva do livro está grudada no −3, não na frase inteira: quem
        // Aponta não deixou de ser zarolho, só compensou a profundidade naquele
        // tiro. Cancelar tudo daria de graça a vantagem de enxergar dos dois
        // olhos.
        val p = com(desv(ZarolhoRules.ID))
        assertEquals(-1, ZarolhoRules.penalidadeNoAtaque(p, ehADistancia = true, apontou = true))
    }

    @Test
    fun `⚠️ as duas penalidades NAO se somam num tiro`() {
        // O livro descreve duas CATEGORIAS de situação, não dois redutores
        // empilháveis. Um tiro é −3, e não −1 −3 = −4.
        val p = com(desv(ZarolhoRules.ID))
        val tiro = ZarolhoRules.penalidadeNoAtaque(p, ehADistancia = true, apontou = false)
        assertEquals(-3, tiro)
        assertTrue("nao pode ser -4", tiro != -4)
    }

    @Test
    fun `no corpo a corpo o Zarolho vale 1, com ou sem Apontar`() {
        val p = com(desv(ZarolhoRules.ID))
        assertEquals(-1, ZarolhoRules.penalidadeNoAtaque(p, ehADistancia = false, apontou = false))
        assertEquals(-1, ZarolhoRules.penalidadeNoAtaque(p, ehADistancia = false, apontou = true))
    }

    @Test
    fun `sem Zarolho na ficha, zero em qualquer combinacao`() {
        val p = com()
        listOf(true, false).forEach { dist ->
            listOf(true, false).forEach { ap ->
                assertEquals(0, ZarolhoRules.penalidadeNoAtaque(p, dist, ap))
            }
        }
    }

    @Test
    fun `o rotulo do Zarolho explica de onde vem o numero`() {
        val p = com(desv(ZarolhoRules.ID))
        val comApontar = ZarolhoRules.rotulo(p, ehADistancia = true, apontou = true)
        assertTrue(comApontar, comApontar.contains("Apontar cancelou"))
        val semApontar = ZarolhoRules.rotulo(p, ehADistancia = true, apontou = false)
        assertTrue(semApontar, semApontar.contains("p.163"))
        assertEquals("sem a desvantagem nao ha rotulo", "",
            ZarolhoRules.rotulo(com(), true, false))
    }

    // ==================================================================
    // Assassino Relutante (MB p.153)
    // ==================================================================

    private fun assassino() = com(desv(PacifismoRules.ID, custo = -5, nome = "Pacifismo"))

    @Test
    fun `Assassino Relutante tira 4 vendo o rosto e 2 sem ver`() {
        val p = assassino()
        assertEquals(-4, PacifismoRules.penalidade(p, ataqueLetal = true, veORosto = true))
        assertEquals(-2, PacifismoRules.penalidade(p, ataqueLetal = true, veORosto = false))
    }

    @Test
    fun `⚠️ sem o jogador confirmar o ataque letal, a penalidade e ZERO`() {
        // O livro lista quatro isenções — veículo, coisa que ele não acredita
        // ser pessoa, alvo que não consegue ver, e o combate corporal — e
        // NENHUMA está na ficha. Aplicar −4 em todo ataque transformaria a
        // desvantagem numa penalidade permanente que o livro não dá.
        val p = assassino()
        assertEquals(0, PacifismoRules.penalidade(p, ataqueLetal = false, veORosto = true))
    }

    @Test
    fun `⚠️ so a variante de menos 5 pontos e o Assassino Relutante`() {
        // O catálogo tem UM id `pacifismo` com quatro custos. As outras três
        // variantes são proibições de conduta ("nunca matar"), que não viram
        // modificador nenhum. Ler o id em vez do custo daria −4 para todas.
        listOf(-10, -15, -30).forEach { custo ->
            val outra = com(desv(PacifismoRules.ID, custo = custo, nome = "Pacifismo"))
            assertFalse("custo $custo nao e Assassino Relutante",
                PacifismoRules.ehAssassinoRelutante(outra))
            assertEquals(0, PacifismoRules.penalidade(outra, ataqueLetal = true, veORosto = true))
        }
    }

    @Test
    fun `o Assassino Relutante BLOQUEIA o Apontar, nao penaliza`() {
        // "não pode Apontar" é proibição, não redutor. Deixar a caixinha
        // marcável e ignorar o efeito faria o número mudar e não mudar.
        val p = assassino()
        assertTrue(PacifismoRules.bloqueiaApontar(p, ataqueLetal = true))
        assertFalse("sem ataque letal, Apontar continua liberado",
            PacifismoRules.bloqueiaApontar(p, ataqueLetal = false))
        assertFalse("ficha sem Pacifismo nunca bloqueia",
            PacifismoRules.bloqueiaApontar(com(), ataqueLetal = true))
    }

    @Test
    fun `🔴 com o Apontar bloqueado, o Zarolho NAO consegue cancelar o menos 3`() {
        // A interação que só existe porque as duas foram feitas juntas: um
        // personagem zarolho E assassino relutante, num ataque letal à
        // distância, fica com o −3 inteiro — porque ele não pode Apontar.
        //
        // Se as duas regras vivessem em arquivos separados, o app leria
        // `apontou = true` (a caixinha marcada antes) e daria o desconto.
        val p = Personagem(
            nome = "T",
            desvantagens = listOf(
                desv(ZarolhoRules.ID),
                desv(PacifismoRules.ID, custo = -5, nome = "Pacifismo")
            )
        )
        val apontarBloqueado = PacifismoRules.bloqueiaApontar(p, ataqueLetal = true)
        val apontouValendo = true && !apontarBloqueado
        assertFalse("o Apontar tem de estar bloqueado", apontouValendo)
        assertEquals(
            "o -3 fica inteiro", -3,
            ZarolhoRules.penalidadeNoAtaque(p, ehADistancia = true, apontou = apontouValendo)
        )
        // E o ataque ainda leva o −4 do Pacifismo por cima: são coisas
        // diferentes, e aí sim somam.
        assertEquals(-4, PacifismoRules.penalidade(p, ataqueLetal = true, veORosto = true))
    }

    // ==================================================================
    // Sem Um Dedo (MB p.157)
    // ==================================================================

    @Test
    fun `Sem Um Dedo tira 1 e Sem o Polegar tira 5`() {
        val dedo = com(desv(SemUmDedoRules.ID, custo = -2))
        val polegar = com(desv(SemUmDedoRules.ID, custo = -5))
        assertEquals(-1, SemUmDedoRules.penalidadeDe(dedo, ehAMaoAfetada = true))
        assertEquals(-5, SemUmDedoRules.penalidadeDe(polegar, ehAMaoAfetada = true))
    }

    @Test
    fun `⚠️ le o CUSTO, nao o nivel - senao o polegar viraria menos 1`() {
        // O catálogo guarda as duas versões como degraus de custo (−2 e −5), e
        // as duas ficam com nível 1. Ler o nível daria −1 para quem pagou pelo
        // polegar, que é a versão cinco vezes pior.
        val polegar = com(desv(SemUmDedoRules.ID, custo = -5))
        assertEquals(1, polegar.desvantagens.single().nivel)
        assertEquals(-5, SemUmDedoRules.penalidadeDe(polegar, ehAMaoAfetada = true))
    }

    @Test
    fun `⚠️ na OUTRA mao a penalidade e zero - o livro diz somente`() {
        // "penalidade de −1 na DX da mão em questão (somente)". A ficha não
        // guarda QUAL mão, então quem responde é o jogador. Assumir que é sempre
        // a inábil daria de graça a versão barata da desvantagem.
        val polegar = com(desv(SemUmDedoRules.ID, custo = -5))
        assertEquals(0, SemUmDedoRules.penalidadeDe(polegar, ehAMaoAfetada = false))
    }

    @Test
    fun `o rotulo diz o que falta, nao so o numero`() {
        val polegar = com(desv(SemUmDedoRules.ID, custo = -5))
        assertTrue(SemUmDedoRules.rotuloDe(polegar).contains("o polegar"))
        val dedo = com(desv(SemUmDedoRules.ID, custo = -2))
        assertTrue(SemUmDedoRules.rotuloDe(dedo).contains("um dedo"))
    }

    @Test
    fun `Sem Um Dedo SOMA com a mao inabil - sao coisas diferentes`() {
        // Usar a mão inábil que também perdeu o polegar é −4 −5 = −9. Nenhuma
        // das duas anula a outra.
        val p = com(desv(SemUmDedoRules.ID, custo = -5))
        val total = MaoInabilRules.penalidadeDe(p, usandoMaoInabil = true) +
            SemUmDedoRules.penalidadeDe(p, ehAMaoAfetada = true)
        assertEquals(-9, total)
    }

    // ==================================================================
    // Invertebrado (MB p.148)
    // ==================================================================

    @Test
    fun `Invertebrado carrega UM QUARTO da Base de Carga`() {
        val p = Personagem(
            nome = "T", forca = 10,
            desvantagens = listOf(desv(DeslocamentosRules.ID_INVERTEBRADO))
        )
        val cheia = p.baseCarga
        assertEquals(cheia / 4f, DeslocamentosRules.baseDeCargaEfetiva(p))
        assertTrue("sem Invertebrado a BC e inteira",
            DeslocamentosRules.baseDeCargaEfetiva(Personagem(nome = "T", forca = 10)) == cheia)
    }

    @Test
    fun `a tabela de carga do Invertebrado usa os tetos reduzidos`() {
        // 🔴 Sem isto o personagem carregava QUATRO VEZES mais do que deveria —
        // e é o tipo de erro que ninguém confere, porque o número parece normal.
        val normal = Personagem(nome = "T", forca = 10)
        val mole = Personagem(
            nome = "T", forca = 10,
            desvantagens = listOf(desv(DeslocamentosRules.ID_INVERTEBRADO))
        )
        val tetoNormal = DeslocamentosRules.tabelaDeCarga(normal).first().conta
        val tetoMole = DeslocamentosRules.tabelaDeCarga(mole).first().conta
        assertTrue("os tetos tem de ser diferentes", tetoNormal != tetoMole)
    }

    @Test
    fun `o resumo avisa que a BC de EMPURRAR continua inteira`() {
        // Sem a nota, o jogador vê uma BC que não bate com a ST dele e acha que
        // o app errou a conta.
        val mole = Personagem(
            nome = "T", forca = 10,
            desvantagens = listOf(desv(DeslocamentosRules.ID_INVERTEBRADO))
        )
        val resumo = DeslocamentosRules.resumoDaCarga(mole)
        assertTrue(resumo, resumo.contains("Invertebrado"))
        assertTrue(resumo, resumo.contains("empurrar"))
        assertFalse("ficha normal nao ganha a nota",
            DeslocamentosRules.resumoDaCarga(Personagem(nome = "T")).contains("Invertebrado"))
    }

    // ==================================================================
    // D-CRIT — Completamente Desastrado (MB p.133)
    // ==================================================================

    private fun desastrado() = com(desv(DesastradoRules.ID, nome = "Completamente Desastrado"))

    private fun classificar(p: Personagem, ehDX: Boolean, soma: Int, alvo: Int?) =
        DesastradoRules.reclassificar(
            p, ehDX, CriticoRules.classificar(soma, alvo), soma, alvo
        )

    @Test
    fun `qualquer fracasso em DX vira falha critica`() {
        // NH 12, rolou 13: falha comum por 1 para qualquer um. Para ele, crítica.
        assertEquals(
            CriticoRules.ResultadoCritico.FALHA_CRITICA,
            classificar(desastrado(), ehDX = true, soma = 13, alvo = 12)
        )
    }

    @Test
    fun `⚠️ fracasso que NAO e de DX continua fracasso comum`() {
        // Um fracasso em Teologia (IQ) ou Corrida (HT) não é assunto desta
        // desvantagem. Reclassificar tudo dobraria o tamanho dela.
        assertEquals(
            CriticoRules.ResultadoCritico.NORMAL,
            classificar(desastrado(), ehDX = false, soma = 13, alvo = 12)
        )
    }

    @Test
    fun `⚠️ SUCESSO em DX nao vira nada - nem o decisivo`() {
        // A desvantagem não tem poder de estragar acerto.
        assertEquals(
            CriticoRules.ResultadoCritico.NORMAL,
            classificar(desastrado(), ehDX = true, soma = 10, alvo = 12)
        )
        assertEquals(
            CriticoRules.ResultadoCritico.DECISIVO,
            classificar(desastrado(), ehDX = true, soma = 3, alvo = 12)
        )
    }

    @Test
    fun `sem a desvantagem nada muda, nem em DX`() {
        assertEquals(
            CriticoRules.ResultadoCritico.NORMAL,
            classificar(com(), ehDX = true, soma = 13, alvo = 12)
        )
    }

    @Test
    fun `⚠️ o Desastrado de menos 5 pontos NAO conta`() {
        // "O Mestre tem que ser criativo para inventar desastres menores" — o
        // nível barato não tem número, e automatizá-lo seria inventar regra.
        val leve = com(desv("desastrado", nome = "Desastrado"))
        assertFalse(DesastradoRules.ativo(leve))
        assertEquals(
            CriticoRules.ResultadoCritico.NORMAL,
            classificar(leve, ehDX = true, soma = 13, alvo = 12)
        )
    }

    @Test
    fun `rolagem sem alvo nao tem fracasso para reclassificar`() {
        assertEquals(
            CriticoRules.ResultadoCritico.NORMAL,
            classificar(desastrado(), ehDX = true, soma = 13, alvo = null)
        )
    }

    @Test
    fun `o aviso so aparece quando o NUMERO sozinho nao explicaria`() {
        val p = desastrado()
        // 13 contra NH 12: falha comum para qualquer um — o aviso é necessário.
        assertTrue(DesastradoRules.explicaOResultado(p, ehBaseDX = true, soma = 13, alvoEfetivo = 12))
        // 18: já é falha crítica para todo mundo. Avisar ali seria ruído.
        assertFalse(DesastradoRules.explicaOResultado(p, ehBaseDX = true, soma = 18, alvoEfetivo = 12))
        // Sucesso: nada a explicar.
        assertFalse(DesastradoRules.explicaOResultado(p, ehBaseDX = true, soma = 8, alvoEfetivo = 12))
        // Sem a desvantagem: nunca.
        assertFalse(DesastradoRules.explicaOResultado(com(), ehBaseDX = true, soma = 13, alvoEfetivo = 12))
    }

    @Test
    fun `o motivo cita a pagina, para o Mestre nao achar que e bug`() {
        assertTrue(DesastradoRules.MOTIVO.contains("p.133"))
        assertTrue(DesastradoRules.MOTIVO.contains("DX"))
    }

    @Test
    fun `os rotulos de base DX saem da ficha, com especializacao junto`() {
        // 🔴 A armadilha do lote: se o rótulo fosse montado aqui em vez de vir da
        // UI, "Faca (Arremesso)" nunca casaria com o rótulo do diálogo e a regra
        // falharia calada para toda perícia especializada.
        val pericias = listOf(
            PericiaSelecionada(
                definicaoId = "faca", nome = "Faca", especializacao = "Arremesso",
                atributoBase = AtributoBase.DX, dificuldade = Dificuldade.FACIL, pontosGastos = 1
            ),
            PericiaSelecionada(
                definicaoId = "teologia", nome = "Teologia",
                atributoBase = AtributoBase.IQ, dificuldade = Dificuldade.DIFICIL, pontosGastos = 4
            )
        )
        val rotuloDe = { p: PericiaSelecionada ->
            if (p.especializacao.isBlank()) p.nome else "${p.nome} (${p.especializacao})"
        }
        val alcance = DesastradoRules.rotulosDeBaseDX(pericias, rotuloDe)
        assertEquals(setOf("Faca (Arremesso)"), alcance)
        assertFalse("Teologia e de IQ", "Teologia" in alcance)
    }
}
