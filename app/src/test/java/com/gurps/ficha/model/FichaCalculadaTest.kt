package com.gurps.ficha.model

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **O bloco `calculado`** — lote CAMPO-16.
 *
 * 🔴 O teste que da nome a este arquivo e o do **envelhecimento**. O perigo real
 * nao e um numero sair errado hoje: e alguem acrescentar um derivado novo ao
 * `Personagem` daqui a seis meses e esquecer de o pôr no bloco. Ele continua a
 * existir, continua certo no que tem, e fica **incompleto sem ninguem perceber**
 * -- a Mesa Virtual passa a receber uma ficha com um buraco.
 *
 * ⚠️ Um teste que so comparasse "o bloco diz 12 e a propriedade diz 12" seria
 * circular: o bloco e feito chamando a propriedade. Compararia a conta consigo
 * mesma e nunca ficaria vermelho.
 */
class FichaCalculadaTest {

    /**
     * As propriedades calculadas do `Personagem` que **de proposito** NAO entram
     * no bloco, e o motivo de cada uma.
     *
     * 🔴 Esta lista e o mecanismo inteiro: acrescentar um derivado novo ao
     * `Personagem` passa a **obrigar uma decisao** -- ou ele entra no bloco, ou
     * entra aqui com um motivo escrito. O esquecimento deixa de ser possivel.
     */
    private val deForaComMotivo = mapOf(
        // -- Listas e passos intermedios: o `character` cru ja os leva --
        "periciasTotais" to "a lista crua ja vai no character; aqui vao os NH",
        "vantagensTotais" to "juntar pessoais e raciais e concatenar lista, nao e regra",
        "desvantagensTotais" to "idem",
        "pesoTotalEquipamentos" to "sai do character, e a Mesa nao pesa nada",
        "baseCarga" to "e um passo intermedio do nivelCarga, que esta no bloco",
        "bonusDeslocamentoAquatico" to "parcela do deslocamentoAquatico",
        "deslocamentoAquatico" to "nadar nao entra no combate em terra (ainda)",
        "modificadorTamanho" to "vem do modeloRacial, que ja vai cru no character",

        // -- A auditoria de pontos por categoria --
        // O bloco leva os TRES numeros do cabecalho (gastos, restantes, total).
        // A conta por categoria fica no app, que tem a tela para a mostrar: a
        // Mesa mostra a ficha, nao a auditoria dela.
        "pontosAtributos" to "auditoria por categoria: e tela do app",
        "pontosSecundarios" to "auditoria por categoria: e tela do app",
        "pontosPericias" to "auditoria por categoria: e tela do app",
        "pontosVantagens" to "auditoria por categoria: e tela do app",
        "pontosDesvantagens" to "auditoria por categoria: e tela do app",
        "pontosMagias" to "auditoria por categoria: e tela do app",
        "pontosPoderes" to "auditoria por categoria: e tela do app",
        "pontosQualidades" to "auditoria por categoria: e tela do app",
        "pontosPeculiaridades" to "auditoria por categoria: e tela do app",
        "pontosTecnicas" to "auditoria por categoria: e tela do app",

        // -- Coisas que so fazem sentido dentro do app --
        "custoTotalEquipamentos" to "e dinheiro; a Mesa nao faz compras",
        "desvantagensExcedemLimite" to "aviso do editor de fichas, nao do tabuleiro",
        "rotuloDePontos" to "texto ja formatado para a tela do app",
        "rotuloDePontosAcessivel" to "texto para o leitor de tela do app"
    )

    private fun umPersonagem() = Personagem(
        nome = "Aria",
        forca = 12,
        destreza = 13,
        inteligencia = 11,
        vitalidade = 12
    )

    // == Os numeros ==================================================

    @Test
    fun `o bloco traz os derivados que o cru nao tem`() {
        val p = umPersonagem()
        val c = FichaCalculada.de(p)

        // Velocidade Basica = (HT + DX) / 4 -- MB p.17. Com HT12 e DX13: 6,25.
        assertEquals(6.25f, c.velocidadeBasica, 0.001f)
        assertEquals(6, c.deslocamentoBasico)
        // PV = ST -- MB p.16.
        assertEquals(12, c.pontosVida)
        // PF = HT.
        assertEquals(12, c.pontosFadiga)
        assertEquals(11, c.vontade)
        assertEquals(11, c.percepcao)
        assertEquals(12, c.st)
        assertEquals(13, c.dx)
    }

