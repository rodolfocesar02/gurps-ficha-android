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
    "ST" -> "Força"
    "DX" -> "Destreza"
    "IQ" -> "Inteligência"
    "HT" -> "Vitalidade"
    "VON" -> "Vontade"
    "PER" -> "Percepção"
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
        nome = "1º Aspecto - Comunicação empática",
        descricao = """
            O jogador pode usar magia da alma para se entapizar com um ser, qualquer ser, e se comunicar de uma maneira diferente.

            Em jogo: A magia da alma permite aos jogadores verem as almas e tudo que há relacionado com ela em “cenaâ€ . Por exemplo, Salamur, ao usar a magia da alma conseguiu “sentirâ€  a presença de uma entidade maior no deserto. Além disso, ao pegar em suas mãos o equipamento de Meldor, ele conseguiu ver seus últimos momentos antes de morrer, dando uma pista de onde começar a procurar por Meldor e o que aconteceu com ele.

            César, em outro momento, utilizou a magia “Luz contínuaâ€  com um adicional de um ponto em magia da alma, o que o ajudou a revelar uma entrada secreta em uma câmara onde, aparentemente, não havia nada.

            Em combate: O jogador pode criar um “vínculoâ€  maior com a alma dos inimigos/aliados. Magias que afetam diretamente a mente/sentidos dos inimigos, que precisam de concentração, agora podem ser utilizadas normalmente, sem uma concentração prévia. Por exemplo, César pôde usar a magia Medo em um “Grande Rotmenâ€  sem precisar se concentrar nela. Além disso, caso algum jogador tivesse interesse, poderia usar Intimidação com Magia da Alma e conseguir afetar todos os jogadores. Magias de cura também podem ser afetadas positivamente pela Magia da Alma, quando utilizadas juntas.
        """.trimIndent()
    ),
    SoulAspectOption(
        nome = "2º Aspecto - Translocação astral",
        descricao = """
            Jogadores conseguem forçar o deslocamento do corpo no mundo real a partir do movimento dele no mundo da alma. (Deslocamento reduzido)

            Em cena: Os jogadores podem “cruzarâ€  lugares utilizando o mundo da alma, é como uma translocação ou teleporte, mas ela permite que os jogadores “vejam/interajamâ€  com o mundo exterior enquanto o fazem.

            Em jogo: O jogador pode gastar 1 ponto de magia da alma para fazer um ataque ou uma defesa ativa oculta, utilizando o mundo espiritual antes do mundo real.

            Em caso de ataque: O jogador deve declarar que irá utilizar a magia da alma e fazer um teste de “sentidosâ€ , antes do ataque. O teste de sentidos é baseado em DX ou HT, seja qual for maior. Após o teste de sentido, caso sucesso, o jogador faz o teste de ataque contra o inimigo. O inimigo tem que fazer um teste de percepção com redutor de -4 para poder usar alguma defesa ativa.

            Em caso de defesa: O jogador deve declarar que irá utilizar esse ponto de magia da alma como uma defesa ativa e, ao fazer, se esquiva automaticamente do ataque.
        """.trimIndent()
    ),
    SoulAspectOption(
        nome = "3º Aspecto - Corrente da alma",
        descricao = """
            Vincula a alma do jogador com um objeto inanimado.
            Ainda há a possibilidade de uma entidade poder ser relacionada à vinculação, podendo intervir positivamente, ou negativamente, no processo.

            Em cena: O jogador Xing tem um machado que estima muito, há muitos anos utiliza o machado para todo tipo de atividade e não se separa por nada dele. Nesses casos, o jogador pode fazer um vínculo de alma com o objeto, intensificando a sua ligação com o objeto para todos os fins.
            Xing, portanto, se concentra, pede bênçãos as entidades em que ele acredita e vincula o machado à sua alma, ampliando as suas habilidades de todas as jogadas com o objeto, podendo acertar o arremesso dessa arma em alvos que, normalmente, talvez não pudesse.

            Em jogo: O jogador utiliza 1 ponto de magia da alma e faz um teste de vontade. Se falhar o teste, o jogador tem um período de 24 horas para tentar novamente. Caso o sucesso aconteça, o jogador irá ampliar as suas capacidades com o objeto. No caso de uma arma, o jogador irá aumentar todo NH efetivo com esse equipamento em 2 pontos, sempre que usar essa arma. Além disso, qualquer personagem que pegar a arma e tentar usá-la, terá uma penalidade de 2 de NH efetivo para o fazer. Em relação ao ponto de alma, ele ficará “presoâ€  na arma até o vínculo ser rompido. Portanto, se o jogador tiver 4 pontos de alma, ele terá, depois da vinculação, 3 pontos.

            Se, por qualquer motivo, o vínculo for rompido sem ser pelo próprio jogador, o jogador terá de fazer um teste de vontade para não ser atordoado. As formas de se romper o vínculo são: Algum outro jogador pode fazer uma jogada de vínculo de alma, fazendo um teste de vontade entre os personagens. Se o jogador for desarmado, o inimigo conseguir segurar a arma, e atacar com ela, o vínculo é rompido. Se o personagem, por algum motivo, arremessar a arma e não conseguir recuperá-la, o vínculo será rompido. Se a arma for roubada, em qualquer tipo de cena ou jogada, o vínculo será rompido.
        """.trimIndent()
    ),
    SoulAspectOption(
        nome = "4° Aspecto - Manipulação da alma",
        descricao = """
            O indivíduo consegue manipular a alma, aumentando a sua projeção em aspectos da sua realidade, podendo impulsionar as suas capacidades, sejam físicas ou mentais.

            Em cena: O jogador pode usar o seu poder da alma para intensificar alguma característica, habilidade, peculiaridade ou perícia, aumentando positivamente suas capacidades.

            Por exemplo: O jogador Xing precisa levantar uma pedra muito pesada, mas não tem ST suficiente, então, pode usar um ponto de magia da alma para ampliar a sua capacidade de carregamento por um breve momento.
            Ou, o jogador César precisava conseguir enxergar uma particularidade, mas a dificuldade da jogada o impedia, portanto ele usou um ponto de magia da alma para intensificar a sua percepção (visão) e conseguiu enxergar o detalhe necessário.

            Em jogo: O jogador consegue usar um ponto de magia da alma para ampliar suas capacidades.
            Tabela: Atributo 1:1, Perícia 1:3. Atributos secundários 1:3. Intensificação de dano: 1 ponto de magia da alma = +1 dano por dado.
        """.trimIndent()
    ),
    SoulAspectOption(
        nome = "1º Aspecto - Expiação",
        descricao = """
            O jogador pode usar a magia da alma para “apatizarâ€  um outro ser, ao se conectar, fazendo o canal das emoções do alvo se atrofiar, a ponto dele praticamente não ter mais emoções.

            Em jogo:.
            Em combate: O jogador usa a conexão da magia da alma para forçar a remoção de uma ou mais emoções no alvo.
        """.trimIndent()
    ),
    SoulAspectOption(
        nome = "2º Aspecto - Intrusão mental",
        descricao = """
            Jogadores conseguem forçar o deslocamento do corpo alheio a partir da alma do alvo.

            Em cena:.
            Em jogo: O jogador pode “atrapalharâ€  o ataque ou a ação do alvo, fazendo o corpo do alvo se movimentar, a partir de uma ação na alma do alvo.
            Em caso de defesa:.
        """.trimIndent()
    ),
    SoulAspectOption(
        nome = "3º Aspecto - Corrente da condenação",
        descricao = """
            Vincula uma alma com um objeto inanimado. Também pode vincular a uma entidade, mas depende da Mão da Criação.
            Em sua versão corrompida, o jogador consegue “amaldiçoarâ€  a alma alheia, a vinculando a um local/item que a prenderá ali eternamente.

            Em cena:.
            Em jogo:.
            Passando ou não no teste, o jogador usa um ponto de magia da alma para abrir o canal de conexão. Para vincular com a entidade, caso ela aceite, o jogador deverá utilizar outro ponto de magia da alma, caso não seja ele o portador, o portador que deverá utilizar esse ponto em seu lugar.
            O criado, faz mais um teste com a perícia e, agora sim, o vínculo está feito.
            *A depender da entidade, mais testes poderão ser exigidos.
        """.trimIndent()
    ),
    SoulAspectOption(
        nome = "4° Aspecto - Manipulação da alma",
        descricao = """
            O indivíduo consegue manipular a alma alheia, a fazendo reduzir a capacidade do alvo em algum aspecto, ou característica, sejam elas físicas ou mentais.

            Em cena:.
            Em jogo: O jogador consegue usar um ponto de magia da alma para ampliar suas capacidades.
            Tabela: Atributo 1:1, Perícia 1:3. Atributos secundários 1:3. Intensificação de dano: 1 ponto de magia da alma = +1 dano por dado.
        """.trimIndent()
    )
)
