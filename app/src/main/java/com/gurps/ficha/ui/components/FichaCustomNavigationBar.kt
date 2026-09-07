package com.gurps.ficha.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gurps.ficha.R

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun FichaCustomNavigationBar(
    tabs: List<String>,
    currentIndex: Int,
    onTabClick: (Int) -> Unit,
    onMestreIAClick: () -> Unit = {},
    onMestreIALongPress: () -> Unit = {},
    mestreIAAberto: Boolean = false,
    estadoVoz: EstadoVoz = EstadoVoz.OCIOSO,
    estadoLive: EstadoLive = EstadoLive.OCIOSO,
    isPraCegoVariant: Boolean = false
) {
    // Mapeia EstadoLive para EstadoVoz para reutilizar o anel visual existente
    val estadoVozEfetivo = when {
        estadoLive == EstadoLive.OUVINDO      -> EstadoVoz.ESCUTANDO    // anel verde
        estadoLive == EstadoLive.FALANDO      -> EstadoVoz.PROCESSANDO  // anel amarelo
        estadoLive == EstadoLive.CONECTANDO   -> EstadoVoz.PROCESSANDO  // anel amarelo
        estadoLive == EstadoLive.PROCESSANDO  -> EstadoVoz.PROCESSANDO  // anel amarelo
        else -> estadoVoz
    }

    /**
     * **O nome aparece e some** — lote BARRA-1.
     *
     * > *"precisamos dos nomes, mas nao quero eles fixos"*
     *
     * 🔴 Antes ele vivia **ao lado do ícone**, posicionado por conta de pixel
     * escrita à mão, e saía da caixa com `unbounded`. No vídeo dele isso aparece
     * inteiro: *PERÍCIAS* escrito por cima dos três últimos ícones, e *TÉCNICAS*
     * cortado no meio, virando *"CNICAS"*.
     *
     * ⚠️ Agora é **um** nome, numa linha própria acima da fila. Não tem como
     * colidir com nada, porque não há nada ao lado dele.
     */
    var nomeAVista by remember { mutableStateOf(false) }
    LaunchedEffect(currentIndex) {
        nomeAVista = true
        kotlinx.coroutines.delay(2000)
        nomeAVista = false
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            // 🔴 Sem isto a barra do Android fica POR CIMA das abas (achado em
            // aparelho físico com Android 15, 31/07).
            //
            // A causa é o `targetSdk = 35`: a partir do **Android 15** o sistema
            // **força** o modo edge-to-edge, e o app passa a desenhar embaixo da
            // barra de navegação. Quem tem de reservar o espaço é o app. O
            // `Scaffold` do Material 3 faz isso sozinho para uma `NavigationBar`
            // dele — mas esta barra é um `Box` nosso, e ninguém reservava nada.
            //
            // ⚠️ `navigationBarsPadding()` usa o inset **ainda não consumido**:
            // se um pai já tiver reservado o espaço, ele aplica zero. Por isso
            // não há risco de padding dobrado.
            .navigationBarsPadding()
            .padding(bottom = 1.dp, top = 2.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // == A linha do nome ==========================================
            //
            // ⚠️ Altura **reservada**, e não `wrapContent`: se ela crescesse e
            // encolhesse com o nome, a fila inteira subiria e desceria a cada
            // troca de aba — que é o defeito que este lote veio consertar, com
            // outra roupa.
            Box(
                modifier = Modifier.height(ALTURA_DO_NOME),
                contentAlignment = Alignment.Center
            ) {
                // ⚠️ Qualificado: dentro de uma `Column` o Kotlin escolhe o
                // `ColumnScope.AnimatedVisibility`, que aqui nao tem receptor.
                androidx.compose.animation.AnimatedVisibility(
                    visible = nomeAVista,
                    enter = fadeIn(tween(220)),
                    exit = fadeOut(tween(400))
                ) {
                    TabNameLabel(tabs.getOrNull(currentIndex).orEmpty())
                }
            }

            /**
             * 🟥 **O Mestre IA entra na FILA** — achado dele, no aparelho:
             *
             * > *"o icone do mestre IA, ficou fora do alinhamento, ele pode
             * > entrar no grupo ficar todos icones na mesma linha!"*
             *
             * 🔴 Ele estava numa âncora à esquerda, fora do grupo, com um
             * contrapeso vazio do outro lado para o centro não sair torto. Era uma
             * conta a mais para uma coisa que se resolve pondo tudo na mesma
             * linha: com ele **dentro**, o grupo é o que é, e o centro é o centro.
             *
             * ⚠️ E ele continua a **não ser uma aba**: toque abre o chat, segurar
             * liga a voz, e o cometa não para debaixo dele — ver o
             * `OTrilhoDoCometa`.
             */
            BoxWithConstraints(
                // ⚠️ A folga em cima, para o ícone crescido não subir para dentro
                // da linha do nome.
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                /**
                 * 🔴 A largura de cada lugar é **calculada**, e não fixa.
                 *
                 * ⚠️ São sete abas num dia e nove no outro (as Magias entram com a
                 * aptidão; a Mesa, com a sala aberta), mais o Mestre IA. Uma
                 * largura fixa que coubesse dez desperdiçaria tela com oito, e uma
                 * que ficasse bonita com oito estouraria a barra com dez.
                 */
                val quantosLugares = tabs.size + 1
                val larguraDaAba =
                    minOf(LARGURA_IDEAL_DA_ABA, maxWidth / quantosLugares)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        // O Mestre IA, primeiro da fila e do mesmo tamanho.
                        Box(modifier = Modifier.width(larguraDaAba)) {
                            RPGNavigationItem(
                                label = "Mestre IA",
                                iconRes = R.drawable.tab_mestre_ia,
                                isSelected = mestreIAAberto,
                                isPraCegoVariant = isPraCegoVariant,
                                estadoVoz = estadoVozEfetivo,
                                onClick = onMestreIAClick,
                                onLongClick = onMestreIALongPress
                            )
                        }

                        tabs.forEachIndexed { index, title ->
                            val iconRes = when (title) {
                                "Geral" -> R.drawable.tab_geral
                                "Traços" -> R.drawable.tab_tracos
                                "Perícias" -> R.drawable.tab_pericias
                                "Técnicas" -> R.drawable.tab_tecnicas
                                "Magias" -> R.drawable.tab_magia
                                "Equipamento" -> R.drawable.tab_equipamentos
                                "Rolagem" -> R.drawable.tab_rolagem
                                "Mesa" -> R.drawable.tab_mesa
                                "Saga" -> R.drawable.tab_mestre_ia
                                else -> R.drawable.tab_geral
                            }
                            // 🟥 A caixa tem largura FIXA, e é ela que segura a
                            // fila no lugar. O ícone escolhido cresce por dentro
                            // dela — ver o `RPGNavigationItem`.
                            Box(modifier = Modifier.width(larguraDaAba)) {
                                RPGNavigationItem(
                                    label = title,
                                    iconRes = iconRes,
                                    isSelected = index == currentIndex,
                                    isPraCegoVariant = isPraCegoVariant,
                                    onClick = { onTabClick(index) }
                                )
                            }
                        }
                    }

                    // 🔴 A folga por onde o ícone cresce.
                    //
                    // ⚠️ O `graphicsLayer` pinta 1,3× SEM pedir espaço a ninguém —
                    // e é por isso que a fila não se mexe. Mas ele pinta por cima
                    // do que estiver colado: sem estes 5dp, o ícone escolhido
                    // encostaria no trilho do cometa.
                    Spacer(modifier = Modifier.height(5.dp))

                    OTrilhoDoCometa(
                        quantosLugares = quantosLugares,
                        // 🔴 `+1`: o lugar zero é o do Mestre IA, e ele não é uma
                        // aba. Sem isto o cometa pararia sempre um lugar atrás.
                        lugarAceso = currentIndex + 1,
                        larguraDaAba = larguraDaAba,
                        aceso = !isPraCegoVariant
                    )
                }
            }
        }
    }
}

