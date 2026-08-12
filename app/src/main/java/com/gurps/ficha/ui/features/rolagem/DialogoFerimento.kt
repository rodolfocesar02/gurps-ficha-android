package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.CoberturaDaArmadura
import com.gurps.ficha.domain.rules.DanoTipo
import com.gurps.ficha.domain.rules.FerimentoPorLocalRules
import com.gurps.ficha.domain.rules.LocalAtaque
import com.gurps.ficha.domain.rules.MapaDaSilhueta
import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.model.TipoEquipamento
import com.gurps.ficha.ui.AppBotaoPrincipal
import com.gurps.ficha.ui.AppBotaoSecundario
import com.gurps.ficha.ui.AppFileiraDeBotoes
import com.gurps.ficha.ui.AppFiltroChip
import com.gurps.ficha.ui.FullscreenDialogContainer
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
@Composable
fun DialogoFerimento(
    pvInicial: Int,
    pvAtual: Int,
    equipamentos: List<Equipamento>,
    guardadas: Set<String>,
    isPraCegoVariant: Boolean,
    onSalvar: (pvNovo: Int, guardadas: Set<String>, resumo: String) -> Unit,
    onFechar: () -> Unit
) {
    // Lote PV-1b: a silhueta guarda a REGIÃO (que tem lado); a variante pracego
    // guarda só o local. As duas alimentam o mesmo `local` da conta.
    // ⚠️ Abre sem nada escolhido, de propósito. Começando com o TRONCO marcado, a
    // primeira coisa que o jogador via era o vazio dos órgãos vitais no meio do
    // realce — que está certo (vitais é outro local, com regra própria) mas lê
    // como defeito. Escolhendo ele mesmo, o buraco vira informação.
    var regiao by remember { mutableStateOf<MapaDaSilhueta.Regiao?>(null) }
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
    // ⚠️ Sem parte escolhida não há local: assumir o torso em silêncio fazia o
    // painel anunciar "RD 2 em Torso" logo abaixo de "Nenhuma parte escolhida".
    // ⚠️ Depois do ACESS-2 as duas variantes escolhem uma REGIÃO — some a
    // exceção que a pracego tinha, e com ela a chance de as duas divergirem.
    val escolheu = regiao != null
    val noLocal = if (!escolheu) emptyList() else {
        armaduras.filter { CoberturaDaArmadura.cobre(it.armaduraLocal, local) }
    }
    val pecasVestidas = noLocal
        .filter { it.nome !in naMochila }
        .mapNotNull { eq ->
            CoberturaDaArmadura.rdDe(eq.rdArmaduraExibicao())?.let { CoberturaDaArmadura.Peca(eq.nome, it) }
        }
    val rd = if (usarRd) CoberturaDaArmadura.rdTotal(pecasVestidas) else 0
    // Lote EQP-9: quanto dessa RD veio de peça FLEXÍVEL (as com `*`). É o que
    // decide o trauma por impacto — ver `TraumaPorImpacto`.
    val rdFlexivel = if (!usarRd) 0 else pecasVestidas.filter { it.rd.flexivel }.sumOf { it.rd.principal }

    val dano = danoTexto.toIntOrNull() ?: 0
    val resultado = remember(dano, tipo, local, rd, rdFlexivel, pvInicial, masculino, escolheu) {
        if (dano <= 0 || !escolheu) null else FerimentoPorLocalRules.aplicar(
            pvInicial = pvInicial,
            danoBruto = dano,
            tipo = tipo,
            local = local,
            rdArmadura = rd,
            masculino = masculino,
            rdFlexivel = rdFlexivel
        )
    }
    val pvNovo = pvAtual - (resultado?.pvPerdidos ?: 0)

    // A silhueta é o conteúdo principal, então ela ganha uma fatia proporcional
    // da tela — num celular pequeno encolhe junto, em vez de empurrar o resto
    // para fora.
    val alturaDaTela = LocalConfiguration.current.screenHeightDp
    val alturaDaSilhueta = (alturaDaTela * 0.42f).dp.coerceIn(260.dp, 520.dp)

    FullscreenDialogContainer(onDismiss = onFechar) {
        Text(
            "Ferimento",
            style = UiEstilos.tituloDialogo,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text("PV atual: $pvAtual de $pvInicial", style = UiEstilos.subtituloDialogo)

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(UiTokens.ItemSpacing)
        ) {
                // A ordem é a da mesa: o Mestre canta o número primeiro. Antes o
                // campo ficava depois da silhueta e do tipo, e o jogador digitava
                // por último — com o resultado já fora da tela.
                OutlinedTextField(
                    value = danoTexto,
                    onValueChange = { novo -> danoTexto = novo.filter { it.isDigit() }.take(4) },
                    label = { Text("Dano rolado (antes da RD)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // ⚠️ Uma silhueta não se tateia. Na variante pracego continua a
                // lista de quadradinhos — ela não foi substituída, coexiste.
                if (isPraCegoVariant) {
                    // 🔴 Lote ACESS-2: a lista velha tinha 11 locais SEM LADO, e a
                    // silhueta tem 16 COM lado. Quem não enxerga não conseguia
                    // registrar qual braço foi decepado — as duas variantes
                    // gravavam coisas diferentes na mesma ficha.
                    //
                    // Agora as duas leem a MESMA fonte (`MapaDaSilhueta.REGIOES`)
                    // e têm a mesma estrutura de dois níveis. Só a entrada muda.
                    ListaDeLocaisPraCego(
                        selecionada = regiao,
                        onSelecionar = {
                            regiao = it
                            local = it.local
                        }
                    )
                } else {
                    SilhuetaDoCorpo(
                        selecionada = regiao,
                        onSelecionar = {
                            regiao = it
                            local = it.local
                        },
                        altura = alturaDaSilhueta
                    )
                    Text(
                        regiao?.nomeCompleto ?: "Nenhuma parte escolhida",
                        style = UiEstilos.nomeDoItem,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text("Tipo de dano", style = UiEstilos.subtituloDialogo, fontWeight = FontWeight.SemiBold)
                GradeDeEscolhas(DanoTipo.entries, rotulo = { rotuloDoTipo(it) }, escolhido = { it == tipo }) {
                    tipo = it
                }

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
                    escolheu = escolheu,
                    noLocal = noLocal,
                    naMochila = naMochila,
                    usarRd = usarRd,
                    rd = rd,
                    onUsarRd = { usarRd = !usarRd },
                    onVestir = { nome -> if (nome in naMochila) naMochila.remove(nome) else naMochila.add(nome) }
                )

                // Os detalhes ficam aqui embaixo; o NÚMERO fica fixo no rodapé.
                resultado?.let { r ->
                    HorizontalDivider(modifier = Modifier.padding(vertical = UiTokens.ItemSpacing))
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

        // 🔴 O resultado fica FIXO acima dos botões.
        //
        // Antes ele nascia no fim da rolagem, depois do painel de armadura: o
        // jogador digitava o dano e o número aparecia fora da tela. Aqui ele está
        // sempre visível e muda ao vivo — trocar o tipo de dano ou desmarcar a
        // armadura mexe no número na frente dos olhos.
        resultado?.let { r ->
            HorizontalDivider(modifier = Modifier.padding(top = UiTokens.ItemSpacing))
            // ⚠️ O visível mantém os sinais ("−12 PV"); o falado soletra, porque
            // o leitor de tela pula o hífen e um redutor vira bônus.
            Text(
                "−${r.pvPerdidos} PV   →   $pvNovo de $pvInicial",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.semantics {
                    contentDescription = r.descricaoAcessivel(pvNovo, pvInicial)
                }
            )
            // A conta escrita: o jogador precisa poder conferir de onde saiu o
            // número, senão o app vira caixa-preta na mesa.
            Text(r.conta, style = UiEstilos.detalheDoItem, color = MaterialTheme.colorScheme.outline)
        }

        AppFileiraDeBotoes {
            AppBotaoSecundario("Cancelar", onFechar)
            AppBotaoPrincipal(
                texto = "Aplicar",
                onClick = {
                    val r = resultado
                    onSalvar(
                        pvNovo,
                        naMochila.toSet(),
                        if (r == null) "" else "${regiao?.nomeCompleto ?: rotuloDoLocal(local)}: ${r.conta}"
                    )
                },
                // Na silhueta não basta ter dano digitado: sem parte escolhida
                // não há o que aplicar — e o padrão silencioso seria o tronco.
                enabled = resultado != null && regiao != null
            )
        }
    }
}

/**
 * Uma grade de escolhas em **duas colunas de largura igual**.
 *
 * ⚠️ Substitui o `FlowRow` de chips. Com o fluxo livre cada botão encolhia até o
 * tamanho do próprio texto: *"Pé"* ficava com um terço da largura de
 * *"Extrem. perf. ×2"*, as fileiras quebravam em lugares diferentes e a tela
 * virava um mosaico torto. Aqui todo botão tem a mesma largura, a mesma altura e
 * o mesmo tamanho de letra.
 *
 * Quando a lista é ímpar, o último ganha um vazio do lado — assim ele mantém a
 * largura dos outros em vez de esticar para a fileira inteira.
 */
@Composable
private fun <T> GradeDeEscolhas(
    itens: List<T>,
    rotulo: (T) -> String,
    escolhido: (T) -> Boolean,
    onEscolher: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(UiTokens.ItemSpacing)) {
        itens.chunked(2).forEach { par ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UiTokens.ItemSpacing)
            ) {
                par.forEach { item ->
                    AppFiltroChip(
                        rotulo = rotulo(item),
                        selecionado = escolhido(item),
                        modifier = Modifier.weight(1f)
                    ) { onEscolher(item) }
                }
                if (par.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PainelDaArmadura(
    local: LocalAtaque,
    escolheu: Boolean,
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
            if (escolheu) "Descontar RD da armadura — RD $rd em ${rotuloDoLocal(local)}"
            else "Descontar RD da armadura",
            style = UiEstilos.nomeDoItem,
            modifier = Modifier.padding(start = 2.dp)
        )
    }

    if (noLocal.isEmpty()) {
        Text(
            if (!escolheu) {
                "Escolha uma parte do corpo para ver a RD que protege ela."
            } else {
                "Nenhuma peça da ficha cobre ${rotuloDoLocal(local)}."
            },
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
                    // ⚠️ NÃO diz "vestindo/guardada": o `linhaAlternavel` já
                    // anuncia marcada/não marcada, e escrever de novo faz o
                    // TalkBack repetir — com risco de uma das duas desatualizar.
                    descricao = "${eq.nome}. Resistência a dano " +
                        "${rdPeca?.principal ?: "desconhecida"}.",
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
                        if (rdPeca?.flexivel == true) append(" · flexível: deixa passar trauma por impacto")
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
// ⚠️ A lista fixa de locais foi embora no ACESS-2: as duas variantes agora leem
// `MapaDaSilhueta.REGIOES`. Manter uma segunda lista aqui era o que permitia a
// pracego ficar com 11 locais sem lado enquanto a silhueta tinha 16 com lado.
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
