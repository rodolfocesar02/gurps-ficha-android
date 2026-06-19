package com.gurps.ficha.ui.features.dice3d

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gurps.ficha.ui.UiActionLabels
import com.gurps.ficha.ui.UiTokens

object DiceColorsStore {
    private const val PREFS_NAME = "DiceColorsPrefs"
    private const val KEY_BODY = "diceBodyColor"
    private const val KEY_NUMBER = "diceNumberColor"

    fun getColors(context: Context): Pair<Color, Color> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val defaultRed = android.graphics.Color.parseColor("#E52E2D")
        val bodyColorInt = prefs.getInt(KEY_BODY, defaultRed)
        val numColorInt = prefs.getInt(KEY_NUMBER, android.graphics.Color.WHITE)
        return Pair(Color(bodyColorInt), Color(numColorInt))
    }

    fun saveColors(context: Context, bodyColor: Color, numColor: Color) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_BODY, bodyColor.toArgb())
            .putInt(KEY_NUMBER, numColor.toArgb())
            .apply()
    }
}

@Composable
fun ConfigurarDadosDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var bodyColor by remember { mutableStateOf(Color.Red) }
    var numColor by remember { mutableStateOf(Color.White) }

    LaunchedEffect(Unit) {
        val (b, n) = DiceColorsStore.getColors(context)
        bodyColor = b
        numColor = n
    }

    val availableColors = listOf(
        Color(0xFFE52E2D), // Red
        Color(0xFF1E88E5), // Blue
        Color(0xFF43A047), // Green
        Color(0xFFFDD835), // Yellow
        Color(0xFF8E24AA), // Purple
        Color(0xFF000000), // Black
        Color(0xFFFFFFFF), // White
        Color(0xFF757575), // Gray
        Color(0xFFF4511E)  // Orange
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cores dos Dados 3D") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Cor do Plástico (Corpo):", style = MaterialTheme.typography.labelLarge)
                ColorPickerRow(
                    colors = availableColors,
                    selectedColor = bodyColor,
                    onColorSelected = { bodyColor = it }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Cor da Tinta (Números):", style = MaterialTheme.typography.labelLarge)
                ColorPickerRow(
                    colors = availableColors,
                    selectedColor = numColor,
                    onColorSelected = { numColor = it }
                )

                // Preview box
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(bodyColor, shape = RoundedCornerShape(8.dp))
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("3", color = numColor, style = MaterialTheme.typography.headlineLarge)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                DiceColorsStore.saveColors(context, bodyColor, numColor)
                onDismiss()
            }) {
                Text(UiActionLabels.SALVAR)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(UiActionLabels.CANCELAR)
            }
        }
    )
}

@Composable
private fun ColorPickerRow(colors: List<Color>, selectedColor: Color, onColorSelected: (Color) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        colors.forEach { color ->
            val isSelected = color == selectedColor
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onColorSelected(color) }
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                        shape = CircleShape
                    )
            )
        }
    }
}
