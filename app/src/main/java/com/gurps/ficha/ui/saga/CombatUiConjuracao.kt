package com.gurps.ficha.ui.saga

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.combat.*
import com.gurps.ficha.viewmodel.FichaViewModel
import com.gurps.ficha.viewmodel.delegates.CombatenteUi
import com.gurps.ficha.viewmodel.delegates.FaixaDistancia

/**
 * Lote REFACTOR-3: UI de CONJURACAO de magia, recortada de CombatUi.kt (que passava de 2000
 * linhas). Reune o dialogo de conjurar em 2 passos, o painel de ritual (C12) e os gatilhos do
 * modo de faixas (P12 Conjurar, C11 encolher zona). Composables PUROS de tela -- nenhuma regra
 * nova, so movidos. Mesmo pacote, entao veem os helpers `internal` de CombatUi (OpcaoRadio).
 */

/**
 * Lote MA-3a: seletor de conjuração. Escolhe a magia, o alvo (um inimigo ou "em mim mesmo") e, para
 * Projéteis, quanta energia investir (1d de dano por ponto, teto na Aptidão Mágica). Conjurar é a
 * manobra Concentrar — gasta o turno. Efeitos bespoke são narrados pelo Mestre (MA-4).
 */
/**
 * Lote UX-1 (pedido do usuário no aparelho): conjurar em DOIS PASSOS.
 *
 * Antes era um diálogo único: a lista inteira de magias em rádios e, lá no fim, alvo/dano/energia/PV.
 * Com muitas magias (o usuário citou o caso de 200) rolar até o fim a cada conjuração trava o ritmo
 * do combate. Agora: **passo 1** = escolher a magia (cada uma é um BOTÃO, com busca); **passo 2** =
 * um diálogo só com os parâmetros daquela magia.
 *
 * Lote UI-MAG-1: **segurar um card por 2 segundos abre a descrição da magia** (regra fiel do livro +
 * ficha técnica), sem sair do combate. Era o atrito relatado no aparelho: para lembrar o que a magia
 * faz o jogador tinha de fechar a luta, ir à aba de Magias e abrir a magia lá.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SubDialogoConjurar(
    magias: List<com.gurps.ficha.viewmodel.delegates.MagiaConjuravelUi>,
    inimigos: List<CombatenteUi>,
    onConjurar: (magiaId: String, alvoId: String?, energia: Int, pvQueimar: Int, causaDano: Boolean,
                 ritual: com.gurps.ficha.domain.magic.RitualDeConjuracao) -> Unit,
    onCarregarProjetil: (magiaId: String, energia: Int) -> Unit = { _, _ -> },
    onMirarArea: (magiaId: String, raio: Int, energia: Int, pvQueimar: Int, causaDano: Boolean,
                  ritual: com.gurps.ficha.domain.magic.RitualDeConjuracao) -> Unit,
    onFechar: () -> Unit,
) {
    // null = ainda escolhendo a magia (passo 1); != null = configurando os parâmetros (passo 2).
    var magiaSel by remember { mutableStateOf<com.gurps.ficha.viewmodel.delegates.MagiaConjuravelUi?>(null) }
    // Lote C12: ritual alternativo (Magia p.9). Começa no PADRÃO — quem não mexer não é penalizado.
    var ritual by remember { mutableStateOf(com.gurps.ficha.domain.magic.RitualDeConjuracao()) }
    var busca by remember { mutableStateOf("") }
    // Lote UI-MAG-1: magia cuja DESCRIÇÃO está aberta (segurar o card 2s). Fica FORA do `if` do passo
    // 1 para o pop-up poder ser aberto também do passo 2 (consultar a regra antes de confirmar).
    var descricaoDe by remember { mutableStateOf<com.gurps.ficha.viewmodel.delegates.MagiaConjuravelUi?>(null) }

    // Pop-up de consulta: o MESMO componente da aba de Magias, agora com barra de rolagem.
    descricaoDe?.let { d ->
        com.gurps.ficha.ui.features.magic.DialogoDescricaoMagia(
            nome = d.nome,
            descricao = d.descricao,
            fichaTecnica = buildList {
                add(d.classe); add("NH ${d.nhBasico}"); add("custo ${d.custoTexto}")
                d.duracao?.let { add("duração $it") }
                d.tempoOperacao?.let { add("operação $it") }
                d.pagina?.let { add("p.$it") }
            }.joinToString(" · "),
            onFechar = { descricaoDe = null },
        )
    }

    val sel = magiaSel
    if (sel == null) {
        val filtradas = remember(busca, magias) {
            if (busca.isBlank()) magias
            else magias.filter { com.gurps.ficha.domain.filters.CatalogFilters.contemBusca(it.nome, busca) }
        }
        AlertDialog(
            onDismissRequest = onFechar,
            title = { Text("Conjurar magia") },
            text = {
                Column {
                    Text("Conjurar é a manobra Concentrar — gasta o turno.",
                        style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    // Lote UI-MAG-1b: a dica precisa estar À VISTA — o recurso de segurar o card não
                    // se descobre sozinho. Mesmo estilo da linha acima (bodySmall itálico).
                    Text("Pressione por 2 segundos o card da magia para ler a descrição dela!",
                        style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    // Busca: o que realmente resolve uma lista longa (o caso das 200 magias).
                    if (magias.size > 6) {
                        OutlinedTextField(
                            value = busca, onValueChange = { busca = it },
                            label = { Text("Buscar magia") }, singleLine = true,
                            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Buscar magia pelo nome" }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    if (filtradas.isEmpty()) {
                        Text("Nenhuma magia com esse nome.", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        filtradas.forEach { m ->
                            // Cada magia é um BOTÃO: um toque já leva ao passo 2 (sem rolar até o fim).
                            // Lote UI-MAG-1: SEGURAR 2s abre a descrição do livro (sem sair do combate).
                            // Por isso é um Surface com combinedClickable, e não um OutlinedButton: o
                            // Button do Material3 não expõe onLongClick (o visual foi preservado).
                            Surface(
                                shape = ButtonDefaults.outlinedShape,
                                color = Color.Transparent,
                                contentColor = if (m.castavel) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                border = ButtonDefaults.outlinedButtonBorder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .combinedClickable(
                                        enabled = m.castavel || !m.descricao.isNullOrBlank(),
                                        onClick = { if (m.castavel) magiaSel = m },
                                        onLongClick = { if (!m.descricao.isNullOrBlank()) descricaoDe = m },
                                        onLongClickLabel = "Ler a descrição de ${m.nome}",
                                    )
                                    .semantics {
                                        contentDescription = "Escolher ${m.nome}, ${m.classe}, NH ${m.nhBasico}, custo ${m.custoTexto}" +
                                            (if (!m.castavel) ", indisponível: ${m.motivo}" else "") +
                                            (if (!m.descricao.isNullOrBlank()) ". Segure para ler a descrição." else "")
                                    }
                            ) {
                                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                                    Text(m.nome, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "${m.classe} · NH ${m.nhBasico} · ${m.custoTexto}" + if (!m.castavel) " · ${m.motivo}" else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = onFechar) { Text("Cancelar") } }
        )
        return
    }

    // ── Passo 2: parâmetros SÓ da magia escolhida ───────────────────────────────────────────────
    // Lote MEC-10: em magia de CURA o alvo padrão é SI MESMO (curar o goblin por acidente seria o
    // pior default possível); nas demais, o primeiro inimigo.
    var alvoId by remember(sel.id) {
        mutableStateOf<String?>(if (sel.ehCura) null else inimigos.firstOrNull()?.id)
    }
    var energia by remember(sel.id) { mutableIntStateOf(1) }
    var pvQueimar by remember(sel.id) { mutableIntStateOf(0) }
    var raio by remember(sel.id) { mutableIntStateOf(2) }
    var causaDano by remember(sel.id) { mutableStateOf(false) }
    val ehArea = sel.ehArea

    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text(sel.nome) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("${sel.classe} · NH ${sel.nhBasico} · custo ${sel.custoTexto}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))

                if (ehArea) {
                    Text("Raio da área: ${raio}m (custo × $raio)", fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = { if (raio > 1) raio-- },
                            modifier = Modifier.semantics { contentDescription = "Menos raio" }) { Text("−") }
                        Text("${raio}m", Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold)
                        OutlinedButton(onClick = { if (raio < 6) raio++ },
                            modifier = Modifier.semantics { contentDescription = "Mais raio" }) { Text("+") }
                    }
                    Text("Depois de confirmar, toque um hex no grid para o centro da explosão.",
                        style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else if (sel.ehToque) {
                    Text("Toque: a mágica carrega sua mão. Depois, ataque um inimigo adjacente para descarregá-la.",
                        style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("Alvo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    OpcaoRadio(alvoId == null, "Em mim mesmo (automagia)", "Conjurar sobre si mesmo") { alvoId = null }
                    inimigos.forEach { a ->
                        OpcaoRadio(alvoId == a.id, "${a.nome} (${a.distanciaM}m)", "Alvo ${a.nome}") { alvoId = a.id }
                    }
                }

                // Lote MA-6: magia de dano DIRETA (Comum/Área que não é Projétil/Toque).
                val proj = sel.ehProjetil
                // MEC-10: magia de CURA nunca oferece "causa dano" (não faz sentido e confundiria).
                // Lote MEC-20: e o toggle só aparece para quem PODE causar dano. A fonte diz que o
                // "1d por ponto de energia" vale para magia de COMBATE e Projétil ("uma mágica de
                // combate talvez cause 1d de dano por ponto") — não existe regra para bombear dano
                // em magia de Informação ou utilidade. Antes o switch aparecia em Localizar Ar e
                // Criar Ar, convidando o jogador a inventar dano que a regra não permite.
                // MEC-24: e quem JA tem dano curado (Morte Putrefata: 1d-1 por turno) tambem nao
                // oferece o toggle — o dano dela ja esta definido pelo livro.
                val efeitoPermiteDano = (sel.efeito == null || sel.efeito == "dano") && !sel.danoJaDefinido
                val podeMarcarDano = !proj && !sel.ehToque && !sel.ehCura && efeitoPermiteDano &&
                    (ehArea || alvoId != null)
                if (podeMarcarDano) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().semantics {
                            contentDescription = "Esta conjuração causa dano, um dado por energia" + if (causaDano) ", ativado" else ", desativado"
                        }) {
                        Switch(checked = causaDano, onCheckedChange = { causaDano = it })
                        Spacer(Modifier.width(8.dp))
                        Text("Causa dano (1d por energia)", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // Energia. Lote MEC-7/MEC-9: o teto vem da REGRA da magia, não da Aptidão pura.
                val escalaBuff = sel.escalaComEnergia
                // Lote MEC-41: CUSTO VARIÁVEL também abre o seletor. Sem isto, magia "Varia"/"1 a 4"
                // que não fosse projétil/dano/buff-que-escala era lançada no mínimo, sem escolha.
                if (proj || (podeMarcarDano && causaDano) || escalaBuff || sel.custoVariavel) {
                    val teto = if (proj || (podeMarcarDano && causaDano)) sel.aptidaoMagica
                        else if (escalaBuff) sel.energiaMax else sel.aptidaoMagica
                    val efeito = if (proj || (podeMarcarDano && causaDano)) "→ ${energia}d de dano" else (sel.dicaEnergia ?: "")
                    Spacer(Modifier.height(8.dp))
                    Text("Energia investida: ${energia} PF  ${if (efeito.isNotBlank()) "($efeito)" else ""}",
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.semantics { contentDescription = "Energia investida: $energia de $teto. $efeito" }) {
                        OutlinedButton(onClick = { if (energia > 1) energia-- },
                            modifier = Modifier.semantics { contentDescription = "Menos energia" }) { Text("−") }
                        Text("${energia}", Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold)
                        OutlinedButton(onClick = { if (energia < teto) energia++ },
                            modifier = Modifier.semantics { contentDescription = "Mais energia" }) { Text("+") }
                        Spacer(Modifier.width(8.dp))
                        Text("máx $teto", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Queimar PV (Magia p.8): paga parte do custo com PV em vez de PF — cada PV é −1 no NH.
                val tetoPv = sel.custoEstimado.coerceAtMost(4)
                if (tetoPv > 0) {
                    Spacer(Modifier.height(8.dp))
                    Text("Queimar PV: ${pvQueimar} (−${pvQueimar} no NH; dói!)", fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = { if (pvQueimar > 0) pvQueimar-- },
                            modifier = Modifier.semantics { contentDescription = "Menos PV queimado" }) { Text("−") }
                        Text("${pvQueimar}", Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold)
                        OutlinedButton(onClick = { if (pvQueimar < tetoPv) pvQueimar++ },
                            modifier = Modifier.semantics { contentDescription = "Mais PV queimado" }) { Text("+") }
                    }
                }

                // Lote C12: RITUAL (Magia p.9). Recolhido por padrão: quem não mexe conjura normal.
                Spacer(Modifier.height(8.dp))
                PainelRitual(ritual) { ritual = it }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val energiaEfetiva = if (sel.ehProjetil || causaDano || sel.escalaComEnergia || sel.custoVariavel) energia else 1 // MEC-41
                    when {
                        sel.ehArea -> onMirarArea(sel.id, raio, energiaEfetiva, pvQueimar, causaDano, ritual)
                        sel.ehToque -> onConjurar(sel.id, null, 1, pvQueimar, false, ritual)
                        else -> onConjurar(sel.id, alvoId, energiaEfetiva, pvQueimar, causaDano, ritual)
                    }
                },
                enabled = sel.castavel,
                modifier = Modifier.semantics {
                    contentDescription = when {
                        ehArea -> "Mirar a área no grid"; sel.ehToque -> "Carregar a mágica na mão"
                        else -> "Conjurar ${sel.nome}"
                    }
                }
            ) { Text(when { ehArea -> "Mirar no grid"; sel.ehToque -> "Carregar na mão"; else -> "Conjurar" }) }
        },
        // "Voltar" à lista em vez de fechar tudo — errar a magia não custa recomeçar a conjuração.
        dismissButton = {
            Row {
                // Lote MEC-39 (P11): projétil pode ser SEGURADO em vez de arremessado, para aumentar
                // por turnos (Magia p.12).
                if (sel.ehProjetil) {
                    TextButton(onClick = { onCarregarProjetil(sel.id, energia) }, enabled = sel.castavel) {
                        Text("Segurar")
                    }
                }
                TextButton(onClick = { magiaSel = null }) { Text("Voltar") }
            }
        }
    )
}


/**
 * Lote C12: painel do RITUAL (Magia p.9, regra opcional).
 *
 * Fica RECOLHIDO por padrão de propósito: a esmagadora maioria das conjurações é o ritual completo,
 * e quem não abrir não é penalizado. Só aparece expandido quando o jogador quer conjurar de forma
 * discreta — amarrado, amordaçado, escondido.
 *
 * O total é mostrado o tempo todo, porque o número é o que decide: `−4` no cabeçalho evita o
 * jogador descobrir a penalidade só depois de errar a jogada.
 */
