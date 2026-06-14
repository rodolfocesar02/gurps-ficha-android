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
                val ehAtaque = m == Manobra.ATAQUE || m == Manobra.ATAQUE_TOTAL
                val temAlvo = estado.alvos.isNotEmpty()
                Button(
                    onClick = {
                        when {
                            ehAtaque && temAlvo -> alvoDialogo = m
                            m == Manobra.MOVER -> viewModel.sagaCombateMover(afastar = false)
                            else -> viewModel.sagaCombateManobra(m)
                        }
                    },
                    enabled = !(ehAtaque && !temAlvo),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        .semantics { contentDescription = "Manobra ${m.rotulo}" + if (ehAtaque && !temAlvo) ", sem alvo ao alcance" else "" }
                ) { Text(m.rotulo + if (ehAtaque && !temAlvo) " (sem alvo)" else "") }
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
        SubDialogoAlvoLocal(
            manobra = manobra,
            alvos = estado.alvos,
            onConfirmar = { alvoId, local, modo ->
                viewModel.sagaCombateAtacar(alvoId, manobra, local, modo)
                alvoDialogo = null
            },
            onFechar = { alvoDialogo = null }
        )
    }
}

@Composable
private fun SeletorDeArma(viewModel: FichaViewModel, estado: com.gurps.ficha.viewmodel.delegates.CombatUiState) {
    val atual = estado.ataqueAtual ?: return
    var aberto by remember { mutableStateOf(false) }
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().semantics {
            contentDescription = "Arma empunhada: ${atual.rotulo}, NH ${atual.nh}, dano ${atual.danoExpr}. Toque para trocar."
        }
    ) {
        Column(Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Empunhando: ${atual.rotulo}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "NH ${atual.nh} · ${atual.danoExpr} ${atual.tipo.rotulo}" + if (atual.aDistancia) " · à distância" else " · corpo-a-corpo",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (estado.ataques.size > 1) {
                    TextButton(onClick = { aberto = true },
                        modifier = Modifier.semantics { contentDescription = "Trocar de arma" }) { Text("Trocar") }
                }
            }
            if (aberto) {
                estado.ataques.forEachIndexed { i, atk ->
                    OpcaoRadio(
                        selecionado = i == estado.ataqueSelecionado,
                        rotulo = "${atk.rotulo} — NH ${atk.nh}, ${atk.danoExpr} ${atk.tipo.rotulo}",
                        descricao = "Empunhar ${atk.rotulo}",
                        onClick = { viewModel.sagaCombateSelecionarAtaque(i); aberto = false }
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
    onConfirmar: (alvoId: String, local: LocalAtaque, modo: AtaqueTotalModo) -> Unit,
    onFechar: () -> Unit
) {
    var alvoId by remember { mutableStateOf(alvos.firstOrNull()?.id ?: "") }
    var local by remember { mutableStateOf(LocalAtaque.TORSO) }
    var modo by remember { mutableStateOf(AtaqueTotalModo.DETERMINADO) }

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
                if (manobra == Manobra.ATAQUE_TOTAL) {
                    Spacer(Modifier.height(8.dp))
                    Text("Modo do Ataque Total", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    listOf(AtaqueTotalModo.DETERMINADO, AtaqueTotalModo.FORTE).forEach { mo ->
                        val desc = if (mo == AtaqueTotalModo.DETERMINADO) "+4 para acertar" else "+2 de dano"
                        OpcaoRadio(
                            selecionado = modo == mo,
                            rotulo = "${mo.rotulo} ($desc)",
                            descricao = "${mo.rotulo}, $desc",
                            onClick = { modo = mo }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (alvoId.isNotBlank()) onConfirmar(alvoId, local, modo) },
                modifier = Modifier.semantics { contentDescription = "Confirmar ataque" }
            ) { Text("Atacar") }
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
                val rotulo = buildString {
                    append("${op.tipo.rotulo} ${op.valorFinal}")
                    if (comps.isNotBlank()) append("  ($comps)")
                    if (!op.disponivel) append(" — ${op.motivoIndisponivel}")
                }
                Button(
                    onClick = { if (op.disponivel) viewModel.sagaCombateDefender(op) },
                    enabled = op.disponivel,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        .semantics { contentDescription = "${op.tipo.rotulo} valor ${op.valorFinal}" + if (!op.disponivel) ", indisponível" else ", rolar" }
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
