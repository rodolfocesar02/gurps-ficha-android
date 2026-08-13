package com.gurps.ficha.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * **O campo de texto compacto** — Lote GER-2.
 *
 * ## 🔴 Por que não é só um `OutlinedTextField` menor
 *
 * O `OutlinedTextField` do Material 3 tem **altura mínima de 56 dp** cravada
 * dentro dele, e um respiro interno de 16 dp em cima e embaixo. Nada disso é
 * parâmetro: `Modifier.height()` por fora só corta o texto, e o padding interno
 * não se alcança de jeito nenhum.
 *
 * A saída é montar o campo com as peças que o Material publica: um
 * `BasicTextField` por dentro e o `OutlinedTextFieldDefaults.DecorationBox`
 * desenhando a moldura, o rótulo e as cores. **A aparência continua sendo a do
 * Material** — o que muda é só o `contentPadding`, que aí é nosso.
 *
 * ⚠️ Foi o pedido do usuário no cartão de nome/jogador/pontos: cinco campos
 * empilhados a 56 dp cada faziam o cartão ocupar meia tela antes de o primeiro
 * atributo aparecer.
 *
 * ## ⚠️ O campo NÃO tem piso de altura, e isso foi decidido olhando a tela
 *
 * A primeira versão prendia a altura em [UiTokens.TouchMinHeight] (48 dp), o
 * mínimo de toque do projeto. Com o piso, sobrava um vão entre o rótulo e o
 * texto — o campo ficava alto sem precisar, porque a altura vinha do piso e não
 * do conteúdo.
 *
 * O usuário comparou as duas na tela e escolheu a **sem piso**: a caixa passa a
 * ter a altura do que está escrito dentro dela, ~29 dp. **É abaixo dos 48 dp de
 * alvo de toque**, e a troca foi feita de olhos abertos. Quem for reapertar isso
 * de novo, saiba que está desfazendo um pedido, não corrigindo um descuido.
 *
 * O que sustenta a decisão: os campos ficam separados por 8 dp num cartão só
 * deles, sem nada clicável ao redor para se errar por perto. E na variante
 * `pracego` a caixa **cresce sozinha**, porque a altura vem do `bodyLarge` do
 * tema — quem usa fonte grande recebe campo grande sem número nenhum cravado.
 */
// O `DecorationBox` ainda e marcado como experimental pelo Material 3. E so o
// ponto de entrada: as pecas que ele monta (moldura, rotulo, cores) sao as mesmas
// do `OutlinedTextField` estavel.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppCampoCompacto(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true
) {
    val interacao = remember { MutableInteractionSource() }
    val cores = OutlinedTextFieldDefaults.colors()

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        interactionSource = interacao,
        decorationBox = { campoInterno ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = campoInterno,
                enabled = true,
                singleLine = singleLine,
                visualTransformation = VisualTransformation.None,
                interactionSource = interacao,
                colors = cores,
                label = {
                    Text(label, style = MaterialTheme.typography.labelSmall)
                },
                // O respiro mínimo que ainda deixa o texto respirar. O de fábrica
                // é 16 dp em cima e embaixo; aqui são 2, e o rótulo flutuante
                // continua cabendo porque ele desenha **sobre** a borda.
                contentPadding = OutlinedTextFieldDefaults.contentPadding(
                    start = 10.dp, top = 2.dp, end = 10.dp, bottom = 2.dp
                ),
                container = {
                    OutlinedTextFieldDefaults.ContainerBox(
                        enabled = true,
                        isError = false,
                        interactionSource = interacao,
                        colors = cores,
                        shape = MaterialTheme.shapes.extraSmall
                    )
                }
            )
        }
    )
}
