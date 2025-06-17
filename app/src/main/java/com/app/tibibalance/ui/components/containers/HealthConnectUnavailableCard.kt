/* HealthConnectUnavailableCard.kt */
package com.app.tibibalance.ui.components.containers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.tibibalance.ui.components.buttons.PrimaryButton
import com.app.tibibalance.ui.components.texts.Subtitle

@Composable
fun HealthConnectUnavailableCard(
    onInstallClick: () -> Unit,
    modifier: Modifier = Modifier
) = Card(
    modifier = modifier,
    colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    shape    = CardDefaults.shape
) {
    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Subtitle("Health Connect no está disponible")
        Text("Instálalo desde Play Store para desbloquear tus métricas.")
        PrimaryButton(text = "Descargar", onClick = onInstallClick)
    }
}


