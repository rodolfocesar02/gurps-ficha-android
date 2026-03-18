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

import androidx.compose.runtime.LaunchedEffect
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

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
                    
                    // Trata intent recebido (ACTION_VIEW ou ACTION_SEND)
                    LaunchedEffect(intent) {
                        tratarIntentRecebido(intent, viewModel)
                    }

                    AppUiEntry(viewModel = viewModel)
                }
            }
        }
    }

    private fun tratarIntentRecebido(intent: Intent, viewModel: FichaViewModel) {
        val action = intent.action
        val type = intent.type
        val uri: Uri? = if (action == Intent.ACTION_VIEW) {
            intent.data
        } else if (action == Intent.ACTION_SEND && type != null) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        } else {
            null
        }

        uri?.let {
            runCatching {
                contentResolver.openInputStream(it)?.use { input ->
                    input.bufferedReader(Charsets.UTF_8).use { it.readText() }
                }
            }.onSuccess { json ->
                if (!json.isNullOrBlank()) {
                    val msg = viewModel.importarFichaJson(json) ?: "Ficha importada com sucesso!"
                    android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
