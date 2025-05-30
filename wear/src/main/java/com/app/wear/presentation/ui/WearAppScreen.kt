package com.app.wear.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import com.app.wear.presentation.viewmodel.WearMetricsUiState
import com.app.wear.presentation.viewmodel.WearMetricsViewModel

@Composable
fun WearAppScreen(viewModel: WearMetricsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (state) {
            is WearMetricsUiState.Loading -> {
                Text("Loading...", textAlign = TextAlign.Center)
            }
            is WearMetricsUiState.Success -> {
                val s = state as WearMetricsUiState.Success
                Text("Steps: ${'$'}{s.steps}")
                Text("HR: ${'$'}{s.heartRate}")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.sendMetrics() }) {
                    Text("Send")
                }
            }
        }
    }
}
