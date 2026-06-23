package com.gurps.ficha.ui.saga

import androidx.compose.foundation.background
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.combat.*
import com.gurps.ficha.viewmodel.FichaViewModel
import com.gurps.ficha.viewmodel.delegates.CombatenteUi
import com.gurps.ficha.viewmodel.delegates.FaixaDistancia

/**
 * Lote 365 (Saga B7): UI de combate (visual aprovado no mockup). Três partes:
 *  - CombatTracker: faixas Engajado→Extremo, avatar de inicial colorida, barra de PV, postura/condições;
 *  - ManeuverCards: só `manobrasLegais()`, com sub-diálogo de alvo + local (penalidades visíveis);
 *  - DefendaSeCard: defesas com valor final + Rolar.
 * Tudo com `contentDescription` para jogar de olhos fechados (variante PraCego).
 *
 * MVP do B7: avatar = inicial colorida (azul herói / vermelho inimigo). Retratos reais entram depois
 * (registro nos LOTE B7/E2 do plano: gerados em tempo real pelo Narrador via GeminiImageService).
 */

private val COR_PV_OK = Color(0xFF4CAF50)
private val COR_PV_MEDIO = Color(0xFFFFC107)
private val COR_PV_BAIXO = Color(0xFFF44336)

@Composable
fun CombatePainel(viewModel: FichaViewModel, modifier: Modifier = Modifier) {
    val estado = viewModel.sagaCombateEstado ?: return
    val defesa = viewModel.sagaCombateDefesaPendente

    Surface(
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.fillMaxSize()) {
            // Cabeçalho da rodada — FIXO no topo do painel.
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Combate — Rodada ${estado.rodada}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                if (estado.encerrado) {
                    TextButton(onClick = { viewModel.sagaCombateEncerrar() },
                        modifier = Modifier.semantics { contentDescription = "Fechar combate e voltar à narração" }) { Text("Fechar") }
                }
            }
            HorizontalDivider()

            // Conteúdo ROLÁVEL: tracker + manobras/defesa (resolve card tampando o chat e manobras cortadas).
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                CombatTracker(estado.combatentes)

                when {
                    estado.encerrado -> FimDeCombate(estado.resultado)
                    defesa != null -> DefendaSeCard(viewModel, defesa)
                    estado.vezDoHeroi -> ManeuverCards(viewModel, estado)
                    else -> AguardandoInimigos()
                }
            }
        }
    }
}

// ── CombatTracker ───────────────────────────────────────────────────────────

