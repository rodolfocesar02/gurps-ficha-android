package com.gurps.ficha.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.gurps.ficha.viewmodel.FichaViewModel

@Composable
fun AppUiEntry(viewModel: FichaViewModel) {
    val density = LocalDensity.current
    val scaledDensity = Density(density = density.density, fontScale = (density.fontScale * 1.25f).coerceAtMost(2.0f))
    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        FichaScreen(viewModel = viewModel)
    }
}
