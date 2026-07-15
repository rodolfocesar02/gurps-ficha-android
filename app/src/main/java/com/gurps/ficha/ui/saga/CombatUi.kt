package com.gurps.ficha.ui.saga

import androidx.compose.foundation.background
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
fun CombatePainel(
    viewModel: FichaViewModel,
    modifier: Modifier = Modifier,
    // Lote TOK-6b-1: no modo TÁTICO a vida mora no token (barra sobre a cabeça) — o tracker de
    // cards duplicaria a informação e roubaria espaço da grade. No modo faixas continua true.
    mostrarTracker: Boolean = true,
) {
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
                if (mostrarTracker) CombatTracker(estado.combatentes)

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

/**
 * Lote TOK-6b-3: overlay de status do combate tático, SOBRE a grade (o painel fixo do rodapé saiu
 * pra dar todo o espaço vertical ao grid). Só aparece quando exige atenção: Defenda-se!, fim de
 * combate, ou a vez dos inimigos. **Na vez do herói não renderiza NADA** — as ações moram nos
 * tokens (menu) e o movimento nos hexes verdes.
 */
@Composable
fun CombateStatusTatico(viewModel: FichaViewModel, modifier: Modifier = Modifier) {
    val estado = viewModel.sagaCombateEstado ?: return
    val defesa = viewModel.sagaCombateDefesaPendente
    val conjurando = estado.conjurando
    when {
        estado.encerrado -> Column(
            modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FimDeCombate(estado.resultado)
            Button(
                onClick = { viewModel.sagaCombateEncerrar() },
                modifier = Modifier.padding(bottom = 8.dp)
                    .semantics { contentDescription = "Fechar combate e voltar à narração" }
            ) { Text("Fechar") }
        }
        // Defenda-se! é o momento mais crítico — o card cheio (opaco) sobre a grade.
        defesa != null -> Box(modifier.fillMaxWidth()) { DefendaSeCard(viewModel, defesa) }
        // Lote MA-3c: conjurando uma magia multi-turno — só Continuar ou Abortar.
        conjurando != null && estado.vezDoHeroi -> Surface(
            color = Color(0xE6152238), contentColor = Color.White, shape = RoundedCornerShape(16.dp),
            modifier = modifier.padding(8.dp).fillMaxWidth(0.96f)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text("🔮 Conjurando ${conjurando.nome}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text("${conjurando.turnosRestantes}s de concentração restante(s). Ser ferido ou atordoado pode fazer perder a magia.",
                    style = MaterialTheme.typography.bodySmall, color = Color(0xCCFFFFFF))
                Spacer(Modifier.height(8.dp))
                Row {
                    Button(onClick = { viewModel.sagaCombateContinuarConjuracao() },
                        modifier = Modifier.semantics { contentDescription = "Continuar concentrando na magia" }) { Text("Continuar") }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = { viewModel.sagaCombateAbortarConjuracao() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier.semantics { contentDescription = "Abortar a conjuração" }) { Text("Abortar") }
                }
            }
        }
        // Vez dos inimigos: pílula translúcida discreta.
        !estado.vezDoHeroi -> Surface(
            color = Color(0xCC10161F), contentColor = Color.White, shape = RoundedCornerShape(16.dp),
            modifier = modifier.padding(8.dp)
        ) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Inimigos agindo…", style = MaterialTheme.typography.bodySmall)
            }
        }
        // else: vez do herói → nada (menu do token + hexes verdes cuidam de tudo).
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
    var estrangularDialogo by remember { mutableStateOf(false) }
    var chaveMembroDialogo by remember { mutableStateOf(false) } // Lote PONTE-1
    var mataLeaoDialogo by remember { mutableStateOf(false) }     // Lote PONTE-1
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
                val ehAtaque = m == Manobra.ATAQUE || m == Manobra.ATAQUE_TOTAL || m == Manobra.GOLPE_RAPIDO ||
                    m == Manobra.ATAQUE_DEDICADO || m == Manobra.ATAQUE_DEFENSIVO // Lote PONTE-4: abrem o diálogo de alvo/local
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
                            m == Manobra.ESTRANGULAR -> estrangularDialogo = true
                            m == Manobra.CHAVE_MEMBRO -> chaveMembroDialogo = true // Lote PONTE-1
                            m == Manobra.MATA_LEAO -> mataLeaoDialogo = true         // Lote PONTE-1
                            m == Manobra.MUDAR_POSTURA -> posturaDialogo = true
                            m == Manobra.DEFESA_TOTAL -> defesaTotalDialogo = true
                            m == Manobra.FOGO_RETENCAO -> viewModel.sagaCombateFogoRetencao() // Lote 396: área, sem alvo
                            m == Manobra.AGUARDAR -> viewModel.sagaCombateAguardar() // Lote 399: Interromper Investida
                            m == Manobra.DESVENCILHAR -> viewModel.sagaCombateDesvencilhar() // Lote 422: herói preso se solta
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
            onConfirmar = { alvoId, local, modo, offHand, enganoso, telegrafico, dedicadoModo, benefDefensivo ->
                when {
                    ehMoverAtacar -> viewModel.sagaCombateMoverEAtacar(alvoId, local)
                    manobra == Manobra.GOLPE_RAPIDO -> viewModel.sagaCombateGolpeRapido(alvoId, local) // Lote 408
                    manobra == Manobra.ATAQUE_DEDICADO -> viewModel.sagaCombateAtaqueDedicado(alvoId, local, dedicadoModo) // Lote PONTE-4
                    manobra == Manobra.ATAQUE_DEFENSIVO -> viewModel.sagaCombateAtaqueDefensivo(alvoId, local, benefDefensivo) // Lote PONTE-4
                    modo == AtaqueTotalModo.DUPLO && offHand != null -> viewModel.sagaCombateAtacarDuplo(alvoId, local, offHand)
                    else -> viewModel.sagaCombateAtacar(alvoId, manobra, local, modo, enganoso, telegrafico)
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

    if (estrangularDialogo) {
        SubDialogoEscolherAlvo(
            titulo = "Estrangular quem? (precisa estar agarrado)",
            descricaoConfirmar = "Estrangular o alvo agarrado (Disputa de ST; dano e sufocamento)",
            alvos = estado.combatentes.filter { !it.ehHeroi && it.vivo && it.condicoes.contains("agarrado") },
            onConfirmar = { alvoId -> viewModel.sagaCombateEstrangular(alvoId); estrangularDialogo = false },
            onFechar = { estrangularDialogo = false }
        )
    }

    if (chaveMembroDialogo) {
        SubDialogoEscolherAlvo(
            titulo = "Chave de membro em quem? (precisa estar agarrado)",
            descricaoConfirmar = "Aplicar chave no braço do alvo agarrado (Disputa de ST; dano por contusão)",
            alvos = estado.combatentes.filter { !it.ehHeroi && it.vivo && it.condicoes.contains("agarrado") },
            onConfirmar = { alvoId -> viewModel.sagaCombateChaveMembro(alvoId); chaveMembroDialogo = false },
            onFechar = { chaveMembroDialogo = false }
        )
    }

    if (mataLeaoDialogo) {
        SubDialogoEscolherAlvo(
            titulo = "Mata-leão em quem? (precisa estar agarrado)",
            descricaoConfirmar = "Estrangular com as duas mãos (+3 ST; dano no pescoço e sufocamento)",
            alvos = estado.combatentes.filter { !it.ehHeroi && it.vivo && it.condicoes.contains("agarrado") },
            onConfirmar = { alvoId -> viewModel.sagaCombateMataLeao(alvoId); mataLeaoDialogo = false },
            onFechar = { mataLeaoDialogo = false }
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
    onConfirmar: (alvoId: String, local: LocalAtaque, modo: AtaqueTotalModo, offHandIndex: Int?, enganoso: Int, telegrafico: Boolean, dedicadoModo: DedicadoModo, benefDefensivo: CombatResolver.TipoDefesa?) -> Unit,
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
    var telegrafico by remember { mutableStateOf(false) } // Lote PONTE-3: Ataque Telegráfico (AM p.109)
    var dedicadoModo by remember { mutableStateOf(DedicadoModo.DETERMINADO) } // Lote PONTE-4
    var benefDefensivo by remember { mutableStateOf(CombatResolver.TipoDefesa.APARA) } // Lote PONTE-4: defesa que ganha +1

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
                if (manobra == Manobra.ATAQUE && !armaDistancia && maxEnganoso > 0) {
                    Spacer(Modifier.height(8.dp))
                    Text("Ataque Enganoso (−2 no acerto por −1 na defesa do alvo)",
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = { if (enganoso > 0) enganoso-- }, enabled = enganoso > 0,
                            modifier = Modifier.semantics { contentDescription = "Diminuir engano" }) { Text("−") }
                        // No passo 0 a "−" fica cinza (não há como descer): deixa claro que é a "+" que ativa o engano.
                        Text(
                            if (enganoso == 0) "  toque +  (cada passo: −2 no acerto, −1 na defesa do alvo; até $maxEnganoso)  "
                            else "  $enganoso  →  −${enganoso * 2} no acerto / −$enganoso na defesa do alvo  ",
                            style = MaterialTheme.typography.bodyMedium)
                        OutlinedButton(onClick = { if (enganoso < maxEnganoso && !telegrafico) enganoso++ }, enabled = enganoso < maxEnganoso && !telegrafico,
                            modifier = Modifier.semantics { contentDescription = "Aumentar engano" }) { Text("+") }
                    }
                } else if (manobra == Manobra.ATAQUE && !armaDistancia) {
                    // Item 4 do teste: explicar POR QUE o Enganoso não aparece (ex.: Briga com NH baixo) em vez de só sumir.
                    Spacer(Modifier.height(8.dp))
                    Text("Ataque Enganoso indisponível: o NH efetivo não pode cair abaixo de 10, então é preciso NH ≥ 12 no ataque escolhido.",
                        style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // Ataque Telegráfico (Lote PONTE-3, AM p.109): +4 para acertar, mas +2 nas defesas do alvo. Oposto do
                // Enganoso (exclusivos). Corpo-a-corpo apenas; sem o limite de NH≥12 (o +4 nunca derruba o NH).
                if (manobra == Manobra.ATAQUE && !armaDistancia) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = telegrafico, enabled = enganoso == 0,
                            onCheckedChange = { telegrafico = it },
                            modifier = Modifier.semantics { contentDescription = "Ataque telegráfico: +4 para acertar, +2 nas defesas do alvo" })
                        Spacer(Modifier.width(8.dp))
                        Text("Ataque Telegráfico (+4 no acerto, mas +2 em todas as defesas do alvo)",
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (manobra == Manobra.ATAQUE_DEDICADO) {
                    Spacer(Modifier.height(8.dp))
                    Text("Modo do Ataque Dedicado (−2 nas suas defesas até o próximo turno)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    OpcaoRadio(selecionado = dedicadoModo == DedicadoModo.DETERMINADO, rotulo = "Determinado (+2 para acertar)",
                        descricao = "Ataque Dedicado Determinado", onClick = { dedicadoModo = DedicadoModo.DETERMINADO })
                    OpcaoRadio(selecionado = dedicadoModo == DedicadoModo.FORTE, rotulo = "Forte (+1 de dano)",
                        descricao = "Ataque Dedicado Forte", onClick = { dedicadoModo = DedicadoModo.FORTE })
                }
                if (manobra == Manobra.ATAQUE_DEFENSIVO) {
                    Spacer(Modifier.height(8.dp))
                    Text("Ataque Defensivo (−2 de dano, ou −1/dado, o pior) — defesa reforçada (+1):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    OpcaoRadio(selecionado = benefDefensivo == CombatResolver.TipoDefesa.APARA, rotulo = "Aparar +1",
                        descricao = "Ataque Defensivo: +1 ao Aparar", onClick = { benefDefensivo = CombatResolver.TipoDefesa.APARA })
                    OpcaoRadio(selecionado = benefDefensivo == CombatResolver.TipoDefesa.BLOQUEIO, rotulo = "Bloquear +1",
                        descricao = "Ataque Defensivo: +1 ao Bloquear", onClick = { benefDefensivo = CombatResolver.TipoDefesa.BLOQUEIO })
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
                            if (manobra == Manobra.ATAQUE) enganoso else 0,
                            manobra == Manobra.ATAQUE && telegrafico,
                            dedicadoModo, benefDefensivo)
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
                val sufixo = when { // Lote 389/404/405/414
                    op.recuo -> " ↩ recuar"
                    op.jogarSeAoChao -> " ⤓ jogar-se ao chão"
                    op.maoInabil -> " 🤚 mão inábil"
                    op.acrobatica -> " 🤸 acrobática (±2)"
                    else -> ""
                }
                val rotulo = buildString {
                    if (op.magiaBloqueioNome != null) { // Lote MA-3d-3: defesa por mágica de Bloqueio
                        append("🔮 ${op.magiaBloqueioNome} (bloqueio) ${op.valorFinal}")
                    } else {
                        append("${op.tipo.rotulo}$sufixo ${op.valorFinal}")
                        if (comps.isNotBlank()) append("  ($comps)")
                    }
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

// ── Lote TOK-6b-2: menu do TOKEN (carrossel translúcido sobre a grade) ──────────────────────────
//
// A manobra mora onde ela acontece: tocar no SEU token abre as manobras sobre si mesmo; tocar num
// INIMIGO abre as ações direcionadas a ele (o toque JÁ escolhe o alvo — os diálogos pulam essa
// etapa). Mover não é botão: continua sendo tocar num hex verde (TOK-4).

/** Manobras que o herói faz SOBRE SI MESMO — menu do próprio token (MB p.363-366). */
internal val MANOBRAS_SOBRE_SI = setOf(
    Manobra.MUDAR_POSTURA, Manobra.DEFESA_TOTAL, Manobra.AGUARDAR, Manobra.PREPARAR,
    Manobra.CONCENTRAR, Manobra.DESVENCILHAR, Manobra.FOGO_RETENCAO, Manobra.NAO_FAZER_NADA
)

/** Menu do token do HERÓI: filtra das manobras legais as que não precisam de alvo. */
internal fun menuTaticoHeroi(manobras: List<Manobra>): List<Manobra> =
    manobras.filter { it in MANOBRAS_SOBRE_SI }

/**
 * Menu de um token INIMIGO: manobras direcionadas, gateadas EXATAMENTE pelas precondições do motor
 * (senão o chip dispara, o motor rejeita e `depoisDaAcaoDoHeroi` consome o turno à toa — o soft-fail
 * que o teste de batalha reprovou). Ver `CombatSession`:
 *  - golpes de arma / Fintar exigem o alvo dentro do ALCANCE da arma (`aoAlcance`, estado.alvos);
 *  - Agarrar/Derrubar/Empurrão exigem ADJACÊNCIA real (dist ≤ 1) — não bastam 2 m de lança (MB p.370-371);
 *  - Encontrão carrega até o corpo-a-corpo pelo Deslocamento (`alcancaMovendo`, MB p.371);
 *  - Mover e Atacar exige alcançá-lo com o Deslocamento (estado.alvosMoverEAtacar);
 *  - Avaliar/Apontar valem para qualquer inimigo vivo à vista (MB p.364);
 *  - Estrangular/Chave/Mata-Leão exigem o alvo JÁ AGARRADO (MB p.371, AM p.69-77);
 *  - Imobilizar exige agarrado **E no chão** (deitado/caído) — derrube antes (MB p.371).
 */
internal fun menuTaticoInimigo(
    manobras: List<Manobra>,
    aoAlcance: Boolean,
    adjacente: Boolean,
    alcancaMovendo: Boolean,
    agarrado: Boolean,
    alvoNoChao: Boolean,
): List<Manobra> = manobras.filter { m ->
    when (m) {
        Manobra.ATAQUE, Manobra.ATAQUE_TOTAL, Manobra.ATAQUE_DEDICADO, Manobra.ATAQUE_DEFENSIVO,
        Manobra.GOLPE_RAPIDO, Manobra.FINTAR -> aoAlcance
        Manobra.AGARRAR, Manobra.DERRUBAR, Manobra.EMPURRAO -> adjacente
        Manobra.ENCONTRAO -> alcancaMovendo
        Manobra.MOVER_E_ATACAR -> alcancaMovendo
        Manobra.AVALIAR, Manobra.APONTAR -> true
        Manobra.ESTRANGULAR, Manobra.CHAVE_MEMBRO, Manobra.MATA_LEAO -> agarrado
        Manobra.IMOBILIZAR -> agarrado && alvoNoChao
        else -> false // MOVER (hex verde) e manobras sobre si nunca aparecem no inimigo
    }
}

/** Ícone decorativo do chip (o rótulo é quem carrega o significado). */
internal fun iconeDaManobra(m: Manobra): String = when (m) {
    Manobra.ATAQUE -> "⚔️"
    Manobra.ATAQUE_TOTAL -> "💥"
    Manobra.ATAQUE_DEDICADO -> "⚡"
    Manobra.ATAQUE_DEFENSIVO -> "🤺"
    Manobra.GOLPE_RAPIDO -> "🌀"
    Manobra.MOVER_E_ATACAR -> "🏃"
    Manobra.FINTAR -> "🎭"
    Manobra.AVALIAR -> "👁️"
    Manobra.APONTAR -> "🎯"
    Manobra.AGARRAR -> "🤜"
    Manobra.DERRUBAR -> "⤵️"
    Manobra.ENCONTRAO -> "🐏"
    Manobra.EMPURRAO -> "🖐️"
    Manobra.IMOBILIZAR -> "🔒"
    Manobra.ESTRANGULAR -> "🪢"
    Manobra.CHAVE_MEMBRO -> "🦾"
    Manobra.MATA_LEAO -> "🦁"
    Manobra.MUDAR_POSTURA -> "🧎"
    Manobra.DEFESA_TOTAL -> "🛡️"
    Manobra.AGUARDAR -> "⏳"
    Manobra.PREPARAR -> "🎒"
    Manobra.CONCENTRAR -> "🧘"
    Manobra.FOGO_RETENCAO -> "🔫"
    Manobra.DESVENCILHAR -> "✊"
    Manobra.NAO_FAZER_NADA -> "💤"
    Manobra.MOVER -> "🏃"
}

/**
 * Carrossel de manobras do token selecionado na grade. Renderiza NADA quando não é a vez do
 * herói, há defesa pendente ou o combate acabou (o painel de baixo cuida desses estados).
 * Ações que só precisam do alvo disparam DIRETO (o toque no token já foi a escolha); as que
 * pedem parâmetro extra (local do golpe, modo, postura…) reusam os sub-diálogos com o alvo fixo.
 */
@Composable
fun MenuTaticoDoToken(
    viewModel: FichaViewModel,
    tokenId: String,
    onFechar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val estado = viewModel.sagaCombateEstado ?: return
    if (estado.encerrado || !estado.vezDoHeroi || viewModel.sagaCombateDefesaPendente != null) return

    var alvoDialogo by remember(tokenId) { mutableStateOf<Manobra?>(null) }
    var apontarDialogo by remember(tokenId) { mutableStateOf(false) }
    var posturaDialogo by remember(tokenId) { mutableStateOf(false) }
    var defesaTotalDialogo by remember(tokenId) { mutableStateOf(false) }
    var trocarArmaDialogo by remember(tokenId) { mutableStateOf(false) } // Lote TOK-6b-3: Trocar arma virou chip do token
    var conjurarDialogo by remember(tokenId) { mutableStateOf(false) }    // Lote MA-3a: chip 🔮 Conjurar

    val ehHeroi = tokenId == "heroi"
    val alvo = if (ehHeroi) null
        else estado.combatentes.firstOrNull { it.id == tokenId && !it.ehHeroi && it.vivo } ?: return
    val manobras = if (ehHeroi) {
        menuTaticoHeroi(estado.manobrasHeroi)
    } else {
        menuTaticoInimigo(
            manobras = estado.manobrasHeroi,
            aoAlcance = estado.alvos.any { it.id == tokenId },
            adjacente = alvo!!.distanciaM <= 1, // agarrar/derrubar/empurrão exigem corpo-a-corpo real
            alcancaMovendo = estado.alvosMoverEAtacar.any { it.id == tokenId },
            agarrado = alvo.condicoes.contains(Condicao.AGARRADO.rotulo),
            alvoNoChao = alvo.postura == Postura.DEITADO.rotulo || alvo.condicoes.contains(Condicao.CAIDO.rotulo),
        )
    }

    Surface(
        color = Color(0xCC10161F),
        contentColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (ehHeroi) "Você" else "→ ${alvo!!.nome}",
                style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 6.dp)
            )
            if (manobras.isEmpty() && !ehHeroi) {
                // Se o herói NÃO tem nenhuma manobra direcionada (atordoado/agarrado → só age sobre
                // si), o menu vazio não é "fora de alcance" — mandar tocar no próprio token.
                val temDirecionada = estado.manobrasHeroi.any { it !in MANOBRAS_SOBRE_SI && it != Manobra.MOVER }
                Text(
                    if (temDirecionada) "Fora de alcance — avance pelos hexes verdes (toque no SEU token)"
                    else "Você só pode agir sobre si agora — toque no SEU token",
                    style = MaterialTheme.typography.labelSmall, fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
            manobras.forEach { m ->
                val descricao = m.rotulo + if (ehHeroi) "" else " em ${alvo!!.nome}"
                Surface(
                    onClick = {
                        when (m) {
                            // Pedem parâmetro extra → sub-diálogo com o alvo já fixado.
                            Manobra.ATAQUE, Manobra.ATAQUE_TOTAL, Manobra.ATAQUE_DEDICADO,
                            Manobra.ATAQUE_DEFENSIVO, Manobra.GOLPE_RAPIDO, Manobra.MOVER_E_ATACAR ->
                                alvoDialogo = m
                            Manobra.APONTAR -> apontarDialogo = true
                            Manobra.MUDAR_POSTURA -> posturaDialogo = true
                            Manobra.DEFESA_TOTAL -> defesaTotalDialogo = true
                            // O toque no token já escolheu o alvo → dispara direto.
                            Manobra.AVALIAR -> { viewModel.sagaCombateAvaliar(tokenId); onFechar() }
                            Manobra.FINTAR -> { viewModel.sagaCombateFintar(tokenId); onFechar() }
                            Manobra.AGARRAR -> { viewModel.sagaCombateAgarrar(tokenId); onFechar() }
                            Manobra.DERRUBAR -> { viewModel.sagaCombateDerrubar(tokenId); onFechar() }
                            Manobra.ENCONTRAO -> { viewModel.sagaCombateEncontrao(tokenId); onFechar() }
                            Manobra.EMPURRAO -> { viewModel.sagaCombateEmpurrao(tokenId); onFechar() }
                            Manobra.IMOBILIZAR -> { viewModel.sagaCombateImobilizar(tokenId); onFechar() }
                            Manobra.ESTRANGULAR -> { viewModel.sagaCombateEstrangular(tokenId); onFechar() }
                            Manobra.CHAVE_MEMBRO -> { viewModel.sagaCombateChaveMembro(tokenId); onFechar() }
                            Manobra.MATA_LEAO -> { viewModel.sagaCombateMataLeao(tokenId); onFechar() }
                            Manobra.AGUARDAR -> { viewModel.sagaCombateAguardar(); onFechar() }
                            Manobra.FOGO_RETENCAO -> { viewModel.sagaCombateFogoRetencao(); onFechar() }
                            Manobra.DESVENCILHAR -> { viewModel.sagaCombateDesvencilhar(); onFechar() }
                            else -> { viewModel.sagaCombateManobra(m); onFechar() } // Preparar/Concentrar/Nada
                        }
                    },
                    color = Color(0x33FFFFFF),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.padding(horizontal = 3.dp)
                        .semantics { contentDescription = "Manobra $descricao" }
                ) {
                    Text(
                        "${iconeDaManobra(m)} ${m.rotulo}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }
            }
            // Lote MA-3d-2: mágica de TOQUE carregada — chip de ENTREGAR num inimigo adjacente
            // (menu do inimigo) e de DISSIPAR (menu do herói).
            if (!ehHeroi && estado.toqueCarregado != null && alvo != null && alvo.distanciaM <= 1) {
                Surface(
                    onClick = { viewModel.sagaCombateEntregarToque(tokenId); onFechar() },
                    color = Color(0x33FF8A65), contentColor = Color.White, shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.padding(horizontal = 3.dp)
                        .semantics { contentDescription = "Descarregar ${estado.toqueCarregado} em ${alvo.nome}" }
                ) {
                    Text("✋ ${estado.toqueCarregado}", style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp))
                }
            }
            if (ehHeroi && estado.toqueCarregado != null) {
                Surface(
                    onClick = { viewModel.sagaCombateDissiparToque(); onFechar() },
                    color = Color(0x33FF8A65), contentColor = Color.White, shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.padding(horizontal = 3.dp)
                        .semantics { contentDescription = "Dissipar ${estado.toqueCarregado} da mão" }
                ) {
                    Text("✋ Dissipar", style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp))
                }
            }
            // Lote MA-3a: chip 🔮 Conjurar — só no herói e só se ele conhece magias.
            if (ehHeroi && estado.magiasConjuraveis.isNotEmpty()) {
                Surface(
                    onClick = { conjurarDialogo = true },
                    color = Color(0x334FC3F7), contentColor = Color.White, shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.padding(horizontal = 3.dp)
                        .semantics { contentDescription = "Conjurar magia" }
                ) {
                    Text("🔮 Conjurar", style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp))
                }
            }
            // Lote TOK-6b-3: "Trocar arma" saiu do painel de baixo (removido) e virou chip do herói —
            // abre o diálogo de armas; sacar é Preparar (gasta o turno) ou livre com Saque Rápido.
            if (ehHeroi && estado.ataques.size > 1) {
                val armaAtual = estado.ataqueAtual?.rotulo?.substringBefore(" (")?.trim() ?: "arma"
                Surface(
                    onClick = { trocarArmaDialogo = true },
                    color = Color(0x33FFFFFF), contentColor = Color.White, shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.padding(horizontal = 3.dp)
                        .semantics { contentDescription = "Trocar arma — empunhando $armaAtual" }
                ) {
                    Text("🔄 $armaAtual", style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp))
                }
            }
            Surface(
                onClick = onFechar,
                color = Color(0x33FFFFFF), contentColor = Color.White, shape = CircleShape,
                modifier = Modifier.padding(start = 4.dp)
                    .semantics { contentDescription = "Fechar o menu do token" }
            ) { Text("✕", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) }
        }
    }

    // ── Sub-diálogos com o ALVO FIXO (lista de 1 → já pré-selecionado) ──
    alvoDialogo?.let { manobra ->
        SubDialogoAlvoLocal(
            manobra = manobra,
            alvos = listOf(alvo!!),
            ataques = estado.ataques,
            ataqueSelecionado = estado.ataqueSelecionado,
            ambidestro = estado.heroiAmbidestro,
            onConfirmar = { alvoId, local, modo, offHand, enganoso, telegrafico, dedicadoModo, benefDefensivo ->
                when {
                    manobra == Manobra.MOVER_E_ATACAR -> viewModel.sagaCombateMoverEAtacar(alvoId, local)
                    manobra == Manobra.GOLPE_RAPIDO -> viewModel.sagaCombateGolpeRapido(alvoId, local)
                    manobra == Manobra.ATAQUE_DEDICADO -> viewModel.sagaCombateAtaqueDedicado(alvoId, local, dedicadoModo)
                    manobra == Manobra.ATAQUE_DEFENSIVO -> viewModel.sagaCombateAtaqueDefensivo(alvoId, local, benefDefensivo)
                    modo == AtaqueTotalModo.DUPLO && offHand != null -> viewModel.sagaCombateAtacarDuplo(alvoId, local, offHand)
                    else -> viewModel.sagaCombateAtacar(alvoId, manobra, local, modo, enganoso, telegrafico)
                }
                alvoDialogo = null; onFechar()
            },
            onFechar = { alvoDialogo = null }
        )
    }

    if (apontarDialogo) {
        SubDialogoApontar(
            alvos = listOf(alvo!!),
            podeFirmar = estado.ataques.getOrNull(estado.ataqueSelecionado)?.armaDeFogo == true,
            onConfirmar = { alvoId, firmado -> viewModel.sagaCombateApontar(alvoId, firmado); apontarDialogo = false; onFechar() },
            onFechar = { apontarDialogo = false }
        )
    }

    if (posturaDialogo) {
        SubDialogoPostura(
            posturaAtual = estado.posturaHeroi,
            posturas = estado.posturasAlcancaveis,
            onConfirmar = { postura -> viewModel.sagaCombateManobra(Manobra.MUDAR_POSTURA, postura); posturaDialogo = false; onFechar() },
            onFechar = { posturaDialogo = false }
        )
    }

    if (defesaTotalDialogo) {
        SubDialogoDefesaTotal(
            onConfirmar = { modo, aumentadaEm -> viewModel.sagaCombateDefesaTotal(modo, aumentadaEm); defesaTotalDialogo = false; onFechar() },
            onFechar = { defesaTotalDialogo = false }
        )
    }

    if (trocarArmaDialogo) {
        SubDialogoTrocarArma(
            ataques = estado.ataques,
            selecionado = estado.ataqueSelecionado,
            onEscolher = { i -> viewModel.sagaCombateSacarArma(i); trocarArmaDialogo = false; onFechar() },
            onFechar = { trocarArmaDialogo = false }
        )
    }

    if (conjurarDialogo) {
        SubDialogoConjurar(
            magias = estado.magiasConjuraveis,
            inimigos = estado.combatentes.filter { !it.ehHeroi && it.vivo },
            onConjurar = { magiaId, alvoId, energia, pvQueimar, causaDano ->
                viewModel.sagaCombateConjurar(magiaId, alvoId, energia, pvQueimar, causaDano); conjurarDialogo = false; onFechar()
            },
            onMirarArea = { magiaId, raio, energia, pvQueimar, causaDano ->
                viewModel.sagaIniciarMiraArea(magiaId, raio, energia, pvQueimar, causaDano); conjurarDialogo = false; onFechar()
            },
            onFechar = { conjurarDialogo = false }
        )
    }
}

/**
 * Lote MA-3a: seletor de conjuração. Escolhe a magia, o alvo (um inimigo ou "em mim mesmo") e, para
 * Projéteis, quanta energia investir (1d de dano por ponto, teto na Aptidão Mágica). Conjurar é a
 * manobra Concentrar — gasta o turno. Efeitos bespoke são narrados pelo Mestre (MA-4).
 */
@Composable
private fun SubDialogoConjurar(
    magias: List<com.gurps.ficha.viewmodel.delegates.MagiaConjuravelUi>,
    inimigos: List<CombatenteUi>,
    onConjurar: (magiaId: String, alvoId: String?, energia: Int, pvQueimar: Int, causaDano: Boolean) -> Unit,
    onMirarArea: (magiaId: String, raio: Int, energia: Int, pvQueimar: Int, causaDano: Boolean) -> Unit,
    onFechar: () -> Unit,
) {
    var magiaSel by remember { mutableStateOf(magias.firstOrNull()) }
    // null = "em mim mesmo" (automagia); senão o id do inimigo.
    var alvoId by remember { mutableStateOf<String?>(inimigos.firstOrNull()?.id) }
    var energia by remember { mutableIntStateOf(1) }
    var pvQueimar by remember { mutableIntStateOf(0) }
    var raio by remember { mutableIntStateOf(2) } // Lote MA-3d: raio da magia de área
    var causaDano by remember(magiaSel?.id) { mutableStateOf(false) } // Lote MA-6: magia de dano direta
    val ehArea = magiaSel?.ehArea == true

    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text("Conjurar magia") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("Conjurar é a manobra Concentrar — gasta o turno.",
                    style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Text("Magia", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                magias.forEach { m ->
                    OpcaoRadio(
                        selecionado = magiaSel?.id == m.id,
                        rotulo = "${m.nome} — ${m.classe}, NH ${m.nhBasico}, ${m.custoTexto}" + if (!m.castavel) " (${m.motivo})" else "",
                        descricao = "Conjurar ${m.nome}, classe ${m.classe}, custo ${m.custoTexto}" + if (!m.castavel) ", indisponível: ${m.motivo}" else "",
                        onClick = { magiaSel = m; if (!m.ehProjetil) energia = 1 else energia = energia.coerceIn(1, m.aptidaoMagica) }
                    )
                }

                if (ehArea) {
                    // Área (Lote MA-3d): o alvo é um HEX no grid; aqui só se escolhe o RAIO (custo × raio).
                    Spacer(Modifier.height(8.dp))
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
                } else if (magiaSel?.ehToque == true) {
                    // Toque (Lote MA-3d-2): lança em si (carrega a mão) → entrega depois num ataque.
                    Spacer(Modifier.height(8.dp))
                    Text("Toque: a mágica carrega sua mão. Depois, ataque um inimigo adjacente para descarregá-la.",
                        style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Spacer(Modifier.height(8.dp))
                    Text("Alvo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    OpcaoRadio(alvoId == null, "Em mim mesmo (automagia)", "Conjurar sobre si mesmo") { alvoId = null }
                    inimigos.forEach { a ->
                        OpcaoRadio(alvoId == a.id, "${a.nome} (${a.distanciaM}m)", "Alvo ${a.nome}") { alvoId = a.id }
                    }
                }

                // Lote MA-6: magia de dano DIRETA (Comum/Área que não é Projétil/Toque) — o jogador marca
                // "causa dano" e o motor aplica 1d por energia (diretriz de Mágicas de Combate, Magia p.14).
                val proj = magiaSel?.ehProjetil == true
                val podeMarcarDano = magiaSel != null && !proj && magiaSel!!.ehToque.not() && (ehArea || alvoId != null)
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
                // Energia investida → dados de dano (Projétil sempre; Comum/Área quando "causa dano").
                if (proj || (podeMarcarDano && causaDano)) {
                    Spacer(Modifier.height(8.dp))
                    Text("Energia investida: ${energia} (→ ${energia}d de dano)", fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = { if (energia > 1) energia-- },
                            modifier = Modifier.semantics { contentDescription = "Menos energia" }) { Text("−") }
                        Text("${energia}", Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold)
                        val teto = magiaSel?.aptidaoMagica ?: 1
                        OutlinedButton(onClick = { if (energia < teto) energia++ },
                            modifier = Modifier.semantics { contentDescription = "Mais energia" }) { Text("+") }
                    }
                }

                // Queimar PV (Magia p.8): paga parte do custo com PV em vez de PF — cada PV é −1 no NH.
                val tetoPv = (magiaSel?.custoEstimado ?: 0).coerceAtMost(4)
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
            }
        },
        confirmButton = {
            val m = magiaSel
            Button(
                onClick = {
                    if (m != null) {
                        // Energia vira dados de dano no Projétil, ou quando o jogador marcou "causa dano".
                        val energiaEfetiva = if (m.ehProjetil || causaDano) energia else 1
                        when {
                            m.ehArea -> onMirarArea(m.id, raio, energiaEfetiva, pvQueimar, causaDano)
                            m.ehToque -> onConjurar(m.id, null, 1, pvQueimar, false) // Toque lança em si → carrega a mão
                            else -> onConjurar(m.id, alvoId, energiaEfetiva, pvQueimar, causaDano)
                        }
                    }
                },
                enabled = m != null && m.castavel,
                modifier = Modifier.semantics {
                    contentDescription = when {
                        ehArea -> "Mirar a área no grid"; magiaSel?.ehToque == true -> "Carregar a mágica na mão"
                        else -> "Conjurar a magia escolhida"
                    }
                }
            ) { Text(when { ehArea -> "Mirar no grid"; magiaSel?.ehToque == true -> "Carregar na mão"; else -> "Conjurar" }) }
        },
        dismissButton = { TextButton(onClick = onFechar) { Text("Cancelar") } }
    )
}

/** Lote TOK-6b-3: troca de arma como diálogo do token (substitui o painel de arma fixo do rodapé). */
@Composable
private fun SubDialogoTrocarArma(
    ataques: List<AtaqueHeroi>,
    selecionado: Int,
    onEscolher: (Int) -> Unit,
    onFechar: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text("Trocar arma") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "Sacar uma arma é a manobra Preparar (gasta o turno) — livre com Saque Rápido.",
                    style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                ataques.forEachIndexed { i, atk ->
                    val empunhada = i == selecionado
                    val alcanceTxt = if (atk.aDistancia) "à distância (Máx ${atk.alcance}m)" else "corpo-a-corpo (${atk.alcance}m)"
                    OpcaoRadio(
                        selecionado = empunhada,
                        rotulo = "${atk.rotulo} — NH ${atk.nh}, ${atk.danoExpr} ${atk.tipo.rotulo}" + if (empunhada) " (na mão)" else "",
                        descricao = if (empunhada) "${atk.rotulo}, já empunhada, $alcanceTxt" else "Sacar ${atk.rotulo}, $alcanceTxt",
                        onClick = { if (!empunhada) onEscolher(i) else onFechar() }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onFechar) { Text("Cancelar") } }
    )
}
