package com.gurps.ficha.ui.features.mesa

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.remember
import android.net.Uri
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
 * 🔴 O `aoReceberPedido` entrega o pedido no MESMO lugar em que o link de fora do
 * aplicativo o entregava (`MainActivity`), e daí em diante o caminho é o do
 * CC-5: a `FichaScreen` salta para a Rolagem e a `TabRolagem` abre o diálogo.
 * Nada disso é novo, e é de propósito.
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
    aoSairDaMesa: () -> Unit,
    aoReceberPedido: (PedidoDaMesa.Pedido) -> Unit
) {
    // 🔴 O botão Voltar anda para trás **dentro da página**. Sem isto ele sairia
    // do aplicativo a meio de uma cena, que é o que um navegador nunca faz.
    //
    // ⚠️ Quando não há para onde voltar, o `BackHandler` continua ligado e não
    // faz nada — de propósito. Deixá-lo passar faria o Voltar fechar o
    // aplicativo, e a pessoa perderia a mesa por carregar uma vez de mais.
    BackHandler { SalaDaMesa.voltarDentroDaPagina() }

    /**
     * **Quem abre a caixa de permissão do Android** — MNA-7.
     *
     * 🔴 A resposta chega **depois**, e por isso o que a página pediu tem de
     * ficar guardado até lá. Sem isto, o pedido do microfone se perderia entre a
     * pergunta e a resposta, e a página ficaria esperando para sempre — que é
     * exatamente o defeito que o `onPermissionRequest` veio consertar.
     */
    val aEsperaDaResposta = remember { arrayOfNulls<((Boolean) -> Unit)>(1) }
    val caixaDePermissao = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        val deu = resultado.values.all { it }
        aEsperaDaResposta[0]?.invoke(deu)
        aEsperaDaResposta[0] = null
    }

    /**
     * **O explorador de arquivos** — MNA-9. O retrato e a foto do chat.
     *
     * 🟥 A resposta é obrigatória mesmo quando é "nada": um cancelamento sem
     * resposta deixa o campo da página **morto para sempre**, e não só desta vez.
     * Por isso o guardado é chamado no `?:` também.
     */
    val aEsperaDoArquivo = remember { arrayOfNulls<((Uri?) -> Unit)>(1) }
    val explorador = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { escolhido ->
        aEsperaDoArquivo[0]?.invoke(escolhido)
        aEsperaDoArquivo[0] = null
    }

    DisposableEffect(Unit) {
        SalaDaMesa.escolherArquivo = { tipos, responder ->
            aEsperaDoArquivo[0] = responder
            try {
                explorador.launch(tipos)
                true
            } catch (_: Exception) {
                // ⚠️ Aparelho sem explorador de arquivos. Devolver `false` faz o
                // WebView responder "nada" e o campo continua vivo.
                aEsperaDoArquivo[0] = null
                false
            }
        }
        // 🔴 Sair da sala tira da aba — MNA-1b.
        SalaDaMesa.aoSairDaAba = aoSairDaMesa
        SalaDaMesa.pedirAoTelefone = { quais, responder ->
            aEsperaDaResposta[0] = responder
            caixaDePermissao.launch(quais.toTypedArray())
        }
        // ⚠️ Ao sair da aba, a sala perde a forma de perguntar — de propósito.
        // Uma caixa de permissão por cima da aba Perícias seria um susto sem
        // explicação nenhuma na tela.
        onDispose {
            SalaDaMesa.pedirAoTelefone = null
            SalaDaMesa.escolherArquivo = null
            SalaDaMesa.aoSairDaAba = null
            // 🔴 Quem estivesse esperando um arquivo recebe "nada", e não o
            // silêncio: sem isto o campo da página fica morto para sempre.
            aEsperaDoArquivo[0]?.invoke(null)
            aEsperaDoArquivo[0] = null
        }
    }

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
