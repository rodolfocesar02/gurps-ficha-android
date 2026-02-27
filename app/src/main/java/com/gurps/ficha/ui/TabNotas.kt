package com.gurps.ficha.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gurps.ficha.viewmodel.FichaViewModel

@Composable
fun TabNotas(viewModel: FichaViewModel) {
    StandardTabColumn {
        SectionCard(title = "Notas (Reservada)") {
            Text(
                "A aba Notas está temporariamente limpa enquanto a jogabilidade é centralizada na aba Rolagem.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Os dados já existentes da ficha não foram removidos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

