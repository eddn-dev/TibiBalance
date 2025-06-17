package com.app.tibibalance.ui.components.containers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.tibibalance.ui.components.buttons.PrimaryButton
import com.app.tibibalance.ui.components.texts.Subtitle

/* HealthPermissionsCard.kt */
@Composable
fun HealthPermissionsCard(
    onGrantClick: () -> Unit,
    modifier: Modifier = Modifier
) = Card(
    modifier = modifier,
    colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    shape    = CardDefaults.shape
) {
    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Subtitle("Activa permisos de salud")
        Text("Necesitamos acceso a pasos, calorías y frecuencia cardiaca para mostrar tu progreso.")
        PrimaryButton(text = "Activar", onClick = onGrantClick)
    }
}