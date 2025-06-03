package com.app.tibibalance.ui.screens.home.activities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.domain.enums.ActivityStatus
import com.app.tibibalance.ui.components.buttons.PrimaryButton
import com.app.tibibalance.ui.components.buttons.SecondaryButton
import com.app.tibibalance.ui.components.containers.ModalContainer
import com.app.tibibalance.ui.components.inputs.QuantitySlider
import com.app.tibibalance.ui.components.texts.Title

@Composable
fun ActivityLogDialog(
    ui        : ActivityUi,
    onDismiss : () -> Unit,
    onConfirm : (qty: Int?, status: ActivityStatus) -> Unit
) {
    val target = ui.act.targetQty          // null → hábito binario

    /* ── estado ── */
    var qty by remember(ui) {
        mutableStateOf(ui.act.recordedQty ?: target ?: 0)
    }
    var completed by remember(ui) {
        mutableStateOf(ui.act.status == ActivityStatus.COMPLETED)
    }

    /* actualizar checkbox cuando la barra cambia */
    if (target != null) {
        completed = qty >= target
    }

    /* error de rango */
    val qtyError = target != null && (qty < 0 || qty > target)

    ModalContainer(onDismissRequest = onDismiss) {

        Title(ui.name)
        Text(
            ui.act.scheduledTime.toString() ?: "Cualquier momento",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))

        if (target != null) {
            /* ---------- cantidad ---------- */
            Text("Progreso: $qty / $target")

            QuantitySlider(
                qty         = qty,
                onQtyChange = { qty = it },
                target      = target
            )

            /* ---------- checkbox ---------- */
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = completed,
                    onCheckedChange = { chk ->
                        completed = chk
                        qty = if (chk) target else 0          // ⚑ cambio clave
                    }
                )
                Spacer(Modifier.width(4.dp))
                Text("Objetivo cumplido")
            }

            if (qtyError)
                Text("Valor fuera de rango", color = MaterialTheme.colorScheme.error)

        } else {
            /* ---------- hábito binario ---------- */
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = completed,
                    onCheckedChange = { completed = it }
                )
                Spacer(Modifier.width(4.dp))
                Text("Hecho")
            }
        }

        Spacer(Modifier.height(24.dp))

        /* ---------- botones ---------- */
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            SecondaryButton("Cancelar", onDismiss)
            Spacer(Modifier.width(8.dp))
            PrimaryButton(
                text = "Guardar",
                enabled = !qtyError,
                onClick = {
                    val status = when {
                        completed                      -> ActivityStatus.COMPLETED
                        target != null && qty in 1 until target -> ActivityStatus.PARTIALLY_COMPLETED
                        else                           -> ActivityStatus.MISSED
                    }
                    onConfirm(qty, status)
                }
            )
        }
    }
}
