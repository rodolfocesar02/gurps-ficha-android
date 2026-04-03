file_path = r"c:\Users\Rodolfo\Desktop\ficha gurps\ficha-gurps\gurps_app\gurps-ficha-android\app\src\main\java\com\gurps\ficha\ui\TabRolagem.kt"
with open(file_path, "r", encoding="utf-8") as f:
    lines = f.readlines()

import_str = "import com.gurps.ficha.ui.features.rolagem.*\n"

lines.insert(73, import_str)

start_del = -1
end_del = -1
for i, line in enumerate(lines):
    if "private const val CUSTOM_ROLL_RETENTION_MS" in line:
        start_del = i
    if "@OptIn(ExperimentalMaterial3Api::class)" in line and "@Composable" in lines[i+1]:
        end_del = i
        break

if start_del != -1 and end_del != -1:
    lines = lines[:start_del] + lines[end_del:]

start_dialogs = -1
for i, line in enumerate(lines):
    if "if (showConfigAtaqueDialog)" in line:
        start_dialogs = i
        break

end_historico = -1
for i, line in enumerate(lines):
    if "SectionCard(title = \"Historico da Sessao\")" in line:
        end_historico = i
        break

if start_dialogs != -1 and end_historico != -1:
    dialog_code_1 = """            if (showConfigAtaqueDialog) {
                RolagemConfigurarAtaqueDialog(
                    opcoesAtaque = opcoesAtaque,
                    ataqueAtual = ataqueAtual,
                    onAtaqueSelecionado = { id -> viewModel.selecionarAtaque(id) },
                    onDismiss = { showConfigAtaqueDialog = false }
                )
            }

            if (showConfigDanoDialog) {
                RolagemConfigurarDanoDialog(
                    fontesDano = fontesDano,
                    fonteDanoAtual = fonteDanoAtual,
                    onFonteDanoSelecionada = { id -> viewModel.selecionarFonteDano(id) },
                    onDismiss = { showConfigDanoDialog = false }
                )
            }

            if (showPericiasDialog) {
                RolagemPericiasDialog(
                    opcoesPericia = opcoesPericia,
                    modificadoresPericia = modificadoresPericia,
                    isPraCegoVariant = isPraCegoVariant,
                    onShowDescricao = { descricaoDialog = it },
                    onExecutarRolagem = { contexto, alvo, mod -> executarRolagem(TipoTeste.PERICIA, contexto, alvo, mod) },
                    onDismiss = { showPericiasDialog = false }
                )
            }

            if (showRolagemPersonalizadaDialog) {
                RolagemPersonalizadaDialog(
                    dadosPersonalizadosQuantidade = dadosPersonalizadosQuantidade,
                    dadosPersonalizadosFaces = dadosPersonalizadosFaces,
                    dadosPersonalizadosModificador = dadosPersonalizadosModificador,
                    dadosPersonalizadosQuantidadeInput = dadosPersonalizadosQuantidadeInput,
                    dadosPersonalizadosFacesInput = dadosPersonalizadosFacesInput,
                    dadosPersonalizadosModificadorInput = dadosPersonalizadosModificadorInput,
                    expressaoPersonalizada = expressaoPersonalizada,
                    isPraCegoVariant = isPraCegoVariant,
                    onUpdateQuantidade = { dadosPersonalizadosQuantidade = it },
                    onUpdateFaces = { dadosPersonalizadosFaces = it },
                    onUpdateModificador = { dadosPersonalizadosModificador = it },
                    onInputQuantidade = { raw -> dadosPersonalizadosQuantidadeInput = raw.filter { it.isDigit() }.take(3) },
                    onInputFaces = { raw -> dadosPersonalizadosFacesInput = raw.filter { it.isDigit() }.take(3) },
                    onInputModificador = { raw -> 
                        val allowed = setOf('-') + ('0'..'9')
                        dadosPersonalizadosModificadorInput = raw.filter { it in allowed }.take(4)
                    },
                    onExecutarRolagem = {
                        val inputQtd = dadosPersonalizadosQuantidadeInput.toIntOrNull()
                        if (inputQtd != null && inputQtd in 1..300) dadosPersonalizadosQuantidade = inputQtd
                        val inputFaces = dadosPersonalizadosFacesInput.toIntOrNull()
                        if (inputFaces != null && inputFaces in 1..1000) dadosPersonalizadosFaces = inputFaces
                        val inputMod = dadosPersonalizadosModificadorInput.toIntOrNull()
                        if (inputMod != null && inputMod in -999..999) dadosPersonalizadosModificador = inputMod
                        executarRolagemPersonalizada()
                    },
                    onDismiss = { showRolagemPersonalizadaDialog = false }
                )
            }

            if (showMagiaAlmaDialog) {
                RolagemMagiaAlmaDialog(
                    aspectos = SOUL_ASPECT_OPTIONS,
                    onAspectoSelecionado = { aspectoMagiaAlmaSelecionado = it },
                    onDismiss = { showMagiaAlmaDialog = false }
                )
            }

            aspectoMagiaAlmaSelecionado?.let { aspecto ->
                RolagemDescricaoDialogModal(
                    dialogInfo = RollDescricaoDialog(titulo = "Aspecto: ${aspecto.nome}", texto = aspecto.descricao),
                    onDismiss = { aspectoMagiaAlmaSelecionado = null }
                )
            }

            if (showMagiasDialog) {
                RolagemMagiasDialog(
                    opcoesMagia = opcoesMagia,
                    modificadoresMagia = modificadoresMagia,
                    isPraCegoVariant = isPraCegoVariant,
                    onShowDescricao = { descricaoDialog = it },
                    onExecutarRolagem = { magia, modMagia ->
                        executarRolagem(
                            tipo = TipoTeste.MAGIA,
                            contextoLabel = magia.contextLabel,
                            alvo = magia.target,
                            mod = modMagia
                        )
                        tratarCustoEnergiaAposRolagemMagia(magia)
                    },
                    onDismiss = { showMagiasDialog = false }
                )
            }

            if (showTecnicasDialog) {
                RolagemTecnicasDialog(
                    opcoesTecnica = opcoesTecnica,
                    modificadoresTecnica = modificadoresTecnica,
                    isPraCegoVariant = isPraCegoVariant,
                    onShowDescricao = { descricaoDialog = it },
                    onExecutarRolagem = { contexto, alvo, mod -> executarRolagem(TipoTeste.TECNICA, contexto, alvo, mod) },
                    onDismiss = { showTecnicasDialog = false }
                )
            }

            descricaoDialog?.let { dialog ->
                RolagemDescricaoDialogModal(
                    dialogInfo = dialog,
                    onDismiss = { descricaoDialog = null }
                )
            }

            if (showEnergiaManualDialog && magiaPendenteEnergia != null) {
                RolagemEnergiaManualDialog(
                    magiaEnergia = magiaPendenteEnergia!!,
                    pfAtualRolagem = pfAtualRolagem,
                    energiaManualInput = energiaManualInput,
                    talismaMagiaVinculada = talismaMagiaVinculada,
                    repertorioParaTalisma = repertorioParaTalisma,
                    isPraCegoVariant = isPraCegoVariant,
                    onInputMudou = { raw -> energiaManualInput = raw.filter { it.isDigit() }.take(4) },
                    onTalismaVinculadoMudou = { talismaMagiaVinculada = it },
                    onAplicar = { custoFinal -> consumirEnergiaMagia(custoFinal) },
                    onDismiss = {
                        showEnergiaManualDialog = false
                        magiaPendenteEnergia = null
                        energiaManualInput = ""
                        talismaMagiaVinculada = null
                    }
                )
            }

            if (showEditarPvRolagemDialog) {
                RolagemEditarPvDialog(
                    pvFixoRolagem = pvFixoRolagem,
                    maxPvRolagem = maxPvRolagem,
                    pvAtualInput = pvAtualInput,
                    onInputMudou = { raw -> pvAtualInput = raw.filter { it.isDigit() }.take(4) },
                    onSalvar = {
                        val valor = pvAtualInput.toIntOrNull() ?: pvAtualRolagem
                        viewModel.atualizarPontosVidaRolagemAtual(valor)
                        showEditarPvRolagemDialog = false
                    },
                    onDismiss = {
                        pvAtualInput = pvAtualRolagem.toString()
                        showEditarPvRolagemDialog = false
                    }
                )
            }

            if (showEditarPfRolagemDialog) {
                RolagemEditarPfDialog(
                    pfFixoRolagem = pfFixoRolagem,
                    pfAtualInput = pfAtualInput,
                    onInputMudou = { raw -> pfAtualInput = raw.filter { it.isDigit() }.take(4) },
                    onSalvar = {
                        val valor = pfAtualInput.toIntOrNull() ?: pfAtualRolagem
                        viewModel.atualizarPontosFadigaRolagemAtual(valor)
                        showEditarPfRolagemDialog = false
                    },
                    onDismiss = {
                        pfAtualInput = pfAtualRolagem.toString()
                        showEditarPfRolagemDialog = false
                    }
                )
            }
"""
    lines = lines[:start_dialogs] + [dialog_code_1] + lines[end_historico:]

end_canal = -1
for i, line in enumerate(lines):
    if "if (showEditarCanalDialog)" in line:
        end_canal = i
        break

if end_canal != -1:
    dialog_code_2 = """    if (showEditarCanalDialog) {
        RolagemEditarCanalDialog(
            canaisDiscord = canaisDiscord,
            canalSelecionadoNome = canalSelecionadoNome,
            canaisCarregando = canaisCarregando,
            canaisErro = canaisErro,
            backendOnline = backendOnline,
            onAtualizarCanais = { viewModel.atualizarCanaisDiscord() },
            onCanalSelecionado = { canal -> viewModel.selecionarCanalDiscord(canal) },
            onDismiss = { showEditarCanalDialog = false }
        )
    }
}
"""
    lines = lines[:end_canal] + [dialog_code_2]

with open(file_path, "w", encoding="utf-8") as f:
    f.writelines(lines)
