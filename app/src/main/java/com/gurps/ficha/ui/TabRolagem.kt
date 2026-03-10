package com.gurps.ficha.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gurps.ficha.data.network.DiscordRollPayload
import com.gurps.ficha.domain.rules.MagiaEnergiaRules
import com.gurps.ficha.model.PericiaSelecionada
import com.gurps.ficha.model.PERICIAS_COMBATE
import com.gurps.ficha.model.TipoEquipamento
import com.gurps.ficha.viewmodel.DefenseType
import com.gurps.ficha.viewmodel.FichaViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlinx.coroutines.launch
import kotlin.random.Random
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gurps.ficha.BuildConfig

private const val CUSTOM_ROLL_RETENTION_MS = 5 * 60 * 1000L

private enum class TipoTeste(val label: String) {
    ATRIBUTO("Atributo"),
    ATAQUE("Ataque"),
    PERICIA("Pericia"),
    TECNICA("Tecnica"),
    MAGIA("Magia"),
    DEFESA("Defesa"),
    LIVRE("Livre")
}

private data class HistoricoRolagemItem(
    val texto: String,
    val payload: DiscordRollPayload,
    val statusEnvio: String?,
    val detalheErro: String?
)

private data class RollMappedOption(
    val id: String,
    val label: String,
    val contextLabel: String,
    val target: Int?
)

private data class DamageSourceOption(
    val id: String,
    val label: String,
    val contextLabel: String,
    val damageExpression: String
)

private enum class StDamageMode(val label: String) {
    GDP("GdP"),
    GEB("GeB")
}

private data class PericiaRollOption(
    val id: String,
    val nome: String,
    val especializacao: String,
    val contextLabel: String,
    val target: Int,
    val descricao: String
)

