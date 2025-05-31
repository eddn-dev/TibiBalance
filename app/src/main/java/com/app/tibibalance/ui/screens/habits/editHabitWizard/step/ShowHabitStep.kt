/* ui/screens/habits/editHabitWizard/step/ShowHabitStep.kt */
package com.app.tibibalance.ui.screens.habits.editHabitWizard.step

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.domain.entities.Habit
import com.app.domain.config.Repeat
import com.app.tibibalance.ui.components.buttons.DangerButton
import com.app.tibibalance.ui.components.inputs.InputIcon
import com.app.tibibalance.ui.components.texts.Title
import com.app.tibibalance.ui.components.buttons.PrimaryButton
import com.app.tibibalance.ui.components.utils.ToggleRow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
private val dateFmt = DateTimeFormatter.ofPattern("d LLL uuuu")

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ShowHabitStep(
    habit         : Habit,
    onEditNotif   : () -> Unit,
    onToggleNotif : (Boolean) -> Unit,
    onDelete      : () -> Unit,
    deleting      : Boolean
) {
    /* helpers -------------------------------------------------------- */
    val repeatTxt = remember(habit.repeat) { repeatAsText(habit.repeat) }
    val createdOn = remember(habit.meta.createdAt) {
        habit.meta.createdAt
            ?.toLocalDateTime(TimeZone.currentSystemDefault())
            ?.date
            ?.toJavaLocalDate()         // ⇐ conversión a java.time.LocalDate
            ?.format(dateFmt) ?: "—"
    }

    /* UI ------------------------------------------------------------- */
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InputIcon(habit.icon, {}, isEditing = false, description = "Icono")
        Title(habit.name)
        if (habit.description.isNotBlank()) Text(habit.description)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement   = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AssistChip({}, label = { Text(habit.category.name) })
            AssistChip({}, label = { Text(repeatTxt) })
            AssistChip({}, label = { Text("Creado: $createdOn") })
        }

        ToggleRow(
            label    = "Notificaciones",
            checked  = habit.notifConfig.enabled,
            onToggle = { onToggleNotif(!habit.notifConfig.enabled) }
        )

        PrimaryButton(
            text     = "Editar notificaciones",
            onClick  = onEditNotif,
            modifier = Modifier.fillMaxWidth()
        )

        DangerButton(
            text       = "Eliminar hábito",
            onClick    = onDelete,
            isLoading  = deleting,
            modifier   = Modifier.fillMaxWidth()
        )
    }
}

/* helper Repeat → texto legible ------------------------------------- */
@RequiresApi(Build.VERSION_CODES.O)
private fun repeatAsText(r: Repeat): String = when (r) {
    Repeat.None              -> "Sin repetición"
    is Repeat.Daily          -> if (r.every == 1) "Diario" else "Cada ${r.every} días"
    is Repeat.Weekly         -> "Semanal (${r.days.joinToString { it.name.first().toString() }})"
    is Repeat.Monthly        -> "Mensual (día ${r.dayOfMonth})"
    is Repeat.MonthlyByWeek  ->
        "Mensual (${r.occurrence.name.lowercase()} ${r.dayOfWeek.name.lowercase()})"
    is Repeat.Yearly         -> "Anual (${r.day}/${r.month})"
    is Repeat.BusinessDays   -> if (r.every == 1) "Laboral diario" else "Cada ${r.every} días laborables"
}
