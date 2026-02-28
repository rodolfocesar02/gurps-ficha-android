package com.gurps.ficha

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gurps.ficha.ui.AppUiEntry
import com.gurps.ficha.ui.theme.GURPSFichaTheme
import com.gurps.ficha.viewmodel.FichaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GURPSFichaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: FichaViewModel = viewModel()
                    AppUiEntry(viewModel = viewModel)
                }
            }
        }
    }
}
