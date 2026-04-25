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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import android.content.Intent
import android.net.Uri
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    // Estado para rastrear a intenção atual e forçar re-processamento no Compose
    private val currentIntent = mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentIntent.value = intent

        setContent {
            GURPSFichaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: FichaViewModel = viewModel()
                    val intentToProcess by currentIntent
                    
                    // Trata intent recebido (ACTION_VIEW ou ACTION_SEND)
                    LaunchedEffect(intentToProcess) {
                        intentToProcess?.let {
                            tratarIntentRecebido(it, viewModel)
                            // Limpa a intenção após processar para evitar re-processamento em recomposições
                            currentIntent.value = null
                        }
                    }

                    AppUiEntry(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentIntent.value = intent
    }

    private fun tratarIntentRecebido(intent: Intent, viewModel: FichaViewModel) {
        val action = intent.action
        val type = intent.type
        val uri: Uri? = if (action == Intent.ACTION_VIEW) {
            intent.data
        } else if (action == Intent.ACTION_SEND && type != null) {
            IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            null
        }

        uri?.let {
            lifecycleScope.launch {
                val result = runCatching {
                    withContext(Dispatchers.IO) {
                        contentResolver.openInputStream(it)?.use { input ->
                            input.bufferedReader(Charsets.UTF_8).use { it.readText() }
                        }
                    }
                }
                
                result.onSuccess { json ->
                    if (!json.isNullOrBlank()) {
                        val msg = viewModel.importarFichaJson(json) ?: "Ficha importada com sucesso!"
                        val toastMsg = if (msg == "Sucesso") "Ficha importada com sucesso!" else msg
                        android.widget.Toast.makeText(this@MainActivity, toastMsg, android.widget.Toast.LENGTH_LONG).show()
                    }
                }.onFailure { error ->
                    android.widget.Toast.makeText(this@MainActivity, "Erro ao ler arquivo: ${error.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
