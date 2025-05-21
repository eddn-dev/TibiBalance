/* ui/screens/habits/addHabitWizard/step/SuggestionStep.kt
package com.app.tibibalance.ui.screens.habits.addHabitWizard.step

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.app.domain.entities.HabitTemplate
import com.app.tibibalance.ui.components.buttons.RoundedIconButton
import com.app.tibibalance.ui.components.inputs.iconByName
import com.app.tibibalance.ui.components.texts.Title

/**
 * Primer paso del asistente: muestra la biblioteca de plantillas.
 */
@Composable
fun SuggestionStep(
    templates: List<HabitTemplate>,
    onSuggestion: (HabitTemplate) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Title(
            text = "Hábitos sugeridos",
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(templates, key = { it.id }) { tpl ->
                Card(
                    onClick = { onSuggestion(tpl) },                 // ← firma correcta Material3
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        /* --- icono + nombre de la plantilla --- */
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = rememberVectorPainter(iconByName(tpl.icon)),
                                contentDescription = null,           // decorativo
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(tpl.name, style = MaterialTheme.typography.bodyLarge)
                        }

                        /* --- botón añadir --- */
                        RoundedIconButton(
                            onClick = { onSuggestion(tpl) },
                            icon = Icons.Default.Add               // contentDescription opcional
                        )
                    }
                }
            }
        }
    }
}
*/