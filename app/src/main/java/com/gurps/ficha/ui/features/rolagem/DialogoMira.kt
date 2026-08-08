package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.GolpeRapidoEAparaRules
import com.gurps.ficha.domain.rules.MiraRules
import com.gurps.ficha.ui.FullscreenDialogContainer
import com.gurps.ficha.ui.UiActionLabels
import com.gurps.ficha.ui.appCardColors
import com.gurps.ficha.ui.linhaAlternavel
import com.gurps.ficha.domain.rules.TabelaVelocidadeDistancia
import com.gurps.ficha.domain.rules.AlcanceDoAtaque
import com.gurps.ficha.domain.rules.DisopiaRules
import com.gurps.ficha.domain.rules.ApontarRules
import com.gurps.ficha.domain.rules.AvancarEAtacarRules
import com.gurps.ficha.domain.rules.AtiradorRules
import com.gurps.ficha.domain.rules.TamanhoDoAlvoRules
import com.gurps.ficha.domain.rules.ModificadoresDeCombate
import com.gurps.ficha.domain.rules.PacifismoRules
import com.gurps.ficha.domain.rules.ZarolhoRules
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.foundation.layout.PaddingValues

/**
 * **Onde acertar** — o diálogo de mira (Lote MIRA-1, MB p.398-400).
 *
 * Ideia do usuário: tocar no NH do ataque abre esta lista, e cada linha mostra
 * **o número já reduzido**. Se a Faca é NH 12 e você quer o olho (−9), a linha
 * do olho mostra **3**. Sem penalidade entre parênteses, sem conta mental no
 * meio da mesa.
 *
 * Tocou, o diálogo fecha e a rolagem sai — um gesto só.
 *
 * **Não calcula dano de propósito.** O dano localizado depende da RD do
 * oponente, que a ficha não tem e nunca terá: é informação do Mestre. Mostrar um
 * número ali seria inventar.
 *
 * O `detalhe` de cada linha existe porque a escolha real é pelo **efeito**, não
 * pelo número: mirar no crânio vale a pena por causa do ×4 de ferimento, não
 * apesar do −7.
 */