@Composable
private fun CombatTracker(combatentes: List<CombatenteUi>) {
    // Herói primeiro (fixo), inimigos agrupados por faixa de distância.
    val heroi = combatentes.firstOrNull { it.ehHeroi }
    val porFaixa = combatentes.filter { !it.ehHeroi }.groupBy { it.faixa }

    Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)) {
        heroi?.let {
            FaixaRotulo("Você")
            CombatenteChip(it, destaque = true)
            Spacer(Modifier.height(4.dp))
        }
        FaixaDistancia.values().forEach { faixa ->
            val grupo = porFaixa[faixa].orEmpty().filter { it.vivo }
            if (grupo.isNotEmpty()) {
                FaixaRotulo("${faixa.rotulo} (${grupo.first().distanciaM}m)")
                grupo.forEach { CombatenteChip(it, destaque = false) }
                Spacer(Modifier.height(4.dp))
            }
        }
        val caidos = combatentes.filter { !it.ehHeroi && !it.vivo }
        if (caidos.isNotEmpty()) {
            Text("Fora de combate: ${caidos.joinToString(", ") { it.nome }}",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FaixaRotulo(texto: String) {
    Text(texto, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
}

@Composable
private fun CombatenteChip(c: CombatenteUi, destaque: Boolean) {
    val corAvatar = if (c.ehHeroi) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    Surface(
        color = if (destaque) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .semantics { contentDescription = c.descricaoAcessivel }
    ) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            // Avatar de inicial colorida (placeholder do retrato real — ver registro do plano)
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(corAvatar),
                contentAlignment = Alignment.Center
            ) {
                Text(c.nome.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(c.nome, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                // Barra de PV
                val cor = when { c.fracaoPv > 0.5f -> COR_PV_OK; c.fracaoPv > 0.25f -> COR_PV_MEDIO; else -> COR_PV_BAIXO }
                Box(
                    Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Box(Modifier.fillMaxWidth(c.fracaoPv).height(6.dp).clip(RoundedCornerShape(3.dp)).background(cor))
                }
                val infos = buildList {
                    add("PV ${c.pvAtual}/${c.pvMax}")
                    add(c.postura)
                    if (c.condicoes.isNotEmpty()) add(c.condicoes.joinToString(", "))
                }
                Text(infos.joinToString(" · "), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ── ManeuverCards ───────────────────────────────────────────────────────────

@Composable
private fun ManeuverCards(viewModel: FichaViewModel, estado: com.gurps.ficha.viewmodel.delegates.CombatUiState) {
    var alvoDialogo by remember { mutableStateOf<Manobra?>(null) }
    var moverDialogo by remember { mutableStateOf(false) }
    var avaliarDialogo by remember { mutableStateOf(false) }
    var apontarDialogo by remember { mutableStateOf(false) }
    var fintarDialogo by remember { mutableStateOf(false) }
    var agarrarDialogo by remember { mutableStateOf(false) }
    var derrubarDialogo by remember { mutableStateOf(false) }
    var encontraoDialogo by remember { mutableStateOf(false) }
    var empurraoDialogo by remember { mutableStateOf(false) }
    var imobilizarDialogo by remember { mutableStateOf(false) }
    var posturaDialogo by remember { mutableStateOf(false) }
    var defesaTotalDialogo by remember { mutableStateOf(false) }

    Card(
        Modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Sua vez — escolha a manobra", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))

            // Seletor de arma/ataque (o jogador vê e escolhe o que está empunhando).
            SeletorDeArma(viewModel, estado)

            Spacer(Modifier.height(8.dp))
            estado.manobrasHeroi.forEach { m ->
                val ehAtaque = m == Manobra.ATAQUE || m == Manobra.ATAQUE_TOTAL || m == Manobra.GOLPE_RAPIDO
                val ehMoverAtacar = m == Manobra.MOVER_E_ATACAR
                val precisaAlvo = ehAtaque || ehMoverAtacar
                val temAlvo = if (ehMoverAtacar) estado.alvosMoverEAtacar.isNotEmpty() else estado.alvos.isNotEmpty()
                Button(
                    onClick = {
                        when {
                            precisaAlvo && temAlvo -> alvoDialogo = m
                            m == Manobra.MOVER -> moverDialogo = true
                            m == Manobra.AVALIAR -> avaliarDialogo = true
                            m == Manobra.APONTAR -> apontarDialogo = true
                            m == Manobra.FINTAR -> fintarDialogo = true
                            m == Manobra.AGARRAR -> agarrarDialogo = true
                            m == Manobra.DERRUBAR -> derrubarDialogo = true
                            m == Manobra.ENCONTRAO -> encontraoDialogo = true
                            m == Manobra.EMPURRAO -> empurraoDialogo = true
                            m == Manobra.IMOBILIZAR -> imobilizarDialogo = true
                            m == Manobra.MUDAR_POSTURA -> posturaDialogo = true
                            m == Manobra.DEFESA_TOTAL -> defesaTotalDialogo = true
                            m == Manobra.FOGO_RETENCAO -> viewModel.sagaCombateFogoRetencao() // Lote 396: área, sem alvo
                            m == Manobra.AGUARDAR -> viewModel.sagaCombateAguardar() // Lote 399: Interromper Investida
                            else -> viewModel.sagaCombateManobra(m)
                        }
                    },
                    enabled = !(precisaAlvo && !temAlvo),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        .semantics { contentDescription = "Manobra ${m.rotulo}" + if (precisaAlvo && !temAlvo) ", sem alvo ao alcance" else "" }
                ) { Text(m.rotulo + if (precisaAlvo && !temAlvo) " (sem alvo)" else "") }
            }
            val ranged = estado.ataqueAtual?.aDistancia == true
            if (estado.alvos.isEmpty() && !ranged) {
                Text("Nenhum inimigo ao alcance do corpo-a-corpo — use Mover para avançar (ou empunhe uma arma à distância).",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp))
            }
        }
    }

    alvoDialogo?.let { manobra ->
        val ehMoverAtacar = manobra == Manobra.MOVER_E_ATACAR
        SubDialogoAlvoLocal(
            manobra = manobra,
            alvos = if (ehMoverAtacar) estado.alvosMoverEAtacar else estado.alvos,
            ataques = estado.ataques,
            ataqueSelecionado = estado.ataqueSelecionado,
            ambidestro = estado.heroiAmbidestro,
            onConfirmar = { alvoId, local, modo, offHand, enganoso ->
                when {
                    ehMoverAtacar -> viewModel.sagaCombateMoverEAtacar(alvoId, local)
                    manobra == Manobra.GOLPE_RAPIDO -> viewModel.sagaCombateGolpeRapido(alvoId, local) // Lote 408
                    modo == AtaqueTotalModo.DUPLO && offHand != null -> viewModel.sagaCombateAtacarDuplo(alvoId, local, offHand)
                    else -> viewModel.sagaCombateAtacar(alvoId, manobra, local, modo, enganoso)
                }
                alvoDialogo = null
            },
            onFechar = { alvoDialogo = null }
        )
    }

    if (moverDialogo) {
        SubDialogoMover(
            inimigos = estado.combatentes.filter { !it.ehHeroi && it.vivo },
            deslocamentoMax = estado.deslocamentoHeroi,
            onConfirmar = { alvoId, afastar, metros ->
                viewModel.sagaCombateMover(alvoId, afastar, metros); moverDialogo = false
            },
            onFechar = { moverDialogo = false }
        )
    }

    if (avaliarDialogo) {
        SubDialogoEscolherAlvo(
            titulo = "Avaliar quem?",
            descricaoConfirmar = "Avaliar alvo",
            alvos = estado.combatentes.filter { !it.ehHeroi && it.vivo },
            onConfirmar = { alvoId -> viewModel.sagaCombateAvaliar(alvoId); avaliarDialogo = false },
            onFechar = { avaliarDialogo = false }
        )
    }

    if (apontarDialogo) {
        SubDialogoApontar(
            alvos = estado.combatentes.filter { !it.ehHeroi && it.vivo },
            podeFirmar = estado.ataques.getOrNull(estado.ataqueSelecionado)?.armaDeFogo == true,
            onConfirmar = { alvoId, firmado -> viewModel.sagaCombateApontar(alvoId, firmado); apontarDialogo = false },
            onFechar = { apontarDialogo = false }
        )
    }

    if (fintarDialogo) {
        SubDialogoEscolherAlvo(
            titulo = "Fintar quem?",
            descricaoConfirmar = "Fintar o alvo (reduz a defesa dele no próximo golpe)",
            alvos = estado.alvos, // corpo-a-corpo: alvos ao alcance da arma
            onConfirmar = { alvoId -> viewModel.sagaCombateFintar(alvoId); fintarDialogo = false },
            onFechar = { fintarDialogo = false }
        )
    }

    if (agarrarDialogo) {
        SubDialogoEscolherAlvo(
            titulo = "Agarrar quem?",
            descricaoConfirmar = "Agarrar o alvo (fica preso, −4 na defesa)",
            alvos = estado.alvos,
            onConfirmar = { alvoId -> viewModel.sagaCombateAgarrar(alvoId); agarrarDialogo = false },
            onFechar = { agarrarDialogo = false }
        )
    }

    if (derrubarDialogo) {
        SubDialogoEscolherAlvo(
            titulo = "Derrubar quem?",
            descricaoConfirmar = "Derrubar o alvo (Disputa de ST/DX; melhor se já estiver agarrado)",
            alvos = estado.alvos,
            onConfirmar = { alvoId -> viewModel.sagaCombateDerrubar(alvoId); derrubarDialogo = false },
            onFechar = { derrubarDialogo = false }
        )
    }

    if (encontraoDialogo) {
        SubDialogoEscolherAlvo(
            titulo = "Encontrão em quem?",
            descricaoConfirmar = "Dar um encontrão no alvo (colisão corporal, dano mútuo, derrubada)",
            alvos = estado.alvos,
            onConfirmar = { alvoId -> viewModel.sagaCombateEncontrao(alvoId); encontraoDialogo = false },
            onFechar = { encontraoDialogo = false }
        )
    }

    if (empurraoDialogo) {
        SubDialogoEscolherAlvo(
            titulo = "Empurrar quem?",
            descricaoConfirmar = "Empurrar o alvo (projeção para trás, sem lesão)",
            alvos = estado.alvos,
            onConfirmar = { alvoId -> viewModel.sagaCombateEmpurrao(alvoId); empurraoDialogo = false },
            onFechar = { empurraoDialogo = false }
        )
    }

    if (imobilizarDialogo) {
        SubDialogoEscolherAlvo(
            titulo = "Imobilizar quem? (precisa estar agarrado e no chão)",
            descricaoConfirmar = "Imobilizar o alvo agarrado (Disputa de ST; deixa indefeso)",
            alvos = estado.combatentes.filter { !it.ehHeroi && it.vivo && it.condicoes.contains("agarrado") },
            onConfirmar = { alvoId -> viewModel.sagaCombateImobilizar(alvoId); imobilizarDialogo = false },
            onFechar = { imobilizarDialogo = false }
        )
    }

    if (posturaDialogo) {
        SubDialogoPostura(
            posturaAtual = estado.posturaHeroi,
            posturas = estado.posturasAlcancaveis,
            onConfirmar = { postura -> viewModel.sagaCombateManobra(Manobra.MUDAR_POSTURA, postura); posturaDialogo = false },
            onFechar = { posturaDialogo = false }
        )
    }

    if (defesaTotalDialogo) {
        SubDialogoDefesaTotal(
            onConfirmar = { modo, aumentadaEm ->
                viewModel.sagaCombateDefesaTotal(modo, aumentadaEm); defesaTotalDialogo = false
            },
            onFechar = { defesaTotalDialogo = false }
        )
    }
}

/** Lote 388: escolhe a opção da Defesa Total — Aumentada (+2 numa defesa) ou Dupla (2ª defesa). MB p.366. */
@Composable
private fun SubDialogoDefesaTotal(
    onConfirmar: (DefesaTotalModo, CombatResolver.TipoDefesa?) -> Unit,
    onFechar: () -> Unit
) {
    var modo by remember { mutableStateOf(DefesaTotalModo.AUMENTADA) }
    var aumentadaEm by remember { mutableStateOf(CombatResolver.TipoDefesa.ESQUIVA) }
    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text("Defesa Total") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OpcaoRadio(modo == DefesaTotalModo.AUMENTADA, "Aumentada (+2 numa defesa)",
                    "Defesa Total Aumentada, +2 numa defesa à escolha") { modo = DefesaTotalModo.AUMENTADA }
                OpcaoRadio(modo == DefesaTotalModo.DUPLA, "Dupla (2ª defesa se a 1ª falhar)",
                    "Defesa Total Dupla, tenta uma segunda defesa diferente se a primeira falhar") { modo = DefesaTotalModo.DUPLA }
                if (modo == DefesaTotalModo.AUMENTADA) {
                    Spacer(Modifier.height(8.dp))
                    Text("+2 em qual defesa?", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    listOf(
                        CombatResolver.TipoDefesa.ESQUIVA, CombatResolver.TipoDefesa.APARA, CombatResolver.TipoDefesa.BLOQUEIO
                    ).forEach { d ->
                        OpcaoRadio(aumentadaEm == d, d.rotulo, "+2 em ${d.rotulo}") { aumentadaEm = d }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmar(modo, if (modo == DefesaTotalModo.AUMENTADA) aumentadaEm else null) },
                modifier = Modifier.semantics { contentDescription = "Confirmar Defesa Total" }
            ) { Text("Assumir") }
        },
        dismissButton = { TextButton(onClick = onFechar) { Text("Cancelar") } }
    )
}

@Composable
private fun SubDialogoMover(
    inimigos: List<CombatenteUi>,
    deslocamentoMax: Int,
    onConfirmar: (alvoId: String?, afastar: Boolean, metros: Int) -> Unit,
    onFechar: () -> Unit
) {
    var alvoId by remember { mutableStateOf(inimigos.firstOrNull()?.id) }
    var afastar by remember { mutableStateOf(false) }
    var metros by remember { mutableIntStateOf(deslocamentoMax.coerceAtLeast(1)) }

    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text("Mover") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("Direção", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                OpcaoRadio(!afastar, "Avançar (aproximar)", "Avançar, aproximar do alvo") { afastar = false }
                OpcaoRadio(afastar, "Recuar (afastar)", "Recuar, afastar do alvo") { afastar = true }

                Spacer(Modifier.height(8.dp))
                Text("Em relação a", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                OpcaoRadio(alvoId == null, "Todos os inimigos", "Mover em relação a todos") { alvoId = null }
                inimigos.forEach { a ->
                    OpcaoRadio(alvoId == a.id, "${a.nome} (${a.distanciaM}m)", "Mover em relação a ${a.nome}") { alvoId = a.id }
                }

                Spacer(Modifier.height(8.dp))
                Text("Distância: ${metros}m (até ${deslocamentoMax}m)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { if (metros > 1) metros-- },
                        modifier = Modifier.semantics { contentDescription = "Menos um metro" }) { Text("−") }
                    Text("${metros}m", Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold)
                    OutlinedButton(onClick = { if (metros < deslocamentoMax) metros++ },
                        modifier = Modifier.semantics { contentDescription = "Mais um metro" }) { Text("+") }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirmar(alvoId, afastar, metros) },
                modifier = Modifier.semantics { contentDescription = "Confirmar movimento" }) { Text("Mover") }
        },
        dismissButton = { TextButton(onClick = onFechar) { Text("Cancelar") } }
    )
}

@Composable
private fun SubDialogoEscolherAlvo(
    titulo: String,
    descricaoConfirmar: String,
    alvos: List<CombatenteUi>,
    onConfirmar: (alvoId: String) -> Unit,
    onFechar: () -> Unit
) {
    var alvoId by remember { mutableStateOf(alvos.firstOrNull()?.id ?: "") }
    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text(titulo) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                alvos.forEach { a ->
                    OpcaoRadio(alvoId == a.id, "${a.nome} — PV ${a.pvAtual}/${a.pvMax} (${a.distanciaM}m)", "Alvo ${a.nome}") { alvoId = a.id }
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (alvoId.isNotBlank()) onConfirmar(alvoId) },
                enabled = alvoId.isNotBlank(),
                modifier = Modifier.semantics { contentDescription = descricaoConfirmar }) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = onFechar) { Text("Cancelar") } }
    )
}