private data class MagiaRollOption(
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

private data class TecnicaRollOption(
    val id: String,
    val nome: String,
    val periciaBaseNome: String,
    val contextLabel: String,
    val target: Int?,
    val descricao: String
)

private data class ParsedDamage(
    val diceCount: Int,
    val modifier: Int,
    val suffix: String
)

private data class SoulAspectOption(
    val nome: String,
    val descricao: String
)

private data class RollDescricaoDialog(
    val titulo: String,
    val texto: String
)

private fun atributoNomeCompleto(sigla: String): String = when (sigla.uppercase()) {
    "ST" -> "ForÃ§a"
    "DX" -> "Destreza"
    "IQ" -> "InteligÃªncia"
    "HT" -> "Vitalidade"
    "VON" -> "Vontade"
    "PER" -> "PercepÃ§Ã£o"
    else -> sigla
}

private val SOUL_ASPECT_OPTIONS = listOf(
    SoulAspectOption(
        nome = "1Âº Aspecto - ComunicaÃ§Ã£o empÃ¡tica",
        descricao = """
            O jogador pode usar magia da alma para se entapizar com um ser, qualquer ser, e se comunicar de uma maneira diferente.

            Em jogo: A magia da alma permite aos jogadores verem as almas e tudo que hÃ¡ relacionado com ela em â€œcenaâ€. Por exemplo, Salamur, ao usar a magia da alma conseguiu â€œsentirâ€ a presenÃ§a de uma entidade maior no deserto. AlÃ©m disso, ao pegar em suas mÃ£os o equipamento de Meldor, ele conseguiu ver seus Ãºltimos momentos antes de morrer, dando uma pista de onde comeÃ§ar a procurar por Meldor e o que aconteceu com ele.

            CÃ©sar, em outro momento, utilizou a magia â€œLuz contÃ­nuaâ€ com um adicional de um ponto em magia da alma, o que o ajudou a revelar uma entrada secreta em uma cÃ¢mara onde, aparentemente, nÃ£o havia nada.

            Em combate: O jogador pode criar um â€œvÃ­nculoâ€ maior com a alma dos inimigos/aliados. Magias que afetam diretamente a mente/sentidos dos inimigos, que precisam de concentraÃ§Ã£o, agora podem ser utilizadas normalmente, sem uma concentraÃ§Ã£o prÃ©via. Por exemplo, CÃ©sar pÃ´de usar a magia Medo em um â€œGrande Rotmenâ€ sem precisar se concentrar nela. AlÃ©m disso, caso algum jogador tivesse interesse, poderia usar IntimidaÃ§Ã£o com Magia da Alma e conseguir afetar todos os jogadores. Magias de cura tambÃ©m podem ser afetadas positivamente pela Magia da Alma, quando utilizadas juntas.
        """.trimIndent()
    ),
    SoulAspectOption(
        nome = "2Âº Aspecto - TranslocaÃ§Ã£o astral",
        descricao = """
            Jogadores conseguem forÃ§ar o deslocamento do corpo no mundo real a partir do movimento dele no mundo da alma. (Deslocamento reduzido)

            Em cena: Os jogadores podem â€œcruzarâ€ lugares utilizando o mundo da alma, Ã© como uma translocaÃ§Ã£o ou teleporte, mas ela permite que os jogadores â€œvejam/interajamâ€ com o mundo exterior enquanto o fazem.

            Em jogo: O jogador pode gastar 1 ponto de magia da alma para fazer um ataque ou uma defesa ativa oculta, utilizando o mundo espiritual antes do mundo real.

            Em caso de ataque: O jogador deve declarar que irÃ¡ utilizar a magia da alma e fazer um teste de â€œsentidosâ€, antes do ataque. O teste de sentidos Ã© baseado em DX ou HT, seja qual for maior. ApÃ³s o teste de sentido, caso sucesso, o jogador faz o teste de ataque contra o inimigo. O inimigo tem que fazer um teste de percepÃ§Ã£o com redutor de -4 para poder usar alguma defesa ativa.

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

            Em jogo: O jogador utiliza 1 ponto de magia da alma e faz um teste de vontade. Se falhar o teste, o jogador tem um perÃ­odo de 24 horas para tentar novamente. Caso o sucesso aconteÃ§a, o jogador irÃ¡ ampliar as suas capacidades com o objeto. No caso de uma arma, o jogador irÃ¡ aumentar todo NH efetivo com esse equipamento em 2 pontos, sempre que usar essa arma. AlÃ©m disso, qualquer personagem que pegar a arma e tentar usÃ¡-la, terÃ¡ uma penalidade de 2 de NH efetivo para o fazer. Em relaÃ§Ã£o ao ponto de alma, ele ficarÃ¡ â€œpresoâ€ na arma atÃ© o vÃ­nculo ser rompido. Portanto, se o jogador tiver 4 pontos de alma, ele terÃ¡, depois da vinculaÃ§Ã£o, 3 pontos.

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
            O jogador pode usar a magia da alma para â€œapatizarâ€ um outro ser, ao se conectar, fazendo o canal das emoÃ§Ãµes do alvo se atrofiar, a ponto dele praticamente nÃ£o ter mais emoÃ§Ãµes.

            Em jogo:.
            Em combate: O jogador usa a conexÃ£o da magia da alma para forÃ§ar a remoÃ§Ã£o de uma ou mais emoÃ§Ãµes no alvo.
        """.trimIndent()
    ),
    SoulAspectOption(
        nome = "2Âº Aspecto - IntrusÃ£o mental",
        descricao = """
            Jogadores conseguem forÃ§ar o deslocamento do corpo alheio a partir da alma do alvo.

            Em cena:.
            Em jogo: O jogador pode â€œatrapalharâ€ o ataque ou a aÃ§Ã£o do alvo, fazendo o corpo do alvo se movimentar, a partir de uma aÃ§Ã£o na alma do alvo.
            Em caso de defesa:.
        """.trimIndent()
    ),
    SoulAspectOption(
        nome = "3Âº Aspecto - Corrente da condenaÃ§Ã£o",
        descricao = """
            Vincula uma alma com um objeto inanimado. TambÃ©m pode vincular a uma entidade, mas depende da MÃ£o da CriaÃ§Ã£o.
            Em sua versÃ£o corrompida, o jogador consegue â€œamaldiÃ§oarâ€ a alma alheia, a vinculando a um local/item que a prenderÃ¡ ali eternamente.

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

private fun periciaLabel(pericia: PericiaSelecionada): String {
    return if (pericia.especializacao.isBlank()) {
        pericia.nome
    } else {
        "${pericia.nome} (${pericia.especializacao})"
    }
}

private fun periciaSelectionKey(pericia: PericiaSelecionada, index: Int): String {
    return "${pericia.definicaoId}|${pericia.especializacao}|$index"
}

private fun parseDamageExpression(expr: String): ParsedDamage? {
    val match = Regex("""^\s*(\d+)d((?:\s*[+-]\s*\d+)*)\s*(.*)$""").find(expr) ?: return null
    val diceCount = match.groupValues[1].toIntOrNull() ?: return null
    val modsRaw = match.groupValues[2]
    val modTokens = Regex("""[+-]\s*\d+""").findAll(modsRaw).map { it.value.replace(" ", "") }.toList()
    val modifier = modTokens.sumOf { it.toIntOrNull() ?: return null }
    val suffix = match.groupValues[3].trim()
    if (diceCount <= 0) return null
    return ParsedDamage(diceCount = diceCount, modifier = modifier, suffix = suffix)
}

private fun formatDamageCore(parsed: ParsedDamage): String {
    val mod = when {
        parsed.modifier > 0 -> "+${parsed.modifier}"
        parsed.modifier < 0 -> parsed.modifier.toString()
        else -> ""
    }
    return "${parsed.diceCount}d$mod"
}

private fun splitDamageEntries(expression: String): List<String> {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabRolagem(viewModel: FichaViewModel) {
    val p = viewModel.personagem
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val isSmallScreen = screenWidthDp <= 380
    val isVerySmallScreen = screenWidthDp <= 360
    val isTinyScreen = screenWidthDp <= 320
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    val historico = remember { mutableStateListOf<HistoricoRolagemItem>() }
    val coroutineScope = rememberCoroutineScope()
    val canaisDiscord = viewModel.canaisDiscord
    val canalSelecionadoId = viewModel.canalDiscordSelecionadoId
    val canalSelecionadoNome = viewModel.canalDiscordSelecionadoNome
    val canaisCarregando = viewModel.canaisDiscordCarregando
    val canaisErro = viewModel.canaisDiscordErro
    val backendOnline = canaisErro.isNullOrBlank()
    var showEditarCanalDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (canaisDiscord.isEmpty() && !canaisCarregando) {
            viewModel.atualizarCanaisDiscord()
        }
    }

    var ataqueSelecionadoKey by remember { mutableStateOf<String?>(null) }
    var fonteDanoSelecionadaId by remember { mutableStateOf<String?>(null) }
    var modificadorAtaque by remember { mutableIntStateOf(0) }
    val atributosRapidos = listOf("ST", "DX", "IQ", "HT", "VON", "PER")
    val modificadoresAtributo = remember {
        mutableStateMapOf(
            "ST" to 0, "DX" to 0, "IQ" to 0, "HT" to 0, "VON" to 0, "PER" to 0
        )
    }
    val modificadoresDefesa = remember {
        mutableStateMapOf(
            DefenseType.ESQUIVA to 0,
            DefenseType.APARA to 0,
            DefenseType.BLOQUEIO to 0
        )
    }
    val defesasPorTipo = viewModel.defesasAtivasVisiveis.associateBy { it.type }
    var showPericiasDialog by remember { mutableStateOf(false) }
    var showTecnicasDialog by remember { mutableStateOf(false) }
    var showMagiasDialog by remember { mutableStateOf(false) }
    var showRolagemPersonalizadaDialog by remember { mutableStateOf(false) }
    var showMagiaAlmaDialog by remember { mutableStateOf(false) }
    var showEnergiaManualDialog by remember { mutableStateOf(false) }
    var showEditarPvRolagemDialog by remember { mutableStateOf(false) }
    var showEditarPfRolagemDialog by remember { mutableStateOf(false) }
    var magiaPendenteEnergia by remember { mutableStateOf<MagiaRollOption?>(null) }
    var energiaManualInput by remember { mutableStateOf("") }
    var talismaMagiaVinculada by remember { mutableStateOf<String?>(null) }
    var aspectoMagiaAlmaSelecionado by remember { mutableStateOf<SoulAspectOption?>(null) }
    var descricaoDialog by remember { mutableStateOf<RollDescricaoDialog?>(null) }
    var stDamageMode by remember { mutableStateOf(StDamageMode.GDP) }
    var modificadorMagiaAlma by remember { mutableIntStateOf(0) }
    var modificadorGlobalPraCego by remember { mutableIntStateOf(0) }
    var dadosPersonalizadosQuantidade by remember { mutableIntStateOf(1) }
    var dadosPersonalizadosFaces by remember { mutableIntStateOf(6) }
    var dadosPersonalizadosModificador by remember { mutableIntStateOf(0) }
    var dadosPersonalizadosQuantidadeInput by remember { mutableStateOf("1") }
    var dadosPersonalizadosFacesInput by remember { mutableStateOf("6") }
    var dadosPersonalizadosModificadorInput by remember { mutableStateOf("0") }
    var ultimoUsoRolagemPersonalizadaMs by remember { mutableStateOf<Long?>(null) }
    val modificadoresPericia = remember { mutableStateMapOf<String, Int>() }
    val modificadoresTecnica = remember { mutableStateMapOf<String, Int>() }
    val modificadoresMagia = remember { mutableStateMapOf<String, Int>() }
    val horizontalPadding = when {
        isTinyScreen -> 6.dp
        isVerySmallScreen -> 8.dp
        else -> 10.dp
    }
    val rowSpacing = when {
        isTinyScreen -> 4.dp
        else -> 6.dp
    }
    val innerCardPadding = when {
        isTinyScreen -> 4.dp
        else -> 6.dp
    }
    val outerCardVerticalPadding = when {
        isTinyScreen -> 4.dp
        isVerySmallScreen -> 5.dp
        else -> 6.dp
    }
    val innerCardVerticalPadding = when {
        isTinyScreen -> 2.dp
        isVerySmallScreen -> 3.dp
        else -> 4.dp
    }
    val statsNumberStyle = when {
        isTinyScreen -> MaterialTheme.typography.headlineSmall
        isVerySmallScreen -> MaterialTheme.typography.headlineMedium
        else -> MaterialTheme.typography.headlineLarge
    }
    val defenseNumberStyle = when {
        isTinyScreen -> MaterialTheme.typography.headlineSmall
        isVerySmallScreen -> MaterialTheme.typography.headlineMedium
        else -> MaterialTheme.typography.headlineMedium
    }
    val cardTitleStyle = when {
        isTinyScreen -> MaterialTheme.typography.titleSmall
        isVerySmallScreen -> MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp)
        else -> MaterialTheme.typography.titleMedium
    }
    val compactLabelStyle = if (isVerySmallScreen) {
        MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
    } else {
        MaterialTheme.typography.labelSmall
    }

    val pvFixoRolagem = p.pontosVida.coerceAtLeast(0)
    val pfFixoRolagem = p.pontosFadiga.coerceAtLeast(0)
    val maxPvRolagem = (pvFixoRolagem * 5).coerceAtLeast(0)
    val pvAtualRolagem = (p.pontosVidaRolagemAtual ?: pvFixoRolagem).coerceIn(0, maxPvRolagem)
    val pfAtualRolagem = (p.pontosFadigaRolagemAtual ?: pfFixoRolagem).coerceAtLeast(0)
    var pvAtualInput by remember { mutableStateOf(pvAtualRolagem.toString()) }
    var pfAtualInput by remember { mutableStateOf(pfAtualRolagem.toString()) }

    val periciasCombate = p.pericias.filter { it.definicaoId in PERICIAS_COMBATE }
    val basePericiasAtaque = if (periciasCombate.isNotEmpty()) periciasCombate else p.pericias
    val opcoesPericia = p.pericias.mapIndexed { index, pericia ->
        val nivel = pericia.calcularNivel(p)
        val descricaoRegra = viewModel.dataRepository
            .regraPericiaV2(pericia.definicaoId)
            ?.descricao
            .orEmpty()
        PericiaRollOption(
            id = "pericia_${periciaSelectionKey(pericia, index)}",
            nome = pericia.nome,
            especializacao = pericia.especializacao,
            contextLabel = "Pericia ${periciaLabel(pericia)}",
            target = nivel,
            descricao = descricaoRegra
        )
    }
    val opcoesMagia = p.magias.mapIndexedNotNull { index, magia ->
        val definicaoMagia = viewModel.dataRepository.getMagiaPorId(magia.definicaoId)
        // only include if prereqs satisfied
        if (definicaoMagia == null || !viewModel.prereqsSatisfied(definicaoMagia)) return@mapIndexedNotNull null
        val nivel = magia.calcularNivel(p, viewModel.nivelAptidaoMagica)
        val descricaoMagia = magia.texto?.trim().orEmpty().ifBlank { definicaoMagia.texto?.trim().orEmpty() }
        MagiaRollOption(
            id = "magia_${magia.definicaoId}_$index",
            definicaoId = magia.definicaoId,
            nome = magia.nome,
            contextLabel = "Magia ${magia.nome}",
            target = nivel,
            duracao = magia.duracao ?: definicaoMagia.duracao,
            energia = magia.energia ?: definicaoMagia.energia,
            tempoOperacao = magia.tempoOperacao ?: definicaoMagia.tempoOperacao,
            encantamentoAlvo = magia.encantamentoAlvo,
            descricao = descricaoMagia
        )
    }
    val repertorioParaTalisma = p.magias
        .map { it.nome }
        .filter { !it.equals("TalismÃ£", ignoreCase = true) && !it.equals("Talisma", ignoreCase = true) }
        .distinct()
        .sorted()
    val opcoesTecnica = p.tecnicas.mapIndexed { index, tecnica ->
        val descricaoTecnica = viewModel.tecnicasCatalogo
            .firstOrNull { it.id.equals(tecnica.definicaoId, ignoreCase = true) }
            ?.descricao
            .orEmpty()
        TecnicaRollOption(
            id = "tecnica_${tecnica.definicaoId}_$index",
            nome = tecnica.nome,
            periciaBaseNome = tecnica.periciaBaseNome,
            contextLabel = "Tecnica ${tecnica.nome}",
            target = tecnica.calcularNivel(p),
            descricao = descricaoTecnica
        )
    }
    val nivelMagiaDaAlma = 10 + viewModel.nivelAptidaoAstral
    val opcoesAtaque = basePericiasAtaque.mapIndexed { index, pericia ->
        val nivel = pericia.calcularNivel(p)
        RollMappedOption(
            id = periciaSelectionKey(pericia, index),
            label = "${periciaLabel(pericia)} ($nivel)",
            contextLabel = "Ataque ${periciaLabel(pericia)}",
            target = nivel
        )
    }
    val armasEquipadas = p.equipamentos
        .filter { it.tipo == TipoEquipamento.ARMA }
        .mapIndexed { index, equipamento ->
            val dano = equipamento.danoCalculadoComSt(p) ?: equipamento.armaDanoRaw?.trim().orEmpty()
            DamageSourceOption(
                id = "arma_$index",
                label = equipamento.nome,
                contextLabel = "Dano ${equipamento.nome}",
                damageExpression = dano.ifBlank { "-" }
            )
        }
    val fallbackSt = DamageSourceOption(
        id = "st_base",
        label = "Sem arma (${stDamageMode.label})",
        contextLabel = "Dano ST ${stDamageMode.label}",
        damageExpression = if (stDamageMode == StDamageMode.GDP) p.danoGdP else p.danoGeB
    )
    val fontesDano = if (armasEquipadas.isNotEmpty()) {
        listOf(fallbackSt) + armasEquipadas
    } else {
        listOf(fallbackSt)
    }

    val ataqueAtual = opcoesAtaque.firstOrNull { it.id == ataqueSelecionadoKey }
    val fonteDanoAtual = fontesDano.firstOrNull { it.id == fonteDanoSelecionadaId } ?: fontesDano.first()

    LaunchedEffect(opcoesAtaque) {
        if (opcoesAtaque.isNotEmpty() && opcoesAtaque.none { it.id == ataqueSelecionadoKey }) {
            ataqueSelecionadoKey = opcoesAtaque.first().id
        }
    }
    LaunchedEffect(fontesDano) {
        if (fontesDano.isNotEmpty() && fontesDano.none { it.id == fonteDanoSelecionadaId }) {
            fonteDanoSelecionadaId = fontesDano.first().id
        }
    }
    LaunchedEffect(opcoesPericia) {
        val ids = opcoesPericia.map { it.id }.toSet()
        modificadoresPericia.keys.toList().forEach { id ->
            if (id !in ids) modificadoresPericia.remove(id)
        }
        opcoesPericia.forEach { pericia ->
            if (modificadoresPericia[pericia.id] == null) {
                modificadoresPericia[pericia.id] = 0
            }
        }
    }
    LaunchedEffect(opcoesMagia) {
        val ids = opcoesMagia.map { it.id }.toSet()
        modificadoresMagia.keys.toList().forEach { id ->
            if (id !in ids) modificadoresMagia.remove(id)
        }
        opcoesMagia.forEach { magia ->
            if (modificadoresMagia[magia.id] == null) {
                modificadoresMagia[magia.id] = 0
            }
        }
    }
    LaunchedEffect(opcoesTecnica) {
        val ids = opcoesTecnica.map { it.id }.toSet()
        modificadoresTecnica.keys.toList().forEach { id ->
            if (id !in ids) modificadoresTecnica.remove(id)
        }
        opcoesTecnica.forEach { tecnica ->
            if (modificadoresTecnica[tecnica.id] == null) {
                modificadoresTecnica[tecnica.id] = 0
            }
        }
    }
    LaunchedEffect(pvAtualRolagem) {
        pvAtualInput = pvAtualRolagem.toString()
    }
    LaunchedEffect(pfAtualRolagem) {
        pfAtualInput = pfAtualRolagem.toString()
    }
    LaunchedEffect(p.pontosVidaRolagemAtual, pvFixoRolagem, maxPvRolagem) {
        val normalizado = (p.pontosVidaRolagemAtual ?: pvFixoRolagem).coerceIn(0, maxPvRolagem)
        if (p.pontosVidaRolagemAtual != normalizado) {
            viewModel.atualizarPontosVidaRolagemAtual(normalizado)
        }
    }
    LaunchedEffect(p.pontosFadigaRolagemAtual, pfFixoRolagem) {
        val normalizado = (p.pontosFadigaRolagemAtual ?: pfFixoRolagem).coerceAtLeast(0)
        if (p.pontosFadigaRolagemAtual != normalizado) {
            viewModel.atualizarPontosFadigaRolagemAtual(normalizado)
        }
    }

    fun registrarResultado(
        resultado: RolagemResultado,
        payload: DiscordRollPayload,
        statusEnvio: String?,
        detalheErro: String?,
        tipoLabel: String,
        contextoLabel: String,
        alvo: Int?,
        mod: Int
    ) {
        val hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val dadosTexto = resultado.dadosIndividuais.joinToString(" ")
        val resultadoTexto = if (alvo != null) {
            val margemTexto = if (resultado.margem >= 0) "+${resultado.margem}" else "${resultado.margem}"
            "${resultado.tipoResultado.name.replace("_", " ")} $margemTexto"
        } else {
            resultado.total.toString()
        }
        val linha = """
            $hora | ${payload.character}
            $tipoLabel ($contextoLabel)
            Dados: $dadosTexto = ${resultado.total}
            Resultado: $resultadoTexto
        """.trimIndent()
        historico.add(
            0,
            HistoricoRolagemItem(
                texto = linha,
                payload = payload,
                statusEnvio = statusEnvio,
                detalheErro = detalheErro
            )
        )
        if (historico.size > 20) {
            historico.removeLast()
        }
    }

    fun executarRolagem(tipo: TipoTeste, contextoLabel: String, alvo: Int?, mod: Int) {
        val modEfetivo = if (isPraCegoVariant) {
            (mod + modificadorGlobalPraCego).coerceIn(-999, 999)
        } else {
            mod
        }
        val resultado = rolarDados(3, modEfetivo, alvo)
        val payload = DiscordRollPayload(
            character = p.nome.ifBlank { "Personagem" },
            testType = tipo.label,
            context = contextoLabel,
            target = alvo,
            modifier = modEfetivo,
            dice = resultado.dadosIndividuais,
            total = resultado.total,
            outcome = resultado.tipoResultado.name,
            margin = if (resultado.alvo != null) resultado.margem else null,
            channelId = canalSelecionadoId
        )
        coroutineScope.launch {
            val envio = viewModel.enviarRolagemDiscord(payload)
            registrarResultado(
                resultado = resultado,
                payload = payload,
                statusEnvio = if (envio.enviado) "enviado" else "erro",
                detalheErro = envio.detalhe,
                tipoLabel = tipo.label,
                contextoLabel = contextoLabel,
                alvo = alvo,
                mod = modEfetivo
            )
        }
    }

    fun executarRolagemDano(contextoLabel: String, danoExpr: String) {
        val parsed = parseDamageExpression(danoExpr)
        val hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        if (parsed == null) {
            val linha = """
                $hora | ${p.nome.ifBlank { "Personagem" }}
                Dano ($contextoLabel)
                Dados: -
                Resultado: expressao nao rolavel ($danoExpr)
            """.trimIndent()
            val payload = DiscordRollPayload(
                character = p.nome.ifBlank { "Personagem" },
                testType = "Dano",
                context = contextoLabel,
                target = null,
                modifier = 0,
                dice = emptyList(),
                total = 0,
                outcome = TipoResultado.NENHUM.name,
                margin = null,
                channelId = canalSelecionadoId
            )
            coroutineScope.launch {
                val envio = viewModel.enviarRolagemDiscord(payload)
                historico.add(
                    0,
                    HistoricoRolagemItem(
                        texto = linha,
                        payload = payload,
                        statusEnvio = if (envio.enviado) "enviado" else "erro",
                        detalheErro = envio.detalhe
                    )
                )
            }
            return
        }

        val dados = (1..parsed.diceCount).map { Random.nextInt(1, 7) }
        val soma = dados.sum()
        val modEfetivo = if (isPraCegoVariant) {
            (parsed.modifier + modificadorGlobalPraCego).coerceIn(-999, 999)
        } else {
            parsed.modifier
        }
        val total = soma + modEfetivo
        val dadosTexto = dados.joinToString(" ")
        val resultadoTexto = buildString {
            append(total)
            if (parsed.suffix.isNotBlank()) append(" ${parsed.suffix}")
            if (modEfetivo != 0) append(" (mod ${if (modEfetivo > 0) "+$modEfetivo" else "$modEfetivo"})")
        }
        val linha = """
            $hora | ${p.nome.ifBlank { "Personagem" }}
            Dano ($contextoLabel)
            Dados: $dadosTexto = $total
            Resultado: $resultadoTexto
        """.trimIndent()
        val payload = DiscordRollPayload(
            character = p.nome.ifBlank { "Personagem" },
            testType = "Dano",
            context = contextoLabel,
            target = null,
            modifier = modEfetivo,
            dice = dados,
            total = total,
            outcome = TipoResultado.NENHUM.name,
            margin = null,
            channelId = canalSelecionadoId
        )
        coroutineScope.launch {
            val envio = viewModel.enviarRolagemDiscord(payload)
            historico.add(
                0,
                HistoricoRolagemItem(
                    texto = linha,
                    payload = payload,
                    statusEnvio = if (envio.enviado) "enviado" else "erro",
                    detalheErro = envio.detalhe
                )
            )
            if (historico.size > 20) {
                historico.removeLast()
            }
        }
    }

    fun executarRolagemPersonalizada(contextoLabel: String, quantidade: Int, faces: Int, mod: Int) {
        val qtdNormalizada = quantidade.coerceIn(1, 300)
        val facesNormalizadas = faces.coerceIn(1, 1000)
        val modNormalizado = mod.coerceIn(-999, 999)
        val modEfetivo = if (isPraCegoVariant) {
            (modNormalizado + modificadorGlobalPraCego).coerceIn(-999, 999)
        } else {
            modNormalizado
        }
        val dados = (1..qtdNormalizada).map { Random.nextInt(1, facesNormalizadas + 1) }
        val soma = dados.sum()
        val total = soma + modEfetivo
        val expr = buildString {
            append("${qtdNormalizada}d$facesNormalizadas")
            if (modEfetivo > 0) append("+$modEfetivo")
            if (modEfetivo < 0) append(modEfetivo)
        }
        val hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val linha = """
            $hora | ${p.nome.ifBlank { "Personagem" }}
            Livre ($contextoLabel)
            Dados: ${dados.joinToString(" ")} = $total
            Resultado: $total ($expr)
        """.trimIndent()
        val payload = DiscordRollPayload(
            character = p.nome.ifBlank { "Personagem" },
            testType = "Livre",
            context = contextoLabel,
            target = null,
            modifier = modEfetivo,
            dice = dados,
            total = total,
            outcome = TipoResultado.NENHUM.name,
            margin = null,
            channelId = canalSelecionadoId
        )
        coroutineScope.launch {
            val envio = viewModel.enviarRolagemDiscord(payload)
            historico.add(
                0,
                HistoricoRolagemItem(
                    texto = linha,
                    payload = payload,
                    statusEnvio = if (envio.enviado) "enviado" else "erro",
                    detalheErro = envio.detalhe
                )
            )
            if (historico.size > 20) {
                historico.removeLast()
            }
        }
    }

    fun resetarRolagemPersonalizadaParaPadrao() {
        dadosPersonalizadosQuantidade = 1
        dadosPersonalizadosFaces = 6
        dadosPersonalizadosModificador = 0
        dadosPersonalizadosQuantidadeInput = "1"
        dadosPersonalizadosFacesInput = "6"
        dadosPersonalizadosModificadorInput = "0"
    }

    fun reterRolagemPersonalizadaAindaValida(): Boolean {
        val ultimoUso = ultimoUsoRolagemPersonalizadaMs ?: return false
        return (System.currentTimeMillis() - ultimoUso) <= CUSTOM_ROLL_RETENTION_MS
    }

    fun atualizarQuantidadePorInput(raw: String) {
        val filtrado = raw.filter { it.isDigit() }.take(3)
        dadosPersonalizadosQuantidadeInput = filtrado
        filtrado.toIntOrNull()?.let {
            dadosPersonalizadosQuantidade = it.coerceIn(1, 300)
            dadosPersonalizadosQuantidadeInput = dadosPersonalizadosQuantidade.toString()
        }
    }

    fun atualizarFacesPorInput(raw: String) {
        val filtrado = raw.filter { it.isDigit() }.take(4)
        dadosPersonalizadosFacesInput = filtrado
        filtrado.toIntOrNull()?.let {
            dadosPersonalizadosFaces = it.coerceIn(1, 1000)
            dadosPersonalizadosFacesInput = dadosPersonalizadosFaces.toString()
        }
    }

    fun atualizarModificadorPorInput(raw: String) {
        var filtrado = raw.filterIndexed { index, c -> c.isDigit() || (index == 0 && c == '-') }
        if (filtrado.count { it == '-' } > 1) {
            filtrado = filtrado.replace("-", "")
        }
        if (filtrado.isNotEmpty() && !filtrado.startsWith("-")) {
            filtrado = filtrado.filter { it.isDigit() }
        }
        filtrado = if (filtrado.startsWith("-")) {
            "-" + filtrado.drop(1).filter { it.isDigit() }.take(3)
        } else {
            filtrado.filter { it.isDigit() }.take(3)
        }
        dadosPersonalizadosModificadorInput = filtrado
        filtrado.toIntOrNull()?.let {
            dadosPersonalizadosModificador = it.coerceIn(-999, 999)
            dadosPersonalizadosModificadorInput = dadosPersonalizadosModificador.toString()
        }
    }

    fun custoEnergiaFixo(energia: String?): Int? {
        val texto = energia?.trim().orEmpty()
        if (texto.isBlank()) return null
        return texto.toIntOrNull()
    }

    fun consumirEnergiaMagia(custoEnergia: Int) {
        if (custoEnergia <= 0) return
        val novoPf = (pfAtualRolagem - custoEnergia).coerceAtLeast(0)
        viewModel.atualizarPontosFadigaRolagemAtual(novoPf)
    }

    fun custoEnergiaComReducaoNh(custoBase: Int, nhBasico: Int): Int {
        return MagiaEnergiaRules.custoAjustadoPorNh(custoBase, nhBasico)
    }

    fun tratarCustoEnergiaAposRolagemMagia(magia: MagiaRollOption) {
        val nhBasico = magia.target
        val isTalisma = magia.definicaoId.equals("talisma", ignoreCase = true)
        val custoFixo = custoEnergiaFixo(magia.energia)
        if (custoFixo != null && !isTalisma) {
            consumirEnergiaMagia(custoEnergiaComReducaoNh(custoFixo, nhBasico))
            return
        }
        val energiaTexto = magia.energia?.trim().orEmpty()
        if (energiaTexto.isBlank() && !isTalisma) return
        magiaPendenteEnergia = magia
        energiaManualInput = custoFixo?.toString() ?: ""
        talismaMagiaVinculada = magia.encantamentoAlvo?.takeIf { it.isNotBlank() }
        showEnergiaManualDialog = true
    }

    fun ajustarPvRolagemPorSwipe(incrementar: Boolean) {
        val atual = pvAtualRolagem
        val novo = if (incrementar) atual + 1 else atual - 1
        viewModel.atualizarPontosVidaRolagemAtual(novo)
    }

    fun ajustarPfRolagemPorSwipe(incrementar: Boolean) {
        val atual = pfAtualRolagem
        val novo = if (incrementar) atual + 1 else atual - 1
        viewModel.atualizarPontosFadigaRolagemAtual(novo)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = horizontalPadding, top = 6.dp, end = horizontalPadding, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Button(
            onClick = { showEditarCanalDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (backendOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "EDITAR CANAL",
                    fontSize = if (isVerySmallScreen) 16.sp else 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = canalSelecionadoNome ?: "Selecionar canal de voz",
                    style = compactLabelStyle,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = appCardColors()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = outerCardVerticalPadding),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!isPraCegoVariant) {
                    Text(
                        text = "Deslize para cima/baixo em cada atributo para ajustar o modificador.",
                        style = compactLabelStyle,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                if (isPraCegoVariant) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = appCardColors()
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = innerCardVerticalPadding),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("PV: $pvFixoRolagem/$pvAtualRolagem", style = cardTitleStyle, fontWeight = FontWeight.SemiBold)
                                    TextButton(
                                        onClick = { showEditarPvRolagemDialog = true },
                                        modifier = Modifier.semantics { contentDescription = "Editar pontos de vida da rolagem" }
                                    ) {
                                        Text("Editar PV")
                                    }
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = appCardColors()
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = innerCardVerticalPadding),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("PF: $pfFixoRolagem/$pfAtualRolagem", style = cardTitleStyle, fontWeight = FontWeight.SemiBold)
                                    TextButton(
                                        onClick = { showEditarPfRolagemDialog = true },
                                        modifier = Modifier.semantics { contentDescription = "Editar pontos de fadiga da rolagem" }
                                    ) {
                                        Text("Editar PF")
                                    }
                                }
                            }
                        }
                        atributosRapidos.forEach { attr ->
                            val valor = p.getAtributo(attr)
                            val nomeAttr = atributoNomeCompleto(attr)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = appCardColors()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("$attr - $nomeAttr", style = cardTitleStyle, fontWeight = FontWeight.SemiBold)
                                    }
                                    Text(
                                        text = valor.toString(),
                                        modifier = Modifier
                                            .semantics {
                                                contentDescription = "Rolar $attr $valor"
                                            }
                                            .clickable {
                                                executarRolagem(
                                                    tipo = TipoTeste.ATRIBUTO,
                                                    contextoLabel = attr,
                                                    alvo = valor,
                                                    mod = 0
                                                )
                                            },
                                        textAlign = TextAlign.Center,
                                        style = statsNumberStyle,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(if (isTinyScreen) 4.dp else 8.dp)
                        ) {
                            atributosRapidos.forEach { attr ->
                                val valor = p.getAtributo(attr)
                                val modAttr = modificadoresAtributo[attr] ?: 0
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = innerCardVerticalPadding),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = attr,
                                        textAlign = TextAlign.Center,
                                        style = cardTitleStyle,
                                        maxLines = 1,
                                        overflow = TextOverflow.Clip,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = valor.toString(),
                                        modifier = Modifier
                                            .pointerInput(attr, modAttr) {
                                                var dragAcumulado = 0f
                                                val passoPx = 20f
                                                detectVerticalDragGestures(
                                                    onVerticalDrag = { change, dragAmount ->
                                                        change.consume()
                                                        dragAcumulado += dragAmount
                                                        while (abs(dragAcumulado) >= passoPx) {
                                                            val atual = modificadoresAtributo[attr] ?: 0
                                                            if (dragAcumulado < 0f) {
                                                                modificadoresAtributo[attr] = (atual + 1).coerceIn(-20, 20)
                                                                dragAcumulado += passoPx
                                                            } else {
                                                                modificadoresAtributo[attr] = (atual - 1).coerceIn(-20, 20)
                                                                dragAcumulado -= passoPx
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                            .clickable {
                                                executarRolagem(
                                                    tipo = TipoTeste.ATRIBUTO,
                                                    contextoLabel = attr,
                                                    alvo = valor,
                                                    mod = modAttr
                                                )
                                            },
                                        textAlign = TextAlign.Center,
                                        style = statsNumberStyle,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    if (modAttr != 0) {
                                        Text(
                                            text = "mod ${if (modAttr >= 0) "+$modAttr" else modAttr}",
                                            style = compactLabelStyle,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = appCardColors()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .pointerInput(pvAtualRolagem) {
                                            var dragAcumulado = 0f
                                            val passoPx = 20f
                                            detectVerticalDragGestures(
                                                onVerticalDrag = { change, dragAmount ->
                                                    change.consume()
                                                    dragAcumulado += dragAmount
                                                    while (abs(dragAcumulado) >= passoPx) {
                                                        ajustarPvRolagemPorSwipe(incrementar = dragAcumulado < 0f)
                                                        dragAcumulado += if (dragAcumulado < 0f) passoPx else -passoPx
                                                    }
                                                }
                                            )
                                        },
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("PV", style = cardTitleStyle, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "$pvFixoRolagem/$pvAtualRolagem",
                                        style = defenseNumberStyle,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = appCardColors()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .pointerInput(pfAtualRolagem) {
                                            var dragAcumulado = 0f
                                            val passoPx = 20f
                                            detectVerticalDragGestures(
                                                onVerticalDrag = { change, dragAmount ->
                                                    change.consume()
                                                    dragAcumulado += dragAmount
                                                    while (abs(dragAcumulado) >= passoPx) {
                                                        ajustarPfRolagemPorSwipe(incrementar = dragAcumulado < 0f)
                                                        dragAcumulado += if (dragAcumulado < 0f) passoPx else -passoPx
                                                    }
                                                }
                                            )
                                        },
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("PF", style = cardTitleStyle, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "$pfFixoRolagem/$pfAtualRolagem",
                                        style = defenseNumberStyle,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isPraCegoVariant) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = appCardColors()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = outerCardVerticalPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Modificador Global: ${if (modificadorGlobalPraCego >= 0) "+$modificadorGlobalPraCego" else "$modificadorGlobalPraCego"}",
                        style = cardTitleStyle,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(-5, -2, -1, 0, 1, 2, 5).forEach { delta ->
                            val label = if (delta == 0) "C" else if (delta > 0) "+$delta" else "$delta"
                            val descricao = when {
                                delta < 0 -> "Diminuir modificador em ${abs(delta)}"
                                delta > 0 -> "Aumentar modificador em $delta"
                                else -> "Limpar modificadores"
                            }
                            OutlinedButton(
                                onClick = {
                                    modificadorGlobalPraCego = if (delta == 0) 0 else (modificadorGlobalPraCego + delta).coerceIn(-999, 999)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics { contentDescription = descricao },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                            ) {
                                Text(label)
                            }
                        }
                    }
                }
            }
        }

        var showConfigAtaqueDialog by remember { mutableStateOf(false) }
        var showConfigDanoDialog by remember { mutableStateOf(false) }
        if (opcoesAtaque.isEmpty()) {
            Text(
                "Sem pericias para ataque. Adicione pericias de combate na aba Pericias.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(rowSpacing)
            ) {
                Button(
                    onClick = { showConfigAtaqueDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        "Ataque",
                        style = if (isVerySmallScreen) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                        maxLines = 1
                    )
                }
                Button(
                    onClick = { showConfigDanoDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        "Dano",
                        style = if (isVerySmallScreen) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            val modAtaqueAtual = if (isPraCegoVariant) 0 else modificadorAtaque
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (!isPraCegoVariant) {
                            Modifier.pointerInput(modAtaqueAtual) {
                                var dragAcumulado = 0f
                                val passoPx = 20f
                                detectVerticalDragGestures(
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        dragAcumulado += dragAmount
                                        while (abs(dragAcumulado) >= passoPx) {
                                            if (dragAcumulado < 0f) {
                                                modificadorAtaque = (modificadorAtaque + 1).coerceIn(-20, 20)
                                                dragAcumulado += passoPx
                                            } else {
                                                modificadorAtaque = (modificadorAtaque - 1).coerceIn(-20, 20)
                                                dragAcumulado -= passoPx
                                            }
                                        }
                                    }
                                )
                            }
                        } else {
                            Modifier
                        }
                    ),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(rowSpacing),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            colors = appCardColors()
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = innerCardPadding, vertical = innerCardVerticalPadding),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text(
                                    ataqueAtual?.contextLabel?.removePrefix("Ataque ") ?: "Ataque",
                                    style = cardTitleStyle,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "NH ${ataqueAtual?.target ?: "-"}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .semantics {
                                            contentDescription = "Rolar ${ataqueAtual?.contextLabel ?: "Ataque"}"
                                        }
                                        .clickable(enabled = ataqueAtual?.target != null) {
                                            executarRolagem(
                                                tipo = TipoTeste.ATAQUE,
                                                contextoLabel = ataqueAtual?.contextLabel ?: "Ataque",
                                                alvo = ataqueAtual?.target,
                                                mod = modAtaqueAtual
                                            )
                                        },
                                    style = defenseNumberStyle,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                                if (!isPraCegoVariant && modAtaqueAtual != 0) {
                                    Text(
                                        "mod ${if (modAtaqueAtual >= 0) "+$modAtaqueAtual" else "$modAtaqueAtual"}",
                                        style = compactLabelStyle,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            colors = appCardColors()
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = innerCardPadding, vertical = innerCardVerticalPadding),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text(
                                    fonteDanoAtual.label,
                                    style = cardTitleStyle,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (fonteDanoAtual.id == "st_base") {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        StDamageMode.entries.forEach { mode ->
                                            FilterChip(
                                                selected = stDamageMode == mode,
                                                onClick = { stDamageMode = mode },
                                                label = { Text(mode.label) }
                                            )
                                        }
                                    }
                                }
                                val danos = splitDamageEntries(fonteDanoAtual.damageExpression)
                                danos.forEach { danoLinha ->
                                    val danoRolavel = parseDamageExpression(danoLinha) != null
                                    Text(
                                        danoLinha,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .semantics {
                                                contentDescription = "Rolar dano ${fonteDanoAtual.contextLabel}: $danoLinha"
                                            }
                                            .clickable(enabled = danoRolavel) {
                                                executarRolagemDano(
                                                    contextoLabel = fonteDanoAtual.contextLabel,
                                                    danoExpr = danoLinha
                                                )
                                            },
                                        style = cardTitleStyle,
                                        color = if (danoRolavel) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (!isPraCegoVariant && modAtaqueAtual != 0) {
                                    Text(
                                        "mod ${if (modAtaqueAtual >= 0) "+$modAtaqueAtual" else "$modAtaqueAtual"}",
                                        style = compactLabelStyle,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showConfigAtaqueDialog) {
                var expandedAtaque by remember { mutableStateOf(false) }
                AlertDialog(
                    onDismissRequest = { showConfigAtaqueDialog = false },
                    title = { Text("Configurar Ataque") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ExposedDropdownMenuBox(
                                expanded = expandedAtaque,
                                onExpandedChange = { expandedAtaque = !expandedAtaque }
                            ) {
                                OutlinedTextField(
                                    value = ataqueAtual?.label ?: "Selecionar pericia",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Pericia de combate") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAtaque) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedAtaque,
                                    onDismissRequest = { expandedAtaque = false }
                                ) {
                                    opcoesAtaque.forEach { ataque ->
                                        DropdownMenuItem(
                                            text = { Text(ataque.label) },
                                            onClick = {
                                                ataqueSelecionadoKey = ataque.id
                                                expandedAtaque = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showConfigAtaqueDialog = false }) {
                            Text("Fechar")
                        }
                    }
                )
            }

            if (showConfigDanoDialog) {
                var expandedFonteDano by remember { mutableStateOf(false) }
                AlertDialog(
                    onDismissRequest = { showConfigDanoDialog = false },
                    title = { Text("Configurar Dano") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ExposedDropdownMenuBox(
                                expanded = expandedFonteDano,
                                onExpandedChange = { expandedFonteDano = !expandedFonteDano }
                            ) {
                                OutlinedTextField(
                                    value = fonteDanoAtual.label,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Arma / Fonte de dano") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFonteDano) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedFonteDano,
                                    onDismissRequest = { expandedFonteDano = false }
                                ) {
                                    fontesDano.forEach { fonte ->
                                        DropdownMenuItem(
                                            text = { Text(fonte.label) },
                                            onClick = {
                                                fonteDanoSelecionadaId = fonte.id
                                                expandedFonteDano = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showConfigDanoDialog = false }) {
                            Text("Fechar")
                        }
                    }
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                "DEFESAS",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(rowSpacing),
                verticalAlignment = Alignment.Top
            ) {
                listOf(DefenseType.ESQUIVA, DefenseType.APARA, DefenseType.BLOQUEIO).forEach { tipoDefesa ->
                    val defesa = defesasPorTipo[tipoDefesa]
                    val modDefesa = if (isPraCegoVariant) 0 else (modificadoresDefesa[tipoDefesa] ?: 0)
                    val nomeDefesa = when (tipoDefesa) {
                        DefenseType.ESQUIVA -> "Esquiva"
                        DefenseType.APARA -> "Apara"
                        DefenseType.BLOQUEIO -> "Bloqueio"
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (!isPraCegoVariant) {
                                        Modifier.pointerInput(tipoDefesa, modDefesa, defesa?.finalValue) {
                                            var dragAcumulado = 0f
                                            val passoPx = 20f
                                            detectVerticalDragGestures(
                                                onVerticalDrag = { change, dragAmount ->
                                                    change.consume()
                                                    dragAcumulado += dragAmount
                                                    while (abs(dragAcumulado) >= passoPx) {
                                                        val atual = modificadoresDefesa[tipoDefesa] ?: 0
                                                        if (dragAcumulado < 0f) {
                                                            modificadoresDefesa[tipoDefesa] = (atual + 1).coerceIn(-20, 20)
                                                            dragAcumulado += passoPx
                                                        } else {
                                                            modificadoresDefesa[tipoDefesa] = (atual - 1).coerceIn(-20, 20)
                                                            dragAcumulado -= passoPx
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    } else {
                                        Modifier
                                    }
                                ),
                            colors = appCardColors()
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = innerCardPadding, vertical = innerCardVerticalPadding),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text(
                                    nomeDefesa,
                                    style = cardTitleStyle,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    (defesa?.finalValue?.toString() ?: "-"),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .semantics {
                                            contentDescription = if (defesa != null) {
                                                "Rolar $nomeDefesa ${defesa.finalValue}"
                                            } else {
                                                "Defesa $nomeDefesa indisponÃ­vel"
                                            }
                                        }
                                        .clickable(enabled = defesa != null) {
                                            executarRolagem(
                                                tipo = TipoTeste.DEFESA,
                                                contextoLabel = "Defesa $nomeDefesa",
                                                alvo = defesa?.finalValue,
                                                mod = modDefesa
                                            )
                                        },
                                    style = defenseNumberStyle,
                                    fontWeight = FontWeight.Bold,
                                    color = if (defesa != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                if (!isPraCegoVariant && modDefesa != 0) {
                                    Text(
                                        "mod ${if (modDefesa >= 0) "+$modDefesa" else "$modDefesa"}",
                                        style = compactLabelStyle,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (viewModel.temAptidaoAstral) {
            val modMagiaAlma = if (isPraCegoVariant) 0 else modificadorMagiaAlma
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showMagiaAlmaDialog = true },
                    modifier = Modifier
                        .weight(2f)
                        .height(40.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        "Magia da Alma",
                        style = if (isVerySmallScreen) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                        maxLines = 1
                    )
                }
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (!isPraCegoVariant) {
                                Modifier.pointerInput(modificadorMagiaAlma, nivelMagiaDaAlma) {
                                    var dragAcumulado = 0f
                                    val passoPx = 20f
                                    detectVerticalDragGestures(
                                        onVerticalDrag = { change, dragAmount ->
                                            change.consume()
                                            dragAcumulado += dragAmount
                                            while (abs(dragAcumulado) >= passoPx) {
                                                if (dragAcumulado < 0f) {
                                                    modificadorMagiaAlma = (modificadorMagiaAlma + 1).coerceIn(-20, 20)
                                                    dragAcumulado += passoPx
                                                } else {
                                                    modificadorMagiaAlma = (modificadorMagiaAlma - 1).coerceIn(-20, 20)
                                                    dragAcumulado -= passoPx
                                                }
                                            }
                                        }
                                    )
                                }
                            } else {
                                Modifier
                            }
                        ),
                    colors = appCardColors()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = innerCardPadding, vertical = innerCardVerticalPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            "NH $nivelMagiaDaAlma",
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "Rolar Magia da Alma" }
                                .clickable {
                                    executarRolagem(
                                        tipo = TipoTeste.MAGIA,
                                        contextoLabel = "Magia da Alma",
                                        alvo = nivelMagiaDaAlma,
                                        mod = modMagiaAlma
                                    )
                                },
                            style = defenseNumberStyle,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        if (!isPraCegoVariant && modMagiaAlma != 0) {
                            Text(
                                "mod ${if (modMagiaAlma >= 0) "+$modMagiaAlma" else "$modMagiaAlma"}",
                                style = compactLabelStyle,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                if (!reterRolagemPersonalizadaAindaValida()) {
                    resetarRolagemPersonalizadaParaPadrao()
                }
                showRolagemPersonalizadaDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
            val exprAtual = buildString {
                if (reterRolagemPersonalizadaAindaValida()) {
                    append("${dadosPersonalizadosQuantidade}d${dadosPersonalizadosFaces}")
                    if (dadosPersonalizadosModificador > 0) append("+$dadosPersonalizadosModificador")
                    if (dadosPersonalizadosModificador < 0) append(dadosPersonalizadosModificador)
                } else {
                    append("1d6")
                }
            }
            Text(
                "Rolagem Personalizada ($exprAtual)",
                style = if (isVerySmallScreen) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Button(
            onClick = { showPericiasDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                "Pericias",
                style = if (isVerySmallScreen) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                maxLines = 1
            )
        }

        if (opcoesTecnica.isNotEmpty()) {
            Button(
                onClick = { showTecnicasDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    "Tecnicas",
                    style = if (isVerySmallScreen) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                    maxLines = 1
                )
            }
        }

        if (viewModel.temAptidaoMagica) {
            Button(
                onClick = { showMagiasDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    "Magias",
                    style = if (isVerySmallScreen) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                    maxLines = 1
                )
            }
        }

        if (showPericiasDialog) {
            Dialog(
                onDismissRequest = { showPericiasDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(0.dp),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Pericias",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        if (opcoesPericia.isEmpty()) {
                            Text(
                                "Sem pericias configuradas na aba Pericias.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                opcoesPericia.forEach { pericia ->
                                    val modPericia = if (isPraCegoVariant) 0 else (modificadoresPericia[pericia.id] ?: 0)
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = appCardColors()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = innerCardPadding, vertical = innerCardVerticalPadding),
                                            verticalArrangement = Arrangement.spacedBy(1.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(
                                                    modifier = Modifier.weight(2f),
                                                    horizontalAlignment = Alignment.Start,
                                                    verticalArrangement = Arrangement.spacedBy(1.dp)
                                                ) {
                                                    val descricaoPericia = pericia.descricao.ifBlank { "Sem descrição disponível." }
                                                    Text(
                                                        pericia.nome,
                                                        style = defenseNumberStyle,
                                                        fontWeight = FontWeight.SemiBold,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                descricaoDialog = RollDescricaoDialog(
                                                                    titulo = "Descrição: ${pericia.nome}",
                                                                    texto = descricaoPericia
                                                                )
                                                            }
                                                            .semantics {
                                                                if (isPraCegoVariant) {
                                                                    contentDescription = "Nome da perícia ${pericia.nome}. Toque para abrir descrição."
                                                                }
                                                            },
                                                        textAlign = TextAlign.Start,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    if (pericia.especializacao.isNotBlank()) {
                                                        Text(
                                                            pericia.especializacao,
                                                            style = defenseNumberStyle,
                                                            modifier = Modifier.fillMaxWidth(),
                                                            textAlign = TextAlign.Start,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                                Text(
                                                    "NH ${pericia.target}",
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .then(
                                                            if (!isPraCegoVariant) {
                                                                Modifier.pointerInput(pericia.id, modPericia) {
                                                                    var dragAcumulado = 0f
                                                                    val passoPx = 20f
                                                                    detectVerticalDragGestures(
                                                                        onVerticalDrag = { change, dragAmount ->
                                                                            change.consume()
                                                                            dragAcumulado += dragAmount
                                                                            while (abs(dragAcumulado) >= passoPx) {
                                                                                val atual = modificadoresPericia[pericia.id] ?: 0
                                                                                if (dragAcumulado < 0f) {
                                                                                    modificadoresPericia[pericia.id] = (atual + 1).coerceIn(-20, 20)
                                                                                    dragAcumulado += passoPx
                                                                                } else {
                                                                                    modificadoresPericia[pericia.id] = (atual - 1).coerceIn(-20, 20)
                                                                                    dragAcumulado -= passoPx
                                                                                }
                                                                            }
                                                                        }
                                                                    )
                                                                }
                                                            } else {
                                                                Modifier
                                                            }
                                                        )
                                                        .semantics {
                                                            contentDescription = "Rolar pericia ${pericia.nome}"
                                                        }
                                                        .clickable {
                                                            executarRolagem(
                                                                tipo = TipoTeste.PERICIA,
                                                                contextoLabel = pericia.contextLabel,
                                                                alvo = pericia.target,
                                                                mod = modPericia
                                                            )
                                                            showPericiasDialog = false
                                                        },
                                                    style = defenseNumberStyle,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    textAlign = TextAlign.End,
                                                    maxLines = 1
                                                )
                                            }
                                            if (!isPraCegoVariant && modPericia != 0) {
                                                Text(
                                                    "mod ${if (modPericia >= 0) "+$modPericia" else "$modPericia"}",
                                                    style = compactLabelStyle,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    textAlign = TextAlign.End,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showPericiasDialog = false }) {
                                Text("Fechar")
                            }
                        }
                    }
                }
            }
        }

        if (showRolagemPersonalizadaDialog) {
            Dialog(
                onDismissRequest = { showRolagemPersonalizadaDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(0.dp),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "Rolagem Personalizada",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Text(
                            "Deslize para cima/baixo em cada card para ajustar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .then(
                                        if (!isPraCegoVariant) {
                                            Modifier.pointerInput(dadosPersonalizadosQuantidade) {
                                                var dragAcumulado = 0f
                                                val passoPx = 20f
                                                detectVerticalDragGestures(
                                                    onVerticalDrag = { change, dragAmount ->
                                                        change.consume()
                                                        dragAcumulado += dragAmount
                                                        while (abs(dragAcumulado) >= passoPx) {
                                                            if (dragAcumulado < 0f) {
                                                                dadosPersonalizadosQuantidade = (dadosPersonalizadosQuantidade + 1).coerceIn(1, 300)
                                                                dadosPersonalizadosQuantidadeInput = dadosPersonalizadosQuantidade.toString()
                                                                dragAcumulado += passoPx
                                                            } else {
                                                                dadosPersonalizadosQuantidade = (dadosPersonalizadosQuantidade - 1).coerceIn(1, 300)
                                                                dadosPersonalizadosQuantidadeInput = dadosPersonalizadosQuantidade.toString()
                                                                dragAcumulado -= passoPx
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                        } else {
                                            Modifier
                                        }
                                    ),
                                colors = appCardColors()
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Qtd", style = MaterialTheme.typography.labelSmall)
                                    Text("$dadosPersonalizadosQuantidade", style = defenseNumberStyle, fontWeight = FontWeight.Bold)
                                }
                            }
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .then(
                                        if (!isPraCegoVariant) {
                                            Modifier.pointerInput(dadosPersonalizadosFaces) {
                                                var dragAcumulado = 0f
                                                val passoPx = 20f
                                                detectVerticalDragGestures(
                                                    onVerticalDrag = { change, dragAmount ->
                                                        change.consume()
                                                        dragAcumulado += dragAmount
                                                        while (abs(dragAcumulado) >= passoPx) {
                                                            if (dragAcumulado < 0f) {
                                                                dadosPersonalizadosFaces = (dadosPersonalizadosFaces + 1).coerceIn(1, 1000)
                                                                dadosPersonalizadosFacesInput = dadosPersonalizadosFaces.toString()
                                                                dragAcumulado += passoPx
                                                            } else {
                                                                dadosPersonalizadosFaces = (dadosPersonalizadosFaces - 1).coerceIn(1, 1000)
                                                                dadosPersonalizadosFacesInput = dadosPersonalizadosFaces.toString()
                                                                dragAcumulado -= passoPx
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                        } else {
                                            Modifier
                                        }
                                    ),
                                colors = appCardColors()
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Faces", style = MaterialTheme.typography.labelSmall)
                                    Text("$dadosPersonalizadosFaces", style = defenseNumberStyle, fontWeight = FontWeight.Bold)
                                }
                            }
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .then(
                                        if (!isPraCegoVariant) {
                                            Modifier.pointerInput(dadosPersonalizadosModificador) {
                                                var dragAcumulado = 0f
                                                val passoPx = 20f
                                                detectVerticalDragGestures(
                                                    onVerticalDrag = { change, dragAmount ->
                                                        change.consume()
                                                        dragAcumulado += dragAmount
                                                        while (abs(dragAcumulado) >= passoPx) {
                                                            if (dragAcumulado < 0f) {
                                                                dadosPersonalizadosModificador = (dadosPersonalizadosModificador + 1).coerceIn(-999, 999)
                                                                dadosPersonalizadosModificadorInput = dadosPersonalizadosModificador.toString()
                                                                dragAcumulado += passoPx
                                                            } else {
                                                                dadosPersonalizadosModificador = (dadosPersonalizadosModificador - 1).coerceIn(-999, 999)
                                                                dadosPersonalizadosModificadorInput = dadosPersonalizadosModificador.toString()
                                                                dragAcumulado -= passoPx
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                        } else {
                                            Modifier
                                        }
                                    ),
                                colors = appCardColors()
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Mod", style = MaterialTheme.typography.labelSmall)
                                    val modTexto = when {
                                        dadosPersonalizadosModificador > 0 -> "+$dadosPersonalizadosModificador"
                                        else -> dadosPersonalizadosModificador.toString()
                                    }
                                    Text(modTexto, style = defenseNumberStyle, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = dadosPersonalizadosQuantidadeInput,
                                onValueChange = { atualizarQuantidadePorInput(it) },
                                modifier = Modifier.weight(1f),
                                label = { Text("Qtd") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = dadosPersonalizadosFacesInput,
                                onValueChange = { atualizarFacesPorInput(it) },
                                modifier = Modifier.weight(1f),
                                label = { Text("Faces") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = dadosPersonalizadosModificadorInput,
                                onValueChange = { atualizarModificadorPorInput(it) },
                                modifier = Modifier.weight(1f),
                                label = { Text("Mod") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        val expressaoPersonalizada = buildString {
                            append("${dadosPersonalizadosQuantidade}d${dadosPersonalizadosFaces}")
                            if (dadosPersonalizadosModificador > 0) append("+$dadosPersonalizadosModificador")
                            if (dadosPersonalizadosModificador < 0) append(dadosPersonalizadosModificador)
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = appCardColors()
                        ) {
                            Text(
                                "ExpressÃ£o: $expressaoPersonalizada",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                executarRolagemPersonalizada(
                                    contextoLabel = "Rolagem Personalizada",
                                    quantidade = dadosPersonalizadosQuantidade,
                                    faces = dadosPersonalizadosFaces,
                                    mod = dadosPersonalizadosModificador
                                )
                                ultimoUsoRolagemPersonalizadaMs = System.currentTimeMillis()
                                showRolagemPersonalizadaDialog = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                        ) {
                            Text(
                                "Rolar $expressaoPersonalizada",
                                style = if (isVerySmallScreen) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                                maxLines = 1
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showRolagemPersonalizadaDialog = false }) {
                                Text("Fechar")
                            }
                        }
                    }
                }
            }
        }

        if (showMagiaAlmaDialog && viewModel.temAptidaoAstral) {
            Dialog(
                onDismissRequest = { showMagiaAlmaDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(0.dp),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "Magia da Alma",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            SOUL_ASPECT_OPTIONS.forEach { aspecto ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { aspectoMagiaAlmaSelecionado = aspecto },
                                    colors = appCardColors()
                                ) {
                                    Text(
                                        text = aspecto.nome,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showMagiaAlmaDialog = false }) {
                                Text("Fechar")
                            }
                        }
                    }
                }
            }
        }

        aspectoMagiaAlmaSelecionado?.let { aspecto ->
            AlertDialog(
                onDismissRequest = { aspectoMagiaAlmaSelecionado = null },
                title = { Text(aspecto.nome) },
                text = {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 460.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(aspecto.descricao, style = MaterialTheme.typography.bodyMedium)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { aspectoMagiaAlmaSelecionado = null }) {
                        Text("Fechar")
                    }
                }
            )
        }

        if (showMagiasDialog && viewModel.temAptidaoMagica) {
            Dialog(
                onDismissRequest = { showMagiasDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(0.dp),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Magias",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        if (opcoesMagia.isEmpty()) {
                            Text(
                                "Sem magias configuradas na aba Magia.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                opcoesMagia.forEach { magia ->
                                    val modMagia = if (isPraCegoVariant) 0 else (modificadoresMagia[magia.id] ?: 0)
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = appCardColors()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = innerCardPadding, vertical = innerCardVerticalPadding),
                                            verticalArrangement = Arrangement.spacedBy(1.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    magia.nome,
                                                    style = defenseNumberStyle,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier
                                                        .weight(2f)
                                                        .clickable {
                                                            val descricaoMagia = magia.descricao.ifBlank { "Sem descrição disponível." }
                                                            descricaoDialog = RollDescricaoDialog(
                                                                titulo = "Descrição: ${magia.nome}",
                                                                texto = descricaoMagia
                                                            )
                                                        }
                                                        .semantics {
                                                            if (isPraCegoVariant) {
                                                                contentDescription = "Nome da magia ${magia.nome}. Toque para abrir descrição."
                                                            }
                                                        },
                                                    textAlign = TextAlign.Start,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    "NH ${magia.target}",
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .then(
                                                            if (!isPraCegoVariant) {
                                                                Modifier.pointerInput(magia.id, modMagia) {
                                                                    var dragAcumulado = 0f
                                                                    val passoPx = 20f
                                                                    detectVerticalDragGestures(
                                                                        onVerticalDrag = { change, dragAmount ->
                                                                            change.consume()
                                                                            dragAcumulado += dragAmount
                                                                            while (abs(dragAcumulado) >= passoPx) {
                                                                                val atual = modificadoresMagia[magia.id] ?: 0
                                                                                if (dragAcumulado < 0f) {
                                                                                    modificadoresMagia[magia.id] = (atual + 1).coerceIn(-20, 20)
                                                                                    dragAcumulado += passoPx
                                                                                } else {
                                                                                    modificadoresMagia[magia.id] = (atual - 1).coerceIn(-20, 20)
                                                                                    dragAcumulado -= passoPx
                                                                                }
                                                                            }
                                                                        }
                                                                    )
                                                                }
                                                            } else {
                                                                Modifier
                                                            }
                                                        )
                                                        .semantics {
                                                            contentDescription = "Rolar magia ${magia.nome}"
                                                        }
                                                        .clickable {
                                                            executarRolagem(
                                                                tipo = TipoTeste.MAGIA,
                                                                contextoLabel = magia.contextLabel,
                                                                alvo = magia.target,
                                                                mod = modMagia
                                                            )
                                                            tratarCustoEnergiaAposRolagemMagia(magia)
                                                            showMagiasDialog = false
                                                        },
                                                    style = defenseNumberStyle,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    textAlign = TextAlign.End,
                                                    maxLines = 1
                                                )
                                            }
                                            magia.duracao?.takeIf { it.isNotBlank() }?.let { duracao ->
                                                Text(
                                                    "Duracao: $duracao",
                                                    style = compactLabelStyle,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .semantics {
                                                            if (isPraCegoVariant) contentDescription = "Duracao da magia ${magia.nome}: $duracao"
                                                        },
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            magia.energia?.takeIf { it.isNotBlank() }?.let { energia ->
                                                Text(
                                                    "Energia: $energia",
                                                    style = compactLabelStyle,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .semantics {
                                                            if (isPraCegoVariant) contentDescription = "Energia da magia ${magia.nome}: $energia"
                                                        },
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            magia.tempoOperacao?.takeIf { it.isNotBlank() }?.let { tempo ->
                                                Text(
                                                    "Tempo de operacao: $tempo",
                                                    style = compactLabelStyle,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .semantics {
                                                            if (isPraCegoVariant) contentDescription = "Tempo de operacao da magia ${magia.nome}: $tempo"
                                                        },
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            if (!isPraCegoVariant && modMagia != 0) {
                                                Text(
                                                    "mod ${if (modMagia >= 0) "+$modMagia" else "$modMagia"}",
                                                    style = compactLabelStyle,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    textAlign = TextAlign.End,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showMagiasDialog = false }) {
                                Text("Fechar")
                            }
                        }
                    }
                }
            }
        }

        if (showTecnicasDialog) {
            Dialog(
                onDismissRequest = { showTecnicasDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(0.dp),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Tecnicas",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        if (opcoesTecnica.isEmpty()) {
                            Text(
                                "Sem tecnicas configuradas na aba Tecnicas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                opcoesTecnica.forEach { tecnica ->
                                    val modTecnica = if (isPraCegoVariant) 0 else (modificadoresTecnica[tecnica.id] ?: 0)
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = appCardColors()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = innerCardPadding, vertical = innerCardVerticalPadding),
                                            verticalArrangement = Arrangement.spacedBy(1.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(
                                                    modifier = Modifier.weight(2f),
                                                    horizontalAlignment = Alignment.Start,
                                                    verticalArrangement = Arrangement.spacedBy(1.dp)
                                                ) {
                                                    val descricaoTecnica = tecnica.descricao.ifBlank { "Sem descrição disponível." }
                                                    Text(
                                                        tecnica.nome,
                                                        style = defenseNumberStyle,
                                                        fontWeight = FontWeight.SemiBold,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                descricaoDialog = RollDescricaoDialog(
                                                                    titulo = "Descrição: ${tecnica.nome}",
                                                                    texto = descricaoTecnica
                                                                )
                                                            }
                                                            .semantics {
                                                                if (isPraCegoVariant) {
                                                                    contentDescription = "Nome da técnica ${tecnica.nome}. Toque para abrir descrição."
                                                                }
                                                            },
                                                        textAlign = TextAlign.Start,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    if (tecnica.periciaBaseNome.isNotBlank()) {
                                                        Text(
                                                            tecnica.periciaBaseNome,
                                                            style = compactLabelStyle,
                                                            modifier = Modifier.fillMaxWidth(),
                                                            textAlign = TextAlign.Start,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                                Text(
                                                    "NH ${tecnica.target ?: "-"}",
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .then(
                                                            if (!isPraCegoVariant && tecnica.target != null) {
                                                                Modifier.pointerInput(tecnica.id, modTecnica) {
                                                                    var dragAcumulado = 0f
                                                                    val passoPx = 20f
                                                                    detectVerticalDragGestures(
                                                                        onVerticalDrag = { change, dragAmount ->
                                                                            change.consume()
                                                                            dragAcumulado += dragAmount
                                                                            while (abs(dragAcumulado) >= passoPx) {
                                                                                val atual = modificadoresTecnica[tecnica.id] ?: 0
                                                                                if (dragAcumulado < 0f) {
                                                                                    modificadoresTecnica[tecnica.id] = (atual + 1).coerceIn(-20, 20)
                                                                                    dragAcumulado += passoPx
                                                                                } else {
                                                                                    modificadoresTecnica[tecnica.id] = (atual - 1).coerceIn(-20, 20)
                                                                                    dragAcumulado -= passoPx
                                                                                }
                                                                            }
                                                                        }
                                                                    )
                                                                }
                                                            } else {
                                                                Modifier
                                                            }
                                                        )
                                                        .semantics {
                                                            contentDescription = if (tecnica.target == null) {
                                                                "Tecnica ${tecnica.nome} sem nivel disponivel"
                                                            } else {
                                                                "Rolar tecnica ${tecnica.nome}"
                                                            }
                                                        }
                                                        .clickable(enabled = tecnica.target != null) {
                                                            executarRolagem(
                                                                tipo = TipoTeste.TECNICA,
                                                                contextoLabel = tecnica.contextLabel,
                                                                alvo = tecnica.target,
                                                                mod = modTecnica
                                                            )
                                                            showTecnicasDialog = false
                                                        },
                                                    style = defenseNumberStyle,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    textAlign = TextAlign.End,
                                                    maxLines = 1
                                                )
                                            }
                                            if (!isPraCegoVariant && modTecnica != 0 && tecnica.target != null) {
                                                Text(
                                                    "mod ${if (modTecnica >= 0) "+$modTecnica" else "$modTecnica"}",
                                                    style = compactLabelStyle,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    textAlign = TextAlign.End,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showTecnicasDialog = false }) {
                                Text("Fechar")
                            }
                        }
                    }
                }
            }
        }

        descricaoDialog?.let { dialog ->
            AlertDialog(
                onDismissRequest = { descricaoDialog = null },
                title = { Text(dialog.titulo) },
                text = {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 460.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(dialog.texto, style = MaterialTheme.typography.bodyMedium)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { descricaoDialog = null }) {
                        Text("Fechar")
                    }
                }
            )
        }

        if (showEnergiaManualDialog && magiaPendenteEnergia != null) {
            val magiaEnergia = magiaPendenteEnergia!!
            val exigeVinculoTalisma = magiaEnergia.definicaoId.equals("talisma", ignoreCase = true)
            var menuTalismaExpandido by remember { mutableStateOf(false) }
            AlertDialog(
                onDismissRequest = {
                    showEnergiaManualDialog = false
                    magiaPendenteEnergia = null
                    talismaMagiaVinculada = null
                },
                title = { Text("Gasto de energia") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Magia: ${magiaEnergia.nome}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        magiaEnergia.energia?.takeIf { it.isNotBlank() }?.let { energia ->
                            Text(
                                "Energia da ficha: $energia",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (exigeVinculoTalisma) {
                            Text(
                                "TalismÃ£: escolha uma magia do repertÃ³rio para finalizar a rolagem.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            ExposedDropdownMenuBox(
                                expanded = menuTalismaExpandido,
                                onExpandedChange = { menuTalismaExpandido = !menuTalismaExpandido },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = talismaMagiaVinculada.orEmpty(),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("TalismÃ£: magia vinculada") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuTalismaExpandido)
                                    },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = menuTalismaExpandido,
                                    onDismissRequest = { menuTalismaExpandido = false }
                                ) {
                                    repertorioParaTalisma.forEach { nomeMagia ->
                                        DropdownMenuItem(
                                            text = { Text(nomeMagia) },
                                            onClick = {
                                                talismaMagiaVinculada = nomeMagia
                                                menuTalismaExpandido = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        OutlinedTextField(
                            value = energiaManualInput,
                            onValueChange = { raw ->
                                energiaManualInput = raw.filter { it.isDigit() }.take(4)
                            },
                            label = { Text("Custo base da magia agora") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    if (isPraCegoVariant) contentDescription = "Informar energia gasta para a magia ${magiaEnergia.nome}"
                                }
                        )
                        Text(
                            "PF da rolagem atual: $pfAtualRolagem",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        energiaManualInput.toIntOrNull()?.let { custoBase ->
                            val reducao = MagiaEnergiaRules.reducaoPorNh(magiaEnergia.target)
                            val custoFinal = custoEnergiaComReducaoNh(custoBase, magiaEnergia.target)
                            Text(
                                "Reducao por NH ${magiaEnergia.target}: -$reducao | custo final: $custoFinal",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            energiaManualInput.toIntOrNull()?.let { custoBase ->
                                val custoFinal = custoEnergiaComReducaoNh(custoBase, magiaEnergia.target)
                                consumirEnergiaMagia(custoFinal)
                            }
                            showEnergiaManualDialog = false
                            magiaPendenteEnergia = null
                            energiaManualInput = ""
                            talismaMagiaVinculada = null
                        },
                        enabled = energiaManualInput.toIntOrNull() != null &&
                            (!exigeVinculoTalisma || !talismaMagiaVinculada.isNullOrBlank())
                    ) {
                        Text("Aplicar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showEnergiaManualDialog = false
                            magiaPendenteEnergia = null
                            energiaManualInput = ""
                            talismaMagiaVinculada = null
                        }
                    ) {
                        Text("Ignorar")
                    }
                }
            )
        }

        if (showEditarPvRolagemDialog) {
            AlertDialog(
                onDismissRequest = { showEditarPvRolagemDialog = false },
                title = { Text("Editar PV da Rolagem") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("PV fixo: $pvFixoRolagem | Limite atual: 0 a $maxPvRolagem")
                        OutlinedTextField(
                            value = pvAtualInput,
                            onValueChange = { raw ->
                                val filtrado = raw.filter { it.isDigit() }.take(4)
                                pvAtualInput = filtrado
                            },
                            label = { Text("PV atual") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "Campo de pontos de vida da rolagem" }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val valor = pvAtualInput.toIntOrNull() ?: pvAtualRolagem
                        viewModel.atualizarPontosVidaRolagemAtual(valor)
                        showEditarPvRolagemDialog = false
                    }) {
                        Text("Salvar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        pvAtualInput = pvAtualRolagem.toString()
                        showEditarPvRolagemDialog = false
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (showEditarPfRolagemDialog) {
            AlertDialog(
                onDismissRequest = { showEditarPfRolagemDialog = false },
                title = { Text("Editar PF da Rolagem") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("PF fixo: $pfFixoRolagem | Minimo atual: 0")
                        OutlinedTextField(
                            value = pfAtualInput,
                            onValueChange = { raw ->
                                val filtrado = raw.filter { it.isDigit() }.take(4)
                                pfAtualInput = filtrado
                            },
                            label = { Text("PF atual") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "Campo de pontos de fadiga da rolagem" }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val valor = pfAtualInput.toIntOrNull() ?: pfAtualRolagem
                        viewModel.atualizarPontosFadigaRolagemAtual(valor)
                        showEditarPfRolagemDialog = false
                    }) {
                        Text("Salvar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        pfAtualInput = pfAtualRolagem.toString()
                        showEditarPfRolagemDialog = false
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        SectionCard(title = "Historico da Sessao") {
            if (historico.isEmpty()) {
                Text(
                    "Nenhuma rolagem ainda.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                historico.forEachIndexed { index, item ->
                    Text(
                        item.texto,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.statusEnvio == "erro") {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    item.statusEnvio?.let { status ->
                        Text(
                            "envio: $status",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (status == "erro") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (item.statusEnvio == "erro" && !item.detalheErro.isNullOrBlank()) {
                        Text(
                            "detalhe: ${item.detalheErro}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val envio = viewModel.enviarRolagemDiscord(item.payload)
                                    val atualizado = item.copy(
                                        statusEnvio = if (envio.enviado) "enviado" else "erro",
                                        detalheErro = envio.detalhe
                                    )
                                    if (index in historico.indices) {
                                        historico[index] = atualizado
                                    }
                                }
                            }
                        ) {
                            Text("Reenviar")
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showEditarCanalDialog) {
        var expandedCanal by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showEditarCanalDialog = false },
            title = { Text("Canal de envio Discord") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = expandedCanal,
                        onExpandedChange = { expandedCanal = !expandedCanal }
                    ) {
                        val canalLabel = when {
                            canaisCarregando -> "Carregando canais..."
                            !canalSelecionadoNome.isNullOrBlank() -> canalSelecionadoNome
                            else -> "Selecionar canal de voz"
                        }
                        OutlinedTextField(
                            value = canalLabel,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCanal) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCanal,
                            onDismissRequest = { expandedCanal = false }
                        ) {
                            canaisDiscord.forEach { canal ->
                                DropdownMenuItem(
                                    text = { Text("${canal.guildName} / ${canal.name}") },
                                    onClick = {
                                        viewModel.selecionarCanalDiscord(canal)
                                        expandedCanal = false
                                    }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.atualizarCanaisDiscord() },
                        enabled = !canaisCarregando,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (backendOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = if (canaisCarregando) "ATUALIZANDO..." else "ATUALIZAR CANAL",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!canaisErro.isNullOrBlank()) {
                        Text(
                            "Erro ao carregar canais: $canaisErro",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEditarCanalDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }
}



