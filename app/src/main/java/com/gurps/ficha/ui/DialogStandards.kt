package com.gurps.ficha.ui

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun FullscreenDialogContainer(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // Mesmo problema da barra de abas (Android 15 força
                    // edge-to-edge com `targetSdk = 35`): o diálogo ocupa a tela
                    // inteira, e o botão **Fechar** ficava debaixo da barra de
                    // navegação do sistema.
                    //
                    // ⚠️ Só o de baixo. O topo o `Dialog` já resolve, e somar
                    // `statusBarsPadding` aqui empurraria o título para baixo
                    // sem necessidade — o inset já consumido devolve zero, mas o
                    // que **não** foi consumido dobraria o espaço.
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                content = content
            )
        }
    }
}