/** Lote 395: Apontar — escolhe o alvo e (só para arma de fogo) se vai "firmar" a arma (+1 Acc). MB p.364. */
@Composable
private fun SubDialogoApontar(
    alvos: List<CombatenteUi>,
    podeFirmar: Boolean,
    onConfirmar: (alvoId: String, firmado: Boolean) -> Unit,
    onFechar: () -> Unit
) {
    var alvoId by remember { mutableStateOf(alvos.firstOrNull()?.id ?: "") }
    var firmado by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text("Apontar (mirar) em quem?") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                alvos.forEach { a ->
                    OpcaoRadio(alvoId == a.id, "${a.nome} — PV ${a.pvAtual}/${a.pvMax} (${a.distanciaM}m)", "Alvo ${a.nome}") { alvoId = a.id }
                }
                if (podeFirmar) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().semantics {
                            contentDescription = "Firmar a arma, mais um na precisão" + if (firmado) ", ativado" else ", desativado"
                        }
                    ) {
                        Switch(checked = firmado, onCheckedChange = { firmado = it })
                        Spacer(Modifier.width(8.dp))
                        Text("Firmar a arma (+1 Prec.)", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (alvoId.isNotBlank()) onConfirmar(alvoId, firmado && podeFirmar) },
                enabled = alvoId.isNotBlank(),
                modifier = Modifier.semantics { contentDescription = "Apontar no alvo" }) { Text("Apontar") }
        },
        dismissButton = { TextButton(onClick = onFechar) { Text("Cancelar") } }
    )
}

