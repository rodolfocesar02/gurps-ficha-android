package com.gurps.ficha.ui.features.traits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.gurps.ficha.domain.rules.poderes.RegrasDePoder
import com.gurps.ficha.model.FonteDoPoder
import com.gurps.ficha.model.Poder
import com.gurps.ficha.model.PoderDefinicao
import com.gurps.ficha.ui.AppBotaoIcone
import com.gurps.ficha.ui.AppBotaoSecundario
import com.gurps.ficha.ui.AppCampoCompacto
import com.gurps.ficha.ui.AppSelectionRow
import com.gurps.ficha.ui.FullscreenDialogContainer
import com.gurps.ficha.ui.UiEstilos
import com.gurps.ficha.viewmodel.FichaViewModel

/**
 * **O teto de altura de todo diálogo de poder** — Lote POD-20.
 *
 * 🔴 Sem ele o diálogo cresce junto com o conteúdo e o rodapé de botões sai da
 * tela. Quem tinha um poder com habilidades, reserva e sugestões do livro não
 * conseguia **salvar nem cancelar** — os dois botões ficavam abaixo da borda.
 *
 * ⚠️ Um número só, para os três diálogos: dois valores diferentes seriam duas
 * rotas para a mesma decisão, e a que ninguém olha é a que quebra.
 */
internal val ALTURA_MAXIMA_DO_DIALOGO = 600.dp

/**
 * **A folga que o rótulo flutuante precisa** — Lote POD-27.
 *
 * O rótulo do `AppCampoCompacto` (*"Nome do Poder"*) é desenhado **sobre a
 * borda** da caixa, metade dele acima do topo. Fora de um recorte isso é
 * invisível; dentro de um `verticalScroll`, que recorta no limite do miolo, o
 * rótulo do primeiro campo aparece cortado ao meio.
 *
 * ⚠️ Achado pelo usuário no aparelho, e o defeito era **meu, do lote anterior**:
 * o campo estava certo desde o GER-2; foi a rolagem do POD-20 que trouxe o
 * recorte junto.
 */
internal val FOLGA_DO_ROTULO_FLUTUANTE = 8.dp

/**
 * **Configurar Poderes** — GURPS Poderes. Lotes POD-1 a POD-3.
 *
 * ## 🔴 O que mudou, e por quê
 *
 * O modificador de poder **deixou de ser um número que o jogador digita**. O
 * livro diz, em cada verbete, quais fontes aquele poder aceita e quanto cada uma
 * vale: *"Este modificador normalmente é Divino (-10%), Elemental (-10%),
 * Espiritual (-25%), Mágico (-10%), ou Super (-10%)"* (Água, p.121). Agora se
 * **escolhe a fonte** e o percentual vem do livro.
 *
 * O campo de digitar continua existindo, porque o Mestre pode montar um
 * modificador próprio somando componentes (p.20-25) — mas ele deixou de ser o
 * único caminho, que era o que fazia todo poder nascer com 0%.
 *
 * E o Talento **ganhou campo**: ele existia no modelo, era salvo e nunca era
 * desenhado, então nunca saía de zero.
 */
