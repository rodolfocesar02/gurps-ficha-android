package com.gurps.ficha.ui.features.rolagem

import com.gurps.ficha.data.network.DiscordRollPayload
import com.gurps.ficha.model.PericiaSelecionada

const val CUSTOM_ROLL_RETENTION_MS = 5 * 60 * 1000L

enum class TipoTeste(val label: String) {
    ATRIBUTO("Atributo"),
    ATAQUE("Ataque"),
    PERICIA("Pericia"),
    TECNICA("Tecnica"),
    MAGIA("Magia"),
    DEFESA("Defesa"),
    LIVRE("Livre")
}

data class HistoricoRolagemItem(
    val texto: String,
    val payload: DiscordRollPayload,
    val statusEnvio: String?,
    val detalheErro: String?
)

data class RollMappedOption(
    val id: String,
    val label: String,
    val contextLabel: String,
    val target: Int?
)

data class DamageSourceOption(
    val id: String,
    val label: String,
    val contextLabel: String,
    val damageExpression: String
)

enum class StDamageMode(val label: String) {
    GDP("GdP"),
    GEB("GeB")
}

data class PericiaRollOption(
    val id: String,
    val nome: String,
    val especializacao: String,
    val contextLabel: String,
    val target: Int,
    val descricao: String
)

data class MagiaRollOption(
    val id: String,
    val definicaoId: String,
    val nome: String,
    val contextLabel: String,
    val target: Int,
    val duracao: String?,
    val energia: String?,
    val tempoOperacao: String?,
    val encantamentoAlvo: String?,
    val descricao: String
)

data class TecnicaRollOption(
    val id: String,
    val nome: String,
    val periciaBaseNome: String,
    val contextLabel: String,
    val target: Int?,
    val descricao: String
)

data class ParsedDamage(
    val diceCount: Int,
    val modifier: Int,
    val suffix: String
)

data class SoulAspectOption(
    val nome: String,
    val descricao: String
)

data class RollDescricaoDialog(
    val titulo: String,
    val texto: String
)

fun atributoNomeCompleto(sigla: String): String = when (sigla.uppercase()) {
    "ST" -> "ForÃ§a"
    "DX" -> "Destreza"
    "IQ" -> "InteligÃªncia"
    "HT" -> "Vitalidade"
    "VON" -> "Vontade"
    "PER" -> "PercepÃ§Ã£o"
    else -> sigla
}

fun periciaLabel(pericia: PericiaSelecionada): String {
    return if (pericia.especializacao.isBlank()) {
        pericia.nome
    } else {
        "${pericia.nome} (${pericia.especializacao})"
    }
}

fun periciaSelectionKey(pericia: PericiaSelecionada, index: Int): String {
    return "${pericia.definicaoId}|${pericia.especializacao}|$index"
}

fun parseDamageExpression(expr: String): ParsedDamage? {
    val match = Regex("""^\s*(\d+)d((?:\s*[+-]\s*\d+)*)\s*(.*)$""").find(expr) ?: return null
    val diceCount = match.groupValues[1].toIntOrNull() ?: return null
    val modsRaw = match.groupValues[2]
    val modTokens = Regex("""[+-]\s*\d+""").findAll(modsRaw).map { it.value.replace(" ", "") }.toList()
    val modifier = modTokens.sumOf { it.toIntOrNull() ?: return null }
    val suffix = match.groupValues[3].trim()
    if (diceCount <= 0) return null
    return ParsedDamage(diceCount = diceCount, modifier = modifier, suffix = suffix)
}

fun formatDamageCore(parsed: ParsedDamage): String {
    val mod = when {
        parsed.modifier > 0 -> "+${parsed.modifier}"
        parsed.modifier < 0 -> parsed.modifier.toString()
        else -> ""
    }
    return "${parsed.diceCount}d$mod"
}

fun splitDamageEntries(expression: String): List<String> {
    val rawParts = expression.split("/").map { it.trim() }.filter { it.isNotBlank() }
    if (rawParts.isEmpty()) return listOf(expression.trim()).filter { it.isNotBlank() }

    var lastCore: String? = null
    return rawParts.map { part ->
        val parsed = parseDamageExpression(part)
        if (parsed != null) {
            lastCore = formatDamageCore(parsed)
            part
        } else {
            val core = lastCore
            if (core != null) "$core $part" else part
        }
    }
}

