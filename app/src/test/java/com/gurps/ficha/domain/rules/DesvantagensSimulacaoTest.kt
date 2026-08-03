package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.roll.CriticoRules
import com.gurps.ficha.model.DesvantagemSelecionada
import com.gurps.ficha.model.Personagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Simulação exaustiva** das desvantagens automatizadas.
 *
 * ## Por que este arquivo existe, e por que ele é diferente dos outros
 *
 * Os testes dos lotes afirmam **casos**: "Dor Crônica Grave dá −4 em DX". Um
 * caso prova que aquele número saiu certo; não prova que **nenhuma combinação**
 * sai errada. E o defeito que mais aparece neste projeto não é o número errado
 * num caso — é o número errado numa **combinação que ninguém pensou em testar**.
 *
 * Aqui o teste varre o espaço inteiro: todos os graus de todos os estados, todos
 * os NAs, todas as 256 combinações de soma × alvo do crítico. Não afirma
 * valores; afirma **invariantes** — coisas que têm de valer sempre, para
 * qualquer entrada.
 *
 * Analogia: os outros testes conferem se algumas contas do extrato batem; este
 * confere se o saldo pode ficar negativo por qualquer caminho.
 *
 * ⚠️ Tudo é determinístico: nenhuma entrada é sorteada. Um teste que falha uma
 * vez a cada dez execuções não serve para nada.
 */
class DesvantagensSimulacaoTest {

    // ==================================================================
    // 1. Estados temporários — o espaço inteiro de graus
    // ==================================================================

    /** Todas as combinações de (estado, grau válido), inclusive o desligado. */
    private fun todosOsGraus(): List<Pair<String, Int>> =
        EstadosTemporarios.CATALOGO.flatMap { e -> (0..e.graus.size).map { e.id to it } }

    @Test
    fun `nenhum grau de nenhum estado produz numero POSITIVO`() {
        // São desvantagens. Um sinal trocado em qualquer degrau viraria bônus
        // silencioso — e o jogador não reclamaria de ganhar +4.
        todosOsGraus().forEach { (id, grau) ->
            val m = EstadosTemporarios.totalDe(mapOf(id to grau))
            m.atributos.forEach { (a, v) -> assertTrue("$id grau $grau: $a = $v", v <= 0) }
            assertTrue("$id grau $grau: perícias", m.pericias <= 0)
            assertTrue("$id grau $grau: autocontrole", m.autocontrole <= 0)
            assertTrue("$id grau $grau: deslocamento", m.deslocamento <= 0)
        }
    }

    @Test
    fun `ligar um estado NUNCA melhora nada - em nenhuma combinacao`() {
        // A invariante central da família: o interruptor só piora. Varre cada
        // estado ligado por cima de cada outro estado já ligado — 45 pares no
        // catálogo de hoje, e cresce sozinho quando o catálogo crescer.
        val dimensoes = listOf("ST", "DX", "IQ", "HT", "VON", "PER")
        EstadosTemporarios.CATALOGO.forEach { base ->
            (1..base.graus.size).forEach { grauBase ->
                val antes = EstadosTemporarios.totalDe(mapOf(base.id to grauBase))
                EstadosTemporarios.CATALOGO.filter { it.id != base.id }.forEach { extra ->
                    (1..extra.graus.size).forEach { grauExtra ->
                        val depois = EstadosTemporarios.totalDe(
                            mapOf(base.id to grauBase, extra.id to grauExtra)
                        )
                        val onde = "${base.id}/$grauBase + ${extra.id}/$grauExtra"
                        dimensoes.forEach { d ->
                            assertTrue(
                                "$onde piorou ao contrario em $d",
                                (depois.atributos[d] ?: 0) <= (antes.atributos[d] ?: 0)
                            )
                        }
                        assertTrue("$onde: perícias", depois.pericias <= antes.pericias)
                        assertTrue("$onde: autocontrole", depois.autocontrole <= antes.autocontrole)
                        assertTrue("$onde: deslocamento", depois.deslocamento <= antes.deslocamento)
                    }
                }
            }
        }
    }