@Composable
private fun PainelRitual(
    ritual: com.gurps.ficha.domain.magic.RitualDeConjuracao,
    onMudar: (com.gurps.ficha.domain.magic.RitualDeConjuracao) -> Unit,
) {
    var aberto by remember { mutableStateOf(false) }
    val mod = ritual.modificador
    val rotuloMod = when {
        mod > 0 -> "+$mod"
        mod < 0 -> "$mod"
        else -> "sem modificador"
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { aberto = !aberto }
            .semantics { contentDescription = "Ritual da conjuração, $rotuloMod. Toque para ajustar." }
    ) {
        Text("🕯️ Ritual: $rotuloMod", fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge,
            color = if (mod < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.weight(1f))
        Text(if (aberto) "▲" else "▼", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary)
    }
    if (aberto) {
        Text("Omitir partes do ritual penaliza o NH; caprichar dá +1 mas DOBRA o tempo de operação.",
            style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))

        Text("Gestos", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        com.gurps.ficha.domain.magic.GestoDoRitual.entries.forEach { g ->
            OpcaoRadio(ritual.gesto == g, "${g.rotulo}${modTexto(g.modificador)}", g.rotulo) {
                onMudar(ritual.copy(gesto = g))
            }
        }
        Text("Voz", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        com.gurps.ficha.domain.magic.VozDoRitual.entries.forEach { v ->
            OpcaoRadio(ritual.voz == v, "${v.rotulo}${modTexto(v.modificador)}", v.rotulo) {
                onMudar(ritual.copy(voz = v))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = !ritual.passos, onCheckedChange = { onMudar(ritual.copy(passos = !it)) },
                modifier = Modifier.semantics { contentDescription = "Omitir os movimentos dos pés" })
            Spacer(Modifier.width(8.dp))
            Text("Omitir os movimentos dos pés (−2)", style = MaterialTheme.typography.bodyMedium)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = ritual.caprichado, onCheckedChange = { onMudar(ritual.copy(caprichado = it)) },
                modifier = Modifier.semantics { contentDescription = "Caprichar no ritual" })
            Spacer(Modifier.width(8.dp))
            Text("Caprichar: +1, mas dobra o tempo", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun modTexto(m: Int): String = when {
    m > 0 -> " (+$m)"
    m < 0 -> " ($m)"
    else -> ""
}

/**
 * Lote P12: botão de conjurar no modo de FAIXAS (sem grade).
 *
 * A conjuração só existia dentro do menu do token, que só existe no grid tático. No modo de faixas
 * o jogador com mágicas simplesmente **não tinha como lançá-las** — era o P12 da lista de
 * pendências. Reusa exatamente o mesmo `SubDialogoConjurar` de dois passos, então busca, seletor de
 * energia, "Segurar" projétil e o painel de ritual vêm de graça.
 */
@Composable
internal fun BotaoConjurarFaixas(
    viewModel: FichaViewModel,
    estado: com.gurps.ficha.viewmodel.delegates.CombatUiState,
) {
    if (estado.magiasConjuraveis.isEmpty()) return
    // Conjuração multi-turno em andamento: o jogador só continua ou aborta (mesma regra do grid).
    if (estado.conjurando != null) return
    var dialogo by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
        Button(
            onClick = { dialogo = true },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Conjurar uma mágica" }
        ) { Text("🔮 Conjurar") }
    }
    if (dialogo) {
        SubDialogoConjurar(
            magias = estado.magiasConjuraveis,
            inimigos = estado.combatentes.filter { !it.ehHeroi && it.vivo },
            onConjurar = { magiaId, alvoId, energia, pvQueimar, causaDano, ritual ->
                viewModel.sagaCombateConjurar(magiaId, alvoId, energia, pvQueimar, causaDano, ritual)
                dialogo = false
            },
            onCarregarProjetil = { magiaId, energia ->
                viewModel.sagaCarregarProjetil(magiaId, energia); dialogo = false
            },
            onMirarArea = { magiaId, raio, energia, pvQueimar, causaDano, ritual ->
                // Sem grade não há hex para mirar: o controller resolve na hora, por faixa.
                viewModel.sagaIniciarMiraArea(magiaId, raio, energia, pvQueimar, causaDano, ritual)
                dialogo = false
            },
            onFechar = { dialogo = false }
        )
    }
}

/**
 * Lote C11: zonas ativas com a opção de ENCOLHER (Magia p.10).
 *
 * *"Um mágico pode optar por manter apenas parte da área de uma mágica."* Sem este gatilho a regra
 * existia no motor e era inalcançável em jogo. **Expandir não é oferecido** — o livro proíbe, e o
 * motor recusa com uma linha no log se alguém tentar.
 */
@Composable
internal fun ZonasAtivasFaixas(viewModel: FichaViewModel) {
    val zonas = viewModel.sagaZonasAtivas
    if (zonas.isEmpty()) return
    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
        Text("☁️ Áreas ativas", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        zonas.forEach { (nome, raio) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$nome — raio ${raio}m", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
                if (raio > 1) {
                    TextButton(
                        onClick = { viewModel.sagaEncolherZona(nome, raio - 1) },
                        modifier = Modifier.semantics {
                            contentDescription = "Encolher $nome para ${raio - 1} metros"
                        }
                    ) { Text("Encolher para ${raio - 1}m") }
                }
            }
        }
    }
}
