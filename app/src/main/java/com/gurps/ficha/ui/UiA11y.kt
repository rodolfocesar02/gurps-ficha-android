package com.gurps.ficha.ui

import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import com.gurps.ficha.BuildConfig

fun Modifier.pracegoTraversal(index: Int): Modifier {
    if (!BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)) return this
    return this.semantics { traversalIndex = index.toFloat() }
}

/**
 * Linha inteira com caixa de seleção, para o TalkBack ler como **um** elemento.
 *
 * ## O problema que isto resolve
 *
 * O jeito intuitivo de montar uma linha com caixinha é:
 *
 * ```kotlin
 * Row(Modifier.clickable { alternar() }.semantics { contentDescription = "..." }) {
 *     Checkbox(checked = x, onCheckedChange = { alternar() })   // ← aqui
 *     Text("ST Braçal +3")
 * }
 * ```
 *
 * Parece certo e **não é**. O `Checkbox` com `onCheckedChange` preenchido é
 * clicável por conta própria, então vira um **segundo** ponto de parada do
 * TalkBack — e sem rótulo nenhum. Quem navega por toque ouve a descrição
 * inteira, arrasta o dedo, e ouve *"caixa de seleção, não marcada"* sozinha,
 * sem saber do quê. Pior: a linha de cima é anunciada como botão e **não diz se
 * está marcada**.
 *
 * ## O jeito certo
 *
 * `toggleable` no Row, com `role = Role.Checkbox`, e o `Checkbox` visual com
 * `onCheckedChange = null` — desligado como alvo de toque, mantido como desenho.
 * Resultado: **um** ponto de parada, que anuncia o rótulo E o estado, e responde
 * ao gesto de alternar do TalkBack.
 *
 * Analogia: é a diferença entre uma etiqueta colada na caixa e uma etiqueta
 * solta ao lado dela. A informação existe nas duas, mas só na primeira ela vem
 * junto com o objeto.
 *
 * @param marcado estado atual, anunciado pelo leitor de tela.
 * @param descricao o que o TalkBack lê. **Não inclua "marcado"/"desmarcado"** —
 *   o sistema já anuncia o estado, e repetir vira eco.
 */
fun Modifier.linhaAlternavel(
    marcado: Boolean,
    descricao: String,
    onAlternar: () -> Unit
): Modifier = this
    .toggleable(
        value = marcado,
        role = Role.Checkbox,
        onValueChange = { onAlternar() }
    )
    .semantics { contentDescription = descricao }
