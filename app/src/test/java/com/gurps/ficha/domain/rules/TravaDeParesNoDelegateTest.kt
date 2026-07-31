package com.gurps.ficha.domain.rules

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **A trava de pares está LIGADA nos dois caminhos de adição?** (achado no
 * aparelho em 31/07/2026)
 *
 * ## O bug que este arquivo existe para nunca mais deixar passar
 *
 * O `IncompatibilidadeDeTracos` estava **certo**: simétrico, com 13 pares e 17
 * testes, incluindo uma varredura que provava que todo par recusa nos dois
 * sentidos. E mesmo assim, no aparelho:
 *
 * - com **Reflexos em Combate** na ficha, dava para adicionar **Paralisia
 *   Frente ao Combate** — mas não o contrário;
 * - dava para ter **Oblívio** e **Pouca Empatia** juntos.
 *
 * O motivo: `FichaTraitDelegate.adicionarVantagem` chamava a regra e
 * **`adicionarDesvantagem` não chamava**. Nos pares em que os dois lados são
 * desvantagem, ela nunca era consultada.
 *
 * ## ⚠️ Por que 17 testes verdes não pegaram
 *
 * Porque todos os 17 exercitavam a **regra**, e o defeito estava na **fiação**.
 * Provar que o alarme toca não prova que ele está ligado na tomada — e era
 * exatamente disso que se tratava.
 *
 * ## Por que este teste lê o CÓDIGO-FONTE
 *
 * O caminho natural seria instanciar o `FichaTraitDelegate` e tentar adicionar.
 * Não dá: ele exige um `DataRepository`, que exige `Context` do Android, e o
 * projeto não tem mockito nem robolectric. Criar essa infraestrutura inteira
 * para uma linha de fiação seria caro e, pior, adiaria o conserto.
 *
 * Então o teste faz o que dá para fazer **hoje**: abre o arquivo e exige que
 * **todo** método que adiciona traço consulte a trava, antes de criar o objeto.
 * É um teste de fiação, não de comportamento — e está escrito aqui o que ele
 * prova e o que não prova.
 *
 * ⚠️ **O que ele NÃO prova:** que a mensagem chega na tela, nem que a UI mostra
 * o erro. Isso continua só no aparelho (T-P1 a T-P3 do roteiro).
 */
class TravaDeParesNoDelegateTest {

    private fun fonteDoDelegate(): String {
        val caminho = "src/main/java/com/gurps/ficha/viewmodel/delegates/FichaTraitDelegate.kt"
        val direto = File(caminho)
        val arquivo = if (direto.exists()) direto else File("app/$caminho")
        assertTrue("fonte nao encontrada: ${arquivo.absolutePath}", arquivo.exists())
        return arquivo.readText(Charsets.UTF_8)
    }

    /** O corpo de uma função, do `fun nome(` até a próxima declaração de topo. */
    private fun corpoDe(fonte: String, nome: String): String {
        val inicio = fonte.indexOf("fun $nome(")
        assertTrue("nao achei `fun $nome(`", inicio >= 0)
        val resto = fonte.substring(inicio + 4)
        val fim = resto.indexOf("\n    fun ")
        return if (fim >= 0) resto.substring(0, fim) else resto
    }

    private companion object {
        /**
         * Os métodos que colocam um traço novo na ficha.
         *
         * ⚠️ Crescer esta lista é obrigatório quando nascer um `adicionarX` novo
         * — e é justamente o esquecimento que o teste abaixo persegue.
         */
        val QUE_ADICIONAM = listOf("adicionarVantagem", "adicionarDesvantagem")

        /**
         * Métodos de adição que **não** precisam da trava, com o motivo.
         *
         * A trava casa **id de catálogo** contra id de catálogo. Quem não põe um
         * traço com id na ficha não tem o que checar:
         *
         * - **Qualidade** e **Peculiaridade** recebem `String` de texto livre —
         *   não existe id, não existe par. São os traços de −1 ponto que o
         *   jogador escreve à mão.
         * - **Poder** é **recipiente**, não traço: guarda nome, fonte, foco e o
         *   modificador. Quem aponta para ele é o `poderId` das vantagens, e
         *   essas passam por `adicionarVantagem`, que checa.
         */
        val ISENTOS = mapOf(
            "adicionarQualidade" to "texto livre, sem id de catalogo",
            "adicionarPeculiaridade" to "texto livre, sem id de catalogo",
            "adicionarPoder" to "recipiente, nao traco -- as vantagens dele passam por adicionarVantagem"
        )

        const val CHAMADA = "IncompatibilidadeDeTracos.motivoParaRecusar"
    }

