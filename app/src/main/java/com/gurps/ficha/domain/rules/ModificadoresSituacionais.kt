package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.traits.BonusCondicional

/**
 * **Os modificadores de situação das perícias** (Lote P-SIT).
 *
 * ## De onde isto veio
 *
 * Cada perícia do livro termina com um rodapé **`Modificadores:`**. O app já
 * guardava esse texto em `pericias_v2_rules_map.json` — 157 perícias, 299
 * números — e **nunca o lia**: a nota do arquivo dizia *"não automatizar nesta
 * etapa"*.
 *
 * Aqui entram os que dependem só da **situação**: nada de traço, nada de
 * equipamento, nada que a ficha saiba sozinha. *"−5 para animal selvagem"*,
 * *"+10 se a vítima estiver dormindo"*. Quem sabe se a situação vale é o
 * jogador, na hora — então cada um vira **caixinha**, o mesmo mecanismo do
 * bônus condicional das vantagens.
 *
 * ## ⚠️ Não são 53 regras: são famílias
 *
 * Olhando de perto, elas se repetem. *"−10 se instantâneo, reduz com o tempo de
 * concentração"* é a mesma frase em **quatro** perícias de chi; *"aparar chutes
 * −2, aparar armas −3"* aparece em **quatro** de luta; *"tipo não familiar −2,
 * más condições −4"* em **nove** de veículo e arma. Os blocos abaixo estão
 * agrupados por família, com os clientes listados — mexer numa é mexer em todas.
 *
 * ## O que ficou de fora, e por quê
 *
 * ⚠️ **Faixas do tipo "−1 a −5"** não entram. O livro deixa o número a critério
 * do Mestre (*"−1 a −5 p/ informações incompletas"*), e oferecer o meio da faixa
 * como se fosse regra seria inventar precisão que o livro não dá. Onde a faixa é
 * o único conteúdo, a perícia não ganha caixinha nenhuma.
 *
 * Kotlin puro e testável. O nome da perícia é o de **`pericias.json`** — o
 * casamento é por nome exato, e "Arco" (do rodapé) é **"Arcos"** no catálogo.
 */
object ModificadoresSituacionais {

    /** Uma situação que o jogador pode marcar, com o número do livro. */
    data class Situacao(val rotulo: String, val valor: Int)

    // ==================================================================
    // Família 1 — "não familiar / em más condições"
    // Nove clientes: veículos e armas pesadas.
    // ==================================================================

    private val NAO_FAMILIAR_E_CONDICOES = listOf(
        Situacao("tipo não familiar", -2),
        Situacao("em más condições", -4)
    )

    // ==================================================================
    // Família 2 — "instantâneo −10, reduz com o tempo de concentração"
    // Quatro clientes, todas as perícias de chi. A frase é idêntica nas quatro.
    // ==================================================================

    private val CHI_INSTANTANEO = listOf(
        Situacao("usado instantaneamente, sem Concentrar", -10)
    )

    // ==================================================================
    // Família 3 — aparar o que a perícia não foi feita para aparar
    // Quatro clientes de luta desarmada (MB p.376).
    // ==================================================================

    private val APARAR_CHUTE_E_ARMA = listOf(
        Situacao("aparando um chute", -2),
        Situacao("aparando uma arma", -3)
    )

    private val APARAR_SO_ARMA = listOf(
        Situacao("aparando uma arma", -3)
    )

    // ==================================================================
    // Família 4 — o alvo distraído
    // ==================================================================

    private val ALVO_DISTRAIDO = listOf(
        Situacao("a vítima está distraída", 5),
        Situacao("a vítima está dormindo ou bêbada", 10)
    )

    private val LUZ_FRACA_OU_DISTRACAO = listOf(
        Situacao("iluminação fraca ou o alvo distraído", 3)
    )