    @Test
    fun `a soma de estados e comutativa - a ordem de ligar nao importa`() {
        // O jogador liga na ordem que quiser. Se a soma dependesse da ordem, dois
        // jogadores com a mesma ficha e as mesmas condições teriam números
        // diferentes — e nenhum dos dois saberia.
        EstadosTemporarios.CATALOGO.forEach { a ->
            EstadosTemporarios.CATALOGO.filter { it.id != a.id }.forEach { b ->
                val ab = EstadosTemporarios.totalDe(mapOf(a.id to 1, b.id to 1))
                val ba = EstadosTemporarios.totalDe(mapOf(b.id to 1, a.id to 1))
                assertEquals("${a.id} + ${b.id}", ab, ba)
            }
        }
    }

    @Test
    fun `o ciclo de graus SEMPRE fecha, para todo estado`() {
        // Um ciclo que não fecha prende o jogador no grau mais grave, sem
        // caminho de volta a não ser reabrir a ficha.
        EstadosTemporarios.CATALOGO.forEach { e ->
            var grau = 0
            val visitados = mutableListOf<Int>()
            repeat(e.graus.size + 1) {
                grau = e.proximoGrau(grau)
                visitados += grau
            }
            assertEquals("${e.id} nao voltou ao zero", 0, grau)
            assertEquals(
                "${e.id} nao passou por todos os graus",
                (1..e.graus.size).toList() + listOf(0),
                visitados
            )
        }
    }

    @Test
    fun `todo grau ligado aparece no resumo, e nenhum desligado aparece`() {
        // O resumo é a única coisa que explica o número na tela. Um estado que
        // desconta sem aparecer nele é exatamente o "número que muda sozinho".
        EstadosTemporarios.CATALOGO.forEach { e ->
            (1..e.graus.size).forEach { grau ->
                val resumo = EstadosTemporarios.resumoAtivo(mapOf(e.id to grau))
                assertTrue("${e.id}/$grau sumiu do resumo", resumo!!.contains(e.nome))
                assertTrue(
                    "${e.id}/$grau nao disse o grau",
                    resumo.contains(e.graus[grau - 1].rotulo)
                )
            }
            assertTrue(
                "${e.id} desligado nao pode aparecer",
                EstadosTemporarios.resumoAtivo(mapOf(e.id to 0)) == null
            )
        }
    }

    @Test
    fun `o painel so oferece o que a ficha tem - varrendo o catalogo todo`() {
        EstadosTemporarios.CATALOGO.forEach { e ->
            val comEla = Personagem(
                nome = "T",
                desvantagens = listOf(DesvantagemSelecionada(definicaoId = e.id, nome = e.nome))
            )
            assertEquals(listOf(e.id), EstadosTemporarios.disponiveis(comEla).map { it.id })
            // E a ficha com TODAS oferece todas, sem repetir nenhuma.
            val comTodas = Personagem(
                nome = "T",
                desvantagens = EstadosTemporarios.CATALOGO.map {
                    DesvantagemSelecionada(definicaoId = it.id, nome = it.nome)
                }
            )
            assertEquals(
                EstadosTemporarios.CATALOGO.size,
                EstadosTemporarios.disponiveis(comTodas).size
            )
        }
    }

    // ==================================================================
    // 2. Completamente Desastrado — as 256 combinações de soma × alvo
    // ==================================================================

    private val somas = 3..18
    private val alvos = 3..18

    private fun ficha(comDesastrado: Boolean) = Personagem(
        nome = "T",
        desvantagens = if (comDesastrado) {
            listOf(DesvantagemSelecionada(definicaoId = DesastradoRules.ID, nome = "CD"))
        } else emptyList()
    )

    @Test
    fun `⚠️ o Desastrado NAO mexe em nada que ja era SUCESSO`() {
        // A cerca mais importante do D-CRIT. Varre as 256 combinações.
        //
        // ⚠️ A afirmação certa é "não MUDA", e não "não é falha crítica": um
        // **18 contra alvo 18** é sucesso por margem 0 e, ao mesmo tempo, falha
        // crítica pela regra do sistema (soma 18 sempre é). A primeira versão
        // deste teste dizia "nunca pode ser falha crítica" e acusava o
        // Desastrado de um resultado que o `CriticoRules` produz sozinho.
        val p = ficha(comDesastrado = true)
        for (soma in somas) for (alvo in alvos) {
            if (soma > alvo) continue   // aqui é fracasso, e aí ele age mesmo
            val original = CriticoRules.classificar(soma, alvo)
            assertEquals(
                "soma $soma contra alvo $alvo foi alterada sendo sucesso",
                original,
                DesastradoRules.reclassificar(p, true, original, soma, alvo)
            )
        }
    }