/** A linha do nome, reservada mesmo quando ele não está à vista. */
private val ALTURA_DO_NOME = 20.dp

/** O ícone em si. Era 31dp, e o escolhido ia a 62dp — ver o `RPGNavigationItem`. */
private val TAMANHO_DO_ICONE = 30.dp

/** O quanto o escolhido cresce. Escolha dele: 1,3× — nota-se sem dominar. */
private const val CRESCIMENTO_DO_ESCOLHIDO = 1.3f

/** O maior que uma aba fica quando sobra espaço. */
private val LARGURA_IDEAL_DA_ABA = 42.dp

/**
 * **O cometa** — lote BARRA-1, ideia dele.
 *
 * > *"talvez, esse pulinho nao esteja legal, talvez algo fixo, com um comet e um
 * > pulsar"*
 *
 * Um risco de luz corre pelo trilho até parar debaixo da aba escolhida. Ele diz
 * duas coisas ao mesmo tempo: **onde você está** e **de onde veio** — e é a
 * segunda que o pulinho nunca disse.
 *
 * 🔴 O trilho e as abas usam a **mesma** `larguraDaAba`, e é isso que os mantém
 * alinhados. Uma segunda conta aqui dentro seria uma conta para divergir no dia
 * em que a largura mudasse.
 *
 * ⚠️ Fora do `pracego`: um risco de luz não diz nada a quem não vê a tela, e o
 * anel do microfone continua a ser o único efeito que **avisa** alguma coisa.
 */