    /**
     * O catálogo, perícia por perícia.
     *
     * Os números saem do rodapé de cada página; a família é só o agrupamento
     * que evita repetir o texto e mantém as irmãs iguais.
     */
    private val POR_PERICIA: Map<String, List<Situacao>> = buildMap {
        // --- Família 1 ---
        listOf(
            "Condução/NT", "Manejo de Barcos/NT", "Submarino/NT",
            "Armas de Fogo/NT", "Armas de Feixe/NT", "Artilharia/NT",
            "Canhoneiro/NT", "Projetor de Líquidos/NT", "Remo/Vela/NT"
        ).forEach { put(it, NAO_FAMILIAR_E_CONDICOES) }

        // --- Família 2 ---
        listOf(
            "Golpe Poderoso", "Pontaria Zen", "Salto Voador", "Arqueiro Zen"
        ).forEach { put(it, CHI_INSTANTANEO) }

        // --- Família 3 ---
        put("Boxe", APARAR_CHUTE_E_ARMA)
        put("Sumô", APARAR_CHUTE_E_ARMA)
        put("Briga", APARAR_SO_ARMA)
        put("Luta Greco-Romana", APARAR_SO_ARMA)

        // --- Família 4 ---
        put("Punga", ALVO_DISTRAIDO)
        put("Surrupiar", LUZ_FRACA_OU_DISTRACAO)
        put("Prestidigitação", LUZ_FRACA_OU_DISTRACAO)

        // --- Avulsas, cada uma com o número da sua página ---
        put("Adestramento de Animais", listOf(
            Situacao("animal não familiarizado ou estressado", -5),
            Situacao("animal selvagem", -5),
            Situacao("animal que ataca seres humanos", -10)
        ))
        put("Veterinária/NT", listOf(
            Situacao("o animal está assustado", -5)
        ))
        put("Carroceiro", listOf(
            Situacao("mais de quatro animais", -2),
            Situacao("animais desconhecidos", -2)
        ))
        put("Furtividade", listOf(
            Situacao("sem esconderijo nenhum por perto", -5),
            Situacao("movendo-se acima de Deslocamento 1", -5)
        ))
        put("Arcos", listOf(
            Situacao("arco composto que ele nunca viu antes", -2)
        ))
        put("Encenação de Combate", listOf(
            Situacao("arma desconhecida", -4)
        ))
        put("Arquitetura/NT", listOf(
            Situacao("tipo de construção desconhecido", -2),
            Situacao("construção alienígena", -5)
        ))
        put("Operação de Computadores/NT", listOf(
            Situacao("sistema ou programa não familiar", -2)
        ))
        put("Programação de Computadores/NT", listOf(
            Situacao("linguagem estranha", -2)
        ))
        put("Fotografia/NT", listOf(
            Situacao("máquina não familiar", -3),
            Situacao("filmando, e não fotografando", -3)
        ))
        // ⚠️ Mergulho está TAMBÉM na lista do seletor de equipamento, e os dois
        // números somam de propósito: o seletor mede a **qualidade** do aparelho
        // (MB p.346) e este mede a **familiaridade** com ele. Um mergulhador com
        // equipamento improvisado E que nunca viu aquele modelo leva os dois.
        // O texto diz "que ele nunca usou" justamente para não parecer repetição
        // do seletor lá em cima.
        put("Mergulho/NT", listOf(
            Situacao("aparelho que ele nunca usou antes", -2)
        ))
        put("Paraquedismo/NT", listOf(
            Situacao("o peso passa de dez vezes a Base de Carga", -2)
        ))
        put("Literatura", listOf(
            Situacao("o personagem é analfabeto", -5)
        ))
        put("Lutar às Cegas", listOf(
            Situacao("o personagem é surdo", -7)
        ))
        put("Zarabatana", listOf(
            Situacao("ataque corpo a corpo com o pó", 2),
            Situacao("está ventando", -2)
        ))
        put("Arremessador de Lança", listOf(
            Situacao("em lugar apertado", -5)
        ))
        put("Arte da Invisibilidade", listOf(
            Situacao("com bomba de fumaça", 3),
            Situacao("completamente imóvel", 1)
        ))
        put("Auto-Hipnose", listOf(
            Situacao("para anular dor ou fadiga", -4),
            Situacao("para aumentar a Vontade", -2)
        ))
        put("Bloqueio Mental", listOf(
            Situacao("concentrando-se só nisso", 2),
            Situacao("atordoado", -3)
        ))
        put("Hipnotismo", listOf(
            Situacao("sugestões por Diapsiquia", 2)
        ))
        put("Composição Musical", listOf(
            Situacao("por grupo de instrumentos secundários", -1)
        ))
        put("Desenho de Símbolos", listOf(
            Situacao("métodos não tradicionais", -1),
            Situacao("superfície inadequada", -1)
        ))
        put("Ventriloquismo", listOf(
            Situacao("com boneco ou distração", 5),
            Situacao("a audiência está desconfiada", -3)
        ))
        put("Rastreamento", listOf(
            Situacao("seguindo um homem só", 3),
            Situacao("seguindo um grupo", 6)
        ))
        put("Passos Leves", listOf(
            Situacao("superfície muito frágil, tipo papel de arroz", -8)
        ))
    }

    /** As situações desta perícia — vazia para a esmagadora maioria. */
    fun de(nomeDaPericia: String): List<Situacao> = POR_PERICIA[nomeDaPericia].orEmpty()

    /** Se vale desenhar caixinha nesta perícia. */
    fun tem(nomeDaPericia: String): Boolean = POR_PERICIA.containsKey(nomeDaPericia)

    /**
     * As caixinhas prontas para a tela.
     *
     * Vira [BonusCondicional] para reusar o `PainelBonusCondicional`, que já
     * existe desde o Lote V-5 — sem inventar componente novo. O "traço" aqui é a
     * própria perícia, porque a situação é dela e não de um traço da ficha.
     */
    fun condicionaisDe(nomeDaPericia: String): List<BonusCondicional> =
        de(nomeDaPericia).map {
            BonusCondicional(
                nomeDoTraco = nomeDaPericia,
                alvo = nomeDaPericia,
                valor = it.valor,
                condicao = it.rotulo
            )
        }

    /** Quantas perícias o lote alcança — usado pelo teste, e bom de saber. */
    val QUANTAS_PERICIAS: Int get() = POR_PERICIA.size
}
