package com.gurps.ficha.domain.rules

/**
 * **O mapa de toque da silhueta do botão PV** — Lote PV-1a.
 *
 * O jogador toca o corpo em vez de procurar o nome numa lista de onze itens.
 * Este arquivo é a única coisa que sabe **onde cada parte do corpo fica** na
 * arte — e ele é Kotlin puro, sem Android, para poder ser provado por teste.
 *
 * ## 🔴 O mapa é medido, não desenhado no olho
 *
 * Todas as linhas de corte aqui saíram de **medição da arte de verdade**
 * (`res/drawable-nodpi/silhueta_corpo.png`, 591 × 1555). A ferramenta que mede
 * está em `docs/arte/silhueta/mapa_silhueta.py`: ela varre a imagem, acha o
 * pescoço pela largura mínima, a axila pela primeira linha em que o braço se
 * separa do tronco, a virilha pela linha em que a silhueta se parte em duas — e
 * depois **pinta o mapa por cima da arte** para conferência humana.
 *
 * Errar aqui é o pior tipo de erro que este app pode ter: o toque cai no membro
 * errado, a tela continua bonita, e o jogador só descobre três sessões depois
 * com o braço errado decepado na ficha. Por isso o ciclo foi medir → pintar →
 * olhar → corrigir, e ele pegou três erros:
 *
 * 1. Cortar o queixo no ponto mais estreito da cabeça jogava a **boca** para
 *    dentro do pescoço — neste desenho o maxilar tem quase a largura do pescoço.
 * 2. O pescoço ia até a clavícula e engolia o peito inteiro.
 * 3. Os órgãos vitais eram um retângulo saindo para fora do tórax.
 *
 * ## ⚠️ Tocar e pintar são coisas diferentes
 *
 * [regiaoEm] é **só geometria**: ela cobre o retângulo inteiro da tela, sem
 * olhar se o ponto caiu dentro do desenho. É de propósito — a mão tem só 44 dp
 * de largura na arte, e exigir acerto no traço deixaria o alvo **abaixo do
 * mínimo de 48 dp** que o app cobra de qualquer botão. Um toque logo ao lado da
 * mão ainda seleciona a mão, e não rouba de ninguém porque as regiões
 * particionam a tela sem sobra.
 *
 * O **destaque visual**, esse sim, é recortado no canal alfa da imagem — senão o
 * realce viraria um retângulo em volta do braço.
 *
 * ## ⚠️ "Esquerdo" é o lado DELE, não o da tela
 *
 * A figura está de frente para quem olha, então o braço que aparece à esquerda
 * da imagem é o braço **direito** do personagem. Decisão do usuário em 10/08.
 * É o que fica registrado na ficha quando o membro é incapacitado.
 */
object MapaDaSilhueta {

    /** O tamanho da arte. Todo x,y deste arquivo é nessa escala. */
    const val LARGURA = 591
    const val ALTURA = 1555

    enum class Lado(val rotulo: String) { ESQUERDO("esquerdo"), DIREITO("direito") }

    /**
     * As três telas de zoom.
     *
     * 🔴 Os retângulos **não** foram escolhidos: são os recortes que o usuário
     * já tinha desenhado, e o casamento de traço deu **100%** contra o corpo
     * inteiro — são recorte exato 1:1. Por isso o app precisa de **um** arquivo
     * só, e o zoom é mover a janela sobre a mesma imagem, sem troca de asset e
     * sem desalinhamento possível.
     */
    enum class Tela(
        val rotulo: String,
        val x0: Int, val y0: Int, val x1: Int, val y1: Int
    ) {
        CABECA("Cabeça e pescoço", 156, 0, 435, 258),
        TRONCO("Tronco, braços e virilha", 0, 269, 591, 911),
        PERNAS("Pernas e pés", 40, 908, 551, 1555);

        val largura: Int get() = x1 - x0
        val altura: Int get() = y1 - y0
        fun contem(x: Int, y: Int): Boolean = x in x0 until x1 && y in y0 until y1
    }

