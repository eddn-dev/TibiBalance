package com.app.tibibalance.ui.components.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.tibibalance.ui.components.buttons.SwitchToggle

@Composable
fun ToggleRow(
    label: String,
    checked: Boolean,
    onToggle: () -> Unit,
    trailing: @Composable () -> Unit = {} // Slot para contenido al final, antes del Switch
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onToggle() }, // Hace toda la fila clicable para alternar el switch.
        verticalAlignment = Alignment.CenterVertically, // Centra elementos verticalmente.
        horizontalArrangement = Arrangement.SpaceBetween // Espacia el texto y el grupo switch+trailing.
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium) // Etiqueta del toggle.
        Row(verticalAlignment = Alignment.CenterVertically) { // Fila para agrupar trailing y Switch.
            trailing() // Renderiza el contenido del slot 'trailing'.
            Spacer(Modifier.width(4.dp)) // Pequeño espacio.
            SwitchToggle(checked = checked, onCheckedChange = { onToggle() }) // El interruptor.
        }
    }
}