package com.app.wear.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.app.wear.R
import com.app.wear.presentation.viewmodel.WearMetricsUiState
import com.app.wear.presentation.viewmodel.WearMetricsViewModel

/**
 * @brief Pantalla principal de Wear OS con el gradiente de fondo y la imagen.
 *        Integra la lógica de estados (Loading/Success/Error) proveniente del ViewModel.
 *
 * @param metricsViewModel Hilt-inyecta el ViewModel que expone uiState.
 */
@Composable
fun WearAppScreen(
    metricsViewModel: WearMetricsViewModel = hiltViewModel()
) {
    // Obsérvese el estado UI proveniente del ViewModel
    val uiState by metricsViewModel.uiState.collectAsState()

    // Definición del gradiente vertical
    val gradient= Brush.verticalGradient(
        colors = listOf(
            Color(0xFF3EA8FE).copy(alpha = 0.45f),
            Color.White
        )
    )

    // Contenedor principal con gradiente
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen superior (si está disponible en drawable)
            Image(
                painter = painterResource(id = R.drawable.tibiowatchimage),
                contentDescription = "Icono de reloj TibiBalance",
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 12.dp),
                contentScale = ContentScale.Fit
            )

            // Título (opcional, si se desea mantener texto estático)
            Text(
                text = "TibiBalance Watch",
                style = MaterialTheme.typography.title3,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Renderizado condicional según el estado
            when (val state = uiState) {
                is WearMetricsUiState.Loading -> {
                    CircularProgressIndicator()
                    Text(
                        text = "Cargando datos...",
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                is WearMetricsUiState.Success -> {
                    // Muestra métricas (pasos y frecuencia cardíaca)
                    Text(
                        text = "Pasos: ${state.steps}",
                        style = MaterialTheme.typography.body1
                    )
                    Text(
                        text = "Ritmo Cardíaco: ${state.heartRate ?: "N/A"}",
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón para enviar métricas
                    Button(
                        onClick = { metricsViewModel.onSendMetricsClicked() },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(40.dp)
                    ) {
                        Text("Enviar Métricas")
                    }

                    // Texto de estado posterior al envío
                    if (state.lastSentStatus.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.lastSentStatus,
                            style = MaterialTheme.typography.caption2,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                is WearMetricsUiState.Error -> {
                    // Mensaje de error con opción para reintentar
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Button(onClick = { metricsViewModel.onSendMetricsClicked() }) {
                        Text("Reintentar")
                    }
                }
            }
        }
    }
}