    @Test
    fun `🔴 TODO metodo que adiciona traco consulta a trava de pares`() {
        val fonte = fonteDoDelegate()
        val semTrava = QUE_ADICIONAM.filterNot { corpoDe(fonte, it).contains(CHAMADA) }
        assertTrue(
            "Estes metodos adicionam traco SEM consultar a trava: $semTrava.\n" +
                "Foi exatamente este o bug de 31/07: `adicionarDesvantagem` nao " +
                "chamava, e os pares em que os dois lados sao desvantagem nunca " +
                "eram checados.",
            semTrava.isEmpty()
        )
    }

    @Test
    fun `⚠️ a trava vem ANTES de o traco ser criado`() {
        // Checar depois de criar seria pior que não checar: o objeto já existiria
        // e qualquer `return` esquecido no meio o deixaria entrar. A ordem é o
        // que garante que a recusa acontece antes de qualquer efeito colateral.
        val fonte = fonteDoDelegate()
        QUE_ADICIONAM.forEach { metodo ->
            val corpo = corpoDe(fonte, metodo)
            val ondeChecaste = corpo.indexOf(CHAMADA)
            val ondeCria = corpo.indexOf("dataRepository.criar")
            assertTrue("$metodo: nao achei a criacao do traco", ondeCria >= 0)
            assertTrue(
                "$metodo: a trava e consultada DEPOIS de criar o traco",
                ondeChecaste in 0 until ondeCria
            )
        }
    }

    @Test
    fun `a recusa interrompe a adicao, e nao so registra`() {
        // Um `motivoParaRecusar(...)` cujo resultado fosse ignorado passaria nos
        // dois testes acima e não faria nada. Exige o `return` na mesma frase.
        val fonte = fonteDoDelegate()
        QUE_ADICIONAM.forEach { metodo ->
            val corpo = corpoDe(fonte, metodo)
            val depoisDaChamada = corpo.substring(corpo.indexOf(CHAMADA))
                .take(200)
            assertTrue(
                "$metodo: o motivo da recusa nao vira `return Result.failure`",
                depoisDaChamada.contains("return Result.failure")
            )
        }
    }

    @Test
    fun `nenhum metodo de adicao ficou de fora da lista deste teste`() {
        // 🔴 A cerca contra o próximo esquecimento: se nascer um
        // `adicionarQualidade` ou `adicionarPeculiaridade` que ponha traço na
        // ficha, ele tem de entrar em QUE_ADICIONAM — ou este teste cai.
        val fonte = fonteDoDelegate()
        val encontrados = Regex("""fun (adicionar[A-Za-zÀ-ú]+)\(""")
            .findAll(fonte)
            .map { it.groupValues[1] }
            .filterNot { it.contains("Modificador") }   // mexe em traço existente
            .toSet()
        val naoCobertos = encontrados - QUE_ADICIONAM.toSet() - ISENTOS.keys
        assertTrue(
            "Metodos de adicao fora da cobertura deste teste: $naoCobertos.\n" +
                "Ou eles consultam a trava e entram em QUE_ADICIONAM, ou entram " +
                "em ISENTOS com o motivo escrito.",
            naoCobertos.isEmpty()
        )
        // E o inverso: um isento que SUMIU do arquivo deixa a explicação órfã.
        val isentosFantasma = ISENTOS.keys - encontrados
        assertTrue(
            "ISENTOS cita metodos que nao existem mais: $isentosFantasma",
            isentosFantasma.isEmpty()
        )
    }
}
