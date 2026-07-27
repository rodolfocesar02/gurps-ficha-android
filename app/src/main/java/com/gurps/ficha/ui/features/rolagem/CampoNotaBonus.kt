package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * Campo onde o jogador anota DE ONDE vem um bônus digitado à mão.
 *
 * Por que existe: os bônus manuais de Esquiva/Apara/Bloqueio são um número solto
 * — ninguém sabe se o +1 veio de um anel, de uma magia, de uma decisão do Mestre
 * ou de uma vantagem que o app ainda não calculava. Quando as vantagens forem
 * automatizadas, um bônus digitado para algo que o app passou a calcular vira
 * CONTAGEM DUPLA silenciosa. A nota é o que permite decidir o que fazer.
 *
 * Um componente só, usado pelos três diálogos de defesa
 * (`RolagemSecondaryDialogs.kt`), para o texto e o comportamento não divergirem
 * — e porque aquele arquivo já está perto do teto de 1.000 linhas.
 *
 * Apagar a nota NÃO mexe no bônus, e mudar o bônus não apaga a nota: são
 * informações independentes.
 */
@Composable
fun CampoNotaBonus(
    nota: String,
    onNotaChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = nota,
        onValueChange = { onNotaChange(it.take(MAX_NOTA)) },
        label = { Text("De onde vem este bônus? (opcional)") },
        placeholder = { Text("ex.: anel encantado, bênção do clérigo") },
        singleLine = false,
        maxLines = 2,
        modifier = modifier.fillMaxWidth(),
        trailingIcon = {
            if (nota.isNotBlank()) {
                IconButton(
                    onClick = { onNotaChange("") },
                    modifier = Modifier.semantics { contentDescription = "Apagar a nota do bônus" }
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }
        }
    )
}

/** Mesmo teto aplicado no `FichaCombatDelegate` ao gravar. */
private const val MAX_NOTA = 120