    data class Regiao(
        val id: String,
        val local: LocalAtaque,
        val lado: Lado?,
        val rotulo: String,
        val tela: Tela
    ) {
        /** O nome que o Mestre ouve: "braço esquerdo", "olho direito", "tronco". */
        val nomeCompleto: String get() = rotulo

        /**
         * O que o leitor de tela fala.
         *
         * ⚠️ **Não repete o estado da caixinha.** Quem monta a lista usa
         * `linhaAlternavel`, que já anuncia "marcada"/"não marcada" sozinho —
         * escrever "selecionado" aqui faria o TalkBack dizer as duas coisas, e
         * uma delas podia estar desatualizada.
         */
        val descricaoAcessivel: String
            get() = "$rotulo. Penalidade de ataque " +
                "${RotuloAcessivel.modificador(local.penalidadeAtaque)}."
    }

    // ── As linhas de corte, todas medidas na arte ────────────────────────
    /** Sobrancelhas em y 85; acima disso é crânio. */
    const val TESTA = 82

    /**
     * ⚠️ **Não** é o ponto mais estreito da cabeça (que é 176).
     *
     * Neste desenho o maxilar tem quase a mesma largura do pescoço, então
     * "onde a cabeça é mais fina" é um péssimo detector de queixo: cortar ali
     * jogava a **boca** (y 168) para dentro do pescoço.
     */
    const val QUEIXO = 192

    /**
     * ⚠️ Os ombros começam a abrir em 235, mas cortar ali deixava o pescoço com
     * **44,7 dp** de altura na tela — abaixo do mínimo de toque de 48.
     */
    const val PESCOCO_FIM = 244

    /** Primeira linha em que o braço se separa do tronco no desenho. */
    const val AXILA = 448

    /** O braço volta a alargar: começa a mão. */
    const val PUNHO = 758

    const val VIRILHA_TOPO = 690

    /** A silhueta se parte em duas pernas. */
    const val VIRILHA_FIM = 789

    const val MAO_FIM = 884
    const val TORNOZELO = 1387

    /** Divisor entre as duas pernas. */
    const val MEIO = 295

    /** Onde a reta braço×tronco é medida abaixo da axila. */
    private const val REF_BAIXO = 829

    // Elipses (cx, cy, rx, ry).
    private val VITAIS = intArrayOf(295, 380, 105, 92)
    private val VIRILHA = intArrayOf(295, 748, 118, 68)

    /**
     * Os olhos, achados por varredura da tinta interna da cabeça — junto com
     * sobrancelhas, orelhas, nariz e boca, que serviram para conferir o resto.
     *
     * ⚠️ O que aparece à **esquerda** da imagem é o olho **direito** dele.
     */
    private val OLHO_DIREITO = intArrayOf(251, 278, 99, 110)
    private val OLHO_ESQUERDO = intArrayOf(312, 339, 99, 110)

    /** O olho é minúsculo na arte; a área de toque cresce em volta dele. */
    const val FOLGA_OLHO = 16

    private fun dentroDaElipse(x: Int, y: Int, e: IntArray): Boolean {
        val dx = (x - e[0]).toDouble() / e[2]
        val dy = (y - e[1]).toDouble() / e[3]
        return dx * dx + dy * dy <= 1.0
    }

    private fun noOlho(x: Int, y: Int, o: IntArray): Boolean =
        x >= o[0] - FOLGA_OLHO && x <= o[1] + FOLGA_OLHO &&
            y >= o[2] - FOLGA_OLHO && y <= o[3] + FOLGA_OLHO

    /**
     * A fronteira entre o braço **direito** dele e o tronco — o que aparece à
     * esquerda da imagem.
     *
     * Entre o ombro e a axila os dois estão colados no desenho, então a
     * fronteira é uma reta; abaixo da axila ela segue o vão real medido.
     */
    fun limiteLadoDireito(y: Int): Double = if (y < AXILA) {
        175 + (156 - 175) * (y - PESCOCO_FIM).toDouble() / (AXILA - PESCOCO_FIM)
    } else {
        156 + (110 - 156) * (y - AXILA).toDouble() / (REF_BAIXO - AXILA)
    }

    /** O braço **esquerdo** dele, que aparece à direita da imagem. */
    fun limiteLadoEsquerdo(y: Int): Double = if (y < AXILA) {
        416 + (433 - 416) * (y - PESCOCO_FIM).toDouble() / (AXILA - PESCOCO_FIM)
    } else {
        433 + (478 - 433) * (y - AXILA).toDouble() / (REF_BAIXO - AXILA)
    }

