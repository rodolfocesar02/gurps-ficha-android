package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.ui.appCardColors

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
                onExecutarRolagem = onRolarAtributo
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
