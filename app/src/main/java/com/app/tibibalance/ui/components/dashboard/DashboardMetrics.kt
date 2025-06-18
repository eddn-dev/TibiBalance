/* ui/components/dashboard/DashboardMetrics.kt */
package com.app.tibibalance.ui.components.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.domain.entities.DashboardSnapshot
import com.app.tibibalance.ui.components.containers.StatContainer
import java.text.NumberFormat
import java.util.Locale

/* ––––– helpers ––––– */
private val numberFmt by lazy { NumberFormat.getIntegerInstance(Locale.getDefault()) }
private fun Int.formatInt(): String = numberFmt.format(this)

private fun formatHrAge(ms: Long): String = when {
    ms == Long.MAX_VALUE      -> "Sin datos"
    ms < 60_000               -> "hace ${(ms / 1_000)} s"
    ms < 3_600_000            -> "hace ${(ms / 60_000)} min"
    ms < 86_400_000           -> "hace ${(ms / 3_600_000)} h"
    else                      -> "hace ${(ms / 86_400_000)} días"
}

/* ––––– Composable ––––– */
@Composable
fun DashboardMetrics(
    snapshot: DashboardSnapshot,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        /* fila superior: Pasos + Kcal */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatContainer(
                icon   = Icons.AutoMirrored.Filled.DirectionsRun,
                value  = snapshot.stepsToday.formatInt(),
                label  = "Pasos hoy",
                modifier = Modifier.weight(1f)
            )
            StatContainer(
                icon   = Icons.Filled.Whatshot,
                value  = "${snapshot.kcalToday.formatInt()} kcal",
                label  = "Calorías",
                modifier = Modifier.weight(1f)
            )
        }

        /* fila inferior: Frecuencia cardíaca (ancho completo) */
        StatContainer(
            icon      = Icons.Filled.Favorite,
            iconTint  = Color.Red,
            value     = snapshot.heartRate?.let { "$it bpm" } ?: "--",
            label     = formatHrAge(snapshot.hrAgeMillis),
            modifier  = Modifier.fillMaxWidth()
        )
    }
}

/* ––––– Preview ––––– */
@Preview(showBackground = true)
@Composable
private fun DashboardMetricsPreview() {
    MaterialTheme {
        DashboardMetrics(
            snapshot = DashboardSnapshot(
                stepsToday  = 8540,
                kcalToday   = 630,
                heartRate   = 78,
                hrAgeMillis = 32_000
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
