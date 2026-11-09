package com.gurps.ficha.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.model.MontariaDados

/**
 * **Editar a montaria** — RG-11 (pedido do Rodolfo, 09/nov: "como todos os itens podem ser editados").
 *
 * O Mestre pode dar ao animal números diferentes do livro (ST maior, uma perícia melhor, treinado para a guerra): todos os
 * números da ficha técnica, as perícias e as características se editam aqui, e vão para a Mesa no arquivo da ficha. O botão
 * "Voltar aos números do livro" põe de novo os da página (pelo `id` do catálogo). O item continua com peso 0.
 */

/** Os textos dos campos, para editar (números como texto, listas uma por linha). */
internal data class CamposDaMontaria(
    val nome: String, val custo: String, val notas: String,
    val st: String, val dx: String, val iq: String, val ht: String, val pv: String, val pf: String,
    val vontade: String, val percepcao: String, val velocidade: String, val esquiva: String, val deslocamento: String,
    val mt: String, val hexes: String, val rd: String,
    val pericias: String, val caracteristicas: String, val guerra: Boolean, val montavel: Boolean
)

private fun num(f: Float): String = if (f % 1f == 0f) f.toInt().toString() else f.toString().replace('.', ',')

internal fun camposDe(nome: String, custo: Float, notas: String, m: MontariaDados) = CamposDaMontaria(
    nome, num(custo), notas, m.st.toString(), m.dx.toString(), m.iq.toString(), m.ht.toString(), m.pv.toString(), m.pf.toString(),
    m.vontade.toString(), m.percepcao.toString(), num(m.velocidadeBasica), m.esquiva.toString(), m.deslocamento.toString(),
    m.mt.toString(), m.hexes.toString(), m.rd.toString(), m.pericias.joinToString("\n"), m.caracteristicas.joinToString("\n"),
    m.treinadaParaGuerra, m.montavel
)

private fun inteiro(t: String, padrao: Int) = t.trim().toIntOrNull() ?: padrao
private fun decimal(t: String, padrao: Float) = t.trim().replace(',', '.').toFloatOrNull() ?: padrao
private fun linhas(t: String) = t.split('\n', ';').map { it.trim() }.filter { it.isNotEmpty() }

/** Aplica o que foi escrito na montaria (o que não for número fica como estava). */
internal fun aplicarNaMontaria(c: CamposDaMontaria, m: MontariaDados): MontariaDados = m.copy(
    nome = c.nome.trim().ifBlank { m.nome },
    st = inteiro(c.st, m.st).coerceIn(1, 999), dx = inteiro(c.dx, m.dx).coerceIn(1, 99), iq = inteiro(c.iq, m.iq).coerceIn(1, 99),
    ht = inteiro(c.ht, m.ht).coerceIn(1, 99), pv = inteiro(c.pv, m.pv).coerceIn(1, 9999), pf = inteiro(c.pf, m.pf).coerceIn(1, 999),
    vontade = inteiro(c.vontade, m.vontade).coerceIn(1, 99), percepcao = inteiro(c.percepcao, m.percepcao).coerceIn(1, 99),
    velocidadeBasica = decimal(c.velocidade, m.velocidadeBasica).coerceIn(0.25f, 99f), esquiva = inteiro(c.esquiva, m.esquiva).coerceIn(1, 99),
    deslocamento = inteiro(c.deslocamento, m.deslocamento).coerceIn(0, 999), mt = inteiro(c.mt, m.mt).coerceIn(-10, 20),
    hexes = inteiro(c.hexes, m.hexes).coerceIn(1, 20), rd = inteiro(c.rd, m.rd).coerceIn(0, 999),
    pericias = linhas(c.pericias), caracteristicas = linhas(c.caracteristicas), treinadaParaGuerra = c.guerra, montavel = c.montavel
)

/** O item editado: a ficha técnica nova dentro, o nome, o custo e as notas; o peso continua 0. */
fun montariaEditada(item: Equipamento, nome: String, custo: String, notas: String, nova: MontariaDados): Equipamento =
    item.copy(nome = nome.trim().ifBlank { item.nome }, custo = decimal(custo, item.custo).coerceAtLeast(0f), notas = notas, peso = 0f, montaria = nova)

@Composable
private fun CampoDeNumero(rotulo: String, valor: String, onValor: (String) -> Unit, modifier: Modifier, decimalPermitido: Boolean = false) {
    OutlinedTextField(
        value = valor, onValueChange = onValor, label = { Text(rotulo) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (decimalPermitido) KeyboardType.Decimal else KeyboardType.Number),
        modifier = modifier
    )
}

