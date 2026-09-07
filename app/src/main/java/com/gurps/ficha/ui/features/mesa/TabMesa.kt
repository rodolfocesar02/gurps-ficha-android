package com.gurps.ficha.ui.features.mesa

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.gurps.ficha.data.network.MesaApiClient
import com.gurps.ficha.domain.rules.PedidoDaMesa

/**
 * **A aba da Mesa** — lote MNA-1.
 *
 * 🔴 Esta função é **uma parede**, e não uma sala. Ela pendura a janela que o
 * [SalaDaMesa] guarda e, quando a aba sai da frente, tira a janela da parede
 * sem a apagar.
 *
 * ⚠️ Por isso ela é tão curta, e tem de continuar assim: tudo o que for estado
 * escrito aqui dentro morre na próxima troca de aba, porque a `FichaScreen`
 * desenha as abas com um `when`. Estado que precisa viver vai para o
 * [SalaDaMesa].
 */
@Composable
fun TabMesa(
    nome: String?,
    token: String?,
    aoReceberPedido: (PedidoDaMesa.Pedido) -> Unit
) {
    // 🔴 O botão Voltar anda para trás **dentro da página**. Sem isto ele sairia
    // do aplicativo a meio de uma cena, que é o que um navegador nunca faz.
    //
    // ⚠️ Quando não há para onde voltar, o `BackHandler` continua ligado e não
    // faz nada — de propósito. Deixá-lo passar faria o Voltar fechar o
    // aplicativo, e a pessoa perderia a mesa por carregar uma vez de mais.
    BackHandler { SalaDaMesa.voltarDentroDaPagina() }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        // ⚠️ A moldura é nova a cada vez, e é barata. Quem **não** pode ser novo
        // é o que vai dentro dela.
        factory = { ctx -> FrameLayout(ctx) },
        update = { moldura ->
            // ⚠️ Quem entra vai ANTES de abrir: o `onPageFinished` pode disparar
            // antes de a próxima recomposição chegar, e um convite sem nome é um
            // convite que não sai.
            SalaDaMesa.quemEntra(nome, token)
            val janela = SalaDaMesa.aJanela(
                moldura.context, MesaApiClient.ENDERECO_PADRAO, aoReceberPedido
            )
            // 🔴 Tira da parede antiga antes de pendurar nesta. Uma view só tem
            // um pai, e pendurá-la duas vezes é um erro em tempo de execução.
            (janela.parent as? ViewGroup)?.removeView(janela)
            moldura.addView(
                janela,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
            SalaDaMesa.abrirSePreciso()
        }
    )

    // 🟥 Ao sair da aba, DESPENDURA — não apaga.
    //
    // É a linha inteira do lote. Um `SalaDaMesa.sair()` aqui seria a mesma
    // demolição de sempre, escrita com outro nome.
    DisposableEffect(Unit) {
        onDispose { SalaDaMesa.despendurar() }
    }
}
