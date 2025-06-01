/*
 * @file    SuggestionStep.kt
 * @ingroup ui_wizard_addHabit
 */
package com.app.tibibalance.ui.screens.habits.addHabitWizard.step

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.app.domain.entities.Habit
import com.app.tibibalance.ui.components.buttons.RoundedIconButton
import com.app.tibibalance.ui.components.inputs.iconByName
import com.app.tibibalance.ui.components.texts.Title

/**
 * Paso 0 — Biblioteca de sugerencias.
 *
 * @param suggestions  Lista de hábitos sugeridos (plantillas).
 * @param onSuggestion  Callback cuando se selecciona una sugerencia.
 * @param onCustom      Callback “Crear hábito personalizado”.
 */
@Composable
fun SuggestionStep(
    suggestions: List<Habit>,
    onSuggestion: (Habit) -> Unit,
    onCustom: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        /* ---------- Encabezado ---------- */
        Title(
            text = "Hábitos sugeridos",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = MaterialTheme.colorScheme.onSurface
        )

        /* ---------- Lista ---------- */
        if (suggestions.isEmpty()) {
            // Placeholder sencillo mientras llega la lista desde Firestore/Room.
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Cargando plantillas…", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)            // Deja espacio para el botón inferior
            ) {
                items(suggestions, key = { it.id.raw }) { tpl ->
                    Card(
                        onClick = { onSuggestion(tpl) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                        ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            /* --- icono + nombre --- */
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = rememberVectorPainter(iconByName(tpl.icon)),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(tpl.name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.inverseOnSurface)
                            }

                            /* --- botón “añadir” --- */
                            RoundedIconButton(
                                onClick = { onSuggestion(tpl) },
                                icon = Icons.Default.Add
                            )
                        }
                    }
                }
            }
        }
    }
}
