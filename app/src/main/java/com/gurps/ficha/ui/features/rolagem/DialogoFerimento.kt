package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.CoberturaDaArmadura
import com.gurps.ficha.domain.rules.DanoTipo
import com.gurps.ficha.domain.rules.FerimentoPorLocalRules
import com.gurps.ficha.domain.rules.LocalAtaque
import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.model.TipoEquipamento
import com.gurps.ficha.ui.AppBotaoPrincipal
import com.gurps.ficha.ui.AppBotaoSecundario
import com.gurps.ficha.ui.AppFileiraDeBotoes
import com.gurps.ficha.ui.AppFiltroChip
import com.gurps.ficha.ui.UiEstilos
import com.gurps.ficha.ui.UiTokens
import com.gurps.ficha.ui.linhaAlternavel

/**
 * **O botão PV** — Lote MB-7 (MB p.399-400 e 419-422).
 *
 * O Mestre diz *"5 de corte no braço"*; o jogador toca o local, digita o dano,
 * escolhe o tipo — e o app faz o que ninguém faz de cabeça no meio da mesa: tira
 * a RD certa **daquele** local, multiplica pelo tipo **e** pelo local, aplica o
 * teto do membro e diz se o braço foi incapacitado ou decepado.
 *
 * ## 🔴 A RD vem das armaduras que ele está VESTINDO
 *
 * A ficha sabe quais peças o personagem comprou e o que cada uma cobre. O que
 * ela não sabia é se ele está **usando** — e comprar não é vestir. Por isso cada
 * peça que cobre o local escolhido aparece com a sua caixinha, e a marca fica
 * salva na ficha: quem tira a armadura para dormir não precisa remarcar a cada
 * golpe.
 *
 * ⚠️ **Somar camadas é decisão do Mestre.** O app soma e **mostra as peças** —
 * esconder a conta seria pior que somar errado.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DialogoFerimento(
    pvInicial: Int,
    pvAtual: Int,
    equipamentos: List<Equipamento>,
    guardadas: Set<String>,
    onSalvar: (pvNovo: Int, guardadas: Set<String>, resumo: String) -> Unit,
    onFechar: () -> Unit
) {
    var local by remember { mutableStateOf(LocalAtaque.TORSO) }
    var tipo by remember { mutableStateOf(DanoTipo.CORT) }
    var danoTexto by remember { mutableStateOf("") }
    var usarRd by remember { mutableStateOf(true) }
    // ⚠️ A ficha não guarda o sexo do personagem, e o dobro de choque na virilha
    // só vale para "humanoide macho" (MB p.400). Deixar fixo em "sim" daria −8
    // onde metade dos personagens leva −4 — então a caixinha aparece, e só
    // quando o golpe é ali.
    var masculino by remember { mutableStateOf(true) }
    // ⚠️ A ficha guarda o que está GUARDADO, não o que está vestido. Assim uma
    // ficha antiga (lista vazia) continua vestindo tudo, que é o caso comum —
    // guardar o inverso faria todo personagem existente ficar nu de repente.
    val naMochila = remember(guardadas) { mutableStateListOf<String>().apply { addAll(guardadas) } }

    val armaduras = remember(equipamentos) {
        equipamentos.filter { it.tipo == TipoEquipamento.ARMADURA && !it.confiscado }
    }
    val noLocal = armaduras.filter { CoberturaDaArmadura.cobre(it.armaduraLocal, local) }
    val pecasVestidas = noLocal
        .filter { it.nome !in naMochila }
        .mapNotNull { eq ->
            CoberturaDaArmadura.rdDe(eq.rdArmaduraExibicao())?.let { CoberturaDaArmadura.Peca(eq.nome, it) }
        }
    val rd = if (usarRd) CoberturaDaArmadura.rdTotal(pecasVestidas) else 0

    val dano = danoTexto.toIntOrNull() ?: 0
    val resultado = remember(dano, tipo, local, rd, pvInicial, masculino) {
        if (dano <= 0) null else FerimentoPorLocalRules.aplicar(
            pvInicial = pvInicial,
            danoBruto = dano,
            tipo = tipo,
            local = local,
            rdArmadura = rd,
            masculino = masculino
        )
    }
    val pvNovo = pvAtual - (resultado?.pvPerdidos ?: 0)

    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text("Ferimento", style = UiEstilos.tituloDialogo) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(UiTokens.ItemSpacing)
            ) {
                Text("PV atual: $pvAtual de $pvInicial", style = UiEstilos.subtituloDialogo)

                Text("Onde acertou", style = UiEstilos.subtituloDialogo, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(UiTokens.ItemSpacing)) {
                    LOCAIS_NA_TELA.forEach { l ->
                        AppFiltroChip(rotuloDoLocal(l), l == local) { local = l }
                    }
                }

                Text("Tipo de dano", style = UiEstilos.subtituloDialogo, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(UiTokens.ItemSpacing)) {
                    DanoTipo.entries.forEach { t ->
                        AppFiltroChip(rotuloDoTipo(t), t == tipo) { tipo = t }
                    }
                }

                OutlinedTextField(
                    value = danoTexto,
                    onValueChange = { novo -> danoTexto = novo.filter { it.isDigit() }.take(4) },
                    label = { Text("Dano rolado (antes da RD)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                if (local == LocalAtaque.INGLE) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .linhaAlternavel(
                                marcado = masculino,
                                descricao = "Humanoide macho: dobra o choque por contusão na virilha, até menos oito.",
                                onAlternar = { masculino = !masculino }
                            )
                            .padding(
                                horizontal = UiTokens.LinhaDeListaPaddingH,
                                vertical = UiTokens.LinhaDeListaPaddingV
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = masculino, onCheckedChange = null)
                        Text(
                            "Humanoide macho — dobra o choque por contusão (até −8)",
                            style = UiEstilos.nomeDoItem,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = UiTokens.ItemSpacing))
                PainelDaArmadura(
                    local = local,
                    noLocal = noLocal,
                    naMochila = naMochila,
                    usarRd = usarRd,
                    rd = rd,
                    onUsarRd = { usarRd = !usarRd },
                    onVestir = { nome -> if (nome in naMochila) naMochila.remove(nome) else naMochila.add(nome) }
                )

                resultado?.let { r ->
                    HorizontalDivider(modifier = Modifier.padding(vertical = UiTokens.ItemSpacing))
                    Text(
                        "−${r.pvPerdidos} PV   →   $pvNovo de $pvInicial",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    // A conta escrita: o jogador precisa poder conferir de onde
                    // saiu o número, senão o app vira caixa-preta na mesa.
                    Text(r.conta, style = UiEstilos.detalheDoItem, color = MaterialTheme.colorScheme.outline)
                    if (r.choque != 0) {
                        Text(
                            "Choque de ${r.choque} em DX e IQ só no próximo turno (não afeta defesas).",
                            style = UiEstilos.detalheDoItem
                        )
                    }
                    r.testeDeNocaute?.let { t ->
                        val sinal = if (t.modificador == 0) "" else " (${t.modificador})"
                        Text(
                            "⚠️ Teste de HT$sinal para não ficar atordoado nem cair — ${t.motivo}.",
                            style = UiEstilos.detalheDoItem,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    r.avisos.forEach {
                        Text(it, style = UiEstilos.detalheDoItem, color = MaterialTheme.colorScheme.outline)
                    }
                    FerimentoPorLocalRules.situacao(pvNovo, pvInicial).forEach {
                        Text(it, style = UiEstilos.detalheDoItem, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            AppFileiraDeBotoes {
                AppBotaoSecundario("Cancelar", onFechar)
                AppBotaoPrincipal(
                    texto = "Aplicar",
                    onClick = {
                        val r = resultado
                        onSalvar(
                            pvNovo,
                            naMochila.toSet(),
                            if (r == null) "" else "${rotuloDoLocal(local)}: ${r.conta}"
                        )
                    },
                    enabled = resultado != null
                )
            }
        }
    )
}

@Composable
private fun PainelDaArmadura(
    local: LocalAtaque,
    noLocal: List<Equipamento>,
    naMochila: List<String>,
    usarRd: Boolean,
    rd: Int,
    onUsarRd: () -> Unit,
    onVestir: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .linhaAlternavel(
                marcado = usarRd,
                descricao = "Descontar a resistência a dano da armadura. RD atual $rd.",
                onAlternar = onUsarRd
            )
            .padding(
                horizontal = UiTokens.LinhaDeListaPaddingH,
                vertical = UiTokens.LinhaDeListaPaddingV
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = usarRd, onCheckedChange = null)
        Text(
            "Descontar RD da armadura — RD $rd em ${rotuloDoLocal(local)}",
            style = UiEstilos.nomeDoItem,
            modifier = Modifier.padding(start = 2.dp)
        )
    }

    if (noLocal.isEmpty()) {
        Text(
            "Nenhuma peça da ficha cobre ${rotuloDoLocal(local)}.",
            style = UiEstilos.detalheDoItem,
            color = MaterialTheme.colorScheme.outline
        )
        return
    }

    // ⚠️ Comprar não é vestir. A caixinha fica salva na ficha.
    noLocal.forEach { eq ->
        val marcada = eq.nome !in naMochila
        val rdPeca = CoberturaDaArmadura.rdDe(eq.rdArmaduraExibicao())
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .linhaAlternavel(
                    marcado = marcada,
                    descricao = "${eq.nome}, RD ${rdPeca?.principal ?: "desconhecida"}. " +
                        if (marcada) "Vestindo." else "Guardada.",
                    onAlternar = { onVestir(eq.nome) }
                )
                .padding(
                    horizontal = UiTokens.LinhaDeListaPaddingH,
                    vertical = UiTokens.LinhaDeListaPaddingV
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = marcada, onCheckedChange = null)
            Column(modifier = Modifier.weight(1f).padding(start = 2.dp)) {
                Text(eq.nome, style = UiEstilos.nomeDoItem)
                Text(
                    buildString {
                        append("RD ${rdPeca?.raw ?: "?"}")
                        if (rdPeca?.flexivel == true) append(" · flexível: não impede trauma por impacto")
                        if (rdPeca?.dividida == true) append(" · atrás é ${rdPeca.secundaria}")
                        if (rdPeca?.frontalSomente == true) append(" · só na frente")
                    },
                    style = UiEstilos.detalheDoItem,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

/**
 * A ordem da tela é a do corpo, não a da penalidade — quem apanhou já sabe onde
 * doeu e não quer procurar numa lista ordenada por dificuldade de acerto.
 */