@Composable
private fun OTrilhoDoCometa(
    quantosLugares: Int,
    lugarAceso: Int,
    larguraDaAba: Dp,
    aceso: Boolean
) {
    if (quantosLugares <= 0) return

    val cor = MaterialTheme.colorScheme.primary
    // ⚠️ A viagem é `spring`, e não `tween`: um cometa que chega e para seco
    // parece um corte de vídeo. A mola encosta e assenta.
    val ondeEle by animateDpAsState(
        targetValue = larguraDaAba * lugarAceso.coerceIn(0, quantosLugares - 1),
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
        label = "Cometa"
    )

    Box(
        modifier = Modifier
            .width(larguraDaAba * quantosLugares)
            .height(3.dp)
    ) {
        // O trilho, apagado: só o suficiente para o cometa ter por onde correr.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.Center)
                .drawBehind { drawRect(cor.copy(alpha = 0.12f)) }
        )
        if (aceso) {
            Box(
                modifier = Modifier
                    .offset(x = ondeEle)
                    .width(larguraDaAba)
                    .height(3.dp)
                    .drawBehind {
                        // 🔴 Aceso no meio e apagado nas pontas: é o rastro. Um
                        // retângulo de cor sólida seria um traço, e não um cometa.
                        drawRect(
                            brush = Brush.horizontalGradient(
                                0f to Color.Transparent,
                                0.5f to cor,
                                1f to Color.Transparent
                            )
                        )
                    }
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun RPGNavigationItem(
    label: String,
    iconRes: Int,
    isSelected: Boolean,
    isPraCegoVariant: Boolean,
    estadoVoz: EstadoVoz = EstadoVoz.OCIOSO,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    /**
     * 🟥 **O escolhido cresce por DENTRO da caixa dele.**
     *
     * Aqui estava o defeito que ele viu no vídeo. O ícone escolhido ia de 31dp a
     * **62dp de layout** — ele passava a ocupar o dobro do espaço e **empurrava
     * os vizinhos**. A fila inteira deslizava a cada toque, e é isso que se sente
     * como *"muito desalinhado"*.
     *
     * 🔴 Agora a caixa **nunca muda de tamanho**: quem cresce é o desenho, pelo
     * `graphicsLayer`, que pinta maior sem pedir espaço a ninguém. Nada se move,
     * em nenhuma troca.
     *
     * ⚠️ E 1,3× em vez de 2× — escolha dele. O dobro não era só feio: era o que
     * mandava na altura da barra inteira.
     */
    val escala by animateFloatAsState(
        targetValue = if (isSelected) CRESCIMENTO_DO_ESCOLHIDO else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow),
        label = "Escala"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "RPGAction")

    /**
     * **O brilho pulsa em CLARIDADE, e não em tamanho** — achado dele:
     *
     * > *"o brilho esta passando pra fora do icone, tem como limitar ate o
     * > tamanho do icone?"*
     *
     * 🔴 Tem, e a causa era essa: ele era desenhado com `scale(1.4)`, ou seja
     * **nascia maior que a caixa** e vazava por fora. Agora o raio é o da própria
     * caixa e o que varia é o quanto ele acende.
     */
    val brilho by infiniteTransition.animateFloat(
        initialValue = 0.18f,
        targetValue = if (isSelected) 0.55f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Brilho"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val escutando = estadoVoz == EstadoVoz.ESCUTANDO || estadoVoz == EstadoVoz.PROCESSANDO

    // Anel pulsante durante escuta de voz. ⚠️ Este FICA: é o único efeito da
    // barra que **avisa** alguma coisa, em vez de só enfeitar.
    val vozRingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (escutando) 1.6f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "VozRing"
    )
    val vozRingAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (escutando) 0.6f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "VozAlpha"
    )
    val vozColor = if (estadoVoz == EstadoVoz.PROCESSANDO) Color(0xFFFFA000) else Color(0xFF4CAF50)

    val clickModifier = if (onLongClick != null) {
        Modifier.pointerInput(onClick, onLongClick) {
            detectTapGestures(
                onTap = { onClick() },
                onLongPress = { onLongClick() }
            )
        }
    } else {
        Modifier.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = when {
                    escutando -> "$label (Escutando voz)"
                    isSelected -> "$label (Aba Selecionada)"
                    else -> "Aba $label"
                }
            }
            .then(clickModifier),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier.size(TAMANHO_DO_ICONE),
            contentAlignment = Alignment.Center
        ) {
            // Anel verde/amarelo pulsante ao escutar
            if (escutando) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = vozRingScale
                            scaleY = vozRingScale
                            alpha = vozRingAlpha
                        }
                        .drawBehind { drawCircle(color = vozColor) }
                )
            }

            // O brilho, PRESO ao tamanho do ícone.
            if (isSelected && !isPraCegoVariant) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = brilho),
                                        Color.Transparent
                                    ),
                                    // ⚠️ O raio é o da caixa. É esta linha que o
                                    // impede de vazar para cima dos vizinhos.
                                    radius = size.minDimension / 2f
                                ),
                                radius = size.minDimension / 2f
                            )
                        }
                )
            }

            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = escala
                        scaleY = escala
                    }
            )
        }
    }
}

/**
 * O nome da aba escolhida.
 *
 * ⚠️ Sem animação própria: quem faz o aparecer e o sumir é a **linha do nome**,
 * lá em cima. Duas animações sobre a mesma coisa deixaria um `fade` dentro
 * de outro, e nenhum dos dois com o tempo que se pediu.
 */
@Composable
private fun TabNameLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge.copy(
            // ⚠️ Menor do que era (16sp): o nome deixou de disputar espaço com os
            // ícones, e passou a ter uma linha só para ele.
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 3.sp,
            shadow = androidx.compose.ui.graphics.Shadow(
                color = Color.Black.copy(alpha = 0.5f),
                offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                blurRadius = 4f
            )
        ),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        softWrap = false
    )
}
