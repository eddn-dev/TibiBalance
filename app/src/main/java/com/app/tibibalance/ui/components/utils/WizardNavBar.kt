package com.app.tibibalance.ui.components.utils

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.tibibalance.ui.components.buttons.PrimaryButton
import com.app.tibibalance.ui.components.buttons.SecondaryButton

/**
 * Barra inferior genérica para los wizards de **alta / edición**.
 *
 * - Muestra **Cancelar** siempre que el wizard esté en modo edición.
 * - El botón **Atrás** aparece a partir del paso 1.
 * - **Siguiente** se oculta si: `onNext == null` → flujo lineal truncado
 *   (p. ej. cuando el hábito está en reto).
 * - En el paso final (o cuando no hay `onNext`) se muestra **Guardar**.
 *
 * @param step        Paso actual (0-based: 0..3).
 * @param stepValid   `true` si los campos del paso son válidos.
 * @param saving      `true` mientras se persisten los cambios.
 * @param notifyOn    Si `false` y estamos en el paso 2 se permite guardar directo.
 * @param onBack      Callback botón «Atrás».
 * @param onNext      *Opcional* «Siguiente».  Cuando es `null` el botón se omite.
 * @param onSave      Callback «Guardar».
 * @param onCancel    Callback «Cancelar / Cerrar sin guardar».
 */
@Composable
fun WizardNavBar(
    step      : Int,
    stepValid : Boolean,
    saving    : Boolean,
    notifyOn  : Boolean,
    onBack    : (() -> Unit)? = null,
    onNext    : (() -> Unit)? = null,
    onSave    : () -> Unit,
    onCancel  : () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        if (step > 0 && onBack != null) {
            SecondaryButton(
                text    = "Atrás",
                onClick = { if (!saving) onBack() },
                enabled = !saving
            )
        }

        Spacer(Modifier.weight(1f))

        /* ------------------- acción principal a la derecha -------------- */
        val showSave =
            /* último paso normal */       step == 3 ||
                /* flujo sin onNext */         onNext == null ||
                /* guardar directo (paso 2 sin notificaciones) */
                (step == 2 && !notifyOn)

        if (showSave) {
            PrimaryButton(
                text      = "Guardar",
                isLoading = saving,
                enabled   = stepValid && !saving,
                onClick   = onSave
            )
        } else {                       // botón “Siguiente”
            PrimaryButton(
                text      = "Siguiente",
                enabled   = stepValid && !saving,
                onClick   = onNext!!           // sólo llega aquí si no es null
            )
        }
    }
}