    /**
     * O id da região naquele ponto da arte. **Só geometria** — ver o aviso no
     * cabeçalho sobre tocar × pintar.
     */
    fun idEm(x: Int, y: Int): String {
        if (y < TESTA) return "CRANIO"
        if (y < QUEIXO) {
            if (noOlho(x, y, OLHO_DIREITO)) return "OLHO_D"
            if (noOlho(x, y, OLHO_ESQUERDO)) return "OLHO_E"
            return "ROSTO"
        }
        if (y < PESCOCO_FIM) return "PESCOCO"
        if (y < VIRILHA_FIM) {
            if (x < limiteLadoDireito(y)) return if (y >= PUNHO) "MAO_D" else "BRACO_D"
            if (x > limiteLadoEsquerdo(y)) return if (y >= PUNHO) "MAO_E" else "BRACO_E"
            if (y >= VIRILHA_TOPO && dentroDaElipse(x, y, VIRILHA)) return "VIRILHA"
            if (dentroDaElipse(x, y, VITAIS)) return "VITAIS"
            return "TRONCO"
        }
        if (y < MAO_FIM) {
            val yy = if (y > REF_BAIXO) REF_BAIXO else y
            if (x < limiteLadoDireito(yy)) return "MAO_D"
            if (x > limiteLadoEsquerdo(yy)) return "MAO_E"
        }
        val lado = if (x < MEIO) "D" else "E"
        return (if (y >= TORNOZELO) "PE_" else "PERNA_") + lado
    }

    fun regiaoEm(x: Int, y: Int): Regiao? = PORid[idEm(x, y)]

    /**
     * Em qual das três telas o toque cai, na silhueta índice.
     *
     * ⚠️ Usa **faixas contíguas**, não os retângulos de recorte. Os recortes têm
     * um vão de 11 px entre a cabeça (termina em 258) e o tronco (começa em 269)
     * — na tela índice isso seria uma faixa morta na base do pescoço, em que o
     * toque não faria nada e o jogador não teria como entender por quê.
     */
    fun telaEm(y: Int): Tela = when {
        y < Tela.TRONCO.y0 -> Tela.CABECA
        y < Tela.PERNAS.y0 + 3 -> Tela.TRONCO
        else -> Tela.PERNAS
    }

    fun de(id: String): Regiao? = PORid[id]

    private fun r(id: String, local: LocalAtaque, lado: Lado?, rotulo: String, tela: Tela) =
        Regiao(id, local, lado, rotulo, tela)

    val REGIOES: List<Regiao> = listOf(
        r("CRANIO", LocalAtaque.CRANIO, null, "Crânio", Tela.CABECA),
        r("ROSTO", LocalAtaque.ROSTO, null, "Rosto", Tela.CABECA),
        r("OLHO_E", LocalAtaque.OLHO, Lado.ESQUERDO, "Olho esquerdo", Tela.CABECA),
        r("OLHO_D", LocalAtaque.OLHO, Lado.DIREITO, "Olho direito", Tela.CABECA),
        r("PESCOCO", LocalAtaque.PESCOCO, null, "Pescoço", Tela.CABECA),
        r("TRONCO", LocalAtaque.TORSO, null, "Tronco", Tela.TRONCO),
        r("VITAIS", LocalAtaque.VITAIS, null, "Órgãos vitais", Tela.TRONCO),
        r("VIRILHA", LocalAtaque.INGLE, null, "Virilha", Tela.TRONCO),
        r("BRACO_E", LocalAtaque.BRACO, Lado.ESQUERDO, "Braço esquerdo", Tela.TRONCO),
        r("BRACO_D", LocalAtaque.BRACO, Lado.DIREITO, "Braço direito", Tela.TRONCO),
        r("MAO_E", LocalAtaque.MAO, Lado.ESQUERDO, "Mão esquerda", Tela.TRONCO),
        r("MAO_D", LocalAtaque.MAO, Lado.DIREITO, "Mão direita", Tela.TRONCO),
        r("PERNA_E", LocalAtaque.PERNA, Lado.ESQUERDO, "Perna esquerda", Tela.PERNAS),
        r("PERNA_D", LocalAtaque.PERNA, Lado.DIREITO, "Perna direita", Tela.PERNAS),
        r("PE_E", LocalAtaque.PE, Lado.ESQUERDO, "Pé esquerdo", Tela.PERNAS),
        r("PE_D", LocalAtaque.PE, Lado.DIREITO, "Pé direito", Tela.PERNAS)
    )

