package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gurps.ficha.domain.rules.DxBracalRules
import com.gurps.ficha.domain.rules.StBracalRules
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.ui.appCardColors
import com.gurps.ficha.ui.linhaAlternavel

/**
 * Cartão de Atributos + PV/PF do topo da aba Rolagem.
 *
 * Extraído no Lote D-1: a `TabRolagem` chegou a 1.004 linhas ao ganhar o painel
 * de autocontrole e voltou a estourar o teto de 1.000. Este era o candidato
 * mapeado no plano — é só um invólucro em volta de dois painéis que já viviam
 * em `ui/features/rolagem/`.
 *
 * A tela não muda: mesmo layout, mesmos gestos, mesma variante PraCego.
 */
@Composable
fun PainelAtributosEStatus(
    personagem: Personagem,
    atributosRapidos: List<String>,
    modificadoresAtributo: SnapshotStateMap<String, Int>,
    pvFixo: Int,
    pvAtual: Int,
    pfFixo: Int,
    pfAtual: Int,
    isPraCegoVariant: Boolean,
    cardTitleStyle: TextStyle,
    statsNumberStyle: TextStyle,
    defenseNumberStyle: TextStyle,
    compactLabelStyle: TextStyle,
    outerCardVerticalPadding: Dp,
    innerCardVerticalPadding: Dp,
    stBracalAtivo: Boolean,
    onAlternarStBracal: () -> Unit,
    dxBracalAtivo: Boolean,
    onAlternarDxBracal: () -> Unit,
    onRolarAtributo: (atributo: String, valor: Int, mod: Int) -> Unit,
    onEditPv: () -> Unit,
    onEditPf: () -> Unit,
    onAjustarPv: (Boolean) -> Unit,
    onAjustarPf: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = appCardColors()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = outerCardVerticalPadding),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (!isPraCegoVariant) {
                Text(
                    text = "Deslize para cima/baixo em cada atributo para ajustar o modificador.",
                    style = compactLabelStyle,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            AtributosQuickRollPanel(
                personagem = personagem,
                atributosRapidos = atributosRapidos,
                modificadoresAtributo = modificadoresAtributo,
                isPraCegoVariant = isPraCegoVariant,
                cardTitleStyle = cardTitleStyle,
                statsNumberStyle = statsNumberStyle,
                compactLabelStyle = compactLabelStyle,
                innerCardVerticalPadding = innerCardVerticalPadding,
                bonusStBracal = if (stBracalAtivo) StBracalRules.bonusDe(personagem) else 0,
                bonusDxBracal = if (dxBracalAtivo) DxBracalRules.bonusDe(personagem) else 0,
                onExecutarRolagem = onRolarAtributo
            )

            // Logo abaixo dos atributos, porque é deles que as Braçais falam.
            PainelStBracal(
                personagem = personagem,
                ativo = stBracalAtivo,
                onAlternar = onAlternarStBracal
            )

            PainelDxBracal(
                personagem = personagem,
                ativo = dxBracalAtivo,
                onAlternar = onAlternarDxBracal
            )

            PvPfQuickRollPanel(
                pvFixo = pvFixo,
                pvAtual = pvAtual,
                pfFixo = pfFixo,
                pfAtual = pfAtual,
                isPraCegoVariant = isPraCegoVariant,
                cardTitleStyle = cardTitleStyle,
                defenseNumberStyle = defenseNumberStyle,
                innerCardVerticalPadding = innerCardVerticalPadding,
                onEditPv = onEditPv,
                onEditPf = onEditPf,
                onAjustarPv = onAjustarPv,
                onAjustarPf = onAjustarPf
            )
        }
    }
}

/**
 * Seletor da **ST Braçal**, logo abaixo da linha de atributos.
 *
 * Por que é uma caixinha e não um número somado ao ST: o livro (MB p.89) diz
 * que a ST Braçal vale para erguer, arremessar e **atacar com os braços** — e
 * não vale para PV, Base de Carga nem esforço do corpo inteiro. Somar no ST
 * daria força de sobra para chutar e aumentaria os PV, que é justamente o que a
 * regra proíbe.
 *
 * Marcado, o ST rolado e o **Dano ST** passam a usar a ST dos braços. É o mesmo
 * gesto do bônus condicional de perícia: quem sabe se a ação é de braço é o
 * jogador, na hora.
 *
 * **Não renderiza nada** sem ST Braçal na ficha.
 */
@Composable
fun PainelStBracal(
    personagem: Personagem,
    ativo: Boolean,
    onAlternar: () -> Unit
) {
    if (!StBracalRules.temStBracal(personagem)) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 32.dp)
            // linhaAlternavel: UM ponto de parada no TalkBack, com rótulo E
            // estado. Ver o porquê em `UiA11y.kt`.
            .linhaAlternavel(
                marcado = ativo,
                descricao = StBracalRules.rotuloAcessivel(personagem),
                onAlternar = onAlternar
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = ativo, onCheckedChange = null)
        Text(
            StBracalRules.rotulo(personagem),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}

/**
 * Seletor da **DX Braçal**, irmão do de ST — com uma diferença que importa.
 *
 * O livro (MB p.56) diz que *"as perícias de combate dependem da DX corporal e
 * não se beneficiam de forma alguma da destreza braçal"*. Então, ao contrário
 * do ST Braçal — que faz a arma bater mais forte —, este seletor **não** mexe
 * em NH de ataque nenhum. Ele muda só o valor de DX rolado, para as tarefas de
 * mão que não são combate: Arrombamento, Prestidigitação, Cirurgia, Costura.
 *
 * O rótulo carrega esse aviso de propósito: é a pegadinha da vantagem, e quem
 * lê "+3 DX" no meio da mesa assume que o ataque melhorou.
 *
 * **Não renderiza nada** sem DX Braçal na ficha.
 */
@Composable
fun PainelDxBracal(
    personagem: Personagem,
    ativo: Boolean,
    onAlternar: () -> Unit
) {
    if (!DxBracalRules.temDxBracal(personagem)) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 32.dp)
            .linhaAlternavel(
                marcado = ativo,
                descricao = DxBracalRules.rotuloAcessivel(personagem),
                onAlternar = onAlternar
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = ativo, onCheckedChange = null)
        Text(
            DxBracalRules.rotulo(personagem),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}