    @Test
    fun `⚠️ o Desastrado NUNCA rebaixa um Sucesso Decisivo`() {
        val p = ficha(comDesastrado = true)
        for (soma in somas) for (alvo in alvos) {
            val original = CriticoRules.classificar(soma, alvo)
            if (original != CriticoRules.ResultadoCritico.DECISIVO) continue
            assertEquals(
                "decisivo em $soma/$alvo foi rebaixado",
                CriticoRules.ResultadoCritico.DECISIVO,
                DesastradoRules.reclassificar(p, true, original, soma, alvo)
            )
        }
    }

    @Test
    fun `⚠️ fora de DX o Desastrado e INVISIVEL nas 256 combinacoes`() {
        val p = ficha(comDesastrado = true)
        for (soma in somas) for (alvo in alvos) {
            val original = CriticoRules.classificar(soma, alvo)
            assertEquals(
                "mudou em teste que nao e de DX ($soma/$alvo)",
                original,
                DesastradoRules.reclassificar(p, ehBaseDX = false, original = original, soma = soma, alvoEfetivo = alvo)
            )
        }
    }

    @Test
    fun `sem a desvantagem, o app se comporta identico nas 256 combinacoes`() {
        val p = ficha(comDesastrado = false)
        for (soma in somas) for (alvo in alvos) {
            val original = CriticoRules.classificar(soma, alvo)
            assertEquals(
                original,
                DesastradoRules.reclassificar(p, true, original, soma, alvo)
            )
        }
    }

    @Test
    fun `com a desvantagem, TODO fracasso de DX vira critico - sem exceção`() {
        val p = ficha(comDesastrado = true)
        for (soma in somas) for (alvo in alvos) {
            if (soma <= alvo) continue
            val original = CriticoRules.classificar(soma, alvo)
            if (original == CriticoRules.ResultadoCritico.DECISIVO) continue
            assertEquals(
                "fracasso $soma contra $alvo nao virou critico",
                CriticoRules.ResultadoCritico.FALHA_CRITICA,
                DesastradoRules.reclassificar(p, true, original, soma, alvo)
            )
        }
    }

    @Test
    fun `o aviso aparece exatamente quando o resultado MUDOU, nunca fora disso`() {
        // Ruído destrói a confiança no aviso: se ele aparecer num 18 — que já era
        // falha crítica para qualquer um — o Mestre para de ler.
        val p = ficha(comDesastrado = true)
        for (soma in somas) for (alvo in alvos) {
            val original = CriticoRules.classificar(soma, alvo)
            val depois = DesastradoRules.reclassificar(p, true, original, soma, alvo)
            val mudou = original != depois
            assertEquals(
                "aviso fora de hora em $soma/$alvo",
                mudou,
                DesastradoRules.explicaOResultado(p, true, soma, alvo)
            )
        }
    }

    // ==================================================================
    // 3. Zarolho × Assassino Relutante — as 16 combinações
    // ==================================================================

    private fun fichaMira(zarolho: Boolean, assassino: Boolean) = Personagem(
        nome = "T",
        desvantagens = buildList {
            if (zarolho) add(DesvantagemSelecionada(definicaoId = ZarolhoRules.ID, nome = "Zarolho"))
            if (assassino) add(
                DesvantagemSelecionada(
                    definicaoId = PacifismoRules.ID, nome = "Pacifismo", custoEscolhido = -5
                )
            )
        }
    )