    @Test
    fun `a Velocidade Basica NAO e arredondada`() {
        // 🔴 5,25 e 5,50 sao ordens diferentes na iniciativa da mesa (CAMPO-15).
        // Arredondar aqui empataria quem nao empata, e o Mestre teria de
        // desempatar a mao uma luta inteira sem saber porque.
        val p = Personagem(destreza = 11, vitalidade = 10)   // (10+11)/4 = 5,25
        assertEquals(5.25f, FichaCalculada.de(p).velocidadeBasica, 0.001f)

        val q = Personagem(destreza = 12, vitalidade = 10)   // (10+12)/4 = 5,50
        assertEquals(5.5f, FichaCalculada.de(q).velocidadeBasica, 0.001f)
    }

    @Test
    fun `o bloco concorda com as propriedades da tela`() {
        // ⚠️ Este teste sozinho e fraco -- e circular, porque o bloco e feito
        // chamando estas mesmas propriedades. Ele existe so para apanhar um
        // campo LIGADO AO ERRADO: `vontade = personagem.percepcao`, que e um
        // erro de copiar e colar que ninguem le no diff.
        val p = umPersonagem()
        val c = FichaCalculada.de(p)
        assertEquals(p.st, c.st)
        assertEquals(p.dx, c.dx)
        assertEquals(p.iq, c.iq)
        assertEquals(p.ht, c.ht)
        assertEquals(p.pontosVida, c.pontosVida)
        assertEquals(p.pontosFadiga, c.pontosFadiga)
        assertEquals(p.vontade, c.vontade)
        assertEquals(p.percepcao, c.percepcao)
        assertEquals(p.deslocamentoBasico, c.deslocamentoBasico)
        assertEquals(p.deslocamentoAtual, c.deslocamentoAtual)
        assertEquals(p.nivelCarga, c.nivelCarga)
        assertEquals(p.danoGdP, c.danoGdP)
        assertEquals(p.danoGeB, c.danoGeB)
        assertEquals(p.velocidadeBasica, c.velocidadeBasica, 0.001f)
    }

    @Test
    fun `sem pericia de apara e sem escudo, apara e bloqueio sao nulos`() {
        // ⚠️ `null` NAO e zero: zero seria "apara e falha sempre"; o certo e
        // "nao apara". Um zero no lugar faria a Mesa desenhar uma defesa que a
        // pessoa nao tem.
        val c = FichaCalculada.de(umPersonagem())
        assertNull("sem pericia de apara, apara devia ser nulo", c.apara)
        assertNull("sem escudo, bloqueio devia ser nulo", c.bloqueio)
        // A Esquiva, essa, existe sempre.
        assertTrue("a esquiva devia ser um numero de jogo", c.esquiva > 0)
    }

    @Test
    fun `as pericias saem com NH, e nao com pontos gastos`() {
        // 🔴 O JSON cru tem `pontosGastos`. Quem le de fora nao consegue virar
        // isso em NH sem reimplementar a tabela do livro (MB p.170) -- que e
        // exatamente a segunda conta que nao pode existir.
        val p = umPersonagem().apply {
            pericias = listOf(
                PericiaSelecionada(
                    definicaoId = "rastrear",
                    nome = "Rastrear",
                    atributoBase = AtributoBase.IQ,
                    dificuldade = Dificuldade.MEDIA,
                    pontosGastos = 4
                )
            )
        }
        val c = FichaCalculada.de(p)
        assertEquals(1, c.pericias.size)
        val rastrear = c.pericias.first()
        assertEquals("rastrear", rastrear.id)
        assertEquals("Rastrear", rastrear.nome)
        // IQ 11, Media, 4 pontos: IQ+1 = 12 (MB p.170).
        assertEquals(12, rastrear.nh)
    }

    // == O envelope ==================================================

    @Test
    fun `o JSON exportado carrega o bloco calculado`() {
        val json = PersonagemInterop.exportarJson(umPersonagem())
        val root = JsonParser.parseString(json).asJsonObject
        val bloco = root.getAsJsonObject("calculado")
        assertNotNull("o JSON saiu sem o bloco calculado", bloco)
        assertEquals(6.25, bloco.get("velocidadeBasica").asDouble, 0.001)
        assertEquals(12, bloco.get("pontosVida").asInt)
    }

