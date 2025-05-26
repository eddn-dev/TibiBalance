/*
 * @file    BasicInfoStep.kt
 * @ingroup ui_wizard_addHabit
 */
package com.app.tibibalance.ui.screens.habits.addHabitWizard.step

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.domain.model.HabitForm
import com.app.domain.enums.HabitCategory
import com.app.tibibalance.ui.components.inputs.*
import com.app.tibibalance.ui.components.texts.Title

/**
 * Paso 1 — Información básica del hábito.
 *
 * @param form      Estado actual del [HabitForm].
 * @param onForm    Callback que envía el formulario actualizado al ViewModel.
 */
@Composable
fun BasicInfoStep(
    form: HabitForm,
    onForm: (HabitForm) -> Unit
) {
    /* -------- estado local editable -------- */
    var localForm by remember(form) { mutableStateOf(form) }

    /*  Sincroniza con ViewModel al cambiar cualquier campo  */
    LaunchedEffect(localForm) { onForm(localForm) }

    /*  Flag de error para el nombre  */
    val nameError = localForm.name.isBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        /* -------- Título -------- */
        Title(
            text = "Información básica",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            textAlign = TextAlign.Center
        )

        /* -------- Icono -------- */
        InputIcon(
            iconName    = localForm.icon,
            isEditing   = true,
            description = "Icono del hábito",
            modifier    = Modifier.align(Alignment.CenterHorizontally),
            onChange    = { newIcon -> localForm = localForm.copy(icon = newIcon) }
        )

        /* -------- Nombre (obligatorio) -------- */
        InputText(
            value           = localForm.name,
            onValueChange   = { localForm = localForm.copy(name = it) },
            placeholder     = "Nombre del hábito *",
            maxChars        = 30,
            isError         = nameError,
            supportingText  = if (nameError) "Obligatorio" else null,
            modifier        = Modifier.fillMaxWidth()
        )

        /* -------- Descripción -------- */
        InputText(
            value         = localForm.desc,
            onValueChange = { localForm = localForm.copy(desc = it) },
            placeholder   = "Descripción",
            singleLine    = false,
            maxChars      = 140,
            modifier      = Modifier
                .fillMaxWidth()
                .heightIn(min = 96.dp)
        )

        /* -------- Categoría -------- */
        val categories = remember { HabitCategory.entries }
        InputSelect(
            label            = "Categoría *",
            options          = categories.map { it.name },
            selectedOption   = localForm.category.name,
            onOptionSelected = { display ->
                localForm = localForm.copy(category = categories.first { it.name == display })
            }
        )

        /* -------- Nota UX -------- */
        if (nameError) {
            Text(
                text = "Debes ingresar un nombre para continuar.",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.error)
            )
        }
    }
}