private val LOCAIS_NA_TELA = listOf(
    LocalAtaque.TORSO, LocalAtaque.VITAIS, LocalAtaque.INGLE,
    LocalAtaque.BRACO, LocalAtaque.MAO, LocalAtaque.PERNA, LocalAtaque.PE,
    LocalAtaque.PESCOCO, LocalAtaque.ROSTO, LocalAtaque.CRANIO, LocalAtaque.OLHO
)

private fun rotuloDoLocal(l: LocalAtaque): String = when (l) {
    LocalAtaque.TORSO -> "Torso"
    LocalAtaque.VITAIS -> "Vitais"
    LocalAtaque.INGLE -> "Virilha"
    LocalAtaque.BRACO -> "Braço"
    LocalAtaque.MAO -> "Mão"
    LocalAtaque.PERNA -> "Perna"
    LocalAtaque.PE -> "Pé"
    LocalAtaque.PESCOCO -> "Pescoço"
    LocalAtaque.ROSTO -> "Rosto"
    LocalAtaque.CRANIO -> "Crânio"
    LocalAtaque.OLHO -> "Olho"
}

/**
 * ⚠️ O nome do enum nunca chega ao jogador — `PI_MAIS_MAIS` não quer dizer nada
 * na mesa. O rótulo carrega o multiplicador, que é a informação que decide.
 */
private fun rotuloDoTipo(t: DanoTipo): String = when (t) {
    DanoTipo.CONT -> "Contusão ×1"
    DanoTipo.CORT -> "Corte ×1,5"
    DanoTipo.PERF -> "Perfuração ×2"
    DanoTipo.PI_MENOS -> "Pouco perf. ×0,5"
    DanoTipo.PI -> "Perfurante ×1"
    DanoTipo.PI_MAIS -> "Muito perf. ×1,5"
    DanoTipo.PI_MAIS_MAIS -> "Extrem. perf. ×2"
}
