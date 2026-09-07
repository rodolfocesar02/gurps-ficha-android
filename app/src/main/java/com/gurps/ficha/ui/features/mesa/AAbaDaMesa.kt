package com.gurps.ficha.ui.features.mesa

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.gurps.ficha.domain.rules.DestinoDaRolagem
import com.gurps.ficha.viewmodel.FichaViewModel

/**
 * **Quando a aba da Mesa existe, e quando se salta para ela** — MNA-1 e MNA-10.
 *
 * ## ⚠️ Por que isto não mora na `FichaScreen`
 *
 * Morava, e não podia. O `FichaScreen.kt` já estava em **1048 linhas** antes
 * deste plano, contra o teto de 1000 do projeto — e eu escrevi no plano, com
 * todas as letras, que a aba entraria por *"três linhas, sem engordar"*. Entrou
 * por sessenta e três.
 *
 * 🔴 O teto não é enfeite: a `FichaScreen` é a tela por onde tudo passa, e cada
 * regra escrita lá dentro é uma regra que só se lê rolando por mil linhas de
 * outra coisa. Estas duas são de um assunto só — a Mesa —, e é aqui que quem
 * procura a Mesa vai olhar.
 */

/**
 * **A aba só aparece quando você está ligado à Mesa.**
 *
 * > *"É igual a aba da magia, ela só aparece quando tem magia."*
 *
 * 🔴 Ela é a **consequência** de estar conectado, e nunca a porta de entrada:
 * quem liga a Mesa pela primeira vez faz isso na tela de configuração, que já
 * existe e já testa a sala.
 *
 * ⚠️ Fora do `pracego` por enquanto. Uma Mesa dentro de um navegador é uma coisa
 * visual, e fingir que não é seria pior do que não a ter — ela volta quando
 * houver plano próprio para essa variante.
 */
fun aAbaDaMesaAparece(viewModel: FichaViewModel, ehPraCego: Boolean): Boolean =
    !ehPraCego &&
        viewModel.destinoDaRolagem == DestinoDaRolagem.MESA &&
        !viewModel.mesaToken.isNullOrBlank()

/**
 * **CONECTAR À MESA abre a aba** — MNA-10.
 *
 * @param aparece se a aba já está na lista de abas
 * @param irPara o que fazer para trocar de aba
 *
 * ⚠️ Espera a aba **existir** antes de saltar, e é por isso que o [aparece] entra
 * aqui. O token acabou de ser guardado e a recomposição pode não ter chegado; um
 * salto para uma aba que ainda não está na lista cai na primeira, e a pessoa fica
 * olhando para o Geral sem entender o que aconteceu.
 *
 * 🔴 E o pedido é limpo **depois** do salto, e não por quem o fez: limpá-lo cedo
 * gastaria a única chance que a aba tem de aparecer.
 */
@Composable
fun SaltarParaAMesaQuandoPedirem(
    viewModel: FichaViewModel,
    aparece: Boolean,
    irPara: (String) -> Unit
) {
    LaunchedEffect(viewModel.irParaAMesa, aparece) {
        if (viewModel.irParaAMesa && aparece) {
            irPara("Mesa")
            viewModel.irParaAMesa = false
        }
    }
}

/**
 * **A Mesa manda no microfone enquanto a sala estiver ligada** — MNA-7.
 * Decisão dele.
 *
 * @return o recado a mostrar, ou `null` quando o microfone está livre
 *
 * 🔴 O Android **não garante dois donos do microfone**. Deixar os dois ligados
 * daria um dos dois emudecendo em silêncio, sem erro na tela — e quem emudecesse
 * só seria descoberto por outra pessoa dizendo *"não te ouço"*, no meio de uma
 * cena.
 *
 * ⚠️ Um recado, e não um botão morto: um botão que não faz nada é pior do que um
 * botão que explica.
 */
fun oQueImpedeAVozDoMestreIA(): String? =
    if (SalaDaMesa.estaDePe) {
        "O microfone está com a Mesa. Saia da mesa para falar com o Mestre IA."
    } else {
        null
    }