@Composable
private fun SubDialogoPostura(
    posturaAtual: String,
    posturas: List<Postura>,
    onConfirmar: (Postura) -> Unit,
    onFechar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text("Mudar de postura") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("Atual: $posturaAtual", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                posturas.forEach { p ->
                    Button(
                        onClick = { onConfirmar(p) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                            .semantics { contentDescription = "Mudar para ${p.rotulo}" }
                    ) { Text(p.rotulo.replaceFirstChar { it.uppercase() }) }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onFechar) { Text("Cancelar") } }
    )
}

@Composable
private fun SeletorDeArma(viewModel: FichaViewModel, estado: com.gurps.ficha.viewmodel.delegates.CombatUiState) {
    val atual = estado.ataqueAtual ?: return
    var aberto by remember { mutableStateOf(false) }
    val alcanceTxt = if (atual.aDistancia) " · à distância (Máx ${atual.alcance}m)" else " · corpo-a-corpo (alcance ${atual.alcance}m)"
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().semantics {
            contentDescription = "Arma empunhada: ${atual.rotulo}, NH ${atual.nh}, dano ${atual.danoExpr}. Toque em Sacar para trocar de arma."
        }
    ) {
        Column(Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Empunhando: ${atual.rotulo}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "NH ${atual.nh} · ${atual.danoExpr} ${atual.tipo.rotulo}$alcanceTxt",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (estado.ataques.size > 1) {
                    OutlinedButton(onClick = { aberto = !aberto },
                        modifier = Modifier.semantics { contentDescription = if (aberto) "Fechar troca de arma" else "Trocar a arma empunhada" }) {
                        Text(if (aberto) "Fechar" else "Trocar arma")
                    }
                }
            }
            if (aberto) {
                Text("Toque numa arma para empunhá-la:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                Text(
                    "Trocar de arma é a manobra Preparar (gasta o turno) — livre com Saque Rápido.",
                    style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                estado.ataques.forEachIndexed { i, atk ->
                    val empunhada = i == estado.ataqueSelecionado
                    OpcaoRadio(
                        selecionado = empunhada,
                        rotulo = "${atk.rotulo} — NH ${atk.nh}, ${atk.danoExpr} ${atk.tipo.rotulo}" + if (empunhada) " (na mão)" else "",
                        descricao = if (empunhada) "${atk.rotulo}, já empunhada" else "Sacar ${atk.rotulo}",
                        onClick = { if (!empunhada) viewModel.sagaCombateSacarArma(i); aberto = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun SubDialogoAlvoLocal(
    manobra: Manobra,
    alvos: List<CombatenteUi>,
    ataques: List<AtaqueHeroi>,
    ataqueSelecionado: Int,
    ambidestro: Boolean,
    onConfirmar: (alvoId: String, local: LocalAtaque, modo: AtaqueTotalModo, offHandIndex: Int?, enganoso: Int) -> Unit,
    onFechar: () -> Unit
) {
    var alvoId by remember { mutableStateOf(alvos.firstOrNull()?.id ?: "") }
    var local by remember { mutableStateOf(LocalAtaque.TORSO) }
    var modo by remember { mutableStateOf(AtaqueTotalModo.DETERMINADO) }
    // Ataque Total (Duplo): a 2ª arma (mão inábil) é qualquer ataque diferente do empunhado.
    val opcoesOffHand = remember(ataques, ataqueSelecionado) {
        ataques.withIndex().filter { it.index != ataqueSelecionado }
    }
    var offHand by remember { mutableStateOf(opcoesOffHand.firstOrNull()?.index) }
    val podeDuplo = manobra == Manobra.ATAQUE_TOTAL && opcoesOffHand.isNotEmpty()
    // Lote 387/401: arma à distância muda o Ataque Total e desabilita o Ataque Enganoso (corpo-a-corpo).
    val armaDistancia = ataques.getOrNull(ataqueSelecionado)?.aDistancia == true
    // Ataque Enganoso (Lote 401, MB p.369): −2 no NH por −1 na defesa do alvo; o NH efetivo não pode cair abaixo de 10.
    val maxEnganoso = if (armaDistancia || manobra != Manobra.ATAQUE) 0
        else (((ataques.getOrNull(ataqueSelecionado)?.nh ?: 10) - 10) / 2).coerceIn(0, 4)
    var enganoso by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text("${manobra.rotulo}: alvo e local") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("Alvo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                alvos.forEach { a ->
                    OpcaoRadio(
                        selecionado = alvoId == a.id,
                        rotulo = "${a.nome} — PV ${a.pvAtual}/${a.pvMax}",
                        descricao = "Alvo ${a.nome}",
                        onClick = { alvoId = a.id }
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text("Local do golpe", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                LOCAIS_ATAQUE.forEach { l ->
                    val pen = if (l.penalidadeAtaque == 0) "sem penalidade" else "${l.penalidadeAtaque} ao acertar"
                    OpcaoRadio(
                        selecionado = local == l,
                        rotulo = "${l.rotulo} ($pen)",
                        descricao = "Local ${l.rotulo}, $pen",
                        onClick = { local = l }
                    )
                }
                if (manobra == Manobra.ATAQUE && maxEnganoso > 0) {
                    Spacer(Modifier.height(8.dp))
                    Text("Ataque Enganoso (−2 no acerto por −1 na defesa do alvo)",
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = { if (enganoso > 0) enganoso-- }, enabled = enganoso > 0,
                            modifier = Modifier.semantics { contentDescription = "Diminuir engano" }) { Text("−") }
                        Text("  $enganoso  →  −${enganoso * 2} acerto / −$enganoso na defesa do alvo  ",
                            style = MaterialTheme.typography.bodyMedium)
                        OutlinedButton(onClick = { if (enganoso < maxEnganoso) enganoso++ }, enabled = enganoso < maxEnganoso,
                            modifier = Modifier.semantics { contentDescription = "Aumentar engano" }) { Text("+") }
                    }
                }
                if (manobra == Manobra.ATAQUE_TOTAL) {
                    Spacer(Modifier.height(8.dp))
                    Text("Modo do Ataque Total", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    // Lote 387: à distância o Determinado é +1 (não +4) e não existe "Forte" (MB p.365).
                    val modos = buildList {
                        add(AtaqueTotalModo.DETERMINADO to if (armaDistancia) "+1 para acertar" else "+4 para acertar")
                        if (!armaDistancia) add(AtaqueTotalModo.FORTE to "+2 de dano, ou +1/dado")
                        if (podeDuplo) add(AtaqueTotalModo.DUPLO to "2 golpes, 2 armas")
                    }
                    modos.forEach { (mo, desc) ->
                        OpcaoRadio(
                            selecionado = modo == mo,
                            rotulo = "${mo.rotulo} ($desc)",
                            descricao = "${mo.rotulo}, $desc",
                            onClick = { modo = mo }
                        )
                    }
                    // Duplo: escolher a 2ª arma (mão inábil) + notinha do −4/Ambidestria (MB p.366).
                    if (modo == AtaqueTotalModo.DUPLO && podeDuplo) {
                        Spacer(Modifier.height(8.dp))
                        val notinha = if (ambidestro) "Ambidestria: a 2ª arma ataca sem penalidade."
                            else "Mão inábil: a 2ª arma ataca com −4 (Ambidestria anularia)."
                        Text("2ª arma — $notinha",
                            fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        opcoesOffHand.forEach { (i, atk) ->
                            OpcaoRadio(
                                selecionado = offHand == i,
                                rotulo = "${atk.rotulo} — NH ${atk.nh}, ${atk.danoExpr} ${atk.tipo.rotulo}",
                                descricao = "Segunda arma ${atk.rotulo}",
                                onClick = { offHand = i }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (alvoId.isNotBlank())
                        onConfirmar(alvoId, local, modo, if (modo == AtaqueTotalModo.DUPLO) offHand else null,
                            if (manobra == Manobra.ATAQUE) enganoso else 0)
                },
                enabled = alvoId.isNotBlank() && !(modo == AtaqueTotalModo.DUPLO && offHand == null),
                modifier = Modifier.semantics { contentDescription = "Confirmar ataque" }
            ) { Text(if (modo == AtaqueTotalModo.DUPLO) "Atacar (Duplo)" else "Atacar") }
        },
        dismissButton = { TextButton(onClick = onFechar) { Text("Cancelar") } }
    )
}

@Composable
private fun OpcaoRadio(selecionado: Boolean, rotulo: String, descricao: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp).semantics { contentDescription = descricao },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selecionado, onClick = onClick)
        Spacer(Modifier.width(4.dp))
        Text(rotulo, style = MaterialTheme.typography.bodyMedium)
    }
}

// ── DefendaSeCard ───────────────────────────────────────────────────────────

@Composable
private fun DefendaSeCard(viewModel: FichaViewModel, defesa: com.gurps.ficha.viewmodel.delegates.DefesaPendenteUi) {
    Card(
        Modifier.fillMaxWidth().padding(8.dp)
            .semantics { contentDescription = "${defesa.descricaoAtaque} Opções de defesa abaixo." },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("🛡️ Defenda-se!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(defesa.descricaoAtaque, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            defesa.opcoes.forEach { op ->
                val comps = op.componentes.joinToString(" ") { (if (it.valor >= 0) "+${it.valor}" else "${it.valor}") + " ${it.nome}" }
                val sufixo = when { // Lote 389/404/405
                    op.recuo -> " ↩ recuar"
                    op.jogarSeAoChao -> " ⤓ jogar-se ao chão"
                    op.maoInabil -> " 🤚 mão inábil"
                    else -> ""
                }
                val rotulo = buildString {
                    append("${op.tipo.rotulo}$sufixo ${op.valorFinal}")
                    if (comps.isNotBlank()) append("  ($comps)")
                    if (!op.disponivel) append(" — ${op.motivoIndisponivel}")
                }
                Button(
                    onClick = { if (op.disponivel) viewModel.sagaCombateDefender(op) },
                    enabled = op.disponivel,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        .semantics { contentDescription = "${op.tipo.rotulo}${if (op.recuo) " com recuo" else ""}${if (op.jogarSeAoChao) " jogando-se ao chão" else ""} valor ${op.valorFinal}" + if (!op.disponivel) ", indisponível" else ", rolar" }
                ) { Text(rotulo) }
            }
        }
    }
}

@Composable
private fun FimDeCombate(resultado: ResultadoCombate?) {
    val (txt, cor) = when (resultado) {
        ResultadoCombate.VITORIA -> "🏆 Vitória!" to MaterialTheme.colorScheme.primaryContainer
        ResultadoCombate.DERROTA -> "💀 Você caiu." to MaterialTheme.colorScheme.errorContainer
        ResultadoCombate.FUGA -> "🏃 Os inimigos fugiram." to MaterialTheme.colorScheme.tertiaryContainer
        null -> "Combate encerrado." to MaterialTheme.colorScheme.surfaceVariant
    }
    Surface(color = cor, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Text(txt, Modifier.padding(16.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun AguardandoInimigos() {
    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
        Spacer(Modifier.width(8.dp))
        Text("Inimigos agindo…", style = MaterialTheme.typography.bodySmall)
    }
}

/** Locais oferecidos no sub-diálogo (ordem do mais comum ao mais arriscado). */
private val LOCAIS_ATAQUE = listOf(
    LocalAtaque.TORSO, LocalAtaque.ROSTO, LocalAtaque.PESCOCO, LocalAtaque.CRANIO,
    LocalAtaque.VITAIS, LocalAtaque.BRACO, LocalAtaque.PERNA, LocalAtaque.MAO, LocalAtaque.PE,
    LocalAtaque.INGLE, LocalAtaque.OLHO
)
