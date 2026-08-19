package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.gurps.ficha.R
import androidx.compose.ui.platform.LocalContext
import com.gurps.ficha.domain.rules.CorDaNota
import com.gurps.ficha.domain.rules.NotaParaDiscord
import com.gurps.ficha.model.NotaDeJogo
import com.gurps.ficha.ui.AppBotaoPrincipal
import com.gurps.ficha.ui.AppBotaoSecundario
import com.gurps.ficha.ui.AppFileiraDeBotoes
import com.gurps.ficha.ui.UiEstilos
import kotlinx.coroutines.launch
import com.gurps.ficha.viewmodel.FichaViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * **O Bloco de Notas** — Lote NOTA-1.
 *
 * O pedido do usuário: *"é basicamente um Google Notas, só que dentro do app: o
 * jogador faz anotações dos acontecimentos do jogo, e pode compartilhar para
 * fora do app e enviar para o Discord"*.
 *
 * Esta é a **lista** — o caderno aberto. Cada nota é um cartão com o título
 * derivado, a data e a cor de fundo. Toque abre o [EditorDeNota]; o ícone do
 * Discord manda a anotação para a mesa.
 *
 * Três destinos, e cada um tem o seu lugar:
 *
 * | Para onde | Onde fica o botão | O que viaja |
 * |---|---|---|
 * | Fica no app | (automático, ao fechar o editor) | tudo: texto, cor, datas |
 * | Fora do app | dentro do editor, ícone de compartilhar | só o texto |
 * | Discord | aqui na lista, ícone do Discord | o texto, como anotação |
 *
 * ## ⚠️ O envio ao Discord pede confirmação
 *
 * Mandar para a mesa é **irreversível**: o que caiu no canal, caiu. Um toque
 * sem volta ao lado de um toque que só abre a nota é um acidente à espera —
 * por isso existe o [ConfirmarEnvioDaNota] no meio dos dois.
 *
 * ## 🔴 A anotação viaja no envelope de uma ROLAGEM
 *
 * Não há rota de "mandar texto" no servidor: existe [POST /api/rolls] e mais
 * nada. A nota entra ali com [testType] valendo "Nota", para o outro lado
 * distinguir, e quem monta o pacote é [NotaParaDiscord] — **fora da tela**,
 * porque este era o quinto lugar do app a montar um [DiscordRollPayload] à mão.
 *
 * O Lote NOTA-2 nasceu disso: a anotação chegava ao Discord **parecendo uma
 * rolagem**, com dados e margem de sucesso que nunca existiram.
 *
 * ⚠️ O canal é o mesmo já escolhido na aba Rolagem
 * ([FichaViewModel.canalDiscordSelecionadoId]). Sem canal escolhido o envio
 * falha, e o aviso vem por Toast — com o motivo, e não só "não deu".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoBlocoDeNotas(
    viewModel: FichaViewModel,
    onClose: () -> Unit
) {
    val notas = viewModel.personagem.notasDeJogo
    var notaEmEdicao by remember { mutableStateOf<NotaDeJogo?>(null) }
    // Lote NOTA-1: a nota esperando confirmação para ir ao Discord.
    var notaParaEnviar by remember { mutableStateOf<NotaDeJogo?>(null) }
    val escopo = rememberCoroutineScope()
    val contexto = LocalContext.current

    notaParaEnviar?.let { nota ->
        ConfirmarEnvioDaNota(
            nota = nota,
            onCancelar = { notaParaEnviar = null },
            onConfirmar = {
                notaParaEnviar = null
                // ⚠️ O envio é irreversível e pode falhar (rede, chave, canal).
                // O usuário precisa saber das duas coisas — sucesso silencioso
                // faria ele mandar de novo achando que não foi.
                // 🔴 Achado pelo usuário no aparelho: *"o canal está definido,
                // porém quando mando a msg ele diz canal de envio não
                // definido"*. Estava certo — eu não passava o canal, e o
                // servidor devolvia 400.
                //
                // ⚠️ As quatro rolagens da aba já passavam
                // `canalDiscordSelecionadoId`. A nota era a **quinta rota** para
                // o mesmo envio, e nasceu diferente das outras quatro — o
                // formato de defeito de sempre neste projeto.
                NotaParaDiscord.payloadDe(
                    nomeDoPersonagem = viewModel.personagem.nome,
                    nota = nota,
                    canalId = viewModel.canalDiscordSelecionadoId
                )?.let { pacote ->
                    escopo.launch {
                        val envio = viewModel.enviarRolagemDiscord(pacote)
                        Toast.makeText(
                            contexto,
                            if (envio.enviado) "Anotação enviada."
                            else "Não foi enviada: ${envio.detalhe ?: "erro desconhecido"}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
    }

    if (notaEmEdicao != null) {
        EditorDeNota(
            nota = notaEmEdicao!!,
            onSalvar = { 
                viewModel.salvarNotaDeJogo(it)
                notaEmEdicao = null
            },
            onExcluir = { id ->
                viewModel.excluirNotaDeJogo(id)
                notaEmEdicao = null
            },
            onClose = { notaEmEdicao = null }
        )
    } else {
        Dialog(
            onDismissRequest = onClose,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Notas", style = MaterialTheme.typography.headlineMedium) },
                            actions = {
                                IconButton(onClick = onClose) {
                                    Icon(Icons.Default.Close, contentDescription = "Fechar")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent
                            )
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { notaEmEdicao = NotaDeJogo() },
                            containerColor = Color(0xFFFFB300),
                            contentColor = Color.White,
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Nova Nota", modifier = Modifier.size(32.dp))
                        }
                    },
                    containerColor = Color.Transparent
                ) { paddingValues ->
                    if (notas.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Nenhuma nota. Clique em + para criar.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalItemSpacing = 8.dp,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            items(notas) { nota ->
                                CardNota(
                                    nota = nota,
                                    onClick = { notaEmEdicao = nota },
                                    onEnviarDiscord = { notaParaEnviar = nota }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardNota(
    nota: NotaDeJogo,
    onClick: () -> Unit,
    onEnviarDiscord: () -> Unit = {}
) {
    val sdf = remember { SimpleDateFormat("dd 'de' MMM", Locale("pt", "BR")) }
    val dataExibicao = sdf.format(Date(nota.dataCriacao))
    val previa = if (nota.texto.length > 60) nota.texto.take(60) + "…" else nota.texto
    val descricaoAcessivel = "Nota: ${nota.titulo}. $previa. Criada em $dataExibicao"
    // A cor da nota, ou null quando ela não tem (ou o hex não dá para ler).
    val corDaNota = nota.corHex?.let {
        try {
            Color(android.graphics.Color.parseColor(it))
        } catch (e: Exception) {
            null
        }
    }
    val backgroundColor = corDaNota ?: MaterialTheme.colorScheme.surfaceVariant

    /**
     * 🔴 A cor do texto sai do FUNDO quando a nota TEM cor.
     *
     * O fundo colorido é sempre claro, e `onSurface` no tema escuro é quase
     * branco — a nota ficava ilegivel em metade dos aparelhos da mesa.
     *
     * ⚠️ **Só quando ela tem cor.** Sem cor o fundo é `surfaceVariant`, que é
     * do tema, e ali a cor do tema é a certa. Sobrepor também nesse caso
     * trocaria um problema de contraste por outro.
     */
    val corDoTexto = if (corDaNota != null) {
        Color(android.graphics.Color.parseColor(CorDaNota.textoSobre(nota.corHex)))
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            // 🔴 Lote NOTA-1: `mergeDescendants` funde o card num nó só, e com
            // isso o ícone de enviar **some** para o leitor de tela — ele deixa
            // de ser um alvo próprio. Na variante pracego o envio ficaria
            // inalcançável, e ninguém perceberia olhando a tela.
            //
            // A saída do Compose para isso é a ação personalizada: o card
            // continua sendo lido de uma vez, e o envio vira um item do menu de
            // ações do leitor.
            .semantics(mergeDescendants = true) {
                contentDescription = descricaoAcessivel
                if (nota.texto.isNotBlank()) {
                    customActions = listOf(
                        CustomAccessibilityAction("Enviar esta anotação para o Discord") {
                            onEnviarDiscord()
                            true
                        }
                    )
                }
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = nota.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = corDoTexto
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = nota.texto,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                color = corDoTexto.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            // Lote NOTA-1: a data de um lado, o envio do outro — o canto oposto,
            // como o usuário pediu. O ícone fica FORA do `clickable` do card:
            // tocar no card abre a nota para editar, tocar aqui manda para a
            // mesa. Duas ações muito diferentes não podem dividir o mesmo alvo.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dataExibicao,
                    style = MaterialTheme.typography.labelSmall,
                    color = corDoTexto.copy(alpha = 0.7f)
                )
                // ⚠️ Só aparece quando há o que mandar. Nota em branco não
                // oferece o botão — e a regra também recusa, do outro lado.
                if (nota.texto.isNotBlank()) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_discord),
                        contentDescription = "Enviar a anotação ${nota.titulo} para o Discord",
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onEnviarDiscord)
                            .padding(4.dp)
                            .size(TAMANHO_DO_ICONE_DISCORD)
                    )
                }
            }
        }
    }
}

/**
 * O tamanho do logo do Discord no canto do card.
 *
 * ⚠️ O PNG é de 40 px. Desenhá-lo maior que isso deixa a marca serrilhada — e um
 * logo borrado lê como app mal-acabado, não como decisão.
 *
 * 🔴 `Image`, e **não** `Icon`: o `Icon` do Material pinta o desenho inteiro com
 * uma cor só (`tint`), e o logo do Discord viraria uma mancha chapada.
 */
private val TAMANHO_DO_ICONE_DISCORD = 20.dp

/**
 * A confirmação antes de mandar para a mesa — Lote NOTA-1.
 *
 * ⚠️ Ela existe porque **enviar é irreversível**: a mensagem cai no canal e não
 * volta. O padrão do app para isso é este — pergunta curta, prévia do que vai
 * sair, e os dois botões na `AppFileiraDeBotoes`, com o "Não" em secundário.
 */
@Composable
fun ConfirmarEnvioDaNota(
    nota: NotaDeJogo,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    Dialog(onDismissRequest = onCancelar) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Enviar para o Discord", style = UiEstilos.tituloDialogo)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    NotaParaDiscord.perguntaDeConfirmacao(nota),
                    style = UiEstilos.detalheDoItem,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                AppFileiraDeBotoes {
                    AppBotaoSecundario("Não", onCancelar)
                    AppBotaoPrincipal("Sim", onConfirmar)
                }
            }
        }
    }
}
