package com.gurps.ficha.ui.features.mesa

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
 * **A aba existe enquanto a SALA estiver de pé** — MNA-1c.
 *
 * ## 🟥 A regra anterior estava certa no papel e errada na tela
 *
 * Era *"destino MESA **e** token guardado"* — a mesma forma da aba Magia, e foi
 * o que ficou combinado. Só que o token **fica guardado para sempre** depois da
 * primeira conexão. Na prática a aba virava permanente, que é o contrário de
 * *"só aparece quando você entra"*.
 *
 * Ele viu no aparelho e perguntou:
 *
 * > *"ainda está aparecendo! mesmo não tendo entrado na mesa! a ideia não seria
 * > aparecer apenas quando 'entrasse' na mesa? ou eu estou confuso?"*
 *
 * Não estava confuso. Eu é que tinha cumprido a letra da regra sem olhar o que
 * ela fazia depois da segunda vez.
 *
 * ## 🔴 A regra nova, e o nó que ela precisa desatar
 *
 * *"A aba existe enquanto a sala estiver de pé."* Dito assim, é impossível: a
 * sala só sobe quando a aba abre, e a aba só abre se a sala estiver de pé.
 *
 * O nó desata na **porta de entrada**, que já existe e é o CONECTAR À MESA
 * (MNA-10). Ele levanta o [FichaViewModel.irParaAMesa], e é esse pedido — e só
 * ele — que faz a aba aparecer antes de a sala existir.
 *
 * Então o ciclo inteiro é:
 *
 * 1. CONECTAR À MESA, com a sala aceitando o token → a aba aparece;
 * 2. você entra → a sala fica de pé → a aba fica pelo mesmo motivo;
 * 3. SAIR → a sala cai → **a aba some**, e você volta para a ficha.
 *
 * ⚠️ E fechar o aplicativo derruba a sala junto. Ao reabrir não há aba nenhuma,
 * e entra-se pela porta — que é onde se confere o token de qualquer maneira.
 */
fun aAbaDaMesaAparece(viewModel: FichaViewModel, ehPraCego: Boolean): Boolean =
    !ehPraCego && (SalaDaMesa.estaDePe || viewModel.irParaAMesa)

/**
 * **CONECTAR À MESA abre a aba** — MNA-10.
 *
 * @param aparece se a aba já está na lista de abas
 * @param irPara o que fazer para trocar de aba
 *
 * ⚠️ Espera a aba **existir** antes de saltar. O token acabou de ser guardado e a
 * recomposição pode não ter chegado; um salto para uma aba que ainda não está na
 * lista cai na primeira, e a pessoa fica olhando para o Geral sem entender.
 *
 * ## 🟥 E o pedido só é largado quando a SALA está mesmo de pé
 *
 * 🔴 Este é o ponto delicado do MNA-1c, e a primeira forma que escrevi tinha o
 * defeito dentro. Limpar o pedido logo depois de saltar parece a coisa arrumada
 * a fazer — e é uma corrida perdida: entre pôr a aba na frente e a janela
 * nascer há uma volta de desenho. Naquela volta a sala **ainda não está de pé**,
 * e sem o pedido levantado a aba desapareceria da lista no exato momento em que
 * a pessoa acabou de ser mandada para ela.
 *
 * Então o pedido fica levantado até a sala existir. É ela que o baixa.
 */
@Composable
fun SaltarParaAMesaQuandoPedirem(
    viewModel: FichaViewModel,
    aparece: Boolean,
    irPara: (String) -> Unit
) {
    LaunchedEffect(viewModel.irParaAMesa, aparece) {
        if (viewModel.irParaAMesa && aparece) irPara("Mesa")
    }
    LaunchedEffect(SalaDaMesa.estaDePe) {
        // A sala subiu: o pedido já foi cumprido e pode ser baixado.
        if (SalaDaMesa.estaDePe) viewModel.irParaAMesa = false
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
