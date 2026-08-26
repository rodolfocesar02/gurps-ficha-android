package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import com.gurps.ficha.viewmodel.delegates.ResultadoDaConexao
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gurps.ficha.domain.rules.DestinoDaRolagem
import com.gurps.ficha.ui.rolagemVertical
import kotlinx.coroutines.launch

/**
 * **O botão do destino** — Lote MESA-7.
 *
 * ## Por que é um botão NOVO, e não o de CANAL renomeado
 *
 * Pedido do usuário, palavra por palavra: *"não vamos substituir o nosso botão,
 * vamos acrescentar um novo, aí podemos pelo app escolher qual o servidor vamos
 * usar."*
 *
 * A tentação era transformar o botão CANAL em "ONDE ENVIAR" e pendurar o Discord
 * dentro dele. Seria uma tela a menos — e uma regressão para quem só usa Discord:
 * o caminho que essa pessoa faz há meses (um toque, escolher o canal) passaria a
 * ter dois toques e uma pergunta nova. O botão de CANAL fica exatamente onde
 * estava, fazendo exatamente o que fazia.
 *
 * ⚠️ Este botão mostra o **destino atual**, não o que está pronto. Se faltar o
 * token da Mesa ele continua dizendo "Mesa virtual" e avisa embaixo — esconder a
 * escolha porque a configuração está incompleta faria o usuário procurar um botão
 * que sumiu.
 */
