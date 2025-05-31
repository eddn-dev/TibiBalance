package com.app.wear.presentation.ui // Asegúrate que el paquete sea correcto

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
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.wear.presentation.viewmodel.WearMetricsUiState
import com.app.wear.presentation.viewmodel.WearMetricsViewModel
// It seems R.drawable.tibiowatchimage is not used in the new version.
// If it was, an import like import com.app.wear.R would be needed,
// but the new UI structure doesn't include the image.

@Composable
fun WearAppScreen(
    // Hilt inyectará el ViewModel aquí
    metricsViewModel: WearMetricsViewModel = hiltViewModel()
) {
    val uiState by metricsViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("TibiBalance Watch", style = MaterialTheme.typography.title3)
        Spacer(modifier = Modifier.height(12.dp))

        when (val state = uiState) {
            is WearMetricsUiState.Loading -> {
                CircularProgressIndicator()
                Text("Cargando datos...", modifier = Modifier.padding(top = 8.dp))
            }
            is WearMetricsUiState.Success -> {
                Text("Pasos: ${state.steps}", style = MaterialTheme.typography.body1)
                Text("Ritmo Cardíaco: ${state.heartRate ?: "N/A"}", style = MaterialTheme.typography.body1)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { metricsViewModel.onSendMetricsClicked() }) {
                    Text("Enviar Métricas")
                }
                if (state.lastSentStatus.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.lastSentStatus,
                        style = MaterialTheme.typography.caption2,
                        textAlign = TextAlign.Center
                    )
                }
            }
            is WearMetricsUiState.Error -> {
                Text("Error: ${state.message}", color = MaterialTheme.colors.error)
                 Button(onClick = { /* Lógica para reintentar, si aplica */ }) {
                    Text("Reintentar")
                }
            }
        }
    }
}

// The old Preview is not compatible as it doesn't use the ViewModel.
// A new preview would require providing a mock ViewModel or disabling ViewModel creation in preview.
// For now, I will remove the old preview. A new one can be added later if needed.
// @Preview(showBackground = true, widthDp = 200, heightDp = 200)
// @Composable
// fun WearAppScreenPreview() {
//     // This preview would need a way to provide a WearMetricsViewModel
//     // or a mock version of WearMetricsUiState.
//     // WearAppScreen() // This won't work directly with Hilt ViewModels in Preview
// }