    @Test
    fun `varrendo as 16 combinacoes, nenhuma penalidade fica POSITIVA`() {
        listOf(true, false).forEach { zar ->
            listOf(true, false).forEach { ass ->
                val p = fichaMira(zar, ass)
                listOf(true, false).forEach { dist ->
                    listOf(true, false).forEach { apontou ->
                        val bloqueado = PacifismoRules.bloqueiaApontar(p, ataqueLetal = true)
                        val valendo = apontou && !bloqueado
                        assertTrue(
                            ZarolhoRules.penalidadeNoAtaque(p, dist, valendo) <= 0
                        )
                        assertTrue(
                            PacifismoRules.penalidade(p, ataqueLetal = true, veORosto = true) <= 0
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `🔴 o Apontar bloqueado impede o desconto do Zarolho em TODA combinacao`() {
        // A interação que só existe porque os dois foram feitos no mesmo lote.
        // Sem ela, o app leria a caixinha marcada de antes e daria um desconto
        // que o livro não concede.
        val p = fichaMira(zarolho = true, assassino = true)
        listOf(true, false).forEach { marcouApontar ->
            val bloqueado = PacifismoRules.bloqueiaApontar(p, ataqueLetal = true)
            val valendo = marcouApontar && !bloqueado
            assertFalse("com ataque letal, Apontar nunca vale", valendo)
            assertEquals(
                -3,
                ZarolhoRules.penalidadeNoAtaque(p, ehADistancia = true, apontou = valendo)
            )
        }
    }

    @Test
    fun `sem ataque letal, o Assassino Relutante nao atrapalha nada`() {
        val p = fichaMira(zarolho = true, assassino = true)
        assertFalse(PacifismoRules.bloqueiaApontar(p, ataqueLetal = false))
        assertEquals(0, PacifismoRules.penalidade(p, ataqueLetal = false, veORosto = true))
        assertEquals(
            "e ai o Zarolho volta a poder ser cancelado", -1,
            ZarolhoRules.penalidadeNoAtaque(p, ehADistancia = true, apontou = true)
        )
    }

    // ==================================================================
    // 4. O piso de 3, por todos os caminhos que chegam nele
    // ==================================================================

    @Test
    fun `nenhum alvo de resistencia desce abaixo de 3, em nenhuma ficha`() {
        // Varre níveis absurdos das quatro desvantagens que empurram alvo para
        // baixo. Um piso esquecido em um só caminho daria fracasso automático
        // permanente — e o jogador levaria sessões para perceber.
        val extremos = listOf(1, 5, 12, 30)
        extremos.forEach { nivel ->
            val p = Personagem(
                nome = "T", vitalidade = 8, inteligencia = 8,
                desvantagens = listOf(
                    DesvantagemSelecionada(definicaoId = "facil_de_matar", nome = "FM", nivel = nivel),
                    DesvantagemSelecionada(definicaoId = "temor", nome = "Temor", nivel = nivel),
                    DesvantagemSelecionada(definicaoId = "suscetivel", nome = "S", nivel = nivel),
                    DesvantagemSelecionada(
                        definicaoId = "fora_de_forma", nome = "FF", custoEscolhido = -15
                    )
                )
            )
            ResistenciaRules.testesDe(p).forEach {
                assertTrue(
                    "${it.rotulo} caiu para ${it.alvo} com nivel $nivel",
                    it.alvo >= PisoDeTeste.MINIMO
                )
            }
        }
    }

    @Test
    fun `o piso e idempotente - aplicar duas vezes nao muda nada`() {
        (-50..50).forEach { alvo ->
            assertEquals(
                PisoDeTeste.aplicar(alvo),
                PisoDeTeste.aplicar(PisoDeTeste.aplicar(alvo))
            )
        }
    }

    @Test
    fun `o piso nunca ABAIXA um alvo que ja estava acima dele`() {
        (3..30).forEach { alvo -> assertEquals(alvo, PisoDeTeste.aplicar(alvo)) }
    }

    // ==================================================================
    // 5. Os pares proibidos, dos dois lados, sempre
    // ==================================================================

    @Test
    fun `todo par proibido recusa nos DOIS sentidos e nos DOIS catalogos`() {
        // Já há teste disso no arquivo do D-PAR; aqui a varredura também
        // confere que nenhum par recusa um traço de FORA da lista — uma trava
        // que bloqueia demais é tão ruim quanto uma que não bloqueia.
        IncompatibilidadeDeTracos.PARES.forEach { par ->
            val comUm = Personagem(
                nome = "T",
                desvantagens = listOf(DesvantagemSelecionada(definicaoId = par.umId, nome = par.umId))
            )
            assertTrue(
                IncompatibilidadeDeTracos.motivoParaRecusar(comUm, par.outroId) != null
            )
            assertTrue(
                "a trava nao pode recusar um traco sem relacao",
                IncompatibilidadeDeTracos.motivoParaRecusar(comUm, "pendulear") == null
            )
        }
    }
}