    @Test
    fun `🔴 o bloco calculado do arquivo e IGNORADO ao importar`() {
        // 🔴 Um arquivo mexido a mao poria uma Esquiva 20 aqui, e sem esta
        // garantia o app acreditaria. Pior: ela sobreviveria a tudo, porque nao
        // ha nada nos dados crus que a contradiga.
        val json = PersonagemInterop.exportarJson(umPersonagem())
        val root = JsonParser.parseString(json).asJsonObject
        root.getAsJsonObject("calculado").addProperty("esquiva", 20)
        root.getAsJsonObject("calculado").addProperty("pontosVida", 999)

        val voltou = PersonagemInterop.importarJson(root.toString()).personagem
        assertEquals("o PV do arquivo entrou no personagem", 12, voltou.pontosVida)
        val recalculado = FichaCalculada.de(voltou)
        assertTrue("a Esquiva mentirosa do arquivo entrou", recalculado.esquiva < 20)
    }

    @Test
    fun `uma ficha exportada ANTES deste lote continua a abrir`() {
        // ⚠️ Um arquivo antigo nao tem o bloco. Ele tem de abrir na mesma.
        val json = PersonagemInterop.exportarJson(umPersonagem())
        val root = JsonParser.parseString(json).asJsonObject
        root.remove("calculado")

        val voltou = PersonagemInterop.importarJson(root.toString())
        assertEquals("Aria", voltou.personagem.nome)
        assertEquals(12, voltou.personagem.forca)
    }

    // == O envelhecimento ============================================

    @Test
    fun `🔴 todo derivado do Personagem esta no bloco ou tem motivo escrito`() {
        // 🔴 ESTE e o teste do lote. Ele varre as propriedades CALCULADAS do
        // `Personagem` -- as que nao tem campo por tras -- e cobra que cada uma
        // esteja no bloco ou na lista de exclusoes, com motivo.
        //
        // ⚠️ Sem ele, acrescentar um derivado novo daqui a seis meses deixaria o
        // bloco incompleto em silencio. Com ele, acrescentar um derivado obriga
        // uma decisao: ou entra, ou se escreve por que nao.
        val campos = Personagem::class.java.declaredFields.map { it.name }.toSet()

        val derivados = Personagem::class.java.methods
            .filter { it.parameterCount == 0 && it.name.startsWith("get") && it.name.length > 3 }
            .map { it.name.removePrefix("get").replaceFirstChar { c -> c.lowercaseChar() } }
            .filter { nome ->
                // ⚠️ So o que NAO tem campo por tras e um derivado. O resto sao
                // os getters dos 43 campos crus, que ja viajam no `character`.
                nome !in campos &&
                    // O Kotlin gera acessorios de `copy`/`component` e afins.
                    !nome.startsWith("component") && nome != "class"
            }
            .toSortedSet()

        assertTrue("nao achei derivados nenhuns; a reflexao mudou de forma?",
            derivados.size >= 10)

        val noBloco = FichaCalculada::class.java.declaredFields.map { it.name }.toSet()
        val naoResolvidos = derivados.filter { it !in noBloco && it !in deForaComMotivo }

        assertTrue(
            "Estes derivados do Personagem nao estao no bloco `calculado` nem na " +
                "lista de exclusoes com motivo: $naoResolvidos\n" +
                "Ou acrescente ao FichaCalculada, ou ponha em `deForaComMotivo` " +
                "dizendo por que nao vai.",
            naoResolvidos.isEmpty()
        )
    }

    @Test
    fun `a lista de exclusoes nao guarda nome que ja nao existe`() {
        // ⚠️ O outro lado da mesma moeda: uma exclusao para um derivado que foi
        // apagado vira lixo que ninguem tem coragem de tirar, e comeca a
        // esconder derivados novos com o mesmo nome.
        val derivados = Personagem::class.java.methods
            .filter { it.parameterCount == 0 && it.name.startsWith("get") }
            .map { it.name.removePrefix("get").replaceFirstChar { c -> c.lowercaseChar() } }
            .toSet()
        val orfas = deForaComMotivo.keys.filter { it !in derivados }
        assertTrue("estas exclusoes nao correspondem a derivado nenhum: $orfas",
            orfas.isEmpty())
    }
}
