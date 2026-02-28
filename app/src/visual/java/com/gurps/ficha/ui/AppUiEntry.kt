package com.gurps.ficha.ui

import androidx.compose.runtime.Composable
import com.gurps.ficha.viewmodel.FichaViewModel

@Composable
fun AppUiEntry(viewModel: FichaViewModel) {
    FichaScreen(viewModel = viewModel)
}
