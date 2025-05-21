/* ui/screens/habits/addHabitWizard/step/BasicInfoStep.kt
package com.app.tibibalance.ui.screens.habits.addHabitWizard.step

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.domain.enums.HabitCategory
import com.app.domain.entities.HabitForm
import com.app.domain.error.BasicError
import com.app.tibibalance.ui.components.inputs.*
import com.app.tibibalance.ui.components.texts.Title
import com.app.tibibalance.ui.screens.habits.addHabitWizard.HabitFormSaver

/**
 * Paso ➊ — Información mínima para crear un hábito.
 */
@Composable
fun BasicInfoStep(
    initial      : HabitForm,
    errors       : List<BasicError>,
    onFormChange : (HabitForm) -> Unit,
    onBack       : () -> Unit = {}
) {
    var form by rememberSaveable(stateSaver = HabitFormSaver) { mutableStateOf(initial) }

    /* Sincroniza con el ViewModel */
    LaunchedEffect(form) { onFormChange(form) }

    val nameErr = BasicError.NameRequired in errors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Title(
            text = "Información básica",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        /* Icono */
        InputIcon(
            iconName    = form.icon,
            isEditing   = true,
            description = "Icono del hábito",
            modifier    = Modifier.align(Alignment.CenterHorizontally),
            onChange    = { form = form.copy(icon = it) }
        )

        /* Nombre (obligatorio) */
        InputText(
            value           = form.name,
            onValueChange   = { form = form.copy(name = it) },
            placeholder     = "Nombre del hábito *",
            maxChars        = 30,
            isError         = nameErr,
            supportingText  = if (nameErr) "Obligatorio" else null,
            modifier        = Modifier.fillMaxWidth()
        )

        /* Descripción (opcional) */
        InputText(
            value         = form.desc,
            onValueChange = { form = form.copy(desc = it) },
            placeholder   = "Descripción",
            singleLine    = false,
            maxChars      = 140,
            modifier      = Modifier
                .fillMaxWidth()
                .heightIn(min = 96.dp)
        )

        /* Categoría */
        val categories = remember { HabitCategory.entries }
        InputSelect(
            label            = "Categoría *",
            options          = categories.map { it.name },
            selectedOption   = form.category.name,
            onOptionSelected = { display ->
                form = form.copy(category = categories.first { it.name == display })
            }
        )
    }
}
*/