@Composable
fun RolagemDestinoBotao(
    destino: DestinoDaRolagem,
    oQueFalta: String?,
    isVerySmallScreen: Boolean,
    onClick: () -> Unit
) {
    val tamanho = if (isVerySmallScreen) 15.sp else 17.sp
    val faltaAlgo = oQueFalta != null

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .semantics {
                contentDescription = buildString {
                    append("Escolher para onde a rolagem vai. Destino atual: ")
                    append(destino.rotulo)
                    append(".")
                    if (faltaAlgo) append(" Atenção: " + oQueFalta)
                }
            },
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
        colors = ButtonDefaults.buttonColors(
            // 🔴 Vermelho quando falta dado: sem isso o usuário só descobriria o
            // problema no meio da sessão, com a rolagem feita e ninguém vendo.
            containerColor = if (faltaAlgo) MaterialTheme.colorScheme.error
                             else MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ENVIAR PARA",
                fontSize = tamanho,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
            )
            Text(
                text = "  " + destino.rotulo,
                fontSize = tamanho,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

/**
 * **A escolha do destino, e a configuração da Mesa.**
 *
 * ⚠️ Os campos da Mesa só aparecem quando a Mesa está escolhida. Não é enfeite:
 * são dois campos que só fazem sentido juntos, e mostrá-los para quem usa Discord
 * seria oferecer uma configuração que não muda nada.
 *
 * ⚠️ O endereço **não é validado** enquanto se digita. Quem digita isso está no
 * telefone, lendo o endereço em voz alta do PC do Mestre — reclamar a cada letra
 * ("endereço inválido") deixaria o campo vermelho o tempo todo. Quem diz se está
 * certo é o botão de testar, que fala com a sala de verdade.
 */
@Composable
fun RolagemDestinoDialog(
    destino: DestinoDaRolagem,
    tokenAtual: String?,
    nomeNaMesaAtual: String?,
    oQueFalta: String?,
    onEscolherDestino: (DestinoDaRolagem) -> Unit,
    onSalvarMesa: (String?, String?) -> Unit,
    /**
     * Testa a ligacao. Devolve **se deu certo** e **o que dizer**.
     *
     * 🔴 Duas coisas separadas de proposito. A primeira versao devolvia so a
     * frase e esta tela adivinhava o resto procurando "erro" dentro dela --
     * e "Token errado" passava pela peneira.
     */
    onTestarMesa: suspend () -> ResultadoDaConexao,
    /**
     * Abre a mesa no navegador que a pessoa escolher, ja com o nome e o token.
     *
     * 🔴 Quem monta o endereco e quem sabe abrir e o `TabRolagem`: um dialogo
     * nao devia saber o que e um `Intent`.
     */
    onAbrirAMesa: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var token by remember(tokenAtual) { mutableStateOf(tokenAtual.orEmpty()) }
    var nomeNaMesa by remember(nomeNaMesaAtual) { mutableStateOf(nomeNaMesaAtual.orEmpty()) }
    var resultadoDoTeste by remember { mutableStateOf<ResultadoDaConexao?>(null) }
    var testando by remember { mutableStateOf(false) }
    val escopo = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Para onde vão as rolagens") },
        text = {
            Column(
                modifier = Modifier.rolagemVertical(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(Modifier.selectableGroup()) {
                    DestinoDaRolagem.entries.forEach { opcao ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = destino == opcao,
                                    onClick = { onEscolherDestino(opcao) }
                                )
                                .semantics {
                                    contentDescription = opcao.rotulo + ". " + opcao.descricao
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = destino == opcao, onClick = null)
                            Column {
                                Text(opcao.rotulo, fontWeight = FontWeight.Bold)
                                Text(
                                    opcao.descricao,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (destino == DestinoDaRolagem.MESA) {
                    // 🔴 **O endereco saiu da tela** (MESA-44). Ele nunca muda, e era
                    // mais um campo para digitar errado. Vive agora em
                    // `MesaApiClient.ENDERECO_PADRAO`.
                    //
                    // ⚠️ Esta tela virou o que ja era na pratica: a porta de entrada da
                    // mesa. O nome e o token sao os MESMOS que se digitam no site --
                    // e o nome, que antes era um terceiro campo misterioso ("seu nome
                    // na mesa"), agora explica-se sozinho por estar ao lado do token.
                    OutlinedTextField(
                        value = nomeNaMesa,
                        onValueChange = { nomeNaMesa = it },
                        label = { Text("Seu nome") },
                        placeholder = { Text("o nome com que voce entra na mesa") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription =
                                    "Seu nome na mesa virtual, o mesmo com que voce entra na sala"
                            }
                    )
                    OutlinedTextField(
                        value = token,
                        // ⚠️ Maiúsculas na entrada: o token é ditado em voz alta na
                        // mesa e digitado às pressas. O servidor compara byte a byte.
                        onValueChange = { token = it.uppercase() },
                        label = { Text("Token da sala") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Token da sala da mesa virtual" }
                    )

                    Button(
                        onClick = {
                            onSalvarMesa(token, nomeNaMesa)
                            testando = true
                            resultadoDoTeste = null
                            escopo.launch {
                                val r = onTestarMesa()
                                resultadoDoTeste = r
                                testando = false
                                // 🔴 **So abre a mesa se a sala tiver aceitado o token.**
                                //
                                // Abrir o navegador com token errado poria a pessoa
                                // diante da tela de entrada da mesa, sem saber por que
                                // -- e ela culparia o navegador, nao o token. Assim o
                                // recado aparece aqui, onde ela esta olhando.
                                if (r.ok) onAbrirAMesa(nomeNaMesa.trim(), token.trim())
                            }
                        },
                        // ⚠️ O botao so acende com os dois campos preenchidos: sem eles
                        // nao ha o que testar nem o que abrir, e um botao que nao faz
                        // nada e pior que um botao apagado.
                        enabled = !testando &&
                            nomeNaMesa.isNotBlank() && token.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        Text(
                            text = if (testando) "CONECTANDO..." else "CONECTAR À MESA",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    resultadoDoTeste?.let { r ->
                        Text(r.recado, style = MaterialTheme.typography.labelSmall)
                    }
                }

                if (oQueFalta != null) {
                    Text(
                        oQueFalta,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                // ⚠️ Salvar ao fechar, e não só no botão de testar: quem digitou
                // certo e fechou sem testar perderia tudo o que escreveu.
                if (destino == DestinoDaRolagem.MESA) onSalvarMesa(token, nomeNaMesa)
                onDismiss()
            }) {
                Text("Fechar")
            }
        }
    )
}
