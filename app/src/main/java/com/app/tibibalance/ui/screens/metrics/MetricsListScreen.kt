package com.app.tibibalance.ui.screens.metrics

/**
 * @file MetricsListScreen.kt
 * @brief Interfaz de Jetpack Compose para mostrar las métricas guardadas.
 */

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.domain.entities.DailyMetrics

/**
 * @file MetricsListScreen.kt
 * @brief Pantalla que muestra las métricas almacenadas.
 */
@Composable
fun MetricsListScreen(
    navController: NavHostController,
    viewModel: MetricsViewModel = hiltViewModel()
) {
    val metrics by viewModel.metrics.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(metrics) { item -> MetricRow(item) }
        }
    }
}

@Composable
private fun MetricRow(metric: DailyMetrics) {
    ListItem(
        headlineText = { Text(metric.date.toString()) },
        supportingText = { Text("Pasos: ${metric.steps}") }
    )
}

