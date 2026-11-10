package com.gurps.ficha.ui.features.equipamento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.gurps.ficha.domain.rules.TextoDoCatalogo
import com.gurps.ficha.model.ArmaduraCatalogoItem
import com.gurps.ficha.model.EscudoCatalogoItem
import com.gurps.ficha.ui.AppBotaoDiscreto
import com.gurps.ficha.ui.AppFileiraDeBotoes
import com.gurps.ficha.ui.AppFiltroChip
import com.gurps.ficha.ui.AppSelectionDialog
import com.gurps.ficha.ui.AppSelectionRow
import com.gurps.ficha.ui.LOCAIS_ARMADURA
import com.gurps.ficha.ui.UiEstilos
import com.gurps.ficha.ui.contadorDe
import com.gurps.ficha.viewmodel.FichaViewModel

/**
 * **Escolher o escudo e a armadura do catálogo** — os dois diálogos de seleção que moravam no `TabEquipamentos.kt`.
 *
 * Saíram de lá no IC-2 do `mesa-virtual/planos/itens/PLANO.md` (10/nov/2026), sem mudar uma linha do que fazem: a aba tinha passado
 * do teto de 1.000 linhas, e o diálogo novo de "Adicionar Itens" precisava de espaço.
 */

private fun observacoesFormatadas(armadura: ArmaduraCatalogoItem): List<String> {
    val refs = Regex("\\[(\\d+)]")
        .findAll(armadura.observacoes)
        .mapNotNull { it.groupValues.getOrNull(1)?.toIntOrNull() }
        .toList()
    val detalhesOriginais = armadura.observacoesDetalhadas
        .map { it.trim() }
        .filter { it.isNotBlank() }
    if (detalhesOriginais.isEmpty()) return emptyList()

    val saida = mutableListOf<String>()
    var detalhes = detalhesOriginais

    // Para NT alto, a observacao global (NT7+) deve aparecer sem numero.
    val primeira = detalhes.firstOrNull()
    if (primeira != null && primeira.contains("NT7+", ignoreCase = true)) {
        saida.add(primeira)
        detalhes = detalhes.drop(1)
    }

    if (refs.isEmpty() || detalhes.isEmpty()) return saida

    val pareadas = refs.zip(detalhes).map { (ref, texto) -> "[$ref] $texto" }
    saida.addAll(pareadas)
    if (detalhes.size > refs.size) {
        detalhes.drop(refs.size).forEach { extra -> saida.add(extra) }
    }
    return saida
}

@Composable
internal fun SelecionarEscudoEquipamentoDialog(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit,
    onSelect: (EscudoCatalogoItem) -> Unit
) {
    val escudos = viewModel.escudosEquipamentosFiltrados
    val stAtual = viewModel.personagem.forca
    // Lote LAYOUT-6.
    AppSelectionDialog(
        titulo = "Selecionar Escudo",
        subtitulo = "ST do personagem: $stAtual",
        busca = viewModel.equipmentSearch.query,
        onBusca = { viewModel.atualizarBuscaEscudoEquipamento(it) },
        rotuloDaBusca = "Buscar escudo",
        contador = contadorDe(escudos.size, "escudo", "escudos"),
        onDismiss = onDismiss
    ) {
        items(escudos, key = { it.id }) { escudo ->
            AppSelectionRow(
                nome = escudo.nome,
                detalhe = "BD +${escudo.db} | Custo: $${escudo.custo ?: 0f} | Peso: ${escudo.pesoKg ?: 0f} kg",
                onClick = { onSelect(escudo) }
            )
        }
    }
}

@Composable
internal fun SelecionarArmaduraEquipamentoDialog(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit,
    onSelect: (ArmaduraCatalogoItem) -> Unit
) {
    val armaduras = viewModel.armadurasEquipamentosFiltradas
    val filtrosAtivos = viewModel.equipmentSearch.query.isNotBlank() ||
        viewModel.equipmentSearch.armorerLocation != null ||
        viewModel.equipmentSearch.armorerNt != null
    // Lote LAYOUT-6. O "Resultados: N" vira o contador padrão, e o "Limpar
    // filtros" entra no cabeçalho livre — ele é uma ação, não um filtro.
    AppSelectionDialog(
        titulo = "Selecionar Armadura",
        subtitulo = "Use Local e NT para refinar mais rápido.",
        busca = viewModel.equipmentSearch.query,
        onBusca = { viewModel.atualizarBuscaArmaduraEquipamento(it) },
        rotuloDaBusca = "Buscar armadura",
        contador = contadorDe(armaduras.size, "armadura", "armaduras"),
        filtros = {
            AppFiltroChip("Local: Todos", viewModel.equipmentSearch.armorerLocation == null) { viewModel.atualizarFiltroLocalArmaduraEquipamento(null) }
            LOCAIS_ARMADURA.forEach { (id, label) ->
                AppFiltroChip(label, viewModel.equipmentSearch.armorerLocation == id) { viewModel.atualizarFiltroLocalArmaduraEquipamento(id) }
            }
        },
        filtrosSecundarios = {
            AppFiltroChip("NT: Todas", viewModel.equipmentSearch.armorerNt == null) { viewModel.atualizarFiltroNtArmaduraEquipamento(null) }
            for (nt in 0..10) {
                AppFiltroChip("NT $nt", viewModel.equipmentSearch.armorerNt == nt) { viewModel.atualizarFiltroNtArmaduraEquipamento(nt) }
            }
        },
        cabecalhoExtra = if (!filtrosAtivos) null else ({
            AppFileiraDeBotoes(alinhamento = Arrangement.Start) {
                AppBotaoDiscreto(texto = "Limpar filtros", onClick = { viewModel.limparFiltrosArmaduraEquipamento() })
            }
        }),
        onDismiss = onDismiss
    ) {
        items(armaduras, key = { it.id }) { armadura ->
            val observacoes = observacoesFormatadas(armadura)
            AppSelectionRow(
                nome = TextoDoCatalogo.corrigir(armadura.nome),
                detalhe = "NT ${armadura.nt ?: "—"} | RD ${armadura.rd} | Peso ${armadura.pesoBaseKg ?: 0f} kg | Custo $${armadura.custoBase ?: 0f}",
                onClick = { onSelect(armadura) },
                descricaoAcessivel = "${TextoDoCatalogo.corrigir(armadura.nome)}. NT ${armadura.nt ?: "não cadastrado"}, " +
                    "RD ${armadura.rd}. Local: ${TextoDoCatalogo.corrigir(armadura.local)}.",
                extra = {
                    Text(
                        "Local: ${TextoDoCatalogo.corrigir(armadura.local)}",
                        style = UiEstilos.detalheDoItem,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    observacoes.forEach { linha ->
                        Text(
                            linha,
                            style = UiEstilos.detalheDoItem,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    }
}