@Composable
fun DialogoMira(
    rotuloDoAtaque: String,
    nhBase: Int,
    isPraCegoVariant: Boolean,
    onEscolher: (rotulo: String, nh: Int) -> Unit,
    onDismiss: () -> Unit,
    // --- Lote MIRA-2: só chegam preenchidos em ataque à distância. ---
    ehADistancia: Boolean = false,
    alcance: AlcanceDoAtaque.Alcance = AlcanceDoAtaque.Alcance(null, null),
    // Lote MESTRE-1: Golpe Rapido e opcao de corpo a corpo (MB p.371).
    personagem: com.gurps.ficha.model.Personagem? = null,
    // O seletor anda de DEGRAU da tabela, não de metro em metro: cada toque
    // vale exatamente −1. Ver `TabelaVelocidadeDistancia`.
    //
    // ⚠️ O estado mora na ABA, não aqui. Se morasse aqui, ele sumiria ao fechar
    // o diálogo e o toque simples no NH voltaria a rolar sem a distância — sem
    // avisar ninguém. Erro silencioso é o pior tipo.
    indiceDistancia: Int = TabelaVelocidadeDistancia.INDICE_PADRAO,
    indiceVelocidade: Int = -1,
    onIndices: (distancia: Int, velocidade: Int) -> Unit = { _, _ -> },
    // Lote ARMA-5: a frase de divergência entre a perícia do ataque e a arma
    // escolhida na fonte de dano. Nula quando estão coerentes, que é o normal.
    conflitoArmaPericia: String? = null,
    // Lote ARMA-8/9: a perícia do ataque decide se Atirador ou Arqueiro Heroico
    // valem aqui — é assim que o livro escreve ("qualquer arma que utilize as
    // perícias…"), e não pelo tipo da arma.
    periciaDoAtaque: String? = null
) {
    var desarmar by remember { mutableStateOf(false) }
    var golpeRapido by remember { mutableStateOf(false) }
    // Lote MIRA-4: o Apontar deixou de ser liga/desliga. O livro (MB p.364)
    // deixa acumular segundos: +1 com dois, +2 com tres ou mais.
    var turnosApontando by remember { mutableIntStateOf(0) }
    var armaFirmada by remember { mutableStateOf(false) }
    // Lote ARMA-5: a mira embutida da arma ("Prec 6+1"). Começa MARCADA porque
    // quem tem luneta no rifle está usando a luneta — desmarcar é a exceção.
    var usandoMiraAcoplada by remember { mutableStateOf(true) }
    // Lote ARMA-7: atacar em movimento (MB p.366).
    var avancarEAtacar by remember { mutableStateOf(false) }
    // Lote MB-3: o Modificador de Tamanho do ALVO — o passo 2 do livro (p.549),
    // que o app pulava. Começa no humano.
    var indiceTamanhoAlvo by remember { mutableIntStateOf(TamanhoDoAlvoRules.INDICE_PADRAO) }
    // Lotes MB-1 e MB-4: os modificadores condicionais do livro (p.547-549).
    // Mapa id -> quantidade; zero (ou ausente) quer dizer desmarcado.
    var modsDeCombate by remember { mutableStateOf(mapOf<String, Int>()) }
    var miope by remember { mutableStateOf(false) }
    // Lote D-MIRA: o app não sabe se o alvo é uma pessoa nem se o rosto está à
    // mostra — nenhuma das quatro isenções do livro está na ficha. Pergunta.
    var ataqueLetal by remember { mutableStateOf(false) }
    var veORosto by remember { mutableStateOf(true) }
    val opcoes = MiraRules.opcoes(desarmar)

    // Golpe Rapido nao existe em ataque a distancia -- e opcao de corpo a corpo.
    val penalidadeGolpeRapido = if (golpeRapido && !ehADistancia && personagem != null) {
        GolpeRapidoEAparaRules.penalidadeGolpeRapido(personagem)
    } else 0

    val metros = TabelaVelocidadeDistancia.degrau(indiceDistancia).metros
    val velocidade = if (indiceVelocidade < 0) 0 else
        TabelaVelocidadeDistancia.degrau(indiceVelocidade).metros
    val penalidadeDistancia = if (ehADistancia) {
        TabelaVelocidadeDistancia.penalidadeCombinada(metros, velocidade, miope)
    } else {
        0
    }
    // O Assassino Relutante NÃO PODE Apontar (MB p.153). A trava vem antes do
    // bônus: deixar a caixinha marcada e ignorar o efeito faria o número mudar
    // e não mudar, que é pior que esconder.
    val apontarBloqueado = personagem != null &&
        PacifismoRules.bloqueiaApontar(personagem, ataqueLetal)
    // Bloqueado zera os turnos para efeito de conta -- o Assassino Relutante
    // nao pode Apontar, entao segundo acumulado nao vale nada.
    // ⚠️ Lote ARMA-7: Apontar e Avançar e Atacar se EXCLUEM. Não existe acumular
    // segundos de pontaria enquanto se corre — deixar os dois marcados somaria um
    // bônus que a regra não dá.
    val turnosValendo = if (apontarBloqueado || avancarEAtacar) 0 else turnosApontando
    val apontouValendo = turnosValendo > 0

    // Lote ARMA-8/9: qual vantagem cinematográfica vale NESTE ataque.
    val estiloDeTiro = AtiradorRules.estiloDe(personagem, periciaDoAtaque)

    val penalidadeAvancar = if (!avancarEAtacar) {
        0
    } else if (AtiradorRules.ignoraAvancarEAtacar(estiloDeTiro)) {
        // MB p.43/45: a vantagem apaga a penalidade da manobra — em troca da
        // Precisão de graça, que o `bonusNoAtaque` zera logo abaixo.
        0
    } else if (ehADistancia) {
        AvancarEAtacarRules.penalidadeADistancia(alcance.magnitude)
    } else {
        AvancarEAtacarRules.penalidadeCorpoACorpo(nhBase)
    }

    // O bônus da mira só existe se a arma tiver mira E o jogador estiver usando.
    val bonusDaMira = if (usandoMiraAcoplada) (alcance.precisaoAcessorio ?: 0) else 0

    // Apontar traz duas coisas de uma vez: a Precisão da arma e o dobro do
    // desconto da Visão Telescópica. Ver `ApontarRules`.
    val bonusApontar = if (ehADistancia && personagem != null) {
        ApontarRules.bonusTotalDoApontar(
            personagem, alcance.precisao, penalidadeDistancia,
            turnosValendo, armaFirmada, bonusDaMira
        )
    } else 0

    // 🔴 Lote ARMA-8/9: a Precisão que entra SEM Apontar. Zera sozinha quando o
    // jogador está apontando (senão a Prec contaria duas vezes) e quando ele
    // marca Avançar e Atacar (o livro troca uma coisa pela outra).
    val bonusDoAtirador = AtiradorRules.bonusNoAtaque(
        estilo = estiloDeTiro,
        precisao = alcance.precisao,
        duasMaos = alcance.duasMaos,
        cadenciaTiro = alcance.cadenciaTiro,
        avancarEAtacar = avancarEAtacar,
        apontou = apontouValendo
    )
    // 🔴 O Arqueiro Heroico acumula os segundos UM TURNO MAIS CEDO (MB p.45).
    // A diferença entra como acréscimo sobre o que o `ApontarRules` já somou.
    val extraDoArqueiro = if (apontouValendo) {
        AtiradorRules.bonusPorTurnos(estiloDeTiro, turnosValendo) -
            ApontarRules.bonusPorTurnos(turnosValendo)
    } else 0
    // ⚠️ O Zarolho lê `apontouValendo`, não `apontou`: quem não pode Apontar
    // também não pode usar o Apontar para cancelar o −3.
    val penalidadeZarolho = if (personagem != null) {
        ZarolhoRules.penalidadeNoAtaque(personagem, ehADistancia, apontouValendo)
    } else 0
    val penalidadePacifismo = if (personagem != null) {
        PacifismoRules.penalidade(personagem, ataqueLetal, veORosto)
    } else 0
    // Lote MB-3: o MT do alvo só existe em ataque à distância — o passo 2 do
    // livro está na lista do tiro, não na do corpo a corpo.
    val degrauTamanho = TamanhoDoAlvoRules.degrau(indiceTamanhoAlvo)
    val modificadorTamanho = if (ehADistancia) {
        TamanhoDoAlvoRules.modificadorNoAtaque(degrauTamanho.mt)
    } else 0

    // Lotes MB-1 e MB-4. ⚠️ A ordem importa e é a do livro: soma tudo, DEPOIS
    // trava a visibilidade, DEPOIS aplica o teto de 9 do asterisco. A penalidade
    // de luz já aplicada entra na conta do limite de −10 — senão o app
    // permitiria −10 de escuridão MAIS −10 de fumaça.
    val listaDeMods = (
        if (ehADistancia) ModificadoresDeCombate.A_DISTANCIA
        else ModificadoresDeCombate.CORPO_A_CORPO
        ).mapNotNull { m ->
        modsDeCombate[m.id]?.takeIf { it > 0 }?.let { ModificadoresDeCombate.Escolha(m, it) }
    }

    val nhAntesDosMods = nhBase + penalidadeDistancia + penalidadeGolpeRapido +
        bonusApontar + penalidadeZarolho + penalidadePacifismo + penalidadeAvancar +
        bonusDoAtirador + extraDoArqueiro + modificadorTamanho

    val resultadoDosMods = ModificadoresDeCombate.aplicar(
        nhBase = nhAntesDosMods,
        escolhas = listaDeMods,
        penalidadeDeVisibilidadeJaAplicada = 0
    )
    val nhComDistancia = resultadoDosMods.nhFinal

    FullscreenDialogContainer(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                "Onde acertar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "$rotuloDoAtaque — NH $nhBase no torso",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )

            if (ehADistancia) {
                LinhaDeDistancia(
                    metros = metros,
                    velocidade = velocidade,
                    velocidadeVisivel = indiceVelocidade >= 0,
                    penalidade = penalidadeDistancia,
                    alcance = alcance,
                    onDistancia = { delta ->
                        onIndices(
                            (indiceDistancia + delta)
                                .coerceIn(0, TabelaVelocidadeDistancia.DEGRAUS.lastIndex),
                            indiceVelocidade
                        )
                    },
                    onVelocidade = { delta ->
                        onIndices(
                            indiceDistancia,
                            (indiceVelocidade + delta)
                                .coerceIn(-1, TabelaVelocidadeDistancia.DEGRAUS.lastIndex)
                        )
                    },
                    onMostrarVelocidade = { onIndices(indiceDistancia, 0) }
                )

                // Lote MB-3: o tamanho do alvo, logo abaixo da distância — é a
                // ordem do livro (passo 2 antes do passo 3) e a ordem em que o
                // jogador pensa: "atirei no quê, a que distância".
                LinhaDeTamanhoDoAlvo(
                    degrau = degrauTamanho,
                    onPasso = { delta ->
                        indiceTamanhoAlvo = (indiceTamanhoAlvo + delta)
                            .coerceIn(0, TamanhoDoAlvoRules.DEGRAUS.lastIndex)
                    }
                )
            }

            // Disopia tem DUAS variantes pelo mesmo custo, e a ficha nao guarda
            // qual delas e -- entao o app oferece em vez de adivinhar.
            if (ehADistancia && personagem != null && DisopiaRules.tem(personagem)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .linhaAlternavel(
                            marcado = miope,
                            descricao = DisopiaRules.ROTULO_ACESSIVEL_MIOPE,
                            onAlternar = { miope = !miope }
                        )
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = miope, onCheckedChange = null)
                    Text(
                        DisopiaRules.ROTULO_MIOPE,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }

            // Assassino Relutante (MB p.153). Aparece em ataque de qualquer
            // alcance: o livro fala de "ataque letal", não de arma de fogo.
            if (personagem != null && PacifismoRules.ehAssassinoRelutante(personagem)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .linhaAlternavel(
                            marcado = ataqueLetal,
                            descricao = PacifismoRules.ROTULO_ACESSIVEL_LETAL,
                            onAlternar = { ataqueLetal = !ataqueLetal }
                        )
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = ataqueLetal, onCheckedChange = null)
                    Text(
                        PacifismoRules.rotulo(personagem, ataqueLetal, veORosto),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (ataqueLetal) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
                // A segunda pergunta só faz sentido depois da primeira: é ela
                // que decide entre −4 e −2.
                if (ataqueLetal) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .linhaAlternavel(
                                marcado = veORosto,
                                descricao = PacifismoRules.ROTULO_ACESSIVEL_ROSTO,
                                onAlternar = { veORosto = !veORosto }
                            )
                            .padding(start = 16.dp, top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = veORosto, onCheckedChange = null)
                        Text(
                            PacifismoRules.ROTULO_VE_O_ROSTO,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }

            if (ehADistancia && personagem != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .linhaAlternavel(
                            marcado = apontouValendo,
                            descricao = if (apontarBloqueado) {
                                "Apontar indisponível: o Assassino Relutante não " +
                                    "pode Apontar num ataque letal."
                            } else {
                                ApontarRules.rotuloAcessivelApontar(
                                    personagem, alcance.precisao, penalidadeDistancia,
                                    turnosValendo
                                )
                            },
                            // ⚠️ Não é mais liga/desliga: o toque ACUMULA segundos
                            // e cicla 0 → 1 → 2 → 3 → 0 (MB p.364).
                            onAlternar = {
                                if (!apontarBloqueado) {
                                    turnosApontando = ApontarRules.proximoTurno(turnosApontando)
                                }
                            }
                        )
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = apontouValendo,
                        onCheckedChange = null,
                        enabled = !apontarBloqueado
                    )
                    Text(
                        if (apontarBloqueado) {
                            "Apontar bloqueado — o Assassino Relutante não pode " +
                                "Apontar num ataque letal (MB p.153)"
                        } else {
                            ApontarRules.rotuloApontar(
                                personagem, alcance.precisao, penalidadeDistancia,
                                turnosValendo, armaFirmada, bonusDaMira
                            )
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }

                // A arma firmada só faz sentido depois de começar a apontar —
                // o +1 do livro é "adicional na Prec", e a Prec só vale
                // apontando.
                if (apontouValendo) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .linhaAlternavel(
                                marcado = armaFirmada,
                                descricao = ApontarRules.ROTULO_ACESSIVEL_FIRMADA,
                                onAlternar = { armaFirmada = !armaFirmada }
                            )
                            .padding(start = 16.dp, top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = armaFirmada, onCheckedChange = null)
                        Text(
                            ApontarRules.ROTULO_ARMA_FIRMADA,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }

                // 🔴 Lote ARMA-5: a mira acoplada. Só aparece quando ESTA arma
                // tem mira embutida no catálogo ("Prec 6+1") — as 12 armas de
                // fogo cujo "+N" o app descartava. Como o bônus é "adicional na
                // Prec" (MB p.270), a caixinha segue a mesma regra da arma
                // firmada: só faz sentido depois de começar a apontar.
                val bonusDisponivel = alcance.precisaoAcessorio ?: 0
                if (apontouValendo && bonusDisponivel > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .linhaAlternavel(
                                marcado = usandoMiraAcoplada,
                                descricao = ApontarRules.rotuloAcessivelMiraAcoplada(bonusDisponivel),
                                onAlternar = { usandoMiraAcoplada = !usandoMiraAcoplada }
                            )
                            .padding(start = 16.dp, top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = usandoMiraAcoplada, onCheckedChange = null)
                        Text(
                            ApontarRules.rotuloMiraAcoplada(bonusDisponivel),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }

            // Lote ARMA-8/9: a vantagem cinematográfica em ação. Não tem
            // caixinha — não há o que escolher: ou o personagem tem, ou não tem.
            AtiradorRules.rotulo(
                estiloDeTiro, alcance.precisao, alcance.duasMaos, alcance.cadenciaTiro,
                avancarEAtacar, apontouValendo
            )?.let { texto ->
                Text(
                    texto,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Lote ARMA-7: atacar em movimento. Fica **abaixo** do Apontar porque
            // é a alternativa a ele — e marcá-la desliga o Apontar, que é o que a
            // regra manda.
            if (personagem != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .linhaAlternavel(
                            marcado = avancarEAtacar,
                            descricao = AvancarEAtacarRules.rotuloAcessivel(
                                ehADistancia, alcance.magnitude, nhBase
                            ),
                            onAlternar = { avancarEAtacar = !avancarEAtacar }
                        )
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = avancarEAtacar, onCheckedChange = null)
                    Text(
                        AvancarEAtacarRules.rotulo(ehADistancia, alcance.magnitude, nhBase),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (avancarEAtacar) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
                // O jogador precisa saber POR QUE o Apontar sumiu do total.
                if (avancarEAtacar && turnosApontando > 0) {
                    Text(
                        AvancarEAtacarRules.AVISO_EXCLUSIVO,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }

            // Lotes MB-1 e MB-4: os modificadores condicionais.
            PainelModificadoresDeCombate(
                ehADistancia = ehADistancia,
                escolhas = modsDeCombate,
                onAlternar = { m ->
                    modsDeCombate = modsDeCombate.toMutableMap().apply {
                        if ((this[m.id] ?: 0) > 0) remove(m.id) else put(m.id, 1)
                    }
                },
                onQuantidade = { m, q ->
                    modsDeCombate = modsDeCombate.toMutableMap().apply { put(m.id, q) }
                }
            )

            // ⚠️ O teto de 9 avisa quando corta. Um NH que para de cair sem
            // explicação parece defeito — foi a mesma decisão do Apontar.
            ModificadoresDeCombate.avisoDoTeto(resultadoDosMods)?.let { aviso ->
                Text(
                    aviso,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // 🔴 Lote ARMA-5: perícia e arma discordando. O app segue a perícia
            // — que é o ataque que o jogador tocou — mas DIZ que seguiu. Antes
            // ele escolhia calado e o jogador perdia distância, 1/2D, Máx e o
            // Apontar inteiro sem nenhum aviso.
            conflitoArmaPericia?.let { aviso ->
                Text(
                    aviso,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Zarolho (MB p.163). Não tem caixinha: a penalidade não depende de
            // escolha nenhuma do jogador — só de já ter marcado o Apontar.
            if (personagem != null && ZarolhoRules.tem(personagem)) {
                Text(
                    ZarolhoRules.rotulo(personagem, ehADistancia, apontouValendo),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MiraRules.Grupo.values().forEach { grupo ->
                    val doGrupo = opcoes.filter { it.grupo == grupo }
                    if (doGrupo.isEmpty()) return@forEach

                    item {
                        Text(
                            grupo.rotulo,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Golpe Rápido é opção de ataque corpo a corpo: dois
                    // ataques no turno, os dois penalizados (MB p.371).
                    if (grupo == MiraRules.Grupo.CORPO && !ehADistancia && personagem != null) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .linhaAlternavel(
                                        marcado = golpeRapido,
                                        descricao = GolpeRapidoEAparaRules
                                            .rotuloAcessivelGolpeRapido(personagem),
                                        onAlternar = { golpeRapido = !golpeRapido }
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(checked = golpeRapido, onCheckedChange = null)
                                Text(
                                    GolpeRapidoEAparaRules.rotuloGolpeRapido(personagem),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        }
                    }

                    // A opção de desarmar só faz sentido sobre a arma do
                    // oponente — no corpo não existe "desarmar".
                    if (grupo == MiraRules.Grupo.ARMA) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .linhaAlternavel(
                                        marcado = desarmar,
                                        descricao = "Golpear para desarmar em vez de quebrar. " +
                                            "Penalidade adicional de menos 2.",
                                        onAlternar = { desarmar = !desarmar }
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(checked = desarmar, onCheckedChange = null)
                                Text(
                                    "Desarmar em vez de quebrar (−2)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        }
                    }

                    items(doGrupo) { opcao ->
                        LinhaDeMira(opcao, nhComDistancia, isPraCegoVariant) {
                            // O rótulo leva a distância junto: sem isso o log do
                            // Discord diria "Crânio 5" e ninguém saberia de onde
                            // saiu o 5.
                            val onde = if (ehADistancia && penalidadeDistancia != 0) {
                                "$rotuloDoAtaque — ${opcao.rotulo} a ${metros}m"
                            } else {
                                "$rotuloDoAtaque — ${opcao.rotulo}"
                            }
                            val ondeEQuando =
                                if (penalidadeGolpeRapido != 0) "$onde (Golpe Rápido)" else onde
                            onEscolher(ondeEQuando, opcao.nhCom(nhComDistancia))
                        }
                    }
                }

                item {
                    Text(
                        "O dano não entra aqui: ele depende da RD do oponente, que " +
                            "só o Mestre tem.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text(UiActionLabels.FECHAR) }
            }
        }
    }
}

/**
 * A linha de **distância do alvo** (Lote MIRA-2, MB p.550-551).
 *
 * ## Por que o `−/+` anda de degrau, e não de metro
 *
 * A tabela do livro é logarítmica. Se cada toque valesse 1 metro, chegar a 100
 * metros seriam 98 toques. Andando pela tabela — 2, 3, 5, 7, 10, 15, 20… — são
 * 10 toques, e **cada toque vale exatamente −1**. O botão deixa de ser um
 * contador de metros e passa a ser a própria regra.
 *
 * ## Por que a velocidade começa escondida
 *
 * O livro: *"Na maioria dos combates que envolve combatentes a pé e objetos
 * inanimados, é preferível ignorar a velocidade"*. Ela aparece só quando o
 * Mestre disser que o alvo está correndo.
 *
 * ⚠️ E quando aparece, ela **soma à distância** antes de consultar a tabela —
 * não é uma segunda penalidade. Por isso a explicação mostra a conta inteira.
 */
@Composable
private fun LinhaDeDistancia(
    metros: Int,
    velocidade: Int,
    velocidadeVisivel: Boolean,
    penalidade: Int,
    alcance: AlcanceDoAtaque.Alcance,
    onDistancia: (Int) -> Unit,
    onVelocidade: (Int) -> Unit,
    onMostrarVelocidade: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = appCardColors()
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Passo(
                rotulo = "Alvo a",
                valor = TabelaVelocidadeDistancia.degrau(
                    TabelaVelocidadeDistancia.indiceDoDegrau(metros)
                ).rotulo,
                descricaoAcessivel = "Distância do alvo: $metros metros",
                penalidade = if (velocidadeVisivel) null else penalidade,
                onPasso = onDistancia
            )

            if (velocidadeVisivel) {
                Passo(
                    rotulo = "Velocidade",
                    valor = "$velocidade m/s",
                    descricaoAcessivel = "Velocidade do alvo: $velocidade metros por segundo",
                    penalidade = penalidade,
                    onPasso = onVelocidade
                )
                // A conta inteira à vista: o jogador precisa poder desconfiar do
                // número. Mesma razão das notinhas de origem do Lote NOTA-1.
                Text(
                    TabelaVelocidadeDistancia.explicacao(metros, velocidade),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                TextButton(
                    onClick = onMostrarVelocidade,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text("+ alvo em movimento", style = MaterialTheme.typography.labelSmall)
                }
            }

            AvisoDeAlcance(metros, alcance)
        }
    }
}

/**
 * **O tamanho do alvo** (Lote MB-3, MB p.549).
 *
 * ⚠️ Mostra o **exemplo** junto do número, e não só o MT. "MT −2" não diz nada a
 * quem não decorou a tabela; "criança, cachorro médio" diz na hora — e é assim
 * que o Mestre descreve o alvo na mesa.
 */
@Composable
private fun LinhaDeTamanhoDoAlvo(
    degrau: TamanhoDoAlvoRules.Degrau,
    onPasso: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = appCardColors()
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Passo(
                rotulo = "Alvo",
                valor = degrau.exemplo,
                descricaoAcessivel = TamanhoDoAlvoRules.rotuloAcessivel(degrau.mt, degrau.exemplo),
                penalidade = degrau.mt.takeIf { it != 0 },
                onPasso = onPasso
            )
            Text(
                TamanhoDoAlvoRules.rotulo(degrau.mt, degrau.exemplo),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

/** Uma linha `[−] valor [+]` com a penalidade à direita. */
@Composable
private fun Passo(
    rotulo: String,
    valor: String,
    descricaoAcessivel: String,
    penalidade: Int?,
    onPasso: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            rotulo,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline
        )
        TextButton(
            onClick = { onPasso(-1) },
            modifier = Modifier.semantics { contentDescription = "Diminuir. $descricaoAcessivel" },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
        ) { Text("−", style = MaterialTheme.typography.titleMedium) }
        Text(
            valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { contentDescription = descricaoAcessivel }
        )
        TextButton(
            onClick = { onPasso(1) },
            modifier = Modifier.semantics { contentDescription = "Aumentar. $descricaoAcessivel" },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
        ) { Text("+", style = MaterialTheme.typography.titleMedium) }

        if (penalidade != null) {
            Text(
                if (penalidade == 0) "0" else "$penalidade",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (penalidade < 0) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = "Modificador de distância: " +
                            if (penalidade < 0) "menos ${-penalidade}" else "zero"
                    },
                textAlign = TextAlign.End
            )
        } else {
            Text("", modifier = Modifier.weight(1f))
        }
    }
}

/**
 * O alcance da arma: os limites sempre à vista, e o aviso quando passa deles.
 *
 * ## Por que os limites aparecem mesmo quando está tudo bem
 *
 * Achado de 29/07: o usuário testou a 50 metros e não viu aviso nenhum, e
 * concluiu que a automação não estava pegando. Estava — só que a arqueira tem
 * **ST 9** e um Arco Longo (`×15/×20`), então o 1/2D dela é **135 m**. Não havia
 * o que avisar.
 *
 * Silêncio é uma resposta ambígua: pode ser "está tudo certo" ou "não
 * funcionou". Mostrando **1/2D 135 m · Máx 180 m** o tempo todo, o jogador vê o
 * alvo se aproximando do limite e sabe que o app está olhando.
 *
 * ⚠️ E quando a arma não tem alcance cadastrado (ficha anterior ao Lote 371), a
 * linha **diz isso** em vez de não aparecer. A pior mensagem é nenhuma.
 *
 * O **1/2D** é o que mais escapa na mesa, porque não muda o ataque — muda o
 * **dano**, que sai pela metade. O jogador rola, acerta, e comemora um dano que
 * na verdade é metade daquilo.
 */
@Composable
private fun AvisoDeAlcance(metros: Int, alcance: AlcanceDoAtaque.Alcance) {
    val max = alcance.maximo
    val meio = alcance.meioDano

    if (max == null && meio == null) {
        Text(
            "Alcance desta arma não cadastrado — sem aviso de 1/2D nem de Máx.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
        return
    }

    when {
        max != null && metros > max -> Text(
            "Fora de alcance: o Máx da arma é $max m.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold
        )
        meio != null && metros > meio -> Text(
            "Além do 1/2D ($meio m): o dano sai pela metade." +
                (max?.let { " Máx $it m." } ?: ""),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Medium
        )
        // Dentro do alcance: os limites ficam à vista mesmo assim, para o
        // jogador ver de longe que o app está acompanhando.
        else -> Text(
            listOfNotNull(
                meio?.let { "1/2D $it m" },
                max?.let { "Máx $it m" }
            ).joinToString(" · "),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun LinhaDeMira(
    opcao: MiraRules.Opcao,
    nhBase: Int,
    isPraCegoVariant: Boolean,
    onEscolher: () -> Unit
) {
    val nh = opcao.nhCom(nhBase)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = appCardColors()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEscolher() }
                .semantics { contentDescription = opcao.descricaoAcessivel(nhBase) }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    opcao.rotulo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                opcao.detalhe?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Text(
                if (isPraCegoVariant) "Rolar ($nh)" else "$nh",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                // NH negativo é informação, não erro: mostra em vermelho para o
                // jogador ver na hora que aquele alvo está fora de alcance.
                color = if (nh < 3) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
