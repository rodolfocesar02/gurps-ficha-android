package com.gurps.ficha.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gurps.ficha.viewmodel.FichaViewModel

@Composable
fun TabNotas(viewModel: FichaViewModel) {
    val p = viewModel.personagem

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(title = "Aparência") {
            OutlinedTextField(value = p.aparencia, onValueChange = { viewModel.atualizarAparencia(it) },
                modifier = Modifier.fillMaxWidth().height(120.dp), placeholder = { Text("Descreva a aparência...") })
        }
        SectionCard(title = "Histórico") {
            OutlinedTextField(value = p.historico, onValueChange = { viewModel.atualizarHistorico(it) },
                modifier = Modifier.fillMaxWidth().height(200.dp), placeholder = { Text("Conte a história...") })
        }
        SectionCard(title = "Notas Gerais") {
            OutlinedTextField(value = p.notas, onValueChange = { viewModel.atualizarNotas(it) },
                modifier = Modifier.fillMaxWidth().height(150.dp), placeholder = { Text("Anotações diversas...") })
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

