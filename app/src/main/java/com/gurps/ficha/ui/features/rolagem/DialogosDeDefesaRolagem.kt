package com.gurps.ficha.ui.features.rolagem

import androidx.compose.runtime.Composable
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.viewmodel.FichaViewModel

/**
 * Os três diálogos de configuração de defesa da aba Rolagem.
 *
 * Extraído da `TabRolagem` em 28/07: o Lote MARCOS-1 levou o arquivo a 1.046
 * linhas e o projeto tem teto de 1.000. Este bloco era o candidato natural —
 * são três `if` independentes que só amarram estado de visibilidade a diálogos
 * que já viviam em `RolagemSecondaryDialogs.kt`.
 *
 * Nada mudou de comportamento: mesmo código, mesma ordem, outro arquivo.
 */
@Composable
fun DialogosDeDefesaRolagem(
    viewModel: FichaViewModel,
    personagem: Personagem,
    mostrarEsquiva: Boolean,
    mostrarApara: Boolean,
    mostrarBloqueio: Boolean,
    onFecharEsquiva: () -> Unit,
    onFecharApara: () -> Unit,
    onFecharBloqueio: () -> Unit
) {
    if (mostrarEsquiva) {
        EditarEsquivaBonusDialog(
            personagem = personagem,
            bonusAtual = personagem.defesasAtivas.bonusManualEsquiva,
            notaAtual = personagem.defesasAtivas.notaBonusManualEsquiva,
            onDismiss = onFecharEsquiva,
            onConfirm = { bonus, nota ->
                viewModel.atualizarBonusManualEsquiva(bonus, nota)
                onFecharEsquiva()
            }
        )
    }

    if (mostrarApara) {
        EditarAparaDialog(
            personagem = personagem,
            pericias = viewModel.periciasParaApara,
            periciaSelecionadaId = personagem.defesasAtivas.periciaAparaId,
            bonusAtual = personagem.defesasAtivas.bonusManualApara,
            notaAtual = personagem.defesasAtivas.notaBonusManualApara,
            onDismiss = onFecharApara,
            onConfirm = { periciaId, bonus, nota ->
                viewModel.atualizarPericiaApara(periciaId)
                viewModel.atualizarBonusManualApara(bonus, nota)
                onFecharApara()
            }
        )
    }

    if (mostrarBloqueio) {
        EditarBloqueioDialog(
            personagem = personagem,
            pericias = viewModel.periciasParaBloqueio,
            escudos = viewModel.escudosEquipados,
            periciaSelecionadaId = personagem.defesasAtivas.periciaBloqueioId,
            escudoSelecionadoNome = personagem.defesasAtivas.escudoSelecionadoNome,
            bonusAtual = personagem.defesasAtivas.bonusManualBloqueio,
            notaAtual = personagem.defesasAtivas.notaBonusManualBloqueio,
            onDismiss = onFecharBloqueio,
            onConfirm = { periciaId, escudoNome, bonus, nota ->
                viewModel.atualizarPericiaBloqueio(periciaId)
                viewModel.atualizarEscudoBloqueio(escudoNome)
                viewModel.atualizarBonusManualBloqueio(bonus, nota)
                onFecharBloqueio()
            }
        )
    }
}

/**
 * Os diálogos de editar PV e PF atuais.
 *
 * Vieram junto na extração de 28/07, pelo mesmo motivo de teto de linhas.
 *
 * A validação do texto digitado ficou aqui dentro (`filter { isDigit() }`,
 * `take(4)`), e o que sai é **Int?** — nulo quando o campo está vazio ou
 * inválido. Assim quem chama decide o que fazer com o vazio, em vez de receber
 * um zero silencioso.
 */
@Composable
fun DialogosDePvPfRolagem(
    mostrarPv: Boolean,
    mostrarPf: Boolean,
    pvFixo: Int,
    maxPv: Int,
    pfFixo: Int,
    pvInput: String,
    pfInput: String,
    onPvInputMudou: (String) -> Unit,
    onPfInputMudou: (String) -> Unit,
    onSalvarPv: (Int?) -> Unit,
    onSalvarPf: (Int?) -> Unit,
    onFecharPv: () -> Unit,
    onFecharPf: () -> Unit
) {
    if (mostrarPv) {
        RolagemEditarPvDialog(
            pvFixoRolagem = pvFixo,
            maxPvRolagem = maxPv,
            pvAtualInput = pvInput,
            onInputMudou = { raw -> onPvInputMudou(raw.filter { it.isDigit() }.take(4)) },
            onSalvar = { onSalvarPv(pvInput.toIntOrNull()) },
            onDismiss = onFecharPv
        )
    }

    if (mostrarPf) {
        RolagemEditarPfDialog(
            pfFixoRolagem = pfFixo,
            pfAtualInput = pfInput,
            onInputMudou = { raw -> onPfInputMudou(raw.filter { it.isDigit() }.take(4)) },
            onSalvar = { onSalvarPf(pfInput.toIntOrNull()) },
            onDismiss = onFecharPf
        )
    }
}