    private val PORid: Map<String, Regiao> = REGIOES.associateBy { it.id }

    // ==================================================================
    // A máscara do corpo
    // ==================================================================

    /**
     * Onde o corpo está desenhado, linha por linha.
     *
     * ## Por que existe um arquivo em vez de olhar o PNG
     *
     * A arte é **desenho de contorno**: o fundo é transparente e o **interior do
     * corpo também**. Então não dá para perguntar "esse pixel é opaco?" — só o
     * traço é. Descobrir o interior exige inundar a imagem a partir das bordas,
     * o que é caro para fazer no celular a cada abertura e impossível no teste,
     * que roda sem AWT.
     *
     * A ferramenta `docs/arte/silhueta/mapa_silhueta.py` faz isso uma vez e grava
     * as faixas em `assets/silhueta_corpo_mascara.txt` (32 KB).
     *
     * ⚠️ O cabeçalho carrega o **sha256 da arte**. Um arquivo derivado pode
     * envelhecer em silêncio — trocam o desenho, esquecem de rodar a ferramenta,
     * e tudo continua verde medindo um corpo que não existe mais. O hash é a
     * única amarra entre os dois.
     */
    class Mascara(
        val largura: Int,
        val altura: Int,
        val sha: String,
        private val faixas: Map<Int, List<IntRange>>
    ) {
        fun dentro(x: Int, y: Int): Boolean = faixas[y]?.any { x in it } == true

        fun faixasDe(y: Int): List<IntRange> = faixas[y].orEmpty()

        /**
         * As faixas horizontais de **uma região** naquela linha — é com isso que
         * a tela monta o destaque, sem precisar de polígono nenhum.
         *
         * Exato por construção: o mesmo [idEm] que decide o toque decide o
         * desenho, então realce e toque não têm como discordar.
         */
        fun faixasDaRegiao(id: String, y: Int): List<IntRange> {
            val saida = mutableListOf<IntRange>()
            faixasDe(y).forEach { faixa ->
                var ini = -1
                for (x in faixa) {
                    if (idEm(x, y) == id) {
                        if (ini < 0) ini = x
                    } else if (ini >= 0) {
                        saida += ini until x
                        ini = -1
                    }
                }
                if (ini >= 0) saida += ini..faixa.last
            }
            return saida
        }
    }

    /**
     * Lê o arquivo da máscara. Recebe as linhas de fora para servir tanto ao app
     * (que lê de `assets`) quanto ao teste (que lê do disco) — uma leitura só,
     * em vez de duas que podem divergir.
     */
    fun lerMascara(linhas: Sequence<String>): Mascara {
        var largura = 0
        var altura = 0
        var sha = ""
        val faixas = HashMap<Int, List<IntRange>>()
        linhas.forEach { linha ->
            when {
                linha.isBlank() || linha.startsWith("#") || linha.startsWith("arte=") -> Unit
                linha.startsWith("largura=") -> largura = linha.substringAfter("=").trim().toInt()
                linha.startsWith("altura=") -> altura = linha.substringAfter("=").trim().toInt()
                linha.startsWith("sha256=") -> sha = linha.substringAfter("=").trim()
                else -> {
                    val y = linha.substringBefore(":").toInt()
                    faixas[y] = linha.substringAfter(":").split(",").map {
                        val a = it.substringBefore("-").toInt()
                        val b = it.substringAfter("-").toInt()
                        a..b
                    }
                }
            }
        }
        return Mascara(largura, altura, sha, faixas)
    }

    const val ARQUIVO_DA_MASCARA = "silhueta_corpo_mascara.txt"

    /**
     * ⚠️ Os locais do livro que **não** entram na silhueta, e por quê.
     *
     * *Braço com escudo* (−4), *mão com escudo* (−8) e *arma do oponente* são
     * penalidades de **acertar** — o escudo atrapalha quem ataca. Depois que o
     * golpe entrou, o dano num braço com escudo segue a regra de braço comum.
     * Eles vivem no `MiraRules`, do lado do ataque, e trazê-los para cá seria
     * oferecer ao jogador uma escolha que não muda nada no ferimento.
     */
    val FORA_DA_SILHUETA = listOf(
        "Braço com escudo", "Mão com escudo", "Arma do oponente"
    )
}