@Composable
private fun SecaoDaMontaria(texto: String) {
    Text(texto, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun ParDeCampos(r1: String, v1: String, on1: (String) -> Unit, r2: String, v2: String, on2: (String) -> Unit, decimalNo1: Boolean = false) {
    Row(horizontalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)) {
        CampoDeNumero(r1, v1, on1, Modifier.weight(1f), decimalNo1)
        CampoDeNumero(r2, v2, on2, Modifier.weight(1f))
    }
}

@Composable
private fun LinhaDeCaixa(marcado: Boolean, texto: String, onAlternar: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().linhaAlternavel(marcado, texto, onAlternar), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = marcado, onCheckedChange = null)
        Text(texto, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun EditarMontariaDialog(item: Equipamento, onDismiss: () -> Unit, onSave: (Equipamento) -> Unit) {
    val context = LocalContext.current
    val original = item.montaria ?: MontariaDados(nome = item.nome)
    var c by remember { mutableStateOf(camposDe(item.nome, item.custo, item.notas, original)) }
    val doLivro = remember { carregarMontarias(context).firstOrNull { it.id == original.id } }

    FullscreenDialogContainer(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(c.nome.ifBlank { "Editar Montaria" }, style = UiEstilos.tituloDialogo, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Os números que o Mestre definir valem na Mesa. Peso 0: não pesa na sua carga.", style = UiEstilos.subtituloDialogo, color = MaterialTheme.colorScheme.outline)
            Column(modifier = Modifier.weight(1f).rolagemVertical(), verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)) {
                SecaoDaMontaria("Item")
                OutlinedTextField(value = c.nome, onValueChange = { c = c.copy(nome = it) }, label = { Text("Nome") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                CampoDeNumero("Custo (\$)", c.custo, { c = c.copy(custo = it) }, Modifier.fillMaxWidth(), decimalPermitido = true)

                SecaoDaMontaria("Atributos")
                ParDeCampos("ST", c.st, { c = c.copy(st = it) }, "DX", c.dx, { c = c.copy(dx = it) })
                ParDeCampos("IQ", c.iq, { c = c.copy(iq = it) }, "HT", c.ht, { c = c.copy(ht = it) })
                ParDeCampos("PV", c.pv, { c = c.copy(pv = it) }, "PF", c.pf, { c = c.copy(pf = it) })

                SecaoDaMontaria("Secundários")
                ParDeCampos("Vontade", c.vontade, { c = c.copy(vontade = it) }, "Percepção", c.percepcao, { c = c.copy(percepcao = it) })
                ParDeCampos("Velocidade", c.velocidade, { c = c.copy(velocidade = it) }, "Deslocamento", c.deslocamento, { c = c.copy(deslocamento = it) }, decimalNo1 = true)
                ParDeCampos("Esquiva", c.esquiva, { c = c.copy(esquiva = it) }, "RD", c.rd, { c = c.copy(rd = it) })

                SecaoDaMontaria("Tamanho")
                ParDeCampos("MT", c.mt, { c = c.copy(mt = it) }, "Hexágonos", c.hexes, { c = c.copy(hexes = it) })

                SecaoDaMontaria("Treino")
                LinhaDeCaixa(c.guerra, "Treinada para a guerra") { c = c.copy(guerra = !c.guerra) }
                LinhaDeCaixa(c.montavel, "Pode ser montada") { c = c.copy(montavel = !c.montavel) }

                SecaoDaMontaria("Perícias e características")
                OutlinedTextField(value = c.pericias, onValueChange = { c = c.copy(pericias = it) }, label = { Text("Perícias (uma por linha: Briga 12)") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = c.caracteristicas, onValueChange = { c = c.copy(caracteristicas = it) }, label = { Text("Características (uma por linha)") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = c.notas, onValueChange = { c = c.copy(notas = it) }, label = { Text("Notas") }, minLines = 2, modifier = Modifier.fillMaxWidth())

                if (doLivro != null) {
                    AppBotaoDiscreto(texto = "Voltar aos números do livro (MB p.${doLivro.pagina})",
                        onClick = { c = camposDe(doLivro.nome, doLivro.custo, "MB p.${doLivro.pagina}", doLivro) })
                }
            }
            AppFileiraDeBotoes {
                AppBotaoSecundario(texto = UiActionLabels.CANCELAR, onClick = onDismiss)
                AppBotaoPrincipal(texto = UiActionLabels.SALVAR, enabled = c.nome.isNotBlank(), onClick = {
                    onSave(montariaEditada(item, c.nome, c.custo, c.notas, aplicarNaMontaria(c, original)))
                })
            }
        }
    }
}
