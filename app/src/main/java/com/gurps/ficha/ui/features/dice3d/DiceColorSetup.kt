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
import androidx.compose.ui.unit.sp
import com.gurps.ficha.ui.UiActionLabels
import com.gurps.ficha.ui.UiTokens
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import io.github.sceneview.Scene
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberRenderer
import io.github.sceneview.rememberView
import io.github.sceneview.model.materialInstances
import com.google.android.filament.LightManager
import io.github.sceneview.node.LightNode
import io.github.sceneview.math.Rotation

object DiceColorsStore {
    private const val PREFS_NAME = "DiceColorsPrefs"
    private const val KEY_BODY = "diceBodyColor"
    private const val KEY_NUMBER = "diceNumberColor"
    private const val KEY_MATERIAL = "diceMaterial"

    data class DiceConfig(val bodyColor: Color, val numColor: Color, val materialType: String)

    fun getConfig(context: Context): DiceConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val defaultRed = android.graphics.Color.parseColor("#E52E2D")
        val bodyColorInt = prefs.getInt(KEY_BODY, defaultRed)
        val numColorInt = prefs.getInt(KEY_NUMBER, android.graphics.Color.WHITE)
        val mat = prefs.getString(KEY_MATERIAL, "plastic") ?: "plastic"
        return DiceConfig(Color(bodyColorInt), Color(numColorInt), mat)
    }

    fun saveConfig(context: Context, bodyColor: Color, numColor: Color, materialType: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_BODY, bodyColor.toArgb())
            .putInt(KEY_NUMBER, numColor.toArgb())
            .putString(KEY_MATERIAL, materialType)
            .apply()
    }

    // Keep old getColors for compatibility just in case
    fun getColors(context: Context): Pair<Color, Color> {
        val cfg = getConfig(context)
        return Pair(cfg.bodyColor, cfg.numColor)
    }
}

@Composable
fun ConfigurarDadosDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var bodyColor by remember { mutableStateOf(Color.Red) }
    var numColor by remember { mutableStateOf(Color.White) }
    var materialType by remember { mutableStateOf("plastic") }

    LaunchedEffect(Unit) {
        val config = DiceColorsStore.getConfig(context)
        bodyColor = config.bodyColor
        numColor = config.numColor
        materialType = config.materialType
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

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF2B2B36), Color(0xFF1E1E24))
                    )
                )
                .border(2.dp, Color(0xFF4A4A5A), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Título
                Text(
                    text = "Aparência dos Dados",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE0E0E0),
                        letterSpacing = 1.2.sp
                    )
                )
                
                Divider(color = Color(0xFF4A4A5A), thickness = 1.dp)

                // Material selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val mats = listOf("matte" to "Fosco", "plastic" to "Plástico", "metal" to "Metal")
                    mats.forEach { (key, label) ->
                        val isSelected = materialType == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF3A3A4A))
                                .clickable { materialType = key }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color(0xFFAAAAAA),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                // Plástico
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Material Base",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFAAAAAA),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ColorPickerGrid(
                        colors = availableColors,
                        selectedColor = bodyColor,
                        onColorSelected = { bodyColor = it }
                    )
                }

                // Números
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Tinta dos Números",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFAAAAAA),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ColorPickerGrid(
                        colors = availableColors,
                        selectedColor = numColor,
                        onColorSelected = { numColor = it }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Preview Estilizado com o Modelo 3D
                Dice3DPreview(
                    bodyColor = bodyColor,
                    numColor = numColor,
                    materialType = materialType,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF2B2B36),
                                    Color.Black.copy(alpha = 0.8f)
                                ),
                                radius = 200f
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                )

                // Botões de Ação
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(UiActionLabels.CANCELAR, color = Color(0xFFB0B0B0))
                    }
                    Button(
                        onClick = {
                            DiceColorsStore.saveConfig(context, bodyColor, numColor, materialType)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(UiActionLabels.SALVAR, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorPickerGrid(colors: List<Color>, selectedColor: Color, onColorSelected: (Color) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        colors.forEach { color ->
            val isSelected = color == selectedColor
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onColorSelected(color) }
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) Color.White else Color(0xFF222222),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.6f))
                    )
                }
            }
        }
    }
}

@Composable
fun Dice3DPreview(bodyColor: Color, numColor: Color, materialType: String, modifier: Modifier = Modifier) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val view = rememberView(engine)
    val renderer = rememberRenderer(engine)
    val cameraNode = rememberCameraNode(engine).apply {
        position = io.github.sceneview.math.Position(x = 0f, y = 0f, z = 4f)
    }

    val modelNodeState = remember { mutableStateOf<ModelNode?>(null) }

    LaunchedEffect(Unit) {
        var angle = 0f
        while (true) {
            androidx.compose.runtime.withFrameNanos { time ->
                angle += 1f
                modelNodeState.value?.rotation = Rotation(angle * 0.5f, angle, angle * 0.2f)
            }
        }
    }

    Scene(
        modifier = modifier,
        engine = engine,
        modelLoader = modelLoader,
        view = view,
        renderer = renderer,
        cameraNode = cameraNode,
        isOpaque = false
    ) {
        LightNode(
            type = LightManager.Type.SUN
        )

        val model = rememberModelInstance(modelLoader, "models/Dado.glb")
        if (model != null) {
            LaunchedEffect(model, bodyColor, numColor, materialType) {
                val linearBody = bodyColor.convert(ColorSpaces.LinearSrgb)
                val linearNum = numColor.convert(ColorSpaces.LinearSrgb)
                
                model.materialInstances?.forEach { materialInstance ->
                    if (materialInstance.name.startsWith("bod_red")) {
                        materialInstance.setParameter("baseColorFactor", linearBody.red, linearBody.green, linearBody.blue, linearBody.alpha)
                        when (materialType) {
                            "matte" -> {
                                materialInstance.setParameter("metallicFactor", 0.0f)
                                materialInstance.setParameter("roughnessFactor", 0.8f)
                            }
                            "metal" -> {
                                materialInstance.setParameter("metallicFactor", 1.0f)
                                materialInstance.setParameter("roughnessFactor", 0.15f)
                            }
                            else -> { // plastic
                                materialInstance.setParameter("metallicFactor", 0.1f)
                                materialInstance.setParameter("roughnessFactor", 0.3f)
                            }
                        }
                    }
                    if (materialInstance.name.startsWith("Numbers_Black")) {
                        materialInstance.setParameter("baseColorFactor", linearNum.red, linearNum.green, linearNum.blue, linearNum.alpha)
                    }
                }
            }
            ModelNode(
                modelInstance = model,
                scaleToUnits = 1.8f,
                apply = {
                    modelNodeState.value = this
                }
            )
        }
    }
}