val SOUL_ASPECT_OPTIONS = listOf(
    SoulAspectOption(
        nome = "1Âº Aspecto - ComunicaÃ§Ã£o empÃ¡tica",
        descricao = """
            O jogador pode usar magia da alma para se entapizar com um ser, qualquer ser, e se comunicar de uma maneira diferente.

            Em jogo: A magia da alma permite aos jogadores verem as almas e tudo que hÃ¡ relacionado com ela em â€œcenaâ€ . Por exemplo, Salamur, ao usar a magia da alma conseguiu â€œsentirâ€  a presenÃ§a de uma entidade maior no deserto. AlÃ©m disso, ao pegar em suas mÃ£os o equipamento de Meldor, ele conseguiu ver seus Ãºltimos momentos antes de morrer, dando uma pista de onde comeÃ§ar a procurar por Meldor e o que aconteceu com ele.

            CÃ©sar, em outro momento, utilizou a magia â€œLuz contÃ­nuaâ€  com um adicional de um ponto em magia da alma, o que o ajudou a revelar uma entrada secreta em uma cÃ¢mara onde, aparentemente, nÃ£o havia nada.

            Em combate: O jogador pode criar um â€œvÃ­nculoâ€  maior com a alma dos inimigos/aliados. Magias que afetam diretamente a mente/sentidos dos inimigos, que precisam de concentraÃ§Ã£o, agora podem ser utilizadas normalmente, sem uma concentraÃ§Ã£o prÃ©via. Por exemplo, CÃ©sar pÃ´de usar a magia Medo em um â€œGrande Rotmenâ€  sem precisar se concentrar nela. AlÃ©m disso, caso algum jogador tivesse interesse, poderia usar IntimidaÃ§Ã£o com Magia da Alma e conseguir afetar todos os jogadores. Magias de cura tambÃ©m podem ser afetadas positivamente pela Magia da Alma, quando utilizadas juntas.
        """.trimIndent()
    ),
    SoulAspectOption(
        nome = "2Âº Aspecto - TranslocaÃ§Ã£o astral",
        descricao = """
            Jogadores conseguem forÃ§ar o deslocamento do corpo no mundo real a partir do movimento dele no mundo da alma. (Deslocamento reduzido)

            Em cena: Os jogadores podem â€œcruzarâ€  lugares utilizando o mundo da alma, Ã© como uma translocaÃ§Ã£o ou teleporte, mas ela permite que os jogadores â€œvejam/interajamâ€  com o mundo exterior enquanto o fazem.

            Em jogo: O jogador pode gastar 1 ponto de magia da alma para fazer um ataque ou uma defesa ativa oculta, utilizando o mundo espiritual antes do mundo real.

            Em caso de ataque: O jogador deve declarar que irÃ¡ utilizar a magia da alma e fazer um teste de â€œsentidosâ€ , antes do ataque. O teste de sentidos Ã© baseado em DX ou HT, seja qual for maior. ApÃ³s o teste de sentido, caso sucesso, o jogador faz o teste de ataque contra o inimigo. O inimigo tem que fazer um teste de percepÃ§Ã£o com redutor de -4 para poder usar alguma defesa ativa.

            Em caso de defesa: O jogador deve declarar que irÃ¡ utilizar esse ponto de magia da alma como uma defesa ativa e, ao fazer, se esquiva automaticamente do ataque.
        """.trimIndent()
    ),
    SoulAspectOption(
        nome = "3Âº Aspecto - Corrente da alma",
        descricao = """
            Vincula a alma do jogador com um objeto inanimado.
            Ainda hÃ¡ a possibilidade de uma entidade poder ser relacionada Ã  vinculaÃ§Ã£o, podendo intervir positivamente, ou negativamente, no processo.

            Em cena: O jogador Xing tem um machado que estima muito, hÃ¡ muitos anos utiliza o machado para todo tipo de atividade e nÃ£o se separa por nada dele. Nesses casos, o jogador pode fazer um vÃ­nculo de alma com o objeto, intensificando a sua ligaÃ§Ã£o com o objeto para todos os fins.
            Xing, portanto, se concentra, pede bÃªnÃ§Ã£os as entidades em que ele acredita e vincula o machado Ã  sua alma, ampliando as suas habilidades de todas as jogadas com o objeto, podendo acertar o arremesso dessa arma em alvos que, normalmente, talvez nÃ£o pudesse.

            Em jogo: O jogador utiliza 1 ponto de magia da alma e faz um teste de vontade. Se falhar o teste, o jogador tem um perÃ­odo de 24 horas para tentar novamente. Caso o sucesso aconteÃ§a, o jogador irÃ¡ ampliar as suas capacidades com o objeto. No caso de uma arma, o jogador irÃ¡ aumentar todo NH efetivo com esse equipamento em 2 pontos, sempre que usar essa arma. AlÃ©m disso, qualquer personagem que pegar a arma e tentar usÃ¡-la, terÃ¡ uma penalidade de 2 de NH efetivo para o fazer. Em relaÃ§Ã£o ao ponto de alma, ele ficarÃ¡ â€œpresoâ€  na arma atÃ© o vÃ­nculo ser rompido. Portanto, se o jogador tiver 4 pontos de alma, ele terÃ¡, depois da vinculaÃ§Ã£o, 3 pontos.

            Se, por qualquer motivo, o vÃ­nculo for rompido sem ser pelo prÃ³prio jogador, o jogador terÃ¡ de fazer um teste de vontade para nÃ£o ser atordoado. As formas de se romper o vÃ­nculo sÃ£o: Algum outro jogador pode fazer uma jogada de vÃ­nculo de alma, fazendo um teste de vontade entre os personagens. Se o jogador for desarmado, o inimigo conseguir segurar a arma, e atacar com ela, o vÃ­nculo Ã© rompido. Se o personagem, por algum motivo, arremessar a arma e nÃ£o conseguir recuperÃ¡-la, o vÃ­nculo serÃ¡ rompido. Se a arma for roubada, em qualquer tipo de cena ou jogada, o vÃ­nculo serÃ¡ rompido.
        """.trimIndent()
    ),
    SoulAspectOption(
        nome = "4Â° Aspecto - ManipulaÃ§Ã£o da alma",
        descricao = """
            O indivÃ­duo consegue manipular a alma, aumentando a sua projeÃ§Ã£o em aspectos da sua realidade, podendo impulsionar as suas capacidades, sejam fÃ­sicas ou mentais.

            Em cena: O jogador pode usar o seu poder da alma para intensificar alguma caracterÃ­stica, habilidade, peculiaridade ou perÃ­cia, aumentando positivamente suas capacidades.

            Por exemplo: O jogador Xing precisa levantar uma pedra muito pesada, mas nÃ£o tem ST suficiente, entÃ£o, pode usar um ponto de magia da alma para ampliar a sua capacidade de carregamento por um breve momento.
            Ou, o jogador CÃ©sar precisava conseguir enxergar uma particularidade, mas a dificuldade da jogada o impedia, portanto ele usou um ponto de magia da alma para intensificar a sua percepÃ§Ã£o (visÃ£o) e conseguiu enxergar o detalhe necessÃ¡rio.

            Em jogo: O jogador consegue usar um ponto de magia da alma para ampliar suas capacidades.
            Tabela: Atributo 1:1, PerÃ­cia 1:3. Atributos secundÃ¡rios 1:3. IntensificaÃ§Ã£o de dano: 1 ponto de magia da alma = +1 dano por dado.
        """.trimIndent()
    ),
    SoulAspectOption(
        nome = "1Âº Aspecto - ExpiaÃ§Ã£o",
        descricao = """
            O jogador pode usar a magia da alma para â€œapatizarâ€  um outro ser, ao se conectar, fazendo o canal das emoÃ§Ãµes do alvo se atrofiar, a ponto dele praticamente nÃ£o ter mais emoÃ§Ãµes.

            Em jogo:.
            Em combate: O jogador usa a conexÃ£o da magia da alma para forÃ§ar a remoÃ§Ã£o de uma ou mais emoÃ§Ãµes no alvo.
        """.trimIndent()
    ),
    SoulAspectOption(
        nome = "2Âº Aspecto - IntrusÃ£o mental",
        descricao = """
            Jogadores conseguem forÃ§ar o deslocamento do corpo alheio a partir da alma do alvo.

            Em cena:.
            Em jogo: O jogador pode â€œatrapalharâ€  o ataque ou a aÃ§Ã£o do alvo, fazendo o corpo do alvo se movimentar, a partir de uma aÃ§Ã£o na alma do alvo.
            Em caso de defesa:.
        """.trimIndent()
    ),
    SoulAspectOption(
        nome = "3Âº Aspecto - Corrente da condenaÃ§Ã£o",
        descricao = """
            Vincula uma alma com um objeto inanimado. TambÃ©m pode vincular a uma entidade, mas depende da MÃ£o da CriaÃ§Ã£o.
            Em sua versÃ£o corrompida, o jogador consegue â€œamaldiÃ§oarâ€  a alma alheia, a vinculando a um local/item que a prenderÃ¡ ali eternamente.

            Em cena:.
            Em jogo:.
            Passando ou nÃ£o no teste, o jogador usa um ponto de magia da alma para abrir o canal de conexÃ£o. Para vincular com a entidade, caso ela aceite, o jogador deverÃ¡ utilizar outro ponto de magia da alma, caso nÃ£o seja ele o portador, o portador que deverÃ¡ utilizar esse ponto em seu lugar.
            O criado, faz mais um teste com a perÃ­cia e, agora sim, o vÃ­nculo estÃ¡ feito.
            *A depender da entidade, mais testes poderÃ£o ser exigidos.
        """.trimIndent()
    ),
    SoulAspectOption(
        nome = "4Â° Aspecto - ManipulaÃ§Ã£o da alma",
        descricao = """
            O indivÃ­duo consegue manipular a alma alheia, a fazendo reduzir a capacidade do alvo em algum aspecto, ou caracterÃ­stica, sejam elas fÃ­sicas ou mentais.

            Em cena:.
            Em jogo: O jogador consegue usar um ponto de magia da alma para ampliar suas capacidades.
            Tabela: Atributo 1:1, PerÃ­cia 1:3. Atributos secundÃ¡rios 1:3. IntensificaÃ§Ã£o de dano: 1 ponto de magia da alma = +1 dano por dado.
        """.trimIndent()
    )
)
