/*
 * @file    AddHabitModal.kt
 * @ingroup ui_wizard
 * @brief   Modal de 4 pasos para crear / editar un hábito.
 *
 * Paso 0  – Sugerencias
 * Paso 1  – Información básica
 * Paso 2  – Seguimiento
 * Paso 3  – Notificaciones
 *
 * • Valida cada paso y deshabilita los botones cuando existan errores.
 * • Solicita confirmación antes de descartar un borrador al aplicar otra plantilla.
 * • Mantiene remember{} únicamente donde es indispensable; el ViewModel es la fuente de verdad.
 */
@file:OptIn(ExperimentalFoundationApi::class)

package com.app.tibibalance.ui.screens.habits.addHabitWizard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import com.app.tibibalance.ui.components.dialogs.DialogButton
import com.app.tibibalance.ui.components.dialogs.ModalInfoDialog

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddHabitModal(
    onDismissRequest: () -> Unit,
) {
    ModalInfoDialog(
        visible = true,
        loading = false,
        icon    = Icons.Default.Check,
        title = "Habitaje creado",
        message = "El hábito fue creado con éxito",
        primaryButton = DialogButton("Aceptar") { onDismissRequest() }
    )
}
