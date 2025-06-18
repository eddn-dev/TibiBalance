

/* :app/ui/components/containers/ConnectWatchCard.kt */
package com.app.tibibalance.ui.components.containers

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.buttons.PrimaryButton
import com.app.tibibalance.ui.components.texts.Subtitle

@Composable
fun HealthPermissionsCard(
    onGrantClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.large)
            .clickable(onClick = onGrantClick)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.ic_fruit_watch),
                contentDescription = "Personaje con reloj",
                modifier = Modifier
                    .size(80.dp)
                    .padding(8.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Subtitle("Activa permisos de salud")
                Text("Necesitamos acceso a pasos, calorías y frecuencia cardiaca para mostrar tu progreso.")
                PrimaryButton(text = "Activar", onClick = onGrantClick)
            }
        }
    }
}