@Composable
fun DialogsPoderes(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit
) {
    val personagem = viewModel.personagem
    var showEditDialog by remember { mutableStateOf<Poder?>(null) }
    var showSelecionarDialog by remember { mutableStateOf(false) }
    var showNovoDialog by remember { mutableStateOf(false) }
    var novoPoderPreenchido by remember { mutableStateOf<Poder?>(null) }
    var definicaoEmUso by remember { mutableStateOf<PoderDefinicao?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().heightIn(max = ALTURA_MAXIMA_DO_DIALOGO)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Poderes do Personagem",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (personagem.pontosPoderes != 0) {
                    // O total dos Talentos, que passou a custar pontos no POD-3.
                    Text(
                        "Talentos: ${personagem.pontosPoderes} pontos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Lote POD-20: mesma correção do diálogo de edição — com
                // fill=false a lista podia pedir mais altura do que sobrava e
                // desenhar por cima do rodapé.
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(personagem.poderes) { index, poder ->
                        AppSelectionRow(
                            nome = poder.nome,
                            // Lote POD-5: a linha passa a dizer QUANTAS habilidades
                            // o poder reúne. Antes só mostrava fonte e percentual —
                            // e um poder sem habilidade nenhuma parecia igual a um
                            // poder inteiro.
                            detalhe = detalheDoPoder(poder) +
                                " · " + resumoCurtoDoPoder(viewModel, poder),
                            onClick = { showEditDialog = poder },
                            descricaoAcessivel = poder.descricaoAcessivel + ". " +
                                resumoCurtoDoPoder(viewModel, poder) + ". Toque para editar.",
                            acoes = {
                                AppBotaoIcone(
                                    icone = Icons.Default.Edit,
                                    descricao = "Editar o poder ${poder.nome}",
                                    onClick = { showEditDialog = poder }
                                )
                                AppBotaoIcone(
                                    icone = Icons.Default.Delete,
                                    descricao = "Apagar o poder ${poder.nome}",
                                    onClick = { viewModel.removerPoder(index) }
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showSelecionarDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Novo Poder")
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Fechar")
                }
            }
        }
    }

    if (showSelecionarDialog) {
        SelecionarPoderDialog(
            viewModel = viewModel,
            onDismiss = { showSelecionarDialog = false },
            onSelect = { definicao ->
                showSelecionarDialog = false
                definicaoEmUso = definicao
                novoPoderPreenchido = definicao?.let { d ->
                    // ⚠️ Já nasce com a PRIMEIRA fonte do livro e o percentual
                    // dela. Antes nascia com 0%, e o jogador tinha de saber o
                    // número de cabeça para o poder valer alguma coisa.
                    val padrao = d.fontePadrao
                    Poder(
                        nome = d.nome,
                        fonte = padrao?.fonte.orEmpty(),
                        foco = d.foco,
                        modificadorDePoder = padrao?.valor ?: 0,
                        custoTalentoNivel = if (d.semTalento) 0 else d.custoTalentoPorNivel,
                        nomeDoModificador = d.nomeDoModificador
                    )
                }
                showNovoDialog = true
            }
        )
    }

    if (showNovoDialog) {
        PoderEditDialog(
            poderBase = novoPoderPreenchido,
            definicao = definicaoEmUso,
            fontesGerais = viewModel.dataRepository.fontesDePoder.map {
                FonteDoPoder(it.nome, it.valor)
            },
            isNew = true,
            onDismiss = { showNovoDialog = false },
            onSave = { novoPoder ->
                viewModel.adicionarPoder(novoPoder)
                showNovoDialog = false
            }
        )
    }

    showEditDialog?.let { poderEdit ->
        val index = personagem.poderes.indexOfFirst { it.id == poderEdit.id }
        PoderEditDialog(
            poderBase = poderEdit,
            definicao = viewModel.dataRepository.poderes
                .firstOrNull { it.nome.equals(poderEdit.nome, ignoreCase = true) },
            fontesGerais = viewModel.dataRepository.fontesDePoder.map {
                FonteDoPoder(it.nome, it.valor)
            },
            isNew = false,
            viewModelParaHabilidades = viewModel,
            onDismiss = { showEditDialog = null },
            onSave = { modPoder ->
                if (index >= 0) viewModel.atualizarPoder(index, modPoder)
                showEditDialog = null
            }
        )
    }
}

private fun detalheDoPoder(poder: Poder): String = buildString {
    if (poder.fonte.isNotBlank()) append(poder.fonte) else append("sem fonte")
    append(" — ")
    append(if (poder.modificadorDePoder > 0) "+" else "")
    append("${poder.modificadorDePoder}%")
    if (poder.nivelTalento > 0) {
        append(" · Talento ${poder.nivelTalento} (${poder.custoTotalTalento} pts)")
    }
}

@Composable
fun PoderEditDialog(
    poderBase: Poder?,
    definicao: PoderDefinicao? = null,
    fontesGerais: List<FonteDoPoder> = emptyList(),
    isNew: Boolean = false,
    // Lote POD-5: só o poder JÁ SALVO tem habilidades para mostrar. Um poder
    // que está nascendo não pode ligar nada — ele ainda não existe na ficha.
    viewModelParaHabilidades: FichaViewModel? = null,
    onDismiss: () -> Unit,
    onSave: (Poder) -> Unit
) {
    var mostrarLigar by remember(poderBase) { mutableStateOf(false) }
    // Lote POD-7: o montador de modificador por componentes.
    var mostrarMontador by remember(poderBase) { mutableStateOf(false) }
    // Lote POD-14: comprar a habilidade de dentro do poder.
    var mostrarComprar by remember(poderBase) { mutableStateOf(false) }
    // Lote POD-9: a Reserva de Energia edita o proprio poder, entao ela
    // precisa de um rascunho local ate o Salvar.
    var rascunho by remember(poderBase) { mutableStateOf(poderBase ?: Poder()) }
    var nome by remember(poderBase) { mutableStateOf(poderBase?.nome ?: "") }
    var fonte by remember(poderBase) { mutableStateOf(poderBase?.fonte ?: "") }
    var foco by remember(poderBase) { mutableStateOf(poderBase?.foco ?: "") }
    var modificador by remember(poderBase) {
        mutableStateOf(poderBase?.modificadorDePoder?.toString() ?: "0")
    }
    var nivelTalento by remember(poderBase) {
        mutableStateOf(poderBase?.nivelTalento?.toString() ?: "0")
    }
    val custoNivel = poderBase?.custoTalentoNivel
        ?: definicao?.custoTalentoPorNivel
        ?: RegrasDePoder.CUSTO_PADRAO_POR_NIVEL

    // As fontes que ESTE poder aceita. Quando o poder é personalizado (sem
    // verbete no livro), valem as onze genéricas.
    val fontesDoPoder = definicao?.modificadores?.takeIf { it.isNotEmpty() } ?: fontesGerais
    val nivel = nivelTalento.toIntOrNull() ?: 0

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            // 🔴 Lote POD-20: sem teto de altura o diálogo crescia com o
            // conteúdo (habilidades + reserva + sugestões do livro) e o rodapé
            // com Salvar/Cancelar saía para fora da tela. O poder ficava
            // impossível de salvar — achado pelo usuário no aparelho.
            modifier = Modifier.fillMaxWidth().heightIn(max = ALTURA_MAXIMA_DO_DIALOGO)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    if (isNew) "Novo Poder" else "Editar Poder",
                    style = MaterialTheme.typography.titleLarge
                )
                if (definicao != null && definicao.pagina > 0) {
                    Text(
                        "GURPS Poderes, p.${definicao.pagina}" +
                            // Lote POD-15: o Módulo Básico também descreve este poder.
                            if (definicao.paginaModuloBasico > 0)
                                " · Módulo Básico, p.${definicao.paginaModuloBasico}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // ── O miolo rola; o rodapé fica parado (Lote POD-20) ──────
                // ⚠️ `weight(1f)` com fill=true: o miolo ocupa exatamente o que
                // sobrou depois do título e do rodapé. Com fill=false ele podia
                // pedir mais do que sobrava e o Salvar ficava por baixo.
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        // 🔴 Lote POD-27: o rótulo flutuante do `AppCampoCompacto`
                        // desenha **meio corpo acima** da borda da caixa — é assim
                        // que o Material o faz. O `verticalScroll` recorta no
                        // limite do miolo, e o "Nome do Poder" ficava cortado ao
                        // meio. O defeito nasceu no POD-20, junto com a rolagem.
                        .padding(top = FOLGA_DO_ROTULO_FLUTUANTE)
                ) {
                AppCampoCompacto(
                    value = nome,
                    onValueChange = { nome = it },
                    label = "Nome do Poder",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 🔴 Lote POD-24: o Antipsi **não tem** modificador nenhum
                // ("Modificador de Poder: Nenhum, já que as habilidades Antipsi
                // não podem ser bloqueadas!" — MB p.256), e mesmo assim a tela
                // oferecia três fontes. Achado pelo usuário no aparelho.
                if (definicao?.semModificador == true) {
                    Text(
                        "Este poder não tem modificador de poder. " +
                            "As habilidades dele custam o preço cheio, porque não " +
                            "podem ser bloqueadas (Módulo Básico, p.${definicao.paginaModuloBasico}).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    EscolhaDaFonte(
                        fonteAtual = fonte,
                        fontes = fontesDoPoder,
                        onEscolher = { escolhida ->
                            fonte = escolhida.fonte
                            // 🔴 O percentual acompanha a fonte. Era este o elo que
                            // não existia: o app guardava os dois lados soltos.
                            modificador = escolhida.valor.toString()
                        }
                    )
                    // 🔴 Lote POD-24: o nome do modificador na hora de ESCOLHER.
                    // Ele existia desde o POD-15, mas só aparecia depois, ao ligar
                    // uma habilidade — e o usuário concluiu, com razão, que os
                    // poderes psíquicos do Módulo Básico não tinham sido feitos.
                    definicao?.takeIf { it.modificadorProprio.isNotBlank() }?.let { d ->
                        Text(
                            "Na ficha as habilidades levam “${d.modificadorProprio}, " +
                                "${modificador.toIntOrNull() ?: 0}%” — é o nome que o " +
                                "Módulo Básico dá a este modificador (p.${d.paginaModuloBasico}).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                AppCampoCompacto(
                    value = foco,
                    onValueChange = { foco = it },
                    label = "Foco (ex: Fogo, Mentes)",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Lote POD-24: o Antipsi não tem modificador **nem** Talento —
                // são duas ausências distintas, e por isso dois campos separados
                // no catálogo (`sem_modificador`, `sem_talento`).
                val temModificador = definicao?.semModificador != true
                val temTalento = definicao?.semTalento != true
                if (temModificador || temTalento) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (temModificador) {
                            AppCampoCompacto(
                                value = modificador,
                                onValueChange = {
                                    modificador = it.filter { c -> c.isDigit() || c == '-' }
                                },
                                label = "Modificador (%)",
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        if (temTalento) {
                            AppCampoCompacto(
                                value = nivelTalento,
                                onValueChange = { nivelTalento = it.filter(Char::isDigit).take(2) },
                                label = "Talento (nível)",
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                }

                // Lote POD-26: o montador (p.20-26) serve para montar um
                // modificador **próprio**. Nos 47 verbetes do livro o valor já
                // vem pronto com a fonte, e o botão só atrapalha quem está
                // seguindo o catálogo.
                if (definicao == null) {
                    AppBotaoSecundario(
                        "Montar o modificador por componentes",
                        { mostrarMontador = true },
                        larguraTotal = true
                    )
                }

                // O custo do Talento, à vista. Ele passou a entrar no total da
                // ficha no POD-3 — antes não custava nada.
                if (temTalento) Text(
                    "Talento: $custoNivel pontos/nível · " +
                        "${RegrasDePoder.custoDoTalento(nivel, custoNivel)} pontos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                RegrasDePoder.avisoDoTeto(nivel)?.let { aviso ->
                    Text(
                        aviso,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Lote POD-5: o poder mostra o que ele reúne.
                if (!isNew && viewModelParaHabilidades != null && poderBase != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    PainelDeHabilidades(
                        viewModel = viewModelParaHabilidades,
                        poder = poderBase,
                        onPedirParaLigar = { mostrarLigar = true },
                        onPedirParaComprar = { mostrarComprar = true }
                    )
                    // Lote POD-9: a Reserva de Energia deste poder.
                    PainelDaReserva(rascunho) { rascunho = it }
                    // Lote POD-10: o que o livro sugere para este poder.
                    SugestoesDoLivro(definicao)
                }
                } // ── fim do miolo rolável ──

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val base = rascunho
                        onSave(
                            base.copy(
                                nome = nome,
                                fonte = fonte,
                                foco = foco,
                                modificadorDePoder = modificador.toIntOrNull() ?: 0,
                                nivelTalento = nivel,
                                custoTalentoNivel = custoNivel
                            )
                        )
                    }) {
                        Text("Salvar")
                    }
                }
            }
        }
    }

    if (mostrarComprar && viewModelParaHabilidades != null && poderBase != null) {
        SelecionarVantagemDialog(
            viewModel = viewModelParaHabilidades,
            onDismiss = { mostrarComprar = false },
            onSelect = { v ->
                viewModelParaHabilidades.adicionarHabilidadeAoPoder(v, poderBase)
                mostrarComprar = false
            }
        )
    }

    if (mostrarMontador) {
        MontadorDeModificadorDialog(
            valorAtual = modificador.toIntOrNull() ?: 0,
            onDismiss = { mostrarMontador = false },
            onAplicar = { total ->
                // ⚠️ O montador ESCREVE no campo de modificador, e nao num
                // campo proprio: e o mesmo numero, e dois lugares divergiriam.
                modificador = total.toString()
                mostrarMontador = false
            }
        )
    }

    if (mostrarLigar && viewModelParaHabilidades != null && poderBase != null) {
        LigarHabilidadeDialog(
            viewModel = viewModelParaHabilidades,
            poder = poderBase,
            onDismiss = { mostrarLigar = false }
        )
    }
}

/**
 * A escolha da fonte. É aqui que o percentual do livro entra na ficha.
 *
 * ⚠️ Mostra o valor ao lado do nome porque o número é a razão da escolha —
 * Espiritual custa -25% e Cósmico dá +50%, e isso muda o preço de toda
 * habilidade do poder.
 */
@Composable
private fun EscolhaDaFonte(
    fonteAtual: String,
    fontes: List<FonteDoPoder>,
    onEscolher: (FonteDoPoder) -> Unit
) {
    var aberto by remember { mutableStateOf(false) }
    val rotulo = if (fonteAtual.isBlank()) "Escolher a fonte" else fonteAtual

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { aberto = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(rotulo, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Escolher a fonte do poder")
        }
        DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
            fontes.forEach { f ->
                val sinal = if (f.valor > 0) "+" else ""
                DropdownMenuItem(
                    text = { Text("${f.fonte} ($sinal${f.valor}%)") },
                    onClick = {
                        onEscolher(f)
                        aberto = false
                    }
                )
            }
        }
    }
}

@Composable
fun SelecionarPoderDialog(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit,
    onSelect: (PoderDefinicao?) -> Unit
) {
    var busca by remember { mutableStateOf("") }
    val listaPoderes = viewModel.dataRepository.poderes.filter {
        busca.isBlank() ||
            it.nome.contains(busca, ignoreCase = true) ||
            it.foco.contains(busca, ignoreCase = true)
    }

    FullscreenDialogContainer(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                "Selecionar Poder",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            AppCampoCompacto(
                value = busca,
                onValueChange = { busca = it },
                label = "Buscar por nome ou foco...",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { onSelect(null) }, modifier = Modifier.fillMaxWidth()) {
                Text("Criar Poder Personalizado")
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "${listaPoderes.size} poderes (GURPS Poderes, p.121-136)",
                style = MaterialTheme.typography.bodySmall
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(listaPoderes) { definicao ->
                    AppSelectionRow(
                        nome = definicao.nome,
                        detalhe = "Foco: ${definicao.foco}",
                        onClick = { onSelect(definicao) },
                        descricaoAcessivel = descricaoAcessivelDaDefinicao(definicao),
                        extra = {
                            // As fontes válidas COM o percentual: é o que o
                            // jogador precisa para decidir, e o que o catálogo
                            // antigo não tinha (todo poder vinha com 0%).
                            Text(
                                definicao.modificadores.joinToString(" · ") { f ->
                                    "${f.fonte} ${if (f.valor > 0) "+" else ""}${f.valor}%"
                                },
                                style = UiEstilos.detalheDoItem,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (definicao.habilidades.isNotEmpty()) {
                                Text(
                                    "${definicao.habilidades.size} habilidades sugeridas",
                                    style = UiEstilos.detalheDoItem,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (definicao.descricao.isNotBlank()) {
                                Text(definicao.descricao, style = UiEstilos.detalheDoItem)
                            }
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) { Text("Fechar") }
            }
        }
    }
}

private fun descricaoAcessivelDaDefinicao(d: PoderDefinicao): String = buildString {
    append(d.nome)
    append(". Foco: ").append(d.foco)
    if (d.modificadores.isNotEmpty()) {
        append(". Fontes: ")
        append(d.modificadores.joinToString(", ") { f ->
            val v = if (f.valor < 0) "menos ${-f.valor}" else "${f.valor}"
            "${f.fonte}, $v por cento"
        })
    }
    append(". ").append(d.descricao)
}
